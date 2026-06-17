package com.tenli.aiot.ui.features.setting.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LogoutConfirmDialog(isLoggingOut: Boolean, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss, title = { Text("Thông báo") },
        text = { Text("Bạn có chắc chắn muốn đăng xuất khỏi tài khoản này?") },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isLoggingOut) {
                if (isLoggingOut) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text("Đăng xuất", color = Color.Red)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}