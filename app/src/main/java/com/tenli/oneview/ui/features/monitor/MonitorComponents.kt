package com.tenli.oneview.ui.features.monitor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tenli.oneview.data.mapper.EventProcessor
import com.tenli.oneview.data.mapper.EventProcessor.getMonitorGroupTitle
import com.tenli.oneview.data.repository.DataRepository
import com.tenli.oneview.model.network.EventItem
import com.tenli.oneview.model.network.MonitorDisplayItem
import com.tenli.oneview.ui.component.AppConfirmDialog
import com.tenli.oneview.ui.component.MjpegStreamPlayer
import com.tenli.oneview.ui.component.SafeAsyncImage
import com.tenli.oneview.ui.features.home.component.RecentEventItem
import com.tenli.oneview.ui.theme.spacing
import com.tenli.oneview.ui.utils.bounceClick

@Composable
fun MonitorGroupHeader(type: String) {
    val groupDisplayName = getMonitorGroupTitle(type)

    Text(
        text = groupDisplayName,
        style = MaterialTheme.typography.titleSmall,
        color = Color.Gray,
        modifier = Modifier.padding(start = MaterialTheme.spacing.large, top = 20.dp, bottom = 0.dp)
    )
}

@Composable
fun MonitorImageCard(item: MonitorDisplayItem, onClick: () -> Unit) {
    val typeDef = DataRepository.eventTypeDefs.find { it.key == item.monitor.type }
    val uiConfig = EventProcessor.getUIConfig(
        eType = item.monitor.type ?: "",
        eTypeGroup = typeDef?.key ?: ""
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.medium, vertical = 6.dp)
            .bounceClick { onClick() },
        shape = RoundedCornerShape(MaterialTheme.spacing.radiusMedium),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        ) {
            SafeAsyncImage(url = item.snapshotUrl, deviceKey = item.deviceKey)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                        )
                    )
            )
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(uiConfig.iconRes),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                Text(
                    text = item.monitor.name ?: "",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                )
            }

            Surface(
                color = if (item.monitor.enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                shape = RoundedCornerShape(MaterialTheme.spacing.extraSmall),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(5.dp)
            ) {
                Text(
                    text = if (item.monitor.enabled) "ACTIVE" else "INACTIVE",
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitorDetailTopBar(
    title: String,
    onBack: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }
        },
        actions = {
            Spacer(modifier = Modifier.width(48.dp))
        },
        windowInsets = WindowInsets(0.dp),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Unspecified,
            navigationIconContentColor = Color.Unspecified,
            titleContentColor = Color.Unspecified,
            actionIconContentColor = Color.Unspecified
        )
    )
}

@Composable
fun MonitorDetailView(
    listState: LazyListState,
    item: MonitorDisplayItem?,
    viewModel: MonitorViewModel,
    onEventClick: (EventItem) -> Unit
) {
    if (item == null) return
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentItem = uiState.selectedMonitor ?: item
    val events by viewModel.monitorEvents.collectAsStateWithLifecycle()
    val isLogicPerson = currentItem.monitor.type == "logic-person"

    val getActualSecurityState = { param: Map<String, Any?>? ->
        val value = param?.get("enableCreateEvent")
        value == true || value == "true" || (value as? Number)?.toInt() == 1
    }
    var localSecurityState by remember(currentItem.monitor.id) {
        mutableStateOf(getActualSecurityState(currentItem.monitor.param))
    }
    var lastInteractionTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(currentItem.monitor.param) {
        val serverState = getActualSecurityState(currentItem.monitor.param)
        val currentTime = System.currentTimeMillis()
        if (serverState != localSecurityState && (currentTime - lastInteractionTime > 2500)) {
            localSecurityState = serverState
        }
    }

    var showConfirmDialog by remember { mutableStateOf(false) }
    var pendingCheckedState by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        AppConfirmDialog(
            title = "${if (pendingCheckedState) "Bật" else "Tắt"} an ninh",
            message = "Xác nhận thay đổi trạng thái an ninh cho camera này?",
            confirmColor = if (pendingCheckedState) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            onDismiss = { showConfirmDialog = false },
            onConfirm = {
                lastInteractionTime = System.currentTimeMillis()
                localSecurityState = pendingCheckedState
                viewModel.toggleMonitorStatus(currentItem, pendingCheckedState)

                showConfirmDialog = false
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color.Black)
        ) {
            MjpegStreamPlayer(
                url = currentItem.streamUrl,
                thumbnailUrl = currentItem.snapshotUrl,
                deviceKey = currentItem.deviceKey
            )
            Surface(
                color = Color.Red.copy(alpha = 0.8f),
                shape = RoundedCornerShape(MaterialTheme.spacing.extraSmall),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(5.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(6.dp)
                            .background(Color.White, CircleShape)
                    )
                    Spacer(Modifier.width(MaterialTheme.spacing.extraSmall))
                    Text("LIVE", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (isLogicPerson) {
            val hasSecurityParam = currentItem.monitor.param?.containsKey("enableCreateEvent") == true
            Card(
                modifier = Modifier
                    .padding(MaterialTheme.spacing.medium)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Trạng thái an ninh",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (hasSecurityParam) Color.Unspecified else Color.Gray
                    )
                    Switch(
                        checked = localSecurityState,
                        enabled = currentItem.monitor.param?.containsKey("enableCreateEvent") == true,
                        onCheckedChange = { newValue ->
                            pendingCheckedState = newValue
                            showConfirmDialog = true
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }

        Text(
            text = "Sự kiện gần đây",
            modifier = Modifier.padding(
                start = 20.dp, bottom = 10.dp,
                top = if (isLogicPerson) 0.dp else 20.dp
            ),
            style = MaterialTheme.typography.titleSmall,
            color = Color.Gray,
            fontWeight = FontWeight.Bold
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = MaterialTheme.spacing.extraLarge)
        ) {
            items(items = events, key = { it.id }) { event ->
                RecentEventItem(
                    event = event,
                    onClick = {
                        viewModel.markEventAsRead(event)
                        onEventClick(event)
                    }
                )
            }
        }
    }
}