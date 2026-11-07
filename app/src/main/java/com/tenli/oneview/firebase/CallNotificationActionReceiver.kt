package com.tenli.oneview.firebase

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Xử lý nút hành động "Bỏ qua" trực tiếp trên thông báo Heads-Up của cuộc gọi cảnh báo
 * (trong trường hợp thiết bị chưa cấp quyền hiển thị đè lên ứng dụng khác nên hệ thống hiển thị Heads-Up banner).
 */
class CallNotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ACTION_DISMISS_CALL_NOTIFICATION) return
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)
        val eventId = intent.getStringExtra(EXTRA_EVENT_ID).orEmpty()

        runCatching {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            if (notificationId != 0) {
                nm.cancel(notificationId)
            }
            if (eventId.isNotEmpty()) {
                nm.cancel(eventId.hashCode())
            }
        }
    }

    companion object {
        const val ACTION_DISMISS_CALL_NOTIFICATION = "com.tenli.oneview.action.DISMISS_CALL_NOTIFICATION"
        const val EXTRA_NOTIFICATION_ID = "NOTIFICATION_ID"
        const val EXTRA_EVENT_ID = "EID"
    }
}

