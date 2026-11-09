package com.tenli.oneview.ui.features.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tenli.oneview.BuildConfig
import com.tenli.oneview.TenliApp
import com.tenli.oneview.data.local.CachedHomeData
import com.tenli.oneview.data.local.UserPermissionManager
import com.tenli.oneview.data.network.websocket.WebSocketManager
import com.tenli.oneview.data.repository.DashboardRepository
import com.tenli.oneview.model.network.AIServiceModel
import com.tenli.oneview.model.network.CameraModel
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.model.network.VmsCountOverviewModel
import com.tenli.oneview.model.network.VmsEventCountByTypeModel
import com.tenli.oneview.ui.features.event.permittedDistinct
import com.tenli.oneview.ui.features.event.uniqueKey
import com.tenli.oneview.ui.utils.AiTypeHelper
import com.tenli.oneview.util.EventBus
import com.tenli.oneview.util.NetworkMonitor
import com.tenli.oneview.util.safeApiCall
import com.tenli.oneview.util.toUserFriendlyMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class TimeFilter(val title: String, val titleEn: String = title) {
    TODAY("Hôm nay", "Today"),
    YESTERDAY("Hôm qua", "Yesterday"),
    LAST_7_DAYS("7 ngày qua", "Last 7 days"),
    CUSTOM_DATE("Tùy chọn ngày", "Select date"),
    CUSTOM_RANGE("Tùy chỉnh", "Custom");

    companion object {
        private val dateFormatThreadLocal = object : ThreadLocal<SimpleDateFormat>() {
            override fun initialValue(): SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        }
        private val dateTimeFormatThreadLocal = object : ThreadLocal<SimpleDateFormat>() {
            override fun initialValue(): SimpleDateFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
        }

        @Volatile
        var customDateStartMs: Long = run {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }

        @Volatile
        var customDateEndMs: Long = run {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            cal.timeInMillis
        }

        @Volatile
        var customRangeFromMs: Long = System.currentTimeMillis() - 24L * 3600L * 1000L

        @Volatile
        var customRangeToMs: Long = System.currentTimeMillis()

        fun setCustomDate(dayMillis: Long) {
            val cal = Calendar.getInstance().apply { timeInMillis = dayMillis }
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            customDateStartMs = cal.timeInMillis
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            customDateEndMs = cal.timeInMillis
        }

        fun setCustomRange(fromMillis: Long, toMillis: Long) {
            if (fromMillis <= toMillis) {
                customRangeFromMs = fromMillis
                customRangeToMs = toMillis
            } else {
                customRangeFromMs = toMillis
                customRangeToMs = fromMillis
            }
        }

        fun cacheKeyToken(filter: TimeFilter): String {
            return when (filter) {
                CUSTOM_DATE -> "${filter.name}_${customDateStartMs}"
                CUSTOM_RANGE -> "${filter.name}_${customRangeFromMs}_${customRangeToMs}"
                else -> filter.name
            }
        }

        fun resolveTimeRangeMillis(filter: TimeFilter): Pair<Long, Long> {
            val calendar = Calendar.getInstance()
            return when (filter) {
                TODAY -> {
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val fromTime = calendar.timeInMillis
                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                    calendar.set(Calendar.MINUTE, 59)
                    calendar.set(Calendar.SECOND, 59)
                    calendar.set(Calendar.MILLISECOND, 999)
                    Pair(fromTime, calendar.timeInMillis)
                }
                YESTERDAY -> {
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val toTime = calendar.timeInMillis - 1
                    calendar.add(Calendar.DAY_OF_YEAR, -1)
                    Pair(calendar.timeInMillis, toTime)
                }
                LAST_7_DAYS -> {
                    val toTime = calendar.timeInMillis
                    calendar.add(Calendar.DAY_OF_YEAR, -7)
                    Pair(calendar.timeInMillis, toTime)
                }
                CUSTOM_DATE -> Pair(customDateStartMs, customDateEndMs)
                CUSTOM_RANGE -> Pair(customRangeFromMs, customRangeToMs)
            }
        }

        fun formatDisplayLabel(filter: TimeFilter, isEn: Boolean): String {
            return when (filter) {
                TODAY -> if (isEn) TODAY.titleEn else TODAY.title
                YESTERDAY -> if (isEn) YESTERDAY.titleEn else YESTERDAY.title
                LAST_7_DAYS -> if (isEn) LAST_7_DAYS.titleEn else LAST_7_DAYS.title
                CUSTOM_DATE -> {
                    val prefix = if (isEn) CUSTOM_DATE.titleEn else CUSTOM_DATE.title
                    val dateFmt = dateFormatThreadLocal.get()!!
                    "$prefix (${dateFmt.format(Date(customDateStartMs))})"
                }
                CUSTOM_RANGE -> {
                    val dateTimeFmt = dateTimeFormatThreadLocal.get()!!
                    "${dateTimeFmt.format(Date(customRangeFromMs))} - ${dateTimeFmt.format(Date(customRangeToMs))}"
                }
            }
        }
    }
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val selectedFilter: TimeFilter = TimeFilter.TODAY,
    val selectedServiceId: Int? = null,
    val overviewStats: List<VmsCountOverviewModel> = emptyList(),
    val eventsByType: List<VmsEventCountByTypeModel> = emptyList(),
    val cameraList: List<CameraModel> = emptyList(),
    val aiServices: List<AIServiceModel> = emptyList(),
    val recentEvents: List<EventData> = emptyList(),
    val cameraStatusMap: Map<String, String> = emptyMap(),
    val recordStatusMap: Map<String, String> = emptyMap(),
    val totalDetectionsCount: Long = 0L,
    val totalCamerasCount: Int = 0,
    val onlineCamerasCount: Int = 0,
    val recordingCamerasCount: Int = 0,
    val runningAiTasks: Int = 0,
    val totalAiTasks: Int = 0,
    val isListView: Boolean = true,
    val error: String? = null
)

@OptIn(FlowPreview::class)
class HomeViewModel(
    application: Application,
    private val dashboardRepository: DashboardRepository,
    private val webSocketManager: WebSocketManager
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(buildInitialState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var dashboardJob: Job? = null
    private var recentEventsJob: Job? = null
    @Volatile private var isHomeTabActive: Boolean = true
    @Volatile private var pendingNotifyRefresh: Boolean = false
    @Volatile private var pendingCameraStatusRecompute: Boolean = false

    init {
        fetchDashboardData()
        listenToWebSocket()
        webSocketManager.connectAllReports()
    }

    fun setHomeTabActive(active: Boolean) {
        if (isHomeTabActive == active) return
        isHomeTabActive = active
        webSocketManager.setReportsActive(active)
        if (!active) {
            recentEventsJob?.cancel()
            return
        }
        if (pendingCameraStatusRecompute) {
            pendingCameraStatusRecompute = false
            viewModelScope.launch(Dispatchers.Default) {
                val camMap = webSocketManager.cameraStatusMap.value
                val recMap = webSocketManager.recordStatusMap.value
                _uiState.update {
                    it.copy(cameraStatusMap = camMap, recordStatusMap = recMap).withRecomputedCameraMetrics()
                }
            }
        }
        if (pendingNotifyRefresh && _uiState.value.selectedFilter == TimeFilter.TODAY) {
            pendingNotifyRefresh = false
            fetchRecentEvents()
        }
    }

    private fun buildInitialState(): HomeUiState {
        val isList = dashboardRepository.isListViewMode()
        val memCached = dashboardRepository.getHomeDataInMemory(TimeFilter.TODAY, null)
        return if (memCached != null) {
            val rawCameras = memCached.cameraList.ifEmpty { dashboardRepository.getCamerasInMemory() }
            val permittedCameras = UserPermissionManager.filterPermittedCameras(rawCameras)
            val services = memCached.aiServices.ifEmpty { dashboardRepository.getAiServicesInMemory() }
            HomeUiState(
                isLoading = false,
                isListView = isList,
                overviewStats = memCached.overviewStats,
                eventsByType = memCached.eventsByType,
                cameraList = permittedCameras,
                aiServices = services,
                recentEvents = memCached.recentEvents.permittedDistinct(rawCameras)
                    .take(MAX_RECENT_EVENTS),
                runningAiTasks = memCached.runningAiTasks,
                totalAiTasks = memCached.totalAiTasks
            ).withRecomputedMetrics()
        } else {
            val rawCameras = dashboardRepository.getCamerasInMemory()
            val permittedCameras = UserPermissionManager.filterPermittedCameras(rawCameras)
            HomeUiState(
                isLoading = true,
                isListView = isList,
                cameraList = permittedCameras,
                aiServices = dashboardRepository.getAiServicesInMemory()
            ).withRecomputedMetrics()
        }
    }

    fun toggleViewMode() {
        val nextIsList = !_uiState.value.isListView
        dashboardRepository.setListViewMode(nextIsList)
        _uiState.update { it.copy(isListView = nextIsList) }
    }

    fun syncViewModeFromPrefs() {
        val savedIsList = dashboardRepository.isListViewMode()
        if (_uiState.value.isListView != savedIsList) {
            _uiState.update { it.copy(isListView = savedIsList) }
        }
    }

    fun onEventSelected(event: EventData) {
        val cur = _uiState.value
        dashboardRepository.cacheClickedEvent(
            event = event,
            cameras = cur.cameraList,
            aiServices = cur.aiServices
        )
    }

    private fun listenToWebSocket() {
        viewModelScope.launch {
            webSocketManager.notifyEvent
                .debounce(NOTIFY_DEBOUNCE_MS)
                .collect { _ ->
                    if (_uiState.value.selectedFilter == TimeFilter.TODAY) {
                        if (isHomeTabActive) {
                            fetchRecentEvents()
                        } else {
                            pendingNotifyRefresh = true
                        }
                    }
                }
        }
        viewModelScope.launch(Dispatchers.Default) {
            webSocketManager.cameraStatusMap
                .debounce(STATUS_DEBOUNCE_MS)
                .collect { statusMap ->
                    if (isHomeTabActive) {
                        _uiState.update { it.copy(cameraStatusMap = statusMap).withRecomputedCameraMetrics() }
                    } else {
                        pendingCameraStatusRecompute = true
                    }
                }
        }
        viewModelScope.launch(Dispatchers.Default) {
            webSocketManager.recordStatusMap
                .debounce(STATUS_DEBOUNCE_MS)
                .collect { recMap ->
                    if (isHomeTabActive) {
                        _uiState.update { it.copy(recordStatusMap = recMap).withRecomputedCameraMetrics() }
                    } else {
                        pendingCameraStatusRecompute = true
                    }
                }
        }
        viewModelScope.launch {
            EventBus.eventReportedFalse.collect { eventId ->
                _uiState.update { state ->
                    state.copy(recentEvents = state.recentEvents.filter { it.id != eventId })
                }
            }
        }
        viewModelScope.launch {
            EventBus.networkRestored.collect {
                fetchDashboardData()
            }
        }
        viewModelScope.launch {
            var lastVersion = UserPermissionManager.permissionState.value.version
            UserPermissionManager.permissionState.collect { permState ->
                if (permState.version != lastVersion) {
                    lastVersion = permState.version
                    val allCameras = dashboardRepository.getCamerasInMemory()
                    _uiState.update { cur ->
                        val baseCameras = allCameras.ifEmpty { cur.cameraList }
                        cur.copy(
                            cameraList = UserPermissionManager.filterPermittedCameras(baseCameras),
                            recentEvents = cur.recentEvents.permittedDistinct(baseCameras)
                                .take(MAX_RECENT_EVENTS)
                        ).withRecomputedMetrics()
                    }
                    fetchDashboardData()
                }
            }
        }
    }

    private data class RecentEventsFetchResult(
        val events: List<EventData>,
        val anySuccessful: Boolean,
        val firstError: Throwable? = null
    )

    /**
     * Khớp hoàn toàn với MobileHomePage.vue:
     * Fetch theo batch 50 sự kiện (tối đa 3 lần), lọc các loại AI và camera được phân quyền,
     * loại trùng ID và chỉ giữ đúng 5 sự kiện mới nhất (permittedEvents.slice(0, 5)).
     */
    private suspend fun fetchPermittedRecentEvents(
        fromSec: Long,
        toSec: Long,
        serviceId: Int?,
        allCameras: List<CameraModel> = dashboardRepository.getCamerasInMemory()
    ): RecentEventsFetchResult {
        if (!UserPermissionManager.canAccessEvents()) {
            return RecentEventsFetchResult(events = emptyList(), anySuccessful = true)
        }
        val permitted = mutableListOf<EventData>()
        val seenIds = HashSet<Int>()
        val seenKeys = HashSet<String>()
        var lastId: Int? = null
        var attempts = 0
        var anySuccessful = false
        var firstError: Throwable? = null

        while (permitted.size < MAX_RECENT_EVENTS && attempts < MAX_BATCH_ATTEMPTS) {
            attempts++
            val result = safeApiCall {
                dashboardRepository.getEventDataList(
                    count = RECENT_BATCH_SIZE,
                    lastId = lastId,
                    from = fromSec,
                    to = toSec,
                    serviceId = serviceId
                )
            }
            val resp = result.getOrNull()
            if (resp == null) {
                if (firstError == null) firstError = result.exceptionOrNull()
                break
            }
            anySuccessful = true
            val data = resp.body() ?: emptyList()
            if (data.isEmpty()) break

            val camerasForPerm = allCameras.ifEmpty { dashboardRepository.getCamerasInMemory() }
            for (ev in data) {
                if (UserPermissionManager.isEventPermitted(ev, camerasForPerm) &&
                    seenIds.add(ev.id) &&
                    seenKeys.add(ev.uniqueKey())
                ) {
                    permitted.add(ev)
                    if (permitted.size >= MAX_RECENT_EVENTS) break
                }
            }

            if (data.size < RECENT_BATCH_SIZE) break
            lastId = data.lastOrNull()?.id ?: break
        }

        return RecentEventsFetchResult(
            events = permitted.take(MAX_RECENT_EVENTS),
            anySuccessful = anySuccessful,
            firstError = if (anySuccessful) null else firstError
        )
    }

    private fun fetchRecentEvents() {
        recentEventsJob?.cancel()
        recentEventsJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val filter = _uiState.value.selectedFilter
                val serviceId = _uiState.value.selectedServiceId
                val serviceIdStr = serviceId?.toString()
                val (fromTime, toTime) = TimeFilter.resolveTimeRangeMillis(filter)
                coroutineScope {
                    val overviewDeferred = async {
                        safeApiCall {
                            dashboardRepository.getVmsCountOverview(
                                serviceId = serviceIdStr ?: "-1",
                                fromTime = fromTime,
                                toTime = toTime
                            )
                        }.getOrNull()
                    }
                    val byTypeDeferred = async {
                        safeApiCall {
                            dashboardRepository.getVmsEventCountByType(
                                fromTime = fromTime,
                                toTime = toTime,
                                serviceId = serviceIdStr
                            )
                        }.getOrNull()
                    }
                    val recentDeferred = async {
                        fetchPermittedRecentEvents(
                            fromSec = fromTime / 1000,
                            toSec = toTime / 1000,
                            serviceId = serviceId
                        )
                    }
                    val overviewResp = overviewDeferred.await()
                    val byTypeResp = byTypeDeferred.await()
                    val recentResult = recentDeferred.await()
                    val newEvents = recentResult.events

                    if (newEvents.isNotEmpty()) {
                        dashboardRepository.cacheInMemory(events = newEvents)
                    }
                    val nextState = withContext(Dispatchers.Default) {
                        val cur = _uiState.value
                        val allCameras = dashboardRepository.getCamerasInMemory().ifEmpty { cur.cameraList }
                        val mergedEvents = if (newEvents.isNotEmpty()) {
                            (newEvents + cur.recentEvents)
                                .permittedDistinct(allCameras)
                                .distinctBy { it.id }
                                .take(MAX_RECENT_EVENTS)
                        } else {
                            cur.recentEvents
                                .permittedDistinct(allCameras)
                                .distinctBy { it.id }
                                .take(MAX_RECENT_EVENTS)
                        }
                        cur.copy(
                            overviewStats = if (overviewResp?.isSuccessful == true) overviewResp.body() ?: cur.overviewStats else cur.overviewStats,
                            eventsByType = if (byTypeResp?.isSuccessful == true) byTypeResp.body() ?: cur.eventsByType else cur.eventsByType,
                            recentEvents = mergedEvents
                        ).withRecomputedMetrics()
                    }
                    _uiState.value = nextState
                    dashboardRepository.saveHomeData(
                        filter = filter,
                        serviceId = serviceId,
                        data = CachedHomeData(
                            overviewStats = nextState.overviewStats,
                            eventsByType = nextState.eventsByType,
                            cameraList = dashboardRepository.getCamerasInMemory().ifEmpty { nextState.cameraList },
                            aiServices = nextState.aiServices,
                            recentEvents = nextState.recentEvents,
                            runningAiTasks = nextState.runningAiTasks,
                            totalAiTasks = nextState.totalAiTasks,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) Log.e(TAG, "Error fetching recent events via WebSocket", e)
            }
        }
    }

    fun setTimeFilter(filter: TimeFilter) {
        val isCustom = filter == TimeFilter.CUSTOM_DATE || filter == TimeFilter.CUSTOM_RANGE
        if (!isCustom && _uiState.value.selectedFilter == filter) return
        _uiState.update { it.copy(selectedFilter = filter, isRefreshing = false) }
        fetchDashboardData()
    }

    fun setServiceFilter(serviceId: Int?) {
        if (_uiState.value.selectedServiceId == serviceId) return
        _uiState.update { it.copy(selectedServiceId = serviceId, isRefreshing = false) }
        fetchDashboardData()
    }

    fun applyFilters(filter: TimeFilter, serviceId: Int?) {
        val isCustom = filter == TimeFilter.CUSTOM_DATE || filter == TimeFilter.CUSTOM_RANGE
        if (!isCustom && _uiState.value.selectedFilter == filter && _uiState.value.selectedServiceId == serviceId) return
        _uiState.update { it.copy(selectedFilter = filter, selectedServiceId = serviceId, isRefreshing = false) }
        fetchDashboardData()
    }

    fun resetFilters() {
        applyFilters(TimeFilter.TODAY, null)
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        fetchDashboardData(isUserRefresh = true)
    }

    fun fetchDashboardData(isUserRefresh: Boolean = false) {
        dashboardJob?.cancel()
        dashboardJob = viewModelScope.launch(Dispatchers.IO) {
            val filter = _uiState.value.selectedFilter
            val serviceId = _uiState.value.selectedServiceId

            // 1. Ưu tiên đọc ngay dữ liệu từ RAM / Disk Cache (Stale-While-Revalidate) khi chưa phải thao tác kéo refresh
            val cachedData = dashboardRepository.getHomeData(filter, serviceId)
            if (!isUserRefresh) {
                if (cachedData != null) {
                    val updatedFromCache = withContext(Dispatchers.Default) {
                        val cur = _uiState.value
                        val rawCachedCameras = cachedData.cameraList
                            .ifEmpty { dashboardRepository.getCamerasInMemory() }
                            .ifEmpty { cur.cameraList }
                        val permittedCameras = UserPermissionManager.filterPermittedCameras(rawCachedCameras)
                        cur.copy(
                            isLoading = false,
                            error = null,
                            overviewStats = cachedData.overviewStats,
                            eventsByType = cachedData.eventsByType,
                            cameraList = permittedCameras,
                            aiServices = cachedData.aiServices.ifEmpty { cur.aiServices },
                            recentEvents = cachedData.recentEvents
                                .permittedDistinct(rawCachedCameras)
                                .take(MAX_RECENT_EVENTS),
                            runningAiTasks = if (cachedData.totalAiTasks > 0) cachedData.runningAiTasks else cur.runningAiTasks,
                            totalAiTasks = if (cachedData.totalAiTasks > 0) cachedData.totalAiTasks else cur.totalAiTasks
                        ).withRecomputedMetrics()
                    }
                    _uiState.value = updatedFromCache
                } else {
                    _uiState.update { it.copy(isLoading = true, error = null) }
                }
            }

            try {
                val (fromTime, toTime) = TimeFilter.resolveTimeRangeMillis(filter)

                // 2. Gọi song song các API thực sự hiển thị trên Trang chủ (đã bỏ 2 API chart không dùng)
                coroutineScope {
                    val serviceIdStr = serviceId?.toString()
                    val overviewDeferred = async {
                        safeApiCall {
                            dashboardRepository.getVmsCountOverview(
                                serviceId = serviceIdStr ?: "-1",
                                fromTime = fromTime,
                                toTime = toTime
                            )
                        }
                    }
                    val byTypeDeferred = async {
                        safeApiCall {
                            dashboardRepository.getVmsEventCountByType(
                                fromTime = fromTime,
                                toTime = toTime,
                                serviceId = serviceIdStr
                            )
                        }
                    }
                    val cameraDeferred = async {
                        safeApiCall { dashboardRepository.getCameraList() }
                    }
                    val aiServiceDeferred = async {
                        safeApiCall { dashboardRepository.getAIServiceList() }
                    }

                    // Nếu tài khoản đang áp dụng phân quyền camera custom và RAM chưa có danh sách camera để map UUID,
                    // đợi cameraDeferred xong trước khi lọc sự kiện để đảm bảo ngay lần tải đầu tiên lọc đúng camera UUID.
                    if (UserPermissionManager.isCustomCameraPermission() &&
                        dashboardRepository.getCamerasInMemory().isEmpty()
                    ) {
                        val preCamResp = cameraDeferred.await().getOrNull()
                        if (preCamResp?.isSuccessful == true) {
                            val preCamList = preCamResp.body() ?: emptyList()
                            if (preCamList.isNotEmpty()) {
                                dashboardRepository.cacheInMemory(cameras = preCamList, fromNetwork = true)
                            }
                        }
                    }

                    val recentEventsDeferred = async {
                        fetchPermittedRecentEvents(
                            fromSec = fromTime / 1000,
                            toSec = toTime / 1000,
                            serviceId = serviceId,
                            allCameras = dashboardRepository.getCamerasInMemory()
                        )
                    }

                    // Pre-warm danh sách hồ sơ khuôn mặt trong nền nếu chưa có để màn Chi tiết sự kiện mở 0ms
                    launch {
                        dashboardRepository.prewarmProfilesIfNeeded()
                    }

                    val overviewResult = overviewDeferred.await()
                    val byTypeResult = byTypeDeferred.await()
                    val recentResult = recentEventsDeferred.await()
                    val cameraResult = cameraDeferred.await()
                    val aiServiceResult = aiServiceDeferred.await()

                    val overviewResponse = overviewResult.getOrNull()
                    val byTypeResponse = byTypeResult.getOrNull()
                    val recentEvents = recentResult.events
                    val recentEventsSucceeded = recentResult.anySuccessful
                    val cameraResponse = cameraResult.getOrNull()
                    val aiServiceResponse = aiServiceResult.getOrNull()

                    val overviewStats = if (overviewResponse?.isSuccessful == true) {
                        overviewResponse.body() ?: emptyList()
                    } else _uiState.value.overviewStats

                    val eventsByType = if (byTypeResponse?.isSuccessful == true) {
                        byTypeResponse.body() ?: emptyList()
                    } else _uiState.value.eventsByType

                    val allCameraList = if (cameraResponse?.isSuccessful == true) {
                        cameraResponse.body() ?: emptyList()
                    } else {
                        dashboardRepository.getCamerasInMemory().ifEmpty { _uiState.value.cameraList }
                    }
                    val permittedCameraList = UserPermissionManager.filterPermittedCameras(allCameraList)

                    val aiServices = if (aiServiceResponse?.isSuccessful == true) {
                        aiServiceResponse.body() ?: emptyList()
                    } else _uiState.value.aiServices

                    // Compute running AI tasks & total AI tasks matching MobileHomePage.vue (song song trên các AI Service)
                    // Chỉ gọi Wave 2 khi có kết nối mạng và ít nhất 1 API Wave 1 thành công để tránh treo chờ timeout khi mất mạng/sập backend
                    var runningTasks = 0
                    var totalTasks = 0
                    val wave1Alive = NetworkMonitor.isNetworkAvailable() &&
                        (overviewResponse?.isSuccessful == true || aiServiceResponse?.isSuccessful == true)
                    if (wave1Alive && UserPermissionManager.canAccessEvents()) {
                        try {
                            val targetServices = if (serviceId != null) {
                                aiServices.filter { it.id == serviceId }
                            } else {
                                aiServices
                            }
                            if (targetServices.isEmpty()) {
                                val monResp = dashboardRepository.getMonitorList()
                                if (monResp.isSuccessful) {
                                    val monitors = (monResp.body() ?: emptyList()).filter { AiTypeHelper.isAiTypeEnabled(it.type) }
                                    totalTasks = monitors.size
                                    runningTasks = monitors.count { it.enabled }
                                }
                            } else {
                                val monitorResults = targetServices.map { srv ->
                                    async {
                                        val host = srv.publicHost.ifBlank { srv.privateHost }.trimEnd('/')
                                        safeApiCall {
                                            if (host.isNotBlank() && srv.id != 0) {
                                                dashboardRepository.getMonitorListExt(
                                                    url = "$host/api/Monitor",
                                                    authHeader = "Bearer ${srv.apiKey}"
                                                )
                                            } else {
                                                dashboardRepository.getMonitorList()
                                            }
                                        }.getOrNull()
                                    }
                                }.map { it.await() }

                                for (monResp in monitorResults) {
                                    if (monResp != null && monResp.isSuccessful) {
                                        val monitors = (monResp.body() ?: emptyList()).filter { AiTypeHelper.isAiTypeEnabled(it.type) }
                                        totalTasks += monitors.size
                                        runningTasks += monitors.count { it.enabled }
                                    }
                                }
                            }
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            if (BuildConfig.DEBUG) Log.w(TAG, "Fallback monitor count from overviewStats", e)
                        }
                    }
                    val isRestrictedAiScope = UserPermissionManager.isCustomAiEventPermission() ||
                        AiTypeHelper.isProjectModuleRestricted
                    if (totalTasks == 0 && UserPermissionManager.canAccessEvents() && !isRestrictedAiScope) {
                        val overviewMonitorCount = overviewStats
                            .find { it.label == "monitor-count" && it.tag == "all" }
                            ?.count?.toString()?.toDoubleOrNull()?.toInt() ?: 0
                        if (overviewMonitorCount > 0) {
                            totalTasks = overviewMonitorCount
                            runningTasks = if (_uiState.value.runningAiTasks > 0) {
                                _uiState.value.runningAiTasks
                            } else {
                                overviewMonitorCount
                            }
                        } else {
                            totalTasks = _uiState.value.totalAiTasks
                            runningTasks = _uiState.value.runningAiTasks
                        }
                    }

                    // Báo lỗi backend kể cả khi đã có dữ liệu cache hiển thị trên màn hình
                    val primaryFailure = overviewResult.exceptionOrNull()
                        ?: recentResult.firstError
                        ?: byTypeResult.exceptionOrNull()
                        ?: cameraResult.exceptionOrNull()
                    val errorMsg = primaryFailure?.toUserFriendlyMessage()

                    val rawFinalRecentEvents = if (recentEventsSucceeded || recentEvents.isNotEmpty() || cachedData == null) {
                        recentEvents
                    } else {
                        _uiState.value.recentEvents
                    }
                    val finalRecentEvents = rawFinalRecentEvents
                        .permittedDistinct(allCameraList)
                        .distinctBy { it.id }
                        .take(MAX_RECENT_EVENTS)

                    dashboardRepository.cacheInMemory(
                        events = finalRecentEvents,
                        cameras = allCameraList,
                        aiServices = aiServices,
                        fromNetwork = cameraResponse?.isSuccessful == true
                    )

                    val nextState = withContext(Dispatchers.Default) {
                        _uiState.value.copy(
                            isLoading = false,
                            isRefreshing = false,
                            overviewStats = overviewStats,
                            eventsByType = eventsByType,
                            cameraList = permittedCameraList,
                            aiServices = aiServices,
                            recentEvents = finalRecentEvents,
                            runningAiTasks = runningTasks,
                            totalAiTasks = totalTasks,
                            error = errorMsg
                        ).withRecomputedMetrics()
                    }
                    _uiState.value = nextState

                    if (overviewResponse?.isSuccessful == true || recentEventsSucceeded) {
                        val newCachedData = CachedHomeData(
                            overviewStats = overviewStats,
                            eventsByType = eventsByType,
                            cameraList = allCameraList,
                            aiServices = aiServices,
                            recentEvents = finalRecentEvents,
                            runningAiTasks = runningTasks,
                            totalAiTasks = totalTasks,
                            updatedAt = System.currentTimeMillis()
                        )
                        dashboardRepository.saveHomeData(filter, serviceId, newCachedData)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) Log.e(TAG, "Error fetching data", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = e.toUserFriendlyMessage()
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun HomeUiState.withRecomputedMetrics(): HomeUiState {
        if (!UserPermissionManager.canAccessEvents()) {
            return copy(totalDetectionsCount = 0L).withRecomputedCameraMetrics()
        }
        val sumByType = eventsByType
            .filter { AiTypeHelper.isAiTypeEnabled(it.type) }
            .sumOf { it.count.toString().toDoubleOrNull()?.toLong() ?: 0L }
        val isRestrictedAiScope = UserPermissionManager.isCustomAiEventPermission() ||
            AiTypeHelper.isProjectModuleRestricted
        val totalDetections = if (sumByType > 0L) {
            sumByType
        } else if (isRestrictedAiScope) {
            if (eventsByType.isNotEmpty()) 0L else recentEvents.size.toLong()
        } else {
            overviewStats
                .find { it.label == "event-count" && it.tag == "all" }
                ?.count?.toString()?.toDoubleOrNull()?.toLong() ?: recentEvents.size.toLong()
        }
        return copy(totalDetectionsCount = totalDetections).withRecomputedCameraMetrics()
    }

    private fun HomeUiState.withRecomputedCameraMetrics(): HomeUiState {
        val totalCams = if (UserPermissionManager.isCustomCameraPermission()) {
            cameraList.size
        } else {
            val totalFromOverview = overviewStats
                .find { it.label == "camera-count" && it.tag == "all" }
                ?.count?.toString()?.toDoubleOrNull()?.toInt() ?: 0
            if (cameraList.isNotEmpty()) cameraList.size else totalFromOverview
        }

        val onlineCams = if (cameraList.isEmpty()) {
            totalCams
        } else {
            cameraList.count { cam ->
                val wsStatus = cameraStatusMap["cam-0-${cam.id}"]
                    ?: cameraStatusMap["cam-${cam.vmsId}-${cam.cameraId}"]
                    ?: cameraStatusMap["cam-${cam.vmsId}-${cam.id}"]
                    ?: cameraStatusMap["cam-${cam.id}"]
                var isOffline = wsStatus == "red"
                if (!isOffline) {
                    val issues = cam.extra?.latestReport?.issues
                    if (issues != null && (issues.contains("loi_ket_noi") || issues.contains("mat_ket_noi"))) {
                        isOffline = true
                    }
                }
                !isOffline
            }
        }

        val recordingCams = cameraList.count { cam ->
            val wsRecStatus = recordStatusMap["cam-0-${cam.id}"]
                ?: recordStatusMap["cam-${cam.vmsId}-${cam.cameraId}"]
                ?: recordStatusMap["cam-${cam.vmsId}-${cam.id}"]
                ?: recordStatusMap["cam-${cam.id}"]
            if (wsRecStatus != null) {
                wsRecStatus == "green"
            } else {
                cam.enableRecord || cam.extra?.recording == true || cam.extra?.recordStatus == "green"
            }
        }

        return copy(
            totalCamerasCount = totalCams,
            onlineCamerasCount = onlineCams,
            recordingCamerasCount = recordingCams
        )
    }

    companion object {
        private const val TAG = "HomeViewModel"
        private const val MAX_RECENT_EVENTS = 5
        private const val RECENT_BATCH_SIZE = 50
        private const val MAX_BATCH_ATTEMPTS = 3
        private const val NOTIFY_DEBOUNCE_MS = 600L
        private const val STATUS_DEBOUNCE_MS = 800L

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as TenliApp
                HomeViewModel(
                    application = application,
                    dashboardRepository = application.container.dashboardRepository,
                    webSocketManager = application.container.webSocketManager
                )
            }
        }
    }
}
