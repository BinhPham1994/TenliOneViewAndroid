package com.tenli.oneview.data.repository

import android.content.Context
import androidx.core.content.edit
import com.tenli.oneview.data.local.CachedHomeData
import com.tenli.oneview.data.local.EventCacheManager
import com.tenli.oneview.data.local.GlobalData
import com.tenli.oneview.data.local.HomeCacheManager
import com.tenli.oneview.data.local.MonitorCacheManager
import com.tenli.oneview.data.network.ApiProvider
import com.tenli.oneview.data.network.api.AiApi
import com.tenli.oneview.data.network.api.BsApi
import com.tenli.oneview.data.network.api.EventApi
import com.tenli.oneview.data.network.api.VmsApi
import com.tenli.oneview.model.network.AIServiceModel
import com.tenli.oneview.model.network.CameraImageLinkModel
import com.tenli.oneview.model.network.CameraModel
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.model.network.Monitor
import com.tenli.oneview.model.network.VmsCountOverviewModel
import com.tenli.oneview.model.network.VmsEventCountByTypeModel
import com.tenli.oneview.ui.features.home.TimeFilter
import com.tenli.oneview.ui.utils.AiTypeHelper
import com.tenli.oneview.util.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import retrofit2.Response

/**
 * Repository quản lý dữ liệu Trang chủ (Dashboard):
 * Điều phối giữa RAM/Disk Cache ([HomeCacheManager], [EventCacheManager]) và các API dịch vụ ([BsApi], [VmsApi], [EventApi], [AiApi]).
 */
class DashboardRepository(
    private val context: Context,
    private val vmsApi: VmsApi = ApiProvider.vmsApi,
    private val eventApi: EventApi = ApiProvider.eventApi,
    private val bsApi: BsApi = ApiProvider.bsApi,
    private val aiApi: AiApi = ApiProvider.fastAiApi
) {
    private val appContext: Context = context.applicationContext

    fun isListViewMode(): Boolean =
        GlobalData.preferences.getString(PREF_VIEW_MODE, "list") != "grid"

    fun setListViewMode(isList: Boolean) {
        GlobalData.preferences.edit {
            putString(PREF_VIEW_MODE, if (isList) "list" else "grid")
        }
    }

    fun getHomeDataInMemory(filter: TimeFilter, serviceId: Int?): CachedHomeData? =
        HomeCacheManager.getHomeDataInMemory(filter, serviceId)

    suspend fun getHomeData(filter: TimeFilter, serviceId: Int?): CachedHomeData? =
        HomeCacheManager.getHomeData(appContext, filter, serviceId)

    suspend fun saveHomeData(filter: TimeFilter, serviceId: Int?, data: CachedHomeData) {
        HomeCacheManager.saveHomeData(appContext, filter, serviceId, data)
    }

    fun getCamerasInMemory(): List<CameraModel> =
        EventCacheManager.getCamerasInMemory()

    fun getAiServicesInMemory(): List<AIServiceModel> =
        EventCacheManager.getAiServicesInMemory()

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
        cameras: List<CameraModel>,
        aiServices: List<AIServiceModel>
    ) {
        EventCacheManager.cacheClickedEvent(
            event = event,
            cameras = cameras,
            aiServices = aiServices
        )
    }

    suspend fun prewarmProfilesIfNeeded() {
        val needProfiles = AiTypeHelper.isAiTypeEnabled("logic-face") ||
            AiTypeHelper.isAiTypeEnabled("sensor-uniform-camera")
        if (!needProfiles) return
        if (EventCacheManager.getProfilesInMemory().isNotEmpty()) return
        prewarmProfilesMutex.withLock {
            if (EventCacheManager.getProfilesInMemory().isNotEmpty()) return
            coroutineScope {
                val profilesDeferred = async { safeApiCall { bsApi.getProfileList() }.getOrNull()?.body().orEmpty() }
                val groupsDeferred = async { safeApiCall { bsApi.getProfileGroupList() }.getOrNull()?.body().orEmpty() }
                val profiles = profilesDeferred.await()
                val groups = groupsDeferred.await()
                if (profiles.isNotEmpty() || groups.isNotEmpty()) {
                    EventCacheManager.saveProfilesCache(appContext, profiles, groups)
                }
            }
        }
    }

    suspend fun getVmsCountOverview(
        serviceId: String,
        fromTime: Long,
        toTime: Long,
        eventType: String? = null,
        dataEventType: String? = null
    ): Response<List<VmsCountOverviewModel>> =
        bsApi.getVmsCountOverview(
            serviceId = serviceId,
            fromTime = fromTime,
            toTime = toTime,
            eventType = eventType,
            dataEventType = dataEventType
        )

    suspend fun getVmsEventCountByType(
        fromTime: Long,
        toTime: Long,
        serviceId: String?
    ): Response<List<VmsEventCountByTypeModel>> =
        bsApi.getVmsEventCountByType(fromTime = fromTime, toTime = toTime, serviceId = serviceId)

    suspend fun getCameraList(): Response<List<CameraModel>> =
        vmsApi.getCameraList()

    suspend fun getAIServiceList(): Response<List<AIServiceModel>> =
        eventApi.getAIServiceList()

    suspend fun getEventDataList(
        count: Int,
        lastId: Int?,
        from: Long,
        to: Long,
        serviceId: Int?,
        type: String? = null
    ): Response<List<EventData>> =
        eventApi.getDataList(
            count = count,
            lastId = lastId,
            from = from,
            to = to,
            serviceId = serviceId,
            type = type
        )

    suspend fun getMonitorList(): Response<List<Monitor>> =
        aiApi.getMonitorList()

    suspend fun getMonitorListExt(url: String, authHeader: String): Response<List<Monitor>> =
        aiApi.getMonitorListExt(url = url, authHeader = authHeader)

    fun getFavoriteCameraIds(): Set<Int> =
        MonitorCacheManager.readFavoriteCameraIds()

    val favoriteCameraIdsFlow: StateFlow<Set<Int>>
        get() = MonitorCacheManager.favoriteCameraIdsFlow

    val snapshotUpdatesFlow: SharedFlow<Pair<Int, String>>
        get() = MonitorCacheManager.snapshotUpdates

    fun getCachedCameraImageLinkInMemory(cameraId: Int): String? =
        MonitorCacheManager.getCachedCameraImageLinkInMemory(cameraId)

    suspend fun getCachedCameraImageLink(cameraId: Int): String? =
        withContext(Dispatchers.IO) {
            MonitorCacheManager.getCachedCameraImageLink(appContext, cameraId)
        }

    fun setCachedCameraImageLink(cameraId: Int, link: String) {
        MonitorCacheManager.setCachedCameraImageLink(appContext, cameraId, link)
    }

    suspend fun getCameraImageLink(cameraId: Int): Response<CameraImageLinkModel> =
        vmsApi.getCameraImageLink(cameraId)

    companion object {
        private const val PREF_VIEW_MODE = "mobile_event_view_mode"
        private val prewarmProfilesMutex = Mutex()
    }
}
