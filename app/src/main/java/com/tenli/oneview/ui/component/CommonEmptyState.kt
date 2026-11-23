package com.tenli.oneview.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.tenli.oneview.ui.theme.LucideIcons
import com.tenli.oneview.ui.theme.appColors
import com.tenli.oneview.ui.theme.spacing

/** Độ mờ của icon trạng thái rỗng */
private const val EMPTY_ICON_ALPHA = 0.6f

@Composable
fun CommonEmptyState(
    modifier: Modifier = Modifier,
    icon: ImageVector = LucideIcons.Inbox,
    text: String = "Không có dữ liệu",
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    iconSize: Dp = MaterialTheme.spacing.iconLarge
) {
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.appColors
    val badgeSize = (iconSize * 1.9f).coerceAtLeast(spacing.avatarLarge)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = spacing.space32),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(badgeSize)
                .clip(CircleShape)
                .background(colors.controlBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.textMuted.copy(alpha = EMPTY_ICON_ALPHA),
                modifier = Modifier.size(iconSize)
            )
        }
        Spacer(modifier = Modifier.height(spacing.space12))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = colors.textTitle,
            textAlign = TextAlign.Center
        )
        if (!subtitle.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(spacing.space4))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted,
                textAlign = TextAlign.Center
            )
        }
        if (!actionLabel.isNullOrBlank() && onAction != null) {
            Spacer(modifier = Modifier.height(spacing.space14))
            Box(
                modifier = Modifier
                    .heightIn(min = spacing.buttonHeightSmall)
                    .clip(RoundedCornerShape(50))
                    .background(colors.accent.copy(alpha = if (colors.isDark) 0.20f else 0.12f))
                    .clickable(onClick = onAction)
                    .padding(horizontal = spacing.space18, vertical = spacing.space8),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.activeContent
                )
            }
        }
    }
}
