package com.tenli.aiot.data.network.api;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\b\bf\u0018\u00002\u00020\u0001J*\u0010\u0002\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00010\u00032\b\b\u0001\u0010\u0004\u001a\u00020\u00052\b\b\u0003\u0010\u0006\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0007JD\u0010\b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\u00032\b\b\u0001\u0010\u000b\u001a\u00020\f2\b\b\u0001\u0010\r\u001a\u00020\u00052\b\b\u0003\u0010\u000e\u001a\u00020\u000f2\n\b\u0003\u0010\u0010\u001a\u0004\u0018\u00010\fH\u00a7@\u00a2\u0006\u0002\u0010\u0011J \u0010\u0012\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00010\u00032\b\b\u0001\u0010\u000b\u001a\u00020\fH\u00a7@\u00a2\u0006\u0002\u0010\u0013J*\u0010\u0014\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00010\u00032\b\b\u0001\u0010\u000b\u001a\u00020\f2\b\b\u0001\u0010\u0015\u001a\u00020\fH\u00a7@\u00a2\u0006\u0002\u0010\u0016J*\u0010\u0017\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00010\u00032\b\b\u0001\u0010\u000b\u001a\u00020\f2\b\b\u0001\u0010\u0018\u001a\u00020\u0001H\u00a7@\u00a2\u0006\u0002\u0010\u0019J$\u0010\u001a\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001b0\t0\u00032\b\b\u0001\u0010\u000b\u001a\u00020\fH\u00a7@\u00a2\u0006\u0002\u0010\u0013J \u0010\u001c\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001e0\u001d0\t0\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u001fJ \u0010 \u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00010\u00032\b\b\u0001\u0010!\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\"J*\u0010#\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00010\u00032\b\b\u0001\u0010\u000b\u001a\u00020\f2\b\b\u0003\u0010$\u001a\u00020\u000fH\u00a7@\u00a2\u0006\u0002\u0010%\u00a8\u0006&"}, d2 = {"Lcom/tenli/aiot/data/network/api/GroupApi;", "", "createGroup", "Lretrofit2/Response;", "name", "", "itemType", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createShareCode", "Lcom/tenli/aiot/model/network/BaseResponse;", "Lcom/tenli/aiot/model/network/ShareCodeData;", "id", "", "role", "createNew", "", "expiredIn", "(ILjava/lang/String;ZLjava/lang/Integer;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteGroup", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteUser", "userId", "(IILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "editGroup", "raw", "(ILjava/lang/Object;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getGroupDetail", "Lcom/tenli/aiot/model/network/GroupDetailData;", "getListGroup", "", "Lcom/tenli/aiot/model/network/HomeGroup;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "joinGroup", "shareCode", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "leaveGroup", "force", "(IZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public abstract interface GroupApi {
    
    @retrofit2.http.GET(value = "user/userItemGroup/get-list")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getListGroup(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.aiot.model.network.BaseResponse<java.util.List<com.tenli.aiot.model.network.HomeGroup>>>> $completion);
    
    @retrofit2.http.GET(value = "user/userItemGroup/get/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getGroupDetail(@retrofit2.http.Path(value = "id")
    int id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.aiot.model.network.BaseResponse<com.tenli.aiot.model.network.GroupDetailData>>> $completion);
    
    @retrofit2.http.FormUrlEncoded()
    @retrofit2.http.POST(value = "user/userItemGroup/leave/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object leaveGroup(@retrofit2.http.Path(value = "id")
    int id, @retrofit2.http.Field(value = "force")
    boolean force, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.lang.Object>> $completion);
    
    @retrofit2.http.FormUrlEncoded()
    @retrofit2.http.POST(value = "user/userItemGroup/create")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createGroup(@retrofit2.http.Field(value = "name")
    @org.jetbrains.annotations.NotNull()
    java.lang.String name, @retrofit2.http.Field(value = "itemType")
    @org.jetbrains.annotations.NotNull()
    java.lang.String itemType, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.lang.Object>> $completion);
    
    @retrofit2.http.PUT(value = "user/userItemGroup/update/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object editGroup(@retrofit2.http.Path(value = "id")
    int id, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    java.lang.Object raw, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.lang.Object>> $completion);
    
    @retrofit2.http.DELETE(value = "user/userItemGroup/delete/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteGroup(@retrofit2.http.Path(value = "id")
    int id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.lang.Object>> $completion);
    
    @retrofit2.http.FormUrlEncoded()
    @retrofit2.http.POST(value = "user/userItemGroup/create-share-code/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createShareCode(@retrofit2.http.Path(value = "id")
    int id, @retrofit2.http.Field(value = "role")
    @org.jetbrains.annotations.NotNull()
    java.lang.String role, @retrofit2.http.Field(value = "createNew")
    boolean createNew, @retrofit2.http.Field(value = "expiredIn")
    @org.jetbrains.annotations.Nullable()
    java.lang.Integer expiredIn, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.aiot.model.network.BaseResponse<com.tenli.aiot.model.network.ShareCodeData>>> $completion);
    
    @retrofit2.http.FormUrlEncoded()
    @retrofit2.http.POST(value = "user/userItemGroup/join")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object joinGroup(@retrofit2.http.Field(value = "shareCode")
    @org.jetbrains.annotations.NotNull()
    java.lang.String shareCode, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.lang.Object>> $completion);
    
    @retrofit2.http.FormUrlEncoded()
    @retrofit2.http.POST(value = "user/userItemGroup/remove-user/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteUser(@retrofit2.http.Path(value = "id")
    int id, @retrofit2.http.Field(value = "userId")
    int userId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.lang.Object>> $completion);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
    }
}