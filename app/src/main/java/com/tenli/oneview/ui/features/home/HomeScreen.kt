package com.tenli.oneview.ui.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tenli.oneview.R
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.ui.component.CommonEmptyState
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
import com.tenli.oneview.ui.theme.BrandAccent
import com.tenli.oneview.ui.theme.InfoSkyDeep
import com.tenli.oneview.ui.theme.LucideIcons
import com.tenli.oneview.ui.theme.OrangeDeep
import com.tenli.oneview.ui.theme.OrangeUserAccent
import com.tenli.oneview.ui.theme.PurpleAccent
import com.tenli.oneview.ui.theme.SuccessGreen
import com.tenli.oneview.ui.theme.appColors
import com.tenli.oneview.ui.theme.spacing

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
    listState: LazyListState = rememberLazyListState(),
    isTabActive: Boolean = true,
    onEventClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isEn = rememberIsEnglish()
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.appColors
    var showFilterSheet by remember { mutableStateOf(false) }

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
            LazyColumn(
                state = listState,
                userScrollEnabled = true,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = spacing.space2, bottom = HomeBottomContentPadding)
            ) {
                // 2x2 Stat Grid matching MobileHomePage.vue (số liệu đã tính sẵn trên background thread của ViewModel)
                item(key = "home_stat_grid", contentType = "stat_grid") {
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
                                modifier = Modifier.weight(1f)
                            )
                            MobileStatCard(
                                label = if (isEn) "ONLINE CAMERAS" else "CAMERA HOẠT ĐỘNG",
                                mainValue = uiState.onlineCamerasCount.toString(),
                                subValue = "/$totalCameras",
                                accentColor = SuccessGreen,
                                iconGradient = StatOnlineCamerasGradient,
                                icon = LucideIcons.Video,
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
                                modifier = Modifier.weight(1f)
                            )
                            MobileStatCard(
                                label = if (isEn) "RECORDING CAMERAS" else "CAMERA GHI HÌNH",
                                mainValue = uiState.recordingCamerasCount.toString(),
                                subValue = "/$totalCameras",
                                accentColor = OrangeUserAccent,
                                iconGradient = StatRecordingCamerasGradient,
                                icon = LucideIcons.Disc,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Section Header: Sự kiện gần đây + View Mode Toggle Button
                item(key = "home_recent_header", contentType = "header") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = spacing.space16,
                                end = spacing.space12,
                                top = spacing.space14,
                                bottom = spacing.space6
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
                        Box(
                            modifier = Modifier
                                .size(spacing.iconButtonSmall)
                                .clip(RoundedCornerShape(spacing.radiusSmall))
                                .clickable(onClick = viewModel::toggleViewMode),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (uiState.isListView) LucideIcons.LayoutGrid else LucideIcons.List,
                                contentDescription = if (uiState.isListView) "Grid View" else "List View",
                                tint = colors.iconDefault,
                                modifier = Modifier.size(spacing.iconSmallMedium)
                            )
                        }
                    }
                }

                // Recent Events List (strictly max 5 events) / Empty / Loading
                if (uiState.isLoading && recentFiveEvents.isEmpty()) {
                    item(key = "home_loading", contentType = "loading") {
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
                    }
                } else if (recentFiveEvents.isEmpty()) {
                    item(key = "home_empty", contentType = "empty") {
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
                    }
                } else {
                    itemsIndexed(
                        items = recentFiveEvents,
                        key = { _, event -> "home_ev_${event.serviceId}_${event.id}_${event.time}" },
                        contentType = { _, _ -> "event_card" }
                    ) { index, event ->
                        val cameraName = event.cameraUuidOrNull?.let { cameraMap[it] }.orEmpty()
                        Box(
                            modifier = Modifier
                                .padding(horizontal = spacing.space10)
                                .padding(top = if (index == 0) 0.dp else spacing.space10)
                        ) {
                            MobileEventCard(
                                event = event,
                                cameraName = cameraName,
                                isListView = uiState.isListView,
                                enableSharedElement = false,
                                onClick = {
                                    viewModel.onEventSelected(event)
                                    onEventClick(event.id.toString())
                                }
                            )
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
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val cardShape = remember(spacing.radiusCompact) { RoundedCornerShape(spacing.radiusCompact) }
    val gradientBrush = remember(iconGradient) { Brush.linearGradient(iconGradient) }
    val fullValue = if (!subValue.isNullOrEmpty()) "$mainValue$subValue" else mainValue

    Column(
        modifier = modifier
            .shadow(
                elevation = if (colors.isDark) 0.dp else spacing.space1,
                shape = cardShape,
                ambientColor = Color.Black.copy(alpha = 0.03f),
                spotColor = Color.Black.copy(alpha = 0.04f)
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
