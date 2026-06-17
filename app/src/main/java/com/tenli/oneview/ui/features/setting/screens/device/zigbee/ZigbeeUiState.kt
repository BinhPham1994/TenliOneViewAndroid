package com.tenli.oneview.ui.features.setting.screens.device.zigbee

import com.tenli.oneview.model.network.IotItem

enum class MqttStatus { CONNECTING, CONNECTED, DISCONNECTED, ERROR }

data class ZigbeeUiState(
    val iots: List<IotItem> = emptyList(),
    val isLoading: Boolean = false,
    val mqttStatus: MqttStatus = MqttStatus.DISCONNECTED,
    val errorMessage: String? = null
)