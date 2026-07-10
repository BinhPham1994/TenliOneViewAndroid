package com.tenli.oneview.data.network.websocket

import android.util.Log
import com.google.gson.Gson
import com.tenli.oneview.model.network.ReportPayload
import kotlinx.coroutines.*
import okhttp3.*

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
    private val reconnectDelay = 3000L
    private val heartbeatInterval = 20000L

    fun connect(urlStr: String) {
        if (webSocket != null && isConnected) {
            Log.w("WebSocketReport", "WebSocketReport already connected for VMS $vmsId")
            return
        }

        var protocolUrl = urlStr
        if (protocolUrl.startsWith("http://")) protocolUrl = protocolUrl.replaceFirst("http://", "ws://")
        if (protocolUrl.startsWith("https://")) protocolUrl = protocolUrl.replaceFirst("https://", "wss://")

        this.url = protocolUrl
        Log.d("WebSocketReport", "Connecting to Report for VMS $vmsId: $url")

        val request = Request.Builder().url(protocolUrl).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocketReport", "Connected VMS $vmsId")
                isConnected = true
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

        reconnectJob = scope.launch {
            delay(reconnectDelay)
            connect(url)
        }
    }
}
