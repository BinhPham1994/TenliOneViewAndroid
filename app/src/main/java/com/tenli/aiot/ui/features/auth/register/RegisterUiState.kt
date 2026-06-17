package com.tenli.aiot.ui.features.auth.register

import androidx.compose.runtime.Immutable
import com.tenli.aiot.ui.utils.UiText

enum class RegisterStep { EMAIL, OTP, DETAILS }

@Immutable
data class RegisterUiState(
    val step: RegisterStep = RegisterStep.EMAIL,
    val email: String = "",
    val otp: String = "",
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isAccepted: Boolean = false,
    val isShowPassword: Boolean = false,
    val isShowConfirmPassword: Boolean = false,
    val isLoading: Boolean = false,
    val authCode: String = "",
)

sealed class RegisterEvent {
    object RegisterSuccess : RegisterEvent()
    data class ShowError(val message: UiText) : RegisterEvent()
}