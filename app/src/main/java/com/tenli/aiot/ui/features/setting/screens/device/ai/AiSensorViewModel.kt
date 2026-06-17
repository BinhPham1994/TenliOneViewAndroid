package com.tenli.aiot.ui.features.setting.screens.device.ai

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tenli.aiot.data.repository.BoxRepository
import com.tenli.aiot.model.network.CameraItem
import com.tenli.aiot.model.network.DeviceItem
import com.tenli.aiot.model.network.MonitorDisplayItem
import com.tenli.aiot.model.network.MonitorType
import com.tenli.aiot.ui.features.setting.core.SettingScreenType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AiSensorViewModel(
    private val boxRepository: BoxRepository,
    private val device: DeviceItem,
    val onNavigateBack: () -> Unit,
    val onNavigateTo: (SettingScreenType, String) -> Unit,
    val onShowSnackbar: (String) -> Unit
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiSensorState())
    val uiState = _uiState.asStateFlow()

    

    fun fetchMonitorTypes() {
        _uiState.update {
            it.copy(
                isLoading = true,
                monitorTypes = emptyList()
            )
        }
        viewModelScope.launch {
            try {
                val result = boxRepository.getMonitorType(device.publicTargetURI, device.key)
                if (result.isSuccess) {
                    _uiState.update {
                        it.copy(
                            monitorTypes = result.getOrNull() ?: emptyList(),
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                    onShowSnackbar("Không thể lấy danh sách bài AI từ Box")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                onShowSnackbar("Box đang Offline hoặc lỗi kết nối")
            }
        }
    }

    fun setSelectedMonitorType(item: MonitorType) {
        _uiState.update {
            it.copy(selectedMonitorType = item)
        }
        loadDefaultParams(item.type)
    }

    fun fetchCameras() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val result = boxRepository.getCameras(device.publicTargetURI, device.key)
                if (result.isSuccess) {
                    val cameraList = result.getOrNull() ?: emptyList()
                    _uiState.update { state ->
                        val autoSelectedCam = if (state.editingCameraId != null) {
                            cameraList.find { it.id.toString() == state.editingCameraId.toString() }
                        } else null

                        state.copy(
                            cameras = cameraList,
                            isLoading = false,
                            selectedCamera = state.selectedCamera ?: autoSelectedCam
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun nextWizardStep() {
        _uiState.update { it.copy(currentWizardStep = it.currentWizardStep + 1) }
    }

    fun previousWizardStep() {
        val currentStep = uiState.value.currentWizardStep
        if (currentStep > 1) {
            _uiState.update {
                it.copy(currentWizardStep = currentStep - 1)
            }
        } else {
            onNavigateBack()
        }
    }

    fun selectCamera(camera: CameraItem) {
        _uiState.update { it.copy(selectedCamera = camera) }
    }

    fun addAiPoint(offset: Offset, size: IntSize) {
        if (size.width <= 0 || size.height <= 0) return

        val normalizedX = offset.x / size.width.toFloat()
        val normalizedY = offset.y / size.height.toFloat()

        _uiState.update {
            it.copy(
                currentPoints = it.currentPoints + Offset(normalizedX, normalizedY)
            )
        }
    }

    fun finishAiZone(isInclude: Boolean) {
        val box = uiState.value
        if (box.currentPoints.size < 3) {
            onShowSnackbar("Cần ít nhất 3 điểm để tạo vùng")
            return
        }

        val flattened = box.currentPoints.flatMap {
            listOf(it.x.toDouble(), it.y.toDouble())
        }

        _uiState.update { currentBox ->
            currentBox.copy(
                currentPoints = emptyList(),
                includeMasks = if (isInclude) currentBox.includeMasks + listOf(flattened) else currentBox.includeMasks,
                excludeMasks = if (!isInclude) currentBox.excludeMasks + listOf(flattened) else currentBox.excludeMasks
            )
        }
    }

    fun clearAiZones(isInclude: Boolean) {
        _uiState.update { currentBox ->
            currentBox.copy(
                currentPoints = emptyList(),
                includeMasks = if (isInclude) emptyList() else currentBox.includeMasks,
                excludeMasks = if (!isInclude) emptyList() else currentBox.excludeMasks
            )
        }
    }

    fun resetCurrentDrawing() {
        _uiState.update {
            it.copy(currentPoints = emptyList())
        }
    }

    fun updateAiEnabled(enabled: Boolean) {
        _uiState.update { it.copy(aiEnabled = enabled) }
    }

    fun updateMqttEnabled(enabled: Boolean) {
        _uiState.update { it.copy(mqttEnabled = enabled) }
    }

    fun handleNextWithValidation(isInclude: Boolean) {
        val box = uiState.value
        val pointsCount = box.currentPoints.size

        when {
            pointsCount == 0 -> nextWizardStep()
            pointsCount >= 3 -> {
                finishAiZone(isInclude)
                nextWizardStep()
            }

            else -> {
                _uiState.update { it.copy(showIncompleteZoneConfirm = true) }
            }
        }
    }

    fun handleBackWithValidation() {
        val state = uiState.value
        val currentStep = state.currentWizardStep
        val isSensor = state.selectedMonitorType?.type?.contains("sensor", ignoreCase = true) == true
        if (currentStep == 2 && isSensor) {
            _uiState.update { it.copy(showBackConfirm = true) }
        } else {
            previousWizardStep()
        }
    }

    fun confirmResetAndBack() {
        _uiState.update {
            it.copy(
                includeMasks = emptyList(),
                excludeMasks = emptyList(),
                currentPoints = emptyList(),
                selectedInputSensorIds = emptySet(),
                showBackConfirm = false,
                currentWizardStep = 1
            )
        }
    }

    fun dismissDialogs() {
        _uiState.update { it.copy(showBackConfirm = false, showIncompleteZoneConfirm = false) }
    }

    fun saveSensorMonitor(onSuccess: () -> Unit) {
        val boxState = uiState.value
        val selectedCamera = boxState.selectedCamera ?: return
        val monitorType = boxState.selectedMonitorType?.type ?: ""
        val isEdit = boxState.editingMonitorId != null

        val paramMap = boxState.dynamicParams.toMutableMap().apply {
            this["includeMasks"] = boxState.includeMasks
            this["excludeMasks"] = boxState.excludeMasks
            val hasPostUrl = !device.postURL.isNullOrBlank()
            this["enablePostEvent"] = hasPostUrl
            this["postUrl"] = device.postURL ?: ""
            this["postHeaders"] = device.postHeaders?.map { "${it.key}: ${it.value}" } ?: emptyList<String>()
            this["mqttEnable"] = boxState.mqttEnabled
            this["mqttTopic"] = boxState.mqttTopic
            this["enableAlarm"] = boxState.alarmEnabled
            this["activateScore"] = boxState.sensitivity * 8.0
        }

        val request = com.tenli.aiot.model.network.AddMonitorRequest(
            id = if (isEdit) boxState.editingMonitorId ?: 0 else 0,
            name = boxState.monitorName,
            enabled = boxState.aiEnabled,
            cameraId = selectedCamera.id,
            type = monitorType,
            param = paramMap as Map<String, Any>
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = if (isEdit) {
                    boxRepository.updateMonitor(device.publicTargetURI, device.key, boxState.editingMonitorId!!, request)
                } else {
                    boxRepository.addMonitor(device.publicTargetURI, device.key, request)
                }

                if (result.isSuccess) {
                    onSuccess()
                } else {
                    onShowSnackbar("Lưu thất bại: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                onShowSnackbar("Lỗi kết nối: ${e.message}")
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateMonitorName(name: String) {
        _uiState.update { it.copy(monitorName = name) }
    }

    fun updateMqttTopic(topic: String) {
        _uiState.update { it.copy(mqttTopic = topic) }
    }

    fun loadDefaultParams(monitorType: String) {
        viewModelScope.launch {
            try {
                val result = boxRepository.getParamDefault(device.publicTargetURI, device.key, monitorType)
                if (result.isSuccess) {
                    val params = result.getOrNull() as? Map<String, Any?> ?: emptyMap()
                    _uiState.update { state ->
                        state.copy(
                            dynamicParams = params,
                            monitorName = state.monitorName.ifBlank { params["name"]?.toString() ?: "" },
                            mqttTopic = params["mqttTopic"]?.toString() ?: state.mqttTopic
                        )
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    fun updateAlarmEnabled(enabled: Boolean) {
        _uiState.update { it.copy(alarmEnabled = enabled) }
    }

    fun updateAiSensitivity(level: Int) {
        _uiState.update { it.copy(sensitivity = level) }
    }

    private fun parseMasks(raw: Any?): List<List<Double>> {
        val list = raw as? List<*> ?: return emptyList()
        return list.mapNotNull { inner ->
            (inner as? List<*>)?.mapNotNull { (it as? Number)?.toDouble() }
        }
    }

    fun setupEditMonitor(item: MonitorDisplayItem) {
        val monitor = item.monitor
        val type = monitor.type ?: ""
        val params = monitor.param ?: emptyMap()
        val savedIds = extractMonitorIds(params)

        _uiState.update { state ->
            val typeObj = state.monitorTypes.find { it.type == type } ?: MonitorType(type = type, name = "")
            state.copy(
                isLoading = true,
                currentWizardStep = 1,
                selectedMonitorType = typeObj,
                selectedCamera = null,
                editingMonitorId = monitor.id,
                editingCameraId = monitor.cameraId,
                monitorName = monitor.name ?: "",
                aiEnabled = monitor.enabled,
                selectedInputSensorIds = savedIds,

                includeMasks = parseMasks(params["includeMasks"]),
                excludeMasks = parseMasks(params["excludeMasks"]),

                alarmEnabled = params["enableAlarm"] as? Boolean ?: false,
                mqttEnabled = params["mqttEnable"] as? Boolean ?: true,
                mqttTopic = params["mqttTopic"]?.toString() ?: "",
                sensitivity = (((params["activateScore"] as? Double ?: 0.0) / 8.0).toInt()).coerceIn(1, 5),
                dynamicParams = params
            )
        }

        fetchCameras()
        fetchMonitorTypes()
        if (type.contains("logic", ignoreCase = true)) {
            fetchSensorsForLogic(type)
        }
    }

    fun prepareAddNewMonitor(categoryIndex: Int) {
        _uiState.update { state ->
            state.copy(
                selectedCamera = null,
                editingMonitorId = null,
                editingCameraId = null,
                selectedAiCategory = categoryIndex,
                currentWizardStep = 1,
                monitorName = "",
                selectedInputSensorIds = emptySet(),
                availableInputSensors = emptyList()
            )
        }
    }

    fun askDeleteMonitor() {
        _uiState.update { it.copy(showDeleteConfirm = true) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(showDeleteConfirm = false) }
    }

    fun confirmDeleteMonitor(onSuccess: () -> Unit) { // Thêm tham số này [cite: 2026-03-08]
        val monitorId = uiState.value.editingMonitorId ?: return
        dismissDeleteDialog()

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = boxRepository.deleteMonitor(device.publicTargetURI, device.key, monitorId)
                if (result.isSuccess) {
                    onShowSnackbar("Đã xóa bài AI thành công")
                    onSuccess()
                } else {
                    onShowSnackbar("Xóa thất bại: ${result.exceptionOrNull()?.message}")
                }
            } catch (_: Exception) {
                onShowSnackbar("Lỗi kết nối khi xóa")
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun fetchSensorsForLogic(explicitType: String? = null) {
        val device = device
        val logicType = explicitType ?: uiState.value.selectedMonitorType?.type ?: ""
        val allowedSensorTypes = getCompatibleSensorTypes(logicType)
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = boxRepository.getMonitors(device.publicTargetURI, device.key)
                if (result.isSuccess) {
                    val allMonitors = result.getOrNull() ?: emptyList()
                    val compatibleSensors = allMonitors.filter { monitor ->
                        allowedSensorTypes.contains(monitor.type)
                    }.map {
                        MonitorDisplayItem(it, device.publicTargetURI, device.key, device.id)
                    }
                    _uiState.update {
                        it.copy(availableInputSensors = compatibleSensors, isLoading = false)
                    }
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun toggleInputSensor(monitorId: Int) {
        _uiState.update { state ->
            val current = state.selectedInputSensorIds
            val next = if (current.contains(monitorId)) current - monitorId else current + monitorId
            state.copy(selectedInputSensorIds = next)
        }
    }

    private fun extractMonitorIds(params: Map<String, Any?>?): Set<Int> {
        if (params == null) return emptySet()
        val possibleKeys = listOf("inputSensor", "inputSources", "sensors",  "monitors")
        for (key in possibleKeys) {
            val list = params[key] as? List<*>
            if (!list.isNullOrEmpty()) {
                return list.mapNotNull { item ->
                    when (item) {
                        is Map<*, *> -> {
                            val idValue = item["monitorId"] ?: item["id"]
                            (idValue as? Number)?.toInt()
                        }

                        is Number -> item.toInt()
                        else -> null
                    }
                }.toSet()
            }
        }
        return emptySet()
    }

    private fun getCompatibleSensorTypes(logicType: String): List<String> {
        return when (logicType) {
            "logic-person" -> listOf("sensor-person-camera")
            "logic-fall-person" -> listOf("sensor-fall-person-camera")
            "logic-fire" -> listOf(
                "sensor-fire-camera",
                "sensor-smoke-camera",
                "sensor-mqtt-thermo",
                "sensor-mqtt-smoke",
                "sensor-mqtt-gas"
            )
            "logic-person-pond" -> listOf("sensor-person-pond-camera")
            "logic-fish-out" -> listOf("sensor-fish-out-camera")
            else -> emptyList()
        }
    }

    fun saveLogicMonitor(onSuccess: () -> Unit) {
        val boxState = uiState.value
        val finalCameraId = boxState.selectedCamera?.id ?: boxState.editingCameraId ?: return
        val monitorType = boxState.selectedMonitorType?.type ?: ""
        val isEdit = boxState.editingMonitorId != null

        val packedSensors = boxState.selectedInputSensorIds.map { id ->
            mapOf("monitorId" to id)
        }

        val paramMap = boxState.dynamicParams.toMutableMap().apply {
            when (monitorType) {
                "logic-fire" -> {
                    this["inputSources"] = packedSensors
                }

                "logic-person", "logic-fish-out", "logic-person-pond" -> {
                    this["inputSensor"] = packedSensors
                }

                "logic-fall-person" -> {
                    this["sensors"] = packedSensors
                }

                else -> {
                    this["sensors"] = packedSensors
                    this["inputSensor"] = packedSensors
                    this["inputSources"] = packedSensors
                }
            }

            val hasPostUrl = !device.postURL.isNullOrBlank()
            this["enablePostEvent"] = hasPostUrl
            this["postUrl"] = device.postURL ?: ""
            this["postHeaders"] = device.postHeaders?.map { "${it.key}: ${it.value}" } ?: emptyList<String>()

            this["enableAlarm"] = boxState.alarmEnabled
            this["mqttEnable"] = boxState.mqttEnabled
            this["mqttTopic"] = boxState.mqttTopic
        }
        
        val request = com.tenli.aiot.model.network.AddMonitorRequest(
            id = if (isEdit) boxState.editingMonitorId ?: 0 else 0,
            name = boxState.monitorName,
            enabled = boxState.aiEnabled,
            cameraId = finalCameraId,
            type = monitorType,
            param = paramMap as Map<String, Any>
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = if (isEdit) {
                    boxRepository.updateMonitor(device.publicTargetURI, device.key, boxState.editingMonitorId!!, request)
                } else {
                    boxRepository.addMonitor(device.publicTargetURI, device.key, request)
                }

                if (result.isSuccess) {
                    onShowSnackbar(if (isEdit) "Cập nhật bài Logic thành công" else "Thêm bài Logic thành công")
                    onSuccess()
                } else {
                    val errorBody = result.exceptionOrNull()?.message ?: "Lỗi không xác định"
                    onShowSnackbar("Lỗi lưu bài Logic: $errorBody")
                }
            } catch (e: Exception) {
                onShowSnackbar("Lỗi kết nối đến Box AI")
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}

class AiSensorViewModelFactory(
    private val boxRepository: BoxRepository,
    private val device: DeviceItem,
    private val initialEditItem: MonitorDisplayItem? = null,
    private val onShowSnackbar: (String) -> Unit,
    private val onNavigateBack: () -> Unit,
    private val onNavigateTo: (SettingScreenType, String) -> Unit
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AiSensorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AiSensorViewModel(
                boxRepository = boxRepository,
                device = device,
                onNavigateBack = onNavigateBack,
                onNavigateTo = onNavigateTo,
                onShowSnackbar = onShowSnackbar
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}