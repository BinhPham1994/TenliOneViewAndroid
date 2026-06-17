package com.tenli.oneview.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tenli.oneview.ui.features.event.component.EventDetailScreen
import com.tenli.oneview.ui.features.main.MainScreen

@Composable
fun AppNavigation(
    onLogoutRequest: () -> Unit,
    initialEventId: String? = null
) {
    val navController = rememberNavController()

    LaunchedEffect(initialEventId) {
        if (!initialEventId.isNullOrEmpty()) {
            val idAsLong = initialEventId.toLongOrNull()
            if (idAsLong != null) {
                navController.navigate("event_detail/$idAsLong") {
                    popUpTo("main") { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }

    NavHost(navController = navController, startDestination = "main") {
        composable(
            route = "main",
            popEnterTransition = { EnterTransition.None }
        ) {
            MainScreen(
                onLogoutRequest = onLogoutRequest,
                onEventClick = { event ->
                    navController.navigate("event_detail/${event.id}")
                })
        }
        composable(
            route = "event_detail/{eventId}",
            arguments = listOf(navArgument("eventId") { type = NavType.LongType }),
            popExitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None }
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getLong("eventId") ?: -1L
            EventDetailScreen(
                eventId = eventId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}