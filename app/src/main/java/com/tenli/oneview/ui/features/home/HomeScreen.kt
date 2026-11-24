package com.tenli.oneview.ui.features.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import com.tenli.oneview.R
import com.tenli.oneview.data.local.MonitorCacheManager
import com.tenli.oneview.data.local.UserPermissionManager
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.model.network.CameraModel
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.ui.component.CardPressIndication
import com.tenli.oneview.ui.component.MobileEventCard
import com.tenli.oneview.ui.component.MobileSectionHeader
import com.tenli.oneview.ui.component.MobileTopBar
import com.tenli.oneview.ui.component.MobileTopPopupManager
import com.tenli.oneview.ui.component.rememberIsEnglish
import com.tenli.oneview.ui.component.rememberShimmerProgressState
import com.tenli.oneview.ui.component.shimmerBackground
import com.tenli.oneview.ui.features.event.buildCameraNameMap
import com.tenli.oneview.ui.features.event.cameraUuidOrNull
import com.tenli.oneview.ui.features.monitor.CameraQuickFilter
import com.tenli.oneview.ui.theme.BrandAccent
import com.tenli.oneview.ui.theme.InfoSkyDeep
import com.tenli.oneview.ui.theme.LucideIcons
import com.tenli.oneview.ui.theme.OrangeDeep
import com.tenli.oneview.ui.theme.OrangeUserAccent
import com.tenli.oneview.ui.theme.PurpleAccent
import com.tenli.oneview.ui.theme.SuccessGreen
import com.tenli.oneview.ui.theme.appColors
import com.tenli.oneview.ui.theme.micro
import com.tenli.oneview.ui.theme.spacing
import com.tenli.oneview.ui.utils.AiTypeHelper
import kotlin.math.abs

// Gradient stops của 4 thẻ thống kê (đồng bộ với MobileHomePage.vue)
private val StatBlueGradientStart = Color(0xFF0D8BF2)
private val StatGreenGradientEnd = Color(0xFF059669)
private val StatPurpleGradientEnd = Color(0xFF9333EA)

private val StatTotalEventsGradient = listOf(StatBlueGradientStart, InfoSkyDeep)
private val StatOnlineCamerasGradient = listOf(SuccessGreen, StatGreenGradientEnd)
private val StatActiveAiTasksGradient = listOf(PurpleAccent, StatPurpleGradientEnd)
private val StatRecordingCamerasGradient = listOf(OrangeUserAccent, OrangeDeep)

private val HomeBottomContentPadding = 130.dp
private const val HOME_CAROUSEL_ASPECT_RATIO = 16f / 9f
private const val HOME_SHELF_IMAGE_WIDTH_PX = 540
private const val HOME_SHELF_IMAGE_HEIGHT_PX = 304
private val StatLabelLetterSpacing = 0.sp
private val FavoriteStarColor = Color(0xFFFBBF24)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
    listState: LazyListState = rememberLazyListState(),
    isTabActive: Boolean = true,
    onEventClick: (String) -> Unit = {},
    onNavigateToEvents: (TimeFilter, Int?, String?) -> Unit = { _, _, _ -> },
    onNavigateToMonitor: (CameraQuickFilter) -> Unit = {},
    onOpenCamera: (CameraModel) -> Unit = {},
    onHorizontalSwipeZoneChanged: (String, LayoutCoordinates?) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val permissionState by UserPermissionManager.permissionState.collectAsStateWithLifecycle()
    val isEn = rememberIsEnglish()
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.appColors
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val density = LocalDensity.current
    val containerWidthPx = LocalWindowInfo.current.containerSize.width
    val containerWidthDp = remember(containerWidthPx, screenWidthDp, density) {
        if (containerWidthPx > 0) with(density) { containerWidthPx.toDp() } else screenWidthDp.dp
    }
    val shelfCardWidth = remember(screenWidthDp) {
        ((screenWidthDp * 0.64f).coerceIn(216f, 268f)).dp
    }

    val currentSelectedFilter by rememberUpdatedState(uiState.selectedFilter)
    val currentSelectedServiceId by rememberUpdatedState(uiState.selectedServiceId)
    val currentOnNavigateToEvents by rememberUpdatedState(onNavigateToEvents)
    val currentOnNavigateToMonitor by rememberUpdatedState(onNavigateToMonitor)
    val currentOnOpenCamera by rememberUpdatedState(onOpenCamera)
    val currentOnEventClick by rememberUpdatedState(onEventClick)
    val currentOnSwipeZoneChanged by rememberUpdatedState(onHorizontalSwipeZoneChanged)

    LaunchedEffect(Unit) {
        viewModel.syncViewModeFromPrefs()
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { errMsg ->
            MobileTopPopupManager.error(errMsg)
            viewModel.clearError()
        }
    }

    val cameraMap = remember(uiState.cameraList) {
        buildCameraNameMap(uiState.cameraList)
    }

    val favoriteCameras = remember(uiState.cameraList, uiState.favoriteCameraIds) {
        if (uiState.favoriteCameraIds.isEmpty()) {
            emptyList()
        } else {
            uiState.cameraList.filter { it.id in uiState.favoriteCameraIds }
        }
    }

    val recentFiveEvents = uiState.recentEvents

    val handleViewAllEvents: () -> Unit = remember {
        { currentOnNavigateToEvents(currentSelectedFilter, currentSelectedServiceId, null) }
    }
    val handleViewFavoriteCameras: () -> Unit = remember {
        { currentOnNavigateToMonitor(CameraQuickFilter.FAVORITES) }
    }
    val handleOpenRecordingCameras: () -> Unit = remember {
        { currentOnNavigateToMonitor(CameraQuickFilter.ALL) }
    }
    val handleOpenCamera: (CameraModel) -> Unit = remember {
        { cam -> currentOnOpenCamera(cam) }
    }
    val handleSelectAiModule: (String) -> Unit = remember {
        { sensorType ->
            currentOnNavigateToEvents(
                currentSelectedFilter,
                currentSelectedServiceId,
                sensorType
            )
        }
    }
    val handleEventCardClick: (EventData) -> Unit = remember(viewModel) {
        { event ->
            viewModel.onEventSelected(event)
            currentOnEventClick(event.id.toString())
        }
    }
    val handleSwipeZoneChanged: (String, LayoutCoordinates?) -> Unit = remember {
        { key, coords -> currentOnSwipeZoneChanged(key, coords) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.pageBg)
    ) {
        MobileTopBar(
            title = stringResource(id = R.string.tab_home)
        )

        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Dùng LazyColumn kết hợp LazyRow cho từng hàng bài AI & camera yêu thích:
            // chỉ dựng và decode ảnh của các hàng đang hiển thị giúp cuộn mượt 120Hz.
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = spacing.space8, bottom = HomeBottomContentPadding)
            ) {
                // 1. Khối "Sự kiện gần đây" dạng Hero Carousel 16:9 cuộn ngang đặt nổi bật trên cùng
                if (permissionState.canAccessEvents) {
                    item(key = "home_recent_events_carousel", contentType = "recent_carousel") {
                        HomeRecentEventsCarouselSection(
                            events = recentFiveEvents,
                            isLoading = uiState.isLoading,
                            isTabActive = isTabActive,
                            cameraMap = cameraMap,
                            isEn = isEn,
                            onEventClick = handleEventCardClick,
                            onViewAllClick = handleViewAllEvents,
                            zoneKey = "home_recent_carousel",
                            onHorizontalSwipeZoneChanged = handleSwipeZoneChanged
                        )
                        Spacer(modifier = Modifier.height(spacing.space18))
                    }
                }

                // 2. 2x2 Stat Grid matching MobileHomePage.vue + tiêu đề "Tổng quan hôm nay"
                item(key = "home_stat_grid", contentType = "stat_grid") {
                    val totalCameras = uiState.totalCamerasCount
                    val hasOfflineCameras = totalCameras > 0 && uiState.onlineCamerasCount < totalCameras

                    Column(modifier = Modifier.fillMaxWidth()) {
                        MobileSectionHeader(
                            title = if (isEn) "Today's overview" else "Tổng quan hôm nay"
                        )

                        Column(
                            modifier = Modifier.padding(horizontal = spacing.space10),
                            verticalArrangement = Arrangement.spacedBy(spacing.space10)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(spacing.space10)
                            ) {
                                MobileStatCard(
                                    label = if (isEn) "TOTAL EVENTS" else "TỔNG SỰ KIỆN",
                                    mainValue = formatGroupedCount(uiState.totalDetectionsCount, isEn),
                                    subValue = null,
                                    accentColor = BrandAccent,
                                    iconGradient = StatTotalEventsGradient,
                                    icon = LucideIcons.Shield,
                                    onClick = if (permissionState.canAccessEvents) handleViewAllEvents else null,
                                    modifier = Modifier.weight(1f)
                                )
                                MobileStatCard(
                                    label = if (isEn) "ONLINE CAMERAS" else "CAMERA HOẠT ĐỘNG",
                                    mainValue = uiState.onlineCamerasCount.toString(),
                                    subValue = "/$totalCameras",
                                    accentColor = SuccessGreen,
                                    iconGradient = StatOnlineCamerasGradient,
                                    icon = LucideIcons.Video,
                                    onClick = if (permissionState.canAccessLiveStream) {
                                        remember(hasOfflineCameras) {
                                            {
                                                currentOnNavigateToMonitor(
                                                    if (hasOfflineCameras) CameraQuickFilter.OFFLINE else CameraQuickFilter.ALL
                                                )
                                            }
                                        }
                                    } else {
                                        null
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(spacing.space10)
                            ) {
                                MobileStatCard(
                                    label = if (isEn) "ACTIVE AI TASKS" else "BÀI AI ĐANG CHẠY",
                                    mainValue = uiState.runningAiTasks.toString(),
                                    subValue = "/${uiState.totalAiTasks}",
                                    accentColor = PurpleAccent,
                                    iconGradient = StatActiveAiTasksGradient,
                                    icon = LucideIcons.Settings,
                                    onClick = if (permissionState.canAccessEvents) handleViewAllEvents else null,
                                    modifier = Modifier.weight(1f)
                                )
                                MobileStatCard(
                                    label = if (isEn) "RECORDING CAMERAS" else "CAMERA GHI HÌNH",
                                    mainValue = uiState.recordingCamerasCount.toString(),
                                    subValue = "/$totalCameras",
                                    accentColor = OrangeUserAccent,
                                    iconGradient = StatRecordingCamerasGradient,
                                    icon = LucideIcons.Disc,
                                    onClick = if (permissionState.canAccessLiveStream) handleOpenRecordingCameras else null,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // 3. Dải Camera yêu thích xem nhanh 1 chạm (tự ẩn nếu người dùng chưa ghim sao camera nào)
                if (permissionState.canAccessLiveStream && favoriteCameras.isNotEmpty()) {
                    item(key = "home_favorite_cameras", contentType = "favorite_cameras_shelf") {
                        HomeFavoriteCamerasSection(
                            cameras = favoriteCameras,
                            offlineCameraIds = uiState.offlineCameraIds,
                            cameraSnapshots = uiState.cameraSnapshots,
                            isEn = isEn,
                            onCameraClick = handleOpenCamera,
                            onViewAllClick = handleViewFavoriteCameras,
                            zoneKey = "home_favorite_cameras",
                            onHorizontalSwipeZoneChanged = handleSwipeZoneChanged
                        )
                    }
                }

                // 4. Phân bố sự kiện AI dạng danh sách cuộn ngang từng bài AI (tỷ lệ thẻ nhỏ gọn hơn Hero để phân cấp thị giác rõ)
                if (permissionState.canAccessEvents && uiState.aiDistribution.isNotEmpty()) {
                    items(
                        items = uiState.aiDistribution,
                        key = { item -> "ai_shelf_${item.filterSensorType}" },
                        contentType = { "ai_module_shelf" }
                    ) { item ->
                        val moduleEvents = uiState.aiModuleEvents[item.filterSensorType].orEmpty()
                        val isModuleLoading = item.filterSensorType in uiState.loadingAiModuleTypes
                        HomeAiModuleEventsShelf(
                            item = item,
                            events = moduleEvents,
                            isLoading = isModuleLoading,
                            isTabActive = isTabActive,
                            cameraMap = cameraMap,
                            cardWidth = shelfCardWidth,
                            containerWidth = containerWidthDp,
                            isEn = isEn,
                            onSelectModule = handleSelectAiModule,
                            onEventClick = handleEventCardClick,
                            zoneKey = "ai_shelf_${item.filterSensorType}",
                            onHorizontalSwipeZoneChanged = handleSwipeZoneChanged
                        )
                    }
                }
            }
        }
    }
}

/**
 * Khối "Sự kiện mới nhất" dạng Hero Carousel 16:9 cuộn ngang ở đầu Trang chủ:
 * - Vuốt ngang snap mượt qua tối đa 5 sự kiện mới nhất, hé nhẹ mép thẻ tiếp theo.
 */
@Composable
private fun HomeRecentEventsCarouselSection(
    events: List<EventData>,
    isLoading: Boolean,
    isTabActive: Boolean,
    cameraMap: Map<String, String>,
    isEn: Boolean,
    onEventClick: (EventData) -> Unit,
    onViewAllClick: () -> Unit,
    zoneKey: String,
    onHorizontalSwipeZoneChanged: (String, LayoutCoordinates?) -> Unit
) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val cardShape = remember(spacing.radiusCompact) { RoundedCornerShape(spacing.radiusCompact) }
    val currentOnSwipeZoneChanged by rememberUpdatedState(onHorizontalSwipeZoneChanged)

    DisposableEffect(zoneKey) {
        onDispose { currentOnSwipeZoneChanged(zoneKey, null) }
    }

    LaunchedEffect(zoneKey, events.size) {
        if (events.size <= 1) {
            currentOnSwipeZoneChanged(zoneKey, null)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        MobileSectionHeader(
            title = if (isEn) "Latest events" else "Sự kiện mới nhất",
            actionText = if (isEn) "View all" else "Xem tất cả",
            onActionClick = onViewAllClick
        )

        if (isLoading && events.isEmpty()) {
            if (isTabActive) {
                val shimmerProgress = rememberShimmerProgressState()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.space10)
                        .aspectRatio(HOME_CAROUSEL_ASPECT_RATIO)
                        .clip(cardShape)
                        .shimmerBackground(shimmerProgress, colors.isDark)
                )
            }
        } else if (events.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.space10)
                    .clip(cardShape)
                    .background(colors.surface)
                    .border(spacing.borderMedium, colors.border, cardShape)
                    .padding(horizontal = spacing.space14, vertical = spacing.space14),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.space12)
            ) {
                Box(
                    modifier = Modifier
                        .size(spacing.avatarMedium)
                        .clip(CircleShape)
                        .background(colors.controlBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = LucideIcons.Inbox,
                        contentDescription = null,
                        tint = colors.textMuted,
                        modifier = Modifier.size(spacing.iconRegular)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(spacing.space2)
                ) {
                    Text(
                        text = if (isEn) "No recent events" else "Chưa có sự kiện nào gần đây",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textTitle
                    )
                    Text(
                        text = if (isEn) "New AI alerts will appear here immediately" else "Sự kiện AI mới sẽ hiển thị ngay tại đây",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textMuted
                    )
                }
            }
        } else {
            val pagerState = rememberPagerState(
                initialPage = 0,
                pageCount = { events.size }
            )
            val topEventKey = events.firstOrNull()?.let { "${it.serviceId}_${it.id}_${it.time}" }
            var lastTopEventKey by remember { mutableStateOf(topEventKey) }
            LaunchedEffect(topEventKey, events.size) {
                if (topEventKey != null && topEventKey != lastTopEventKey) {
                    lastTopEventKey = topEventKey
                    if (pagerState.currentPage != 0) {
                        pagerState.animateScrollToPage(0)
                    }
                } else if (pagerState.currentPage >= events.size && events.isNotEmpty()) {
                    pagerState.scrollToPage(0)
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onPlaced { coords ->
                            currentOnSwipeZoneChanged(
                                zoneKey,
                                if (events.size > 1) coords else null
                            )
                        }
                ) {
                    HorizontalPager(
                        state = pagerState,
                        contentPadding = PaddingValues(
                            start = spacing.space10,
                            end = if (events.size > 1) spacing.space24 else spacing.space10
                        ),
                        pageSpacing = spacing.space10,
                        beyondViewportPageCount = 1,
                        key = { idx ->
                            val ev = events[idx]
                            "${ev.serviceId}_${ev.id}_${ev.time}"
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { page ->
                        val event = events[page]
                        val cameraName = event.cameraUuidOrNull?.let { cameraMap[it] }.orEmpty()
                        MobileEventCard(
                            event = event,
                            cameraName = cameraName,
                            isListView = false,
                            enableSharedElement = false,
                            onClick = { onEventClick(event) }
                        )
                    }
                }

                if (events.size > 1) {
                    Spacer(modifier = Modifier.height(spacing.space8))
                    HomeCarouselDots(
                        pagerState = pagerState,
                        count = events.size
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeCarouselDots(
    pagerState: PagerState,
    count: Int
) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val currentPage = pagerState.currentPage
    Row(
        horizontalArrangement = Arrangement.spacedBy(spacing.space6),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { index ->
            val isSelected = currentPage == index
            val dotWidth by animateDpAsState(
                targetValue = if (isSelected) spacing.space18 else spacing.space6,
                animationSpec = tween(durationMillis = 200),
                label = "homeCarouselDotWidth"
            )
            val dotColor by animateColorAsState(
                targetValue = if (isSelected) colors.accent else colors.textPlaceholder.copy(alpha = 0.45f),
                animationSpec = tween(durationMillis = 200),
                label = "homeCarouselDotColor"
            )
            Box(
                modifier = Modifier
                    .width(dotWidth)
                    .height(spacing.space6)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
    }
}

/**
 * Thẻ thống kê dọc căn giữa giống hệt PWA MobileHomePage.vue (.stat-card):
 * - Icon squircle 40x40dp bo góc 12dp ở trên cùng
 * - Nhãn chữ in hoa (UPPERCASE) ở giữa
 * - Số liệu chữ đậm có màu tương ứng ở dưới cùng
 */
@Composable
fun MobileStatCard(
    label: String,
    mainValue: String,
    subValue: String? = null,
    accentColor: Color,
    iconGradient: List<Color> = listOf(accentColor, accentColor),
    icon: ImageVector,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val haptic = LocalHapticFeedback.current
    val cardShape = remember(spacing.radiusCompact) { RoundedCornerShape(spacing.radiusCompact) }
    val gradientBrush = remember(iconGradient) { Brush.linearGradient(iconGradient) }
    val valueText = remember(mainValue, subValue, accentColor) {
        buildAnnotatedString {
            withStyle(
                SpanStyle(
                    color = accentColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontFeatureSettings = "tnum"
                )
            ) {
                append(mainValue)
            }
            if (!subValue.isNullOrEmpty()) {
                withStyle(
                    SpanStyle(
                        color = accentColor.copy(alpha = 0.52f),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFeatureSettings = "tnum"
                    )
                ) {
                    append(subValue)
                }
            }
        }
    }
    val currentOnClick by rememberUpdatedState(onClick)
    val pressIndication = remember(spacing.radiusCompact) {
        CardPressIndication(
            pressedScale = 0.97f,
            cornerRadius = spacing.radiusCompact
        )
    }

    Column(
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = null,
                        indication = pressIndication,
                        onClick = {
                            try {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            } catch (_: Exception) {
                            }
                            currentOnClick?.invoke()
                        }
                    )
                } else {
                    Modifier
                }
            )
            .clip(cardShape)
            .background(colors.surface)
            .padding(vertical = spacing.space16, horizontal = spacing.space12),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(spacing.avatarMedium)
                .clip(cardShape)
                .background(gradientBrush),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = colors.onAccent,
                modifier = Modifier.size(spacing.iconRegular)
            )
        }
        Spacer(modifier = Modifier.height(spacing.space10))
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = colors.textBody,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            letterSpacing = StatLabelLetterSpacing
        )
        Spacer(modifier = Modifier.height(spacing.space4))
        Text(
            text = valueText,
            style = MaterialTheme.typography.headlineLarge.copy(fontFeatureSettings = "tnum"),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Dải Camera yêu thích cuộn ngang xem nhanh 1 chạm trên Trang chủ.
 * Chỉ hiển thị khi người dùng đã ghim sao ít nhất 1 camera trong tab Trực tiếp.
 */
@Composable
private fun HomeFavoriteCamerasSection(
    cameras: List<CameraModel>,
    offlineCameraIds: Set<Int>,
    cameraSnapshots: Map<Int, String>,
    isEn: Boolean,
    onCameraClick: (CameraModel) -> Unit,
    onViewAllClick: () -> Unit,
    zoneKey: String,
    onHorizontalSwipeZoneChanged: (String, LayoutCoordinates?) -> Unit
) {
    val spacing = MaterialTheme.spacing
    val currentOnSwipeZoneChanged by rememberUpdatedState(onHorizontalSwipeZoneChanged)
    val hasScrollable = cameras.size > 1

    DisposableEffect(zoneKey) {
        onDispose { currentOnSwipeZoneChanged(zoneKey, null) }
    }

    LaunchedEffect(zoneKey, hasScrollable) {
        if (!hasScrollable) {
            currentOnSwipeZoneChanged(zoneKey, null)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = spacing.space18)
    ) {
        MobileSectionHeader(
            title = if (isEn) "Favorite cameras" else "Camera yêu thích",
            actionText = if (isEn) "View all" else "Xem tất cả",
            onActionClick = onViewAllClick
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onPlaced { coords ->
                    currentOnSwipeZoneChanged(
                        zoneKey,
                        if (hasScrollable) coords else null
                    )
                }
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = spacing.space10),
                horizontalArrangement = Arrangement.spacedBy(spacing.space10),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(
                    items = cameras,
                    key = { cam -> "home_fav_cam_${cam.id}" },
                    contentType = { "home_favorite_camera_card" }
                ) { cam ->
                    HomeFavoriteCameraCard(
                        camera = cam,
                        snapshotUrl = cameraSnapshots[cam.id],
                        isOffline = cam.id in offlineCameraIds,
                        isEn = isEn,
                        onClick = { onCameraClick(cam) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeFavoriteCameraCard(
    camera: CameraModel,
    snapshotUrl: String?,
    isOffline: Boolean,
    isEn: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val haptic = LocalHapticFeedback.current
    val cardShape = remember(spacing.radiusCompact) { RoundedCornerShape(spacing.radiusCompact) }

    val imageRequest = remember(camera.id, snapshotUrl) {
        val url = snapshotUrl?.trim().orEmpty()
        if (url.isEmpty()) {
            null
        } else {
            ImageRequest.Builder(context)
                .data(MonitorCacheManager.coilDataModel(url))
                .size(MonitorCacheManager.SNAPSHOT_CARD_WIDTH_PX, MonitorCacheManager.SNAPSHOT_CARD_HEIGHT_PX)
                .precision(Precision.INEXACT)
                .allowHardware(true)
                .allowRgb565(true)
                .crossfade(false)
                .memoryCacheKey(MonitorCacheManager.cardMemCacheKey(camera.id, url))
                .diskCacheKey(MonitorCacheManager.diskCacheKey(camera.id, url))
                .build()
        }
    }

    Column(
        modifier = Modifier
            .width(168.dp)
            .clip(cardShape)
            .background(colors.surface)
            .border(spacing.borderMedium, colors.border, cardShape)
            .clickable {
                try {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                } catch (_: Exception) {
                }
                onClick()
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(HOME_CAROUSEL_ASPECT_RATIO)
                .background(colors.thumbnailBg),
            contentAlignment = Alignment.Center
        ) {
            if (imageRequest != null) {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = camera.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = LucideIcons.Video,
                    contentDescription = null,
                    tint = colors.textPlaceholder,
                    modifier = Modifier.size(spacing.iconRegular)
                )
            }

            if (isOffline) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(spacing.space6)
                        .clip(RoundedCornerShape(spacing.radiusTiny))
                        .background(colors.errorStrong.copy(alpha = 0.88f))
                        .padding(horizontal = spacing.space6, vertical = spacing.space2)
                ) {
                    Text(
                        text = if (isEn) "Offline" else "Mất kết nối",
                        style = MaterialTheme.typography.micro,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(spacing.space6)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = LucideIcons.StarFilled,
                    contentDescription = null,
                    tint = FavoriteStarColor,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.space8, vertical = spacing.space6),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.space6)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (isOffline) colors.error else colors.success)
            )
            Text(
                text = camera.name,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.textTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Một hàng bài AI trong phần Phân bố sự kiện AI:
 * - Tiêu đề rõ gọn (tên bài AI + "Xem tất cả >").
 * - Danh sách cuộn ngang hiển thị tối đa 5 sự kiện mới nhất của bài AI với kích thước thẻ gọn hơn Hero Carousel (~64% chiều rộng)
 *   để tạo phân cấp thị giác rõ và lộ thẻ kế tiếp.
 */
@Composable
private fun HomeAiModuleEventsShelf(
    item: HomeAiDistributionItem,
    events: List<EventData>,
    isLoading: Boolean,
    isTabActive: Boolean,
    cameraMap: Map<String, String>,
    cardWidth: Dp,
    containerWidth: Dp,
    isEn: Boolean,
    onSelectModule: (String) -> Unit,
    onEventClick: (EventData) -> Unit,
    zoneKey: String,
    onHorizontalSwipeZoneChanged: (String, LayoutCoordinates?) -> Unit
) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val haptic = LocalHapticFeedback.current
    val currentOnSelectModule by rememberUpdatedState(onSelectModule)
    val currentOnSwipeZoneChanged by rememberUpdatedState(onHorizontalSwipeZoneChanged)

    val moduleColor = remember(item.filterSensorType) {
        AiTypeHelper.getAiColor(item.filterSensorType)
    }
    val formattedCount = remember(item.count, isEn) {
        formatGroupedCount(item.count, isEn)
    }
    val cardHeight = cardWidth / HOME_CAROUSEL_ASPECT_RATIO
    val cardShape = remember(spacing.radiusCompact) { RoundedCornerShape(spacing.radiusCompact) }
    // Luôn hiển thị thẻ "Xem tất cả" ở cuối khi có từ 2 sự kiện trở lên hoặc còn nhiều sự kiện hơn 5 thẻ,
    // đồng thời canh độ rộng vừa khít phần còn lại của màn hình để sự kiện cuối cùng snap thẳng mép trái.
    val showViewMoreTile = events.isNotEmpty() && (item.count > events.size.toLong() || events.size > 1)
    val viewMoreTileWidth = remember(containerWidth, cardWidth, spacing.space10) {
        (containerWidth - spacing.space10 * 3 - cardWidth).coerceIn(96.dp, 148.dp)
    }
    val naturalContentWidth = remember(events.size, showViewMoreTile, cardWidth, viewMoreTileWidth, spacing.space10) {
        if (events.isEmpty()) {
            0.dp
        } else {
            val eventsSpan = cardWidth * events.size + spacing.space10 * (events.size - 1)
            val viewMoreSpan = if (showViewMoreTile) spacing.space10 + viewMoreTileWidth else 0.dp
            eventsSpan + viewMoreSpan + spacing.space10 * 2
        }
    }
    val isHorizontallyScrollable = events.size > 1 && naturalContentWidth > containerWidth
    val endContentPadding = remember(
        isHorizontallyScrollable,
        showViewMoreTile,
        containerWidth,
        cardWidth,
        viewMoreTileWidth,
        spacing.space10
    ) {
        if (!isHorizontallyScrollable) {
            spacing.space10
        } else {
            val trailingAnchorSpan = cardWidth + if (showViewMoreTile) spacing.space10 + viewMoreTileWidth else 0.dp
            (containerWidth - spacing.space10 - trailingAnchorSpan).coerceAtLeast(spacing.space10)
        }
    }
    val hasHorizontalScrollableItems = isHorizontallyScrollable || (isLoading && events.isEmpty())

    val openModuleEvents: () -> Unit = remember(item.filterSensorType) {
        {
            try {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            } catch (_: Exception) {
            }
            currentOnSelectModule(item.filterSensorType)
        }
    }

    DisposableEffect(zoneKey) {
        onDispose { currentOnSwipeZoneChanged(zoneKey, null) }
    }

    LaunchedEffect(zoneKey, hasHorizontalScrollableItems) {
        if (!hasHorizontalScrollableItems) {
            currentOnSwipeZoneChanged(zoneKey, null)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = spacing.space18)
    ) {
        MobileSectionHeader(
            title = if (isEn) item.labelEn else item.labelVi,
            actionText = if (isEn) "View all" else "Xem tất cả",
            onTitleClick = openModuleEvents,
            onActionClick = openModuleEvents
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onPlaced { coords ->
                    currentOnSwipeZoneChanged(
                        zoneKey,
                        if (hasHorizontalScrollableItems) coords else null
                    )
                }
        ) {
            if (isLoading && events.isEmpty()) {
                if (isTabActive) {
                    val shimmerProgress = rememberShimmerProgressState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.space10),
                        horizontalArrangement = Arrangement.spacedBy(spacing.space10)
                    ) {
                        repeat(2) {
                            Box(
                                modifier = Modifier
                                    .width(cardWidth)
                                    .aspectRatio(HOME_CAROUSEL_ASPECT_RATIO)
                                    .clip(cardShape)
                                    .shimmerBackground(shimmerProgress, colors.isDark)
                            )
                        }
                    }
                }
            } else if (events.isEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.space10)
                        .clip(cardShape)
                        .background(colors.surface)
                        .border(spacing.borderMedium, colors.border, cardShape)
                        .clickable(onClick = openModuleEvents)
                        .padding(horizontal = spacing.space14, vertical = spacing.space12),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isEn) "Tap to view $formattedCount events" else "Chạm để xem $formattedCount sự kiện",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted
                    )
                    Icon(
                        imageVector = LucideIcons.ChevronRight,
                        contentDescription = null,
                        tint = colors.textPlaceholder,
                        modifier = Modifier.size(spacing.iconXSmall)
                    )
                }
            } else {
                val rowState = rememberLazyListState()
                val snapFlingBehavior = rememberSingleEventSnapFlingBehavior(
                    lazyListState = rowState,
                    maxSnapIndex = events.lastIndex
                )

                LaunchedEffect(events.size) {
                    if (events.isNotEmpty() && rowState.firstVisibleItemIndex >= events.size) {
                        rowState.scrollToItem(0)
                    }
                }

                LazyRow(
                    state = rowState,
                    flingBehavior = snapFlingBehavior,
                    contentPadding = PaddingValues(
                        start = spacing.space10,
                        end = endContentPadding
                    ),
                    horizontalArrangement = Arrangement.spacedBy(spacing.space10),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(
                        items = events,
                        key = { ev -> "shelf_${item.filterSensorType}_${ev.serviceId}_${ev.id}_${ev.time}" },
                        contentType = { "shelf_event_grid_card" }
                    ) { event ->
                        val cameraName = event.cameraUuidOrNull?.let { cameraMap[it] }.orEmpty()
                        val showEntityBadge = remember(event.serviceId, event.id, event.time) {
                            shouldShowShelfEntityBadge(event)
                        }
                        Box(modifier = Modifier.width(cardWidth)) {
                            MobileEventCard(
                                event = event,
                                cameraName = cameraName,
                                isListView = false,
                                enableSharedElement = false,
                                showGridBadge = showEntityBadge,
                                imageWidthPx = HOME_SHELF_IMAGE_WIDTH_PX,
                                imageHeightPx = HOME_SHELF_IMAGE_HEIGHT_PX,
                                onClick = { onEventClick(event) }
                            )
                        }
                    }

                    if (showViewMoreTile) {
                        item(
                            key = "shelf_more_${item.filterSensorType}",
                            contentType = "shelf_view_more"
                        ) {
                            Column(
                                modifier = Modifier
                                    .width(viewMoreTileWidth)
                                    .height(cardHeight)
                                    .clip(cardShape)
                                    .background(colors.surface)
                                    .border(spacing.borderMedium, colors.border, cardShape)
                                    .clickable(onClick = openModuleEvents)
                                    .padding(spacing.space10),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(spacing.iconButton)
                                        .clip(CircleShape)
                                        .background(moduleColor.copy(alpha = if (colors.isDark) 0.22f else 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = LucideIcons.ChevronRight,
                                        contentDescription = null,
                                        tint = moduleColor,
                                        modifier = Modifier.size(spacing.iconRegular)
                                    )
                                }
                                Spacer(modifier = Modifier.height(spacing.space8))
                                Text(
                                    text = if (isEn) "View all" else "Xem tất cả",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textTitle,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * SnapFlingBehavior chuyển từng sự kiện một theo mép trái (SnapPosition.Start),
 * không bao giờ dừng nửa vời giữa 2 thẻ sự kiện và không snap riêng vào ô "Xem tất cả".
 */
@Composable
private fun rememberSingleEventSnapFlingBehavior(
    lazyListState: LazyListState,
    maxSnapIndex: Int
): FlingBehavior {
    val density = LocalDensity.current
    val snapLayoutInfoProvider = remember(lazyListState, maxSnapIndex, density) {
        val minFlingVelocityPx = with(density) { 280.dp.toPx() }
        object : SnapLayoutInfoProvider {
            override fun calculateApproachOffset(velocity: Float, decayOffset: Float): Float = 0f

            override fun calculateSnapOffset(velocity: Float): Float {
                val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
                if (visibleItems.isEmpty()) return 0f

                var lowerBoundOffset = Float.NEGATIVE_INFINITY
                var upperBoundOffset = Float.POSITIVE_INFINITY

                for (i in visibleItems.indices) {
                    val item = visibleItems[i]
                    if (item.index > maxSnapIndex) continue
                    val distance = item.offset.toFloat()
                    if (distance <= 0f && distance > lowerBoundOffset) {
                        lowerBoundOffset = distance
                    }
                    if (distance >= 0f && distance < upperBoundOffset) {
                        upperBoundOffset = distance
                    }
                }

                if (lowerBoundOffset == Float.NEGATIVE_INFINITY && upperBoundOffset == Float.POSITIVE_INFINITY) {
                    return 0f
                }
                if (lowerBoundOffset == Float.NEGATIVE_INFINITY) return upperBoundOffset
                if (upperBoundOffset == Float.POSITIVE_INFINITY) return lowerBoundOffset

                return when {
                    velocity > minFlingVelocityPx -> upperBoundOffset
                    velocity < -minFlingVelocityPx -> lowerBoundOffset
                    else -> if (abs(lowerBoundOffset) <= abs(upperBoundOffset)) {
                        lowerBoundOffset
                    } else {
                        upperBoundOffset
                    }
                }
            }
        }
    }

    return rememberSnapFlingBehavior(snapLayoutInfoProvider = snapLayoutInfoProvider)
}

/**
 * Trên từng hàng bài AI (đã có tên bài ở tiêu đề phía trên):
 * - Ẩn badge lặp lại tên bài AI (vd. "Rời vị trí", "Xâm nhập", "Phân tích mật độ", "Tụ tập đám đông"...).
 * - Chỉ giữ badge khi đó là định danh đối tượng cụ thể: tên người / người lạ (Khuôn mặt) hoặc số biển số xe (LPR).
 */
private fun shouldShowShelfEntityBadge(event: EventData): Boolean {
    if (AiTypeHelper.isFaceEvent(event)) {
        return AiTypeHelper.isStranger(event) || AiTypeHelper.extractFacePersonName(event).isNotEmpty()
    }
    val t = event.type.lowercase()
    if (t.contains("lpr") || t.contains("plate") || t.contains("license") || t.contains("vehicle")) {
        val plate = event.data?.plate?.plate ?: event.data?.plate?.number ?: event.plate
        return !plate.isNullOrBlank()
    }
    return false
}

private fun formatGroupedCount(value: Long, isEn: Boolean): String {
    if (value in -999L..999L) return value.toString()
    val sep = if (isEn) ',' else '.'
    val raw = value.toString()
    val startIdx = if (raw[0] == '-') 1 else 0
    val digits = raw.length - startIdx
    if (digits <= 3) return raw
    val sb = StringBuilder(raw.length + (digits - 1) / 3)
    if (startIdx == 1) sb.append('-')
    for (i in startIdx until raw.length) {
        val posFromStart = i - startIdx
        if (posFromStart > 0 && (digits - posFromStart) % 3 == 0) {
            sb.append(sep)
        }
        sb.append(raw[i])
    }
    return sb.toString()
}

fun findAllBoxes(event: EventData): List<List<Float>> {
    val d = event.data ?: return emptyList()
    val boxes = ArrayList<List<Float>>(8)

    fun addBox(b: List<Double>?) {
        if (b != null && b.size >= 4) {
            boxes.add(b.map { it.toFloat() })
        }
    }

    addBox(d.box)
    addBox(d.uniform?.cropBox)
    addBox(d.face?.cropBox)
    addBox(d.plate?.cropBox)
    addBox(d.attribute?.cropBox)
    addBox(d.objectData?.cropBox)
    d.boxes?.forEach { item -> addBox(item.box) }

    return if (boxes.size <= 1) boxes else boxes.distinct()
}

fun getEventCropUrls(event: EventData): List<String> {
    val d = event.data ?: return emptyList()
    val containerId = d.containerId ?: return emptyList()
    val domain = UserSession.domain.trimEnd('/')
    val serviceId = event.serviceId
    val urls = ArrayList<String>(4)

    fun addUrl(file: String?) {
        if (!file.isNullOrEmpty() && file != d.image) {
            val resolved = if (file.startsWith("http")) file else "$domain/Data/api/Data/Media/$serviceId/$containerId/$file"
            if (resolved !in urls) urls.add(resolved)
        }
    }

    (d.plate?.deblurCropImage ?: d.plate?.cropImage)?.let(::addUrl)
    if (urls.isEmpty()) {
        addUrl(d.objectData?.cropImage)
        addUrl(d.uniform?.cropImage)
        val faceCrop = d.face?.cropImage ?: d.faceCrop
        if (faceCrop != null) {
            addUrl(faceCrop)
        } else if (d.cropImage != d.image) {
            addUrl(d.cropImage)
        }
        addUrl(d.attribute?.cropImage)
    }
    return urls
}

fun getEventImageUrl(event: EventData): String? {
    val d = event.data ?: return null
    val filename = d.image ?: d.cropImage ?: return null
    if (filename.startsWith("http")) return filename
    val containerId = d.containerId ?: return null
    val domain = UserSession.domain.trimEnd('/')
    return "$domain/Data/api/Data/Media/${event.serviceId}/$containerId/$filename"
}

fun getEventVideoUrl(event: EventData): String? {
    val d = event.data ?: return null
    val filename = d.video ?: return null
    if (filename.startsWith("http")) return filename
    val containerId = d.containerId ?: return null
    val domain = UserSession.domain.trimEnd('/')
    return "$domain/Data/api/Data/Media/${event.serviceId}/$containerId/$filename"
}
