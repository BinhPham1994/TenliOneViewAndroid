package com.tenli.oneview.ui.features.setting.core;

/**
 * BoxUiState MỚI: Chỉ giữ lại thông tin hệ thống
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000V\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b3\b\u0087\b\u0018\u00002\u00020\u0001B\u00db\u0001\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u0012\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\t\u0012\u000e\b\u0002\u0010\n\u001a\b\u0012\u0004\u0012\u00020\f0\u000b\u0012\b\b\u0002\u0010\r\u001a\u00020\u000e\u0012\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u0010\u0012\b\b\u0002\u0010\u0011\u001a\u00020\u000e\u0012\b\b\u0002\u0010\u0012\u001a\u00020\u0003\u0012\u000e\b\u0002\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00140\u000b\u0012\n\b\u0002\u0010\u0015\u001a\u0004\u0018\u00010\u0016\u0012\b\b\u0002\u0010\u0017\u001a\u00020\u0003\u0012\u000e\b\u0002\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00190\u000b\u0012\n\b\u0002\u0010\u001a\u001a\u0004\u0018\u00010\u001b\u0012\n\b\u0002\u0010\u001c\u001a\u0004\u0018\u00010\u001b\u0012\n\b\u0002\u0010\u001d\u001a\u0004\u0018\u00010\u001b\u0012\b\b\u0002\u0010\u001e\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u001f\u001a\u00020\u0003\u00a2\u0006\u0002\u0010 J\t\u00107\u001a\u00020\u0003H\u00c6\u0003J\u000f\u00108\u001a\b\u0012\u0004\u0012\u00020\u00140\u000bH\u00c6\u0003J\u000b\u00109\u001a\u0004\u0018\u00010\u0016H\u00c6\u0003J\t\u0010:\u001a\u00020\u0003H\u00c6\u0003J\u000f\u0010;\u001a\b\u0012\u0004\u0012\u00020\u00190\u000bH\u00c6\u0003J\u000b\u0010<\u001a\u0004\u0018\u00010\u001bH\u00c6\u0003J\u000b\u0010=\u001a\u0004\u0018\u00010\u001bH\u00c6\u0003J\u000b\u0010>\u001a\u0004\u0018\u00010\u001bH\u00c6\u0003J\t\u0010?\u001a\u00020\u0003H\u00c6\u0003J\t\u0010@\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010A\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010B\u001a\u0004\u0018\u00010\u0007H\u00c6\u0003J\u000b\u0010C\u001a\u0004\u0018\u00010\tH\u00c6\u0003J\u000f\u0010D\u001a\b\u0012\u0004\u0012\u00020\f0\u000bH\u00c6\u0003J\t\u0010E\u001a\u00020\u000eH\u00c6\u0003J\u000b\u0010F\u001a\u0004\u0018\u00010\u0010H\u00c6\u0003J\t\u0010G\u001a\u00020\u000eH\u00c6\u0003J\t\u0010H\u001a\u00020\u0003H\u00c6\u0003J\u00df\u0001\u0010I\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u00072\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\t2\u000e\b\u0002\u0010\n\u001a\b\u0012\u0004\u0012\u00020\f0\u000b2\b\b\u0002\u0010\r\u001a\u00020\u000e2\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u00102\b\b\u0002\u0010\u0011\u001a\u00020\u000e2\b\b\u0002\u0010\u0012\u001a\u00020\u00032\u000e\b\u0002\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00140\u000b2\n\b\u0002\u0010\u0015\u001a\u0004\u0018\u00010\u00162\b\b\u0002\u0010\u0017\u001a\u00020\u00032\u000e\b\u0002\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00190\u000b2\n\b\u0002\u0010\u001a\u001a\u0004\u0018\u00010\u001b2\n\b\u0002\u0010\u001c\u001a\u0004\u0018\u00010\u001b2\n\b\u0002\u0010\u001d\u001a\u0004\u0018\u00010\u001b2\b\b\u0002\u0010\u001e\u001a\u00020\u00032\b\b\u0002\u0010\u001f\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010J\u001a\u00020\u00032\b\u0010K\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010L\u001a\u00020\u000eH\u00d6\u0001J\t\u0010M\u001a\u00020\u001bH\u00d6\u0001R\u0017\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00140\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\"R\u0013\u0010\u000f\u001a\u0004\u0018\u00010\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010$R\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010&R\u0011\u0010\u0012\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\'R\u0011\u0010\u001e\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\'R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0002\u0010\'R\u0011\u0010\u0017\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\'R\u0011\u0010\u001f\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010\'R\u0017\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00190\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b(\u0010\"R\u0013\u0010\u001d\u001a\u0004\u0018\u00010\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010*R\u0017\u0010\n\u001a\b\u0012\u0004\u0012\u00020\f0\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b+\u0010\"R\u0011\u0010\u0011\u001a\u00020\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b,\u0010-R\u0013\u0010\u001c\u001a\u0004\u0018\u00010\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b.\u0010*R\u0011\u0010\r\u001a\u00020\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b/\u0010-R\u0013\u0010\u0015\u001a\u0004\u0018\u00010\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b0\u00101R\u0013\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b2\u00103R\u0013\u0010\b\u001a\u0004\u0018\u00010\t\u00a2\u0006\b\n\u0000\u001a\u0004\b4\u00105R\u0013\u0010\u001a\u001a\u0004\u0018\u00010\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b6\u0010*\u00a8\u0006N"}, d2 = {"Lcom/tenli/oneview/ui/features/setting/core/BoxUiState;", "", "isLoading", "", "info", "Lcom/tenli/oneview/model/network/BoxSystemInfo;", "state", "Lcom/tenli/oneview/model/network/BoxSystemState;", "systemSetting", "Lcom/tenli/oneview/model/network/SystemSettingResponse;", "monitorTypes", "", "Lcom/tenli/oneview/model/network/MonitorType;", "selectedAiCategory", "", "editingMonitorItem", "Lcom/tenli/oneview/model/network/MonitorDisplayItem;", "monitorUpdateTicket", "isAddDeviceMenuExpanded", "deviceGroups", "Lcom/tenli/oneview/ui/features/setting/core/DeviceGroupDisplay;", "selectedDeviceItem", "Lcom/tenli/oneview/model/network/DeviceItem;", "isScanningLan", "lanDevices", "Lcom/tenli/oneview/model/network/LanDevice;", "verifiedKey", "", "pendingKey", "manualBaseUrl", "isCameraMenuExpanded", "isScriptMenuExpanded", "(ZLcom/tenli/oneview/model/network/BoxSystemInfo;Lcom/tenli/oneview/model/network/BoxSystemState;Lcom/tenli/oneview/model/network/SystemSettingResponse;Ljava/util/List;ILcom/tenli/oneview/model/network/MonitorDisplayItem;IZLjava/util/List;Lcom/tenli/oneview/model/network/DeviceItem;ZLjava/util/List;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZZ)V", "getDeviceGroups", "()Ljava/util/List;", "getEditingMonitorItem", "()Lcom/tenli/oneview/model/network/MonitorDisplayItem;", "getInfo", "()Lcom/tenli/oneview/model/network/BoxSystemInfo;", "()Z", "getLanDevices", "getManualBaseUrl", "()Ljava/lang/String;", "getMonitorTypes", "getMonitorUpdateTicket", "()I", "getPendingKey", "getSelectedAiCategory", "getSelectedDeviceItem", "()Lcom/tenli/oneview/model/network/DeviceItem;", "getState", "()Lcom/tenli/oneview/model/network/BoxSystemState;", "getSystemSetting", "()Lcom/tenli/oneview/model/network/SystemSettingResponse;", "getVerifiedKey", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "hashCode", "toString", "app_debug"})
public final class BoxUiState {
    private final boolean isLoading = false;
    @org.jetbrains.annotations.Nullable()
    private final com.tenli.oneview.model.network.BoxSystemInfo info = null;
    @org.jetbrains.annotations.Nullable()
    private final com.tenli.oneview.model.network.BoxSystemState state = null;
    @org.jetbrains.annotations.Nullable()
    private final com.tenli.oneview.model.network.SystemSettingResponse systemSetting = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.tenli.oneview.model.network.MonitorType> monitorTypes = null;
    private final int selectedAiCategory = 0;
    @org.jetbrains.annotations.Nullable()
    private final com.tenli.oneview.model.network.MonitorDisplayItem editingMonitorItem = null;
    private final int monitorUpdateTicket = 0;
    private final boolean isAddDeviceMenuExpanded = false;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.tenli.oneview.ui.features.setting.core.DeviceGroupDisplay> deviceGroups = null;
    @org.jetbrains.annotations.Nullable()
    private final com.tenli.oneview.model.network.DeviceItem selectedDeviceItem = null;
    private final boolean isScanningLan = false;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.tenli.oneview.model.network.LanDevice> lanDevices = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String verifiedKey = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String pendingKey = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String manualBaseUrl = null;
    private final boolean isCameraMenuExpanded = false;
    private final boolean isScriptMenuExpanded = false;
    
    public final boolean component1() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.tenli.oneview.ui.features.setting.core.DeviceGroupDisplay> component10() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.oneview.model.network.DeviceItem component11() {
        return null;
    }
    
    public final boolean component12() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.tenli.oneview.model.network.LanDevice> component13() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component14() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component15() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component16() {
        return null;
    }
    
    public final boolean component17() {
        return false;
    }
    
    public final boolean component18() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.oneview.model.network.BoxSystemInfo component2() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.oneview.model.network.BoxSystemState component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.oneview.model.network.SystemSettingResponse component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.tenli.oneview.model.network.MonitorType> component5() {
        return null;
    }
    
    public final int component6() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.oneview.model.network.MonitorDisplayItem component7() {
        return null;
    }
    
    public final int component8() {
        return 0;
    }
    
    public final boolean component9() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.oneview.ui.features.setting.core.BoxUiState copy(boolean isLoading, @org.jetbrains.annotations.Nullable()
    com.tenli.oneview.model.network.BoxSystemInfo info, @org.jetbrains.annotations.Nullable()
    com.tenli.oneview.model.network.BoxSystemState state, @org.jetbrains.annotations.Nullable()
    com.tenli.oneview.model.network.SystemSettingResponse systemSetting, @org.jetbrains.annotations.NotNull()
    java.util.List<com.tenli.oneview.model.network.MonitorType> monitorTypes, int selectedAiCategory, @org.jetbrains.annotations.Nullable()
    com.tenli.oneview.model.network.MonitorDisplayItem editingMonitorItem, int monitorUpdateTicket, boolean isAddDeviceMenuExpanded, @org.jetbrains.annotations.NotNull()
    java.util.List<com.tenli.oneview.ui.features.setting.core.DeviceGroupDisplay> deviceGroups, @org.jetbrains.annotations.Nullable()
    com.tenli.oneview.model.network.DeviceItem selectedDeviceItem, boolean isScanningLan, @org.jetbrains.annotations.NotNull()
    java.util.List<com.tenli.oneview.model.network.LanDevice> lanDevices, @org.jetbrains.annotations.Nullable()
    java.lang.String verifiedKey, @org.jetbrains.annotations.Nullable()
    java.lang.String pendingKey, @org.jetbrains.annotations.Nullable()
    java.lang.String manualBaseUrl, boolean isCameraMenuExpanded, boolean isScriptMenuExpanded) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
    
    public BoxUiState(boolean isLoading, @org.jetbrains.annotations.Nullable()
    com.tenli.oneview.model.network.BoxSystemInfo info, @org.jetbrains.annotations.Nullable()
    com.tenli.oneview.model.network.BoxSystemState state, @org.jetbrains.annotations.Nullable()
    com.tenli.oneview.model.network.SystemSettingResponse systemSetting, @org.jetbrains.annotations.NotNull()
    java.util.List<com.tenli.oneview.model.network.MonitorType> monitorTypes, int selectedAiCategory, @org.jetbrains.annotations.Nullable()
    com.tenli.oneview.model.network.MonitorDisplayItem editingMonitorItem, int monitorUpdateTicket, boolean isAddDeviceMenuExpanded, @org.jetbrains.annotations.NotNull()
    java.util.List<com.tenli.oneview.ui.features.setting.core.DeviceGroupDisplay> deviceGroups, @org.jetbrains.annotations.Nullable()
    com.tenli.oneview.model.network.DeviceItem selectedDeviceItem, boolean isScanningLan, @org.jetbrains.annotations.NotNull()
    java.util.List<com.tenli.oneview.model.network.LanDevice> lanDevices, @org.jetbrains.annotations.Nullable()
    java.lang.String verifiedKey, @org.jetbrains.annotations.Nullable()
    java.lang.String pendingKey, @org.jetbrains.annotations.Nullable()
    java.lang.String manualBaseUrl, boolean isCameraMenuExpanded, boolean isScriptMenuExpanded) {
        super();
    }
    
    public final boolean isLoading() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.oneview.model.network.BoxSystemInfo getInfo() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.oneview.model.network.BoxSystemState getState() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.oneview.model.network.SystemSettingResponse getSystemSetting() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.tenli.oneview.model.network.MonitorType> getMonitorTypes() {
        return null;
    }
    
    public final int getSelectedAiCategory() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.oneview.model.network.MonitorDisplayItem getEditingMonitorItem() {
        return null;
    }
    
    public final int getMonitorUpdateTicket() {
        return 0;
    }
    
    public final boolean isAddDeviceMenuExpanded() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.tenli.oneview.ui.features.setting.core.DeviceGroupDisplay> getDeviceGroups() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.oneview.model.network.DeviceItem getSelectedDeviceItem() {
        return null;
    }
    
    public final boolean isScanningLan() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.tenli.oneview.model.network.LanDevice> getLanDevices() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getVerifiedKey() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getPendingKey() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getManualBaseUrl() {
        return null;
    }
    
    public final boolean isCameraMenuExpanded() {
        return false;
    }
    
    public final boolean isScriptMenuExpanded() {
        return false;
    }
    
    public BoxUiState() {
        super();
    }
}