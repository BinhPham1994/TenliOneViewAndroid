package com.tenli.oneview.data.local

import android.content.SharedPreferences
import com.google.gson.Gson
import com.tenli.oneview.model.network.UserData
import com.tenli.oneview.ui.utils.AppKeys

object UserSession {
    var userData: UserData? = null
    var accessToken: String = ""
    var refreshToken: String = ""
    var domain: String = ""
    var savedUsername: String = ""
    var savedPassword: String = ""

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
        val userDataJson = Gson().toJson(this.userData)
        prefs?.edit()?.putString(AppKeys.USER_DATA_KEY, userDataJson)?.apply()
    }
}