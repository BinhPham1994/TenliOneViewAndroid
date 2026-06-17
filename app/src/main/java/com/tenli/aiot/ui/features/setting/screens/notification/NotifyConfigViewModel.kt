package com.tenli.aiot.ui.features.setting.screens.notification

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tenli.aiot.R
import com.tenli.aiot.data.repository.AlarmRepository
import com.tenli.aiot.data.repository.DataRepository
import com.tenli.aiot.ui.features.setting.core.SettingScreenType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotifyConfigViewModel(
    private val alarmRepository: AlarmRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotifyConfigUiState())
    val uiState = _uiState.asStateFlow()
    private val repository = DataRepository
    private val displayOrder = listOf(
        "user-info",
        "device-info",
        "pond-abnormal",
        "fire-smoke",
        "human-monitor",
        "power-network",
        "sensor",
        "other"
    )

    init {
        fetchSettings()
    }

    fun fetchSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = alarmRepository.getNotifySetting()
                if (result.isSuccess) {
                    val body = result.getOrNull()
                    if (body != null) {
                        // Thực hiện sắp xếp danh sách theo displayOrder [cite: 2026-03-14]
                        val sortedGroups = body.eventTypeGroups.sortedBy { group ->
                            val index = displayOrder.indexOf(group.key)
                            if (index != -1) index else displayOrder.size // Nếu key lạ thì cho xuống cuối
                        }

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                masterEnabled = body.notification.enabled,
                                groups = sortedGroups // Lưu danh sách đã sắp xếp vào State
                            )
                        }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Lỗi kết nối") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun getDisplayInfo(key: String): Triple<String, Int, Color> {
        val def = repository.eventGroupDefs.find { it.key == key }
        val title = def?.display?.getLocalText() ?: key
        return when (key) {
            "user-info" -> Triple(title, R.drawable.member, Color(0xFF3F51B5))
            "device-info" -> Triple(title, R.drawable.device_login, Color(0xFF2196F3))
            "pond-abnormal" -> Triple(title, R.drawable.fish_icon, Color(0xFFE91E63))
            "fire-smoke" -> Triple(title, R.drawable.device_icon, Color(0xFFF44336))
            "human-monitor" -> Triple(title, R.drawable.person_ai, Color(0xFFFF9800))
            "power-network" -> Triple(title, R.drawable.power_icon, Color(0xFFF44336))
            "sensor" -> Triple(title, R.drawable.setting, Color(0xFF673AB7))
            else -> Triple(title, R.drawable.setting, Color(0xFF3F51B5))
        }
    }

    fun fetchGroupDetail(groupKey: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDetailLoading = true) }
            try {
                val result = alarmRepository.getNotifySettingEventGroup(groupKey)

                if (result.isSuccess) {
                    // Cập nhật State từ đối tượng trả về
                    val detailData = result.getOrNull()
                    _uiState.update {
                        it.copy(
                            selectedGroupDetail = detailData,
                            isDetailLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isDetailLoading = false, errorMessage = "Lỗi API") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isDetailLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun onGroupClick(groupKey: String, navigate: (SettingScreenType, String) -> Unit) {
        fetchGroupDetail(groupKey) // Gọi API lấy chi tiết

        val def = repository.eventGroupDefs.find { it.key == groupKey }
        val title = def?.display?.getLocalText() ?: "Chi tiết"
        navigate(SettingScreenType.NotifyDetail, title)
    }

    // Hàm bổ trợ để dịch Mode cảnh báo sang tiếng Việt như trong ảnh
    fun getAlarmModeText(mode: String?): String {
        return when (mode) {
            "push" -> "Gửi thông báo"
            "call" -> "Cuộc gọi qua internet"
            "none" -> "Không cảnh báo"
            else -> "Mặc định"
        }
    }

    fun openModeSelection(typeKey: String, currentMode: String?) {
        _uiState.update { it.copy(showModeSheet = true, selectedTypeKey = typeKey, currentMode = currentMode) }
    }

    fun closeModeSelection() {
        _uiState.update { it.copy(showModeSheet = false) }
    }

    fun getTypeIcon(key: String): Int {
        return when {
            key.contains("person") -> R.drawable.person_ai
            key.contains("fish") -> R.drawable.fish_icon
            key.contains("fire") || key.contains("smoke") -> R.drawable.smoke_ai
            key.contains("face") -> R.drawable.face_ai
            key.contains("security") -> R.drawable.password
            key.contains("power") -> R.drawable.power_icon
            else -> R.drawable.ai_icon
        }
    }

    fun updateMasterNotify(enabled: Boolean) {
        val previousState = _uiState.value.masterEnabled
        _uiState.update { it.copy(masterEnabled = enabled) }
        viewModelScope.launch {
            try {
                val request = com.tenli.aiot.model.network.UpdateNotifySettingRequest(
                    notification = com.tenli.aiot.model.network.NotifyEnabledConfig(enabled = enabled)
                )
                val result = alarmRepository.updateUserNotifySetting(request)
                if (result.isFailure) {
                    _uiState.update { it.copy(masterEnabled = previousState) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(masterEnabled = previousState) }
            }
        }
    }

    fun updateGroupEnabled(groupKey: String?, enabled: Boolean) {
        if (groupKey == null) return

        // 1. Lưu lại trạng thái cũ để Rollback nếu lỗi
        val previousState = _uiState.value

        // 2. Cập nhật UI ngay lập tức (Optimistic Update)
        _uiState.update { state ->
            // Cập nhật danh sách groups ở màn hình Cha [cite: 2026-03-15]
            val updatedGroups = state.groups.map { group ->
                if (group.key == groupKey) {
                    group.copy(notification = group.notification.copy(enabled = enabled))
                } else {
                    group
                }
            }

            // Cập nhật detail ở màn hình Chi tiết
            val updatedDetail = state.selectedGroupDetail?.let {
                if (it.key == groupKey) {
                    it.copy(notification = it.notification.copy(enabled = enabled))
                } else it
            }

            state.copy(
                groups = updatedGroups,
                selectedGroupDetail = updatedDetail
            )
        }

        viewModelScope.launch {
            try {
                val request = com.tenli.aiot.model.network.UpdateNotifyGroupRequest(
                    key = groupKey,
                    notification = com.tenli.aiot.model.network.NotifyConfigPayload(enabled = enabled)
                )

                val result = alarmRepository.updateNotifySettingEventGroup(request)

                if (result.isFailure) {
                    // Rollback nếu API lỗi
                    _uiState.value = previousState
                }
            } catch (e: Exception) {
                // Rollback nếu mất mạng
                _uiState.value = previousState
            }
        }
    }

    fun updateAlarmMode(newMode: String) {
        val typeKey = _uiState.value.selectedTypeKey ?: return
        val groupKey = _uiState.value.selectedGroupDetail?.key ?: return

        viewModelScope.launch {
            try {
                // 1. Đóng BottomSheet ngay cho mượt UI
                closeModeSelection()

                val request = com.tenli.aiot.model.network.UpdateNotifyGroupRequest(
                    key = typeKey,
                    notification = com.tenli.aiot.model.network.NotifyConfigPayload(
                        enabled = newMode != "none",
                        alarmMode = com.tenli.aiot.model.network.AlarmModePayload(default = newMode)
                    )
                )

                val result = alarmRepository.updateEventType(request)

                if (result.isSuccess) {
                    // 3. Cập nhật thành công thì nạp lại dữ liệu chi tiết để UI đổi chữ "Gửi thông báo/Gọi điện"
                    fetchGroupDetail(groupKey)
                } else {
                    // Xử lý báo lỗi nếu cần
                }
            } catch (e: Exception) {
                // Xử lý lỗi kết nối
            }
        }
    }

    class Factory(
        private val alarmRepository: AlarmRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NotifyConfigViewModel::class.java)) {
                return NotifyConfigViewModel(alarmRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}