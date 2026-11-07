package com.tenli.oneview.ui.features.event

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tenli.oneview.data.local.CachedEventData
import com.tenli.oneview.data.local.EventCacheManager
import com.tenli.oneview.data.local.GlobalData
import com.tenli.oneview.data.local.UserPermissionManager
import com.tenli.oneview.data.network.ApiProvider
import com.tenli.oneview.data.network.websocket.WebSocketManager
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
    private val webSocketManager: WebSocketManager
) : AndroidViewModel(application) {
    private val eventApi get() = ApiProvider.eventApi
    private val vmsApi get() = ApiProvider.vmsApi

    private val _uiState = MutableStateFlow(
        run {
            val isList = readIsListViewPref()
            val memCached = EventCacheManager.getEventDataInMemory(TimeFilter.TODAY, null, null, null)
            if (memCached != null) {
                val rawCameras = memCached.cameraList.ifEmpty { EventCacheManager.getCamerasInMemory() }
                val permittedCameras = UserPermissionManager.filterPermittedCameras(rawCameras)
                val permitted = memCached.events.permittedDistinct(rawCameras)
                EventScreenUiState(
                    isListView = isList,
                    isLoading = false,
                    events = permitted,
                    cameraList = permittedCameras,
                    cameraNameMap = buildCameraNameMap(rawCameras),
                    aiServices = memCached.aiServices.ifEmpty { EventCacheManager.getAiServicesInMemory() },
                    hasMore = permitted.isNotEmpty()
                )
            } else {
                val rawCameras = EventCacheManager.getCamerasInMemory()
                val permittedCameras = UserPermissionManager.filterPermittedCameras(rawCameras)
                EventScreenUiState(
                    isListView = isList,
                    isLoading = true,
                    cameraList = permittedCameras,
                    cameraNameMap = buildCameraNameMap(rawCameras),
                    aiServices = EventCacheManager.getAiServicesInMemory()
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

    private fun readIsListViewPref(): Boolean =
        GlobalData.preferences.getString(PREF_VIEW_MODE, VIEW_MODE_LIST) != VIEW_MODE_GRID

    fun toggleViewMode() {
        val nextIsList = !_uiState.value.isListView
        GlobalData.preferences.edit()
            .putString(PREF_VIEW_MODE, if (nextIsList) VIEW_MODE_LIST else VIEW_MODE_GRID)
            .apply()
        _uiState.update { it.copy(isListView = nextIsList) }
    }

    fun syncViewModeFromPrefs() {
        val savedIsList = readIsListViewPref()
        if (_uiState.value.isListView != savedIsList) {
            _uiState.update { it.copy(isListView = savedIsList) }
        }
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
                    val allCameras = EventCacheManager.getCamerasInMemory()
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
        allCameras: List<CameraModel> = EventCacheManager.getCamerasInMemory()
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
                eventApi.getDataList(
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

            val camerasForPerm = allCameras.ifEmpty { EventCacheManager.getCamerasInMemory() }
            for (ev in batchData) {
                if (UserPermissionManager.isEventPermitted(ev, camerasForPerm) && seenKeys.add(ev.uniqueKey())) {
                    validData.add(ev)
                }
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
                val (from, to) = getTimeRange(state.selectedFilter)
                val response = eventApi.getDataList(
                    count = PAGE_TARGET_COUNT,
                    from = from,
                    to = to,
                    serviceId = state.selectedServiceId,
                    cameraUUID = state.selectedCameraUuid,
                    type = resolveApiEventType(state.selectedAiType)
                )
                if (response.isSuccessful) {
                    val allCameras = EventCacheManager.getCamerasInMemory().ifEmpty { state.cameraList }
                    val permittedNew = UserPermissionManager.filterEventsByPermissions(
                        events = response.body() ?: emptyList(),
                        allCameras = allCameras
                    )
                    if (permittedNew.isNotEmpty()) {
                        EventCacheManager.cacheInMemory(events = permittedNew)
                        var updatedList: List<EventData> = emptyList()
                        _uiState.update { s ->
                            val merged = (permittedNew + s.events).distinctBy { it.uniqueKey() }
                            updatedList = merged
                            s.copy(events = merged)
                        }
                        val latestState = _uiState.value
                        EventCacheManager.saveEventData(
                            context = getApplication(),
                            filter = latestState.selectedFilter,
                            serviceId = latestState.selectedServiceId,
                            aiType = latestState.selectedAiType,
                            data = CachedEventData(
                                events = updatedList.take(RAW_BATCH_SIZE),
                                cameraList = EventCacheManager.getCamerasInMemory().ifEmpty { latestState.cameraList },
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
        initialFetchJob = viewModelScope.launch(Dispatchers.IO) {
            lastFetchedRawId = null
            _uiState.update { it.copy(isPaginating = false) }
            val startState = _uiState.value
            val filter = startState.selectedFilter
            val serviceId = startState.selectedServiceId
            val cameraUuid = startState.selectedCameraUuid
            val aiType = startState.selectedAiType

            // 1. Ưu tiên hiển thị ngay dữ liệu từ RAM / Disk Cache (Stale-While-Revalidate)
            val cachedData = EventCacheManager.getEventData(
                context = getApplication(),
                filter = filter,
                serviceId = serviceId,
                aiType = aiType,
                cameraUuid = cameraUuid
            )
            if (cachedData != null && !isUserRefresh) {
                val rawCachedCameras = cachedData.cameraList.ifEmpty {
                    EventCacheManager.getCamerasInMemory().ifEmpty { _uiState.value.cameraList }
                }
                val permittedCached = withContext(Dispatchers.Default) {
                    cachedData.events.permittedDistinct(rawCachedCameras)
                }
                lastFetchedRawId = permittedCached.lastOrNull()?.id
                _uiState.update {
                    val permittedCameras = UserPermissionManager.filterPermittedCameras(rawCachedCameras)
                    val services = cachedData.aiServices.ifEmpty {
                        it.aiServices.ifEmpty { EventCacheManager.getAiServicesInMemory() }
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
                _uiState.update { it.copy(isLoading = true, error = null, hasMore = true) }
            }

            try {
                coroutineScope {
                    val hasFreshMetadata = !isUserRefresh && EventCacheManager.hasFreshSessionMetadata()
                    val needCameras = (reloadMetadata && !hasFreshMetadata) ||
                        EventCacheManager.getCamerasInMemory().isEmpty()
                    val needAiServices = (reloadMetadata && !hasFreshMetadata) || _uiState.value.aiServices.isEmpty()
                    val cameraDeferred = async {
                        if (needCameras) safeApiCall { vmsApi.getCameraList() } else null
                    }
                    val aiServiceDeferred = async {
                        if (needAiServices) safeApiCall { eventApi.getAIServiceList() } else null
                    }

                    // Nếu tài khoản có phân quyền camera custom và RAM chưa có danh sách camera,
                    // đợi cameraDeferred xong trước khi lọc sự kiện để có map UUID đầy đủ ngay lượt đầu.
                    if (UserPermissionManager.isCustomCameraPermission() &&
                        EventCacheManager.getCamerasInMemory().isEmpty()
                    ) {
                        val preCamResp = cameraDeferred.await()?.getOrNull()
                        if (preCamResp?.isSuccessful == true) {
                            val preCamList = preCamResp.body() ?: emptyList()
                            if (preCamList.isNotEmpty()) {
                                EventCacheManager.cacheInMemory(cameras = preCamList, fromNetwork = true)
                            }
                        }
                    }

                    val (from, to) = getTimeRange(filter)
                    val eventsDeferred = async {
                        fetchPermittedEventBatch(
                            startLastId = null,
                            fromSec = from,
                            toSec = to,
                            serviceId = serviceId,
                            cameraUuid = cameraUuid,
                            resolvedType = resolveApiEventType(aiType),
                            allCameras = EventCacheManager.getCamerasInMemory()
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
                        EventCacheManager.getCamerasInMemory().isNotEmpty() -> EventCacheManager.getCamerasInMemory()
                        else -> currentState.cameraList
                    }
                    val permittedCameraList = UserPermissionManager.filterPermittedCameras(allCameraList)

                    val aiServices = when {
                        aiServiceResponse?.isSuccessful == true -> aiServiceResponse.body() ?: emptyList()
                        currentState.aiServices.isNotEmpty() -> currentState.aiServices
                        else -> EventCacheManager.getAiServicesInMemory()
                    }

                    val rawFinalEvents = if (batchResult.apiSucceeded || newEvents.isNotEmpty() || cachedData == null) {
                        lastFetchedRawId = nextLastId
                        newEvents
                    } else {
                        currentState.events
                    }
                    val finalEvents = rawFinalEvents.permittedDistinct(allCameraList)

                    val failure = batchResult.firstError
                        ?: cameraResult?.exceptionOrNull()
                        ?: aiServiceResult?.exceptionOrNull()

                    EventCacheManager.cacheInMemory(
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
                            error = failure?.toUserFriendlyMessage()
                        )
                    }

                    if (batchResult.apiSucceeded || newEvents.isNotEmpty()) {
                        EventCacheManager.saveEventData(
                            context = getApplication(),
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
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        hasMore = false,
                        error = e.toUserFriendlyMessage()
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
                val startId = lastFetchedRawId ?: currentState.events.last().id
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
                val nextLastId = batchResult.lastRawId
                val hasMore = batchResult.hasMore
                lastFetchedRawId = nextLastId
                if (newEvents.isNotEmpty()) {
                    EventCacheManager.cacheInMemory(events = newEvents)
                }

                _uiState.update {
                    val merged = (it.events + newEvents).distinctBy { ev -> ev.uniqueKey() }
                    it.copy(
                        isPaginating = false,
                        events = merged,
                        hasMore = hasMore && merged.size > it.events.size,
                        error = batchResult.firstError?.toUserFriendlyMessage()
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error loading more events", e)
                _uiState.update { it.copy(isPaginating = false, hasMore = false, error = e.toUserFriendlyMessage()) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    companion object {
        private const val TAG = "EventViewModel"
        private const val PREF_VIEW_MODE = "mobile_event_view_mode"
        private const val VIEW_MODE_LIST = "list"
        private const val VIEW_MODE_GRID = "grid"
        private const val RAW_BATCH_SIZE = 50
        private const val PAGE_TARGET_COUNT = 20
        private const val MAX_BATCH_ATTEMPTS = 5

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as com.tenli.oneview.TenliApp
                EventViewModel(application, application.container.webSocketManager)
            }
        }
    }
}
