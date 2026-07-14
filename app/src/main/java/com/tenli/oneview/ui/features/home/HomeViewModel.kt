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
import com.tenli.oneview.data.local.HomeCacheManager
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
import com.tenli.oneview.util.toUserFriendlyMessage
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

enum class TimeFilter(val title: String) {
    TODAY("Hôm nay"),
    YESTERDAY("Hôm qua"),
    LAST_7_DAYS("7 ngày qua"),
    LAST_30_DAYS("30 ngày qua")
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

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        fetchDashboardData()
        listenToWebSocket()
    }

    private fun listenToWebSocket() {
        viewModelScope.launch {
            Log.d("HomeViewModel", "Started listening to notifyEvent flow")
            webSocketManager.notifyEvent
                .debounce(600L)
                .collect { _ ->
                    Log.d("HomeViewModel", "notifyEvent debounced → filter=${_uiState.value.selectedFilter}")
                    // Chỉ cập nhật nếu đang ở filter TODAY
                    if (_uiState.value.selectedFilter == TimeFilter.TODAY) {
                        fetchRecentEvents()
                    }
                }
        }
        viewModelScope.launch {
            webSocketManager.cameraStatusMap.collect { statusMap ->
                _uiState.update { it.copy(cameraStatusMap = statusMap) }
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

    private fun fetchRecentEvents() {
        viewModelScope.launch {
            try {
                val calendar = Calendar.getInstance()
                val toTime = calendar.timeInMillis
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val fromTime = calendar.timeInMillis

                val recentResponse = eventApi.getDataList(
                    count = 5,
                    from = fromTime / 1000,
                    to = toTime / 1000,
                    serviceId = _uiState.value.selectedServiceId
                )
                if (recentResponse.isSuccessful) {
                    val newEvents = recentResponse.body() ?: emptyList()
                    _uiState.update { state ->
                        // Hợp nhất (merge) để tránh trùng lặp
                        val merged = (newEvents + state.recentEvents)
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
        if (_uiState.value.selectedFilter == filter) return
        _uiState.update { it.copy(selectedFilter = filter) }
        fetchDashboardData()
    }

    fun setServiceFilter(serviceId: Int?) {
        if (_uiState.value.selectedServiceId == serviceId) return
        _uiState.update { it.copy(selectedServiceId = serviceId) }
        fetchDashboardData()
    }

    fun fetchDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // Try to load from cache first
            val filter = _uiState.value.selectedFilter
            val serviceId = _uiState.value.selectedServiceId
            val cachedData = HomeCacheManager.getHomeData(getApplication(), filter, serviceId)
            if (cachedData != null) {
                _uiState.update {
                    it.copy(
                        overviewStats = cachedData.overviewStats,
                        eventsOverTime = cachedData.eventsOverTime,
                        eventsByType = cachedData.eventsByType,
                        eventsByCamera = cachedData.eventsByCamera,
                        cameraList = cachedData.cameraList,
                        aiServices = cachedData.aiServices,
                        recentEvents = cachedData.recentEvents
                    )
                }
            }

            try {
                val calendar = Calendar.getInstance()
                val toTime: Long
                val fromTime: Long
                
                when (filter) {
                    TimeFilter.TODAY -> {
                        toTime = calendar.timeInMillis
                        calendar.set(Calendar.HOUR_OF_DAY, 0)
                        calendar.set(Calendar.MINUTE, 0)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        fromTime = calendar.timeInMillis
                    }
                    TimeFilter.YESTERDAY -> {
                        calendar.set(Calendar.HOUR_OF_DAY, 0)
                        calendar.set(Calendar.MINUTE, 0)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        toTime = calendar.timeInMillis - 1
                        calendar.add(Calendar.DAY_OF_YEAR, -1)
                        fromTime = calendar.timeInMillis
                    }
                    TimeFilter.LAST_7_DAYS -> {
                        toTime = calendar.timeInMillis
                        calendar.add(Calendar.DAY_OF_YEAR, -7)
                        fromTime = calendar.timeInMillis
                    }
                    TimeFilter.LAST_30_DAYS -> {
                        toTime = calendar.timeInMillis
                        calendar.add(Calendar.DAY_OF_YEAR, -30)
                        fromTime = calendar.timeInMillis
                    }
                }

                // Lấy dữ liệu Overview
                val overviewResponse = bsApi.getVmsCountOverview(
                    serviceId = _uiState.value.selectedServiceId?.toString() ?: "-1",
                    fromTime = fromTime,
                    toTime = toTime
                )
                val overviewStats = if (overviewResponse.isSuccessful) {
                    overviewResponse.body() ?: emptyList()
                } else emptyList()

                // Phân bố sự kiện theo thời gian
                val overTimeResponse = bsApi.getVmsEventStatsOverTime(
                    fromTime = fromTime,
                    toTime = toTime,
                    serviceId = _uiState.value.selectedServiceId?.toString()
                )
                val eventsOverTime = if (overTimeResponse.isSuccessful) {
                    overTimeResponse.body() ?: emptyList()
                } else emptyList()

                // Phân bố sự kiện theo bài AI
                val byTypeResponse = bsApi.getVmsEventCountByType(
                    fromTime = fromTime,
                    toTime = toTime,
                    serviceId = _uiState.value.selectedServiceId?.toString()
                )
                val eventsByType = if (byTypeResponse.isSuccessful) {
                    byTypeResponse.body() ?: emptyList()
                } else emptyList()

                // Phân bố sự kiện theo camera
                val byCameraResponse = bsApi.getVmsEventCountByCamera(
                    fromTime = fromTime,
                    toTime = toTime,
                    serviceId = _uiState.value.selectedServiceId?.toString()
                )
                val eventsByCamera = if (byCameraResponse.isSuccessful) {
                    byCameraResponse.body() ?: emptyList()
                } else emptyList()

                // Sự kiện gần đây
                val recentResponse = eventApi.getDataList(
                    count = 5,
                    from = fromTime / 1000,
                    to = toTime / 1000,
                    serviceId = _uiState.value.selectedServiceId
                )
                val recentEvents = if (recentResponse.isSuccessful) {
                    recentResponse.body() ?: emptyList()
                } else emptyList()

                // Danh sách Camera
                val cameraResponse = vmsApi.getCameraList()
                val cameraList = if (cameraResponse.isSuccessful) {
                    cameraResponse.body() ?: emptyList()
                } else emptyList()

                // Danh sách dịch vụ AI
                val aiServiceResponse = eventApi.getAIServiceList()
                val aiServices = if (aiServiceResponse.isSuccessful) {
                    aiServiceResponse.body() ?: emptyList()
                } else emptyList()

                var errorMsg: String? = null
                if (!overviewResponse.isSuccessful) errorMsg = "Lỗi Overview: ${overviewResponse.code()}"
                else if (!overTimeResponse.isSuccessful) errorMsg = "Lỗi Biểu đồ Thời gian: ${overTimeResponse.code()}"
                else if (!recentResponse.isSuccessful) errorMsg = "Lỗi Sự kiện: ${recentResponse.code()}"

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        overviewStats = overviewStats,
                        eventsOverTime = eventsOverTime,
                        eventsByType = eventsByType,
                        eventsByCamera = eventsByCamera,
                        cameraList = cameraList,
                        aiServices = aiServices,
                        recentEvents = recentEvents,
                        error = errorMsg
                    )
                }

                // Save fresh data to cache
                if (errorMsg == null) {
                    val newCachedData = CachedHomeData(
                        overviewStats = overviewStats,
                        eventsOverTime = eventsOverTime,
                        eventsByType = eventsByType,
                        eventsByCamera = eventsByCamera,
                        cameraList = cameraList,
                        aiServices = aiServices,
                        recentEvents = recentEvents
                    )
                    HomeCacheManager.saveHomeData(getApplication(), filter, serviceId, newCachedData)
                }

            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching data", e)
                _uiState.update { it.copy(isLoading = false, error = e.toUserFriendlyMessage()) }
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
