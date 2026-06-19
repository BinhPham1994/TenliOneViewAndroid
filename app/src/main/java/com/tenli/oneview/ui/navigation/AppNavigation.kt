package com.tenli.oneview.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tenli.oneview.ui.features.main.MainScreen

@Composable
fun AppNavigation(
    onLogoutRequest: () -> Unit,
    initialEventId: String? = null
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable(
            route = "main",
            popEnterTransition = { EnterTransition.None }
        ) {
            MainScreen(
                onLogoutRequest = onLogoutRequest
            )
        }
    }
}