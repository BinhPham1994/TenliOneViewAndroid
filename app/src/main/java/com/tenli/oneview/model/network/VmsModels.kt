package com.tenli.oneview.model.network

import androidx.compose.runtime.Immutable

@Immutable
data class VMSServiceModel(
    val id: Int = 0,
    val name: String = "",
    val privateHost: String = "",
    val publicHost: String = "",
    val apiKey: String = "",
    val extra: Map<String, Any>? = null
)

data class VisualMonitoringFeatures(
    val overview: Boolean? = null,
    val monitoring: Boolean? = null,
    val playback: Boolean? = null,
    val map: Boolean? = null,
    val floorPlan: Boolean? = null,
    val config: Boolean? = null,
    val vmsConfig: Boolean? = null,
    val storageConfig: Boolean? = null,
    val dataSharing: Boolean? = null
)

data class AiConfigFeatures(
    val overview: Boolean? = null,
    val management: Boolean? = null,
    val serviceConfig: Boolean? = null
)

data class OperationsFeatures(
    val dashboard: Boolean? = null,
    val smartSearch: Boolean? = null
)

data class SectionPermission<T>(
    val enabled: Boolean? = null,
    val features: T? = null
)

data class UserPermissions(
    val isAdmin: Boolean? = null,
    val allowSettings: Boolean? = null,
    val allowPeripheralDevices: Boolean? = null,
    val allowOnvifControl: Boolean? = null,
    val visualMonitoring: SectionPermission<VisualMonitoringFeatures>? = null,
    val aiConfig: SectionPermission<AiConfigFeatures>? = null,
    val operations: SectionPermission<OperationsFeatures>? = null,
    val allowEditCamera: Boolean? = null,
    val allowConfigAI: Boolean? = null,
    val allowSmartSearch: Boolean? = null,
    val cameras: CameraPermissions? = null,
    val aiEvents: AiEventPermissions? = null,
    val events: AiEventPermissions? = null,
    val modules: List<String>? = null
)

data class CameraPermissions(
    val mode: String? = "all", // 'all' | 'custom'
    val list: List<CameraPermissionItem>? = emptyList()
)

data class CameraPermissionItem(
    val vmsId: Int = 0,
    val cameraId: Int = 0
)

data class AiEventPermissions(
    val mode: String? = "all", // 'all' | 'custom'
    val list: List<String>? = emptyList()
)

data class UserExtra(
    val permissions: UserPermissions? = null,
    val extraData: Map<String, Any>? = null
)

data class VmsUserModel(
    val id: Int = 0,
    val username: String? = null,
    val password: String? = null,
    val name: String? = null,
    val extra: UserExtra? = null
)

data class StorageStatusModel(
    val id: Int = 0,
    val total: Long = 0L,
    val used: Long = 0L,
    val available: Long = 0L
)

@Immutable
data class TimeRange(
    val from: Long = 0L,
    val to: Long = 0L
)

@Immutable
data class RecordSchedule(
    val repeat: List<List<TimeRange>> = emptyList()
)

@Immutable
data class Location(
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Immutable
data class CameraOnvifModel(
    val ip: String = "",
    val port: Any? = null,
    val model: String = "",
    val name: String? = null,
    val mac: String = "",
    val brand: String? = null,
    val authenticated: Boolean? = null,
    val rtspLinks: List<String>? = null,
    val username: String? = null,
    val password: String? = null,
    val channelIndex: Int? = null,
    val isChannel: Boolean? = null,
    val isNVRNode: Boolean? = null,
    val mainRtsp: String? = null,
    val subRtsp: String? = null,
    val hardwareId: String? = null
)

@Immutable
data class CameraGeoConfig(
    val mountType: String? = null, // 'fixed' | 'ptz'
    val mountHeight: Double? = null,
    val azimuth: Double? = null,
    val tilt: Double? = null,
    val hfov: Double? = null,
    val vfov: Double? = null,
    val hfovTele: Double? = null,
    val panMinDeg: Double? = null,
    val panMaxDeg: Double? = null,
    val tiltMinDeg: Double? = null,
    val tiltMaxDeg: Double? = null,
    val tiltOffset: Double? = null
)

@Immutable
data class CameraLatestReport(
    val issues: List<String>? = null
)

@Immutable
data class CameraExtra(
    val location: Location? = null,
    val cameraOnvif: CameraOnvifModel? = null,
    val permanentLink: Boolean? = null,
    val uuid: String? = null,
    val status: String? = null,
    val recordStatus: String? = null,
    val recording: Boolean? = null,
    val geo: CameraGeoConfig? = null,
    val latestReport: CameraLatestReport? = null
)

@Immutable
data class CameraModel(
    val id: Int = 0,
    val name: String = "",
    val main: String = "",
    val sub: String = "",
    val enableRecord: Boolean = false,
    val recordSchedule: RecordSchedule? = null,
    val enableSubRecord: Boolean = false,
    val subRecordSchedule: RecordSchedule? = null,
    val extra: CameraExtra? = null,
    val vmsId: Int = 0,
    val cameraId: Int = 0
)

data class CameraGroupModel(
    val id: Int = 0,
    val parentGroupId: Int = 0,
    val name: String = "",
    val vmsId: Int = 0,
    val extra: Map<String, Any>? = null
)

data class CameraInGroupModel(
    val id: Int = 0,
    val cameraId: Int = 0,
    val groupId: Int = 0
)

data class LiveStreamModel(
    val cameraId: Int = 0,
    val channel: Int = 0,
    val link: String = "",
    val videoCodec: String = "",
    val audioCodec: String = "",
    val videoCodecTag: String = ""
)

@Immutable
data class VideoModel(
    val time: String = "",
    val videoLink: String = "",
    val fallbackLinks: List<String>? = null,
    val imageLink: String = "",
    val thumbnailLink: String = "",
    val isLoaded: Boolean? = null,
    val sourceType: String? = null, // 'vms' | 'nvr'
    val duration: Double? = null,
    val displayTime: String = "",
    val displayDateTime: String = "",
    val epochMillis: Long = 0L
)

data class ReportLinkModel(
    val link: String = ""
)

data class CameraImageLinkModel(
    val link: String = ""
)

// ==================== Auth ====================

data class LogInModel(
    val username: String = "",
    val password: String = ""
)

data class TokenModel(
    val userId: Int = 0,
    val isAdmin: Boolean = false,
    val token: String = ""
)

data class ChangePasswordModel(
    val userId: Int = 0,
    val oldPassword: String = "",
    val newPassword: String = ""
)
