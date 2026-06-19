package com.tenli.oneview.firebase

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.core.app.NotificationCompat
import coil.Coil
import coil.request.ImageRequest
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.tenli.oneview.R
import com.tenli.oneview.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class EventMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data
        Log.e("FEfsfs", data.toString())
        val makeCallValue = data["makeCall"]
        val isCall = makeCallValue == "true"

        val title = data["title"] ?: remoteMessage.notification?.title ?: "Cảnh báo"
        val body = data["body"] ?: remoteMessage.notification?.body ?: ""
        val imageUrl = data["imageUrl"]
        val eventId = data["eId"] ?: ""

        if (isCall) {
            val currentTime = System.currentTimeMillis()
            val sentTime = remoteMessage.sentTime // Firebase tự động cung cấp (miliseconds)
            val delay = currentTime - sentTime
            if (delay > 60000) {
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

    private fun showMissedCallNotification(title: String, body: String, url: String?, eId: String) {
        val notificationId = getSafeNotificationId(eId, 1)

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("EVENT_ID", eId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pIntent = PendingIntent.getActivity(
            this, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, "ai_event_channel")
            .setSmallIcon(R.mipmap.app_icon_notify)
            .setContentTitle("Cuộc gọi nhỡ: $title")
            .setContentText("Phát hiện lúc: ${body.take(20)}...")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pIntent)

        loadImageAndNotify(builder, url, notificationId)
    }

    private fun loadImageAndNotify(
        builder: NotificationCompat.Builder,
        url: String?,
        notificationId: Int
    ) {
        val manager = androidx.core.app.NotificationManagerCompat.from(this)
        
        // Gửi thông báo ngay lập tức để tránh process bị kill
        try {
            manager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        if (!url.isNullOrEmpty()) {
            val loader = Coil.imageLoader(this)
            val request = ImageRequest.Builder(this).data(url).build()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val result = loader.execute(request)
                    val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
                    if (bitmap != null) {
                        builder.setLargeIcon(bitmap)
                        builder.setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmap))
                        try {
                            manager.notify(notificationId, builder.build())
                        } catch (e: SecurityException) {
                            e.printStackTrace()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun handleIncomingCall(title: String, body: String, url: String?, eId: String) {
        val manager = androidx.core.app.NotificationManagerCompat.from(this)
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

        val detailIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("EVENT_ID", eId)
            putExtra("FROM_NOTIFICATION", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val detailPendingIntent = PendingIntent.getActivity(
            this, notificationId + 2, detailIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, "emergency_call_channel")
            .setSmallIcon(R.mipmap.app_icon_notify)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(callPendingIntent, true)
            .setContentIntent(detailPendingIntent)
            .setOngoing(true)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        try {
            manager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        try {
            startActivity(callIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showRegularNotification(title: String, body: String, url: String?, eId: String) {
        val notificationId = getSafeNotificationId(eId)
        
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("EVENT_ID", eId)
            putExtra("FROM_NOTIFICATION", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, "ai_event_channel")
            .setSmallIcon(R.mipmap.app_icon_notify)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pIntent)

        loadImageAndNotify(builder, url, notificationId)
    }
}