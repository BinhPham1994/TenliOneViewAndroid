package com.tenli.aiot.model.network;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\t\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b5\b\u0087\b\u0018\u00002\u00020\u0001B\u00bd\u0001\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0007\u001a\u00020\u0005\u0012\b\b\u0002\u0010\b\u001a\u00020\u0005\u0012\b\b\u0002\u0010\t\u001a\u00020\u0005\u0012\b\b\u0002\u0010\n\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u000b\u001a\u00020\u0003\u0012\b\b\u0002\u0010\f\u001a\u00020\u0003\u0012\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u0005\u0012\u0010\b\u0002\u0010\u000e\u001a\n\u0012\u0004\u0012\u00020\u0010\u0018\u00010\u000f\u0012\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\u0012\u0012\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\u0014\u0012\n\b\u0002\u0010\u0015\u001a\u0004\u0018\u00010\u0016\u0012\u0010\b\u0002\u0010\u0017\u001a\n\u0012\u0004\u0012\u00020\u0018\u0018\u00010\u000f\u0012\b\b\u0002\u0010\u0019\u001a\u00020\u001a\u00a2\u0006\u0002\u0010\u001bJ\t\u0010:\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010;\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u0011\u0010<\u001a\n\u0012\u0004\u0012\u00020\u0010\u0018\u00010\u000fH\u00c6\u0003J\u000b\u0010=\u001a\u0004\u0018\u00010\u0012H\u00c6\u0003J\u000b\u0010>\u001a\u0004\u0018\u00010\u0014H\u00c6\u0003J\u000b\u0010?\u001a\u0004\u0018\u00010\u0016H\u00c6\u0003J\u0011\u0010@\u001a\n\u0012\u0004\u0012\u00020\u0018\u0018\u00010\u000fH\u00c6\u0003J\t\u0010A\u001a\u00020\u001aH\u00c6\u0003J\t\u0010B\u001a\u00020\u0005H\u00c6\u0003J\t\u0010C\u001a\u00020\u0005H\u00c6\u0003J\t\u0010D\u001a\u00020\u0005H\u00c6\u0003J\t\u0010E\u001a\u00020\u0005H\u00c6\u0003J\t\u0010F\u001a\u00020\u0005H\u00c6\u0003J\t\u0010G\u001a\u00020\u0003H\u00c6\u0003J\t\u0010H\u001a\u00020\u0003H\u00c6\u0003J\t\u0010I\u001a\u00020\u0003H\u00c6\u0003J\u00c1\u0001\u0010J\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00052\b\b\u0002\u0010\u0007\u001a\u00020\u00052\b\b\u0002\u0010\b\u001a\u00020\u00052\b\b\u0002\u0010\t\u001a\u00020\u00052\b\b\u0002\u0010\n\u001a\u00020\u00032\b\b\u0002\u0010\u000b\u001a\u00020\u00032\b\b\u0002\u0010\f\u001a\u00020\u00032\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u00052\u0010\b\u0002\u0010\u000e\u001a\n\u0012\u0004\u0012\u00020\u0010\u0018\u00010\u000f2\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\u00122\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\u00142\n\b\u0002\u0010\u0015\u001a\u0004\u0018\u00010\u00162\u0010\b\u0002\u0010\u0017\u001a\n\u0012\u0004\u0012\u00020\u0018\u0018\u00010\u000f2\b\b\u0002\u0010\u0019\u001a\u00020\u001aH\u00c6\u0001J\u0013\u0010K\u001a\u00020\u001a2\b\u0010L\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010M\u001a\u00020\u0003H\u00d6\u0001J\t\u0010N\u001a\u00020\u0005H\u00d6\u0001R\u0011\u0010\u001c\u001a\u00020\u00038F\u00a2\u0006\u0006\u001a\u0004\b\u001d\u0010\u001eR\u001a\u0010\u0019\u001a\u00020\u001aX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001f\u0010 \"\u0004\b!\u0010\"R\u0011\u0010\u000b\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010\u001eR\u0011\u0010\u0007\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010%R\u0013\u0010\u0013\u001a\u0004\u0018\u00010\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\b&\u0010\'R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b(\u0010\u001eR\u0013\u0010\u0011\u001a\u0004\u0018\u00010\u0012\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010*R\u0011\u0010+\u001a\u00020\u00038F\u00a2\u0006\u0006\u001a\u0004\b,\u0010\u001eR\u0011\u0010-\u001a\u00020\u001a8F\u00a2\u0006\u0006\u001a\u0004\b-\u0010 R\u0011\u0010\b\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b.\u0010%R\u0013\u0010\u0015\u001a\u0004\u0018\u00010\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b/\u00100R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b1\u0010%R\u0019\u0010\u000e\u001a\n\u0012\u0004\u0012\u00020\u0010\u0018\u00010\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b2\u00103R\u0013\u0010\r\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b4\u0010%R\u0011\u0010\t\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b5\u0010%R\u0019\u0010\u0017\u001a\n\u0012\u0004\u0012\u00020\u0018\u0018\u00010\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b6\u00103R\u0011\u0010\n\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b7\u0010\u001eR\u0011\u0010\f\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b8\u0010\u001eR\u0011\u0010\u0006\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b9\u0010%\u00a8\u0006O"}, d2 = {"Lcom/tenli/aiot/model/network/DeviceItem;", "", "id", "", "name", "", "uuid", "deviceCode", "key", "publicTargetURI", "status", "connectState", "userGroupId", "postURL", "postHeaders", "", "Lcom/tenli/aiot/model/network/PostHeader;", "info", "Lcom/tenli/aiot/model/network/DevInfo;", "extendedData", "Lcom/tenli/aiot/model/network/ExtendedData;", "mqttInfo", "Lcom/tenli/aiot/model/network/MqttInfo;", "roles", "Lcom/tenli/aiot/model/network/RoleDevice;", "checked", "", "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IIILjava/lang/String;Ljava/util/List;Lcom/tenli/aiot/model/network/DevInfo;Lcom/tenli/aiot/model/network/ExtendedData;Lcom/tenli/aiot/model/network/MqttInfo;Ljava/util/List;Z)V", "cameraCount", "getCameraCount", "()I", "getChecked", "()Z", "setChecked", "(Z)V", "getConnectState", "getDeviceCode", "()Ljava/lang/String;", "getExtendedData", "()Lcom/tenli/aiot/model/network/ExtendedData;", "getId", "getInfo", "()Lcom/tenli/aiot/model/network/DevInfo;", "iotCount", "getIotCount", "isOnline", "getKey", "getMqttInfo", "()Lcom/tenli/aiot/model/network/MqttInfo;", "getName", "getPostHeaders", "()Ljava/util/List;", "getPostURL", "getPublicTargetURI", "getRoles", "getStatus", "getUserGroupId", "getUuid", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "hashCode", "toString", "app_productionDebug"})
public final class DeviceItem {
    private final int id = 0;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String name = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String uuid = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String deviceCode = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String key = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String publicTargetURI = null;
    private final int status = 0;
    private final int connectState = 0;
    private final int userGroupId = 0;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String postURL = null;
    @org.jetbrains.annotations.Nullable()
    private final java.util.List<com.tenli.aiot.model.network.PostHeader> postHeaders = null;
    @org.jetbrains.annotations.Nullable()
    private final com.tenli.aiot.model.network.DevInfo info = null;
    @org.jetbrains.annotations.Nullable()
    private final com.tenli.aiot.model.network.ExtendedData extendedData = null;
    @org.jetbrains.annotations.Nullable()
    private final com.tenli.aiot.model.network.MqttInfo mqttInfo = null;
    @org.jetbrains.annotations.Nullable()
    private final java.util.List<com.tenli.aiot.model.network.RoleDevice> roles = null;
    private boolean checked;
    
    public final int component1() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component10() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<com.tenli.aiot.model.network.PostHeader> component11() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.aiot.model.network.DevInfo component12() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.aiot.model.network.ExtendedData component13() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.aiot.model.network.MqttInfo component14() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<com.tenli.aiot.model.network.RoleDevice> component15() {
        return null;
    }
    
    public final boolean component16() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component6() {
        return null;
    }
    
    public final int component7() {
        return 0;
    }
    
    public final int component8() {
        return 0;
    }
    
    public final int component9() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.aiot.model.network.DeviceItem copy(int id, @org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    java.lang.String uuid, @org.jetbrains.annotations.NotNull()
    java.lang.String deviceCode, @org.jetbrains.annotations.NotNull()
    java.lang.String key, @org.jetbrains.annotations.NotNull()
    java.lang.String publicTargetURI, int status, int connectState, int userGroupId, @org.jetbrains.annotations.Nullable()
    java.lang.String postURL, @org.jetbrains.annotations.Nullable()
    java.util.List<com.tenli.aiot.model.network.PostHeader> postHeaders, @org.jetbrains.annotations.Nullable()
    com.tenli.aiot.model.network.DevInfo info, @org.jetbrains.annotations.Nullable()
    com.tenli.aiot.model.network.ExtendedData extendedData, @org.jetbrains.annotations.Nullable()
    com.tenli.aiot.model.network.MqttInfo mqttInfo, @org.jetbrains.annotations.Nullable()
    java.util.List<com.tenli.aiot.model.network.RoleDevice> roles, boolean checked) {
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
    
    public DeviceItem(int id, @org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    java.lang.String uuid, @org.jetbrains.annotations.NotNull()
    java.lang.String deviceCode, @org.jetbrains.annotations.NotNull()
    java.lang.String key, @org.jetbrains.annotations.NotNull()
    java.lang.String publicTargetURI, int status, int connectState, int userGroupId, @org.jetbrains.annotations.Nullable()
    java.lang.String postURL, @org.jetbrains.annotations.Nullable()
    java.util.List<com.tenli.aiot.model.network.PostHeader> postHeaders, @org.jetbrains.annotations.Nullable()
    com.tenli.aiot.model.network.DevInfo info, @org.jetbrains.annotations.Nullable()
    com.tenli.aiot.model.network.ExtendedData extendedData, @org.jetbrains.annotations.Nullable()
    com.tenli.aiot.model.network.MqttInfo mqttInfo, @org.jetbrains.annotations.Nullable()
    java.util.List<com.tenli.aiot.model.network.RoleDevice> roles, boolean checked) {
        super();
    }
    
    public final int getId() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getUuid() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getDeviceCode() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getKey() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getPublicTargetURI() {
        return null;
    }
    
    public final int getStatus() {
        return 0;
    }
    
    public final int getConnectState() {
        return 0;
    }
    
    public final int getUserGroupId() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getPostURL() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<com.tenli.aiot.model.network.PostHeader> getPostHeaders() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.aiot.model.network.DevInfo getInfo() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.aiot.model.network.ExtendedData getExtendedData() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.aiot.model.network.MqttInfo getMqttInfo() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<com.tenli.aiot.model.network.RoleDevice> getRoles() {
        return null;
    }
    
    public final boolean getChecked() {
        return false;
    }
    
    public final void setChecked(boolean p0) {
    }
    
    public final boolean isOnline() {
        return false;
    }
    
    public final int getCameraCount() {
        return 0;
    }
    
    public final int getIotCount() {
        return 0;
    }
    
    public DeviceItem() {
        super();
    }
}