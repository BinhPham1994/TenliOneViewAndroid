package com.tenli.aiot;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\t\u001a\u00020\nH\u0016J\b\u0010\u000b\u001a\u00020\nH\u0002J\b\u0010\f\u001a\u00020\nH\u0002J\u0010\u0010\r\u001a\u00020\n2\u0006\u0010\u000e\u001a\u00020\u000fH\u0002R\u001a\u0010\u0003\u001a\u00020\u0004X\u0086.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\b\u00a8\u0006\u0010"}, d2 = {"Lcom/tenli/aiot/TenliApp;", "Landroid/app/Application;", "()V", "container", "Lcom/tenli/aiot/di/AppContainer;", "getContainer", "()Lcom/tenli/aiot/di/AppContainer;", "setContainer", "(Lcom/tenli/aiot/di/AppContainer;)V", "onCreate", "", "setupBackgroundWorkers", "setupNotificationChannels", "startKeepAliveService", "context", "Landroid/content/Context;", "app_debug"})
public final class TenliApp extends android.app.Application {
    public com.tenli.aiot.di.AppContainer container;
    
    public TenliApp() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.aiot.di.AppContainer getContainer() {
        return null;
    }
    
    public final void setContainer(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.di.AppContainer p0) {
    }
    
    @java.lang.Override()
    public void onCreate() {
    }
    
    private final void setupBackgroundWorkers() {
    }
    
    private final void setupNotificationChannels() {
    }
    
    private final void startKeepAliveService(android.content.Context context) {
    }
}