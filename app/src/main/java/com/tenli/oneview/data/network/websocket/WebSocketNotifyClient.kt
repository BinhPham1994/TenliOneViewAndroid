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
    @Volatile
    private var webSocket: WebSocket? = null
    @Volatile
    private var url: String = ""
    @Volatile
    private var isConnected = false
    @Volatile
    private var isConnecting = false
    @Volatile
    private var manuallyDisconnected = false
    private val lock = Any()

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
        if (UserSession.accessToken.isEmpty()) return

        var protocolUrl = urlStr.trim()
        if (protocolUrl.startsWith("http://")) protocolUrl = protocolUrl.replaceFirst("http://", "ws://")
        if (protocolUrl.startsWith("https://")) protocolUrl = protocolUrl.replaceFirst("https://", "wss://")
        if (protocolUrl.isEmpty()) return

        synchronized(lock) {
            if (webSocket != null && (isConnected || isConnecting) && this.url == protocolUrl) {
                Log.d("WebSocketNotify", "WebSocketNotify already connected/connecting")
                return
            }

            manuallyDisconnected = false
            reconnectJob?.cancel()
            reconnectJob = null
            stopHeartbeat()

            // Đóng kết nối cũ (nếu có) trước khi mở socket mới để tránh rò rỉ socket song song
            val oldSocket = webSocket
            webSocket = null
            isConnected = false
            isConnecting = true
            try {
                oldSocket?.cancel()
            } catch (_: Exception) {
            }

            this.url = protocolUrl
            val resolvedUrl = appendTokenToUrl(protocolUrl)
            Log.d("WebSocketNotify", "Connecting to Notify: $protocolUrl")

            val request = Request.Builder().url(resolvedUrl).build()

            webSocket = client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(ws: WebSocket, response: Response) {
                    if (ws !== this@WebSocketNotifyClient.webSocket) {
                        try { ws.close(1000, null) } catch (_: Exception) {}
                        return
                    }
                    Log.d("WebSocketNotify", "Connected")
                    isConnecting = false
                    isConnected = true
                    reconnectDelay = 3000L
                    reconnectJob?.cancel()

                    // Subscribe to notify topic
                    send(mapOf("action" to "subscribe", "topic" to "notify"))
                    startHeartbeat()
                }

                override fun onMessage(ws: WebSocket, text: String) {
                    if (ws !== this@WebSocketNotifyClient.webSocket) return
                    try {
                        val data = gson.fromJson(text, NotifyData::class.java)
                        if (data != null) {
                            onMessageReceived(data)
                        }
                    } catch (e: Exception) {
                        Log.e("WebSocketNotify", "Error parsing message: $text", e)
                    }
                }

                override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                    if (ws !== this@WebSocketNotifyClient.webSocket) return
                    Log.d("WebSocketNotify", "Closed: $reason")
                    handleDisconnect()
                }

                override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                    if (ws !== this@WebSocketNotifyClient.webSocket) return
                    Log.e("WebSocketNotify", "Failure", t)
                    handleDisconnect()
                }
            })
        }
    }

    private fun handleDisconnect() {
        isConnecting = false
        isConnected = false
        stopHeartbeat()
        webSocket = null
        if (!manuallyDisconnected && UserSession.accessToken.isNotEmpty()) {
            scheduleReconnect()
        }
    }

    fun send(data: Map<String, Any>) {
        val ws = webSocket
        if (isConnected && ws != null) {
            val json = gson.toJson(data)
            ws.send(json)
        }
    }

    fun disconnect() {
        synchronized(lock) {
            Log.w("WebSocketNotify", "Disconnecting")
            manuallyDisconnected = true
            isConnecting = false
            isConnected = false
            stopHeartbeat()
            reconnectJob?.cancel()
            reconnectJob = null
            val ws = webSocket
            webSocket = null
            try {
                ws?.close(1000, "User disconnected")
            } catch (_: Exception) {
                try { ws?.cancel() } catch (_: Exception) {}
            }
        }
    }

    fun isConnected(): Boolean = isConnected || isConnecting

    private fun startHeartbeat() {
        stopHeartbeat()
        heartbeatJob = scope.launch {
            while (isActive && isConnected) {
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
        if (manuallyDisconnected || reconnectJob?.isActive == true || url.isEmpty() || UserSession.accessToken.isEmpty()) return

        val currentDelay = reconnectDelay
        reconnectDelay = min((reconnectDelay * 1.5).roundToLong(), 30000L)

        reconnectJob = scope.launch {
            delay(currentDelay)
            if (!manuallyDisconnected && UserSession.accessToken.isNotEmpty()) {
                connect(url)
            }
        }
    }
}
