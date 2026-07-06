package com.tenli.oneview.ui.features.main

import android.app.Application
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tenli.oneview.data.local.GlobalData
import com.tenli.oneview.data.local.UserSession
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    application: Application,
    private val networkMonitor: com.tenli.oneview.util.NetworkMonitor
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<MainEvent>()
    val event = _event.asSharedFlow()

    init {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                _uiState.update { it.copy(isOffline = !isOnline) }
            }
        }
    }

    fun onTabSelected(tab: MainTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun setBottomBarVisibility(isVisible: Boolean) {
        _uiState.update { it.copy(isBottomBarVisible = isVisible) }
    }

    fun logout(isChangePassword: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            UserSession.clear()
            GlobalData.preferences.edit { clear() }
            
            // Clear all local caches
            com.tenli.oneview.data.local.HomeCacheManager.clearCache(getApplication())
            com.tenli.oneview.data.local.EventCacheManager.clearCache(getApplication())
            com.tenli.oneview.data.local.MonitorCacheManager.clearCache(getApplication())

            _event.emit(MainEvent.Logout)
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as com.tenli.oneview.TenliApp
                MainViewModel(application, application.container.networkMonitor)
            }
        }
    }
}