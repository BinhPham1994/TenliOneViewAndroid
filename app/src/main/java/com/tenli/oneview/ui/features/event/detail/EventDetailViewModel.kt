package com.tenli.oneview.ui.features.event.detail

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tenli.oneview.data.local.EventCacheManager
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

data class EventDetailUiState(
    val event: EventData? = null,
    val camera: CameraModel? = null,
    val relatedEvents: List<EventData> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class EventDetailViewModel(
    application: Application,
    private val eventId: Int
) : AndroidViewModel(application) {

    private val eventApi = LoginAuthClient.create(EventApi::class.java)
    private val vmsApi = LoginAuthClient.create(VmsApi::class.java)

    private val _uiState = MutableStateFlow(EventDetailUiState())
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()

    init {
        loadEventDetails()
    }

    private fun loadEventDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Find event in cache across all time filters
                var foundEvent: EventData? = null
                var cameraList: List<CameraModel> = emptyList()

                for (filter in TimeFilter.values()) {
                    val cache = EventCacheManager.getEventData(getApplication(), filter)
                    if (cache != null) {
                        foundEvent = cache.events.find { it.id == eventId }
                        if (cameraList.isEmpty() && cache.cameraList.isNotEmpty()) {
                            cameraList = cache.cameraList
                        }
                        if (foundEvent != null) break
                    }
                }

                if (foundEvent == null) {
                    // Try fetching from API directly
                    val response = eventApi.getDataList(lastId = eventId + 1, count = 1)
                    if (response.isSuccessful) {
                        val events = response.body() ?: emptyList()
                        if (events.isNotEmpty() && events.first().id == eventId) {
                            foundEvent = events.first()
                        }
                    }
                }

                if (foundEvent == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Không tìm thấy sự kiện") }
                    return@launch
                }

                if (cameraList.isEmpty()) {
                    val camResponse = vmsApi.getCameraList()
                    if (camResponse.isSuccessful) {
                        cameraList = camResponse.body() ?: emptyList()
                    }
                }

                val camera = cameraList.find { it.extra?.uuid == foundEvent?.data?.cameraUUID }

                _uiState.update { 
                    it.copy(
                        event = foundEvent,
                        camera = camera,
                        isLoading = false
                    )
                }

                // Fetch related events for "Hành trình tại vị trí"
                foundEvent.data?.cameraUUID?.let { uuid ->
                    fetchRelatedEvents(uuid)
                }

            } catch (e: Exception) {
                Log.e("EventDetailVM", "Error loading event", e)
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    private suspend fun fetchRelatedEvents(cameraUUID: String) {
        try {
            // Get today range
            val calendar = java.util.Calendar.getInstance()
            val toTime = calendar.timeInMillis / 1000
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            val fromTime = calendar.timeInMillis / 1000

            val response = eventApi.getDataList(count = 20, from = fromTime, to = toTime, cameraUUID = cameraUUID)
            if (response.isSuccessful) {
                val events = response.body() ?: emptyList()
                _uiState.update { it.copy(relatedEvents = events) }
            }
        } catch (e: Exception) {
            Log.e("EventDetailVM", "Error fetching related events", e)
        }
    }

    companion object {
        fun provideFactory(eventId: Int): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                EventDetailViewModel(application, eventId)
            }
        }
    }
}
