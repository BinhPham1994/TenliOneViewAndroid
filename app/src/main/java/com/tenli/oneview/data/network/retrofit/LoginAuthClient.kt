package com.tenli.oneview.data.network.retrofit

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.tenli.oneview.BuildConfig
import com.tenli.oneview.model.network.CameraExtra
import com.tenli.oneview.model.network.CameraMonitorExtra
import com.tenli.oneview.model.network.EventDataDetail
import com.tenli.oneview.model.network.UserExtra
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

object LoginAuthClient {

    /**
     * Deserializer linh hoạt hỗ trợ cả JSON Object và JSON String được stringify từ backend
     * (tương đương logic `typeof x === 'string' ? JSON.parse(x) : x` trong PWA eventData.ts / vmsData.ts / auth.ts).
     */
    private class StringOrObjectDeserializer<T>(private val clazz: Class<T>) : JsonDeserializer<T?> {
        private val delegateGson = GsonBuilder().setLenient().create()

        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): T? {
            if (json == null || json.isJsonNull) return null
            return try {
                if (json.isJsonObject) {
                    delegateGson.fromJson(json, clazz)
                } else if (json.isJsonPrimitive && json.asJsonPrimitive.isString) {
                    val raw = json.asString.trim()
                    if (raw.startsWith("{")) {
                        delegateGson.fromJson(raw, clazz)
                    } else {
                        null
                    }
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    val client: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }

        // Timeouts đồng bộ với api.ts (GET_TIMEOUT_MS = 30s, WRITE_TIMEOUT_MS = 60s)
        OkHttpClient.Builder()
            .addInterceptor(LoginAuthInterceptor())
            .addInterceptor(logging)
            .authenticator(TokenAuthenticator())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    val gson: Gson = GsonBuilder()
        .setLenient()
        .registerTypeAdapter(EventDataDetail::class.java, StringOrObjectDeserializer(EventDataDetail::class.java))
        .registerTypeAdapter(CameraExtra::class.java, StringOrObjectDeserializer(CameraExtra::class.java))
        .registerTypeAdapter(UserExtra::class.java, StringOrObjectDeserializer(UserExtra::class.java))
        .registerTypeAdapter(CameraMonitorExtra::class.java, StringOrObjectDeserializer(CameraMonitorExtra::class.java))
        .create()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.DOMAIN_CLOUD)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    fun <T> create(service: Class<T>): T {
        return retrofit.create(service)
    }
}
