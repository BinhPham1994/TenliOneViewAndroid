package com.tenli.aiot.ui.features.setting.screens.device

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tenli.aiot.model.network.DeviceItem
import com.tenli.aiot.ui.component.CommonEmptyState
import com.tenli.aiot.ui.features.setting.components.DeviceItemRow
import com.tenli.aiot.ui.features.setting.core.SettingViewModel
import com.tenli.aiot.ui.features.setting.core.fetchDevices
import com.tenli.aiot.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesManagementScreen(
    viewModel: SettingViewModel, onDeviceDetail: (DeviceItem) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing = uiState.box.isLoading

    PullToRefreshBox(
        isRefreshing = isRefreshing, onRefresh = {
            viewModel.fetchDevices()
        }, modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = MaterialTheme.spacing.extraLarge)
        ) {
            if (uiState.box.deviceGroups.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxHeight(), contentAlignment = Alignment.Center
                    ) {
                        CommonEmptyState(
                            text = "Bạn chưa có thiết bị AI nào.\nChọn + ở góc trên để thêm mới"
                        )
                    }
                }
            } else {
                uiState.box.deviceGroups.forEach { groupDisplay ->
                    item {
                        Text(
                            text = groupDisplay.groupName, style = MaterialTheme.typography.titleSmall, color = Color.Gray, modifier = Modifier.padding(start = MaterialTheme.spacing.large, top = 20.dp, bottom = 0.dp)
                        )
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = MaterialTheme.spacing.medium, vertical = 6.dp)
                                .clip(RoundedCornerShape(MaterialTheme.spacing.radiusMedium))
                                .background(Color.White)
                        ) {
                            groupDisplay.devices.forEachIndexed { index, device ->
                                DeviceItemRow(
                                    device = device, showDivider = index < groupDisplay.devices.size - 1, onClick = { onDeviceDetail(device) })
                            }
                        }
                    }
                }
            }
        }
    }
}