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
    const val EVENT = "ai_event_channel_v2"
    const val EMERGENCY_CALL = "emergency_call_channel_v2"
    private val LEGACY_CHANNELS = listOf(
        "keep_alive_channel",
        "ai_event_channel",
        "emergency_call_channel"
    )

    val EVENT_VIBRATION_PATTERN = longArrayOf(0L, 250L, 150L, 250L)
    val CALL_VIBRATION_PATTERN = longArrayOf(0L, 1000L, 500L, 1000L)

    /** Tạo (hoặc cập nhật tên/mô tả theo ngôn ngữ) toàn bộ channel. An toàn khi gọi nhiều lần. */
    fun createAll(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        for (legacyId in LEGACY_CHANNELS) {
            runCatching { manager.deleteNotificationChannel(legacyId) }
        }
        // Dọn dẹp bất kỳ channel cũ nào bị lưu pattern rung [0] không hợp lệ trong hệ thống từ các bản cài trước
        runCatching {
            for (ch in manager.notificationChannels) {
                val pat = ch.vibrationPattern
                if (pat != null && pat.none { it > 0L }) {
                    manager.deleteNotificationChannel(ch.id)
                }
            }
        }

        val eventChannel = NotificationChannel(
            EVENT,
            context.getString(R.string.notif_channel_event_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notif_channel_event_desc)
            enableVibration(true)
            vibrationPattern = EVENT_VIBRATION_PATTERN
            setShowBadge(true)
        }

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .build()
        val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
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
            setSound(ringtoneUri, audioAttributes)
        }

        manager.createNotificationChannels(listOf(eventChannel, callChannel))
    }
}

