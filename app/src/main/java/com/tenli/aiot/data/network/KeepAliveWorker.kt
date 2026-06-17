package com.tenli.aiot.data.network

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.messaging.FirebaseMessaging

class KeepAliveWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        FirebaseMessaging.getInstance().token.addOnCompleteListener {}
        return Result.success()
    }
}