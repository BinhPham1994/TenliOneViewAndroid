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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Check
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tenli.oneview.ui.component.CommonEmptyState
import com.tenli.oneview.ui.features.home.RecentEventItem
import com.tenli.oneview.ui.features.home.TimeFilter
import com.tenli.oneview.model.network.AIServiceModel
import com.tenli.oneview.ui.utils.AiTypeHelper

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
    var showFilterSheet by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            isRefreshing = false
        }
    }

    if (showFilterSheet) {
        EventFilterBottomSheet(
            uiState = uiState,
            onDismiss = { showFilterSheet = false },
            onApply = { time, serviceId, aiType ->
                viewModel.applyFilters(time, serviceId, aiType)
                showFilterSheet = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sự kiện",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { showFilterSheet = true }) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Lọc sự kiện",
                    tint = MaterialTheme.colorScheme.primary
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


@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun EventFilterBottomSheet(
    uiState: EventScreenUiState,
    onDismiss: () -> Unit,
    onApply: (TimeFilter, Int?, String?) -> Unit
) {
    var tempTimeFilter by remember { mutableStateOf(uiState.selectedFilter) }
    var tempServiceId by remember { mutableStateOf(uiState.selectedServiceId) }
    var tempAiType by remember { mutableStateOf(uiState.selectedAiType) }

    var timeExpanded by remember { mutableStateOf(false) }
    var serviceExpanded by remember { mutableStateOf(false) }
    var aiTypeExpanded by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Time Filter Dropdown
            ExposedDropdownMenuBox(
                expanded = timeExpanded,
                onExpandedChange = { timeExpanded = !timeExpanded }
            ) {
                OutlinedTextField(
                    value = tempTimeFilter.title,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Thời gian") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = timeExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = timeExpanded,
                    onDismissRequest = { timeExpanded = false }
                ) {
                    TimeFilter.entries.forEach { filter ->
                        DropdownMenuItem(
                            text = { Text(filter.title) },
                            onClick = {
                                tempTimeFilter = filter
                                timeExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Service Dropdown
            val selectedServiceName = if (tempServiceId == null) "Tất cả chi nhánh" else uiState.aiServices.find { it.id == tempServiceId }?.name ?: "Chi nhánh $tempServiceId"
            ExposedDropdownMenuBox(
                expanded = serviceExpanded,
                onExpandedChange = { serviceExpanded = !serviceExpanded }
            ) {
                OutlinedTextField(
                    value = selectedServiceName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Chi nhánh") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = serviceExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = serviceExpanded,
                    onDismissRequest = { serviceExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Tất cả chi nhánh") },
                        onClick = {
                            tempServiceId = null
                            serviceExpanded = false
                        }
                    )
                    uiState.aiServices.forEach { service ->
                        DropdownMenuItem(
                            text = { Text(service.name) },
                            onClick = {
                                tempServiceId = service.id
                                serviceExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Type Dropdown
            val aiTypes = listOf(
                "sensor-person-camera", "logic-face", "sensor-license-plate", "logic-uniform", "logic-fire",
                "sensor-object", "sensor-heatmap-camera", "sensor-crowd-camera", "sensor-violence",
                "sensor-parking-camera", "sensor-animal-camera", "sensor-weapon-camera"
            )
            val selectedAiTypeName = if (tempAiType == null) "Tất cả bài AI" else AiTypeHelper.getTypeName(tempAiType)
            ExposedDropdownMenuBox(
                expanded = aiTypeExpanded,
                onExpandedChange = { aiTypeExpanded = !aiTypeExpanded }
            ) {
                OutlinedTextField(
                    value = selectedAiTypeName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Bài AI") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = aiTypeExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = aiTypeExpanded,
                    onDismissRequest = { aiTypeExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Tất cả bài AI") },
                        onClick = {
                            tempAiType = null
                            aiTypeExpanded = false
                        }
                    )
                    aiTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(AiTypeHelper.getTypeName(type)) },
                            onClick = {
                                tempAiType = type
                                aiTypeExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                androidx.compose.material3.OutlinedButton(
                    onClick = {
                        tempTimeFilter = TimeFilter.TODAY
                        tempServiceId = null
                        tempAiType = null
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Xoá bộ lọc",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Button(
                    onClick = { onApply(tempTimeFilter, tempServiceId, tempAiType) },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Áp dụng",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp)) // padding bottom for safe area
        }
    }
}
