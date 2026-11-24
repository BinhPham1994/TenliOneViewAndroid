package com.tenli.oneview.ui.features.splash

import androidx.compose.runtime.Immutable

@Immutable
sealed class SplashDestination {
    object Loading : SplashDestination()
    object Main : SplashDestination()
    object Login : SplashDestination()
}

@Immutable
data class SplashUiState(
    val destination: SplashDestination = SplashDestination.Loading
)