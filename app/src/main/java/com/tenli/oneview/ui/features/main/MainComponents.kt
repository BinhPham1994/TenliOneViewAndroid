package com.tenli.oneview.ui.features.main

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.shadow
import com.tenli.oneview.R
import com.tenli.oneview.ui.theme.spacing

@Composable
fun MainBottomNavigation(
    currentTab: MainTab,
    isCurrentTabScrolled: Boolean,
    onTabClick: (MainTab) -> Unit,
    onScrollToTop: (MainTab) -> Unit
) {
    NavigationBar(
        modifier = Modifier.height(76.dp),
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
        windowInsets = WindowInsets(0, 0, 0, 40)
    ) {
        val items = listOf(
            Triple(MainTab.Home, R.string.tab_home, Icons.Rounded.Home),
            Triple(MainTab.Monitor, R.string.tab_monitor, Icons.Rounded.Videocam),
            Triple(MainTab.Event, R.string.tab_event, Icons.Rounded.Notifications),
            Triple(MainTab.Setting, R.string.tab_setting, Icons.Rounded.Settings)
        )

        items.forEach { (tab, labelRes, iconRes) ->
            val isSelected = currentTab == tab
            val showBackToTop = isSelected && isCurrentTabScrolled

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (showBackToTop) {
                        onScrollToTop(tab)
                    } else {
                        onTabClick(tab)
                    }
                },
                alwaysShowLabel = true,
                label = {
                    Text(
                        text = stringResource(id = labelRes),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.offset(y = (-3).dp)
                    )
                },
                icon = {
                    Icon(
                        imageVector = iconRes,
                        contentDescription = stringResource(id = labelRes),
                        modifier = Modifier
                            .size(MaterialTheme.spacing.iconMedium)
                            .offset(y = MaterialTheme.spacing.borderMedium)
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFF97316),
                    selectedTextColor = Color(0xFFF97316),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}