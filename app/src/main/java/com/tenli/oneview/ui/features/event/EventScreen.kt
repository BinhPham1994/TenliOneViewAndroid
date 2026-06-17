package com.tenli.oneview.ui.features.event

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tenli.oneview.model.network.EventItem
import com.tenli.oneview.ui.component.CommonEmptyState
import com.tenli.oneview.ui.features.home.component.RecentEventItem
import com.tenli.oneview.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventScreen(
    listState: LazyListState,
    viewModel: EventViewModel = viewModel(factory = EventViewModel.Factory),
    onEventClick: (EventItem) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    if (uiState.currentLevel == 2) {
        EventFilterSheet(
            uiState = uiState,
            viewModel = viewModel,
            onDismiss = { viewModel.navigateBack() }
        )
    }

    BackHandler(enabled = true) {
        if (uiState.currentLevel > 0) {
            viewModel.navigateBack()
        } else {
            (context as? Activity)?.moveTaskToBack(true)
        }
    }

    val shouldLoadMore = remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItem = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1
            lastVisibleItem > (totalItems - 5) && totalItems > 0
        }
    }

    LaunchedEffect(shouldLoadMore.value, uiState.isRefreshing, uiState.isPagingLoading) {
        if (shouldLoadMore.value && !uiState.isPagingLoading && !uiState.isRefreshing && !uiState.isEndReached) {
            viewModel.loadEvents(isRefresh = false)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        EventTopHeader(onFilterClick = { viewModel.openFilter() })
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.loadEvents(isRefresh = true) },
            modifier = Modifier.weight(1f)
        ) {
            if (uiState.events.isEmpty() && !uiState.isRefreshing) {
                CommonEmptyState(
                    text = "Không tìm thấy sự kiện nào"
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = MaterialTheme.spacing.small, bottom = 100.dp)
                ) {
                    items(
                        items = uiState.events,
                        key = { it.id }
                    ) { event ->
                        RecentEventItem(
                            event = event,
                            onClick = {
                                viewModel.markEventAsRead(event)
                                onEventClick(event)
                            }
                        )
                    }

                    if (uiState.isPagingLoading) {
                        item(key = "paging_loading") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(MaterialTheme.spacing.medium),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(MaterialTheme.spacing.iconMedium),
                                    strokeWidth = 3.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
