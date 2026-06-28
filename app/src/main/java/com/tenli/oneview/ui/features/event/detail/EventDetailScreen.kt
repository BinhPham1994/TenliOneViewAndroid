package com.tenli.oneview.ui.features.event.detail

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
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
    var expandedCropUrl by remember { mutableStateOf<String?>(null) }
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
                // Tiêu đề & Tag
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .alpha(instantAlpha)
                        .clip(RoundedCornerShape(8.dp)),
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
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            val timeStr = formatDateTime(event.time)
                            val parts = timeStr.split(" ")
                            Text(
                                text = androidx.compose.ui.text.buildAnnotatedString {
                                    if (parts.size >= 2) {
                                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                            append(parts[0])
                                        }
                                        append(" " + parts.drop(1).joinToString(" "))
                                    } else {
                                        append(timeStr)
                                    }
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Nhãn sự kiện
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = com.tenli.oneview.ui.utils.AiTypeHelper.getAiColor(event)
                        ) {
                            Text(
                                text = com.tenli.oneview.ui.utils.AiTypeHelper.getEventName(event).uppercase(),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }

                        val baseEvent = uiState.baseEvent
                        val plateText = event.data?.plate?.plate ?: (if (event.type.contains("plate", ignoreCase = true)) event.data?.value else null)
                        val aiService = uiState.aiServices.find { it.id == event.serviceId }
                        val aiServiceName = aiService?.name
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!aiServiceName.isNullOrBlank()) {
                                Text(
                                    text = aiServiceName,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                                    color = Color.Gray,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                            
                            if (!plateText.isNullOrEmpty()) {
                                Text(
                                    text = plateText,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            if (baseEvent != null && event.id != baseEvent.id) {
                                androidx.compose.material3.TextButton(
                                    onClick = {
                                        viewModel.selectEvent(baseEvent)
                                        selectedTab = EventMediaTab.EVENT_IMAGE
                                        expandedCropUrl = null
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    androidx.compose.material3.Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                                        contentDescription = "Reset",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Về sự kiện gốc", 
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                // Media Tabs & Crop Images
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                        .alpha(instantAlpha),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Media Tabs
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        EventMediaTab.values().forEach { tab ->
                            val isSelected = selectedTab == tab
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable { selectedTab = tab }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tab.title,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Right: Crop Images
                    val cropUrls = com.tenli.oneview.ui.features.home.getEventCropUrls(event)
                    if (cropUrls.isNotEmpty()) {
                        androidx.compose.foundation.layout.Row(
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            cropUrls.forEach { url ->
                                coil.compose.SubcomposeAsyncImage(
                                    model = url,
                                    contentDescription = "Crop Image"
                                ) {
                                    val currentPainter = painter
                                    val state = currentPainter.state
                                    if (state is coil.compose.AsyncImagePainter.State.Success) {
                                        Box {
                                            androidx.compose.foundation.Image(
                                                painter = currentPainter,
                                                contentDescription = "Crop Image",
                                                modifier = Modifier
                                                    .height(44.dp)
                                                    .widthIn(max = 120.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color.Black.copy(alpha = 0.5f))
                                                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                                                    .clickable { expandedCropUrl = if (expandedCropUrl == url) null else url },
                                                contentScale = ContentScale.Fit
                                            )
                                            
                                            if (expandedCropUrl == url) {
                                                androidx.compose.ui.window.Popup(
                                                    alignment = Alignment.Center,
                                                    onDismissRequest = { expandedCropUrl = null }
                                                ) {
                                                    val intrinsicSize = currentPainter.intrinsicSize
                                                    val isSizeValid = intrinsicSize != androidx.compose.ui.geometry.Size.Unspecified && intrinsicSize.width > 0f && intrinsicSize.height > 0f
                                                    val imgModifier = if (isSizeValid) {
                                                        val ratio = intrinsicSize.width / intrinsicSize.height
                                                        Modifier
                                                            .widthIn(max = 360.dp)
                                                            .heightIn(max = 132.dp)
                                                            .aspectRatio(ratio, matchHeightConstraintsFirst = false)
                                                    } else {
                                                        Modifier
                                                            .height(132.dp)
                                                            .widthIn(max = 360.dp)
                                                    }

                                                    androidx.compose.foundation.Image(
                                                        painter = currentPainter,
                                                        contentDescription = "Expanded Crop Image",
                                                        modifier = imgModifier
                                                            .clip(RoundedCornerShape(12.dp))
                                                            .background(Color.Black.copy(alpha = 0.8f))
                                                            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                                                            .clickable { expandedCropUrl = null },
                                                        contentScale = ContentScale.Fit
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .height(44.dp)
                                                .width(60.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.Gray.copy(alpha = 0.3f))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

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
                                sharedContentState = rememberSharedContentState(key = "event_image_${eventId}"),
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
                                    val eventBoxes = com.tenli.oneview.ui.features.home.findAllBoxes(event)
                                    val palette = listOf(
                                        Color(0xFFff4d4f), // Red
                                        Color(0xFF38dd0f), // Neon Green
                                        Color(0xFF1890ff), // Blue
                                        Color(0xFFfaad14), // Gold
                                        Color(0xFF13c2c2), // Cyan
                                        Color(0xFFeb2f96)  // Magenta
                                    )

                                    AsyncImage(
                                        model = imageUrl,
                                        contentDescription = "Event Image",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .drawWithContent {
                                                drawContent()
                                                eventBoxes.forEachIndexed { index, boxArr ->
                                                    if (boxArr.size == 4) {
                                                        val minX = boxArr[0]
                                                        val minY = boxArr[1]
                                                        val maxX = boxArr[2]
                                                        val maxY = boxArr[3]

                                                        val w = (maxX - minX) * size.width
                                                        val h = (maxY - minY) * size.height
                                                        val x = minX * size.width
                                                        val y = minY * size.height

                                                        val color = palette[index % palette.size]
                                                        drawRect(
                                                            color = color,
                                                            topLeft = Offset(x, y),
                                                            size = Size(w, h),
                                                            style = Stroke(width = 4f)
                                                        )
                                                    }
                                                }
                                            },
                                        contentScale = ContentScale.FillBounds
                                    )
                                } else {
                                    Text("Không có ảnh", color = Color.White)
                                }
                            }
                            EventMediaTab.ORIGINAL_VIDEO -> {
                                LaunchedEffect(Unit) {
                                    viewModel.loadOriginalVideo()
                                }
                                
                                val eventBoxes = com.tenli.oneview.ui.features.home.findAllBoxes(event)
                                val mainBox = eventBoxes.firstOrNull()
                                var pivotX = 0.5f
                                var pivotY = 0.5f
                                if (mainBox != null && mainBox.size == 4) {
                                    pivotX = ((mainBox[0] + mainBox[2]) / 2).toFloat()
                                    pivotY = ((mainBox[1] + mainBox[3]) / 2).toFloat()
                                }

                                var showEventEffect by remember { mutableStateOf(false) }
                                val scale by animateFloatAsState(
                                    targetValue = if (showEventEffect) 1.8f else 1f,
                                    animationSpec = tween(
                                        durationMillis = if (showEventEffect) 2000 else 1200,
                                        easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
                                    ),
                                    label = "zoom_effect"
                                )

                                if (uiState.originalVideoLoading) {
                                    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                    }
                                } else if (uiState.originalVideoError != null) {
                                    CommonEmptyState(
                                        text = uiState.originalVideoError ?: "Lỗi tải video",
                                        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)
                                    )
                                } else {
                                    val videoUrl = uiState.originalVideoUrl
                                    if (videoUrl != null) {
                                        Box(modifier = Modifier
                                            .fillMaxSize()
                                            .clip(androidx.compose.ui.graphics.RectangleShape)
                                        ) {
                                            Box(modifier = Modifier
                                                .fillMaxSize()
                                                .graphicsLayer {
                                                    scaleX = scale
                                                    scaleY = scale
                                                    transformOrigin = TransformOrigin(pivotX, pivotY)
                                                }
                                            ) {
                                                com.tenli.oneview.ui.component.VideoPlayer(
                                                    videoUrl = videoUrl,
                                                    thumbnailUrl = null,
                                                    deviceKey = com.tenli.oneview.data.local.UserSession.accessToken,
                                                    initialSeekPositionMs = uiState.originalVideoSeekTimeMs,
                                                    onTimeUpdate = { currentMs ->
                                                        val seekTimeMs = uiState.originalVideoSeekTimeMs ?: return@VideoPlayer
                                                        if (currentMs in seekTimeMs..(seekTimeMs + 10000)) {
                                                            showEventEffect = true
                                                        } else {
                                                            showEventEffect = false
                                                        }
                                                    }
                                                )

                                                androidx.compose.foundation.layout.Spacer(modifier = Modifier
                                                    .fillMaxSize()
                                                    .drawWithContent {
                                                        if (showEventEffect && mainBox != null && mainBox.size == 4) {
                                                            val overlayAlpha = 0.7f
                                                            val padding = 12f * density
                                                            
                                                            val minX = mainBox[0].toFloat()
                                                            val minY = mainBox[1].toFloat()
                                                            val maxX = mainBox[2].toFloat()
                                                            val maxY = mainBox[3].toFloat()
                                                            
                                                            val boxW = (maxX - minX) * size.width
                                                            val boxH = (maxY - minY) * size.height
                                                            val boxX = minX * size.width
                                                            val boxY = minY * size.height
                                                            
                                                            val cutX = (boxX - padding).coerceAtLeast(0f)
                                                            val cutY = (boxY - padding).coerceAtLeast(0f)
                                                            val cutW = (boxX + boxW + padding).coerceAtMost(size.width) - cutX
                                                            val cutH = (boxY + boxH + padding).coerceAtMost(size.height) - cutY
                                                            
                                                            val darkColor = Color.Black.copy(alpha = overlayAlpha)
                                                            drawRect(darkColor, Offset(0f, 0f), Size(size.width, cutY))
                                                            drawRect(darkColor, Offset(0f, cutY + cutH), Size(size.width, size.height - (cutY + cutH)))
                                                            drawRect(darkColor, Offset(0f, cutY), Size(cutX, cutH))
                                                            drawRect(darkColor, Offset(cutX + cutW, cutY), Size(size.width - (cutX + cutW), cutH))
                                                            
                                                            drawRect(Color.Red, Offset(cutX, cutY), Size(cutW, cutH), style = Stroke(width = 4f))
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    } else {
                                        CommonEmptyState(
                                            text = "Chưa có dữ liệu",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                            EventMediaTab.EVENT_VIDEO -> {
                                val videoUrl = com.tenli.oneview.ui.features.home.getEventVideoUrl(event)
                                if (videoUrl != null) {
                                    com.tenli.oneview.ui.component.VideoPlayer(
                                        videoUrl = videoUrl,
                                        thumbnailUrl = com.tenli.oneview.ui.features.home.getEventImageUrl(event),
                                        deviceKey = com.tenli.oneview.data.local.UserSession.accessToken
                                    )
                                } else {
                                    CommonEmptyState(
                                        text = "Không có video sự kiện",
                                        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)
                                    )
                                }
                            }
                            else -> {
                                CommonEmptyState(
                                    text = "Chưa hỗ trợ",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }


                    }

                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {


                // Tabs Lịch sử & Biển số
                    Row(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .padding(horizontal = 16.dp)
                            .offset(y = offsetY)
                            .alpha(finalAlpha)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        EventHistoryTab.values().forEach { tab ->
                            val isSelected = selectedHistoryTab == tab
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable { selectedHistoryTab = tab }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tab.title,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                val currentEventsList = if (selectedHistoryTab == EventHistoryTab.HISTORY) {
                    uiState.relatedEvents
                } else {
                    uiState.licensePlateEvents
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(8.dp)
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
                            val aiServiceName = uiState.aiServices.find { it.id == relatedEvent.serviceId }?.name ?: "Unknown Service"
                            Box(modifier = Modifier.offset(y = offsetY).alpha(finalAlpha)) {
                                RecentEventItem(
                                    event = relatedEvent,
                                    cameraName = cameraName,
                                    aiServiceName = aiServiceName,
                                    isSelected = relatedEvent.id == event.id,
                                    enableSharedElement = false,
                                    onClick = { 
                                        if (selectedHistoryTab == EventHistoryTab.HISTORY) {
                                            viewModel.selectBaseEvent(relatedEvent)
                                        } else {
                                            viewModel.selectEvent(relatedEvent)
                                        }
                                        selectedTab = EventMediaTab.EVENT_IMAGE
                                        expandedCropUrl = null
                                    }
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
