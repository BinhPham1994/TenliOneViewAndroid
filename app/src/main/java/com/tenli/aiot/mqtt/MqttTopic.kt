package com.tenli.aiot.mqtt

class MqttTopic {
    companion object Factory {
        const val SCAN_DEVICE_TOPIC = "device/bridge/devices"
        const val DEVICE_TOPIC = "device/%s"
        const val DEVICE_ALL_TOPIC = "device/%s/#"
        const val GET_DEVICE_STATE_TOPIC = "device/%s/get"
        const val SET_DEVICE_STATE_TOPIC = "device/%s/set"
        const val BRIDGE_REQUEST_JOIN_TOPIC = "device/bridge/request/permit_join"
        const val DEVICE_JOIN_TOPIC = "device/bridge/#"
    }
}