package com.tenli.oneview.ui.component

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.media.MediaMetadataRetriever
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

@Composable
fun VideoFrameImage(
    url: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    var isVideoFallback by remember(url) { mutableStateOf(false) }

    if (!isVideoFallback) {
        SubcomposeAsyncImage(
            model = url,
            contentDescription = "Event Image",
            contentScale = contentScale,
            modifier = modifier,
            loading = {
                LoadingPlaceholder()
            },
            error = {
                // If normal image loading fails, we fallback to video frame extraction
                LaunchedEffect(Unit) {
                    isVideoFallback = true
                }
            }
        )
    } else {
        // Here we handle the video frame extraction
        var bitmap by remember(url) { mutableStateOf<Bitmap?>(null) }
        var hasError by remember(url) { mutableStateOf(false) }
        val context = LocalContext.current

        LaunchedEffect(url) {
            if (url == null) {
                hasError = true
                return@LaunchedEffect
            }
            try {
                bitmap = extractVideoFrameWithRange(context, url)
                if (bitmap == null) hasError = true
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
            ErrorPlaceholder(modifier)
        } else {
            LoadingPlaceholder(modifier)
        }
    }
}

@Composable
private fun LoadingPlaceholder(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().background(Color(0xFFF3F4F6)), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            color = Color.LightGray,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun ErrorPlaceholder(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().background(Color(0xFFF3F4F6)), contentAlignment = Alignment.Center) {
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
        val client = OkHttpClient.Builder().build()
        val request = Request.Builder()
            .url(url)
            .header("Range", "bytes=0-2097152") // First 2MB
            .build()
            
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
