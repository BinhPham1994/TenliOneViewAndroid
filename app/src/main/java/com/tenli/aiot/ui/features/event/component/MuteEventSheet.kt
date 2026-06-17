package com.tenli.aiot.ui.features.event.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tenli.aiot.R
import com.tenli.aiot.ui.theme.spacing
import com.tenli.aiot.ui.utils.DateTimeUtils

data class MuteOption(
    val label: String,
    val duration: Long,
    val icon: Int = R.drawable.sound_off
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuteEventSheet(
    onOptionSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val muteOptions = remember {
        listOf(
            MuteOption("Tắt tạm thời", 0L),
            MuteOption("Trong 1 giờ", 3600L),
            MuteOption("Trong 4 giờ", 4 * 3600L),
            MuteOption("Trong 6 giờ", 6 * 3600L),
            MuteOption("Đến 8h sáng", DateTimeUtils.getSecondsUntil8AMNextDay())
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = MaterialTheme.spacing.large),
            contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.medium)
        ) {
            item {
                Text(
                    text = "Tùy chọn tắt thông báo",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = MaterialTheme.spacing.medium, start = MaterialTheme.spacing.extraSmall)
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(MaterialTheme.spacing.radiusMedium))
                        .background(Color.White)
                ) {
                    muteOptions.forEachIndexed { index, option ->
                        MuteOptionItem(
                            title = option.label,
                            icon = option.icon,
                            onClick = {
                                onOptionSelected(option.duration)
                                onDismiss()
                            }
                        )

                        if (index < muteOptions.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
                                thickness = MaterialTheme.spacing.borderThin,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MuteOptionItem(
    title: String,
    icon: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(MaterialTheme.spacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}