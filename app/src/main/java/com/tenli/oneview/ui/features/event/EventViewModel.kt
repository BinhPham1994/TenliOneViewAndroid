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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EventScreenUiState(
    val events: List<EventData> = emptyList(),
    val cameraList: List<CameraModel> = emptyList(),
    val selectedFilter: TimeFilter = TimeFilter.TODAY,
    val isLoading: Boolean = false,
    val isPaginating: Boolean = false,
    val hasMore: Boolean = true,
    val error: String? = null
)

class EventViewModel : ViewModel() {
    private val eventApi = LoginAuthClient.create(EventApi::class.java)
    private val vmsApi = LoginAuthClient.create(VmsApi::class.java)

    private val _uiState = MutableStateFlow(EventScreenUiState())
    val uiState: StateFlow<EventScreenUiState> = _uiState.asStateFlow()

    init {
        fetchInitialData()
    }

    fun setTimeFilter(filter: TimeFilter) {
        if (_uiState.value.selectedFilter == filter) return
        _uiState.update { it.copy(selectedFilter = filter) }
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
            try {
                // Fetch cameras first if needed, or in parallel
                val cameraResponse = vmsApi.getCameraList()
                val cameraList = if (cameraResponse.isSuccessful) {
                    cameraResponse.body() ?: emptyList()
                } else emptyList()

                val (from, to) = getTimeRange()
                val response = eventApi.getDataList(count = 20, from = from, to = to)
                val newEvents = if (response.isSuccessful) {
                    response.body() ?: emptyList()
                } else emptyList()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        cameraList = cameraList,
                        events = newEvents,
                        hasMore = newEvents.size == 20
                    )
                }
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error fetching initial events", e)
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
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
                val response = eventApi.getDataList(lastId = lastId, count = 20, from = from, to = to)
                
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
                _uiState.update { it.copy(isPaginating = false, error = e.localizedMessage) }
            }
        }
    }
}
