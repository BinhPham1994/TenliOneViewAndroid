package com.tenli.oneview.ui.features.setting.core;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000~\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\f\n\u0002\u0010 \n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0011\u001a\n\u0010\u0005\u001a\u0004\u0018\u00010\u0006H\u0002\u001a\b\u0010\u0007\u001a\u00020\bH\u0002\u001a(\u0010\t\u001a\u00020\b*\u00020\n2\u0006\u0010\u000b\u001a\u00020\u00062\u0006\u0010\f\u001a\u00020\u00062\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\b0\u000e\u001a#\u0010\u000f\u001a\u00020\b*\u00020\n2\u0006\u0010\u0010\u001a\u00020\u00062\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\u0012\u00a2\u0006\u0002\u0010\u0013\u001a\u001a\u0010\u0014\u001a\u00020\b*\u00020\n2\u0006\u0010\u0010\u001a\u00020\u00062\u0006\u0010\u0015\u001a\u00020\u0006\u001a\u0012\u0010\u0016\u001a\u00020\b*\u00020\n2\u0006\u0010\u0017\u001a\u00020\u0018\u001a\n\u0010\u0019\u001a\u00020\b*\u00020\n\u001a\n\u0010\u001a\u001a\u00020\b*\u00020\n\u001a\u0012\u0010\u001b\u001a\u00020\b*\u00020\n2\u0006\u0010\u001c\u001a\u00020\u001d\u001a\n\u0010\u001e\u001a\u00020\b*\u00020\n\u001a\u0012\u0010\u001f\u001a\u00020\b*\u00020\n2\u0006\u0010 \u001a\u00020!\u001a\n\u0010\"\u001a\u00020\b*\u00020\n\u001a\n\u0010#\u001a\u00020\b*\u00020\n\u001a\u001a\u0010$\u001a\u00020\b*\u00020\n2\u0006\u0010%\u001a\u00020&2\u0006\u0010\'\u001a\u00020\u0006\u001a\u0012\u0010(\u001a\u00020\b*\u00020\n2\u0006\u0010 \u001a\u00020!\u001a\u0012\u0010)\u001a\u00020\b*\u00020\n2\u0006\u0010*\u001a\u00020+\u001a\n\u0010,\u001a\u00020\b*\u00020\n\u001a\u0012\u0010-\u001a\u00020\b*\u00020\n2\u0006\u0010 \u001a\u00020.\u001a\u0012\u0010/\u001a\u00020\b*\u00020\n2\u0006\u00100\u001a\u000201\u001a0\u00102\u001a\u00020\b*\u00020\n2\u0006\u00103\u001a\u00020\u001d2\u0006\u00104\u001a\u00020\u00062\u0006\u00105\u001a\u00020\u00062\f\u00106\u001a\b\u0012\u0004\u0012\u00020\b0\u000e\u001a\u0012\u00107\u001a\u00020\b*\u00020\n2\u0006\u00108\u001a\u00020\u0018\u001a\u0012\u00109\u001a\u00020\b*\u00020\n2\u0006\u00108\u001a\u00020\u0018\u001a\u0012\u0010:\u001a\u00020\b*\u00020\n2\u0006\u0010;\u001a\u00020\u001d\u001a\u001a\u0010<\u001a\u00020\b*\u00020\n2\f\u0010=\u001a\b\u0012\u0004\u0012\u00020.0>H\u0002\u001a0\u0010?\u001a\u00020\b*\u00020\n2\u0006\u0010@\u001a\u00020\u00062\u0006\u0010A\u001a\u00020\u00062\u0012\u0010B\u001a\u000e\u0012\u0004\u0012\u00020!\u0012\u0004\u0012\u00020!0CH\u0002\u001aX\u0010D\u001a\u00020\b*\u00020\n2\u0006\u0010E\u001a\u00020\u00182\u0006\u0010F\u001a\u00020\u00062\u0006\u0010G\u001a\u00020\u00062\u0006\u0010H\u001a\u00020\u00062\u0006\u0010I\u001a\u00020\u00062\u0006\u0010J\u001a\u00020\u00062\u0006\u0010K\u001a\u00020\u00062\u0006\u0010L\u001a\u00020\u00062\f\u00106\u001a\b\u0012\u0004\u0012\u00020\b0\u000e\u001a8\u0010M\u001a\u00020\b*\u00020\n2\u0006\u0010N\u001a\u00020\u00062\u0006\u0010O\u001a\u00020\u00062\u0006\u0010P\u001a\u00020\u00062\u0006\u0010Q\u001a\u00020\u00062\f\u00106\u001a\b\u0012\u0004\u0012\u00020\b0\u000e\u001a \u0010R\u001a\u00020\b*\u00020\n2\u0006\u0010S\u001a\u00020\u00062\f\u00106\u001a\b\u0012\u0004\u0012\u00020\b0\u000e\"\u0010\u0010\u0000\u001a\u0004\u0018\u00010\u0001X\u0082\u000e\u00a2\u0006\u0002\n\u0000\"\u0014\u0010\u0002\u001a\b\u0018\u00010\u0003R\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006T"}, d2 = {"jmdns", "Ljavax/jmdns/JmDNS;", "multicastLock", "Landroid/net/wifi/WifiManager$MulticastLock;", "Landroid/net/wifi/WifiManager;", "getLocalIpAddress", "", "stopLanDiscovery", "", "checkManualDevice", "Lcom/tenli/oneview/ui/features/setting/core/SettingViewModel;", "input", "key", "onUnauthorized", "Lkotlin/Function0;", "controlAlarm", "command", "duration", "", "(Lcom/tenli/oneview/ui/features/setting/core/SettingViewModel;Ljava/lang/String;Ljava/lang/Long;)V", "controlBoxSystem", "successMessage", "deleteDeviceFromSystem", "isResetLocal", "", "exitAiWizard", "fetchBoxInfo", "fetchDeviceDetail", "deviceId", "", "fetchDevices", "fetchLanDeviceInfo", "device", "Lcom/tenli/oneview/model/network/LanDevice;", "fetchMonitorsForDevice", "fetchSystemSettings", "navigateToAndClearStack", "screen", "Lcom/tenli/oneview/ui/features/setting/core/SettingScreenType;", "title", "prepareAddLanDevice", "prepareEditMonitor", "item", "Lcom/tenli/oneview/model/network/MonitorDisplayItem;", "refreshSystemState", "selectDevice", "Lcom/tenli/oneview/model/network/DeviceItem;", "startLanDiscovery", "context", "Landroid/content/Context;", "startProvisioningDevice", "userGroupId", "newKey", "customName", "onSuccess", "toggleAddDeviceMenu", "expanded", "toggleCameraMenu", "updateAiCategory", "index", "updateDeviceGroupUi", "devices", "", "updateLanDevice", "name", "ip", "transform", "Lkotlin/Function1;", "updateMqttConfig", "enabled", "host", "port", "clientId", "user", "pass", "topic", "qos", "updateStorageConfig", "videoDays", "imageDays", "dataDays", "diskPercent", "updateVoiceConfig", "newVoice", "app_debug"})
public final class SettingViewModelDeviceExtKt {
    @org.jetbrains.annotations.Nullable()
    private static javax.jmdns.JmDNS jmdns;
    @org.jetbrains.annotations.Nullable()
    private static android.net.wifi.WifiManager.MulticastLock multicastLock;
    
    public static final void fetchDevices(@org.jetbrains.annotations.NotNull()
    com.tenli.oneview.ui.features.setting.core.SettingViewModel $this$fetchDevices) {
    }
    
    private static final void updateDeviceGroupUi(com.tenli.oneview.ui.features.setting.core.SettingViewModel $this$updateDeviceGroupUi, java.util.List<com.tenli.oneview.model.network.DeviceItem> devices) {
    }
    
    public static final void selectDevice(@org.jetbrains.annotations.NotNull()
    com.tenli.oneview.ui.features.setting.core.SettingViewModel $this$selectDevice, @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.DeviceItem device) {
    }
    
    public static final void fetchDeviceDetail(@org.jetbrains.annotations.NotNull()
    com.tenli.oneview.ui.features.setting.core.SettingViewModel $this$fetchDeviceDetail, int deviceId) {
    }
    
    public static final void controlBoxSystem(@org.jetbrains.annotations.NotNull()
    com.tenli.oneview.ui.features.setting.core.SettingViewModel $this$controlBoxSystem, @org.jetbrains.annotations.NotNull()
    java.lang.String command, @org.jetbrains.annotations.NotNull()
    java.lang.String successMessage) {
    }
    
    public static final void fetchBoxInfo(@org.jetbrains.annotations.NotNull()
    com.tenli.oneview.ui.features.setting.core.SettingViewModel $this$fetchBoxInfo) {
    }
    
    public static final void refreshSystemState(@org.jetbrains.annotations.NotNull()
    com.tenli.oneview.ui.features.setting.core.SettingViewModel $this$refreshSystemState) {
    }
    
    public static final void fetchSystemSettings(@org.jetbrains.annotations.NotNull()
    com.tenli.oneview.ui.features.setting.core.SettingViewModel $this$fetchSystemSettings) {
    }
    
    public static final void updateMqttConfig(@org.jetbrains.annotations.NotNull()
    com.tenli.oneview.ui.features.setting.core.SettingViewModel $this$updateMqttConfig, boolean enabled, @org.jetbrains.annotations.NotNull()
    java.lang.String host, @org.jetbrains.annotations.NotNull()
    java.lang.String port, @org.jetbrains.annotations.NotNull()
    java.lang.String clientId, @org.jetbrains.annotations.NotNull()
    java.lang.String user, @org.jetbrains.annotations.NotNull()
    java.lang.String pass, @org.jetbrains.annotations.NotNull()
    java.lang.String topic, @org.jetbrains.annotations.NotNull()
    java.lang.String qos, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onSuccess) {
    }
    
    public static final void updateStorageConfig(@org.jetbrains.annotations.NotNull()
    com.tenli.oneview.ui.features.setting.core.SettingViewModel $this$updateStorageConfig, @org.jetbrains.annotations.NotNull()
    java.lang.String videoDays, @org.jetbrains.annotations.NotNull()
    java.lang.String imageDays, @org.jetbrains.annotations.NotNull()
    java.lang.String dataDays, @org.jetbrains.annotations.NotNull()
    java.lang.String diskPercent, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onSuccess) {
    }
    
    public static final void updateVoiceConfig(@org.jetbrains.annotations.NotNull()
    com.tenli.oneview.ui.features.setting.core.SettingViewModel $this$updateVoiceConfig, @org.jetbrains.annotations.NotNull()
    java.lang.String newVoice, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onSuccess) {
    }
    
    public static final void fetchMonitorsForDevice(@org.jetbrains.annotations.NotNull()
    com.tenli.oneview.ui.features.setting.core.SettingViewModel $this$fetchMonitorsForDevice) {
    }
    
    public static final void updateAiCategory(@org.jetbrains.annotations.NotNull()
    com.tenli.oneview.ui.features.setting.core.SettingViewModel $this$updateAiCategory, int index) {
    }
    
    public static final void prepareEditMonitor(@org.jetbrains.annotations.NotNull()
    com.tenli.oneview.ui.features.setting.core.SettingViewModel $this$prepareEditMonitor, @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.MonitorDisplayItem item) {
    }
    
    public static final void exitAiWizard(@org.jetbrains.annotations.NotNull()
    com.tenli.oneview.ui.features.setting.core.SettingViewModel $this$exitAiWizard) {
    }
    
    public static final void toggleAddDeviceMenu(@org.jetbrains.annotations.NotNull()
    com.tenli.oneview.ui.features.setting.core.SettingViewModel $this$toggleAddDeviceMenu, boolean expanded) {
    }
    
    public static final void startLanDiscovery(@org.jetbrains.annotations.NotNull()
    com.tenli.oneview.ui.features.setting.core.SettingViewModel $this$startLanDiscovery, @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    public static final void fetchLanDeviceInfo(@org.jetbrains.annotations.NotNull()
    com.tenli.oneview.ui.features.setting.core.SettingViewModel $this$fetchLanDeviceInfo, @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.LanDevice device) {
    }
    
    private static final void updateLanDevice(com.tenli.oneview.ui.features.setting.core.SettingViewModel $this$updateLanDevice, java.lang.String name, java.lang.String ip, kotlin.jvm.functions.Function1<? super com.tenli.oneview.model.network.LanDevice, com.tenli.oneview.model.network.LanDevice> transform) {
    }
    
    private static final void stopLanDiscovery() {
    }
    
    private static final java.lang.String getLocalIpAddress() {
        return null;
    }
    
    public static final void startProvisioningDevice(@org.jetbrains.annotations.NotNull()
    com.tenli.oneview.ui.features.setting.core.SettingViewModel $this$startProvisioningDevice, int userGroupId, @org.jetbrains.annotations.NotNull()
    java.lang.String newKey, @org.jetbrains.annotations.NotNull()
    java.lang.String customName, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onSuccess) {
    }
    
    public static final void prepareAddLanDevice(@org.jetbrains.annotations.NotNull()
    com.tenli.oneview.ui.features.setting.core.SettingViewModel $this$prepareAddLanDevice, @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.LanDevice device) {
    }
    
    public static final void deleteDeviceFromSystem(@org.jetbrains.annotations.NotNull()
    com.tenli.oneview.ui.features.setting.core.SettingViewModel $this$deleteDeviceFromSystem, boolean isResetLocal) {
    }
    
    public static final void navigateToAndClearStack(@org.jetbrains.annotations.NotNull()
    com.tenli.oneview.ui.features.setting.core.SettingViewModel $this$navigateToAndClearStack, @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.ui.features.setting.core.SettingScreenType screen, @org.jetbrains.annotations.NotNull()
    java.lang.String title) {
    }
    
    public static final void checkManualDevice(@org.jetbrains.annotations.NotNull()
    com.tenli.oneview.ui.features.setting.core.SettingViewModel $this$checkManualDevice, @org.jetbrains.annotations.NotNull()
    java.lang.String input, @org.jetbrains.annotations.NotNull()
    java.lang.String key, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onUnauthorized) {
    }
    
    public static final void toggleCameraMenu(@org.jetbrains.annotations.NotNull()
    com.tenli.oneview.ui.features.setting.core.SettingViewModel $this$toggleCameraMenu, boolean expanded) {
    }
    
    public static final void controlAlarm(@org.jetbrains.annotations.NotNull()
    com.tenli.oneview.ui.features.setting.core.SettingViewModel $this$controlAlarm, @org.jetbrains.annotations.NotNull()
    java.lang.String command, @org.jetbrains.annotations.Nullable()
    java.lang.Long duration) {
    }
}