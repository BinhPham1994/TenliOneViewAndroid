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

    if (uiState.isLoading && uiState.overviewStats.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = BrandPrimary, strokeWidth = 6.dp)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tổng quan",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
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
                contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 4.dp, bottom = 16.dp),
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
                                    .background(Color.White, shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
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
                                    .background(Color.White, shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
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
                                    .background(Color.White, shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
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
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.recentEvents.forEachIndexed { index, event ->
                            val cameraName = uiState.cameraList.find { it.extra?.uuid == event.data?.cameraUUID }?.name ?: "Camera"
                            RecentEventItem(event, cameraName) {
                                onEventClick(event.id.toString())
                            }
                        }
                    }
                }
            }
        }
    } // End of LazyColumn

    if (uiState.isLoading && !isRefreshing) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.5f))
                .pointerInput(Unit) {},
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = BrandPrimary, strokeWidth = 6.dp)
        }
    }
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
fun RecentEventItem(event: EventData, cameraName: String, isSelected: Boolean = false, enableSharedElement: Boolean = true, onClick: () -> Unit) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.White)
            .clickable(onClick = onClick)
            .padding(start = 4.dp, top = 4.dp, bottom = 4.dp, end = 12.dp), // Ảnh sát mép 4dp, chữ cách lề phải 12dp
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
                Text(
                    text = cameraName,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                val timeStr = formatEventTime(event.time)
                Text(
                    text = androidx.compose.ui.text.buildAnnotatedString {
                        val parts = timeStr.split(" ")
                        if (parts.size == 2) {
                            withStyle(style = androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.SemiBold, color = Color.DarkGray)) {
                                append(parts[0])
                            }
                            append(" ")
                            append(parts[1])
                        } else {
                            append(timeStr)
                        }
                    },
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                val aiColor = AiTypeHelper.getAiColor(event.type)
                Text(
                    text = AiTypeHelper.getTypeName(event.type),
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
            Spacer(modifier = Modifier.width(12.dp))
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Chi tiết",
                tint = Color.LightGray,
                modifier = Modifier.size(24.dp)
            )
    }
}

private val eventTimeFormat = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault())

private fun formatEventTime(time: Double?): String {
    if (time == null || time == 0.0) return "N/A"
    // API returns time in seconds as Double
    val timeMillis = if (time < 100000000000.0) (time * 1000).toLong() else time.toLong()
    return eventTimeFormat.format(Date(timeMillis))
}

fun getEventImageUrl(event: EventData): String? {
    val filename = event.data?.image ?: event.data?.cropImage ?: return null
    if (filename.startsWith("http")) return filename
    val domain = UserSession.domain.trimEnd('/')
    val serviceId = event.serviceId
    val containerId = event.data?.containerId ?: return null
    return "$domain/Data/api/Data/Media/$serviceId/$containerId/$filename"
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TimeFilterDropdown(
    selectedFilter: TimeFilter,
    onFilterSelected: (TimeFilter) -> Unit
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    Box {
        Row(
            modifier = Modifier
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                .clickable { showBottomSheet = true }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedFilter.title,
                color = Color.Black,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Select Time Filter",
                tint = Color.Black
            )
        }

        if (showBottomSheet) {
            androidx.compose.material3.ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = Color.White,
                dragHandle = { androidx.compose.material3.BottomSheetDefaults.DragHandle() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
                ) {
                    Text(
                        text = "Chọn thời gian",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp, start = 8.dp)
                    )
                    
                    TimeFilter.entries.forEach { filter ->
                        val isSelected = filter == selectedFilter
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                                .background(if (isSelected) BrandPrimary.copy(alpha = 0.1f) else Color.Transparent)
                                .clickable {
                                    scope.launch {
                                        sheetState.hide()
                                        showBottomSheet = false
                                        onFilterSelected(filter)
                                    }
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = filter.title,
                                color = if (isSelected) BrandPrimary else Color.Black,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 16.sp,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}
