package com.tenli.aiot.ui.features.setting.screens.device.setting

import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tenli.aiot.R
import com.tenli.aiot.ui.features.setting.components.ConfigInputField
import com.tenli.aiot.ui.features.setting.core.SettingViewModel
import com.tenli.aiot.ui.features.setting.core.updateStorageConfig
import com.tenli.aiot.ui.features.setting.screens.device.info.DeviceInfoGroup
import com.tenli.aiot.ui.features.setting.screens.device.info.DeviceInfoSectionTitle
import com.tenli.aiot.ui.theme.spacing

@Composable
fun StorageConfigScreen(viewModel: SettingViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val data = uiState.box.systemSetting?.settings
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Khởi tạo State từ dữ liệu Box
    var videoDays by remember(data) { mutableStateOf(data?.videoRetainDays?.toInt()?.toString() ?: "") }
    var imageDays by remember(data) { mutableStateOf(data?.imageRetainDays?.toInt()?.toString() ?: "") }
    var dataDays by remember(data) { mutableStateOf(data?.dataRetainDays?.toInt()?.toString() ?: "") }
    var diskPercent by remember(data) { mutableStateOf(data?.diskFullPercent?.toInt()?.toString() ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(bottom = MaterialTheme.spacing.large)
        ) {
            // 1. Hiển thị thông báo nếu thiết bị offline
            if (data == null) {
                StorageOfflineBanner()
            }

            // 2. Nhóm: Thời gian lưu trữ
            DeviceInfoSectionTitle("Thời gian lưu trữ")
            DeviceInfoGroup {
                ConfigInputField(
                    label = "Thời gian lưu trữ video (ngày)",
                    value = videoDays,
                    onValueChange = { if (it.all { c -> c.isDigit() }) videoDays = it },
                    onClear = { videoDays = "" },
                    keyboardType = KeyboardType.Number,
                    placeholder = "30"
                )
                StorageDivider()
                ConfigInputField(
                    label = "Thời gian lưu trữ ảnh (ngày)",
                    value = imageDays,
                    onValueChange = { if (it.all { c -> c.isDigit() }) imageDays = it },
                    onClear = { imageDays = "" },
                    keyboardType = KeyboardType.Number,
                    placeholder = "30"
                )
                StorageDivider()
                ConfigInputField(
                    label = "Thời gian lưu trữ dữ liệu (ngày)",
                    value = dataDays,
                    onValueChange = { if (it.all { c -> c.isDigit() }) dataDays = it },
                    onClear = { dataDays = "" },
                    keyboardType = KeyboardType.Number,
                    placeholder = "30"
                )
            }

            // 3. Nhóm: Bảo trì hệ thống
            DeviceInfoSectionTitle("Giới hạn hệ thống")
            DeviceInfoGroup {
                ConfigInputField(
                    label = "Tự động xóa khi dung lượng đầy (%)",
                    value = diskPercent,
                    onValueChange = { if (it.all { c -> c.isDigit() }) diskPercent = it },
                    onClear = { diskPercent = "" },
                    keyboardType = KeyboardType.Number,
                    placeholder = "80"
                )
            }

            Text(
                text = "Hệ thống sẽ tự động dọn dẹp các tệp tin cũ nhất khi chạm ngưỡng dung lượng thiết lập phía trên.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.large, vertical = 12.dp)
            )
        }

        // 4. Nút Lưu cố định ở cuối
        Box(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.updateStorageConfig(
                        videoDays = videoDays,
                        imageDays = imageDays,
                        dataDays = dataDays,
                        diskPercent = diskPercent,
                        onSuccess = { viewModel.navigateBack() }
                    )
                },
                enabled = data != null && !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(MaterialTheme.spacing.radiusMedium),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(MaterialTheme.spacing.iconMedium), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Lưu cấu hình", style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}

@Composable
fun StorageDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
        thickness = MaterialTheme.spacing.borderThin,
        color = Color.LightGray.copy(alpha = 0.3f)
    )
}

@Composable
fun StorageOfflineBanner() {
    Surface(
        modifier = Modifier.padding(MaterialTheme.spacing.medium).fillMaxWidth(),
        color = Color(0xFFFFEBEE),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.reset_icon),
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
            Text(
                text = "Thiết bị ngoại tuyến. Không thể cập nhật cấu hình.",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}