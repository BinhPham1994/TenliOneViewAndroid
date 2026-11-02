package com.tenli.oneview.ui.utils

import androidx.compose.ui.graphics.Color
import com.tenli.oneview.model.network.EventData

data class EventSentenceParts(
    val prefix: String,
    val subject: String,
    val color: Color,
    val cameraName: String
)

data class AiModuleOption(
    val key: String,
    val sensorType: String,
    val labelVi: String,
    val labelEn: String
)

object AiTypeHelper {

    val activeModules: List<AiModuleOption> = listOf(
        AiModuleOption("FaceID", "logic-face", "Nhận diện khuôn mặt", "Face recognition"),
        AiModuleOption("Uniform", "sensor-uniform-camera", "Nhận diện trang phục", "Uniform detection"),
        AiModuleOption("Crowd", "sensor-crowd-camera", "Phát hiện đông người", "Crowd detection"),
        AiModuleOption("LPR", "sensor-license-plate", "Nhận diện biển số", "License plate recognition"),
        AiModuleOption("FireSmoke", "sensor-fire-camera", "Phát hiện Cháy & Khói", "Fire & smoke detection"),
        AiModuleOption("Helmet", "sensor-helmet-camera", "Không đội mũ bảo hiểm", "No helmet violation"),
        AiModuleOption("Parking", "sensor-parking-camera", "Phát hiện dừng đỗ xe", "Illegal parking"),
        AiModuleOption("Garbage", "sensor-littering-camera", "Phát hiện xả rác", "Littering detection"),
        AiModuleOption("Intrusion", "sensor-person-camera", "Phát hiện xâm nhập", "Intrusion detection"),
        AiModuleOption("VehicleRecognition", "sensor-vehicle-camera", "Nhận diện phương tiện", "Vehicle recognition"),
        AiModuleOption("SidewalkEncroachment", "sensor-encroachment-camera", "Lấn chiếm vỉa hè", "Sidewalk encroachment"),
        AiModuleOption("ConstructionDetection", "sensor-construction-camera", "Phát hiện thi công", "Construction detection"),
        AiModuleOption("WrongWayDetection", "sensor-crossing-tracking", "Đi ngược chiều", "Wrong-way driving"),
        AiModuleOption("Violence", "sensor-violence", "Phát hiện đánh nhau", "Violence detection"),
        AiModuleOption("Smoking", "sensor-smoking", "Phát hiện hút thuốc", "Smoking detection"),
        AiModuleOption("Phone", "sensor-phone-usage", "Sử dụng điện thoại", "Phone usage"),
        AiModuleOption("FallDetection", "sensor-fall-person-camera", "Phát hiện người ngã", "Fall detection"),
        AiModuleOption("ObjectTracking", "sensor-object-tracking", "Theo dõi đối tượng", "Object tracking"),
        AiModuleOption("GlovesDetection", "sensor-gloves", "Không đeo găng tay", "No gloves violation"),
        AiModuleOption("HairnetDetection", "sensor-hairnet", "Không đội mũ bảo hộ", "No hairnet violation"),
        AiModuleOption("NoMaskDetection", "sensor-face-masked", "Không đeo khẩu trang", "No mask violation")
    )

    // Matching TAB_CONFIG.business.modules enabled types in src/config/modules.ts
    private val enabledAiTypes = setOf(
        "logic-face",
        "sensor-face-unknown-camera",
        "sensor-uniform-camera",
        "logic-uniform",
        "sensor-crowd-camera",
        "sensor-license-plate",
        "sensor-fire-camera",
        "sensor-smoke-camera",
        "logic-fire",
        "sensor-helmet-camera",
        "sensor-helmet-violation-camera",
        "sensor-parking-camera",
        "sensor-littering-camera",
        "sensor-person-camera",
        "logic-person",
        "sensor-vehicle-camera",
        "sensor-encroachment-camera",
        "sensor-construction-camera",
        "construction-sensor-camera",
        "sensor-crossing-tracking",
        "sensor-violence",
        "sensor-smoking",
        "sensor-pose-camera",
        "sensor-phone-usage",
        "sensor-phone-camera",
        "sensor-fall-person-camera",
        "logic-fall-person",
        "sensor-object-tracking",
        "sensor-object",
        "object-begin",
        "object-end",
        "sensor-gloves",
        "sensor-hairnet",
        "sensor-face-masked"
    )

    fun isAiTypeEnabled(type: String?): Boolean {
        if (type.isNullOrBlank()) return false
        val t = type.trim()
        if (t == "camera-online" || t == "camera-offline") return false
        return enabledAiTypes.contains(t)
    }

    fun getAiEventTypes(aiType: String): List<String> {
        return when (aiType.trim()) {
            "logic-face", "FaceID", "face" -> listOf("logic-face", "sensor-face-unknown-camera")
            "sensor-fire-camera", "FireSmoke", "fire_smoke" -> listOf("sensor-fire-camera", "sensor-smoke-camera")
            "sensor-helmet-camera", "Helmet", "helmet" -> listOf("sensor-helmet-violation-camera", "sensor-helmet-camera")
            "sensor-person-camera", "Intrusion", "intrusion" -> listOf("sensor-person-camera")
            else -> listOf(aiType.trim())
        }
    }

    fun isFaceEvent(event: EventData): Boolean {
        val t = event.type.lowercase()
        val d = event.data
        return !t.contains("uniform") && !t.contains("clothing") && (
            t.contains("face") ||
            t.contains("attendance") ||
            t.contains("stranger") ||
            d?.face != null ||
            !d?.profileUUID.isNullOrEmpty() ||
            !d?.profileUUIDUpper.isNullOrEmpty() ||
            d?.profileId != null ||
            !d?.faceCrop.isNullOrEmpty() ||
            !d?.faceCropImageSnake.isNullOrEmpty() ||
            !d?.faceCropImage.isNullOrEmpty()
        )
    }

    fun isStranger(event: EventData): Boolean {
        val t = event.type.lowercase()
        val d = event.data
        return t.contains("unknown") ||
            t.contains("stranger") ||
            event.isStranger == true ||
            d?.isStranger == true ||
            d?.face?.isStranger == true ||
            t == "sensor-face-unknown-camera" ||
            event.groupId == "stranger" ||
            d?.groupId == "stranger"
    }

    fun extractFacePersonName(event: EventData, isEn: Boolean = false): String {
        val d = event.data
        if (!event.name.isNullOrBlank() && event.name != "---") return event.name.trim()
        if (!d?.name.isNullOrBlank()) return d!!.name!!.trim()
        if (!d?.face?.name.isNullOrBlank()) return d!!.face!!.name!!.trim()
        if (!d?.face?.profileName.isNullOrBlank()) return d!!.face!!.profileName!!.trim()
        if (!d?.value.isNullOrBlank()) return d!!.value!!.trim()
        if (isStranger(event)) return if (isEn) "Stranger" else "Người lạ"
        return ""
    }

    fun extractUniformLabel(event: EventData, isEn: Boolean = false): String {
        val d = event.data
        val raw = d?.uniform?.uniformLabel
            ?: d?.uniform?.code
            ?: d?.uniform?.label
            ?: d?.uniform?.name
            ?: d?.uniformLabel
            ?: ""
        val lower = raw.trim().lowercase()
        if (raw.isBlank() || raw == "-" || lower == "unknown" || lower == "không xác định") {
            return if (isEn) "Wrong uniform" else "Đồng phục không đúng"
        }
        return raw.trim()
    }

    fun isUniformViolation(event: EventData): Boolean {
        val d = event.data
        val v = d?.isViolation ?: d?.uniform?.isViolation ?: event.isViolation
        return v ?: true
    }

    fun getEventBadgeLabel(event: EventData, isEn: Boolean = false): String {
        val t0 = event.type.lowercase()
        val d = event.data

        if (isFaceEvent(event)) {
            val personName = extractFacePersonName(event, isEn)
            if (personName.isNotEmpty()) return personName
            if (isStranger(event)) return if (isEn) "Stranger" else "Người lạ"
            return if (isEn) "Face" else "Khuôn mặt"
        }

        val plate = d?.plate?.plate ?: d?.plate?.number ?: event.plate
        if (!plate.isNullOrBlank()) {
            return plate.trim()
        }

        if (t0.contains("uniform") || t0.contains("clothing")) {
            return extractUniformLabel(event, isEn)
        }

        if (t0.contains("helmet")) return if (isEn) "No helmet" else "Không đội mũ bảo hiểm"
        if (t0.contains("hairnet")) return if (isEn) "No safety cap" else "Không đội mũ bảo hộ"
        if (t0.contains("face-masked") || t0.contains("no-mask") || t0.contains("mask")) {
            return if (isEn) "No mask" else "Không đeo khẩu trang"
        }
        if (t0.contains("glove")) return if (isEn) "No gloves" else "Không đeo găng tay"
        if (t0.contains("crowd")) return if (isEn) "Crowd" else "Đám đông"
        if (t0.contains("fire") || t0.contains("smoke")) return if (isEn) "Fire / Smoke" else "Cháy / Khói"
        if (t0.contains("parking")) return if (isEn) "Illegal parking" else "Dừng đỗ"
        if (t0.contains("lpr") || t0.contains("plate") || t0.contains("license")) {
            return if (isEn) "License plate" else "Biển số"
        }
        if (t0.contains("vehicle") || t0.contains("object-end")) {
            return if (isEn) "Vehicle" else "Phương tiện"
        }
        if (t0.contains("garbage") || t0.contains("litter")) {
            return if (isEn) "Littering" else "Xả rác"
        }
        if (t0.contains("sidewalk") || t0.contains("encroach")) {
            return if (isEn) "Encroachment" else "Lấn chiếm"
        }
        if (t0.contains("person") || t0.contains("intrusion")) {
            return if (isEn) "Intrusion" else "Xâm nhập"
        }

        return getTypeName(event.type, isEn)
    }

    fun getEventName(event: EventData): String {
        return getEventBadgeLabel(event, isEn = false)
    }

    fun getTypeName(type: String?, isEn: Boolean = false): String {
        if (type == null) return if (isEn) "Unknown event" else "Sự kiện không xác định"
        if (isEn) {
            val enMap = mapOf(
                "logic-person" to "Intrusion detection",
                "sensor-person-camera" to "Intrusion detection",
                "logic-fire" to "Fire & smoke detection",
                "sensor-fire-camera" to "Fire & smoke detection",
                "sensor-smoke-camera" to "Smoke detection",
                "logic-fall-person" to "Fall detection",
                "sensor-fall-person-camera" to "Fall detection",
                "logic-face" to "Face recognition",
                "sensor-face-unknown-camera" to "Stranger detection",
                "sensor-license-plate" to "License plate recognition",
                "logic-uniform" to "Uniform detection",
                "sensor-uniform-camera" to "Uniform detection",
                "sensor-crowd-camera" to "Crowd detection",
                "sensor-violence" to "Violence detection",
                "sensor-smoking" to "Smoking detection",
                "sensor-pose-camera" to "Smoking detection",
                "sensor-phone-usage" to "Phone usage",
                "sensor-phone-camera" to "Phone usage",
                "sensor-vehicle-camera" to "Vehicle recognition",
                "sensor-helmet-camera" to "No helmet violation",
                "sensor-helmet-violation-camera" to "No helmet violation",
                "sensor-parking-camera" to "Illegal parking",
                "sensor-encroachment-camera" to "Sidewalk encroachment",
                "sensor-littering-camera" to "Littering detection",
                "sensor-construction-camera" to "Construction detection",
                "construction-sensor-camera" to "Construction detection",
                "sensor-crossing-tracking" to "Wrong-way driving",
                "sensor-gloves" to "No gloves violation",
                "sensor-hairnet" to "No safety cap",
                "sensor-face-masked" to "No mask violation"
            )
            return enMap[type] ?: type
        }
        val typeMap = mapOf(
            "logic-person" to "Phát hiện xâm nhập",
            "sensor-person-camera" to "Phát hiện xâm nhập",
            "logic-fish-out" to "Phát hiện cá koi nhảy lên bờ",
            "sensor-fish-out-camera" to "Phát hiện cá koi nhảy lên bờ",
            "logic-person-pond" to "Phát hiện người ngã xuống Hồ Koi",
            "sensor-person-pond-camera" to "Phát hiện người ngã xuống Hồ Koi",
            "sensor-person-pond-camera-v2" to "Phát hiện người ngã xuống Hồ Koi",
            "logic-fire" to "Phát hiện Cháy & Khói",
            "sensor-fire-camera" to "Phát hiện Cháy & Khói",
            "sensor-smoke-camera" to "Phát hiện Khói",
            "logic-fall-person" to "Phát hiện người ngã bất thường",
            "sensor-fall-person-camera" to "Phát hiện người ngã bất thường",
            "logic-power" to "Phát hiện mất điện",
            "sensor-person-entry-exit-camera" to "Đếm người vào ra",
            "sensor-person-dwell-camera" to "Phát hiện người ở lâu",
            "logic-face" to "Nhận diện khuôn mặt",
            "sensor-license-plate" to "Nhận diện biển số",
            "sensor-face-unknown-camera" to "Người lạ",
            "logic-uniform" to "Nhận diện trang phục",
            "sensor-object" to "Nhận diện đối tượng",
            "object-begin" to "Nhận diện đối tượng",
            "object-end" to "Nhận diện đối tượng",
            "sensor-object-tracking" to "Theo dõi đối tượng",
            "sensor-gesture-camera" to "Nhận diện cử chỉ",
            "sensor-heatmap-camera" to "Phân tích mật độ",
            "sensor-crowd-camera" to "Phát hiện đông người",
            "sensor-uniform-camera" to "Nhận diện trang phục",
            "sensor-violence" to "Phát hiện đánh nhau",
            "sensor-pose-camera" to "Phát hiện hút thuốc",
            "sensor-smoking" to "Phát hiện hút thuốc",
            "sensor-phone-camera" to "Sử dụng điện thoại",
            "sensor-phone-usage" to "Sử dụng điện thoại",
            "sensor-vehicle-camera" to "Nhận diện phương tiện",
            "sensor-helmet-camera" to "Không đội mũ bảo hiểm",
            "sensor-helmet-violation-camera" to "Không đội mũ bảo hiểm",
            "sensor-parking-camera" to "Phát hiện dừng đỗ xe",
            "sensor-animal-camera" to "Phát hiện động vật",
            "sensor-weapon-camera" to "Phát hiện vũ khí",
            "sensor-absence-tracking-camera" to "Phát hiện rời vị trí",
            "sensor-encroachment-camera" to "Lấn chiếm vỉa hè",
            "sensor-littering-camera" to "Phát hiện xả rác",
            "sensor-construction-camera" to "Phát hiện thi công",
            "construction-sensor-camera" to "Phát hiện thi công",
            "sensor-crossing-tracking" to "Đi ngược chiều",
            "sensor-gloves" to "Không đeo găng tay",
            "sensor-hairnet" to "Không đội mũ bảo hộ",
            "sensor-face-masked" to "Không đeo khẩu trang"
        )
        return typeMap[type] ?: type
    }

    fun parseHexColor(colorStr: String?): Color? {
        if (colorStr.isNullOrBlank()) return null
        val clean = colorStr.trim().removePrefix("#")
        return try {
            when (clean.length) {
                6 -> Color(("FF$clean").toLong(16))
                8 -> {
                    val rrggbb = clean.substring(0, 6)
                    val aa = clean.substring(6, 8)
                    Color(("$aa$rrggbb").toLong(16))
                }
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    fun getAiColor(event: EventData): Color {
        val t0 = event.type.lowercase()
        val d = event.data

        if (isFaceEvent(event)) {
            if (isStranger(event)) {
                return Color(0xFFDC2626) // FACE_UNKNOWN
            }
            val directColor = event.groupColor
                ?: d?.groupColor
                ?: d?.groupColorSnake
                ?: d?.face?.groupColor
                ?: d?.face?.groupColorSnake
            parseHexColor(directColor)?.let { return it }
            return Color(0xFFA07E0E) // AI_MODULE_COLORS.FACE
        }

        if (t0.contains("uniform") || t0.contains("clothing")) {
            return if (isUniformViolation(event)) Color(0xFFEF4444) else Color(0xFF267BE2)
        }

        return getAiColor(event.type)
    }

    fun getAiColor(type: String?): Color {
        val t = type?.lowercase() ?: return Color(0xFF0F81C4)

        return when {
            t.contains("face-unknown") || t.contains("stranger") -> Color(0xFFDC2626)
            t.contains("face-masked") || t.contains("no-mask") || t.contains("no_mask") -> Color(0xFFA855F7)
            t.contains("face") -> Color(0xFFA07E0E)
            t.contains("uniform") || t.contains("clothing") -> Color(0xFF267BE2)
            t.contains("crowd") -> Color(0xFF3B9D0E)
            t.contains("heatmap") -> Color(0xFF6366F1)
            t.contains("lpr") || t.contains("plate") || t.contains("license") -> Color(0xFFEA580C)
            t.contains("fire") || t.contains("smoke") -> Color(0xFFDC2626)
            t.contains("helmet") -> Color(0xFF198D0A)
            t.contains("parking") -> Color(0xFFA50F69)
            t.contains("garbage") || t.contains("dumping") || t.contains("litter") -> Color(0xFF9E8609)
            t.contains("person") || t.contains("intrusion") -> Color(0xFF0A9C47)
            t.contains("vehicle") -> Color(0xFF0284C7)
            t.contains("sidewalk") || t.contains("encroach") -> Color(0xFFA78F08)
            t.contains("construction") -> Color(0xFFB97B0F)
            t.contains("crossing") || t.contains("wrong") -> Color(0xFFE11D48)
            t.contains("violence") || t.contains("fight") -> Color(0xFF7F10AB)
            t.contains("smoking") || t.contains("pose") -> Color(0xFFBB3258)
            t.contains("phone") || t.contains("calling") -> Color(0xFF2F71BC)
            t.contains("fall") -> Color(0xFFAD830D)
            t.contains("glove") -> Color(0xFF940D7B)
            t.contains("hairnet") -> Color(0xFF627E11)
            t.contains("object") || t.contains("tracking") -> Color(0xFF3B82F6)
            else -> Color(0xFF0F81C4)
        }
    }

    /**
     * Cloned from MobileEventCard.vue eventSentenceParts
     */
    fun getEventSentenceParts(
        event: EventData,
        cameraName: String,
        isEn: Boolean = false
    ): EventSentenceParts {
        val rawType = event.type.lowercase()
        val data = event.data
        val badgeLabel = getEventBadgeLabel(event, isEn)
        val badgeColor = getAiColor(event)

        fun formatPrefix(p: String): String {
            if (p.isBlank()) return if (isEn) "Detected event" else "Phát hiện sự kiện"
            val trimmed = p.trim()
            val lower = trimmed.lowercase()
            return if (isEn) {
                if (lower.startsWith("detected ") || lower == "detected") trimmed
                else "Detected ${trimmed.replaceFirstChar { it.lowercase() }}"
            } else {
                if (lower.startsWith("phát hiện ") || lower == "phát hiện") trimmed
                else "Phát hiện ${trimmed.replaceFirstChar { it.lowercase() }}"
            }
        }

        fun createPart(prefix: String, subject: String, color: Color = badgeColor): EventSentenceParts {
            val cleanP = prefix.trim().lowercase()
                .replace(Regex("^phát hiện\\s+"), "")
                .replace(Regex("^detected\\s+"), "")
                .replace(Regex("[/&]"), " ")
                .trim()
            val cleanS = subject.trim().lowercase()
                .replace(Regex("[/&]"), " ")
                .trim()

            var finalPrefix = prefix
            if (cleanP.isNotEmpty() && cleanS.isNotEmpty() && (
                    cleanP == cleanS ||
                    cleanS.startsWith(cleanP) ||
                    cleanP.startsWith(cleanS) ||
                    cleanS.contains(cleanP) ||
                    cleanP.contains(cleanS)
                )
            ) {
                finalPrefix = if (isEn) "Detected" else "Phát hiện"
            }

            return EventSentenceParts(
                prefix = formatPrefix(finalPrefix),
                subject = subject,
                color = color,
                cameraName = cameraName
            )
        }

        // 1. Face / Stranger
        if (isFaceEvent(event)) {
            if (isStranger(event)) {
                return createPart(
                    if (isEn) "Face" else "Khuôn mặt",
                    if (isEn) "Stranger" else "Người lạ",
                    badgeColor
                )
            }
            val personName = extractFacePersonName(event, isEn)
            if (personName.isNotEmpty()) {
                return createPart(
                    if (isEn) "Face" else "Khuôn mặt",
                    personName,
                    badgeColor
                )
            }
            return createPart(
                if (isEn) "Face" else "Khuôn mặt",
                if (isEn) "Face detection" else "Khuôn mặt",
                badgeColor
            )
        }

        // 2. Parking
        if (rawType.contains("parking")) {
            return createPart(
                if (isEn) "Violation" else "Vi phạm",
                if (isEn) "Illegal parking" else "Dừng đỗ sai quy định"
            )
        }

        // 3. LPR / Vehicle
        if (rawType.contains("lpr") || rawType.contains("plate") || rawType.contains("license")) {
            val plate = data?.plate?.plate ?: data?.plate?.number ?: event.plate
            if (!plate.isNullOrBlank()) {
                return createPart(
                    if (isEn) "License plate" else "Biển số xe",
                    plate.trim()
                )
            }
            return createPart(
                if (isEn) "Vehicle" else "Phương tiện",
                if (isEn) "Detected vehicle" else "Phương tiện"
            )
        }

        if (rawType.contains("vehicle")) {
            val plate = data?.plate?.plate ?: data?.plate?.number ?: event.plate
            if (!plate.isNullOrBlank()) {
                return createPart(
                    if (isEn) "Vehicle" else "Phương tiện",
                    plate.trim()
                )
            }
            return createPart(
                if (isEn) "Vehicle" else "Phương tiện",
                if (isEn) "Detected vehicle" else "Phương tiện"
            )
        }

        // 4. Helmet
        if (rawType.contains("helmet")) {
            return createPart(
                if (isEn) "Violation" else "Vi phạm",
                if (isEn) "No helmet" else "Không đội mũ bảo hiểm"
            )
        }

        // 5. Hairnet
        if (rawType.contains("hairnet")) {
            return createPart(
                if (isEn) "Safety violation" else "Vi phạm bảo hộ",
                if (isEn) "No safety cap" else "Không đội mũ bảo hộ"
            )
        }

        // 6. Mask
        if (rawType.contains("mask")) {
            return createPart(
                if (isEn) "Health violation" else "Vi phạm y tế",
                if (isEn) "No mask" else "Không đeo khẩu trang"
            )
        }

        // 7. Gloves
        if (rawType.contains("glove")) {
            return createPart(
                if (isEn) "Safety violation" else "Vi phạm bảo hộ",
                if (isEn) "No gloves" else "Không đeo găng tay"
            )
        }

        // 8. Uniform
        if (rawType.contains("uniform") || rawType.contains("clothing")) {
            val subj = extractUniformLabel(event, isEn)
            return createPart(
                if (isEn) "Uniform" else "Đồng phục",
                subj
            )
        }

        // 9. Intrusion
        if (rawType.contains("intrusion") || rawType == "logic-person" || rawType == "sensor-person-camera") {
            return createPart(
                if (isEn) "Detected" else "Phát hiện",
                if (isEn) "Intrusion" else "Xâm nhập"
            )
        }

        // 10. Crowd
        if (rawType.contains("crowd")) {
            return createPart(
                if (isEn) "Detected" else "Phát hiện",
                if (isEn) "Crowd gathering" else "Tụ tập đám đông"
            )
        }

        // 11. Fire & Smoke
        if (rawType.contains("fire") || rawType.contains("smoke")) {
            val isSmokeOnly = rawType.contains("smoke") && !rawType.contains("fire")
            return createPart(
                if (isEn) "Detected" else "Phát hiện",
                if (isSmokeOnly) (if (isEn) "Smoke" else "Khói") else (if (isEn) "Fire & smoke" else "Cháy & khói")
            )
        }

        // 12. Garbage / Littering
        if (rawType.contains("garbage") || rawType.contains("litter")) {
            return createPart(
                if (isEn) "Detected" else "Phát hiện",
                if (isEn) "Littering" else "Xả rác bừa bãi"
            )
        }

        // 13. Sidewalk encroachment
        if (rawType.contains("sidewalk") || rawType.contains("encroach")) {
            return createPart(
                if (isEn) "Detected" else "Phát hiện",
                if (isEn) "Sidewalk encroachment" else "Lấn chiếm vỉa hè"
            )
        }

        // 14. Wrong-way
        if (rawType.contains("wrong") || rawType.contains("crossing")) {
            return createPart(
                if (isEn) "Violation" else "Vi phạm",
                if (isEn) "Wrong-way driving" else "Đi ngược chiều"
            )
        }

        // 15. Violence
        if (rawType.contains("violence") || rawType.contains("fight")) {
            return createPart(
                if (isEn) "Detected" else "Phát hiện",
                if (isEn) "Violent activity" else "Hành vi bạo lực"
            )
        }

        // 16. Fall
        if (rawType.contains("fall")) {
            return createPart(
                if (isEn) "Detected" else "Phát hiện",
                if (isEn) "Fall" else "Người té ngã"
            )
        }

        // 17. Smoking
        if (rawType.contains("smoking") || rawType.contains("pose")) {
            return createPart(
                if (isEn) "Detected" else "Phát hiện",
                if (isEn) "Smoking" else "Hút thuốc lá"
            )
        }

        // 18. Phone
        if (rawType.contains("phone")) {
            return createPart(
                if (isEn) "Detected" else "Phát hiện",
                if (isEn) "Mobile phone usage" else "Sử dụng điện thoại"
            )
        }

        // 19. Construction
        if (rawType.contains("construct")) {
            return createPart(
                if (isEn) "Detected" else "Phát hiện",
                if (isEn) "Construction activity" else "Thi công"
            )
        }

        val cleanLabel = badgeLabel
            .replace(Regex("^Phát hiện\\s+", RegexOption.IGNORE_CASE), "")
            .replace(Regex("^Detected\\s+", RegexOption.IGNORE_CASE), "")
            .trim()
            .replaceFirstChar { it.uppercase() }

        return createPart(
            if (isEn) "Detected" else "Phát hiện",
            cleanLabel.ifEmpty { if (isEn) "AI event" else "Sự kiện AI" }
        )
    }

    fun formatRelativeTime(timestampSeconds: Double, isEn: Boolean = false): String {
        if (timestampSeconds <= 0) return ""
        val timeMs = if (timestampSeconds < 1e11) (timestampSeconds * 1000).toLong() else timestampSeconds.toLong()
        val diffSec = ((System.currentTimeMillis() - timeMs) / 1000).coerceAtLeast(0)
        if (diffSec < 60) return if (isEn) "Just now" else "Vừa xong"
        val diffMin = diffSec / 60
        if (diffMin < 60) return if (isEn) "${diffMin}m ago" else "$diffMin phút trước"
        val diffHour = diffMin / 60
        if (diffHour < 24) return if (isEn) "${diffHour}h ago" else "$diffHour giờ trước"
        val diffDay = diffHour / 24
        if (diffDay < 30) return if (isEn) "${diffDay}d ago" else "$diffDay ngày trước"
        val diffMonth = diffDay / 30
        if (diffMonth < 12) return if (isEn) "${diffMonth}mo ago" else "$diffMonth tháng trước"
        val diffYear = diffDay / 365
        return if (isEn) "${diffYear}y ago" else "$diffYear năm trước"
    }

    fun getSnapshotUrl(event: EventData, baseDomain: String): String {
        val domain = baseDomain.trimEnd('/')
        val data = event.data ?: return ""
        val containerId = data.containerId ?: "0"
        val t0 = event.type.lowercase()

        val imageFile = when {
            isFaceEvent(event) -> {
                data.face?.cropImage
                    ?: data.faceCrop
                    ?: data.faceCropImageSnake
                    ?: data.faceCropImage
                    ?: data.cropImage
                    ?: data.image
            }
            t0.contains("lpr") || t0.contains("plate") || data.plate != null -> {
                data.plate?.cropImage ?: data.cropImage ?: data.image
            }
            t0.contains("person") || t0.contains("intrusion") -> {
                data.cropImage ?: data.person?.cropImage ?: data.image
            }
            t0.contains("helmet") -> {
                data.image ?: data.fullImage ?: data.hat?.image ?: data.hat?.cropImage ?: data.cropImage
            }
            t0.contains("uniform") || data.uniform != null -> {
                data.image ?: data.fullImage ?: data.uniform?.image ?: data.uniform?.cropImage ?: data.cropImage
            }
            data.vehicle?.cropImage != null || data.objectData?.image != null -> {
                data.vehicle?.cropImage ?: data.objectData?.image ?: data.image
            }
            else -> data.image ?: data.cropImage
        } ?: return ""

        if (imageFile.startsWith("http://") || imageFile.startsWith("https://")) return imageFile
        if (imageFile.startsWith("/")) return "$domain$imageFile"
        return "$domain/Data/api/Data/Media/${event.serviceId}/$containerId/$imageFile"
    }

    fun getFullImageUrl(event: EventData, baseDomain: String): String {
        val domain = baseDomain.trimEnd('/')
        val data = event.data ?: return ""
        val containerId = data.containerId ?: "0"
        val imageFile = data.image ?: data.fullImage ?: data.cropImage ?: return ""
        if (imageFile.startsWith("http://") || imageFile.startsWith("https://")) return imageFile
        if (imageFile.startsWith("/")) return "$domain$imageFile"
        return "$domain/Data/api/Data/Media/${event.serviceId}/$containerId/$imageFile"
    }

    fun getFaceCropUrl(event: EventData, baseDomain: String): String? {
        if (!isFaceEvent(event)) return null
        val domain = baseDomain.trimEnd('/')
        val data = event.data ?: return null
        val containerId = data.containerId ?: "0"
        val cropFile = data.face?.cropImage
            ?: data.faceCrop
            ?: data.faceCropImageSnake
            ?: data.faceCropImage
            ?: data.cropImage
            ?: return null
        if (cropFile == data.image) return null
        if (cropFile.startsWith("http://") || cropFile.startsWith("https://")) return cropFile
        if (cropFile.startsWith("/")) return "$domain$cropFile"
        return "$domain/Data/api/Data/Media/${event.serviceId}/$containerId/$cropFile"
    }

    fun getVideoUrl(event: EventData, baseDomain: String): String? {
        val domain = baseDomain.trimEnd('/')
        val data = event.data ?: return null
        val containerId = data.containerId ?: "0"
        val videoFile = data.video ?: data.videoUrl ?: data.focusVideo ?: return null
        if (videoFile.isBlank()) return null
        if (videoFile.startsWith("http://") || videoFile.startsWith("https://")) return videoFile
        if (videoFile.startsWith("/")) return "$domain$videoFile"
        return "$domain/Data/api/Data/Media/${event.serviceId}/$containerId/$videoFile"
    }
}
