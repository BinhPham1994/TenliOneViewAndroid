package com.tenli.oneview.ui.features.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
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
import kotlinx.coroutines.launch
import kotlin.math.abs

private val OrderedMainTabs = listOf(
    MainTab.Home,
    MainTab.Event,
    MainTab.Monitor,
    MainTab.Setting
)

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

    val homeListState = rememberLazyListState()
    val monitorState = rememberLazyListState()
    val eventListState = rememberLazyListState()
    val settingListState = rememberLazyListState()

    val isHomeScrolled by remember { derivedStateOf { homeListState.firstVisibleItemIndex > 0 } }
    val isMonitorScrolled by remember { derivedStateOf { monitorState.firstVisibleItemIndex > 0 } }
    val isEventScrolled by remember { derivedStateOf { eventListState.firstVisibleItemIndex > 0 } }
    val isSettingScrolled by remember { derivedStateOf { settingListState.firstVisibleItemIndex > 0 } }

    val isCurrentTabScrolled = when (uiState.currentTab) {
        MainTab.Home -> isHomeScrolled
        MainTab.Monitor -> isMonitorScrolled
        MainTab.Event -> isEventScrolled
        MainTab.Setting -> isSettingScrolled
    }

    LaunchedEffect(uiState.currentTab) {
        if (uiState.currentTab != MainTab.Monitor) {
            showBottomBar = true
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

    val switchToTab: (MainTab, Boolean) -> Unit = { targetTab, withHaptic ->
        if (targetTab != currentTabState) {
            showBottomBar = true
            if (withHaptic) {
                try {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                } catch (_: Exception) {}
            }
            viewModel.onTabSelected(targetTab)
        }
    }

    val edgeBufferPx = remember(density) { with(density) { 20.dp.toPx() } }
    val bottomNavExclusionPx = remember(density) { with(density) { 96.dp.toPx() } }
    val lockThresholdPx = remember(density) { with(density) { 10.dp.toPx() } }
    val minFlickDistPx = remember(density) { with(density) { 30.dp.toPx() } }
    val minDragDistPx = remember(density) { with(density) { 45.dp.toPx() } }
    val longDragDistPx = remember(density) { with(density) { 90.dp.toPx() } }
    val minFlickVelDpPerMs = 0.28f

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
                .padding(top = innerPadding.calculateTopPadding())
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
            AnimatedContent(
                targetState = uiState.currentTab,
                modifier = Modifier.fillMaxSize(),
                transitionSpec = {
                    val fromIdx = OrderedMainTabs.indexOf(initialState)
                    val toIdx = OrderedMainTabs.indexOf(targetState)
                    val isForward = toIdx >= fromIdx

                    // Matching MobileLayout.vue .mobile-tab-slide-left / .mobile-tab-slide-right
                    val enterSlide = slideInHorizontally(
                        animationSpec = tween(
                            durationMillis = 220,
                            easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
                        ),
                        initialOffsetX = { fullWidth ->
                            if (isForward) (fullWidth * 0.35f).toInt() else -(fullWidth * 0.35f).toInt()
                        }
                    ) + fadeIn(
                        animationSpec = tween(durationMillis = 170)
                    )

                    val exitSlide = slideOutHorizontally(
                        animationSpec = tween(
                            durationMillis = 120,
                            easing = CubicBezierEasing(0.35f, 0f, 0.7f, 0.2f)
                        ),
                        targetOffsetX = { fullWidth ->
                            if (isForward) -(fullWidth * 0.24f).toInt() else (fullWidth * 0.24f).toInt()
                        }
                    ) + fadeOut(
                        animationSpec = tween(durationMillis = 100)
                    )

                    (enterSlide togetherWith exitSlide).using(SizeTransform(clip = false))
                },
                label = "mainTabSwipeTransition"
            ) { tab ->
                saveableStateHolder.SaveableStateProvider(key = tab.route) {
                    when (tab) {
                        MainTab.Home -> {
                            HomeScreen(
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
                                listState = eventListState,
                                onEventClick = { eventId -> onNavigateToEventDetail(eventId) }
                            )
                        }

                        MainTab.Monitor -> {
                            com.tenli.oneview.ui.features.monitor.MonitorScreen(
                                listState = monitorState,
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
                                onNavigateToDetail = navigateToSettingTarget
                            )
                        }
                    }
                }
            }

            if (uiState.isBottomBarVisible && showBottomBar) {
                Box(
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    MainBottomNavigation(
                        currentTab = uiState.currentTab,
                        isCurrentTabScrolled = isCurrentTabScrolled,
                        onTabClick = { tab ->
                            switchToTab(tab, false)
                        },
                        onScrollToTop = {
                            scope.launch {
                                val currentState = when (uiState.currentTab) {
                                    MainTab.Home -> homeListState
                                    MainTab.Monitor -> monitorState
                                    MainTab.Event -> eventListState
                                    MainTab.Setting -> settingListState
                                }
                                if (currentState.firstVisibleItemIndex > 15) {
                                    currentState.scrollToItem(10)
                                }
                                currentState.animateScrollToItem(0)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingScreen(
    listState: androidx.compose.foundation.lazy.LazyListState = rememberLazyListState(),
    onLogoutClick: (Boolean) -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val themeMode by ThemeManager.themeModeFlow.collectAsStateWithLifecycle()
    var showLogoutSheet by remember { mutableStateOf(false) }
    var showLanguageSheet by remember { mutableStateOf(false) }
    var showThemeSheet by remember { mutableStateOf(false) }
    var showChangePasswordSheet by remember { mutableStateOf(false) }
    var showNotificationConfigSheet by remember { mutableStateOf(false) }

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
                            fontSize = 15.sp,
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
                            fontSize = 15.sp,
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
                    fontSize = 14.sp,
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
                        onClick = { showNotificationConfigSheet = true },
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
                    fontSize = 14.sp,
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
                    fontSize = 14.sp,
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
                            fontWeight = FontWeight.Medium,
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
                    HorizontalDivider(color = dividerColor, thickness = 0.5.dp, modifier = Modifier.padding(start = 48.dp))
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
                            fontWeight = FontWeight.Medium,
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
                                fontWeight = FontWeight.Medium,
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
                        if (idx < options.lastIndex) {
                            HorizontalDivider(color = dividerColor, thickness = 0.5.dp, modifier = Modifier.padding(start = 58.dp))
                        }
                    }
                }
            }
        }

        // Notification Config Sheet matching MobileNotificationConfigModal.vue
        if (showNotificationConfigSheet) {
            MobileNotificationConfigSheet(
                isDark = isDark,
                onDismiss = { showNotificationConfigSheet = false }
            )
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
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
private fun MobileNotificationConfigSheet(
    isDark: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val bsApi = remember { LoginAuthClient.create(BsApi::class.java) }

    var hasOsPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasOsPermission = granted
    }

    var isLoading by remember { mutableStateOf(true) }
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

    MobileNativeBottomSheet(
        title = "Cấu hình thông báo",
        isDark = isDark,
        onDismiss = onDismiss
    ) {
        val cardBg = if (isDark) Color(0xFF242F41) else Color.White
        val titleColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF1E293B)
        val subTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
        val dividerColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.06f)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(cardBg)
        ) {
            // Row 1: Push Notification Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF59E0B)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = LucideIcons.Bell,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Nhận thông báo sự kiện",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = titleColor
                    )
                    Text(
                        text = "Thông báo đẩy khi phát hiện sự kiện AI mới",
                        fontSize = 12.sp,
                        color = subTextColor
                    )
                }
                if (isLoading || isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = BrandAccent
                    )
                } else {
                    Switch(
                        checked = pushEnabled,
                        onCheckedChange = { nextVal ->
                            pushEnabled = nextVal
                            isSaving = true
                            GlobalData.preferences.edit()
                                .putBoolean("push_notifications_enabled", nextVal)
                                .apply()
                            scope.launch {
                                try {
                                    bsApi.updateUserNotificationSettings(
                                        UserNotificationSettingsModel(
                                            global = UserNotificationGlobalSetting(enabled = nextVal)
                                        )
                                    )
                                } catch (_: Exception) {
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

            HorizontalDivider(color = dividerColor, thickness = 0.5.dp, modifier = Modifier.padding(start = 60.dp))

            // Row 2: Device OS Permission Status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !hasOsPermission) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (hasOsPermission) Color(0xFF10B981) else BrandAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = LucideIcons.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Quyền thông báo thiết bị",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = titleColor
                    )
                    Text(
                        text = if (hasOsPermission) "Đã cấp quyền thông báo" else "Chạm để cấp quyền thông báo trên thiết bị",
                        fontSize = 12.sp,
                        color = if (hasOsPermission) Color(0xFF10B981) else BrandAccent
                    )
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
    val dividerColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.06f)
    val cancelBtnBg = if (isDark) Color(0xFF263449) else Color.White

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
                placeholderColor = subTextColor
            )
            HorizontalDivider(color = dividerColor, thickness = 0.5.dp, modifier = Modifier.padding(start = 56.dp))
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
                placeholderColor = subTextColor
            )
            HorizontalDivider(color = dividerColor, thickness = 0.5.dp, modifier = Modifier.padding(start = 56.dp))
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
                placeholderColor = subTextColor
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
                                    Toast.makeText(context, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show()
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
    placeholderColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                cursorBrush = SolidColor(BrandAccent),
                modifier = Modifier.fillMaxWidth()
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
    showDivider: Boolean = true
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
                fontWeight = FontWeight.Medium,
                color = titleColor,
                modifier = Modifier.weight(1f)
            )
            if (valueText != null) {
                Text(
                    text = valueText,
                    fontSize = 14.sp,
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
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 62.dp, end = 16.dp),
                color = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.06f),
                thickness = 0.5.dp
            )
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
