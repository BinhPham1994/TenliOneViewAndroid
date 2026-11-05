package com.tenli.oneview.ui.features.main

import androidx.compose.runtime.Immutable

@Immutable
data class MainUiState(
    val currentTab: MainTab = MainTab.Home,
    val isOffline: Boolean = false
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

/** Trạng thái màn hình "Cấu hình thông báo" (MobileNotificationConfigModal.vue) */
@Immutable
data class NotificationConfigState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val pushEnabled: Boolean = true
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
