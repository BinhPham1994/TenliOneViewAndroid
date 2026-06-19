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
}

class DefaultAppContainer : AppContainer {
    override val authRepository: AuthRepository by lazy {
        AuthRepository()
    }

    override val dispatcherProvider: DispatcherProvider by lazy {
        DefaultDispatcherProvider()
    }
}
