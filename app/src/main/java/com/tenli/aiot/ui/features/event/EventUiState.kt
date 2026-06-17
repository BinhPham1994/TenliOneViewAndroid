package com.tenli.aiot.ui.features.event

import androidx.compose.runtime.Immutable
import com.tenli.aiot.model.network.DeviceItem
import com.tenli.aiot.model.network.EventItem
import com.tenli.aiot.model.network.EventTypeDef

@Immutable
data class EventUiState(
    val events: List<EventItem> = emptyList(),
    val isEventFilter: Boolean = false,
    val isRefreshing: Boolean = false,
    val isPagingLoading: Boolean = false,
    val isDataEmpty: Boolean = false,
    val selectedEvent: EventItem? = null,
    val currentLevel: Int = 0, // 0: Danh sách, 1: Chi tiết
    val isEndReached: Boolean = false,

    val selectedDeviceIds: List<Int> = emptyList(),
    val selectedETypes: List<String> = emptyList(),
    val fromTime: String? = null,
    val toTime: String? = null,

    val availableDevices: List<DeviceItem> = emptyList(),
    val availableTypeGroups: Map<String, List<EventTypeDef>> = emptyMap()
)