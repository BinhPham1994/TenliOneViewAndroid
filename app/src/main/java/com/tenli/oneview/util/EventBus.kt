package com.tenli.oneview.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object EventBus {
    private val _eventReportedFalse = MutableSharedFlow<Int>(extraBufferCapacity = 8)
    val eventReportedFalse = _eventReportedFalse.asSharedFlow()

    private val _networkRestored = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val networkRestored = _networkRestored.asSharedFlow()

    private val _unauthorized = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val unauthorized = _unauthorized.asSharedFlow()

    /** Phát khi nhận được tín hiệu sự kiện mới qua FCM ở foreground (kênh dự phòng song song với WebSocket) */
    private val _realtimeEventHint = MutableSharedFlow<Unit>(extraBufferCapacity = 8)
    val realtimeEventHint = _realtimeEventHint.asSharedFlow()

    /** Phát khi [com.tenli.oneview.data.network.retrofit.TokenAuthenticator] vừa lấy lại JWT mới */
    private val _tokenRefreshed = MutableSharedFlow<String>(extraBufferCapacity = 2)
    val tokenRefreshed = _tokenRefreshed.asSharedFlow()

    suspend fun emitEventReportedFalse(eventId: Int) {
        _eventReportedFalse.emit(eventId)
    }

    fun emitNetworkRestored() {
        _networkRestored.tryEmit(Unit)
    }

    fun emitUnauthorized() {
        _unauthorized.tryEmit(Unit)
    }

    fun emitRealtimeEventHint() {
        _realtimeEventHint.tryEmit(Unit)
    }

    fun emitTokenRefreshed(newToken: String) {
        _tokenRefreshed.tryEmit(newToken)
    }
}
