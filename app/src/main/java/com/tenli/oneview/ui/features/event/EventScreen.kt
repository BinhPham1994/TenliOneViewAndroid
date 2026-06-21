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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tenli.oneview.ui.component.CommonEmptyState
import com.tenli.oneview.ui.features.home.RecentEventItem

@Composable
fun EventScreen(
    viewModel: EventViewModel = viewModel(factory = EventViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

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

        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (uiState.events.isEmpty() && uiState.error == null) {
                CommonEmptyState(text = "Chưa có sự kiện nào", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .background(androidx.compose.ui.graphics.Color.White, shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                        .padding(vertical = 4.dp)
                ) {
                    items(items = uiState.events, key = { it.id }) { event ->
                        val currentCamera = uiState.cameraList.find { it.extra?.uuid == event.data?.cameraUUID }
                        val cameraListForEvent = currentCamera?.let { listOf(it) } ?: emptyList()
                        
                        RecentEventItem(
                            event = event,
                            cameraList = cameraListForEvent,
                            onClick = {
                                Toast.makeText(context, "Clicked Event ${event.id}", Toast.LENGTH_SHORT).show()
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
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
