package com.tenli.oneview.data.repository

import android.content.Context
import android.graphics.Bitmap
import androidx.core.content.edit
import com.tenli.oneview.data.local.CachedMonitorData
import com.tenli.oneview.data.local.EventCacheManager
import com.tenli.oneview.data.local.GlobalData
import com.tenli.oneview.data.local.MonitorCacheManager
import com.tenli.oneview.data.network.ApiProvider
import com.tenli.oneview.data.network.api.EventApi
import com.tenli.oneview.data.network.api.VmsApi
import com.tenli.oneview.model.network.AIServiceModel
import com.tenli.oneview.model.network.CameraGroupModel
import com.tenli.oneview.model.network.CameraImageLinkModel
import com.tenli.oneview.model.network.CameraInGroupModel
import com.tenli.oneview.model.network.CameraModel
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.model.network.LiveStreamModel
import com.tenli.oneview.model.network.VMSServiceModel
import com.tenli.oneview.model.network.VideoModel
import kotlinx.coroutines.flow.SharedFlow
import retrofit2.Response

/**
 * Repository quản lý dữ liệu Giám sát Camera (Cây VMS/Group/Camera, Snapshot, LiveStream, Xem lại & Sự kiện của Camera):
 * Đóng gói truy cập RAM/Disk Cache ([MonitorCacheManager], [EventCacheManager]) và các API ([VmsApi], [EventApi]).
 */
class MonitorRepository(
    private val context: Context,
    private val vmsApi: VmsApi = ApiProvider.vmsApi,
    private val eventApi: EventApi = ApiProvider.eventApi
) {
    private val appContext: Context = context.applicationContext

    val snapshotUpdates: SharedFlow<Pair<Int, String>>
        get() = MonitorCacheManager.snapshotUpdates

    val failedPlaybackThumbUrls: MutableSet<String>
        get() = MonitorCacheManager.failedPlaybackThumbUrls

    fun getExpandedNodesPref(): Set<String> =
        GlobalData.preferences.getStringSet(PREF_EXPANDED_NODES, emptySet()) ?: emptySet()

    fun saveExpandedNodesPref(nodes: Set<String>) {
        GlobalData.preferences.edit { putStringSet(PREF_EXPANDED_NODES, nodes) }
    }

    fun saveGridSizePref(size: Int) {
        GlobalData.preferences.edit { putInt(PREF_GRID_SIZE, size) }
    }

    fun saveSelectedCameraIdsPref(idsCsv: String, firstCameraId: Int?) {
        GlobalData.preferences.edit {
            putString(PREF_GRID_CAMERA_IDS, idsCsv)
            if (firstCameraId != null) {
                putInt(PREF_CAMERA_ID, firstCameraId)
            } else {
                remove(PREF_CAMERA_ID)
            }
        }
    }

    fun getMonitorDataInMemory(): CachedMonitorData? =
        MonitorCacheManager.getMonitorDataInMemory()

    suspend fun getMonitorData(): CachedMonitorData? =
        MonitorCacheManager.getMonitorData(appContext)

    suspend fun saveMonitorData(data: CachedMonitorData) {
        MonitorCacheManager.saveMonitorData(appContext, data)
    }

    fun isSnapshotsLoadedInMemory(): Boolean =
        MonitorCacheManager.isSnapshotsLoadedInMemory()

    fun getCachedCameraImageLinkInMemory(cameraId: Int): String? =
        MonitorCacheManager.getCachedCameraImageLinkInMemory(cameraId)

    fun getCachedCameraImageLink(cameraId: Int): String? =
        MonitorCacheManager.getCachedCameraImageLink(appContext, cameraId)

    fun isSnapshotCacheFresh(cameraId: Int): Boolean =
        MonitorCacheManager.isSnapshotCacheFresh(appContext, cameraId)

    fun markSnapshotCheckedInSession(cameraId: Int) {
        MonitorCacheManager.markSnapshotCheckedInSession(cameraId)
    }

    fun setCachedCameraImageLink(cameraId: Int, link: String) {
        MonitorCacheManager.setCachedCameraImageLink(appContext, cameraId, link)
    }

    fun removeCachedCameraImageLink(cameraId: Int) {
        MonitorCacheManager.removeCachedCameraImageLink(appContext, cameraId)
    }

    fun saveCapturedCameraFrame(cameraId: Int, bitmap: Bitmap) {
        MonitorCacheManager.saveCapturedCameraFrame(appContext, cameraId, bitmap)
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

    fun cacheClickedCameraEvent(
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

    suspend fun getVMSServiceList(): Response<List<VMSServiceModel>> =
        vmsApi.getVMSServiceList()

    suspend fun getCameraGroupList(): Response<List<CameraGroupModel>> =
        vmsApi.getCameraGroupList()

    suspend fun getCameraList(): Response<List<CameraModel>> =
        vmsApi.getCameraList()

    suspend fun getCameraInGroupList(): Response<List<CameraInGroupModel>> =
        vmsApi.getCameraInGroupList()

    suspend fun getAIServiceList(): Response<List<AIServiceModel>> =
        eventApi.getAIServiceList()

    suspend fun getCameraImageLink(cameraId: Int): Response<CameraImageLinkModel> =
        vmsApi.getCameraImageLink(cameraId = cameraId)

    suspend fun createLiveStream(request: LiveStreamModel): Response<LiveStreamModel> =
        vmsApi.createLiveStream(request)

    suspend fun getVideoList(
        camera: Int,
        count: Int,
        from: String? = null,
        to: String? = null,
        channel: String = "main"
    ): Response<List<VideoModel>> =
        vmsApi.getVideoList(
            camera = camera,
            count = count,
            from = from,
            to = to,
            channel = channel
        )

    suspend fun getEventDataList(
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

    companion object {
        private const val PREF_EXPANDED_NODES = "saved_expanded_nodes"
        private const val PREF_GRID_SIZE = "saved_monitor_grid_size"
        private const val PREF_GRID_CAMERA_IDS = "saved_monitor_grid_camera_ids"
        private const val PREF_CAMERA_ID = "saved_monitor_camera_id"
    }
}
