package com.tenli.oneview.ui.features.main

import android.graphics.BlurMaskFilter
import android.graphics.Paint as FrameworkPaint
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
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tenli.oneview.R
import com.tenli.oneview.ui.theme.BrandActiveNavDark
import com.tenli.oneview.ui.theme.BrandPrimary
import com.tenli.oneview.ui.theme.LucideIcons
import com.tenli.oneview.ui.theme.ThemeManager
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.roundToInt

// Exact cubic-bezier curve from tenli-oneview MobileLayout.vue & mobile.css:
// transition: transform 0.32s cubic-bezier(0.18, 0.9, 0.3, 1)
private val NavCapsuleGlideEasing = CubicBezierEasing(0.18f, 0.9f, 0.3f, 1f)

/**
 * Custom modifier vẽ hiệu ứng "iPhone iOS 26 Liquid Glass" cho thanh Dock chính:
 * - Bóng đổ (Drop Shadow) vẽ tách biệt bên ngoài viên nang (ClipOp.Difference) để không làm đục mặt kính bên trong.
 * - Thân kính lỏng đa lớp (Smoky/Pearlescent Frosted Glass ở Light Mode & Deep Obsidian Glass ở Dark Mode)
 *   giúp nổi bật rõ ràng ngay cả khi đè lên thẻ sự kiện màu trắng (#FFFFFF).
 * - Lớp phản chiếu mặt kính cong phía trên (Top Curved Lens Sheen) & khúc xạ đáy (Bottom Bounce).
 * - Viền khúc xạ ánh sáng 3D (Diagonal Specular Rim + Inner Bevel + Outer Micro-Contour).
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
    val ambientBlurPx = 18.dp.toPx()
    val ambientDyPx = 7.dp.toPx()
    val ambientShadowColor = if (isDark) {
        Color.Black.copy(alpha = 0.62f)
    } else {
        Color(0xFF0F172A).copy(alpha = 0.18f)
    }
    val ambientShadowPaint = FrameworkPaint(FrameworkPaint.ANTI_ALIAS_FLAG).apply {
        color = ambientShadowColor.toArgb()
        maskFilter = BlurMaskFilter(ambientBlurPx, BlurMaskFilter.Blur.NORMAL)
    }

    val contactBlurPx = 5.dp.toPx()
    val contactDyPx = 2.dp.toPx()
    val contactShadowColor = if (isDark) {
        Color.Black.copy(alpha = 0.42f)
    } else {
        Color(0xFF0F172A).copy(alpha = 0.10f)
    }
    val contactShadowPaint = FrameworkPaint(FrameworkPaint.ANTI_ALIAS_FLAG).apply {
        color = contactShadowColor.toArgb()
        maskFilter = BlurMaskFilter(contactBlurPx, BlurMaskFilter.Blur.NORMAL)
    }

    // 2. Thân kính lỏng (Liquid Glass Body)
    // Light Mode dùng tông kính khói ngọc trai (Smoky Ice-Slate Glass) để tách biệt rõ với thẻ trắng #FFFFFF
    val baseGlassBrush = if (isDark) {
        Brush.verticalGradient(
            0.0f to Color(0xF219263E),
            0.48f to Color(0xF50E1729),
            1.0f to Color(0xF2142238)
        )
    } else {
        Brush.verticalGradient(
            0.0f to Color(0xF6E3EAF5),
            0.50f to Color(0xF6D5DFEE),
            1.0f to Color(0xF6DFE7F4)
        )
    }

    // Lớp khúc xạ ánh sáng chéo (Diagonal Caustic Refraction giống iOS 26 Dock)
    val diagonalRefractionBrush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.10f),
                Color.Transparent,
                Color(0xFF38BDF8).copy(alpha = 0.08f)
            ),
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.42f),
                Color(0xFF94A3B8).copy(alpha = 0.14f),
                Color(0xFFCBD5E1).copy(alpha = 0.10f),
                Color.White.copy(alpha = 0.48f)
            ),
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height)
        )
    }

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
                Color.White.copy(alpha = 0.72f),
                Color.White.copy(alpha = 0.25f),
                Color.Transparent
            )
        },
        startY = 0f,
        endY = size.height * 0.48f
    )

    // Ánh sáng dội ngược dưới đáy kính (Bottom Internal Bounce)
    val bottomBounceBrush = Brush.verticalGradient(
        colors = if (isDark) {
            listOf(
                Color.Transparent,
                Color(0xFF60A5FA).copy(alpha = 0.10f)
            )
        } else {
            listOf(
                Color.Transparent,
                Color.White.copy(alpha = 0.55f)
            )
        },
        startY = size.height * 0.68f,
        endY = size.height
    )

    // 4. Viền dày khối kính 3D bên trong (Inner 3D Bevel Thickness)
    val innerBevelStrokePx = 2.dp.toPx()
    val innerBevelInsetPx = 1.6.dp.toPx()
    val innerBevelBrush = Brush.verticalGradient(
        colors = if (isDark) {
            listOf(
                Color.White.copy(alpha = 0.16f),
                Color.Black.copy(alpha = 0.35f),
                Color.White.copy(alpha = 0.12f)
            )
        } else {
            listOf(
                Color.White.copy(alpha = 0.85f),
                Color(0xFF64748B).copy(alpha = 0.22f),
                Color.White.copy(alpha = 0.78f)
            )
        }
    )

    // 5. Viền phản quang kim cương 3D (Signature iOS 26 Diagonal Specular Rim)
    val specularRimStrokePx = 1.35.dp.toPx()
    val specularRimInsetPx = 0.75.dp.toPx()
    val specularRimBrush = Brush.linearGradient(
        colors = if (isDark) {
            listOf(
                Color.White.copy(alpha = 0.52f),
                Color.White.copy(alpha = 0.18f),
                Color.White.copy(alpha = 0.08f),
                Color(0xFF93C5FD).copy(alpha = 0.26f),
                Color.White.copy(alpha = 0.45f)
            )
        } else {
            listOf(
                Color.White,
                Color.White.copy(alpha = 0.88f),
                Color(0xFFF8FAFC).copy(alpha = 0.60f),
                Color.White.copy(alpha = 0.88f),
                Color.White
            )
        },
        start = Offset(0f, 0f),
        end = Offset(size.width, size.height)
    )

    // 6. Viền định hình siêu mảnh ngoài cùng (Outer Micro-Contour giúp tách nền trắng 100%)
    val outerContourStrokePx = 0.85.dp.toPx()
    val outerContourColor = if (isDark) {
        Color.Black.copy(alpha = 0.60f)
    } else {
        Color(0xFF0F172A).copy(alpha = 0.15f)
    }

    onDrawBehind {
        // Vẽ bóng đổ bên ngoài viên nang (không bị lọt vào bên trong lớp kính)
        clipPath(capsulePath, clipOp = ClipOp.Difference) {
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawRoundRect(
                    2.dp.toPx(),
                    ambientDyPx,
                    size.width - 2.dp.toPx(),
                    size.height + ambientDyPx,
                    cornerRadiusPx,
                    cornerRadiusPx,
                    ambientShadowPaint
                )
                canvas.nativeCanvas.drawRoundRect(
                    1.dp.toPx(),
                    contactDyPx,
                    size.width - 1.dp.toPx(),
                    size.height + contactDyPx,
                    cornerRadiusPx,
                    cornerRadiusPx,
                    contactShadowPaint
                )
            }
        }

        // Vẽ các lớp kính lỏng bên trong viên nang
        clipPath(capsulePath) {
            drawRoundRect(
                brush = baseGlassBrush,
                cornerRadius = cornerRadius
            )
            drawRoundRect(
                brush = diagonalRefractionBrush,
                cornerRadius = cornerRadius
            )
            drawRect(
                brush = topLensSheenBrush,
                topLeft = Offset.Zero,
                size = Size(size.width, size.height * 0.48f)
            )
            drawRect(
                brush = bottomBounceBrush,
                topLeft = Offset(0f, size.height * 0.68f),
                size = Size(size.width, size.height * 0.32f)
            )
        }

        // Vẽ viền khối 3D bên trong (Inner Bevel)
        drawRoundRect(
            brush = innerBevelBrush,
            topLeft = Offset(innerBevelInsetPx, innerBevelInsetPx),
            size = Size(
                size.width - innerBevelInsetPx * 2f,
                size.height - innerBevelInsetPx * 2f
            ),
            cornerRadius = CornerRadius(
                (cornerRadiusPx - innerBevelInsetPx).coerceAtLeast(0f),
                (cornerRadiusPx - innerBevelInsetPx).coerceAtLeast(0f)
            ),
            style = Stroke(width = innerBevelStrokePx)
        )

        // Vẽ viền phản quang ánh sáng 3D (Specular Rim)
        drawRoundRect(
            brush = specularRimBrush,
            topLeft = Offset(specularRimInsetPx, specularRimInsetPx),
            size = Size(
                size.width - specularRimInsetPx * 2f,
                size.height - specularRimInsetPx * 2f
            ),
            cornerRadius = CornerRadius(
                (cornerRadiusPx - specularRimInsetPx).coerceAtLeast(0f),
                (cornerRadiusPx - specularRimInsetPx).coerceAtLeast(0f)
            ),
            style = Stroke(width = specularRimStrokePx)
        )

        // Vẽ viền định hình ngoài cùng (Outer Contour Hairline)
        drawRoundRect(
            color = outerContourColor,
            topLeft = Offset(outerContourStrokePx / 2f, outerContourStrokePx / 2f),
            size = Size(
                size.width - outerContourStrokePx,
                size.height - outerContourStrokePx
            ),
            cornerRadius = cornerRadius,
            style = Stroke(width = outerContourStrokePx)
        )
    }
}

/**
 * Custom modifier vẽ viên nang kính lỏng cho Tab đang chọn (Sliding Active Liquid Glass Capsule):
 * - Nổi khối 3D rõ rệt trên nền kính khói của Dock.
 * - Có bóng đổ ánh xanh dịu bên ngoài viên nang, mặt kính cong phản quang phía trên và viền sáng 3D.
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

    val shadowBlurPx = (if (isDragging) 12.dp else 8.dp).toPx()
    val shadowDyPx = (if (isDragging) 4.dp else 2.5.dp).toPx()
    val pillShadowColor = if (isDark) {
        if (isDragging) BrandPrimary.copy(alpha = 0.42f) else Color.Black.copy(alpha = 0.48f)
    } else {
        if (isDragging) BrandPrimary.copy(alpha = 0.24f) else Color(0xFF1E3A8A).copy(alpha = 0.16f)
    }
    val pillShadowPaint = FrameworkPaint(FrameworkPaint.ANTI_ALIAS_FLAG).apply {
        color = pillShadowColor.toArgb()
        maskFilter = BlurMaskFilter(shadowBlurPx, BlurMaskFilter.Blur.NORMAL)
    }

    val pillBodyBrush = if (isDark) {
        Brush.verticalGradient(
            0.0f to Color(0xFF3B82F6).copy(alpha = if (isDragging) 0.44f else 0.32f),
            0.50f to Color(0xFF2563EB).copy(alpha = if (isDragging) 0.28f else 0.18f),
            1.0f to Color(0xFF1D4ED8).copy(alpha = if (isDragging) 0.36f else 0.25f)
        )
    } else {
        Brush.verticalGradient(
            0.0f to Color.White,
            0.55f to Color(0xFFF8FBFF),
            1.0f to if (isDragging) Color(0xFFE6F0FF) else Color(0xFFEEF5FF)
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

    val innerSpecularStrokePx = 1.2.dp.toPx()
    val innerSpecularInsetPx = 0.7.dp.toPx()
    val innerSpecularBrush = Brush.linearGradient(
        colors = if (isDark) {
            listOf(
                Color.White.copy(alpha = 0.50f),
                BrandActiveNavDark.copy(alpha = 0.25f),
                Color(0xFF93C5FD).copy(alpha = 0.45f)
            )
        } else {
            listOf(
                Color.White,
                Color(0xFFE0ECFF),
                Color.White
            )
        },
        start = Offset(0f, 0f),
        end = Offset(size.width, size.height)
    )

    val outerPillContourPx = 0.8.dp.toPx()
    val outerPillContourColor = if (isDark) {
        BrandActiveNavDark.copy(alpha = if (isDragging) 0.55f else 0.35f)
    } else {
        BrandPrimary.copy(alpha = if (isDragging) 0.30f else 0.18f)
    }

    onDrawBehind {
        // Bóng đổ bên ngoài viên nang đang chọn
        clipPath(pillPath, clipOp = ClipOp.Difference) {
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawRoundRect(
                    1.dp.toPx(),
                    shadowDyPx,
                    size.width - 1.dp.toPx(),
                    size.height + shadowDyPx,
                    cornerRadiusPx,
                    cornerRadiusPx,
                    pillShadowPaint
                )
            }
        }

        // Thân kính của viên nang đang chọn
        clipPath(pillPath) {
            drawRoundRect(
                brush = pillBodyBrush,
                cornerRadius = cornerRadius
            )
            drawRect(
                brush = pillTopSheenBrush,
                topLeft = Offset.Zero,
                size = Size(size.width, size.height * 0.50f)
            )
        }

        // Viền phản quang 3D bên trong viên nang
        drawRoundRect(
            brush = innerSpecularBrush,
            topLeft = Offset(innerSpecularInsetPx, innerSpecularInsetPx),
            size = Size(
                size.width - innerSpecularInsetPx * 2f,
                size.height - innerSpecularInsetPx * 2f
            ),
            cornerRadius = CornerRadius(
                (cornerRadiusPx - innerSpecularInsetPx).coerceAtLeast(0f),
                (cornerRadiusPx - innerSpecularInsetPx).coerceAtLeast(0f)
            ),
            style = Stroke(width = innerSpecularStrokePx)
        )

        // Viền định hình mảnh bên ngoài viên nang
        drawRoundRect(
            color = outerPillContourColor,
            topLeft = Offset(outerPillContourPx / 2f, outerPillContourPx / 2f),
            size = Size(
                size.width - outerPillContourPx,
                size.height - outerPillContourPx
            ),
            cornerRadius = cornerRadius,
            style = Stroke(width = outerPillContourPx)
        )
    }
}

@Composable
fun MainBottomNavigation(
    currentTab: MainTab,
    isCurrentTabScrolled: Boolean,
    onTabClick: (MainTab) -> Unit,
    onScrollToTop: (MainTab) -> Unit
) {
    val themeMode by ThemeManager.themeModeFlow.collectAsStateWithLifecycle()
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        0 -> false
        1 -> true
        else -> isSystemDark
    }
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current

    // Active / Inactive text & icon colors với độ tương phản sắc nét cho cả Light và Dark theme
    val activeContentColor = if (isDark) Color(0xFF60A5FA) else BrandPrimary
    val inactiveContentColor = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)

    // Tab order matching tenli-oneview MobileLayout.vue:
    // 1. Trang chủ (Home) -> 2. Sự kiện (Event) -> 3. Trực tiếp (Monitor/Live) -> 4. Cài đặt (Setting)
    val items = listOf(
        Triple(MainTab.Home, R.string.tab_home, LucideIcons.Home),
        Triple(MainTab.Event, R.string.tab_event, LucideIcons.Bell),
        Triple(MainTab.Monitor, R.string.tab_monitor, LucideIcons.Video),
        Triple(MainTab.Setting, R.string.tab_setting, LucideIcons.Settings)
    )
    val selectedIndex = items.indexOfFirst { it.first == currentTab }.coerceAtLeast(0)
    val scope = rememberCoroutineScope()

    var isDraggingNav by remember { mutableStateOf(false) }
    var rawDragLeftPx by remember { mutableFloatStateOf(0f) }
    var dragHoverIndex by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .iosLiquidGlassDock(isDark = isDark)
                .padding(horizontal = 5.dp, vertical = 5.dp)
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
                            durationMillis = 320,
                            easing = NavCapsuleGlideEasing
                        )
                    )
                }
            }

            val pillScale by animateFloatAsState(
                targetValue = if (isDraggingNav) 1.05f else 1f,
                animationSpec = tween(durationMillis = 150),
                label = "pillScale"
            )

            // Sliding Liquid Glass Capsule Indicator
            Box(
                modifier = Modifier
                    .offset { IntOffset(indicatorLeftAnim.value.roundToInt(), 0) }
                    .width(tabWidth)
                    .fillMaxHeight()
                    .scale(pillScale)
                    .iosLiquidGlassActivePill(
                        isDark = isDark,
                        isDragging = isDraggingNav
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(selectedIndex, tabWidthPx) {
                        detectHorizontalDragGestures(
                            onDragStart = { startOffset ->
                                isDraggingNav = true
                                val initialLeft = startOffset.x - tabWidthPx / 2f
                                rawDragLeftPx = initialLeft
                                val dampedLeft = when {
                                    initialLeft < 0f -> -(-initialLeft).pow(0.75f)
                                    initialLeft > maxLeftPx -> maxLeftPx + (initialLeft - maxLeftPx).pow(0.75f)
                                    else -> initialLeft
                                }
                                scope.launch { indicatorLeftAnim.snapTo(dampedLeft) }
                                val idx = ((dampedLeft + tabWidthPx / 2f) / tabWidthPx)
                                    .toInt()
                                    .coerceIn(0, items.size - 1)
                                dragHoverIndex = idx
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                val nextRaw = rawDragLeftPx + dragAmount
                                rawDragLeftPx = nextRaw
                                // Rubber-band edge resistance matching MobileLayout.vue lines 405-409
                                val dampedLeft = when {
                                    nextRaw < 0f -> -(-nextRaw).pow(0.75f)
                                    nextRaw > maxLeftPx -> maxLeftPx + (nextRaw - maxLeftPx).pow(0.75f)
                                    else -> nextRaw
                                }
                                scope.launch { indicatorLeftAnim.snapTo(dampedLeft) }
                                val idx = ((dampedLeft + tabWidthPx / 2f) / tabWidthPx)
                                    .toInt()
                                    .coerceIn(0, items.size - 1)
                                if (dragHoverIndex != idx) {
                                    dragHoverIndex = idx
                                    try {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    } catch (_: Exception) {}
                                }
                            },
                            onDragEnd = {
                                val targetIdx = dragHoverIndex
                                isDraggingNav = false
                                dragHoverIndex = null
                                if (targetIdx != null && targetIdx in items.indices) {
                                    val targetTab = items[targetIdx].first
                                    if (targetTab == currentTab) {
                                        onScrollToTop(targetTab)
                                    } else {
                                        onTabClick(targetTab)
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
                items.forEachIndexed { index, (tab, labelRes, iconRes) ->
                    val isSelected = if (isDraggingNav && dragHoverIndex != null) {
                        dragHoverIndex == index
                    } else {
                        currentTab == tab
                    }
                    val isDragTarget = isDraggingNav && dragHoverIndex == index
                    val showBackToTop = !isDraggingNav && currentTab == tab && isCurrentTabScrolled
                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) activeContentColor else inactiveContentColor,
                        animationSpec = tween(durationMillis = 180),
                        label = "tabColor"
                    )
                    val iconScale by animateFloatAsState(
                        targetValue = when {
                            isDragTarget -> 1.12f
                            isSelected -> 1.04f
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
                            isDragTarget -> (-2).dp
                            isSelected -> (-1).dp
                            else -> 0.dp
                        },
                        animationSpec = tween(durationMillis = 180),
                        label = "iconOffsetY"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(26.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (currentTab == tab) {
                                    onScrollToTop(tab)
                                } else {
                                    onTabClick(tab)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = iconRes,
                                contentDescription = stringResource(id = labelRes),
                                tint = contentColor,
                                modifier = Modifier
                                    .offset(y = iconOffsetY)
                                    .scale(iconScale)
                                    .size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(id = labelRes),
                                color = contentColor,
                                fontSize = 11.5.sp,
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

