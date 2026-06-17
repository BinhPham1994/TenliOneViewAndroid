package com.tenli.aiot.ui.features.auth.login

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
import com.tenli.aiot.data.local.GlobalData
import com.tenli.aiot.data.local.UserSession
import com.tenli.aiot.data.repository.AuthRepository
import com.tenli.aiot.model.network.CreateUserOptions
import com.tenli.aiot.model.network.DeviceInfo
import com.tenli.aiot.model.network.LoginDeviceConfig
import com.tenli.aiot.model.network.LoginNotifyConfig
import com.tenli.aiot.model.network.LoginRequest
import com.tenli.aiot.model.network.LoginResponseData
import com.tenli.aiot.model.network.UserGroupOptions
import com.tenli.aiot.ui.utils.AppConfig
import com.tenli.aiot.ui.utils.AppKeys
import com.tenli.aiot.ui.utils.ValidationUtils
import com.tenli.aiot.util.DispatcherProvider
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

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, isLoginActive = email.isNotEmpty() && it.password.isNotEmpty()) }
    }

    fun onPasswordChanged(pass: String) {
        _uiState.update { it.copy(password = pass, isLoginActive = pass.isNotEmpty() && it.email.isNotEmpty()) }
    }

    fun toggleShowPassword() {
        _uiState.update { it.copy(showPassword = !it.showPassword) }
    }

    fun login() {
        val email = _uiState.value.email
        if (!ValidationUtils.validateEmail(email)) {
            viewModelScope.launch { _event.emit(LoginEvent.ShowError("Email không hợp lệ")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val loginRequest = createLoginRequest()
            android.util.Log.e("LoginAAAA", "JSON Đóng gói: " + com.google.gson.Gson().toJson(loginRequest))
            Log.e("LoginAAAA", loginRequest.toString())
            try {
                val result = authRepository.login(loginRequest)
                if (result.isSuccess) {
                    val loginData = result.getOrNull()
                    if (loginData != null) {
                        handleLoginSuccess(loginData)
                        _event.emit(LoginEvent.LoginSuccess)
                    } else {
                        _event.emit(LoginEvent.ShowError("Dữ liệu phản hồi rỗng"))
                    }
                } else {
                    _event.emit(LoginEvent.ShowError("Tài khoản hoặc mật khẩu không chính xác"))
                }
            } catch (e: Exception) {
                _event.emit(LoginEvent.ShowError("Lỗi kết nối: ${e.message}"))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun loginWithGoogle(firebaseIdToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val loginRequest = createLoginRequest(googleIdToken = firebaseIdToken)

            try {
                val result = authRepository.login(loginRequest)
                if (result.isSuccess) {
                    val loginData = result.getOrNull()
                    if (loginData != null) {
                        handleLoginSuccess(loginData)
                        _event.emit(LoginEvent.LoginSuccess)
                    } else {
                        _event.emit(LoginEvent.ShowError("Dữ liệu phản hồi rỗng"))
                    }
                } else {
                    val exception = result.exceptionOrNull()
                    _event.emit(LoginEvent.ShowError(exception?.message ?: "Đăng nhập Google thất bại"))
                }
            } catch (e: Exception) {
                _event.emit(LoginEvent.ShowError("Lỗi kết nối: ${e.message}"))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

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

    @SuppressLint("HardwareIds")
    private suspend fun createLoginRequest(googleIdToken: String = ""): LoginRequest {
        if (notifyToken.isEmpty()) {
            try {
                notifyToken = FirebaseMessaging.getInstance().token.await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        val context = getApplication<Application>()
        val deviceID = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown_device"

        return LoginRequest(
            email = if (googleIdToken.isEmpty()) _uiState.value.email else "",
            password = if (googleIdToken.isEmpty()) _uiState.value.password else "",
            googleIdToken = googleIdToken,

            notifyConfig = LoginNotifyConfig(token = notifyToken),
            device = LoginDeviceConfig(
                uuid = deviceID,
                model = Build.MODEL,
                name = AppConfig.getDeviceName(context),
                information = DeviceInfo(
                    description = "Tenli AIoT App v1.0",
                    systemName = "Android",
                    systemVersion = Build.VERSION.RELEASE
                )
            ),
            saveLogin = true,
            createIfNotExist = true,
            createUserOptions = CreateUserOptions(
                group = UserGroupOptions(joinToDeviceSample = false)
            )
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as com.tenli.aiot.TenliApp
                LoginViewModel(
                    application = application,
                    authRepository = application.container.authRepository,
                    dispatcherProvider = application.container.dispatcherProvider
                )
            }
        }
    }
}