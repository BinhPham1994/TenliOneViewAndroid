package com.tenli.oneview.di;

/**
 * Dependency Injection container for manual DI.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\bf\u0018\u00002\u00020\u0001R\u0012\u0010\u0002\u001a\u00020\u0003X\u00a6\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0004\u0010\u0005R\u0012\u0010\u0006\u001a\u00020\u0007X\u00a6\u0004\u00a2\u0006\u0006\u001a\u0004\b\b\u0010\tR\u0012\u0010\n\u001a\u00020\u000bX\u00a6\u0004\u00a2\u0006\u0006\u001a\u0004\b\f\u0010\rR\u0012\u0010\u000e\u001a\u00020\u000fX\u00a6\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0010\u0010\u0011R\u0012\u0010\u0012\u001a\u00020\u0013X\u00a6\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0014\u0010\u0015R\u0012\u0010\u0016\u001a\u00020\u0017X\u00a6\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0018\u0010\u0019R\u0012\u0010\u001a\u001a\u00020\u001bX\u00a6\u0004\u00a2\u0006\u0006\u001a\u0004\b\u001c\u0010\u001d\u00a8\u0006\u001e"}, d2 = {"Lcom/tenli/oneview/di/AppContainer;", "", "alarmRepository", "Lcom/tenli/oneview/data/repository/AlarmRepository;", "getAlarmRepository", "()Lcom/tenli/oneview/data/repository/AlarmRepository;", "appRepository", "Lcom/tenli/oneview/data/repository/AppRepository;", "getAppRepository", "()Lcom/tenli/oneview/data/repository/AppRepository;", "authRepository", "Lcom/tenli/oneview/data/repository/AuthRepository;", "getAuthRepository", "()Lcom/tenli/oneview/data/repository/AuthRepository;", "boxRepository", "Lcom/tenli/oneview/data/repository/BoxRepository;", "getBoxRepository", "()Lcom/tenli/oneview/data/repository/BoxRepository;", "dispatcherProvider", "Lcom/tenli/oneview/util/DispatcherProvider;", "getDispatcherProvider", "()Lcom/tenli/oneview/util/DispatcherProvider;", "eventRepository", "Lcom/tenli/oneview/data/repository/EventRepository;", "getEventRepository", "()Lcom/tenli/oneview/data/repository/EventRepository;", "userRepository", "Lcom/tenli/oneview/data/repository/UserRepository;", "getUserRepository", "()Lcom/tenli/oneview/data/repository/UserRepository;", "app_debug"})
public abstract interface AppContainer {
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.tenli.oneview.data.repository.AlarmRepository getAlarmRepository();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.tenli.oneview.data.repository.AppRepository getAppRepository();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.tenli.oneview.data.repository.AuthRepository getAuthRepository();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.tenli.oneview.data.repository.BoxRepository getBoxRepository();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.tenli.oneview.data.repository.EventRepository getEventRepository();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.tenli.oneview.data.repository.UserRepository getUserRepository();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.tenli.oneview.util.DispatcherProvider getDispatcherProvider();
}