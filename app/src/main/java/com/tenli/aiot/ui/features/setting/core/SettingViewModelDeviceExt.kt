package com.tenli.aiot.ui.features.setting.core

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.tenli.aiot.R
import com.tenli.aiot.data.repository.DataRepository
import com.tenli.aiot.model.network.BoxSystemInfo
import com.tenli.aiot.model.network.DeviceItem
import com.tenli.aiot.model.network.LanDevice
import com.tenli.aiot.model.network.MonitorDisplayItem
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceListener

fun SettingViewModel.fetchDevices() {
    _uiState.update { it.copy(box = it.box.copy(isLoading = true)) }
    updateDeviceGroupUi(DataRepository.deviceList)
    viewModelScope.launch {
        try {
            val result = appRepository.getListDevice()
            if (result.isSuccess) {
                val freshDevices = result.getOrNull() ?: emptyList()
                DataRepository.deviceList = freshDevices
                DataRepository.persist()
                updateDeviceGroupUi(freshDevices)
            }
        } catch (_: Exception) {
        } finally {
            _uiState.update { it.copy(box = it.box.copy(isLoading = false)) }
        }
    }
}

private fun SettingViewModel.updateDeviceGroupUi(devices: List<DeviceItem>) {
    val allGroups = DataRepository.groupList
    val grouped = allGroups.map { groupDisplay ->
        DeviceGroupDisplay(
            groupName = groupDisplay.displayName,
            devices = devices.filter { it.userGroupId == groupDisplay.group.id }
        )
    }.filter { it.devices.isNotEmpty() }

    _uiState.update {
        it.copy(box = it.box.copy(deviceGroups = grouped))
    }
}

fun SettingViewModel.selectDevice(device: DeviceItem) {
    _uiState.update {
        it.copy(
            box = it.box.copy(
                selectedDeviceItem = device,
                monitorTypes = emptyList()
            )
        )
    }
    fetchDeviceDetail(device.id)
    navigateTo(SettingScreenType.DeviceDetail, device.name)
}

fun SettingViewModel.fetchDeviceDetail(deviceId: Int) {
    viewModelScope.launch {
        try {
            val result = appRepository.getDeviceDetail(deviceId)

            if (result.isSuccess) {
                val detailItem = result.getOrNull()
                if (detailItem != null) {
                    _uiState.update { state ->
                        state.copy(box = state.box.copy(selectedDeviceItem = detailItem))
                    }
                }
            }
        } catch (_: Exception) {
        }
    }
}

fun SettingViewModel.controlBoxSystem(command: String, successMessage: String) {
    val device = uiState.value.box.selectedDeviceItem ?: return

    _uiState.update { it.copy(box = it.box.copy(isLoading = true)) }

    viewModelScope.launch {
        try {
            // 2. Khởi tạo API với publicTargetURI và key của thiết bị [cite: 2026-03-10]
            val request = com.tenli.aiot.model.network.DeviceCommandRequest(command = command)
            val result = boxRepository.controlDevice(device.publicTargetURI, device.key, request)
            if (result.isSuccess) {
                showSnackbar(successMessage)
            } else {
                val errorMsg = when ((result.exceptionOrNull() as? retrofit2.HttpException)?.code()) {
                    401 -> "Sai mã bảo mật (Device Key)"
                    404 -> "Thiết bị không hỗ trợ lệnh này"
                    else -> "Lỗi hệ thống: ${(result.exceptionOrNull() as? retrofit2.HttpException)?.code()}"
                }
                showSnackbar(errorMsg)
            }
        } catch (e: Exception) {
            showSnackbar(getApplication<android.app.Application>().getString(R.string.msg_cannot_connect_device_network))
        } finally {
            _uiState.update { it.copy(box = it.box.copy(isLoading = false)) }
        }
    }
}

fun SettingViewModel.fetchBoxInfo() {
    val device = uiState.value.box.selectedDeviceItem ?: return
    _uiState.update {
        it.copy(
            box = it.box.copy(
                isLoading = true,
                state = null,
                info = null
            )
        )
    }
    viewModelScope.launch {
        try {
            val stateDeferred = async { boxRepository.getSystemState(device.publicTargetURI, device.key) }
            val settingDeferred = async { boxRepository.getDeviceInformation(device.publicTargetURI, device.key) }

            val stateRes = stateDeferred.await()
            val settingRes = settingDeferred.await()

            if (stateRes.isSuccess && settingRes.isSuccess) {
                _uiState.update {
                    it.copy(
                        box = it.box.copy(
                            state = stateRes.getOrNull(),
                            info = settingRes.getOrNull(),
                            isLoading = false
                        )
                    )
                }
            } else {
                _uiState.update { it.copy(box = it.box.copy(isLoading = false)) }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(box = it.box.copy(isLoading = false)) }
        }
    }
}

fun SettingViewModel.refreshSystemState() {
    val device = uiState.value.box.selectedDeviceItem ?: return
    viewModelScope.launch {
        try {
            val result = boxRepository.getSystemState(device.publicTargetURI, device.key)
            if (result.isSuccess) {
                _uiState.update { state ->
                    state.copy(box = state.box.copy(state = result.getOrNull()))
                }
            }
        } catch (_: Exception) {
        }
    }
}

fun SettingViewModel.fetchSystemSettings() {
    val device = uiState.value.box.selectedDeviceItem ?: return
    _uiState.update {
        it.copy(
            box = it.box.copy(
                isLoading = true,
                systemSetting = null
            )
        )
    }

    viewModelScope.launch {
        try {
            val result = boxRepository.getSystemSetting(device.publicTargetURI, device.key)

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        box = it.box.copy(
                            systemSetting = result.getOrNull(),
                            isLoading = false
                        )
                    )
                }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(box = it.box.copy(isLoading = false)) }
        }
    }
}

fun SettingViewModel.updateMqttConfig(
    enabled: Boolean, host: String, port: String, clientId: String,
    user: String, pass: String, topic: String, qos: String,
    onSuccess: () -> Unit
) {
    val device = uiState.value.box.selectedDeviceItem ?: return
    val fullResponse = uiState.value.box.systemSetting ?: return

    val newSettings = fullResponse.settings.copy(
        mqttEnabled = enabled,
        mqttHost = host,
        mqttPort = port.toIntOrNull() ?: 1883,
        mqttClientId = clientId,
        mqttUser = user,
        mqttPassword = pass,
        mqttRootTopic = topic,
        mqttSubQoS = qos.toIntOrNull() ?: 2
    )

    val requestBody = fullResponse.copy(settings = newSettings)
    _uiState.update { it.copy(box = it.box.copy(isLoading = true)) }

    viewModelScope.launch {
        try {
            val result = boxRepository.updateSystemSetting(device.publicTargetURI, device.key, requestBody)

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(box = it.box.copy(systemSetting = requestBody, isLoading = false))
                }
                showSnackbar(getApplication<android.app.Application>().getString(R.string.msg_save_config_success))
                onSuccess()
            }
        } catch (_: Exception) {
            _uiState.update { it.copy(box = it.box.copy(isLoading = false)) }
        }
    }
}

fun SettingViewModel.updateStorageConfig(
    videoDays: String, imageDays: String, dataDays: String, diskPercent: String,
    onSuccess: () -> Unit
) {
    val device = uiState.value.box.selectedDeviceItem ?: return
    val fullResponse = uiState.value.box.systemSetting ?: return

    val updatedSettings = fullResponse.settings.copy(
        videoRetainDays = videoDays.toDoubleOrNull() ?: fullResponse.settings.videoRetainDays,
        imageRetainDays = imageDays.toDoubleOrNull() ?: fullResponse.settings.imageRetainDays,
        dataRetainDays = dataDays.toDoubleOrNull() ?: fullResponse.settings.dataRetainDays,
        diskFullPercent = diskPercent.toDoubleOrNull() ?: fullResponse.settings.diskFullPercent
    )

    val requestBody = fullResponse.copy(settings = updatedSettings)
    _uiState.update { it.copy(box = it.box.copy(isLoading = true)) }

    viewModelScope.launch {
        try {
            val result = boxRepository.updateSystemSetting(device.publicTargetURI, device.key, requestBody)

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(box = it.box.copy(systemSetting = requestBody, isLoading = false))
                }
                showSnackbar(getApplication<android.app.Application>().getString(R.string.msg_update_storage_config_success))
                onSuccess()
            } else {
                _uiState.update { it.copy(box = it.box.copy(isLoading = false)) }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(box = it.box.copy(isLoading = false)) }
        }
    }
}

fun SettingViewModel.updateVoiceConfig(
    newVoice: String,
    onSuccess: () -> Unit
) {
    val device = uiState.value.box.selectedDeviceItem ?: return
    val fullResponse = uiState.value.box.systemSetting ?: return

    val requestBody = fullResponse.copy(settings = fullResponse.settings.copy(alarmAudio = newVoice))
    _uiState.update { it.copy(box = it.box.copy(isLoading = true)) }

    viewModelScope.launch {
        try {
            val result = boxRepository.updateSystemSetting(device.publicTargetURI, device.key, requestBody)

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(box = it.box.copy(systemSetting = requestBody, isLoading = false))
                }
                showSnackbar(getApplication<android.app.Application>().getString(R.string.msg_change_voice_success))
                onSuccess()
            } else {
                _uiState.update { it.copy(box = it.box.copy(isLoading = false)) }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(box = it.box.copy(isLoading = false)) }
        }
    }
}

fun SettingViewModel.fetchMonitorsForDevice() {
    val device = uiState.value.box.selectedDeviceItem ?: return
    _uiState.update { it.copy(box = it.box.copy(isLoading = true)) }
    viewModelScope.launch {
        try {
            val result = boxRepository.getMonitors(device.publicTargetURI, device.key)

            if (result.isSuccess) {
                val freshMonitors = result.getOrNull()?.map { monitor ->
                    MonitorDisplayItem(
                        monitor = monitor,
                        deviceUri = device.publicTargetURI,
                        deviceKey = device.key,
                        deviceId = device.id
                    )
                } ?: emptyList()
                DataRepository.monitorList = freshMonitors
                DataRepository.persist()
                _uiState.update {
                    it.copy(
                        box = it.box.copy(
                            isLoading = false,
                            monitorUpdateTicket = it.box.monitorUpdateTicket + 1
                        )
                    )
                }
            }
        } catch (_: Exception) {
            _uiState.update { it.copy(box = it.box.copy(isLoading = false)) }
        }
    }
}

fun SettingViewModel.updateAiCategory(index: Int) {
    _uiState.update {
        it.copy(box = it.box.copy(selectedAiCategory = index))
    }
}

fun SettingViewModel.prepareEditMonitor(item: MonitorDisplayItem) {
    _uiState.update {
        it.copy(box = it.box.copy(editingMonitorItem = item))
    }
    val targetScreen = if (item.monitor.type?.contains("sensor", ignoreCase = true) == true) {
        SettingScreenType.AiSensorWizard
    } else {
        SettingScreenType.AiLogicWizard
    }

    navigateTo(targetScreen, item.monitor.name.toString())
}

fun SettingViewModel.exitAiWizard() {
    _uiState.update {
        it.copy(box = it.box.copy(editingMonitorItem = null))
    }
    navigateBack()
    if (uiState.value.currentScreen == SettingScreenType.SelectAiTask) {
        navigateBack()
    }
    fetchMonitorsForDevice()
}

fun SettingViewModel.toggleAddDeviceMenu(expanded: Boolean) {
    _uiState.update {
        it.copy(box = it.box.copy(isAddDeviceMenuExpanded = expanded))
    }
}

private var jmdns: JmDNS? = null
private var multicastLock: android.net.wifi.WifiManager.MulticastLock? = null

fun SettingViewModel.startLanDiscovery(context: Context) {
    _uiState.update { it.copy(box = it.box.copy(isScanningLan = true, lanDevices = emptyList())) }

    viewModelScope.launch(dispatcherProvider.io) {
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager
            multicastLock = wifiManager.createMulticastLock("jmdns_lock").apply {
                setReferenceCounted(true)
                acquire()
            }
            val localAddress = InetAddress.getByName(getLocalIpAddress())
            jmdns = JmDNS.create(localAddress, "Android_Scan")
            jmdns?.addServiceListener("_avcore._tcp.local.", object : ServiceListener {
                override fun serviceAdded(event: ServiceEvent) {
                    jmdns?.requestServiceInfo(event.type, event.name)
                }

                override fun serviceRemoved(event: ServiceEvent) {}
                override fun serviceResolved(event: ServiceEvent) {
                    val info = event.info
                    val ipv4 = info.inet4Addresses.firstOrNull()?.hostAddress?.replace("/", "")
                    val ip = ipv4 ?: info.inet6Addresses.firstOrNull { !it.isLinkLocalAddress }?.hostAddress?.split("%")?.get(0)
                    if (ip == null || ip.startsWith("fe80")) {
                        return
                    }
                    if (ip.isNotEmpty()) {
                        val port = info.port
                        val macFromTxt = info.getPropertyString("mac")
                        val initialSystemInfo = if (macFromTxt != null) {
                            BoxSystemInfo(macAddress = macFromTxt)
                        } else null

                        val newDevice = LanDevice(
                            name = event.name,
                            ip = ip,
                            port = port,
                            isLoading = true,
                            needAuth = false,
                            systemInfo = initialSystemInfo,
                            systemState = null
                        )

                        var shouldFetch = false

                        _uiState.update { state ->
                            val currentList = state.box.lanDevices
                            val existingDevice = currentList.find { it.name == event.name || it.ip == ip }

                            if (existingDevice == null) {
                                shouldFetch = true
                                state.copy(
                                    box = state.box.copy(
                                        lanDevices = currentList + newDevice
                                    )
                                )
                            } else {
                                val updatedList = currentList.map {
                                    if (it.name == event.name) {
                                        val betterIp = if (!ip.contains(":") && it.ip.contains(":")) ip else it.ip
                                        it.copy(
                                            ip = betterIp,
                                            systemInfo = it.systemInfo ?: initialSystemInfo
                                        )
                                    } else it
                                }
                                state.copy(box = state.box.copy(lanDevices = updatedList))
                            }
                        }
                        if (shouldFetch) {
                            fetchLanDeviceInfo(newDevice)
                        }
                    }
                }
            })
            delay(5000)
            _uiState.update { it.copy(box = it.box.copy(isScanningLan = false)) }

        } catch (e: Exception) {
            _uiState.update { it.copy(box = it.box.copy(isScanningLan = false)) }
        } finally {
            stopLanDiscovery()
        }
    }
}

fun SettingViewModel.fetchLanDeviceInfo(device: LanDevice) {
    viewModelScope.launch(dispatcherProvider.io) {
        val formattedIp = if (device.ip.contains(":")) "[${device.ip}]" else device.ip
        val baseUrl = "http://$formattedIp:${device.port}"
        try {
            val infoRes = boxRepository.getDeviceInformation(baseUrl, "key")
            val code = (infoRes.exceptionOrNull() as? retrofit2.HttpException)?.code()
            if (code == 401) {
                updateLanDevice(device.name, device.ip) {
                    it.copy(needAuth = true, isLoading = false)
                }
                return@launch
            }
            if (infoRes.isSuccess) {
                val info = infoRes.getOrNull()
                val stateRes = boxRepository.getSystemState(baseUrl, "key")
                updateLanDevice(device.name, device.ip) {
                    it.copy(
                        isLoading = false,
                        systemInfo = info,
                        systemState = stateRes.getOrNull(),
                        name = info?.deviceName?.ifEmpty { it.name } ?: it.name
                    )
                }
            } else {
                updateLanDevice(device.name, device.ip) { it.copy(isLoading = false) }
            }
        } catch (_: Exception) {
            updateLanDevice(device.name, device.ip) { it.copy(isLoading = false) }
        }
    }
}

private fun SettingViewModel.updateLanDevice(
    name: String,
    ip: String,
    transform: (LanDevice) -> LanDevice
) {
    _uiState.update { state ->
        val currentList = state.box.lanDevices
        val targetDevice = currentList.find { it.name == name || it.ip == ip }
            ?: return@update state

        val updatedDevice = transform(targetDevice)
        val mac = updatedDevice.systemInfo?.macAddress

        val newList = if (!mac.isNullOrEmpty()) {
            val filteredList = currentList.filterNot { it.name != name && it.systemInfo?.macAddress == mac }
            filteredList.map { if (it.name == name) updatedDevice else it }
        } else {
            currentList.map { if (it.name == name) updatedDevice else it }
        }

        state.copy(box = state.box.copy(lanDevices = newList))
    }
}

private fun stopLanDiscovery() {
    try {
        jmdns?.close()
    } catch (_: Exception) {
    } finally {
        jmdns = null
        if (multicastLock?.isHeld == true) {
            multicastLock?.release()
        }
        multicastLock = null
    }
}

private fun getLocalIpAddress(): String? {
    try {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val networkInterface = interfaces.nextElement()
            val addresses = networkInterface.inetAddresses
            while (addresses.hasMoreElements()) {
                val address = addresses.nextElement()
                if (!address.isLoopbackAddress && address is Inet4Address) {
                    return address.hostAddress
                }
            }
        }
    } catch (_: SocketException) {
    }
    return null
}

fun SettingViewModel.startProvisioningDevice(
    userGroupId: Int,
    newKey: String,
    customName: String,
    onSuccess: () -> Unit
) {
    val boxState = uiState.value.box
    val boxInfo = boxState.info ?: return
    if (boxInfo.macAddress.isBlank()) {
        showSnackbar(getApplication<android.app.Application>().getString(R.string.msg_invalid_mac))
        return
    }
    val activeKey = boxState.verifiedKey ?: "key"
    val baseUrl = boxState.manualBaseUrl ?: run {
        val lan = boxState.lanDevices.find { it.systemInfo?.macAddress == boxInfo.macAddress }
        if (lan != null) "http://${lan.ip}:${lan.port}" else ""
    }

    if (baseUrl.isEmpty()) {
        showSnackbar(getApplication<android.app.Application>().getString(R.string.msg_device_address_not_found))
        return
    }

    viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        try {
            val cloudRes = appRepository.addDevice(
                name = customName,
                deviceCode = boxInfo.macAddress.replace(":", ""),
                key = newKey,
                userGroupId = userGroupId
            )

            if (cloudRes.isSuccess) {
                try {
                    val request = com.tenli.aiot.model.network.ConfigHttpKeyRequest(httpKey = newKey)
                    val localRes = boxRepository.configDeviceKey(baseUrl, activeKey, request)

                    if (localRes.isSuccess) {
                        showSnackbar(getApplication<android.app.Application>().getString(R.string.msg_add_device_success))
                        fetchDevices()
                        onSuccess()
                    } else {
                        showSnackbar(getApplication<android.app.Application>().getString(R.string.msg_add_device_local_key_failed))
                        fetchDevices()
                        onSuccess()
                    }
                } catch (_: Exception) {
                    showSnackbar(getApplication<android.app.Application>().getString(R.string.msg_add_device_cloud_success_local_failed))
                    fetchDevices()
                    onSuccess()
                }
            } else {
                val errorMsg = when ((cloudRes.exceptionOrNull() as? retrofit2.HttpException)?.code()) {
                    400 -> "Thiết bị này đã tồn tại trên hệ thống"
                    else -> "Lỗi Cloud: ${cloudRes.exceptionOrNull()?.message}"
                }
                showSnackbar(errorMsg)
            }
        } catch (e: Exception) {
            showSnackbar("${getApplication<android.app.Application>().getString(R.string.msg_connection_error)}: ${e.message}")
        } finally {
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}

fun SettingViewModel.prepareAddLanDevice(device: LanDevice) {
    val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    val generatedKey = (1..16).map { chars.random() }.joinToString("")

    _uiState.update { state ->
        state.copy(
            box = state.box.copy(
                info = device.systemInfo,
                state = device.systemState,
                verifiedKey = "key",
                pendingKey = generatedKey,
                manualBaseUrl = null
            )
        )
    }
    navigateTo(SettingScreenType.AddDeviceScreen, "Thông tin thiết bị")
}

fun SettingViewModel.deleteDeviceFromSystem(isResetLocal: Boolean) {
    val device = uiState.value.box.selectedDeviceItem ?: return
    _uiState.update { it.copy(box = it.box.copy(isLoading = true)) }
    viewModelScope.launch {
        try {
            val result = appRepository.deleteDevice(device.id)

            if (result.isSuccess) {
                if (isResetLocal) {
                    try {
                        val request = com.tenli.aiot.model.network.DeviceCommandRequest(command = "reset")
                        boxRepository.controlDevice(device.publicTargetURI, device.key, request)
                    } catch (_: Exception) {
                    }
                }
                showSnackbar(getApplication<android.app.Application>().getString(R.string.msg_delete_device_success))
                fetchDevices()
                navigateBack()
            } else {
                showSnackbar("${getApplication<android.app.Application>().getString(R.string.msg_cloud_delete_device_failed)} (${(result.exceptionOrNull() as? retrofit2.HttpException)?.code()})")
            }
        } catch (e: Exception) {
            showSnackbar("${getApplication<android.app.Application>().getString(R.string.msg_connection_error)}: ${e.message}")
        } finally {
            _uiState.update { it.copy(box = it.box.copy(isLoading = false)) }
        }
    }
}

fun SettingViewModel.navigateToAndClearStack(screen: SettingScreenType, title: String) {
    navigationStack.clear()
    navigationStack.add(SettingScreenType.Main to "Cài đặt")
    navigationStack.add(screen to title)
    _uiState.update {
        it.copy(
            currentScreen = screen,
            title = title
        )
    }
}

fun SettingViewModel.checkManualDevice(input: String, key: String, onUnauthorized: () -> Unit) {
    _uiState.update { it.copy(box = it.box.copy(isLoading = true)) }

    viewModelScope.launch(dispatcherProvider.io) {
        val inputTrimmed = input.trim()
        val baseUrl = when {
            inputTrimmed.startsWith("http") -> inputTrimmed
            inputTrimmed.contains(".") -> "http://$inputTrimmed:8088"
            else -> "https://$inputTrimmed.dev.tenli.ai"
        }
        try {
            val infoRes = boxRepository.getDeviceInformation(baseUrl, key)
            val code = (infoRes.exceptionOrNull() as? retrofit2.HttpException)?.code()
            if (code == 401) {
                _uiState.update { it.copy(box = it.box.copy(isLoading = false)) }
                launch(dispatcherProvider.main) {
                    if (key == "key") onUnauthorized()
                    else showSnackbar(getApplication<android.app.Application>().getString(R.string.msg_incorrect_security_code))
                }
                return@launch
            }

            if (infoRes.isSuccess) {
                val info = infoRes.getOrNull()
                val stateRes = boxRepository.getSystemState(baseUrl, "key")
                val finalPendingKey = if (key == "key") {
                    val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
                    (1..16).map { chars.random() }.joinToString("")
                } else {
                    key
                }
                _uiState.update { state ->
                    state.copy(
                        box = state.box.copy(
                            info = info,
                            state = stateRes.getOrNull(),
                            verifiedKey = key,
                            pendingKey = finalPendingKey,
                            manualBaseUrl = baseUrl
                        )
                    )
                }
                launch(dispatcherProvider.main) {
                    navigateTo(SettingScreenType.AddDeviceScreen, "Thêm thiết bị")
                }
            } else {
                showSnackbar(getApplication<android.app.Application>().getString(R.string.msg_device_not_found_at_address))
            }
        } catch (e: Exception) {
            showSnackbar(getApplication<android.app.Application>().getString(R.string.msg_connection_error_ip_sn))
        } finally {
            _uiState.update { it.copy(box = it.box.copy(isLoading = false)) }
        }
    }
}

fun SettingViewModel.toggleCameraMenu(expanded: Boolean) {
    _uiState.update {
        it.copy(box = it.box.copy(isCameraMenuExpanded = expanded))
    }
}

fun SettingViewModel.controlAlarm(command: String, duration: Long? = null) {
    val device = uiState.value.box.selectedDeviceItem ?: return
    _uiState.update { it.copy(box = it.box.copy(isLoading = true)) }
    viewModelScope.launch {
        try {
            val request = com.tenli.aiot.model.network.DeviceCommandRequest(command = command, duration = duration)
            val result = boxRepository.controlDevice(device.publicTargetURI, device.key, request)
            if (result.isSuccess) {
                val msg = when {
                    command == "alarm" -> "Bắt đầu kiểm tra âm thanh"
                    command == "mute" && duration == 0L -> "Đã bật âm thanh cảnh báo" // [cite: 2026-03-16]
                    command == "mute" && duration == null -> "Đã tắt kiểm tra âm thanh"
                    command == "mute" && (duration ?: 0L) > 0L -> "Đã tạm tắt âm thanh cảnh báo"
                    else -> "Thao tác thành công"
                }
                showSnackbar(msg)
                refreshSystemState()
            }
        } catch (_: Exception) {
            showSnackbar(getApplication<android.app.Application>().getString(R.string.msg_connection_error_device))
        } finally {
            _uiState.update { it.copy(box = it.box.copy(isLoading = false)) }
        }
    }
}