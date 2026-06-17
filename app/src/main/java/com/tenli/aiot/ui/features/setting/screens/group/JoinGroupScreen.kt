package com.tenli.aiot.ui.features.setting.screens.group

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.tenli.aiot.ui.features.setting.components.SettingInputField
import com.tenli.aiot.ui.features.setting.components.SettingPrimaryButton
import com.tenli.aiot.ui.features.setting.core.SettingViewModel
import com.tenli.aiot.ui.features.setting.core.joinGroup
import com.tenli.aiot.ui.features.setting.core.onGroupFieldChange
import com.tenli.aiot.ui.theme.spacing

@Composable
fun JoinGroupScreen(viewModel: SettingViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val groupState = uiState.group
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.medium)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }) {
        SettingInputField(
            label = "Mã gia nhập",
            value = groupState.tempJoinCode,
            placeholder = "Nhập mã gia nhập",
            modifier = Modifier.focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            onValueChange = {
                if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                    viewModel.onGroupFieldChange(code = it)
                }
            },
            onClear = { viewModel.onGroupFieldChange(code = "") }
        )

        Text(
            text = "Vui lòng nhập mã gia nhập gồm 6 ký tự được gửi từ chủ nhà.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.weight(1f))

        SettingPrimaryButton(
            text = "Gia nhập",
            enabled = groupState.tempJoinCode.length == 6,
            isLoading = uiState.isLoading,
            onClick = {
                focusManager.clearFocus()
                viewModel.joinGroup { viewModel.navigateBack() }
            }
        )
    }
}