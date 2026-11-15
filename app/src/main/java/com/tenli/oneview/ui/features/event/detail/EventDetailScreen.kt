package com.tenli.oneview.ui.features.event.detail

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.SystemClock
import android.view.OrientationEventListener
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.Coil
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.memory.MemoryCache
import coil.request.ImageRequest
import coil.size.Precision
import com.tenli.oneview.R
import com.tenli.oneview.data.local.MonitorCacheManager
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.ui.component.AnchoredFilterDropdownMenu
import com.tenli.oneview.ui.component.AppConfirmDialog
import com.tenli.oneview.ui.component.CommonEmptyState
import com.tenli.oneview.ui.component.CustomDatePickerDialogModal
import com.tenli.oneview.ui.component.CustomRangePickerDialogModal
import com.tenli.oneview.ui.component.FilterCellRow
import com.tenli.oneview.ui.component.LiveStreamPlayer
import com.tenli.oneview.ui.component.MobileEventCard
import com.tenli.oneview.ui.component.MobileTopPopupManager
import com.tenli.oneview.ui.component.SheetCloseButton
import com.tenli.oneview.ui.component.VideoFrameImage
import com.tenli.oneview.ui.component.VideoPlayer
import com.tenli.oneview.ui.component.WaveDotsLoading
import com.tenli.oneview.ui.component.ZoomableMediaBox
import com.tenli.oneview.ui.component.rememberIsEnglish
import com.tenli.oneview.ui.features.event.buildCameraNameMap
import com.tenli.oneview.ui.features.event.cameraUuidOrNull
import com.tenli.oneview.ui.features.event.formatEventTimeOnly
import com.tenli.oneview.ui.features.event.isPlateBlacklisted
import com.tenli.oneview.ui.features.event.uniqueKey
import com.tenli.oneview.ui.features.home.TimeFilter
import com.tenli.oneview.ui.theme.BrandActiveNavDark
import com.tenli.oneview.ui.theme.BrandLight
import com.tenli.oneview.ui.theme.BrandPrimary
import com.tenli.oneview.ui.theme.LucideIcons
import com.tenli.oneview.ui.theme.OrangeDeep
import com.tenli.oneview.ui.theme.Slate500
import com.tenli.oneview.ui.theme.Slate900
import com.tenli.oneview.ui.theme.SurfaceVariant
import com.tenli.oneview.ui.theme.SurfaceVariantDark
import com.tenli.oneview.ui.theme.WarningAmberDeep
import com.tenli.oneview.ui.theme.WarningOrange
import com.tenli.oneview.ui.theme.appColors
import com.tenli.oneview.ui.theme.caption
import com.tenli.oneview.ui.theme.spacing
import com.tenli.oneview.ui.utils.AiTypeHelper
import com.tenli.oneview.ui.utils.MediaActionHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

enum class EventMediaTab(val titleVi: String, val titleEn: String) {
    LIVE_CAMERA("Live Camera", "Live Camera"),
    EVENT_IMAGE("Ảnh sự kiện", "Event photo"),
    EVENT_VIDEO("Video sự kiện", "Event video")
}

private val MediaTabs = EventMediaTab.entries

// ---------------------------------------------------------------------------------------------
// Ngưỡng cử chỉ: kéo xuống để đóng & vuốt ngang để đổi tab media (khớp bottom-sheet của PWA)
// ---------------------------------------------------------------------------------------------
/** Quãng đường sheet trượt xuống khi đóng */
private val DismissTravelDistance = 950.dp
private val HorizontalLockSlop = 16.dp
private val PreConsumeVerticalSlop = 5.dp
private val VerticalLockSlop = 14.dp
/** Vùng thanh điều khiển (seekbar) dưới đáy video – không bắt cử chỉ vuốt */
private val VideoControllerZoneHeight = 50.dp
private const val SHEET_DRAG_RESISTANCE = 0.9f
private const val SHEET_DRAGGED_THRESHOLD_PX = 2f
private const val TAB_SWIPE_DISTANCE_DP = 42f
private const val TAB_FLICK_DISTANCE_DP = 26f
private const val TAB_FLICK_MAX_MS = 300L
private const val TAB_SWIPE_COOLDOWN_MS = 250L
private const val DISMISS_DISTANCE_DP = 85f
private const val DISMISS_FLICK_DISTANCE_DP = 42f
private const val DISMISS_FLICK_VELOCITY_DP_PER_MS = 0.45f
private const val DISMISS_FLICK_MAX_MS = 400L

private const val DISMISS_SCRIM_MAX_ALPHA = 0.45f
private val DismissScrimColor = Color.Black

// ---------------------------------------------------------------------------------------------
// Animation specs (khởi tạo 1 lần)
// ---------------------------------------------------------------------------------------------
private val StandardEasing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
private val SegmentEasing = CubicBezierEasing(0.18f, 0.9f, 0.3f, 1f)
private val SlideEasing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
private val PopupInEasing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

// ---------------------------------------------------------------------------------------------
// Màu & kích thước đặc thù
// ---------------------------------------------------------------------------------------------
/** Màu bounding box trên ảnh sự kiện (khớp palette của PWA) */
private val BoundingBoxPalette = listOf(
    Color(0xFFFF4D4F),
    Color(0xFF38DD0F),
    Color(0xFF1890FF),
    Color(0xFFFAAD14),
    Color(0xFF13C2C2),
    Color(0xFFEB2F96)
)
private val BoundingBoxStroke = Stroke(width = 4f)
private val PillShadowColor = Color.Black.copy(alpha = 0.12f)

private val LocationMaxWidth = 230.dp
private val CropPopupMaxWidth = 280.dp
private val CropPopupMaxHeight = 180.dp
private val CropPopupFallbackSize = 156.dp
private val RelatedPlateCardWidth = 148.dp
private val PlatesLoadingHeight = 110.dp
private val HistoryLoadingHeight = 180.dp
private val HistoryEmptyHeight = 160.dp

private val SectionLetterSpacing = 0.6.sp
private val PlateLetterSpacing = 0.8.sp

/** Chiều cao thanh chọn tab media / bộ chọn khoảng thời gian (42dp) */
@Composable
private fun segmentControlHeight(): Dp = MaterialTheme.spacing.space40 + MaterialTheme.spacing.space2

/**
 * Tọa độ các vùng trên màn hình – chỉ đọc trong bộ xử lý cử chỉ nên dùng biến thường
 * (không phải snapshot state) để cập nhật liên tục khi cuộn mà không gây recomposition.
 */
private class GestureBounds {
    var rootTopY = 0f
    var pinnedTopBottomY = 0f
    var dataBoardBottomY = 0f
    var lastTabSwipeTimeMs = 0L
    var sheetRootCoords: LayoutCoordinates? = null
    var mediaViewportCoords: LayoutCoordinates? = null
    var periodRowCoords: LayoutCoordinates? = null
}

@Composable
fun EventDetailScreen(
    eventId: Int,
    onBack: () -> Unit,
    isClosing: Boolean = false,
    onSwipeDismissed: () -> Unit = onBack,
    viewModel: EventDetailViewModel = viewModel(
        key = "event_detail_$eventId",
        factory = EventDetailViewModel.provideFactory(
            eventId = eventId,
            fallbackApplication = LocalContext.current.applicationContext as android.app.Application
        )
    )
) {
    var isDismissing by remember(eventId) { mutableStateOf(false) }
    val handleBack: () -> Unit = {
        if (!isDismissing) {
            isDismissing = true
            viewModel.stopLiveStream()
            onBack()
        }
    }
    BackHandler(onBack = handleBack)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isEn = rememberIsEnglish()
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current

    var selectedTab by remember(eventId) { mutableStateOf(EventMediaTab.EVENT_IMAGE) }

    // 1. Fullscreen cho Live Camera & Video sự kiện (xoay cửa sổ Activity, đồng bộ với trạng thái xoay thực tế
    //    để không bị nháy khung dọc lúc mở và không bị lộ các tab ở chế độ ngang lúc thu nhỏ)
    var wantsVideoFullscreen by remember(eventId) { mutableStateOf(false) }
    var isExitingVideoFullscreen by remember(eventId) { mutableStateOf(false) }
    var videoFullscreenFallbackReady by remember(eventId) { mutableStateOf(false) }
    val isWindowLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    LaunchedEffect(wantsVideoFullscreen) {
        if (wantsVideoFullscreen) {
            videoFullscreenFallbackReady = false
            delay(450L)
            videoFullscreenFallbackReady = true
        } else {
            videoFullscreenFallbackReady = false
        }
    }

    LaunchedEffect(isWindowLandscape, isExitingVideoFullscreen) {
        if (isExitingVideoFullscreen) {
            if (!isWindowLandscape) {
                isExitingVideoFullscreen = false
            } else {
                delay(500L)
                isExitingVideoFullscreen = false
            }
        }
    }

    val isMediaFullscreen =
        (wantsVideoFullscreen && (isWindowLandscape || videoFullscreenFallbackReady)) ||
            (isExitingVideoFullscreen && isWindowLandscape)

    // 2. Fullscreen cho Ảnh sự kiện: Hero-Rotation 120Hz xoay ngang mượt 0° <-> ±90° ngay trong cửa sổ Portrait
    //    (không bao giờ hiển thị dọc trước rồi mới giật ngang, và khi thu nhỏ xoay thẳng ảnh về đúng khung cũ)
    var isImageFullscreenOpen by remember(eventId) { mutableStateOf(false) }
    var imageInlineOriginRect by remember(eventId) { mutableStateOf<Rect?>(null) }
    var inlineImageResetTrigger by remember(eventId) { mutableIntStateOf(0) }
    var overlayImageResetTrigger by remember(eventId) { mutableIntStateOf(0) }
    val imageFullscreenProgress = remember(eventId) { Animatable(0f) }
    val isImageOverlayActive by remember(imageFullscreenProgress) {
        derivedStateOf { isImageFullscreenOpen || imageFullscreenProgress.value > 0.001f }
    }

    var isMediaZoomed by remember(eventId) { mutableStateOf(false) }
    var expandedCropUrl by remember(eventId) { mutableStateOf<String?>(null) }
    var showConfirmReportDialog by remember(eventId) { mutableStateOf(false) }
    var showCustomDateModal by remember(eventId) { mutableStateOf(false) }
    var showCustomRangeModal by remember(eventId) { mutableStateOf(false) }
    var customPickerVersion by remember(eventId) { mutableIntStateOf(0) }
    var showPeriodMenu by remember(eventId) { mutableStateOf(false) }
    var periodAnchorRect by remember(eventId) { mutableStateOf<Rect?>(null) }

    val currentIsMediaFullscreen by rememberUpdatedState(isMediaFullscreen || isImageOverlayActive)
    val currentIsMediaZoomed by rememberUpdatedState(isMediaZoomed)

    LaunchedEffect(selectedTab, uiState.event?.id) {
        isMediaZoomed = false
    }

    LaunchedEffect(isImageFullscreenOpen) {
        val activity = context.findActivity()
        val insetsController = activity?.window?.let { win ->
            WindowCompat.getInsetsController(win, win.decorView)
        }
        if (isImageFullscreenOpen) {
            if (insetsController != null) {
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
            }
            imageFullscreenProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 300,
                    easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
                )
            )
        } else {
            if (!wantsVideoFullscreen) {
                insetsController?.show(WindowInsetsCompat.Type.systemBars())
            }
            if (imageFullscreenProgress.value > 0f) {
                imageFullscreenProgress.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = 280,
                        easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
                    )
                )
            }
        }
    }

    LandscapeFullscreenEffect(
        isFullscreen = wantsVideoFullscreen,
        onExitFullscreen = {
            isExitingVideoFullscreen = true
            wantsVideoFullscreen = false
        }
    )

    BackHandler(enabled = showPeriodMenu) {
        showPeriodMenu = false
    }

    val listState = rememberLazyListState()
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress && showPeriodMenu) {
            showPeriodMenu = false
        }
    }

    // Drag-to-dismiss vertical offset (matching PWA bottom-sheet swipe down to dismiss)
    val sheetOffsetY = remember(eventId) { Animatable(0f) }
    val screenHeightPx = remember(density) { with(density) { DismissTravelDistance.toPx() } }
    val horizontalLockSlopPx = remember(density) { with(density) { HorizontalLockSlop.toPx() } }
    val preConsumeVerticalSlopPx = remember(density) { with(density) { PreConsumeVerticalSlop.toPx() } }
    val verticalLockSlopPx = remember(density) { with(density) { VerticalLockSlop.toPx() } }
    val videoControllerZoneHeightPx = remember(density) { with(density) { VideoControllerZoneHeight.toPx() } }
    val bounds = remember(eventId) { GestureBounds() }

    fun captureInlineMediaViewportRect(): Rect? {
        val root = bounds.sheetRootCoords
        val vp = bounds.mediaViewportCoords
        return if (root != null && vp != null && root.isAttached && vp.isAttached) {
            root.localBoundingBoxOf(vp, clipBounds = false)
        } else {
            null
        }
    }

    fun openImageFullscreen() {
        captureInlineMediaViewportRect()?.let { imageInlineOriginRect = it }
        inlineImageResetTrigger++
        isImageFullscreenOpen = true
    }

    fun closeImageFullscreen() {
        captureInlineMediaViewportRect()?.let { imageInlineOriginRect = it }
        overlayImageResetTrigger++
        isImageFullscreenOpen = false
    }

    BackHandler(enabled = isImageFullscreenOpen) {
        closeImageFullscreen()
    }

    fun triggerSmoothDismiss() {
        if (isDismissing) return
        isDismissing = true
        isImageFullscreenOpen = false
        wantsVideoFullscreen = false
        isExitingVideoFullscreen = false
        viewModel.stopLiveStream()
        scope.launch {
            sheetOffsetY.animateTo(
                targetValue = screenHeightPx,
                animationSpec = tween(durationMillis = 300, easing = StandardEasing)
            )
            onSwipeDismissed()
        }
    }

    // Chỉ đổi shape khi sheet bắt đầu/kết thúc bị kéo – không recompose theo từng pixel kéo
    val isSheetDragged by remember(sheetOffsetY) {
        derivedStateOf { sheetOffsetY.value > SHEET_DRAGGED_THRESHOLD_PX }
    }
    val sheetShape = if (isSheetDragged) {
        RoundedCornerShape(topStart = spacing.radiusLarge, topEnd = spacing.radiusLarge)
    } else {
        RectangleShape
    }

    /** Chuyển sang xem 1 sự kiện khác: về tab ảnh & đóng popup ảnh minh chứng */
    fun resetMediaState() {
        selectedTab = EventMediaTab.EVENT_IMAGE
        isMediaZoomed = false
        isImageFullscreenOpen = false
        wantsVideoFullscreen = false
        isExitingVideoFullscreen = false
        expandedCropUrl = null
        showPeriodMenu = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                // Đọc offset trong pha draw → kéo sheet chỉ vẽ lại lớp scrim, không recompose
                val dragProgress = (sheetOffsetY.value / screenHeightPx).coerceIn(0f, 1f)
                drawRect(DismissScrimColor, alpha = DISMISS_SCRIM_MAX_ALPHA * (1f - dragProgress))
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { bounds.sheetRootCoords = it }
                .graphicsLayer {
                    translationY = if (isMediaFullscreen) 0f else sheetOffsetY.value
                }
                .clip(if (isMediaFullscreen) RectangleShape else sheetShape)
                .background(if (isMediaFullscreen) Color.Black else colors.pageBg),
            contentAlignment = Alignment.TopCenter
        ) {
            val event = uiState.event
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    WaveDotsLoading()
                }
            } else if (uiState.error != null || event == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CommonEmptyState(text = uiState.error ?: if (isEn) "Event not found" else "Không tìm thấy sự kiện")
                    // Close button even on error state
                    SheetCloseButton(
                        onClick = onBack,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .statusBarsPadding()
                            .padding(top = spacing.space12, end = spacing.space14)
                    )
                }
            } else {
                val baseEvent = uiState.baseEvent
                val camera = uiState.camera
                val aiService = remember(uiState.aiServices, event.serviceId) {
                    uiState.aiServices.find { it.id == event.serviceId }
                }
                val display = remember(event, camera, aiService, uiState.profileList, uiState.profileGroupList, isEn) {
                    buildEventDetailDisplay(
                        event = event,
                        camera = camera,
                        aiService = aiService,
                        profiles = uiState.profileList,
                        groups = uiState.profileGroupList,
                        isEn = isEn
                    )
                }
                val cameraMap = if (uiState.cameraNameMap.isNotEmpty()) {
                    uiState.cameraNameMap
                } else {
                    remember(uiState.cameraList) { buildCameraNameMap(uiState.cameraList) }
                }

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .then(
                            if (isMediaFullscreen) {
                                Modifier.fillMaxWidth()
                            } else {
                                Modifier
                                    .widthIn(max = spacing.contentMaxWidth)
                                    .fillMaxWidth()
                                    .statusBarsPadding()
                            }
                        )
                        .onGloballyPositioned { coords ->
                            bounds.rootTopY = coords.positionInRoot().y
                        }
                        .pointerInput(eventId) {
                            awaitEachGesture {
                                val down = awaitFirstDown(
                                    requireUnconsumed = false,
                                    pass = PointerEventPass.Initial
                                )
                                if (currentIsMediaFullscreen) {
                                    return@awaitEachGesture
                                }

                                val startX = down.position.x
                                val startY = down.position.y
                                val downRootY = bounds.rootTopY + startY
                                val startTimeMs = SystemClock.uptimeMillis()
                                val pinnedBottomY = bounds.pinnedTopBottomY

                                val isInsidePinnedTop =
                                    pinnedBottomY > 0f && downRootY <= pinnedBottomY
                                val hasVideoSeekbar =
                                    selectedTab == EventMediaTab.EVENT_VIDEO
                                val isInsideVideoSeekbarZone =
                                    hasVideoSeekbar &&
                                        pinnedBottomY > 0f &&
                                        downRootY in (pinnedBottomY - videoControllerZoneHeightPx)..pinnedBottomY
                                val isDataBoardVisible =
                                    listState.layoutInfo.visibleItemsInfo.any {
                                        it.key == KEY_DATA_BOARD || it.key == KEY_INFO_HEADER
                                    }
                                val isInsideDataBoard =
                                    isDataBoardVisible &&
                                        pinnedBottomY > 0f &&
                                        bounds.dataBoardBottomY > pinnedBottomY &&
                                        downRootY > pinnedBottomY &&
                                        downRootY <= bounds.dataBoardBottomY

                                val isBlockedByMediaZoom = currentIsMediaZoomed && isInsidePinnedTop

                                // Horizontal swipe to switch media tabs: works across the screen except the video player seekbar or zoomed media
                                var canSwipeTab = !isInsideVideoSeekbarZone && !isBlockedByMediaZoom

                                // Vertical swipe-down to dismiss:
                                // - Allowed on Pinned Top Section (Header + Tabs + 16:9 Media area) unless zoomed in
                                // - Allowed on THÔNG TIN SỰ KIỆN section (header + .event-data-board card)
                                // - NEVER allowed when touching inside LỊCH SỬ XUẤT HIỆN (related events list)
                                var canSwipeDismiss =
                                    !isInsideVideoSeekbarZone &&
                                        !isBlockedByMediaZoom &&
                                        (isInsidePinnedTop || isInsideDataBoard)

                                var lockedMode = 0 // 0 = NONE, 1 = HORIZONTAL_TAB, 2 = VERTICAL_DISMISS
                                var lastDx = 0f
                                var lastDy = 0f
                                val pointerId = down.id

                                while (true) {
                                    val ptrEvent = awaitPointerEvent(PointerEventPass.Initial)
                                    // Ngay khi người dùng chạm ngón thứ 2 (Pinch-to-Zoom), hủy ngay cử chỉ 1 ngón
                                    if (ptrEvent.changes.count { it.pressed } > 1 || currentIsMediaFullscreen) {
                                        lockedMode = 0
                                        if (sheetOffsetY.value > 0f) {
                                            scope.launch {
                                                sheetOffsetY.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                                            }
                                        }
                                        break
                                    }
                                    val change = ptrEvent.changes.firstOrNull { it.id == pointerId }
                                        ?: ptrEvent.changes.firstOrNull()
                                        ?: break
                                    if (!change.pressed) {
                                        break
                                    }

                                    val dx = change.position.x - startX
                                    val dy = change.position.y - startY
                                    val absX = abs(dx)
                                    val absY = abs(dy)
                                    lastDx = dx
                                    lastDy = dy

                                    // If user drags finger upward (scrolling down the page), disable swipe-to-dismiss
                                    // so LazyColumn can scroll down to LỊCH SỬ XUẤT HIỆN normally
                                    if (lockedMode == 0 && dy < -preConsumeVerticalSlopPx) {
                                        canSwipeDismiss = false
                                    }

                                    if (lockedMode == 0) {
                                        if (canSwipeTab && absX > horizontalLockSlopPx && absX > absY * 1.3f) {
                                            lockedMode = 1 // HORIZONTAL_TAB
                                            canSwipeDismiss = false
                                            change.consume()
                                        } else if (canSwipeDismiss && dy > verticalLockSlopPx && dy > absX * 1.35f) {
                                            lockedMode = 2 // VERTICAL_DISMISS
                                            canSwipeTab = false
                                            change.consume()
                                            val dragOffset = ((dy - verticalLockSlopPx) * SHEET_DRAG_RESISTANCE).coerceAtLeast(0f)
                                            scope.launch { sheetOffsetY.snapTo(dragOffset) }
                                        } else if (canSwipeDismiss && dy > preConsumeVerticalSlopPx && dy > absX * 1.25f) {
                                            // Pre-consume downward drag in Initial pass before LazyColumn's 8dp touchSlop
                                            // on Main pass can intercept the gesture inside THÔNG TIN SỰ KIỆN
                                            change.consume()
                                        }
                                    } else if (lockedMode == 1) {
                                        change.consume()
                                    } else if (lockedMode == 2) {
                                        change.consume()
                                        val dragOffset = ((dy - verticalLockSlopPx) * SHEET_DRAG_RESISTANCE).coerceAtLeast(0f)
                                        scope.launch { sheetOffsetY.snapTo(dragOffset) }
                                    }
                                }

                                val elapsedMs = (SystemClock.uptimeMillis() - startTimeMs).coerceAtLeast(1L)

                                if (lockedMode == 1) {
                                    val absXDp = abs(lastDx).toDp().value
                                    val isThresholdMet = absXDp >= TAB_SWIPE_DISTANCE_DP ||
                                        (absXDp >= TAB_FLICK_DISTANCE_DP && elapsedMs < TAB_FLICK_MAX_MS)
                                    val now = SystemClock.uptimeMillis()
                                    if (isThresholdMet && (now - bounds.lastTabSwipeTimeMs >= TAB_SWIPE_COOLDOWN_MS)) {
                                        bounds.lastTabSwipeTimeMs = now
                                        val curIdx = MediaTabs.indexOf(selectedTab)
                                        val nextIdx = when {
                                            lastDx < 0f && curIdx < MediaTabs.lastIndex -> curIdx + 1
                                            lastDx > 0f && curIdx > 0 -> curIdx - 1
                                            else -> -1
                                        }
                                        if (nextIdx >= 0) {
                                            runCatching { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) }
                                            selectedTab = MediaTabs[nextIdx]
                                        }
                                    }
                                } else if (lockedMode == 2) {
                                    val pulledDp = sheetOffsetY.value.toDp().value
                                    val totalDyDp = lastDy.toDp().value
                                    val velocityDpPerMs = totalDyDp / elapsedMs.toFloat()
                                    val shouldDismiss = pulledDp >= DISMISS_DISTANCE_DP ||
                                        (pulledDp >= DISMISS_FLICK_DISTANCE_DP &&
                                            velocityDpPerMs >= DISMISS_FLICK_VELOCITY_DP_PER_MS &&
                                            elapsedMs < DISMISS_FLICK_MAX_MS)
                                    if (shouldDismiss) {
                                        triggerSmoothDismiss()
                                    } else {
                                        scope.launch {
                                            sheetOffsetY.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                                        }
                                    }
                                } else if (sheetOffsetY.value > 0f) {
                                    scope.launch {
                                        sheetOffsetY.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                                    }
                                }
                            }
                        }
                ) {
                    // =========================================================================
                    // 1. PINNED TOP SECTION (.media-header-pinned-section in BaseAiEventDetailModal.vue)
                    //    Giữ MediaViewport ở đúng vị trí cây Compose để xoay ngang Fullscreen
                    //    không bao giờ khởi tạo lại ExoPlayer / LiveStreamPlayer (0ms reload).
                    // =========================================================================
                    Column(
                        modifier = (if (isMediaFullscreen) Modifier.fillMaxSize() else Modifier.fillMaxWidth())
                            .background(if (isMediaFullscreen) Color.Black else colors.pageBg)
                            .onGloballyPositioned { coords ->
                                bounds.pinnedTopBottomY = coords.positionInRoot().y + coords.size.height
                            }
                    ) {
                        if (!isMediaFullscreen) {
                            // Header: Centered AI Task Title + Top-right Circular X Button (.dashboard-header)
                            DetailHeader(
                                title = display.aiTaskTitle,
                                showBackToOriginal = baseEvent != null && baseEvent.id != event.id,
                                isEn = isEn,
                                onBackToOriginal = {
                                    if (baseEvent != null) {
                                        viewModel.selectEvent(baseEvent)
                                        resetMediaState()
                                    }
                                },
                                onClose = handleBack
                            )

                            // 3-Segment Media Tabs (.custom-tabs .ant-tabs-nav-list) with Smooth Sliding Pill Indicator
                            MediaTabSelector(
                                selectedTab = selectedTab,
                                isEn = isEn,
                                onSelect = { selectedTab = it }
                            )
                        }

                        // Full-bleed 16:9 Media Viewport (hoặc Fullscreen Landscape khi phóng to toàn màn hình)
                        MediaViewport(
                            selectedTab = selectedTab,
                            isScreenActive = !isClosing && !isDismissing,
                            isFullscreen = isMediaFullscreen,
                            isImageHiddenForOverlay = isImageOverlayActive,
                            inlineImageResetTrigger = inlineImageResetTrigger,
                            eventId = event.id,
                            cameraId = camera?.id ?: 0,
                            hasSubStream = camera?.sub?.isNotBlank() == true,
                            display = display,
                            liveStreamLoading = uiState.liveStreamLoading,
                            liveStreamError = uiState.liveStreamError,
                            liveStreamUrl = uiState.liveStreamUrl,
                            liveStreamChannel = uiState.liveStreamChannel,
                            liveCameraSnapshotUrl = uiState.liveCameraSnapshotUrl,
                            isEn = isEn,
                            modifier = Modifier.onGloballyPositioned { coords ->
                                bounds.mediaViewportCoords = coords
                            },
                            onToggleFullscreen = {
                                if (selectedTab == EventMediaTab.EVENT_IMAGE) {
                                    if (isImageFullscreenOpen) {
                                        closeImageFullscreen()
                                    } else {
                                        openImageFullscreen()
                                    }
                                } else {
                                    if (wantsVideoFullscreen) {
                                        isExitingVideoFullscreen = true
                                        wantsVideoFullscreen = false
                                    } else {
                                        isExitingVideoFullscreen = false
                                        wantsVideoFullscreen = true
                                    }
                                }
                            },
                            onZoomChanged = { zoomed -> isMediaZoomed = zoomed },
                            onLoadLiveStream = viewModel::loadLiveStream,
                            onSwitchLiveChannel = viewModel::switchLiveChannel,
                            onLiveStreamFirstFrame = viewModel::onLiveStreamFirstFrame,
                            onRetryLiveStream = viewModel::retryLiveStream,
                            onStopLiveStream = viewModel::stopLiveStream
                        )

                        if (!isMediaFullscreen) {
                            Spacer(modifier = Modifier.height(spacing.space6))
                        }
                    }

                    // =========================================================================
                    // 2. SCROLLABLE BOTTOM CONTAINER (.event-info-scroll-container in PWA)
                    // =========================================================================
                    if (!isMediaFullscreen) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(bottom = spacing.space32 + spacing.space4)
                    ) {
                        // Section Title: THÔNG TIN SỰ KIỆN
                        item(key = KEY_INFO_HEADER, contentType = CONTENT_TYPE_SECTION_HEADER) {
                            SectionHeaderText(
                                text = stringResource(id = R.string.detail_header_info).uppercase(),
                                modifier = Modifier.padding(
                                    start = spacing.space24,
                                    end = spacing.space14,
                                    top = spacing.space16,
                                    bottom = spacing.space8
                                )
                            )
                        }

                        // Card: .event-data-board
                        item(key = KEY_DATA_BOARD) {
                            SectionCard(
                                modifier = Modifier.onGloballyPositioned { coords ->
                                    bounds.dataBoardBottomY = coords.positionInRoot().y + coords.size.height
                                }
                            ) {
                                EventInfoBoard(
                                    eventId = event.id,
                                    selectedTab = selectedTab,
                                    display = display,
                                    isEn = isEn,
                                    isRelatedPlatesOpen = uiState.showRelatedPlates,
                                    expandedCropUrl = expandedCropUrl,
                                    onCropClick = { url -> expandedCropUrl = if (expandedCropUrl == url) null else url },
                                    onCropDismiss = { expandedCropUrl = null },
                                    onToggleRelatedPlates = viewModel::toggleRelatedLicensePlates,
                                    onReport = { showConfirmReportDialog = true }
                                )
                            }
                        }

                        // =====================================================================
                        // Optional Section: BIỂN SỐ LIÊN QUAN (.related-plates-section in PWA)
                        // =====================================================================
                        if (uiState.showRelatedPlates) {
                            item(key = KEY_RELATED_PLATES) {
                                RelatedPlatesSection(
                                    plates = uiState.licensePlateEvents,
                                    isLoading = uiState.relatedPlatesLoading,
                                    activeEventId = event.id,
                                    isEn = isEn,
                                    onClose = viewModel::closeRelatedLicensePlates,
                                    onSelect = { plateEv ->
                                        viewModel.selectEvent(plateEv)
                                        resetMediaState()
                                    }
                                )
                            }
                        }

                        // =====================================================================
                        // 3. LỊCH SỬ XUẤT HIỆN (.mobile-history-sidebar in PWA)
                        // =====================================================================
                        item(key = KEY_HISTORY_HEADER) {
                            val periodDisplayLabel = remember(uiState.selectedJourneyPeriod, customPickerVersion, isEn) {
                                uiState.selectedJourneyPeriod.formatDisplayLabel(isEn)
                            }
                            HistoryHeader(
                                periodLabel = periodDisplayLabel,
                                isExpanded = showPeriodMenu,
                                isEn = isEn,
                                anchorModifier = Modifier.onGloballyPositioned { coords ->
                                    bounds.periodRowCoords = coords
                                    if (showPeriodMenu) {
                                        val root = bounds.sheetRootCoords
                                        if (root != null && root.isAttached && coords.isAttached) {
                                            val rect = root.localBoundingBoxOf(coords, clipBounds = false)
                                            if (periodAnchorRect != rect) periodAnchorRect = rect
                                        }
                                    }
                                },
                                onToggleMenu = {
                                    val nextOpen = !showPeriodMenu
                                    if (nextOpen) {
                                        val root = bounds.sheetRootCoords
                                        val row = bounds.periodRowCoords
                                        periodAnchorRect = if (root != null && row != null && root.isAttached && row.isAttached) {
                                            root.localBoundingBoxOf(row, clipBounds = false)
                                        } else {
                                            null
                                        }
                                    }
                                    showPeriodMenu = nextOpen && periodAnchorRect != null
                                }
                            )
                        }

                        // History List Content
                        if (uiState.isJourneyLoading) {
                            item(key = KEY_HISTORY_LOADING, contentType = CONTENT_TYPE_STATE) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(HistoryLoadingHeight)
                                        .padding(horizontal = spacing.space10),
                                    contentAlignment = Alignment.Center
                                ) {
                                    WaveDotsLoading()
                                }
                            }
                        } else if (uiState.relatedEvents.isEmpty()) {
                            item(key = KEY_HISTORY_EMPTY, contentType = CONTENT_TYPE_STATE) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = spacing.space10)
                                        .height(HistoryEmptyHeight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CommonEmptyState(text = if (isEn) "No related events" else "Không có dữ liệu")
                                }
                            }
                        } else {
                            // Danh sách đã được ViewModel loại trùng → key ổn định theo sự kiện (không phụ thuộc vị trí)
                            items(
                                items = uiState.relatedEvents,
                                key = { relEv -> "hist_${relEv.uniqueKey()}" },
                                contentType = { CONTENT_TYPE_HISTORY_CARD }
                            ) { relEv ->
                                val relCamName = relEv.cameraUuidOrNull?.let { cameraMap[it] }
                                    ?: camera?.name
                                    ?: "Camera"

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = spacing.space10, vertical = spacing.space5)
                                ) {
                                    MobileEventCard(
                                        event = relEv,
                                        cameraName = relCamName,
                                        isListView = true,
                                        isSelected = relEv.id == event.id,
                                        enableSharedElement = false,
                                        onClick = {
                                            viewModel.selectBaseEvent(relEv)
                                            resetMediaState()
                                        }
                                    )
                                }
                            }

                            // End of history message (.mobile-history-end-message in PWA)
                            item(key = KEY_HISTORY_END, contentType = CONTENT_TYPE_STATE) {
                                HistoryEndMessage(
                                    text = if (isEn) "All events displayed" else "Đã hiển thị tất cả sự kiện"
                                )
                            }
                        }
                    }
                    }
                }

                // Anchored Period Dropdown Menu dùng chung chính hàm AnchoredFilterDropdownMenu với MobileFilterBottomSheet
                // (kế thừa đúng responsiveDensity, tự mở lên trên khi ở sát đáy màn hình, không bao giờ đè lên ô chọn)
                val currentAnchorRect = periodAnchorRect
                val rawRootHeightPx = bounds.sheetRootCoords?.size?.height?.toFloat() ?: 0f
                val navBarBottomPx = WindowInsets.navigationBars.getBottom(density).toFloat()
                val usableRootHeightPx = (rawRootHeightPx - navBarBottomPx).coerceAtLeast(0f)
                if (showPeriodMenu && currentAnchorRect != null && usableRootHeightPx > 0f) {
                    val periodOptions = remember(isEn) {
                        JourneyPeriod.entries.map { period -> period to period.label(isEn) }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { showPeriodMenu = false }
                            )
                    ) {
                        AnchoredFilterDropdownMenu(
                            anchorRect = currentAnchorRect,
                            rootHeightPx = usableRootHeightPx,
                            options = periodOptions,
                            selectedKey = uiState.selectedJourneyPeriod,
                            onSelect = { period ->
                                showPeriodMenu = false
                                when (period) {
                                    JourneyPeriod.CUSTOM_DATE -> showCustomDateModal = true
                                    JourneyPeriod.CUSTOM_RANGE -> showCustomRangeModal = true
                                    else -> viewModel.setJourneyPeriod(period)
                                }
                            }
                        )
                    }
                }

                // Lớp phủ phóng to ngang mượt 120Hz cho Ảnh sự kiện (xoay trực tiếp từ khung 16:9 và thu thẳng về chỗ cũ)
                if (isImageOverlayActive && !display.imageUrl.isNullOrEmpty()) {
                    FullscreenEventImageOverlay(
                        imageUrl = display.imageUrl,
                        boxes = display.boxes,
                        isOpen = isImageFullscreenOpen,
                        progressProvider = { imageFullscreenProgress.value },
                        originRect = imageInlineOriginRect,
                        smoothResetTrigger = overlayImageResetTrigger,
                        isEn = isEn,
                        onRequestClose = ::closeImageFullscreen
                    )
                }
            }
        }
    }

    if (showCustomDateModal) {
        CustomDatePickerDialogModal(
            initialMillis = TimeFilter.customDateStartMs,
            isEn = isEn,
            isDark = colors.isDark,
            onDismiss = { showCustomDateModal = false },
            onConfirm = { pickedMillis ->
                TimeFilter.setCustomDate(pickedMillis)
                customPickerVersion++
                showCustomDateModal = false
                viewModel.setJourneyPeriod(JourneyPeriod.CUSTOM_DATE)
            }
        )
    }

    if (showCustomRangeModal) {
        CustomRangePickerDialogModal(
            initialFromMillis = TimeFilter.customRangeFromMs,
            initialToMillis = TimeFilter.customRangeToMs,
            isEn = isEn,
            isDark = colors.isDark,
            onDismiss = { showCustomRangeModal = false },
            onConfirm = { fromMillis, toMillis ->
                TimeFilter.setCustomRange(fromMillis, toMillis)
                customPickerVersion++
                showCustomRangeModal = false
                viewModel.setJourneyPeriod(JourneyPeriod.CUSTOM_RANGE)
            }
        )
    }

    if (showConfirmReportDialog) {
        AppConfirmDialog(
            title = if (isEn) "Report event" else "Báo cáo sự kiện",
            headline = if (isEn) "Report this event as false?" else "Báo cáo sự kiện sai?",
            message = if (isEn) "Are you sure you want to report this event as false?" else "Bạn có chắc chắn muốn báo cáo sự kiện này là sai?",
            confirmText = if (isEn) "Report" else "Báo cáo",
            cancelText = stringResource(id = R.string.dialog_btn_cancel),
            onDismiss = { showConfirmReportDialog = false },
            onConfirm = {
                showConfirmReportDialog = false
                val targetId = uiState.event?.id ?: eventId
                viewModel.reportEventAsFalse(
                    eventId = targetId,
                    onSuccess = {
                        MobileTopPopupManager.success(
                            if (isEn) "Reported false event successfully" else "Đã báo cáo sự kiện sai"
                        )
                        onBack()
                    },
                    onError = { errMsg -> MobileTopPopupManager.error(errMsg) }
                )
            }
        )
    }
}

// LazyColumn keys & content types
private const val KEY_INFO_HEADER = "section_event_info_header"
private const val KEY_DATA_BOARD = "section_event_data_board"
private const val KEY_RELATED_PLATES = "section_related_plates"
private const val KEY_HISTORY_HEADER = "section_history_header_and_filter"
private const val KEY_HISTORY_LOADING = "section_history_loading"
private const val KEY_HISTORY_EMPTY = "section_history_empty"
private const val KEY_HISTORY_END = "section_history_end_msg"
private const val CONTENT_TYPE_SECTION_HEADER = "section_header"
private const val CONTENT_TYPE_STATE = "state"
private const val CONTENT_TYPE_HISTORY_CARD = "history_event_card"

// =============================================================================================
// Pinned top section
// =============================================================================================

@Composable
private fun DetailHeader(
    title: String,
    showBackToOriginal: Boolean,
    isEn: Boolean,
    onBackToOriginal: () -> Unit,
    onClose: () -> Unit
) {
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.appColors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = spacing.space12, bottom = spacing.space6, start = spacing.space14, end = spacing.space14)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // Chừa đều 2 bên theo kích thước nút đóng (avatarSmall) bên phải để tiêu đề luôn căn chính giữa màn hình
                .padding(horizontal = spacing.avatarSmall + spacing.space6)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.space4)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.textStrong,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.basicMarquee(
                    iterations = Int.MAX_VALUE,
                    initialDelayMillis = 1200,
                    repeatDelayMillis = 1600,
                    velocity = 28.dp
                )
            )

            if (showBackToOriginal) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(spacing.radiusTiny))
                        .background(BrandPrimary)
                        .clickable(onClick = onBackToOriginal)
                        .padding(horizontal = spacing.space8, vertical = spacing.space3),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.space4)
                ) {
                    Icon(
                        imageVector = LucideIcons.ArrowLeft,
                        contentDescription = null,
                        tint = colors.onAccent,
                        modifier = Modifier.size(spacing.iconTiny)
                    )
                    Text(
                        text = if (isEn) "Back to original" else "Về sự kiện gốc",
                        color = colors.onAccent,
                        style = MaterialTheme.typography.caption,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }
        }

        // Circular Close Button chuẩn hóa giống nút đóng trong bottom sheet
        SheetCloseButton(onClick = onClose, modifier = Modifier.align(Alignment.CenterEnd))
    }
}

@Composable
private fun MediaTabSelector(
    selectedTab: EventMediaTab,
    isEn: Boolean,
    onSelect: (EventMediaTab) -> Unit
) {
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.appColors
    val tabsTrackBg = if (colors.isDark) colors.controlBg else colors.surface
    val activePillBg = if (colors.isDark) SurfaceVariantDark else SurfaceVariant
    val pillShape = RoundedCornerShape(spacing.radiusSmall)
    val selectedTabIndex = MediaTabs.indexOf(selectedTab).coerceAtLeast(0)

    val pillIndex by animateFloatAsState(
        targetValue = selectedTabIndex.toFloat(),
        animationSpec = tween(durationMillis = 260, easing = SegmentEasing),
        label = "mediaSegmentIndicator"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = spacing.space10, end = spacing.space10, top = spacing.space6, bottom = spacing.space12)
            .height(segmentControlHeight())
            .clip(RoundedCornerShape(spacing.radiusCompact))
            .background(tabsTrackBg)
            .padding(spacing.space3)
    ) {
        // Sliding Active Pill – dịch chuyển trực tiếp trên GPU RenderThread, không dùng BoxWithConstraints (SubcomposeLayout)
        Box(
            modifier = Modifier
                .fillMaxWidth(1f / MediaTabs.size)
                .fillMaxHeight()
                .graphicsLayer {
                    translationX = pillIndex * size.width
                }
                .shadow(
                    elevation = spacing.space2,
                    shape = pillShape,
                    ambientColor = PillShadowColor,
                    spotColor = PillShadowColor
                )
                .clip(pillShape)
                .background(activePillBg)
        )

        val currentDensity = LocalDensity.current
        val clampedDensity = remember(currentDensity) {
            Density(
                density = currentDensity.density,
                fontScale = currentDensity.fontScale.coerceAtMost(1.05f)
            )
        }

        CompositionLocalProvider(LocalDensity provides clampedDensity) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MediaTabs.forEach { tab ->
                    val isSelected = selectedTab == tab
                    val tabTextColor by animateColorAsState(
                        targetValue = if (isSelected) colors.accent else Slate500,
                        animationSpec = tween(durationMillis = 180),
                        label = "mediaTabTextColor"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(pillShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onSelect(tab) }
                            .padding(horizontal = spacing.space4),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isEn) tab.titleEn else tab.titleVi,
                            color = tabTextColor,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@Composable
private fun LandscapeFullscreenEffect(
    isFullscreen: Boolean,
    onExitFullscreen: () -> Unit
) {
    val context = LocalContext.current
    BackHandler(enabled = isFullscreen) {
        onExitFullscreen()
    }
    LaunchedEffect(isFullscreen) {
        val activity = context.findActivity() ?: return@LaunchedEffect
        val window = activity.window
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        if (isFullscreen) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            val activity = context.findActivity() ?: return@onDispose
            val window = activity.window
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}

@Composable
private fun MediaViewport(
    selectedTab: EventMediaTab,
    isScreenActive: Boolean,
    isFullscreen: Boolean,
    isImageHiddenForOverlay: Boolean = false,
    inlineImageResetTrigger: Int = 0,
    eventId: Int,
    cameraId: Int,
    hasSubStream: Boolean,
    display: EventDetailDisplay,
    liveStreamLoading: Boolean,
    liveStreamError: String?,
    liveStreamUrl: String?,
    liveStreamChannel: Int,
    liveCameraSnapshotUrl: String?,
    isEn: Boolean,
    modifier: Modifier = Modifier,
    onToggleFullscreen: () -> Unit,
    onZoomChanged: (Boolean) -> Unit,
    onLoadLiveStream: (Boolean) -> Unit,
    onSwitchLiveChannel: (Int) -> Unit,
    onLiveStreamFirstFrame: () -> Unit,
    onRetryLiveStream: () -> Unit,
    onStopLiveStream: () -> Unit
) {
    val viewportBg = if (isFullscreen) Color.Black else MaterialTheme.appColors.surface
    Box(
        modifier = modifier
            .then(
                if (isFullscreen) {
                    Modifier.fillMaxSize()
                } else {
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                }
            )
            .background(viewportBg),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = selectedTab,
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds(),
            transitionSpec = {
                val isForward = MediaTabs.indexOf(targetState) >= MediaTabs.indexOf(initialState)
                val enterSlide = slideInHorizontally(
                    animationSpec = tween(durationMillis = 250, easing = SlideEasing),
                    initialOffsetX = { fullWidth -> if (isForward) fullWidth else -fullWidth }
                )
                val exitSlide = slideOutHorizontally(
                    animationSpec = tween(durationMillis = 250, easing = SlideEasing),
                    targetOffsetX = { fullWidth -> if (isForward) -fullWidth else fullWidth }
                )
                enterSlide togetherWith exitSlide
            },
            label = "mediaViewportTransition"
        ) { activeMediaTab ->
            val isTabActive = isScreenActive && activeMediaTab == selectedTab
            when (activeMediaTab) {
                EventMediaTab.LIVE_CAMERA -> LiveCameraPane(
                    eventId = eventId,
                    cameraId = cameraId,
                    cameraName = display.locationText,
                    hasSubStream = hasSubStream,
                    channel = liveStreamChannel,
                    snapshotUrl = display.imageUrl ?: liveCameraSnapshotUrl,
                    isCameraFallbackSnapshot = display.imageUrl.isNullOrEmpty(),
                    isLoading = liveStreamLoading,
                    error = liveStreamError,
                    streamUrl = liveStreamUrl,
                    isActive = isTabActive,
                    isFullscreen = isFullscreen,
                    isEn = isEn,
                    onToggleFullscreen = onToggleFullscreen,
                    onZoomChanged = onZoomChanged,
                    onLoad = onLoadLiveStream,
                    onSwitchChannel = onSwitchLiveChannel,
                    onFirstFrame = onLiveStreamFirstFrame,
                    onRetry = onRetryLiveStream,
                    onStop = onStopLiveStream
                )

                EventMediaTab.EVENT_IMAGE -> EventImageWithBoxes(
                    imageUrl = display.imageUrl,
                    boxes = display.boxes,
                    isFullscreen = isFullscreen,
                    isHiddenForOverlay = isImageHiddenForOverlay,
                    smoothResetTrigger = inlineImageResetTrigger,
                    isEn = isEn,
                    onToggleFullscreen = onToggleFullscreen,
                    onZoomChanged = onZoomChanged
                )

                EventMediaTab.EVENT_VIDEO -> {
                    val noVideoText = if (isEn) "No event video" else "Không có video sự kiện"
                    val videoUrl = display.videoUrl
                    if (videoUrl != null) {
                        VideoPlayer(
                            videoUrl = videoUrl,
                            thumbnailUrl = display.imageUrl,
                            deviceKey = UserSession.accessToken,
                            isOriginalRatio = isFullscreen,
                            isFullscreen = isFullscreen,
                            playWhenReady = isTabActive,
                            errorText = noVideoText,
                            onFullscreenToggle = onToggleFullscreen,
                            enableZoom = true,
                            onZoomChanged = onZoomChanged
                        )
                    } else {
                        CommonEmptyState(text = noVideoText, modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveCameraPane(
    eventId: Int,
    cameraId: Int,
    cameraName: String,
    hasSubStream: Boolean,
    channel: Int,
    snapshotUrl: String?,
    isCameraFallbackSnapshot: Boolean = false,
    isLoading: Boolean,
    error: String?,
    streamUrl: String?,
    isActive: Boolean,
    isFullscreen: Boolean,
    isEn: Boolean,
    onToggleFullscreen: () -> Unit,
    onZoomChanged: (Boolean) -> Unit,
    onLoad: (Boolean) -> Unit,
    onSwitchChannel: (Int) -> Unit,
    onFirstFrame: () -> Unit,
    onRetry: () -> Unit,
    onStop: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    var isPlaying by remember(cameraId, streamUrl) { mutableStateOf(false) }
    var isOriginalRatio by remember(cameraId) { mutableStateOf(false) }
    var showInfo by remember(cameraId) { mutableStateOf(false) }
    var showChannelDropdown by remember(cameraId) { mutableStateOf(false) }
    var autoHideToken by remember(cameraId) { mutableIntStateOf(0) }
    val currentIsActive by rememberUpdatedState(isActive)
    val currentOnLoad by rememberUpdatedState(onLoad)
    val currentOnStop by rememberUpdatedState(onStop)

    LaunchedEffect(eventId, cameraId, isActive) {
        if (isActive) {
            onLoad(false)
        } else {
            onStop()
        }
    }
    DisposableEffect(lifecycleOwner) {
        var stoppedInBackground = false
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_STOP -> {
                    stoppedInBackground = true
                    currentOnStop()
                }
                androidx.lifecycle.Lifecycle.Event.ON_START -> {
                    if (stoppedInBackground) {
                        stoppedInBackground = false
                        if (currentIsActive) {
                            currentOnLoad(true)
                        }
                    }
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (currentIsActive) {
                currentOnStop()
            }
        }
    }
    LaunchedEffect(showInfo, autoHideToken, isPlaying) {
        if (showInfo && isPlaying) {
            delay(3000L)
            showChannelDropdown = false
            showInfo = false
        }
    }

    ZoomableMediaBox(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isFullscreen) Color.Black else colors.surface),
        zoomResetKey = "$eventId-$cameraId-$channel",
        enableDoubleTapZoom = true,
        badgeAlignment = Alignment.BottomStart,
        badgePadding = PaddingValues(spacing.space8),
        onZoomChanged = onZoomChanged,
        onTap = {
            if (error != null) {
                onLoad(true)
            } else if (isPlaying) {
                val nextShow = !showInfo
                showInfo = nextShow
                if (!nextShow) {
                    showChannelDropdown = false
                } else {
                    autoHideToken++
                }
            }
        },
        overlay = { _, _ ->
            if (error != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (isFullscreen) Color.Black else colors.surface)
                        .clickable { onLoad(true) },
                    contentAlignment = Alignment.Center
                ) {
                    CommonEmptyState(text = error, modifier = Modifier.fillMaxSize())
                }
                if (isFullscreen) {
                    MediaFullscreenFloatingButton(
                        isFullscreen = true,
                        isEn = isEn,
                        onClick = onToggleFullscreen,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(spacing.space10)
                    )
                }
            }

            // Top-Left Camera Name Pill & Top-Right Stream Controls (đồng bộ với LiveCameraView ở màn Giám sát)
            AnimatedVisibility(
                visible = showInfo && isPlaying && error == null,
                enter = fadeIn(animationSpec = tween(200)),
                exit = fadeOut(animationSpec = tween(200)),
                modifier = Modifier.fillMaxSize()
            ) {
                val overlayPillBg = colors.mediaBg.copy(alpha = 0.42f)
                val overlayPillBorder = colors.onMedia.copy(alpha = 0.12f)
                val pillHeight = spacing.iconButtonSmall
                val actionBtnSize = spacing.iconMedium
                val actionIconSize = spacing.iconSmall
                val textStyle = MaterialTheme.typography.labelMedium

                val currentStreamLabel = if (channel == 1) {
                    if (isEn) "Sub Stream" else "Luồng phụ"
                } else {
                    if (isEn) "Main Stream" else "Luồng chính"
                }
                val alternateStreamLabel = if (channel == 0) {
                    if (isEn) "Sub Stream" else "Luồng phụ"
                } else {
                    if (isEn) "Main Stream" else "Luồng chính"
                }
                val alternateChannel = if (channel == 0) 1 else 0

                Box(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .padding(spacing.space8),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        // Camera Name Pill (Top-Left)
                        if (cameraName.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .padding(end = spacing.space8)
                                    .height(pillHeight)
                                    .clip(RoundedCornerShape(spacing.radiusSmall))
                                    .background(overlayPillBg)
                                    .border(spacing.borderMedium, overlayPillBorder, RoundedCornerShape(spacing.radiusSmall))
                                    .padding(horizontal = spacing.space8),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = cameraName,
                                    color = colors.onMedia,
                                    style = textStyle,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(spacing.space1))
                        }

                        // Action Controls Pill + Channel Dropdown (Top-Right)
                        Column(horizontalAlignment = Alignment.Start) {
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
                                // 1. Channel selector (Luồng chính / Luồng phụ)
                                Row(
                                    modifier = Modifier
                                        .height(actionBtnSize)
                                        .clip(RoundedCornerShape(spacing.radiusTiny))
                                        .background(
                                            if (showChannelDropdown && hasSubStream) {
                                                colors.onMedia.copy(alpha = 0.2f)
                                            } else {
                                                Color.Transparent
                                            }
                                        )
                                        .alpha(if (hasSubStream) 1f else 0.65f)
                                        .then(
                                            if (hasSubStream) {
                                                Modifier.clickable {
                                                    showChannelDropdown = !showChannelDropdown
                                                    autoHideToken++
                                                }
                                            } else {
                                                Modifier
                                            }
                                        )
                                        .padding(horizontal = spacing.space8),
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
                                    if (hasSubStream) {
                                        Icon(
                                            imageVector = LucideIcons.CaretDown,
                                            contentDescription = null,
                                            tint = colors.onMedia.copy(alpha = 0.85f),
                                            modifier = Modifier.size(spacing.iconTiny)
                                        )
                                    }
                                }

                                // 2. Aspect ratio toggle button (Tỷ lệ khung hình gốc / Lấp đầy khung 16:9)
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
                                            isOriginalRatio = !isOriginalRatio
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

                                // 3. Reload stream button (Tải lại luồng trực tiếp)
                                Box(
                                    modifier = Modifier
                                        .size(actionBtnSize)
                                        .clip(RoundedCornerShape(spacing.radiusTiny))
                                        .clickable {
                                            showChannelDropdown = false
                                            autoHideToken++
                                            onLoad(true)
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

                                // 4. Fullscreen landscape toggle button
                                Box(
                                    modifier = Modifier
                                        .size(actionBtnSize)
                                        .clip(RoundedCornerShape(spacing.radiusTiny))
                                        .clickable {
                                            showChannelDropdown = false
                                            autoHideToken++
                                            onToggleFullscreen()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isFullscreen) LucideIcons.Minimize else LucideIcons.Maximize,
                                        contentDescription = if (isFullscreen) {
                                            if (isEn) "Exit fullscreen" else "Thoát toàn màn hình"
                                        } else {
                                            if (isEn) "Fullscreen" else "Toàn màn hình"
                                        },
                                        tint = colors.onMedia,
                                        modifier = Modifier.size(actionIconSize)
                                    )
                                }
                            }

                            if (showChannelDropdown && hasSubStream) {
                                Spacer(modifier = Modifier.height(spacing.space4))
                                Box(
                                    modifier = Modifier
                                        .padding(start = spacing.space3)
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
                }
            }

            if (error == null && (!isPlaying || showInfo)) {
                MediaFullscreenFloatingButton(
                    isFullscreen = isFullscreen,
                    isEn = isEn,
                    onClick = onToggleFullscreen,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(spacing.space8)
                )
            }
        }
    ) {
        if (!streamUrl.isNullOrEmpty() && error == null) {
            LiveStreamPlayer(
                wsUrl = streamUrl,
                cameraId = cameraId,
                isOriginalRatio = isOriginalRatio,
                isPaused = !isActive,
                onPlayingChange = { playing ->
                    isPlaying = playing
                    if (playing) {
                        onFirstFrame()
                    }
                },
                onErrorChange = { isErr ->
                    if (isErr && isActive) {
                        isPlaying = false
                        onRetry()
                    }
                }
            )
        }

        val showPreviewOverlay = (isLoading || !isPlaying) && error == null
        AnimatedVisibility(
            visible = showPreviewOverlay,
            enter = fadeIn(animationSpec = tween(150)),
            exit = fadeOut(animationSpec = tween(300)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isFullscreen) Color.Black else colors.surface),
                contentAlignment = Alignment.Center
            ) {
                if (!snapshotUrl.isNullOrEmpty()) {
                    val previewRequest = remember(snapshotUrl, cameraId, isCameraFallbackSnapshot) {
                        buildEventPreviewImageRequest(
                            context = context,
                            imageUrl = snapshotUrl,
                            cameraId = cameraId,
                            isCameraFallback = isCameraFallbackSnapshot
                        )
                    }
                    AsyncImage(
                        model = previewRequest,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = if (isFullscreen) ContentScale.Fit else ContentScale.FillBounds
                    )
                }
                if (isActive) {
                    WaveDotsLoading()
                }
            }
        }
    }
}

@Composable
private fun MediaFullscreenFloatingButton(
    isFullscreen: Boolean,
    isEn: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val btnShape = RoundedCornerShape(spacing.radiusSmall)
    Box(
        modifier = modifier
            .size(spacing.iconButtonSmall)
            .clip(btnShape)
            .background(colors.mediaBg.copy(alpha = 0.52f))
            .border(spacing.borderMedium, colors.onMedia.copy(alpha = 0.16f), btnShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isFullscreen) LucideIcons.Minimize else LucideIcons.Maximize,
            contentDescription = if (isFullscreen) {
                if (isEn) "Exit fullscreen" else "Thoát toàn màn hình"
            } else {
                if (isEn) "Fullscreen" else "Toàn màn hình"
            },
            tint = colors.onMedia,
            modifier = Modifier.size(spacing.iconSmall)
        )
    }
}

private fun findCachedEventImageKey(
    context: Context,
    imageUrl: String,
    cameraId: Int = 0
): String? {
    val memCache = Coil.imageLoader(context).memoryCache ?: return null
    fun has(key: String): Boolean = memCache[MemoryCache.Key(key)] != null

    val fullKey = "${imageUrl}_1280x720"
    if (has(fullKey)) return fullKey
    val gridKey = "${imageUrl}_800x450"
    if (has(gridKey)) return gridKey
    val mediumKey = "${imageUrl}_480x270"
    if (has(mediumKey)) return mediumKey
    val listKey = "${imageUrl}_420x236"
    if (has(listKey)) return listKey
    if (cameraId > 0) {
        val liveCamKey = MonitorCacheManager.liveStageMemCacheKey(cameraId, imageUrl)
        if (has(liveCamKey)) return liveCamKey
        val camCardKey = MonitorCacheManager.cardMemCacheKey(cameraId, imageUrl)
        if (has(camCardKey)) return camCardKey
    }
    if (has(imageUrl)) return imageUrl
    return null
}

private fun buildEventPreviewImageRequest(
    context: Context,
    imageUrl: String,
    cameraId: Int = 0,
    isCameraFallback: Boolean = false
): ImageRequest {
    val targetMemKey = if (isCameraFallback && cameraId > 0) {
        MonitorCacheManager.liveStageMemCacheKey(cameraId, imageUrl)
    } else {
        "${imageUrl}_1280x720"
    }
    val placeholderKey = findCachedEventImageKey(context, imageUrl, cameraId)
    return ImageRequest.Builder(context)
        .data(MonitorCacheManager.coilDataModel(imageUrl))
        .size(1280, 720)
        .precision(Precision.INEXACT)
        .allowHardware(true)
        .crossfade(false)
        .memoryCacheKey(targetMemKey)
        .apply {
            if (!imageUrl.startsWith("file://")) {
                diskCacheKey(
                    if (isCameraFallback && cameraId > 0) {
                        MonitorCacheManager.diskCacheKey(cameraId, imageUrl)
                    } else {
                        imageUrl.substringBefore('?')
                    }
                )
            }
            if (placeholderKey != null) {
                placeholderMemoryCacheKey(placeholderKey)
            }
        }
        .build()
}

private fun Modifier.drawEventBoundingBoxes(boxes: List<List<Float>>): Modifier =
    drawWithContent {
        drawContent()
        boxes.forEachIndexed { index, boxArr ->
            if (boxArr.size == 4) {
                val minX = boxArr[0]
                val minY = boxArr[1]
                val maxX = boxArr[2]
                val maxY = boxArr[3]
                drawRect(
                    color = BoundingBoxPalette[index % BoundingBoxPalette.size],
                    topLeft = Offset(minX * size.width, minY * size.height),
                    size = Size((maxX - minX) * size.width, (maxY - minY) * size.height),
                    style = BoundingBoxStroke
                )
            }
        }
    }

@Composable
private fun EventImageWithBoxes(
    imageUrl: String?,
    boxes: List<List<Float>>,
    isFullscreen: Boolean,
    isHiddenForOverlay: Boolean = false,
    smoothResetTrigger: Int = 0,
    isEn: Boolean,
    onToggleFullscreen: () -> Unit,
    onZoomChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val spacing = MaterialTheme.spacing
    var imageLoadError by remember(imageUrl) { mutableStateOf(false) }
    val request = remember(imageUrl) {
        if (imageUrl.isNullOrEmpty()) {
            null
        } else {
            buildEventPreviewImageRequest(context = context, imageUrl = imageUrl)
        }
    }
    if (request != null && !imageLoadError) {
        ZoomableMediaBox(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isFullscreen) Color.Black else MaterialTheme.appColors.surface)
                .graphicsLayer { alpha = if (isHiddenForOverlay) 0f else 1f },
            zoomResetKey = imageUrl,
            smoothResetTrigger = smoothResetTrigger,
            enableDoubleTapZoom = true,
            badgeAlignment = Alignment.BottomStart,
            badgePadding = PaddingValues(spacing.space8),
            onZoomChanged = onZoomChanged,
            overlay = { _, _ ->
                MediaFullscreenFloatingButton(
                    isFullscreen = isFullscreen,
                    isEn = isEn,
                    onClick = onToggleFullscreen,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(spacing.space8)
                )
            }
        ) {
            val imageBoxModifier = if (isFullscreen) {
                Modifier.aspectRatio(16f / 9f, matchHeightConstraintsFirst = true)
            } else {
                Modifier.fillMaxSize()
            }
            AsyncImage(
                model = request,
                contentDescription = "Event Image",
                onError = { imageLoadError = true },
                modifier = imageBoxModifier.drawEventBoundingBoxes(boxes),
                contentScale = ContentScale.FillBounds
            )
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            CommonEmptyState(
                text = if (isEn) "No image" else "Không có ảnh",
                modifier = Modifier.fillMaxSize()
            )
            if (isFullscreen) {
                MediaFullscreenFloatingButton(
                    isFullscreen = true,
                    isEn = isEn,
                    onClick = onToggleFullscreen,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(spacing.space8)
                )
            }
        }
    }
}

/**
 * Lớp phủ phóng to ngang 120Hz cho Ảnh sự kiện:
 * - Xoay trực tiếp từ khung 16:9 inline sang ngang (±90°) bằng GPU `graphicsLayer`, không làm xoay lại
 *   layout dọc bên dưới của `EventDetailScreen`.
 * - Hỗ trợ cảm biến xoay 2 chiều ngang (+90° / -90°), Pinch-to-Zoom 5x, chạm đúp 2.5x và khi bấm thu nhỏ
 *   sẽ xoay mượt ảnh thẳng về đúng khung inline ban đầu.
 */
@Composable
private fun FullscreenEventImageOverlay(
    imageUrl: String,
    boxes: List<List<Float>>,
    isOpen: Boolean,
    progressProvider: () -> Float,
    originRect: Rect?,
    smoothResetTrigger: Int,
    isEn: Boolean,
    onRequestClose: () -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val spacing = MaterialTheme.spacing
    val request = remember(imageUrl) {
        buildEventPreviewImageRequest(context = context, imageUrl = imageUrl)
    }

    var targetLandscapeAngle by remember { mutableStateOf(90f) }
    LaunchedEffect(isOpen) {
        if (!isOpen) {
            delay(300L)
            targetLandscapeAngle = 90f
        }
    }
    DisposableEffect(context, isOpen) {
        if (!isOpen) {
            onDispose { }
        } else {
            val listener = object : OrientationEventListener(context) {
                override fun onOrientationChanged(orientation: Int) {
                    if (orientation == ORIENTATION_UNKNOWN) return
                    when (orientation) {
                        in 235..305 -> if (targetLandscapeAngle != 90f) targetLandscapeAngle = 90f
                        in 55..125 -> if (targetLandscapeAngle != -90f) targetLandscapeAngle = -90f
                    }
                }
            }
            if (listener.canDetectOrientation()) {
                listener.enable()
            }
            onDispose {
                listener.disable()
            }
        }
    }

    val animatedLandscapeAngle by animateFloatAsState(
        targetValue = targetLandscapeAngle,
        animationSpec = tween(durationMillis = 260, easing = StandardEasing),
        label = "fullscreenImageLandscapeAngle"
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val rootW = constraints.maxWidth
        val rootH = constraints.maxHeight

        // Lớp nền đen mờ dần theo đúng tiến độ xoay mở / thu nhỏ ảnh
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(Color.Black, alpha = progressProvider().coerceIn(0f, 1f))
                }
                .then(
                    if (isOpen) {
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                    } else {
                        Modifier
                    }
                )
        )

        if (rootW > 0 && rootH > 0) {
            val landW = rootH
            val landH = rootW
            val targetAspect = 16f / 9f
            val fittedImgW: Float
            val fittedImgH: Float
            if (landW.toFloat() / landH.toFloat() >= targetAspect) {
                fittedImgH = landH.toFloat()
                fittedImgW = fittedImgH * targetAspect
            } else {
                fittedImgW = landW.toFloat()
                fittedImgH = fittedImgW / targetAspect
            }
            val fittedImgWidthDp = with(density) { fittedImgW.toDp() }
            val fittedImgHeightDp = with(density) { fittedImgH.toDp() }
            val safeControlPadding = PaddingValues(
                horizontal = spacing.space20,
                vertical = spacing.space12
            )

            Layout(
                content = {
                    ZoomableMediaBox(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                val p = progressProvider().coerceIn(0f, 1f)
                                val startW = originRect?.width?.takeIf { it > 1f } ?: rootW.toFloat()
                                val startH = originRect?.height?.takeIf { it > 1f } ?: (startW * 9f / 16f)
                                val startCx = originRect?.center?.x ?: (rootW * 0.5f)
                                val startCy = originRect?.center?.y ?: (rootH * 0.5f)

                                val startScaleX = startW / fittedImgW
                                val startScaleY = startH / fittedImgH
                                val startTx = startCx - rootW * 0.5f
                                val startTy = startCy - rootH * 0.5f

                                scaleX = startScaleX + (1f - startScaleX) * p
                                scaleY = startScaleY + (1f - startScaleY) * p
                                rotationZ = animatedLandscapeAngle * p
                                translationX = startTx * (1f - p)
                                translationY = startTy * (1f - p)
                            },
                        zoomResetKey = imageUrl,
                        smoothResetTrigger = smoothResetTrigger,
                        enableDoubleTapZoom = isOpen,
                        badgeAlignment = Alignment.BottomStart,
                        badgePadding = safeControlPadding,
                        overlay = { _, resetZoom ->
                            MediaFullscreenFloatingButton(
                                isFullscreen = true,
                                isEn = isEn,
                                onClick = {
                                    if (isOpen) {
                                        resetZoom()
                                        onRequestClose()
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(safeControlPadding)
                                    .graphicsLayer {
                                        alpha = progressProvider().coerceIn(0f, 1f)
                                    }
                            )
                        }
                    ) {
                        AsyncImage(
                            model = request,
                            contentDescription = "Event Image Fullscreen",
                            modifier = Modifier
                                .size(width = fittedImgWidthDp, height = fittedImgHeightDp)
                                .drawEventBoundingBoxes(boxes),
                            contentScale = ContentScale.FillBounds
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) { measurables, _ ->
                val placeable = measurables[0].measure(Constraints.fixed(landW, landH))
                layout(rootW, rootH) {
                    placeable.place((rootW - landW) / 2, (rootH - landH) / 2)
                }
            }
        }
    }
}

// =============================================================================================
// Event information board (.event-data-board)
// =============================================================================================

@Composable
private fun EventInfoBoard(
    eventId: Int,
    selectedTab: EventMediaTab,
    display: EventDetailDisplay,
    isEn: Boolean,
    isRelatedPlatesOpen: Boolean,
    expandedCropUrl: String?,
    onCropClick: (String) -> Unit,
    onCropDismiss: () -> Unit,
    onToggleRelatedPlates: () -> Unit,
    onReport: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.appColors
    var isDownloading by remember(eventId) { mutableStateOf(false) }
    var isSharing by remember(eventId) { mutableStateOf(false) }

    val performMediaSave: () -> Unit = {
        if (!isDownloading) {
            val prefersVideo = selectedTab == EventMediaTab.EVENT_VIDEO && !display.videoUrl.isNullOrBlank()
            val primaryImgUrl = display.imageUrl ?: display.cropUrls.firstOrNull()
            val onlyVideoAvailable = primaryImgUrl.isNullOrBlank() && !display.videoUrl.isNullOrBlank()
            when {
                prefersVideo || onlyVideoAvailable -> {
                    val vUrl = display.videoUrl.orEmpty()
                    isDownloading = true
                    scope.launch {
                        try {
                            MediaActionHelper.downloadEventVideoToGallery(
                                context = context,
                                videoUrl = vUrl,
                                eventId = eventId,
                                isEn = isEn
                            )
                        } finally {
                            isDownloading = false
                        }
                    }
                }
                !primaryImgUrl.isNullOrBlank() -> {
                    val boxesToDraw = if (primaryImgUrl == display.imageUrl) display.boxes else emptyList()
                    isDownloading = true
                    scope.launch {
                        try {
                            MediaActionHelper.downloadEventImageToGallery(
                                context = context,
                                imageUrl = primaryImgUrl,
                                boxes = boxesToDraw,
                                eventId = eventId,
                                isEn = isEn
                            )
                        } finally {
                            isDownloading = false
                        }
                    }
                }
                else -> {
                    MobileTopPopupManager.warning(
                        if (isEn) "No media available to save" else "Không có hình ảnh hoặc video để tải về"
                    )
                }
            }
        }
    }

    val legacyStoragePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            performMediaSave()
        } else {
            MobileTopPopupManager.warning(
                if (isEn) "Storage permission is required to save media" else "Cần cấp quyền bộ nhớ để lưu vào Thư viện"
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.space16, vertical = spacing.space12),
        verticalArrangement = Arrangement.spacedBy(spacing.space2)
    ) {
        // 1. Biển số xe (if present)
        if (display.plateNumber.isNotEmpty()) {
            EventInfoDataRow(label = if (isEn) "License plate" else "Biển số xe") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.space8)
                ) {
                    PlateBadgePill(plate = display.plateNumber, isBlacklist = display.isPlateBlacklist)
                    Box(
                        modifier = Modifier
                            .size(spacing.space24 + spacing.space2)
                            .clip(RoundedCornerShape(spacing.radiusTiny))
                            .background(colors.controlBg)
                            .clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                clipboard?.setPrimaryClip(ClipData.newPlainText("plate", display.plateNumber))
                                MobileTopPopupManager.success(
                                    if (isEn) "Copied: ${display.plateNumber}" else "Đã sao chép biển số: ${display.plateNumber}"
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = LucideIcons.Copy,
                            contentDescription = "Copy plate",
                            tint = colors.textMuted,
                            modifier = Modifier.size(spacing.iconXSmall)
                        )
                    }
                }
            }
        }

        // 2. Face Recognition: Định danh + Nhóm (FaceEventDetailFrame.vue)
        display.face?.let { face ->
            EventInfoDataRow(label = if (isEn) "Identity" else "Định danh") {
                if (face.showNameBadge) {
                    ColoredLabelBadge(text = face.profileName, color = face.groupColor)
                } else {
                    InfoValueText(text = face.profileName)
                }
            }
            EventInfoDataRow(label = if (isEn) "Group" else "Nhóm") {
                InfoValueText(text = face.groupName)
            }
        }

        // 3. Uniform Detection: Định danh + Trang phục (UniformEventDetailFrame.vue)
        display.uniform?.let { uniform ->
            EventInfoDataRow(label = if (isEn) "Identity" else "Định danh") {
                if (uniform.isIdentified) {
                    ColoredLabelBadge(text = uniform.personName, color = uniform.color)
                } else {
                    InfoValueText(text = uniform.personName, color = colors.textMuted, fontWeight = FontWeight.Medium)
                }
            }
            if (uniform.uniformLabel.isNotEmpty()) {
                EventInfoDataRow(label = if (isEn) "Uniform" else "Trang phục") {
                    ColoredLabelBadge(text = uniform.uniformLabel, color = uniform.color)
                }
            }
        }

        // 4. Fire & Smoke: Loại cảnh báo (FireEventDetailFrame.vue)
        display.fireAlertLabel?.let { label ->
            EventInfoDataRow(label = if (isEn) "Alert type" else "Loại cảnh báo") {
                InfoValueText(text = label)
            }
        }

        // 5. Crowd Detection: Số lượng người / Ngưỡng cảnh báo (CrowdEventDetailFrame.vue)
        display.crowd?.let { crowd ->
            EventInfoDataRow(label = if (isEn) "People count" else "Số lượng người") {
                InfoValueText(text = crowd.countText, color = colors.success, fontWeight = FontWeight.Bold)
            }
            crowd.thresholdText?.let { threshold ->
                EventInfoDataRow(label = if (isEn) "Threshold" else "Ngưỡng cảnh báo") {
                    InfoValueText(text = threshold)
                }
            }
        }

        // 6. Vehicle Attributes (Loại phương tiện, Màu xe, Hãng xe, Tốc độ) & 7. Khu vực (Zone)
        OptionalInfoRow(label = if (isEn) "Vehicle type" else "Loại phương tiện", value = display.vehicleType)
        OptionalInfoRow(label = if (isEn) "Vehicle color" else "Màu xe", value = display.vehicleColor)
        OptionalInfoRow(label = if (isEn) "Brand" else "Hãng xe", value = display.vehicleBrand)
        OptionalInfoRow(label = if (isEn) "Speed" else "Tốc độ", value = display.vehicleSpeed)
        OptionalInfoRow(label = if (isEn) "Zone" else "Khu vực", value = display.zoneName)

        // 8. Thời gian (.time-data-item)
        EventInfoDataRow(label = if (isEn) "Time" else "Thời gian") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.space6)
            ) {
                InfoValueText(text = display.formattedTime, fontWeight = FontWeight.Bold)
                if (display.formattedDate.isNotEmpty()) {
                    InfoValueText(text = display.formattedDate, color = colors.textMuted, fontWeight = FontWeight.Medium)
                }
            }
        }

        // 9. Vị trí (.location-data-item)
        EventInfoDataRow(label = if (isEn) "Location" else "Vị trí") {
            InfoValueText(
                text = display.locationText,
                textAlign = TextAlign.End,
                maxLines = 2,
                modifier = Modifier.widthIn(max = LocationMaxWidth)
            )
        }

        // 10. Ảnh minh chứng (.crop-data-item)
        if (display.cropUrls.isNotEmpty()) {
            EventInfoDataRow(label = if (isEn) "Target photo" else "Ảnh minh chứng") {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(spacing.space8),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    display.cropUrls.forEach { url ->
                        EvidenceCropThumbnail(
                            url = url,
                            isExpanded = expandedCropUrl == url,
                            onClick = { onCropClick(url) },
                            onDismiss = onCropDismiss
                        )
                    }
                }
            }
        }

        // 11. Hành động (.action-data-item)
        EventInfoDataRow(label = if (isEn) "Actions" else "Hành động") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.space8),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Download Image / Video directly to Gallery (.download-btn)
                PwaActionButton(
                    icon = LucideIcons.Download,
                    bgColor = if (isDownloading) colors.accent.copy(alpha = 0.55f) else colors.accent,
                    onClick = {
                        if (!isDownloading) {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                legacyStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            } else {
                                performMediaSave()
                            }
                        }
                    }
                )

                // Share Event with Annotated Photo (.share-btn)
                PwaActionButton(
                    icon = LucideIcons.Share2,
                    bgColor = if (isSharing) colors.success.copy(alpha = 0.55f) else colors.success,
                    onClick = {
                        if (!isSharing) {
                            isSharing = true
                            scope.launch {
                                try {
                                    shareEvent(context, eventId, display, isEn)
                                } finally {
                                    isSharing = false
                                }
                            }
                        }
                    }
                )

                // Related License Plates Toggle Button (.lpr-btn)
                if (display.canShowRelatedPlates) {
                    PwaActionButton(
                        icon = LucideIcons.Barcode,
                        bgColor = if (isRelatedPlatesOpen) WarningAmberDeep else colors.warning,
                        onClick = onToggleRelatedPlates
                    )
                }

                // Report Wrong Event Button (.report-btn)
                PwaActionButton(
                    icon = LucideIcons.AlertTriangle,
                    bgColor = colors.error,
                    onClick = onReport
                )
            }
        }
    }
}

private suspend fun shareEvent(
    context: Context,
    eventId: Int,
    display: EventDetailDisplay,
    isEn: Boolean
) {
    val primaryImgUrl = display.imageUrl ?: display.cropUrls.firstOrNull()
    val boxesToDraw = if (primaryImgUrl == display.imageUrl) display.boxes else emptyList()
    val title = display.aiTaskTitle
    val subjectText = if (isEn) "[AI Event] $title" else "[Sự kiện AI] $title"
    val timeText = listOf(display.formattedTime, display.formattedDate)
        .filter { it.isNotBlank() }
        .joinToString(" ")
    val shareText = buildString {
        appendLine(subjectText)
        if (display.plateNumber.isNotEmpty()) {
            appendLine(if (isEn) "License plate: ${display.plateNumber}" else "Biển số xe: ${display.plateNumber}")
        }
        display.face?.let { face ->
            if (face.profileName.isNotBlank() && face.profileName != "---") {
                appendLine(if (isEn) "Identity: ${face.profileName}" else "Định danh: ${face.profileName}")
            }
        }
        display.fireAlertLabel?.let { fireLabel ->
            appendLine(if (isEn) "Alert type: $fireLabel" else "Loại cảnh báo: $fireLabel")
        }
        appendLine(if (isEn) "Location: ${display.locationText}" else "Vị trí: ${display.locationText}")
        append(if (isEn) "Time: $timeText" else "Thời gian: $timeText")
    }
    MediaActionHelper.shareEventWithMedia(
        context = context,
        imageUrl = primaryImgUrl,
        boxes = boxesToDraw,
        eventId = eventId,
        subjectText = subjectText,
        shareText = shareText,
        chooserTitle = if (isEn) "Share event" else "Chia sẻ sự kiện"
    )
}

/** Ảnh minh chứng 48dp, chạm để phóng to trong popup (giới hạn kích thước giải mã theo popup) */
@Composable
private fun EvidenceCropThumbnail(
    url: String,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.appColors
    val thumbShape = RoundedCornerShape(spacing.radiusSmall)
    val thumbSize = spacing.space48

    // Không giải mã ảnh gốc: giới hạn theo kích thước popup lớn nhất, chỉ thu nhỏ (không phóng to ảnh nhỏ)
    val decodeSizePx = remember(density) { with(density) { CropPopupMaxWidth.roundToPx() } }
    val request = remember(url, decodeSizePx) {
        ImageRequest.Builder(context)
            .data(url)
            .size(decodeSizePx)
            .precision(Precision.INEXACT)
            .allowHardware(true)
            .crossfade(false)
            .memoryCacheKey("${url}_crop_$decodeSizePx")
            .diskCacheKey(url.substringBefore('?'))
            .build()
    }

    val currentPainter = rememberAsyncImagePainter(model = request)
    if (currentPainter.state is AsyncImagePainter.State.Success) {
        var keepPopupMounted by remember(url) { mutableStateOf(false) }
        var animateIn by remember(url) { mutableStateOf(false) }

        LaunchedEffect(isExpanded) {
            if (isExpanded) {
                keepPopupMounted = true
                animateIn = true
            } else if (keepPopupMounted) {
                animateIn = false
                delay(180)
                keepPopupMounted = false
            }
        }

        Box {
            Image(
                painter = currentPainter,
                contentDescription = "Evidence Crop",
                modifier = Modifier
                    .size(thumbSize)
                    .clip(thumbShape)
                    .background(colors.thumbnailBg)
                    .border(width = spacing.borderThick, color = colors.border, shape = thumbShape)
                    .clickable(onClick = onClick),
                contentScale = ContentScale.Crop
            )

            if (keepPopupMounted) {
                val popupScale by animateFloatAsState(
                    targetValue = if (animateIn) 1f else 0.65f,
                    animationSpec = tween(
                        durationMillis = if (animateIn) 240 else 170,
                        easing = if (animateIn) PopupInEasing else StandardEasing
                    ),
                    label = "cropZoomScale"
                )
                val popupAlpha by animateFloatAsState(
                    targetValue = if (animateIn) 1f else 0f,
                    animationSpec = tween(durationMillis = if (animateIn) 200 else 160),
                    label = "cropZoomAlpha"
                )
                val shadowPadPx = with(density) { spacing.space10.roundToPx() }
                val popupShape = RoundedCornerShape(spacing.radiusCompact)

                Popup(
                    alignment = Alignment.CenterEnd,
                    offset = IntOffset(x = shadowPadPx, y = 0),
                    onDismissRequest = onDismiss
                ) {
                    val intrinsicSize = currentPainter.intrinsicSize
                    val isSizeValid = intrinsicSize != Size.Unspecified && intrinsicSize.width > 0f && intrinsicSize.height > 0f
                    val imgModifier = if (isSizeValid) {
                        Modifier
                            .widthIn(max = CropPopupMaxWidth)
                            .heightIn(max = CropPopupMaxHeight)
                            .aspectRatio(intrinsicSize.width / intrinsicSize.height, matchHeightConstraintsFirst = false)
                    } else {
                        Modifier.size(CropPopupFallbackSize)
                    }

                    Box(
                        modifier = Modifier
                            .padding(spacing.space10)
                            .graphicsLayer {
                                scaleX = popupScale
                                scaleY = popupScale
                                alpha = popupAlpha
                                transformOrigin = TransformOrigin(1f, 0.5f)
                            }
                    ) {
                        Image(
                            painter = currentPainter,
                            contentDescription = "Zoomed Crop",
                            modifier = imgModifier
                                .shadow(spacing.space16, popupShape)
                                .clip(popupShape)
                                .background(Slate900)
                                .clickable(onClick = onDismiss),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
    } else {
        Box(
            modifier = Modifier
                .size(thumbSize)
                .clip(thumbShape)
                .background(colors.thumbnailBg)
        )
    }
}

// =============================================================================================
// Related license plates (.related-plates-section)
// =============================================================================================

@Composable
private fun RelatedPlatesSection(
    plates: List<EventData>,
    isLoading: Boolean,
    activeEventId: Int,
    isEn: Boolean,
    onClose: () -> Unit,
    onSelect: (EventData) -> Unit
) {
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.appColors
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = spacing.space24, end = spacing.space14, top = spacing.space6, bottom = spacing.space8),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeaderText(
                text = if (isEn) "RELATED LICENSE PLATES" else "BIỂN SỐ LIÊN QUAN",
                modifier = Modifier.weight(1f, fill = false)
            )
            Box(
                modifier = Modifier
                    .size(spacing.space24)
                    .clip(RoundedCornerShape(spacing.radiusTiny))
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = LucideIcons.X,
                    contentDescription = "Close related plates",
                    tint = colors.textMuted,
                    modifier = Modifier.size(spacing.iconXSmall)
                )
            }
        }

        SectionCard {
            when {
                isLoading -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(PlatesLoadingHeight),
                    contentAlignment = Alignment.Center
                ) {
                    WaveDotsLoading()
                }

                plates.isEmpty() -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = spacing.space24),
                    contentAlignment = Alignment.Center
                ) {
                    CommonEmptyState(
                        text = if (isEn) "No related license plates found" else "Không tìm thấy biển số liên quan"
                    )
                }

                else -> Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(spacing.space12),
                    horizontalArrangement = Arrangement.spacedBy(spacing.space10)
                ) {
                    plates.forEach { plateEv ->
                        RelatedPlateCard(
                            plateEvent = plateEv,
                            isActive = plateEv.id == activeEventId,
                            onClick = { onSelect(plateEv) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RelatedPlateCard(plateEvent: EventData, isActive: Boolean, onClick: () -> Unit) {
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.appColors
    val cardShape = RoundedCornerShape(spacing.radiusCompact)
    val plateText = remember(plateEvent) { plateEvent.data?.plate?.plate ?: plateEvent.data?.plate?.number ?: "---" }
    val timeText = remember(plateEvent) { formatEventTimeOnly(plateEvent.time) }
    val snapshotUrl = remember(plateEvent) {
        AiTypeHelper.getFullImageUrl(plateEvent, UserSession.domain)
            .ifEmpty { AiTypeHelper.getSnapshotUrl(plateEvent, UserSession.domain) }
    }

    Column(
        modifier = Modifier
            .width(RelatedPlateCardWidth)
            .clip(cardShape)
            .background(
                if (isActive) colors.accent.copy(alpha = if (colors.isDark) 0.14f else 0.10f)
                else colors.surfaceRaised
            )
            .border(
                width = if (isActive) spacing.borderThick else spacing.borderMedium,
                color = if (isActive) colors.accent else colors.border,
                shape = cardShape
            )
            .clickable(onClick = onClick)
            .padding(spacing.space8),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.space6)
    ) {
        VideoFrameImage(
            url = snapshotUrl.ifEmpty { null },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(spacing.radiusSmall)),
            contentScale = ContentScale.Crop
        )
        PlateBadgePill(plate = plateText, isBlacklist = plateEvent.isPlateBlacklisted)
        Text(
            text = timeText,
            style = MaterialTheme.typography.caption,
            color = colors.textMuted,
            fontWeight = FontWeight.Medium
        )
    }
}

// =============================================================================================
// Appearance history (.mobile-history-sidebar)
// =============================================================================================

@Composable
private fun HistoryHeader(
    periodLabel: String,
    isExpanded: Boolean,
    isEn: Boolean,
    anchorModifier: Modifier = Modifier,
    onToggleMenu: () -> Unit
) {
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.appColors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.space10)
    ) {
        SectionHeaderText(
            text = if (isEn) "APPEARANCE HISTORY" else "LỊCH SỬ XUẤT HIỆN",
            modifier = Modifier.padding(start = spacing.space14, top = spacing.space6, bottom = spacing.space8)
        )

        // Period Filter Row dùng chung chính hàm FilterCellRow với MobileFilterBottomSheet
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = spacing.space6)
                .then(anchorModifier)
                .clip(RoundedCornerShape(spacing.radiusMedium))
                .background(colors.surface)
        ) {
            FilterCellRow(
                icon = LucideIcons.Clock,
                badgeColor = WarningOrange,
                label = periodLabel,
                textColor = colors.textTitle,
                secondaryColor = MaterialTheme.colorScheme.onSurfaceVariant,
                isExpanded = isExpanded,
                onClick = onToggleMenu
            )
        }
    }
}

@Composable
private fun HistoryEndMessage(text: String) {
    val spacing = MaterialTheme.spacing
    val lineColor = MaterialTheme.appColors.textMuted.copy(alpha = 0.25f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.space28 + spacing.space2, vertical = spacing.space18),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        HorizontalDivider(
            modifier = Modifier.width(spacing.space48),
            thickness = spacing.borderMedium,
            color = lineColor
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.appColors.textMuted.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1f, fill = false)
                .padding(horizontal = spacing.space12)
        )
        HorizontalDivider(
            modifier = Modifier.width(spacing.space48),
            thickness = spacing.borderMedium,
            color = lineColor
        )
    }
}

// =============================================================================================
// Building blocks dùng chung trong màn
// =============================================================================================

/** Tiêu đề mục in hoa (THÔNG TIN SỰ KIỆN / BIỂN SỐ LIÊN QUAN / LỊCH SỬ XUẤT HIỆN) */
@Composable
private fun SectionHeaderText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        letterSpacing = SectionLetterSpacing,
        color = MaterialTheme.appColors.textMuted,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

/** Thẻ nền surface bo 16dp, cách lề ngang 10dp (khung .event-data-board / biển số liên quan) */
@Composable
private fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val spacing = MaterialTheme.spacing
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.space10)
            .padding(bottom = spacing.space20),
        color = MaterialTheme.appColors.surface,
        shape = RoundedCornerShape(spacing.radiusMedium),
        content = content
    )
}

/** 1 dòng "nhãn — giá trị" trong bảng thông tin sự kiện */
@Composable
private fun EventInfoDataRow(
    label: String,
    content: @Composable () -> Unit
) {
    val spacing = MaterialTheme.spacing
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = spacing.space32)
            .padding(vertical = spacing.space5),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = MaterialTheme.appColors.textMuted,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.width(spacing.space12))
        // weight(fill = false): giá trị dài tự co lại thay vì đẩy tràn nhãn trên máy màn nhỏ
        Box(modifier = Modifier.weight(1f, fill = false), contentAlignment = Alignment.CenterEnd) {
            content()
        }
    }
}

/** Dòng thông tin chỉ hiển thị khi có giá trị */
@Composable
private fun OptionalInfoRow(label: String, value: String) {
    if (value.isEmpty()) return
    EventInfoDataRow(label = label) {
        InfoValueText(text = value)
    }
}

/** Giá trị trong bảng thông tin (mặc định: chữ đậm màu textStrong) */
@Composable
private fun InfoValueText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.appColors.textStrong,
    fontWeight: FontWeight = FontWeight.SemiBold,
    textAlign: TextAlign? = null,
    maxLines: Int = 2
) {
    Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = fontWeight,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

/** Nhãn chữ trắng trên nền màu (tên người theo màu nhóm, nhãn đồng phục) */
@Composable
private fun ColoredLabelBadge(text: String, color: Color) {
    val spacing = MaterialTheme.spacing
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(spacing.radiusXSmall))
            .background(color)
            .padding(horizontal = spacing.space8, vertical = spacing.space3)
    ) {
        Text(
            text = text,
            color = MaterialTheme.appColors.onAccent,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PwaActionButton(
    icon: ImageVector,
    bgColor: Color,
    onClick: () -> Unit
) {
    val spacing = MaterialTheme.spacing
    Box(
        modifier = Modifier
            .size(spacing.space32 + spacing.space2)
            .clip(RoundedCornerShape(spacing.radiusSmall))
            .background(bgColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.appColors.onAccent,
            modifier = Modifier.size(spacing.iconSmallMedium)
        )
    }
}

@Composable
private fun PlateBadgePill(
    plate: String,
    isBlacklist: Boolean
) {
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.appColors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(spacing.radiusTiny))
            .background(if (isBlacklist) colors.error else OrangeDeep)
            .padding(horizontal = spacing.space8, vertical = spacing.space3),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = plate,
            color = colors.onAccent,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = PlateLetterSpacing,
            maxLines = 1
        )
    }
}
