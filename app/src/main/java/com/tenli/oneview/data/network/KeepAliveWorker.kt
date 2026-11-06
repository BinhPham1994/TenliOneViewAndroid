package com.tenli.oneview.data.network

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tenli.oneview.firebase.FcmTokenManager

class KeepAliveWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        FcmTokenManager.registerDevicePushToken(force = false)
        return Result.success()
    }
}