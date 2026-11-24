package com.tenli.oneview.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.tenli.oneview.ui.theme.LucideIcons
import com.tenli.oneview.ui.theme.appColors
import com.tenli.oneview.ui.theme.spacing
import java.util.Locale
import kotlin.math.abs
import kotlinx.coroutines.launch

private const val DEFAULT_MIN_SCALE = 1f
private const val DEFAULT_MAX_SCALE = 5f
private const val DOUBLE_TAP_TARGET_SCALE = 2.5f
private const val ZOOM_ACTIVE_EPSILON = 1.02f
private const val ZOOM_BADGE_THRESHOLD = 1.06f

/**
 * Khung media hỗ trợ Pinch-to-Zoom 2 ngón (1.0x..5.0x), kéo pan 1 ngón khi đang zoom,
 * chạm đúp để phóng to/thu nhỏ và pill hiển thị mức zoom ("2.4x ✕") để đặt lại nhanh.
 *
 * - Chỉ phóng to lớp [content] bằng GPU `graphicsLayer`, giữ nguyên kích thước 1.0x cho lớp [overlay].
 * - Khi `scale == 1f` và người dùng chỉ kéo 1 ngón: KHÔNG nuốt sự kiện để cử chỉ vuốt đổi tab / kéo đóng
 *   sheet của màn hình cha vẫn hoạt động mượt 120Hz.
 */
@Composable
fun ZoomableMediaBox(
    modifier: Modifier = Modifier,
    zoomResetKey: Any? = Unit,
    smoothResetTrigger: Int = 0,
    minScale: Float = DEFAULT_MIN_SCALE,
    maxScale: Float = DEFAULT_MAX_SCALE,
    enableDoubleTapZoom: Boolean = true,
    showZoomBadge: Boolean = true,
    badgeAlignment: Alignment = Alignment.BottomStart,
    badgePadding: PaddingValues = PaddingValues(8.dp),
    onTap: (() -> Unit)? = null,
    onDoubleTap: (() -> Unit)? = null,
    onZoomChanged: (Boolean) -> Unit = {},
    overlay: @Composable BoxScope.(scale: Float, resetZoom: () -> Unit) -> Unit = { _, _ -> },
    content: @Composable BoxScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var containerSize = remember { IntSize.Zero }

    val currentOnTap by rememberUpdatedState(onTap)
    val currentOnDoubleTap by rememberUpdatedState(onDoubleTap)
    val currentOnZoomChanged by rememberUpdatedState(onZoomChanged)
    val currentEnableDoubleTapZoom by rememberUpdatedState(enableDoubleTapZoom)

    fun clampOffset(rawX: Float, rawY: Float, targetScale: Float): Offset {
        if (targetScale <= 1.001f || containerSize.width <= 0 || containerSize.height <= 0) {
            return Offset.Zero
        }
        val maxX = (containerSize.width * (targetScale - 1f)) * 0.5f
        val maxY = (containerSize.height * (targetScale - 1f)) * 0.5f
        return Offset(
            x = rawX.coerceIn(-maxX, maxX),
            y = rawY.coerceIn(-maxY, maxY)
        )
    }

    fun applyTransformInstant(targetScale: Float, targetOffset: Offset) {
        val clampedScale = targetScale.coerceIn(minScale, maxScale)
        val clampedOffset = clampOffset(targetOffset.x, targetOffset.y, clampedScale)
        val wasZoomed = scale > ZOOM_ACTIVE_EPSILON
        scale = clampedScale
        offsetX = clampedOffset.x
        offsetY = clampedOffset.y
        val nowZoomed = clampedScale > ZOOM_ACTIVE_EPSILON
        if (wasZoomed != nowZoomed) {
            currentOnZoomChanged(nowZoomed)
        }
    }

    val resetZoomSmooth: () -> Unit = {
        scope.launch {
            val startScale = scale
            val startOffset = Offset(offsetX, offsetY)
            if (startScale <= 1.005f && startOffset == Offset.Zero) {
                applyTransformInstant(1f, Offset.Zero)
                return@launch
            }
            val anim = Animatable(0f)
            anim.animateTo(1f, animationSpec = tween(durationMillis = 200)) {
                val fraction = value
                val s = startScale + (1f - startScale) * fraction
                val ox = startOffset.x * (1f - fraction)
                val oy = startOffset.y * (1f - fraction)
                applyTransformInstant(s, Offset(ox, oy))
            }
            applyTransformInstant(1f, Offset.Zero)
        }
    }

    LaunchedEffect(zoomResetKey) {
        if (scale != 1f || offsetX != 0f || offsetY != 0f) {
            applyTransformInstant(1f, Offset.Zero)
        }
    }

    LaunchedEffect(smoothResetTrigger) {
        if (smoothResetTrigger > 0) {
            resetZoomSmooth()
        }
    }

    Box(
        modifier = modifier
            .clipToBounds()
            .onSizeChanged { size ->
                containerSize = size
                if (scale > 1.001f) {
                    val clamped = clampOffset(offsetX, offsetY, scale)
                    offsetX = clamped.x
                    offsetY = clamped.y
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(minScale, maxScale) {
                    val touchSlop = viewConfiguration.touchSlop
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        var isTransforming = false
                        var panAccumulated = Offset.Zero

                        do {
                            val event = awaitPointerEvent(PointerEventPass.Main)
                            val canceled = event.changes.any { it.isConsumed }
                            if (canceled) continue

                            val pressedCount = event.changes.count { it.pressed }
                            if (pressedCount == 0) break

                            if (pressedCount >= 2) {
                                val zoomChange = event.calculateZoom()
                                val panChange = event.calculatePan()
                                val centroid = event.calculateCentroid(useCurrent = false)

                                if (!isTransforming) {
                                    panAccumulated += panChange
                                    if (abs(1f - zoomChange) > 0.015f || panAccumulated.getDistance() > touchSlop) {
                                        isTransforming = true
                                    }
                                }

                                if (isTransforming && centroid != Offset.Unspecified) {
                                    val oldScale = scale
                                    val newScale = (oldScale * zoomChange).coerceIn(minScale, maxScale)
                                    val effectiveZoom = if (oldScale > 0f) newScale / oldScale else 1f
                                    val center = Offset(size.width * 0.5f, size.height * 0.5f)
                                    val newOffset = (centroid - center) * (1f - effectiveZoom) +
                                        Offset(offsetX, offsetY) * effectiveZoom +
                                        panChange
                                    applyTransformInstant(newScale, newOffset)
                                }

                                event.changes.forEach { change ->
                                    if (change.positionChanged()) {
                                        change.consume()
                                    }
                                }
                            } else if (scale > ZOOM_ACTIVE_EPSILON) {
                                val panChange = event.calculatePan()
                                if (!isTransforming) {
                                    panAccumulated += panChange
                                    if (panAccumulated.getDistance() > touchSlop) {
                                        isTransforming = true
                                    }
                                }
                                if (isTransforming) {
                                    applyTransformInstant(
                                        targetScale = scale,
                                        targetOffset = Offset(offsetX + panChange.x, offsetY + panChange.y)
                                    )
                                    event.changes.forEach { change ->
                                        if (change.positionChanged()) {
                                            change.consume()
                                        }
                                    }
                                }
                            }
                        } while (event.changes.any { it.pressed })

                        if (scale in 1.0001f..1.03f) {
                            applyTransformInstant(1f, Offset.Zero)
                        }
                    }
                }
                .pointerInput(onDoubleTap != null, enableDoubleTapZoom) {
                    detectTapGestures(
                        onTap = {
                            currentOnTap?.invoke()
                        },
                        onDoubleTap = if (onDoubleTap != null || enableDoubleTapZoom) {
                            { tapOffset ->
                                val customDoubleTap = currentOnDoubleTap
                                if (customDoubleTap != null) {
                                    customDoubleTap.invoke()
                                } else if (currentEnableDoubleTapZoom) {
                                    if (scale > ZOOM_BADGE_THRESHOLD) {
                                        resetZoomSmooth()
                                    } else {
                                        val targetScale = DOUBLE_TAP_TARGET_SCALE.coerceAtMost(maxScale)
                                        val center = Offset(size.width * 0.5f, size.height * 0.5f)
                                        val rawTargetOffset = (tapOffset - center) * (1f - targetScale)
                                        val targetOffset = clampOffset(rawTargetOffset.x, rawTargetOffset.y, targetScale)
                                        scope.launch {
                                            val anim = Animatable(Offset(0f, 0f), Offset.VectorConverter)
                                            val startScale = scale
                                            val startOffset = Offset(offsetX, offsetY)
                                            anim.animateTo(
                                                targetValue = Offset(1f, 0f),
                                                animationSpec = tween(durationMillis = 220)
                                            ) {
                                                val f = value.x
                                                val s = startScale + (targetScale - startScale) * f
                                                val ox = startOffset.x + (targetOffset.x - startOffset.x) * f
                                                val oy = startOffset.y + (targetOffset.y - startOffset.y) * f
                                                applyTransformInstant(s, Offset(ox, oy))
                                            }
                                            applyTransformInstant(targetScale, targetOffset)
                                        }
                                    }
                                }
                            }
                        } else null
                    )
                }
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offsetX
                    translationY = offsetY
                },
            contentAlignment = Alignment.Center,
            content = content
        )

        overlay(scale, resetZoomSmooth)

        if (showZoomBadge) {
            AnimatedVisibility(
                visible = scale > ZOOM_BADGE_THRESHOLD,
                enter = fadeIn(animationSpec = tween(160)),
                exit = fadeOut(animationSpec = tween(160)),
                modifier = Modifier
                    .align(badgeAlignment)
                    .padding(badgePadding)
            ) {
                ZoomResetPill(
                    scale = scale,
                    onClick = resetZoomSmooth
                )
            }
        }
    }
}

@Composable
internal fun ZoomResetPill(
    scale: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val pillShape = RoundedCornerShape(spacing.radiusSmall)
    val label = remember(scale) {
        String.format(Locale.US, "%.1fx", scale)
    }

    Row(
        modifier = modifier
            .clip(pillShape)
            .background(colors.mediaBg.copy(alpha = 0.68f))
            .border(spacing.borderMedium, colors.onMedia.copy(alpha = 0.18f), pillShape)
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.space8, vertical = spacing.space4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.space4)
    ) {
        Text(
            text = label,
            color = colors.onMedia,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
        Icon(
            imageVector = LucideIcons.X,
            contentDescription = "Reset zoom",
            tint = colors.onMedia.copy(alpha = 0.85f),
            modifier = Modifier.size(spacing.iconTiny)
        )
    }
}
