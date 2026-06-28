package com.tenli.oneview.data.network.retrofit

import android.util.Log
import androidx.core.content.edit
import com.tenli.oneview.BuildConfig
import com.tenli.oneview.data.local.GlobalData
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.model.network.LogInModel
import com.tenli.oneview.ui.utils.AppKeys
import okhttp3.Authenticator
import okhttp3.Credentials
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
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        // Không retry cho request login — tránh vòng lặp vô hạn
        if (response.request.url.encodedPath.contains("LogIn", ignoreCase = true) ||
            response.request.url.encodedPath.contains("LogOut", ignoreCase = true)) {
            return null
        }

        // Chỉ retry 1 lần duy nhất
        if (responseCount(response) >= 2) {
            Log.w(TAG, "Đã retry re-login 1 lần, vẫn bị 401 → dừng lại")
            return null
        }

        // Không có credentials đã lưu → không thể re-login
        if (!UserSession.hasSavedCredentials()) {
            Log.w(TAG, "Không có credentials đã lưu → không thể tự re-login")
            return null
        }

        // Token lúc request bị 401
        val failedToken = UserSession.accessToken

        synchronized(lock) {
            // Kiểm tra xem thread khác đã re-login thành công chưa
            val currentToken = UserSession.accessToken
            if (currentToken.isNotEmpty() && currentToken != failedToken) {
                // Token đã được thread khác cập nhật → dùng luôn token mới
                Log.d(TAG, "Token đã được cập nhật bởi thread khác → dùng token mới")
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            // Thread này phải thực hiện re-login
            Log.d(TAG, "Đang thực hiện re-login...")
            return try {
                val newToken = performSyncLogin()
                if (newToken != null) {
                    Log.d(TAG, "Re-login thành công → retry request với token mới")
                    response.request.newBuilder()
                        .header("Authorization", "Bearer $newToken")
                        .build()
                } else {
                    Log.w(TAG, "Re-login thất bại → trả 401")
                    null
                }
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

        val loginUrl = "$domain/VMS/api/LogIn"

        // Tạo OkHttpClient riêng cho login — KHÔNG có Authenticator
        val loginClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()

        val gson = com.google.gson.Gson()
        val jsonBody = gson.toJson(LogInModel(username = username, password = password))
        val body = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(loginUrl)
            .post(body)
            .header("Authorization", Credentials.basic(BuildConfig.CLIENT_ID, BuildConfig.CLIENT_SECRET))
            .header("Content-Type", "application/json")
            .build()

        val response = loginClient.newCall(request).execute()

        if (!response.isSuccessful) {
            Log.w(TAG, "Re-login HTTP ${response.code}")
            response.close()
            return null
        }

        val responseBody = response.body?.string() ?: return null
        val tokenModel = try {
            gson.fromJson(responseBody, com.tenli.oneview.model.network.TokenModel::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Parse token response lỗi: ${e.message}")
            return null
        }

        // Cập nhật UserSession
        UserSession.accessToken = tokenModel.token
        UserSession.refreshToken = tokenModel.token

        // Cập nhật SharedPreferences
        try {
            GlobalData.preferences.edit {
                putString(AppKeys.ACCESS_TOKEN_KEY, tokenModel.token)
                putString(AppKeys.REFRESH_TOKEN_KEY, tokenModel.token)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Lưu token vào SharedPreferences lỗi: ${e.message}")
        }

        return tokenModel.token
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
