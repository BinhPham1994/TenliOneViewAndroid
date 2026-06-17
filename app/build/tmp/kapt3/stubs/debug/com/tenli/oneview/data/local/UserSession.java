package com.tenli.oneview.data.local;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0012\u001a\u00020\u0013J\u0010\u0010\u0014\u001a\u00020\u00132\b\u0010\u0015\u001a\u0004\u0018\u00010\u0016R\u001a\u0010\u0003\u001a\u00020\u0004X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\bR\u001a\u0010\t\u001a\u00020\u0004X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\n\u0010\u0006\"\u0004\b\u000b\u0010\bR\u001c\u0010\f\u001a\u0004\u0018\u00010\rX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000e\u0010\u000f\"\u0004\b\u0010\u0010\u0011\u00a8\u0006\u0017"}, d2 = {"Lcom/tenli/oneview/data/local/UserSession;", "", "()V", "accessToken", "", "getAccessToken", "()Ljava/lang/String;", "setAccessToken", "(Ljava/lang/String;)V", "refreshToken", "getRefreshToken", "setRefreshToken", "userData", "Lcom/tenli/oneview/model/network/UserData;", "getUserData", "()Lcom/tenli/oneview/model/network/UserData;", "setUserData", "(Lcom/tenli/oneview/model/network/UserData;)V", "clear", "", "saveSession", "prefs", "Landroid/content/SharedPreferences;", "app_debug"})
public final class UserSession {
    @org.jetbrains.annotations.Nullable()
    private static com.tenli.oneview.model.network.UserData userData;
    @org.jetbrains.annotations.NotNull()
    private static java.lang.String accessToken = "";
    @org.jetbrains.annotations.NotNull()
    private static java.lang.String refreshToken = "";
    @org.jetbrains.annotations.NotNull()
    public static final com.tenli.oneview.data.local.UserSession INSTANCE = null;
    
    private UserSession() {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.oneview.model.network.UserData getUserData() {
        return null;
    }
    
    public final void setUserData(@org.jetbrains.annotations.Nullable()
    com.tenli.oneview.model.network.UserData p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getAccessToken() {
        return null;
    }
    
    public final void setAccessToken(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getRefreshToken() {
        return null;
    }
    
    public final void setRefreshToken(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    public final void clear() {
    }
    
    public final void saveSession(@org.jetbrains.annotations.Nullable()
    android.content.SharedPreferences prefs) {
    }
}