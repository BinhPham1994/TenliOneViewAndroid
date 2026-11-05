package com.tenli.oneview.data.network

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.tenli.oneview.firebase.FcmTokenManager

class KeepAliveWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        FcmTokenManager.registerDevicePushTokenAsync(force = false)
        return Result.success()
    }
}