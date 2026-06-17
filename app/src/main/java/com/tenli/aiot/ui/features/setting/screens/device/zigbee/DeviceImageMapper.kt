package com.tenli.aiot.ui.features.setting.screens.device.zigbee

import com.tenli.aiot.R

object DeviceImageMapper {
    /**
     * Trả về Resource ID của ảnh local dựa trên modelId.
     * Nếu không tìm thấy, trả về null.
     */
    fun getLocalImage(modelId: String?, displayName: String): Int? {
        return when (modelId) {
            "TS0003" -> R.drawable.img_tuya_3gang
            else -> null
        }
    }
}

//"TS0003" -> R.drawable.img_tuya_3gang // Bạn cần thêm file này vào drawable
//"TS0001" -> R.drawable.img_tuya_1gang
//"WXKG01LM" -> R.drawable.img_xiaomi_button
//"TS0216" -> R.drawable.img_siren_alarm
//"ZB-SP1000" -> R.drawable.img_mp3_player