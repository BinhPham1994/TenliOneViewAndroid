package com.tenli.oneview.ui.features.setting.screens.notification.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import com.tenli.oneview.data.repository.DataRepository
import com.tenli.oneview.ui.features.setting.screens.notification.NotifyConfigViewModel
import com.tenli.oneview.ui.features.setting.screens.notification.NotifyMasterCard
import com.tenli.oneview.ui.features.setting.screens.notification.component.AlarmModeBottomSheet
import com.tenli.oneview.ui.features.setting.screens.notification.component.NotifyTypeItem
import com.tenli.oneview.ui.theme.spacing

@Composable
fun NotifyGroupDetailScreen(
    viewModel: NotifyConfigViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val detail = uiState.selectedGroupDetail
    val isGroupEnabled = detail?.notification?.enabled ?: false

    if (uiState.showModeSheet) {
        AlarmModeBottomSheet(
            currentMode = uiState.currentMode,
            onDismiss = { viewModel.closeModeSelection() },
            onSelect = { newMode ->
                viewModel.updateAlarmMode(newMode)
            }
        )
    }

    if (uiState.isDetailLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(MaterialTheme.spacing.iconMedium), strokeWidth = 5.dp)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(MaterialTheme.spacing.medium)
        ) {
            // 1. Master Switch của nhóm - Luôn hiển thị
            NotifyMasterCard(
                isEnabled = isGroupEnabled,
                onCheckedChange = { isEnabled ->
                    viewModel.updateGroupEnabled(detail?.key, isEnabled)
                }
            )

            // Chỉ hiển thị nội dung bên dưới khi Switch ở trên được Bật
            if (isGroupEnabled) {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

                Text(
                    text = "Cài đặt mặc định",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = MaterialTheme.spacing.small)
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MaterialTheme.spacing.small),
                    shape = RoundedCornerShape(MaterialTheme.spacing.radiusMedium),
                    color = Color.White
                ) {
                    Column {
                        detail?.eventTypes?.forEachIndexed { index, type ->
                            val typeDef = DataRepository.eventTypeDefs.find { it.key == type.key }
                            val name = typeDef?.display?.getLocalText() ?: type.key
                            val mode = type.notification.alarmMode?.default
                            val displayStatus = if (type.notification.enabled) {
                                viewModel.getAlarmModeText(mode)
                            } else {
                                "Không cảnh báo"
                            }

                            NotifyTypeItem(
                                iconRes = viewModel.getTypeIcon(type.key),
                                title = name,
                                isEnabled = type.notification.enabled,
                                statusText = displayStatus,
                                showIconBg = false,
                                isLast = index == (detail.eventTypes.size - 1),
                                onClick = {
                                    viewModel.openModeSelection(type.key, mode)
                                }
                            )
                        }
                    }
                }

                // 3. Footer Note - Cũng chỉ hiện khi On
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Chọn hình thức cảnh báo mặc định khi không có kịch bản hoặc khi kịch bản không hoạt động.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = MaterialTheme.spacing.small)
                )
            }
        }
    }
}