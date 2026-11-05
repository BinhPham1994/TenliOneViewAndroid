package com.tenli.oneview.data.local

import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.tenli.oneview.model.network.UserData
import com.tenli.oneview.ui.utils.AppKeys

/**
 * Phiên đăng nhập hiện tại (in-memory).
 * Các field được đọc từ nhiều thread (OkHttp interceptor/authenticator, WebSocket, UI) nên đánh dấu @Volatile
 * để mọi thread luôn thấy giá trị mới nhất (vd. token vừa được re-login).
 */
object UserSession {
    @Volatile var userData: UserData? = null
    @Volatile var accessToken: String = ""
    @Volatile var refreshToken: String = ""
    @Volatile var domain: String = ""
    @Volatile var savedUsername: String = ""
    @Volatile var savedPassword: String = ""

    private val gson by lazy { Gson() }

    private const val DOMAIN_KEY = "DOMAIN_KEY"

    fun hasSavedCredentials(): Boolean =
        savedUsername.isNotEmpty() && savedPassword.isNotEmpty() && domain.isNotEmpty()

    /**
     * Khôi phục phiên đăng nhập từ [SharedPreferences] nếu RAM đang trống
     * (vd. khi tiến trình được hệ thống khởi chạy ngầm để nhận FCM push hoặc chạy Worker).
     */
    @Synchronized
    fun ensureRestored(prefs: SharedPreferences? = runCatching { GlobalData.preferences }.getOrNull()) {
        val sp = prefs ?: return
        if (accessToken.isNotEmpty() || refreshToken.isNotEmpty()) return
        accessToken = sp.getString(AppKeys.ACCESS_TOKEN_KEY, "").orEmpty()
        refreshToken = sp.getString(AppKeys.REFRESH_TOKEN_KEY, "").orEmpty()
        domain = sp.getString(DOMAIN_KEY, "").orEmpty()
        savedUsername = sp.getString(AppKeys.USERNAME_KEY, "").orEmpty()
        savedPassword = sp.getString(AppKeys.PASSWORD_KEY, "").orEmpty()
        val userDataJson = sp.getString(AppKeys.USER_DATA_KEY, null)
        if (!userDataJson.isNullOrEmpty() && userData == null) {
            userData = runCatching { gson.fromJson(userDataJson, UserData::class.java) }.getOrNull()
        }
    }

    fun clear() {
        userData = null
        accessToken = ""
        refreshToken = ""
        domain = ""
        savedUsername = ""
        savedPassword = ""
    }

    fun saveSession(prefs: SharedPreferences?) {
        val userDataJson = gson.toJson(this.userData)
        prefs?.edit { putString(AppKeys.USER_DATA_KEY, userDataJson) }
    }
}
