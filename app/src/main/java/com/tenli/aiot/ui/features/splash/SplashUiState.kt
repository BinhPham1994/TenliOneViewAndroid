package com.tenli.aiot.ui.features.splash

sealed class SplashDestination {
    object Loading : SplashDestination()
    object Main : SplashDestination()
    object Login : SplashDestination()
}

data class SplashUiState(
    val destination: SplashDestination = SplashDestination.Loading
)