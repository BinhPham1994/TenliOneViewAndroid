package com.tenli.oneview.ui.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.model.network.CameraModel
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.ui.theme.BrandPrimary
import com.tenli.oneview.ui.utils.AiTypeHelper
import com.tenli.oneview.ui.component.TimeFilterDropdown
import com.tenli.oneview.ui.component.AiServiceFilterDropdown
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
    listState: androidx.compose.foundation.lazy.LazyListState = androidx.compose.foundation.lazy.rememberLazyListState(),
    onEventClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isRefreshing by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            isRefreshing = false
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AiServiceFilterDropdown(
                services = uiState.aiServices,
                selectedServiceId = uiState.selectedServiceId,
                onServiceSelected = { viewModel.setServiceFilter(it) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            TimeFilterDropdown(
                selectedFilter = uiState.selectedFilter,
                onFilterSelected = { viewModel.setTimeFilter(it) }
            )
        }

        androidx.compose.material3.pulltorefresh.PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.fetchDashboardData()
            },
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 4.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

        // 4 Thẻ thống kê
        item {
            // Lấy giá trị từ Overview
            fun Any?.formatCount(): String {
                val num = (this as? Number)?.toLong() ?: this?.toString()?.toDoubleOrNull()?.toLong() ?: 0L
                return java.text.NumberFormat.getNumberInstance(java.util.Locale("vi", "VN")).format(num)
            }

            val totalEvents = uiState.overviewStats.find { it.label == "event-count" && it.tag == "all" }?.count.formatCount()
            val totalCameras = uiState.overviewStats.find { it.label == "camera-count" && it.tag == "all" }?.count.formatCount()
            val totalAI = uiState.overviewStats.find { it.label == "monitor-count" && it.tag == "all" }?.count.formatCount()
            val systemStatus = "An toàn" // Default mock as per requirement

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Tổng sự kiện",
                        value = totalEvents,
                        iconColor = Color(0xFFF97316),
                        icon = androidx.compose.material.icons.Icons.Default.Notifications,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Camera",
                        value = totalCameras,
                        iconColor = Color(0xFF52C41A),
                        icon = androidx.compose.material.icons.Icons.Default.Videocam,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Tiến trình AI",
                        value = totalAI,
                        iconColor = Color(0xFFFF1493), // Vibrant Deep Pink
                        icon = androidx.compose.material.icons.Icons.Default.Memory,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Trạng thái",
                        value = systemStatus,
                        iconColor = Color(0xFF13C2C2),
                        icon = androidx.compose.material.icons.Icons.Default.VerifiedUser,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Biểu đồ: Sự kiện theo thời gian và bài AI (Cuộn ngang)
        item {
            val isMultiDay = uiState.selectedFilter == TimeFilter.LAST_7_DAYS || uiState.selectedFilter == TimeFilter.LAST_30_DAYS
            val overTimeData = uiState.eventsOverTime.map {
                val label = if (isMultiDay) {
                    val dateParts = it.date.split("T").first().split("-")
                    if (dateParts.size >= 3) "${dateParts[2]}/${dateParts[1]}" else it.date
                } else {
                    "${it.hour}h"
                }
                Pair(label, it.value.toFloatOrNull() ?: 0f)
            }
            
            val byTypeData = uiState.eventsByType
                .filter { 
                    !it.type.contains("online", ignoreCase = true) && 
                    !it.type.contains("offline", ignoreCase = true) &&
                    !it.type.contains("security-enable", ignoreCase = true) &&
                    !it.type.contains("security-disable", ignoreCase = true)
                }
                .map {
                    Pair(AiTypeHelper.getTypeName(it.type), it.count.toFloat())
                }
                .sortedByDescending { it.second }

            val byCameraData = uiState.eventsByCamera
                .map {
                    val cameraName = uiState.cameraList.find { cam -> cam.extra?.uuid == it.cameraUUID }?.name 
                                     ?: it.cameraUUID.take(6).let { uuidStr -> "Camera $uuidStr" }
                    Pair(cameraName, it.count.toFloat())
                }
                .sortedByDescending { it.second }
                .take(10)

            val configuration = androidx.compose.ui.platform.LocalConfiguration.current

            val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { 3 })
            androidx.compose.foundation.pager.HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(end = configuration.screenWidthDp.dp * 0.06f),
                pageSpacing = 8.dp
            ) { page ->
                when (page) {
                    0 -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            SectionTitle("Phân bố sự kiện theo thời gian")
                            HomeLineChart(
                                data = overTimeData,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f)
                                    .background(MaterialTheme.colorScheme.surface, shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                                    .padding(8.dp)
                            )
                        }
                    }
                    1 -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            SectionTitle("Phân bố sự kiện theo bài AI")
                            HomeHorizontalBarChart(
                                data = byTypeData,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f)
                                    .background(MaterialTheme.colorScheme.surface, shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                                    .padding(8.dp)
                            )
                        }
                    }
                    2 -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            SectionTitle("Phân bố sự kiện theo camera")
                            HomeHorizontalBarChart(
                                data = byCameraData,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f)
                                    .background(MaterialTheme.colorScheme.surface, shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Danh sách sự kiện gần đây
        item {
            Column {
                SectionTitle("Sự kiện gần đây")
                
                if (uiState.error != null) {
                    Text(
                        text = uiState.error ?: "",
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp)
                    )
                } else if (uiState.recentEvents.isEmpty()) {
                    com.tenli.oneview.ui.component.CommonEmptyState(
                        text = "Chưa có sự kiện nào",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp)
                    )
                } else {
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val cameraMap = androidx.compose.runtime.remember(uiState.cameraList) {
                            uiState.cameraList.associateBy({ it.extra?.uuid }, { it.name })
                        }
                        val serviceMap = androidx.compose.runtime.remember(uiState.aiServices) {
                            uiState.aiServices.associateBy({ it.id }, { it.name })
                        }
                        uiState.recentEvents.forEachIndexed { index, event ->
                            val cameraName = cameraMap[event.data?.cameraUUID] ?: "Camera"
                            val aiServiceName = serviceMap[event.serviceId] ?: "Unknown Service"
                            RecentEventItem(event, cameraName, aiServiceName) {
                                onEventClick(event.id.toString())
                            }
                        }
                    }
                }
            }
        }
    } // End of LazyColumn


    } // End of PullToRefreshBox
} // End of Column
} // End of HomeScreen

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)
@Composable
fun RecentEventItem(event: EventData, cameraName: String, aiServiceName: String? = null, isSelected: Boolean = false, enableSharedElement: Boolean = true, onClick: () -> Unit) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(start = 4.dp, top = 4.dp, bottom = 4.dp, end = 4.dp), // Ảnh sát mép 4dp, chữ cách lề phải 4dp
        verticalAlignment = Alignment.CenterVertically
    ) {
            val sharedTransitionScope = com.tenli.oneview.ui.navigation.LocalSharedTransitionScope.current
            val animatedVisibilityScope = com.tenli.oneview.ui.navigation.LocalAnimatedVisibilityScope.current
            
                var imageModifier = Modifier
                    .width(140.dp)
                    .aspectRatio(16f / 9f)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                
            if (enableSharedElement && sharedTransitionScope != null && animatedVisibilityScope != null) {
                with(sharedTransitionScope) {
                    imageModifier = imageModifier.sharedElement(
                        sharedContentState = rememberSharedContentState(key = "event_image_${event.id}"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                }
            }

            com.tenli.oneview.ui.component.VideoFrameImage(
                url = getEventImageUrl(event),
                modifier = imageModifier,
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                val timeStr = androidx.compose.runtime.remember(event.time) {
                    formatEventTime(event.time)
                }
                val parts = timeStr.split(" ")
                val timePart = if (parts.isNotEmpty()) parts[0] else ""
                val datePart = if (parts.size > 1) parts[1] else ""
                
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = cameraName,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = timePart,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!aiServiceName.isNullOrBlank()) {
                        Text(
                            text = aiServiceName,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    if (datePart.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = datePart,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                val aiColor = AiTypeHelper.getAiColor(event)
                Text(
                    text = AiTypeHelper.getEventName(event),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier
                        .background(color = aiColor, shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
    }
}

private val eventTimeFormat = object : ThreadLocal<SimpleDateFormat>() {
    override fun initialValue(): SimpleDateFormat {
        return SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault())
    }
}

private fun formatEventTime(time: Double?): String {
    if (time == null || time == 0.0) return "N/A"
    // API returns time in seconds as Double
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

    // Root level boxes
    addBox(event.data?.box)

    // Nested AI model boxes
    val d = event.data
    addBox(d?.uniform?.cropBox)
    addBox(d?.face?.cropBox)
    addBox(d?.plate?.cropBox)
    addBox(d?.attribute?.cropBox)
    addBox(d?.objectData?.cropBox)

    // Array of boxes
    d?.boxes?.forEach { item ->
        addBox(item.box)
    }


    // Deduplicate
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
