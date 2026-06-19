package com.tenli.oneview.ui.features.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

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
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Home Screen (Placeholder)")
                }
            }

            composable(MainTab.Monitor.route) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Monitor Screen (Placeholder)")
                }
            }

            composable(MainTab.Event.route) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Event Screen (Placeholder)")
                }
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
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Setting Screen (Placeholder)")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.logout() }) {
                            Text("Đăng xuất")
                        }
                    }
                }
            }
        }
    }
}