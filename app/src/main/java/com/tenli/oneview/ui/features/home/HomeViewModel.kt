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
        HomeUiState(
            isListView = GlobalData.preferences.getString("mobile_event_view_mode", "list") != "grid"
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

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
            webSocketManager.cameraStatusMap.collect { statusMap ->
                _uiState.update { it.copy(cameraStatusMap = statusMap) }
            }
        }
        viewModelScope.launch {
            webSocketManager.recordStatusMap.collect { recMap ->
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
        viewModelScope.launch {
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
        if (_uiState.value.selectedFilter == filter) return
        _uiState.update { it.copy(selectedFilter = filter) }
        fetchDashboardData()
    }

    fun setServiceFilter(serviceId: Int?) {
        if (_uiState.value.selectedServiceId == serviceId) return
        _uiState.update { it.copy(selectedServiceId = serviceId) }
        fetchDashboardData()
    }

    fun applyFilters(filter: TimeFilter, serviceId: Int?) {
        if (_uiState.value.selectedFilter == filter && _uiState.value.selectedServiceId == serviceId) return
        _uiState.update { it.copy(selectedFilter = filter, selectedServiceId = serviceId) }
        fetchDashboardData()
    }

    fun resetFilters() {
        applyFilters(TimeFilter.TODAY, null)
    }

    fun fetchDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

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
                            .filter { ev -> AiTypeHelper.isAiTypeEnabled(ev.type) }
                            .take(5)
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

                val overviewResponse = bsApi.getVmsCountOverview(
                    serviceId = _uiState.value.selectedServiceId?.toString() ?: "-1",
                    fromTime = fromTime,
                    toTime = toTime
                )
                val overviewStats = if (overviewResponse.isSuccessful) {
                    overviewResponse.body() ?: emptyList()
                } else emptyList()

                val overTimeResponse = bsApi.getVmsEventStatsOverTime(
                    fromTime = fromTime,
                    toTime = toTime,
                    serviceId = _uiState.value.selectedServiceId?.toString()
                )
                val eventsOverTime = if (overTimeResponse.isSuccessful) {
                    overTimeResponse.body() ?: emptyList()
                } else emptyList()

                val byTypeResponse = bsApi.getVmsEventCountByType(
                    fromTime = fromTime,
                    toTime = toTime,
                    serviceId = _uiState.value.selectedServiceId?.toString()
                )
                val eventsByType = if (byTypeResponse.isSuccessful) {
                    byTypeResponse.body() ?: emptyList()
                } else emptyList()

                val byCameraResponse = bsApi.getVmsEventCountByCamera(
                    fromTime = fromTime,
                    toTime = toTime,
                    serviceId = _uiState.value.selectedServiceId?.toString()
                )
                val eventsByCamera = if (byCameraResponse.isSuccessful) {
                    byCameraResponse.body() ?: emptyList()
                } else emptyList()

                val recentEvents = fetchPermittedRecentEvents(
                    fromSec = fromTime / 1000,
                    toSec = toTime / 1000,
                    serviceId = _uiState.value.selectedServiceId
                )

                val cameraResponse = vmsApi.getCameraList()
                val cameraList = if (cameraResponse.isSuccessful) {
                    cameraResponse.body() ?: emptyList()
                } else emptyList()

                val aiServiceResponse = eventApi.getAIServiceList()
                val aiServices = if (aiServiceResponse.isSuccessful) {
                    aiServiceResponse.body() ?: emptyList()
                } else emptyList()

                // Compute running AI tasks & total AI tasks matching MobileHomePage.vue
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
                        for (srv in targetServices) {
                            val host = srv.publicHost.ifBlank { srv.privateHost }.trimEnd('/')
                            val monResp = if (host.isNotBlank() && srv.id != 0) {
                                aiApi.getMonitorListExt(
                                    url = "$host/api/Monitor",
                                    authHeader = "Bearer ${srv.apiKey}"
                                )
                            } else {
                                aiApi.getMonitorList()
                            }
                            if (monResp.isSuccessful) {
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
                if (!overviewResponse.isSuccessful) errorMsg = "Lỗi Overview: ${overviewResponse.code()}"

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
                        runningAiTasks = runningTasks,
                        totalAiTasks = totalTasks,
                        error = errorMsg
                    )
                }

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
