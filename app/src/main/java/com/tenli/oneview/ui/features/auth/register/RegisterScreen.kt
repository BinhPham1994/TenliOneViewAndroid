package com.tenli.oneview.ui.features.auth.register

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tenli.oneview.R
import com.tenli.oneview.ui.theme.spacing
import com.tenli.oneview.ui.utils.bounceClick
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = viewModel(factory = RegisterViewModel.Factory),
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val successMsg = stringResource(id = R.string.register_success_msg)         // "Tạo tài khoản thành công!"
    val btnFinish = stringResource(id = R.string.register_btn_finish)           // "Đăng ký"
    val btnContinue = stringResource(id = R.string.register_btn_continue)       // "Tiếp tục"
    val alreadyAccountText = stringResource(id = R.string.register_already_have_account) // "Bạn đã có tài khoản? "
    val loginActionText = stringResource(id = R.string.register_login_now)

    val titleEmail = stringResource(id = R.string.register_email_label)
    val titleOtp = stringResource(id = R.string.register_otp_label)
    val titleDetails = stringResource(id = R.string.register_details_label)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is RegisterEvent.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = event.message.asString(context),
//                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is RegisterEvent.RegisterSuccess -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(message = successMsg)
                    }
                    delay(1000)
                    onFinish()
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            val currentTitle = when (uiState.step) {
                RegisterStep.EMAIL -> titleEmail
                RegisterStep.OTP -> titleOtp
                RegisterStep.DETAILS -> titleDetails
            }
            RegisterTopBar(title = currentTitle, onBack = onFinish)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = MaterialTheme.spacing.large)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                AnimatedContent(
                    targetState = uiState.step,
                    transitionSpec = {
                        fadeIn() + slideInHorizontally { it } togetherWith fadeOut() + slideOutHorizontally { -it }
                    },
                    label = "RegisterStepTransition"
                ) { step ->
                    when (step) {
                        RegisterStep.EMAIL -> RegisterEmailStep(uiState, viewModel)
                        RegisterStep.OTP -> RegisterOtpStep(uiState, viewModel)
                        RegisterStep.DETAILS -> RegisterDetailsStep(uiState, viewModel)
                    }
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraLarge))

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        when (uiState.step) {
                            RegisterStep.EMAIL -> viewModel.verifyEmail()
                            RegisterStep.OTP -> viewModel.verifyOtp()
                            RegisterStep.DETAILS -> viewModel.createAccount()
                        }
                    },
                    enabled = when (uiState.step) {
                        RegisterStep.EMAIL -> uiState.email.isNotEmpty() && !uiState.isLoading
                        RegisterStep.OTP -> uiState.otp.isNotEmpty() && !uiState.isLoading
                        RegisterStep.DETAILS -> uiState.isAccepted && uiState.username.isNotEmpty() && !uiState.isLoading
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
                    elevation = if (!uiState.isLoading) ButtonDefaults.buttonElevation() else ButtonDefaults.buttonElevation(0.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(MaterialTheme.spacing.iconMedium),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (uiState.step == RegisterStep.DETAILS) btnFinish else btnContinue,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = MaterialTheme.spacing.large),
                horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = alreadyAccountText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = loginActionText,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleSmall, // 16sp Bold
                    modifier = Modifier
                        .clip(RoundedCornerShape(MaterialTheme.spacing.extraSmall))
                        .bounceClick { onFinish() }
                        .padding(horizontal = MaterialTheme.spacing.extraSmall, vertical = 2.dp)
                )
            }
        }
    }
}