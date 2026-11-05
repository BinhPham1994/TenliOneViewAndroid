package com.tenli.oneview.ui.features.event.detail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.model.network.BsProfileGroupModel
import com.tenli.oneview.model.network.BsProfileModel
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.ui.component.AppConfirmDialog
import com.tenli.oneview.ui.component.CommonEmptyState
import com.tenli.oneview.ui.component.MobileEventCard
import com.tenli.oneview.ui.component.VideoFrameImage
import com.tenli.oneview.ui.component.VideoPlayer
import com.tenli.oneview.ui.component.WaveDotsLoading
import com.tenli.oneview.ui.component.isUuidString
import com.tenli.oneview.ui.component.rememberIsDarkTheme
import com.tenli.oneview.ui.features.home.findAllBoxes
import com.tenli.oneview.ui.features.home.getEventCropUrls
import com.tenli.oneview.ui.features.home.getEventImageUrl
import com.tenli.oneview.ui.features.home.getEventVideoUrl
import com.tenli.oneview.ui.theme.BackgroundDark
import com.tenli.oneview.ui.theme.BackgroundLight
import com.tenli.oneview.ui.theme.BrandAccent
import com.tenli.oneview.ui.theme.BrandPrimary
import com.tenli.oneview.ui.theme.ErrorRed
import com.tenli.oneview.ui.theme.LucideIcons
import com.tenli.oneview.ui.theme.SurfaceDark
import com.tenli.oneview.ui.theme.SurfaceLight
import com.tenli.oneview.ui.theme.SurfaceVariant
import com.tenli.oneview.ui.theme.SurfaceVariantDark
import com.tenli.oneview.ui.theme.TextPrimary
import com.tenli.oneview.ui.theme.TextPrimaryDark
import com.tenli.oneview.ui.theme.TextSecondary
import com.tenli.oneview.ui.theme.TextSecondaryDark
import com.tenli.oneview.ui.theme.TextTitleLight
import com.tenli.oneview.ui.utils.AiTypeHelper
import com.tenli.oneview.ui.utils.LocaleManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

enum class EventMediaTab(val titleVi: String, val titleEn: String) {
    ORIGINAL_VIDEO("Video gốc", "Original video"),
    EVENT_IMAGE("Ảnh sự kiện", "Event photo"),
    EVENT_VIDEO("Video sự kiện", "Event video")
}

@OptIn(ExperimentalMaterial3Api::class)
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
    androidx.activity.compose.BackHandler {
        onBack()
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val isEn = com.tenli.oneview.ui.component.rememberIsEnglish()
    val isDark = rememberIsDarkTheme()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    var selectedTab by remember(eventId) { mutableStateOf(EventMediaTab.EVENT_IMAGE) }
    var expandedCropUrl by remember(eventId) { mutableStateOf<String?>(null) }
    var showConfirmReportDialog by remember(eventId) { mutableStateOf(false) }
    var showPeriodMenu by remember(eventId) { mutableStateOf(false) }
    var showCustomDateModal by remember(eventId) { mutableStateOf(false) }
    var showCustomRangeModal by remember(eventId) { mutableStateOf(false) }
    var customPickerVersion by remember(eventId) { mutableStateOf(0) }

    val listState = rememberLazyListState()
    val haptic = LocalHapticFeedback.current
    val mediaTabs = remember { EventMediaTab.values() }

    // Drag-to-dismiss vertical offset (matching PWA bottom-sheet swipe down to dismiss)
    val sheetOffsetY = remember(eventId) { Animatable(0f) }
    val screenHeightPx = remember(density) { with(density) { 950.dp.toPx() } }
    val horizontalLockSlopPx = remember(density) { with(density) { 16.dp.toPx() } }
    val preConsumeVerticalSlopPx = remember(density) { with(density) { 5.dp.toPx() } }
    val verticalLockSlopPx = remember(density) { with(density) { 14.dp.toPx() } }
    val videoControllerZoneHeightPx = remember(density) { with(density) { 50.dp.toPx() } }

    var rootColumnTopY by remember(eventId) { mutableFloatStateOf(0f) }
    var pinnedTopSectionBottomY by remember(eventId) { mutableFloatStateOf(0f) }
    var dataBoardBottomY by remember(eventId) { mutableFloatStateOf(0f) }
    var lastTabSwipeTimeMs by remember(eventId) { mutableLongStateOf(0L) }

    fun triggerSmoothDismiss() {
        scope.launch {
            sheetOffsetY.animateTo(
                targetValue = screenHeightPx,
                animationSpec = tween(
                    durationMillis = 300,
                    easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
                )
            )
            onSwipeDismissed()
        }
    }

    val pageBgColor = if (isDark) BackgroundDark else BackgroundLight
    val cardBgColor = if (isDark) SurfaceDark else SurfaceLight
    val labelTextColor = if (isDark) Color(0xFF94A3B8) else TextSecondary
    val valueTextColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val sectionHeaderColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    val dragProgress = (sheetOffsetY.value / screenHeightPx).coerceIn(0f, 1f)
    val scrimAlpha = 0.45f * (1f - dragProgress)
    val sheetShape = if (sheetOffsetY.value > 2f) {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    } else {
        RectangleShape
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = scrimAlpha))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, sheetOffsetY.value.roundToInt()) }
                .clip(sheetShape)
                .background(pageBgColor)
        ) {
            if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                WaveDotsLoading()
            }
        } else if (uiState.error != null || uiState.event == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CommonEmptyState(text = uiState.error ?: if (isEn) "Event not found" else "Không tìm thấy sự kiện")
                // Close button even on error state
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(top = 12.dp, end = 14.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f))
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = LucideIcons.X,
                        contentDescription = "Close",
                        tint = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else {
            val event = uiState.event!!
            val baseEvent = uiState.baseEvent
            val camera = uiState.camera
            val aiService = uiState.aiServices.find { it.id == event.serviceId }

            val aiTaskTitle = remember(event, isEn) {
                resolvePwaAiTaskTitle(event, isEn)
            }

            val locationText = remember(camera, event, aiService) {
                val rawCam = camera?.name?.takeIf { it.isNotBlank() && !isUuidString(it) }
                    ?: event.data?.cameraName?.takeIf { it.isNotBlank() && !isUuidString(it) }
                    ?: event.cameraName?.takeIf { it.isNotBlank() && !isUuidString(it) }
                    ?: ""
                val rawSvc = aiService?.name?.trim().orEmpty()
                when {
                    rawCam.isNotEmpty() && rawSvc.isNotEmpty() -> "$rawCam - $rawSvc"
                    rawCam.isNotEmpty() -> rawCam
                    rawSvc.isNotEmpty() -> rawSvc
                    else -> "---"
                }
            }

            val (formattedTime, formattedDate) = remember(event.time, isEn) {
                formatPwaEventTimeAndDate(event.time, isEn)
            }

            val cropUrls = remember(event) { getEventCropUrls(event) }

            val plateNumber = remember(event) {
                (event.data?.plate?.plate
                    ?: event.data?.plate?.number
                    ?: event.data?.vehicle?.plate
                    ?: event.plate)?.trim().orEmpty()
            }

            val isPlateBlacklist = remember(event) {
                event.data?.plate?.isBlacklist == true || event.data?.plate?.isBlacklistSnake == true
            }

            val canShowRelatedPlates = remember(event, plateNumber) {
                isVehicleRelatedEvent(event, plateNumber)
            }

            val cameraMap = remember(uiState.cameraList) {
                uiState.cameraList
                    .filter { it.extra?.uuid != null }
                    .associate { it.extra!!.uuid!! to it.name }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .onGloballyPositioned { coords ->
                        rootColumnTopY = coords.positionInRoot().y
                    }
                    .pointerInput(eventId) {
                        awaitEachGesture {
                            val down = awaitFirstDown(
                                requireUnconsumed = false,
                                pass = PointerEventPass.Initial
                            )
                            val startX = down.position.x
                            val startY = down.position.y
                            val downRootY = rootColumnTopY + startY
                            val startTimeMs = SystemClock.uptimeMillis()

                            val isInsidePinnedTop =
                                pinnedTopSectionBottomY > 0f && downRootY <= pinnedTopSectionBottomY
                            val isInsideVideoSeekbarZone =
                                selectedTab != EventMediaTab.EVENT_IMAGE &&
                                    pinnedTopSectionBottomY > 0f &&
                                    downRootY in (pinnedTopSectionBottomY - videoControllerZoneHeightPx)..pinnedTopSectionBottomY
                            val isDataBoardVisible =
                                listState.layoutInfo.visibleItemsInfo.any {
                                    it.key == "section_event_data_board" || it.key == "section_event_info_header"
                                }
                            val isInsideDataBoard =
                                isDataBoardVisible &&
                                    pinnedTopSectionBottomY > 0f &&
                                    dataBoardBottomY > pinnedTopSectionBottomY &&
                                    downRootY > pinnedTopSectionBottomY &&
                                    downRootY <= dataBoardBottomY

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
                                        val dragOffset = ((dy - verticalLockSlopPx) * 0.9f).coerceAtLeast(0f)
                                        scope.launch {
                                            sheetOffsetY.snapTo(dragOffset)
                                        }
                                    } else if (canSwipeDismiss && dy > preConsumeVerticalSlopPx && dy > absX * 1.25f) {
                                        // Pre-consume downward drag in Initial pass before LazyColumn's 8dp touchSlop
                                        // on Main pass can intercept the gesture inside THÔNG TIN SỰ KIỆN
                                        change.consume()
                                    }
                                } else if (lockedMode == 1) {
                                    change.consume()
                                } else if (lockedMode == 2) {
                                    change.consume()
                                    val dragOffset = ((dy - verticalLockSlopPx) * 0.9f).coerceAtLeast(0f)
                                    scope.launch {
                                        sheetOffsetY.snapTo(dragOffset)
                                    }
                                }
                            }

                            val elapsedMs = (SystemClock.uptimeMillis() - startTimeMs).coerceAtLeast(1L)

                            if (lockedMode == 1) {
                                val absXDp = with(density) { abs(lastDx).toDp().value }
                                val isThresholdMet = absXDp >= 42f || (absXDp >= 26f && elapsedMs < 300L)
                                val now = SystemClock.uptimeMillis()
                                if (isThresholdMet && (now - lastTabSwipeTimeMs >= 250L)) {
                                    lastTabSwipeTimeMs = now
                                    val curIdx = mediaTabs.indexOf(selectedTab)
                                    if (lastDx < 0f && curIdx < mediaTabs.lastIndex) {
                                        try {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        } catch (_: Exception) {}
                                        selectedTab = mediaTabs[curIdx + 1]
                                    } else if (lastDx > 0f && curIdx > 0) {
                                        try {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        } catch (_: Exception) {}
                                        selectedTab = mediaTabs[curIdx - 1]
                                    }
                                }
                            } else if (lockedMode == 2) {
                                val pulledDp = with(density) { sheetOffsetY.value.toDp().value }
                                val totalDyDp = with(density) { lastDy.toDp().value }
                                val velocityDpPerMs = totalDyDp / elapsedMs.toFloat()
                                val shouldDismiss = pulledDp >= 85f ||
                                    (pulledDp >= 42f && velocityDpPerMs >= 0.45f && elapsedMs < 400L)
                                if (shouldDismiss) {
                                    triggerSmoothDismiss()
                                } else {
                                    scope.launch {
                                        sheetOffsetY.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                        )
                                    }
                                }
                            } else if (sheetOffsetY.value > 0f) {
                                scope.launch {
                                    sheetOffsetY.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                    )
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
                        .background(pageBgColor)
                        .onGloballyPositioned { coords ->
                            pinnedTopSectionBottomY = coords.positionInRoot().y + coords.size.height
                        }
                ) {
                    // Header: Centered AI Task Title + Top-right Circular X Button (.dashboard-header)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 6.dp, start = 14.dp, end = 14.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 38.dp)
                                .align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = aiTaskTitle,
                                fontSize = 17.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A),
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (baseEvent != null && baseEvent.id != event.id) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(BrandPrimary)
                                        .clickable {
                                            viewModel.selectEvent(baseEvent)
                                            selectedTab = EventMediaTab.EVENT_IMAGE
                                            expandedCropUrl = null
                                        }
                                        .padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = LucideIcons.ArrowLeft,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = if (isEn) "Back to original" else "Về sự kiện gốc",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Circular Close Button (.ant-modal-close)
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isDark) Color.White.copy(alpha = 0.12f)
                                    else Color.Black.copy(alpha = 0.08f)
                                )
                                .clickable { onBack() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = LucideIcons.X,
                                contentDescription = "Close",
                                tint = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569),
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    // 3-Segment Media Tabs (.custom-tabs .ant-tabs-nav-list) with Smooth Sliding Pill Indicator
                    val tabsTrackBg = if (isDark) Color.White.copy(alpha = 0.08f) else SurfaceLight
                    val activePillBg = if (isDark) SurfaceVariantDark else SurfaceVariant
                    val activeTabTextColor = if (isDark) BrandAccent else BrandAccent
                    val inactiveTabTextColor = Color(0xFF64748B)
                    val selectedTabIndex = mediaTabs.indexOf(selectedTab).coerceAtLeast(0)

                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 10.dp, end = 10.dp, top = 6.dp, bottom = 12.dp)
                            .height(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(tabsTrackBg)
                            .padding(3.dp)
                    ) {
                        val segmentWidth = maxWidth / mediaTabs.size
                        val pillOffset by animateDpAsState(
                            targetValue = segmentWidth * selectedTabIndex,
                            animationSpec = tween(
                                durationMillis = 280,
                                easing = CubicBezierEasing(0.18f, 0.9f, 0.3f, 1f)
                            ),
                            label = "mediaSegmentIndicator"
                        )

                        // Sliding Active Pill
                        Box(
                            modifier = Modifier
                                .offset(x = pillOffset)
                                .width(segmentWidth)
                                .fillMaxHeight()
                                .shadow(
                                    elevation = 2.dp,
                                    shape = RoundedCornerShape(9.dp),
                                    ambientColor = Color.Black.copy(alpha = 0.12f),
                                    spotColor = Color.Black.copy(alpha = 0.12f)
                                )
                                .clip(RoundedCornerShape(9.dp))
                                .background(activePillBg)
                        )

                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            mediaTabs.forEach { tab ->
                                val isSelected = selectedTab == tab
                                val tabTextColor by animateColorAsState(
                                    targetValue = if (isSelected) activeTabTextColor else inactiveTabTextColor,
                                    animationSpec = tween(durationMillis = 180),
                                    label = "mediaTabTextColor"
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(9.dp))
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            selectedTab = tab
                                        }
                                        .padding(horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isEn) tab.titleEn else tab.titleVi,
                                        color = tabTextColor,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    // Full-bleed 16:9 Media Viewport (.media-display-wrapper)
                    // Supports horizontal swipe to switch between Video gốc / Ảnh sự kiện / Video sự kiện
                    val mediaViewportBg = cardBgColor
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .background(mediaViewportBg),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = selectedTab,
                            modifier = Modifier.fillMaxSize(),
                            transitionSpec = {
                                val fromIdx = mediaTabs.indexOf(initialState)
                                val toIdx = mediaTabs.indexOf(targetState)
                                val isForward = toIdx >= fromIdx

                                val enterSlide = slideInHorizontally(
                                    animationSpec = tween(
                                        durationMillis = 250,
                                        easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
                                    ),
                                    initialOffsetX = { fullWidth ->
                                        if (isForward) fullWidth else -fullWidth
                                    }
                                )

                                val exitSlide = slideOutHorizontally(
                                    animationSpec = tween(
                                        durationMillis = 250,
                                        easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
                                    ),
                                    targetOffsetX = { fullWidth ->
                                        if (isForward) -fullWidth else fullWidth
                                    }
                                )

                                (enterSlide togetherWith exitSlide).using(SizeTransform(clip = true))
                            },
                            label = "mediaViewportTransition"
                        ) { activeMediaTab ->
                        when (activeMediaTab) {
                            EventMediaTab.EVENT_IMAGE -> {
                                val imageUrl = getEventImageUrl(event)
                                var imageLoadError by remember(imageUrl) { mutableStateOf(false) }
                                if (imageUrl != null && !imageLoadError) {
                                    val eventBoxes = remember(event) { findAllBoxes(event) }
                                    val palette = remember {
                                        listOf(
                                            Color(0xFFFF4D4F),
                                            Color(0xFF38DD0F),
                                            Color(0xFF1890FF),
                                            Color(0xFFFAAD14),
                                            Color(0xFF13C2C2),
                                            Color(0xFFEB2F96)
                                        )
                                    }

                                    AsyncImage(
                                        model = imageUrl,
                                        contentDescription = "Event Image",
                                        onError = { imageLoadError = true },
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .drawWithContent {
                                                drawContent()
                                                eventBoxes.forEachIndexed { index, boxArr ->
                                                    if (boxArr.size == 4) {
                                                        val minX = boxArr[0]
                                                        val minY = boxArr[1]
                                                        val maxX = boxArr[2]
                                                        val maxY = boxArr[3]

                                                        val w = (maxX - minX) * size.width
                                                        val h = (maxY - minY) * size.height
                                                        val x = minX * size.width
                                                        val y = minY * size.height

                                                        val color = palette[index % palette.size]
                                                        drawRect(
                                                            color = color,
                                                            topLeft = Offset(x, y),
                                                            size = Size(w, h),
                                                            style = Stroke(width = 4f)
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

                            EventMediaTab.ORIGINAL_VIDEO -> {
                                LaunchedEffect(event.id) {
                                    viewModel.loadOriginalVideo()
                                }

                                val eventBoxes = remember(event) { findAllBoxes(event) }
                                val mainBox = eventBoxes.firstOrNull()
                                var pivotX = 0.5f
                                var pivotY = 0.5f
                                if (mainBox != null && mainBox.size == 4) {
                                    pivotX = ((mainBox[0] + mainBox[2]) / 2).toFloat()
                                    pivotY = ((mainBox[1] + mainBox[3]) / 2).toFloat()
                                }

                                var showEventEffect by remember { mutableStateOf(false) }
                                val scale by animateFloatAsState(
                                    targetValue = if (showEventEffect) 1.8f else 1f,
                                    animationSpec = tween(
                                        durationMillis = if (showEventEffect) 2000 else 1200,
                                        easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
                                    ),
                                    label = "zoom_effect"
                                )

                                if (uiState.originalVideoLoading) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(mediaViewportBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val fallbackImg = getEventImageUrl(event)
                                        if (fallbackImg != null) {
                                            AsyncImage(
                                                model = fallbackImg,
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                        WaveDotsLoading()
                                    }
                                } else if (uiState.originalVideoError != null) {
                                    CommonEmptyState(
                                        text = uiState.originalVideoError ?: if (isEn) "Original video unavailable" else "Video không khả dụng",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    val videoUrl = uiState.originalVideoUrl
                                    if (videoUrl != null) {
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
                                                    initialSeekPositionMs = uiState.originalVideoSeekTimeMs,
                                                    errorText = if (isEn) "Original video unavailable" else "Video gốc không khả dụng",
                                                    onTimeUpdate = { currentMs ->
                                                        val seekTimeMs = uiState.originalVideoSeekTimeMs ?: return@VideoPlayer
                                                        showEventEffect = currentMs in seekTimeMs..(seekTimeMs + 10000)
                                                    }
                                                )

                                                Spacer(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .drawWithContent {
                                                            if (showEventEffect && mainBox != null && mainBox.size == 4) {
                                                                val overlayAlpha = 0.7f
                                                                val padding = 12f * this.density

                                                                val minX = mainBox[0].toFloat()
                                                                val minY = mainBox[1].toFloat()
                                                                val maxX = mainBox[2].toFloat()
                                                                val maxY = mainBox[3].toFloat()

                                                                val boxW = (maxX - minX) * size.width
                                                                val boxH = (maxY - minY) * size.height
                                                                val boxX = minX * size.width
                                                                val boxY = minY * size.height

                                                                val cutX = (boxX - padding).coerceAtLeast(0f)
                                                                val cutY = (boxY - padding).coerceAtLeast(0f)
                                                                val cutW = (boxX + boxW + padding).coerceAtMost(size.width) - cutX
                                                                val cutH = (boxY + boxH + padding).coerceAtMost(size.height) - cutY

                                                                val darkColor = Color.Black.copy(alpha = overlayAlpha)
                                                                drawRect(darkColor, Offset(0f, 0f), Size(size.width, cutY))
                                                                drawRect(darkColor, Offset(0f, cutY + cutH), Size(size.width, size.height - (cutY + cutH)))
                                                                drawRect(darkColor, Offset(0f, cutY), Size(cutX, cutH))
                                                                drawRect(darkColor, Offset(cutX + cutW, cutY), Size(size.width - (cutX + cutW), cutH))

                                                                drawRect(Color.Red, Offset(cutX, cutY), Size(cutW, cutH), style = Stroke(width = 4f))
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
                            }

                            EventMediaTab.EVENT_VIDEO -> {
                                val videoUrl = getEventVideoUrl(event)
                                if (videoUrl != null) {
                                    VideoPlayer(
                                        videoUrl = videoUrl,
                                        thumbnailUrl = getEventImageUrl(event),
                                        deviceKey = UserSession.accessToken,
                                        errorText = if (isEn) "No event video" else "Không có video sự kiện"
                                    )
                                } else {
                                    CommonEmptyState(
                                        text = if (isEn) "No event video" else "Không có video sự kiện",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
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
                    contentPadding = PaddingValues(bottom = 36.dp)
                ) {
                    // Section Title: THÔNG TIN SỰ KIỆN
                    item(key = "section_event_info_header") {
                        Text(
                            text = if (isEn) "EVENT INFORMATION" else "THÔNG TIN SỰ KIỆN",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp,
                            color = sectionHeaderColor,
                            modifier = Modifier.padding(start = 24.dp, end = 14.dp, top = 10.dp, bottom = 6.dp)
                        )
                    }

                    // Card: .event-data-board
                    item(key = "section_event_data_board") {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp)
                                .padding(bottom = 14.dp)
                                .onGloballyPositioned { coords ->
                                    dataBoardBottomY = coords.positionInRoot().y + coords.size.height
                                },
                            color = cardBgColor,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                // Module-specific Extra Data rows matching PWA frames:
                                val typeLower = event.type.lowercase()

                                // 1. Biển số xe (if present)
                                if (plateNumber.isNotEmpty()) {
                                    EventInfoDataRow(
                                        label = if (isEn) "License plate" else "Biển số xe",
                                        labelColor = labelTextColor
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            PlateBadgePill(
                                                plate = plateNumber,
                                                isBlacklist = isPlateBlacklist
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFF1F5F9))
                                                    .clickable {
                                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                                        clipboard?.setPrimaryClip(ClipData.newPlainText("plate", plateNumber))
                                                        com.tenli.oneview.ui.component.MobileTopPopupManager.success(
                                                            if (isEn) "Copied: $plateNumber" else "Đã sao chép biển số: $plateNumber"
                                                        )
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = LucideIcons.Copy,
                                                    contentDescription = "Copy plate",
                                                    tint = labelTextColor,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                // 2. Face Recognition: Định danh + Nhóm (FaceEventDetailFrame.vue)
                                if (AiTypeHelper.isFaceEvent(event)) {
                                    val isStranger = AiTypeHelper.isStranger(event)
                                    val profileName = remember(event, uiState.profileList, isEn) {
                                        resolveFaceProfileDisplayName(event, uiState.profileList, isEn)
                                    }
                                    val (groupName, groupColor) = remember(event, uiState.profileList, uiState.profileGroupList, isEn) {
                                        resolveFaceGroupNameAndColor(event, uiState.profileList, uiState.profileGroupList, isEn)
                                    }

                                    EventInfoDataRow(
                                        label = if (isEn) "Identity" else "Định danh",
                                        labelColor = labelTextColor
                                    ) {
                                        if (!isStranger && profileName != "---" && profileName != "Người lạ" && profileName != "Stranger") {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(groupColor)
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = profileName,
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = profileName,
                                                color = valueTextColor,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }

                                    EventInfoDataRow(
                                        label = if (isEn) "Group" else "Nhóm",
                                        labelColor = labelTextColor
                                    ) {
                                        Text(
                                            text = groupName,
                                            color = valueTextColor,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                // 3. Uniform Detection: Định danh + Trang phục (UniformEventDetailFrame.vue)
                                if (typeLower.contains("uniform") || typeLower.contains("clothing")) {
                                    val personName = remember(event, uiState.profileList, isEn) {
                                        val name = AiTypeHelper.extractFacePersonName(event, isEn)
                                        if (name.isNotBlank() && name != "Người lạ" && name != "Stranger") name
                                        else if (isEn) "Unidentified" else "Chưa định danh"
                                    }
                                    val isIdentified = personName != "Chưa định danh" && personName != "Unidentified"
                                    val uniformLabel = remember(event, isEn) {
                                        AiTypeHelper.extractUniformLabel(event, isEn)
                                    }
                                    val uniformColor = remember(event) { AiTypeHelper.getAiColor(event) }

                                    EventInfoDataRow(
                                        label = if (isEn) "Identity" else "Định danh",
                                        labelColor = labelTextColor
                                    ) {
                                        if (isIdentified) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(uniformColor)
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = personName,
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = personName,
                                                color = labelTextColor,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }

                                    if (uniformLabel.isNotBlank() && uniformLabel != "-") {
                                        EventInfoDataRow(
                                            label = if (isEn) "Uniform" else "Trang phục",
                                            labelColor = labelTextColor
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(uniformColor)
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = uniformLabel,
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }

                                // 4. Fire & Smoke: Loại cảnh báo (FireEventDetailFrame.vue)
                                if (typeLower.contains("fire") || typeLower.contains("smoke")) {
                                    val isSmoke = typeLower.contains("smoke") && !typeLower.contains("fire")
                                    val alertLabel = if (isSmoke) {
                                        if (isEn) "Smoke detected" else "Phát hiện khói"
                                    } else {
                                        if (isEn) "Fire detected" else "Phát hiện lửa"
                                    }
                                    EventInfoDataRow(
                                        label = if (isEn) "Alert type" else "Loại cảnh báo",
                                        labelColor = labelTextColor
                                    ) {
                                        Text(
                                            text = alertLabel,
                                            color = valueTextColor,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                // 5. Crowd Detection: Số lượng người / Ngưỡng cảnh báo (CrowdEventDetailFrame.vue)
                                if (typeLower.contains("crowd")) {
                                    val countVal = event.data?.count ?: event.data?.peopleCount ?: event.data?.crowdCount
                                    val countStr = if (countVal != null) {
                                        "$countVal ${if (isEn) "people" else "người"}"
                                    } else {
                                        if (isEn) "High density" else "Mật độ cao"
                                    }
                                    EventInfoDataRow(
                                        label = if (isEn) "People count" else "Số lượng người",
                                        labelColor = labelTextColor
                                    ) {
                                        Text(
                                            text = countStr,
                                            color = Color(0xFF10B981),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    val thVal = event.data?.threshold ?: event.data?.crowdThreshold
                                    if (thVal != null) {
                                        EventInfoDataRow(
                                            label = if (isEn) "Threshold" else "Ngưỡng cảnh báo",
                                            labelColor = labelTextColor
                                        ) {
                                            Text(
                                                text = "> $thVal ${if (isEn) "people" else "người"}",
                                                color = valueTextColor,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }

                                // 6. Vehicle Attributes (Loại phương tiện, Màu xe, Hãng xe, Tốc độ)
                                val vehicleType = remember(event, aiTaskTitle, isEn) { resolveVehicleTypeLabel(event, aiTaskTitle, isEn) }
                                val vehicleColor = remember(event, isEn) { resolveVehicleColorLabel(event, isEn) }
                                val vehicleBrand = remember(event) {
                                    (event.data?.vehicle?.brand ?: event.data?.brand ?: event.data?.vehicleBrand)?.trim().orEmpty()
                                }
                                val vehicleSpeed = remember(event) {
                                    val s = event.data?.vehicle?.speed ?: event.data?.speed ?: event.data?.vehicleSpeed
                                    if (s != null && s.toString().isNotBlank()) "${s} km/h" else ""
                                }

                                if (vehicleType.isNotEmpty()) {
                                    EventInfoDataRow(
                                        label = if (isEn) "Vehicle type" else "Loại phương tiện",
                                        labelColor = labelTextColor
                                    ) {
                                        Text(
                                            text = vehicleType,
                                            color = valueTextColor,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                if (vehicleColor.isNotEmpty()) {
                                    EventInfoDataRow(
                                        label = if (isEn) "Vehicle color" else "Màu xe",
                                        labelColor = labelTextColor
                                    ) {
                                        Text(
                                            text = vehicleColor,
                                            color = valueTextColor,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                if (vehicleBrand.isNotEmpty()) {
                                    EventInfoDataRow(
                                        label = if (isEn) "Brand" else "Hãng xe",
                                        labelColor = labelTextColor
                                    ) {
                                        Text(
                                            text = vehicleBrand,
                                            color = valueTextColor,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                if (vehicleSpeed.isNotEmpty()) {
                                    EventInfoDataRow(
                                        label = if (isEn) "Speed" else "Tốc độ",
                                        labelColor = labelTextColor
                                    ) {
                                        Text(
                                            text = vehicleSpeed,
                                            color = valueTextColor,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                // 7. Khu vực (Zone) if present
                                val zoneName = (event.data?.zoneName ?: event.data?.zone)?.trim().orEmpty()
                                if (zoneName.isNotEmpty()) {
                                    EventInfoDataRow(
                                        label = if (isEn) "Zone" else "Khu vực",
                                        labelColor = labelTextColor
                                    ) {
                                        Text(
                                            text = zoneName,
                                            color = valueTextColor,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                // 8. Thời gian (.time-data-item)
                                EventInfoDataRow(
                                    label = if (isEn) "Time" else "Thời gian",
                                    labelColor = labelTextColor
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = formattedTime,
                                            color = valueTextColor,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (formattedDate.isNotEmpty()) {
                                            Text(
                                                text = formattedDate,
                                                color = labelTextColor,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                // 9. Vị trí (.location-data-item)
                                EventInfoDataRow(
                                    label = if (isEn) "Location" else "Vị trí",
                                    labelColor = labelTextColor
                                ) {
                                    Text(
                                        text = locationText,
                                        color = valueTextColor,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.End,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.widthIn(max = 230.dp)
                                    )
                                }

                                // 10. Ảnh minh chứng (.crop-data-item)
                                if (cropUrls.isNotEmpty()) {
                                    EventInfoDataRow(
                                        label = if (isEn) "Target photo" else "Ảnh minh chứng",
                                        labelColor = labelTextColor
                                    ) {
                                        Row(
                                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            cropUrls.forEach { url ->
                                                SubcomposeAsyncImage(
                                                    model = url,
                                                    contentDescription = "Evidence Crop"
                                                ) {
                                                    val currentPainter = painter
                                                    val state = currentPainter.state
                                                    if (state is AsyncImagePainter.State.Success) {
                                                        val isThisCropExpanded = expandedCropUrl == url
                                                        var keepPopupMounted by remember(url) { mutableStateOf(false) }
                                                        var animateIn by remember(url) { mutableStateOf(false) }

                                                        LaunchedEffect(isThisCropExpanded) {
                                                            if (isThisCropExpanded) {
                                                                keepPopupMounted = true
                                                                animateIn = true
                                                            } else if (keepPopupMounted) {
                                                                animateIn = false
                                                                kotlinx.coroutines.delay(180)
                                                                keepPopupMounted = false
                                                            }
                                                        }

                                                        Box {
                                                            androidx.compose.foundation.Image(
                                                                painter = currentPainter,
                                                                contentDescription = "Evidence Crop",
                                                                modifier = Modifier
                                                                    .size(48.dp)
                                                                    .clip(RoundedCornerShape(8.dp))
                                                                    .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                                                                    .border(
                                                                        width = 1.5.dp,
                                                                        color = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f),
                                                                        shape = RoundedCornerShape(8.dp)
                                                                    )
                                                                    .clickable {
                                                                        expandedCropUrl = if (expandedCropUrl == url) null else url
                                                                    },
                                                                contentScale = ContentScale.Crop
                                                            )

                                                            if (keepPopupMounted) {
                                                                val popupScale by animateFloatAsState(
                                                                    targetValue = if (animateIn) 1f else 0.65f,
                                                                    animationSpec = tween(
                                                                        durationMillis = if (animateIn) 240 else 170,
                                                                        easing = if (animateIn) {
                                                                            CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
                                                                        } else {
                                                                            CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
                                                                        }
                                                                    ),
                                                                    label = "cropZoomScale"
                                                                )
                                                                val popupAlpha by animateFloatAsState(
                                                                    targetValue = if (animateIn) 1f else 0f,
                                                                    animationSpec = tween(durationMillis = if (animateIn) 200 else 160),
                                                                    label = "cropZoomAlpha"
                                                                )
                                                                val shadowPadPx = with(density) { 10.dp.roundToPx() }

                                                                Popup(
                                                                    alignment = Alignment.CenterEnd,
                                                                    offset = IntOffset(x = shadowPadPx, y = 0),
                                                                    onDismissRequest = { expandedCropUrl = null }
                                                                ) {
                                                                    val intrinsicSize = currentPainter.intrinsicSize
                                                                    val isSizeValid = intrinsicSize != Size.Unspecified && intrinsicSize.width > 0f && intrinsicSize.height > 0f
                                                                    val imgModifier = if (isSizeValid) {
                                                                        val ratio = intrinsicSize.width / intrinsicSize.height
                                                                        Modifier
                                                                            .widthIn(max = 280.dp)
                                                                            .heightIn(max = 180.dp)
                                                                            .aspectRatio(ratio, matchHeightConstraintsFirst = false)
                                                                    } else {
                                                                        Modifier.size(156.dp)
                                                                    }

                                                                    Box(
                                                                        modifier = Modifier
                                                                            .padding(10.dp)
                                                                            .graphicsLayer {
                                                                                scaleX = popupScale
                                                                                scaleY = popupScale
                                                                                alpha = popupAlpha
                                                                                transformOrigin = TransformOrigin(1f, 0.5f)
                                                                            }
                                                                    ) {
                                                                        androidx.compose.foundation.Image(
                                                                            painter = currentPainter,
                                                                            contentDescription = "Zoomed Crop",
                                                                            modifier = imgModifier
                                                                                .shadow(16.dp, RoundedCornerShape(12.dp))
                                                                                .clip(RoundedCornerShape(12.dp))
                                                                                .background(Color(0xFF0F172A))
                                                                                .clickable { expandedCropUrl = null },
                                                                            contentScale = ContentScale.Fit
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(48.dp)
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // 11. Hành động (.action-data-item)
                                EventInfoDataRow(
                                    label = if (isEn) "Actions" else "Hành động",
                                    labelColor = labelTextColor
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Download Image Button (.download-btn)
                                        PwaActionButton(
                                            icon = LucideIcons.Download,
                                            tint = Color.White,
                                            bgColor = Color(0xFF0980EE),
                                            onClick = {
                                                val imgUrl = getEventImageUrl(event) ?: cropUrls.firstOrNull()
                                                if (!imgUrl.isNullOrBlank()) {
                                                    runCatching {
                                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(imgUrl)))
                                                    }
                                                } else {
                                                    com.tenli.oneview.ui.component.MobileTopPopupManager.warning(
                                                        if (isEn) "No image available" else "Không có hình ảnh để tải về"
                                                    )
                                                }
                                            }
                                        )

                                        // Share Event Button (.share-btn)
                                        PwaActionButton(
                                            icon = LucideIcons.Share2,
                                            tint = Color.White,
                                            bgColor = Color(0xFF10B981),
                                            onClick = {
                                                val imgUrl = getEventImageUrl(event).orEmpty()
                                                val subjectText = if (isEn) "[AI Event] $aiTaskTitle" else "[Sự kiện AI] $aiTaskTitle"
                                                val shareText = buildString {
                                                    appendLine(subjectText)
                                                    appendLine(if (isEn) "Location: $locationText" else "Vị trí: $locationText")
                                                    appendLine(if (isEn) "Time: $formattedTime $formattedDate" else "Thời gian: $formattedTime $formattedDate")
                                                    if (imgUrl.isNotEmpty()) {
                                                        appendLine(if (isEn) "Image: $imgUrl" else "Hình ảnh: $imgUrl")
                                                    }
                                                }
                                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(Intent.EXTRA_SUBJECT, subjectText)
                                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                                }
                                                context.startActivity(Intent.createChooser(sendIntent, if (isEn) "Share event" else "Chia sẻ sự kiện"))
                                            }
                                        )

                                        // Related License Plates Toggle Button (.lpr-btn)
                                        if (canShowRelatedPlates) {
                                            PwaActionButton(
                                                icon = LucideIcons.Barcode,
                                                tint = Color.White,
                                                bgColor = if (uiState.showRelatedPlates) Color(0xFFD97706) else Color(0xFFF59E0B),
                                                onClick = {
                                                    viewModel.toggleRelatedLicensePlates()
                                                }
                                            )
                                        }

                                        // Report Wrong Event Button (.report-btn)
                                        PwaActionButton(
                                            icon = LucideIcons.AlertTriangle,
                                            tint = Color.White,
                                            bgColor = ErrorRed,
                                            onClick = {
                                                showConfirmReportDialog = true
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // =====================================================================
                    // Optional Section: BIỂN SỐ LIÊN QUAN (.related-plates-section in PWA)
                    // =====================================================================
                    if (uiState.showRelatedPlates) {
                        item(key = "section_related_plates") {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 24.dp, end = 14.dp, top = 2.dp, bottom = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isEn) "RELATED LICENSE PLATES" else "BIỂN SỐ LIÊN QUAN",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.6.sp,
                                        color = sectionHeaderColor
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { viewModel.closeRelatedLicensePlates() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = LucideIcons.X,
                                            contentDescription = "Close related plates",
                                            tint = sectionHeaderColor,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp)
                                        .padding(bottom = 14.dp),
                                    color = cardBgColor,
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    if (uiState.relatedPlatesLoading) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(110.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            WaveDotsLoading()
                                        }
                                    } else if (uiState.licensePlateEvents.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 24.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CommonEmptyState(
                                                text = if (isEn) "No related license plates found" else "Không tìm thấy biển số liên quan"
                                            )
                                        }
                                    } else {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState())
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            uiState.licensePlateEvents.forEach { plateEv ->
                                                val isPlateActive = plateEv.id == event.id
                                                val pText = plateEv.data?.plate?.plate ?: plateEv.data?.plate?.number ?: "---"
                                                val pBlack = plateEv.data?.plate?.isBlacklist == true || plateEv.data?.plate?.isBlacklistSnake == true
                                                val pTime = formatTimeOnly(plateEv.time)
                                                val snap = AiTypeHelper.getFullImageUrl(plateEv, UserSession.domain)
                                                    .ifEmpty { AiTypeHelper.getSnapshotUrl(plateEv, UserSession.domain) }

                                                Column(
                                                    modifier = Modifier
                                                        .width(148.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(
                                                            if (isPlateActive) {
                                                                if (isDark) Color(0xFF1B2E4B) else Color(0xFFE8F3FF)
                                                            } else {
                                                                if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC)
                                                            }
                                                        )
                                                        .border(
                                                            width = if (isPlateActive) 1.5.dp else 1.dp,
                                                            color = if (isPlateActive) BrandAccent else if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE2E8F0),
                                                            shape = RoundedCornerShape(10.dp)
                                                        )
                                                        .clickable {
                                                            viewModel.selectEvent(plateEv)
                                                            selectedTab = EventMediaTab.EVENT_IMAGE
                                                            expandedCropUrl = null
                                                        }
                                                        .padding(8.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    VideoFrameImage(
                                                        url = snap.ifEmpty { null },
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .aspectRatio(16f / 9f)
                                                            .clip(RoundedCornerShape(6.dp)),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                    PlateBadgePill(plate = pText, isBlacklist = pBlack)
                                                    Text(
                                                        text = pTime,
                                                        fontSize = 11.sp,
                                                        color = labelTextColor,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // =====================================================================
                    // 3. LỊCH SỬ XUẤT HIỆN (.mobile-history-sidebar in PWA)
                    // =====================================================================
                    item(key = "section_history_header_and_filter") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp)
                        ) {
                            Text(
                                text = if (isEn) "APPEARANCE HISTORY" else "LỊCH SỬ XUẤT HIỆN",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp,
                                color = sectionHeaderColor,
                                modifier = Modifier.padding(start = 14.dp, top = 2.dp, bottom = 8.dp)
                            )

                            // Period Filter Dropdown (.journey-filter-compact BaseSelect with clock icon)
                            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(cardBgColor)
                                        .clickable { showPeriodMenu = !showPeriodMenu }
                                        .padding(horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = LucideIcons.Clock,
                                            contentDescription = null,
                                            tint = if (showPeriodMenu) BrandAccent else sectionHeaderColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        val periodDisplayLabel = remember(uiState.selectedJourneyPeriod, customPickerVersion, isEn) {
                                            uiState.selectedJourneyPeriod.formatDisplayLabel(isEn)
                                        }
                                        Text(
                                            text = periodDisplayLabel,
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isDark) TextPrimaryDark else TextPrimary
                                        )
                                    }
                                    Icon(
                                        imageVector = LucideIcons.ChevronDown,
                                        contentDescription = null,
                                        tint = if (showPeriodMenu) BrandAccent else sectionHeaderColor,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .rotate(if (showPeriodMenu) 180f else 0f)
                                    )
                                }

                                if (showPeriodMenu) {
                                    val popupOffsetY = with(density) { 46.dp.roundToPx() }
                                    val menuBg = if (isDark) Color(0xFF1E293B) else Color.White
                                    val menuBorder = if (isDark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.08f)
                                    val selectedPillBg = BrandAccent.copy(alpha = if (isDark) 0.18f else 0.10f)
                                    val itemTextColor = if (isDark) TextPrimaryDark else TextPrimary

                                    Popup(
                                        alignment = Alignment.TopStart,
                                        offset = IntOffset(0, popupOffsetY),
                                        onDismissRequest = { showPeriodMenu = false },
                                        properties = PopupProperties(focusable = true)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .width(IntrinsicSize.Max)
                                                .widthIn(min = 190.dp)
                                                .shadow(
                                                    elevation = 16.dp,
                                                    shape = RoundedCornerShape(14.dp),
                                                    ambientColor = Color.Black.copy(alpha = if (isDark) 0.5f else 0.15f),
                                                    spotColor = Color.Black.copy(alpha = if (isDark) 0.6f else 0.2f)
                                                )
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(menuBg)
                                                .border(1.dp, menuBorder, RoundedCornerShape(14.dp))
                                                .padding(5.dp),
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            JourneyPeriod.values().forEach { period ->
                                                val isPeriodSelected = uiState.selectedJourneyPeriod == period
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(if (isPeriodSelected) selectedPillBg else Color.Transparent)
                                                        .clickable {
                                                            showPeriodMenu = false
                                                            when (period) {
                                                                JourneyPeriod.CUSTOM_DATE -> showCustomDateModal = true
                                                                JourneyPeriod.CUSTOM_RANGE -> showCustomRangeModal = true
                                                                else -> viewModel.setJourneyPeriod(period)
                                                            }
                                                        }
                                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = if (isEn) period.labelEn else period.labelVi,
                                                        fontSize = 13.5.sp,
                                                        fontWeight = if (isPeriodSelected) FontWeight.SemiBold else FontWeight.Medium,
                                                        color = if (isPeriodSelected) BrandAccent else itemTextColor
                                                    )
                                                    if (isPeriodSelected) {
                                                        Spacer(modifier = Modifier.width(12.dp))
                                                        Icon(
                                                            imageVector = LucideIcons.Check,
                                                            contentDescription = null,
                                                            tint = BrandAccent,
                                                            modifier = Modifier.size(15.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (showCustomDateModal) {
                                com.tenli.oneview.ui.component.CustomDatePickerDialogModal(
                                    initialMillis = com.tenli.oneview.ui.features.home.TimeFilter.customDateStartMs,
                                    isEn = isEn,
                                    isDark = isDark,
                                    onDismiss = { showCustomDateModal = false },
                                    onConfirm = { pickedMillis ->
                                        com.tenli.oneview.ui.features.home.TimeFilter.setCustomDate(pickedMillis)
                                        customPickerVersion++
                                        showCustomDateModal = false
                                        viewModel.setJourneyPeriod(JourneyPeriod.CUSTOM_DATE)
                                    }
                                )
                            }

                            if (showCustomRangeModal) {
                                com.tenli.oneview.ui.component.CustomRangePickerDialogModal(
                                    initialFromMillis = com.tenli.oneview.ui.features.home.TimeFilter.customRangeFromMs,
                                    initialToMillis = com.tenli.oneview.ui.features.home.TimeFilter.customRangeToMs,
                                    isEn = isEn,
                                    isDark = isDark,
                                    onDismiss = { showCustomRangeModal = false },
                                    onConfirm = { fromMillis, toMillis ->
                                        com.tenli.oneview.ui.features.home.TimeFilter.setCustomRange(fromMillis, toMillis)
                                        customPickerVersion++
                                        showCustomRangeModal = false
                                        viewModel.setJourneyPeriod(JourneyPeriod.CUSTOM_RANGE)
                                    }
                                )
                            }
                        }
                    }

                    // History List Content
                    if (uiState.isJourneyLoading) {
                        item(key = "section_history_loading") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                WaveDotsLoading()
                            }
                        }
                    } else if (uiState.relatedEvents.isEmpty()) {
                        item(key = "section_history_empty") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp)
                                    .height(160.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CommonEmptyState(
                                    text = if (isEn) "No related events" else "Không có dữ liệu"
                                )
                            }
                        }
                    } else {
                        itemsIndexed(
                            items = uiState.relatedEvents,
                            key = { idx, relEv -> "hist_${idx}_${relEv.serviceId}_${relEv.id}_${relEv.time}" },
                            contentType = { _, _ -> "history_event_card" }
                        ) { _, relEv ->
                            val isActive = relEv.id == event.id
                            val camUuid = relEv.data?.cameraUUID ?: relEv.data?.cameraUuidLower
                            val relCamName = (if (camUuid != null) cameraMap[camUuid] else null)
                                ?: camera?.name
                                ?: "Camera"

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                MobileEventCard(
                                    event = relEv,
                                    cameraName = relCamName,
                                    isListView = true,
                                    isSelected = isActive,
                                    enableSharedElement = false,
                                    onClick = {
                                        viewModel.selectBaseEvent(relEv)
                                        selectedTab = EventMediaTab.EVENT_IMAGE
                                        expandedCropUrl = null
                                    }
                                )
                            }
                        }

                        // End of history message (.mobile-history-end-message in PWA)
                        item(key = "section_history_end_msg") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 30.dp, vertical = 18.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                HorizontalDivider(
                                    modifier = Modifier.width(48.dp),
                                    thickness = 1.dp,
                                    color = sectionHeaderColor.copy(alpha = 0.25f)
                                )
                                Text(
                                    text = if (isEn) "All events displayed" else "Đã hiển thị tất cả sự kiện",
                                    fontSize = 12.5.sp,
                                    color = sectionHeaderColor.copy(alpha = 0.75f),
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                                HorizontalDivider(
                                    modifier = Modifier.width(48.dp),
                                    thickness = 1.dp,
                                    color = sectionHeaderColor.copy(alpha = 0.25f)
                                )
                            }
                        }
                    }
                }
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
            cancelText = if (isEn) "Cancel" else "Hủy",
            onDismiss = { showConfirmReportDialog = false },
            onConfirm = {
                showConfirmReportDialog = false
                val targetId = uiState.event?.id ?: eventId
                viewModel.reportEventAsFalse(
                    eventId = targetId,
                    onSuccess = {
                        com.tenli.oneview.ui.component.MobileTopPopupManager.success(
                            if (isEn) "Reported false event successfully" else "Đã báo cáo sự kiện sai"
                        )
                        onBack()
                    },
                    onError = { errMsg ->
                        com.tenli.oneview.ui.component.MobileTopPopupManager.error(errMsg)
                    }
                )
            }
        )
    }
}

@Composable
private fun EventInfoDataRow(
    label: String,
    labelColor: Color,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 32.dp)
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = labelColor,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
        Spacer(modifier = Modifier.width(12.dp))
        Box(contentAlignment = Alignment.CenterEnd) {
            content()
        }
    }
}

@Composable
private fun PwaActionButton(
    icon: ImageVector,
    tint: Color,
    bgColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(17.dp)
        )
    }
}

@Composable
private fun PlateBadgePill(
    plate: String,
    isBlacklist: Boolean
) {
    val bgColor = if (isBlacklist) Color(0xFFEF4444) else Color(0xFFEA580C)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = plate,
            color = Color.White,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )
    }
}

/**
 * Matches `aiTaskName` in `BaseAiEventDetailModal.vue` lines 1323-1422.
 */
private fun resolvePwaAiTaskTitle(event: EventData, isEn: Boolean): String {
    val rawType = event.type.trim()
    val tStr = rawType.lowercase()

    if (AiTypeHelper.isFaceEvent(event)) {
        return if (isEn) "Face recognition" else "Nhận diện khuôn mặt"
    }
    if (tStr.contains("lpr") || tStr.contains("plate") || tStr.contains("license") || event.data?.plate != null) {
        return if (isEn) "License plate recognition" else "Nhận diện biển số xe"
    }
    if (tStr.contains("fire") || tStr.contains("smoke")) {
        return if (isEn) "Fire & smoke detection" else "Phát hiện cháy / khói"
    }
    if (tStr.contains("crowd")) {
        return if (isEn) "Crowd detection" else "Phát hiện đám đông"
    }
    if (tStr.contains("uniform") || tStr.contains("clothing")) {
        return if (isEn) "Uniform monitoring" else "Giám sát đồng phục"
    }
    if (tStr.contains("vehicle") || tStr.contains("object-end")) {
        return if (isEn) "Vehicle recognition" else "Nhận diện phương tiện"
    }
    if (tStr.contains("helmet")) {
        return if (isEn) "No helmet detection" else "Phát hiện không đội mũ bảo hiểm"
    }
    if (tStr.contains("parking")) {
        return if (isEn) "Illegal parking" else "Dừng đỗ sai quy định"
    }
    if (tStr.contains("wrong_way") || tStr.contains("crossing")) {
        return if (isEn) "Wrong-way driving" else "Đi ngược chiều"
    }
    if (tStr.contains("person") || tStr.contains("intrusion")) {
        return if (isEn) "Intrusion detection" else "Phát hiện xâm nhập"
    }
    if (tStr.contains("sidewalk") || tStr.contains("encroachment")) {
        return if (isEn) "Sidewalk encroachment" else "Lấn chiếm vỉa hè"
    }
    if (tStr.contains("garbage") || tStr.contains("littering")) {
        return if (isEn) "Littering detection" else "Phát hiện xả rác"
    }
    if (tStr.contains("fall")) {
        return if (isEn) "Fall detection" else "Phát hiện té ngã"
    }
    if (tStr.contains("smoking") || tStr.contains("pose")) {
        return if (isEn) "Smoking detection" else "Phát hiện hút thuốc"
    }
    if (tStr.contains("phone")) {
        return if (isEn) "Phone usage" else "Sử dụng điện thoại"
    }
    if (tStr.contains("violence")) {
        return if (isEn) "Violent behavior" else "Hành vi bạo lực"
    }
    if (tStr.contains("no_mask") || tStr.contains("masked") || tStr.contains("mask")) {
        return if (isEn) "No mask violation" else "Không đeo khẩu trang"
    }
    if (tStr.contains("gloves") || tStr.contains("glove")) {
        return if (isEn) "No gloves violation" else "Không đeo găng tay"
    }
    if (tStr.contains("hairnet")) {
        return if (isEn) "No safety cap" else "Không đội mũ bảo hộ"
    }
    if (tStr.contains("construction")) {
        return if (isEn) "Construction detection" else "Phát hiện thi công"
    }

    return AiTypeHelper.getTypeName(rawType, isEn)
}

/**
 * Matches `formattedTimeObj` in `BaseAiEventDetailModal.vue` lines 3035-3046:
 * `{ time: 'HH:mm:ss', date: 'Hôm nay' | 'DD/MM/YYYY' }`
 */
private fun formatPwaEventTimeAndDate(timestamp: Double, isEn: Boolean): Pair<String, String> {
    if (timestamp <= 0) return Pair("--", "")
    val timeMs = if (timestamp < 100000000000.0) (timestamp * 1000).toLong() else timestamp.toLong()
    val date = Date(timeMs)
    val timeFmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val dateFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val calNow = Calendar.getInstance()
    val calEv = Calendar.getInstance().apply { timeInMillis = timeMs }
    val isToday = calNow.get(Calendar.YEAR) == calEv.get(Calendar.YEAR) &&
        calNow.get(Calendar.DAY_OF_YEAR) == calEv.get(Calendar.DAY_OF_YEAR)

    val dateStr = if (isToday) {
        if (isEn) "Today" else "Hôm nay"
    } else {
        dateFmt.format(date)
    }
    return Pair(timeFmt.format(date), dateStr)
}

private fun formatTimeOnly(timestamp: Double): String {
    if (timestamp <= 0) return "--:--:--"
    val timeMs = if (timestamp < 100000000000.0) (timestamp * 1000).toLong() else timestamp.toLong()
    return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timeMs))
}

private fun isVehicleRelatedEvent(event: EventData, plateNumber: String): Boolean {
    if (plateNumber.isNotEmpty()) return true
    if (AiTypeHelper.isFaceEvent(event)) return false
    val d = event.data
    if (d?.plate != null || d?.vehicle != null) return true
    val t = event.type.lowercase()
    val nonVehicleKeywords = listOf(
        "uniform", "clothing", "fire", "smoke", "crowd", "face", "attendance",
        "fall", "smoking", "phone", "violence", "gloves", "hairnet", "mask",
        "garbage", "littering", "intrusion", "person", "heatmap", "construction"
    )
    if (nonVehicleKeywords.any { t.contains(it) }) return false
    val vehicleKeywords = listOf(
        "license-plate", "lpr", "plate", "vehicle", "object-end", "parking",
        "crossing-tracking", "wrong_way", "wrong-way", "traffic", "helmet",
        "sidewalk", "encroachment"
    )
    return vehicleKeywords.any { t.contains(it) }
}

private fun resolveVehicleTypeLabel(event: EventData, taskTitle: String, isEn: Boolean = false): String {
    val d = event.data ?: return ""
    val rawType = (d.vehicle?.type ?: d.vehicleTypeSnake ?: d.vehicleType ?: d.plate?.vehicleType)?.trim().orEmpty()
    if (rawType.isEmpty()) return ""
    val lower = rawType.lowercase()
    if (lower.startsWith("sensor-") || lower.contains("sensor") || lower == "vehicle" || lower == "phương tiện" || lower == "nhận diện phương tiện") {
        return ""
    }
    if (taskTitle.isNotEmpty() && (lower == taskTitle.lowercase() || taskTitle.lowercase().contains(lower))) {
        return ""
    }
    val map = if (isEn) {
        mapOf(
            "car" to "Car",
            "truck" to "Truck",
            "bus" to "Bus",
            "motorbike" to "Motorbike",
            "motorcycle" to "Motorcycle",
            "bicycle" to "Bicycle",
            "bike" to "Motorbike",
            "van" to "Van / Pickup",
            "container" to "Container truck",
            "suv" to "SUV",
            "sedan" to "Sedan"
        )
    } else {
        mapOf(
            "car" to "Ô tô",
            "truck" to "Xe tải",
            "bus" to "Xe khách / Xe buýt",
            "motorbike" to "Xe máy",
            "motorcycle" to "Xe máy",
            "bicycle" to "Xe đạp",
            "bike" to "Xe máy",
            "van" to "Xe bán tải / Van",
            "container" to "Xe container",
            "suv" to "Xe SUV",
            "sedan" to "Xe Sedan"
        )
    }
    return map[lower] ?: rawType
}

private fun resolveVehicleColorLabel(event: EventData, isEn: Boolean = false): String {
    val d = event.data ?: return ""
    val raw = (d.vehicle?.color ?: d.color ?: d.vehicleColor)?.trim().orEmpty()
    if (raw.isEmpty()) return ""
    val lower = raw.lowercase()
    val map = if (isEn) {
        mapOf(
            "white" to "White",
            "black" to "Black",
            "red" to "Red",
            "blue" to "Blue",
            "green" to "Green",
            "yellow" to "Yellow",
            "silver" to "Silver",
            "grey" to "Gray",
            "gray" to "Gray",
            "white_silver" to "Silver White"
        )
    } else {
        mapOf(
            "white" to "Trắng",
            "black" to "Đen",
            "red" to "Đỏ",
            "blue" to "Xanh dương",
            "green" to "Xanh lá",
            "yellow" to "Vàng",
            "silver" to "Bạc",
            "grey" to "Xám",
            "gray" to "Xám",
            "white_silver" to "Trắng bạc"
        )
    }
    return map[lower] ?: raw
}

private fun resolveFaceProfileDisplayName(
    event: EventData,
    profiles: List<BsProfileModel>,
    isEn: Boolean
): String {
    if (AiTypeHelper.isStranger(event)) {
        return if (isEn) "Stranger" else "Người lạ"
    }
    val d = event.data
    val directName = AiTypeHelper.extractFacePersonName(event, isEn)
    val profileUuid = (d?.face?.profileUUID ?: d?.face?.profileUUIDUpper ?: d?.profileUUID ?: d?.profileUUIDUpper)?.trim()
    val profileId = d?.face?.profileId ?: d?.profileId

    val matchedProfile = when {
        !profileUuid.isNullOrBlank() -> profiles.find { it.uuid.equals(profileUuid, ignoreCase = true) }
        profileId != null -> profiles.find { it.id == profileId }
        directName.isNotBlank() -> profiles.find { it.name.trim().equals(directName.trim(), ignoreCase = true) }
        else -> null
    }

    val baseName = matchedProfile?.name?.takeIf { it.isNotBlank() }
        ?: directName.takeIf { it.isNotBlank() }
        ?: (if (isEn) "Stranger" else "Người lạ")

    val idCode = (d?.idCode ?: d?.idCodeSnake ?: matchedProfile?.idCode ?: matchedProfile?.idCodeSnake)?.trim().orEmpty()
    if (idCode.isNotEmpty() && baseName != "Người lạ" && baseName != "Stranger" && !baseName.contains(idCode)) {
        return "$baseName ($idCode)"
    }
    return baseName
}

private fun resolveFaceGroupNameAndColor(
    event: EventData,
    profiles: List<BsProfileModel>,
    groups: List<BsProfileGroupModel>,
    isEn: Boolean
): Pair<String, Color> {
    if (AiTypeHelper.isStranger(event)) {
        return Pair(if (isEn) "Stranger" else "Người lạ", Color(0xFFDC2626))
    }
    val d = event.data
    val directGroupName = (d?.face?.groupName ?: d?.groupName)?.trim().orEmpty()
    val directGroupColor = d?.face?.groupColor ?: d?.face?.groupColorSnake ?: d?.groupColor ?: d?.groupColorSnake ?: event.groupColor

    val profileUuid = (d?.face?.profileUUID ?: d?.face?.profileUUIDUpper ?: d?.profileUUID ?: d?.profileUUIDUpper)?.trim()
    val profileId = d?.face?.profileId ?: d?.profileId
    val directName = AiTypeHelper.extractFacePersonName(event, isEn)

    val matchedProfile = when {
        !profileUuid.isNullOrBlank() -> profiles.find { it.uuid.equals(profileUuid, ignoreCase = true) }
        profileId != null -> profiles.find { it.id == profileId }
        directName.isNotBlank() -> profiles.find { it.name.trim().equals(directName.trim(), ignoreCase = true) }
        else -> null
    }

    val profileGroup = matchedProfile?.groups?.firstOrNull()
        ?: matchedProfile?.groupIds?.firstOrNull()?.let { gId -> groups.find { it.id == gId } }
        ?: (d?.face?.groupId ?: d?.groupId ?: event.groupId)?.let { gKey ->
            groups.find { it.uuid == gKey || it.id?.toString() == gKey }
        }

    val resolvedGroupName = when {
        directGroupName.isNotEmpty() && directGroupName != "---" -> directGroupName
        !matchedProfile?.groupName.isNullOrBlank() -> matchedProfile!!.groupName!!
        !matchedProfile?.groupNameSnake.isNullOrBlank() -> matchedProfile!!.groupNameSnake!!
        !profileGroup?.name.isNullOrBlank() -> profileGroup!!.name
        else -> "---"
    }

    val colorHex = directGroupColor
        ?: matchedProfile?.groupColor
        ?: matchedProfile?.groupColorSnake
        ?: profileGroup?.config?.ui?.color
        ?: profileGroup?.config?.ui?.background

    val resolvedColor = AiTypeHelper.parseHexColor(colorHex) ?: Color(0xFF0980EE)
    return Pair(resolvedGroupName, resolvedColor)
}
