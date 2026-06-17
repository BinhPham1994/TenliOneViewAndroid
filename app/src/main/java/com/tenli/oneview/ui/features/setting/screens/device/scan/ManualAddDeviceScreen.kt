package com.tenli.oneview.ui.features.setting.screens.device.scan

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tenli.oneview.ui.features.auth.login.LoginInputField
import com.tenli.oneview.ui.features.setting.core.SettingViewModel
import com.tenli.oneview.ui.features.setting.core.checkManualDevice
import com.tenli.oneview.ui.theme.spacing

@Composable
fun ManualAddDeviceScreen(viewModel: SettingViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val isLocalLoading = uiState.box.isLoading

    var ipOrSn by remember { mutableStateOf("") }
    var deviceKey by remember { mutableStateOf("") }
    var showKeyInput by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
            .padding(MaterialTheme.spacing.large)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Nhập địa chỉ IP hoặc SN của thiết bị đang trong hệ thống mạng của bạn",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = MaterialTheme.spacing.extraLarge, top = MaterialTheme.spacing.small),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Địa chỉ IP/SN",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(5.dp))
                LoginInputField(
                    value = ipOrSn,
                    onValueChange = { ipOrSn = it },
                    hint = "192.168.1.10 hoặc 00e04c548c40",
                    onClear = { ipOrSn = "" },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = {
                        if (showKeyInput) focusManager.moveFocus(FocusDirection.Down)
                        else focusManager.clearFocus()
                    })
                )
            }

            if (showKeyInput) {
                Spacer(modifier = Modifier.height(20.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Mã bảo mật (Device Key)",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    LoginInputField(
                        value = deviceKey,
                        onValueChange = { deviceKey = it },
                        hint = "Nhập mã bảo mật của thiết bị",
                        isPassword = true,
                        onClear = { deviceKey = "" },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                    )
                    Text(
                        text = "Thiết bị yêu cầu mã bảo mật để truy cập. Vui lòng nhập mã chính xác.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = MaterialTheme.spacing.small)
                    )
                }
            }
        }

        Button(
            onClick = {
                focusManager.clearFocus()
                viewModel.checkManualDevice(
                    input = ipOrSn,
                    key = deviceKey.ifEmpty { "key" },
                    onUnauthorized = { showKeyInput = true }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(MaterialTheme.spacing.radiusMedium),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.primary,

                contentColor = Color.White,
                disabledContentColor = Color.White.copy(alpha = 0.5f)
            ),
            enabled = ipOrSn.isNotBlank() && !isLocalLoading,
            elevation = if (ipOrSn.isNotBlank() && !uiState.isLoading)
                ButtonDefaults.buttonElevation()
            else
                ButtonDefaults.buttonElevation(0.dp)
        ) {
            if (isLocalLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(MaterialTheme.spacing.iconMedium),
                    color = Color.White.copy(alpha = 0.5f),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Tiếp tục",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
    }
}