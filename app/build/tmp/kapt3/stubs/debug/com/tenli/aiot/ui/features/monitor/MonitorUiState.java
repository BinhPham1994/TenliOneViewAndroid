package com.tenli.aiot.ui.features.monitor;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010$\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0002\b!\b\u0087\b\u0018\u00002\u00020\u0001B\u0081\u0001\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\u000e\b\u0002\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u0012\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\u0006\u0012\u000e\b\u0002\u0010\b\u001a\b\u0012\u0004\u0012\u00020\t0\u0005\u0012\u001a\b\u0002\u0010\n\u001a\u0014\u0012\u0004\u0012\u00020\u0003\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\u00050\u000b\u0012\b\b\u0002\u0010\f\u001a\u00020\r\u0012\b\b\u0002\u0010\u000e\u001a\u00020\u000f\u0012\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\t\u0012\b\b\u0002\u0010\u0011\u001a\u00020\u000f\u00a2\u0006\u0002\u0010\u0012J\t\u0010\"\u001a\u00020\u0003H\u00c6\u0003J\u000f\u0010#\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005H\u00c6\u0003J\u000b\u0010$\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\u000f\u0010%\u001a\b\u0012\u0004\u0012\u00020\t0\u0005H\u00c6\u0003J\u001b\u0010&\u001a\u0014\u0012\u0004\u0012\u00020\u0003\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\u00050\u000bH\u00c6\u0003J\t\u0010\'\u001a\u00020\rH\u00c6\u0003J\t\u0010(\u001a\u00020\u000fH\u00c6\u0003J\u000b\u0010)\u001a\u0004\u0018\u00010\tH\u00c6\u0003J\t\u0010*\u001a\u00020\u000fH\u00c6\u0003J\u0085\u0001\u0010+\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\u000e\b\u0002\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\u00062\u000e\b\u0002\u0010\b\u001a\b\u0012\u0004\u0012\u00020\t0\u00052\u001a\b\u0002\u0010\n\u001a\u0014\u0012\u0004\u0012\u00020\u0003\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\u00050\u000b2\b\b\u0002\u0010\f\u001a\u00020\r2\b\b\u0002\u0010\u000e\u001a\u00020\u000f2\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\t2\b\b\u0002\u0010\u0011\u001a\u00020\u000fH\u00c6\u0001J\u0013\u0010,\u001a\u00020\r2\b\u0010-\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010.\u001a\u00020\u000fH\u00d6\u0001J\t\u0010/\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u000e\u001a\u00020\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014R\u0017\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R#\u0010\n\u001a\u0014\u0012\u0004\u0012\u00020\u0003\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\u00050\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u001aR\u0011\u0010\f\u001a\u00020\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\u001bR\u0011\u0010\u0011\u001a\u00020\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u0014R\u0017\u0010\b\u001a\b\u0012\u0004\u0012\u00020\t0\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u0016R\u0013\u0010\u0007\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001fR\u0013\u0010\u0010\u001a\u0004\u0018\u00010\t\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010!\u00a8\u00060"}, d2 = {"Lcom/tenli/aiot/ui/features/monitor/MonitorUiState;", "", "homeName", "", "displayGroups", "", "Lcom/tenli/aiot/model/network/HomeGroupDisplay;", "selectedGroup", "monitors", "Lcom/tenli/aiot/model/network/MonitorDisplayItem;", "groupedMonitors", "", "isRefreshing", "", "currentLevel", "", "selectedMonitor", "monitorUpdateTicket", "(Ljava/lang/String;Ljava/util/List;Lcom/tenli/aiot/model/network/HomeGroupDisplay;Ljava/util/List;Ljava/util/Map;ZILcom/tenli/aiot/model/network/MonitorDisplayItem;I)V", "getCurrentLevel", "()I", "getDisplayGroups", "()Ljava/util/List;", "getGroupedMonitors", "()Ljava/util/Map;", "getHomeName", "()Ljava/lang/String;", "()Z", "getMonitorUpdateTicket", "getMonitors", "getSelectedGroup", "()Lcom/tenli/aiot/model/network/HomeGroupDisplay;", "getSelectedMonitor", "()Lcom/tenli/aiot/model/network/MonitorDisplayItem;", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "hashCode", "toString", "app_debug"})
public final class MonitorUiState {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String homeName = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.tenli.aiot.model.network.HomeGroupDisplay> displayGroups = null;
    @org.jetbrains.annotations.Nullable()
    private final com.tenli.aiot.model.network.HomeGroupDisplay selectedGroup = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.tenli.aiot.model.network.MonitorDisplayItem> monitors = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Map<java.lang.String, java.util.List<com.tenli.aiot.model.network.MonitorDisplayItem>> groupedMonitors = null;
    private final boolean isRefreshing = false;
    private final int currentLevel = 0;
    @org.jetbrains.annotations.Nullable()
    private final com.tenli.aiot.model.network.MonitorDisplayItem selectedMonitor = null;
    private final int monitorUpdateTicket = 0;
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.tenli.aiot.model.network.HomeGroupDisplay> component2() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.aiot.model.network.HomeGroupDisplay component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.tenli.aiot.model.network.MonitorDisplayItem> component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<java.lang.String, java.util.List<com.tenli.aiot.model.network.MonitorDisplayItem>> component5() {
        return null;
    }
    
    public final boolean component6() {
        return false;
    }
    
    public final int component7() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.aiot.model.network.MonitorDisplayItem component8() {
        return null;
    }
    
    public final int component9() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.aiot.ui.features.monitor.MonitorUiState copy(@org.jetbrains.annotations.NotNull()
    java.lang.String homeName, @org.jetbrains.annotations.NotNull()
    java.util.List<com.tenli.aiot.model.network.HomeGroupDisplay> displayGroups, @org.jetbrains.annotations.Nullable()
    com.tenli.aiot.model.network.HomeGroupDisplay selectedGroup, @org.jetbrains.annotations.NotNull()
    java.util.List<com.tenli.aiot.model.network.MonitorDisplayItem> monitors, @org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.String, ? extends java.util.List<com.tenli.aiot.model.network.MonitorDisplayItem>> groupedMonitors, boolean isRefreshing, int currentLevel, @org.jetbrains.annotations.Nullable()
    com.tenli.aiot.model.network.MonitorDisplayItem selectedMonitor, int monitorUpdateTicket) {
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
    
    public MonitorUiState(@org.jetbrains.annotations.NotNull()
    java.lang.String homeName, @org.jetbrains.annotations.NotNull()
    java.util.List<com.tenli.aiot.model.network.HomeGroupDisplay> displayGroups, @org.jetbrains.annotations.Nullable()
    com.tenli.aiot.model.network.HomeGroupDisplay selectedGroup, @org.jetbrains.annotations.NotNull()
    java.util.List<com.tenli.aiot.model.network.MonitorDisplayItem> monitors, @org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.String, ? extends java.util.List<com.tenli.aiot.model.network.MonitorDisplayItem>> groupedMonitors, boolean isRefreshing, int currentLevel, @org.jetbrains.annotations.Nullable()
    com.tenli.aiot.model.network.MonitorDisplayItem selectedMonitor, int monitorUpdateTicket) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getHomeName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.tenli.aiot.model.network.HomeGroupDisplay> getDisplayGroups() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.aiot.model.network.HomeGroupDisplay getSelectedGroup() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.tenli.aiot.model.network.MonitorDisplayItem> getMonitors() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<java.lang.String, java.util.List<com.tenli.aiot.model.network.MonitorDisplayItem>> getGroupedMonitors() {
        return null;
    }
    
    public final boolean isRefreshing() {
        return false;
    }
    
    public final int getCurrentLevel() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.aiot.model.network.MonitorDisplayItem getSelectedMonitor() {
        return null;
    }
    
    public final int getMonitorUpdateTicket() {
        return 0;
    }
    
    public MonitorUiState() {
        super();
    }
}