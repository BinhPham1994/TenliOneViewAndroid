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
import com.tenli.oneview.ui.component.TimeFilterDropdown
import com.tenli.oneview.ui.theme.BrandPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitorScreen(
    viewModel: MonitorViewModel = viewModel(factory = MonitorViewModel.Factory),
    listState: androidx.compose.foundation.lazy.LazyListState = androidx.compose.foundation.lazy.rememberLazyListState(),
    onEventClick: (Int) -> Unit = {},
    onPlaybackClick: (videoLink: String, time: String, imageLink: String, cameraName: String) -> Unit = { _, _, _, _ -> },
    onFullscreenChange: (Boolean) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showBottomSheet by remember { mutableStateOf(false) }
    var activeSlotIndex by remember { mutableIntStateOf(-1) }
    val context = LocalContext.current
    var fullscreenIndex by remember { mutableStateOf<Int?>(null) }
    val activity = LocalContext.current as? Activity

    LaunchedEffect(fullscreenIndex) {
        onFullscreenChange(fullscreenIndex != null)
    }

    val coroutineScope = rememberCoroutineScope()
    
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
                        onFullscreenClick = {
                            if (fullscreenIndex == index) {
                                coroutineScope.launch {
                                    // Trigger orientation change FIRST so system captures a non-black screenshot
                                    activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                                    val window = activity?.window
                                    val insetsController = window?.let { androidx.core.view.WindowCompat.getInsetsController(it, it.decorView) }
                                    insetsController?.show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                                    
                                    kotlinx.coroutines.delay(150)
                                    fullscreenIndex = null
                                }
                            } else {
                                fullscreenIndex = index
                            }
                        },
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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
        // Header
        val selectedCam = uiState.selectedCameras.firstOrNull()
        val headerText = selectedCam?.camera?.name ?: "Giám sát"
        val vmsName = selectedCam?.vmsName?.takeIf { it.isNotBlank() }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clickable {
                    activeSlotIndex = 0
                    showBottomSheet = true
                }
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .height(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null,
                    tint = BrandPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = headerText,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.labelLarge,
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
            
            if (vmsName != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Row(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        tint = BrandPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = vmsName,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }
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
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isEvents = uiState.selectedTab == MonitorTab.EVENTS
            
            // Tabs Segmented Control
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
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
                        .clip(RoundedCornerShape(8.dp))
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
            // TimeFilterDropdown instead of MonitorTimeFilterDropdown
            TimeFilterDropdown(
                selectedFilter = uiState.selectedTimeFilter,
                onFilterSelected = { viewModel.setTimeFilter(it) }
            )
        }

        // --- Danh sách (Events / Playbacks) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 10.dp)
        ) {
            val isEmpty = if (uiState.selectedTab == MonitorTab.EVENTS) uiState.events.isEmpty() else uiState.playbacks.isEmpty()
            
            if (uiState.isLoading && isEmpty) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    com.tenli.oneview.ui.component.WaveDotsLoading()
                }
            } else if (isEmpty) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    com.tenli.oneview.ui.component.CommonEmptyState(
                        text = "Không có dữ liệu"
                    )
                }
            } else {
                androidx.compose.foundation.lazy.LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (uiState.selectedTab == MonitorTab.EVENTS) {
                        val currentCamera = uiState.selectedCameras.firstOrNull()?.camera
                        val cameraName = currentCamera?.name ?: "Camera"
                        val currentStreamUrl = uiState.selectedCameras.firstOrNull()?.streamUrl ?: ""
                        items(items = uiState.events, key = { it.id }) { event ->
                            val videoUrl = event.data?.video ?: ""
                            val dummyUrl = "error://no-video?id=${event.id}"
                            val isSelected = currentStreamUrl.isNotEmpty() && 
                                (if (videoUrl.isNotEmpty()) currentStreamUrl.endsWith(videoUrl) else currentStreamUrl == dummyUrl)

                            val aiServiceName = uiState.aiServices.find { it.id == event.serviceId }?.name ?: "Unknown Service"

                            com.tenli.oneview.ui.features.home.RecentEventItem(
                                event = event,
                                cameraName = cameraName,
                                aiServiceName = aiServiceName,
                                isSelected = isSelected,
                                onClick = { 
                                    onEventClick(event.id)
                                }
                            )
                        }
                        if (uiState.hasMoreEvents && uiState.events.isNotEmpty()) {
                            item {
                                LaunchedEffect(Unit) {
                                    viewModel.loadMoreData()
                                }
                                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    if (uiState.isPaginating) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 6.dp)
                                    }
                                }
                            }
                        }
                    } else {
                        val currentCameraName = uiState.selectedCameras.firstOrNull()?.camera?.name ?: "Camera"
                        val currentStreamUrl = uiState.selectedCameras.firstOrNull()?.streamUrl ?: ""
                        items(items = uiState.playbacks, key = { it.videoLink }) { playback ->
                            val isSelected = currentStreamUrl.isNotEmpty() && playback.videoLink.isNotEmpty() && currentStreamUrl.endsWith(playback.videoLink)
                            PlaybackItemView(
                                playback = playback, 
                                cameraName = currentCameraName,
                                isSelected = isSelected,
                                onClick = { 
                                    onPlaybackClick(playback.videoLink, playback.time, playback.imageLink, currentCameraName)
                                }
                            )
                        }
                        if (uiState.hasMorePlaybacks && uiState.playbacks.isNotEmpty()) {
                            item {
                                LaunchedEffect(Unit) {
                                    viewModel.loadMoreData()
                                }
                                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    if (uiState.isPaginating) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 6.dp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showBottomSheet) {
            Dialog(
                onDismissRequest = { showBottomSheet = false },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = false
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null,
                            onClick = { showBottomSheet = false }
                        ),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.8f)
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null,
                                onClick = {} // Chặn click xuyên qua box để không bị đóng khi bấm nhầm
                            ),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .navigationBarsPadding()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Chọn Camera",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                IconButton(onClick = { showBottomSheet = false }) {
                                    Icon(Icons.Default.Close, contentDescription = "Đóng")
                                }
                            }

                            if (uiState.isLoading) {
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(strokeWidth = 6.dp)
                                }
                            } else {
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    items(items = uiState.treeData, key = { it.key }) { node ->
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
        } // End of Column

        // Fullscreen overlay
        if (fullscreenIndex != null) {
            val index = fullscreenIndex!!
            val selectedCam = uiState.selectedCameras[index]
            
            if (selectedCam != null) {
                DisposableEffect(Unit) {
                    val originalOrientation = activity?.requestedOrientation
                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    
                    val window = activity?.window
                    val insetsController = window?.let { androidx.core.view.WindowCompat.getInsetsController(it, it.decorView) }
                    
                    if (insetsController != null) {
                        insetsController.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                        insetsController.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                    }
                    
                    onDispose {
                        activity?.requestedOrientation = originalOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        insetsController?.show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                    }
                }
                
                androidx.activity.compose.BackHandler {
                    coroutineScope.launch {
                        activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        val window = activity?.window
                        val insetsController = window?.let { androidx.core.view.WindowCompat.getInsetsController(it, it.decorView) }
                        insetsController?.show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                        
                        kotlinx.coroutines.delay(150)
                        fullscreenIndex = null
                    }
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null,
                            onClick = {} // Block clicks from passing through
                        )
                ) {
                    cameraItems[index](selectedCam)
                }
            } else {
                fullscreenIndex = null
            }
        }
    } // End of root Box
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
    val hasError = isLiveViewError || selectedCamera.streamUrl.startsWith("error://")
    
    var shouldRenderPlayer by remember(selectedCamera.streamUrl) { mutableStateOf(false) }
    LaunchedEffect(selectedCamera.streamUrl) {
        if (selectedCamera.streamUrl.isNotEmpty() && !selectedCamera.streamUrl.startsWith("error://")) {
            kotlinx.coroutines.delay(350)
            shouldRenderPlayer = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isFullscreen) Modifier.fillMaxHeight() else Modifier.aspectRatio(16f / 9f))
            .clip(RoundedCornerShape(0.dp))
            .background(Color.Black)
    ) {
        if (shouldRenderPlayer && selectedCamera.streamUrl.isNotEmpty() && !selectedCamera.streamUrl.startsWith("error://")) {
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
        if (selectedCamera.streamUrl.isEmpty() || (!isLiveViewPlaying && !hasError && selectedCamera.streamUrl.isNotEmpty() && !selectedCamera.streamUrl.startsWith("error://"))) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                com.tenli.oneview.ui.component.WaveDotsLoading(
                    dotColor = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Error Overlay
        if (hasError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                val errorMsg = when {
                    selectedCamera.streamUrl.startsWith("error://no-video") -> "Không có video cho sự kiện này"
                    selectedCamera.streamUrl.startsWith("error://failed") -> "Lỗi không xem được camera"
                    else -> "Lỗi tải video"
                }

                if (selectedCamera.fallbackImageUrl.isNotEmpty()) {
                    com.tenli.oneview.ui.component.VideoFrameImage(
                        url = selectedCamera.fallbackImageUrl,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        errorContent = {
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                com.tenli.oneview.ui.component.CommonEmptyState(text = errorMsg)
                            }
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
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
                Icon(if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(Icons.Default.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 8.dp))
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
                Icon(if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 8.dp))
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
                Icon(Icons.Default.Videocam, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(end = 8.dp))
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
            .background(MaterialTheme.colorScheme.surface)
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
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
            .clickable { onClick() }
            .padding(start = 4.dp, top = 4.dp, bottom = 4.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(140.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(8.dp))
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
            val timeAnnotatedString = remember(formattedTime) {
                androidx.compose.ui.text.buildAnnotatedString {
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
                }
            }
            Text(
                text = timeAnnotatedString,
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
