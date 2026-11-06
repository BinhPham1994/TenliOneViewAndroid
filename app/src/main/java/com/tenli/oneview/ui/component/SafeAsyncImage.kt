package com.tenli.oneview.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrokenImage
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.tenli.oneview.ui.theme.appColors
import com.tenli.oneview.ui.theme.spacing

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
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.appColors
    var isImageLoading by remember(url) { mutableStateOf(true) }
    val context = LocalContext.current

    // Xây dựng ImageRequest với Header Authorization và giới hạn kích thước giải mã
    val imageRequest = remember(context, url, deviceKey) {
        ImageRequest.Builder(context)
            .data(url)
            .size(1280, 720)
            .precision(coil.size.Precision.INEXACT)
            .allowHardware(true)
            .apply {
                if (deviceKey.isNotEmpty()) {
                    addHeader("Authorization", "Bearer $deviceKey")
                }
            }
            .crossfade(false)
            .diskCachePolicy(CachePolicy.ENABLED)
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

        if (showLoading && isImageLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(spacing.avatarSmall),
                color = colors.onMedia.copy(alpha = 0.5f),
                strokeWidth = spacing.space3
            )
        }
    }
}
