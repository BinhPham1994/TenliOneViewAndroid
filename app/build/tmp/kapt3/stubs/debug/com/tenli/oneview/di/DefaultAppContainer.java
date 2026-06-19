package com.tenli.oneview.di;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002R\u001b\u0010\u0003\u001a\u00020\u00048VX\u0096\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0007\u0010\b\u001a\u0004\b\u0005\u0010\u0006R\u001b\u0010\t\u001a\u00020\n8VX\u0096\u0084\u0002\u00a2\u0006\f\n\u0004\b\r\u0010\b\u001a\u0004\b\u000b\u0010\f\u00a8\u0006\u000e"}, d2 = {"Lcom/tenli/oneview/di/DefaultAppContainer;", "Lcom/tenli/oneview/di/AppContainer;", "()V", "authRepository", "Lcom/tenli/oneview/data/repository/AuthRepository;", "getAuthRepository", "()Lcom/tenli/oneview/data/repository/AuthRepository;", "authRepository$delegate", "Lkotlin/Lazy;", "dispatcherProvider", "Lcom/tenli/oneview/util/DispatcherProvider;", "getDispatcherProvider", "()Lcom/tenli/oneview/util/DispatcherProvider;", "dispatcherProvider$delegate", "app_debug"})
public final class DefaultAppContainer implements com.tenli.oneview.di.AppContainer {
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy authRepository$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy dispatcherProvider$delegate = null;
    
    public DefaultAppContainer() {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public com.tenli.oneview.data.repository.AuthRepository getAuthRepository() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public com.tenli.oneview.util.DispatcherProvider getDispatcherProvider() {
        return null;
    }
}