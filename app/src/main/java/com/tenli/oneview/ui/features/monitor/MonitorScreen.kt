package com.tenli.oneview.ui.features.monitor

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import com.tenli.oneview.R
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.model.network.CameraModel
import com.tenli.oneview.ui.component.CommonEmptyState
import com.tenli.oneview.ui.component.IconBadge
import com.tenli.oneview.ui.component.LiveStreamPlayer
import com.tenli.oneview.ui.component.MobileFullScreenOverlay
import com.tenli.oneview.ui.component.MobileTopActionButton
import com.tenli.oneview.ui.component.MobileTopBar
import com.tenli.oneview.ui.component.MobileTopPopupManager
import com.tenli.oneview.ui.component.VideoPlayer
import com.tenli.oneview.ui.component.WaveDotsLoading
import com.tenli.oneview.ui.component.rememberIsEnglish
import com.tenli.oneview.ui.theme.LucideIcons
import com.tenli.oneview.ui.theme.appColors
import com.tenli.oneview.ui.theme.caption
import com.tenli.oneview.ui.theme.micro
import com.tenli.oneview.ui.theme.spacing
import kotlinx.coroutines.delay

private const val LIVE_STAGE_ASPECT_RATIO = 16f / 9f
private const val SNAPSHOT_CARD_WIDTH_PX = 420
private const val SNAPSHOT_CARD_HEIGHT_PX = 236
private val GroupFilterSheetMaxHeight = 520.dp
private val GroupFilterEmptyHeight = 160.dp
private val GroupRowBaseHeight = 38.dp
private val CameraInfoPillMaxWidthCompact = 140.dp
private val CameraInfoPillMaxWidthRegular = 260.dp

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
    val isEn = rememberIsEnglish()
    val activity = LocalContext.current as? Activity
    val cameraGridState = rememberLazyGridState()
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing

    // fullscreenIndex != null: 1x1 single-camera fullscreen
    var fullscreenIndex by remember { mutableStateOf<Int?>(null) }
    // isGridFullscreen: 2x2 grid fullscreen matching MobileLivePage.vue
    var isGridFullscreen by remember { mutableStateOf(false) }
    // Thu gọn mặc định các ô lọc, chọn lưới 1x1/2x2 để giao diện gọn gàng
    var isControlsExpanded by remember { mutableStateOf(false) }

    var wasDeactivated by remember { mutableStateOf(true) }
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

    LaunchedEffect(uiState.error, isTabActive) {
        uiState.error?.let {
            if (isTabActive) {
                MobileTopPopupManager.error(it)
            }
            viewModel.clearError()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val currentIsTabActive by rememberUpdatedState(isTabActive)
    DisposableEffect(lifecycleOwner) {
        var isFirstResume = true
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.setMonitorTabActive(currentIsTabActive)
                if (isFirstResume) {
                    isFirstResume = false
                } else if (currentIsTabActive) {
                    viewModel.refreshActiveStreams()
                    viewModel.loadVisibleCameraSnapshots()
                }
            } else if (event == Lifecycle.Event.ON_STOP) {
                viewModel.setMonitorTabActive(false)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Derived data precomputed on background/ViewModel layer
    val hasAvailableCameras = uiState.hasAvailableCameras
    val totalAllCamerasCount = uiState.totalAllCamerasCount
    val groupTreeRows = uiState.groupTreeRows
    val pagedCameraItems = uiState.pagedCameraItems
    val totalCameraPages = uiState.totalCameraPages

    val sheetTitle = if (isEn) "Filter camera groups" else "Lọc nhóm camera"

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
        uiState.searchQuery
    ) {
        cameraGridState.scrollToItem(0)
        viewModel.loadVisibleCameraSnapshots()
    }

    val panelBg = colors.surface
    val gridDividerBg = colors.borderStrong

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

    val bottomClearance = spacing.bottomNavHeight + spacing.space20

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isAnyFullscreen) colors.mediaBg else MaterialTheme.colorScheme.background)
        ) {
            if (!isAnyFullscreen) {
                MobileTopBar(
                    title = stringResource(id = R.string.tab_monitor),
                    rightAction = {
                        MobileTopActionButton(
                            icon = LucideIcons.ListFilter,
                            contentDescription = if (isEn) "Filter" else "Bộ lọc",
                            hasActiveDot = uiState.selectedFilterNodeKey != MonitorViewModel.FILTER_ALL,
                            onClick = { viewModel.openGroupFilterSheet() }
                        )
                    }
                )
            }

            if (uiState.isLoading && !hasAvailableCameras) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = bottomClearance),
                    contentAlignment = Alignment.Center
                ) {
                    WaveDotsLoading()
                }
            } else if (!hasAvailableCameras) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = bottomClearance),
                    contentAlignment = Alignment.Center
                ) {
                    CommonEmptyState(text = if (isEn) "No cameras available" else "Không có camera nào")
                }
            } else {
                // 1. Live Video Stage Card (.mobile-live-stage-card)
                // Giữ nguyên cùng 1 node trong cây Compose khi bật/tắt fullscreen để ExoPlayer & WebSocket không bị huỷ hoặc kết nối lại
                Box(
                    modifier = if (isAnyFullscreen) {
                        Modifier
                            .fillMaxSize()
                            .background(colors.mediaBg)
                    } else {
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(LIVE_STAGE_ASPECT_RATIO)
                            .background(if (uiState.gridSize == 4) gridDividerBg else panelBg)
                    }
                ) {
                    if (uiState.gridSize == 1) {
                        val selectedCam = uiState.selectedCameras.getOrNull(0)
                        val isBlinking = uiState.blinkStates.getOrNull(0) == true
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(if (isSingleFullscreen) colors.mediaBg else panelBg)
                                .then(
                                    if (isBlinking && !isSingleFullscreen) {
                                        Modifier.border(spacing.borderFocus, colors.accent)
                                    } else {
                                        Modifier
                                    }
                                )
                        ) {
                            if (selectedCam != null) {
                                CameraGridItem(
                                    selectedCamera = selectedCam,
                                    isTabActive = isTabActive,
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
                                    isActiveSlot = false
                                )
                            }
                        }
                    } else {
                        // 2x2 Grid (.mobile-grid-2x2 with 2.dp gap) - giữ nguyên instance khi phóng to/thu nhỏ
                        LiveGrid2x2Stage(
                            uiState = uiState,
                            isTabActive = isTabActive,
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
                                    .padding(spacing.space12)
                                    .size(spacing.iconButton)
                                    .clip(RoundedCornerShape(spacing.radiusSmallMedium))
                                    .background(colors.mediaOverlay)
                                    .border(
                                        spacing.borderMedium,
                                        colors.onMedia.copy(alpha = 0.18f),
                                        RoundedCornerShape(spacing.radiusSmallMedium)
                                    )
                                    .clickable {
                                        restoreSystemOrientationAndBars(activity)
                                        isGridFullscreen = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = LucideIcons.Minimize,
                                    contentDescription = "Thu nhỏ lưới 2x2",
                                    tint = colors.onMedia,
                                    modifier = Modifier.size(spacing.iconSmall)
                                )
                            }
                        }
                    }
                }

                if (!isAnyFullscreen) {
                    Spacer(modifier = Modifier.height(spacing.space10))

                    // 2. Camera Panel (.mobile-camera-panel)
                    val controlBg = colors.controlBg
                    val mutedText = colors.textSubtle
                    val placeholderColor = colors.textPlaceholder

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .navigationBarsPadding()
                            .padding(horizontal = spacing.space10)
                            .padding(bottom = bottomClearance)
                            .clip(RoundedCornerShape(spacing.radiusRegular))
                            .background(panelBg)
                    ) {
                        // .camera-filter-header
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = spacing.space10,
                                    end = spacing.space10,
                                    top = spacing.space8,
                                    bottom = spacing.space4
                                )
                        ) {
                            // Row 1 (Collapsible): .tree-search-bar (Search + 1x1/2x2 selector + 2x2 Fullscreen button)
                            AnimatedVisibility(
                                visible = isControlsExpanded,
                                enter = expandVertically(
                                    animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
                                ) + fadeIn(animationSpec = tween(durationMillis = 180)),
                                exit = shrinkVertically(
                                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
                                ) + fadeOut(animationSpec = tween(durationMillis = 150))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = spacing.space8),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(spacing.space6)
                                ) {
                                    // .search-input-box
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(spacing.buttonHeightSmall)
                                            .clip(RoundedCornerShape(spacing.radiusSmallMedium))
                                            .background(controlBg)
                                            .padding(horizontal = spacing.space10),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = LucideIcons.Search,
                                            contentDescription = if (isEn) "Search camera" else "Tìm camera",
                                            tint = if (colors.isDark) colors.accent else colors.textMuted,
                                            modifier = Modifier.size(spacing.iconSmall)
                                        )
                                        Spacer(modifier = Modifier.width(spacing.space6))
                                        Box(
                                            modifier = Modifier.weight(1f),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            if (uiState.searchQuery.isEmpty()) {
                                                Text(
                                                    text = if (isEn) "Search camera..." else "Tìm camera...",
                                                    color = placeholderColor,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                            BasicTextField(
                                                value = uiState.searchQuery,
                                                onValueChange = { viewModel.setSearchQuery(it) },
                                                singleLine = true,
                                                textStyle = MaterialTheme.typography.bodySmall.copy(
                                                    color = colors.textStrong
                                                ),
                                                cursorBrush = SolidColor(colors.accent),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                        if (uiState.searchQuery.isNotEmpty()) {
                                            Box(
                                                modifier = Modifier
                                                    .size(spacing.badgeMinSize)
                                                    .clip(CircleShape)
                                                    .background(colors.textMuted.copy(alpha = 0.2f))
                                                    .clickable { viewModel.setSearchQuery("") },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = LucideIcons.X,
                                                    contentDescription = if (isEn) "Clear search" else "Xóa tìm kiếm",
                                                    tint = colors.textMuted,
                                                    modifier = Modifier.size(spacing.iconTiny)
                                                )
                                            }
                                        }
                                    }

                                    // .mobile-grid-selector ("1x1" / "2x2")
                                    Row(
                                        modifier = Modifier
                                            .height(spacing.buttonHeightSmall)
                                            .clip(RoundedCornerShape(spacing.radiusSmallMedium))
                                            .background(controlBg)
                                            .padding(spacing.space3),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(spacing.space2)
                                    ) {
                                        GridOptionButton(
                                            label = "1x1",
                                            isSelected = uiState.gridSize == 1,
                                            onClick = { viewModel.setGridSize(1) }
                                        )
                                        GridOptionButton(
                                            label = "2x2",
                                            isSelected = uiState.gridSize == 4,
                                            onClick = { viewModel.setGridSize(4) }
                                        )
                                    }

                                    // .grid-fullscreen-btn (visible when gridSize == 4)
                                    if (uiState.gridSize == 4) {
                                        Box(
                                            modifier = Modifier
                                                .size(spacing.iconButton)
                                                .clip(RoundedCornerShape(spacing.radiusSmallMedium))
                                                .background(controlBg)
                                                .clickable { isGridFullscreen = true },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = LucideIcons.Maximize,
                                                contentDescription = "Phóng to lưới 2x2",
                                                tint = mutedText,
                                                modifier = Modifier.size(spacing.iconSmall)
                                            )
                                        }
                                    }
                                }
                            }

                            // Row 2: Toggle Arrow Button + Pagination controls
                            val arrowRotation by animateFloatAsState(
                                targetValue = if (isControlsExpanded) 180f else 0f,
                                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                                label = "controlsToggleArrow"
                            )
                            val hasActiveCustomControl = uiState.searchQuery.isNotEmpty()

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Nút mũi tên đóng/mở thanh công cụ tìm kiếm và chọn lưới 1x1/2x2
                                Box(
                                    modifier = Modifier
                                        .height(spacing.chipHeight)
                                        .widthIn(min = spacing.chipHeight)
                                        .clip(RoundedCornerShape(spacing.radiusSmall))
                                        .background(
                                            if (isControlsExpanded || hasActiveCustomControl) {
                                                colors.accent.copy(alpha = if (colors.isDark) 0.20f else 0.12f)
                                            } else {
                                                controlBg
                                            }
                                        )
                                        .clickable { isControlsExpanded = !isControlsExpanded }
                                        .padding(horizontal = spacing.space6),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = LucideIcons.ChevronDown,
                                        contentDescription = if (isEn) {
                                            if (isControlsExpanded) "Collapse controls" else "Expand controls"
                                        } else {
                                            if (isControlsExpanded) "Thu gọn công cụ" else "Mở rộng công cụ"
                                        },
                                        tint = if (isControlsExpanded || hasActiveCustomControl) colors.accent else mutedText,
                                        modifier = Modifier
                                            .size(spacing.iconSmall)
                                            .rotate(arrowRotation)
                                    )
                                }

                                // .pagination-controls (luôn hiển thị)
                                if (totalCameraPages > 1) {
                                    Spacer(modifier = Modifier.width(spacing.space6))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(spacing.space3)
                                    ) {
                                        val canPrev = uiState.currentCameraPage > 1
                                        val canNext = uiState.currentCameraPage < totalCameraPages

                                        Box(
                                            modifier = Modifier
                                                .height(spacing.chipHeight)
                                                .widthIn(min = spacing.space24)
                                                .clip(RoundedCornerShape(spacing.radiusSmall))
                                                .background(controlBg)
                                                .alpha(if (canPrev) 1f else 0.4f)
                                                .clickable(enabled = canPrev) { viewModel.prevCameraPage() }
                                                .padding(horizontal = spacing.space5),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = LucideIcons.ChevronLeft,
                                                contentDescription = "Trang trước",
                                                tint = mutedText,
                                                modifier = Modifier.size(spacing.iconXSmall)
                                            )
                                        }

                                        cameraPageNumbers.forEach { page ->
                                            val isPageActive = page == uiState.currentCameraPage
                                            Box(
                                                modifier = Modifier
                                                    .height(spacing.chipHeight)
                                                    .widthIn(min = spacing.space24)
                                                    .clip(RoundedCornerShape(spacing.radiusSmall))
                                                    .background(if (isPageActive) colors.accent else controlBg)
                                                    .clickable { viewModel.setCameraPage(page) }
                                                    .padding(horizontal = spacing.space6),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = page.toString(),
                                                    color = if (isPageActive) colors.onAccent else mutedText,
                                                    style = MaterialTheme.typography.caption,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .height(spacing.chipHeight)
                                                .widthIn(min = spacing.space24)
                                                .clip(RoundedCornerShape(spacing.radiusSmall))
                                                .background(controlBg)
                                                .alpha(if (canNext) 1f else 0.4f)
                                                .clickable(enabled = canNext) { viewModel.nextCameraPage() }
                                                .padding(horizontal = spacing.space5),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = LucideIcons.ChevronRight,
                                                contentDescription = "Trang sau",
                                                tint = mutedText,
                                                modifier = Modifier.size(spacing.iconXSmall)
                                            )
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
                                    .padding(spacing.space16),
                                contentAlignment = Alignment.Center
                            ) {
                                CommonEmptyState(
                                    text = if (isEn) "No matching cameras found" else "Không tìm thấy camera phù hợp"
                                )
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                state = cameraGridState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = spacing.space8,
                                    end = spacing.space8,
                                    top = spacing.space6,
                                    bottom = spacing.space12
                                ),
                                horizontalArrangement = Arrangement.spacedBy(spacing.space8),
                                verticalArrangement = Arrangement.spacedBy(spacing.space8)
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
                                        onSnapshotError = { viewModel.onCardSnapshotError(item.camera) },
                                        onClick = {
                                            viewModel.selectCameraFromTree(
                                                camera = item.camera,
                                                onGridFull = {
                                                    MobileTopPopupManager.warning(gridFullMsg)
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
                title = sheetTitle,
                rows = groupTreeRows,
                totalAllCamerasCount = totalAllCamerasCount,
                selectedFilterNodeKey = uiState.selectedFilterNodeKey,
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
            WindowCompat.getInsetsController(it, it.decorView)
        }

        if (insetsController != null) {
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        }

        onDispose {
            activity?.requestedOrientation =
                originalOrientation ?: ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            insetsController?.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    BackHandler {
        onExit()
    }
}

private fun restoreSystemOrientationAndBars(activity: Activity?) {
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    val window = activity?.window
    val insetsController = window?.let {
        WindowCompat.getInsetsController(it, it.decorView)
    }
    insetsController?.show(WindowInsetsCompat.Type.systemBars())
}

@Composable
private fun LiveGrid2x2Stage(
    uiState: MonitorUiState,
    isTabActive: Boolean,
    panelBg: Color,
    gridDividerBg: Color,
    onCellTap: (Int) -> Unit,
    onRetryClick: (Int) -> Unit,
    onSuccess: (Int) -> Unit,
    onSnapshotError: (Int) -> Unit,
    onReloadClick: (CameraModel, Int) -> Unit,
    onRemoveClick: (Int) -> Unit
) {
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.appColors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gridDividerBg),
        verticalArrangement = Arrangement.spacedBy(spacing.space2)
    ) {
        for (row in 0..1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(spacing.space2)
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
                                    Modifier.border(spacing.borderFocus, colors.accent)
                                } else {
                                    Modifier
                                }
                            )
                    ) {
                        if (selectedCam != null) {
                            CameraGridItem(
                                selectedCamera = selectedCam,
                                isTabActive = isTabActive,
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
                                isActiveSlot = isActiveSlot
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
    onClick: () -> Unit
) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val bgColor = if (isSelected) colors.accent else Color.Transparent
    val contentColor = if (isSelected) colors.onAccent else colors.textSubtle

    Box(
        modifier = Modifier
            .height(spacing.avatarSmall)
            .clip(RoundedCornerShape(spacing.radiusSmall))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.space8),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = contentColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun MobileCameraSnapshotCard(
    camera: CameraModel,
    isPlaying: Boolean,
    hasSnapKey: Boolean,
    snapshotUrl: String?,
    onSnapshotError: () -> Unit,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing

    val cardBg = when {
        isPlaying && colors.isDark -> colors.accent.copy(alpha = 0.14f)
        isPlaying -> colors.accent.copy(alpha = 0.10f)
        else -> colors.inputBg
    }
    val thumbBg = colors.thumbnailBg
    val noImageTint = colors.textPlaceholder.copy(alpha = 0.45f)
    val nameColor = if (isPlaying) colors.accent else colors.textTitle

    val imageRequest = remember(snapshotUrl) {
        if (snapshotUrl.isNullOrEmpty()) {
            null
        } else {
            ImageRequest.Builder(context)
                .data(snapshotUrl)
                .size(SNAPSHOT_CARD_WIDTH_PX, SNAPSHOT_CARD_HEIGHT_PX)
                .precision(Precision.INEXACT)
                .allowHardware(true)
                .crossfade(false)
                .build()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(spacing.radiusSmallMedium))
            .background(cardBg)
            .clickable(onClick = onClick)
    ) {
        // .cam-card-thumb (16:9)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(LIVE_STAGE_ASPECT_RATIO)
                .background(thumbBg),
            contentAlignment = Alignment.Center
        ) {
            if (imageRequest != null) {
                SubcomposeAsyncImage(
                    model = imageRequest,
                    contentDescription = camera.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            WaveDotsLoading(
                                dotSize = spacing.space6,
                                dotColor = colors.accent,
                                maxOffset = spacing.space6
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
                                modifier = Modifier.size(spacing.iconMedium)
                            )
                        }
                    }
                )
            } else if (!hasSnapKey) {
                WaveDotsLoading(
                    dotSize = spacing.space6,
                    dotColor = colors.accent,
                    maxOffset = spacing.space6
                )
            } else {
                Icon(
                    imageVector = LucideIcons.ImageOff,
                    contentDescription = null,
                    tint = noImageTint,
                    modifier = Modifier.size(spacing.iconMedium)
                )
            }

            // .cam-card-overlay-badge -> .cam-playing-badge
            if (isPlaying) {
                val isEn = rememberIsEnglish()
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(spacing.space6)
                        .clip(CircleShape)
                        .background(colors.accent)
                        .padding(horizontal = spacing.space8, vertical = spacing.space2)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(spacing.space5)
                    ) {
                        PlayingEqualizerBars()
                        Text(
                            text = if (isEn) "Playing" else "Đang xem",
                            color = colors.onAccent,
                            style = MaterialTheme.typography.micro,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // .cam-card-footer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = spacing.avatarSmall)
                .padding(horizontal = spacing.space8, vertical = spacing.space6),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = camera.name,
                color = nameColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isPlaying) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PlayingEqualizerBars() {
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.appColors
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
        modifier = Modifier.height(spacing.space10),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(spacing.space2)
    ) {
        Box(
            modifier = Modifier
                .width(spacing.space2)
                .height(spacing.space4)
                .graphicsLayer {
                    scaleY = scale1
                    transformOrigin = TransformOrigin(0.5f, 1f)
                }
                .background(colors.onAccent, RoundedCornerShape(spacing.space1))
        )
        Box(
            modifier = Modifier
                .width(spacing.space2)
                .height(spacing.space10)
                .graphicsLayer {
                    scaleY = scale2
                    transformOrigin = TransformOrigin(0.5f, 1f)
                }
                .background(colors.onAccent, RoundedCornerShape(spacing.space1))
        )
        Box(
            modifier = Modifier
                .width(spacing.space2)
                .height(spacing.space6)
                .graphicsLayer {
                    scaleY = scale3
                    transformOrigin = TransformOrigin(0.5f, 1f)
                }
                .background(colors.onAccent, RoundedCornerShape(spacing.space1))
        )
    }
}

@Composable
private fun MobileGroupFilterBottomSheet(
    title: String,
    rows: List<GroupSheetRow>,
    totalAllCamerasCount: Int,
    selectedFilterNodeKey: String,
    onDismiss: () -> Unit,
    onToggleExpand: (String) -> Unit,
    onRowClick: (String) -> Unit
) {
    val isEn = rememberIsEnglish()
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing

    MobileFullScreenOverlay(
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
                .padding(horizontal = spacing.space10, vertical = spacing.space10),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = spacing.contentMaxWidth)
                    .fillMaxWidth()
                    .heightIn(max = GroupFilterSheetMaxHeight)
                    .clip(RoundedCornerShape(spacing.radiusLarge))
                    .background(colors.surface)
                    .border(spacing.borderMedium, colors.border, RoundedCornerShape(spacing.radiusLarge))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .padding(
                        start = spacing.space10,
                        end = spacing.space10,
                        top = spacing.space10,
                        bottom = spacing.space12
                    )
            ) {
                // Sheet Header (drag handle + icon ở giữa + tiêu đề căn giữa + nút đóng)
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(spacing.sheetHandleWidth)
                        .height(spacing.sheetHandleHeight)
                        .clip(CircleShape)
                        .background(colors.handle)
                )

                Spacer(modifier = Modifier.height(spacing.space10))

                Box(modifier = Modifier.fillMaxWidth()) {
                    IconBadge(
                        icon = LucideIcons.ListFilter,
                        backgroundColor = colors.accent,
                        size = spacing.avatarLarge,
                        iconSize = spacing.space28,
                        cornerRadius = spacing.radiusMedium,
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = spacing.space4)
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(spacing.avatarSmall)
                            .clip(CircleShape)
                            .background(colors.controlBg)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = LucideIcons.X,
                            contentDescription = if (isEn) "Close" else "Đóng",
                            tint = colors.textMuted,
                            modifier = Modifier.size(spacing.iconSmallMedium)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(spacing.space14))

                Text(
                    text = title,
                    color = colors.textStrong,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(horizontal = spacing.space8)
                        .padding(bottom = spacing.space12)
                )

                // Sheet Body
                if (rows.isEmpty() && totalAllCamerasCount == 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(GroupFilterEmptyHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        CommonEmptyState(text = if (isEn) "No camera groups" else "Không có nhóm camera")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(spacing.space2)
                    ) {
                        // Top option: "Tất cả camera" / "All cameras"
                        item(key = MonitorViewModel.FILTER_ALL) {
                            val isAllSelected = selectedFilterNodeKey == MonitorViewModel.FILTER_ALL
                            val allIconTint = if (isAllSelected) colors.accent else colors.textMuted
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = GroupRowBaseHeight)
                                    .clip(RoundedCornerShape(spacing.radiusSmall))
                                    .background(
                                        if (isAllSelected) colors.accent.copy(alpha = 0.12f) else Color.Transparent
                                    )
                                    .clickable { onRowClick(MonitorViewModel.FILTER_ALL) }
                                    .padding(
                                        start = spacing.space10,
                                        end = spacing.space10,
                                        top = spacing.space6,
                                        bottom = spacing.space6
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(spacing.space8)
                            ) {
                                Spacer(modifier = Modifier.size(spacing.iconMedium))

                                Icon(
                                    imageVector = LucideIcons.Video,
                                    contentDescription = null,
                                    tint = allIconTint,
                                    modifier = Modifier.size(spacing.iconSmall)
                                )

                                Text(
                                    text = if (isEn) "All cameras" else "Tất cả camera",
                                    color = if (isAllSelected) colors.accent else colors.textStrong,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isAllSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(spacing.radiusTiny))
                                        .background(
                                            if (isAllSelected) colors.accent.copy(alpha = 0.18f) else colors.controlBg
                                        )
                                        .padding(horizontal = spacing.space6, vertical = spacing.space2),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = totalAllCamerasCount.toString(),
                                        color = if (isAllSelected) colors.accent else colors.textMuted,
                                        style = MaterialTheme.typography.caption,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                if (isAllSelected) {
                                    Icon(
                                        imageVector = LucideIcons.Check,
                                        contentDescription = null,
                                        tint = colors.accent,
                                        modifier = Modifier.size(spacing.iconSmall)
                                    )
                                }
                            }
                        }

                        items(items = rows, key = { it.key }) { row ->
                            val isSelected = selectedFilterNodeKey == row.key
                            val indentDp = spacing.space10 + (spacing.space16 * minOf(row.depth, 5))
                            val iconTint = if (isSelected) colors.accent else colors.textMuted

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = GroupRowBaseHeight)
                                    .clip(RoundedCornerShape(spacing.radiusSmall))
                                    .background(
                                        if (isSelected) colors.accent.copy(alpha = 0.12f) else Color.Transparent
                                    )
                                    .clickable { onRowClick(row.key) }
                                    .padding(
                                        start = indentDp,
                                        end = spacing.space10,
                                        top = spacing.space6,
                                        bottom = spacing.space6
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(spacing.space8)
                            ) {
                                if (row.hasSubGroups) {
                                    Box(
                                        modifier = Modifier
                                            .size(spacing.iconMedium)
                                            .clip(RoundedCornerShape(spacing.radiusTiny))
                                            .clickable { onToggleExpand(row.key) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = LucideIcons.ChevronDown,
                                            contentDescription = if (isEn) "Expand group" else "Mở rộng nhóm",
                                            tint = iconTint,
                                            modifier = Modifier
                                                .size(spacing.iconSmall)
                                                .rotate(if (row.isExpanded) 0f else -90f)
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.size(spacing.iconMedium))
                                }

                                Icon(
                                    imageVector = if (row.isExpanded && row.hasSubGroups) {
                                        LucideIcons.FolderOpen
                                    } else {
                                        LucideIcons.Folder
                                    },
                                    contentDescription = null,
                                    tint = iconTint,
                                    modifier = Modifier.size(spacing.iconSmall)
                                )

                                Text(
                                    text = row.title,
                                    color = if (isSelected) colors.accent else colors.textStrong,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected || row.isVmsNode) FontWeight.SemiBold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(spacing.radiusTiny))
                                        .background(
                                            if (isSelected) colors.accent.copy(alpha = 0.18f) else colors.controlBg
                                        )
                                        .padding(horizontal = spacing.space6, vertical = spacing.space2),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = row.count.toString(),
                                        color = if (isSelected) colors.accent else colors.textMuted,
                                        style = MaterialTheme.typography.caption,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = LucideIcons.Check,
                                        contentDescription = null,
                                        tint = colors.accent,
                                        modifier = Modifier.size(spacing.iconSmall)
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
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
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

    val surfaceBg = if (isFullscreen) colors.mediaBg else colors.surface

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
                LiveStreamPlayer(
                    wsUrl = selectedCamera.streamUrl,
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
                    .background(surfaceBg),
                contentAlignment = Alignment.Center
            ) {
                if (selectedCamera.fallbackImageUrl.isNotEmpty() && !snapshotFailed) {
                    AsyncImage(
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
                WaveDotsLoading(dotColor = colors.accent)
            }
        }

        // Error Overlay (chạm để tải lại luồng live giống reloadStream trong LiveVideoView.vue)
        if (hasError) {
            val isEn = rememberIsEnglish()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(surfaceBg)
                    .clickable {
                        onClick()
                        infoTrigger++
                        onBackToLiveClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                val errorMsg = when {
                    selectedCamera.streamUrl.startsWith("error://no-video") -> if (isEn) "No signal" else "Không có tín hiệu"
                    selectedCamera.streamUrl.startsWith("error://failed") -> if (isEn) "Camera connection error" else "Lỗi kết nối camera"
                    else -> if (isEn) "Video load error" else "Lỗi tải video"
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
            val overlayPillBg = colors.mediaBg.copy(alpha = 0.42f)
            val overlayPillBorder = colors.onMedia.copy(alpha = 0.12f)
            val edgePadding = if (isCompact) spacing.space4 else spacing.space8

            Box(modifier = Modifier.fillMaxSize()) {
                // .info-name
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(edgePadding)
                        .widthIn(max = if (isCompact) CameraInfoPillMaxWidthCompact else CameraInfoPillMaxWidthRegular)
                        .clip(RoundedCornerShape(spacing.radiusSmall))
                        .background(overlayPillBg)
                        .border(spacing.borderMedium, overlayPillBorder, RoundedCornerShape(spacing.radiusSmall))
                        .padding(
                            horizontal = if (isCompact) spacing.space6 else spacing.space8,
                            vertical = if (isCompact) spacing.space2 else spacing.space3
                        )
                ) {
                    Text(
                        text = selectedCamera.camera.name,
                        color = colors.onMedia,
                        style = if (isCompact) MaterialTheme.typography.caption else MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // .info-action (Close button x-circle)
                if (!isFullscreen) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(edgePadding)
                            .clip(RoundedCornerShape(spacing.radiusSmall))
                            .background(overlayPillBg)
                            .border(spacing.borderMedium, overlayPillBorder, RoundedCornerShape(spacing.radiusSmall))
                            .padding(if (isCompact) spacing.space2 else spacing.space3)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(if (isCompact) spacing.iconMedium else spacing.iconButtonSmall)
                                .clip(RoundedCornerShape(spacing.radiusTiny))
                                .clickable { onRemoveClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = LucideIcons.XCircle,
                                contentDescription = "Đóng luồng trực tiếp",
                                tint = colors.onMedia,
                                modifier = Modifier.size(if (isCompact) spacing.iconXSmall else spacing.iconSmall)
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
                    .padding(if (isCompact) spacing.space4 else spacing.space6)
                    .size(if (isFullscreen) spacing.iconLarge else spacing.iconButtonSmall)
                    .clip(RoundedCornerShape(spacing.radiusSmall))
                    .background(colors.mediaBg.copy(alpha = 0.42f))
                    .clickable { onFullscreenClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isFullscreen) LucideIcons.Minimize else LucideIcons.Maximize,
                    contentDescription = "Toàn màn hình",
                    tint = colors.onMedia,
                    modifier = Modifier.size(if (isFullscreen) spacing.iconSmallMedium else spacing.iconSmall)
                )
            }
        }
    }
}

@Composable
fun EmptyGridItem(
    onClick: () -> Unit,
    isCompact: Boolean = false,
    isActiveSlot: Boolean = false
) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val iconColor = if (isActiveSlot) colors.accent else colors.textPlaceholder

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = LucideIcons.Video,
            contentDescription = "Chọn Camera",
            tint = iconColor,
            modifier = Modifier.size(if (isCompact) spacing.iconLarge else spacing.iconExtraLarge)
        )
    }
}
