package com.tenli.oneview.ui.features.setting.screens.device.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tenli.oneview.ui.features.setting.core.SettingViewModel
import com.tenli.oneview.ui.features.setting.core.updateVoiceConfig
import com.tenli.oneview.ui.theme.spacing

@Composable
fun VoiceConfigScreen(viewModel: SettingViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val data = uiState.box.systemSetting

    var selectedVoice by remember(data) {
        mutableStateOf(data?.settings?.alarmAudio ?: "male-north")
    }

    val voiceOptions = listOf(
        "male-north" to "Giọng Nam miền Bắc",
        "female-north" to "Giọng Nữ miền Bắc",
        "male-south" to "Giọng Nam miền Nam",
        "female-south" to "Giọng Nữ miền Nam"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.medium)
    ) {
        if (data == null) {
            Surface(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color(0xFFFFEBEE),
                shape = RoundedCornerShape(MaterialTheme.spacing.radiusSmall)
            ) {
                Text(
                    text = "Thiết bị ngoại tuyến. Vui lòng kiểm tra lại kết nối.",
                    modifier = Modifier.padding(12.dp),
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Chọn giọng nói cảnh báo",
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray,
                modifier = Modifier.padding(start = MaterialTheme.spacing.small, bottom = MaterialTheme.spacing.small, top = MaterialTheme.spacing.small)
            )

            // Danh sách các lựa chọn
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(MaterialTheme.spacing.radiusMedium))
                    .background(Color.White)
            ) {
                voiceOptions.forEachIndexed { index, (key, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedVoice = key } // Chỉ đổi state tạm thời tại UI
                            .padding(MaterialTheme.spacing.medium),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = label, style = MaterialTheme.typography.bodyLarge)
                        if (selectedVoice == key) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    if (index < voiceOptions.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
                            thickness = MaterialTheme.spacing.borderThin,
                            color = Color.LightGray.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                viewModel.updateVoiceConfig(selectedVoice) {
                    viewModel.navigateBack()
                }
            },
            enabled = data != null && !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = MaterialTheme.spacing.medium)
                .height(52.dp),
            shape = RoundedCornerShape(MaterialTheme.spacing.radiusMedium),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(MaterialTheme.spacing.iconMedium), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("Lưu", style = MaterialTheme.typography.titleSmall, color = Color.White)
            }
        }
    }
}