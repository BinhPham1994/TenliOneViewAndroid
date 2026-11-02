package com.tenli.oneview.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tenli.oneview.ui.features.event.detail.EventDetailScreen
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
    var activeEventDetailId by rememberSaveable { mutableStateOf<Int?>(null) }
    var lastEventDetailId by rememberSaveable { mutableStateOf<Int?>(null) }
    var skipExitAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(initialEventId) {
        val parsed = initialEventId?.toIntOrNull()
        if (parsed != null) {
            skipExitAnimation = false
            lastEventDetailId = parsed
            activeEventDetailId = parsed
        }
    }

    SharedTransitionLayout {
        CompositionLocalProvider(
            LocalSharedTransitionScope provides this@SharedTransitionLayout
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                NavHost(navController = navController, startDestination = "main") {
                    composable(
                        route = "main",
                        popEnterTransition = { EnterTransition.None }
                    ) {
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this@composable) {
                            MainScreen(
                                onLogoutRequest = onLogoutRequest,
                                onNavigateToEventDetail = { eventId ->
                                    skipExitAnimation = false
                                    lastEventDetailId = eventId
                                    activeEventDetailId = eventId
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
                        route = "playback_detail/{videoLink}/{time}/{imageLink}/{cameraName}",
                        arguments = listOf(
                            androidx.navigation.navArgument("videoLink") { type = androidx.navigation.NavType.StringType },
                            androidx.navigation.navArgument("time") { type = androidx.navigation.NavType.StringType },
                            androidx.navigation.navArgument("imageLink") { type = androidx.navigation.NavType.StringType },
                            androidx.navigation.navArgument("cameraName") { type = androidx.navigation.NavType.StringType }
                        ),
                        enterTransition = {
                            androidx.compose.animation.fadeIn(
                                animationSpec = tween(1000)
                            )
                        },
                        exitTransition = {
                            androidx.compose.animation.fadeOut(
                                animationSpec = tween(300)
                            )
                        },
                        popEnterTransition = { EnterTransition.None },
                        popExitTransition = {
                            androidx.compose.animation.fadeOut(
                                animationSpec = tween(400)
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

                // Overlay EventDetailScreen directly on top of MainScreen so the previous screen
                // remains rendered and visible underneath when swiping down to dismiss.
                AnimatedVisibility(
                    visible = activeEventDetailId != null,
                    enter = slideInVertically(
                        initialOffsetY = { fullHeight -> fullHeight },
                        animationSpec = tween(
                            durationMillis = 420,
                            easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
                        )
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { fullHeight -> fullHeight },
                        animationSpec = tween(
                            durationMillis = if (skipExitAnimation) 1 else 360,
                            easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
                        )
                    )
                ) {
                    val displayEventId = activeEventDetailId ?: lastEventDetailId
                    if (displayEventId != null) {
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this@AnimatedVisibility) {
                            EventDetailScreen(
                                eventId = displayEventId,
                                onBack = {
                                    skipExitAnimation = false
                                    activeEventDetailId = null
                                },
                                onSwipeDismissed = {
                                    skipExitAnimation = true
                                    activeEventDetailId = null
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}