package com.tenli.oneview.model.network

import com.google.gson.annotations.SerializedName

// ==================== Plate ====================

data class ExtraInfo(
    @SerializedName("unit_price")
    val unitPrice: Double = 0.0
)

data class PlateModel(
    val id: Int,
    @SerializedName("plate_number")
    val plateNumber: String,
    @SerializedName("vehicle_type")
    val vehicleType: String,
    @SerializedName("internal_id")
    val internalId: String,
    @SerializedName("vehicle_class")
    val vehicleClass: String,
    @SerializedName("guest_type")
    val guestType: String,
    @SerializedName("extra_info")
    val extraInfo: ExtraInfo? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    val tags: Any? = null,            // String | List<String> | null
    @SerializedName("group_name")
    val groupName: String? = null
)

// ==================== Contact ====================

data class ContractModel(
    val id: Int,
    val uuid: String,
    val name: String,
    val email: String? = null,
    val phone: String? = null,
    @SerializedName("other_info")
    val otherInfo: Any? = null,
    val tags: Any? = null,            // String | List<String> | null
    @SerializedName("group_name")
    val groupName: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
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
    val groupIds: List<Int>? = null
)

// ==================== Image Profile ====================

data class ImageProfileModel(
    val id: Int,
    val uuid: String,
    @SerializedName("profile_id")
    val profileId: Int,
    @SerializedName("face_image")
    val faceImage: String,
    @SerializedName("feature_vector")
    val featureVector: List<Double>? = null,
    @SerializedName("feature_model")
    val featureModel: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null
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

data class TaskStatus(
    @SerializedName("last_status")
    val lastStatus: String? = null,
    val error: String? = null
)

data class TaskModel(
    val id: Int,
    val name: String,
    val type: String,
    @SerializedName("cron_expression")
    val cronExpression: String,
    var config: TaskConfigModel? = null,
    @SerializedName("is_active")
    val isActive: Int,
    @SerializedName("last_run_at")
    val lastRunAt: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    val status: TaskStatus? = null,
    @SerializedName("is_logging_enabled")
    val isLoggingEnabled: Int? = null,
    @SerializedName("paused_until")
    val pausedUntil: String? = null,
    @SerializedName("start_at")
    val startAt: String? = null,
    @SerializedName("end_at")
    val endAt: String? = null,
    @SerializedName("excluded_dates")
    val excludedDates: Any? = null,     // List | String | null
    @SerializedName("specific_dates")
    val specificDates: Any? = null,     // List | String | null
    @SerializedName("datasource_id")
    val datasourceId: Int? = null,
    @SerializedName("template_id")
    val templateId: Int? = null
)

// ==================== Vehicle Statistics ====================

data class VehicleStatisticalOverTimeModel(
    val time: Double,
    val date: String,
    val hour: Int,
    val value: String
)

data class VehicleQuickCountModel(
    val label: String,
    val count: Int
)

data class UniformCountByLabelModel(
    val label: String,
    val count: Int
)

data class VehicleCountByPrefixModel(
    @SerializedName("plate_prefix")
    val platePrefix: String,
    val count: Int
)

// ==================== VMS Event Statistics ====================

data class VmsEventStatisticalOverTimeModel(
    val time: Double,
    val date: String,
    val hour: Int,
    val value: String
)

data class VmsCountOverviewModel(
    val label: String,
    val tag: String,
    val count: Any,                     // String hoặc Int
    @SerializedName("service_id")
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

data class VmsEventCountByCameraModel(
    val cameraUUID: String,
    val count: Int
)

// ==================== File ====================

data class FileModel(
    val id: Int,
    val uuid: String,
    @SerializedName("storage_id")
    val storageId: Int,
    @SerializedName("relative_path")
    val relativePath: String,
    val filename: String,
    val mimetype: String,
    val size: Long,
    val collection: String,
    @SerializedName("access_scope")
    val accessScope: Int,
    @SerializedName("owner_type")
    val ownerType: String,
    @SerializedName("owner_id")
    val ownerId: Int? = null,
    val status: Int,
    @SerializedName("expires_at")
    val expiresAt: String? = null,
    val metadata: String? = null,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("link_salt")
    val linkSalt: String
)

data class FileLinks(
    val direct: String,
    val temporary: String,
    val token: String
)

data class FileUploadResponse(
    val message: String,
    val file: FileModel,
    val links: FileLinks
)

// ==================== Vehicle Session ====================

data class VehicleSessionInfo(
    @SerializedName("internal_id")
    val internalId: Int? = null,
    @SerializedName("vehicle_class")
    val vehicleClass: String? = null,
    @SerializedName("guest_type")
    val guestType: String? = null,
    @SerializedName("unit_price")
    val unitPrice: Double? = null,
    val name: String? = null
)

data class VehicleSessionCamera(
    val id: Int,
    val uuid: String,
    val name: String
)

data class VehicleSessionEvent(
    val id: Int,
    @SerializedName("service_id")
    val serviceId: Int,
    val epoch: Long,
    val time: String,
    val camera: VehicleSessionCamera
)

// ==================== Food Traceability ====================

data class FoodTraceability(
    val sourceUrl: String,
    val extractedUrl: String,
    val foodName: String? = null,
    val supplier: String? = null,
    val expiryDate: String? = null,
    val scannedAt: String,
    val organization: String,
    val foodImage: String? = null,
    val certificate: String? = null,
    val synced: Boolean? = null
)

data class FoodModel(
    val id: Int,
    val userId: Int,
    val extra: FoodTraceability
)
