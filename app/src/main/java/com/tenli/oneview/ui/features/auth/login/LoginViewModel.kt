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
                        handleLoginSuccess(loginData, domain)
                        _event.emit(LoginEvent.LoginSuccess)
                    } else {
                        _event.emit(LoginEvent.ShowError("Dữ liệu phản hồi rỗng"))
                    }
                } else {
                    val exception = result.exceptionOrNull()
                    val errorMsg = exception?.message ?: "Unknown error"
                    
                    if (errorMsg.contains("failed with code 401") || errorMsg.contains("failed with code 400")) {
                        _event.emit(LoginEvent.ShowError("Thông tin tài khoản hoặc mật khẩu không chính xác."))
                    } else if (exception is java.net.UnknownHostException || errorMsg.contains("Unable to resolve host")) {
                        _event.emit(LoginEvent.ShowError("Không thể kết nối máy chủ. Vui lòng kiểm tra lại Domain dự án hoặc mạng."))
                    } else if (exception is java.net.SocketTimeoutException || errorMsg.contains("timeout")) {
                        _event.emit(LoginEvent.ShowError("Kết nối mạng quá hạn, vui lòng thử lại."))
                    } else {
                        _event.emit(LoginEvent.ShowError("Đã có lỗi xảy ra. Vui lòng thử lại sau."))
                    }
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: ""
                if (e is java.net.UnknownHostException || errorMsg.contains("Unable to resolve host")) {
                    _event.emit(LoginEvent.ShowError("Không thể kết nối máy chủ. Vui lòng kiểm tra lại Domain dự án hoặc mạng."))
                } else if (e is java.net.SocketTimeoutException || errorMsg.contains("timeout")) {
                    _event.emit(LoginEvent.ShowError("Kết nối mạng quá hạn, vui lòng thử lại."))
                } else {
                    _event.emit(LoginEvent.ShowError("Đã có lỗi xảy ra. Vui lòng thử lại sau."))
                }
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

    private fun handleLoginSuccess(loginData: LoginResponseData, domain: String) {
        val formattedDomain = if (!domain.startsWith("http://") && !domain.startsWith("https://")) "http://$domain" else domain
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