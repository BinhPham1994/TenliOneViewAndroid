package com.tenli.oneview.ui.component

import android.net.Uri
import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.BaseDataSource
import androidx.media3.datasource.DataSpec
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
    private val dataQueue = LinkedBlockingQueue<ByteArray>()
    private var currentBuffer: ByteArray? = null
    private var currentBufferPosition = 0
    private var isOpen = false
    private var error: IOException? = null

    override fun open(dataSpec: DataSpec): Long {
        this.dataSpec = dataSpec
        var urlStr = dataSpec.uri.toString()
        if (urlStr.startsWith("http://")) urlStr = urlStr.replaceFirst("http://", "ws://")
        if (urlStr.startsWith("https://")) urlStr = urlStr.replaceFirst("https://", "wss://")
        
        Log.d("WebSocketDataSource", "Connecting to WebSocket: $urlStr")
        val uri = java.net.URI(urlStr)
        val scheme = if (uri.scheme == "wss") "https" else "http"
        val origin = "$scheme://${uri.host}${if (uri.port != -1) ":${uri.port}" else ""}"
        
        val requestBuilder = Request.Builder()
            .url(urlStr)
            .addHeader("Origin", origin)
            .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 13; Pixel 6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36")
        
        val request = requestBuilder.build()
        dataQueue.clear()
        currentBuffer = null
        currentBufferPosition = 0
        isOpen = true
        error = null
        
        transferInitializing(dataSpec)

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocketDataSource", "WebSocket Opened")
            }
            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                if (isOpen) {
                    dataQueue.put(bytes.toByteArray())
                }
            }
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocketDataSource", "WebSocket Closed: $reason")
                isOpen = false
                dataQueue.put(ByteArray(0)) // Unblock read
            }
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocketDataSource", "WebSocket Failure", t)
                isOpen = false
                error = IOException("WebSocket failure", t)
                dataQueue.put(ByteArray(0)) // Unblock read
            }
        })
        
        transferStarted(dataSpec)
        return C.LENGTH_UNSET.toLong()
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (length == 0) return 0
        
        if (error != null) throw error!!

        while (currentBuffer == null || currentBufferPosition >= currentBuffer!!.size) {
            try {
                currentBuffer = dataQueue.take()
                currentBufferPosition = 0
                if (error != null) throw error!!
                if (currentBuffer!!.isEmpty() && !isOpen) {
                    return C.RESULT_END_OF_INPUT
                }
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                return 0
            }
        }
        
        val bytesToCopy = Math.min(length, currentBuffer!!.size - currentBufferPosition)
        System.arraycopy(currentBuffer!!, currentBufferPosition, buffer, offset, bytesToCopy)
        currentBufferPosition += bytesToCopy
        
        bytesTransferred(bytesToCopy)
        return bytesToCopy
    }

    override fun getUri(): Uri? = dataSpec?.uri

    override fun close() {
        Log.d("WebSocketDataSource", "Closing WebSocket")
        val wasOpen = isOpen
        isOpen = false
        try {
            webSocket?.close(1000, "Player closed")
        } catch (e: Exception) {
            try { webSocket?.cancel() } catch (_: Exception) {}
        }
        webSocket = null
        // Unblock any thread stuck in read() waiting on dataQueue.take()
        dataQueue.put(ByteArray(0))
        if (wasOpen) {
            transferEnded()
        }
    }
}
