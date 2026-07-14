package com.tenli.oneview.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object EventBus {
    private val _eventReportedFalse = MutableSharedFlow<Int>()
    val eventReportedFalse = _eventReportedFalse.asSharedFlow()

    suspend fun emitEventReportedFalse(eventId: Int) {
        _eventReportedFalse.emit(eventId)
    }
}
