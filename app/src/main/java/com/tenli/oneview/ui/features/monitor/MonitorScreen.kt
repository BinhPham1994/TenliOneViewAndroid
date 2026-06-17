package com.tenli.oneview.ui.features.monitor

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tenli.oneview.model.network.EventItem
import com.tenli.oneview.ui.component.CommonEmptyState
import com.tenli.oneview.ui.features.home.HomeHeaderSimple
import com.tenli.oneview.ui.features.home.component.HomeGroupSheet
import com.tenli.oneview.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitorScreen(
    listState: LazyListState,
    viewModel: MonitorViewModel = viewModel(factory = MonitorViewModel.Factory),
    onEventClick: (EventItem) -> Unit,
    onShowBottomBar: (Boolean) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showGroupSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current
    LaunchedEffect(uiState.currentLevel) {
        onShowBottomBar(uiState.currentLevel == 0)
    }

    BackHandler(enabled = true) {
        if (uiState.currentLevel > 0) {
            viewModel.navigateBack()
        } else {
            (context as? Activity)?.moveTaskToBack(true)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (uiState.currentLevel == 0) {
            HomeHeaderSimple(homeName = uiState.homeName, onClick = { showGroupSheet = true })
        } else {
            MonitorDetailTopBar(
                title = uiState.selectedMonitor?.monitor?.name ?: "Giám sát",
                onBack = { viewModel.navigateBack() }
            )
        }

        HorizontalDivider(thickness = MaterialTheme.spacing.borderThin, color = Color.LightGray.copy(alpha = 0.3f))

        Box(modifier = Modifier
            .weight(1f)
            .padding(top = 0.dp)) {
            if (uiState.currentLevel == 0) {
                if (uiState.groupedMonitors.isEmpty() && !uiState.isRefreshing) {
                    CommonEmptyState(
                        text = "Bạn chưa có cấu hình AI nào\nChọn + để thêm mới"
                    )
                } else {
                    PullToRefreshBox(
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = { viewModel.refreshMonitors() },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            uiState.groupedMonitors.forEach { (type, monitorList) ->
                                item { MonitorGroupHeader(type) }
                                items(
                                    items = monitorList,
                                    key = { "${it.deviceId}_${it.monitor.id}" }
                                ) { item ->
                                    MonitorImageCard(item) {
                                        viewModel.onMonitorSelected(item)
                                    }
                                }
                            }
                            item { Spacer(modifier = Modifier.height(100.dp)) }
                        }
                    }
                }
            } else {
                MonitorDetailView(listState, uiState.selectedMonitor, viewModel, onEventClick = onEventClick)
            }
        }
    }

    if (showGroupSheet) {
        HomeGroupSheet(
            groups = uiState.displayGroups,
            selectedGroupId = uiState.selectedGroup?.group?.id ?: -1,
            onGroupSelected = { viewModel.onGroupSelected(it); showGroupSheet = false },
            onManageHome = { showGroupSheet = false },
            onDismiss = { showGroupSheet = false }
        )
    }
}
