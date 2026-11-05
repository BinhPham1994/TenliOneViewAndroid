package com.tenli.oneview.ui.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.items
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
import com.tenli.oneview.ui.component.MobileViewToggleButton
import com.tenli.oneview.ui.component.WaveDotsLoading
import com.tenli.oneview.ui.component.rememberIsEnglish
import com.tenli.oneview.ui.theme.LucideIcons
import com.tenli.oneview.ui.theme.ThemeManager
import com.tenli.oneview.ui.utils.AiTypeHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
    listState: androidx.compose.foundation.lazy.LazyListState = androidx.compose.foundation.lazy.rememberLazyListState(),
    onEventClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isEn = rememberIsEnglish()
    var isRefreshing by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.syncViewModeFromPrefs()
    }

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            isRefreshing = false
        }
    }

    val hasActiveFilter = uiState.selectedServiceId != null || uiState.selectedFilter != TimeFilter.TODAY

    val cameraMap = remember(uiState.cameraList) {
        uiState.cameraList.associateBy({ it.extra?.uuid }, { it.name })
    }

    // Strictly take at most 5 recent events matching permittedEvents.slice(0, 5)
    val recentFiveEvents = remember(uiState.recentEvents) {
        uiState.recentEvents.take(5)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Sticky Mobile Header Bar matching .mobile-top-bar (position: sticky; top: 0; z-index: 50)
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
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.fetchDashboardData()
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LazyColumn(
                state = listState,
                userScrollEnabled = true,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 2.dp, bottom = 130.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 2x2 Stat Grid matching MobileHomePage.vue
                item(key = "home_stat_grid", contentType = "stat_grid") {
                    // Total detections matching MobileHomePage.vue totalDetectionsCount (no thousand dot formatting)
                    val totalEventsNum = remember(uiState.eventsByType, uiState.overviewStats) {
                        val sumByType = uiState.eventsByType
                            .filter { AiTypeHelper.isAiTypeEnabled(it.type) }
                            .sumOf { it.count.toString().toDoubleOrNull()?.toLong() ?: 0L }
                        if (sumByType > 0L) {
                            sumByType
                        } else {
                            uiState.overviewStats
                                .find { it.label == "event-count" && it.tag == "all" }
                                ?.count?.toString()?.toDoubleOrNull()?.toLong() ?: 0L
                        }
                    }
                    val totalEventsStr = totalEventsNum.toString()

                    val totalCamerasFromOverview = uiState.overviewStats
                        .find { it.label == "camera-count" && it.tag == "all" }
                        ?.count?.toString()?.toDoubleOrNull()?.toInt() ?: 0
                    val totalCameras = if (uiState.cameraList.isNotEmpty()) uiState.cameraList.size else totalCamerasFromOverview

                    // Online cameras matching MobileHomePage.vue onlineCamerasCount
                    val onlineCameras = remember(uiState.cameraList, uiState.cameraStatusMap, totalCameras) {
                        if (uiState.cameraList.isEmpty()) {
                            totalCameras
                        } else {
                            uiState.cameraList.count { cam ->
                                val wsStatus = uiState.cameraStatusMap["cam-0-${cam.id}"]
                                    ?: uiState.cameraStatusMap["cam-${cam.vmsId}-${cam.cameraId}"]
                                    ?: uiState.cameraStatusMap["cam-${cam.vmsId}-${cam.id}"]
                                    ?: uiState.cameraStatusMap["cam-${cam.id}"]

                                when (wsStatus) {
                                    "green" -> true
                                    "red", "orange" -> false
                                    else -> {
                                        val issues = cam.extra?.latestReport?.issues
                                        if (issues != null) {
                                            !issues.contains("loi_ket_noi") && !issues.contains("mat_ket_noi")
                                        } else {
                                            val legacyStatus = cam.extra?.status
                                            legacyStatus != "red" && legacyStatus != "offline"
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Recording cameras matching MobileHomePage.vue recordingCamerasCount
                    val recordingCameras = remember(uiState.cameraList, uiState.recordStatusMap) {
                        uiState.cameraList.count { cam ->
                            val wsRecStatus = uiState.recordStatusMap["cam-0-${cam.id}"]
                                ?: uiState.recordStatusMap["cam-${cam.vmsId}-${cam.cameraId}"]
                                ?: uiState.recordStatusMap["cam-${cam.vmsId}-${cam.id}"]
                                ?: uiState.recordStatusMap["cam-${cam.id}"]

                            if (wsRecStatus != null) {
                                wsRecStatus == "green"
                            } else {
                                cam.enableRecord ||
                                    cam.extra?.recording == true ||
                                    cam.extra?.recordStatus == "green"
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MobileStatCard(
                                label = if (isEn) "TOTAL EVENTS" else "TỔNG SỰ KIỆN",
                                mainValue = totalEventsStr,
                                subValue = null,
                                accentColor = Color(0xFF0980EE),
                                iconGradient = listOf(Color(0xFF0D8BF2), Color(0xFF0284C7)),
                                icon = LucideIcons.Shield,
                                modifier = Modifier.weight(1f)
                            )
                            MobileStatCard(
                                label = if (isEn) "ONLINE CAMERAS" else "CAMERA HOẠT ĐỘNG",
                                mainValue = onlineCameras.toString(),
                                subValue = "/$totalCameras",
                                accentColor = Color(0xFF10B981),
                                iconGradient = listOf(Color(0xFF10B981), Color(0xFF059669)),
                                icon = LucideIcons.Video,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MobileStatCard(
                                label = if (isEn) "ACTIVE AI TASKS" else "BÀI AI ĐANG CHẠY",
                                mainValue = uiState.runningAiTasks.toString(),
                                subValue = "/${uiState.totalAiTasks}",
                                accentColor = Color(0xFFA855F7),
                                iconGradient = listOf(Color(0xFFA855F7), Color(0xFF9333EA)),
                                icon = LucideIcons.Settings,
                                modifier = Modifier.weight(1f)
                            )
                            MobileStatCard(
                                label = if (isEn) "RECORDING CAMERAS" else "CAMERA GHI HÌNH",
                                mainValue = recordingCameras.toString(),
                                subValue = "/$totalCameras",
                                accentColor = Color(0xFF0EA5E9),
                                iconGradient = listOf(Color(0xFF0EA5E9), Color(0xFF0284C7)),
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
                            .padding(horizontal = 10.dp)
                            .padding(top = 6.dp, bottom = 0.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEn) "Recent events" else "Sự kiện gần đây",
                            fontSize = 17.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        MobileViewToggleButton(
                            isListView = uiState.isListView,
                            onToggle = { viewModel.toggleViewMode() }
                        )
                    }
                }

                // Recent Events List (strictly max 5 events) / Empty / Loading
                if (uiState.isLoading && recentFiveEvents.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp)
                                .height(220.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            WaveDotsLoading()
                        }
                    }
                } else if (recentFiveEvents.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp)
                                .height(220.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CommonEmptyState(
                                text = if (isEn) "No events in this time range" else "Không có sự kiện nào trong khoảng thời gian này"
                            )
                        }
                    }
                } else {
                    items(
                        items = recentFiveEvents,
                        key = { event -> "home_ev_${event.serviceId}_${event.id}_${event.time}" },
                        contentType = { "event_card" }
                    ) { event ->
                        val cameraName = cameraMap[event.data?.cameraUUID] ?: "Camera"
                        Box(modifier = Modifier.padding(horizontal = 10.dp)) {
                            MobileEventCard(
                                event = event,
                                cameraName = cameraName,
                                isListView = uiState.isListView,
                                enableSharedElement = false,
                                onClick = {
                                    com.tenli.oneview.data.local.EventCacheManager.cacheClickedEvent(
                                        event = event,
                                        cameras = uiState.cameraList,
                                        aiServices = uiState.aiServices
                                    )
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
            onReset = {
                viewModel.resetFilters()
            },
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
    val themeMode by ThemeManager.themeModeFlow.collectAsStateWithLifecycle()
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        0 -> false
        1 -> true
        else -> isSystemDark
    }
    val labelColor = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155)
    val fullValue = if (!subValue.isNullOrEmpty()) "$mainValue$subValue" else mainValue

    Column(
        modifier = modifier
            .shadow(
                elevation = if (isDark) 0.dp else 1.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color.Black.copy(alpha = 0.03f),
                spotColor = Color.Black.copy(alpha = 0.04f)
            )
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 16.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.linearGradient(iconGradient)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(21.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = label.uppercase(),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = labelColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            letterSpacing = 0.2.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = fullValue,
            color = accentColor,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)
@Composable
fun RecentEventItem(
    event: EventData,
    cameraName: String,
    aiServiceName: String? = null,
    isSelected: Boolean = false,
    enableSharedElement: Boolean = true,
    onClick: () -> Unit
) {
    MobileEventCard(
        event = event,
        cameraName = cameraName,
        isListView = true,
        isSelected = isSelected,
        enableSharedElement = enableSharedElement,
        onClick = onClick
    )
}

private val eventTimeFormat = object : ThreadLocal<SimpleDateFormat>() {
    override fun initialValue(): SimpleDateFormat {
        return SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault())
    }
}

private fun formatEventTime(time: Double?): String {
    if (time == null || time == 0.0) return "N/A"
    val timeMillis = if (time < 100000000000.0) (time * 1000).toLong() else time.toLong()
    return eventTimeFormat.get()!!.format(Date(timeMillis))
}

fun findAllBoxes(event: EventData): List<List<Float>> {
    val boxes = mutableListOf<List<Float>>()

    val addBox = { b: List<Double>? ->
        if (b != null && b.size >= 4) {
            boxes.add(b.map { it.toFloat() })
        }
    }

    addBox(event.data?.box)

    val d = event.data
    addBox(d?.uniform?.cropBox)
    addBox(d?.face?.cropBox)
    addBox(d?.plate?.cropBox)
    addBox(d?.attribute?.cropBox)
    addBox(d?.objectData?.cropBox)

    d?.boxes?.forEach { item ->
        addBox(item.box)
    }

    return boxes.distinctBy { it.joinToString(",") }
}

fun getEventCropUrls(event: EventData): List<String> {
    val urls = mutableListOf<String>()
    val domain = UserSession.domain.trimEnd('/')
    val serviceId = event.serviceId
    val containerId = event.data?.containerId ?: return emptyList()

    val addUrl = { file: String? ->
        if (!file.isNullOrEmpty() && file != event.data?.image) {
            if (!file.startsWith("http")) {
                urls.add("$domain/Data/api/Data/Media/$serviceId/$containerId/$file")
            } else {
                urls.add(file)
            }
        }
    }

    event.data?.plate?.deblurCropImage?.let { addUrl(it) } ?: event.data?.plate?.cropImage?.let { addUrl(it) }
    if (urls.isEmpty()) {
        addUrl(event.data?.objectData?.cropImage)
        addUrl(event.data?.uniform?.cropImage)
        val faceCrop = event.data?.face?.cropImage ?: event.data?.faceCrop
        if (faceCrop != null) {
            addUrl(faceCrop)
        } else if (event.data?.cropImage != event.data?.image) {
            addUrl(event.data?.cropImage)
        }
        addUrl(event.data?.attribute?.cropImage)
    }
    return urls.distinct()
}

fun getEventImageUrl(event: EventData): String? {
    val filename = event.data?.image ?: event.data?.cropImage ?: return null
    if (filename.startsWith("http")) return filename
    val domain = UserSession.domain.trimEnd('/')
    val serviceId = event.serviceId
    val containerId = event.data?.containerId ?: return null
    return "$domain/Data/api/Data/Media/$serviceId/$containerId/$filename"
}

fun getEventVideoUrl(event: EventData): String? {
    val filename = event.data?.video ?: return null
    if (filename.startsWith("http")) return filename
    val domain = UserSession.domain.trimEnd('/')
    val serviceId = event.serviceId
    val containerId = event.data?.containerId ?: return null
    return "$domain/Data/api/Data/Media/$serviceId/$containerId/$filename"
}
