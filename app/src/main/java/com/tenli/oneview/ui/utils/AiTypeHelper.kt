package com.tenli.oneview.ui.utils

import com.tenli.oneview.model.network.EventData

object AiTypeHelper {
    fun getEventName(event: EventData): String {
        val type = event.type
        if (type == "logic-uniform" || type == "sensor-uniform-camera") {
            val data = event.data
            if (data?.uniform != null) {
                return "Vi phạm trang phục"
            }
            if (data?.face != null) {
                if (data.face.hasMask == false) {
                    return "Không đeo khẩu trang"
                }
                return "Vi phạm trang phục"
            }
            return "Vi phạm trang phục"
        }
        return getTypeName(type)
    }

    fun getAiColor(event: EventData): androidx.compose.ui.graphics.Color {
        val eventName = getEventName(event)
        if (eventName == "Không đeo khẩu trang") {
            return androidx.compose.ui.graphics.Color(0xFFA2821B) // Color for mask/face
        }
        return getAiColor(event.type)
    }

    fun getTypeName(type: String?): String {
        if (type == null) return "Sự kiện không xác định"
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
            "logic-fall-person" to "Phát hiện người ngã bất thường",
            "sensor-fall-person-camera" to "Phát hiện người ngã bất thường",
            "logic-power" to "Phát hiện mất điện",
            "sensor-person-entry-exit-camera" to "Đếm người vào ra",
            "sensor-person-dwell-camera" to "Phát hiện người ở lâu",
            "logic-face" to "Nhận diện khuôn mặt",
            "sensor-license-plate" to "Nhận dạng biển số",
            "sensor-face-unknown-camera" to "Nhận diện khuôn mặt",
            "logic-uniform" to "Nhận diện trang phục",
            "sensor-object" to "Nhận diện đối tượng",
            "object-begin" to "Nhận diện đối tượng",
            "object-end" to "Nhận diện đối tượng",
            "sensor-object-tracking" to "Nhận diện đối tượng",
            "sensor-gesture-camera" to "Nhận diện cử chỉ",
            "sensor-heatmap-camera" to "Phân tích mật độ",
            "sensor-crowd-camera" to "Phát hiện đông người",
            "sensor-uniform-camera" to "Nhận diện trang phục",
            "sensor-violence" to "Phát hiện đánh nhau",
            "sensor-pose-camera" to "Phát hiện hút thuốc",
            "sensor-phone-camera" to "Phát hiện dùng điện thoại",
            "sensor-vehicle-camera" to "Nhận diện phương tiện",
            "sensor-helmet-camera" to "Không đội mũ bảo hiểm",
            "sensor-helmet-violation-camera" to "Không đội mũ bảo hiểm",
            "sensor-parking-camera" to "Phát hiện dừng đỗ xe",
            "sensor-animal-camera" to "Phát hiện động vật",
            "sensor-weapon-camera" to "Phát hiện vũ khí",
            "sensor-absence-tracking-camera" to "Phát hiện rời vị trí",
            "sensor-encroachment-camera" to "Lấn chiếm vỉa hè",
            "sensor-littering-camera" to "Phát hiện đổ rác",
            "construction-sensor-camera" to "Xây dựng trái phép"
        )
        return typeMap[type] ?: type
    }

    fun getAiColor(type: String?): androidx.compose.ui.graphics.Color {
        val t = type?.lowercase() ?: return androidx.compose.ui.graphics.Color(0xFF17B37F)
        
        return when {
            t.contains("face") || t.contains("mask") -> androidx.compose.ui.graphics.Color(0xFFA2821B)
            t.contains("uniform") || t.contains("hat") || t.contains("vest") || t.contains("id-card") -> androidx.compose.ui.graphics.Color(0xFF0A8F8D)
            t.contains("crowd") -> androidx.compose.ui.graphics.Color(0xFF3B9D0E)
            t.contains("heatmap") -> androidx.compose.ui.graphics.Color(0xFF6366F1)
            t.contains("lpr") || t.contains("plate") || t.contains("license") -> androidx.compose.ui.graphics.Color(0xFFEA580C)
            t.contains("fire") || t.contains("smoke") -> androidx.compose.ui.graphics.Color(0xFFDC2626)
            t.contains("helmet") -> androidx.compose.ui.graphics.Color(0xFF198D0A)
            t.contains("parking") -> androidx.compose.ui.graphics.Color(0xFFA50F69)
            t.contains("garbage") || t.contains("dumping") || t.contains("littering") -> androidx.compose.ui.graphics.Color(0xFF9E8609)
            t.contains("person") || t.contains("intrusion") -> androidx.compose.ui.graphics.Color(0xFF0A9C47)
            t.contains("vehicle") -> androidx.compose.ui.graphics.Color(0xFF0284C7)
            t.contains("sidewalk") || t.contains("encroachment") -> androidx.compose.ui.graphics.Color(0xFFA78F08)
            t.contains("construction") -> androidx.compose.ui.graphics.Color(0xFFB97B0F)
            t.contains("violence") || t.contains("fight") -> androidx.compose.ui.graphics.Color(0xFF7F10AB)
            t.contains("smoking") || t.contains("pose") -> androidx.compose.ui.graphics.Color(0xFFD97706)
            t.contains("phone") || t.contains("calling") -> androidx.compose.ui.graphics.Color(0xFF6366F1)
            t.contains("object") || t.contains("tracking") -> androidx.compose.ui.graphics.Color(0xFF3B82F6)
            t.contains("absence") -> androidx.compose.ui.graphics.Color(0xFF9CA3AF)
            t.contains("fall") -> androidx.compose.ui.graphics.Color(0xFFEF4444)
            else -> androidx.compose.ui.graphics.Color(0xFF17B37F)
        }
    }
}
