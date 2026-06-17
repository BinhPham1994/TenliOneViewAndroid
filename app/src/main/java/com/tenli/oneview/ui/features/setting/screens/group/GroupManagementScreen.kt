package com.tenli.oneview.ui.features.setting.screens.group

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tenli.oneview.R
import com.tenli.oneview.model.network.HomeGroupDisplay
import com.tenli.oneview.ui.features.setting.core.SettingScreenType
import com.tenli.oneview.ui.features.setting.core.SettingViewModel
import com.tenli.oneview.ui.features.setting.core.fetchGroupDetail
import com.tenli.oneview.ui.theme.spacing

@Composable
fun GroupManagementScreen(viewModel: SettingViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val groupState = uiState.group
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(MaterialTheme.spacing.medium)
    ) {
        item {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(MaterialTheme.spacing.radiusMedium))
                    .background(Color.White)
            ) {
                groupState.displayGroups.forEachIndexed { index, groupDisplay ->
                    GroupItem(
                        groupDisplay = groupDisplay,
                        showDivider = index != groupState.displayGroups.size - 1,
                        onClick = {
                            viewModel.fetchGroupDetail(groupDisplay)
                            viewModel.navigateTo(SettingScreenType.GroupDetail, groupDisplay.displayName)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GroupItem(
    groupDisplay: HomeGroupDisplay,
    showDivider: Boolean,
    onClick: () -> Unit
) {
    val role = groupDisplay.group.userRequestRole
    val (roleText, roleColor) = when (role) {
        "owner" -> {
            "Chủ nhà" to MaterialTheme.colorScheme.primary
        }

        "technical" -> {
            "Hỗ trợ kỹ thuật" to Color.Gray
        }

        else -> {
            "Thành viên" to Color.Gray
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }) {
        Row(
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.home),
                    contentDescription = null,
                    modifier = Modifier.size(26.dp),
                    tint = Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = groupDisplay.displayName, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = roleText, style = MaterialTheme.typography.bodyMedium, color = roleColor)
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