package com.tenli.oneview.model.network

import com.google.gson.annotations.SerializedName

// ==================== Plate & Plate Group ====================

data class PlateConfigUi(
    val color: String? = null,
    val background: String? = null,
    val icon: String? = null
)

data class PlateConfig(
    val ui: PlateConfigUi? = null
)

data class PlateGroupConfigUi(
    val background: String? = null,
    val icon: String? = null,
    val color: String? = null
)

data class PlateGroupConfig(
    val ui: PlateGroupConfigUi? = null
)

// ==================== BS Profile Group ====================

data class BsProfileGroupConfigUi(
    val background: String? = null,
    val icon: String? = null,
    val color: String? = null
)

data class BsProfileGroupConfig(
    val ui: BsProfileGroupConfigUi? = null
)

data class BsProfileGroupModel(
    val id: Int? = null,
    val uuid: String? = null,
    val name: String,
    val description: String? = null,
    val info: Any? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    val config: BsProfileGroupConfig? = null,
    @SerializedName("parent_uuid")
    val parentUuid: String? = null,
    val parentUUID: String? = null
)

// ==================== BS Profile ====================

data class BsProfileConfigUi(
    val color: String? = null
)

data class BsProfileConfig(
    val ui: BsProfileConfigUi? = null
)

data class BsProfileModel(
    val id: Int? = null,
    val uuid: String? = null,
    val name: String,
    val idCode: String? = null,
    @SerializedName("id_code")
    val idCodeSnake: String? = null,
    val description: String? = null,
    val avatar: String? = null,
    @SerializedName("other_info")
    val otherInfo: Any? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    val config: BsProfileConfig? = null,
    val groups: List<BsProfileGroupModel>? = null,
    @SerializedName("group_ids")
    val groupIds: List<Int>? = null,
    val groupColor: String? = null,
    @SerializedName("group_color")
    val groupColorSnake: String? = null,
    val groupName: String? = null,
    @SerializedName("group_name")
    val groupNameSnake: String? = null
)

// ==================== Task ====================

data class TaskVariablesModel(
    val cameraIds: List<String>? = null
)

data class TaskConfigModel(
    val timezone: String? = null,
    val locale: String? = null,
    val templateName: String? = null,
    val emailTemplateName: String? = null,
    val subject: String? = null,
    val email: List<String>? = null,
    val variables: TaskVariablesModel? = null
)

// ==================== VMS Event Statistics ====================

data class VmsCountOverviewModel(
    val label: String,
    val tag: String,
    val count: Double,
    @SerializedName("service_id", alternate = ["serviceId"])
    val serviceId: String? = null
)

data class VmsEventCountByTypeModel(
    val type: String,
    val count: Int,
    @SerializedName("min_time")
    val minTime: Double? = null,
    @SerializedName("max_time")
    val maxTime: Double? = null
)

// ==================== Event Config & Notification Settings ====================

data class UserNotificationGlobalSetting(
    var enabled: Boolean = true,
    @SerializedName("pause_until")
    var pauseUntil: Any? = null
)

data class UserNotificationAlarmMode(
    var default: String? = "push"
)

data class UserNotificationDetailSetting(
    var enabled: Boolean? = true,
    var alarmMode: UserNotificationAlarmMode? = null
)

data class UserNotificationItemSetting(
    var notification: UserNotificationDetailSetting? = null
)

data class UserNotificationOverrides(
    @SerializedName("event_type_groups")
    var eventTypeGroups: MutableMap<String, UserNotificationItemSetting>? = mutableMapOf(),
    @SerializedName("event_types")
    var eventTypes: MutableMap<String, UserNotificationItemSetting>? = mutableMapOf()
)

data class UserNotificationSettingsModel(
    var global: UserNotificationGlobalSetting = UserNotificationGlobalSetting(),
    var overrides: UserNotificationOverrides = UserNotificationOverrides(),
    @SerializedName("global_scripts")
    var globalScripts: List<Any>? = null
)

// ==================== Uniform Config & Labels ====================

data class UniformLabelUiConfig(
    val color: String? = null
)

data class UniformLabelExtraConfig(
    val ui: UniformLabelUiConfig? = null,
    @SerializedName("gallery_images")
    val galleryImages: List<String>? = null
)
