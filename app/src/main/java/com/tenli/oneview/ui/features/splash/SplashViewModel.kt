package com.tenli.oneview.ui.features.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.tenli.oneview.data.local.GlobalData
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.model.network.UserData
import com.tenli.oneview.ui.utils.AppKeys
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

    fun navigateToMain() {
        _uiState.update { it.copy(destination = SplashDestination.Main) }
    }

    fun logout() {
        // Clear session data
        UserSession.accessToken = ""
        UserSession.refreshToken = ""
        UserSession.userData = null
        
        val prefs = GlobalData.preferences
        prefs.edit().apply {
            remove(AppKeys.ACCESS_TOKEN_KEY)
            remove(AppKeys.REFRESH_TOKEN_KEY)
            remove(AppKeys.USER_DATA_KEY)
            apply()
        }
        
        _uiState.update { it.copy(destination = SplashDestination.Login) }
    }

    init {
        setupAndNavigate()
    }
}