package com.tenli.oneview.ui.features.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenli.oneview.data.network.api.BsApi
import com.tenli.oneview.data.network.api.EventApi
import com.tenli.oneview.data.network.retrofit.LoginAuthClient
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.model.network.VmsCountOverviewModel
import com.tenli.oneview.model.network.VmsEventCountByCameraModel
import com.tenli.oneview.model.network.VmsEventCountByTypeModel
import com.tenli.oneview.model.network.VmsEventStatisticalOverTimeModel
import com.tenli.oneview.data.network.api.VmsApi
import com.tenli.oneview.model.network.CameraModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val overviewStats: List<VmsCountOverviewModel> = emptyList(),
    val eventsOverTime: List<VmsEventStatisticalOverTimeModel> = emptyList(),
    val eventsByType: List<VmsEventCountByTypeModel> = emptyList(),
    val eventsByCamera: List<VmsEventCountByCameraModel> = emptyList(),
    val cameraList: List<CameraModel> = emptyList(),
    val recentEvents: List<EventData> = emptyList(),
    val error: String? = null
)

class HomeViewModel : ViewModel() {

    private val bsApi = LoginAuthClient.create(BsApi::class.java)
    private val eventApi = LoginAuthClient.create(EventApi::class.java)
    private val vmsApi = LoginAuthClient.create(VmsApi::class.java)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        fetchDashboardData()
    }

    fun setTimeFilter(filter: TimeFilter) {
        if (_uiState.value.selectedFilter == filter) return
        _uiState.update { it.copy(selectedFilter = filter) }
        fetchDashboardData()
    }

    fun fetchDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val calendar = Calendar.getInstance()
                val toTime: Long
                val fromTime: Long
                
                when (_uiState.value.selectedFilter) {
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
                    serviceId = "-1",
                    fromTime = fromTime,
                    toTime = toTime
                )
                val overviewStats = if (overviewResponse.isSuccessful) {
                    overviewResponse.body() ?: emptyList()
                } else emptyList()

                // Phân bố sự kiện theo thời gian
                val overTimeResponse = bsApi.getVmsEventStatsOverTime(
                    fromTime = fromTime,
                    toTime = toTime
                )
                val eventsOverTime = if (overTimeResponse.isSuccessful) {
                    overTimeResponse.body() ?: emptyList()
                } else emptyList()

                // Phân bố sự kiện theo bài AI
                val byTypeResponse = bsApi.getVmsEventCountByType(
                    fromTime = fromTime,
                    toTime = toTime
                )
                val eventsByType = if (byTypeResponse.isSuccessful) {
                    byTypeResponse.body() ?: emptyList()
                } else emptyList()

                // Phân bố sự kiện theo camera
                val byCameraResponse = bsApi.getVmsEventCountByCamera(
                    fromTime = fromTime,
                    toTime = toTime
                )
                val eventsByCamera = if (byCameraResponse.isSuccessful) {
                    byCameraResponse.body() ?: emptyList()
                } else emptyList()

                // Sự kiện gần đây
                val recentResponse = eventApi.getDataList(
                    count = 5,
                    from = fromTime / 1000,
                    to = toTime / 1000
                )
                val recentEvents = if (recentResponse.isSuccessful) {
                    recentResponse.body() ?: emptyList()
                } else emptyList()

                // Danh sách Camera
                val cameraResponse = vmsApi.getCameraList()
                val cameraList = if (cameraResponse.isSuccessful) {
                    cameraResponse.body() ?: emptyList()
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
                        recentEvents = recentEvents,
                        error = errorMsg
                    )
                }

            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching data", e)
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }
}
