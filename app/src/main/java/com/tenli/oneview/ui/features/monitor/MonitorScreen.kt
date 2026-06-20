package com.tenli.oneview.ui.features.monitor

import android.widget.Toast
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
                        onRemoveClick = { viewModel.removeCamera(index) },
                        onRetryClick = { viewModel.retryCameraStream(index) },
                        onSuccess = { viewModel.resetRetryCount(index) },
                        onFullscreenClick = { fullscreenIndex = index },
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
        Text(
            text = "Giám sát",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
        )

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

        // --- Tabs: Sự kiện | Playback ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val isEvents = uiState.selectedTab == MonitorTab.EVENTS
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isEvents) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { viewModel.setTab(MonitorTab.EVENTS) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Sự kiện", color = if (isEvents) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (!isEvents) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { viewModel.setTab(MonitorTab.PLAYBACK) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Playback", color = if (!isEvents) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            }
        }

        // --- Time Filter Scrollable Row ---
        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(MonitorTimeFilter.entries.toTypedArray()) { filter ->
                val isSelected = uiState.selectedTimeFilter == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { viewModel.setTimeFilter(filter) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = filter.title,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }
            }
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
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                if (uiState.selectedTab == MonitorTab.EVENTS) {
                    val currentCamera = uiState.selectedCameras.firstOrNull()?.camera
                    val cameraListForEvent = currentCamera?.let { listOf(it) } ?: emptyList()
                    items(uiState.events) { event ->
                        com.tenli.oneview.ui.features.home.RecentEventItem(
                            event = event,
                            cameraList = cameraListForEvent,
                            onClick = { /* TODO: Navigate to Event Details */ }
                        )
                    }
                } else {
                    val currentCameraName = uiState.selectedCameras.firstOrNull()?.camera?.name ?: "Camera"
                    items(uiState.playbacks) { playback ->
                        PlaybackItemView(playback, currentCameraName)
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
    onRemoveClick: () -> Unit,
    onRetryClick: () -> Unit,
    onSuccess: () -> Unit,
    onFullscreenClick: () -> Unit,
    isFullscreen: Boolean = false
) {
    val deviceKey = UserSession.accessToken
    val camera = selectedCamera.camera
    var isLiveViewPlaying by remember(selectedCamera.streamUrl) { mutableStateOf(false) }
    var isLiveViewError by remember(selectedCamera.streamUrl) { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isFullscreen) Modifier.fillMaxHeight() else Modifier.aspectRatio(16f / 9f))
            .clip(RoundedCornerShape(0.dp))
            .background(Color.Black)
    ) {
        if (selectedCamera.streamUrl.isNotEmpty()) {
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
        if (selectedCamera.streamUrl.isEmpty() || (!isLiveViewPlaying && !isLiveViewError)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        // Overlay for Camera Name and Close Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isLiveViewPlaying || isLiveViewError) Color.Black.copy(alpha = 0.5f) else Color.Transparent)
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLiveViewPlaying || isLiveViewError) {
                Text(
                    text = camera.name,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
            // Delete button
            IconButton(
                onClick = onRemoveClick,
                modifier = Modifier
                    .size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Xoá camera",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        // Fullscreen button at Bottom Right
        IconButton(
            onClick = onFullscreenClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .size(32.dp)
        ) {
            Icon(
                imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                contentDescription = "Toàn màn hình",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
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
fun PlaybackItemView(playback: com.tenli.oneview.model.network.VideoModel, cameraName: String) {
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
                java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
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
            .clickable { /* TODO: Playback click */ }
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
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
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
                fontSize = 14.sp
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
