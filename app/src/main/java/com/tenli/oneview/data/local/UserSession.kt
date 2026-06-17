package com.tenli.oneview.data.local

import android.content.SharedPreferences
import com.google.gson.Gson
import com.tenli.oneview.model.network.UserData
import com.tenli.oneview.ui.utils.AppKeys

object UserSession {
    var userData: UserData? = null
    var accessToken: String = ""
    var refreshToken: String = ""

    fun clear() {
        userData = null
        accessToken = ""
        refreshToken = ""
    }

    fun saveSession(prefs: SharedPreferences?) {
        val userDataJson = Gson().toJson(this.userData)
        prefs?.edit()?.putString(AppKeys.USER_DATA_KEY, userDataJson)?.apply()
    }
}