package com.tenli.aiot.mqtt;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\b\u0007\u0018\u0000 \u00032\u00020\u0001:\u0001\u0003B\u0005\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0004"}, d2 = {"Lcom/tenli/aiot/mqtt/MqttTopic;", "", "()V", "Factory", "app_productionDebug"})
public final class MqttTopic {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String SCAN_DEVICE_TOPIC = "device/bridge/devices";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String DEVICE_TOPIC = "device/%s";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String DEVICE_ALL_TOPIC = "device/%s/#";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String GET_DEVICE_STATE_TOPIC = "device/%s/get";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String SET_DEVICE_STATE_TOPIC = "device/%s/set";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String BRIDGE_REQUEST_JOIN_TOPIC = "device/bridge/request/permit_join";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String DEVICE_JOIN_TOPIC = "device/bridge/#";
    @org.jetbrains.annotations.NotNull()
    public static final com.tenli.aiot.mqtt.MqttTopic.Factory Factory = null;
    
    public MqttTopic() {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0007\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000b"}, d2 = {"Lcom/tenli/aiot/mqtt/MqttTopic$Factory;", "", "()V", "BRIDGE_REQUEST_JOIN_TOPIC", "", "DEVICE_ALL_TOPIC", "DEVICE_JOIN_TOPIC", "DEVICE_TOPIC", "GET_DEVICE_STATE_TOPIC", "SCAN_DEVICE_TOPIC", "SET_DEVICE_STATE_TOPIC", "app_productionDebug"})
    public static final class Factory {
        
        private Factory() {
            super();
        }
    }
}