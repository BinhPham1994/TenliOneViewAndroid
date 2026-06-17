package com.tenli.aiot.model.network;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010 \n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b#\b\u0087\b\u0018\u00002\u00020\u0001Bc\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\u0010\b\u0002\u0010\u0007\u001a\n\u0012\u0004\u0012\u00020\t\u0018\u00010\b\u0012\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\t\u0012\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\f\u0012\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u000e\u0012\u0006\u0010\u000f\u001a\u00020\u0010\u0012\u0006\u0010\u0011\u001a\u00020\u0010\u00a2\u0006\u0002\u0010\u0012J\t\u0010$\u001a\u00020\u0003H\u00c6\u0003J\t\u0010%\u001a\u00020\u0003H\u00c6\u0003J\t\u0010&\u001a\u00020\u0006H\u00c6\u0003J\u0011\u0010\'\u001a\n\u0012\u0004\u0012\u00020\t\u0018\u00010\bH\u00c6\u0003J\u0010\u0010(\u001a\u0004\u0018\u00010\tH\u00c6\u0003\u00a2\u0006\u0002\u0010\u001fJ\u000b\u0010)\u001a\u0004\u0018\u00010\fH\u00c6\u0003J\u000b\u0010*\u001a\u0004\u0018\u00010\u000eH\u00c6\u0003J\t\u0010+\u001a\u00020\u0010H\u00c6\u0003J\t\u0010,\u001a\u00020\u0010H\u00c6\u0003Jv\u0010-\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\u0010\b\u0002\u0010\u0007\u001a\n\u0012\u0004\u0012\u00020\t\u0018\u00010\b2\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\t2\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\f2\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u000e2\b\b\u0002\u0010\u000f\u001a\u00020\u00102\b\b\u0002\u0010\u0011\u001a\u00020\u0010H\u00c6\u0001\u00a2\u0006\u0002\u0010.J\u0013\u0010/\u001a\u00020\u00062\b\u00100\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u00101\u001a\u00020\tH\u00d6\u0001J\t\u00102\u001a\u00020\u0003H\u00d6\u0001R\u0013\u0010\r\u001a\u0004\u0018\u00010\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014R\u0011\u0010\u000f\u001a\u00020\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u001aR\u0019\u0010\u0007\u001a\n\u0012\u0004\u0012\u00020\t\u0018\u00010\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u001cR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u001aR\u0015\u0010\n\u001a\u0004\u0018\u00010\t\u00a2\u0006\n\n\u0002\u0010 \u001a\u0004\b\u001e\u0010\u001fR\u0013\u0010\u000b\u001a\u0004\u0018\u00010\f\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\"R\u0011\u0010\u0011\u001a\u00020\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010\u0016\u00a8\u00063"}, d2 = {"Lcom/tenli/aiot/model/network/ScriptItem;", "", "id", "", "name", "enabled", "", "monitorIds", "", "", "resetModeAfterTime", "time", "Lcom/tenli/aiot/model/network/ScriptTime;", "actions", "Lcom/tenli/aiot/model/network/ScriptActions;", "createdAt", "", "updatedAt", "(Ljava/lang/String;Ljava/lang/String;ZLjava/util/List;Ljava/lang/Integer;Lcom/tenli/aiot/model/network/ScriptTime;Lcom/tenli/aiot/model/network/ScriptActions;JJ)V", "getActions", "()Lcom/tenli/aiot/model/network/ScriptActions;", "getCreatedAt", "()J", "getEnabled", "()Z", "getId", "()Ljava/lang/String;", "getMonitorIds", "()Ljava/util/List;", "getName", "getResetModeAfterTime", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getTime", "()Lcom/tenli/aiot/model/network/ScriptTime;", "getUpdatedAt", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(Ljava/lang/String;Ljava/lang/String;ZLjava/util/List;Ljava/lang/Integer;Lcom/tenli/aiot/model/network/ScriptTime;Lcom/tenli/aiot/model/network/ScriptActions;JJ)Lcom/tenli/aiot/model/network/ScriptItem;", "equals", "other", "hashCode", "toString", "app_debug"})
public final class ScriptItem {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String id = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String name = null;
    private final boolean enabled = false;
    @org.jetbrains.annotations.Nullable()
    private final java.util.List<java.lang.Integer> monitorIds = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer resetModeAfterTime = null;
    @org.jetbrains.annotations.Nullable()
    private final com.tenli.aiot.model.network.ScriptTime time = null;
    @org.jetbrains.annotations.Nullable()
    private final com.tenli.aiot.model.network.ScriptActions actions = null;
    private final long createdAt = 0L;
    private final long updatedAt = 0L;
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component2() {
        return null;
    }
    
    public final boolean component3() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<java.lang.Integer> component4() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component5() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.aiot.model.network.ScriptTime component6() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.aiot.model.network.ScriptActions component7() {
        return null;
    }
    
    public final long component8() {
        return 0L;
    }
    
    public final long component9() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.aiot.model.network.ScriptItem copy(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    java.lang.String name, boolean enabled, @org.jetbrains.annotations.Nullable()
    java.util.List<java.lang.Integer> monitorIds, @org.jetbrains.annotations.Nullable()
    java.lang.Integer resetModeAfterTime, @org.jetbrains.annotations.Nullable()
    com.tenli.aiot.model.network.ScriptTime time, @org.jetbrains.annotations.Nullable()
    com.tenli.aiot.model.network.ScriptActions actions, long createdAt, long updatedAt) {
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
    
    public ScriptItem(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    java.lang.String name, boolean enabled, @org.jetbrains.annotations.Nullable()
    java.util.List<java.lang.Integer> monitorIds, @org.jetbrains.annotations.Nullable()
    java.lang.Integer resetModeAfterTime, @org.jetbrains.annotations.Nullable()
    com.tenli.aiot.model.network.ScriptTime time, @org.jetbrains.annotations.Nullable()
    com.tenli.aiot.model.network.ScriptActions actions, long createdAt, long updatedAt) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getId() {
        return null;
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
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getResetModeAfterTime() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.aiot.model.network.ScriptTime getTime() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.aiot.model.network.ScriptActions getActions() {
        return null;
    }
    
    public final long getCreatedAt() {
        return 0L;
    }
    
    public final long getUpdatedAt() {
        return 0L;
    }
}