package com.tenli.oneview.ui.features.setting.screens.device.script

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tenli.oneview.R
import com.tenli.oneview.data.repository.DataRepository
import com.tenli.oneview.model.network.ScriptItem
import com.tenli.oneview.ui.component.AppConfirmDialog
import com.tenli.oneview.ui.component.AppTextField
import com.tenli.oneview.ui.features.setting.screens.device.script.component.SelectMonitorBottomSheet
import com.tenli.oneview.ui.theme.spacing
import com.tenli.oneview.ui.utils.DateTimeUtils.formatTimeInput
import com.tenli.oneview.ui.utils.bounceClick

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScriptFormScreen(
    viewModel: ScriptViewModel,
    script: ScriptItem? = null, // Nếu null = Add, nếu != null = Edit
    showDeleteDialog: Boolean = false,
    onDismissDelete: () -> Unit = {},
    onBack: () -> Unit
) {
    val focusManager = LocalFocusManager.current // Dùng để ẩn bàn phím [cite: 2026-03-16]
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isEdit = script != null

    // --- 1. KHỞI TẠO STATE (Dùng script làm key để reset khi dữ liệu thay đổi) ---
    var scriptName by remember(script) { mutableStateOf(script?.name ?: "") }
    var isEnabled by remember(script) { mutableStateOf(script?.enabled ?: true) }
    var resetTime by remember(script) { mutableStateOf(script?.resetModeAfterTime?.toString() ?: "1800") }

    var startTime by remember(script) {
        val h = script?.time?.activate?.hour ?: 18
        val m = script?.time?.activate?.minute ?: 0
        mutableStateOf("${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}")
    }
    var endTime by remember(script) {
        val h = script?.time?.deactivate?.hour ?: 5
        val m = script?.time?.deactivate?.minute ?: 0
        mutableStateOf("${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}")
    }

    var selectedDays by remember(script) { mutableStateOf(script?.time?.dayOfWeeks ?: listOf(1, 2, 3, 4, 5, 6, 0)) }
    var selectedMonitorIds by remember(script) {
        val ids = script?.actions?.activate?.firstOrNull()?.monitorIds ?: emptyList()
        mutableStateOf(ids)
    }

    var showMonitorSheet by remember { mutableStateOf(false) }

    // --- 2. DIALOGS & BOTTOM SHEETS ---
    if (isEdit && showDeleteDialog) {
        AppConfirmDialog(
            title = "Xóa kịch bản",
            message = "Bạn có chắc chắn muốn xóa kịch bản \"${script.name}\" không?",
            confirmText = "Xóa ngay",
            iconRes = R.drawable.delete,
            onDismiss = onDismissDelete,
            onConfirm = {
                viewModel.deleteScript(script.id) {
                    onDismissDelete()
                    onBack()
                }
            }
        )
    }

    if (showMonitorSheet) {
        val allMonitors = DataRepository.monitorMap[viewModel.device.id] ?: emptyList()
        val filteredMonitors = allMonitors.filter { it.monitor.type == "logic-person" }
        SelectMonitorBottomSheet(
            monitors = filteredMonitors,
            selectedIds = selectedMonitorIds,
            onDismiss = { showMonitorSheet = false },
            onToggleMonitor = { id ->
                selectedMonitorIds = if (selectedMonitorIds.contains(id)) selectedMonitorIds - id else selectedMonitorIds + id
            },
            onConfirm = { showMonitorSheet = false }
        )
    }

    // --- 3. GIAO DIỆN CHÍNH ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = MaterialTheme.spacing.medium, vertical = MaterialTheme.spacing.small),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
        ) {
            // Card Thông tin chung
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(MaterialTheme.spacing.radiusMedium)) {
                Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
                    Text("Thông tin kịch bản", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    AppTextField(
                        value = scriptName,
                        onValueChange = { scriptName = it },
                        label = "Tên kịch bản",
                        placeholder = "Ví dụ: An ninh ban đêm"
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = MaterialTheme.spacing.small)) {
                        Text("Kích hoạt kịch bản", modifier = Modifier.weight(1f))
                        Switch(checked = isEnabled, onCheckedChange = { isEnabled = it }, colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary))
                    }
                }
            }

            // Card Thời gian
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(MaterialTheme.spacing.radiusMedium)) {
                Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
                    Text("Thời gian hoạt động", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AppTextField(
                            value = startTime,
                            onValueChange = {
                                val newValue = if (startTime.contains(":") && !it.contains(":")) it.dropLast(1) else it
                                startTime = formatTimeInput(newValue)
                            },
                            label = "Bắt đầu",
                            placeholder = "HH:mm",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        AppTextField(
                            value = endTime,
                            onValueChange = {
                                val newValue = if (endTime.contains(":") && !it.contains(":")) it.dropLast(1) else it
                                endTime = formatTimeInput(newValue)
                            },
                            label = "Kết thúc",
                            placeholder = "HH:mm",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                    Text("Lặp lại hàng tuần", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        val daysList = listOf(1, 2, 3, 4, 5, 6, 0)
                        val dayNames = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
                        daysList.forEachIndexed { index, day ->
                            val isSelected = selectedDays.contains(day)
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFF0F0F0), CircleShape)
                                    .clickable { selectedDays = if (isSelected) selectedDays - day else selectedDays + day },
                                contentAlignment = Alignment.Center
                            ) { Text(dayNames[index], color = if (isSelected) Color.White else Color.Black, fontSize = 12.sp) }
                        }
                    }
                }
            }

            // Card Nâng cao
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(MaterialTheme.spacing.radiusMedium)) {
                Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
                    Text("Cài đặt nâng cao", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    AppTextField(
                        value = resetTime,
                        onValueChange = { resetTime = it },
                        label = "Thời gian tự bật lại (giây)",
                        placeholder = "Mặc định: 1800",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            // Card Tiến trình AI
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(MaterialTheme.spacing.radiusMedium)) {
                Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Tiến trình áp dụng", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                        TextButton(onClick = { showMonitorSheet = true }) { Icon(Icons.Default.Add, null, modifier = Modifier.size(MaterialTheme.spacing.iconSmall)); Text(" Chọn") }
                    }
                    if (selectedMonitorIds.isEmpty()) Text("Chưa chọn tiến trình nào", color = Color.LightGray, style = MaterialTheme.typography.bodyMedium)
                    else Text("Đã chọn ${selectedMonitorIds.size} tiến trình",  style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }

        // --- NÚT LƯU / CẬP NHẬT ---
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.medium)
                .height(54.dp)
                .bounceClick {
                    if (startTime.length < 5 || endTime.length < 5) {
                        viewModel.onShowSnackbar("Thời gian chưa đúng định dạng HH:mm")
                    } else {
                        if (isEdit) {
                            viewModel.editSecurityScript(script.id, scriptName, isEnabled, startTime, endTime, selectedDays, selectedMonitorIds, resetTime.toIntOrNull() ?: 1800, onBack)
                        } else {
                            viewModel.saveSecurityScript(scriptName, isEnabled, startTime, endTime, selectedDays, selectedMonitorIds, resetTime.toIntOrNull() ?: 1800, onBack)
                        }
                    }
                },
            shape = RoundedCornerShape(MaterialTheme.spacing.radiusMedium), color = MaterialTheme.colorScheme.primary
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (uiState.isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(MaterialTheme.spacing.iconMedium), strokeWidth = 5.dp)
                else Text(if (isEdit) "Cập nhật" else "Lưu", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}