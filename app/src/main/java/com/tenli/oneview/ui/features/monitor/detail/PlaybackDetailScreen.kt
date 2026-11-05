package com.tenli.oneview.ui.features.monitor.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.tenli.oneview.ui.component.VideoPlayer
import com.tenli.oneview.ui.theme.appColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackDetailScreen(
    videoLink: String,
    time: String,
    imageLink: String, // unused now but kept for signature compatibility
    cameraName: String,
    onBack: () -> Unit
) {
    val colors = MaterialTheme.appColors
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
                    containerColor = colors.mediaBg,
                    titleContentColor = colors.onMedia,
                    navigationIconContentColor = colors.onMedia
                )
            )
        },
        containerColor = colors.mediaBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colors.mediaBg),
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
                Text("Không có đường dẫn video", color = colors.onMedia)
            }
        }
    }
}
