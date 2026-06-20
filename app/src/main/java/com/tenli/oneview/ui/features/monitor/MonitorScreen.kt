package com.tenli.oneview.ui.features.monitor

import android.widget.Toast
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.content.pm.ActivityInfo
import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.model.network.CameraModel
import com.tenli.oneview.ui.component.VideoPlayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitorScreen(
    viewModel: MonitorViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showBottomSheet by remember { mutableStateOf(false) }
    var activeSlotIndex by remember { mutableIntStateOf(-1) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    var fullscreenIndex by remember { mutableStateOf<Int?>(null) }
    val activity = LocalContext.current as? Activity

    val cameraItems = remember(uiState.selectedCameras.size) {
        List(uiState.selectedCameras.size) { index ->
            movableContentOf { selectedCam: SelectedCamera? ->
                if (selectedCam != null) {
                    CameraGridItem(
                        selectedCamera = selectedCam,
                        onClick = {
                            activeSlotIndex = index
                            showBottomSheet = true
                        },
                        onRetryClick = { viewModel.retryCameraStream(index) },
                        onSuccess = { viewModel.resetRetryCount(index) },
                        onFullscreenClick = { fullscreenIndex = index },
                        onBackToLiveClick = { viewModel.addCamera(selectedCam.camera, index) },
                        isFullscreen = fullscreenIndex == index
                    )
                } else {
                    EmptyGridItem(
                        onClick = {
                            activeSlotIndex = index
                            showBottomSheet = true
                        }
                    )
                }
            }
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    // Re-fetch stream URLs when returning from background or switching tabs
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        var isFirstResume = true
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                if (isFirstResume) {
                    isFirstResume = false
                } else {
                    viewModel.refreshActiveStreams()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (fullscreenIndex != null) {
        val index = fullscreenIndex!!
        val selectedCam = uiState.selectedCameras[index]
        
        if (selectedCam != null) {
            Dialog(
                onDismissRequest = { fullscreenIndex = null },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = false
                )
            ) {
                // Remove the default fade-in/out animations and dim background of the Dialog Window
                val dialogWindowProvider = androidx.compose.ui.platform.LocalView.current.parent as? androidx.compose.ui.window.DialogWindowProvider
                dialogWindowProvider?.window?.let { window ->
                    window.setWindowAnimations(0)
                    window.setDimAmount(0f)
                }

                DisposableEffect(Unit) {
                    val originalOrientation = activity?.requestedOrientation
                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    onDispose {
                        activity?.requestedOrientation = originalOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    }
                }
                
                Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                    cameraItems[index](selectedCam)
                }
            }
        } else {
            fullscreenIndex = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        val headerText = uiState.selectedCameras.firstOrNull()?.camera?.name ?: "Giám sát"
        Row(
            modifier = Modifier
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
                .clickable {
                    activeSlotIndex = 0
                    showBottomSheet = true
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = headerText,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Chọn Camera",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(uiState.selectedCameras.size) { index ->
                    if (fullscreenIndex == index) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black)
                        )
                    } else {
                        cameraItems[index](uiState.selectedCameras[index])
                    }
                }
            }
        }

        // --- Header Row: Tabs (Left) and Time Filter (Right) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isEvents = uiState.selectedTab == MonitorTab.EVENTS
            
            // Tabs Segmented Control
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isEvents) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { viewModel.setTab(MonitorTab.EVENTS) }
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Sự kiện",
                        color = if (isEvents) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isEvents) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (!isEvents) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { viewModel.setTab(MonitorTab.PLAYBACK) }
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Playback",
                        color = if (!isEvents) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (!isEvents) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }

            // Time Filter Dropdown
            MonitorTimeFilterDropdown(
                selectedFilter = uiState.selectedTimeFilter,
                onFilterSelected = { viewModel.setTimeFilter(it) }
            )
        }

        // --- Danh sách (Events / Playbacks) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .background(Color.White, shape = RoundedCornerShape(12.dp))
        ) {
            val isEmpty = if (uiState.selectedTab == MonitorTab.EVENTS) uiState.events.isEmpty() else uiState.playbacks.isEmpty()
            
            if (isEmpty) {
                com.tenli.oneview.ui.component.CommonEmptyState(
                    text = "Không có dữ liệu",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    if (uiState.selectedTab == MonitorTab.EVENTS) {
                        val currentCamera = uiState.selectedCameras.firstOrNull()?.camera
                        val cameraListForEvent = currentCamera?.let { listOf(it) } ?: emptyList()
                        val currentStreamUrl = uiState.selectedCameras.firstOrNull()?.streamUrl ?: ""
                        items(uiState.events) { event ->
                            val videoUrl = event.data?.video ?: ""
                            val dummyUrl = "error://no-video?id=${event.id}"
                            val isSelected = currentStreamUrl.isNotEmpty() && 
                                (if (videoUrl.isNotEmpty()) currentStreamUrl.endsWith(videoUrl) else currentStreamUrl == dummyUrl)

                            com.tenli.oneview.ui.features.home.RecentEventItem(
                                event = event,
                                cameraList = cameraListForEvent,
                                isSelected = isSelected,
                                onClick = { 
                                    val imageUrl = com.tenli.oneview.ui.features.home.getEventImageUrl(event) ?: ""
                                    if (videoUrl.isEmpty()) {
                                        viewModel.playVideo(dummyUrl, "EVENT", fallbackImageUrl = imageUrl)
                                    } else {
                                        viewModel.playVideo(videoUrl, "EVENT", fallbackImageUrl = imageUrl)
                                    }
                                }
                            )
                        }
                    } else {
                        val currentCameraName = uiState.selectedCameras.firstOrNull()?.camera?.name ?: "Camera"
                        val currentStreamUrl = uiState.selectedCameras.firstOrNull()?.streamUrl ?: ""
                        items(uiState.playbacks) { playback ->
                            val isSelected = currentStreamUrl.isNotEmpty() && playback.videoLink.isNotEmpty() && currentStreamUrl.endsWith(playback.videoLink)
                            PlaybackItemView(
                                playback = playback, 
                                cameraName = currentCameraName,
                                isSelected = isSelected,
                                onClick = { viewModel.playVideo(playback.videoLink, "PLAYBACK") }
                            )
                        }
                    }
                }
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.8f)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Chọn Camera",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (uiState.isLoading) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(uiState.treeData) { node ->
                                CameraTreeNodeView(
                                    node = node,
                                    depth = 0,
                                    expandedNodes = uiState.expandedNodes,
                                    onToggleExpand = { viewModel.toggleNodeExpansion(it) },
                                    onCameraClick = { camera ->
                                        if (activeSlotIndex in 0..3) {
                                            viewModel.addCamera(camera, activeSlotIndex)
                                        }
                                        showBottomSheet = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CameraGridItem(
    selectedCamera: SelectedCamera,
    onClick: () -> Unit,
    onRetryClick: () -> Unit,
    onSuccess: () -> Unit,
    onFullscreenClick: () -> Unit,
    onBackToLiveClick: () -> Unit,
    isFullscreen: Boolean = false
) {
    val deviceKey = UserSession.accessToken
    val camera = selectedCamera.camera
    var isLiveViewPlaying by remember(selectedCamera.streamUrl) { mutableStateOf(false) }
    var isLiveViewError by remember(selectedCamera.streamUrl) { mutableStateOf(false) }
    val hasError = isLiveViewError || selectedCamera.streamUrl.startsWith("error://no-video")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isFullscreen) Modifier.fillMaxHeight() else Modifier.aspectRatio(16f / 9f))
            .clip(RoundedCornerShape(0.dp))
            .background(Color.Black)
    ) {
        if (selectedCamera.streamUrl.isNotEmpty() && !selectedCamera.streamUrl.startsWith("error://no-video")) {
            if (selectedCamera.streamUrl.startsWith("ws")) {
                // WebSocket stream - use native ExoPlayer with forced FragmentedMp4Extractor
                com.tenli.oneview.ui.component.LiveStreamPlayer(
                    wsUrl = selectedCamera.streamUrl,
                    videoCodecTag = selectedCamera.videoCodecTag.ifEmpty { "hvc1.1.6.L90" },
                    onPlayingChange = { 
                        isLiveViewPlaying = it 
                        if (it) {
                            onSuccess()
                        }
                    },
                    onErrorChange = { isError -> 
                        isLiveViewError = isError 
                        if (isError) {
                            onRetryClick()
                        }
                    }
                )
            } else {
                // HTTP stream - use ExoPlayer
                VideoPlayer(
                    videoUrl = selectedCamera.streamUrl,
                    thumbnailUrl = null,
                    deviceKey = deviceKey,
                    onPlayingChange = { 
                        isLiveViewPlaying = it 
                        if (it) {
                            onSuccess()
                        }
                    },
                    onErrorChange = { isLiveViewError = it }
                )
            }
        }

        // Loading Overlay
        if (selectedCamera.streamUrl.isEmpty() || (!isLiveViewPlaying && !hasError && selectedCamera.streamUrl.isNotEmpty())) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        // Error Overlay
        if (hasError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                val errorMsg = if (selectedCamera.streamUrl.startsWith("error://no-video")) "Không có video cho sự kiện này" else "Lỗi tải video"

                if (selectedCamera.fallbackImageUrl.isNotEmpty()) {
                    com.tenli.oneview.ui.component.VideoFrameImage(
                        url = selectedCamera.fallbackImageUrl,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        errorContent = {
                            Box(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                com.tenli.oneview.ui.component.CommonEmptyState(text = errorMsg)
                            }
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        com.tenli.oneview.ui.component.CommonEmptyState(text = errorMsg)
                    }
                }
            }
        }

        // Stream Type Indicator Overlay (LIVE, EVENT, PLAYBACK)
        if (selectedCamera.streamUrl.isNotEmpty() && (isLiveViewPlaying || selectedCamera.streamType != "LIVE")) {
            val badgeText = selectedCamera.streamType
            val badgeColor = when (selectedCamera.streamType) {
                "LIVE" -> Color.Red
                "EVENT" -> Color(0xFFF59E0B) // Orange
                "PLAYBACK" -> Color(0xFF3B82F6) // Blue
                else -> Color.Gray
            }
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(badgeColor, androidx.compose.foundation.shape.CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = badgeText,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }
        }
        
        // Back to Live button
        if (selectedCamera.streamType != "LIVE") {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { onBackToLiveClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Về xem Live",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }
        }

        // Fullscreen button at Bottom Right
        IconButton(
            onClick = onFullscreenClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(48.dp)
        ) {
            Icon(
                imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                contentDescription = "Toàn màn hình",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun CameraTreeNodeView(
    node: CameraTreeNode,
    depth: Int,
    expandedNodes: Set<Any>,
    onToggleExpand: (Any) -> Unit,
    onCameraClick: (CameraModel) -> Unit
) {
    val paddingStart = (depth * 16).dp

    when (node) {
        is CameraTreeNode.VMSNode -> {
            val isExpanded = expandedNodes.contains(node.key)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand(node.key) }
                    .padding(vertical = 8.dp)
                    .padding(start = paddingStart),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight, contentDescription = null)
                Icon(Icons.Default.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 8.dp))
                Text(node.vms.name, style = MaterialTheme.typography.bodyLarge)
            }
            if (isExpanded) {
                node.children.forEach { child ->
                    CameraTreeNodeView(child, depth + 1, expandedNodes, onToggleExpand, onCameraClick)
                }
            }
        }
        is CameraTreeNode.GroupNode -> {
            val isExpanded = expandedNodes.contains(node.key)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand(node.key) }
                    .padding(vertical = 8.dp)
                    .padding(start = paddingStart),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight, contentDescription = null)
                Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(horizontal = 8.dp))
                Text(node.group.name, style = MaterialTheme.typography.bodyMedium)
            }
            if (isExpanded) {
                node.children.forEach { child ->
                    CameraTreeNodeView(child, depth + 1, expandedNodes, onToggleExpand, onCameraClick)
                }
            }
        }
        is CameraTreeNode.CameraLeaf -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCameraClick(node.camera) }
                    .padding(vertical = 8.dp)
                    .padding(start = paddingStart + 24.dp), // Extra padding to align with folder content
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Videocam, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.padding(end = 8.dp))
                Text(node.camera.name, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun EmptyGridItem(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Videocam,
            contentDescription = "Chọn Camera",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(56.dp)
        )
    }
}

@Composable
fun PlaybackItemView(playback: com.tenli.oneview.model.network.VideoModel, cameraName: String, isSelected: Boolean = false, onClick: () -> Unit) {
    val formattedTime = remember(playback.time) {
        var parsedDate: java.util.Date? = null
        
        // Try parsing as numeric timestamp first (like event time)
        val numericTime = playback.time.toDoubleOrNull()
        if (numericTime != null && numericTime > 0) {
            val timeMillis = if (numericTime < 100000000000.0) (numericTime * 1000).toLong() else numericTime.toLong()
            parsedDate = java.util.Date(timeMillis)
        } else {
            val formatters = listOf(
                java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", java.util.Locale.getDefault()).apply { timeZone = java.util.TimeZone.getTimeZone("UTC") },
                java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault()).apply { timeZone = java.util.TimeZone.getTimeZone("UTC") },
                java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()),
                java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()),
                java.text.SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", java.util.Locale.getDefault())
            )
            
            for (f in formatters) {
                try {
                    parsedDate = f.parse(playback.time)
                    if (parsedDate != null) break
                } catch (e: Exception) {
                    // ignore
                }
            }
        }
        
        val outputFormatter = java.text.SimpleDateFormat("HH:mm:ss dd/MM/yyyy", java.util.Locale.getDefault())
        parsedDate?.let { outputFormatter.format(it) } ?: playback.time
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(120.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFF3F4F6))
        ) {
            val rawLink = playback.thumbnailLink.ifEmpty { playback.imageLink }
            val link = if (rawLink.startsWith("http")) rawLink else if (rawLink.isNotEmpty()) "${UserSession.domain.trimEnd('/')}/$rawLink" else ""
            if (link.isNotEmpty()) {
                com.tenli.oneview.ui.component.VideoFrameImage(
                    url = link,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    cacheKey = "playback_${cameraName}_${playback.time}"
                )
            }
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = cameraName,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = androidx.compose.ui.text.buildAnnotatedString {
                    val parts = formattedTime.split(" ")
                    if (parts.size == 2) {
                        withStyle(style = androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.SemiBold, color = Color.DarkGray)) {
                            append(parts[0])
                        }
                        append(" ")
                        append(parts[1])
                    } else {
                        append(formattedTime)
                    }
                },
                color = Color.Gray,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "PLAYBACK",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(4.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun MonitorTimeFilterDropdown(
    selectedFilter: MonitorTimeFilter,
    onFilterSelected: (MonitorTimeFilter) -> Unit
) {
    var showBottomSheet by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    Box {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { showBottomSheet = true }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedFilter.title,
                color = Color.Black,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Select Time Filter",
                tint = Color.Black
            )
        }

        if (showBottomSheet) {
            androidx.compose.material3.ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = Color.White,
                dragHandle = { androidx.compose.material3.BottomSheetDefaults.DragHandle() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
                ) {
                    Text(
                        text = "Chọn thời gian",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp, start = 8.dp)
                    )
                    
                    MonitorTimeFilter.entries.forEach { filter ->
                        val isSelected = filter == selectedFilter
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                                .clickable {
                                    scope.launch {
                                        sheetState.hide()
                                        showBottomSheet = false
                                        onFilterSelected(filter)
                                    }
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = filter.title,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 16.sp,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}
