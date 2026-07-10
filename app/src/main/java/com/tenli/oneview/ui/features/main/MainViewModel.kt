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
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay

@OptIn(FlowPreview::class)
class MainViewModel(
    application: Application,
    private val networkMonitor: com.tenli.oneview.util.NetworkMonitor,
    private val webSocketManager: com.tenli.oneview.data.network.websocket.WebSocketManager,
    private val eventApi: com.tenli.oneview.data.network.api.EventApi
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<MainEvent>()
    val event = _event.asSharedFlow()

    // Dedup: track shown event IDs to prevent duplicate popups (max 100)
    private val shownEventIds = LinkedHashSet<Int>()

    init {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                _uiState.update { it.copy(isOffline = !isOnline) }
            }
        }
        
        viewModelScope.launch {
            webSocketManager.notifyEvent
                .debounce(600L) // Wait 600ms before fetching
                .collect { _ ->
                    fetchLatestEvents()
                }
        }
    }

    private fun fetchLatestEvents() {
        viewModelScope.launch {
            try {
                // Fetch the latest 5 events (matching Web logic)
                val response = eventApi.getDataList(count = 5)
                if (response.isSuccessful) {
                    val events = response.body() ?: emptyList()
                    
                    // Process from oldest to newest (matching Web: for i = length-1 downTo 0)
                    for (event in events.reversed()) {
                        // Dedup: skip if already shown
                        if (shownEventIds.contains(event.id)) continue

                        // Age check: skip events older than 30 seconds
                        val eventTimeSec = if (event.time < 100000000000.0) event.time else event.time / 1000.0
                        val nowSec = System.currentTimeMillis() / 1000.0
                        if (nowSec - eventTimeSec > 30) continue

                        // Register as shown
                        shownEventIds.add(event.id)
                        if (shownEventIds.size > 100) {
                            shownEventIds.remove(shownEventIds.first())
                        }

                        // Emit and show popup
                        _event.emit(MainEvent.NewNotification(event))
                        _uiState.update { it.copy(latestNotificationEvent = event) }
                        
                        // Clear the notification after 5 seconds
                        delay(5000L)
                        _uiState.update { 
                            if (it.latestNotificationEvent == event) {
                                it.copy(latestNotificationEvent = null)
                            } else {
                                it
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore fetch errors for in-app notification
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

            // Disconnect websockets
            val app = getApplication<com.tenli.oneview.TenliApp>()
            app.container.webSocketManager.disconnectAll()

            _event.emit(MainEvent.Logout)
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as com.tenli.oneview.TenliApp
                MainViewModel(
                    application, 
                    application.container.networkMonitor,
                    application.container.webSocketManager,
                    com.tenli.oneview.data.network.retrofit.LoginAuthClient.create(com.tenli.oneview.data.network.api.EventApi::class.java)
                )
            }
        }
    }
}