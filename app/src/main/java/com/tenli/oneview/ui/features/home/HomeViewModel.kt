package com.tenli.oneview.ui.features.home

import android.app.Application
import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.Immutable
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
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.data.network.websocket.WebSocketManager
import com.tenli.oneview.data.repository.DashboardRepository
import com.tenli.oneview.model.network.AIServiceModel
import com.tenli.oneview.model.network.CameraModel
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.model.network.VmsCountOverviewModel
import com.tenli.oneview.model.network.VmsEventCountByTypeModel
import com.tenli.oneview.ui.features.event.eventTimeToSeconds
import com.tenli.oneview.ui.features.event.isWithinTimeRangeSeconds
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
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
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
                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                    calendar.set(Calendar.MINUTE, 59)
                    calendar.set(Calendar.SECOND, 59)
                    calendar.set(Calendar.MILLISECOND, 999)
                    val toTime = calendar.timeInMillis
                    calendar.add(Calendar.DAY_OF_YEAR, -7)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
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

@Immutable
data class HomeAiDistributionItem(
    val filterSensorType: String,
    val labelVi: String,
    val labelEn: String,
    val count: Long,
    val percentage: Float
)

@Immutable
data class HomeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val selectedFilter: TimeFilter = TimeFilter.TODAY,
    val selectedServiceId: Int? = null,
    val overviewStats: List<VmsCountOverviewModel> = emptyList(),
    val eventsByType: List<VmsEventCountByTypeModel> = emptyList(),
    val hasLoadedEventsByType: Boolean = false,
    val aiDistribution: List<HomeAiDistributionItem> = emptyList(),
    val cameraList: List<CameraModel> = emptyList(),
    val aiServices: List<AIServiceModel> = emptyList(),
    val recentEvents: List<EventData> = emptyList(),
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

    @Volatile private var latestCameraStatusMap: Map<String, String> = webSocketManager.cameraStatusMap.value
    @Volatile private var latestRecordStatusMap: Map<String, String> = webSocketManager.recordStatusMap.value

    private val _uiState = MutableStateFlow(buildInitialState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var dashboardJob: Job? = null
    private var recentEventsJob: Job? = null
    @Volatile private var isHomeTabActive: Boolean = true
    @Volatile private var pendingNotifyRefresh: Boolean = false
    @Volatile private var pendingCameraStatusRecompute: Boolean = false
    @Volatile private var lastByTypeOverviewSyncMs: Long = 0L
    @Volatile private var lastCountedRealtimeEventId: Int = 0

    init {
        fetchDashboardData()
        listenToWebSocket()
        webSocketManager.connectAllReports()
    }

    fun setHomeTabActive(active: Boolean) {
        if (isHomeTabActive == active) return
        isHomeTabActive = active
        webSocketManager.setReportsActive(active, "home")
        if (!active) {
            recentEventsJob?.cancel()
            return
        }
        if (pendingCameraStatusRecompute) {
            pendingCameraStatusRecompute = false
            viewModelScope.launch(Dispatchers.Default) {
                latestCameraStatusMap = webSocketManager.cameraStatusMap.value
                latestRecordStatusMap = webSocketManager.recordStatusMap.value
                _uiState.update { it.withRecomputedCameraMetrics() }
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
            val (fromMs, toMs) = TimeFilter.resolveTimeRangeMillis(TimeFilter.TODAY)
            val fromSec = fromMs / 1000
            val toSec = toMs / 1000
            val rawCameras = memCached.cameraList.ifEmpty { dashboardRepository.getCamerasInMemory() }
            val permittedCameras = UserPermissionManager.filterPermittedCameras(rawCameras)
            val services = memCached.aiServices.ifEmpty { dashboardRepository.getAiServicesInMemory() }
            HomeUiState(
                isLoading = false,
                isListView = isList,
                overviewStats = memCached.overviewStats,
                eventsByType = memCached.eventsByType,
                hasLoadedEventsByType = memCached.hasLoadedEventsByType || memCached.eventsByType.isNotEmpty(),
                cameraList = permittedCameras,
                aiServices = services,
                recentEvents = memCached.recentEvents
                    .filter { it.isWithinTimeRangeSeconds(fromSec, toSec) }
                    .permittedDistinct(rawCameras)
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
                    latestCameraStatusMap = statusMap
                    if (isHomeTabActive) {
                        _uiState.update { it.withRecomputedCameraMetrics() }
                    } else {
                        pendingCameraStatusRecompute = true
                    }
                }
        }
        viewModelScope.launch(Dispatchers.Default) {
            webSocketManager.recordStatusMap
                .debounce(STATUS_DEBOUNCE_MS)
                .collect { recMap ->
                    latestRecordStatusMap = recMap
                    if (isHomeTabActive) {
                        _uiState.update { it.withRecomputedCameraMetrics() }
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
                    if (UserSession.accessToken.isEmpty()) return@collect
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
                if (ev.isWithinTimeRangeSeconds(fromSec, toSec) &&
                    UserPermissionManager.isEventPermitted(ev, camerasForPerm) &&
                    seenIds.add(ev.id) &&
                    seenKeys.add(ev.uniqueKey())
                ) {
                    permitted.add(ev)
                    if (permitted.size >= MAX_RECENT_EVENTS) break
                }
            }

            // Danh sách trả về theo thứ tự mới -> cũ (order=desc); nếu toàn bộ batch đã cũ hơn fromSec thì dừng phân trang
            val allOlderThanFrom = data.all { it.time > 0.0 && eventTimeToSeconds(it.time) < fromSec }
            if (allOlderThanFrom || data.size < RECENT_BATCH_SIZE) break
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
                if (filter != TimeFilter.TODAY) return@launch
                val serviceId = _uiState.value.selectedServiceId
                val serviceIdStr = serviceId?.toString()
                val (fromTime, toTime) = TimeFilter.resolveTimeRangeMillis(filter)
                val fromSec = fromTime / 1000
                val toSec = toTime / 1000
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
                            fromSec = fromSec,
                            toSec = toSec,
                            serviceId = serviceId
                        )
                    }
                    val overviewResp = overviewDeferred.await()
                    val byTypeResp = byTypeDeferred.await()
                    val recentResult = recentDeferred.await()
                    val newEvents = recentResult.events

                    if (_uiState.value.selectedFilter != filter || _uiState.value.selectedServiceId != serviceId) {
                        return@coroutineScope
                    }

                    if (newEvents.isNotEmpty()) {
                        dashboardRepository.cacheInMemory(events = newEvents)
                    }
                    val latestOverviewStats = if (overviewResp?.isSuccessful == true) {
                        overviewResp.body() ?: _uiState.value.overviewStats
                    } else {
                        _uiState.value.overviewStats
                    }
                    val byTypeSucceeded = byTypeResp?.isSuccessful == true
                    val byTypeBody = byTypeResp?.body().orEmpty()
                    val nowElapsed = SystemClock.elapsedRealtime()
                    val curSnapshot = _uiState.value
                    val (resolvedByType, resolvedHasLoadedByType) = if (byTypeSucceeded && byTypeBody.isNotEmpty()) {
                        lastByTypeOverviewSyncMs = nowElapsed
                        lastCountedRealtimeEventId = maxOf(
                            lastCountedRealtimeEventId,
                            newEvents.maxOfOrNull { it.id } ?: 0
                        )
                        byTypeBody to true
                    } else if (
                        UserPermissionManager.canAccessEvents() &&
                        NetworkMonitor.isNetworkAvailable() &&
                        (curSnapshot.eventsByType.isEmpty() || nowElapsed - lastByTypeOverviewSyncMs >= BY_TYPE_WS_REFRESH_INTERVAL_MS)
                    ) {
                        val fallback = fetchEventCountsViaOverview(
                            fromTime = fromTime,
                            toTime = toTime,
                            serviceIdStr = serviceIdStr,
                            unfilteredOverviewStats = latestOverviewStats,
                            seedEvents = newEvents
                        )
                        if (fallback != null) {
                            lastByTypeOverviewSyncMs = SystemClock.elapsedRealtime()
                            lastCountedRealtimeEventId = maxOf(
                                lastCountedRealtimeEventId,
                                newEvents.maxOfOrNull { it.id } ?: 0
                            )
                            fallback to true
                        } else {
                            curSnapshot.eventsByType to (curSnapshot.hasLoadedEventsByType || byTypeSucceeded)
                        }
                    } else if (curSnapshot.eventsByType.isNotEmpty()) {
                        val (incremented, nextMaxId) = mergeRealtimeDeltasIntoByType(
                            currentByType = curSnapshot.eventsByType,
                            newEvents = newEvents,
                            baselineMaxId = lastCountedRealtimeEventId
                        )
                        lastCountedRealtimeEventId = nextMaxId
                        incremented to true
                    } else {
                        val emptyOrExisting = if (byTypeSucceeded) emptyList() else curSnapshot.eventsByType
                        emptyOrExisting to (curSnapshot.hasLoadedEventsByType || byTypeSucceeded)
                    }

                    if (_uiState.value.selectedFilter != filter || _uiState.value.selectedServiceId != serviceId) {
                        return@coroutineScope
                    }

                    val nextState = withContext(Dispatchers.Default) {
                        val cur = _uiState.value
                        val allCameras = dashboardRepository.getCamerasInMemory().ifEmpty { cur.cameraList }
                        val inRangeExisting = cur.recentEvents.filter { it.isWithinTimeRangeSeconds(fromSec, toSec) }
                        val mergedEvents = if (newEvents.isNotEmpty()) {
                            (newEvents + inRangeExisting)
                                .permittedDistinct(allCameras)
                                .distinctBy { it.id }
                                .take(MAX_RECENT_EVENTS)
                        } else {
                            inRangeExisting
                                .permittedDistinct(allCameras)
                                .distinctBy { it.id }
                                .take(MAX_RECENT_EVENTS)
                        }
                        cur.copy(
                            overviewStats = latestOverviewStats,
                            eventsByType = resolvedByType,
                            hasLoadedEventsByType = resolvedHasLoadedByType,
                            recentEvents = mergedEvents
                        ).withRecomputedMetrics()
                    }
                    if (_uiState.value.selectedFilter != filter || _uiState.value.selectedServiceId != serviceId) {
                        return@coroutineScope
                    }
                    _uiState.value = nextState
                    dashboardRepository.saveHomeData(
                        filter = filter,
                        serviceId = serviceId,
                        data = CachedHomeData(
                            overviewStats = nextState.overviewStats,
                            eventsByType = nextState.eventsByType,
                            hasLoadedEventsByType = nextState.hasLoadedEventsByType,
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
        recentEventsJob?.cancel()
        pendingNotifyRefresh = false
        lastByTypeOverviewSyncMs = 0L
        lastCountedRealtimeEventId = 0
        _uiState.update { it.copy(selectedFilter = filter, isRefreshing = false) }
        fetchDashboardData()
    }

    fun setServiceFilter(serviceId: Int?) {
        if (_uiState.value.selectedServiceId == serviceId) return
        recentEventsJob?.cancel()
        pendingNotifyRefresh = false
        lastByTypeOverviewSyncMs = 0L
        lastCountedRealtimeEventId = 0
        _uiState.update { it.copy(selectedServiceId = serviceId, isRefreshing = false) }
        fetchDashboardData()
    }

    fun applyFilters(filter: TimeFilter, serviceId: Int?) {
        val isCustom = filter == TimeFilter.CUSTOM_DATE || filter == TimeFilter.CUSTOM_RANGE
        if (!isCustom && _uiState.value.selectedFilter == filter && _uiState.value.selectedServiceId == serviceId) return
        recentEventsJob?.cancel()
        pendingNotifyRefresh = false
        lastByTypeOverviewSyncMs = 0L
        lastCountedRealtimeEventId = 0
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
        recentEventsJob?.cancel()
        if (UserSession.accessToken.isEmpty() || UserSession.domain.isEmpty()) return
        dashboardJob = viewModelScope.launch(Dispatchers.IO) {
            val filter = _uiState.value.selectedFilter
            val serviceId = _uiState.value.selectedServiceId
            val (fromTime, toTime) = TimeFilter.resolveTimeRangeMillis(filter)
            val fromSec = fromTime / 1000
            val toSec = toTime / 1000

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
                            hasLoadedEventsByType = cachedData.hasLoadedEventsByType || cachedData.eventsByType.isNotEmpty(),
                            cameraList = permittedCameras,
                            aiServices = cachedData.aiServices.ifEmpty { cur.aiServices },
                            recentEvents = cachedData.recentEvents
                                .filter { it.isWithinTimeRangeSeconds(fromSec, toSec) }
                                .permittedDistinct(rawCachedCameras)
                                .take(MAX_RECENT_EVENTS),
                            runningAiTasks = if (cachedData.totalAiTasks > 0) cachedData.runningAiTasks else cur.runningAiTasks,
                            totalAiTasks = if (cachedData.totalAiTasks > 0) cachedData.totalAiTasks else cur.totalAiTasks
                        ).withRecomputedMetrics()
                    }
                    _uiState.value = updatedFromCache
                } else {
                    // Chưa có cache cho bộ lọc mới: xóa ngay dữ liệu sự kiện/thống kê của bộ lọc cũ để tránh hiển thị nhầm số liệu
                    _uiState.update {
                        it.copy(
                            isLoading = true,
                            error = null,
                            overviewStats = emptyList(),
                            eventsByType = emptyList(),
                            hasLoadedEventsByType = false,
                            recentEvents = emptyList(),
                            totalDetectionsCount = 0L
                        )
                    }
                }
            }

            try {
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
                    val byTypeSucceeded = byTypeResponse?.isSuccessful == true
                    val recentEvents = recentResult.events
                    val recentEventsSucceeded = recentResult.anySuccessful
                    val cameraResponse = cameraResult.getOrNull()
                    val aiServiceResponse = aiServiceResult.getOrNull()

                    if (_uiState.value.selectedFilter != filter || _uiState.value.selectedServiceId != serviceId) {
                        return@coroutineScope
                    }

                    val overviewStats = if (overviewResponse?.isSuccessful == true) {
                        overviewResponse.body() ?: emptyList()
                    } else _uiState.value.overviewStats

                    val byTypeBody = byTypeResponse?.body().orEmpty()

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
                        (overviewResponse?.isSuccessful == true ||
                            byTypeSucceeded ||
                            aiServiceResponse?.isSuccessful == true)
                    val fallbackByTypeDeferred = if (
                        !(byTypeSucceeded && byTypeBody.isNotEmpty()) &&
                        wave1Alive &&
                        UserPermissionManager.canAccessEvents()
                    ) {
                        async {
                            fetchEventCountsViaOverview(
                                fromTime = fromTime,
                                toTime = toTime,
                                serviceIdStr = serviceIdStr,
                                unfilteredOverviewStats = overviewStats,
                                seedEvents = recentEvents
                            )
                        }
                    } else {
                        null
                    }
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
                                        srv.id to safeApiCall {
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

                                val isCustomUserAi = UserPermissionManager.isCustomAiEventPermission()
                                for ((srvId, monResp) in monitorResults) {
                                    if (monResp != null && monResp.isSuccessful) {
                                        val monitors = (monResp.body() ?: emptyList()).filter { AiTypeHelper.isAiTypeEnabled(it.type) }
                                        totalTasks += monitors.size
                                        runningTasks += monitors.count { it.enabled }
                                    } else if (!isCustomUserAi) {
                                        val srvIdStr = srvId.toString()
                                        val srvTotal = overviewStats
                                            .find { it.label == "monitor-count" && it.tag == "item" && it.serviceId == srvIdStr }
                                            ?.count?.toString()?.toDoubleOrNull()?.toInt() ?: 0
                                        val srvRunning = overviewStats
                                            .find { it.label == "monitor-enabled-count" && it.tag == "item" && it.serviceId == srvIdStr }
                                            ?.count?.toString()?.toDoubleOrNull()?.toInt() ?: 0
                                        totalTasks += srvTotal
                                        runningTasks += srvRunning
                                    }
                                }
                            }
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            if (BuildConfig.DEBUG) Log.w(TAG, "Fallback monitor count from overviewStats", e)
                        }
                    }
                    if (totalTasks == 0 && UserPermissionManager.canAccessEvents() && !UserPermissionManager.isCustomAiEventPermission()) {
                        val srvIdStr = serviceId?.toString()
                        val overviewMonitorCount = if (srvIdStr != null) {
                            overviewStats
                                .find { it.label == "monitor-count" && it.tag == "item" && it.serviceId == srvIdStr }
                                ?.count?.toString()?.toDoubleOrNull()?.toInt()
                                ?: overviewStats
                                    .find { it.label == "monitor-count" && it.tag == "all" }
                                    ?.count?.toString()?.toDoubleOrNull()?.toInt() ?: 0
                        } else {
                            overviewStats
                                .find { it.label == "monitor-count" && it.tag == "all" }
                                ?.count?.toString()?.toDoubleOrNull()?.toInt() ?: 0
                        }
                        val overviewEnabledCount = if (srvIdStr != null) {
                            overviewStats
                                .find { it.label == "monitor-enabled-count" && it.tag == "item" && it.serviceId == srvIdStr }
                                ?.count?.toString()?.toDoubleOrNull()?.toInt() ?: 0
                        } else {
                            overviewStats
                                .filter { it.label == "monitor-enabled-count" }
                                .sumOf { it.count?.toString()?.toDoubleOrNull()?.toInt() ?: 0 }
                        }
                        if (overviewMonitorCount > 0) {
                            totalTasks = overviewMonitorCount
                            runningTasks = when {
                                overviewEnabledCount > 0 -> overviewEnabledCount
                                _uiState.value.runningAiTasks > 0 -> _uiState.value.runningAiTasks
                                else -> 0
                            }
                        } else {
                            totalTasks = _uiState.value.totalAiTasks
                            runningTasks = _uiState.value.runningAiTasks
                        }
                    }

                    val fallbackByType = fallbackByTypeDeferred?.await()
                    val (eventsByType, hasLoadedEventsByType) = when {
                        byTypeSucceeded && byTypeBody.isNotEmpty() -> {
                            lastByTypeOverviewSyncMs = SystemClock.elapsedRealtime()
                            lastCountedRealtimeEventId = maxOf(
                                lastCountedRealtimeEventId,
                                recentEvents.maxOfOrNull { it.id } ?: 0
                            )
                            byTypeBody to true
                        }
                        fallbackByType != null -> {
                            lastByTypeOverviewSyncMs = SystemClock.elapsedRealtime()
                            lastCountedRealtimeEventId = maxOf(
                                lastCountedRealtimeEventId,
                                recentEvents.maxOfOrNull { it.id } ?: 0
                            )
                            fallbackByType to true
                        }
                        else -> {
                            _uiState.value.eventsByType to (byTypeSucceeded || _uiState.value.hasLoadedEventsByType)
                        }
                    }

                    // Báo lỗi backend kể cả khi đã có dữ liệu cache hiển thị trên màn hình
                    val primaryFailure = overviewResult.exceptionOrNull()
                        ?: recentResult.firstError
                        ?: byTypeResult.exceptionOrNull()
                        ?: cameraResult.exceptionOrNull()
                    val errorMsg = primaryFailure?.toUserFriendlyMessage()?.takeIf { it.isNotEmpty() }

                    if (UserSession.accessToken.isEmpty()) return@coroutineScope

                    val rawFinalRecentEvents = if (recentEventsSucceeded || recentEvents.isNotEmpty() || cachedData == null) {
                        recentEvents
                    } else {
                        _uiState.value.recentEvents
                    }
                    val finalRecentEvents = rawFinalRecentEvents
                        .filter { it.isWithinTimeRangeSeconds(fromSec, toSec) }
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
                            hasLoadedEventsByType = hasLoadedEventsByType,
                            cameraList = permittedCameraList,
                            aiServices = aiServices,
                            recentEvents = finalRecentEvents,
                            runningAiTasks = runningTasks,
                            totalAiTasks = totalTasks,
                            error = errorMsg
                        ).withRecomputedMetrics()
                    }
                    if (_uiState.value.selectedFilter != filter || _uiState.value.selectedServiceId != serviceId) {
                        return@coroutineScope
                    }
                    _uiState.value = nextState

                    if (overviewResponse?.isSuccessful == true || byTypeSucceeded || fallbackByType != null || recentEventsSucceeded) {
                        val newCachedData = CachedHomeData(
                            overviewStats = overviewStats,
                            eventsByType = eventsByType,
                            hasLoadedEventsByType = hasLoadedEventsByType,
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
                if (UserSession.accessToken.isEmpty()) return@launch
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = e.toUserFriendlyMessage().takeIf { msg -> msg.isNotEmpty() }
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun extractOverviewEventCount(
        stats: List<VmsCountOverviewModel>,
        serviceIdStr: String?,
        expectedEventType: String? = null
    ): Long? {
        if (stats.isEmpty()) {
            return if (expectedEventType != null) 0L else null
        }
        val relevant = stats.filter { item ->
            item.label == "event-count" &&
                (expectedEventType == null ||
                    item.eventType.isNullOrBlank() ||
                    item.eventType.equals(expectedEventType, ignoreCase = true))
        }
        if (relevant.isEmpty()) {
            return when {
                expectedEventType != null -> 0L
                serviceIdStr != null -> 0L
                else -> null
            }
        }
        return if (serviceIdStr != null) {
            val hasItemBreakdown = relevant.any { it.tag == "item" }
            if (hasItemBreakdown) {
                relevant.find { it.tag == "item" && it.serviceId == serviceIdStr }
                    ?.count?.toLong() ?: 0L
            } else {
                relevant.find { it.tag == "all" }?.count?.toLong()
            }
        } else {
            relevant.find { it.tag == "all" }?.count?.toLong()
        }
    }

    private fun collectEnabledEventTypesForOverview(seedEvents: List<EventData>): List<String> {
        val result = LinkedHashSet<String>()
        for (mod in AiTypeHelper.activeModules) {
            for (et in mod.eventTypes) {
                val trimmed = et.trim()
                if (trimmed.isNotEmpty() && AiTypeHelper.isAiTypeEnabled(trimmed)) {
                    result.add(trimmed)
                }
            }
        }
        for (ev in seedEvents) {
            val trimmed = ev.type.trim()
            if (trimmed.isNotEmpty() && AiTypeHelper.isAiTypeEnabled(trimmed)) {
                result.add(trimmed)
            }
        }
        return result.toList()
    }

    /**
     * Khi `vms-event/count-by-type` trả về mảng rỗng `[]` (nhưng hệ thống có sự kiện),
     * truy vấn song song `vms-core/count-overview?eventType=<type>` theo đúng cơ chế Dashboard từng bài AI trên Web
     * để lấy số lượng sự kiện thực tế của từng bài AI.
     * Trả về `null` nếu tất cả request đều lỗi mạng để bảo toàn state/cache hiện tại.
     */
    private suspend fun fetchEventCountsViaOverview(
        fromTime: Long,
        toTime: Long,
        serviceIdStr: String?,
        unfilteredOverviewStats: List<VmsCountOverviewModel>,
        seedEvents: List<EventData>
    ): List<VmsEventCountByTypeModel>? = coroutineScope {
        val unfilteredTotal = extractOverviewEventCount(unfilteredOverviewStats, serviceIdStr)
        if (unfilteredTotal != null && unfilteredTotal <= 0L) {
            return@coroutineScope emptyList()
        }
        val eventTypes = collectEnabledEventTypesForOverview(seedEvents)
        if (eventTypes.isEmpty()) {
            return@coroutineScope emptyList()
        }
        val semaphore = Semaphore(MAX_OVERVIEW_BY_TYPE_CONCURRENCY)
        val queryOutputs = eventTypes.map { et ->
            async {
                semaphore.withPermit {
                    val resp = safeApiCall {
                        dashboardRepository.getVmsCountOverview(
                            serviceId = serviceIdStr ?: "-1",
                            fromTime = fromTime,
                            toTime = toTime,
                            eventType = et
                        )
                    }.getOrNull()
                    if (resp?.isSuccessful == true) {
                        val body = resp.body() ?: emptyList()
                        val echoedEventType = body.any { !it.eventType.isNullOrBlank() }
                        val cnt = extractOverviewEventCount(body, serviceIdStr, et) ?: 0L
                        Triple(et, cnt, echoedEventType)
                    } else {
                        null
                    }
                }
            }
        }.awaitAll()

        val succeeded = queryOutputs.filterNotNull()
        if (succeeded.isEmpty()) {
            return@coroutineScope null
        }

        // Phòng trường hợp backend cũ bỏ qua query param `eventType` và trả trùng tổng unfiltered cho mọi loại
        if (unfilteredTotal != null && unfilteredTotal > 0L && succeeded.size >= 2) {
            val distinctModules = succeeded.map { (et, _, _) ->
                AiTypeHelper.findModuleByEventType(et)?.sensorType ?: et
            }.toSet()
            val noneEchoed = succeeded.none { (_, _, echoed) -> echoed }
            val allEqualTotal = succeeded.all { (_, cnt, _) -> cnt == unfilteredTotal }
            if (distinctModules.size >= 2 && noneEchoed && allEqualTotal) {
                return@coroutineScope emptyList()
            }
        }

        succeeded
            .filter { (_, cnt, _) -> cnt > 0L }
            .map { (et, cnt, _) ->
                VmsEventCountByTypeModel(
                    type = et,
                    count = cnt.toDouble()
                )
            }
    }

    private fun mergeRealtimeDeltasIntoByType(
        currentByType: List<VmsEventCountByTypeModel>,
        newEvents: List<EventData>,
        baselineMaxId: Int
    ): Pair<List<VmsEventCountByTypeModel>, Int> {
        if (baselineMaxId <= 0 || newEvents.isEmpty()) {
            val nextMax = maxOf(baselineMaxId, newEvents.maxOfOrNull { it.id } ?: 0)
            return currentByType to nextMax
        }
        val freshEvents = newEvents.filter { it.id > baselineMaxId && AiTypeHelper.isAiTypeEnabled(it.type) }
        if (freshEvents.isEmpty()) {
            return currentByType to baselineMaxId
        }
        val nextMaxId = maxOf(baselineMaxId, freshEvents.maxOf { it.id })
        val countsMap = LinkedHashMap<String, Double>()
        for (item in currentByType) {
            if (item.type.isNotBlank()) {
                countsMap[item.type] = (countsMap[item.type] ?: 0.0) + item.count
            }
        }
        for (ev in freshEvents) {
            val t = ev.type.trim()
            if (t.isNotEmpty()) {
                countsMap[t] = (countsMap[t] ?: 0.0) + 1.0
            }
        }
        val updated = countsMap.map { (type, cnt) ->
            VmsEventCountByTypeModel(type = type, count = cnt)
        }
        return updated to nextMaxId
    }

    private fun HomeUiState.withRecomputedMetrics(): HomeUiState {
        if (!UserPermissionManager.canAccessEvents()) {
            return copy(
                totalDetectionsCount = 0L,
                aiDistribution = emptyList()
            ).withRecomputedCameraMetrics()
        }
        val enabledByType = eventsByType.filter { AiTypeHelper.isAiTypeEnabled(it.type) }
        val sumByType = enabledByType
            .sumOf { it.count.toLong() }
        val isCustomUserAi = UserPermissionManager.isCustomAiEventPermission()
        val totalDetections = when {
            sumByType > 0L -> maxOf(sumByType, recentEvents.size.toLong())
            eventsByType.isNotEmpty() && isCustomUserAi -> {
                // Khi vms-event/count-by-type trả về danh sách theo từng bài AI nhưng không có bài nào thuộc phân quyền của user
                recentEvents.size.toLong()
            }
            else -> {
                val overviewCount = extractOverviewEventCount(overviewStats, selectedServiceId?.toString())
                if (overviewCount != null) {
                    maxOf(overviewCount, recentEvents.size.toLong())
                } else {
                    recentEvents.size.toLong()
                }
            }
        }

        // Tổng hợp phân bố sự kiện theo từng bài AI (gộp các eventTypes cùng module vào 1 dòng)
        data class ModuleBucket(
            val sensorType: String,
            val labelVi: String,
            val labelEn: String,
            var count: Long
        )
        val bucketMap = LinkedHashMap<String, ModuleBucket>()
        if (sumByType > 0L) {
            for (item in enabledByType) {
                val cnt = item.count.toLong()
                if (cnt <= 0L) continue
                val mod = AiTypeHelper.findModuleByEventType(item.type)
                val key = mod?.sensorType ?: item.type.trim()
                val existing = bucketMap[key]
                if (existing != null) {
                    existing.count += cnt
                } else {
                    bucketMap[key] = ModuleBucket(
                        sensorType = key,
                        labelVi = mod?.labelVi ?: AiTypeHelper.getTypeName(item.type, isEn = false),
                        labelEn = mod?.labelEn ?: AiTypeHelper.getTypeName(item.type, isEn = true),
                        count = cnt
                    )
                }
            }
        } else if (
            recentEvents.isNotEmpty() &&
            totalDetections <= recentEvents.size.toLong() &&
            !(eventsByType.isNotEmpty() && isCustomUserAi)
        ) {
            for (ev in recentEvents) {
                if (!AiTypeHelper.isAiTypeEnabled(ev.type)) continue
                val mod = AiTypeHelper.findModuleByEventType(ev.type)
                val key = mod?.sensorType ?: ev.type.trim()
                if (key.isEmpty()) continue
                val existing = bucketMap[key]
                if (existing != null) {
                    existing.count += 1L
                } else {
                    bucketMap[key] = ModuleBucket(
                        sensorType = key,
                        labelVi = mod?.labelVi ?: AiTypeHelper.getTypeName(ev.type, isEn = false),
                        labelEn = mod?.labelEn ?: AiTypeHelper.getTypeName(ev.type, isEn = true),
                        count = 1L
                    )
                }
            }
        }

        val totalBucketCount = bucketMap.values.sumOf { it.count }
        val distribution = if (totalBucketCount > 0L) {
            bucketMap.values
                .sortedByDescending { it.count }
                .map { b ->
                    HomeAiDistributionItem(
                        filterSensorType = b.sensorType,
                        labelVi = b.labelVi,
                        labelEn = b.labelEn,
                        count = b.count,
                        percentage = ((b.count.toFloat() * 100f) / totalBucketCount.toFloat()).coerceIn(0f, 100f)
                    )
                }
        } else {
            emptyList()
        }

        return copy(
            totalDetectionsCount = totalDetections,
            aiDistribution = if (distribution == aiDistribution) aiDistribution else distribution
        ).withRecomputedCameraMetrics()
    }

    private fun HomeUiState.withRecomputedCameraMetrics(): HomeUiState {
        val statusMap = latestCameraStatusMap
        val recMap = latestRecordStatusMap
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
                val wsStatus = statusMap["cam-0-${cam.id}"]
                    ?: statusMap["cam-${cam.vmsId}-${cam.cameraId}"]
                    ?: statusMap["cam-${cam.vmsId}-${cam.id}"]
                    ?: statusMap["cam-${cam.id}"]
                var isOffline = wsStatus == "red"
                if (!isOffline && wsStatus != "green") {
                    val issues = cam.extra?.latestReport?.issues
                    if (issues != null && (issues.contains("loi_ket_noi") || issues.contains("mat_ket_noi"))) {
                        isOffline = true
                    }
                }
                !isOffline
            }
        }

        val recordingCams = cameraList.count { cam ->
            val wsRecStatus = recMap["cam-0-${cam.id}"]
                ?: recMap["cam-${cam.vmsId}-${cam.cameraId}"]
                ?: recMap["cam-${cam.vmsId}-${cam.id}"]
                ?: recMap["cam-${cam.id}"]
            if (wsRecStatus != null) {
                wsRecStatus == "green"
            } else {
                cam.enableRecord || cam.extra?.recording == true || cam.extra?.recordStatus == "green"
            }
        }

        if (totalCamerasCount == totalCams &&
            onlineCamerasCount == onlineCams &&
            recordingCamerasCount == recordingCams
        ) {
            return this
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
        private const val BY_TYPE_WS_REFRESH_INTERVAL_MS = 10_000L
        private const val MAX_OVERVIEW_BY_TYPE_CONCURRENCY = 8

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
