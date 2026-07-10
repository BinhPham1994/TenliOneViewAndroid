package com.tenli.oneview.ui.features.event

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenli.oneview.data.network.api.EventApi
import com.tenli.oneview.data.network.api.VmsApi
import com.tenli.oneview.data.network.retrofit.LoginAuthClient
import com.tenli.oneview.model.network.CameraModel
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.ui.features.home.TimeFilter
import com.tenli.oneview.model.network.AIServiceModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.coroutineScope
import com.tenli.oneview.util.toUserFriendlyMessage
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.FlowPreview
import java.util.Calendar
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tenli.oneview.data.local.CachedEventData
import com.tenli.oneview.data.local.EventCacheManager
data class EventScreenUiState(
    val events: List<EventData> = emptyList(),
    val cameraList: List<CameraModel> = emptyList(),
    val aiServices: List<AIServiceModel> = emptyList(),
    val selectedFilter: TimeFilter = TimeFilter.TODAY,
    val selectedServiceId: Int? = null,
    val selectedAiType: String? = null,
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

    private val _uiState = MutableStateFlow(EventScreenUiState())
    val uiState: StateFlow<EventScreenUiState> = _uiState.asStateFlow()

    init {
        fetchInitialData()
        listenToWebSocket()
    }

    private fun listenToWebSocket() {
        viewModelScope.launch {
            android.util.Log.d("EventViewModel", "Started listening to notifyEvent flow")
            webSocketManager.notifyEvent
                .debounce(600L)
                .collect { _ ->
                    android.util.Log.d("EventViewModel", "notifyEvent debounced → filter=${_uiState.value.selectedFilter}")
                    // Chỉ cập nhật nếu đang ở filter TODAY
                    if (_uiState.value.selectedFilter == TimeFilter.TODAY) {
                        fetchRecentEvents()
                    }
                }
        }
    }

    private fun fetchRecentEvents() {
        viewModelScope.launch {
            try {
                val (from, to) = getTimeRange()
                val response = eventApi.getDataList(
                    count = 5,
                    from = from,
                    to = to,
                    serviceId = _uiState.value.selectedServiceId,
                    type = _uiState.value.selectedAiType
                )
                if (response.isSuccessful) {
                    val newEvents = response.body() ?: emptyList()
                    _uiState.update { state ->
                        // Hợp nhất (merge) để tránh trùng lặp
                        val merged = (newEvents + state.events)
                            .distinctBy { it.id }
                        state.copy(events = merged)
                    }
                }
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error fetching recent events via WebSocket", e)
            }
        }
    }

    fun applyFilters(filter: TimeFilter, serviceId: Int?, aiType: String?) {
        if (_uiState.value.selectedFilter == filter && 
            _uiState.value.selectedServiceId == serviceId &&
            _uiState.value.selectedAiType == aiType) return
        _uiState.update { it.copy(selectedFilter = filter, selectedServiceId = serviceId, selectedAiType = aiType) }
        fetchInitialData()
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
            _uiState.update { it.copy(isLoading = true, error = null, hasMore = true) }
            
            // Try to load from cache first
            val filter = _uiState.value.selectedFilter
            val serviceId = _uiState.value.selectedServiceId
            val aiType = _uiState.value.selectedAiType
            val cachedData = EventCacheManager.getEventData(getApplication(), filter, serviceId, aiType)
            if (cachedData != null) {
                _uiState.update {
                    it.copy(
                        cameraList = cachedData.cameraList,
                        events = cachedData.events,
                        hasMore = cachedData.events.size >= 20 // Fallback
                    )
                }
            }

            try {
                kotlinx.coroutines.coroutineScope {
                    // Fetch cameras and AI Services first if needed, or in parallel
                    val cameraDeferred = async { vmsApi.getCameraList() }
                    val aiServiceDeferred = async { eventApi.getAIServiceList() }
                    
                    val cameraResponse = cameraDeferred.await()
                    val aiServiceResponse = aiServiceDeferred.await()

                    val cameraList = if (cameraResponse.isSuccessful) {
                        cameraResponse.body() ?: emptyList()
                    } else emptyList()

                    val aiServices = if (aiServiceResponse.isSuccessful) {
                        aiServiceResponse.body() ?: emptyList()
                    } else _uiState.value.aiServices

                    val (from, to) = getTimeRange()
                    val response = eventApi.getDataList(count = 20, from = from, to = to, serviceId = serviceId, type = aiType)
                    val newEvents = if (response.isSuccessful) {
                        response.body() ?: emptyList()
                    } else emptyList()

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            cameraList = cameraList,
                            aiServices = aiServices,
                            events = newEvents,
                            hasMore = newEvents.size == 20
                        )
                    }

                    // Save to cache on success
                    if (response.isSuccessful && cameraResponse.isSuccessful) {
                        val freshCachedData = CachedEventData(events = newEvents, cameraList = cameraList)
                        EventCacheManager.saveEventData(getApplication(), filter, serviceId, aiType, freshCachedData)
                    }
                }
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error fetching initial events", e)
                _uiState.update { it.copy(isLoading = false, error = e.toUserFriendlyMessage()) }
            }
        }
    }

    fun loadMoreEvents() {
        val currentState = _uiState.value
        if (currentState.isLoading || currentState.isPaginating || !currentState.hasMore || currentState.events.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isPaginating = true, error = null) }
            try {
                val lastId = currentState.events.last().id
                val (from, to) = getTimeRange()
                val response = eventApi.getDataList(lastId = lastId, count = 20, from = from, to = to, serviceId = currentState.selectedServiceId, type = currentState.selectedAiType)
                
                if (response.isSuccessful) {
                    val newEvents = response.body() ?: emptyList()
                    _uiState.update {
                        it.copy(
                            isPaginating = false,
                            events = it.events + newEvents,
                            hasMore = newEvents.size == 20
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(isPaginating = false, error = "Lỗi tải thêm: ${response.code()}")
                    }
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
