package com.tenli.aiot.ui.features.auth.forgot

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tenli.aiot.R
import com.tenli.aiot.ui.features.auth.login.LoginInputField
import com.tenli.aiot.ui.theme.spacing

@Composable
fun ForgotEmailStep(email: String, onEmailChange: (String) -> Unit) {
    val label = stringResource(id = R.string.forgot_pass_email_label)
    val hint = stringResource(id = R.string.forgot_pass_email_hint)
    val description = stringResource(id = R.string.forgot_pass_email_desc)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        LoginInputField(
            value = email,
            onValueChange = onEmailChange,
            hint = hint,
            onClear = { onEmailChange("") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
            )
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium, // 14sp Normal
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 10.dp)
        )
    }
}

@Composable
fun ForgotOtpStep(email: String, otp: String, onOtpChange: (String) -> Unit) {
    val label = stringResource(id = R.string.forgot_pass_otp_label)
    val hint = stringResource(id = R.string.forgot_pass_otp_hint)
    val description = stringResource(id = R.string.forgot_pass_otp_desc, email)
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        LoginInputField(
            value = otp,
            onValueChange = { if (it.length <= 6) onOtpChange(it) },
            hint = hint,
            onClear = { onOtpChange("") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            )
        )
    }
}

@Composable
fun ForgotNewPasswordStep(uiState: ForgotPassUiState, viewModel: ForgotPassViewModel) {
    val title = stringResource(id = R.string.forgot_pass_new_pass_title)
    val newPassHint = stringResource(id = R.string.forgot_pass_new_pass_hint)
    val confirmPassHint = stringResource(id = R.string.forgot_pass_confirm_pass_hint)
    val description = stringResource(id = R.string.forgot_pass_new_pass_desc)

    val focusManager = LocalFocusManager.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        LoginInputField(
            value = uiState.password,
            onValueChange = { viewModel.onPasswordChange(it, uiState.confirmPassword) },
            hint = newPassHint,
            isPassword = true,
            showPassword = uiState.isShowPassword,
            onPasswordToggle = { viewModel.togglePassword() },
            onClear = { viewModel.onPasswordChange("", uiState.confirmPassword) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        LoginInputField(
            value = uiState.confirmPassword,
            onValueChange = { viewModel.onConfirmPasswordChange(it) },
            hint = confirmPassHint,
            isPassword = true,
            showPassword = uiState.isShowConfirmPassword,
            onPasswordToggle = { viewModel.toggleConfirmPassword() },
            onClear = { viewModel.onConfirmPasswordChange("") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            )
        )

        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}