package com.tenli.oneview.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.tenli.oneview.ui.theme.appColors
import com.tenli.oneview.ui.theme.spacing

/**
 * Hàng 2 nút ngang nhau ở cuối bottom sheet: [Hủy] (nền phụ) + [Xác nhận] (nền màu nhấn).
 *
 * @param confirmColor Màu nền nút xác nhận (mặc định accent; dùng error cho thao tác nguy hiểm).
 * @param enabled Khi false cả 2 nút không nhận click (vd. đang gửi request).
 * @param isLoading Hiển thị vòng xoay thay cho chữ ở nút xác nhận.
 */
@Composable
fun SheetActionButtons(
    cancelText: String,
    confirmText: String,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    confirmColor: Color = MaterialTheme.appColors.accent,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.appColors
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing.space10)
    ) {
        SheetButton(
            text = cancelText,
            background = colors.buttonSecondaryBg,
            contentColor = MaterialTheme.colorScheme.onBackground,
            enabled = enabled,
            onClick = onCancel,
            modifier = Modifier.weight(1f)
        )
        SheetButton(
            text = confirmText,
            background = confirmColor,
            contentColor = colors.onAccent,
            enabled = enabled,
            isLoading = isLoading,
            onClick = onConfirm,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SheetButton(
    text: String,
    background: Color,
    contentColor: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    val spacing = MaterialTheme.spacing
    Box(
        modifier = modifier
            .heightIn(min = spacing.buttonHeight)
            .clip(RoundedCornerShape(spacing.radiusCompact))
            .background(background)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = spacing.space8),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = contentColor,
                modifier = Modifier.size(spacing.iconRegular),
                strokeWidth = spacing.progressStroke
            )
        } else {
            Text(
                text = text,
                color = contentColor,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
