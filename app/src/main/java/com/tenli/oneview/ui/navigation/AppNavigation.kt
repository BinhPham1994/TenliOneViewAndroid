package com.tenli.oneview.ui.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tenli.oneview.ui.features.main.MainScreen

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavigation(
    onLogoutRequest: () -> Unit,
    initialEventId: String? = null
) {
    val navController = rememberNavController()

    SharedTransitionLayout {
        CompositionLocalProvider(
            LocalSharedTransitionScope provides this@SharedTransitionLayout
        ) {
            NavHost(navController = navController, startDestination = "main") {
                composable(
                    route = "main",
                    popEnterTransition = { EnterTransition.None }
                ) {
                    CompositionLocalProvider(LocalAnimatedVisibilityScope provides this@composable) {
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
                }
                
                composable(
                    route = "event_detail/{eventId}",
                    arguments = listOf(androidx.navigation.navArgument("eventId") { type = androidx.navigation.NavType.IntType }),
                    enterTransition = {
                        androidx.compose.animation.fadeIn(
                            animationSpec = androidx.compose.animation.core.tween(1000)
                        )
                    },
                    exitTransition = {
                        androidx.compose.animation.fadeOut(
                            animationSpec = androidx.compose.animation.core.tween(1000)
                        )
                    },
                    popEnterTransition = { EnterTransition.None },
                    popExitTransition = {
                        androidx.compose.animation.fadeOut(
                            animationSpec = androidx.compose.animation.core.tween(400)
                        )
                    }
                ) { backStackEntry ->
                    val eventId = backStackEntry.arguments?.getInt("eventId") ?: return@composable
                    CompositionLocalProvider(LocalAnimatedVisibilityScope provides this@composable) {
                        com.tenli.oneview.ui.features.event.detail.EventDetailScreen(
                            eventId = eventId,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
                
                composable(
                    route = "playback_detail/{videoLink}/{time}/{imageLink}/{cameraName}",
                    arguments = listOf(
                        androidx.navigation.navArgument("videoLink") { type = androidx.navigation.NavType.StringType },
                        androidx.navigation.navArgument("time") { type = androidx.navigation.NavType.StringType },
                        androidx.navigation.navArgument("imageLink") { type = androidx.navigation.NavType.StringType },
                        androidx.navigation.navArgument("cameraName") { type = androidx.navigation.NavType.StringType }
                    ),
                    enterTransition = {
                        androidx.compose.animation.fadeIn(
                            animationSpec = androidx.compose.animation.core.tween(1000)
                        )
                    },
                    exitTransition = {
                        androidx.compose.animation.fadeOut(
                            animationSpec = androidx.compose.animation.core.tween(300)
                        )
                    },
                    popEnterTransition = { EnterTransition.None },
                    popExitTransition = {
                        androidx.compose.animation.fadeOut(
                            animationSpec = androidx.compose.animation.core.tween(400)
                        )
                    }
                ) { backStackEntry ->
                    val flags = android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP or android.util.Base64.NO_PADDING
                    val vLink = String(android.util.Base64.decode(backStackEntry.arguments?.getString("videoLink") ?: "", flags)).trim()
                    val pTime = String(android.util.Base64.decode(backStackEntry.arguments?.getString("time") ?: "", flags)).trim()
                    val iLink = String(android.util.Base64.decode(backStackEntry.arguments?.getString("imageLink") ?: "", flags)).trim()
                    val cName = String(android.util.Base64.decode(backStackEntry.arguments?.getString("cameraName") ?: "", flags)).trim()
                    CompositionLocalProvider(LocalAnimatedVisibilityScope provides this@composable) {
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
        }
    }
}