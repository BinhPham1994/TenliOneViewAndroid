package com.tenli.aiot.ui.features.setting.screens.device.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tenli.aiot.R
import com.tenli.aiot.model.network.DeviceItem
import com.tenli.aiot.ui.component.AppConfirmDialog
import com.tenli.aiot.ui.theme.spacing

@Composable
fun AiLogicWizardScreen(viewModel: AiSensorViewModel, device: DeviceItem, onSuccess: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val step = uiState.currentWizardStep

    if (uiState.showDeleteConfirm) {
        AppConfirmDialog(
            title = "Xác nhận xóa",
            message = "Bạn có chắc chắn muốn xóa bài AI '${uiState.monitorName}' này không? Hành động này không thể hoàn tác.",
            confirmText = "Xóa ngay",
            cancelText = "Để sau",
            confirmColor = MaterialTheme.colorScheme.error, // Màu đỏ đậm cho hành động xóa
            iconRes = R.drawable.delete, // Bạn nên dùng icon thùng rác nếu có, hoặc để mặc định ai_icon
            onConfirm = {
                viewModel.confirmDeleteMonitor {
                    onSuccess()
                }
            },
            onDismiss = { viewModel.dismissDeleteDialog() }
        )
    }

// 2. Dialog xác nhận QUAY LẠI (Sử dụng màu Xanh thương hiệu Tenli)
    if (uiState.showBackConfirm) {
        AppConfirmDialog(
            title = "Quay lại chọn Camera?",
            message = "Các tiến trình đã chọn sẽ không bị mất, nhưng bạn có chắc chắn muốn quay lại bước 1?",
            confirmText = "Quay lại",
            cancelText = "Ở lại",
            confirmColor = MaterialTheme.colorScheme.primary,
            iconRes = R.drawable.ai_icon,
            onConfirm = { viewModel.confirmResetAndBack() },
            onDismiss = { viewModel.dismissDialogs() }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        WizardStepIndicator3(currentStep = step)
        Box(modifier = Modifier.weight(1f)) {
            when (step) {
                1 -> Step1SelectCamera(viewModel, device)
                2 -> Step2SelectInputProcesses(viewModel, device)
                3 -> Step4ConfigureParams(viewModel) // Dùng lại UI bước cấu hình cuối [cite: 2026-03-08]
            }
        }

        WizardNavigationButtons(
            onBack = { viewModel.previousWizardStep() },
            onNext = {
                when (step) {
                    1 -> {
                        val currentCam = uiState.selectedCamera
                        val editingId = uiState.editingCameraId // Lấy ID camera cũ đang sửa [cite: 2026-03-09]

                        if (currentCam != null) {
                            // Trường hợp người dùng đã chọn camera (mới hoặc cũ)
                            viewModel.fetchSensorsForLogic()
                            viewModel.nextWizardStep()
                        } else if (editingId != null) {
                            // Trường hợp ĐANG SỬA: Tự động tìm và gán camera cũ nếu người dùng không bấm gì [cite: 2026-03-09]
                            val camInList = uiState.cameras.find { it.id == editingId }
                            if (camInList != null) {
                                viewModel.selectCamera(camInList) // Gán vào selectedCamera
                                viewModel.fetchSensorsForLogic()
                                viewModel.nextWizardStep()
                            } else {
                                viewModel.onShowSnackbar("Đang tải dữ liệu camera...")
                            }
                        } else {
                            viewModel.onShowSnackbar("Vui lòng chọn camera")
                        }
                    }

                    2 -> {
                        if (uiState.selectedInputSensorIds.isNotEmpty()) {
                            viewModel.nextWizardStep()
                        } else {
                            viewModel.onShowSnackbar("Vui lòng chọn ít nhất một tiến trình")
                        }
                    }

                    3 -> viewModel.saveLogicMonitor(onSuccess)
                }
            },
            nextLabel = if (step == 3) "Lưu" else "Tiếp theo"
        )
    }
}

@Composable
fun Step2SelectInputProcesses(viewModel: AiSensorViewModel, device: DeviceItem) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sensors = uiState.availableInputSensors

    Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
        Text(text = "Bước 2: Chọn tiến trình đầu vào", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(sensors) { item ->
                    val isSelected = uiState.selectedInputSensorIds.contains(item.monitor.id)
                    CameraSelectionCard(
                        name = item.monitor.name ?: "Không tên",
                        snapshotUrl = item.snapshotUrl,
                        authKey = device.key,
                        isSelected = isSelected
                    ) {
                        viewModel.toggleInputSensor(item.monitor.id)
                    }
                }
            }
        }
    }
}

@Composable
fun WizardStepIndicator3(currentStep: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 30.dp, vertical = 10.dp), // Tăng padding ngang cho thoáng
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(3) { index ->
            val step = index + 1
            val isActive = step <= currentStep
            val isCompleted = step < currentStep

            // 1. Vòng tròn số thứ tự
            Box(
                modifier = Modifier
                    .size(MaterialTheme.spacing.iconLarge) // Tăng nhẹ kích thước cho dễ nhìn
                    .clip(CircleShape)
                    .background(if (isActive) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = step.toString(),
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall, // Font đậm hơn chút
                    fontWeight = FontWeight.Bold
                )
            }

            // 2. Đường kẻ nối (Chỉ vẽ giữa bước 1-2 và 2-3)
            if (index < 2) {
                Box(
                    modifier = Modifier
                        .width(70.dp) // Độ dài đường nối dài hơn bản 4 bước
                        .height(2.dp)
                        .background(if (isCompleted) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f))
                )
            }
        }
    }
}