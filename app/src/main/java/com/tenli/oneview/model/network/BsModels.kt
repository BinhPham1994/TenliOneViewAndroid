package com.tenli.oneview.model.network

import com.google.gson.annotations.SerializedName

// ==================== Plate & Plate Group ====================

data class ExtraInfo(
    @SerializedName("unit_price")
    val unitPrice: Double = 0.0
)

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

data class PlateGroupModel(
    val id: Int? = null,
    val uuid: String? = null,
    val name: String,
    val description: String? = null,
    val info: Map<String, Any>? = null,
    val config: PlateGroupConfig? = null,
    @SerializedName("parent_uuid")
    val parentUuid: String? = null,
    val parentUUID: String? = null,
    val type: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    @SerializedName("plate_count")
    val plateCount: Int? = null,
    @SerializedName("plate_count_recursive")
    val plateCountRecursive: Int? = null
)

data class PlateModel(
    val id: Int? = null,
    @SerializedName("plate_number")
    val plateNumber: String,
    @SerializedName("vehicle_type")
    val vehicleType: String? = null,
    @SerializedName("internal_id")
    val internalId: String? = null,
    @SerializedName("vehicle_class")
    val vehicleClass: String? = null,
    @SerializedName("guest_type")
    val guestType: String? = null,
    @SerializedName("extra_info")
    val extraInfo: ExtraInfo? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    val tags: Any? = null,            // String | List<String> | null
    @SerializedName("group_name")
    val groupName: String? = null,
    val config: PlateConfig? = null,
    val groups: List<PlateGroupModel>? = null,
    @SerializedName("group_ids")
    val groupIds: List<Int>? = null,
    @SerializedName("group_uuids")
    val groupUuids: List<String>? = null,
    @SerializedName("plate_unit_price")
    val plateUnitPrice: Double? = null
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

// ==================== VMS Event Count By Camera & Type ====================

data class VmsEventCountByCameraAndTypeModel(
    val eventType: String,
    val cameraUUID: String,
    val count: Int
)

// ==================== Attendance & Shift & Holiday ====================

data class AttendanceAssignmentModel(
    val id: Int? = null,
    @SerializedName("target_type")
    val targetType: String,
    @SerializedName("target_group_uuid")
    val targetGroupUuid: String? = null,
    @SerializedName("target_profile_uuid")
    val targetProfileUuid: String? = null,
    @SerializedName("shift_uuid")
    val shiftUuid: String,
    @SerializedName("valid_from")
    val validFrom: String? = null,
    @SerializedName("valid_to")
    val validTo: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    @SerializedName("shift_name")
    val shiftName: String? = null,
    @SerializedName("shift_code")
    val shiftCode: String? = null,
    @SerializedName("shift_id")
    val shiftId: Int? = null
)

data class ShiftModel(
    val id: Int? = null,
    val uuid: String? = null,
    val code: String,
    val name: String,
    val description: String? = null,
    @SerializedName("start_time")
    val startTime: String,
    @SerializedName("end_time")
    val endTime: String,
    @SerializedName("start_work")
    val startWork: String,
    @SerializedName("end_work")
    val endWork: String,
    @SerializedName("day_of_week_from")
    val dayOfWeekFrom: Int? = null,
    @SerializedName("day_of_week_to")
    val dayOfWeekTo: Int? = null,
    @SerializedName("week_modulus")
    val weekModulus: Int? = null,
    @SerializedName("week_remainder")
    val weekRemainder: Int? = null,
    @SerializedName("check_mode")
    val checkMode: String? = null,
    @SerializedName("valid_from")
    val validFrom: String? = null,
    @SerializedName("valid_to")
    val validTo: String? = null,
    @SerializedName("extra_config")
    val extraConfig: Any? = null,
    @SerializedName("days_of_week")
    val daysOfWeek: String? = null,
    @SerializedName("days_mask")
    val daysMask: Int? = null,
    @SerializedName("specific_days")
    val specificDays: List<Int>? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class HolidayModel(
    val id: Int? = null,
    val uuid: String? = null,
    val name: String,
    @SerializedName("holiday_date")
    val holidayDate: String,
    @SerializedName("is_yearly_recurring")
    val isYearlyRecurring: Any? = null,
    val description: String? = null,
    @SerializedName("extra_info")
    val extraInfo: Any? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class FaceRecognitionCountByTimeModel(
    val time: Double,
    val date: String,
    val hour: Int,
    val evalue: Int,
    val pvalue: Int
)

data class AttendanceDataModel(
    val id: Int,
    @SerializedName("service_id")
    val serviceId: Int,
    @SerializedName("origin_id")
    val originId: Int,
    @SerializedName("camera_uuid")
    val cameraUuid: String,
    val image: String,
    val video: String,
    @SerializedName("profile_name")
    val profileName: String,
    @SerializedName("profile_uuid")
    val profileUuid: String,
    @SerializedName("face_crop_image")
    val faceCropImage: String? = null,
    @SerializedName("container_id")
    val containerId: String? = null,
    val time: Double,
    @SerializedName("shift_date")
    val shiftDate: String,
    @SerializedName("time_in_day")
    val timeInDay: String,
    @SerializedName("day_of_week")
    val dayOfWeek: Int,
    @SerializedName("week_of_year")
    val weekOfYear: Int,
    val tz: String,
    @SerializedName("shift_code")
    val shiftCode: String,
    @SerializedName("shift_start_time_work")
    val shiftStartTimeWork: String,
    @SerializedName("shift_end_time_work")
    val shiftEndTimeWork: String,
    @SerializedName("arrival_time")
    val arrivalTime: Double,
    @SerializedName("departure_time")
    val departureTime: Double,
    @SerializedName("max_start_time")
    val maxStartTime: Double,
    @SerializedName("min_end_time")
    val minEndTime: Double,
    @SerializedName("is_arrival")
    val isArrival: Int,
    @SerializedName("is_departure")
    val isDeparture: Int,
    @SerializedName("arrival_late_time")
    val arrivalLateTime: Double,
    @SerializedName("departure_early_time")
    val departureEarlyTime: Double
)

data class EventProfileGroupCountRecursive(
    val count: Int,
    @SerializedName("min_time")
    val minTime: Double? = null,
    @SerializedName("max_time")
    val maxTime: Double? = null
)

data class EventProfileGroupCountModel(
    @SerializedName("group_id")
    val groupId: Int,
    @SerializedName("group_uuid")
    val groupUuid: String,
    @SerializedName("group_name")
    val groupName: String,
    @SerializedName("group_parent_uuid")
    val groupParentUuid: String? = null,
    val count: Int,
    @SerializedName("min_time")
    val minTime: Double? = null,
    @SerializedName("max_time")
    val maxTime: Double? = null,
    val recursive: EventProfileGroupCountRecursive? = null
)

data class ProfileShiftConfigModel(
    val profileUUID: String,
    val shiftCode: String
)

// ==================== Event Config & Notification Settings ====================

data class EventConfigNotification(
    val enabled: Boolean? = null
)

data class EventConfigGroupModel(
    val key: String,
    val source: String? = null,
    val features: List<String>? = null,
    val notification: EventConfigNotification? = null
)

data class EventConfigDevice(
    val models: List<String>? = null,
    val syncTypes: List<String>? = null
)

data class EventConfigDisplay(
    @SerializedName("lang_key")
    val langKey: String? = null,
    val values: List<Any>? = null
)

data class EventConfigContentFormat(
    @SerializedName("lang_key_format")
    val langKeyFormat: String? = null
)

data class EventConfigContent(
    val subject: EventConfigContentFormat? = null,
    val body: EventConfigContentFormat? = null
)

data class EventConfigNotificationDetail(
    val enabled: Boolean? = null,
    val alarmMode: Map<String, Any>? = null,
    val reduce: Map<String, Any>? = null,
    val reCheck: Map<String, Any>? = null
)

data class EventConfigMedia(
    val downloadVideo: Boolean? = null,
    val downloadImage: Boolean? = null,
    val ignoreDownload: Boolean? = null,
    val includeSensor: Boolean? = null,
    val multi: Boolean? = null,
    val cropImageFields: List<String>? = null,
    val priorityCrop: Boolean? = null
)

data class EventConfigValueValidate(
    val type: String? = null,
    val emptyAble: Boolean? = null,
    val nullAble: Boolean? = null
)

data class EventConfigValueMapping(
    val from: String? = null,
    val to: String? = null,
    val validate: EventConfigValueValidate? = null,
    val defaultValue: Any? = null
)

data class EventConfigValues(
    val mappings: List<EventConfigValueMapping>? = null
)

data class EventConfigModel(
    val key: String,
    val target: String? = null,
    val device: Map<String, Any>? = null,
    val display: Map<String, Any>? = null,
    val content: Map<String, Any>? = null,
    val notification: Map<String, Any>? = null,
    val level: Int? = null,
    val media: Map<String, Any>? = null,
    val data: Any? = null,
    val confirmAble: Boolean? = null,
    val reportMode: String? = null,
    val viewMode: String? = null,
    val values: Map<String, Any>? = null,
    val eventTypeGroup: String? = null,
    val source: String? = null
)

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

data class UserNotificationScriptTimePoint(
    val hour: Int = 0,
    val minute: Int = 0
)

data class UserNotificationScriptTimePeriod(
    val from: UserNotificationScriptTimePoint? = null,
    val to: UserNotificationScriptTimePoint? = null
)

data class UserNotificationScriptSchedule(
    val repeat: String? = null,
    val dayOfWeeks: List<Int>? = null,
    val dayPeriods: List<UserNotificationScriptTimePeriod>? = null
)

data class UserNotificationScriptFilter(
    @SerializedName("event_types")
    val eventTypes: List<String>? = null,
    @SerializedName("camera_ids")
    val cameraIds: List<Any>? = null,
    val where: Any? = null
)

data class UserNotificationScriptAction(
    val type: String? = null,
    val values: List<String>? = null,
    val channels: List<String>? = null
)

data class UserNotificationScriptModel(
    val id: String,
    val name: String,
    val enabled: Boolean? = true,
    val priority: Int? = null,
    @SerializedName("unmatched_behavior")
    val unmatchedBehavior: String? = null,
    @SerializedName("flow_id")
    val flowId: String? = null,
    val filter: UserNotificationScriptFilter? = null,
    val schedule: UserNotificationScriptSchedule? = null,
    val actions: List<UserNotificationScriptAction>? = null,
    val createdAt: String? = null
)

data class UserNotificationSettingsModel(
    var global: UserNotificationGlobalSetting = UserNotificationGlobalSetting(),
    var overrides: UserNotificationOverrides = UserNotificationOverrides(),
    @SerializedName("global_scripts")
    var globalScripts: List<Any>? = null
)

data class FirebaseConfigModel(
    val apiKey: String? = null,
    val projectId: String? = null,
    val authDomain: String? = null,
    val messagingSenderId: String? = null,
    val appId: String? = null,
    val storageBucket: String? = null,
    val measurementId: String? = null,
    val vapidKey: String? = null
)

data class PublicConfigModel(
    val timezone: String? = null,
    val locale: String? = null,
    val organizationName: String? = null,
    val organizationCode: String? = null,
    val basePath: String? = null,
    val publicUrl: String? = null,
    val apiVersion: String? = null,
    val buildVersion: String? = null,
    val serverBuildTime: String? = null,
    val commitHash: String? = null,
    val buildDescription: String? = null,
    val redirectShortPath: String? = null,
    val firebase: FirebaseConfigModel? = null
)

data class NotificationTokenRegisterModel(
    val token: String,
    val type: String = "android",
    val status: Int? = 1,
    @SerializedName("client_id")
    val clientId: String? = null
)

data class OccupationItem(
    val key: String,
    val display: String
)

data class ProfileOccupationsConfig(
    val default: List<OccupationItem>? = null,
    val custom: List<OccupationItem>? = null
)

data class ProfileOneviewConfig(
    val occupations: ProfileOccupationsConfig? = null
)

data class AttendanceOneviewConfig(
    val minDepartureFromArrivalTimeInSeconds: Int? = null
)

data class UniformLabel(
    val label: String,
    val display: String,
    val code: String? = null,
    val group: String
)

data class UniformOneviewConfig(
    val validLabels: List<UniformLabel>? = null
)

data class OneviewConfigModel(
    val profile: Map<String, Any>? = null,
    val attendance: Map<String, Any>? = null,
    val uniform: Map<String, Any>? = null
)

// ==================== Uniform Config & Labels ====================

data class UniformRuleItem(
    val required: Boolean = false,
    val labels: List<String> = emptyList()
)

data class UniformRuleData(
    val uniform: UniformRuleItem? = null,
    val hat: UniformRuleItem? = null,
    val gloves: UniformRuleItem? = null,
    val shoes: UniformRuleItem? = null,
    val mask: UniformRuleItem? = null,
    val vest: UniformRuleItem? = null
)

data class UniformConfigRules(
    val data: Map<String, UniformRuleItem> = emptyMap(),
    val options: Map<String, Any>? = null
)

data class UniformCategoryLabel(
    val key: String,
    val name: String,
    @SerializedName("default_required")
    val defaultRequired: Boolean? = null,
    val labels: List<String> = emptyList()
)

data class UniformAvailableLabelsResponse(
    val source: String? = null,
    @SerializedName("dataset_code")
    val datasetCode: String? = null,
    val categories: List<UniformCategoryLabel> = emptyList()
)

data class UniformConfigModel(
    val id: Int? = null,
    val uuid: String? = null,
    val code: String,
    val name: String,
    val description: String? = null,
    @SerializedName("check_mode")
    val checkMode: String? = null,
    @SerializedName("valid_from")
    val validFrom: String? = null,
    @SerializedName("valid_to")
    val validTo: String? = null,
    val rules: Map<String, Any>? = null,
    @SerializedName("extra_config")
    val extraConfig: Map<String, Any>? = null,
    @SerializedName("extra_info")
    val extraInfo: Any? = null,
    @SerializedName("is_active")
    val isActive: Any? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    @SerializedName("valid_from_epoch")
    val validFromEpoch: Long? = null,
    @SerializedName("valid_to_epoch")
    val validToEpoch: Long? = null
)

data class UniformLabelUiConfig(
    val color: String? = null
)

data class UniformLabelExtraConfig(
    val ui: UniformLabelUiConfig? = null,
    @SerializedName("gallery_images")
    val galleryImages: List<String>? = null
)

data class UniformLabelModel(
    val id: Int,
    val uuid: String,
    val code: String,
    val name: String,
    @SerializedName("category_key")
    val categoryKey: String,
    @SerializedName("image_uuid")
    val imageUuid: String? = null,
    val description: String? = null,
    val priority: Int? = null,
    @SerializedName("is_active")
    val isActive: Any? = null,
    @SerializedName("extra_config")
    val extraConfig: UniformLabelExtraConfig? = null,
    @SerializedName("extra_info")
    val extraInfo: Any? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    @SerializedName("category_name")
    val categoryName: String? = null,
    @SerializedName("category_uuid")
    val categoryUuid: String? = null
)

data class FetchUniformLabelsParams(
    @SerializedName("category_key")
    val categoryKey: String? = null,
    @SerializedName("is_active")
    val isActive: Any? = null,
    val search: String? = null
)

data class UniformAssignmentModel(
    val id: Int? = null,
    val uuid: String? = null,
    val code: String? = null,
    val name: String,
    val description: String? = null,
    @SerializedName("config_uuid")
    val configUuid: String,
    @SerializedName("config_name")
    val configName: String? = null,
    @SerializedName("config_code")
    val configCode: String? = null,
    @SerializedName("uniform_config_id")
    val uniformConfigId: Int? = null,
    @SerializedName("target_type")
    val targetType: String,
    @SerializedName("target_values")
    val targetValues: List<String> = emptyList(),
    @SerializedName("zone_type")
    val zoneType: String? = null,
    @SerializedName("zone_values")
    val zoneValues: Any? = null,
    @SerializedName("shift_uuids")
    val shiftUuids: List<String>? = null,
    val priority: Int? = null,
    @SerializedName("is_active")
    val isActive: Any? = null,
    @SerializedName("empty_action")
    val emptyAction: String? = null,
    @SerializedName("effective_empty_action")
    val effectiveEmptyAction: String? = null,
    @SerializedName("system_default_action")
    val systemDefaultAction: String? = null,
    @SerializedName("extra_config")
    val extraConfig: Map<String, Any>? = null,
    @SerializedName("extra_info")
    val extraInfo: Any? = null,
    @SerializedName("valid_from")
    val validFrom: String? = null,
    @SerializedName("valid_to")
    val validTo: String? = null,
    @SerializedName("valid_from_epoch")
    val validFromEpoch: Long? = null,
    @SerializedName("valid_to_epoch")
    val validToEpoch: Long? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class UniformEvaluationInfo(
    @SerializedName("is_violation")
    val isViolation: Boolean = false,
    @SerializedName("config_code")
    val configCode: String? = null,
    @SerializedName("assignment_uuid")
    val assignmentUuid: String? = null,
    @SerializedName("matched_shift_code")
    val matchedShiftCode: String? = null,
    @SerializedName("matched_scope")
    val matchedScope: String? = null,
    @SerializedName("valid_labels")
    val validLabels: List<String>? = null
)

data class UniformComplianceEvaluationItem(
    val id: Any,
    val serviceId: Any,
    val originId: Any? = null,
    val cameraId: Any? = null,
    val type: String,
    val time: Double,
    val isConfirmed: Boolean = true,
    val data: EventDataDetail? = null,
    @SerializedName("uniform_evaluation")
    val uniformEvaluation: UniformEvaluationInfo? = null
)

data class FetchUniformComplianceParams(
    val fromTime: Long? = null,
    val toTime: Long? = null,
    val from: Long? = null,
    val to: Long? = null,
    val serviceId: String? = null,
    val timezone: String? = null,
    val eventType: String? = null,
    val isViolation: Any? = null,
    val cameraId: String? = null,
    val cameraUUID: String? = null,
    val camerauuid: String? = null,
    val profileUUID: String? = null,
    val assignmentUuid: String? = null,
    val configCode: String? = null,
    val count: Int? = null,
    val order: String? = null,
    val lastId: Int? = null
)
