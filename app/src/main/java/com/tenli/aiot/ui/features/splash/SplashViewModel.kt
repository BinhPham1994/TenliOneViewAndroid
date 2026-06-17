package com.tenli.aiot.ui.features.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.tenli.aiot.data.local.GlobalData
import com.tenli.aiot.data.local.UserSession
import com.tenli.aiot.model.network.UserData
import com.tenli.aiot.ui.utils.AppKeys
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState = _uiState.asStateFlow()

    private fun setupAndNavigate() {
        val prefs = GlobalData.preferences
        UserSession.accessToken = prefs.getString(AppKeys.ACCESS_TOKEN_KEY, "").orEmpty()
        UserSession.refreshToken = prefs.getString(AppKeys.REFRESH_TOKEN_KEY, "").orEmpty()
        val userDataJson = prefs.getString(AppKeys.USER_DATA_KEY, null)
        if (!userDataJson.isNullOrEmpty()) {
            try {
                UserSession.userData = Gson().fromJson(userDataJson, UserData::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        viewModelScope.launch {
            val hasSession = UserSession.refreshToken.isNotEmpty() && UserSession.userData != null
            if (hasSession) {
                _uiState.update { it.copy(destination = SplashDestination.Main) }
            } else {
                _uiState.update { it.copy(destination = SplashDestination.Login) }
            }
        }
    }

    init {
        setupAndNavigate()
    }
}