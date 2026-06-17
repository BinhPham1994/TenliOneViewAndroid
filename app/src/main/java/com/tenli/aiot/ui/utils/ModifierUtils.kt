package com.tenli.aiot.ui.utils

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics

/**
 * Hiệu ứng nhún (bounce) phản hồi TỨC THÌ, loại bỏ hoàn toàn độ trễ của scroll view.
 */
fun Modifier.bounceClick(
    scaleDown: Float = 0.92f, // Độ lún của nút, bạn có thể chỉnh cho vừa mắt
    onClick: () -> Unit
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    // Animation làm mịn chuyển động lún xuống và nảy lên
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        label = "BounceClickAnimation"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .semantics {
            role = Role.Button // Hỗ trợ cho người khiếm thị (TalkBack)
        }
        .pointerInput(Unit) {
            awaitEachGesture {
                // 1. Bắt SÓNG ngay khoảnh khắc ngón tay chạm màn hình (Không có độ trễ)
                val down = awaitFirstDown(requireUnconsumed = false)
                isPressed = true

                // 2. Chờ người dùng nhấc ngón tay lên hoặc trượt đi chỗ khác
                val up = waitForUpOrCancellation()
                isPressed = false

                // 3. Nếu nhấc ngón tay lên hợp lệ -> Gọi hàm onClick
                if (up != null) {
                    onClick()
                }
            }
        }
}