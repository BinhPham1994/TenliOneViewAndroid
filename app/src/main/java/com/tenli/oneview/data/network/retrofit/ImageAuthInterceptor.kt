package com.tenli.oneview.data.network.retrofit

import com.tenli.oneview.data.local.UserSession
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Gắn Bearer token cho request ảnh (Coil) khi ảnh thuộc backend Tenli.
 * Không gắn token cho host bên thứ 3 (vd. URL ký sẵn của storage) để tránh lộ token / lỗi 400.
 */
class ImageAuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        val token = UserSession.accessToken
        if (token.isEmpty() || req.header("Authorization") != null) {
            return chain.proceed(req)
        }

        val path = req.url.encodedPath
        val domainHost = HostResolver.currentDomainHost()
        val needsAuth = (domainHost != null && req.url.host.equals(domainHost, ignoreCase = true)) ||
            AUTH_PATH_MARKERS.any { path.contains(it) }

        return if (needsAuth) {
            chain.proceed(req.newBuilder().header("Authorization", "Bearer $token").build())
        } else {
            chain.proceed(req)
        }
    }

    private companion object {
        val AUTH_PATH_MARKERS = arrayOf("/files/download/", "/Data/", "/VMS/", "/api/", "/ts/api/")
    }
}

