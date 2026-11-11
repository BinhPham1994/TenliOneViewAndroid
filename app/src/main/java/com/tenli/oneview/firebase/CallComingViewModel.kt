package com.tenli.oneview.firebase

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tenli.oneview.TenliApp
import com.tenli.oneview.data.repository.EventRepository
import com.tenli.oneview.model.network.AIServiceModel
import com.tenli.oneview.model.network.BsProfileGroupModel
import com.tenli.oneview.model.network.BsProfileModel
import com.tenli.oneview.model.network.CameraModel
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.ui.features.event.resolveCameraForEvent
import com.tenli.oneview.ui.utils.AiTypeHelper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Dữ liệu cuộc gọi cảnh báo nhận từ EventMessagingService */
@Immutable
data class IncomingCall(
    val title: String = "",
    val body: String = "",
    val imageUrl: String? = null,
    val eventId: String = "",
    val notificationId: Int = 0,
    val eventType: String = "",
    val cameraUuid: String = "",
    val cameraName: String = "",
    val eventTime: Double = 0.0
)

/** Trạng thái UI của màn hình cuộc gọi cảnh báo */
@Immutable
data class CallComingUiState(
    val call: IncomingCall = IncomingCall(),
    val enrichedEvent: EventData? = null,
    val enrichedCamera: CameraModel? = null,
    val enrichedAiService: AIServiceModel? = null,
    val enrichedProfiles: List<BsProfileModel> = emptyList(),
    val enrichedGroups: List<BsProfileGroupModel> = emptyList()
)

class CallComingViewModel(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CallComingUiState())
    val uiState: StateFlow<CallComingUiState> = _uiState.asStateFlow()

    private var eventFetchJob: Job? = null

    /**
     * Nhận dữ liệu cuộc gọi mới và tải đầy đủ chi tiết sự kiện (`EventData`, camera, hệ thống AI,
     * hồ sơ khuôn mặt/đồng phục) từ cache hoặc API thông qua [EventRepository].
     */
    fun onIncomingCall(incoming: IncomingCall) {
        eventFetchJob?.cancel()
        val memCameras = eventRepository.getCamerasInMemory()
        val memServices = eventRepository.getAiServicesInMemory()
        val memProfiles = eventRepository.getProfilesInMemory()
        val memGroups = eventRepository.getProfileGroupsInMemory()

        var initialCamera = if (incoming.cameraUuid.isNotBlank()) {
            memCameras.find { it.extra?.uuid.equals(incoming.cameraUuid, ignoreCase = true) }
        } else {
            null
        }
        var initialEvent: EventData? = null
        var initialService: AIServiceModel? = null

        val eventIdInt = incoming.eventId.trim().toIntOrNull()
        if (eventIdInt != null) {
            eventRepository.getEventInMemory(eventIdInt)?.let { cachedEv ->
                initialEvent = cachedEv
                initialCamera = resolveCameraForEvent(cachedEv, memCameras) ?: initialCamera
                initialService = memServices.find { it.id == cachedEv.serviceId }
            }
        }

        _uiState.value = CallComingUiState(
            call = incoming,
            enrichedEvent = initialEvent,
            enrichedCamera = initialCamera,
            enrichedAiService = initialService,
            enrichedProfiles = memProfiles,
            enrichedGroups = memGroups
        )

        if (eventIdInt == null) return

        eventFetchJob = viewModelScope.launch {
            runCatching {
                var foundEvent = initialEvent
                var cameraList = memCameras

                if (foundEvent == null) {
                    val diskPair = withContext(Dispatchers.IO) {
                        eventRepository.findEventById(eventIdInt)
                    }
                    if (diskPair != null) {
                        foundEvent = diskPair.first
                        if (diskPair.second.isNotEmpty()) {
                            cameraList = diskPair.second
                        }
                    }
                }

                if (foundEvent == null || foundEvent.data?.video.isNullOrBlank()) {
                    val resp = withContext(Dispatchers.IO) {
                        runCatching { eventRepository.getDataList(lastId = eventIdInt + 1, count = 5) }
                            .onFailure { if (it is CancellationException) throw it }
                            .getOrNull()
                    }
                    val fetched = resp?.body()?.firstOrNull { it.id == eventIdInt }
                    if (fetched != null) {
                        foundEvent = fetched
                    }
                }

                val targetEvent = foundEvent ?: return@runCatching
                val needProfiles = AiTypeHelper.isFaceEvent(targetEvent) ||
                    targetEvent.type.lowercase().contains("uniform")

                val resolvedCamInitial = resolveCameraForEvent(targetEvent, cameraList) ?: _uiState.value.enrichedCamera
                val resolvedSvcInitial = memServices.find { it.id == targetEvent.serviceId }
                _uiState.update {
                    it.copy(
                        enrichedEvent = targetEvent,
                        enrichedCamera = resolvedCamInitial,
                        enrichedAiService = resolvedSvcInitial
                    )
                }

                val fetchCameras = cameraList.isEmpty()
                val fetchServices = memServices.isEmpty()
                val fetchProfiles = needProfiles && memProfiles.isEmpty()
                val fetchGroups = needProfiles && memGroups.isEmpty()

                var latestCameras = cameraList
                var latestServices = memServices

                if (fetchCameras || fetchServices || fetchProfiles || fetchGroups) {
                    coroutineScope {
                        val camDef = async(Dispatchers.IO) {
                            if (fetchCameras) {
                                runCatching { eventRepository.getCameraList() }.getOrNull()?.body() ?: emptyList()
                            } else cameraList
                        }
                        val svcDef = async(Dispatchers.IO) {
                            if (fetchServices) {
                                runCatching { eventRepository.getAIServiceList() }.getOrNull()?.body() ?: emptyList()
                            } else memServices
                        }
                        val profDef = async(Dispatchers.IO) {
                            if (fetchProfiles) {
                                runCatching { eventRepository.getProfileList() }.getOrNull()?.body() ?: emptyList()
                            } else memProfiles
                        }
                        val grpDef = async(Dispatchers.IO) {
                            if (fetchGroups) {
                                runCatching { eventRepository.getProfileGroupList() }.getOrNull()?.body() ?: emptyList()
                            } else memGroups
                        }

                        val finalCameras = camDef.await()
                        val finalServices = svcDef.await()
                        val finalProfiles = profDef.await()
                        val finalGroups = grpDef.await()

                        latestCameras = finalCameras
                        latestServices = finalServices

                        eventRepository.cacheInMemory(
                            events = listOf(targetEvent),
                            cameras = finalCameras,
                            aiServices = finalServices
                        )
                        _uiState.update { state ->
                            state.copy(
                                enrichedCamera = resolveCameraForEvent(targetEvent, finalCameras) ?: state.enrichedCamera,
                                enrichedAiService = finalServices.find { it.id == targetEvent.serviceId },
                                enrichedProfiles = finalProfiles,
                                enrichedGroups = finalGroups
                            )
                        }
                    }
                } else {
                    eventRepository.cacheInMemory(
                        events = listOf(targetEvent),
                        cameras = cameraList,
                        aiServices = memServices
                    )
                }

                // Nếu ngay lúc nhận thông báo FCM, server AI đang hoàn tất ghi file .mp4 của sự kiện,
                // kiểm tra lại 1 lần sau 1.8s để tự động hiển thị tab "Video sự kiện" ngay khi có.
                if (targetEvent.data?.video.isNullOrBlank()) {
                    delay(1800L)
                    val retryResp = withContext(Dispatchers.IO) {
                        runCatching { eventRepository.getDataList(lastId = eventIdInt + 1, count = 5) }
                            .onFailure { if (it is CancellationException) throw it }
                            .getOrNull()
                    }
                    val refreshed = retryResp?.body()?.firstOrNull { it.id == eventIdInt }
                    if (refreshed != null && !refreshed.data?.video.isNullOrBlank()) {
                        _uiState.update { it.copy(enrichedEvent = refreshed) }
                        eventRepository.cacheInMemory(
                            events = listOf(refreshed),
                            cameras = latestCameras,
                            aiServices = latestServices
                        )
                    }
                }
            }.onFailure { e ->
                if (e is CancellationException) throw e
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TenliApp
                CallComingViewModel(app.container.eventRepository)
            }
        }
    }
}

