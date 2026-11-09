package com.tenli.oneview.ui.features.event

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.tenli.oneview.ui.component.MobileTopPopupManager
import com.tenli.oneview.ui.component.MobileViewToggleButton
import com.tenli.oneview.ui.component.WaveDotsLoading
import com.tenli.oneview.ui.theme.LucideIcons
import com.tenli.oneview.ui.theme.appColors
import com.tenli.oneview.ui.theme.spacing

/** Khoảng trống cuối danh sách để item cuối không bị bottom navigation (nổi) che khuất */
private val ListBottomClearance = 130.dp

/** Bắt đầu tải trang tiếp khi còn cách cuối danh sách bấy nhiêu item */
private const val LOAD_MORE_THRESHOLD = 5

/** Danh sách quá ngắn (chưa đủ cuộn) thì tự tải thêm */
private const val AUTO_FILL_MAX_ITEMS = 12

private const val CONTENT_TYPE_EVENT = "event_card"
private const val CONTENT_TYPE_STATE = "state"
private const val CONTENT_TYPE_PAGING = "paging"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventScreen(
    viewModel: EventViewModel = viewModel(factory = EventViewModel.Factory),
    listState: LazyListState = rememberLazyListState(),
    isTabActive: Boolean = true,
    onEventClick: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showFilterSheet by remember { mutableStateOf(false) }
    val spacing = MaterialTheme.spacing

    LaunchedEffect(Unit) {
        viewModel.syncViewModeFromPrefs()
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { errMsg ->
            MobileTopPopupManager.error(errMsg)
            viewModel.clearError()
        }
    }

    val shouldLoadMore by remember(listState) {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 1 && lastVisibleIndex >= totalItems - LOAD_MORE_THRESHOLD
        }
    }

    // Không truyền isLoading/isPaginating làm key để tránh vòng lặp vô hạn khi loadMore kết thúc mà events.size không đổi
    LaunchedEffect(isTabActive, shouldLoadMore, uiState.events.size, uiState.hasMore) {
        if (isTabActive &&
            (shouldLoadMore || uiState.events.size in 1..AUTO_FILL_MAX_ITEMS) &&
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
            .background(MaterialTheme.appColors.pageBg)
    ) {
        // Sticky Mobile Top Bar matching .mobile-top-bar (position: sticky; top: 0; z-index: 50):
        // Left: View Mode Toggle Button, Center: "Sự kiện", Right: Filter Action Button
        MobileTopBar(
            title = stringResource(id = R.string.tab_event),
            leftAction = {
                MobileViewToggleButton(
                    isListView = uiState.isListView,
                    onToggle = viewModel::toggleViewMode
                )
            },
            rightAction = {
                MobileTopActionButton(
                    icon = LucideIcons.ListFilter,
                    contentDescription = stringResource(id = R.string.filter_title),
                    hasActiveDot = uiState.hasActiveFilter,
                    onClick = { showFilterSheet = true }
                )
            }
        )

        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = spacing.space2, bottom = ListBottomClearance),
                verticalArrangement = Arrangement.spacedBy(spacing.space10)
            ) {
                if (uiState.isLoading && uiState.events.isEmpty()) {
                    item(key = "state_loading", contentType = CONTENT_TYPE_STATE) {
                        Box(
                            modifier = Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isTabActive) {
                                WaveDotsLoading()
                            }
                        }
                    }
                } else if (uiState.events.isEmpty()) {
                    item(key = "state_empty", contentType = CONTENT_TYPE_STATE) {
                        Box(
                            modifier = Modifier.fillParentMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CommonEmptyState(text = stringResource(id = R.string.event_empty_state))
                        }
                    }
                } else {
                    items(
                        items = uiState.events,
                        key = { event -> "ev_${event.uniqueKey()}" },
                        contentType = { CONTENT_TYPE_EVENT }
                    ) { event ->
                        Box(modifier = Modifier.padding(horizontal = spacing.space10)) {
                            MobileEventCard(
                                event = event,
                                cameraName = event.cameraUuidOrNull?.let { uiState.cameraNameMap[it] }.orEmpty(),
                                isListView = uiState.isListView,
                                enableSharedElement = false,
                                onClick = {
                                    viewModel.onEventSelected(event)
                                    onEventClick(event.id)
                                }
                            )
                        }
                    }

                    if (uiState.isPaginating || (uiState.hasMore && !uiState.isLoading)) {
                        item(key = "paging_indicator", contentType = CONTENT_TYPE_PAGING) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(spacing.space16),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isTabActive) {
                                    WaveDotsLoading(
                                        dotSize = spacing.space6,
                                        dotColor = MaterialTheme.appColors.accent,
                                        maxOffset = spacing.space6
                                    )
                                }
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
            onReset = viewModel::resetFilters,
            onDismiss = { showFilterSheet = false }
        )
    }
}
