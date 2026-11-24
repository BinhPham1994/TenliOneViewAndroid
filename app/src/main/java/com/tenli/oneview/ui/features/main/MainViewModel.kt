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
import com.tenli.oneview.data.local.GlobalData
import com.tenli.oneview.data.local.UserPermissionManager
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.data.network.retrofit.LoginAuthClient
import com.tenli.oneview.data.network.websocket.WebSocketManager
import com.tenli.oneview.data.repository.AuthRepository
import com.tenli.oneview.firebase.FcmTokenManager
import com.tenli.oneview.model.network.ChangePasswordModel
import com.tenli.oneview.model.network.UserNotificationGlobalSetting
import com.tenli.oneview.model.network.UserNotificationSettingsModel
import com.tenli.oneview.ui.component.MobileTopPopupManager
import com.tenli.oneview.ui.utils.AiTypeHelper
import com.tenli.oneview.ui.utils.AppKeys
import com.tenli.oneview.ui.utils.LocaleManager
import com.tenli.oneview.ui.utils.PermissionUtils
import com.tenli.oneview.util.BackendHttpException
import com.tenli.oneview.util.EventBus
import com.tenli.oneview.util.NetworkMonitor
import com.tenli.oneview.util.noNetworkMessage
import com.tenli.oneview.util.toUserFriendlyMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

class MainViewModel(
    application: Application,
    private val authRepository: AuthRepository,
    private val networkMonitor: NetworkMonitor,
    private val webSocketManager: WebSocketManager
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(
        run {
            UserPermissionManager.syncFromUserSession()
            val perm = UserPermissionManager.permissionState.value
            MainUiState(
                accountName = resolveAccountName(),
                canAccessEvents = perm.canAccessEvents,
                canAccessLiveStream = perm.canAccessLiveStream
            )
        }
    )
    val uiState = _uiState.asStateFlow()

    private val _event = Channel<MainEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    private val _notificationConfigState = MutableStateFlow(NotificationConfigState())
    val notificationConfigState = _notificationConfigState.asStateFlow()

    private val _passwordChangeState = MutableStateFlow(PasswordChangeState())
    val passwordChangeState = _passwordChangeState.asStateFlow()

    private val isLoggingOut = AtomicBoolean(false)
    private var permissionSyncJob: Job? = null
    private var notificationLoadJob: Job? = null
    private var pushToggleJob: Job? = null
    private var aiModuleSaveJob: Job? = null
    private var passwordChangeJob: Job? = null

    @Volatile
    private var isAppInForeground: Boolean = true

    private val pendingNetworkRefresh = AtomicBoolean(false)
    private val pendingNetworkRestoredEvent = AtomicBoolean(false)
    private val pendingActiveNetworkReconnect = AtomicBoolean(false)

    @Volatile
    private var cachedNotificationSettings: UserNotificationSettingsModel? = null

    @Volatile
    private var lastServerConfirmedSettings: UserNotificationSettingsModel? = null

    @Volatile
    private var hasResolvedNotificationSettingsOnce: Boolean = false

    private fun syncCallPermissionRequirement(
        pushEnabled: Boolean,
        aiModules: List<AiModuleNotifyConfig>
    ) {
        hasResolvedNotificationSettingsOnce = true
        val required = UserSession.accessToken.isNotEmpty() &&
            UserPermissionManager.canAccessEvents() &&
            pushEnabled &&
            aiModules.any { it.enabled && it.alarmMode == AiAlarmMode.Call }
        PermissionUtils.updateCallPermissionRequirement(required)
    }

    private suspend fun syncCallPermissionRequirementFromCacheFallback() {
        if (UserSession.accessToken.isEmpty() || !UserPermissionManager.canAccessEvents()) {
            hasResolvedNotificationSettingsOnce = true
            PermissionUtils.updateCallPermissionRequirement(false)
            return
        }
        val snapshot = cachedNotificationSettings
            ?: loadCachedNotificationSettingsFromPrefs()?.also { cached ->
                if (cachedNotificationSettings == null && UserSession.accessToken.isNotEmpty()) {
                    cachedNotificationSettings = cached
                }
            }
        if (snapshot == null) {
            PermissionUtils.updateCallPermissionRequirement(false)
            return
        }
        val pushEnabled = GlobalData.preferences.getBoolean(
            PREF_PUSH_NOTIFICATIONS_ENABLED,
            snapshot.global?.enabled ?: true
        )
        syncCallPermissionRequirement(pushEnabled, buildAiModuleConfigs(snapshot))
    }

    /** Ngôn ngữ hiện tại (cùng nguồn với rememberIsEnglish()) – dùng cho thông báo popup phát từ ViewModel */
    private val isEnglish: Boolean
        get() = LocaleManager.languageFlow.value.startsWith("en")

    private fun localized(en: String, vi: String): String = if (isEnglish) en else vi

    private fun resolveAccountName(): String =
        UserSession.userData?.name?.takeIf { it.isNotBlank() }
            ?: UserSession.savedUsername.takeIf { it.isNotBlank() }
            ?: ""

    fun setAppForeground(inForeground: Boolean) {
        isAppInForeground = inForeground
        if (inForeground) {
            flushPendingNetworkRefreshIfNeeded()
        }
    }

    private fun flushPendingNetworkRefreshIfNeeded() {
        val shouldRefresh = pendingNetworkRefresh.getAndSet(false)
        val shouldEmitRestored = pendingNetworkRestoredEvent.getAndSet(false)
        val shouldReconnectActiveNetwork = pendingActiveNetworkReconnect.getAndSet(false)
        if (!shouldRefresh && !shouldEmitRestored && !shouldReconnectActiveNetwork) return
        if (!NetworkMonitor.isNetworkAvailable()) return

        if (shouldEmitRestored) {
            EventBus.emitNetworkRestored()
        }
        if (UserSession.accessToken.isNotEmpty()) {
            val baseUrl = UserSession.domain.trim()
            if (baseUrl.isNotEmpty()) {
                if (shouldReconnectActiveNetwork) {
                    webSocketManager.onActiveNetworkChanged()
                } else {
                    webSocketManager.connectNotify(baseUrl)
                    webSocketManager.connectAllReports()
                }
            }
            if (shouldRefresh) {
                refreshPermissionsAndSyncNotifications()
            }
        }
    }

    init {
        viewModelScope.launch {
            var lastPermVersion = UserPermissionManager.permissionState.value.version
            UserPermissionManager.permissionState.collect { permState ->
                if (UserSession.accessToken.isEmpty()) return@collect
                _uiState.update {
                    it.copy(
                        accountName = resolveAccountName(),
                        canAccessEvents = permState.canAccessEvents,
                        canAccessLiveStream = permState.canAccessLiveStream
                    )
                }
                if (permState.version != lastPermVersion) {
                    lastPermVersion = permState.version
                    val snapshot = cachedNotificationSettings
                        ?: loadCachedNotificationSettingsFromPrefs()?.also { cached ->
                            if (cachedNotificationSettings == null && UserSession.accessToken.isNotEmpty()) {
                                cachedNotificationSettings = cached
                            }
                        }
                    val rebuiltModules = buildAiModuleConfigs(snapshot)
                    _notificationConfigState.update { state ->
                        state.copy(aiModules = rebuiltModules)
                    }
                    if (!permState.canAccessEvents) {
                        PermissionUtils.updateCallPermissionRequirement(false)
                    } else if (hasResolvedNotificationSettingsOnce) {
                        syncCallPermissionRequirement(
                            pushEnabled = _notificationConfigState.value.pushEnabled,
                            aiModules = rebuiltModules
                        )
                    }
                }
            }
        }
        viewModelScope.launch {
            var wasOffline = false
            networkMonitor.isOnline.collect { isOnline ->
                _uiState.update { it.copy(isOffline = !isOnline) }
                webSocketManager.onConnectivityChanged(isOnline)

                if (!isOnline) {
                    wasOffline = true
                    pendingNetworkRefresh.set(false)
                    pendingNetworkRestoredEvent.set(false)
                    pendingActiveNetworkReconnect.set(false)
                    if (!hasResolvedNotificationSettingsOnce && UserSession.accessToken.isNotEmpty()) {
                        syncCallPermissionRequirementFromCacheFallback()
                    }
                    if (isAppInForeground) {
                        MobileTopPopupManager.error(noNetworkMessage(isEnglish))
                    }
                } else {
                    val restoredFromOffline = wasOffline
                    wasOffline = false
                    if (isAppInForeground) {
                        if (restoredFromOffline) {
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
                    } else {
                        // App đang ở nền -> hoãn refresh & sự kiện khôi phục mạng cho tới khi quay lại foreground
                        if (restoredFromOffline) {
                            pendingNetworkRestoredEvent.set(true)
                        }
                        if (UserSession.accessToken.isNotEmpty()) {
                            pendingNetworkRefresh.set(true)
                        }
                    }
                }
            }
        }
        viewModelScope.launch {
            networkMonitor.activeNetworkChanged.collect {
                if (UserSession.accessToken.isNotEmpty() && UserSession.domain.isNotBlank()) {
                    if (isAppInForeground) {
                        webSocketManager.onActiveNetworkChanged()
                    } else {
                        pendingNetworkRefresh.set(true)
                        pendingActiveNetworkReconnect.set(true)
                    }
                }
            }
        }
    }

    private fun refreshPermissionsAndSyncNotifications() {
        permissionSyncJob?.cancel()
        permissionSyncJob = viewModelScope.launch(Dispatchers.IO) {
            if (UserSession.accessToken.isEmpty()) return@launch
            runCatching {
                UserPermissionManager.fetchAndApplyPermissions()
            }.onFailure { e ->
                if (e is CancellationException) throw e
            }
            if (UserSession.accessToken.isEmpty()) return@launch
            if (UserPermissionManager.canAccessEvents()) {
                syncNotificationSettingsAndRegisterToken()
            } else {
                hasResolvedNotificationSettingsOnce = true
                PermissionUtils.updateCallPermissionRequirement(false)
                logInfo { "[SYNC CẤU HÌNH] Tài khoản không có quyền xem sự kiện AI -> hủy đăng ký FCM token nếu có" }
                runCatching { FcmTokenManager.unregisterDevicePushToken() }.onFailure { e ->
                    if (e is CancellationException) throw e
                }
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

    private suspend fun syncNotificationSettingsAndRegisterToken() {
        if (UserSession.accessToken.isEmpty()) return
        var shouldRegisterToken = true
        runCatching {
            logInfo { "[SYNC CẤU HÌNH] GET /ts/api/v1/user/settings/notification_settings | ${currentAccountDebugInfo()}" }
            val resp = authRepository.getUserNotificationSettings()
            if (UserSession.accessToken.isEmpty()) return
            if (resp.isSuccessful) {
                val body = resp.body()
                logInfo {
                    val rawJson = runCatching { LoginAuthClient.gson.toJson(body) }.getOrNull()
                    "✅ [SYNC CẤU HÌNH THÀNH CÔNG] HTTP ${resp.code()} | body=$rawJson"
                }
                if (body == null) {
                    logWarn {
                        "⚠️ [SYNC CẤU HÌNH] HTTP ${resp.code()} nhưng body=null -> giữ nguyên cache cục bộ, không ghi đè server"
                    }
                    syncCallPermissionRequirementFromCacheFallback()
                    shouldRegisterToken = GlobalData.preferences.getBoolean(PREF_PUSH_NOTIFICATIONS_ENABLED, true)
                } else {
                    val explicitEnabled = body.global?.enabled
                    if (explicitEnabled != null) {
                        val localSnapshot = cachedNotificationSettings ?: loadCachedNotificationSettingsFromPrefs()
                        val mergedOverrides = AiTypeHelper.mergeNotificationOverrides(
                            localOverrides = localSnapshot?.overrides,
                            serverOverrides = body.overrides
                        )
                        val mergedModel = body.copy(overrides = mergedOverrides)
                        lastServerConfirmedSettings = mergedModel
                        saveNotificationSettingsCache(mergedModel)
                        shouldRegisterToken = explicitEnabled
                        savePushEnabledPref(explicitEnabled)
                        TenliApp.syncKeepAliveWorker(getApplication(), explicitEnabled)
                        val rebuiltModules = buildAiModuleConfigs(mergedModel)
                        _notificationConfigState.update {
                            it.copy(
                                pushEnabled = explicitEnabled,
                                aiModules = rebuiltModules
                            )
                        }
                        syncCallPermissionRequirement(explicitEnabled, rebuiltModules)
                    } else {
                        // Tài khoản mới chưa có cấu hình global.enabled trên server -> chủ động khởi tạo mặc định (enabled = true)
                        val initModel = UserNotificationSettingsModel(
                            global = UserNotificationGlobalSetting(
                                enabled = true,
                                pauseUntil = body.global?.pauseUntil
                            ),
                            overrides = body.overrides ?: UserNotificationSettingsModel.defaultNotificationOverridesJson(),
                            globalScripts = body.globalScripts
                        )
                        logInfo {
                            "[KHỞI TẠO CẤU HÌNH CHO TÀI KHOẢN MỚI] PUT /ts/api/v1/user/settings/notification_settings | payload=${runCatching { LoginAuthClient.gson.toJson(initModel) }.getOrNull()}"
                        }
                        val putResp = authRepository.updateUserNotificationSettings(initModel)
                        if (UserSession.accessToken.isEmpty()) return
                        val effectiveModel = if (putResp.isSuccessful) {
                            logInfo { "✅ [KHỞI TẠO CẤU HÌNH MẶC ĐỊNH THÀNH CÔNG] HTTP ${putResp.code()}" }
                            val confirmed = putResp.body()?.takeIf { it.overrides != null } ?: initModel
                            lastServerConfirmedSettings = confirmed
                            confirmed
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
                        TenliApp.syncKeepAliveWorker(getApplication(), true)
                        val rebuiltModules = buildAiModuleConfigs(effectiveModel)
                        _notificationConfigState.update {
                            it.copy(
                                pushEnabled = true,
                                aiModules = rebuiltModules
                            )
                        }
                        syncCallPermissionRequirement(true, rebuiltModules)
                    }
                }
            } else {
                syncCallPermissionRequirementFromCacheFallback()
                logError {
                    val errBody = runCatching { resp.errorBody()?.string() }.getOrNull()
                    "❌ [SYNC CẤU HÌNH THẤT BẠI] HTTP ${resp.code()} | errorBody=$errBody | ${currentAccountDebugInfo()}"
                }
            }
        }.onFailure { e ->
            if (e is CancellationException) throw e
            syncCallPermissionRequirementFromCacheFallback()
            logError(e) { "❌ [EXCEPTION KHI SYNC CẤU HÌNH] ${e.javaClass.simpleName}: ${e.message} | ${currentAccountDebugInfo()}" }
        }
        if (UserSession.accessToken.isEmpty()) return
        if (shouldRegisterToken) {
            FcmTokenManager.registerDevicePushToken(force = false)
        } else {
            logInfo { "[SYNC CẤU HÌNH] Tài khoản đang tắt thông báo trên server (enabled=false) -> không đăng ký FCM token" }
        }
    }

    fun onTabSelected(tab: MainTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun logout() {
        if (!isLoggingOut.compareAndSet(false, true)) return
        permissionSyncJob?.cancel()
        notificationLoadJob?.cancel()
        pushToggleJob?.cancel()
        aiModuleSaveJob?.cancel()
        passwordChangeJob?.cancel()
        pendingNetworkRefresh.set(false)
        pendingNetworkRestoredEvent.set(false)
        pendingActiveNetworkReconnect.set(false)
        cachedNotificationSettings = null
        lastServerConfirmedSettings = null
        hasResolvedNotificationSettingsOnce = false
        PermissionUtils.resetCallPermissionRequirement()
        webSocketManager.disconnectAll()
        _event.trySend(MainEvent.Logout)
    }

    override fun onCleared() {
        permissionSyncJob?.cancel()
        notificationLoadJob?.cancel()
        pushToggleJob?.cancel()
        aiModuleSaveJob?.cancel()
        passwordChangeJob?.cancel()
        super.onCleared()
    }

    // ---------------------------------------------------------------------------------
    // Cấu hình thông báo
    // ---------------------------------------------------------------------------------

    /** Gọi khi mở màn hình Cấu hình thông báo: hiển thị giá trị cache ngay, rồi đồng bộ với server. */
    fun loadNotificationConfig() {
        val prefs = GlobalData.preferences
        val hasCachedPref = prefs.contains(PREF_PUSH_NOTIFICATIONS_ENABLED)
        val cachedEnabled = prefs.getBoolean(PREF_PUSH_NOTIFICATIONS_ENABLED, true)
        val inMemorySettings = cachedNotificationSettings
        val initialModules = buildAiModuleConfigs(inMemorySettings)
        _notificationConfigState.value = NotificationConfigState(
            isLoading = !hasCachedPref && inMemorySettings == null,
            isSaving = false,
            pushEnabled = cachedEnabled,
            aiModules = initialModules
        )

        notificationLoadJob?.cancel()
        notificationLoadJob = viewModelScope.launch {
            val currentJob = coroutineContext[Job]
            try {
                // Giải mã JSON cache từ đĩa trên Dispatchers.IO để không chặn Main thread
                if (cachedNotificationSettings == null) {
                    val diskCached = loadCachedNotificationSettingsFromPrefs()
                    if (diskCached != null && cachedNotificationSettings == null && UserSession.accessToken.isNotEmpty()) {
                        cachedNotificationSettings = diskCached
                        val diskEnabled = prefs.getBoolean(
                            PREF_PUSH_NOTIFICATIONS_ENABLED,
                            diskCached.global?.enabled ?: cachedEnabled
                        )
                        val diskModules = buildAiModuleConfigs(diskCached)
                        _notificationConfigState.update {
                            it.copy(
                                isLoading = false,
                                pushEnabled = diskEnabled,
                                aiModules = diskModules
                            )
                        }
                    }
                }

                // Đồng bộ lại quyền mới nhất của user từ server để danh sách bài AI luôn khớp phân quyền hiện tại
                val resp = withContext(Dispatchers.IO) {
                    runCatching { UserPermissionManager.fetchAndApplyPermissions() }.onFailure { e ->
                        if (e is CancellationException) throw e
                    }
                    logInfo {
                        "[TẢI CẤU HÌNH] GET /ts/api/v1/user/settings/notification_settings | hasCachedPref=$hasCachedPref, cachedEnabled=$cachedEnabled | ${currentAccountDebugInfo()}"
                    }
                    authRepository.getUserNotificationSettings()
                }
                if (resp.isSuccessful) {
                    val body = resp.body()
                    withContext(Dispatchers.IO) {
                        logInfo {
                            val rawJson = runCatching { LoginAuthClient.gson.toJson(body) }.getOrNull()
                            "✅ [TẢI CẤU HÌNH THÀNH CÔNG] HTTP ${resp.code()} | body=$rawJson"
                        }
                    }
                    if (body != null) {
                        val localSnapshot = cachedNotificationSettings ?: loadCachedNotificationSettingsFromPrefs()
                        val mergedOverrides = AiTypeHelper.mergeNotificationOverrides(
                            localOverrides = localSnapshot?.overrides,
                            serverOverrides = body.overrides
                        )
                        val mergedModel = body.copy(overrides = mergedOverrides)
                        lastServerConfirmedSettings = mergedModel
                        saveNotificationSettingsCache(mergedModel)
                        val enabled = mergedModel.global?.enabled ?: true
                        val rebuiltModules = buildAiModuleConfigs(mergedModel)
                        _notificationConfigState.update {
                            it.copy(
                                pushEnabled = enabled,
                                aiModules = rebuiltModules
                            )
                        }
                        savePushEnabledPref(enabled)
                        syncCallPermissionRequirement(enabled, rebuiltModules)
                    }
                } else {
                    withContext(Dispatchers.IO) {
                        logError {
                            val errBody = runCatching { resp.errorBody()?.string() }.getOrNull()
                            "❌ [TẢI CẤU HÌNH THẤT BẠI] GET /ts/api/v1/user/settings/notification_settings -> HTTP ${resp.code()} | errorBody=$errBody | ${currentAccountDebugInfo()}"
                        }
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
                if (notificationLoadJob === currentJob) {
                    _notificationConfigState.update { it.copy(isLoading = false) }
                }
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
        pushToggleJob?.cancel()

        val previousSettings = lastServerConfirmedSettings ?: cachedNotificationSettings
        val previousEnabled = lastServerConfirmedSettings?.global?.enabled ?: _notificationConfigState.value.pushEnabled
        val previousModules = _notificationConfigState.value.aiModules

        _notificationConfigState.update { it.copy(pushEnabled = enabled, isSaving = true) }
        savePushEnabledPref(enabled)
        syncCallPermissionRequirement(enabled, previousModules)

        pushToggleJob = viewModelScope.launch {
            val currentJob = coroutineContext[Job]
            val saveFailedMsg = localized("Failed to save notification settings", "Không thể lưu cấu hình thông báo")
            var resolvedRollbackSettings = previousSettings
            try {
                logInfo {
                    "[LƯU CẤU HÌNH - BẮT ĐẦU] enabled=$enabled, hasOsPermission=$hasOsPermission, hasCachedModel=${cachedNotificationSettings != null} | ${currentAccountDebugInfo()}"
                }
                val baseSettings = cachedNotificationSettings
                    ?: loadCachedNotificationSettingsFromPrefs()
                    ?: fetchServerNotificationSettingsQuietly()
                if (resolvedRollbackSettings == null) {
                    resolvedRollbackSettings = baseSettings
                }

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

                val resp = withContext(Dispatchers.IO) {
                    logInfo {
                        val requestJson = runCatching { LoginAuthClient.gson.toJson(updatedModel) }.getOrNull()
                        "[LƯU CẤU HÌNH - GỬI PUT] PUT /ts/api/v1/user/settings/notification_settings | payload=$requestJson | ${currentAccountDebugInfo()}"
                    }
                    authRepository.updateUserNotificationSettings(updatedModel)
                }
                if (resp.isSuccessful) {
                    val respBody = resp.body()
                    withContext(Dispatchers.IO) {
                        logInfo {
                            val respJson = runCatching { LoginAuthClient.gson.toJson(respBody) }.getOrNull()
                            "✅ [LƯU CẤU HÌNH THÀNH CÔNG] PUT /ts/api/v1/user/settings/notification_settings -> HTTP ${resp.code()} | response=$respJson"
                        }
                    }
                    // Giữ nguyên updatedModel (không ghi đè bằng resp.body() vì PUT trả về model rỗng overrides)
                    lastServerConfirmedSettings = updatedModel
                    saveNotificationSettingsCache(updatedModel)
                    val rebuiltModules = buildAiModuleConfigs(updatedModel)
                    _notificationConfigState.update {
                        it.copy(aiModules = rebuiltModules)
                    }
                    syncCallPermissionRequirement(enabled, rebuiltModules)
                    withContext(Dispatchers.IO) {
                        TenliApp.syncKeepAliveWorker(getApplication(), enabled)
                        runCatching {
                            if (enabled) {
                                FcmTokenManager.registerDevicePushToken(force = true)
                            } else {
                                FcmTokenManager.unregisterDevicePushToken()
                            }
                        }.onFailure { tokenErr ->
                            if (tokenErr is CancellationException) throw tokenErr
                            logWarn(tokenErr) { "⚠️ [LƯU CẤU HÌNH] Lỗi đồng bộ FCM token sau khi PUT thành công: ${tokenErr.message}" }
                        }
                    }
                } else {
                    withContext(Dispatchers.IO) {
                        logError {
                            val errBody = runCatching { resp.errorBody()?.string() }.getOrNull()
                            val requestJson = runCatching { LoginAuthClient.gson.toJson(updatedModel) }.getOrNull()
                            "❌ [LƯU CẤU HÌNH THẤT BẠI - HTTP ERROR] PUT /ts/api/v1/user/settings/notification_settings -> HTTP ${resp.code()} (${resp.message()}) | errorBody=$errBody | requestPayload=$requestJson | ${currentAccountDebugInfo()}"
                        }
                    }
                    val rollbackEnabled = resolvedRollbackSettings?.global?.enabled ?: previousEnabled
                    val rollbackModules = if (resolvedRollbackSettings != null) {
                        buildAiModuleConfigs(resolvedRollbackSettings)
                    } else {
                        previousModules
                    }
                    saveNotificationSettingsCache(resolvedRollbackSettings)
                    _notificationConfigState.update {
                        it.copy(pushEnabled = rollbackEnabled, aiModules = rollbackModules)
                    }
                    savePushEnabledPref(rollbackEnabled)
                    syncCallPermissionRequirement(rollbackEnabled, rollbackModules)
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
                val rollbackEnabled = resolvedRollbackSettings?.global?.enabled ?: previousEnabled
                val rollbackModules = if (resolvedRollbackSettings != null) {
                    buildAiModuleConfigs(resolvedRollbackSettings)
                } else {
                    previousModules
                }
                saveNotificationSettingsCache(resolvedRollbackSettings)
                _notificationConfigState.update {
                    it.copy(pushEnabled = rollbackEnabled, aiModules = rollbackModules)
                }
                savePushEnabledPref(rollbackEnabled)
                syncCallPermissionRequirement(rollbackEnabled, rollbackModules)
                MobileTopPopupManager.error(saveFailedMsg)
            } finally {
                if (pushToggleJob === currentJob) {
                    _notificationConfigState.update { it.copy(isSaving = false) }
                }
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
        val previousSettings = cachedNotificationSettings
        val currentPushEnabled = _notificationConfigState.value.pushEnabled

        val immediateOptimisticModel = if (previousSettings != null) {
            val baseGlobal = previousSettings.global ?: UserNotificationGlobalSetting(enabled = currentPushEnabled)
            val updatedOverrides = AiTypeHelper.applyModuleOverrideToJson(
                baseOverrides = previousSettings.overrides,
                module = module,
                enabled = enabled,
                alarmMode = alarmMode.apiValue
            )
            previousSettings.copy(
                global = baseGlobal,
                overrides = updatedOverrides
            ).also { cachedNotificationSettings = it }
        } else {
            null
        }

        // Cập nhật UI tức thì trên Main thread (0ms Gson I/O; giữ nguyên vị trí các bài AI)
        val optimisticModules = if (immediateOptimisticModel != null) {
            buildAiModuleConfigs(immediateOptimisticModel)
        } else {
            previousModules.map { item ->
                if (item.moduleKey == module.key) item.copy(enabled = enabled, alarmMode = alarmMode) else item
            }
        }
        _notificationConfigState.update { state ->
            state.copy(aiModules = optimisticModules)
        }
        syncCallPermissionRequirement(currentPushEnabled, optimisticModules)

        aiModuleSaveJob?.cancel()
        aiModuleSaveJob = viewModelScope.launch {
            val saveFailedMsg = localized("Failed to save notification settings", "Không thể lưu cấu hình thông báo")
            var fallbackRollbackSettings = previousSettings
            try {
                val modelToSave = immediateOptimisticModel ?: run {
                    val diskSettings = loadCachedNotificationSettingsFromPrefs()
                    fallbackRollbackSettings = diskSettings
                    val baseGlobal = diskSettings?.global ?: UserNotificationGlobalSetting(enabled = currentPushEnabled)
                    val updatedOverrides = AiTypeHelper.applyModuleOverrideToJson(
                        baseOverrides = diskSettings?.overrides,
                        module = module,
                        enabled = enabled,
                        alarmMode = alarmMode.apiValue
                    )
                    if (diskSettings != null) {
                        diskSettings.copy(global = baseGlobal, overrides = updatedOverrides)
                    } else {
                        UserNotificationSettingsModel(global = baseGlobal, overrides = updatedOverrides)
                    }
                }

                // Serialize JSON xuống SharedPreferences trên Dispatchers.IO
                saveNotificationSettingsCache(modelToSave)

                val resp = withContext(Dispatchers.IO) {
                    logInfo {
                        val requestJson = runCatching { LoginAuthClient.gson.toJson(modelToSave) }.getOrNull()
                        "[LƯU CẤU HÌNH BÀI AI] module=${module.key}, enabled=$enabled, alarmMode=${alarmMode.apiValue} | PUT /ts/api/v1/user/settings/notification_settings | payload=$requestJson"
                    }
                    authRepository.updateUserNotificationSettings(modelToSave)
                }
                if (resp.isSuccessful) {
                    lastServerConfirmedSettings = modelToSave
                    logInfo {
                        "✅ [LƯU CẤU HÌNH BÀI AI THÀNH CÔNG] module=${module.key}, enabled=$enabled, alarmMode=${alarmMode.apiValue} -> HTTP ${resp.code()}"
                    }
                    // Lưu ý: Không ghi đè cachedNotificationSettings bằng resp.body() vì API PUT trả về object không chứa overrides.event_types
                } else {
                    withContext(Dispatchers.IO) {
                        logError {
                            val errBody = runCatching { resp.errorBody()?.string() }.getOrNull()
                            "❌ [LƯU CẤU HÌNH BÀI AI THẤT BẠI] module=${module.key} -> HTTP ${resp.code()} | errorBody=$errBody"
                        }
                    }
                    val rollbackModel = lastServerConfirmedSettings ?: fallbackRollbackSettings
                    val rollbackModules = if (rollbackModel != null) buildAiModuleConfigs(rollbackModel) else previousModules
                    val rollbackPushEnabled = rollbackModel?.global?.enabled ?: _notificationConfigState.value.pushEnabled
                    saveNotificationSettingsCache(rollbackModel)
                    _notificationConfigState.update {
                        it.copy(pushEnabled = rollbackPushEnabled, aiModules = rollbackModules)
                    }
                    savePushEnabledPref(rollbackPushEnabled)
                    syncCallPermissionRequirement(rollbackPushEnabled, rollbackModules)
                    MobileTopPopupManager.error(saveFailedMsg)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                logError(e) {
                    "❌ [EXCEPTION LƯU CẤU HÌNH BÀI AI] module=${module.key} -> ${e.javaClass.simpleName}: ${e.message}"
                }
                val rollbackModel = lastServerConfirmedSettings ?: fallbackRollbackSettings
                val rollbackModules = if (rollbackModel != null) buildAiModuleConfigs(rollbackModel) else previousModules
                val rollbackPushEnabled = rollbackModel?.global?.enabled ?: _notificationConfigState.value.pushEnabled
                saveNotificationSettingsCache(rollbackModel)
                _notificationConfigState.update {
                    it.copy(pushEnabled = rollbackPushEnabled, aiModules = rollbackModules)
                }
                savePushEnabledPref(rollbackPushEnabled)
                syncCallPermissionRequirement(rollbackPushEnabled, rollbackModules)
                MobileTopPopupManager.error(saveFailedMsg)
            }
        }
    }

    private suspend fun fetchServerNotificationSettingsQuietly(): UserNotificationSettingsModel? {
        return withContext(Dispatchers.IO) {
            runCatching {
                logInfo { "[LƯU CẤU HÌNH] cachedNotificationSettings=null -> gọi GET /ts/api/v1/user/settings/notification_settings trước khi PUT" }
                val getResp = authRepository.getUserNotificationSettings()
                if (getResp.isSuccessful) {
                    getResp.body()?.also { serverBody ->
                        if (lastServerConfirmedSettings == null) {
                            lastServerConfirmedSettings = serverBody
                        }
                    }
                } else {
                    null
                }
            }.onFailure { e ->
                if (e is CancellationException) throw e
            }.getOrNull()
        }
    }

    private fun buildAiModuleConfigs(
        settings: UserNotificationSettingsModel?
    ): List<AiModuleNotifyConfig> {
        val overrides = settings?.overrides
        return AiTypeHelper.activeModules.map { mod ->
            val (enabled, modeStr) = AiTypeHelper.extractModuleNotifyConfig(mod, overrides)
            AiModuleNotifyConfig(
                moduleKey = mod.key,
                sensorType = mod.sensorType,
                labelVi = mod.labelVi,
                labelEn = mod.labelEn,
                eventTypes = mod.eventTypes,
                enabled = enabled,
                alarmMode = AiAlarmMode.fromApiValue(modeStr)
            )
        }
    }

    private suspend fun saveNotificationSettingsCache(model: UserNotificationSettingsModel?) {
        if (model != null && UserSession.accessToken.isEmpty()) return
        cachedNotificationSettings = model
        withContext(Dispatchers.IO) {
            if (model != null && UserSession.accessToken.isEmpty()) return@withContext
            // Tránh bản ghi cũ trên IO ghi đè model mới hơn đã cập nhật trong RAM (hoặc đã logout)
            if (cachedNotificationSettings !== model) return@withContext
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
    }

    private suspend fun loadCachedNotificationSettingsFromPrefs(): UserNotificationSettingsModel? {
        return withContext(Dispatchers.IO) {
            runCatching {
                val raw = GlobalData.preferences.getString(
                    AiTypeHelper.PREF_USER_NOTIFICATION_SETTINGS_JSON,
                    null
                )
                raw?.let { LoginAuthClient.gson.fromJson(it, UserNotificationSettingsModel::class.java) }
            }.getOrNull()
        }
    }

    private fun savePushEnabledPref(enabled: Boolean) {
        if (UserSession.accessToken.isEmpty()) return
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
                val resp = authRepository.changePassword(
                    ChangePasswordModel(
                        userId = userId,
                        oldPassword = oldPassword,
                        newPassword = newPassword
                    )
                )
                if (resp.isSuccessful) {
                    UserSession.savedPassword = ""
                    GlobalData.preferences.edit {
                        remove(AppKeys.PASSWORD_KEY)
                    }
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
                    application = application,
                    authRepository = application.container.authRepository,
                    networkMonitor = application.container.networkMonitor,
                    webSocketManager = application.container.webSocketManager
                )
            }
        }
    }
}
