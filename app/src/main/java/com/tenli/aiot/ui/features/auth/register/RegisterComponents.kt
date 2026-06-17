package com.tenli.aiot.ui.features.auth.register

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tenli.aiot.R
import com.tenli.aiot.ui.features.auth.login.LoginInputField
import com.tenli.aiot.ui.theme.spacing
import com.tenli.aiot.ui.utils.bounceClick

@Composable
fun RegisterEmailStep(uiState: RegisterUiState, viewModel: RegisterViewModel) {
    val label = stringResource(id = R.string.register_email_label)
    val hint = stringResource(id = R.string.register_email_hint)
    val desc = stringResource(id = R.string.register_email_desc)
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
        LoginInputField(
            value = uiState.email,
            onValueChange = { viewModel.onEmailChange(it) },
            hint = hint,
            onClear = { viewModel.onEmailChange("") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
            )
        )
        Text(
            text = desc,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 10.dp)
        )
    }
}

@Composable
fun RegisterOtpStep(uiState: RegisterUiState, viewModel: RegisterViewModel) {
    val label = stringResource(id = R.string.register_otp_label)
    val hint = stringResource(id = R.string.register_otp_hint)
    val resendText = stringResource(id = R.string.register_btn_resend_otp)
    val desc = stringResource(id = R.string.register_otp_desc, uiState.email)
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.align(Alignment.Start),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        Text(
            text = desc,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 20.dp)
        )

        LoginInputField(
            value = uiState.otp,
            onValueChange = { if (it.length <= 6) viewModel.onOtpChange(it) },
            hint = hint,
            onClear = { viewModel.onOtpChange("") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = resendText,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier
                .clip(RoundedCornerShape(MaterialTheme.spacing.extraSmall))
                .bounceClick { /* Logic gửi lại OTP */ }
                .padding(MaterialTheme.spacing.small)
        )
    }
}

@Composable
fun RegisterDetailsStep(uiState: RegisterUiState, viewModel: RegisterViewModel) {
    val titleLabel = stringResource(id = R.string.register_details_label)
    val userHint = stringResource(id = R.string.register_username_hint)
    val passHint = stringResource(id = R.string.register_password_hint)
    val confirmHint = stringResource(id = R.string.register_confirm_pass_hint)

    // Phần text Điều khoản
    val agreePrefix = stringResource(id = R.string.register_terms_i_agree)
    val termsLabel = stringResource(id = R.string.register_terms_label)
    val andText = stringResource(id = R.string.register_terms_and)
    val policyLabel = stringResource(id = R.string.register_policy_label)

    val focusManager = LocalFocusManager.current
    val uriHandler = LocalUriHandler.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = titleLabel,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
        LoginInputField(
            value = uiState.username,
            onValueChange = { viewModel.onUsernameChange(it) },
            hint = userHint,
            onClear = { viewModel.onUsernameChange("") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        LoginInputField(
            value = uiState.password,
            onValueChange = { viewModel.onPasswordChange(it) },
            hint = passHint,
            isPassword = true,
            showPassword = uiState.isShowPassword,
            onPasswordToggle = { viewModel.togglePassword() },
            onClear = { viewModel.onPasswordChange("") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        LoginInputField(
            value = uiState.confirmPassword,
            onValueChange = { viewModel.onConfirmPasswordChange(it) },
            hint = confirmHint,
            isPassword = true,
            showPassword = uiState.isShowConfirmPassword,
            onPasswordToggle = { viewModel.toggleConfirmPassword() },
            onClear = { viewModel.onConfirmPasswordChange("") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = uiState.isAccepted,
                onCheckedChange = { viewModel.onAcceptChange(it) },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.outline,
                    checkmarkColor = Color.White
                )
            )

            // Sử dụng Theme color cho Annotated String [cite: 2026-02-25]
            val annotatedText = buildAnnotatedString {
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)) {
                    append(agreePrefix)
                }
                pushStringAnnotation(tag = "URL", annotation = "https://www.tenli.ai/term-of-use-vi/")
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                ) {
                    append(termsLabel)
                }
                pop()

                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)) {
                    append(andText)
                }

                pushStringAnnotation(tag = "URL", annotation = "https://www.tenli.ai/policy-vi/")
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                ) {
                    append(policyLabel)
                }
                pop()
            }

            ClickableText(
                text = annotatedText,
                modifier = Modifier.padding(MaterialTheme.spacing.extraSmall),
                onClick = { offset ->
                    annotatedText.getStringAnnotations(tag = "URL", start = offset, end = offset)
                        .firstOrNull()?.let { annotation ->
                            uriHandler.openUri(annotation.item)
                        }
                }
            )
        }
    }
}

@Composable
fun RegisterTopBar(
    title: String,
    onBack: () -> Unit
) {
    val backDesc = stringResource(id = R.string.common_back)
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = MaterialTheme.spacing.small),
            contentAlignment = Alignment.CenterStart
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.bounceClick { onBack() }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = backDesc,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 48.dp),
            )
        }
    }
}