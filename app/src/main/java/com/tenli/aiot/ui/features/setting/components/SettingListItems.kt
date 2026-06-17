package com.tenli.aiot.ui.features.setting.components

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
import com.tenli.aiot.R
import com.tenli.aiot.model.network.ClientDevice
import com.tenli.aiot.ui.theme.spacing

@Composable
fun SettingMenuItem(
    iconRes: Int,
    title: String,
    iconTint: Color,
    showBadge: Boolean = false,
    showDivider: Boolean = true,
    textColor: Color = Color.Unspecified,
    extraContent: @Composable () -> Unit = {},
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }) {
        Row(
            modifier = Modifier
                .padding(horizontal = MaterialTheme.spacing.medium, vertical = 12.dp)
                .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(MaterialTheme.spacing.iconLarge)
                    .clip(RoundedCornerShape(MaterialTheme.spacing.radiusSmall))
                    .background(iconTint), contentAlignment = Alignment.Center
            ) {
                Icon(painterResource(iconRes), null, modifier = Modifier.size(18.dp), tint = Color.White)
            }
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))
            Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, color = textColor)
            extraContent()
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.outlineVariant)
        }
        if (showDivider) HorizontalDivider(modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium), thickness = MaterialTheme.spacing.borderThin, color = Color.LightGray.copy(alpha = 0.3f))
    }
}

@Composable
fun ClientItem(
    device: ClientDevice,
    showDivider: Boolean = true,
    onClick: () -> Unit
) {
    val iconRes = when (device.type) {
        5 -> R.drawable.ios        // iOS
        4 -> R.drawable.android    // Android
        else -> R.drawable.unknown  // Trường hợp còn lại
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = Color.Gray
            )

            Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = device.name,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    if (device.isCurrent) {
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                        Text(
                            text = "(Hiện tại)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = device.model,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.outlineVariant)
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
                thickness = MaterialTheme.spacing.borderThin,
                color = Color.LightGray.copy(alpha = 0.5f)
            )
        }
    }
}