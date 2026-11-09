package com.tenli.oneview.ui.features.event.detail

import android.app.Application
import android.util.Log
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
import com.tenli.oneview.model.network.DataSearchPlateModel
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.model.network.LiveStreamModel
import com.tenli.oneview.model.network.ParamPlateModel
import com.tenli.oneview.ui.features.event.cameraUuidOrNull
import com.tenli.oneview.ui.features.event.directProfileId
import com.tenli.oneview.ui.features.event.directProfileUuid
import com.tenli.oneview.ui.features.event.eventTimeToMillis
import com.tenli.oneview.ui.features.event.eventTimeToSeconds
import com.tenli.oneview.ui.features.event.formatVideoQueryTime
import com.tenli.oneview.ui.features.event.moveToStartOfDay
import com.tenli.oneview.ui.features.event.parseVideoQueryTime
import com.tenli.oneview.ui.features.event.resolveCameraForEvent
import com.tenli.oneview.ui.features.event.uniqueKey
import com.tenli.oneview.ui.features.home.TimeFilter
import com.tenli.oneview.ui.component.MobileTopPopupManager
import com.tenli.oneview.ui.utils.AiTypeHelper
import com.tenli.oneview.ui.utils.LocaleManager
import com.tenli.oneview.util.safeApiCall
import com.tenli.oneview.util.toUserFriendlyMessage
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
import java.util.Calendar

/**
 * Khoảng thời gian của "Lịch sử xuất hiện". Nhãn hiển thị dùng chung với [TimeFilter] để không lặp chuỗi.
 */
enum class JourneyPeriod(val code: String, private val timeFilter: TimeFilter) {
    TODAY("1d", TimeFilter.TODAY),
    YESTERDAY("2d", TimeFilter.YESTERDAY),
    LAST_7_DAYS("7d", TimeFilter.LAST_7_DAYS),
    CUSTOM_DATE("custom_date", TimeFilter.CUSTOM_DATE),
    CUSTOM_RANGE("custom_range", TimeFilter.CUSTOM_RANGE);

    /** Nhãn ngắn dùng trong menu chọn */
    fun label(isEn: Boolean): String = if (isEn) timeFilter.titleEn else timeFilter.title

    /** Nhãn đầy đủ (kèm ngày / khoảng ngày tùy chọn) hiển thị trên nút chọn */
    fun formatDisplayLabel(isEn: Boolean): String = TimeFilter.formatDisplayLabel(timeFilter, isEn)

    fun cacheCode(): String {
        return when (this) {
            CUSTOM_DATE -> "${code}_${TimeFilter.customDateStartMs}"
            CUSTOM_RANGE -> "${code}_${TimeFilter.customRangeFromMs}_${TimeFilter.customRangeToMs}"
            else -> code
        }
    }

    val isCustom: Boolean get() = this == CUSTOM_DATE || this == CUSTOM_RANGE
}

data class EventDetailUiState(
    val baseEvent: EventData? = null,
    val event: EventData? = null,
    val camera: CameraModel? = null,
    val cameraList: List<CameraModel> = emptyList(),
    val relatedEvents: List<EventData> = emptyList(),
    val isJourneyLoading: Boolean = false,
    val selectedJourneyPeriod: JourneyPeriod = JourneyPeriod.TODAY,
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
    val originalVideoUrl: String? = null,
    val originalVideoSeekTimeMs: Long? = null,
    val originalVideoEventPositionMs: Long? = null,
    val originalVideoLoading: Boolean = false,
    val originalVideoError: String? = null,
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
        val initialRelated = eventRepository.getRelatedEventsInMemory(eventId, JourneyPeriod.TODAY.code)

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
                relatedEvents = initialRelated ?: emptyList(),
                isLoading = initialEvent == null,
                isJourneyLoading = initialRelated == null
            )
        )
    }
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()

    private var relatedEventsJob: Job? = null
    private var licensePlateJob: Job? = null
    private var originalVideoJob: Job? = null
    private var liveStreamJob: Job? = null
    private var liveRetryCount: Int = 0
    private var liveFallbackTried: Boolean = false
    private var pendingLiveStreamOnCameraResolved: Boolean = false

    private val isEnglishLocale: Boolean
        get() = LocaleManager.getLocale(getApplication()).startsWith("en")

    init {
        loadEventDetails()
    }

    /** Khuôn mặt / Đồng phục cần đối chiếu danh sách profile (tên, nhóm, UUID cho lịch sử xuất hiện). */
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
                                isJourneyLoading = false,
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
                            isLoading = false,
                            isJourneyLoading = true
                        )
                    }
                }

                // Gọi API lấy danh sách Lịch sử xuất hiện (Sự kiện liên quan) ở phần dưới màn hình
                // Nếu là sự kiện Khuôn mặt / Đồng phục cần đối chiếu profileList mà chưa có trong RAM thì tải profileList trước
                val needProfiles = eventNeedsProfiles(foundEvent)
                val needProfilesForRelated = needProfiles && _uiState.value.profileList.isEmpty()

                if (!needProfilesForRelated) {
                    loadRelatedEvents()
                }

                // Tải ngầm các metadata phụ trợ CHƯA có sẵn trong bộ nhớ (không bật isLoading toàn màn hình).
                // Danh sách profile / nhóm chỉ cần cho sự kiện Khuôn mặt / Đồng phục.
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
                    _uiState.update { it.copy(isLoading = false) }
                }

                if (needProfilesForRelated) {
                    loadRelatedEvents()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error loading event", e)
                _uiState.update { state ->
                    if (state.event != null) {
                        state.copy(isLoading = false, isJourneyLoading = false)
                    } else {
                        state.copy(isLoading = false, isJourneyLoading = false, error = e.localizedMessage)
                    }
                }
            }
        }
    }

    /** Đổi sự kiện đang xem (ảnh/video/thông tin), hủy tải video gốc & luồng live của sự kiện trước. */
    private fun showEvent(event: EventData) {
        originalVideoJob?.cancel()
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
                liveCameraSnapshotUrl = resolvedSnap,
                originalVideoUrl = null,
                originalVideoLoading = false,
                originalVideoError = null
            )
        }
    }

    fun selectEvent(event: EventData) {
        showEvent(event)
    }

    fun selectBaseEvent(event: EventData) {
        showEvent(event)
        // Only refetch if event is not already inside current relatedEvents list
        val alreadyInHistory = _uiState.value.relatedEvents.any { it.id == event.id }
        if (!alreadyInHistory) {
            _uiState.update { it.copy(baseEvent = event) }
            loadRelatedEvents()
        }
    }

    fun setJourneyPeriod(period: JourneyPeriod) {
        if (!period.isCustom && _uiState.value.selectedJourneyPeriod == period) return
        _uiState.update { it.copy(selectedJourneyPeriod = period) }
        loadRelatedEvents()
    }

    private fun getJourneyTimeRange(period: JourneyPeriod): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        val nowSec = cal.timeInMillis / 1000
        return when (period) {
            JourneyPeriod.TODAY -> Pair(cal.moveToStartOfDay() / 1000, nowSec)
            JourneyPeriod.YESTERDAY -> {
                val endYesterday = (cal.moveToStartOfDay() / 1000) - 1
                cal.add(Calendar.DAY_OF_YEAR, -1)
                Pair(cal.timeInMillis / 1000, endYesterday)
            }
            JourneyPeriod.LAST_7_DAYS -> {
                cal.add(Calendar.DAY_OF_YEAR, -6)
                Pair(cal.moveToStartOfDay() / 1000, nowSec)
            }
            JourneyPeriod.CUSTOM_DATE -> Pair(TimeFilter.customDateStartMs / 1000, TimeFilter.customDateEndMs / 1000)
            JourneyPeriod.CUSTOM_RANGE -> Pair(TimeFilter.customRangeFromMs / 1000, TimeFilter.customRangeToMs / 1000)
        }
    }

    private fun isPlateType(typeLower: String): Boolean = typeLower.contains("plate") || typeLower.contains("lpr")

    /** UUID profile của sự kiện; nếu thiếu thì tra theo profileId trong danh sách profile đã tải. */
    private fun resolveProfileUuid(event: EventData, profiles: List<BsProfileModel>): String {
        val direct = event.directProfileUuid()
        if (direct.isNotEmpty()) return direct
        val profileId = event.directProfileId ?: return ""
        return profiles.find { it.id == profileId }?.uuid?.trim().orEmpty()
    }

    /**
     * Gọi API lấy dữ liệu thô của "Lịch sử xuất hiện" theo loại sự kiện (khớp useRelatedEvents.ts trong PWA):
     * 1. LPR / Plate -> gọi searchAIData (/Data/api/DataSearch)
     * 2. Face -> gọi getFaceDataByProfileUUID (/Data/api/FaceData/{uuid}) hoặc getDataList nếu stranger
     * 3. Uniform -> gọi bsApi.getUniformData (/ts/api/v1/e/uniform/get-data)
     * 4. Các module AI khác -> gọi getDataList (/Data/api/Data) theo cameraUUID & type
     */
    private suspend fun fetchRelatedRaw(
        targetEvent: EventData,
        fromTime: Long,
        toTime: Long,
        profiles: List<BsProfileModel>
    ): Result<List<EventData>> {
        val typeLower = targetEvent.type.lowercase()
        val evData = targetEvent.data
        val serviceId = targetEvent.serviceId.takeIf { it > 0 }
        val cameraUUID = targetEvent.cameraUuidOrNull

        val result = when {
            typeLower == "sensor-license-plate" || isPlateType(typeLower) || !evData?.plate?.plate.isNullOrBlank() -> {
                val plate = (evData?.plate?.plate ?: evData?.plate?.number ?: targetEvent.plate)?.trim()
                if (plate.isNullOrBlank()) return Result.success(emptyList())
                val searchItems = listOf(
                    DataSearchPlateModel(
                        type = "plate",
                        confidence = 0.5,
                        param = ParamPlateModel(value = plate)
                    )
                )
                safeApiCall {
                    eventRepository.searchAIData(
                        from = fromTime,
                        to = toTime,
                        serviceId = serviceId,
                        count = 100,
                        fast = "yes",
                        searchItems = searchItems
                    )
                }
            }

            AiTypeHelper.isFaceEvent(targetEvent) -> {
                var profileUuid = resolveProfileUuid(targetEvent, profiles)
                if (profileUuid.isEmpty() && !AiTypeHelper.isStranger(targetEvent)) {
                    val rawName = evData?.face?.name ?: evData?.name ?: targetEvent.name
                    if (!rawName.isNullOrBlank()) {
                        profileUuid = profiles.find {
                            it.name.trim().equals(rawName.trim(), ignoreCase = true)
                        }?.uuid?.trim().orEmpty()
                    }
                }

                if (profileUuid.isNotEmpty()) {
                    safeApiCall {
                        eventRepository.getFaceDataByProfileUUID(
                            profileUUID = profileUuid,
                            count = 50,
                            from = fromTime,
                            to = toTime,
                            serviceId = serviceId
                        )
                    }
                } else {
                    safeApiCall {
                        eventRepository.getDataList(
                            count = 50,
                            serviceId = serviceId,
                            type = targetEvent.type.ifBlank { "sensor-face-unknown-camera" },
                            from = fromTime,
                            to = toTime,
                            cameraUUID = cameraUUID
                        )
                    }
                }
            }

            typeLower.contains("uniform") -> {
                val profileUuid = resolveProfileUuid(targetEvent, profiles)
                val params = mutableMapOf(
                    "isConfirmed" to "1",
                    "order" to "desc",
                    "count" to "50",
                    "eventType" to "sensor-uniform-camera",
                    "from" to fromTime.toString(),
                    "to" to toTime.toString()
                )
                if (profileUuid.isNotEmpty()) {
                    params["profileUUID"] = profileUuid
                    params["onlyProfileUnknown"] = "0"
                } else {
                    params["onlyProfileUnknown"] = "1"
                }
                safeApiCall { eventRepository.getUniformData(params) }
            }

            else -> safeApiCall {
                eventRepository.getDataList(
                    count = 50,
                    serviceId = serviceId,
                    type = targetEvent.type,
                    from = fromTime,
                    to = toTime,
                    cameraUUID = cameraUUID
                )
            }
        }

        return result.map { it.body() ?: emptyList() }
    }

    fun loadRelatedEvents() {
        val state = _uiState.value
        val targetEvent = state.event ?: state.baseEvent ?: return
        val period = state.selectedJourneyPeriod
        val cacheCode = period.cacheCode()
        val cachedRelated = eventRepository.getRelatedEventsInMemory(targetEvent.id, cacheCode)

        relatedEventsJob?.cancel()
        relatedEventsJob = viewModelScope.launch {
            if (cachedRelated != null) {
                _uiState.update { it.copy(relatedEvents = cachedRelated, isJourneyLoading = false) }
            } else {
                _uiState.update { it.copy(isJourneyLoading = true) }
            }
            try {
                val (fromTime, toTime) = getJourneyTimeRange(period)
                val fetchedResult = fetchRelatedRaw(targetEvent, fromTime, toTime, _uiState.value.profileList)
                val fetched = fetchedResult.getOrElse { err ->
                    MobileTopPopupManager.error(err.toUserFriendlyMessage())
                    _uiState.update { it.copy(isJourneyLoading = false) }
                    return@launch
                }

                // Lọc / sắp xếp trên Dispatchers.Default để không chiếm Main thread
                val sorted = withContext(Dispatchers.Default) {
                    val typeLower = targetEvent.type.lowercase()
                    val cameraUUID = targetEvent.cameraUuidOrNull
                    // Lọc theo cameraUUID đối với các module AI thông thường (như useRelatedEvents.ts dòng 278-292)
                    val filterByCamera = !isPlateType(typeLower) &&
                        !AiTypeHelper.isFaceEvent(targetEvent) &&
                        !typeLower.contains("uniform") &&
                        !cameraUUID.isNullOrBlank()

                    val camerasForPerm = _uiState.value.cameraList.ifEmpty { eventRepository.getCamerasInMemory() }
                    fetched
                        .asSequence()
                        .filter { item ->
                            item.id == targetEvent.id || UserPermissionManager.isEventPermitted(item, camerasForPerm)
                        }
                        .filter { item ->
                            if (!filterByCamera) return@filter true
                            val cId = item.cameraUuidOrNull
                            cId.isNullOrBlank() || cId == cameraUUID
                        }
                        // Loại bản ghi trùng để key của LazyColumn luôn duy nhất
                        .distinctBy { it.uniqueKey() }
                        .sortedByDescending { it.time }
                        .toList()
                }
                eventRepository.cacheRelatedEventsInMemory(targetEvent.id, cacheCode, sorted)
                _uiState.update { it.copy(relatedEvents = sorted, isJourneyLoading = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching related events", e)
                MobileTopPopupManager.error(e.toUserFriendlyMessage())
                _uiState.update { it.copy(isJourneyLoading = false) }
            }
        }
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
        pendingLiveStreamOnCameraResolved = false
        _uiState.update { it.copy(liveStreamLoading = false) }
    }

    fun loadOriginalVideo() {
        val state = _uiState.value
        val event = state.event ?: return
        val isEn = isEnglishLocale
        val camera = state.camera ?: resolveCameraForEvent(event, state.cameraList)

        if (camera == null) {
            _uiState.update {
                it.copy(
                    originalVideoLoading = false,
                    originalVideoError = if (isEn) "Original video unavailable" else "Video không khả dụng"
                )
            }
            return
        }

        if (state.originalVideoUrl != null || state.originalVideoLoading) {
            return
        }

        originalVideoJob?.cancel()
        originalVideoJob = viewModelScope.launch {
            _uiState.update { it.copy(originalVideoLoading = true, originalVideoError = null) }
            try {
                val eventTimeMs = eventTimeToMillis(event.time)
                val fromMs = eventTimeMs - ORIGINAL_VIDEO_LOOKBACK_MS

                val response = eventRepository.getVideoList(
                    camera = camera.id,
                    count = 1,
                    from = formatVideoQueryTime(fromMs),
                    to = formatVideoQueryTime(eventTimeMs)
                )
                val videoItem = if (response.isSuccessful) response.body()?.firstOrNull() else null
                when {
                    !response.isSuccessful -> _uiState.update {
                        it.copy(
                            originalVideoLoading = false,
                            originalVideoError = if (isEn) "Error fetching original video" else "Lỗi khi lấy video gốc"
                        )
                    }

                    videoItem != null && videoItem.videoLink.isNotEmpty() -> {
                        val videoStartMs = parseVideoQueryTime(videoItem.time) ?: 0L
                        val diffSeconds = (eventTimeMs - videoStartMs) / 1000
                        val seekSeconds = (diffSeconds - 10L).coerceIn(0L, 61L)

                        _uiState.update {
                            it.copy(
                                originalVideoUrl = resolveFullMediaUrl(videoItem.videoLink),
                                originalVideoSeekTimeMs = seekSeconds * 1000,
                                originalVideoEventPositionMs = diffSeconds * 1000,
                                originalVideoLoading = false
                            )
                        }
                    }

                    else -> _uiState.update {
                        it.copy(
                            originalVideoLoading = false,
                            originalVideoError = if (isEn) "Original video unavailable" else "Video không khả dụng"
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error loading original video", e)
                _uiState.update {
                    it.copy(
                        originalVideoLoading = false,
                        originalVideoError = if (isEn) "Error loading video" else "Lỗi khi tải video"
                    )
                }
            }
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
        /** Video gốc lấy từ 61 giây trước thời điểm sự kiện */
        private const val ORIGINAL_VIDEO_LOOKBACK_MS = 61_000L
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
