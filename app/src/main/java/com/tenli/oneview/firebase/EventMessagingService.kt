package com.tenli.oneview.firebase

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import coil.Coil
import coil.request.ImageRequest
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.tenli.oneview.BuildConfig
import com.tenli.oneview.R
import com.tenli.oneview.TenliApp
import com.tenli.oneview.main.MainActivity
import com.tenli.oneview.service.NotificationChannels
import kotlinx.coroutines.launch

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class EventMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data
        if (BuildConfig.DEBUG) Log.d(TAG, "FCM data: $data")
        val isCall = data["makeCall"] == "true"

        val title = data["title"] ?: remoteMessage.notification?.title ?: getString(R.string.notif_default_title)
        val body = data["body"] ?: remoteMessage.notification?.body ?: ""
        val imageUrl = data["imageUrl"]
        val eventId = data["eId"] ?: ""

        if (isCall) {
            // Firebase tự động cung cấp sentTime (miliseconds)
            val delay = System.currentTimeMillis() - remoteMessage.sentTime
            if (delay > MISSED_CALL_THRESHOLD_MS) {
                showMissedCallNotification(title, body, imageUrl, eventId)
            } else {
                handleIncomingCall(title, body, imageUrl, eventId)
            }
        } else {
            showRegularNotification(title, body, imageUrl, eventId)
        }
    }

    private fun getSafeNotificationId(eId: String, offset: Int = 0): Int {
        return if (eId.isNotEmpty()) eId.hashCode() + offset else (System.currentTimeMillis().toInt() + offset)
    }

    /** Intent mở chi tiết sự kiện trong MainActivity */
    private fun eventDetailPendingIntent(eId: String, requestCode: Int, fromNotification: Boolean): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(EXTRA_EVENT_ID, eId)
            if (fromNotification) putExtra(EXTRA_FROM_NOTIFICATION, true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            this, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun showMissedCallNotification(title: String, body: String, url: String?, eId: String) {
        val notificationId = getSafeNotificationId(eId, 1)

        val builder = NotificationCompat.Builder(this, NotificationChannels.EVENT)
            .setSmallIcon(R.mipmap.app_icon_notify)
            .setContentTitle(getString(R.string.notif_missed_call_title, title))
            .setContentText(getString(R.string.notif_missed_call_text, body.take(MISSED_CALL_BODY_PREVIEW)))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(eventDetailPendingIntent(eId, notificationId, fromNotification = false))

        loadImageAndNotify(builder, url, notificationId)
    }

    private fun loadImageAndNotify(
        builder: NotificationCompat.Builder,
        url: String?,
        notificationId: Int
    ) {
        val manager = NotificationManagerCompat.from(this)

        // Gửi thông báo ngay lập tức để tránh process bị kill
        notifySafely(manager, notificationId, builder)

        if (url.isNullOrEmpty()) return
        val app = applicationContext as? TenliApp ?: return
        val loader = Coil.imageLoader(this)
        // Giới hạn kích thước decode: ảnh thông báo không cần full-res → tiết kiệm RAM/CPU
        val request = ImageRequest.Builder(this)
            .data(url)
            .size(NOTIFICATION_IMAGE_MAX_PX)
            .allowHardware(false)
            .build()
        app.appScope.launch {
            try {
                val bitmap = (loader.execute(request).drawable as? BitmapDrawable)?.bitmap ?: return@launch
                builder.setLargeIcon(bitmap)
                builder.setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmap))
                notifySafely(manager, notificationId, builder)
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) Log.w(TAG, "Load notification image failed: ${e.message}")
            }
        }
    }

    private fun notifySafely(manager: NotificationManagerCompat, id: Int, builder: NotificationCompat.Builder) {
        try {
            manager.notify(id, builder.build())
        } catch (e: SecurityException) {
            if (BuildConfig.DEBUG) Log.w(TAG, "Missing notification permission: ${e.message}")
        }
    }

    private fun handleIncomingCall(title: String, body: String, url: String?, eId: String) {
        val manager = NotificationManagerCompat.from(this)
        val notificationId = getSafeNotificationId(eId)

        val callIntent = Intent(this, CallComingActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("TITLE", title)
            putExtra("BODY", body)
            putExtra("URL", url)
            putExtra("EID", eId)
        }
        val callPendingIntent = PendingIntent.getActivity(
            this, notificationId, callIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, NotificationChannels.EMERGENCY_CALL)
            .setSmallIcon(R.mipmap.app_icon_notify)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(callPendingIntent, true)
            .setContentIntent(eventDetailPendingIntent(eId, notificationId + 2, fromNotification = true))
            .setOngoing(true)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        notifySafely(manager, notificationId, builder)

        try {
            startActivity(callIntent)
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.w(TAG, "Cannot start call activity: ${e.message}")
        }
    }

    private fun showRegularNotification(title: String, body: String, url: String?, eId: String) {
        val notificationId = getSafeNotificationId(eId)

        val builder = NotificationCompat.Builder(this, NotificationChannels.EVENT)
            .setSmallIcon(R.mipmap.app_icon_notify)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(eventDetailPendingIntent(eId, notificationId, fromNotification = true))

        loadImageAndNotify(builder, url, notificationId)
    }

    private companion object {
        const val TAG = "EventMessagingService"
        const val EXTRA_EVENT_ID = "EVENT_ID"
        const val EXTRA_FROM_NOTIFICATION = "FROM_NOTIFICATION"
        const val MISSED_CALL_THRESHOLD_MS = 60_000L
        const val MISSED_CALL_BODY_PREVIEW = 20
        const val NOTIFICATION_IMAGE_MAX_PX = 1024
    }
}
