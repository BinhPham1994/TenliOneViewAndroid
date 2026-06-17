package com.tenli.aiot.data.network.api;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000r\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\t\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J.\u0010\u0002\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00040\u00032\b\b\u0001\u0010\u0006\u001a\u00020\u00072\b\b\u0001\u0010\b\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010\tJB\u0010\n\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00040\u00032\b\b\u0001\u0010\u000b\u001a\u00020\u00072\b\b\u0001\u0010\f\u001a\u00020\u00072\b\b\u0001\u0010\r\u001a\u00020\u00072\b\b\u0001\u0010\u000e\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010\u000fJ \u0010\u0010\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00120\u00110\u00040\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0013J\u001a\u0010\u0014\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00040\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0013J$\u0010\u0015\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00160\u00040\u00032\b\b\u0001\u0010\u0017\u001a\u00020\u0018H\u00a7@\u00a2\u0006\u0002\u0010\u0019J\u001a\u0010\u001a\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00040\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0013J0\u0010\u001b\u001a\u0010\u0012\f\u0012\n\u0012\u0006\u0012\u0004\u0018\u00010\u00010\u00040\u00032\b\b\u0001\u0010\u001c\u001a\u00020\u001d2\b\b\u0003\u0010\u001e\u001a\u00020\u001fH\u00a7@\u00a2\u0006\u0002\u0010 J\u001a\u0010!\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00040\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0013J(\u0010\"\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020$0\u00040#2\b\b\u0001\u0010%\u001a\u00020\u00072\b\b\u0003\u0010&\u001a\u00020\u0007H\'J8\u0010\'\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00040\u00032\b\b\u0001\u0010\f\u001a\u00020\u00072\b\b\u0001\u0010\b\u001a\u00020\u00072\b\b\u0001\u0010\u000e\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010(J$\u0010)\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020*0\u00040\u00032\b\b\u0001\u0010+\u001a\u00020,H\u00a7@\u00a2\u0006\u0002\u0010-J$\u0010.\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00040\u00032\b\b\u0001\u0010\u0017\u001a\u00020/H\u00a7@\u00a2\u0006\u0002\u00100J.\u00101\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00040\u00032\b\b\u0001\u0010\f\u001a\u00020\u00072\b\b\u0001\u00102\u001a\u00020\u001fH\u00a7@\u00a2\u0006\u0002\u00103J.\u00104\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u0002050\u00040\u00032\b\b\u0001\u0010\f\u001a\u00020\u00072\b\b\u0001\u00106\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010\t\u00a8\u00067"}, d2 = {"Lcom/tenli/aiot/data/network/api/UserApi;", "", "changePassword", "Lretrofit2/Response;", "Lcom/tenli/aiot/model/network/BaseResponse;", "", "oldPassword", "", "newPassword", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "create", "name", "email", "password", "verifyAuthCode", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getClients", "", "Lcom/tenli/aiot/model/network/ClientDevice;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getInfoUser", "login", "Lcom/tenli/aiot/model/network/LoginResponseData;", "request", "Lcom/tenli/aiot/model/network/LoginRequest;", "(Lcom/tenli/aiot/model/network/LoginRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "logout", "logoutClient", "id", "", "force", "", "(IZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "ping", "refreshTokenSync", "Lretrofit2/Call;", "Lcom/tenli/aiot/model/network/RefreshTokenData;", "token", "grantType", "resetPassword", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateAvatar", "Lcom/tenli/aiot/model/network/UserData;", "image", "Lokhttp3/MultipartBody$Part;", "(Lokhttp3/MultipartBody$Part;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateUser", "Lcom/tenli/aiot/model/network/UpdateUserRequest;", "(Lcom/tenli/aiot/model/network/UpdateUserRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "verifyEmail", "existRequired", "(Ljava/lang/String;ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "verifyOTP", "Lcom/tenli/aiot/model/network/OTPData;", "otpCode", "app_productionDebug"})
public abstract interface UserApi {
    
    @retrofit2.http.Headers(value = {"Content-Type: application/json"})
    @retrofit2.http.POST(value = "auth/login")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object login(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.aiot.model.network.LoginRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.aiot.model.network.BaseResponse<com.tenli.aiot.model.network.LoginResponseData>>> $completion);
    
    @retrofit2.http.FormUrlEncoded()
    @retrofit2.http.POST(value = "user/create")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object create(@retrofit2.http.Field(value = "name")
    @org.jetbrains.annotations.NotNull()
    java.lang.String name, @retrofit2.http.Field(value = "email")
    @org.jetbrains.annotations.NotNull()
    java.lang.String email, @retrofit2.http.Field(value = "password")
    @org.jetbrains.annotations.NotNull()
    java.lang.String password, @retrofit2.http.Field(value = "verifyAuthCode")
    @org.jetbrains.annotations.NotNull()
    java.lang.String verifyAuthCode, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.aiot.model.network.BaseResponse<kotlin.Unit>>> $completion);
    
    @retrofit2.http.FormUrlEncoded()
    @retrofit2.http.POST(value = "user/create-new-password")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object resetPassword(@retrofit2.http.Field(value = "email")
    @org.jetbrains.annotations.NotNull()
    java.lang.String email, @retrofit2.http.Field(value = "new_password")
    @org.jetbrains.annotations.NotNull()
    java.lang.String newPassword, @retrofit2.http.Field(value = "verifyAuthCode")
    @org.jetbrains.annotations.NotNull()
    java.lang.String verifyAuthCode, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.aiot.model.network.BaseResponse<kotlin.Unit>>> $completion);
    
    @retrofit2.http.FormUrlEncoded()
    @retrofit2.http.POST(value = "auth/token")
    @org.jetbrains.annotations.NotNull()
    public abstract retrofit2.Call<com.tenli.aiot.model.network.BaseResponse<com.tenli.aiot.model.network.RefreshTokenData>> refreshTokenSync(@retrofit2.http.Field(value = "token")
    @org.jetbrains.annotations.NotNull()
    java.lang.String token, @retrofit2.http.Field(value = "grant_type")
    @org.jetbrains.annotations.NotNull()
    java.lang.String grantType);
    
    @retrofit2.http.POST(value = "auth/logout")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object logout(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.aiot.model.network.BaseResponse<kotlin.Unit>>> $completion);
    
    @retrofit2.http.GET(value = "user")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getInfoUser(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.aiot.model.network.BaseResponse<kotlin.Unit>>> $completion);
    
    @retrofit2.http.GET(value = "system/ping")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object ping(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.aiot.model.network.BaseResponse<kotlin.Unit>>> $completion);
    
    @retrofit2.http.FormUrlEncoded()
    @retrofit2.http.POST(value = "user/change-password")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object changePassword(@retrofit2.http.Field(value = "old_password")
    @org.jetbrains.annotations.NotNull()
    java.lang.String oldPassword, @retrofit2.http.Field(value = "new_password")
    @org.jetbrains.annotations.NotNull()
    java.lang.String newPassword, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.aiot.model.network.BaseResponse<kotlin.Unit>>> $completion);
    
    @retrofit2.http.GET(value = "auth/user-app-client/get-list")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getClients(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.aiot.model.network.BaseResponse<java.util.List<com.tenli.aiot.model.network.ClientDevice>>>> $completion);
    
    @retrofit2.http.FormUrlEncoded()
    @retrofit2.http.POST(value = "auth/user-app-client/remove/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object logoutClient(@retrofit2.http.Path(value = "id")
    int id, @retrofit2.http.Field(value = "force")
    boolean force, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.aiot.model.network.BaseResponse<java.lang.Object>>> $completion);
    
    @retrofit2.http.FormUrlEncoded()
    @retrofit2.http.POST(value = "verify/send-verify-email")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object verifyEmail(@retrofit2.http.Field(value = "email")
    @org.jetbrains.annotations.NotNull()
    java.lang.String email, @retrofit2.http.Field(value = "existRequired")
    boolean existRequired, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.aiot.model.network.BaseResponse<kotlin.Unit>>> $completion);
    
    @retrofit2.http.FormUrlEncoded()
    @retrofit2.http.POST(value = "verify/verify-email")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object verifyOTP(@retrofit2.http.Field(value = "email")
    @org.jetbrains.annotations.NotNull()
    java.lang.String email, @retrofit2.http.Field(value = "otpCode")
    @org.jetbrains.annotations.NotNull()
    java.lang.String otpCode, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.aiot.model.network.BaseResponse<com.tenli.aiot.model.network.OTPData>>> $completion);
    
    @retrofit2.http.PUT(value = "user/update")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateUser(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.aiot.model.network.UpdateUserRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.aiot.model.network.BaseResponse<kotlin.Unit>>> $completion);
    
    @retrofit2.http.Multipart()
    @retrofit2.http.PUT(value = "user/update-avatar")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateAvatar(@retrofit2.http.Part()
    @org.jetbrains.annotations.NotNull()
    okhttp3.MultipartBody.Part image, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.aiot.model.network.BaseResponse<com.tenli.aiot.model.network.UserData>>> $completion);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
    }
}