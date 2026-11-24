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
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

@UnstableApi
class WebSocketDataSource(
    private val okHttpClient: OkHttpClient,
    private val token: String? = null
) : BaseDataSource(true) {

    @Volatile
    private var webSocket: WebSocket? = null
    private var dataSpec: DataSpec? = null
    // Giới hạn kích thước queue đủ chứa burst keyframe fMP4 mà không làm tràn bộ nhớ RAM
    private val dataQueue = LinkedBlockingQueue<ByteArray>(MAX_QUEUED_CHUNKS)
    private val queuedBytes = AtomicInteger(0)
    private val openGeneration = AtomicLong(0L)
    private var currentBuffer: ByteArray? = null
    private var currentBufferPosition = 0
    @Volatile
    private var isOpen = false
    @Volatile
    private var error: IOException? = null

    private fun clearQueue() {
        dataQueue.clear()
        queuedBytes.set(0)
    }

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
        clearQueue()
        currentBuffer = null
        currentBufferPosition = 0
        error = null
        isOpen = true
        val gen = openGeneration.incrementAndGet()
        
        transferInitializing(dataSpec)

        try {
            webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
                private fun isCurrentGeneration(): Boolean = openGeneration.get() == gen

                override fun onOpen(webSocket: WebSocket, response: Response) {
                    if (!isOpen || !isCurrentGeneration()) {
                        runCatching { webSocket.cancel() }
                        return
                    }
                    if (BuildConfig.DEBUG) Log.d("WebSocketDataSource", "WebSocket Opened")
                }

                override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                    if (isOpen && isCurrentGeneration()) {
                        if (bytes.size == 0) return
                        val chunk = bytes.toByteArray()
                        val newTotalBytes = queuedBytes.addAndGet(chunk.size)
                        if (newTotalBytes > MAX_QUEUED_BYTES || !dataQueue.offer(chunk)) {
                            // Không poll() bỏ 1 chunk giữa chừng vì làm hỏng cấu trúc moof/mdat của fMP4.
                            // Báo lỗi tràn bộ đệm để LiveStreamPlayer tự kết nối lại luồng mới từ keyframe.
                            error = IOException("WebSocket live buffer overflow")
                            isOpen = false
                            clearQueue()
                            dataQueue.offer(ByteArray(0))
                            runCatching { webSocket.cancel() }
                        }
                    } else {
                        // Nếu player đã đóng mà server vẫn đẩy dữ liệu video xuống, ngắt ngay socket để tránh tốn băng thông & nóng máy
                        runCatching { webSocket.cancel() }
                    }
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    if (!isCurrentGeneration()) return
                    if (BuildConfig.DEBUG) Log.d("WebSocketDataSource", "WebSocket Closed: $code $reason")
                    if (isOpen) {
                        error = IOException("WebSocket closed unexpectedly: $code $reason")
                    }
                    isOpen = false
                    clearQueue()
                    dataQueue.offer(ByteArray(0)) // Unblock read
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    if (!isCurrentGeneration()) return
                    if (BuildConfig.DEBUG) Log.e("WebSocketDataSource", "WebSocket Failure", t)
                    error = IOException("WebSocket failure", t)
                    isOpen = false
                    clearQueue()
                    dataQueue.offer(ByteArray(0)) // Unblock read
                }
            })
        } catch (e: Exception) {
            openGeneration.incrementAndGet()
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
                val nextBuffer = dataQueue.poll(15, TimeUnit.SECONDS)
                if (nextBuffer == null) {
                    error?.let { throw it }
                    if (!isOpen) {
                        return C.RESULT_END_OF_INPUT
                    } else {
                        throw IOException("WebSocket read timeout (no data received for 15s)")
                    }
                }
                if (nextBuffer.isNotEmpty()) {
                    queuedBytes.updateAndGet { cur -> (cur - nextBuffer.size).coerceAtLeast(0) }
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
        openGeneration.incrementAndGet()
        val wasOpen = isOpen
        isOpen = false
        val ws = webSocket
        webSocket = null
        currentBuffer = null
        currentBufferPosition = 0
        clearQueue()
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

    private companion object {
        const val MAX_QUEUED_CHUNKS = 256
        const val MAX_QUEUED_BYTES = 6 * 1024 * 1024
    }
}
