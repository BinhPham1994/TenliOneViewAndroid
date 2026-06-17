package com.tenli.aiot.ui.features.setting.screens.notification.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tenli.aiot.ui.theme.spacing

@Composable
fun NotifyTypeItem(
    iconRes: Int,
    title: String,
    isEnabled: Boolean,
    statusText: String? = null,
    iconBgColor: Color = Color.Transparent,
    showIconBg: Boolean = true,
    isLast: Boolean = false,
    onClick: () -> Unit
) {
    val iconSize = if (showIconBg) 18.dp else 22.dp
    val isActive = isEnabled && statusText != "Không cảnh báo"

    // Xác định màu sắc dựa trên nội dung text [cite: 2026-03-15]
    val statusColor = when {
        statusText == "Cuộc gọi qua internet" -> Color.Red
        isActive -> MaterialTheme.colorScheme.primary // Màu xanh lá mặc định
        else -> Color.Gray
    }

    Column(modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick() }) {
        Row(
            modifier = Modifier
                .padding(horizontal = MaterialTheme.spacing.medium, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(MaterialTheme.spacing.iconLarge)
                    .clip(RoundedCornerShape(MaterialTheme.spacing.radiusSmall))
                    .background(if (showIconBg) iconBgColor else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(iconSize),
                    tint = if (showIconBg) Color.White else Color.Black.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = statusText ?: (if (isEnabled) "Bật" else "Tắt"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = statusColor // Sử dụng màu đã tính toán ở trên [cite: 2026-03-15]
                )
            }

            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.outlineVariant)
        }
        if (!isLast) HorizontalDivider(
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
            thickness = MaterialTheme.spacing.borderThin,
            color = Color.LightGray.copy(alpha = 0.3f)
        )
    }
}