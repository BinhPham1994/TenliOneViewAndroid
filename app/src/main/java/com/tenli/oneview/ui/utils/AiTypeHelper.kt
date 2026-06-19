package com.tenli.oneview.ui.utils

object AiTypeHelper {
    fun getTypeName(type: String?): String {
        if (type == null) return "Sự kiện không xác định"
        val typeMap = mapOf(
            "logic-person" to "Xâm nhập",
            "sensor-person-camera" to "Xâm nhập",
            "logic-fish-out" to "Cá nhảy ra ngoài",
            "sensor-fish-out-camera" to "Cá nhảy ra ngoài",
            "logic-person-pond" to "Người tới gần hồ",
            "sensor-person-pond-camera" to "Người tới gần hồ",
            "sensor-person-pond-camera-v2" to "Người tới gần hồ",
            "logic-fire" to "Báo cháy, khói",
            "sensor-fire-camera" to "Báo cháy, khói",
            "logic-fall-person" to "Phát hiện té ngã",
            "sensor-fall-person-camera" to "Phát hiện té ngã",
            "logic-power" to "Cảnh báo điện",
            "sensor-person-entry-exit-camera" to "Vào/Ra",
            "sensor-person-dwell-camera" to "Lảng vảng",
            "logic-face" to "Nhận diện khuôn mặt",
            "sensor-license-plate" to "Nhận diện biển số",
            "sensor-face-unknown-camera" to "Nhận diện khuôn mặt",
            "logic-uniform" to "Kiểm tra đồng phục",
            "sensor-object" to "Phát hiện đồ vật",
            "object-begin" to "Phát hiện đồ vật",
            "object-end" to "Phát hiện đồ vật",
            "sensor-object-tracking" to "Phát hiện đồ vật",
            "sensor-gesture-camera" to "Hành động/Cử chỉ",
            "sensor-heatmap-camera" to "Biểu đồ nhiệt (Heatmap)",
            "sensor-crowd-camera" to "Phát hiện đám đông",
            "sensor-uniform-camera" to "Kiểm tra đồng phục",
            "sensor-violence" to "Đánh nhau/Bạo lực",
            "sensor-pose-camera" to "Hút thuốc",
            "sensor-phone-camera" to "Sử dụng điện thoại",
            "sensor-vehicle-camera" to "Nhận diện phương tiện",
            "sensor-helmet-camera" to "Không đội mũ bảo hiểm",
            "sensor-helmet-violation-camera" to "Không đội mũ bảo hiểm",
            "sensor-parking-camera" to "Đỗ xe sai quy định",
            "sensor-animal-camera" to "Phát hiện động vật",
            "sensor-weapon-camera" to "Phát hiện vũ khí",
            "sensor-absence-tracking-camera" to "Giám sát vắng mặt",
            "sensor-encroachment-camera" to "Lấn chiếm vỉa hè",
            "sensor-littering-camera" to "Xả rác bừa bãi",
            "construction-sensor-camera" to "Giám sát thi công"
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
