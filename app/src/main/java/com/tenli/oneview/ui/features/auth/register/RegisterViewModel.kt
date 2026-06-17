package com.tenli.oneview.ui.features.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tenli.oneview.R
import com.tenli.oneview.data.repository.AuthRepository
import com.tenli.oneview.ui.utils.UiText
import com.tenli.oneview.ui.utils.ValidationUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<RegisterEvent>()
    val event = _event.asSharedFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onOtpChange(otp: String) {
        _uiState.update { it.copy(otp = otp) }
    }

    fun onUsernameChange(name: String) {
        _uiState.update { it.copy(username = name) }
    }

    fun onPasswordChange(p: String) {
        _uiState.update { it.copy(password = p) }
    }

    fun onConfirmPasswordChange(cp: String) {
        _uiState.update { it.copy(confirmPassword = cp) }
    }

    fun onAcceptChange(accepted: Boolean) {
        _uiState.update { it.copy(isAccepted = accepted) }
    }

    fun togglePassword() {
        _uiState.update { it.copy(isShowPassword = !it.isShowPassword) }
    }

    fun toggleConfirmPassword() {
        _uiState.update { it.copy(isShowConfirmPassword = !it.isShowConfirmPassword) }
    }

    private fun sendError(uiText: UiText) {
        viewModelScope.launch {
            _event.emit(RegisterEvent.ShowError(uiText))
        }
    }

    fun verifyEmail() {
        val email = _uiState.value.email

        if (!ValidationUtils.validateEmail(email)) {
            sendError(UiText.StringResource(R.string.err_invalid_email))
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                val result = authRepository.verifyEmail(email, false)

                if (result.isSuccess) {
                    _uiState.update {
                        it.copy(
                            step = RegisterStep.OTP,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                    val errorMsg = result.exceptionOrNull()?.message ?: ""
                    val error = when {
                        errorMsg.contains("400") -> UiText.StringResource(R.string.err_email_unavailable)
                        errorMsg.contains("429") -> UiText.StringResource(R.string.err_too_many_requests)
                        else -> UiText.StringResource(R.string.err_verify_failed)
                    }
                    sendError(error)
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                sendError(UiText.StringResource(R.string.err_no_internet))
            }
        }
    }

    fun verifyOtp() {
        val email = _uiState.value.email
        val otp = _uiState.value.otp
        if (otp.length < 6) {
            sendError(UiText.StringResource(R.string.err_otp_too_short))
            return
        }
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val result = authRepository.verifyOTP(email, otp)
                if (result.isSuccess) {
                    val authCode = result.getOrNull() ?: ""
                    _uiState.update {
                        it.copy(
                            step = RegisterStep.DETAILS,
                            authCode = authCode,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                    val errorMsg = result.exceptionOrNull()?.message ?: ""
                    val error = when {
                        errorMsg.contains("400") -> UiText.StringResource(R.string.err_otp_incorrect)
                        else -> UiText.StringResource(R.string.err_verify_failed)
                    }
                    sendError(error)
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                sendError(UiText.StringResource(R.string.err_no_internet))
            }
        }
    }

    fun createAccount() {
        val state = _uiState.value
        val username = state.username.trim()
        val pw = state.password
        val cpw = state.confirmPassword

        if (!ValidationUtils.isValidUsername(username)) {
            val errorRes = when {
                username.length < 3 -> R.string.err_username_too_short
                username.length > 20 -> R.string.err_username_too_long
                else -> R.string.err_username_invalid_chars
            }
            sendError(UiText.StringResource(errorRes))
            return
        }

        if (!ValidationUtils.isPasswordStrong(pw)) {
            sendError(UiText.StringResource(R.string.err_password_weak))
            return
        }

        if (!ValidationUtils.doPasswordsMatch(pw, cpw)) {
            sendError(UiText.StringResource(R.string.err_passwords_mismatch))
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val result = authRepository.createAccount(state.username, state.email, state.password, state.authCode)
                _uiState.update { it.copy(isLoading = false) }
                if (result.isSuccess) {
                    _event.emit(RegisterEvent.RegisterSuccess)
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: ""
                    val error = when {
                        errorMsg.contains("409") -> UiText.StringResource(R.string.err_conflict_user_email)
                        else -> UiText.StringResource(R.string.err_register_failed)
                    }
                    sendError(error)
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                sendError(UiText.StringResource(R.string.err_no_internet))
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as com.tenli.oneview.TenliApp
                RegisterViewModel(application.container.authRepository)
            }
        }
    }
}