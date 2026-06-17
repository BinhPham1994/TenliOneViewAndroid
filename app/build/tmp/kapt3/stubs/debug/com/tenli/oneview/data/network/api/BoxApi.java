package com.tenli.oneview.data.network.api;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000b\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u000b\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\bf\u0018\u00002\u00020\u0001J \u0010\u0002\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00010\u00032\b\b\u0001\u0010\u0004\u001a\u00020\u0001H\u00a7@\u00a2\u0006\u0002\u0010\u0005J \u0010\u0006\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00010\u00032\b\b\u0001\u0010\u0004\u001a\u00020\u0001H\u00a7@\u00a2\u0006\u0002\u0010\u0005J \u0010\u0007\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00010\u00032\b\b\u0001\u0010\u0004\u001a\u00020\u0001H\u00a7@\u00a2\u0006\u0002\u0010\u0005J \u0010\b\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00010\u00032\b\b\u0001\u0010\u0004\u001a\u00020\u0001H\u00a7@\u00a2\u0006\u0002\u0010\u0005J \u0010\t\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00010\u00032\b\b\u0001\u0010\u0004\u001a\u00020\u0001H\u00a7@\u00a2\u0006\u0002\u0010\u0005J \u0010\n\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00010\u00032\b\b\u0001\u0010\u0004\u001a\u00020\u0001H\u00a7@\u00a2\u0006\u0002\u0010\u0005J\u001e\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\f0\u00032\b\b\u0001\u0010\r\u001a\u00020\u000eH\u00a7@\u00a2\u0006\u0002\u0010\u000fJ\u001e\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\f0\u00032\b\b\u0001\u0010\r\u001a\u00020\u000eH\u00a7@\u00a2\u0006\u0002\u0010\u000fJ\u001e\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\f0\u00032\b\b\u0001\u0010\r\u001a\u00020\u0012H\u00a7@\u00a2\u0006\u0002\u0010\u0013J \u0010\u0014\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00010\u00032\b\b\u0001\u0010\r\u001a\u00020\u0012H\u00a7@\u00a2\u0006\u0002\u0010\u0013J\u0016\u0010\u0015\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00010\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0016J*\u0010\u0017\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00010\u00032\b\b\u0001\u0010\r\u001a\u00020\u000e2\b\b\u0001\u0010\u0004\u001a\u00020\u0001H\u00a7@\u00a2\u0006\u0002\u0010\u0018J*\u0010\u0019\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00010\u00032\b\b\u0001\u0010\r\u001a\u00020\u00122\b\b\u0001\u0010\u0004\u001a\u00020\u0001H\u00a7@\u00a2\u0006\u0002\u0010\u001aJ \u0010\u001b\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00010\u00032\b\b\u0001\u0010\u0004\u001a\u00020\u0001H\u00a7@\u00a2\u0006\u0002\u0010\u0005J\u0016\u0010\u001c\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00010\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0016J\u001a\u0010\u001d\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001f0\u001e0\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0016J\u0014\u0010 \u001a\b\u0012\u0004\u0012\u00020!0\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0016J \u0010\"\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00010\u00032\b\b\u0001\u0010#\u001a\u00020\u000eH\u00a7@\u00a2\u0006\u0002\u0010\u000fJ4\u0010$\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00010\u00032\b\b\u0001\u0010%\u001a\u00020\u00122\b\b\u0001\u0010&\u001a\u00020\u00122\b\b\u0001\u0010\'\u001a\u00020\u0012H\u00a7@\u00a2\u0006\u0002\u0010(J\u001a\u0010)\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020*0\u001e0\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0016J\u001a\u0010+\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020,0\u001e0\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0016J \u0010-\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00010\u00032\b\b\u0001\u0010\r\u001a\u00020\u0012H\u00a7@\u00a2\u0006\u0002\u0010\u0013J\u0016\u0010.\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00010\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0016J \u0010/\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u0002010\u001e000\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0016J\u0016\u00102\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00010\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0016J\u0014\u00103\u001a\b\u0012\u0004\u0012\u0002040\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0016J\u0014\u00105\u001a\b\u0012\u0004\u0012\u0002060\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0016J*\u00107\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00010\u00032\b\b\u0001\u0010\r\u001a\u00020\u00122\b\b\u0001\u0010\u0004\u001a\u00020\u0001H\u00a7@\u00a2\u0006\u0002\u0010\u001aJ*\u00108\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00010\u00032\b\b\u0001\u0010\r\u001a\u00020\u000e2\b\b\u0001\u0010\u0004\u001a\u00020\u0001H\u00a7@\u00a2\u0006\u0002\u0010\u0018J\u001e\u00109\u001a\b\u0012\u0004\u0012\u0002040\u00032\b\b\u0001\u0010\u0004\u001a\u000204H\u00a7@\u00a2\u0006\u0002\u0010:\u00a8\u0006;"}, d2 = {"Lcom/tenli/oneview/data/network/api/BoxApi;", "", "addCamera", "Lretrofit2/Response;", "raw", "(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "addMonitor", "addScript", "addSensor", "configDeviceKey", "controlDevice", "deleteCamera", "", "id", "", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteMonitor", "deleteScript", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteSensor", "discoveryDevice", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "editCamera", "(ILjava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "editScript", "(Ljava/lang/String;Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "editSensor", "getCameraStatus", "getCameras", "", "Lcom/tenli/oneview/model/network/CameraItem;", "getDeviceInformation", "Lcom/tenli/oneview/model/network/BoxSystemInfo;", "getListFace", "profileId", "getListProfile", "urlOnvif", "userName", "pass", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getMonitorType", "Lcom/tenli/oneview/model/network/MonitorType;", "getMonitors", "Lcom/tenli/oneview/model/network/Monitor;", "getParamDefault", "getProfiles", "getScripts", "Lcom/tenli/oneview/model/network/BaseResponse;", "Lcom/tenli/oneview/model/network/ScriptItem;", "getSensor", "getSystemSetting", "Lcom/tenli/oneview/model/network/SystemSettingResponse;", "getSystemState", "Lcom/tenli/oneview/model/network/BoxSystemState;", "monitorControl", "updateMonitor", "updateSystemSetting", "(Lcom/tenli/oneview/model/network/SystemSettingResponse;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public abstract interface BoxApi {
    
    @retrofit2.http.POST(value = "/api/Camera")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object addCamera(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    java.lang.Object raw, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.lang.Object>> $completion);
    
    @retrofit2.http.PUT(value = "/api/Camera/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object editCamera(@retrofit2.http.Path(value = "id")
    int id, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    java.lang.Object raw, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.lang.Object>> $completion);
    
    @retrofit2.http.DELETE(value = "/api/Camera/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteCamera(@retrofit2.http.Path(value = "id")
    int id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    @retrofit2.http.GET(value = "/api/Camera")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getCameras(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.CameraItem>>> $completion);
    
    @retrofit2.http.GET(value = "/api/CameraState")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getCameraStatus(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.lang.Object>> $completion);
    
    @retrofit2.http.GET(value = "/api/Monitor")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getMonitors(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.Monitor>>> $completion);
    
    @retrofit2.http.GET(value = "/api/MonitorType")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getMonitorType(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.MonitorType>>> $completion);
    
    @retrofit2.http.POST(value = "/api/Monitor")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object addMonitor(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    java.lang.Object raw, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.lang.Object>> $completion);
    
    @retrofit2.http.PUT(value = "/api/Monitor/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateMonitor(@retrofit2.http.Path(value = "id")
    int id, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    java.lang.Object raw, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.lang.Object>> $completion);
    
    @retrofit2.http.DELETE(value = "/api/Monitor/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteMonitor(@retrofit2.http.Path(value = "id")
    int id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    @retrofit2.http.GET(value = "/local/tcp2mqtt/default/camera")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getSensor(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.lang.Object>> $completion);
    
    @retrofit2.http.GET(value = "/nr/schedule/get")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getScripts(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.BaseResponse<java.util.List<com.tenli.oneview.model.network.ScriptItem>>>> $completion);
    
    @retrofit2.http.Headers(value = {"Content-Type: application/json"})
    @retrofit2.http.POST(value = "/nr/schedule/add")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object addScript(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    java.lang.Object raw, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.lang.Object>> $completion);
    
    @retrofit2.http.Headers(value = {"Content-Type: application/json"})
    @retrofit2.http.PUT(value = "/nr/schedule/update/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object editScript(@retrofit2.http.Path(value = "id")
    @org.jetbrains.annotations.NotNull()
    java.lang.String id, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    java.lang.Object raw, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.lang.Object>> $completion);
    
    @retrofit2.http.DELETE(value = "/nr/schedule/delete/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteScript(@retrofit2.http.Path(value = "id")
    @org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    @retrofit2.http.Headers(value = {"Content-Type: application/json"})
    @retrofit2.http.POST(value = "/local/tcp2mqtt/default/camera/save")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object addSensor(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    java.lang.Object raw, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.lang.Object>> $completion);
    
    @retrofit2.http.Headers(value = {"Content-Type: application/json"})
    @retrofit2.http.POST(value = "/local/tcp2mqtt/default/camera/save")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object editSensor(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    java.lang.Object raw, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.lang.Object>> $completion);
    
    @retrofit2.http.DELETE(value = "/local/tcp2mqtt/default/camera/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteSensor(@retrofit2.http.Path(value = "id")
    @org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.lang.Object>> $completion);
    
    @retrofit2.http.GET(value = "/api/SystemState")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getSystemState(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.BoxSystemState>> $completion);
    
    @retrofit2.http.GET(value = "/api/SystemInfo")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getDeviceInformation(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.BoxSystemInfo>> $completion);
    
    @retrofit2.http.GET(value = "/api/SystemSetting")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getSystemSetting(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.SystemSettingResponse>> $completion);
    
    @retrofit2.http.POST(value = "/api/SystemSetting")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateSystemSetting(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.SystemSettingResponse raw, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.SystemSettingResponse>> $completion);
    
    @retrofit2.http.POST(value = "/api/SystemKey")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object configDeviceKey(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    java.lang.Object raw, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.lang.Object>> $completion);
    
    @retrofit2.http.POST(value = "/api/SystemControl")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object controlDevice(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    java.lang.Object raw, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.lang.Object>> $completion);
    
    @retrofit2.http.POST(value = "/api/MonitorControl/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object monitorControl(@retrofit2.http.Path(value = "id")
    @org.jetbrains.annotations.NotNull()
    java.lang.String id, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    java.lang.Object raw, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.lang.Object>> $completion);
    
    @retrofit2.http.GET(value = "/local/onvif/discovery")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object discoveryDevice(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.lang.Object>> $completion);
    
    @retrofit2.http.FormUrlEncoded()
    @retrofit2.http.POST(value = "/local/onvif/listProfile")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getListProfile(@retrofit2.http.Field(value = "xaddr")
    @org.jetbrains.annotations.NotNull()
    java.lang.String urlOnvif, @retrofit2.http.Field(value = "user")
    @org.jetbrains.annotations.NotNull()
    java.lang.String userName, @retrofit2.http.Field(value = "pass")
    @org.jetbrains.annotations.NotNull()
    java.lang.String pass, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.lang.Object>> $completion);
    
    @retrofit2.http.GET(value = "/api/MonitorParam/{monitorType}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getParamDefault(@retrofit2.http.Path(value = "monitorType")
    @org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.lang.Object>> $completion);
    
    @retrofit2.http.GET(value = "/api/Profile")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getProfiles(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.lang.Object>> $completion);
    
    @retrofit2.http.GET(value = "/api/Face")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getListFace(@retrofit2.http.Query(value = "profileId")
    int profileId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.lang.Object>> $completion);
}