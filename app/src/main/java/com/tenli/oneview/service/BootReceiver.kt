package com.tenli.oneview.service // Thay đổi theo package thực tế của bạn

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {

            val i = Intent(context, KeepAliveService::class.java)
            context.startForegroundService(i)
        }
    }
}