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
    val hasMask: Boolean? = null
)

data class PlateData(
    val plate: String? = null,
    val cropImage: String? = null,
    val deblurCropImage: String? = null,
    val similarity: Double = 0.0,
    val cropBox: List<Double>? = null
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
    val cropImage: String? = null
)

data class ObjectData(
    val image: String? = null,
    val cropBox: List<Double>? = null,
    val cropImage: String? = null
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
    val video: String? = null,
    val cropImage: String? = null,
    val name: String? = null,
    val box: List<Double>? = null,
    val value: String? = null,
    val face: FaceData? = null,
    val plate: PlateData? = null,
    val attribute: AttributeData? = null,
    val focusVideo: String? = null,
    val profileId: Int? = null,
    val faceCrop: String? = null,
    val uniform: UniformData? = null,
    @SerializedName("object")
    val objectData: ObjectData? = null,
    val weapon: WeaponData? = null,
    val similarity: Double = 0.0,
    val containerId: String? = null,
    val cameraUUID: String? = null,
    val boxes: List<BoxData>? = null
)

data class EventData(
    val id: Int,
    val serviceId: Int,
    val originId: Int,
    val type: String,
    val time: Double,
    val isConfirmed: Boolean,
    val data: EventDataDetail? = null
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
