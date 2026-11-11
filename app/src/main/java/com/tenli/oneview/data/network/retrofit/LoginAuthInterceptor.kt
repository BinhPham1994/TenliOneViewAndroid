package com.tenli.oneview.data.network.retrofit

import com.tenli.oneview.data.local.UserSession
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class LoginAuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder()

        // Replace Host with dynamic domain from UserSession only if request uses the default placeholder baseUrl
        val defaultHost = HostResolver.defaultHost
        var effectiveUrl = request.url
        if (effectiveUrl.host == defaultHost) {
            val domainUrl = HostResolver.currentDomainUrl()
                ?: throw IOException("No active backend domain configured")
            var newPath = effectiveUrl.encodedPath
            if (newPath.startsWith("/api/v1/")) {
                newPath = newPath.removePrefix("/api/v1")
            }

            effectiveUrl = effectiveUrl.newBuilder()
                .scheme(domainUrl.scheme)
                .host(domainUrl.host)
                .port(domainUrl.port)
                .encodedPath(newPath)
                .build()
            builder.url(effectiveUrl)
        }

        // Chỉ gắn Bearer token của phiên hiện tại khi gọi đúng host backend đang đăng nhập
        // (tránh gửi nhầm token cũ khi tải modules-config.json của domain mới trước lúc đăng nhập)
        if (request.header("Authorization") == null &&
            !effectiveUrl.encodedPath.contains("LogIn", ignoreCase = true) &&
            !effectiveUrl.encodedPath.endsWith("/modules-config.json", ignoreCase = true) &&
            HostResolver.isMainBackendHost(effectiveUrl.host)
        ) {
            val token = UserSession.accessToken
            if (token.isNotEmpty()) {
                builder.header("Authorization", "Bearer $token")
            }
        }

        return chain.proceed(builder.build())
    }
}
