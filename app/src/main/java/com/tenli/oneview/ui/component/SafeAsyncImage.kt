package com.tenli.oneview.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrokenImage
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun SafeAsyncImage(
    url: String?,
    deviceKey: String,
    modifier: Modifier = Modifier.fillMaxSize(),
    contentScale: ContentScale = ContentScale.Crop,
    placeholderIcon: ImageVector = Icons.Rounded.Image,
    errorIcon: ImageVector = Icons.Rounded.BrokenImage,
    showLoading: Boolean = true
) {
    var isImageLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current

    // Xây dựng ImageRequest với Header Authorization [cite: 2026-03-16]
    val imageRequest = remember(url, deviceKey) {
        ImageRequest.Builder(context)
            .data(url)
            .addHeader("Authorization", "Bearer $deviceKey")
            .crossfade(true)
            .diskCachePolicy(coil.request.CachePolicy.ENABLED)
            .build()
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AsyncImage(
            model = imageRequest,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = contentScale,
            onSuccess = { isImageLoading = false },
            onError = { isImageLoading = false },
            placeholder = rememberVectorPainter(image = placeholderIcon),
            error = rememberVectorPainter(image = errorIcon)
        )

        // Hiển thị vòng xoay loading nếu được phép [cite: 2026-03-16]
        if (showLoading && isImageLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(30.dp),
                color = Color.White.copy(alpha = 0.5f),
                strokeWidth = 3.dp
            )
        }
    }
}