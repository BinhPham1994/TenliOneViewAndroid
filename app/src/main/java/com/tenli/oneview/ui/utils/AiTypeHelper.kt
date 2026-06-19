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
}
