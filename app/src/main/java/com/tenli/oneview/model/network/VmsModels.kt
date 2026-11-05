package com.tenli.oneview.model.network

import androidx.compose.runtime.Immutable

@Immutable
data class VMSServiceModel(
    val id: Int,
    val name: String,
    val privateHost: String,
    val publicHost: String,
    val apiKey: String,
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
    val mode: String, // 'all' | 'custom'
    val list: List<CameraPermissionItem>
)

data class CameraPermissionItem(
    val vmsId: Int,
    val cameraId: Int
)

data class AiEventPermissions(
    val mode: String, // 'all' | 'custom'
    val list: List<String>
)

data class UserExtra(
    val permissions: UserPermissions? = null,
    val extraData: Map<String, Any>? = null
)

data class StorageStatusModel(
    val id: Int,
    val total: Long,
    val used: Long,
    val available: Long
)

@Immutable
data class TimeRange(
    val from: Long,
    val to: Long
)

@Immutable
data class RecordSchedule(
    val repeat: List<List<TimeRange>>
)

@Immutable
data class Location(
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Immutable
data class CameraOnvifModel(
    val ip: String,
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
    val id: Int,
    val name: String,
    val main: String,
    val sub: String,
    val enableRecord: Boolean,
    val recordSchedule: RecordSchedule? = null,
    val extra: CameraExtra? = null,
    val vmsId: Int,
    val cameraId: Int
)

data class CameraGroupModel(
    val id: Int,
    val parentGroupId: Int,
    val name: String,
    val vmsId: Int,
    val extra: Map<String, Any>? = null
)

data class CameraInGroupModel(
    val id: Int,
    val cameraId: Int,
    val groupId: Int
)

data class LiveStreamModel(
    val cameraId: Int,
    val channel: Int,
    val link: String = "",
    val videoCodec: String = "",
    val audioCodec: String = "",
    val videoCodecTag: String = ""
)

data class VideoModel(
    val time: String,
    val videoLink: String,
    val fallbackLinks: List<String>? = null,
    val imageLink: String = "",
    val thumbnailLink: String = "",
    val isLoaded: Boolean? = null,
    val sourceType: String? = null, // 'vms' | 'nvr'
    val duration: Double? = null
)

data class ReportLinkModel(
    val link: String
)

data class CameraImageLinkModel(
    val link: String
)

// ==================== Auth ====================

data class LogInModel(
    val username: String,
    val password: String
)

data class TokenModel(
    val userId: Int,
    val isAdmin: Boolean,
    val token: String
)

data class ChangePasswordModel(
    val userId: Int,
    val oldPassword: String,
    val newPassword: String
)
