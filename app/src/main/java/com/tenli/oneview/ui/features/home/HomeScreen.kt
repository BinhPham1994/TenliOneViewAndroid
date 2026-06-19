package com.tenli.oneview.ui.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.ui.theme.BrandPrimary
import com.tenli.oneview.ui.utils.AiTypeHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onEventClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading && uiState.overviewStats.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = BrandPrimary)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Text(
                text = "Tổng quan",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
        }

        // 4 Thẻ thống kê
        item {
            // Lấy giá trị từ Overview (nếu API có nhãn cụ thể, ở đây minh họa cách lấy)
            val totalEvents = uiState.overviewStats.find { it.label == "event-count" && it.tag == "all" }?.count?.toString() ?: "0"
            val totalCameras = uiState.overviewStats.find { it.label == "camera-count" && it.tag == "all" }?.count?.toString() ?: "0"
            val totalAI = uiState.overviewStats.find { it.label == "monitor-count" && it.tag == "all" }?.count?.toString() ?: "0"
            val systemStatus = "An toàn" // Default mock as per requirement

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Tổng sự kiện",
                        value = totalEvents,
                        iconColor = Color(0xFF1890FF),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Tổng số camera",
                        value = totalCameras,
                        iconColor = Color(0xFF52C41A),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Tổng tiến trình AI",
                        value = totalAI,
                        iconColor = Color(0xFF722ED1),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Tình trạng hệ thống",
                        value = systemStatus,
                        iconColor = Color(0xFF13C2C2),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Biểu đồ: Sự kiện theo thời gian
        item {
            SectionTitle("Phân bố sự kiện theo thời gian")
            val overTimeData = uiState.eventsOverTime.map {
                Pair("${it.hour}h", it.value.toFloatOrNull() ?: 0f)
            }
            HomeLineChart(
                data = overTimeData,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFFF8F9FA), shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                    .padding(8.dp)
            )
        }

        // Biểu đồ: Sự kiện theo bài AI
        item {
            SectionTitle("Phân bố sự kiện theo bài AI")
            val byTypeData = uiState.eventsByType.map {
                Pair(AiTypeHelper.getTypeName(it.type), it.count.toFloat())
            }
            HomeHorizontalBarChart(
                data = byTypeData,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(Color(0xFFF8F9FA), shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                    .padding(8.dp)
            )
        }

        // Danh sách sự kiện gần đây
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionTitle("Sự kiện gần đây")
        }

        if (uiState.error != null) {
            item {
                Text(
                    text = uiState.error ?: "",
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else if (uiState.recentEvents.isEmpty()) {
            item {
                Text("Chưa có sự kiện nào", color = Color.Gray, modifier = Modifier.padding(16.dp))
            }
        } else {
            items(uiState.recentEvents) { event ->
                RecentEventItem(event) {
                    onEventClick(event.id.toString())
                }
            }
        }
    }
}

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentEventItem(event: EventData, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = event.data?.image ?: event.data?.cropImage,
                contentDescription = "Event Image",
                modifier = Modifier
                    .size(60.dp)
                    .background(Color.DarkGray, androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.data?.cameraUUID?.take(6)?.let { "Camera $it" } ?: "Camera",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = AiTypeHelper.getTypeName(event.type),
                    color = BrandPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = formatEventTime(event.time),
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

private fun formatEventTime(time: Double?): String {
    if (time == null || time == 0.0) return "N/A"
    // API returns time in seconds as Double
    val timeMillis = if (time < 100000000000.0) (time * 1000).toLong() else time.toLong()
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timeMillis))
}
