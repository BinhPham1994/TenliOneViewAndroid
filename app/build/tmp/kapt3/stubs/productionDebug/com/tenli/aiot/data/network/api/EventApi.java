package com.tenli.aiot.data.network.api;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\t\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\b\bf\u0018\u00002\u00020\u0001JX\u0010\u0002\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\u00040\u00032\u0018\b\u0001\u0010\u0007\u001a\u0012\u0012\u0004\u0012\u00020\t0\bj\b\u0012\u0004\u0012\u00020\t`\n2\b\b\u0001\u0010\u000b\u001a\u00020\f2\b\b\u0003\u0010\r\u001a\u00020\f2\b\b\u0003\u0010\u000e\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\u000fJR\u0010\u0010\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\u00040\u00032\b\b\u0001\u0010\u0011\u001a\u00020\f2\b\b\u0001\u0010\u000b\u001a\u00020\f2\b\b\u0001\u0010\u0012\u001a\u00020\f2\b\b\u0003\u0010\r\u001a\u00020\f2\b\b\u0003\u0010\u000e\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\u0013J.\u0010\u0014\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00040\u00032\b\b\u0001\u0010\u0015\u001a\u00020\u00162\b\b\u0003\u0010\u0017\u001a\u00020\fH\u00a7@\u00a2\u0006\u0002\u0010\u0018J \u0010\u0019\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001a0\u00050\u00040\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u001bJ \u0010\u001c\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001d0\u00050\u00040\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u001bJ>\u0010\u001e\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\u00040\u00032\b\b\u0001\u0010\u0012\u001a\u00020\f2\b\b\u0003\u0010\r\u001a\u00020\f2\b\b\u0003\u0010\u000e\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\u001fJz\u0010 \u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\u00040\u00032\b\b\u0001\u0010\u0012\u001a\u00020\f2\u0010\b\u0003\u0010\u0007\u001a\n\u0012\u0004\u0012\u00020\t\u0018\u00010\u00052\n\b\u0003\u0010!\u001a\u0004\u0018\u00010\t2\n\b\u0003\u0010\"\u001a\u0004\u0018\u00010\t2\u0010\b\u0003\u0010\u000b\u001a\n\u0012\u0004\u0012\u00020\f\u0018\u00010\u00052\b\b\u0003\u0010\r\u001a\u00020\f2\b\b\u0003\u0010\u000e\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010#J>\u0010$\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\u00040\u00032\b\b\u0003\u0010\r\u001a\u00020\f2\b\b\u0001\u0010\u000b\u001a\u00020\f2\b\b\u0003\u0010\u000e\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\u001f\u00a8\u0006%"}, d2 = {"Lcom/tenli/aiot/data/network/api/EventApi;", "", "getEventByDeviceID", "Lretrofit2/Response;", "Lcom/tenli/aiot/model/network/BaseResponse;", "", "Lcom/tenli/aiot/model/network/EventItem;", "eType", "Ljava/util/ArrayList;", "", "Lkotlin/collections/ArrayList;", "deviceId", "", "pageSize", "orderByList", "(Ljava/util/ArrayList;IILjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getEventByMonitor", "monitorID", "pageNumber", "(IIIILjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getEventDetail", "id", "", "seen", "(JILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getEventType", "Lcom/tenli/aiot/model/network/EventTypeDef;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getEventTypeGroup", "Lcom/tenli/aiot/model/network/EventGroupDef;", "getListEvents", "(IILjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getListFilterEvents", "fromTime", "toTime", "(ILjava/util/List;Ljava/lang/String;Ljava/lang/String;Ljava/util/List;ILjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getRecentEvent", "app_productionDebug"})
public abstract interface EventApi {
    
    @retrofit2.http.GET(value = "event/get-list")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getRecentEvent(@retrofit2.http.Query(value = "pageSize")
    int pageSize, @retrofit2.http.Query(value = "deviceId")
    int deviceId, @retrofit2.http.Query(value = "orderByList")
    @org.jetbrains.annotations.NotNull()
    java.lang.String orderByList, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.aiot.model.network.BaseResponse<java.util.List<com.tenli.aiot.model.network.EventItem>>>> $completion);
    
    @retrofit2.http.GET(value = "event/get-list")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getEventByMonitor(@retrofit2.http.Query(value = "eProcessId")
    int monitorID, @retrofit2.http.Query(value = "deviceId")
    int deviceId, @retrofit2.http.Query(value = "pageNumber")
    int pageNumber, @retrofit2.http.Query(value = "pageSize")
    int pageSize, @retrofit2.http.Query(value = "orderByList")
    @org.jetbrains.annotations.NotNull()
    java.lang.String orderByList, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.aiot.model.network.BaseResponse<java.util.List<com.tenli.aiot.model.network.EventItem>>>> $completion);
    
    @retrofit2.http.GET(value = "event/get-list")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getEventByDeviceID(@retrofit2.http.Query(value = "eTypeGroup")
    @org.jetbrains.annotations.NotNull()
    java.util.ArrayList<java.lang.String> eType, @retrofit2.http.Query(value = "deviceId")
    int deviceId, @retrofit2.http.Query(value = "pageSize")
    int pageSize, @retrofit2.http.Query(value = "orderByList")
    @org.jetbrains.annotations.NotNull()
    java.lang.String orderByList, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.aiot.model.network.BaseResponse<java.util.List<com.tenli.aiot.model.network.EventItem>>>> $completion);
    
    @retrofit2.http.GET(value = "event/get-list")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getListEvents(@retrofit2.http.Query(value = "pageNumber")
    int pageNumber, @retrofit2.http.Query(value = "pageSize")
    int pageSize, @retrofit2.http.Query(value = "orderByList")
    @org.jetbrains.annotations.NotNull()
    java.lang.String orderByList, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.aiot.model.network.BaseResponse<java.util.List<com.tenli.aiot.model.network.EventItem>>>> $completion);
    
    @retrofit2.http.GET(value = "event/get-list")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getListFilterEvents(@retrofit2.http.Query(value = "pageNumber")
    int pageNumber, @retrofit2.http.Query(value = "eType")
    @org.jetbrains.annotations.Nullable()
    java.util.List<java.lang.String> eType, @retrofit2.http.Query(value = "fromTime")
    @org.jetbrains.annotations.Nullable()
    java.lang.String fromTime, @retrofit2.http.Query(value = "toTime")
    @org.jetbrains.annotations.Nullable()
    java.lang.String toTime, @retrofit2.http.Query(value = "deviceId")
    @org.jetbrains.annotations.Nullable()
    java.util.List<java.lang.Integer> deviceId, @retrofit2.http.Query(value = "pageSize")
    int pageSize, @retrofit2.http.Query(value = "orderByList")
    @org.jetbrains.annotations.NotNull()
    java.lang.String orderByList, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.aiot.model.network.BaseResponse<java.util.List<com.tenli.aiot.model.network.EventItem>>>> $completion);
    
    @retrofit2.http.GET(value = "event/get/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getEventDetail(@retrofit2.http.Path(value = "id")
    long id, @retrofit2.http.Query(value = "seen")
    int seen, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.aiot.model.network.BaseResponse<com.tenli.aiot.model.network.EventItem>>> $completion);
    
    @retrofit2.http.GET(value = "event/get-event-type-defines")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getEventType(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.aiot.model.network.BaseResponse<java.util.List<com.tenli.aiot.model.network.EventTypeDef>>>> $completion);
    
    @retrofit2.http.GET(value = "event/get-event-type-group-defines")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getEventTypeGroup(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.aiot.model.network.BaseResponse<java.util.List<com.tenli.aiot.model.network.EventGroupDef>>>> $completion);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
    }
}