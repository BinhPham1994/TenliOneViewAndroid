package com.tenli.aiot.ui.utils

import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

object PermissionUtils {

    /**
     * Kiểm tra App đã được đưa vào danh sách "Không tối ưu hóa pin" chưa.
     * Trả về true = Đã sẵn sàng chạy ngầm.
     */
    fun isIgnoringBattery(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    /**
     * Kiểm tra quyền Thông báo hệ thống.
     */
    fun isNotificationEnabled(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    /**
     * Tổng hợp tất cả các quyền để biết App có thể nhận Call 100% hay chưa.
     */
    fun isCallReady(context: Context): Boolean {
        return Settings.canDrawOverlays(context) &&
                isNotificationEnabled(context) &&
                isIgnoringBattery(context)
    }

    /**
     * Mở cài đặt quyền đặc biệt cho các máy Trung Quốc (Xiaomi, Oppo, Vivo).
     */
    fun openManufacturerPermissions(context: Context) {
        val manufacturer = android.os.Build.MANUFACTURER.lowercase()
        val intents = mutableListOf<Intent>()

        when {
            manufacturer.contains("xiaomi") || manufacturer.contains("redmi") || manufacturer.contains("poco") -> {
                intents.add(Intent("miui.intent.action.APP_PERM_EDITOR").apply {
                    setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity")
                    putExtra("extra_pkgname", context.packageName)
                })
                intents.add(Intent("miui.intent.action.APP_PERM_EDITOR").apply {
                    setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity")
                    putExtra("extra_pkgname", context.packageName)
                })
            }
            manufacturer.contains("oppo") || manufacturer.contains("realme") || manufacturer.contains("oneplus") -> {
                intents.add(Intent().setClassName("com.coloros.safecenter", "com.coloros.safecenter.permission.PermissionManagerActivity"))
                intents.add(Intent().setClassName("com.coloros.safecenter", "com.coloros.safecenter.permission.floatwindow.FloatWindowListActivity"))
                intents.add(Intent().setClassName("com.oppo.safe", "com.oppo.safe.permission.PermissionAppListActivity"))
                intents.add(Intent().setClassName("com.coloros.safecenter", "com.coloros.safecenter.sysmacb.SysMACBActivity"))
            }
            manufacturer.contains("vivo") || manufacturer.contains("iqoo") -> {
                intents.add(Intent().setClassName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.PurviewTabActivity"))
                intents.add(Intent().setClassName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.SoftwareManagerActivity"))
                intents.add(Intent().setClassName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"))
            }
            manufacturer.contains("huawei") || manufacturer.contains("honor") -> {
                intents.add(Intent().setClassName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity"))
                intents.add(Intent().setClassName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"))
            }
            manufacturer.contains("samsung") -> {
                intents.add(Intent().setClassName("com.samsung.android.sm_cn", "com.samsung.android.sm.ui.ram.AutoRunActivity"))
                intents.add(Intent().setClassName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity"))
            }
        }

        // Default Overlay Permission (Most reliable for generic Android)
        intents.add(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
            data = android.net.Uri.parse("package:${context.packageName}")
        })

        // Final Fallback to App Details Settings
        intents.add(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.parse("package:${context.packageName}")
        })

        // Loop through the intents and start the first one that exists
        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return // Success
            } catch (e: Exception) {
                // Ignore and try next intent
            }
        }
    }
}