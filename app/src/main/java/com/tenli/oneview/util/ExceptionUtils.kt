package com.tenli.oneview.util

import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun Throwable.toUserFriendlyMessage(): String {
    return when (this) {
        is UnknownHostException -> "Không có kết nối mạng. Vui lòng kiểm tra lại."
        is ConnectException -> "Không thể kết nối đến máy chủ. Vui lòng thử lại sau."
        is SocketTimeoutException -> "Kết nối quá hạn. Vui lòng thử lại."
        else -> this.localizedMessage ?: "Đã xảy ra lỗi không xác định."
    }
}
