package com.tenli.oneview.data.network.websocket

import android.util.Log
import com.google.gson.Gson
import com.tenli.oneview.data.network.api.VmsApi
import com.tenli.oneview.model.network.CameraInfo
import com.tenli.oneview.model.network.NotifyData
import com.tenli.oneview.model.network.ReportPayload
import com.tenli.oneview.model.network.StorageStatusModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

class WebSocketManager(
    private val client: OkHttpClient,
    private val gson: Gson,
    private val vmsApi: VmsApi
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // WebSockets
    private var notifyClient: WebSocketNotifyClient? = null
    private val dataNotifyClients = mutableMapOf<String, WebSocketNotifyClient>()
    private val reportClients = mutableMapOf<Int, WebSocketReportClient>()

    // Exposed Flows
    private val _notifyEvent = MutableSharedFlow<NotifyData>(extraBufferCapacity = 10)
    val notifyEvent: SharedFlow<NotifyData> = _notifyEvent.asSharedFlow()

    private val _storageStatusMap = MutableStateFlow<Map<Int, StorageStatusModel>>(emptyMap())
    val storageStatusMap: StateFlow<Map<Int, StorageStatusModel>> = _storageStatusMap.asStateFlow()

    private val _cameraStatusMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val cameraStatusMap: StateFlow<Map<String, String>> = _cameraStatusMap.asStateFlow()

    private val _recordStatusMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val recordStatusMap: StateFlow<Map<String, String>> = _recordStatusMap.asStateFlow()

    // Status label map matching web useReportHandler.ts
    private val captureStatusLabelMap = mutableMapOf<String, StatusLabel>()
    private val recordStatusLabelMap = mutableMapOf<String, StatusLabel>()
    private val captureRedLatchMap = mutableMapOf<String, Boolean>()

    data class StatusLabel(val color: String, val label: String)

    private fun normalizeWsBaseUrl(baseUrl: String): String {
        var clean = baseUrl.trim().trimEnd('/')
        if (clean.endsWith("/api/v1")) {
            clean = clean.removeSuffix("/api/v1")
        }
        if (clean.startsWith("https://")) {
            clean = clean.replaceFirst("https://", "wss://")
        } else if (clean.startsWith("http://")) {
            clean = clean.replaceFirst("http://", "ws://")
        }
        return clean
    }

    /**
     * Connect to the main Notify WebSocket (/Data/ws/Notify — khớp websocketManager.ts dòng 102).
     */
    fun connectNotify(baseUrl: String) {
        if (notifyClient?.isConnected() == true) {
            Log.d("WebSocketManager", "Notify already connected, skipping")
            return
        }

        notifyClient?.disconnect()
        notifyClient = WebSocketNotifyClient(client, gson) { data ->
            Log.d("WebSocketManager", "Received notify event/channel: ${data.topic}")
            if (data.topic != "pong") {
                scope.launch {
                    _notifyEvent.emit(data)
                }
            }
        }

        val protocolUrl = normalizeWsBaseUrl(baseUrl)
        val wsUrl = "$protocolUrl/Data/ws/Notify"
        Log.d("WebSocketManager", "Connecting Notify WebSocket to: $wsUrl")
        notifyClient?.connect(wsUrl)
    }

    /**
     * Connect to per-type DataNotify WebSockets (/Data/ws/DataNotify/{type} — khớp websocketManager.ts dòng 107).
     */
    fun connectDataNotify(baseUrl: String, types: List<String>) {
        val protocolUrl = normalizeWsBaseUrl(baseUrl)
        val desiredUrls = types
            .filter { it.isNotBlank() && it != "all" && it != "sensor-smoke-camera" }
            .associateBy { "$protocolUrl/Data/ws/DataNotify/$it" }

        val currentUrls = dataNotifyClients.keys.toList()
        for (url in currentUrls) {
            if (!desiredUrls.containsKey(url)) {
                dataNotifyClients.remove(url)?.disconnect()
            }
        }

        for (url in desiredUrls.keys) {
            val existing = dataNotifyClients[url]
            if (existing == null) {
                val socket = WebSocketNotifyClient(client, gson) { data ->
                    if (data.topic != "pong") {
                        scope.launch {
                            _notifyEvent.emit(data)
                        }
                    }
                }
                socket.connect(url)
                dataNotifyClients[url] = socket
            } else if (!existing.isConnected()) {
                existing.connect(url)
            }
        }
    }

    /**
     * Fetch VMS list and connect report websockets
     */
    fun connectAllReports() {
        if (reportClients.isNotEmpty()) {
            Log.d("WebSocketManager", "Report clients already connected, skipping")
            return
        }
        scope.launch {
            try {
                val response = vmsApi.getVMSServiceList()
                if (response.isSuccessful) {
                    val vmsList = response.body() ?: emptyList()
                    val ids = mutableListOf(0) // local VMS
                    ids.addAll(vmsList.map { it.id })
                    connectReports(ids.distinct())
                } else {
                    connectReports(listOf(0))
                }
            } catch (e: Exception) {
                Log.e("WebSocketManager", "Failed to fetch VMS list", e)
                connectReports(listOf(0))
            }
        }
    }

    /**
     * Connect or update Report WebSockets for the given VMS IDs.
     */
    private fun connectReports(vmsIds: List<Int>) {
        val currentIds = reportClients.keys.toList()

        // Disconnect removed VMS IDs
        currentIds.forEach { id ->
            if (!vmsIds.contains(id)) {
                reportClients[id]?.disconnect()
                reportClients.remove(id)
            }
        }

        // Connect new VMS IDs
        vmsIds.forEach { id ->
            if (!reportClients.containsKey(id)) {
                scope.launch {
                    try {
                        val response = vmsApi.getReportLink(id)
                        if (response.isSuccessful && response.body() != null) {
                            val link = response.body()!!.link
                            val reportClient = WebSocketReportClient(id, client, gson) { payload, vmsId ->
                                handleReportPayload(payload, vmsId)
                            }
                            reportClient.connect(link)
                            reportClients[id] = reportClient
                        }
                    } catch (e: Exception) {
                        Log.e("WebSocketManager", "Failed to fetch report link for VMS $id", e)
                    }
                }
            }
        }
    }

    private fun handleReportPayload(payload: ReportPayload, vmsId: Int) {
        val actual = payload.data ?: payload
        // Update Storage Status
        actual.storage?.let { storageList ->
            _storageStatusMap.update { currentMap ->
                val newMap = currentMap.toMutableMap()
                storageList.forEach { s ->
                    newMap[s.id] = s
                }
                newMap
            }
        }

        // Update Camera Status
        actual.camera?.let { cameraList ->
            handleCameraReport(cameraList, vmsId)
        }
    }

    /**
     * Khớp hoàn toàn với useReportHandler.ts của PWA:
     * - captureState = cam.main?.state ?: cam.sub?.state
     * - recordState = cam.recorder?.state
     * - captureStatus.color ("green" | "orange" | "red") lưu vào cameraStatusMap
     * - recordStatus.color ("green" | "orange" | "red") lưu vào recordStatusMap
     */
    private fun handleCameraReport(items: List<CameraInfo>, vmsId: Int) {
        val nextCamMap = _cameraStatusMap.value.toMutableMap()
        val nextRecMap = _recordStatusMap.value.toMutableMap()

        for (cam in items) {
            if (cam.main == null && cam.sub == null && cam.recorder == null) continue

            val key = "cam-$vmsId-${cam.id}"
            val captureState = cam.main?.state ?: cam.sub?.state
            val captureStatus = getCaptureStatus(captureState, key)
            val recordStatus = getRecordStatus(cam.recorder?.state, key)

            captureStatusLabelMap[key] = captureStatus
            recordStatusLabelMap[key] = recordStatus

            nextCamMap[key] = captureStatus.color
            nextRecMap[key] = recordStatus.color

            if (vmsId == 0) {
                nextCamMap["cam-${cam.id}"] = captureStatus.color
                nextRecMap["cam-${cam.id}"] = recordStatus.color
            }
        }

        if (nextCamMap != _cameraStatusMap.value) {
            _cameraStatusMap.value = nextCamMap
        }
        if (nextRecMap != _recordStatusMap.value) {
            _recordStatusMap.value = nextRecMap
        }
    }

    private fun getCaptureStatus(state: String?, key: String): StatusLabel {
        if (state == null) {
            return captureStatusLabelMap[key] ?: StatusLabel("orange", "Disconnected")
        }
        return when (state) {
            "connected" -> {
                captureRedLatchMap.remove(key)
                StatusLabel("green", "Connected")
            }
            "error" -> {
                captureRedLatchMap[key] = true
                StatusLabel("red", "Connection Error")
            }
            else -> {
                if (captureRedLatchMap[key] == true) {
                    StatusLabel("red", "Connection Error")
                } else {
                    StatusLabel("orange", "Disconnected")
                }
            }
        }
    }

    private fun getRecordStatus(state: String?, key: String): StatusLabel {
        if (state == null) {
            return recordStatusLabelMap[key] ?: StatusLabel("orange", "Not Recording")
        }
        return when (state) {
            "recording" -> StatusLabel("green", "Recording")
            "error" -> StatusLabel("red", "Record Error")
            else -> StatusLabel("orange", "Not Recording")
        }
    }

    /**
     * Disconnect everything when user logs out
     */
    fun disconnectAll() {
        notifyClient?.disconnect()
        notifyClient = null

        dataNotifyClients.values.forEach { it.disconnect() }
        dataNotifyClients.clear()

        reportClients.values.forEach { it.disconnect() }
        reportClients.clear()

        _storageStatusMap.value = emptyMap()
        _cameraStatusMap.value = emptyMap()
        _recordStatusMap.value = emptyMap()
        captureStatusLabelMap.clear()
        recordStatusLabelMap.clear()
        captureRedLatchMap.clear()
    }
}
