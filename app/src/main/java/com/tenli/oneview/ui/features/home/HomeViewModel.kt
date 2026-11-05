package com.tenli.oneview.ui.features.home

import android.app.Application
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tenli.oneview.BuildConfig
import com.tenli.oneview.TenliApp
import com.tenli.oneview.data.local.CachedHomeData
import com.tenli.oneview.data.local.EventCacheManager
import com.tenli.oneview.data.local.GlobalData
import com.tenli.oneview.data.local.HomeCacheManager
import com.tenli.oneview.data.network.ApiProvider
import com.tenli.oneview.data.network.websocket.WebSocketManager
import com.tenli.oneview.model.network.AIServiceModel
import com.tenli.oneview.model.network.CameraModel
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.model.network.VmsCountOverviewModel
import com.tenli.oneview.model.network.VmsEventCountByTypeModel
import com.tenli.oneview.ui.utils.AiTypeHelper
import com.tenli.oneview.util.EventBus
import com.tenli.oneview.util.toUserFriendlyMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
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

        /**
         * Khớp hoàn toàn với getTimeRange() trong useTimeRange.ts của tenli-oneview:
         * - TODAY: 00:00:00.000 -> 23:59:59.999 của ngày hôm nay (tránh lệch vài giây đồng hồ giữa máy và server làm sót sự kiện mới vừa nổ qua socket)
         * - YESTERDAY: 00:00:00.000 -> 23:59:59.999 của ngày hôm qua
         * - LAST_7_DAYS: 00:00:00.000 của 7 ngày trước -> 23:59:59.999 của ngày hôm nay
         */
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
    private val webSocketManager: WebSocketManager
) : AndroidViewModel(application) {

    private val bsApi get() = ApiProvider.bsApi
    private val eventApi get() = ApiProvider.eventApi
    private val vmsApi get() = ApiProvider.vmsApi
    private val aiApi get() = ApiProvider.aiApi

    private val _uiState = MutableStateFlow(buildInitialState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var dashboardJob: Job? = null
    private var realtimeRefreshJob: Job? = null
    private var trailingNotifyJob: Job? = null
    private var followUpStatsJob: Job? = null
    @Volatile private var lastRealtimeRefreshAt: Long = System.currentTimeMillis()
    @Volatile private var isHomeTabActive: Boolean = true

    init {
        fetchDashboardData()
        listenToWebSocket()
        startPeriodicPolling()
        webSocketManager.connectAllReports()
    }

    private fun buildInitialState(): HomeUiState {
        val isList = GlobalData.preferences.getString(PREF_VIEW_MODE, "list") != "grid"
        val memCached = HomeCacheManager.getHomeDataInMemory(TimeFilter.TODAY, null)
        val liveCamMap = webSocketManager.cameraStatusMap.value
        val liveRecMap = webSocketManager.recordStatusMap.value
        return if (memCached != null) {
            val cameras = memCached.cameraList.ifEmpty { EventCacheManager.getCamerasInMemory() }
            val services = memCached.aiServices.ifEmpty { EventCacheManager.getAiServicesInMemory() }
            HomeUiState(
                isLoading = false,
                isListView = isList,
                overviewStats = memCached.overviewStats,
                eventsByType = memCached.eventsByType,
                cameraList = cameras,
                aiServices = services,
                recentEvents = memCached.recentEvents
                    .filter { ev -> AiTypeHelper.isAiTypeEnabled(ev.type) }
                    .take(MAX_RECENT_EVENTS),
                cameraStatusMap = memCached.cameraStatusMap.orEmpty() + liveCamMap,
                recordStatusMap = memCached.recordStatusMap.orEmpty() + liveRecMap,
                runningAiTasks = memCached.runningAiTasks,
                totalAiTasks = memCached.totalAiTasks
            ).withRecomputedMetrics()
        } else {
            val cameras = EventCacheManager.getCamerasInMemory()
            HomeUiState(
                isLoading = true,
                isListView = isList,
                cameraList = cameras,
                aiServices = EventCacheManager.getAiServicesInMemory(),
                cameraStatusMap = liveCamMap,
                recordStatusMap = liveRecMap
            ).withRecomputedMetrics()
        }
    }

    fun toggleViewMode() {
        val nextIsList = !_uiState.value.isListView
        GlobalData.preferences.edit {
            putString(PREF_VIEW_MODE, if (nextIsList) "list" else "grid")
        }
        _uiState.update { it.copy(isListView = nextIsList) }
    }

    fun syncViewModeFromPrefs() {
        val savedIsList = GlobalData.preferences.getString(PREF_VIEW_MODE, "list") != "grid"
        if (_uiState.value.isListView != savedIsList) {
            _uiState.update { it.copy(isListView = savedIsList) }
        }
    }

    fun setHomeTabActive(active: Boolean) {
        if (isHomeTabActive == active) return
        isHomeTabActive = active
        if (active && _uiState.value.selectedFilter != TimeFilter.YESTERDAY) {
            val elapsed = System.currentTimeMillis() - lastRealtimeRefreshAt
            if (elapsed >= POLL_INTERVAL_MS / 2) {
                scheduleRealtimeRefresh()
            }
        }
    }

    private fun listenToWebSocket() {
        viewModelScope.launch {
            webSocketManager.notifyEvent
                .debounce(NOTIFY_DEBOUNCE_MS)
                .collect { _ ->
                    // Khớp useAiEventSocket('all', () => fetchDashboardStats(false), { minInterval: 8000 }) trong MobileHomePage.vue:
                    // Gộp các sự kiện nổ dồn dập và giữ trailing timer để sự kiện đến trong khoảng throttle 8s luôn được cập nhật đầy đủ.
                    if (_uiState.value.selectedFilter != TimeFilter.YESTERDAY) {
                        scheduleRealtimeRefresh()
                    }
                }
        }
        viewModelScope.launch(Dispatchers.Default) {
            webSocketManager.cameraStatusMap
                .debounce(STATUS_DEBOUNCE_MS)
                .collect { statusMap ->
                    if (statusMap.isEmpty()) return@collect
                    _uiState.update { state ->
                        val merged = state.cameraStatusMap + statusMap
                        if (merged == state.cameraStatusMap) state
                        else state.copy(cameraStatusMap = merged).withRecomputedCameraMetrics()
                    }
                    HomeCacheManager.updateCameraAndRecordStatus(
                        context = getApplication(),
                        cameraStatusMap = statusMap,
                        recordStatusMap = emptyMap()
                    )
                }
        }
        viewModelScope.launch(Dispatchers.Default) {
            webSocketManager.recordStatusMap
                .debounce(STATUS_DEBOUNCE_MS)
                .collect { recMap ->
                    if (recMap.isEmpty()) return@collect
                    _uiState.update { state ->
                        val merged = state.recordStatusMap + recMap
                        if (merged == state.recordStatusMap) state
                        else state.copy(recordStatusMap = merged).withRecomputedCameraMetrics()
                    }
                    HomeCacheManager.updateCameraAndRecordStatus(
                        context = getApplication(),
                        cameraStatusMap = emptyMap(),
                        recordStatusMap = recMap
                    )
                }
        }
        viewModelScope.launch {
            EventBus.eventReportedFalse.collect { eventId ->
                _uiState.update { state ->
                    state.copy(recentEvents = state.recentEvents.filter { it.id != eventId })
                }
                scheduleRealtimeRefresh(forceImmediate = true)
            }
        }
    }

    /**
     * Khớp startPolling() (30s/lần) trong MobileHomePage.vue:
     * Tự động làm mới thống kê & sự kiện gần đây định kỳ để luôn đồng bộ số liệu với server.
     */
    private fun startPeriodicPolling() {
        viewModelScope.launch {
            while (isActive) {
                delay(POLL_INTERVAL_MS)
                if (isHomeTabActive && _uiState.value.selectedFilter != TimeFilter.YESTERDAY) {
                    lastRealtimeRefreshAt = System.currentTimeMillis()
                    refreshRealtimeStatsAndEvents()
                }
            }
        }
    }

    private fun scheduleRealtimeRefresh(forceImmediate: Boolean = false) {
        val now = System.currentTimeMillis()
        val elapsed = now - lastRealtimeRefreshAt
        if (forceImmediate || elapsed >= NOTIFY_MIN_INTERVAL_MS) {
            trailingNotifyJob?.cancel()
            trailingNotifyJob = null
            lastRealtimeRefreshAt = now
            refreshRealtimeStatsAndEvents()
        } else if (trailingNotifyJob?.isActive != true) {
            val remainingMs = (NOTIFY_MIN_INTERVAL_MS - elapsed).coerceAtLeast(500L)
            trailingNotifyJob = viewModelScope.launch {
                delay(remainingMs)
                lastRealtimeRefreshAt = System.currentTimeMillis()
                refreshRealtimeStatsAndEvents()
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
        val seenIds = HashSet<Int>()
        var lastId: Int? = null
        var attempts = 0

        while (permitted.size < MAX_RECENT_EVENTS && attempts < MAX_BATCH_ATTEMPTS) {
            attempts++
            val resp = runCatching {
                eventApi.getDataList(
                    count = RECENT_BATCH_SIZE,
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
                if (AiTypeHelper.isAiTypeEnabled(ev.type) && seenIds.add(ev.id)) {
                    permitted.add(ev)
                    if (permitted.size >= MAX_RECENT_EVENTS) break
                }
            }

            if (data.size < RECENT_BATCH_SIZE) break
            lastId = data.last().id
        }

        return permitted.take(MAX_RECENT_EVENTS)
    }

    /**
     * Khớp fetchDashboardStats(false) trong MobileHomePage.vue (dòng 233-244):
     * Khi nhận sự kiện mới từ WebSocket (/Data/ws/Notify) hoặc tới chu kỳ poll 30s,
     * gọi song song cả 3 API: count-overview, count-by-type và danh sách sự kiện gần đây
     * để cả thẻ "TỔNG SỰ KIỆN" lẫn danh sách "Sự kiện gần đây" đều cập nhật tức thì.
     */
    private fun refreshRealtimeStatsAndEvents() {
        if (dashboardJob?.isActive == true) return
        realtimeRefreshJob?.cancel()
        realtimeRefreshJob = viewModelScope.launch {
            try {
                val stateSnapshot = _uiState.value
                val filter = stateSnapshot.selectedFilter
                val serviceId = stateSnapshot.selectedServiceId
                val serviceIdStr = serviceId?.toString()
                val (fromTime, toTime) = TimeFilter.resolveTimeRangeMillis(filter)

                coroutineScope {
                    val overviewDeferred = async {
                        runCatching {
                            bsApi.getVmsCountOverview(
                                serviceId = serviceIdStr ?: "-1",
                                fromTime = fromTime,
                                toTime = toTime
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
                    val recentEventsDeferred = async {
                        fetchPermittedRecentEvents(
                            fromSec = fromTime / 1000,
                            toSec = toTime / 1000,
                            serviceId = serviceId
                        )
                    }

                    val overviewResponse = overviewDeferred.await()
                    val byTypeResponse = byTypeDeferred.await()
                    val newEvents = recentEventsDeferred.await()

                    val curState = _uiState.value
                    val nextOverview = if (overviewResponse?.isSuccessful == true) {
                        overviewResponse.body() ?: curState.overviewStats
                    } else {
                        curState.overviewStats
                    }
                    val nextByType = if (byTypeResponse?.isSuccessful == true) {
                        byTypeResponse.body() ?: curState.eventsByType
                    } else {
                        curState.eventsByType
                    }

                    val previousIds = curState.recentEvents.mapTo(HashSet()) { it.id }
                    val previousMaxId = curState.recentEvents.maxOfOrNull { it.id } ?: 0
                    val newlyAddedCount = newEvents.count { it.id > previousMaxId && it.id !in previousIds }

                    val mergedEvents = if (newEvents.isNotEmpty()) {
                        EventCacheManager.cacheInMemory(events = newEvents)
                        val merged = withContext(Dispatchers.Default) {
                            (newEvents + curState.recentEvents)
                                .filter { AiTypeHelper.isAiTypeEnabled(it.type) }
                                .distinctBy { it.id }
                                .take(MAX_RECENT_EVENTS)
                        }
                        HomeCacheManager.preloadEventThumbnails(
                            context = getApplication(),
                            events = merged,
                            diskOnly = false
                        )
                        merged
                    } else {
                        curState.recentEvents
                    }

                    val previousTotal = curState.totalDetectionsCount
                    val recomputedState = withContext(Dispatchers.Default) {
                        curState.copy(
                            overviewStats = nextOverview,
                            eventsByType = nextByType,
                            recentEvents = mergedEvents
                        ).withRecomputedMetrics()
                    }

                    // Nếu danh sách Sự kiện gần đây đã có thêm sự kiện mới nhưng API thống kê BS chậm cập nhật vài trăm ms,
                    // cộng ngay số sự kiện mới vào TỔNG SỰ KIỆN để đồng bộ tức thì với danh sách, đồng thời hẹn 1 lần đồng bộ lại sau 2.5s.
                    val needsFollowUpSync = newlyAddedCount > 0 && recomputedState.totalDetectionsCount <= previousTotal
                    val finalState = if (needsFollowUpSync) {
                        recomputedState.copy(totalDetectionsCount = previousTotal + newlyAddedCount)
                    } else {
                        recomputedState
                    }
                    _uiState.value = finalState

                    val newCachedData = CachedHomeData(
                        overviewStats = finalState.overviewStats,
                        eventsByType = finalState.eventsByType,
                        cameraList = finalState.cameraList,
                        aiServices = finalState.aiServices,
                        recentEvents = finalState.recentEvents,
                        runningAiTasks = finalState.runningAiTasks,
                        totalAiTasks = finalState.totalAiTasks,
                        cameraStatusMap = finalState.cameraStatusMap,
                        recordStatusMap = finalState.recordStatusMap,
                        updatedAt = System.currentTimeMillis()
                    )
                    HomeCacheManager.saveHomeData(getApplication(), filter, serviceId, newCachedData)

                    if (needsFollowUpSync) {
                        scheduleFollowUpStatsSync(filter, serviceId)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) Log.e(TAG, "Error refreshing realtime stats and events", e)
            }
        }
    }

    private fun scheduleFollowUpStatsSync(filter: TimeFilter, serviceId: Int?) {
        followUpStatsJob?.cancel()
        followUpStatsJob = viewModelScope.launch {
            delay(FOLLOW_UP_STATS_SYNC_DELAY_MS)
            if (_uiState.value.selectedFilter != filter || _uiState.value.selectedServiceId != serviceId) return@launch
            val serviceIdStr = serviceId?.toString()
            val (fromTime, toTime) = TimeFilter.resolveTimeRangeMillis(filter)
            coroutineScope {
                val overviewDeferred = async {
                    runCatching {
                        bsApi.getVmsCountOverview(
                            serviceId = serviceIdStr ?: "-1",
                            fromTime = fromTime,
                            toTime = toTime
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
                val overviewResp = overviewDeferred.await()
                val byTypeResp = byTypeDeferred.await()
                if (overviewResp?.isSuccessful == true || byTypeResp?.isSuccessful == true) {
                    _uiState.update { state ->
                        val nextOverview = if (overviewResp?.isSuccessful == true) {
                            overviewResp.body() ?: state.overviewStats
                        } else state.overviewStats
                        val nextByType = if (byTypeResp?.isSuccessful == true) {
                            byTypeResp.body() ?: state.eventsByType
                        } else state.eventsByType
                        val recomputed = state.copy(
                            overviewStats = nextOverview,
                            eventsByType = nextByType
                        ).withRecomputedMetrics()
                        if (recomputed.totalDetectionsCount >= state.totalDetectionsCount) {
                            recomputed
                        } else {
                            recomputed.copy(totalDetectionsCount = state.totalDetectionsCount)
                        }
                    }
                }
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
        realtimeRefreshJob?.cancel()
        followUpStatsJob?.cancel()
        dashboardJob = viewModelScope.launch {
            val filter = _uiState.value.selectedFilter
            val serviceId = _uiState.value.selectedServiceId

            // 1. Ưu tiên đọc ngay dữ liệu từ RAM / Disk Cache (Stale-While-Revalidate) khi chưa phải thao tác kéo refresh
            val alreadyWarmFromInit = !isUserRefresh &&
                !_uiState.value.isLoading &&
                _uiState.value.recentEvents.isNotEmpty() &&
                filter == TimeFilter.TODAY &&
                serviceId == null
            val cachedData = HomeCacheManager.getHomeData(getApplication(), filter, serviceId)
            if (!isUserRefresh && !alreadyWarmFromInit) {
                if (cachedData != null) {
                    HomeCacheManager.preloadEventThumbnails(
                        context = getApplication(),
                        events = cachedData.recentEvents,
                        diskOnly = true
                    )
                    val updatedFromCache = withContext(Dispatchers.Default) {
                        val cur = _uiState.value
                        val liveCamMap = webSocketManager.cameraStatusMap.value
                        val liveRecMap = webSocketManager.recordStatusMap.value
                        cur.copy(
                            isLoading = false,
                            error = null,
                            overviewStats = cachedData.overviewStats,
                            eventsByType = cachedData.eventsByType,
                            cameraList = cachedData.cameraList.ifEmpty { cur.cameraList },
                            aiServices = cachedData.aiServices.ifEmpty { cur.aiServices },
                            recentEvents = cachedData.recentEvents
                                .filter { ev -> AiTypeHelper.isAiTypeEnabled(ev.type) }
                                .take(MAX_RECENT_EVENTS),
                            cameraStatusMap = (cachedData.cameraStatusMap ?: cur.cameraStatusMap) + liveCamMap,
                            recordStatusMap = (cachedData.recordStatusMap ?: cur.recordStatusMap) + liveRecMap,
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
                        runCatching {
                            bsApi.getVmsCountOverview(
                                serviceId = serviceIdStr ?: "-1",
                                fromTime = fromTime,
                                toTime = toTime
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
                        if (EventCacheManager.getProfilesInMemory().isEmpty()) {
                            val profiles = runCatching { bsApi.getProfileList() }.getOrNull()?.body() ?: emptyList()
                            val groups = runCatching { bsApi.getProfileGroupList() }.getOrNull()?.body() ?: emptyList()
                            if (profiles.isNotEmpty() || groups.isNotEmpty()) {
                                EventCacheManager.saveProfilesCache(getApplication(), profiles, groups)
                            }
                        }
                    }

                    val overviewResponse = overviewDeferred.await()
                    val byTypeResponse = byTypeDeferred.await()
                    val recentEvents = recentEventsDeferred.await()
                    val cameraResponse = cameraDeferred.await()
                    val aiServiceResponse = aiServiceDeferred.await()

                    val overviewStats = if (overviewResponse?.isSuccessful == true) {
                        overviewResponse.body() ?: emptyList()
                    } else _uiState.value.overviewStats

                    val eventsByType = if (byTypeResponse?.isSuccessful == true) {
                        byTypeResponse.body() ?: emptyList()
                    } else _uiState.value.eventsByType

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
                        if (BuildConfig.DEBUG) Log.w(TAG, "Fallback monitor count from overviewStats", e)
                    }
                    if (totalTasks == 0) {
                        val overviewMonitorCount = overviewStats
                            .find { it.label == "monitor-count" && it.tag == "all" }
                            ?.count?.toString()?.toDoubleOrNull()?.toInt() ?: 0
                        if (overviewMonitorCount > 0) {
                            totalTasks = overviewMonitorCount
                            runningTasks = overviewMonitorCount
                        } else if (_uiState.value.totalAiTasks > 0) {
                            totalTasks = _uiState.value.totalAiTasks
                            runningTasks = _uiState.value.runningAiTasks
                        }
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

                    EventCacheManager.cacheInMemory(
                        events = finalRecentEvents,
                        cameras = cameraList,
                        aiServices = aiServices
                    )
                    // Ánh xạ lại dữ liệu Report WebSocket vào tất cả khoá cam-{vmsId}-{id}/{cameraId} khi đã có cameraList
                    webSocketManager.reapplyCameraReports()
                    webSocketManager.connectAllReports()

                    if (finalRecentEvents.isNotEmpty()) {
                        HomeCacheManager.preloadEventThumbnails(
                            context = getApplication(),
                            events = finalRecentEvents,
                            diskOnly = false
                        )
                    }

                    val nextState = withContext(Dispatchers.Default) {
                        val liveCamMap = webSocketManager.cameraStatusMap.value
                        val liveRecMap = webSocketManager.recordStatusMap.value
                        _uiState.value.copy(
                            isLoading = false,
                            isRefreshing = false,
                            overviewStats = overviewStats,
                            eventsByType = eventsByType,
                            cameraList = cameraList,
                            aiServices = aiServices,
                            recentEvents = finalRecentEvents,
                            cameraStatusMap = _uiState.value.cameraStatusMap + liveCamMap,
                            recordStatusMap = _uiState.value.recordStatusMap + liveRecMap,
                            runningAiTasks = runningTasks,
                            totalAiTasks = totalTasks,
                            error = errorMsg
                        ).withRecomputedMetrics()
                    }
                    _uiState.value = nextState
                    lastRealtimeRefreshAt = System.currentTimeMillis()

                    if (overviewResponse?.isSuccessful == true) {
                        val newCachedData = CachedHomeData(
                            overviewStats = overviewStats,
                            eventsByType = eventsByType,
                            cameraList = cameraList,
                            aiServices = aiServices,
                            recentEvents = finalRecentEvents,
                            runningAiTasks = runningTasks,
                            totalAiTasks = totalTasks,
                            cameraStatusMap = nextState.cameraStatusMap,
                            recordStatusMap = nextState.recordStatusMap,
                            updatedAt = System.currentTimeMillis()
                        )
                        HomeCacheManager.saveHomeData(getApplication(), filter, serviceId, newCachedData)
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
                        error = if (cachedData == null) e.toUserFriendlyMessage() else null
                    )
                }
            }
        }
    }

    /**
     * Khớp hoàn toàn với totalDetectionsCount trong MobileHomePage.vue (dòng 459-467):
     * Ưu tiên tổng từ vmsEventCountByType (các loại AI được phép), fallback về vmsCountOverview (event-count/all)
     * hoặc recentEvents.size.
     */
    private fun HomeUiState.withRecomputedMetrics(): HomeUiState {
        val sumByType = eventsByType
            .filter { AiTypeHelper.isAiTypeEnabled(it.type) }
            .sumOf { it.count.toString().toDoubleOrNull()?.toLong() ?: 0L }
        val totalDetections = if (sumByType > 0L) {
            sumByType
        } else {
            val fromOverview = overviewStats
                .find { it.label == "event-count" && it.tag == "all" }
                ?.count?.toString()?.toDoubleOrNull()?.toLong() ?: 0L
            if (fromOverview > 0L) fromOverview else recentEvents.size.toLong()
        }
        return copy(totalDetectionsCount = totalDetections).withRecomputedCameraMetrics()
    }

    /**
     * Khớp hoàn toàn với totalCamerasCount, onlineCamerasCount, recordingCamerasCount
     * trong MobileHomePage.vue (dòng 343-407):
     * - totalCamerasCount: ưu tiên camera-count (all) từ overviewStats nếu > 0, fallback về cameraList.size.
     * - onlineCamerasCount: chỉ tính là offline khi status == "red" hoặc cam.extra.latestReport.issues
     *   chứa "loi_ket_noi" / "mat_ket_noi" (KHÔNG coi "orange" là offline vì camera kết nối bình thường nhưng
     *   không bật ghi hình sẽ có status tổng hợp là "orange").
     * - recordingCamerasCount: ưu tiên recordStatusMap == "green", fallback về cam.enableRecord / extra.recording / extra.recordStatus.
     */
    private fun HomeUiState.withRecomputedCameraMetrics(): HomeUiState {
        val totalFromOverview = overviewStats
            .find { it.label == "camera-count" && it.tag == "all" }
            ?.count?.toString()?.toDoubleOrNull()?.toInt() ?: 0
        val totalCams = if (totalFromOverview > 0) totalFromOverview else cameraList.size

        val onlineCams = if (cameraList.isEmpty()) {
            totalCams
        } else {
            cameraList.count { cam ->
                val k2Id = if (cam.cameraId != 0) cam.cameraId else cam.id
                val wsStatus = cameraStatusMap["cam-0-${cam.id}"]
                    ?: cameraStatusMap["cam-${cam.vmsId}-$k2Id"]
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
            val k2Id = if (cam.cameraId != 0) cam.cameraId else cam.id
            val wsRecStatus = recordStatusMap["cam-0-${cam.id}"]
                ?: recordStatusMap["cam-${cam.vmsId}-$k2Id"]
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
        private const val PREF_VIEW_MODE = "mobile_event_view_mode"
        private const val MAX_RECENT_EVENTS = 5
        private const val RECENT_BATCH_SIZE = 50
        private const val MAX_BATCH_ATTEMPTS = 3
        private const val NOTIFY_DEBOUNCE_MS = 600L
        private const val NOTIFY_MIN_INTERVAL_MS = 8_000L
        private const val FOLLOW_UP_STATS_SYNC_DELAY_MS = 2_500L
        private const val POLL_INTERVAL_MS = 30_000L
        private const val STATUS_DEBOUNCE_MS = 800L

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as TenliApp
                HomeViewModel(application, application.container.webSocketManager)
            }
        }
    }
}
