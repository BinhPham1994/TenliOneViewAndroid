package com.tenli.oneview

import android.app.Activity
import android.app.Application
import android.content.Intent
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
import com.google.gson.GsonBuilder
import com.tenli.oneview.data.local.EventCacheManager
import com.tenli.oneview.data.local.GlobalData
import com.tenli.oneview.data.local.HomeCacheManager
import com.tenli.oneview.data.local.MonitorCacheManager
import com.tenli.oneview.data.network.KeepAliveWorker
import com.tenli.oneview.data.network.retrofit.LoginAuthClient
import com.tenli.oneview.di.AppContainer
import com.tenli.oneview.di.DefaultAppContainer
import com.tenli.oneview.service.KeepAliveService
import com.tenli.oneview.service.NotificationChannels
import com.tenli.oneview.ui.theme.ThemeManager
import com.tenli.oneview.ui.utils.AppKeys
import com.tenli.oneview.ui.utils.LocaleManager
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
            .crossfade(true)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
        ThemeManager.init(this)
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(LocaleManager.getLocale(this)))

        GlobalData.apply {
            preferences = getSharedPreferences(AppKeys.KEY_HOME_APP, MODE_PRIVATE)
            gson = GsonBuilder().serializeNulls().create()
        }

        // Tạo channel đồng bộ (rất nhẹ) để FCM/Service luôn có channel ngay khi cần
        NotificationChannels.createAll(this)

        // Pre-warm dữ liệu cache từ ổ đĩa lên RAM ngay khi khởi tạo tiến trình ứng dụng
        // để cả 3 màn hình chính (Trang chủ, Sự kiện, Trực tiếp) và Chi tiết sự kiện có dữ liệu 0ms ở frame đầu tiên
        appScope.launch {
            runCatching { HomeCacheManager.preloadDefaultCache(this@TenliApp) }
            runCatching { EventCacheManager.preloadDefaultCache(this@TenliApp) }
            runCatching { MonitorCacheManager.preloadDefaultCache(this@TenliApp) }
        }

        registerActivityLifecycleCallbacks(ForegroundTracker())
        startKeepAliveService()
        setupBackgroundWorkers()
    }

    /**
     * Theo dõi app foreground/background (đếm số Activity đang started) để WebSocket chỉ chạy khi người dùng
     * đang mở app → giảm đáng kể pin/nhiệt khi app ở nền.
     */
    private inner class ForegroundTracker : ActivityLifecycleCallbacks {
        private var startedCount = 0

        override fun onActivityStarted(activity: Activity) {
            if (startedCount++ == 0) container.webSocketManager.onAppForegroundChanged(true)
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

    private fun setupBackgroundWorkers() {
        runCatching {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED) // Chỉ chạy khi có mạng
                .build()
            val workRequest = PeriodicWorkRequestBuilder<KeepAliveWorker>(KEEP_ALIVE_WORK_INTERVAL_MIN, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                KEEP_ALIVE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }

    private fun startKeepAliveService() {
        runCatching { startForegroundService(Intent(this, KeepAliveService::class.java)) }
    }

    private companion object {
        const val IMAGE_MEMORY_CACHE_PERCENT = 0.25
        const val IMAGE_DISK_CACHE_BYTES = 150L * 1024 * 1024
        const val KEEP_ALIVE_WORK_NAME = "ping_firebase"
        const val KEEP_ALIVE_WORK_INTERVAL_MIN = 15L
    }
}
