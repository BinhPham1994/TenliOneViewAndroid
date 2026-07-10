package com.tenli.oneview.di

import com.tenli.oneview.data.repository.AuthRepository
import com.tenli.oneview.util.DefaultDispatcherProvider
import com.tenli.oneview.util.DispatcherProvider

/**
 * Dependency Injection container for manual DI.
 */
interface AppContainer {
    val authRepository: AuthRepository
    val dispatcherProvider: DispatcherProvider
    val networkMonitor: com.tenli.oneview.util.NetworkMonitor
    val webSocketManager: com.tenli.oneview.data.network.websocket.WebSocketManager
}

class DefaultAppContainer(private val context: android.content.Context) : AppContainer {
    override val authRepository: AuthRepository by lazy {
        AuthRepository()
    }

    override val dispatcherProvider: DispatcherProvider by lazy {
        DefaultDispatcherProvider()
    }

    override val networkMonitor: com.tenli.oneview.util.NetworkMonitor by lazy {
        com.tenli.oneview.util.NetworkMonitor(context, dispatcherProvider)
    }

    override val webSocketManager: com.tenli.oneview.data.network.websocket.WebSocketManager by lazy {
        // Tạo OkHttpClient riêng cho WebSocket:
        // - Bỏ readTimeout (WebSocket cần giữ kết nối lâu dài, readTimeout=15s sẽ kill connection)
        // - Thêm pingInterval để OkHttp tự gửi ping/pong giữ kết nối sống
        val wsClient = com.tenli.oneview.data.network.retrofit.LoginAuthClient.client
            .newBuilder()
            .readTimeout(0, java.util.concurrent.TimeUnit.SECONDS)
            .pingInterval(20, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        val gson = com.tenli.oneview.data.network.retrofit.LoginAuthClient.gson
        val vmsApi = com.tenli.oneview.data.network.retrofit.LoginAuthClient.create(com.tenli.oneview.data.network.api.VmsApi::class.java)
        com.tenli.oneview.data.network.websocket.WebSocketManager(wsClient, gson, vmsApi)
    }
}
