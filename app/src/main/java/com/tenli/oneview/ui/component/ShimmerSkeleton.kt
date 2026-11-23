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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tenli.oneview.ui.theme.Slate100
import com.tenli.oneview.ui.theme.Slate200
import com.tenli.oneview.ui.theme.appColors
import com.tenli.oneview.ui.theme.spacing

private val ShimmerDarkBase = Color(0xFF1E2B42)
private val ShimmerDarkHighlight = Color(0xFF2B3B56)
private val ListSkeletonThumbBaseWidth = 156.dp
private const val SKELETON_ASPECT_RATIO = 16f / 9f

/**
 * Brush ánh sáng quét ngang nhẹ nhàng cho trạng thái Skeleton Loading (tự đổi màu theo Light/Dark).
 */
@Composable
fun rememberShimmerBrush(): Brush {
    val isDark = MaterialTheme.appColors.isDark
    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val progress by transition.animateFloat(
        initialValue = -0.6f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1250, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerProgress"
    )
    val baseColor = if (isDark) ShimmerDarkBase else Slate200
    val highlightColor = if (isDark) ShimmerDarkHighlight else Slate100
    val startX = progress * 900f
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
    modifier: Modifier = Modifier
) {
    val spacing = MaterialTheme.spacing
    val brush = rememberShimmerBrush()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.space10),
        verticalArrangement = Arrangement.spacedBy(spacing.space10)
    ) {
        repeat(count) {
            if (isListView) {
                EventListCardSkeleton(brush = brush)
            } else {
                EventGridCardSkeleton(brush = brush)
            }
        }
    }
}

@Composable
private fun EventListCardSkeleton(brush: Brush) {
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
                .background(brush)
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
                    .background(brush)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .height(spacing.space20)
                    .clip(pillShape)
                    .background(brush)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.68f)
                    .height(spacing.space12)
                    .clip(pillShape)
                    .background(brush)
            )
            Spacer(modifier = Modifier.height(spacing.space2))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.42f)
                    .height(spacing.space10)
                    .clip(pillShape)
                    .background(brush)
            )
        }
    }
}

@Composable
private fun EventGridCardSkeleton(brush: Brush) {
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
                .background(brush)
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
                    .background(brush)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(spacing.space10)
                    .clip(pillShape)
                    .background(brush)
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
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val brush = rememberShimmerBrush()
    val cardShape = remember(spacing.radiusCompact) { RoundedCornerShape(spacing.radiusCompact) }
    val pillShape = remember(spacing.radiusTiny) { RoundedCornerShape(spacing.radiusTiny) }

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
                    .clip(RoundedCornerShape(50))
                    .background(brush)
            )
            Box(
                modifier = Modifier
                    .width(92.dp * spacing.scaleFactor)
                    .height(spacing.space28)
                    .clip(RoundedCornerShape(50))
                    .background(brush)
            )
            Box(
                modifier = Modifier
                    .width(108.dp * spacing.scaleFactor)
                    .height(spacing.space28)
                    .clip(RoundedCornerShape(50))
                    .background(brush)
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
                                .background(brush)
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
                                    .background(brush)
                            )
                        }
                    }
                }
            }
        }
    }
}
