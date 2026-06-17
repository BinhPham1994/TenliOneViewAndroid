package com.tenli.oneview.ui.features.setting.screens.device.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tenli.oneview.R
import com.tenli.oneview.model.network.CameraInfo
import com.tenli.oneview.ui.component.CommonEmptyState
import com.tenli.oneview.ui.component.SafeAsyncImage
import com.tenli.oneview.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraManagementScreen(viewModel: CameraViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 1. Quản lý trạng thái pull-to-refresh [cite: 2026-03-13]
    val refreshState = rememberPullToRefreshState()

    // 2. Bao bọc toàn bộ nội dung trong PullToRefreshBox
    PullToRefreshBox(
        modifier = Modifier.fillMaxSize(),
        state = refreshState,
        isRefreshing = uiState.isLoading, // Đồng bộ với loading trong ViewModel
        onRefresh = {
            viewModel.refreshCameras() // Gọi hàm làm mới dữ liệu [cite: 2026-03-13]
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.cameras.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CommonEmptyState(
                        text = "Chưa có camera nào được thêm.\nNhấn nút + để thêm camera mới"
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(MaterialTheme.spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.cameras) { camera ->
                        CameraCard(
                            camera = camera,
                            snapshotUrl = viewModel.getSnapshotUrl(camera),
                            accessKey = viewModel.getAccessKey(),
                            onClick = {
                                viewModel.selectCameraForEdit(camera)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CameraCard(camera: CameraInfo, snapshotUrl: String, accessKey: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clickable { onClick() },
        shape = RoundedCornerShape(MaterialTheme.spacing.radiusMedium),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            SafeAsyncImage(
                url = snapshotUrl,
                deviceKey = accessKey
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.5f),
                                Color.Transparent,
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = 300f
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.camera_icon),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                Text(
                    text = camera.name,
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1
                )
            }

            if (camera.state == "error") {
                Surface(
                    color = Color.Red.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(5.dp) // Thêm padding để không dính sát mép bo góc
                ) {
                    Text(
                        text = "Ngoại tuyến",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.small, vertical = MaterialTheme.spacing.extraSmall),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}