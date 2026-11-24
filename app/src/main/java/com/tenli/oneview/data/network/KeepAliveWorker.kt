package com.tenli.oneview.data.network

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tenli.oneview.TenliApp
import com.tenli.oneview.data.local.GlobalData
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.firebase.FcmTokenManager

class KeepAliveWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        UserSession.ensureRestored()
        val pushEnabled = runCatching {
            GlobalData.preferences.getBoolean(PREF_PUSH_NOTIFICATIONS_ENABLED, true)
        }.getOrDefault(true)

        if (UserSession.accessToken.isEmpty() || !pushEnabled) {
            TenliApp.syncKeepAliveWorker(applicationContext, enabled = false)
            return Result.success()
        }

        val token = FcmTokenManager.registerDevicePushToken(force = false)
        if (token != null) {
            return Result.success()
        }

        if (UserSession.accessToken.isEmpty()) {
            TenliApp.syncKeepAliveWorker(applicationContext, enabled = false)
            return Result.success()
        }

        return if (runAttemptCount < MAX_RETRY_ATTEMPTS) Result.retry() else Result.success()
    }

    private companion object {
        const val PREF_PUSH_NOTIFICATIONS_ENABLED = "push_notifications_enabled"
        const val MAX_RETRY_ATTEMPTS = 3
    }
}