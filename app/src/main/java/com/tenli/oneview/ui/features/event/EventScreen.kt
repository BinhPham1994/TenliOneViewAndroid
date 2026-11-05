package com.tenli.oneview.ui.features.event

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tenli.oneview.R
import com.tenli.oneview.ui.component.CommonEmptyState
import com.tenli.oneview.ui.component.MobileEventCard
import com.tenli.oneview.ui.component.MobileFilterBottomSheet
import com.tenli.oneview.ui.component.MobileTopActionButton
import com.tenli.oneview.ui.component.MobileTopBar
import com.tenli.oneview.ui.component.MobileViewToggleButton
import com.tenli.oneview.ui.component.WaveDotsLoading
import com.tenli.oneview.ui.features.home.TimeFilter
import com.tenli.oneview.ui.theme.LucideIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventScreen(
    viewModel: EventViewModel = viewModel(factory = EventViewModel.Factory),
    listState: androidx.compose.foundation.lazy.LazyListState = androidx.compose.foundation.lazy.rememberLazyListState(),
    onEventClick: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isRefreshing by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.syncViewModeFromPrefs()
    }

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            isRefreshing = false
        }
    }

    val hasActiveFilter = uiState.selectedServiceId != null ||
        uiState.selectedFilter != TimeFilter.TODAY ||
        uiState.selectedCameraUuid != null ||
        uiState.selectedAiType != null

    val cameraMap = remember(uiState.cameraList) {
        uiState.cameraList.mapNotNull { cam ->
            val uuid = cam.extra?.uuid
            if (!uuid.isNullOrBlank()) uuid to cam.name else null
        }.toMap()
    }

    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 1 && lastVisibleIndex >= totalItems - 5
        }
    }

    // Không truyền isLoading/isPaginating làm key để tránh vòng lặp vô hạn khi loadMore kết thúc mà events.size không đổi
    LaunchedEffect(shouldLoadMore, uiState.events.size, uiState.hasMore) {
        if ((shouldLoadMore || uiState.events.size in 1..12) &&
            uiState.hasMore &&
            !uiState.isLoading &&
            !uiState.isPaginating
        ) {
            viewModel.loadMoreEvents()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Sticky Mobile Top Bar matching .mobile-top-bar (position: sticky; top: 0; z-index: 50):
        // Left: View Mode Toggle Button, Center: "Sự kiện", Right: Filter Action Button
        MobileTopBar(
            title = stringResource(id = R.string.tab_event),
            leftAction = {
                MobileViewToggleButton(
                    isListView = uiState.isListView,
                    onToggle = { viewModel.toggleViewMode() }
                )
            },
            rightAction = {
                MobileTopActionButton(
                    icon = LucideIcons.ListFilter,
                    contentDescription = "Bộ lọc",
                    hasActiveDot = hasActiveFilter,
                    onClick = { showFilterSheet = true }
                )
            }
        )

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.fetchInitialData()
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LazyColumn(
                state = listState,
                userScrollEnabled = true,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 2.dp, bottom = 130.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (uiState.isLoading && uiState.events.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            WaveDotsLoading()
                        }
                    }
                } else if (uiState.events.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CommonEmptyState(text = "Không tìm thấy sự kiện nào")
                        }
                    }
                } else {
                    items(
                        items = uiState.events,
                        key = { event -> "ev_${event.serviceId}_${event.id}_${event.time}" },
                        contentType = { "event_card" }
                    ) { event ->
                        val cameraName = cameraMap[event.data?.cameraUUID] ?: "Camera"
                        Box(modifier = Modifier.padding(horizontal = 10.dp)) {
                            MobileEventCard(
                                event = event,
                                cameraName = cameraName,
                                isListView = uiState.isListView,
                                enableSharedElement = false,
                                onClick = {
                                    com.tenli.oneview.data.local.EventCacheManager.cacheClickedEvent(
                                        event = event,
                                        cameras = uiState.cameraList,
                                        aiServices = uiState.aiServices
                                    )
                                    onEventClick(event.id)
                                }
                            )
                        }
                    }

                    if (uiState.isPaginating || (uiState.hasMore && !uiState.isLoading)) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp),
                                    strokeWidth = 3.dp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        MobileFilterBottomSheet(
            showScope = true,
            showTime = true,
            showCamera = true,
            showEventType = true,
            aiServices = uiState.aiServices,
            cameras = uiState.cameraList,
            selectedServiceId = uiState.selectedServiceId,
            selectedTimeFilter = uiState.selectedFilter,
            selectedCameraUuid = uiState.selectedCameraUuid,
            selectedAiType = uiState.selectedAiType,
            onApply = { serviceId, timeFilter, cameraUuid, aiType ->
                viewModel.applyFilters(
                    filter = timeFilter,
                    serviceId = serviceId,
                    aiType = aiType,
                    cameraUuid = cameraUuid
                )
            },
            onReset = {
                viewModel.resetFilters()
            },
            onDismiss = { showFilterSheet = false }
        )
    }
}
