package com.tenli.oneview.model.network;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u001a\b\u0087\b\u0018\u00002\u00020\u0001BE\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\b\u0012\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\n\u0012\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\f\u00a2\u0006\u0002\u0010\rJ\t\u0010\u001a\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001b\u001a\u00020\u0005H\u00c6\u0003J\u000b\u0010\u001c\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u0010\u0010\u001d\u001a\u0004\u0018\u00010\bH\u00c6\u0003\u00a2\u0006\u0002\u0010\u0015J\u000b\u0010\u001e\u001a\u0004\u0018\u00010\nH\u00c6\u0003J\u000b\u0010\u001f\u001a\u0004\u0018\u00010\fH\u00c6\u0003JR\u0010 \u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\b2\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\n2\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\fH\u00c6\u0001\u00a2\u0006\u0002\u0010!J\u0013\u0010\"\u001a\u00020\u00032\b\u0010#\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010$\u001a\u00020\bH\u00d6\u0001J\t\u0010%\u001a\u00020\u0005H\u00d6\u0001R\u0013\u0010\t\u001a\u0004\u0018\u00010\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0013\u0010\u000b\u001a\u0004\u0018\u00010\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0015\u0010\u0007\u001a\u0004\u0018\u00010\b\u00a2\u0006\n\n\u0002\u0010\u0016\u001a\u0004\b\u0014\u0010\u0015R\u0013\u0010\u0006\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u0018\u00a8\u0006&"}, d2 = {"Lcom/tenli/oneview/model/network/MqttInfo;", "", "enabled", "", "uri", "", "privateUri", "localPort", "", "auth", "Lcom/tenli/oneview/model/network/MqttAuth;", "bridge", "Lcom/tenli/oneview/model/network/MqttBridge;", "(ZLjava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Lcom/tenli/oneview/model/network/MqttAuth;Lcom/tenli/oneview/model/network/MqttBridge;)V", "getAuth", "()Lcom/tenli/oneview/model/network/MqttAuth;", "getBridge", "()Lcom/tenli/oneview/model/network/MqttBridge;", "getEnabled", "()Z", "getLocalPort", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getPrivateUri", "()Ljava/lang/String;", "getUri", "component1", "component2", "component3", "component4", "component5", "component6", "copy", "(ZLjava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Lcom/tenli/oneview/model/network/MqttAuth;Lcom/tenli/oneview/model/network/MqttBridge;)Lcom/tenli/oneview/model/network/MqttInfo;", "equals", "other", "hashCode", "toString", "app_debug"})
public final class MqttInfo {
    private final boolean enabled = false;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String uri = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String privateUri = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer localPort = null;
    @org.jetbrains.annotations.Nullable()
    private final com.tenli.oneview.model.network.MqttAuth auth = null;
    @org.jetbrains.annotations.Nullable()
    private final com.tenli.oneview.model.network.MqttBridge bridge = null;
    
    public final boolean component1() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component4() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.oneview.model.network.MqttAuth component5() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.oneview.model.network.MqttBridge component6() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.oneview.model.network.MqttInfo copy(boolean enabled, @org.jetbrains.annotations.NotNull()
    java.lang.String uri, @org.jetbrains.annotations.Nullable()
    java.lang.String privateUri, @org.jetbrains.annotations.Nullable()
    java.lang.Integer localPort, @org.jetbrains.annotations.Nullable()
    com.tenli.oneview.model.network.MqttAuth auth, @org.jetbrains.annotations.Nullable()
    com.tenli.oneview.model.network.MqttBridge bridge) {
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
    
    public MqttInfo(boolean enabled, @org.jetbrains.annotations.NotNull()
    java.lang.String uri, @org.jetbrains.annotations.Nullable()
    java.lang.String privateUri, @org.jetbrains.annotations.Nullable()
    java.lang.Integer localPort, @org.jetbrains.annotations.Nullable()
    com.tenli.oneview.model.network.MqttAuth auth, @org.jetbrains.annotations.Nullable()
    com.tenli.oneview.model.network.MqttBridge bridge) {
        super();
    }
    
    public final boolean getEnabled() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getUri() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getPrivateUri() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getLocalPort() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.oneview.model.network.MqttAuth getAuth() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.oneview.model.network.MqttBridge getBridge() {
        return null;
    }
}