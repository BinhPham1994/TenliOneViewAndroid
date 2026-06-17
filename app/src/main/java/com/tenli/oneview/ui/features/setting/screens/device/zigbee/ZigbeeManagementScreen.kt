package com.tenli.oneview.ui.features.setting.screens.device.zigbee

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.tenli.oneview.R
import com.tenli.oneview.model.network.IotItem
import com.tenli.oneview.ui.theme.spacing

@Composable
fun ZigbeeManagementScreen(viewModel: ZigbeeViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // 1. Thanh hiển thị trạng thái kết nối MQTT
//        MqttStatusBanner(uiState.mqttStatus)

        if (uiState.iots.isEmpty()) {
            EmptyZigbeeView()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(MaterialTheme.spacing.medium)
            ) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(MaterialTheme.spacing.radiusMedium),
                        color = Color.White
                    ) {
                        Column {
                            uiState.iots.forEachIndexed { index, iot ->
                                ZigbeeDeviceRow(
                                    iot = iot,
                                    isLast = index == uiState.iots.size - 1,
                                    onClick = {
                                        // TODO: Chuyển vào màn hình điều khiển chi tiết
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ZigbeeDeviceRow(
    iot: IotItem,
    isLast: Boolean,
    onClick: () -> Unit
) {
    // 1. Lấy dữ liệu từ exposes [cite: 2026-03-16]
    val modelId = iot.exposes["model_id"]
    val availability = iot.exposes["availability"] ?: "unknown"
    val isOnline = availability.lowercase() == "online"
    val battery = iot.exposes["battery"]

    // 2. Xác định nguồn ảnh
    val localImageRes = DeviceImageMapper.getLocalImage(modelId, iot.displayName)
    val imageUrl = if (localImageRes == null && !modelId.isNullOrEmpty()) {
        "https://www.zigbee2mqtt.io/images/devices/$modelId.png"
    } else null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(MaterialTheme.spacing.iconExtraLarge)
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (localImageRes != null) {
                    // Ưu tiên 1: Ảnh Local (luôn hiển thị ngay lập tức)
                    Icon(
                        painter = painterResource(id = localImageRes),
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = Color.Unspecified
                    )
                } else {
                    // Ưu tiên 2 & 3: Hiển thị Icon mặc định màu xám nhạt làm nền
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp),
                        tint = Color.LightGray.copy(alpha = 0.5f) // Màu cực nhạt để không gây chói
                    )

                    // Nếu có URL, load ảnh đè lên với hiệu ứng Crossfade
                    if (imageUrl != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true) // Bật hiệu ứng mờ dần [cite: 2026-03-16]
                                .crossfade(400)  // Thời gian chuyển cảnh 0.4s
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.size(42.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))

            // PHẦN THÔNG TIN CHI TIẾT
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = iot.displayName.ifEmpty { iot.ieeeAddress },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isOnline) Color.Unspecified else Color.Gray
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = if (isOnline) "Đang hoạt động" else "Ngoại tuyến",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )

                    // Hiển thị Pin (Nếu có)
                    if (battery != null) {
                        Text(
                            text = " • Pin: $battery%",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = MaterialTheme.spacing.extraSmall)
                        )
                    }
                }
            }

            // Mũi tên điều hướng
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.outlineVariant)
        }

        // Đường kẻ ngăn cách giữa các dòng
        if (!isLast) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
                thickness = MaterialTheme.spacing.borderThin,
                color = Color.LightGray.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun MqttStatusBanner(status: MqttStatus) {
    val (bgColor, textColor, label) = when (status) {
        MqttStatus.CONNECTED -> Triple(Color(0xFFE8F5E9), MaterialTheme.colorScheme.primary, "Máy chủ: Đã kết nối")
        MqttStatus.CONNECTING -> Triple(Color(0xFFFFF3E0), Color(0xFFFF9800), "Máy chủ: Đang kết nối...")
        MqttStatus.ERROR -> Triple(Color(0xFFFFEBEE), MaterialTheme.colorScheme.error, "Máy chủ: Lỗi kết nối")
        MqttStatus.DISCONNECTED -> Triple(Color(0xFFF5F5F5), Color.Gray, "Máy chủ: Đã ngắt kết nối")
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = textColor, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun EmptyZigbeeView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.data_empty),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Không tìm thấy thiết bị Zigbee nào\nChọn dấu + để thêm thiết bị mới",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = androidx.compose.ui.unit.TextUnit.Unspecified
        )
    }
}