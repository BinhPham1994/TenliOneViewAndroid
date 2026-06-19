package com.tenli.oneview.data.network.retrofit

import com.tenli.oneview.BuildConfig
import com.tenli.oneview.data.local.UserSession
import okhttp3.Credentials
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response

class LoginAuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val builder = request.newBuilder()

        // Replace Host with dynamic domain from UserSession
        if (UserSession.domain.isNotEmpty()) {
            val domainUrl = UserSession.domain.toHttpUrlOrNull()
            if (domainUrl != null) {
                var newPath = request.url.encodedPath
                if (newPath.startsWith("/api/v1/")) {
                    newPath = newPath.removePrefix("/api/v1")
                }

                val newUrlStr = "${domainUrl.scheme}://${domainUrl.host}:${domainUrl.port}$newPath"
                val parsedUrl = newUrlStr.toHttpUrlOrNull()
                
                if (parsedUrl != null) {
                    val urlBuilder = parsedUrl.newBuilder()
                    for (i in 0 until request.url.querySize) {
                        urlBuilder.addQueryParameter(request.url.queryParameterName(i), request.url.queryParameterValue(i))
                    }
                    builder.url(urlBuilder.build())
                }
            }
        }

        // Apply correct Authorization Header
        if (request.url.encodedPath.contains("LogIn", ignoreCase = true)) {
            builder.header("Authorization", Credentials.basic(BuildConfig.CLIENT_ID, BuildConfig.CLIENT_SECRET))
        } else if (UserSession.accessToken.isNotEmpty()) {
            builder.header("Authorization", "Bearer ${UserSession.accessToken}")
        }

        return chain.proceed(builder.build())
    }
}