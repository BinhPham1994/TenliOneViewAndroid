package com.tenli.oneview.firebase

import android.app.Activity
import android.app.KeyguardManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import coil.compose.AsyncImage
import com.tenli.oneview.R
import com.tenli.oneview.main.MainActivity
import com.tenli.oneview.ui.theme.TenliAIoTTheme
import com.tenli.oneview.ui.utils.AppState

class CallComingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT), navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        setupWindowFlags()

        setContent {
            TenliAIoTTheme {
                CallScreenContent(
                    onClose = { finishAndRemoveTask() },
                    onAccept = { eId ->
                        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.cancel(eId.hashCode())
                        val intent = Intent(this@CallComingActivity, MainActivity::class.java).apply {
                            putExtra("EVENT_ID", eId)
                            putExtra("FROM_CALL", true)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }

    private fun setupWindowFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
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
fun CallScreenContent(onClose: () -> Unit, onAccept: (String) -> Unit) {
    val context = LocalContext.current
    val intent = (context as Activity).intent
    val title = intent.getStringExtra("TITLE") ?: "Cảnh báo an ninh"
    val body = intent.getStringExtra("BODY") ?: "Phát hiện sự kiện lạ"
    val imageUrl = intent.getStringExtra("URL")
    val eId = intent.getStringExtra("EID") ?: ""

    DisposableEffect(Unit) {
        AppState.isShowCallActivity = true
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        val ringtone = RingtoneManager.getRingtone(context, uri)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) ringtone.isLooping = true
        ringtone.play()

        val timer = java.util.Timer()
        timer.schedule(object : java.util.TimerTask() {
            override fun run() {
                context.runOnUiThread {
                    showMissedCallNotification(context, title, eId)
                    onClose()
                }
            }
        }, 30000)

        onDispose {
            ringtone.stop()
            timer.cancel()
            AppState.isShowCallActivity = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .safeDrawingPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape, color = Color.White, shadowElevation = 4.dp, modifier = Modifier.size(65.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.logo_app),
                    contentDescription = null, modifier = Modifier.padding(12.dp), tint = Color.Unspecified
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = title, style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, start = 20.dp, end = 20.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()             // Chiếm hết chiều ngang màn hình
                    .padding(horizontal = 10.dp) // Cách hai bên lề 24dp cho đẹp [cite: 2026-03-17]
                    .aspectRatio(16f / 9f),     // Ép tỉ lệ 16:9 chuẩn camera
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 1.dp,
                color = Color.White
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
            }
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
                .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically
        ) {
            CallActionControl(icon = Icons.Default.Close, label = "Từ chối", color = Color(0xFFFF3B30), labelColor = Color.Gray, isPulse = false, onClick = onClose)
            CallActionControl(icon = Icons.Default.Call, label = "Xem ngay", color = Color(0xFF34C759), labelColor = Color(0xFF34C759), isPulse = true, onClick = { onAccept(eId) })
        }
    }
}

private fun showMissedCallNotification(context: Context, title: String, eId: String) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Intent khi bấm vào thông báo nhỡ sẽ mở app vào sự kiện đó
    val intent = Intent(context, MainActivity::class.java).apply {
        putExtra("EVENT_ID", eId)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    val pendingIntent = PendingIntent.getActivity(
        context,
        eId.hashCode() + 1, // ID khác với cuộc gọi đang reo
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val missedCallBuilder = NotificationCompat.Builder(context, "ai_event_channel") // Dùng kênh cảnh báo thường
        .setSmallIcon(R.mipmap.app_icon_notify) // Thay bằng icon của bạn
        .setContentTitle("Cuộc gọi nhỡ: $title")
        .setContentText("Bấm để xem lại sự kiện cảnh báo")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .setCategory(NotificationCompat.CATEGORY_MESSAGE)

    notificationManager.notify(eId.hashCode() + 1, missedCallBuilder.build())
}

@Composable
fun CallActionControl(
    icon: ImageVector, label: String, color: Color, labelColor: Color, isPulse: Boolean, onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            if (isPulse) {
                // Hiệu ứng vòng tròn lan tỏa [cite: 2026-03-17]
                PulseAnimation(color = color)
            }

            IconButton(
                onClick = onClick, modifier = Modifier
                    .size(72.dp)
                    .shadow(if (isPulse) 8.dp else 0.dp, CircleShape)
                    .background(color, CircleShape)
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
            }
        }
        Text(
            text = label, color = labelColor, modifier = Modifier.padding(top = 16.dp), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PulseAnimation(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.6f, animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing), repeatMode = RepeatMode.Restart
        ), label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0f, animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing), repeatMode = RepeatMode.Restart
        ), label = "alpha"
    )

    Box(
        modifier = Modifier
            .size(72.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha)
            .background(color, CircleShape)
    )
}