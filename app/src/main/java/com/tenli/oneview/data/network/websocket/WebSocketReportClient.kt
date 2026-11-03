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
    private var webSocket: WebSocket? = null
    private var url: String = ""
    private var isConnected = false
    
    private var heartbeatJob: Job? = null
    private var reconnectJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var reconnectDelay = 2000L
    private val heartbeatInterval = 10000L

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
        if (token.isEmpty() || normalizedUrl.contains("token=")) return normalizedUrl
        val sep = if (normalizedUrl.contains("?")) "&" else "?"
        return "$normalizedUrl${sep}token=${URLEncoder.encode(token, "UTF-8")}"
    }

    fun connect(urlStr: String) {
        if (webSocket != null && isConnected) {
            Log.w("WebSocketReport", "WebSocketReport already connected for VMS $vmsId")
            return
        }

        val protocolUrl = normalizeUrl(urlStr)
        if (protocolUrl.isEmpty()) return

        this.url = urlStr
        val finalUrl = appendTokenToUrl(protocolUrl)
        Log.d("WebSocketReport", "Connecting to Report for VMS $vmsId: $protocolUrl")

        val request = Request.Builder().url(finalUrl).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocketReport", "Connected VMS $vmsId")
                isConnected = true
                reconnectDelay = 2000L
                reconnectJob?.cancel()
                
                // Subscribe to report topic
                send(mapOf("action" to "subscribe", "topic" to "report"))
                startHeartbeat()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val data = gson.fromJson(text, ReportPayload::class.java)
                    onMessageReceived(data, vmsId)
                } catch (e: Exception) {
                    // Report parsing error can be ignored or logged as warning
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocketReport", "Closed VMS $vmsId: $reason")
                handleDisconnect()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocketReport", "Failure VMS $vmsId", t)
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
            Log.w("WebSocketReport", "Not connected VMS $vmsId")
        }
    }

    fun disconnect() {
        Log.w("WebSocketReport", "Disconnecting VMS $vmsId")
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
        reconnectDelay = min((reconnectDelay * 1.5).roundToLong(), 15000L)

        reconnectJob = scope.launch {
            delay(currentDelay)
            connect(url)
        }
    }
}
