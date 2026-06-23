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
import androidx.compose.material.icons.filled.Group
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
                    onLogoutClick = { viewModel.logout() },
                    onNavigateToDetail = navigateToSettingTarget
                )
            }
        }
    }
}

@Composable
fun SettingScreen(
    onLogoutClick: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    var themeMode by remember { mutableStateOf(2) } // 0: Light, 1: Dark, 2: System
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
        item {
            // Profile Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar",
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Người dùng",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Quản trị viên",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        item {
            Text(
                text = "Cài đặt chung",
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            SettingItem(
                icon = Icons.Default.Settings,
                title = "Cấu hình chung",
                onClick = { onNavigateToDetail("general") }
            )
            SettingItem(
                icon = Icons.Default.Notifications,
                title = "Thông báo hệ thống",
                onClick = { onNavigateToDetail("notifications") }
            )
            SettingItem(
                icon = Icons.Default.AutoMode,
                title = "Kịch bản tự động",
                onClick = { onNavigateToDetail("automation") }
            )
            SettingItem(
                icon = Icons.Default.Group,
                title = "Quản lý người dùng",
                onClick = { onNavigateToDetail("users") }
            )
        }
        
        item {
            Divider(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp))
            Text(
                text = "Cá nhân hóa",
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            SettingItem(
                icon = Icons.Default.Language,
                title = "Ngôn ngữ",
                onClick = { onNavigateToDetail("language") }
            )
            
            // Theme toggle row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { }
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Giao diện",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(4.dp)
                ) {
                    ThemeModeButton(
                        icon = Icons.Default.LightMode,
                        isSelected = themeMode == 0,
                        onClick = { themeMode = 0 }
                    )
                    ThemeModeButton(
                        icon = Icons.Default.DarkMode,
                        isSelected = themeMode == 1,
                        onClick = { themeMode = 1 }
                    )
                    ThemeModeButton(
                        icon = Icons.Default.Monitor,
                        isSelected = themeMode == 2,
                        onClick = { themeMode = 2 }
                    )
                }
            }
        }
        
        item {
            Divider(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp))
            Text(
                text = "Tài khoản",
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            SettingItem(
                icon = Icons.Default.VpnKey,
                title = "Đổi mật khẩu",
                onClick = { onNavigateToDetail("password") }
            )
            SettingItem(
                icon = Icons.Default.Logout,
                title = "Đăng xuất",
                onClick = onLogoutClick,
                titleColor = MaterialTheme.colorScheme.error,
                iconColor = MaterialTheme.colorScheme.error,
                showChevron = false
            )
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
    }
}

@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onBackground,
    iconColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant,
    showChevron: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = titleColor,
            modifier = Modifier.weight(1f)
        )
        if (showChevron) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
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
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}