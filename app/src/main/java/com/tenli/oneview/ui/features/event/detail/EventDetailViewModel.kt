package com.tenli.oneview.ui.features.event.detail

import android.app.Application
import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tenli.oneview.TenliApp
import com.tenli.oneview.data.local.UserPermissionManager
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.data.repository.EventRepository
import com.tenli.oneview.model.network.AIServiceModel
import com.tenli.oneview.model.network.BsProfileGroupModel
import com.tenli.oneview.model.network.BsProfileModel
import com.tenli.oneview.model.network.CameraModel
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.model.network.LiveStreamModel
import com.tenli.oneview.ui.features.event.cameraUuidOrNull
import com.tenli.oneview.ui.features.event.eventTimeToSeconds
import com.tenli.oneview.ui.features.event.resolveCameraForEvent
import com.tenli.oneview.ui.features.event.uniqueKey
import com.tenli.oneview.ui.utils.AiTypeHelper
import com.tenli.oneview.ui.utils.LocaleManager
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

@Immutable
data class EventDetailUiState(
    val baseEvent: EventData? = null,
    val event: EventData? = null,
    val camera: CameraModel? = null,
    val cameraList: List<CameraModel> = emptyList(),
    val showRelatedPlates: Boolean = false,
    val relatedPlatesLoading: Boolean = false,
    val licensePlateEvents: List<EventData> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val liveStreamUrl: String? = null,
    val liveStreamLoading: Boolean = false,
    val liveStreamError: String? = null,
    val liveStreamChannel: Int = 1,
    val liveCameraSnapshotUrl: String? = null,
    val aiServices: List<AIServiceModel> = emptyList(),
    val profileList: List<BsProfileModel> = emptyList(),
    val profileGroupList: List<BsProfileGroupModel> = emptyList()
)

class EventDetailViewModel(
    application: Application,
    private val eventId: Int,
    private val eventRepository: EventRepository
) : AndroidViewModel(application) {

    private val _uiState: MutableStateFlow<EventDetailUiState> = run {
        val initialEvent = eventRepository.getEventInMemory(eventId)
        val initialCameras = eventRepository.getCamerasInMemory()
        val initialCamera = initialEvent?.let { resolveCameraForEvent(it, initialCameras) }
        val initialSnap = initialCamera?.id?.let { camId ->
            eventRepository.getCachedCameraImageLinkInMemory(camId)
        }

        MutableStateFlow(
            EventDetailUiState(
                baseEvent = initialEvent,
                event = initialEvent,
                camera = initialCamera,
                cameraList = initialCameras,
                liveCameraSnapshotUrl = initialSnap?.let { raw ->
                    val trimmed = raw.trim()
                    if (trimmed.isEmpty() || trimmed == "NO_IMAGE") null
                    else if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("file://") || trimmed.startsWith("data:")) trimmed
                    else {
                        val base = UserSession.domain.trim().trimEnd('/').removeSuffix("/api/v1")
                        if (trimmed.startsWith("/")) "$base$trimmed" else "$base/$trimmed"
                    }
                },
                aiServices = eventRepository.getAiServicesInMemory(),
                profileList = eventRepository.getProfilesInMemory(),
                profileGroupList = eventRepository.getProfileGroupsInMemory(),
                isLoading = initialEvent == null
            )
        )
    }
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()

    private var licensePlateJob: Job? = null
    private var liveStreamJob: Job? = null
    private var liveRetryCount: Int = 0
    private var liveFallbackTried: Boolean = false
    private var pendingLiveStreamOnCameraResolved: Boolean = false

    private val isEnglishLocale: Boolean
        get() = LocaleManager.getLocale(getApplication()).startsWith("en")

    init {
        loadEventDetails()
    }

    /** Khuôn mặt / Đồng phục cần đối chiếu danh sách profile (tên, nhóm). */
    private fun eventNeedsProfiles(event: EventData): Boolean =
        AiTypeHelper.isFaceEvent(event) || event.type.lowercase().contains("uniform")

    private fun loadEventDetails() {
        viewModelScope.launch {
            try {
                var foundEvent = _uiState.value.event
                var cameraList = _uiState.value.cameraList

                if (foundEvent == null) {
                    _uiState.update { it.copy(isLoading = true, error = null) }

                    val cacheResult = eventRepository.findEventById(eventId)
                    if (cacheResult != null) {
                        foundEvent = cacheResult.first
                        if (cacheResult.second.isNotEmpty()) {
                            cameraList = cacheResult.second
                        }
                    }

                    if (foundEvent == null) {
                        val response = eventRepository.getDataList(lastId = eventId + 1, count = 1)
                        if (response.isSuccessful) {
                            val first = response.body()?.firstOrNull()
                            if (first != null && first.id == eventId) {
                                foundEvent = first
                            }
                        }
                    }

                    if (foundEvent == null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = if (isEnglishLocale) "Event not found" else "Không tìm thấy sự kiện"
                            )
                        }
                        return@launch
                    }

                    // Hiển thị ngay lập tức thông tin sự kiện vừa tìm thấy, không đợi các API phụ trợ
                    val resolvedCam = resolveCameraForEvent(foundEvent, cameraList)
                    _uiState.update {
                        it.copy(
                            baseEvent = foundEvent,
                            event = foundEvent,
                            camera = resolvedCam,
                            cameraList = cameraList,
                            isLoading = false
                        )
                    }
                } else {
                    // Khi sự kiện đã có sẵn trong RAM, nhường 100% CPU/GPU cho hiệu ứng trượt mở màn hình
                    // trước khi khởi chạy các API phụ trợ.
                    delay(200L)
                }

                // Tải ngầm các metadata phụ trợ CHƯA có sẵn trong bộ nhớ (không bật isLoading toàn màn hình).
                // Danh sách profile / nhóm chỉ cần cho sự kiện Khuôn mặt / Đồng phục.
                val needProfiles = eventNeedsProfiles(foundEvent)
                val current = _uiState.value
                val fetchCameras = cameraList.isEmpty()
                val fetchAiServices = current.aiServices.isEmpty()
                val fetchProfiles = needProfiles && current.profileList.isEmpty()
                val fetchGroups = needProfiles && current.profileGroupList.isEmpty()

                if (fetchCameras || fetchAiServices || fetchProfiles || fetchGroups) {
                    coroutineScope {
                        val camDeferred = async {
                            if (fetchCameras) {
                                runCatching { eventRepository.getCameraList() }.getOrNull()?.body() ?: emptyList()
                            } else cameraList
                        }
                        val aiSvcDeferred = async {
                            if (fetchAiServices) {
                                runCatching { eventRepository.getAIServiceList() }.getOrNull()?.body() ?: emptyList()
                            } else current.aiServices
                        }
                        val profilesDeferred = async {
                            if (fetchProfiles) {
                                runCatching { eventRepository.getProfileList() }.getOrNull()?.body() ?: emptyList()
                            } else current.profileList
                        }
                        val groupsDeferred = async {
                            if (fetchGroups) {
                                runCatching { eventRepository.getProfileGroupList() }.getOrNull()?.body() ?: emptyList()
                            } else current.profileGroupList
                        }

                        val finalCameras = camDeferred.await()
                        val finalAiServices = aiSvcDeferred.await()
                        val finalProfiles = profilesDeferred.await()
                        val finalGroups = groupsDeferred.await()

                        eventRepository.cacheInMemory(
                            events = listOf(foundEvent),
                            cameras = finalCameras,
                            aiServices = finalAiServices
                        )
                        // Chỉ ghi cache profile xuống đĩa khi vừa tải mới (tránh serialize JSON lớn mỗi lần mở chi tiết)
                        if ((fetchProfiles && finalProfiles.isNotEmpty()) || (fetchGroups && finalGroups.isNotEmpty())) {
                            eventRepository.saveProfilesCache(
                                profiles = finalProfiles,
                                groups = finalGroups
                            )
                        }

                        val currentEv = _uiState.value.event ?: foundEvent
                        val camera = resolveCameraForEvent(currentEv, finalCameras)
                        val resolvedSnap = camera?.id?.let { camId ->
                            eventRepository.getCachedCameraImageLinkInMemory(camId)
                        }?.let(::resolveFullMediaUrl)?.takeIf { it.isNotEmpty() }

                        _uiState.update {
                            it.copy(
                                camera = camera ?: it.camera,
                                cameraList = finalCameras,
                                liveCameraSnapshotUrl = resolvedSnap ?: it.liveCameraSnapshotUrl,
                                aiServices = finalAiServices,
                                profileList = finalProfiles,
                                profileGroupList = finalGroups,
                                isLoading = false
                            )
                        }
                        if (pendingLiveStreamOnCameraResolved && camera != null) {
                            pendingLiveStreamOnCameraResolved = false
                            loadLiveStream(forceReload = true)
                        }
                    }
                } else {
                    eventRepository.cacheInMemory(
                        events = listOf(foundEvent),
                        cameras = cameraList,
                        aiServices = current.aiServices
                    )
                    if (_uiState.value.isLoading) {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error loading event", e)
                _uiState.update { state ->
                    if (state.event != null) {
                        state.copy(isLoading = false)
                    } else {
                        state.copy(isLoading = false, error = e.localizedMessage)
                    }
                }
            }
        }
    }

    /** Đổi sự kiện đang xem (ảnh/video/thông tin), hủy luồng live của sự kiện trước. */
    private fun showEvent(event: EventData) {
        liveStreamJob?.cancel()
        liveRetryCount = 0
        liveFallbackTried = false
        pendingLiveStreamOnCameraResolved = false
        _uiState.update {
            val resolvedCam = resolveCameraForEvent(event, it.cameraList) ?: it.camera
            val resolvedSnap = resolvedCam?.id?.let { camId ->
                eventRepository.getCachedCameraImageLinkInMemory(camId)
            }?.let(::resolveFullMediaUrl)?.takeIf { it.isNotEmpty() }
            it.copy(
                event = event,
                camera = resolvedCam,
                liveStreamUrl = null,
                liveStreamLoading = false,
                liveStreamError = null,
                liveCameraSnapshotUrl = resolvedSnap
            )
        }
    }

    fun selectEvent(event: EventData) {
        showEvent(event)
    }

    fun toggleRelatedLicensePlates() {
        if (_uiState.value.showRelatedPlates) {
            closeRelatedLicensePlates()
        } else {
            val currentEv = _uiState.value.event ?: return
            _uiState.update { it.copy(showRelatedPlates = true, relatedPlatesLoading = true, licensePlateEvents = emptyList()) }
            fetchLicensePlateEvents(currentEv.time, currentEv.cameraUuidOrNull)
        }
    }

    fun closeRelatedLicensePlates() {
        licensePlateJob?.cancel()
        val base = _uiState.value.baseEvent
        val curr = _uiState.value.event
        if (base != null && curr != null && base.id != curr.id) {
            showEvent(base)
        }
        _uiState.update {
            it.copy(
                showRelatedPlates = false,
                relatedPlatesLoading = false,
                licensePlateEvents = emptyList()
            )
        }
    }

    private fun fetchLicensePlateEvents(eventTime: Double, cameraUUID: String?) {
        licensePlateJob?.cancel()
        licensePlateJob = viewModelScope.launch {
            try {
                val timeSec = eventTimeToSeconds(eventTime)
                val response = eventRepository.getDataList(
                    count = 20,
                    from = timeSec - PLATE_WINDOW_SEC,
                    to = timeSec + PLATE_WINDOW_SEC,
                    cameraUUID = cameraUUID,
                    type = "sensor-license-plate"
                )
                if (response.isSuccessful) {
                    val camerasForPerm = _uiState.value.cameraList.ifEmpty { eventRepository.getCamerasInMemory() }
                    val events = UserPermissionManager.filterEventsByPermissions(
                        response.body() ?: emptyList(),
                        camerasForPerm
                    ).distinctBy { it.uniqueKey() }
                    _uiState.update { it.copy(licensePlateEvents = events, relatedPlatesLoading = false) }
                } else {
                    _uiState.update { it.copy(relatedPlatesLoading = false) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching license plate events", e)
                _uiState.update { it.copy(relatedPlatesLoading = false) }
            }
        }
    }

    private fun resolveFullMediaUrl(rawUrl: String): String {
        val trimmed = rawUrl.trim()
        if (trimmed.isEmpty() || trimmed == "NO_IMAGE") return ""
        if (trimmed.startsWith("http://") ||
            trimmed.startsWith("https://") ||
            trimmed.startsWith("file://") ||
            trimmed.startsWith("data:")
        ) {
            return trimmed
        }
        val base = UserSession.domain
            .trim()
            .trimEnd('/')
            .removeSuffix("/api/v1")
        return if (trimmed.startsWith("/")) "$base$trimmed" else "$base/$trimmed"
    }

    private fun resolveWebSocketUrl(rawLink: String): String {
        val trimmed = rawLink.trim()
        if (trimmed.isEmpty()) return ""
        val base = UserSession.domain
            .trim()
            .trimEnd('/')
            .removeSuffix("/api/v1")
        var resolved = when {
            trimmed.startsWith("/") -> "$base$trimmed"
            !trimmed.startsWith("ws://") &&
                !trimmed.startsWith("wss://") &&
                !trimmed.startsWith("http://") &&
                !trimmed.startsWith("https://") -> "$base/$trimmed"
            else -> trimmed
        }
        if (resolved.startsWith("http://")) resolved = resolved.replaceFirst("http://", "ws://")
        if (resolved.startsWith("https://")) resolved = resolved.replaceFirst("https://", "wss://")
        return resolved
    }

    fun loadLiveStream(forceReload: Boolean = false) {
        val state = _uiState.value
        val event = state.event ?: return
        val isEn = isEnglishLocale
        val camera = state.camera ?: resolveCameraForEvent(event, state.cameraList)

        if (camera == null) {
            if (state.cameraList.isEmpty()) {
                pendingLiveStreamOnCameraResolved = true
                _uiState.update { it.copy(liveStreamLoading = true, liveStreamError = null) }
            } else {
                _uiState.update {
                    it.copy(
                        liveStreamLoading = false,
                        liveStreamError = if (isEn) "Camera unavailable" else "Camera không khả dụng"
                    )
                }
            }
            return
        }

        if (!UserPermissionManager.canAccessLiveStream() || !UserPermissionManager.isCameraPermitted(camera)) {
            _uiState.update {
                it.copy(
                    liveStreamLoading = false,
                    liveStreamError = if (isEn) "No live view permission" else "Không có quyền xem trực tiếp"
                )
            }
            return
        }

        if (!forceReload && (state.liveStreamUrl != null || state.liveStreamLoading)) {
            return
        }

        val initialChannel = if (forceReload) {
            if (state.liveStreamChannel == 1 && camera.sub.isBlank()) 0 else state.liveStreamChannel
        } else if (camera.sub.isNotBlank()) {
            1
        } else {
            0
        }
        if (forceReload) {
            liveRetryCount = 0
            liveFallbackTried = false
        }
        startLiveStreamPolling(camera, initialChannel, isFallback = false)
    }

    fun switchLiveChannel(channel: Int) {
        val state = _uiState.value
        val event = state.event ?: return
        val camera = state.camera ?: resolveCameraForEvent(event, state.cameraList) ?: return
        if (channel == 1 && camera.sub.isBlank()) return
        if (state.liveStreamChannel == channel && (state.liveStreamUrl != null || state.liveStreamLoading)) return
        liveRetryCount = 0
        liveFallbackTried = true
        startLiveStreamPolling(camera, channel, isFallback = true)
    }

    private fun startLiveStreamPolling(camera: CameraModel, channel: Int, isFallback: Boolean) {
        liveStreamJob?.cancel()
        liveFallbackTried = isFallback
        val memSnap = eventRepository.getCachedCameraImageLinkInMemory(camera.id)
            ?.let(::resolveFullMediaUrl)
            ?.takeIf { it.isNotEmpty() }
        _uiState.update {
            it.copy(
                liveStreamUrl = null,
                liveStreamLoading = true,
                liveStreamError = null,
                liveStreamChannel = channel,
                liveCameraSnapshotUrl = memSnap ?: it.liveCameraSnapshotUrl
            )
        }

        liveStreamJob = viewModelScope.launch {
            if (_uiState.value.liveCameraSnapshotUrl.isNullOrEmpty()) {
                val diskSnap = withContext(Dispatchers.IO) {
                    eventRepository.getCachedCameraImageLink(camera.id)
                        ?.let(::resolveFullMediaUrl)
                        ?.takeIf { it.isNotEmpty() }
                }
                if (diskSnap != null) {
                    _uiState.update {
                        if (it.liveCameraSnapshotUrl.isNullOrEmpty()) it.copy(liveCameraSnapshotUrl = diskSnap) else it
                    }
                }
            }

            for (attempt in 1..LIVE_POLL_MAX_ATTEMPTS) {
                val activeCamId = _uiState.value.camera?.id ?: camera.id
                if (activeCamId != camera.id) return@launch

                try {
                    val response = eventRepository.createLiveStream(
                        LiveStreamModel(
                            cameraId = camera.id,
                            channel = channel,
                            link = "",
                            videoCodec = "",
                            audioCodec = "",
                            videoCodecTag = ""
                        )
                    )
                    if (response.isSuccessful) {
                        val body = response.body()
                        val rawLink = body?.link?.trim().orEmpty()
                        val videoCodec = body?.videoCodec?.trim().orEmpty()
                        if (rawLink.isNotEmpty() && videoCodec.isNotEmpty()) {
                            val wsUrl = resolveWebSocketUrl(rawLink)
                            _uiState.update {
                                it.copy(
                                    liveStreamUrl = wsUrl,
                                    liveStreamLoading = false,
                                    liveStreamError = null,
                                    liveStreamChannel = channel
                                )
                            }
                            return@launch
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // Chờ lượt poll tiếp theo
                }

                if (attempt < LIVE_POLL_MAX_ATTEMPTS) {
                    delay(LIVE_POLL_INTERVAL_MS)
                }
            }

            if (!isFallback && camera.sub.isNotBlank()) {
                val fallbackChannel = if (channel == 1) 0 else 1
                startLiveStreamPolling(camera, fallbackChannel, isFallback = true)
            } else {
                val isEn = isEnglishLocale
                _uiState.update {
                    it.copy(
                        liveStreamUrl = null,
                        liveStreamLoading = false,
                        liveStreamError = if (isEn) "No signal" else "Không có tín hiệu"
                    )
                }
            }
        }
    }

    fun onLiveStreamFirstFrame() {
        liveRetryCount = 0
        val camId = _uiState.value.camera?.id ?: return
        val latestSnap = eventRepository.getCachedCameraImageLinkInMemory(camId)
            ?.let(::resolveFullMediaUrl)
            ?.takeIf { it.isNotEmpty() }
        if (latestSnap != null) {
            _uiState.update { it.copy(liveCameraSnapshotUrl = latestSnap) }
        }
    }

    fun retryLiveStream() {
        val state = _uiState.value
        val event = state.event ?: return
        val camera = state.camera ?: resolveCameraForEvent(event, state.cameraList) ?: return
        val isEn = isEnglishLocale

        if (liveRetryCount >= 2 && !liveFallbackTried && camera.sub.isNotBlank()) {
            val nextChannel = if (state.liveStreamChannel == 1) 0 else 1
            liveRetryCount = 0
            startLiveStreamPolling(camera, nextChannel, isFallback = true)
            return
        }

        if (liveRetryCount >= 5) {
            _uiState.update {
                it.copy(
                    liveStreamUrl = null,
                    liveStreamLoading = false,
                    liveStreamError = if (isEn) "Camera connection error" else "Lỗi kết nối camera"
                )
            }
            return
        }

        val nextRetry = liveRetryCount + 1
        liveRetryCount = nextRetry
        liveStreamJob?.cancel()
        _uiState.update { it.copy(liveStreamUrl = null, liveStreamLoading = true, liveStreamError = null) }
        liveStreamJob = viewModelScope.launch {
            delay((1000L shl (nextRetry - 1)).coerceAtMost(8000L))
            startLiveStreamPolling(camera, state.liveStreamChannel, isFallback = liveFallbackTried)
        }
    }

    fun stopLiveStream() {
        liveStreamJob?.cancel()
        liveStreamJob = null
        pendingLiveStreamOnCameraResolved = false
        liveRetryCount = 0
        liveFallbackTried = false
        val camId = _uiState.value.camera?.id
        val latestSnap = if (camId != null && camId > 0) {
            eventRepository.getCachedCameraImageLinkInMemory(camId)
                ?.let(::resolveFullMediaUrl)
                ?.takeIf { it.isNotEmpty() }
        } else {
            null
        }
        _uiState.update {
            it.copy(
                liveStreamUrl = null,
                liveStreamLoading = false,
                liveStreamError = null,
                liveCameraSnapshotUrl = latestSnap ?: it.liveCameraSnapshotUrl
            )
        }
    }

    fun reportEventAsFalse(eventId: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val isEn = isEnglishLocale
        viewModelScope.launch {
            try {
                val response = eventRepository.reportEventAsFalse(eventId)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError(if (isEn) "Error reporting event: ${response.code()}" else "Lỗi khi báo cáo: ${response.code()}")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error reporting event", e)
                onError(if (isEn) "Connection error while reporting" else "Lỗi kết nối khi báo cáo")
            }
        }
    }

    companion object {
        private const val TAG = "EventDetailVM"
        /** Cửa sổ ±15 giây quanh thời điểm sự kiện để tìm biển số liên quan */
        private const val PLATE_WINDOW_SEC = 15L
        private const val LIVE_POLL_MAX_ATTEMPTS = 10
        private const val LIVE_POLL_INTERVAL_MS = 2000L

        fun provideFactory(eventId: Int, fallbackApplication: Application? = null): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] ?: fallbackApplication) as Application
                val eventRepository = (application as? TenliApp)?.container?.eventRepository
                    ?: EventRepository(application)
                EventDetailViewModel(application, eventId, eventRepository)
            }
        }
    }
}
