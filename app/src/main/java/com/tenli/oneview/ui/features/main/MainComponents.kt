package com.tenli.oneview.ui.features.main

import android.graphics.BlurMaskFilter
import android.graphics.Paint as FrameworkPaint
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.tenli.oneview.R
import com.tenli.oneview.ui.theme.BrandActiveNavDark
import com.tenli.oneview.ui.theme.BrandContainerDark
import com.tenli.oneview.ui.theme.BrandLight
import com.tenli.oneview.ui.theme.BrandLighter
import com.tenli.oneview.ui.theme.BrandPrimary
import com.tenli.oneview.ui.theme.LucideIcons
import com.tenli.oneview.ui.theme.Slate300
import com.tenli.oneview.ui.theme.Slate500
import com.tenli.oneview.ui.theme.Slate600
import com.tenli.oneview.ui.theme.Slate900
import com.tenli.oneview.ui.theme.appColors
import com.tenli.oneview.ui.theme.caption
import com.tenli.oneview.ui.theme.spacing
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.roundToInt

// Exact cubic-bezier curve from tenli-oneview MobileLayout.vue & mobile.css:
// transition: transform 0.32s cubic-bezier(0.18, 0.9, 0.3, 1)
private val NavCapsuleGlideEasing = CubicBezierEasing(0.18f, 0.9f, 0.3f, 1f)
private const val NAV_CAPSULE_GLIDE_DURATION_MS = 320
private const val NAV_PILL_SCALE_DURATION_MS = 150
private const val NAV_CONTENT_ANIM_DURATION_MS = 180
private const val NAV_PILL_DRAG_SCALE = 1.05f
private const val NAV_ICON_DRAG_SCALE = 1.12f
private const val NAV_ICON_SELECTED_SCALE = 1.04f
/** Lực cản cao su khi kéo viên nang vượt mép (MobileLayout.vue lines 405-409) */
private const val RUBBER_BAND_EXPONENT = 0.75f

enum class CameraDetailTab {
    LIVE,
    PLAYBACK,
    EVENTS
}

private data class NavItem<T>(val tab: T, @StringRes val labelRes: Int, val icon: ImageVector)

// Tab order matching tenli-oneview MobileLayout.vue:
// 1. Trang chủ (Home) -> 2. Sự kiện (Event) -> 3. Trực tiếp (Monitor/Live) -> 4. Cài đặt (Setting)
private val MainNavItems = listOf(
    NavItem(MainTab.Home, R.string.tab_home, LucideIcons.Home),
    NavItem(MainTab.Event, R.string.tab_event, LucideIcons.Bell),
    NavItem(MainTab.Monitor, R.string.tab_monitor, LucideIcons.Video),
    NavItem(MainTab.Setting, R.string.tab_setting, LucideIcons.Settings)
)

// Menu bottom riêng cho màn hình chi tiết Camera:
// 1. Trực tiếp (Live) -> 2. Xem lại (Playback VMS) -> 3. Sự kiện (Events theo camera UUID)
private val CameraDetailNavItems = listOf(
    NavItem(CameraDetailTab.LIVE, R.string.tab_monitor, LucideIcons.Video),
    NavItem(CameraDetailTab.PLAYBACK, R.string.tab_playback, LucideIcons.History),
    NavItem(CameraDetailTab.EVENTS, R.string.tab_event, LucideIcons.Bell)
)

// ---------------------------------------------------------------------------------------------
// Bảng màu riêng của hiệu ứng Liquid Glass (đặc thù, không đổi theo token theme chung)
// ---------------------------------------------------------------------------------------------
private val DockGlassDarkTop = Color(0xF219263E)
private val DockGlassDarkMid = Color(0xF50E1729)
private val DockGlassDarkBottom = Color(0xF2142238)
private val DockGlassLightTop = Color(0xF7FFFFFF)
private val DockGlassLightMid = Color(0xEFF7F8FB)
private val DockGlassLightBottom = Color(0xF7FFFFFF)
private val GlassCausticSky = Color(0xFF38BDF8)
private val PillBlueDeep = Color(0xFF1D4ED8)
private val PillLightMid = Color(0xFFF4F8FF)
private val PillLightBottom = Color(0xFFE9F2FF)
private val PillLightBottomDragging = Color(0xFFDDEBFF)
private val PillLightSpecular = Color(0xFFE8F1FF)

// Kích thước hiệu ứng (px tính trong DrawScope – không phụ thuộc thang spacing)
private val DockAmbientShadowBlur = 18.dp
private val DockAmbientShadowOffsetY = 6.dp
private val DockContactShadowBlur = 5.dp
private val DockContactShadowOffsetY = 2.dp
private val DockAmbientShadowInset = 2.dp
private val DockContactShadowInset = 1.dp
private val DockRimBloomStroke = 5.dp
private val DockRimBloomBlur = 4.5.dp
private val DockInnerBevelStroke = 1.6.dp
private val DockInnerBevelInset = 2.1.dp
private val DockSpecularRimStroke = 1.65.dp
private val DockSpecularRimInset = 0.7.dp
private val DockOuterContourStroke = 0.65.dp
private val PillShadowBlur = 9.dp
private val PillShadowBlurDragging = 13.dp
private val PillShadowOffsetY = 2.5.dp
private val PillShadowOffsetYDragging = 4.dp
private val PillShadowInset = 1.dp
private val PillRimBloomStroke = 3.5.dp
private val PillRimBloomBlur = 3.dp
private val PillInnerSpecularStroke = 1.4.dp
private val PillInnerSpecularInset = 0.65.dp
private val PillOuterContourStroke = 0.85.dp

/**
 * Custom modifier vẽ hiệu ứng "iPhone iOS 26 Liquid Glass" cho thanh Dock chính:
 * - Bóng đổ (Drop Shadow) vẽ tách biệt bên ngoài viên nang (ClipOp.Difference) để không làm đục mặt kính bên trong.
 * - Thân kính lỏng đa lớp: Light Mode thiên về trắng trong trẻo (#F7F8FB -> #FFFFFF) kết hợp vầng lóa viền
 *   quang học (Optical Rim Glare / Specular Rim Bloom) chuẩn iOS Liquid Glass; Dark Mode dùng Deep Obsidian Glass.
 * - Lớp phản chiếu mặt kính cong phía trên (Top Curved Lens Sheen) & khúc xạ đáy (Bottom Bounce).
 * - Viền khúc xạ ánh sáng 3D (Optical Rim Bloom + Diagonal Specular Rim + Inner Bevel + Outer Micro-Contour).
 *
 * Toàn bộ Path/Brush/Paint được tạo trong drawWithCache => chỉ tạo lại khi đổi kích thước hoặc theme.
 */
private fun Modifier.iosLiquidGlassDock(isDark: Boolean): Modifier = this.drawWithCache {
    val cornerRadiusPx = size.height / 2f
    val cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)

    val capsulePath = Path().apply {
        addRoundRect(
            RoundRect(
                rect = Rect(Offset.Zero, size),
                cornerRadius = cornerRadius
            )
        )
    }

    // 1. Paint bóng đổ ngoài (Ambient + Contact Shadow)
    val ambientDyPx = DockAmbientShadowOffsetY.toPx()
    val ambientShadowColor = if (isDark) {
        Color.Black.copy(alpha = 0.62f)
    } else {
        Slate900.copy(alpha = 0.13f)
    }
    val ambientShadowPaint = FrameworkPaint(FrameworkPaint.ANTI_ALIAS_FLAG).apply {
        color = ambientShadowColor.toArgb()
        maskFilter = BlurMaskFilter(DockAmbientShadowBlur.toPx(), BlurMaskFilter.Blur.NORMAL)
    }

    val contactDyPx = DockContactShadowOffsetY.toPx()
    val contactShadowColor = if (isDark) {
        Color.Black.copy(alpha = 0.42f)
    } else {
        Slate900.copy(alpha = 0.07f)
    }
    val contactShadowPaint = FrameworkPaint(FrameworkPaint.ANTI_ALIAS_FLAG).apply {
        color = contactShadowColor.toArgb()
        maskFilter = BlurMaskFilter(DockContactShadowBlur.toPx(), BlurMaskFilter.Blur.NORMAL)
    }
    val ambientInsetPx = DockAmbientShadowInset.toPx()
    val contactInsetPx = DockContactShadowInset.toPx()

    // 2. Thân kính lỏng (Liquid Glass Body)
    // Light Mode dùng nền trắng kính trong trẻo, không bị ám xám xanh
    val baseGlassBrush = if (isDark) {
        Brush.verticalGradient(
            0.0f to DockGlassDarkTop,
            0.48f to DockGlassDarkMid,
            1.0f to DockGlassDarkBottom
        )
    } else {
        Brush.verticalGradient(
            0.0f to DockGlassLightTop,
            0.52f to DockGlassLightMid,
            1.0f to DockGlassLightBottom
        )
    }

    // Lớp khúc xạ ánh sáng chéo (Diagonal Caustic Refraction giống iOS 26 Dock)
    val diagonalRefractionBrush = Brush.linearGradient(
        colors = if (isDark) {
            listOf(
                Color.White.copy(alpha = 0.10f),
                Color.Transparent,
                GlassCausticSky.copy(alpha = 0.08f)
            )
        } else {
            listOf(
                Color.White.copy(alpha = 0.92f),
                Color(0xFFF8FAFC).copy(alpha = 0.35f),
                Color(0xFFF3F6FA).copy(alpha = 0.28f),
                Color.White.copy(alpha = 0.95f)
            )
        },
        start = Offset.Zero,
        end = Offset(size.width, size.height)
    )

    // 3. Độ bóng vòm kính phía trên (Top Curved Lens Specular Sheen)
    val topLensSheenBrush = Brush.verticalGradient(
        colors = if (isDark) {
            listOf(
                Color.White.copy(alpha = 0.18f),
                Color.White.copy(alpha = 0.05f),
                Color.Transparent
            )
        } else {
            listOf(
                Color.White.copy(alpha = 0.95f),
                Color.White.copy(alpha = 0.45f),
                Color.Transparent
            )
        },
        startY = 0f,
        endY = size.height * 0.46f
    )

    // Ánh sáng dội ngược dưới đáy kính (Bottom Internal Bounce)
    val bottomBounceBrush = Brush.verticalGradient(
        colors = if (isDark) {
            listOf(Color.Transparent, BrandLight.copy(alpha = 0.10f))
        } else {
            listOf(Color.Transparent, Color.White.copy(alpha = 0.88f))
        },
        startY = size.height * 0.66f,
        endY = size.height
    )

    // Vầng lóa viền quang học bên trong mép kính (iOS Liquid Glass Optical Rim Bloom)
    val rimBloomStrokePx = DockRimBloomStroke.toPx()
    val rimBloomPaint = FrameworkPaint(FrameworkPaint.ANTI_ALIAS_FLAG).apply {
        style = FrameworkPaint.Style.STROKE
        strokeWidth = rimBloomStrokePx
        color = (if (isDark) Color.White.copy(alpha = 0.22f) else Color.White).toArgb()
        maskFilter = BlurMaskFilter(DockRimBloomBlur.toPx(), BlurMaskFilter.Blur.NORMAL)
    }

    // 4. Viền khúc xạ thấu kính bên trong (Inner Curved Lens Refraction Bevel)
    val innerBevelStrokePx = DockInnerBevelStroke.toPx()
    val innerBevelInsetPx = DockInnerBevelInset.toPx()
    val innerBevelBrush = Brush.verticalGradient(
        colors = if (isDark) {
            listOf(
                Color.White.copy(alpha = 0.16f),
                Color.Black.copy(alpha = 0.35f),
                Color.White.copy(alpha = 0.12f)
            )
        } else {
            listOf(
                Color.White.copy(alpha = 0.95f),
                Slate300.copy(alpha = 0.34f),
                Color.White.copy(alpha = 0.92f)
            )
        }
    )

    // 5. Viền lóa sáng phản quang kim cương 3D (Signature iOS 26 Diagonal Specular Rim Glare)
    val specularRimStrokePx = DockSpecularRimStroke.toPx()
    val specularRimInsetPx = DockSpecularRimInset.toPx()
    val specularRimBrush = Brush.linearGradient(
        colors = if (isDark) {
            listOf(
                Color.White.copy(alpha = 0.52f),
                Color.White.copy(alpha = 0.18f),
                Color.White.copy(alpha = 0.08f),
                BrandLighter.copy(alpha = 0.26f),
                Color.White.copy(alpha = 0.45f)
            )
        } else {
            listOf(
                Color.White,
                Color.White.copy(alpha = 0.96f),
                Color(0xFFEFF6FF).copy(alpha = 0.85f),
                Color.White.copy(alpha = 0.96f),
                Color.White
            )
        },
        start = Offset.Zero,
        end = Offset(size.width, size.height)
    )

    // 6. Viền định hình siêu mảnh ngoài cùng (Outer Micro-Contour giữ nét viền lóa trên nền trắng)
    val outerContourStrokePx = DockOuterContourStroke.toPx()
    val outerContourColor = if (isDark) {
        Color.Black.copy(alpha = 0.60f)
    } else {
        Slate500.copy(alpha = 0.13f)
    }

    // Hình học tính sẵn (không cấp phát trong onDrawBehind)
    val topSheenSize = Size(size.width, size.height * 0.46f)
    val bottomBounceTopLeft = Offset(0f, size.height * 0.66f)
    val bottomBounceSize = Size(size.width, size.height * 0.34f)
    val innerBevelTopLeft = Offset(innerBevelInsetPx, innerBevelInsetPx)
    val innerBevelSize = Size(size.width - innerBevelInsetPx * 2f, size.height - innerBevelInsetPx * 2f)
    val innerBevelRadius = (cornerRadiusPx - innerBevelInsetPx).coerceAtLeast(0f)
    val innerBevelStroke = Stroke(width = innerBevelStrokePx)
    val specularTopLeft = Offset(specularRimInsetPx, specularRimInsetPx)
    val specularSize = Size(size.width - specularRimInsetPx * 2f, size.height - specularRimInsetPx * 2f)
    val specularRadius = (cornerRadiusPx - specularRimInsetPx).coerceAtLeast(0f)
    val specularStroke = Stroke(width = specularRimStrokePx)
    val contourTopLeft = Offset(outerContourStrokePx / 2f, outerContourStrokePx / 2f)
    val contourSize = Size(size.width - outerContourStrokePx, size.height - outerContourStrokePx)
    val contourStroke = Stroke(width = outerContourStrokePx)

    onDrawBehind {
        // Vẽ bóng đổ bên ngoài viên nang (không bị lọt vào bên trong lớp kính)
        clipPath(capsulePath, clipOp = ClipOp.Difference) {
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawRoundRect(
                    ambientInsetPx,
                    ambientDyPx,
                    size.width - ambientInsetPx,
                    size.height + ambientDyPx,
                    cornerRadiusPx,
                    cornerRadiusPx,
                    ambientShadowPaint
                )
                canvas.nativeCanvas.drawRoundRect(
                    contactInsetPx,
                    contactDyPx,
                    size.width - contactInsetPx,
                    size.height + contactDyPx,
                    cornerRadiusPx,
                    cornerRadiusPx,
                    contactShadowPaint
                )
            }
        }

        // Vẽ các lớp kính lỏng + vầng lóa viền bên trong viên nang
        clipPath(capsulePath) {
            drawRoundRect(brush = baseGlassBrush, cornerRadius = cornerRadius)
            drawRoundRect(brush = diagonalRefractionBrush, cornerRadius = cornerRadius)
            drawRect(brush = topLensSheenBrush, topLeft = Offset.Zero, size = topSheenSize)
            drawRect(brush = bottomBounceBrush, topLeft = bottomBounceTopLeft, size = bottomBounceSize)
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawRoundRect(
                    0f,
                    0f,
                    size.width,
                    size.height,
                    cornerRadiusPx,
                    cornerRadiusPx,
                    rimBloomPaint
                )
            }
        }

        // Vẽ viền khúc xạ thấu kính 3D bên trong (Inner Bevel)
        drawRoundRect(
            brush = innerBevelBrush,
            topLeft = innerBevelTopLeft,
            size = innerBevelSize,
            cornerRadius = CornerRadius(innerBevelRadius, innerBevelRadius),
            style = innerBevelStroke
        )

        // Vẽ viền lóa sáng phản quang 3D (Specular Rim)
        drawRoundRect(
            brush = specularRimBrush,
            topLeft = specularTopLeft,
            size = specularSize,
            cornerRadius = CornerRadius(specularRadius, specularRadius),
            style = specularStroke
        )

        // Vẽ viền định hình ngoài cùng (Outer Contour Hairline)
        drawRoundRect(
            color = outerContourColor,
            topLeft = contourTopLeft,
            size = contourSize,
            cornerRadius = cornerRadius,
            style = contourStroke
        )
    }
}

/**
 * Custom modifier vẽ viên nang kính lỏng cho Tab đang chọn (Sliding Active Liquid Glass Capsule):
 * - Nổi khối 3D rõ rệt trên nền kính trắng của Dock với vầng lóa viền sáng (Optical Rim Glare).
 * - Có bóng đổ dịu bên ngoài viên nang, mặt kính cong phản quang phía trên và viền sáng 3D.
 */
private fun Modifier.iosLiquidGlassActivePill(
    isDark: Boolean,
    isDragging: Boolean
): Modifier = this.drawWithCache {
    val cornerRadiusPx = size.height / 2f
    val cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)

    val pillPath = Path().apply {
        addRoundRect(
            RoundRect(
                rect = Rect(Offset.Zero, size),
                cornerRadius = cornerRadius
            )
        )
    }

    val shadowBlurPx = (if (isDragging) PillShadowBlurDragging else PillShadowBlur).toPx()
    val shadowDyPx = (if (isDragging) PillShadowOffsetYDragging else PillShadowOffsetY).toPx()
    val shadowInsetPx = PillShadowInset.toPx()
    val pillShadowColor = if (isDark) {
        if (isDragging) BrandPrimary.copy(alpha = 0.42f) else Color.Black.copy(alpha = 0.48f)
    } else {
        if (isDragging) BrandPrimary.copy(alpha = 0.22f) else BrandContainerDark.copy(alpha = 0.14f)
    }
    val pillShadowPaint = FrameworkPaint(FrameworkPaint.ANTI_ALIAS_FLAG).apply {
        color = pillShadowColor.toArgb()
        maskFilter = BlurMaskFilter(shadowBlurPx, BlurMaskFilter.Blur.NORMAL)
    }

    val pillRimBloomPaint = FrameworkPaint(FrameworkPaint.ANTI_ALIAS_FLAG).apply {
        style = FrameworkPaint.Style.STROKE
        strokeWidth = PillRimBloomStroke.toPx()
        color = (if (isDark) Color.White.copy(alpha = 0.25f) else Color.White).toArgb()
        maskFilter = BlurMaskFilter(PillRimBloomBlur.toPx(), BlurMaskFilter.Blur.NORMAL)
    }

    val pillBodyBrush = if (isDark) {
        Brush.verticalGradient(
            0.0f to BrandActiveNavDark.copy(alpha = if (isDragging) 0.44f else 0.32f),
            0.50f to BrandPrimary.copy(alpha = if (isDragging) 0.28f else 0.18f),
            1.0f to PillBlueDeep.copy(alpha = if (isDragging) 0.36f else 0.25f)
        )
    } else {
        Brush.verticalGradient(
            0.0f to Color.White,
            0.52f to PillLightMid,
            1.0f to if (isDragging) PillLightBottomDragging else PillLightBottom
        )
    }

    val pillTopSheenBrush = Brush.verticalGradient(
        colors = if (isDark) {
            listOf(
                Color.White.copy(alpha = if (isDragging) 0.28f else 0.20f),
                Color.Transparent
            )
        } else {
            listOf(
                Color.White,
                Color.White.copy(alpha = 0.0f)
            )
        },
        startY = 0f,
        endY = size.height * 0.50f
    )

    val innerSpecularStrokePx = PillInnerSpecularStroke.toPx()
    val innerSpecularInsetPx = PillInnerSpecularInset.toPx()
    val innerSpecularBrush = Brush.linearGradient(
        colors = if (isDark) {
            listOf(
                Color.White.copy(alpha = 0.50f),
                BrandActiveNavDark.copy(alpha = 0.25f),
                BrandLighter.copy(alpha = 0.45f)
            )
        } else {
            listOf(
                Color.White,
                PillLightSpecular,
                Color.White
            )
        },
        start = Offset.Zero,
        end = Offset(size.width, size.height)
    )

    val outerPillContourPx = PillOuterContourStroke.toPx()
    val outerPillContourColor = if (isDark) {
        BrandActiveNavDark.copy(alpha = if (isDragging) 0.55f else 0.35f)
    } else {
        BrandPrimary.copy(alpha = if (isDragging) 0.26f else 0.15f)
    }

    // Hình học tính sẵn (không cấp phát trong onDrawBehind)
    val topSheenSize = Size(size.width, size.height * 0.50f)
    val specularTopLeft = Offset(innerSpecularInsetPx, innerSpecularInsetPx)
    val specularSize = Size(size.width - innerSpecularInsetPx * 2f, size.height - innerSpecularInsetPx * 2f)
    val specularRadius = (cornerRadiusPx - innerSpecularInsetPx).coerceAtLeast(0f)
    val specularStroke = Stroke(width = innerSpecularStrokePx)
    val contourTopLeft = Offset(outerPillContourPx / 2f, outerPillContourPx / 2f)
    val contourSize = Size(size.width - outerPillContourPx, size.height - outerPillContourPx)
    val contourStroke = Stroke(width = outerPillContourPx)

    onDrawBehind {
        // Bóng đổ bên ngoài viên nang đang chọn
        clipPath(pillPath, clipOp = ClipOp.Difference) {
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawRoundRect(
                    shadowInsetPx,
                    shadowDyPx,
                    size.width - shadowInsetPx,
                    size.height + shadowDyPx,
                    cornerRadiusPx,
                    cornerRadiusPx,
                    pillShadowPaint
                )
            }
        }

        // Thân kính + vầng lóa viền của viên nang đang chọn
        clipPath(pillPath) {
            drawRoundRect(brush = pillBodyBrush, cornerRadius = cornerRadius)
            drawRect(brush = pillTopSheenBrush, topLeft = Offset.Zero, size = topSheenSize)
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawRoundRect(
                    0f,
                    0f,
                    size.width,
                    size.height,
                    cornerRadiusPx,
                    cornerRadiusPx,
                    pillRimBloomPaint
                )
            }
        }

        // Viền phản quang 3D bên trong viên nang
        drawRoundRect(
            brush = innerSpecularBrush,
            topLeft = specularTopLeft,
            size = specularSize,
            cornerRadius = CornerRadius(specularRadius, specularRadius),
            style = specularStroke
        )

        // Viền định hình mảnh bên ngoài viên nang
        drawRoundRect(
            color = outerPillContourColor,
            topLeft = contourTopLeft,
            size = contourSize,
            cornerRadius = cornerRadius,
            style = contourStroke
        )
    }
}

/** Lực cản cao su khi kéo viên nang vượt ra ngoài [0, maxLeftPx] */
private fun rubberBand(rawLeft: Float, maxLeftPx: Float): Float = when {
    rawLeft < 0f -> -(-rawLeft).pow(RUBBER_BAND_EXPONENT)
    rawLeft > maxLeftPx -> maxLeftPx + (rawLeft - maxLeftPx).pow(RUBBER_BAND_EXPONENT)
    else -> rawLeft
}

private fun hoverIndexOf(leftPx: Float, tabWidthPx: Float, itemCount: Int): Int =
    ((leftPx + tabWidthPx / 2f) / tabWidthPx).toInt().coerceIn(0, (itemCount - 1).coerceAtLeast(0))

@Composable
fun MainBottomNavigation(
    currentTab: MainTab,
    onTabClick: (MainTab) -> Unit,
    onScrollToTop: (MainTab) -> Unit,
    visibleTabs: List<MainTab>? = null
) {
    val filteredItems = remember(visibleTabs) {
        if (visibleTabs == null) MainNavItems
        else MainNavItems.filter { it.tab in visibleTabs }
    }
    if (filteredItems.isEmpty()) return
    LiquidGlassBottomNavigation(
        items = filteredItems,
        currentTab = currentTab,
        onTabClick = onTabClick,
        onScrollToTop = onScrollToTop
    )
}

@Composable
fun CameraDetailBottomNavigation(
    currentTab: CameraDetailTab,
    onTabClick: (CameraDetailTab) -> Unit,
    onScrollToTop: (CameraDetailTab) -> Unit = {},
    visibleTabs: List<CameraDetailTab>? = null
) {
    val filteredItems = remember(visibleTabs) {
        if (visibleTabs == null) CameraDetailNavItems
        else CameraDetailNavItems.filter { it.tab in visibleTabs }
    }
    if (filteredItems.isEmpty()) return
    LiquidGlassBottomNavigation(
        items = filteredItems,
        currentTab = currentTab,
        onTabClick = onTabClick,
        onScrollToTop = onScrollToTop
    )
}

@Composable
private fun <T> LiquidGlassBottomNavigation(
    items: List<NavItem<T>>,
    currentTab: T,
    onTabClick: (T) -> Unit,
    onScrollToTop: (T) -> Unit
) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val isDark = colors.isDark
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current

    // Active / Inactive text & icon colors với độ tương phản sắc nét cho cả Light và Dark theme
    val activeContentColor = colors.activeContent
    val inactiveContentColor = if (isDark) Slate300 else Slate600

    val selectedIndex = items.indexOfFirst { it.tab == currentTab }.coerceAtLeast(0)
    val scope = rememberCoroutineScope()

    // Đọc giá trị mới nhất trong gesture detector mà không phải khởi động lại pointerInput
    val itemsState by rememberUpdatedState(items)
    val currentTabState by rememberUpdatedState(currentTab)
    val onTabClickState by rememberUpdatedState(onTabClick)
    val onScrollToTopState by rememberUpdatedState(onScrollToTop)

    var isDraggingNav by remember { mutableStateOf(false) }
    var rawDragLeftPx by remember { mutableFloatStateOf(0f) }
    var dragHoverIndex by remember { mutableStateOf<Int?>(null) }

    val iconSize = spacing.iconRegular + spacing.space2
    val selectedIconOffset = -spacing.space1
    val dragIconOffset = -spacing.space2

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = spacing.space14, vertical = spacing.space10),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier
                // Trên tablet/foldable giữ Dock gọn ở giữa thay vì kéo dài toàn màn hình
                .widthIn(max = spacing.contentMaxWidth)
                .fillMaxWidth()
                .height(spacing.bottomNavHeight)
                .iosLiquidGlassDock(isDark = isDark)
                .padding(spacing.space5)
        ) {
            val tabWidth = maxWidth / items.size
            val tabWidthPx = with(density) { tabWidth.toPx() }
            val maxLeftPx = tabWidthPx * (items.size - 1)

            // Unified pixel Animatable matching MobileLayout.vue:
            // transition: transform 0.32s cubic-bezier(0.18, 0.9, 0.3, 1)
            val indicatorLeftAnim = remember { Animatable(selectedIndex * tabWidthPx) }

            LaunchedEffect(selectedIndex, tabWidthPx, isDraggingNav) {
                if (!isDraggingNav && tabWidthPx > 0f) {
                    indicatorLeftAnim.animateTo(
                        targetValue = selectedIndex * tabWidthPx,
                        animationSpec = tween(
                            durationMillis = NAV_CAPSULE_GLIDE_DURATION_MS,
                            easing = NavCapsuleGlideEasing
                        )
                    )
                }
            }

            val pillScale by animateFloatAsState(
                targetValue = if (isDraggingNav) NAV_PILL_DRAG_SCALE else 1f,
                animationSpec = tween(durationMillis = NAV_PILL_SCALE_DURATION_MS),
                label = "pillScale"
            )

            // Sliding Liquid Glass Capsule Indicator
            // graphicsLayer: viên nang có layer riêng => di chuyển/scale không vẽ lại Dock (blur shadow),
            // và giá trị animation chỉ được đọc ở pha layout/draw (không recompose mỗi frame).
            Box(
                modifier = Modifier
                    .offset { IntOffset(indicatorLeftAnim.value.roundToInt(), 0) }
                    .width(tabWidth)
                    .fillMaxHeight()
                    .graphicsLayer {
                        scaleX = pillScale
                        scaleY = pillScale
                    }
                    .iosLiquidGlassActivePill(
                        isDark = isDark,
                        isDragging = isDraggingNav
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    // Layer riêng cho icon/nhãn: animation màu/scale của tab không làm vẽ lại Dock
                    .graphicsLayer()
                    .pointerInput(tabWidthPx, items.size) {
                        detectHorizontalDragGestures(
                            onDragStart = { startOffset ->
                                isDraggingNav = true
                                val initialLeft = startOffset.x - tabWidthPx / 2f
                                rawDragLeftPx = initialLeft
                                val dampedLeft = rubberBand(initialLeft, maxLeftPx)
                                scope.launch { indicatorLeftAnim.snapTo(dampedLeft) }
                                dragHoverIndex = hoverIndexOf(dampedLeft, tabWidthPx, items.size)
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                val nextRaw = rawDragLeftPx + dragAmount
                                rawDragLeftPx = nextRaw
                                val dampedLeft = rubberBand(nextRaw, maxLeftPx)
                                scope.launch { indicatorLeftAnim.snapTo(dampedLeft) }
                                val idx = hoverIndexOf(dampedLeft, tabWidthPx, items.size)
                                if (dragHoverIndex != idx) {
                                    dragHoverIndex = idx
                                    try {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    } catch (_: Exception) {
                                    }
                                }
                            },
                            onDragEnd = {
                                val targetIdx = dragHoverIndex
                                isDraggingNav = false
                                dragHoverIndex = null
                                val currentItems = itemsState
                                if (targetIdx != null && targetIdx in currentItems.indices) {
                                    val targetTab = currentItems[targetIdx].tab
                                    if (targetTab == currentTabState) {
                                        onScrollToTopState(targetTab)
                                    } else {
                                        onTabClickState(targetTab)
                                    }
                                }
                            },
                            onDragCancel = {
                                isDraggingNav = false
                                dragHoverIndex = null
                            }
                        )
                    },
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = if (isDraggingNav && dragHoverIndex != null) {
                        dragHoverIndex == index
                    } else {
                        currentTab == item.tab
                    }
                    val isDragTarget = isDraggingNav && dragHoverIndex == index
                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) activeContentColor else inactiveContentColor,
                        animationSpec = tween(durationMillis = NAV_CONTENT_ANIM_DURATION_MS),
                        label = "tabColor"
                    )
                    val iconScale by animateFloatAsState(
                        targetValue = when {
                            isDragTarget -> NAV_ICON_DRAG_SCALE
                            isSelected -> NAV_ICON_SELECTED_SCALE
                            else -> 1f
                        },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "iconScale"
                    )
                    val iconOffsetY by animateDpAsState(
                        targetValue = when {
                            isDragTarget -> dragIconOffset
                            isSelected -> selectedIconOffset
                            else -> 0.dp
                        },
                        animationSpec = tween(durationMillis = NAV_CONTENT_ANIM_DURATION_MS),
                        label = "iconOffsetY"
                    )
                    val label = stringResource(id = item.labelRes)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(spacing.radiusExtraLarge))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (currentTab == item.tab) {
                                    onScrollToTop(item.tab)
                                } else {
                                    onTabClick(item.tab)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = label,
                                tint = contentColor,
                                modifier = Modifier
                                    // Đọc animation trong graphicsLayer => chỉ vẽ lại, không recompose
                                    .graphicsLayer {
                                        translationY = iconOffsetY.toPx()
                                        scaleX = iconScale
                                        scaleY = iconScale
                                    }
                                    .size(iconSize)
                            )
                            Spacer(modifier = Modifier.height(spacing.space2))
                            Text(
                                text = label,
                                color = contentColor,
                                style = MaterialTheme.typography.caption,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
