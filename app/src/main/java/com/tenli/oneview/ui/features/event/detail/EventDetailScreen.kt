package com.tenli.oneview.ui.features.event.detail

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInRoot
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
import com.tenli.oneview.ui.component.AppConfirmDialog
import com.tenli.oneview.ui.component.CommonEmptyState
import com.tenli.oneview.ui.component.LiveStreamPlayer
import com.tenli.oneview.ui.component.MobileSectionHeader
import com.tenli.oneview.ui.component.MobileTopPopupManager
import com.tenli.oneview.ui.component.SheetCloseButton
import com.tenli.oneview.ui.component.VideoFrameImage
import com.tenli.oneview.ui.component.VideoPlayer
import com.tenli.oneview.ui.component.WaveDotsLoading
import com.tenli.oneview.ui.component.ZoomableMediaBox
import com.tenli.oneview.ui.component.rememberIsEnglish
import com.tenli.oneview.ui.features.event.formatEventTimeOnly
import com.tenli.oneview.ui.features.event.isPlateBlacklisted
import com.tenli.oneview.ui.theme.BrandActiveNavDark
import com.tenli.oneview.ui.theme.BrandLight
import com.tenli.oneview.ui.theme.BrandPrimary
import com.tenli.oneview.ui.theme.LucideIcons
import com.tenli.oneview.ui.theme.OrangeDeep
import com.tenli.oneview.ui.theme.Slate900
import com.tenli.oneview.ui.theme.WarningAmberDeep
import com.tenli.oneview.ui.theme.appColors
import com.tenli.oneview.ui.theme.caption
import com.tenli.oneview.ui.theme.spacing
import com.tenli.oneview.ui.utils.AiTypeHelper
import com.tenli.oneview.ui.utils.MediaActionHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

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

private val LocationMaxWidth = 230.dp
private val CropPopupMaxWidth = 280.dp
private val CropPopupMaxHeight = 180.dp
private val CropPopupFallbackSize = 156.dp
private val RelatedPlateCardWidth = 148.dp
private val PlatesLoadingHeight = 110.dp

private val PlateLetterSpacing = 0.8.sp

/**
 * Tọa độ các vùng trên màn hình – chỉ đọc trong bộ xử lý cử chỉ nên dùng biến thường
 * (không phải snapshot state) để cập nhật liên tục khi cuộn mà không gây recomposition.
 */
private class GestureBounds {
    var rootTopY = 0f
    var pinnedTopBottomY = 0f
    var dataBoardCoords: LayoutCoordinates? = null
    var lastTabSwipeTimeMs = 0L
    var inlineMediaSlotRect: Rect? = null
    var lastRootWidthPx = 0
    var lastRootHeightPx = 0
    var lastFittedLandWidthPx = 0
    var lastFittedLandHeightPx = 0
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
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current

    var selectedTab by remember(eventId) { mutableStateOf(EventMediaTab.EVENT_IMAGE) }

    // 1. Fullscreen cho Live Camera & Video sự kiện: Hero-Rotation 120Hz xoay ngang mượt 0° <-> ±90°
    //    trực tiếp trên khung video ngay trong cửa sổ Portrait (không xoay cửa sổ Activity, không bao giờ
    //    làm lộ hay xoay 3 tab media lúc phóng to cũng như lúc thu nhỏ, và giữ nguyên ExoPlayer 0ms reload).
    var isVideoFullscreenOpen by remember(eventId) { mutableStateOf(false) }
    var inlineMediaResetTrigger by remember(eventId) { mutableIntStateOf(0) }
    val videoFullscreenProgress = remember(eventId) { Animatable(0f) }
    val isVideoOverlayActive by remember(videoFullscreenProgress) {
        derivedStateOf { isVideoFullscreenOpen || videoFullscreenProgress.value > 0.001f }
    }

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

    val currentIsMediaFullscreen by rememberUpdatedState(isVideoOverlayActive || isImageOverlayActive)
    val currentIsMediaZoomed by rememberUpdatedState(isMediaZoomed)
    val currentOnSwipeDismissed by rememberUpdatedState(onSwipeDismissed)

    LaunchedEffect(selectedTab, uiState.event?.id) {
        isMediaZoomed = false
    }

    LaunchedEffect(isVideoFullscreenOpen) {
        val activity = context.findActivity()
        val insetsController = activity?.window?.let { win ->
            WindowCompat.getInsetsController(win, win.decorView)
        }
        if (isVideoFullscreenOpen) {
            if (insetsController != null) {
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
            }
            videoFullscreenProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 300,
                    easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
                )
            )
        } else {
            if (!isImageFullscreenOpen) {
                insetsController?.show(WindowInsetsCompat.Type.systemBars())
            }
            if (videoFullscreenProgress.value > 0f) {
                videoFullscreenProgress.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = 280,
                        easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
                    )
                )
            }
        }
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
            if (!isVideoFullscreenOpen) {
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

    DisposableEffect(Unit) {
        onDispose {
            val activity = context.findActivity() ?: return@onDispose
            val controller = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    val listState = rememberLazyListState()

    // Drag-to-dismiss vertical offset (matching PWA bottom-sheet swipe down to dismiss)
    val sheetOffsetY = remember(eventId) { Animatable(0f) }
    var activeDragOffsetY by remember(eventId) { mutableFloatStateOf(0f) }
    var isDraggingSheet by remember(eventId) { mutableStateOf(false) }
    val settleJobHolder = remember(eventId) { arrayOfNulls<Job>(1) }
    val screenHeightPx = remember(density) { with(density) { DismissTravelDistance.toPx() } }
    val horizontalLockSlopPx = remember(density) { with(density) { HorizontalLockSlop.toPx() } }
    val preConsumeVerticalSlopPx = remember(density) { with(density) { PreConsumeVerticalSlop.toPx() } }
    val verticalLockSlopPx = remember(density) { with(density) { VerticalLockSlop.toPx() } }
    val videoControllerZoneHeightPx = remember(density) { with(density) { VideoControllerZoneHeight.toPx() } }
    val bounds = remember(eventId) { GestureBounds() }

    fun captureInlineMediaViewportRect(): Rect? = bounds.inlineMediaSlotRect

    fun openVideoFullscreen() {
        inlineMediaResetTrigger++
        isVideoFullscreenOpen = true
    }

    fun closeVideoFullscreen() {
        inlineMediaResetTrigger++
        isVideoFullscreenOpen = false
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

    BackHandler(enabled = isVideoFullscreenOpen) {
        closeVideoFullscreen()
    }

    BackHandler(enabled = isImageFullscreenOpen) {
        closeImageFullscreen()
    }

    fun triggerSmoothDismiss(startOffsetY: Float? = null) {
        if (isDismissing) return
        isDismissing = true
        isImageFullscreenOpen = false
        isVideoFullscreenOpen = false
        viewModel.stopLiveStream()
        settleJobHolder[0]?.cancel()
        settleJobHolder[0] = scope.launch {
            if (startOffsetY != null) {
                sheetOffsetY.snapTo(startOffsetY)
                isDraggingSheet = false
                activeDragOffsetY = 0f
            }
            sheetOffsetY.animateTo(
                targetValue = screenHeightPx,
                animationSpec = tween(durationMillis = 300, easing = StandardEasing)
            )
            currentOnSwipeDismissed()
        }
    }

    // Chỉ đổi shape khi sheet bắt đầu/kết thúc bị kéo – không recompose theo từng pixel kéo
    val isSheetDragged by remember(eventId, sheetOffsetY) {
        derivedStateOf {
            val offset = if (isDraggingSheet) activeDragOffsetY else sheetOffsetY.value
            offset > SHEET_DRAGGED_THRESHOLD_PX
        }
    }
    val draggedSheetShape = remember(spacing.radiusLarge) {
        RoundedCornerShape(topStart = spacing.radiusLarge, topEnd = spacing.radiusLarge)
    }
    val sheetShape = if (isSheetDragged) draggedSheetShape else RectangleShape

    /** Chuyển sang xem 1 sự kiện khác: về tab ảnh & đóng popup ảnh minh chứng */
    fun resetMediaState() {
        selectedTab = EventMediaTab.EVENT_IMAGE
        isMediaZoomed = false
        isImageFullscreenOpen = false
        isVideoFullscreenOpen = false
        expandedCropUrl = null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                // Đọc offset trong pha draw → kéo sheet chỉ vẽ lại lớp scrim, không recompose
                val effectiveOffsetY = if (isDraggingSheet) activeDragOffsetY else sheetOffsetY.value
                val dragProgress = (effectiveOffsetY / screenHeightPx).coerceIn(0f, 1f)
                drawRect(DismissScrimColor, alpha = DISMISS_SCRIM_MAX_ALPHA * (1f - dragProgress))
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val effectiveOffsetY = if (isDraggingSheet) activeDragOffsetY else sheetOffsetY.value
                    translationY = if (isVideoOverlayActive || isImageOverlayActive) 0f else effectiveOffsetY
                }
                .clip(if (isVideoOverlayActive || isImageOverlayActive) RectangleShape else sheetShape)
                .background(colors.pageBg),
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
                        onClick = handleBack,
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

                Box(
                    modifier = Modifier
                        .fillMaxSize()
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
                                val dataBoardBottomY = bounds.dataBoardCoords
                                    ?.takeIf { it.isAttached }
                                    ?.let { it.positionInRoot().y + it.size.height }
                                    ?: 0f

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
                                        it.key == KEY_MEDIA_TABS || it.key == KEY_DATA_BOARD || it.key == KEY_INFO_HEADER
                                    }
                                val isInsideDataBoard =
                                    isDataBoardVisible &&
                                        pinnedBottomY > 0f &&
                                        dataBoardBottomY > pinnedBottomY &&
                                        downRootY > pinnedBottomY &&
                                        downRootY <= dataBoardBottomY

                                val isBlockedByMediaZoom = currentIsMediaZoomed && isInsidePinnedTop

                                // Horizontal swipe to switch media tabs: works across the screen except the video player seekbar or zoomed media
                                var canSwipeTab = !isInsideVideoSeekbarZone && !isBlockedByMediaZoom

                                // Vertical swipe-down to dismiss:
                                // - Allowed on Pinned Top Section (Header + Tabs + 16:9 Media area) unless zoomed in
                                // - Allowed on THÔNG TIN SỰ KIỆN section or anywhere when the list is at top
                                val isListAtTop =
                                    listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
                                var canSwipeDismiss =
                                    !isInsideVideoSeekbarZone &&
                                        !isBlockedByMediaZoom &&
                                        (isInsidePinnedTop || isInsideDataBoard || isListAtTop)

                                var lockedMode = 0 // 0 = NONE, 1 = HORIZONTAL_TAB, 2 = VERTICAL_DISMISS
                                var lastDx = 0f
                                var lastDy = 0f
                                val pointerId = down.id

                                while (true) {
                                    val ptrEvent = awaitPointerEvent(PointerEventPass.Initial)
                                    // Ngay khi người dùng chạm ngón thứ 2 (Pinch-to-Zoom), hủy ngay cử chỉ 1 ngón
                                    if (ptrEvent.changes.count { it.pressed } > 1 || currentIsMediaFullscreen) {
                                        val wasDragging = isDraggingSheet
                                        val finalDrag = activeDragOffsetY
                                        lockedMode = 0
                                        if (wasDragging && finalDrag > 0f) {
                                            settleJobHolder[0]?.cancel()
                                            settleJobHolder[0] = scope.launch {
                                                sheetOffsetY.snapTo(finalDrag)
                                                isDraggingSheet = false
                                                activeDragOffsetY = 0f
                                                sheetOffsetY.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                                            }
                                        } else {
                                            isDraggingSheet = false
                                            activeDragOffsetY = 0f
                                            if (sheetOffsetY.value > 0f) {
                                                settleJobHolder[0]?.cancel()
                                                settleJobHolder[0] = scope.launch {
                                                    sheetOffsetY.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                                                }
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
                                            settleJobHolder[0]?.cancel()
                                            val dragOffset = ((dy - verticalLockSlopPx) * SHEET_DRAG_RESISTANCE).coerceAtLeast(0f)
                                            isDraggingSheet = true
                                            activeDragOffsetY = dragOffset
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
                                        activeDragOffsetY = dragOffset
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
                                    val finalDrag = activeDragOffsetY
                                    val pulledDp = finalDrag.toDp().value
                                    val totalDyDp = lastDy.toDp().value
                                    val velocityDpPerMs = totalDyDp / elapsedMs.toFloat()
                                    val shouldDismiss = pulledDp >= DISMISS_DISTANCE_DP ||
                                        (pulledDp >= DISMISS_FLICK_DISTANCE_DP &&
                                            velocityDpPerMs >= DISMISS_FLICK_VELOCITY_DP_PER_MS &&
                                            elapsedMs < DISMISS_FLICK_MAX_MS)
                                    if (shouldDismiss) {
                                        triggerSmoothDismiss(startOffsetY = finalDrag)
                                    } else {
                                        settleJobHolder[0]?.cancel()
                                        settleJobHolder[0] = scope.launch {
                                            sheetOffsetY.snapTo(finalDrag)
                                            isDraggingSheet = false
                                            activeDragOffsetY = 0f
                                            sheetOffsetY.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                                        }
                                    }
                                } else if (isDraggingSheet || sheetOffsetY.value > 0f) {
                                    val wasDragging = isDraggingSheet
                                    val finalDrag = activeDragOffsetY
                                    settleJobHolder[0]?.cancel()
                                    settleJobHolder[0] = scope.launch {
                                        if (wasDragging) {
                                            sheetOffsetY.snapTo(finalDrag)
                                            isDraggingSheet = false
                                            activeDragOffsetY = 0f
                                        }
                                        sheetOffsetY.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                                    }
                                }
                            }
                        }
                ) {
                    val liveStatusBarTopPx = WindowInsets.statusBars.getTop(density)
                    val lockedStatusBarTopPxHolder = remember { intArrayOf(liveStatusBarTopPx) }
                    if (!isVideoOverlayActive && !isImageOverlayActive && liveStatusBarTopPx > 0) {
                        lockedStatusBarTopPxHolder[0] = liveStatusBarTopPx
                    }
                    val effectiveStatusBarTopDp = with(density) {
                        (if (isVideoOverlayActive || isImageOverlayActive) lockedStatusBarTopPxHolder[0] else liveStatusBarTopPx)
                            .coerceAtLeast(0)
                            .toDp()
                    }

                    EventDetailBodyLayout(
                        isVideoOverlayActive = isVideoOverlayActive,
                        isVideoFullscreenOpen = isVideoFullscreenOpen,
                        videoProgressProvider = { videoFullscreenProgress.value },
                        bounds = bounds,
                        pinnedHeader = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(colors.pageBg)
                                    .padding(top = effectiveStatusBarTopDp)
                            ) {
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
                            }
                        },
                        bottomContent = {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(colors.pageBg)
                            ) {
                                // =========================================================================
                                // 2. SCROLLABLE BOTTOM CONTAINER (.event-info-scroll-container in PWA)
                                // =========================================================================
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentPadding = PaddingValues(
                                        top = spacing.space12,
                                        bottom = spacing.space32 + spacing.space4
                                    )
                                ) {
                        // Cụm 3 nút chọn chế độ xem (Live Camera / Ảnh sự kiện / Video sự kiện) ngay dưới khung 16:9
                        // đồng bộ phong cách cụm nút thao tác nhanh trên màn hình Trực tiếp
                        item(key = KEY_MEDIA_TABS) {
                            EventMediaTabCard(
                                selectedTab = selectedTab,
                                isEn = isEn,
                                onSelect = { tab ->
                                    if (selectedTab != tab) {
                                        runCatching { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) }
                                        selectedTab = tab
                                    }
                                }
                            )
                        }

                        // Section Title: Thông tin sự kiện (chuẩn hóa giống tiêu đề cụm Cài đặt)
                        item(key = KEY_INFO_HEADER, contentType = CONTENT_TYPE_SECTION_HEADER) {
                            MobileSectionHeader(
                                title = stringResource(id = R.string.detail_header_info),
                                modifier = Modifier.padding(top = spacing.space18)
                            )
                        }

                        // Card: .event-data-board
                        item(key = KEY_DATA_BOARD) {
                            SectionCard(
                                modifier = Modifier.onPlaced { coords ->
                                    bounds.dataBoardCoords = coords
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
                        }
                            }
                        },
                        scrimContent = {
                            if (isVideoOverlayActive) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .drawBehind {
                                            drawRect(Color.Black, alpha = videoFullscreenProgress.value.coerceIn(0f, 1f))
                                        }
                                        .then(
                                            if (isVideoFullscreenOpen) {
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
                            }
                        },
                        mediaContent = {
                            MediaViewport(
                                selectedTab = selectedTab,
                                isScreenActive = !isClosing && !isDismissing,
                                isFullscreen = isVideoFullscreenOpen,
                                isOverlayExpandingOrOpen = isVideoOverlayActive,
                                isImageHiddenForOverlay = isImageOverlayActive,
                                inlineImageResetTrigger = inlineImageResetTrigger,
                                inlineMediaResetTrigger = inlineMediaResetTrigger,
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
                                onToggleFullscreen = {
                                    if (selectedTab == EventMediaTab.EVENT_IMAGE) {
                                        if (isImageFullscreenOpen) {
                                            closeImageFullscreen()
                                        } else {
                                            openImageFullscreen()
                                        }
                                    } else {
                                        if (isVideoFullscreenOpen) {
                                            closeVideoFullscreen()
                                        } else {
                                            openVideoFullscreen()
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
                        }
                    )
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
                        handleBack()
                    },
                    onError = { errMsg -> MobileTopPopupManager.error(errMsg) }
                )
            }
        )
    }
}

// LazyColumn keys & content types
private const val KEY_MEDIA_TABS = "section_event_media_tabs"
private const val KEY_INFO_HEADER = "section_event_info_header"
private const val KEY_DATA_BOARD = "section_event_data_board"
private const val KEY_RELATED_PLATES = "section_related_plates"
private const val CONTENT_TYPE_SECTION_HEADER = "section_header"

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
    val backPillShape = remember(spacing.radiusTiny) { RoundedCornerShape(spacing.radiusTiny) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = spacing.space12, bottom = spacing.space10, start = spacing.space14, end = spacing.space14)
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
                    iterations = 2,
                    initialDelayMillis = 1200,
                    repeatDelayMillis = 1600,
                    velocity = 28.dp
                )
            )

            if (showBackToOriginal) {
                Row(
                    modifier = Modifier
                        .clip(backPillShape)
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

/**
 * Cụm 3 nút chọn chế độ xem (Live Camera / Ảnh sự kiện / Video sự kiện) nằm ngay dưới khung 16:9,
 * đồng bộ trực quan với cụm nút thao tác nhanh (icon + nhãn trong ô bo góc) của màn hình Trực tiếp.
 */
@Composable
private fun EventMediaTabCard(
    selectedTab: EventMediaTab,
    isEn: Boolean,
    onSelect: (EventMediaTab) -> Unit
) {
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.appColors
    val cardShape = remember(spacing.radiusMedium) { RoundedCornerShape(spacing.radiusMedium) }
    val currentDensity = LocalDensity.current
    val clampedDensity = remember(currentDensity) {
        Density(
            density = currentDensity.density,
            fontScale = currentDensity.fontScale.coerceAtMost(1.05f)
        )
    }

    CompositionLocalProvider(LocalDensity provides clampedDensity) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.space10)
                .clip(cardShape)
                .background(colors.surface)
                .padding(spacing.space12),
            horizontalArrangement = Arrangement.spacedBy(spacing.space8)
        ) {
            MediaTabs.forEach { tab ->
                val icon = when (tab) {
                    EventMediaTab.LIVE_CAMERA -> LucideIcons.Video
                    EventMediaTab.EVENT_IMAGE -> LucideIcons.Image
                    EventMediaTab.EVENT_VIDEO -> LucideIcons.PlayCircle
                }
                EventMediaTabTile(
                    icon = icon,
                    label = if (isEn) tab.titleEn else tab.titleVi,
                    isSelected = selectedTab == tab,
                    onClick = { onSelect(tab) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun EventMediaTabTile(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val tileShape = remember(spacing.radiusSmallMedium) { RoundedCornerShape(spacing.radiusSmallMedium) }

    val targetBg = if (isSelected) {
        colors.accent.copy(alpha = if (colors.isDark) 0.22f else 0.11f)
    } else {
        colors.controlBg
    }
    val targetIconTint = if (isSelected) colors.activeContent else colors.iconDefault
    val targetLabelColor = if (isSelected) colors.activeContent else colors.textTitle

    val bgColor by animateColorAsState(
        targetValue = targetBg,
        animationSpec = tween(durationMillis = 180),
        label = "eventMediaTileBg"
    )
    val iconTint by animateColorAsState(
        targetValue = targetIconTint,
        animationSpec = tween(durationMillis = 180),
        label = "eventMediaTileIconTint"
    )
    val labelColor by animateColorAsState(
        targetValue = targetLabelColor,
        animationSpec = tween(durationMillis = 180),
        label = "eventMediaTileLabelColor"
    )

    Column(
        modifier = modifier
            .heightIn(min = spacing.space56)
            .clip(tileShape)
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.space6, vertical = spacing.space8),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(spacing.iconSmallMedium)
        )
        Spacer(modifier = Modifier.height(spacing.space5))
        Text(
            text = label,
            style = MaterialTheme.typography.caption,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = labelColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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
private fun EventDetailBodyLayout(
    isVideoOverlayActive: Boolean,
    isVideoFullscreenOpen: Boolean,
    videoProgressProvider: () -> Float,
    bounds: GestureBounds,
    pinnedHeader: @Composable () -> Unit,
    bottomContent: @Composable () -> Unit,
    scrimContent: @Composable () -> Unit,
    mediaContent: @Composable () -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val spacing = MaterialTheme.spacing
    val maxContentWidthPx = with(density) { spacing.contentMaxWidth.roundToPx() }
    val space6Px = with(density) { spacing.space6.toPx() }

    var targetLandscapeAngle by remember { mutableStateOf(90f) }
    LaunchedEffect(isVideoFullscreenOpen) {
        if (!isVideoFullscreenOpen) {
            delay(300L)
            targetLandscapeAngle = 90f
        }
    }
    DisposableEffect(context, isVideoFullscreenOpen) {
        if (!isVideoFullscreenOpen) {
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

    val animatedLandscapeAngleState = animateFloatAsState(
        targetValue = targetLandscapeAngle,
        animationSpec = tween(durationMillis = 260, easing = StandardEasing),
        label = "heroVideoLandscapeAngle"
    )

    Layout(
        content = {
            Box { pinnedHeader() }
            Box { bottomContent() }
            Box { scrimContent() }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        if (!isVideoOverlayActive) {
                            scaleX = 1f
                            scaleY = 1f
                            rotationZ = 0f
                            translationX = 0f
                            translationY = 0f
                        } else {
                            val p = videoProgressProvider().coerceIn(0f, 1f)
                            val slotRect = bounds.inlineMediaSlotRect
                            val rootW = bounds.lastRootWidthPx.toFloat().coerceAtLeast(1f)
                            val rootH = bounds.lastRootHeightPx.toFloat().coerceAtLeast(1f)
                            val fitW = bounds.lastFittedLandWidthPx.toFloat().coerceAtLeast(1f)
                            val fitH = bounds.lastFittedLandHeightPx.toFloat().coerceAtLeast(1f)

                            val startW = slotRect?.width?.takeIf { it > 1f } ?: rootW
                            val startH = slotRect?.height?.takeIf { it > 1f } ?: (startW * 9f / 16f)
                            val startCx = slotRect?.center?.x ?: (rootW * 0.5f)
                            val startCy = slotRect?.center?.y ?: (rootH * 0.5f)

                            val startScaleX = startW / fitW
                            val startScaleY = startH / fitH
                            val startTx = startCx - rootW * 0.5f
                            val startTy = startCy - rootH * 0.5f

                            scaleX = startScaleX + (1f - startScaleX) * p
                            scaleY = startScaleY + (1f - startScaleY) * p
                            rotationZ = animatedLandscapeAngleState.value * p
                            translationX = startTx * (1f - p)
                            translationY = startTy * (1f - p)
                        }
                    }
            ) {
                mediaContent()
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { measurables, constraints ->
        val rootW = constraints.maxWidth.coerceAtLeast(1)
        val rootH = constraints.maxHeight.coerceAtLeast(1)
        val contentW = minOf(rootW, maxContentWidthPx).coerceAtLeast(1)
        val contentLeft = (rootW - contentW) / 2

        val headerPlaceable = measurables[0].measure(
            Constraints(minWidth = contentW, maxWidth = contentW, minHeight = 0, maxHeight = rootH)
        )
        val slotTop = headerPlaceable.height
        val slotW = contentW
        val slotH = (slotW * 9f / 16f).roundToInt().coerceAtLeast(1)

        bounds.inlineMediaSlotRect = Rect(
            left = contentLeft.toFloat(),
            top = slotTop.toFloat(),
            right = (contentLeft + slotW).toFloat(),
            bottom = (slotTop + slotH).toFloat()
        )
        bounds.pinnedTopBottomY = bounds.rootTopY + slotTop + slotH + space6Px
        bounds.lastRootWidthPx = rootW
        bounds.lastRootHeightPx = rootH

        val remainingH = (rootH - slotTop - slotH).coerceAtLeast(0)
        val bottomPlaceable = measurables[1].measure(Constraints.fixed(contentW, remainingH))
        val scrimPlaceable = measurables[2].measure(Constraints.fixed(rootW, rootH))

        val landW = rootH
        val landH = rootW
        val targetAspect = 16f / 9f
        val fittedW: Float
        val fittedH: Float
        if (landW.toFloat() / landH.toFloat() >= targetAspect) {
            fittedH = landH.toFloat()
            fittedW = fittedH * targetAspect
        } else {
            fittedW = landW.toFloat()
            fittedH = fittedW / targetAspect
        }
        val fittedWidthPx = fittedW.roundToInt().coerceAtLeast(1)
        val fittedHeightPx = fittedH.roundToInt().coerceAtLeast(1)
        bounds.lastFittedLandWidthPx = fittedWidthPx
        bounds.lastFittedLandHeightPx = fittedHeightPx

        val mediaPlaceable = if (isVideoOverlayActive) {
            measurables[3].measure(Constraints.fixed(fittedWidthPx, fittedHeightPx))
        } else {
            measurables[3].measure(Constraints.fixed(slotW, slotH))
        }

        layout(rootW, rootH) {
            val hideBehindFullscreenVideo = isVideoFullscreenOpen && videoProgressProvider() >= 0.999f
            if (!hideBehindFullscreenVideo) {
                headerPlaceable.place(contentLeft, 0)
                bottomPlaceable.place(contentLeft, slotTop + slotH)
            }
            if (isVideoOverlayActive) {
                scrimPlaceable.place(0, 0)
                mediaPlaceable.place((rootW - fittedWidthPx) / 2, (rootH - fittedHeightPx) / 2)
            } else {
                mediaPlaceable.place(contentLeft, slotTop)
            }
        }
    }
}

@Composable
private fun MediaViewport(
    selectedTab: EventMediaTab,
    isScreenActive: Boolean,
    isFullscreen: Boolean,
    isOverlayExpandingOrOpen: Boolean = false,
    isImageHiddenForOverlay: Boolean = false,
    inlineImageResetTrigger: Int = 0,
    inlineMediaResetTrigger: Int = 0,
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
    val currentOnToggleFullscreen by rememberUpdatedState(onToggleFullscreen)
    val currentOnZoomChanged by rememberUpdatedState(onZoomChanged)
    val viewportBg = if (isOverlayExpandingOrOpen) Color.Black else MaterialTheme.appColors.surface
    Box(
        modifier = modifier
            .fillMaxSize()
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
                    smoothResetTrigger = inlineMediaResetTrigger,
                    isEn = isEn,
                    onToggleFullscreen = { currentOnToggleFullscreen() },
                    onZoomChanged = { currentOnZoomChanged(it) },
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
                    onToggleFullscreen = { currentOnToggleFullscreen() },
                    onZoomChanged = { currentOnZoomChanged(it) }
                )

                EventMediaTab.EVENT_VIDEO -> {
                    val noVideoText = if (isEn) "No event video" else "Không có video sự kiện"
                    val videoUrl = display.videoUrl
                    if (videoUrl != null) {
                        var userWantsVideoPlay by remember(eventId, videoUrl) { mutableStateOf(true) }
                        VideoPlayer(
                            videoUrl = videoUrl,
                            thumbnailUrl = display.imageUrl,
                            deviceKey = UserSession.accessToken,
                            isOriginalRatio = isOverlayExpandingOrOpen,
                            isFullscreen = isFullscreen,
                            playWhenReady = userWantsVideoPlay,
                            isActive = isTabActive,
                            onPlayWhenReadyChange = { wantPlay -> userWantsVideoPlay = wantPlay },
                            errorText = noVideoText,
                            onFullscreenToggle = { currentOnToggleFullscreen() },
                            enableZoom = true,
                            smoothResetTrigger = inlineMediaResetTrigger,
                            isExternallyTransformed = isOverlayExpandingOrOpen,
                            onZoomChanged = { currentOnZoomChanged(it) }
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
    smoothResetTrigger: Int = 0,
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
            isPlaying = false
            onStop()
        }
    }
    DisposableEffect(lifecycleOwner) {
        var stoppedInBackground = false
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_STOP -> {
                    stoppedInBackground = true
                    isPlaying = false
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
        smoothResetTrigger = smoothResetTrigger,
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
                val pillShape = remember(spacing.radiusSmall) { RoundedCornerShape(spacing.radiusSmall) }
                val tinyShape = remember(spacing.radiusTiny) { RoundedCornerShape(spacing.radiusTiny) }

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
                                    .clip(pillShape)
                                    .background(overlayPillBg)
                                    .border(spacing.borderMedium, overlayPillBorder, pillShape)
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
                                    .clip(pillShape)
                                    .background(overlayPillBg)
                                    .border(spacing.borderMedium, overlayPillBorder, pillShape)
                                    .padding(horizontal = spacing.space2),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(spacing.space2)
                            ) {
                                // 1. Channel selector (Luồng chính / Luồng phụ)
                                Row(
                                    modifier = Modifier
                                        .height(actionBtnSize)
                                        .clip(tinyShape)
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
                                        .clip(tinyShape)
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
                                        .clip(tinyShape)
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
                                        .clip(tinyShape)
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
                                        .clip(tinyShape)
                                        .background(colors.mediaOverlay.copy(alpha = 0.95f))
                                        .border(
                                            spacing.borderMedium,
                                            colors.onMedia.copy(alpha = 0.15f),
                                            tinyShape
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
    val btnShape = remember(spacing.radiusSmall) { RoundedCornerShape(spacing.radiusSmall) }
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

    val animatedLandscapeAngleState = animateFloatAsState(
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
                                rotationZ = animatedLandscapeAngleState.value * p
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
                    val copyBtnShape = remember(spacing.radiusTiny) { RoundedCornerShape(spacing.radiusTiny) }
                    Box(
                        modifier = Modifier
                            .size(spacing.space24 + spacing.space2)
                            .clip(copyBtnShape)
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
    val thumbShape = remember(spacing.radiusSmall) { RoundedCornerShape(spacing.radiusSmall) }
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
                val popupScaleState = animateFloatAsState(
                    targetValue = if (animateIn) 1f else 0.65f,
                    animationSpec = tween(
                        durationMillis = if (animateIn) 240 else 170,
                        easing = if (animateIn) PopupInEasing else StandardEasing
                    ),
                    label = "cropZoomScale"
                )
                val popupAlphaState = animateFloatAsState(
                    targetValue = if (animateIn) 1f else 0f,
                    animationSpec = tween(durationMillis = if (animateIn) 200 else 160),
                    label = "cropZoomAlpha"
                )
                val shadowPadPx = with(density) { spacing.space10.roundToPx() }
                val popupShape = remember(spacing.radiusCompact) { RoundedCornerShape(spacing.radiusCompact) }

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
                                val scale = popupScaleState.value
                                scaleX = scale
                                scaleY = scale
                                alpha = popupAlphaState.value
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
    val closeBtnShape = remember(spacing.radiusTiny) { RoundedCornerShape(spacing.radiusTiny) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = spacing.space18)
    ) {
        MobileSectionHeader(
            title = if (isEn) "Related license plates" else "Biển số liên quan",
            trailingContent = {
                Box(
                    modifier = Modifier
                        .size(spacing.space24)
                        .clip(closeBtnShape)
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
        )

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

                else -> LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(spacing.space12),
                    horizontalArrangement = Arrangement.spacedBy(spacing.space10)
                ) {
                    items(
                        items = plates,
                        key = { it.id },
                        contentType = { "related_plate_card" }
                    ) { plateEv ->
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
    val cardShape = remember(spacing.radiusCompact) { RoundedCornerShape(spacing.radiusCompact) }
    val thumbShape = remember(spacing.radiusSmall) { RoundedCornerShape(spacing.radiusSmall) }
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
                .clip(thumbShape),
            contentScale = ContentScale.Crop,
            targetWidthPx = 360,
            targetHeightPx = 202
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
// Building blocks dùng chung trong màn
// =============================================================================================

/** Thẻ nền surface bo 16dp, cách lề ngang 10dp (khung .event-data-board / biển số liên quan) */
@Composable
private fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val spacing = MaterialTheme.spacing
    val cardShape = remember(spacing.radiusMedium) { RoundedCornerShape(spacing.radiusMedium) }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.space10)
            .padding(bottom = spacing.space20),
        color = MaterialTheme.appColors.surface,
        shape = cardShape,
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
    val badgeShape = remember(spacing.radiusXSmall) { RoundedCornerShape(spacing.radiusXSmall) }
    Box(
        modifier = Modifier
            .clip(badgeShape)
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
    val btnShape = remember(spacing.radiusSmall) { RoundedCornerShape(spacing.radiusSmall) }
    Box(
        modifier = Modifier
            .size(spacing.space32 + spacing.space2)
            .clip(btnShape)
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
    val pillShape = remember(spacing.radiusTiny) { RoundedCornerShape(spacing.radiusTiny) }
    Box(
        modifier = Modifier
            .clip(pillShape)
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
