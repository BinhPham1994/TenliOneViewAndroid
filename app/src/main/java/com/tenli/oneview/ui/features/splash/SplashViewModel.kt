package com.tenli.oneview.ui.features.splash

import android.app.Application
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tenli.oneview.data.local.EventCacheManager
import com.tenli.oneview.data.local.GlobalData
import com.tenli.oneview.data.local.HomeCacheManager
import com.tenli.oneview.data.local.MonitorCacheManager
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.ui.utils.AppKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState = _uiState.asStateFlow()

    init {
        restoreSessionAndNavigate()
    }

    /**
     * Đọc phiên đăng nhập đã lưu + nạp sẵn dữ liệu & ảnh cache của Trang chủ lên RAM trên IO thread
     * trước khi tắt Splash screen hệ thống để Frame 1 của Trang chủ hiển thị đầy đủ dữ liệu ngay lập tức.
     */
    private fun restoreSessionAndNavigate() {
        viewModelScope.launch {
            val app = getApplication<Application>()
            val hasSession = withContext(Dispatchers.IO) {
                UserSession.ensureRestored(GlobalData.preferences)
                val loggedIn = UserSession.refreshToken.isNotEmpty() && UserSession.userData != null
                if (loggedIn) {
                    coroutineScope {
                        launch { runCatching { HomeCacheManager.preloadDefaultCache(app) } }
                        launch { runCatching { EventCacheManager.preloadDefaultCache(app) } }
                        launch { runCatching { MonitorCacheManager.preloadDefaultCache(app) } }
                    }
                }
                loggedIn
            }
            _uiState.update {
                it.copy(destination = if (hasSession) SplashDestination.Main else SplashDestination.Login)
            }
        }
    }

    fun navigateToMain() {
        _uiState.update { it.copy(destination = SplashDestination.Main) }
    }

    fun logout() {
        UserSession.clear()

        val prefs = GlobalData.preferences
        val rememberMe = prefs.getBoolean(AppKeys.REMEMBER_ME_KEY, false)
        prefs.edit {
            remove(AppKeys.ACCESS_TOKEN_KEY)
            remove(AppKeys.REFRESH_TOKEN_KEY)
            remove(AppKeys.USER_DATA_KEY)
            remove(AppKeys.GROUP_ID)
            remove(AppKeys.GROUP_ID_CAMERA)
            if (!rememberMe) {
                remove(AppKeys.USERNAME_KEY)
                remove(AppKeys.PASSWORD_KEY)
            }
        }

        _uiState.update { it.copy(destination = SplashDestination.Login) }

        // Xoá cache (RAM + file) ngoài Main thread để chuyển màn không bị giật
        val app = getApplication<Application>()
        viewModelScope.launch(Dispatchers.IO) {
            HomeCacheManager.clearCache(app)
            EventCacheManager.clearCache(app)
            MonitorCacheManager.clearCache(app)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                SplashViewModel(application)
            }
        }
    }
}
