package com.tenli.oneview.ui.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.tenli.oneview.ui.theme.spacing
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val DOT_COUNT = 3
private const val DEFAULT_DELAY_UNIT_MS = 300
private const val PULSE_HALF_CYCLE_MS = 450
private const val PULSE_CYCLE_DURATION_MS = PULSE_HALF_CYCLE_MS * 2
private const val ROTATION_DURATION_MS = 1050
private const val CLUSTER_RADIUS_FACTOR = 0.85f
private const val CLUSTER_CONTAINER_FACTOR = 2.8f
private const val MIN_DOT_SCALE = 0.84f
private const val MAX_DOT_SCALE = 1.06f

/**
 * Hiển thị 3 chấm tròn chụm lại thành cụm tam giác đều ở tâm và xoay mượt.
 */
@Composable
fun WaveDotsLoading(
    modifier: Modifier = Modifier,
    dotSize: Dp = MaterialTheme.spacing.space8,
    dotColor: Color = MaterialTheme.colorScheme.primary,
    dotColors: List<Color> = remember(dotColor) {
        listOf(dotColor, dotColor.copy(alpha = 0.72f), dotColor.copy(alpha = 0.45f))
    },
    delayUnit: Int = DEFAULT_DELAY_UNIT_MS,
    @Suppress("UNUSED_PARAMETER")
    maxOffset: Dp = MaterialTheme.spacing.space10
) {
    val density = LocalDensity.current
    val orbitRadiusPx = remember(density, dotSize) {
        with(density) { (dotSize * CLUSTER_RADIUS_FACTOR).toPx() }
    }
    val containerSize = remember(dotSize) { dotSize * CLUSTER_CONTAINER_FACTOR }

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

    val rotationDeg: Float
    val phaseMs: Float
    if (isStarted) {
        val transition = rememberInfiniteTransition(label = "clusterDots")
        val animRotation by transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = ROTATION_DURATION_MS, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "clusterRotation"
        )
        val animPhase by transition.animateFloat(
            initialValue = 0f,
            targetValue = PULSE_CYCLE_DURATION_MS.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = PULSE_CYCLE_DURATION_MS, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "clusterPulse"
        )
        rotationDeg = animRotation
        phaseMs = animPhase
    } else {
        rotationDeg = 0f
        phaseMs = 0f
    }

    Box(
        modifier = modifier
            .size(containerSize)
            .graphicsLayer {
                rotationZ = rotationDeg
            },
        contentAlignment = Alignment.Center
    ) {
        for (index in 0 until DOT_COUNT) {
            val angleRad = remember(index) {
                (-PI / 2.0) + index * (2.0 * PI / DOT_COUNT)
            }
            val offsetX = remember(orbitRadiusPx, angleRad) {
                (orbitRadiusPx * cos(angleRad)).toFloat()
            }
            val offsetY = remember(orbitRadiusPx, angleRad) {
                (orbitRadiusPx * sin(angleRad)).toFloat()
            }

            Box(
                modifier = Modifier
                    .size(dotSize)
                    .graphicsLayer {
                        val shifted = (phaseMs - index * delayUnit + PULSE_CYCLE_DURATION_MS * 4) % PULSE_CYCLE_DURATION_MS
                        val rawFraction = if (shifted <= PULSE_HALF_CYCLE_MS) {
                            shifted / PULSE_HALF_CYCLE_MS.toFloat()
                        } else {
                            (PULSE_CYCLE_DURATION_MS - shifted) / PULSE_HALF_CYCLE_MS.toFloat()
                        }
                        val eased = FastOutSlowInEasing.transform(rawFraction.coerceIn(0f, 1f))
                        val scale = MIN_DOT_SCALE + (MAX_DOT_SCALE - MIN_DOT_SCALE) * eased
                        translationX = offsetX
                        translationY = offsetY
                        scaleX = scale
                        scaleY = scale
                        shape = CircleShape
                        clip = true
                    }
                    .background(dotColors.getOrElse(index) { dotColor })
            )
        }
    }
}
