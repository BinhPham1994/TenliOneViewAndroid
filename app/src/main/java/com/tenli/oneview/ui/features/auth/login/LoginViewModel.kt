package com.tenli.oneview.ui.features.auth.login

import android.annotation.SuppressLint
import android.app.Application
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.tenli.oneview.data.local.GlobalData
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.data.repository.AuthRepository
import com.tenli.oneview.model.network.LoginResponseData
import com.tenli.oneview.ui.utils.AppConfig
import com.tenli.oneview.ui.utils.AppKeys
import com.tenli.oneview.ui.utils.ValidationUtils
import com.tenli.oneview.util.DispatcherProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel(
    application: Application,
    private val authRepository: AuthRepository,
    private val dispatcherProvider: DispatcherProvider
) : AndroidViewModel(application) {

    init {
        viewModelScope.launch {
            try {
                notifyToken = FirebaseMessaging.getInstance().token.await()
            } catch (_: Exception) {
            }
        }
    }

    fun onDomainChanged(domain: String) {
        _uiState.update { it.copy(domain = domain, isLoginActive = domain.isNotEmpty() && it.email.isNotEmpty() && it.password.isNotEmpty()) }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, isLoginActive = it.domain.isNotEmpty() && email.isNotEmpty() && it.password.isNotEmpty()) }
    }

    fun onPasswordChanged(pass: String) {
        _uiState.update { it.copy(password = pass, isLoginActive = pass.isNotEmpty() && it.email.isNotEmpty() && it.domain.isNotEmpty()) }
    }

    fun toggleShowPassword() {
        _uiState.update { it.copy(showPassword = !it.showPassword) }
    }

    fun login() {
        val email = _uiState.value.email
        val domain = _uiState.value.domain
        
        // Removed email validation because VMS username might not be an email
        if (email.isEmpty()) {
            viewModelScope.launch { _event.emit(LoginEvent.ShowError("Tài khoản không được để trống")) }
            return
        }
        
        if (domain.isEmpty()) {
            viewModelScope.launch { _event.emit(LoginEvent.ShowError("Domain không được để trống")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val request = com.tenli.oneview.model.network.LogInModel(
                username = email,
                password = _uiState.value.password
            )
            try {
                val result = authRepository.loginVms(domain, request)
                if (result.isSuccess) {
                    val loginData = result.getOrNull()
                    if (loginData != null) {
                        handleLoginSuccess(loginData)
                        _event.emit(LoginEvent.LoginSuccess)
                    } else {
                        _event.emit(LoginEvent.ShowError("Dữ liệu phản hồi rỗng"))
                    }
                } else {
                    val errorMsg = result.exceptionOrNull()?.message ?: "Unknown error"
                    if (errorMsg.contains("failed with code 401") || errorMsg.contains("failed with code 400")) {
                        _event.emit(LoginEvent.ShowError("Tài khoản hoặc mật khẩu không chính xác"))
                    } else {
                        _event.emit(LoginEvent.ShowError("Lỗi: $errorMsg"))
                    }
                }
            } catch (e: Exception) {
                _event.emit(LoginEvent.ShowError("Lỗi hệ thống: ${e.message}"))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // Removed Google login logic

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()
    private val _event = MutableSharedFlow<LoginEvent>()
    val event = _event.asSharedFlow()

    private var notifyToken: String = ""

    private fun handleLoginSuccess(loginData: LoginResponseData) {
        UserSession.apply {
            userData = loginData.target
            accessToken = loginData.credential.accessToken
            refreshToken = loginData.credential.refreshToken
        }
        val userDataJson = Gson().toJson(loginData.target)
        GlobalData.preferences.edit {
            putString(AppKeys.USER_DATA_KEY, userDataJson)
            putString(AppKeys.ACCESS_TOKEN_KEY, loginData.credential.accessToken)
            putString(AppKeys.REFRESH_TOKEN_KEY, loginData.credential.refreshToken)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as com.tenli.oneview.TenliApp
                LoginViewModel(
                    application = application,
                    authRepository = application.container.authRepository,
                    dispatcherProvider = application.container.dispatcherProvider
                )
            }
        }
    }
}