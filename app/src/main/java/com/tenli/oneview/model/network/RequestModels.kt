package com.tenli.oneview.model.network

import androidx.annotation.Keep

@Keep
data class EditGroupRequest(
    val name: String
)

@Keep
data class EditDeviceRequest(
    val name: String,
    val groupId: Int
)

@Keep
data class UpdateAiStatusRequest(
    val aiStatus: Boolean
)

@Keep
data class DeviceCommandRequest(
    val command: String,
    val duration: Long? = null
)

@Keep
data class ConfigHttpKeyRequest(
    val httpKey: String
)

@Keep
data class UpdateNotifySettingRequest(
    val notification: NotifyEnabledConfig
)

@Keep
data class NotifyEnabledConfig(
    val enabled: Boolean
)

@Keep
data class UpdateNotifyGroupRequest(
    val key: String,
    val notification: NotifyConfigPayload
)

@Keep
data class NotifyConfigPayload(
    val enabled: Boolean,
    val alarmMode: AlarmModePayload? = null
)

@Keep
data class AlarmModePayload(
    val default: String
)

@Keep
data class ScriptJsonRequest(
    val name: String,
    val enabled: Boolean,
    val monitorIds: List<Int>? = null,
    val resetModeAfterTime: Int,
    val time: ScriptTimeConfig,
    val actions: ScriptActionsConfig
)

@Keep
data class ScriptTimeConfig(
    val repeat: String,
    val dayOfWeeks: List<Int>,
    val activate: ScriptTimeDetails,
    val deactivate: ScriptTimeDetails
)

@Keep
data class ScriptTimeDetails(
    val hour: Int,
    val minute: Int,
    val second: Int
)

@Keep
data class ScriptActionsConfig(
    val activate: List<ScriptActionItem>,
    val deactivate: List<ScriptActionItem>
)

@Keep
data class ScriptActionItem(
    val type: String,
    val enabled: Boolean,
    val monitorIds: List<Int>,
    val delay: Int,
    val data: Map<String, Any> = emptyMap(),
    val options: Map<String, Any> = emptyMap()
)

@Keep
data class EventUpdateRequest(
    val name: String,
    val note: String,
    val type: String
)

@Keep
data class MonitorCommandRequest(
    val command: String,
    val enable: Boolean
)

@Keep
data class AddCameraRequest(
    val id: Int,
    val name: String,
    val url: String,
    val type: String,
    val uuid: String,
    val extra: Any?
)

@Keep
data class AddMonitorRequest(
    val id: Int,
    val name: String,
    val enabled: Boolean,
    val cameraId: Int,
    val type: String,
    val param: Map<String, Any>
)
