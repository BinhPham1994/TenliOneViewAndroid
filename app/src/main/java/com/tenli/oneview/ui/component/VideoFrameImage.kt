package com.tenli.oneview.ui.component

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import android.graphics.Color as AndroidColor

object VideoFrameCache {
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = (maxMemory / 10).coerceAtLeast(4096)
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
                            VideoFrameCache.failedUrls.add(effectiveKey)
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
                VideoFrameCache.failedUrls.add(effectiveKey)
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

private suspend fun extractVideoFrameWithRange(
    context: Context,
    url: String,
    cacheKey: String
): Bitmap? = withContext(Dispatchers.IO) {
    // 1. Kiểm tra disk cache trước để không phải tải lại 2MB và chạy MediaMetadataRetriever
    val thumbDir = File(context.cacheDir, "video_thumbs").apply { if (!exists()) mkdirs() }
    val diskCacheFile = File(thumbDir, "vf_${cacheKey.hashCode().toUInt()}.jpg")
    if (diskCacheFile.exists() && diskCacheFile.length() > 0L) {
        runCatching { BitmapFactory.decodeFile(diskCacheFile.absolutePath) }.getOrNull()?.let {
            return@withContext it
        }
    }

    // 2. Giới hạn tối đa 2 luồng giải mã video frame cùng lúc
    VideoFrameCache.extractionSemaphore.withPermit {
        VideoFrameCache.cache.get(cacheKey)?.let { return@withPermit it }

        var tempFile: File? = null
        val retriever = MediaMetadataRetriever()
        try {
            val token = com.tenli.oneview.data.local.UserSession.accessToken
            val client = VideoFrameCache.rangeHttpClient
            val requestBuilder = Request.Builder()
                .url(url)
                .header("Range", "bytes=0-2097152") // First 2MB
            if (token.isNotEmpty()) {
                requestBuilder.header("Authorization", "Bearer $token")
            }
            val request = requestBuilder.build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful && response.code != 206) {
                    return@withPermit null
                }
                val contentType = response.header("Content-Type")?.lowercase().orEmpty()
                if (contentType.isNotEmpty() && !contentType.startsWith("video/") && !contentType.contains("octet-stream") && !VideoFrameCache.looksLikeVideoUrl(url)) {
                    return@withPermit null
                }

                val body = response.body ?: return@withPermit null
                tempFile = File.createTempFile("video_frame_", ".mp4", context.cacheDir)
                FileOutputStream(tempFile).use { fos ->
                    body.byteStream().copyTo(fos)
                }
            }

            val videoFile = tempFile ?: return@withPermit null
            retriever.setDataSource(videoFile.absolutePath)

            val tryPoints = longArrayOf(100_000L, 1_000L, 500_000L)
            var bestFrame: Bitmap? = null

            for (timeUs in tryPoints) {
                val frame = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                if (frame != null) {
                    if (getBrightness(frame) > 20f) {
                        bestFrame = frame
                        break
                    }
                }
            }

            if (bestFrame == null) {
                bestFrame = retriever.getFrameAtTime()
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
                // Lưu xuống disk cache dưới dạng JPEG nhẹ để lần sau mở lên trong 0ms
                runCatching {
                    FileOutputStream(diskCacheFile).use { out ->
                        bestFrame.compress(Bitmap.CompressFormat.JPEG, 82, out)
                    }
                }
            }

            bestFrame
        } catch (_: Exception) {
            null
        } finally {
            runCatching { retriever.release() }
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
        scaled.recycle()
        (r + g + b).toFloat() / 300f
    } catch (_: Exception) {
        100f
    }
}
