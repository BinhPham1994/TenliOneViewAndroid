package com.tenli.oneview.model.network

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName

@Immutable
data class VMSServiceModel(
    val id: Int,
    val name: String,
    val privateHost: String,
    val publicHost: String,
    val apiKey: String,
    val extra: Map<String, Any>? = null
)

data class AuthTokenModel(
    val id: Int,
    val name: String,
    val token: String,
    val userId: Int,
    val isAdmin: Boolean
)

data class AuthTokenRequest(
    val name: String = "Auth Token",
    val username: String,
    val password: String
)

data class UserGroupModel(
    val id: Int,
    val name: String
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

data class UserModel(
    val id: Int,
    val username: String,
    val password: String? = null,
    val name: String,
    val extra: UserExtra? = null
)

data class UserInGroupModel(
    val id: Int,
    val userId: Int,
    val groupId: Int
)

data class SystemSettingModel(
    val settings: Map<String, Any>? = null
)

data class StorageModel(
    val id: Int,
    val name: String,
    val path: String,
    val limitSize: Long,
    val level: Int,
    val extra: Map<String, Any>? = null,
    val vmsId: Int,
    val storageId: Int
)

data class StorageStatusModel(
    val id: Int,
    val total: Long,
    val used: Long,
    val available: Long
)

data class TimeRange(
    val from: Long,
    val to: Long
)

data class RecordSchedule(
    val repeat: List<List<TimeRange>>
)

data class Location(
    val latitude: Double? = null,
    val longitude: Double? = null
)

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

data class RecordStreamRequest(
    val id: String? = null,
    val rtsp: String? = null
)

data class RecordStreamModel(
    val id: String,
    val rtsp: String? = null,
    val state: String = "connecting", // 'connecting' | 'connected' | 'error'
    val link: String? = null,
    val fallbackLinks: List<String>? = null,
    val videoCodecTag: String? = null
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

data class DeviceDiscoveryModel(
    val xml: String
)

data class DeviceServiceRequest(
    @SerializedName("addr", alternate = ["url"])
    val addr: String,
    val username: String = "",
    val password: String = "",
    val xml: String
) {
    val url: String get() = addr
}

data class DeviceServiceResponse(
    val xml: String? = null,
    val status: Int? = null
)

data class PermanentLinkModel(
    val id: Int,
    val name: String,
    val cameraId: Int,
    val path: String,
    val localLink: String,
    val privateLink: String,
    val publicLink: String,
    val vmsId: Int,
    val permanentLinkId: Int
)

data class CameraGroupAccessModel(
    val id: Int,
    val userGroupId: Int,
    val cameraGroupId: Int
)

data class CameraServiceModel(
    val id: Int,
    val apiKey: String,
    val publicHost: String
)

data class CameraViewModel(
    val id: Int,
    val name: String,
    val path: String,
    val userId: Int,
    val data: CameraViewData
)

data class CameraViewData(
    val liveCameraNodeIds: List<CellData>,
    val gridSize: Int,
    val services: List<CameraServiceModel>
)

data class ReportLinkModel(
    val link: String
)

data class ReportLinkEntry(
    val item: ReportLinkModel,
    val isLoaded: Boolean
)

data class VideoLinkModel(
    val link: String
)

data class CameraImageLinkModel(
    val link: String
)

data class MergeInfo(
    val startIndex: Int,
    val indexes: List<Int>
)

data class CellData(
    val cameraId: String? = null,
    val merge: MergeInfo? = null,
    val isBlinking: Boolean? = null
)

// ==================== Auth ====================

data class LogInModel(
    val username: String,
    val password: String
)

data class LogOutModel(
    val token: String
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

data class ResetPasswordModel(
    val userId: Int,
    val password: String
)
