package com.tenli.oneview.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.tenli.oneview.ui.theme.spacing

/** Độ mờ của icon trạng thái rỗng */
private const val EMPTY_ICON_ALPHA = 0.4f

@Composable
fun CommonEmptyState(
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Rounded.Inbox, // Mặc định là icon Inbox
    text: String = "Không có dữ liệu",      // Mặc định nếu không truyền text
    iconSize: Dp = MaterialTheme.spacing.iconHuge
) {
    val spacing = MaterialTheme.spacing
    val contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = spacing.space40),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor.copy(alpha = EMPTY_ICON_ALPHA),
            modifier = Modifier.size(iconSize)
        )
        Spacer(modifier = Modifier.height(spacing.space6))
        Text(
            text = text,
            // bodyMedium có lineHeight 20sp giúp text 2 dòng dễ đọc hơn
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
            textAlign = TextAlign.Center
        )
    }
}
