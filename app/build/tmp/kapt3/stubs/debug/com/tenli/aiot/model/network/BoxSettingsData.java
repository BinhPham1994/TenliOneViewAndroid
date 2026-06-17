package com.tenli.aiot.model.network;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u000b\n\u0002\u0010\u0006\n\u0002\b\u000b\n\u0002\u0010 \n\u0002\bT\b\u0087\b\u0018\u00002\u00020\u0001B\u008b\u0003\u0012\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u0012\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u0007\u0012\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u0007\u0012\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\u0013\u0012\n\b\u0002\u0010\u0014\u001a\u0004\u0018\u00010\u0013\u0012\n\b\u0002\u0010\u0015\u001a\u0004\u0018\u00010\u0013\u0012\n\b\u0002\u0010\u0016\u001a\u0004\u0018\u00010\u0013\u0012\n\b\u0002\u0010\u0017\u001a\u0004\u0018\u00010\u0013\u0012\n\b\u0002\u0010\u0018\u001a\u0004\u0018\u00010\u0013\u0012\n\b\u0002\u0010\u0019\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u001a\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u001b\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u001c\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u001d\u001a\u0004\u0018\u00010\u0005\u0012\u0010\b\u0002\u0010\u001e\u001a\n\u0012\u0004\u0012\u00020\u0005\u0018\u00010\u001f\u0012\u0010\b\u0002\u0010 \u001a\n\u0012\u0004\u0012\u00020\u0005\u0018\u00010\u001f\u0012\u0010\b\u0002\u0010!\u001a\n\u0012\u0004\u0012\u00020\u0005\u0018\u00010\u001f\u0012\n\b\u0002\u0010\"\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010#\u001a\u0004\u0018\u00010\u0007\u0012\n\b\u0002\u0010$\u001a\u0004\u0018\u00010\u0007\u0012\n\b\u0002\u0010%\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\u0002\u0010&J\u0010\u0010N\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u0010-J\u000b\u0010O\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u0010\u0010P\u001a\u0004\u0018\u00010\u0007H\u00c6\u0003\u00a2\u0006\u0002\u00105J\u0010\u0010Q\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u0010-J\u000b\u0010R\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u0010\u0010S\u001a\u0004\u0018\u00010\u0013H\u00c6\u0003\u00a2\u0006\u0002\u00100J\u0010\u0010T\u001a\u0004\u0018\u00010\u0013H\u00c6\u0003\u00a2\u0006\u0002\u00100J\u0010\u0010U\u001a\u0004\u0018\u00010\u0013H\u00c6\u0003\u00a2\u0006\u0002\u00100J\u0010\u0010V\u001a\u0004\u0018\u00010\u0013H\u00c6\u0003\u00a2\u0006\u0002\u00100J\u0010\u0010W\u001a\u0004\u0018\u00010\u0013H\u00c6\u0003\u00a2\u0006\u0002\u00100J\u0010\u0010X\u001a\u0004\u0018\u00010\u0013H\u00c6\u0003\u00a2\u0006\u0002\u00100J\u000b\u0010Y\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u0010\u0010Z\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u0010-J\u0010\u0010[\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u0010-J\u0010\u0010\\\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u0010-J\u0010\u0010]\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u0010-J\u000b\u0010^\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u0011\u0010_\u001a\n\u0012\u0004\u0012\u00020\u0005\u0018\u00010\u001fH\u00c6\u0003J\u0011\u0010`\u001a\n\u0012\u0004\u0012\u00020\u0005\u0018\u00010\u001fH\u00c6\u0003J\u0011\u0010a\u001a\n\u0012\u0004\u0012\u00020\u0005\u0018\u00010\u001fH\u00c6\u0003J\u0010\u0010b\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u0010-J\u0010\u0010c\u001a\u0004\u0018\u00010\u0007H\u00c6\u0003\u00a2\u0006\u0002\u00105J\u0010\u0010d\u001a\u0004\u0018\u00010\u0007H\u00c6\u0003\u00a2\u0006\u0002\u00105J\u0010\u0010e\u001a\u0004\u0018\u00010\u0007H\u00c6\u0003\u00a2\u0006\u0002\u00105J\u0010\u0010f\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u0010-J\u000b\u0010g\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010h\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010i\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010j\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u0010\u0010k\u001a\u0004\u0018\u00010\u0007H\u00c6\u0003\u00a2\u0006\u0002\u00105J\u0010\u0010l\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u0010-J\u0094\u0003\u0010m\u001a\u00020\u00002\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u00072\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u00072\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u00072\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\u00132\n\b\u0002\u0010\u0014\u001a\u0004\u0018\u00010\u00132\n\b\u0002\u0010\u0015\u001a\u0004\u0018\u00010\u00132\n\b\u0002\u0010\u0016\u001a\u0004\u0018\u00010\u00132\n\b\u0002\u0010\u0017\u001a\u0004\u0018\u00010\u00132\n\b\u0002\u0010\u0018\u001a\u0004\u0018\u00010\u00132\n\b\u0002\u0010\u0019\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u001a\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u001b\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u001c\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u001d\u001a\u0004\u0018\u00010\u00052\u0010\b\u0002\u0010\u001e\u001a\n\u0012\u0004\u0012\u00020\u0005\u0018\u00010\u001f2\u0010\b\u0002\u0010 \u001a\n\u0012\u0004\u0012\u00020\u0005\u0018\u00010\u001f2\u0010\b\u0002\u0010!\u001a\n\u0012\u0004\u0012\u00020\u0005\u0018\u00010\u001f2\n\b\u0002\u0010\"\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010#\u001a\u0004\u0018\u00010\u00072\n\b\u0002\u0010$\u001a\u0004\u0018\u00010\u00072\n\b\u0002\u0010%\u001a\u0004\u0018\u00010\u0003H\u00c6\u0001\u00a2\u0006\u0002\u0010nJ\u0013\u0010o\u001a\u00020\u00032\b\u0010p\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010q\u001a\u00020\u0007H\u00d6\u0001J\t\u0010r\u001a\u00020\u0005H\u00d6\u0001R\u0013\u0010\u001d\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010(R\u0019\u0010 \u001a\n\u0012\u0004\u0012\u00020\u0005\u0018\u00010\u001f\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010*R\u0019\u0010\u001e\u001a\n\u0012\u0004\u0012\u00020\u0005\u0018\u00010\u001f\u00a2\u0006\b\n\u0000\u001a\u0004\b+\u0010*R\u0015\u0010\"\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\n\n\u0002\u0010.\u001a\u0004\b,\u0010-R\u0015\u0010\u0016\u001a\u0004\u0018\u00010\u0013\u00a2\u0006\n\n\u0002\u00101\u001a\u0004\b/\u00100R\u0015\u0010\u0017\u001a\u0004\u0018\u00010\u0013\u00a2\u0006\n\n\u0002\u00101\u001a\u0004\b2\u00100R\u0015\u0010\u0015\u001a\u0004\u0018\u00010\u0013\u00a2\u0006\n\n\u0002\u00101\u001a\u0004\b3\u00100R\u0015\u0010$\u001a\u0004\u0018\u00010\u0007\u00a2\u0006\n\n\u0002\u00106\u001a\u0004\b4\u00105R\u0015\u0010%\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\n\n\u0002\u0010.\u001a\u0004\b7\u0010-R\u0019\u0010!\u001a\n\u0012\u0004\u0012\u00020\u0005\u0018\u00010\u001f\u00a2\u0006\b\n\u0000\u001a\u0004\b8\u0010*R\u0013\u0010\b\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b9\u0010(R\u0015\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\n\n\u0002\u0010.\u001a\u0004\b:\u0010-R\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b;\u0010(R\u0013\u0010\n\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b<\u0010(R\u0015\u0010\r\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\n\n\u0002\u0010.\u001a\u0004\b=\u0010-R\u0015\u0010\u0012\u001a\u0004\u0018\u00010\u0013\u00a2\u0006\n\n\u0002\u00101\u001a\u0004\b>\u00100R\u0013\u0010\u0011\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b?\u0010(R\u0015\u0010\u000f\u001a\u0004\u0018\u00010\u0007\u00a2\u0006\n\n\u0002\u00106\u001a\u0004\b@\u00105R\u0015\u0010\u0010\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\n\n\u0002\u0010.\u001a\u0004\bA\u0010-R\u0013\u0010\u000e\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\bB\u0010(R\u0015\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u00a2\u0006\n\n\u0002\u00106\u001a\u0004\bC\u00105R\u0013\u0010\u000b\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\bD\u0010(R\u0015\u0010\f\u001a\u0004\u0018\u00010\u0007\u00a2\u0006\n\n\u0002\u00106\u001a\u0004\bE\u00105R\u0013\u0010\t\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\bF\u0010(R\u0015\u0010#\u001a\u0004\u0018\u00010\u0007\u00a2\u0006\n\n\u0002\u00106\u001a\u0004\bG\u00105R\u0015\u0010\u001a\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\n\n\u0002\u0010.\u001a\u0004\bH\u0010-R\u0015\u0010\u0019\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\n\n\u0002\u0010.\u001a\u0004\bI\u0010-R\u0015\u0010\u0018\u001a\u0004\u0018\u00010\u0013\u00a2\u0006\n\n\u0002\u00101\u001a\u0004\bJ\u00100R\u0015\u0010\u001c\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\n\n\u0002\u0010.\u001a\u0004\bK\u0010-R\u0015\u0010\u001b\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\n\n\u0002\u0010.\u001a\u0004\bL\u0010-R\u0015\u0010\u0014\u001a\u0004\u0018\u00010\u0013\u00a2\u0006\n\n\u0002\u00101\u001a\u0004\bM\u00100\u00a8\u0006s"}, d2 = {"Lcom/tenli/aiot/model/network/BoxSettingsData;", "", "mqttEnabled", "", "mqttHost", "", "mqttPort", "", "mqttClientId", "mqttUser", "mqttPassword", "mqttRootTopic", "mqttSubQoS", "mqttPingEnabled", "mqttPingTopic", "mqttPingQoS", "mqttPingRetain", "mqttPingMessage", "mqttPingInterval", "", "videoRetainDays", "imageRetainDays", "dataRetainDays", "diskFullPercent", "reportRetainDays", "reportMonitorState", "reportCameraState", "reportUpdateEvent", "reportSystemStatus", "alarmAudio", "alarmOn", "", "alarmOff", "location", "cameraAuthorizedCheck", "numberReconnectAuthorized", "keepDataCount", "keepDataMedia", "(Ljava/lang/Boolean;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Boolean;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Boolean;Ljava/lang/String;Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Boolean;Ljava/lang/Boolean;Ljava/lang/Boolean;Ljava/lang/Boolean;Ljava/lang/String;Ljava/util/List;Ljava/util/List;Ljava/util/List;Ljava/lang/Boolean;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Boolean;)V", "getAlarmAudio", "()Ljava/lang/String;", "getAlarmOff", "()Ljava/util/List;", "getAlarmOn", "getCameraAuthorizedCheck", "()Ljava/lang/Boolean;", "Ljava/lang/Boolean;", "getDataRetainDays", "()Ljava/lang/Double;", "Ljava/lang/Double;", "getDiskFullPercent", "getImageRetainDays", "getKeepDataCount", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getKeepDataMedia", "getLocation", "getMqttClientId", "getMqttEnabled", "getMqttHost", "getMqttPassword", "getMqttPingEnabled", "getMqttPingInterval", "getMqttPingMessage", "getMqttPingQoS", "getMqttPingRetain", "getMqttPingTopic", "getMqttPort", "getMqttRootTopic", "getMqttSubQoS", "getMqttUser", "getNumberReconnectAuthorized", "getReportCameraState", "getReportMonitorState", "getReportRetainDays", "getReportSystemStatus", "getReportUpdateEvent", "getVideoRetainDays", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component19", "component2", "component20", "component21", "component22", "component23", "component24", "component25", "component26", "component27", "component28", "component29", "component3", "component30", "component31", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(Ljava/lang/Boolean;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Boolean;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Boolean;Ljava/lang/String;Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Double;Ljava/lang/Boolean;Ljava/lang/Boolean;Ljava/lang/Boolean;Ljava/lang/Boolean;Ljava/lang/String;Ljava/util/List;Ljava/util/List;Ljava/util/List;Ljava/lang/Boolean;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Boolean;)Lcom/tenli/aiot/model/network/BoxSettingsData;", "equals", "other", "hashCode", "toString", "app_debug"})
public final class BoxSettingsData {
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Boolean mqttEnabled = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String mqttHost = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer mqttPort = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String mqttClientId = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String mqttUser = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String mqttPassword = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String mqttRootTopic = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer mqttSubQoS = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Boolean mqttPingEnabled = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String mqttPingTopic = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer mqttPingQoS = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Boolean mqttPingRetain = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String mqttPingMessage = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Double mqttPingInterval = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Double videoRetainDays = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Double imageRetainDays = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Double dataRetainDays = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Double diskFullPercent = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Double reportRetainDays = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Boolean reportMonitorState = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Boolean reportCameraState = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Boolean reportUpdateEvent = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Boolean reportSystemStatus = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String alarmAudio = null;
    @org.jetbrains.annotations.Nullable()
    private final java.util.List<java.lang.String> alarmOn = null;
    @org.jetbrains.annotations.Nullable()
    private final java.util.List<java.lang.String> alarmOff = null;
    @org.jetbrains.annotations.Nullable()
    private final java.util.List<java.lang.String> location = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Boolean cameraAuthorizedCheck = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer numberReconnectAuthorized = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer keepDataCount = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Boolean keepDataMedia = null;
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Boolean component1() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component10() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component11() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Boolean component12() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component13() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double component14() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double component15() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double component16() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double component17() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double component18() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double component19() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Boolean component20() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Boolean component21() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Boolean component22() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Boolean component23() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component24() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<java.lang.String> component25() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<java.lang.String> component26() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<java.lang.String> component27() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Boolean component28() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component29() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component30() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Boolean component31() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component5() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component6() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component7() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component8() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Boolean component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.aiot.model.network.BoxSettingsData copy(@org.jetbrains.annotations.Nullable()
    java.lang.Boolean mqttEnabled, @org.jetbrains.annotations.Nullable()
    java.lang.String mqttHost, @org.jetbrains.annotations.Nullable()
    java.lang.Integer mqttPort, @org.jetbrains.annotations.Nullable()
    java.lang.String mqttClientId, @org.jetbrains.annotations.Nullable()
    java.lang.String mqttUser, @org.jetbrains.annotations.Nullable()
    java.lang.String mqttPassword, @org.jetbrains.annotations.Nullable()
    java.lang.String mqttRootTopic, @org.jetbrains.annotations.Nullable()
    java.lang.Integer mqttSubQoS, @org.jetbrains.annotations.Nullable()
    java.lang.Boolean mqttPingEnabled, @org.jetbrains.annotations.Nullable()
    java.lang.String mqttPingTopic, @org.jetbrains.annotations.Nullable()
    java.lang.Integer mqttPingQoS, @org.jetbrains.annotations.Nullable()
    java.lang.Boolean mqttPingRetain, @org.jetbrains.annotations.Nullable()
    java.lang.String mqttPingMessage, @org.jetbrains.annotations.Nullable()
    java.lang.Double mqttPingInterval, @org.jetbrains.annotations.Nullable()
    java.lang.Double videoRetainDays, @org.jetbrains.annotations.Nullable()
    java.lang.Double imageRetainDays, @org.jetbrains.annotations.Nullable()
    java.lang.Double dataRetainDays, @org.jetbrains.annotations.Nullable()
    java.lang.Double diskFullPercent, @org.jetbrains.annotations.Nullable()
    java.lang.Double reportRetainDays, @org.jetbrains.annotations.Nullable()
    java.lang.Boolean reportMonitorState, @org.jetbrains.annotations.Nullable()
    java.lang.Boolean reportCameraState, @org.jetbrains.annotations.Nullable()
    java.lang.Boolean reportUpdateEvent, @org.jetbrains.annotations.Nullable()
    java.lang.Boolean reportSystemStatus, @org.jetbrains.annotations.Nullable()
    java.lang.String alarmAudio, @org.jetbrains.annotations.Nullable()
    java.util.List<java.lang.String> alarmOn, @org.jetbrains.annotations.Nullable()
    java.util.List<java.lang.String> alarmOff, @org.jetbrains.annotations.Nullable()
    java.util.List<java.lang.String> location, @org.jetbrains.annotations.Nullable()
    java.lang.Boolean cameraAuthorizedCheck, @org.jetbrains.annotations.Nullable()
    java.lang.Integer numberReconnectAuthorized, @org.jetbrains.annotations.Nullable()
    java.lang.Integer keepDataCount, @org.jetbrains.annotations.Nullable()
    java.lang.Boolean keepDataMedia) {
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
    
    public BoxSettingsData(@org.jetbrains.annotations.Nullable()
    java.lang.Boolean mqttEnabled, @org.jetbrains.annotations.Nullable()
    java.lang.String mqttHost, @org.jetbrains.annotations.Nullable()
    java.lang.Integer mqttPort, @org.jetbrains.annotations.Nullable()
    java.lang.String mqttClientId, @org.jetbrains.annotations.Nullable()
    java.lang.String mqttUser, @org.jetbrains.annotations.Nullable()
    java.lang.String mqttPassword, @org.jetbrains.annotations.Nullable()
    java.lang.String mqttRootTopic, @org.jetbrains.annotations.Nullable()
    java.lang.Integer mqttSubQoS, @org.jetbrains.annotations.Nullable()
    java.lang.Boolean mqttPingEnabled, @org.jetbrains.annotations.Nullable()
    java.lang.String mqttPingTopic, @org.jetbrains.annotations.Nullable()
    java.lang.Integer mqttPingQoS, @org.jetbrains.annotations.Nullable()
    java.lang.Boolean mqttPingRetain, @org.jetbrains.annotations.Nullable()
    java.lang.String mqttPingMessage, @org.jetbrains.annotations.Nullable()
    java.lang.Double mqttPingInterval, @org.jetbrains.annotations.Nullable()
    java.lang.Double videoRetainDays, @org.jetbrains.annotations.Nullable()
    java.lang.Double imageRetainDays, @org.jetbrains.annotations.Nullable()
    java.lang.Double dataRetainDays, @org.jetbrains.annotations.Nullable()
    java.lang.Double diskFullPercent, @org.jetbrains.annotations.Nullable()
    java.lang.Double reportRetainDays, @org.jetbrains.annotations.Nullable()
    java.lang.Boolean reportMonitorState, @org.jetbrains.annotations.Nullable()
    java.lang.Boolean reportCameraState, @org.jetbrains.annotations.Nullable()
    java.lang.Boolean reportUpdateEvent, @org.jetbrains.annotations.Nullable()
    java.lang.Boolean reportSystemStatus, @org.jetbrains.annotations.Nullable()
    java.lang.String alarmAudio, @org.jetbrains.annotations.Nullable()
    java.util.List<java.lang.String> alarmOn, @org.jetbrains.annotations.Nullable()
    java.util.List<java.lang.String> alarmOff, @org.jetbrains.annotations.Nullable()
    java.util.List<java.lang.String> location, @org.jetbrains.annotations.Nullable()
    java.lang.Boolean cameraAuthorizedCheck, @org.jetbrains.annotations.Nullable()
    java.lang.Integer numberReconnectAuthorized, @org.jetbrains.annotations.Nullable()
    java.lang.Integer keepDataCount, @org.jetbrains.annotations.Nullable()
    java.lang.Boolean keepDataMedia) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Boolean getMqttEnabled() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getMqttHost() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getMqttPort() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getMqttClientId() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getMqttUser() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getMqttPassword() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getMqttRootTopic() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getMqttSubQoS() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Boolean getMqttPingEnabled() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getMqttPingTopic() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getMqttPingQoS() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Boolean getMqttPingRetain() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getMqttPingMessage() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double getMqttPingInterval() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double getVideoRetainDays() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double getImageRetainDays() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double getDataRetainDays() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double getDiskFullPercent() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Double getReportRetainDays() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Boolean getReportMonitorState() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Boolean getReportCameraState() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Boolean getReportUpdateEvent() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Boolean getReportSystemStatus() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getAlarmAudio() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<java.lang.String> getAlarmOn() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<java.lang.String> getAlarmOff() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<java.lang.String> getLocation() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Boolean getCameraAuthorizedCheck() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getNumberReconnectAuthorized() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getKeepDataCount() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Boolean getKeepDataMedia() {
        return null;
    }
    
    public BoxSettingsData() {
        super();
    }
}