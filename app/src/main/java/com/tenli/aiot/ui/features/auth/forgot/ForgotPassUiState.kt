package com.tenli.aiot.ui.features.auth.forgot

import androidx.compose.runtime.Immutable

enum class ForgotPassStep { EMAIL, OTP, NEW_PASSWORD }

@Immutable
data class ForgotPassUiState(
    val step: ForgotPassStep = ForgotPassStep.EMAIL,
    val email: String = "",
    val otp: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val authCode: String = "",
    val isShowPassword: Boolean = false,
    val isShowConfirmPassword: Boolean = false,
    val isLoading: Boolean = false
) {
    val isButtonActive: Boolean get() = when (step) {
        // Chỉ cần có chữ và có dấu @ là nút sẽ sáng lên ngay
        ForgotPassStep.EMAIL -> email.isNotEmpty() && email.contains("@")
        ForgotPassStep.OTP -> otp.length >= 6
        ForgotPassStep.NEW_PASSWORD -> password.isNotEmpty() && confirmPassword.isNotEmpty()
    }
}

sealed class ForgotPassEvent {
    data class ShowError(val message: String) : ForgotPassEvent()
    object ResetSuccess : ForgotPassEvent()
}