package com.tenli.aiot.model.network

import com.google.gson.JsonElement

data class NotificationSettingResponse(
    val notification: MasterNotifyInfo,
    val eventTypeGroups: List<EventTypeGroup>,
    val eventTypes: List<EventTypeDetail>,
    val devices: List<Any>
)

data class MasterNotifyInfo(
    val enabled: Boolean
)

data class EventTypeGroup(
    val key: String,
    val notification: NotificationDetailInfo
)

data class EventTypeDetail(
    val key: String,
    val notification: NotificationDetailInfo
)

data class NotificationDetailInfo(
    val uuid: String? = null,
    val enabled: Boolean,
    val alarmMode: AlarmMode? = null,
    val alarmOptions: AlarmOptions? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class AlarmOptions(
    val pauseUtil: Double? = null
)

data class EventTypeGroupDetail(
    val key: String,
    val notification: NotificationDetailInfo,
    val eventTypes: List<EventTypeDetailItem>,
    val features: List<String>
)

data class EventTypeDetailItem(
    val key: String,
    val notification: NotificationDetailInfo
)

data class AlarmMode(
    val default: String? = null,
    val custom: String? = null,
    val inDisturb: String? = null
)


data class Schedule(
    val repeat: String,
    val dayPeriods: List<TimeRange>?,
    val dayOfMonths: List<Int>? = null
)

data class Script(
    val name: String,
    val tag: String,
    val enabled: Boolean,
    val schedule: Schedule,
    val actions: List<Action>,
    val priority: Int
)

data class Action(val filter: JsonElement?, val values: JsonElement)
data class TimeRange(val from: TimePoint, val to: TimePoint)
data class TimePoint(val hour: Int, val minute: Int, val second: Int, val milisecond: Int)

data class AddScriptResponse(val key: String, val script: Script)
data class UpdateNotificationResponse(val key: String, val notification: NotificationDetail)
data class NotificationDetail(val enabled: Boolean, val alarmMode: JsonElement?, val scripts: ScriptGroup)
data class ScriptGroup(val customs: List<Script>)