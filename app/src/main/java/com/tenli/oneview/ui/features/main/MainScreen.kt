package com.tenli.oneview.ui.features.main

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tenli.oneview.BuildConfig
import com.tenli.oneview.R
import com.tenli.oneview.data.local.UserPermissionManager
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.ui.component.IconBadge
import com.tenli.oneview.ui.component.LocalMobileOverlayHost
import com.tenli.oneview.ui.component.MobileNativeBottomSheet
import com.tenli.oneview.ui.component.MobileOverlayHostState
import com.tenli.oneview.ui.component.MobileTopBar
import com.tenli.oneview.ui.component.SheetActionButtons
import com.tenli.oneview.ui.component.rememberIsEnglish
import com.tenli.oneview.ui.features.event.EventScreen
import com.tenli.oneview.ui.features.event.EventViewModel
import com.tenli.oneview.ui.features.home.HomeScreen
import com.tenli.oneview.ui.features.home.HomeViewModel
import com.tenli.oneview.ui.features.monitor.MonitorLiveCameraOverlay
import com.tenli.oneview.ui.features.monitor.MonitorScreen
import com.tenli.oneview.ui.features.monitor.MonitorViewModel
import com.tenli.oneview.ui.theme.LucideIcons
import com.tenli.oneview.ui.theme.Slate300
import com.tenli.oneview.ui.theme.Slate500
import com.tenli.oneview.ui.theme.ThemeManager
import com.tenli.oneview.ui.theme.appColors
import com.tenli.oneview.ui.theme.spacing
import com.tenli.oneview.ui.utils.AiTypeHelper
import com.tenli.oneview.ui.utils.LocaleManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

private val OrderedMainTabs = listOf(
    MainTab.Home,
    MainTab.Event,
    MainTab.Monitor,
    MainTab.Setting
)

// --- Animation (cubic-bezier khớp tenli-oneview MobileLayout.vue / MobileNotificationConfigModal.vue) ---
private const val TAB_SWITCH_DURATION_MS = 250
private val TabSwitchEasing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
private val TabSwitchAnimSpec = tween<Float>(durationMillis = TAB_SWITCH_DURATION_MS, easing = TabSwitchEasing)
private const val PAGE_ENTER_DURATION_MS = 280
private const val PAGE_EXIT_DURATION_MS = 240
private val PageEnterEasing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
private val PageExitEasing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)

// --- Ngưỡng cử chỉ vuốt chuyển tab (khớp MobileLayout.vue) ---
/** Bỏ qua chạm sát mép trái/phải để nhường cử chỉ Back hệ thống */
private val SwipeEdgeBuffer = 20.dp
/** Bỏ qua vùng thanh điều hướng dưới cùng */
private val SwipeBottomNavExclusion = 96.dp
/** Khoảng di chuyển tối thiểu để khóa hướng (ngang/dọc) */
private val SwipeDirectionLockThreshold = 8.dp
private val SwipeMinFlickDistance = 26.dp
private const val SWIPE_MIN_FLICK_VELOCITY_DP_PER_MS = 0.25f
private const val SWIPE_HORIZONTAL_DOMINANCE = 1.2f

// --- Cuộn về đầu danh sách ---
private const val SCROLL_TO_TOP_JUMP_THRESHOLD = 12
private const val SCROLL_TO_TOP_JUMP_TARGET = 8

private const val ERROR_BOX_BG_ALPHA = 0.12f
private const val ERROR_BOX_BORDER_ALPHA = 0.25f

/** Chờ sheet đổi mật khẩu hiển thị xong mới focus + bật bàn phím (đồng bộ với LoginScreen) */
private const val PASSWORD_SHEET_FOCUS_DELAY_MS = 700L
private const val KEYBOARD_SHOW_DELAY_MS = 100L

private const val SUPPORT_URL = "https://tenli.ai"

private val PasswordMask = PasswordVisualTransformation()

private data class LanguageOption(val code: String, val labelRes: Int, val flag: String)

private val LanguageOptions = listOf(
    LanguageOption("vi", R.string.lang_vi, "🇻🇳"),
    LanguageOption("en", R.string.lang_en, "🇺🇸")
)

private val ThemeModes = listOf(ThemeManager.MODE_LIGHT, ThemeManager.MODE_DARK, ThemeManager.MODE_SYSTEM)

private fun themeModeLabel(mode: Int, isEn: Boolean): String = when (mode) {
    ThemeManager.MODE_LIGHT -> if (isEn) "Light" else "Sáng"
    ThemeManager.MODE_DARK -> if (isEn) "Dark" else "Tối"
    else -> if (isEn) "System" else "Hệ thống"
}

@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel(factory = MainViewModel.Factory),
    onLogoutRequest: () -> Unit,
    isOverlayCovered: Boolean = false,
    onNavigateToEventDetail: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val permissionState by UserPermissionManager.permissionState.collectAsStateWithLifecycle()

    val visibleMainTabs = remember(permissionState.canAccessEvents, permissionState.canAccessLiveStream) {
        buildList {
            add(MainTab.Home)
            if (permissionState.canAccessEvents) add(MainTab.Event)
            if (permissionState.canAccessLiveStream) add(MainTab.Monitor)
            add(MainTab.Setting)
        }
    }
    val visibleMainTabsState by rememberUpdatedState(visibleMainTabs)

    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val saveableStateHolder = rememberSaveableStateHolder()
    var showBottomBar by remember { mutableStateOf(true) }
    var showNotificationConfigPage by remember { mutableStateOf(false) }
    var showAboutPage by remember { mutableStateOf(false) }
    var isLiveCameraPageOpen by remember { mutableStateOf(false) }
    val overlayHostState = remember { MobileOverlayHostState() }

    LaunchedEffect(visibleMainTabs, uiState.currentTab) {
        if (uiState.currentTab !in visibleMainTabs) {
            viewModel.onTabSelected(MainTab.Home)
        }
    }

    val homeListState = rememberLazyListState()
    val monitorGridState: LazyGridState = rememberLazyGridState()
    val eventListState = rememberLazyListState()
    val settingListState = rememberLazyListState()

    // Keep-Alive (<keep-alive>) state + simultaneous edge-to-edge GPU slide animation.
    // Chỉ khởi tạo tab khi người dùng thực sự chuyển tới tab đó lần đầu (dữ liệu cache đã được pre-warm vào RAM
    // từ TenliApp.onCreate nên mở tab lần đầu vẫn đạt 0ms blank flash mà không tốn mạng/pin chạy ngầm).
    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
    var mountedTabs by remember { mutableStateOf(setOf(uiState.currentTab)) }

    var activeTab by remember { mutableStateOf(uiState.currentTab) }
    var outgoingTab by remember { mutableStateOf<MainTab?>(null) }
    var partnerSide by remember { mutableIntStateOf(1) }
    var isDraggingTab by remember { mutableStateOf(false) }
    var dragOffsetFraction by remember { mutableFloatStateOf(0f) }
    val activeOffsetFraction = remember { Animatable(0f) }

    LaunchedEffect(uiState.currentTab, visibleMainTabs) {
        val targetTab = uiState.currentTab
        if (targetTab !in mountedTabs) {
            mountedTabs = mountedTabs + targetTab
        }
        if (targetTab != MainTab.Monitor) {
            showBottomBar = true
        }
        if (targetTab != MainTab.Setting) {
            showNotificationConfigPage = false
            showAboutPage = false
        }
        if (targetTab == MainTab.Home) {
            homeViewModel.syncViewModeFromPrefs()
        }

        // Chỉ chạy hiệu ứng chuyển tab tự động khi bấm thanh điều hướng dưới / nút Back (targetTab != activeTab).
        // Nếu người dùng vừa vuốt tay sang targetTab thì pointerInput đã tự khởi chạy animation settle ở ngay Frame 0.
        if (targetTab != activeTab) {
            val fromIdx = OrderedMainTabs.indexOf(activeTab)
            val toIdx = OrderedMainTabs.indexOf(targetTab)
            val dir = if (toIdx >= fromIdx) 1 else -1
            outgoingTab = activeTab
            activeTab = targetTab
            partnerSide = -dir
            isDraggingTab = false
            activeOffsetFraction.snapTo(dir.toFloat())
            activeOffsetFraction.animateTo(targetValue = 0f, animationSpec = TabSwitchAnimSpec)
            if (!isDraggingTab) {
                outgoingTab = null
            }
        }

        // Pre-mount & pre-measure toàn bộ các tab được phép truy cập theo 2 nhịp ngắn khi màn hình đang nghỉ,
        // đảm bảo khi người dùng vuốt ngang qua lại giữa bất kỳ tab nào đều đạt 0ms measure hitch & 0 recomposition.
        val currIdx = visibleMainTabs.indexOf(targetTab)
        if (currIdx != -1) {
            val neighbors = buildSet {
                visibleMainTabs.getOrNull(currIdx - 1)?.let { add(it) }
                visibleMainTabs.getOrNull(currIdx + 1)?.let { add(it) }
            }
            if (!mountedTabs.containsAll(neighbors)) {
                delay(100L)
                mountedTabs = mountedTabs + neighbors
            }
        }
        if (!mountedTabs.containsAll(visibleMainTabs)) {
            delay(140L)
            mountedTabs = mountedTabs + visibleMainTabs
        }
    }

    val currentOnLogoutRequest by rememberUpdatedState(onLogoutRequest)
    LaunchedEffect(viewModel) {
        viewModel.event.collect { event ->
            when (event) {
                MainEvent.Logout -> currentOnLogoutRequest()
            }
        }
    }

    val currentTabState by rememberUpdatedState(uiState.currentTab)
    val showBottomBarState by rememberUpdatedState(showBottomBar)
    val showNotificationConfigPageState by rememberUpdatedState(showNotificationConfigPage)
    val showAboutPageState by rememberUpdatedState(showAboutPage)
    val isLiveCameraPageOpenState by rememberUpdatedState(isLiveCameraPageOpen)

    val isScreenUncovered = !showNotificationConfigPage && !showAboutPage && !isOverlayCovered
    val isHomeActive = uiState.currentTab == MainTab.Home && isScreenUncovered
    val isEventActive = uiState.currentTab == MainTab.Event && isScreenUncovered
    val isMonitorActive = uiState.currentTab == MainTab.Monitor && isScreenUncovered

    LaunchedEffect(isHomeActive) {
        homeViewModel.setHomeTabActive(isHomeActive)
    }

    // Callback ổn định (không tạo lambda mới mỗi lần recompose -> các tab con được skip)
    val currentOnNavigateToEventDetail by rememberUpdatedState(onNavigateToEventDetail)
    val onEventIdClick: (Int) -> Unit = remember { { id: Int -> currentOnNavigateToEventDetail(id) } }
    val onHomeEventClick: (String) -> Unit = remember {
        { eventId: String -> eventId.toIntOrNull()?.let { currentOnNavigateToEventDetail(it) } }
    }
    val onMonitorFullscreenChange: (Boolean) -> Unit = remember { { isFullscreen: Boolean -> showBottomBar = !isFullscreen } }
    val onLiveCameraPageOpenChange: (Boolean) -> Unit = remember { { isOpen: Boolean -> isLiveCameraPageOpen = isOpen } }
    val onLogoutClick: () -> Unit = remember(viewModel) { { viewModel.logout() } }
    val onOpenNotificationConfig: () -> Unit = remember(viewModel) {
        {
            viewModel.loadNotificationConfig()
            showNotificationConfigPage = true
        }
    }
    val onCloseNotificationConfig: () -> Unit = remember { { showNotificationConfigPage = false } }
    val onOpenAbout: () -> Unit = remember { { showAboutPage = true } }
    val onCloseAbout: () -> Unit = remember { { showAboutPage = false } }

    val scrollTabToTop: (MainTab) -> Unit = remember(scope, haptic) {
        { targetTab: MainTab ->
            scope.launch {
                if (targetTab == MainTab.Monitor) {
                    if (monitorGridState.firstVisibleItemIndex > 0 || monitorGridState.firstVisibleItemScrollOffset > 0) {
                        try {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        } catch (_: Exception) {
                        }
                        if (monitorGridState.firstVisibleItemIndex > SCROLL_TO_TOP_JUMP_THRESHOLD) {
                            monitorGridState.scrollToItem(SCROLL_TO_TOP_JUMP_TARGET)
                        }
                        monitorGridState.animateScrollToItem(0, 0)
                    }
                    return@launch
                }

                val targetListState = when (targetTab) {
                    MainTab.Home -> homeListState
                    MainTab.Event -> eventListState
                    MainTab.Setting -> settingListState
                    MainTab.Monitor -> homeListState
                }
                if (targetListState.firstVisibleItemIndex > 0 || targetListState.firstVisibleItemScrollOffset > 0) {
                    try {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    } catch (_: Exception) {
                    }
                    if (targetListState.firstVisibleItemIndex > SCROLL_TO_TOP_JUMP_THRESHOLD) {
                        targetListState.scrollToItem(SCROLL_TO_TOP_JUMP_TARGET)
                    }
                    targetListState.animateScrollToItem(0, 0)
                }
            }
        }
    }

    val switchToTab: (MainTab, Boolean) -> Unit = remember(viewModel, haptic, scrollTabToTop) {
        { targetTab: MainTab, withHaptic: Boolean ->
            if (targetTab != currentTabState) {
                showBottomBar = true
                if (withHaptic) {
                    try {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    } catch (_: Exception) {
                    }
                }
                viewModel.onTabSelected(targetTab)
            } else {
                scrollTabToTop(targetTab)
            }
        }
    }
    val onBottomTabClick: (MainTab) -> Unit = remember(switchToTab) { { tab: MainTab -> switchToTab(tab, false) } }

    // Khi đang ở tab khác Trang chủ (Sự kiện, Trực tiếp, Cài đặt), ấn Back sẽ quay về tab Trang chủ trước
    BackHandler(
        enabled = uiState.currentTab != MainTab.Home &&
            showBottomBar &&
            !showNotificationConfigPage &&
            !showAboutPage &&
            !isLiveCameraPageOpen
    ) {
        switchToTab(MainTab.Home, false)
    }

    // Khi đang mở màn hình Cấu hình thông báo hoặc Giới thiệu, ấn Back sẽ trượt đóng màn hình này từ trái sang phải
    BackHandler(enabled = showNotificationConfigPage) {
        showNotificationConfigPage = false
    }
    BackHandler(enabled = showAboutPage) {
        showAboutPage = false
    }

    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing

    CompositionLocalProvider(LocalMobileOverlayHost provides overlayHostState) {
        Scaffold { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .pointerInput(Unit) {
                        val edgeBufferPx = SwipeEdgeBuffer.toPx()
                        val bottomNavExclusionPx = SwipeBottomNavExclusion.toPx()
                        val lockThresholdPx = SwipeDirectionLockThreshold.toPx()
                        val minFlickDistPx = SwipeMinFlickDistance.toPx()

                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                            val widthPx = size.width.toFloat()
                            val heightPx = size.height.toFloat()
                            val startX = down.position.x
                            val startY = down.position.y

                            // Ignore touches near screen left/right edges for system back gesture,
                            // or in the bottom navigation bar zone, or when video is in fullscreen
                            if (!showBottomBarState ||
                                showNotificationConfigPageState ||
                                showAboutPageState ||
                                isLiveCameraPageOpenState ||
                                overlayHostState.hasOverlays ||
                                startX < edgeBufferPx ||
                                startX > widthPx - edgeBufferPx ||
                                startY > heightPx - bottomNavExclusionPx
                            ) {
                                return@awaitEachGesture
                            }

                            val startTime = down.uptimeMillis
                            var endTime = startTime
                            var gestureDirection = 0 // 0 = undecided, 1 = horizontal, 2 = vertical
                            var lastX = startX
                            var lastY = startY
                            var dragAnchorX = startX
                            var recentX = startX
                            var recentTime = startTime
                            var prevRecentX = startX
                            var prevRecentTime = startTime

                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                endTime = change.uptimeMillis

                                if (change.changedToUpIgnoreConsumed()) {
                                    lastX = change.position.x
                                    lastY = change.position.y
                                    if (gestureDirection == 1) {
                                        change.consume()
                                    }
                                    break
                                }

                                if (!change.pressed) break

                                if (endTime - recentTime >= 40L) {
                                    prevRecentX = recentX
                                    prevRecentTime = recentTime
                                    recentX = lastX
                                    recentTime = endTime
                                }

                                lastX = change.position.x
                                lastY = change.position.y
                                val dx = lastX - startX
                                val dy = lastY - startY

                                if (gestureDirection == 0) {
                                    if (abs(dx) >= lockThresholdPx || abs(dy) >= lockThresholdPx) {
                                        if (abs(dy) >= abs(dx) || abs(dx) < abs(dy) * SWIPE_HORIZONTAL_DOMINANCE) {
                                            gestureDirection = 2
                                        } else {
                                            gestureDirection = 1
                                            val currentVisibleTabs = visibleMainTabsState
                                            val currIdx = currentVisibleTabs.indexOf(activeTab)
                                            val inFlightOffset =
                                                if (!isDraggingTab && outgoingTab != null) activeOffsetFraction.value else 0f
                                            val canCatchInFlight =
                                                (inFlightOffset > 0.002f && outgoingTab == currentVisibleTabs.getOrNull(currIdx - 1)) ||
                                                    (inFlightOffset < -0.002f && outgoingTab == currentVisibleTabs.getOrNull(currIdx + 1))
                                            dragAnchorX = if (canCatchInFlight) lastX - inFlightOffset * widthPx else lastX
                                            if (canCatchInFlight) {
                                                dragOffsetFraction = inFlightOffset
                                                isDraggingTab = true
                                                scope.launch { activeOffsetFraction.stop() }
                                            }
                                            recentX = lastX
                                            recentTime = endTime
                                            prevRecentX = lastX
                                            prevRecentTime = endTime
                                        }
                                    }
                                }

                                if (gestureDirection == 2) {
                                    // Locked to vertical scrolling -> let child LazyColumn handle it
                                    break
                                } else if (gestureDirection == 1) {
                                    // Consume horizontal drag & di chuyển 2 màn hình tab song song 1:1 theo ngón tay (kiểu Facebook)
                                    change.consume()
                                    val currentVisibleTabs = visibleMainTabsState
                                    val currIdx = currentVisibleTabs.indexOf(activeTab)
                                    val effectiveDx = lastX - dragAnchorX
                                    val rawFraction = if (widthPx > 0f) effectiveDx / widthPx else 0f

                                    if (rawFraction < 0f) {
                                        val nextTab = currentVisibleTabs.getOrNull(currIdx + 1)
                                        if (nextTab != null) {
                                            if (nextTab !in mountedTabs) mountedTabs = mountedTabs + nextTab
                                            outgoingTab = nextTab
                                            partnerSide = 1
                                            dragOffsetFraction = rawFraction.coerceIn(-1f, 0f)
                                            isDraggingTab = true
                                        } else {
                                            outgoingTab = null
                                            dragOffsetFraction = 0f
                                            isDraggingTab = false
                                        }
                                    } else if (rawFraction > 0f) {
                                        val prevTab = currentVisibleTabs.getOrNull(currIdx - 1)
                                        if (prevTab != null) {
                                            if (prevTab !in mountedTabs) mountedTabs = mountedTabs + prevTab
                                            outgoingTab = prevTab
                                            partnerSide = -1
                                            dragOffsetFraction = rawFraction.coerceIn(0f, 1f)
                                            isDraggingTab = true
                                        } else {
                                            outgoingTab = null
                                            dragOffsetFraction = 0f
                                            isDraggingTab = false
                                        }
                                    } else {
                                        dragOffsetFraction = 0f
                                    }
                                }
                            }

                            if (gestureDirection == 1 && isDraggingTab) {
                                val neighborTab = outgoingTab
                                val side = partnerSide
                                val draggedProgress = (-dragOffsetFraction * side).coerceIn(0f, 1f)
                                val usePrevWindow = (endTime - recentTime) < 20L && prevRecentTime < recentTime
                                val refX = if (usePrevWindow) prevRecentX else recentX
                                val refTime = if (usePrevWindow) prevRecentTime else recentTime
                                val dt = (endTime - refTime).coerceAtLeast(1L).toFloat()
                                val velocityX = (lastX - refX).toDp().value / dt
                                val velocityTowardPartner = -velocityX * side
                                val totalAbsX = abs(lastX - startX)

                                val isFlickTowardPartner =
                                    velocityTowardPartner >= SWIPE_MIN_FLICK_VELOCITY_DP_PER_MS &&
                                        totalAbsX >= minFlickDistPx &&
                                        draggedProgress >= 0.03f
                                val isFlickBack = velocityTowardPartner <= -SWIPE_MIN_FLICK_VELOCITY_DP_PER_MS
                                val isDraggedPastThreshold = !isFlickBack && draggedProgress >= 0.26f

                                if (neighborTab != null && (isFlickTowardPartner || isDraggedPastThreshold)) {
                                    val newStartOffset = (dragOffsetFraction + side).coerceIn(-1f, 1f)
                                    val settleDurationMs = (140 + (abs(newStartOffset) * 115f).toInt()).coerceIn(140, 250)
                                    outgoingTab = activeTab
                                    activeTab = neighborTab
                                    partnerSide = -side
                                    dragOffsetFraction = newStartOffset
                                    scope.launch {
                                        activeOffsetFraction.snapTo(newStartOffset)
                                        isDraggingTab = false
                                        activeOffsetFraction.animateTo(
                                            targetValue = 0f,
                                            animationSpec = tween(durationMillis = settleDurationMs, easing = TabSwitchEasing)
                                        )
                                        if (!isDraggingTab) {
                                            outgoingTab = null
                                        }
                                    }
                                    if (neighborTab != currentTabState) {
                                        switchToTab(neighborTab, true)
                                    }
                                } else {
                                    val startFrom = dragOffsetFraction
                                    val settleDurationMs = (130 + (abs(startFrom) * 120f).toInt()).coerceIn(130, 230)
                                    scope.launch {
                                        activeOffsetFraction.snapTo(startFrom)
                                        isDraggingTab = false
                                        activeOffsetFraction.animateTo(
                                            targetValue = 0f,
                                            animationSpec = tween(durationMillis = settleDurationMs, easing = TabSwitchEasing)
                                        )
                                        if (!isDraggingTab) {
                                            outgoingTab = null
                                        }
                                    }
                                }
                            }
                        }
                    }
            ) {
                val effectiveMountedTabs = remember(mountedTabs, uiState.currentTab) {
                    mountedTabs + uiState.currentTab
                }

                // Keep-Alive (<keep-alive>) Tab Container + Simultaneous Edge-to-Edge Slide Transition
                // Tất cả các tab được pre-measure sẵn 1 lần khi màn hình nghỉ; trong suốt lúc kéo tay và trượt:
                // - 0 recomposition
                // - 0 layout re-measurement
                // - Chỉ chạy khối placement nhẹ 120Hz (tab không hiển thị không được place => hoàn toàn không tốn GPU draw).
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding())
                        .zIndex(1f)
                ) {
                    OrderedMainTabs.forEach { tab ->
                        if (tab in effectiveMountedTabs) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .layout { measurable, constraints ->
                                        val placeable = measurable.measure(constraints)
                                        layout(placeable.width, placeable.height) {
                                            val currentActive = activeTab
                                            val currentOutgoing = outgoingTab
                                            if (tab == currentActive || tab == currentOutgoing) {
                                                val offsetFraction =
                                                    if (isDraggingTab) dragOffsetFraction else activeOffsetFraction.value
                                                val tabFraction = when (tab) {
                                                    currentActive -> if (currentOutgoing != null) offsetFraction else 0f
                                                    else -> offsetFraction + partnerSide
                                                }
                                                val offsetX = (tabFraction * placeable.width).toInt()
                                                val visibility = (1f - abs(tabFraction)).coerceIn(0f, 1f)
                                                val pageAlpha = visibility * visibility * (3f - 2f * visibility)
                                                placeable.placeRelativeWithLayer(
                                                    x = offsetX,
                                                    y = 0,
                                                    zIndex = if (tab == currentActive) 2f else 1f
                                                ) {
                                                    alpha = pageAlpha
                                                }
                                            }
                                        }
                                    }
                            ) {
                                saveableStateHolder.SaveableStateProvider(key = tab.route) {
                                    when (tab) {
                                        MainTab.Home -> HomeScreen(
                                            viewModel = homeViewModel,
                                            listState = homeListState,
                                            onEventClick = onHomeEventClick
                                        )

                                        MainTab.Event -> {
                                            val eventViewModel: EventViewModel = viewModel(factory = EventViewModel.Factory)
                                            LaunchedEffect(isEventActive) {
                                                eventViewModel.setEventTabActive(isEventActive)
                                                if (isEventActive) {
                                                    eventViewModel.syncViewModeFromPrefs()
                                                }
                                            }
                                            EventScreen(
                                                viewModel = eventViewModel,
                                                listState = eventListState,
                                                onEventClick = onEventIdClick
                                            )
                                        }

                                        MainTab.Monitor -> {
                                            val monitorViewModel: MonitorViewModel = viewModel(factory = MonitorViewModel.Factory)
                                            MonitorScreen(
                                                viewModel = monitorViewModel,
                                                gridState = monitorGridState,
                                                isTabActive = isMonitorActive,
                                                onEventClick = onEventIdClick,
                                                onFullscreenChange = onMonitorFullscreenChange
                                            )
                                        }

                                        MainTab.Setting -> SettingScreen(
                                            viewModel = viewModel,
                                            listState = settingListState,
                                            onLogoutClick = onLogoutClick,
                                            onOpenNotificationConfig = onOpenNotificationConfig,
                                            onOpenAbout = onOpenAbout
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (showBottomBar) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .zIndex(10f)
                    ) {
                        MainBottomNavigation(
                            currentTab = uiState.currentTab,
                            onTabClick = onBottomTabClick,
                            onScrollToTop = scrollTabToTop,
                            visibleTabs = visibleMainTabs
                        )
                    }
                }

                // Full-screen Live Camera Page sliding in from right & sliding out to right at zIndex(30f)
                // (above MainBottomNavigation at zIndex(10f), identical to MobileNotificationConfigScreen)
                if (MainTab.Monitor in effectiveMountedTabs) {
                    val monitorViewModel: MonitorViewModel = viewModel(factory = MonitorViewModel.Factory)
                    MonitorLiveCameraOverlay(
                        viewModel = monitorViewModel,
                        isTabActive = isMonitorActive,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = innerPadding.calculateTopPadding())
                            .zIndex(30f),
                        onFullscreenChange = onMonitorFullscreenChange,
                        onLiveScreenOpenChange = onLiveCameraPageOpenChange,
                        onEventClick = onEventIdClick
                    )
                }

                // Full-screen Notification Configuration Page sliding in from right & sliding out to right (matching MobileNotificationConfigModal.vue)
                AnimatedVisibility(
                    visible = showNotificationConfigPage,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding())
                        .zIndex(30f),
                    enter = slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(durationMillis = PAGE_ENTER_DURATION_MS, easing = PageEnterEasing)
                    ),
                    exit = slideOutHorizontally(
                        targetOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(durationMillis = PAGE_EXIT_DURATION_MS, easing = PageExitEasing)
                    )
                ) {
                    MobileNotificationConfigScreen(
                        viewModel = viewModel,
                        onBackClick = onCloseNotificationConfig
                    )
                }

                // Full-screen About ("Giới thiệu") Page sliding in from right & sliding out to right
                AnimatedVisibility(
                    visible = showAboutPage,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding())
                        .zIndex(30f),
                    enter = slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(durationMillis = PAGE_ENTER_DURATION_MS, easing = PageEnterEasing)
                    ),
                    exit = slideOutHorizontally(
                        targetOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(durationMillis = PAGE_EXIT_DURATION_MS, easing = PageExitEasing)
                    )
                ) {
                    MobileAboutScreen(
                        onBackClick = onCloseAbout
                    )
                }

                // Edge-to-Edge Root Overlay Host (above status bar & bottom navigation bar, in the same Activity window so system bars never turn black)
                if (overlayHostState.hasOverlays) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(50f)
                    ) {
                        overlayHostState.Content()
                    }
                }
            }
        }
    }
}

@Composable
fun SettingScreen(
    viewModel: MainViewModel,
    listState: LazyListState,
    onLogoutClick: () -> Unit,
    onOpenNotificationConfig: () -> Unit,
    onOpenAbout: () -> Unit = {}
) {
    val themeMode by ThemeManager.themeModeFlow.collectAsStateWithLifecycle()
    val permissionState by UserPermissionManager.permissionState.collectAsStateWithLifecycle()
    var showLogoutSheet by remember { mutableStateOf(false) }
    var showLanguageSheet by remember { mutableStateOf(false) }
    var showThemeSheet by remember { mutableStateOf(false) }
    var showChangePasswordSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var currentLanguage by remember { mutableStateOf(LocaleManager.getLocale(context)) }

    val uriHandler = LocalUriHandler.current
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val textColor = MaterialTheme.colorScheme.onBackground

    val defaultMyPage = stringResource(id = R.string.setting_my_page)
    val userName: String = remember(permissionState.version) {
        UserSession.userData?.name?.takeIf { it.isNotBlank() }
            ?: UserSession.savedUsername.takeIf { it.isNotBlank() }
            ?: defaultMyPage
    }

    val isEn = currentLanguage.startsWith("en")
    val groupModifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = spacing.space10)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Sticky Mobile Top Bar matching MobileSettingsPage.vue (.mobile-top-bar)
        MobileTopBar(
            title = stringResource(id = R.string.lbl_settings)
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(top = spacing.space2, bottom = spacing.bottomNavHeight + spacing.space56),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 2 Top Square Cards (User Profile & Support) matching .top-cards-grid (padding: 10px, gap: 10px)
            item(key = "top_cards") {
                Column(modifier = Modifier.widthIn(max = spacing.contentMaxWidth).fillMaxWidth()) {
                    Row(
                        modifier = groupModifier,
                        horizontalArrangement = Arrangement.spacedBy(spacing.space10)
                    ) {
                        SettingTopCard(
                            icon = LucideIcons.User,
                            iconBackground = colors.orange,
                            label = userName,
                            modifier = Modifier.weight(1f)
                        )
                        SettingTopCard(
                            icon = LucideIcons.Info,
                            iconBackground = colors.accent,
                            label = stringResource(id = R.string.setting_support),
                            onClick = { runCatching { uriHandler.openUri(SUPPORT_URL) } },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(spacing.space18))
                }
            }

            // Section: Cài đặt chung
            item(key = "section_general") {
                SettingsSection(title = stringResource(id = R.string.setting_general), groupModifier = groupModifier) {
                    SettingsRow(
                        icon = LucideIcons.Bell,
                        iconTint = colors.warning,
                        title = stringResource(id = R.string.setting_notifications),
                        onClick = onOpenNotificationConfig
                    )
                    SettingsRow(
                        icon = LucideIcons.Info,
                        iconTint = colors.info,
                        title = stringResource(id = R.string.setting_introduce),
                        onClick = onOpenAbout
                    )
                }
                Spacer(modifier = Modifier.height(spacing.space18))
            }

            // Section: Giao diện
            item(key = "section_appearance") {
                SettingsSection(title = if (isEn) "Appearance" else "Giao diện", groupModifier = groupModifier) {
                    SettingsRow(
                        icon = LucideIcons.Globe,
                        iconTint = colors.indigo,
                        title = stringResource(id = R.string.setting_language),
                        valueText = if (currentLanguage == "vi") stringResource(id = R.string.lang_vi) else stringResource(id = R.string.lang_en),
                        onClick = { showLanguageSheet = true }
                    )
                    SettingsRow(
                        icon = LucideIcons.Palette,
                        iconTint = colors.pink,
                        title = stringResource(id = R.string.setting_theme),
                        valueText = themeModeLabel(themeMode, isEn),
                        onClick = { showThemeSheet = true }
                    )
                }
                Spacer(modifier = Modifier.height(spacing.space18))
            }

            // Section: Tài khoản
            item(key = "section_account") {
                SettingsSection(title = stringResource(id = R.string.setting_account_title), groupModifier = groupModifier) {
                    SettingsRow(
                        icon = LucideIcons.Lock,
                        iconTint = colors.success,
                        title = stringResource(id = R.string.acc_password_security),
                        onClick = { showChangePasswordSheet = true }
                    )
                    SettingsRow(
                        icon = LucideIcons.LogOut,
                        iconTint = colors.error,
                        title = stringResource(id = R.string.setting_logout),
                        onClick = { showLogoutSheet = true },
                        titleColor = colors.error,
                        showChevron = false
                    )
                }
            }
        }

        // Language Bottom Sheet matching MobileSettingsPage.vue
        if (showLanguageSheet) {
            MobileNativeBottomSheet(
                title = stringResource(id = R.string.setting_choose_language),
                icon = LucideIcons.Globe,
                iconColor = colors.indigo,
                onDismiss = { showLanguageSheet = false }
            ) {
                OptionsCard {
                    LanguageOptions.forEach { option ->
                        SelectableOptionRow(
                            label = stringResource(id = option.labelRes),
                            isSelected = currentLanguage == option.code,
                            onClick = {
                                LocaleManager.setLocale(context, option.code)
                                currentLanguage = option.code
                                showLanguageSheet = false
                                (context as? Activity)?.recreate()
                            },
                            leading = {
                                Text(text = option.flag, style = MaterialTheme.typography.titleLarge)
                            }
                        )
                    }
                }
            }
        }

        // Theme Bottom Sheet matching MobileSettingsPage.vue
        if (showThemeSheet) {
            MobileNativeBottomSheet(
                title = stringResource(id = R.string.setting_theme),
                icon = LucideIcons.Palette,
                iconColor = colors.pink,
                onDismiss = { showThemeSheet = false }
            ) {
                OptionsCard {
                    ThemeModes.forEach { mode ->
                        val label = themeModeLabel(mode, isEn)
                        val (icon, badgeColor) = when (mode) {
                            ThemeManager.MODE_LIGHT -> LucideIcons.Sun to colors.warning
                            ThemeManager.MODE_DARK -> LucideIcons.Moon to colors.indigo
                            else -> LucideIcons.Monitor to colors.accent
                        }
                        SelectableOptionRow(
                            label = label,
                            isSelected = themeMode == mode,
                            onClick = {
                                ThemeManager.setThemeMode(mode)
                                showThemeSheet = false
                            },
                            leading = {
                                IconBadge(icon = icon, backgroundColor = badgeColor, contentDescription = label)
                            }
                        )
                    }
                }
            }
        }

        // Password Change Sheet matching MobileSettingsPage.vue
        if (showChangePasswordSheet) {
            MobilePasswordChangeSheet(
                viewModel = viewModel,
                onDismiss = { showChangePasswordSheet = false },
                onSuccess = {
                    showChangePasswordSheet = false
                    onLogoutClick()
                }
            )
        }

        // Logout Confirmation Sheet matching MobileSettingsPage.vue
        if (showLogoutSheet) {
            MobileNativeBottomSheet(
                title = null,
                onDismiss = { showLogoutSheet = false }
            ) {
                ConfirmSheetMessage(
                    icon = LucideIcons.LogOut,
                    accentColor = colors.error,
                    title = if (isEn) "Log out of your account?" else "Bạn muốn đăng xuất?",
                    message = if (isEn) {
                        "Your current session will end and you will need to sign in again to continue."
                    } else {
                        "Phiên làm việc hiện tại sẽ kết thúc và bạn cần đăng nhập lại để tiếp tục sử dụng."
                    }
                )
                Spacer(modifier = Modifier.height(spacing.space14))
                SheetActionButtons(
                    cancelText = stringResource(id = R.string.dialog_btn_cancel),
                    confirmText = stringResource(id = R.string.setting_logout),
                    onCancel = { showLogoutSheet = false },
                    onConfirm = {
                        showLogoutSheet = false
                        onLogoutClick()
                    },
                    confirmColor = colors.error
                )
            }
        }
    }
}

/** Thẻ vuông phía trên trang Cài đặt (.top-cards-grid): icon badge lớn + nhãn */
@Composable
private fun SettingTopCard(
    icon: ImageVector,
    iconBackground: Color,
    label: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val spacing = MaterialTheme.spacing
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(spacing.radiusMedium))
            .background(MaterialTheme.colorScheme.surface)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = spacing.space16, horizontal = spacing.space14),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconBadge(
            icon = icon,
            backgroundColor = iconBackground,
            contentDescription = label,
            size = spacing.iconExtraLarge,
            iconSize = spacing.iconMedium,
            cornerRadius = spacing.radiusCompact
        )
        Spacer(modifier = Modifier.height(spacing.space10))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Tiêu đề nhóm cài đặt (chữ phụ, đậm vừa) */
@Composable
private fun SettingsSectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.appColors.textMuted,
        fontWeight = FontWeight.SemiBold,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

/** Khối thẻ bo góc chứa các hàng cài đặt */
@Composable
private fun SettingsGroupCard(
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(MaterialTheme.spacing.radiusMedium))
            .background(background)
            .padding(vertical = MaterialTheme.spacing.space2),
        content = content
    )
}

/** Thẻ danh sách lựa chọn trong bottom sheet (ngôn ngữ, giao diện, mật khẩu) */
@Composable
private fun OptionsCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(MaterialTheme.spacing.radiusMedium))
            .background(MaterialTheme.appColors.surfaceRaised)
            .padding(vertical = MaterialTheme.spacing.space4),
        content = content
    )
}

/** Header + thẻ nhóm của trang Cài đặt, giới hạn độ rộng trên màn hình lớn */
@Composable
private fun SettingsSection(
    title: String,
    groupModifier: Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val spacing = MaterialTheme.spacing
    Column(modifier = Modifier.widthIn(max = spacing.contentMaxWidth).fillMaxWidth()) {
        SettingsSectionHeader(
            text = title,
            modifier = Modifier.padding(start = spacing.space14, end = spacing.space10, bottom = spacing.space6)
        )
        SettingsGroupCard(modifier = groupModifier, content = content)
    }
}

/** Hàng chọn 1 giá trị trong sheet: [leading] + nhãn + dấu check khi được chọn */
@Composable
private fun SelectableOptionRow(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    leading: @Composable () -> Unit
) {
    val spacing = MaterialTheme.spacing
    val accent = MaterialTheme.appColors.accent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.space14, vertical = spacing.space10),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leading()
        Spacer(modifier = Modifier.width(spacing.space12))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (isSelected) accent else MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (isSelected) {
            Icon(
                imageVector = LucideIcons.Check,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(spacing.iconSmallMedium)
            )
        }
    }
}

/** Nội dung sheet xác nhận: icon badge lớn ở giữa trên cùng + tiêu đề + mô tả rõ nét */
@Composable
private fun ConfirmSheetMessage(
    icon: ImageVector,
    accentColor: Color,
    title: String,
    message: String
) {
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.appColors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.space12)
            .padding(top = spacing.space4, bottom = spacing.space8),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconBadge(
            icon = icon,
            backgroundColor = accentColor,
            size = spacing.avatarLarge,
            iconSize = spacing.space28,
            cornerRadius = spacing.radiusMedium,
            tint = Color.White
        )
        Spacer(modifier = Modifier.height(spacing.space14))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.textStrong,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(spacing.space16))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textBody,
            textAlign = TextAlign.Center
        )
    }
}

private fun hasOsNotificationPermission(context: Context): Boolean {
    val enabledByManager = NotificationManagerCompat.from(context).areNotificationsEnabled()
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        enabledByManager &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else {
        enabledByManager
    }
}

private fun hasOverlayPermission(context: Context): Boolean {
    return com.tenli.oneview.ui.utils.PermissionUtils.isEmergencyCallPermissionGranted(context)
}

@Composable
private fun MobileNotificationConfigScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state by viewModel.notificationConfigState.collectAsStateWithLifecycle()

    var hasOsPermission by remember { mutableStateOf(hasOsNotificationPermission(context)) }
    var hasOverlayPerm by remember { mutableStateOf(hasOverlayPermission(context)) }
    var showOverlayPermissionPrompt by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasOsPermission = hasOsNotificationPermission(context)
                hasOverlayPerm = hasOverlayPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val openAppNotificationSettings: () -> Unit = {
        try {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    val openOverlayPermissionSettings: () -> Unit = {
        com.tenli.oneview.ui.utils.PermissionUtils.openEmergencyCallPermissionSettings(
            context = context,
            preferManufacturerSettings = false
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasOsPermission = hasOsNotificationPermission(context)
        if (!granted && !hasOsPermission) {
            openAppNotificationSettings()
        }
    }

    val requestOrOpenPermission: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            openAppNotificationSettings()
        }
    }

    val isEn = rememberIsEnglish()
    var pendingPushState by remember { mutableStateOf<Boolean?>(null) }

    val applyPushNotificationChange: (Boolean) -> Unit = { nextVal ->
        if (nextVal && !hasOsPermission) {
            requestOrOpenPermission()
        }
        viewModel.setPushNotificationsEnabled(nextVal, hasOsPermission)
    }

    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val backgroundColor = MaterialTheme.colorScheme.background

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
    ) {
        SettingSubPageHeader(
            title = stringResource(id = R.string.setting_notifications),
            onBackClick = onBackClick
        )

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = colors.accent,
                    modifier = Modifier.size(spacing.space28),
                    strokeWidth = spacing.progressStroke
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = spacing.contentMaxWidth)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding()
                        .padding(spacing.space10)
                ) {
                    val sectionHeaderModifier = Modifier.padding(start = spacing.space4, bottom = spacing.space6)

                    // 1. Device Permission Status Card (Thông báo + Hiển thị cuộc gọi trên ứng dụng khác)
                    SettingsSectionHeader(
                        text = if (isEn) "Device Permissions" else "Quyền trên thiết bị",
                        modifier = sectionHeaderModifier
                    )
                    SettingsGroupCard {
                        if (hasOsPermission) {
                            NotificationSettingRow(
                                icon = LucideIcons.CheckCircle,
                                iconColor = colors.success,
                                title = if (isEn) "Notification permission" else "Quyền thông báo",
                                subtitle = if (isEn) "This device is ready to receive alerts." else "Thiết bị này đã sẵn sàng nhận cảnh báo."
                            ) {
                                StatusChip(
                                    text = if (isEn) "Enabled" else "Đang bật",
                                    color = colors.success
                                )
                            }
                        } else {
                            NotificationSettingRow(
                                icon = LucideIcons.Bell,
                                iconColor = colors.warning,
                                title = if (isEn) "Notification permission" else "Quyền thông báo",
                                subtitle = if (isEn) {
                                    "Notification permission is required to receive alerts for new events."
                                } else {
                                    "Cần cấp quyền thông báo để nhận cảnh báo khi có sự kiện mới."
                                }
                            ) {
                                StatusChip(
                                    text = if (isEn) "Grant now" else "Cấp quyền ngay",
                                    color = colors.accent,
                                    onClick = requestOrOpenPermission
                                )
                            }
                        }

                        HorizontalDivider(
                            color = colors.divider,
                            thickness = spacing.borderThin
                        )

                        if (hasOverlayPerm) {
                            NotificationSettingRow(
                                icon = LucideIcons.PhoneCall,
                                iconColor = colors.success,
                                title = if (isEn) "Emergency call" else "Cuộc gọi khẩn cấp",
                                subtitle = if (isEn) {
                                    "Allows incoming call alerts to pop up over other apps."
                                } else {
                                    "Cho phép bật màn hình cuộc gọi cảnh báo đè lên ứng dụng khác."
                                }
                            ) {
                                StatusChip(
                                    text = if (isEn) "Enabled" else "Đang bật",
                                    color = colors.success
                                )
                            }
                        } else {
                            NotificationSettingRow(
                                icon = LucideIcons.PhoneCall,
                                iconColor = colors.warning,
                                title = if (isEn) "Emergency call" else "Cuộc gọi khẩn cấp",
                                subtitle = if (isEn) {
                                    "Required to pop up the incoming call screen when the app is in background."
                                } else {
                                    "Cần cấp quyền hiển thị trên ứng dụng khác để bật màn hình cuộc gọi khi có cảnh báo."
                                }
                            ) {
                                StatusChip(
                                    text = if (isEn) "Grant now" else "Cấp quyền ngay",
                                    color = colors.accent,
                                    onClick = openOverlayPermissionSettings
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(spacing.space18))

                    // 2. System General Setting matching MobileNotificationConfigModal.vue
                    SettingsSectionHeader(
                        text = stringResource(id = R.string.setting_general),
                        modifier = sectionHeaderModifier
                    )
                    SettingsGroupCard {
                        NotificationSettingRow(
                            icon = LucideIcons.Bell,
                            iconColor = colors.warning,
                            title = if (isEn) "Allow push notifications" else "Cho phép nhận thông báo",
                            subtitle = if (!hasOsPermission && state.pushEnabled) {
                                if (isEn) "⚠️ Device permission is not granted so notifications cannot be received" else "⚠️ Thiết bị này chưa cấp quyền nên chưa thể nhận thông báo"
                            } else {
                                null
                            },
                            subtitleColor = colors.warning,
                            subtitleTopSpacing = spacing.space4
                        ) {
                            Switch(
                                checked = state.pushEnabled,
                                enabled = !state.isSaving,
                                onCheckedChange = { nextVal -> pendingPushState = nextVal },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = colors.onAccent,
                                    checkedTrackColor = colors.accent
                                )
                            )
                        }
                    }

                    // 3. Cấu hình chi tiết theo từng bài AI (overrides.event_types) - Gom gọn trong 1 Card + Thu gọn/Mở rộng
                    if (state.aiModules.isNotEmpty()) {
                        var isAiModulesExpanded by remember { mutableStateOf(false) }
                        val previewCount = 3
                        val primaryModules = remember(state.aiModules) {
                            state.aiModules.take(previewCount)
                        }
                        val extraModules = remember(state.aiModules) {
                            state.aiModules.drop(previewCount)
                        }
                        val callCount = remember(state.aiModules) {
                            state.aiModules.count { it.enabled && it.alarmMode == AiAlarmMode.Call }
                        }
                        val pushCount = remember(state.aiModules) {
                            state.aiModules.count { it.enabled && it.alarmMode == AiAlarmMode.Push }
                        }
                        val offCount = remember(state.aiModules) {
                            state.aiModules.count { !it.enabled }
                        }
                        val chevronRotation by animateFloatAsState(
                            targetValue = if (isAiModulesExpanded) 180f else 0f,
                            animationSpec = tween(220),
                            label = "aiModulesChevron"
                        )

                        Spacer(modifier = Modifier.height(spacing.space18))

                        SettingsSectionHeader(
                            text = if (isEn) "AI Event Alert Configuration" else "Cấu hình thông báo theo bài AI",
                            modifier = sectionHeaderModifier
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .alpha(if (state.pushEnabled) 1f else 0.5f)
                        ) {
                            SettingsGroupCard {
                                // Thanh tóm tắt trạng thái các bài AI ở đầu thẻ
                                AiNotifySummaryBar(
                                    isEn = isEn,
                                    pushCount = pushCount,
                                    callCount = callCount,
                                    offCount = offCount
                                )

                                HorizontalDivider(
                                    color = colors.divider,
                                    thickness = spacing.borderThin
                                )

                                // 3 bài AI đầu tiên hiển thị mặc định (ưu tiên: Cuộc gọi -> Tắt thông báo -> Thông báo)
                                primaryModules.forEachIndexed { index, moduleConfig ->
                                    androidx.compose.runtime.key(moduleConfig.moduleKey) {
                                        if (index > 0) {
                                            HorizontalDivider(
                                                color = colors.divider,
                                                thickness = spacing.borderThin
                                            )
                                        }
                                        AiModuleNotifyRow(
                                            config = moduleConfig,
                                            isEn = isEn,
                                            masterEnabled = state.pushEnabled,
                                            hasOverlayPermission = hasOverlayPerm,
                                            onToggleEnabled = { nextEnabled ->
                                                viewModel.updateAiModuleNotifyConfig(
                                                    moduleKey = moduleConfig.moduleKey,
                                                    enabled = nextEnabled,
                                                    alarmMode = moduleConfig.alarmMode
                                                )
                                            },
                                            onSelectAlarmMode = { nextMode ->
                                                if (nextMode != moduleConfig.alarmMode) {
                                                    viewModel.updateAiModuleNotifyConfig(
                                                        moduleKey = moduleConfig.moduleKey,
                                                        enabled = moduleConfig.enabled,
                                                        alarmMode = nextMode
                                                    )
                                                }
                                                if (nextMode == AiAlarmMode.Call && !hasOverlayPerm) {
                                                    showOverlayPermissionPrompt = true
                                                }
                                            }
                                        )
                                    }
                                }

                                // Phần danh sách mở rộng
                                if (extraModules.isNotEmpty()) {
                                    AnimatedVisibility(
                                        visible = isAiModulesExpanded,
                                        enter = expandVertically(tween(240)) + fadeIn(tween(200)),
                                        exit = shrinkVertically(tween(200)) + fadeOut(tween(150))
                                    ) {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            extraModules.forEach { moduleConfig ->
                                                androidx.compose.runtime.key(moduleConfig.moduleKey) {
                                                    HorizontalDivider(
                                                        color = colors.divider,
                                                        thickness = spacing.borderThin
                                                    )
                                                    AiModuleNotifyRow(
                                                        config = moduleConfig,
                                                        isEn = isEn,
                                                        masterEnabled = state.pushEnabled,
                                                        hasOverlayPermission = hasOverlayPerm,
                                                        onToggleEnabled = { nextEnabled ->
                                                            viewModel.updateAiModuleNotifyConfig(
                                                                moduleKey = moduleConfig.moduleKey,
                                                                enabled = nextEnabled,
                                                                alarmMode = moduleConfig.alarmMode
                                                            )
                                                        },
                                                        onSelectAlarmMode = { nextMode ->
                                                            if (nextMode != moduleConfig.alarmMode) {
                                                                viewModel.updateAiModuleNotifyConfig(
                                                                    moduleKey = moduleConfig.moduleKey,
                                                                    enabled = moduleConfig.enabled,
                                                                    alarmMode = nextMode
                                                                )
                                                            }
                                                            if (nextMode == AiAlarmMode.Call && !hasOverlayPerm) {
                                                                showOverlayPermissionPrompt = true
                                                            }
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    HorizontalDivider(
                                        color = colors.divider,
                                        thickness = spacing.borderThin
                                    )

                                    // Nút Xem tất cả / Thu gọn
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { isAiModulesExpanded = !isAiModulesExpanded }
                                            .padding(horizontal = spacing.space14, vertical = spacing.space12),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isAiModulesExpanded) {
                                                if (isEn) "Show less" else "Thu gọn danh sách"
                                            } else {
                                                if (isEn) {
                                                    "View all ${state.aiModules.size} AI modules (+${extraModules.size})"
                                                } else {
                                                    "Xem tất cả ${state.aiModules.size} bài AI (+${extraModules.size} bài khác)"
                                                }
                                            },
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = colors.accent
                                        )
                                        Spacer(modifier = Modifier.width(spacing.space6))
                                        Icon(
                                            imageVector = LucideIcons.ChevronDown,
                                            contentDescription = null,
                                            tint = colors.accent,
                                            modifier = Modifier
                                                .size(spacing.iconSmall)
                                                .graphicsLayer { rotationZ = chevronRotation }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Sheet xác nhận khi bật/tắt Cho phép nhận thông báo
        val targetPushState = pendingPushState
        if (targetPushState != null) {
            val accentColor = if (targetPushState) colors.accent else colors.error

            MobileNativeBottomSheet(
                title = null,
                onDismiss = { pendingPushState = null }
            ) {
                ConfirmSheetMessage(
                    icon = LucideIcons.Bell,
                    accentColor = accentColor,
                    title = if (targetPushState) {
                        if (isEn) "Allow push notifications?" else "Bật nhận thông báo?"
                    } else {
                        if (isEn) "Turn off push notifications?" else "Tắt nhận thông báo?"
                    },
                    message = if (targetPushState) {
                        if (isEn) {
                            "Your device will receive instant alerts whenever new AI events are detected."
                        } else {
                            "Thiết bị sẽ nhận cảnh báo tức thì ngay khi hệ thống phát hiện sự kiện mới."
                        }
                    } else {
                        if (isEn) {
                            "You will no longer receive push alerts for new events until you turn notifications back on."
                        } else {
                            "Bạn sẽ không nhận được thông báo đẩy khi có sự kiện mới cho đến khi bật lại."
                        }
                    }
                )
                Spacer(modifier = Modifier.height(spacing.space14))
                SheetActionButtons(
                    cancelText = stringResource(id = R.string.dialog_btn_cancel),
                    confirmText = if (isEn) "Confirm" else "Xác nhận",
                    onCancel = { pendingPushState = null },
                    onConfirm = {
                        pendingPushState = null
                        applyPushNotificationChange(targetPushState)
                    },
                    confirmColor = accentColor
                )
            }
        }

        // Bottom Sheet nhắc cấp quyền Hiển thị trên ứng dụng khác khi chọn hình thức "Cuộc gọi"
        if (showOverlayPermissionPrompt) {
            MobileNativeBottomSheet(
                title = null,
                onDismiss = { showOverlayPermissionPrompt = false }
            ) {
                ConfirmSheetMessage(
                    icon = LucideIcons.PhoneCall,
                    accentColor = colors.accent,
                    title = if (isEn) {
                        "Grant call overlay permission?"
                    } else {
                        "Cấp quyền hiển thị cuộc gọi?"
                    },
                    message = if (isEn) {
                        "To automatically display the incoming call alert screen when your phone is on another app or locked, please allow Tenli OneView to display over other apps."
                    } else {
                        "Để ứng dụng tự động bật màn hình cuộc gọi đến khi phát hiện sự kiện AI (kể cả khi đang mở ứng dụng khác hoặc chạy ngầm), vui lòng cấp quyền \"Hiển thị trên ứng dụng khác\"."
                    }
                )
                Spacer(modifier = Modifier.height(spacing.space14))
                SheetActionButtons(
                    cancelText = if (isEn) "Later" else "Để sau",
                    confirmText = if (isEn) "Grant now" else "Cấp quyền ngay",
                    onCancel = { showOverlayPermissionPrompt = false },
                    onConfirm = {
                        showOverlayPermissionPrompt = false
                        openOverlayPermissionSettings()
                    },
                    confirmColor = colors.accent
                )
            }
        }
    }
}

@Composable
private fun AiNotifySummaryBar(
    isEn: Boolean,
    pushCount: Int,
    callCount: Int,
    offCount: Int
) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.space14, vertical = spacing.space10),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.space6)
    ) {
        AiSummaryBadge(
            icon = LucideIcons.PhoneCall,
            text = if (isEn) "$callCount Call" else "$callCount Cuộc gọi",
            tint = colors.error
        )
        AiSummaryBadge(
            icon = LucideIcons.Bell,
            text = if (isEn) "$pushCount Push" else "$pushCount Thông báo",
            tint = colors.accent
        )
        AiSummaryBadge(
            icon = LucideIcons.BellOff,
            text = if (isEn) "$offCount Off" else "$offCount Tắt",
            tint = if (colors.isDark) colors.textPlaceholder else colors.textMuted
        )
    }
}

@Composable
private fun AiSummaryBadge(
    icon: ImageVector?,
    text: String,
    tint: Color
) {
    val spacing = MaterialTheme.spacing
    val contentColor = MaterialTheme.appColors.onAccent
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(spacing.radiusTiny))
            .background(tint)
            .padding(horizontal = spacing.space8, vertical = spacing.space4),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(spacing.iconTiny)
            )
            Spacer(modifier = Modifier.width(spacing.space4))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
    }
}

@Composable
private fun AiModuleNotifyRow(
    config: AiModuleNotifyConfig,
    isEn: Boolean,
    masterEnabled: Boolean,
    hasOverlayPermission: Boolean,
    onToggleEnabled: (Boolean) -> Unit,
    onSelectAlarmMode: (AiAlarmMode) -> Unit
) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val isInteractive = masterEnabled

    val subtitleText = when {
        !config.enabled -> if (isEn) "Alerts disabled for this AI module" else "Đang tắt thông báo"
        config.alarmMode == AiAlarmMode.Call && !hasOverlayPermission ->
            if (isEn) "Incoming Call • Grant overlay permission" else "Hình thức: Cuộc gọi • Cần cấp quyền đè"
        config.alarmMode == AiAlarmMode.Call ->
            if (isEn) "Alert mode: Incoming call" else "Hình thức: Đổ chuông cuộc gọi"
        else ->
            if (isEn) "Alert mode: Push notification" else "Hình thức: Thông báo đẩy"
    }

    val subtitleColor = when {
        !config.enabled -> colors.textPlaceholder
        config.alarmMode == AiAlarmMode.Call && !hasOverlayPermission -> colors.warning
        config.alarmMode == AiAlarmMode.Call -> colors.error
        else -> colors.textMuted
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.space14, vertical = spacing.space12)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isEn) config.labelEn else config.labelVi,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (config.enabled) MaterialTheme.colorScheme.onBackground else colors.textMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(spacing.space2))
                Text(
                    text = subtitleText,
                    style = MaterialTheme.typography.labelSmall,
                    color = subtitleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(spacing.space10))
            Switch(
                checked = config.enabled,
                enabled = isInteractive,
                onCheckedChange = onToggleEnabled,
                modifier = Modifier.scale(0.88f),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colors.onAccent,
                    checkedTrackColor = colors.accent
                )
            )
        }

        AnimatedVisibility(
            visible = config.enabled,
            enter = expandVertically(tween(200)) + fadeIn(tween(200)),
            exit = shrinkVertically(tween(180)) + fadeOut(tween(150))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(spacing.space10))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(spacing.radiusSmallMedium))
                        .background(colors.controlBg)
                        .padding(spacing.space3),
                    horizontalArrangement = Arrangement.spacedBy(spacing.space4)
                ) {
                    AlarmModeOptionButton(
                        modifier = Modifier.weight(1f),
                        icon = LucideIcons.Bell,
                        label = if (isEn) "Notification" else "Thông báo",
                        selected = config.alarmMode == AiAlarmMode.Push,
                        activeColor = colors.accent,
                        enabled = isInteractive,
                        onClick = { onSelectAlarmMode(AiAlarmMode.Push) }
                    )
                    AlarmModeOptionButton(
                        modifier = Modifier.weight(1f),
                        icon = LucideIcons.PhoneCall,
                        label = if (isEn) "Incoming Call" else "Cuộc gọi",
                        selected = config.alarmMode == AiAlarmMode.Call,
                        activeColor = colors.error,
                        enabled = isInteractive,
                        onClick = { onSelectAlarmMode(AiAlarmMode.Call) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AlarmModeOptionButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    selected: Boolean,
    activeColor: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val shape = RoundedCornerShape(spacing.radiusSmall)
    val bgColor = if (selected) activeColor else Color.Transparent
    val contentColor = if (selected) colors.onAccent else colors.textMuted

    Row(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = spacing.space10, vertical = spacing.space6),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(spacing.iconXSmall)
        )
        Spacer(modifier = Modifier.width(spacing.space6))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Thanh tiêu đề dùng chung cho các trang con trong Cài đặt (nút Back bên trái + tiêu đề căn giữa) */
@Composable
private fun SettingSubPageHeader(
    title: String,
    onBackClick: () -> Unit
) {
    val spacing = MaterialTheme.spacing
    val backgroundColor = MaterialTheme.colorScheme.background
    val titleColor = MaterialTheme.colorScheme.onBackground

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(spacing.topBarHeight)
            .background(backgroundColor)
            .padding(horizontal = spacing.space10),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(spacing.iconButton)
                .clip(CircleShape)
                .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = LucideIcons.ChevronLeft,
                contentDescription = stringResource(id = R.string.common_back),
                tint = titleColor,
                modifier = Modifier.size(spacing.iconMedium)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = titleColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(spacing.iconButton))
    }
}

/** Màn hình Giới thiệu: hiển thị icon ứng dụng, tên "Tenli OneView" và phiên bản lấy từ [BuildConfig.VERSION_NAME] */
@Composable
private fun MobileAboutScreen(
    onBackClick: () -> Unit
) {
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.appColors
    val backgroundColor = MaterialTheme.colorScheme.background
    val brandTitle = remember(colors.textTitle, colors.accent) {
        buildAnnotatedString {
            withStyle(SpanStyle(color = colors.textTitle)) { append("Tenli ") }
            withStyle(SpanStyle(color = colors.accent)) { append("OneView") }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
    ) {
        SettingSubPageHeader(
            title = stringResource(id = R.string.setting_introduce),
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(top = spacing.aboutTopPadding, start = spacing.paddingScreen, end = spacing.paddingScreen),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_pwa),
                contentDescription = stringResource(id = R.string.intro_logo_description),
                modifier = Modifier.size(spacing.logoAbout)
            )
            Spacer(modifier = Modifier.height(spacing.space2))
            Text(
                text = brandTitle,
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(spacing.space6))
            Text(
                text = stringResource(id = R.string.intro_version_label, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = colors.textSubtle,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Hàng trong màn hình Cấu hình thông báo: icon badge + tiêu đề/mô tả + phần tử bên phải */
@Composable
private fun NotificationSettingRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String?,
    subtitleColor: Color = MaterialTheme.appColors.textMuted,
    subtitleTopSpacing: Dp = MaterialTheme.spacing.space2,
    trailing: @Composable () -> Unit
) {
    val spacing = MaterialTheme.spacing
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(spacing.space16),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(
            icon = icon,
            backgroundColor = iconColor,
            size = spacing.iconButton,
            iconSize = spacing.iconSmallMedium,
            cornerRadius = spacing.radiusSmall
        )
        Spacer(modifier = Modifier.width(spacing.space14))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(subtitleTopSpacing))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = subtitleColor
                )
            }
        }
        Spacer(modifier = Modifier.width(spacing.space10))
        trailing()
    }
}

/** Nhãn trạng thái / nút nhỏ bo góc (vd. "Đang bật", "Cấp quyền ngay") */
@Composable
private fun StatusChip(
    text: String,
    color: Color,
    onClick: (() -> Unit)? = null
) {
    val spacing = MaterialTheme.spacing
    Box(
        modifier = Modifier
            .then(if (onClick != null) Modifier.heightIn(min = spacing.avatarSmall) else Modifier)
            .clip(RoundedCornerShape(spacing.radiusSmall))
            .background(color)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(
                horizontal = if (onClick != null) spacing.space12 else spacing.space10,
                vertical = spacing.space4
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.appColors.onAccent,
            maxLines = 1
        )
    }
}

@Composable
private fun passwordChangeErrorText(error: PasswordChangeError, isEn: Boolean): String = when (error) {
    PasswordChangeError.EmptyFields -> if (isEn) "Please fill in all fields" else "Vui lòng nhập đầy đủ thông tin"
    PasswordChangeError.TooShort -> if (isEn) "New password must be at least 6 characters" else "Mật khẩu mới phải có ít nhất 6 ký tự"
    PasswordChangeError.Mismatch -> stringResource(id = R.string.err_passwords_mismatch)
    PasswordChangeError.UserNotFound -> if (isEn) "User information not found" else "Không tìm thấy thông tin người dùng"
    is PasswordChangeError.WrongCurrentPassword ->
        if (isEn) "Current password is incorrect (${error.code})" else "Mật khẩu hiện tại không đúng (${error.code})"
    is PasswordChangeError.Unexpected ->
        error.message ?: if (isEn) "An error occurred while changing password" else "Có lỗi xảy ra khi đổi mật khẩu"
}

@Composable
private fun MobilePasswordChangeSheet(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val isEn = rememberIsEnglish()
    val keyboardController = LocalSoftwareKeyboardController.current
    val state by viewModel.passwordChangeState.collectAsStateWithLifecycle()

    val oldPasswordFocusRequester = remember { FocusRequester() }
    val newPasswordFocusRequester = remember { FocusRequester() }
    val confirmPasswordFocusRequester = remember { FocusRequester() }

    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showOld by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing

    DisposableEffect(viewModel) {
        viewModel.resetPasswordChange()
        onDispose { viewModel.resetPasswordChange() }
    }

    val currentOnSuccess by rememberUpdatedState(onSuccess)
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) currentOnSuccess()
    }

    LaunchedEffect(Unit) {
        delay(PASSWORD_SHEET_FOCUS_DELAY_MS)
        try {
            oldPasswordFocusRequester.requestFocus()
            delay(KEYBOARD_SHOW_DELAY_MS)
            keyboardController?.show()
        } catch (_: Exception) {
        }
    }

    MobileNativeBottomSheet(
        title = stringResource(id = R.string.acc_password_security),
        icon = LucideIcons.Lock,
        iconColor = colors.success,
        onDismiss = onDismiss
    ) {
        val error = state.error
        if (error != null) {
            val errorShape = RoundedCornerShape(spacing.radiusCompact)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = spacing.space12)
                    .clip(errorShape)
                    .background(colors.error.copy(alpha = ERROR_BOX_BG_ALPHA))
                    .border(spacing.borderMedium, colors.error.copy(alpha = ERROR_BOX_BORDER_ALPHA), errorShape)
                    .padding(horizontal = spacing.space12, vertical = spacing.space10)
            ) {
                Text(
                    text = passwordChangeErrorText(error, isEn),
                    color = colors.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        OptionsCard {
            PasswordFieldRow(
                badgeColor = colors.accent,
                badgeIcon = LucideIcons.Lock,
                placeholder = if (isEn) "Current password" else "Mật khẩu hiện tại",
                value = oldPassword,
                onValueChange = {
                    oldPassword = it
                    viewModel.clearPasswordChangeError()
                },
                isVisible = showOld,
                onToggleVisible = { showOld = !showOld },
                focusRequester = oldPasswordFocusRequester,
                imeAction = ImeAction.Next,
                onImeAction = {
                    try {
                        newPasswordFocusRequester.requestFocus()
                    } catch (_: Exception) {
                    }
                }
            )
            PasswordFieldDivider()
            PasswordFieldRow(
                badgeColor = colors.warning,
                badgeIcon = LucideIcons.ShieldAlert,
                placeholder = stringResource(id = R.string.forgot_pass_new_pass_hint),
                value = newPassword,
                onValueChange = {
                    newPassword = it
                    viewModel.clearPasswordChangeError()
                },
                isVisible = showNew,
                onToggleVisible = { showNew = !showNew },
                focusRequester = newPasswordFocusRequester,
                imeAction = ImeAction.Next,
                onImeAction = {
                    try {
                        confirmPasswordFocusRequester.requestFocus()
                    } catch (_: Exception) {
                    }
                }
            )
            PasswordFieldDivider()
            PasswordFieldRow(
                badgeColor = colors.success,
                badgeIcon = LucideIcons.CheckCircle,
                placeholder = stringResource(id = R.string.forgot_pass_confirm_pass_hint),
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    viewModel.clearPasswordChangeError()
                },
                isVisible = showConfirm,
                onToggleVisible = { showConfirm = !showConfirm },
                focusRequester = confirmPasswordFocusRequester,
                imeAction = ImeAction.Done,
                onImeAction = { keyboardController?.hide() }
            )
        }

        Spacer(modifier = Modifier.height(spacing.space16))

        SheetActionButtons(
            cancelText = stringResource(id = R.string.dialog_btn_cancel),
            confirmText = if (isEn) "Confirm" else "Xác nhận",
            onCancel = onDismiss,
            onConfirm = { viewModel.changePassword(oldPassword, newPassword, confirmPassword) },
            enabled = !state.isSubmitting,
            isLoading = state.isSubmitting
        )
    }
}

@Composable
private fun PasswordFieldDivider() {
    HorizontalDivider(
        thickness = MaterialTheme.spacing.borderThin,
        color = MaterialTheme.appColors.border,
        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.space14)
    )
}

@Composable
private fun PasswordFieldRow(
    badgeColor: Color,
    badgeIcon: ImageVector,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    isVisible: Boolean,
    onToggleVisible: () -> Unit,
    focusRequester: FocusRequester,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {}
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.appColors
    val textColor = colors.textTitle
    val placeholderColor = colors.textMuted
    val baseTextStyle = MaterialTheme.typography.bodyMedium
    val textStyle = remember(baseTextStyle, textColor) {
        baseTextStyle.copy(color = textColor, fontWeight = FontWeight.Medium)
    }
    val cursorBrush = remember(colors.accent) { SolidColor(colors.accent) }
    val keyboardOptions = remember(imeAction) {
        KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                try {
                    focusRequester.requestFocus()
                    keyboardController?.show()
                } catch (_: Exception) {
                }
            }
            .padding(horizontal = spacing.space14, vertical = spacing.space12),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(icon = badgeIcon, backgroundColor = badgeColor, contentDescription = placeholder)
        Spacer(modifier = Modifier.width(spacing.space12))
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = placeholderColor,
                    style = baseTextStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = textStyle,
                visualTransformation = if (isVisible) VisualTransformation.None else PasswordMask,
                keyboardOptions = keyboardOptions,
                keyboardActions = KeyboardActions(
                    onNext = { onImeAction() },
                    onDone = { onImeAction() }
                ),
                cursorBrush = cursorBrush,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
        }
        Icon(
            imageVector = if (isVisible) LucideIcons.EyeOff else LucideIcons.Eye,
            contentDescription = null,
            tint = placeholderColor,
            modifier = Modifier
                .size(spacing.iconRegular)
                .clickable(onClick = onToggleVisible)
        )
    }
}

/** Hàng cài đặt (MobileSettingsPage.vue): icon badge màu + tiêu đề + giá trị + mũi tên */
@Composable
private fun SettingsRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    onClick: () -> Unit,
    titleColor: Color = MaterialTheme.colorScheme.onBackground,
    valueText: String? = null,
    showChevron: Boolean = true
) {
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.appColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.space16, vertical = spacing.space10),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Squircle colored icon badge matching MobileSettingsPage.vue
        IconBadge(
            icon = icon,
            backgroundColor = iconTint,
            size = spacing.iconLarge,
            iconSize = spacing.iconSmallMedium
        )
        Spacer(modifier = Modifier.width(spacing.space14))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = titleColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (valueText != null) {
            Text(
                text = valueText,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .widthIn(max = SettingsValueMaxWidth)
                    .padding(start = spacing.space8, end = spacing.space6)
            )
        }
        if (showChevron) {
            Icon(
                imageVector = LucideIcons.ChevronRight,
                contentDescription = null,
                tint = if (colors.isDark) Slate500 else Slate300,
                modifier = Modifier.size(spacing.iconSmallMedium)
            )
        }
    }
}

/** Giới hạn độ rộng giá trị bên phải hàng cài đặt để tiêu đề không bị ép mất trên máy hẹp */
private val SettingsValueMaxWidth = 140.dp
