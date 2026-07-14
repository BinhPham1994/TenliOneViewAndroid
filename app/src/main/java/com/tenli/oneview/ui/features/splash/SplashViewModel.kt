package com.tenli.oneview.ui.features.splash

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.gson.Gson
import com.tenli.oneview.data.local.GlobalData
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.model.network.UserData
import com.tenli.oneview.ui.utils.AppKeys
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SplashViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState = _uiState.asStateFlow()

    private fun setupAndNavigate() {
        val prefs = GlobalData.preferences
        UserSession.accessToken = prefs.getString(AppKeys.ACCESS_TOKEN_KEY, "").orEmpty()
        UserSession.refreshToken = prefs.getString(AppKeys.REFRESH_TOKEN_KEY, "").orEmpty()
        UserSession.domain = prefs.getString("DOMAIN_KEY", "").orEmpty()
        UserSession.savedUsername = prefs.getString(AppKeys.USERNAME_KEY, "").orEmpty()
        UserSession.savedPassword = prefs.getString(AppKeys.PASSWORD_KEY, "").orEmpty()
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
        UserSession.clear()
        
        val prefs = GlobalData.preferences
        prefs.edit().apply {
            remove(AppKeys.ACCESS_TOKEN_KEY)
            remove(AppKeys.REFRESH_TOKEN_KEY)
            remove(AppKeys.USER_DATA_KEY)
            remove(AppKeys.USERNAME_KEY)
            remove(AppKeys.PASSWORD_KEY)
            remove("DOMAIN_KEY")
            apply()
        }
        
        // Clear all local caches
        com.tenli.oneview.data.local.HomeCacheManager.clearCache(getApplication())
        com.tenli.oneview.data.local.EventCacheManager.clearCache(getApplication())
        com.tenli.oneview.data.local.MonitorCacheManager.clearCache(getApplication())
        
        _uiState.update { it.copy(destination = SplashDestination.Login) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                SplashViewModel(application)
            }
        }
    }

    init {
        setupAndNavigate()
    }
}