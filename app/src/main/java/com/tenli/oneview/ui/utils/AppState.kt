package com.tenli.oneview.ui.utils

object AppState {
    /** true khi màn hình cuộc gọi cảnh báo (CallComingActivity) đang hiển thị */
    @Volatile
    var isShowCallActivity: Boolean = false
}
