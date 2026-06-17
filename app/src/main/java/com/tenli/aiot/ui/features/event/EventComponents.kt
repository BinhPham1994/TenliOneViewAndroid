package com.tenli.aiot.ui.features.event

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tenli.aiot.R
import com.tenli.aiot.data.repository.DataRepository
import com.tenli.aiot.ui.theme.spacing
import com.tenli.aiot.ui.utils.bounceClick
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EventTopHeader(onFilterClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = MaterialTheme.spacing.borderMedium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(48.dp))

        Text(
            text = "Thông báo",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        IconButton(onClick = onFilterClick) {
            Icon(
                painter = painterResource(id = R.drawable.filter),
                contentDescription = "Filter",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
fun FilterCardContainer(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(top = 20.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = Color.Gray,
            modifier = Modifier.padding(start = MaterialTheme.spacing.extraSmall, bottom = MaterialTheme.spacing.small),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(MaterialTheme.spacing.radiusMedium))
                .background(Color.White)
        ) {
            content()
        }
    }
}

@Composable
fun FilterItemRow(
    label: String,
    isSelected: Boolean,
    iconRes: Int,
    onClick: () -> Unit,
    showDivider: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(start = 15.dp, end = 10.dp, top = MaterialTheme.spacing.small, bottom = MaterialTheme.spacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
        )
        TenliCheckbox(
            isSelected = isSelected,
            onClick = onClick,
            size = 20,         // Bạn có thể tùy chỉnh to nhỏ tùy ý
            cornerRadius = 6    // Bo góc 6dp nhìn rất sang
        )
    }
    if (showDivider) {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
            thickness = MaterialTheme.spacing.borderThin,
            color = Color.LightGray.copy(alpha = 0.3f)
        )
    }
}

@Composable
fun TenliCheckbox(
    isSelected: Boolean,
    onClick: () -> Unit,
    size: Int = 22,        // Chỉnh size tùy ý tại đây [cite: 2026-03-01]
    cornerRadius: Int = 6,  // Chỉnh bo góc tại đây
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape(cornerRadius.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .border(
                width = 1.5.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFD1D1D1),
                shape = RoundedCornerShape(cornerRadius.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                painter = rememberVectorPainter(image = Icons.Default.Check),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size((size * 0.6).dp)
            )
        }
    }
}

@Composable
fun TimePickerRowStyle(
    label: String,
    value: String?,
    onClick: () -> Unit,
    showDivider: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(start = 15.dp, end = 10.dp, top = MaterialTheme.spacing.small, bottom = MaterialTheme.spacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
        Text(
            text = value ?: "",
            style = MaterialTheme.typography.bodyLarge,
            color = if (value != null) Color.Black else Color.Gray
        )
        Spacer(modifier = Modifier.width(10.dp))
        Icon(
            painter = painterResource(id = R.drawable.date),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
    if (showDivider) {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
            thickness = MaterialTheme.spacing.borderThin,
            color = Color.LightGray.copy(alpha = 0.3f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventFilterSheet(
    uiState: EventUiState,
    viewModel: EventViewModel,
    onDismiss: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var pickingForStart by remember { mutableStateOf(true) } // true: Bắt đầu, false: Kết thúc

    val datePickerState = rememberDatePickerState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background, // Nền xám nhạt để nổi bật Card trắng
        dragHandle = { BottomSheetDefaults.DragHandle() },
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = MaterialTheme.spacing.medium, end = MaterialTheme.spacing.medium),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Bộ lọc",
                    style = MaterialTheme.typography.titleMedium,
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .bounceClick { viewModel.resetFilter() } // Giữ hiệu ứng nhún của bạn
                        .padding(MaterialTheme.spacing.extraSmall) // Tăng vùng chạm
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.refresh),
                        contentDescription = "Reset",
                        tint = Color(0xFFE42E1B),
                        modifier = Modifier.size(MaterialTheme.spacing.iconMedium)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false) // Chỉ cao vừa đủ nội dung
                    .padding(horizontal = MaterialTheme.spacing.medium)
            ) {
                // 1. Khối Thời gian
                item {
                    FilterCardContainer(title = "Khoảng thời gian") {
                        TimePickerRowStyle(
                            label = "Bắt đầu",
                            value = uiState.fromTime,
                            onClick = {
                                pickingForStart = true
                                showDatePicker = true
                            },
                            showDivider = true
                        )
                        TimePickerRowStyle(
                            label = "Kết thúc",
                            value = uiState.toTime,
                            onClick = {
                                pickingForStart = false
                                showDatePicker = true
                            },
                            showDivider = false
                        )
                    }
                }

                // 2. Khối Thiết bị
                item {
                    FilterCardContainer(title = "Danh sách thiết bị") {
                        uiState.availableDevices.forEachIndexed { index, device ->
                            FilterItemRow(
                                label = device.name,
                                isSelected = uiState.selectedDeviceIds.contains(device.id),
                                iconRes = R.drawable.device_icon,
                                onClick = { viewModel.toggleDeviceSelection(device.id) },
                                showDivider = index < uiState.availableDevices.size - 1
                            )
                        }
                    }
                }

                uiState.availableTypeGroups.forEach { (groupKey, types) ->
                    val groupTitle = DataRepository.eventGroupDefs
                        .find { it.key == groupKey }?.display?.getLocalText() ?: groupKey

                    item {
                        FilterCardContainer(title = groupTitle) {
                            types.forEachIndexed { index, typeDef ->
                                FilterItemRow(
                                    label = typeDef.display.getLocalText(),
                                    isSelected = uiState.selectedETypes.contains(typeDef.key),
                                    iconRes = R.drawable.ai_icon,
                                    onClick = { viewModel.toggleTypeSelection(typeDef.key) },
                                    showDivider = index < types.size - 1
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(MaterialTheme.spacing.large)) }
            }

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            val selectedDate = datePickerState.selectedDateMillis
                            if (selectedDate != null) {
                                // Chuyển Long sang String định dạng yyyy-MM-dd để gửi API [cite: 2026-03-01]
                                val dateStr = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(selectedDate))
                                if (pickingForStart) {
                                    viewModel.updateFromTime(dateStr)
                                } else {
                                    viewModel.updateToTime(dateStr)
                                }
                            }
                            showDatePicker = false
                        }) {
                            Text("Xác nhận", color = MaterialTheme.colorScheme.primary)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Hủy")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            Button(
                onClick = {
                    viewModel.applyFilter()
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.medium)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(MaterialTheme.spacing.radiusMedium)
            ) {
                Text("Xác nhận", color = Color.White, style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}