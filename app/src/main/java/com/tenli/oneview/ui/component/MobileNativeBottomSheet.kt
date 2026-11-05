package com.tenli.oneview.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.tenli.oneview.R
import com.tenli.oneview.ui.theme.LucideIcons
import com.tenli.oneview.ui.theme.appColors
import com.tenli.oneview.ui.theme.spacing

/**
 * Bottom sheet kiểu iOS (MobileSettingsPage.vue) hiển thị qua [MobileFullScreenOverlay]:
 * thanh kéo + tiêu đề + nút đóng tròn, chạm ra ngoài để đóng.
 *
 * Trên màn hình rộng (tablet/foldable/landscape) sheet được giới hạn độ rộng
 * [com.tenli.oneview.ui.theme.Dimensions.contentMaxWidth] và căn giữa.
 */
@Composable
fun MobileNativeBottomSheet(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.appColors

    MobileFullScreenOverlay(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .widthIn(max = spacing.contentMaxWidth)
                    .fillMaxWidth()
                    // Chặn click xuyên xuống lớp nền (không đóng sheet khi chạm vào nội dung)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    ),
                shape = RoundedCornerShape(topStart = spacing.radiusLarge, topEnd = spacing.radiusLarge),
                color = colors.surfaceGrouped
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = spacing.space16)
                        .padding(top = spacing.space10, bottom = spacing.space20)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(spacing.sheetHandleWidth)
                            .height(spacing.sheetHandleHeight)
                            .clip(CircleShape)
                            .background(colors.handle)
                    )
                    Spacer(modifier = Modifier.height(spacing.space14))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = spacing.space14),
                        horizontalArrangement = Arrangement.spacedBy(spacing.space12),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.textTitle,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        SheetCloseButton(onClick = onDismiss)
                    }
                    content()
                }
            }
        }
    }
}

/** Nút đóng tròn (X) ở góc phải header của sheet. */
@Composable
fun SheetCloseButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.appColors
    Box(
        modifier = modifier
            .size(spacing.avatarSmall)
            .clip(CircleShape)
            .background(colors.closeButtonBg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = LucideIcons.X,
            contentDescription = stringResource(id = R.string.dialog_btn_close),
            tint = colors.textMuted,
            modifier = Modifier.size(spacing.iconSmall)
        )
    }
}
