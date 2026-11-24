package com.tenli.oneview.util

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.concurrent.atomic.AtomicBoolean

object EventBus {
    private val _eventReportedFalse = MutableSharedFlow<Int>(extraBufferCapacity = 8)
    val eventReportedFalse = _eventReportedFalse.asSharedFlow()

    private val _networkRestored = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val networkRestored = _networkRestored.asSharedFlow()

    private val _unauthorized = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val unauthorized = _unauthorized.asSharedFlow()
    private val pendingUnauthorized = AtomicBoolean(false)

    /** Phát khi nhận được tín hiệu sự kiện mới qua FCM ở foreground (kênh dự phòng song song với WebSocket) */
    private val _realtimeEventHint = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val realtimeEventHint = _realtimeEventHint.asSharedFlow()

    /** Phát khi [com.tenli.oneview.data.network.retrofit.TokenAuthenticator] vừa lấy lại JWT mới */
    private val _tokenRefreshed = MutableSharedFlow<String>(
        extraBufferCapacity = 2,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val tokenRefreshed = _tokenRefreshed.asSharedFlow()

    /** Phát khi người dùng bấm "Từ chối" cuộc gọi từ thanh thông báo để đóng CallComingActivity nếu đang hiển thị */
    private val _callDismissed = MutableSharedFlow<Int>(
        extraBufferCapacity = 2,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val callDismissed = _callDismissed.asSharedFlow()

    suspend fun emitEventReportedFalse(eventId: Int) {
        _eventReportedFalse.emit(eventId)
    }

    fun emitNetworkRestored() {
        _networkRestored.tryEmit(Unit)
    }

    fun emitUnauthorized() {
        pendingUnauthorized.set(true)
        _unauthorized.tryEmit(Unit)
    }

    fun consumePendingUnauthorized(): Boolean = pendingUnauthorized.getAndSet(false)

    fun emitRealtimeEventHint() {
        _realtimeEventHint.tryEmit(Unit)
    }

    fun emitTokenRefreshed(newToken: String) {
        _tokenRefreshed.tryEmit(newToken)
    }

    fun emitCallDismissed(notificationId: Int) {
        _callDismissed.tryEmit(notificationId)
    }
}
