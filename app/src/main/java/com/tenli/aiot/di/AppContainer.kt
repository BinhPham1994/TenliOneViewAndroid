package com.tenli.aiot.di

import com.tenli.aiot.data.repository.AlarmRepository
import com.tenli.aiot.data.repository.AppRepository
import com.tenli.aiot.data.repository.AuthRepository
import com.tenli.aiot.data.repository.BoxRepository
import com.tenli.aiot.data.repository.DataRepository
import com.tenli.aiot.data.repository.EventRepository
import com.tenli.aiot.data.repository.UserRepository
import com.tenli.aiot.util.DefaultDispatcherProvider
import com.tenli.aiot.util.DispatcherProvider

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
