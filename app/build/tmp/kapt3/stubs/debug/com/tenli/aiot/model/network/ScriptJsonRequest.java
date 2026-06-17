package com.tenli.aiot.model.network;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010 \n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0019\b\u0087\b\u0018\u00002\u00020\u0001B?\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0010\b\u0002\u0010\u0006\u001a\n\u0012\u0004\u0012\u00020\b\u0018\u00010\u0007\u0012\u0006\u0010\t\u001a\u00020\b\u0012\u0006\u0010\n\u001a\u00020\u000b\u0012\u0006\u0010\f\u001a\u00020\r\u00a2\u0006\u0002\u0010\u000eJ\t\u0010\u001b\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001c\u001a\u00020\u0005H\u00c6\u0003J\u0011\u0010\u001d\u001a\n\u0012\u0004\u0012\u00020\b\u0018\u00010\u0007H\u00c6\u0003J\t\u0010\u001e\u001a\u00020\bH\u00c6\u0003J\t\u0010\u001f\u001a\u00020\u000bH\u00c6\u0003J\t\u0010 \u001a\u00020\rH\u00c6\u0003JM\u0010!\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\u0010\b\u0002\u0010\u0006\u001a\n\u0012\u0004\u0012\u00020\b\u0018\u00010\u00072\b\b\u0002\u0010\t\u001a\u00020\b2\b\b\u0002\u0010\n\u001a\u00020\u000b2\b\b\u0002\u0010\f\u001a\u00020\rH\u00c6\u0001J\u0013\u0010\"\u001a\u00020\u00052\b\u0010#\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010$\u001a\u00020\bH\u00d6\u0001J\t\u0010%\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\f\u001a\u00020\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012R\u0019\u0010\u0006\u001a\n\u0012\u0004\u0012\u00020\b\u0018\u00010\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R\u0011\u0010\t\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018R\u0011\u0010\n\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u001a\u00a8\u0006&"}, d2 = {"Lcom/tenli/aiot/model/network/ScriptJsonRequest;", "", "name", "", "enabled", "", "monitorIds", "", "", "resetModeAfterTime", "time", "Lcom/tenli/aiot/model/network/ScriptTimeConfig;", "actions", "Lcom/tenli/aiot/model/network/ScriptActionsConfig;", "(Ljava/lang/String;ZLjava/util/List;ILcom/tenli/aiot/model/network/ScriptTimeConfig;Lcom/tenli/aiot/model/network/ScriptActionsConfig;)V", "getActions", "()Lcom/tenli/aiot/model/network/ScriptActionsConfig;", "getEnabled", "()Z", "getMonitorIds", "()Ljava/util/List;", "getName", "()Ljava/lang/String;", "getResetModeAfterTime", "()I", "getTime", "()Lcom/tenli/aiot/model/network/ScriptTimeConfig;", "component1", "component2", "component3", "component4", "component5", "component6", "copy", "equals", "other", "hashCode", "toString", "app_debug"})
@androidx.annotation.Keep()
public final class ScriptJsonRequest {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String name = null;
    private final boolean enabled = false;
    @org.jetbrains.annotations.Nullable()
    private final java.util.List<java.lang.Integer> monitorIds = null;
    private final int resetModeAfterTime = 0;
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.aiot.model.network.ScriptTimeConfig time = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.aiot.model.network.ScriptActionsConfig actions = null;
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    public final boolean component2() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<java.lang.Integer> component3() {
        return null;
    }
    
    public final int component4() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.aiot.model.network.ScriptTimeConfig component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.aiot.model.network.ScriptActionsConfig component6() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.aiot.model.network.ScriptJsonRequest copy(@org.jetbrains.annotations.NotNull()
    java.lang.String name, boolean enabled, @org.jetbrains.annotations.Nullable()
    java.util.List<java.lang.Integer> monitorIds, int resetModeAfterTime, @org.jetbrains.annotations.NotNull()
    com.tenli.aiot.model.network.ScriptTimeConfig time, @org.jetbrains.annotations.NotNull()
    com.tenli.aiot.model.network.ScriptActionsConfig actions) {
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
    
    public ScriptJsonRequest(@org.jetbrains.annotations.NotNull()
    java.lang.String name, boolean enabled, @org.jetbrains.annotations.Nullable()
    java.util.List<java.lang.Integer> monitorIds, int resetModeAfterTime, @org.jetbrains.annotations.NotNull()
    com.tenli.aiot.model.network.ScriptTimeConfig time, @org.jetbrains.annotations.NotNull()
    com.tenli.aiot.model.network.ScriptActionsConfig actions) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getName() {
        return null;
    }
    
    public final boolean getEnabled() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<java.lang.Integer> getMonitorIds() {
        return null;
    }
    
    public final int getResetModeAfterTime() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.aiot.model.network.ScriptTimeConfig getTime() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.aiot.model.network.ScriptActionsConfig getActions() {
        return null;
    }
}