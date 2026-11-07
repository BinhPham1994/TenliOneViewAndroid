package com.tenli.oneview.ui.component

import android.net.Uri
import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.BaseDataSource
import androidx.media3.datasource.DataSpec
import com.tenli.oneview.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.io.IOException
import java.util.concurrent.LinkedBlockingQueue

@UnstableApi
class WebSocketDataSource(
    private val okHttpClient: OkHttpClient,
    private val token: String? = null
) : BaseDataSource(true) {

    private var webSocket: WebSocket? = null
    private var dataSpec: DataSpec? = null
    // Giới hạn kích thước queue đủ chứa burst keyframe fMP4 mà không làm tràn bộ nhớ RAM
    private val dataQueue = LinkedBlockingQueue<ByteArray>(256)
    private var currentBuffer: ByteArray? = null
    private var currentBufferPosition = 0
    @Volatile
    private var isOpen = false
    @Volatile
    private var error: IOException? = null

    override fun open(dataSpec: DataSpec): Long {
        this.dataSpec = dataSpec
        var urlStr = dataSpec.uri.toString().trim()
        if (urlStr.startsWith("http://")) urlStr = urlStr.replaceFirst("http://", "ws://")
        if (urlStr.startsWith("https://")) urlStr = urlStr.replaceFirst("https://", "wss://")
        
        if (BuildConfig.DEBUG) Log.d("WebSocketDataSource", "Connecting to WebSocket: $urlStr")
        val uri = runCatching { java.net.URI(urlStr) }.getOrNull()
        val scheme = if (uri?.scheme == "wss") "https" else "http"
        val host = uri?.host ?: ""
        val port = uri?.port ?: -1
        val origin = if (host.isNotEmpty()) "$scheme://$host${if (port != -1) ":$port" else ""}" else ""
        
        val request = try {
            val requestBuilder = Request.Builder()
                .url(urlStr)
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 13; Pixel 6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36")
            if (origin.isNotEmpty()) {
                requestBuilder.addHeader("Origin", origin)
            }
            requestBuilder.build()
        } catch (e: Exception) {
            isOpen = false
            throw IOException("Invalid WebSocket URL: $urlStr", e)
        }
        dataQueue.clear()
        currentBuffer = null
        currentBufferPosition = 0
        isOpen = true
        error = null
        
        transferInitializing(dataSpec)

        try {
            webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    if (BuildConfig.DEBUG) Log.d("WebSocketDataSource", "WebSocket Opened")
                }
                override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                    if (isOpen) {
                        val chunk = bytes.toByteArray()
                        if (!dataQueue.offer(chunk)) {
                            dataQueue.poll()
                            dataQueue.offer(chunk)
                        }
                    } else {
                        // Nếu player đã đóng mà server vẫn đẩy dữ liệu video xuống, ngắt ngay socket để tránh tốn băng thông & nóng máy
                        runCatching { webSocket.cancel() }
                    }
                }
                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    if (BuildConfig.DEBUG) Log.d("WebSocketDataSource", "WebSocket Closed: $reason")
                    isOpen = false
                    dataQueue.clear()
                    dataQueue.offer(ByteArray(0)) // Unblock read
                }
                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    if (BuildConfig.DEBUG) Log.e("WebSocketDataSource", "WebSocket Failure", t)
                    isOpen = false
                    error = IOException("WebSocket failure", t)
                    dataQueue.clear()
                    dataQueue.offer(ByteArray(0)) // Unblock read
                }
            })
        } catch (e: Exception) {
            isOpen = false
            throw IOException("Failed to initialize WebSocket", e)
        }
        
        transferStarted(dataSpec)
        return C.LENGTH_UNSET.toLong()
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (length == 0) return 0
        
        error?.let { throw it }

        var buf = currentBuffer
        while (buf == null || currentBufferPosition >= buf.size) {
            try {
                val nextBuffer = dataQueue.poll(15, java.util.concurrent.TimeUnit.SECONDS)
                if (nextBuffer == null) {
                    if (!isOpen) {
                        return C.RESULT_END_OF_INPUT
                    } else {
                        throw IOException("WebSocket read timeout (no data received for 15s)")
                    }
                }
                currentBufferPosition = 0
                error?.let { throw it }
                if (nextBuffer.isEmpty() && !isOpen) {
                    return C.RESULT_END_OF_INPUT
                }
                buf = nextBuffer
                currentBuffer = nextBuffer
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                return C.RESULT_END_OF_INPUT
            }
        }
        
        val bytesToCopy = Math.min(length, buf.size - currentBufferPosition)
        System.arraycopy(buf, currentBufferPosition, buffer, offset, bytesToCopy)
        currentBufferPosition += bytesToCopy
        
        bytesTransferred(bytesToCopy)
        return bytesToCopy
    }

    override fun getUri(): Uri? = dataSpec?.uri

    override fun close() {
        if (BuildConfig.DEBUG) Log.d("WebSocketDataSource", "Closing WebSocket")
        val wasOpen = isOpen
        isOpen = false
        val ws = webSocket
        webSocket = null
        currentBuffer = null
        currentBufferPosition = 0
        dataQueue.clear()
        // Unblock any thread stuck in read() waiting on dataQueue.poll()
        dataQueue.offer(ByteArray(0))
        if (ws != null) {
            try {
                if (!ws.close(1000, "Player closed")) {
                    ws.cancel()
                }
            } catch (_: Exception) {
                runCatching { ws.cancel() }
            }
        }
        if (wasOpen) {
            transferEnded()
        }
    }
}
