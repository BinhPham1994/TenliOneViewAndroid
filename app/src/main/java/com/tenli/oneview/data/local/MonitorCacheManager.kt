package com.tenli.oneview.data.local

import android.content.Context
import android.graphics.Bitmap
import coil.Coil
import coil.memory.MemoryCache
import com.google.gson.reflect.TypeToken
import com.tenli.oneview.data.network.retrofit.LoginAuthClient
import com.tenli.oneview.model.network.CameraGroupModel
import com.tenli.oneview.model.network.CameraInGroupModel
import com.tenli.oneview.model.network.CameraModel
import com.tenli.oneview.model.network.VMSServiceModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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

    private val gson get() = LoginAuthClient.gson
    private val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val diskReadMutex = Mutex()
    private val snapshotMapType = object : TypeToken<Map<String, SnapshotCacheItem>>() {}.type

    /** Gom nhiều lần cập nhật snapshot liên tiếp thành 1 lần ghi đĩa (tránh ghi file liên tục khi xem nhiều camera) */
    private const val SNAPSHOT_PERSIST_DEBOUNCE_MS = 2_000L
    /** Giữ ưu tiên ảnh chụp trực tiếp từ luồng Live trong suốt phiên (10 phút) để ảnh cũ từ API không ghi đè */
    private const val LIVE_FRAME_PROTECT_MS = 10 * 60 * 1000L
    private const val SNAPSHOT_MAX_DIMENSION_PX = 640
    private const val SNAPSHOT_JPEG_QUALITY = 75
    private const val MIN_VALID_IMAGE_BYTES = 200L
    const val SNAPSHOT_CARD_WIDTH_PX = 420
    const val SNAPSHOT_CARD_HEIGHT_PX = 236
    const val LIVE_STAGE_SNAPSHOT_WIDTH_PX = 1280
    const val LIVE_STAGE_SNAPSHOT_HEIGHT_PX = 720
    @Volatile
    private var persistJob: Job? = null
    @Volatile
    private var snapshotDir: File? = null

    /** Thời gian giữ kết quả đã kiểm tra/làm mới trong phiên hiện tại (10 phút) để cuộn danh sách hoặc Back từ Live về không gọi API lặp lại */
    private const val SNAPSHOT_SESSION_FRESH_TTL_MS = 10 * 60 * 1000L
    private const val SNAPSHOT_STORAGE_MAX_AGE_MS = 7 * 24 * 60 * 60 * 1000L // 7 ngày

    @Volatile
    private var inMemoryMonitorData: CachedMonitorData? = null

    private val snapshotMap = ConcurrentHashMap<Int, SnapshotCacheItem>()
    /** Đánh dấu thời điểm camera đã được cập nhật snapshot thực tế trong phiên mở app hiện tại */
    private val sessionRefreshedAtMap = ConcurrentHashMap<Int, Long>()
    /** Lưu các URL thumbnail playback bị lỗi (404) trong phiên để khi cuộn lên xuống không gọi mạng lại */
    val failedPlaybackThumbUrls: MutableSet<String> = java.util.Collections.newSetFromMap(ConcurrentHashMap<String, Boolean>())
    @Volatile
    private var snapshotsLoaded = false

    private val _snapshotUpdates = MutableSharedFlow<Pair<Int, String>>(extraBufferCapacity = 32)
    val snapshotUpdates: SharedFlow<Pair<Int, String>> = _snapshotUpdates.asSharedFlow()

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
        return File(context.cacheDir, "monitor_camera_snapshots_v4.json")
    }

    private fun deleteLegacySnapshotCacheFiles(context: Context) {
        runCatching {
            val legacyV3 = File(context.cacheDir, "monitor_camera_snapshots_v3.json")
            if (legacyV3.exists()) legacyV3.delete()
        }
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
        val now = System.currentTimeMillis()
        try {
            deleteLegacySnapshotCacheFiles(context)
            val parsed: Map<String, SnapshotCacheItem>? = JsonFileStore.read(gson, getSnapshotCacheFile(context), snapshotMapType)
            if (parsed != null) {
                parsed.forEach { (keyStr, item) ->
                    val id = keyStr.toIntOrNull()
                    if (id != null && id > 0 && item.link.isNotBlank() && item.link != "NO_IMAGE") {
                        if (now - item.updatedAt <= SNAPSHOT_STORAGE_MAX_AGE_MS) {
                            if (item.link.startsWith("file://")) {
                                val rawPath = item.link.removePrefix("file://").substringBefore("?")
                                val expectedSuffix = "/cam_${id}.jpg"
                                if (rawPath.endsWith(expectedSuffix) && File(rawPath).exists()) {
                                    snapshotMap[id] = item
                                }
                            } else {
                                snapshotMap[id] = item
                            }
                        }
                    }
                }
            }
            // Nạp sẵn các ảnh chụp từ luồng live lưu trên đĩa (cam_{id}.jpg) vào RAM để tra cứu 0ms không cần gọi File.exists() lặp lại
            val dir = getSnapshotImageDir(context)
            val files = dir.listFiles()
            if (files != null) {
                for (file in files) {
                    val name = file.name
                    if (name.startsWith("cam_") && name.endsWith(".jpg") && file.length() > MIN_VALID_IMAGE_BYTES) {
                        val camId = name.removePrefix("cam_").removeSuffix(".jpg").toIntOrNull() ?: continue
                        if (camId <= 0) continue
                        val lastMod = file.lastModified()
                        if (now - lastMod <= SNAPSHOT_STORAGE_MAX_AGE_MS) {
                            val existing = snapshotMap[camId]
                            if (existing == null || lastMod > existing.updatedAt) {
                                snapshotMap[camId] = SnapshotCacheItem(
                                    link = "file://${file.absolutePath}?t=$lastMod",
                                    updatedAt = lastMod
                                )
                            }
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

    /**
     * Tra cứu tức thì từ RAM (0ms, không chạm đĩa) khi snapshotMap đã được preload.
     * Lưu ý: Chỉ dùng duy nhất `camera.id` (ID toàn cục trên OneView), tuyệt đối không fallback sang `camera.cameraId`
     * (ID cục bộ trong từng VMS con, rất dễ trùng nhau giữa các VMS gây hiển thị nhầm ảnh của camera khác).
     */
    fun getCachedCameraImageLinkInMemory(cameraId: Int): String? {
        if (cameraId <= 0) return null
        val now = System.currentTimeMillis()
        val item = snapshotMap[cameraId]
        if (item != null && item.link.isNotBlank() && item.link != "NO_IMAGE" && now - item.updatedAt <= SNAPSHOT_STORAGE_MAX_AGE_MS) {
            return item.link
        }
        return null
    }

    fun getCachedCameraImageLink(context: Context, cameraId: Int): String? {
        ensureSnapshotsLoaded(context)
        return getCachedCameraImageLinkInMemory(cameraId)
    }

    fun isSnapshotsLoadedInMemory(): Boolean = snapshotsLoaded

    fun cardMemCacheKey(cameraId: Int, url: String): String =
        "cam_${cameraId}_${url}_${SNAPSHOT_CARD_WIDTH_PX}x${SNAPSHOT_CARD_HEIGHT_PX}"

    fun playbackThumbMemCacheKey(cameraId: Int, url: String): String =
        "pb_card_${cameraId}_${url}_${SNAPSHOT_CARD_WIDTH_PX}x${SNAPSHOT_CARD_HEIGHT_PX}"

    fun playbackThumbDiskCacheKey(cameraId: Int, url: String): String =
        "pb_card_${cameraId}_${url.substringBefore('?')}"

    fun liveStageMemCacheKey(cameraId: Int, url: String): String =
        "cam_${cameraId}_${url}_${LIVE_STAGE_SNAPSHOT_WIDTH_PX}x${LIVE_STAGE_SNAPSHOT_HEIGHT_PX}"

    fun diskCacheKey(cameraId: Int, url: String): String =
        "cam_${cameraId}_$url"

    fun coilDataModel(url: String): Any =
        if (url.startsWith("file://")) {
            File(url.removePrefix("file://").substringBefore("?"))
        } else {
            url
        }

    /**
     * Trả về `true` nếu camera này ĐÃ được kiểm tra/làm mới từ server (hoặc chụp từ luồng live)
     * trong phiên mở app hiện tại và chưa quá [SNAPSHOT_SESSION_FRESH_TTL_MS] (10 phút).
     * Kể cả khi server trả về không có ảnh (đã gọi [markSnapshotCheckedInSession]), hàm vẫn trả về `true`
     * để không gọi lặp lại API mỗi khi người dùng cuộn danh sách hoặc bấm Back từ màn hình Live.
     */
    fun isSnapshotCacheFresh(context: Context, cameraId: Int): Boolean {
        ensureSnapshotsLoaded(context)
        val refreshedAt = sessionRefreshedAtMap[cameraId] ?: return false
        return (System.currentTimeMillis() - refreshedAt) < SNAPSHOT_SESSION_FRESH_TTL_MS
    }

    fun markSnapshotCheckedInSession(cameraId: Int) {
        if (cameraId > 0) {
            sessionRefreshedAtMap[cameraId] = System.currentTimeMillis()
        }
    }

    fun setCachedCameraImageLink(context: Context, cameraId: Int, link: String) {
        if (UserSession.accessToken.isEmpty() || cameraId <= 0 || link.isBlank() || link == "NO_IMAGE") return
        ensureSnapshotsLoaded(context)
        val now = System.currentTimeMillis()
        sessionRefreshedAtMap[cameraId] = now
        // Không ghi đè ảnh frame vừa chụp từ luồng live bằng link ảnh cũ hơn từ server trong phiên hiện tại
        val existing = snapshotMap[cameraId]
        if (existing != null &&
            existing.link.startsWith("file://") &&
            !link.startsWith("file://") &&
            (now - existing.updatedAt) < LIVE_FRAME_PROTECT_MS
        ) {
            return
        }
        val newItem = SnapshotCacheItem(link = link, updatedAt = now)
        snapshotMap[cameraId] = newItem
        _snapshotUpdates.tryEmit(cameraId to link)
        persistSnapshotsAsync(context)
    }

    fun removeCachedCameraImageLink(context: Context, cameraId: Int) {
        if (cameraId <= 0) return
        ensureSnapshotsLoaded(context)
        val removed = snapshotMap.remove(cameraId)
        sessionRefreshedAtMap.remove(cameraId)
        if (removed?.link?.startsWith("file://") == true) {
            runCatching {
                File(getSnapshotImageDir(context), "cam_${cameraId}.jpg").takeIf { it.exists() }?.delete()
            }
        }
        persistSnapshotsAsync(context)
    }

    /**
     * Chụp frame từ luồng live (TextureView), nạp thẳng Bitmap vào MemoryCache của Coil (để thẻ camera cập nhật 0ms khi Back)
     * và lưu xuống đĩa giống `captureImmediateFrame()` trong `LivePlayer.vue`.
     */
    fun saveCapturedCameraFrame(context: Context, cameraId: Int, bitmap: Bitmap) {
        if (UserSession.accessToken.isEmpty() || cameraId <= 0 || bitmap.width <= 0 || bitmap.height <= 0) return
        val appContext = context.applicationContext
        ioScope.launch {
            try {
                if (UserSession.accessToken.isEmpty()) return@launch
                val maxDim = SNAPSHOT_MAX_DIMENSION_PX
                val scale = minOf(1f, maxDim.toFloat() / bitmap.width.toFloat())
                val targetW = (bitmap.width * scale).toInt().coerceAtLeast(1)
                val targetH = (bitmap.height * scale).toInt().coerceAtLeast(1)
                val scaled = if (targetW != bitmap.width || targetH != bitmap.height) {
                    Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
                } else {
                    bitmap
                }
                if (UserSession.accessToken.isEmpty()) {
                    if (scaled !== bitmap) scaled.recycle()
                    return@launch
                }
                val outFile = File(getSnapshotImageDir(appContext), "cam_${cameraId}.jpg")
                FileOutputStream(outFile).use { fos ->
                    scaled.compress(Bitmap.CompressFormat.JPEG, SNAPSHOT_JPEG_QUALITY, fos)
                }
                if (outFile.exists() && outFile.length() > MIN_VALID_IMAGE_BYTES) {
                    val fileUrl = "file://${outFile.absolutePath}?t=${System.currentTimeMillis()}"
                    // Đưa trực tiếp Bitmap vừa chụp vào MemoryCache của Coil cho cả thẻ lưới & màn hình Live
                    // để khi Back lại danh sách, ảnh hiển thị ngay lập tức (0ms) mà không cần đọc đĩa/giải mã lại JPEG
                    runCatching {
                        val memoryCache = Coil.imageLoader(appContext).memoryCache
                        if (memoryCache != null && !scaled.isRecycled) {
                            val cacheValue = MemoryCache.Value(scaled)
                            memoryCache[MemoryCache.Key(cardMemCacheKey(cameraId, fileUrl))] = cacheValue
                            memoryCache[MemoryCache.Key(liveStageMemCacheKey(cameraId, fileUrl))] = cacheValue
                        }
                    }
                    setCachedCameraImageLink(appContext, cameraId, fileUrl)
                } else if (scaled !== bitmap) {
                    scaled.recycle()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun saveMonitorData(context: Context, data: CachedMonitorData) {
        if (UserSession.accessToken.isEmpty()) return
        val stamped = if (data.updatedAt > 0L) data else data.copy(updatedAt = System.currentTimeMillis())
        inMemoryMonitorData = stamped
        EventCacheManager.cacheInMemory(cameras = stamped.cameraList, aiServices = stamped.aiServices)
        withContext(Dispatchers.IO) {
            if (UserSession.accessToken.isEmpty()) return@withContext
            JsonFileStore.write(gson, getCacheFile(context), stamped)
        }
    }

    suspend fun getMonitorData(context: Context): CachedMonitorData? {
        inMemoryMonitorData?.let { return it }
        return withContext(Dispatchers.IO) {
            diskReadMutex.withLock {
                inMemoryMonitorData?.let { return@withContext it }
                JsonFileStore.read(gson, getCacheFile(context), CachedMonitorData::class.java)?.also { parsed ->
                    inMemoryMonitorData = parsed
                    EventCacheManager.cacheInMemory(cameras = parsed.cameraList.orEmpty(), aiServices = parsed.aiServices.orEmpty())
                }
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
            deleteLegacySnapshotCacheFiles(context)
            getSnapshotImageDir(context).listFiles()?.forEach { it.delete() }
            snapshotMap.clear()
            sessionRefreshedAtMap.clear()
            failedPlaybackThumbUrls.clear()
            runCatching {
                context.getSharedPreferences("tenli_camera_prefs", Context.MODE_PRIVATE).edit().clear().apply()
            }
            runCatching {
                Coil.imageLoader(context.applicationContext).memoryCache?.clear()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
