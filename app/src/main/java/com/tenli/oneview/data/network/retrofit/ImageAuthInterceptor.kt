package com.tenli.oneview.data.network.retrofit

import com.tenli.oneview.data.local.UserSession
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Gắn Bearer token cho request ảnh (Coil) khi ảnh thuộc host backend Tenli đang đăng nhập.
 * Không gắn token cho host bên thứ 3 hoặc URL ký sẵn của storage để tránh lộ token / lỗi 400.
 */
class ImageAuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        val token = UserSession.accessToken
        if (token.isEmpty() || req.header("Authorization") != null) {
            return chain.proceed(req)
        }

        val url = req.url
        // Không gắn Authorization vào URL lưu trữ đã ký sẵn (S3/MinIO/GCS presigned URL) để tránh lỗi HTTP 400 & tránh lộ token
        if (url.queryParameterNames.any { it.lowercase() in PRESIGNED_QUERY_KEYS_LOWER }) {
            return chain.proceed(req)
        }

        return if (HostResolver.isMainBackendHost(url.host)) {
            chain.proceed(req.newBuilder().header("Authorization", "Bearer $token").build())
        } else {
            chain.proceed(req)
        }
    }

    private companion object {
        val PRESIGNED_QUERY_KEYS_LOWER = setOf(
            "x-amz-signature",
            "x-amz-algorithm",
            "x-amz-credential",
            "googleaccessid",
            "signature"
        )
    }
}

