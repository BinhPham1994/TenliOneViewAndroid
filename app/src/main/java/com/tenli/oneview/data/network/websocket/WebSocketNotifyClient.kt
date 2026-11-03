package com.tenli.oneview.data.network.websocket

import android.util.Log
import com.google.gson.Gson
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.model.network.NotifyData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.net.URLEncoder
import kotlin.math.min
import kotlin.math.roundToLong

class WebSocketNotifyClient(
    private val client: OkHttpClient,
    private val gson: Gson,
    private val onMessageReceived: (NotifyData) -> Unit
) {
    private var webSocket: WebSocket? = null
    private var url: String = ""
    private var isConnected = false
    
    private var heartbeatJob: Job? = null
    private var reconnectJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var reconnectDelay = 3000L
    private val heartbeatInterval = 15000L

    private fun appendTokenToUrl(rawUrl: String): String {
        val token = UserSession.accessToken
        val base = rawUrl
            .replace(Regex("([?&])token=[^&]*(&|$)")) { m ->
                val p1 = m.groupValues[1]
                val p2 = m.groupValues[2]
                if (p2.isNotEmpty()) p1 else ""
            }
            .replace(Regex("[?&]$"), "")
        if (token.isEmpty()) return base
        val sep = if (base.contains("?")) "&" else "?"
        return "$base${sep}token=${URLEncoder.encode(token, "UTF-8")}"
    }

    fun connect(urlStr: String) {
        if (webSocket != null && isConnected) {
            Log.w("WebSocketNotify", "WebSocketNotify already connected")
            return
        }

        var protocolUrl = urlStr.trim()
        if (protocolUrl.startsWith("http://")) protocolUrl = protocolUrl.replaceFirst("http://", "ws://")
        if (protocolUrl.startsWith("https://")) protocolUrl = protocolUrl.replaceFirst("https://", "wss://")

        this.url = protocolUrl
        val resolvedUrl = appendTokenToUrl(protocolUrl)
        Log.d("WebSocketNotify", "Connecting to Notify: $protocolUrl")

        val request = Request.Builder().url(resolvedUrl).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocketNotify", "Connected")
                isConnected = true
                reconnectDelay = 3000L
                reconnectJob?.cancel()
                
                // Subscribe to notify topic
                send(mapOf("action" to "subscribe", "topic" to "notify"))
                startHeartbeat()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val data = gson.fromJson(text, NotifyData::class.java)
                    onMessageReceived(data)
                } catch (e: Exception) {
                    Log.e("WebSocketNotify", "Error parsing message: $text", e)
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocketNotify", "Closed: $reason")
                handleDisconnect()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocketNotify", "Failure", t)
                handleDisconnect()
            }
        })
    }

    private fun handleDisconnect() {
        isConnected = false
        stopHeartbeat()
        webSocket = null
        scheduleReconnect()
    }

    fun send(data: Map<String, Any>) {
        if (isConnected && webSocket != null) {
            val json = gson.toJson(data)
            webSocket?.send(json)
        } else {
            Log.w("WebSocketNotify", "Not connected")
        }
    }

    fun disconnect() {
        Log.w("WebSocketNotify", "Disconnecting")
        stopHeartbeat()
        reconnectJob?.cancel()
        reconnectJob = null
        try {
            webSocket?.close(1000, "User disconnected")
        } catch (e: Exception) {
            webSocket?.cancel()
        }
        webSocket = null
        isConnected = false
    }

    fun isConnected(): Boolean = isConnected

    private fun startHeartbeat() {
        stopHeartbeat()
        heartbeatJob = scope.launch {
            while (isActive) {
                delay(heartbeatInterval)
                if (isConnected) {
                    send(mapOf("action" to "ping"))
                }
            }
        }
    }

    private fun stopHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = null
    }

    private fun scheduleReconnect() {
        if (reconnectJob?.isActive == true || url.isEmpty()) return

        val currentDelay = reconnectDelay
        reconnectDelay = min((reconnectDelay * 1.5).roundToLong(), 30000L)

        reconnectJob = scope.launch {
            delay(currentDelay)
            connect(url)
        }
    }
}
