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
import com.tenli.oneview.data.local.GlobalData
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.main.MainActivity
import kotlinx.coroutines.*

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
            if (UserSession.accessToken != null) {
                wsManager.connectNotify(BuildConfig.DOMAIN_CLOUD)
                wsManager.connectAllReports()
            }

            wsManager.notifyEvent.collect { data ->
                if (data.event == "ai-data") {
                    showAiNotification(data.message)
                }
            }
        }
    }

    private fun showAiNotification(message: String) {
        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "keep_alive_channel")
            .setContentTitle("Sự kiện AI mới")
            .setContentText(message)
            .setSmallIcon(R.mipmap.app_icon_notify)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager?.notify(System.currentTimeMillis().toInt(), notification)
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