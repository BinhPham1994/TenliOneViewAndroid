package com.tenli.aiot.ui.features.setting.screens.device.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tenli.aiot.model.network.commonBrands
import com.tenli.aiot.ui.features.setting.components.SettingInputField
import com.tenli.aiot.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCameraBrandScreen(viewModel: CameraViewModel) {
    var name by remember { mutableStateOf("") }
    var user by remember { mutableStateOf("admin") }
    var pass by remember { mutableStateOf("") }
    var ip by remember { mutableStateOf("192.168.1.10") }
    var port by remember { mutableStateOf("554") }
    var channel by remember { mutableStateOf("1") }

    var showPassword by remember { mutableStateOf(false) }
    var selectedBrand by remember { mutableStateOf(commonBrands[0]) }
    var brandExpanded by remember { mutableStateOf(false) }

    val decodingOptions = listOf("Tự động", "Phần mềm", "Phần cứng")
    var selectedDecoding by remember { mutableStateOf(decodingOptions[0]) }
    var decodingExpanded by remember { mutableStateOf(false) }

    val generatedUrl = remember(name, user, pass, ip, port, channel, selectedBrand) {
        selectedBrand.rtspTemplate(user, pass, ip, port, channel)
    }
    val focusManager = LocalFocusManager.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = MaterialTheme.spacing.medium, vertical = MaterialTheme.spacing.small),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
            contentPadding = PaddingValues(top = 0.dp, bottom = 0.dp)
        ) {
            item {
                SettingInputField(
                    label = "Tên camera",
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Nhập tên camera",
                    onClear = { name = "" }
                )
            }

            item {
                SettingInputField(
                    label = "Tài khoản",
                    value = user,
                    onValueChange = { user = it },
                    placeholder = "admin",
                    onClear = { user = "" }
                )
            }

            item {
                SettingInputField(
                    label = "Mật khẩu",
                    value = pass,
                    onValueChange = { pass = it },
                    placeholder = "Nhập mật khẩu",
                    isPassword = true,
                    showPassword = showPassword,
                    onPasswordToggle = { showPassword = !showPassword },
                    onClear = { pass = "" }
                )
            }

            item {
                SettingInputField(
                    label = "Địa chỉ IPv4",
                    value = ip,
                    onValueChange = { ip = it },
                    placeholder = "0.0.0.0",
                    onClear = { ip = "" },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            item {
                SettingInputField(
                    label = "Cổng dịch vụ",
                    value = port,
                    onValueChange = { port = it },
                    placeholder = "554",
                    onClear = { port = "" },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            item {
                Text(
                    text = "Hãng sản xuất",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = MaterialTheme.spacing.small)
                )
                BrandDropdownField(
                    selectedValue = selectedBrand.brandName,
                    expanded = brandExpanded,
                    onExpand = { brandExpanded = it }
                ) {
                    commonBrands.forEach { brand ->
                        DropdownMenuItem(
                            text = { Text(brand.brandName) },
                            onClick = {
                                selectedBrand = brand
                                brandExpanded = false
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
            }

            item {
                SettingInputField(
                    label = "Kênh",
                    value = channel,
                    onValueChange = { channel = it },
                    placeholder = "1",
                    onClear = { channel = "" },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

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

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = MaterialTheme.spacing.medium)
                ) {
                    Text(
                        text = "Đường dẫn (Tự động)",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = MaterialTheme.spacing.small),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(MaterialTheme.spacing.radiusMedium),
                        color = Color.White, // Giữ màu trắng đồng bộ với các ô nhập
                        shadowElevation = 0.dp // Hoặc để MaterialTheme.spacing.borderMedium nếu muốn nổi bật hơn
                    ) {
                        androidx.compose.foundation.text.selection.SelectionContainer {
                            Text(
                                text = generatedUrl,
                                modifier = Modifier.padding(MaterialTheme.spacing.medium),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF616161) // Màu xám đậm
                            )
                        }
                    }
                    Text(
                        text = "Đường dẫn được tạo tự động dựa trên thông tin phía trên.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = MaterialTheme.spacing.extraSmall, start = MaterialTheme.spacing.extraSmall)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                Button(
                    onClick = {
                        viewModel.addCameraByBrand(name, generatedUrl, selectedDecoding) {
                            viewModel.onNavigateBack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(MaterialTheme.spacing.radiusMedium),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Thêm", color = Color.White, style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrandDropdownField(
    selectedValue: String,
    expanded: Boolean,
    onExpand: (Boolean) -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpand) {
        TextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .menuAnchor(),
            shape = RoundedCornerShape(MaterialTheme.spacing.radiusMedium),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpand(false) },
            modifier = Modifier.background(Color.White)
        ) {
            content()
        }
    }
}

@Composable
fun InputLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(bottom = 6.dp, start = MaterialTheme.spacing.extraSmall)
    )
}