package com.tenli.oneview.ui.component

import android.content.ComponentCallbacks2
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Build
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.tenli.oneview.data.network.retrofit.LoginAuthClient
import com.tenli.oneview.ui.theme.appColors
import com.tenli.oneview.ui.theme.spacing
import com.tenli.oneview.util.NetworkMonitor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resumeWithException
import android.graphics.Color as AndroidColor

object VideoFrameCache {
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = (maxMemory / 16).coerceAtLeast(4096)
    val cache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }

    // Reuse shared OkHttpClient connection pool & dispatcher instead of allocating a new one
    val rangeHttpClient: OkHttpClient get() = LoginAuthClient.baseClient

    // Track URLs that are confirmed to be videos
    val knownVideoUrls: MutableSet<String> = Collections.newSetFromMap(ConcurrentHashMap<String, Boolean>())

    // Track URLs that permanently failed (404 / non-video error) so scrolling never re-requests them
    val failedUrls: MutableSet<String> = Collections.newSetFromMap(ConcurrentHashMap<String, Boolean>())

    // Giới hạn tối đa 2 MediaMetadataRetriever chạy đồng thời để chống nóng máy & nghẽn I/O khi cuộn nhanh
    val extractionSemaphore = Semaphore(2)

    private val PRESIGNED_QUERY_KEYS = listOf(
        "X-Amz-Signature",
        "X-Amz-Algorithm",
        "X-Amz-Credential",
        "GoogleAccessId",
        "Signature"
    )

    fun isPresignedUrl(url: String): Boolean {
        val parsed = url.toHttpUrlOrNull() ?: return false
        return PRESIGNED_QUERY_KEYS.any { parsed.queryParameter(it) != null }
    }

    @Suppress("DEPRECATION")
    fun trimMemory(level: Int) {
        if (level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND ||
            level == ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL ||
            level == ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW
        ) {
            cache.evictAll()
        } else if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE) {
            cache.trimToSize(cache.maxSize() / 2)
        }
    }

    fun clear(context: Context) {
        cache.evictAll()
        knownVideoUrls.clear()
        failedUrls.clear()
        runCatching {
            File(context.cacheDir, "video_thumbs").listFiles()?.forEach { it.delete() }
        }
    }

    fun looksLikeVideoUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val clean = url.substringBefore('?').lowercase()
        return clean.endsWith(".mp4") ||
            clean.endsWith(".mov") ||
            clean.endsWith(".mkv") ||
            clean.endsWith(".webm") ||
            clean.endsWith(".avi") ||
            clean.endsWith(".ts") ||
            clean.contains("/video/")
    }
}

@Composable
fun VideoFrameImage(
    url: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    cacheKey: String? = null,
    targetWidthPx: Int = 480,
    targetHeightPx: Int = 270,
    errorContent: @Composable (() -> Unit)? = null
) {
    val context = LocalContext.current
    val effectiveKey = cacheKey ?: url ?: ""

    var isVideoFallback by remember(effectiveKey) {
        mutableStateOf(
            url != null && (VideoFrameCache.knownVideoUrls.contains(effectiveKey) || VideoFrameCache.looksLikeVideoUrl(url))
        )
    }

    if (!isVideoFallback) {
        var isImageError by remember(effectiveKey, url) {
            mutableStateOf(effectiveKey.isNotEmpty() && VideoFrameCache.failedUrls.contains(effectiveKey))
        }
        val imageRequest = remember(url, effectiveKey, targetWidthPx, targetHeightPx) {
            if (url.isNullOrBlank()) {
                null
            } else {
                coil.request.ImageRequest.Builder(context)
                    .data(url)
                    .size(targetWidthPx, targetHeightPx)
                    .precision(coil.size.Precision.INEXACT)
                    .allowHardware(true)
                    .allowRgb565(true)
                    .crossfade(false)
                    .memoryCacheKey("${effectiveKey}_${targetWidthPx}x${targetHeightPx}")
                    .diskCacheKey(url.substringBefore('?'))
                    .build()
            }
        }

        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (imageRequest != null && !isImageError) {
                coil.compose.AsyncImage(
                    model = imageRequest,
                    contentDescription = "Event Image",
                    contentScale = contentScale,
                    modifier = Modifier.fillMaxSize(),
                    onError = {
                        // Chỉ fallback sang trích xuất frame video khi URL thực sự là video hoặc chưa từng thất bại
                        if (url != null && !VideoFrameCache.failedUrls.contains(effectiveKey) && VideoFrameCache.looksLikeVideoUrl(url)) {
                            VideoFrameCache.knownVideoUrls.add(effectiveKey)
                            isVideoFallback = true
                        } else if (effectiveKey.isNotEmpty()) {
                            if (NetworkMonitor.isNetworkAvailable()) {
                                VideoFrameCache.failedUrls.add(effectiveKey)
                            }
                            isImageError = true
                        }
                    }
                )
            }
        }
    } else {
        var bitmap by remember(effectiveKey) {
            mutableStateOf<Bitmap?>(if (effectiveKey.isNotEmpty()) VideoFrameCache.cache.get(effectiveKey) else null)
        }
        var hasError by remember(effectiveKey) {
            mutableStateOf(effectiveKey.isEmpty() || VideoFrameCache.failedUrls.contains(effectiveKey))
        }

        LaunchedEffect(effectiveKey, url) {
            if (url == null || effectiveKey.isEmpty() || hasError || bitmap != null) {
                return@LaunchedEffect
            }

            val extracted = extractVideoFrameWithRange(context, url, effectiveKey)
            if (extracted == null) {
                if (NetworkMonitor.isNetworkAvailable()) {
                    VideoFrameCache.failedUrls.add(effectiveKey)
                }
                hasError = true
            } else {
                VideoFrameCache.cache.put(effectiveKey, extracted)
                bitmap = extracted
            }
        }

        val validBitmap = bitmap?.takeIf { !it.isRecycled }
        if (validBitmap != null) {
            val imageBitmap = remember(validBitmap) { validBitmap.asImageBitmap() }
            Image(
                bitmap = imageBitmap,
                contentDescription = "Video Frame",
                contentScale = contentScale,
                modifier = modifier
            )
        } else if (hasError) {
            if (errorContent != null) {
                errorContent()
            } else {
                ErrorPlaceholder(modifier)
            }
        } else {
            LoadingPlaceholder(modifier)
        }
    }
}

@Composable
private fun LoadingPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
}

@Composable
private fun ErrorPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = android.R.drawable.ic_menu_gallery),
            contentDescription = "Error",
            tint = MaterialTheme.appColors.textMuted,
            modifier = Modifier.size(MaterialTheme.spacing.iconMedium)
        )
    }
}

private fun decodeThumbnailFrame(
    retriever: MediaMetadataRetriever,
    timeUs: Long
): Bitmap? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        runCatching {
            retriever.getScaledFrameAtTime(
                timeUs,
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                480,
                270
            )
        }.getOrNull()?.let { return it }
    }
    return runCatching {
        retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
    }.getOrNull()
}

private const val MAX_RANGE_DOWNLOAD_BYTES = 2L * 1024L * 1024L
private const val MAX_VIDEO_THUMB_FILES = 150
private const val VIDEO_THUMB_MAX_AGE_MS = 7L * 24L * 60L * 60L * 1000L
private const val ORPHAN_TMP_MAX_AGE_MS = 5L * 60L * 1000L
private val HEX_CHARS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')
private val thumbPruneInProgress = AtomicBoolean(false)

private fun sha256Hex(input: String): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
    val chars = CharArray(digest.size * 2)
    for (i in digest.indices) {
        val v = digest[i].toInt() and 0xFF
        chars[i * 2] = HEX_CHARS[v ushr 4]
        chars[i * 2 + 1] = HEX_CHARS[v and 0x0F]
    }
    return String(chars)
}

private suspend fun Call.awaitResponse(): Response = suspendCancellableCoroutine { cont ->
    cont.invokeOnCancellation {
        runCatching { cancel() }
    }
    enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            if (cont.isCancelled) return
            cont.resumeWithException(e)
        }

        override fun onResponse(call: Call, response: Response) {
            if (cont.isCancelled) {
                runCatching { response.close() }
                return
            }
            cont.resume(response) { _, value, _ ->
                runCatching { value.close() }
            }
        }
    })
}

private fun pruneVideoThumbDirIfNeeded(thumbDir: File) {
    if (!thumbPruneInProgress.compareAndSet(false, true)) return
    try {
        val files = thumbDir.listFiles() ?: return
        val now = System.currentTimeMillis()
        val liveThumbs = ArrayList<File>(files.size)
        for (file in files) {
            if (!file.isFile) continue
            val ageMs = now - file.lastModified()
            if (file.name.endsWith(".tmp")) {
                if (ageMs > ORPHAN_TMP_MAX_AGE_MS) {
                    runCatching { file.delete() }
                }
                continue
            }
            if (ageMs > VIDEO_THUMB_MAX_AGE_MS) {
                runCatching { file.delete() }
            } else {
                liveThumbs.add(file)
            }
        }
        if (liveThumbs.size > MAX_VIDEO_THUMB_FILES) {
            liveThumbs.sortBy { it.lastModified() }
            val excessCount = liveThumbs.size - MAX_VIDEO_THUMB_FILES
            for (i in 0 until excessCount) {
                runCatching { liveThumbs[i].delete() }
            }
        }
    } finally {
        thumbPruneInProgress.set(false)
    }
}

private suspend fun extractVideoFrameWithRange(
    context: Context,
    url: String,
    cacheKey: String
): Bitmap? = withContext(Dispatchers.IO) {
    VideoFrameCache.cache.get(cacheKey)?.let { return@withContext it }

    // 1. Kiểm tra disk cache trước để không phải tải lại 2MB và chạy MediaMetadataRetriever
    val thumbDir = File(context.cacheDir, "video_thumbs").apply { if (!exists()) mkdirs() }
    val diskCacheFile = File(thumbDir, "vf_${sha256Hex(cacheKey)}.jpg")
    if (diskCacheFile.exists() && diskCacheFile.length() > 0L) {
        runCatching { BitmapFactory.decodeFile(diskCacheFile.absolutePath) }.getOrNull()?.let {
            return@withContext it
        }
    }

    // 2. Giới hạn tối đa 2 luồng giải mã video frame cùng lúc
    VideoFrameCache.extractionSemaphore.withPermit {
        VideoFrameCache.cache.get(cacheKey)?.let { return@withPermit it }

        var tempFile: File? = null
        var retriever: MediaMetadataRetriever? = null
        var activeCall: Call? = null
        try {
            val token = com.tenli.oneview.data.local.UserSession.accessToken
            val client = VideoFrameCache.rangeHttpClient
            val requestBuilder = Request.Builder()
                .url(url)
                .header("Range", "bytes=0-${MAX_RANGE_DOWNLOAD_BYTES - 1L}") // First 2MB
            if (token.isNotEmpty() && !VideoFrameCache.isPresignedUrl(url)) {
                requestBuilder.header("Authorization", "Bearer $token")
            }
            val request = requestBuilder.build()

            val call = client.newCall(request).also { activeCall = it }
            call.awaitResponse().use { response ->
                if (!response.isSuccessful && response.code != 206) {
                    return@withPermit null
                }
                val contentType = response.header("Content-Type")?.lowercase().orEmpty()
                if (contentType.isNotEmpty() && !contentType.startsWith("video/") && !contentType.contains("octet-stream") && !VideoFrameCache.looksLikeVideoUrl(url)) {
                    return@withPermit null
                }

                val body = response.body ?: return@withPermit null
                val tmpVideo = File.createTempFile("video_frame_", ".mp4", context.cacheDir).also {
                    tempFile = it
                }
                var totalRead = 0L
                val buffer = ByteArray(16 * 1024)
                body.byteStream().use { input ->
                    FileOutputStream(tmpVideo).use { fos ->
                        while (totalRead < MAX_RANGE_DOWNLOAD_BYTES) {
                            ensureActive()
                            val toRead = minOf(buffer.size.toLong(), MAX_RANGE_DOWNLOAD_BYTES - totalRead).toInt()
                            val read = input.read(buffer, 0, toRead)
                            if (read == -1) break
                            fos.write(buffer, 0, read)
                            totalRead += read
                        }
                    }
                }
                // Nếu server bỏ qua header Range và trả 200 OK file lớn, hủy call ngay để OkHttp không hút nốt phần còn lại khi close()
                if (totalRead >= MAX_RANGE_DOWNLOAD_BYTES && response.code != 206) {
                    runCatching { call.cancel() }
                }
            }

            ensureActive()
            val videoFile = tempFile ?: return@withPermit null
            val mediaRetriever = MediaMetadataRetriever().also { retriever = it }
            mediaRetriever.setDataSource(videoFile.absolutePath)

            val tryPoints = longArrayOf(100_000L, 1_000L, 500_000L)
            var bestFrame: Bitmap? = null
            var fallbackFrame: Bitmap? = null

            for (timeUs in tryPoints) {
                ensureActive()
                val frame = decodeThumbnailFrame(mediaRetriever, timeUs) ?: continue
                if (getBrightness(frame) > 20f) {
                    bestFrame = frame
                    break
                } else if (fallbackFrame == null) {
                    fallbackFrame = frame
                } else {
                    frame.recycle()
                }
            }

            if (bestFrame != null && fallbackFrame != null && fallbackFrame !== bestFrame) {
                fallbackFrame.recycle()
            } else if (bestFrame == null) {
                bestFrame = fallbackFrame ?: mediaRetriever.getFrameAtTime()
            }

            if (bestFrame != null) {
                val maxWidth = 480
                if (bestFrame.width > maxWidth) {
                    val ratio = maxWidth.toFloat() / bestFrame.width
                    val newHeight = (bestFrame.height * ratio).toInt().coerceAtLeast(1)
                    val scaledFrame = Bitmap.createScaledBitmap(bestFrame, maxWidth, newHeight, true)
                    if (scaledFrame != bestFrame) {
                        bestFrame.recycle()
                        bestFrame = scaledFrame
                    }
                }
                // Lưu xuống disk cache nguyên tử (.tmp + rename) dưới dạng JPEG nhẹ để lần sau mở lên trong 0ms
                val tmpThumb = File(thumbDir, "${diskCacheFile.name}.${System.nanoTime()}.tmp")
                runCatching {
                    FileOutputStream(tmpThumb).use { out ->
                        bestFrame.compress(Bitmap.CompressFormat.JPEG, 82, out)
                        out.flush()
                    }
                    if (!tmpThumb.renameTo(diskCacheFile)) {
                        diskCacheFile.delete()
                        if (!tmpThumb.renameTo(diskCacheFile)) {
                            tmpThumb.delete()
                        }
                    }
                    pruneVideoThumbDirIfNeeded(thumbDir)
                }.onFailure {
                    runCatching { tmpThumb.delete() }
                }
            }

            bestFrame
        } catch (e: CancellationException) {
            runCatching { activeCall?.cancel() }
            throw e
        } catch (_: Exception) {
            null
        } finally {
            runCatching {
                retriever?.let { r ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) r.close() else r.release()
                }
            }
            runCatching { tempFile?.delete() }
        }
    }
}

private fun getBrightness(bitmap: Bitmap): Float {
    return try {
        val scaled = Bitmap.createScaledBitmap(bitmap, 10, 10, true)
        var r = 0L
        var g = 0L
        var b = 0L
        for (x in 0 until 10) {
            for (y in 0 until 10) {
                val pixel = scaled.getPixel(x, y)
                r += AndroidColor.red(pixel)
                g += AndroidColor.green(pixel)
                b += AndroidColor.blue(pixel)
            }
        }
        if (scaled !== bitmap) {
            scaled.recycle()
        }
        (r + g + b).toFloat() / 300f
    } catch (_: Exception) {
        100f
    }
}
