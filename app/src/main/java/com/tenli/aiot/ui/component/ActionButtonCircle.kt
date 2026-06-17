package com.tenli.aiot.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tenli.aiot.ui.utils.bounceClick

@Composable
fun ActionButtonCircle(
    icon: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    activeColor: Color = Color(0xFFE42E1B) // Mặc định là màu đỏ khi active [cite: 2026-03-17]
) {
    // Định nghĩa màu sắc để code sạch hơn
    val backgroundColor = if (isSelected) activeColor else Color(0xFFE3E1E1)
    val iconTint = if (isSelected) Color.White else Color.Gray.copy(alpha = 0.5f) // Xám đậm trên nền xám nhạt
    val textColor = if (isSelected) activeColor else Color.Gray

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.bounceClick(onClick = onClick)
    ) {
        Surface(
            // 1. Chuyển sang CircleShape để có ô tròn hoàn hảo [cite: 2026-03-17]
            shape = CircleShape,
            color = backgroundColor,
            modifier = Modifier.size(56.dp), // Tăng nhẹ size lên 56dp cho cân đối hình tròn
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp), // Size icon chuẩn
                    tint = iconTint // 2. Màu icon tương phản với nền [cite: 2026-03-17]
                )
            }
        }

        Spacer(Modifier.height(5.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}