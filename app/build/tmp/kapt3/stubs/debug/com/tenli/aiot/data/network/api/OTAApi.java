package com.tenli.aiot.data.network.api;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\bf\u0018\u00002\u00020\u0001J\u0012\u0010\u0002\u001a\f\u0012\u0006\u0012\u0004\u0018\u00010\u0001\u0018\u00010\u0003H\'J\u0012\u0010\u0004\u001a\f\u0012\u0006\u0012\u0004\u0018\u00010\u0001\u0018\u00010\u0003H\'J\u001c\u0010\u0005\u001a\f\u0012\u0006\u0012\u0004\u0018\u00010\u0001\u0018\u00010\u00032\b\b\u0001\u0010\u0006\u001a\u00020\u0007H\'\u00a8\u0006\b"}, d2 = {"Lcom/tenli/aiot/data/network/api/OTAApi;", "", "getLastOTA", "Lretrofit2/Call;", "getStatus", "install", "raw", "Lcom/google/gson/JsonObject;", "app_debug"})
public abstract interface OTAApi {
    
    @retrofit2.http.GET(value = "ota/get-last")
    @org.jetbrains.annotations.Nullable()
    public abstract retrofit2.Call<java.lang.Object> getLastOTA();
    
    @retrofit2.http.GET(value = "/ota/default/quick-status")
    @org.jetbrains.annotations.Nullable()
    public abstract retrofit2.Call<java.lang.Object> getStatus();
    
    @retrofit2.http.POST(value = "/ota/default/quick-install")
    @org.jetbrains.annotations.Nullable()
    public abstract retrofit2.Call<java.lang.Object> install(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.google.gson.JsonObject raw);
}