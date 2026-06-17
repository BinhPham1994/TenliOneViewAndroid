package com.tenli.aiot.ui.features.setting.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.tenli.aiot.R
import com.tenli.aiot.model.network.DeviceItem
import com.tenli.aiot.ui.component.SafeAsyncImage
import com.tenli.aiot.ui.features.setting.screens.device.ai.AiSensorViewModel
import com.tenli.aiot.ui.features.setting.screens.device.ai.DrawToolButton
import com.tenli.aiot.ui.theme.spacing

@Composable
fun StepDrawZone(
    viewModel: AiSensorViewModel, // Đổi sang AiSensorViewModel
    device: DeviceItem,           // Nhận thêm device để load ảnh camera [cite: 2026-03-08]
    title: String,
    isInclude: Boolean
) {
    // SỬA: Collect trực tiếp từ AiSensorViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val selectedCamera = uiState.selectedCamera
    val savedMasks = if (isInclude) uiState.includeMasks else uiState.excludeMasks
    val currentPoints = uiState.currentPoints

    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = title, modifier = Modifier.padding(MaterialTheme.spacing.medium), style = MaterialTheme.typography.titleSmall)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16 / 9f)
                .background(Color.Black)
                .pointerInput(isInclude) {
                    detectTapGestures { offset ->
                        // GỌI HÀM: Giờ là hàm chính chủ của AiSensorViewModel [cite: 2026-03-08]
                        viewModel.addAiPoint(offset, size)
                    }
                }
        ) {
            if (selectedCamera != null) {
                SafeAsyncImage(
                    url = "${device.publicTargetURI}/api/CameraImage/${selectedCamera.id}",
                    deviceKey = device.key
                )
            }

            // Vẽ Canvas (Component này giữ nguyên logic)
            AiDrawCanvas(
                savedMasks = savedMasks,
                currentPoints = currentPoints,
                isInclude = isInclude
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DrawToolButton(R.drawable.refresh, "Vẽ lại") {
                viewModel.resetCurrentDrawing()
            }
            DrawToolButton(R.drawable.camera_icon, "Thêm vùng") {
                viewModel.finishAiZone(isInclude)
            }
            DrawToolButton(R.drawable.delete, "Xóa hết") {
                viewModel.clearAiZones(isInclude)
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

/**
 * AiDrawCanvas: Giữ nguyên logic vẽ, chỉ đảm bảo các tham số truyền vào đúng kiểu dữ liệu
 */
@Composable
private fun AiDrawCanvas(
    savedMasks: List<List<Double>>,
    currentPoints: List<Offset>,
    isInclude: Boolean
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val circleRadius = MaterialTheme.spacing.extraSmall

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Vẽ các vùng đã chốt (Masks)
        savedMasks.forEach { mask ->
            val path = Path().apply {
                for (i in mask.indices step 2) {
                    if (i + 1 < mask.size) {
                        val px = (mask[i] * w).toFloat()
                        val py = (mask[i + 1] * h).toFloat()
                        if (i == 0) moveTo(px, py) else lineTo(px, py)
                    }
                }
                close()
            }
            val baseColor = if (isInclude) primaryColor else Color.Red
            drawPath(path, color = baseColor.copy(alpha = 0.3f))
            drawPath(path, color = baseColor, style = Stroke(width = 2.dp.toPx()))
        }

        // Vẽ vùng đang vẽ dở (Current Points)
        if (currentPoints.isNotEmpty()) {
            val drawPath = Path().apply {
                currentPoints.forEachIndexed { index, point ->
                    val px = point.x * w
                    val py = point.y * h
                    if (index == 0) moveTo(px, py) else lineTo(px, py)
                }
                if (currentPoints.size >= 3) close()
            }

            drawPath(drawPath, color = Color.Yellow.copy(alpha = 0.2f))
            drawPath(drawPath, color = Color.Yellow, style = Stroke(width = 2.dp.toPx()))

            // Vẽ các điểm nút
            currentPoints.forEach { point ->
                drawCircle(
                    color = Color.Yellow,
                    radius = circleRadius.toPx(),
                    center = Offset(point.x * w, point.y * h)
                )
            }
        }
    }
}