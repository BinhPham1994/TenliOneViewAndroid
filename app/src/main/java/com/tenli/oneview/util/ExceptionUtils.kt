package com.tenli.oneview.util

import com.google.gson.JsonSyntaxException
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.ui.utils.LocaleManager
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import java.io.EOFException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/** Ném ra khi request mạng vẫn chạy sau khi phiên đăng nhập đã bị xoá (logout) – không phải lỗi kết nối máy chủ. */
class SessionClearedException : IOException("No active backend domain configured")

class BackendHttpException(
    val code: Int,
    val rawMessage: String = ""
) : IOException("HTTP $code $rawMessage".trim())

/**
 * Thực thi lời gọi Retrofit an toàn:
 * - Luôn ném lại [CancellationException] để hủy coroutine không bao giờ bị báo nhầm là lỗi mạng/backend.
 * - Nếu HTTP status không thành công (`!isSuccessful`, VD: 500, 502, 503, 404...), trả về [Result.failure] với [BackendHttpException].
 */
suspend inline fun <T> safeApiCall(crossinline block: suspend () -> Response<T>): Result<Response<T>> {
    return try {
        val response = block()
        if (response.isSuccessful) {
            Result.success(response)
        } else {
            Result.failure(BackendHttpException(response.code(), response.message()))
        }
    } catch (e: CancellationException) {
        throw e
    } catch (t: Throwable) {
        Result.failure(t)
    }
}

fun noNetworkMessage(
    isEn: Boolean = LocaleManager.languageFlow.value.startsWith("en")
): String = if (isEn) {
    "No network connection, please check again"
} else {
    "Không có kết nối mạng, vui lòng kiểm tra lại"
}

fun Throwable.toUserFriendlyMessage(
    isEn: Boolean = LocaleManager.languageFlow.value.startsWith("en")
): String {
    if (this is SessionClearedException || UserSession.accessToken.isEmpty()) {
        return ""
    }
    if (!NetworkMonitor.isNetworkAvailable()) {
        return noNetworkMessage(isEn)
    }
    return when (this) {
        is BackendHttpException -> when (code) {
            401, 403 -> if (isEn) {
                "Session expired or access denied (HTTP $code)"
            } else {
                "Phiên đăng nhập hết hạn hoặc không có quyền truy cập (HTTP $code)"
            }
            404 -> if (isEn) {
                "Service not found on server (HTTP 404)"
            } else {
                "Không tìm thấy dịch vụ trên máy chủ (HTTP 404)"
            }
            429 -> if (isEn) {
                "Too many requests to server (HTTP 429)"
            } else {
                "Quá nhiều yêu cầu đến máy chủ (HTTP 429)"
            }
            in 500..599 -> if (isEn) {
                "Server error (HTTP $code). Displaying cached data"
            } else {
                "Máy chủ đang gặp sự cố (HTTP $code). Đang hiển thị dữ liệu đã lưu"
            }
            else -> if (isEn) {
                "Server returned an error (HTTP $code)"
            } else {
                "Máy chủ phản hồi lỗi (HTTP $code)"
            }
        }
        is SSLException -> if (isEn) {
            "Secure connection error to server (SSL)"
        } else {
            "Lỗi kết nối bảo mật đến máy chủ (SSL)"
        }
        is JsonSyntaxException, is EOFException -> if (isEn) {
            "Invalid response data from server"
        } else {
            "Dữ liệu phản hồi từ máy chủ không hợp lệ"
        }
        is UnknownHostException, is ConnectException, is SocketTimeoutException, is IOException -> if (isEn) {
            "Connection error to server, please try again later"
        } else {
            "Lỗi kết nối đến máy chủ, vui lòng thử lại sau"
        }
        else -> if (isEn) {
            "Connection error to server, please try again later"
        } else {
            "Lỗi kết nối đến máy chủ, vui lòng thử lại sau"
        }
    }
}
