package com.tenli.oneview.model.network;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0002\b\u0018\n\u0002\u0010\b\n\u0002\b\u0002\b\u0087\b\u0018\u00002\u00020\u0001B_\u0012\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\b\u0012\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\n\u0012\u0010\b\u0002\u0010\u000b\u001a\n\u0012\u0004\u0012\u00020\r\u0018\u00010\f\u00a2\u0006\u0002\u0010\u000eJ\u0010\u0010\u001a\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u0010\u0012J\u0010\u0010\u001b\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u0010\u0012J\u0010\u0010\u001c\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u0010\u0012J\u0010\u0010\u001d\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u0010\u0012J\u000b\u0010\u001e\u001a\u0004\u0018\u00010\bH\u00c6\u0003J\u000b\u0010\u001f\u001a\u0004\u0018\u00010\nH\u00c6\u0003J\u0011\u0010 \u001a\n\u0012\u0004\u0012\u00020\r\u0018\u00010\fH\u00c6\u0003Jh\u0010!\u001a\u00020\u00002\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\b2\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\n2\u0010\b\u0002\u0010\u000b\u001a\n\u0012\u0004\u0012\u00020\r\u0018\u00010\fH\u00c6\u0001\u00a2\u0006\u0002\u0010\"J\u0013\u0010#\u001a\u00020\u00032\b\u0010$\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010%\u001a\u00020&H\u00d6\u0001J\t\u0010\'\u001a\u00020\rH\u00d6\u0001R\u0013\u0010\t\u001a\u0004\u0018\u00010\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u0015\u0010\u0006\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\n\n\u0002\u0010\u0013\u001a\u0004\b\u0011\u0010\u0012R\u0015\u0010\u0005\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\n\n\u0002\u0010\u0013\u001a\u0004\b\u0014\u0010\u0012R\u0015\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\n\n\u0002\u0010\u0013\u001a\u0004\b\u0015\u0010\u0012R\u0013\u0010\u0007\u001a\u0004\u0018\u00010\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u0015\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\n\n\u0002\u0010\u0013\u001a\u0004\b\u0002\u0010\u0012R\u0019\u0010\u000b\u001a\n\u0012\u0004\u0012\u00020\r\u0018\u00010\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019\u00a8\u0006("}, d2 = {"Lcom/tenli/oneview/model/network/UserPermissions;", "", "isAdmin", "", "allowSettings", "allowEditCamera", "allowConfigAI", "cameras", "Lcom/tenli/oneview/model/network/CameraPermissions;", "aiEvents", "Lcom/tenli/oneview/model/network/AiEventPermissions;", "modules", "", "", "(Ljava/lang/Boolean;Ljava/lang/Boolean;Ljava/lang/Boolean;Ljava/lang/Boolean;Lcom/tenli/oneview/model/network/CameraPermissions;Lcom/tenli/oneview/model/network/AiEventPermissions;Ljava/util/List;)V", "getAiEvents", "()Lcom/tenli/oneview/model/network/AiEventPermissions;", "getAllowConfigAI", "()Ljava/lang/Boolean;", "Ljava/lang/Boolean;", "getAllowEditCamera", "getAllowSettings", "getCameras", "()Lcom/tenli/oneview/model/network/CameraPermissions;", "getModules", "()Ljava/util/List;", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "copy", "(Ljava/lang/Boolean;Ljava/lang/Boolean;Ljava/lang/Boolean;Ljava/lang/Boolean;Lcom/tenli/oneview/model/network/CameraPermissions;Lcom/tenli/oneview/model/network/AiEventPermissions;Ljava/util/List;)Lcom/tenli/oneview/model/network/UserPermissions;", "equals", "other", "hashCode", "", "toString", "app_debug"})
public final class UserPermissions {
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Boolean isAdmin = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Boolean allowSettings = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Boolean allowEditCamera = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Boolean allowConfigAI = null;
    @org.jetbrains.annotations.Nullable()
    private final com.tenli.oneview.model.network.CameraPermissions cameras = null;
    @org.jetbrains.annotations.Nullable()
    private final com.tenli.oneview.model.network.AiEventPermissions aiEvents = null;
    @org.jetbrains.annotations.Nullable()
    private final java.util.List<java.lang.String> modules = null;
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Boolean component1() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Boolean component2() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Boolean component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Boolean component4() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.oneview.model.network.CameraPermissions component5() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.oneview.model.network.AiEventPermissions component6() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<java.lang.String> component7() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.oneview.model.network.UserPermissions copy(@org.jetbrains.annotations.Nullable()
    java.lang.Boolean isAdmin, @org.jetbrains.annotations.Nullable()
    java.lang.Boolean allowSettings, @org.jetbrains.annotations.Nullable()
    java.lang.Boolean allowEditCamera, @org.jetbrains.annotations.Nullable()
    java.lang.Boolean allowConfigAI, @org.jetbrains.annotations.Nullable()
    com.tenli.oneview.model.network.CameraPermissions cameras, @org.jetbrains.annotations.Nullable()
    com.tenli.oneview.model.network.AiEventPermissions aiEvents, @org.jetbrains.annotations.Nullable()
    java.util.List<java.lang.String> modules) {
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
    
    public UserPermissions(@org.jetbrains.annotations.Nullable()
    java.lang.Boolean isAdmin, @org.jetbrains.annotations.Nullable()
    java.lang.Boolean allowSettings, @org.jetbrains.annotations.Nullable()
    java.lang.Boolean allowEditCamera, @org.jetbrains.annotations.Nullable()
    java.lang.Boolean allowConfigAI, @org.jetbrains.annotations.Nullable()
    com.tenli.oneview.model.network.CameraPermissions cameras, @org.jetbrains.annotations.Nullable()
    com.tenli.oneview.model.network.AiEventPermissions aiEvents, @org.jetbrains.annotations.Nullable()
    java.util.List<java.lang.String> modules) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Boolean isAdmin() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Boolean getAllowSettings() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Boolean getAllowEditCamera() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Boolean getAllowConfigAI() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.oneview.model.network.CameraPermissions getCameras() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.oneview.model.network.AiEventPermissions getAiEvents() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<java.lang.String> getModules() {
        return null;
    }
    
    public UserPermissions() {
        super();
    }
}