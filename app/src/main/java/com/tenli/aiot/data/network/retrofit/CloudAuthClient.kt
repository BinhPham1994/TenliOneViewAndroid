package com.tenli.aiot.data.network.retrofit

import androidx.core.content.edit
import com.google.gson.GsonBuilder
import com.tenli.aiot.BuildConfig
import com.tenli.aiot.data.local.GlobalData
import com.tenli.aiot.data.local.UserSession
import com.tenli.aiot.data.network.api.UserApi
import com.tenli.aiot.ui.utils.AppKeys
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object CloudAuthClient {

    private val logging = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
        else HttpLoggingInterceptor.Level.NONE
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val token = UserSession.accessToken
                val newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                chain.proceed(newRequest)
            }
            .authenticator { _, response ->
                if (response.code == 401) {
                    synchronized(this) {
                        val failedToken = response.request.header("Authorization")?.removePrefix("Bearer ")
                        if (failedToken != UserSession.accessToken) {
                            response.request.newBuilder()
                                .header("Authorization", "Bearer ${UserSession.accessToken}")
                                .build()
                        } else {
                            val isSuccess = refreshAccessTokenSync()
                            if (isSuccess) {
                                response.request.newBuilder()
                                    .header("Authorization", "Bearer ${UserSession.accessToken}")
                                    .build()
                            } else {
                                null
                            }
                        }
                    }
                } else null
            }
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        val gson = GsonBuilder().setLenient().create()
        Retrofit.Builder()
            .baseUrl(BuildConfig.DOMAIN_CLOUD)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private fun refreshAccessTokenSync(): Boolean {
        return try {
            val currentRefreshToken = UserSession.refreshToken
            if (currentRefreshToken.isEmpty()) return false

            val api = create(UserApi::class.java)
            val response = api.refreshTokenSync(currentRefreshToken).execute()

            if (response.isSuccessful && response.body()?.code == 600) {
                val tokenData = response.body()?.data ?: return false
                UserSession.accessToken = tokenData.accessToken
                UserSession.refreshToken = tokenData.refreshToken

                GlobalData.preferences.edit {
                    putString(AppKeys.ACCESS_TOKEN_KEY, tokenData.accessToken)
                    putString(AppKeys.REFRESH_TOKEN_KEY, tokenData.refreshToken)
                }
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    fun <T> create(service: Class<T>): T {
        return retrofit.create(service)
    }
}