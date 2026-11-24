package com.tenli.oneview.ui.features.event

import android.app.Application
import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tenli.oneview.TenliApp
import com.tenli.oneview.data.local.CachedEventData
import com.tenli.oneview.data.local.UserPermissionManager
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.data.network.websocket.WebSocketManager
import com.tenli.oneview.data.repository.EventRepository
import com.tenli.oneview.model.network.AIServiceModel
import com.tenli.oneview.model.network.CameraModel
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.ui.features.home.TimeFilter
import com.tenli.oneview.ui.utils.AiTypeHelper
import com.tenli.oneview.util.EventBus
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

@Immutable
data class EventScreenUiState(
    val events: List<EventData> = emptyList(),
    val cameraList: List<CameraModel> = emptyList(),
    /** Map UUID camera → tên camera, tính sẵn để item danh sách không phải tra cứu lại */
    val cameraNameMap: Map<String, String> = emptyMap(),
    val aiServices: List<AIServiceModel> = emptyList(),
    val selectedFilter: TimeFilter = TimeFilter.TODAY,
    val selectedServiceId: Int? = null,
    val selectedCameraUuid: String? = null,
    val selectedAiType: String? = null,
    val customDateStartMs: Long = TimeFilter.customDateStartMs,
    val customDateEndMs: Long = TimeFilter.customDateEndMs,
    val customRangeFromMs: Long = TimeFilter.customRangeFromMs,
    val customRangeToMs: Long = TimeFilter.customRangeToMs,
    val isListView: Boolean = true,
    val isLoading: Boolean = false,
    /** Đang làm mới do người dùng kéo để tải lại (pull-to-refresh) */
    val isRefreshing: Boolean = false,
    val isPaginating: Boolean = false,
    val hasMore: Boolean = true,
    val error: String? = null
) {
    val hasActiveFilter: Boolean
        get() = selectedServiceId != null ||
            selectedFilter != TimeFilter.TODAY ||
            selectedCameraUuid != null ||
            selectedAiType != null
}

@OptIn(FlowPreview::class)
class EventViewModel(
    application: Application,
    private val eventRepository: EventRepository,
    private val webSocketManager: WebSocketManager
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(
        run {
            val isList = eventRepository.isListViewMode()
            val memCached = eventRepository.getEventDataInMemory(TimeFilter.TODAY, null, null, null)
            if (memCached != null) {
                val rawCameras = memCached.cameraList.ifEmpty { eventRepository.getCamerasInMemory() }
                val permittedCameras = UserPermissionManager.filterPermittedCameras(rawCameras)
                val permitted = memCached.events.permittedDistinct(rawCameras)
                EventScreenUiState(
                    isListView = isList,
                    isLoading = false,
                    events = permitted,
                    cameraList = permittedCameras,
                    cameraNameMap = buildCameraNameMap(rawCameras),
                    aiServices = memCached.aiServices.ifEmpty { eventRepository.getAiServicesInMemory() },
                    hasMore = permitted.isNotEmpty()
                )
            } else {
                val rawCameras = eventRepository.getCamerasInMemory()
                val permittedCameras = UserPermissionManager.filterPermittedCameras(rawCameras)
                EventScreenUiState(
                    isListView = isList,
                    isLoading = true,
                    cameraList = permittedCameras,
                    cameraNameMap = buildCameraNameMap(rawCameras),
                    aiServices = eventRepository.getAiServicesInMemory()
                )
            }
        }
    )
    val uiState: StateFlow<EventScreenUiState> = _uiState.asStateFlow()

    // Track last raw ID returned by API so pagination advances even past disabled AI event batches
    @Volatile private var lastFetchedRawId: Int? = null
    @Volatile private var lastFetchedRawIdByType: Map<String, Int?> = emptyMap()
    @Volatile private var exhaustedEventTypes: Set<String> = emptySet()
    @Volatile private var effectiveUnfilteredTypes: List<String> = emptyList()
    private var initialFetchJob: Job? = null
    private var paginateJob: Job? = null
    private val realtimeTriggerChannel = Channel<Boolean>(Channel.CONFLATED)
    @Volatile private var isEventTabActive: Boolean = false
    @Volatile private var pendingNotifyRefresh: Boolean = false
    @Volatile private var pendingNetworkRefresh: Boolean = false

    private fun EventScreenUiState.matchesFilter(
        filter: TimeFilter,
        serviceId: Int?,
        cameraUuid: String?,
        aiType: String?,
        customDateStartMs: Long,
        customDateEndMs: Long,
        customRangeFromMs: Long,
        customRangeToMs: Long
    ): Boolean {
        if (selectedFilter != filter ||
            selectedServiceId != serviceId ||
            selectedCameraUuid != cameraUuid ||
            selectedAiType != aiType
        ) {
            return false
        }
        return when (filter) {
            TimeFilter.CUSTOM_DATE ->
                this.customDateStartMs == customDateStartMs && this.customDateEndMs == customDateEndMs
            TimeFilter.CUSTOM_RANGE ->
                this.customRangeFromMs == customRangeFromMs && this.customRangeToMs == customRangeToMs
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
        fetchInitialData(reloadMetadata = true)
        listenToWebSocket()
    }

    fun setEventTabActive(active: Boolean) {
        if (isEventTabActive == active) return
        isEventTabActive = active
        if (!active) {
            return
        }
        if (pendingNetworkRefresh) {
            pendingNetworkRefresh = false
            pendingNotifyRefresh = false
            fetchInitialData(reloadMetadata = true)
        } else if (pendingNotifyRefresh && _uiState.value.selectedFilter == TimeFilter.TODAY) {
            pendingNotifyRefresh = false
            realtimeTriggerChannel.trySend(false)
        }
    }

    fun toggleViewMode() {
        val nextIsList = !_uiState.value.isListView
        eventRepository.setListViewMode(nextIsList)
        _uiState.update { it.copy(isListView = nextIsList) }
    }

    fun syncViewModeFromPrefs() {
        val savedIsList = eventRepository.isListViewMode()
        if (_uiState.value.isListView != savedIsList) {
            _uiState.update { it.copy(isListView = savedIsList) }
        }
    }

    fun onEventSelected(event: EventData) {
        val cur = _uiState.value
        eventRepository.cacheClickedEvent(
            event = event,
            cameras = cur.cameraList,
            aiServices = cur.aiServices
        )
    }

    private fun listenToWebSocket() {
        viewModelScope.launch(Dispatchers.IO) {
            for (mayLagDbCommit in realtimeTriggerChannel) {
                if (!isEventTabActive || _uiState.value.selectedFilter != TimeFilter.TODAY) continue
                processRealtimeRecentEvents(mayLagDbCommit = mayLagDbCommit)
                delay(REALTIME_MIN_INTERVAL_MS)
            }
        }

        viewModelScope.launch {
            webSocketManager.notifyEvent.collect { notify ->
                if (_uiState.value.selectedFilter == TimeFilter.TODAY) {
                    if (isEventTabActive) {
                        val mayLag = notify.event != WebSocketManager.TOPIC_APP_RESUMED
                        realtimeTriggerChannel.trySend(mayLag)
                    } else {
                        pendingNotifyRefresh = true
                    }
                }
            }
        }

        viewModelScope.launch {
            EventBus.eventReportedFalse.collect { eventId ->
                _uiState.update { state ->
                    state.copy(events = state.events.filter { it.id != eventId })
                }
            }
        }

        viewModelScope.launch {
            EventBus.networkRestored.collect {
                if (isEventTabActive) {
                    fetchInitialData(reloadMetadata = true)
                } else {
                    pendingNetworkRefresh = true
                }
            }
        }

        viewModelScope.launch {
            var lastVersion = UserPermissionManager.permissionState.value.version
            UserPermissionManager.permissionState.collect { permState ->
                if (permState.version != lastVersion) {
                    lastVersion = permState.version
                    if (UserSession.accessToken.isEmpty()) return@collect
                    val allCameras = eventRepository.getCamerasInMemory()
                    val permittedCameras = UserPermissionManager.filterPermittedCameras(
                        allCameras.ifEmpty { _uiState.value.cameraList }
                    )
                    val permittedUuids = permittedCameras.mapNotNull { it.extra?.uuid }.toSet()
                    val activeAiSensors = permState.activeAiModules.map { it.sensorType }.toSet()
                    val activeAiKeys = permState.activeAiModules.map { it.key }.toSet()

                    _uiState.update { cur ->
                        val nextSelectedCam = cur.selectedCameraUuid?.takeIf {
                            permittedUuids.isEmpty() || it in permittedUuids
                        }
                        val nextSelectedAi = cur.selectedAiType?.takeIf {
                            it in activeAiSensors || it in activeAiKeys || AiTypeHelper.isAiTypeEnabled(it)
                        }
                        cur.copy(
                            cameraList = permittedCameras,
                            selectedCameraUuid = nextSelectedCam,
                            selectedAiType = nextSelectedAi,
                            events = cur.events.permittedDistinct(allCameras.ifEmpty { cur.cameraList })
                        )
                    }
                    fetchInitialData(reloadMetadata = false)
                }
            }
        }
    }

    private data class EventBatchResult(
        val events: List<EventData>,
        val lastRawId: Int?,
        val lastRawIdByType: Map<String, Int?> = emptyMap(),
        val exhaustedTypes: Set<String> = emptySet(),
        val hasMore: Boolean,
        val reachedEnd: Boolean = true,
        val apiSucceeded: Boolean,
        val hadTransientError: Boolean = false,
        val firstError: Throwable? = null
    )

    private suspend fun resolveFallbackEventTypesForAllFilter(
        filter: TimeFilter,
        fromSec: Long,
        toSec: Long,
        serviceId: Int?,
        hitUnfilteredCeiling: Boolean
    ): List<String> {
        val cachedCounts = eventRepository.getCachedHomeEventsByType(filter, serviceId)
        val cachedActive = expandPermittedApiEventTypesFromCounts(cachedCounts)
        if (cachedActive.isNotEmpty()) {
            return cachedActive
        }

        val countResp = safeApiCall {
            eventRepository.getVmsEventCountByType(
                fromTime = fromSec * 1000L,
                toTime = toSec * 1000L + 999L,
                serviceId = serviceId?.toString()
            )
        }.getOrNull()
        if (countResp?.isSuccessful == true) {
            val body = countResp.body().orEmpty()
            val apiActive = expandPermittedApiEventTypesFromCounts(body)
            if (apiActive.isNotEmpty()) {
                return apiActive
            }
            if (body.isNotEmpty()) {
                return emptyList()
            }
        }

        return if (hitUnfilteredCeiling) {
            resolveAllPermittedApiEventTypes()
        } else {
            emptyList()
        }
    }

    private suspend fun fetchSingleTypeEventBatch(
        startLastId: Int?,
        fromSec: Long,
        toSec: Long,
        serviceId: Int?,
        cameraUuid: String?,
        resolvedType: String?,
        targetCount: Int = PAGE_TARGET_COUNT,
        allCameras: List<CameraModel> = eventRepository.getCamerasInMemory()
    ): EventBatchResult {
        if (!UserPermissionManager.canAccessEvents()) {
            return EventBatchResult(
                events = emptyList(),
                lastRawId = startLastId,
                hasMore = false,
                reachedEnd = true,
                apiSucceeded = true
            )
        }
        val validData = mutableListOf<EventData>()
        val seenKeys = mutableSetOf<String>()
        var currentLastId = startLastId
        var attempts = 0
        var reachedEnd = false
        var anySuccessful = false
        var hadTransientError = false
        var firstError: Throwable? = null

        val canUseFastFirstBatch = targetCount <= PAGE_TARGET_COUNT &&
            !UserPermissionManager.isCustomCameraPermission() &&
            (resolvedType != null || (!UserPermissionManager.isCustomAiEventPermission() && !AiTypeHelper.isProjectModuleRestricted))

        while (attempts < MAX_BATCH_ATTEMPTS) {
            attempts++
            val requestBatchSize = if (attempts == 1 && canUseFastFirstBatch) FIRST_FAST_BATCH_SIZE else RAW_BATCH_SIZE
            val result = safeApiCall {
                eventRepository.getDataList(
                    lastId = currentLastId,
                    count = requestBatchSize,
                    from = fromSec,
                    to = toSec,
                    serviceId = serviceId,
                    cameraUUID = cameraUuid,
                    type = resolvedType
                )
            }
            val response = result.getOrNull()
            if (response == null) {
                if (firstError == null) firstError = result.exceptionOrNull()
                hadTransientError = true
                break
            }
            anySuccessful = true

            val batchData = response.body() ?: emptyList()
            if (batchData.isEmpty()) {
                reachedEnd = true
                break
            }

            val camerasForPerm = allCameras.ifEmpty { eventRepository.getCamerasInMemory() }
            val inRangeBatch = batchData.filter { it.isWithinTimeRangeSeconds(fromSec, toSec) }
            val permittedBatch = UserPermissionManager.filterEventsByPermissions(inRangeBatch, camerasForPerm)
            for (ev in permittedBatch) {
                if (seenKeys.add(ev.uniqueKey())) {
                    validData.add(ev)
                }
            }

            val allOlderThanFrom = batchData.all { it.time > 0.0 && eventTimeToSeconds(it.time) < fromSec }
            if (allOlderThanFrom) {
                reachedEnd = true
                break
            }

            val lastItem = batchData.lastOrNull()
            if (lastItem == null || lastItem.id <= 0 || lastItem.id == currentLastId) {
                reachedEnd = true
                break
            }
            currentLastId = lastItem.id

            if (batchData.size < requestBatchSize) {
                reachedEnd = true
                break
            }
            if (validData.size >= targetCount) {
                break
            }
        }

        // Nếu sau 5 lượt (250 bản ghi thô) vẫn không có sự kiện hợp lệ nào thì dừng tự động phân trang để tránh vòng lặp vô hạn;
        // tuy nhiên nếu gặp lỗi mạng tạm thời ở lượt #2+ sau khi lượt #1 đã thành công thì giữ hasMore = true để người dùng cuộn thử lại.
        val hasMore = !reachedEnd && (validData.isNotEmpty() || (anySuccessful && hadTransientError))
        return EventBatchResult(
            events = validData,
            lastRawId = currentLastId,
            hasMore = hasMore,
            reachedEnd = reachedEnd,
            apiSucceeded = anySuccessful,
            hadTransientError = hadTransientError,
            firstError = if (anySuccessful) null else firstError
        )
    }

    /**
     * Khớp hoàn toàn với fetchEvents() / fetchDataList() trên Web:
     * - Khi lọc 1 bài AI có nhiều `eventTypes` (như Không đội mũ bảo hiểm, Cháy & Khói, Khuôn mặt...),
     *   truy vấn song song toàn bộ các `type` của bài AI đó, gộp loại trùng và sắp xếp giảm dần theo thời gian.
     * - Duy trì con trỏ `lastId` riêng cho từng `type` khi phân trang cuộn tiếp.
     */
    private suspend fun fetchPermittedEventBatch(
        startLastId: Int?,
        fromSec: Long,
        toSec: Long,
        serviceId: Int?,
        cameraUuid: String?,
        resolvedTypes: List<String>,
        startLastIdByType: Map<String, Int?> = emptyMap(),
        currentExhaustedTypes: Set<String> = emptySet(),
        isLoadMore: Boolean = false,
        targetCount: Int = PAGE_TARGET_COUNT,
        allCameras: List<CameraModel> = eventRepository.getCamerasInMemory()
    ): EventBatchResult = coroutineScope {
        if (!UserPermissionManager.canAccessEvents()) {
            return@coroutineScope EventBatchResult(
                events = emptyList(),
                lastRawId = startLastId,
                hasMore = false,
                reachedEnd = true,
                apiSucceeded = true
            )
        }
        if (resolvedTypes.size <= 1) {
            return@coroutineScope fetchSingleTypeEventBatch(
                startLastId = startLastId,
                fromSec = fromSec,
                toSec = toSec,
                serviceId = serviceId,
                cameraUuid = cameraUuid,
                resolvedType = resolvedTypes.firstOrNull(),
                targetCount = targetCount,
                allCameras = allCameras
            )
        }

        val typesToQuery = if (isLoadMore) {
            resolvedTypes.filter { it !in currentExhaustedTypes }
        } else {
            resolvedTypes
        }
        if (typesToQuery.isEmpty()) {
            return@coroutineScope EventBatchResult(
                events = emptyList(),
                lastRawId = null,
                lastRawIdByType = startLastIdByType,
                exhaustedTypes = currentExhaustedTypes,
                hasMore = false,
                reachedEnd = true,
                apiSucceeded = true
            )
        }

        val semaphore = Semaphore(MAX_TYPED_QUERY_CONCURRENCY)
        val subResults = typesToQuery.map { type ->
            async {
                semaphore.withPermit {
                    type to fetchSingleTypeEventBatch(
                        startLastId = if (isLoadMore) startLastIdByType[type] else null,
                        fromSec = fromSec,
                        toSec = toSec,
                        serviceId = serviceId,
                        cameraUuid = cameraUuid,
                        resolvedType = type,
                        targetCount = targetCount,
                        allCameras = allCameras
                    )
                }
            }
        }.awaitAll()

        val nextCursors = LinkedHashMap<String, Int?>(startLastIdByType)
        val nextExhausted = HashSet<String>(if (isLoadMore) currentExhaustedTypes else emptySet())
        var anySucceeded = false
        var anyTransientError = false
        var firstError: Throwable? = null
        val combined = ArrayList<EventData>()

        for ((type, sub) in subResults) {
            if (sub.hadTransientError) {
                anyTransientError = true
            }
            if (sub.apiSucceeded) {
                anySucceeded = true
                nextCursors[type] = sub.lastRawId
                if (!sub.hasMore) {
                    nextExhausted.add(type)
                } else {
                    nextExhausted.remove(type)
                }
            } else if (firstError == null && sub.firstError != null) {
                firstError = sub.firstError
            }
            combined.addAll(sub.events)
        }

        val mergedEvents = combined
            .distinctBy { it.uniqueKey() }
            .sortedWith(EventDescendingComparator)
        val hasMore = resolvedTypes.any { it !in nextExhausted } && (mergedEvents.isNotEmpty() || anyTransientError)

        EventBatchResult(
            events = mergedEvents,
            lastRawId = mergedEvents.lastOrNull()?.id,
            lastRawIdByType = nextCursors,
            exhaustedTypes = nextExhausted,
            hasMore = hasMore,
            reachedEnd = !hasMore && !anyTransientError,
            apiSucceeded = anySucceeded,
            hadTransientError = anyTransientError,
            firstError = if (anySucceeded) null else firstError
        )
    }

    private suspend fun queryRealtimeRawEvents(
        from: Long,
        to: Long,
        serviceId: Int?,
        cameraUuid: String?,
        resolvedTypes: List<String>,
        includeUnfilteredHeadProbe: Boolean = false
    ): Pair<Boolean, List<EventData>> {
        return if (resolvedTypes.size <= 1 && !includeUnfilteredHeadProbe) {
            val response = safeApiCall {
                eventRepository.getDataList(
                    count = PAGE_TARGET_COUNT,
                    from = from,
                    to = to,
                    serviceId = serviceId,
                    cameraUUID = cameraUuid,
                    type = resolvedTypes.firstOrNull()
                )
            }.getOrNull()
            val ok = response?.isSuccessful == true
            ok to (if (ok) response?.body().orEmpty() else emptyList())
        } else {
            coroutineScope {
                val semaphore = Semaphore(MAX_TYPED_QUERY_CONCURRENCY)
                val queryTypes: List<String?> = if (includeUnfilteredHeadProbe) {
                    listOf<String?>(null) + resolvedTypes
                } else {
                    resolvedTypes
                }
                val responses = queryTypes.map { t ->
                    async {
                        semaphore.withPermit {
                            safeApiCall {
                                eventRepository.getDataList(
                                    count = PAGE_TARGET_COUNT,
                                    from = from,
                                    to = to,
                                    serviceId = serviceId,
                                    cameraUUID = cameraUuid,
                                    type = t
                                )
                            }.getOrNull()
                        }
                    }
                }.awaitAll()
                val ok = responses.any { it?.isSuccessful == true }
                val items = responses
                    .filter { it?.isSuccessful == true }
                    .flatMap { it?.body().orEmpty() }
                    .sortedWith(EventDescendingComparator)
                ok to items
            }
        }
    }

    private suspend fun processRealtimeRecentEvents(mayLagDbCommit: Boolean) {
        try {
            if (!UserPermissionManager.canAccessEvents()) return
            val state = _uiState.value
            val filter = state.selectedFilter
            if (filter != TimeFilter.TODAY) return
            val serviceId = state.selectedServiceId
            val cameraUuid = state.selectedCameraUuid
            val aiType = state.selectedAiType
            val customDateStartMs = state.customDateStartMs
            val customDateEndMs = state.customDateEndMs
            val customRangeFromMs = state.customRangeFromMs
            val customRangeToMs = state.customRangeToMs
            val (from, to) = getTimeRange(
                filter = filter,
                customDateStartMs = customDateStartMs,
                customDateEndMs = customDateEndMs,
                customRangeFromMs = customRangeFromMs,
                customRangeToMs = customRangeToMs
            )
            val fallbackTypes = effectiveUnfilteredTypes
            val isUsingUnfilteredFallback = aiType.isNullOrBlank() && fallbackTypes.isNotEmpty()
            val resolvedTypes = resolveApiEventTypes(aiType).ifEmpty { fallbackTypes }
            val prevMaxId = state.events.maxOfOrNull { it.id } ?: 0

            var (anySuccessful, rawEvents) = queryRealtimeRawEvents(
                from = from,
                to = to,
                serviceId = serviceId,
                cameraUuid = cameraUuid,
                resolvedTypes = resolvedTypes,
                includeUnfilteredHeadProbe = isUsingUnfilteredFallback
            )
            val firstMaxId = rawEvents.maxOfOrNull { it.id } ?: 0
            if (mayLagDbCommit && firstMaxId <= prevMaxId) {
                delay(REALTIME_DB_COMMIT_RETRY_MS)
                if (!isEventTabActive ||
                    !_uiState.value.matchesFilter(
                        filter = filter,
                        serviceId = serviceId,
                        cameraUuid = cameraUuid,
                        aiType = aiType,
                        customDateStartMs = customDateStartMs,
                        customDateEndMs = customDateEndMs,
                        customRangeFromMs = customRangeFromMs,
                        customRangeToMs = customRangeToMs
                    )
                ) {
                    return
                }
                val retry = queryRealtimeRawEvents(
                    from = from,
                    to = to,
                    serviceId = serviceId,
                    cameraUuid = cameraUuid,
                    resolvedTypes = resolvedTypes,
                    includeUnfilteredHeadProbe = isUsingUnfilteredFallback
                )
                if (retry.first) {
                    anySuccessful = true
                    rawEvents = retry.second
                }
            }

            if (anySuccessful) {
                val allCameras = eventRepository.getCamerasInMemory().ifEmpty { state.cameraList }
                val permittedNew = UserPermissionManager.filterEventsByPermissions(
                    events = rawEvents.filter { it.isWithinTimeRangeSeconds(from, to) },
                    allCameras = allCameras
                )
                if (permittedNew.isNotEmpty()) {
                    var didUpdateState = false
                    var updatedList: List<EventData> = emptyList()
                    var committedCameras: List<CameraModel> = emptyList()
                    var committedAiServices: List<AIServiceModel> = emptyList()

                    _uiState.update { s ->
                        if (!s.matchesFilter(
                                filter = filter,
                                serviceId = serviceId,
                                cameraUuid = cameraUuid,
                                aiType = aiType,
                                customDateStartMs = customDateStartMs,
                                customDateEndMs = customDateEndMs,
                                customRangeFromMs = customRangeFromMs,
                                customRangeToMs = customRangeToMs
                            )
                        ) {
                            return@update s
                        }
                        val mergedAll = (permittedNew + s.events.filter { it.isWithinTimeRangeSeconds(from, to) })
                            .distinctBy { it.uniqueKey() }
                            .sortedWith(EventDescendingComparator)
                        val maxCap = maxOf(MAX_REALTIME_EVENTS, s.events.size)
                        val merged = if (mergedAll.size > maxCap) mergedAll.take(maxCap) else mergedAll
                        val isIdentical = merged.size == s.events.size &&
                            merged.indices.all { idx ->
                                merged[idx].id == s.events[idx].id && merged[idx].time == s.events[idx].time
                            }
                        if (isIdentical) {
                            return@update s
                        }
                        didUpdateState = true
                        updatedList = merged
                        committedCameras = s.cameraList
                        committedAiServices = s.aiServices
                        s.copy(events = merged)
                    }

                    if (didUpdateState) {
                        eventRepository.cacheInMemory(events = permittedNew)
                        eventRepository.saveEventData(
                            filter = filter,
                            serviceId = serviceId,
                            aiType = aiType,
                            data = CachedEventData(
                                events = updatedList.take(RAW_BATCH_SIZE),
                                cameraList = eventRepository.getCamerasInMemory().ifEmpty { committedCameras },
                                aiServices = committedAiServices,
                                updatedAt = System.currentTimeMillis()
                            ),
                            cameraUuid = cameraUuid
                        )
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching recent events via WebSocket", e)
        }
    }

    private data class ExternalFilterRestoreState(
        val filter: TimeFilter,
        val serviceId: Int?,
        val cameraUuid: String?,
        val aiType: String?,
        val customDateStartMs: Long,
        val customDateEndMs: Long,
        val customRangeFromMs: Long,
        val customRangeToMs: Long
    )

    /** Lưu lại bộ lọc gốc trước khi điều hướng tạm thời từ Trang chủ (vd. bấm vào 1 bài AI trong Phân bố AI). */
    private var savedFilterBeforeExternalNav: ExternalFilterRestoreState? = null

    fun applyFilters(
        filter: TimeFilter,
        serviceId: Int?,
        aiType: String?,
        cameraUuid: String? = _uiState.value.selectedCameraUuid,
        customDateStartMs: Long? = null,
        customDateEndMs: Long? = null,
        customRangeFromMs: Long? = null,
        customRangeToMs: Long? = null
    ) {
        // Người dùng chủ động chọn lọc trên màn hình Sự kiện → giữ làm bộ lọc chính thức, huỷ cờ lọc tạm thời
        savedFilterBeforeExternalNav = null
        applyFiltersInternal(
            filter = filter,
            serviceId = serviceId,
            aiType = aiType,
            cameraUuid = cameraUuid,
            customDateStartMs = customDateStartMs,
            customDateEndMs = customDateEndMs,
            customRangeFromMs = customRangeFromMs,
            customRangeToMs = customRangeToMs
        )
    }

    private fun applyFiltersInternal(
        filter: TimeFilter,
        serviceId: Int?,
        aiType: String?,
        cameraUuid: String?,
        customDateStartMs: Long? = null,
        customDateEndMs: Long? = null,
        customRangeFromMs: Long? = null,
        customRangeToMs: Long? = null
    ) {
        val current = _uiState.value
        val resolvedDateStart = when {
            customDateStartMs != null -> customDateStartMs
            filter == TimeFilter.CUSTOM_DATE -> TimeFilter.customDateStartMs
            else -> current.customDateStartMs
        }
        val resolvedDateEnd = when {
            customDateEndMs != null -> customDateEndMs
            filter == TimeFilter.CUSTOM_DATE -> TimeFilter.customDateEndMs
            else -> current.customDateEndMs
        }
        val resolvedRangeFrom = when {
            customRangeFromMs != null -> customRangeFromMs
            filter == TimeFilter.CUSTOM_RANGE -> TimeFilter.customRangeFromMs
            else -> current.customRangeFromMs
        }
        val resolvedRangeTo = when {
            customRangeToMs != null -> customRangeToMs
            filter == TimeFilter.CUSTOM_RANGE -> TimeFilter.customRangeToMs
            else -> current.customRangeToMs
        }
        if (current.matchesFilter(
                filter = filter,
                serviceId = serviceId,
                cameraUuid = cameraUuid,
                aiType = aiType,
                customDateStartMs = resolvedDateStart,
                customDateEndMs = resolvedDateEnd,
                customRangeFromMs = resolvedRangeFrom,
                customRangeToMs = resolvedRangeTo
            )
        ) return
        pendingNotifyRefresh = false
        pendingNetworkRefresh = false
        _uiState.update {
            it.copy(
                selectedFilter = filter,
                selectedServiceId = serviceId,
                selectedCameraUuid = cameraUuid,
                selectedAiType = aiType,
                customDateStartMs = resolvedDateStart,
                customDateEndMs = resolvedDateEnd,
                customRangeFromMs = resolvedRangeFrom,
                customRangeToMs = resolvedRangeTo,
                isRefreshing = false
            )
        }
        // Đổi bộ lọc chỉ cần tải lại sự kiện; danh sách camera / AI service đã có thì dùng lại
        fetchInitialData(reloadMetadata = false)
    }

    fun resetFilters() {
        savedFilterBeforeExternalNav = null
        applyFiltersInternal(
            filter = TimeFilter.TODAY,
            serviceId = null,
            aiType = null,
            cameraUuid = null
        )
    }

    fun applyExternalNavigationFilter(
        timeFilter: TimeFilter,
        serviceId: Int?,
        aiType: String?
    ) {
        if (savedFilterBeforeExternalNav == null) {
            val current = _uiState.value
            savedFilterBeforeExternalNav = ExternalFilterRestoreState(
                filter = current.selectedFilter,
                serviceId = current.selectedServiceId,
                cameraUuid = current.selectedCameraUuid,
                aiType = current.selectedAiType,
                customDateStartMs = current.customDateStartMs,
                customDateEndMs = current.customDateEndMs,
                customRangeFromMs = current.customRangeFromMs,
                customRangeToMs = current.customRangeToMs
            )
        }
        applyFiltersInternal(
            filter = timeFilter,
            serviceId = serviceId,
            aiType = aiType,
            cameraUuid = null
        )
    }

    /**
     * Khôi phục lại bộ lọc trước đó khi người dùng rời khỏi tab Sự kiện sau khi xem nhanh từ Trang chủ.
     * @return `true` nếu có bộ lọc tạm thời vừa được khôi phục lại.
     */
    fun clearTemporaryExternalFilter(): Boolean {
        val prev = savedFilterBeforeExternalNav ?: return false
        savedFilterBeforeExternalNav = null
        val current = _uiState.value
        if (current.matchesFilter(
                filter = prev.filter,
                serviceId = prev.serviceId,
                cameraUuid = prev.cameraUuid,
                aiType = prev.aiType,
                customDateStartMs = prev.customDateStartMs,
                customDateEndMs = prev.customDateEndMs,
                customRangeFromMs = prev.customRangeFromMs,
                customRangeToMs = prev.customRangeToMs
            )
        ) {
            return false
        }
        applyFiltersInternal(
            filter = prev.filter,
            serviceId = prev.serviceId,
            aiType = prev.aiType,
            cameraUuid = prev.cameraUuid,
            customDateStartMs = prev.customDateStartMs,
            customDateEndMs = prev.customDateEndMs,
            customRangeFromMs = prev.customRangeFromMs,
            customRangeToMs = prev.customRangeToMs
        )
        return true
    }

    /** Kéo để làm mới: tải lại cả sự kiện lẫn danh sách camera / AI service. */
    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        fetchInitialData(reloadMetadata = true, isUserRefresh = true)
    }

    private fun getTimeRange(
        filter: TimeFilter,
        customDateStartMs: Long = TimeFilter.customDateStartMs,
        customDateEndMs: Long = TimeFilter.customDateEndMs,
        customRangeFromMs: Long = TimeFilter.customRangeFromMs,
        customRangeToMs: Long = TimeFilter.customRangeToMs
    ): Pair<Long, Long> {
        val (fromTime, toTime) = TimeFilter.getDateRange(
            filter = filter,
            customDateStartMs = customDateStartMs,
            customDateEndMs = customDateEndMs,
            customRangeFromMs = customRangeFromMs,
            customRangeToMs = customRangeToMs
        )
        return Pair(fromTime / 1000, toTime / 1000)
    }

    /**
     * @param reloadMetadata `true` để tải lại danh sách camera / AI service từ API;
     * `false` thì chỉ tải khi trong state chưa có dữ liệu.
     */
    fun fetchInitialData(reloadMetadata: Boolean = true, isUserRefresh: Boolean = false) {
        initialFetchJob?.cancel()
        paginateJob?.cancel()
        pendingNetworkRefresh = false
        if (UserSession.accessToken.isEmpty() || UserSession.domain.isEmpty()) return
        initialFetchJob = viewModelScope.launch(Dispatchers.IO) {
            lastFetchedRawId = null
            lastFetchedRawIdByType = emptyMap()
            exhaustedEventTypes = emptySet()
            effectiveUnfilteredTypes = emptyList()
            _uiState.update { it.copy(isPaginating = false) }
            val startState = _uiState.value
            val filter = startState.selectedFilter
            val serviceId = startState.selectedServiceId
            val cameraUuid = startState.selectedCameraUuid
            val aiType = startState.selectedAiType
            val customDateStartMs = startState.customDateStartMs
            val customDateEndMs = startState.customDateEndMs
            val customRangeFromMs = startState.customRangeFromMs
            val customRangeToMs = startState.customRangeToMs

            val (from, to) = getTimeRange(
                filter = filter,
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

            // 1. Ưu tiên hiển thị ngay dữ liệu từ RAM / Disk Cache (Stale-While-Revalidate) nếu có bản ghi hợp lệ
            val cachedData = if (canUseRepoCache) {
                eventRepository.getEventData(
                    filter = filter,
                    serviceId = serviceId,
                    aiType = aiType,
                    cameraUuid = cameraUuid
                )
            } else {
                null
            }
            if (cachedData != null && cachedData.events.isNotEmpty() && !isUserRefresh) {
                val rawCachedCameras = cachedData.cameraList.ifEmpty {
                    eventRepository.getCamerasInMemory().ifEmpty { _uiState.value.cameraList }
                }
                val permittedCached = withContext(Dispatchers.Default) {
                    cachedData.events
                        .filter { it.isWithinTimeRangeSeconds(from, to) }
                        .permittedDistinct(rawCachedCameras)
                }
                if (permittedCached.isNotEmpty()) {
                    lastFetchedRawId = permittedCached.lastOrNull()?.id
                    lastFetchedRawIdByType = permittedCached
                        .groupBy { it.type }
                        .mapValues { (_, list) -> list.minOfOrNull { it.id } }
                    _uiState.update {
                        val permittedCameras = UserPermissionManager.filterPermittedCameras(rawCachedCameras)
                        val services = cachedData.aiServices.ifEmpty {
                            it.aiServices.ifEmpty { eventRepository.getAiServicesInMemory() }
                        }
                        it.copy(
                            isLoading = false,
                            error = null,
                            cameraList = permittedCameras,
                            cameraNameMap = buildCameraNameMap(rawCachedCameras),
                            aiServices = services,
                            events = permittedCached,
                            hasMore = true
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = true,
                            error = null,
                            events = emptyList(),
                            hasMore = true
                        )
                    }
                }
            } else if (!isUserRefresh) {
                _uiState.update {
                    it.copy(
                        isLoading = true,
                        error = null,
                        events = emptyList(),
                        hasMore = true
                    )
                }
            }

            val fetchStartedMaxId = _uiState.value.events.maxOfOrNull { it.id } ?: 0

            try {
                coroutineScope {
                    val hasWarmCameras = eventRepository.getCamerasInMemory().isNotEmpty()
                    val hasWarmAiServices = _uiState.value.aiServices.isNotEmpty() ||
                        eventRepository.getAiServicesInMemory().isNotEmpty()
                    val hasFreshMetadata = !isUserRefresh && eventRepository.hasFreshSessionMetadata()
                    val needCameras = (reloadMetadata && !hasFreshMetadata) || !hasWarmCameras
                    val needAiServices = (reloadMetadata && !hasFreshMetadata) || !hasWarmAiServices
                    val cameraDeferred = async {
                        if (needCameras) safeApiCall { eventRepository.getCameraList() } else null
                    }
                    val aiServiceDeferred = async {
                        if (needAiServices) safeApiCall { eventRepository.getAIServiceList() } else null
                    }

                    // Nếu tài khoản có phân quyền camera custom và RAM chưa có danh sách camera,
                    // đợi cameraDeferred xong trước khi lọc sự kiện để có map UUID đầy đủ ngay lượt đầu.
                    if (UserPermissionManager.isCustomCameraPermission() && !hasWarmCameras) {
                        val preCamResp = cameraDeferred.await()?.getOrNull()
                        if (preCamResp?.isSuccessful == true) {
                            val preCamList = preCamResp.body() ?: emptyList()
                            if (preCamList.isNotEmpty()) {
                                eventRepository.cacheInMemory(cameras = preCamList, fromNetwork = true)
                            }
                        }
                    }

                    val eventsDeferred = async {
                        fetchPermittedEventBatch(
                            startLastId = null,
                            fromSec = from,
                            toSec = to,
                            serviceId = serviceId,
                            cameraUuid = cameraUuid,
                            resolvedTypes = resolveApiEventTypes(aiType),
                            allCameras = eventRepository.getCamerasInMemory()
                        )
                    }

                    // --- GIAI ĐOẠN 1: Trả ngay danh sách sự kiện & tắt vòng xoay refresh mà không chờ tải lại toàn bộ CameraList/AIServiceList ---
                    var batchResult = eventsDeferred.await()
                    val hitUnfilteredCeiling = batchResult.apiSucceeded &&
                        !batchResult.reachedEnd &&
                        !batchResult.hadTransientError &&
                        batchResult.events.size < PAGE_TARGET_COUNT
                    if (aiType.isNullOrBlank() && (hitUnfilteredCeiling || (batchResult.apiSucceeded && batchResult.events.isEmpty()))) {
                        val fallbackTypes = resolveFallbackEventTypesForAllFilter(
                            filter = filter,
                            fromSec = from,
                            toSec = to,
                            serviceId = serviceId,
                            hitUnfilteredCeiling = hitUnfilteredCeiling
                        )
                        if (fallbackTypes.isNotEmpty()) {
                            effectiveUnfilteredTypes = fallbackTypes
                            val typedBatch = fetchPermittedEventBatch(
                                startLastId = null,
                                fromSec = from,
                                toSec = to,
                                serviceId = serviceId,
                                cameraUuid = cameraUuid,
                                resolvedTypes = fallbackTypes,
                                allCameras = eventRepository.getCamerasInMemory()
                            )
                            if (typedBatch.apiSucceeded || typedBatch.events.isNotEmpty()) {
                                val mergedEvents = (batchResult.events + typedBatch.events)
                                    .distinctBy { it.uniqueKey() }
                                    .sortedWith(EventDescendingComparator)
                                batchResult = typedBatch.copy(
                                    events = mergedEvents,
                                    lastRawId = if (fallbackTypes.size <= 1) typedBatch.lastRawId else mergedEvents.lastOrNull()?.id
                                )
                            }
                        }
                    }
                    val earlyCameraResult = if (!hasWarmCameras || cameraDeferred.isCompleted) cameraDeferred.await() else null
                    val earlyAiServiceResult = if (!hasWarmAiServices || aiServiceDeferred.isCompleted) aiServiceDeferred.await() else null

                    val earlyCameraResp = earlyCameraResult?.getOrNull()
                    val earlyAiServiceResp = earlyAiServiceResult?.getOrNull()
                    val newEvents = batchResult.events
                    val nextLastId = batchResult.lastRawId
                    val hasMore = batchResult.hasMore

                    val currentState = _uiState.value
                    val stage1AllCameras = when {
                        earlyCameraResp?.isSuccessful == true -> earlyCameraResp.body() ?: emptyList()
                        eventRepository.getCamerasInMemory().isNotEmpty() -> eventRepository.getCamerasInMemory()
                        else -> currentState.cameraList
                    }
                    val stage1PermittedCameras = UserPermissionManager.filterPermittedCameras(stage1AllCameras)
                    val stage1AiServices = when {
                        earlyAiServiceResp?.isSuccessful == true -> earlyAiServiceResp.body() ?: emptyList()
                        currentState.aiServices.isNotEmpty() -> currentState.aiServices
                        else -> eventRepository.getAiServicesInMemory()
                    }

                    val fetchedMaxId = newEvents.maxOfOrNull { it.id } ?: 0
                    if (batchResult.apiSucceeded || newEvents.isNotEmpty() || cachedData == null) {
                        lastFetchedRawId = nextLastId
                        lastFetchedRawIdByType = batchResult.lastRawIdByType
                        exhaustedEventTypes = batchResult.exhaustedTypes
                    }

                    val stage1Failure = batchResult.firstError
                        ?: earlyCameraResult?.exceptionOrNull()
                        ?: earlyAiServiceResult?.exceptionOrNull()

                    if (UserSession.accessToken.isEmpty()) return@coroutineScope

                    val (stage1CameraNameMap, permittedNewEvents) = withContext(Dispatchers.Default) {
                        val nameMap = buildCameraNameMap(stage1AllCameras)
                        val permitted = newEvents
                            .filter { it.isWithinTimeRangeSeconds(from, to) }
                            .permittedDistinct(stage1AllCameras)
                        nameMap to permitted
                    }

                    var stage1Matched = false
                    _uiState.update { cur ->
                        if (!cur.matchesFilter(
                                filter = filter,
                                serviceId = serviceId,
                                cameraUuid = cameraUuid,
                                aiType = aiType,
                                customDateStartMs = customDateStartMs,
                                customDateEndMs = customDateEndMs,
                                customRangeFromMs = customRangeFromMs,
                                customRangeToMs = customRangeToMs
                            )
                        ) {
                            return@update cur
                        }
                        stage1Matched = true
                        val stage1Events = when {
                            batchResult.apiSucceeded && (isUserRefresh || fetchedMaxId == 0) -> {
                                permittedNewEvents
                            }
                            batchResult.apiSucceeded -> {
                                val cutoffId = maxOf(fetchedMaxId, fetchStartedMaxId)
                                val newerRealtime = cur.events.filter {
                                    it.id > cutoffId && it.isWithinTimeRangeSeconds(from, to)
                                }
                                if (newerRealtime.isEmpty()) {
                                    permittedNewEvents
                                } else {
                                    (newerRealtime + permittedNewEvents)
                                        .distinctBy { it.uniqueKey() }
                                        .sortedWith(EventDescendingComparator)
                                }
                            }
                            newEvents.isNotEmpty() -> {
                                (cur.events.filter { it.isWithinTimeRangeSeconds(from, to) } + permittedNewEvents)
                                    .permittedDistinct(stage1AllCameras)
                                    .sortedWith(EventDescendingComparator)
                            }
                            else -> cur.events
                        }
                        cur.copy(
                            isLoading = false,
                            isRefreshing = false,
                            cameraList = stage1PermittedCameras,
                            cameraNameMap = stage1CameraNameMap,
                            aiServices = stage1AiServices,
                            events = stage1Events,
                            hasMore = hasMore,
                            error = stage1Failure?.toUserFriendlyMessage()?.takeIf { msg -> msg.isNotEmpty() }
                        )
                    }
                    if (!stage1Matched) return@coroutineScope

                    // --- GIAI ĐOẠN 2 (chạy nền): Cập nhật bổ sung Camera / AI Services nếu đang làm mới ---
                    val cameraResult = earlyCameraResult ?: cameraDeferred.await()
                    val aiServiceResult = earlyAiServiceResult ?: aiServiceDeferred.await()
                    val cameraResponse = cameraResult?.getOrNull()
                    val aiServiceResponse = aiServiceResult?.getOrNull()

                    val allCameraList = when {
                        cameraResponse?.isSuccessful == true -> cameraResponse.body() ?: emptyList()
                        eventRepository.getCamerasInMemory().isNotEmpty() -> eventRepository.getCamerasInMemory()
                        else -> _uiState.value.cameraList
                    }
                    val permittedCameraList = UserPermissionManager.filterPermittedCameras(allCameraList)
                    val aiServices = when {
                        aiServiceResponse?.isSuccessful == true -> aiServiceResponse.body() ?: emptyList()
                        _uiState.value.aiServices.isNotEmpty() -> _uiState.value.aiServices
                        else -> eventRepository.getAiServicesInMemory()
                    }

                    if (UserSession.accessToken.isEmpty()) return@coroutineScope

                    val stage2SnapEvents = _uiState.value.events
                    val (cameraNameMap, preFilteredEvents) = withContext(Dispatchers.Default) {
                        val nameMap = if (cameraResponse?.isSuccessful == true || aiServiceResponse?.isSuccessful == true) {
                            buildCameraNameMap(allCameraList)
                        } else {
                            null
                        }
                        val cleaned = stage2SnapEvents
                            .filter { it.isWithinTimeRangeSeconds(from, to) }
                            .permittedDistinct(allCameraList)
                        nameMap to cleaned
                    }

                    var committedStage2Events: List<EventData>? = null
                    if (cameraResponse?.isSuccessful == true || aiServiceResponse?.isSuccessful == true) {
                        _uiState.update { cur ->
                            if (!cur.matchesFilter(
                                    filter = filter,
                                    serviceId = serviceId,
                                    cameraUuid = cameraUuid,
                                    aiType = aiType,
                                    customDateStartMs = customDateStartMs,
                                    customDateEndMs = customDateEndMs,
                                    customRangeFromMs = customRangeFromMs,
                                    customRangeToMs = customRangeToMs
                                )
                            ) {
                                return@update cur
                            }
                            val cleaned = if (cur.events === stage2SnapEvents) {
                                preFilteredEvents
                            } else {
                                cur.events
                                    .filter { it.isWithinTimeRangeSeconds(from, to) }
                                    .permittedDistinct(allCameraList)
                            }
                            committedStage2Events = cleaned
                            cur.copy(
                                cameraList = permittedCameraList,
                                cameraNameMap = cameraNameMap ?: cur.cameraNameMap,
                                aiServices = aiServices,
                                events = if (cleaned == cur.events) cur.events else cleaned
                            )
                        }
                    } else {
                        val latest = _uiState.value
                        if (latest.matchesFilter(
                                filter = filter,
                                serviceId = serviceId,
                                cameraUuid = cameraUuid,
                                aiType = aiType,
                                customDateStartMs = customDateStartMs,
                                customDateEndMs = customDateEndMs,
                                customRangeFromMs = customRangeFromMs,
                                customRangeToMs = customRangeToMs
                            )
                        ) {
                            committedStage2Events = if (latest.events === stage2SnapEvents) {
                                preFilteredEvents
                            } else {
                                latest.events
                                    .filter { it.isWithinTimeRangeSeconds(from, to) }
                                    .permittedDistinct(allCameraList)
                            }
                        }
                    }

                    val finalEvents = committedStage2Events ?: return@coroutineScope
                    eventRepository.cacheInMemory(
                        events = finalEvents,
                        cameras = allCameraList,
                        aiServices = aiServices,
                        fromNetwork = cameraResponse?.isSuccessful == true
                    )

                    if (canUseRepoCache && (batchResult.apiSucceeded || newEvents.isNotEmpty())) {
                        eventRepository.saveEventData(
                            filter = filter,
                            serviceId = serviceId,
                            aiType = aiType,
                            data = CachedEventData(
                                events = finalEvents.take(RAW_BATCH_SIZE),
                                cameraList = allCameraList,
                                aiServices = aiServices,
                                updatedAt = System.currentTimeMillis()
                            ),
                            cameraUuid = cameraUuid
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching initial events", e)
                if (UserSession.accessToken.isEmpty()) return@launch
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        hasMore = false,
                        error = e.toUserFriendlyMessage().takeIf { msg -> msg.isNotEmpty() }
                    )
                }
            }
        }
    }

    fun loadMoreEvents() {
        if (initialFetchJob?.isActive == true) return
        val currentState = _uiState.value
        if (currentState.isLoading || currentState.isPaginating || !currentState.hasMore || currentState.events.isEmpty()) return

        paginateJob?.cancel()
        paginateJob = viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isPaginating = true, error = null) }
            try {
                var resolvedTypes = resolveApiEventTypes(currentState.selectedAiType)
                    .ifEmpty { effectiveUnfilteredTypes }
                val startId: Int? = if (resolvedTypes.size <= 1) {
                    val sid = lastFetchedRawId ?: currentState.events.lastOrNull()?.id
                    if (sid == null) {
                        _uiState.update { it.copy(isPaginating = false, hasMore = false) }
                        return@launch
                    }
                    sid
                } else {
                    if (resolvedTypes.all { it in exhaustedEventTypes }) {
                        _uiState.update { it.copy(isPaginating = false, hasMore = false) }
                        return@launch
                    }
                    null
                }
                val fallbackCursors = if (resolvedTypes.size > 1 && lastFetchedRawIdByType.isEmpty()) {
                    currentState.events
                        .groupBy { it.type }
                        .mapValues { (_, list) -> list.minOfOrNull { it.id } }
                } else {
                    lastFetchedRawIdByType
                }
                val (from, to) = getTimeRange(
                    filter = currentState.selectedFilter,
                    customDateStartMs = currentState.customDateStartMs,
                    customDateEndMs = currentState.customDateEndMs,
                    customRangeFromMs = currentState.customRangeFromMs,
                    customRangeToMs = currentState.customRangeToMs
                )
                var batchResult = fetchPermittedEventBatch(
                    startLastId = startId,
                    fromSec = from,
                    toSec = to,
                    serviceId = currentState.selectedServiceId,
                    cameraUuid = currentState.selectedCameraUuid,
                    resolvedTypes = resolvedTypes,
                    startLastIdByType = fallbackCursors,
                    currentExhaustedTypes = exhaustedEventTypes,
                    isLoadMore = true
                )
                if (currentState.selectedAiType.isNullOrBlank() &&
                    resolvedTypes.isEmpty() &&
                    batchResult.apiSucceeded &&
                    !batchResult.reachedEnd &&
                    !batchResult.hadTransientError &&
                    batchResult.events.isEmpty()
                ) {
                    val discoveredTypes = resolveFallbackEventTypesForAllFilter(
                        filter = currentState.selectedFilter,
                        fromSec = from,
                        toSec = to,
                        serviceId = currentState.selectedServiceId,
                        hitUnfilteredCeiling = true
                    )
                    if (discoveredTypes.isNotEmpty()) {
                        effectiveUnfilteredTypes = discoveredTypes
                        resolvedTypes = discoveredTypes
                        val seededCursors = currentState.events
                            .groupBy { it.type }
                            .mapValues { (_, list) -> list.minOfOrNull { it.id } }
                        batchResult = fetchPermittedEventBatch(
                            startLastId = if (discoveredTypes.size == 1) seededCursors[discoveredTypes[0]] else null,
                            fromSec = from,
                            toSec = to,
                            serviceId = currentState.selectedServiceId,
                            cameraUuid = currentState.selectedCameraUuid,
                            resolvedTypes = discoveredTypes,
                            startLastIdByType = seededCursors,
                            currentExhaustedTypes = emptySet(),
                            isLoadMore = true
                        )
                    }
                }
                val newEvents = batchResult.events
                if (batchResult.apiSucceeded) {
                    lastFetchedRawId = batchResult.lastRawId
                    lastFetchedRawIdByType = batchResult.lastRawIdByType
                    exhaustedEventTypes = batchResult.exhaustedTypes
                }
                if (newEvents.isNotEmpty()) {
                    eventRepository.cacheInMemory(events = newEvents)
                }

                _uiState.update { cur ->
                    if (!cur.matchesFilter(
                            filter = currentState.selectedFilter,
                            serviceId = currentState.selectedServiceId,
                            cameraUuid = currentState.selectedCameraUuid,
                            aiType = currentState.selectedAiType,
                            customDateStartMs = currentState.customDateStartMs,
                            customDateEndMs = currentState.customDateEndMs,
                            customRangeFromMs = currentState.customRangeFromMs,
                            customRangeToMs = currentState.customRangeToMs
                        )
                    ) {
                        return@update cur.copy(isPaginating = false)
                    }
                    val merged = (cur.events + newEvents)
                        .distinctBy { ev -> ev.uniqueKey() }
                        .let { list -> if (resolvedTypes.size > 1) list.sortedWith(EventDescendingComparator) else list }
                    cur.copy(
                        isPaginating = false,
                        events = merged,
                        hasMore = if (batchResult.apiSucceeded) {
                            batchResult.hasMore && (merged.size > cur.events.size || batchResult.hadTransientError)
                        } else {
                            cur.hasMore
                        },
                        error = batchResult.firstError?.toUserFriendlyMessage()?.takeIf { msg -> msg.isNotEmpty() }
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error loading more events", e)
                if (UserSession.accessToken.isEmpty()) return@launch
                _uiState.update {
                    it.copy(
                        isPaginating = false,
                        error = e.toUserFriendlyMessage().takeIf { msg -> msg.isNotEmpty() }
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    companion object {
        private const val TAG = "EventViewModel"
        private const val FIRST_FAST_BATCH_SIZE = 25
        private const val RAW_BATCH_SIZE = 50
        private const val PAGE_TARGET_COUNT = 20
        private const val MAX_BATCH_ATTEMPTS = 5
        private const val MAX_TYPED_QUERY_CONCURRENCY = 10
        private const val MAX_REALTIME_EVENTS = 500
        private const val REALTIME_MIN_INTERVAL_MS = 350L
        private const val REALTIME_DB_COMMIT_RETRY_MS = 650L

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as TenliApp
                EventViewModel(
                    application = application,
                    eventRepository = application.container.eventRepository,
                    webSocketManager = application.container.webSocketManager
                )
            }
        }
    }
}
