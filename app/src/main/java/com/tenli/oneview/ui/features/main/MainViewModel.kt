package com.tenli.oneview.ui.features.main

import android.app.Application
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tenli.oneview.TenliApp
import com.tenli.oneview.data.local.EventCacheManager
import com.tenli.oneview.data.local.GlobalData
import com.tenli.oneview.data.local.HomeCacheManager
import com.tenli.oneview.data.local.MonitorCacheManager
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.data.network.ApiProvider
import com.tenli.oneview.data.network.retrofit.LoginAuthClient
import com.tenli.oneview.data.network.websocket.WebSocketManager
import com.tenli.oneview.firebase.FcmTokenManager
import com.tenli.oneview.model.network.ChangePasswordModel
import com.tenli.oneview.model.network.UserNotificationGlobalSetting
import com.tenli.oneview.model.network.UserNotificationSettingsModel
import com.tenli.oneview.ui.component.MobileTopPopupManager
import com.tenli.oneview.ui.utils.AppKeys
import com.tenli.oneview.ui.utils.LocaleManager
import com.tenli.oneview.util.NetworkMonitor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(
    application: Application,
    private val networkMonitor: NetworkMonitor,
    private val webSocketManager: WebSocketManager
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<MainEvent>()
    val event = _event.asSharedFlow()

    private val _notificationConfigState = MutableStateFlow(NotificationConfigState())
    val notificationConfigState = _notificationConfigState.asStateFlow()

    private val _passwordChangeState = MutableStateFlow(PasswordChangeState())
    val passwordChangeState = _passwordChangeState.asStateFlow()

    private var logoutJob: Job? = null
    private var notificationLoadJob: Job? = null
    private var passwordChangeJob: Job? = null

    @Volatile
    private var cachedNotificationSettings: UserNotificationSettingsModel? = null

    /** Ngôn ngữ hiện tại (cùng nguồn với rememberIsEnglish()) – dùng cho thông báo popup phát từ ViewModel */
    private val isEnglish: Boolean
        get() = LocaleManager.languageFlow.value.startsWith("en")

    private fun localized(en: String, vi: String): String = if (isEnglish) en else vi

    init {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                _uiState.update { it.copy(isOffline = !isOnline) }

                // Reconnect WebSockets & đồng bộ cài đặt + FCM push token khi có mạng trở lại hoặc khởi chạy MainViewModel
                if (isOnline && UserSession.accessToken.isNotEmpty()) {
                    val baseUrl = UserSession.domain.trim()
                    if (baseUrl.isNotEmpty()) {
                        webSocketManager.connectNotify(baseUrl)
                        webSocketManager.connectAllReports()
                    }
                    syncNotificationSettingsAndRegisterToken()
                }
            }
        }
    }

    private fun currentAccountDebugInfo(): String {
        val user = UserSession.userData
        val token = UserSession.accessToken
        return "userId=${user?.id}, userName=${user?.name ?: UserSession.savedUsername}, domain=${UserSession.domain}, hasToken=${token.isNotEmpty()}, tokenPrefix=${token.take(16)}..."
    }

    private fun syncNotificationSettingsAndRegisterToken() {
        viewModelScope.launch(Dispatchers.IO) {
            var shouldRegisterToken = true
            runCatching {
                Log.i(NOTIFY_LOG_TAG, "[SYNC CẤU HÌNH] GET /ts/api/v1/user/settings/notification_settings | ${currentAccountDebugInfo()}")
                val resp = ApiProvider.bsApi.getUserNotificationSettings()
                if (resp.isSuccessful) {
                    val body = resp.body()
                    val rawJson = runCatching { LoginAuthClient.gson.toJson(body) }.getOrNull()
                    Log.i(NOTIFY_LOG_TAG, "✅ [SYNC CẤU HÌNH THÀNH CÔNG] HTTP ${resp.code()} | body=$rawJson")
                    val explicitEnabled = body?.global?.enabled
                    if (body != null && explicitEnabled != null) {
                        cachedNotificationSettings = body
                        shouldRegisterToken = explicitEnabled
                        savePushEnabledPref(explicitEnabled)
                        _notificationConfigState.update { it.copy(pushEnabled = explicitEnabled) }
                    } else {
                        // Tài khoản mới chưa có cấu hình global.enabled trên server -> chủ động khởi tạo mặc định (enabled = true)
                        val initModel = UserNotificationSettingsModel(
                            global = UserNotificationGlobalSetting(
                                enabled = true,
                                pauseUntil = body?.global?.pauseUntil
                            ),
                            overrides = body?.overrides ?: UserNotificationSettingsModel.defaultNotificationOverridesJson(),
                            globalScripts = body?.globalScripts
                        )
                        Log.i(
                            NOTIFY_LOG_TAG,
                            "[KHỞI TẠO CẤU HÌNH CHO TÀI KHOẢN MỚI] PUT /ts/api/v1/user/settings/notification_settings | payload=${runCatching { LoginAuthClient.gson.toJson(initModel) }.getOrNull()}"
                        )
                        val putResp = ApiProvider.bsApi.updateUserNotificationSettings(initModel)
                        if (putResp.isSuccessful) {
                            cachedNotificationSettings = putResp.body() ?: initModel
                            Log.i(NOTIFY_LOG_TAG, "✅ [KHỞI TẠO CẤU HÌNH MẶC ĐỊNH THÀNH CÔNG] HTTP ${putResp.code()}")
                        } else {
                            cachedNotificationSettings = initModel
                            val putErr = runCatching { putResp.errorBody()?.string() }.getOrNull()
                            Log.w(NOTIFY_LOG_TAG, "⚠️ [KHỞI TẠO CẤU HÌNH MẶC ĐỊNH THẤT BẠI] HTTP ${putResp.code()} | errorBody=$putErr")
                        }
                        shouldRegisterToken = true
                        savePushEnabledPref(true)
                        _notificationConfigState.update { it.copy(pushEnabled = true) }
                    }
                } else {
                    val errBody = runCatching { resp.errorBody()?.string() }.getOrNull()
                    Log.e(NOTIFY_LOG_TAG, "❌ [SYNC CẤU HÌNH THẤT BẠI] HTTP ${resp.code()} | errorBody=$errBody | ${currentAccountDebugInfo()}")
                }
            }.onFailure { e ->
                Log.e(NOTIFY_LOG_TAG, "❌ [EXCEPTION KHI SYNC CẤU HÌNH] ${e.javaClass.simpleName}: ${e.message} | ${currentAccountDebugInfo()}", e)
            }
            if (shouldRegisterToken) {
                FcmTokenManager.registerDevicePushToken(force = false)
            } else {
                Log.i(NOTIFY_LOG_TAG, "[SYNC CẤU HÌNH] Tài khoản đang tắt thông báo trên server (enabled=false) -> không đăng ký FCM token")
            }
        }
    }

    fun onTabSelected(tab: MainTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun logout() {
        if (logoutJob?.isActive == true) return
        logoutJob = viewModelScope.launch {
            // Huỷ đăng ký FCM token trên server trước khi xoá UserSession.accessToken (giống tenli-oneview auth.ts)
            FcmTokenManager.unregisterDevicePushToken()
            UserSession.clear()
            cachedNotificationSettings = null

            // Xóa token/prefs + cache cục bộ (đọc/ghi file) trên IO để không chặn Main thread
            withContext(Dispatchers.IO) {
                val prefs = GlobalData.preferences
                val rememberMe = prefs.getBoolean(AppKeys.REMEMBER_ME_KEY, false)
                prefs.edit {
                    remove(AppKeys.ACCESS_TOKEN_KEY)
                    remove(AppKeys.REFRESH_TOKEN_KEY)
                    remove(AppKeys.USER_DATA_KEY)
                    remove(AppKeys.GROUP_ID)
                    remove(AppKeys.GROUP_ID_CAMERA)
                    remove(PREF_PUSH_NOTIFICATIONS_ENABLED)
                    if (!rememberMe) {
                        remove(AppKeys.USERNAME_KEY)
                        remove(AppKeys.PASSWORD_KEY)
                    }
                }

                val app = getApplication<Application>()
                HomeCacheManager.clearCache(app)
                EventCacheManager.clearCache(app)
                MonitorCacheManager.clearCache(app)
            }

            webSocketManager.disconnectAll()

            _event.emit(MainEvent.Logout)
        }
    }

    // ---------------------------------------------------------------------------------
    // Cấu hình thông báo
    // ---------------------------------------------------------------------------------

    /** Gọi khi mở màn hình Cấu hình thông báo: hiển thị giá trị cache ngay, rồi đồng bộ với server. */
    fun loadNotificationConfig() {
        val prefs = GlobalData.preferences
        val hasCachedPref = prefs.contains(PREF_PUSH_NOTIFICATIONS_ENABLED)
        val cachedEnabled = prefs.getBoolean(PREF_PUSH_NOTIFICATIONS_ENABLED, true)
        _notificationConfigState.value = NotificationConfigState(
            isLoading = !hasCachedPref,
            isSaving = false,
            pushEnabled = cachedEnabled
        )

        notificationLoadJob?.cancel()
        notificationLoadJob = viewModelScope.launch {
            try {
                Log.i(
                    NOTIFY_LOG_TAG,
                    "[TẢI CẤU HÌNH] GET /ts/api/v1/user/settings/notification_settings | hasCachedPref=$hasCachedPref, cachedEnabled=$cachedEnabled | ${currentAccountDebugInfo()}"
                )
                val resp = ApiProvider.bsApi.getUserNotificationSettings()
                if (resp.isSuccessful) {
                    val body = resp.body()
                    val rawJson = runCatching { LoginAuthClient.gson.toJson(body) }.getOrNull()
                    Log.i(NOTIFY_LOG_TAG, "✅ [TẢI CẤU HÌNH THÀNH CÔNG] HTTP ${resp.code()} | body=$rawJson")
                    if (body != null) {
                        cachedNotificationSettings = body
                        val enabled = body.global?.enabled ?: true
                        _notificationConfigState.update { it.copy(pushEnabled = enabled) }
                        savePushEnabledPref(enabled)
                    }
                } else {
                    val errBody = runCatching { resp.errorBody()?.string() }.getOrNull()
                    Log.e(
                        NOTIFY_LOG_TAG,
                        "❌ [TẢI CẤU HÌNH THẤT BẠI] GET /ts/api/v1/user/settings/notification_settings -> HTTP ${resp.code()} | errorBody=$errBody | ${currentAccountDebugInfo()}"
                    )
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(
                    NOTIFY_LOG_TAG,
                    "❌ [EXCEPTION KHI TẢI CẤU HÌNH] ${e.javaClass.simpleName}: ${e.message} | ${currentAccountDebugInfo()}",
                    e
                )
            } finally {
                _notificationConfigState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Bật/tắt "Cho phép nhận thông báo".
     * Giữ nguyên `overrides` (`event_type_groups`, `event_types`), `pause_until` và `global_scripts`
     * đã cấu hình trên server để không làm mất cấu hình nhận thông báo của từng loại sự kiện.
     * @param hasOsPermission Thiết bị đã cấp quyền POST_NOTIFICATIONS hay chưa (để chọn nội dung popup).
     */
    fun setPushNotificationsEnabled(enabled: Boolean, hasOsPermission: Boolean) {
        val previousEnabled = _notificationConfigState.value.pushEnabled
        _notificationConfigState.update { it.copy(pushEnabled = enabled, isSaving = true) }
        savePushEnabledPref(enabled)
        viewModelScope.launch {
            val saveFailedMsg = localized("Failed to save notification settings", "Không thể lưu cấu hình thông báo")
            try {
                Log.i(
                    NOTIFY_LOG_TAG,
                    "[LƯU CẤU HÌNH - BẮT ĐẦU] enabled=$enabled, hasOsPermission=$hasOsPermission, hasCachedModel=${cachedNotificationSettings != null} | ${currentAccountDebugInfo()}"
                )
                val baseSettings = cachedNotificationSettings ?: runCatching {
                    Log.i(NOTIFY_LOG_TAG, "[LƯU CẤU HÌNH] cachedNotificationSettings=null -> gọi GET /ts/api/v1/user/settings/notification_settings trước khi PUT")
                    val getResp = ApiProvider.bsApi.getUserNotificationSettings()
                    if (getResp.isSuccessful) {
                        val getBody = getResp.body()
                        Log.i(
                            NOTIFY_LOG_TAG,
                            "[LƯU CẤU HÌNH] GET trước PUT thành công -> HTTP ${getResp.code()} | body=${runCatching { LoginAuthClient.gson.toJson(getBody) }.getOrNull()}"
                        )
                        getBody
                    } else {
                        val getErrBody = runCatching { getResp.errorBody()?.string() }.getOrNull()
                        Log.w(
                            NOTIFY_LOG_TAG,
                            "⚠️ [LƯU CẤU HÌNH] GET trước PUT thất bại -> HTTP ${getResp.code()} | errorBody=$getErrBody"
                        )
                        null
                    }
                }.onFailure { getErr ->
                    Log.w(NOTIFY_LOG_TAG, "⚠️ [LƯU CẤU HÌNH] Exception khi GET trước PUT: ${getErr.javaClass.simpleName}: ${getErr.message}", getErr)
                }.getOrNull()

                val baseGlobal = baseSettings?.global ?: UserNotificationGlobalSetting()
                val baseOverrides = baseSettings?.overrides ?: UserNotificationSettingsModel.defaultNotificationOverridesJson()

                val updatedModel = if (baseSettings != null) {
                    baseSettings.copy(
                        global = baseGlobal.copy(enabled = enabled),
                        overrides = baseOverrides
                    )
                } else {
                    UserNotificationSettingsModel(
                        global = UserNotificationGlobalSetting(enabled = enabled),
                        overrides = baseOverrides
                    )
                }

                val requestJson = runCatching { LoginAuthClient.gson.toJson(updatedModel) }.getOrNull()
                Log.i(
                    NOTIFY_LOG_TAG,
                    "[LƯU CẤU HÌNH - GỬI PUT] PUT /ts/api/v1/user/settings/notification_settings | payload=$requestJson | ${currentAccountDebugInfo()}"
                )

                val resp = ApiProvider.bsApi.updateUserNotificationSettings(updatedModel)
                if (resp.isSuccessful) {
                    val respBody = resp.body()
                    val respJson = runCatching { LoginAuthClient.gson.toJson(respBody) }.getOrNull()
                    Log.i(
                        NOTIFY_LOG_TAG,
                        "✅ [LƯU CẤU HÌNH THÀNH CÔNG] PUT /ts/api/v1/user/settings/notification_settings -> HTTP ${resp.code()} | response=$respJson"
                    )
                    cachedNotificationSettings = respBody ?: updatedModel
                    runCatching {
                        if (enabled) {
                            FcmTokenManager.registerDevicePushToken(force = true)
                        } else {
                            FcmTokenManager.unregisterDevicePushToken()
                        }
                    }.onFailure { tokenErr ->
                        Log.w(NOTIFY_LOG_TAG, "⚠️ [LƯU CẤU HÌNH] Lỗi đồng bộ FCM token sau khi PUT thành công: ${tokenErr.message}", tokenErr)
                    }
                } else {
                    val errBody = runCatching { resp.errorBody()?.string() }.getOrNull()
                    Log.e(
                        NOTIFY_LOG_TAG,
                        "❌ [LƯU CẤU HÌNH THẤT BẠI - HTTP ERROR] PUT /ts/api/v1/user/settings/notification_settings -> HTTP ${resp.code()} (${resp.message()}) | errorBody=$errBody | requestPayload=$requestJson | ${currentAccountDebugInfo()}"
                    )
                    _notificationConfigState.update { it.copy(pushEnabled = previousEnabled) }
                    savePushEnabledPref(previousEnabled)
                }
                when {
                    !resp.isSuccessful -> MobileTopPopupManager.error(saveFailedMsg)
                    !enabled -> MobileTopPopupManager.info(
                        localized("Notifications disabled.", "Đã tắt nhận thông báo.")
                    )
                    hasOsPermission -> MobileTopPopupManager.success(
                        localized("Notifications enabled successfully!", "Đã bật thông báo thành công!")
                    )
                    else -> MobileTopPopupManager.warning(
                        localized(
                            "Notification permission is not granted on this device.",
                            "Bạn chưa cấp quyền thông báo trên thiết bị."
                        )
                    )
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(
                    NOTIFY_LOG_TAG,
                    "❌ [LƯU CẤU HÌNH THẤT BẠI - EXCEPTION] ${e.javaClass.simpleName}: ${e.message} | ${currentAccountDebugInfo()}",
                    e
                )
                _notificationConfigState.update { it.copy(pushEnabled = previousEnabled) }
                savePushEnabledPref(previousEnabled)
                MobileTopPopupManager.error(saveFailedMsg)
            } finally {
                _notificationConfigState.update { it.copy(isSaving = false) }
            }
        }
    }

    private fun savePushEnabledPref(enabled: Boolean) {
        // apply() ghi đĩa bất đồng bộ – an toàn trên Main thread
        GlobalData.preferences.edit { putBoolean(PREF_PUSH_NOTIFICATIONS_ENABLED, enabled) }
    }

    // ---------------------------------------------------------------------------------
    // Đổi mật khẩu
    // ---------------------------------------------------------------------------------

    /** Reset trạng thái khi mở/đóng sheet đổi mật khẩu. */
    fun resetPasswordChange() {
        passwordChangeJob?.cancel()
        _passwordChangeState.value = PasswordChangeState()
    }

    fun clearPasswordChangeError() {
        if (_passwordChangeState.value.error != null) {
            _passwordChangeState.update { it.copy(error = null) }
        }
    }

    fun changePassword(oldPassword: String, newPassword: String, confirmPassword: String) {
        if (_passwordChangeState.value.isSubmitting) return

        val validationError = when {
            oldPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank() -> PasswordChangeError.EmptyFields
            newPassword.length < MIN_PASSWORD_LENGTH -> PasswordChangeError.TooShort
            newPassword != confirmPassword -> PasswordChangeError.Mismatch
            else -> null
        }
        if (validationError != null) {
            _passwordChangeState.update { it.copy(error = validationError) }
            return
        }
        val userId = UserSession.userData?.id
        if (userId == null) {
            _passwordChangeState.update { it.copy(error = PasswordChangeError.UserNotFound) }
            return
        }

        _passwordChangeState.update { it.copy(isSubmitting = true, error = null) }
        passwordChangeJob = viewModelScope.launch {
            try {
                val resp = ApiProvider.vmsApi.changePassword(
                    ChangePasswordModel(
                        userId = userId,
                        oldPassword = oldPassword,
                        newPassword = newPassword
                    )
                )
                if (resp.isSuccessful) {
                    FcmTokenManager.unregisterDevicePushToken()
                    MobileTopPopupManager.success(
                        localized("Password changed successfully!", "Đổi mật khẩu thành công!")
                    )
                    _passwordChangeState.update { it.copy(isSubmitting = false, isSuccess = true) }
                } else {
                    _passwordChangeState.update {
                        it.copy(isSubmitting = false, error = PasswordChangeError.WrongCurrentPassword(resp.code()))
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _passwordChangeState.update {
                    it.copy(isSubmitting = false, error = PasswordChangeError.Unexpected(e.message))
                }
            }
        }
    }

    companion object {
        private const val NOTIFY_LOG_TAG = "NOTIFY_CFG"
        private const val PREF_PUSH_NOTIFICATIONS_ENABLED = "push_notifications_enabled"
        private const val MIN_PASSWORD_LENGTH = 6

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as TenliApp
                MainViewModel(
                    application,
                    application.container.networkMonitor,
                    application.container.webSocketManager
                )
            }
        }
    }
}
