package com.tenli.oneview.data.mapper

import androidx.compose.ui.graphics.Color
import com.tenli.oneview.R
import com.tenli.oneview.data.repository.DataRepository
import com.tenli.oneview.model.network.DeviceItem
import com.tenli.oneview.model.network.EventItem
import com.tenli.oneview.model.network.EventTypeDef
import com.tenli.oneview.model.network.formatTimeAgo

data class EventUIConfig(
    val iconRes: Int,
    val color: Color? = null,
    val groupLabel: String
)

object EventProcessor {

    fun enrich(event: EventItem, deviceList: List<DeviceItem>, typeDefs: List<EventTypeDef>): EventItem {
        val targetDevice = deviceList.find { it.id == event.deviceId }
        val typeDef = typeDefs.find { it.key == event.eType }
        val aiTitle = typeDef?.display?.getLocalText() ?: "Phát hiện sự kiện"

        val deviceNameFromRepo = targetDevice?.name
            ?: event.deviceName?.ifBlank { "Thiết bị" }
            ?: "Thiết bị"

        val firstSensor = event.sData?.sensors?.firstOrNull()
        val effectiveCameraId = firstSensor?.cameraId ?: event.eCameraId
        val cameraInfo = targetDevice?.info?.cameraInfo?.find { it.id == effectiveCameraId }
        val cameraNameFromRepo = cameraInfo?.name ?: "không xác định"
        var finalActionPart = ""
        var finalDeviceName = ""
        var finalCameraName = ""
        var finalConnector = ""
        val mutableValues = event.eValues?.toMutableMap() ?: mutableMapOf()

        when (event.eType) {
            "new-device-login" -> {
                val saved = event.eValues?.get("saved") as? Map<*, *>
                val loginName = saved?.get("name")?.toString() ?: "không rõ"

                finalActionPart = "Thiết bị $loginName vừa đăng nhập vào tài khoản của bạn"
                mutableValues["profileName"] = loginName
                finalDeviceName = ""
            }

            "logic-face" -> {
                val profileName = event.eValues?.get("profileName") as? String ?: "người lạ"
                finalActionPart = if (aiTitle.contains("camera", ignoreCase = true)) {
                    "$aiTitle: $profileName"
                } else {
                    "$aiTitle $profileName"
                }
                finalConnector = " tại camera "
                finalCameraName = cameraNameFromRepo
                finalDeviceName = deviceNameFromRepo
            }

            else -> {
                finalActionPart = aiTitle
                if (effectiveCameraId != null) {
                    finalConnector = when {
                        aiTitle.contains("đến camera", ignoreCase = true) || aiTitle.contains("tại camera", ignoreCase = true) || aiTitle.contains("trên camera", ignoreCase = true) -> " "
                        aiTitle.contains("camera", ignoreCase = true) -> " tại "
                        else -> " tại camera "
                    }
                    finalCameraName = cameraNameFromRepo
                    finalDeviceName = deviceNameFromRepo
                } else {
                    finalConnector = if (aiTitle.contains("thiết bị", ignoreCase = true)) " " else " trên "
                    finalDeviceName = deviceNameFromRepo
                }
            }
        }

        val finalImage = firstSensor?.let {
            it.imageName?.ifBlank { it.image } ?: it.image
        }?.takeIf { it.isNotBlank() }
            ?: event.sData?.let {
                it.faceCrop?.ifBlank { it.image } ?: it.image
            }?.takeIf { it.isNotBlank() }
            ?: event.eImages

        val finalVideo = firstSensor?.let {
            it.videoName?.ifBlank { it.video } ?: it.video
        }?.takeIf { it.isNotBlank() }
            ?: event.sData?.video?.takeIf { it.isNotBlank() }
            ?: event.eVideos

        return event.copy(
            aiTitle = aiTitle,
            deviceUri = targetDevice?.publicTargetURI ?: event.deviceUri,
            deviceKey = targetDevice?.key ?: event.deviceKey,
            displayTitle = finalActionPart,
            eValues = mutableValues,
            eImages = finalImage,
            eVideos = finalVideo,
            timeAgo = formatTimeAgo(event.eTimestamp),
            actionPart = finalActionPart,
            foundConnector = finalConnector,
            cameraName = finalCameraName,
            deviceName = finalDeviceName
        )
    }

    fun getMonitorGroupTitle(type: String?): String {
        val typeDef = DataRepository.eventTypeDefs.find { it.key == type }
        return typeDef?.display?.getLocalText() ?: "Thiết bị khác"
    }

    fun getMonitorTitle(type: String?): String {
        val typeDef = DataRepository.eventTypeDefs.find { it.key == type }
        return typeDef?.display?.getLocalText() ?: (type ?: "Thiết bị khác")
    }

    fun getUIConfig(eType: String, eTypeGroup: String): EventUIConfig {
        val colorRed = Color(0xFFE42E1B)
        val colorGreen = Color(0xFF4AA541)
        val colorYellow = Color(0xFFFFC107)

        return when (eType) {
            // =====================================================================
            // 1. NHÓM HỆ THỐNG (Trạng thái thiết bị, tài khoản)
            // =====================================================================
            "new-device-login" ->
                EventUIConfig(R.drawable.member, colorGreen, "Hệ thống")

            "device-status-connected" ->
                EventUIConfig(R.drawable.device_icon, colorGreen, "Hệ thống")

            "device-status-disconnected" ->
                EventUIConfig(R.drawable.device_icon, colorRed, "Hệ thống")

            "device-status-issue" ->
                EventUIConfig(R.drawable.device_icon, colorYellow, "Hệ thống")

            "device-time-warning" ->
                EventUIConfig(R.drawable.time_icon_red, colorYellow, "Hệ thống")

            "device-storage-warning" ->
                EventUIConfig(R.drawable.setting, colorYellow, "Hệ thống")

            "camera-online" ->
                EventUIConfig(R.drawable.camera_icon, colorGreen, "Hệ thống")

            "camera-offline" ->
                EventUIConfig(R.drawable.camera_icon, colorRed, "Hệ thống")

            // =====================================================================
            // 2. NHÓM AN NINH (Con người & Trạng thái bảo vệ)
            // =====================================================================
            "logic-person" ->
                EventUIConfig(R.drawable.person_ai, colorRed, "An ninh")

            "sensor-person-camera" ->
                EventUIConfig(R.drawable.person_ai, colorYellow, "An ninh")

            "logic-fall-person" ->
                EventUIConfig(R.drawable.fall_person, colorRed, "An ninh")

            "sensor-fall-person-camera" ->
                EventUIConfig(R.drawable.fall_person, colorYellow, "An ninh")

            "security-enable" ->
                EventUIConfig(R.drawable.ai_icon, colorGreen, "An ninh")

            "security-disable" ->
                EventUIConfig(R.drawable.ai_icon, colorRed, "An ninh")

            // =====================================================================
            // 3. NHÓM HỎA HOẠN (Cháy, khói, khí gas, nhiệt độ)
            // =====================================================================
            "logic-fire" ->
                EventUIConfig(R.drawable.fire_event_group, colorRed, "Hỏa hoạn")

            "logic-fire-warning",
            "sensor-fire-camera",
            "sensor-smoke-camera",
            "sensor-mqtt-thermo",
            "sensor-mqtt-smoke",
            "sensor-mqtt-gas" ->
                EventUIConfig(R.drawable.fire_event_group, colorYellow, "Hỏa hoạn")

            // =====================================================================
            // 4. NHÓM NĂNG LƯỢNG (Điện, Công suất, Trạng thái cảm biến)
            // =====================================================================
            "sensor-mqtt-state-online" ->
                EventUIConfig(R.drawable.sensor_power_off, colorGreen, "Năng lượng")

            "sensor-mqtt-state-offline" ->
                EventUIConfig(R.drawable.sensor_power_off, colorRed, "Năng lượng")

            "sensor-mqtt-electric",
            "sensor-mqtt-power",
            "logic-high-power",
            "logic-low-power" ->
                EventUIConfig(R.drawable.power, colorYellow, "Năng lượng")

            "logic-no-power" ->
                EventUIConfig(R.drawable.power, colorRed, "Năng lượng")

            // =====================================================================
            // 5. NHÓM BỂ CÁ (Giám sát hồ cá)
            // =====================================================================
            "logic-person-pond" ->
                EventUIConfig(R.drawable.person_in_pond, colorRed, "Bể cá")

            "sensor-person-pond-camera" ->
                EventUIConfig(R.drawable.person_in_pond, colorYellow, "Bể cá")

            "logic-fish-out" ->
                EventUIConfig(R.drawable.fish_ai_monitor, colorRed, "Bể cá")

            "sensor-fish-out-camera" ->
                EventUIConfig(R.drawable.fish_ai_monitor, colorYellow, "Bể cá")

            // =====================================================================
            // 6. NHÓM NHẬN DIỆN KHUÔN MẶT
            // =====================================================================
            "logic-face",
            "sensor-face-camera" ->
                EventUIConfig(R.drawable.face_ai, colorGreen, "Khuôn mặt")

            "sensor-face-unknown-camera" ->
                EventUIConfig(R.drawable.face_ai, colorYellow, "Khuôn mặt")

            // =====================================================================
            // 7. NHÓM GIÁM SÁT AI KHÁC (Hành vi, Phương tiện, Đám đông...)
            // =====================================================================
            "sensor-gesture-camera",
            "logic-pose",
            "sensor-parking-camera",
            "sensor-uniform-camera",
            "sensor-helmet-camera",
            "sensor-crowd-camera",
            "sensor-vehicle-camera" ->
                EventUIConfig(R.drawable.ai_icon, colorGreen, "Giám sát AI")

            // MẶC ĐỊNH
            else -> EventUIConfig(R.drawable.ai_icon, colorGreen, "Sự kiện")
        }
    }
}
