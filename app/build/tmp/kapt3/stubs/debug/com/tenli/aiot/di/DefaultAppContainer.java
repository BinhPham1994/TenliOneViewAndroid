package com.tenli.aiot.di;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002R\u001b\u0010\u0003\u001a\u00020\u00048VX\u0096\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0007\u0010\b\u001a\u0004\b\u0005\u0010\u0006R\u001b\u0010\t\u001a\u00020\n8VX\u0096\u0084\u0002\u00a2\u0006\f\n\u0004\b\r\u0010\b\u001a\u0004\b\u000b\u0010\fR\u001b\u0010\u000e\u001a\u00020\u000f8VX\u0096\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0012\u0010\b\u001a\u0004\b\u0010\u0010\u0011R\u001b\u0010\u0013\u001a\u00020\u00148VX\u0096\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0017\u0010\b\u001a\u0004\b\u0015\u0010\u0016R\u001b\u0010\u0018\u001a\u00020\u00198VX\u0096\u0084\u0002\u00a2\u0006\f\n\u0004\b\u001c\u0010\b\u001a\u0004\b\u001a\u0010\u001bR\u001b\u0010\u001d\u001a\u00020\u001e8VX\u0096\u0084\u0002\u00a2\u0006\f\n\u0004\b!\u0010\b\u001a\u0004\b\u001f\u0010 R\u001b\u0010\"\u001a\u00020#8VX\u0096\u0084\u0002\u00a2\u0006\f\n\u0004\b&\u0010\b\u001a\u0004\b$\u0010%\u00a8\u0006\'"}, d2 = {"Lcom/tenli/aiot/di/DefaultAppContainer;", "Lcom/tenli/aiot/di/AppContainer;", "()V", "alarmRepository", "Lcom/tenli/aiot/data/repository/AlarmRepository;", "getAlarmRepository", "()Lcom/tenli/aiot/data/repository/AlarmRepository;", "alarmRepository$delegate", "Lkotlin/Lazy;", "appRepository", "Lcom/tenli/aiot/data/repository/AppRepository;", "getAppRepository", "()Lcom/tenli/aiot/data/repository/AppRepository;", "appRepository$delegate", "authRepository", "Lcom/tenli/aiot/data/repository/AuthRepository;", "getAuthRepository", "()Lcom/tenli/aiot/data/repository/AuthRepository;", "authRepository$delegate", "boxRepository", "Lcom/tenli/aiot/data/repository/BoxRepository;", "getBoxRepository", "()Lcom/tenli/aiot/data/repository/BoxRepository;", "boxRepository$delegate", "dispatcherProvider", "Lcom/tenli/aiot/util/DispatcherProvider;", "getDispatcherProvider", "()Lcom/tenli/aiot/util/DispatcherProvider;", "dispatcherProvider$delegate", "eventRepository", "Lcom/tenli/aiot/data/repository/EventRepository;", "getEventRepository", "()Lcom/tenli/aiot/data/repository/EventRepository;", "eventRepository$delegate", "userRepository", "Lcom/tenli/aiot/data/repository/UserRepository;", "getUserRepository", "()Lcom/tenli/aiot/data/repository/UserRepository;", "userRepository$delegate", "app_debug"})
public final class DefaultAppContainer implements com.tenli.aiot.di.AppContainer {
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy alarmRepository$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy appRepository$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy authRepository$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy boxRepository$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy eventRepository$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy userRepository$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy dispatcherProvider$delegate = null;
    
    public DefaultAppContainer() {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public com.tenli.aiot.data.repository.AlarmRepository getAlarmRepository() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public com.tenli.aiot.data.repository.AppRepository getAppRepository() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public com.tenli.aiot.data.repository.AuthRepository getAuthRepository() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public com.tenli.aiot.data.repository.BoxRepository getBoxRepository() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public com.tenli.aiot.data.repository.EventRepository getEventRepository() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public com.tenli.aiot.data.repository.UserRepository getUserRepository() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public com.tenli.aiot.util.DispatcherProvider getDispatcherProvider() {
        return null;
    }
}