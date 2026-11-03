package com.tenli.oneview.ui.features.monitor

import android.app.Activity
import android.content.pm.ActivityInfo
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tenli.oneview.R
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.model.network.CameraModel
import com.tenli.oneview.ui.component.CommonEmptyState
import com.tenli.oneview.ui.component.MobileTopBar
import com.tenli.oneview.ui.component.VideoPlayer
import com.tenli.oneview.ui.component.WaveDotsLoading
import com.tenli.oneview.ui.theme.BrandAccent
import com.tenli.oneview.ui.theme.LucideIcons
import com.tenli.oneview.ui.theme.ThemeManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitorScreen(
    viewModel: MonitorViewModel = viewModel(factory = MonitorViewModel.Factory),
    listState: androidx.compose.foundation.lazy.LazyListState = androidx.compose.foundation.lazy.rememberLazyListState(),
    isTabActive: Boolean = true,
    onEventClick: (Int) -> Unit = {},
    onPlaybackClick: (videoLink: String, time: String, imageLink: String, cameraName: String) -> Unit = { _, _, _, _ -> },
    onFullscreenChange: (Boolean) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var fullscreenIndex by remember { mutableStateOf<Int?>(null) }
    val activity = LocalContext.current as? Activity
    val coroutineScope = rememberCoroutineScope()

    val themeMode by ThemeManager.themeModeFlow.collectAsStateWithLifecycle()
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        0 -> false
        1 -> true
        else -> isSystemDark
    }

    var wasDeactivated by remember { mutableStateOf(false) }
    LaunchedEffect(isTabActive) {
        if (!isTabActive) {
            wasDeactivated = true
            if (fullscreenIndex != null) {
                fullscreenIndex = null
            }
        } else if (wasDeactivated) {
            wasDeactivated = false
            viewModel.refreshActiveStreams()
        }
    }

    LaunchedEffect(fullscreenIndex) {
        onFullscreenChange(fullscreenIndex != null)
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
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

    val activeCameraIds = remember(uiState.selectedCameras, uiState.gridSize) {
        (0 until uiState.gridSize).mapNotNull { idx ->
            uiState.selectedCameras.getOrNull(idx)?.camera?.id
        }.toSet()
    }

    val filteredTree = remember(uiState.treeData, uiState.searchQuery) {
        val query = uiState.searchQuery.trim().lowercase()
        if (query.isEmpty()) {
            uiState.treeData
        } else {
            filterTreeNodes(uiState.treeData, query)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Mobile Top Bar matching MobileLivePage.vue
            MobileTopBar(
                title = stringResource(id = R.string.tab_monitor)
            )

            // 16:9 Live Video Player Container (supports 1x1 and 2x2 grid)
            // Đồng bộ màu nền theo theme từ MobileLivePage.vue & LiveVideoView.vue (--color-input-bg / --color-card-bg)
            val liveContainerBg = if (isDark) Color(0xFF1E293B) else Color(0xFFE8ECF2)
            val gridDividerBg = if (isDark) Color(0xFF2E3A4E) else Color(0xFFD5DCE6)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(if (uiState.gridSize == 4) gridDividerBg else liveContainerBg)
            ) {
                if (uiState.gridSize == 1) {
                    val selectedCam = uiState.selectedCameras.getOrNull(0)
                    if (fullscreenIndex == 0) {
                        Box(modifier = Modifier.fillMaxSize().background(liveContainerBg))
                    } else if (selectedCam != null) {
                        CameraGridItem(
                            selectedCamera = selectedCam,
                            isTabActive = isTabActive,
                            isDark = isDark,
                            onClick = {},
                            onRetryClick = { viewModel.retryCameraStream(0) },
                            onSuccess = { viewModel.resetRetryCount(0) },
                            onSnapshotError = { viewModel.onSnapshotLoadError(0) },
                            onFullscreenClick = { fullscreenIndex = 0 },
                            onBackToLiveClick = { viewModel.addCamera(selectedCam.camera, 0) },
                            onRemoveClick = { viewModel.removeCamera(0) },
                            isFullscreen = false,
                            isCompact = false
                        )
                    } else {
                        EmptyGridItem(onClick = {}, isCompact = false, isDark = isDark)
                    }
                } else {
                    // 2x2 Grid (1.dp gap matching .mobile-grid-2x2 in MobileLivePage.vue)
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        for (row in 0..1) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                for (col in 0..1) {
                                    val slotIdx = row * 2 + col
                                    val selectedCam = uiState.selectedCameras.getOrNull(slotIdx)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .background(liveContainerBg)
                                    ) {
                                        if (fullscreenIndex == slotIdx) {
                                            Box(modifier = Modifier.fillMaxSize().background(liveContainerBg))
                                        } else if (selectedCam != null) {
                                            CameraGridItem(
                                                selectedCamera = selectedCam,
                                                isTabActive = isTabActive,
                                                isDark = isDark,
                                                onClick = {},
                                                onRetryClick = { viewModel.retryCameraStream(slotIdx) },
                                                onSuccess = { viewModel.resetRetryCount(slotIdx) },
                                                onSnapshotError = { viewModel.onSnapshotLoadError(slotIdx) },
                                                onFullscreenClick = { fullscreenIndex = slotIdx },
                                                onBackToLiveClick = { viewModel.addCamera(selectedCam.camera, slotIdx) },
                                                onRemoveClick = { viewModel.removeCamera(slotIdx) },
                                                isFullscreen = false,
                                                isCompact = true
                                            )
                                        } else {
                                            EmptyGridItem(onClick = {}, isCompact = true, isDark = isDark)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Embedded Camera Tree Card matching MobileLivePage.vue (.mobile-camera-tree-wrapper)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 10.dp)
                    .padding(bottom = 86.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                // Top Row: Search Bar (left) + Grid Selector 1 / 4 (right)
                val controlBg = if (isDark) Color(0xFF242F41) else Color(0xFFEDF0F5)
                val mutedText = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Search Input
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(controlBg)
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = LucideIcons.Search,
                            contentDescription = "Tìm camera",
                            tint = mutedText,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                            if (uiState.searchQuery.isEmpty()) {
                                Text(
                                    text = "Tìm camera...",
                                    color = mutedText,
                                    fontSize = 14.sp
                                )
                            }
                            BasicTextField(
                                value = uiState.searchQuery,
                                onValueChange = { viewModel.setSearchQuery(it) },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp
                                ),
                                cursorBrush = SolidColor(BrandAccent),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (uiState.searchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = LucideIcons.X,
                                contentDescription = "Xóa",
                                tint = mutedText,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { viewModel.setSearchQuery("") }
                            )
                        }
                    }

                    // Grid Selector (1x1 / 2x2) matching .mobile-grid-selector ("1" and "4")
                    Row(
                        modifier = Modifier
                            .height(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(controlBg)
                            .padding(3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        GridOptionButton(
                            label = "1",
                            isSelected = uiState.gridSize == 1,
                            isDark = isDark,
                            onClick = { viewModel.setGridSize(1) }
                        )
                        GridOptionButton(
                            label = "4",
                            isSelected = uiState.gridSize == 4,
                            isDark = isDark,
                            onClick = { viewModel.setGridSize(4) }
                        )
                    }
                }

                // Camera Tree List
                if (uiState.isLoading && uiState.treeData.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        WaveDotsLoading()
                    }
                } else if (filteredTree.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CommonEmptyState(text = "Không có camera nào")
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(items = filteredTree, key = { it.key }) { node ->
                            CameraTreeNodeView(
                                node = node,
                                depth = 0,
                                expandedNodes = if (uiState.searchQuery.isNotBlank()) {
                                    collectAllNodeKeys(filteredTree)
                                } else {
                                    uiState.expandedNodes
                                },
                                cameraStatusMap = uiState.cameraStatusMap,
                                recordStatusMap = uiState.recordStatusMap,
                                activeCameraIds = activeCameraIds,
                                onToggleExpand = { viewModel.toggleNodeExpansion(it) },
                                onCameraClick = { camera ->
                                    viewModel.selectCameraFromTree(camera)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Fullscreen Overlay
        if (fullscreenIndex != null) {
            val index = fullscreenIndex!!
            val selectedCam = uiState.selectedCameras.getOrNull(index)

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
                        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
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
                            onClick = {}
                        )
                ) {
                    CameraGridItem(
                        selectedCamera = selectedCam,
                        isDark = isDark,
                        onClick = {},
                        onRetryClick = { viewModel.retryCameraStream(index) },
                        onSuccess = { viewModel.resetRetryCount(index) },
                        onSnapshotError = { viewModel.onSnapshotLoadError(index) },
                        onFullscreenClick = {
                            coroutineScope.launch {
                                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                                val window = activity?.window
                                val insetsController = window?.let { androidx.core.view.WindowCompat.getInsetsController(it, it.decorView) }
                                insetsController?.show(androidx.core.view.WindowInsetsCompat.Type.systemBars())

                                kotlinx.coroutines.delay(150)
                                fullscreenIndex = null
                            }
                        },
                        onBackToLiveClick = { viewModel.addCamera(selectedCam.camera, index) },
                        onRemoveClick = {
                            fullscreenIndex = null
                            viewModel.removeCamera(index)
                        },
                        isFullscreen = true,
                        isCompact = false
                    )
                }
            } else {
                fullscreenIndex = null
            }
        }
    }
}

@Composable
private fun GridOptionButton(
    label: String,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) BrandAccent else Color.Transparent
    val contentColor = if (isSelected) {
        Color.White
    } else if (isDark) {
        Color(0xFF94A3B8)
    } else {
        Color(0xFF64748B)
    }

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(30.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = contentColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun filterTreeNodes(nodes: List<CameraTreeNode>, query: String): List<CameraTreeNode> {
    val result = mutableListOf<CameraTreeNode>()
    for (node in nodes) {
        when (node) {
            is CameraTreeNode.CameraLeaf -> {
                if (node.camera.name.lowercase().contains(query)) {
                    result.add(node)
                }
            }
            is CameraTreeNode.GroupNode -> {
                if (node.group.name.lowercase().contains(query)) {
                    result.add(node)
                } else {
                    val filteredChildren = filterTreeNodes(node.children, query)
                    if (filteredChildren.isNotEmpty()) {
                        result.add(node.copy(children = filteredChildren.toMutableList()))
                    }
                }
            }
            is CameraTreeNode.VMSNode -> {
                val filteredChildren = filterTreeNodes(node.children, query)
                if (filteredChildren.isNotEmpty()) {
                    result.add(node.copy(children = filteredChildren.toMutableList()))
                }
            }
        }
    }
    return result
}

private fun collectAllNodeKeys(nodes: List<CameraTreeNode>): Set<String> {
    val keys = mutableSetOf<String>()
    fun walk(list: List<CameraTreeNode>) {
        for (n in list) {
            keys.add(n.key)
            if (n is CameraTreeNode.VMSNode) walk(n.children)
            if (n is CameraTreeNode.GroupNode) walk(n.children)
        }
    }
    walk(nodes)
    return keys
}

@Composable
fun CameraGridItem(
    selectedCamera: SelectedCamera,
    isTabActive: Boolean = true,
    isDark: Boolean = true,
    onClick: () -> Unit,
    onRetryClick: () -> Unit,
    onSuccess: () -> Unit,
    onSnapshotError: () -> Unit = {},
    onFullscreenClick: () -> Unit,
    onBackToLiveClick: () -> Unit,
    onRemoveClick: () -> Unit = {},
    isFullscreen: Boolean = false,
    isCompact: Boolean = false
) {
    val deviceKey = UserSession.accessToken
    var isLiveViewPlaying by remember(selectedCamera.camera.id, selectedCamera.streamUrl, isTabActive) { mutableStateOf(false) }
    var isLiveViewError by remember(selectedCamera.camera.id, selectedCamera.streamUrl, isTabActive) { mutableStateOf(false) }
    var snapshotFailed by remember(selectedCamera.camera.id, selectedCamera.fallbackImageUrl) { mutableStateOf(false) }
    val hasError = isLiveViewError || selectedCamera.streamUrl.startsWith("error://")

    // Đồng bộ màu nền container của LiveVideoView.vue (.base, .loading, .unsupported):
    // Chỉ dùng nền đen khi đang phát video thực tế hoặc ở chế độ toàn màn hình; khi đang tải/lỗi dùng màu surface theo theme.
    val surfaceBg = when {
        isFullscreen || isLiveViewPlaying -> Color.Black
        isDark -> Color(0xFF1E293B)
        else -> Color(0xFFE8ECF2)
    }

    val canPlayStream = isTabActive &&
        selectedCamera.streamUrl.isNotEmpty() &&
        !selectedCamera.streamUrl.startsWith("error://")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceBg)
    ) {
        if (canPlayStream) {
            if (selectedCamera.streamUrl.startsWith("ws")) {
                com.tenli.oneview.ui.component.LiveStreamPlayer(
                    wsUrl = selectedCamera.streamUrl,
                    videoCodecTag = selectedCamera.videoCodecTag.ifEmpty { "hvc1.1.6.L90" },
                    cameraId = selectedCamera.camera.id,
                    altCameraId = selectedCamera.camera.cameraId,
                    showInternalOverlay = false,
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

        // Cached Snapshot Preview + Loading Overlay (đồng bộ .cached-snapshot-preview và fade-snapshot 0.35s trong LiveVideoView.vue)
        val isLoadingStream = !isLiveViewPlaying && !hasError
        AnimatedVisibility(
            visible = isLoadingStream,
            enter = fadeIn(animationSpec = tween(150)),
            exit = fadeOut(animationSpec = tween(350)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isFullscreen) Color.Black else if (isDark) Color(0xFF1E293B) else Color(0xFFE8ECF2)),
                contentAlignment = Alignment.Center
            ) {
                if (selectedCamera.fallbackImageUrl.isNotEmpty() && !snapshotFailed) {
                    coil.compose.AsyncImage(
                        model = selectedCamera.fallbackImageUrl,
                        contentDescription = selectedCamera.camera.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        onError = {
                            snapshotFailed = true
                            onSnapshotError()
                        }
                    )
                    // Lớp phủ mờ nhẹ giúp 3 chấm loading luôn nổi bật trên nền ảnh snapshot sáng
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.16f))
                    )
                }
                WaveDotsLoading(dotColor = BrandAccent)
            }
        }

        // Error Overlay (chạm để thử tải lại luồng live giống reloadStream trong LiveVideoView.vue)
        if (hasError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isFullscreen) Color.Black else if (isDark) Color(0xFF1E293B) else Color(0xFFE8ECF2))
                    .clickable { onBackToLiveClick() },
                contentAlignment = Alignment.Center
            ) {
                val errorMsg = when {
                    selectedCamera.streamUrl.startsWith("error://no-video") -> "Không có tín hiệu"
                    selectedCamera.streamUrl.startsWith("error://failed") -> "Lỗi kết nối camera"
                    else -> "Lỗi tải video"
                }
                CommonEmptyState(text = errorMsg)
            }
        }

        // Camera Name + LIVE badge at Top-Left
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(if (isCompact) 4.dp else 8.dp)
                .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(Color(0xFFEF4444), CircleShape)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = selectedCamera.camera.name,
                color = Color.White,
                fontSize = if (isCompact) 11.sp else 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Close / Remove Camera Button at Top-Right (when not fullscreen)
        if (!isFullscreen) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(if (isCompact) 4.dp else 8.dp)
                    .size(if (isCompact) 22.dp else 26.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable { onRemoveClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = LucideIcons.X,
                    contentDescription = "Đóng camera",
                    tint = Color.White,
                    modifier = Modifier.size(if (isCompact) 13.dp else 15.dp)
                )
            }
        }

        // Fullscreen Button at Bottom-Right (có nền pill bán trong suốt giống .fullscreen-btn trong LiveVideoView.vue)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(if (isCompact) 4.dp else 8.dp)
                .size(if (isCompact) 24.dp else 30.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Black.copy(alpha = 0.50f))
                .clickable { onFullscreenClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isFullscreen) LucideIcons.Minimize else LucideIcons.Maximize,
                contentDescription = "Toàn màn hình",
                tint = Color.White,
                modifier = Modifier.size(if (isCompact) 14.dp else 16.dp)
            )
        }
    }
}

@Composable
fun CameraTreeNodeView(
    node: CameraTreeNode,
    depth: Int,
    expandedNodes: Set<String>,
    cameraStatusMap: Map<String, String>,
    recordStatusMap: Map<String, String> = emptyMap(),
    activeCameraIds: Set<Int> = emptySet(),
    onToggleExpand: (String) -> Unit,
    onCameraClick: (CameraModel) -> Unit
) {
    val paddingStart = 6.dp + (depth * 14).dp

    when (node) {
        is CameraTreeNode.VMSNode -> {
            val isExpanded = expandedNodes.contains(node.key)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onToggleExpand(node.key) }
                    .padding(vertical = 8.dp, horizontal = 4.dp)
                    .padding(start = paddingStart),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isExpanded) LucideIcons.ChevronDown else LucideIcons.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Icon(
                    imageVector = LucideIcons.Server,
                    contentDescription = null,
                    tint = BrandAccent,
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .size(16.dp)
                )
                Text(
                    text = node.vms.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (isExpanded) {
                node.children.forEach { child ->
                    CameraTreeNodeView(
                        node = child,
                        depth = depth + 1,
                        expandedNodes = expandedNodes,
                        cameraStatusMap = cameraStatusMap,
                        recordStatusMap = recordStatusMap,
                        activeCameraIds = activeCameraIds,
                        onToggleExpand = onToggleExpand,
                        onCameraClick = onCameraClick
                    )
                }
            }
        }
        is CameraTreeNode.GroupNode -> {
            val isExpanded = expandedNodes.contains(node.key)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onToggleExpand(node.key) }
                    .padding(vertical = 7.dp, horizontal = 4.dp)
                    .padding(start = paddingStart),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isExpanded) LucideIcons.ChevronDown else LucideIcons.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Icon(
                    imageVector = LucideIcons.Folder,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .size(16.dp)
                )
                Text(
                    text = node.group.name,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (isExpanded) {
                node.children.forEach { child ->
                    CameraTreeNodeView(
                        node = child,
                        depth = depth + 1,
                        expandedNodes = expandedNodes,
                        cameraStatusMap = cameraStatusMap,
                        recordStatusMap = recordStatusMap,
                        activeCameraIds = activeCameraIds,
                        onToggleExpand = onToggleExpand,
                        onCameraClick = onCameraClick
                    )
                }
            }
        }
        is CameraTreeNode.CameraLeaf -> {
            val wsStatus = cameraStatusMap[node.key]
            val issues = node.camera.extra?.latestReport?.issues ?: emptyList()
            val isOfflineFromIssues = issues.contains("loi_ket_noi") || issues.contains("mat_ket_noi")
            val isOffline = when {
                wsStatus == "red" || wsStatus == "offline" -> true
                wsStatus == "green" || wsStatus == "orange" -> false
                else -> isOfflineFromIssues || node.camera.extra?.status == "red" || node.camera.extra?.status == "offline"
            }
            val statusColor = if (isOffline) Color(0xFFEF4444) else Color(0xFF10B981)
            val isSelected = activeCameraIds.contains(node.camera.id)
            val recordStatus = recordStatusMap[node.key]
                ?: node.camera.extra?.recordStatus
                ?: if (node.camera.enableRecord) "green" else null

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) BrandAccent.copy(alpha = 0.14f) else Color.Transparent
                    )
                    .clickable { onCameraClick(node.camera) }
                    .padding(vertical = 8.dp)
                    .padding(start = paddingStart + 20.dp, end = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = LucideIcons.Video,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(16.dp)
                )
                Text(
                    text = node.camera.name,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) BrandAccent else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (!recordStatus.isNullOrBlank()) {
                    val recColor = when (recordStatus) {
                        "green" -> Color(0xFF10B981)
                        "yellow", "orange" -> Color(0xFFF59E0B)
                        "red" -> Color(0xFFEF4444)
                        else -> Color.Transparent
                    }
                    if (recColor != Color.Transparent) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(recColor, CircleShape)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyGridItem(onClick: () -> Unit, isCompact: Boolean = false, isDark: Boolean = true) {
    val bgColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE8ECF2)
    val contentColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = LucideIcons.Video,
            contentDescription = "Chọn Camera",
            tint = contentColor.copy(alpha = 0.65f),
            modifier = Modifier.size(if (isCompact) 26.dp else 40.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Chọn camera bên dưới",
            color = contentColor.copy(alpha = 0.8f),
            fontSize = if (isCompact) 12.sp else 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun PlaybackItemView(playback: com.tenli.oneview.model.network.VideoModel, cameraName: String, isSelected: Boolean = false, onClick: () -> Unit) {
    val formattedTime = remember(playback.time) {
        var parsedDate: java.util.Date? = null

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
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            val timeAnnotatedString = remember(formattedTime) {
                buildAnnotatedString {
                    val parts = formattedTime.split(" ")
                    if (parts.size == 2) {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
