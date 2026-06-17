package com.tenli.oneview.ui.features.setting.screens.device.zigbee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tenli.oneview.model.network.DeviceItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

class ZigbeeViewModel(private val device: DeviceItem) : ViewModel() {
    private val _uiState = MutableStateFlow(ZigbeeUiState(iots = device.extendedData?.iots ?: emptyList()))
    val uiState = _uiState.asStateFlow()

    private var mqttClient: MqttAsyncClient? = null
    private val gson = Gson()

    init {
        connectMqtt()
    }

    private fun connectMqtt() {
        val mqtt = device.mqttInfo ?: return
        if (!mqtt.enabled) return

        _uiState.update { it.copy(mqttStatus = MqttStatus.CONNECTING) }

        try {
            val clientId = "application.${device.uuid}.${System.currentTimeMillis()}"
            mqttClient = MqttAsyncClient(mqtt.uri, clientId, MemoryPersistence())

            val options = MqttConnectOptions().apply {
                userName = "application"
                password = mqtt.auth?.password?.toCharArray()
                isAutomaticReconnect = true
                isCleanSession = true
                connectionTimeout = 15
            }

            mqttClient?.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    _uiState.update { it.copy(mqttStatus = MqttStatus.CONNECTED) }
                    subscribeToDevices()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    _uiState.update { it.copy(mqttStatus = MqttStatus.ERROR, errorMessage = exception?.message) }
                }
            })

            mqttClient?.setCallback(object : MqttCallbackExtended {
                override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                    subscribeToDevices()
                }

                override fun connectionLost(cause: Throwable?) {
                    _uiState.update { it.copy(mqttStatus = MqttStatus.DISCONNECTED) }
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {}

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    val payload = message?.toString() ?: return
                    handleMqttMessage(topic, payload)
                }
            })

        } catch (e: Exception) {
            _uiState.update { it.copy(mqttStatus = MqttStatus.ERROR, errorMessage = e.message) }
        }
    }

    private fun subscribeToDevices() {
        val outTopic = device.mqttInfo?.bridge?.topics?.outTopic ?: return
        val bridgeDevicesTopic = "device/bridge/devices"
        val statusTopic = if (outTopic.endsWith("/")) "${outTopic}#" else "$outTopic/#"
        try {
            mqttClient?.subscribe(bridgeDevicesTopic, 1)
            mqttClient?.subscribe(statusTopic, 1)
        } catch (e: Exception) {
        }
    }

    private fun handleMqttMessage(topic: String?, payload: String) {
        viewModelScope.launch {
            try {
                if (topic == null) return@launch

                if (topic == "device/bridge/devices") {
                    val listType = object : TypeToken<List<Map<String, Any>>>() {}.type
                    val mqttDevices: List<Map<String, Any>> = gson.fromJson(payload, listType)

                    _uiState.update { state ->
                        val updatedIots = state.iots.map { iot ->
                            val found = mqttDevices.find { it["ieee_address"] == iot.ieeeAddress }
                            if (found != null) {
                                val definition = found["definition"] as? Map<*, *>
                                val modelFromDef = definition?.get("model")?.toString()
                                val modelIdAtRoot = found["model_id"]?.toString()
                                val finalModelId = modelFromDef ?: modelIdAtRoot ?: ""
                                val newExposes = iot.exposes.toMutableMap()
                                if (finalModelId.isNotEmpty()) {
                                    newExposes["model_id"] = finalModelId
                                }
                                val isInterviewDone = found["interview_completed"] as? Boolean ?: false
                                newExposes["availability"] = if (isInterviewDone) "online" else "offline"

                                iot.copy(exposes = newExposes)
                            } else iot
                        }
                        state.copy(iots = updatedIots)
                    }
                    return@launch
                }
                val outTopicBase = device.mqttInfo?.bridge?.topics?.outTopic ?: ""
                if (topic.startsWith(outTopicBase)) {
                    val relativePath = topic.removePrefix(outTopicBase).trim('/')
                    val ieeeAddress = relativePath.split("/").firstOrNull() ?: return@launch
                    if (ieeeAddress == "bridge" || ieeeAddress == "config") return@launch
                    val newData: Map<String, String> = if (payload.trim().startsWith("{")) {
                        val type = object : TypeToken<Map<String, String>>() {}.type
                        gson.fromJson(payload, type)
                    } else {
                        mapOf("availability" to payload.trim())
                    }

                    _uiState.update { state ->
                        val updatedIots = state.iots.map { iot ->
                            if (iot.ieeeAddress == ieeeAddress) {
                                iot.copy(exposes = iot.exposes + newData)
                            } else iot
                        }
                        state.copy(iots = updatedIots)
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    fun controlDevice(ieeeAddress: String, key: String, value: String) {
        val baseInTopic = device.mqttInfo?.bridge?.topics?.inTopic ?: return
        val fullInTopic = "${baseInTopic}${ieeeAddress}/set"
        val command = gson.toJson(mapOf(key to value))
        try {
            mqttClient?.publish(fullInTopic, MqttMessage(command.toByteArray()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCleared() {
        mqttClient?.disconnect()
        super.onCleared()
    }
}

class ZigbeeViewModelFactory(private val device: DeviceItem) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ZigbeeViewModel(device) as T
    }
}