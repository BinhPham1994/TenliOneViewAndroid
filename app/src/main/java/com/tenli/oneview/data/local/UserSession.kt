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

    fun hasSavedCredentials(): Boolean =
        savedUsername.isNotEmpty() && savedPassword.isNotEmpty() && domain.isNotEmpty()

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
