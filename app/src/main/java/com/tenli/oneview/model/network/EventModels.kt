package com.tenli.oneview.model.network

import com.google.gson.annotations.SerializedName

// ==================== AI Service ====================

data class AIServiceModel(
    val id: Int,
    val name: String,
    val type: String,
    val privateHost: String,
    val publicHost: String,
    val mediaSyncType: String,
    val sharedMediaDirectory: String,
    val apiKey: String,
    val extra: Map<String, Any>? = null
)

// ==================== Event Data ====================

data class FaceData(
    val cropImage: String? = null,
    val similarity: Double = 0.0,
    val cropBox: List<Double>? = null,
    val hasMask: Boolean? = null,
    val profileId: Int? = null,
    val profileName: String? = null,
    val profileUUID: String? = null,
    @SerializedName("ProfileUUID")
    val profileUUIDUpper: String? = null,
    val name: String? = null,
    val isStranger: Boolean? = null,
    val groupId: String? = null,
    val groupName: String? = null,
    val groupColor: String? = null,
    @SerializedName("group_color")
    val groupColorSnake: String? = null
)

data class PlateData(
    val plate: String? = null,
    val number: String? = null,
    val cropImage: String? = null,
    val deblurCropImage: String? = null,
    val similarity: Double = 0.0,
    val cropBox: List<Double>? = null,
    @SerializedName("is_blacklist")
    val isBlacklistSnake: Boolean? = null,
    val isBlacklist: Boolean? = null,
    @SerializedName("vehicle_type")
    val vehicleType: String? = null
)

data class AttributeData(
    val label: Int = 0,
    val cropImage: String? = null,
    val similarity: Double = 0.0,
    val cropBox: List<Double>? = null
)

data class BoxData(
    val box: List<Double>? = null,
    val time: Double = 0.0
)

data class UniformData(
    val image: String? = null,
    val cropBox: List<Double>? = null,
    val cropImage: String? = null,
    val uniformLabel: String? = null,
    val code: String? = null,
    val label: String? = null,
    val name: String? = null,
    val isViolation: Boolean? = null
)

data class ObjectData(
    val image: String? = null,
    val cropBox: List<Double>? = null,
    val cropImage: String? = null,
    val type: String? = null,
    val color: String? = null,
    val brand: String? = null,
    val speed: Any? = null,
    val plate: String? = null
)

data class WeaponData(
    val image: String? = null,
    val label: String? = null,
    val cropImage: String? = null,
    val cropBox: List<Double>? = null
)

data class EventDataDetail(
    val type: String? = null,
    val image: String? = null,
    val fullImage: String? = null,
    val video: String? = null,
    val videoUrl: String? = null,
    val cropImage: String? = null,
    val name: String? = null,
    val box: List<Double>? = null,
    val value: String? = null,
    val face: FaceData? = null,
    val hat: ObjectData? = null,
    val card: ObjectData? = null,
    val vehicle: ObjectData? = null,
    val person: ObjectData? = null,
    val plate: PlateData? = null,
    val attribute: AttributeData? = null,
    val focusVideo: String? = null,
    val profileId: Int? = null,
    val profileUUID: String? = null,
    @SerializedName("ProfileUUID")
    val profileUUIDUpper: String? = null,
    val faceCrop: String? = null,
    @SerializedName("face_crop_image")
    val faceCropImageSnake: String? = null,
    val faceCropImage: String? = null,
    val faceId: Int? = null,
    val distance: Double? = null,
    val monitorId: Int? = null,
    val monitorType: String? = null,
    val uniform: UniformData? = null,
    val uniformLabel: String? = null,
    val isViolation: Boolean? = null,
    val isStranger: Boolean? = null,
    val groupId: String? = null,
    val groupName: String? = null,
    val groupColor: String? = null,
    @SerializedName("group_color")
    val groupColorSnake: String? = null,
    val idCode: String? = null,
    @SerializedName("id_code")
    val idCodeSnake: String? = null,
    @SerializedName("object")
    val objectData: ObjectData? = null,
    val weapon: WeaponData? = null,
    val similarity: Double = 0.0,
    val containerId: String? = null,
    val cameraUUID: String? = null,
    @SerializedName("camerauuid")
    val cameraUuidLower: String? = null,
    val cameraName: String? = null,
    val boxes: List<BoxData>? = null,
    @SerializedName("vehicle_type")
    val vehicleTypeSnake: String? = null,
    val vehicleType: String? = null,
    val color: String? = null,
    val vehicleColor: String? = null,
    val brand: String? = null,
    val vehicleBrand: String? = null,
    val speed: Any? = null,
    val vehicleSpeed: Any? = null,
    val zoneName: String? = null,
    val zone: String? = null,
    val count: Int? = null,
    val peopleCount: Int? = null,
    @SerializedName("crowd_count")
    val crowdCount: Int? = null,
    val threshold: Int? = null,
    val crowdThreshold: Int? = null,
    val violation: String? = null,
    val violationName: String? = null,
    @SerializedName("violation_name")
    val violationNameSnake: String? = null,
    val reason: String? = null,
    val note: String? = null,
    val behavior: String? = null
)

data class EventData(
    val id: Int,
    val serviceId: Int,
    val originId: Int = 0,
    val cameraId: Any? = null,
    val type: String,
    val time: Double,
    val isConfirmed: Boolean = true,
    val name: String? = null,
    val cameraName: String? = null,
    val plate: String? = null,
    val isStranger: Boolean? = null,
    val isViolation: Boolean? = null,
    val groupId: String? = null,
    val groupColor: String? = null,
    val data: EventDataDetail? = null
)

data class UpdateEventTimeModel(
    val newEventTime: Long
)

// ==================== Search ====================

data class ParamPlateModel(
    val value: String
)

data class DataSearchPlateModel(
    val type: String,
    val confidence: Double,
    val param: ParamPlateModel
)

data class ParamFaceModel(
    val modelName: String,
    val embedding: List<Double>
)

data class DataSearchFaceModel(
    val type: String,
    val confidence: Double,
    val param: ParamFaceModel
)

data class FeatureModel(
    val modelName: String,
    val embedding: List<Double>
)

data class SearchModel(
    val objects: List<String>? = null,
    val time: SearchTimeRange? = null,
    val plates: List<String>? = null,
    val faces: List<String>? = null,
    val colors: List<String>? = null,
    val faceConfidence: Double? = null,
    val plateConfidence: Double? = null,
    val advancedText: List<String>? = null,
    val advancedTextConfidence: Double? = null,
    val advanceImages: List<String>? = null,
    val advanceImageConfidence: Double? = null,
    val timeSearch: String? = null,       // '1w' | '1m' | '2m' | 'all'
    val typeSearch: String? = null,       // 'all' | 'face' | 'plate' | 'attribute'
    val searchMode: String? = null,       // 'fast' | 'deep'
    val count: Int? = null
)

data class SearchTimeRange(
    val start: Long,
    val end: Long
)

// ==================== Model Run Response ====================

data class ModelRunEmbeddingText(
    val model: String? = null,
    val text: List<Double>? = null,
    @SerializedName("text_input")
    val textInput: String? = null
)

data class ModelRunTextResponse(
    val embeddings: ModelRunEmbeddingText? = null,
    val time: Double? = null,
    val id: String? = null,
    val alias: String? = null,
    val host: String? = null
)

data class ModelRunEmbeddingImage(
    val model: String? = null,
    val image: List<Double>? = null,
    val face: List<Double>? = null
)

data class ModelRunImageResponse(
    val embeddings: ModelRunEmbeddingImage? = null,
    val time: Double? = null,
    val id: String? = null,
    val alias: String? = null,
    val host: String? = null
)

// ==================== Confirm ====================

data class Confirm(
    val isConfirmed: Boolean
)

data class DataSummary(
    val total: Int,
    val groups: Map<String, Int>? = null
)

// ==================== Face ====================

data class FaceImageCrop(
    val faceCropId: String? = null
)

data class Face(
    val id: Int,
    val profileId: Int
)

data class FaceEntry(
    val items: List<Face>,
    val isLoaded: Boolean
)

// ==================== Profile In Group ====================

data class ProfileInProfileGroup(
    val id: Int? = null,
    val groupId: Int,
    val profileId: Int
)

data class UpdateProfileInProfileGroup(
    val profileId: Int,
    val groupIds: List<Int>
)

// ==================== License Plate ====================

data class LicensePlate(
    val id: Int,
    val uuid: String,
    val plate: String,
    val type: String,
    val enabled: Boolean,
    val name: String? = null,
    val department: String? = null,
    val position: String? = null
)

data class LicensePlateEntry(
    val items: List<LicensePlate>,
    val isLoaded: Boolean
)

// ==================== Rule ====================

data class TargetModel(
    val type: String,
    val value: String,
    val enabled: Boolean? = null
)

data class ParamActionModel(
    val targets: List<TargetModel>,
    val variables: ActionVariables,
    val imageBase64: String? = null
)

data class ActionVariables(
    val publicDomain: String,
    val faceName: String? = null,
    val scenarioName: String? = null
)

data class ActionModel(
    val action: String,
    val param: ParamActionModel
)

data class ConditionModel(
    val type: String,
    val op: String,
    val expected: Any,  // String hoặc List<Int> trong TS
    val similarity: Double
)

data class RuleObjectModel(
    val conditions: List<ConditionModel>,
    val actions: List<ActionModel>
)

data class RuleModel(
    val id: Int,
    val name: String,
    val key: String? = null,
    val enabled: Boolean,
    val rule: RuleObjectModel
)
