package com.tenli.oneview.ui.features.monitor

import com.tenli.oneview.model.network.HomeGroupDisplay
import com.tenli.oneview.model.network.MonitorDisplayItem

data class MonitorUiState(
    val homeName: String = "Chọn nhà",
    val displayGroups: List<HomeGroupDisplay> = emptyList(),
    val selectedGroup: HomeGroupDisplay? = null,
    val monitors: List<MonitorDisplayItem> = emptyList(),
    val groupedMonitors: Map<String, List<MonitorDisplayItem>> = emptyMap(), // Gom nhóm tại đây
    val isRefreshing: Boolean = false,
    val currentLevel: Int = 0,
    val selectedMonitor: MonitorDisplayItem? = null,
    val monitorUpdateTicket: Int = 0
)