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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EventDetailUiState(
    val baseEvent: EventData? = null,
    val event: EventData? = null,
    val camera: CameraModel? = null,
    val relatedEvents: List<EventData> = emptyList(),
    val licensePlateEvents: List<EventData> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val originalVideoUrl: String? = null,
    val originalVideoSeekTimeMs: Long? = null,
    val originalVideoEventPositionMs: Long? = null,
    val originalVideoLoading: Boolean = false,
    val originalVideoError: String? = null,
    val aiServices: List<com.tenli.oneview.model.network.AIServiceModel> = emptyList()
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
                // Find event in cache across all cache files
                var foundEvent: EventData? = null
                var cameraList: List<CameraModel> = emptyList()

                val cacheResult = EventCacheManager.findEventById(getApplication(), eventId)
                if (cacheResult != null) {
                    foundEvent = cacheResult.first
                    cameraList = cacheResult.second
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

                val aiServiceResponse = eventApi.getAIServiceList()
                val aiServices = if (aiServiceResponse.isSuccessful) aiServiceResponse.body() ?: emptyList() else emptyList()

                _uiState.update { 
                    it.copy(
                        baseEvent = foundEvent,
                        event = foundEvent,
                        camera = camera,
                        aiServices = aiServices,
                        isLoading = false
                    )
                }
                
                loadRelatedEvents()

            } catch (e: Exception) {
                Log.e("EventDetailVM", "Error loading event", e)
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    fun selectEvent(event: EventData) {
        _uiState.update { 
            it.copy(
                event = event,
                originalVideoUrl = null,
                originalVideoLoading = false,
                originalVideoError = null
            ) 
        }
    }

    fun selectBaseEvent(event: EventData) {
        _uiState.update { 
            it.copy(
                baseEvent = event,
                event = event,
                originalVideoUrl = null,
                originalVideoLoading = false,
                originalVideoError = null
            ) 
        }
        loadRelatedEvents()
    }

    fun loadRelatedEvents() {
        viewModelScope.launch {
            _uiState.value.baseEvent?.let { event ->
                event.data?.cameraUUID?.let { uuid ->
                    fetchRelatedEvents(uuid, event.time, event.serviceId, event.type)
                }
                fetchLicensePlateEvents(event.time, event.data?.cameraUUID)
            }
        }
    }

    private suspend fun fetchRelatedEvents(cameraUUID: String, eventTime: Double, serviceId: Int?, type: String?) {
        try {
            // Lấy khoảng thời gian trong cùng ngày của sự kiện
            val calendar = java.util.Calendar.getInstance()
            calendar.timeInMillis = (eventTime * 1000).toLong()
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            val fromTime = calendar.timeInMillis / 1000

            calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
            calendar.set(java.util.Calendar.MINUTE, 59)
            calendar.set(java.util.Calendar.SECOND, 59)
            val toTime = calendar.timeInMillis / 1000

            val response = eventApi.getDataList(count = 20, from = fromTime, to = toTime, cameraUUID = cameraUUID, serviceId = serviceId, type = type)
            if (response.isSuccessful) {
                val events = response.body() ?: emptyList()
                _uiState.update { it.copy(relatedEvents = events) }
            }
        } catch (e: Exception) {
            Log.e("EventDetailVM", "Error fetching related events", e)
        }
    }

    private suspend fun fetchLicensePlateEvents(eventTime: Double, cameraUUID: String?) {
        try {
            val fromTime = (eventTime - 10).toLong()
            val toTime = (eventTime + 10).toLong()

            val response = eventApi.getDataList(
                count = 20,
                from = fromTime,
                to = toTime,
                cameraUUID = cameraUUID,
                type = "sensor-license-plate"
            )
            if (response.isSuccessful) {
                val events = response.body() ?: emptyList()
                _uiState.update { it.copy(licensePlateEvents = events) }
            }
        } catch (e: Exception) {
            Log.e("EventDetailVM", "Error fetching license plate events", e)
        }
    }

    fun loadOriginalVideo() {
        val event = _uiState.value.event ?: return
        val camera = _uiState.value.camera ?: return
        
        if (_uiState.value.originalVideoUrl != null || _uiState.value.originalVideoLoading) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(originalVideoLoading = true, originalVideoError = null) }
            try {
                val eventTimeMs = (event.time * 1000).toLong()
                val fromMs = eventTimeMs - 61 * 1000
                val toMs = eventTimeMs

                val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", java.util.Locale.getDefault())
                val fromDate = dateFormat.format(java.util.Date(fromMs))
                val toDate = dateFormat.format(java.util.Date(toMs))

                val response = vmsApi.getVideoList(
                    camera = camera.id,
                    count = 1,
                    from = fromDate,
                    to = toDate
                )
                if (response.isSuccessful) {
                    val videos = response.body()
                    if (!videos.isNullOrEmpty() && videos.first().videoLink.isNotEmpty()) {
                        val videoItem = videos.first()
                        
                        val videoStartMs = dateFormat.parse(videoItem.time)?.time ?: 0L
                        val diffSeconds = (eventTimeMs - videoStartMs) / 1000
                        val seekSeconds = java.lang.Math.max(0, java.lang.Math.min(61, diffSeconds - 10))
                        
                        _uiState.update {
                            it.copy(
                                originalVideoUrl = videoItem.videoLink,
                                originalVideoSeekTimeMs = seekSeconds * 1000,
                                originalVideoEventPositionMs = diffSeconds * 1000,
                                originalVideoLoading = false
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                originalVideoLoading = false,
                                originalVideoError = "Video không khả dụng"
                            )
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            originalVideoLoading = false,
                            originalVideoError = "Lỗi khi lấy video gốc"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("EventDetailVM", "Error loading original video", e)
                _uiState.update {
                    it.copy(
                        originalVideoLoading = false,
                        originalVideoError = "Lỗi khi tải video"
                    )
                }
            }
        }
    }

    fun reportEventAsFalse(eventId: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = eventApi.verifyEvent(eventId, com.tenli.oneview.model.network.Confirm(false))
                if (response.isSuccessful) {
                    com.tenli.oneview.util.EventBus.emitEventReportedFalse(eventId)
                    onSuccess()
                } else {
                    onError("Lỗi khi báo cáo: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("EventDetailVM", "Error reporting event", e)
                onError("Lỗi kết nối khi báo cáo")
            }
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
