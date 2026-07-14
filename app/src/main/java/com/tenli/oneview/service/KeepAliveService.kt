package com.tenli.oneview.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.tenli.oneview.BuildConfig
import com.tenli.oneview.R
import com.tenli.oneview.TenliApp
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class KeepAliveService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var webSocketJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "keep_alive_channel")
            .setContentTitle("Hệ thống bảo vệ đang chạy")
            .setContentText("Tenli AIoT luôn sẵn sàng nhận cảnh báo 24/7")
            .setSmallIcon(R.mipmap.app_icon_notify)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1001, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING)
        } else {
            startForeground(1001, notification)
        }

        startWebSocket()

        return START_STICKY
    }

    private fun startWebSocket() {
        val app = application as TenliApp
        val wsManager = app.container.webSocketManager

        webSocketJob?.cancel()
        webSocketJob = serviceScope.launch {
            // Dùng UserSession.domain (domain thực tế sau login) thay vì BuildConfig.DOMAIN_CLOUD
            // vì LoginAuthInterceptor cũng rewrite URL về UserSession.domain
            val baseUrl = UserSession.domain.ifEmpty { BuildConfig.DOMAIN_CLOUD }
            if (UserSession.accessToken.isNotEmpty()) {
                wsManager.connectNotify(baseUrl)
                wsManager.connectAllReports()
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            "keep_alive_channel",
            "Dịch vụ chạy nền Tenli",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(serviceChannel)
    }
}