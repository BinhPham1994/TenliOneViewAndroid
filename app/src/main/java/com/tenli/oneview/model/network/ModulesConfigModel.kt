package com.tenli.oneview.model.network

import com.google.gson.annotations.SerializedName

/**
 * Cấu hình phân quyền bài AI cấp dự án được sinh tự động khi build trên Web PC:
 * `GET https://<domain-backend>/modules-config.json`
 */
data class ModulesConfigModel(
    @SerializedName("buildTime")
    val buildTime: String? = null,
    @SerializedName("enabledAiModules")
    val enabledAiModules: List<String>? = null,
    @SerializedName("disabledAiModules")
    val disabledAiModules: List<String>? = null,
    @SerializedName("enabledSensorTypes")
    val enabledSensorTypes: List<String>? = null,
    @SerializedName("aiModules")
    val aiModules: Map<String, AiModuleConfigItem>? = null,
    @SerializedName("moduleNameMap")
    val moduleNameMap: Map<String, String>? = null,
    @SerializedName("tabs")
    val tabs: ModulesTabsConfig? = null
) {
    /**
     * Xác thực dữ liệu trả về thực sự là cấu hình `modules-config.json` hợp lệ
     * (tránh trường hợp backend cũ trả về JSON rỗng `{}` hoặc object khác).
     */
    fun isValidConfig(): Boolean {
        return enabledAiModules != null ||
            !aiModules.isNullOrEmpty() ||
            !tabs?.business?.modules.isNullOrEmpty()
    }
}

data class AiModuleConfigItem(
    @SerializedName("enabled")
    val enabled: Boolean? = null,
    @SerializedName("enableStranger")
    val enableStranger: Boolean? = null,
    @SerializedName("color")
    val color: String? = null,
    @SerializedName("icon")
    val icon: String? = null,
    @SerializedName("sensorType")
    val sensorType: String? = null,
    @SerializedName("eventTypes")
    val eventTypes: List<String>? = null,
    @SerializedName("features")
    val features: Map<String, Boolean>? = null
)

data class ModulesTabsConfig(
    @SerializedName("camera")
    val camera: ModuleTabGroupConfig? = null,
    @SerializedName("ai")
    val ai: ModuleTabGroupConfig? = null,
    @SerializedName("business")
    val business: ModuleTabGroupConfig? = null
)

data class ModuleTabGroupConfig(
    @SerializedName("enabled")
    val enabled: Boolean? = null,
    @SerializedName("label")
    val label: String? = null,
    @SerializedName("icon")
    val icon: String? = null,
    @SerializedName("modules")
    val modules: Map<String, AiModuleConfigItem>? = null
)

