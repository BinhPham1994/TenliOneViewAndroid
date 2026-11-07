package com.tenli.oneview.ui.features.event.detail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Precision
import com.tenli.oneview.R
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.ui.component.AppConfirmDialog
import com.tenli.oneview.ui.component.CommonEmptyState
import com.tenli.oneview.ui.component.CustomDatePickerDialogModal
import com.tenli.oneview.ui.component.CustomRangePickerDialogModal
import com.tenli.oneview.ui.component.MobileEventCard
import com.tenli.oneview.ui.component.MobileTopPopupManager
import com.tenli.oneview.ui.component.VideoFrameImage
import com.tenli.oneview.ui.component.VideoPlayer
import com.tenli.oneview.ui.component.WaveDotsLoading
import com.tenli.oneview.ui.component.rememberIsEnglish
import com.tenli.oneview.ui.features.event.buildCameraNameMap
import com.tenli.oneview.ui.features.event.cameraUuidOrNull
import com.tenli.oneview.ui.features.event.formatEventTimeOnly
import com.tenli.oneview.ui.features.event.isPlateBlacklisted
import com.tenli.oneview.ui.features.event.uniqueKey
import com.tenli.oneview.ui.features.home.TimeFilter
import com.tenli.oneview.ui.theme.BrandPrimary
import com.tenli.oneview.ui.theme.LucideIcons
import com.tenli.oneview.ui.theme.OrangeDeep
import com.tenli.oneview.ui.theme.Slate500
import com.tenli.oneview.ui.theme.Slate900
import com.tenli.oneview.ui.theme.SurfaceVariant
import com.tenli.oneview.ui.theme.SurfaceVariantDark
import com.tenli.oneview.ui.theme.WarningAmberDeep
import com.tenli.oneview.ui.theme.appColors
import com.tenli.oneview.ui.theme.caption
import com.tenli.oneview.ui.theme.spacing
import com.tenli.oneview.ui.utils.AiTypeHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

enum class EventMediaTab(val titleVi: String, val titleEn: String) {
    ORIGINAL_VIDEO("Video gốc", "Original video"),
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
private val FocusOverlayColor = Color.Black.copy(alpha = 0.7f)
private val FocusHighlightColor = Color.Red
private const val FOCUS_PADDING_DP = 12f
private const val EVENT_ZOOM_SCALE = 1.8f
private const val EVENT_EFFECT_WINDOW_MS = 10_000L
private val PillShadowColor = Color.Black.copy(alpha = 0.12f)

private val LocationMaxWidth = 230.dp
private val CropPopupMaxWidth = 280.dp
private val CropPopupMaxHeight = 180.dp
private val CropPopupFallbackSize = 156.dp
private val RelatedPlateCardWidth = 148.dp
private val PeriodMenuMinWidth = 190.dp
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
}

@Composable
fun EventDetailScreen(
    eventId: Int,
    onBack: () -> Unit,
    onSwipeDismissed: () -> Unit = onBack,
    viewModel: EventDetailViewModel = viewModel(
        key = "event_detail_$eventId",
        factory = EventDetailViewModel.provideFactory(eventId)
    )
) {
    BackHandler { onBack() }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isEn = rememberIsEnglish()
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current

    var selectedTab by remember(eventId) { mutableStateOf(EventMediaTab.EVENT_IMAGE) }
    var expandedCropUrl by remember(eventId) { mutableStateOf<String?>(null) }
    var showConfirmReportDialog by remember(eventId) { mutableStateOf(false) }
    var showCustomDateModal by remember(eventId) { mutableStateOf(false) }
    var showCustomRangeModal by remember(eventId) { mutableStateOf(false) }
    var customPickerVersion by remember(eventId) { mutableIntStateOf(0) }

    val listState = rememberLazyListState()

    // Drag-to-dismiss vertical offset (matching PWA bottom-sheet swipe down to dismiss)
    val sheetOffsetY = remember(eventId) { Animatable(0f) }
    val screenHeightPx = remember(density) { with(density) { DismissTravelDistance.toPx() } }
    val horizontalLockSlopPx = remember(density) { with(density) { HorizontalLockSlop.toPx() } }
    val preConsumeVerticalSlopPx = remember(density) { with(density) { PreConsumeVerticalSlop.toPx() } }
    val verticalLockSlopPx = remember(density) { with(density) { VerticalLockSlop.toPx() } }
    val videoControllerZoneHeightPx = remember(density) { with(density) { VideoControllerZoneHeight.toPx() } }
    val bounds = remember(eventId) { GestureBounds() }

    fun triggerSmoothDismiss() {
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
        expandedCropUrl = null
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
                .offset { IntOffset(0, sheetOffsetY.value.roundToInt()) }
                .clip(sheetShape)
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
                    CircleCloseButton(
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
                val cameraMap = remember(uiState.cameraList) { buildCameraNameMap(uiState.cameraList) }

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = spacing.contentMaxWidth)
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .onGloballyPositioned { coords ->
                            bounds.rootTopY = coords.positionInRoot().y
                        }
                        .pointerInput(eventId) {
                            awaitEachGesture {
                                val down = awaitFirstDown(
                                    requireUnconsumed = false,
                                    pass = PointerEventPass.Initial
                                )
                                val startX = down.position.x
                                val startY = down.position.y
                                val downRootY = bounds.rootTopY + startY
                                val startTimeMs = SystemClock.uptimeMillis()
                                val pinnedBottomY = bounds.pinnedTopBottomY

                                val isInsidePinnedTop =
                                    pinnedBottomY > 0f && downRootY <= pinnedBottomY
                                val isInsideVideoSeekbarZone =
                                    selectedTab != EventMediaTab.EVENT_IMAGE &&
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

                                // Horizontal swipe to switch media tabs: works across the screen except the video player seekbar
                                var canSwipeTab = !isInsideVideoSeekbarZone

                                // Vertical swipe-down to dismiss:
                                // - Allowed on Pinned Top Section (Header + Tabs + 16:9 Media area)
                                // - Allowed on THÔNG TIN SỰ KIỆN section (header + .event-data-board card)
                                // - NEVER allowed when touching inside LỊCH SỬ XUẤT HIỆN (related events list)
                                var canSwipeDismiss = !isInsideVideoSeekbarZone && (isInsidePinnedTop || isInsideDataBoard)

                                var lockedMode = 0 // 0 = NONE, 1 = HORIZONTAL_TAB, 2 = VERTICAL_DISMISS
                                var lastDx = 0f
                                var lastDy = 0f
                                val pointerId = down.id

                                while (true) {
                                    val ptrEvent = awaitPointerEvent(PointerEventPass.Initial)
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
                    //    Supports swipe-down to dismiss & horizontal swipe to switch media tab
                    // =========================================================================
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.pageBg)
                            .onGloballyPositioned { coords ->
                                bounds.pinnedTopBottomY = coords.positionInRoot().y + coords.size.height
                            }
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
                            onClose = onBack
                        )

                        // 3-Segment Media Tabs (.custom-tabs .ant-tabs-nav-list) with Smooth Sliding Pill Indicator
                        MediaTabSelector(
                            selectedTab = selectedTab,
                            isEn = isEn,
                            onSelect = { selectedTab = it }
                        )

                        // Full-bleed 16:9 Media Viewport (.media-display-wrapper)
                        // Supports horizontal swipe to switch between Video gốc / Ảnh sự kiện / Video sự kiện
                        MediaViewport(
                            selectedTab = selectedTab,
                            eventId = event.id,
                            display = display,
                            originalVideoLoading = uiState.originalVideoLoading,
                            originalVideoError = uiState.originalVideoError,
                            originalVideoUrl = uiState.originalVideoUrl,
                            originalVideoSeekTimeMs = uiState.originalVideoSeekTimeMs,
                            isEn = isEn,
                            onLoadOriginalVideo = viewModel::loadOriginalVideo
                        )

                        Spacer(modifier = Modifier.height(spacing.space6))
                    }

                    // =========================================================================
                    // 2. SCROLLABLE BOTTOM CONTAINER (.event-info-scroll-container in PWA)
                    //    Contains:
                    //    - THÔNG TIN SỰ KIỆN + .event-data-board card
                    //    - BIỂN SỐ LIÊN QUAN (when toggled via barcode button)
                    //    - LỊCH SỬ XUẤT HIỆN + Period Select + MobileEventCard list + End message
                    // =========================================================================
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
                                    top = spacing.space10,
                                    bottom = spacing.space6
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
                                selectedPeriod = uiState.selectedJourneyPeriod,
                                periodLabel = periodDisplayLabel,
                                isEn = isEn,
                                onPeriodSelected = { period ->
                                    when (period) {
                                        JourneyPeriod.CUSTOM_DATE -> showCustomDateModal = true
                                        JourneyPeriod.CUSTOM_RANGE -> showCustomRangeModal = true
                                        else -> viewModel.setJourneyPeriod(period)
                                    }
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
                // Chừa đều 2 bên theo kích thước nút đóng 32dp bên phải để tiêu đề luôn căn chính giữa màn hình
                .padding(horizontal = spacing.space32 + spacing.space6)
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

        // Circular Close Button (.ant-modal-close)
        CircleCloseButton(onClick = onClose, modifier = Modifier.align(Alignment.CenterEnd))
    }
}

@Composable
private fun CircleCloseButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.appColors
    Box(
        modifier = modifier
            .size(spacing.space32)
            .clip(CircleShape)
            .background(colors.closeButtonBg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = LucideIcons.X,
            contentDescription = "Close",
            tint = colors.textSubtle,
            modifier = Modifier.size(spacing.iconSmallMedium)
        )
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

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = spacing.space10, end = spacing.space10, top = spacing.space6, bottom = spacing.space12)
            .height(segmentControlHeight())
            .clip(RoundedCornerShape(spacing.radiusCompact))
            .background(tabsTrackBg)
            .padding(spacing.space3)
    ) {
        val segmentWidth = maxWidth / MediaTabs.size
        val pillOffset by animateDpAsState(
            targetValue = segmentWidth * selectedTabIndex,
            animationSpec = tween(durationMillis = 280, easing = SegmentEasing),
            label = "mediaSegmentIndicator"
        )

        // Sliding Active Pill – offset đọc trong pha layout nên animation không gây recompose
        Box(
            modifier = Modifier
                .offset { IntOffset(pillOffset.roundToPx(), 0) }
                .width(segmentWidth)
                .fillMaxHeight()
                .shadow(
                    elevation = spacing.space2,
                    shape = pillShape,
                    ambientColor = PillShadowColor,
                    spotColor = PillShadowColor
                )
                .clip(pillShape)
                .background(activePillBg)
        )

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

@Composable
private fun MediaViewport(
    selectedTab: EventMediaTab,
    eventId: Int,
    display: EventDetailDisplay,
    originalVideoLoading: Boolean,
    originalVideoError: String?,
    originalVideoUrl: String?,
    originalVideoSeekTimeMs: Long?,
    isEn: Boolean,
    onLoadOriginalVideo: () -> Unit
) {
    val viewportBg = MaterialTheme.appColors.surface
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(viewportBg),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = selectedTab,
            modifier = Modifier.fillMaxSize(),
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
                (enterSlide togetherWith exitSlide).using(SizeTransform(clip = true))
            },
            label = "mediaViewportTransition"
        ) { activeMediaTab ->
            when (activeMediaTab) {
                EventMediaTab.EVENT_IMAGE -> EventImageWithBoxes(
                    imageUrl = display.imageUrl,
                    boxes = display.boxes,
                    isEn = isEn
                )

                EventMediaTab.ORIGINAL_VIDEO -> OriginalVideoPane(
                    eventId = eventId,
                    display = display,
                    isLoading = originalVideoLoading,
                    error = originalVideoError,
                    videoUrl = originalVideoUrl,
                    seekTimeMs = originalVideoSeekTimeMs,
                    isEn = isEn,
                    onLoad = onLoadOriginalVideo
                )

                EventMediaTab.EVENT_VIDEO -> {
                    val noVideoText = if (isEn) "No event video" else "Không có video sự kiện"
                    val videoUrl = display.videoUrl
                    if (videoUrl != null) {
                        VideoPlayer(
                            videoUrl = videoUrl,
                            thumbnailUrl = display.imageUrl,
                            deviceKey = UserSession.accessToken,
                            errorText = noVideoText
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
private fun EventImageWithBoxes(imageUrl: String?, boxes: List<List<Float>>, isEn: Boolean) {
    val context = LocalContext.current
    var imageLoadError by remember(imageUrl) { mutableStateOf(false) }
    val request = remember(imageUrl) {
        if (imageUrl.isNullOrEmpty()) {
            null
        } else {
            ImageRequest.Builder(context)
                .data(imageUrl)
                .size(1280, 720)
                .precision(Precision.INEXACT)
                .allowHardware(true)
                .crossfade(false)
                .build()
        }
    }
    if (request != null && !imageLoadError) {
        AsyncImage(
            model = request,
            contentDescription = "Event Image",
            onError = { imageLoadError = true },
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
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
                },
            contentScale = ContentScale.FillBounds
        )
    } else {
        CommonEmptyState(
            text = if (isEn) "No image" else "Không có ảnh",
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun OriginalVideoPane(
    eventId: Int,
    display: EventDetailDisplay,
    isLoading: Boolean,
    error: String?,
    videoUrl: String?,
    seekTimeMs: Long?,
    isEn: Boolean,
    onLoad: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(eventId) { onLoad() }

    val mainBox = display.mainBox
    val pivotX = mainBox?.let { (it[0] + it[2]) / 2 } ?: 0.5f
    val pivotY = mainBox?.let { (it[1] + it[3]) / 2 } ?: 0.5f

    var showEventEffect by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (showEventEffect) EVENT_ZOOM_SCALE else 1f,
        animationSpec = tween(
            durationMillis = if (showEventEffect) 2000 else 1200,
            easing = StandardEasing
        ),
        label = "zoom_effect"
    )

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.appColors.surface),
            contentAlignment = Alignment.Center
        ) {
            val previewUrl = display.imageUrl
            if (!previewUrl.isNullOrEmpty()) {
                val previewRequest = remember(previewUrl) {
                    ImageRequest.Builder(context)
                        .data(previewUrl)
                        .size(1280, 720)
                        .precision(Precision.INEXACT)
                        .allowHardware(true)
                        .crossfade(false)
                        .build()
                }
                AsyncImage(
                    model = previewRequest,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            WaveDotsLoading()
        }
    } else if (error != null) {
        CommonEmptyState(text = error, modifier = Modifier.fillMaxSize())
    } else if (videoUrl != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RectangleShape)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        transformOrigin = TransformOrigin(pivotX, pivotY)
                    }
            ) {
                VideoPlayer(
                    videoUrl = videoUrl,
                    thumbnailUrl = null,
                    deviceKey = UserSession.accessToken,
                    initialSeekPositionMs = seekTimeMs,
                    errorText = if (isEn) "Original video unavailable" else "Video gốc không khả dụng",
                    onTimeUpdate = { currentMs ->
                        val seek = seekTimeMs ?: return@VideoPlayer
                        showEventEffect = currentMs in seek..(seek + EVENT_EFFECT_WINDOW_MS)
                    }
                )

                // Làm tối vùng ngoài đối tượng & viền đỏ quanh đối tượng trong khoảng thời gian sự kiện
                Spacer(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithContent {
                            if (showEventEffect && mainBox != null) {
                                val padding = FOCUS_PADDING_DP * density

                                val boxX = mainBox[0] * size.width
                                val boxY = mainBox[1] * size.height
                                val boxW = (mainBox[2] - mainBox[0]) * size.width
                                val boxH = (mainBox[3] - mainBox[1]) * size.height

                                val cutX = (boxX - padding).coerceAtLeast(0f)
                                val cutY = (boxY - padding).coerceAtLeast(0f)
                                val cutW = (boxX + boxW + padding).coerceAtMost(size.width) - cutX
                                val cutH = (boxY + boxH + padding).coerceAtMost(size.height) - cutY

                                drawRect(FocusOverlayColor, Offset(0f, 0f), Size(size.width, cutY))
                                drawRect(FocusOverlayColor, Offset(0f, cutY + cutH), Size(size.width, size.height - (cutY + cutH)))
                                drawRect(FocusOverlayColor, Offset(0f, cutY), Size(cutX, cutH))
                                drawRect(FocusOverlayColor, Offset(cutX + cutW, cutY), Size(size.width - (cutX + cutW), cutH))

                                drawRect(FocusHighlightColor, Offset(cutX, cutY), Size(cutW, cutH), style = BoundingBoxStroke)
                            }
                        }
                )
            }
        }
    } else {
        CommonEmptyState(
            text = if (isEn) "No video data" else "Chưa có dữ liệu",
            modifier = Modifier.fillMaxSize()
        )
    }
}

// =============================================================================================
// Event information board (.event-data-board)
// =============================================================================================

@Composable
private fun EventInfoBoard(
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
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.appColors

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
                // Download Image Button (.download-btn)
                PwaActionButton(
                    icon = LucideIcons.Download,
                    bgColor = colors.accent,
                    onClick = {
                        val imgUrl = display.imageUrl ?: display.cropUrls.firstOrNull()
                        if (!imgUrl.isNullOrBlank()) {
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(imgUrl)))
                            }
                        } else {
                            MobileTopPopupManager.warning(
                                if (isEn) "No image available" else "Không có hình ảnh để tải về"
                            )
                        }
                    }
                )

                // Share Event Button (.share-btn)
                PwaActionButton(
                    icon = LucideIcons.Share2,
                    bgColor = colors.success,
                    onClick = { shareEvent(context, display, isEn) }
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

private fun shareEvent(context: Context, display: EventDetailDisplay, isEn: Boolean) {
    val imgUrl = display.imageUrl.orEmpty()
    val title = display.aiTaskTitle
    val subjectText = if (isEn) "[AI Event] $title" else "[Sự kiện AI] $title"
    val timeText = "${display.formattedTime} ${display.formattedDate}"
    val shareText = buildString {
        appendLine(subjectText)
        appendLine(if (isEn) "Location: ${display.locationText}" else "Vị trí: ${display.locationText}")
        appendLine(if (isEn) "Time: $timeText" else "Thời gian: $timeText")
        if (imgUrl.isNotEmpty()) {
            appendLine(if (isEn) "Image: $imgUrl" else "Hình ảnh: $imgUrl")
        }
    }
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subjectText)
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    runCatching {
        context.startActivity(Intent.createChooser(sendIntent, if (isEn) "Share event" else "Chia sẻ sự kiện"))
    }
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
                .padding(start = spacing.space24, end = spacing.space14, top = spacing.space2, bottom = spacing.space6),
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
    selectedPeriod: JourneyPeriod,
    periodLabel: String,
    isEn: Boolean,
    onPeriodSelected: (JourneyPeriod) -> Unit
) {
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.appColors
    val density = LocalDensity.current
    val controlHeight = segmentControlHeight()
    var showPeriodMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.space10)
    ) {
        SectionHeaderText(
            text = if (isEn) "APPEARANCE HISTORY" else "LỊCH SỬ XUẤT HIỆN",
            modifier = Modifier.padding(start = spacing.space14, top = spacing.space2, bottom = spacing.space8)
        )

        // Period Filter Dropdown (.journey-filter-compact BaseSelect with clock icon)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = spacing.space12)
        ) {
            val iconTint = if (showPeriodMenu) colors.accent else colors.textMuted
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(controlHeight)
                    .clip(RoundedCornerShape(spacing.radiusCompact))
                    .background(colors.surface)
                    .clickable { showPeriodMenu = !showPeriodMenu }
                    .padding(horizontal = spacing.space14),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.space10)
                ) {
                    Icon(
                        imageVector = LucideIcons.Clock,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(spacing.iconSmall)
                    )
                    Text(
                        text = periodLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = colors.textTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    imageVector = LucideIcons.ChevronDown,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier
                        .size(spacing.iconSmall)
                        .rotate(if (showPeriodMenu) 180f else 0f)
                )
            }

            if (showPeriodMenu) {
                val popupOffsetY = with(density) { (controlHeight + spacing.space4).roundToPx() }
                JourneyPeriodMenu(
                    selectedPeriod = selectedPeriod,
                    isEn = isEn,
                    offsetY = popupOffsetY,
                    onDismiss = { showPeriodMenu = false },
                    onSelect = { period ->
                        showPeriodMenu = false
                        onPeriodSelected(period)
                    }
                )
            }
        }
    }
}

@Composable
private fun JourneyPeriodMenu(
    selectedPeriod: JourneyPeriod,
    isEn: Boolean,
    offsetY: Int,
    onDismiss: () -> Unit,
    onSelect: (JourneyPeriod) -> Unit
) {
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.appColors
    val menuShape = RoundedCornerShape(spacing.radiusCompact)
    val itemShape = RoundedCornerShape(spacing.radiusSmall)
    val selectedPillBg = colors.accent.copy(alpha = if (colors.isDark) 0.18f else 0.10f)

    Popup(
        alignment = Alignment.TopStart,
        offset = IntOffset(0, offsetY),
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Column(
            modifier = Modifier
                .width(IntrinsicSize.Max)
                .widthIn(min = PeriodMenuMinWidth)
                .shadow(
                    elevation = spacing.space16,
                    shape = menuShape,
                    ambientColor = Color.Black.copy(alpha = if (colors.isDark) 0.5f else 0.15f),
                    spotColor = Color.Black.copy(alpha = if (colors.isDark) 0.6f else 0.2f)
                )
                .clip(menuShape)
                .background(colors.surfaceMenu)
                .border(spacing.borderMedium, colors.border, menuShape)
                .padding(spacing.space5),
            verticalArrangement = Arrangement.spacedBy(spacing.space2)
        ) {
            JourneyPeriod.entries.forEach { period ->
                val isPeriodSelected = selectedPeriod == period
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(itemShape)
                        .background(if (isPeriodSelected) selectedPillBg else Color.Transparent)
                        .clickable { onSelect(period) }
                        .padding(horizontal = spacing.space12, vertical = spacing.space8),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = period.label(isEn),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isPeriodSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isPeriodSelected) colors.accent else colors.textTitle
                    )
                    if (isPeriodSelected) {
                        Spacer(modifier = Modifier.width(spacing.space12))
                        Icon(
                            imageVector = LucideIcons.Check,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(spacing.iconXSmall)
                        )
                    }
                }
            }
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
            .padding(bottom = spacing.space14),
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
