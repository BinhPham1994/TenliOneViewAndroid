package com.tenli.aiot.ui.features.setting.screens.account

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import com.tenli.aiot.R
import com.tenli.aiot.ui.features.setting.components.SettingInputField
import com.tenli.aiot.ui.features.setting.components.SettingPrimaryButton
import com.tenli.aiot.ui.features.setting.core.SettingViewModel
import com.tenli.aiot.ui.features.setting.core.changePassword
import com.tenli.aiot.ui.features.setting.core.onPasswordChange
import com.tenli.aiot.ui.theme.spacing

@Composable
fun ChangePasswordScreen(viewModel: SettingViewModel, onLogoutSuccess: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    var showOld by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.medium)
            .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SettingInputField(
            label = "Mật khẩu hiện tại", value = uiState.account.oldPassword, placeholder = "Nhập mật khẩu hiện tại",
            leadingIconRes = R.drawable.password, isPassword = true, showPassword = showOld,
            onPasswordToggle = { showOld = !showOld }, onClear = { viewModel.onPasswordChange(old = "") },
            onValueChange = { viewModel.onPasswordChange(old = it) }
        )

        SettingInputField(
            label = "Mật khẩu mới", value = uiState.account.newPassword, placeholder = "Nhập mật khẩu mới",
            leadingIconRes = R.drawable.password, isPassword = true, showPassword = showNew,
            onPasswordToggle = { showNew = !showNew }, onClear = { viewModel.onPasswordChange(new = "") },
            onValueChange = { viewModel.onPasswordChange(new = it) }
        )

        SettingInputField(
            label = "Xác nhận mật khẩu", value = uiState.account.confirmPassword, placeholder = "Nhập lại mật khẩu mới",
            leadingIconRes = R.drawable.password, isPassword = true, showPassword = showConfirm,
            onPasswordToggle = { showConfirm = !showConfirm }, onClear = { viewModel.onPasswordChange(confirm = "") },
            onValueChange = { viewModel.onPasswordChange(confirm = it) }
        )

        Spacer(modifier = Modifier.weight(1f))

        SettingPrimaryButton(
            text = "Đồng ý",
            isLoading = uiState.account.isChangingPassword,
            enabled = uiState.account.newPassword.isNotEmpty() && uiState.account.newPassword == uiState.account.confirmPassword,
            onClick = {
                focusManager.clearFocus()
                viewModel.changePassword(onSuccess = onLogoutSuccess)
            }
        )
    }
}