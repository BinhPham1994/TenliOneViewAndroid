package com.tenli.oneview.data.network.retrofit

import com.tenli.oneview.BuildConfig
import com.tenli.oneview.data.local.UserSession
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * Cache kết quả parse URL domain (BuildConfig + domain người dùng đăng nhập).
 * Interceptor chạy cho MỌI request (API + từng ảnh), nên tránh parse lại chuỗi URL mỗi lần.
 */
internal object HostResolver {

    /** Host mặc định cấu hình lúc build (placeholder baseUrl của Retrofit) */
    val defaultHost: String? by lazy { BuildConfig.DOMAIN_CLOUD.toHttpUrlOrNull()?.host }

    @Volatile
    private var cachedDomainRaw: String? = null

    @Volatile
    private var cachedDomainUrl: HttpUrl? = null

    /** HttpUrl của domain người dùng hiện tại (null nếu chưa đăng nhập / domain không hợp lệ) */
    fun currentDomainUrl(): HttpUrl? {
        val raw = UserSession.domain
        if (raw.isEmpty()) return null
        if (raw != cachedDomainRaw) {
            cachedDomainUrl = raw.trim().toHttpUrlOrNull()
            cachedDomainRaw = raw
        }
        return cachedDomainUrl
    }

    fun currentDomainHost(): String? = currentDomainUrl()?.host

    /** true nếu host thuộc backend chính (domain đăng nhập hoặc domain mặc định) */
    fun isMainBackendHost(host: String): Boolean {
        val current = currentDomainHost()
        val default = defaultHost
        return (current != null && host.equals(current, ignoreCase = true)) ||
            (default != null && host.equals(default, ignoreCase = true))
    }
}

