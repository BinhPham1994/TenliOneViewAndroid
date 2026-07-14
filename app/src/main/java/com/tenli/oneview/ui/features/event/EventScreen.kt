package com.tenli.oneview.ui.features.event

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tenli.oneview.ui.component.AiServiceFilterDropdown
import com.tenli.oneview.ui.component.AiTaskFilterDropdown
import com.tenli.oneview.ui.component.CommonEmptyState
import com.tenli.oneview.ui.component.TimeFilterDropdown
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
    var isRefreshing by remember { mutableStateOf(false) }

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
        // Filters
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                TimeFilterDropdown(
                    selectedFilter = uiState.selectedFilter,
                    onFilterSelected = { viewModel.applyFilters(it, uiState.selectedServiceId, uiState.selectedAiType) }
                )
            }
            item {
                AiServiceFilterDropdown(
                    services = uiState.aiServices,
                    selectedServiceId = uiState.selectedServiceId,
                    onServiceSelected = { viewModel.applyFilters(uiState.selectedFilter, it, uiState.selectedAiType) }
                )
            }
            item {
                AiTaskFilterDropdown(
                    selectedAiType = uiState.selectedAiType,
                    onAiTypeSelected = { viewModel.applyFilters(uiState.selectedFilter, uiState.selectedServiceId, it) }
                )
            }
        }

        androidx.compose.material3.pulltorefresh.PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.fetchInitialData()
            },
            modifier = Modifier.fillMaxSize()
        ) {
            if (uiState.isLoading && uiState.events.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    com.tenli.oneview.ui.component.WaveDotsLoading()
                }
            } else if (uiState.events.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    CommonEmptyState(text = "Chưa có sự kiện nào")
                }
            } else {
                val cameraMap = androidx.compose.runtime.remember(uiState.cameraList) {
                    uiState.cameraList.associateBy({ it.extra?.uuid }, { it.name })
                }
                val serviceMap = androidx.compose.runtime.remember(uiState.aiServices) {
                    uiState.aiServices.associateBy({ it.id }, { it.name })
                }
                
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                ) {
                    items(items = uiState.events, key = { it.id }) { event ->
                        val cameraName = cameraMap[event.data?.cameraUUID] ?: "Camera"
                        val aiServiceName = serviceMap[event.serviceId] ?: "Unknown Service"
                        
                        RecentEventItem(
                            event = event,
                            cameraName = cameraName,
                            aiServiceName = aiServiceName,
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


        }
    }
}
