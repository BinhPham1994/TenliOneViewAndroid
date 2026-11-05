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
import com.tenli.oneview.data.network.api.BsApi
import com.tenli.oneview.data.network.api.EventApi
import com.tenli.oneview.data.network.api.VmsApi
import com.tenli.oneview.data.network.retrofit.LoginAuthClient
import com.tenli.oneview.model.network.AIServiceModel
import com.tenli.oneview.model.network.BsProfileGroupModel
import com.tenli.oneview.model.network.BsProfileModel
import com.tenli.oneview.model.network.CameraModel
import com.tenli.oneview.model.network.Confirm
import com.tenli.oneview.model.network.DataSearchPlateModel
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.model.network.ParamPlateModel
import com.tenli.oneview.ui.features.home.TimeFilter
import com.tenli.oneview.ui.utils.AiTypeHelper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class JourneyPeriod(val code: String, val labelVi: String, val labelEn: String) {
    TODAY("1d", "Hôm nay", "Today"),
    YESTERDAY("2d", "Hôm qua", "Yesterday"),
    LAST_7_DAYS("7d", "7 ngày qua", "Last 7 days"),
    CUSTOM_DATE("custom_date", "Tùy chọn ngày", "Select date"),
    CUSTOM_RANGE("custom_range", "Tùy chỉnh", "Custom");

    fun formatDisplayLabel(isEn: Boolean): String {
        val dateFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateTimeFmt = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
        return when (this) {
            TODAY -> if (isEn) labelEn else labelVi
            YESTERDAY -> if (isEn) labelEn else labelVi
            LAST_7_DAYS -> if (isEn) labelEn else labelVi
            CUSTOM_DATE -> {
                val prefix = if (isEn) labelEn else labelVi
                "$prefix (${dateFmt.format(Date(TimeFilter.customDateStartMs))})"
            }
            CUSTOM_RANGE -> {
                "${dateTimeFmt.format(Date(TimeFilter.customRangeFromMs))} - ${dateTimeFmt.format(Date(TimeFilter.customRangeToMs))}"
            }
        }
    }

    fun cacheCode(): String {
        return when (this) {
            CUSTOM_DATE -> "${code}_${TimeFilter.customDateStartMs}"
            CUSTOM_RANGE -> "${code}_${TimeFilter.customRangeFromMs}_${TimeFilter.customRangeToMs}"
            else -> code
        }
    }
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
    private val eventId: Int
) : AndroidViewModel(application) {

    private val eventApi = LoginAuthClient.create(EventApi::class.java)
    private val vmsApi = LoginAuthClient.create(VmsApi::class.java)
    private val bsApi = LoginAuthClient.create(BsApi::class.java)

    private val _uiState: MutableStateFlow<EventDetailUiState> = run {
        val initialEvent = EventCacheManager.getEventInMemory(eventId)
        val initialCameras = EventCacheManager.getCamerasInMemory()
        val initialAiServices = EventCacheManager.getAiServicesInMemory()
        val initialProfiles = EventCacheManager.getProfilesInMemory()
        val initialGroups = EventCacheManager.getProfileGroupsInMemory()
        val initialRelated = EventCacheManager.getRelatedEventsInMemory(eventId, JourneyPeriod.TODAY.code)
        val initialCamera = if (initialEvent != null) {
            resolveCameraForEvent(initialEvent, initialCameras)
        } else null

        MutableStateFlow(
            EventDetailUiState(
                baseEvent = initialEvent,
                event = initialEvent,
                camera = initialCamera,
                cameraList = initialCameras,
                aiServices = initialAiServices,
                profileList = initialProfiles,
                profileGroupList = initialGroups,
                relatedEvents = initialRelated ?: emptyList(),
                isLoading = initialEvent == null,
                isJourneyLoading = initialRelated == null
            )
        )
    }
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()

    private var relatedEventsJob: Job? = null

    init {
        loadEventDetails()
    }

    private fun loadEventDetails() {
        viewModelScope.launch {
            try {
                var foundEvent = _uiState.value.event
                var cameraList = _uiState.value.cameraList

                if (foundEvent == null) {
                    _uiState.update { it.copy(isLoading = true, error = null) }

                    val cacheResult = EventCacheManager.findEventById(getApplication(), eventId)
                    if (cacheResult != null) {
                        foundEvent = cacheResult.first
                        if (cacheResult.second.isNotEmpty()) {
                            cameraList = cacheResult.second
                        }
                    }

                    if (foundEvent == null) {
                        val response = eventApi.getDataList(lastId = eventId + 1, count = 1)
                        if (response.isSuccessful) {
                            val events = response.body() ?: emptyList()
                            if (events.isNotEmpty() && events.first().id == eventId) {
                                foundEvent = events.first()
                            }
                        }
                    }

                    if (foundEvent == null) {
                        val isEn = com.tenli.oneview.ui.utils.LocaleManager.getLocale(getApplication()).startsWith("en")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isJourneyLoading = false,
                                error = if (isEn) "Event not found" else "Không tìm thấy sự kiện"
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
                val needProfilesForRelated = (AiTypeHelper.isFaceEvent(foundEvent) || foundEvent.type.lowercase().contains("uniform")) &&
                    _uiState.value.profileList.isEmpty()

                if (!needProfilesForRelated) {
                    loadRelatedEvents()
                }

                // Tải ngầm các metadata phụ trợ nếu chưa có sẵn trong bộ nhớ (không bật isLoading toàn màn hình)
                coroutineScope {
                    val camDeferred = async {
                        if (cameraList.isEmpty()) {
                            runCatching { vmsApi.getCameraList() }.getOrNull()?.body() ?: emptyList()
                        } else cameraList
                    }
                    val aiSvcDeferred = async {
                        if (_uiState.value.aiServices.isEmpty()) {
                            runCatching { eventApi.getAIServiceList() }.getOrNull()?.body() ?: emptyList()
                        } else _uiState.value.aiServices
                    }
                    val profilesDeferred = async {
                        if (_uiState.value.profileList.isEmpty()) {
                            runCatching { bsApi.getProfileList() }.getOrNull()?.body() ?: emptyList()
                        } else _uiState.value.profileList
                    }
                    val groupsDeferred = async {
                        if (_uiState.value.profileGroupList.isEmpty()) {
                            runCatching { bsApi.getProfileGroupList() }.getOrNull()?.body() ?: emptyList()
                        } else _uiState.value.profileGroupList
                    }

                    val finalCameras = camDeferred.await()
                    val finalAiServices = aiSvcDeferred.await()
                    val finalProfiles = profilesDeferred.await()
                    val finalGroups = groupsDeferred.await()

                    EventCacheManager.cacheInMemory(
                        events = listOf(foundEvent),
                        cameras = finalCameras,
                        aiServices = finalAiServices
                    )
                    EventCacheManager.saveProfilesCache(
                        context = getApplication(),
                        profiles = finalProfiles,
                        groups = finalGroups
                    )

                    val currentEv = _uiState.value.event ?: foundEvent
                    val camera = resolveCameraForEvent(currentEv, finalCameras)

                    _uiState.update {
                        it.copy(
                            camera = camera ?: it.camera,
                            cameraList = finalCameras,
                            aiServices = finalAiServices,
                            profileList = finalProfiles,
                            profileGroupList = finalGroups,
                            isLoading = false
                        )
                    }
                }

                if (needProfilesForRelated) {
                    loadRelatedEvents()
                }

            } catch (e: Exception) {
                Log.e("EventDetailVM", "Error loading event", e)
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

    private fun resolveCameraForEvent(event: EventData, cameras: List<CameraModel>): CameraModel? {
        val camUuid = event.data?.cameraUUID ?: event.data?.cameraUuidLower
        if (!camUuid.isNullOrBlank()) {
            cameras.find { it.extra?.uuid == camUuid }?.let { return it }
        }
        if (event.originId > 0) {
            cameras.find { it.id == event.originId }?.let { return it }
        }
        return null
    }

    fun selectEvent(event: EventData) {
        val cam = resolveCameraForEvent(event, _uiState.value.cameraList) ?: _uiState.value.camera
        _uiState.update {
            it.copy(
                event = event,
                camera = cam,
                originalVideoUrl = null,
                originalVideoLoading = false,
                originalVideoError = null
            )
        }
    }

    fun selectBaseEvent(event: EventData) {
        val cam = resolveCameraForEvent(event, _uiState.value.cameraList) ?: _uiState.value.camera
        _uiState.update {
            it.copy(
                event = event,
                camera = cam,
                originalVideoUrl = null,
                originalVideoLoading = false,
                originalVideoError = null
            )
        }
        // Only refetch if event is not already inside current relatedEvents list
        val alreadyInHistory = _uiState.value.relatedEvents.any { it.id == event.id }
        if (!alreadyInHistory) {
            _uiState.update { it.copy(baseEvent = event) }
            loadRelatedEvents()
        }
    }

    fun setJourneyPeriod(period: JourneyPeriod) {
        val isCustom = period == JourneyPeriod.CUSTOM_DATE || period == JourneyPeriod.CUSTOM_RANGE
        if (!isCustom && _uiState.value.selectedJourneyPeriod == period) return
        _uiState.update { it.copy(selectedJourneyPeriod = period) }
        loadRelatedEvents()
    }

    private fun getJourneyTimeRange(period: JourneyPeriod): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        val nowSec = cal.timeInMillis / 1000
        return when (period) {
            JourneyPeriod.TODAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                Pair(cal.timeInMillis / 1000, nowSec)
            }
            JourneyPeriod.YESTERDAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val endYesterday = (cal.timeInMillis / 1000) - 1
                cal.add(Calendar.DAY_OF_YEAR, -1)
                val startYesterday = cal.timeInMillis / 1000
                Pair(startYesterday, endYesterday)
            }
            JourneyPeriod.LAST_7_DAYS -> {
                cal.add(Calendar.DAY_OF_YEAR, -6)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                Pair(cal.timeInMillis / 1000, nowSec)
            }
            JourneyPeriod.CUSTOM_DATE -> {
                Pair(TimeFilter.customDateStartMs / 1000, TimeFilter.customDateEndMs / 1000)
            }
            JourneyPeriod.CUSTOM_RANGE -> {
                Pair(TimeFilter.customRangeFromMs / 1000, TimeFilter.customRangeToMs / 1000)
            }
        }
    }

    /**
     * Khớp hoàn toàn với useRelatedEvents.ts trong PWA:
     * 1. LPR / Plate -> gọi searchAIData (/Data/api/DataSearch)
     * 2. Face -> gọi getFaceDataByProfileUUID (/Data/api/FaceData/{uuid}) hoặc getDataList nếu stranger
     * 3. Uniform -> gọi bsApi.getUniformData (/ts/api/v1/e/uniform/get-data)
     * 4. Các module AI khác -> gọi getDataList (/Data/api/Data) theo cameraUUID & type
     */
    fun loadRelatedEvents() {
        val targetEvent = _uiState.value.event ?: _uiState.value.baseEvent ?: return
        val period = _uiState.value.selectedJourneyPeriod
        val cachedRelated = EventCacheManager.getRelatedEventsInMemory(targetEvent.id, period.cacheCode())

        relatedEventsJob?.cancel()
        relatedEventsJob = viewModelScope.launch {
            if (cachedRelated != null) {
                _uiState.update { it.copy(relatedEvents = cachedRelated, isJourneyLoading = false) }
            } else {
                _uiState.update { it.copy(isJourneyLoading = true) }
            }
            try {
                val (fromTime, toTime) = getJourneyTimeRange(period)
                val typeLower = targetEvent.type.lowercase()
                val evData = targetEvent.data
                val serviceId = targetEvent.serviceId.takeIf { it > 0 }
                val cameraUUID = evData?.cameraUUID ?: evData?.cameraUuidLower

                var fetched: List<EventData> = emptyList()

                if (typeLower == "sensor-license-plate" || typeLower.contains("plate") || typeLower.contains("lpr") || !evData?.plate?.plate.isNullOrBlank()) {
                    val plate = (evData?.plate?.plate ?: evData?.plate?.number ?: targetEvent.plate)?.trim()
                    if (!plate.isNullOrBlank()) {
                        val searchItems = listOf(
                            DataSearchPlateModel(
                                type = "plate",
                                confidence = 0.5,
                                param = ParamPlateModel(value = plate)
                            )
                        )
                        val res = runCatching {
                            eventApi.searchAIData(
                                from = fromTime,
                                to = toTime,
                                serviceId = serviceId,
                                count = 100,
                                fast = "yes",
                                searchItems = searchItems
                            )
                        }.getOrNull()
                        if (res?.isSuccessful == true) {
                            fetched = res.body() ?: emptyList()
                        }
                    }
                } else if (AiTypeHelper.isFaceEvent(targetEvent)) {
                    var profileUuid = (evData?.face?.profileUUID
                        ?: evData?.face?.profileUUIDUpper
                        ?: evData?.profileUUID
                        ?: evData?.profileUUIDUpper)?.trim().orEmpty()

                    if (profileUuid.isEmpty()) {
                        val profileId = evData?.face?.profileId ?: evData?.profileId
                        if (profileId != null) {
                            profileUuid = _uiState.value.profileList.find { it.id == profileId }?.uuid?.trim().orEmpty()
                        }
                    }
                    if (profileUuid.isEmpty() && !AiTypeHelper.isStranger(targetEvent)) {
                        val rawName = evData?.face?.name ?: evData?.name ?: targetEvent.name
                        if (!rawName.isNullOrBlank()) {
                            profileUuid = _uiState.value.profileList.find {
                                it.name.trim().equals(rawName.trim(), ignoreCase = true)
                            }?.uuid?.trim().orEmpty()
                        }
                    }

                    if (profileUuid.isNotEmpty()) {
                        val res = runCatching {
                            eventApi.getFaceDataByProfileUUID(
                                profileUUID = profileUuid,
                                count = 50,
                                from = fromTime,
                                to = toTime,
                                serviceId = serviceId
                            )
                        }.getOrNull()
                        if (res?.isSuccessful == true) {
                            fetched = res.body() ?: emptyList()
                        }
                    } else {
                        val res = runCatching {
                            eventApi.getDataList(
                                count = 50,
                                serviceId = serviceId,
                                type = targetEvent.type.ifBlank { "sensor-face-unknown-camera" },
                                from = fromTime,
                                to = toTime,
                                cameraUUID = cameraUUID
                            )
                        }.getOrNull()
                        if (res?.isSuccessful == true) {
                            fetched = res.body() ?: emptyList()
                        }
                    }
                } else if (typeLower.contains("uniform")) {
                    var profileUuid = (evData?.face?.profileUUID
                        ?: evData?.face?.profileUUIDUpper
                        ?: evData?.profileUUID
                        ?: evData?.profileUUIDUpper)?.trim().orEmpty()

                    if (profileUuid.isEmpty()) {
                        val profileId = evData?.face?.profileId ?: evData?.profileId
                        if (profileId != null) {
                            profileUuid = _uiState.value.profileList.find { it.id == profileId }?.uuid?.trim().orEmpty()
                        }
                    }

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
                    val res = runCatching { bsApi.getUniformData(params) }.getOrNull()
                    if (res?.isSuccessful == true) {
                        fetched = res.body() ?: emptyList()
                    }
                } else {
                    val res = runCatching {
                        eventApi.getDataList(
                            count = 50,
                            serviceId = serviceId,
                            type = targetEvent.type,
                            from = fromTime,
                            to = toTime,
                            cameraUUID = cameraUUID
                        )
                    }.getOrNull()
                    if (res?.isSuccessful == true) {
                        fetched = res.body() ?: emptyList()
                    }
                }

                // Lọc theo cameraUUID đối với các module AI thông thường (như useRelatedEvents.ts dòng 278-292)
                var filtered = fetched.filter { AiTypeHelper.isAiTypeEnabled(it.type) || it.type == targetEvent.type }
                if (!typeLower.contains("plate") && !typeLower.contains("lpr") && !AiTypeHelper.isFaceEvent(targetEvent) && !typeLower.contains("uniform")) {
                    if (!cameraUUID.isNullOrBlank()) {
                        filtered = filtered.filter { item ->
                            val cId = item.data?.cameraUUID ?: item.data?.cameraUuidLower
                            cId.isNullOrBlank() || cId == cameraUUID
                        }
                    }
                }

                val sorted = filtered.sortedByDescending { it.time }
                EventCacheManager.cacheRelatedEventsInMemory(targetEvent.id, period.cacheCode(), sorted)
                _uiState.update { it.copy(relatedEvents = sorted, isJourneyLoading = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("EventDetailVM", "Error fetching related events", e)
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
            fetchLicensePlateEvents(currentEv.time, currentEv.data?.cameraUUID ?: currentEv.data?.cameraUuidLower)
        }
    }

    fun closeRelatedLicensePlates() {
        val base = _uiState.value.baseEvent
        val curr = _uiState.value.event
        _uiState.update {
            it.copy(
                showRelatedPlates = false,
                relatedPlatesLoading = false,
                licensePlateEvents = emptyList(),
                event = if (base != null && curr != null && base.id != curr.id) base else curr
            )
        }
    }

    private fun fetchLicensePlateEvents(eventTime: Double, cameraUUID: String?) {
        viewModelScope.launch {
            try {
                val timeSec = if (eventTime > 1e11) (eventTime / 1000).toLong() else eventTime.toLong()
                val fromTime = timeSec - 15
                val toTime = timeSec + 15

                val response = eventApi.getDataList(
                    count = 20,
                    from = fromTime,
                    to = toTime,
                    cameraUUID = cameraUUID,
                    type = "sensor-license-plate"
                )
                if (response.isSuccessful) {
                    val events = response.body() ?: emptyList()
                    _uiState.update { it.copy(licensePlateEvents = events, relatedPlatesLoading = false) }
                } else {
                    _uiState.update { it.copy(relatedPlatesLoading = false) }
                }
            } catch (e: Exception) {
                Log.e("EventDetailVM", "Error fetching license plate events", e)
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
        val base = com.tenli.oneview.data.local.UserSession.domain
            .ifEmpty { com.tenli.oneview.BuildConfig.DOMAIN_CLOUD }
            .trim()
            .trimEnd('/')
            .removeSuffix("/api/v1")
        return if (trimmed.startsWith("/")) "$base$trimmed" else "$base/$trimmed"
    }

    fun loadOriginalVideo() {
        val event = _uiState.value.event ?: return
        val isEn = com.tenli.oneview.ui.utils.LocaleManager.getLocale(getApplication()).startsWith("en")
        val camera = _uiState.value.camera
            ?: resolveCameraForEvent(event, _uiState.value.cameraList)

        if (camera == null) {
            _uiState.update {
                it.copy(
                    originalVideoLoading = false,
                    originalVideoError = if (isEn) "Original video unavailable" else "Video không khả dụng"
                )
            }
            return
        }

        if (_uiState.value.originalVideoUrl != null || _uiState.value.originalVideoLoading) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(originalVideoLoading = true, originalVideoError = null) }
            try {
                val eventTimeMs = if (event.time < 10000000000.0) (event.time * 1000).toLong() else event.time.toLong()
                val fromMs = eventTimeMs - 61 * 1000
                val toMs = eventTimeMs

                val dateFormat = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault())
                val fromDate = dateFormat.format(Date(fromMs))
                val toDate = dateFormat.format(Date(toMs))

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
                        val seekSeconds = Math.max(0L, Math.min(61L, diffSeconds - 10L))

                        _uiState.update {
                            it.copy(
                                originalVideoUrl = resolveFullMediaUrl(videoItem.videoLink),
                                originalVideoSeekTimeMs = seekSeconds * 1000,
                                originalVideoEventPositionMs = diffSeconds * 1000,
                                originalVideoLoading = false
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                originalVideoLoading = false,
                                originalVideoError = if (isEn) "Original video unavailable" else "Video không khả dụng"
                            )
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            originalVideoLoading = false,
                            originalVideoError = if (isEn) "Error fetching original video" else "Lỗi khi lấy video gốc"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("EventDetailVM", "Error loading original video", e)
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
        val isEn = com.tenli.oneview.ui.utils.LocaleManager.getLocale(getApplication()).startsWith("en")
        viewModelScope.launch {
            try {
                val response = eventApi.verifyEvent(eventId, Confirm(false))
                if (response.isSuccessful) {
                    com.tenli.oneview.util.EventBus.emitEventReportedFalse(eventId)
                    onSuccess()
                } else {
                    onError(if (isEn) "Error reporting event: ${response.code()}" else "Lỗi khi báo cáo: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("EventDetailVM", "Error reporting event", e)
                onError(if (isEn) "Connection error while reporting" else "Lỗi kết nối khi báo cáo")
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
