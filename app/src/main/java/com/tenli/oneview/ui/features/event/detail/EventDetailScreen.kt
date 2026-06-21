package com.tenli.oneview.ui.features.event.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.tenli.oneview.R
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.ui.component.CommonEmptyState
import com.tenli.oneview.ui.features.home.RecentEventItem
import com.tenli.oneview.ui.features.home.getEventImageUrl
import com.tenli.oneview.ui.theme.BrandPrimary
import com.tenli.oneview.ui.theme.spacing

enum class EventMediaTab(val title: String) {
    ORIGINAL_VIDEO("Video gốc"),
    EVENT_IMAGE("Ảnh sự kiện"),
    EVENT_VIDEO("Video sự kiện"),
    MAP("Bản đồ")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: Int,
    onBack: () -> Unit,
    viewModel: EventDetailViewModel = viewModel(factory = EventDetailViewModel.provideFactory(eventId))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(EventMediaTab.EVENT_IMAGE) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết sự kiện", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandPrimary)
            }
        } else if (uiState.error != null || uiState.event == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CommonEmptyState(text = uiState.error ?: "Không tìm thấy sự kiện")
            }
        } else {
            val event = uiState.event!!
            val camera = uiState.camera

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Tiêu đề & Tag
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = camera?.name ?: "Camera không xác định",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info, // Placeholder for AI Server Icon
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "AI Server", // Replace with real server name if available
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Nhãn sự kiện
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF22C55E) // Green color from design
                        ) {
                            Text(
                                text = event.type.uppercase(),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Media Tabs
                item {
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab.ordinal,
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary,
                        edgePadding = 0.dp,
                        divider = {}
                    ) {
                        EventMediaTab.values().forEach { tab ->
                            Tab(
                                selected = selectedTab == tab,
                                onClick = { selectedTab = tab },
                                text = { 
                                    Text(
                                        text = tab.title,
                                        fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                                    ) 
                                }
                            )
                        }
                    }
                }

                // Media Viewer
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        when (selectedTab) {
                            EventMediaTab.EVENT_IMAGE -> {
                                val imageUrl = getEventImageUrl(event)
                                if (imageUrl != null) {
                                    AsyncImage(
                                        model = imageUrl,
                                        contentDescription = "Event Image",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    Text("Không có ảnh", color = Color.White)
                                }
                            }
                            else -> {
                                Text("Chưa hỗ trợ", color = Color.White)
                            }
                        }
                    }
                }

                // Chi tiết metadata
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("ID SỰ KIỆN", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("#${event.id}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                            }
                            Column {
                                Text("THỜI GIAN", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(formatDateTime(event.time), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("HÀNH ĐỘNG", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Icons for actions
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFF10B981)
                                    ) {
                                        Icon(
                                            imageVector = androidx.compose.material.icons.Icons.Default.Search,
                                            contentDescription = "Action",
                                            modifier = Modifier.padding(4.dp).size(20.dp),
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Hành trình tại vị trí (Related Events)
                item {
                    Text(
                        text = "Hành trình tại vị trí",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (uiState.relatedEvents.isEmpty()) {
                    item {
                        Text(
                            text = "Không có sự kiện liên quan",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(items = uiState.relatedEvents.filter { it.id != event.id }) { relatedEvent ->
                        val cameraListForEvent = camera?.let { listOf(it) } ?: emptyList()
                        RecentEventItem(
                            event = relatedEvent,
                            cameraList = cameraListForEvent,
                            onClick = { /* Could navigate to another event */ }
                        )
                    }
                }
            }
        }
    }
}

private fun formatDateTime(timestamp: Double): String {
    val sdf = java.text.SimpleDateFormat("HH:mm:ss dd/MM", java.util.Locale.getDefault())
    return sdf.format(java.util.Date((timestamp * 1000).toLong()))
}
