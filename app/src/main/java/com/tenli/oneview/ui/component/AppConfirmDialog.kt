package com.tenli.oneview.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@Composable
fun AppConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "Đồng ý",
    cancelText: String = "Huỷ",
    confirmColor: Color = MaterialTheme.colorScheme.error,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title, fontWeight = FontWeight.Bold)
        },
        text = {
            Text(text = message)
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(text = confirmText, color = confirmColor)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(text = cancelText, color = MaterialTheme.colorScheme.onSurface)
            }
        },
        modifier = modifier
    )
}