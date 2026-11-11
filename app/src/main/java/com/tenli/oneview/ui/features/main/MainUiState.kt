package com.tenli.oneview.ui.features.main

import androidx.compose.runtime.Immutable

@Immutable
data class MainUiState(
    val currentTab: MainTab = MainTab.Home,
    val isOffline: Boolean = false,
    val accountName: String = "",
    val canAccessEvents: Boolean = true,
    val canAccessLiveStream: Boolean = true
)

enum class MainTab(val route: String) {
    Home("home"),
    Monitor("monitor"),
    Event("event"),
    Setting("setting")
}

sealed interface MainEvent {
    data object Logout : MainEvent
}

/** Chế độ cảnh báo cho từng bài AI ("push" = thông báo chuẩn, "call" = màn hình cuộc gọi đến) */
enum class AiAlarmMode(val apiValue: String) {
    Push("push"),
    Call("call");

    companion object {
        fun fromApiValue(raw: String?): AiAlarmMode {
            return if (raw?.trim()?.equals("call", ignoreCase = true) == true) Call else Push
        }
    }
}

/** Cấu hình thông báo của một bài AI cụ thể trong `overrides.event_types` */
@Immutable
data class AiModuleNotifyConfig(
    val moduleKey: String,
    val sensorType: String,
    val labelVi: String,
    val labelEn: String,
    val eventTypes: List<String>,
    val enabled: Boolean = true,
    val alarmMode: AiAlarmMode = AiAlarmMode.Push
)

/** Trạng thái màn hình "Cấu hình thông báo" (MobileNotificationConfigModal.vue) */
@Immutable
data class NotificationConfigState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val pushEnabled: Boolean = true,
    val aiModules: List<AiModuleNotifyConfig> = emptyList()
)

/** Trạng thái sheet "Đổi mật khẩu" */
@Immutable
data class PasswordChangeState(
    val isSubmitting: Boolean = false,
    val error: PasswordChangeError? = null,
    val isSuccess: Boolean = false
)

/** Lỗi đổi mật khẩu – UI tự chuyển sang chuỗi hiển thị theo ngôn ngữ */
sealed interface PasswordChangeError {
    data object EmptyFields : PasswordChangeError
    data object TooShort : PasswordChangeError
    data object Mismatch : PasswordChangeError
    data object UserNotFound : PasswordChangeError
    data class WrongCurrentPassword(val code: Int) : PasswordChangeError
    data class Unexpected(val message: String?) : PasswordChangeError
}
