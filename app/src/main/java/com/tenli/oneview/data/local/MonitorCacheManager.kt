package com.tenli.oneview.data.local

import android.content.Context
import android.graphics.Bitmap
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tenli.oneview.model.network.CameraGroupModel
import com.tenli.oneview.model.network.CameraInGroupModel
import com.tenli.oneview.model.network.CameraModel
import com.tenli.oneview.model.network.VMSServiceModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap

data class CachedMonitorData(
    val vmsList: List<VMSServiceModel> = emptyList(),
    val groupList: List<CameraGroupModel> = emptyList(),
    val cameraList: List<CameraModel> = emptyList(),
    val cameraInGroups: List<CameraInGroupModel> = emptyList(),
    val aiServices: List<com.tenli.oneview.model.network.AIServiceModel> = emptyList(),
    val updatedAt: Long = 0L
)

data class SnapshotCacheItem(
    val link: String = "",
    val updatedAt: Long = 0L
)

object MonitorCacheManager {

    private val gson = Gson()
    private val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val snapshotMapType = object : TypeToken<Map<String, SnapshotCacheItem>>() {}.type

    /** Gom nhiều lần cập nhật snapshot liên tiếp thành 1 lần ghi đĩa (tránh ghi file liên tục khi xem nhiều camera) */
    private const val SNAPSHOT_PERSIST_DEBOUNCE_MS = 2_000L
    private const val LIVE_FRAME_PROTECT_MS = 30_000L
    private const val SNAPSHOT_MAX_DIMENSION_PX = 640
    private const val SNAPSHOT_JPEG_QUALITY = 70
    private const val MIN_VALID_IMAGE_BYTES = 200L
    @Volatile
    private var persistJob: Job? = null
    @Volatile
    private var snapshotDir: File? = null

    private const val SNAPSHOT_TTL_MS = 60 * 60 * 1000L // 1 giờ (đồng bộ vmsData.ts)
    private const val SNAPSHOT_STORAGE_MAX_AGE_MS = 7 * 24 * 60 * 60 * 1000L // 7 ngày

    @Volatile
    private var inMemoryMonitorData: CachedMonitorData? = null

    private val snapshotMap = ConcurrentHashMap<Int, SnapshotCacheItem>()
    @Volatile
    private var snapshotsLoaded = false

    fun getMonitorDataInMemory(): CachedMonitorData? = inMemoryMonitorData

    suspend fun preloadDefaultCache(context: Context) {
        withContext(Dispatchers.IO) {
            ensureSnapshotsLoaded(context)
        }
        getMonitorData(context)
    }

    private fun getCacheFile(context: Context): File {
        return File(context.cacheDir, "monitor_cache_raw_data.json")
    }

    private fun getSnapshotCacheFile(context: Context): File {
        return File(context.cacheDir, "monitor_camera_snapshots_v3.json")
    }

    private fun getSnapshotImageDir(context: Context): File {
        snapshotDir?.let { return it }
        return File(context.cacheDir, "camera_snapshots").apply {
            if (!exists()) mkdirs()
            snapshotDir = this
        }
    }

    private fun ensureSnapshotsLoaded(context: Context) {
        // Fast-path không cần lock (được gọi rất thường xuyên khi hiển thị lưới camera)
        if (snapshotsLoaded) return
        loadSnapshotsLocked(context)
    }

    @Synchronized
    private fun loadSnapshotsLocked(context: Context) {
        if (snapshotsLoaded) return
        snapshotsLoaded = true
        try {
            val parsed: Map<String, SnapshotCacheItem>? = JsonFileStore.read(gson, getSnapshotCacheFile(context), snapshotMapType)
            if (parsed != null) {
                val now = System.currentTimeMillis()
                parsed.forEach { (keyStr, item) ->
                    val id = keyStr.toIntOrNull()
                    if (id != null && item.link.isNotBlank() && item.link != "NO_IMAGE") {
                        if (now - item.updatedAt <= SNAPSHOT_STORAGE_MAX_AGE_MS) {
                            snapshotMap[id] = item
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun persistSnapshotsAsync(context: Context) {
        val appContext = context.applicationContext
        persistJob?.cancel()
        persistJob = ioScope.launch {
            delay(SNAPSHOT_PERSIST_DEBOUNCE_MS)
            val now = System.currentTimeMillis()
            val cleanMap = HashMap<String, SnapshotCacheItem>(snapshotMap.size)
            for ((id, item) in snapshotMap.entries) {
                if (item.link.isNotBlank() && now - item.updatedAt <= SNAPSHOT_STORAGE_MAX_AGE_MS) {
                    cleanMap[id.toString()] = item
                }
            }
            JsonFileStore.write(gson, getSnapshotCacheFile(appContext), cleanMap)
        }
    }

    fun getCachedCameraImageLink(context: Context, cameraId: Int, altCameraId: Int = 0): String? {
        ensureSnapshotsLoaded(context)
        val idsToCheck = buildList {
            if (cameraId > 0) add(cameraId)
            if (altCameraId > 0 && altCameraId != cameraId) add(altCameraId)
        }
        val now = System.currentTimeMillis()
        for (id in idsToCheck) {
            val item = snapshotMap[id]
            if (item != null && item.link.isNotBlank() && item.link != "NO_IMAGE") {
                if (now - item.updatedAt <= SNAPSHOT_STORAGE_MAX_AGE_MS) {
                    if (item.link.startsWith("file://")) {
                        val rawPath = item.link.removePrefix("file://").substringBefore("?")
                        if (File(rawPath).exists()) {
                            return item.link
                        }
                    } else {
                        return item.link
                    }
                }
            }
            val localFile = File(getSnapshotImageDir(context), "cam_${id}.jpg")
            if (localFile.exists() && localFile.length() > MIN_VALID_IMAGE_BYTES) {
                return "file://${localFile.absolutePath}?t=${localFile.lastModified()}"
            }
        }
        return null
    }

    fun isSnapshotCacheFresh(context: Context, cameraId: Int): Boolean {
        ensureSnapshotsLoaded(context)
        val item = snapshotMap[cameraId] ?: return false
        if (item.link.isBlank() || item.link == "NO_IMAGE") return false
        return (System.currentTimeMillis() - item.updatedAt) < SNAPSHOT_TTL_MS
    }

    fun setCachedCameraImageLink(context: Context, cameraId: Int, link: String, altCameraId: Int = 0) {
        if (cameraId <= 0 || link.isBlank() || link == "NO_IMAGE") return
        ensureSnapshotsLoaded(context)
        // Không ghi đè ảnh frame vừa chụp từ luồng live bằng link ảnh cũ hơn nếu frame đó còn rất mới (< 30s)
        val existing = snapshotMap[cameraId]
        val now = System.currentTimeMillis()
        if (existing != null &&
            existing.link.startsWith("file://") &&
            !link.startsWith("file://") &&
            (now - existing.updatedAt) < LIVE_FRAME_PROTECT_MS
        ) {
            return
        }
        val newItem = SnapshotCacheItem(link = link, updatedAt = now)
        snapshotMap[cameraId] = newItem
        if (altCameraId > 0 && altCameraId != cameraId) {
            snapshotMap[altCameraId] = newItem
        }
        persistSnapshotsAsync(context)
    }

    fun removeCachedCameraImageLink(context: Context, cameraId: Int, altCameraId: Int = 0) {
        ensureSnapshotsLoaded(context)
        snapshotMap.remove(cameraId)
        if (altCameraId > 0) {
            snapshotMap.remove(altCameraId)
        }
        persistSnapshotsAsync(context)
    }

    /**
     * Chụp frame từ luồng live (TextureView) và lưu vào cache đĩa giống `captureImmediateFrame()` trong `LivePlayer.vue`.
     */
    fun saveCapturedCameraFrame(context: Context, cameraId: Int, bitmap: Bitmap, altCameraId: Int = 0) {
        if (cameraId <= 0 || bitmap.width <= 0 || bitmap.height <= 0) return
        val appContext = context.applicationContext
        ioScope.launch {
            try {
                val maxDim = SNAPSHOT_MAX_DIMENSION_PX
                val scale = minOf(1f, maxDim.toFloat() / bitmap.width.toFloat())
                val targetW = (bitmap.width * scale).toInt().coerceAtLeast(1)
                val targetH = (bitmap.height * scale).toInt().coerceAtLeast(1)
                val scaled = if (targetW != bitmap.width || targetH != bitmap.height) {
                    Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
                } else {
                    bitmap
                }
                val outFile = File(getSnapshotImageDir(appContext), "cam_${cameraId}.jpg")
                FileOutputStream(outFile).use { fos ->
                    scaled.compress(Bitmap.CompressFormat.JPEG, SNAPSHOT_JPEG_QUALITY, fos)
                }
                if (scaled !== bitmap) {
                    scaled.recycle()
                }
                if (outFile.exists() && outFile.length() > MIN_VALID_IMAGE_BYTES) {
                    val fileUrl = "file://${outFile.absolutePath}?t=${System.currentTimeMillis()}"
                    setCachedCameraImageLink(appContext, cameraId, fileUrl, altCameraId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun saveMonitorData(context: Context, data: CachedMonitorData) {
        val stamped = if (data.updatedAt > 0L) data else data.copy(updatedAt = System.currentTimeMillis())
        inMemoryMonitorData = stamped
        EventCacheManager.cacheInMemory(cameras = stamped.cameraList, aiServices = stamped.aiServices)
        withContext(Dispatchers.IO) {
            JsonFileStore.write(gson, getCacheFile(context), stamped)
        }
    }

    suspend fun getMonitorData(context: Context): CachedMonitorData? {
        inMemoryMonitorData?.let { return it }
        return withContext(Dispatchers.IO) {
            JsonFileStore.read(gson, getCacheFile(context), CachedMonitorData::class.java)?.also { parsed ->
                inMemoryMonitorData = parsed
                EventCacheManager.cacheInMemory(cameras = parsed.cameraList.orEmpty(), aiServices = parsed.aiServices.orEmpty())
            }
        }
    }

    fun clearCache(context: Context) {
        inMemoryMonitorData = null
        persistJob?.cancel()
        try {
            val file = getCacheFile(context)
            if (file.exists()) {
                file.delete()
            }
            val snapFile = getSnapshotCacheFile(context)
            if (snapFile.exists()) {
                snapFile.delete()
            }
            snapshotMap.clear()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
