package com.tenli.oneview.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = BrandOnPrimary,
    primaryContainer = BrandContainer,
    onPrimaryContainer = Color(0xFF331400),

    secondary = BrandPrimary,
    tertiary = BrandPrimary.copy(alpha = 0.7f),
    surfaceTint = Color.Transparent,

    background = BackgroundLight,
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

private val DarkColorScheme = androidx.compose.material3.darkColorScheme(
    primary = BrandPrimary,
    onPrimary = BrandOnPrimary,
    primaryContainer = BrandContainer,
    onPrimaryContainer = Color(0xFF331400),

    secondary = BrandPrimary,
    tertiary = BrandPrimary.copy(alpha = 0.7f),
    surfaceTint = Color.Transparent,

    background = BackgroundDark,
    onBackground = TextPrimaryDark,

    surface = SurfaceDark,
    onSurface = TextPrimaryDark,

    onSurfaceVariant = TextSecondaryDark,

    outline = TextDisabledDark,
    outlineVariant = SurfaceVariantDark,

    error = ErrorRed,
    onError = Color.White,

    surfaceContainer = SurfaceDark
)

val MaterialTheme.spacing: Dimensions
    @Composable
    @ReadOnlyComposable
    get() = LocalSpacing.current

@Composable
fun TenliAIoTTheme(content: @Composable () -> Unit) {
    val themeMode by ThemeManager.themeModeFlow.collectAsState()
    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
    
    val useDarkTheme = when (themeMode) {
        0 -> false
        1 -> true
        else -> isSystemDark
    }

    val colorScheme = if (useDarkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    CompositionLocalProvider(
        LocalSpacing provides Dimensions()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}