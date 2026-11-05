package com.tenli.oneview.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tenli.oneview.data.local.EventCacheManager
import com.tenli.oneview.data.network.ApiProvider
import com.tenli.oneview.ui.features.event.detail.EventDetailScreen
import com.tenli.oneview.ui.features.main.MainScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Thời gian chờ sau khi frame đầu tiên của Trang chủ đã vẽ xong ở lần mở app mới (cold start)
 * (bao gồm thời gian hiệu ứng mở cửa sổ của hệ thống ~350ms + khoảng nghỉ ~600ms để mắt người dùng
 * nhìn rõ Trang chủ ổn định rồi mới trượt màn Chi tiết sự kiện lên).
 */
private const val COLD_START_EVENT_DETAIL_DELAY_MS = 950L

@Immutable
data class PendingEventNav(
    val eventId: String,
    val isColdStart: Boolean,
    val triggerId: Long = System.nanoTime()
)

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavigation(
    onLogoutRequest: () -> Unit,
    pendingEventNav: PendingEventNav? = null,
    onEventNavConsumed: () -> Unit = {}
) {
    val navController = rememberNavController()
    var activeEventDetailId by rememberSaveable { mutableStateOf<Int?>(null) }
    var lastEventDetailId by rememberSaveable { mutableStateOf<Int?>(null) }
    var skipExitAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(pendingEventNav) {
        val nav = pendingEventNav ?: return@LaunchedEffect
        val parsed = nav.eventId.toIntOrNull()
        if (parsed != null) {
            if (nav.isColdStart) {
                // Trong lúc chờ hiển thị Trang chủ, tải sẵn dữ liệu chi tiết sự kiện vào RAM ở nền để khi trượt lên có dữ liệu ngay
                launch(Dispatchers.IO) {
                    if (EventCacheManager.getEventInMemory(parsed) == null) {
                        val response = runCatching {
                            ApiProvider.eventApi.getDataList(lastId = parsed + 1, count = 1)
                        }.getOrNull()
                        val first = response?.body()?.firstOrNull()
                        if (first != null && first.id == parsed) {
                            EventCacheManager.cacheInMemory(events = listOf(first))
                        }
                    }
                }
                // Đợi frame đầu tiên của Trang chủ thực sự vẽ lên màn hình rồi mới bắt đầu đếm nhịp nghỉ
                withFrameNanos { }
                delay(COLD_START_EVENT_DETAIL_DELAY_MS)
            }
            skipExitAnimation = false
            lastEventDetailId = parsed
            activeEventDetailId = parsed
        }
        onEventNavConsumed()
    }

    SharedTransitionLayout {
        CompositionLocalProvider(
            LocalSharedTransitionScope provides this@SharedTransitionLayout
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                NavHost(
                    navController = navController,
                    startDestination = "main",
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None },
                    popEnterTransition = { EnterTransition.None },
                    popExitTransition = { ExitTransition.None }
                ) {
                    composable(
                        route = "main",
                        enterTransition = { EnterTransition.None },
                        exitTransition = { ExitTransition.None },
                        popEnterTransition = { EnterTransition.None },
                        popExitTransition = { ExitTransition.None }
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
                        val eventDetailOverlayHostState = remember(displayEventId) {
                            com.tenli.oneview.ui.component.MobileOverlayHostState()
                        }
                        CompositionLocalProvider(
                            LocalAnimatedVisibilityScope provides this@AnimatedVisibility,
                            com.tenli.oneview.ui.component.LocalMobileOverlayHost provides eventDetailOverlayHostState
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
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
                                if (eventDetailOverlayHostState.hasOverlays) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        eventDetailOverlayHostState.Content()
                                    }
                                }
                            }
                        }
                    }
                }

                // Global Top Popup Notification Host above both MainScreen and EventDetailScreen
                com.tenli.oneview.ui.component.MobileTopPopupHost(
                    modifier = Modifier.align(androidx.compose.ui.Alignment.TopCenter)
                )
            }
        }
    }
}