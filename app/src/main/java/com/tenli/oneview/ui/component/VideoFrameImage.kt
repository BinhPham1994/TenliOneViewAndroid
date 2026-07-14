package com.tenli.oneview.ui.component

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import android.graphics.Color as AndroidColor

object VideoFrameCache {
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8 // Use 1/8th of available memory for cache
    val cache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }
    
    // Track URLs that are confirmed to be videos (failed as images)
    val knownVideoUrls = java.util.Collections.newSetFromMap(java.util.concurrent.ConcurrentHashMap<String, Boolean>())
}

@Composable
fun VideoFrameImage(
    url: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    cacheKey: String? = null,
    errorContent: @Composable (() -> Unit)? = null
) {
    val effectiveKey = cacheKey ?: url ?: ""
    
    // Automatically fallback if we already know this URL is a video
    var isVideoFallback by remember(effectiveKey) { 
        mutableStateOf(url != null && (VideoFrameCache.knownVideoUrls.contains(effectiveKey) || url.lowercase().endsWith(".mp4"))) 
    }

    if (!isVideoFallback) {
        var imageState by remember(url) { mutableStateOf<coil.compose.AsyncImagePainter.State>(coil.compose.AsyncImagePainter.State.Empty) }
        
        Box(modifier = modifier) {
            coil.compose.AsyncImage(
                model = url,
                contentDescription = "Event Image",
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize(),
                onState = { state ->
                    imageState = state
                    if (state is coil.compose.AsyncImagePainter.State.Error) {
                        if (effectiveKey.isNotEmpty()) {
                            VideoFrameCache.knownVideoUrls.add(effectiveKey)
                        }
                        isVideoFallback = true
                    }
                }
            )
            if (imageState is coil.compose.AsyncImagePainter.State.Loading || imageState is coil.compose.AsyncImagePainter.State.Empty) {
                LoadingPlaceholder(Modifier.fillMaxSize())
            }
        }
    } else {
        // Here we handle the video frame extraction
        var bitmap by remember(effectiveKey) { mutableStateOf<Bitmap?>(if (effectiveKey.isNotEmpty()) VideoFrameCache.cache.get(effectiveKey) else null) }
        var hasError by remember(effectiveKey) { mutableStateOf(false) }
        val context = LocalContext.current

        LaunchedEffect(effectiveKey, url) {
            if (url == null || effectiveKey.isEmpty()) {
                hasError = true
                return@LaunchedEffect
            }
            if (bitmap != null) return@LaunchedEffect
            
            try {
                val extracted = extractVideoFrameWithRange(context, url)
                if (extracted == null) {
                    hasError = true
                } else {
                    bitmap = extracted
                    VideoFrameCache.cache.put(effectiveKey, extracted)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                hasError = true
            }
        }

        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
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
    Box(modifier = modifier.fillMaxSize().background(androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            color = Color.LightGray,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun ErrorPlaceholder(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().background(androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
        Icon(
            painter = painterResource(id = android.R.drawable.ic_menu_gallery),
            contentDescription = "Error",
            tint = Color.LightGray,
            modifier = Modifier.size(24.dp)
        )
    }
}

private suspend fun extractVideoFrameWithRange(context: Context, url: String): Bitmap? = withContext(Dispatchers.IO) {
    var tempFile: File? = null
    val retriever = MediaMetadataRetriever()
    try {
        val token = com.tenli.oneview.data.local.UserSession.accessToken
        val client = OkHttpClient.Builder().build()
        val requestBuilder = Request.Builder()
            .url(url)
            .header("Range", "bytes=0-2097152") // First 2MB
        if (token.isNotEmpty()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }
        val request = requestBuilder.build()
            
        val response = client.newCall(request).execute()
        if (!response.isSuccessful && response.code != 206) {
            return@withContext null
        }
        
        val body = response.body ?: return@withContext null
        tempFile = File.createTempFile("video_frame", ".mp4", context.cacheDir)
        
        FileOutputStream(tempFile).use { fos ->
            fos.write(body.bytes())
        }
        
        retriever.setDataSource(tempFile.absolutePath)
        
        // Try to get frame at 0.1s, 0.001s, 0.5s like web
        val tryPoints = listOf(100000L, 1000L, 500000L) // in microseconds
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
        
        // Fallback to default frame
        if (bestFrame == null) {
            bestFrame = retriever.getFrameAtTime()
        }
        
        if (bestFrame != null) {
            val maxWidth = 600
            if (bestFrame.width > maxWidth) {
                val ratio = maxWidth.toFloat() / bestFrame.width
                val newHeight = (bestFrame.height * ratio).toInt()
                val scaledFrame = Bitmap.createScaledBitmap(bestFrame, maxWidth, newHeight, true)
                if (scaledFrame != bestFrame) {
                    // bestFrame.recycle() // Avoid recycling as it might be used internally or cause issues, but generally it's safe to recycle the original if we created a scaled copy
                    bestFrame.recycle()
                    bestFrame = scaledFrame
                }
            }
        }
        
        return@withContext bestFrame
    } catch (e: Exception) {
        e.printStackTrace()
        return@withContext null
    } finally {
        try {
            retriever.release()
        } catch (e: Exception) {
            // ignore
        }
        tempFile?.delete()
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
    } catch (e: Exception) {
        100f // Fallback to safe brightness
    }
}
