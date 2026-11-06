package com.tenli.oneview.ui.features.monitor

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
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

private const val LIVE_STAGE_ASPECT_RATIO = 16f / 9f
private const val SNAPSHOT_CARD_WIDTH_PX = 420
private const val SNAPSHOT_CARD_HEIGHT_PX = 236
private const val LIVE_STAGE_SNAPSHOT_WIDTH_PX = 1280
private const val LIVE_STAGE_SNAPSHOT_HEIGHT_PX = 720
private const val LIVE_PAGE_ENTER_DURATION_MS = 280
private const val LIVE_PAGE_EXIT_DURATION_MS = 240
private val LivePageEnterEasing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
private val LivePageExitEasing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
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
    val view = LocalView.current
    val uiScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val searchFocusRequester = remember { FocusRequester() }
    var isSearchExpanded by remember { mutableStateOf(false) }
    val currentSearchQuery by rememberUpdatedState(uiState.searchQuery)
    val dismissKeyboard: () -> Unit = remember(focusManager, keyboardController) {
        {
            focusManager.clearFocus()
            keyboardController?.hide()
            if (currentSearchQuery.isEmpty()) {
                isSearchExpanded = false
            }
        }
    }
    val cameraGridState = rememberLazyGridState()
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing

    val activeLiveCamera = uiState.selectedCameras.getOrNull(0)
    val isLiveScreenOpen = activeLiveCamera != null

    LaunchedEffect(uiState.searchQuery) {
        if (uiState.searchQuery.isNotEmpty()) {
            isSearchExpanded = true
        }
    }

    LaunchedEffect(isSearchExpanded) {
        if (isSearchExpanded) {
            delay(32L)
            runCatching { searchFocusRequester.requestFocus() }
            keyboardController?.show()
        }
    }

    BackHandler(
        enabled = isTabActive && isSearchExpanded && uiState.searchQuery.isEmpty() && !isLiveScreenOpen && !uiState.showGroupFilterSheet
    ) {
        dismissKeyboard()
    }

    var wasDeactivated by remember { mutableStateOf(true) }
    LaunchedEffect(isTabActive) {
        viewModel.setMonitorTabActive(isTabActive)
        if (!isTabActive) {
            dismissKeyboard()
            wasDeactivated = true
        } else if (wasDeactivated) {
            wasDeactivated = false
            viewModel.refreshActiveStreams()
            viewModel.loadVisibleCameraSnapshots()
        }
    }

    LaunchedEffect(isLiveScreenOpen) {
        if (isLiveScreenOpen) {
            dismissKeyboard()
        }
    }

    LaunchedEffect(cameraGridState.isScrollInProgress) {
        if (cameraGridState.isScrollInProgress) {
            dismissKeyboard()
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
    val totalFilteredCamerasCount = uiState.totalFilteredCamerasCount
    val groupTreeRows = uiState.groupTreeRows
    val pagedCameraItems = uiState.pagedCameraItems
    val currentPagedCameraItems by rememberUpdatedState(pagedCameraItems)

    val sheetTitle = if (isEn) "Filter camera groups" else "Lọc nhóm camera"

    // Theo dõi các camera đang thực sự hiển thị trong khung nhìn của LazyVerticalGrid:
    // - Chỉ tải ảnh snapshot cho những camera đang hiển thị trên màn hình.
    // - Ngay khi camera cuộn khuất (hoặc chuyển sang màn hình Live / rời tab), hủy ngay việc tải đang dở.
    LaunchedEffect(cameraGridState, isTabActive, isLiveScreenOpen) {
        if (!isTabActive || isLiveScreenOpen) {
            viewModel.cancelAllCardSnapshotFetches()
            return@LaunchedEffect
        }
        snapshotFlow {
            val items = currentPagedCameraItems
            if (items.isEmpty()) {
                emptyList()
            } else {
                cameraGridState.layoutInfo.visibleItemsInfo.mapNotNull { info ->
                    items.getOrNull(info.index)?.camera
                }
            }
        }
            .distinctUntilChanged { old, new ->
                old.size == new.size && old.indices.all { idx -> old[idx].id == new[idx].id }
            }
            .collect { visibleCameras ->
                viewModel.onVisibleCamerasChanged(visibleCameras)
            }
    }

    // Scroll camera grid to top on filter / search change
    LaunchedEffect(
        uiState.selectedFilterNodeKey,
        uiState.searchQuery
    ) {
        cameraGridState.scrollToItem(0)
    }

    val panelBg = colors.surface
    val bottomClearance = spacing.bottomNavHeight + spacing.space20

    Box(modifier = Modifier.fillMaxSize()) {
        // =========================================================================================
        // 1. MÀN HÌNH DANH SÁCH LƯỚI CAMERA BAN ĐẦU (Không phát live tự động)
        // =========================================================================================
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { dismissKeyboard() })
                }
        ) {
            MobileTopBar(
                title = stringResource(id = R.string.tab_monitor),
                rightAction = {
                    MobileTopActionButton(
                        icon = LucideIcons.ListFilter,
                        contentDescription = if (isEn) "Filter" else "Bộ lọc",
                        hasActiveDot = uiState.selectedFilterNodeKey != MonitorViewModel.FILTER_ALL,
                        onClick = {
                            val isImeOpen = ViewCompat.getRootWindowInsets(view)
                                ?.isVisible(WindowInsetsCompat.Type.ime()) == true
                            dismissKeyboard()
                            if (isImeOpen) {
                                uiScope.launch {
                                    delay(240L)
                                    viewModel.openGroupFilterSheet()
                                }
                            } else {
                                viewModel.openGroupFilterSheet()
                            }
                        }
                    )
                }
            )

            if (uiState.isLoading && !hasAvailableCameras) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = bottomClearance),
                    contentAlignment = Alignment.Center
                ) {
                    if (isTabActive) {
                        WaveDotsLoading()
                    }
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
                Spacer(modifier = Modifier.height(spacing.space4))

                // Camera Panel (.mobile-camera-panel) chiếm toàn bộ chiều cao màn hình
                val controlBg = colors.controlBg
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
                    // .camera-filter-header: Bên trái hiển thị đường dẫn nhóm ("Tất cả camera" hoặc "Nhóm - VMS"),
                    // bên phải là nút icon tìm kiếm thu gọn; khi bấm vào sẽ trượt rộng ra toàn chiều ngang.
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = spacing.space10,
                                end = spacing.space10,
                                top = spacing.space8,
                                bottom = spacing.space4
                            )
                            .height(spacing.buttonHeightSmall)
                    ) {
                        val expandProgress by animateFloatAsState(
                            targetValue = if (isSearchExpanded) 1f else 0f,
                            animationSpec = tween(
                                durationMillis = 280,
                                easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
                            ),
                            label = "cameraSearchExpandProgress"
                        )
                        val collapsedWidth = spacing.buttonHeightSmall
                        val currentSearchWidth =
                            collapsedWidth + (maxWidth - collapsedWidth) * expandProgress
                        val pathAlpha = (1f - expandProgress * 1.85f).coerceIn(0f, 1f)
                        val searchContentAlpha =
                            ((expandProgress - 0.15f) / 0.85f).coerceIn(0f, 1f)
                        val searchHorizontalPad =
                            (spacing.buttonHeightSmall - spacing.iconSmall) / 2

                        if (pathAlpha > 0.01f) {
                            val isAllCameras =
                                uiState.selectedFilterNodeKey == MonitorViewModel.FILTER_ALL ||
                                    (uiState.selectedFilterGroupName.isBlank() && uiState.selectedFilterVmsName.isBlank())
                            val basePathText = when {
                                isAllCameras -> if (isEn) "All cameras" else "Tất cả camera"
                                uiState.selectedFilterGroupName.isBlank() -> uiState.selectedFilterVmsName
                                uiState.selectedFilterVmsName.isNotBlank() ->
                                    "${uiState.selectedFilterGroupName} - ${uiState.selectedFilterVmsName}"
                                else -> uiState.selectedFilterGroupName
                            }
                            Row(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .fillMaxHeight()
                                    .padding(
                                        start = spacing.space2,
                                        end = collapsedWidth + spacing.space8
                                    )
                                    .graphicsLayer {
                                        alpha = pathAlpha
                                        translationX = -expandProgress * 16.dp.toPx()
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = LucideIcons.FolderOpen,
                                    contentDescription = null,
                                    tint = colors.textMuted,
                                    modifier = Modifier.size(spacing.iconSmall)
                                )
                                Spacer(modifier = Modifier.width(spacing.space6))
                                Text(
                                    text = basePathText,
                                    color = colors.textMuted,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.width(spacing.space4))
                                Text(
                                    text = "(${totalFilteredCamerasCount})",
                                    color = colors.textMuted,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .width(currentSearchWidth)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(spacing.radiusSmallMedium))
                                .background(controlBg)
                                .then(
                                    if (!isSearchExpanded) {
                                        Modifier.clickable {
                                            isSearchExpanded = true
                                        }
                                    } else {
                                        Modifier
                                    }
                                )
                                .padding(horizontal = searchHorizontalPad),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = LucideIcons.Search,
                                contentDescription = if (isEn) "Search camera" else "Tìm camera",
                                tint = if (colors.isDark) colors.accent else colors.textMuted,
                                modifier = Modifier.size(spacing.iconSmall)
                            )
                            if (expandProgress > 0.08f) {
                                Spacer(modifier = Modifier.width(spacing.space6))
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .graphicsLayer { alpha = searchContentAlpha },
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (uiState.searchQuery.isEmpty()) {
                                        Text(
                                            text = if (isEn) "Search camera..." else "Tìm camera...",
                                            color = placeholderColor,
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    BasicTextField(
                                        value = uiState.searchQuery,
                                        onValueChange = { viewModel.setSearchQuery(it) },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                        keyboardActions = KeyboardActions(
                                            onSearch = { dismissKeyboard() },
                                            onDone = { dismissKeyboard() }
                                        ),
                                        textStyle = MaterialTheme.typography.bodySmall.copy(
                                            color = colors.textStrong
                                        ),
                                        cursorBrush = SolidColor(colors.accent),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(searchFocusRequester)
                                    )
                                }
                                Spacer(modifier = Modifier.width(spacing.space4))
                                Box(
                                    modifier = Modifier
                                        .size(spacing.badgeMinSize)
                                        .graphicsLayer { alpha = searchContentAlpha }
                                        .clip(CircleShape)
                                        .background(colors.textMuted.copy(alpha = 0.2f))
                                        .clickable {
                                            if (uiState.searchQuery.isNotEmpty()) {
                                                viewModel.setSearchQuery("")
                                                searchFocusRequester.requestFocus()
                                                keyboardController?.show()
                                            } else {
                                                dismissKeyboard()
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = LucideIcons.X,
                                        contentDescription = if (isEn) "Clear or close search" else "Xóa hoặc đóng tìm kiếm",
                                        tint = colors.textMuted,
                                        modifier = Modifier.size(spacing.iconTiny)
                                    )
                                }
                            }
                        }
                    }

                    // .mobile-camera-list-scroll -> Lưới camera 2 cột
                    if (pagedCameraItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(
                                start = spacing.space8,
                                end = spacing.space8,
                                top = spacing.space6,
                                bottom = spacing.space8
                            ),
                            horizontalArrangement = Arrangement.spacedBy(spacing.space8),
                            verticalArrangement = Arrangement.spacedBy(spacing.space8)
                        ) {
                            items(
                                items = pagedCameraItems,
                                key = { it.key },
                                contentType = { "camera_card" }
                            ) { item ->
                                val hasSnapKey = uiState.cameraSnapshots.containsKey(item.camera.id)
                                val snapUrl = uiState.cameraSnapshots[item.camera.id]

                                MobileCameraSnapshotCard(
                                    camera = item.camera,
                                    isTabActive = isTabActive && !isLiveScreenOpen,
                                    hasSnapKey = hasSnapKey,
                                    snapshotUrl = snapUrl,
                                    onSnapshotError = { viewModel.onCardSnapshotError(item.camera) },
                                    onClick = {
                                        val isImeOpen = ViewCompat.getRootWindowInsets(view)
                                            ?.isVisible(WindowInsetsCompat.Type.ime()) == true
                                        dismissKeyboard()
                                        if (isImeOpen) {
                                            uiScope.launch {
                                                delay(240L)
                                                viewModel.openLiveCamera(item.camera)
                                            }
                                        } else {
                                            viewModel.openLiveCamera(item.camera)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Group Filter Bottom Sheet (.mobile-group-sheet-overlay)
        if (uiState.showGroupFilterSheet && !isLiveScreenOpen) {
            MobileGroupFilterBottomSheet(
                title = sheetTitle,
                rows = groupTreeRows,
                selectedFilterNodeKey = uiState.selectedFilterNodeKey,
                onDismiss = { viewModel.closeGroupFilterSheet() },
                onToggleExpand = { viewModel.toggleNodeExpansion(it) },
                onRowClick = { viewModel.handleGroupRowClick(it) }
            )
        }
    }
}

/**
 * Lớp phủ toàn màn hình cho trang Xem trực tiếp 1x1 (đặt ở zIndex(30f) trong MainScreen,
 * phía trên thanh điều hướng dưới cùng giống hệt màn hình Cấu hình thông báo).
 * Nhờ đó khi trượt vào hoặc bấm Back trượt ra, toàn bộ chiều cao từ đỉnh xuống đáy màn hình
 * (bao gồm phần đè lên menu bottom) cùng di chuyển mượt mà.
 */
@Composable
fun MonitorLiveCameraOverlay(
    viewModel: MonitorViewModel,
    isTabActive: Boolean,
    modifier: Modifier = Modifier,
    onFullscreenChange: (Boolean) -> Unit = {},
    onLiveScreenOpenChange: (Boolean) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current as? Activity

    // fullscreenIndex != null: 1x1 single-camera landscape fullscreen
    var fullscreenIndex by remember { mutableStateOf<Int?>(null) }

    val activeLiveCamera = uiState.selectedCameras.getOrNull(0)
    val isLiveScreenOpen = activeLiveCamera != null

    // Giữ lại thông tin camera cuối cùng trong lúc hiệu ứng trượt đóng màn hình Live đang chạy
    var displayedLiveCamera by remember { mutableStateOf<SelectedCamera?>(null) }
    if (activeLiveCamera != null) {
        displayedLiveCamera = activeLiveCamera
    }

    val isSingleFullscreen = fullscreenIndex == 0 && isLiveScreenOpen

    LaunchedEffect(isTabActive) {
        if (!isTabActive && fullscreenIndex != null) {
            restoreSystemOrientationAndBars(activity)
            fullscreenIndex = null
        }
    }

    LaunchedEffect(isLiveScreenOpen) {
        onLiveScreenOpenChange(isLiveScreenOpen)
    }

    LaunchedEffect(isLiveScreenOpen, isSingleFullscreen) {
        if (!isLiveScreenOpen && fullscreenIndex != null) {
            restoreSystemOrientationAndBars(activity)
            fullscreenIndex = null
        }
        onFullscreenChange(isSingleFullscreen)
    }

    if (isSingleFullscreen) {
        LandscapeFullscreenHandler(
            activity = activity,
            onExit = {
                restoreSystemOrientationAndBars(activity)
                fullscreenIndex = null
            }
        )
    } else if (isLiveScreenOpen && isTabActive) {
        BackHandler {
            viewModel.closeLiveCamera()
        }
    }

    AnimatedVisibility(
        visible = isLiveScreenOpen,
        modifier = modifier.fillMaxSize(),
        enter = slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(durationMillis = LIVE_PAGE_ENTER_DURATION_MS, easing = LivePageEnterEasing)
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(durationMillis = LIVE_PAGE_EXIT_DURATION_MS, easing = LivePageExitEasing)
        )
    ) {
        val isSliding = transition.isRunning
        val currentCam = activeLiveCamera ?: displayedLiveCamera
        if (currentCam != null) {
            val locationLabel = remember(currentCam.camera.id, uiState.groupTreeRows) {
                viewModel.getCameraLocationLabel(currentCam.camera)
            }
            MobileLiveCameraScreen(
                selectedCamera = currentCam,
                locationLabel = locationLabel,
                isTabActive = isTabActive && isLiveScreenOpen,
                isFullscreen = isSingleFullscreen,
                isSliding = isSliding,
                onBackClick = {
                    if (isSingleFullscreen) {
                        restoreSystemOrientationAndBars(activity)
                        fullscreenIndex = null
                    }
                    viewModel.closeLiveCamera()
                },
                onToggleFullscreen = {
                    if (isSingleFullscreen) {
                        restoreSystemOrientationAndBars(activity)
                        fullscreenIndex = null
                    } else {
                        fullscreenIndex = 0
                    }
                },
                onRetryClick = { viewModel.retryCameraStream(0) },
                onSuccess = { viewModel.resetRetryCount(0) },
                onSnapshotError = { viewModel.onSnapshotLoadError(0) },
                onReloadClick = { viewModel.reloadLiveCamera() },
                onSwitchChannel = { channel -> viewModel.switchLiveChannel(channel) }
            )
        }
    }
}

@Composable
private fun MobileLiveCameraScreen(
    selectedCamera: SelectedCamera,
    locationLabel: String,
    isTabActive: Boolean,
    isFullscreen: Boolean,
    isSliding: Boolean = false,
    onBackClick: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onRetryClick: () -> Unit,
    onSuccess: () -> Unit,
    onSnapshotError: () -> Unit,
    onReloadClick: () -> Unit,
    onSwitchChannel: (Int) -> Unit
) {
    val isEn = rememberIsEnglish()
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val edgeBorderColor = colors.border
    val ipText = remember(selectedCamera.camera.id) {
        MonitorViewModel.getCameraIpText(selectedCamera.camera)
    }
    val hasDualStreams = remember(selectedCamera.camera.id) {
        selectedCamera.camera.sub.isNotBlank() && selectedCamera.camera.main.isNotBlank()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isFullscreen) colors.mediaBg else MaterialTheme.colorScheme.background)
            .drawWithContent {
                drawContent()
                if (isSliding && !isFullscreen) {
                    drawRect(
                        color = edgeBorderColor,
                        topLeft = Offset.Zero,
                        size = Size(spacing.borderThin.toPx(), size.height)
                    )
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
    ) {
        if (!isFullscreen) {
            MobileTopBar(
                title = selectedCamera.camera.name,
                leftAction = {
                    Box(
                        modifier = Modifier
                            .size(spacing.iconButton)
                            .clip(CircleShape)
                            .clickable(onClick = onBackClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = LucideIcons.ChevronLeft,
                            contentDescription = stringResource(id = R.string.common_back),
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(spacing.iconMedium)
                        )
                    }
                },
                rightAction = {
                    MobileTopActionButton(
                        icon = LucideIcons.RefreshCw,
                        contentDescription = if (isEn) "Reload stream" else "Tải lại luồng",
                        onClick = onReloadClick
                    )
                }
            )
        }

        // Khung phát Live 1x1 (giữ nguyên cùng 1 node trong cây Compose khi xoay ngang/dọc để không ngắt kết nối WS/ExoPlayer)
        Box(
            modifier = if (isFullscreen) {
                Modifier
                    .fillMaxSize()
                    .background(colors.mediaBg)
            } else {
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(LIVE_STAGE_ASPECT_RATIO)
                    .background(colors.mediaBg)
            }
        ) {
            CameraGridItem(
                selectedCamera = selectedCamera,
                isTabActive = isTabActive,
                onClick = {},
                onRetryClick = onRetryClick,
                onSuccess = onSuccess,
                onSnapshotError = onSnapshotError,
                onFullscreenClick = onToggleFullscreen,
                onBackToLiveClick = onReloadClick,
                onRemoveClick = onBackClick,
                isFullscreen = isFullscreen,
                isCompact = false,
                showFullscreenButton = true,
                showCloseButton = false
            )
        }

        if (!isFullscreen) {
            Spacer(modifier = Modifier.height(spacing.space6))

            Text(
                text = if (isEn) "CAMERA INFORMATION" else "THÔNG TIN CAMERA",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                color = colors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(
                    start = spacing.space24,
                    end = spacing.space14,
                    top = spacing.space10,
                    bottom = spacing.space6
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.space10)
                    .clip(RoundedCornerShape(spacing.radiusMedium))
                    .background(colors.surface)
                    .padding(horizontal = spacing.space16, vertical = spacing.space12),
                verticalArrangement = Arrangement.spacedBy(spacing.space2)
            ) {
                LiveCameraInfoRow(
                    label = if (isEn) "Camera name" else "Tên camera",
                    value = selectedCamera.camera.name
                )

                if (locationLabel.isNotEmpty()) {
                    LiveCameraInfoRow(
                        label = if (isEn) "Group / Area" else "Khu vực / Nhóm",
                        value = locationLabel
                    )
                }

                if (ipText.isNotEmpty()) {
                    LiveCameraInfoRow(
                        label = if (isEn) "IP address" else "Địa chỉ IP",
                        value = ipText
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = spacing.space32)
                        .padding(vertical = spacing.space5),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isEn) "Stream quality" else "Chất lượng luồng",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textMuted,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(spacing.space12))
                    if (hasDualStreams) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(spacing.radiusSmallMedium))
                                .background(colors.controlBg)
                                .padding(spacing.space3),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.space2)
                        ) {
                            GridOptionButton(
                                label = if (isEn) "Standard (SD)" else "Tiêu chuẩn (SD)",
                                isSelected = selectedCamera.channel == 1,
                                onClick = { onSwitchChannel(1) }
                            )
                            GridOptionButton(
                                label = if (isEn) "High (HD)" else "Cao (HD)",
                                isSelected = selectedCamera.channel == 0,
                                onClick = { onSwitchChannel(0) }
                            )
                        }
                    } else {
                        Text(
                            text = if (selectedCamera.channel == 0) {
                                if (isEn) "High (HD)" else "Chất lượng cao (HD)"
                            } else {
                                if (isEn) "Standard (SD)" else "Tiêu chuẩn (SD)"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textStrong,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveCameraInfoRow(
    label: String,
    value: String
) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = spacing.space32)
            .padding(vertical = spacing.space5),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textMuted,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.width(spacing.space12))
        Box(modifier = Modifier.weight(1f, fill = false), contentAlignment = Alignment.CenterEnd) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textStrong,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MobileCameraSnapshotCard(
    camera: CameraModel,
    isTabActive: Boolean,
    hasSnapKey: Boolean,
    snapshotUrl: String?,
    onSnapshotError: () -> Unit,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var isTapFlashing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val currentOnClick by rememberUpdatedState(onClick)
    val currentIsPressed by rememberUpdatedState(isPressed)

    val handleCardClick: () -> Unit = remember(scope, view, focusManager, keyboardController) {
        {
            val keyboardWasOpen = ViewCompat.getRootWindowInsets(view)
                ?.isVisible(WindowInsetsCompat.Type.ime()) == true
            focusManager.clearFocus()
            keyboardController?.hide()

            if (keyboardWasOpen) {
                // Nếu bàn phím đang mở: ưu tiên đóng bàn phím xong hoàn toàn (~260ms) rồi mới trượt mở màn hình Live
                isTapFlashing = true
                scope.launch {
                    delay(260L)
                    currentOnClick()
                    delay(160L)
                    isTapFlashing = false
                }
            } else if (currentIsPressed) {
                isTapFlashing = true
                currentOnClick()
                scope.launch {
                    delay(180L)
                    isTapFlashing = false
                }
            } else {
                isTapFlashing = true
                scope.launch {
                    delay(55L)
                    currentOnClick()
                    delay(180L)
                    isTapFlashing = false
                }
            }
        }
    }

    val isActivePress = isPressed || isTapFlashing
    val targetCardBg = when {
        isActivePress && colors.isDark -> Color(0xFF28374E)
        isActivePress -> colors.closeButtonBg
        else -> colors.inputBg
    }
    val cardBg by animateColorAsState(
        targetValue = targetCardBg,
        animationSpec = tween(durationMillis = if (isActivePress) 35 else 180),
        label = "cameraCardBg"
    )
    val pressScale by animateFloatAsState(
        targetValue = if (isActivePress) 0.98f else 1f,
        animationSpec = tween(durationMillis = if (isActivePress) 45 else 160),
        label = "cameraCardScale"
    )
    val thumbBg = colors.thumbnailBg
    val noImageTint = colors.textPlaceholder.copy(alpha = 0.45f)
    val nameColor = colors.textTitle

    var lastLoadedCacheKey by remember(camera.id) { mutableStateOf<String?>(null) }
    var isImageLoading by remember(snapshotUrl) {
        mutableStateOf(!snapshotUrl.isNullOrEmpty() && lastLoadedCacheKey == null)
    }
    var isImageError by remember(snapshotUrl) { mutableStateOf(false) }

    val targetMemKey = remember(snapshotUrl) {
        if (snapshotUrl.isNullOrEmpty()) null else "${snapshotUrl}_${SNAPSHOT_CARD_WIDTH_PX}x${SNAPSHOT_CARD_HEIGHT_PX}"
    }
    val imageRequest = remember(snapshotUrl, lastLoadedCacheKey) {
        if (snapshotUrl.isNullOrEmpty()) {
            null
        } else {
            ImageRequest.Builder(context)
                .data(snapshotUrl)
                .size(SNAPSHOT_CARD_WIDTH_PX, SNAPSHOT_CARD_HEIGHT_PX)
                .precision(Precision.INEXACT)
                .allowHardware(true)
                .allowRgb565(true)
                .crossfade(false)
                .memoryCacheKey(targetMemKey)
                .diskCacheKey(snapshotUrl)
                .placeholderMemoryCacheKey(lastLoadedCacheKey)
                .build()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clip(RoundedCornerShape(spacing.radiusSmallMedium))
            .background(cardBg)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = handleCardClick
            )
    ) {
        // .cam-card-thumb (16:9) - Dùng AsyncImage trực tiếp thay vì SubcomposeAsyncImage để cuộn lưới mượt 60/120fps
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(LIVE_STAGE_ASPECT_RATIO)
                .background(thumbBg),
            contentAlignment = Alignment.Center
        ) {
            if (imageRequest != null && !isImageError) {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = camera.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    onSuccess = {
                        isImageLoading = false
                        isImageError = false
                        lastLoadedCacheKey = targetMemKey
                    },
                    onError = {
                        isImageLoading = false
                        if (lastLoadedCacheKey == null) {
                            isImageError = true
                        }
                        onSnapshotError()
                    }
                )
                if (isImageLoading && lastLoadedCacheKey == null && isTabActive) {
                    WaveDotsLoading(
                        dotSize = spacing.space6,
                        dotColor = colors.accent,
                        maxOffset = spacing.space6
                    )
                }
            } else if (!hasSnapKey && isTabActive) {
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
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PlayingEqualizerBars(isTabActive: Boolean = true) {
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.appColors
    if (!isTabActive) {
        Row(
            modifier = Modifier.height(spacing.space10),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(spacing.space2)
        ) {
            Box(
                modifier = Modifier
                    .width(spacing.space2)
                    .height(spacing.space4)
                    .background(colors.onAccent, RoundedCornerShape(spacing.space1))
            )
            Box(
                modifier = Modifier
                    .width(spacing.space2)
                    .height(spacing.space8)
                    .background(colors.onAccent, RoundedCornerShape(spacing.space1))
            )
            Box(
                modifier = Modifier
                    .width(spacing.space2)
                    .height(spacing.space6)
                    .background(colors.onAccent, RoundedCornerShape(spacing.space1))
            )
        }
        return
    }
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
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = spacing.contentMaxWidth)
                    .fillMaxWidth()
                    .heightIn(max = GroupFilterSheetMaxHeight)
                    .clip(RoundedCornerShape(topStart = spacing.radiusLarge, topEnd = spacing.radiusLarge))
                    .background(colors.surfaceGrouped)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .navigationBarsPadding()
                    .padding(horizontal = spacing.space16)
                    .padding(top = spacing.space10, bottom = spacing.space20)
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
                            .background(colors.closeButtonBg)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = LucideIcons.X,
                            contentDescription = if (isEn) "Close" else "Đóng",
                            tint = colors.textSubtle,
                            modifier = Modifier.size(spacing.iconSmall)
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
                        .padding(bottom = spacing.space16)
                )

                // Sheet Body
                if (rows.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(GroupFilterEmptyHeight)
                            .clip(RoundedCornerShape(spacing.radiusRegular))
                            .background(colors.surfaceRaised),
                        contentAlignment = Alignment.Center
                    ) {
                        CommonEmptyState(text = if (isEn) "No camera groups" else "Không có nhóm camera")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(spacing.radiusRegular))
                            .background(colors.surfaceRaised)
                            .padding(vertical = spacing.space4),
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
                                    imageVector = LucideIcons.Folder,
                                    contentDescription = null,
                                    tint = allIconTint,
                                    modifier = Modifier.size(spacing.iconSmall)
                                )

                                Text(
                                    text = if (isEn) "All cameras" else "Tất cả camera",
                                    color = if (isAllSelected) colors.accent else colors.textStrong,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isAllSelected) FontWeight.SemiBold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )

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
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected || row.isVmsNode) FontWeight.SemiBold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )

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
    showFullscreenButton: Boolean = true,
    showCloseButton: Boolean = true
) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val deviceKey = UserSession.accessToken
    var isLiveViewPlaying by remember(selectedCamera.camera.id, selectedCamera.streamUrl, isTabActive) { mutableStateOf(false) }
    var isLiveViewError by remember(selectedCamera.camera.id, selectedCamera.streamUrl, isTabActive) { mutableStateOf(false) }
    var snapshotFailed by remember(selectedCamera.camera.id, selectedCamera.fallbackImageUrl) { mutableStateOf(false) }
    val hasError = isLiveViewError || selectedCamera.streamUrl.startsWith("error://")

    // Mặc định ẩn toàn bộ overlay (tên camera, nút đóng, nút phóng to).
    // Chạm vào màn hình live: đang ẩn -> hiện lên và tự ẩn sau 3 giây; đang hiện -> chạm vùng trống sẽ ẩn ngay.
    var showInfo by remember(selectedCamera.camera.id) { mutableStateOf(false) }
    var autoHideToken by remember(selectedCamera.camera.id) { mutableIntStateOf(0) }
    LaunchedEffect(showInfo, autoHideToken) {
        if (showInfo) {
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
                        if (showInfo) {
                            showInfo = false
                        } else {
                            showInfo = true
                            autoHideToken++
                        }
                    },
                    onDoubleTap = {
                        if (!isCompact && showFullscreenButton) {
                            showInfo = false
                            onFullscreenClick()
                        } else {
                            onClick()
                            showInfo = !showInfo
                            if (showInfo) autoHideToken++
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
        val context = LocalContext.current
        val fallbackUrl = selectedCamera.fallbackImageUrl
        var lastFallbackCacheKey by remember(selectedCamera.camera.id) { mutableStateOf<String?>(null) }
        val targetFallbackMemKey = remember(fallbackUrl) {
            if (fallbackUrl.isEmpty()) null else "${fallbackUrl}_${LIVE_STAGE_SNAPSHOT_WIDTH_PX}x${LIVE_STAGE_SNAPSHOT_HEIGHT_PX}"
        }
        val fallbackImageRequest = remember(fallbackUrl, lastFallbackCacheKey) {
            if (fallbackUrl.isEmpty()) {
                null
            } else {
                ImageRequest.Builder(context)
                    .data(fallbackUrl)
                    .size(LIVE_STAGE_SNAPSHOT_WIDTH_PX, LIVE_STAGE_SNAPSHOT_HEIGHT_PX)
                    .precision(Precision.INEXACT)
                    .allowHardware(true)
                    .crossfade(false)
                    .memoryCacheKey(targetFallbackMemKey)
                    .diskCacheKey(fallbackUrl)
                    .placeholderMemoryCacheKey(lastFallbackCacheKey)
                    .build()
            }
        }
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
                if (fallbackImageRequest != null && !snapshotFailed) {
                    AsyncImage(
                        model = fallbackImageRequest,
                        contentDescription = selectedCamera.camera.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        onSuccess = {
                            lastFallbackCacheKey = targetFallbackMemKey
                        },
                        onError = {
                            if (lastFallbackCacheKey == null) {
                                snapshotFailed = true
                            }
                            onSnapshotError()
                        }
                    )
                }
                if (isTabActive) {
                    WaveDotsLoading(dotColor = colors.accent)
                }
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
                        showInfo = true
                        autoHideToken++
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

        // Top-Left Camera Name Pill (.info-name), Top-Right Close Button (.info-action),
        // & Bottom-Right Fullscreen Button (.fullscreen-btn) — mặc định ẩn, chạm hiện 3s hoặc chạm vùng trống ẩn ngay
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
                if (!isFullscreen && showCloseButton) {
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

                // Bottom-Right Fullscreen Button (.fullscreen-btn)
                if (showFullscreenButton && (isLiveViewPlaying || isFullscreen)) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(if (isCompact) spacing.space4 else spacing.space6)
                            .size(if (isFullscreen) spacing.iconLarge else spacing.iconButtonSmall)
                            .clip(RoundedCornerShape(spacing.radiusSmall))
                            .background(overlayPillBg)
                            .border(spacing.borderMedium, overlayPillBorder, RoundedCornerShape(spacing.radiusSmall))
                            .clickable {
                                showInfo = false
                                onFullscreenClick()
                            },
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
