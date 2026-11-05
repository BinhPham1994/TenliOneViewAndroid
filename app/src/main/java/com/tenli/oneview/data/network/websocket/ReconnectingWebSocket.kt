package com.tenli.oneview.data.network.websocket

import android.util.Log
import com.google.gson.Gson
import com.tenli.oneview.BuildConfig
import com.tenli.oneview.data.local.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
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
import kotlin.random.Random

/**
 * WebSocket dùng chung cho Notify / Report (trước đây là 2 class gần như trùng lặp 100%).
 *
 * - Tự gắn `?token=` mới nhất mỗi lần kết nối.
 * - Tự kết nối lại với exponential backoff (3s → x1.5 → tối đa 30s) + jitter để nhiều socket
 *   không reconnect dồn cùng lúc.
 * - Heartbeat mức ứng dụng (`{"action":"ping"}`) khớp với websocketManager.ts của PWA.
 * - Dùng chung [scope] của WebSocketManager (không tự tạo CoroutineScope riêng không bao giờ hủy).
 */
abstract class ReconnectingWebSocket(
    private val client: OkHttpClient,
    protected val gson: Gson,
    private val scope: CoroutineScope,
    private val logTag: String,
    private val subscribeTopic: String,
    private val preserveExistingUrlToken: Boolean = false
) {
    @Volatile private var webSocket: WebSocket? = null
    /** URL gốc (chưa chuẩn hóa) – được chuẩn hóa lại mỗi lần connect để luôn theo domain/token hiện tại */
    @Volatile private var rawUrl: String = ""
    @Volatile private var isConnected = false
    @Volatile private var isConnecting = false
    @Volatile private var manuallyDisconnected = false
    @Volatile private var hasReceivedMessage = false
    private val lock = Any()

    @Volatile private var heartbeatJob: Job? = null
    @Volatile private var subscribeRetryJob: Job? = null
    @Volatile private var reconnectJob: Job? = null
    @Volatile private var reconnectDelay = INITIAL_RECONNECT_DELAY_MS

    /** Xử lý 1 message text nhận được (chạy trên thread của OkHttp) */
    protected abstract fun onTextMessage(text: String)

    fun connect(urlStr: String) {
        if (UserSession.accessToken.isEmpty()) return
        val protocolUrl = normalizeUrl(urlStr)
        if (protocolUrl.isEmpty()) return

        synchronized(lock) {
            if (webSocket != null && (isConnected || isConnecting) && rawUrl == urlStr) {
                return
            }

            manuallyDisconnected = false
            hasReceivedMessage = false
            reconnectJob?.cancel()
            reconnectJob = null
            subscribeRetryJob?.cancel()
            subscribeRetryJob = null
            stopHeartbeat()

            // Đóng kết nối cũ (nếu có) trước khi mở socket mới để tránh rò rỉ socket song song
            val oldSocket = webSocket
            webSocket = null
            isConnected = false
            isConnecting = true
            runCatching { oldSocket?.cancel() }

            rawUrl = urlStr
            val finalUrl = appendTokenToUrl(protocolUrl, preserveExistingToken = preserveExistingUrlToken)
            if (BuildConfig.DEBUG) Log.d(logTag, "Connecting: $protocolUrl")
            val request = Request.Builder().url(finalUrl).build()
            webSocket = client.newWebSocket(request, Listener())
        }
    }

    fun send(data: Map<String, Any>) {
        val ws = webSocket
        if (isConnected && ws != null) {
            ws.send(gson.toJson(data))
        }
    }

    fun disconnect() {
        synchronized(lock) {
            manuallyDisconnected = true
            isConnecting = false
            isConnected = false
            hasReceivedMessage = false
            stopHeartbeat()
            subscribeRetryJob?.cancel()
            subscribeRetryJob = null
            reconnectJob?.cancel()
            reconnectJob = null
            val ws = webSocket
            webSocket = null
            try {
                ws?.close(NORMAL_CLOSURE, "User disconnected")
            } catch (_: Exception) {
                runCatching { ws?.cancel() }
            }
        }
    }

    /** true khi đang kết nối hoặc đã kết nối */
    fun isConnected(): Boolean = isConnected || isConnecting

    private inner class Listener : WebSocketListener() {
        private fun isCurrent(ws: WebSocket) = ws === this@ReconnectingWebSocket.webSocket

        override fun onOpen(webSocket: WebSocket, response: Response) {
            if (!isCurrent(webSocket)) {
                runCatching { webSocket.close(NORMAL_CLOSURE, null) }
                return
            }
            if (BuildConfig.DEBUG) Log.d(logTag, "Connected")
            isConnecting = false
            isConnected = true
            hasReceivedMessage = false
            reconnectDelay = INITIAL_RECONNECT_DELAY_MS
            reconnectJob?.cancel()
            send(mapOf("action" to "subscribe", "topic" to subscribeTopic))
            // Khớp websocketReport.ts: gửi lại subscribe 1 lần sau 3.5s nếu chưa nhận dữ liệu
            subscribeRetryJob?.cancel()
            subscribeRetryJob = scope.launch {
                delay(SUBSCRIBE_RETRY_DELAY_MS)
                if (isCurrent(webSocket) && isConnected && !hasReceivedMessage) {
                    send(mapOf("action" to "subscribe", "topic" to subscribeTopic))
                }
            }
            startHeartbeat()
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            if (!isCurrent(webSocket)) return
            hasReceivedMessage = true
            subscribeRetryJob?.cancel()
            subscribeRetryJob = null
            try {
                onTextMessage(text)
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) Log.w(logTag, "Error handling message", e)
            }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            if (!isCurrent(webSocket)) return
            if (BuildConfig.DEBUG) Log.d(logTag, "Closed: $reason")
            handleDisconnect()
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            if (!isCurrent(webSocket)) return
            if (BuildConfig.DEBUG) Log.w(logTag, "Failure: ${t.message}")
            handleDisconnect()
        }
    }

    private fun handleDisconnect() {
        isConnecting = false
        isConnected = false
        hasReceivedMessage = false
        subscribeRetryJob?.cancel()
        subscribeRetryJob = null
        stopHeartbeat()
        webSocket = null
        if (!manuallyDisconnected && UserSession.accessToken.isNotEmpty()) {
            scheduleReconnect()
        }
    }

    private fun startHeartbeat() {
        stopHeartbeat()
        heartbeatJob = scope.launch {
            while (isActive && isConnected) {
                delay(HEARTBEAT_INTERVAL_MS)
                if (isConnected) send(PING_MESSAGE)
            }
        }
    }

    private fun stopHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = null
    }

    private fun scheduleReconnect() {
        if (manuallyDisconnected || reconnectJob?.isActive == true || rawUrl.isEmpty() || UserSession.accessToken.isEmpty()) return

        val currentDelay = reconnectDelay + Random.nextLong(RECONNECT_JITTER_MS)
        reconnectDelay = min((reconnectDelay * BACKOFF_MULTIPLIER).roundToLong(), MAX_RECONNECT_DELAY_MS)

        reconnectJob = scope.launch {
            delay(currentDelay)
            if (!manuallyDisconnected && UserSession.accessToken.isNotEmpty()) {
                connect(rawUrl)
            }
        }
    }

    companion object {
        private const val NORMAL_CLOSURE = 1000
        private const val INITIAL_RECONNECT_DELAY_MS = 3_000L
        private const val MAX_RECONNECT_DELAY_MS = 30_000L
        private const val RECONNECT_JITTER_MS = 1_000L
        private const val BACKOFF_MULTIPLIER = 1.5
        private const val HEARTBEAT_INTERVAL_MS = 15_000L
        private const val SUBSCRIBE_RETRY_DELAY_MS = 3_500L
        private val PING_MESSAGE = mapOf("action" to "ping")
        private val TOKEN_PARAM_REGEX = Regex("([?&])token=[^&]*(&|$)")
        private val EXISTING_TOKEN_VALUE_REGEX = Regex("[?&]token=([^&]+)")
        private val TRAILING_SEPARATOR_REGEX = Regex("[?&]$")

        /** Bỏ hậu tố /api/v1 của domain cloud để lấy gốc server */
        fun stripApiSuffix(url: String): String {
            val clean = url.trim().trimEnd('/')
            return if (clean.endsWith("/api/v1")) clean.removeSuffix("/api/v1") else clean
        }

        /** http(s):// → ws(s):// */
        fun toWsScheme(url: String): String = when {
            url.startsWith("https://") -> url.replaceFirst("https://", "wss://")
            url.startsWith("http://") -> url.replaceFirst("http://", "ws://")
            else -> url
        }

        /**
         * Chuẩn hóa URL: hỗ trợ link tương đối (bắt đầu bằng "/") theo domain hiện tại, đổi scheme sang ws/wss.
         */
        fun normalizeUrl(rawUrl: String): String {
            val trimmed = rawUrl.trim()
            if (trimmed.isEmpty()) return ""
            val absolute = if (trimmed.startsWith("/")) {
                stripApiSuffix(UserSession.domain) + trimmed
            } else {
                trimmed
            }
            return toWsScheme(absolute)
        }

        /**
         * Thay/gắn tham số token mới nhất vào URL.
         * Nếu [preserveExistingToken] = true (dành cho ReportLink của VMS nhánh có sẵn token riêng),
         * giữ nguyên token có sẵn trong URL giống websocketReport.ts dòng 103-107.
         */
        fun appendTokenToUrl(normalizedUrl: String, preserveExistingToken: Boolean = false): String {
            if (preserveExistingToken) {
                val existingToken = EXISTING_TOKEN_VALUE_REGEX.find(normalizedUrl)?.groupValues?.getOrNull(1)?.trim()
                if (!existingToken.isNullOrEmpty()) {
                    return normalizedUrl
                }
            }
            val token = UserSession.accessToken
            val base = normalizedUrl
                .replace(TOKEN_PARAM_REGEX) { m -> if (m.groupValues[2].isNotEmpty()) m.groupValues[1] else "" }
                .replace(TRAILING_SEPARATOR_REGEX, "")
            if (token.isEmpty()) return base
            val sep = if (base.contains("?")) "&" else "?"
            return "$base${sep}token=${URLEncoder.encode(token, "UTF-8")}"
        }
    }
}

/** WebSocket kênh Notify (/Data/ws/Notify) */
class WebSocketNotifyClient(
    client: OkHttpClient,
    gson: Gson,
    scope: CoroutineScope,
    private val onMessageReceived: (com.tenli.oneview.model.network.NotifyData) -> Unit
) : ReconnectingWebSocket(client, gson, scope, "WebSocketNotify", "notify", preserveExistingUrlToken = false) {
    override fun onTextMessage(text: String) {
        gson.fromJson(text, com.tenli.oneview.model.network.NotifyData::class.java)?.let(onMessageReceived)
    }
}

/** WebSocket kênh Report của từng VMS (trạng thái camera / ghi hình / ổ lưu trữ) */
class WebSocketReportClient(
    val vmsId: Int,
    client: OkHttpClient,
    gson: Gson,
    scope: CoroutineScope,
    private val onMessageReceived: (com.tenli.oneview.model.network.ReportPayload, Int) -> Unit
) : ReconnectingWebSocket(client, gson, scope, "WebSocketReport-$vmsId", "report", preserveExistingUrlToken = true) {
    override fun onTextMessage(text: String) {
        gson.fromJson(text, com.tenli.oneview.model.network.ReportPayload::class.java)?.let { onMessageReceived(it, vmsId) }
    }
}

