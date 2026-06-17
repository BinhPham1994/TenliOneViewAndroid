package com.tenli.aiot.model.network

import com.google.gson.annotations.SerializedName

data class DeviceItem(
    val id: Int = 0,
    val name: String = "",
    val uuid: String = "",
    val deviceCode: String = "",
    val key: String = "",
    val publicTargetURI: String = "",
    val status: Int = 0,
    val connectState: Int = 0,
    val userGroupId: Int = 0,

    val postURL: String? = null,
    val postHeaders: List<PostHeader>? = null,

    val info: DevInfo? = null,
    val extendedData: ExtendedData? = null,

    val mqttInfo: MqttInfo? = null,
    val roles: List<RoleDevice>? = null,
    var checked: Boolean = false
) {
    val isOnline: Boolean get() = info?.isOnline ?: false
    val cameraCount: Int get() = info?.cameraInfo?.size ?: 0
    val iotCount: Int get() = extendedData?.iots?.size ?: 0
}

data class PostHeader(
    val key: String = "",
    val value: String = ""
)

data class DevInfo(
    val isOnline: Boolean = false,
    val cameraInfo: List<CameraInfo> = emptyList()
)

data class CameraInfo(
    val id: Int,
    val name: String,
    val type: String,
    val state: String,
    val snapshot: SnapshotInfo? = null,
    val url: String? = null
)

data class SnapshotInfo(
    val uuid: String,
    val publicURL: String
)

data class ExtendedData(
    val iots: List<IotItem>? = null
)

data class IotItem(
    val displayName: String = "",
    val ieeeAddress: String = "",
    val exposes: Map<String, String> = emptyMap()
)

data class MqttInfo(
    val enabled: Boolean,
    val uri: String,
    val privateUri: String? = null, // Thêm để đầy đủ theo JSON
    val localPort: Int? = null,      // Thêm để đầy đủ theo JSON
    val auth: MqttAuth? = null,
    val bridge: MqttBridge? = null
)

data class MqttBridge(
    val topics: MqttTopics? = null
)

data class MqttTopics(
    val root: String = "",
    @SerializedName("in") val inTopic: String = "", // Dùng SerializedName vì "in" là từ khóa cấm trong Kotlin
    @SerializedName("out") val outTopic: String = "",
    val ins: List<String> = emptyList()
)

data class MqttAuth(
    val userName: MqttUserName? = null, // Thêm để lấy "application" hoặc "device"
    val password: String = "",
    val clientId: String = ""           // Thêm để lấy template clientId
)

data class MqttUserName(
    val application: String = "",
    val device: String = ""
)

data class RoleDevice(
    val role: String = "",
    val isCurrent: Boolean = false
)