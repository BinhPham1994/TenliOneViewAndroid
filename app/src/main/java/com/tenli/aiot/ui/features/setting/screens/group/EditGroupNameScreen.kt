package com.tenli.aiot.ui.features.setting.screens.group

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import com.tenli.aiot.ui.features.setting.components.SettingInputField
import com.tenli.aiot.ui.features.setting.components.SettingPrimaryButton
import com.tenli.aiot.ui.features.setting.core.SettingViewModel
import com.tenli.aiot.ui.features.setting.core.onGroupFieldChange
import com.tenli.aiot.ui.features.setting.core.updateGroupName
import com.tenli.aiot.ui.theme.spacing

@Composable
fun EditGroupNameScreen(viewModel: SettingViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.medium)
            .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
    ) {
        SettingInputField(
            label = "Tên nhà",
            value = uiState.group.tempGroupName,
            placeholder = "Nhập tên nhà mới",
            onValueChange = { viewModel.onGroupFieldChange(name = it) },
            onClear = { viewModel.onGroupFieldChange(name = "") }
        )

        Spacer(modifier = Modifier.weight(1f))

        SettingPrimaryButton(
            text = "Lưu",
            enabled = uiState.group.tempGroupName.isNotBlank(),
            isLoading = uiState.isLoading,
            onClick = {
                focusManager.clearFocus()
                viewModel.updateGroupName {
                    viewModel.navigateBack()
                }
            }
        )
    }
}