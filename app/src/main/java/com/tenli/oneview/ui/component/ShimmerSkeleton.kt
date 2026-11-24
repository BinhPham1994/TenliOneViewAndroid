package com.tenli.oneview.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.tenli.oneview.ui.theme.Slate100
import com.tenli.oneview.ui.theme.Slate200
import com.tenli.oneview.ui.theme.appColors
import com.tenli.oneview.ui.theme.spacing

private val ShimmerDarkBase = Color(0xFF1E2B42)
private val ShimmerDarkHighlight = Color(0xFF2B3B56)
private val ListSkeletonThumbBaseWidth = 156.dp
private const val SKELETON_ASPECT_RATIO = 16f / 9f

/**
 * Trả về [State] tiến độ shimmer (0 recomposition khi dùng với [Modifier.shimmerBackground] đọc trong pha draw).
 * Tự động dừng hoàn toàn khi [enabled] = false hoặc Lifecycle < STARTED (0 Choreographer ticks khi ẩn/nền).
 */
@Composable
fun rememberShimmerProgressState(enabled: Boolean = true): State<Float> {
    val lifecycleOwner = LocalLifecycleOwner.current
    var isStarted by remember(lifecycleOwner) {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, _ ->
            isStarted = lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val staticState = remember { mutableFloatStateOf(-1f) }
    return if (enabled && isStarted) {
        val transition = rememberInfiniteTransition(label = "shimmerTransition")
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1250, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmerProgress"
        )
    } else {
        staticState
    }
}

/**
 * Vẽ hiệu ứng shimmer hoàn toàn trong pha draw (`drawWithCache`) —
 * cache Brush 1 lần theo kích thước/theme, 0 cấp phát đối tượng và 0 recomposition trên mỗi frame 60/120fps.
 */
fun Modifier.shimmerBackground(
    progressState: State<Float>,
    isDark: Boolean
): Modifier = drawWithCache {
    val baseColor = if (isDark) ShimmerDarkBase else Slate200
    val highlightColor = if (isDark) ShimmerDarkHighlight else Slate100
    val bandWidth = maxOf(size.width * 0.6f, 380f)
    val tiltY = minOf(size.height * 0.45f, bandWidth * 0.47f)
    val skewLeft = if (bandWidth > 0f) size.height * (tiltY / bandWidth) else 0f
    val rectWidth = skewLeft + bandWidth + if (bandWidth > 0f) (tiltY * tiltY) / bandWidth else 0f
    val highlightSize = Size(rectWidth, size.height)
    val cachedBrush = Brush.linearGradient(
        colors = listOf(highlightColor.copy(alpha = 0f), highlightColor, highlightColor.copy(alpha = 0f)),
        start = Offset(skewLeft, 0f),
        end = Offset(skewLeft + bandWidth, tiltY)
    )

    onDrawBehind {
        drawRect(color = baseColor)
        val progress = progressState.value
        if (progress >= 0f && size.width > 0f && size.height > 0f) {
            val translateX = -rectWidth + progress * (size.width + rectWidth)
            clipRect {
                translate(left = translateX) {
                    drawRect(brush = cachedBrush, size = highlightSize)
                }
            }
        }
    }
}

/**
 * Brush ánh sáng quét ngang nhẹ nhàng cho trạng thái Skeleton Loading (tự đổi màu theo Light/Dark).
 */
@Composable
fun rememberShimmerBrush(): Brush {
    val isDark = MaterialTheme.appColors.isDark
    val progress by rememberShimmerProgressState()
    val baseColor = if (isDark) ShimmerDarkBase else Slate200
    if (progress < 0f) return SolidColor(baseColor)
    val highlightColor = if (isDark) ShimmerDarkHighlight else Slate100
    val startX = (-0.6f + progress * 2.2f) * 900f
    return Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start = Offset(startX, 0f),
        end = Offset(startX + 380f, 180f)
    )
}

/**
 * Danh sách khung xương (Skeleton) khớp đúng kích thước thẻ [MobileEventCard] ở cả chế độ List và Grid,
 * giúp trải nghiệm tải lần đầu mượt mà, không bị giật khung khi dữ liệu thật đổ về.
 */
@Composable
fun EventCardSkeletonList(
    isListView: Boolean,
    count: Int = 5,
    modifier: Modifier = Modifier,
    animate: Boolean = true
) {
    val spacing = MaterialTheme.spacing
    val isDark = MaterialTheme.appColors.isDark
    val progressState = rememberShimmerProgressState(enabled = animate)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.space10),
        verticalArrangement = Arrangement.spacedBy(spacing.space10)
    ) {
        repeat(count) {
            if (isListView) {
                EventListCardSkeleton(progressState = progressState, isDark = isDark)
            } else {
                EventGridCardSkeleton(progressState = progressState, isDark = isDark)
            }
        }
    }
}

@Composable
private fun EventListCardSkeleton(
    progressState: State<Float>,
    isDark: Boolean
) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val cardShape = remember(spacing.radiusCompact) { RoundedCornerShape(spacing.radiusCompact) }
    val thumbShape = remember(spacing.radiusSmall) { RoundedCornerShape(spacing.radiusSmall) }
    val pillShape = remember(spacing.radiusTiny) { RoundedCornerShape(spacing.radiusTiny) }
    val thumbWidth = ListSkeletonThumbBaseWidth * spacing.scaleFactor

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(colors.surface)
            .padding(spacing.space10),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(thumbWidth)
                .aspectRatio(SKELETON_ASPECT_RATIO)
                .clip(thumbShape)
                .shimmerBackground(progressState, isDark)
        )
        Spacer(modifier = Modifier.width(spacing.space12))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(spacing.space8)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.45f)
                    .height(spacing.space12)
                    .clip(pillShape)
                    .shimmerBackground(progressState, isDark)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .height(spacing.space20)
                    .clip(pillShape)
                    .shimmerBackground(progressState, isDark)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.68f)
                    .height(spacing.space12)
                    .clip(pillShape)
                    .shimmerBackground(progressState, isDark)
            )
            Spacer(modifier = Modifier.height(spacing.space2))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.42f)
                    .height(spacing.space10)
                    .clip(pillShape)
                    .shimmerBackground(progressState, isDark)
            )
        }
    }
}

@Composable
private fun EventGridCardSkeleton(
    progressState: State<Float>,
    isDark: Boolean
) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val cardShape = remember(spacing.radiusCompact) { RoundedCornerShape(spacing.radiusCompact) }
    val pillShape = remember(spacing.radiusTiny) { RoundedCornerShape(spacing.radiusTiny) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(colors.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(SKELETON_ASPECT_RATIO)
                .shimmerBackground(progressState, isDark)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.space12),
            verticalArrangement = Arrangement.spacedBy(spacing.space8)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.62f)
                    .height(spacing.space14)
                    .clip(pillShape)
                    .shimmerBackground(progressState, isDark)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(spacing.space10)
                    .clip(pillShape)
                    .shimmerBackground(progressState, isDark)
            )
        }
    }
}

/**
 * Khung xương lưới 2 cột cho danh sách Camera ở tab Trực tiếp.
 */
@Composable
fun CameraGridSkeleton(
    rowCount: Int = 3,
    modifier: Modifier = Modifier,
    animate: Boolean = true
) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val isDark = colors.isDark
    val progressState = rememberShimmerProgressState(enabled = animate)
    val cardShape = remember(spacing.radiusCompact) { RoundedCornerShape(spacing.radiusCompact) }
    val pillShape = remember(spacing.radiusTiny) { RoundedCornerShape(spacing.radiusTiny) }
    val chipShape = remember { RoundedCornerShape(50) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.space10, vertical = spacing.space8),
        verticalArrangement = Arrangement.spacedBy(spacing.space8)
    ) {
        // Thanh header nhỏ + dải chip lọc nhanh giả lập
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = spacing.space4),
            horizontalArrangement = Arrangement.spacedBy(spacing.space8)
        ) {
            Box(
                modifier = Modifier
                    .width(72.dp * spacing.scaleFactor)
                    .height(spacing.space28)
                    .clip(chipShape)
                    .shimmerBackground(progressState, isDark)
            )
            Box(
                modifier = Modifier
                    .width(92.dp * spacing.scaleFactor)
                    .height(spacing.space28)
                    .clip(chipShape)
                    .shimmerBackground(progressState, isDark)
            )
            Box(
                modifier = Modifier
                    .width(108.dp * spacing.scaleFactor)
                    .height(spacing.space28)
                    .clip(chipShape)
                    .shimmerBackground(progressState, isDark)
            )
        }

        repeat(rowCount) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.space8)
            ) {
                repeat(2) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(cardShape)
                            .background(colors.surface)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(SKELETON_ASPECT_RATIO)
                                .shimmerBackground(progressState, isDark)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = spacing.space8, vertical = spacing.space8),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.72f)
                                    .height(spacing.space12)
                                    .clip(pillShape)
                                    .shimmerBackground(progressState, isDark)
                            )
                        }
                    }
                }
            }
        }
    }
}
