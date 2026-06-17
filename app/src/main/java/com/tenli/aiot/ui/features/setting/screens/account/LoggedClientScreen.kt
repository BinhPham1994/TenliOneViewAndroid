package com.tenli.aiot.ui.features.setting.screens.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tenli.aiot.ui.features.setting.components.ClientItem
import com.tenli.aiot.ui.features.setting.core.SettingViewModel
import com.tenli.aiot.ui.features.setting.core.viewClientDetail
import com.tenli.aiot.ui.theme.spacing

@Composable
fun LoggedDevicesScreen(viewModel: SettingViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val currentDevice = uiState.client.clientDevices.find { it.isCurrent }
    val otherDevices = uiState.client.clientDevices.filter { !it.isCurrent && it.status == 0 }
    val loggedOutDevices = uiState.client.clientDevices.filter { it.status == 1 }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(MaterialTheme.spacing.medium)
    ) {
        if (currentDevice != null) {
            item { Text("Thiết bị hiện tại", style = MaterialTheme.typography.labelLarge, color = Color.Gray, modifier = Modifier.padding(bottom = MaterialTheme.spacing.small)) }
            item {
                Card(shape = RoundedCornerShape(MaterialTheme.spacing.radiusMedium), elevation = CardDefaults.cardElevation(0.dp)) {
                    ClientItem(currentDevice, showDivider = false) {
                        viewModel.viewClientDetail(currentDevice)
                    }
                }
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
            }
        }

        if (otherDevices.isNotEmpty()) {
            item { Text("Thiết bị khác đang đăng nhập", style = MaterialTheme.typography.labelLarge, color = Color.Gray, modifier = Modifier.padding(bottom = MaterialTheme.spacing.small)) }
            item {
                Card(shape = RoundedCornerShape(MaterialTheme.spacing.radiusMedium), elevation = CardDefaults.cardElevation(0.dp)) {
                    Column {
                        otherDevices.forEachIndexed { index, device ->
                            ClientItem(device, showDivider = index != otherDevices.size - 1) {
                                viewModel.viewClientDetail(device)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
            }
        }

        if (loggedOutDevices.isNotEmpty()) {
            item { Text("Thiết bị đã đăng xuất", style = MaterialTheme.typography.labelLarge, color = Color.Gray, modifier = Modifier.padding(bottom = MaterialTheme.spacing.small)) }
            item {
                Card(shape = RoundedCornerShape(MaterialTheme.spacing.radiusMedium), elevation = CardDefaults.cardElevation(0.dp)) {
                    Column {
                        loggedOutDevices.forEachIndexed { index, device ->
                            ClientItem(device, showDivider = index != loggedOutDevices.size - 1) {
                                viewModel.viewClientDetail(device) // Vẫn cho phép xem chi tiết [cite: 2026-03-03]
                            }
                        }
                    }
                }
            }
        }
    }
}