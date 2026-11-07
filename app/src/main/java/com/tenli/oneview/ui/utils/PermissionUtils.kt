package com.tenli.oneview.ui.utils

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
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
     * Kiểm tra quyền hiển thị thông báo toàn màn hình (USE_FULL_SCREEN_INTENT) trên Android 14+ (API 34+).
     * Trên Android 13 trở xuống quyền này được cấp tự động khi khai báo trong Manifest.
     */
    fun canUseFullScreenIntent(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val nm = context.getSystemService(NotificationManager::class.java)
            nm?.canUseFullScreenIntent() ?: true
        } else {
            true
        }
    }

    /**
     * Kiểm tra đủ quyền hiển thị màn hình Cuộc gọi khẩn cấp trên mọi trạng thái (đang mở máy + khoá màn hình):
     * - `Settings.canDrawOverlays`: cho phép bật Activity cuộc gọi đè lên ứng dụng khác.
     * - `canUseFullScreenIntent`: cho phép đánh thức màn hình khoá bằng FullScreenIntent trên Android 14+.
     */
    fun isEmergencyCallPermissionGranted(context: Context): Boolean {
        return Settings.canDrawOverlays(context) && canUseFullScreenIntent(context)
    }

    /**
     * Tổng hợp tất cả các quyền để biết App có thể nhận Call 100% hay chưa.
     */
    fun isCallReady(context: Context): Boolean {
        return isEmergencyCallPermissionGranted(context) &&
                isNotificationEnabled(context) &&
                isIgnoringBattery(context)
    }

    /**
     * Mở trang cài đặt quyền còn thiếu cho Cuộc gọi khẩn cấp:
     * - Nếu thiếu `SYSTEM_ALERT_WINDOW` -> mở cài đặt "Hiển thị trên ứng dụng khác" (hoặc cài đặt OEM).
     * - Nếu đã có `SYSTEM_ALERT_WINDOW` nhưng thiếu `USE_FULL_SCREEN_INTENT` (Android 14+) -> mở trang cấp quyền Full-screen intent.
     */
    fun openEmergencyCallPermissionSettings(context: Context, preferManufacturerSettings: Boolean = false) {
        val pkgUri = Uri.parse("package:${context.packageName}")
        if (!Settings.canDrawOverlays(context)) {
            if (preferManufacturerSettings) {
                openManufacturerPermissions(context)
                return
            }
            try {
                context.startActivity(
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, pkgUri).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                )
                return
            } catch (_: Exception) {
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && !canUseFullScreenIntent(context)) {
            try {
                context.startActivity(
                    Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, pkgUri).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                )
                return
            } catch (_: Exception) {
            }
        }

        openManufacturerPermissions(context)
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