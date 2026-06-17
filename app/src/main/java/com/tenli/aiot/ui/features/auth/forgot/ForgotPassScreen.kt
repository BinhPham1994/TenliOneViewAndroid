package com.tenli.aiot.ui.features.auth.forgot

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tenli.aiot.R
import com.tenli.aiot.ui.features.auth.register.RegisterTopBar
import com.tenli.aiot.ui.theme.spacing
import kotlinx.coroutines.launch

@Composable
fun ForgotPassScreen(
    viewModel: ForgotPassViewModel = viewModel(factory = ForgotPassViewModel.Factory),
    onFinish: () -> Unit
) {
    val titleMain = stringResource(id = R.string.forgot_pass_title_main)           // "Quên mật khẩu"
    val titleOtp = stringResource(id = R.string.forgot_pass_title_otp)             // "Xác thực Email"
    val titleNewPass = stringResource(id = R.string.forgot_pass_title_new_pass)     // "Đặt lại mật khẩu"
    val btnReset = stringResource(id = R.string.forgot_pass_btn_reset)             // "Đổi mật khẩu"
    val btnContinue = stringResource(id = R.string.forgot_pass_btn_continue)       // "Tiếp tục"
    val successMsg = stringResource(id = R.string.forgot_pass_success_msg)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is ForgotPassEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
                is ForgotPassEvent.ResetSuccess -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(successMsg)
                    }
                    kotlinx.coroutines.delay(1000)
                    onFinish()
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            val title = when (uiState.step) {
                ForgotPassStep.EMAIL -> titleMain
                ForgotPassStep.OTP -> titleOtp
                ForgotPassStep.NEW_PASSWORD -> titleNewPass
            }
            RegisterTopBar(title = title, onBack = onFinish)
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
                        fadeIn() + slideInHorizontally { it } togetherWith
                                fadeOut() + slideOutHorizontally { -it }
                    },
                    label = "ForgotPassTransition"
                ) { step ->
                    when (step) {
                        ForgotPassStep.EMAIL -> ForgotEmailStep(
                            email = uiState.email,
                            onEmailChange = { viewModel.onEmailChange(it) }
                        )

                        ForgotPassStep.OTP -> ForgotOtpStep(
                            email = uiState.email,
                            otp = uiState.otp,
                            onOtpChange = { viewModel.onOtpChange(it) }
                        )

                        ForgotPassStep.NEW_PASSWORD -> ForgotNewPasswordStep(
                            uiState = uiState,
                            viewModel = viewModel
                        )
                    }
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraLarge))
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        when (uiState.step) {
                            ForgotPassStep.EMAIL -> viewModel.verifyEmail()
                            ForgotPassStep.OTP -> viewModel.verifyOtp()
                            ForgotPassStep.NEW_PASSWORD -> viewModel.resetPassword()
                        }
                    },
                    enabled = uiState.isButtonActive && !uiState.isLoading,
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
                    elevation = if (uiState.isButtonActive && !uiState.isLoading)
                        ButtonDefaults.buttonElevation() else ButtonDefaults.buttonElevation(0.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(MaterialTheme.spacing.iconMedium),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (uiState.step == ForgotPassStep.NEW_PASSWORD) btnReset else btnContinue,
                            style = MaterialTheme.typography.titleMedium // 18sp Bold
                        )
                    }
                }
            }
        }
    }
}