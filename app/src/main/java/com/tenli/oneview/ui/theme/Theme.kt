package com.tenli.oneview.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val LightColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = BrandOnPrimary,
    primaryContainer = BrandContainer,
    onPrimaryContainer = BrandContainerDark,

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

private val DarkColorScheme = darkColorScheme(
    primary = BrandActiveNavDark,
    onPrimary = BrandOnPrimary,
    primaryContainer = BrandContainerDark,
    onPrimaryContainer = BrandContainer,

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

/** Kích thước dùng chung (spacing, icon, radius, component size) – đã co giãn theo màn hình */
val MaterialTheme.spacing: Dimensions
    @Composable
    @ReadOnlyComposable
    get() = LocalSpacing.current

/** Màu ngữ nghĩa mở rộng – tự đổi theo Light/Dark */
val MaterialTheme.appColors: AppColors
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current

/** Baseline độ rộng thiết kế (bản PWA mobile) */
private const val DESIGN_BASELINE_WIDTH_DP = 365f
private const val MIN_SCREEN_WIDTH_DP = 320
private const val MIN_SCREEN_SCALE = 0.95f
private const val MAX_SCREEN_SCALE = 1.18f
private const val MIN_SYSTEM_FONT_SCALE = 0.95f
private const val MAX_SYSTEM_FONT_SCALE = 1.15f
private const val MIN_FONT_SCALE = 1.02f
private const val MAX_FONT_SCALE = 1.24f

@Composable
fun TenliAIoTTheme(content: @Composable () -> Unit) {
    val themeMode by ThemeManager.themeModeFlow.collectAsStateWithLifecycle()
    val isSystemDark = isSystemInDarkTheme()

    val useDarkTheme = when (themeMode) {
        ThemeManager.MODE_LIGHT -> false
        ThemeManager.MODE_DARK -> true
        else -> isSystemDark
    }

    val colorScheme = if (useDarkTheme) DarkColorScheme else LightColorScheme
    val appColors = if (useDarkTheme) DarkAppColors else LightAppColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !useDarkTheme
                insetsController.isAppearanceLightNavigationBars = !useDarkTheme
            }
        }
    }

    // Tự động co giãn kích thước chữ (sp) và khoảng cách (dp) theo độ rộng màn hình thiết bị (baseline 365dp của bản PWA).
    // Trên các máy Android màn hình rộng (411dp - 448dp), chữ tự động lớn hơn ~10%-16% để cân đối với bố cục thẻ/ảnh,
    // đồng thời giới hạn an toàn fontScale hệ thống để không bao giờ bị vỡ khung giao diện.
    val configuration = LocalConfiguration.current
    val baseDensity = LocalDensity.current
    val smallestWidthDp = minOf(configuration.screenWidthDp, configuration.screenHeightDp)
        .coerceAtLeast(MIN_SCREEN_WIDTH_DP)
    val screenScale = (smallestWidthDp / DESIGN_BASELINE_WIDTH_DP).coerceIn(MIN_SCREEN_SCALE, MAX_SCREEN_SCALE)
    val safeSystemFontScale = baseDensity.fontScale.coerceIn(MIN_SYSTEM_FONT_SCALE, MAX_SYSTEM_FONT_SCALE)
    val responsiveFontScale = (screenScale * safeSystemFontScale).coerceIn(MIN_FONT_SCALE, MAX_FONT_SCALE)

    val responsiveDensity = remember(baseDensity.density, responsiveFontScale) {
        Density(density = baseDensity.density, fontScale = responsiveFontScale)
    }
    val responsiveDimensions = remember(screenScale) { Dimensions.scaled(screenScale) }

    CompositionLocalProvider(
        LocalDensity provides responsiveDensity,
        LocalSpacing provides responsiveDimensions,
        LocalAppColors provides appColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
