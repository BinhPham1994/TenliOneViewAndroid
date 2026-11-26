package com.tenli.oneview.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.tenli.oneview.ui.theme.LucideIcons
import com.tenli.oneview.ui.theme.appColors
import com.tenli.oneview.ui.theme.caption
import com.tenli.oneview.ui.theme.spacing

/** Độ mờ của icon trạng thái rỗng */
private const val EMPTY_ICON_ALPHA = 0.6f

private val MediaStagePillShape = RoundedCornerShape(50)

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
                    .clip(MediaStagePillShape)
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

/**
 * Trạng thái trống / lỗi tinh gọn dành riêng cho khung media 16:9 (Video sự kiện, Live Camera, Ảnh sự kiện):
 * dùng đúng nền `colors.surface` theo Light/Dark theme (trắng ở Light mode, tối ở Dark mode),
 * và chuyển sang nền đen `colors.mediaBg` khi bật [forceDark] ở chế độ toàn màn hình.
 */
@Composable
fun MediaStageEmptyState(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = LucideIcons.VideoOff,
    subtitle: String? = null,
    actionLabel: String? = null,
    actionIcon: ImageVector? = LucideIcons.RefreshCw,
    onAction: (() -> Unit)? = null,
    forceDark: Boolean = false
) {
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.appColors
    val onMedia = colors.onMedia
    val stageBg = if (forceDark) colors.mediaBg else colors.surface
    val badgeBg = remember(forceDark, onMedia, colors.controlBg) {
        if (forceDark) onMedia.copy(alpha = 0.10f) else colors.controlBg
    }
    val iconTint = remember(forceDark, onMedia, colors.textMuted) {
        if (forceDark) onMedia.copy(alpha = 0.72f) else colors.textMuted.copy(alpha = 0.72f)
    }
    val titleColor = remember(forceDark, onMedia, colors.textTitle) {
        if (forceDark) onMedia.copy(alpha = 0.88f) else colors.textTitle
    }
    val subtitleColor = remember(forceDark, onMedia, colors.textMuted) {
        if (forceDark) onMedia.copy(alpha = 0.56f) else colors.textMuted
    }
    val actionBg = remember(forceDark, onMedia, colors.accent, colors.isDark) {
        if (forceDark) {
            onMedia.copy(alpha = 0.14f)
        } else {
            colors.accent.copy(alpha = if (colors.isDark) 0.18f else 0.10f)
        }
    }
    val actionContentColor = remember(forceDark, onMedia, colors.activeContent) {
        if (forceDark) onMedia.copy(alpha = 0.92f) else colors.activeContent
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(stageBg)
            .padding(horizontal = spacing.space24),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(spacing.iconButtonLarge)
                .clip(CircleShape)
                .background(badgeBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(spacing.iconSmallMedium)
            )
        }

        Spacer(modifier = Modifier.height(spacing.space10))

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = titleColor,
            textAlign = TextAlign.Center,
            maxLines = 2
        )

        if (!subtitle.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(spacing.space3))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.caption,
                color = subtitleColor,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }

        if (!actionLabel.isNullOrBlank() && onAction != null) {
            Spacer(modifier = Modifier.height(spacing.space12))
            Row(
                modifier = Modifier
                    .heightIn(min = spacing.space32)
                    .clip(MediaStagePillShape)
                    .background(actionBg)
                    .clickable(onClick = onAction)
                    .padding(horizontal = spacing.space14, vertical = spacing.space5),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.space5)
            ) {
                if (actionIcon != null) {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = null,
                        tint = actionContentColor,
                        modifier = Modifier.size(spacing.iconXSmall)
                    )
                }
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = actionContentColor
                )
            }
        }
    }
}
