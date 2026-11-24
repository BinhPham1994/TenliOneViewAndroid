package com.tenli.oneview.data.network.websocket

import android.util.Log
import com.google.gson.Gson
import com.tenli.oneview.BuildConfig
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.data.network.api.VmsApi
import com.tenli.oneview.model.network.CameraInfo
import com.tenli.oneview.model.network.NotifyData
import com.tenli.oneview.model.network.ReportPayload
import com.tenli.oneview.util.EventBus
import com.tenli.oneview.util.NetworkMonitor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
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
    private val reportClientsLock = Any()

    // Trạng thái mong muốn (để tự khôi phục khi app quay lại foreground)
    @Volatile private var desiredNotifyBaseUrl: String? = null
    @Volatile private var reportsRequested = false
    private val activeReportConsumers = ConcurrentHashMap.newKeySet<String>().apply { add("home") }
    private val isReportsTabActive: Boolean
        get() = activeReportConsumers.isNotEmpty()
    @Volatile private var isAppInForeground = false
    @Volatile private var suspendedForBackground = false
    @Volatile private var suspendedForOffline = false
    @Volatile private var backgroundJob: Job? = null
    @Volatile private var reportInactiveJob: Job? = null
    private val sessionGeneration = AtomicLong(0L)
    private val reportGeneration = AtomicLong(0L)

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

    init {
        scope.launch {
            EventBus.tokenRefreshed.collect { newToken ->
                if (isAppInForeground && newToken.isNotEmpty()) {
                    synchronized(notifyLock) {
                        notifyClient?.reconnectIfTokenChanged(newToken)
                    }
                    if (reportsRequested && isReportsTabActive) {
                        connectAllReports()
                    }
                }
            }
        }
        scope.launch {
            EventBus.realtimeEventHint.collect {
                if (isAppInForeground) {
                    _notifyEvent.tryEmit(NotifyData(event = TOPIC_FCM_HINT))
                }
            }
        }
    }

    data class StatusLabel(val color: String, val label: String)

    /**
     * Gọi từ Application khi app chuyển foreground/background (đếm Activity started).
     */
    fun onAppForegroundChanged(inForeground: Boolean) {
        isAppInForeground = inForeground
        backgroundJob?.cancel()
        if (inForeground) {
            if (NetworkMonitor.isNetworkAvailable()) {
                suspendedForOffline = false
            }
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
     * Gọi khi trạng thái kết nối mạng thay đổi (online ↔ offline):
     * - Khi mất mạng: tạm ngắt toàn bộ socket và huỷ lịch reconnect để tránh spam CPU/pin nhưng vẫn giữ ý định kết nối.
     * - Khi có mạng lại (và app đang ở foreground, đã đăng nhập): khôi phục lại các kết nối đang cần.
     */
    fun onConnectivityChanged(isOnline: Boolean) {
        if (!isOnline) {
            if (BuildConfig.DEBUG) Log.d(TAG, "Network offline → suspend websockets")
            suspendedForOffline = true
            synchronized(notifyLock) {
                notifyClient?.suspendForOffline()
            }
            synchronized(reportClientsLock) {
                reportGeneration.incrementAndGet()
                reportClients.values.forEach { it.suspendForOffline() }
            }
        } else {
            suspendedForOffline = false
            if (!isAppInForeground || UserSession.accessToken.isEmpty()) return
            var notifyNeedsConnect = false
            synchronized(notifyLock) {
                val existing = notifyClient
                if (existing != null) {
                    existing.resumeFromOffline()
                    notifyNeedsConnect = !existing.isConnected()
                } else {
                    notifyNeedsConnect = true
                }
            }
            if (notifyNeedsConnect) {
                desiredNotifyBaseUrl?.let { connectNotify(it) }
            }
            if (reportsRequested && isReportsTabActive) {
                val allResumed = synchronized(reportClientsLock) {
                    if (reportClients.isNotEmpty()) {
                        reportClients.values.forEach { it.resumeFromOffline() }
                        reportClients.values.all { it.isConnected() }
                    } else {
                        false
                    }
                }
                if (!allResumed) {
                    connectAllReports()
                }
            }
        }
    }

    /**
     * Chỉ giữ kết nối các WebSocket Report (trạng thái camera/ghi hình của từng VMS) khi đang ở tab Trang chủ hoặc Trực tiếp.
     * Khi rời sang các tab khác quá [REPORT_INACTIVE_GRACE_MS], tự ngắt các socket Report để không tốn CPU/mạng/pin
     * nhận bản tin JSON vài giây/lần và ping định kỳ.
     */
    fun setReportsActive(active: Boolean, consumer: String = "home") {
        if (active) {
            activeReportConsumers.add(consumer)
            reportInactiveJob?.cancel()
            if (reportsRequested && isAppInForeground) {
                connectAllReports()
            }
        } else {
            activeReportConsumers.remove(consumer)
            if (activeReportConsumers.isEmpty()) {
                reportInactiveJob?.cancel()
                reportInactiveJob = scope.launch {
                    delay(REPORT_INACTIVE_GRACE_MS)
                    if (!isReportsTabActive) {
                        synchronized(reportClientsLock) {
                            if (!isReportsTabActive) {
                                reportGeneration.incrementAndGet()
                                reportClients.values.forEach { it.disconnect() }
                                reportClients.clear()
                            }
                        }
                    }
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
        synchronized(reportClientsLock) {
            reportGeneration.incrementAndGet()
            reportClients.values.forEach { it.disconnect() }
            reportClients.clear()
        }
    }

    /**
     * Gọi khi mạng mặc định của thiết bị chuyển đổi liền mạch (vd. Wi-Fi ↔ 4G/5G):
     * chủ động đóng socket cũ (gắn với IP cũ đã rớt) và mở lại kết nối ngay lập tức, đồng thời báo UI bù dữ liệu.
     */
    fun onActiveNetworkChanged() {
        if (!isAppInForeground || UserSession.accessToken.isEmpty() || !NetworkMonitor.isNetworkAvailable()) return
        val gen = sessionGeneration.get()
        scope.launch {
            if (gen != sessionGeneration.get() || !isAppInForeground || UserSession.accessToken.isEmpty() || !NetworkMonitor.isNetworkAvailable()) return@launch
            suspendedForOffline = false
            synchronized(notifyLock) {
                val baseUrl = (desiredNotifyBaseUrl ?: UserSession.domain).trim()
                if (baseUrl.isNotEmpty()) {
                    desiredNotifyBaseUrl = baseUrl
                    val wsUrl = ReconnectingWebSocket.toWsScheme(ReconnectingWebSocket.stripApiSuffix(baseUrl)) + "/Data/ws/Notify"
                    val existing = notifyClient
                    if (existing != null) {
                        existing.forceReconnect(wsUrl)
                    } else {
                        connectNotify(baseUrl)
                    }
                }
            }
            if (reportsRequested && isReportsTabActive) {
                val shouldFetchReports = synchronized(reportClientsLock) {
                    if (reportClients.isEmpty()) {
                        true
                    } else {
                        reportClients.values.forEach { it.forceReconnect() }
                        false
                    }
                }
                if (shouldFetchReports) {
                    connectAllReports()
                }
            }
            _notifyEvent.tryEmit(NotifyData(event = TOPIC_APP_RESUMED))
        }
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
        if (!isAppInForeground || !isReportsTabActive || UserSession.accessToken.isEmpty() || !NetworkMonitor.isNetworkAvailable()) return
        val alreadyAllConnected = synchronized(reportClientsLock) {
            reportClients.isNotEmpty() && reportClients.values.all { it.isConnected() }
        }
        if (alreadyAllConnected) return
        if (!isConnectingReports.compareAndSet(false, true)) return
        val gen = sessionGeneration.get()
        val reportGen = reportGeneration.get()

        scope.launch {
            try {
                val response = vmsApi.getVMSServiceList()
                if (gen != sessionGeneration.get() ||
                    reportGen != reportGeneration.get() ||
                    !isAppInForeground ||
                    !isReportsTabActive ||
                    !reportsRequested ||
                    UserSession.accessToken.isEmpty()
                ) {
                    return@launch
                }
                val ids = if (response.isSuccessful) {
                    (listOf(LOCAL_VMS_ID) + (response.body() ?: emptyList()).map { it.id }).distinct()
                } else {
                    listOf(LOCAL_VMS_ID)
                }
                connectReports(ids, gen, reportGen)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) Log.w(TAG, "Failed to fetch VMS list: ${e.message}")
                if (gen == sessionGeneration.get() &&
                    reportGen == reportGeneration.get() &&
                    isAppInForeground &&
                    isReportsTabActive &&
                    reportsRequested &&
                    UserSession.accessToken.isNotEmpty() &&
                    NetworkMonitor.isNetworkAvailable()
                ) {
                    connectReports(listOf(LOCAL_VMS_ID), gen, reportGen)
                }
            } finally {
                isConnectingReports.set(false)
            }
        }
    }

    /**
     * Connect or update Report WebSockets for the given VMS IDs.
     */
    private suspend fun connectReports(vmsIds: List<Int>, expectedGen: Long, expectedReportGen: Long) {
        if (expectedGen != sessionGeneration.get() || expectedReportGen != reportGeneration.get()) return
        val idsToConnect = synchronized(reportClientsLock) {
            if (expectedGen != sessionGeneration.get() || expectedReportGen != reportGeneration.get()) {
                return
            }
            // Disconnect removed VMS IDs
            reportClients.keys.toList().forEach { id ->
                if (id !in vmsIds) reportClients.remove(id)?.disconnect()
            }
            vmsIds.filter { id -> reportClients[id]?.isConnected() != true }
        }
        if (idsToConnect.isEmpty()) return

        // Connect new VMS IDs in parallel and join all children before clearing isConnectingReports
        coroutineScope {
            idsToConnect.forEach { id ->
                launch {
                    try {
                        val response = vmsApi.getReportLink(id)
                        val link = response.body()?.link
                        if (response.isSuccessful && link != null) {
                            synchronized(reportClientsLock) {
                                if (expectedGen == sessionGeneration.get() &&
                                    expectedReportGen == reportGeneration.get() &&
                                    isAppInForeground &&
                                    isReportsTabActive &&
                                    reportsRequested &&
                                    UserSession.accessToken.isNotEmpty() &&
                                    NetworkMonitor.isNetworkAvailable()
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
                                    reportClient.connect(link)
                                }
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
        suspendedForOffline = false
        backgroundJob?.cancel()
        reportInactiveJob?.cancel()

        synchronized(notifyLock) {
            notifyClient?.disconnect()
            notifyClient = null
        }

        synchronized(reportClientsLock) {
            reportGeneration.incrementAndGet()
            reportClients.values.forEach { it.disconnect() }
            reportClients.clear()
        }

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
        /** Tín hiệu dự phòng từ FCM ở foreground báo có sự kiện mới trên server */
        const val TOPIC_FCM_HINT = "fcm_hint"
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
