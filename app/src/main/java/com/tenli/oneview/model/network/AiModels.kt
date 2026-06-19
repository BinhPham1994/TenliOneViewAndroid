package com.tenli.oneview.model.network

// ==================== Monitor ====================

data class Monitor(
    val id: Int,
    val name: String,
    val type: String,
    val cameraId: Int,
    val enabled: Boolean,
    val param: Any? = null
)

data class MonitorEntry(
    val items: List<Monitor>,
    val isLoaded: Boolean
)

data class MonitorType(
    val name: String,
    val type: String
)

data class MonitorTypeEntry(
    val items: List<MonitorType>,
    val isLoaded: Boolean
)

data class MonitorState(
    val id: Int,
    val state: String? = null
)

data class MonitorStateEntry(
    val items: List<MonitorState>,
    val isLoaded: Boolean
)

// ==================== Camera Monitor ====================

data class CameraVMS(
    val vmsId: Int,
    val cameraId: Int
)

data class CameraMonitorExtra(
    val location: Location? = null,
    val extraData: Map<String, Any>? = null
)

data class CameraMonitor(
    val id: Int,
    val name: String,
    val url: String,
    val type: String,
    val uuid: String,
    val extra: CameraMonitorExtra? = null
)

data class CameraMonitorEntry(
    val items: List<CameraMonitor>,
    val isLoaded: Boolean
)

// ==================== Profile Group ====================

data class ProfileGroupEntry(
    val items: List<ProfileGroup>,
    val isLoaded: Boolean
)

data class ProfileGroup(
    val id: Int,
    val name: String,
    val uuid: String,
    val type: String,
    val info: String,
    val parentUUID: String,
    var data: ProfileGroupData?
)

data class ProfileGroupData(
    val color: String
)

// ==================== Profile ====================

data class ProfileEntry(
    val items: List<Profile>,
    val isLoaded: Boolean
)

data class Profile(
    val id: Int,
    val name: String,
    val uuid: String,
    val numFace: Int,
    val lastFaceId: Int,
    val idCode: String? = null,
    val department: String? = null,
    val position: String? = null
)
