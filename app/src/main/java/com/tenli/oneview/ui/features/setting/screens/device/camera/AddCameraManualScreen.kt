package com.tenli.oneview.ui.features.setting.screens.device.camera

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.tenli.oneview.ui.features.setting.components.SettingInputField
import com.tenli.oneview.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCameraManualScreen(viewModel: CameraViewModel) {
    var name by remember { mutableStateOf("") }
    var rtspUrl by remember { mutableStateOf("") }

    val decodingOptions = listOf("Tự động", "Phần mềm", "Phần cứng")
    var selectedDecoding by remember { mutableStateOf(decodingOptions[0]) }
    var decodingExpanded by remember { mutableStateOf(false) }

    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = MaterialTheme.spacing.medium, vertical = MaterialTheme.spacing.small),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
            contentPadding = PaddingValues(top = 0.dp, bottom = 0.dp)
        ) {
            // 1. Tên Camera
            item {
                SettingInputField(
                    label = "Tên camera",
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Nhập tên camera",
                    onClear = { name = "" }
                )
            }

            // 2. Chọn Giải mã
            item {
                Text(
                    text = "Giải mã",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = MaterialTheme.spacing.small)
                )
                BrandDropdownField(
                    selectedValue = selectedDecoding,
                    expanded = decodingExpanded,
                    onExpand = { decodingExpanded = it }
                ) {
                    decodingOptions.forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode) },
                            onClick = {
                                selectedDecoding = mode
                                decodingExpanded = false
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
            }

            // 3. Ô nhập Đường dẫn RTSP (Cho phép nhập nhiều dòng) [cite: 2026-03-13]
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.spacing.large)) {
                    Text(
                        text = "Đường dẫn",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = MaterialTheme.spacing.small)
                    )
                    TextField(
                        value = rtspUrl,
                        onValueChange = { rtspUrl = it },
                        modifier = Modifier.fillMaxWidth().height(120.dp), // Chiều cao cố định như ảnh
                        placeholder = { Text("rtsp://admin:password@192.168.1.10:554/...", color = Color.LightGray) },
                        shape = RoundedCornerShape(MaterialTheme.spacing.radiusMedium),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        maxLines = 4 // Cho phép xuống dòng khi link quá dài
                    )
                }
            }

            // 4. Nút bấm
            item {
                Button(
                    onClick = {
                        if (name.isNotBlank() && rtspUrl.isNotBlank()) {
                            viewModel.addCameraByBrand(name, rtspUrl, selectedDecoding) {
                                viewModel.onNavigateBack()
                            }
                        } else {
                            viewModel.onShowSnackbar("Vui lòng nhập đầy đủ thông tin")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(MaterialTheme.spacing.radiusMedium),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Thêm", color = Color.White, style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}