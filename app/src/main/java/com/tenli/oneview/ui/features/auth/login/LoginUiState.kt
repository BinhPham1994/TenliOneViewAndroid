package com.tenli.oneview.ui.features.auth.login

import androidx.compose.runtime.Immutable

@Immutable
data class LoginUiState(
    val domain: String = "",
    val email: String = "",
    val password: String = "",
    val showPassword: Boolean = false,
    val isLoading: Boolean = false,
    val isVietnamese: Boolean = true,
    val isLoginActive: Boolean = false,
    val errorMessage: String? = null
)

sealed class LoginEvent {
    object LoginSuccess : LoginEvent()
    data class ShowError(val message: String) : LoginEvent()
}