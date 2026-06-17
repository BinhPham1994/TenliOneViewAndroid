package com.tenli.aiot.ui.features.setting.screens.device.ai

import androidx.compose.ui.geometry.Offset
import com.tenli.aiot.model.network.CameraItem
import com.tenli.aiot.model.network.MonitorDisplayItem
import com.tenli.aiot.model.network.MonitorType

data class SensitivityLevel(
    val label: String,
    val value: Int
)

data class AiSensorState(
    val isLoading: Boolean = false,
    val monitorTypes: List<MonitorType> = emptyList(),
    val selectedAiCategory: Int = 0,
    val selectedMonitorType: MonitorType? = null,

    val cameras: List<CameraItem> = emptyList(),
    val selectedCamera: CameraItem? = null,
    val currentWizardStep: Int = 1,
    val aiStatus: Boolean = true,
    val includeMasks: List<List<Double>> = emptyList(),
    val excludeMasks: List<List<Double>> = emptyList(),
    val currentPoints: List<Offset> = emptyList(),
    val aiEnabled: Boolean = true,
    val alarmEnabled: Boolean = false,
    val sensitivity: Int = 3, // Giờ dùng thang 1-5 cho đồng bộ với UI [cite: 2026-03-08]
    val mqttEnabled: Boolean = true,
    val showBackConfirm: Boolean = false,
    val showIncompleteZoneConfirm: Boolean = false,
    val monitorName: String = "",
    val mqttTopic: String = "aibox/person",
    val dynamicParams: Map<String, Any?> = emptyMap(),
    val isLoadingParams: Boolean = false,
    val editingMonitorId: Int? = null,
    val editingCameraId: Int? = null,
    val showDeleteConfirm: Boolean = false,
    val availableInputSensors: List<MonitorDisplayItem> = emptyList(),
    val selectedInputSensorIds: Set<Int> = emptySet(),
)

val sensitivityLevels = listOf(
    SensitivityLevel("Rất thấp", 1),
    SensitivityLevel("Thấp", 2),
    SensitivityLevel("Trung bình", 3),
    SensitivityLevel("Cao", 4),
    SensitivityLevel("Rất cao", 5)
)

fun getSensitivityLabel(value: Int): String {
    return sensitivityLevels.find { it.value == value }?.label ?: "Trung bình"
}