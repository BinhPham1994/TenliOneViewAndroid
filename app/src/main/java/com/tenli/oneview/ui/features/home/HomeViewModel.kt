package com.tenli.oneview.ui.features.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tenli.oneview.data.local.CachedHomeData
import com.tenli.oneview.data.local.GlobalData
import com.tenli.oneview.data.local.HomeCacheManager
import com.tenli.oneview.data.network.api.AiApi
import com.tenli.oneview.data.network.api.BsApi
import com.tenli.oneview.data.network.api.EventApi
import com.tenli.oneview.data.network.api.VmsApi
import com.tenli.oneview.data.network.retrofit.LoginAuthClient
import com.tenli.oneview.model.network.CameraModel
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.model.network.VmsCountOverviewModel
import com.tenli.oneview.model.network.VmsEventCountByCameraModel
import com.tenli.oneview.model.network.VmsEventCountByTypeModel
import com.tenli.oneview.model.network.VmsEventStatisticalOverTimeModel
import com.tenli.oneview.ui.utils.AiTypeHelper
import com.tenli.oneview.util.toUserFriendlyMessage
import kotlinx.coroutines.CancellationException
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
import java.util.Calendar

enum class TimeFilter(val title: String, val titleEn: String = title) {
    TODAY("Hôm nay", "Today"),
    YESTERDAY("Hôm qua", "Yesterday"),
    LAST_7_DAYS("7 ngày qua", "Last 7 days"),
    CUSTOM_DATE("Tùy chọn ngày", "Select date"),
    CUSTOM_RANGE("Tùy chỉnh", "Custom");

    companion object {
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
                    val toTime = calendar.timeInMillis
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    Pair(calendar.timeInMillis, toTime)
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
            val dateFmt = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            val dateTimeFmt = java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault())
            return when (filter) {
                TODAY -> if (isEn) TODAY.titleEn else TODAY.title
                YESTERDAY -> if (isEn) YESTERDAY.titleEn else YESTERDAY.title
                LAST_7_DAYS -> if (isEn) LAST_7_DAYS.titleEn else LAST_7_DAYS.title
                CUSTOM_DATE -> {
                    val prefix = if (isEn) CUSTOM_DATE.titleEn else CUSTOM_DATE.title
                    "$prefix (${dateFmt.format(java.util.Date(customDateStartMs))})"
                }
                CUSTOM_RANGE -> {
                    "${dateTimeFmt.format(java.util.Date(customRangeFromMs))} - ${dateTimeFmt.format(java.util.Date(customRangeToMs))}"
                }
            }
        }
    }
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val selectedFilter: TimeFilter = TimeFilter.TODAY,
    val selectedServiceId: Int? = null,
    val overviewStats: List<VmsCountOverviewModel> = emptyList(),
    val eventsOverTime: List<VmsEventStatisticalOverTimeModel> = emptyList(),
    val eventsByType: List<VmsEventCountByTypeModel> = emptyList(),
    val eventsByCamera: List<VmsEventCountByCameraModel> = emptyList(),
    val cameraList: List<CameraModel> = emptyList(),
    val aiServices: List<com.tenli.oneview.model.network.AIServiceModel> = emptyList(),
    val recentEvents: List<EventData> = emptyList(),
    val cameraStatusMap: Map<String, String> = emptyMap(),
    val recordStatusMap: Map<String, String> = emptyMap(),
    val runningAiTasks: Int = 0,
    val totalAiTasks: Int = 0,
    val isListView: Boolean = true,
    val error: String? = null
)

@OptIn(FlowPreview::class)
class HomeViewModel(
    application: Application,
    private val webSocketManager: com.tenli.oneview.data.network.websocket.WebSocketManager
) : AndroidViewModel(application) {

    private val bsApi = LoginAuthClient.create(BsApi::class.java)
    private val eventApi = LoginAuthClient.create(EventApi::class.java)
    private val vmsApi = LoginAuthClient.create(VmsApi::class.java)
    private val aiApi = LoginAuthClient.create(AiApi::class.java)

    private val _uiState = MutableStateFlow(
        run {
            val isList = GlobalData.preferences.getString("mobile_event_view_mode", "list") != "grid"
            val memCached = HomeCacheManager.getHomeDataInMemory(TimeFilter.TODAY, null)
            if (memCached != null) {
                HomeUiState(
                    isLoading = false,
                    isListView = isList,
                    overviewStats = memCached.overviewStats,
                    eventsOverTime = memCached.eventsOverTime,
                    eventsByType = memCached.eventsByType,
                    eventsByCamera = memCached.eventsByCamera,
                    cameraList = memCached.cameraList.ifEmpty { com.tenli.oneview.data.local.EventCacheManager.getCamerasInMemory() },
                    aiServices = memCached.aiServices.ifEmpty { com.tenli.oneview.data.local.EventCacheManager.getAiServicesInMemory() },
                    recentEvents = memCached.recentEvents
                        .filter { ev -> AiTypeHelper.isAiTypeEnabled(ev.type) }
                        .take(5),
                    runningAiTasks = memCached.runningAiTasks,
                    totalAiTasks = memCached.totalAiTasks
                )
            } else {
                HomeUiState(
                    isListView = isList,
                    cameraList = com.tenli.oneview.data.local.EventCacheManager.getCamerasInMemory(),
                    aiServices = com.tenli.oneview.data.local.EventCacheManager.getAiServicesInMemory()
                )
            }
        }
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var dashboardJob: Job? = null
    private var recentEventsJob: Job? = null

    init {
        fetchDashboardData()
        listenToWebSocket()
        webSocketManager.connectAllReports()
    }

    fun toggleViewMode() {
        val nextIsList = !_uiState.value.isListView
        GlobalData.preferences.edit()
            .putString("mobile_event_view_mode", if (nextIsList) "list" else "grid")
            .apply()
        _uiState.update { it.copy(isListView = nextIsList) }
    }

    fun syncViewModeFromPrefs() {
        val savedIsList = GlobalData.preferences.getString("mobile_event_view_mode", "list") != "grid"
        if (_uiState.value.isListView != savedIsList) {
            _uiState.update { it.copy(isListView = savedIsList) }
        }
    }

    private fun listenToWebSocket() {
        viewModelScope.launch {
            Log.d("HomeViewModel", "Started listening to notifyEvent flow")
            webSocketManager.notifyEvent
                .debounce(600L)
                .collect { _ ->
                    Log.d("HomeViewModel", "notifyEvent debounced -> filter=${_uiState.value.selectedFilter}")
                    if (_uiState.value.selectedFilter == TimeFilter.TODAY) {
                        fetchRecentEvents()
                    }
                }
        }
        viewModelScope.launch {
            webSocketManager.cameraStatusMap
                .debounce(800L)
                .collect { statusMap ->
                    _uiState.update { it.copy(cameraStatusMap = statusMap) }
                }
        }
        viewModelScope.launch {
            webSocketManager.recordStatusMap
                .debounce(800L)
                .collect { recMap ->
                    _uiState.update { it.copy(recordStatusMap = recMap) }
                }
        }
        viewModelScope.launch {
            com.tenli.oneview.util.EventBus.eventReportedFalse.collect { eventId ->
                _uiState.update { state ->
                    state.copy(
                        recentEvents = state.recentEvents.filter { it.id != eventId }
                    )
                }
            }
        }
    }

    /**
     * Khớp hoàn toàn với MobileHomePage.vue:
     * Fetch theo batch 50 sự kiện (tối đa 3 lần), lọc các loại AI được bật (AiTypeHelper.isAiTypeEnabled),
     * loại trùng ID và chỉ giữ đúng 5 sự kiện mới nhất (permittedEvents.slice(0, 5)).
     */
    private suspend fun fetchPermittedRecentEvents(
        fromSec: Long,
        toSec: Long,
        serviceId: Int?
    ): List<EventData> {
        val permitted = mutableListOf<EventData>()
        val seenIds = mutableSetOf<Int>()
        var lastId: Int? = null
        val batchSize = 50
        var attempts = 0
        val maxAttempts = 3

        while (permitted.size < 5 && attempts < maxAttempts) {
            attempts++
            val resp = runCatching {
                eventApi.getDataList(
                    count = batchSize,
                    lastId = lastId,
                    from = fromSec,
                    to = toSec,
                    serviceId = serviceId
                )
            }.getOrNull()

            if (resp == null || !resp.isSuccessful) break
            val data = resp.body() ?: emptyList()
            if (data.isEmpty()) break

            for (ev in data) {
                if (AiTypeHelper.isAiTypeEnabled(ev.type) && !seenIds.contains(ev.id)) {
                    seenIds.add(ev.id)
                    permitted.add(ev)
                    if (permitted.size >= 5) break
                }
            }

            if (data.size < batchSize) break
            lastId = data.last().id
        }

        return permitted.take(5)
    }

    private fun fetchRecentEvents() {
        recentEventsJob?.cancel()
        recentEventsJob = viewModelScope.launch {
            try {
                val calendar = Calendar.getInstance()
                val toTime = calendar.timeInMillis
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val fromTime = calendar.timeInMillis

                val newEvents = fetchPermittedRecentEvents(
                    fromSec = fromTime / 1000,
                    toSec = toTime / 1000,
                    serviceId = _uiState.value.selectedServiceId
                )
                if (newEvents.isNotEmpty()) {
                    com.tenli.oneview.data.local.EventCacheManager.cacheInMemory(events = newEvents)
                    _uiState.update { state ->
                        val merged = (newEvents + state.recentEvents)
                            .filter { AiTypeHelper.isAiTypeEnabled(it.type) }
                            .distinctBy { it.id }
                            .take(5)
                        state.copy(recentEvents = merged)
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching recent events via WebSocket", e)
            }
        }
    }

    fun setTimeFilter(filter: TimeFilter) {
        val isCustom = filter == TimeFilter.CUSTOM_DATE || filter == TimeFilter.CUSTOM_RANGE
        if (!isCustom && _uiState.value.selectedFilter == filter) return
        _uiState.update { it.copy(selectedFilter = filter) }
        fetchDashboardData()
    }

    fun setServiceFilter(serviceId: Int?) {
        if (_uiState.value.selectedServiceId == serviceId) return
        _uiState.update { it.copy(selectedServiceId = serviceId) }
        fetchDashboardData()
    }

    fun applyFilters(filter: TimeFilter, serviceId: Int?) {
        val isCustom = filter == TimeFilter.CUSTOM_DATE || filter == TimeFilter.CUSTOM_RANGE
        if (!isCustom && _uiState.value.selectedFilter == filter && _uiState.value.selectedServiceId == serviceId) return
        _uiState.update { it.copy(selectedFilter = filter, selectedServiceId = serviceId) }
        fetchDashboardData()
    }

    fun resetFilters() {
        applyFilters(TimeFilter.TODAY, null)
    }

    fun fetchDashboardData() {
        dashboardJob?.cancel()
        dashboardJob = viewModelScope.launch {
            val filter = _uiState.value.selectedFilter
            val serviceId = _uiState.value.selectedServiceId

            // 1. Ưu tiên đọc ngay dữ liệu từ RAM / Disk Cache (Stale-While-Revalidate)
            val cachedData = HomeCacheManager.getHomeData(getApplication(), filter, serviceId)
            if (cachedData != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = null,
                        overviewStats = cachedData.overviewStats,
                        eventsOverTime = cachedData.eventsOverTime,
                        eventsByType = cachedData.eventsByType,
                        eventsByCamera = cachedData.eventsByCamera,
                        cameraList = cachedData.cameraList.ifEmpty { it.cameraList },
                        aiServices = cachedData.aiServices.ifEmpty { it.aiServices },
                        recentEvents = cachedData.recentEvents
                            .filter { ev -> AiTypeHelper.isAiTypeEnabled(ev.type) }
                            .take(5),
                        runningAiTasks = if (cachedData.totalAiTasks > 0) cachedData.runningAiTasks else it.runningAiTasks,
                        totalAiTasks = if (cachedData.totalAiTasks > 0) cachedData.totalAiTasks else it.totalAiTasks
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }

            try {
                val (fromTime, toTime) = TimeFilter.resolveTimeRangeMillis(filter)

                // 2. Gọi song song tất cả các API Dashboard để rút ngắn thời gian phản hồi xuống 1 RTT duy nhất
                coroutineScope {
                    val serviceIdStr = serviceId?.toString()
                    val overviewDeferred = async {
                        runCatching {
                            bsApi.getVmsCountOverview(
                                serviceId = serviceIdStr ?: "-1",
                                fromTime = fromTime,
                                toTime = toTime
                            )
                        }.getOrNull()
                    }
                    val overTimeDeferred = async {
                        runCatching {
                            bsApi.getVmsEventStatsOverTime(
                                fromTime = fromTime,
                                toTime = toTime,
                                serviceId = serviceIdStr
                            )
                        }.getOrNull()
                    }
                    val byTypeDeferred = async {
                        runCatching {
                            bsApi.getVmsEventCountByType(
                                fromTime = fromTime,
                                toTime = toTime,
                                serviceId = serviceIdStr
                            )
                        }.getOrNull()
                    }
                    val byCameraDeferred = async {
                        runCatching {
                            bsApi.getVmsEventCountByCamera(
                                fromTime = fromTime,
                                toTime = toTime,
                                serviceId = serviceIdStr
                            )
                        }.getOrNull()
                    }
                    val recentEventsDeferred = async {
                        fetchPermittedRecentEvents(
                            fromSec = fromTime / 1000,
                            toSec = toTime / 1000,
                            serviceId = serviceId
                        )
                    }
                    val cameraDeferred = async {
                        runCatching { vmsApi.getCameraList() }.getOrNull()
                    }
                    val aiServiceDeferred = async {
                        runCatching { eventApi.getAIServiceList() }.getOrNull()
                    }

                    // Pre-warm danh sách hồ sơ khuôn mặt trong nền nếu chưa có để màn Chi tiết sự kiện mở 0ms
                    launch {
                        if (com.tenli.oneview.data.local.EventCacheManager.getProfilesInMemory().isEmpty()) {
                            val profiles = runCatching { bsApi.getProfileList() }.getOrNull()?.body() ?: emptyList()
                            val groups = runCatching { bsApi.getProfileGroupList() }.getOrNull()?.body() ?: emptyList()
                            if (profiles.isNotEmpty() || groups.isNotEmpty()) {
                                com.tenli.oneview.data.local.EventCacheManager.saveProfilesCache(getApplication(), profiles, groups)
                            }
                        }
                    }

                    val overviewResponse = overviewDeferred.await()
                    val overTimeResponse = overTimeDeferred.await()
                    val byTypeResponse = byTypeDeferred.await()
                    val byCameraResponse = byCameraDeferred.await()
                    val recentEvents = recentEventsDeferred.await()
                    val cameraResponse = cameraDeferred.await()
                    val aiServiceResponse = aiServiceDeferred.await()

                    val overviewStats = if (overviewResponse?.isSuccessful == true) {
                        overviewResponse.body() ?: emptyList()
                    } else _uiState.value.overviewStats

                    val eventsOverTime = if (overTimeResponse?.isSuccessful == true) {
                        overTimeResponse.body() ?: emptyList()
                    } else _uiState.value.eventsOverTime

                    val eventsByType = if (byTypeResponse?.isSuccessful == true) {
                        byTypeResponse.body() ?: emptyList()
                    } else _uiState.value.eventsByType

                    val eventsByCamera = if (byCameraResponse?.isSuccessful == true) {
                        byCameraResponse.body() ?: emptyList()
                    } else _uiState.value.eventsByCamera

                    val cameraList = if (cameraResponse?.isSuccessful == true) {
                        cameraResponse.body() ?: emptyList()
                    } else _uiState.value.cameraList

                    val aiServices = if (aiServiceResponse?.isSuccessful == true) {
                        aiServiceResponse.body() ?: emptyList()
                    } else _uiState.value.aiServices

                    // Compute running AI tasks & total AI tasks matching MobileHomePage.vue (song song trên các AI Service)
                    var runningTasks = 0
                    var totalTasks = 0
                    try {
                        val targetServices = if (serviceId != null) {
                            aiServices.filter { it.id == serviceId }
                        } else {
                            aiServices
                        }
                        if (targetServices.isEmpty()) {
                            val monResp = aiApi.getMonitorList()
                            if (monResp.isSuccessful) {
                                val monitors = (monResp.body() ?: emptyList()).filter { AiTypeHelper.isAiTypeEnabled(it.type) }
                                totalTasks = monitors.size
                                runningTasks = monitors.count { it.enabled }
                            }
                        } else {
                            val monitorResults = targetServices.map { srv ->
                                async {
                                    val host = srv.publicHost.ifBlank { srv.privateHost }.trimEnd('/')
                                    runCatching {
                                        if (host.isNotBlank() && srv.id != 0) {
                                            aiApi.getMonitorListExt(
                                                url = "$host/api/Monitor",
                                                authHeader = "Bearer ${srv.apiKey}"
                                            )
                                        } else {
                                            aiApi.getMonitorList()
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
                    } catch (e: Exception) {
                        Log.w("HomeViewModel", "Fallback monitor count from overviewStats", e)
                    }
                    if (totalTasks == 0) {
                        val overviewMonitorCount = overviewStats
                            .find { it.label == "monitor-count" && it.tag == "all" }
                            ?.count?.toString()?.toDoubleOrNull()?.toInt() ?: 0
                        totalTasks = overviewMonitorCount
                        runningTasks = overviewMonitorCount
                    }

                    var errorMsg: String? = null
                    if (overviewResponse != null && !overviewResponse.isSuccessful && cachedData == null) {
                        errorMsg = "Lỗi Overview: ${overviewResponse.code()}"
                    }

                    val finalRecentEvents = if (recentEvents.isNotEmpty() || cachedData == null) {
                        recentEvents
                    } else {
                        _uiState.value.recentEvents
                    }

                    com.tenli.oneview.data.local.EventCacheManager.cacheInMemory(
                        events = finalRecentEvents,
                        cameras = cameraList,
                        aiServices = aiServices
                    )

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            overviewStats = overviewStats,
                            eventsOverTime = eventsOverTime,
                            eventsByType = eventsByType,
                            eventsByCamera = eventsByCamera,
                            cameraList = cameraList,
                            aiServices = aiServices,
                            recentEvents = finalRecentEvents,
                            runningAiTasks = runningTasks,
                            totalAiTasks = totalTasks,
                            error = errorMsg
                        )
                    }

                    if (overviewResponse?.isSuccessful == true) {
                        val newCachedData = CachedHomeData(
                            overviewStats = overviewStats,
                            eventsOverTime = eventsOverTime,
                            eventsByType = eventsByType,
                            eventsByCamera = eventsByCamera,
                            cameraList = cameraList,
                            aiServices = aiServices,
                            recentEvents = finalRecentEvents,
                            runningAiTasks = runningTasks,
                            totalAiTasks = totalTasks,
                            updatedAt = System.currentTimeMillis()
                        )
                        HomeCacheManager.saveHomeData(getApplication(), filter, serviceId, newCachedData)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching data", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = if (cachedData == null) e.toUserFriendlyMessage() else null
                    )
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as com.tenli.oneview.TenliApp
                HomeViewModel(application, application.container.webSocketManager)
            }
        }
    }
}
