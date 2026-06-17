package com.tenli.aiot.data.network.retrofit

import okhttp3.Interceptor

class BoxAuthInterceptor(private val tokenType: String, private val accessToken: String) :
    Interceptor {

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        var request = chain.request()
        request = request.newBuilder().header("Authorization", "$tokenType $accessToken").build()
        val response = chain.proceed(request)
        return response
    }
}