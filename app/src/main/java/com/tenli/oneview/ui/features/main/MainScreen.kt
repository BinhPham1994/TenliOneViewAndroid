package com.tenli.oneview.ui.features.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
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

@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel(factory = MainViewModel.Factory),
    onLogoutRequest: () -> Unit
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
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(MainTab.Home.route) {
                HomeScreen(
                    listState = homeListState,
                    onEventClick = { eventId ->
                        // Điều hướng đến chi tiết sự kiện nếu có
                    }
                )
            }

            composable(MainTab.Monitor.route) {
                com.tenli.oneview.ui.features.monitor.MonitorScreen(listState = monitorState)
            }

            composable(MainTab.Event.route) {
                com.tenli.oneview.ui.features.event.EventScreen(listState = eventListState)
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(16.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Quản lý tài khoản",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.logout() },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                        ) {
                            Text("Đăng xuất")
                        }
                    }
                }
            }
        }
    }
}