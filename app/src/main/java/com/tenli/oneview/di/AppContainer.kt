package com.tenli.oneview.di

import android.content.Context
import com.tenli.oneview.data.network.ApiProvider
import com.tenli.oneview.data.network.retrofit.LoginAuthClient
import com.tenli.oneview.data.network.websocket.WebSocketManager
import com.tenli.oneview.data.repository.AuthRepository
import com.tenli.oneview.util.DefaultDispatcherProvider
import com.tenli.oneview.util.DispatcherProvider
import com.tenli.oneview.util.NetworkMonitor

/**
 * Dependency Injection container for manual DI.
 */
interface AppContainer {
    val authRepository: AuthRepository
    val dispatcherProvider: DispatcherProvider
    val networkMonitor: NetworkMonitor
    val webSocketManager: WebSocketManager
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val authRepository: AuthRepository by lazy { AuthRepository() }

    override val dispatcherProvider: DispatcherProvider by lazy { DefaultDispatcherProvider() }

    override val networkMonitor: NetworkMonitor by lazy { NetworkMonitor(context, dispatcherProvider) }

    override val webSocketManager: WebSocketManager by lazy {
        // OkHttpClient WebSocket dùng chung connection pool với REST API (xem LoginAuthClient.webSocketClient)
        WebSocketManager(LoginAuthClient.webSocketClient, LoginAuthClient.gson, ApiProvider.vmsApi)
    }
}
