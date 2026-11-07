package com.tenli.oneview.firebase

import android.app.ActivityOptions
import android.app.KeyguardManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.window.SplashScreen
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.main.MainActivity
import com.tenli.oneview.ui.component.enableAppEdgeToEdge
import com.tenli.oneview.ui.component.rememberIsEnglish
import com.tenli.oneview.ui.theme.LucideIcons
import com.tenli.oneview.ui.theme.TenliAIoTTheme
import com.tenli.oneview.ui.theme.appColors
import com.tenli.oneview.ui.theme.spacing
import com.tenli.oneview.ui.utils.AppState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "CallComingActivity"

/** Tự đóng màn cuộc gọi (và hiện thông báo cuộc gọi nhỡ) sau khoảng thời gian này */
private const val CALL_TIMEOUT_MS = 30_000L
private const val MISSED_CALL_CHANNEL_ID = "ai_event_channel"
private const val EXTRA_TITLE = "TITLE"
private const val EXTRA_BODY = "BODY"
private const val EXTRA_URL = "URL"
private const val EXTRA_EID = "EID"
private const val EXTRA_EVENT_ID = "EVENT_ID"
private const val EXTRA_FROM_CALL = "FROM_CALL"

private const val PULSE_DURATION_MS = 1400
private const val PULSE_MAX_SCALE = 1.62f
private const val PULSE_START_ALPHA = 0.36f
private const val PULSE_INNER_MAX_SCALE = 1.32f
private const val PULSE_INNER_START_ALPHA = 0.25f

private val CallLogoSize = 72.dp
private val CallCardShadowAmbientDark = 0.42f
private val CallCardShadowAmbientLight = 0.14f
private val CallCardShadowSpotDark = 0.52f
private val CallCardShadowSpotLight = 0.18f

/** Dữ liệu cuộc gọi cảnh báo nhận từ EventMessagingService */
@Immutable
private data class IncomingCall(
    val title: String,
    val body: String,
    val imageUrl: String?,
    val eventId: String
)

private fun Intent.toIncomingCall() = IncomingCall(
    title = getStringExtra(EXTRA_TITLE) ?: "Cảnh báo an ninh",
    body = getStringExtra(EXTRA_BODY) ?: "Phát hiện sự kiện lạ",
    imageUrl = getStringExtra(EXTRA_URL),
    eventId = getStringExtra(EXTRA_EID).orEmpty()
)

class CallComingActivity : ComponentActivity() {

    private var call by mutableStateOf(IncomingCall("", "", null, ""))
    private var ringtone: Ringtone? = null
    private var timeoutJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableAppEdgeToEdge()
        super.onCreate(savedInstanceState)
        setupWindowFlags()

        UserSession.ensureRestored()
        AppState.isShowCallActivity = true
        call = intent.toIncomingCall()
        startRinging()
        scheduleTimeout()

        setContent {
            TenliAIoTTheme {
                CallScreenContent(
                    call = call,
                    onDecline = ::declineCall,
                    onAccept = ::acceptCall
                )
            }
        }
    }

    /** singleInstance: cảnh báo mới đến khi màn đang mở → cập nhật nội dung và đếm lại thời gian chờ */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        call = intent.toIncomingCall()
        if (ringtone?.isPlaying != true) startRinging()
        scheduleTimeout()
    }

    override fun onDestroy() {
        timeoutJob?.cancel()
        stopRinging()
        AppState.isShowCallActivity = false
        super.onDestroy()
    }

    private fun startRinging() {
        stopRinging()
        ringtone = try {
            RingtoneManager.getRingtone(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE))?.apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) isLooping = true
                play()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Cannot play ringtone", e)
            null
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
    }

    private fun scheduleTimeout() {
        timeoutJob?.cancel()
        // lifecycleScope tự huỷ khi Activity bị destroy → không rò rỉ Timer/Handler
        timeoutJob = lifecycleScope.launch {
            delay(CALL_TIMEOUT_MS)
            val current = call
            showMissedCallNotification(this@CallComingActivity, current.title, current.eventId)
            closeCall(current.eventId)
        }
    }

    private fun declineCall() = closeCall(call.eventId)

    private fun acceptCall() {
        val eventId = call.eventId
        timeoutJob?.cancel()
        stopRinging()
        cancelCallNotification(eventId)
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                putExtra(EXTRA_EVENT_ID, eventId)
                putExtra(EXTRA_FROM_CALL, true)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
        )
        finish()
    }

    private fun closeCall(eventId: String) {
        timeoutJob?.cancel()
        stopRinging()
        // Thông báo "cuộc gọi" đang reo là ongoing → phải huỷ thủ công khi kết thúc cuộc gọi
        cancelCallNotification(eventId)
        finishAndRemoveTask()
    }

    /** Huỷ thông báo cuộc gọi (EventMessagingService đăng với id = eventId.hashCode()) */
    private fun cancelCallNotification(eventId: String) {
        if (eventId.isEmpty()) return
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(eventId.hashCode())
    }

    private fun setupWindowFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
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
    onDecline: () -> Unit,
    onAccept: () -> Unit
) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val isEn = rememberIsEnglish()
    val isDark = colors.isDark

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
    val snapshotShape = RoundedCornerShape(spacing.radiusCompact)

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
                    top = spacing.space24,
                    bottom = spacing.space32,
                    start = spacing.paddingScreen,
                    end = spacing.paddingScreen
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

            Spacer(modifier = Modifier.height(spacing.space10))

            Text(
                text = brandTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(spacing.space8))

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

            Spacer(modifier = Modifier.height(spacing.space20))

            // 2. Thẻ thông tin sự kiện & Ảnh chụp camera (đồng bộ style Card với LoginCard / SettingsGroupCard)
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.space16),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = call.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.textStrong,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (call.body.isNotBlank()) {
                        Spacer(modifier = Modifier.height(spacing.space6))
                        Text(
                            text = call.body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textMuted,
                            textAlign = TextAlign.Center,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(spacing.space14))

                    // Khung ảnh sự kiện tỉ lệ 16:9 chuẩn camera
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(snapshotShape)
                            .background(colors.thumbnailBg)
                            .border(
                                width = spacing.borderThin,
                                color = colors.border,
                                shape = snapshotShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!call.imageUrl.isNullOrBlank()) {
                            SubcomposeAsyncImage(
                                model = call.imageUrl,
                                contentDescription = call.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                loading = {
                                    CallSnapshotPlaceholder(isEn = isEn)
                                },
                                error = {
                                    CallSnapshotPlaceholder(isEn = isEn)
                                }
                            )
                        } else {
                            CallSnapshotPlaceholder(isEn = isEn)
                        }

                        // Badge góc trên trái của ảnh
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(spacing.space8)
                                .clip(RoundedCornerShape(spacing.radiusTiny))
                                .background(colors.mediaOverlay)
                                .padding(horizontal = spacing.space8, vertical = spacing.space4),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = LucideIcons.Video,
                                contentDescription = null,
                                tint = colors.onMedia,
                                modifier = Modifier.size(spacing.iconTiny)
                            )
                            Spacer(modifier = Modifier.width(spacing.space4))
                            Text(
                                text = if (isEn) "AI Snapshot" else "Ảnh sự kiện",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.onMedia
                            )
                        }
                    }
                }
            }

            // Đẩy cụm nút hành động xuống đáy màn hình
            Spacer(modifier = Modifier.weight(1f))

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

            Spacer(modifier = Modifier.height(spacing.space20))

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
                    color = colors.success,
                    labelColor = colors.success,
                    isPulse = true,
                    onClick = onAccept
                )
            }

            Spacer(modifier = Modifier.height(spacing.space16))
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

    val pendingIntent = PendingIntent.getActivity(
        context,
        eId.hashCode() + 1, // ID khác với cuộc gọi đang reo
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        splashOptions
    )

    val missedCallBuilder = NotificationCompat.Builder(context, MISSED_CALL_CHANNEL_ID) // Dùng kênh cảnh báo thường
        .setSmallIcon(R.mipmap.app_icon_notify)
        .setContentTitle("Cuộc gọi nhỡ: $title")
        .setContentText("Bấm để xem lại sự kiện cảnh báo")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .setCategory(NotificationCompat.CATEGORY_MESSAGE)

    notificationManager.notify(eId.hashCode() + 1, missedCallBuilder.build())
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
