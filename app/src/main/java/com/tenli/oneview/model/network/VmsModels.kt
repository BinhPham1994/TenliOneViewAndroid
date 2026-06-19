package com.tenli.oneview.model.network

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

data class UserPermissions(
    val isAdmin: Boolean? = null,
    val allowSettings: Boolean? = null,
    val allowEditCamera: Boolean? = null,
    val allowConfigAI: Boolean? = null,
    val cameras: CameraPermissions? = null,
    val aiEvents: AiEventPermissions? = null,
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
    val model: String,
    val mac: String,
    val brand: String? = null,
    val username: String? = null,
    val password: String? = null
)

data class CameraExtra(
    val location: Location? = null,
    val cameraOnvif: CameraOnvifModel? = null,
    val permanentLink: Boolean? = null,
    val uuid: String? = null
)

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
    val link: String,
    val videoCodec: String,
    val audioCodec: String,
    val videoCodecTag: String
)

data class VideoModel(
    val time: String,
    val videoLink: String,
    val imageLink: String,
    val thumbnailLink: String,
    val isLoaded: Boolean? = null
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
