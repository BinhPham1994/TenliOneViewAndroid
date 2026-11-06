package com.tenli.oneview.ui.component

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.model.network.AIServiceModel
import com.tenli.oneview.model.network.CameraModel
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.ui.features.home.TimeFilter
import com.tenli.oneview.ui.features.home.findAllBoxes
import com.tenli.oneview.ui.navigation.LocalAnimatedVisibilityScope
import com.tenli.oneview.ui.navigation.LocalSharedTransitionScope
import com.tenli.oneview.ui.theme.BrandAccent
import com.tenli.oneview.ui.theme.BrandContainer
import com.tenli.oneview.ui.theme.BrandPrimary
import com.tenli.oneview.ui.theme.ErrorRed
import com.tenli.oneview.ui.theme.LocalAppColors
import com.tenli.oneview.ui.theme.LucideIcons
import com.tenli.oneview.ui.theme.SuccessGreen
import com.tenli.oneview.ui.theme.SuccessGreenDeep
import com.tenli.oneview.ui.theme.VioletAccent
import com.tenli.oneview.ui.theme.WarningOrange
import com.tenli.oneview.ui.theme.appColors
import com.tenli.oneview.ui.theme.spacing
import com.tenli.oneview.ui.utils.AiModuleOption
import com.tenli.oneview.ui.utils.AiTypeHelper
import com.tenli.oneview.ui.utils.LocaleManager
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// =====================================================================================
// Hằng số dùng chung trong file
// =====================================================================================

private val UUID_REGEX = Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")

/** Màu nền popup cảnh báo (.ant-message-warning #C94A08) – palette chưa có tông cam cháy này. */
private val PopupWarningBg = Color(0xFFC94A08)

/** Nền thẻ sự kiện đang được chọn trong Dark mode (#1E3A5F). Light mode dùng [BrandContainer]. */
private val SelectedCardBgDark = Color(0xFF1E3A5F)

/** Bóng chữ nhẹ trên popup màu (giữ độ đọc tốt trên nền đỏ/cam/xanh) */
private val PopupTextStyle = TextStyle(
    fontWeight = FontWeight.SemiBold,
    letterSpacing = 0.2.sp,
    shadow = Shadow(color = Color.Black.copy(alpha = 0.20f), offset = Offset(0f, 2f), blurRadius = 4f)
)

/** Gradient tối phía dưới thẻ Grid để chữ trắng luôn đọc được trên ảnh (tạo 1 lần, dùng lại cho mọi item). */
private val GridBottomScrimBrush = Brush.verticalGradient(
    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f), Color.Black.copy(alpha = 0.9f))
)

/** Độ rộng ảnh thumbnail ở chế độ List (theo thiết kế @365dp, nhân với hệ số co giãn màn hình). */
private val ListThumbnailBaseWidth = 156.dp
private const val THUMBNAIL_ASPECT_RATIO = 16f / 9f

/** Ảnh crop khuôn mặt trên thẻ Grid: 72x90dp (tỉ lệ 4:5) */
private const val FACE_CROP_ASPECT_RATIO = 72f / 90f
private const val FACE_CROP_REQUEST_WIDTH_PX = 160
private const val FACE_CROP_REQUEST_HEIGHT_PX = 200

/** Kích thước decode ảnh (px) – giới hạn để tiết kiệm RAM trong list dài */
private const val LIST_THUMB_WIDTH_PX = 420
private const val LIST_THUMB_HEIGHT_PX = 236
private const val GRID_IMAGE_WIDTH_PX = 800
private const val GRID_IMAGE_HEIGHT_PX = 450

/** Độ dày khung bounding box (px) vẽ trên ảnh Grid */
private const val BOUNDING_BOX_STROKE_PX = 3.5f

/** Số dòng tối đa hiển thị trong dropdown trước khi phải cuộn */
private const val DROPDOWN_MAX_VISIBLE_ITEMS = 6

// --- Animation overlay (scrim fade trước, sheet trượt lên sau) ---
private val OverlayEnterEasing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
private val OverlayExitEasing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
private val ScrimEnterTransition: EnterTransition = fadeIn(animationSpec = tween(durationMillis = 200))
private val ScrimExitTransition: ExitTransition = fadeOut(animationSpec = tween(durationMillis = 260))
private val SheetEnterTransition: EnterTransition = slideInVertically(
    initialOffsetY = { fullHeight -> fullHeight },
    animationSpec = tween(durationMillis = 360, easing = OverlayEnterEasing)
)
private val SheetExitTransition: ExitTransition = slideOutVertically(
    targetOffsetY = { fullHeight -> fullHeight },
    animationSpec = tween(durationMillis = 300, easing = OverlayExitEasing)
)
private const val SCRIM_LEAD_MS = 130L
private const val SHEET_EXIT_LEAD_MS = 80L
private const val SCRIM_EXIT_MS = 280L

// --- Animation popup top ---
private val PopupEnterTransition: EnterTransition = slideInVertically(
    initialOffsetY = { -it / 2 },
    animationSpec = tween(durationMillis = 200, easing = OverlayEnterEasing)
) + scaleIn(
    initialScale = 0.96f,
    animationSpec = tween(durationMillis = 200, easing = OverlayEnterEasing)
) + fadeIn(animationSpec = tween(durationMillis = 160))
private val PopupExitTransition: ExitTransition = slideOutVertically(
    targetOffsetY = { -it / 2 },
    animationSpec = tween(durationMillis = 170)
) + scaleOut(
    targetScale = 0.96f,
    animationSpec = tween(durationMillis = 170)
) + fadeOut(animationSpec = tween(durationMillis = 150))

// --- Định dạng thời gian (java.time: thread-safe, tạo 1 lần) ---
private val TimeSecondsFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
private val HourMinuteFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val DayMonthYearFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

private fun formatMillis(millis: Long, formatter: DateTimeFormatter): String =
    formatter.format(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()))

// =====================================================================================
// Popup thông báo phía trên màn hình
// =====================================================================================

/**
 * Phân loại màu nền Popup Top đồng bộ 100% với `src/assets/styles/notifications.css` (lines 176-230) trên PWA:
 * 1. ERROR (Đỏ): #EF4444 (.ant-message-error)
 * 2. WARNING (Cam): #C94A08 (.ant-message-warning)
 * 3. SUCCESS (Xanh lá): #15803D (.ant-message-success)
 * 4. INFO (Xanh dương / Mặc định): #0980EE (.ant-message-info)
 */
enum class MobileTopPopupType(
    val bgColor: Color,
    val shadowColor: Color
) {
    ERROR(
        bgColor = ErrorRed,
        shadowColor = ErrorRed.copy(alpha = 0.45f)
    ),
    WARNING(
        bgColor = PopupWarningBg,
        shadowColor = PopupWarningBg.copy(alpha = 0.50f)
    ),
    SUCCESS(
        bgColor = SuccessGreenDeep,
        shadowColor = SuccessGreenDeep.copy(alpha = 0.50f)
    ),
    INFO(
        bgColor = BrandAccent,
        shadowColor = BrandAccent.copy(alpha = 0.45f)
    )
}

data class MobileTopPopupMessage(
    val id: Long = System.nanoTime(),
    val text: String,
    val type: MobileTopPopupType = MobileTopPopupType.INFO,
    val durationMs: Long = 2600L
)

object MobileTopPopupManager {
    private val _currentMessage = MutableStateFlow<MobileTopPopupMessage?>(null)
    val currentMessage: StateFlow<MobileTopPopupMessage?> = _currentMessage.asStateFlow()

    fun show(
        text: String,
        type: MobileTopPopupType = MobileTopPopupType.INFO,
        durationMs: Long = 2600L
    ) {
        if (text.isBlank()) return
        _currentMessage.value = MobileTopPopupMessage(
            id = System.nanoTime(),
            text = text.trim(),
            type = type,
            durationMs = durationMs
        )
    }

    fun error(text: String, durationMs: Long = 2600L) {
        show(text = text, type = MobileTopPopupType.ERROR, durationMs = durationMs)
    }

    fun warning(text: String, durationMs: Long = 2600L) {
        show(text = text, type = MobileTopPopupType.WARNING, durationMs = durationMs)
    }

    fun success(text: String, durationMs: Long = 2600L) {
        show(text = text, type = MobileTopPopupType.SUCCESS, durationMs = durationMs)
    }

    fun info(text: String, durationMs: Long = 2600L) {
        show(text = text, type = MobileTopPopupType.INFO, durationMs = durationMs)
    }

    fun dismiss() {
        _currentMessage.value = null
    }
}

@Composable
fun MobileTopPopupHost(
    modifier: Modifier = Modifier
) {
    val activeMessage by MobileTopPopupManager.currentMessage.collectAsStateWithLifecycle()
    // Giữ message cuối để còn hiển thị trong lúc animation thoát (holder thường, không phải state → không gây recompose thêm)
    val lastRendered = remember { arrayOfNulls<MobileTopPopupMessage>(1) }
    activeMessage?.let { lastRendered[0] = it }

    LaunchedEffect(activeMessage?.id) {
        val current = activeMessage ?: return@LaunchedEffect
        delay(current.durationMs)
        if (MobileTopPopupManager.currentMessage.value?.id == current.id) {
            MobileTopPopupManager.dismiss()
        }
    }

    val spacing = MaterialTheme.spacing
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = spacing.space8, start = spacing.space16, end = spacing.space16),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = activeMessage != null,
            enter = PopupEnterTransition,
            exit = PopupExitTransition
        ) {
            val msg = activeMessage ?: lastRendered[0]
            if (msg != null) {
                MobileTopPopupCard(msg)
            }
        }
    }
}

@Composable
private fun MobileTopPopupCard(msg: MobileTopPopupMessage) {
    val spacing = MaterialTheme.spacing
    val contentColor = MaterialTheme.appColors.onAccent
    val shape = RoundedCornerShape(spacing.radiusSmallMedium)
    Row(
        modifier = Modifier
            .widthIn(max = spacing.dialogMaxWidth)
            .shadow(
                elevation = spacing.space10,
                shape = shape,
                ambientColor = msg.type.shadowColor,
                spotColor = msg.type.shadowColor
            )
            .clip(shape)
            .background(msg.type.bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { MobileTopPopupManager.dismiss() }
            )
            .padding(horizontal = spacing.space18, vertical = spacing.space8),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.space10)
    ) {
        MobileTopPopupFilledIcon(
            type = msg.type,
            circleColor = contentColor,
            cutoutColor = msg.type.bgColor
        )

        Text(
            text = msg.text,
            color = contentColor,
            style = MaterialTheme.typography.bodySmall.merge(PopupTextStyle),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MobileTopPopupFilledIcon(
    type: MobileTopPopupType,
    circleColor: Color,
    cutoutColor: Color
) {
    Canvas(modifier = Modifier.size(MaterialTheme.spacing.iconSmall)) {
        val w = size.width
        val h = size.height
        val radius = minOf(w, h) / 2f
        val strokeW = w * 0.12f

        // Solid white circle matching `.ant-message-custom-content .anticon svg { fill: #ffffff }`
        drawCircle(
            color = circleColor,
            radius = radius,
            center = Offset(w / 2f, h / 2f)
        )

        when (type) {
            MobileTopPopupType.SUCCESS -> {
                val checkPath = Path().apply {
                    moveTo(w * 0.29f, h * 0.52f)
                    lineTo(w * 0.44f, h * 0.67f)
                    lineTo(w * 0.72f, h * 0.36f)
                }
                drawPath(
                    path = checkPath,
                    color = cutoutColor,
                    style = Stroke(
                        width = strokeW,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            MobileTopPopupType.WARNING -> {
                drawLine(
                    color = cutoutColor,
                    start = Offset(w * 0.5f, h * 0.28f),
                    end = Offset(w * 0.5f, h * 0.55f),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = cutoutColor,
                    radius = strokeW * 0.58f,
                    center = Offset(w * 0.5f, h * 0.72f)
                )
            }

            MobileTopPopupType.ERROR -> {
                drawLine(
                    color = cutoutColor,
                    start = Offset(w * 0.35f, h * 0.35f),
                    end = Offset(w * 0.65f, h * 0.65f),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = cutoutColor,
                    start = Offset(w * 0.65f, h * 0.35f),
                    end = Offset(w * 0.35f, h * 0.65f),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
            }

            MobileTopPopupType.INFO -> {
                drawCircle(
                    color = cutoutColor,
                    radius = strokeW * 0.58f,
                    center = Offset(w * 0.5f, h * 0.30f)
                )
                drawLine(
                    color = cutoutColor,
                    start = Offset(w * 0.5f, h * 0.47f),
                    end = Offset(w * 0.5f, h * 0.72f),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

// =====================================================================================
// Overlay toàn màn hình (bottom sheet / dialog tùy biến)
// =====================================================================================

private class MobileOverlayEntry(
    val key: Any,
    var onDismissRequest: () -> Unit,
    var content: @Composable () -> Unit
) {
    var isScrimVisible by mutableStateOf(false)
    var isSheetVisible by mutableStateOf(false)
    var isClosing by mutableStateOf(false)
}

class MobileOverlayHostState {
    private val entries = mutableStateListOf<MobileOverlayEntry>()

    fun show(
        key: Any,
        onDismissRequest: () -> Unit = {},
        content: @Composable () -> Unit
    ) {
        val idx = entries.indexOfFirst { it.key == key }
        if (idx >= 0) {
            entries[idx].onDismissRequest = onDismissRequest
            entries[idx].content = content
            entries[idx].isClosing = false
            entries[idx].isScrimVisible = true
            entries[idx].isSheetVisible = true
        } else {
            entries.add(MobileOverlayEntry(key, onDismissRequest, content))
        }
    }

    fun hide(key: Any) {
        val idx = entries.indexOfFirst { it.key == key }
        if (idx >= 0) {
            entries[idx].isClosing = true
            entries[idx].isSheetVisible = false
        }
    }

    val hasOverlays: Boolean
        get() = entries.isNotEmpty()

    @Composable
    fun Content() {
        entries.forEach { entry ->
            androidx.compose.runtime.key(entry.key) {
                LaunchedEffect(entry.key, entry.isClosing) {
                    if (!entry.isClosing) {
                        // Hiển thị lớp nền tối trước, sau đó mới trượt Bottom Sheet từ dưới lên
                        entry.isScrimVisible = true
                        delay(SCRIM_LEAD_MS)
                        if (!entry.isClosing) {
                            entry.isSheetVisible = true
                        }
                    } else {
                        // Khi đóng: trượt Bottom Sheet xuống trước một nhịp rồi làm mờ dần nền tối
                        entry.isSheetVisible = false
                        delay(SHEET_EXIT_LEAD_MS)
                        entry.isScrimVisible = false
                        delay(SCRIM_EXIT_MS)
                        entries.remove(entry)
                    }
                }

                OverlayLayers(
                    isScrimVisible = entry.isScrimVisible,
                    isSheetVisible = entry.isSheetVisible,
                    onDismissRequest = entry.onDismissRequest,
                    content = entry.content
                )
            }
        }
    }
}

/**
 * Lớp nền mờ (fade) + nội dung sheet (trượt từ dưới lên + hỗ trợ vuốt từ trên xuống để đóng) –
 * dùng chung cho host và chế độ fallback.
 */
@Composable
private fun OverlayLayers(
    isScrimVisible: Boolean,
    isSheetVisible: Boolean,
    onDismissRequest: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val scrimColor = MaterialTheme.appColors.scrim
    val density = LocalDensity.current
    val dismissThresholdPx = remember(density) { with(density) { 68.dp.toPx() } }
    val dismissVelocityPx = remember(density) { with(density) { 420.dp.toPx() } }
    val scope = rememberCoroutineScope()
    val currentOnDismiss by rememberUpdatedState(onDismissRequest)

    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var isDismissedBySwipe by remember { mutableStateOf(false) }

    val finishDrag: (Float) -> Unit = remember(scope, dismissThresholdPx, dismissVelocityPx) {
        { velocityY ->
            isDragging = false
            val shouldDismiss =
                dragOffsetY >= dismissThresholdPx ||
                    (dragOffsetY > 12f && velocityY >= dismissVelocityPx)
            if (shouldDismiss && !isDismissedBySwipe) {
                isDismissedBySwipe = true
                currentOnDismiss()
            } else if (dragOffsetY > 0f && !isDismissedBySwipe) {
                val startOffset = dragOffsetY
                scope.launch {
                    animate(
                        initialValue = startOffset,
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
                    ) { value, _ ->
                        if (!isDragging && !isDismissedBySwipe) {
                            dragOffsetY = value
                        }
                    }
                }
            }
        }
    }

    val nestedScrollConnection = remember(finishDrag) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput && dragOffsetY > 0f && available.y < 0f) {
                    val consumedY = maxOf(available.y, -dragOffsetY)
                    dragOffsetY = (dragOffsetY + consumedY).coerceAtLeast(0f)
                    return Offset(0f, consumedY)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (source == NestedScrollSource.UserInput && available.y > 0f && !isDismissedBySwipe) {
                    isDragging = true
                    dragOffsetY = (dragOffsetY + available.y).coerceAtLeast(0f)
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (dragOffsetY > 0f) {
                    finishDrag(available.y)
                    return available
                }
                return Velocity.Zero
            }
        }
    }

    AnimatedVisibility(
        visible = isScrimVisible,
        enter = ScrimEnterTransition,
        exit = ScrimExitTransition
    ) {
        val dragScrimAlpha =
            (1f - (dragOffsetY / (dismissThresholdPx * 3.5f))).coerceIn(0.25f, 1f)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = dragScrimAlpha }
                .background(scrimColor)
        )
    }
    AnimatedVisibility(
        visible = isSheetVisible,
        enter = SheetEnterTransition,
        exit = SheetExitTransition
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
                .pointerInput(Unit) {
                    val velocityTracker = VelocityTracker()
                    detectVerticalDragGestures(
                        onDragStart = {
                            velocityTracker.resetTracking()
                            isDragging = true
                        },
                        onVerticalDrag = { change, dragAmount ->
                            velocityTracker.addPosition(change.uptimeMillis, change.position)
                            if ((dragAmount > 0f || dragOffsetY > 0f) && !isDismissedBySwipe) {
                                change.consume()
                                isDragging = true
                                dragOffsetY = (dragOffsetY + dragAmount).coerceAtLeast(0f)
                            }
                        },
                        onDragEnd = {
                            val velocityY = velocityTracker.calculateVelocity().y
                            finishDrag(velocityY)
                        },
                        onDragCancel = {
                            finishDrag(0f)
                        }
                    )
                }
                .graphicsLayer {
                    translationY = dragOffsetY
                }
        ) {
            content()
        }
    }
}

val LocalMobileOverlayHost = staticCompositionLocalOf<MobileOverlayHostState?> { null }

@Composable
fun MobileFullScreenOverlay(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    val host = LocalMobileOverlayHost.current
    val currentOnDismiss by rememberUpdatedState(onDismissRequest)
    val currentContentState = rememberUpdatedState(content)
    val key = remember { Any() }

    androidx.activity.compose.BackHandler(enabled = true) {
        currentOnDismiss()
    }

    if (host != null) {
        DisposableEffect(host, key) {
            host.show(
                key = key,
                onDismissRequest = { currentOnDismiss() },
                content = {
                    currentContentState.value.invoke()
                }
            )
            onDispose {
                host.hide(key)
            }
        }
    } else {
        var isScrimVisible by remember { mutableStateOf(false) }
        var isSheetVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            isScrimVisible = true
            delay(SCRIM_LEAD_MS)
            isSheetVisible = true
        }
        Box(modifier = Modifier.fillMaxSize()) {
            OverlayLayers(
                isScrimVisible = isScrimVisible,
                isSheetVisible = isSheetVisible,
                onDismissRequest = { currentOnDismiss() },
                content = content
            )
        }
    }
}

fun isUuidString(value: String?): Boolean {
    if (value.isNullOrBlank()) return false
    return UUID_REGEX.matches(value.trim())
}

// =====================================================================================
// Theme / ngôn ngữ
// =====================================================================================

/**
 * Trạng thái Dark mode hiện tại của app (đã tính theo cài đặt người dùng + hệ thống trong [com.tenli.oneview.ui.theme.TenliAIoTTheme]).
 * Đọc trực tiếp từ CompositionLocal nên không phát sinh thêm flow collector mỗi lần gọi.
 */
@Composable
@ReadOnlyComposable
fun rememberIsDarkTheme(): Boolean = LocalAppColors.current.isDark

/**
 * Ngôn ngữ hiện tại có phải tiếng Anh không – được cung cấp 1 lần ở gốc cây UI bởi [ProvideAppLanguage].
 * `null` = chưa có provider (khi đó [rememberIsEnglish] tự collect flow như cũ).
 */
val LocalIsEnglish = staticCompositionLocalOf<Boolean?> { null }

/**
 * Thu thập [LocaleManager.languageFlow] DUY NHẤT 1 lần và cung cấp cho toàn bộ cây con qua [LocalIsEnglish].
 * Nên bọc ở gốc UI (MainActivity: `TenliAIoTTheme { ProvideAppLanguage { ... } }`) để mọi item trong list
 * gọi [rememberIsEnglish] chỉ còn là 1 lần đọc CompositionLocal (không tạo coroutine/collector riêng).
 */
@Composable
fun ProvideAppLanguage(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val langCode by LocaleManager.languageFlow.collectAsStateWithLifecycle(
        initialValue = remember(context) { LocaleManager.getLocale(context) }
    )
    CompositionLocalProvider(LocalIsEnglish provides langCode.startsWith("en"), content = content)
}

@Composable
fun rememberIsEnglish(): Boolean {
    LocalIsEnglish.current?.let { return it }
    // Fallback khi chưa bọc ProvideAppLanguage: tự collect flow (giữ hành vi cũ)
    val context = LocalContext.current
    val langCode by LocaleManager.languageFlow.collectAsStateWithLifecycle(
        initialValue = remember(context) { LocaleManager.getLocale(context) }
    )
    return langCode.startsWith("en")
}

// =====================================================================================
// Top bar
// =====================================================================================

/**
 * Top bar matching `.mobile-top-bar` in `mobile.css`
 */
@Composable
fun MobileTopBar(
    title: String,
    leftAction: @Composable (() -> Unit)? = null,
    rightAction: @Composable (() -> Unit)? = null
) {
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.appColors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(spacing.touchTarget)
            .background(colors.pageBg)
            .padding(horizontal = spacing.space10),
        contentAlignment = Alignment.Center
    ) {
        if (leftAction != null) {
            Box(modifier = Modifier.align(Alignment.CenterStart)) {
                leftAction()
            }
        }

        Text(
            text = title,
            color = colors.textTitle,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            // Chừa chỗ cho nút trái/phải để tiêu đề luôn nằm giữa
            modifier = Modifier.padding(horizontal = spacing.iconButtonLarge)
        )

        if (rightAction != null) {
            Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                rightAction()
            }
        }
    }
}

@Composable
fun MobileTopActionButton(
    icon: ImageVector,
    contentDescription: String? = null,
    hasActiveDot: Boolean = false,
    onClick: () -> Unit
) {
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.appColors

    TopBarIconBox(onClick = onClick, cornerRadius = spacing.radiusSmallMedium) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = colors.iconDefault,
            modifier = Modifier.size(spacing.iconRegular)
        )
        if (hasActiveDot) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = spacing.space5, end = spacing.space5)
                    .size(spacing.statusDot)
                    .clip(CircleShape)
                    .background(colors.accent)
            )
        }
    }
}

@Composable
fun MobileViewToggleButton(
    isListView: Boolean,
    onToggle: () -> Unit
) {
    val spacing = MaterialTheme.spacing
    TopBarIconBox(onClick = onToggle, cornerRadius = spacing.radiusSmall) {
        Icon(
            imageVector = if (isListView) LucideIcons.LayoutGrid else LucideIcons.List,
            contentDescription = if (isListView) "Grid View" else "List View",
            tint = MaterialTheme.appColors.iconDefault,
            modifier = Modifier.size(spacing.iconRegular)
        )
    }
}

/** Vùng chạm vuông bo góc cho nút icon trên top bar */
@Composable
private fun TopBarIconBox(
    onClick: () -> Unit,
    cornerRadius: Dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .size(MaterialTheme.spacing.iconButton)
            .clip(RoundedCornerShape(cornerRadius))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
        content = content
    )
}

// =====================================================================================
// Thẻ sự kiện (dùng trong list dài: Home, Event, EventDetail) – giữ thật nhẹ
// =====================================================================================

/** Dữ liệu hiển thị tính sẵn 1 lần cho mỗi sự kiện (không phụ thuộc ngôn ngữ). */
@Immutable
private class EventCardImages(
    val cameraName: String,
    val snapshotUrl: String?,
    val fullImageUrl: String?,
    val faceCropUrl: String?
)

private fun resolveCardImages(event: EventData, cameraName: String, domain: String): EventCardImages {
    val resolvedCameraName = cameraName.takeIf {
        it.isNotBlank() && !isUuidString(it) && !it.equals("Camera", ignoreCase = true)
    }
        ?: event.data?.cameraName?.takeIf { it.isNotBlank() && !isUuidString(it) }
        ?: event.cameraName?.takeIf { it.isNotBlank() && !isUuidString(it) }
        ?: ""
    val faceUrl = AiTypeHelper.getFaceCropUrl(event, domain)
    val snapUrl = AiTypeHelper.getSnapshotUrl(event, domain)
    val fullUrl = AiTypeHelper.getFullImageUrl(event, domain)
    val fullImageUrl = fullUrl.ifEmpty { snapUrl }.ifEmpty { null }
    return EventCardImages(
        cameraName = resolvedCameraName,
        snapshotUrl = (faceUrl ?: snapUrl.ifEmpty { fullUrl }).ifEmpty { null },
        fullImageUrl = fullImageUrl,
        faceCropUrl = faceUrl?.takeIf { it.isNotBlank() && it != fullImageUrl }
    )
}

/** Gắn shared element (ảnh sự kiện → màn chi tiết) khi được bật và có scope điều hướng. */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun Modifier.eventImageSharedElement(enabled: Boolean, eventId: Any?): Modifier {
    if (!enabled) return this
    val sharedTransitionScope = LocalSharedTransitionScope.current ?: return this
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current ?: return this
    return with(sharedTransitionScope) {
        this@eventImageSharedElement.sharedElement(
            sharedContentState = rememberSharedContentState(key = "event_image_$eventId"),
            animatedVisibilityScope = animatedVisibilityScope
        )
    }
}

/**
 * Cloned from `MobileEventCard.vue` supporting both List View (`isListView = true`) and Grid/Card View (`isListView = false`).
 */
@Composable
fun MobileEventCard(
    event: EventData,
    cameraName: String,
    isListView: Boolean = true,
    isSelected: Boolean = false,
    enableSharedElement: Boolean = false,
    onClick: () -> Unit
) {
    val isEn = rememberIsEnglish()
    val colors = MaterialTheme.appColors
    val domain = UserSession.domain
    val eventKey = "${event.serviceId}_${event.id}_${event.time}"

    val images = remember(eventKey, cameraName, domain) {
        resolveCardImages(event, cameraName, domain)
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var isTapFlashing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val currentOnClick by rememberUpdatedState(onClick)
    val currentIsPressed by rememberUpdatedState(isPressed)

    val handleCardClick: () -> Unit = remember(scope) {
        {
            if (currentIsPressed) {
                isTapFlashing = true
                currentOnClick()
                scope.launch {
                    delay(180L)
                    isTapFlashing = false
                }
            } else {
                // Khi chạm nhanh (<100ms) trong LazyColumn, isPressed bị trễ bởi TapIndicationDelay.
                // Bật nền phản hồi ngay lập tức trong ~60ms trước khi chuyển màn hình để người dùng cảm nhận rõ thao tác bấm.
                isTapFlashing = true
                scope.launch {
                    delay(60L)
                    currentOnClick()
                    delay(180L)
                    isTapFlashing = false
                }
            }
        }
    }

    val isActivePress = isPressed || isTapFlashing
    val targetBgColor = when {
        isActivePress && colors.isDark -> Color(0xFF28374E)
        isActivePress -> colors.closeButtonBg
        !isSelected -> colors.surface
        colors.isDark -> SelectedCardBgDark
        else -> BrandContainer
    }
    val cardBgColor by animateColorAsState(
        targetValue = targetBgColor,
        animationSpec = tween(durationMillis = if (isActivePress) 35 else 180),
        label = "eventCardBg"
    )
    val pressScale by animateFloatAsState(
        targetValue = if (isActivePress) 0.982f else 1f,
        animationSpec = tween(durationMillis = if (isActivePress) 45 else 160),
        label = "eventCardScale"
    )

    if (isListView) {
        EventListCard(
            event = event,
            eventKey = eventKey,
            images = images,
            isEn = isEn,
            cardBgColor = cardBgColor,
            pressScale = pressScale,
            interactionSource = interactionSource,
            enableSharedElement = enableSharedElement,
            onClick = handleCardClick
        )
    } else {
        EventGridCard(
            event = event,
            eventKey = eventKey,
            images = images,
            isEn = isEn,
            cardBgColor = cardBgColor,
            pressScale = pressScale,
            isActivePress = isActivePress,
            interactionSource = interactionSource,
            enableSharedElement = enableSharedElement,
            onClick = handleCardClick
        )
    }
}

/** Horizontal List Card matching `.mobile-event-card-horizontal` */
@Composable
private fun EventListCard(
    event: EventData,
    eventKey: String,
    images: EventCardImages,
    isEn: Boolean,
    cardBgColor: Color,
    pressScale: Float,
    interactionSource: MutableInteractionSource,
    enableSharedElement: Boolean,
    onClick: () -> Unit
) {
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.appColors
    val typography = MaterialTheme.typography
    val prefixSuffixColor = colors.textBody
    val strongCameraColor = colors.textTitle

    val sentenceParts = remember(eventKey, images.cameraName, isEn) {
        AiTypeHelper.getEventSentenceParts(event, images.cameraName, isEn)
    }
    val relativeTime = remember(event.time, isEn) {
        AiTypeHelper.formatRelativeTime(event.time, isEn)
    }
    // Line 3: Camera clause ("tại CameraName")
    val suffixAnnotated = remember(sentenceParts.cameraName, isEn, prefixSuffixColor, strongCameraColor) {
        buildAnnotatedString {
            val trimmed = sentenceParts.cameraName.trim()
            if (trimmed.isEmpty() || isUuidString(trimmed) || trimmed.equals("camera", ignoreCase = true)) {
                withStyle(SpanStyle(color = prefixSuffixColor)) {
                    append(if (isEn) "at camera" else "tại camera")
                }
            } else {
                withStyle(SpanStyle(color = prefixSuffixColor)) {
                    append(if (isEn) "at " else "tại ")
                }
                withStyle(SpanStyle(color = strongCameraColor, fontWeight = FontWeight.SemiBold)) {
                    append(trimmed)
                }
            }
        }
    }

    val cardShape = remember(spacing) { RoundedCornerShape(spacing.radiusCompact) }
    val thumbShape = remember(spacing) { RoundedCornerShape(spacing.radiusSmallMedium) }
    val pillShape = remember(spacing) { RoundedCornerShape(spacing.radiusXSmall) }
    val thumbWidth = ListThumbnailBaseWidth * spacing.scaleFactor

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clip(cardShape)
            .background(cardBgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(start = spacing.space8, top = spacing.space8, bottom = spacing.space8, end = spacing.space12),
        verticalAlignment = Alignment.CenterVertically
    ) {
        VideoFrameImage(
            url = images.snapshotUrl,
            modifier = Modifier
                .width(thumbWidth)
                .aspectRatio(THUMBNAIL_ASPECT_RATIO)
                .clip(thumbShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .eventImageSharedElement(enableSharedElement, event.id),
            contentScale = ContentScale.Crop,
            targetWidthPx = LIST_THUMB_WIDTH_PX,
            targetHeightPx = LIST_THUMB_HEIGHT_PX
        )

        Spacer(modifier = Modifier.width(spacing.space12))

        Column(
            modifier = Modifier
                .weight(1f)
                // Cao tối thiểu bằng thumbnail để dòng thời gian luôn nằm sát đáy
                .heightIn(min = thumbWidth / THUMBNAIL_ASPECT_RATIO),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.space3)) {
                // Line 1: Prefix (e.g., "Phát hiện khuôn mặt", "Phát hiện vi phạm")
                if (sentenceParts.prefix.isNotEmpty()) {
                    Text(
                        text = sentenceParts.prefix,
                        color = prefixSuffixColor,
                        style = typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Line 2: Highlighted pill subject
                if (sentenceParts.subject.isNotEmpty()) {
                    Text(
                        text = sentenceParts.subject,
                        color = colors.onAccent,
                        style = typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .clip(pillShape)
                            .background(sentenceParts.color)
                            .padding(horizontal = spacing.space6, vertical = spacing.space2)
                    )
                }

                Text(
                    text = suffixAnnotated,
                    style = typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Bottom row: Relative time ("5 phút trước")
            Text(
                text = relativeTime,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Grid / Large 16:9 Card View matching `.mobile-event-card` */
@Composable
private fun EventGridCard(
    event: EventData,
    eventKey: String,
    images: EventCardImages,
    isEn: Boolean,
    cardBgColor: Color,
    pressScale: Float,
    isActivePress: Boolean,
    interactionSource: MutableInteractionSource,
    enableSharedElement: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.appColors
    val typography = MaterialTheme.typography
    val onMedia = colors.onMedia

    val badgeLabel = remember(eventKey, isEn) { AiTypeHelper.getEventBadgeLabel(event, isEn) }
    val badgeColor = remember(eventKey) { AiTypeHelper.getAiColor(event) }
    val (formattedTime, formattedDate) = remember(event.time, isEn) {
        formatGridEventTimeAndDate(event.time, isEn)
    }
    val boxes = remember(eventKey) { findAllBoxes(event) }

    val cardShape = remember(spacing) { RoundedCornerShape(spacing.radiusCompact) }
    val faceShape = remember(spacing) { RoundedCornerShape(spacing.radiusSmall) }
    val pillShape = remember(spacing) { RoundedCornerShape(spacing.radiusXSmall) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(THUMBNAIL_ASPECT_RATIO)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clip(cardShape)
            .background(cardBgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .eventImageSharedElement(enableSharedElement, event.id)
    ) {
        VideoFrameImage(
            url = images.fullImageUrl,
            modifier = Modifier
                .fillMaxSize()
                .then(if (boxes.isEmpty()) Modifier else Modifier.boundingBoxes(boxes)),
            contentScale = ContentScale.Crop,
            targetWidthPx = GRID_IMAGE_WIDTH_PX,
            targetHeightPx = GRID_IMAGE_HEIGHT_PX
        )

        if (isActivePress) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.14f))
            )
        }

        // Face crop overlay on TopEnd (72x90dp)
        val faceCropUrl = images.faceCropUrl
        if (faceCropUrl != null) {
            val faceCropRequest = remember(faceCropUrl) {
                ImageRequest.Builder(context)
                    .data(faceCropUrl)
                    .size(FACE_CROP_REQUEST_WIDTH_PX, FACE_CROP_REQUEST_HEIGHT_PX)
                    .precision(Precision.INEXACT)
                    .allowHardware(true)
                    .crossfade(false)
                    .memoryCacheKey("${faceCropUrl}_${FACE_CROP_REQUEST_WIDTH_PX}x${FACE_CROP_REQUEST_HEIGHT_PX}")
                    .diskCacheKey(faceCropUrl)
                    .build()
            }
            AsyncImage(
                model = faceCropRequest,
                contentDescription = "Face Crop",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(spacing.space5)
                    .width(spacing.avatarExtraLarge)
                    .aspectRatio(FACE_CROP_ASPECT_RATIO)
                    .clip(faceShape)
                    .background(colors.mediaOverlay)
                    .border(spacing.borderMedium, onMedia.copy(alpha = 0.25f), faceShape)
            )
        }

        // Bottom gradient overlay
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(GridBottomScrimBrush)
                .padding(top = spacing.space24, bottom = spacing.space8, start = spacing.space10, end = spacing.space10),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(spacing.space3)
            ) {
                Text(
                    text = badgeLabel,
                    color = onMedia,
                    style = typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .clip(pillShape)
                        .background(badgeColor)
                        .padding(horizontal = spacing.space8, vertical = spacing.space2)
                )
                if (images.cameraName.isNotEmpty()) {
                    Text(
                        text = images.cameraName,
                        color = onMedia,
                        style = typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(spacing.space10))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(spacing.space2)
            ) {
                Text(
                    text = formattedTime,
                    color = onMedia,
                    style = typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formattedDate,
                    color = onMedia.copy(alpha = 0.8f),
                    style = typography.labelSmall
                )
            }
        }
    }
}

/** Vẽ các khung bounding box (toạ độ chuẩn hoá 0..1) lên trên ảnh – chỉ chạy ở pha draw. */
private fun Modifier.boundingBoxes(boxes: List<List<Float>>): Modifier = drawWithContent {
    drawContent()
    boxes.forEach { boxArr ->
        if (boxArr.size >= 4) {
            val minX = boxArr[0]
            val minY = boxArr[1]
            val maxX = boxArr[2]
            val maxY = boxArr[3]
            drawRect(
                color = ErrorRed,
                topLeft = Offset(minX * size.width, minY * size.height),
                size = Size((maxX - minX) * size.width, (maxY - minY) * size.height),
                style = Stroke(width = BOUNDING_BOX_STROKE_PX)
            )
        }
    }
}

private fun formatGridEventTimeAndDate(timeSeconds: Double, isEn: Boolean): Pair<String, String> {
    if (timeSeconds <= 0) return Pair("", "")
    val timeMs = if (timeSeconds < 1e11) (timeSeconds * 1000).toLong() else timeSeconds.toLong()
    val zone = ZoneId.systemDefault()
    val dateTime = Instant.ofEpochMilli(timeMs).atZone(zone)
    val isToday = dateTime.toLocalDate() == LocalDate.now(zone)

    val dateLabel = if (isToday) {
        if (isEn) "Today" else "Hôm nay"
    } else {
        DayMonthYearFormatter.format(dateTime)
    }

    return Pair(TimeSecondsFormatter.format(dateTime), dateLabel)
}

// =====================================================================================
// Bottom sheet bộ lọc
// =====================================================================================

/** Các ô chọn trong bộ lọc – mỗi ô có 1 dropdown neo ngay bên dưới/bên trên. */
private enum class FilterPicker { SCOPE, TIME, STREAM_CHANNEL, CAMERA, EVENT }

/**
 * Cloned from `MobileFilterBottomSheet.vue`
 */
@Composable
fun MobileFilterBottomSheet(
    title: String = "Bộ lọc",
    showScope: Boolean = true,
    aiServices: List<AIServiceModel> = emptyList(),
    selectedServiceId: Int? = null,
    showTime: Boolean = true,
    selectedTimeFilter: TimeFilter = TimeFilter.TODAY,
    showPlaybackChannel: Boolean = false,
    selectedPlaybackChannel: String = "main",
    defaultPlaybackChannel: String = "main",
    onApplyPlayback: ((timeFilter: TimeFilter, playbackChannel: String) -> Unit)? = null,
    showCamera: Boolean = false,
    cameras: List<CameraModel> = emptyList(),
    selectedCameraUuid: String? = null,
    showEventType: Boolean = false,
    activeModules: List<AiModuleOption> = AiTypeHelper.activeModules,
    selectedAiType: String? = null,
    onDismiss: () -> Unit,
    onApply: (serviceId: Int?, timeFilter: TimeFilter, cameraUuid: String?, aiType: String?) -> Unit = { _, _, _, _ -> },
    onReset: () -> Unit
) {
    val isEn = rememberIsEnglish()
    val isDark = rememberIsDarkTheme()
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.appColors

    var draftServiceId by remember(selectedServiceId) { mutableStateOf(selectedServiceId) }
    var draftTimeFilter by remember(selectedTimeFilter) { mutableStateOf(selectedTimeFilter) }
    var draftPlaybackChannel by remember(selectedPlaybackChannel) {
        mutableStateOf(if (selectedPlaybackChannel == "sub") "sub" else "main")
    }
    var draftCameraUuid by remember(selectedCameraUuid) { mutableStateOf(selectedCameraUuid) }
    var draftAiType by remember(selectedAiType) { mutableStateOf(selectedAiType) }
    var customPickerVersion by remember { mutableIntStateOf(0) }
    var showCustomDateModal by remember { mutableStateOf(false) }
    var showCustomRangeModal by remember { mutableStateOf(false) }

    // Dropdown đang mở (null = không có)
    var activePicker by remember { mutableStateOf<FilterPicker?>(null) }
    var rootCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val rowRects = remember { mutableStateMapOf<FilterPicker, Rect>() }

    val textColor = colors.textTitle
    val secondaryColor = MaterialTheme.colorScheme.onSurfaceVariant

    val allSystemsLabel = if (isEn) "All systems" else "Tất cả hệ thống"
    val allCamerasLabel = if (isEn) "All cameras" else "Tất cả camera"
    val allEventsLabel = if (isEn) "All events" else "Tất cả sự kiện"

    val dismissPickerOrSheet: () -> Unit = {
        if (activePicker != null) activePicker = null else onDismiss()
    }
    val togglePicker: (FilterPicker) -> Unit = { picker ->
        activePicker = if (activePicker == picker) null else picker
    }

    /** Ghi lại vị trí ô (so với root) để neo dropdown; chỉ ghi khi thay đổi để tránh recompose thừa. */
    fun anchorOf(picker: FilterPicker): Modifier = Modifier.onGloballyPositioned { coords ->
        val root = rootCoords
        if (root != null && root.isAttached && coords.isAttached) {
            val rect = root.localBoundingBoxOf(coords, clipBounds = false)
            if (rowRects[picker] != rect) rowRects[picker] = rect
        }
    }

    MobileFullScreenOverlay(onDismissRequest = dismissPickerOrSheet) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { rootCoords = it }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = dismissPickerOrSheet
                )
        ) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .widthIn(max = spacing.contentMaxWidth)
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { activePicker = null }
                    ),
                shape = RoundedCornerShape(topStart = spacing.radiusLarge, topEnd = spacing.radiusLarge),
                color = colors.surfaceGrouped
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = spacing.space16)
                        .padding(top = spacing.space10, bottom = spacing.space20),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Drag handle
                    Box(
                        modifier = Modifier
                            .width(spacing.sheetHandleWidth)
                            .height(spacing.sheetHandleHeight)
                            .clip(CircleShape)
                            .background(colors.handle)
                    )

                    Spacer(modifier = Modifier.height(spacing.space10))

                    // Header: Icon ở giữa trên cùng, nút đóng (X) ở góc phải trên
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

                        SheetCloseButton(
                            onClick = {
                                activePicker = null
                                onDismiss()
                            },
                            modifier = Modifier.align(Alignment.TopEnd)
                        )
                    }

                    Spacer(modifier = Modifier.height(spacing.space14))

                    Text(
                        text = if (isEn && title == "Bộ lọc") "Filter" else title,
                        color = colors.textStrong,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.space8)
                            .padding(bottom = spacing.space16)
                    )

                    // Unified Filter Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(spacing.radiusRegular))
                            .background(colors.surfaceRaised)
                            .padding(vertical = spacing.space4)
                    ) {
                        // 1. Scope / Chi nhánh
                        if (showScope && aiServices.isNotEmpty()) {
                            FilterCellRow(
                                icon = LucideIcons.MapPin,
                                badgeColor = BrandAccent,
                                label = aiServices.find { it.id == draftServiceId }?.name ?: allSystemsLabel,
                                textColor = textColor,
                                secondaryColor = secondaryColor,
                                isExpanded = activePicker == FilterPicker.SCOPE,
                                onClick = { togglePicker(FilterPicker.SCOPE) },
                                modifier = anchorOf(FilterPicker.SCOPE)
                            )
                        }

                        // 2. Time Range
                        if (showTime) {
                            val timeLabel = remember(draftTimeFilter, customPickerVersion, isEn) {
                                TimeFilter.formatDisplayLabel(draftTimeFilter, isEn)
                            }
                            FilterCellRow(
                                icon = LucideIcons.Clock,
                                badgeColor = WarningOrange,
                                label = timeLabel,
                                textColor = textColor,
                                secondaryColor = secondaryColor,
                                isExpanded = activePicker == FilterPicker.TIME,
                                onClick = { togglePicker(FilterPicker.TIME) },
                                modifier = anchorOf(FilterPicker.TIME)
                            )
                        }

                        // 2b. Stream Channel (Luồng chính / Luồng phụ - dùng ở tab Xem lại)
                        if (showPlaybackChannel) {
                            val channelLabel = if (draftPlaybackChannel == "sub") {
                                if (isEn) "Sub Stream" else "Luồng phụ"
                            } else {
                                if (isEn) "Main Stream" else "Luồng chính"
                            }
                            FilterCellRow(
                                icon = LucideIcons.Video,
                                badgeColor = BrandAccent,
                                label = channelLabel,
                                textColor = textColor,
                                secondaryColor = secondaryColor,
                                isExpanded = activePicker == FilterPicker.STREAM_CHANNEL,
                                onClick = { togglePicker(FilterPicker.STREAM_CHANNEL) },
                                modifier = anchorOf(FilterPicker.STREAM_CHANNEL)
                            )
                        }

                        // 3. Camera
                        if (showCamera) {
                            val cameraLabel = remember(cameras, draftCameraUuid) {
                                cameras.find { cameraKey(it) == draftCameraUuid }?.name
                            } ?: allCamerasLabel
                            FilterCellRow(
                                icon = LucideIcons.Video,
                                badgeColor = SuccessGreen,
                                label = cameraLabel,
                                textColor = textColor,
                                secondaryColor = secondaryColor,
                                isExpanded = activePicker == FilterPicker.CAMERA,
                                onClick = { togglePicker(FilterPicker.CAMERA) },
                                modifier = anchorOf(FilterPicker.CAMERA)
                            )
                        }

                        // 4. AI Event Type
                        if (showEventType) {
                            val moduleLabel = activeModules.find { it.sensorType == draftAiType }?.let {
                                if (isEn) it.labelEn else it.labelVi
                            } ?: allEventsLabel
                            FilterCellRow(
                                icon = LucideIcons.ListFilter,
                                badgeColor = VioletAccent,
                                label = moduleLabel,
                                textColor = textColor,
                                secondaryColor = secondaryColor,
                                isExpanded = activePicker == FilterPicker.EVENT,
                                onClick = { togglePicker(FilterPicker.EVENT) },
                                modifier = anchorOf(FilterPicker.EVENT)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(spacing.space16))

                    // Footer Buttons: Reset & Apply
                    FilterFooterButtons(
                        resetText = if (isEn) "Reset" else "Đặt lại",
                        applyText = if (isEn) "Apply" else "Áp dụng",
                        onReset = {
                            activePicker = null
                            draftServiceId = null
                            draftTimeFilter = TimeFilter.TODAY
                            draftPlaybackChannel = if (defaultPlaybackChannel == "sub") "sub" else "main"
                            draftCameraUuid = null
                            draftAiType = null
                            onReset()
                            onDismiss()
                        },
                        onApply = {
                            activePicker = null
                            if (onApplyPlayback != null) {
                                onApplyPlayback(draftTimeFilter, draftPlaybackChannel)
                            } else {
                                onApply(draftServiceId, draftTimeFilter, draftCameraUuid, draftAiType)
                            }
                            onDismiss()
                        }
                    )
                }
            }

            // Anchored Dropdown Menu right at the clicked filter cell (matching BaseSelect in MobileFilterBottomSheet.vue)
            val currentPicker = activePicker
            val anchorRect = if (currentPicker != null) rowRects[currentPicker] else null
            val rootHeightPx = rootCoords?.size?.height?.toFloat() ?: 0f

            if (currentPicker != null && anchorRect != null && rootHeightPx > 0f) {
                when (currentPicker) {
                    FilterPicker.SCOPE -> {
                        val options = remember(aiServices, allSystemsLabel) {
                            buildList<Pair<Int?, String>> {
                                add(null to allSystemsLabel)
                                aiServices.forEach { add(it.id to it.name) }
                            }
                        }
                        AnchoredFilterDropdownMenu(
                            anchorRect = anchorRect,
                            rootHeightPx = rootHeightPx,
                            options = options,
                            selectedKey = draftServiceId,
                            onSelect = {
                                draftServiceId = it
                                draftCameraUuid = null
                                activePicker = null
                            }
                        )
                    }
                    FilterPicker.TIME -> {
                        val options = remember(isEn) {
                            listOf(
                                TimeFilter.TODAY to (if (isEn) "Today" else "Hôm nay"),
                                TimeFilter.YESTERDAY to (if (isEn) "Yesterday" else "Hôm qua"),
                                TimeFilter.LAST_7_DAYS to (if (isEn) "Last 7 days" else "7 ngày qua"),
                                TimeFilter.CUSTOM_DATE to (if (isEn) "Select date" else "Tùy chọn ngày"),
                                TimeFilter.CUSTOM_RANGE to (if (isEn) "Custom" else "Tùy chỉnh")
                            )
                        }
                        AnchoredFilterDropdownMenu(
                            anchorRect = anchorRect,
                            rootHeightPx = rootHeightPx,
                            options = options,
                            selectedKey = draftTimeFilter,
                            onSelect = { selected ->
                                activePicker = null
                                when (selected) {
                                    TimeFilter.CUSTOM_DATE -> showCustomDateModal = true
                                    TimeFilter.CUSTOM_RANGE -> showCustomRangeModal = true
                                    else -> draftTimeFilter = selected
                                }
                            }
                        )
                    }
                    FilterPicker.STREAM_CHANNEL -> {
                        val options = remember(isEn) {
                            listOf(
                                "main" to (if (isEn) "Main Stream" else "Luồng chính"),
                                "sub" to (if (isEn) "Sub Stream" else "Luồng phụ")
                            )
                        }
                        AnchoredFilterDropdownMenu(
                            anchorRect = anchorRect,
                            rootHeightPx = rootHeightPx,
                            options = options,
                            selectedKey = draftPlaybackChannel,
                            onSelect = { selected ->
                                draftPlaybackChannel = selected
                                activePicker = null
                            }
                        )
                    }
                    FilterPicker.CAMERA -> {
                        val options = remember(cameras, allCamerasLabel) {
                            buildList<Pair<String?, String>> {
                                add(null to allCamerasLabel)
                                cameras.forEach { cam -> add(cameraKey(cam) to cam.name) }
                            }
                        }
                        AnchoredFilterDropdownMenu(
                            anchorRect = anchorRect,
                            rootHeightPx = rootHeightPx,
                            options = options,
                            selectedKey = draftCameraUuid,
                            onSelect = {
                                draftCameraUuid = it
                                activePicker = null
                            }
                        )
                    }
                    FilterPicker.EVENT -> {
                        val options = remember(activeModules, isEn) {
                            buildList<Pair<String?, String>> {
                                add(null to allEventsLabel)
                                activeModules.forEach { mod ->
                                    add(mod.sensorType to (if (isEn) mod.labelEn else mod.labelVi))
                                }
                            }
                        }
                        AnchoredFilterDropdownMenu(
                            anchorRect = anchorRect,
                            rootHeightPx = rootHeightPx,
                            options = options,
                            selectedKey = draftAiType,
                            onSelect = {
                                draftAiType = it
                                activePicker = null
                            }
                        )
                    }
                }
            }

            if (showCustomDateModal) {
                CustomDatePickerDialogModal(
                    initialMillis = TimeFilter.customDateStartMs,
                    isEn = isEn,
                    isDark = isDark,
                    onDismiss = { showCustomDateModal = false },
                    onConfirm = { pickedMillis ->
                        TimeFilter.setCustomDate(pickedMillis)
                        draftTimeFilter = TimeFilter.CUSTOM_DATE
                        customPickerVersion++
                        showCustomDateModal = false
                    }
                )
            }

            if (showCustomRangeModal) {
                CustomRangePickerDialogModal(
                    initialFromMillis = TimeFilter.customRangeFromMs,
                    initialToMillis = TimeFilter.customRangeToMs,
                    isEn = isEn,
                    isDark = isDark,
                    onDismiss = { showCustomRangeModal = false },
                    onConfirm = { fromMillis, toMillis ->
                        TimeFilter.setCustomRange(fromMillis, toMillis)
                        draftTimeFilter = TimeFilter.CUSTOM_RANGE
                        customPickerVersion++
                        showCustomRangeModal = false
                    }
                )
            }
        }
    }
}

/** Khóa định danh camera dùng trong bộ lọc (uuid nếu có, ngược lại id) */
private fun cameraKey(camera: CameraModel): String = camera.extra?.uuid ?: camera.id.toString()

/** Hàng 2 nút [Đặt lại] (nền thẻ) + [Áp dụng] (nền chính) ở cuối bộ lọc */
@Composable
private fun FilterFooterButtons(
    resetText: String,
    applyText: String,
    onReset: () -> Unit,
    onApply: () -> Unit
) {
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.appColors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing.space10)
    ) {
        FilterFooterButton(
            text = resetText,
            containerColor = colors.buttonSecondaryBg,
            contentColor = colors.textStrong,
            fontWeight = FontWeight.SemiBold,
            onClick = onReset
        )
        FilterFooterButton(
            text = applyText,
            containerColor = colors.accent,
            contentColor = colors.onAccent,
            fontWeight = FontWeight.Bold,
            onClick = onApply
        )
    }
}

@Composable
private fun RowScope.FilterFooterButton(
    text: String,
    containerColor: Color,
    contentColor: Color,
    fontWeight: FontWeight,
    onClick: () -> Unit
) {
    val spacing = MaterialTheme.spacing
    Box(
        modifier = Modifier
            .weight(1f)
            .heightIn(min = spacing.iconButtonLarge)
            .clip(RoundedCornerShape(spacing.radiusCompact))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.space8),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = fontWeight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FilterCellRow(
    icon: ImageVector,
    badgeColor: Color,
    label: String,
    textColor: Color,
    secondaryColor: Color,
    isExpanded: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.appColors
    val activeRowBg = if (isExpanded) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f) else Color.Transparent

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(spacing.listItemHeight)
            .background(activeRowBg)
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.space14),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(
            icon = icon,
            backgroundColor = badgeColor
        )
        Spacer(modifier = Modifier.width(spacing.space12))
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(spacing.space8))
        Icon(
            imageVector = LucideIcons.ChevronDown,
            contentDescription = null,
            tint = if (isExpanded) colors.accent else secondaryColor,
            modifier = Modifier
                .size(spacing.iconSmall)
                .rotate(if (isExpanded) 180f else 0f)
        )
    }
}

@Composable
private fun <K> AnchoredFilterDropdownMenu(
    anchorRect: Rect,
    rootHeightPx: Float,
    options: List<Pair<K, String>>,
    selectedKey: K,
    onSelect: (K) -> Unit
) {
    val density = LocalDensity.current
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.appColors
    val accent = colors.accent
    val itemTextColor = colors.textTitle
    val selectedItemBg = accent.copy(alpha = if (colors.isDark) 0.16f else 0.09f)
    val menuShape = RoundedCornerShape(spacing.radiusCompact)
    val itemShape = RoundedCornerShape(spacing.radiusSmall)

    val itemHeightDp = spacing.buttonHeightSmall
    val verticalPaddingDp = spacing.space8
    val maxDropdownHeightDp = itemHeightDp * DROPDOWN_MAX_VISIBLE_ITEMS
    val gapPx = with(density) { spacing.space4.toPx() }
    val screenEdgeMarginPx = with(density) { spacing.space14.toPx() }

    val naturalHeightDp = (itemHeightDp * options.size + verticalPaddingDp).coerceAtMost(maxDropdownHeightDp)
    val naturalHeightPx = with(density) { naturalHeightDp.toPx() }

    val spaceBelowPx = (rootHeightPx - anchorRect.bottom - gapPx - screenEdgeMarginPx).coerceAtLeast(0f)
    val spaceAbovePx = (anchorRect.top - gapPx - screenEdgeMarginPx).coerceAtLeast(0f)

    val openDownward = spaceBelowPx >= naturalHeightPx || spaceBelowPx >= spaceAbovePx

    val maxAllowedHeightPx = if (openDownward) {
        spaceBelowPx.coerceAtMost(naturalHeightPx)
    } else {
        spaceAbovePx.coerceAtMost(naturalHeightPx)
    }

    val topOffsetPx = if (openDownward) {
        anchorRect.bottom + gapPx
    } else {
        (anchorRect.top - gapPx - maxAllowedHeightPx).coerceAtLeast(screenEdgeMarginPx)
    }

    val widthDp = with(density) { anchorRect.width.toDp() }
    val maxHeightDp = with(density) { maxAllowedHeightPx.toDp() }.coerceAtLeast(itemHeightDp + verticalPaddingDp)

    Surface(
        shape = menuShape,
        color = colors.surfaceMenu,
        shadowElevation = spacing.space12,
        border = BorderStroke(spacing.borderMedium, colors.border),
        modifier = Modifier
            .offset {
                IntOffset(
                    x = anchorRect.left.roundToInt(),
                    y = topOffsetPx.roundToInt()
                )
            }
            .width(widthDp)
            .heightIn(max = maxHeightDp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.space4)
        ) {
            items(options) { (key, label) ->
                val isSelected = key == selectedKey
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeightDp)
                        .clip(itemShape)
                        .background(if (isSelected) selectedItemBg else Color.Transparent)
                        .clickable { onSelect(key) }
                        .padding(horizontal = spacing.space12),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) accent else itemTextColor,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(spacing.space8))
                        Icon(
                            imageVector = LucideIcons.Check,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(spacing.iconSmall)
                        )
                    }
                }
            }
        }
    }
}

// =====================================================================================
// Chọn ngày / khoảng thời gian tùy chỉnh
// =====================================================================================

/**
 * Mở DatePicker hệ thống.
 * @param preserveTimeOfDay true = giữ giờ/phút của [currentMs]; false = giờ hiện tại (hành vi cũ của chọn 1 ngày).
 */
private fun showSystemDatePicker(
    context: Context,
    currentMs: Long,
    preserveTimeOfDay: Boolean,
    onResult: (Long) -> Unit
) {
    val cal = Calendar.getInstance().apply { timeInMillis = currentMs }
    android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val c = Calendar.getInstance().apply {
                if (preserveTimeOfDay) timeInMillis = currentMs
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
            }
            onResult(c.timeInMillis)
        },
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH)
    ).show()
}

/** Mở TimePicker hệ thống (24h), giữ nguyên ngày của [currentMs]. */
private fun showSystemTimePicker(context: Context, currentMs: Long, onResult: (Long) -> Unit) {
    val cal = Calendar.getInstance().apply { timeInMillis = currentMs }
    android.app.TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val c = Calendar.getInstance().apply {
                timeInMillis = currentMs
                set(Calendar.HOUR_OF_DAY, hourOfDay)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
            }
            onResult(c.timeInMillis)
        },
        cal.get(Calendar.HOUR_OF_DAY),
        cal.get(Calendar.MINUTE),
        true
    ).show()
}

@Composable
fun CustomDatePickerDialogModal(
    initialMillis: Long,
    isEn: Boolean,
    @Suppress("UNUSED_PARAMETER") isDark: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val context = LocalContext.current
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.appColors
    val typography = MaterialTheme.typography
    var selectedMillis by remember(initialMillis) { mutableLongStateOf(initialMillis) }
    val subColor = MaterialTheme.colorScheme.onSurfaceVariant

    MobileNativeBottomSheet(
        title = if (isEn) "Select Date" else "Tùy chọn ngày",
        icon = LucideIcons.Clock,
        iconColor = WarningOrange,
        onDismiss = onDismiss
    ) {
        Text(
            text = if (isEn) "Choose a date to filter events (00:00 - 23:59)" else "Chọn ngày cần tra cứu dữ liệu (00:00 - 23:59)",
            color = subColor,
            style = typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(spacing.space14))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = spacing.inputHeight)
                .clip(RoundedCornerShape(spacing.radiusCompact))
                .background(colors.surfaceRaised)
                .clickable {
                    showSystemDatePicker(context, selectedMillis, preserveTimeOfDay = false) { selectedMillis = it }
                }
                .padding(horizontal = spacing.space14),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.space10)
        ) {
            Icon(
                imageVector = LucideIcons.Clock,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(spacing.iconSmallMedium)
            )
            Text(
                text = formatMillis(selectedMillis, DayMonthYearFormatter),
                color = MaterialTheme.colorScheme.onBackground,
                style = typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (isEn) "Change" else "Chọn lịch",
                color = colors.accent,
                style = typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(spacing.space20))

        SheetActionButtons(
            cancelText = if (isEn) "Cancel" else "Hủy",
            confirmText = if (isEn) "Confirm" else "Xác nhận",
            onCancel = onDismiss,
            onConfirm = { onConfirm(selectedMillis) },
            confirmColor = BrandPrimary
        )
    }
}

@Composable
fun CustomRangePickerDialogModal(
    initialFromMillis: Long,
    initialToMillis: Long,
    isEn: Boolean,
    @Suppress("UNUSED_PARAMETER") isDark: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Long, Long) -> Unit
) {
    val context = LocalContext.current
    val spacing = MaterialTheme.spacing
    var fromMillis by remember(initialFromMillis) { mutableLongStateOf(initialFromMillis) }
    var toMillis by remember(initialToMillis) { mutableLongStateOf(initialToMillis) }

    MobileNativeBottomSheet(
        title = if (isEn) "Custom Time Range" else "Tùy chỉnh thời gian",
        icon = LucideIcons.Clock,
        iconColor = WarningOrange,
        onDismiss = onDismiss
    ) {
        RangeDateTimeRow(
            label = if (isEn) "From" else "Từ thời gian",
            millis = fromMillis,
            onPickDate = { showSystemDatePicker(context, fromMillis, preserveTimeOfDay = true) { fromMillis = it } },
            onPickTime = { showSystemTimePicker(context, fromMillis) { fromMillis = it } }
        )

        Spacer(modifier = Modifier.height(spacing.space12))

        RangeDateTimeRow(
            label = if (isEn) "To" else "Đến thời gian",
            millis = toMillis,
            onPickDate = { showSystemDatePicker(context, toMillis, preserveTimeOfDay = true) { toMillis = it } },
            onPickTime = { showSystemTimePicker(context, toMillis) { toMillis = it } }
        )

        Spacer(modifier = Modifier.height(spacing.space20))

        SheetActionButtons(
            cancelText = if (isEn) "Cancel" else "Hủy",
            confirmText = if (isEn) "Confirm" else "Xác nhận",
            onCancel = onDismiss,
            onConfirm = { onConfirm(fromMillis, toMillis) },
            confirmColor = BrandPrimary
        )
    }
}

/** Nhãn + 2 ô chọn [ngày | giờ] (chia theo weight để vừa cả màn hình 320dp) */
@Composable
private fun RangeDateTimeRow(
    label: String,
    millis: Long,
    onPickDate: () -> Unit,
    onPickTime: () -> Unit
) {
    val spacing = MaterialTheme.spacing
    Text(
        text = label,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelMedium
    )
    Spacer(modifier = Modifier.height(spacing.space6))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing.space8)
    ) {
        PickerField(
            text = formatMillis(millis, DayMonthYearFormatter),
            onClick = onPickDate,
            modifier = Modifier.weight(1.4f)
        )
        PickerField(
            text = formatMillis(millis, HourMinuteFormatter),
            onClick = onPickTime,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PickerField(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = MaterialTheme.spacing
    Box(
        modifier = modifier
            .heightIn(min = spacing.iconButtonLarge)
            .clip(RoundedCornerShape(spacing.radiusSmallMedium))
            .background(MaterialTheme.appColors.surfaceRaised)
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.space12),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
