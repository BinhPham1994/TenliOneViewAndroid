package com.tenli.oneview.data.network.api;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\b\u0005\bf\u0018\u00002\u00020\u0001J>\u0010\u0002\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00010\u00032\b\b\u0001\u0010\u0004\u001a\u00020\u00052\b\b\u0001\u0010\u0006\u001a\u00020\u00052\b\b\u0001\u0010\u0007\u001a\u00020\u00052\b\b\u0001\u0010\b\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ \u0010\u000b\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00010\u00032\b\b\u0001\u0010\f\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\rJ*\u0010\u000e\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00010\u00032\b\b\u0001\u0010\u000f\u001a\u00020\t2\b\b\u0001\u0010\u0010\u001a\u00020\u0001H\u00a7@\u00a2\u0006\u0002\u0010\u0011J \u0010\u0012\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00010\u00032\b\b\u0001\u0010\f\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\rJ$\u0010\u0013\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00150\u00140\u00032\b\b\u0001\u0010\f\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\rJ \u0010\u0016\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00150\u00170\u00140\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0018J*\u0010\u0019\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00010\u00032\b\b\u0001\u0010\f\u001a\u00020\t2\b\b\u0001\u0010\u001a\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u001b\u00a8\u0006\u001c"}, d2 = {"Lcom/tenli/oneview/data/network/api/DeviceApi;", "", "addDevice", "Lretrofit2/Response;", "name", "", "deviceCode", "key", "userGroupId", "", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteDevice", "deviceId", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "editDevice", "id", "raw", "(ILjava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAlarm", "getDeviceDetail", "Lcom/tenli/oneview/model/network/BaseResponse;", "Lcom/tenli/oneview/model/network/DeviceItem;", "getListDevice", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "setAlarm", "pauseUtil", "(ILjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public abstract interface DeviceApi {
    
    @retrofit2.http.FormUrlEncoded()
    @retrofit2.http.POST(value = "device/create")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object addDevice(@retrofit2.http.Field(value = "name")
    @org.jetbrains.annotations.NotNull()
    java.lang.String name, @retrofit2.http.Field(value = "deviceCode")
    @org.jetbrains.annotations.NotNull()
    java.lang.String deviceCode, @retrofit2.http.Field(value = "key")
    @org.jetbrains.annotations.NotNull()
    java.lang.String key, @retrofit2.http.Field(value = "userGroupId")
    int userGroupId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.lang.Object>> $completion);
    
    @retrofit2.http.GET(value = "device/get-list")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getListDevice(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.BaseResponse<java.util.List<com.tenli.oneview.model.network.DeviceItem>>>> $completion);
    
    @retrofit2.http.DELETE(value = "device/delete/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteDevice(@retrofit2.http.Path(value = "id")
    int deviceId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.lang.Object>> $completion);
    
    @retrofit2.http.GET(value = "device/get/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getDeviceDetail(@retrofit2.http.Path(value = "id")
    int deviceId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.BaseResponse<com.tenli.oneview.model.network.DeviceItem>>> $completion);
    
    @retrofit2.http.PUT(value = "device/update/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object editDevice(@retrofit2.http.Path(value = "id")
    int id, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    java.lang.Object raw, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.lang.Object>> $completion);
    
    @retrofit2.http.GET(value = "device/get-alarm-options/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getAlarm(@retrofit2.http.Path(value = "id")
    int deviceId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.lang.Object>> $completion);
    
    @retrofit2.http.FormUrlEncoded()
    @retrofit2.http.POST(value = "device/update-alarm-options/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object setAlarm(@retrofit2.http.Path(value = "id")
    int deviceId, @retrofit2.http.Field(value = "pauseUtil")
    @org.jetbrains.annotations.NotNull()
    java.lang.String pauseUtil, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.lang.Object>> $completion);
}