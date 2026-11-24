package com.tenli.oneview.data.network.retrofit

import com.tenli.oneview.data.local.UserSession
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * Cache kết quả parse URL domain người dùng đăng nhập (cơ chế động giống PWA tenli-oneview).
 * Interceptor chạy cho MỌI request (API + từng ảnh), nên tránh parse lại chuỗi URL mỗi lần.
 */
internal object HostResolver {

    /** BaseUrl placeholder cho Retrofit (sẽ được LoginAuthInterceptor thay thế bằng UserSession.domain động) */
    const val PLACEHOLDER_BASE_URL = "http://localhost/"

    /** Host placeholder mặc định của Retrofit */
    const val defaultHost: String = "localhost"

    private data class CachedDomain(val raw: String, val url: HttpUrl?)

    @Volatile
    private var cachedDomain: CachedDomain? = null

    /** HttpUrl của domain người dùng hiện tại (null nếu chưa đăng nhập / domain không hợp lệ) */
    fun currentDomainUrl(): HttpUrl? {
        val raw = UserSession.domain
        if (raw.isEmpty()) return null
        val current = cachedDomain
        if (current != null && current.raw == raw) {
            return current.url
        }
        val parsed = raw.trim().toHttpUrlOrNull()
        cachedDomain = CachedDomain(raw, parsed)
        return parsed
    }

    fun currentDomainHost(): String? = currentDomainUrl()?.host

    /** true nếu host thuộc backend chính (domain đăng nhập hiện tại) */
    fun isMainBackendHost(host: String): Boolean {
        val current = currentDomainHost()
        return current != null && host.equals(current, ignoreCase = true)
    }
}

