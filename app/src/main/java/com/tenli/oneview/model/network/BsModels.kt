package com.tenli.oneview.model.network

import androidx.compose.runtime.Immutable
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

// ==================== Plate & Plate Group ====================

@Immutable
data class PlateConfigUi(
    val color: String? = null,
    val background: String? = null,
    val icon: String? = null
)

@Immutable
data class PlateConfig(
    val ui: PlateConfigUi? = null
)

@Immutable
data class PlateGroupConfigUi(
    val background: String? = null,
    val icon: String? = null,
    val color: String? = null
)

@Immutable
data class PlateGroupConfig(
    val ui: PlateGroupConfigUi? = null
)

// ==================== BS Profile Group ====================

@Immutable
data class BsProfileGroupConfigUi(
    val background: String? = null,
    val icon: String? = null,
    val color: String? = null
)

@Immutable
data class BsProfileGroupConfig(
    val ui: BsProfileGroupConfigUi? = null
)

@Immutable
data class BsProfileGroupModel(
    val id: Int? = null,
    val uuid: String? = null,
    val name: String = "",
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

@Immutable
data class BsProfileConfigUi(
    val color: String? = null
)

@Immutable
data class BsProfileConfig(
    val ui: BsProfileConfigUi? = null
)

@Immutable
data class BsProfileModel(
    val id: Int? = null,
    val uuid: String? = null,
    val name: String = "",
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

@Immutable
data class TaskVariablesModel(
    val cameraIds: List<String>? = null
)

@Immutable
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

@Immutable
data class VmsCountOverviewModel(
    val label: String = "",
    val tag: String = "",
    val count: Double = 0.0,
    @SerializedName("service_id", alternate = ["serviceId"])
    val serviceId: String? = null,
    @SerializedName("event_type", alternate = ["eventType"])
    val eventType: String? = null
)

@Immutable
data class VmsEventCountByTypeModel(
    @SerializedName("type", alternate = ["event_type", "eventType"])
    val type: String = "",
    val count: Double = 0.0,
    @SerializedName("min_time")
    val minTime: Double? = null,
    @SerializedName("max_time")
    val maxTime: Double? = null
)

// ==================== Event Config & Notification Settings ====================

data class UserNotificationGlobalSetting(
    var enabled: Boolean? = true,
    @SerializedName("pause_until")
    var pauseUntil: JsonElement? = null
)

data class UserNotificationSettingsModel(
    var global: UserNotificationGlobalSetting? = UserNotificationGlobalSetting(),
    var overrides: JsonElement? = defaultNotificationOverridesJson(),
    @SerializedName("global_scripts")
    var globalScripts: JsonElement? = null
) {
    companion object {
        fun defaultNotificationOverridesJson(): JsonObject = JsonObject().apply {
            add("event_type_groups", JsonObject())
            add("event_types", JsonObject())
        }
    }
}

@Immutable
data class NotificationTokenRegisterModel(
    val token: String,
    val type: String = "firebase",
    val status: Int = 1,
    @SerializedName("client_id")
    val clientId: String = "oneview-android"
)

@Immutable
data class FirebaseConfigModel(
    @SerializedName("apiKey", alternate = ["api_key"])
    val apiKey: String? = null,
    @SerializedName("projectId", alternate = ["project_id"])
    val projectId: String? = null,
    @SerializedName("authDomain", alternate = ["auth_domain"])
    val authDomain: String? = null,
    @SerializedName("messagingSenderId", alternate = ["messaging_sender_id", "senderId", "sender_id"])
    val messagingSenderId: String? = null,
    @SerializedName("appId", alternate = ["app_id"])
    val appId: String? = null,
    @SerializedName("storageBucket", alternate = ["storage_bucket"])
    val storageBucket: String? = null,
    @SerializedName("measurementId", alternate = ["measurement_id"])
    val measurementId: String? = null,
    @SerializedName("vapidKey", alternate = ["vapid_key"])
    val vapidKey: String? = null
)

@Immutable
data class PublicConfigModel(
    val firebase: FirebaseConfigModel? = null
)

// ==================== Uniform Config & Labels ====================

@Immutable
data class UniformLabelUiConfig(
    val color: String? = null
)

@Immutable
data class UniformLabelExtraConfig(
    val ui: UniformLabelUiConfig? = null,
    @SerializedName("gallery_images")
    val galleryImages: List<String>? = null
)
