package com.tenli.oneview.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.tenli.oneview.ui.theme.spacing
import kotlinx.coroutines.delay

private const val DOT_COUNT = 3
private const val DEFAULT_DELAY_UNIT_MS = 200
private const val ANIMATION_DURATION_MS = 500

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
    val dots = remember { List(DOT_COUNT) { Animatable(0f) } }

    dots.forEachIndexed { index, animatable ->
        LaunchedEffect(animatable, delayUnit) {
            delay((index * delayUnit).toLong())
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = ANIMATION_DURATION_MS, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing.space6),
        verticalAlignment = Alignment.CenterVertically
    ) {
        dots.forEachIndexed { index, animatable ->
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .graphicsLayer {
                        translationY = -maxOffsetPx * animatable.value
                        shape = CircleShape
                        clip = true
                    }
                    .background(dotColors.getOrElse(index) { dotColor })
            )
        }
    }
}
