package com.tenli.oneview.data.repository

import android.content.Context
import androidx.core.content.edit
import com.tenli.oneview.data.local.CachedEventData
import com.tenli.oneview.data.local.EventCacheManager
import com.tenli.oneview.data.local.GlobalData
import com.tenli.oneview.data.local.HomeCacheManager
import com.tenli.oneview.data.local.MonitorCacheManager
import com.tenli.oneview.data.network.ApiProvider
import com.tenli.oneview.data.network.api.BsApi
import com.tenli.oneview.data.network.api.EventApi
import com.tenli.oneview.data.network.api.VmsApi
import com.tenli.oneview.model.network.AIServiceModel
import com.tenli.oneview.model.network.BsProfileGroupModel
import com.tenli.oneview.model.network.BsProfileModel
import com.tenli.oneview.model.network.CameraModel
import com.tenli.oneview.model.network.Confirm
import com.tenli.oneview.model.network.DataSearchPlateModel
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.model.network.LiveStreamModel
import com.tenli.oneview.model.network.VideoModel
import com.tenli.oneview.ui.features.home.TimeFilter
import com.tenli.oneview.util.EventBus
import retrofit2.Response

/**
 * Repository quản lý dữ liệu Sự kiện AI (Danh sách sự kiện, Chi tiết sự kiện, Lịch sử xuất hiện và Cuộc gọi cảnh báo):
 * Đóng gói truy cập RAM/Disk Cache ([EventCacheManager], [HomeCacheManager]) và các API ([EventApi], [VmsApi], [BsApi]).
 */
class EventRepository(
    private val context: Context,
    private val eventApi: EventApi = ApiProvider.eventApi,
    private val vmsApi: VmsApi = ApiProvider.vmsApi,
    private val bsApi: BsApi = ApiProvider.bsApi
) {
    private val appContext: Context = context.applicationContext

    fun isListViewMode(): Boolean =
        GlobalData.preferences.getString(PREF_VIEW_MODE, VIEW_MODE_LIST) != VIEW_MODE_GRID

    fun setListViewMode(isList: Boolean) {
        GlobalData.preferences.edit {
            putString(PREF_VIEW_MODE, if (isList) VIEW_MODE_LIST else VIEW_MODE_GRID)
        }
    }

    fun getEventDataInMemory(
        filter: TimeFilter,
        serviceId: Int?,
        aiType: String?,
        cameraUuid: String? = null
    ): CachedEventData? =
        EventCacheManager.getEventDataInMemory(filter, serviceId, aiType, cameraUuid)

    suspend fun getEventData(
        filter: TimeFilter,
        serviceId: Int?,
        aiType: String?,
        cameraUuid: String? = null
    ): CachedEventData? =
        EventCacheManager.getEventData(appContext, filter, serviceId, aiType, cameraUuid)

    suspend fun saveEventData(
        filter: TimeFilter,
        serviceId: Int?,
        aiType: String?,
        data: CachedEventData,
        cameraUuid: String? = null
    ) {
        EventCacheManager.saveEventData(appContext, filter, serviceId, aiType, data, cameraUuid)
    }

    fun hasFreshSessionMetadata(maxAgeMs: Long = 30_000L): Boolean =
        EventCacheManager.hasFreshSessionMetadata(maxAgeMs)

    fun getCamerasInMemory(): List<CameraModel> =
        EventCacheManager.getCamerasInMemory()

    fun getCameraNameMapInMemory(): Map<String, String> =
        EventCacheManager.getCameraNameMapInMemory()

    fun getAiServicesInMemory(): List<AIServiceModel> =
        EventCacheManager.getAiServicesInMemory()

    fun getProfilesInMemory(): List<BsProfileModel> =
        EventCacheManager.getProfilesInMemory()

    fun getProfileGroupsInMemory(): List<BsProfileGroupModel> =
        EventCacheManager.getProfileGroupsInMemory()

    fun getEventInMemory(eventId: Int): EventData? =
        EventCacheManager.getEventInMemory(eventId)

    suspend fun findEventById(eventId: Int): Pair<EventData, List<CameraModel>>? =
        EventCacheManager.findEventById(appContext, eventId)

    fun cacheInMemory(
        events: List<EventData> = emptyList(),
        cameras: List<CameraModel> = emptyList(),
        aiServices: List<AIServiceModel> = emptyList(),
        fromNetwork: Boolean = false
    ) {
        EventCacheManager.cacheInMemory(
            events = events,
            cameras = cameras,
            aiServices = aiServices,
            fromNetwork = fromNetwork
        )
    }

    fun cacheClickedEvent(
        event: EventData,
        cameras: List<CameraModel> = emptyList(),
        aiServices: List<AIServiceModel> = emptyList()
    ) {
        EventCacheManager.cacheClickedEvent(
            event = event,
            cameras = cameras,
            aiServices = aiServices
        )
    }

    suspend fun saveProfilesCache(
        profiles: List<BsProfileModel>,
        groups: List<BsProfileGroupModel>
    ) {
        EventCacheManager.saveProfilesCache(appContext, profiles, groups)
    }

    fun getRelatedEventsInMemory(eventId: Int, periodCode: String): List<EventData>? =
        EventCacheManager.getRelatedEventsInMemory(eventId, periodCode)

    fun cacheRelatedEventsInMemory(eventId: Int, periodCode: String, events: List<EventData>) {
        EventCacheManager.cacheRelatedEventsInMemory(eventId, periodCode, events)
    }

    suspend fun getDataList(
        lastId: Int? = null,
        count: Int? = null,
        from: Long? = null,
        to: Long? = null,
        serviceId: Int? = null,
        cameraUUID: String? = null,
        type: String? = null
    ): Response<List<EventData>> =
        eventApi.getDataList(
            lastId = lastId,
            count = count,
            from = from,
            to = to,
            serviceId = serviceId,
            cameraUUID = cameraUUID,
            type = type
        )

    suspend fun getCameraList(): Response<List<CameraModel>> =
        vmsApi.getCameraList()

    suspend fun getAIServiceList(): Response<List<AIServiceModel>> =
        eventApi.getAIServiceList()

    suspend fun getProfileList(): Response<List<BsProfileModel>> =
        bsApi.getProfileList()

    suspend fun getProfileGroupList(): Response<List<BsProfileGroupModel>> =
        bsApi.getProfileGroupList()

    suspend fun getUniformData(params: Map<String, String>): Response<List<EventData>> =
        bsApi.getUniformData(params)

    suspend fun searchAIData(
        from: Long,
        to: Long,
        serviceId: Int?,
        count: Int,
        fast: String,
        searchItems: List<DataSearchPlateModel>
    ): Response<List<EventData>> =
        eventApi.searchAIData(
            from = from,
            to = to,
            serviceId = serviceId,
            count = count,
            fast = fast,
            searchItems = searchItems
        )

    suspend fun getFaceDataByProfileUUID(
        profileUUID: String,
        count: Int,
        from: Long,
        to: Long,
        serviceId: Int?
    ): Response<List<EventData>> =
        eventApi.getFaceDataByProfileUUID(
            profileUUID = profileUUID,
            count = count,
            from = from,
            to = to,
            serviceId = serviceId
        )

    suspend fun getVideoList(
        camera: Int,
        count: Int,
        from: String,
        to: String,
        channel: String = "main"
    ): Response<List<VideoModel>> =
        vmsApi.getVideoList(camera = camera, count = count, from = from, to = to, channel = channel)

    suspend fun createLiveStream(request: LiveStreamModel): Response<LiveStreamModel> =
        vmsApi.createLiveStream(request)

    fun getCachedCameraImageLinkInMemory(cameraId: Int): String? =
        MonitorCacheManager.getCachedCameraImageLinkInMemory(cameraId)

    suspend fun getCachedCameraImageLink(cameraId: Int): String? =
        MonitorCacheManager.getCachedCameraImageLink(appContext, cameraId)

    /**
     * Báo cáo sự kiện sai lên server và đồng bộ xoá sự kiện khỏi toàn bộ RAM/Disk Cache + thông báo cho các ViewModel.
     */
    suspend fun reportEventAsFalse(eventId: Int): Response<Unit> {
        val response = eventApi.verifyEvent(eventId, Confirm(false))
        if (response.isSuccessful) {
            EventCacheManager.removeEvent(appContext, eventId)
            HomeCacheManager.removeEvent(appContext, eventId)
            EventBus.emitEventReportedFalse(eventId)
        }
        return response
    }

    companion object {
        private const val PREF_VIEW_MODE = "mobile_event_view_mode"
        private const val VIEW_MODE_LIST = "list"
        private const val VIEW_MODE_GRID = "grid"
    }
}
