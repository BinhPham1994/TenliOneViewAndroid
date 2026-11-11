package com.tenli.oneview.data.network.websocket

import android.util.Log
import com.google.gson.Gson
import com.tenli.oneview.BuildConfig
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.data.network.api.VmsApi
import com.tenli.oneview.model.network.CameraInfo
import com.tenli.oneview.model.network.NotifyData
import com.tenli.oneview.model.network.ReportPayload
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * Quản lý toàn bộ WebSocket realtime (Notify + Report từng VMS).
 *
 * Tiết kiệm pin / chống nóng máy:
 * - Dữ liệu WebSocket chỉ phục vụ giao diện (làm mới danh sách sự kiện, trạng thái camera). Cảnh báo khi app
 *   ở nền đã do FCM đảm nhiệm, nên khi app vào nền quá [BACKGROUND_GRACE_MS] toàn bộ socket được đóng
 *   (không còn ping 15s/lần cho mỗi socket đánh thức radio liên tục).
 * - Khi app quay lại foreground: tự kết nối lại những gì đang cần + phát 1 sự kiện làm mới để UI cập nhật dữ liệu bị lỡ.
 */
class WebSocketManager(
    clientProvider: () -> OkHttpClient,
    gsonProvider: () -> Gson,
    vmsApiProvider: () -> VmsApi
) {
    private val client: OkHttpClient by lazy(clientProvider)
    private val gson: Gson by lazy(gsonProvider)
    private val vmsApi: VmsApi by lazy(vmsApiProvider)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // WebSockets
    private var notifyClient: WebSocketNotifyClient? = null
    private val reportClients = ConcurrentHashMap<Int, WebSocketReportClient>()
    private val isConnectingReports = AtomicBoolean(false)
    private val notifyLock = Any()
    private val reportLock = Any()

    // Trạng thái mong muốn (để tự khôi phục khi app quay lại foreground)
    @Volatile private var desiredNotifyBaseUrl: String? = null
    @Volatile private var reportsRequested = false
    @Volatile private var isReportsTabActive = true
    @Volatile private var isAppInForeground = false
    @Volatile private var suspendedForBackground = false
    @Volatile private var backgroundJob: Job? = null
    @Volatile private var reportInactiveJob: Job? = null
    private val sessionGeneration = AtomicLong(0L)

    // Exposed Flows
    private val _notifyEvent = MutableSharedFlow<NotifyData>(extraBufferCapacity = 10)
    val notifyEvent: SharedFlow<NotifyData> = _notifyEvent.asSharedFlow()

    private val _cameraStatusMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val cameraStatusMap: StateFlow<Map<String, String>> = _cameraStatusMap.asStateFlow()

    private val _recordStatusMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val recordStatusMap: StateFlow<Map<String, String>> = _recordStatusMap.asStateFlow()

    // Status label map matching web useReportHandler.ts (truy cập dưới reportLock vì nhiều socket Report ghi đồng thời)
    private val captureStatusLabelMap = HashMap<String, StatusLabel>()
    private val recordStatusLabelMap = HashMap<String, StatusLabel>()
    private val captureRedLatchSet = HashSet<String>()

    data class StatusLabel(val color: String, val label: String)

    /**
     * Gọi từ Application khi app chuyển foreground/background (đếm Activity started).
     */
    fun onAppForegroundChanged(inForeground: Boolean) {
        isAppInForeground = inForeground
        backgroundJob?.cancel()
        if (inForeground) {
            if (suspendedForBackground) {
                suspendedForBackground = false
                desiredNotifyBaseUrl?.let { connectNotify(it) }
                if (reportsRequested && isReportsTabActive) connectAllReports()
                // Báo UI làm mới dữ liệu có thể đã lỡ trong lúc ở nền
                _notifyEvent.tryEmit(NotifyData(event = TOPIC_APP_RESUMED))
            } else {
                // Lần đầu vào foreground: thực hiện các kết nối đã được yêu cầu trước đó
                desiredNotifyBaseUrl?.let { connectNotify(it) }
                if (reportsRequested && isReportsTabActive) connectAllReports()
            }
        } else {
            backgroundJob = scope.launch {
                delay(BACKGROUND_GRACE_MS)
                if (!isAppInForeground) suspendConnections()
            }
        }
    }

    /**
     * Chỉ giữ kết nối các WebSocket Report (trạng thái camera/ghi hình của từng VMS) khi đang ở tab Trang chủ.
     * Khi rời sang tab khác quá [REPORT_INACTIVE_GRACE_MS], tự ngắt các socket Report để không tốn CPU/mạng/pin
     * nhận bản tin JSON vài giây/lần và ping 15s/lần.
     */
    fun setReportsActive(active: Boolean) {
        isReportsTabActive = active
        reportInactiveJob?.cancel()
        if (active) {
            if (reportsRequested && isAppInForeground) {
                connectAllReports()
            }
        } else {
            reportInactiveJob = scope.launch {
                delay(REPORT_INACTIVE_GRACE_MS)
                if (!isReportsTabActive) {
                    reportClients.values.forEach { it.disconnect() }
                    reportClients.clear()
                }
            }
        }
    }

    /** Đóng socket khi app ở nền lâu nhưng giữ trạng thái mong muốn để khôi phục */
    private fun suspendConnections() {
        if (BuildConfig.DEBUG) Log.d(TAG, "App in background → suspend websockets")
        sessionGeneration.incrementAndGet()
        suspendedForBackground = true
        reportInactiveJob?.cancel()
        synchronized(notifyLock) { notifyClient?.disconnect() }
        reportClients.values.forEach { it.disconnect() }
        reportClients.clear()
    }

    /**
     * Connect to the main Notify WebSocket (/Data/ws/Notify — khớp websocketManager.ts dòng 102).
     */
    fun connectNotify(baseUrl: String) {
        desiredNotifyBaseUrl = baseUrl
        if (!isAppInForeground || UserSession.accessToken.isEmpty()) return
        val gen = sessionGeneration.get()

        scope.launch {
            synchronized(notifyLock) {
                if (gen != sessionGeneration.get() || !isAppInForeground || UserSession.accessToken.isEmpty()) {
                    return@launch
                }
                if (notifyClient?.isConnected() == true) return@launch

                val clientInstance = notifyClient ?: WebSocketNotifyClient(client, gson, scope) { data ->
                    if (data.topic != TOPIC_PONG) {
                        _notifyEvent.tryEmit(data)
                    }
                }.also { notifyClient = it }

                val wsUrl = ReconnectingWebSocket.toWsScheme(ReconnectingWebSocket.stripApiSuffix(baseUrl)) + "/Data/ws/Notify"
                clientInstance.connect(wsUrl)
            }
        }
    }

    /**
     * Fetch VMS list and connect report websockets
     */
    fun connectAllReports() {
        reportsRequested = true
        if (!isAppInForeground || !isReportsTabActive || UserSession.accessToken.isEmpty()) return
        if (reportClients.isNotEmpty() && reportClients.values.all { it.isConnected() }) return
        if (!isConnectingReports.compareAndSet(false, true)) return
        val gen = sessionGeneration.get()

        scope.launch {
            try {
                val response = vmsApi.getVMSServiceList()
                if (gen != sessionGeneration.get() || !isAppInForeground || !isReportsTabActive || !reportsRequested || UserSession.accessToken.isEmpty()) {
                    return@launch
                }
                val ids = if (response.isSuccessful) {
                    (listOf(LOCAL_VMS_ID) + (response.body() ?: emptyList()).map { it.id }).distinct()
                } else {
                    listOf(LOCAL_VMS_ID)
                }
                connectReports(ids, gen)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) Log.w(TAG, "Failed to fetch VMS list: ${e.message}")
                if (gen == sessionGeneration.get() && isAppInForeground && isReportsTabActive && reportsRequested && UserSession.accessToken.isNotEmpty()) {
                    connectReports(listOf(LOCAL_VMS_ID), gen)
                }
            } finally {
                isConnectingReports.set(false)
            }
        }
    }

    /**
     * Connect or update Report WebSockets for the given VMS IDs.
     */
    private fun connectReports(vmsIds: List<Int>, expectedGen: Long) {
        if (expectedGen != sessionGeneration.get()) return
        // Disconnect removed VMS IDs
        reportClients.keys.toList().forEach { id ->
            if (id !in vmsIds) reportClients.remove(id)?.disconnect()
        }

        // Connect new VMS IDs
        vmsIds.forEach { id ->
            if (reportClients[id]?.isConnected() == true) return@forEach
            scope.launch {
                try {
                    val response = vmsApi.getReportLink(id)
                    val link = response.body()?.link
                    if (expectedGen == sessionGeneration.get() &&
                        response.isSuccessful &&
                        link != null &&
                        isAppInForeground &&
                        isReportsTabActive &&
                        reportsRequested &&
                        UserSession.accessToken.isNotEmpty()
                    ) {
                        val reportClient = reportClients.getOrPut(id) {
                            WebSocketReportClient(
                                vmsId = id,
                                client = client,
                                gson = gson,
                                scope = scope,
                                onMessageReceived = ::handleReportPayload,
                                onReconnectRefreshUrl = { vmsApi.getReportLink(id).body()?.link }
                            )
                        }
                        if (expectedGen == sessionGeneration.get() && isAppInForeground && isReportsTabActive && reportsRequested) {
                            reportClient.connect(link)
                        } else {
                            reportClients.remove(id)?.disconnect()
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    if (BuildConfig.DEBUG) Log.w(TAG, "Failed to fetch report link for VMS $id: ${e.message}")
                }
            }
        }
    }

    private fun handleReportPayload(payload: ReportPayload, vmsId: Int) {
        val actual = payload.data ?: payload
        // Chỉ xử lý trạng thái Camera (bỏ qua storage vì UI mobile không hiển thị dữ liệu ổ lưu trữ)
        actual.camera?.let { cameraList -> handleCameraReport(cameraList, vmsId) }
    }

    /**
     * Khớp hoàn toàn với useReportHandler.ts của PWA:
     * - captureState = cam.main?.state ?: cam.sub?.state
     * - recordState = cam.recorder?.state
     * - captureStatus.color ("green" | "orange" | "red") lưu vào cameraStatusMap
     * - recordStatus.color ("green" | "orange" | "red") lưu vào recordStatusMap
     */
    private fun handleCameraReport(items: List<CameraInfo>, vmsId: Int) = synchronized(reportLock) {
        val nextCamMap = _cameraStatusMap.value.toMutableMap()
        val nextRecMap = _recordStatusMap.value.toMutableMap()

        for (cam in items) {
            if (cam.main == null && cam.sub == null && cam.recorder == null) continue

            val key = "cam-$vmsId-${cam.id}"
            val captureStatus = getCaptureStatus(cam.main?.state ?: cam.sub?.state, key)
            val recordStatus = getRecordStatus(cam.recorder?.state, key)

            captureStatusLabelMap[key] = captureStatus
            recordStatusLabelMap[key] = recordStatus

            nextCamMap[key] = captureStatus.color
            nextRecMap[key] = recordStatus.color

            if (vmsId == LOCAL_VMS_ID) {
                nextCamMap["cam-${cam.id}"] = captureStatus.color
                nextRecMap["cam-${cam.id}"] = recordStatus.color
            }
        }

        // Chỉ phát giá trị mới khi thực sự thay đổi → tránh recompose UI vô ích mỗi lần report (vài giây/lần)
        if (nextCamMap != _cameraStatusMap.value) _cameraStatusMap.value = nextCamMap
        if (nextRecMap != _recordStatusMap.value) _recordStatusMap.value = nextRecMap
    }

    private fun getCaptureStatus(state: String?, key: String): StatusLabel {
        if (state == null) {
            return captureStatusLabelMap[key] ?: STATUS_DISCONNECTED
        }
        return when (state) {
            "connected" -> {
                captureRedLatchSet.remove(key)
                STATUS_CONNECTED
            }
            "error" -> {
                captureRedLatchSet.add(key)
                STATUS_CONNECTION_ERROR
            }
            else -> if (key in captureRedLatchSet) STATUS_CONNECTION_ERROR else STATUS_DISCONNECTED
        }
    }

    private fun getRecordStatus(state: String?, key: String): StatusLabel {
        if (state == null) {
            return recordStatusLabelMap[key] ?: STATUS_NOT_RECORDING
        }
        return when (state) {
            "recording" -> STATUS_RECORDING
            "error" -> STATUS_RECORD_ERROR
            else -> STATUS_NOT_RECORDING
        }
    }

    /**
     * Disconnect everything when user logs out
     */
    fun disconnectAll() {
        sessionGeneration.incrementAndGet()
        desiredNotifyBaseUrl = null
        reportsRequested = false
        suspendedForBackground = false
        backgroundJob?.cancel()
        reportInactiveJob?.cancel()

        synchronized(notifyLock) {
            notifyClient?.disconnect()
            notifyClient = null
        }

        reportClients.values.forEach { it.disconnect() }
        reportClients.clear()

        _cameraStatusMap.value = emptyMap()
        _recordStatusMap.value = emptyMap()
        synchronized(reportLock) {
            captureStatusLabelMap.clear()
            recordStatusLabelMap.clear()
            captureRedLatchSet.clear()
        }
    }

    companion object {
        private const val TAG = "WebSocketManager"
        private const val LOCAL_VMS_ID = 0
        private const val TOPIC_PONG = "pong"
        /** Sự kiện nội bộ phát khi app quay lại foreground sau khi socket bị tạm ngắt */
        const val TOPIC_APP_RESUMED = "app_resumed"
        /** Thời gian chờ trước khi đóng socket khi app vào nền (tránh đóng/mở liên tục khi chuyển app nhanh) */
        private const val BACKGROUND_GRACE_MS = 30_000L
        /** Thời gian chờ trước khi đóng các socket Report khi người dùng rời khỏi tab Trang chủ */
        private const val REPORT_INACTIVE_GRACE_MS = 15_000L

        private val STATUS_CONNECTED = StatusLabel("green", "Connected")
        private val STATUS_CONNECTION_ERROR = StatusLabel("red", "Connection Error")
        private val STATUS_DISCONNECTED = StatusLabel("orange", "Disconnected")
        private val STATUS_RECORDING = StatusLabel("green", "Recording")
        private val STATUS_RECORD_ERROR = StatusLabel("red", "Record Error")
        private val STATUS_NOT_RECORDING = StatusLabel("orange", "Not Recording")
    }
}
