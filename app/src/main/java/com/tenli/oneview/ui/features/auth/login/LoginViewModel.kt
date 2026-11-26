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
import com.tenli.oneview.data.repository.AuthRepository
import com.tenli.oneview.model.network.LogInModel
import com.tenli.oneview.model.network.LoginResponseData
import com.tenli.oneview.ui.component.MobileTopPopupManager
import com.tenli.oneview.ui.utils.AppKeys
import com.tenli.oneview.ui.utils.LocaleManager
import com.tenli.oneview.util.DispatcherProvider
import com.tenli.oneview.util.NetworkMonitor
import com.tenli.oneview.util.noNetworkMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

private const val DOMAIN_KEY = "DOMAIN_KEY"

class LoginViewModel(
    application: Application,
    private val authRepository: AuthRepository,
    private val dispatcherProvider: DispatcherProvider
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()
    private val _event = Channel<LoginEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    private var loginJob: Job? = null
    private var needsReloadFromPrefs: Boolean = true

    init {
        loadSavedCredentials()
    }

    fun reloadSavedCredentialsIfNeeded() {
        if (needsReloadFromPrefs) {
            loadSavedCredentials()
        }
    }

    fun loadSavedCredentials() {
        val prefs = GlobalData.preferences
        val isFromQr = prefs.getBoolean(AppKeys.DOMAIN_FROM_QR_KEY, false)
        val savedDomain = stripDomainScheme(prefs.getString(DOMAIN_KEY, "").orEmpty())
        val hasValidSavedDomain = isValidProjectDomain(savedDomain)
        val termsAccepted = prefs.getBoolean(AppKeys.TERMS_ACCEPTED_KEY, false)
        _uiState.update {
            it.copy(
                step = if (hasValidSavedDomain) LoginStep.CREDENTIALS else LoginStep.DOMAIN,
                domain = if (hasValidSavedDomain) savedDomain else "",
                isDomainFromQr = hasValidSavedDomain && isFromQr,
                email = "",
                password = "",
                termsAccepted = termsAccepted,
                showPassword = false
            )
        }
        needsReloadFromPrefs = false
    }

    private fun stripDomainScheme(raw: String): String = normalizeDomainInput(raw)

    fun onDomainChanged(domain: String) {
        val cleaned = if (domain.contains("://")) {
            domain.replace(SCHEME_REGEX, "")
        } else {
            domain
        }
        _uiState.update { it.copy(domain = cleaned, isDomainFromQr = false) }
    }

    fun onQrDomainScanned(scannedDomain: String) {
        _uiState.update {
            it.copy(
                step = LoginStep.CREDENTIALS,
                domain = normalizeDomainInput(scannedDomain),
                isDomainFromQr = true,
                email = "",
                password = "",
                showPassword = false
            )
        }
    }

    fun onProceedToCredentialsStep() {
        _uiState.update {
            it.copy(
                step = LoginStep.CREDENTIALS,
                domain = normalizeDomainInput(it.domain),
                isDomainFromQr = false,
                email = "",
                password = "",
                showPassword = false
            )
        }
    }

    fun onBackToDomainStep() {
        val wasFromQr = _uiState.value.isDomainFromQr
        if (wasFromQr) {
            GlobalData.preferences.edit {
                remove(DOMAIN_KEY)
                remove(AppKeys.DOMAIN_FROM_QR_KEY)
            }
        }
        _uiState.update {
            it.copy(
                step = LoginStep.DOMAIN,
                domain = if (wasFromQr) "" else it.domain,
                isDomainFromQr = false,
                email = "",
                password = "",
                showPassword = false
            )
        }
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

    fun toggleTermsAccepted() {
        _uiState.update { it.copy(termsAccepted = !it.termsAccepted) }
    }

    fun login() {
        // Chặn bấm liên tiếp khi request trước chưa xong
        if (loginJob?.isActive == true) return

        val state = _uiState.value
        val email = state.email.trim()
        val domain = state.domain.trim()
        val password = state.password
        val isDomainFromQr = state.isDomainFromQr
        val isEn = LocaleManager.getLocale(getApplication()).startsWith("en")

        val validationError = when {
            domain.isEmpty() -> if (isEn) "Please enter project domain" else "Vui lòng nhập domain dự án"
            !isValidProjectDomain(domain) -> if (isEn) "Invalid project domain" else "Domain dự án không hợp lệ"
            email.isEmpty() -> if (isEn) "Please enter username" else "Vui lòng nhập tên đăng nhập"
            password.isEmpty() -> if (isEn) "Please enter password" else "Vui lòng nhập mật khẩu"
            !state.termsAccepted -> if (isEn) {
                "Please agree to Terms of Use & Privacy Policy"
            } else {
                "Vui lòng đồng ý với Điều khoản sử dụng & Chính sách Bảo mật"
            }
            else -> null
        }
        if (validationError != null) {
            MobileTopPopupManager.error(validationError)
            return
        }

        if (!NetworkMonitor.isNetworkAvailable()) {
            MobileTopPopupManager.error(noNetworkMessage(isEn))
            return
        }

        loginJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = withContext(dispatcherProvider.io) {
                    authRepository.loginVms(domain, LogInModel(username = email, password = password))
                }
                val loginData = result.getOrNull()
                when {
                    result.isFailure -> MobileTopPopupManager.error(
                        loginErrorMessage(result.exceptionOrNull(), isEn, isUnexpected = false)
                    )
                    loginData == null -> MobileTopPopupManager.error(
                        if (isEn) "Empty response data" else "Dữ liệu phản hồi rỗng"
                    )
                    else -> {
                        handleLoginSuccess(
                            loginData = loginData,
                            domain = domain,
                            username = email,
                            password = password,
                            isDomainFromQr = isDomainFromQr
                        )
                        _event.send(LoginEvent.LoginSuccess)
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
        if (!NetworkMonitor.isNetworkAvailable()) {
            return noNetworkMessage(isEn)
        }
        val errorMsg = error?.message.orEmpty()
        val httpCode = (error as? HttpException)?.code()
            ?: HTTP_CODE_REGEX.find(errorMsg)?.groupValues?.getOrNull(1)?.toIntOrNull()
        val rootCause = error?.cause
        val isConnectionError = error is UnknownHostException ||
            error is SocketTimeoutException ||
            error is ConnectException ||
            error is SSLException ||
            error is IOException ||
            rootCause is UnknownHostException ||
            rootCause is SocketTimeoutException ||
            rootCause is ConnectException ||
            rootCause is SSLException ||
            rootCause is IOException ||
            errorMsg.contains("Unable to resolve host", ignoreCase = true) ||
            errorMsg.contains("timeout", ignoreCase = true) ||
            errorMsg.contains("Failed to connect", ignoreCase = true)

        val connectionMsg = if (isEn) {
            "Connection error to server, please try again later"
        } else {
            "Lỗi kết nối đến máy chủ, vui lòng thử lại sau"
        }
        val invalidCredentialsMsg = if (isEn) {
            "Invalid login credentials"
        } else {
            "Thông tin đăng nhập không chính xác"
        }
        val genericErrorMsg = if (isEn) {
            "An error occurred, please try again later."
        } else {
            "Có lỗi xảy ra, vui lòng thử lại sau."
        }

        return when {
            !isUnexpected && (httpCode == 400 || httpCode == 401 || httpCode == 403 ||
                errorMsg.contains("Failed to authenticate", ignoreCase = true)) ->
                invalidCredentialsMsg
            httpCode == 404 || (httpCode != null && httpCode >= 500) ->
                connectionMsg
            isConnectionError ->
                connectionMsg
            else ->
                genericErrorMsg
        }
    }

    private suspend fun handleLoginSuccess(
        loginData: LoginResponseData,
        domain: String,
        username: String,
        password: String,
        isDomainFromQr: Boolean
    ) {
        val cleanHost = stripDomainScheme(domain)
        val formattedDomain = loginData.resolvedDomain.ifEmpty {
            if (!domain.startsWith("http://", ignoreCase = true) &&
                !domain.startsWith("https://", ignoreCase = true)
            ) {
                "https://$cleanHost"
            } else {
                domain.trim().trimEnd('/')
            }
        }
        // Ủy quyền toàn bộ việc xoá cache phiên cũ, thiết lập UserSession, đồng bộ quyền và ghi SharedPreferences cho AuthRepository
        withContext(dispatcherProvider.io) {
            authRepository.establishAuthenticatedSession(
                loginData = loginData,
                formattedDomain = formattedDomain,
                username = username,
                password = password,
                isDomainFromQr = isDomainFromQr
            )
        }
        // Chuẩn bị sẵn trạng thái sạch cho lần quay lại màn hình Login tiếp theo (sau khi đăng xuất):
        // - Mở thẳng form đăng nhập (giữ domain & cờ QR để đăng nhập lại nhanh; bấm Back nếu muốn đổi domain)
        // - Luôn xoá trống tài khoản & mật khẩu để nhập mới
        _uiState.update {
            it.copy(
                step = LoginStep.CREDENTIALS,
                domain = cleanHost,
                isDomainFromQr = isDomainFromQr,
                email = "",
                password = "",
                termsAccepted = true,
                showPassword = false
            )
        }
        needsReloadFromPrefs = true
    }

    companion object {
        private val SCHEME_REGEX = Regex("^https?://", RegexOption.IGNORE_CASE)
        private val MOBILE_PATH_SUFFIX_REGEX = Regex("/m/?$", RegexOption.IGNORE_CASE)
        private val HTTP_CODE_REGEX = Regex("failed with code (\\d{3})")
        private val DOMAIN_LABEL_REGEX = Regex("^[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?$")
        private val DOMAIN_TLD_REGEX = Regex("^[a-zA-Z]{2,63}$")

        fun normalizeDomainInput(raw: String): String =
            raw.trim()
                .replace(SCHEME_REGEX, "")
                .replace(MOBILE_PATH_SUFFIX_REGEX, "")
                .trimEnd('/')

        /**
         * Kiểm tra định dạng domain dự án hợp lệ:
         * - Chấp nhận tên miền chuẩn (vd: `app.tenli.ai`, `vms.tenli.vn:8080`)
         * - Hoặc địa chỉ IPv4 kèm/không kèm port (vd: `192.168.1.10`, `192.168.1.10:8000`)
         */
        fun isValidProjectDomain(raw: String): Boolean {
            val cleaned = normalizeDomainInput(raw)
            if (cleaned.isEmpty() || cleaned.any { it.isWhitespace() } || cleaned.contains('/')) {
                return false
            }

            val colonCount = cleaned.count { it == ':' }
            if (colonCount > 1) return false

            val host = if (colonCount == 1) {
                val parts = cleaned.split(':')
                val port = parts[1].toIntOrNull() ?: return false
                if (port !in 1..65535) return false
                parts[0]
            } else {
                cleaned
            }

            if (host.isEmpty() || host.length > 253) return false

            val labels = host.split('.')
            if (labels.size < 2) return false

            // Nếu tất cả các phần đều là chữ số -> bắt buộc là IPv4 hợp lệ (4 octet từ 0..255)
            if (labels.all { it.isNotEmpty() && it.all(Char::isDigit) }) {
                if (labels.size != 4) return false
                return labels.all { octet ->
                    (octet.length == 1 || !octet.startsWith('0')) &&
                        (octet.toIntOrNull() ?: -1) in 0..255
                }
            }

            // Tên miền chuẩn: mỗi label hợp lệ và TLD cuối phải là chữ cái (>= 2 ký tự)
            return labels.all { DOMAIN_LABEL_REGEX.matches(it) } &&
                DOMAIN_TLD_REGEX.matches(labels.last())
        }

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
