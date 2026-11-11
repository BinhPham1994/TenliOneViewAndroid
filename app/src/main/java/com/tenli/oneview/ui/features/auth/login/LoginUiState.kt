package com.tenli.oneview.ui.features.auth.login

import androidx.compose.runtime.Immutable

@Immutable
data class LoginUiState(
    val domain: String = "",
    val isDomainFromQr: Boolean = false,
    val email: String = "",
    val password: String = "",
    val termsAccepted: Boolean = false,
    val showPassword: Boolean = false,
    val isLoading: Boolean = false
)

sealed interface LoginEvent {
    data object LoginSuccess : LoginEvent
}
