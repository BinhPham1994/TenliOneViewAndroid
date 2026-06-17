package com.tenli.oneview.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = GreenOnPrimary,
    primaryContainer = GreenContainer,
    onPrimaryContainer = Color(0xFF002105),

    secondary = GreenPrimary,
    tertiary = GreenPrimary.copy(alpha = 0.7f),
    surfaceTint = Color.Transparent,

    background = IoSBackgroundLight,
    onBackground = TextPrimary,

    surface = SurfaceLight,
    onSurface = TextPrimary,

    onSurfaceVariant = TextSecondary,

    outline = TextDisabled,
    outlineVariant = SurfaceVariant,

    error = ErrorRed,
    onError = Color.White,

    surfaceContainer = SurfaceLight
)

val MaterialTheme.spacing: Dimensions
    @Composable
    @ReadOnlyComposable
    get() = LocalSpacing.current

@Composable
fun TenliAIoTTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalSpacing provides Dimensions()
    ) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            typography = AppTypography,
            content = content
        )
    }
}