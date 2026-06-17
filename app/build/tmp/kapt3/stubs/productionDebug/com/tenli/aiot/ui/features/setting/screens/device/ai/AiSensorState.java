package com.tenli.aiot.ui.features.setting.screens.device.ai;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000X\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0006\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010$\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\"\n\u0002\bD\b\u0087\b\u0018\u00002\u00020\u0001B\u00d5\u0002\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\u000e\b\u0002\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u0012\b\b\u0002\u0010\u0007\u001a\u00020\b\u0012\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\u0006\u0012\u000e\b\u0002\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0005\u0012\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u000b\u0012\b\b\u0002\u0010\r\u001a\u00020\b\u0012\b\b\u0002\u0010\u000e\u001a\u00020\u0003\u0012\u0014\b\u0002\u0010\u000f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00100\u00050\u0005\u0012\u0014\b\u0002\u0010\u0011\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00100\u00050\u0005\u0012\u000e\b\u0002\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00130\u0005\u0012\b\b\u0002\u0010\u0014\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0015\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0016\u001a\u00020\b\u0012\b\b\u0002\u0010\u0017\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0018\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0019\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u001a\u001a\u00020\u001b\u0012\b\b\u0002\u0010\u001c\u001a\u00020\u001b\u0012\u0016\b\u0002\u0010\u001d\u001a\u0010\u0012\u0004\u0012\u00020\u001b\u0012\u0006\u0012\u0004\u0018\u00010\u00010\u001e\u0012\b\b\u0002\u0010\u001f\u001a\u00020\u0003\u0012\n\b\u0002\u0010 \u001a\u0004\u0018\u00010\b\u0012\n\b\u0002\u0010!\u001a\u0004\u0018\u00010\b\u0012\b\b\u0002\u0010\"\u001a\u00020\u0003\u0012\u000e\b\u0002\u0010#\u001a\b\u0012\u0004\u0012\u00020$0\u0005\u0012\u000e\b\u0002\u0010%\u001a\b\u0012\u0004\u0012\u00020\b0&\u00a2\u0006\u0002\u0010\'J\t\u0010J\u001a\u00020\u0003H\u00c6\u0003J\u0015\u0010K\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00100\u00050\u0005H\u00c6\u0003J\u000f\u0010L\u001a\b\u0012\u0004\u0012\u00020\u00130\u0005H\u00c6\u0003J\t\u0010M\u001a\u00020\u0003H\u00c6\u0003J\t\u0010N\u001a\u00020\u0003H\u00c6\u0003J\t\u0010O\u001a\u00020\bH\u00c6\u0003J\t\u0010P\u001a\u00020\u0003H\u00c6\u0003J\t\u0010Q\u001a\u00020\u0003H\u00c6\u0003J\t\u0010R\u001a\u00020\u0003H\u00c6\u0003J\t\u0010S\u001a\u00020\u001bH\u00c6\u0003J\t\u0010T\u001a\u00020\u001bH\u00c6\u0003J\u000f\u0010U\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005H\u00c6\u0003J\u0017\u0010V\u001a\u0010\u0012\u0004\u0012\u00020\u001b\u0012\u0006\u0012\u0004\u0018\u00010\u00010\u001eH\u00c6\u0003J\t\u0010W\u001a\u00020\u0003H\u00c6\u0003J\u0010\u0010X\u001a\u0004\u0018\u00010\bH\u00c6\u0003\u00a2\u0006\u0002\u00105J\u0010\u0010Y\u001a\u0004\u0018\u00010\bH\u00c6\u0003\u00a2\u0006\u0002\u00105J\t\u0010Z\u001a\u00020\u0003H\u00c6\u0003J\u000f\u0010[\u001a\b\u0012\u0004\u0012\u00020$0\u0005H\u00c6\u0003J\u000f\u0010\\\u001a\b\u0012\u0004\u0012\u00020\b0&H\u00c6\u0003J\t\u0010]\u001a\u00020\bH\u00c6\u0003J\u000b\u0010^\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\u000f\u0010_\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0005H\u00c6\u0003J\u000b\u0010`\u001a\u0004\u0018\u00010\u000bH\u00c6\u0003J\t\u0010a\u001a\u00020\bH\u00c6\u0003J\t\u0010b\u001a\u00020\u0003H\u00c6\u0003J\u0015\u0010c\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00100\u00050\u0005H\u00c6\u0003J\u00de\u0002\u0010d\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\u000e\b\u0002\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\b\b\u0002\u0010\u0007\u001a\u00020\b2\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\u00062\u000e\b\u0002\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u000b0\u00052\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u000b2\b\b\u0002\u0010\r\u001a\u00020\b2\b\b\u0002\u0010\u000e\u001a\u00020\u00032\u0014\b\u0002\u0010\u000f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00100\u00050\u00052\u0014\b\u0002\u0010\u0011\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00100\u00050\u00052\u000e\b\u0002\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00130\u00052\b\b\u0002\u0010\u0014\u001a\u00020\u00032\b\b\u0002\u0010\u0015\u001a\u00020\u00032\b\b\u0002\u0010\u0016\u001a\u00020\b2\b\b\u0002\u0010\u0017\u001a\u00020\u00032\b\b\u0002\u0010\u0018\u001a\u00020\u00032\b\b\u0002\u0010\u0019\u001a\u00020\u00032\b\b\u0002\u0010\u001a\u001a\u00020\u001b2\b\b\u0002\u0010\u001c\u001a\u00020\u001b2\u0016\b\u0002\u0010\u001d\u001a\u0010\u0012\u0004\u0012\u00020\u001b\u0012\u0006\u0012\u0004\u0018\u00010\u00010\u001e2\b\b\u0002\u0010\u001f\u001a\u00020\u00032\n\b\u0002\u0010 \u001a\u0004\u0018\u00010\b2\n\b\u0002\u0010!\u001a\u0004\u0018\u00010\b2\b\b\u0002\u0010\"\u001a\u00020\u00032\u000e\b\u0002\u0010#\u001a\b\u0012\u0004\u0012\u00020$0\u00052\u000e\b\u0002\u0010%\u001a\b\u0012\u0004\u0012\u00020\b0&H\u00c6\u0001\u00a2\u0006\u0002\u0010eJ\u0013\u0010f\u001a\u00020\u00032\b\u0010g\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010h\u001a\u00020\bH\u00d6\u0001J\t\u0010i\u001a\u00020\u001bH\u00d6\u0001R\u0011\u0010\u0014\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b(\u0010)R\u0011\u0010\u000e\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b*\u0010)R\u0011\u0010\u0015\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b+\u0010)R\u0017\u0010#\u001a\b\u0012\u0004\u0012\u00020$0\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b,\u0010-R\u0017\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b.\u0010-R\u0017\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00130\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b/\u0010-R\u0011\u0010\r\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b0\u00101R\u001f\u0010\u001d\u001a\u0010\u0012\u0004\u0012\u00020\u001b\u0012\u0006\u0012\u0004\u0018\u00010\u00010\u001e\u00a2\u0006\b\n\u0000\u001a\u0004\b2\u00103R\u0015\u0010!\u001a\u0004\u0018\u00010\b\u00a2\u0006\n\n\u0002\u00106\u001a\u0004\b4\u00105R\u0015\u0010 \u001a\u0004\u0018\u00010\b\u00a2\u0006\n\n\u0002\u00106\u001a\u0004\b7\u00105R\u001d\u0010\u0011\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00100\u00050\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b8\u0010-R\u001d\u0010\u000f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00100\u00050\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b9\u0010-R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0002\u0010)R\u0011\u0010\u001f\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010)R\u0011\u0010\u001a\u001a\u00020\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b:\u0010;R\u0017\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b<\u0010-R\u0011\u0010\u0017\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b=\u0010)R\u0011\u0010\u001c\u001a\u00020\u001b\u00a2\u0006\b\n\u0000\u001a\u0004\b>\u0010;R\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b?\u00101R\u0013\u0010\f\u001a\u0004\u0018\u00010\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b@\u0010AR\u0017\u0010%\u001a\b\u0012\u0004\u0012\u00020\b0&\u00a2\u0006\b\n\u0000\u001a\u0004\bB\u0010CR\u0013\u0010\t\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\bD\u0010ER\u0011\u0010\u0016\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\bF\u00101R\u0011\u0010\u0018\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bG\u0010)R\u0011\u0010\"\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bH\u0010)R\u0011\u0010\u0019\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\bI\u0010)\u00a8\u0006j"}, d2 = {"Lcom/tenli/aiot/ui/features/setting/screens/device/ai/AiSensorState;", "", "isLoading", "", "monitorTypes", "", "Lcom/tenli/aiot/model/network/MonitorType;", "selectedAiCategory", "", "selectedMonitorType", "cameras", "Lcom/tenli/aiot/model/network/CameraItem;", "selectedCamera", "currentWizardStep", "aiStatus", "includeMasks", "", "excludeMasks", "currentPoints", "Landroidx/compose/ui/geometry/Offset;", "aiEnabled", "alarmEnabled", "sensitivity", "mqttEnabled", "showBackConfirm", "showIncompleteZoneConfirm", "monitorName", "", "mqttTopic", "dynamicParams", "", "isLoadingParams", "editingMonitorId", "editingCameraId", "showDeleteConfirm", "availableInputSensors", "Lcom/tenli/aiot/model/network/MonitorDisplayItem;", "selectedInputSensorIds", "", "(ZLjava/util/List;ILcom/tenli/aiot/model/network/MonitorType;Ljava/util/List;Lcom/tenli/aiot/model/network/CameraItem;IZLjava/util/List;Ljava/util/List;Ljava/util/List;ZZIZZZLjava/lang/String;Ljava/lang/String;Ljava/util/Map;ZLjava/lang/Integer;Ljava/lang/Integer;ZLjava/util/List;Ljava/util/Set;)V", "getAiEnabled", "()Z", "getAiStatus", "getAlarmEnabled", "getAvailableInputSensors", "()Ljava/util/List;", "getCameras", "getCurrentPoints", "getCurrentWizardStep", "()I", "getDynamicParams", "()Ljava/util/Map;", "getEditingCameraId", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getEditingMonitorId", "getExcludeMasks", "getIncludeMasks", "getMonitorName", "()Ljava/lang/String;", "getMonitorTypes", "getMqttEnabled", "getMqttTopic", "getSelectedAiCategory", "getSelectedCamera", "()Lcom/tenli/aiot/model/network/CameraItem;", "getSelectedInputSensorIds", "()Ljava/util/Set;", "getSelectedMonitorType", "()Lcom/tenli/aiot/model/network/MonitorType;", "getSensitivity", "getShowBackConfirm", "getShowDeleteConfirm", "getShowIncompleteZoneConfirm", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component19", "component2", "component20", "component21", "component22", "component23", "component24", "component25", "component26", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(ZLjava/util/List;ILcom/tenli/aiot/model/network/MonitorType;Ljava/util/List;Lcom/tenli/aiot/model/network/CameraItem;IZLjava/util/List;Ljava/util/List;Ljava/util/List;ZZIZZZLjava/lang/String;Ljava/lang/String;Ljava/util/Map;ZLjava/lang/Integer;Ljava/lang/Integer;ZLjava/util/List;Ljava/util/Set;)Lcom/tenli/aiot/ui/features/setting/screens/device/ai/AiSensorState;", "equals", "other", "hashCode", "toString", "app_productionDebug"})
public final class AiSensorState {
    private final boolean isLoading = false;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.tenli.aiot.model.network.MonitorType> monitorTypes = null;
    private final int selectedAiCategory = 0;
    @org.jetbrains.annotations.Nullable()
    private final com.tenli.aiot.model.network.MonitorType selectedMonitorType = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.tenli.aiot.model.network.CameraItem> cameras = null;
    @org.jetbrains.annotations.Nullable()
    private final com.tenli.aiot.model.network.CameraItem selectedCamera = null;
    private final int currentWizardStep = 0;
    private final boolean aiStatus = false;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<java.util.List<java.lang.Double>> includeMasks = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<java.util.List<java.lang.Double>> excludeMasks = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<androidx.compose.ui.geometry.Offset> currentPoints = null;
    private final boolean aiEnabled = false;
    private final boolean alarmEnabled = false;
    private final int sensitivity = 0;
    private final boolean mqttEnabled = false;
    private final boolean showBackConfirm = false;
    private final boolean showIncompleteZoneConfirm = false;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String monitorName = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String mqttTopic = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Map<java.lang.String, java.lang.Object> dynamicParams = null;
    private final boolean isLoadingParams = false;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer editingMonitorId = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer editingCameraId = null;
    private final boolean showDeleteConfirm = false;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.tenli.aiot.model.network.MonitorDisplayItem> availableInputSensors = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Set<java.lang.Integer> selectedInputSensorIds = null;
    
    public final boolean component1() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.util.List<java.lang.Double>> component10() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<androidx.compose.ui.geometry.Offset> component11() {
        return null;
    }
    
    public final boolean component12() {
        return false;
    }
    
    public final boolean component13() {
        return false;
    }
    
    public final int component14() {
        return 0;
    }
    
    public final boolean component15() {
        return false;
    }
    
    public final boolean component16() {
        return false;
    }
    
    public final boolean component17() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component18() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component19() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.tenli.aiot.model.network.MonitorType> component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<java.lang.String, java.lang.Object> component20() {
        return null;
    }
    
    public final boolean component21() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component22() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component23() {
        return null;
    }
    
    public final boolean component24() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.tenli.aiot.model.network.MonitorDisplayItem> component25() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Set<java.lang.Integer> component26() {
        return null;
    }
    
    public final int component3() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.aiot.model.network.MonitorType component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.tenli.aiot.model.network.CameraItem> component5() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.aiot.model.network.CameraItem component6() {
        return null;
    }
    
    public final int component7() {
        return 0;
    }
    
    public final boolean component8() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.util.List<java.lang.Double>> component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.aiot.ui.features.setting.screens.device.ai.AiSensorState copy(boolean isLoading, @org.jetbrains.annotations.NotNull()
    java.util.List<com.tenli.aiot.model.network.MonitorType> monitorTypes, int selectedAiCategory, @org.jetbrains.annotations.Nullable()
    com.tenli.aiot.model.network.MonitorType selectedMonitorType, @org.jetbrains.annotations.NotNull()
    java.util.List<com.tenli.aiot.model.network.CameraItem> cameras, @org.jetbrains.annotations.Nullable()
    com.tenli.aiot.model.network.CameraItem selectedCamera, int currentWizardStep, boolean aiStatus, @org.jetbrains.annotations.NotNull()
    java.util.List<? extends java.util.List<java.lang.Double>> includeMasks, @org.jetbrains.annotations.NotNull()
    java.util.List<? extends java.util.List<java.lang.Double>> excludeMasks, @org.jetbrains.annotations.NotNull()
    java.util.List<androidx.compose.ui.geometry.Offset> currentPoints, boolean aiEnabled, boolean alarmEnabled, int sensitivity, boolean mqttEnabled, boolean showBackConfirm, boolean showIncompleteZoneConfirm, @org.jetbrains.annotations.NotNull()
    java.lang.String monitorName, @org.jetbrains.annotations.NotNull()
    java.lang.String mqttTopic, @org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.String, ? extends java.lang.Object> dynamicParams, boolean isLoadingParams, @org.jetbrains.annotations.Nullable()
    java.lang.Integer editingMonitorId, @org.jetbrains.annotations.Nullable()
    java.lang.Integer editingCameraId, boolean showDeleteConfirm, @org.jetbrains.annotations.NotNull()
    java.util.List<com.tenli.aiot.model.network.MonitorDisplayItem> availableInputSensors, @org.jetbrains.annotations.NotNull()
    java.util.Set<java.lang.Integer> selectedInputSensorIds) {
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
    
    public AiSensorState(boolean isLoading, @org.jetbrains.annotations.NotNull()
    java.util.List<com.tenli.aiot.model.network.MonitorType> monitorTypes, int selectedAiCategory, @org.jetbrains.annotations.Nullable()
    com.tenli.aiot.model.network.MonitorType selectedMonitorType, @org.jetbrains.annotations.NotNull()
    java.util.List<com.tenli.aiot.model.network.CameraItem> cameras, @org.jetbrains.annotations.Nullable()
    com.tenli.aiot.model.network.CameraItem selectedCamera, int currentWizardStep, boolean aiStatus, @org.jetbrains.annotations.NotNull()
    java.util.List<? extends java.util.List<java.lang.Double>> includeMasks, @org.jetbrains.annotations.NotNull()
    java.util.List<? extends java.util.List<java.lang.Double>> excludeMasks, @org.jetbrains.annotations.NotNull()
    java.util.List<androidx.compose.ui.geometry.Offset> currentPoints, boolean aiEnabled, boolean alarmEnabled, int sensitivity, boolean mqttEnabled, boolean showBackConfirm, boolean showIncompleteZoneConfirm, @org.jetbrains.annotations.NotNull()
    java.lang.String monitorName, @org.jetbrains.annotations.NotNull()
    java.lang.String mqttTopic, @org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.String, ? extends java.lang.Object> dynamicParams, boolean isLoadingParams, @org.jetbrains.annotations.Nullable()
    java.lang.Integer editingMonitorId, @org.jetbrains.annotations.Nullable()
    java.lang.Integer editingCameraId, boolean showDeleteConfirm, @org.jetbrains.annotations.NotNull()
    java.util.List<com.tenli.aiot.model.network.MonitorDisplayItem> availableInputSensors, @org.jetbrains.annotations.NotNull()
    java.util.Set<java.lang.Integer> selectedInputSensorIds) {
        super();
    }
    
    public final boolean isLoading() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.tenli.aiot.model.network.MonitorType> getMonitorTypes() {
        return null;
    }
    
    public final int getSelectedAiCategory() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.aiot.model.network.MonitorType getSelectedMonitorType() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.tenli.aiot.model.network.CameraItem> getCameras() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.aiot.model.network.CameraItem getSelectedCamera() {
        return null;
    }
    
    public final int getCurrentWizardStep() {
        return 0;
    }
    
    public final boolean getAiStatus() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.util.List<java.lang.Double>> getIncludeMasks() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.util.List<java.lang.Double>> getExcludeMasks() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<androidx.compose.ui.geometry.Offset> getCurrentPoints() {
        return null;
    }
    
    public final boolean getAiEnabled() {
        return false;
    }
    
    public final boolean getAlarmEnabled() {
        return false;
    }
    
    public final int getSensitivity() {
        return 0;
    }
    
    public final boolean getMqttEnabled() {
        return false;
    }
    
    public final boolean getShowBackConfirm() {
        return false;
    }
    
    public final boolean getShowIncompleteZoneConfirm() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getMonitorName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getMqttTopic() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<java.lang.String, java.lang.Object> getDynamicParams() {
        return null;
    }
    
    public final boolean isLoadingParams() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getEditingMonitorId() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getEditingCameraId() {
        return null;
    }
    
    public final boolean getShowDeleteConfirm() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.tenli.aiot.model.network.MonitorDisplayItem> getAvailableInputSensors() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Set<java.lang.Integer> getSelectedInputSensorIds() {
        return null;
    }
    
    public AiSensorState() {
        super();
    }
}