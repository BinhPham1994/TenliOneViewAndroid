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
    onPrimaryContainer = Color(0xFF1E3A8A),

    secondary = BrandAccent,
    tertiary = BrandPrimaryButton,
    surfaceTint = Color.Transparent,

    background = BackgroundLight,
    onBackground = TextTitleLight,

    surface = SurfaceLight,
    onSurface = TextTitleLight,

    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,

    outline = TextDisabled,
    outlineVariant = SurfaceSubtleLight,

    error = ErrorRed,
    onError = Color.White,

    surfaceContainer = SurfaceLight,
    surfaceContainerHigh = SurfaceSubtleLight,
    surfaceContainerLow = SurfaceVariant
)

private val DarkColorScheme = androidx.compose.material3.darkColorScheme(
    primary = BrandActiveNavDark,
    onPrimary = BrandOnPrimary,
    primaryContainer = BrandContainerDark,
    onPrimaryContainer = Color(0xFFDBEAFE),

    secondary = BrandAccent,
    tertiary = BrandPrimaryButton,
    surfaceTint = Color.Transparent,

    background = BackgroundDark,
    onBackground = TextPrimaryDark,

    surface = SurfaceDark,
    onSurface = TextPrimaryDark,

    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,

    outline = TextDisabledDark,
    outlineVariant = SurfaceSubtleDark,

    error = ErrorRed,
    onError = Color.White,

    surfaceContainer = SurfaceDark,
    surfaceContainerHigh = SurfaceSubtleDark,
    surfaceContainerLow = SurfaceVariantDark
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

    val view = androidx.compose.ui.platform.LocalView.current
    if (!view.isInEditMode) {
        androidx.compose.runtime.SideEffect {
            val window = (view.context as? android.app.Activity)?.window
            if (window != null) {
                val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !useDarkTheme
                insetsController.isAppearanceLightNavigationBars = !useDarkTheme
            }
        }
    }

    // Tự động co giãn kích thước chữ (sp) và khoảng cách (dp) theo độ rộng màn hình thiết bị (baseline 365dp của bản PWA).
    // Trên các máy Android màn hình rộng (411dp - 448dp), chữ tự động lớn hơn ~10%-16% để cân đối với bố cục thẻ/ảnh,
    // đồng thời giới hạn an toàn fontScale hệ thống để không bao giờ bị vỡ khung giao diện.
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val baseDensity = androidx.compose.ui.platform.LocalDensity.current
    val smallestWidthDp = minOf(configuration.screenWidthDp, configuration.screenHeightDp).coerceAtLeast(320)
    val screenScale = (smallestWidthDp / 365f).coerceIn(0.95f, 1.18f)
    val safeSystemFontScale = baseDensity.fontScale.coerceIn(0.95f, 1.15f)
    val responsiveFontScale = (screenScale * safeSystemFontScale).coerceIn(1.02f, 1.24f)

    val responsiveDensity = androidx.compose.runtime.remember(baseDensity.density, responsiveFontScale) {
        androidx.compose.ui.unit.Density(
            density = baseDensity.density,
            fontScale = responsiveFontScale
        )
    }
    val responsiveDimensions = androidx.compose.runtime.remember(screenScale) {
        Dimensions.scaled(screenScale)
    }

    CompositionLocalProvider(
        androidx.compose.ui.platform.LocalDensity provides responsiveDensity,
        LocalSpacing provides responsiveDimensions
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}