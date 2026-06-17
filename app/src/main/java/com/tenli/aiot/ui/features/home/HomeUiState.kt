package com.tenli.aiot.ui.features.home

import androidx.compose.runtime.Immutable
import com.tenli.aiot.model.network.EventItem
import com.tenli.aiot.model.network.HomeGroupDisplay

@Immutable
data class HomeUiState(
    val userId: Int = -1,
    val homeName: String = "Nhà của tôi",
    val aiBoxCount: Int = 0,
    val memberCount: Int = 0,
    val scriptCount: Int = 0,
    val iotCount: Int = 0,

    val recentEvents: List<EventItem> = emptyList(),
    val isRecentEventLoading: Boolean = false,
    val selectedEvent: EventItem? = null,
    val isShowingDetail: Boolean = false,

    val displayGroups: List<HomeGroupDisplay> = emptyList(),
    val selectedGroup: HomeGroupDisplay? = null,

    val groupedEvents: Map<String, List<EventItem>> = emptyMap(),
    val isGroupEventLoading: Boolean = false,
    val currentDeviceIds: List<Int> = emptyList(),

    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)