package com.tenli.oneview.ui.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.tenli.oneview.ui.theme.spacing

private const val DOT_COUNT = 3
private const val DEFAULT_DELAY_UNIT_MS = 200
private const val ANIMATION_DURATION_MS = 500
private const val CYCLE_DURATION_MS = ANIMATION_DURATION_MS * 2

@Composable
fun WaveDotsLoading(
    modifier: Modifier = Modifier,
    dotSize: Dp = MaterialTheme.spacing.space8,
    dotColor: Color = MaterialTheme.colorScheme.primary,
    dotColors: List<Color> = remember(dotColor) {
        listOf(dotColor, dotColor.copy(alpha = 0.7f), dotColor.copy(alpha = 0.4f))
    },
    delayUnit: Int = DEFAULT_DELAY_UNIT_MS,
    maxOffset: Dp = MaterialTheme.spacing.space10
) {
    val spacing = MaterialTheme.spacing
    val density = LocalDensity.current
    val maxOffsetPx = remember(density, maxOffset) { with(density) { maxOffset.toPx() } }

    // Dùng DUY NHẤT 1 InfiniteTransition thay vì 3 coroutine Animatable riêng lẻ cho mỗi chấm
    val transition = rememberInfiniteTransition(label = "waveDots")
    val phaseMs by transition.animateFloat(
        initialValue = 0f,
        targetValue = CYCLE_DURATION_MS.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = CYCLE_DURATION_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing.space6),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (index in 0 until DOT_COUNT) {
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .graphicsLayer {
                        val shifted = (phaseMs - index * delayUnit + CYCLE_DURATION_MS * 4) % CYCLE_DURATION_MS
                        val rawFraction = if (shifted <= ANIMATION_DURATION_MS) {
                            shifted / ANIMATION_DURATION_MS.toFloat()
                        } else {
                            (CYCLE_DURATION_MS - shifted) / ANIMATION_DURATION_MS.toFloat()
                        }
                        val eased = FastOutSlowInEasing.transform(rawFraction.coerceIn(0f, 1f))
                        translationY = -maxOffsetPx * eased
                        shape = CircleShape
                        clip = true
                    }
                    .background(dotColors.getOrElse(index) { dotColor })
            )
        }
    }
}
