package com.tenli.oneview.ui.features.setting.screens.device.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tenli.oneview.R
import com.tenli.oneview.model.network.LanDevice
import com.tenli.oneview.ui.component.CommonEmptyState
import com.tenli.oneview.ui.features.setting.core.SettingViewModel
import com.tenli.oneview.ui.features.setting.core.prepareAddLanDevice
import com.tenli.oneview.ui.features.setting.core.startLanDiscovery
import com.tenli.oneview.ui.theme.spacing

@Composable
fun LanScanScreen(viewModel: SettingViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.startLanDiscovery(context)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            if (uiState.box.lanDevices.isEmpty() && !uiState.box.isScanningLan) {
                CommonEmptyState(
                    text = "Không tìm thấy thiết bị nào"
                )
            } else if (uiState.box.lanDevices.isEmpty() && uiState.box.isScanningLan) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, strokeWidth = 5.dp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(MaterialTheme.spacing.medium), verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.box.lanDevices) { device ->
                        LanDeviceItem(device) {
                            if (!device.isLoading && !device.needAuth && device.systemInfo != null) {
                                viewModel.prepareAddLanDevice(device)
                            }
                        }
                    }

                    if (uiState.box.isScanningLan) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(15.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(25.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 5.dp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LanDeviceItem(device: LanDevice, onClick: () -> Unit) {
    val isClickable = !device.needAuth && !device.isLoading && device.systemInfo != null
    val subText = when {
        device.needAuth -> "Thiết bị đã được thêm"
        device.systemInfo?.macAddress != null -> device.systemInfo.macAddress
        device.isLoading -> "Đang kiểm tra thiết bị..."
        else -> "Không thể lấy thông tin"
    }

    val subTextColor = when {
        device.needAuth -> MaterialTheme.colorScheme.error
        device.systemInfo?.macAddress != null -> Color.Gray
        else -> Color.LightGray
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(MaterialTheme.spacing.radiusMedium))
            .background(Color.White)
            .clickable(enabled = isClickable, onClick = onClick)
            .padding(MaterialTheme.spacing.medium), verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.device_icon_event),
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = if (device.needAuth) Color.Gray else MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = device.name,
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = subText, style = MaterialTheme.typography.bodyMedium, color = subTextColor
            )
        }
        if (device.isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(MaterialTheme.spacing.iconSmall), strokeWidth = 3.dp, color = MaterialTheme.colorScheme.primary)
        } else if (isClickable) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}