package com.tenli.aiot.data.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000`\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010%\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010(\u001a\u00020)J\u0006\u0010*\u001a\u00020+J\u0006\u0010,\u001a\u00020+J\u0006\u0010-\u001a\u00020)J\u0006\u0010.\u001a\u00020)R \u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\tR \u0010\n\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0004X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\f\u0010\u0007\"\u0004\b\r\u0010\tR \u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u000f0\u0004X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0010\u0010\u0007\"\u0004\b\u0011\u0010\tR0\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00130\u00042\f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00130\u0004@FX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0015\u0010\u0007\"\u0004\b\u0016\u0010\tR\u000e\u0010\u0017\u001a\u00020\u0018X\u0082\u0004\u00a2\u0006\u0002\n\u0000R \u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u001a0\u0004X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001b\u0010\u0007\"\u0004\b\u001c\u0010\tR,\u0010\u001d\u001a\u0014\u0012\u0004\u0012\u00020\u001f\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001a0\u00040\u001eX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b \u0010!\"\u0004\b\"\u0010#R,\u0010$\u001a\u0014\u0012\u0004\u0012\u00020\u001f\u0012\n\u0012\b\u0012\u0004\u0012\u00020%0\u00040\u001eX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b&\u0010!\"\u0004\b\'\u0010#\u00a8\u0006/"}, d2 = {"Lcom/tenli/aiot/data/repository/DataRepository;", "", "()V", "deviceList", "", "Lcom/tenli/aiot/model/network/DeviceItem;", "getDeviceList", "()Ljava/util/List;", "setDeviceList", "(Ljava/util/List;)V", "eventGroupDefs", "Lcom/tenli/aiot/model/network/EventGroupDef;", "getEventGroupDefs", "setEventGroupDefs", "eventTypeDefs", "Lcom/tenli/aiot/model/network/EventTypeDef;", "getEventTypeDefs", "setEventTypeDefs", "value", "Lcom/tenli/aiot/model/network/HomeGroupDisplay;", "groupList", "getGroupList", "setGroupList", "gson", "Lcom/google/gson/Gson;", "monitorList", "Lcom/tenli/aiot/model/network/MonitorDisplayItem;", "getMonitorList", "setMonitorList", "monitorMap", "", "", "getMonitorMap", "()Ljava/util/Map;", "setMonitorMap", "(Ljava/util/Map;)V", "scriptMap", "Lcom/tenli/aiot/model/network/ScriptItem;", "getScriptMap", "setScriptMap", "clearCache", "", "hasDefines", "", "hasDevices", "persist", "restore", "app_debug"})
public final class DataRepository {
    @org.jetbrains.annotations.NotNull()
    private static java.util.List<com.tenli.aiot.model.network.DeviceItem> deviceList;
    @org.jetbrains.annotations.NotNull()
    private static java.util.Map<java.lang.Integer, java.util.List<com.tenli.aiot.model.network.ScriptItem>> scriptMap;
    @org.jetbrains.annotations.NotNull()
    private static java.util.List<com.tenli.aiot.model.network.HomeGroupDisplay> groupList;
    @org.jetbrains.annotations.NotNull()
    private static java.util.List<com.tenli.aiot.model.network.EventTypeDef> eventTypeDefs;
    @org.jetbrains.annotations.NotNull()
    private static java.util.List<com.tenli.aiot.model.network.EventGroupDef> eventGroupDefs;
    @org.jetbrains.annotations.NotNull()
    private static java.util.Map<java.lang.Integer, java.util.List<com.tenli.aiot.model.network.MonitorDisplayItem>> monitorMap;
    @org.jetbrains.annotations.NotNull()
    private static java.util.List<com.tenli.aiot.model.network.MonitorDisplayItem> monitorList;
    @org.jetbrains.annotations.NotNull()
    private static final com.google.gson.Gson gson = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.tenli.aiot.data.repository.DataRepository INSTANCE = null;
    
    private DataRepository() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.tenli.aiot.model.network.DeviceItem> getDeviceList() {
        return null;
    }
    
    public final void setDeviceList(@org.jetbrains.annotations.NotNull()
    java.util.List<com.tenli.aiot.model.network.DeviceItem> p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<java.lang.Integer, java.util.List<com.tenli.aiot.model.network.ScriptItem>> getScriptMap() {
        return null;
    }
    
    public final void setScriptMap(@org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.Integer, java.util.List<com.tenli.aiot.model.network.ScriptItem>> p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.tenli.aiot.model.network.HomeGroupDisplay> getGroupList() {
        return null;
    }
    
    public final void setGroupList(@org.jetbrains.annotations.NotNull()
    java.util.List<com.tenli.aiot.model.network.HomeGroupDisplay> value) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.tenli.aiot.model.network.EventTypeDef> getEventTypeDefs() {
        return null;
    }
    
    public final void setEventTypeDefs(@org.jetbrains.annotations.NotNull()
    java.util.List<com.tenli.aiot.model.network.EventTypeDef> p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.tenli.aiot.model.network.EventGroupDef> getEventGroupDefs() {
        return null;
    }
    
    public final void setEventGroupDefs(@org.jetbrains.annotations.NotNull()
    java.util.List<com.tenli.aiot.model.network.EventGroupDef> p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<java.lang.Integer, java.util.List<com.tenli.aiot.model.network.MonitorDisplayItem>> getMonitorMap() {
        return null;
    }
    
    public final void setMonitorMap(@org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.Integer, java.util.List<com.tenli.aiot.model.network.MonitorDisplayItem>> p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.tenli.aiot.model.network.MonitorDisplayItem> getMonitorList() {
        return null;
    }
    
    public final void setMonitorList(@org.jetbrains.annotations.NotNull()
    java.util.List<com.tenli.aiot.model.network.MonitorDisplayItem> p0) {
    }
    
    public final boolean hasDevices() {
        return false;
    }
    
    public final boolean hasDefines() {
        return false;
    }
    
    public final void persist() {
    }
    
    public final void restore() {
    }
    
    public final void clearCache() {
    }
}