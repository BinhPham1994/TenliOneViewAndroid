package com.tenli.oneview.ui.component

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream

@Composable
fun MjpegStreamPlayer(url: String, thumbnailUrl: String?, deviceKey: String) {
    var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var isError by remember { mutableStateOf(false) }
    val client = remember { OkHttpClient.Builder().connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS).build() }

    LaunchedEffect(url) {
        if (url.isBlank()) {
            isError = true; return@LaunchedEffect
        }
        isError = false
        withContext(Dispatchers.IO) {
            var call: okhttp3.Call? = null
            try {
                val request = Request.Builder().url(url).addHeader("Authorization", "Bearer $deviceKey").build()
                call = client.newCall(request)
                call.execute().use { response ->
                    if (!response.isSuccessful) {
                        isError = true; return@use
                    }
                    val inputStream = BufferedInputStream(response.body.byteStream())
                    val buffer = ByteArrayOutputStream()
                    val data = ByteArray(8192)

                    while (isActive) {
                        val bytesRead = inputStream.read(data)
                        if (bytesRead == -1) break
                        buffer.write(data, 0, bytesRead)
                        
                        val fullBytes = buffer.toByteArray()
                        val endIdx = findJpegEnd(fullBytes)
                        
                        if (endIdx != -1) {
                            val startIdx = findJpegStartBackwards(fullBytes, endIdx)
                            if (startIdx != -1) {
                                val cleanBytes = fullBytes.copyOfRange(startIdx, endIdx + 2)
                                val decoded = BitmapFactory.decodeByteArray(cleanBytes, 0, cleanBytes.size)
                                if (decoded != null) {
                                    bitmap = decoded
                                }
                            }
                            
                            buffer.reset()
                            val remaining = fullBytes.size - (endIdx + 2)
                            if (remaining > 0) {
                                buffer.write(fullBytes, endIdx + 2, remaining)
                            }
                        }
                    }
                }
            } catch (_: Exception) {
                if (isActive) isError = true
            } finally {
                call?.cancel()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            bitmap?.recycle()
            bitmap = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black), contentAlignment = Alignment.Center
    ) {
        SafeAsyncImage(thumbnailUrl, deviceKey)
        bitmap?.let {
            Image(bitmap = it.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
        }
        if (bitmap == null && !isError) {
            CircularProgressIndicator(modifier = Modifier.size(40.dp), color = Color.White)
        }
    }
}

private fun findJpegStartBackwards(bytes: ByteArray, endIdx: Int): Int {
    for (i in endIdx - 1 downTo 0) {
        if (bytes[i].toInt() and 0xFF == 0xFF && bytes[i + 1].toInt() and 0xFF == 0xD8) {
            return i
        }
    }
    return -1
}

private fun findJpegEnd(bytes: ByteArray): Int {
    for (i in 0 until bytes.size - 1) {
        if (bytes[i].toInt() and 0xFF == 0xFF && bytes[i + 1].toInt() and 0xFF == 0xD9) {
            return i
        }
    }
    return -1
}