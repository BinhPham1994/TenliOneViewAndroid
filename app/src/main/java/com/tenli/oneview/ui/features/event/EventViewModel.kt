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
import com.tenli.oneview.data.network.api.EventApi
import com.tenli.oneview.data.network.api.VmsApi
import com.tenli.oneview.data.network.retrofit.LoginAuthClient
import com.tenli.oneview.model.network.AIServiceModel
import com.tenli.oneview.model.network.CameraModel
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.ui.features.home.TimeFilter
import com.tenli.oneview.ui.utils.AiTypeHelper
import com.tenli.oneview.util.toUserFriendlyMessage
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EventScreenUiState(
    val events: List<EventData> = emptyList(),
    val cameraList: List<CameraModel> = emptyList(),
    val aiServices: List<AIServiceModel> = emptyList(),
    val selectedFilter: TimeFilter = TimeFilter.TODAY,
    val selectedServiceId: Int? = null,
    val selectedCameraUuid: String? = null,
    val selectedAiType: String? = null,
    val isListView: Boolean = true,
    val isLoading: Boolean = false,
    val isPaginating: Boolean = false,
    val hasMore: Boolean = true,
    val error: String? = null
)

@OptIn(FlowPreview::class)
class EventViewModel(
    application: Application,
    private val webSocketManager: com.tenli.oneview.data.network.websocket.WebSocketManager
) : AndroidViewModel(application) {
    private val eventApi = LoginAuthClient.create(EventApi::class.java)
    private val vmsApi = LoginAuthClient.create(VmsApi::class.java)

    private val _uiState = MutableStateFlow(
        run {
            val isList = GlobalData.preferences.getString("mobile_event_view_mode", "list") != "grid"
            val memCached = EventCacheManager.getEventDataInMemory(TimeFilter.TODAY, null, null, null)
            if (memCached != null) {
                val permitted = memCached.events
                    .filter { AiTypeHelper.isAiTypeEnabled(it.type) }
                    .distinctBy { "${it.serviceId}_${it.id}_${it.time}" }
                EventScreenUiState(
                    isListView = isList,
                    isLoading = false,
                    events = permitted,
                    cameraList = memCached.cameraList.ifEmpty { EventCacheManager.getCamerasInMemory() },
                    aiServices = memCached.aiServices.ifEmpty { EventCacheManager.getAiServicesInMemory() },
                    hasMore = permitted.isNotEmpty()
                )
            } else {
                EventScreenUiState(
                    isListView = isList,
                    cameraList = EventCacheManager.getCamerasInMemory(),
                    aiServices = EventCacheManager.getAiServicesInMemory()
                )
            }
        }
    )
    val uiState: StateFlow<EventScreenUiState> = _uiState.asStateFlow()

    // Track last raw ID returned by API so pagination advances even past disabled AI event batches
    private var lastFetchedRawId: Int? = null

    init {
        fetchInitialData()
        listenToWebSocket()
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
            Log.d("EventViewModel", "Started listening to notifyEvent flow")
            webSocketManager.notifyEvent
                .debounce(600L)
                .collect { _ ->
                    Log.d("EventViewModel", "notifyEvent debounced -> filter=${_uiState.value.selectedFilter}")
                    if (_uiState.value.selectedFilter == TimeFilter.TODAY) {
                        fetchRecentEvents()
                    }
                }
        }

        viewModelScope.launch {
            com.tenli.oneview.util.EventBus.eventReportedFalse.collect { eventId ->
                _uiState.update { state ->
                    state.copy(
                        events = state.events.filter { it.id != eventId }
                    )
                }
            }
        }
    }

    private fun resolveApiEventType(aiType: String?): String? {
        if (aiType.isNullOrBlank()) return null
        return AiTypeHelper.getAiEventTypes(aiType).firstOrNull() ?: aiType
    }

    private fun eventUniqueKey(ev: EventData): String {
        return "${ev.serviceId}_${ev.id}_${ev.time}"
    }

    /**
     * Khớp hoàn toàn với fetchEvents() trong MobileEventsPage.vue:
     * Lặp tối đa 5 lượt (mỗi lượt 50 bản ghi thô) để lọc đủ tối thiểu 20 sự kiện hợp lệ (isAiTypeEnabled),
     * tránh việc 1 batch 50 bản ghi chỉ có vài sự kiện hợp lệ làm danh sách quá ngắn không cuộn được.
     */
    private suspend fun fetchPermittedEventBatch(
        startLastId: Int?,
        fromSec: Long,
        toSec: Long,
        serviceId: Int?,
        cameraUuid: String?,
        resolvedType: String?,
        targetCount: Int = 20
    ): Triple<List<EventData>, Int?, Boolean> {
        val validData = mutableListOf<EventData>()
        val seenKeys = mutableSetOf<String>()
        var currentLastId = startLastId
        var attempts = 0
        val maxAttempts = 5
        var reachedEnd = false

        while (attempts < maxAttempts) {
            attempts++
            val response = runCatching {
                eventApi.getDataList(
                    lastId = currentLastId,
                    count = 50,
                    from = fromSec,
                    to = toSec,
                    serviceId = serviceId,
                    cameraUUID = cameraUuid,
                    type = resolvedType
                )
            }.getOrNull()

            if (response == null || !response.isSuccessful) {
                if (attempts == 1) reachedEnd = true
                break
            }

            val batchData = response.body() ?: emptyList()
            if (batchData.isEmpty()) {
                reachedEnd = true
                break
            }

            for (ev in batchData) {
                if (AiTypeHelper.isAiTypeEnabled(ev.type)) {
                    val key = eventUniqueKey(ev)
                    if (seenKeys.add(key)) {
                        validData.add(ev)
                    }
                }
            }

            val lastItem = batchData.lastOrNull()
            if (lastItem == null || lastItem.id <= 0 || lastItem.id == currentLastId) {
                reachedEnd = true
                break
            }
            currentLastId = lastItem.id

            if (batchData.size < 50) {
                reachedEnd = true
                break
            }
            if (validData.size >= targetCount) {
                break
            }
        }

        val hasMore = !reachedEnd && (validData.isNotEmpty() || attempts == maxAttempts)
        return Triple(validData, currentLastId, hasMore)
    }

    private fun fetchRecentEvents() {
        viewModelScope.launch {
            try {
                val (from, to) = getTimeRange()
                val resolvedType = resolveApiEventType(_uiState.value.selectedAiType)
                val response = eventApi.getDataList(
                    count = 20,
                    from = from,
                    to = to,
                    serviceId = _uiState.value.selectedServiceId,
                    cameraUUID = _uiState.value.selectedCameraUuid,
                    type = resolvedType
                )
                if (response.isSuccessful) {
                    val rawEvents = response.body() ?: emptyList()
                    val permittedNew = rawEvents.filter { AiTypeHelper.isAiTypeEnabled(it.type) }
                    if (permittedNew.isNotEmpty()) {
                        EventCacheManager.cacheInMemory(events = permittedNew)
                        _uiState.update { state ->
                            val merged = (permittedNew + state.events)
                                .distinctBy { eventUniqueKey(it) }
                            state.copy(events = merged)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error fetching recent events via WebSocket", e)
            }
        }
    }

    fun applyFilters(
        filter: TimeFilter,
        serviceId: Int?,
        aiType: String?,
        cameraUuid: String? = _uiState.value.selectedCameraUuid
    ) {
        if (_uiState.value.selectedFilter == filter &&
            _uiState.value.selectedServiceId == serviceId &&
            _uiState.value.selectedCameraUuid == cameraUuid &&
            _uiState.value.selectedAiType == aiType
        ) return
        _uiState.update {
            it.copy(
                selectedFilter = filter,
                selectedServiceId = serviceId,
                selectedCameraUuid = cameraUuid,
                selectedAiType = aiType
            )
        }
        fetchInitialData()
    }

    fun resetFilters() {
        applyFilters(
            filter = TimeFilter.TODAY,
            serviceId = null,
            aiType = null,
            cameraUuid = null
        )
    }

    private fun getTimeRange(): Pair<Long, Long> {
        val calendar = java.util.Calendar.getInstance()
        val toTime: Long
        val fromTime: Long

        when (_uiState.value.selectedFilter) {
            TimeFilter.TODAY -> {
                toTime = calendar.timeInMillis
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                fromTime = calendar.timeInMillis
            }
            TimeFilter.YESTERDAY -> {
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                toTime = calendar.timeInMillis - 1
                calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
                fromTime = calendar.timeInMillis
            }
            TimeFilter.LAST_7_DAYS -> {
                toTime = calendar.timeInMillis
                calendar.add(java.util.Calendar.DAY_OF_YEAR, -7)
                fromTime = calendar.timeInMillis
            }
            TimeFilter.LAST_30_DAYS -> {
                toTime = calendar.timeInMillis
                calendar.add(java.util.Calendar.DAY_OF_YEAR, -30)
                fromTime = calendar.timeInMillis
            }
        }
        return Pair(fromTime / 1000, toTime / 1000)
    }

    fun fetchInitialData() {
        viewModelScope.launch {
            lastFetchedRawId = null
            val filter = _uiState.value.selectedFilter
            val serviceId = _uiState.value.selectedServiceId
            val cameraUuid = _uiState.value.selectedCameraUuid
            val aiType = _uiState.value.selectedAiType

            // 1. Ưu tiên hiển thị ngay dữ liệu từ RAM / Disk Cache (Stale-While-Revalidate)
            val cachedData = EventCacheManager.getEventData(
                context = getApplication(),
                filter = filter,
                serviceId = serviceId,
                aiType = aiType,
                cameraUuid = cameraUuid
            )
            if (cachedData != null) {
                val permittedCached = cachedData.events
                    .filter { AiTypeHelper.isAiTypeEnabled(it.type) }
                    .distinctBy { eventUniqueKey(it) }
                lastFetchedRawId = permittedCached.lastOrNull()?.id
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = null,
                        cameraList = cachedData.cameraList.ifEmpty { it.cameraList },
                        aiServices = cachedData.aiServices.ifEmpty { it.aiServices },
                        events = permittedCached,
                        hasMore = permittedCached.isNotEmpty()
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = true, error = null, hasMore = true) }
            }

            try {
                kotlinx.coroutines.coroutineScope {
                    val cameraDeferred = async {
                        runCatching { vmsApi.getCameraList() }.getOrNull()
                    }
                    val aiServiceDeferred = async {
                        runCatching { eventApi.getAIServiceList() }.getOrNull()
                    }
                    val (from, to) = getTimeRange()
                    val resolvedType = resolveApiEventType(aiType)
                    val eventsDeferred = async {
                        fetchPermittedEventBatch(
                            startLastId = null,
                            fromSec = from,
                            toSec = to,
                            serviceId = serviceId,
                            cameraUuid = cameraUuid,
                            resolvedType = resolvedType,
                            targetCount = 20
                        )
                    }

                    val cameraResponse = cameraDeferred.await()
                    val aiServiceResponse = aiServiceDeferred.await()
                    val (newEvents, nextLastId, hasMore) = eventsDeferred.await()

                    val cameraList = if (cameraResponse?.isSuccessful == true) {
                        cameraResponse.body() ?: emptyList()
                    } else _uiState.value.cameraList

                    val aiServices = if (aiServiceResponse?.isSuccessful == true) {
                        aiServiceResponse.body() ?: emptyList()
                    } else _uiState.value.aiServices

                    val finalEvents = if (newEvents.isNotEmpty() || cachedData == null) {
                        lastFetchedRawId = nextLastId
                        newEvents
                    } else {
                        _uiState.value.events
                    }

                    EventCacheManager.cacheInMemory(
                        events = finalEvents,
                        cameras = cameraList,
                        aiServices = aiServices
                    )

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            cameraList = cameraList,
                            aiServices = aiServices,
                            events = finalEvents,
                            hasMore = hasMore
                        )
                    }

                    if (newEvents.isNotEmpty()) {
                        val freshCachedData = CachedEventData(
                            events = newEvents,
                            cameraList = cameraList,
                            aiServices = aiServices,
                            updatedAt = System.currentTimeMillis()
                        )
                        EventCacheManager.saveEventData(
                            context = getApplication(),
                            filter = filter,
                            serviceId = serviceId,
                            aiType = aiType,
                            data = freshCachedData,
                            cameraUuid = cameraUuid
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error fetching initial events", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = if (cachedData == null) e.toUserFriendlyMessage() else null
                    )
                }
            }
        }
    }

    fun loadMoreEvents() {
        val currentState = _uiState.value
        if (currentState.isLoading || currentState.isPaginating || !currentState.hasMore || currentState.events.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isPaginating = true, error = null) }
            try {
                val startId = lastFetchedRawId ?: currentState.events.last().id
                val (from, to) = getTimeRange()
                val resolvedType = resolveApiEventType(currentState.selectedAiType)
                val (newEvents, nextLastId, hasMore) = fetchPermittedEventBatch(
                    startLastId = startId,
                    fromSec = from,
                    toSec = to,
                    serviceId = currentState.selectedServiceId,
                    cameraUuid = currentState.selectedCameraUuid,
                    resolvedType = resolvedType,
                    targetCount = 20
                )
                lastFetchedRawId = nextLastId
                if (newEvents.isNotEmpty()) {
                    EventCacheManager.cacheInMemory(events = newEvents)
                }

                _uiState.update {
                    it.copy(
                        isPaginating = false,
                        events = (it.events + newEvents).distinctBy { ev -> eventUniqueKey(ev) },
                        hasMore = hasMore
                    )
                }
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error loading more events", e)
                _uiState.update { it.copy(isPaginating = false, error = e.toUserFriendlyMessage()) }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as com.tenli.oneview.TenliApp
                EventViewModel(application, application.container.webSocketManager)
            }
        }
    }
}
