package com.tenli.oneview.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import com.tenli.oneview.ui.theme.appColors
import com.tenli.oneview.ui.theme.spacing

/**
 * Ô vuông bo góc (squircle) có màu nền + icon ở giữa – kiểu icon badge của trang Cài đặt iOS / PWA.
 *
 * Mặc định: 30dp, icon 16dp, bo góc 8dp, icon màu trắng ([com.tenli.oneview.ui.theme.AppColors.onAccent]).
 */
@Composable
fun IconBadge(
    icon: ImageVector,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    size: Dp = MaterialTheme.spacing.avatarSmall,
    iconSize: Dp = MaterialTheme.spacing.iconSmall,
    cornerRadius: Dp = MaterialTheme.spacing.radiusSmall,
    tint: Color = MaterialTheme.appColors.onAccent
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}
