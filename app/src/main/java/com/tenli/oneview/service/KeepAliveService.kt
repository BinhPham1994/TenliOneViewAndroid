package com.tenli.oneview.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.tenli.oneview.R
import com.tenli.oneview.main.MainActivity

/**
 * Foreground service giữ tiến trình sẵn sàng nhận cảnh báo (FCM / màn hình cuộc gọi).
 *
 * Lưu ý tiết kiệm pin: service KHÔNG còn tự mở WebSocket. Kết nối realtime do WebSocketManager quản lý
 * theo vòng đời giao diện (chỉ chạy khi app ở foreground). Cảnh báo khi app ở nền đi qua FCM.
 */
class KeepAliveService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        NotificationChannels.createAll(this)

        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, NotificationChannels.KEEP_ALIVE)
            .setContentTitle(getString(R.string.notif_keep_alive_title))
            .setContentText(getString(R.string.notif_keep_alive_text))
            .setSmallIcon(R.mipmap.app_icon_notify)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        return START_STICKY
    }

    private companion object {
        const val NOTIFICATION_ID = 1001
    }
}
