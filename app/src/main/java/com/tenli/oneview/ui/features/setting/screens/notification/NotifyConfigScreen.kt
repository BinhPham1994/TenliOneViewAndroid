package com.tenli.oneview.ui.features.setting.screens.notification

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tenli.oneview.ui.features.setting.core.SettingScreenType
import com.tenli.oneview.ui.features.setting.screens.notification.component.NotifyTypeItem
import com.tenli.oneview.ui.theme.spacing

@Composable
fun NotifyConfigScreen(
    viewModel: NotifyConfigViewModel,
    onNavigate: (SettingScreenType, String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
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
            // Luôn hiển thị Master Card để bật/tắt
            NotifyMasterCard(uiState.masterEnabled) { isChecked ->
                viewModel.updateMasterNotify(isChecked)
            }

            // Chỉ hiển thị danh sách loại cảnh báo khi masterEnabled == true
            if (uiState.masterEnabled) {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

                Text(
                    text = "Loại cảnh báo",
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
                        uiState.groups.forEachIndexed { index, group ->
                            val (name, icon, color) = viewModel.getDisplayInfo(group.key)

                            NotifyTypeItem(
                                iconRes = icon,
                                title = name,
                                isEnabled = group.notification.enabled,
                                iconBgColor = color,
                                isLast = index == uiState.groups.size - 1,
                                onClick = {
                                    viewModel.onGroupClick(group.key, onNavigate)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotifyMasterCard(
    isEnabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(MaterialTheme.spacing.radiusMedium),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .padding(MaterialTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Cho phép cảnh báo",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = isEnabled,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedBorderColor = Color.LightGray
                )
            )
        }
    }
}