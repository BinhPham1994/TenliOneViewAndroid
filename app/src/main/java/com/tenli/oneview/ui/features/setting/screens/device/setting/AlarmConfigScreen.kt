package com.tenli.oneview.ui.features.setting.screens.device.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tenli.oneview.R
import com.tenli.oneview.ui.features.setting.core.SettingViewModel
import com.tenli.oneview.ui.theme.spacing

@Composable
fun AlarmConfigScreen(viewModel: SettingViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val data = uiState.box.systemSetting?.settings
    val alarmOnList = remember(data) { mutableStateListOf<String>().apply { addAll(data?.alarmOn ?: emptyList()) } }
    val alarmOffList = remember(data) { mutableStateListOf<String>().apply { addAll(data?.alarmOff ?: emptyList()) } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.medium)
    ) {
        // Nhóm kịch bản BẬT
        AlarmSection(
            title = "Kịch bản Bật cảnh báo",
            items = alarmOnList,
            onAdd = { alarmOnList.add("Lệnh mới ${alarmOnList.size + 1}") },
            onRemove = { alarmOnList.removeAt(it) }
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

        // Nhóm kịch bản TẮT
        AlarmSection(
            title = "Kịch bản Tắt cảnh báo",
            items = alarmOffList,
            onAdd = { alarmOffList.add("Lệnh mới ${alarmOffList.size + 1}") },
            onRemove = { alarmOffList.removeAt(it) }
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { /* Save API */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(MaterialTheme.spacing.radiusMedium)
        ) {
            Text("Lưu cấu hình", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AlarmSection(title: String, items: List<String>, onAdd: () -> Unit, onRemove: (Int) -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            IconButton(onClick = onAdd) {
                Icon(imageVector = androidx.compose.material.icons.Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(MaterialTheme.spacing.radiusMedium))
                .background(Color.White)
        ) {
            if (items.isEmpty()) {
                Text("Chưa có cấu hình", modifier = Modifier.padding(MaterialTheme.spacing.medium), color = Color.LightGray)
            } else {
                items.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .padding(MaterialTheme.spacing.medium)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(item, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        IconButton(onClick = { onRemove(index) }, modifier = Modifier.size(MaterialTheme.spacing.iconMedium)) {
                            Icon(
                                painter = painterResource(id = R.drawable.delete),
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    if (index < items.size - 1) HorizontalDivider(modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium), thickness = MaterialTheme.spacing.borderThin, color = Color.LightGray.copy(alpha = 0.3f))
                }
            }
        }
    }
}