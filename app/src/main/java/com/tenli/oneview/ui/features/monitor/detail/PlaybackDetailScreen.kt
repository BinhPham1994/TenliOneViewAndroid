package com.tenli.oneview.ui.features.monitor.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tenli.oneview.ui.component.VideoPlayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackDetailScreen(
    videoLink: String,
    time: String,
    imageLink: String, // unused now but kept for signature compatibility
    cameraName: String,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = cameraName.ifEmpty { "Playback" },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = time,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Trở về")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (videoLink.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)) {
                    VideoPlayer(
                        videoUrl = videoLink,
                        thumbnailUrl = null,
                        deviceKey = ""
                    )
                }
            } else {
                Text("Không có đường dẫn video", color = Color.White)
            }
        }
    }
}
