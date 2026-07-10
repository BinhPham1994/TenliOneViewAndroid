package com.tenli.oneview.model.network

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

// ==================== WebSocket Notify ====================

data class NotifyData(
    val event: String? = null,
    val channel: String? = null,
    val message: String? = null,
    val data: JsonElement? = null
) {
    val topic: String?
        get() = event ?: channel
}

// ==================== WebSocket Report ====================

data class StreamState(
    val state: String? = null
)

data class RecordState(
    val state: String? = null,
    @SerializedName("schedule-state")
    val scheduleState: String? = null
)

data class CameraInfo(
    val id: Int,
    val main: StreamState? = null,
    val sub: StreamState? = null,
    val recorder: RecordState? = null
)

data class ReportPayload(
    val system: JsonElement? = null,
    val storage: List<StorageStatusModel>? = null,
    val camera: List<CameraInfo>? = null
)
