package com.tenli.oneview.data.local

import android.content.Context
import coil.Coil
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale
import com.tenli.oneview.data.network.retrofit.LoginAuthClient
import com.tenli.oneview.model.network.AIServiceModel
import com.tenli.oneview.model.network.CameraModel
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.model.network.VmsCountOverviewModel
import com.tenli.oneview.model.network.VmsEventCountByTypeModel
import com.tenli.oneview.ui.component.FACE_CROP_REQUEST_HEIGHT_PX
import com.tenli.oneview.ui.component.FACE_CROP_REQUEST_WIDTH_PX
import com.tenli.oneview.ui.component.GRID_IMAGE_HEIGHT_PX
import com.tenli.oneview.ui.component.GRID_IMAGE_WIDTH_PX
import com.tenli.oneview.ui.component.LIST_THUMB_HEIGHT_PX
import com.tenli.oneview.ui.component.LIST_THUMB_WIDTH_PX
import com.tenli.oneview.ui.features.home.TimeFilter
import com.tenli.oneview.ui.utils.AiTypeHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.util.concurrent.ConcurrentHashMap

data class CachedHomeData(
    val overviewStats: List<VmsCountOverviewModel> = emptyList(),
    val eventsByType: List<VmsEventCountByTypeModel> = emptyList(),
    val cameraList: List<CameraModel> = emptyList(),
    val aiServices: List<AIServiceModel> = emptyList(),
    val recentEvents: List<EventData> = emptyList(),
    val runningAiTasks: Int = 0,
    val totalAiTasks: Int = 0,
    val cameraStatusMap: Map<String, String>? = null,
    val recordStatusMap: Map<String, String>? = null,
    val updatedAt: Long = 0L
)

object HomeCacheManager {

    internal const val HOME_CACHE_PREFIX = "home_cache_"
    private const val MAX_PRELOAD_EVENTS = 5
    private const val NETWORK_PRELOAD_TIMEOUT_MS = 2_200L

    private val gson get() = LoginAuthClient.gson

    // RAM Cache giúp hiển thị tức thì (0ms) ngay từ frame đầu tiên khi mở app hoặc chuyển bộ lọc
    private val inMemoryHomeCache = ConcurrentHashMap<String, CachedHomeData>()

    private fun cacheKey(filter: TimeFilter, serviceId: Int?): String {
        val servicePart = serviceId?.toString() ?: "all"
        return "${TimeFilter.cacheKeyToken(filter)}_${servicePart}"
    }

    private fun getCacheFile(context: Context, filter: TimeFilter, serviceId: Int?): File {
        return File(context.cacheDir, "$HOME_CACHE_PREFIX${cacheKey(filter, serviceId)}.json")
    }

    fun getHomeDataInMemory(filter: TimeFilter, serviceId: Int?): CachedHomeData? {
        return inMemoryHomeCache[cacheKey(filter, serviceId)]
    }

    suspend fun preloadDefaultCache(context: Context) {
        val data = getHomeData(context, TimeFilter.TODAY, null)
        if (data != null && data.recentEvents.isNotEmpty()) {
            preloadEventThumbnails(context, data.recentEvents, diskOnly = true)
        }
    }

    /**
     * Nạp sẵn ảnh thumbnail của 5 sự kiện Trang chủ vào Coil MemoryCache:
     * - [diskOnly] = true (khi mở app ở Splash): đọc nhanh từ DiskCache lên RAM (~10-20ms), không chờ mạng,
     *   để Frame 1 của HomeScreen vẽ ngay ảnh Bitmap thật mà không có ô xám chờ tải.
     * - [diskOnly] = false (khi API/WebSocket trả về sự kiện mới trong lúc màn hình đã hiển thị cache):
     *   tải trước ảnh mới vào RAM rồi mới cập nhật State để danh sách không bị nháy ảnh trống.
     */
    suspend fun preloadEventThumbnails(
        context: Context,
        events: List<EventData>,
        diskOnly: Boolean
    ) {
        val domain = UserSession.domain
        if (domain.isBlank() || events.isEmpty()) return

        val appContext = context.applicationContext
        val imageLoader = Coil.imageLoader(appContext)
        val memCache = imageLoader.memoryCache
        val targetEvents = events
            .filter { AiTypeHelper.isAiTypeEnabled(it.type) }
            .take(MAX_PRELOAD_EVENTS)

        val requests = ArrayList<ImageRequest>(targetEvents.size * 2)
        for (event in targetEvents) {
            val faceUrl = AiTypeHelper.getFaceCropUrl(event, domain)
            val snapUrl = AiTypeHelper.getSnapshotUrl(event, domain)
            val fullUrl = AiTypeHelper.getFullImageUrl(event, domain)
            val fullImageUrl = fullUrl.ifEmpty { snapUrl }.ifEmpty { null }
            val snapshotUrl = (faceUrl ?: snapUrl.ifEmpty { fullUrl }).ifEmpty { null }
            val faceCropUrl = faceUrl?.takeIf { it.isNotBlank() && it != fullImageUrl }

            fun enqueueIfNeeded(url: String?, w: Int, h: Int) {
                if (url.isNullOrBlank() || url.lowercase().endsWith(".mp4")) return
                val key = "${url}_${w}x${h}"
                if (memCache?.get(MemoryCache.Key(key))?.bitmap != null) return
                val builder = ImageRequest.Builder(appContext)
                    .data(url)
                    .size(w, h)
                    .scale(Scale.FILL)
                    .precision(Precision.INEXACT)
                    .allowHardware(true)
                    .crossfade(false)
                    .memoryCacheKey(key)
                    .diskCacheKey(url)
                if (diskOnly) {
                    builder.networkCachePolicy(CachePolicy.DISABLED)
                }
                requests.add(builder.build())
            }

            enqueueIfNeeded(snapshotUrl, LIST_THUMB_WIDTH_PX, LIST_THUMB_HEIGHT_PX)
            enqueueIfNeeded(fullImageUrl, GRID_IMAGE_WIDTH_PX, GRID_IMAGE_HEIGHT_PX)
            enqueueIfNeeded(faceCropUrl, FACE_CROP_REQUEST_WIDTH_PX, FACE_CROP_REQUEST_HEIGHT_PX)
        }

        if (requests.isEmpty()) return

        withContext(Dispatchers.IO) {
            if (diskOnly) {
                coroutineScope {
                    requests.map { req ->
                        async { runCatching { imageLoader.execute(req) } }
                    }.awaitAll()
                }
            } else {
                withTimeoutOrNull(NETWORK_PRELOAD_TIMEOUT_MS) {
                    coroutineScope {
                        requests.map { req ->
                            async { runCatching { imageLoader.execute(req) } }
                        }.awaitAll()
                    }
                }
            }
        }
    }

    suspend fun saveHomeData(context: Context, filter: TimeFilter, serviceId: Int?, data: CachedHomeData) {
        val stamped = if (data.updatedAt > 0L) data else data.copy(updatedAt = System.currentTimeMillis())
        inMemoryHomeCache[cacheKey(filter, serviceId)] = stamped
        EventCacheManager.cacheInMemory(stamped.recentEvents, stamped.cameraList, stamped.aiServices)
        withContext(Dispatchers.IO) {
            JsonFileStore.write(gson, getCacheFile(context, filter, serviceId), stamped)
        }
    }

    suspend fun updateCameraAndRecordStatus(
        context: Context,
        cameraStatusMap: Map<String, String>,
        recordStatusMap: Map<String, String>
    ) {
        val defaultKey = cacheKey(TimeFilter.TODAY, null)
        val current = inMemoryHomeCache[defaultKey] ?: return
        val mergedCam = if (cameraStatusMap.isNotEmpty()) {
            current.cameraStatusMap.orEmpty() + cameraStatusMap
        } else {
            current.cameraStatusMap.orEmpty()
        }
        val mergedRec = if (recordStatusMap.isNotEmpty()) {
            current.recordStatusMap.orEmpty() + recordStatusMap
        } else {
            current.recordStatusMap.orEmpty()
        }
        if (mergedCam == current.cameraStatusMap.orEmpty() && mergedRec == current.recordStatusMap.orEmpty()) {
            return
        }
        val updated = current.copy(
            cameraStatusMap = mergedCam,
            recordStatusMap = mergedRec
        )
        inMemoryHomeCache[defaultKey] = updated
        withContext(Dispatchers.IO) {
            JsonFileStore.write(gson, getCacheFile(context, TimeFilter.TODAY, null), updated)
        }
    }

    suspend fun getHomeData(context: Context, filter: TimeFilter, serviceId: Int?): CachedHomeData? {
        val key = cacheKey(filter, serviceId)
        inMemoryHomeCache[key]?.let { return it }

        return withContext(Dispatchers.IO) {
            JsonFileStore.read(gson, getCacheFile(context, filter, serviceId), CachedHomeData::class.java)?.also { parsed ->
                inMemoryHomeCache[key] = parsed
                EventCacheManager.cacheInMemory(parsed.recentEvents, parsed.cameraList, parsed.aiServices)
            }
        }
    }

    fun clearCache(context: Context) {
        inMemoryHomeCache.clear()
        JsonFileStore.deleteMatching(context.cacheDir) { it.startsWith(HOME_CACHE_PREFIX) }
    }
}
