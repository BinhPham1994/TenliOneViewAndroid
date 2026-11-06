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
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import com.tenli.oneview.R
import com.tenli.oneview.main.MainActivity
import com.tenli.oneview.ui.component.enableAppEdgeToEdge
import com.tenli.oneview.ui.theme.IosGreen
import com.tenli.oneview.ui.theme.IosRed
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

private const val PULSE_DURATION_MS = 1200
private const val PULSE_MAX_SCALE = 1.6f
private const val PULSE_START_ALPHA = 0.4f

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surfaceGrouped)
            .safeDrawingPadding()
            .padding(top = spacing.space40),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = colors.surface,
            shadowElevation = spacing.space4,
            modifier = Modifier.size(spacing.iconHuge)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.logo_app),
                contentDescription = null,
                modifier = Modifier.padding(spacing.space12),
                tint = Color.Unspecified
            )
        }
        Spacer(modifier = Modifier.height(spacing.space24))
        Text(
            text = call.title,
            style = MaterialTheme.typography.titleLarge,
            color = colors.textTitle,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = spacing.space20)
        )
        Text(
            text = call.body,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = spacing.space4, start = spacing.space20, end = spacing.space20)
        )

        Spacer(modifier = Modifier.height(spacing.space40))

        // Ảnh sự kiện tỉ lệ 16:9 chuẩn camera, giới hạn bề rộng trên màn hình lớn
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.space10)
                .widthIn(max = spacing.contentMaxWidth)
                .aspectRatio(16f / 9f),
            shape = RoundedCornerShape(spacing.radiusMedium),
            shadowElevation = spacing.space1,
            color = colors.surface
        ) {
            AsyncImage(
                model = call.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
        }

        // Đẩy cụm nút xuống đáy; tự co lại trên máy màn hình thấp để không chồng lên nội dung
        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CallActionControl(
                icon = Icons.Default.Close,
                label = "Từ chối",
                color = IosRed,
                labelColor = colors.textMuted,
                isPulse = false,
                onClick = onDecline
            )
            CallActionControl(
                icon = Icons.Default.Call,
                label = "Xem ngay",
                color = IosGreen,
                labelColor = IosGreen,
                isPulse = true,
                onClick = onAccept
            )
        }

        Spacer(modifier = Modifier.height(spacing.space64 + spacing.space32))
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
    val buttonSize = spacing.avatarExtraLarge
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            if (isPulse) {
                // Hiệu ứng vòng tròn lan tỏa
                PulseAnimation(color = color, size = buttonSize)
            }

            IconButton(
                onClick = onClick,
                modifier = Modifier
                    .size(buttonSize)
                    .shadow(if (isPulse) spacing.space8 else 0.dp, CircleShape)
                    .background(color, CircleShape)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.appColors.onAccent,
                    modifier = Modifier.size(spacing.space28)
                )
            }
        }
        Text(
            text = label,
            color = labelColor,
            modifier = Modifier.padding(top = spacing.space16),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PulseAnimation(color: Color, size: androidx.compose.ui.unit.Dp) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseSpec = infiniteRepeatable<Float>(
        animation = tween(PULSE_DURATION_MS, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = PULSE_MAX_SCALE, animationSpec = pulseSpec, label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = PULSE_START_ALPHA, targetValue = 0f, animationSpec = pulseSpec, label = "alpha"
    )

    // Đọc giá trị animation trong graphicsLayer {} → chỉ vẽ lại, không recompose mỗi frame
    Box(
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .background(color, CircleShape)
    )
}
