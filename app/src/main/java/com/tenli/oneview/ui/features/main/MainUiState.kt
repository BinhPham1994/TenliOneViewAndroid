package com.tenli.oneview.ui.features.main

import androidx.compose.runtime.Immutable

@Immutable
data class MainUiState(
    val currentTab: MainTab = MainTab.Home,
    val isBottomBarVisible: Boolean = true,
    val isLoading: Boolean = false,
    val unreadEventCount: Int = 0,
    val isOffline: Boolean = false,
    val latestNotificationEvent: com.tenli.oneview.model.network.EventData? = null
)

enum class MainTab(val route: String) {
    Home("home"),
    Monitor("monitor"),
    Event("event"),
    Setting("setting")
}

sealed class MainEvent {
    object Logout : MainEvent()
    data class ShowToast(val message: String, val isError: Boolean = true) : MainEvent()
    data class OpenUrl(val url: String) : MainEvent()
    data class NewNotification(val event: com.tenli.oneview.model.network.EventData) : MainEvent()
}