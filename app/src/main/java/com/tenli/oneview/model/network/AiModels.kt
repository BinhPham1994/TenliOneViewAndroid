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

// ==================== Camera Monitor ====================

data class CameraMonitorExtra(
    val location: Location? = null,
    val extraData: Map<String, Any>? = null
)

// ==================== Profile Group ====================

data class ProfileGroup(
    val id: Int,
    val name: String,
    val uuid: String = "",
    val type: String = "",
    val info: String = "",
    val parentUUID: String = "",
    var data: ProfileGroupData? = null
)

data class ProfileGroupData(
    val color: String = "#1677ff"
)

// ==================== Profile ====================

data class Profile(
    val id: Int,
    val name: String,
    val uuid: String,
    val numFace: Int = 0,
    val lastFaceId: Int = 0,
    val idCode: String? = null,
    val department: String? = null,
    val position: String? = null
)
