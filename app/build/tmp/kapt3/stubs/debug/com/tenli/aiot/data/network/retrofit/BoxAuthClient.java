package com.tenli.aiot.data.network.retrofit;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010%\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0011\u001a\u00020\u0012J/\u0010\u0013\u001a\u0002H\u0014\"\u0004\b\u0000\u0010\u00142\f\u0010\u0015\u001a\b\u0012\u0004\u0012\u0002H\u00140\u00162\u0006\u0010\u0017\u001a\u00020\u00102\u0006\u0010\u0018\u001a\u00020\u0010\u00a2\u0006\u0002\u0010\u0019J&\u0010\u0013\u001a\u0002H\u0014\"\u0006\b\u0000\u0010\u0014\u0018\u00012\u0006\u0010\u0017\u001a\u00020\u00102\u0006\u0010\u0018\u001a\u00020\u0010H\u0086\b\u00a2\u0006\u0002\u0010\u001aR\u001b\u0010\u0003\u001a\u00020\u00048BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0007\u0010\b\u001a\u0004\b\u0005\u0010\u0006R\u0016\u0010\t\u001a\n \u000b*\u0004\u0018\u00010\n0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u000e\u001a\u000e\u0012\u0004\u0012\u00020\u0010\u0012\u0004\u0012\u00020\u00010\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001b"}, d2 = {"Lcom/tenli/aiot/data/network/retrofit/BoxAuthClient;", "", "()V", "baseClient", "Lokhttp3/OkHttpClient;", "getBaseClient", "()Lokhttp3/OkHttpClient;", "baseClient$delegate", "Lkotlin/Lazy;", "gson", "Lcom/google/gson/Gson;", "kotlin.jvm.PlatformType", "logging", "Lokhttp3/logging/HttpLoggingInterceptor;", "serviceCache", "", "", "clearCache", "", "create", "T", "service", "Ljava/lang/Class;", "deviceDomain", "deviceToken", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;", "app_debug"})
public final class BoxAuthClient {
    private static final com.google.gson.Gson gson = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.Map<java.lang.String, java.lang.Object> serviceCache = null;
    @org.jetbrains.annotations.NotNull()
    private static final okhttp3.logging.HttpLoggingInterceptor logging = null;
    @org.jetbrains.annotations.NotNull()
    private static final kotlin.Lazy baseClient$delegate = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.tenli.aiot.data.network.retrofit.BoxAuthClient INSTANCE = null;
    
    private BoxAuthClient() {
        super();
    }
    
    private final okhttp3.OkHttpClient getBaseClient() {
        return null;
    }
    
    public final <T extends java.lang.Object>T create(@org.jetbrains.annotations.NotNull()
    java.lang.Class<T> service, @org.jetbrains.annotations.NotNull()
    java.lang.String deviceDomain, @org.jetbrains.annotations.NotNull()
    java.lang.String deviceToken) {
        return null;
    }
    
    public final void clearCache() {
    }
}