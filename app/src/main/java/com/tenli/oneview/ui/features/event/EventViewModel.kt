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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    private var lastFetchedRawId: Int? = null
    private var initialFetchJob: Job? = null
    private var recentFetchJob: Job? = null
    private var paginateJob: Job? = null
    @Volatile private var isEventTabActive: Boolean = false
    @Volatile private var pendingNotifyRefresh: Boolean = false

    init {
        fetchInitialData(reloadMetadata = true)
        listenToWebSocket()
    }

    fun setEventTabActive(active: Boolean) {
        if (isEventTabActive == active) return
        isEventTabActive = active
        if (!active) {
            recentFetchJob?.cancel()
            return
        }
        if (pendingNotifyRefresh && _uiState.value.selectedFilter == TimeFilter.TODAY) {
            pendingNotifyRefresh = false
            fetchRecentEvents()
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
        viewModelScope.launch {
            webSocketManager.notifyEvent
                .debounce(600L)
                .collect {
                    if (_uiState.value.selectedFilter == TimeFilter.TODAY) {
                        if (isEventTabActive) {
                            fetchRecentEvents()
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
                fetchInitialData(reloadMetadata = true)
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
        val hasMore: Boolean,
        val apiSucceeded: Boolean,
        val firstError: Throwable? = null
    )

    /**
     * Khớp hoàn toàn với fetchEvents() trong MobileEventsPage.vue:
     * Lặp tối đa 5 lượt (mỗi lượt 50 bản ghi thô) để lọc đủ tối thiểu 20 sự kiện hợp lệ (isEventPermitted),
     * tránh việc 1 batch 50 bản ghi chỉ có vài sự kiện hợp lệ làm danh sách quá ngắn không cuộn được.
     */
    private suspend fun fetchPermittedEventBatch(
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
                apiSucceeded = true
            )
        }
        val validData = mutableListOf<EventData>()
        val seenKeys = mutableSetOf<String>()
        var currentLastId = startLastId
        var attempts = 0
        var reachedEnd = false
        var anySuccessful = false
        var firstError: Throwable? = null

        while (attempts < MAX_BATCH_ATTEMPTS) {
            attempts++
            val result = safeApiCall {
                eventRepository.getDataList(
                    lastId = currentLastId,
                    count = RAW_BATCH_SIZE,
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
                reachedEnd = true
                break
            }
            anySuccessful = true

            val batchData = response.body() ?: emptyList()
            if (batchData.isEmpty()) {
                reachedEnd = true
                break
            }

            val camerasForPerm = allCameras.ifEmpty { eventRepository.getCamerasInMemory() }
            for (ev in batchData) {
                if (ev.isWithinTimeRangeSeconds(fromSec, toSec) &&
                    UserPermissionManager.isEventPermitted(ev, camerasForPerm) &&
                    seenKeys.add(ev.uniqueKey())
                ) {
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

            if (batchData.size < RAW_BATCH_SIZE) {
                reachedEnd = true
                break
            }
            if (validData.size >= targetCount) {
                break
            }
        }

        // Nếu sau 5 lượt (250 bản ghi thô) vẫn không có sự kiện hợp lệ nào thì dừng tự động phân trang để tránh vòng lặp vô hạn
        val hasMore = !reachedEnd && validData.isNotEmpty()
        return EventBatchResult(
            events = validData,
            lastRawId = currentLastId,
            hasMore = hasMore,
            apiSucceeded = anySuccessful,
            firstError = if (anySuccessful) null else firstError
        )
    }

    private fun fetchRecentEvents() {
        recentFetchJob?.cancel()
        recentFetchJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!UserPermissionManager.canAccessEvents()) return@launch
                val state = _uiState.value
                if (state.selectedFilter != TimeFilter.TODAY) return@launch
                val (from, to) = getTimeRange(state.selectedFilter)
                val response = eventRepository.getDataList(
                    count = PAGE_TARGET_COUNT,
                    from = from,
                    to = to,
                    serviceId = state.selectedServiceId,
                    cameraUUID = state.selectedCameraUuid,
                    type = resolveApiEventType(state.selectedAiType)
                )
                if (response.isSuccessful) {
                    if (_uiState.value.selectedFilter != state.selectedFilter ||
                        _uiState.value.selectedServiceId != state.selectedServiceId ||
                        _uiState.value.selectedCameraUuid != state.selectedCameraUuid ||
                        _uiState.value.selectedAiType != state.selectedAiType
                    ) {
                        return@launch
                    }
                    val allCameras = eventRepository.getCamerasInMemory().ifEmpty { state.cameraList }
                    val permittedNew = UserPermissionManager.filterEventsByPermissions(
                        events = (response.body() ?: emptyList()).filter { it.isWithinTimeRangeSeconds(from, to) },
                        allCameras = allCameras
                    )
                    if (permittedNew.isNotEmpty()) {
                        eventRepository.cacheInMemory(events = permittedNew)
                        var updatedList: List<EventData> = emptyList()
                        _uiState.update { s ->
                            val merged = (permittedNew + s.events.filter { it.isWithinTimeRangeSeconds(from, to) })
                                .distinctBy { it.uniqueKey() }
                            updatedList = merged
                            s.copy(events = merged)
                        }
                        val latestState = _uiState.value
                        eventRepository.saveEventData(
                            filter = latestState.selectedFilter,
                            serviceId = latestState.selectedServiceId,
                            aiType = latestState.selectedAiType,
                            data = CachedEventData(
                                events = updatedList.take(RAW_BATCH_SIZE),
                                cameraList = eventRepository.getCamerasInMemory().ifEmpty { latestState.cameraList },
                                aiServices = latestState.aiServices,
                                updatedAt = System.currentTimeMillis()
                            ),
                            cameraUuid = latestState.selectedCameraUuid
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching recent events via WebSocket", e)
            }
        }
    }

    fun applyFilters(
        filter: TimeFilter,
        serviceId: Int?,
        aiType: String?,
        cameraUuid: String? = _uiState.value.selectedCameraUuid
    ) {
        val isCustom = filter == TimeFilter.CUSTOM_DATE || filter == TimeFilter.CUSTOM_RANGE
        val current = _uiState.value
        if (!isCustom &&
            current.selectedFilter == filter &&
            current.selectedServiceId == serviceId &&
            current.selectedCameraUuid == cameraUuid &&
            current.selectedAiType == aiType
        ) return
        recentFetchJob?.cancel()
        pendingNotifyRefresh = false
        _uiState.update {
            it.copy(
                selectedFilter = filter,
                selectedServiceId = serviceId,
                selectedCameraUuid = cameraUuid,
                selectedAiType = aiType,
                isRefreshing = false
            )
        }
        // Đổi bộ lọc chỉ cần tải lại sự kiện; danh sách camera / AI service đã có thì dùng lại
        fetchInitialData(reloadMetadata = false)
    }

    fun resetFilters() {
        applyFilters(
            filter = TimeFilter.TODAY,
            serviceId = null,
            aiType = null,
            cameraUuid = null
        )
    }

    /** Kéo để làm mới: tải lại cả sự kiện lẫn danh sách camera / AI service. */
    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        fetchInitialData(reloadMetadata = true, isUserRefresh = true)
    }

    private fun getTimeRange(filter: TimeFilter): Pair<Long, Long> {
        val (fromTime, toTime) = TimeFilter.resolveTimeRangeMillis(filter)
        return Pair(fromTime / 1000, toTime / 1000)
    }

    /**
     * @param reloadMetadata `true` để tải lại danh sách camera / AI service từ API;
     * `false` thì chỉ tải khi trong state chưa có dữ liệu.
     */
    fun fetchInitialData(reloadMetadata: Boolean = true, isUserRefresh: Boolean = false) {
        initialFetchJob?.cancel()
        paginateJob?.cancel()
        recentFetchJob?.cancel()
        if (UserSession.accessToken.isEmpty() || UserSession.domain.isEmpty()) return
        initialFetchJob = viewModelScope.launch(Dispatchers.IO) {
            lastFetchedRawId = null
            _uiState.update { it.copy(isPaginating = false) }
            val startState = _uiState.value
            val filter = startState.selectedFilter
            val serviceId = startState.selectedServiceId
            val cameraUuid = startState.selectedCameraUuid
            val aiType = startState.selectedAiType

            val (from, to) = getTimeRange(filter)

            // 1. Ưu tiên hiển thị ngay dữ liệu từ RAM / Disk Cache (Stale-While-Revalidate)
            val cachedData = eventRepository.getEventData(
                filter = filter,
                serviceId = serviceId,
                aiType = aiType,
                cameraUuid = cameraUuid
            )
            if (cachedData != null && !isUserRefresh) {
                val rawCachedCameras = cachedData.cameraList.ifEmpty {
                    eventRepository.getCamerasInMemory().ifEmpty { _uiState.value.cameraList }
                }
                val permittedCached = withContext(Dispatchers.Default) {
                    cachedData.events
                        .filter { it.isWithinTimeRangeSeconds(from, to) }
                        .permittedDistinct(rawCachedCameras)
                }
                lastFetchedRawId = permittedCached.lastOrNull()?.id
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
                        hasMore = permittedCached.isNotEmpty()
                    )
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

            try {
                coroutineScope {
                    val hasFreshMetadata = !isUserRefresh && eventRepository.hasFreshSessionMetadata()
                    val needCameras = (reloadMetadata && !hasFreshMetadata) ||
                        eventRepository.getCamerasInMemory().isEmpty()
                    val needAiServices = (reloadMetadata && !hasFreshMetadata) || _uiState.value.aiServices.isEmpty()
                    val cameraDeferred = async {
                        if (needCameras) safeApiCall { eventRepository.getCameraList() } else null
                    }
                    val aiServiceDeferred = async {
                        if (needAiServices) safeApiCall { eventRepository.getAIServiceList() } else null
                    }

                    // Nếu tài khoản có phân quyền camera custom và RAM chưa có danh sách camera,
                    // đợi cameraDeferred xong trước khi lọc sự kiện để có map UUID đầy đủ ngay lượt đầu.
                    if (UserPermissionManager.isCustomCameraPermission() &&
                        eventRepository.getCamerasInMemory().isEmpty()
                    ) {
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
                            resolvedType = resolveApiEventType(aiType),
                            allCameras = eventRepository.getCamerasInMemory()
                        )
                    }

                    val cameraResult = cameraDeferred.await()
                    val aiServiceResult = aiServiceDeferred.await()
                    val batchResult = eventsDeferred.await()
                    val cameraResponse = cameraResult?.getOrNull()
                    val aiServiceResponse = aiServiceResult?.getOrNull()
                    val newEvents = batchResult.events
                    val nextLastId = batchResult.lastRawId
                    val hasMore = batchResult.hasMore

                    val currentState = _uiState.value
                    val allCameraList = when {
                        cameraResponse?.isSuccessful == true -> cameraResponse.body() ?: emptyList()
                        eventRepository.getCamerasInMemory().isNotEmpty() -> eventRepository.getCamerasInMemory()
                        else -> currentState.cameraList
                    }
                    val permittedCameraList = UserPermissionManager.filterPermittedCameras(allCameraList)

                    val aiServices = when {
                        aiServiceResponse?.isSuccessful == true -> aiServiceResponse.body() ?: emptyList()
                        currentState.aiServices.isNotEmpty() -> currentState.aiServices
                        else -> eventRepository.getAiServicesInMemory()
                    }

                    val rawFinalEvents = if (batchResult.apiSucceeded || newEvents.isNotEmpty() || cachedData == null) {
                        lastFetchedRawId = nextLastId
                        newEvents
                    } else {
                        currentState.events
                    }
                    val finalEvents = rawFinalEvents
                        .filter { it.isWithinTimeRangeSeconds(from, to) }
                        .permittedDistinct(allCameraList)

                    val failure = batchResult.firstError
                        ?: cameraResult?.exceptionOrNull()
                        ?: aiServiceResult?.exceptionOrNull()

                    if (UserSession.accessToken.isEmpty()) return@coroutineScope

                    eventRepository.cacheInMemory(
                        events = finalEvents,
                        cameras = allCameraList,
                        aiServices = aiServices,
                        fromNetwork = cameraResponse?.isSuccessful == true
                    )

                    val cameraNameMap = withContext(Dispatchers.Default) { buildCameraNameMap(allCameraList) }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            cameraList = permittedCameraList,
                            cameraNameMap = cameraNameMap,
                            aiServices = aiServices,
                            events = finalEvents,
                            hasMore = hasMore,
                            error = failure?.toUserFriendlyMessage()?.takeIf { msg -> msg.isNotEmpty() }
                        )
                    }

                    if (batchResult.apiSucceeded || newEvents.isNotEmpty()) {
                        eventRepository.saveEventData(
                            filter = filter,
                            serviceId = serviceId,
                            aiType = aiType,
                            data = CachedEventData(
                                events = finalEvents,
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
        paginateJob = viewModelScope.launch {
            _uiState.update { it.copy(isPaginating = true, error = null) }
            try {
                val startId = lastFetchedRawId ?: currentState.events.lastOrNull()?.id ?: return@launch
                val (from, to) = getTimeRange(currentState.selectedFilter)
                val batchResult = fetchPermittedEventBatch(
                    startLastId = startId,
                    fromSec = from,
                    toSec = to,
                    serviceId = currentState.selectedServiceId,
                    cameraUuid = currentState.selectedCameraUuid,
                    resolvedType = resolveApiEventType(currentState.selectedAiType)
                )
                val newEvents = batchResult.events
                if (batchResult.apiSucceeded) {
                    lastFetchedRawId = batchResult.lastRawId
                }
                if (newEvents.isNotEmpty()) {
                    eventRepository.cacheInMemory(events = newEvents)
                }

                _uiState.update {
                    val merged = (it.events + newEvents).distinctBy { ev -> ev.uniqueKey() }
                    it.copy(
                        isPaginating = false,
                        events = merged,
                        hasMore = if (batchResult.apiSucceeded) {
                            batchResult.hasMore && merged.size > it.events.size
                        } else {
                            it.hasMore
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
        private const val RAW_BATCH_SIZE = 50
        private const val PAGE_TARGET_COUNT = 20
        private const val MAX_BATCH_ATTEMPTS = 5

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
