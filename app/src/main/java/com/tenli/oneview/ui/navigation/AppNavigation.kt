package com.tenli.oneview.ui.navigation

import android.app.Application
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tenli.oneview.ui.features.event.detail.EventDetailScreen
import com.tenli.oneview.ui.features.main.MainScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

/**
 * Thời gian chờ sau khi giao diện chính (MainScreen) đã resume và vẽ xong frame đầu tiên
 * trước khi trượt màn Chi tiết sự kiện lên từ thông báo:
 * - Cold start (mở mới từ trạng thái tắt app): chờ 1100ms để hiệu ứng đóng splash + mở cửa sổ hoàn tất và màn chính hiển thị ổn định.
 * - Warm start (app đang chạy nền / kéo thanh thông báo bấm vào): chờ 650ms để thanh thông báo thu gọn & cửa sổ app ổn định trước khi mở chi tiết.
 */
private const val COLD_START_EVENT_DETAIL_DELAY_MS = 1100L
private const val WARM_START_EVENT_DETAIL_DELAY_MS = 650L
private const val EVENT_DETAIL_ENTER_DURATION_MS = 260
private const val EVENT_DETAIL_EXIT_DURATION_MS = 230
private val EventDetailEnterEasing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
private val EventDetailExitEasing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)

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
    val lifecycleOwner = LocalLifecycleOwner.current
    var activeEventDetailId by rememberSaveable { mutableStateOf<Int?>(null) }
    var lastEventDetailId by rememberSaveable { mutableStateOf<Int?>(null) }
    var skipExitAnimation by remember { mutableStateOf(false) }

    // Trượt mở/đóng trực tiếp trên GPU RenderThread (0 layout invalidation mỗi frame, không bị trễ PreEnter 2 frame của AnimatedVisibility)
    val slideFraction = remember { Animatable(if (activeEventDetailId != null) 0f else 1f) }

    // Trì hoãn thông báo "đã che phủ" xuống MainScreen cho tới khi hiệu ứng trượt lên hoàn tất,
    // tránh việc MainScreen recompose + giải phóng MediaCodec ngay ở Frame 0 gây khựng khi vừa bấm vào sự kiện.
    var isMainScreenCovered by remember { mutableStateOf(activeEventDetailId != null) }
    LaunchedEffect(activeEventDetailId != null) {
        if (activeEventDetailId != null) {
            delay(EVENT_DETAIL_ENTER_DURATION_MS.toLong() + 16L)
            isMainScreenCovered = true
        } else {
            isMainScreenCovered = false
        }
    }

    var animatedForEventId by remember { mutableStateOf(activeEventDetailId) }
    LaunchedEffect(activeEventDetailId) {
        val targetId = activeEventDetailId
        if (targetId != null) {
            lastEventDetailId = targetId
            if (animatedForEventId != targetId) {
                animatedForEventId = targetId
                slideFraction.snapTo(1f)
            }
            if (slideFraction.value > 0f) {
                slideFraction.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = EVENT_DETAIL_ENTER_DURATION_MS,
                        easing = EventDetailEnterEasing
                    )
                )
            }
        } else if (lastEventDetailId != null) {
            animatedForEventId = null
            if (skipExitAnimation) {
                slideFraction.snapTo(1f)
            } else {
                slideFraction.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = EVENT_DETAIL_EXIT_DURATION_MS,
                        easing = EventDetailExitEasing
                    )
                )
            }
            if (activeEventDetailId == null) {
                lastEventDetailId = null
            }
        }
    }

    LaunchedEffect(pendingEventNav) {
        val nav = pendingEventNav ?: return@LaunchedEffect
        val parsed = nav.eventId.toIntOrNull()
        if (parsed != null) {
            // 1. Đảm bảo Activity đã ở trạng thái RESUMED và MainScreen đã vẽ xong frame đầu tiên
            lifecycleOwner.lifecycle.currentStateFlow.first { it.isAtLeast(Lifecycle.State.RESUMED) }
            withFrameNanos { }
            // 2. Chờ cửa sổ app và màn hình chính hiển thị ổn định rồi mới trượt màn Chi tiết sự kiện lên
            delay(
                if (nav.isColdStart) {
                    COLD_START_EVENT_DETAIL_DELAY_MS
                } else {
                    WARM_START_EVENT_DETAIL_DELAY_MS
                }
            )
            skipExitAnimation = false
            lastEventDetailId = parsed
            activeEventDetailId = parsed
        }
        onEventNavConsumed()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = "main") {
            composable(
                route = "main",
                popEnterTransition = { EnterTransition.None }
            ) {
                CompositionLocalProvider(LocalAnimatedVisibilityScope provides this@composable) {
                    MainScreen(
                        onLogoutRequest = onLogoutRequest,
                        isOverlayCovered = isMainScreenCovered,
                        onNavigateToEventDetail = { eventId ->
                            skipExitAnimation = false
                            lastEventDetailId = eventId
                            activeEventDetailId = eventId
                        }
                    )
                }
            }
        }

        // Overlay EventDetailScreen directly on top of MainScreen so the previous screen
        // remains rendered and visible underneath when swiping down to dismiss.
        val displayEventId = activeEventDetailId ?: lastEventDetailId
        if (displayEventId != null) {
            val parentStoreOwner = LocalViewModelStoreOwner.current
            val application = LocalContext.current.applicationContext as Application
            val eventDetailStoreOwner = remember(displayEventId) {
                object : ViewModelStoreOwner, HasDefaultViewModelProviderFactory {
                    override val viewModelStore: ViewModelStore = ViewModelStore()
                    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
                        get() = (parentStoreOwner as? HasDefaultViewModelProviderFactory)
                            ?.defaultViewModelProviderFactory
                            ?: ViewModelProvider.AndroidViewModelFactory.getInstance(application)
                    override val defaultViewModelCreationExtras: CreationExtras
                        get() = (parentStoreOwner as? HasDefaultViewModelProviderFactory)
                            ?.defaultViewModelCreationExtras
                            ?: MutableCreationExtras().apply {
                                set(ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY, application)
                            }
                }
            }
            DisposableEffect(eventDetailStoreOwner) {
                onDispose {
                    eventDetailStoreOwner.viewModelStore.clear()
                }
            }
            val eventDetailOverlayHostState = remember(displayEventId) {
                com.tenli.oneview.ui.component.MobileOverlayHostState()
            }
            CompositionLocalProvider(
                LocalViewModelStoreOwner provides eventDetailStoreOwner,
                com.tenli.oneview.ui.component.LocalMobileOverlayHost provides eventDetailOverlayHostState
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationY = slideFraction.value * size.height
                        }
                ) {
                    EventDetailScreen(
                        eventId = displayEventId,
                        isClosing = activeEventDetailId == null,
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

        // Global Top Popup Notification Host above both MainScreen and EventDetailScreen
        com.tenli.oneview.ui.component.MobileTopPopupHost(
            modifier = Modifier.align(androidx.compose.ui.Alignment.TopCenter)
        )
    }
}