package com.tenli.oneview.ui.features.event

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tenli.oneview.ui.component.CommonEmptyState
import com.tenli.oneview.ui.features.home.RecentEventItem

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun EventScreen(
    viewModel: EventViewModel = viewModel(factory = EventViewModel.Factory),
    listState: androidx.compose.foundation.lazy.LazyListState = androidx.compose.foundation.lazy.rememberLazyListState(),
    onEventClick: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var isRefreshing by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            isRefreshing = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sự kiện",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            com.tenli.oneview.ui.features.home.TimeFilterDropdown(
                selectedFilter = uiState.selectedFilter,
                onFilterSelected = { viewModel.setTimeFilter(it) }
            )
        }

        androidx.compose.material3.pulltorefresh.PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.fetchInitialData()
            },
            modifier = Modifier.fillMaxSize()
        ) {
            if (uiState.events.isEmpty() && uiState.error == null) {
                CommonEmptyState(text = "Chưa có sự kiện nào", modifier = Modifier.align(Alignment.Center))
            } else {
                val cameraMap = androidx.compose.runtime.remember(uiState.cameraList) {
                    uiState.cameraList.associateBy({ it.extra?.uuid }, { it.name })
                }
                
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
                ) {
                    items(items = uiState.events, key = { it.id }) { event ->
                        val cameraName = cameraMap[event.data?.cameraUUID] ?: "Camera"
                        
                        RecentEventItem(
                            event = event,
                            cameraName = cameraName,
                            onClick = {
                                onEventClick(event.id)
                            }
                        )
                    }

                    if (uiState.hasMore && !uiState.isLoading) {
                        item {
                            LaunchedEffect(uiState.events.size) {
                                viewModel.loadMoreEvents()
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, strokeWidth = 6.dp)
                            }
                        }
                    }
                }
            }

            uiState.error?.let {
                if (uiState.events.isEmpty()) {
                    CommonEmptyState(
                        text = it,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}
