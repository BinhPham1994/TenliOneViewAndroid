package com.tenli.oneview.ui.features.monitor

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.foundation.lazy.grid.LazyGridState
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
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
import androidx.compose.ui.zIndex
import kotlin.math.abs
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
import com.tenli.oneview.data.local.MonitorCacheManager
import com.tenli.oneview.data.local.UserPermissionManager
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.model.network.CameraModel
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.model.network.VideoModel
import com.tenli.oneview.ui.component.CommonEmptyState
import com.tenli.oneview.ui.component.IconBadge
import com.tenli.oneview.ui.component.LiveStreamPlayer
import com.tenli.oneview.ui.component.MobileEventCard
import com.tenli.oneview.ui.component.MobileFilterBottomSheet
import com.tenli.oneview.ui.component.MobileFullScreenOverlay
import com.tenli.oneview.ui.component.MobileTopActionButton
import com.tenli.oneview.ui.component.MobileTopBar
import com.tenli.oneview.ui.component.MobileTopPopupManager
import com.tenli.oneview.ui.component.VideoPlayer
import com.tenli.oneview.ui.component.WaveDotsLoading
import com.tenli.oneview.ui.component.rememberIsEnglish
import com.tenli.oneview.ui.features.event.parseVideoQueryTime
import com.tenli.oneview.ui.features.event.uniqueKey
import com.tenli.oneview.ui.features.home.TimeFilter
import com.tenli.oneview.ui.features.main.CameraDetailBottomNavigation
import com.tenli.oneview.ui.features.main.CameraDetailTab
import com.tenli.oneview.ui.theme.BrandActiveNavDark
import com.tenli.oneview.ui.theme.BrandLight
import com.tenli.oneview.ui.theme.LucideIcons
import com.tenli.oneview.ui.theme.appColors
import com.tenli.oneview.ui.theme.caption
import com.tenli.oneview.ui.theme.micro
import com.tenli.oneview.ui.theme.spacing
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

private class CacheKeyHolder(var key: String? = null)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitorScreen(
    viewModel: MonitorViewModel = viewModel(factory = MonitorViewModel.Factory),
    gridState: LazyGridState = rememberLazyGridState(),
    isTabActive: Boolean = true,
    onEventClick: (Int) -> Unit = {},
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
    val cameraGridState = gridState
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing

    val activeLiveCamera = uiState.selectedCameras.getOrNull(0)
    val isLiveScreenOpen = activeLiveCamera != null

    val onCardSnapshotError: (CameraModel) -> Unit = remember(viewModel) {
        { cam -> viewModel.onCardSnapshotError(cam) }
    }
    val onCardClick: (CameraModel) -> Unit = remember(viewModel, dismissKeyboard) {
        { cam ->
            dismissKeyboard()
            viewModel.openLiveCamera(cam)
        }
    }

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
        val errMsg = uiState.error ?: return@LaunchedEffect
        if (isTabActive) {
            MobileTopPopupManager.error(errMsg)
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

    // Khi màn hình Live đã trượt vào che kín 100% màn hình (sau 280ms), tạm ngưng place lưới camera bên dưới
    // để GPU không phải vẽ đè (overdraw) danh sách thẻ camera ở mỗi frame phát video 30/60fps.
    // Ngay ở Frame 1 khi bấm Back (isLiveScreenOpen = false), lưới camera được place lại tức thì (0ms, không cần measure lại).
    var isCoveredByLiveOverlay by remember { mutableStateOf(false) }
    LaunchedEffect(isLiveScreenOpen) {
        if (isLiveScreenOpen) {
            delay(LIVE_PAGE_ENTER_DURATION_MS.toLong() + 16L)
            isCoveredByLiveOverlay = true
        } else {
            isCoveredByLiveOverlay = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                layout(placeable.width, placeable.height) {
                    if (!isCoveredByLiveOverlay || !isLiveScreenOpen) {
                        placeable.placeRelative(0, 0)
                    }
                }
            }
    ) {
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
                                    onSnapshotError = onCardSnapshotError,
                                    onClick = onCardClick
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
    onLiveScreenOpenChange: (Boolean) -> Unit = {},
    onEventClick: (Int) -> Unit = {}
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

    val canUseDetailFullscreen = uiState.cameraDetailTab == CameraDetailTab.LIVE ||
        (uiState.cameraDetailTab == CameraDetailTab.PLAYBACK && uiState.selectedPlaybackVideo != null)
    val isSingleFullscreen =
        fullscreenIndex == 0 && isLiveScreenOpen && canUseDetailFullscreen

    LaunchedEffect(isTabActive, canUseDetailFullscreen) {
        if ((!isTabActive || !canUseDetailFullscreen) && fullscreenIndex != null) {
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

    val handleCameraDetailBack: () -> Unit = {
        when {
            isSingleFullscreen -> {
                restoreSystemOrientationAndBars(activity)
                fullscreenIndex = null
            }
            uiState.cameraDetailTab != CameraDetailTab.LIVE -> {
                viewModel.selectCameraDetailTab(CameraDetailTab.LIVE)
            }
            else -> {
                viewModel.closeLiveCamera()
            }
        }
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
        BackHandler(onBack = handleCameraDetailBack)
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
        val isClosing = !isLiveScreenOpen
        val currentCam = activeLiveCamera ?: displayedLiveCamera
        if (currentCam != null) {
            MobileLiveCameraScreen(
                uiState = uiState,
                selectedCamera = currentCam,
                isTabActive = isTabActive && isLiveScreenOpen,
                isFullscreen = isSingleFullscreen,
                isSliding = isSliding,
                isClosing = isClosing,
                onBackClick = handleCameraDetailBack,
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
                onSwitchChannel = { channel -> viewModel.switchLiveChannel(channel) },
                onSelectDetailTab = { tab -> viewModel.selectCameraDetailTab(tab) },
                onApplyPlaybackFilters = { filter, channel -> viewModel.applyPlaybackFilters(filter, channel) },
                onResetPlaybackFilters = { viewModel.resetPlaybackFilters() },
                onSelectPlaybackVideo = { video -> viewModel.selectPlaybackVideo(video) },
                onLoadMorePlaybackForDay = { dateKey -> viewModel.loadMorePlaybackForDay(dateKey) },
                onLoadMorePlaybackDays = { viewModel.loadMorePlaybackDays() },
                onApplyEventFilters = { filter, aiType -> viewModel.applyCameraEventFilters(filter, aiType) },
                onResetEventFilters = { viewModel.resetCameraEventFilters() },
                onRefreshEvents = { viewModel.refreshCameraEvents() },
                onLoadMoreEvents = { viewModel.loadMoreCameraEvents() },
                onEventItemClick = { event ->
                    viewModel.cacheClickedCameraEvent(event)
                    onEventClick(event.id)
                }
            )
        }
    }
}

private const val CAMERA_EVENT_LOAD_MORE_THRESHOLD = 5
private const val CAMERA_EVENT_AUTO_FILL_MAX = 12

private val OrderedCameraDetailTabs = listOf(
    CameraDetailTab.LIVE,
    CameraDetailTab.PLAYBACK,
    CameraDetailTab.EVENTS
)

private const val DETAIL_TAB_SWITCH_DURATION_MS = 250
private val DetailTabSwitchEasing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
private val DetailTabSwitchAnimSpec = tween<Float>(durationMillis = DETAIL_TAB_SWITCH_DURATION_MS, easing = DetailTabSwitchEasing)

private val DetailSwipeEdgeBuffer = 20.dp
private val DetailSwipeBottomNavExclusion = 96.dp
private val DetailSwipeDirectionLockThreshold = 10.dp
private val DetailSwipeMinFlickDistance = 30.dp
private val DetailSwipeMinDragDistance = 45.dp
private val DetailSwipeLongDragDistance = 90.dp
private const val DETAIL_SWIPE_MIN_FLICK_VELOCITY_DP_PER_MS = 0.28f
private const val DETAIL_SWIPE_MAX_DRAG_DURATION_MS = 700L
private const val DETAIL_SWIPE_HORIZONTAL_DOMINANCE = 1.25f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MobileLiveCameraScreen(
    uiState: MonitorUiState,
    selectedCamera: SelectedCamera,
    isTabActive: Boolean,
    isFullscreen: Boolean,
    isSliding: Boolean = false,
    isClosing: Boolean = false,
    onBackClick: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onRetryClick: () -> Unit,
    onSuccess: () -> Unit,
    onSnapshotError: () -> Unit,
    onReloadClick: () -> Unit,
    onSwitchChannel: (Int) -> Unit,
    onSelectDetailTab: (CameraDetailTab) -> Unit,
    onApplyPlaybackFilters: (TimeFilter, String) -> Unit,
    onResetPlaybackFilters: () -> Unit,
    onSelectPlaybackVideo: (VideoModel?) -> Unit,
    onLoadMorePlaybackForDay: (String) -> Unit,
    onLoadMorePlaybackDays: () -> Unit,
    onApplyEventFilters: (TimeFilter, String?) -> Unit,
    onResetEventFilters: () -> Unit,
    onRefreshEvents: () -> Unit,
    onLoadMoreEvents: () -> Unit,
    onEventItemClick: (EventData) -> Unit
) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val edgeBorderColor = colors.border
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val permissionState by UserPermissionManager.permissionState.collectAsStateWithLifecycle()

    val visibleDetailTabs = remember(permissionState.canAccessPlayback, permissionState.canAccessEvents) {
        buildList {
            add(CameraDetailTab.LIVE)
            if (permissionState.canAccessPlayback) add(CameraDetailTab.PLAYBACK)
            if (permissionState.canAccessEvents) add(CameraDetailTab.EVENTS)
        }
    }
    val visibleDetailTabsState by rememberUpdatedState(visibleDetailTabs)

    LaunchedEffect(visibleDetailTabs, uiState.cameraDetailTab) {
        if (uiState.cameraDetailTab !in visibleDetailTabs) {
            onSelectDetailTab(CameraDetailTab.LIVE)
        }
    }

    val playbackListState = rememberLazyListState()
    val eventListState = rememberLazyListState()

    var showPlaybackFilterSheet by remember { mutableStateOf(false) }
    var showEventFilterSheet by remember { mutableStateOf(false) }

    val bottomClearance = spacing.bottomNavHeight + spacing.space28

    // Keep-Alive + Simultaneous Edge-to-Edge Slide Transition cho 3 tab chi tiết camera (Trực tiếp / Xem lại / Sự kiện)
    var mountedDetailTabs by remember(selectedCamera.camera.id) { mutableStateOf(setOf(uiState.cameraDetailTab)) }
    var activeDetailTab by remember(selectedCamera.camera.id) { mutableStateOf(uiState.cameraDetailTab) }
    var outgoingDetailTab by remember(selectedCamera.camera.id) { mutableStateOf<CameraDetailTab?>(null) }
    var detailSlideDirection by remember(selectedCamera.camera.id) { mutableIntStateOf(1) }
    val detailTransitionProgress = remember(selectedCamera.camera.id) { Animatable(1f) }

    LaunchedEffect(uiState.cameraDetailTab) {
        val targetTab = uiState.cameraDetailTab
        if (targetTab !in mountedDetailTabs) {
            mountedDetailTabs = mountedDetailTabs + targetTab
        }
        if (targetTab == activeDetailTab) {
            detailTransitionProgress.animateTo(targetValue = 1f, animationSpec = DetailTabSwitchAnimSpec)
            outgoingDetailTab = null
            return@LaunchedEffect
        }
        val fromIdx = OrderedCameraDetailTabs.indexOf(activeDetailTab)
        val toIdx = OrderedCameraDetailTabs.indexOf(targetTab)
        detailSlideDirection = if (toIdx >= fromIdx) 1 else -1
        outgoingDetailTab = activeDetailTab
        activeDetailTab = targetTab
        detailTransitionProgress.snapTo(0f)
        detailTransitionProgress.animateTo(targetValue = 1f, animationSpec = DetailTabSwitchAnimSpec)
        outgoingDetailTab = null
    }

    val currentDetailTabState by rememberUpdatedState(uiState.cameraDetailTab)
    val isFullscreenState by rememberUpdatedState(isFullscreen)
    val isFilterSheetOpenState by rememberUpdatedState(showPlaybackFilterSheet || showEventFilterSheet)
    val currentOnSelectDetailTab by rememberUpdatedState(onSelectDetailTab)

    val switchDetailTabBySwipe: (CameraDetailTab) -> Unit = remember(haptic) {
        { targetTab: CameraDetailTab ->
            if (targetTab != currentDetailTabState) {
                try {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                } catch (_: Exception) {
                }
                currentOnSelectDetailTab(targetTab)
            }
        }
    }

    Box(
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
            .pointerInput(Unit) {
                val edgeBufferPx = DetailSwipeEdgeBuffer.toPx()
                val bottomNavExclusionPx = DetailSwipeBottomNavExclusion.toPx()
                val lockThresholdPx = DetailSwipeDirectionLockThreshold.toPx()
                val minFlickDistPx = DetailSwipeMinFlickDistance.toPx()
                val minDragDistPx = DetailSwipeMinDragDistance.toPx()
                val longDragDistPx = DetailSwipeLongDragDistance.toPx()

                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    val widthPx = size.width.toFloat()
                    val heightPx = size.height.toFloat()
                    val startX = down.position.x
                    val startY = down.position.y

                    if (isFullscreenState ||
                        isFilterSheetOpenState ||
                        startX < edgeBufferPx ||
                        startX > widthPx - edgeBufferPx ||
                        startY > heightPx - bottomNavExclusionPx
                    ) {
                        return@awaitEachGesture
                    }

                    val startTime = down.uptimeMillis
                    var endTime = startTime
                    var gestureDirection = 0 // 0 = undecided, 1 = horizontal, 2 = vertical
                    var lastX = startX
                    var lastY = startY

                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        endTime = change.uptimeMillis

                        if (change.changedToUpIgnoreConsumed()) {
                            lastX = change.position.x
                            lastY = change.position.y
                            if (gestureDirection == 1) {
                                change.consume()
                            }
                            break
                        }

                        if (!change.pressed) break

                        lastX = change.position.x
                        lastY = change.position.y
                        val dx = lastX - startX
                        val dy = lastY - startY

                        if (gestureDirection == 0) {
                            if (abs(dx) >= lockThresholdPx || abs(dy) >= lockThresholdPx) {
                                gestureDirection = if (abs(dy) >= abs(dx)) 2 else 1
                            }
                        }

                        if (gestureDirection == 2) {
                            break
                        } else if (gestureDirection == 1) {
                            change.consume()
                        }
                    }

                    if (gestureDirection == 1) {
                        val diffX = lastX - startX
                        val diffY = lastY - startY
                        val absX = abs(diffX)
                        val absY = abs(diffY)
                        val elapsed = (endTime - startTime).coerceAtLeast(1L)
                        val velocityDpPerMs = absX.toDp().value / elapsed.toFloat()

                        val isFlick = velocityDpPerMs > DETAIL_SWIPE_MIN_FLICK_VELOCITY_DP_PER_MS && absX > minFlickDistPx
                        val isDrag = absX >= minDragDistPx && elapsed <= DETAIL_SWIPE_MAX_DRAG_DURATION_MS
                        val isLongDrag = absX >= longDragDistPx

                        if ((isFlick || isDrag || isLongDrag) && absX >= absY * DETAIL_SWIPE_HORIZONTAL_DOMINANCE) {
                            val currentVisibleTabs = visibleDetailTabsState
                            val currIdx = currentVisibleTabs.indexOf(currentDetailTabState)
                            if (currIdx != -1) {
                                if (diffX < 0f && currIdx < currentVisibleTabs.size - 1) {
                                    switchDetailTabBySwipe(currentVisibleTabs[currIdx + 1])
                                } else if (diffX > 0f && currIdx > 0) {
                                    switchDetailTabBySwipe(currentVisibleTabs[currIdx - 1])
                                }
                            }
                        }
                    }
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                    rightAction = when (uiState.cameraDetailTab) {
                        CameraDetailTab.LIVE -> null
                        CameraDetailTab.PLAYBACK -> {
                            {
                                MobileTopActionButton(
                                    icon = LucideIcons.ListFilter,
                                    contentDescription = stringResource(id = R.string.filter_title),
                                    hasActiveDot = uiState.hasActivePlaybackFilter,
                                    onClick = { showPlaybackFilterSheet = true }
                                )
                            }
                        }
                        CameraDetailTab.EVENTS -> {
                            {
                                MobileTopActionButton(
                                    icon = LucideIcons.ListFilter,
                                    contentDescription = stringResource(id = R.string.filter_title),
                                    hasActiveDot = uiState.hasActiveCameraEventFilter,
                                    onClick = { showEventFilterSheet = true }
                                )
                            }
                        }
                    }
                )
            }

            val effectiveMountedDetailTabs = remember(
                mountedDetailTabs,
                uiState.cameraDetailTab,
                activeDetailTab,
                outgoingDetailTab
            ) {
                buildSet {
                    addAll(mountedDetailTabs)
                    add(uiState.cameraDetailTab)
                    add(activeDetailTab)
                    outgoingDetailTab?.let { add(it) }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                OrderedCameraDetailTabs.forEach { tab ->
                    if (tab in effectiveMountedDetailTabs) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .zIndex(
                                    when (tab) {
                                        activeDetailTab -> 2f
                                        outgoingDetailTab -> 1f
                                        else -> 0f
                                    }
                                )
                                .layout { measurable, constraints ->
                                    val currentOutgoing = outgoingDetailTab
                                    if (tab != activeDetailTab && tab != currentOutgoing) {
                                        layout(0, 0) {}
                                    } else {
                                        val placeable = measurable.measure(constraints)
                                        layout(placeable.width, placeable.height) {
                                            val progress = detailTransitionProgress.value
                                            val offsetX = when (tab) {
                                                activeDetailTab -> if (currentOutgoing != null) {
                                                    (detailSlideDirection * (1f - progress) * placeable.width).toInt()
                                                } else {
                                                    0
                                                }
                                                else -> (-detailSlideDirection * progress * placeable.width).toInt()
                                            }
                                            placeable.placeRelativeWithLayer(offsetX, 0)
                                        }
                                    }
                                }
                        ) {
                            when (tab) {
                                CameraDetailTab.LIVE -> {
                                    val isEnteringLivePage = isSliding && !isClosing
                                    val isClosingOrLeavingLive = isClosing ||
                                        (outgoingDetailTab == CameraDetailTab.LIVE && activeDetailTab != CameraDetailTab.LIVE)
                                    Column(modifier = Modifier.fillMaxSize()) {
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
                                                isTabActive = isTabActive && uiState.cameraDetailTab == CameraDetailTab.LIVE,
                                                isEntering = isEnteringLivePage,
                                                isClosing = isClosingOrLeavingLive,
                                                onClick = {},
                                                onRetryClick = onRetryClick,
                                                onSuccess = onSuccess,
                                                onSnapshotError = onSnapshotError,
                                                onFullscreenClick = onToggleFullscreen,
                                                onBackToLiveClick = onReloadClick,
                                                onSwitchChannel = onSwitchChannel,
                                                onRemoveClick = onBackClick,
                                                isFullscreen = isFullscreen,
                                                isCompact = false,
                                                showFullscreenButton = true,
                                                showCloseButton = false
                                            )
                                        }
                                        if (!isFullscreen) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }

                                CameraDetailTab.PLAYBACK -> {
                                    val isPlaybackTabVisible = isTabActive &&
                                        (activeDetailTab == CameraDetailTab.PLAYBACK || outgoingDetailTab == CameraDetailTab.PLAYBACK)
                                    CameraPlaybackTabContent(
                                        uiState = uiState,
                                        selectedCamera = selectedCamera,
                                        isTabVisible = isPlaybackTabVisible,
                                        isFullscreen = isFullscreen && uiState.cameraDetailTab == CameraDetailTab.PLAYBACK,
                                        listState = playbackListState,
                                        bottomClearance = bottomClearance,
                                        onToggleFullscreen = onToggleFullscreen,
                                        onSelectVideo = onSelectPlaybackVideo,
                                        onLoadMoreForDay = onLoadMorePlaybackForDay,
                                        onLoadMoreDays = onLoadMorePlaybackDays,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                CameraDetailTab.EVENTS -> {
                                    val isEventsTabVisible = isTabActive &&
                                        (activeDetailTab == CameraDetailTab.EVENTS || outgoingDetailTab == CameraDetailTab.EVENTS)
                                    CameraEventsTabContent(
                                        uiState = uiState,
                                        cameraName = selectedCamera.camera.name,
                                        isTabVisible = isEventsTabVisible,
                                        listState = eventListState,
                                        bottomClearance = bottomClearance,
                                        onRefresh = onRefreshEvents,
                                        onLoadMore = onLoadMoreEvents,
                                        onEventClick = onEventItemClick,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Menu bottom 3 tính năng của Camera: Trực tiếp | Xem lại | Sự kiện
        if (!isFullscreen && visibleDetailTabs.size > 1) {
            Box(
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                CameraDetailBottomNavigation(
                    currentTab = uiState.cameraDetailTab,
                    onTabClick = onSelectDetailTab,
                    onScrollToTop = { tab ->
                        scope.launch {
                            when (tab) {
                                CameraDetailTab.PLAYBACK -> playbackListState.animateScrollToItem(0)
                                CameraDetailTab.EVENTS -> eventListState.animateScrollToItem(0)
                                CameraDetailTab.LIVE -> Unit
                            }
                        }
                    },
                    visibleTabs = visibleDetailTabs
                )
            }
        }

        if (showPlaybackFilterSheet) {
            MobileFilterBottomSheet(
                showScope = false,
                showTime = true,
                selectedTimeFilter = uiState.playbackTimeFilter,
                showPlaybackChannel = true,
                selectedPlaybackChannel = uiState.playbackChannel,
                defaultPlaybackChannel = uiState.defaultPlaybackChannel,
                onApplyPlayback = { timeFilter, channel ->
                    onApplyPlaybackFilters(timeFilter, channel)
                },
                showCamera = false,
                showEventType = false,
                onReset = onResetPlaybackFilters,
                onDismiss = { showPlaybackFilterSheet = false }
            )
        }

        if (showEventFilterSheet) {
            MobileFilterBottomSheet(
                showScope = false,
                showTime = true,
                showCamera = false,
                showEventType = true,
                selectedTimeFilter = uiState.cameraEventTimeFilter,
                selectedAiType = uiState.cameraEventAiType,
                onApply = { _, timeFilter, _, aiType ->
                    onApplyEventFilters(timeFilter, aiType)
                },
                onReset = onResetEventFilters,
                onDismiss = { showEventFilterSheet = false }
            )
        }
    }
}

@Composable
private fun CameraPlaybackTabContent(
    uiState: MonitorUiState,
    selectedCamera: SelectedCamera,
    isTabVisible: Boolean,
    isFullscreen: Boolean,
    listState: LazyListState,
    bottomClearance: androidx.compose.ui.unit.Dp,
    onToggleFullscreen: () -> Unit,
    onSelectVideo: (VideoModel?) -> Unit,
    onLoadMoreForDay: (String) -> Unit,
    onLoadMoreDays: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val isEn = rememberIsEnglish()

    val ratioPrefs = remember(context) {
        context.getSharedPreferences("tenli_camera_prefs", Context.MODE_PRIVATE)
    }
    var isOriginalRatio by remember(selectedCamera.camera.id) {
        mutableStateOf(
            ratioPrefs.getString("camera_fit_ratio_${selectedCamera.camera.id}", "cover") == "contain"
        )
    }
    val toggleAspectRatio: () -> Unit = remember(selectedCamera.camera.id, ratioPrefs) {
        {
            val next = !isOriginalRatio
            isOriginalRatio = next
            ratioPrefs.edit()
                .putString("camera_fit_ratio_${selectedCamera.camera.id}", if (next) "contain" else "cover")
                .apply()
        }
    }

    val dayHeaderShape = remember(spacing.radiusRegular) {
        RoundedCornerShape(topStart = spacing.radiusRegular, topEnd = spacing.radiusRegular)
    }
    val dayFooterShape = remember(spacing.radiusRegular) {
        RoundedCornerShape(bottomStart = spacing.radiusRegular, bottomEnd = spacing.radiusRegular)
    }

    val shouldAutoLoadMore by remember(listState) {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 2 && lastVisibleIndex >= totalItems - 2
        }
    }

    LaunchedEffect(
        shouldAutoLoadMore,
        uiState.playbackDayGroups,
        uiState.hasMorePlaybackDays,
        uiState.isLoadingPlaybacks,
        uiState.isLoadingMorePlaybackDays
    ) {
        if (!shouldAutoLoadMore || uiState.isLoadingPlaybacks) return@LaunchedEffect
        val groups = uiState.playbackDayGroups
        if (groups.size == 1 && !uiState.hasMorePlaybackDays) {
            val onlyGroup = groups[0]
            if (onlyGroup.hasMore && !onlyGroup.isLoadingMore) {
                onLoadMoreForDay(onlyGroup.dateKey)
            }
        } else if (uiState.hasMorePlaybackDays && !uiState.isLoadingMorePlaybackDays) {
            val lastGroup = groups.lastOrNull()
            if (lastGroup != null && !lastGroup.hasMore) {
                onLoadMoreDays()
            }
        }
    }

    Column(modifier = modifier) {
        // Trình phát video ghi hình VMS tràn viền ngang (hỗ trợ xoay ngang toàn màn hình giống Live Camera)
        val activeVideo = uiState.selectedPlaybackVideo
        if (activeVideo != null) {
            val activeDateTimeText = activeVideo.displayDateTime.ifEmpty {
                formatPlaybackDateTime(activeVideo.time)
            }
            var isPlaybackPlaying by remember(activeVideo.videoLink) { mutableStateOf(false) }
            var isPlaybackError by remember(activeVideo.videoLink) { mutableStateOf(false) }
            var isControllerVisible by remember(activeVideo.videoLink) { mutableStateOf(true) }

            val overlayPillBg = colors.mediaBg.copy(alpha = 0.42f)
            val overlayPillBorder = colors.onMedia.copy(alpha = 0.12f)
            val pillHeight = spacing.iconButtonSmall
            val actionBtnSize = spacing.iconMedium
            val actionIconSize = spacing.iconSmall

            Box(
                modifier = if (isFullscreen) {
                    Modifier
                        .fillMaxSize()
                        .background(colors.mediaBg)
                } else {
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = spacing.space6)
                        .aspectRatio(LIVE_STAGE_ASPECT_RATIO)
                        .background(colors.surface)
                }
            ) {
                VideoPlayer(
                    videoUrl = activeVideo.videoLink,
                    thumbnailUrl = null,
                    deviceKey = UserSession.accessToken,
                    isOriginalRatio = isOriginalRatio,
                    onPlayingChange = { isPlaybackPlaying = it },
                    onErrorChange = { isPlaybackError = it },
                    onControllerVisibilityChange = { isControllerVisible = it }
                )

                androidx.compose.animation.AnimatedVisibility(
                    visible = !isPlaybackPlaying || isPlaybackError || isControllerVisible,
                    enter = fadeIn(animationSpec = tween(200)),
                    exit = fadeOut(animationSpec = tween(200)),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth()
                                .padding(spacing.space8),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            // Top-Left: Thời gian bản ghi (.info-name style)
                            Row(
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .padding(end = spacing.space8)
                                    .height(pillHeight)
                                    .clip(RoundedCornerShape(spacing.radiusSmall))
                                    .background(overlayPillBg)
                                    .border(spacing.borderMedium, overlayPillBorder, RoundedCornerShape(spacing.radiusSmall))
                                    .padding(horizontal = spacing.space8),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(spacing.space6)
                            ) {
                                Icon(
                                    imageVector = LucideIcons.Clock,
                                    contentDescription = null,
                                    tint = colors.onMedia,
                                    modifier = Modifier.size(actionIconSize)
                                )
                                Text(
                                    text = activeDateTimeText,
                                    color = colors.onMedia,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Top-Right: Tỷ lệ khung hình, Toàn màn hình & Đóng (.info-action style)
                            Row(
                                modifier = Modifier
                                    .height(pillHeight)
                                    .clip(RoundedCornerShape(spacing.radiusSmall))
                                    .background(overlayPillBg)
                                    .border(spacing.borderMedium, overlayPillBorder, RoundedCornerShape(spacing.radiusSmall))
                                    .padding(horizontal = spacing.space2),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(spacing.space2)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(actionBtnSize)
                                        .clip(RoundedCornerShape(spacing.radiusTiny))
                                        .background(
                                            if (isOriginalRatio) {
                                                BrandActiveNavDark.copy(alpha = 0.35f)
                                            } else {
                                                Color.Transparent
                                            }
                                        )
                                        .clickable { toggleAspectRatio() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isOriginalRatio) LucideIcons.StretchHorizontal else LucideIcons.Proportions,
                                        contentDescription = if (isOriginalRatio) {
                                            if (isEn) "Fill 16:9 frame" else "Lấp đầy khung 16:9"
                                        } else {
                                            if (isEn) "Original aspect ratio" else "Tỷ lệ khung hình gốc"
                                        },
                                        tint = if (isOriginalRatio) BrandLight else colors.onMedia,
                                        modifier = Modifier.size(actionIconSize)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(actionBtnSize)
                                        .clip(RoundedCornerShape(spacing.radiusTiny))
                                        .clickable { onToggleFullscreen() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isFullscreen) LucideIcons.Minimize else LucideIcons.Maximize,
                                        contentDescription = if (isEn) "Fullscreen" else "Toàn màn hình",
                                        tint = colors.onMedia,
                                        modifier = Modifier.size(actionIconSize)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(actionBtnSize)
                                        .clip(RoundedCornerShape(spacing.radiusTiny))
                                        .clickable {
                                            if (isFullscreen) {
                                                onToggleFullscreen()
                                            }
                                            onSelectVideo(null)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = LucideIcons.X,
                                        contentDescription = stringResource(id = R.string.dialog_btn_close),
                                        tint = colors.onMedia,
                                        modifier = Modifier.size(actionIconSize)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!isFullscreen) {
            // Danh sách file ghi hình từ VMS nhóm theo ngày (đồng bộ PlaybackPage.vue)
            if (uiState.isLoadingPlaybacks && uiState.playbackDayGroups.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = bottomClearance),
                    contentAlignment = Alignment.Center
                ) {
                    if (isTabVisible) {
                        WaveDotsLoading()
                    }
                }
            } else if (uiState.playbackDayGroups.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = spacing.space16)
                        .padding(bottom = bottomClearance),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CommonEmptyState(
                        text = if (isEn) {
                            "No recordings in this time range"
                        } else {
                            "Không có dữ liệu ghi hình trong khoảng thời gian này"
                        }
                    )
                    if (uiState.hasMorePlaybackDays) {
                        Spacer(modifier = Modifier.height(spacing.space12))
                        LoadMoreDaysButton(
                            isLoading = uiState.isLoadingMorePlaybackDays,
                            isEn = isEn,
                            onClick = onLoadMoreDays
                        )
                    }
                }
            } else {
                val camId = selectedCamera.camera.id
                val fallbackSnapUrl = selectedCamera.fallbackImageUrl

                // Virtualize theo từng hàng (mỗi hàng 2 thẻ video) thay vì dồn cả ngày vào 1 item duy nhất,
                // giúp LazyColumn tái sử dụng node (contentType = "pb_video_row") và cuộn mượt 60/120fps kể cả khi tải nhiều trang.
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        start = spacing.space10,
                        end = spacing.space10,
                        top = spacing.space4,
                        bottom = bottomClearance
                    ),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    uiState.playbackDayGroups.forEachIndexed { groupIdx, group ->
                        if (groupIdx > 0) {
                            item(
                                key = "pb_spacer_${group.dateKey}",
                                contentType = "pb_day_spacer"
                            ) {
                                Spacer(modifier = Modifier.height(spacing.space10))
                            }
                        }

                        val totalCells = group.videos.size + if (group.hasMore) 1 else 0
                        val rowCount = (totalCells + 1) / 2

                        // 1. Header của ngày (.day-header)
                        item(
                            key = "pb_header_${group.dateKey}",
                            contentType = "pb_day_header"
                        ) {
                            val dayLabel = if (isEn) {
                                group.labelEn.ifEmpty { formatPlaybackDayLabel(group.dateKey, true) }
                            } else {
                                group.labelVi.ifEmpty { formatPlaybackDayLabel(group.dateKey, false) }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(if (rowCount == 0) RoundedCornerShape(spacing.radiusRegular) else dayHeaderShape)
                                    .background(colors.surface)
                                    .padding(
                                        start = spacing.space12,
                                        end = spacing.space10,
                                        top = spacing.space10,
                                        bottom = if (rowCount == 0) spacing.space10 else spacing.space8
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(spacing.space6)
                            ) {
                                Icon(
                                    imageVector = LucideIcons.Calendar,
                                    contentDescription = null,
                                    tint = colors.textMuted,
                                    modifier = Modifier.size(spacing.iconSmall)
                                )
                                Text(
                                    text = dayLabel,
                                    color = colors.textMuted,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // 2. Các hàng video 2 cột được virtualize riêng lẻ
                        items(
                            count = rowCount,
                            key = { rowIdx -> "pb_row_${group.dateKey}_$rowIdx" },
                            contentType = { "pb_video_row" }
                        ) { rowIdx ->
                            val isLastRow = rowIdx == rowCount - 1
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(if (isLastRow) Modifier.clip(dayFooterShape) else Modifier)
                                    .background(colors.surface)
                                    .padding(
                                        start = spacing.space10,
                                        end = spacing.space10,
                                        bottom = if (isLastRow) spacing.space10 else spacing.space8
                                    ),
                                horizontalArrangement = Arrangement.spacedBy(spacing.space8)
                            ) {
                                for (colIdx in 0..1) {
                                    val cellIdx = rowIdx * 2 + colIdx
                                    if (cellIdx < group.videos.size) {
                                        val video = group.videos[cellIdx]
                                        val isSelected = activeVideo != null &&
                                            activeVideo.videoLink == video.videoLink &&
                                            activeVideo.time == video.time
                                        PlaybackVideoGridCard(
                                            video = video,
                                            cameraId = camId,
                                            fallbackSnapshotUrl = fallbackSnapUrl,
                                            isSelected = isSelected,
                                            onClick = { onSelectVideo(video) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    } else if (cellIdx == group.videos.size && group.hasMore) {
                                        LoadMoreDayVideosCard(
                                            isLoading = group.isLoadingMore,
                                            isEn = isEn,
                                            onClick = { onLoadMoreForDay(group.dateKey) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    if (uiState.hasMorePlaybackDays) {
                        item(
                            key = "load_more_playback_days",
                            contentType = "pb_load_more_days"
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = spacing.space12, bottom = spacing.space6),
                                contentAlignment = Alignment.Center
                            ) {
                                LoadMoreDaysButton(
                                    isLoading = uiState.isLoadingMorePlaybackDays,
                                    isEn = isEn,
                                    onClick = onLoadMoreDays
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
private fun PlaybackVideoGridCard(
    video: VideoModel,
    cameraId: Int,
    fallbackSnapshotUrl: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val shape = remember(spacing.radiusSmall) { RoundedCornerShape(spacing.radiusSmall) }
    val timeBadgeShape = remember(spacing.radiusXSmall) { RoundedCornerShape(spacing.radiusXSmall) }

    // Đồng bộ màu nền & viền với các nút điều khiển trong ô Live Camera (CameraGridItem)
    val overlayPillBg = colors.mediaBg.copy(alpha = 0.42f)
    val overlayPillBorder = colors.onMedia.copy(alpha = 0.12f)

    // Ưu tiên ảnh thumbnail/image của bản ghi, nếu không có hoặc bị lỗi thì fallback sang ảnh snapshot đã cache của camera
    val primaryThumbUrl = video.thumbnailLink.ifEmpty { video.imageLink }
    var primaryThumbFailed by remember(cameraId, primaryThumbUrl) {
        mutableStateOf(
            primaryThumbUrl.isNotEmpty() && MonitorCacheManager.failedPlaybackThumbUrls.contains(primaryThumbUrl)
        )
    }
    val effectiveThumbUrl = if (primaryThumbUrl.isNotEmpty() && !primaryThumbFailed) {
        primaryThumbUrl
    } else {
        fallbackSnapshotUrl
    }
    val isUsingFallbackSnap = effectiveThumbUrl == fallbackSnapshotUrl && fallbackSnapshotUrl.isNotEmpty()

    val fallbackCardMemKey = remember(cameraId, fallbackSnapshotUrl) {
        if (fallbackSnapshotUrl.isEmpty()) null else MonitorCacheManager.cardMemCacheKey(cameraId, fallbackSnapshotUrl)
    }
    val targetMemKey = remember(cameraId, effectiveThumbUrl, isUsingFallbackSnap, fallbackCardMemKey) {
        when {
            effectiveThumbUrl.isEmpty() -> null
            isUsingFallbackSnap -> fallbackCardMemKey
            else -> MonitorCacheManager.playbackThumbMemCacheKey(cameraId, effectiveThumbUrl)
        }
    }
    val imageRequest = remember(context, cameraId, effectiveThumbUrl, isUsingFallbackSnap, fallbackCardMemKey) {
        if (effectiveThumbUrl.isEmpty()) {
            null
        } else {
            val diskKey = if (isUsingFallbackSnap) {
                MonitorCacheManager.diskCacheKey(cameraId, fallbackSnapshotUrl)
            } else {
                MonitorCacheManager.playbackThumbDiskCacheKey(cameraId, effectiveThumbUrl)
            }
            ImageRequest.Builder(context)
                .data(MonitorCacheManager.coilDataModel(effectiveThumbUrl))
                .size(SNAPSHOT_CARD_WIDTH_PX, SNAPSHOT_CARD_HEIGHT_PX)
                .precision(Precision.INEXACT)
                .allowHardware(true)
                .allowRgb565(true)
                .crossfade(false)
                .memoryCacheKey(targetMemKey)
                .diskCacheKey(diskKey)
                .placeholderMemoryCacheKey(fallbackCardMemKey)
                .build()
        }
    }

    val timeOnlyText = remember(video.time, video.displayTime) {
        video.displayTime.ifEmpty { formatPlaybackTimeOnly(video.time) }
    }

    Box(
        modifier = modifier
            .aspectRatio(LIVE_STAGE_ASPECT_RATIO)
            .clip(shape)
            .background(colors.thumbnailBg)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = spacing.borderFocus,
                        color = colors.accent,
                        shape = shape
                    )
                } else {
                    Modifier.border(
                        width = spacing.borderThin,
                        color = colors.border,
                        shape = shape
                    )
                }
            )
            .clickable(onClick = onClick)
    ) {
        if (imageRequest != null) {
            AsyncImage(
                model = imageRequest,
                contentDescription = timeOnlyText,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                onError = {
                    if (!isUsingFallbackSnap && primaryThumbUrl.isNotEmpty()) {
                        MonitorCacheManager.failedPlaybackThumbUrls.add(primaryThumbUrl)
                        primaryThumbFailed = true
                    }
                }
            )
        }

        // Play icon overlay (.play-overlay)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(spacing.iconButtonSmall)
                .clip(CircleShape)
                .background(overlayPillBg)
                .border(
                    width = spacing.borderThin,
                    color = colors.onMedia.copy(alpha = 0.28f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = LucideIcons.Play,
                contentDescription = null,
                tint = colors.onMedia,
                modifier = Modifier.size(spacing.iconXSmall)
            )
        }

        // .video-time-overlay (đồng bộ màu nền & viền với các nút trong ô Live Camera)
        if (timeOnlyText.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(spacing.space6)
                    .clip(timeBadgeShape)
                    .background(overlayPillBg)
                    .border(
                        width = spacing.borderMedium,
                        color = overlayPillBorder,
                        shape = timeBadgeShape
                    )
                    .padding(horizontal = spacing.space6, vertical = spacing.space2)
            ) {
                Text(
                    text = timeOnlyText,
                    color = colors.onMedia,
                    style = MaterialTheme.typography.micro,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun LoadMoreDayVideosCard(
    isLoading: Boolean,
    isEn: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val shape = RoundedCornerShape(spacing.radiusSmall)

    Box(
        modifier = modifier
            .aspectRatio(LIVE_STAGE_ASPECT_RATIO)
            .clip(shape)
            .background(colors.controlBg)
            .clickable(enabled = !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.space4),
            modifier = Modifier.padding(spacing.space8)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = colors.accent,
                    modifier = Modifier.size(spacing.iconRegular),
                    strokeWidth = spacing.progressStroke
                )
            } else {
                Icon(
                    imageVector = LucideIcons.PlusCircle,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(spacing.iconRegular)
                )
            }
            Text(
                text = if (isLoading) {
                    if (isEn) "Loading..." else "Đang tải..."
                } else {
                    if (isEn) "View more videos" else "Xem thêm video"
                },
                color = colors.textStrong,
                style = MaterialTheme.typography.caption,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LoadMoreDaysButton(
    isLoading: Boolean,
    isEn: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(spacing.radiusCompact))
            .background(colors.surface)
            .border(
                width = spacing.borderThin,
                color = colors.border,
                shape = RoundedCornerShape(spacing.radiusCompact)
            )
            .clickable(enabled = !isLoading, onClick = onClick)
            .padding(horizontal = spacing.space14, vertical = spacing.space8),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.space8)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = colors.accent,
                modifier = Modifier.size(spacing.iconSmall),
                strokeWidth = spacing.progressStroke
            )
        } else {
            Icon(
                imageVector = LucideIcons.Calendar,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(spacing.iconSmall)
            )
        }
        Text(
            text = if (isLoading) {
                if (isEn) "Loading older days..." else "Đang tải thêm ngày..."
            } else {
                if (isEn) "Load older days" else "Tải thêm ngày cũ hơn"
            },
            color = colors.textStrong,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CameraEventsTabContent(
    uiState: MonitorUiState,
    cameraName: String,
    isTabVisible: Boolean,
    listState: LazyListState,
    bottomClearance: androidx.compose.ui.unit.Dp,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onEventClick: (EventData) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = MaterialTheme.spacing

    val shouldLoadMore by remember(listState) {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 1 && lastVisibleIndex >= totalItems - CAMERA_EVENT_LOAD_MORE_THRESHOLD
        }
    }

    LaunchedEffect(shouldLoadMore, uiState.cameraEvents.size, uiState.hasMoreCameraEvents) {
        if ((shouldLoadMore || uiState.cameraEvents.size in 1..CAMERA_EVENT_AUTO_FILL_MAX) &&
            uiState.hasMoreCameraEvents &&
            !uiState.isLoadingCameraEvents &&
            !uiState.isPaginatingCameraEvents
        ) {
            onLoadMore()
        }
    }

    PullToRefreshBox(
        isRefreshing = uiState.isRefreshingCameraEvents,
        onRefresh = onRefresh,
        modifier = modifier
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = spacing.space4, bottom = bottomClearance),
            verticalArrangement = Arrangement.spacedBy(spacing.space10)
        ) {
            if (uiState.isLoadingCameraEvents && uiState.cameraEvents.isEmpty()) {
                item(key = "cam_event_loading") {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isTabVisible) {
                            WaveDotsLoading()
                        }
                    }
                }
            } else if (uiState.cameraEvents.isEmpty()) {
                item(key = "cam_event_empty") {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CommonEmptyState(text = stringResource(id = R.string.event_empty_state))
                    }
                }
            } else {
                items(
                    items = uiState.cameraEvents,
                    key = { event -> "cam_ev_${event.uniqueKey()}" }
                ) { event ->
                    Box(modifier = Modifier.padding(horizontal = spacing.space10)) {
                        MobileEventCard(
                            event = event,
                            cameraName = cameraName,
                            isListView = true,
                            enableSharedElement = false,
                            onClick = { onEventClick(event) }
                        )
                    }
                }

                if (uiState.isPaginatingCameraEvents || (uiState.hasMoreCameraEvents && !uiState.isLoadingCameraEvents)) {
                    item(key = "cam_event_paging") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.space16),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(spacing.space28),
                                strokeWidth = spacing.space3
                            )
                        }
                    }
                }
            }
        }
    }
}

private val dateKeyParserThreadLocal = object : ThreadLocal<SimpleDateFormat>() {
    override fun initialValue(): SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
}
private val dayDisplayFormatterThreadLocal = object : ThreadLocal<SimpleDateFormat>() {
    override fun initialValue(): SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
}
private val timeOnlyFormatterThreadLocal = object : ThreadLocal<SimpleDateFormat>() {
    override fun initialValue(): SimpleDateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
}
private val dateTimeFormatterThreadLocal = object : ThreadLocal<SimpleDateFormat>() {
    override fun initialValue(): SimpleDateFormat = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault())
}

private fun formatPlaybackDayLabel(dateKey: String, isEn: Boolean): String {
    val parsed = runCatching {
        dateKeyParserThreadLocal.get()?.parse(dateKey)
    }.getOrNull() ?: return dateKey

    val targetCal = Calendar.getInstance().apply { time = parsed }
    val todayCal = Calendar.getInstance()
    if (targetCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
        targetCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)
    ) {
        return if (isEn) "Today" else "Hôm nay"
    }

    val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    if (targetCal.get(Calendar.YEAR) == yesterdayCal.get(Calendar.YEAR) &&
        targetCal.get(Calendar.DAY_OF_YEAR) == yesterdayCal.get(Calendar.DAY_OF_YEAR)
    ) {
        return if (isEn) "Yesterday" else "Hôm qua"
    }

    return dayDisplayFormatterThreadLocal.get()?.format(parsed) ?: dateKey
}

private fun formatPlaybackTimeOnly(timeStr: String): String {
    val millis = parseVideoQueryTime(timeStr) ?: return timeStr
    return timeOnlyFormatterThreadLocal.get()?.format(Date(millis)) ?: timeStr
}

private fun formatPlaybackDateTime(timeStr: String): String {
    val millis = parseVideoQueryTime(timeStr) ?: return timeStr
    return dateTimeFormatterThreadLocal.get()?.format(Date(millis)) ?: timeStr
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
private fun MobileCameraSnapshotCard(
    camera: CameraModel,
    isTabActive: Boolean,
    hasSnapKey: Boolean,
    snapshotUrl: String?,
    onSnapshotError: (CameraModel) -> Unit,
    onClick: (CameraModel) -> Unit
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
    val currentCamera by rememberUpdatedState(camera)
    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnSnapshotError by rememberUpdatedState(onSnapshotError)

    val handleCardClick: () -> Unit = remember(scope, view, focusManager, keyboardController) {
        {
            val keyboardWasOpen = ViewCompat.getRootWindowInsets(view)
                ?.isVisible(WindowInsetsCompat.Type.ime()) == true
            focusManager.clearFocus()
            keyboardController?.hide()

            if (keyboardWasOpen) {
                // Nếu bàn phím đang mở: ưu tiên đóng bàn phím xong hoàn toàn (~240ms) rồi mới trượt mở màn hình Live
                isTapFlashing = true
                scope.launch {
                    delay(240L)
                    currentOnClick(currentCamera)
                    delay(160L)
                    isTapFlashing = false
                }
            } else {
                isTapFlashing = true
                currentOnClick(currentCamera)
                scope.launch {
                    delay(160L)
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

    // Giữ cache key của ảnh đã tải thành công trước đó trong holder không gây recompose để tránh tạo lại ImageRequest 2 lần
    val lastLoadedCacheKeyHolder = remember(camera.id) { CacheKeyHolder() }
    var hasLoadedAnyImage by remember(camera.id) { mutableStateOf(false) }
    var isImageLoading by remember(camera.id, snapshotUrl) {
        mutableStateOf(!snapshotUrl.isNullOrEmpty() && !hasLoadedAnyImage)
    }
    var isImageError by remember(camera.id, snapshotUrl) { mutableStateOf(false) }

    val targetMemKey = remember(camera.id, snapshotUrl) {
        if (snapshotUrl.isNullOrEmpty()) null else MonitorCacheManager.cardMemCacheKey(camera.id, snapshotUrl)
    }
    val imageRequest = remember(camera.id, snapshotUrl) {
        if (snapshotUrl.isNullOrEmpty()) {
            null
        } else {
            ImageRequest.Builder(context)
                .data(MonitorCacheManager.coilDataModel(snapshotUrl))
                .size(SNAPSHOT_CARD_WIDTH_PX, SNAPSHOT_CARD_HEIGHT_PX)
                .precision(Precision.INEXACT)
                .allowHardware(true)
                .allowRgb565(true)
                .crossfade(false)
                .memoryCacheKey(targetMemKey)
                .diskCacheKey(MonitorCacheManager.diskCacheKey(camera.id, snapshotUrl))
                .placeholderMemoryCacheKey(lastLoadedCacheKeyHolder.key)
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
                        hasLoadedAnyImage = true
                        lastLoadedCacheKeyHolder.key = targetMemKey
                    },
                    onError = {
                        isImageLoading = false
                        if (!hasLoadedAnyImage) {
                            isImageError = true
                        }
                        currentOnSnapshotError(currentCamera)
                    }
                )
                if (isImageLoading && !hasLoadedAnyImage && isTabActive) {
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
    isEntering: Boolean = false,
    isClosing: Boolean = false,
    onClick: () -> Unit,
    onRetryClick: () -> Unit,
    onSuccess: () -> Unit,
    onSnapshotError: () -> Unit = {},
    onFullscreenClick: () -> Unit,
    onBackToLiveClick: () -> Unit,
    onSwitchChannel: (Int) -> Unit = {},
    onRemoveClick: () -> Unit = {},
    isFullscreen: Boolean = false,
    isCompact: Boolean = false,
    showFullscreenButton: Boolean = true,
    showCloseButton: Boolean = true
) {
    val context = LocalContext.current
    val isEn = rememberIsEnglish()
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val deviceKey = UserSession.accessToken
    // Không reset isLiveViewPlaying về false ngay ở Frame 1 khi bấm Back (isClosing = true)
    // để giữ nguyên khung hình video cuối cùng trong suốt 240ms trượt đóng mà không bật lại vòng xoay loading
    var isLiveViewPlaying by remember(selectedCamera.camera.id, selectedCamera.streamUrl) { mutableStateOf(false) }
    var isLiveViewError by remember(selectedCamera.camera.id, selectedCamera.streamUrl) { mutableStateOf(false) }
    var snapshotFailed by remember(selectedCamera.camera.id, selectedCamera.fallbackImageUrl) { mutableStateOf(false) }
    val hasError = isLiveViewError || selectedCamera.streamUrl.startsWith("error://")
    val hasSub = remember(selectedCamera.camera.id, selectedCamera.camera.sub, selectedCamera.camera.main) {
        selectedCamera.camera.sub.isNotBlank() && selectedCamera.camera.main.isNotBlank()
    }

    // Đồng bộ camera_fit_ratio_${camId} ('contain' / 'cover') từ LiveVideoView.vue
    val ratioPrefs = remember(context) {
        context.getSharedPreferences("tenli_camera_prefs", Context.MODE_PRIVATE)
    }
    var isOriginalRatio by remember(selectedCamera.camera.id) {
        mutableStateOf(
            ratioPrefs.getString("camera_fit_ratio_${selectedCamera.camera.id}", "cover") == "contain"
        )
    }
    val toggleAspectRatio: () -> Unit = remember(selectedCamera.camera.id, ratioPrefs) {
        {
            val next = !isOriginalRatio
            isOriginalRatio = next
            ratioPrefs.edit()
                .putString("camera_fit_ratio_${selectedCamera.camera.id}", if (next) "contain" else "cover")
                .apply()
        }
    }

    // Mặc định ẩn toàn bộ overlay (tên camera, các nút action, nút phóng to).
    // Chạm vào màn hình live: đang ẩn -> hiện lên và tự ẩn sau 3 giây; đang hiện -> chạm vùng trống sẽ ẩn ngay.
    var showInfo by remember(selectedCamera.camera.id) { mutableStateOf(false) }
    var showChannelDropdown by remember(selectedCamera.camera.id) { mutableStateOf(false) }
    var autoHideToken by remember(selectedCamera.camera.id) { mutableIntStateOf(0) }
    LaunchedEffect(showInfo, autoHideToken) {
        if (showInfo) {
            delay(3000L)
            showInfo = false
            showChannelDropdown = false
        } else {
            showChannelDropdown = false
        }
    }

    val surfaceBg = if (isFullscreen || isOriginalRatio || !showCloseButton) colors.mediaBg else colors.surface
    val stateOverlayBg = if (isFullscreen) colors.mediaBg else colors.surface

    // Chờ hoàn tất hiệu ứng trượt mở (isEntering = false, ~280ms) mới mount ExoPlayer/TextureView để trượt vào mượt 120fps;
    // Khi trượt đóng (isClosing = true), giữ PlayerView với trạng thái isPaused = true (ngắt ngay WS & giải mã) cho đến khi trượt xong mới tháo View.
    val isStreamPaused = isClosing || !isTabActive
    val canPlayStream = (isTabActive || (isClosing && isLiveViewPlaying)) &&
        !isEntering &&
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
                            showChannelDropdown = false
                        } else {
                            showInfo = true
                            autoHideToken++
                        }
                    },
                    onDoubleTap = {
                        if (!isCompact && showFullscreenButton) {
                            showInfo = false
                            showChannelDropdown = false
                            onFullscreenClick()
                        } else {
                            onClick()
                            showInfo = !showInfo
                            if (showInfo) autoHideToken++ else showChannelDropdown = false
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
                    isOriginalRatio = isOriginalRatio,
                    isPaused = isStreamPaused,
                    showInternalOverlay = false,
                    onPlayingChange = {
                        isLiveViewPlaying = it
                        if (it) {
                            onSuccess()
                        }
                    },
                    onErrorChange = { isError ->
                        if (!isStreamPaused) {
                            isLiveViewError = isError
                            if (isError) {
                                onRetryClick()
                            }
                        }
                    }
                )
            } else {
                VideoPlayer(
                    videoUrl = selectedCamera.streamUrl,
                    thumbnailUrl = null,
                    deviceKey = deviceKey,
                    isOriginalRatio = isOriginalRatio,
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
        val camId = selectedCamera.camera.id
        val fallbackUrl = selectedCamera.fallbackImageUrl
        val lastFallbackKeyHolder = remember(camId) { CacheKeyHolder() }
        val targetFallbackMemKey = remember(camId, fallbackUrl) {
            if (fallbackUrl.isEmpty()) null else MonitorCacheManager.liveStageMemCacheKey(camId, fallbackUrl)
        }
        val cardFallbackMemKey = remember(camId, fallbackUrl) {
            if (fallbackUrl.isEmpty()) null else MonitorCacheManager.cardMemCacheKey(camId, fallbackUrl)
        }
        val fallbackImageRequest = remember(camId, fallbackUrl) {
            if (fallbackUrl.isEmpty()) {
                null
            } else {
                ImageRequest.Builder(context)
                    .data(MonitorCacheManager.coilDataModel(fallbackUrl))
                    .size(LIVE_STAGE_SNAPSHOT_WIDTH_PX, LIVE_STAGE_SNAPSHOT_HEIGHT_PX)
                    .precision(Precision.INEXACT)
                    .allowHardware(true)
                    .allowRgb565(true)
                    .crossfade(false)
                    .memoryCacheKey(targetFallbackMemKey)
                    .diskCacheKey(MonitorCacheManager.diskCacheKey(camId, fallbackUrl))
                    .placeholderMemoryCacheKey(lastFallbackKeyHolder.key ?: cardFallbackMemKey)
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
                    .background(stateOverlayBg),
                contentAlignment = Alignment.Center
            ) {
                if (fallbackImageRequest != null && !snapshotFailed) {
                    AsyncImage(
                        model = fallbackImageRequest,
                        contentDescription = selectedCamera.camera.name,
                        contentScale = if (isOriginalRatio) ContentScale.Fit else ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        onSuccess = {
                            lastFallbackKeyHolder.key = targetFallbackMemKey
                        },
                        onError = {
                            if (lastFallbackKeyHolder.key == null) {
                                snapshotFailed = true
                            }
                            onSnapshotError()
                        }
                    )
                }
                if (isTabActive && !isClosing) {
                    WaveDotsLoading(dotColor = colors.accent)
                }
            }
        }

        // Error Overlay (chạm để tải lại luồng live giống reloadStream trong LiveVideoView.vue)
        if (hasError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(stateOverlayBg)
                    .clickable {
                        onClick()
                        showInfo = true
                        showChannelDropdown = false
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

        // Top-Left Camera Name Pill (.info-name), Top-Right Action Pill (.info-action),
        // & Bottom-Right Fullscreen Button (.fullscreen-btn) — đồng bộ Web PC (LiveVideoView.vue)
        AnimatedVisibility(
            visible = showInfo,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(200)),
            modifier = Modifier.fillMaxSize()
        ) {
            val overlayPillBg = colors.mediaBg.copy(alpha = 0.42f)
            val overlayPillBorder = colors.onMedia.copy(alpha = 0.12f)
            val edgePadding = if (isCompact) spacing.space4 else spacing.space8
            val pillHeight = if (isCompact) spacing.iconMedium else spacing.iconButtonSmall
            val pillInnerPad = if (isCompact) spacing.space1 else spacing.space2
            val actionBtnSize = if (isCompact) spacing.iconRegular else spacing.iconMedium
            val actionIconSize = if (isCompact) spacing.iconXSmall else spacing.iconSmall
            val textStyle = if (isCompact) MaterialTheme.typography.caption else MaterialTheme.typography.labelMedium

            val currentStreamLabel = if (selectedCamera.channel == 1) {
                if (isEn) "Sub Stream" else "Luồng phụ"
            } else {
                if (isEn) "Main Stream" else "Luồng chính"
            }
            val alternateStreamLabel = if (selectedCamera.channel == 0) {
                if (isEn) "Sub Stream" else "Luồng phụ"
            } else {
                if (isEn) "Main Stream" else "Luồng chính"
            }
            val alternateChannel = if (selectedCamera.channel == 0) 1 else 0

            Box(modifier = Modifier.fillMaxSize()) {
                // .info-container -> .info
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .padding(edgePadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // .info-name (cùng chiều cao pillHeight với .info-action để cân đối trên 1 hàng)
                    Box(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .padding(end = spacing.space8)
                            .height(pillHeight)
                            .clip(RoundedCornerShape(spacing.radiusSmall))
                            .background(overlayPillBg)
                            .border(spacing.borderMedium, overlayPillBorder, RoundedCornerShape(spacing.radiusSmall))
                            .padding(horizontal = if (isCompact) spacing.space6 else spacing.space8),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = selectedCamera.camera.name,
                            color = colors.onMedia,
                            style = textStyle,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // .info-action + .channel-dropdown
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            modifier = Modifier
                                .height(pillHeight)
                                .clip(RoundedCornerShape(spacing.radiusSmall))
                                .background(overlayPillBg)
                                .border(spacing.borderMedium, overlayPillBorder, RoundedCornerShape(spacing.radiusSmall))
                                .padding(horizontal = pillInnerPad),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.space2)
                        ) {
                            // 1. .channel-selector (Luồng chính / Luồng phụ)
                            Row(
                                modifier = Modifier
                                    .height(actionBtnSize)
                                    .clip(RoundedCornerShape(spacing.radiusTiny))
                                    .background(
                                        if (showChannelDropdown && hasSub) {
                                            colors.onMedia.copy(alpha = 0.2f)
                                        } else {
                                            Color.Transparent
                                        }
                                    )
                                    .alpha(if (hasSub) 1f else 0.65f)
                                    .then(
                                        if (hasSub) {
                                            Modifier.clickable {
                                                showChannelDropdown = !showChannelDropdown
                                                autoHideToken++
                                            }
                                        } else {
                                            Modifier
                                        }
                                    )
                                    .padding(horizontal = if (isCompact) spacing.space6 else spacing.space8),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(spacing.space4)
                            ) {
                                Text(
                                    text = currentStreamLabel,
                                    color = colors.onMedia,
                                    style = textStyle,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                                if (hasSub) {
                                    Icon(
                                        imageVector = LucideIcons.CaretDown,
                                        contentDescription = null,
                                        tint = colors.onMedia.copy(alpha = 0.85f),
                                        modifier = Modifier.size(spacing.iconTiny)
                                    )
                                }
                            }

                            // 2. .aspect-ratio-btn (Tỷ lệ khung hình gốc / Lấp đầy khung 16:9)
                            Box(
                                modifier = Modifier
                                    .size(actionBtnSize)
                                    .clip(RoundedCornerShape(spacing.radiusTiny))
                                    .background(
                                        if (isOriginalRatio) {
                                            BrandActiveNavDark.copy(alpha = 0.35f)
                                        } else {
                                            Color.Transparent
                                        }
                                    )
                                    .clickable {
                                        showChannelDropdown = false
                                        autoHideToken++
                                        toggleAspectRatio()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isOriginalRatio) LucideIcons.StretchHorizontal else LucideIcons.Proportions,
                                    contentDescription = if (isOriginalRatio) {
                                        if (isEn) "Fill 16:9 frame" else "Lấp đầy khung 16:9"
                                    } else {
                                        if (isEn) "Original aspect ratio" else "Tỷ lệ khung hình gốc"
                                    },
                                    tint = if (isOriginalRatio) BrandLight else colors.onMedia,
                                    modifier = Modifier.size(actionIconSize)
                                )
                            }

                            // 3. .reload-button (Tải lại luồng trực tiếp)
                            Box(
                                modifier = Modifier
                                    .size(actionBtnSize)
                                    .clip(RoundedCornerShape(spacing.radiusTiny))
                                    .clickable {
                                        showChannelDropdown = false
                                        autoHideToken++
                                        onBackToLiveClick()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = LucideIcons.RotateCw,
                                    contentDescription = if (isEn) "Reload live stream" else "Tải lại luồng trực tiếp",
                                    tint = colors.onMedia,
                                    modifier = Modifier.size(actionIconSize)
                                )
                            }

                            // 4. .action-button (Close x-circle, nếu bật showCloseButton)
                            if (!isFullscreen && showCloseButton) {
                                Box(
                                    modifier = Modifier
                                        .size(actionBtnSize)
                                        .clip(RoundedCornerShape(spacing.radiusTiny))
                                        .clickable {
                                            showChannelDropdown = false
                                            onRemoveClick()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = LucideIcons.XCircle,
                                        contentDescription = if (isEn) "Close live stream" else "Đóng luồng trực tiếp",
                                        tint = colors.onMedia,
                                        modifier = Modifier.size(actionIconSize)
                                    )
                                }
                            }
                        }

                        // .channel-dropdown (nằm ngay dưới .channel-selector, cách 4dp)
                        if (showChannelDropdown && hasSub) {
                            Spacer(modifier = Modifier.height(spacing.space4))
                            Box(
                                modifier = Modifier
                                    .padding(start = if (isCompact) spacing.space2 else spacing.space3)
                                    .clip(RoundedCornerShape(spacing.radiusTiny))
                                    .background(colors.mediaOverlay.copy(alpha = 0.95f))
                                    .border(
                                        spacing.borderMedium,
                                        colors.onMedia.copy(alpha = 0.15f),
                                        RoundedCornerShape(spacing.radiusTiny)
                                    )
                                    .clickable {
                                        showChannelDropdown = false
                                        autoHideToken++
                                        onSwitchChannel(alternateChannel)
                                    }
                                    .padding(
                                        horizontal = spacing.space10,
                                        vertical = spacing.space5
                                    ),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = alternateStreamLabel,
                                    color = colors.onMedia.copy(alpha = 0.9f),
                                    style = textStyle,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
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
                                showChannelDropdown = false
                                onFullscreenClick()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFullscreen) LucideIcons.Minimize else LucideIcons.Maximize,
                            contentDescription = if (isEn) "Fullscreen" else "Toàn màn hình",
                            tint = colors.onMedia,
                            modifier = Modifier.size(if (isFullscreen) spacing.iconSmallMedium else spacing.iconSmall)
                        )
                    }
                }
            }
        }
    }
}
