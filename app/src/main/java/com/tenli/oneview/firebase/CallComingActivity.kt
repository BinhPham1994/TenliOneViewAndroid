package com.tenli.oneview.firebase

import android.app.ActivityOptions
import android.app.KeyguardManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.WindowManager
import android.window.SplashScreen
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import coil.compose.SubcomposeAsyncImage
import com.tenli.oneview.R
import com.tenli.oneview.data.local.EventCacheManager
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.data.network.ApiProvider
import com.tenli.oneview.main.MainActivity
import com.tenli.oneview.model.network.AIServiceModel
import com.tenli.oneview.model.network.BsProfileGroupModel
import com.tenli.oneview.model.network.BsProfileModel
import com.tenli.oneview.model.network.CameraModel
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.ui.component.VideoFrameImage
import com.tenli.oneview.ui.component.VideoPlayer
import com.tenli.oneview.ui.component.enableAppEdgeToEdge
import com.tenli.oneview.ui.component.rememberIsEnglish
import com.tenli.oneview.ui.features.event.detail.EventDetailDisplay
import com.tenli.oneview.ui.features.event.detail.buildEventDetailDisplay
import com.tenli.oneview.ui.features.event.resolveCameraForEvent
import com.tenli.oneview.ui.theme.ErrorRed
import com.tenli.oneview.ui.theme.LucideIcons
import com.tenli.oneview.ui.theme.OrangeDeep
import com.tenli.oneview.ui.theme.TenliAIoTTheme
import com.tenli.oneview.ui.theme.appColors
import com.tenli.oneview.ui.theme.spacing
import com.tenli.oneview.ui.utils.AiTypeHelper
import com.tenli.oneview.ui.utils.AppState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "CallComingActivity"

/** Tự đóng màn cuộc gọi (và hiện thông báo cuộc gọi nhỡ) sau khoảng thời gian này */
private const val CALL_TIMEOUT_MS = 30_000L
private const val MISSED_CALL_CHANNEL_ID = com.tenli.oneview.service.NotificationChannels.EVENT
private const val EXTRA_TITLE = "TITLE"
private const val EXTRA_BODY = "BODY"
private const val EXTRA_URL = "URL"
private const val EXTRA_EID = "EID"
private const val EXTRA_NOTIFICATION_ID = "NOTIFICATION_ID"
private const val EXTRA_EVENT_TYPE = "EVENT_TYPE"
private const val EXTRA_CAMERA_UUID = "CAMERA_UUID"
private const val EXTRA_CAMERA_NAME = "CAMERA_NAME"
private const val EXTRA_EVENT_TIME = "EVENT_TIME"
private const val EXTRA_EVENT_ID = "EVENT_ID"
private const val EXTRA_FROM_CALL = "FROM_CALL"

private const val PULSE_DURATION_MS = 1400
private const val PULSE_MAX_SCALE = 1.62f
private const val PULSE_START_ALPHA = 0.36f
private const val PULSE_INNER_MAX_SCALE = 1.32f
private const val PULSE_INNER_START_ALPHA = 0.25f

private val CallLogoSize = 72.dp
private val CallAcceptGreen = Color(0xFF16A34A)
private val CallCardShadowAmbientDark = 0.42f
private val CallCardShadowAmbientLight = 0.14f
private val CallCardShadowSpotDark = 0.52f
private val CallCardShadowSpotLight = 0.18f
private const val CALL_BOUNDING_BOX_STROKE_PX = 3.5f
private const val CALL_CROP_ASPECT_RATIO = 72f / 90f
private const val CALL_SWIPE_THRESHOLD_PX = 70f

private enum class CallMediaTab {
    EVENT_IMAGE,
    EVENT_VIDEO
}

/** Dữ liệu cuộc gọi cảnh báo nhận từ EventMessagingService */
@Immutable
private data class IncomingCall(
    val title: String,
    val body: String,
    val imageUrl: String?,
    val eventId: String,
    val notificationId: Int = 0,
    val eventType: String = "",
    val cameraUuid: String = "",
    val cameraName: String = "",
    val eventTime: Double = 0.0
)

private fun Intent.toIncomingCall() = IncomingCall(
    title = getStringExtra(EXTRA_TITLE) ?: "Cảnh báo an ninh",
    body = getStringExtra(EXTRA_BODY) ?: "",
    imageUrl = getStringExtra(EXTRA_URL),
    eventId = getStringExtra(EXTRA_EID).orEmpty(),
    notificationId = getIntExtra(EXTRA_NOTIFICATION_ID, 0),
    eventType = getStringExtra(EXTRA_EVENT_TYPE).orEmpty(),
    cameraUuid = getStringExtra(EXTRA_CAMERA_UUID).orEmpty(),
    cameraName = getStringExtra(EXTRA_CAMERA_NAME).orEmpty(),
    eventTime = getDoubleExtra(EXTRA_EVENT_TIME, 0.0)
)

class CallComingActivity : ComponentActivity() {

    private var call by mutableStateOf(IncomingCall("", "", null, ""))
    private var enrichedEvent by mutableStateOf<EventData?>(null)
    private var enrichedCamera by mutableStateOf<CameraModel?>(null)
    private var enrichedAiService by mutableStateOf<AIServiceModel?>(null)
    private var enrichedProfiles by mutableStateOf<List<BsProfileModel>>(emptyList())
    private var enrichedGroups by mutableStateOf<List<BsProfileGroupModel>>(emptyList())
    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null
    private var timeoutJob: Job? = null
    private var eventFetchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableAppEdgeToEdge()
        super.onCreate(savedInstanceState)
        setupWindowFlags()

        UserSession.ensureRestored()
        AppState.isShowCallActivity = true
        val initialCall = intent.toIncomingCall()
        call = initialCall
        loadEventDetailsForCall(initialCall)
        startRinging()
        scheduleTimeout()

        setContent {
            TenliAIoTTheme {
                CallScreenContent(
                    call = call,
                    enrichedEvent = enrichedEvent,
                    enrichedCamera = enrichedCamera,
                    enrichedAiService = enrichedAiService,
                    enrichedProfiles = enrichedProfiles,
                    enrichedGroups = enrichedGroups,
                    onUserInteract = ::scheduleTimeout,
                    onDecline = ::declineCall,
                    onAccept = ::acceptCall
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        AppState.isShowCallActivity = true
        // Khi màn hình Call đã hiển thị trực tiếp trước mặt người dùng, gỡ thông báo Heads-Up trên thanh trạng thái
        // để không bị đè banner lên giao diện Call và không phát 2 tiếng nhạc chuông chồng nhau.
        cancelCallNotification(call.eventId, call.notificationId)
    }

    /** singleInstance: cảnh báo mới đến khi màn đang mở → cập nhật nội dung và đếm lại thời gian chờ */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val previous = call
        val next = intent.toIncomingCall()
        cancelCallNotification(previous.eventId, previous.notificationId)
        cancelCallNotification(next.eventId, next.notificationId)
        setIntent(intent)
        call = next
        loadEventDetailsForCall(next)
        if (ringtone?.isPlaying != true) startRinging()
        scheduleTimeout()
    }

    override fun onDestroy() {
        timeoutJob?.cancel()
        eventFetchJob?.cancel()
        stopRinging()
        AppState.isShowCallActivity = false
        super.onDestroy()
    }

    /**
     * Tải đầy đủ chi tiết sự kiện (`EventData`, camera, hệ thống AI, hồ sơ khuôn mặt/đồng phục)
     * từ cache hoặc API theo `eventId` để màn hình cuộc gọi luôn hiển thị trọn vẹn thông tin
     * (tên bài AI, đối tượng/biển số, tên camera, thời gian, ảnh sự kiện + khung bounding box + ảnh crop).
     */
    private fun loadEventDetailsForCall(incoming: IncomingCall) {
        eventFetchJob?.cancel()
        val memCameras = EventCacheManager.getCamerasInMemory()
        val memServices = EventCacheManager.getAiServicesInMemory()
        val memProfiles = EventCacheManager.getProfilesInMemory()
        val memGroups = EventCacheManager.getProfileGroupsInMemory()

        enrichedEvent = null
        enrichedCamera = if (incoming.cameraUuid.isNotBlank()) {
            memCameras.find { it.extra?.uuid.equals(incoming.cameraUuid, ignoreCase = true) }
        } else {
            null
        }
        enrichedAiService = null
        enrichedProfiles = memProfiles
        enrichedGroups = memGroups

        val eventIdInt = incoming.eventId.trim().toIntOrNull() ?: return
        EventCacheManager.getEventInMemory(eventIdInt)?.let { cachedEv ->
            enrichedEvent = cachedEv
            enrichedCamera = resolveCameraForEvent(cachedEv, memCameras) ?: enrichedCamera
            enrichedAiService = memServices.find { it.id == cachedEv.serviceId }
        }

        eventFetchJob = lifecycleScope.launch {
            runCatching {
                var foundEvent = enrichedEvent
                var cameraList = memCameras

                if (foundEvent == null) {
                    val diskPair = withContext(Dispatchers.IO) {
                        EventCacheManager.findEventById(applicationContext, eventIdInt)
                    }
                    if (diskPair != null) {
                        foundEvent = diskPair.first
                        if (diskPair.second.isNotEmpty()) {
                            cameraList = diskPair.second
                        }
                    }
                }

                if (foundEvent == null || foundEvent.data?.video.isNullOrBlank()) {
                    val resp = withContext(Dispatchers.IO) {
                        runCatching { ApiProvider.eventApi.getDataList(lastId = eventIdInt + 1, count = 5) }.getOrNull()
                    }
                    val fetched = resp?.body()?.firstOrNull { it.id == eventIdInt }
                    if (fetched != null) {
                        foundEvent = fetched
                    }
                }

                val targetEvent = foundEvent ?: return@runCatching
                val needProfiles = AiTypeHelper.isFaceEvent(targetEvent) ||
                    targetEvent.type.lowercase().contains("uniform")

                val resolvedCamInitial = resolveCameraForEvent(targetEvent, cameraList) ?: enrichedCamera
                val resolvedSvcInitial = memServices.find { it.id == targetEvent.serviceId }
                enrichedEvent = targetEvent
                enrichedCamera = resolvedCamInitial
                enrichedAiService = resolvedSvcInitial

                val fetchCameras = cameraList.isEmpty()
                val fetchServices = memServices.isEmpty()
                val fetchProfiles = needProfiles && memProfiles.isEmpty()
                val fetchGroups = needProfiles && memGroups.isEmpty()

                var latestCameras = cameraList
                var latestServices = memServices

                if (fetchCameras || fetchServices || fetchProfiles || fetchGroups) {
                    coroutineScope {
                        val camDef = async(Dispatchers.IO) {
                            if (fetchCameras) {
                                runCatching { ApiProvider.vmsApi.getCameraList() }.getOrNull()?.body() ?: emptyList()
                            } else cameraList
                        }
                        val svcDef = async(Dispatchers.IO) {
                            if (fetchServices) {
                                runCatching { ApiProvider.eventApi.getAIServiceList() }.getOrNull()?.body() ?: emptyList()
                            } else memServices
                        }
                        val profDef = async(Dispatchers.IO) {
                            if (fetchProfiles) {
                                runCatching { ApiProvider.bsApi.getProfileList() }.getOrNull()?.body() ?: emptyList()
                            } else memProfiles
                        }
                        val grpDef = async(Dispatchers.IO) {
                            if (fetchGroups) {
                                runCatching { ApiProvider.bsApi.getProfileGroupList() }.getOrNull()?.body() ?: emptyList()
                            } else memGroups
                        }

                        val finalCameras = camDef.await()
                        val finalServices = svcDef.await()
                        val finalProfiles = profDef.await()
                        val finalGroups = grpDef.await()

                        latestCameras = finalCameras
                        latestServices = finalServices

                        EventCacheManager.cacheInMemory(
                            events = listOf(targetEvent),
                            cameras = finalCameras,
                            aiServices = finalServices
                        )
                        enrichedCamera = resolveCameraForEvent(targetEvent, finalCameras) ?: enrichedCamera
                        enrichedAiService = finalServices.find { it.id == targetEvent.serviceId }
                        enrichedProfiles = finalProfiles
                        enrichedGroups = finalGroups
                    }
                } else {
                    EventCacheManager.cacheInMemory(
                        events = listOf(targetEvent),
                        cameras = cameraList,
                        aiServices = memServices
                    )
                }

                // Nếu ngay lúc nhận thông báo FCM, server AI đang hoàn tất ghi file .mp4 của sự kiện,
                // kiểm tra lại 1 lần sau 1.8s để tự động hiển thị tab "Video sự kiện" ngay khi có.
                if (targetEvent.data?.video.isNullOrBlank()) {
                    delay(1800L)
                    val retryResp = withContext(Dispatchers.IO) {
                        runCatching { ApiProvider.eventApi.getDataList(lastId = eventIdInt + 1, count = 5) }.getOrNull()
                    }
                    val refreshed = retryResp?.body()?.firstOrNull { it.id == eventIdInt }
                    if (refreshed != null && !refreshed.data?.video.isNullOrBlank()) {
                        enrichedEvent = refreshed
                        EventCacheManager.cacheInMemory(
                            events = listOf(refreshed),
                            cameras = latestCameras,
                            aiServices = latestServices
                        )
                    }
                }
            }
        }
    }

    private fun startRinging() {
        stopRinging()
        val ringAttrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        ringtone = try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            RingtoneManager.getRingtone(this, uri)?.apply {
                audioAttributes = ringAttrs
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) isLooping = true
                play()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Cannot play ringtone", e)
            null
        }

        // Bật rung lặp theo nhịp cuộc gọi (tương thích mọi máy kể cả khi để chế độ Rung)
        runCatching {
            val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (vib?.hasVibrator() == true) {
                vibrator = vib
                val pattern = longArrayOf(0L, 1000L, 800L)
                @Suppress("DEPRECATION")
                vib.vibrate(VibrationEffect.createWaveform(pattern, 0), ringAttrs)
            }
        }
    }

    private fun stopRinging() {
        ringtone?.let {
            try {
                it.stop()
            } catch (e: Exception) {
                Log.w(TAG, "Cannot stop ringtone", e)
            }
        }
        ringtone = null
        vibrator?.let {
            runCatching { it.cancel() }
        }
        vibrator = null
    }

    private fun scheduleTimeout() {
        timeoutJob?.cancel()
        // lifecycleScope tự huỷ khi Activity bị destroy → không rò rỉ Timer/Handler
        timeoutJob = lifecycleScope.launch {
            delay(CALL_TIMEOUT_MS)
            val current = call
            showMissedCallNotification(this@CallComingActivity, current.title, current.eventId)
            closeCall(current.eventId, current.notificationId)
        }
    }

    private fun declineCall() = closeCall(call.eventId, call.notificationId)

    private fun acceptCall() {
        val current = call
        timeoutJob?.cancel()
        stopRinging()
        cancelCallNotification(current.eventId, current.notificationId)
        runCatching {
            startActivity(
                Intent(this, MainActivity::class.java).apply {
                    putExtra(EXTRA_EVENT_ID, current.eventId)
                    putExtra(EXTRA_FROM_CALL, true)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
            )
        }
        finish()
    }

    private fun closeCall(eventId: String, notificationId: Int) {
        timeoutJob?.cancel()
        stopRinging()
        // Thông báo "cuộc gọi" đang reo là ongoing → phải huỷ thủ công khi kết thúc cuộc gọi
        cancelCallNotification(eventId, notificationId)
        finishAndRemoveTask()
    }

    /** Huỷ thông báo cuộc gọi (EventMessagingService đăng với id = notificationId hoặc eventId.hashCode()) */
    private fun cancelCallNotification(eventId: String, notificationId: Int = 0) {
        runCatching {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            if (notificationId != 0) {
                notificationManager.cancel(notificationId)
            }
            if (eventId.isNotEmpty()) {
                notificationManager.cancel(eventId.hashCode())
            }
        }
    }

    private fun setupWindowFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            runCatching {
                val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
                keyguardManager?.requestDismissKeyguard(this, null)
            }
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }
}

@Composable
private fun CallScreenContent(
    call: IncomingCall,
    enrichedEvent: EventData?,
    enrichedCamera: CameraModel?,
    enrichedAiService: AIServiceModel?,
    enrichedProfiles: List<BsProfileModel>,
    enrichedGroups: List<BsProfileGroupModel>,
    onUserInteract: () -> Unit,
    onDecline: () -> Unit,
    onAccept: () -> Unit
) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val isEn = rememberIsEnglish()
    val isDark = colors.isDark

    val display: EventDetailDisplay? = remember(
        enrichedEvent,
        enrichedCamera,
        enrichedAiService,
        enrichedProfiles,
        enrichedGroups,
        isEn
    ) {
        enrichedEvent?.let { ev ->
            buildEventDetailDisplay(
                event = ev,
                camera = enrichedCamera,
                aiService = enrichedAiService,
                profiles = enrichedProfiles,
                groups = enrichedGroups,
                isEn = isEn
            )
        }
    }

    val effectiveTitle = remember(display, call.title, call.eventType, isEn) {
        display?.aiTaskTitle?.takeIf { it.isNotBlank() }
            ?: call.title.takeIf { it.isNotBlank() }
            ?: if (call.eventType.isNotBlank()) AiTypeHelper.getTypeName(call.eventType, isEn) else if (isEn) "Security Alert" else "Cảnh báo an ninh"
    }

    val effectiveImageUrl = remember(display, call.imageUrl) {
        display?.imageUrl?.takeIf { it.isNotBlank() } ?: call.imageUrl?.takeIf { it.isNotBlank() }
    }

    val effectiveVideoUrl = remember(display, call.imageUrl) {
        display?.videoUrl?.takeIf { it.isNotBlank() }
            ?: call.imageUrl?.takeIf {
                it.isNotBlank() && it.substringBefore('?').endsWith(".mp4", ignoreCase = true)
            }
    }

    val hasEventVideo = !effectiveVideoUrl.isNullOrBlank()
    var selectedMediaTab by remember(call.eventId) { mutableStateOf(CallMediaTab.EVENT_IMAGE) }
    val activeMediaTab = if (hasEventVideo) selectedMediaTab else CallMediaTab.EVENT_IMAGE

    val primaryCropUrl = remember(display) {
        display?.cropUrls?.firstOrNull()?.takeIf { it.isNotBlank() }
    }

    val boxes = display?.boxes.orEmpty()

    val brandTitle = remember(colors.textTitle, colors.accent) {
        buildAnnotatedString {
            withStyle(SpanStyle(color = colors.textTitle)) { append("Tenli ") }
            withStyle(SpanStyle(color = colors.accent)) { append("OneView") }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "callHeaderPulse")
    val liveDotAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "liveDotAlpha"
    )
    val logoGlowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoGlowScale"
    )

    val cardShape = RoundedCornerShape(spacing.radiusExtraLarge)
    val cropShape = RoundedCornerShape(spacing.radiusSmall)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.pageBg)
    ) {
        // Lớp ánh sáng nền nhẹ phía trên tạo chiều sâu theo đúng tone thương hiệu
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(spacing.space64 * 4)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colors.accent.copy(alpha = if (isDark) 0.14f else 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(
                    top = spacing.space20,
                    bottom = spacing.space28,
                    start = spacing.space10,
                    end = spacing.space10
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Cụm Logo App chính thức (logo_pwa) + Tên thương hiệu + Nhãn trạng thái cuộc gọi cảnh báo
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(CallLogoSize + spacing.space16)
                        .graphicsLayer {
                            scaleX = logoGlowScale
                            scaleY = logoGlowScale
                        }
                        .background(
                            color = colors.accent.copy(alpha = if (isDark) 0.18f else 0.10f),
                            shape = RoundedCornerShape(spacing.radiusLarge)
                        )
                )
                Image(
                    painter = painterResource(id = R.drawable.logo_pwa),
                    contentDescription = "Tenli OneView",
                    modifier = Modifier.size(CallLogoSize)
                )
            }

            Spacer(modifier = Modifier.height(spacing.space8))

            Text(
                text = brandTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(spacing.space6))

            // Pill trạng thái cuộc gọi khẩn cấp (nền đỏ, chữ trắng)
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(colors.error)
                    .padding(horizontal = spacing.space12, vertical = spacing.space6),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(spacing.statusDot)
                        .graphicsLayer { alpha = liveDotAlpha }
                        .background(colors.onAccent, CircleShape)
                )
                Spacer(modifier = Modifier.width(spacing.space6))
                Text(
                    text = if (isEn) "AI SECURITY ALERT • INCOMING CALL" else "CẢNH BÁO AN NINH AI • CUỘC GỌI ĐẾN",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onAccent
                )
            }

            Spacer(modifier = Modifier.height(spacing.space16))

            // 2. Thẻ thông tin sự kiện & Media 16:9 tràn viền thẻ (hỗ trợ cả Ảnh sự kiện và Video sự kiện)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = spacing.contentMaxWidth)
                    .shadow(
                        elevation = if (isDark) spacing.space20 else spacing.space16,
                        shape = cardShape,
                        ambientColor = Color.Black.copy(
                            alpha = if (isDark) CallCardShadowAmbientDark else CallCardShadowAmbientLight
                        ),
                        spotColor = Color.Black.copy(
                            alpha = if (isDark) CallCardShadowSpotDark else CallCardShadowSpotLight
                        )
                    )
                    .border(
                        width = spacing.borderThin,
                        color = colors.border,
                        shape = cardShape
                    ),
                shape = cardShape,
                color = colors.surface
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = spacing.space16,
                                end = spacing.space16,
                                top = spacing.space16,
                                bottom = spacing.space12
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = effectiveTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.textStrong,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Hàng Badge đối tượng phát hiện (Tên khuôn mặt / Người lạ / Biển số / Đồng phục / Cháy khói / Đám đông)
                        CallEventSubjectBadges(
                            display = display,
                            enrichedEvent = enrichedEvent,
                            isEn = isEn
                        )

                        val showBodyLine = call.body.isNotBlank() &&
                            !call.body.equals(effectiveTitle, ignoreCase = true)

                        if (showBodyLine) {
                            Spacer(modifier = Modifier.height(spacing.space8))
                            Text(
                                text = call.body,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textMuted,
                                textAlign = TextAlign.Center,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Khung media sự kiện 16:9 tràn sát mép thẻ trắng (hỗ trợ cả Ảnh sự kiện và Video sự kiện nếu có)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .background(colors.thumbnailBg)
                            .then(
                                if (hasEventVideo) {
                                    Modifier.pointerInput(call.eventId, hasEventVideo) {
                                        var totalDragX = 0f
                                        detectHorizontalDragGestures(
                                            onDragStart = { totalDragX = 0f },
                                            onHorizontalDrag = { change, dragAmount ->
                                                change.consume()
                                                totalDragX += dragAmount
                                            },
                                            onDragEnd = {
                                                if (totalDragX <= -CALL_SWIPE_THRESHOLD_PX &&
                                                    selectedMediaTab != CallMediaTab.EVENT_VIDEO
                                                ) {
                                                    onUserInteract()
                                                    selectedMediaTab = CallMediaTab.EVENT_VIDEO
                                                } else if (totalDragX >= CALL_SWIPE_THRESHOLD_PX &&
                                                    selectedMediaTab != CallMediaTab.EVENT_IMAGE
                                                ) {
                                                    onUserInteract()
                                                    selectedMediaTab = CallMediaTab.EVENT_IMAGE
                                                }
                                            }
                                        )
                                    }
                                } else {
                                    Modifier
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = activeMediaTab,
                            modifier = Modifier.fillMaxSize(),
                            transitionSpec = {
                                val isForward = targetState == CallMediaTab.EVENT_VIDEO
                                val enterSlide = slideInHorizontally(
                                    animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
                                    initialOffsetX = { fullWidth -> if (isForward) fullWidth else -fullWidth }
                                ) + fadeIn(animationSpec = tween(180))
                                val exitSlide = slideOutHorizontally(
                                    animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
                                    targetOffsetX = { fullWidth -> if (isForward) -fullWidth else fullWidth }
                                ) + fadeOut(animationSpec = tween(180))
                                (enterSlide togetherWith exitSlide).using(SizeTransform(clip = true))
                            },
                            label = "callMediaTabTransition"
                        ) { tab ->
                            when (tab) {
                                CallMediaTab.EVENT_IMAGE -> {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (!effectiveImageUrl.isNullOrBlank()) {
                                            VideoFrameImage(
                                                url = effectiveImageUrl,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .then(if (boxes.isEmpty()) Modifier else Modifier.callBoundingBoxes(boxes)),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            CallSnapshotPlaceholder(isEn = isEn)
                                        }

                                        // Ảnh crop cận cảnh (khuôn mặt / biển số / đối tượng) ở góc phải trên
                                        if (!primaryCropUrl.isNullOrBlank() && primaryCropUrl != effectiveImageUrl) {
                                            SubcomposeAsyncImage(
                                                model = primaryCropUrl,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(spacing.space8)
                                                    .width(spacing.avatarLarge)
                                                    .aspectRatio(CALL_CROP_ASPECT_RATIO)
                                                    .clip(cropShape)
                                                    .background(colors.mediaOverlay)
                                                    .border(spacing.borderMedium, colors.onMedia.copy(alpha = 0.3f), cropShape)
                                            )
                                        }
                                    }
                                }

                                CallMediaTab.EVENT_VIDEO -> {
                                    if (!effectiveVideoUrl.isNullOrBlank()) {
                                        VideoPlayer(
                                            videoUrl = effectiveVideoUrl,
                                            thumbnailUrl = effectiveImageUrl,
                                            deviceKey = UserSession.accessToken,
                                            errorText = if (isEn) "No event video" else "Không có video sự kiện"
                                        )
                                    } else {
                                        CallSnapshotPlaceholder(isEn = isEn)
                                    }
                                }
                            }
                        }

                        // Đồng bộ màu nền & viền với tên camera (.info-name) và cụm nút (.info-action) trong ô Live Camera
                        val overlayPillBg = colors.mediaBg.copy(alpha = 0.42f)
                        val overlayPillBorder = colors.onMedia.copy(alpha = 0.12f)
                        val pillHeight = spacing.iconButtonSmall
                        val pillShape = RoundedCornerShape(spacing.radiusSmall)

                        // Góc trên trái: nếu có cả video sự kiện thì hiển thị thanh chuyển đổi "Ảnh sự kiện | Video sự kiện",
                        // nếu chỉ có ảnh thì hiển thị nhãn "Ảnh sự kiện".
                        if (hasEventVideo) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(spacing.space8)
                                    .height(pillHeight)
                                    .clip(pillShape)
                                    .background(overlayPillBg)
                                    .border(
                                        width = spacing.borderMedium,
                                        color = overlayPillBorder,
                                        shape = pillShape
                                    )
                                    .padding(horizontal = spacing.space2),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(spacing.space2)
                            ) {
                                CallMediaSwitchPill(
                                    icon = LucideIcons.Eye,
                                    label = if (isEn) "Event photo" else "Ảnh sự kiện",
                                    selected = activeMediaTab == CallMediaTab.EVENT_IMAGE,
                                    onClick = {
                                        onUserInteract()
                                        selectedMediaTab = CallMediaTab.EVENT_IMAGE
                                    }
                                )
                                CallMediaSwitchPill(
                                    icon = LucideIcons.Video,
                                    label = if (isEn) "Event video" else "Video sự kiện",
                                    selected = activeMediaTab == CallMediaTab.EVENT_VIDEO,
                                    onClick = {
                                        onUserInteract()
                                        selectedMediaTab = CallMediaTab.EVENT_VIDEO
                                    }
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(spacing.space8)
                                    .height(pillHeight)
                                    .clip(pillShape)
                                    .background(overlayPillBg)
                                    .border(
                                        width = spacing.borderMedium,
                                        color = overlayPillBorder,
                                        shape = pillShape
                                    )
                                    .padding(horizontal = spacing.space8),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(spacing.space6)
                            ) {
                                Icon(
                                    imageVector = LucideIcons.Video,
                                    contentDescription = null,
                                    tint = colors.onMedia,
                                    modifier = Modifier.size(spacing.iconSmall)
                                )
                                Text(
                                    text = if (isEn) "AI Snapshot" else "Ảnh sự kiện",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.onMedia,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.space16))

            Text(
                text = if (isEn) {
                    "Tap \"View now\" to open event details immediately"
                } else {
                    "Bấm \"Xem ngay\" để mở chi tiết sự kiện cảnh báo"
                },
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSubtle,
                textAlign = TextAlign.Center
            )

            // Đẩy cụm nút hành động xuống đáy màn hình
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(spacing.space16))

            // 3. Cụm nút Từ chối / Xem ngay chuẩn hệ thống
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = spacing.dialogMaxWidth),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CallActionControl(
                    icon = LucideIcons.X,
                    label = if (isEn) "Decline" else "Từ chối",
                    color = colors.error,
                    labelColor = colors.textBody,
                    isPulse = false,
                    onClick = onDecline
                )
                CallActionControl(
                    icon = LucideIcons.PhoneCall,
                    label = if (isEn) "View now" else "Xem ngay",
                    color = CallAcceptGreen,
                    labelColor = CallAcceptGreen,
                    isPulse = true,
                    onClick = onAccept
                )
            }

            Spacer(modifier = Modifier.height(spacing.space16))
        }
    }
}

@Composable
private fun CallMediaSwitchPill(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val contentAlpha = if (selected) 1f else 0.72f
    Row(
        modifier = Modifier
            .height(spacing.iconMedium)
            .clip(RoundedCornerShape(spacing.radiusTiny))
            .background(if (selected) colors.onMedia.copy(alpha = 0.2f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.space8),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.space4)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.onMedia.copy(alpha = contentAlpha),
            modifier = Modifier.size(spacing.iconSmall)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = colors.onMedia.copy(alpha = contentAlpha),
            maxLines = 1
        )
    }
}

/** Hiển thị badge đối tượng chính của sự kiện trên màn hình cuộc gọi (khuôn mặt, biển số, đồng phục, cháy/khói, đám đông) */
@Composable
private fun CallEventSubjectBadges(
    display: EventDetailDisplay?,
    enrichedEvent: EventData?,
    isEn: Boolean
) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val pillShape = RoundedCornerShape(spacing.radiusTiny)

    val badges = remember(display, enrichedEvent, isEn, colors.error) {
        buildList<Pair<String, Color>> {
            when {
                display?.face != null -> {
                    val face = display.face
                    if (face.profileName.isNotBlank() && face.profileName != "---") {
                        add(face.profileName to face.groupColor)
                    }
                    if (face.groupName.isNotBlank() &&
                        face.groupName != "---" &&
                        !face.groupName.equals(face.profileName, ignoreCase = true)
                    ) {
                        add(face.groupName to face.groupColor.copy(alpha = 0.85f))
                    }
                }
                !display?.plateNumber.isNullOrBlank() -> {
                    val plateColor = if (display!!.isPlateBlacklist) colors.error else OrangeDeep
                    add(display.plateNumber to plateColor)
                    if (display.vehicleType.isNotBlank()) {
                        add(display.vehicleType to AiTypeHelper.getAiColor(enrichedEvent?.type))
                    }
                }
                display?.uniform != null -> {
                    val uni = display.uniform
                    if (uni.isIdentified && uni.personName.isNotBlank()) {
                        add(uni.personName to uni.color)
                    }
                    if (uni.uniformLabel.isNotBlank()) {
                        add(uni.uniformLabel to uni.color)
                    }
                }
                !display?.fireAlertLabel.isNullOrBlank() -> {
                    add(display!!.fireAlertLabel!! to colors.error)
                }
                display?.crowd != null -> {
                    add(display.crowd.countText to AiTypeHelper.getAiColor(enrichedEvent?.type))
                }
                enrichedEvent != null -> {
                    val label = AiTypeHelper.getEventBadgeLabel(enrichedEvent, isEn)
                    if (label.isNotBlank() && !label.equals(display?.aiTaskTitle, ignoreCase = true)) {
                        add(label to AiTypeHelper.getAiColor(enrichedEvent))
                    }
                }
            }
        }
    }

    if (badges.isEmpty()) return

    Spacer(modifier = Modifier.height(spacing.space8))
    Row(
        horizontalArrangement = Arrangement.spacedBy(spacing.space6, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        badges.forEach { (text, bgColor) ->
            Box(
                modifier = Modifier
                    .clip(pillShape)
                    .background(bgColor)
                    .padding(horizontal = spacing.space10, vertical = spacing.space4),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onAccent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun Modifier.callBoundingBoxes(boxes: List<List<Float>>): Modifier = drawWithContent {
    drawContent()
    boxes.forEach { boxArr ->
        if (boxArr.size >= 4) {
            val minX = boxArr[0]
            val minY = boxArr[1]
            val maxX = boxArr[2]
            val maxY = boxArr[3]
            drawRect(
                color = ErrorRed,
                topLeft = Offset(minX * size.width, minY * size.height),
                size = Size((maxX - minX) * size.width, (maxY - minY) * size.height),
                style = Stroke(width = CALL_BOUNDING_BOX_STROKE_PX)
            )
        }
    }
}

@Composable
private fun CallSnapshotPlaceholder(isEn: Boolean) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(spacing.space16),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = LucideIcons.ShieldAlert,
            contentDescription = null,
            tint = colors.textPlaceholder,
            modifier = Modifier.size(spacing.iconLarge)
        )
        Spacer(modifier = Modifier.height(spacing.space6))
        Text(
            text = if (isEn) "Camera event snapshot" else "Hình ảnh sự kiện từ camera",
            style = MaterialTheme.typography.labelSmall,
            color = colors.textPlaceholder
        )
    }
}

private fun showMissedCallNotification(context: Context, title: String, eId: String) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Intent khi bấm vào thông báo nhỡ sẽ mở app vào sự kiện đó
    val intent = Intent(context, MainActivity::class.java).apply {
        putExtra(EXTRA_EVENT_ID, eId)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    val splashOptions = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
            ActivityOptions.makeBasic()
                .setSplashScreenStyle(SplashScreen.SPLASH_SCREEN_STYLE_ICON)
                .toBundle()
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            ActivityOptions.makeBasic().toBundle().apply {
                putInt("android.activity.splashScreenStyle", 1)
            }
        }
        else -> null
    }

    val piFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    val pendingIntent = runCatching {
        PendingIntent.getActivity(
            context,
            eId.hashCode() + 1, // ID khác với cuộc gọi đang reo
            intent,
            piFlags,
            splashOptions
        )
    }.getOrElse {
        PendingIntent.getActivity(context, eId.hashCode() + 1, intent, piFlags)
    }

    val missedCallBuilder = NotificationCompat.Builder(context, MISSED_CALL_CHANNEL_ID) // Dùng kênh cảnh báo thường
        .setSmallIcon(R.mipmap.app_icon_notify)
        .setContentTitle("Cuộc gọi nhỡ: $title")
        .setContentText("Bấm để xem lại sự kiện cảnh báo")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .setCategory(NotificationCompat.CATEGORY_MESSAGE)

    runCatching {
        notificationManager.notify(eId.hashCode() + 1, missedCallBuilder.build())
    }
}

@Composable
private fun CallActionControl(
    icon: ImageVector,
    label: String,
    color: Color,
    labelColor: Color,
    isPulse: Boolean,
    onClick: () -> Unit
) {
    val spacing = MaterialTheme.spacing
    val colors = MaterialTheme.appColors
    val buttonSize = spacing.avatarExtraLarge
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(buttonSize + spacing.space24),
            contentAlignment = Alignment.Center
        ) {
            if (isPulse) {
                PulseAnimation(color = color, size = buttonSize)
            }

            Box(
                modifier = Modifier
                    .size(buttonSize)
                    .shadow(
                        elevation = spacing.space10,
                        shape = CircleShape,
                        ambientColor = color.copy(alpha = 0.35f),
                        spotColor = color.copy(alpha = 0.45f)
                    )
                    .clip(CircleShape)
                    .background(color, CircleShape)
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = colors.onAccent,
                    modifier = Modifier.size(spacing.space28)
                )
            }
        }
        Spacer(modifier = Modifier.height(spacing.space6))
        Text(
            text = label,
            color = labelColor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PulseAnimation(color: Color, size: Dp) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val outerScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = PULSE_MAX_SCALE,
        animationSpec = infiniteRepeatable(
            animation = tween(PULSE_DURATION_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outerScale"
    )
    val outerAlpha by infiniteTransition.animateFloat(
        initialValue = PULSE_START_ALPHA,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(PULSE_DURATION_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outerAlpha"
    )
    val innerScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = PULSE_INNER_MAX_SCALE,
        animationSpec = infiniteRepeatable(
            animation = tween(PULSE_DURATION_MS, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "innerScale"
    )

    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    scaleX = outerScale
                    scaleY = outerScale
                    alpha = outerAlpha
                }
                .background(color, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    scaleX = innerScale
                    scaleY = innerScale
                    alpha = PULSE_INNER_START_ALPHA
                }
                .background(color, CircleShape)
        )
    }
}
