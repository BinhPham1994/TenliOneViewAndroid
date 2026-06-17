package com.tenli.oneview.ui.utils

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed class UiText {
    // Dùng cho chuỗi trả về từ API hoặc chuỗi cứng
    data class DynamicString(val value: String) : UiText()

    // Dùng cho chuỗi trong strings.xml
    class StringResource(
        @StringRes val resId: Int,
        vararg val args: Any
    ) : UiText()

    // Hàm để lấy chuỗi ra trong Composable [cite: 2026-03-25]
    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> stringResource(resId, *args)
        }
    }

    // Hàm để lấy chuỗi ra ở những nơi không phải Composable (như Toast, Snackbar)
    fun asString(context: Context): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> context.getString(resId, *args)
        }
    }
}