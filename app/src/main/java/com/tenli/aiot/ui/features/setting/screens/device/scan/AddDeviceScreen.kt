package com.tenli.aiot.ui.features.setting.screens.device.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tenli.aiot.data.repository.DataRepository
import com.tenli.aiot.ui.features.setting.core.SettingScreenType
import com.tenli.aiot.ui.features.setting.core.SettingViewModel
import com.tenli.aiot.ui.features.setting.core.navigateToAndClearStack
import com.tenli.aiot.ui.features.setting.core.startProvisioningDevice
import com.tenli.aiot.ui.features.setting.screens.device.info.DeviceInfoGroup
import com.tenli.aiot.ui.features.setting.screens.device.info.DeviceInfoRow
import com.tenli.aiot.ui.features.setting.screens.device.info.DeviceInfoSectionTitle
import com.tenli.aiot.ui.features.setting.screens.device.info.StorageStatusView
import com.tenli.aiot.ui.features.setting.screens.device.info.formatBoxTime
import com.tenli.aiot.ui.theme.spacing

@Composable
fun AddDeviceScreen(viewModel: SettingViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    val boxInfo = uiState.box.info
    val boxState = uiState.box.state
    val ownedGroups = remember {
        DataRepository.groupList.filter { it.group.userRequestRole == "owner" }
    }
    val pendingKey = uiState.box.pendingKey ?: ""
    var deviceName by remember(boxInfo) {
        mutableStateOf(boxInfo?.deviceName?.ifEmpty { "AI Box Tenli" } ?: "AI Box Tenli")
    }

    var selectedGroup by remember(ownedGroups) {
        mutableStateOf(ownedGroups.find { it.displayName.contains("Nhà của tôi", ignoreCase = true) } ?: ownedGroups.firstOrNull())
    }
    var expandedHomeMenu by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 80.dp)
        ) {
            DeviceInfoSectionTitle("Định danh hệ thống")
            DeviceInfoGroup {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedHomeMenu = true }
                            .padding(MaterialTheme.spacing.medium),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Nhà", style = MaterialTheme.typography.bodyLarge)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = selectedGroup?.displayName ?: "Chọn nhà",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.Gray, modifier = Modifier
                                .padding(start = 2.dp)
                                .size(MaterialTheme.spacing.iconLarge))
                        }
                    }

                    DropdownMenu(
                        expanded = expandedHomeMenu,
                        onDismissRequest = { expandedHomeMenu = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        if (ownedGroups.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Không có nhà sở hữu", color = Color.Gray) },
                                onClick = { expandedHomeMenu = false }
                            )
                        } else {
                            ownedGroups.forEach { groupItem ->
                                DropdownMenuItem(
                                    text = { Text(groupItem.displayName) },
                                    onClick = {
                                        selectedGroup = groupItem
                                        expandedHomeMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium), thickness = MaterialTheme.spacing.borderThin, color = Color.LightGray.copy(alpha = 0.3f))
                EditableDeviceInfoRow(
                    label = "Tên thiết bị",
                    value = deviceName,
                    onValueChange = { deviceName = it }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium), thickness = MaterialTheme.spacing.borderThin, color = Color.LightGray.copy(alpha = 0.3f))
                DeviceInfoRow("Device Key", pendingKey)
            }

            DeviceInfoSectionTitle("Tài nguyên hệ thống")
            DeviceInfoGroup {
                StorageStatusView(boxState)
            }

            DeviceInfoSectionTitle("Thông số kỹ thuật")
            DeviceInfoGroup {
                DeviceInfoRow("Địa chỉ MAC", boxInfo?.macAddress ?: "")
                HorizontalDivider(modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium), thickness = MaterialTheme.spacing.borderThin, color = Color.LightGray.copy(alpha = 0.3f))
                DeviceInfoRow("Firmware", boxInfo?.firmwareVersion ?: "")
                HorizontalDivider(modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium), thickness = MaterialTheme.spacing.borderThin, color = Color.LightGray.copy(alpha = 0.3f))
                DeviceInfoRow("Model", boxInfo?.deviceModel ?: "")
                HorizontalDivider(modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium), thickness = MaterialTheme.spacing.borderThin, color = Color.LightGray.copy(alpha = 0.3f))
                DeviceInfoRow("Ngày giờ", formatBoxTime(boxState?.deviceTime))
                HorizontalDivider(modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium), thickness = MaterialTheme.spacing.borderThin, color = Color.LightGray.copy(alpha = 0.3f))
                DeviceInfoRow("Múi giờ", boxState?.deviceTimeZone ?: "")
            }
        }

        Button(
            onClick = {
                selectedGroup?.let { group ->
                    viewModel.startProvisioningDevice(
                        userGroupId = group.group.id,
                        newKey = pendingKey,
                        customName = deviceName,
                        onSuccess = {
                            viewModel.navigateToAndClearStack(SettingScreenType.DevicesManagement, "Quản lý thiết bị")
                        }
                    )
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(20.dp)
                .height(50.dp),
            shape = RoundedCornerShape(MaterialTheme.spacing.radiusMedium),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            enabled = !uiState.box.isLoading && boxInfo != null && selectedGroup != null
        ) {
            if (uiState.box.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(MaterialTheme.spacing.iconMedium), color = Color.White, strokeWidth = 5.dp)
            } else {
                Text("Thêm thiết bị", style = MaterialTheme.typography.titleSmall, color = Color.White)
            }
        }
    }
}

@Composable
fun EditableDeviceInfoRow(label: String, value: String, onValueChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.medium, vertical = MaterialTheme.spacing.small), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))

        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1.5f),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = Color.Transparent
            ),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                textAlign = androidx.compose.ui.text.style.TextAlign.End,
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray
            ),
            singleLine = true
        )
    }
}