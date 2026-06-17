package com.tenli.oneview.ui.features.auth.forgot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tenli.oneview.data.repository.AuthRepository
import com.tenli.oneview.ui.utils.ValidationUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ForgotPassViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ForgotPassUiState())
    val uiState = _uiState.asStateFlow()

    // Để bắn thông báo lỗi/thành công sang Screen
    private val _event = MutableSharedFlow<ForgotPassEvent>()
    val event = _event.asSharedFlow()

    // Cập nhật từng trường một cách độc lập
    fun onEmailChange(email: String) = _uiState.update { it.copy(email = email) }
    fun onOtpChange(otp: String) = _uiState.update { it.copy(otp = otp) }
    fun onPasswordChange(p: String, confirmPassword: String) = _uiState.update { it.copy(password = p) }
    fun onConfirmPasswordChange(cp: String) = _uiState.update { it.copy(confirmPassword = cp) }

    // Điều khiển con mắt ẩn/hiện mật khẩu
    fun togglePassword() = _uiState.update { it.copy(isShowPassword = !it.isShowPassword) }
    fun toggleConfirmPassword() = _uiState.update { it.copy(isShowConfirmPassword = !it.isShowConfirmPassword) }

    // BƯỚC 1: Xác thực Email
    fun verifyEmail() {
        if (!ValidationUtils.validateEmail(_uiState.value.email)) {
            viewModelScope.launch { _event.emit(ForgotPassEvent.ShowError("Email không đúng định dạng")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = authRepository.verifyEmail(_uiState.value.email, reset = true)
                _uiState.update { it.copy(isLoading = false) }

                if (result.isSuccess) {
                    _uiState.update { it.copy(step = ForgotPassStep.OTP) }
                } else {
                    _event.emit(ForgotPassEvent.ShowError("Email này chưa được đăng ký tài khoản"))
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                _event.emit(ForgotPassEvent.ShowError("Lỗi kết nối máy chủ"))
            }
        }
    }

    // BƯỚC 2: Xác thực OTP (Cần thêm hàm này vì UI sẽ gọi)
    fun verifyOtp() {
        val state = _uiState.value
        if (state.otp.length < 6) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = authRepository.verifyOTP(state.email, state.otp)
                _uiState.update { it.copy(isLoading = false) }

                if (result.isSuccess) {
                    val authCode = result.getOrNull() ?: ""
                    _uiState.update { it.copy(step = ForgotPassStep.NEW_PASSWORD, authCode = authCode) }
                } else {
                    _event.emit(ForgotPassEvent.ShowError("Mã OTP không chính xác hoặc đã hết hạn"))
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                _event.emit(ForgotPassEvent.ShowError("Không thể xác thực mã OTP"))
            }
        }
    }

    // BƯỚC 3: Đặt lại mật khẩu
    fun resetPassword() {
        val state = _uiState.value

        // Kiểm tra độ mạnh mật khẩu (dùng hàm Regex mới sửa)
        if (!ValidationUtils.isPasswordStrong(state.password)) {
            viewModelScope.launch { _event.emit(ForgotPassEvent.ShowError("Mật khẩu phải từ 6-20 ký tự, có cả chữ và số")) }
            return
        }

        // Kiểm tra khớp mật khẩu
        if (!ValidationUtils.doPasswordsMatch(state.password, state.confirmPassword)) {
            viewModelScope.launch { _event.emit(ForgotPassEvent.ShowError("Mật khẩu xác nhận không khớp")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = authRepository.resetPassword(state.email, state.password, state.authCode)

                _uiState.update { it.copy(isLoading = false) }

                if (result.isSuccess) {
                    _event.emit(ForgotPassEvent.ResetSuccess)
                } else {
                    _event.emit(ForgotPassEvent.ShowError("Có lỗi xảy ra, vui lòng thử lại sau"))
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                _event.emit(ForgotPassEvent.ShowError("Lỗi kết nối máy chủ"))
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as com.tenli.oneview.TenliApp
                ForgotPassViewModel(application.container.authRepository)
            }
        }
    }
}