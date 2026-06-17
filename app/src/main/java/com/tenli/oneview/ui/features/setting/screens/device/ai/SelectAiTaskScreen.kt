package com.tenli.oneview.ui.features.setting.screens.device.ai

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tenli.oneview.data.mapper.EventProcessor
import com.tenli.oneview.model.network.MonitorType
import com.tenli.oneview.ui.features.setting.core.SettingScreenType
import com.tenli.oneview.ui.theme.spacing

@Composable
fun SelectAiTaskScreen(viewModel: AiSensorViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filteredList = remember(uiState.monitorTypes, uiState.selectedAiCategory) {
        val keyword = if (uiState.selectedAiCategory == 0) "logic" else "sensor"
        uiState.monitorTypes.filter { it.type.contains(keyword, ignoreCase = true) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
        } else {
            LazyColumn(
                contentPadding = PaddingValues(MaterialTheme.spacing.medium),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList) { item ->
                    val prettyName = EventProcessor.getMonitorTitle(item.type)
                    AiTaskItemCard(item = item, displayName = prettyName) {
                        viewModel.setSelectedMonitorType(item)
                        viewModel.fetchCameras()
                        if (item.type.contains("sensor", ignoreCase = true)) {
                            viewModel.onNavigateTo(SettingScreenType.AiSensorWizard, prettyName)
                        } else {
                            viewModel.onNavigateTo(SettingScreenType.AiLogicWizard, prettyName)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AiTaskItemCard(
    item: MonitorType,
    displayName: String,
    onClick: () -> Unit
) {
    val uiConfig = EventProcessor.getUIConfig(eType = item.type, eTypeGroup = "")
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(MaterialTheme.spacing.radiusMedium),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(uiConfig.iconRes),
                contentDescription = null,
                modifier = Modifier.size(MaterialTheme.spacing.iconMedium),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))

            Text(
                text = displayName,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
            )

            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}