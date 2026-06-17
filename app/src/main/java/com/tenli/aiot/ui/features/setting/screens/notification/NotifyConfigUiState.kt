package com.tenli.aiot.ui.features.setting.screens.notification

import com.tenli.aiot.model.network.EventTypeGroup
import com.tenli.aiot.model.network.EventTypeGroupDetail
import com.tenli.aiot.ui.features.setting.core.SettingScreenType

data class NotifyConfigUiState(
    val isLoading: Boolean = false,
    val masterEnabled: Boolean = false,
    val groups: List<EventTypeGroup> = emptyList(),
    val errorMessage: String? = null,
    val currentScreen: SettingScreenType = SettingScreenType.Notify,
    val screenTitle: String = "Cấu hình cảnh báo",

    val selectedGroupDetail: EventTypeGroupDetail? = null,
    val isDetailLoading: Boolean = false,

    // --- THÊM 3 DÒNG NÀY ---
    val showModeSheet: Boolean = false,
    val selectedTypeKey: String? = null,
    val currentMode: String? = null
)