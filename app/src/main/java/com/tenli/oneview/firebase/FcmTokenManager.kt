package com.tenli.oneview.firebase

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.content.edit
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.tenli.oneview.data.local.GlobalData
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.data.network.ApiProvider
import com.tenli.oneview.model.network.NotificationTokenRegisterModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.lang.reflect.Method
import kotlin.coroutines.resume

/**
 * Quản lý vòng đời đăng ký / huỷ đăng ký FCM Push Token với backend Tenli OneView,
 * đồng bộ 100% với `src/services/firebaseMessaging.ts` trên `tenli-oneview` Web:
 * - Tự động kiểm tra `GET /ts/api/v1/config/public?client_id=oneview-web` của server đang đăng nhập
 *   và lưu cache `serverSenderId` / `serverProjectId` theo từng domain để khi chạy ngầm (`KeepAliveWorker`)
 *   nếu mạng chập chờn không bao giờ bị fallback nhầm về `defaultSenderId` (`kk`) ghi đè mất token `kk-dev`.
 * - Nếu `mobilesdk_app_id` trong `google-services.json` thay đổi giữa các bản build, tự động làm mới
 *   token cục bộ để tránh gửi token cũ không còn hợp lệ.
 * - Đăng ký: `POST /ts/api/v1/notification-tokens` (`type = "firebase"`, `status = 1`, `client_id = "oneview-web"`)
 * - Huỷ đăng ký: `DELETE /ts/api/v1/notification-tokens/{token}` (gọi trước khi xoá accessToken khi logout / tắt thông báo)
 * - Log chi tiết với tag `FCM_DEBUG` để dễ dàng lọc trong Logcat (`adb logcat -s FCM_DEBUG`).
 */
object FcmTokenManager {

    const val LOG_TAG = "FCM_DEBUG"

    private const val PREF_REGISTERED_TOKEN = "fcm_registered_token"
    private const val PREF_REGISTERED_USER = "fcm_registered_user"
    private const val PREF_REGISTERED_DOMAIN = "fcm_registered_domain"
    private const val PREF_REGISTERED_SENDER = "fcm_registered_sender"
    private const val PREF_REGISTERED_TIME = "fcm_registered_time"
    private const val PREF_LATEST_DEVICE_TOKEN = "fcm_latest_device_token"
    private const val PREF_CACHED_SERVER_DOMAIN = "fcm_cached_server_domain"
    private const val PREF_CACHED_SERVER_SENDER_ID = "fcm_cached_server_sender_id"
    private const val PREF_CACHED_SERVER_PROJECT_ID = "fcm_cached_server_project_id"
    private const val PREF_LAST_GOOGLE_APP_ID = "fcm_last_google_app_id"
    private const val PREF_SERVER_FIREBASE_CONFIG = "fcm_server_firebase_config_json"
    private const val PREF_PUSH_NOTIFICATIONS_ENABLED = "push_notifications_enabled"

    /** Chu kỳ tối thiểu gửi lại cùng 1 token cho cùng 1 user (2 giờ – giống `tenli-oneview`) */
    private const val REGISTER_DEDUP_WINDOW_MS = 2 * 60 * 60 * 1000L
    private const val TOKEN_FETCH_TIMEOUT_MS = 10_000L
    private const val FID_FETCH_TIMEOUT_MS = 4_000L
    private const val UNREGISTER_TIMEOUT_MS = 4_000L

    @Volatile
    private var appContext: Context? = null

    @Volatile
    private var cachedGmsRpcInstance: Any? = null

    @Volatile
    private var cachedGmsRpcSendMethod: Method? = null

    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val registerMutex = Mutex()

    /**
     * Khởi tạo trong [com.tenli.oneview.TenliApp.onCreate].
     */
    fun init(context: Context) {
        appContext = context.applicationContext
        runCatching {
            GlobalData.preferences.edit { remove(PREF_SERVER_FIREBASE_CONFIG) }
        }
    }

    /**
     * Gọi bất đồng bộ (fire-and-forget) để đăng ký FCM token mà không chặn luồng UI/ViewModel.
     */
    fun registerDevicePushTokenAsync(force: Boolean = false) {
        ioScope.launch {
            registerDevicePushToken(force = force)
        }
    }

    /**
     * Được gọi từ [EventMessagingService.onNewToken] khi Firebase cấp mới hoặc xoay vòng token mặc định.
     */
    fun onNewToken(newToken: String) {
        val trimmed = newToken.trim()
        Log.i(LOG_TAG, "[onNewToken] Firebase cấp FCM token mặc định mới: $trimmed")
        if (trimmed.isEmpty()) return
        ioScope.launch {
            UserSession.ensureRestored()
            if (UserSession.accessToken.isNotEmpty() && isPushEnabledInPrefs()) {
                registerDevicePushToken(force = true, overrideToken = trimmed)
            } else {
                Log.w(
                    LOG_TAG,
                    "[onNewToken] Chưa đăng ký lên server vì hasAccessToken=${UserSession.accessToken.isNotEmpty()}, pushEnabled=${isPushEnabledInPrefs()}"
                )
            }
        }
    }

    /**
     * Lấy FCM token của thiết bị và gửi lên backend `POST /ts/api/v1/notification-tokens`.
     * @return FCM token nếu lấy & đăng ký thành công (hoặc đã đăng ký còn hạn), `null` nếu thất bại.
     */
    suspend fun registerDevicePushToken(
        force: Boolean = false,
        overrideToken: String? = null
    ): String? = withContext(Dispatchers.IO) {
        registerMutex.withLock {
            try {
                UserSession.ensureRestored()
                val currentUserKey = currentUserIdentifier()
                val currentDomain = UserSession.domain.trim()

                Log.i(
                    LOG_TAG,
                    "=== [BẮT ĐẦU ĐĂNG KÝ FCM TOKEN] force=$force | domain=$currentDomain | user=$currentUserKey | hasAccessToken=${UserSession.accessToken.isNotEmpty()} ==="
                )

                if (UserSession.accessToken.isEmpty() || currentDomain.isEmpty()) {
                    Log.w(LOG_TAG, "[BỎ QUA] Chưa đăng nhập (accessToken hoặc domain rỗng), không thể gọi API đăng ký FCM token")
                    return@withLock null
                }
                if (!isPushEnabledInPrefs()) {
                    Log.w(LOG_TAG, "[BỎ QUA] Người dùng đang tắt nhận thông báo trong cài đặt")
                    return@withLock null
                }

                val prefs = runCatching { GlobalData.preferences }.getOrNull()
                val appOpts = runCatching { FirebaseApp.getInstance().options }.getOrNull()
                val defaultProjectId = appOpts?.projectId.orEmpty()
                val defaultSenderId = appOpts?.gcmSenderId.orEmpty()
                val defaultAppId = appOpts?.applicationId.orEmpty()

                // Nếu google-services.json đổi mobilesdk_app_id (vd. chuyển từ kk sang kk-dev),
                // xoá token mặc định cũ để Firebase cấp lại token mới khớp với mobilesdk_app_id hiện tại.
                if (prefs != null && defaultAppId.isNotEmpty()) {
                    val lastAppId = prefs.getString(PREF_LAST_GOOGLE_APP_ID, null)
                    if (lastAppId != defaultAppId) {
                        Log.i(
                            LOG_TAG,
                            "🔄 [GOOGLE APP ID THAY ĐỔI] $lastAppId -> $defaultAppId, xoá FCM token cũ và cấp mới theo project '$defaultProjectId'..."
                        )
                        awaitDeleteDefaultFcmToken()
                        prefs.edit {
                            putString(PREF_LAST_GOOGLE_APP_ID, defaultAppId)
                            remove(PREF_LATEST_DEVICE_TOKEN)
                            remove(PREF_REGISTERED_TOKEN)
                            remove(PREF_REGISTERED_TIME)
                        }
                    }
                }

                var serverProjectId: String? = null
                var serverSenderId: String? = null
                var publicConfigFetched = false

                // 1. Lấy cấu hình Firebase từ server (/ts/api/v1/config/public?client_id=oneview-web) giống tenli-oneview Web
                runCatching {
                    val pubResp = ApiProvider.bsApi.getPublicConfig()
                    if (pubResp.isSuccessful) {
                        publicConfigFetched = true
                        val srvFb = pubResp.body()?.firebase
                        serverProjectId = srvFb?.projectId?.trim()?.takeIf { it.isNotEmpty() }
                        serverSenderId = srvFb?.messagingSenderId?.trim()?.takeIf { it.isNotEmpty() }
                        Log.i(
                            LOG_TAG,
                            "[Firebase Config] App google-services.json: projectId=$defaultProjectId (senderId=$defaultSenderId) | Server PublicConfig: projectId=$serverProjectId (senderId=$serverSenderId)"
                        )
                        if (prefs != null) {
                            prefs.edit {
                                putString(PREF_CACHED_SERVER_DOMAIN, currentDomain)
                                if (!serverSenderId.isNullOrBlank()) {
                                    putString(PREF_CACHED_SERVER_SENDER_ID, serverSenderId)
                                    putString(PREF_CACHED_SERVER_PROJECT_ID, serverProjectId ?: "")
                                } else {
                                    remove(PREF_CACHED_SERVER_SENDER_ID)
                                    remove(PREF_CACHED_SERVER_PROJECT_ID)
                                }
                            }
                        }
                    } else {
                        Log.w(LOG_TAG, "[Server PublicConfig] GET /ts/api/v1/config/public trả về HTTP ${pubResp.code()}")
                    }
                }.onFailure { e ->
                    Log.w(LOG_TAG, "[Server PublicConfig] Lỗi khi kiểm tra /ts/api/v1/config/public: ${e.message}")
                }

                // Nếu gọi /ts/api/v1/config/public thất bại (vd. mất mạng thoáng qua khi chạy ngầm KeepAliveWorker),
                // khôi phục senderId đã biết của domain hiện tại từ cache để KHÔNG BAO GIỜ lấy nhầm token của
                // project mặc định (kk) ghi đè lên token của server (kk-dev).
                if (!publicConfigFetched && prefs != null) {
                    val cachedDomain = prefs.getString(PREF_CACHED_SERVER_DOMAIN, null)
                    val regDomain = prefs.getString(PREF_REGISTERED_DOMAIN, null)
                    when {
                        cachedDomain == currentDomain -> {
                            serverSenderId = prefs.getString(PREF_CACHED_SERVER_SENDER_ID, null)?.takeIf { it.isNotBlank() }
                            serverProjectId = prefs.getString(PREF_CACHED_SERVER_PROJECT_ID, null)?.takeIf { it.isNotBlank() }
                            Log.i(
                                LOG_TAG,
                                "[Firebase Config Cache] Dùng lại cấu hình đã lưu cho domain=$currentDomain: projectId=$serverProjectId, senderId=$serverSenderId"
                            )
                        }
                        regDomain == currentDomain -> {
                            serverSenderId = prefs.getString(PREF_REGISTERED_SENDER, null)?.takeIf { it.isNotBlank() }
                            Log.i(
                                LOG_TAG,
                                "[Firebase Config Cache] Dùng lại senderId đã đăng ký cho domain=$currentDomain: senderId=$serverSenderId"
                            )
                        }
                        !force -> {
                            Log.w(
                                LOG_TAG,
                                "[BỎ QUA] Chưa xác định được Firebase Config của server '$currentDomain' khi chạy ngầm, đợi lần kiểm tra sau"
                            )
                            return@withLock null
                        }
                    }
                }

                val targetSenderId = serverSenderId ?: defaultSenderId
                val targetProjectId = serverProjectId ?: defaultProjectId
                val useCustomSender = !serverSenderId.isNullOrBlank() && serverSenderId != defaultSenderId

                // 2. Lấy FCM token:
                // - Nếu server dùng senderId khác google-services.json (vd. server dùng kk-dev-6fde6 / 106599562247):
                //   xin trực tiếp token cho senderId của server qua Google Play Services Rpc.
                // - Nếu cùng senderId: dùng FirebaseMessaging.getInstance().token mặc định.
                val token = if (useCustomSender) {
                    val ctx = appContext
                    if (ctx != null) {
                        Log.i(
                            LOG_TAG,
                            "🔄 [ĐỒNG BỘ SENDER ID THEO SERVER] Xin FCM token cho project='$targetProjectId' (senderId=$targetSenderId) qua Google Play Services..."
                        )
                        awaitTokenViaGmsRpc(ctx, targetSenderId)
                    } else null
                } else {
                    overrideToken?.trim()?.takeIf { it.isNotEmpty() }
                        ?: awaitDefaultFcmToken()
                        ?: prefs?.getString(PREF_LATEST_DEVICE_TOKEN, null)
                }

                if (token.isNullOrBlank()) {
                    Log.e(
                        LOG_TAG,
                        "❌ [LỖI] Không lấy được FCM token cho project '$targetProjectId' (senderId=$targetSenderId)!"
                    )
                    return@withLock null
                }

                Log.i(
                    LOG_TAG,
                    "[FCM Token thiết bị] projectId=$targetProjectId | senderId=$targetSenderId | token=$token"
                )

                prefs?.edit { putString(PREF_LATEST_DEVICE_TOKEN, token) }

                val now = System.currentTimeMillis()
                if (!force && prefs != null) {
                    val lastToken = prefs.getString(PREF_REGISTERED_TOKEN, null)
                    val lastUser = prefs.getString(PREF_REGISTERED_USER, null)
                    val lastDomain = prefs.getString(PREF_REGISTERED_DOMAIN, null)
                    val lastSender = prefs.getString(PREF_REGISTERED_SENDER, null)
                    val lastTime = prefs.getLong(PREF_REGISTERED_TIME, 0L)

                    if (lastToken == token &&
                        lastUser == currentUserKey &&
                        lastDomain == currentDomain &&
                        lastSender == targetSenderId &&
                        now - lastTime < REGISTER_DEDUP_WINDOW_MS
                    ) {
                        val remainMin = (REGISTER_DEDUP_WINDOW_MS - (now - lastTime)) / 60_000L
                        Log.i(
                            LOG_TAG,
                            "✅ [ĐÃ ĐĂNG KÝ TRƯỚC ĐÓ] Token đã được đăng ký cho user='$currentUserKey' tại domain='$currentDomain' (senderId=$targetSenderId, còn hiệu lực cache ${remainMin} phút)"
                        )
                        return@withLock token
                    }
                }

                val payload = NotificationTokenRegisterModel(
                    token = token,
                    type = "firebase",
                    status = 1,
                    clientId = "oneview-web"
                )
                Log.i(
                    LOG_TAG,
                    "[GỬI API] POST $currentDomain/ts/api/v1/notification-tokens | payload={type=${payload.type}, status=${payload.status}, client_id=${payload.clientId}, senderId=$targetSenderId, token=${token.take(24)}...}"
                )

                val response = ApiProvider.bsApi.registerNotificationToken(payload)

                if (response.isSuccessful) {
                    prefs?.edit {
                        putString(PREF_REGISTERED_TOKEN, token)
                        putString(PREF_REGISTERED_USER, currentUserKey)
                        putString(PREF_REGISTERED_DOMAIN, currentDomain)
                        putString(PREF_REGISTERED_SENDER, targetSenderId)
                        putLong(PREF_REGISTERED_TIME, now)
                    }
                    Log.i(
                        LOG_TAG,
                        "✅ [ĐĂNG KÝ THÀNH CÔNG] POST /ts/api/v1/notification-tokens -> HTTP ${response.code()} | projectId=$targetProjectId (senderId=$targetSenderId) | response=${response.body()}"
                    )
                    token
                } else {
                    val errBody = runCatching { response.errorBody()?.string() }.getOrNull()
                    Log.e(
                        LOG_TAG,
                        "❌ [ĐĂNG KÝ THẤT BẠI] POST /ts/api/v1/notification-tokens -> HTTP ${response.code()} | errorBody=$errBody"
                    )
                    null
                }
            } catch (t: Throwable) {
                Log.e(LOG_TAG, "❌ [EXCEPTION KHI ĐĂNG KÝ FCM TOKEN] ${t.message}", t)
                null
            }
        }
    }

    /**
     * Huỷ đăng ký FCM token trên backend `DELETE /ts/api/v1/notification-tokens/{token}`
     * và xoá token cục bộ (`FirebaseMessaging.deleteToken()`) giống `unregisterDevicePushToken` trong `firebaseMessaging.ts`
     * để khi đăng nhập tài khoản mới trên cùng thiết bị sẽ được cấp FCM token mới hoàn toàn.
     * Lưu ý: Phải gọi **trước khi** xoá `UserSession.accessToken` khi đăng xuất để request có `Bearer` token hợp lệ.
     */
    suspend fun unregisterDevicePushToken(deleteLocalToken: Boolean = true) = withContext(Dispatchers.IO) {
        registerMutex.withLock {
            try {
                UserSession.ensureRestored()
                val prefs = runCatching { GlobalData.preferences }.getOrNull()
                val targetToken = prefs?.getString(PREF_REGISTERED_TOKEN, null)?.takeIf { it.isNotBlank() }
                    ?: prefs?.getString(PREF_LATEST_DEVICE_TOKEN, null)?.takeIf { it.isNotBlank() }
                    ?: awaitDefaultFcmToken()

                // Xoá cache đăng ký ngay lập tức để lần đăng nhập tiếp theo luôn đăng ký mới
                prefs?.edit {
                    remove(PREF_REGISTERED_TOKEN)
                    remove(PREF_REGISTERED_USER)
                    remove(PREF_REGISTERED_DOMAIN)
                    remove(PREF_REGISTERED_SENDER)
                    remove(PREF_REGISTERED_TIME)
                    if (deleteLocalToken) {
                        remove(PREF_LATEST_DEVICE_TOKEN)
                    }
                }

                if (!targetToken.isNullOrBlank() && UserSession.accessToken.isNotEmpty()) {
                    Log.i(
                        LOG_TAG,
                        "[HUỶ ĐĂNG KÝ FCM] DELETE /ts/api/v1/notification-tokens/${targetToken.take(24)}... | user=${currentUserIdentifier()}"
                    )
                    withTimeoutOrNull(UNREGISTER_TIMEOUT_MS) {
                        runCatching {
                            ApiProvider.bsApi.unregisterNotificationToken(targetToken)
                        }.onSuccess { resp ->
                            val errBody = if (!resp.isSuccessful) {
                                runCatching { resp.errorBody()?.string() }.getOrNull()
                            } else null
                            Log.i(
                                LOG_TAG,
                                "[HUỶ ĐĂNG KÝ FCM] Kết quả HTTP ${resp.code()}${if (errBody != null) " | errorBody=$errBody" else ""}"
                            )
                        }.onFailure { err ->
                            Log.w(LOG_TAG, "[HUỶ ĐĂNG KÝ FCM] Lỗi: ${err.message}")
                        }
                    }
                } else {
                    Log.w(
                        LOG_TAG,
                        "[HUỶ ĐĂNG KÝ FCM] Bỏ qua gọi DELETE vì hasToken=${!targetToken.isNullOrBlank()}, hasAccessToken=${UserSession.accessToken.isNotEmpty()}"
                    )
                }

                if (deleteLocalToken) {
                    Log.i(LOG_TAG, "[HUỶ ĐĂNG KÝ FCM] Xoá FCM token cục bộ (FirebaseMessaging.deleteToken) để cấp mới cho phiên sau...")
                    awaitDeleteDefaultFcmToken()
                }
            } catch (t: Throwable) {
                Log.w(LOG_TAG, "[HUỶ ĐĂNG KÝ FCM] Exception: ${t.message}", t)
            }
        }
    }

    private fun currentUserIdentifier(): String {
        val user = UserSession.userData
        return (user?.id?.toString()?.takeIf { it.isNotBlank() }
            ?: user?.name?.takeIf { it.isNotBlank() }
            ?: UserSession.savedUsername).trim()
    }

    private fun isPushEnabledInPrefs(): Boolean =
        runCatching { GlobalData.preferences.getBoolean(PREF_PUSH_NOTIFICATIONS_ENABLED, true) }
            .getOrDefault(true)

    @Synchronized
    private fun getOrCreateGmsRpc(context: Context): Pair<Any, Method>? {
        val existingInstance = cachedGmsRpcInstance
        val existingMethod = cachedGmsRpcSendMethod
        if (existingInstance != null && existingMethod != null) {
            return existingInstance to existingMethod
        }
        return runCatching {
            val rpcClass = Class.forName("com.google.android.gms.cloudmessaging.Rpc")
            val instance = rpcClass.getConstructor(Context::class.java).newInstance(context.applicationContext)
            val method = rpcClass.getMethod("send", Bundle::class.java)
            cachedGmsRpcInstance = instance
            cachedGmsRpcSendMethod = method
            instance to method
        }.onFailure { e ->
            Log.e(LOG_TAG, "❌ Không thể khởi tạo com.google.android.gms.cloudmessaging.Rpc: ${e.message}", e)
        }.getOrNull()
    }

    /**
     * Xin FCM registration token trực tiếp cho `senderId` của server thông qua `com.google.android.gms.cloudmessaging.Rpc`
     * (không cần xoá/khởi tạo lại `FirebaseApp` và không bị chặn bởi kiểm tra `:android:` của `FirebaseInstallations`).
     */
    private suspend fun awaitTokenViaGmsRpc(context: Context, senderId: String): String? {
        val fid = awaitFirebaseInstallationId()
        return withTimeoutOrNull(TOKEN_FETCH_TIMEOUT_MS) {
            suspendCancellableCoroutine { cont ->
                try {
                    val (rpcInstance, sendMethod) = getOrCreateGmsRpc(context) ?: run {
                        if (cont.isActive) cont.resume(null)
                        return@suspendCancellableCoroutine
                    }
                    val pkgInfo = runCatching {
                        context.packageManager.getPackageInfo(context.packageName, 0)
                    }.getOrNull()
                    val bundle = Bundle().apply {
                        putString("scope", "*")
                        putString("sender", senderId)
                        putString("subtype", senderId)
                        putString("osv", Build.VERSION.SDK_INT.toString())
                        putString("cliv", "fcm-25.0.1")
                        if (!fid.isNullOrBlank()) {
                            putString("appid", fid)
                        }
                        if (pkgInfo != null) {
                            @Suppress("DEPRECATION")
                            putString("app_ver", pkgInfo.versionCode.toString())
                            pkgInfo.versionName?.let { putString("app_ver_name", it) }
                        }
                    }
                    @Suppress("UNCHECKED_CAST")
                    val task = sendMethod.invoke(rpcInstance, bundle) as Task<Bundle>
                    task.addOnCompleteListener { resultTask ->
                        if (!cont.isActive) return@addOnCompleteListener
                        if (resultTask.isSuccessful) {
                            val resBundle = resultTask.result
                            val rawRegId = resBundle?.getString("registration_id")
                                ?: resBundle?.getString("unregistered")
                            val cleanToken = rawRegId
                                ?.removePrefix("|ID|")
                                ?.substringAfter(':')
                                ?.trim()
                                ?.takeIf { it.isNotEmpty() }
                                ?: rawRegId?.trim()?.takeIf { it.isNotEmpty() }
                            if (!cleanToken.isNullOrBlank()) {
                                cont.resume(cleanToken)
                            } else {
                                val err = resBundle?.getString("error")
                                Log.e(
                                    LOG_TAG,
                                    "❌ GmsRpc không trả về token cho senderId=$senderId: error=$err, bundle=$resBundle"
                                )
                                cont.resume(null)
                            }
                        } else {
                            Log.e(
                                LOG_TAG,
                                "❌ GmsRpc thất bại cho senderId=$senderId: ${resultTask.exception?.message}",
                                resultTask.exception
                            )
                            cont.resume(null)
                        }
                    }
                } catch (t: Throwable) {
                    Log.e(LOG_TAG, "❌ Lỗi khi gọi GmsRpc cho senderId=$senderId: ${t.message}", t)
                    if (cont.isActive) cont.resume(null)
                }
            }
        }
    }

    private suspend fun awaitFirebaseInstallationId(): String? = withTimeoutOrNull(FID_FETCH_TIMEOUT_MS) {
        suspendCancellableCoroutine { cont ->
            try {
                val fiClass = Class.forName("com.google.firebase.installations.FirebaseInstallations")
                val fiInstance = fiClass.getMethod("getInstance").invoke(null)
                @Suppress("UNCHECKED_CAST")
                val idTask = fiClass.getMethod("getId").invoke(fiInstance) as Task<String>
                idTask.addOnCompleteListener { task ->
                    if (!cont.isActive) return@addOnCompleteListener
                    cont.resume(if (task.isSuccessful) task.result?.trim()?.takeIf { it.isNotEmpty() } else null)
                }
            } catch (_: Throwable) {
                if (cont.isActive) cont.resume(null)
            }
        }
    }

    private suspend fun awaitDefaultFcmToken(): String? = withTimeoutOrNull(TOKEN_FETCH_TIMEOUT_MS) {
        suspendCancellableCoroutine { cont ->
            try {
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (!cont.isActive) return@addOnCompleteListener
                    if (task.isSuccessful) {
                        cont.resume(task.result?.trim()?.takeIf { it.isNotEmpty() })
                    } else {
                        Log.e(LOG_TAG, "❌ FirebaseMessaging.getInstance().token thất bại: ${task.exception?.message}", task.exception)
                        cont.resume(null)
                    }
                }
            } catch (t: Throwable) {
                Log.e(LOG_TAG, "❌ Exception khi gọi FirebaseMessaging.getInstance().token: ${t.message}", t)
                if (cont.isActive) cont.resume(null)
            }
        }
    }

    private suspend fun awaitDeleteDefaultFcmToken() {
        withTimeoutOrNull(UNREGISTER_TIMEOUT_MS) {
            suspendCancellableCoroutine { cont ->
                try {
                    FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener {
                        if (cont.isActive) cont.resume(Unit)
                    }
                } catch (_: Throwable) {
                    if (cont.isActive) cont.resume(Unit)
                }
            }
        }
    }
}
