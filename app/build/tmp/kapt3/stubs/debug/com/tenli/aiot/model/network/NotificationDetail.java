package com.tenli.aiot.model.network;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u000e\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0087\b\u0018\u00002\u00020\u0001B\u001f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\t\u0010\u000f\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010\u0010\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\t\u0010\u0011\u001a\u00020\u0007H\u00c6\u0003J)\u0010\u0012\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u0007H\u00c6\u0001J\u0013\u0010\u0013\u001a\u00020\u00032\b\u0010\u0014\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0015\u001a\u00020\u0016H\u00d6\u0001J\t\u0010\u0017\u001a\u00020\u0018H\u00d6\u0001R\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000e\u00a8\u0006\u0019"}, d2 = {"Lcom/tenli/aiot/model/network/NotificationDetail;", "", "enabled", "", "alarmMode", "Lcom/google/gson/JsonElement;", "scripts", "Lcom/tenli/aiot/model/network/ScriptGroup;", "(ZLcom/google/gson/JsonElement;Lcom/tenli/aiot/model/network/ScriptGroup;)V", "getAlarmMode", "()Lcom/google/gson/JsonElement;", "getEnabled", "()Z", "getScripts", "()Lcom/tenli/aiot/model/network/ScriptGroup;", "component1", "component2", "component3", "copy", "equals", "other", "hashCode", "", "toString", "", "app_debug"})
public final class NotificationDetail {
    private final boolean enabled = false;
    @org.jetbrains.annotations.Nullable()
    private final com.google.gson.JsonElement alarmMode = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.aiot.model.network.ScriptGroup scripts = null;
    
    public final boolean component1() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.google.gson.JsonElement component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.aiot.model.network.ScriptGroup component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.aiot.model.network.NotificationDetail copy(boolean enabled, @org.jetbrains.annotations.Nullable()
    com.google.gson.JsonElement alarmMode, @org.jetbrains.annotations.NotNull()
    com.tenli.aiot.model.network.ScriptGroup scripts) {
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
    
    public NotificationDetail(boolean enabled, @org.jetbrains.annotations.Nullable()
    com.google.gson.JsonElement alarmMode, @org.jetbrains.annotations.NotNull()
    com.tenli.aiot.model.network.ScriptGroup scripts) {
        super();
    }
    
    public final boolean getEnabled() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.google.gson.JsonElement getAlarmMode() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.aiot.model.network.ScriptGroup getScripts() {
        return null;
    }
}