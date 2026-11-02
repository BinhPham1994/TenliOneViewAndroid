package com.tenli.oneview.ui.features.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
import com.tenli.oneview.ui.theme.TextSecondary
import com.tenli.oneview.ui.theme.TextSecondaryDark
import com.tenli.oneview.ui.theme.ThemeManager
import kotlin.math.roundToInt

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

    // Liquid glass container colors matching mobile.css (.mobile-bottom-nav)
    val navBgColor = if (isDark) Color(0xEB151F32) else Color(0xD9FFFFFF)
    val navBorderColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.85f)

    // Liquid glass sliding indicator capsule matching mobile.css (.mobile-nav-sliding-indicator)
    val pillBgColor = if (isDark) BrandPrimary.copy(alpha = 0.16f) else Color(0xFFFFFFFF)
    val pillBorderColor = if (isDark) BrandActiveNavDark.copy(alpha = 0.28f) else Color.Black.copy(alpha = 0.04f)

    // Active / Inactive text & icon colors matching mobile.css (.mobile-nav-item)
    val activeContentColor = if (isDark) BrandActiveNavDark else BrandPrimary
    val inactiveContentColor = if (isDark) TextSecondaryDark else TextSecondary

    // Tab order matching tenli-oneview MobileLayout.vue:
    // 1. Trang chủ (Home) -> 2. Sự kiện (Event) -> 3. Trực tiếp (Monitor/Live) -> 4. Cài đặt (Setting)
    val items = listOf(
        Triple(MainTab.Home, R.string.tab_home, LucideIcons.Home),
        Triple(MainTab.Event, R.string.tab_event, LucideIcons.Bell),
        Triple(MainTab.Monitor, R.string.tab_monitor, LucideIcons.Video),
        Triple(MainTab.Setting, R.string.tab_setting, LucideIcons.Settings)
    )
    val selectedIndex = items.indexOfFirst { it.first == currentTab }.coerceAtLeast(0)

    var isDraggingNav by remember { mutableStateOf(false) }
    var dragLeftPx by remember { mutableFloatStateOf(0f) }
    var dragHoverIndex by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .shadow(
                    elevation = if (isDark) 16.dp else 12.dp,
                    shape = RoundedCornerShape(32.dp),
                    ambientColor = Color.Black.copy(alpha = if (isDark) 0.45f else 0.12f),
                    spotColor = Color.Black.copy(alpha = if (isDark) 0.45f else 0.14f)
                )
                .clip(RoundedCornerShape(32.dp))
                .background(navBgColor)
                .border(1.dp, navBorderColor, RoundedCornerShape(32.dp))
                .padding(4.dp)
        ) {
            val tabWidth = maxWidth / items.size
            val tabWidthPx = with(density) { tabWidth.toPx() }
            val maxLeftPx = tabWidthPx * (items.size - 1)

            val indicatorOffset by animateDpAsState(
                targetValue = tabWidth * selectedIndex,
                animationSpec = spring(
                    dampingRatio = 0.82f,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "liquidGlassIndicator"
            )

            val pillScale by animateFloatAsState(
                targetValue = if (isDraggingNav) 1.05f else 1f,
                animationSpec = tween(durationMillis = 150),
                label = "pillScale"
            )

            // Sliding Liquid Glass Capsule Indicator
            Box(
                modifier = Modifier
                    .then(
                        if (isDraggingNav) {
                            Modifier.offset { IntOffset(dragLeftPx.roundToInt(), 0) }
                        } else {
                            Modifier.offset(x = indicatorOffset)
                        }
                    )
                    .width(tabWidth)
                    .fillMaxHeight()
                    .scale(pillScale)
                    .shadow(
                        elevation = if (isDark) 0.dp else 3.dp,
                        shape = RoundedCornerShape(26.dp),
                        ambientColor = Color.Black.copy(alpha = 0.08f),
                        spotColor = Color.Black.copy(alpha = 0.08f)
                    )
                    .clip(RoundedCornerShape(26.dp))
                    .background(pillBgColor)
                    .border(
                        width = if (isDark) 1.dp else 0.5.dp,
                        color = pillBorderColor,
                        shape = RoundedCornerShape(26.dp)
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(selectedIndex, tabWidthPx) {
                        detectHorizontalDragGestures(
                            onDragStart = { startOffset ->
                                isDraggingNav = true
                                val initialLeft = (startOffset.x - tabWidthPx / 2f).coerceIn(0f, maxLeftPx)
                                dragLeftPx = initialLeft
                                val idx = ((initialLeft + tabWidthPx / 2f) / tabWidthPx)
                                    .toInt()
                                    .coerceIn(0, items.size - 1)
                                dragHoverIndex = idx
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                val nextLeft = (dragLeftPx + dragAmount).coerceIn(0f, maxLeftPx)
                                dragLeftPx = nextLeft
                                val idx = ((nextLeft + tabWidthPx / 2f) / tabWidthPx)
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
                                    onTabClick(items[targetIdx].first)
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
                    val showBackToTop = !isDraggingNav && currentTab == tab && isCurrentTabScrolled
                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) activeContentColor else inactiveContentColor,
                        animationSpec = tween(durationMillis = 180),
                        label = "tabColor"
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
                                if (showBackToTop) {
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
                                modifier = Modifier.size(21.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(id = labelRes),
                                color = contentColor,
                                fontSize = 11.sp,
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
