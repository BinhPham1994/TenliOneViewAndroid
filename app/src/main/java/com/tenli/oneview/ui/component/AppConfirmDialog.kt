package com.tenli.oneview.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.tenli.oneview.ui.theme.ErrorRed
import com.tenli.oneview.ui.theme.LucideIcons
import com.tenli.oneview.ui.theme.appColors
import com.tenli.oneview.ui.theme.spacing

/** Độ đậm nền ô icon (màu xác nhận pha loãng) */
private const val ICON_CONTAINER_ALPHA = 0.14f

/**
 * Bottom sheet xác nhận thao tác (icon + tiêu đề + mô tả + [Hủy]/[Xác nhận]).
 * Dùng chung khung [MobileNativeBottomSheet] + [SheetActionButtons].
 *
 * @param modifier áp dụng cho phần thân nội dung (icon + chữ).
 */
@Composable
fun AppConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "Đồng ý",
    cancelText: String = "Hủy",
    confirmColor: Color = ErrorRed,
    icon: ImageVector = LucideIcons.AlertTriangle,
    headline: String? = null,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.appColors

    MobileNativeBottomSheet(title = title, onDismiss = onDismiss) {
        // Body Content
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.space20, vertical = spacing.space12),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(spacing.avatarLarge)
                    .clip(RoundedCornerShape(spacing.radiusMedium))
                    .background(confirmColor.copy(alpha = ICON_CONTAINER_ALPHA)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = confirmColor,
                    modifier = Modifier.size(spacing.space28)
                )
            }
            Spacer(modifier = Modifier.height(spacing.space14))
            Text(
                text = headline ?: title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.textTitle,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(spacing.space6))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(spacing.space14))

        // Action Buttons
        SheetActionButtons(
            cancelText = cancelText,
            confirmText = confirmText,
            onCancel = onDismiss,
            onConfirm = onConfirm,
            confirmColor = confirmColor
        )
    }
}
