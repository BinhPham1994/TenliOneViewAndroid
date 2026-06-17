package com.tenli.oneview.ui.features.setting.screens.device.sound

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tenli.oneview.ui.features.event.component.MuteEventSheet
import com.tenli.oneview.ui.features.setting.core.SettingViewModel
import com.tenli.oneview.ui.features.setting.core.controlAlarm
import com.tenli.oneview.ui.features.setting.core.refreshSystemState
import com.tenli.oneview.ui.features.setting.screens.device.DeviceDetailGroup
import com.tenli.oneview.ui.theme.spacing
import com.tenli.oneview.ui.utils.DateTimeUtils.formatMuteRemain

@Composable
fun AlarmSoundScreen(viewModel: SettingViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val systemState = uiState.box.state
    val isMuted = systemState?.muted ?: false
    val muteRemainSeconds = systemState?.muteRemain ?: 0.0
    val isPlaying = systemState?.playing ?: false
    var showMuteSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refreshSystemState()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = MaterialTheme.spacing.medium)
    ) {
        DeviceDetailGroup {
            AlarmSwitchItem(
                title = "Kiểm tra âm thanh",
                checked = isPlaying,
                onCheckedChange = { checked ->
                    viewModel.controlAlarm(if (checked) "alarm" else "mute")
                }
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = MaterialTheme.spacing.medium, vertical = 5.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isMuted) "Đang tắt tạm thời" else "Bật",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Switch(
                        checked = !isMuted,
                        onCheckedChange = { checked ->
                            if (checked) {
                                viewModel.controlAlarm("mute", 0L)
                            } else {
                                showMuteSheet = true
                            }
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF4CAF50))
                    )
                }

                if (isMuted && muteRemainSeconds > 0) {
                    Text(
                        text = "Thiết bị có thể phát âm thanh cảnh báo khi có sự kiện sau khoảng ${formatMuteRemain(muteRemainSeconds)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = MaterialTheme.spacing.medium, end = MaterialTheme.spacing.medium, bottom = 12.dp)
                    )
                }
            }
        }
    }

    if (showMuteSheet) {
        MuteEventSheet(
            onOptionSelected = { duration ->
                viewModel.controlAlarm("mute", duration)
                showMuteSheet = false
            },
            onDismiss = { showMuteSheet = false }
        )
    }
}

@Composable
fun AlarmSwitchItem(
    title: String,
    checked: Boolean,
    showDivider: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.medium, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = Color(0xFF4CAF50)
            )
        )
    }
    if (showDivider) {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
            thickness = MaterialTheme.spacing.borderThin,
            color = Color.LightGray.copy(alpha = 0.5f)
        )
    }
}