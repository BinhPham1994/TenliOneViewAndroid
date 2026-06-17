package com.tenli.aiot.ui.features.setting.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tenli.aiot.R
import com.tenli.aiot.ui.component.AppDropdownMenu
import com.tenli.aiot.ui.component.AppDropdownMenuItem
import com.tenli.aiot.ui.features.setting.core.SettingScreenType
import com.tenli.aiot.ui.features.setting.core.SettingUiState
import com.tenli.aiot.ui.features.setting.core.SettingViewModel
import com.tenli.aiot.ui.features.setting.core.prepareEditGroupName
import com.tenli.aiot.ui.features.setting.core.toggleAddDeviceMenu
import com.tenli.aiot.ui.features.setting.core.toggleCameraMenu
import com.tenli.aiot.ui.features.setting.core.toggleGroupMenu
import com.tenli.aiot.ui.theme.spacing

@Composable
fun SettingTopHeader(
    uiState: SettingUiState,
    viewModel: SettingViewModel,
    onBack: () -> Unit,
    onDeleteClick: (() -> Unit)? = null
) {
    val currentScreen = uiState.currentScreen
    val title = uiState.title
    val groupState = uiState.group
    val displayTitle = when (currentScreen) {
        SettingScreenType.GroupDetail -> uiState.group.selectedGroup?.displayName ?: title
        SettingScreenType.EditGroupName -> "Sửa tên nhà"
        else -> title
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(MaterialTheme.spacing.iconExtraLarge), contentAlignment = Alignment.Center) {
            if (currentScreen != SettingScreenType.Main) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Back",
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Text(
            text = displayTitle,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            maxLines = 1
        )

        Box(modifier = Modifier.size(MaterialTheme.spacing.iconExtraLarge), contentAlignment = Alignment.Center) {
            when (currentScreen) {
                SettingScreenType.Group -> {
                    Box {
                        IconButton(onClick = { viewModel.toggleGroupMenu(true) }) {
                            Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(MaterialTheme.spacing.iconLarge))
                        }
                        AppDropdownMenu(
                            expanded = groupState.isGroupMenuExpanded,
                            onDismissRequest = { viewModel.toggleGroupMenu(false) }
                        ) {
                            AppDropdownMenuItem(
                                text = "Tạo nhà",
                                iconRes = R.drawable.home,
                                onClick = {
                                    viewModel.toggleGroupMenu(false)
                                    viewModel.navigateTo(SettingScreenType.CreateGroup, "Tạo nhà mới")
                                }
                            )
                            AppDropdownMenuItem(
                                text = "Gia nhập",
                                iconRes = R.drawable.edit_ai_active,
                                showDivider = false,
                                onClick = {
                                    viewModel.toggleGroupMenu(false)
                                    viewModel.navigateTo(SettingScreenType.JoinGroup, "Gia nhập nhà")
                                }
                            )
                        }
                    }
                }

                SettingScreenType.GroupDetail -> {
                    val group = groupState.selectedGroup?.group
                    val isOwner = group?.userRequestRole == "owner"
                    if (isOwner && title != "Nhà của tôi") {
                        IconButton(onClick = {
                            viewModel.prepareEditGroupName()
                            viewModel.navigateTo(SettingScreenType.EditGroupName, "Sửa tên nhà")
                        }) {
                            Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(MaterialTheme.spacing.iconMedium))
                        }
                    }
                }

                SettingScreenType.DevicesManagement -> {
                    Box {
                        IconButton(onClick = { viewModel.toggleAddDeviceMenu(true) }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Device",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(MaterialTheme.spacing.iconLarge)
                            )
                        }

                        AppDropdownMenu(
                            expanded = uiState.box.isAddDeviceMenuExpanded,
                            onDismissRequest = { viewModel.toggleAddDeviceMenu(false) }
                        ) {
                            AppDropdownMenuItem(
                                text = "Quét trong LAN",
                                iconRes = R.drawable.no_internet,
                                onClick = {
                                    viewModel.toggleAddDeviceMenu(false)
                                    viewModel.navigateTo(SettingScreenType.LanScan, "Danh sách thiết bị")
                                }
                            )

                            AppDropdownMenuItem(
                                text = "Nhập thủ công",
                                iconRes = R.drawable.edit_ai_active,
                                onClick = {
                                    viewModel.toggleAddDeviceMenu(false)
                                    viewModel.navigateTo(SettingScreenType.AddDeviceManual, "Nhập thủ công")
                                }
                            )

                            AppDropdownMenuItem(
                                text = "Ảnh QRCode",
                                iconRes = R.drawable.iot,
                                showDivider = false,
                                onClick = {
                                    viewModel.toggleAddDeviceMenu(false)
                                }
                            )
                        }
                    }
                }

                SettingScreenType.CameraManagement -> {
                    Box {
                        IconButton(onClick = { viewModel.toggleCameraMenu(true) }) {
                            Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(MaterialTheme.spacing.iconLarge))
                        }
                        AppDropdownMenu(
                            expanded = uiState.box.isCameraMenuExpanded,
                            onDismissRequest = { viewModel.toggleCameraMenu(false) }
                        ) {
                            AppDropdownMenuItem(
                                text = "Tìm kiếm ONVIF",
                                iconRes = R.drawable.scan_lan,
                                onClick = {
                                    viewModel.toggleCameraMenu(false)
                                    // viewModel.navigateTo(...)
                                }
                            )
                            AppDropdownMenuItem(
                                text = "Nhập theo hãng",
                                iconRes = R.drawable.edit_ai_active,
                                onClick = {
                                    viewModel.toggleCameraMenu(false)
                                    viewModel.navigateTo(SettingScreenType.AddCameraBrand, "Nhập theo hãng")
                                }
                            )
                            AppDropdownMenuItem(
                                text = "Nhập thủ công",
                                iconRes = R.drawable.edit_ai_active,
                                showDivider = false,
                                onClick = {
                                    viewModel.toggleCameraMenu(false)
                                    viewModel.navigateTo(SettingScreenType.AddCameraManual, "Nhập thủ công")
                                }
                            )
                        }
                    }
                }

                SettingScreenType.ScriptManagement -> {
                    Box {
                        IconButton(onClick = { viewModel.toggleScriptMenu(true) }) {
                            Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(MaterialTheme.spacing.iconLarge))
                        }
                        AppDropdownMenu(
                            expanded = uiState.box.isScriptMenuExpanded,
                            onDismissRequest = { viewModel.toggleScriptMenu(false) }
                        ) {
                            AppDropdownMenuItem(
                                text = "Kịch bản an ninh",
                                iconRes = R.drawable.script,
                                showDivider = false,
                                onClick = {
                                    viewModel.toggleScriptMenu(false)
                                    viewModel.navigateTo(SettingScreenType.AddScript, "Thêm kịch bản an ninh")
                                }
                            )
                        }
                    }
                }

                SettingScreenType.AiSensorWizard,
                SettingScreenType.AiLogicWizard,
                SettingScreenType.EditCamera,
                SettingScreenType.EditScript -> {
                    val shouldShowDelete = (currentScreen == SettingScreenType.EditCamera ||
                            currentScreen == SettingScreenType.EditScript ||
                            uiState.box.editingMonitorItem != null)

                    if (shouldShowDelete) {
                        IconButton(onClick = { onDeleteClick?.invoke() }) {
                            Icon(
                                painter = painterResource(R.drawable.delete),
                                contentDescription = "Delete",
                                tint = Color.Red.copy(alpha = 0.8f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                SettingScreenType.AiSetting -> {
                    IconButton(onClick = {
                        viewModel.navigateTo(SettingScreenType.SelectAiTask, "Chọn bài AI")
                    }) {
                        Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(MaterialTheme.spacing.iconLarge))
                    }
                }

                else -> Spacer(modifier = Modifier.size(MaterialTheme.spacing.iconExtraLarge))
            }
        }
    }
}