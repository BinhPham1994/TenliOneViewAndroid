package com.tenli.oneview.ui.features.setting.screens.device.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tenli.oneview.R
import com.tenli.oneview.data.mapper.EventProcessor
import com.tenli.oneview.data.repository.DataRepository
import com.tenli.oneview.model.network.MonitorDisplayItem
import com.tenli.oneview.ui.component.SafeAsyncImage
import com.tenli.oneview.ui.features.setting.core.SettingViewModel
import com.tenli.oneview.ui.features.setting.core.fetchMonitorsForDevice
import com.tenli.oneview.ui.features.setting.core.prepareEditMonitor
import com.tenli.oneview.ui.features.setting.core.updateAiCategory
import com.tenli.oneview.ui.theme.spacing

@Composable
fun AiSettingScreen(viewModel: SettingViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val device = uiState.box.selectedDeviceItem ?: return
    var selectedTab by remember { mutableIntStateOf(uiState.box.selectedAiCategory) }

    val allMonitors = remember(device.id, uiState.box.isLoading, uiState.box.monitorUpdateTicket) {
        DataRepository.monitorList.filter { it.deviceId == device.id }
    }

    LaunchedEffect(device.id) {
        viewModel.fetchMonitorsForDevice()
    }

    val tabs = listOf("Logical", "Sensor")

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp), contentAlignment = Alignment.Center
        ) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                tabs.forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = tabs.size,
                            baseShape = RoundedCornerShape(MaterialTheme.spacing.radiusMedium)
                        ),
                        onClick = {
                            selectedTab = index
                            viewModel.updateAiCategory(index)
                        },
                        selected = selectedTab == index,
                        label = { Text(label, style = MaterialTheme.typography.labelLarge) },
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = MaterialTheme.colorScheme.primary,
                            activeContentColor = Color.White,
                        )
                    )
                }
            }
        }

        val displayList = remember(selectedTab, allMonitors) {
            if (selectedTab == 0) {
                allMonitors.filter { it.monitor.type?.contains("sensor", ignoreCase = true) == false }
            } else {
                allMonitors.filter { it.monitor.type?.contains("sensor", ignoreCase = true) == true }
            }
        }

        if (displayList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (!uiState.isLoading) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(id = R.drawable.data_empty),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(100.dp),
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            "Chưa có cấu hình AI",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(MaterialTheme.spacing.medium),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayList, key = { it.monitor.id }) { item ->
                    AiMonitorCard(item) {
                        viewModel.prepareEditMonitor(item)
                    }
                }
            }
        }
    }
}

@Composable
fun AiMonitorCard(item: MonitorDisplayItem, onClick: () -> Unit) {
    val typeDef = DataRepository.eventTypeDefs.find { it.key == item.monitor.type }
    val uiConfig = EventProcessor.getUIConfig(
        eType = item.monitor.type ?: "",
        eTypeGroup = typeDef?.key ?: ""
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(MaterialTheme.spacing.radiusMedium),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        ) {
            SafeAsyncImage(
                url = item.snapshotUrl,
                deviceKey = item.deviceKey
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent)
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = uiConfig.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                Text(
                    text = item.monitor.name ?: "Chưa đặt tên",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Surface(
                modifier = Modifier
                    .padding(5.dp)
                    .align(Alignment.BottomEnd),
                color = if (item.monitor.enabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else Color.Red.copy(alpha = 0.8f),
                shape = RoundedCornerShape(MaterialTheme.spacing.extraSmall)
            ) {
                Text(
                    text = if (item.monitor.enabled) "ACTIVE" else "INACTIVE",
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}