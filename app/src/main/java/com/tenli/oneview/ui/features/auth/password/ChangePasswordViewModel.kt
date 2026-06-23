package com.tenli.oneview.ui.features.auth.password

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.data.network.api.VmsApi
import com.tenli.oneview.data.network.retrofit.LoginAuthClient
import com.tenli.oneview.model.network.ChangePasswordModel
import com.tenli.oneview.ui.utils.ValidationUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChangePasswordViewModel(application: Application) : AndroidViewModel(application) {

    private val vmsApi = LoginAuthClient.create(VmsApi::class.java)

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<ChangePasswordEvent>()
    val event = _event.asSharedFlow()

    fun onOldPasswordChanged(password: String) {
        _uiState.update { it.copy(oldPassword = password, error = null) }
        checkCanSave()
    }

    fun onNewPasswordChanged(password: String) {
        _uiState.update { it.copy(newPassword = password, error = null) }
        checkCanSave()
    }

    fun onConfirmPasswordChanged(password: String) {
        _uiState.update { it.copy(confirmPassword = password, error = null) }
        checkCanSave()
    }

    private fun checkCanSave() {
        val state = _uiState.value
        val canSave = state.oldPassword.isNotEmpty() &&
                state.newPassword.isNotEmpty() &&
                state.confirmPassword.isNotEmpty()
        _uiState.update { it.copy(isSaveActive = canSave) }
    }

    fun toggleShowOldPassword() {
        _uiState.update { it.copy(showOldPassword = !it.showOldPassword) }
    }

    fun toggleShowNewPassword() {
        _uiState.update { it.copy(showNewPassword = !it.showNewPassword) }
    }

    fun toggleShowConfirmPassword() {
        _uiState.update { it.copy(showConfirmPassword = !it.showConfirmPassword) }
    }

    fun changePassword() {
        val state = _uiState.value
        
        if (state.newPassword != state.confirmPassword) {
            _uiState.update { it.copy(error = "Mật khẩu xác nhận không khớp") }
            return
        }

        if (!ValidationUtils.isPasswordStrong(state.newPassword)) {
            _uiState.update { it.copy(error = "Mật khẩu mới phải từ 6-20 ký tự, bao gồm cả chữ và số") }
            return
        }

        val userId = UserSession.userData?.id
        if (userId == null) {
            _uiState.update { it.copy(error = "Không tìm thấy thông tin người dùng") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val request = ChangePasswordModel(
                    userId = userId,
                    oldPassword = state.oldPassword,
                    newPassword = state.newPassword
                )
                val response = vmsApi.changePassword(request)
                if (response.isSuccessful) {
                    _event.emit(ChangePasswordEvent.Success)
                } else {
                    val code = response.code()
                    if (code == 400 || code == 401) {
                        _uiState.update { it.copy(error = "Mật khẩu hiện tại không chính xác") }
                    } else {
                        _uiState.update { it.copy(error = "Đã có lỗi xảy ra. Vui lòng thử lại sau.") }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Không thể kết nối máy chủ. Vui lòng thử lại.") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                ChangePasswordViewModel(application)
            }
        }
    }
}

data class ChangePasswordUiState(
    val oldPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val showOldPassword: Boolean = false,
    val showNewPassword: Boolean = false,
    val showConfirmPassword: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaveActive: Boolean = false
)

sealed class ChangePasswordEvent {
    object Success : ChangePasswordEvent()
}
