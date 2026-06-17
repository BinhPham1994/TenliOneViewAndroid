package com.tenli.aiot.ui.features.setting.screens.device.script

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tenli.aiot.model.network.ScriptItem
import com.tenli.aiot.model.network.getDisplaySchedule
import com.tenli.aiot.ui.component.CommonEmptyState
import com.tenli.aiot.ui.theme.spacing

@Composable
fun ScriptManagementScreen(
    viewModel: ScriptViewModel,
    onEditScript: (ScriptItem) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (uiState.isLoading && uiState.scripts.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        } else if (uiState.scripts.isEmpty()) {
            CommonEmptyState(
                text = "Chưa có kịch bản nào được thiết lập"
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(MaterialTheme.spacing.medium)
            ) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White
                    ) {
                        Column {
                            uiState.scripts.forEachIndexed { index, script ->
                                ScriptItemRow(script = script) {
                                    onEditScript(script)
                                }
                                if (index < uiState.scripts.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
                                        thickness = MaterialTheme.spacing.borderThin,
                                        color = Color.LightGray.copy(alpha = 0.3f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScriptItemRow(script: ScriptItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(MaterialTheme.spacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Đặt Icon trực tiếp, không cần bọc Box [cite: 2026-03-16]
        Icon(
            imageVector = Icons.Outlined.Schedule,
            contentDescription = null,
            modifier = Modifier.size(MaterialTheme.spacing.iconMedium), // Kích thước icon vẫn giữ MaterialTheme.spacing.large
            tint = Color.Gray
        )

        Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = script.name,
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = script.getDisplaySchedule(),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.LightGray.copy(alpha = 0.8f),
            modifier = Modifier.size(22.dp)
        )
    }
}