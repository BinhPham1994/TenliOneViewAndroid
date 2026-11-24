package com.tenli.oneview.ui.features.home

import android.app.Application
import android.os.SystemClock
import android.util.Log
import android.util.LruCache
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
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
import com.tenli.oneview.ui.features.event.EventDescendingComparator
import com.tenli.oneview.ui.features.event.eventTimeToSeconds
import com.tenli.oneview.ui.features.event.expandPermittedApiEventTypesFromCounts
import com.tenli.oneview.ui.features.event.isWithinTimeRangeSeconds
import com.tenli.oneview.ui.features.event.permittedDistinct
import com.tenli.oneview.ui.features.event.resolveAllPermittedApiEventTypes
import com.tenli.oneview.ui.features.event.resolveApiEventTypes
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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
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

        fun cacheKeyToken(
            filter: TimeFilter,
            customDateStartMs: Long = TimeFilter.customDateStartMs,
            customDateEndMs: Long = TimeFilter.customDateEndMs,
            customRangeFromMs: Long = TimeFilter.customRangeFromMs,
            customRangeToMs: Long = TimeFilter.customRangeToMs
        ): String {
            return when (filter) {
                CUSTOM_DATE -> "${filter.name}_${customDateStartMs}_${customDateEndMs}"
                CUSTOM_RANGE -> "${filter.name}_${customRangeFromMs}_${customRangeToMs}"
                else -> filter.name
            }
        }

        fun getDateRange(
            filter: TimeFilter,
            customDateStartMs: Long = TimeFilter.customDateStartMs,
            customDateEndMs: Long = TimeFilter.customDateEndMs,
            customRangeFromMs: Long = TimeFilter.customRangeFromMs,
            customRangeToMs: Long = TimeFilter.customRangeToMs
        ): Pair<Long, Long> {
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

        fun resolveTimeRangeMillis(filter: TimeFilter): Pair<Long, Long> =
            getDateRange(filter)

        fun formatDisplayLabel(
            filter: TimeFilter,
            isEn: Boolean,
            customDateStartMs: Long = TimeFilter.customDateStartMs,
            customRangeFromMs: Long = TimeFilter.customRangeFromMs,
            customRangeToMs: Long = TimeFilter.customRangeToMs
        ): String {
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
    val customDateStartMs: Long = TimeFilter.customDateStartMs,
    val customDateEndMs: Long = TimeFilter.customDateEndMs,
    val customRangeFromMs: Long = TimeFilter.customRangeFromMs,
    val customRangeToMs: Long = TimeFilter.customRangeToMs,
    val overviewStats: List<VmsCountOverviewModel> = emptyList(),
    val eventsByType: List<VmsEventCountByTypeModel> = emptyList(),
    val hasLoadedEventsByType: Boolean = false,
    val aiDistribution: List<HomeAiDistributionItem> = emptyList(),
    val aiModuleEvents: Map<String, List<EventData>> = emptyMap(),
    val loadingAiModuleTypes: Set<String> = emptySet(),
    val cameraList: List<CameraModel> = emptyList(),
    val offlineCameraIds: Set<Int> = emptySet(),
    val favoriteCameraIds: Set<Int> = emptySet(),
    val cameraSnapshots: Map<Int, String> = emptyMap(),
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

@Stable
@OptIn(FlowPreview::class)
class HomeViewModel(
    application: Application,
    private val dashboardRepository: DashboardRepository,
    private val webSocketManager: WebSocketManager
) : AndroidViewModel(application) {

    @Volatile private var latestCameraStatusMap: Map<String, String> = webSocketManager.cameraStatusMap.value
    @Volatile private var latestRecordStatusMap: Map<String, String> = webSocketManager.recordStatusMap.value
    private val moduleEventsCacheLock = Any()
    private val moduleEventsRamCache = LruCache<String, Map<String, List<EventData>>>(MAX_MODULE_EVENTS_CACHE_ENTRIES)

    private val _uiState = MutableStateFlow(buildInitialState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var dashboardJob: Job? = null
    private var statsReconcileJob: Job? = null
    private var moduleEventsJob: Job? = null
    private var favoriteSnapshotsJob: Job? = null
    private val realtimeTriggerChannel = Channel<Boolean>(Channel.CONFLATED)
    @Volatile private var isHomeTabActive: Boolean = true
    @Volatile private var pendingNotifyRefresh: Boolean = false
    @Volatile private var pendingNetworkRefresh: Boolean = false
    @Volatile private var pendingCameraStatusRecompute: Boolean = false
    @Volatile private var lastByTypeOverviewSyncMs: Long = 0L
    @Volatile private var lastCountedRealtimeEventId: Int = 0

    private fun getModuleEventsCache(key: String): Map<String, List<EventData>>? =
        synchronized(moduleEventsCacheLock) { moduleEventsRamCache.get(key) }

    private fun putModuleEventsCache(key: String, value: Map<String, List<EventData>>) {
        synchronized(moduleEventsCacheLock) { moduleEventsRamCache.put(key, value) }
    }

    private fun removeModuleEventsCache(key: String) {
        synchronized(moduleEventsCacheLock) { moduleEventsRamCache.remove(key) }
    }

    private fun pruneEventFromModuleEventsCache(eventId: Int) {
        synchronized(moduleEventsCacheLock) {
            val snapshot = moduleEventsRamCache.snapshot()
            for ((key, map) in snapshot) {
                if (map.values.any { list -> list.any { it.id == eventId } }) {
                    val pruned = map.mapValues { (_, list) ->
                        if (list.any { it.id == eventId }) list.filter { it.id != eventId } else list
                    }
                    moduleEventsRamCache.put(key, pruned)
                }
            }
        }
    }

    private fun HomeUiState.matchesFilter(
        filter: TimeFilter,
        serviceId: Int?,
        customDateStartMs: Long,
        customDateEndMs: Long,
        customRangeFromMs: Long,
        customRangeToMs: Long
    ): Boolean {
        if (selectedFilter != filter || selectedServiceId != serviceId) return false
        return when (filter) {
            TimeFilter.CUSTOM_DATE -> this.customDateStartMs == customDateStartMs && this.customDateEndMs == customDateEndMs
            TimeFilter.CUSTOM_RANGE -> this.customRangeFromMs == customRangeFromMs && this.customRangeToMs == customRangeToMs
            else -> true
        }
    }

    private fun matchesGlobalCustomFilter(
        filter: TimeFilter,
        customDateStartMs: Long,
        customDateEndMs: Long,
        customRangeFromMs: Long,
        customRangeToMs: Long
    ): Boolean = when (filter) {
        TimeFilter.CUSTOM_DATE ->
            TimeFilter.customDateStartMs == customDateStartMs && TimeFilter.customDateEndMs == customDateEndMs
        TimeFilter.CUSTOM_RANGE ->
            TimeFilter.customRangeFromMs == customRangeFromMs && TimeFilter.customRangeToMs == customRangeToMs
        else -> true
    }

    init {
        fetchDashboardData()
        listenToWebSocket()
        webSocketManager.connectAllReports()
        refreshFavoriteCameraSnapshots(_uiState.value.favoriteCameraIds)
    }

    fun setHomeTabActive(active: Boolean) {
        if (isHomeTabActive == active) return
        isHomeTabActive = active
        webSocketManager.setReportsActive(active, "home")
        if (!active) {
            statsReconcileJob?.cancel()
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
        if (pendingNetworkRefresh) {
            pendingNetworkRefresh = false
            pendingNotifyRefresh = false
            fetchDashboardData()
        } else if (pendingNotifyRefresh && _uiState.value.selectedFilter == TimeFilter.TODAY) {
            pendingNotifyRefresh = false
            realtimeTriggerChannel.trySend(false)
        }
        val favIds = dashboardRepository.getFavoriteCameraIds()
        if (favIds != _uiState.value.favoriteCameraIds) {
            _uiState.update { it.copy(favoriteCameraIds = favIds) }
        }
        if (favIds.isNotEmpty()) {
            refreshFavoriteCameraSnapshots(favIds)
        }
    }

    private fun resolveFullCameraSnapshotUrl(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty() || trimmed == "NO_IMAGE") return ""
        if (trimmed.startsWith("http://") ||
            trimmed.startsWith("https://") ||
            trimmed.startsWith("file://") ||
            trimmed.startsWith("data:")
        ) {
            return trimmed
        }
        val base = UserSession.domain.trim().trimEnd('/').removeSuffix("/api/v1")
        return if (trimmed.startsWith("/")) "$base$trimmed" else "$base/$trimmed"
    }

    private fun collectInMemoryFavoriteSnapshots(favIds: Set<Int>): Map<Int, String> {
        if (favIds.isEmpty()) return emptyMap()
        val map = LinkedHashMap<Int, String>(favIds.size)
        for (camId in favIds) {
            val raw = dashboardRepository.getCachedCameraImageLinkInMemory(camId) ?: continue
            val resolved = resolveFullCameraSnapshotUrl(raw)
            if (resolved.isNotEmpty()) {
                map[camId] = resolved
            }
        }
        return map
    }

    private fun refreshFavoriteCameraSnapshots(favIds: Set<Int>) {
        if (favIds.isEmpty() || !UserPermissionManager.canAccessLiveStream()) return
        favoriteSnapshotsJob?.cancel()
        favoriteSnapshotsJob = viewModelScope.launch(Dispatchers.IO) {
            val permittedCameraIds = _uiState.value.cameraList
                .asSequence()
                .filter { UserPermissionManager.isCameraPermitted(it) }
                .map { it.id }
                .toHashSet()
            val permittedFavIds = favIds.filter { it in permittedCameraIds }
            if (permittedFavIds.isEmpty()) return@launch

            val cachedFound = LinkedHashMap<Int, String>()
            val missingIds = ArrayList<Int>()
            for (camId in permittedFavIds) {
                val cached = dashboardRepository.getCachedCameraImageLink(camId)
                val full = cached?.let(::resolveFullCameraSnapshotUrl).orEmpty()
                if (full.isNotEmpty()) {
                    cachedFound[camId] = full
                } else {
                    missingIds.add(camId)
                }
            }
            if (cachedFound.isNotEmpty()) {
                _uiState.update { cur ->
                    val merged = cur.cameraSnapshots + cachedFound
                    if (merged == cur.cameraSnapshots) cur else cur.copy(cameraSnapshots = merged)
                }
            }
            if (missingIds.isEmpty() || !NetworkMonitor.isNetworkAvailable()) return@launch

            val newlyFetched = LinkedHashMap<Int, String>()
            for (camId in missingIds.take(6)) {
                val resp = safeApiCall { dashboardRepository.getCameraImageLink(camId) }.getOrNull()
                val link = resp?.body()?.link?.trim().orEmpty()
                if (link.isNotEmpty() && link != "NO_IMAGE") {
                    dashboardRepository.setCachedCameraImageLink(camId, link)
                    val full = resolveFullCameraSnapshotUrl(link)
                    if (full.isNotEmpty()) {
                        newlyFetched[camId] = full
                    }
                }
            }
            if (newlyFetched.isNotEmpty()) {
                _uiState.update { cur ->
                    cur.copy(cameraSnapshots = cur.cameraSnapshots + newlyFetched)
                }
            }
        }
    }

    private fun filterCachedModuleEvents(
        rawMap: Map<String, List<EventData>>?,
        fromSec: Long,
        toSec: Long,
        allCameras: List<CameraModel>
    ): Map<String, List<EventData>> {
        if (rawMap.isNullOrEmpty()) return emptyMap()
        val result = LinkedHashMap<String, List<EventData>>(rawMap.size)
        for ((key, list) in rawMap) {
            if (key.isBlank() || list.isNullOrEmpty()) continue
            val filtered = list
                .filter { it.isWithinTimeRangeSeconds(fromSec, toSec) }
                .permittedDistinct(allCameras)
                .take(MAX_RECENT_EVENTS)
            if (filtered.isNotEmpty()) {
                result[key] = filtered
            }
        }
        return result
    }

    private fun buildInitialState(): HomeUiState {
        val isList = dashboardRepository.isListViewMode()
        val savedFavorites = dashboardRepository.getFavoriteCameraIds()
        val initialFavSnapshots = collectInMemoryFavoriteSnapshots(savedFavorites)
        val memCached = dashboardRepository.getHomeDataInMemory(TimeFilter.TODAY, null)
        return if (memCached != null) {
            val (fromMs, toMs) = TimeFilter.resolveTimeRangeMillis(TimeFilter.TODAY)
            val fromSec = fromMs / 1000
            val toSec = toMs / 1000
            val rawCameras = memCached.cameraList.ifEmpty { dashboardRepository.getCamerasInMemory() }
            val permittedCameras = UserPermissionManager.filterPermittedCameras(rawCameras)
            val services = memCached.aiServices.ifEmpty { dashboardRepository.getAiServicesInMemory() }
            val initialShelves = filterCachedModuleEvents(
                rawMap = memCached.aiModuleEvents,
                fromSec = fromSec,
                toSec = toSec,
                allCameras = rawCameras
            )
            if (initialShelves.isNotEmpty()) {
                putModuleEventsCache(moduleEventsCacheKey(TimeFilter.TODAY, null), initialShelves)
            }
            HomeUiState(
                isLoading = false,
                isListView = isList,
                overviewStats = memCached.overviewStats,
                eventsByType = memCached.eventsByType,
                hasLoadedEventsByType = memCached.hasLoadedEventsByType || memCached.eventsByType.isNotEmpty(),
                aiModuleEvents = initialShelves,
                cameraList = permittedCameras,
                favoriteCameraIds = savedFavorites,
                cameraSnapshots = initialFavSnapshots,
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
                favoriteCameraIds = savedFavorites,
                cameraSnapshots = initialFavSnapshots,
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
        // Worker đơn luồng dùng Channel.CONFLATED:
        // - Sự kiện đầu tiên xử lý ngay tức thì (0ms artificial debounce delay).
        // - Khi sự kiện bắn dồn dập liên tục, không bao giờ tự cancel job đang chạy dở và không bị nghẽn vô hạn như debounce().
        viewModelScope.launch(Dispatchers.IO) {
            for (mayLagDbCommit in realtimeTriggerChannel) {
                if (!isHomeTabActive || _uiState.value.selectedFilter != TimeFilter.TODAY) continue
                processRealtimeRecentEvents(mayLagDbCommit = mayLagDbCommit)
                delay(REALTIME_MIN_INTERVAL_MS)
            }
        }
        viewModelScope.launch {
            webSocketManager.notifyEvent.collect { notify ->
                if (_uiState.value.selectedFilter == TimeFilter.TODAY) {
                    if (isHomeTabActive) {
                        val mayLag = notify.event != WebSocketManager.TOPIC_APP_RESUMED
                        realtimeTriggerChannel.trySend(mayLag)
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
                pruneEventFromModuleEventsCache(eventId)
                _uiState.update { state ->
                    val prunedModuleEvents = if (state.aiModuleEvents.isEmpty()) {
                        state.aiModuleEvents
                    } else {
                        state.aiModuleEvents.mapValues { (_, list) ->
                            if (list.any { it.id == eventId }) list.filter { it.id != eventId } else list
                        }
                    }
                    state.copy(
                        recentEvents = state.recentEvents.filter { it.id != eventId },
                        aiModuleEvents = prunedModuleEvents
                    )
                }
            }
        }
        viewModelScope.launch {
            EventBus.networkRestored.collect {
                if (isHomeTabActive) {
                    fetchDashboardData()
                } else {
                    pendingNetworkRefresh = true
                }
            }
        }
        viewModelScope.launch {
            dashboardRepository.favoriteCameraIdsFlow.collect { favIds ->
                if (favIds != _uiState.value.favoriteCameraIds) {
                    _uiState.update { state ->
                        val memSnaps = collectInMemoryFavoriteSnapshots(favIds)
                        state.copy(
                            favoriteCameraIds = favIds,
                            cameraSnapshots = if (memSnaps.isEmpty()) state.cameraSnapshots else state.cameraSnapshots + memSnaps
                        )
                    }
                    refreshFavoriteCameraSnapshots(favIds)
                }
            }
        }
        viewModelScope.launch {
            dashboardRepository.snapshotUpdatesFlow.collect { (camId, rawLink) ->
                if (camId in _uiState.value.favoriteCameraIds) {
                    val resolved = resolveFullCameraSnapshotUrl(rawLink)
                    if (resolved.isNotEmpty() && _uiState.value.cameraSnapshots[camId] != resolved) {
                        _uiState.update { state ->
                            state.copy(cameraSnapshots = state.cameraSnapshots + (camId to resolved))
                        }
                    }
                }
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
        val reachedEnd: Boolean = true,
        val firstError: Throwable? = null
    )

    /**
     * Truy vấn bổ sung theo từng mã `type` được phân quyền khi `Data/api/Data` không truyền `type`
     * bị nghẽn bởi hàng trăm bản ghi thô từ các sensor bị tắt / không thuộc phân quyền (vd. Heatmap, Counting...).
     */
    private suspend fun fetchPermittedRecentEventsByTypes(
        fromSec: Long,
        toSec: Long,
        serviceId: Int?,
        candidateTypes: List<String>,
        allCameras: List<CameraModel> = dashboardRepository.getCamerasInMemory()
    ): RecentEventsFetchResult = coroutineScope {
        if (!UserPermissionManager.canAccessEvents() || candidateTypes.isEmpty()) {
            return@coroutineScope RecentEventsFetchResult(events = emptyList(), anySuccessful = true, reachedEnd = true)
        }
        val semaphore = Semaphore(MAX_OVERVIEW_BY_TYPE_CONCURRENCY)
        val perTypeBatchSize = if (UserPermissionManager.isCustomCameraPermission()) {
            MEDIUM_RECENT_BATCH_SIZE
        } else {
            FAST_RECENT_BATCH_SIZE
        }
        val camerasForPerm = allCameras.ifEmpty { dashboardRepository.getCamerasInMemory() }

        val results = candidateTypes.map { type ->
            async {
                semaphore.withPermit {
                    safeApiCall {
                        dashboardRepository.getEventDataList(
                            count = perTypeBatchSize,
                            lastId = null,
                            from = fromSec,
                            to = toSec,
                            serviceId = serviceId,
                            type = type
                        )
                    }
                }
            }
        }.awaitAll()

        var anySuccessful = false
        var firstError: Throwable? = null
        val inRangeAll = ArrayList<EventData>()

        for (res in results) {
            val resp = res.getOrNull()
            if (resp != null) {
                anySuccessful = true
                val body = resp.body().orEmpty()
                for (ev in body) {
                    if (ev.isWithinTimeRangeSeconds(fromSec, toSec)) {
                        inRangeAll.add(ev)
                    }
                }
            } else if (firstError == null) {
                firstError = res.exceptionOrNull()
            }
        }

        val permittedBatch = UserPermissionManager.filterEventsByPermissions(inRangeAll, camerasForPerm)
        val permitted = ArrayList<EventData>(permittedBatch.size)
        val seenIds = HashSet<Int>(permittedBatch.size * 2)
        val seenKeys = HashSet<String>(permittedBatch.size * 2)
        for (ev in permittedBatch) {
            if (seenIds.add(ev.id) && seenKeys.add(ev.uniqueKey())) {
                permitted.add(ev)
            }
        }

        RecentEventsFetchResult(
            events = permitted.sortedWith(EventDescendingComparator).take(MAX_RECENT_EVENTS),
            anySuccessful = anySuccessful,
            reachedEnd = true,
            firstError = if (anySuccessful) null else firstError
        )
    }

    /**
     * Khớp hoàn toàn với MobileHomePage.vue:
     * Fetch theo batch 50 sự kiện (tối đa 3 lần), lọc các loại AI và camera được phân quyền,
     * loại trùng ID và chỉ giữ đúng 5 sự kiện mới nhất (permittedEvents.slice(0, 5)).
     */
    private suspend fun fetchPermittedRecentEvents(
        fromSec: Long,
        toSec: Long,
        serviceId: Int?,
        allCameras: List<CameraModel> = dashboardRepository.getCamerasInMemory(),
        fastFirstAttempt: Boolean = true
    ): RecentEventsFetchResult {
        if (!UserPermissionManager.canAccessEvents()) {
            return RecentEventsFetchResult(events = emptyList(), anySuccessful = true, reachedEnd = true)
        }
        val permitted = mutableListOf<EventData>()
        val seenIds = HashSet<Int>()
        val seenKeys = HashSet<String>()
        var lastId: Int? = null
        var attempts = 0
        var anySuccessful = false
        var reachedEnd = false
        var firstError: Throwable? = null
        val isUnrestricted = !UserPermissionManager.isCustomCameraPermission() &&
            !UserPermissionManager.isCustomAiEventPermission() &&
            !AiTypeHelper.isProjectModuleRestricted

        while (permitted.size < MAX_RECENT_EVENTS && attempts < MAX_BATCH_ATTEMPTS) {
            attempts++
            val batchSize = when {
                attempts == 1 && fastFirstAttempt && isUnrestricted -> FAST_RECENT_BATCH_SIZE
                attempts == 1 && fastFirstAttempt -> MEDIUM_RECENT_BATCH_SIZE
                else -> RECENT_BATCH_SIZE
            }
            val result = safeApiCall {
                dashboardRepository.getEventDataList(
                    count = batchSize,
                    lastId = lastId,
                    from = fromSec,
                    to = toSec,
                    serviceId = serviceId
                )
            }
            val resp = result.getOrNull()
            if (resp == null) {
                if (firstError == null) firstError = result.exceptionOrNull()
                reachedEnd = true
                break
            }
            anySuccessful = true
            val data = resp.body() ?: emptyList()
            if (data.isEmpty()) {
                reachedEnd = true
                break
            }

            val camerasForPerm = allCameras.ifEmpty { dashboardRepository.getCamerasInMemory() }
            val inRange = data.filter { it.isWithinTimeRangeSeconds(fromSec, toSec) }
            val permittedBatch = UserPermissionManager.filterEventsByPermissions(inRange, camerasForPerm)
            for (ev in permittedBatch) {
                if (seenIds.add(ev.id) && seenKeys.add(ev.uniqueKey())) {
                    permitted.add(ev)
                    if (permitted.size >= MAX_RECENT_EVENTS) break
                }
            }

            // Danh sách trả về theo thứ tự mới -> cũ (order=desc); nếu toàn bộ batch đã cũ hơn fromSec thì dừng phân trang
            val allOlderThanFrom = data.all { it.time > 0.0 && eventTimeToSeconds(it.time) < fromSec }
            if (allOlderThanFrom || data.size < batchSize) {
                reachedEnd = true
                break
            }
            val nextLastId = data.lastOrNull()?.id
            if (nextLastId == null || nextLastId <= 0 || nextLastId == lastId) {
                reachedEnd = true
                break
            }
            lastId = nextLastId
        }

        return RecentEventsFetchResult(
            events = permitted.sortedWith(EventDescendingComparator).take(MAX_RECENT_EVENTS),
            anySuccessful = anySuccessful,
            reachedEnd = reachedEnd,
            firstError = if (anySuccessful) null else firstError
        )
    }

    /**
     * Luồng cập nhật nhanh "Sự kiện gần đây" (~60-90ms):
     * Tách riêng khỏi các API thống kê `count-overview` để danh sách sự kiện trên Trang chủ hiển thị tức thì như Tab Sự kiện,
     * đồng thời có nhịp kiểm tra bù sau 650ms nếu bản tin `/Data/ws/Notify` tới sớm hơn thời điểm DB commit `isConfirmed=1`.
     */
    private suspend fun processRealtimeRecentEvents(mayLagDbCommit: Boolean) {
        try {
            val startState = _uiState.value
            val filter = startState.selectedFilter
            if (filter != TimeFilter.TODAY) return
            val serviceId = startState.selectedServiceId
            val customDateStartMs = startState.customDateStartMs
            val customDateEndMs = startState.customDateEndMs
            val customRangeFromMs = startState.customRangeFromMs
            val customRangeToMs = startState.customRangeToMs
            val (fromTime, toTime) = TimeFilter.getDateRange(
                filter = filter,
                customDateStartMs = customDateStartMs,
                customDateEndMs = customDateEndMs,
                customRangeFromMs = customRangeFromMs,
                customRangeToMs = customRangeToMs
            )
            val fromSec = fromTime / 1000
            val toSec = toTime / 1000
            val shelfCacheKey = moduleEventsCacheKey(
                filter = filter,
                serviceId = serviceId,
                customDateStartMs = customDateStartMs,
                customDateEndMs = customDateEndMs,
                customRangeFromMs = customRangeFromMs,
                customRangeToMs = customRangeToMs
            )

            val prevMaxId = maxOf(
                lastCountedRealtimeEventId,
                startState.recentEvents.maxOfOrNull { it.id } ?: 0
            )

            var recentResult = fetchPermittedRecentEvents(
                fromSec = fromSec,
                toSec = toSec,
                serviceId = serviceId
            )
            if (!recentResult.reachedEnd && recentResult.events.size < MAX_RECENT_EVENTS) {
                val targetTypes = expandPermittedApiEventTypesFromCounts(_uiState.value.eventsByType)
                    .ifEmpty { resolveAllPermittedApiEventTypes() }
                if (targetTypes.isNotEmpty()) {
                    val typedFallback = fetchPermittedRecentEventsByTypes(
                        fromSec = fromSec,
                        toSec = toSec,
                        serviceId = serviceId,
                        candidateTypes = targetTypes
                    )
                    if (typedFallback.anySuccessful) {
                        val merged = (recentResult.events + typedFallback.events)
                            .distinctBy { it.id }
                            .sortedWith(EventDescendingComparator)
                            .take(MAX_RECENT_EVENTS)
                        recentResult = RecentEventsFetchResult(
                            events = merged,
                            anySuccessful = true,
                            reachedEnd = true
                        )
                    }
                }
            }

            // Nếu gói WebSocket Notify đến trước lúc bản ghi được đánh dấu isConfirmed=1 khoảng vài trăm ms,
            // thử lại đúng 1 lần nhẹ sau 650ms để bắt chắc chắn sự kiện vừa phát sinh.
            val firstMaxId = recentResult.events.maxOfOrNull { it.id } ?: 0
            if (mayLagDbCommit && firstMaxId <= prevMaxId) {
                delay(REALTIME_DB_COMMIT_RETRY_MS)
                if (!isHomeTabActive ||
                    _uiState.value.selectedFilter != filter ||
                    _uiState.value.selectedServiceId != serviceId
                ) {
                    return
                }
                var retryResult = fetchPermittedRecentEvents(
                    fromSec = fromSec,
                    toSec = toSec,
                    serviceId = serviceId
                )
                if (!retryResult.reachedEnd && retryResult.events.size < MAX_RECENT_EVENTS) {
                    val targetTypes = expandPermittedApiEventTypesFromCounts(_uiState.value.eventsByType)
                        .ifEmpty { resolveAllPermittedApiEventTypes() }
                    if (targetTypes.isNotEmpty()) {
                        val typedRetry = fetchPermittedRecentEventsByTypes(
                            fromSec = fromSec,
                            toSec = toSec,
                            serviceId = serviceId,
                            candidateTypes = targetTypes
                        )
                        if (typedRetry.anySuccessful) {
                            retryResult = RecentEventsFetchResult(
                                events = (retryResult.events + typedRetry.events)
                                    .distinctBy { it.id }
                                    .sortedWith(EventDescendingComparator)
                                    .take(MAX_RECENT_EVENTS),
                                anySuccessful = true,
                                reachedEnd = true
                            )
                        }
                    }
                }
                if (retryResult.anySuccessful) {
                    recentResult = retryResult
                }
            }

            val newEvents = recentResult.events
            if (!_uiState.value.matchesFilter(filter, serviceId, customDateStartMs, customDateEndMs, customRangeFromMs, customRangeToMs)) return

            if (newEvents.isNotEmpty()) {
                dashboardRepository.cacheInMemory(events = newEvents)
            }

            // Precompute derived lists/metrics off-main, then apply atomically inside _uiState.update
            val snap = _uiState.value
            val allCameras = dashboardRepository.getCamerasInMemory().ifEmpty { snap.cameraList }
            val precomputed = withContext(Dispatchers.Default) {
                val permittedFresh = newEvents
                    .filter { it.isWithinTimeRangeSeconds(fromSec, toSec) }
                    .permittedDistinct(allCameras)
                val inRangeExisting = snap.recentEvents.filter { it.isWithinTimeRangeSeconds(fromSec, toSec) }
                val existingByKey = HashMap<String, EventData>(snap.recentEvents.size * 2)
                for (ev in snap.recentEvents) {
                    existingByKey[ev.uniqueKey()] = ev
                }
                val mergedRaw = (permittedFresh + inRangeExisting)
                    .permittedDistinct(allCameras)
                    .distinctBy { it.id }
                    .sortedWith(EventDescendingComparator)
                    .take(MAX_RECENT_EVENTS)
                    .map { ev ->
                        val prev = existingByKey[ev.uniqueKey()]
                        if (prev != null && prev == ev) prev else ev
                    }
                val mergedEvents = if (mergedRaw == snap.recentEvents) snap.recentEvents else mergedRaw

                val (updatedByType, nextCountedId) = if (snap.eventsByType.isNotEmpty()) {
                    mergeRealtimeDeltasIntoByType(
                        currentByType = snap.eventsByType,
                        newEvents = permittedFresh,
                        baselineMaxId = lastCountedRealtimeEventId
                    )
                } else {
                    snap.eventsByType to maxOf(lastCountedRealtimeEventId, permittedFresh.maxOfOrNull { it.id } ?: 0)
                }

                val updatedModuleEvents = mergeRealtimeEventsIntoModuleShelves(
                    currentMap = snap.aiModuleEvents,
                    newEvents = permittedFresh,
                    fromSec = fromSec,
                    toSec = toSec,
                    allCameras = allCameras
                )
                val previewState = snap.copy(
                    eventsByType = if (updatedByType == snap.eventsByType) snap.eventsByType else updatedByType,
                    recentEvents = mergedEvents,
                    aiModuleEvents = if (updatedModuleEvents == snap.aiModuleEvents) snap.aiModuleEvents else updatedModuleEvents
                ).withRecomputedMetrics()

                Triple(
                    Triple(permittedFresh, mergedEvents, updatedByType),
                    Triple(updatedModuleEvents, nextCountedId, previewState.totalDetectionsCount),
                    previewState.aiDistribution
                )
            }

            val (permittedFresh, preMergedEvents, preUpdatedByType) = precomputed.first
            val (preUpdatedModuleEvents, preNextCountedId, preTotalDetections) = precomputed.second
            val preAiDistribution = precomputed.third

            var committedState: HomeUiState? = null
            var didChange = false
            var committedCountedId = preNextCountedId
            _uiState.update { cur ->
                if (!cur.matchesFilter(filter, serviceId, customDateStartMs, customDateEndMs, customRangeFromMs, customRangeToMs)) {
                    return@update cur
                }
                val next = if (
                    cur.recentEvents === snap.recentEvents &&
                    cur.eventsByType === snap.eventsByType &&
                    cur.aiModuleEvents === snap.aiModuleEvents &&
                    cur.overviewStats === snap.overviewStats
                ) {
                    committedCountedId = preNextCountedId
                    cur.copy(
                        eventsByType = if (preUpdatedByType == cur.eventsByType) cur.eventsByType else preUpdatedByType,
                        recentEvents = preMergedEvents,
                        aiModuleEvents = if (preUpdatedModuleEvents == cur.aiModuleEvents) cur.aiModuleEvents else preUpdatedModuleEvents,
                        totalDetectionsCount = preTotalDetections,
                        aiDistribution = if (preAiDistribution == cur.aiDistribution) cur.aiDistribution else preAiDistribution
                    )
                } else {
                    val curExistingByKey = HashMap<String, EventData>(cur.recentEvents.size * 2)
                    for (ev in cur.recentEvents) {
                        curExistingByKey[ev.uniqueKey()] = ev
                    }
                    val curInRange = cur.recentEvents.filter { it.isWithinTimeRangeSeconds(fromSec, toSec) }
                    val curMergedRaw = (permittedFresh + curInRange)
                        .distinctBy { it.uniqueKey() }
                        .distinctBy { it.id }
                        .sortedWith(EventDescendingComparator)
                        .take(MAX_RECENT_EVENTS)
                        .map { ev ->
                            val prev = curExistingByKey[ev.uniqueKey()]
                            if (prev != null && prev == ev) prev else ev
                        }
                    val curMergedEvents = if (curMergedRaw == cur.recentEvents) cur.recentEvents else curMergedRaw
                    val (curByType, curNextId) = if (cur.eventsByType.isNotEmpty()) {
                        mergeRealtimeDeltasIntoByType(
                            currentByType = cur.eventsByType,
                            newEvents = permittedFresh,
                            baselineMaxId = lastCountedRealtimeEventId
                        )
                    } else {
                        cur.eventsByType to maxOf(lastCountedRealtimeEventId, permittedFresh.maxOfOrNull { it.id } ?: 0)
                    }
                    committedCountedId = curNextId
                    val curModuleEvents = mergeRealtimeEventsIntoModuleShelves(
                        currentMap = cur.aiModuleEvents,
                        newEvents = permittedFresh,
                        fromSec = fromSec,
                        toSec = toSec,
                        allCameras = allCameras
                    )
                    cur.copy(
                        eventsByType = if (curByType == cur.eventsByType) cur.eventsByType else curByType,
                        recentEvents = curMergedEvents,
                        aiModuleEvents = if (curModuleEvents == cur.aiModuleEvents) cur.aiModuleEvents else curModuleEvents
                    ).withRecomputedMetrics()
                }
                if (next == cur) {
                    cur
                } else {
                    didChange = true
                    committedState = next
                    next
                }
            }

            lastCountedRealtimeEventId = maxOf(lastCountedRealtimeEventId, committedCountedId)
            val savedState = committedState
            if (didChange && savedState != null) {
                if (savedState.aiModuleEvents.isNotEmpty()) {
                    putModuleEventsCache(shelfCacheKey, savedState.aiModuleEvents)
                }
                dashboardRepository.saveHomeData(
                    filter = filter,
                    serviceId = serviceId,
                    data = CachedHomeData(
                        overviewStats = savedState.overviewStats,
                        eventsByType = savedState.eventsByType,
                        hasLoadedEventsByType = savedState.hasLoadedEventsByType,
                        cameraList = dashboardRepository.getCamerasInMemory().ifEmpty { savedState.cameraList },
                        aiServices = savedState.aiServices,
                        recentEvents = savedState.recentEvents,
                        aiModuleEvents = savedState.aiModuleEvents,
                        runningAiTasks = savedState.runningAiTasks,
                        totalAiTasks = savedState.totalAiTasks,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }

            // Đồng bộ lại số liệu thống kê tổng / phân bố AI hoàn toàn bất đồng bộ (tối đa 1 lần / 10s), không chặn hiển thị sự kiện mới
            val nowElapsed = SystemClock.elapsedRealtime()
            val curAfter = _uiState.value
            if (statsReconcileJob?.isActive != true &&
                (curAfter.eventsByType.isEmpty() || nowElapsed - lastByTypeOverviewSyncMs >= BY_TYPE_WS_REFRESH_INTERVAL_MS)
            ) {
                reconcileOverviewStatsInBackground(
                    filter = filter,
                    serviceId = serviceId,
                    customDateStartMs = customDateStartMs,
                    customDateEndMs = customDateEndMs,
                    customRangeFromMs = customRangeFromMs,
                    customRangeToMs = customRangeToMs,
                    seedEvents = newEvents
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Error processing realtime recent events", e)
        }
    }

    private fun reconcileOverviewStatsInBackground(
        filter: TimeFilter,
        serviceId: Int?,
        customDateStartMs: Long,
        customDateEndMs: Long,
        customRangeFromMs: Long,
        customRangeToMs: Long,
        seedEvents: List<EventData>
    ) {
        statsReconcileJob?.cancel()
        statsReconcileJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!UserPermissionManager.canAccessEvents() || !NetworkMonitor.isNetworkAvailable()) return@launch
                val serviceIdStr = serviceId?.toString()
                val (fromTime, toTime) = TimeFilter.getDateRange(
                    filter = filter,
                    customDateStartMs = customDateStartMs,
                    customDateEndMs = customDateEndMs,
                    customRangeFromMs = customRangeFromMs,
                    customRangeToMs = customRangeToMs
                )
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
                    val overviewResp = overviewDeferred.await()
                    val byTypeResp = byTypeDeferred.await()

                    if (!_uiState.value.matchesFilter(filter, serviceId, customDateStartMs, customDateEndMs, customRangeFromMs, customRangeToMs)) {
                        return@coroutineScope
                    }

                    val latestOverviewStats = if (overviewResp?.isSuccessful == true) {
                        overviewResp.body() ?: _uiState.value.overviewStats
                    } else {
                        _uiState.value.overviewStats
                    }
                    val byTypeSucceeded = byTypeResp?.isSuccessful == true
                    val byTypeBody = byTypeResp?.body().orEmpty()
                    val baselineIdBeforeSync = maxOf(
                        lastCountedRealtimeEventId,
                        _uiState.value.recentEvents.maxOfOrNull { it.id } ?: 0
                    )

                    val (resolvedByType, resolvedHasLoadedByType) = if (byTypeSucceeded && byTypeBody.isNotEmpty()) {
                        lastByTypeOverviewSyncMs = SystemClock.elapsedRealtime()
                        lastCountedRealtimeEventId = maxOf(lastCountedRealtimeEventId, baselineIdBeforeSync)
                        byTypeBody to true
                    } else {
                        val fallback = fetchEventCountsViaOverview(
                            fromTime = fromTime,
                            toTime = toTime,
                            serviceIdStr = serviceIdStr,
                            unfilteredOverviewStats = latestOverviewStats,
                            seedEvents = seedEvents
                        )
                        if (fallback != null) {
                            lastByTypeOverviewSyncMs = SystemClock.elapsedRealtime()
                            lastCountedRealtimeEventId = maxOf(lastCountedRealtimeEventId, baselineIdBeforeSync)
                            fallback to true
                        } else {
                            val cur = _uiState.value
                            cur.eventsByType to (cur.hasLoadedEventsByType || byTypeSucceeded)
                        }
                    }

                    if (!_uiState.value.matchesFilter(filter, serviceId, customDateStartMs, customDateEndMs, customRangeFromMs, customRangeToMs)) {
                        return@coroutineScope
                    }

                    val snap = _uiState.value
                    val precomputedMetrics = withContext(Dispatchers.Default) {
                        snap.copy(
                            overviewStats = if (latestOverviewStats == snap.overviewStats) snap.overviewStats else latestOverviewStats,
                            eventsByType = if (resolvedByType == snap.eventsByType) snap.eventsByType else resolvedByType,
                            hasLoadedEventsByType = resolvedHasLoadedByType
                        ).withRecomputedMetrics()
                    }
                    var committedState: HomeUiState? = null
                    var didChange = false
                    _uiState.update { cur ->
                        if (!cur.matchesFilter(filter, serviceId, customDateStartMs, customDateEndMs, customRangeFromMs, customRangeToMs)) {
                            return@update cur
                        }
                        val next = if (
                            cur.recentEvents === snap.recentEvents &&
                            cur.cameraList === snap.cameraList &&
                            cur.offlineCameraIds === snap.offlineCameraIds &&
                            cur.onlineCamerasCount == snap.onlineCamerasCount &&
                            cur.recordingCamerasCount == snap.recordingCamerasCount
                        ) {
                            cur.copy(
                                overviewStats = if (latestOverviewStats == cur.overviewStats) cur.overviewStats else latestOverviewStats,
                                eventsByType = if (resolvedByType == cur.eventsByType) cur.eventsByType else resolvedByType,
                                hasLoadedEventsByType = resolvedHasLoadedByType,
                                totalDetectionsCount = precomputedMetrics.totalDetectionsCount,
                                aiDistribution = if (precomputedMetrics.aiDistribution == cur.aiDistribution) cur.aiDistribution else precomputedMetrics.aiDistribution,
                                totalCamerasCount = precomputedMetrics.totalCamerasCount,
                                onlineCamerasCount = precomputedMetrics.onlineCamerasCount,
                                recordingCamerasCount = precomputedMetrics.recordingCamerasCount,
                                offlineCameraIds = if (precomputedMetrics.offlineCameraIds == cur.offlineCameraIds) cur.offlineCameraIds else precomputedMetrics.offlineCameraIds
                            )
                        } else {
                            cur.copy(
                                overviewStats = if (latestOverviewStats == cur.overviewStats) cur.overviewStats else latestOverviewStats,
                                eventsByType = if (resolvedByType == cur.eventsByType) cur.eventsByType else resolvedByType,
                                hasLoadedEventsByType = resolvedHasLoadedByType
                            ).withRecomputedMetrics()
                        }
                        if (next == cur) {
                            cur
                        } else {
                            didChange = true
                            committedState = next
                            next
                        }
                    }
                    val savedState = committedState
                    if (didChange && savedState != null &&
                        matchesGlobalCustomFilter(filter, customDateStartMs, customDateEndMs, customRangeFromMs, customRangeToMs)
                    ) {
                        dashboardRepository.saveHomeData(
                            filter = filter,
                            serviceId = serviceId,
                            data = CachedHomeData(
                                overviewStats = savedState.overviewStats,
                                eventsByType = savedState.eventsByType,
                                hasLoadedEventsByType = savedState.hasLoadedEventsByType,
                                cameraList = dashboardRepository.getCamerasInMemory().ifEmpty { savedState.cameraList },
                                aiServices = savedState.aiServices,
                                recentEvents = savedState.recentEvents,
                                aiModuleEvents = savedState.aiModuleEvents,
                                runningAiTasks = savedState.runningAiTasks,
                                totalAiTasks = savedState.totalAiTasks,
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) Log.w(TAG, "Error reconciling overview stats in background", e)
            }
        }
    }

    fun setTimeFilter(filter: TimeFilter) {
        val isCustom = filter == TimeFilter.CUSTOM_DATE || filter == TimeFilter.CUSTOM_RANGE
        if (!isCustom && _uiState.value.selectedFilter == filter) return
        statsReconcileJob?.cancel()
        moduleEventsJob?.cancel()
        pendingNotifyRefresh = false
        pendingNetworkRefresh = false
        lastByTypeOverviewSyncMs = 0L
        lastCountedRealtimeEventId = 0
        _uiState.update { cur ->
            cur.copy(
                selectedFilter = filter,
                customDateStartMs = if (filter == TimeFilter.CUSTOM_DATE) TimeFilter.customDateStartMs else cur.customDateStartMs,
                customDateEndMs = if (filter == TimeFilter.CUSTOM_DATE) TimeFilter.customDateEndMs else cur.customDateEndMs,
                customRangeFromMs = if (filter == TimeFilter.CUSTOM_RANGE) TimeFilter.customRangeFromMs else cur.customRangeFromMs,
                customRangeToMs = if (filter == TimeFilter.CUSTOM_RANGE) TimeFilter.customRangeToMs else cur.customRangeToMs,
                isRefreshing = false
            )
        }
        fetchDashboardData()
    }

    fun setServiceFilter(serviceId: Int?) {
        if (_uiState.value.selectedServiceId == serviceId) return
        statsReconcileJob?.cancel()
        moduleEventsJob?.cancel()
        pendingNotifyRefresh = false
        pendingNetworkRefresh = false
        lastByTypeOverviewSyncMs = 0L
        lastCountedRealtimeEventId = 0
        _uiState.update { it.copy(selectedServiceId = serviceId, isRefreshing = false) }
        fetchDashboardData()
    }

    fun applyFilters(filter: TimeFilter, serviceId: Int?) {
        val isCustom = filter == TimeFilter.CUSTOM_DATE || filter == TimeFilter.CUSTOM_RANGE
        if (!isCustom && _uiState.value.selectedFilter == filter && _uiState.value.selectedServiceId == serviceId) return
        statsReconcileJob?.cancel()
        moduleEventsJob?.cancel()
        pendingNotifyRefresh = false
        pendingNetworkRefresh = false
        lastByTypeOverviewSyncMs = 0L
        lastCountedRealtimeEventId = 0
        _uiState.update { cur ->
            cur.copy(
                selectedFilter = filter,
                selectedServiceId = serviceId,
                customDateStartMs = if (filter == TimeFilter.CUSTOM_DATE) TimeFilter.customDateStartMs else cur.customDateStartMs,
                customDateEndMs = if (filter == TimeFilter.CUSTOM_DATE) TimeFilter.customDateEndMs else cur.customDateEndMs,
                customRangeFromMs = if (filter == TimeFilter.CUSTOM_RANGE) TimeFilter.customRangeFromMs else cur.customRangeFromMs,
                customRangeToMs = if (filter == TimeFilter.CUSTOM_RANGE) TimeFilter.customRangeToMs else cur.customRangeToMs,
                isRefreshing = false
            )
        }
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
        statsReconcileJob?.cancel()
        moduleEventsJob?.cancel()
        pendingNetworkRefresh = false
        if (UserSession.accessToken.isEmpty() || UserSession.domain.isEmpty()) return
        dashboardJob = viewModelScope.launch(Dispatchers.IO) {
            val startState = _uiState.value
            val filter = startState.selectedFilter
            val serviceId = startState.selectedServiceId
            val customDateStartMs = startState.customDateStartMs
            val customDateEndMs = startState.customDateEndMs
            val customRangeFromMs = startState.customRangeFromMs
            val customRangeToMs = startState.customRangeToMs
            val (fromTime, toTime) = TimeFilter.getDateRange(
                filter = filter,
                customDateStartMs = customDateStartMs,
                customDateEndMs = customDateEndMs,
                customRangeFromMs = customRangeFromMs,
                customRangeToMs = customRangeToMs
            )
            val fromSec = fromTime / 1000
            val toSec = toTime / 1000
            val shelfCacheKey = moduleEventsCacheKey(
                filter = filter,
                serviceId = serviceId,
                customDateStartMs = customDateStartMs,
                customDateEndMs = customDateEndMs,
                customRangeFromMs = customRangeFromMs,
                customRangeToMs = customRangeToMs
            )
            val canUseRepoCache = matchesGlobalCustomFilter(
                filter = filter,
                customDateStartMs = customDateStartMs,
                customDateEndMs = customDateEndMs,
                customRangeFromMs = customRangeFromMs,
                customRangeToMs = customRangeToMs
            )

            // 1. Ưu tiên đọc ngay dữ liệu từ RAM / Disk Cache (Stale-While-Revalidate) khi chưa phải thao tác kéo refresh
            val cachedData = if (canUseRepoCache) dashboardRepository.getHomeData(filter, serviceId) else null
            val cachedShelves = getModuleEventsCache(shelfCacheKey).orEmpty()
            if (!isUserRefresh) {
                if (cachedData != null) {
                    val snap = _uiState.value
                    val (permittedCameras, restoredShelves, cachedRecent, precomputedCacheMetrics) = withContext(Dispatchers.Default) {
                        val rawCachedCameras = cachedData.cameraList
                            .ifEmpty { dashboardRepository.getCamerasInMemory() }
                            .ifEmpty { snap.cameraList }
                        val permCams = UserPermissionManager.filterPermittedCameras(rawCachedCameras)
                        val shelves = cachedShelves.ifEmpty {
                            filterCachedModuleEvents(
                                rawMap = cachedData.aiModuleEvents,
                                fromSec = fromSec,
                                toSec = toSec,
                                allCameras = rawCachedCameras
                            )
                        }
                        val recent = cachedData.recentEvents
                            .filter { it.isWithinTimeRangeSeconds(fromSec, toSec) }
                            .permittedDistinct(rawCachedCameras)
                            .take(MAX_RECENT_EVENTS)
                        val preview = snap.copy(
                            overviewStats = cachedData.overviewStats,
                            eventsByType = cachedData.eventsByType,
                            hasLoadedEventsByType = cachedData.hasLoadedEventsByType || cachedData.eventsByType.isNotEmpty(),
                            cameraList = permCams,
                            recentEvents = recent
                        ).withRecomputedMetrics()
                        Quad(permCams, shelves, recent, preview)
                    }
                    if (cachedShelves.isEmpty() && restoredShelves.isNotEmpty()) {
                        putModuleEventsCache(shelfCacheKey, restoredShelves)
                    }
                    _uiState.update { cur ->
                        if (!cur.matchesFilter(filter, serviceId, customDateStartMs, customDateEndMs, customRangeFromMs, customRangeToMs)) {
                            return@update cur
                        }
                        val next = cur.copy(
                            isLoading = false,
                            error = null,
                            overviewStats = cachedData.overviewStats,
                            eventsByType = cachedData.eventsByType,
                            hasLoadedEventsByType = cachedData.hasLoadedEventsByType || cachedData.eventsByType.isNotEmpty(),
                            aiModuleEvents = if (restoredShelves == cur.aiModuleEvents) cur.aiModuleEvents else restoredShelves,
                            loadingAiModuleTypes = emptySet(),
                            cameraList = permittedCameras,
                            aiServices = cachedData.aiServices.ifEmpty { cur.aiServices },
                            recentEvents = cachedRecent,
                            runningAiTasks = if (cachedData.totalAiTasks > 0) cachedData.runningAiTasks else cur.runningAiTasks,
                            totalAiTasks = if (cachedData.totalAiTasks > 0) cachedData.totalAiTasks else cur.totalAiTasks,
                            totalDetectionsCount = precomputedCacheMetrics.totalDetectionsCount,
                            aiDistribution = if (precomputedCacheMetrics.aiDistribution == cur.aiDistribution) cur.aiDistribution else precomputedCacheMetrics.aiDistribution,
                            totalCamerasCount = precomputedCacheMetrics.totalCamerasCount,
                            onlineCamerasCount = precomputedCacheMetrics.onlineCamerasCount,
                            recordingCamerasCount = precomputedCacheMetrics.recordingCamerasCount,
                            offlineCameraIds = if (precomputedCacheMetrics.offlineCameraIds == cur.offlineCameraIds) cur.offlineCameraIds else precomputedCacheMetrics.offlineCameraIds
                        )
                        if (next == cur) cur else next
                    }
                } else {
                    // Chưa có cache cho bộ lọc mới: xóa ngay dữ liệu sự kiện/thống kê của bộ lọc cũ để tránh hiển thị nhầm số liệu
                    _uiState.update { cur ->
                        if (!cur.matchesFilter(filter, serviceId, customDateStartMs, customDateEndMs, customRangeFromMs, customRangeToMs)) {
                            return@update cur
                        }
                        cur.copy(
                            isLoading = true,
                            error = null,
                            overviewStats = emptyList(),
                            eventsByType = emptyList(),
                            hasLoadedEventsByType = false,
                            aiDistribution = emptyList(),
                            aiModuleEvents = cachedShelves,
                            loadingAiModuleTypes = emptySet(),
                            recentEvents = emptyList(),
                            totalDetectionsCount = 0L
                        )
                    }
                }
            }

            val fetchStartedMaxId = _uiState.value.recentEvents.maxOfOrNull { it.id } ?: 0

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

                    val hasWarmCameras = dashboardRepository.getCamerasInMemory().isNotEmpty()
                    if (UserPermissionManager.isCustomCameraPermission() && !hasWarmCameras) {
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

                    // --- GIAI ĐOẠN 1: Trả kết quả sự kiện & thống kê chính và tắt vòng xoay refresh ngay tức thì ---
                    val overviewResult = overviewDeferred.await()
                    val byTypeResult = byTypeDeferred.await()
                    val recentResult = recentEventsDeferred.await()
                    val earlyCameraResult = if (!hasWarmCameras || cameraDeferred.isCompleted) cameraDeferred.await() else null

                    val overviewResponse = overviewResult.getOrNull()
                    val byTypeResponse = byTypeResult.getOrNull()
                    val byTypeSucceeded = byTypeResponse?.isSuccessful == true
                    var recentEvents = recentResult.events
                    var recentEventsSucceeded = recentResult.anySuccessful
                    val earlyCameraResponse = earlyCameraResult?.getOrNull()

                    if (!_uiState.value.matchesFilter(filter, serviceId, customDateStartMs, customDateEndMs, customRangeFromMs, customRangeToMs)) {
                        return@coroutineScope
                    }

                    val overviewStats = if (overviewResponse?.isSuccessful == true) {
                        overviewResponse.body() ?: emptyList()
                    } else _uiState.value.overviewStats

                    val byTypeBody = byTypeResponse?.body().orEmpty()
                    val wave1Alive = NetworkMonitor.isNetworkAvailable() &&
                        (overviewResponse?.isSuccessful == true ||
                            byTypeSucceeded ||
                            recentEventsSucceeded)

                    val fallbackByType = if (
                        !(byTypeSucceeded && byTypeBody.isNotEmpty()) &&
                        wave1Alive &&
                        UserPermissionManager.canAccessEvents()
                    ) {
                        fetchEventCountsViaOverview(
                            fromTime = fromTime,
                            toTime = toTime,
                            serviceIdStr = serviceIdStr,
                            unfilteredOverviewStats = overviewStats,
                            seedEvents = recentEvents
                        )
                    } else {
                        null
                    }

                    val (eventsByType, hasLoadedEventsByType) = when {
                        byTypeSucceeded && byTypeBody.isNotEmpty() -> {
                            lastByTypeOverviewSyncMs = SystemClock.elapsedRealtime()
                            byTypeBody to true
                        }
                        fallbackByType != null -> {
                            lastByTypeOverviewSyncMs = SystemClock.elapsedRealtime()
                            fallbackByType to true
                        }
                        else -> {
                            _uiState.value.eventsByType to (byTypeSucceeded || _uiState.value.hasLoadedEventsByType)
                        }
                    }

                    val initialCameraList = if (earlyCameraResponse?.isSuccessful == true) {
                        earlyCameraResponse.body() ?: emptyList()
                    } else {
                        dashboardRepository.getCamerasInMemory().ifEmpty { _uiState.value.cameraList }
                    }

                    // Nếu truy vấn không truyền `type` bị nghẽn bởi các bản ghi sensor bị tắt / ngoài phân quyền (vd. Heatmap...)
                    // hoặc thống kê Phân bố sự kiện AI cho thấy có nhiều sự kiện hơn kết quả thu được, truy vấn bổ sung đúng các `type` đang có sự kiện.
                    val permittedStatsCount = eventsByType
                        .filter { AiTypeHelper.isAiTypeEnabled(it.type) }
                        .sumOf { it.count.toLong() }
                    if (wave1Alive &&
                        UserPermissionManager.canAccessEvents() &&
                        recentEvents.size < MAX_RECENT_EVENTS &&
                        (!recentResult.reachedEnd || permittedStatsCount > recentEvents.size.toLong())
                    ) {
                        val targetTypes = expandPermittedApiEventTypesFromCounts(eventsByType)
                            .ifEmpty { if (!recentResult.reachedEnd) resolveAllPermittedApiEventTypes() else emptyList() }
                        if (targetTypes.isNotEmpty()) {
                            val typedFallback = fetchPermittedRecentEventsByTypes(
                                fromSec = fromSec,
                                toSec = toSec,
                                serviceId = serviceId,
                                candidateTypes = targetTypes,
                                allCameras = initialCameraList
                            )
                            if (typedFallback.anySuccessful) {
                                recentEventsSucceeded = true
                                recentEvents = (recentEvents + typedFallback.events)
                                    .distinctBy { it.id }
                                    .sortedWith(EventDescendingComparator)
                                    .take(MAX_RECENT_EVENTS)
                            }
                        }
                    }

                    if (hasLoadedEventsByType) {
                        lastCountedRealtimeEventId = maxOf(
                            lastCountedRealtimeEventId,
                            recentEvents.maxOfOrNull { it.id } ?: 0
                        )
                    }

                    val initialPermittedCameras = UserPermissionManager.filterPermittedCameras(initialCameraList)
                    val (preliminaryRunningTasks, preliminaryTotalTasks) = resolveMonitorCountsFromOverview(
                        overviewStats = overviewStats,
                        serviceIdStr = serviceIdStr,
                        fallbackRunning = _uiState.value.runningAiTasks,
                        fallbackTotal = _uiState.value.totalAiTasks
                    )

                    val primaryFailure = overviewResult.exceptionOrNull()
                        ?: recentResult.firstError
                        ?: byTypeResult.exceptionOrNull()
                        ?: earlyCameraResult?.exceptionOrNull()
                    val errorMsg = primaryFailure?.toUserFriendlyMessage()?.takeIf { it.isNotEmpty() }

                    if (UserSession.accessToken.isEmpty()) return@coroutineScope

                    val fetchedMaxId = recentEvents.maxOfOrNull { it.id } ?: 0
                    val stage1Snap = _uiState.value
                    val (permittedFetchedRecent, preStage1Merged, preStage1Metrics) = withContext(Dispatchers.Default) {
                        val permittedFetched = recentEvents
                            .filter { it.isWithinTimeRangeSeconds(fromSec, toSec) }
                            .permittedDistinct(initialCameraList)
                        val newerRealtime = when {
                            recentEventsSucceeded && (isUserRefresh || fetchedMaxId == 0) -> emptyList()
                            recentEventsSucceeded -> {
                                val cutoffId = maxOf(fetchedMaxId, fetchStartedMaxId)
                                stage1Snap.recentEvents.filter { it.id > cutoffId && it.isWithinTimeRangeSeconds(fromSec, toSec) }
                            }
                            else -> stage1Snap.recentEvents.filter { it.isWithinTimeRangeSeconds(fromSec, toSec) }
                        }
                        val merged = (newerRealtime + permittedFetched)
                            .permittedDistinct(initialCameraList)
                            .distinctBy { it.id }
                            .sortedWith(EventDescendingComparator)
                            .take(MAX_RECENT_EVENTS)
                        val preview = stage1Snap.copy(
                            overviewStats = overviewStats,
                            eventsByType = eventsByType,
                            hasLoadedEventsByType = hasLoadedEventsByType,
                            cameraList = initialPermittedCameras,
                            recentEvents = merged
                        ).withRecomputedMetrics()
                        Triple(permittedFetched, merged, preview)
                    }

                    var committedStage1: HomeUiState? = null
                    _uiState.update { cur ->
                        if (!cur.matchesFilter(filter, serviceId, customDateStartMs, customDateEndMs, customRangeFromMs, customRangeToMs)) {
                            return@update cur
                        }
                        val next = if (
                            cur.recentEvents === stage1Snap.recentEvents &&
                            cur.offlineCameraIds === stage1Snap.offlineCameraIds &&
                            cur.onlineCamerasCount == stage1Snap.onlineCamerasCount &&
                            cur.recordingCamerasCount == stage1Snap.recordingCamerasCount
                        ) {
                            cur.copy(
                                isLoading = false,
                                isRefreshing = false,
                                overviewStats = overviewStats,
                                eventsByType = eventsByType,
                                hasLoadedEventsByType = hasLoadedEventsByType,
                                cameraList = initialPermittedCameras,
                                recentEvents = if (preStage1Merged == cur.recentEvents) cur.recentEvents else preStage1Merged,
                                runningAiTasks = preliminaryRunningTasks,
                                totalAiTasks = preliminaryTotalTasks,
                                error = errorMsg,
                                totalDetectionsCount = preStage1Metrics.totalDetectionsCount,
                                aiDistribution = if (preStage1Metrics.aiDistribution == cur.aiDistribution) cur.aiDistribution else preStage1Metrics.aiDistribution,
                                totalCamerasCount = preStage1Metrics.totalCamerasCount,
                                onlineCamerasCount = preStage1Metrics.onlineCamerasCount,
                                recordingCamerasCount = preStage1Metrics.recordingCamerasCount,
                                offlineCameraIds = if (preStage1Metrics.offlineCameraIds == cur.offlineCameraIds) cur.offlineCameraIds else preStage1Metrics.offlineCameraIds
                            )
                        } else {
                            val newerRealtime = when {
                                recentEventsSucceeded && (isUserRefresh || fetchedMaxId == 0) -> emptyList()
                                recentEventsSucceeded -> {
                                    val cutoffId = maxOf(fetchedMaxId, fetchStartedMaxId)
                                    cur.recentEvents.filter { it.id > cutoffId && it.isWithinTimeRangeSeconds(fromSec, toSec) }
                                }
                                else -> cur.recentEvents.filter { it.isWithinTimeRangeSeconds(fromSec, toSec) }
                            }
                            val mergedRecent = (newerRealtime + permittedFetchedRecent)
                                .distinctBy { it.uniqueKey() }
                                .distinctBy { it.id }
                                .sortedWith(EventDescendingComparator)
                                .take(MAX_RECENT_EVENTS)
                            cur.copy(
                                isLoading = false,
                                isRefreshing = false,
                                overviewStats = overviewStats,
                                eventsByType = eventsByType,
                                hasLoadedEventsByType = hasLoadedEventsByType,
                                cameraList = initialPermittedCameras,
                                recentEvents = if (mergedRecent == cur.recentEvents) cur.recentEvents else mergedRecent,
                                runningAiTasks = preliminaryRunningTasks,
                                totalAiTasks = preliminaryTotalTasks,
                                error = errorMsg
                            ).withRecomputedMetrics()
                        }
                        committedStage1 = next
                        if (next == cur) cur else next
                    }
                    val stage1Committed = committedStage1 ?: return@coroutineScope

                    // Nạp song song tối đa 5 sự kiện gần nhất cho từng bài AI trong Phân bố AI (chạy nền, không chặn tắt refresh)
                    if (UserPermissionManager.canAccessEvents()) {
                        launchAiModuleShelvesFetch(
                            filter = filter,
                            serviceId = serviceId,
                            customDateStartMs = customDateStartMs,
                            customDateEndMs = customDateEndMs,
                            customRangeFromMs = customRangeFromMs,
                            customRangeToMs = customRangeToMs,
                            fromSec = fromSec,
                            toSec = toSec,
                            distribution = stage1Committed.aiDistribution,
                            seedEvents = stage1Committed.recentEvents,
                            allCameras = initialCameraList
                        )
                    }

                    // --- GIAI ĐOẠN 2 (chạy nền sau khi đã tắt vòng xoay refresh): Đồng bộ Camera, AI Services & số bài AI đang chạy ---
                    val cameraResult = earlyCameraResult ?: cameraDeferred.await()
                    val aiServiceResult = aiServiceDeferred.await()
                    val cameraResponse = cameraResult.getOrNull()
                    val aiServiceResponse = aiServiceResult.getOrNull()

                    if (!_uiState.value.matchesFilter(filter, serviceId, customDateStartMs, customDateEndMs, customRangeFromMs, customRangeToMs)) {
                        return@coroutineScope
                    }

                    val allCameraList = if (cameraResponse?.isSuccessful == true) {
                        cameraResponse.body() ?: emptyList()
                    } else {
                        dashboardRepository.getCamerasInMemory().ifEmpty { _uiState.value.cameraList }
                    }
                    val permittedCameraList = UserPermissionManager.filterPermittedCameras(allCameraList)
                    val aiServices = if (aiServiceResponse?.isSuccessful == true) {
                        aiServiceResponse.body() ?: emptyList()
                    } else {
                        _uiState.value.aiServices
                    }

                    var runningTasks = preliminaryRunningTasks
                    var totalTasks = preliminaryTotalTasks
                    val canProbeMonitors = NetworkMonitor.isNetworkAvailable() &&
                        UserPermissionManager.canAccessEvents() &&
                        (wave1Alive || aiServiceResponse?.isSuccessful == true)

                    if (canProbeMonitors) {
                        val probedCounts = fetchActiveAiTaskCounts(
                            serviceId = serviceId,
                            aiServices = aiServices,
                            overviewStats = overviewStats
                        )
                        if (probedCounts != null && probedCounts.second > 0) {
                            runningTasks = probedCounts.first
                            totalTasks = probedCounts.second
                        }
                    }

                    if (UserSession.accessToken.isEmpty()) return@coroutineScope

                    val stage2Snap = _uiState.value
                    val (preCleanedRecent, preStage2Metrics) = withContext(Dispatchers.Default) {
                        val cleaned = stage2Snap.recentEvents
                            .filter { it.isWithinTimeRangeSeconds(fromSec, toSec) }
                            .permittedDistinct(allCameraList)
                            .distinctBy { it.id }
                            .sortedWith(EventDescendingComparator)
                            .take(MAX_RECENT_EVENTS)
                        val preview = stage2Snap.copy(
                            cameraList = permittedCameraList,
                            aiServices = aiServices,
                            recentEvents = cleaned,
                            runningAiTasks = runningTasks,
                            totalAiTasks = totalTasks
                        ).withRecomputedMetrics()
                        cleaned to preview
                    }

                    var committedStage2: HomeUiState? = null
                    val hadNoCamerasBefore = stage2Snap.cameraList.isEmpty()
                    _uiState.update { cur ->
                        if (!cur.matchesFilter(filter, serviceId, customDateStartMs, customDateEndMs, customRangeFromMs, customRangeToMs)) {
                            return@update cur
                        }
                        val next = if (
                            cur.recentEvents === stage2Snap.recentEvents &&
                            cur.overviewStats === stage2Snap.overviewStats &&
                            cur.eventsByType === stage2Snap.eventsByType &&
                            cur.offlineCameraIds === stage2Snap.offlineCameraIds &&
                            cur.onlineCamerasCount == stage2Snap.onlineCamerasCount &&
                            cur.recordingCamerasCount == stage2Snap.recordingCamerasCount
                        ) {
                            cur.copy(
                                cameraList = permittedCameraList,
                                aiServices = aiServices,
                                recentEvents = if (preCleanedRecent == cur.recentEvents) cur.recentEvents else preCleanedRecent,
                                runningAiTasks = runningTasks,
                                totalAiTasks = totalTasks,
                                totalDetectionsCount = preStage2Metrics.totalDetectionsCount,
                                aiDistribution = if (preStage2Metrics.aiDistribution == cur.aiDistribution) cur.aiDistribution else preStage2Metrics.aiDistribution,
                                totalCamerasCount = preStage2Metrics.totalCamerasCount,
                                onlineCamerasCount = preStage2Metrics.onlineCamerasCount,
                                recordingCamerasCount = preStage2Metrics.recordingCamerasCount,
                                offlineCameraIds = if (preStage2Metrics.offlineCameraIds == cur.offlineCameraIds) cur.offlineCameraIds else preStage2Metrics.offlineCameraIds
                            )
                        } else {
                            val cleanedRecent = cur.recentEvents
                                .filter { it.isWithinTimeRangeSeconds(fromSec, toSec) }
                                .permittedDistinct(allCameraList)
                                .distinctBy { it.id }
                                .sortedWith(EventDescendingComparator)
                                .take(MAX_RECENT_EVENTS)
                            cur.copy(
                                cameraList = permittedCameraList,
                                aiServices = aiServices,
                                recentEvents = if (cleanedRecent == cur.recentEvents) cur.recentEvents else cleanedRecent,
                                runningAiTasks = runningTasks,
                                totalAiTasks = totalTasks
                            ).withRecomputedMetrics()
                        }
                        committedStage2 = next
                        if (next == cur) cur else next
                    }

                    val latestState = committedStage2 ?: return@coroutineScope
                    dashboardRepository.cacheInMemory(
                        events = latestState.recentEvents,
                        cameras = allCameraList,
                        aiServices = aiServices,
                        fromNetwork = cameraResponse?.isSuccessful == true
                    )

                    if (hadNoCamerasBefore && permittedCameraList.isNotEmpty() && latestState.favoriteCameraIds.isNotEmpty()) {
                        refreshFavoriteCameraSnapshots(latestState.favoriteCameraIds)
                    }

                    if (canUseRepoCache &&
                        (overviewResponse?.isSuccessful == true || byTypeSucceeded || fallbackByType != null || recentEventsSucceeded)
                    ) {
                        val newCachedData = CachedHomeData(
                            overviewStats = latestState.overviewStats,
                            eventsByType = latestState.eventsByType,
                            hasLoadedEventsByType = latestState.hasLoadedEventsByType,
                            cameraList = allCameraList,
                            aiServices = aiServices,
                            recentEvents = latestState.recentEvents,
                            aiModuleEvents = latestState.aiModuleEvents,
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

    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    private fun moduleEventsCacheKey(
        filter: TimeFilter,
        serviceId: Int?,
        customDateStartMs: Long = TimeFilter.customDateStartMs,
        customDateEndMs: Long = TimeFilter.customDateEndMs,
        customRangeFromMs: Long = TimeFilter.customRangeFromMs,
        customRangeToMs: Long = TimeFilter.customRangeToMs
    ): String =
        "${TimeFilter.cacheKeyToken(filter, customDateStartMs, customDateEndMs, customRangeFromMs, customRangeToMs)}_${serviceId ?: -1}"

    private fun resolveDistributionBucketKey(rawType: String): String {
        val mod = AiTypeHelper.findModuleByEventType(rawType)
        return mod?.sensorType ?: rawType.trim()
    }

    private fun mergeRealtimeEventsIntoModuleShelves(
        currentMap: Map<String, List<EventData>>,
        newEvents: List<EventData>,
        fromSec: Long,
        toSec: Long,
        allCameras: List<CameraModel>
    ): Map<String, List<EventData>> {
        if (newEvents.isEmpty()) return currentMap
        val permittedFresh = newEvents
            .filter { it.isWithinTimeRangeSeconds(fromSec, toSec) }
            .permittedDistinct(allCameras)
        if (permittedFresh.isEmpty()) return currentMap

        val updated = LinkedHashMap<String, List<EventData>>(currentMap)
        val grouped = permittedFresh.groupBy { resolveDistributionBucketKey(it.type) }
        for ((bucketKey, freshForBucket) in grouped) {
            if (bucketKey.isEmpty()) continue
            val existing = updated[bucketKey].orEmpty()
                .filter { it.isWithinTimeRangeSeconds(fromSec, toSec) }
            val merged = (freshForBucket + existing)
                .distinctBy { it.id }
                .sortedWith(EventDescendingComparator)
                .take(MAX_RECENT_EVENTS)
            updated[bucketKey] = merged
        }
        return updated
    }

    /**
     * Tải song song tối đa 5 sự kiện mới nhất cho từng bài AI theo đúng thứ tự `distribution` từ trên xuống.
     * Cập nhật tiến dần (progressive update) ngay khi từng bài AI trả dữ liệu về để các hàng trên cùng hiển thị tức thì.
     */
    private fun launchAiModuleShelvesFetch(
        filter: TimeFilter,
        serviceId: Int?,
        customDateStartMs: Long,
        customDateEndMs: Long,
        customRangeFromMs: Long,
        customRangeToMs: Long,
        fromSec: Long,
        toSec: Long,
        distribution: List<HomeAiDistributionItem>,
        seedEvents: List<EventData>,
        allCameras: List<CameraModel>
    ) {
        moduleEventsJob?.cancel()
        val cacheKey = moduleEventsCacheKey(
            filter = filter,
            serviceId = serviceId,
            customDateStartMs = customDateStartMs,
            customDateEndMs = customDateEndMs,
            customRangeFromMs = customRangeFromMs,
            customRangeToMs = customRangeToMs
        )
        if (distribution.isEmpty()) {
            removeModuleEventsCache(cacheKey)
            _uiState.update { cur ->
                if (cur.matchesFilter(filter, serviceId, customDateStartMs, customDateEndMs, customRangeFromMs, customRangeToMs)) {
                    cur.copy(aiModuleEvents = emptyMap(), loadingAiModuleTypes = emptySet())
                } else {
                    cur
                }
            }
            return
        }

        val activeKeys = distribution.map { it.filterSensorType }.toSet()
        val seededFromRecent = seedEvents
            .filter { it.isWithinTimeRangeSeconds(fromSec, toSec) }
            .groupBy { resolveDistributionBucketKey(it.type) }

        // Hiển thị ngay dữ liệu cache/seed sẵn nếu có, đồng thời bật skeleton cho các bài AI chưa có đủ dữ liệu
        _uiState.update { cur ->
            if (!cur.matchesFilter(filter, serviceId, customDateStartMs, customDateEndMs, customRangeFromMs, customRangeToMs)) return@update cur
            val nextMap = LinkedHashMap<String, List<EventData>>()
            val loadingSet = LinkedHashSet<String>()
            for (item in distribution) {
                val key = item.filterSensorType
                val existing = cur.aiModuleEvents[key].orEmpty()
                    .filter { it.isWithinTimeRangeSeconds(fromSec, toSec) }
                val seed = seededFromRecent[key].orEmpty()
                val combined = (seed + existing)
                    .distinctBy { it.id }
                    .sortedWith(EventDescendingComparator)
                    .take(MAX_RECENT_EVENTS)
                if (combined.isNotEmpty()) {
                    nextMap[key] = combined
                }
                if (combined.isEmpty()) {
                    loadingSet.add(key)
                }
            }
            cur.copy(
                aiModuleEvents = nextMap,
                loadingAiModuleTypes = loadingSet
            )
        }

        if (!NetworkMonitor.isNetworkAvailable()) {
            _uiState.update { cur ->
                if (cur.matchesFilter(filter, serviceId, customDateStartMs, customDateEndMs, customRangeFromMs, customRangeToMs)) {
                    cur.copy(loadingAiModuleTypes = emptySet())
                } else {
                    cur
                }
            }
            return
        }

        moduleEventsJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val semaphore = Semaphore(MAX_OVERVIEW_BY_TYPE_CONCURRENCY)
                val perTypeCount = if (UserPermissionManager.isCustomCameraPermission()) {
                    MEDIUM_RECENT_BATCH_SIZE
                } else {
                    FAST_RECENT_BATCH_SIZE
                }
                val camerasForPerm = allCameras.ifEmpty { dashboardRepository.getCamerasInMemory() }

                val moduleResults = coroutineScope {
                    distribution.map { item ->
                        async {
                            val bucketKey = item.filterSensorType
                            val apiTypes = resolveApiEventTypes(bucketKey)
                                .ifEmpty { listOf(bucketKey) }
                            val rawInRangeForModule = ArrayList<EventData>(MAX_RECENT_EVENTS * 2)
                            var anySuccess = false

                            for (apiType in apiTypes) {
                                val res = semaphore.withPermit {
                                    safeApiCall {
                                        dashboardRepository.getEventDataList(
                                            count = perTypeCount,
                                            lastId = null,
                                            from = fromSec,
                                            to = toSec,
                                            serviceId = serviceId,
                                            type = apiType
                                        )
                                    }
                                }
                                val resp = res.getOrNull()
                                if (resp != null && resp.isSuccessful) {
                                    anySuccess = true
                                    val body = resp.body().orEmpty()
                                    for (ev in body) {
                                        if (ev.isWithinTimeRangeSeconds(fromSec, toSec)) {
                                            rawInRangeForModule.add(ev)
                                        }
                                    }
                                }
                            }

                            val topEvents = UserPermissionManager.filterEventsByPermissions(
                                events = rawInRangeForModule,
                                allCameras = camerasForPerm
                            )
                                .distinctBy { it.id }
                                .sortedWith(EventDescendingComparator)
                                .take(MAX_RECENT_EVENTS)

                            if (topEvents.isNotEmpty()) {
                                dashboardRepository.cacheInMemory(events = topEvents)
                            }
                            Triple(bucketKey, anySuccess, topEvents)
                        }
                    }.awaitAll()
                }

                // Gộp cập nhật 1 lần duy nhất sau khi tải song song tất cả bài AI để tránh chuỗi 6-10 lần recompose + nhảy layout lúc mở app
                var didChangeShelves = false
                var committedModuleMap: Map<String, List<EventData>>? = null
                _uiState.update { cur ->
                    if (!cur.matchesFilter(filter, serviceId, customDateStartMs, customDateEndMs, customRangeFromMs, customRangeToMs)) {
                        return@update cur
                    }
                    val nextMap = LinkedHashMap<String, List<EventData>>(cur.aiModuleEvents)
                    for ((bucketKey, anySuccess, topEvents) in moduleResults) {
                        if (anySuccess) {
                            val existing = cur.aiModuleEvents[bucketKey]
                            nextMap[bucketKey] = if (existing == topEvents) existing else topEvents
                        }
                    }
                    // Dọn các key không còn thuộc aiDistribution hiện tại
                    nextMap.keys.retainAll(activeKeys)
                    val finalMap = if (nextMap == cur.aiModuleEvents) cur.aiModuleEvents else nextMap.toMap()
                    committedModuleMap = finalMap
                    if (finalMap === cur.aiModuleEvents && cur.loadingAiModuleTypes.isEmpty()) {
                        cur
                    } else {
                        didChangeShelves = finalMap !== cur.aiModuleEvents
                        cur.copy(
                            aiModuleEvents = finalMap,
                            loadingAiModuleTypes = emptySet()
                        )
                    }
                }

                committedModuleMap?.let { putModuleEventsCache(cacheKey, it) }

                // Lưu kèm các hàng bài AI vào disk cache để lần mở app tiếp theo hiển thị tức thì 0ms
                if (didChangeShelves &&
                    matchesGlobalCustomFilter(filter, customDateStartMs, customDateEndMs, customRangeFromMs, customRangeToMs)
                ) {
                    val latest = _uiState.value
                    if (latest.matchesFilter(filter, serviceId, customDateStartMs, customDateEndMs, customRangeFromMs, customRangeToMs) &&
                        UserSession.accessToken.isNotEmpty()
                    ) {
                        dashboardRepository.saveHomeData(
                            filter = filter,
                            serviceId = serviceId,
                            data = CachedHomeData(
                                overviewStats = latest.overviewStats,
                                eventsByType = latest.eventsByType,
                                hasLoadedEventsByType = latest.hasLoadedEventsByType,
                                cameraList = dashboardRepository.getCamerasInMemory().ifEmpty { latest.cameraList },
                                aiServices = latest.aiServices,
                                recentEvents = latest.recentEvents,
                                aiModuleEvents = latest.aiModuleEvents,
                                runningAiTasks = latest.runningAiTasks,
                                totalAiTasks = latest.totalAiTasks,
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.update { cur ->
                    if (cur.matchesFilter(filter, serviceId, customDateStartMs, customDateEndMs, customRangeFromMs, customRangeToMs)) {
                        cur.copy(loadingAiModuleTypes = emptySet())
                    } else {
                        cur
                    }
                }
            }
        }
    }

    private fun isPrivateOrLoopbackHost(host: String): Boolean {
        val h = host.trim().lowercase()
        if (h.isEmpty()) return false
        if (h == "localhost" || h.startsWith("127.") || h.startsWith("10.") || h.startsWith("192.168.")) {
            return true
        }
        if (h.startsWith("172.")) {
            val second = h.removePrefix("172.").substringBefore('.').toIntOrNull()
            if (second != null && second in 16..31) return true
        }
        return false
    }

    /**
     * Khớp hoàn toàn với `fetchMonitorList` trong `web_bundle.js`:
     * - Tuyệt đối không fallback sang `srv.privateHost` (IP nội bộ của AI Box khiến app treo chờ TCP timeout).
     * - Nếu domain đang đăng nhập là tên miền Internet công khai mà `srv.publicHost` lại khai báo IP nội bộ LAN/loopback,
     *   bỏ qua ngay ở 0ms và dùng số liệu từ `overviewStats`.
     */
    private fun resolveReachableExternalMonitorUrl(srv: AIServiceModel): String? {
        val pub = srv.publicHost.trim().trimEnd('/')
        if (pub.isEmpty()) return null
        if (pub.startsWith("/")) return "$pub/api/Monitor"
        val parsedExt = pub.toHttpUrlOrNull() ?: return null
        val mainDomainHost = UserSession.domain.trim().toHttpUrlOrNull()?.host.orEmpty()
        if (isPrivateOrLoopbackHost(parsedExt.host) && !isPrivateOrLoopbackHost(mainDomainHost)) {
            return null
        }
        return "$pub/api/Monitor"
    }

    private fun resolveMonitorCountsFromOverview(
        overviewStats: List<VmsCountOverviewModel>,
        serviceIdStr: String?,
        fallbackRunning: Int,
        fallbackTotal: Int
    ): Pair<Int, Int> {
        if (!UserPermissionManager.canAccessEvents() || UserPermissionManager.isCustomAiEventPermission()) {
            return fallbackRunning to fallbackTotal
        }
        val overviewMonitorCount = if (serviceIdStr != null) {
            overviewStats
                .find { it.label == "monitor-count" && it.tag == "item" && it.serviceId == serviceIdStr }
                ?.count?.toInt()
                ?: overviewStats
                    .find { it.label == "monitor-count" && it.tag == "all" }
                    ?.count?.toInt() ?: 0
        } else {
            overviewStats
                .find { it.label == "monitor-count" && it.tag == "all" }
                ?.count?.toInt() ?: 0
        }
        val overviewEnabledCount = if (serviceIdStr != null) {
            overviewStats
                .find { it.label == "monitor-enabled-count" && it.tag == "item" && it.serviceId == serviceIdStr }
                ?.count?.toInt() ?: 0
        } else {
            overviewStats
                .filter { it.label == "monitor-enabled-count" }
                .sumOf { it.count.toInt() }
        }
        return if (overviewMonitorCount > 0) {
            val runCnt = when {
                overviewEnabledCount > 0 -> overviewEnabledCount
                fallbackRunning > 0 -> fallbackRunning.coerceAtMost(overviewMonitorCount)
                else -> 0
            }
            runCnt to overviewMonitorCount
        } else {
            fallbackRunning to fallbackTotal
        }
    }

    private suspend fun fetchActiveAiTaskCounts(
        serviceId: Int?,
        aiServices: List<AIServiceModel>,
        overviewStats: List<VmsCountOverviewModel>
    ): Pair<Int, Int>? = coroutineScope {
        try {
            val targetServices = if (serviceId != null) {
                aiServices.filter { it.id == serviceId }
            } else {
                aiServices
            }
            if (targetServices.isEmpty()) {
                val monResp = withTimeoutOrNull(MONITOR_PROBE_TIMEOUT_MS) {
                    safeApiCall { dashboardRepository.getMonitorList() }.getOrNull()
                }
                if (monResp?.isSuccessful == true) {
                    val monitors = (monResp.body() ?: emptyList()).filter { AiTypeHelper.isAiTypeEnabled(it.type) }
                    return@coroutineScope monitors.count { it.enabled } to monitors.size
                }
                return@coroutineScope null
            }

            val monitorResults = targetServices.map { srv ->
                async {
                    srv.id to withTimeoutOrNull(MONITOR_PROBE_TIMEOUT_MS) {
                        if (srv.id == 0) {
                            safeApiCall { dashboardRepository.getMonitorList() }.getOrNull()
                        } else {
                            val extUrl = resolveReachableExternalMonitorUrl(srv)
                            if (extUrl != null) {
                                safeApiCall {
                                    dashboardRepository.getMonitorListExt(
                                        url = extUrl,
                                        authHeader = "Bearer ${srv.apiKey}"
                                    )
                                }.getOrNull()
                            } else {
                                null
                            }
                        }
                    }
                }
            }.awaitAll()

            var runningTasks = 0
            var totalTasks = 0
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
                        ?.count?.toInt() ?: 0
                    val srvRunning = overviewStats
                        .find { it.label == "monitor-enabled-count" && it.tag == "item" && it.serviceId == srvIdStr }
                        ?.count?.toInt() ?: 0
                    totalTasks += srvTotal
                    runningTasks += srvRunning
                }
            }
            if (totalTasks > 0) runningTasks to totalTasks else null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.w(TAG, "Fallback monitor count from overviewStats", e)
            null
        }
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

        val offlineIds = HashSet<Int>()
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
                if (isOffline) offlineIds.add(cam.id)
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

        val resolvedOfflineIds = if (offlineIds.isEmpty()) emptySet() else offlineIds.toSet()
        if (totalCamerasCount == totalCams &&
            onlineCamerasCount == onlineCams &&
            recordingCamerasCount == recordingCams &&
            offlineCameraIds == resolvedOfflineIds
        ) {
            return this
        }

        return copy(
            totalCamerasCount = totalCams,
            onlineCamerasCount = onlineCams,
            recordingCamerasCount = recordingCams,
            offlineCameraIds = resolvedOfflineIds
        )
    }

    companion object {
        private const val TAG = "HomeViewModel"
        private const val MAX_MODULE_EVENTS_CACHE_ENTRIES = 16
        private const val MAX_RECENT_EVENTS = 5
        private const val FAST_RECENT_BATCH_SIZE = 15
        private const val MEDIUM_RECENT_BATCH_SIZE = 25
        private const val RECENT_BATCH_SIZE = 50
        private const val MAX_BATCH_ATTEMPTS = 3
        private const val REALTIME_MIN_INTERVAL_MS = 350L
        private const val REALTIME_DB_COMMIT_RETRY_MS = 650L
        private const val MONITOR_PROBE_TIMEOUT_MS = 2_800L
        private const val STATUS_DEBOUNCE_MS = 800L
        private const val BY_TYPE_WS_REFRESH_INTERVAL_MS = 10_000L
        private const val MAX_OVERVIEW_BY_TYPE_CONCURRENCY = 16

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
