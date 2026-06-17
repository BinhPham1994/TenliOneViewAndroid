package com.tenli.aiot.data.network.retrofit

import com.google.gson.GsonBuilder
import com.tenli.aiot.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object BoxAuthClient {

    private val gson = GsonBuilder().setLenient().create()
    private val serviceCache = mutableMapOf<String, Any>()

    private val logging = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
        else HttpLoggingInterceptor.Level.NONE
    }

    private val baseClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    fun <T> create(service: Class<T>, deviceDomain: String, deviceToken: String): T {
        val cacheKey = "$deviceDomain-$deviceToken-${service.name}"
        if (serviceCache.containsKey(cacheKey)) {
            @Suppress("UNCHECKED_CAST")
            return serviceCache[cacheKey] as T
        }
        val baseUrl = if (deviceDomain.endsWith("/")) deviceDomain else "$deviceDomain/"
        val client = baseClient.newBuilder()
            .addInterceptor(BoxAuthInterceptor("Bearer", deviceToken))
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val apiService = retrofit.create(service)
        serviceCache[cacheKey] = apiService!!
        return apiService
    }

    fun clearCache() {
        serviceCache.clear()
    }

    inline fun <reified T> create(deviceDomain: String, deviceToken: String): T {
        return create(T::class.java, deviceDomain, deviceToken)
    }
}