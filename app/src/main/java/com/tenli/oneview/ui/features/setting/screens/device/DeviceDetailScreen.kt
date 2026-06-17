package com.tenli.oneview.ui.features.setting.screens.device

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tenli.oneview.R
import com.tenli.oneview.ui.component.AppConfirmDialog
import com.tenli.oneview.ui.features.setting.core.SettingScreenType
import com.tenli.oneview.ui.features.setting.core.SettingViewModel
import com.tenli.oneview.ui.features.setting.core.controlBoxSystem
import com.tenli.oneview.ui.features.setting.core.deleteDeviceFromSystem
import com.tenli.oneview.ui.theme.spacing

@Composable
fun DeviceDetailScreen(viewModel: SettingViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val device = uiState.box.selectedDeviceItem ?: return
    val scrollState = rememberScrollState()

    val infoLabel = stringResource(id = R.string.device_detail_info)
    val settingsLabel = stringResource(id = R.string.device_detail_settings)
    val aiLabel = stringResource(id = R.string.device_detail_ai)
    val cameraLabel = stringResource(id = R.string.device_detail_camera)
    val zigbeeLabel = stringResource(id = R.string.device_detail_zigbee)
    val sensorLabel = stringResource(id = R.string.device_detail_sensor)
    val scriptLabel = stringResource(id = R.string.device_detail_script)
    val soundLabel = stringResource(id = R.string.device_detail_sound)

    val actionReload = stringResource(id = R.string.device_action_reload_service)
    val actionReboot = stringResource(id = R.string.device_action_reboot_device)
    val actionDelete = stringResource(id = R.string.device_action_delete)
    val actionReset = stringResource(id = R.string.device_action_reset)
    val deleteAndResetStr = stringResource(id = R.string.lbl_delete_and_reset)

    val msgReloadConfirm = stringResource(id = R.string.device_dialog_reload_msg)
    val msgRebootConfirm = stringResource(id = R.string.device_dialog_reboot_msg)
    val msgDeleteConfirm = stringResource(id = R.string.device_dialog_delete_confirm_msg)
    val msgReloadSuccess = stringResource(id = R.string.device_msg_reload_success)
    val msgRebootSuccess = stringResource(id = R.string.device_msg_reboot_success)

    var showConfirmDialog by remember { mutableStateOf(false) }
    var dialogConfig by remember { mutableStateOf(DialogData()) }

    val isOwner = remember(device) {
        device.roles?.any { it.role.equals("owner", ignoreCase = true) } == true
    }
    val isTechnical = remember(device) {
        device.roles?.any { it.role.equals("technical", ignoreCase = true) } == true
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error

    if (showConfirmDialog) {
        AppConfirmDialog(
            title = dialogConfig.title,
            message = dialogConfig.message,
            confirmColor = dialogConfig.confirmColor,
            iconRes = dialogConfig.iconRes,
            onDismiss = { showConfirmDialog = false },
            onConfirm = {
                showConfirmDialog = false
                if (dialogConfig.isDeleteAction) {
                    viewModel.deleteDeviceFromSystem(isResetLocal = dialogConfig.isResetLocal)
                } else {
                    viewModel.controlBoxSystem(dialogConfig.command, dialogConfig.successMsg)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = MaterialTheme.spacing.extraLarge)
    ) {
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        DeviceDetailGroup {
            DeviceDetailItem(R.drawable.device_icon, infoLabel, Color(0xFF2196F3)) {
                viewModel.navigateTo(SettingScreenType.DeviceInformation, infoLabel)
            }
            DeviceDetailItem(R.drawable.setting, settingsLabel, Color(0xFF5C6BC0), showDivider = false) {
                viewModel.navigateTo(SettingScreenType.DeviceSettings, settingsLabel)
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

        DeviceDetailGroup {
            DeviceDetailItem(R.drawable.ai_icon, aiLabel, Color(0xFF2196F3)) {
                viewModel.navigateTo(SettingScreenType.AiSetting, aiLabel)
            }
            DeviceDetailItem(
                iconRes = R.drawable.camera_icon,
                title = cameraLabel,
                iconBgColor = Color(0xFFF4511E)
            ) {
                viewModel.navigateTo(SettingScreenType.CameraManagement, cameraLabel)
            }
            DeviceDetailItem(R.drawable.iot, zigbeeLabel, Color(0xFFFBC02D)) {
                viewModel.navigateTo(SettingScreenType.ZigbeeManagement, zigbeeLabel)
            }
            DeviceDetailItem(R.drawable.power_icon, sensorLabel, Color(0xFFE64A19)) { }
            DeviceDetailItem(
                iconRes = R.drawable.time_icon_red,
                title = scriptLabel,
                iconBgColor = Color(0xFF5C6BC0),
                showDivider = false
            ) {
                viewModel.navigateTo(SettingScreenType.ScriptManagement, scriptLabel)
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

        DeviceDetailGroup {
            DeviceDetailItem(
                iconRes = R.drawable.sound_icon,
                title = soundLabel,
                iconBgColor = Color(0xFF4CAF50),
                showDivider = false
            ) {
                viewModel.navigateTo(SettingScreenType.AlarmSound, soundLabel)
            }
        }
        if (isOwner || isTechnical) {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
            DeviceDetailGroup {
                DeviceDetailItem(R.drawable.refresh, actionReload, MaterialTheme.colorScheme.error, textColor = Color.Red) {
                    dialogConfig = DialogData(
                        title = actionReload,
                        iconRes = R.drawable.refresh,
                        message = msgReloadConfirm,
                        command = "reload",
                        confirmColor = primaryColor,
                        successMsg = msgReloadSuccess
                    )
                    showConfirmDialog = true
                }

                DeviceDetailItem(
                    iconRes = R.drawable.refresh,
                    title = actionReboot,
                    iconBgColor = MaterialTheme.colorScheme.error,
                    textColor = Color.Red,
                    showDivider = isOwner
                ) {
                    dialogConfig = DialogData(
                        title = actionReboot,
                        message = msgRebootConfirm,
                        command = "reboot",
                        iconRes = R.drawable.refresh,
                        confirmColor = primaryColor,
                        successMsg = msgRebootSuccess
                    )
                    showConfirmDialog = true
                }

                if (isOwner) {
                    DeviceDetailItem(R.drawable.delete, actionDelete, MaterialTheme.colorScheme.error, textColor = Color.Red) {
                        dialogConfig = DialogData(
                            title = actionDelete,
                            iconRes = R.drawable.delete,
                            confirmColor = errorColor,
                            message = msgDeleteConfirm,
                            isDeleteAction = true,
                            isResetLocal = true
                        )
                        showConfirmDialog = true
                    }

                    DeviceDetailItem(R.drawable.reset_icon, actionReset, MaterialTheme.colorScheme.error, textColor = Color.Red, showDivider = false) {
                        dialogConfig = DialogData(
                            title = deleteAndResetStr,
                            confirmColor = errorColor,
                            iconRes = R.drawable.delete,
                            message = msgDeleteConfirm,
                            isDeleteAction = true,
                            isResetLocal = true
                        )
                        showConfirmDialog = true
                    }
                }
            }
        }
    }
}

data class DialogData(
    val title: String = "",
    val message: String = "",
    val command: String = "",
    val successMsg: String = "",
    val isDeleteAction: Boolean = false,
    val isResetLocal: Boolean = false,
    val iconRes: Int? = null,
    val confirmColor: Color = Color(0xFF2196F3)
)

@Composable
fun DeviceDetailGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .padding(horizontal = MaterialTheme.spacing.medium)
            .clip(RoundedCornerShape(MaterialTheme.spacing.radiusMedium))
            .background(Color.White),
        content = content
    )
}

@Composable
fun DeviceDetailItem(
    iconRes: Int,
    title: String,
    iconBgColor: Color,
    value: String = "",
    textColor: Color = Color.Black,
    showDivider: Boolean = true,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }) {
        Row(
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(MaterialTheme.spacing.iconLarge)
                    .clip(RoundedCornerShape(MaterialTheme.spacing.radiusSmall))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(MaterialTheme.spacing.iconSmall),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))

            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
            )

            if (value.isNotEmpty()) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(end = 5.dp)
                )
            }

            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.outlineVariant)
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
                thickness = MaterialTheme.spacing.borderThin,
                color = Color.LightGray.copy(alpha = 0.3f)
            )
        }
    }
}
