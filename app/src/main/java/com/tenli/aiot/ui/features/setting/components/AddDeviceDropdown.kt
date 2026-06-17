package com.tenli.aiot.ui.features.setting.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tenli.aiot.R
import com.tenli.aiot.ui.theme.spacing

@Composable
fun AddDeviceDropdown(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onLanScanClick: () -> Unit,
    onManualEntryClick: () -> Unit,
    onQrCodeClick: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .width(180.dp)
            .background(Color.White, RoundedCornerShape(MaterialTheme.spacing.radiusMedium)) // Bo góc 16dp cho hiện đại
    ) {
        // 1. Quét trong LAN
        AddDeviceMenuItem(
            label = "Quét trong LAN",
            iconRes = R.drawable.no_internet, // Thay bằng id icon của bạn
            onClick = onLanScanClick
        )

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 12.dp),
            thickness = MaterialTheme.spacing.borderThin,
            color = Color.LightGray.copy(alpha = 0.3f)
        )

        // 2. Nhập thủ công
        AddDeviceMenuItem(
            label = "Nhập thủ công",
            iconRes = R.drawable.edit_ai_active, // Thay bằng id icon của bạn
            onClick = onManualEntryClick
        )

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 12.dp),
            thickness = MaterialTheme.spacing.borderThin,
            color = Color.LightGray.copy(alpha = 0.3f)
        )

        // 3. Ảnh QRCode
        AddDeviceMenuItem(
            label = "Ảnh QRCode",
            iconRes = R.drawable.iot, // Thay bằng id icon của bạn
            onClick = onQrCodeClick
        )
    }
}

@Composable
private fun AddDeviceMenuItem(
    label: String,
    iconRes: Int,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Black)
        },
        leadingIcon = {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary // Màu xanh lá thương hiệu
            )
        },
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.medium, vertical = MaterialTheme.spacing.small)
    )
}