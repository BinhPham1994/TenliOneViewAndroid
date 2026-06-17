package com.tenli.oneview.di

import com.tenli.oneview.data.repository.AlarmRepository
import com.tenli.oneview.data.repository.AppRepository
import com.tenli.oneview.data.repository.AuthRepository
import com.tenli.oneview.data.repository.BoxRepository
import com.tenli.oneview.data.repository.DataRepository
import com.tenli.oneview.data.repository.EventRepository
import com.tenli.oneview.data.repository.UserRepository
import com.tenli.oneview.util.DefaultDispatcherProvider
import com.tenli.oneview.util.DispatcherProvider

/**
 * Dependency Injection container for manual DI.
 */
interface AppContainer {
    val alarmRepository: AlarmRepository
    val appRepository: AppRepository
    val authRepository: AuthRepository
    val boxRepository: BoxRepository
    val eventRepository: EventRepository
    val userRepository: UserRepository
    val dispatcherProvider: DispatcherProvider
}

class DefaultAppContainer : AppContainer {
    override val alarmRepository: AlarmRepository by lazy {
        AlarmRepository()
    }

    override val appRepository: AppRepository by lazy {
        AppRepository(DataRepository)
    }

    override val authRepository: AuthRepository by lazy {
        AuthRepository()
    }

    override val boxRepository: BoxRepository by lazy {
        BoxRepository()
    }

    override val eventRepository: EventRepository by lazy {
        EventRepository()
    }

    override val userRepository: UserRepository by lazy {
        UserRepository()
    }

    override val dispatcherProvider: DispatcherProvider by lazy {
        DefaultDispatcherProvider()
    }
}
