package com.tenli.oneview.data.network.websocket

import android.util.Log
import com.google.gson.Gson
import com.tenli.oneview.BuildConfig
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.model.network.ReportPayload
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

class WebSocketReportClient(
    val vmsId: Int,
    private val client: OkHttpClient,
    private val gson: Gson,
    private val onMessageReceived: (ReportPayload, Int) -> Unit
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

    private fun normalizeUrl(rawUrl: String): String {
        val trimmed = rawUrl.trim()
        if (trimmed.isEmpty()) return ""
        var resolved = trimmed
        if (resolved.startsWith("/")) {
            var base = UserSession.domain.ifEmpty { BuildConfig.DOMAIN_CLOUD }.trim().trimEnd('/')
            if (base.endsWith("/api/v1")) base = base.removeSuffix("/api/v1")
            resolved = "$base$resolved"
        }
        if (resolved.startsWith("http://")) resolved = resolved.replaceFirst("http://", "ws://")
        if (resolved.startsWith("https://")) resolved = resolved.replaceFirst("https://", "wss://")
        return resolved
    }

    private fun appendTokenToUrl(normalizedUrl: String): String {
        val token = UserSession.accessToken
        val base = normalizedUrl
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

        val protocolUrl = normalizeUrl(urlStr)
        if (protocolUrl.isEmpty()) return

        synchronized(lock) {
            if (webSocket != null && (isConnected || isConnecting) && this.url == urlStr) {
                Log.d("WebSocketReport", "WebSocketReport already connected/connecting for VMS $vmsId")
                return
            }

            manuallyDisconnected = false
            reconnectJob?.cancel()
            reconnectJob = null
            stopHeartbeat()

            val oldSocket = webSocket
            webSocket = null
            isConnected = false
            isConnecting = true
            try {
                oldSocket?.cancel()
            } catch (_: Exception) {
            }

            this.url = urlStr
            val finalUrl = appendTokenToUrl(protocolUrl)
            Log.d("WebSocketReport", "Connecting to Report for VMS $vmsId: $protocolUrl")

            val request = Request.Builder().url(finalUrl).build()

            webSocket = client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(ws: WebSocket, response: Response) {
                    if (ws !== this@WebSocketReportClient.webSocket) {
                        try { ws.close(1000, null) } catch (_: Exception) {}
                        return
                    }
                    Log.d("WebSocketReport", "Connected VMS $vmsId")
                    isConnecting = false
                    isConnected = true
                    reconnectDelay = 3000L
                    reconnectJob?.cancel()

                    // Subscribe to report topic
                    send(mapOf("action" to "subscribe", "topic" to "report"))
                    startHeartbeat()
                }

                override fun onMessage(ws: WebSocket, text: String) {
                    if (ws !== this@WebSocketReportClient.webSocket) return
                    try {
                        val data = gson.fromJson(text, ReportPayload::class.java)
                        if (data != null) {
                            onMessageReceived(data, vmsId)
                        }
                    } catch (_: Exception) {
                        // Report parsing error can be ignored
                    }
                }

                override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                    if (ws !== this@WebSocketReportClient.webSocket) return
                    Log.d("WebSocketReport", "Closed VMS $vmsId: $reason")
                    handleDisconnect()
                }

                override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                    if (ws !== this@WebSocketReportClient.webSocket) return
                    Log.e("WebSocketReport", "Failure VMS $vmsId", t)
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
            Log.w("WebSocketReport", "Disconnecting VMS $vmsId")
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
