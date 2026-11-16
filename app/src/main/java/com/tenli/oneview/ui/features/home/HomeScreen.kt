package com.tenli.oneview.ui.features.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tenli.oneview.R
import com.tenli.oneview.data.local.UserPermissionManager
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.ui.component.CommonEmptyState
import com.tenli.oneview.ui.component.IconBadge
import com.tenli.oneview.ui.component.MobileEventCard
import com.tenli.oneview.ui.component.MobileFilterBottomSheet
import com.tenli.oneview.ui.component.MobileTopActionButton
import com.tenli.oneview.ui.component.MobileTopBar
import com.tenli.oneview.ui.component.MobileTopPopupManager
import com.tenli.oneview.ui.component.MobileViewToggleButton
import com.tenli.oneview.ui.component.WaveDotsLoading
import com.tenli.oneview.ui.component.rememberIsEnglish
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
import com.tenli.oneview.ui.theme.spacing
import com.tenli.oneview.ui.utils.AiTypeHelper

// Gradient stops của 4 thẻ thống kê (đồng bộ với MobileHomePage.vue)
private val StatBlueGradientStart = Color(0xFF0D8BF2)
private val StatGreenGradientEnd = Color(0xFF059669)
private val StatPurpleGradientEnd = Color(0xFF9333EA)

private val StatTotalEventsGradient = listOf(StatBlueGradientStart, InfoSkyDeep)
private val StatOnlineCamerasGradient = listOf(SuccessGreen, StatGreenGradientEnd)
private val StatActiveAiTasksGradient = listOf(PurpleAccent, StatPurpleGradientEnd)
private val StatRecordingCamerasGradient = listOf(OrangeUserAccent, OrangeDeep)

private val HomeBottomContentPadding = 130.dp
private val HomeEmptyStateHeight = 220.dp
private val StatLabelLetterSpacing = 0.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
    scrollState: ScrollState = rememberScrollState(),
    isTabActive: Boolean = true,
    onEventClick: (String) -> Unit = {},
    onNavigateToEvents: (TimeFilter, Int?, String?) -> Unit = { _, _, _ -> },
    onNavigateToMonitor: (CameraQuickFilter) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val permissionState by UserPermissionManager.permissionState.collectAsStateWithLifecycle()
    val isEn = rememberIsEnglish()
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.appColors
    var showFilterSheet by remember { mutableStateOf(false) }
    var isAiBreakdownExpanded by remember { mutableStateOf(false) }

    val currentSelectedFilter by rememberUpdatedState(uiState.selectedFilter)
    val currentSelectedServiceId by rememberUpdatedState(uiState.selectedServiceId)
    val currentOnNavigateToEvents by rememberUpdatedState(onNavigateToEvents)
    val currentOnNavigateToMonitor by rememberUpdatedState(onNavigateToMonitor)
    val currentOnEventClick by rememberUpdatedState(onEventClick)

    LaunchedEffect(Unit) {
        viewModel.syncViewModeFromPrefs()
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { errMsg ->
            MobileTopPopupManager.error(errMsg)
            viewModel.clearError()
        }
    }

    val hasActiveFilter = uiState.selectedServiceId != null || uiState.selectedFilter != TimeFilter.TODAY

    val cameraMap = remember(uiState.cameraList) {
        buildCameraNameMap(uiState.cameraList)
    }

    val recentFiveEvents = uiState.recentEvents

    val handleSelectAiModule: (String) -> Unit = remember {
        { sensorType ->
            currentOnNavigateToEvents(
                currentSelectedFilter,
                currentSelectedServiceId,
                sensorType
            )
        }
    }
    val handleToggleBreakdown: () -> Unit = remember {
        { isAiBreakdownExpanded = !isAiBreakdownExpanded }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.pageBg)
    ) {
        MobileTopBar(
            title = stringResource(id = R.string.tab_home),
            rightAction = {
                MobileTopActionButton(
                    icon = LucideIcons.ListFilter,
                    contentDescription = if (isEn) "Filter" else "Bộ lọc",
                    hasActiveDot = hasActiveFilter,
                    onClick = { showFilterSheet = true }
                )
            }
        )

        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Dùng Column + verticalScroll thay vì LazyColumn cho Dashboard Trang chủ (luôn cố định 4 thẻ thống kê +
            // 1 bảng phân bố AI + tối đa đúng 5 sự kiện gần đây). Toàn bộ RenderNode và ảnh thumbnail được giữ ấm trên GPU,
            // loại bỏ hoàn toàn hiện tượng khựng khung hình do SubcomposeLayout huỷ / dựng lại khối Phân bố AI và Grid thống kê khi cuộn lên xuống.
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(top = spacing.space2, bottom = HomeBottomContentPadding)
            ) {
                // 2x2 Stat Grid matching MobileHomePage.vue (số liệu đã tính sẵn trên background thread của ViewModel)
                val totalCameras = uiState.totalCamerasCount
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
                            mainValue = uiState.totalDetectionsCount.toString(),
                            subValue = null,
                            accentColor = BrandAccent,
                            iconGradient = StatTotalEventsGradient,
                            icon = LucideIcons.Shield,
                            onClick = if (permissionState.canAccessEvents) {
                                { currentOnNavigateToEvents(currentSelectedFilter, currentSelectedServiceId, null) }
                            } else {
                                null
                            },
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
                                {
                                    val hasOffline = totalCameras > 0 && uiState.onlineCamerasCount < totalCameras
                                    currentOnNavigateToMonitor(
                                        if (hasOffline) CameraQuickFilter.OFFLINE else CameraQuickFilter.ALL
                                    )
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
                            onClick = if (permissionState.canAccessEvents) {
                                { currentOnNavigateToEvents(currentSelectedFilter, currentSelectedServiceId, null) }
                            } else {
                                null
                            },
                            modifier = Modifier.weight(1f)
                        )
                        MobileStatCard(
                            label = if (isEn) "RECORDING CAMERAS" else "CAMERA GHI HÌNH",
                            mainValue = uiState.recordingCamerasCount.toString(),
                            subValue = "/$totalCameras",
                            accentColor = OrangeUserAccent,
                            iconGradient = StatRecordingCamerasGradient,
                            icon = LucideIcons.Disc,
                            onClick = if (permissionState.canAccessLiveStream) {
                                { currentOnNavigateToMonitor(CameraQuickFilter.ALL) }
                            } else {
                                null
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // AI Event Breakdown Card ("Phân bố sự kiện AI") — chạm vào bài AI bất kỳ để sang tab Sự kiện đã lọc sẵn
                if (permissionState.canAccessEvents && uiState.aiDistribution.isNotEmpty()) {
                    HomeAiDistributionSection(
                        items = uiState.aiDistribution,
                        isExpanded = isAiBreakdownExpanded,
                        isEn = isEn,
                        onToggleExpand = handleToggleBreakdown,
                        onSelectModule = handleSelectAiModule
                    )
                }

                // Section Header: Sự kiện gần đây + View Mode Toggle Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = spacing.space16,
                            end = spacing.space12,
                            top = spacing.space20,
                            bottom = spacing.space4
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEn) "Recent events" else "Sự kiện gần đây",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    MobileViewToggleButton(
                        isListView = uiState.isListView,
                        onToggle = viewModel::toggleViewMode,
                        size = spacing.iconButtonSmall
                    )
                }

                // Recent Events List (strictly max 5 events) / Empty / Loading
                if (uiState.isLoading && recentFiveEvents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.space10)
                            .height(HomeEmptyStateHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isTabActive) {
                            WaveDotsLoading()
                        }
                    }
                } else if (recentFiveEvents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.space10)
                            .height(HomeEmptyStateHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        CommonEmptyState(
                            text = if (isEn) "No events in this time range" else "Không có sự kiện nào trong khoảng thời gian này"
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.space10),
                        verticalArrangement = Arrangement.spacedBy(spacing.space10)
                    ) {
                        recentFiveEvents.forEach { event ->
                            key("${event.serviceId}_${event.id}_${event.time}") {
                                val cameraName = event.cameraUuidOrNull?.let { cameraMap[it] }.orEmpty()
                                MobileEventCard(
                                    event = event,
                                    cameraName = cameraName,
                                    isListView = uiState.isListView,
                                    enableSharedElement = false,
                                    onClick = {
                                        viewModel.onEventSelected(event)
                                        currentOnEventClick(event.id.toString())
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        MobileFilterBottomSheet(
            showScope = true,
            showTime = true,
            showCamera = false,
            showEventType = false,
            aiServices = uiState.aiServices,
            cameras = uiState.cameraList,
            selectedServiceId = uiState.selectedServiceId,
            selectedTimeFilter = uiState.selectedFilter,
            selectedCameraUuid = null,
            selectedAiType = null,
            onApply = { serviceId, timeFilter, _, _ ->
                viewModel.applyFilters(timeFilter, serviceId)
            },
            onReset = viewModel::resetFilters,
            onDismiss = { showFilterSheet = false }
        )
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
    val fullValue = if (!subValue.isNullOrEmpty()) "$mainValue$subValue" else mainValue
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScaleState = animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.97f else 1f,
        animationSpec = tween(durationMillis = if (isPressed) 45 else 150),
        label = "statCardPressScale"
    )
    val currentOnClick by rememberUpdatedState(onClick)

    Column(
        modifier = modifier
            .graphicsLayer {
                val s = pressScaleState.value
                scaleX = s
                scaleY = s
            }
            .clip(cardShape)
            .background(colors.surface)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
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
            text = fullValue,
            color = accentColor,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun HomeAiDistributionSection(
    items: List<HomeAiDistributionItem>,
    isExpanded: Boolean,
    isEn: Boolean,
    onToggleExpand: () -> Unit,
    onSelectModule: (String) -> Unit
) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val haptic = LocalHapticFeedback.current
    val currentOnToggleExpand by rememberUpdatedState(onToggleExpand)
    val currentOnSelectModule by rememberUpdatedState(onSelectModule)
    val cardShape = remember(spacing.radiusCompact) { RoundedCornerShape(spacing.radiusCompact) }
    val primaryItems = remember(items) { items.take(5) }
    val extraItems = remember(items) { if (items.size > 5) items.drop(5) else emptyList() }
    val canExpand = extraItems.isNotEmpty()
    val chevronRotationState = animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "aiBreakdownChevron"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = spacing.space16,
                    end = spacing.space16,
                    top = spacing.space20,
                    bottom = spacing.space8
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEn) "AI event breakdown" else "Phân bố sự kiện AI",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.space10)
                .clip(cardShape)
                .background(colors.surface)
        ) {
            // Thanh tỉ trọng đa phân đoạn ở đầu thẻ — vẽ trên 1 Canvas duy nhất thay vì tạo N Box + clip riêng lẻ
            if (items.size >= 2) {
                val segmentSpecs = remember(items) {
                    items.map { seg ->
                        seg.percentage.coerceAtLeast(3f) to AiTypeHelper.getAiColor(seg.filterSensorType)
                    }
                }
                val gapDp = spacing.space2
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = spacing.space14,
                            end = spacing.space14,
                            top = spacing.space14,
                            bottom = spacing.space4
                        )
                        .height(spacing.space8)
                        .clip(CircleShape)
                ) {
                    val count = segmentSpecs.size
                    if (count == 0) return@Canvas
                    val gapPx = gapDp.toPx()
                    val cornerPx = 2.dp.toPx()
                    val cornerRadius = CornerRadius(cornerPx, cornerPx)
                    val totalGapPx = gapPx * (count - 1)
                    val availableWidth = (size.width - totalGapPx).coerceAtLeast(0f)
                    var totalWeight = 0f
                    for (i in 0 until count) {
                        totalWeight += segmentSpecs[i].first
                    }
                    if (totalWeight <= 0f) return@Canvas

                    var cursorX = 0f
                    for (i in 0 until count) {
                        val (weight, segColor) = segmentSpecs[i]
                        val segW = if (i == count - 1) {
                            (size.width - cursorX).coerceAtLeast(0f)
                        } else {
                            availableWidth * (weight / totalWeight)
                        }
                        drawRoundRect(
                            color = segColor,
                            topLeft = Offset(cursorX, 0f),
                            size = Size(segW, size.height),
                            cornerRadius = cornerRadius
                        )
                        cursorX += segW + gapPx
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(spacing.space2))
            }

            primaryItems.forEach { item ->
                key(item.filterSensorType) {
                    AiDistributionModuleRow(
                        item = item,
                        isEn = isEn,
                        onSelectModule = currentOnSelectModule
                    )
                }
            }

            if (canExpand) {
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(tween(220)) + fadeIn(tween(180)),
                    exit = shrinkVertically(tween(180)) + fadeOut(tween(140))
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        extraItems.forEach { item ->
                            key(item.filterSensorType) {
                                AiDistributionModuleRow(
                                    item = item,
                                    isEn = isEn,
                                    onSelectModule = currentOnSelectModule
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(
                    color = colors.divider,
                    thickness = spacing.borderThin
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            try {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            } catch (_: Exception) {
                            }
                            currentOnToggleExpand()
                        }
                        .padding(horizontal = spacing.space14, vertical = spacing.space12),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isExpanded) {
                            if (isEn) "Show less" else "Thu gọn"
                        } else {
                            if (isEn) {
                                "View all ${items.size} AI modules (+${extraItems.size})"
                            } else {
                                "Xem tất cả ${items.size} bài AI (+${extraItems.size} bài khác)"
                            }
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.accent
                    )
                    Spacer(modifier = Modifier.width(spacing.space6))
                    Icon(
                        imageVector = LucideIcons.ChevronDown,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier
                            .size(spacing.iconSmall)
                            .graphicsLayer { rotationZ = chevronRotationState.value }
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(if (items.size >= 2) spacing.space4 else spacing.space2))
            }
        }
    }
}

@Composable
private fun AiDistributionModuleRow(
    item: HomeAiDistributionItem,
    isEn: Boolean,
    onSelectModule: (String) -> Unit
) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val haptic = LocalHapticFeedback.current
    val currentOnSelectModule by rememberUpdatedState(onSelectModule)
    val moduleColor = remember(item.filterSensorType) {
        AiTypeHelper.getAiColor(item.filterSensorType)
    }
    val moduleIcon = remember(item.filterSensorType) {
        resolveAiModuleIcon(item.filterSensorType)
    }
    val formattedCount = remember(item.count, isEn) {
        formatGroupedCount(item.count, isEn)
    }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val rowBgState = animateColorAsState(
        targetValue = if (isPressed) colors.controlBg else Color.Transparent,
        animationSpec = tween(durationMillis = if (isPressed) 35 else 160),
        label = "aiDistRowBg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val bg = rowBgState.value
                if (bg.alpha > 0f) {
                    drawRect(bg)
                }
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    try {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    } catch (_: Exception) {
                    }
                    currentOnSelectModule(item.filterSensorType)
                }
            )
            .padding(horizontal = spacing.space14, vertical = spacing.space10),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(
            icon = moduleIcon,
            backgroundColor = moduleColor
        )
        Spacer(modifier = Modifier.width(spacing.space12))
        Text(
            text = if (isEn) item.labelEn else item.labelVi,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = colors.textTitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(spacing.space8))
        Text(
            text = formattedCount,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = colors.textStrong,
            maxLines = 1
        )
        Spacer(modifier = Modifier.width(spacing.space6))
        Icon(
            imageVector = LucideIcons.ChevronRight,
            contentDescription = null,
            tint = colors.textPlaceholder,
            modifier = Modifier.size(spacing.iconXSmall)
        )
    }
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

private fun resolveAiModuleIcon(sensorType: String): ImageVector {
    val t = sensorType.lowercase()
    return when {
        t.contains("fire") || t.contains("smoke") -> LucideIcons.Flame
        t.contains("face") || t.contains("masked") -> LucideIcons.User
        t.contains("crowd") -> LucideIcons.Users
        t.contains("lpr") || t.contains("plate") || t.contains("license") -> LucideIcons.Barcode
        t.contains("parking") || t.contains("vehicle") || t.contains("crossing") || t.contains("wrong") -> LucideIcons.Car
        t.contains("person") || t.contains("intrusion") -> LucideIcons.ShieldAlert
        t.contains("helmet") || t.contains("uniform") || t.contains("clothing") ||
            t.contains("glove") || t.contains("hairnet") -> LucideIcons.Shield
        t.contains("phone") || t.contains("calling") -> LucideIcons.PhoneCall
        t.contains("object") || t.contains("tracking") -> LucideIcons.Eye
        t.contains("heatmap") -> LucideIcons.Layers
        t.contains("encroach") || t.contains("sidewalk") -> LucideIcons.MapPin
        t.contains("violence") || t.contains("fight") || t.contains("fall") ||
            t.contains("smoking") || t.contains("garbage") || t.contains("litter") ||
            t.contains("construction") || t.contains("flood") -> LucideIcons.AlertTriangle
        else -> LucideIcons.Bell
    }
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
