package com.tenli.oneview.ui.utils

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import kotlin.math.abs

private const val DEFAULT_TOP_CACHE_COUNT = 16
private const val LIST_JUMP_THRESHOLD = 12
private const val LIST_JUMP_TARGET = 8
private const val GRID_JUMP_THRESHOLD = 16
private const val GRID_JUMP_TARGET = 10
private const val MIN_SCROLL_DURATION_MS = 200
private const val MAX_SCROLL_DURATION_MS = 380

private val SmoothScrollToTopEasing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

fun createTopItemSizeCache(size: Int = DEFAULT_TOP_CACHE_COUNT): IntArray = IntArray(size)

/**
 * Ghi nhận chiều cao thực tế của các item đầu danh sách khi bắt đầu/kết thúc cử chỉ cuộn
 * (chỉ chạy 2 lần mỗi lượt cuộn, 0% chi phí CPU trong suốt các frame cuộn tay).
 * Nhờ đó [smoothScrollToTop] biết chính xác tổng quãng đường về `(0, 0)` ngay từ Frame 0 kể cả khi
 * danh sách có các khối chiều cao rất lệch nhau (như Trang chủ: Grid thống kê, Phân bố AI, Sự kiện gần đây).
 */
@Composable
fun TrackTopItemSizesEffect(listState: LazyListState, outSizes: IntArray) {
    LaunchedEffect(listState, outSizes) {
        snapshotFlow { listState.isScrollInProgress }
            .collect {
                captureVisibleListSizes(listState, outSizes)
            }
    }
}

@Composable
fun TrackTopRowSizesEffect(gridState: LazyGridState, outRowSizes: IntArray) {
    LaunchedEffect(gridState, outRowSizes) {
        snapshotFlow { gridState.isScrollInProgress }
            .collect {
                captureVisibleGridRowSizes(gridState, outRowSizes)
            }
    }
}

private fun captureVisibleListSizes(listState: LazyListState, outSizes: IntArray) {
    val info = listState.layoutInfo
    val visible = info.visibleItemsInfo
    if (visible.isEmpty()) return
    val spacing = info.mainAxisItemSpacing
    for (i in visible.indices) {
        val item = visible[i]
        if (item.index in outSizes.indices) {
            outSizes[item.index] = item.size + spacing
        }
    }
}

private fun captureVisibleGridRowSizes(gridState: LazyGridState, outRowSizes: IntArray) {
    val info = gridState.layoutInfo
    val visible = info.visibleItemsInfo
    if (visible.isEmpty()) return
    val spacing = info.mainAxisItemSpacing
    for (i in visible.indices) {
        val item = visible[i]
        val row = item.row
        if (row in outRowSizes.indices) {
            val span = item.size.height + spacing
            if (span > outRowSizes[row]) {
                outRowSizes[row] = span
            }
        }
    }
}

private fun estimateListRemainingToTopPx(
    listState: LazyListState,
    knownTopSizes: IntArray?
): Float {
    val info = listState.layoutInfo
    val visible = info.visibleItemsInfo
    if (visible.isEmpty()) return 0f

    val item0 = visible.firstOrNull { it.index == 0 }
    if (item0 != null) {
        return item0.offset.toFloat()
    }

    val first = visible.first()
    val spacing = info.mainAxisItemSpacing
    var visibleSum = 0
    for (i in visible.indices) {
        visibleSum += visible[i].size
    }
    val fallbackSpan = (visibleSum / visible.size) + spacing

    var aboveTotalPx = 0
    for (idx in 0 until first.index) {
        val known = if (knownTopSizes != null && idx in knownTopSizes.indices) {
            knownTopSizes[idx]
        } else {
            0
        }
        aboveTotalPx += if (known > 0) known else fallbackSpan
    }
    return (first.offset - aboveTotalPx).toFloat()
}

private fun estimateGridRemainingToTopPx(
    gridState: LazyGridState,
    knownTopRowSizes: IntArray?
): Float {
    val info = gridState.layoutInfo
    val visible = info.visibleItemsInfo
    if (visible.isEmpty()) return 0f

    val item0 = visible.firstOrNull { it.index == 0 }
    if (item0 != null) {
        return item0.offset.y.toFloat()
    }

    val first = visible.first()
    val firstRow = first.row
    if (firstRow <= 0) {
        return first.offset.y.toFloat()
    }

    val spacing = info.mainAxisItemSpacing
    var visibleSum = 0
    for (i in visible.indices) {
        visibleSum += visible[i].size.height
    }
    val fallbackRowSpan = (visibleSum / visible.size) + spacing

    var aboveTotalPx = 0
    for (r in 0 until firstRow) {
        val known = if (knownTopRowSizes != null && r in knownTopRowSizes.indices) {
            knownTopRowSizes[r]
        } else {
            0
        }
        aboveTotalPx += if (known > 0) known else fallbackRowSpan
    }
    return (first.offset.y - aboveTotalPx).toFloat()
}

private fun computeScrollDurationMs(initialRemainingPx: Float, viewportHeightPx: Int): Int {
    val safeViewport = viewportHeightPx.coerceAtLeast(1)
    val screens = (abs(initialRemainingPx) / safeViewport).coerceIn(0.15f, 2.2f)
    return (180f + screens * 95f).toInt().coerceIn(MIN_SCROLL_DURATION_MS, MAX_SCROLL_DURATION_MS)
}

/**
 * Cuộn mượt về đầu danh sách `(0, 0)` chỉ trong **1 hành trình liên tục duy nhất** (không bị khựng 2 nhịp
 * như `LazyListState.animateScrollToItem(0)` mặc định của Compose khi chiều cao các mục chênh lệch lớn).
 */
suspend fun LazyListState.smoothScrollToTop(
    knownTopSizes: IntArray? = null,
    jumpThreshold: Int = LIST_JUMP_THRESHOLD,
    jumpTarget: Int = LIST_JUMP_TARGET
) {
    if (firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0) return

    if (firstVisibleItemIndex > jumpThreshold) {
        scrollToItem(jumpTarget, 0)
    }

    val initialRemaining = estimateListRemainingToTopPx(this, knownTopSizes)
    if (initialRemaining >= -0.5f) {
        scrollToItem(0, 0)
        return
    }
    val durationMs = computeScrollDurationMs(initialRemaining, layoutInfo.viewportSize.height)

    scroll {
        var prevProgress = 0f
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(durationMillis = durationMs, easing = SmoothScrollToTopEasing)
        ) { progress, _ ->
            if (knownTopSizes != null) {
                captureVisibleListSizes(this@smoothScrollToTop, knownTopSizes)
            }
            val remainingPx = estimateListRemainingToTopPx(this@smoothScrollToTop, knownTopSizes)
            val denom = 1f - prevProgress
            val stepFraction = if (denom > 1e-4f) {
                ((progress - prevProgress) / denom).coerceIn(0f, 1f)
            } else {
                1f
            }
            prevProgress = progress

            if (remainingPx >= -0.5f || !canScrollBackward) {
                return@animate
            }

            val delta = remainingPx * stepFraction
            if (delta < 0f) {
                scrollBy(delta)
            }
        }
    }

    if (firstVisibleItemIndex != 0 || firstVisibleItemScrollOffset != 0) {
        scrollToItem(0, 0)
    }
}

/**
 * Cuộn mượt về đầu lưới `(0, 0)` chỉ trong **1 hành trình liên tục duy nhất**.
 */
suspend fun LazyGridState.smoothScrollToTop(
    knownTopRowSizes: IntArray? = null,
    jumpThreshold: Int = GRID_JUMP_THRESHOLD,
    jumpTarget: Int = GRID_JUMP_TARGET
) {
    if (firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0) return

    if (firstVisibleItemIndex > jumpThreshold) {
        scrollToItem(jumpTarget, 0)
    }

    val initialRemaining = estimateGridRemainingToTopPx(this, knownTopRowSizes)
    if (initialRemaining >= -0.5f) {
        scrollToItem(0, 0)
        return
    }
    val durationMs = computeScrollDurationMs(initialRemaining, layoutInfo.viewportSize.height)

    scroll {
        var prevProgress = 0f
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(durationMillis = durationMs, easing = SmoothScrollToTopEasing)
        ) { progress, _ ->
            if (knownTopRowSizes != null) {
                captureVisibleGridRowSizes(this@smoothScrollToTop, knownTopRowSizes)
            }
            val remainingPx = estimateGridRemainingToTopPx(this@smoothScrollToTop, knownTopRowSizes)
            val denom = 1f - prevProgress
            val stepFraction = if (denom > 1e-4f) {
                ((progress - prevProgress) / denom).coerceIn(0f, 1f)
            } else {
                1f
            }
            prevProgress = progress

            if (remainingPx >= -0.5f || !canScrollBackward) {
                return@animate
            }

            val delta = remainingPx * stepFraction
            if (delta < 0f) {
                scrollBy(delta)
            }
        }
    }

    if (firstVisibleItemIndex != 0 || firstVisibleItemScrollOffset != 0) {
        scrollToItem(0, 0)
    }
}

/**
 * Cuộn mượt về đầu trang `(0)` cho `Column(Modifier.verticalScroll(scrollState))` theo đúng quãng đường pixel thực tế.
 */
suspend fun ScrollState.smoothScrollToTop() {
    val startValue = value
    if (startValue <= 0) return
    val durationMs = computeScrollDurationMs(startValue.toFloat(), viewportSize)
    animateScrollTo(
        value = 0,
        animationSpec = tween(durationMillis = durationMs, easing = SmoothScrollToTopEasing)
    )
}
