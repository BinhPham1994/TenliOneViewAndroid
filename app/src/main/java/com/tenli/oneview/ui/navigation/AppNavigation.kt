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
                onLogoutRequest = onLogoutRequest,
                onNavigateToEventDetail = { eventId ->
                    navController.navigate("event_detail/$eventId")
                },
                onNavigateToPlaybackDetail = { videoLink, time, imageLink, cameraName ->
                    val flags = android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP or android.util.Base64.NO_PADDING
                    val vLinkEnc = android.util.Base64.encodeToString(videoLink.ifEmpty { " " }.toByteArray(), flags)
                    val pTimeEnc = android.util.Base64.encodeToString(time.ifEmpty { " " }.toByteArray(), flags)
                    val iLinkEnc = android.util.Base64.encodeToString(imageLink.ifEmpty { " " }.toByteArray(), flags)
                    val cNameEnc = android.util.Base64.encodeToString(cameraName.ifEmpty { " " }.toByteArray(), flags)
                    navController.navigate("playback_detail/$vLinkEnc/$pTimeEnc/$iLinkEnc/$cNameEnc")
                }
            )
        }
        
        composable(
            route = "event_detail/{eventId}",
            arguments = listOf(androidx.navigation.navArgument("eventId") { type = androidx.navigation.NavType.IntType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getInt("eventId") ?: return@composable
            com.tenli.oneview.ui.features.event.detail.EventDetailScreen(
                eventId = eventId,
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = "playback_detail/{videoLink}/{time}/{imageLink}/{cameraName}",
            arguments = listOf(
                androidx.navigation.navArgument("videoLink") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("time") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("imageLink") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("cameraName") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val flags = android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP or android.util.Base64.NO_PADDING
            val vLink = String(android.util.Base64.decode(backStackEntry.arguments?.getString("videoLink") ?: "", flags)).trim()
            val pTime = String(android.util.Base64.decode(backStackEntry.arguments?.getString("time") ?: "", flags)).trim()
            val iLink = String(android.util.Base64.decode(backStackEntry.arguments?.getString("imageLink") ?: "", flags)).trim()
            val cName = String(android.util.Base64.decode(backStackEntry.arguments?.getString("cameraName") ?: "", flags)).trim()
            com.tenli.oneview.ui.features.monitor.detail.PlaybackDetailScreen(
                videoLink = vLink,
                time = pTime,
                imageLink = iLink,
                cameraName = cName,
                onBack = { navController.popBackStack() }
            )
        }
    }
}