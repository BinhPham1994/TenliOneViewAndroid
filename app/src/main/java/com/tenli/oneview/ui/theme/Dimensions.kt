package com.tenli.oneview.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Dimensions(
    // Spacing
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp,
    val paddingScreen: Dp = 16.dp,
    
    // Icon Sizes
    val iconSmall: Dp = 16.dp,
    val iconMedium: Dp = 24.dp,
    val iconLarge: Dp = 32.dp,
    val iconExtraLarge: Dp = 48.dp,
    
    // Radius
    val radiusSmall: Dp = 8.dp,
    val radiusMedium: Dp = 16.dp,
    val radiusLarge: Dp = 24.dp,
    
    // Borders
    val borderThin: Dp = 0.5.dp,
    val borderMedium: Dp = 1.dp,

    // Screen scale factor (1.0f at 365dp baseline)
    val scaleFactor: Float = 1f
) {
    companion object {
        fun scaled(scale: Float): Dimensions {
            val s = scale.coerceIn(0.95f, 1.15f)
            return Dimensions(
                extraSmall = (4f * s).dp,
                small = (8f * s).dp,
                medium = (16f * s).dp,
                large = (24f * s).dp,
                extraLarge = (32f * s).dp,
                paddingScreen = (16f * s).dp,
                iconSmall = (16f * s).dp,
                iconMedium = (24f * s).dp,
                iconLarge = (32f * s).dp,
                iconExtraLarge = (48f * s).dp,
                radiusSmall = (8f * s).dp,
                radiusMedium = (16f * s).dp,
                radiusLarge = (24f * s).dp,
                borderThin = 0.5.dp,
                borderMedium = 1.dp,
                scaleFactor = s
            )
        }
    }
}

val LocalSpacing = compositionLocalOf { Dimensions() }
