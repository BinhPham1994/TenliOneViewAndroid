package com.tenli.oneview.data.network.retrofit

import com.tenli.oneview.BuildConfig
import com.tenli.oneview.data.local.UserSession
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response

class LoginAuthInterceptor : Interceptor {

    /** Basic auth cho LogIn – tính 1 lần thay vì encode Base64 mỗi request */
    private val basicAuth: String by lazy { Credentials.basic(BuildConfig.CLIENT_ID, BuildConfig.CLIENT_SECRET) }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder()

        // Replace Host with dynamic domain from UserSession only if request uses the default placeholder baseUrl
        val defaultHost = HostResolver.defaultHost
        if (defaultHost == null || request.url.host == defaultHost) {
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

        // Apply correct Authorization Header if not already explicitly provided by @Header("Authorization")
        if (request.header("Authorization") == null) {
            if (request.url.encodedPath.contains("LogIn", ignoreCase = true)) {
                builder.header("Authorization", basicAuth)
            } else {
                val token = UserSession.accessToken
                if (token.isNotEmpty()) {
                    builder.header("Authorization", "Bearer $token")
                }
            }
        }

        return chain.proceed(builder.build())
    }
}
