package com.tenli.oneview.ui.component

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
    val isEn = remember { LocaleManager.getLocale(context).startsWith("en") }
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

    val cardBgColor = if (isDark) SurfaceDark else SurfaceLight
    val selectedBorderColor = if (isDark) BrandAccent else BrandPrimary

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
                .then(
                    if (isSelected) Modifier.border(1.5.dp, selectedBorderColor, RoundedCornerShape(12.dp))
                    else Modifier
                )
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
            .then(
                if (isSelected) Modifier.border(1.5.dp, selectedBorderColor, RoundedCornerShape(12.dp))
                else Modifier
            )
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
    val isEn = remember { LocaleManager.getLocale(context).startsWith("en") }
    val isDark = rememberIsDarkTheme()

    var draftServiceId by remember(selectedServiceId) { mutableStateOf(selectedServiceId) }
    var draftTimeFilter by remember(selectedTimeFilter) { mutableStateOf(selectedTimeFilter) }
    var draftCameraUuid by remember(selectedCameraUuid) { mutableStateOf(selectedCameraUuid) }
    var draftAiType by remember(selectedAiType) { mutableStateOf(selectedAiType) }

    // Sub-picker dialog state: null, "scope", "time", "camera", "event"
    var activePicker by remember { mutableStateOf<String?>(null) }

    val sheetBg = if (isDark) BackgroundDark else BackgroundLight
    val cardBg = if (isDark) SurfaceDark else SurfaceLight
    val textColor = if (isDark) TextPrimaryDark else TextTitleLight
    val secondaryColor = if (isDark) TextSecondaryDark else TextSecondary

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.38f))
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
                    .clip(RoundedCornerShape(24.dp))
                    .background(sheetBg)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
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
                            .clickable(onClick = onDismiss),
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
                            onClick = { activePicker = "scope" }
                        )
                    }

                    // 2. Time Range
                    if (showTime) {
                        val timeLabel = when (draftTimeFilter) {
                            TimeFilter.TODAY -> if (isEn) "Today" else "Hôm nay"
                            TimeFilter.YESTERDAY -> if (isEn) "Yesterday" else "Hôm qua"
                            TimeFilter.LAST_7_DAYS -> if (isEn) "Last 7 days" else "7 ngày qua"
                            TimeFilter.LAST_30_DAYS -> if (isEn) "Last 30 days" else "30 ngày qua"
                        }
                        FilterCellRow(
                            icon = LucideIcons.Clock,
                            badgeColor = Color(0xFFF59E0B),
                            label = timeLabel,
                            textColor = textColor,
                            secondaryColor = secondaryColor,
                            onClick = { activePicker = "time" }
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
                            onClick = { activePicker = "camera" }
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
                            onClick = { activePicker = "event" }
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
        }
    }

    // Sub-Picker Dialogs
    when (activePicker) {
        "scope" -> {
            val options = buildList<Pair<Int?, String>> {
                add(null to (if (isEn) "All systems" else "Tất cả hệ thống"))
                aiServices.forEach { add(it.id to it.name) }
            }
            OptionPickerDialog(
                title = if (isEn) "Select system" else "Chọn hệ thống",
                options = options,
                selectedKey = draftServiceId,
                isDark = isDark,
                onSelect = {
                    draftServiceId = it
                    draftCameraUuid = null
                    activePicker = null
                },
                onDismiss = { activePicker = null }
            )
        }
        "time" -> {
            val options = listOf(
                TimeFilter.TODAY to (if (isEn) "Today" else "Hôm nay"),
                TimeFilter.YESTERDAY to (if (isEn) "Yesterday" else "Hôm qua"),
                TimeFilter.LAST_7_DAYS to (if (isEn) "Last 7 days" else "7 ngày qua"),
                TimeFilter.LAST_30_DAYS to (if (isEn) "Last 30 days" else "30 ngày qua")
            )
            OptionPickerDialog(
                title = if (isEn) "Time range" else "Khoảng thời gian",
                options = options,
                selectedKey = draftTimeFilter,
                isDark = isDark,
                onSelect = {
                    draftTimeFilter = it
                    activePicker = null
                },
                onDismiss = { activePicker = null }
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
            OptionPickerDialog(
                title = if (isEn) "Select camera" else "Chọn camera",
                options = options,
                selectedKey = draftCameraUuid,
                isDark = isDark,
                onSelect = {
                    draftCameraUuid = it
                    activePicker = null
                },
                onDismiss = { activePicker = null }
            )
        }
        "event" -> {
            val options = buildList<Pair<String?, String>> {
                add(null to (if (isEn) "All events" else "Tất cả sự kiện"))
                activeModules.forEach { mod ->
                    add(mod.sensorType to (if (isEn) mod.labelEn else mod.labelVi))
                }
            }
            OptionPickerDialog(
                title = if (isEn) "Event type" else "Loại sự kiện",
                options = options,
                selectedKey = draftAiType,
                isDark = isDark,
                onSelect = {
                    draftAiType = it
                    activePicker = null
                },
                onDismiss = { activePicker = null }
            )
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
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
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
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = LucideIcons.ChevronDown,
            contentDescription = null,
            tint = secondaryColor,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun <K> OptionPickerDialog(
    title: String,
    options: List<Pair<K, String>>,
    selectedKey: K,
    isDark: Boolean,
    onSelect: (K) -> Unit,
    onDismiss: () -> Unit
) {
    val cardBg = if (isDark) SurfaceDark else SurfaceLight
    val textColor = if (isDark) TextPrimaryDark else TextTitleLight
    val dividerColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.06f)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = cardBg,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 460.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 14.dp)) {
                Text(
                    text = title,
                    color = textColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
                )
                HorizontalDivider(color = dividerColor, thickness = 0.5.dp)
                LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                    items(options) { (key, label) ->
                        val isSelected = key == selectedKey
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(key) }
                                .padding(horizontal = 18.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) BrandAccent else textColor,
                                fontSize = 14.5.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = LucideIcons.Check,
                                    contentDescription = null,
                                    tint = BrandAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        HorizontalDivider(
                            color = dividerColor,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(horizontal = 18.dp)
                        )
                    }
                }
            }
        }
    }
}
