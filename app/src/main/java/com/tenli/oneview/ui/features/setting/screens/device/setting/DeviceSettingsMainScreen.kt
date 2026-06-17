package com.tenli.oneview.ui.features.setting.screens.device.setting

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.tenli.oneview.R
import com.tenli.oneview.ui.features.setting.core.SettingScreenType
import com.tenli.oneview.ui.features.setting.core.SettingViewModel
import com.tenli.oneview.ui.features.setting.core.fetchSystemSettings
import com.tenli.oneview.ui.features.setting.screens.device.DeviceDetailGroup
import com.tenli.oneview.ui.features.setting.screens.device.DeviceDetailItem
import com.tenli.oneview.ui.theme.spacing

@Composable
fun DeviceSettingsMainScreen(viewModel: SettingViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings = uiState.box.systemSetting?.settings

    LaunchedEffect(Unit) { viewModel.fetchSystemSettings() }

    val voiceLabel = when (settings?.alarmAudio) {
        "male-north" -> "Giọng nam miền Bắc"
        "female-north" -> "Giọng nữ miền Bắc"
        "male-south" -> "Giọng nam miền Nam"
        "female-south" -> "Giọng nữ miền Nam"
        else -> ""
    }

    Column(modifier = Modifier
        .fillMaxSize()) {
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
        DeviceDetailGroup {
            DeviceDetailItem(R.drawable.iot, "Cấu hình MQTT", Color(0xFFFBC02D)) {
                viewModel.navigateTo(SettingScreenType.MqttConfig, "Cấu hình MQTT")
            }
            DeviceDetailItem(R.drawable.sound_icon, "Cài đặt cảnh báo", Color(0xFF4CAF50)) {
                viewModel.navigateTo(SettingScreenType.AlarmConfig, "Cài đặt cảnh báo")
            }
            DeviceDetailItem(R.drawable.store_icon, "Cài đặt lưu trữ", Color(0xFFFFA000)) {
                viewModel.navigateTo(SettingScreenType.StorageConfig, "Cài đặt lưu trữ")
            }
            // Hiển thị giá trị hiện tại của Giọng điệu ở bên phải
            DeviceDetailItem(
                iconRes = R.drawable.sound_icon,
                title = "Giọng điệu",
                value = voiceLabel,
                iconBgColor = Color(0xFF4CAF50),
                showDivider = false,
                onClick = { viewModel.navigateTo(SettingScreenType.VoiceConfig, "Giọng điệu") }
            )
        }
    }
}