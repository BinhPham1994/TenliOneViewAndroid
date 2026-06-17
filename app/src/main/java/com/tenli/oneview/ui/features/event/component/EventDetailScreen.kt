package com.tenli.oneview.ui.features.event.component


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tenli.oneview.R
import com.tenli.oneview.data.local.db.AppDatabase
import com.tenli.oneview.data.mapper.EventProcessor
import com.tenli.oneview.data.repository.DataRepository
import com.tenli.oneview.model.network.EventItem
import com.tenli.oneview.ui.component.ActionButtonCircle
import com.tenli.oneview.ui.component.MjpegStreamPlayer
import com.tenli.oneview.ui.component.SafeAsyncImage
import com.tenli.oneview.ui.component.VideoPlayer
import com.tenli.oneview.ui.theme.spacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class VideoMode { RECORDED, LIVE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }
    val eventDao = database.eventDao()
    val snackbarHostState = remember { SnackbarHostState() }
    val appContainer = (context.applicationContext as com.tenli.oneview.TenliApp).container

    var event by remember { mutableStateOf<EventItem?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showMuteSheet by remember { mutableStateOf(false) }

    LaunchedEffect(eventId) {
        isLoading = true
        var retryCount = 0
        val maxRetries = 5
        while (retryCount < maxRetries) {
            try {
                if (DataRepository.deviceList.isEmpty()) {
                    DataRepository.restore()
                }
                var fetchedEvent = withContext(Dispatchers.IO) { eventDao.getEventById(eventId) }
                if (fetchedEvent == null) {
                    val eventRepository = appContainer.eventRepository
                    val result = eventRepository.getEventDetail(eventId)

                    if (result.isSuccess) {
                        val rawEvent = result.getOrNull()
                        if (rawEvent != null && rawEvent.id != 0L) {
                            fetchedEvent = EventProcessor.enrich(
                                rawEvent,
                                DataRepository.deviceList,
                                DataRepository.eventTypeDefs
                            )
                        }
                    }
                }
                if (fetchedEvent != null && fetchedEvent.id != 0L) {
                    event = fetchedEvent
                    if (fetchedEvent.isNew) {
                        scope.launch(Dispatchers.IO) { eventDao.markAsRead(eventId) }
                    }
                    break
                } else {
                    retryCount++
                    if (retryCount < maxRetries) {
                        delay(2000)
                    }
                }
            } catch (_: Exception) {
                retryCount++
            }
        }
        isLoading = false
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(40.dp), color = Color.White.copy(alpha = 0.5f))
        }
        return
    }

    val currentEvent = event ?: return
    var selectedMode by remember(currentEvent.id) { mutableStateOf(VideoMode.RECORDED) }

    if (showMuteSheet) {
        MuteEventSheet(
            onOptionSelected = { duration ->
                scope.launch(Dispatchers.IO) {
                    try {
                        val boxRepository = appContainer.boxRepository
                        val request = com.tenli.oneview.model.network.DeviceCommandRequest(command = "mute", duration = duration)
                        val result = boxRepository.controlDevice(
                            currentEvent.deviceUri ?: "",
                            currentEvent.deviceKey ?: "",
                            request
                        )
                        snackbarHostState.showSnackbar(if (result.isSuccess) "Đã tắt còi tạm thời" else "Lỗi thiết bị")
                    } catch (_: Exception) {
                        snackbarHostState.showSnackbar("Lỗi kết nối")
                    }
                }
            },
            onDismiss = { showMuteSheet = false }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = currentEvent.aiTitle,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis, // Hiện ... nếu quá dài
                        modifier = Modifier.fillMaxWidth() // Chiếm hết không gian ở giữa
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                },
                // Thêm phần này để cân bằng khoảng trống với bên trái [cite: 2026-03-18]
                actions = {
                    Spacer(modifier = Modifier.width(48.dp)) // 48dp là kích thước chuẩn của một IconButton
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground, // Hoặc Color.Unspecified
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(MaterialTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                "Thông tin sự kiện",
                style = MaterialTheme.typography.titleSmall,
                color = Color.Gray,
                modifier = Modifier.padding(start = MaterialTheme.spacing.extraSmall, top = MaterialTheme.spacing.extraSmall)
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(MaterialTheme.spacing.radiusMedium)
            ) {
                Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
                    DetailInfoRow(R.drawable.time_icon_red, formatFullTime(currentEvent.eTimestamp), Color.Gray)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = MaterialTheme.spacing.borderThin, color = Color.LightGray.copy(0.5f))
                    DetailInfoRow(R.drawable.notify, buildAnnotatedDescription(currentEvent), Color.Gray)
                }
            }

            Text(
                text = if (selectedMode == VideoMode.LIVE) "Hình ảnh trực tiếp" else "Video sự kiện",
                style = MaterialTheme.typography.titleSmall,
                color = Color.Gray,
                modifier = Modifier.padding(start = MaterialTheme.spacing.extraSmall)
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(MaterialTheme.spacing.radiusMedium),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                ) {
                    when (selectedMode) {
                        VideoMode.LIVE -> {
                            val liveUrl = currentEvent.monitorLiveUrl
                            if (!liveUrl.isNullOrBlank()) {
                                MjpegStreamPlayer(liveUrl, currentEvent.localImageUrl, currentEvent.deviceKey ?: "")
                            } else {
                                SafeAsyncImage(currentEvent.localImageUrl, currentEvent.deviceKey ?: "")
                            }
                            LiveBadge(
                                Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(MaterialTheme.spacing.small)
                            )
                        }

                        VideoMode.RECORDED -> {
                            val videoUrl = currentEvent.localVideoUrl
                            if (!videoUrl.isNullOrBlank()) {
                                key(videoUrl) { VideoPlayer(videoUrl, currentEvent.localImageUrl, currentEvent.deviceKey ?: "") }
                            } else {
                                SafeAsyncImage(currentEvent.localImageUrl, currentEvent.deviceKey ?: "")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.medium),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ActionButtonCircle(
                    icon = R.drawable.camera_icon,
                    label = "Trực tiếp",
                    isSelected = selectedMode == VideoMode.LIVE,
                    onClick = { if (currentEvent.sData?.monitorId != null) selectedMode = VideoMode.LIVE }
                )
                ActionButtonCircle(
                    icon = R.drawable.video_icon,
                    label = "Video",
                    isSelected = selectedMode == VideoMode.RECORDED,
                    onClick = { selectedMode = VideoMode.RECORDED }
                )
                ActionButtonCircle(
                    icon = R.drawable.sound_icon,
                    label = "Tắt còi",
                    isSelected = false,
                    onClick = { showMuteSheet = true },
                    activeColor = Color(0xFFE42E1B)
                )
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

private fun formatFullTime(timestamp: Long): String {
    return try {
        val sdf = java.text.SimpleDateFormat("HH:mm:ss dd/MM/yyyy", java.util.Locale.getDefault())
        sdf.format(java.util.Date(timestamp))
    } catch (_: Exception) {
        "Không rõ thời gian"
    }
}

@Composable
private fun buildAnnotatedDescription(event: EventItem): AnnotatedString {
    return androidx.compose.ui.text.buildAnnotatedString {
        val description = event.actionPart
        val profileName = event.eValues?.get("profileName") as? String
        if (profileName != null && description.contains(profileName)) {
            val before = description.substringBefore(profileName)
            val after = description.substringAfter(profileName)
            append(before)
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color.Black)) {
                append(profileName)
            }
            append(after)
        } else {
            append(description)
        }
        append(event.foundConnector)
        if (event.cameraName.isNotBlank()) {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
                append(event.cameraName)
            }
        }
        if (event.deviceName.isNotBlank()) {
            append(" (")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(event.deviceName)
            }
            append(")")
        }
    }
}

@Composable
fun LiveBadge(modifier: Modifier) {
    Row(
        modifier = modifier
            .background(Color.Red, RoundedCornerShape(MaterialTheme.spacing.extraSmall))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(Color.White, CircleShape)
        )
        Spacer(Modifier.width(MaterialTheme.spacing.extraSmall))
        Text("LIVE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DetailInfoRow(icon: Int, content: Any, iconTint: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = iconTint
        )
        Spacer(modifier = Modifier.width(12.dp))
        val style = MaterialTheme.typography.bodyMedium
        when (content) {
            is String -> Text(text = content, style = style)
            is AnnotatedString -> Text(text = content, style = style)
        }
    }
}
