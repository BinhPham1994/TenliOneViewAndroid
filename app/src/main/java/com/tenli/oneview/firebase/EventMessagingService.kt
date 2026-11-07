package com.tenli.oneview.firebase

import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.window.SplashScreen
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import coil.Coil
import coil.request.ImageRequest
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.tenli.oneview.BuildConfig
import com.tenli.oneview.R
import com.tenli.oneview.TenliApp
import com.tenli.oneview.data.local.EventCacheManager
import com.tenli.oneview.data.local.UserPermissionManager
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.main.MainActivity
import com.tenli.oneview.service.NotificationChannels
import com.tenli.oneview.ui.utils.AiTypeHelper
import kotlinx.coroutines.launch

class EventMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        if (BuildConfig.DEBUG) {
            Log.i(FcmTokenManager.LOG_TAG, "=== [EventMessagingService.onNewToken] token=$token ===")
        }
        FcmTokenManager.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Đảm bảo phiên đăng nhập (domain + accessToken) đã được nạp vào RAM kể cả khi app đang bị kill
        UserSession.ensureRestored()

        if (!UserPermissionManager.canAccessEvents()) {
            if (BuildConfig.DEBUG) {
                Log.i(FcmTokenManager.LOG_TAG, "[BỎ QUA NOTIFY] Tài khoản không có quyền xem sự kiện AI")
            }
            return
        }

        val data = remoteMessage.data
        val eventType = (data["type"] ?: data["eventType"] ?: data["event_type"]).orEmpty().trim()
        if (eventType.isNotEmpty() && !AiTypeHelper.isAiTypeEnabled(eventType)) {
            if (BuildConfig.DEBUG) {
                Log.i(FcmTokenManager.LOG_TAG, "[BỎ QUA NOTIFY] Loại sự kiện '$eventType' không nằm trong quyền của user")
            }
            return
        }
        val pushCameraUuid = (data["cameraUUID"] ?: data["cameraId"] ?: data["camera_uuid"]).orEmpty().trim()
        if (pushCameraUuid.isNotEmpty() && UserPermissionManager.isCustomCameraPermission()) {
            val cachedCameras = EventCacheManager.getCamerasInMemory()
            if (cachedCameras.isNotEmpty()) {
                val allowedUuids = UserPermissionManager.buildAllowedCameraUuids(cachedCameras)
                if (allowedUuids != null && !allowedUuids.contains(pushCameraUuid.lowercase())) {
                    if (BuildConfig.DEBUG) {
                        Log.i(FcmTokenManager.LOG_TAG, "[BỎ QUA NOTIFY] Camera '$pushCameraUuid' không nằm trong quyền của user")
                    }
                    return
                }
            }
        }
        val notif = remoteMessage.notification
        if (BuildConfig.DEBUG) {
            Log.i(
                FcmTokenManager.LOG_TAG,
                "=== [NHẬN ĐƯỢC NOTIFY TỪ FIREBASE] messageId=${remoteMessage.messageId} | from=${remoteMessage.from} | sentTime=${remoteMessage.sentTime} | notification={title=${notif?.title}, body=${notif?.body}, image=${notif?.imageUrl}} | data=$data ==="
            )
        }
        val isCall = data["makeCall"].equals("true", ignoreCase = true) ||
            data["alarmMode"].equals("call", ignoreCase = true)

        val title = data["title"]
            ?: remoteMessage.notification?.title
            ?: getString(R.string.notif_default_title)
        val body = data["body"]
            ?: data["message"]
            ?: data["content"]
            ?: remoteMessage.notification?.body
            ?: ""
        val rawImageUrl = data["imageUrl"]
            ?: data["image"]
            ?: data["snapshotUrl"]
            ?: data["image_url"]
            ?: data["picture"]
            ?: remoteMessage.notification?.imageUrl?.toString()
        val imageUrl = resolveNotificationImageUrl(rawImageUrl)
        val eventId = (data["eId"] ?: data["eventId"] ?: data["event_id"] ?: data["id"]).orEmpty()

        if (BuildConfig.DEBUG) {
            Log.i(
                FcmTokenManager.LOG_TAG,
                "[XỬ LÝ NOTIFY] isCall=$isCall | eventId=$eventId | title='$title' | body='$body' | imageUrl=$imageUrl"
            )
        }

        if (isCall) {
            // Firebase tự động cung cấp sentTime (miliseconds)
            val delay = if (remoteMessage.sentTime > 0L) {
                System.currentTimeMillis() - remoteMessage.sentTime
            } else {
                0L
            }
            if (delay > MISSED_CALL_THRESHOLD_MS) {
                showMissedCallNotification(title, body, imageUrl, eventId)
            } else {
                handleIncomingCall(title, body, imageUrl, eventId)
            }
        } else {
            showRegularNotification(title, body, imageUrl, eventId)
        }
    }

    /**
     * Chuẩn hoá URL ảnh thông báo: nếu backend gửi đường dẫn tương đối (vd. `/Data/...`, `/files/download/...`),
     * ghép với `UserSession.domain` để Coil và `ImageAuthInterceptor` tải được ảnh kèm Bearer token.
     */
    private fun resolveNotificationImageUrl(rawUrl: String?): String? {
        val trimmed = rawUrl?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        if (trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true)
        ) {
            return trimmed
        }
        val base = UserSession.domain.trim().trimEnd('/')
        if (base.isEmpty()) return trimmed
        return "$base/${trimmed.trimStart('/')}"
    }

    private fun getSafeNotificationId(eId: String, offset: Int = 0): Int {
        return if (eId.isNotEmpty()) eId.hashCode() + offset else (System.currentTimeMillis().toInt() + offset)
    }

    /** Intent mở chi tiết sự kiện trong MainActivity (giữ nguyên instance đang mở nếu có) */
    private fun eventDetailPendingIntent(eId: String, requestCode: Int, fromNotification: Boolean): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(EXTRA_EVENT_ID, eId)
            if (fromNotification) putExtra(EXTRA_FROM_NOTIFICATION, true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            splashIconOptionsBundle()
        )
    }

    /**
     * Trên Android 12+ (API 31+), khi mở Activity từ PendingIntent (thông báo), hệ thống mặc định dùng
     * SPLASH_SCREEN_STYLE_SOLID_COLOR (chỉ hiện nền trắng trống không có logo). Truyền SPLASH_SCREEN_STYLE_ICON
     * để luôn hiển thị đầy đủ màn hình Splash có icon giống như khi mở từ màn hình chính.
     */
    private fun splashIconOptionsBundle(): Bundle? {
        return when {
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
            val areEnabled = manager.areNotificationsEnabled()
            manager.notify(id, builder.build())
            if (BuildConfig.DEBUG) {
                Log.i(
                    FcmTokenManager.LOG_TAG,
                    "✅ [HIỂN THỊ NOTIFY LÊN TRAY] notificationId=$id | areNotificationsEnabled=$areEnabled"
                )
            }
        } catch (e: SecurityException) {
            if (BuildConfig.DEBUG) {
                Log.e(FcmTokenManager.LOG_TAG, "❌ [THIẾU QUYỀN POST_NOTIFICATIONS] ${e.message}", e)
            }
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
