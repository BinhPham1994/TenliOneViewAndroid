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
import com.tenli.oneview.BuildConfig
import com.tenli.oneview.TenliApp
import com.tenli.oneview.data.local.EventCacheManager
import com.tenli.oneview.data.local.GlobalData
import com.tenli.oneview.data.local.HomeCacheManager
import com.tenli.oneview.data.local.MonitorCacheManager
import com.tenli.oneview.data.local.UserPermissionManager
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.data.network.ApiProvider
import com.tenli.oneview.data.network.retrofit.LoginAuthClient
import com.tenli.oneview.data.network.websocket.WebSocketManager
import com.tenli.oneview.firebase.FcmTokenManager
import com.tenli.oneview.model.network.ChangePasswordModel
import com.tenli.oneview.model.network.UserNotificationGlobalSetting
import com.tenli.oneview.model.network.UserNotificationSettingsModel
import com.tenli.oneview.ui.component.MobileTopPopupManager
import com.tenli.oneview.ui.utils.AiTypeHelper
import com.tenli.oneview.ui.utils.AppKeys
import com.tenli.oneview.ui.utils.LocaleManager
import com.tenli.oneview.util.BackendHttpException
import com.tenli.oneview.util.EventBus
import com.tenli.oneview.util.NetworkMonitor
import com.tenli.oneview.util.noNetworkMessage
import com.tenli.oneview.util.toUserFriendlyMessage
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
    private var aiModuleSaveJob: Job? = null
    private var passwordChangeJob: Job? = null

    @Volatile
    private var cachedNotificationSettings: UserNotificationSettingsModel? = null

    /** Ngôn ngữ hiện tại (cùng nguồn với rememberIsEnglish()) – dùng cho thông báo popup phát từ ViewModel */
    private val isEnglish: Boolean
        get() = LocaleManager.languageFlow.value.startsWith("en")

    private fun localized(en: String, vi: String): String = if (isEnglish) en else vi

    init {
        UserPermissionManager.syncFromUserSession()
        viewModelScope.launch {
            var wasOffline = false
            networkMonitor.isOnline.collect { isOnline ->
                _uiState.update { it.copy(isOffline = !isOnline) }

                if (!isOnline) {
                    wasOffline = true
                    MobileTopPopupManager.error(noNetworkMessage(isEnglish))
                } else {
                    if (wasOffline) {
                        wasOffline = false
                        EventBus.emitNetworkRestored()
                    }
                    // Reconnect WebSockets & đồng bộ phân quyền + cài đặt + FCM push token khi có mạng trở lại hoặc khởi chạy MainViewModel
                    if (UserSession.accessToken.isNotEmpty()) {
                        val baseUrl = UserSession.domain.trim()
                        if (baseUrl.isNotEmpty()) {
                            webSocketManager.connectNotify(baseUrl)
                            webSocketManager.connectAllReports()
                        }
                        refreshPermissionsAndSyncNotifications()
                    }
                }
            }
        }
    }

    private fun refreshPermissionsAndSyncNotifications() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                UserPermissionManager.fetchAndApplyPermissions()
            }
            if (UserPermissionManager.canAccessEvents()) {
                syncNotificationSettingsAndRegisterToken()
            } else {
                logInfo { "[SYNC CẤU HÌNH] Tài khoản không có quyền xem sự kiện AI -> hủy đăng ký FCM token nếu có" }
                runCatching { FcmTokenManager.unregisterDevicePushToken() }
            }
        }
    }

    private fun currentAccountDebugInfo(): String {
        val user = UserSession.userData
        val token = UserSession.accessToken
        return "userId=${user?.id}, userName=${user?.name ?: UserSession.savedUsername}, domain=${UserSession.domain}, hasToken=${token.isNotEmpty()}"
    }

    private inline fun logInfo(msg: () -> String) {
        if (BuildConfig.DEBUG) Log.i(NOTIFY_LOG_TAG, msg())
    }

    private inline fun logWarn(throwable: Throwable? = null, msg: () -> String) {
        if (BuildConfig.DEBUG) {
            if (throwable != null) Log.w(NOTIFY_LOG_TAG, msg(), throwable) else Log.w(NOTIFY_LOG_TAG, msg())
        }
    }

    private inline fun logError(throwable: Throwable? = null, msg: () -> String) {
        if (BuildConfig.DEBUG) {
            if (throwable != null) Log.e(NOTIFY_LOG_TAG, msg(), throwable) else Log.e(NOTIFY_LOG_TAG, msg())
        }
    }

    private fun syncNotificationSettingsAndRegisterToken() {
        viewModelScope.launch(Dispatchers.IO) {
            var shouldRegisterToken = true
            runCatching {
                logInfo { "[SYNC CẤU HÌNH] GET /ts/api/v1/user/settings/notification_settings | ${currentAccountDebugInfo()}" }
                val resp = ApiProvider.bsApi.getUserNotificationSettings()
                if (resp.isSuccessful) {
                    val body = resp.body()
                    logInfo {
                        val rawJson = runCatching { LoginAuthClient.gson.toJson(body) }.getOrNull()
                        "✅ [SYNC CẤU HÌNH THÀNH CÔNG] HTTP ${resp.code()} | body=$rawJson"
                    }
                    val explicitEnabled = body?.global?.enabled
                    if (body != null && explicitEnabled != null) {
                        val localSnapshot = cachedNotificationSettings ?: loadCachedNotificationSettingsFromPrefs()
                        val mergedOverrides = AiTypeHelper.mergeNotificationOverrides(
                            localOverrides = localSnapshot?.overrides,
                            serverOverrides = body.overrides
                        )
                        val mergedModel = body.copy(overrides = mergedOverrides)
                        saveNotificationSettingsCache(mergedModel)
                        shouldRegisterToken = explicitEnabled
                        savePushEnabledPref(explicitEnabled)
                        _notificationConfigState.update {
                            it.copy(
                                pushEnabled = explicitEnabled,
                                aiModules = buildAiModuleConfigs(mergedModel)
                            )
                        }
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
                        logInfo {
                            "[KHỞI TẠO CẤU HÌNH CHO TÀI KHOẢN MỚI] PUT /ts/api/v1/user/settings/notification_settings | payload=${runCatching { LoginAuthClient.gson.toJson(initModel) }.getOrNull()}"
                        }
                        val putResp = ApiProvider.bsApi.updateUserNotificationSettings(initModel)
                        val effectiveModel = if (putResp.isSuccessful) {
                            logInfo { "✅ [KHỞI TẠO CẤU HÌNH MẶC ĐỊNH THÀNH CÔNG] HTTP ${putResp.code()}" }
                            putResp.body() ?: initModel
                        } else {
                            logWarn {
                                val putErr = runCatching { putResp.errorBody()?.string() }.getOrNull()
                                "⚠️ [KHỞI TẠO CẤU HÌNH MẶC ĐỊNH THẤT BẠI] HTTP ${putResp.code()} | errorBody=$putErr"
                            }
                            initModel
                        }
                        saveNotificationSettingsCache(effectiveModel)
                        shouldRegisterToken = true
                        savePushEnabledPref(true)
                        _notificationConfigState.update {
                            it.copy(
                                pushEnabled = true,
                                aiModules = buildAiModuleConfigs(effectiveModel)
                            )
                        }
                    }
                } else {
                    logError {
                        val errBody = runCatching { resp.errorBody()?.string() }.getOrNull()
                        "❌ [SYNC CẤU HÌNH THẤT BẠI] HTTP ${resp.code()} | errorBody=$errBody | ${currentAccountDebugInfo()}"
                    }
                }
            }.onFailure { e ->
                logError(e) { "❌ [EXCEPTION KHI SYNC CẤU HÌNH] ${e.javaClass.simpleName}: ${e.message} | ${currentAccountDebugInfo()}" }
            }
            if (shouldRegisterToken) {
                FcmTokenManager.registerDevicePushToken(force = false)
            } else {
                logInfo { "[SYNC CẤU HÌNH] Tài khoản đang tắt thông báo trên server (enabled=false) -> không đăng ký FCM token" }
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
                    remove(AiTypeHelper.PREF_USER_NOTIFICATION_SETTINGS_JSON)
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
        if (cachedNotificationSettings == null) {
            cachedNotificationSettings = loadCachedNotificationSettingsFromPrefs()
        }
        val initialModules = buildAiModuleConfigs(cachedNotificationSettings)
        _notificationConfigState.value = NotificationConfigState(
            isLoading = !hasCachedPref && cachedNotificationSettings == null,
            isSaving = false,
            pushEnabled = cachedEnabled,
            aiModules = initialModules
        )

        notificationLoadJob?.cancel()
        notificationLoadJob = viewModelScope.launch {
            try {
                logInfo {
                    "[TẢI CẤU HÌNH] GET /ts/api/v1/user/settings/notification_settings | hasCachedPref=$hasCachedPref, cachedEnabled=$cachedEnabled | ${currentAccountDebugInfo()}"
                }
                val resp = ApiProvider.bsApi.getUserNotificationSettings()
                if (resp.isSuccessful) {
                    val body = resp.body()
                    logInfo {
                        val rawJson = runCatching { LoginAuthClient.gson.toJson(body) }.getOrNull()
                        "✅ [TẢI CẤU HÌNH THÀNH CÔNG] HTTP ${resp.code()} | body=$rawJson"
                    }
                    if (body != null) {
                        val localSnapshot = cachedNotificationSettings ?: loadCachedNotificationSettingsFromPrefs()
                        val mergedOverrides = AiTypeHelper.mergeNotificationOverrides(
                            localOverrides = localSnapshot?.overrides,
                            serverOverrides = body.overrides
                        )
                        val mergedModel = body.copy(overrides = mergedOverrides)
                        saveNotificationSettingsCache(mergedModel)
                        val enabled = mergedModel.global?.enabled ?: true
                        _notificationConfigState.update {
                            it.copy(
                                pushEnabled = enabled,
                                aiModules = buildAiModuleConfigs(mergedModel)
                            )
                        }
                        savePushEnabledPref(enabled)
                    }
                } else {
                    logError {
                        val errBody = runCatching { resp.errorBody()?.string() }.getOrNull()
                        "❌ [TẢI CẤU HÌNH THẤT BẠI] GET /ts/api/v1/user/settings/notification_settings -> HTTP ${resp.code()} | errorBody=$errBody | ${currentAccountDebugInfo()}"
                    }
                    MobileTopPopupManager.error(
                        BackendHttpException(resp.code(), resp.message()).toUserFriendlyMessage()
                    )
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                logError(e) {
                    "❌ [EXCEPTION KHI TẢI CẤU HÌNH] ${e.javaClass.simpleName}: ${e.message} | ${currentAccountDebugInfo()}"
                }
                MobileTopPopupManager.error(e.toUserFriendlyMessage())
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
        notificationLoadJob?.cancel()
        aiModuleSaveJob?.cancel()
        val previousEnabled = _notificationConfigState.value.pushEnabled
        _notificationConfigState.update { it.copy(pushEnabled = enabled, isSaving = true) }
        savePushEnabledPref(enabled)
        viewModelScope.launch {
            val saveFailedMsg = localized("Failed to save notification settings", "Không thể lưu cấu hình thông báo")
            try {
                logInfo {
                    "[LƯU CẤU HÌNH - BẮT ĐẦU] enabled=$enabled, hasOsPermission=$hasOsPermission, hasCachedModel=${cachedNotificationSettings != null} | ${currentAccountDebugInfo()}"
                }
                val baseSettings = cachedNotificationSettings
                    ?: loadCachedNotificationSettingsFromPrefs()
                    ?: fetchServerNotificationSettingsQuietly()

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

                saveNotificationSettingsCache(updatedModel)

                logInfo {
                    val requestJson = runCatching { LoginAuthClient.gson.toJson(updatedModel) }.getOrNull()
                    "[LƯU CẤU HÌNH - GỬI PUT] PUT /ts/api/v1/user/settings/notification_settings | payload=$requestJson | ${currentAccountDebugInfo()}"
                }

                val resp = ApiProvider.bsApi.updateUserNotificationSettings(updatedModel)
                if (resp.isSuccessful) {
                    val respBody = resp.body()
                    logInfo {
                        val respJson = runCatching { LoginAuthClient.gson.toJson(respBody) }.getOrNull()
                        "✅ [LƯU CẤU HÌNH THÀNH CÔNG] PUT /ts/api/v1/user/settings/notification_settings -> HTTP ${resp.code()} | response=$respJson"
                    }
                    // Giữ nguyên updatedModel (không ghi đè bằng resp.body() vì PUT trả về model rỗng overrides)
                    saveNotificationSettingsCache(updatedModel)
                    _notificationConfigState.update {
                        it.copy(aiModules = buildAiModuleConfigs(updatedModel))
                    }
                    runCatching {
                        if (enabled) {
                            FcmTokenManager.registerDevicePushToken(force = true)
                        } else {
                            FcmTokenManager.unregisterDevicePushToken()
                        }
                    }.onFailure { tokenErr ->
                        logWarn(tokenErr) { "⚠️ [LƯU CẤU HÌNH] Lỗi đồng bộ FCM token sau khi PUT thành công: ${tokenErr.message}" }
                    }
                } else {
                    logError {
                        val errBody = runCatching { resp.errorBody()?.string() }.getOrNull()
                        val requestJson = runCatching { LoginAuthClient.gson.toJson(updatedModel) }.getOrNull()
                        "❌ [LƯU CẤU HÌNH THẤT BẠI - HTTP ERROR] PUT /ts/api/v1/user/settings/notification_settings -> HTTP ${resp.code()} (${resp.message()}) | errorBody=$errBody | requestPayload=$requestJson | ${currentAccountDebugInfo()}"
                    }
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
                logError(e) {
                    "❌ [LƯU CẤU HÌNH THẤT BẠI - EXCEPTION] ${e.javaClass.simpleName}: ${e.message} | ${currentAccountDebugInfo()}"
                }
                _notificationConfigState.update { it.copy(pushEnabled = previousEnabled) }
                savePushEnabledPref(previousEnabled)
                MobileTopPopupManager.error(saveFailedMsg)
            } finally {
                _notificationConfigState.update { it.copy(isSaving = false) }
            }
        }
    }

    /**
     * Cập nhật bật/tắt thông báo hoặc chế độ cảnh báo (Thông báo / Cuộc gọi) cho từng bài AI (`overrides.event_types`).
     */
    fun updateAiModuleNotifyConfig(
        moduleKey: String,
        enabled: Boolean,
        alarmMode: AiAlarmMode
    ) {
        val module = AiTypeHelper.findModuleByKey(moduleKey) ?: return
        notificationLoadJob?.cancel()

        val previousModules = _notificationConfigState.value.aiModules
        val previousSettings = cachedNotificationSettings ?: loadCachedNotificationSettingsFromPrefs()

        val baseGlobal = previousSettings?.global
            ?: UserNotificationGlobalSetting(enabled = _notificationConfigState.value.pushEnabled)
        val updatedOverrides = AiTypeHelper.applyModuleOverrideToJson(
            baseOverrides = previousSettings?.overrides,
            module = module,
            enabled = enabled,
            alarmMode = alarmMode.apiValue
        )
        val optimisticModel = if (previousSettings != null) {
            previousSettings.copy(
                global = baseGlobal,
                overrides = updatedOverrides
            )
        } else {
            UserNotificationSettingsModel(
                global = baseGlobal,
                overrides = updatedOverrides
            )
        }

        // Lưu xuống đĩa và cập nhật UI tức thì trên Main thread (tự động sắp xếp lại theo thứ tự ưu tiên: Cuộc gọi -> Tắt -> Thông báo)
        saveNotificationSettingsCache(optimisticModel)
        _notificationConfigState.update { state ->
            state.copy(
                aiModules = buildAiModuleConfigs(optimisticModel)
            )
        }

        aiModuleSaveJob?.cancel()
        aiModuleSaveJob = viewModelScope.launch {
            val saveFailedMsg = localized("Failed to save notification settings", "Không thể lưu cấu hình thông báo")
            try {
                val modelToSave = cachedNotificationSettings ?: optimisticModel
                logInfo {
                    val requestJson = runCatching { LoginAuthClient.gson.toJson(modelToSave) }.getOrNull()
                    "[LƯU CẤU HÌNH BÀI AI] module=${module.key}, enabled=$enabled, alarmMode=${alarmMode.apiValue} | PUT /ts/api/v1/user/settings/notification_settings | payload=$requestJson"
                }

                val resp = ApiProvider.bsApi.updateUserNotificationSettings(modelToSave)
                if (resp.isSuccessful) {
                    logInfo {
                        "✅ [LƯU CẤU HÌNH BÀI AI THÀNH CÔNG] module=${module.key}, enabled=$enabled, alarmMode=${alarmMode.apiValue} -> HTTP ${resp.code()}"
                    }
                    // Lưu ý: Không ghi đè cachedNotificationSettings bằng resp.body() vì API PUT trả về object không chứa overrides.event_types
                } else {
                    logError {
                        val errBody = runCatching { resp.errorBody()?.string() }.getOrNull()
                        "❌ [LƯU CẤU HÌNH BÀI AI THẤT BẠI] module=${module.key} -> HTTP ${resp.code()} | errorBody=$errBody"
                    }
                    saveNotificationSettingsCache(previousSettings)
                    _notificationConfigState.update { it.copy(aiModules = previousModules) }
                    MobileTopPopupManager.error(saveFailedMsg)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                logError(e) {
                    "❌ [EXCEPTION LƯU CẤU HÌNH BÀI AI] module=${module.key} -> ${e.javaClass.simpleName}: ${e.message}"
                }
                saveNotificationSettingsCache(previousSettings)
                _notificationConfigState.update { it.copy(aiModules = previousModules) }
                MobileTopPopupManager.error(saveFailedMsg)
            }
        }
    }

    private suspend fun fetchServerNotificationSettingsQuietly(): UserNotificationSettingsModel? {
        return runCatching {
            logInfo { "[LƯU CẤU HÌNH] cachedNotificationSettings=null -> gọi GET /ts/api/v1/user/settings/notification_settings trước khi PUT" }
            val getResp = ApiProvider.bsApi.getUserNotificationSettings()
            if (getResp.isSuccessful) {
                getResp.body()
            } else {
                null
            }
        }.getOrNull()
    }

    /**
     * Thứ tự ưu tiên hiển thị bài AI:
     * 1. Cuộc gọi (`enabled = true`, `alarmMode = Call`) -> 0
     * 2. Tắt thông báo (`enabled = false`) -> 1
     * 3. Thông báo đẩy (`enabled = true`, `alarmMode = Push`) -> 2
     */
    private fun aiModuleSortPriority(item: AiModuleNotifyConfig): Int = when {
        item.enabled && item.alarmMode == AiAlarmMode.Call -> 0
        !item.enabled -> 1
        else -> 2
    }

    private fun buildAiModuleConfigs(
        settings: UserNotificationSettingsModel?
    ): List<AiModuleNotifyConfig> {
        val overrides = settings?.overrides
        return AiTypeHelper.activeModules.mapIndexed { index, mod ->
            val (enabled, modeStr) = AiTypeHelper.extractModuleNotifyConfig(mod, overrides)
            index to AiModuleNotifyConfig(
                moduleKey = mod.key,
                sensorType = mod.sensorType,
                labelVi = mod.labelVi,
                labelEn = mod.labelEn,
                eventTypes = mod.eventTypes,
                enabled = enabled,
                alarmMode = AiAlarmMode.fromApiValue(modeStr)
            )
        }.sortedWith(
            compareBy<Pair<Int, AiModuleNotifyConfig>> { (_, item) -> aiModuleSortPriority(item) }
                .thenBy { (originalIndex, _) -> originalIndex }
        ).map { (_, item) -> item }
    }

    private fun saveNotificationSettingsCache(model: UserNotificationSettingsModel?) {
        cachedNotificationSettings = model
        runCatching {
            val json = if (model != null) LoginAuthClient.gson.toJson(model) else null
            GlobalData.preferences.edit {
                if (json != null) {
                    putString(AiTypeHelper.PREF_USER_NOTIFICATION_SETTINGS_JSON, json)
                } else {
                    remove(AiTypeHelper.PREF_USER_NOTIFICATION_SETTINGS_JSON)
                }
            }
        }
    }

    private fun loadCachedNotificationSettingsFromPrefs(): UserNotificationSettingsModel? {
        return runCatching {
            val raw = GlobalData.preferences.getString(
                AiTypeHelper.PREF_USER_NOTIFICATION_SETTINGS_JSON,
                null
            ) ?: return null
            LoginAuthClient.gson.fromJson(raw, UserNotificationSettingsModel::class.java)
        }.getOrNull()
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
                    GlobalData.preferences.edit {
                        remove(AppKeys.PASSWORD_KEY)
                    }
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
