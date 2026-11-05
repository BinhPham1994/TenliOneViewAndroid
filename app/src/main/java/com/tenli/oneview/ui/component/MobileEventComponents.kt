package com.tenli.oneview.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.model.network.AIServiceModel
import com.tenli.oneview.model.network.CameraModel
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.ui.features.home.TimeFilter
import com.tenli.oneview.ui.features.home.findAllBoxes
import com.tenli.oneview.ui.theme.BackgroundDark
import com.tenli.oneview.ui.theme.BackgroundLight
import com.tenli.oneview.ui.theme.BrandAccent
import com.tenli.oneview.ui.theme.BrandPrimary
import com.tenli.oneview.ui.theme.LucideIcons
import com.tenli.oneview.ui.theme.SurfaceDark
import com.tenli.oneview.ui.theme.SurfaceLight
import com.tenli.oneview.ui.theme.SurfaceSubtleDark
import com.tenli.oneview.ui.theme.SurfaceSubtleLight
import com.tenli.oneview.ui.theme.TextPrimary
import com.tenli.oneview.ui.theme.TextPrimaryDark
import com.tenli.oneview.ui.theme.TextSecondary
import com.tenli.oneview.ui.theme.TextSecondaryDark
import com.tenli.oneview.ui.theme.TextTitleLight
import com.tenli.oneview.ui.theme.ThemeManager
import com.tenli.oneview.ui.utils.AiModuleOption
import com.tenli.oneview.ui.utils.AiTypeHelper
import com.tenli.oneview.ui.utils.LocaleManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val UUID_REGEX = Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")

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
        bgColor = Color(0xFFEF4444),
        shadowColor = Color(0xFFEF4444).copy(alpha = 0.45f)
    ),
    WARNING(
        bgColor = Color(0xFFC94A08),
        shadowColor = Color(0xFFC94A08).copy(alpha = 0.50f)
    ),
    SUCCESS(
        bgColor = Color(0xFF15803D),
        shadowColor = Color(0xFF15803D).copy(alpha = 0.50f)
    ),
    INFO(
        bgColor = Color(0xFF0980EE),
        shadowColor = Color(0xFF0980EE).copy(alpha = 0.45f)
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
    var lastRenderedMessage by remember { mutableStateOf<MobileTopPopupMessage?>(null) }

    if (activeMessage != null) {
        lastRenderedMessage = activeMessage
    }

    LaunchedEffect(activeMessage?.id) {
        val current = activeMessage ?: return@LaunchedEffect
        delay(current.durationMs)
        if (MobileTopPopupManager.currentMessage.value?.id == current.id) {
            MobileTopPopupManager.dismiss()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 8.dp, start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = activeMessage != null,
            enter = slideInVertically(
                initialOffsetY = { -it / 2 },
                animationSpec = tween(
                    durationMillis = 200,
                    easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
                )
            ) + scaleIn(
                initialScale = 0.96f,
                animationSpec = tween(
                    durationMillis = 200,
                    easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
                )
            ) + fadeIn(animationSpec = tween(durationMillis = 160)),
            exit = slideOutVertically(
                targetOffsetY = { -it / 2 },
                animationSpec = tween(durationMillis = 170)
            ) + scaleOut(
                targetScale = 0.96f,
                animationSpec = tween(durationMillis = 170)
            ) + fadeOut(animationSpec = tween(durationMillis = 150))
        ) {
            val msg = activeMessage ?: lastRenderedMessage
            if (msg != null) {
                val shape = RoundedCornerShape(10.dp)
                Row(
                    modifier = Modifier
                        .shadow(
                            elevation = 10.dp,
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
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MobileTopPopupFilledIcon(
                        type = msg.type,
                        cutoutColor = msg.type.bgColor
                    )

                    Text(
                        text = msg.text,
                        color = Color.White,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.2.sp,
                        lineHeight = 19.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.20f),
                                offset = Offset(0f, 2f),
                                blurRadius = 4f
                            )
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun MobileTopPopupFilledIcon(
    type: MobileTopPopupType,
    cutoutColor: Color
) {
    Canvas(modifier = Modifier.size(16.dp)) {
        val w = size.width
        val h = size.height
        val radius = minOf(w, h) / 2f
        val strokeW = w * 0.12f

        // Solid white circle matching `.ant-message-custom-content .anticon svg { fill: #ffffff }`
        drawCircle(
            color = Color.White,
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

private class MobileOverlayEntry(
    val key: Any,
    var content: @Composable () -> Unit
) {
    var isScrimVisible by mutableStateOf(false)
    var isSheetVisible by mutableStateOf(false)
    var isClosing by mutableStateOf(false)
}

class MobileOverlayHostState {
    private val entries = mutableStateListOf<MobileOverlayEntry>()

    fun show(key: Any, content: @Composable () -> Unit) {
        val idx = entries.indexOfFirst { it.key == key }
        if (idx >= 0) {
            entries[idx].content = content
            entries[idx].isClosing = false
            entries[idx].isScrimVisible = true
            entries[idx].isSheetVisible = true
        } else {
            entries.add(MobileOverlayEntry(key, content))
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
                        delay(130)
                        if (!entry.isClosing) {
                            entry.isSheetVisible = true
                        }
                    } else {
                        // Khi đóng: trượt Bottom Sheet xuống trước một nhịp rồi làm mờ dần nền tối
                        entry.isSheetVisible = false
                        delay(80)
                        entry.isScrimVisible = false
                        delay(280)
                        entries.remove(entry)
                    }
                }

                // Lớp nền mờ toàn màn hình fade in trước / fade out sau
                AnimatedVisibility(
                    visible = entry.isScrimVisible,
                    enter = fadeIn(animationSpec = tween(durationMillis = 200)),
                    exit = fadeOut(animationSpec = tween(durationMillis = 260))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f))
                    )
                }

                // Nội dung Bottom Sheet trượt từ dưới lên sau khi nền đã tối
                AnimatedVisibility(
                    visible = entry.isSheetVisible,
                    enter = slideInVertically(
                        initialOffsetY = { fullHeight -> fullHeight },
                        animationSpec = tween(
                            durationMillis = 360,
                            easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
                        )
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { fullHeight -> fullHeight },
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
                        )
                    )
                ) {
                    entry.content()
                }
            }
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
            host.show(key) {
                currentContentState.value.invoke()
            }
            onDispose {
                host.hide(key)
            }
        }
    } else {
        var isScrimVisible by remember { mutableStateOf(false) }
        var isSheetVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            isScrimVisible = true
            delay(130)
            isSheetVisible = true
        }
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(
                visible = isScrimVisible,
                enter = fadeIn(animationSpec = tween(durationMillis = 200)),
                exit = fadeOut(animationSpec = tween(durationMillis = 260))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f))
                )
            }
            AnimatedVisibility(
                visible = isSheetVisible,
                enter = slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(
                        durationMillis = 360,
                        easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
                    )
                ),
                exit = slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
                    )
                )
            ) {
                content()
            }
        }
    }
}

fun isUuidString(value: String?): Boolean {
    if (value.isNullOrBlank()) return false
    return UUID_REGEX.matches(value.trim())
}

@Composable
fun rememberIsDarkTheme(): Boolean {
    val themeMode by ThemeManager.themeModeFlow.collectAsStateWithLifecycle()
    val isSystemDark = isSystemInDarkTheme()
    return when (themeMode) {
        0 -> false
        1 -> true
        else -> isSystemDark
    }
}

@Composable
fun rememberIsEnglish(): Boolean {
    val context = LocalContext.current
    val langCode by LocaleManager.languageFlow.collectAsStateWithLifecycle(
        initialValue = LocaleManager.getLocale(context)
    )
    return langCode.startsWith("en")
}

/**
 * Top bar matching `.mobile-top-bar` in `mobile.css`
 */
@Composable
fun MobileTopBar(
    title: String,
    leftAction: @Composable (() -> Unit)? = null,
    rightAction: @Composable (() -> Unit)? = null
) {
    val isDark = rememberIsDarkTheme()
    val bgColor = if (isDark) BackgroundDark else BackgroundLight
    val titleColor = if (isDark) Color.White else TextTitleLight

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(bgColor)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        if (leftAction != null) {
            Box(modifier = Modifier.align(Alignment.CenterStart)) {
                leftAction()
            }
        }

        Text(
            text = title,
            color = titleColor,
            fontSize = 18.5.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 44.dp)
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
    val isDark = rememberIsDarkTheme()
    val iconColor = if (isDark) Color(0xFFE2E8F0) else TextPrimary

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        if (hasActiveDot) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 5.dp, end = 5.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(BrandAccent)
            )
        }
    }
}

@Composable
fun MobileViewToggleButton(
    isListView: Boolean,
    onToggle: () -> Unit
) {
    val isDark = rememberIsDarkTheme()
    val iconColor = if (isDark) Color(0xFFCBD5E1) else TextPrimary

    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onToggle),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isListView) LucideIcons.LayoutGrid else LucideIcons.List,
            contentDescription = if (isListView) "Grid View" else "List View",
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Cloned from `MobileEventCard.vue` supporting both List View (`isListView = true`) and Grid/Card View (`isListView = false`).
 */
@OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)
@Composable
fun MobileEventCard(
    event: EventData,
    cameraName: String,
    isListView: Boolean = true,
    isSelected: Boolean = false,
    enableSharedElement: Boolean = false,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val isEn = rememberIsEnglish()
    val isDark = rememberIsDarkTheme()
    val domain = UserSession.domain
    val eventKey = "${event.serviceId}_${event.id}_${event.time}"

    val resolvedCameraName: String = remember(eventKey, cameraName) {
        val fromProp = cameraName.takeIf { it.isNotBlank() && !isUuidString(it) }
        val fromData = event.data?.cameraName?.takeIf { it.isNotBlank() && !isUuidString(it) }
        val fromRoot = event.cameraName?.takeIf { it.isNotBlank() && !isUuidString(it) }
        fromProp ?: fromData ?: fromRoot ?: ""
    }

    val snapshotUrl = remember(eventKey, domain) {
        val faceUrl = AiTypeHelper.getFaceCropUrl(event, domain)
        val snapUrl = AiTypeHelper.getSnapshotUrl(event, domain)
        val fullUrl = AiTypeHelper.getFullImageUrl(event, domain)
        (faceUrl ?: snapUrl.ifEmpty { fullUrl }).ifEmpty { null }
    }

    val fullImageUrl = remember(eventKey, domain) {
        val fullUrl = AiTypeHelper.getFullImageUrl(event, domain)
        val snapUrl = AiTypeHelper.getSnapshotUrl(event, domain)
        fullUrl.ifEmpty { snapUrl }.ifEmpty { null }
    }

    val faceCropUrl = remember(eventKey, domain, fullImageUrl) {
        val crop = AiTypeHelper.getFaceCropUrl(event, domain)
        if (!crop.isNullOrBlank() && crop != fullImageUrl) crop else null
    }

    val sentenceParts = remember(eventKey, resolvedCameraName, isEn) {
        AiTypeHelper.getEventSentenceParts(event, resolvedCameraName, isEn)
    }

    val badgeLabel = remember(eventKey, isEn) {
        AiTypeHelper.getEventBadgeLabel(event, isEn)
    }

    val badgeColor = remember(eventKey) {
        AiTypeHelper.getAiColor(event)
    }

    val relativeTime = remember(event.time, isEn) {
        AiTypeHelper.formatRelativeTime(event.time, isEn)
    }

    val cardBgColor = if (isSelected) {
        if (isDark) Color(0xFF1E3A5F) else Color(0xFFDCEBFF)
    } else {
        if (isDark) SurfaceDark else SurfaceLight
    }

    val sharedTransitionScope = com.tenli.oneview.ui.navigation.LocalSharedTransitionScope.current
    val animatedVisibilityScope = com.tenli.oneview.ui.navigation.LocalAnimatedVisibilityScope.current

    if (isListView) {
        // Horizontal List Card matching `.mobile-event-card-horizontal`
        val prefixSuffixColor = if (isDark) Color(0xFFCBD5E1) else TextPrimary
        val strongCameraColor = if (isDark) Color.White else TextTitleLight
        val timeAgoColor = if (isDark) TextSecondaryDark else TextSecondary

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(cardBgColor)
                .clickable(onClick = onClick)
                .padding(start = 8.dp, top = 8.dp, bottom = 8.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var thumbModifier = Modifier
                .width(156.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(9.dp))
                .background(if (isDark) SurfaceSubtleDark else SurfaceSubtleLight)

            if (enableSharedElement && sharedTransitionScope != null && animatedVisibilityScope != null) {
                with(sharedTransitionScope) {
                    thumbModifier = thumbModifier.sharedElement(
                        sharedContentState = rememberSharedContentState(key = "event_image_${event.id}"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                }
            }

            VideoFrameImage(
                url = snapshotUrl,
                modifier = thumbModifier,
                contentScale = ContentScale.Crop,
                targetWidthPx = 420,
                targetHeightPx = 236
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 88.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.5.dp)) {
                    // Line 1: Prefix (e.g., "Phát hiện khuôn mặt", "Phát hiện vi phạm")
                    if (sentenceParts.prefix.isNotEmpty()) {
                        Text(
                            text = sentenceParts.prefix,
                            color = prefixSuffixColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Line 2: Highlighted pill subject
                    if (sentenceParts.subject.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(sentenceParts.color)
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = sentenceParts.subject,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
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

                    Text(
                        text = suffixAnnotated,
                        fontSize = 13.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Bottom row: Relative time ("5 phút trước")
                Text(
                    text = relativeTime,
                    color = timeAgoColor,
                    fontSize = 12.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    } else {
        // Grid / Large 16:9 Card View matching `.mobile-event-card`
        val (formattedTime, formattedDate) = remember(event.time, isEn) {
            formatGridEventTimeAndDate(event.time, isEn)
        }
        val boxes = remember(eventKey) { findAllBoxes(event) }

        var cardModifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(12.dp))
            .background(cardBgColor)
            .clickable(onClick = onClick)

        if (enableSharedElement && sharedTransitionScope != null && animatedVisibilityScope != null) {
            with(sharedTransitionScope) {
                cardModifier = cardModifier.sharedElement(
                    sharedContentState = rememberSharedContentState(key = "event_image_${event.id}"),
                    animatedVisibilityScope = animatedVisibilityScope
                )
            }
        }

        Box(modifier = cardModifier) {
            VideoFrameImage(
                url = fullImageUrl,
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        drawContent()
                        boxes.forEach { boxArr ->
                            if (boxArr.size >= 4) {
                                val minX = boxArr[0]
                                val minY = boxArr[1]
                                val maxX = boxArr[2]
                                val maxY = boxArr[3]
                                val w = (maxX - minX) * size.width
                                val h = (maxY - minY) * size.height
                                val x = minX * size.width
                                val y = minY * size.height
                                drawRect(
                                    color = Color(0xFFEF4444),
                                    topLeft = Offset(x, y),
                                    size = Size(w, h),
                                    style = Stroke(width = 3.5f)
                                )
                            }
                        }
                    },
                contentScale = ContentScale.Crop,
                targetWidthPx = 800,
                targetHeightPx = 450
            )

            // Face crop overlay on TopEnd (72x90dp)
            if (faceCropUrl != null) {
                val faceCropRequest = remember(faceCropUrl) {
                    coil.request.ImageRequest.Builder(context)
                        .data(faceCropUrl)
                        .size(160, 200)
                        .precision(coil.size.Precision.INEXACT)
                        .allowHardware(true)
                        .crossfade(false)
                        .memoryCacheKey("${faceCropUrl}_160x200")
                        .diskCacheKey(faceCropUrl)
                        .build()
                }
                coil.compose.AsyncImage(
                    model = faceCropRequest,
                    contentDescription = "Face Crop",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(5.dp)
                        .width(72.dp)
                        .height(90.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.45f))
                        .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                )
            }

            // Bottom gradient overlay
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.5f),
                                Color.Black.copy(alpha = 0.9f)
                            )
                        )
                    )
                    .padding(top = 26.dp, bottom = 8.dp, start = 10.dp, end = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(badgeColor)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badgeLabel,
                                color = Color.White,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (resolvedCameraName.isNotEmpty()) {
                            Text(
                                text = resolvedCameraName,
                                color = Color.White,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = formattedTime,
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = formattedDate,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

private fun formatGridEventTimeAndDate(timeSeconds: Double, isEn: Boolean): Pair<String, String> {
    if (timeSeconds <= 0) return Pair("", "")
    val timeMs = if (timeSeconds < 1e11) (timeSeconds * 1000).toLong() else timeSeconds.toLong()
    val date = Date(timeMs)
    val timeFmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val dateFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val calEvent = Calendar.getInstance().apply { timeInMillis = timeMs }
    val calNow = Calendar.getInstance()
    val isToday = calEvent.get(Calendar.YEAR) == calNow.get(Calendar.YEAR) &&
        calEvent.get(Calendar.DAY_OF_YEAR) == calNow.get(Calendar.DAY_OF_YEAR)

    val dateLabel = if (isToday) {
        if (isEn) "Today" else "Hôm nay"
    } else {
        dateFmt.format(date)
    }

    return Pair(timeFmt.format(date), dateLabel)
}

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
    showCamera: Boolean = false,
    cameras: List<CameraModel> = emptyList(),
    selectedCameraUuid: String? = null,
    showEventType: Boolean = false,
    activeModules: List<AiModuleOption> = AiTypeHelper.activeModules,
    selectedAiType: String? = null,
    onDismiss: () -> Unit,
    onApply: (serviceId: Int?, timeFilter: TimeFilter, cameraUuid: String?, aiType: String?) -> Unit,
    onReset: () -> Unit
) {
    val context = LocalContext.current
    val isEn = rememberIsEnglish()
    val isDark = rememberIsDarkTheme()

    var draftServiceId by remember(selectedServiceId) { mutableStateOf(selectedServiceId) }
    var draftTimeFilter by remember(selectedTimeFilter) { mutableStateOf(selectedTimeFilter) }
    var draftCameraUuid by remember(selectedCameraUuid) { mutableStateOf(selectedCameraUuid) }
    var draftAiType by remember(selectedAiType) { mutableStateOf(selectedAiType) }
    var customPickerVersion by remember { mutableStateOf(0) }
    var showCustomDateModal by remember { mutableStateOf(false) }
    var showCustomRangeModal by remember { mutableStateOf(false) }

    // Active anchored dropdown state: null, "scope", "time", "camera", "event"
    var activePicker by remember { mutableStateOf<String?>(null) }
    var rootCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val rowRects = remember { mutableStateMapOf<String, Rect>() }

    val sheetBg = if (isDark) BackgroundDark else BackgroundLight
    val cardBg = if (isDark) SurfaceDark else SurfaceLight
    val textColor = if (isDark) TextPrimaryDark else TextTitleLight
    val secondaryColor = if (isDark) TextSecondaryDark else TextSecondary

    MobileFullScreenOverlay(
        onDismissRequest = {
            if (activePicker != null) {
                activePicker = null
            } else {
                onDismiss()
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { rootCoords = it }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        if (activePicker != null) {
                            activePicker = null
                        } else {
                            onDismiss()
                        }
                    }
                )
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 10.dp, vertical = 10.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(sheetBg)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            if (activePicker != null) {
                                activePicker = null
                            }
                        }
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (isDark) Color.White.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.15f))
                )

                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, bottom = 18.dp)
                ) {
                    Text(
                        text = if (isEn && title == "Bộ lọc") "Filter" else title,
                        color = textColor,
                        fontSize = 17.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.08f))
                            .clickable {
                                activePicker = null
                                onDismiss()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = LucideIcons.X,
                            contentDescription = "Close",
                            tint = secondaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Unified Filter Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardBg)
                        .padding(vertical = 4.dp)
                ) {
                    // 1. Scope / Chi nhánh
                    if (showScope && aiServices.isNotEmpty()) {
                        val scopeLabel = aiServices.find { it.id == draftServiceId }?.name
                            ?: if (isEn) "All systems" else "Tất cả hệ thống"
                        FilterCellRow(
                            icon = LucideIcons.MapPin,
                            badgeColor = Color(0xFF0980EE),
                            label = scopeLabel,
                            textColor = textColor,
                            secondaryColor = secondaryColor,
                            isExpanded = activePicker == "scope",
                            isDark = isDark,
                            onClick = {
                                activePicker = if (activePicker == "scope") null else "scope"
                            },
                            modifier = Modifier.onGloballyPositioned { coords ->
                                val root = rootCoords
                                if (root != null && root.isAttached && coords.isAttached) {
                                    rowRects["scope"] = root.localBoundingBoxOf(coords, clipBounds = false)
                                }
                            }
                        )
                    }

                    // 2. Time Range
                    if (showTime) {
                        val timeLabel = remember(draftTimeFilter, customPickerVersion, isEn) {
                            TimeFilter.formatDisplayLabel(draftTimeFilter, isEn)
                        }
                        FilterCellRow(
                            icon = LucideIcons.Clock,
                            badgeColor = Color(0xFFF59E0B),
                            label = timeLabel,
                            textColor = textColor,
                            secondaryColor = secondaryColor,
                            isExpanded = activePicker == "time",
                            isDark = isDark,
                            onClick = {
                                activePicker = if (activePicker == "time") null else "time"
                            },
                            modifier = Modifier.onGloballyPositioned { coords ->
                                val root = rootCoords
                                if (root != null && root.isAttached && coords.isAttached) {
                                    rowRects["time"] = root.localBoundingBoxOf(coords, clipBounds = false)
                                }
                            }
                        )
                    }

                    // 3. Camera
                    if (showCamera) {
                        val cameraLabel = cameras.find {
                            (it.extra?.uuid ?: it.id.toString()) == draftCameraUuid
                        }?.name ?: if (isEn) "All cameras" else "Tất cả camera"
                        FilterCellRow(
                            icon = LucideIcons.Video,
                            badgeColor = Color(0xFF10B981),
                            label = cameraLabel,
                            textColor = textColor,
                            secondaryColor = secondaryColor,
                            isExpanded = activePicker == "camera",
                            isDark = isDark,
                            onClick = {
                                activePicker = if (activePicker == "camera") null else "camera"
                            },
                            modifier = Modifier.onGloballyPositioned { coords ->
                                val root = rootCoords
                                if (root != null && root.isAttached && coords.isAttached) {
                                    rowRects["camera"] = root.localBoundingBoxOf(coords, clipBounds = false)
                                }
                            }
                        )
                    }

                    // 4. AI Event Type
                    if (showEventType) {
                        val moduleLabel = activeModules.find { it.sensorType == draftAiType }?.let {
                            if (isEn) it.labelEn else it.labelVi
                        } ?: if (isEn) "All events" else "Tất cả sự kiện"
                        FilterCellRow(
                            icon = LucideIcons.ListFilter,
                            badgeColor = Color(0xFF8B5CF6),
                            label = moduleLabel,
                            textColor = textColor,
                            secondaryColor = secondaryColor,
                            isExpanded = activePicker == "event",
                            isDark = isDark,
                            onClick = {
                                activePicker = if (activePicker == "event") null else "event"
                            },
                            modifier = Modifier.onGloballyPositioned { coords ->
                                val root = rootCoords
                                if (root != null && root.isAttached && coords.isAttached) {
                                    rowRects["event"] = root.localBoundingBoxOf(coords, clipBounds = false)
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Footer Buttons: Reset & Apply
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            activePicker = null
                            draftServiceId = null
                            draftTimeFilter = TimeFilter.TODAY
                            draftCameraUuid = null
                            draftAiType = null
                            onReset()
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = cardBg,
                            contentColor = if (isDark) Color.White.copy(alpha = 0.8f) else Color(0xFF475569)
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp)
                    ) {
                        Text(
                            text = if (isEn) "Reset" else "Đặt lại",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = {
                            activePicker = null
                            onApply(draftServiceId, draftTimeFilter, draftCameraUuid, draftAiType)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandPrimary,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp)
                    ) {
                        Text(
                            text = if (isEn) "Apply" else "Áp dụng",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Anchored Dropdown Menu right at the clicked filter cell (matching BaseSelect in MobileFilterBottomSheet.vue)
            val currentPicker = activePicker
            val anchorRect = if (currentPicker != null) rowRects[currentPicker] else null
            val rootHeightPx = rootCoords?.size?.height?.toFloat() ?: 0f

            if (currentPicker != null && anchorRect != null && rootHeightPx > 0f) {
                when (currentPicker) {
                    "scope" -> {
                        val options = buildList<Pair<Int?, String>> {
                            add(null to (if (isEn) "All systems" else "Tất cả hệ thống"))
                            aiServices.forEach { add(it.id to it.name) }
                        }
                        AnchoredFilterDropdownMenu(
                            anchorRect = anchorRect,
                            rootHeightPx = rootHeightPx,
                            options = options,
                            selectedKey = draftServiceId,
                            isDark = isDark,
                            onSelect = {
                                draftServiceId = it
                                draftCameraUuid = null
                                activePicker = null
                            }
                        )
                    }
                    "time" -> {
                        val options = listOf(
                            TimeFilter.TODAY to (if (isEn) "Today" else "Hôm nay"),
                            TimeFilter.YESTERDAY to (if (isEn) "Yesterday" else "Hôm qua"),
                            TimeFilter.LAST_7_DAYS to (if (isEn) "Last 7 days" else "7 ngày qua"),
                            TimeFilter.CUSTOM_DATE to (if (isEn) "Select date" else "Tùy chọn ngày"),
                            TimeFilter.CUSTOM_RANGE to (if (isEn) "Custom" else "Tùy chỉnh")
                        )
                        AnchoredFilterDropdownMenu(
                            anchorRect = anchorRect,
                            rootHeightPx = rootHeightPx,
                            options = options,
                            selectedKey = draftTimeFilter,
                            isDark = isDark,
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
                    "camera" -> {
                        val options = buildList<Pair<String?, String>> {
                            add(null to (if (isEn) "All cameras" else "Tất cả camera"))
                            cameras.forEach { cam ->
                                val key = cam.extra?.uuid ?: cam.id.toString()
                                add(key to cam.name)
                            }
                        }
                        AnchoredFilterDropdownMenu(
                            anchorRect = anchorRect,
                            rootHeightPx = rootHeightPx,
                            options = options,
                            selectedKey = draftCameraUuid,
                            isDark = isDark,
                            onSelect = {
                                draftCameraUuid = it
                                activePicker = null
                            }
                        )
                    }
                    "event" -> {
                        val options = buildList<Pair<String?, String>> {
                            add(null to (if (isEn) "All events" else "Tất cả sự kiện"))
                            activeModules.forEach { mod ->
                                add(mod.sensorType to (if (isEn) mod.labelEn else mod.labelVi))
                            }
                        }
                        AnchoredFilterDropdownMenu(
                            anchorRect = anchorRect,
                            rootHeightPx = rootHeightPx,
                            options = options,
                            selectedKey = draftAiType,
                            isDark = isDark,
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

@Composable
private fun FilterCellRow(
    icon: ImageVector,
    badgeColor: Color,
    label: String,
    textColor: Color,
    secondaryColor: Color,
    isExpanded: Boolean = false,
    isDark: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeRowBg = when {
        !isExpanded -> Color.Transparent
        isDark -> Color.White.copy(alpha = 0.05f)
        else -> Color.Black.copy(alpha = 0.035f)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(activeRowBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(badgeColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(15.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = LucideIcons.ChevronDown,
            contentDescription = null,
            tint = if (isExpanded) BrandAccent else secondaryColor,
            modifier = Modifier
                .size(16.dp)
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
    isDark: Boolean,
    onSelect: (K) -> Unit
) {
    val density = LocalDensity.current
    val dropdownBg = if (isDark) Color(0xFF1E293B) else Color.White
    val dropdownBorder = if (isDark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.08f)
    val itemTextColor = if (isDark) Color(0xFFE2E8F0) else Color(0xFF1E293B)
    val selectedItemBg = if (isDark) BrandAccent.copy(alpha = 0.16f) else BrandAccent.copy(alpha = 0.09f)

    val itemHeightDp = 38.dp
    val verticalPaddingDp = 8.dp
    val maxDropdownHeightDp = 228.dp
    val gapPx = with(density) { 4.dp.toPx() }
    val screenEdgeMarginPx = with(density) { 14.dp.toPx() }

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
    val maxHeightDp = with(density) { maxAllowedHeightPx.toDp() }.coerceAtLeast(46.dp)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = dropdownBg,
        shadowElevation = 12.dp,
        modifier = Modifier
            .offset {
                IntOffset(
                    x = anchorRect.left.roundToInt(),
                    y = topOffsetPx.roundToInt()
                )
            }
            .width(widthDp)
            .heightIn(max = maxHeightDp)
            .border(1.dp, dropdownBorder, RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            items(options) { (key, label) ->
                val isSelected = key == selectedKey
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeightDp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) selectedItemBg else Color.Transparent)
                        .clickable { onSelect(key) }
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) BrandAccent else itemTextColor,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(8.dp))
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

@Composable
fun CustomDatePickerDialogModal(
    initialMillis: Long,
    isEn: Boolean,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val context = LocalContext.current
    var selectedMillis by remember(initialMillis) { mutableStateOf(initialMillis) }
    val dateFmt = remember { java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()) }

    val sheetBg = if (isDark) Color(0xFF172033) else Color(0xFFF2F2F7)
    val closeBtnBg = if (isDark) Color(0xFF263449) else Color(0xFFE2E8F0)
    val fieldBg = if (isDark) Color(0xFF242F41) else Color.White
    val cancelBtnBg = if (isDark) Color(0xFF263449) else Color.White
    val textColor = if (isDark) TextPrimaryDark else TextTitleLight
    val subColor = if (isDark) TextSecondaryDark else TextSecondary

    MobileFullScreenOverlay(onDismissRequest = onDismiss) {
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
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    ),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                color = sheetBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp)
                        .padding(top = 10.dp, bottom = 20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(36.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.15f))
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEn) "Select Date" else "Tùy chọn ngày",
                            color = textColor,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(closeBtnBg)
                                .clickable(onClick = onDismiss),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = LucideIcons.X,
                                contentDescription = if (isEn) "Close" else "Đóng",
                                tint = subColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = if (isEn) "Choose a date to filter events (00:00 - 23:59)" else "Chọn ngày cần tra cứu dữ liệu (00:00 - 23:59)",
                        color = subColor,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(fieldBg)
                            .clickable {
                                val cal = java.util.Calendar.getInstance().apply { timeInMillis = selectedMillis }
                                android.app.DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        val c = java.util.Calendar.getInstance().apply {
                                            set(java.util.Calendar.YEAR, year)
                                            set(java.util.Calendar.MONTH, month)
                                            set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth)
                                        }
                                        selectedMillis = c.timeInMillis
                                    },
                                    cal.get(java.util.Calendar.YEAR),
                                    cal.get(java.util.Calendar.MONTH),
                                    cal.get(java.util.Calendar.DAY_OF_MONTH)
                                ).show()
                            }
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
                                tint = BrandAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = dateFmt.format(java.util.Date(selectedMillis)),
                                color = textColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = if (isEn) "Change" else "Chọn lịch",
                            color = BrandAccent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(cancelBtnBg)
                                .clickable(onClick = onDismiss),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isEn) "Cancel" else "Hủy",
                                color = textColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(BrandPrimary)
                                .clickable { onConfirm(selectedMillis) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isEn) "Confirm" else "Xác nhận",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomRangePickerDialogModal(
    initialFromMillis: Long,
    initialToMillis: Long,
    isEn: Boolean,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Long, Long) -> Unit
) {
    val context = LocalContext.current
    var fromMillis by remember(initialFromMillis) { mutableStateOf(initialFromMillis) }
    var toMillis by remember(initialToMillis) { mutableStateOf(initialToMillis) }
    val dateFmt = remember { java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()) }
    val timeFmt = remember { java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()) }

    val sheetBg = if (isDark) Color(0xFF172033) else Color(0xFFF2F2F7)
    val closeBtnBg = if (isDark) Color(0xFF263449) else Color(0xFFE2E8F0)
    val fieldBg = if (isDark) Color(0xFF242F41) else Color.White
    val cancelBtnBg = if (isDark) Color(0xFF263449) else Color.White
    val textColor = if (isDark) TextPrimaryDark else TextTitleLight
    val subColor = if (isDark) TextSecondaryDark else TextSecondary

    fun pickDate(currentMs: Long, onResult: (Long) -> Unit) {
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = currentMs }
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val c = java.util.Calendar.getInstance().apply {
                    timeInMillis = currentMs
                    set(java.util.Calendar.YEAR, year)
                    set(java.util.Calendar.MONTH, month)
                    set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                onResult(c.timeInMillis)
            },
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH),
            cal.get(java.util.Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun pickTime(currentMs: Long, onResult: (Long) -> Unit) {
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = currentMs }
        android.app.TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val c = java.util.Calendar.getInstance().apply {
                    timeInMillis = currentMs
                    set(java.util.Calendar.HOUR_OF_DAY, hourOfDay)
                    set(java.util.Calendar.MINUTE, minute)
                    set(java.util.Calendar.SECOND, 0)
                }
                onResult(c.timeInMillis)
            },
            cal.get(java.util.Calendar.HOUR_OF_DAY),
            cal.get(java.util.Calendar.MINUTE),
            true
        ).show()
    }

    MobileFullScreenOverlay(onDismissRequest = onDismiss) {
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
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    ),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                color = sheetBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp)
                        .padding(top = 10.dp, bottom = 20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(36.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.15f))
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEn) "Custom Time Range" else "Tùy chỉnh thời gian",
                            color = textColor,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(closeBtnBg)
                                .clickable(onClick = onDismiss),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = LucideIcons.X,
                                contentDescription = if (isEn) "Close" else "Đóng",
                                tint = subColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = if (isEn) "From" else "Từ thời gian",
                        color = subColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1.4f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(fieldBg)
                                .clickable { pickDate(fromMillis) { fromMillis = it } }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = dateFmt.format(java.util.Date(fromMillis)),
                                color = textColor,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(fieldBg)
                                .clickable { pickTime(fromMillis) { fromMillis = it } }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = timeFmt.format(java.util.Date(fromMillis)),
                                color = textColor,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isEn) "To" else "Đến thời gian",
                        color = subColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1.4f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(fieldBg)
                                .clickable { pickDate(toMillis) { toMillis = it } }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = dateFmt.format(java.util.Date(toMillis)),
                                color = textColor,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(fieldBg)
                                .clickable { pickTime(toMillis) { toMillis = it } }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = timeFmt.format(java.util.Date(toMillis)),
                                color = textColor,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(cancelBtnBg)
                                .clickable(onClick = onDismiss),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isEn) "Cancel" else "Hủy",
                                color = textColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(BrandPrimary)
                                .clickable { onConfirm(fromMillis, toMillis) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isEn) "Confirm" else "Xác nhận",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
