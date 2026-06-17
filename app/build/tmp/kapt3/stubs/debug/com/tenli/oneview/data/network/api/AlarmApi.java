package com.tenli.oneview.data.network.api;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\bf\u0018\u00002\u00020\u0001J$\u0010\u0002\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00040\u00032\b\b\u0001\u0010\u0006\u001a\u00020\u0001H\u00a7@\u00a2\u0006\u0002\u0010\u0007J$\u0010\b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\u00040\u00032\b\b\u0001\u0010\u0006\u001a\u00020\u0001H\u00a7@\u00a2\u0006\u0002\u0010\u0007J$\u0010\n\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00040\u00032\b\b\u0001\u0010\u0006\u001a\u00020\u0001H\u00a7@\u00a2\u0006\u0002\u0010\u0007J\u001a\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u00040\u0003H\u00a7@\u00a2\u0006\u0002\u0010\rJ$\u0010\u000e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\u00040\u00032\b\b\u0001\u0010\u0010\u001a\u00020\u0011H\u00a7@\u00a2\u0006\u0002\u0010\u0012J$\u0010\u0013\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00140\u00040\u00032\b\b\u0001\u0010\u0006\u001a\u00020\u0001H\u00a7@\u00a2\u0006\u0002\u0010\u0007J$\u0010\u0015\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\u00040\u00032\b\b\u0001\u0010\u0006\u001a\u00020\u0001H\u00a7@\u00a2\u0006\u0002\u0010\u0007J$\u0010\u0016\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\u00040\u00032\b\b\u0001\u0010\u0006\u001a\u00020\u0001H\u00a7@\u00a2\u0006\u0002\u0010\u0007J$\u0010\u0017\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\u00040\u00032\b\b\u0001\u0010\u0006\u001a\u00020\u0001H\u00a7@\u00a2\u0006\u0002\u0010\u0007\u00a8\u0006\u0018"}, d2 = {"Lcom/tenli/oneview/data/network/api/AlarmApi;", "", "createScript", "Lretrofit2/Response;", "Lcom/tenli/oneview/model/network/BaseResponse;", "Lcom/tenli/oneview/model/network/AddScriptResponse;", "raw", "(Ljava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteScript", "", "editScript", "getNotifySetting", "Lcom/tenli/oneview/model/network/NotificationSettingResponse;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getNotifySettingEventGroup", "Lcom/tenli/oneview/model/network/EventTypeGroupDetail;", "id", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateEventType", "Lcom/tenli/oneview/model/network/UpdateNotificationResponse;", "updateNotifySetting", "updateNotifySettingEventGroup", "updateUserNotifySetting", "app_debug"})
public abstract interface AlarmApi {
    
    @retrofit2.http.GET(value = "user/get-notification-setting")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getNotifySetting(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.BaseResponse<com.tenli.oneview.model.network.NotificationSettingResponse>>> $completion);
    
    @retrofit2.http.POST(value = "user/update-notification-setting")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateNotifySetting(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    java.lang.Object raw, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.BaseResponse<kotlin.Unit>>> $completion);
    
    @retrofit2.http.POST(value = "user/update-notification-setting/user")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateUserNotifySetting(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    java.lang.Object raw, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.BaseResponse<kotlin.Unit>>> $completion);
    
    @retrofit2.http.POST(value = "user/update-notification-setting/event-type-group")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateNotifySettingEventGroup(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    java.lang.Object raw, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.BaseResponse<kotlin.Unit>>> $completion);
    
    @retrofit2.http.GET(value = "user/get-notification-setting/event-type-group/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getNotifySettingEventGroup(@retrofit2.http.Path(value = "id")
    @org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.BaseResponse<com.tenli.oneview.model.network.EventTypeGroupDetail>>> $completion);
    
    @retrofit2.http.POST(value = "user/update-notification-setting/event-type-group/add-custom-script")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createScript(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    java.lang.Object raw, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.BaseResponse<com.tenli.oneview.model.network.AddScriptResponse>>> $completion);
    
    @retrofit2.http.PUT(value = "user/update-notification-setting/event-type-group/update-custom-script")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object editScript(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    java.lang.Object raw, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.BaseResponse<com.tenli.oneview.model.network.AddScriptResponse>>> $completion);
    
    @retrofit2.http.HTTP(method = "DELETE", path = "user/update-notification-setting/event-type-group/remove-custom-script", hasBody = true)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteScript(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    java.lang.Object raw, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.BaseResponse<kotlin.Unit>>> $completion);
    
    @retrofit2.http.POST(value = "user/update-notification-setting/event-type")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateEventType(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    java.lang.Object raw, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.BaseResponse<com.tenli.oneview.model.network.UpdateNotificationResponse>>> $completion);
}