package com.tenli.oneview.ui.features.auth.login

import android.app.Application
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tenli.oneview.data.local.GlobalData
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.data.repository.AuthRepository
import com.tenli.oneview.model.network.LogInModel
import com.tenli.oneview.model.network.LoginResponseData
import com.tenli.oneview.ui.component.MobileTopPopupManager
import com.tenli.oneview.ui.utils.AppKeys
import com.tenli.oneview.ui.utils.LocaleManager
import com.tenli.oneview.util.DispatcherProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import java.net.UnknownHostException

private const val DOMAIN_KEY = "DOMAIN_KEY"

class LoginViewModel(
    application: Application,
    private val authRepository: AuthRepository,
    private val dispatcherProvider: DispatcherProvider
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()
    private val _event = MutableSharedFlow<LoginEvent>()
    val event = _event.asSharedFlow()

    private var loginJob: Job? = null

    init {
        loadSavedCredentials()
    }

    fun loadSavedCredentials() {
        val prefs = GlobalData.preferences
        val rememberMe = prefs.getBoolean(AppKeys.REMEMBER_ME_KEY, false)
        val savedDomain = prefs.getString(DOMAIN_KEY, "").orEmpty()
        val savedUser = if (rememberMe) prefs.getString(AppKeys.USERNAME_KEY, "").orEmpty() else ""
        val savedPass = if (rememberMe) prefs.getString(AppKeys.PASSWORD_KEY, "").orEmpty() else ""
        _uiState.update {
            it.copy(
                domain = savedDomain,
                email = savedUser,
                password = savedPass,
                rememberMe = rememberMe,
                showPassword = false
            )
        }
    }

    fun onRememberMeChanged(rememberMe: Boolean) {
        _uiState.update { it.copy(rememberMe = rememberMe) }
        if (!rememberMe) {
            GlobalData.preferences.edit {
                putBoolean(AppKeys.REMEMBER_ME_KEY, false)
                remove(AppKeys.USERNAME_KEY)
                remove(AppKeys.PASSWORD_KEY)
            }
        }
    }

    fun onDomainChanged(domain: String) {
        _uiState.update { it.copy(domain = domain) }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onPasswordChanged(pass: String) {
        _uiState.update { it.copy(password = pass) }
    }

    fun toggleShowPassword() {
        _uiState.update { it.copy(showPassword = !it.showPassword) }
    }

    fun login() {
        // Chặn bấm liên tiếp khi request trước chưa xong
        if (loginJob?.isActive == true) return

        val state = _uiState.value
        val email = state.email.trim()
        val domain = state.domain.trim()
        val password = state.password
        val isEn = LocaleManager.getLocale(getApplication()).startsWith("en")

        val validationError = when {
            domain.isEmpty() -> if (isEn) "Please enter project domain" else "Vui lòng nhập domain dự án"
            email.isEmpty() -> if (isEn) "Please enter username" else "Vui lòng nhập tên đăng nhập"
            password.isEmpty() -> if (isEn) "Please enter password" else "Vui lòng nhập mật khẩu"
            else -> null
        }
        if (validationError != null) {
            MobileTopPopupManager.error(validationError)
            return
        }

        loginJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = authRepository.loginVms(domain, LogInModel(username = email, password = password))
                val loginData = result.getOrNull()
                when {
                    result.isFailure -> MobileTopPopupManager.error(
                        loginErrorMessage(result.exceptionOrNull(), isEn, isUnexpected = false)
                    )
                    loginData == null -> MobileTopPopupManager.error(
                        if (isEn) "Empty response data" else "Dữ liệu phản hồi rỗng"
                    )
                    else -> {
                        handleLoginSuccess(loginData, domain, email, password, state.rememberMe)
                        _event.emit(LoginEvent.LoginSuccess)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                MobileTopPopupManager.error(loginErrorMessage(e, isEn, isUnexpected = true))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Ánh xạ lỗi đăng nhập sang thông báo thân thiện.
     * @param isUnexpected true khi lỗi phát sinh ngoài [AuthRepository] (không phải lỗi xác thực).
     */
    private fun loginErrorMessage(error: Throwable?, isEn: Boolean, isUnexpected: Boolean): String {
        val errorMsg = error?.message.orEmpty()
        return when {
            !isUnexpected && (errorMsg.contains("failed with code 401") ||
                errorMsg.contains("failed with code 400") ||
                errorMsg.contains("Failed to authenticate")) ->
                if (isEn) "Invalid login credentials" else "Thông tin đăng nhập không chính xác"
            error is UnknownHostException || errorMsg.contains("Unable to resolve host") ->
                if (isEn) "Cannot connect to server. Please check your connection." else "Không thể kết nối tới máy chủ. Vui lòng kiểm tra đường truyền."
            error is SocketTimeoutException || errorMsg.contains("timeout") ->
                if (isEn) "Connection timed out, please try again." else "Kết nối mạng quá hạn, vui lòng thử lại."
            isUnexpected ->
                if (isEn) "An error occurred, please try again later." else "Có lỗi xảy ra, vui lòng thử lại sau."
            else ->
                if (isEn) "Invalid login credentials" else "Thông tin đăng nhập không chính xác"
        }
    }

    private suspend fun handleLoginSuccess(
        loginData: LoginResponseData,
        domain: String,
        username: String,
        password: String,
        rememberMe: Boolean
    ) {
        val formattedDomain = if (!domain.startsWith("http://") && !domain.startsWith("https://")) "http://$domain" else domain
        UserSession.apply {
            userData = loginData.target
            accessToken = loginData.credential.accessToken
            refreshToken = loginData.credential.refreshToken
            this.domain = formattedDomain
            savedUsername = username
            savedPassword = password
        }
        // Serialize JSON + ghi SharedPreferences ngoài Main thread.
        // Việc đồng bộ notification_settings & đăng ký FCM token được thực hiện tập trung tại MainViewModel.init
        // ngay khi chuyển sang MainScreen để đảm bảo lấy đúng trạng thái bật/tắt thông báo của tài khoản mới trước khi đăng ký token.
        withContext(dispatcherProvider.io) {
            val userDataJson = GlobalData.gson.toJson(loginData.target)
            GlobalData.preferences.edit {
                putString(AppKeys.USER_DATA_KEY, userDataJson)
                putString(AppKeys.ACCESS_TOKEN_KEY, loginData.credential.accessToken)
                putString(AppKeys.REFRESH_TOKEN_KEY, loginData.credential.refreshToken)
                putString(DOMAIN_KEY, formattedDomain)
                putBoolean(AppKeys.REMEMBER_ME_KEY, rememberMe)
                if (rememberMe) {
                    putString(AppKeys.USERNAME_KEY, username)
                    putString(AppKeys.PASSWORD_KEY, password)
                } else {
                    remove(AppKeys.USERNAME_KEY)
                    remove(AppKeys.PASSWORD_KEY)
                }
                remove("push_notifications_enabled")
            }
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
