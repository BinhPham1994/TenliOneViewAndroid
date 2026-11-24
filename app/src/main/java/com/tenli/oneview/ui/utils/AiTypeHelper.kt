package com.tenli.oneview.ui.utils

import android.util.LruCache
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.tenli.oneview.model.network.AiModuleConfigItem
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.model.network.ModulesConfigModel
import com.tenli.oneview.model.network.UserNotificationSettingsModel
import com.tenli.oneview.model.network.UserPermissions
import com.tenli.oneview.ui.theme.BrandActiveNavDark
import com.tenli.oneview.ui.theme.ErrorRed
import com.tenli.oneview.ui.theme.ErrorRedDeep
import com.tenli.oneview.ui.theme.IndigoAccent
import com.tenli.oneview.ui.theme.InfoSkyDeep
import com.tenli.oneview.ui.theme.OrangeDeep
import com.tenli.oneview.ui.theme.PurpleAccent

// Bảng màu cố định cho từng module AI (đồng bộ với AI_MODULE_COLORS trong bản PWA)
private val AiColorFace = Color(0xFFA07E0E)
private val AiColorUniform = Color(0xFF267BE2)
private val AiColorCrowd = Color(0xFF3B9D0E)
private val AiColorHelmet = Color(0xFF198D0A)
private val AiColorParking = Color(0xFFA50F69)
private val AiColorGarbage = Color(0xFF9E8609)
private val AiColorIntrusion = Color(0xFF0A9C47)
private val AiColorPersonIn = Color(0xFF0D9488)
private val AiColorPersonOut = Color(0xFFD97706)
private val AiColorEncroachment = Color(0xFFA78F08)
private val AiColorConstruction = Color(0xFFB97B0F)
private val AiColorWrongWay = Color(0xFFE11D48)
private val AiColorViolence = Color(0xFF7F10AB)
private val AiColorSmoking = Color(0xFFBB3258)
private val AiColorPhone = Color(0xFF2F71BC)
private val AiColorFall = Color(0xFFAD830D)
private val AiColorGloves = Color(0xFF940D7B)
private val AiColorHairnet = Color(0xFF627E11)
private val AiColorFlooding = Color(0xFF0284C7)
private val AiColorDefault = Color(0xFF0F81C4)

@Immutable
data class EventSentenceParts(
    val prefix: String,
    val subject: String,
    val color: Color,
    val cameraName: String
)

@Immutable
data class AiModuleOption(
    val key: String,
    val sensorType: String,
    val labelVi: String,
    val labelEn: String,
    val eventTypes: List<String> = listOf(sensorType)
)

object AiTypeHelper {

    private data class PermissionSnapshot(
        val currentModulesConfig: ModulesConfigModel?,
        val isProjectModuleRestricted: Boolean,
        val activeModules: List<AiModuleOption>,
        val enabledAiTypesLower: Set<String>,
        val aliasLookup: Map<String, String>
    )

    private val REGEX_PREFIX_VI = Regex("^phát hiện\\s+", RegexOption.IGNORE_CASE)
    private val REGEX_PREFIX_EN = Regex("^detected\\s+", RegexOption.IGNORE_CASE)
    private val REGEX_SLASH_AMP = Regex("[/&]")

    private val hexColorCache = LruCache<String, Color>(128)

    val defaultModules: List<AiModuleOption> = listOf(
        AiModuleOption(
            "FaceID",
            "logic-face",
            "Nhận diện khuôn mặt",
            "Face recognition",
            listOf("logic-face", "sensor-face-unknown-camera")
        ),
        AiModuleOption(
            "Uniform",
            "sensor-uniform-camera",
            "Nhận diện trang phục",
            "Uniform detection",
            listOf("sensor-uniform-camera", "logic-uniform")
        ),
        AiModuleOption(
            "Crowd",
            "sensor-crowd-camera",
            "Phát hiện đông người",
            "Crowd detection",
            listOf("sensor-crowd-camera")
        ),
        AiModuleOption(
            "LPR",
            "sensor-license-plate",
            "Nhận diện biển số",
            "License plate recognition",
            listOf("sensor-license-plate")
        ),
        AiModuleOption(
            "FireSmoke",
            "sensor-fire-camera",
            "Phát hiện Cháy & Khói",
            "Fire & smoke detection",
            listOf("sensor-fire-camera", "sensor-smoke-camera", "logic-fire")
        ),
        AiModuleOption(
            "Helmet",
            "sensor-helmet-camera",
            "Không đội mũ bảo hiểm",
            "No helmet violation",
            listOf("sensor-helmet-violation-camera", "sensor-helmet-camera")
        ),
        AiModuleOption(
            "Parking",
            "sensor-parking-camera",
            "Phát hiện dừng đỗ xe",
            "Illegal parking",
            listOf("sensor-parking-camera")
        ),
        AiModuleOption(
            "Garbage",
            "sensor-littering-camera",
            "Phát hiện xả rác",
            "Littering detection",
            listOf("sensor-littering-camera")
        ),
        AiModuleOption(
            "Intrusion",
            "sensor-person-camera",
            "Phát hiện xâm nhập",
            "Intrusion detection",
            listOf("sensor-person-camera", "logic-person")
        ),
        AiModuleOption(
            "VehicleRecognition",
            "sensor-vehicle-camera",
            "Nhận diện phương tiện",
            "Vehicle recognition",
            listOf("sensor-vehicle-camera")
        ),
        AiModuleOption(
            "SidewalkEncroachment",
            "sensor-encroachment-camera",
            "Lấn chiếm vỉa hè",
            "Sidewalk encroachment",
            listOf("sensor-encroachment-camera")
        ),
        AiModuleOption(
            "ConstructionDetection",
            "sensor-construction-camera",
            "Phát hiện thi công",
            "Construction detection",
            listOf("sensor-construction-camera", "construction-sensor-camera")
        ),
        AiModuleOption(
            "WrongWayDetection",
            "sensor-crossing-tracking",
            "Đi ngược chiều",
            "Wrong-way driving",
            listOf("sensor-crossing-tracking")
        ),
        AiModuleOption(
            "Violence",
            "sensor-violence",
            "Phát hiện đánh nhau",
            "Violence detection",
            listOf("sensor-violence")
        ),
        AiModuleOption(
            "Smoking",
            "sensor-smoking",
            "Phát hiện hút thuốc",
            "Smoking detection",
            listOf("sensor-smoking", "sensor-pose-camera")
        ),
        AiModuleOption(
            "Phone",
            "sensor-phone-usage",
            "Sử dụng điện thoại",
            "Phone usage",
            listOf("sensor-phone-usage", "sensor-phone-camera")
        ),
        AiModuleOption(
            "FallDetection",
            "sensor-fall-person-camera",
            "Phát hiện người ngã",
            "Fall detection",
            listOf("sensor-fall-person-camera", "logic-fall-person")
        ),
        AiModuleOption(
            "ObjectTracking",
            "sensor-object-tracking",
            "Theo dõi đối tượng",
            "Object tracking",
            listOf("sensor-object-tracking", "sensor-object", "object-begin", "object-end")
        ),
        AiModuleOption(
            "GlovesDetection",
            "sensor-gloves",
            "Không đeo găng tay",
            "No gloves violation",
            listOf("sensor-gloves")
        ),
        AiModuleOption(
            "HairnetDetection",
            "sensor-hairnet",
            "Không đội mũ bảo hộ",
            "No hairnet violation",
            listOf("sensor-hairnet")
        ),
        AiModuleOption(
            "NoMaskDetection",
            "sensor-face-masked",
            "Không đeo khẩu trang",
            "No mask violation",
            listOf("sensor-face-masked")
        ),
        AiModuleOption(
            "FloodingDetection",
            "flooding",
            "Phát hiện ngập lụt",
            "Flooding detection",
            listOf("flooding", "sensor-flooding-camera")
        )
    )

    private val heatmapModule = AiModuleOption(
        "Heatmap",
        "sensor-heatmap-camera",
        "Phân tích mật độ",
        "Heatmap analysis",
        listOf("sensor-heatmap-camera")
    )

    private val catalogModules: List<AiModuleOption> = defaultModules + heatmapModule

    private val DEFAULT_MODULE_NAME_MAP: Map<String, String> = mapOf(
        "face" to "FaceID",
        "lpr" to "LPR",
        "heatmap" to "Heatmap",
        "garbage" to "Garbage",
        "vehicle" to "VehicleRecognition",
        "sidewalk" to "SidewalkEncroachment",
        "intrusion" to "Intrusion",
        "helmet" to "Helmet",
        "parking" to "Parking",
        "fire_smoke" to "FireSmoke",
        "crowd" to "Crowd",
        "uniform" to "Uniform",
        "construction" to "ConstructionDetection",
        "wrong_way" to "WrongWayDetection",
        "violence" to "Violence",
        "smoking" to "Smoking",
        "phone" to "Phone",
        "fall" to "FallDetection",
        "object_tracking" to "ObjectTracking",
        "gloves" to "GlovesDetection",
        "hairnet" to "HairnetDetection",
        "no_mask" to "NoMaskDetection",
        "flooding" to "FloodingDetection"
    )

    private val defaultEnabledAiTypes: Set<String> = (
        defaultModules.flatMap { it.eventTypes } +
            listOf(
                "sensor-absence-tracking-camera",
                "sensor-person-in-out",
                "sensor-person-in",
                "sensor-person-out"
            )
        ).toSet()

    /**
     * Các mã là type bài AI (monitor/task type) dùng cho đếm/cấu hình bài AI đang chạy,
     * KHÔNG phải type sự kiện (event type) — không đưa vào danh sách lọc loại sự kiện.
     * Ví dụ: bài AI `sensor-person-in-out` phát sinh 2 type sự kiện riêng là `sensor-person-in` và `sensor-person-out`.
     */
    private val NON_EVENT_AI_TASK_TYPES: Set<String> = setOf(
        "sensor-person-in-out",
        "sensor-person-in-out-camera"
    )

    private val defaultEnabledAiTypesLower: Set<String> =
        defaultEnabledAiTypes.mapTo(HashSet(defaultEnabledAiTypes.size)) { it.lowercase() }

    private val defaultAliasLookup: Map<String, String> = buildAliasLookup(null)

    @Volatile
    private var permissionSnapshot: PermissionSnapshot = PermissionSnapshot(
        currentModulesConfig = null,
        isProjectModuleRestricted = false,
        activeModules = defaultModules,
        enabledAiTypesLower = defaultEnabledAiTypesLower,
        aliasLookup = defaultAliasLookup
    )

    val currentModulesConfig: ModulesConfigModel?
        get() = permissionSnapshot.currentModulesConfig

    val isProjectModuleRestricted: Boolean
        get() = permissionSnapshot.isProjectModuleRestricted

    val activeModules: List<AiModuleOption>
        get() = permissionSnapshot.activeModules

    private fun buildAliasLookup(config: ModulesConfigModel?): Map<String, String> {
        val map = HashMap<String, String>(DEFAULT_MODULE_NAME_MAP.size * 2)
        for ((k, v) in DEFAULT_MODULE_NAME_MAP) {
            map[k.lowercase()] = v.lowercase()
        }
        config?.moduleNameMap?.forEach { (k, v) ->
            val cleanK = k.trim().lowercase()
            val cleanV = v.trim().lowercase()
            if (cleanK.isNotEmpty() && cleanV.isNotEmpty()) {
                map[cleanK] = cleanV
            }
        }
        return map
    }

    /**
     * Xác định danh sách bài AI cấp dự án ("Full bài AI"):
     * - Nếu backend có `modules-config.json` hợp lệ: chỉ lấy các bài AI được bật trong file JSON.
     * - Nếu backend chưa hỗ trợ `modules-config.json` (`config == null`): giữ nguyên toàn bộ `defaultModules` như hiện tại.
     */
    private fun resolveProjectModules(
        config: ModulesConfigModel?,
        aliasLookup: Map<String, String>
    ): List<AiModuleOption> {
        val validConfig = config?.takeIf { it.isValidConfig() } ?: return defaultModules

        val rawModulesMap = LinkedHashMap<String, AiModuleConfigItem>()
        validConfig.tabs?.business?.modules?.forEach { (k, v) ->
            rawModulesMap[k.trim().lowercase()] = v
        }
        validConfig.aiModules?.forEach { (k, v) ->
            rawModulesMap[k.trim().lowercase()] = v
        }

        val enabledSetLower: Set<String>? = validConfig.enabledAiModules?.let { list ->
            val set = HashSet<String>(list.size * 2)
            for (raw in list) {
                val lower = raw.trim().lowercase()
                if (lower.isNotEmpty()) {
                    set.add(lower)
                    aliasLookup[lower]?.let { set.add(it) }
                }
            }
            set
        }

        val disabledSetLower: Set<String> = validConfig.disabledAiModules?.let { list ->
            val set = HashSet<String>(list.size * 2)
            for (raw in list) {
                val lower = raw.trim().lowercase()
                if (lower.isNotEmpty()) {
                    set.add(lower)
                    aliasLookup[lower]?.let { set.add(it) }
                }
            }
            set
        } ?: emptySet()

        return catalogModules.mapNotNull { mod ->
            val keyLower = mod.key.lowercase()
            val sensorLower = mod.sensorType.lowercase()
            val item = rawModulesMap[keyLower]
                ?: rawModulesMap.values.firstOrNull { it.sensorType?.trim()?.equals(mod.sensorType, ignoreCase = true) == true }

            val isExplicitlyDisabled = keyLower in disabledSetLower ||
                sensorLower in disabledSetLower ||
                item?.enabled == false

            val isEnabled = when {
                isExplicitlyDisabled -> false
                enabledSetLower != null -> {
                    keyLower in enabledSetLower ||
                        sensorLower in enabledSetLower ||
                        item?.enabled == true
                }
                rawModulesMap.isNotEmpty() -> item?.enabled == true
                else -> mod in defaultModules
            }

            if (!isEnabled) return@mapNotNull null

            val mergedEventTypes = LinkedHashSet<String>()
            mergedEventTypes.addAll(mod.eventTypes)
            item?.sensorType?.trim()?.takeIf { it.isNotEmpty() }?.let { mergedEventTypes.add(it) }
            item?.eventTypes?.forEach { et ->
                val clean = et.trim()
                if (clean.isNotEmpty()) mergedEventTypes.add(clean)
            }

            if (mod.key.equals("FaceID", ignoreCase = true) && item?.enableStranger == false) {
                mergedEventTypes.removeAll { it.equals("sensor-face-unknown-camera", ignoreCase = true) }
            }

            mod.copy(eventTypes = mergedEventTypes.toList())
        }
    }

    @Synchronized
    fun updateModulesConfig(config: ModulesConfigModel?) {
        val validConfig = config?.takeIf { it.isValidConfig() }
        val current = permissionSnapshot
        permissionSnapshot = current.copy(
            currentModulesConfig = validConfig,
            aliasLookup = if (validConfig == null) defaultAliasLookup else buildAliasLookup(validConfig)
        )
    }

    @Synchronized
    fun applyPermissions(
        isAdmin: Boolean,
        permissions: UserPermissions?,
        modulesConfig: ModulesConfigModel? = currentModulesConfig
    ) {
        val validConfig = modulesConfig?.takeIf { it.isValidConfig() }
        val aliasLookup = if (validConfig == null) defaultAliasLookup else buildAliasLookup(validConfig)
        val projectModules = resolveProjectModules(validConfig, aliasLookup)
        val restricted = validConfig != null && projectModules.size < defaultModules.size

        if (!isAdmin && permissions == null) {
            permissionSnapshot = PermissionSnapshot(
                currentModulesConfig = validConfig,
                isProjectModuleRestricted = restricted,
                activeModules = emptyList(),
                enabledAiTypesLower = emptySet(),
                aliasLookup = aliasLookup
            )
            return
        }

        val projectEnabledTypesLower: Set<String> = if (validConfig == null) {
            defaultEnabledAiTypesLower
        } else {
            val set = HashSet<String>()
            projectModules.forEach { mod ->
                mod.eventTypes.forEach { et ->
                    val lower = et.trim().lowercase()
                    if (lower.isNotEmpty()) set.add(lower)
                }
            }
            val faceConfig = validConfig.aiModules?.entries
                ?.firstOrNull { it.key.equals("FaceID", ignoreCase = true) }
                ?.value
                ?: validConfig.tabs?.business?.modules?.entries
                    ?.firstOrNull { it.key.equals("FaceID", ignoreCase = true) }
                    ?.value
            val disableStranger = faceConfig?.enableStranger == false
            validConfig.enabledSensorTypes?.forEach { st ->
                val cleanLower = st.trim().lowercase()
                if (cleanLower.isNotEmpty() && (!disableStranger || cleanLower != "sensor-face-unknown-camera")) {
                    set.add(cleanLower)
                }
            }
            set
        }

        if (isAdmin) {
            permissionSnapshot = PermissionSnapshot(
                currentModulesConfig = validConfig,
                isProjectModuleRestricted = restricted,
                activeModules = projectModules,
                enabledAiTypesLower = projectEnabledTypesLower,
                aliasLookup = aliasLookup
            )
            return
        }

        val perms = permissions!!
        val aiPerms = when {
            perms.aiEvents?.mode == "custom" -> perms.aiEvents
            perms.events?.mode == "custom" -> perms.events
            else -> null
        }

        if (aiPerms != null && aiPerms.mode == "custom") {
            val allowedLower = HashSet<String>()
            (aiPerms.list ?: emptyList()).forEach { raw ->
                val lower = raw.trim().lowercase()
                if (lower.isNotEmpty()) {
                    allowedLower.add(lower)
                    aliasLookup[lower]?.let { allowedLower.add(it) }
                }
            }

            val filteredModules = projectModules.filter { mod ->
                mod.key.lowercase() in allowedLower ||
                    mod.sensorType.lowercase() in allowedLower ||
                    mod.eventTypes.any { et -> et.lowercase() in allowedLower }
            }
            val filteredEnabledLower = HashSet<String>()
            filteredModules.forEach { mod ->
                mod.eventTypes.forEach { et ->
                    val lower = et.trim().lowercase()
                    if (lower.isNotEmpty()) filteredEnabledLower.add(lower)
                }
            }
            permissionSnapshot = PermissionSnapshot(
                currentModulesConfig = validConfig,
                isProjectModuleRestricted = restricted,
                activeModules = filteredModules,
                enabledAiTypesLower = filteredEnabledLower,
                aliasLookup = aliasLookup
            )
        } else {
            permissionSnapshot = PermissionSnapshot(
                currentModulesConfig = validConfig,
                isProjectModuleRestricted = restricted,
                activeModules = projectModules,
                enabledAiTypesLower = projectEnabledTypesLower,
                aliasLookup = aliasLookup
            )
        }
    }

    @Synchronized
    fun resetPermissions() {
        permissionSnapshot = PermissionSnapshot(
            currentModulesConfig = null,
            isProjectModuleRestricted = false,
            activeModules = emptyList(),
            enabledAiTypesLower = emptySet(),
            aliasLookup = defaultAliasLookup
        )
        hexColorCache.evictAll()
    }

    private val TYPE_NAME_EN_MAP = mapOf(
        "logic-person" to "Intrusion detection",
        "sensor-person-camera" to "Intrusion detection",
        "sensor-person-in" to "Person entry detection",
        "sensor-person-in-camera" to "Person entry detection",
        "sensor-person-out" to "Person exit detection",
        "sensor-person-out-camera" to "Person exit detection",
        "sensor-absence-tracking-camera" to "Absence detection",
        "sensor-person-entry-exit-camera" to "People entry & exit counting",
        "sensor-person-dwell-camera" to "Dwell time detection",
        "sensor-gesture-camera" to "Gesture recognition",
        "sensor-heatmap-camera" to "Heatmap analysis",
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
        "sensor-face-masked" to "No mask violation",
        "flooding" to "Flooding detection",
        "sensor-flooding-camera" to "Flooding detection"
    )

    private val TYPE_NAME_VI_MAP = mapOf(
        "logic-person" to "Phát hiện xâm nhập",
        "sensor-person-camera" to "Phát hiện xâm nhập",
        "sensor-person-in" to "Phát hiện người vào",
        "sensor-person-in-camera" to "Phát hiện người vào",
        "sensor-person-out" to "Phát hiện người ra",
        "sensor-person-out-camera" to "Phát hiện người ra",
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
        "sensor-face-masked" to "Không đeo khẩu trang",
        "flooding" to "Phát hiện ngập lụt",
        "sensor-flooding-camera" to "Phát hiện ngập lụt"
    )

    fun isAiTypeEnabled(type: String?): Boolean {
        if (type.isNullOrBlank()) return false
        val t = type.trim().lowercase()
        if (t == "camera-online" || t == "camera-offline") return false
        return permissionSnapshot.enabledAiTypesLower.contains(t)
    }

    /**
     * Kiểm tra một sensorType có phải chỉ là type bài AI (không phải type sự kiện thực tế) hay không:
     * - Khớp danh sách task-only đã biết (vd. `sensor-person-in-out`), HOẶC
     * - Trong `modules-config.json`, bài AI khai báo `sensorType` riêng nhưng `eventTypes` khác rỗng và không chứa `sensorType` đó.
     */
    fun isNonEventTaskType(type: String?, config: ModulesConfigModel? = currentModulesConfig): Boolean {
        val lower = type?.trim()?.lowercase().orEmpty()
        if (lower.isEmpty()) return false
        if (lower in NON_EVENT_AI_TASK_TYPES) return true

        val moduleItems = buildList {
            config?.tabs?.business?.modules?.values?.let { addAll(it) }
            config?.aiModules?.values?.let { addAll(it) }
        }
        val declaredAsEventType = moduleItems.any { item ->
            item.eventTypes?.any { it.trim().equals(lower, ignoreCase = true) } == true
        }
        if (declaredAsEventType) return false

        return moduleItems.any { item ->
            val st = item.sensorType?.trim()?.lowercase()
            val ets = item.eventTypes?.map { it.trim().lowercase() }?.filter { it.isNotEmpty() }.orEmpty()
            st == lower && ets.isNotEmpty() && lower !in ets
        }
    }

    fun getAiEventTypes(aiType: String): List<String> {
        val trimmed = aiType.trim()
        if (trimmed.isEmpty()) return emptyList()
        val snap = permissionSnapshot
        val alias = snap.aliasLookup[trimmed.lowercase()] ?: trimmed
        val matched = snap.activeModules.find { mod ->
            mod.key.equals(trimmed, ignoreCase = true) ||
                mod.key.equals(alias, ignoreCase = true) ||
                mod.sensorType.equals(trimmed, ignoreCase = true) ||
                mod.eventTypes.any { et -> et.equals(trimmed, ignoreCase = true) }
        } ?: catalogModules.find { mod ->
            mod.key.equals(trimmed, ignoreCase = true) ||
                mod.key.equals(alias, ignoreCase = true) ||
                mod.sensorType.equals(trimmed, ignoreCase = true) ||
                mod.eventTypes.any { et -> et.equals(trimmed, ignoreCase = true) }
        }
        if (matched != null) {
            val enabled = matched.eventTypes.filter { snap.enabledAiTypesLower.contains(it.trim().lowercase()) }
            return (enabled.ifEmpty { matched.eventTypes }).distinct()
        }
        return when (trimmed) {
            "logic-face", "FaceID", "face" -> listOf("logic-face", "sensor-face-unknown-camera")
            "sensor-fire-camera", "FireSmoke", "fire_smoke" -> listOf("sensor-fire-camera", "sensor-smoke-camera", "logic-fire")
            "sensor-helmet-camera", "Helmet", "helmet" -> listOf("sensor-helmet-violation-camera", "sensor-helmet-camera")
            "sensor-person-camera", "Intrusion", "intrusion" -> listOf("sensor-person-camera", "logic-person")
            "sensor-person-in-out", "sensor-person-in-out-camera" ->
                listOf("sensor-person-in", "sensor-person-out")
                    .filter { snap.enabledAiTypesLower.contains(it.lowercase()) }
                    .ifEmpty { listOf("sensor-person-in", "sensor-person-out") }
            else -> listOf(trimmed)
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

    /**
     * Trả về nhãn đối tượng / bài AI đồng bộ 100% giữa chế độ List (`sentenceParts.subject`) và chế độ Grid.
     */
    fun getEventBadgeLabel(event: EventData, isEn: Boolean = false): String =
        getEventSentenceParts(event, "", isEn).subject

    fun getEventName(event: EventData): String = getEventBadgeLabel(event, isEn = false)

    fun getTypeName(type: String?, isEn: Boolean = false): String {
        if (type == null) return if (isEn) "Unknown event" else "Sự kiện không xác định"
        val map = if (isEn) TYPE_NAME_EN_MAP else TYPE_NAME_VI_MAP
        return map[type] ?: map[type.lowercase()] ?: type
    }

    fun parseHexColor(colorStr: String?): Color? {
        if (colorStr.isNullOrBlank()) return null
        val clean = colorStr.trim().removePrefix("#")
        if (clean.length != 6 && clean.length != 8) return null
        hexColorCache.get(clean)?.let { return it }
        return try {
            val parsed = when (clean.length) {
                6 -> Color(("FF$clean").toLong(16))
                8 -> {
                    val rrggbb = clean.substring(0, 6)
                    val aa = clean.substring(6, 8)
                    Color(("$aa$rrggbb").toLong(16))
                }
                else -> return null
            }
            hexColorCache.put(clean, parsed)
            parsed
        } catch (_: Exception) {
            null
        }
    }

    fun getAiColor(event: EventData): Color {
        val t0 = event.type.lowercase()
        val d = event.data

        if (isFaceEvent(event)) {
            if (isStranger(event)) {
                return ErrorRedDeep // FACE_UNKNOWN
            }
            val directColor = event.groupColor
                ?: d?.groupColor
                ?: d?.groupColorSnake
                ?: d?.face?.groupColor
                ?: d?.face?.groupColorSnake
            parseHexColor(directColor)?.let { return it }
            return AiColorFace
        }

        if (t0.contains("uniform") || t0.contains("clothing")) {
            return if (isUniformViolation(event)) ErrorRed else AiColorUniform
        }

        return getAiColor(event.type)
    }

    fun getAiColor(type: String?): Color {
        val t = type?.lowercase() ?: return AiColorDefault

        return when {
            t.contains("face-unknown") || t.contains("stranger") -> ErrorRedDeep
            t.contains("face-masked") || t.contains("no-mask") || t.contains("no_mask") -> PurpleAccent
            t.contains("face") -> AiColorFace
            t.contains("uniform") || t.contains("clothing") -> AiColorUniform
            t.contains("crowd") -> AiColorCrowd
            t.contains("heatmap") -> IndigoAccent
            t.contains("lpr") || t.contains("plate") || t.contains("license") -> OrangeDeep
            t.contains("fire") || t.contains("smoke") -> ErrorRedDeep
            t.contains("helmet") -> AiColorHelmet
            t.contains("parking") -> AiColorParking
            t.contains("garbage") || t.contains("dumping") || t.contains("litter") -> AiColorGarbage
            t.contains("person-in") -> AiColorPersonIn
            t.contains("person-out") -> AiColorPersonOut
            t.contains("fall") -> AiColorFall
            t.contains("absence") -> BrandActiveNavDark
            t.contains("person") || t.contains("intrusion") -> AiColorIntrusion
            t.contains("vehicle") -> InfoSkyDeep
            t.contains("sidewalk") || t.contains("encroach") -> AiColorEncroachment
            t.contains("construction") -> AiColorConstruction
            t.contains("crossing") || t.contains("wrong") -> AiColorWrongWay
            t.contains("violence") || t.contains("fight") -> AiColorViolence
            t.contains("smoking") || t.contains("pose") -> AiColorSmoking
            t.contains("phone") || t.contains("calling") -> AiColorPhone
            t.contains("glove") -> AiColorGloves
            t.contains("hairnet") -> AiColorHairnet
            t.contains("flood") -> AiColorFlooding
            t.contains("object") || t.contains("tracking") -> BrandActiveNavDark
            else -> AiColorDefault
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
                .replace(REGEX_PREFIX_VI, "")
                .replace(REGEX_PREFIX_EN, "")
                .replace(REGEX_SLASH_AMP, " ")
                .trim()
            val cleanS = subject.trim().lowercase()
                .replace(REGEX_SLASH_AMP, " ")
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

        if (rawType.contains("parking")) {
            return createPart(
                if (isEn) "Violation" else "Vi phạm",
                if (isEn) "Illegal parking" else "Dừng đỗ sai quy định"
            )
        }

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

        if (rawType.contains("helmet")) {
            return createPart(
                if (isEn) "Violation" else "Vi phạm",
                if (isEn) "No helmet" else "Không đội mũ bảo hiểm"
            )
        }

        if (rawType.contains("hairnet")) {
            return createPart(
                if (isEn) "Safety violation" else "Vi phạm bảo hộ",
                if (isEn) "No safety cap" else "Không đội mũ bảo hộ"
            )
        }

        if (rawType.contains("mask")) {
            return createPart(
                if (isEn) "Health violation" else "Vi phạm y tế",
                if (isEn) "No mask" else "Không đeo khẩu trang"
            )
        }

        if (rawType.contains("glove")) {
            return createPart(
                if (isEn) "Safety violation" else "Vi phạm bảo hộ",
                if (isEn) "No gloves" else "Không đeo găng tay"
            )
        }

        if (rawType.contains("uniform") || rawType.contains("clothing")) {
            return createPart(
                if (isEn) "Uniform" else "Đồng phục",
                extractUniformLabel(event, isEn)
            )
        }

        if (rawType.contains("intrusion") || rawType == "logic-person" || rawType == "sensor-person-camera") {
            return createPart(
                if (isEn) "Detected" else "Phát hiện",
                if (isEn) "Intrusion" else "Xâm nhập"
            )
        }

        if (rawType.contains("person-in")) {
            return createPart(
                if (isEn) "Detected" else "Phát hiện",
                if (isEn) "Person entering" else "Người vào"
            )
        }

        if (rawType.contains("person-out")) {
            return createPart(
                if (isEn) "Detected" else "Phát hiện",
                if (isEn) "Person leaving" else "Người ra"
            )
        }

        if (rawType.contains("absence")) {
            return createPart(
                if (isEn) "Detected" else "Phát hiện",
                if (isEn) "Absence from post" else "Rời vị trí"
            )
        }

        if (rawType.contains("crowd")) {
            return createPart(
                if (isEn) "Detected" else "Phát hiện",
                if (isEn) "Crowd gathering" else "Tụ tập đám đông"
            )
        }

        if (rawType.contains("fire") || rawType.contains("smoke")) {
            val isSmokeOnly = rawType.contains("smoke") && !rawType.contains("fire")
            return createPart(
                if (isEn) "Detected" else "Phát hiện",
                if (isSmokeOnly) (if (isEn) "Smoke" else "Khói") else (if (isEn) "Fire & smoke" else "Cháy & khói")
            )
        }

        if (rawType.contains("garbage") || rawType.contains("litter")) {
            return createPart(
                if (isEn) "Detected" else "Phát hiện",
                if (isEn) "Littering" else "Xả rác bừa bãi"
            )
        }

        if (rawType.contains("sidewalk") || rawType.contains("encroach")) {
            return createPart(
                if (isEn) "Detected" else "Phát hiện",
                if (isEn) "Sidewalk encroachment" else "Lấn chiếm vỉa hè"
            )
        }

        if (rawType.contains("wrong") || rawType.contains("crossing")) {
            return createPart(
                if (isEn) "Violation" else "Vi phạm",
                if (isEn) "Wrong-way driving" else "Đi ngược chiều"
            )
        }

        if (rawType.contains("violence") || rawType.contains("fight")) {
            return createPart(
                if (isEn) "Detected" else "Phát hiện",
                if (isEn) "Violent activity" else "Hành vi bạo lực"
            )
        }

        if (rawType.contains("fall")) {
            return createPart(
                if (isEn) "Detected" else "Phát hiện",
                if (isEn) "Fall" else "Người té ngã"
            )
        }

        if (rawType.contains("smoking") || rawType.contains("pose")) {
            return createPart(
                if (isEn) "Detected" else "Phát hiện",
                if (isEn) "Smoking" else "Hút thuốc lá"
            )
        }

        if (rawType.contains("phone")) {
            return createPart(
                if (isEn) "Detected" else "Phát hiện",
                if (isEn) "Mobile phone usage" else "Sử dụng điện thoại"
            )
        }

        if (rawType.contains("construct")) {
            return createPart(
                if (isEn) "Detected" else "Phát hiện",
                if (isEn) "Construction activity" else "Thi công"
            )
        }

        if (rawType.contains("flood")) {
            return createPart(
                if (isEn) "Detected" else "Phát hiện",
                if (isEn) "Flooding" else "Ngập lụt"
            )
        }

        val cleanLabel = getTypeName(event.type, isEn)
            .replace(REGEX_PREFIX_VI, "")
            .replace(REGEX_PREFIX_EN, "")
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

    private fun resolveMediaUrl(event: EventData, baseDomain: String, file: String?): String? {
        if (file.isNullOrBlank()) return null
        if (file.startsWith("http://") || file.startsWith("https://")) return file
        val domain = baseDomain.trimEnd('/')
        if (file.startsWith("/")) return "$domain$file"
        val containerId = event.data?.containerId ?: "0"
        return "$domain/Data/api/Data/Media/${event.serviceId}/$containerId/$file"
    }

    fun getSnapshotUrl(event: EventData, baseDomain: String): String {
        val data = event.data ?: return ""
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
        }
        return resolveMediaUrl(event, baseDomain, imageFile).orEmpty()
    }

    fun getFullImageUrl(event: EventData, baseDomain: String): String {
        val data = event.data ?: return ""
        val imageFile = data.image ?: data.fullImage ?: data.cropImage
        return resolveMediaUrl(event, baseDomain, imageFile).orEmpty()
    }

    fun getFaceCropUrl(event: EventData, baseDomain: String): String? {
        if (!isFaceEvent(event)) return null
        val data = event.data ?: return null
        val cropFile = data.face?.cropImage
            ?: data.faceCrop
            ?: data.faceCropImageSnake
            ?: data.faceCropImage
            ?: data.cropImage
            ?: return null
        if (cropFile == data.image) return null
        return resolveMediaUrl(event, baseDomain, cropFile)
    }

    fun getVideoUrl(event: EventData, baseDomain: String): String? {
        val data = event.data ?: return null
        val videoFile = data.video ?: data.videoUrl ?: data.focusVideo
        return resolveMediaUrl(event, baseDomain, videoFile)
    }

    // ---------------------------------------------------------------------------------
    // Cấu hình thông báo theo bài AI (overrides.event_types)
    // ---------------------------------------------------------------------------------

    const val PREF_USER_NOTIFICATION_SETTINGS_JSON = "user_notification_settings_json"
    const val PREF_MODULES_CONFIG_JSON = "project_modules_config_json"
    const val PREF_MODULES_CONFIG_DOMAIN = "project_modules_config_domain"

    fun findModuleByKey(moduleKey: String): AiModuleOption? {
        val target = moduleKey.trim()
        if (target.isEmpty()) return null
        val snap = permissionSnapshot
        val alias = snap.aliasLookup[target.lowercase()] ?: target
        return snap.activeModules.firstOrNull {
            it.key.equals(target, ignoreCase = true) ||
                it.key.equals(alias, ignoreCase = true) ||
                it.sensorType.equals(target, ignoreCase = true)
        } ?: catalogModules.firstOrNull {
            it.key.equals(target, ignoreCase = true) ||
                it.key.equals(alias, ignoreCase = true) ||
                it.sensorType.equals(target, ignoreCase = true)
        }
    }

    fun findModuleByEventType(eventType: String): AiModuleOption? {
        val target = eventType.trim().lowercase()
        if (target.isEmpty()) return null
        val snap = permissionSnapshot
        return snap.activeModules.firstOrNull { mod ->
            mod.key.equals(target, ignoreCase = true) ||
                mod.sensorType.equals(target, ignoreCase = true) ||
                mod.eventTypes.any { it.equals(target, ignoreCase = true) }
        } ?: catalogModules.firstOrNull { mod ->
            mod.key.equals(target, ignoreCase = true) ||
                mod.sensorType.equals(target, ignoreCase = true) ||
                mod.eventTypes.any { it.equals(target, ignoreCase = true) }
        }
    }

    private fun extractEventTypeEntryRule(entryElement: JsonElement?): Pair<Boolean?, String?> {
        val entryObj = entryElement?.takeIf { it.isJsonObject }?.asJsonObject ?: return null to null
        val notifObj = entryObj.get("notification")?.takeIf { it.isJsonObject }?.asJsonObject ?: entryObj

        val enabledVal = notifObj.get("enabled")
            ?.takeIf { it.isJsonPrimitive }
            ?.asJsonPrimitive
            ?.let { prim ->
                when {
                    prim.isBoolean -> prim.asBoolean
                    prim.isString -> prim.asString.equals("true", ignoreCase = true)
                    else -> null
                }
            }

        val alarmEl = notifObj.get("alarmMode") ?: notifObj.get("alarm_mode")
        val alarmVal = when {
            alarmEl == null || alarmEl.isJsonNull -> null
            alarmEl.isJsonPrimitive -> alarmEl.asString
            alarmEl.isJsonObject -> {
                val alarmObj = alarmEl.asJsonObject
                alarmObj.get("default")?.takeIf { it.isJsonPrimitive }?.asString
            }
            else -> null
        }?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }

        return enabledVal to alarmVal
    }

    private fun getEventTypesJsonObject(overridesElement: JsonElement?): JsonObject? {
        val overridesObj = overridesElement?.takeIf { it.isJsonObject }?.asJsonObject ?: return null
        return (overridesObj.get("event_types") ?: overridesObj.get("eventTypes"))
            ?.takeIf { it.isJsonObject }
            ?.asJsonObject
    }

    private fun findEventTypeEntryCaseInsensitive(eventTypesObj: JsonObject, key: String): JsonElement? {
        eventTypesObj.get(key)?.let { return it }
        val lower = key.lowercase()
        for ((k, v) in eventTypesObj.entrySet()) {
            if (k.lowercase() == lower) return v
        }
        return null
    }

    /**
     * Đọc cấu hình `(enabled, alarmMode)` của một bài AI từ `overrides.event_types`.
     * Mặc định: `enabled = true`, `alarmMode = "push"`.
     */
    fun extractModuleNotifyConfig(
        module: AiModuleOption,
        overridesElement: JsonElement?
    ): Pair<Boolean, String> {
        val eventTypesObj = getEventTypesJsonObject(overridesElement) ?: return true to "push"
        val candidateKeys = (listOf(module.key, module.sensorType) + module.eventTypes)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()

        var resolvedEnabled: Boolean? = null
        var resolvedAlarmMode: String? = null

        for (key in candidateKeys) {
            val entry = findEventTypeEntryCaseInsensitive(eventTypesObj, key) ?: continue
            val (en, mode) = extractEventTypeEntryRule(entry)
            if (resolvedEnabled == null && en != null) resolvedEnabled = en
            if (resolvedAlarmMode == null && mode != null) resolvedAlarmMode = mode
            if (resolvedEnabled != null && resolvedAlarmMode != null) break
        }

        val finalEnabled = resolvedEnabled ?: true
        val finalMode = if (resolvedAlarmMode == "call") "call" else "push"
        return finalEnabled to finalMode
    }

    /**
     * Suy luận `eventType` từ `title` / `body` của thông báo trong trường hợp bản tin FCM
     * không gửi kèm trường `type` / `eventType` trong `data`.
     */
    fun inferEventTypeFromText(title: String?, body: String?): String {
        val combined = "${title.orEmpty()} ${body.orEmpty()}".trim().lowercase()
        if (combined.isEmpty()) return ""

        // 1. Khớp trực tiếp theo mã sensorType / eventTypes / key
        for (mod in defaultModules) {
            if (mod.eventTypes.any { combined.contains(it.lowercase()) } ||
                combined.contains(mod.sensorType.lowercase())
            ) {
                return mod.sensorType
            }
        }

        // 2. Khớp theo tên bài AI tiếng Việt / tiếng Anh (kể cả bản rút gọn bỏ tiền tố "Phát hiện " / "Detected ")
        for (mod in defaultModules) {
            val viFull = mod.labelVi.lowercase()
            val viShort = mod.labelVi.replace(REGEX_PREFIX_VI, "").trim().lowercase()
            val enFull = mod.labelEn.lowercase()
            val enShort = mod.labelEn.replace(REGEX_PREFIX_EN, "").trim().lowercase()
            if (combined.contains(viFull) ||
                (viShort.length >= 4 && combined.contains(viShort)) ||
                combined.contains(enFull) ||
                (enShort.length >= 4 && combined.contains(enShort))
            ) {
                return mod.sensorType
            }
        }

        // 3. Một số từ khoá phổ biến trong nội dung cảnh báo
        return when {
            combined.contains("người lạ") || combined.contains("stranger") -> "sensor-face-unknown-camera"
            combined.contains("khuôn mặt") -> "logic-face"
            combined.contains("biển số") || combined.contains("license plate") -> "sensor-license-plate"
            combined.contains("mũ bảo hiểm") || combined.contains("helmet") -> "sensor-helmet-camera"
            combined.contains("cháy") || combined.contains("khói") -> "sensor-fire-camera"
            combined.contains("đông người") || combined.contains("đám đông") -> "sensor-crowd-camera"
            combined.contains("trang phục") || combined.contains("đồng phục") -> "sensor-uniform-camera"
            combined.contains("người vào") || combined.contains("person entering") || combined.contains("person entry") -> "sensor-person-in"
            combined.contains("người ra") || combined.contains("person leaving") || combined.contains("person exit") -> "sensor-person-out"
            combined.contains("rời vị trí") || combined.contains("absence") -> "sensor-absence-tracking-camera"
            combined.contains("xâm nhập") -> "sensor-person-camera"
            combined.contains("đỗ xe") -> "sensor-parking-camera"
            combined.contains("xả rác") -> "sensor-littering-camera"
            combined.contains("vỉa hè") || combined.contains("lấn chiếm") -> "sensor-encroachment-camera"
            combined.contains("thi công") -> "sensor-construction-camera"
            combined.contains("ngược chiều") -> "sensor-crossing-tracking"
            combined.contains("đánh nhau") || combined.contains("bạo lực") -> "sensor-violence"
            combined.contains("hút thuốc") -> "sensor-smoking"
            combined.contains("người ngã") || combined.contains("té ngã") -> "sensor-fall-person-camera"
            combined.contains("găng tay") -> "sensor-gloves"
            combined.contains("mũ bảo hộ") -> "sensor-hairnet"
            combined.contains("khẩu trang") -> "sensor-face-masked"
            combined.contains("ngập lụt") || combined.contains("ngập nước") -> "flooding"
            else -> ""
        }
    }

    /**
     * Tra cứu cấu hình `(enabled, alarmMode)` cho một `eventType` cụ thể khi nhận FCM push.
     */
    fun resolveEventNotificationRule(
        eventType: String,
        overridesElement: JsonElement?
    ): Pair<Boolean, String> {
        val trimmed = eventType.trim()
        if (trimmed.isEmpty()) return true to "push"

        val eventTypesObj = getEventTypesJsonObject(overridesElement) ?: return true to "push"
        val directEntry = findEventTypeEntryCaseInsensitive(eventTypesObj, trimmed)
        val (directEnabled, directMode) = extractEventTypeEntryRule(directEntry)
        if (directEnabled != null || directMode != null) {
            return (directEnabled ?: true) to (if (directMode == "call") "call" else "push")
        }

        val module = findModuleByEventType(trimmed)
        if (module != null) {
            return extractModuleNotifyConfig(module, overridesElement)
        }
        return true to "push"
    }

    /**
     * Gộp `overrides` từ cache cục bộ và `overrides` trả về từ server để không bị mất cấu hình
     * khi server trả về `event_types` rỗng hoặc lược bỏ các key chưa phát sinh sự kiện.
     */
    fun mergeNotificationOverrides(
        localOverrides: JsonElement?,
        serverOverrides: JsonElement?
    ): JsonObject {
        val result = localOverrides
            ?.takeIf { it.isJsonObject }
            ?.asJsonObject
            ?.deepCopy()
            ?: UserNotificationSettingsModel.defaultNotificationOverridesJson()

        if (!result.has("event_type_groups") || !result.get("event_type_groups").isJsonObject) {
            result.add("event_type_groups", JsonObject())
        }
        if (!result.has("event_types") || !result.get("event_types").isJsonObject) {
            result.add("event_types", JsonObject())
        }

        val serverObj = serverOverrides?.takeIf { it.isJsonObject }?.asJsonObject ?: return result

        serverObj.get("event_type_groups")?.takeIf { it.isJsonObject }?.asJsonObject?.let { serverGroups ->
            val targetGroups = result.getAsJsonObject("event_type_groups")
            for ((k, v) in serverGroups.entrySet()) {
                val existingGroupKey = targetGroups.entrySet()
                    .firstOrNull { it.key.equals(k, ignoreCase = true) }
                    ?.key
                if (existingGroupKey != null && existingGroupKey != k) {
                    targetGroups.remove(existingGroupKey)
                }
                targetGroups.add(k, v.deepCopy())
            }
        }

        val serverEventTypes = (serverObj.get("event_types") ?: serverObj.get("eventTypes"))
            ?.takeIf { it.isJsonObject }
            ?.asJsonObject

        if (serverEventTypes != null) {
            val targetEventTypes = result.getAsJsonObject("event_types")
            for ((k, serverEntryEl) in serverEventTypes.entrySet()) {
                val existingKey = targetEventTypes.entrySet()
                    .firstOrNull { it.key.equals(k, ignoreCase = true) }
                    ?.key
                val serverEntry = serverEntryEl.takeIf { it.isJsonObject }?.asJsonObject
                if (serverEntry == null) {
                    if (existingKey != null && existingKey != k) {
                        targetEventTypes.remove(existingKey)
                    }
                    targetEventTypes.add(k, serverEntryEl.deepCopy())
                    continue
                }
                val localEntry = existingKey
                    ?.let { targetEventTypes.get(it) }
                    ?.takeIf { it.isJsonObject }
                    ?.asJsonObject
                    ?.deepCopy()
                    ?: JsonObject()
                for ((entryKey, entryVal) in serverEntry.entrySet()) {
                    if (entryKey == "notification" && entryVal.isJsonObject && localEntry.get("notification")?.isJsonObject == true) {
                        val mergedNotif = localEntry.getAsJsonObject("notification").deepCopy()
                        for ((nk, nv) in entryVal.asJsonObject.entrySet()) {
                            mergedNotif.add(nk, nv.deepCopy())
                        }
                        localEntry.add("notification", mergedNotif)
                    } else {
                        localEntry.add(entryKey, entryVal.deepCopy())
                    }
                }
                if (existingKey != null && existingKey != k) {
                    targetEventTypes.remove(existingKey)
                }
                targetEventTypes.add(k, localEntry)
            }
        }

        return result
    }

    /**
     * Cập nhật `enabled` và `alarmMode` ("push" | "call") cho tất cả các `eventTypes` thuộc `module`
     * trong `overrides.event_types` mà vẫn giữ nguyên các trường cấu hình khác trên server.
     */
    fun applyModuleOverrideToJson(
        baseOverrides: JsonElement?,
        module: AiModuleOption,
        enabled: Boolean,
        alarmMode: String
    ): JsonObject {
        val overridesObj = baseOverrides
            ?.takeIf { it.isJsonObject }
            ?.asJsonObject
            ?.deepCopy()
            ?: UserNotificationSettingsModel.defaultNotificationOverridesJson()

        if (!overridesObj.has("event_type_groups") || !overridesObj.get("event_type_groups").isJsonObject) {
            overridesObj.add("event_type_groups", JsonObject())
        }

        val eventTypesObj = (overridesObj.get("event_types") ?: overridesObj.get("eventTypes"))
            ?.takeIf { it.isJsonObject }
            ?.asJsonObject
            ?.deepCopy()
            ?: JsonObject()

        val normalizedMode = if (alarmMode.equals("call", ignoreCase = true)) "call" else "push"
        val targetKeys = buildList {
            add(module.key)
            add(module.sensorType)
            addAll(module.eventTypes)
        }.map { it.trim() }.filter { it.isNotEmpty() }.distinct()

        for (rawKey in targetKeys) {
            val existingActualKey = eventTypesObj.entrySet()
                .firstOrNull { it.key.equals(rawKey, ignoreCase = true) }
                ?.key
            val key = existingActualKey ?: rawKey

            val entryObj = eventTypesObj.get(key)
                ?.takeIf { it.isJsonObject }
                ?.asJsonObject
                ?.deepCopy()
                ?: JsonObject()

            val notifObj = entryObj.get("notification")
                ?.takeIf { it.isJsonObject }
                ?.asJsonObject
                ?.deepCopy()
                ?: JsonObject()

            notifObj.addProperty("enabled", enabled)

            val alarmModeObj = notifObj.get("alarmMode")
                ?.takeIf { it.isJsonObject }
                ?.asJsonObject
                ?.deepCopy()
                ?: JsonObject()
            alarmModeObj.addProperty("default", normalizedMode)
            notifObj.add("alarmMode", alarmModeObj)

            if (notifObj.has("alarm_mode")) {
                val snakeObj = notifObj.get("alarm_mode")
                    ?.takeIf { it.isJsonObject }
                    ?.asJsonObject
                    ?.deepCopy()
                    ?: JsonObject()
                snakeObj.addProperty("default", normalizedMode)
                notifObj.add("alarm_mode", snakeObj)
            }

            entryObj.add("notification", notifObj)
            eventTypesObj.add(key, entryObj)
        }

        overridesObj.add("event_types", eventTypesObj)
        return overridesObj
    }
}
