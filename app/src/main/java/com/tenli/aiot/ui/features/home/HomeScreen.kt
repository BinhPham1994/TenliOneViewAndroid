package com.tenli.aiot.ui.features.home

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tenli.aiot.R
import com.tenli.aiot.model.network.EventItem
import com.tenli.aiot.ui.features.home.component.HomeGroupSheet
import com.tenli.aiot.ui.features.home.component.RecentEventItem
import com.tenli.aiot.ui.theme.spacing
import com.tenli.aiot.ui.utils.bounceClick
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    listState: LazyListState,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
    onShowDevices: () -> Unit,
    onShowMembers: () -> Unit,
    onShowScript: () -> Unit,
    onShowIOT: () -> Unit,
    onEventClick: (EventItem) -> Unit
) {
    val titleAiBox = stringResource(id = R.string.home_title_ai_box)
    val titleMembers = stringResource(id = R.string.home_title_members)
    val titleScripts = stringResource(id = R.string.home_title_scripts)
    val titleIot = stringResource(id = R.string.home_title_iot)
    val sectionRecent = stringResource(id = R.string.home_section_recent)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val countAiBox = stringResource(id = R.string.home_count_devices, uiState.aiBoxCount.toString())
    val countMembers = stringResource(id = R.string.home_count_members, uiState.memberCount.toString())
    val countScripts = stringResource(id = R.string.home_count_scripts, uiState.scriptCount.toString())
    val countIot = stringResource(id = R.string.home_count_devices, uiState.iotCount.toString())

    var showSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val recentEvents = remember(uiState.recentEvents) { uiState.recentEvents }
    val groupedEvents = remember(uiState.groupedEvents) { uiState.groupedEvents }
    val context = LocalContext.current
    BackHandler(enabled = true) {
        if (showSheet) {
            showSheet = false
        } else {
            (context as? Activity)?.moveTaskToBack(true)
        }
    }
    if (showSheet) {
        HomeGroupSheet(
            groups = uiState.displayGroups,
            selectedGroupId = uiState.selectedGroup?.group?.id ?: -1,
            onGroupSelected = { selectedGroup ->
                viewModel.onGroupSelected(selectedGroup)

                showSheet = false

                scope.launch {
                    listState.scrollToItem(0)
                }
            },
            onManageHome = {
                showSheet = false
            },
            onDismiss = { showSheet = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HomeHeaderSimple(
            homeName = uiState.homeName,
            onClick = { showSheet = true }
        )

        HorizontalDivider(
            thickness = MaterialTheme.spacing.borderThin,
            color = Color.LightGray.copy(alpha = 0.3f)
        )

        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.refreshData(forceRefresh = true) },
            modifier = Modifier.weight(1f)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                item(key = "dashboard") {
                    Column(modifier = Modifier.padding(start = MaterialTheme.spacing.medium, end = MaterialTheme.spacing.medium, top = 10.dp, bottom = 10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            DashboardCard(
                                title = titleAiBox,
                                count = countAiBox,
                                iconRes = R.drawable.device_icon,
                                iconColor = colorResource(id = R.color.main_color),
                                modifier = Modifier
                                    .weight(1f)
                                    .bounceClick { onShowDevices() }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            DashboardCard(
                                title = titleMembers,
                                count = countMembers,
                                iconRes = R.drawable.member,
                                iconColor = colorResource(id = R.color.main_color),
                                modifier = Modifier
                                    .weight(1f)
                                    .bounceClick { onShowMembers() }
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            DashboardCard(
                                title = titleScripts,
                                count = countScripts,
                                iconRes = R.drawable.script,
                                iconColor = colorResource(id = R.color.main_color),
                                modifier = Modifier
                                    .weight(1f)
                                    .bounceClick { onShowScript }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            DashboardCard(
                                title = titleIot,
                                count = countIot,
                                iconRes = R.drawable.iot,
                                iconColor = colorResource(id = R.color.main_color),
                                modifier = Modifier
                                    .weight(1f)
                                    .bounceClick { onShowIOT() }
                            )
                        }
                    }
                }
                item(key = "section_header_recent") {
                    SectionHeader(title = sectionRecent) {
//                        onShowDevices()
                    }
                }

                if (recentEvents.isEmpty() && uiState.isRecentEventLoading) {
                    item(key = "loading_recent") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(MaterialTheme.spacing.iconMedium), strokeWidth = 5.dp)
                        }
                    }
                } else {
                    items(
                        items = recentEvents,
                        key = { "recent_${it.id}" },
                        contentType = { "event_item" }
                    ) { event ->
                        RecentEventItem(
                            event = event,
                            onClick = {
                                viewModel.markEventAsRead(event)
                                onEventClick(event)
                            }
                        )
                    }
                }

                if (groupedEvents.isNotEmpty()) {
                    groupedEvents.forEach { (groupName, events) ->
                        item(key = "header_$groupName") {
                            SectionHeader(title = groupName) { }
                        }

                        items(
                            items = events,
                            key = { "group_${groupName}_${it.id}" },
                            contentType = { "event_item" }
                        ) { event ->
                            RecentEventItem(
                                event = event,
                                onClick = {
                                    viewModel.markEventAsRead(event)
                                    onEventClick(event)
                                }
                            )
                        }
                    }
                } else if (uiState.isGroupEventLoading) {
                    item(key = "loading_groups") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(MaterialTheme.spacing.iconMedium), strokeWidth = 3.dp)
                        }
                    }
                }

                item(key = "footer_spacer") {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}