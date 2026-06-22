package com.tenli.oneview

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.gson.GsonBuilder
import com.tenli.oneview.data.local.GlobalData
import com.tenli.oneview.data.network.KeepAliveWorker
import com.tenli.oneview.di.AppContainer
import com.tenli.oneview.di.DefaultAppContainer
import com.tenli.oneview.service.KeepAliveService
import com.tenli.oneview.ui.utils.AppKeys
import com.tenli.oneview.ui.utils.LocaleManager
import java.util.concurrent.TimeUnit
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache

class TenliApp : Application(), ImageLoaderFactory {

    lateinit var container: AppContainer

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(this.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .crossfade(true)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
        val currentLang = LocaleManager.getLocale(this)
        val appLocale = LocaleListCompat.forLanguageTags(currentLang)
        AppCompatDelegate.setApplicationLocales(appLocale)

        setupNotificationChannels()
        GlobalData.apply {
            preferences = getSharedPreferences(AppKeys.KEY_HOME_APP, MODE_PRIVATE)
            gson = GsonBuilder().serializeNulls().create()
        }
        startKeepAliveService(this)
        setupBackgroundWorkers()
    }

    private fun setupBackgroundWorkers() {
        try {
            val constraints = androidx.work.Constraints.Builder()
                .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED) // Chỉ chạy khi có mạng
                .build()
            val workRequest = PeriodicWorkRequestBuilder<KeepAliveWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "ping_firebase",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        } catch (_: Exception) {
        }
    }

    private fun setupNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java) ?: return
        val eventChannel = NotificationChannel(
            "ai_event_channel",
            "Cảnh báo AI",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Thông báo khi phát hiện người, cháy, hoặc khuôn mặt"
            enableVibration(true)
            setShowBadge(true)
        }
        val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .build()
        val callChannel = NotificationChannel(
            "emergency_call_channel",
            "Cuộc gọi cảnh báo",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Kênh dành riêng cho cuộc gọi khẩn cấp từ AI"
            enableLights(true)
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setBypassDnd(true)
            setSound(ringtoneUri, audioAttributes)
        }
        manager.createNotificationChannels(listOf(eventChannel, callChannel))
    }

    private fun startKeepAliveService(context: Context) {
        try {
            val intent = Intent(context, KeepAliveService::class.java)
            context.startForegroundService(intent)
        } catch (_: Exception) {
        }
    }
}