package com.tenli.oneview.di

import android.content.Context
import com.tenli.oneview.data.network.ApiProvider
import com.tenli.oneview.data.network.retrofit.LoginAuthClient
import com.tenli.oneview.data.network.websocket.WebSocketManager
import com.tenli.oneview.data.repository.AuthRepository
import com.tenli.oneview.data.repository.DashboardRepository
import com.tenli.oneview.data.repository.EventRepository
import com.tenli.oneview.data.repository.MonitorRepository
import com.tenli.oneview.util.DefaultDispatcherProvider
import com.tenli.oneview.util.DispatcherProvider
import com.tenli.oneview.util.NetworkMonitor

/**
 * Dependency Injection container chuẩn MVVM cho toàn bộ ứng dụng.
 */
interface AppContainer {
    val authRepository: AuthRepository
    val dashboardRepository: DashboardRepository
    val eventRepository: EventRepository
    val monitorRepository: MonitorRepository
    val dispatcherProvider: DispatcherProvider
    val networkMonitor: NetworkMonitor
    val webSocketManager: WebSocketManager
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    private val appContext: Context = context.applicationContext

    override val authRepository: AuthRepository by lazy {
        AuthRepository(context = appContext)
    }

    override val dashboardRepository: DashboardRepository by lazy {
        DashboardRepository(context = appContext)
    }

    override val eventRepository: EventRepository by lazy {
        EventRepository(context = appContext)
    }

    override val monitorRepository: MonitorRepository by lazy {
        MonitorRepository(context = appContext)
    }

    override val dispatcherProvider: DispatcherProvider by lazy { DefaultDispatcherProvider() }

    override val networkMonitor: NetworkMonitor by lazy { NetworkMonitor(appContext, dispatcherProvider) }

    override val webSocketManager: WebSocketManager by lazy {
        // Khởi tạo lazy các client/API bên trong IO thread khi kết nối thực sự để không chặn Main thread ở Frame 1
        WebSocketManager(
            clientProvider = { LoginAuthClient.webSocketClient },
            gsonProvider = { LoginAuthClient.gson },
            vmsApiProvider = { ApiProvider.vmsApi }
        )
    }
}
