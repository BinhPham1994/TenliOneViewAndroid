package com.tenli.oneview.data.network.retrofit

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.tenli.oneview.BuildConfig
import com.tenli.oneview.model.network.BsProfileConfig
import com.tenli.oneview.model.network.BsProfileGroupConfig
import com.tenli.oneview.model.network.CameraExtra
import com.tenli.oneview.model.network.CameraMonitorExtra
import com.tenli.oneview.model.network.EventDataDetail
import com.tenli.oneview.model.network.PlateConfig
import com.tenli.oneview.model.network.PlateGroupConfig
import com.tenli.oneview.model.network.ProfileGroupData
import com.tenli.oneview.model.network.RuleObjectModel
import com.tenli.oneview.model.network.TaskConfigModel
import com.tenli.oneview.model.network.UniformLabelExtraConfig
import com.tenli.oneview.model.network.UserExtra
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

object LoginAuthClient {

    // Timeouts đồng bộ với api.ts (GET_TIMEOUT_MS = 30s, WRITE_TIMEOUT_MS = 60s)
    private const val CONNECT_TIMEOUT_SEC = 30L
    private const val READ_TIMEOUT_SEC = 30L
    private const val WRITE_TIMEOUT_SEC = 60L
    private const val IMAGE_TIMEOUT_SEC = 15L
    private const val WS_PING_INTERVAL_SEC = 25L
    private const val MAX_REQUESTS = 64
    private const val MAX_REQUESTS_PER_HOST = 16
    private const val MAX_IDLE_CONNECTIONS = 10
    private const val KEEP_ALIVE_MINUTES = 5L

    /** Gson lenient dùng chung cho mọi [StringOrObjectDeserializer] (tránh tạo 1 instance Gson cho mỗi type) */
    private val lenientGson: Gson by lazy { GsonBuilder().setLenient().create() }

    /**
     * Deserializer linh hoạt hỗ trợ cả JSON Object và JSON String được stringify từ backend
     * (tương đương logic `typeof x === 'string' ? JSON.parse(x) : x` trong PWA eventData.ts / vmsData.ts / auth.ts).
     */
    private class StringOrObjectDeserializer<T>(private val clazz: Class<T>) : JsonDeserializer<T?> {
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): T? {
            if (json == null || json.isJsonNull) return null
            return try {
                when {
                    json.isJsonObject -> lenientGson.fromJson(json, clazz)
                    json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                        val raw = json.asString.trim()
                        if (raw.startsWith("{")) lenientGson.fromJson(raw, clazz) else null
                    }
                    else -> null
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    /**
     * Client gốc: giữ DUY NHẤT 1 connection pool + 1 dispatcher (thread pool) cho toàn app.
     * Các client chuyên dụng (API, ảnh, WebSocket, re-login, live stream) đều tạo bằng `newBuilder()`
     * từ client này nên dùng chung tài nguyên → ít thread, ít socket, ít TLS handshake → tiết kiệm pin/CPU.
     */
    val baseClient: OkHttpClient by lazy {
        val dispatcher = Dispatcher().apply {
            maxRequests = MAX_REQUESTS
            maxRequestsPerHost = MAX_REQUESTS_PER_HOST
        }
        OkHttpClient.Builder()
            .dispatcher(dispatcher)
            .connectionPool(ConnectionPool(MAX_IDLE_CONNECTIONS, KEEP_ALIVE_MINUTES, TimeUnit.MINUTES))
            .connectTimeout(CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SEC, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SEC, TimeUnit.SECONDS)
            .build()
    }

    /** Client cho REST API: gắn auth header, rewrite domain động, tự re-login khi 401 */
    val client: OkHttpClient by lazy {
        // Sử dụng BASIC thay vì BODY để tránh buffer toàn bộ JSON lớn vào RAM và spam Logcat gây giật/nóng máy
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
            else HttpLoggingInterceptor.Level.NONE
        }
        baseClient.newBuilder()
            .addInterceptor(LoginAuthInterceptor())
            .addInterceptor(logging)
            .authenticator(TokenAuthenticator())
            .build()
    }

    /**
     * Client cho WebSocket Notify / Report:
     * - Bỏ readTimeout (WebSocket cần giữ kết nối lâu dài, readTimeout sẽ kill connection)
     * - pingInterval để OkHttp phát hiện kết nối chết và đóng → kích hoạt reconnect
     */
    val webSocketClient: OkHttpClient by lazy {
        client.newBuilder()
            .readTimeout(0, TimeUnit.SECONDS)
            .pingInterval(WS_PING_INTERVAL_SEC, TimeUnit.SECONDS)
            .build()
    }

    /** Client cho Coil tải ảnh: chỉ gắn token cho host backend (không lộ token cho host bên thứ 3) */
    val imageClient: OkHttpClient by lazy {
        baseClient.newBuilder()
            .addInterceptor(ImageAuthInterceptor())
            .connectTimeout(IMAGE_TIMEOUT_SEC, TimeUnit.SECONDS)
            .readTimeout(IMAGE_TIMEOUT_SEC, TimeUnit.SECONDS)
            .build()
    }

    val gson: Gson = GsonBuilder()
        .setLenient()
        .registerTypeAdapter(EventDataDetail::class.java, StringOrObjectDeserializer(EventDataDetail::class.java))
        .registerTypeAdapter(CameraExtra::class.java, StringOrObjectDeserializer(CameraExtra::class.java))
        .registerTypeAdapter(UserExtra::class.java, StringOrObjectDeserializer(UserExtra::class.java))
        .registerTypeAdapter(CameraMonitorExtra::class.java, StringOrObjectDeserializer(CameraMonitorExtra::class.java))
        .registerTypeAdapter(ProfileGroupData::class.java, StringOrObjectDeserializer(ProfileGroupData::class.java))
        .registerTypeAdapter(TaskConfigModel::class.java, StringOrObjectDeserializer(TaskConfigModel::class.java))
        .registerTypeAdapter(RuleObjectModel::class.java, StringOrObjectDeserializer(RuleObjectModel::class.java))
        .registerTypeAdapter(BsProfileGroupConfig::class.java, StringOrObjectDeserializer(BsProfileGroupConfig::class.java))
        .registerTypeAdapter(BsProfileConfig::class.java, StringOrObjectDeserializer(BsProfileConfig::class.java))
        .registerTypeAdapter(PlateConfig::class.java, StringOrObjectDeserializer(PlateConfig::class.java))
        .registerTypeAdapter(PlateGroupConfig::class.java, StringOrObjectDeserializer(PlateGroupConfig::class.java))
        .registerTypeAdapter(UniformLabelExtraConfig::class.java, StringOrObjectDeserializer(UniformLabelExtraConfig::class.java))
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
