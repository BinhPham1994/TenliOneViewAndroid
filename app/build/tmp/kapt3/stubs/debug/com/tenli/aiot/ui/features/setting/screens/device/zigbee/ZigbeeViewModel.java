package com.tenli.aiot.ui.features.setting.screens.device.zigbee;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\b\b\u0007\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\b\u0010\u0010\u001a\u00020\u0011H\u0002J\u001e\u0010\u0012\u001a\u00020\u00112\u0006\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u00142\u0006\u0010\u0016\u001a\u00020\u0014J\u001a\u0010\u0017\u001a\u00020\u00112\b\u0010\u0018\u001a\u0004\u0018\u00010\u00142\u0006\u0010\u0019\u001a\u00020\u0014H\u0002J\b\u0010\u001a\u001a\u00020\u0011H\u0014J\b\u0010\u001b\u001a\u00020\u0011H\u0002R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\n\u001a\u0004\u0018\u00010\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0017\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00070\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000f\u00a8\u0006\u001c"}, d2 = {"Lcom/tenli/aiot/ui/features/setting/screens/device/zigbee/ZigbeeViewModel;", "Landroidx/lifecycle/ViewModel;", "device", "Lcom/tenli/aiot/model/network/DeviceItem;", "(Lcom/tenli/aiot/model/network/DeviceItem;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/tenli/aiot/ui/features/setting/screens/device/zigbee/ZigbeeUiState;", "gson", "Lcom/google/gson/Gson;", "mqttClient", "Lorg/eclipse/paho/client/mqttv3/MqttAsyncClient;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "connectMqtt", "", "controlDevice", "ieeeAddress", "", "key", "value", "handleMqttMessage", "topic", "payload", "onCleared", "subscribeToDevices", "app_debug"})
public final class ZigbeeViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.aiot.model.network.DeviceItem device = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.tenli.aiot.ui.features.setting.screens.device.zigbee.ZigbeeUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.tenli.aiot.ui.features.setting.screens.device.zigbee.ZigbeeUiState> uiState = null;
    @org.jetbrains.annotations.Nullable()
    private org.eclipse.paho.client.mqttv3.MqttAsyncClient mqttClient;
    @org.jetbrains.annotations.NotNull()
    private final com.google.gson.Gson gson = null;
    
    public ZigbeeViewModel(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.model.network.DeviceItem device) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.tenli.aiot.ui.features.setting.screens.device.zigbee.ZigbeeUiState> getUiState() {
        return null;
    }
    
    private final void connectMqtt() {
    }
    
    private final void subscribeToDevices() {
    }
    
    private final void handleMqttMessage(java.lang.String topic, java.lang.String payload) {
    }
    
    public final void controlDevice(@org.jetbrains.annotations.NotNull()
    java.lang.String ieeeAddress, @org.jetbrains.annotations.NotNull()
    java.lang.String key, @org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    @java.lang.Override()
    protected void onCleared() {
    }
}