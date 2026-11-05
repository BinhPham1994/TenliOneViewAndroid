package com.tenli.oneview.data.network.retrofit

import com.tenli.oneview.data.local.UserSession
import okhttp3.Interceptor
import okhttp3.Response

class LoginAuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder()

        // Replace Host with dynamic domain from UserSession only if request uses the default placeholder baseUrl
        val defaultHost = HostResolver.defaultHost
        if (request.url.host == defaultHost) {
            val domainUrl = HostResolver.currentDomainUrl()
            if (domainUrl != null) {
                var newPath = request.url.encodedPath
                if (newPath.startsWith("/api/v1/")) {
                    newPath = newPath.removePrefix("/api/v1")
                }

                val updatedUrl = request.url.newBuilder()
                    .scheme(domainUrl.scheme)
                    .host(domainUrl.host)
                    .port(domainUrl.port)
                    .encodedPath(newPath)
                    .build()
                builder.url(updatedUrl)
            }
        }

        // Apply Bearer Authorization Header if not LogIn and not already explicitly provided by @Header("Authorization")
        if (request.header("Authorization") == null &&
            !request.url.encodedPath.contains("LogIn", ignoreCase = true)
        ) {
            val token = UserSession.accessToken
            if (token.isNotEmpty()) {
                builder.header("Authorization", "Bearer $token")
            }
        }

        return chain.proceed(builder.build())
    }
}
