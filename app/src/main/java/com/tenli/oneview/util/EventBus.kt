package com.tenli.oneview.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object EventBus {
    private val _eventReportedFalse = MutableSharedFlow<Int>()
    val eventReportedFalse = _eventReportedFalse.asSharedFlow()

    private val _networkRestored = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val networkRestored = _networkRestored.asSharedFlow()

    private val _unauthorized = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val unauthorized = _unauthorized.asSharedFlow()

    suspend fun emitEventReportedFalse(eventId: Int) {
        _eventReportedFalse.emit(eventId)
    }

    fun emitNetworkRestored() {
        _networkRestored.tryEmit(Unit)
    }

    fun emitUnauthorized() {
        _unauthorized.tryEmit(Unit)
    }
}
