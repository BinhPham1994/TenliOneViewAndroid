package com.tenli.oneview.ui.features.setting.screens.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.tenli.oneview.ui.features.setting.components.SettingInputField
import com.tenli.oneview.ui.features.setting.components.SettingPrimaryButton
import com.tenli.oneview.ui.features.setting.core.SettingViewModel
import com.tenli.oneview.ui.features.setting.core.logoutRemoteClient
import com.tenli.oneview.ui.theme.spacing
import com.tenli.oneview.ui.utils.DateTimeUtils.formatIsoTime

@Composable
fun ClientDetailScreen(viewModel: SettingViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val device = uiState.client.selectedDevice ?: return

    val version = "${device.information?.extend?.systemName ?: ""} ${device.information?.extend?.systemVersion ?: ""}".trim()
    val ipAddress = device.information?.remoteInfo?.remoteIP ?: "---"
    val lastAccessFormatted = formatIsoTime(device.information?.lastAccess ?: device.updatedAt)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.medium)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SettingInputField(label = "Tên thiết bị", value = device.name, enabled = false, onValueChange = {}, onClear = {})
        SettingInputField(label = "Loại thiết bị", value = device.model, enabled = false, onValueChange = {}, onClear = {})
        SettingInputField(label = "Phiên bản", value = version.ifEmpty { "---" }, enabled = false, onValueChange = {}, onClear = {})
        SettingInputField(label = "IP", value = ipAddress, enabled = false, onValueChange = {}, onClear = {})
        SettingInputField(label = "Lần truy cập cuối", value = lastAccessFormatted, enabled = false, onValueChange = {}, onClear = {})

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraLarge))

        if (!device.isCurrent && device.status == 0) {
            SettingPrimaryButton(
                text = "Đăng xuất thiết bị này",
                isLoading = uiState.client.isLoggingOutDevice,
                onClick = { viewModel.logoutRemoteClient(device.id) }
            )
        } else {
            val noteText = if (device.isCurrent) "Đây là thiết bị bạn đang sử dụng" else "Thiết bị này đã được đăng xuất"
            Text(
                text = noteText,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = MaterialTheme.spacing.medium)
            )
        }
    }
}