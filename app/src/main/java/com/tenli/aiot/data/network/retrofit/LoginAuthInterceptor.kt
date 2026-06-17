package com.tenli.aiot.data.network.retrofit

import com.tenli.aiot.BuildConfig
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response

class LoginAuthInterceptor :
    Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        request = request.newBuilder().header("Authorization", Credentials.basic(BuildConfig.CLIENT_ID, BuildConfig.CLIENT_SECRET)).build()
        val response = chain.proceed(request)
        return response
    }
}