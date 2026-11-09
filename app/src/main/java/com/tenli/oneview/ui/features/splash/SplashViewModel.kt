package com.tenli.oneview.ui.features.splash

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tenli.oneview.TenliApp
import com.tenli.oneview.data.local.GlobalData
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.data.network.websocket.WebSocketManager
import com.tenli.oneview.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SplashViewModel(
    application: Application,
    private val authRepository: AuthRepository,
    private val webSocketManager: WebSocketManager
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SplashUiState(destination = resolveInitialDestination()))
    val uiState = _uiState.asStateFlow()

    /**
     * Xác định đích điều hướng ngay tức thì từ phiên đăng nhập đã được khôi phục trong [UserSession]
     * để tắt Splash screen ngay ở Frame 1 thay vì phải chờ nhảy qua IO thread rồi recompose lại.
     */
    private fun resolveInitialDestination(): SplashDestination {
        UserSession.ensureRestored(GlobalData.preferences)
        val hasSession = UserSession.refreshToken.isNotEmpty() && UserSession.userData != null
        return if (hasSession) SplashDestination.Main else SplashDestination.Login
    }

    fun navigateToMain() {
        _uiState.update { it.copy(destination = SplashDestination.Main) }
    }

    fun logout() {
        val needsCleanup = UserSession.accessToken.isNotEmpty() || UserSession.refreshToken.isNotEmpty()
        _uiState.update { it.copy(destination = SplashDestination.Login) }

        if (!needsCleanup) return

        // Dự phòng trường hợp gọi trực tiếp SplashViewModel.logout() (vd. hết hạn phiên 401) mà chưa chạy MainViewModel.logout()
        webSocketManager.disconnectAll()
        viewModelScope.launch(Dispatchers.IO) {
            authRepository.clearAuthenticatedSession()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as TenliApp
                SplashViewModel(
                    application = application,
                    authRepository = application.container.authRepository,
                    webSocketManager = application.container.webSocketManager
                )
            }
        }
    }
}
