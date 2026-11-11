package com.tenli.oneview

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.tenli.oneview.data.local.EventCacheManager
import com.tenli.oneview.data.local.GlobalData
import com.tenli.oneview.data.local.HomeCacheManager
import com.tenli.oneview.data.local.MonitorCacheManager
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.data.network.KeepAliveWorker
import com.tenli.oneview.data.network.retrofit.LoginAuthClient
import com.tenli.oneview.di.AppContainer
import com.tenli.oneview.di.DefaultAppContainer
import com.tenli.oneview.firebase.FcmTokenManager
import com.tenli.oneview.service.NotificationChannels
import com.tenli.oneview.ui.component.VideoFrameCache
import com.tenli.oneview.ui.theme.ThemeManager
import com.tenli.oneview.ui.utils.AppKeys
import com.tenli.oneview.ui.utils.LocaleManager
import com.tenli.oneview.util.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class TenliApp : Application(), ImageLoaderFactory {

    lateinit var container: AppContainer

    /** Scope sống cùng tiến trình cho các tác vụ nền ngắn (preload cache, tải ảnh thông báo...) */
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            // Dùng chung connection pool/thread pool với API (xem LoginAuthClient.imageClient)
            .okHttpClient(LoginAuthClient.imageClient)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(IMAGE_MEMORY_CACHE_PERCENT)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(IMAGE_DISK_CACHE_BYTES) // disk cache cho ảnh sự kiện & snapshot camera
                    .build()
            }
            // Luôn ưu tiên đọc ảnh ngay từ DiskCache khi mở app (0ms network wait), không phụ thuộc Cache-Control của server
            .respectCacheHeaders(false)
            // Tắt crossfade mặc định & cho phép RGB_565 với ảnh không có kênh alpha để cuộn danh sách mượt 60/120fps, giảm 50% RAM bitmap
            .allowRgb565(true)
            .crossfade(false)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        NetworkMonitor.init(this)
        container = DefaultAppContainer(this)
        ThemeManager.init(this)

        val desiredLang = LocaleManager.getLocale(this)
        val currentTags = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        if (!currentTags.equals(desiredLang, ignoreCase = true)) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(desiredLang))
        }

        GlobalData.apply {
            preferences = getSharedPreferences(AppKeys.KEY_HOME_APP, MODE_PRIVATE)
            gson = LoginAuthClient.gson
        }
        // Khôi phục ngay phiên đăng nhập vào RAM (< 0.2ms) để SplashViewModel mở thẳng vào Main ở Frame 1
        runCatching { UserSession.ensureRestored(GlobalData.preferences) }

        // Chỉ pre-warm các cache từ ổ đĩa lên RAM khi đã có phiên đăng nhập hợp lệ
        if (UserSession.accessToken.isNotEmpty() && UserSession.userData != null) {
            appScope.launch { runCatching { HomeCacheManager.preloadDefaultCache(this@TenliApp) } }
            appScope.launch { runCatching { EventCacheManager.preloadDefaultCache(this@TenliApp) } }
            appScope.launch { runCatching { MonitorCacheManager.preloadDefaultCache(this@TenliApp) } }
        }

        FcmTokenManager.init(this)

        appScope.launch {
            runCatching { NotificationChannels.createAll(this@TenliApp) }
            setupBackgroundWorkers()
        }

        registerActivityLifecycleCallbacks(ForegroundTracker())
    }

    /**
     * Theo dõi app foreground/background (đếm số Activity đang started) để WebSocket chỉ chạy khi người dùng
     * đang mở app → giảm đáng kể pin/nhiệt khi app ở nền.
     */
    private inner class ForegroundTracker : ActivityLifecycleCallbacks {
        private var startedCount = 0

        override fun onActivityStarted(activity: Activity) {
            if (startedCount++ == 0) {
                container.webSocketManager.onAppForegroundChanged(true)
            }
        }

        override fun onActivityStopped(activity: Activity) {
            startedCount = (startedCount - 1).coerceAtLeast(0)
            if (startedCount == 0 && !activity.isChangingConfigurations) {
                container.webSocketManager.onAppForegroundChanged(false)
            }
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
        override fun onActivityResumed(activity: Activity) = Unit
        override fun onActivityPaused(activity: Activity) = Unit
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
        override fun onActivityDestroyed(activity: Activity) = Unit
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        VideoFrameCache.trimMemory(level)
    }

    private fun setupBackgroundWorkers() {
        runCatching {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED) // Chỉ chạy khi có mạng
                .setRequiresBatteryNotLow(true) // Không đánh thức máy khi pin yếu
                .build()
            val workRequest = PeriodicWorkRequestBuilder<KeepAliveWorker>(
                KEEP_ALIVE_WORK_INTERVAL_HOURS,
                TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                KEEP_ALIVE_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }
    }

    private companion object {
        const val IMAGE_MEMORY_CACHE_PERCENT = 0.25
        const val IMAGE_DISK_CACHE_BYTES = 150L * 1024 * 1024
        const val KEEP_ALIVE_WORK_NAME = "ping_firebase"
        const val KEEP_ALIVE_WORK_INTERVAL_HOURS = 6L
    }
}
