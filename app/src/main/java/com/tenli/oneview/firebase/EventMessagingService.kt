package com.tenli.oneview.firebase

import android.app.ActivityOptions
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
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
import com.tenli.oneview.data.local.GlobalData
import com.tenli.oneview.data.local.UserPermissionManager
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.data.network.ApiProvider
import com.tenli.oneview.data.network.retrofit.LoginAuthClient
import com.tenli.oneview.main.MainActivity
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.model.network.UserNotificationSettingsModel
import com.tenli.oneview.service.NotificationChannels
import com.tenli.oneview.ui.utils.AiTypeHelper
import com.tenli.oneview.ui.utils.AppState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull

class EventMessagingService : FirebaseMessagingService() {

    /**
     * Bảo hiểm 100% cho trường hợp backend gửi gói FCM kèm khối `notification` (`gcm.n.e = 1`) khi app đang ở nền/bị kill:
     * Mặc định SDK Firebase sẽ chặn bản tin có `gcm.n.e = 1` ở background để tự hiện thông báo hệ thống đơn giản
     * và KHÔNG gọi `onMessageReceived()`, khiến màn hình Cuộc gọi (`CallComingActivity`) không thể bật lên.
     * Hàm này chuyển thông tin `gcm.n.title` / `gcm.n.body` / `gcm.n.image` sang `data` và gỡ cờ `gcm.n.e`
     * để mọi bản tin FCM đều đi vào `onMessageReceived()` xử lý đồng nhất.
     */
    override fun handleIntent(intent: Intent?) {
        if (intent == null) {
            super.handleIntent(null)
            return
        }
        val patchedIntent = runCatching {
            val extras = intent.extras ?: return@runCatching intent
            val isNotifPayload = extras.getString("gcm.n.e") == "1" ||
                extras.getString("gcm.notification.e") == "1"
            if (!isNotifPayload) return@runCatching intent

            Intent(intent).apply {
                val gcmTitle = extras.getString("gcm.n.title") ?: extras.getString("gcm.notification.title")
                val gcmBody = extras.getString("gcm.n.body") ?: extras.getString("gcm.notification.body")
                val gcmImage = extras.getString("gcm.n.image") ?: extras.getString("gcm.notification.image")

                if (!gcmTitle.isNullOrBlank() && extras.getString("title").isNullOrBlank()) {
                    putExtra("title", gcmTitle)
                }
                if (!gcmBody.isNullOrBlank() && extras.getString("body").isNullOrBlank()) {
                    putExtra("body", gcmBody)
                }
                if (!gcmImage.isNullOrBlank() && extras.getString("imageUrl").isNullOrBlank()) {
                    putExtra("imageUrl", gcmImage)
                }
                removeExtra("gcm.n.e")
                removeExtra("gcm.notification.e")
            }
        }.getOrElse { intent }

        super.handleIntent(patchedIntent)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        if (BuildConfig.DEBUG) {
            Log.i(FcmTokenManager.LOG_TAG, "=== [EventMessagingService.onNewToken] token=$token ===")
        }
        FcmTokenManager.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Đảm bảo phiên đăng nhập (domain + accessToken) và kênh thông báo đã sẵn sàng kể cả khi app đang bị kill
        UserSession.ensureRestored()
        if (UserSession.accessToken.isEmpty() || UserSession.userData == null) {
            if (BuildConfig.DEBUG) {
                Log.i(FcmTokenManager.LOG_TAG, "[BỎ QUA NOTIFY] Chưa đăng nhập (accessToken hoặc userData rỗng)")
            }
            return
        }
        runCatching { NotificationChannels.createAll(this) }

        if (!UserPermissionManager.canAccessEvents()) {
            if (BuildConfig.DEBUG) {
                Log.i(FcmTokenManager.LOG_TAG, "[BỎ QUA NOTIFY] Tài khoản không có quyền xem sự kiện AI")
            }
            return
        }

        val prefs = runCatching { GlobalData.preferences }.getOrNull()
        if (prefs != null && !prefs.getBoolean("push_notifications_enabled", true)) {
            if (BuildConfig.DEBUG) {
                Log.i(FcmTokenManager.LOG_TAG, "[BỎ QUA NOTIFY] Người dùng đang tắt nhận thông báo chung")
            }
            return
        }

        val data = remoteMessage.data
        val notif = remoteMessage.notification
        if (BuildConfig.DEBUG) {
            Log.i(
                FcmTokenManager.LOG_TAG,
                "=== [NHẬN ĐƯỢC NOTIFY TỪ FIREBASE] messageId=${remoteMessage.messageId} | from=${remoteMessage.from} | sentTime=${remoteMessage.sentTime} | notification={title=${notif?.title}, body=${notif?.body}, image=${notif?.imageUrl}} | data=$data ==="
            )
        }

        val eventId = (
            data["eId"] ?: data["eventId"] ?: data["event_id"] ?: data["id"] ?: data["OPEN_EVENT_DETAIL"] ?: data["EVENT_ID"]
            ).orEmpty().trim()

        val rawTitle = data["title"]
            ?: notif?.title
            ?: ""
        val rawBody = data["body"]
            ?: data["message"]
            ?: data["content"]
            ?: notif?.body
            ?: ""

        var eventType = (
            data["type"] ?: data["eventType"] ?: data["event_type"] ?: data["sensorType"] ?: data["sensor_type"]
            ).orEmpty().trim()
        var pushCameraUuid = (
            data["cameraUUID"] ?: data["cameraId"] ?: data["camera_uuid"] ?: data["camera_id"]
            ).orEmpty().trim()
        var cameraName = (
            data["cameraName"] ?: data["camera_name"] ?: data["camera"] ?: data["deviceName"] ?: data["device_name"]
            ).orEmpty().trim()
        var rawImageUrl = data["imageUrl"]
            ?: data["image"]
            ?: data["snapshotUrl"]
            ?: data["image_url"]
            ?: data["imagePath"]
            ?: data["image_path"]
            ?: data["picture"]
            ?: notif?.imageUrl?.toString()
        var rawEventTime = (data["time"] ?: data["timestamp"] ?: data["eventTime"] ?: data["event_time"])?.toDoubleOrNull()

        // Nếu gói FCM thiếu `eventType` (hoặc thiếu ảnh/camera) nhưng có `eventId`, truy vấn nhanh EventData
        // để luôn xác định chính xác loại bài AI và áp dụng đúng cấu hình Cuộc gọi / Thông báo
        val eventIdInt = eventId.toIntOrNull()
        if (eventIdInt != null && (eventType.isEmpty() || rawImageUrl.isNullOrBlank() || pushCameraUuid.isEmpty())) {
            val resolvedEvent = fetchEventQuickly(eventIdInt)
            if (resolvedEvent != null) {
                if (eventType.isEmpty()) eventType = resolvedEvent.type.trim()
                if (pushCameraUuid.isEmpty()) {
                    pushCameraUuid = (
                        resolvedEvent.data?.cameraUUID
                            ?: resolvedEvent.data?.cameraUuidLower
                            ?: resolvedEvent.cameraId?.toString()
                        ).orEmpty().trim()
                }
                if (cameraName.isEmpty()) {
                    cameraName = (resolvedEvent.cameraName ?: resolvedEvent.data?.cameraName).orEmpty().trim()
                }
                if (rawImageUrl.isNullOrBlank()) {
                    rawImageUrl = (
                        resolvedEvent.data?.fullImage
                            ?: resolvedEvent.data?.image
                            ?: resolvedEvent.data?.cropImage
                        )?.trim()
                }
                if (rawEventTime == null && resolvedEvent.time > 0.0) rawEventTime = resolvedEvent.time
            }
        }

        // Nếu gói FCM hoàn toàn không gửi kèm `type` và cũng không lấy được từ `eventId`, suy luận từ `title` / `body`
        if (eventType.isEmpty()) {
            eventType = AiTypeHelper.inferEventTypeFromText(rawTitle, rawBody)
        }

        if (eventType.isNotEmpty() && !AiTypeHelper.isAiTypeEnabled(eventType)) {
            if (BuildConfig.DEBUG) {
                Log.i(FcmTokenManager.LOG_TAG, "[BỎ QUA NOTIFY] Loại sự kiện '$eventType' không nằm trong quyền của user")
            }
            return
        }

        val localSettings = runCatching {
            val rawJson = prefs?.getString(AiTypeHelper.PREF_USER_NOTIFICATION_SETTINGS_JSON, null)
            if (!rawJson.isNullOrBlank()) {
                LoginAuthClient.gson.fromJson(rawJson, UserNotificationSettingsModel::class.java)
            } else {
                null
            }
        }.getOrNull()

        val (localTypeEnabled, localAlarmMode) = if (eventType.isNotEmpty()) {
            AiTypeHelper.resolveEventNotificationRule(eventType, localSettings?.overrides)
        } else {
            true to "push"
        }

        if (!localTypeEnabled) {
            if (BuildConfig.DEBUG) {
                Log.i(FcmTokenManager.LOG_TAG, "[BỎ QUA NOTIFY] Bài AI '$eventType' đang được tắt thông báo trong cấu hình")
            }
            return
        }

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

        val explicitMakeCall = (
            data["makeCall"] ?: data["make_call"] ?: data["isCall"] ?: data["is_call"] ?: data["call"]
            ).orEmpty().trim()
        val explicitAlarmMode = (
            data["alarmMode"] ?: data["alarm_mode"] ?: data["notifyType"] ?: data["notify_type"] ?: data["notificationType"] ?: data["notification_type"]
            ).orEmpty().trim()
        val isExplicitlyDisabledCall = explicitMakeCall.equals("false", ignoreCase = true) ||
            explicitMakeCall == "0" ||
            explicitAlarmMode.equals("push", ignoreCase = true)
        val isCall = explicitMakeCall.equals("true", ignoreCase = true) ||
            explicitMakeCall == "1" ||
            explicitAlarmMode.equals("call", ignoreCase = true) ||
            (!isExplicitlyDisabledCall && localAlarmMode.equals("call", ignoreCase = true))

        val title = rawTitle.ifBlank {
            if (eventType.isNotEmpty()) AiTypeHelper.getTypeName(eventType) else getString(R.string.notif_default_title)
        }
        val body = rawBody
        val imageUrl = resolveNotificationImageUrl(rawImageUrl)
        val effectiveEventTime = rawEventTime
            ?: if (remoteMessage.sentTime > 0L) remoteMessage.sentTime.toDouble() else System.currentTimeMillis().toDouble()

        if (BuildConfig.DEBUG) {
            Log.i(
                FcmTokenManager.LOG_TAG,
                "[XỬ LÝ NOTIFY] isCall=$isCall | eventId=$eventId | eventType='$eventType' | cameraName='$cameraName' | title='$title' | body='$body' | imageUrl=$imageUrl"
            )
        }

        if (isCall) {
            if (isStaleDelayedCall(remoteMessage.sentTime, rawEventTime)) {
                showMissedCallNotification(title, body, imageUrl, eventId)
            } else {
                handleIncomingCall(
                    title = title,
                    body = body,
                    url = imageUrl,
                    eId = eventId,
                    eventType = eventType,
                    cameraUuid = pushCameraUuid,
                    cameraName = cameraName,
                    eventTime = effectiveEventTime
                )
            }
        } else {
            showRegularNotification(title, body, imageUrl, eventId)
        }
    }

    /**
     * Truy vấn nhanh `EventData` theo `eventId` từ RAM cache, Disk cache hoặc API (giới hạn tối đa 2.5s)
     * khi bản tin FCM gửi thiếu trường `type` / `imageUrl` / `cameraUUID`.
     */
    private fun fetchEventQuickly(eventIdInt: Int): EventData? {
        EventCacheManager.getEventInMemory(eventIdInt)?.let { return it }
        return runCatching {
            runBlocking(Dispatchers.IO) {
                withTimeoutOrNull(QUICK_EVENT_FETCH_TIMEOUT_MS) {
                    EventCacheManager.findEventById(applicationContext, eventIdInt)?.first
                        ?: runCatching {
                            ApiProvider.eventApi.getDataList(lastId = eventIdInt + 1, count = 1)
                                .body()
                                ?.firstOrNull { it.id == eventIdInt }
                        }.getOrNull()
                }
            }
        }.getOrNull()
    }

    /**
     * Kiểm tra bản tin cuộc gọi có thực sự bị trễ quá lâu (vd. máy tắt mạng/chế độ máy bay rồi mới bật lại) hay không,
     * đồng thời tránh lỗi đồng hồ trên điện thoại bị chỉnh lệch vài phút khiến cuộc gọi mới bị biến thành cuộc gọi nhỡ.
     */
    private fun isStaleDelayedCall(sentTimeMs: Long, rawEventTime: Double?): Boolean {
        if (sentTimeMs <= 0L) return false
        val nowMs = System.currentTimeMillis()
        val phoneDeltaMs = nowMs - sentTimeMs
        if (phoneDeltaMs <= MISSED_CALL_THRESHOLD_MS) return false

        // Nếu có cả thời gian sự kiện từ server và sentTime từ Google FCM (cả hai đều là giờ chuẩn server),
        // kiểm tra độ trễ giữa thời điểm phát sinh sự kiện và thời điểm gửi FCM
        if (rawEventTime != null && rawEventTime > 0.0) {
            val eventTimeMs = if (rawEventTime < 1e11) (rawEventTime * 1000.0).toLong() else rawEventTime.toLong()
            val serverDeltaMs = kotlin.math.abs(sentTimeMs - eventTimeMs)
            // Nếu sự kiện vừa mới xảy ra trên server và FCM gửi đi tức thì, chỉ coi là cuộc gọi nhỡ nếu lệch > 30 phút
            if (serverDeltaMs < 60_000L && phoneDeltaMs < MAX_CLOCK_DRIFT_TOLERANCE_MS) {
                return false
            }
        }
        return true
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
    private fun eventDetailPendingIntent(
        eId: String,
        requestCode: Int,
        fromNotification: Boolean,
        notificationIdToCancel: Int = 0,
        ctx: Context = this
    ): PendingIntent {
        val intent = Intent(ctx, MainActivity::class.java).apply {
            putExtra(EXTRA_EVENT_ID, eId)
            if (fromNotification) putExtra(EXTRA_FROM_NOTIFICATION, true)
            if (notificationIdToCancel != 0) putExtra("NOTIFICATION_ID", notificationIdToCancel)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return runCatching {
            PendingIntent.getActivity(
                ctx,
                requestCode,
                intent,
                flags,
                splashIconOptionsBundle()
            )
        }.getOrElse {
            PendingIntent.getActivity(ctx, requestCode, intent, flags)
        }
    }

    private fun dismissCallPendingIntent(eId: String, notificationId: Int): PendingIntent {
        val intent = Intent(this, CallNotificationActionReceiver::class.java).apply {
            action = CallNotificationActionReceiver.ACTION_DISMISS_CALL_NOTIFICATION
            putExtra(CallNotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(CallNotificationActionReceiver.EXTRA_EVENT_ID, eId)
        }
        return PendingIntent.getBroadcast(
            this,
            notificationId + 3,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
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

    private fun showMissedCallNotification(
        title: String,
        body: String,
        url: String?,
        eId: String,
        ctx: Context = this
    ) {
        val notificationId = getSafeNotificationId(eId, 1)

        val builder = NotificationCompat.Builder(ctx, NotificationChannels.EVENT)
            .setSmallIcon(R.mipmap.app_icon_notify)
            .setContentTitle(ctx.getString(R.string.notif_missed_call_title, title))
            .setContentText(ctx.getString(R.string.notif_missed_call_text, body.take(MISSED_CALL_BODY_PREVIEW)))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVibrate(NotificationChannels.EVENT_VIBRATION_PATTERN)
            .setAutoCancel(true)
            .setContentIntent(
                eventDetailPendingIntent(
                    eId = eId,
                    requestCode = notificationId,
                    fromNotification = true,
                    notificationIdToCancel = notificationId,
                    ctx = ctx
                )
            )

        loadImageAndNotify(builder, url, notificationId, ctx)
    }

    private fun loadImageAndNotify(
        builder: NotificationCompat.Builder,
        url: String?,
        notificationId: Int,
        ctx: Context = this
    ) {
        val appCtx = ctx.applicationContext
        val manager = NotificationManagerCompat.from(appCtx)

        // Gửi thông báo ngay lập tức để tránh process bị kill
        notifySafely(manager, notificationId, builder)

        if (url.isNullOrEmpty()) return
        val app = appCtx as? TenliApp ?: return
        val loader = Coil.imageLoader(appCtx)
        // Giới hạn kích thước decode: ảnh thông báo không cần full-res → tiết kiệm RAM/CPU
        val request = ImageRequest.Builder(appCtx)
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
            } catch (e: CancellationException) {
                throw e
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

    private fun handleIncomingCall(
        title: String,
        body: String,
        url: String?,
        eId: String,
        eventType: String,
        cameraUuid: String,
        cameraName: String,
        eventTime: Double
    ) {
        wakeUpScreenForIncomingCall()

        val manager = NotificationManagerCompat.from(this)
        val notificationId = getSafeNotificationId(eId)

        val callIntent = Intent(this, CallComingActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            )
            putExtra("TITLE", title)
            putExtra("BODY", body)
            putExtra("URL", url)
            putExtra("EID", eId)
            putExtra("NOTIFICATION_ID", notificationId)
            putExtra("EVENT_TYPE", eventType)
            putExtra("CAMERA_UUID", cameraUuid)
            putExtra("CAMERA_NAME", cameraName)
            putExtra("EVENT_TIME", eventTime)
        }
        val callPendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            callIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val acceptPendingIntent = eventDetailPendingIntent(
            eId = eId,
            requestCode = notificationId + 2,
            fromNotification = true,
            notificationIdToCancel = notificationId
        )
        val dismissPendingIntent = dismissCallPendingIntent(eId, notificationId)

        val isEn = com.tenli.oneview.ui.utils.LocaleManager.getLocale(this).equals("en", ignoreCase = true)
        val declineLabel = if (isEn) "Decline" else "Từ chối"
        val acceptLabel = if (isEn) "View now" else "Xem ngay"

        val builder = NotificationCompat.Builder(this, NotificationChannels.EMERGENCY_CALL)
            .setSmallIcon(R.mipmap.app_icon_notify)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVibrate(NotificationChannels.CALL_VIBRATION_PATTERN)
            .setFullScreenIntent(callPendingIntent, true)
            .setContentIntent(callPendingIntent)
            .addAction(0, declineLabel, dismissPendingIntent)
            .addAction(0, acceptLabel, acceptPendingIntent)
            .setOngoing(true)
            .setAutoCancel(true)
            .setTimeoutAfter(CALL_NOTIFICATION_TIMEOUT_MS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        notifySafely(manager, notificationId, builder)

        val appCtx = applicationContext
        var startedDirectly = false
        try {
            startActivity(callIntent)
            startedDirectly = true
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.w(TAG, "Cannot start call activity directly: ${e.message}")
        }
        // Chỉ bồi thêm trên MainLooper nếu lần start trực tiếp bị từ chối khi tiến trình vừa cold-start
        if (!startedDirectly) {
            Handler(Looper.getMainLooper()).post {
                if (!AppState.isShowCallActivity) {
                    try {
                        appCtx.startActivity(callIntent)
                    } catch (e: Exception) {
                        if (BuildConfig.DEBUG) Log.w(TAG, "Cannot start call activity on MainLooper: ${e.message}")
                    }
                }
            }
        }

        // Bảo hiểm trường hợp hệ điều hành chỉ hiển thị Heads-Up Notification mà chặn mở CallComingActivity:
        // Sau 30s tự động huỷ thông báo đang reo và chuyển thành thông báo cuộc gọi nhỡ
        val app = appCtx as? TenliApp
        app?.appScope?.launch {
            delay(CALL_NOTIFICATION_TIMEOUT_MS + 500L)
            val nm = appCtx.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return@launch
            val isStillActive = runCatching {
                nm.activeNotifications.any { it.id == notificationId }
            }.getOrDefault(false)
            if (isStillActive && !AppState.isShowCallActivity) {
                runCatching { nm.cancel(notificationId) }
                showMissedCallNotification(title, body, url, eId, appCtx)
            }
        }
    }

    /**
     * Đánh thức màn hình trong 10 giây khi nhận cuộc gọi cảnh báo lúc máy đang tắt màn hình / app bị kill.
     */
    private fun wakeUpScreenForIncomingCall() {
        runCatching {
            val pm = getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return
            @Suppress("DEPRECATION")
            val wakeLock = pm.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
                    PowerManager.ACQUIRE_CAUSES_WAKEUP or
                    PowerManager.ON_AFTER_RELEASE,
                "tenli:incoming_call_wakelock"
            )
            wakeLock.setReferenceCounted(false)
            wakeLock.acquire(10_000L)
        }
    }

    private fun showRegularNotification(title: String, body: String, url: String?, eId: String) {
        val notificationId = getSafeNotificationId(eId)

        val builder = NotificationCompat.Builder(this, NotificationChannels.EVENT)
            .setSmallIcon(R.mipmap.app_icon_notify)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(NotificationChannels.EVENT_VIBRATION_PATTERN)
            .setAutoCancel(true)
            .setContentIntent(
                eventDetailPendingIntent(
                    eId = eId,
                    requestCode = notificationId,
                    fromNotification = true,
                    notificationIdToCancel = notificationId
                )
            )

        loadImageAndNotify(builder, url, notificationId)
    }

    private companion object {
        const val TAG = "EventMessagingService"
        const val EXTRA_EVENT_ID = "EVENT_ID"
        const val EXTRA_FROM_NOTIFICATION = "FROM_NOTIFICATION"
        const val MISSED_CALL_THRESHOLD_MS = 300_000L
        const val MAX_CLOCK_DRIFT_TOLERANCE_MS = 1_800_000L
        const val CALL_NOTIFICATION_TIMEOUT_MS = 30_000L
        const val QUICK_EVENT_FETCH_TIMEOUT_MS = 2_500L
        const val MISSED_CALL_BODY_PREVIEW = 20
        const val NOTIFICATION_IMAGE_MAX_PX = 1024
    }
}
