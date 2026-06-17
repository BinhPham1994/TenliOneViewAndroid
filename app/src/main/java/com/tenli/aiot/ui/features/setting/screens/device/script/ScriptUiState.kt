package com.tenli.aiot.ui.features.setting.screens.device.script

import com.tenli.aiot.model.network.ScriptItem

data class ScriptUiState(
    val isLoading: Boolean = false,
    val scripts: List<ScriptItem> = emptyList(),
    val errorMessage: String? = null,
    val selectedScript: ScriptItem? = null,
    val isSaving: Boolean = false,
    val selectedDays: List<Int> = listOf(0, 1, 2, 3, 4, 5, 6),
    val lastUpdated: Long = 0L
)