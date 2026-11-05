package com.tenli.oneview.ui.features.event.detail

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.tenli.oneview.model.network.AIServiceModel
import com.tenli.oneview.model.network.BsProfileGroupModel
import com.tenli.oneview.model.network.BsProfileModel
import com.tenli.oneview.model.network.CameraModel
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.ui.component.isUuidString
import com.tenli.oneview.ui.features.event.displayPlateNumber
import com.tenli.oneview.ui.features.event.findMatchedProfile
import com.tenli.oneview.ui.features.event.formatEventTimeAndDate
import com.tenli.oneview.ui.features.event.isPlateBlacklisted
import com.tenli.oneview.ui.features.home.findAllBoxes
import com.tenli.oneview.ui.features.home.getEventCropUrls
import com.tenli.oneview.ui.features.home.getEventImageUrl
import com.tenli.oneview.ui.features.home.getEventVideoUrl
import com.tenli.oneview.ui.theme.BrandAccent
import com.tenli.oneview.ui.theme.ErrorRedDeep
import com.tenli.oneview.ui.utils.AiTypeHelper

/*
 * Dữ liệu hiển thị của màn Chi tiết sự kiện, tính 1 lần cho mỗi (sự kiện, camera, ngôn ngữ, profile)
 * thay vì rải rác hàng chục phép tính / remember trong composition.
 */

/** Thông tin định danh khuôn mặt (FaceEventDetailFrame.vue) */
@Immutable
internal data class FaceDisplay(
    val profileName: String,
    /** Người đã định danh → hiển thị tên trong badge màu nhóm */
    val showNameBadge: Boolean,
    val groupName: String,
    val groupColor: Color
)

/** Thông tin giám sát đồng phục (UniformEventDetailFrame.vue) */
@Immutable
internal data class UniformDisplay(
    val personName: String,
    val isIdentified: Boolean,
    /** Rỗng nếu không có nhãn trang phục */
    val uniformLabel: String,
    val color: Color
)

/** Thông tin đám đông (CrowdEventDetailFrame.vue) */
@Immutable
internal data class CrowdDisplay(
    val countText: String,
    /** null nếu sự kiện không có ngưỡng */
    val thresholdText: String?
)

@Immutable
internal data class EventDetailDisplay(
    val aiTaskTitle: String,
    val locationText: String,
    val formattedTime: String,
    val formattedDate: String,
    val imageUrl: String?,
    val videoUrl: String?,
    val cropUrls: List<String>,
    /** Các bounding box chuẩn hóa [minX, minY, maxX, maxY] */
    val boxes: List<List<Float>>,
    val plateNumber: String,
    val isPlateBlacklist: Boolean,
    val canShowRelatedPlates: Boolean,
    val face: FaceDisplay?,
    val uniform: UniformDisplay?,
    /** Nhãn loại cảnh báo cháy / khói, null nếu không phải sự kiện cháy khói */
    val fireAlertLabel: String?,
    val crowd: CrowdDisplay?,
    val vehicleType: String,
    val vehicleColor: String,
    val vehicleBrand: String,
    val vehicleSpeed: String,
    val zoneName: String
) {
    /** Box chính – dùng làm tâm hiệu ứng zoom trên video gốc */
    val mainBox: List<Float>? get() = boxes.firstOrNull()?.takeIf { it.size == 4 }
}

internal fun buildEventDetailDisplay(
    event: EventData,
    camera: CameraModel?,
    aiService: AIServiceModel?,
    profiles: List<BsProfileModel>,
    groups: List<BsProfileGroupModel>,
    isEn: Boolean
): EventDetailDisplay {
    val typeLower = event.type.lowercase()
    val d = event.data
    val aiTaskTitle = resolvePwaAiTaskTitle(event, isEn)
    val (formattedTime, formattedDate) = formatEventTimeAndDate(event.time, isEn)
    val plateNumber = event.displayPlateNumber()

    val face = if (AiTypeHelper.isFaceEvent(event)) {
        val isStranger = AiTypeHelper.isStranger(event)
        val profileName = resolveFaceProfileDisplayName(event, profiles, isEn)
        val (groupName, groupColor) = resolveFaceGroupNameAndColor(event, profiles, groups, isEn)
        FaceDisplay(
            profileName = profileName,
            showNameBadge = !isStranger && profileName != "---" && !isStrangerLabel(profileName),
            groupName = groupName,
            groupColor = groupColor
        )
    } else null

    val uniform = if (typeLower.contains("uniform") || typeLower.contains("clothing")) {
        val name = AiTypeHelper.extractFacePersonName(event, isEn)
        val isIdentified = name.isNotBlank() && !isStrangerLabel(name)
        val label = AiTypeHelper.extractUniformLabel(event, isEn)
        UniformDisplay(
            personName = if (isIdentified) name else if (isEn) "Unidentified" else "Chưa định danh",
            isIdentified = isIdentified,
            uniformLabel = if (label.isNotBlank() && label != "-") label else "",
            color = AiTypeHelper.getAiColor(event)
        )
    } else null

    val fireAlertLabel = if (typeLower.contains("fire") || typeLower.contains("smoke")) {
        val isSmoke = typeLower.contains("smoke") && !typeLower.contains("fire")
        if (isSmoke) {
            if (isEn) "Smoke detected" else "Phát hiện khói"
        } else {
            if (isEn) "Fire detected" else "Phát hiện lửa"
        }
    } else null

    val crowd = if (typeLower.contains("crowd")) {
        val peopleUnit = if (isEn) "people" else "người"
        val countVal = d?.count ?: d?.peopleCount ?: d?.crowdCount
        val thVal = d?.threshold ?: d?.crowdThreshold
        CrowdDisplay(
            countText = if (countVal != null) "$countVal $peopleUnit" else if (isEn) "High density" else "Mật độ cao",
            thresholdText = thVal?.let { "> $it $peopleUnit" }
        )
    } else null

    val speed = d?.vehicle?.speed ?: d?.speed ?: d?.vehicleSpeed

    return EventDetailDisplay(
        aiTaskTitle = aiTaskTitle,
        locationText = resolveLocationText(event, camera, aiService),
        formattedTime = formattedTime,
        formattedDate = formattedDate,
        imageUrl = getEventImageUrl(event),
        videoUrl = getEventVideoUrl(event),
        cropUrls = getEventCropUrls(event),
        boxes = findAllBoxes(event),
        plateNumber = plateNumber,
        isPlateBlacklist = event.isPlateBlacklisted,
        canShowRelatedPlates = isVehicleRelatedEvent(event, plateNumber),
        face = face,
        uniform = uniform,
        fireAlertLabel = fireAlertLabel,
        crowd = crowd,
        vehicleType = resolveVehicleTypeLabel(event, aiTaskTitle, isEn),
        vehicleColor = resolveVehicleColorLabel(event, isEn),
        vehicleBrand = (d?.vehicle?.brand ?: d?.brand ?: d?.vehicleBrand)?.trim().orEmpty(),
        vehicleSpeed = if (speed != null && speed.toString().isNotBlank()) "$speed km/h" else "",
        zoneName = (d?.zoneName ?: d?.zone)?.trim().orEmpty()
    )
}

private fun isStrangerLabel(name: String): Boolean = name == "Người lạ" || name == "Stranger"

private fun resolveLocationText(event: EventData, camera: CameraModel?, aiService: AIServiceModel?): String {
    val rawCam = camera?.name?.takeIf { it.isNotBlank() && !isUuidString(it) }
        ?: event.data?.cameraName?.takeIf { it.isNotBlank() && !isUuidString(it) }
        ?: event.cameraName?.takeIf { it.isNotBlank() && !isUuidString(it) }
        ?: ""
    val rawSvc = aiService?.name?.trim().orEmpty()
    return when {
        rawCam.isNotEmpty() && rawSvc.isNotEmpty() -> "$rawCam - $rawSvc"
        rawCam.isNotEmpty() -> rawCam
        rawSvc.isNotEmpty() -> rawSvc
        else -> "---"
    }
}

/**
 * Matches `aiTaskName` in `BaseAiEventDetailModal.vue` lines 1323-1422.
 */
private fun resolvePwaAiTaskTitle(event: EventData, isEn: Boolean): String {
    val rawType = event.type.trim()
    val tStr = rawType.lowercase()

    if (AiTypeHelper.isFaceEvent(event)) {
        return if (isEn) "Face recognition" else "Nhận diện khuôn mặt"
    }
    if (tStr.contains("lpr") || tStr.contains("plate") || tStr.contains("license") || event.data?.plate != null) {
        return if (isEn) "License plate recognition" else "Nhận diện biển số xe"
    }
    if (tStr.contains("fire") || tStr.contains("smoke")) {
        return if (isEn) "Fire & smoke detection" else "Phát hiện cháy / khói"
    }
    if (tStr.contains("crowd")) {
        return if (isEn) "Crowd detection" else "Phát hiện đám đông"
    }
    if (tStr.contains("uniform") || tStr.contains("clothing")) {
        return if (isEn) "Uniform monitoring" else "Giám sát đồng phục"
    }
    if (tStr.contains("vehicle") || tStr.contains("object-end")) {
        return if (isEn) "Vehicle recognition" else "Nhận diện phương tiện"
    }
    if (tStr.contains("helmet")) {
        return if (isEn) "No helmet detection" else "Phát hiện không đội mũ bảo hiểm"
    }
    if (tStr.contains("parking")) {
        return if (isEn) "Illegal parking" else "Dừng đỗ sai quy định"
    }
    if (tStr.contains("wrong_way") || tStr.contains("crossing")) {
        return if (isEn) "Wrong-way driving" else "Đi ngược chiều"
    }
    if (tStr.contains("person") || tStr.contains("intrusion")) {
        return if (isEn) "Intrusion detection" else "Phát hiện xâm nhập"
    }
    if (tStr.contains("sidewalk") || tStr.contains("encroachment")) {
        return if (isEn) "Sidewalk encroachment" else "Lấn chiếm vỉa hè"
    }
    if (tStr.contains("garbage") || tStr.contains("littering")) {
        return if (isEn) "Littering detection" else "Phát hiện xả rác"
    }
    if (tStr.contains("fall")) {
        return if (isEn) "Fall detection" else "Phát hiện té ngã"
    }
    if (tStr.contains("smoking") || tStr.contains("pose")) {
        return if (isEn) "Smoking detection" else "Phát hiện hút thuốc"
    }
    if (tStr.contains("phone")) {
        return if (isEn) "Phone usage" else "Sử dụng điện thoại"
    }
    if (tStr.contains("violence")) {
        return if (isEn) "Violent behavior" else "Hành vi bạo lực"
    }
    if (tStr.contains("no_mask") || tStr.contains("masked") || tStr.contains("mask")) {
        return if (isEn) "No mask violation" else "Không đeo khẩu trang"
    }
    if (tStr.contains("gloves") || tStr.contains("glove")) {
        return if (isEn) "No gloves violation" else "Không đeo găng tay"
    }
    if (tStr.contains("hairnet")) {
        return if (isEn) "No safety cap" else "Không đội mũ bảo hộ"
    }
    if (tStr.contains("construction")) {
        return if (isEn) "Construction detection" else "Phát hiện thi công"
    }

    return AiTypeHelper.getTypeName(rawType, isEn)
}

private val NonVehicleKeywords = listOf(
    "uniform", "clothing", "fire", "smoke", "crowd", "face", "attendance",
    "fall", "smoking", "phone", "violence", "gloves", "hairnet", "mask",
    "garbage", "littering", "intrusion", "person", "heatmap", "construction"
)

private val VehicleKeywords = listOf(
    "license-plate", "lpr", "plate", "vehicle", "object-end", "parking",
    "crossing-tracking", "wrong_way", "wrong-way", "traffic", "helmet",
    "sidewalk", "encroachment"
)

private fun isVehicleRelatedEvent(event: EventData, plateNumber: String): Boolean {
    if (plateNumber.isNotEmpty()) return true
    if (AiTypeHelper.isFaceEvent(event)) return false
    val d = event.data
    if (d?.plate != null || d?.vehicle != null) return true
    val t = event.type.lowercase()
    if (NonVehicleKeywords.any { t.contains(it) }) return false
    return VehicleKeywords.any { t.contains(it) }
}

private val VehicleTypeLabelsEn = mapOf(
    "car" to "Car",
    "truck" to "Truck",
    "bus" to "Bus",
    "motorbike" to "Motorbike",
    "motorcycle" to "Motorcycle",
    "bicycle" to "Bicycle",
    "bike" to "Motorbike",
    "van" to "Van / Pickup",
    "container" to "Container truck",
    "suv" to "SUV",
    "sedan" to "Sedan"
)

private val VehicleTypeLabelsVi = mapOf(
    "car" to "Ô tô",
    "truck" to "Xe tải",
    "bus" to "Xe khách / Xe buýt",
    "motorbike" to "Xe máy",
    "motorcycle" to "Xe máy",
    "bicycle" to "Xe đạp",
    "bike" to "Xe máy",
    "van" to "Xe bán tải / Van",
    "container" to "Xe container",
    "suv" to "Xe SUV",
    "sedan" to "Xe Sedan"
)

private fun resolveVehicleTypeLabel(event: EventData, taskTitle: String, isEn: Boolean): String {
    val d = event.data ?: return ""
    val rawType = (d.vehicle?.type ?: d.vehicleTypeSnake ?: d.vehicleType ?: d.plate?.vehicleType)?.trim().orEmpty()
    if (rawType.isEmpty()) return ""
    val lower = rawType.lowercase()
    if (lower.startsWith("sensor-") || lower.contains("sensor") || lower == "vehicle" || lower == "phương tiện" || lower == "nhận diện phương tiện") {
        return ""
    }
    val titleLower = taskTitle.lowercase()
    if (taskTitle.isNotEmpty() && (lower == titleLower || titleLower.contains(lower))) {
        return ""
    }
    val map = if (isEn) VehicleTypeLabelsEn else VehicleTypeLabelsVi
    return map[lower] ?: rawType
}

private val VehicleColorLabelsEn = mapOf(
    "white" to "White",
    "black" to "Black",
    "red" to "Red",
    "blue" to "Blue",
    "green" to "Green",
    "yellow" to "Yellow",
    "silver" to "Silver",
    "grey" to "Gray",
    "gray" to "Gray",
    "white_silver" to "Silver White"
)

private val VehicleColorLabelsVi = mapOf(
    "white" to "Trắng",
    "black" to "Đen",
    "red" to "Đỏ",
    "blue" to "Xanh dương",
    "green" to "Xanh lá",
    "yellow" to "Vàng",
    "silver" to "Bạc",
    "grey" to "Xám",
    "gray" to "Xám",
    "white_silver" to "Trắng bạc"
)

private fun resolveVehicleColorLabel(event: EventData, isEn: Boolean): String {
    val d = event.data ?: return ""
    val raw = (d.vehicle?.color ?: d.color ?: d.vehicleColor)?.trim().orEmpty()
    if (raw.isEmpty()) return ""
    val map = if (isEn) VehicleColorLabelsEn else VehicleColorLabelsVi
    return map[raw.lowercase()] ?: raw
}

private fun resolveFaceProfileDisplayName(
    event: EventData,
    profiles: List<BsProfileModel>,
    isEn: Boolean
): String {
    if (AiTypeHelper.isStranger(event)) {
        return if (isEn) "Stranger" else "Người lạ"
    }
    val d = event.data
    val directName = AiTypeHelper.extractFacePersonName(event, isEn)
    val matchedProfile = findMatchedProfile(event, profiles, isEn)

    val baseName = matchedProfile?.name?.takeIf { it.isNotBlank() }
        ?: directName.takeIf { it.isNotBlank() }
        ?: (if (isEn) "Stranger" else "Người lạ")

    val idCode = (d?.idCode ?: d?.idCodeSnake ?: matchedProfile?.idCode ?: matchedProfile?.idCodeSnake)?.trim().orEmpty()
    if (idCode.isNotEmpty() && !isStrangerLabel(baseName) && !baseName.contains(idCode)) {
        return "$baseName ($idCode)"
    }
    return baseName
}

/** Màu nhóm mặc định khi profile / nhóm không cấu hình màu */
private val DefaultGroupColor = BrandAccent

private fun resolveFaceGroupNameAndColor(
    event: EventData,
    profiles: List<BsProfileModel>,
    groups: List<BsProfileGroupModel>,
    isEn: Boolean
): Pair<String, Color> {
    if (AiTypeHelper.isStranger(event)) {
        return Pair(if (isEn) "Stranger" else "Người lạ", ErrorRedDeep)
    }
    val d = event.data
    val directGroupName = (d?.face?.groupName ?: d?.groupName)?.trim().orEmpty()
    val directGroupColor = d?.face?.groupColor ?: d?.face?.groupColorSnake ?: d?.groupColor ?: d?.groupColorSnake ?: event.groupColor
    val matchedProfile = findMatchedProfile(event, profiles, isEn)

    val profileGroup = matchedProfile?.groups?.firstOrNull()
        ?: matchedProfile?.groupIds?.firstOrNull()?.let { gId -> groups.find { it.id == gId } }
        ?: (d?.face?.groupId ?: d?.groupId ?: event.groupId)?.let { gKey ->
            groups.find { it.uuid == gKey || it.id?.toString() == gKey }
        }

    val resolvedGroupName = when {
        directGroupName.isNotEmpty() && directGroupName != "---" -> directGroupName
        !matchedProfile?.groupName.isNullOrBlank() -> matchedProfile!!.groupName!!
        !matchedProfile?.groupNameSnake.isNullOrBlank() -> matchedProfile!!.groupNameSnake!!
        !profileGroup?.name.isNullOrBlank() -> profileGroup!!.name
        else -> "---"
    }

    val colorHex = directGroupColor
        ?: matchedProfile?.groupColor
        ?: matchedProfile?.groupColorSnake
        ?: profileGroup?.config?.ui?.color
        ?: profileGroup?.config?.ui?.background

    val resolvedColor = AiTypeHelper.parseHexColor(colorHex) ?: DefaultGroupColor
    return Pair(resolvedGroupName, resolvedColor)
}
