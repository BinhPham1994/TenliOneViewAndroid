package com.tenli.aiot.ui.features.setting.screens.device.script

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tenli.aiot.R
import com.tenli.aiot.data.repository.BoxRepository
import com.tenli.aiot.data.repository.DataRepository
import com.tenli.aiot.model.network.DeviceItem
import com.tenli.aiot.model.network.MonitorDisplayItem
import com.tenli.aiot.model.network.ScriptItem
import com.tenli.aiot.ui.features.setting.core.SettingScreenType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ScriptViewModel(
    application: Application,
    private val boxRepository: BoxRepository,
    val device: DeviceItem,
    val onShowSnackbar: (String) -> Unit,
    val onNavigateTo: (SettingScreenType, String) -> Unit
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ScriptUiState())
    val uiState: StateFlow<ScriptUiState> = _uiState.asStateFlow()
    private val repository = DataRepository
    var selectedScript by mutableStateOf<ScriptItem?>(null)

    fun navigateToEditScript(script: ScriptItem) {
        selectedScript = script
        onNavigateTo(SettingScreenType.EditScript, "Chỉnh sửa kịch bản")
    }

    init {
        val cachedScripts = repository.scriptMap[device.id]
        if (!cachedScripts.isNullOrEmpty()) {
            _uiState.update { it.copy(scripts = cachedScripts) }
            fetchScripts(showLoading = false)
        } else {
            fetchScripts(showLoading = true)
        }
        checkAndFetchMonitors()
    }

    private fun checkAndFetchMonitors() {
        val cachedMonitors = repository.monitorMap[device.id]
        if (cachedMonitors.isNullOrEmpty()) {
            fetchMonitors()
        }
    }

    fun fetchMonitors() {
        viewModelScope.launch {
            try {
                val result = boxRepository.getMonitors(device.publicTargetURI, device.key)
                if (result.isSuccess) {
                    val rawMonitors = result.getOrNull() ?: emptyList()
                    val displayItems = rawMonitors.map {
                        MonitorDisplayItem(it, device.publicTargetURI, device.key, device.id)
                    }
                    repository.monitorMap[device.id] = displayItems
                    repository.persist()
                    _uiState.update { it.copy(lastUpdated = System.currentTimeMillis()) }
                }
            } catch (e: Exception) {
                onShowSnackbar(getApplication<android.app.Application>().getString(R.string.msg_cannot_load_ai_processes)) // Dùng onShowSnackbar [cite: 2026-03-16]
            }
        }
    }

    fun saveSecurityScript(
        name: String, enabled: Boolean, startTime: String, endTime: String,
        days: List<Int>, monitorIds: List<Int>, resetTime: Int, onSuccess: () -> Unit
    ) {
        if (!validateInput(name, monitorIds)) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val scriptJson = buildScriptJson(name, enabled, startTime, endTime, days, monitorIds, resetTime)
                val result = boxRepository.addScript(device.publicTargetURI, device.key, scriptJson)
                if (result.isSuccess) {
                    onShowSnackbar(getApplication<android.app.Application>().getString(R.string.msg_create_script_success))
                    fetchScripts()
                    onSuccess()
                } else {
                    onShowSnackbar("${getApplication<android.app.Application>().getString(R.string.msg_error_prefix)}: ${result.exceptionOrNull()?.message ?: "Unknown error"}")
                }
            } catch (e: Exception) {
                onShowSnackbar(getApplication<android.app.Application>().getString(R.string.msg_connection_error_box_ai))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun fetchScripts(showLoading: Boolean = true) {
        if (showLoading) _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val result = boxRepository.getScripts(device.publicTargetURI, device.key)
                if (result.isSuccess) {
                    val freshScripts = result.getOrNull() ?: emptyList()
                    _uiState.update { it.copy(scripts = freshScripts, isLoading = false) }
                    repository.scriptMap[device.id] = freshScripts
                    repository.persist()
                } else {
                    onShowSnackbar(getApplication<android.app.Application>().getString(R.string.msg_cannot_load_scripts))
                }
            } catch (e: Exception) {
                onShowSnackbar(getApplication<android.app.Application>().getString(R.string.msg_network_error_load_scripts))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun editSecurityScript(
        scriptId: String, name: String, enabled: Boolean, startTime: String, endTime: String,
        days: List<Int>, monitorIds: List<Int>, resetTime: Int, onSuccess: () -> Unit
    ) {
        if (!validateInput(name, monitorIds)) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val scriptJson = buildScriptJson(name, enabled, startTime, endTime, days, monitorIds, resetTime)
                val result = boxRepository.editScript(device.publicTargetURI, device.key, scriptId, scriptJson)
                if (result.isSuccess) {
                    onShowSnackbar(getApplication<android.app.Application>().getString(R.string.msg_update_script_success))
                    fetchScripts()
                    onSuccess()
                } else {
                    onShowSnackbar("${getApplication<android.app.Application>().getString(R.string.msg_update_failed)}: ${result.exceptionOrNull()?.message ?: "Unknown error"}")
                }
            } catch (e: Exception) {
                onShowSnackbar("${getApplication<android.app.Application>().getString(R.string.msg_connection_error)}: ${e.message}")
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun validateInput(name: String, monitorIds: List<Int>): Boolean {
        if (name.trim().isEmpty()) {
            onShowSnackbar(getApplication<android.app.Application>().getString(R.string.msg_require_script_name))
            return false
        }
        if (monitorIds.isEmpty()) {
            onShowSnackbar(getApplication<android.app.Application>().getString(R.string.msg_require_ai_process))
            return false
        }
        return true
    }

    fun deleteScript(scriptId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = boxRepository.deleteScript(device.publicTargetURI, device.key, scriptId)
                if (result.isSuccess) {
                    onShowSnackbar(getApplication<android.app.Application>().getString(R.string.msg_delete_script_success))
                    fetchScripts()
                    onSuccess()
                } else {
                    onShowSnackbar(getApplication<android.app.Application>().getString(R.string.msg_delete_script_failed))
                }
            } catch (e: Exception) {
                onShowSnackbar(getApplication<android.app.Application>().getString(R.string.msg_connection_error))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun buildScriptJson(
        name: String, enabled: Boolean, startTime: String, endTime: String,
        days: List<Int>, monitorIds: List<Int>, resetTime: Int
    ): com.tenli.aiot.model.network.ScriptJsonRequest {
        val startParts = startTime.split(":")
        val endParts = endTime.split(":")
        
        val activateDetails = com.tenli.aiot.model.network.ScriptTimeDetails(
            hour = startParts.getOrNull(0)?.toIntOrNull() ?: 0,
            minute = startParts.getOrNull(1)?.toIntOrNull() ?: 0,
            second = 0
        )
        
        val deactivateDetails = com.tenli.aiot.model.network.ScriptTimeDetails(
            hour = endParts.getOrNull(0)?.toIntOrNull() ?: 0,
            minute = endParts.getOrNull(1)?.toIntOrNull() ?: 0,
            second = 0
        )
        
        val actionItem = com.tenli.aiot.model.network.ScriptActionItem(
            type = "monitor-security",
            enabled = true,
            monitorIds = monitorIds,
            delay = 0,
            data = emptyMap(),
            options = emptyMap()
        )
        
        return com.tenli.aiot.model.network.ScriptJsonRequest(
            name = name.trim(),
            enabled = enabled,
            monitorIds = null,
            resetModeAfterTime = resetTime,
            time = com.tenli.aiot.model.network.ScriptTimeConfig(
                repeat = "week",
                dayOfWeeks = days,
                activate = activateDetails,
                deactivate = deactivateDetails
            ),
            actions = com.tenli.aiot.model.network.ScriptActionsConfig(
                activate = listOf(actionItem),
                deactivate = listOf(actionItem)
            )
        )
    }

    class Factory(
        private val application: Application,
        private val boxRepository: BoxRepository,
        private val device: DeviceItem,
        private val onShowSnackbar: (String) -> Unit,
        private val onNavigateTo: (SettingScreenType, String) -> Unit
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ScriptViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ScriptViewModel(application, boxRepository, device, onShowSnackbar, onNavigateTo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}