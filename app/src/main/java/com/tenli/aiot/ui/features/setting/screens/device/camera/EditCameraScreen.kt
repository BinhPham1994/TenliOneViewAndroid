package com.tenli.aiot.ui.features.setting.screens.device.camera

import androidx.compose.foundation.layout.Arrangement
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tenli.aiot.ui.features.setting.components.SettingInputField
import com.tenli.aiot.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCameraScreen(viewModel: CameraViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val camera = uiState.selectedCamera ?: return

    var name by remember { mutableStateOf(camera.name) }
    var rtspUrl by remember { mutableStateOf(camera.url ?: "") }

    val currentMode = when(camera.type) {
        "stream-software" -> "Phần mềm"
        "stream-hardware" -> "Phần cứng"
        else -> "Tự động"
    }

    val decodingOptions = listOf("Tự động", "Phần mềm", "Phần cứng")
    var selectedDecoding by remember { mutableStateOf(currentMode) }
    var decodingExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = MaterialTheme.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
        contentPadding = PaddingValues(top = MaterialTheme.spacing.medium, bottom = MaterialTheme.spacing.large)
    ) {
        item {
            SettingInputField(
                label = "Tên camera",
                value = name,
                onValueChange = { name = it },
                onClear = { name = "" }
            )
        }

        item {
            Text("Giải mã", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = MaterialTheme.spacing.small))
            BrandDropdownField(
                selectedValue = selectedDecoding,
                expanded = decodingExpanded,
                onExpand = { decodingExpanded = it }
            ) {
                decodingOptions.forEach { mode ->
                    DropdownMenuItem(
                        text = { Text(mode) },
                        onClick = { selectedDecoding = mode; decodingExpanded = false }
                    )
                }
            }
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
        }

        item {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.spacing.large)) {
                Text("Đường dẫn", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = MaterialTheme.spacing.small))
                TextField(
                    value = rtspUrl,
                    onValueChange = { rtspUrl = it },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(MaterialTheme.spacing.radiusMedium),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
        }

        item {
            Button(
                onClick = {
                    viewModel.editCamera(camera.id, name, rtspUrl, selectedDecoding) {
                        viewModel.onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(MaterialTheme.spacing.radiusMedium),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Lưu", color = Color.White, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}