package com.tenli.oneview.data.network.retrofit

import android.util.Log
import androidx.core.content.edit
import com.tenli.oneview.data.local.GlobalData
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.model.network.LogInModel
import com.tenli.oneview.ui.utils.AppKeys
import com.tenli.oneview.util.EventBus
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import java.util.concurrent.TimeUnit

/**
 * OkHttp Authenticator xử lý tự động re-login khi gặp 401 Unauthorized.
 *
 * - Khi token hết hạn (401), OkHttp tự gọi [authenticate].
 * - Dùng synchronized để đảm bảo chỉ 1 thread thực hiện re-login,
 *   các thread khác đợi và dùng lại token mới.
 * - Retry tối đa 1 lần — nếu re-login thất bại thì trả null (lỗi 401 đến caller).
 */
class TokenAuthenticator : Authenticator {

    companion object {
        private const val TAG = "TokenAuthenticator"
        private val lock = Any()

        private const val LOGIN_TIMEOUT_SEC = 15L
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        // Client re-login (không có Authenticator để tránh vòng lặp), dùng chung pool/thread với LoginAuthClient
        private val loginClient: OkHttpClient by lazy {
            LoginAuthClient.baseClient.newBuilder()
                .connectTimeout(LOGIN_TIMEOUT_SEC, TimeUnit.SECONDS)
                .readTimeout(LOGIN_TIMEOUT_SEC, TimeUnit.SECONDS)
                .writeTimeout(LOGIN_TIMEOUT_SEC, TimeUnit.SECONDS)
                .build()
        }

        /**
         * Làm mới accessToken đồng bộ (single-flight dưới [lock]), dùng chung cho cả HTTP [Authenticator],
         * WebSocket reconnect và ExoPlayer khi gặp lỗi 401/403 do token ngắn hạn (vd. 15 phút).
         */
        fun refreshAccessToken(failedToken: String = UserSession.accessToken): String? {
            if (!UserSession.hasSavedCredentials()) {
                Log.w(TAG, "Không có credentials đã lưu → không thể tự re-login")
                if (UserSession.accessToken.isNotEmpty()) {
                    EventBus.emitUnauthorized()
                }
                return null
            }

            synchronized(lock) {
                val currentToken = UserSession.accessToken
                if (currentToken.isEmpty()) {
                    return null
                }
                if (failedToken.isNotEmpty() && currentToken != failedToken) {
                    Log.d(TAG, "Token đã được cập nhật bởi thread khác → dùng token mới")
                    return currentToken
                }

                Log.d(TAG, "Đang thực hiện re-login...")
                return try {
                    performSyncLogin()
                } catch (e: Exception) {
                    Log.e(TAG, "Lỗi khi re-login: ${e.message}")
                    null
                }
            }
        }

        /**
         * Thực hiện login đồng bộ (blocking) để lấy token mới.
         * Dùng OkHttpClient riêng (không có Authenticator) để tránh vòng lặp.
         */
        private fun performSyncLogin(): String? {
            val domain = UserSession.domain
            val username = UserSession.savedUsername
            val password = UserSession.savedPassword

            if (domain.isEmpty() || username.isEmpty() || password.isEmpty()) {
                return null
            }

            val normalizedDomain = domain.trim().trimEnd('/').removeSuffix("/api/v1")
            val loginUrl = "$normalizedDomain/VMS/api/LogIn"

            val gson = LoginAuthClient.gson
            val jsonBody = gson.toJson(LogInModel(username = username, password = password))
            val body = jsonBody.toRequestBody(JSON_MEDIA_TYPE)

            val request = Request.Builder()
                .url(loginUrl)
                .post(body)
                .header("Content-Type", "application/json")
                .build()

            return loginClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w(TAG, "Re-login HTTP ${response.code}")
                    if (response.code == 400 || response.code == 401 || response.code == 403) {
                        EventBus.emitUnauthorized()
                    }
                    return null
                }

                val responseBody = response.body?.string() ?: return null
                val tokenModel = try {
                    gson.fromJson(responseBody, com.tenli.oneview.model.network.TokenModel::class.java)
                } catch (e: Exception) {
                    Log.e(TAG, "Parse token response lỗi: ${e.message}")
                    return null
                }

                if (tokenModel?.token.isNullOrBlank()) {
                    return null
                }

                // Chống ghi đè "zombie token" nếu người dùng đã bấm Đăng xuất hoặc đổi tài khoản/domain trong lúc chờ HTTP re-login
                if (UserSession.accessToken.isEmpty() ||
                    UserSession.domain != domain ||
                    UserSession.savedUsername != username
                ) {
                    Log.w(TAG, "Phiên đăng nhập đã thay đổi hoặc đăng xuất trong lúc re-login → huỷ cập nhật token")
                    return null
                }

                // Cập nhật UserSession
                synchronized(UserSession) {
                    UserSession.accessToken = tokenModel.token
                    UserSession.refreshToken = tokenModel.token
                }

                // Cập nhật SharedPreferences
                try {
                    GlobalData.preferences.edit {
                        putString(AppKeys.ACCESS_TOKEN_KEY, tokenModel.token)
                        putString(AppKeys.REFRESH_TOKEN_KEY, tokenModel.token)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Lưu token vào SharedPreferences lỗi: ${e.message}")
                }

                tokenModel.token
            }
        }
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        val reqUrl = response.request.url
        val path = reqUrl.encodedPath

        // Đồng bộ với isAuthBypassUrl trong api.ts của PWA:
        // Không retry re-login cho LogIn, LogOut, DELETE notification-tokens (lúc logout) hoặc host ngoài
        val isUnregisterToken = path.contains("/notification-tokens", ignoreCase = true) &&
            response.request.method.equals("DELETE", ignoreCase = true)
        if (path.contains("LogIn", ignoreCase = true) ||
            path.contains("LogOut", ignoreCase = true) ||
            isUnregisterToken
        ) {
            return null
        }

        if (!HostResolver.isMainBackendHost(reqUrl.host)) {
            return null
        }

        // Chỉ retry 1 lần duy nhất
        if (responseCount(response) >= 2) {
            Log.w(TAG, "Đã retry re-login 1 lần, vẫn bị 401 → dừng lại")
            return null
        }

        // Token thực tế đã gửi kèm request bị 401 (đọc từ request header thay vì biến toàn cục để chống đua luồng 401 lệch nhịp)
        val failedToken = response.request.header("Authorization")
            ?.removePrefix("Bearer ")
            ?.removePrefix("bearer ")
            ?.trim()
            .orEmpty()

        val newToken = refreshAccessToken(failedToken)
        return if (newToken != null) {
            Log.d(TAG, "Re-login thành công → retry request với token mới")
            response.request.newBuilder()
                .header("Authorization", "Bearer $newToken")
                .build()
        } else {
            Log.w(TAG, "Re-login thất bại → trả 401")
            null
        }
    }

    /**
     * Đếm số lần response đã bị retry (qua priorResponse chain).
     */
    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
