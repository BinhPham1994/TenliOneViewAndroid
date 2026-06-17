package com.tenli.oneview.ui.features.setting.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import com.tenli.oneview.ui.features.setting.screens.device.ai.sensitivityLevels
import com.tenli.oneview.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensitivityBottomSheet(
    selectedValue: Int,
    onValueSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFF7F7F7), // Màu nền hơi xám nhẹ để nổi bật Card trắng [cite: 2026-03-08]
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = MaterialTheme.spacing.medium)
                .padding(bottom = MaterialTheme.spacing.extraLarge)
        ) {
            Text(
                text = "Độ nhạy",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = MaterialTheme.spacing.medium)
            )

            // Khối màu trắng bo góc chứa các lựa chọn [cite: 2026-03-08]
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(MaterialTheme.spacing.radiusMedium))
                    .background(Color.White)
            ) {
                sensitivityLevels.forEachIndexed { index, level ->
                    val isSelected = level.value == selectedValue

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onValueSelected(level.value)
                                onDismiss()
                            }
                            .padding(horizontal = 20.dp, vertical = MaterialTheme.spacing.medium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = level.label,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black
                        )

                        if (isSelected) {
                            Icon(
                                painter = rememberVectorPainter(image = Icons.Default.Check),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    if (index < sensitivityLevels.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            thickness = MaterialTheme.spacing.borderThin,
                            color = Color.LightGray.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}