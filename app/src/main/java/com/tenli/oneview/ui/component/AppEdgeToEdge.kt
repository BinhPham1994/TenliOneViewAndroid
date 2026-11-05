package com.tenli.oneview.ui.component

import android.content.res.Configuration
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import com.tenli.oneview.ui.theme.ThemeManager

/**
 * Bật edge-to-edge (status bar + navigation bar trong suốt, nội dung vẽ dưới notch / gesture bar)
 * với màu icon system bar đúng theo giao diện app (ThemeManager) ngay từ frame đầu tiên.
 *
 * - Gọi TRƯỚC `super.onCreate()`; yêu cầu `ThemeManager.init` đã chạy (TenliApp.onCreate).
 * - Sau đó `TenliAIoTTheme` tiếp tục đồng bộ màu icon khi người dùng đổi giao diện.
 * - Dùng style light/dark tường minh (không dùng `auto`) để thanh điều hướng 3 nút cũng trong suốt như bản gốc.
 */
fun ComponentActivity.enableAppEdgeToEdge() {
    val isDark = when (ThemeManager.themeModeFlow.value) {
        ThemeManager.MODE_LIGHT -> false
        ThemeManager.MODE_DARK -> true
        else -> (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }
    val style = if (isDark) {
        SystemBarStyle.dark(Color.TRANSPARENT)
    } else {
        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
    }
    enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
}
