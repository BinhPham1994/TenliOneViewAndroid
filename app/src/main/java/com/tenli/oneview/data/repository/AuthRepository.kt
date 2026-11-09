package com.tenli.oneview.data.repository

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import com.tenli.oneview.data.local.EventCacheManager
import com.tenli.oneview.data.local.GlobalData
import com.tenli.oneview.data.local.HomeCacheManager
import com.tenli.oneview.data.local.MonitorCacheManager
import com.tenli.oneview.data.local.UserPermissionManager
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.data.network.ApiProvider
import com.tenli.oneview.data.network.api.BsApi
import com.tenli.oneview.data.network.api.VmsApi
import com.tenli.oneview.firebase.FcmTokenManager
import com.tenli.oneview.model.network.ChangePasswordModel
import com.tenli.oneview.model.network.LogInModel
import com.tenli.oneview.model.network.LoginResponseData
import com.tenli.oneview.model.network.UserCredential
import com.tenli.oneview.model.network.UserData
import com.tenli.oneview.model.network.UserNotificationSettingsModel
import com.tenli.oneview.ui.utils.AiTypeHelper
import com.tenli.oneview.ui.utils.AppKeys
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class AuthRepository(
    private val context: Context,
    private val vmsApi: VmsApi = ApiProvider.vmsApi,
    private val bsApi: BsApi = ApiProvider.bsApi
) {

    suspend fun loginVms(domain: String, request: LogInModel): Result<LoginResponseData?> {
        val candidates = buildCandidateDomains(domain)
        if (candidates.isEmpty()) {
            return Result.failure(IllegalArgumentException("Domain is empty"))
        }

        var lastError: Exception? = null
        for (formattedDomain in candidates) {
            try {
                val loginUrl = "$formattedDomain/VMS/api/LogIn"
                val response = vmsApi.logInExt(loginUrl, request)
                if (response.isSuccessful) {
                    val tokenModel = response.body() ?: throw Exception("Token response is null")

                    val vmsUser = UserPermissionManager.fetchVmsUserWithRetry(
                        domain = formattedDomain,
                        token = tokenModel.token,
                        userId = tokenModel.userId
                    )
                    val resolvedUsername = vmsUser?.username?.trim()?.takeIf { it.isNotEmpty() }
                        ?: request.username.trim()
                    val effectiveIsAdmin = tokenModel.isAdmin && resolvedUsername == "admin"
                    val resolvedPermissions = vmsUser?.extra?.permissions

                    val loginResponseData = LoginResponseData(
                        target = UserData(
                            id = tokenModel.userId,
                            uuid = tokenModel.userId.toString(),
                            name = vmsUser?.name?.takeIf { it.isNotBlank() } ?: resolvedUsername,
                            email = resolvedUsername,
                            phoneNumber = "",
                            gender = 0,
                            address = "",
                            language = "vi",
                            avatar = null,
                            username = resolvedUsername,
                            isAdmin = effectiveIsAdmin,
                            permissions = resolvedPermissions
                        ),
                        credential = UserCredential(
                            accessToken = tokenModel.token,
                            refreshToken = tokenModel.token
                        ),
                        resolvedDomain = formattedDomain
                    )

                    return Result.success(loginResponseData)
                } else {
                    val code = response.code()
                    val httpError = Exception("Login failed with code $code")
                    // Nếu máy chủ đã phản hồi 400/401 (sai tài khoản/mật khẩu), dừng ngay không thử giao thức còn lại
                    if (code == 400 || code == 401) {
                        return Result.failure(httpError)
                    }
                    lastError = httpError
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                lastError = e
            }
        }

        return Result.failure(lastError ?: Exception("Login failed"))
    }

    /**
     * Thiết lập phiên đăng nhập mới: xoá sạch dữ liệu/cache của phiên trước (nếu có),
     * cập nhật [UserSession], đồng bộ quyền [UserPermissionManager] và lưu vào [SharedPreferences].
     */
    suspend fun establishAuthenticatedSession(
        loginData: LoginResponseData,
        formattedDomain: String,
        username: String,
        password: String,
        rememberMe: Boolean
    ) {
        val appContext = context.applicationContext
        withContext(Dispatchers.IO) {
            HomeCacheManager.clearCache(appContext)
            EventCacheManager.clearCache(appContext)
            MonitorCacheManager.clearCache(appContext)
        }
        UserSession.apply {
            userData = loginData.target
            accessToken = loginData.credential.accessToken
            refreshToken = loginData.credential.refreshToken
            this.domain = formattedDomain
            savedUsername = username
            savedPassword = password
        }
        UserPermissionManager.syncFromUserSession()
        withContext(Dispatchers.IO) {
            val userDataJson = GlobalData.gson.toJson(loginData.target)
            GlobalData.preferences.edit {
                putString(AppKeys.USER_DATA_KEY, userDataJson)
                putString(AppKeys.ACCESS_TOKEN_KEY, loginData.credential.accessToken)
                putString(AppKeys.REFRESH_TOKEN_KEY, loginData.credential.refreshToken)
                putString(PREF_DOMAIN_KEY, formattedDomain)
                putBoolean(AppKeys.REMEMBER_ME_KEY, rememberMe)
                putBoolean(AppKeys.TERMS_ACCEPTED_KEY, true)
                if (rememberMe) {
                    putString(AppKeys.USERNAME_KEY, username)
                    putString(AppKeys.PASSWORD_KEY, password)
                } else {
                    remove(AppKeys.USERNAME_KEY)
                    remove(AppKeys.PASSWORD_KEY)
                }
                remove(PREF_PUSH_NOTIFICATIONS_ENABLED)
                remove(AiTypeHelper.PREF_USER_NOTIFICATION_SETTINGS_JSON)
                remove(PREF_EXPANDED_NODES)
                remove(PREF_MONITOR_GRID_SIZE)
                remove(PREF_MONITOR_GRID_CAMERA_IDS)
                remove(PREF_MONITOR_CAMERA_ID)
            }
        }
    }

    /**
     * Dọn dẹp toàn diện phiên đăng nhập khi người dùng đăng xuất hoặc hết hạn phiên (401):
     * đóng thông báo hệ thống, huỷ đăng ký FCM token, xoá [UserSession], xoá các khóa cấu hình
     * theo tài khoản trong [SharedPreferences] và xoá sạch RAM/Disk/Image cache.
     */
    suspend fun clearAuthenticatedSession() {
        val appContext = context.applicationContext
        runCatching { NotificationManagerCompat.from(appContext).cancelAll() }

        FcmTokenManager.unregisterDevicePushToken()
        UserSession.clear()

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
                remove(PREF_EXPANDED_NODES)
                remove(PREF_MONITOR_GRID_SIZE)
                remove(PREF_MONITOR_GRID_CAMERA_IDS)
                remove(PREF_MONITOR_CAMERA_ID)
                if (!rememberMe) {
                    remove(AppKeys.USERNAME_KEY)
                    remove(AppKeys.PASSWORD_KEY)
                }
            }

            HomeCacheManager.clearCache(appContext)
            EventCacheManager.clearCache(appContext)
            MonitorCacheManager.clearCache(appContext)
        }
    }

    suspend fun getUserNotificationSettings(): Response<UserNotificationSettingsModel> =
        bsApi.getUserNotificationSettings()

    suspend fun updateUserNotificationSettings(
        settings: UserNotificationSettingsModel
    ): Response<UserNotificationSettingsModel> =
        bsApi.updateUserNotificationSettings(settings)

    suspend fun changePassword(request: ChangePasswordModel): Response<Unit> =
        vmsApi.changePassword(request)

    /**
     * Tự động suy luận giao thức từ domain người dùng nhập:
     * - Người dùng chỉ cần nhập domain (vd: `app.tenli.ai` hoặc `192.168.1.10:8000`).
     * - Mặc định ưu tiên `https://<domain>`, nếu không kết nối được (ví dụ máy chủ nội bộ dùng HTTP)
     *   sẽ tự động thử tiếp `http://<domain>`.
     */
    private fun buildCandidateDomains(rawDomain: String): List<String> {
        val trimmed = rawDomain.trim()
        if (trimmed.isEmpty()) return emptyList()

        val explicitHttp = trimmed.startsWith("http://", ignoreCase = true)
        val explicitHttps = trimmed.startsWith("https://", ignoreCase = true)
        val hostPart = trimmed
            .replace(Regex("^https?://", RegexOption.IGNORE_CASE), "")
            .replace(Regex("/m/?$", RegexOption.IGNORE_CASE), "")
            .trimEnd('/')
        if (hostPart.isEmpty()) return emptyList()

        return when {
            explicitHttp -> listOf("http://$hostPart", "https://$hostPart")
            explicitHttps -> listOf("https://$hostPart", "http://$hostPart")
            else -> listOf("https://$hostPart", "http://$hostPart")
        }
    }

    companion object {
        const val PREF_DOMAIN_KEY = "DOMAIN_KEY"
        const val PREF_PUSH_NOTIFICATIONS_ENABLED = "push_notifications_enabled"
        private const val PREF_EXPANDED_NODES = "saved_expanded_nodes"
        private const val PREF_MONITOR_GRID_SIZE = "saved_monitor_grid_size"
        private const val PREF_MONITOR_GRID_CAMERA_IDS = "saved_monitor_grid_camera_ids"
        private const val PREF_MONITOR_CAMERA_ID = "saved_monitor_camera_id"
    }
}
