package com.tenli.oneview.ui.features.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.ui.zIndex
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tenli.oneview.R
import com.tenli.oneview.data.local.GlobalData
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.data.network.api.BsApi
import com.tenli.oneview.data.network.api.VmsApi
import com.tenli.oneview.data.network.retrofit.LoginAuthClient
import com.tenli.oneview.model.network.ChangePasswordModel
import com.tenli.oneview.model.network.UserNotificationGlobalSetting
import com.tenli.oneview.model.network.UserNotificationSettingsModel
import com.tenli.oneview.ui.component.MobileTopBar
import com.tenli.oneview.ui.features.home.HomeScreen
import com.tenli.oneview.ui.theme.BrandAccent
import com.tenli.oneview.ui.theme.ErrorRed
import com.tenli.oneview.ui.theme.LucideIcons
import com.tenli.oneview.ui.theme.ThemeManager
import com.tenli.oneview.ui.utils.LocaleManager
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

private val OrderedMainTabs = listOf(
    MainTab.Home,
    MainTab.Event,
    MainTab.Monitor,
    MainTab.Setting
)

// Exact cubic-bezier curves from tenli-oneview MobileLayout.vue:
// .mobile-tab-slide-*-leave-active: transform 0.09s cubic-bezier(0.35, 0, 0.7, 0.2), opacity 0.08s ease-out
// .mobile-tab-slide-*-enter-active: transform 0.21s cubic-bezier(0.16, 1, 0.3, 1), opacity 0.16s ease-out
private val TabLeaveTransformEasing = CubicBezierEasing(0.35f, 0f, 0.7f, 0.2f)
private val TabEnterTransformEasing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
private val TabOpacityEaseOut = CubicBezierEasing(0f, 0f, 0.58f, 1f)

@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel(factory = MainViewModel.Factory),
    onLogoutRequest: () -> Unit,
    onNavigateToEventDetail: (Int) -> Unit = {},
    onNavigateToPlaybackDetail: (videoLink: String, time: String, imageLink: String, cameraName: String) -> Unit = { _, _, _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigateToSettingTarget: (String) -> Unit = { _ ->
        viewModel.onTabSelected(MainTab.Setting)
    }

    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val saveableStateHolder = rememberSaveableStateHolder()
    var showBottomBar by remember { mutableStateOf(true) }
    var showNotificationConfigPage by remember { mutableStateOf(false) }
    val overlayHostState = remember { com.tenli.oneview.ui.component.MobileOverlayHostState() }

    val homeListState = rememberLazyListState()
    val monitorState = rememberLazyListState()
    val eventListState = rememberLazyListState()
    val settingListState = rememberLazyListState()

    val isHomeScrolled by remember {
        derivedStateOf { homeListState.firstVisibleItemIndex > 0 || homeListState.firstVisibleItemScrollOffset > 0 }
    }
    val isMonitorScrolled by remember {
        derivedStateOf { monitorState.firstVisibleItemIndex > 0 || monitorState.firstVisibleItemScrollOffset > 0 }
    }
    val isEventScrolled by remember {
        derivedStateOf { eventListState.firstVisibleItemIndex > 0 || eventListState.firstVisibleItemScrollOffset > 0 }
    }
    val isSettingScrolled by remember {
        derivedStateOf { settingListState.firstVisibleItemIndex > 0 || settingListState.firstVisibleItemScrollOffset > 0 }
    }

    val isCurrentTabScrolled = when (uiState.currentTab) {
        MainTab.Home -> isHomeScrolled
        MainTab.Monitor -> isMonitorScrolled
        MainTab.Event -> isEventScrolled
        MainTab.Setting -> isSettingScrolled
    }

    // Keep-Alive (<keep-alive>) state + sequential mode="out-in" GPU animation state matching MobileLayout.vue
    val homeViewModel: com.tenli.oneview.ui.features.home.HomeViewModel = viewModel(
        factory = com.tenli.oneview.ui.features.home.HomeViewModel.Factory
    )
    val eventViewModel: com.tenli.oneview.ui.features.event.EventViewModel = viewModel(
        factory = com.tenli.oneview.ui.features.event.EventViewModel.Factory
    )
    var displayedTab by remember { mutableStateOf(uiState.currentTab) }
    val tabOffsetFraction = remember { Animatable(0f) }
    val tabAlpha = remember { Animatable(1f) }

    LaunchedEffect(uiState.currentTab) {
        val targetTab = uiState.currentTab
        if (targetTab != MainTab.Monitor) {
            showBottomBar = true
        }
        if (targetTab != MainTab.Setting) {
            showNotificationConfigPage = false
        }

        if (targetTab == displayedTab) {
            coroutineScope {
                launch {
                    tabOffsetFraction.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 210, easing = TabEnterTransformEasing)
                    )
                }
                launch {
                    tabAlpha.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 160, easing = TabOpacityEaseOut)
                    )
                }
            }
            return@LaunchedEffect
        }

        val fromIdx = OrderedMainTabs.indexOf(displayedTab)
        val toIdx = OrderedMainTabs.indexOf(targetTab)
        val isForward = toIdx >= fromIdx

        // Phase 1 (mode="out-in" Leave): Outgoing tab quickly slides out (-24% / +24%) and fades out in 90ms
        val leaveTargetOffset = if (isForward) -0.24f else 0.24f
        coroutineScope {
            launch {
                tabOffsetFraction.animateTo(
                    targetValue = leaveTargetOffset,
                    animationSpec = tween(durationMillis = 90, easing = TabLeaveTransformEasing)
                )
            }
            launch {
                tabAlpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 80, easing = TabOpacityEaseOut)
                )
            }
        }

        // Swap active tab while opacity == 0 (no overlap / no double header or vertical seam)
        displayedTab = targetTab
        val enterStartOffset = if (isForward) 0.35f else -0.35f
        tabOffsetFraction.snapTo(enterStartOffset)
        tabAlpha.snapTo(0f)

        // Phase 2 (mode="out-in" Enter): Incoming tab smoothly decelerates (+35% / -35% -> 0%) in 210ms
        coroutineScope {
            launch {
                tabOffsetFraction.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 210, easing = TabEnterTransformEasing)
                )
            }
            launch {
                tabAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 160, easing = TabOpacityEaseOut)
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is MainEvent.Logout -> onLogoutRequest()
                else -> {}
            }
        }
    }

    val currentTabState by rememberUpdatedState(uiState.currentTab)
    val showBottomBarState by rememberUpdatedState(showBottomBar)
    val showNotificationConfigPageState by rememberUpdatedState(showNotificationConfigPage)

    val scrollTabToTop: (MainTab) -> Unit = { targetTab ->
        scope.launch {
            val targetListState = when (targetTab) {
                MainTab.Home -> homeListState
                MainTab.Monitor -> monitorState
                MainTab.Event -> eventListState
                MainTab.Setting -> settingListState
            }
            if (targetListState.firstVisibleItemIndex > 0 || targetListState.firstVisibleItemScrollOffset > 0) {
                try {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                } catch (_: Exception) {}
                if (targetListState.firstVisibleItemIndex > 12) {
                    targetListState.scrollToItem(8)
                }
                targetListState.animateScrollToItem(0, 0)
            }
        }
    }

    val switchToTab: (MainTab, Boolean) -> Unit = { targetTab, withHaptic ->
        if (targetTab != currentTabState) {
            showBottomBar = true
            if (withHaptic) {
                try {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                } catch (_: Exception) {}
            }
            viewModel.onTabSelected(targetTab)
        } else {
            scrollTabToTop(targetTab)
        }
    }

    // Khi đang ở tab khác Trang chủ (Sự kiện, Trực tiếp, Cài đặt), ấn Back sẽ quay về tab Trang chủ trước
    androidx.activity.compose.BackHandler(enabled = uiState.currentTab != MainTab.Home && showBottomBar && !showNotificationConfigPage) {
        switchToTab(MainTab.Home, false)
    }

    // Khi đang mở màn hình Cấu hình thông báo, ấn Back sẽ trượt đóng màn hình này từ trái sang phải
    androidx.activity.compose.BackHandler(enabled = showNotificationConfigPage) {
        showNotificationConfigPage = false
    }

    val edgeBufferPx = remember(density) { with(density) { 20.dp.toPx() } }
    val bottomNavExclusionPx = remember(density) { with(density) { 96.dp.toPx() } }
    val lockThresholdPx = remember(density) { with(density) { 10.dp.toPx() } }
    val minFlickDistPx = remember(density) { with(density) { 30.dp.toPx() } }
    val minDragDistPx = remember(density) { with(density) { 45.dp.toPx() } }
    val longDragDistPx = remember(density) { with(density) { 90.dp.toPx() } }
    val minFlickVelDpPerMs = 0.28f

    CompositionLocalProvider(com.tenli.oneview.ui.component.LocalMobileOverlayHost provides overlayHostState) {
        Scaffold(
            topBar = {
                AnimatedVisibility(visible = uiState.isOffline) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ErrorRed)
                            .padding(vertical = 8.dp, horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Không có kết nối mạng. Vui lòng kiểm tra lại.",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                            val widthPx = size.width.toFloat()
                            val heightPx = size.height.toFloat()
                            val startX = down.position.x
                            val startY = down.position.y

                            // Ignore touches near screen left/right edges (<20dp) for system back gesture,
                            // or in the bottom navigation bar zone, or when video is in fullscreen
                            if (!showBottomBarState ||
                                showNotificationConfigPageState ||
                                overlayHostState.hasOverlays ||
                                startX < edgeBufferPx ||
                                startX > widthPx - edgeBufferPx ||
                                startY > heightPx - bottomNavExclusionPx
                            ) {
                                return@awaitEachGesture
                            }

                            val startTime = System.currentTimeMillis()
                            var gestureDirection = 0 // 0 = undecided, 1 = horizontal, 2 = vertical
                            var lastX = startX
                            var lastY = startY

                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break

                                if (change.changedToUpIgnoreConsumed()) {
                                    lastX = change.position.x
                                    lastY = change.position.y
                                    if (gestureDirection == 1) {
                                        change.consume()
                                    }
                                    break
                                }

                                if (!change.pressed) break

                                lastX = change.position.x
                                lastY = change.position.y
                                val dx = lastX - startX
                                val dy = lastY - startY

                                if (gestureDirection == 0) {
                                    if (abs(dx) >= lockThresholdPx || abs(dy) >= lockThresholdPx) {
                                        gestureDirection = if (abs(dy) >= abs(dx)) 2 else 1
                                    }
                                }

                                if (gestureDirection == 2) {
                                    // Locked to vertical scrolling -> let child LazyColumn handle it
                                    break
                                } else if (gestureDirection == 1) {
                                    // Consume horizontal drag so child views don't trigger accidental clicks
                                    change.consume()
                                }
                            }

                            if (gestureDirection == 1) {
                                val diffX = lastX - startX
                                val diffY = lastY - startY
                                val absX = abs(diffX)
                                val absY = abs(diffY)
                                val elapsed = (System.currentTimeMillis() - startTime).coerceAtLeast(1L)
                                val absXDp = with(density) { absX.toDp().value }
                                val velocityDpPerMs = absXDp / elapsed.toFloat()

                                // Match MobileLayout.vue thresholds:
                                // 1. Flick: velocity > 0.28 dp/ms && distance > 30dp
                                // 2. Drag: distance >= 45dp && elapsed <= 700ms
                                // 3. Long drag: distance >= 90dp
                                val isFlick = velocityDpPerMs > minFlickVelDpPerMs && absX > minFlickDistPx
                                val isDrag = absX >= minDragDistPx && elapsed <= 700L
                                val isLongDrag = absX >= longDragDistPx

                                if ((isFlick || isDrag || isLongDrag) && absX >= absY * 1.25f) {
                                    val currIdx = OrderedMainTabs.indexOf(currentTabState)
                                    if (currIdx != -1) {
                                        if (diffX < 0f && currIdx < OrderedMainTabs.size - 1) {
                                            // Swipe right-to-left -> next tab
                                            switchToTab(OrderedMainTabs[currIdx + 1], true)
                                        } else if (diffX > 0f && currIdx > 0) {
                                            // Swipe left-to-right -> previous tab
                                            switchToTab(OrderedMainTabs[currIdx - 1], true)
                                        }
                                    }
                                }
                            }
                        }
                    }
            ) {
                // Keep-Alive (<keep-alive>) Tab Container + Hardware-Accelerated mode="out-in" Transition
                // Only compose displayedTab in the layout tree so inactive tabs (e.g. Monitor live player) never steal frame time during scroll
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding())
                        .zIndex(1f)
                        .graphicsLayer {
                            translationX = tabOffsetFraction.value * size.width
                            alpha = tabAlpha.value
                        }
                ) {
                    saveableStateHolder.SaveableStateProvider(key = displayedTab.route) {
                        when (displayedTab) {
                            MainTab.Home -> {
                                HomeScreen(
                                    viewModel = homeViewModel,
                                    listState = homeListState,
                                    onEventClick = { eventId ->
                                        val id = eventId.toIntOrNull()
                                        if (id != null) {
                                            onNavigateToEventDetail(id)
                                        }
                                    }
                                )
                            }

                            MainTab.Event -> {
                                com.tenli.oneview.ui.features.event.EventScreen(
                                    viewModel = eventViewModel,
                                    listState = eventListState,
                                    onEventClick = { eventId -> onNavigateToEventDetail(eventId) }
                                )
                            }

                            MainTab.Monitor -> {
                                com.tenli.oneview.ui.features.monitor.MonitorScreen(
                                    listState = monitorState,
                                    isTabActive = (uiState.currentTab == MainTab.Monitor),
                                    onEventClick = { eventId -> onNavigateToEventDetail(eventId) },
                                    onPlaybackClick = { videoLink, time, imageLink, cameraName ->
                                        onNavigateToPlaybackDetail(videoLink, time, imageLink, cameraName)
                                    },
                                    onFullscreenChange = { isFullscreen ->
                                        showBottomBar = !isFullscreen
                                    }
                                )
                            }

                            MainTab.Setting -> {
                                SettingScreen(
                                    listState = settingListState,
                                    onLogoutClick = { isChangePassword -> viewModel.logout(isChangePassword) },
                                    onNavigateToDetail = navigateToSettingTarget,
                                    onOpenNotificationConfig = { showNotificationConfigPage = true }
                                )
                            }
                        }
                    }
                }

                if (uiState.isBottomBarVisible && showBottomBar) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .zIndex(10f)
                    ) {
                        MainBottomNavigation(
                            currentTab = uiState.currentTab,
                            isCurrentTabScrolled = isCurrentTabScrolled,
                            onTabClick = { tab ->
                                switchToTab(tab, false)
                            },
                            onScrollToTop = { tab ->
                                scrollTabToTop(tab)
                            }
                        )
                    }
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
                        animationSpec = tween(
                            durationMillis = 280,
                            easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
                        )
                    ),
                    exit = slideOutHorizontally(
                        targetOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(
                            durationMillis = 240,
                            easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
                        )
                    )
                ) {
                    val themeMode by ThemeManager.themeModeFlow.collectAsStateWithLifecycle()
                    val isSystemDark = isSystemInDarkTheme()
                    val isDark = when (themeMode) {
                        0 -> false
                        1 -> true
                        else -> isSystemDark
                    }
                    MobileNotificationConfigScreen(
                        isDark = isDark,
                        onBackClick = { showNotificationConfigPage = false }
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

                // Global Top Popup Notification Host (.ant-message capsule island matching PWA notifications.css)
                com.tenli.oneview.ui.component.MobileTopPopupHost(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .zIndex(100f)
                )
            }
        }
    }
}

@Composable
fun SettingScreen(
    listState: androidx.compose.foundation.lazy.LazyListState = rememberLazyListState(),
    onLogoutClick: (Boolean) -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onOpenNotificationConfig: () -> Unit = {}
) {
    val themeMode by ThemeManager.themeModeFlow.collectAsStateWithLifecycle()
    var showLogoutSheet by remember { mutableStateOf(false) }
    var showLanguageSheet by remember { mutableStateOf(false) }
    var showThemeSheet by remember { mutableStateOf(false) }
    var showChangePasswordSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var currentLanguage by remember { mutableStateOf(LocaleManager.getLocale(context)) }

    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        0 -> false
        1 -> true
        else -> isSystemDark
    }

    val uriHandler = LocalUriHandler.current
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onBackground
    val sectionColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    val defaultMyPage = stringResource(id = R.string.setting_my_page)
    val userName: String = UserSession.userData?.name?.takeIf { it.isNotBlank() }
        ?: UserSession.savedUsername.takeIf { it.isNotBlank() }
        ?: defaultMyPage

    val themeLabel = when (themeMode) {
        0 -> "Sáng"
        1 -> "Tối"
        else -> "Hệ thống"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
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
            contentPadding = PaddingValues(top = 2.dp, bottom = 120.dp)
        ) {
            // 2 Top Square Cards (User Profile & Support) matching .top-cards-grid (padding: 10px, gap: 10px)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // User Card (#F97316 icon box)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(surfaceColor)
                            .padding(vertical = 16.dp, horizontal = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFF97316)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = LucideIcons.User,
                                contentDescription = userName,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = userName,
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor,
                            maxLines = 1
                        )
                    }

                    // Support Card (#0980EE icon box)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(surfaceColor)
                            .clickable { uriHandler.openUri("https://tenli.ai") }
                            .padding(vertical = 16.dp, horizontal = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(BrandAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = LucideIcons.Info,
                                contentDescription = stringResource(id = R.string.setting_support),
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = stringResource(id = R.string.setting_support),
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor,
                            maxLines = 1
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Section: Cài đặt chung
            item {
                Text(
                    text = stringResource(id = R.string.setting_general),
                    modifier = Modifier.padding(start = 14.dp, end = 10.dp, top = 2.dp, bottom = 8.dp),
                    fontSize = 14.5.sp,
                    color = sectionColor,
                    fontWeight = FontWeight.SemiBold
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(surfaceColor)
                ) {
                    ModernSettingItem(
                        icon = LucideIcons.Bell,
                        iconTint = Color(0xFFF59E0B),
                        title = stringResource(id = R.string.setting_notifications),
                        onClick = onOpenNotificationConfig,
                        showDivider = false
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Section: Giao diện
            item {
                Text(
                    text = "Giao diện",
                    modifier = Modifier.padding(start = 14.dp, end = 10.dp, top = 2.dp, bottom = 8.dp),
                    fontSize = 14.5.sp,
                    color = sectionColor,
                    fontWeight = FontWeight.SemiBold
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(surfaceColor)
                ) {
                    ModernSettingItem(
                        icon = LucideIcons.Globe,
                        iconTint = Color(0xFF6366F1),
                        title = stringResource(id = R.string.setting_language),
                        valueText = if (currentLanguage == "vi") stringResource(id = R.string.lang_vi) else stringResource(id = R.string.lang_en),
                        onClick = { showLanguageSheet = true }
                    )
                    ModernSettingItem(
                        icon = LucideIcons.Palette,
                        iconTint = Color(0xFFEC4899),
                        title = stringResource(id = R.string.setting_theme),
                        valueText = themeLabel,
                        onClick = { showThemeSheet = true },
                        showDivider = false
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Section: Tài khoản
            item {
                Text(
                    text = stringResource(id = R.string.setting_account_title),
                    modifier = Modifier.padding(start = 14.dp, end = 10.dp, top = 2.dp, bottom = 8.dp),
                    fontSize = 14.5.sp,
                    color = sectionColor,
                    fontWeight = FontWeight.SemiBold
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(surfaceColor)
                ) {
                    ModernSettingItem(
                        icon = LucideIcons.Lock,
                        iconTint = Color(0xFF10B981),
                        title = stringResource(id = R.string.acc_password_security),
                        onClick = { showChangePasswordSheet = true }
                    )
                    ModernSettingItem(
                        icon = LucideIcons.LogOut,
                        iconTint = Color(0xFFEF4444),
                        title = stringResource(id = R.string.setting_logout),
                        onClick = { showLogoutSheet = true },
                        titleColor = Color(0xFFEF4444),
                        showChevron = false,
                        showDivider = false
                    )
                }
            }
        }

        // Language Bottom Sheet matching MobileSettingsPage.vue
        if (showLanguageSheet) {
            MobileNativeBottomSheet(
                title = stringResource(id = R.string.setting_choose_language),
                isDark = isDark,
                onDismiss = { showLanguageSheet = false }
            ) {
                val cardBg = if (isDark) Color(0xFF242F41) else Color.White
                val dividerColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.06f)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(cardBg)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                LocaleManager.setLocale(context, "vi")
                                currentLanguage = "vi"
                                showLanguageSheet = false
                                (context as? android.app.Activity)?.recreate()
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🇻🇳", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(id = R.string.lang_vi),
                            fontSize = 15.sp,
                            fontWeight = if (currentLanguage == "vi") FontWeight.SemiBold else FontWeight.Normal,
                            color = if (currentLanguage == "vi") BrandAccent else textColor,
                            modifier = Modifier.weight(1f)
                        )
                        if (currentLanguage == "vi") {
                            Icon(
                                imageVector = LucideIcons.Check,
                                contentDescription = null,
                                tint = BrandAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                LocaleManager.setLocale(context, "en")
                                currentLanguage = "en"
                                showLanguageSheet = false
                                (context as? android.app.Activity)?.recreate()
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🇺🇸", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(id = R.string.lang_en),
                            fontSize = 15.sp,
                            fontWeight = if (currentLanguage == "en") FontWeight.SemiBold else FontWeight.Normal,
                            color = if (currentLanguage == "en") BrandAccent else textColor,
                            modifier = Modifier.weight(1f)
                        )
                        if (currentLanguage == "en") {
                            Icon(
                                imageVector = LucideIcons.Check,
                                contentDescription = null,
                                tint = BrandAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Theme Bottom Sheet matching MobileSettingsPage.vue
        if (showThemeSheet) {
            MobileNativeBottomSheet(
                title = stringResource(id = R.string.setting_theme),
                isDark = isDark,
                onDismiss = { showThemeSheet = false }
            ) {
                val cardBg = if (isDark) Color(0xFF242F41) else Color.White
                val dividerColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.06f)

                val options = listOf(
                    Triple(0, "Sáng", Pair(LucideIcons.Sun, Color(0xFFF59E0B))),
                    Triple(1, "Tối", Pair(LucideIcons.Moon, Color(0xFF6366F1))),
                    Triple(2, "Hệ thống", Pair(LucideIcons.Monitor, BrandAccent))
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(cardBg)
                ) {
                    options.forEachIndexed { idx, (modeValue, label, iconPair) ->
                        val (iconVec, badgeColor) = iconPair
                        val isSelected = themeMode == modeValue
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    ThemeManager.setThemeMode(modeValue)
                                    showThemeSheet = false
                                }
                                .padding(horizontal = 16.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(badgeColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = iconVec,
                                    contentDescription = label,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = label,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) BrandAccent else textColor,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = LucideIcons.Check,
                                    contentDescription = null,
                                    tint = BrandAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Password Change Sheet matching MobileSettingsPage.vue
        if (showChangePasswordSheet) {
            MobilePasswordChangeSheet(
                isDark = isDark,
                onDismiss = { showChangePasswordSheet = false },
                onSuccess = {
                    showChangePasswordSheet = false
                    onLogoutClick(true)
                }
            )
        }

        // Logout Confirmation Sheet matching MobileSettingsPage.vue
        if (showLogoutSheet) {
            MobileNativeBottomSheet(
                title = stringResource(id = R.string.dialog_logout_title),
                isDark = isDark,
                onDismiss = { showLogoutSheet = false }
            ) {
                val cardBg = if (isDark) Color(0xFF242F41) else Color.White
                val subTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                val cancelBtnBg = if (isDark) Color(0xFF263449) else Color.White

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(cardBg)
                        .padding(horizontal = 20.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFEF4444).copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = LucideIcons.LogOut,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Bạn muốn đăng xuất?",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Phiên làm việc hiện tại sẽ kết thúc và bạn cần đăng nhập lại để tiếp tục sử dụng.",
                        fontSize = 14.sp,
                        color = subTextColor,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(cancelBtnBg)
                            .clickable { showLogoutSheet = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.dialog_btn_cancel),
                            color = textColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFEF4444))
                            .clickable {
                                showLogoutSheet = false
                                onLogoutClick(false)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.setting_logout),
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MobileNativeBottomSheet(
    title: String,
    isDark: Boolean,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val sheetBg = if (isDark) Color(0xFF172033) else Color(0xFFF2F2F7)
    val closeBtnBg = if (isDark) Color(0xFF263449) else Color(0xFFE2E8F0)
    val titleColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF1E293B)

    com.tenli.oneview.ui.component.MobileFullScreenOverlay(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    ),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                color = sheetBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 16.dp)
                        .padding(top = 10.dp, bottom = 20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(36.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.15f))
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = titleColor
                        )
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(closeBtnBg)
                                .clickable(onClick = onDismiss),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = LucideIcons.X,
                                contentDescription = "Đóng",
                                tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    content()
                }
            }
        }
    }
}

@Composable
private fun MobileNotificationConfigScreen(
    isDark: Boolean,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val bsApi = remember { LoginAuthClient.create(BsApi::class.java) }

    fun checkOsNotificationPermission(): Boolean {
        val enabledByManager = NotificationManagerCompat.from(context).areNotificationsEnabled()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            enabledByManager &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            enabledByManager
        }
    }

    var hasOsPermission by remember { mutableStateOf(checkOsNotificationPermission()) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasOsPermission = checkOsNotificationPermission()
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

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasOsPermission = checkOsNotificationPermission()
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

    val hasCachedPref = remember { GlobalData.preferences.contains("push_notifications_enabled") }
    var isLoading by remember { mutableStateOf(!hasCachedPref) }
    var isSaving by remember { mutableStateOf(false) }
    var pushEnabled by remember {
        mutableStateOf(GlobalData.preferences.getBoolean("push_notifications_enabled", true))
    }

    LaunchedEffect(Unit) {
        try {
            val resp = bsApi.getUserNotificationSettings()
            if (resp.isSuccessful) {
                val settings = resp.body()
                if (settings != null) {
                    pushEnabled = settings.global.enabled
                    GlobalData.preferences.edit()
                        .putBoolean("push_notifications_enabled", settings.global.enabled)
                        .apply()
                }
            }
        } catch (_: Exception) {
        } finally {
            isLoading = false
        }
    }

    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val titleColor = MaterialTheme.colorScheme.onBackground
    val subTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

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
        // Header matching .drawer-header in MobileNotificationConfigModal.vue
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .background(backgroundColor)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onBackClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = LucideIcons.ChevronLeft,
                    contentDescription = "Quay lại",
                    tint = titleColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = "Cấu hình thông báo",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor
            )
            Spacer(modifier = Modifier.width(34.dp))
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = BrandAccent,
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 2.5.dp
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .navigationBarsPadding()
                    .padding(horizontal = 10.dp, vertical = 10.dp)
            ) {
                // 1. Device Permission Status Card matching MobileNotificationConfigModal.vue
                Text(
                    text = "Quyền thông báo trên thiết bị",
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = subTextColor,
                    modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(surfaceColor)
                ) {
                    if (hasOsPermission) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF10B981)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = LucideIcons.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Đã cấp quyền thông báo",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = titleColor
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Thiết bị này đã sẵn sàng nhận cảnh báo.",
                                    fontSize = 12.5.sp,
                                    color = subTextColor,
                                    lineHeight = 17.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        Color(0xFF10B981).copy(alpha = if (isDark) 0.15f else 0.12f)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Đang bật",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDark) Color(0xFF10B981) else Color(0xFF059669)
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFF59E0B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = LucideIcons.Bell,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Chưa cấp quyền trên thiết bị",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = titleColor
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Cần cấp quyền thông báo để nhận cảnh báo khi có sự kiện mới.",
                                    fontSize = 12.5.sp,
                                    color = subTextColor,
                                    lineHeight = 17.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .height(30.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BrandAccent)
                                    .clickable(onClick = requestOrOpenPermission)
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Cấp quyền ngay",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. System General Setting matching MobileNotificationConfigModal.vue
                Text(
                    text = "Cài đặt chung",
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = subTextColor,
                    modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(surfaceColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF59E0B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = LucideIcons.Bell,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Cho phép nhận thông báo",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Normal,
                                color = titleColor
                            )
                            if (!hasOsPermission && pushEnabled) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "⚠️ Thiết bị này chưa cấp quyền nên chưa thể nhận thông báo",
                                    fontSize = 12.sp,
                                    color = Color(0xFFF59E0B),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Switch(
                            checked = pushEnabled,
                            enabled = !isSaving,
                            onCheckedChange = { nextVal ->
                                if (nextVal && !hasOsPermission) {
                                    requestOrOpenPermission()
                                }
                                pushEnabled = nextVal
                                isSaving = true
                                GlobalData.preferences.edit()
                                    .putBoolean("push_notifications_enabled", nextVal)
                                    .apply()
                                scope.launch {
                                    try {
                                        val resp = bsApi.updateUserNotificationSettings(
                                            UserNotificationSettingsModel(
                                                global = UserNotificationGlobalSetting(enabled = nextVal)
                                            )
                                        )
                                        if (resp.isSuccessful) {
                                            if (nextVal) {
                                                if (hasOsPermission) {
                                                    com.tenli.oneview.ui.component.MobileTopPopupManager.success(
                                                        "Đã bật thông báo thành công!"
                                                    )
                                                } else {
                                                    com.tenli.oneview.ui.component.MobileTopPopupManager.warning(
                                                        "Bạn chưa cấp quyền thông báo trên thiết bị."
                                                    )
                                                }
                                            } else {
                                                com.tenli.oneview.ui.component.MobileTopPopupManager.info(
                                                    "Đã tắt nhận thông báo."
                                                )
                                            }
                                        } else {
                                            com.tenli.oneview.ui.component.MobileTopPopupManager.error(
                                                "Không thể lưu cấu hình thông báo"
                                            )
                                        }
                                    } catch (_: Exception) {
                                        com.tenli.oneview.ui.component.MobileTopPopupManager.error(
                                            "Không thể lưu cấu hình thông báo"
                                        )
                                    } finally {
                                        isSaving = false
                                    }
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = BrandAccent
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MobilePasswordChangeSheet(
    isDark: Boolean,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val vmsApi = remember { LoginAuthClient.create(VmsApi::class.java) }
    val keyboardController = LocalSoftwareKeyboardController.current

    val oldPasswordFocusRequester = remember { FocusRequester() }
    val newPasswordFocusRequester = remember { FocusRequester() }
    val confirmPasswordFocusRequester = remember { FocusRequester() }

    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showOld by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val cardBg = if (isDark) Color(0xFF242F41) else Color.White
    val titleColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF1E293B)
    val subTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val dividerColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE2E8F0)
    val cancelBtnBg = if (isDark) Color(0xFF263449) else Color.White

    LaunchedEffect(Unit) {
        delay(80)
        try {
            oldPasswordFocusRequester.requestFocus()
            keyboardController?.show()
        } catch (_: Exception) {
        }
    }

    MobileNativeBottomSheet(
        title = stringResource(id = R.string.acc_password_security),
        isDark = isDark,
        onDismiss = onDismiss
    ) {
        if (!errorMsg.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(ErrorRed.copy(alpha = 0.12f))
                    .border(1.dp, ErrorRed.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = errorMsg!!,
                    color = ErrorRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(cardBg)
        ) {
            PasswordFieldRow(
                badgeColor = BrandAccent,
                badgeIcon = LucideIcons.Lock,
                placeholder = "Mật khẩu hiện tại",
                value = oldPassword,
                onValueChange = {
                    oldPassword = it
                    errorMsg = null
                },
                isVisible = showOld,
                onToggleVisible = { showOld = !showOld },
                textColor = titleColor,
                placeholderColor = subTextColor,
                focusRequester = oldPasswordFocusRequester,
                imeAction = ImeAction.Next,
                onImeAction = {
                    try {
                        newPasswordFocusRequester.requestFocus()
                    } catch (_: Exception) {
                    }
                }
            )
            HorizontalDivider(
                thickness = 0.7.dp,
                color = dividerColor,
                modifier = Modifier.padding(horizontal = 14.dp)
            )
            PasswordFieldRow(
                badgeColor = Color(0xFFF59E0B),
                badgeIcon = LucideIcons.ShieldAlert,
                placeholder = "Mật khẩu mới",
                value = newPassword,
                onValueChange = {
                    newPassword = it
                    errorMsg = null
                },
                isVisible = showNew,
                onToggleVisible = { showNew = !showNew },
                textColor = titleColor,
                placeholderColor = subTextColor,
                focusRequester = newPasswordFocusRequester,
                imeAction = ImeAction.Next,
                onImeAction = {
                    try {
                        confirmPasswordFocusRequester.requestFocus()
                    } catch (_: Exception) {
                    }
                }
            )
            HorizontalDivider(
                thickness = 0.7.dp,
                color = dividerColor,
                modifier = Modifier.padding(horizontal = 14.dp)
            )
            PasswordFieldRow(
                badgeColor = Color(0xFF10B981),
                badgeIcon = LucideIcons.CheckCircle,
                placeholder = "Xác nhận mật khẩu mới",
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    errorMsg = null
                },
                isVisible = showConfirm,
                onToggleVisible = { showConfirm = !showConfirm },
                textColor = titleColor,
                placeholderColor = subTextColor,
                focusRequester = confirmPasswordFocusRequester,
                imeAction = ImeAction.Done,
                onImeAction = {
                    keyboardController?.hide()
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(cancelBtnBg)
                    .clickable(enabled = !isSubmitting) { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.dialog_btn_cancel),
                    color = titleColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BrandAccent)
                    .clickable(enabled = !isSubmitting) {
                        if (oldPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
                            errorMsg = "Vui lòng nhập đầy đủ thông tin"
                            return@clickable
                        }
                        if (newPassword.length < 6) {
                            errorMsg = "Mật khẩu mới phải có ít nhất 6 ký tự"
                            return@clickable
                        }
                        if (newPassword != confirmPassword) {
                            errorMsg = "Mật khẩu xác nhận không khớp"
                            return@clickable
                        }
                        val userId = UserSession.userData?.id
                        if (userId == null) {
                            errorMsg = "Không tìm thấy thông tin người dùng"
                            return@clickable
                        }
                        isSubmitting = true
                        scope.launch {
                            try {
                                val resp = vmsApi.changePassword(
                                    ChangePasswordModel(
                                        userId = userId,
                                        oldPassword = oldPassword,
                                        newPassword = newPassword
                                    )
                                )
                                if (resp.isSuccessful) {
                                    com.tenli.oneview.ui.component.MobileTopPopupManager.success(
                                        "Đổi mật khẩu thành công!"
                                    )
                                    onSuccess()
                                } else {
                                    errorMsg = "Mật khẩu hiện tại không đúng (${resp.code()})"
                                }
                            } catch (e: Exception) {
                                errorMsg = e.message ?: "Có lỗi xảy ra khi đổi mật khẩu"
                            } finally {
                                isSubmitting = false
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Xác nhận",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
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
    textColor: Color,
    placeholderColor: Color,
    focusRequester: FocusRequester,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {}
) {
    val keyboardController = LocalSoftwareKeyboardController.current
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
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(badgeColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = badgeIcon,
                contentDescription = placeholder,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = placeholderColor,
                    fontSize = 14.sp
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = textColor,
                    fontSize = 14.sp
                ),
                visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = imeAction
                ),
                keyboardActions = KeyboardActions(
                    onNext = { onImeAction() },
                    onDone = { onImeAction() }
                ),
                cursorBrush = SolidColor(BrandAccent),
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
                .size(20.dp)
                .clickable(onClick = onToggleVisible)
        )
    }
}

@Composable
fun ModernSettingItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    onClick: () -> Unit,
    titleColor: Color = MaterialTheme.colorScheme.onBackground,
    valueText: String? = null,
    showChevron: Boolean = true,
    showDivider: Boolean = false
) {
    val themeMode by ThemeManager.themeModeFlow.collectAsStateWithLifecycle()
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        0 -> false
        1 -> true
        else -> isSystemDark
    }
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Squircle colored icon badge matching MobileSettingsPage.vue
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = titleColor,
                modifier = Modifier.weight(1f)
            )
            if (valueText != null) {
                Text(
                    text = valueText,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Normal,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    modifier = Modifier.padding(end = 6.dp)
                )
            }
            if (showChevron) {
                Icon(
                    imageVector = LucideIcons.ChevronRight,
                    contentDescription = null,
                    tint = if (isDark) Color(0xFF64748B) else Color(0xFFCBD5E1),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun ThemeModeButton(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val themeMode by ThemeManager.themeModeFlow.collectAsStateWithLifecycle()
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        0 -> false
        1 -> true
        else -> isSystemDark
    }
    val selectedBg = if (isDark) MaterialTheme.colorScheme.primary else Color.White
    val selectedTint = if (isDark) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
    val unselectedTint = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF94A3B8)

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) selectedBg else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) selectedTint else unselectedTint,
            modifier = Modifier.size(20.dp)
        )
    }
}
