package com.tenli.oneview.ui.features.monitor

import android.app.Activity
import android.content.pm.ActivityInfo
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitorScreen(
    viewModel: MonitorViewModel = viewModel(factory = MonitorViewModel.Factory),
    listState: LazyListState = rememberLazyListState(),
    isTabActive: Boolean = true,
    onEventClick: (Int) -> Unit = {},
    onPlaybackClick: (videoLink: String, time: String, imageLink: String, cameraName: String) -> Unit = { _, _, _, _ -> },
    onFullscreenChange: (Boolean) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = LocalContext.current as? Activity
    val coroutineScope = rememberCoroutineScope()
    val cameraGridState = rememberLazyGridState()

    // fullscreenIndex != null: 1x1 single-camera fullscreen
    var fullscreenIndex by remember { mutableStateOf<Int?>(null) }
    // isGridFullscreen: 2x2 grid fullscreen matching MobileLivePage.vue
    var isGridFullscreen by remember { mutableStateOf(false) }

    val themeMode by ThemeManager.themeModeFlow.collectAsStateWithLifecycle()
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        0 -> false
        1 -> true
        else -> isSystemDark
    }

    var wasDeactivated by remember { mutableStateOf(false) }
    LaunchedEffect(isTabActive) {
        viewModel.setMonitorTabActive(isTabActive)
        if (!isTabActive) {
            wasDeactivated = true
            fullscreenIndex = null
            isGridFullscreen = false
        } else if (wasDeactivated) {
            wasDeactivated = false
            viewModel.refreshActiveStreams()
            viewModel.loadVisibleCameraSnapshots()
        }
    }

    LaunchedEffect(fullscreenIndex, isGridFullscreen) {
        onFullscreenChange(fullscreenIndex != null || isGridFullscreen)
    }

    LaunchedEffect(uiState.gridSize) {
        if (uiState.gridSize != 4 && isGridFullscreen) {
            isGridFullscreen = false
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            com.tenli.oneview.ui.component.MobileTopPopupManager.error(it)
            viewModel.clearError()
        }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val currentIsTabActive by androidx.compose.runtime.rememberUpdatedState(isTabActive)
    DisposableEffect(lifecycleOwner) {
        var isFirstResume = true
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.setMonitorTabActive(currentIsTabActive)
                if (isFirstResume) {
                    isFirstResume = false
                } else if (currentIsTabActive) {
                    viewModel.refreshActiveStreams()
                    viewModel.loadVisibleCameraSnapshots()
                }
            } else if (event == androidx.lifecycle.Lifecycle.Event.ON_STOP) {
                viewModel.setMonitorTabActive(false)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Derived data matching MobileLivePage.vue
    val hasAvailableCameras = remember(uiState.treeData) {
        uiState.treeData.any { MonitorViewModel.countNodeCameras(it) > 0 }
    }

    val vmsFilterChips = remember(
        uiState.treeData,
        uiState.activeVmsKey,
        uiState.selectedFilterNodeKey
    ) {
        MonitorViewModel.computeVmsFilterChips(uiState)
    }

    val groupTreeRows = remember(
        uiState.treeData,
        uiState.sheetVmsKey,
        uiState.activeVmsKey,
        uiState.expandedNodes
    ) {
        MonitorViewModel.computeGroupTreeRows(uiState)
    }

    val sheetVmsTitle = remember(uiState.treeData, uiState.sheetVmsKey, uiState.activeVmsKey) {
        val targetKey = uiState.sheetVmsKey.ifEmpty { uiState.activeVmsKey }
        val vmsNode = uiState.treeData.filterIsInstance<CameraTreeNode.VMSNode>()
            .firstOrNull { it.key == targetKey }
            ?: uiState.treeData.filterIsInstance<CameraTreeNode.VMSNode>().firstOrNull()
        vmsNode?.vms?.name?.ifBlank { "Chọn nhóm camera" } ?: "Chọn nhóm camera"
    }

    val displayedCameraItems = remember(
        uiState.treeData,
        uiState.selectedFilterNodeKey,
        uiState.activeVmsKey,
        uiState.searchQuery
    ) {
        MonitorViewModel.computeDisplayedCameraItems(uiState)
    }

    val totalCameraPages = remember(displayedCameraItems.size) {
        maxOf(1, (displayedCameraItems.size + MonitorViewModel.CAMERA_PAGE_SIZE - 1) / MonitorViewModel.CAMERA_PAGE_SIZE)
    }

    val pagedCameraItems = remember(displayedCameraItems, uiState.currentCameraPage) {
        val start = (uiState.currentCameraPage - 1).coerceAtLeast(0) * MonitorViewModel.CAMERA_PAGE_SIZE
        displayedCameraItems.drop(start).take(MonitorViewModel.CAMERA_PAGE_SIZE)
    }

    val cameraPageNumbers = remember(uiState.currentCameraPage, totalCameraPages) {
        val total = totalCameraPages
        val current = uiState.currentCameraPage
        when {
            total <= 3 -> (1..total).toList()
            current <= 2 -> listOf(1, 2, 3)
            current >= total - 1 -> listOf(total - 2, total - 1, total)
            else -> listOf(current - 1, current, current + 1)
        }
    }

    val activeCameraIds = remember(uiState.selectedCameras, uiState.gridSize) {
        (0 until uiState.gridSize).mapNotNull { idx ->
            uiState.selectedCameras.getOrNull(idx)?.camera?.id
        }.toSet()
    }

    // Scroll camera grid to top on page / filter / search change
    LaunchedEffect(
        uiState.currentCameraPage,
        uiState.selectedFilterNodeKey,
        uiState.activeVmsKey,
        uiState.searchQuery
    ) {
        cameraGridState.scrollToItem(0)
        viewModel.loadVisibleCameraSnapshots()
    }

    val panelBg = if (isDark) Color(0xFF172033) else Color.White
    val gridDividerBg = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)

    val isSingleFullscreen = fullscreenIndex == 0 && uiState.gridSize == 1
    val is2x2Fullscreen = isGridFullscreen && uiState.gridSize == 4
    val isAnyFullscreen = isSingleFullscreen || is2x2Fullscreen

    if (isAnyFullscreen) {
        LandscapeFullscreenHandler(
            activity = activity,
            onExit = {
                restoreSystemOrientationAndBars(activity)
                fullscreenIndex = null
                isGridFullscreen = false
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isAnyFullscreen) Color.Black else MaterialTheme.colorScheme.background)
        ) {
            if (!isAnyFullscreen) {
                MobileTopBar(
                    title = stringResource(id = R.string.tab_monitor)
                )
            }

            if (uiState.isLoading && !hasAvailableCameras) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    WaveDotsLoading()
                }
            } else if (!hasAvailableCameras) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CommonEmptyState(text = "Không có camera nào")
                }
            } else {
                // 1. Live Video Stage Card (.mobile-live-stage-card)
                // Giữ nguyên cùng 1 node trong cây Compose khi bật/tắt fullscreen để ExoPlayer & WebSocket không bị huỷ hoặc kết nối lại
                Box(
                    modifier = if (isAnyFullscreen) {
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                    } else {
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .background(if (uiState.gridSize == 4) gridDividerBg else panelBg)
                    }
                ) {
                    if (uiState.gridSize == 1) {
                        val selectedCam = uiState.selectedCameras.getOrNull(0)
                        val isBlinking = uiState.blinkStates.getOrNull(0) == true
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(if (isSingleFullscreen) Color.Black else panelBg)
                                .then(
                                    if (isBlinking && !isSingleFullscreen) {
                                        Modifier.border(2.dp, BrandAccent)
                                    } else {
                                        Modifier
                                    }
                                )
                        ) {
                            if (selectedCam != null) {
                                CameraGridItem(
                                    selectedCamera = selectedCam,
                                    isTabActive = isTabActive,
                                    isDark = isDark,
                                    onClick = {},
                                    onRetryClick = { viewModel.retryCameraStream(0) },
                                    onSuccess = { viewModel.resetRetryCount(0) },
                                    onSnapshotError = { viewModel.onSnapshotLoadError(0) },
                                    onFullscreenClick = {
                                        if (isSingleFullscreen) {
                                            restoreSystemOrientationAndBars(activity)
                                            fullscreenIndex = null
                                        } else {
                                            fullscreenIndex = 0
                                        }
                                    },
                                    onBackToLiveClick = { viewModel.addCamera(selectedCam.camera, 0) },
                                    onRemoveClick = {
                                        if (isSingleFullscreen) {
                                            restoreSystemOrientationAndBars(activity)
                                            fullscreenIndex = null
                                        }
                                        viewModel.removeCamera(0)
                                    },
                                    isFullscreen = isSingleFullscreen,
                                    isCompact = false,
                                    showFullscreenButton = true
                                )
                            } else {
                                if (isSingleFullscreen) {
                                    LaunchedEffect(Unit) {
                                        restoreSystemOrientationAndBars(activity)
                                        fullscreenIndex = null
                                    }
                                }
                                EmptyGridItem(
                                    onClick = {},
                                    isCompact = false,
                                    isActiveSlot = false,
                                    isDark = isDark
                                )
                            }
                        }
                    } else {
                        // 2x2 Grid (.mobile-grid-2x2 with 2.dp gap) - giữ nguyên instance khi phóng to/thu nhỏ
                        LiveGrid2x2Stage(
                            uiState = uiState,
                            isTabActive = isTabActive,
                            isDark = isDark,
                            panelBg = panelBg,
                            gridDividerBg = gridDividerBg,
                            onCellTap = { viewModel.handleCellTap(it) },
                            onRetryClick = { viewModel.retryCameraStream(it) },
                            onSuccess = { viewModel.resetRetryCount(it) },
                            onSnapshotError = { viewModel.onSnapshotLoadError(it) },
                            onReloadClick = { cam, idx -> viewModel.addCamera(cam, idx) },
                            onRemoveClick = { viewModel.removeCamera(it) }
                        )

                        // Floating exit button (.grid-fullscreen-exit-btn) khi ở chế độ phóng to lưới 2x2
                        if (is2x2Fullscreen) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(12.dp)
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(Color(0xFF0F172A).copy(alpha = 0.65f))
                                    .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(9.dp))
                                    .clickable {
                                        restoreSystemOrientationAndBars(activity)
                                        isGridFullscreen = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = LucideIcons.Minimize,
                                    contentDescription = "Thu nhỏ lưới 2x2",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                if (!isAnyFullscreen) {
                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. Camera Panel (.mobile-camera-panel)
                    val controlBg = if (isDark) Color.White.copy(alpha = 0.07f) else Color(0xFFF1F5F9)
                    val mutedText = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
                    val placeholderColor = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .navigationBarsPadding()
                            .padding(horizontal = 10.dp)
                            .padding(bottom = 84.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(panelBg)
                    ) {
                        // .camera-filter-header
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Row 1: .tree-search-bar (Search + 1x1/2x2 selector + 2x2 Fullscreen button)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // .search-input-box
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(controlBg)
                                        .padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = LucideIcons.Search,
                                        contentDescription = "Tìm camera",
                                        tint = if (isDark) BrandAccent else Color(0xFF64748B),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier.weight(1f),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (uiState.searchQuery.isEmpty()) {
                                            Text(
                                                text = "Tìm camera...",
                                                color = placeholderColor,
                                                fontSize = 13.sp
                                            )
                                        }
                                        BasicTextField(
                                            value = uiState.searchQuery,
                                            onValueChange = { viewModel.setSearchQuery(it) },
                                            singleLine = true,
                                            textStyle = TextStyle(
                                                color = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A),
                                                fontSize = 13.sp
                                            ),
                                            cursorBrush = SolidColor(BrandAccent),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                    if (uiState.searchQuery.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF94A3B8).copy(alpha = 0.2f))
                                                .clickable { viewModel.setSearchQuery("") },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = LucideIcons.X,
                                                contentDescription = "Xóa tìm kiếm",
                                                tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                                modifier = Modifier.size(11.dp)
                                            )
                                        }
                                    }
                                }

                                // .mobile-grid-selector ("1x1" / "2x2")
                                Row(
                                    modifier = Modifier
                                        .height(36.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(controlBg)
                                        .padding(3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    GridOptionButton(
                                        label = "1x1",
                                        isSelected = uiState.gridSize == 1,
                                        isDark = isDark,
                                        onClick = { viewModel.setGridSize(1) }
                                    )
                                    GridOptionButton(
                                        label = "2x2",
                                        isSelected = uiState.gridSize == 4,
                                        isDark = isDark,
                                        onClick = { viewModel.setGridSize(4) }
                                    )
                                }

                                // .grid-fullscreen-btn (visible when gridSize == 4)
                                if (uiState.gridSize == 4) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(9.dp))
                                            .background(controlBg)
                                            .clickable { isGridFullscreen = true },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = LucideIcons.Maximize,
                                            contentDescription = "Phóng to lưới 2x2",
                                            tint = mutedText,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            // Row 2: .vms-pagination-row (VMS chips + Pagination controls)
                            if (vmsFilterChips.isNotEmpty() || totalCameraPages > 1) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // .vms-chips-scroll
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .horizontalScroll(rememberScrollState()),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        vmsFilterChips.forEach { chip ->
                                            VmsFilterChipButton(
                                                chip = chip,
                                                isDark = isDark,
                                                panelBg = panelBg,
                                                controlBg = controlBg,
                                                mutedText = mutedText,
                                                onClick = { viewModel.handleVmsChipClick(chip.key) }
                                            )
                                        }
                                    }

                                    // .pagination-controls
                                    if (totalCameraPages > 1) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            val canPrev = uiState.currentCameraPage > 1
                                            val canNext = uiState.currentCameraPage < totalCameraPages

                                            Box(
                                                modifier = Modifier
                                                    .height(28.dp)
                                                    .widthIn(min = 24.dp)
                                                    .clip(RoundedCornerShape(7.dp))
                                                    .background(controlBg)
                                                    .alpha(if (canPrev) 1f else 0.4f)
                                                    .clickable(enabled = canPrev) { viewModel.prevCameraPage() }
                                                    .padding(horizontal = 5.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = LucideIcons.ChevronLeft,
                                                    contentDescription = "Trang trước",
                                                    tint = mutedText,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }

                                            cameraPageNumbers.forEach { page ->
                                                val isPageActive = page == uiState.currentCameraPage
                                                Box(
                                                    modifier = Modifier
                                                        .height(28.dp)
                                                        .widthIn(min = 24.dp)
                                                        .clip(RoundedCornerShape(7.dp))
                                                        .background(if (isPageActive) BrandAccent else controlBg)
                                                        .clickable { viewModel.setCameraPage(page) }
                                                        .padding(horizontal = 6.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = page.toString(),
                                                        color = if (isPageActive) Color.White else mutedText,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .height(28.dp)
                                                    .widthIn(min = 24.dp)
                                                    .clip(RoundedCornerShape(7.dp))
                                                    .background(controlBg)
                                                    .alpha(if (canNext) 1f else 0.4f)
                                                    .clickable(enabled = canNext) { viewModel.nextCameraPage() }
                                                    .padding(horizontal = 5.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = LucideIcons.ChevronRight,
                                                    contentDescription = "Trang sau",
                                                    tint = mutedText,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // .mobile-camera-list-scroll -> 2-column camera cards grid
                        val gridFullMsg = stringResource(id = R.string.camera_grid_full)
                        if (pagedCameraItems.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CommonEmptyState(text = "Không tìm thấy camera phù hợp")
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                state = cameraGridState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = 8.dp,
                                    end = 8.dp,
                                    top = 6.dp,
                                    bottom = 12.dp
                                ),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(
                                    items = pagedCameraItems,
                                    key = { it.key }
                                ) { item ->
                                    val isPlaying = activeCameraIds.contains(item.camera.id)
                                    val hasSnapKey = uiState.cameraSnapshots.containsKey(item.camera.id)
                                    val snapUrl = uiState.cameraSnapshots[item.camera.id]

                                    MobileCameraSnapshotCard(
                                        camera = item.camera,
                                        isPlaying = isPlaying,
                                        hasSnapKey = hasSnapKey,
                                        snapshotUrl = snapUrl,
                                        isDark = isDark,
                                        onSnapshotError = { viewModel.onCardSnapshotError(item.camera) },
                                        onClick = {
                                            viewModel.selectCameraFromTree(
                                                camera = item.camera,
                                                onGridFull = {
                                                    com.tenli.oneview.ui.component.MobileTopPopupManager.warning(gridFullMsg)
                                                }
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Group Filter Bottom Sheet (.mobile-group-sheet-overlay)
        if (uiState.showGroupFilterSheet && !isAnyFullscreen) {
            MobileGroupFilterBottomSheet(
                title = sheetVmsTitle,
                rows = groupTreeRows,
                selectedFilterNodeKey = uiState.selectedFilterNodeKey,
                isDark = isDark,
                onDismiss = { viewModel.closeGroupFilterSheet() },
                onToggleExpand = { viewModel.toggleNodeExpansion(it) },
                onRowClick = { viewModel.handleGroupRowClick(it) }
            )
        }
    }
}

@Composable
private fun LandscapeFullscreenHandler(
    activity: Activity?,
    onExit: () -> Unit
) {
    DisposableEffect(Unit) {
        val originalOrientation = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        val window = activity?.window
        val insetsController = window?.let {
            androidx.core.view.WindowCompat.getInsetsController(it, it.decorView)
        }

        if (insetsController != null) {
            insetsController.systemBarsBehavior =
                androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
        }

        onDispose {
            activity?.requestedOrientation =
                originalOrientation ?: ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            insetsController?.show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
        }
    }

    androidx.activity.compose.BackHandler {
        onExit()
    }
}

private fun restoreSystemOrientationAndBars(activity: Activity?) {
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    val window = activity?.window
    val insetsController = window?.let {
        androidx.core.view.WindowCompat.getInsetsController(it, it.decorView)
    }
    insetsController?.show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
}

@Composable
private fun LiveGrid2x2Stage(
    uiState: MonitorUiState,
    isTabActive: Boolean,
    isDark: Boolean,
    panelBg: Color,
    gridDividerBg: Color,
    onCellTap: (Int) -> Unit,
    onRetryClick: (Int) -> Unit,
    onSuccess: (Int) -> Unit,
    onSnapshotError: (Int) -> Unit,
    onReloadClick: (CameraModel, Int) -> Unit,
    onRemoveClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gridDividerBg),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        for (row in 0..1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                for (col in 0..1) {
                    val slotIdx = row * 2 + col
                    val selectedCam = uiState.selectedCameras.getOrNull(slotIdx)
                    val isBlinking = uiState.blinkStates.getOrNull(slotIdx) == true
                    val isActiveSlot = uiState.activeSlotIndex == slotIdx

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(panelBg)
                            .then(
                                if (isBlinking) {
                                    Modifier.border(2.dp, BrandAccent)
                                } else {
                                    Modifier
                                }
                            )
                    ) {
                        if (selectedCam != null) {
                            CameraGridItem(
                                selectedCamera = selectedCam,
                                isTabActive = isTabActive,
                                isDark = isDark,
                                onClick = { onCellTap(slotIdx) },
                                onRetryClick = { onRetryClick(slotIdx) },
                                onSuccess = { onSuccess(slotIdx) },
                                onSnapshotError = { onSnapshotError(slotIdx) },
                                onFullscreenClick = {},
                                onBackToLiveClick = { onReloadClick(selectedCam.camera, slotIdx) },
                                onRemoveClick = { onRemoveClick(slotIdx) },
                                isFullscreen = false,
                                isCompact = true,
                                showFullscreenButton = false
                            )
                        } else {
                            EmptyGridItem(
                                onClick = { onCellTap(slotIdx) },
                                isCompact = true,
                                isActiveSlot = isActiveSlot,
                                isDark = isDark
                            )
                        }
                    }
                }
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
    val contentColor = when {
        isSelected -> Color.White
        isDark -> Color(0xFF94A3B8)
        else -> Color(0xFF475569)
    }

    Box(
        modifier = Modifier
            .height(30.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = contentColor,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun VmsFilterChipButton(
    chip: VmsFilterChip,
    isDark: Boolean,
    panelBg: Color,
    controlBg: Color,
    mutedText: Color,
    onClick: () -> Unit
) {
    val chipBg = if (chip.isActive) BrandAccent else controlBg
    val textColor = if (chip.isActive) Color.White else mutedText
    val countBg = when {
        chip.isActive -> Color.White.copy(alpha = 0.22f)
        isDark -> Color.White.copy(alpha = 0.08f)
        else -> Color(0xFF0F172A).copy(alpha = 0.07f)
    }
    val displayCount = if (chip.isGroupFiltered) chip.activeCount else chip.totalCount

    Box {
        Row(
            modifier = Modifier
                .height(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(chipBg)
                .clickable(onClick = onClick)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = chip.name,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .background(countBg)
                    .padding(horizontal = 4.dp, vertical = 1.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayCount.toString(),
                    color = textColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (chip.isGroupFiltered) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444))
                    .border(1.5.dp, panelBg, CircleShape)
            )
        }
    }
}

@Composable
private fun MobileCameraSnapshotCard(
    camera: CameraModel,
    isPlaying: Boolean,
    hasSnapKey: Boolean,
    snapshotUrl: String?,
    isDark: Boolean,
    onSnapshotError: () -> Unit,
    onClick: () -> Unit
) {
    val cardBg = when {
        isPlaying && isDark -> BrandAccent.copy(alpha = 0.14f)
        isPlaying && !isDark -> BrandAccent.copy(alpha = 0.10f)
        isDark -> Color.White.copy(alpha = 0.04f)
        else -> Color(0xFFF1F5F9)
    }
    val thumbBg = if (isDark) Color(0xFF0F172A).copy(alpha = 0.45f) else Color(0xFFE2E8F0)
    val noImageTint = (if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)).copy(alpha = 0.45f)
    val nameColor = when {
        isPlaying -> BrandAccent
        isDark -> Color(0xFFF1F5F9)
        else -> Color(0xFF1E293B)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(cardBg)
            .clickable(onClick = onClick)
    ) {
        // .cam-card-thumb (16:9)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(thumbBg),
            contentAlignment = Alignment.Center
        ) {
            if (!snapshotUrl.isNullOrEmpty()) {
                SubcomposeAsyncImage(
                    model = snapshotUrl,
                    contentDescription = camera.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = BrandAccent.copy(alpha = 0.85f),
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    error = {
                        LaunchedEffect(snapshotUrl) {
                            onSnapshotError()
                        }
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = LucideIcons.ImageOff,
                                contentDescription = null,
                                tint = noImageTint,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                )
            } else if (!hasSnapKey) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = BrandAccent.copy(alpha = 0.85f),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = LucideIcons.ImageOff,
                    contentDescription = null,
                    tint = noImageTint,
                    modifier = Modifier.size(26.dp)
                )
            }

            // .cam-card-overlay-badge -> .cam-playing-badge
            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(BrandAccent)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        PlayingEqualizerBars()
                        Text(
                            text = "Đang xem",
                            color = Color.White,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 13.sp
                        )
                    }
                }
            }
        }

        // .cam-card-footer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 30.dp)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = camera.name,
                color = nameColor,
                fontSize = 12.sp,
                fontWeight = if (isPlaying) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PlayingEqualizerBars() {
    val infiniteTransition = rememberInfiniteTransition(label = "playingEqualizer")
    val scale1 by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )
    val scale2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 450, delayMillis = 120, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )
    val scale3 by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 450, delayMillis = 240, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )

    Row(
        modifier = Modifier.height(10.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(1.5.dp)
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(4.dp)
                .graphicsLayer {
                    scaleY = scale1
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f)
                }
                .background(Color.White, RoundedCornerShape(1.dp))
        )
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(9.dp)
                .graphicsLayer {
                    scaleY = scale2
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f)
                }
                .background(Color.White, RoundedCornerShape(1.dp))
        )
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(6.dp)
                .graphicsLayer {
                    scaleY = scale3
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f)
                }
                .background(Color.White, RoundedCornerShape(1.dp))
        )
    }
}

@Composable
private fun MobileGroupFilterBottomSheet(
    title: String,
    rows: List<GroupSheetRow>,
    selectedFilterNodeKey: String,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onToggleExpand: (String) -> Unit,
    onRowClick: (String) -> Unit
) {
    val sheetBg = if (isDark) Color(0xFF172033) else Color.White
    val sheetBorder = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
    val titleColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val closeBtnBg = if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFF1F5F9)
    val mutedColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val rowTextColor = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A)

    com.tenli.oneview.ui.component.MobileFullScreenOverlay(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .navigationBarsPadding()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(sheetBg)
                    .border(1.dp, sheetBorder, RoundedCornerShape(22.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 12.dp)
            ) {
                // Sheet Header (drag handle + title + close button)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color(0xFF94A3B8).copy(alpha = 0.38f))
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = title,
                            color = titleColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(closeBtnBg)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = LucideIcons.X,
                            contentDescription = "Đóng",
                            tint = mutedColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Sheet Body
                if (rows.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CommonEmptyState(text = "Không có nhóm camera con")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(items = rows, key = { it.key }) { row ->
                            val isSelected = selectedFilterNodeKey == row.key
                            val indentDp = (minOf(row.depth, 5) * 16 + 10).dp
                            val iconTint = if (isSelected) BrandAccent else mutedColor

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) BrandAccent.copy(alpha = 0.12f) else Color.Transparent
                                    )
                                    .clickable { onRowClick(row.key) }
                                    .padding(
                                        start = indentDp,
                                        end = 10.dp,
                                        top = 6.dp,
                                        bottom = 6.dp
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (row.hasSubGroups) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { onToggleExpand(row.key) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = LucideIcons.ChevronDown,
                                            contentDescription = "Mở rộng nhóm",
                                            tint = iconTint,
                                            modifier = Modifier
                                                .size(15.dp)
                                                .rotate(if (row.isExpanded) 0f else -90f)
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.size(22.dp))
                                }

                                Icon(
                                    imageVector = if (row.isExpanded && row.hasSubGroups) {
                                        LucideIcons.FolderOpen
                                    } else {
                                        LucideIcons.Folder
                                    },
                                    contentDescription = null,
                                    tint = iconTint,
                                    modifier = Modifier.size(16.dp)
                                )

                                Text(
                                    text = row.title,
                                    color = if (isSelected) BrandAccent else rowTextColor,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )

                                if (isSelected) {
                                    Icon(
                                        imageVector = LucideIcons.Check,
                                        contentDescription = null,
                                        tint = BrandAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
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
    isCompact: Boolean = false,
    showFullscreenButton: Boolean = true
) {
    val deviceKey = UserSession.accessToken
    var isLiveViewPlaying by remember(selectedCamera.camera.id, selectedCamera.streamUrl, isTabActive) { mutableStateOf(false) }
    var isLiveViewError by remember(selectedCamera.camera.id, selectedCamera.streamUrl, isTabActive) { mutableStateOf(false) }
    var snapshotFailed by remember(selectedCamera.camera.id, selectedCamera.fallbackImageUrl) { mutableStateOf(false) }
    val hasError = isLiveViewError || selectedCamera.streamUrl.startsWith("error://")

    // Đồng bộ showInfoTemporarily (3000ms) trong LiveVideoView.vue
    var showInfo by remember(selectedCamera.camera.id) { mutableStateOf(false) }
    var infoTrigger by remember { mutableIntStateOf(0) }
    LaunchedEffect(infoTrigger) {
        if (infoTrigger > 0) {
            showInfo = true
            delay(3000L)
            showInfo = false
        }
    }

    val surfaceBg = when {
        isFullscreen -> Color.Black
        isDark -> Color(0xFF172033)
        else -> Color.White
    }

    val canPlayStream = isTabActive &&
        selectedCamera.streamUrl.isNotEmpty() &&
        !selectedCamera.streamUrl.startsWith("error://")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceBg)
            .pointerInput(isCompact, showFullscreenButton) {
                detectTapGestures(
                    onTap = {
                        onClick()
                        infoTrigger++
                    },
                    onDoubleTap = {
                        if (!isCompact && showFullscreenButton) {
                            onFullscreenClick()
                        } else {
                            onClick()
                            infoTrigger++
                        }
                    }
                )
            }
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

        // Cached Snapshot Preview + Loading Overlay (.cached-snapshot-preview & fade-snapshot 0.35s)
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
                    .background(if (isFullscreen) Color.Black else if (isDark) Color(0xFF172033) else Color.White),
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
                }
                WaveDotsLoading(dotColor = BrandAccent)
            }
        }

        // Error Overlay (chạm để tải lại luồng live giống reloadStream trong LiveVideoView.vue)
        if (hasError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isFullscreen) Color.Black else if (isDark) Color(0xFF172033) else Color.White)
                    .clickable {
                        onClick()
                        infoTrigger++
                        onBackToLiveClick()
                    },
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

        // Top-Left Camera Name Pill (.info-name) & Top-Right Close Button (.info-action) - shown on tap for 3s
        AnimatedVisibility(
            visible = showInfo,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(200)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // .info-name
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(if (isCompact) 4.dp else 8.dp)
                        .widthIn(max = if (isCompact) 140.dp else 260.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.42f))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                        .padding(
                            horizontal = if (isCompact) 6.dp else 8.dp,
                            vertical = if (isCompact) 2.dp else 3.dp
                        )
                ) {
                    Text(
                        text = selectedCamera.camera.name,
                        color = Color.White,
                        fontSize = if (isCompact) 11.sp else 12.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // .info-action (Close button x-circle)
                if (!isFullscreen) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(if (isCompact) 4.dp else 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.42f))
                            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                            .padding(if (isCompact) 2.dp else 3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(if (isCompact) 22.dp else 26.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onRemoveClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = LucideIcons.XCircle,
                                contentDescription = "Đóng luồng trực tiếp",
                                tint = Color.White,
                                modifier = Modifier.size(if (isCompact) 14.dp else 16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Bottom-Right Fullscreen Button (.fullscreen-btn, shown in 1x1 mode when stream is active or fullscreen)
        if (showFullscreenButton && (isLiveViewPlaying || isFullscreen)) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(if (isCompact) 4.dp else 6.dp)
                    .size(if (isFullscreen) 32.dp else 28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.42f))
                    .clickable { onFullscreenClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isFullscreen) LucideIcons.Minimize else LucideIcons.Maximize,
                    contentDescription = "Toàn màn hình",
                    tint = Color.White,
                    modifier = Modifier.size(if (isFullscreen) 17.dp else 15.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyGridItem(
    onClick: () -> Unit,
    isCompact: Boolean = false,
    isActiveSlot: Boolean = false,
    isDark: Boolean = true
) {
    val bgColor = if (isDark) Color(0xFF172033) else Color.White
    val iconColor = when {
        isActiveSlot -> BrandAccent
        isDark -> Color(0xFF64748B)
        else -> Color(0xFF94A3B8)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = LucideIcons.Video,
            contentDescription = "Chọn Camera",
            tint = iconColor,
            modifier = Modifier.size(if (isCompact) 34.dp else 46.dp)
        )
    }
}

@Composable
fun PlaybackItemView(
    playback: com.tenli.oneview.model.network.VideoModel,
    cameraName: String,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
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
                } catch (_: Exception) {
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
                    contentScale = ContentScale.Crop,
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
