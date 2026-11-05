package com.tenli.oneview.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import com.tenli.oneview.R

/**
 * Khai báo tập trung các Notification Channel của app.
 */
object NotificationChannels {
    const val EVENT = "ai_event_channel"
    const val EMERGENCY_CALL = "emergency_call_channel"
    private const val LEGACY_KEEP_ALIVE = "keep_alive_channel"

    private val CALL_VIBRATION_PATTERN = longArrayOf(0, 1000, 500, 1000)

    /** Tạo (hoặc cập nhật tên/mô tả theo ngôn ngữ) toàn bộ channel. An toàn khi gọi nhiều lần. */
    fun createAll(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        runCatching { manager.deleteNotificationChannel(LEGACY_KEEP_ALIVE) }

        val eventChannel = NotificationChannel(
            EVENT,
            context.getString(R.string.notif_channel_event_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notif_channel_event_desc)
            enableVibration(true)
            setShowBadge(true)
        }

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .build()
        val callChannel = NotificationChannel(
            EMERGENCY_CALL,
            context.getString(R.string.notif_channel_call_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notif_channel_call_desc)
            enableLights(true)
            enableVibration(true)
            vibrationPattern = CALL_VIBRATION_PATTERN
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setBypassDnd(true)
            setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE), audioAttributes)
        }

        manager.createNotificationChannels(listOf(eventChannel, callChannel))
    }
}

