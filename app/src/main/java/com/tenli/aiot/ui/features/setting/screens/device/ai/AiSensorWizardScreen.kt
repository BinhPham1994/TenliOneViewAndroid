package com.tenli.aiot.ui.features.setting.screens.device.ai

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tenli.aiot.R
import com.tenli.aiot.model.network.DeviceItem
import com.tenli.aiot.ui.component.AppConfirmDialog
import com.tenli.aiot.ui.component.SafeAsyncImage
import com.tenli.aiot.ui.features.setting.components.SensitivityBottomSheet
import com.tenli.aiot.ui.features.setting.components.SlimEditField
import com.tenli.aiot.ui.features.setting.components.StepDrawZone
import com.tenli.aiot.ui.theme.spacing

@Composable
fun AiSensorWizardScreen(viewModel: AiSensorViewModel, device: DeviceItem, onSuccess: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val step = uiState.currentWizardStep

    if (uiState.showDeleteConfirm) {
        AppConfirmDialog(
            title = "Xác nhận xóa",
            message = "Bạn có chắc chắn muốn xóa bài AI '${uiState.monitorName}' này không? Hành động này không thể hoàn tác.",
            confirmText = "Xóa ngay",
            cancelText = "Hủy",
            confirmColor = MaterialTheme.colorScheme.error,
            iconRes = R.drawable.ai_icon,
            onConfirm = {
                viewModel.confirmDeleteMonitor {
                    onSuccess()
                }
            },
            onDismiss = { viewModel.dismissDeleteDialog() }
        )
    }

    // 1. Dialog xác nhận quay lại (Cảnh báo mất dữ liệu vùng vẽ)
    if (uiState.showBackConfirm) {
        AppConfirmDialog(
            title = "Quay lại chọn Camera?",
            message = "Tất cả các vùng đã khoanh sẽ bị xóa. Bạn có chắc chắn muốn quay lại?",
            confirmText = "Quay lại",
            cancelText = "Ở lại",
            confirmColor = MaterialTheme.colorScheme.error, // Màu đỏ vì hành động này gây mất vùng đã vẽ
            iconRes = R.drawable.introduce_icon, // Hoặc dùng icon cảnh báo nếu có
            onConfirm = { viewModel.confirmResetAndBack() },
            onDismiss = { viewModel.dismissDialogs() }
        )
    }

// 2. Dialog cảnh báo vùng vẽ chưa xong (Ít hơn 3 điểm)
    if (uiState.showIncompleteZoneConfirm) {
        AppConfirmDialog(
            title = "Vùng vẽ chưa hoàn tất",
            message = "Vùng đang vẽ có ít hơn 3 điểm và sẽ bị xóa. Tiếp tục sang bước sau?",
            confirmText = "Tiếp tục",
            cancelText = "Vẽ tiếp",
            confirmColor = MaterialTheme.colorScheme.primary, // Màu xanh để khuyến khích tiến bước tiếp theo
            iconRes = R.drawable.ai_icon,
            onConfirm = {
                viewModel.resetCurrentDrawing()
                viewModel.dismissDialogs()
                viewModel.nextWizardStep()
            },
            onDismiss = { viewModel.dismissDialogs() }
        )
    }

    BackHandler(enabled = step > 1) {
        viewModel.handleBackWithValidation()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        WizardStepIndicator(currentStep = step)

        Box(modifier = Modifier.weight(1f)) {
            when (step) {
                1 -> Step1SelectCamera(viewModel, device)
                2 -> StepDrawZone(viewModel, device, "Bước 2: Khoanh vùng phát hiện", isInclude = true)
                3 -> StepDrawZone(viewModel, device, "Bước 3: Khoanh vùng gây nhiễu", isInclude = false)
                4 -> Step4ConfigureParams(viewModel)
            }
        }

        WizardNavigationButtons(
            onBack = { viewModel.handleBackWithValidation() },
            onNext = {
                when (step) {
                    1 -> {
                        val currentCam = uiState.selectedCamera
                        val editingId = uiState.editingCameraId

                        if (currentCam != null) {
                            viewModel.nextWizardStep()
                        } else if (editingId != null) {
                            val camInList = uiState.cameras.find { it.id == editingId }
                            if (camInList != null) {
                                viewModel.selectCamera(camInList)
                                viewModel.nextWizardStep()
                            } else {
                                viewModel.onShowSnackbar("Đang tải dữ liệu camera...")
                            }
                        } else {
                            viewModel.onShowSnackbar("Vui lòng chọn một camera")
                        }
                    }

                    2 -> viewModel.handleNextWithValidation(isInclude = true)
                    3 -> viewModel.handleNextWithValidation(isInclude = false)
                    4 -> {
                        if (uiState.monitorName.isNotBlank()) {
                            viewModel.saveSensorMonitor { onSuccess() }
                        } else {
                            viewModel.onShowSnackbar("Vui lòng nhập tên bài AI")
                        }
                    }
                }
            },
            nextLabel = if (step == 4) "Lưu" else "Tiếp theo"
        )
    }

    if (uiState.isLoading && step == 4) {
        LoadingSavingOverlay()
    }
}

@Composable
private fun LoadingSavingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center
    ) {
        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(MaterialTheme.spacing.large), horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                Text("Đang lưu cấu hình...", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun Step1SelectCamera(viewModel: AiSensorViewModel, device: DeviceItem) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cameras = uiState.cameras

    LaunchedEffect(Unit) {
        if (cameras.isEmpty()) {
            viewModel.fetchCameras()
        }
    }

    Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
        Text(text = "Bước 1: Chọn camera", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp), contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(cameras, key = { it.id }) { camera ->
                    // Ép kiểu về String để so sánh an toàn tuyệt đối [cite: 2026-03-09]
                    val currentCamId = uiState.selectedCamera?.id?.toString()
                    val editingCamId = uiState.editingCameraId?.toString()
                    val targetCamId = camera.id.toString()

                    val isSelected = if (currentCamId != null) {
                        currentCamId == targetCamId
                    } else {
                        uiState.editingMonitorId != null && editingCamId == targetCamId
                    }

                    CameraSelectionCard(
                        name = camera.name,
                        snapshotUrl = "${device.publicTargetURI}/api/CameraImage/${camera.id}",
                        authKey = device.key,
                        isSelected = isSelected // Viền xanh sẽ dựa trên kết quả này [cite: 2026-03-08]
                    ) {
                        viewModel.selectCamera(camera)
                    }
                }
            }
        }
    }
}

@Composable
fun Step4ConfigureParams(viewModel: AiSensorViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSensitivitySheet by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = MaterialTheme.spacing.medium)
                .verticalScroll(rememberScrollState())
        ) {
            Text(text = "Bước 4: Cấu hình thông số", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(vertical = MaterialTheme.spacing.medium))

            SettingSection(label = "Thông tin chung") {
                SlimEditField(value = uiState.monitorName, onValueChange = { viewModel.updateMonitorName(it) }, placeholder = "Nhập tên bài AI...")
                HorizontalDivider(thickness = MaterialTheme.spacing.borderThin, color = Color.LightGray.copy(alpha = 0.5f))
                SettingSwitchItem("Trạng thái AI", uiState.aiEnabled) { viewModel.updateAiEnabled(it) }
            }

            SettingSection(label = "Cài đặt cảnh báo") {
                SettingSwitchItem("Kích hoạt cảnh báo", uiState.alarmEnabled) { viewModel.updateAlarmEnabled(it) }
            }

            SettingSection(label = "Độ nhạy") {
                SettingSelectorItem(title = "Độ nhạy", value = getSensitivityLabel(uiState.sensitivity)) { showSensitivitySheet = true }
            }

            SettingSection(label = "Cài đặt MQTT") {
                SettingSwitchItem("Trạng thái", uiState.mqttEnabled) { viewModel.updateMqttEnabled(it) }
                HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.spacing.extraSmall), thickness = MaterialTheme.spacing.borderThin, color = Color.LightGray.copy(alpha = 0.5f))
                SlimEditField(value = uiState.mqttTopic, onValueChange = { viewModel.updateMqttTopic(it) }, placeholder = "Ví dụ: aibox/person")
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        if (showSensitivitySheet) {
            SensitivityBottomSheet(
                selectedValue = uiState.sensitivity,
                onValueSelected = { viewModel.updateAiSensitivity(it); showSensitivitySheet = false },
                onDismiss = { showSensitivitySheet = false }
            )
        }
    }
}

@Composable
fun SettingSection(label: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = Color.Gray,
            modifier = Modifier.padding(start = MaterialTheme.spacing.extraSmall, bottom = MaterialTheme.spacing.small)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(MaterialTheme.spacing.radiusMedium),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium)) {
                content()
            }
        }
    }
}

@Composable
fun WizardStepIndicator(currentStep: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(4) { index ->
            val step = index + 1
            val isActive = step <= currentStep
            val isCurrent = step == currentStep

            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(if (isActive) MaterialTheme.colorScheme.primary else Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = step.toString(),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (index < 3) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(2.dp)
                        .background(if (step < currentStep) MaterialTheme.colorScheme.primary else Color.LightGray)
                )
            }
        }
    }
}

@Composable
fun CameraSelectionCard(
    name: String,
    snapshotUrl: String,
    authKey: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, // Viền xanh nếu đang chọn [cite: 2026-03-08]
                shape = RoundedCornerShape(5.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.LightGray.copy(alpha = 0.3f))
            ) {
                SafeAsyncImage(
                    url = snapshotUrl,
                    deviceKey = authKey
                )
            }
            Text(
                text = name,
                modifier = Modifier.padding(5.dp),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun WizardNavigationButtons(
    onBack: () -> Unit,
    onNext: () -> Unit,
    nextLabel: String = "Tiếp theo" // Thêm mặc định là Tiếp theo [cite: 2026-03-07]
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(MaterialTheme.spacing.medium),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
    ) {
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(MaterialTheme.spacing.radiusSmall),
            border = BorderStroke(MaterialTheme.spacing.borderMedium, MaterialTheme.colorScheme.primary)
        ) {
            Text("Quay lại", color = MaterialTheme.colorScheme.primary)
        }
        Button(
            onClick = onNext,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(MaterialTheme.spacing.radiusSmall),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(nextLabel, color = Color.White) // Hiển thị nhãn động
        }
    }
}

@Composable
fun SettingSwitchItem(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = MaterialTheme.spacing.extraSmall), // Giảm padding vì đã có padding của Card bao ngoài [cite: 2026-03-08]
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
                checkedTrackColor = MaterialTheme.colorScheme.primary, // Xanh lá giống mẫu [cite: 2026-03-08]
                uncheckedTrackColor = Color(0xFFE0E0E0)
            )
        )
    }
}

@Composable
fun SettingSelectorItem(title: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Text(text = value, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
fun DrawToolButton(
    iconRes: Int,
    label: String,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(MaterialTheme.spacing.radiusSmall))
            .clickable { onClick() }
            .padding(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.DarkGray,
        )
    }
}