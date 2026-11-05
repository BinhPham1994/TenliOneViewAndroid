package com.tenli.oneview.ui.features.auth.login

import android.app.Application
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
import com.tenli.oneview.ui.component.MobileTopPopupManager
import com.tenli.oneview.ui.utils.AppKeys
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

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()
    private val _event = MutableSharedFlow<LoginEvent>()
    val event = _event.asSharedFlow()

    private var notifyToken: String = ""

    init {
        loadSavedCredentials()
        viewModelScope.launch {
            try {
                notifyToken = FirebaseMessaging.getInstance().token.await()
            } catch (_: Exception) {
            }
        }
    }

    fun loadSavedCredentials() {
        val rememberMe = GlobalData.preferences.getBoolean(AppKeys.REMEMBER_ME_KEY, false)
        val savedDomain = GlobalData.preferences.getString("DOMAIN_KEY", "") ?: ""
        val savedUser = if (rememberMe) GlobalData.preferences.getString(AppKeys.USERNAME_KEY, "") ?: "" else ""
        val savedPass = if (rememberMe) GlobalData.preferences.getString(AppKeys.PASSWORD_KEY, "") ?: "" else ""
        _uiState.update {
            it.copy(
                domain = savedDomain,
                email = savedUser,
                password = savedPass,
                rememberMe = rememberMe,
                showPassword = false,
                errorMessage = null,
                isLoginActive = savedDomain.isNotEmpty() && savedUser.isNotEmpty() && savedPass.isNotEmpty()
            )
        }
    }

    fun onRememberMeChanged(rememberMe: Boolean) {
        _uiState.update { it.copy(rememberMe = rememberMe) }
    }

    fun onDomainChanged(domain: String) {
        _uiState.update { it.copy(domain = domain, errorMessage = null, isLoginActive = domain.isNotEmpty() && it.email.isNotEmpty() && it.password.isNotEmpty()) }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null, isLoginActive = it.domain.isNotEmpty() && email.isNotEmpty() && it.password.isNotEmpty()) }
    }

    fun onPasswordChanged(pass: String) {
        _uiState.update { it.copy(password = pass, errorMessage = null, isLoginActive = pass.isNotEmpty() && it.email.isNotEmpty() && it.domain.isNotEmpty()) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun toggleShowPassword() {
        _uiState.update { it.copy(showPassword = !it.showPassword) }
    }

    fun login() {
        val email = _uiState.value.email.trim()
        val domain = _uiState.value.domain.trim()
        val password = _uiState.value.password
        
        if (domain.isEmpty()) {
            MobileTopPopupManager.error("Vui lòng nhập domain dự án")
            return
        }
        
        if (email.isEmpty()) {
            MobileTopPopupManager.error("Vui lòng nhập tên đăng nhập")
            return
        }

        if (password.isEmpty()) {
            MobileTopPopupManager.error("Vui lòng nhập mật khẩu")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val request = com.tenli.oneview.model.network.LogInModel(
                username = email,
                password = password
            )
            try {
                val result = authRepository.loginVms(domain, request)
                if (result.isSuccess) {
                    val loginData = result.getOrNull()
                    if (loginData != null) {
                        handleLoginSuccess(loginData, domain)
                        _event.emit(LoginEvent.LoginSuccess)
                    } else {
                        MobileTopPopupManager.error("Dữ liệu phản hồi rỗng")
                    }
                } else {
                    val exception = result.exceptionOrNull()
                    val errorMsg = exception?.message ?: "Unknown error"
                    
                    val msg = if (errorMsg.contains("failed with code 401") || errorMsg.contains("failed with code 400") || errorMsg.contains("Failed to authenticate")) {
                        "Thông tin đăng nhập không chính xác"
                    } else if (exception is java.net.UnknownHostException || errorMsg.contains("Unable to resolve host")) {
                        "Không thể kết nối tới máy chủ. Vui lòng kiểm tra đường truyền."
                    } else if (exception is java.net.SocketTimeoutException || errorMsg.contains("timeout")) {
                        "Kết nối mạng quá hạn, vui lòng thử lại."
                    } else {
                        "Thông tin đăng nhập không chính xác"
                    }
                    MobileTopPopupManager.error(msg)
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: ""
                val msg = if (e is java.net.UnknownHostException || errorMsg.contains("Unable to resolve host")) {
                    "Không thể kết nối tới máy chủ. Vui lòng kiểm tra đường truyền."
                } else if (e is java.net.SocketTimeoutException || errorMsg.contains("timeout")) {
                    "Kết nối mạng quá hạn, vui lòng thử lại."
                } else {
                    "Có lỗi xảy ra, vui lòng thử lại sau."
                }
                MobileTopPopupManager.error(msg)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // Removed Google login logic

    private fun handleLoginSuccess(loginData: LoginResponseData, domain: String) {
        val formattedDomain = if (!domain.startsWith("http://") && !domain.startsWith("https://")) "http://$domain" else domain
        val rememberMe = _uiState.value.rememberMe
        UserSession.apply {
            userData = loginData.target
            accessToken = loginData.credential.accessToken
            refreshToken = loginData.credential.refreshToken
            this.domain = formattedDomain
            savedUsername = _uiState.value.email
            savedPassword = _uiState.value.password
        }
        val userDataJson = Gson().toJson(loginData.target)
        GlobalData.preferences.edit {
            putString(AppKeys.USER_DATA_KEY, userDataJson)
            putString(AppKeys.ACCESS_TOKEN_KEY, loginData.credential.accessToken)
            putString(AppKeys.REFRESH_TOKEN_KEY, loginData.credential.refreshToken)
            putString("DOMAIN_KEY", formattedDomain)
            putBoolean(AppKeys.REMEMBER_ME_KEY, rememberMe)
            putString(AppKeys.USERNAME_KEY, _uiState.value.email)
            putString(AppKeys.PASSWORD_KEY, _uiState.value.password)
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