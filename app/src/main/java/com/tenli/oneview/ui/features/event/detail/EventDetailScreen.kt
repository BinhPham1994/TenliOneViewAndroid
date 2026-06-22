package com.tenli.oneview.ui.features.event.detail

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    EVENT_VIDEO("Video sự kiện")
}

enum class EventHistoryTab(val title: String) {
    HISTORY("Lịch sử nhận diện"),
    LICENSE_PLATE("Biển số liên quan")
}


@OptIn(ExperimentalMaterial3Api::class, androidx.compose.animation.ExperimentalSharedTransitionApi::class)
@Composable
fun EventDetailScreen(
    eventId: Int,
    onBack: () -> Unit,
    viewModel: EventDetailViewModel = viewModel(factory = EventDetailViewModel.provideFactory(eventId))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(EventMediaTab.EVENT_IMAGE) }
    var selectedHistoryTab by remember { mutableStateOf(EventHistoryTab.HISTORY) }
    var showMetadataSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(300) // Tải API và bắt đầu hiện thông tin sớm hơn
        viewModel.loadRelatedEvents()
        showContent = true
    }
    val contentAlpha by animateFloatAsState(
        targetValue = if (showContent) 1f else 0f,
        animationSpec = tween(durationMillis = 300) 
    )
    val offsetY by animateDpAsState(
        targetValue = if (showContent) 0.dp else (-50).dp,
        animationSpec = tween(durationMillis = 300)
    )

    val animatedVisibilityScope = com.tenli.oneview.ui.navigation.LocalAnimatedVisibilityScope.current
    val isExiting = animatedVisibilityScope?.transition?.targetState == androidx.compose.animation.EnterExitState.PostExit
    val finalAlpha = if (isExiting) 0f else contentAlpha
    val instantAlpha = if (isExiting) 0f else 1f

    Scaffold(
        containerColor = if (isExiting) Color.Transparent else MaterialTheme.colorScheme.background
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Media Viewer
                    val sharedTransitionScope = com.tenli.oneview.ui.navigation.LocalSharedTransitionScope.current
                    val animatedVisibilityScope = com.tenli.oneview.ui.navigation.LocalAnimatedVisibilityScope.current
                    
                    var boxModifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .zIndex(1f)
                        .pointerInput(Unit) {
                            var totalDrag = 0f
                            detectVerticalDragGestures(
                                onVerticalDrag = { change, dragAmount ->
                                    totalDrag += dragAmount
                                },
                                onDragEnd = {
                                    if (totalDrag > 100f) {
                                        onBack()
                                    }
                                    totalDrag = 0f
                                },
                                onDragCancel = {
                                    totalDrag = 0f
                                }
                            )
                        }
                        
                    if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                        with(sharedTransitionScope) {
                            boxModifier = boxModifier.sharedElement(
                                sharedContentState = rememberSharedContentState(key = "event_image_${event.id}"),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                        }
                    }

                    Box(
                        modifier = boxModifier,
                        contentAlignment = Alignment.Center
                    ) {
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = finalAlpha)))
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

                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                // Media Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .alpha(instantAlpha)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        EventMediaTab.values().forEach { tab ->
                            val isSelected = selectedTab == tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable { selectedTab = tab }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tab.title,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                // Tiêu đề & Tag
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .alpha(instantAlpha)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null,
                                onClick = { showMetadataSheet = true }
                            ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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
                            Text(
                                text = formatDateTime(event.time),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Nhãn sự kiện
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = com.tenli.oneview.ui.utils.AiTypeHelper.getAiColor(event.type)
                        ) {
                            Text(
                                text = com.tenli.oneview.ui.utils.AiTypeHelper.getTypeName(event.type).uppercase(),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                // Tabs Lịch sử & Biển số
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .padding(horizontal = 16.dp)
                            .offset(y = offsetY)
                            .alpha(finalAlpha)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        EventHistoryTab.values().forEach { tab ->
                            val isSelected = selectedHistoryTab == tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable { selectedHistoryTab = tab }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tab.title,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                val currentEventsList = if (selectedHistoryTab == EventHistoryTab.HISTORY) {
                    uiState.relatedEvents.filter { it.id != event.id }
                } else {
                    uiState.licensePlateEvents.filter { it.id != event.id }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp)
                ) {
                    if (currentEventsList.isEmpty()) {
                        item {
                            CommonEmptyState(
                                text = "Không có sự kiện liên quan",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 32.dp)
                                    .offset(y = offsetY)
                                    .alpha(finalAlpha)
                            )
                        }
                    } else {
                        items(items = currentEventsList) { relatedEvent ->
                            val cameraName = camera?.name ?: "Camera"
                            Box(modifier = Modifier.offset(y = offsetY).alpha(finalAlpha)) {
                                RecentEventItem(
                                    event = relatedEvent,
                                    cameraName = cameraName,
                                    enableSharedElement = false,
                                    onClick = { /* Could navigate to another event */ }
                                )
                            } // end Box
                        } // end items
                    } // end else
                } // end LazyColumn
                    } // end inner Column
                    
                    // Overlay Bottom Sheet (inside Box)
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showMetadataSheet,
                        enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it }),
                        exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { it }),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.surface,
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Thông tin sự kiện", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                                    IconButton(onClick = { showMetadataSheet = false }) {
                                        Icon(imageVector = androidx.compose.material.icons.Icons.Default.Close, contentDescription = "Close")
                                    }
                                }
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("ID SỰ KIỆN", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("#${uiState.event?.id}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                                    }
                                    Column {
                                        Text("THỜI GIAN", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(uiState.event?.time?.let { formatDateTime(it) } ?: "", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                                
                                Column {
                                    Text("HÀNH ĐỘNG", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF10B981),
                                            modifier = Modifier.clickable { /* action */ }
                                        ) {
                                            Icon(
                                                imageVector = androidx.compose.material.icons.Icons.Default.Search,
                                                contentDescription = "Action",
                                                modifier = Modifier.padding(12.dp).size(24.dp),
                                                tint = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } // end wrapper Box
        } // end outer Column
    } // end else
    } // end Scaffold
    
    androidx.activity.compose.BackHandler(enabled = showMetadataSheet) {
        showMetadataSheet = false
    }
} // end EventDetailScreen

private fun formatDateTime(timestamp: Double): String {
    val sdf = java.text.SimpleDateFormat("HH:mm:ss dd/MM", java.util.Locale.getDefault())
    return sdf.format(java.util.Date((timestamp * 1000).toLong()))
}
