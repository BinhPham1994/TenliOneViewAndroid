package com.tenli.aiot.model.network

import com.google.gson.annotations.SerializedName

data class ScriptItem(
    val id: String,
    val name: String,
    val enabled: Boolean,
    val monitorIds: List<Int>? = null,
    val resetModeAfterTime: Int? = null,
    val time: ScriptTime? = null,
    val actions: ScriptActions? = null,
    val createdAt: Long,
    val updatedAt: Long
)

data class ScriptTime(
    val repeat: String, // "week", "once", etc.
    val dayOfWeeks: List<Int>? = null, // [0, 1, 2, 3, 4, 5, 6]
    val activate: TimeDetail? = null,
    val deactivate: TimeDetail? = null,
    val weekOfYearDivide: Int? = null,
    val dayOfYearDivide: Int? = null
)

data class TimeDetail(
    val hour: Int,
    val minute: Int,
    val second: Int
)

// --- PHẦN HÀNH ĐỘNG (ACTIONS) --- [cite: 2026-03-16]
data class ScriptActions(
    val activate: List<ActionDetail>? = null,
    val deactivate: List<ActionDetail>? = null
)

data class ActionDetail(
    val type: String, // ví dụ: "monitor-security"
    val enabled: Boolean,
    val monitorIds: List<Int>? = null,
    val delay: Int = 0,
    val data: Map<String, Any?>? = emptyMap(),
    val options: Map<String, Any?>? = emptyMap()
)

data class MonitorDisplayItem(
    val monitor: Monitor,
    val deviceUri: String,
    val deviceKey: String,
    val deviceId: Int
) {
    val snapshotUrl: String
        get() = "${deviceUri.removeSuffix("/")}/api/MonitorLiveImage/${monitor.id}?key=$deviceKey"

    val streamUrl: String
        get() = "${deviceUri.removeSuffix("/")}/api/MonitorStream/${monitor.id}?key=$deviceKey"
}

data class Monitor(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("cameraId") val cameraId: Int,
    @SerializedName("enabled") val enabled: Boolean = false,

    // Sử dụng Map để hứng trọn bộ các tham số bất kỳ từ Box trả về [cite: 2026-02-28]
    @SerializedName("param") val param: Map<String, Any?>? = emptyMap()
)

// --- EXTENSION FUNCTIONS ĐỂ LẤY DỮ LIỆU AN TOÀN ---
// Vì Param là Map, ta tạo các hàm giúp lấy giá trị mà không lo crash [cite: 2026-02-28]

fun Monitor.getParamInt(key: String, default: Int = 0): Int {
    return (param?.get(key) as? Double)?.toInt() ?: default
}

fun Monitor.getParamBoolean(key: String, default: Boolean = false): Boolean {
    return param?.get(key) as? Boolean ?: default
}

fun Monitor.getParamString(key: String, default: String = ""): String {
    return param?.get(key) as? String ?: default
}

data class BoxSystemInfo(
    val deviceName: String = "",
    val deviceModel: String = "",
    val firmwareVersion: String = "",
    val macAddress: String = ""
)

data class BoxSystemState(
    val availableSpace: Long = 0,
    val capacitySpace: Long = 0,
    val deviceTime: Double = 0.0,
    val deviceTimeZone: String = "",
    val muted: Boolean = false,
    val muteRemain: Double = 0.0,
    val playing: Boolean = false,
)

data class SystemSettingResponse(
    val id: Int,
    val httpKey: String,
    val settings: BoxSettingsData
)

data class BoxSettingsData(
    // --- CẤU HÌNH MQTT CHÍNH ---
    val mqttEnabled: Boolean? = null,
    val mqttHost: String? = null,
    val mqttPort: Int? = null,
    val mqttClientId: String? = null,
    val mqttUser: String? = null,
    val mqttPassword: String? = null,
    val mqttRootTopic: String? = null,
    val mqttSubQoS: Int? = null,

    // --- CẤU HÌNH MQTT PING (Thường hay thiếu ở bản cũ) ---
    val mqttPingEnabled: Boolean? = null,
    val mqttPingTopic: String? = null,
    val mqttPingQoS: Int? = null,
    val mqttPingRetain: Boolean? = null,
    val mqttPingMessage: String? = null,
    val mqttPingInterval: Double? = null,

    // --- CẤU HÌNH LƯU TRỮ (STORAGE) ---
    val videoRetainDays: Double? = null,
    val imageRetainDays: Double? = null,
    val dataRetainDays: Double? = null,
    val diskFullPercent: Double? = null,
    val reportRetainDays: Double? = null,

    // --- CẤU HÌNH BÁO CÁO (REPORTING) ---
    val reportMonitorState: Boolean? = null,
    val reportCameraState: Boolean? = null,
    val reportUpdateEvent: Boolean? = null,
    val reportSystemStatus: Boolean? = null,

    // --- CẤU HÌNH CẢNH BÁO & GIỌNG NÓI ---
    val alarmAudio: String? = null,
    val alarmOn: List<String>? = null,
    val alarmOff: List<String>? = null,
    val location: List<String>? = null,

    // --- CẤU HÌNH HỆ THỐNG NÂNG CAO ---
    val cameraAuthorizedCheck: Boolean? = null,
    val numberReconnectAuthorized: Int? = null,
    val keepDataCount: Int? = null,
    val keepDataMedia: Boolean? = null
)

data class MonitorType(
    val name: String,
    val type: String
)

data class CameraItem(
    val id: Int,
    val name: String,
    val url: String,
    val uuid: String,
    val type: String
)

data class LanDevice(
    val name: String,
    val ip: String,
    val port: Int,
    val isLoading: Boolean = true,
    val needAuth: Boolean = false,
    val systemInfo: BoxSystemInfo? = null,
    val systemState: BoxSystemState? = null
)

data class CameraBrandTemplate(
    val brandName: String,
    val rtspTemplate: (user: String, pass: String, ip: String, port: String, channel: String) -> String
)

val commonBrands = listOf(
    CameraBrandTemplate("HikVision") { u, p, ip, port, ch -> "rtsp://$u:$p@$ip:$port/Streaming/Channels/${ch}01" },
    CameraBrandTemplate("Dahua") { u, p, ip, port, ch -> "rtsp://$u:$p@$ip:$port/cam/realmonitor?channel=$ch&subtype=0" },
    CameraBrandTemplate("Ezviz") { u, p, ip, port, _ -> "rtsp://$u:$p@$ip:$port/H.264" },
    CameraBrandTemplate("Imou") { u, p, ip, port, ch -> "rtsp://$u:$p@$ip:$port/cam/realmonitor?channel=$ch&subtype=0" },
    CameraBrandTemplate("KBVision") { u, p, ip, port, ch -> "rtsp://$u:$p@$ip:$port/cam/realmonitor?channel=$ch&subtype=0" },
    CameraBrandTemplate("Tiandy") { u, p, ip, port, ch -> "rtsp://$u:$p@$ip:$port/$ch/1" },
    CameraBrandTemplate("Uniview") { u, p, ip, port, ch -> "rtsp://$u:$p@$ip:$port/unicast/c$ch/s0/live" },
    CameraBrandTemplate("Hanwha") { u, p, ip, port, _ -> "rtsp://$u:$p@$ip:$port/profile2/media.smp" },
    CameraBrandTemplate("Vivotek") { u, p, ip, port, _ -> "rtsp://$u:$p@$ip:$port/live.sdp" },
    CameraBrandTemplate("Generic ONVIF") { u, p, ip, port, _ -> "rtsp://$u:$p@$ip:$port/onvif1" }
)

fun ScriptItem.getDisplaySchedule(): String {
    val t = this.time ?: return "Không có lịch trình"

    // 1. Format giờ (Giữ nguyên logic cũ của bạn)
    val start = t.activate?.let {
        "${it.hour.toString().padStart(2, '0')}:${it.minute.toString().padStart(2, '0')}"
    } ?: "00:00"

    val end = t.deactivate?.let {
        "${it.hour.toString().padStart(2, '0')}:${it.minute.toString().padStart(2, '0')}"
    } ?: "00:00"

    // 2. Map thứ trong tuần
    val dayMap = mapOf(1 to "T2", 2 to "T3", 3 to "T4", 4 to "T5", 5 to "T6", 6 to "T7", 0 to "CN")

    // 3. Logic hiển thị ngày lặp lại thông minh hơn [cite: 2026-03-16]
    val repeatDisplay = when (t.repeat) {
        "week" -> {
            val days = t.dayOfWeeks ?: emptyList()
            when {
                days.size == 7 -> "Hàng ngày"
                days.isEmpty() -> "Không lặp lại"
                else -> {
                    // Sắp xếp và nối các thứ lại (VD: "T2, T4, CN")
                    days.sorted().joinToString(", ") { dayMap[it] ?: "" }
                }
            }
        }

        "once" -> "Một lần"
        else -> t.repeat ?: "Không xác định"
    }

    return "$repeatDisplay, $start - $end"
}