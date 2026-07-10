package com.tenli.oneview.data.network.websocket

import android.util.Log
import com.google.gson.Gson
import com.tenli.oneview.data.local.GlobalData
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
    private val reportClients = mutableMapOf<Int, WebSocketReportClient>()

    // Exposed Flows
    private val _notifyEvent = MutableSharedFlow<NotifyData>(extraBufferCapacity = 10)
    val notifyEvent: SharedFlow<NotifyData> = _notifyEvent.asSharedFlow()

    private val _storageStatusMap = MutableStateFlow<Map<Int, StorageStatusModel>>(emptyMap())
    val storageStatusMap: StateFlow<Map<Int, StorageStatusModel>> = _storageStatusMap.asStateFlow()

    private val _cameraStatusMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val cameraStatusMap: StateFlow<Map<String, String>> = _cameraStatusMap.asStateFlow()

    // Status label map matching web (for internal processing if needed)
    private val captureStatusLabelMap = mutableMapOf<String, StatusLabel>()
    private val recordStatusLabelMap = mutableMapOf<String, StatusLabel>()

    data class StatusLabel(val color: String, val label: String)

    /**
     * Connect to the main Notify WebSocket.
     */
    fun connectNotify(baseUrl: String) {
        if (notifyClient?.isConnected() == true) return
        
        notifyClient = WebSocketNotifyClient(client, gson) { data ->
            if (data.event == "ai-data") {
                scope.launch { _notifyEvent.emit(data) }
            }
        }
        
        val protocolUrl = baseUrl.replaceFirst("http", "ws")
        notifyClient?.connect("$protocolUrl/ws/Notify")
    }

    /**
     * Fetch VMS list and connect report websockets
     */
    fun connectAllReports() {
        scope.launch {
            try {
                val response = vmsApi.getVMSServiceList()
                if (response.isSuccessful) {
                    val vmsList = response.body() ?: emptyList()
                    val ids = mutableListOf(0) // local VMS
                    ids.addAll(vmsList.map { it.id })
                    connectReports(ids)
                } else {
                    // Fallback to just 0
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
        // Update Storage Status
        payload.storage?.let { storageList ->
            _storageStatusMap.update { currentMap ->
                val newMap = currentMap.toMutableMap()
                storageList.forEach { s ->
                    newMap[s.id] = s
                }
                newMap
            }
        }

        // Update Camera Status
        payload.camera?.let { cameraList ->
            handleCameraReport(cameraList, vmsId)
        }
    }

    private fun handleCameraReport(items: List<CameraInfo>, vmsId: Int) {
        _cameraStatusMap.update { currentMap ->
            val newMap = currentMap.toMutableMap()
            
            for (cam in items) {
                if (cam.main == null && cam.recorder == null) continue
                
                val key = "cam-$vmsId-${cam.id}"
                val captureStatus = getCaptureStatus(cam.main?.state, key)
                val recordStatus = getRecordStatus(cam.recorder?.state)

                captureStatusLabelMap[key] = captureStatus
                recordStatusLabelMap[key] = recordStatus

                if (captureStatus.color == "red" || recordStatus.color == "red") {
                    newMap[key] = "red"
                } else {
                    newMap[key] = "green"
                }
            }
            newMap
        }
    }

    private fun getCaptureStatus(state: String?, key: String): StatusLabel {
        if (state == null) {
            return captureStatusLabelMap[key] ?: StatusLabel("green", "Disconnected")
        }
        return when (state) {
            "connected" -> StatusLabel("green", "Connected")
            "error" -> StatusLabel("red", "Connection Error")
            else -> StatusLabel("green", "Disconnected")
        }
    }

    private fun getRecordStatus(state: String?): StatusLabel {
        return when (state) {
            "recording" -> StatusLabel("green", "Recording")
            "error" -> StatusLabel("red", "Record Error")
            else -> StatusLabel("green", "Not Recording")
        }
    }

    /**
     * Disconnect everything when user logs out
     */
    fun disconnectAll() {
        notifyClient?.disconnect()
        notifyClient = null
        
        reportClients.values.forEach { it.disconnect() }
        reportClients.clear()

        _storageStatusMap.value = emptyMap()
        _cameraStatusMap.value = emptyMap()
        captureStatusLabelMap.clear()
        recordStatusLabelMap.clear()
    }
}
