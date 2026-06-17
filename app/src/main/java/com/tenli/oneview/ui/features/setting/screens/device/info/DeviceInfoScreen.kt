package com.tenli.oneview.ui.features.setting.screens.device.info

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tenli.oneview.data.repository.DataRepository
import com.tenli.oneview.model.network.BoxSystemState
import com.tenli.oneview.ui.features.setting.core.SettingViewModel
import com.tenli.oneview.ui.features.setting.core.fetchBoxInfo
import com.tenli.oneview.ui.theme.spacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DeviceInfoScreen(viewModel: SettingViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    val cloudDevice = uiState.box.selectedDeviceItem ?: return
    val groupName = DataRepository.groupList.find { it.group.id == cloudDevice.userGroupId }?.displayName ?: "ID: ${cloudDevice.userGroupId}"

    val boxInfo = uiState.box.info
    val boxState = uiState.box.state

    LaunchedEffect(Unit) {
        viewModel.fetchBoxInfo()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = MaterialTheme.spacing.extraLarge)
    ) {
        DeviceInfoSectionTitle("Định danh")
        DeviceInfoGroup {
            DeviceInfoRow("Nhà", groupName)
            HorizontalDivider(modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium), thickness = MaterialTheme.spacing.borderThin, color = Color.LightGray.copy(alpha = 0.3f))
            DeviceInfoRow("Tên thiết bị", cloudDevice.name)
            HorizontalDivider(modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium), thickness = MaterialTheme.spacing.borderThin, color = Color.LightGray.copy(alpha = 0.3f))
            DeviceInfoRow("Device Key", cloudDevice.key, isCopyable = true)
        }

        DeviceInfoSectionTitle("Tài nguyên")
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
}

@Composable
fun DeviceInfoSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(start = MaterialTheme.spacing.large, top = MaterialTheme.spacing.large, bottom = 5.dp),
    )
}

@Composable
fun DeviceInfoGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .padding(horizontal = MaterialTheme.spacing.medium)
            .clip(RoundedCornerShape(MaterialTheme.spacing.radiusMedium))
            .background(Color.White),
        content = content
    )
}

@Composable
fun DeviceInfoRow(label: String, value: String, isCopyable: Boolean = false) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isCopyable) {
                if (value.isNotEmpty() && value != "---") {
                    clipboardManager.setText(AnnotatedString(value))
                    Toast.makeText(context, "Đã sao chép: $label", Toast.LENGTH_SHORT).show()
                }
            }
            .padding(MaterialTheme.spacing.medium),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = MaterialTheme.spacing.medium)
        )
    }
}

fun formatBoxTime(timestamp: Double?): String {
    if (timestamp == null || timestamp == 0.0) return "---"
    return try {
        val sdf = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault())
        val netDate = Date((timestamp * 1000).toLong())
        sdf.format(netDate)
    } catch (_: Exception) {
        "---"
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun StorageStatusView(state: BoxSystemState?) {
    val capacity = state?.capacitySpace ?: 0L
    val available = state?.availableSpace ?: 0L
    val used = if (capacity > 0) capacity - available else 0L
    val progress = if (capacity > 0) used.toFloat() / capacity.toFloat() else 0f

    val usedGB = used.toDouble() / (1024 * 1024 * 1024)
    val totalGB = capacity.toDouble() / (1024 * 1024 * 1024)

    Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Bộ nhớ đã dùng", style = MaterialTheme.typography.bodyLarge)
            Text("${String.format("%.1f", usedGB)}GB / ${String.format("%.1f", totalGB)}GB", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(MaterialTheme.spacing.small)
                .clip(RoundedCornerShape(MaterialTheme.spacing.extraSmall)),
            color = if (progress > 0.9f) Color.Red else MaterialTheme.colorScheme.primary,
            trackColor = Color(0xFFF0F0F0)
        )
    }
}