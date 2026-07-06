package com.tenli.oneview.ui.features.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.tenli.oneview.ui.features.home.HomeScreen
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.tenli.oneview.R

@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel(factory = MainViewModel.Factory),
    onLogoutRequest: () -> Unit,
    onNavigateToEventDetail: (Int) -> Unit = {},
    onNavigateToPlaybackDetail: (videoLink: String, time: String, imageLink: String, cameraName: String) -> Unit = { _, _, _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val navigateToSettingTarget: (String) -> Unit = { target ->
        viewModel.onTabSelected(MainTab.Setting)

        navController.navigate("${MainTab.Setting.route}?target=$target") {
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = false
        }
    }

    val scope = rememberCoroutineScope()
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

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is MainEvent.Logout -> onLogoutRequest()
                else -> {}
            }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(currentRoute) {
        currentRoute?.let { route ->
            val tab = MainTab.values().find { route.startsWith(it.route) }
            if (tab != null && tab != uiState.currentTab) {
                viewModel.onTabSelected(tab)
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (uiState.isBottomBarVisible && showBottomBar) {
                MainBottomNavigation(
                    currentTab = uiState.currentTab,
                    isCurrentTabScrolled = isCurrentTabScrolled,
                    onTabClick = { tab ->
                        showBottomBar = true
                        viewModel.onTabSelected(tab)
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
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
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MainTab.Home.route,
            modifier = Modifier.padding(if (showBottomBar) innerPadding else PaddingValues(0.dp)),
            enterTransition = {
                androidx.compose.animation.EnterTransition.None
            },
            exitTransition = {
                androidx.compose.animation.ExitTransition.None
            }
        ) {
            composable(MainTab.Home.route) {
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

            composable(MainTab.Monitor.route) {
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

            composable(MainTab.Event.route) {
                com.tenli.oneview.ui.features.event.EventScreen(
                    listState = eventListState,
                    onEventClick = { eventId -> onNavigateToEventDetail(eventId) }
                )
            }

            composable(
                route = "${MainTab.Setting.route}?target={target}",
                arguments = listOf(
                    navArgument("target") {
                        nullable = true
                        defaultValue = null
                    }
                )
            ) {
                SettingScreen(
                    onLogoutClick = { isChangePassword -> viewModel.logout(isChangePassword) },
                    onNavigateToDetail = navigateToSettingTarget
                )
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    onLogoutClick: (Boolean) -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val themeMode by com.tenli.oneview.ui.theme.ThemeManager.themeModeFlow.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    var currentLanguage by remember { mutableStateOf(com.tenli.oneview.ui.utils.LocaleManager.getLocale(context)) }
    
    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isDark = when (themeMode) {
        0 -> false
        1 -> true
        else -> isSystemDark
    }
    
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    val backgroundColor = if (isDark) MaterialTheme.colorScheme.background else androidx.compose.ui.graphics.Color(0xFFF2F4F8)
    val surfaceColor = if (isDark) MaterialTheme.colorScheme.surface else androidx.compose.ui.graphics.Color.White
    val textColor = MaterialTheme.colorScheme.onBackground
    val sectionColor = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else androidx.compose.ui.graphics.Color(0xFF64748B)
    
    val userName = com.tenli.oneview.data.local.UserSession.userData?.name?.takeIf { it.isNotBlank() } ?: stringResource(id = R.string.setting_my_page)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.lbl_settings),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(surfaceColor)
                            .clickable { }
                            .padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = stringResource(id = R.string.setting_profile),
                            tint = com.tenli.oneview.ui.theme.BrandPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = textColor
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(surfaceColor)
                            .clickable { uriHandler.openUri("https://tenli.ai") }
                            .padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(id = R.string.setting_support),
                            tint = androidx.compose.ui.graphics.Color(0xFF3B82F6),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(id = R.string.setting_support),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = textColor
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Text(
                    text = stringResource(id = R.string.setting_general),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleSmall,
                    color = sectionColor,
                    fontWeight = FontWeight.Medium
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(surfaceColor)
                ) {
                    ModernSettingItem(
                        icon = Icons.Default.Settings,
                        iconTint = androidx.compose.ui.graphics.Color(0xFF3B82F6),
                        title = stringResource(id = R.string.setting_general_config),
                        onClick = { onNavigateToDetail("general") }
                    )
                    ModernSettingItem(
                        icon = Icons.Default.Notifications,
                        iconTint = androidx.compose.ui.graphics.Color(0xFFF59E0B),
                        title = stringResource(id = R.string.setting_notifications),
                        onClick = { onNavigateToDetail("notifications") },
                        showDivider = false
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            item {
                Text(
                    text = stringResource(id = R.string.setting_personalization),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleSmall,
                    color = sectionColor,
                    fontWeight = FontWeight.Medium
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(surfaceColor)
                ) {
                    ModernSettingItem(
                        icon = Icons.Default.Language,
                        iconTint = androidx.compose.ui.graphics.Color(0xFF6366F1),
                        title = stringResource(id = R.string.setting_language),
                        valueText = if (currentLanguage == "vi") stringResource(id = R.string.lang_vi) else stringResource(id = R.string.lang_en),
                        onClick = { showLanguageDialog = true }
                    )
                    
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { }
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = androidx.compose.ui.graphics.Color(0xFFEC4899),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = stringResource(id = R.string.setting_theme),
                                style = MaterialTheme.typography.bodyLarge,
                                color = textColor,
                                modifier = Modifier.weight(1f)
                            )
                            
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isDark) MaterialTheme.colorScheme.surfaceVariant else androidx.compose.ui.graphics.Color(0xFFF1F5F9))
                                    .padding(4.dp)
                            ) {
                                ThemeModeButton(
                                    icon = Icons.Default.LightMode,
                                    isSelected = themeMode == 0,
                                    onClick = { com.tenli.oneview.ui.theme.ThemeManager.setThemeMode(0) }
                                )
                                ThemeModeButton(
                                    icon = Icons.Default.DarkMode,
                                    isSelected = themeMode == 1,
                                    onClick = { com.tenli.oneview.ui.theme.ThemeManager.setThemeMode(1) }
                                )
                                ThemeModeButton(
                                    icon = Icons.Default.Monitor,
                                    isSelected = themeMode == 2,
                                    onClick = { com.tenli.oneview.ui.theme.ThemeManager.setThemeMode(2) }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            item {
                Text(
                    text = stringResource(id = R.string.setting_account_title),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleSmall,
                    color = sectionColor,
                    fontWeight = FontWeight.Medium
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(surfaceColor)
                ) {
                    ModernSettingItem(
                        icon = Icons.Default.VpnKey,
                        iconTint = androidx.compose.ui.graphics.Color(0xFF14B8A6),
                        title = stringResource(id = R.string.acc_password_security),
                        onClick = { showChangePasswordDialog = true }
                    )
                    ModernSettingItem(
                        icon = Icons.Default.Logout,
                        iconTint = androidx.compose.ui.graphics.Color(0xFFEF4444),
                        title = stringResource(id = R.string.setting_logout),
                        onClick = { showLogoutDialog = true },
                        titleColor = androidx.compose.ui.graphics.Color(0xFFEF4444),
                        showChevron = false,
                        showDivider = false
                    )
                }
            }
        }

        if (showLogoutDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = {
                    Text(text = stringResource(id = R.string.dialog_logout_title), fontWeight = FontWeight.Bold)
                },
                text = {
                    Text(text = stringResource(id = R.string.dialog_logout_msg))
                },
                confirmButton = {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            showLogoutDialog = false
                            onLogoutClick(false)
                        }
                    ) {
                        Text(stringResource(id = R.string.setting_logout), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(
                        onClick = { showLogoutDialog = false }
                    ) {
                        Text(stringResource(id = R.string.dialog_btn_cancel), color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            )
        }
        
        if (showLanguageDialog) {
            androidx.compose.material3.ModalBottomSheet(
                onDismissRequest = { showLanguageDialog = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.setting_choose_language),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                com.tenli.oneview.ui.utils.LocaleManager.setLocale(context, "vi")
                                currentLanguage = "vi"
                                showLanguageDialog = false
                                (context as? android.app.Activity)?.recreate()
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.RadioButton(
                            selected = currentLanguage == "vi",
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = stringResource(id = R.string.lang_vi), style = MaterialTheme.typography.bodyLarge)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                com.tenli.oneview.ui.utils.LocaleManager.setLocale(context, "en")
                                currentLanguage = "en"
                                showLanguageDialog = false
                                (context as? android.app.Activity)?.recreate()
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.RadioButton(
                            selected = currentLanguage == "en",
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = stringResource(id = R.string.lang_en), style = MaterialTheme.typography.bodyLarge)
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        if (showChangePasswordDialog) {
            val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
            androidx.compose.material3.ModalBottomSheet(
                onDismissRequest = { showChangePasswordDialog = false },
                sheetState = sheetState,
                containerColor = backgroundColor
            ) {
                com.tenli.oneview.ui.features.auth.password.ChangePasswordScreen(
                    onBack = { showChangePasswordDialog = false },
                    onPasswordChangedSuccess = {
                        showChangePasswordDialog = false
                        onLogoutClick(true)
                    }
                )
            }
        }
    }
}

@Composable
fun ModernSettingItem(
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    title: String,
    onClick: () -> Unit,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onBackground,
    valueText: String? = null,
    showChevron: Boolean = true,
    showDivider: Boolean = true
) {
    val themeMode by com.tenli.oneview.ui.theme.ThemeManager.themeModeFlow.collectAsStateWithLifecycle()
    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
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
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = titleColor,
                modifier = Modifier.weight(1f)
            )
            if (valueText != null) {
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else androidx.compose.ui.graphics.Color(0xFF64748B),
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            if (showChevron) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else androidx.compose.ui.graphics.Color(0xFFCBD5E1),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        if (showDivider) {
            Divider(
                modifier = Modifier.padding(start = 60.dp, end = 20.dp),
                color = if (isDark) MaterialTheme.colorScheme.surfaceVariant else androidx.compose.ui.graphics.Color(0xFFF1F5F9),
                thickness = 1.dp
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
    val themeMode by com.tenli.oneview.ui.theme.ThemeManager.themeModeFlow.collectAsStateWithLifecycle()
    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isDark = when (themeMode) {
        0 -> false
        1 -> true
        else -> isSystemDark
    }
    val selectedBg = if (isDark) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.White
    val selectedTint = if (isDark) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
    val unselectedTint = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else androidx.compose.ui.graphics.Color(0xFF94A3B8)

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) selectedBg else androidx.compose.ui.graphics.Color.Transparent)
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