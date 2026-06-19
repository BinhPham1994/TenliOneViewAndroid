package com.tenli.oneview.data.network.api;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u00a0\u0001\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\t\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010$\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J\u001e\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0004H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u001e\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\b0\u00032\b\b\u0001\u0010\u0005\u001a\u00020\bH\u00a7@\u00a2\u0006\u0002\u0010\tJ\u001e\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u000b0\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u000bH\u00a7@\u00a2\u0006\u0002\u0010\fJ\u001e\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000e0\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u000eH\u00a7@\u00a2\u0006\u0002\u0010\u000fJ\u001e\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00110\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0011H\u00a7@\u00a2\u0006\u0002\u0010\u0012J\u001e\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00140\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0014H\u00a7@\u00a2\u0006\u0002\u0010\u0015J\u001e\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00170\u00032\b\b\u0001\u0010\u0018\u001a\u00020\u0019H\u00a7@\u00a2\u0006\u0002\u0010\u001aJ\u001e\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00170\u00032\b\b\u0001\u0010\u001c\u001a\u00020\u0019H\u00a7@\u00a2\u0006\u0002\u0010\u001aJ\u001e\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u00170\u00032\b\b\u0001\u0010\u001e\u001a\u00020\u0019H\u00a7@\u00a2\u0006\u0002\u0010\u001aJ\u001e\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u00170\u00032\b\b\u0001\u0010\u001e\u001a\u00020\u0019H\u00a7@\u00a2\u0006\u0002\u0010\u001aJ\u001e\u0010 \u001a\b\u0012\u0004\u0012\u00020\u00170\u00032\b\b\u0001\u0010\u001c\u001a\u00020\u0019H\u00a7@\u00a2\u0006\u0002\u0010\u001aJ\u001e\u0010!\u001a\b\u0012\u0004\u0012\u00020\u00170\u00032\b\b\u0001\u0010\u001e\u001a\u00020\u0019H\u00a7@\u00a2\u0006\u0002\u0010\u001aJ2\u0010\"\u001a\b\u0012\u0004\u0012\u00020#0\u00032\b\b\u0001\u0010$\u001a\u00020%2\b\b\u0001\u0010&\u001a\u00020%2\b\b\u0001\u0010\'\u001a\u00020(H\u00a7@\u00a2\u0006\u0002\u0010)J(\u0010*\u001a\b\u0012\u0004\u0012\u00020+0\u00032\b\b\u0001\u0010,\u001a\u00020%2\b\b\u0001\u0010$\u001a\u00020%H\u00a7@\u00a2\u0006\u0002\u0010-J\u001e\u0010.\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\b\b\u0001\u0010\'\u001a\u00020(H\u00a7@\u00a2\u0006\u0002\u0010/J\u001e\u00100\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\b\b\u0001\u0010\'\u001a\u00020(H\u00a7@\u00a2\u0006\u0002\u0010/J(\u00101\u001a\b\u0012\u0004\u0012\u0002020\u00032\b\b\u0001\u00103\u001a\u00020\u00192\b\b\u0001\u0010\u0005\u001a\u000202H\u00a7@\u00a2\u0006\u0002\u00104J\u001a\u00105\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u0004060\u0003H\u00a7@\u00a2\u0006\u0002\u00107J$\u00108\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u000209060\u00032\b\b\u0001\u00103\u001a\u00020\u0019H\u00a7@\u00a2\u0006\u0002\u0010\u001aJ\u001a\u0010:\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b060\u0003H\u00a7@\u00a2\u0006\u0002\u00107J\u001a\u0010;\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b060\u0003H\u00a7@\u00a2\u0006\u0002\u00107J\u001e\u0010<\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\b\b\u0001\u0010=\u001a\u00020%H\u00a7@\u00a2\u0006\u0002\u0010>J\u001a\u0010?\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020@060\u0003H\u00a7@\u00a2\u0006\u0002\u00107J\u001a\u0010A\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020B060\u0003H\u00a7@\u00a2\u0006\u0002\u00107J\u001a\u0010C\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u0011060\u0003H\u00a7@\u00a2\u0006\u0002\u00107J\u001a\u0010D\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e060\u0003H\u00a7@\u00a2\u0006\u0002\u00107J$\u0010E\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u0014060\u00032\b\b\u0001\u00103\u001a\u00020\u0019H\u00a7@\u00a2\u0006\u0002\u0010\u001aJ*\u0010F\u001a\b\u0012\u0004\u0012\u00020\u00010\u00032\u0014\b\u0001\u0010G\u001a\u000e\u0012\u0004\u0012\u00020%\u0012\u0004\u0012\u00020%0HH\u00a7@\u00a2\u0006\u0002\u0010IJ(\u0010J\u001a\b\u0012\u0004\u0012\u00020\b0\u00032\b\b\u0001\u0010\u001e\u001a\u00020\u00192\b\b\u0001\u0010\u0005\u001a\u00020\bH\u00a7@\u00a2\u0006\u0002\u0010KJ(\u0010L\u001a\b\u0012\u0004\u0012\u00020\u000b0\u00032\b\b\u0001\u0010\u001e\u001a\u00020\u00192\b\b\u0001\u0010\u0005\u001a\u00020\u000bH\u00a7@\u00a2\u0006\u0002\u0010MJ(\u0010N\u001a\b\u0012\u0004\u0012\u00020\u000e0\u00032\b\b\u0001\u0010\u001e\u001a\u00020\u00192\b\b\u0001\u0010\u0005\u001a\u00020\u000eH\u00a7@\u00a2\u0006\u0002\u0010OJ(\u0010P\u001a\b\u0012\u0004\u0012\u00020\u00110\u00032\b\b\u0001\u0010\u001e\u001a\u00020\u00192\b\b\u0001\u0010\u0005\u001a\u00020\u0011H\u00a7@\u00a2\u0006\u0002\u0010QJ\u001e\u0010R\u001a\b\u0012\u0004\u0012\u00020S0\u00032\b\b\u0001\u0010\u0005\u001a\u00020SH\u00a7@\u00a2\u0006\u0002\u0010T\u00a8\u0006U"}, d2 = {"Lcom/tenli/oneview/data/network/api/AiApi;", "", "createCameraMonitor", "Lretrofit2/Response;", "Lcom/tenli/oneview/model/network/CameraMonitor;", "model", "(Lcom/tenli/oneview/model/network/CameraMonitor;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createLicensePlate", "Lcom/tenli/oneview/model/network/LicensePlate;", "(Lcom/tenli/oneview/model/network/LicensePlate;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createMonitor", "Lcom/tenli/oneview/model/network/Monitor;", "(Lcom/tenli/oneview/model/network/Monitor;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createProfile", "Lcom/tenli/oneview/model/network/Profile;", "(Lcom/tenli/oneview/model/network/Profile;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createProfileGroup", "Lcom/tenli/oneview/model/network/ProfileGroup;", "(Lcom/tenli/oneview/model/network/ProfileGroup;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createProfileInProfileGroup", "Lcom/tenli/oneview/model/network/ProfileInProfileGroup;", "(Lcom/tenli/oneview/model/network/ProfileInProfileGroup;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteFace", "", "faceId", "", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteLicensePlate", "mapId", "deleteMonitor", "id", "deleteProfile", "deleteProfileFromProfileGroup", "deleteProfileGroup", "extractFeatureImage", "Lcom/tenli/oneview/model/network/ModelRunImageResponse;", "f", "", "raw", "body", "Lokhttp3/RequestBody;", "(Ljava/lang/String;Ljava/lang/String;Lokhttp3/RequestBody;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "extractFeatureText", "Lcom/tenli/oneview/model/network/ModelRunTextResponse;", "q", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "faceCheck", "(Lokhttp3/RequestBody;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "faceCrop", "faceImageCrop", "Lcom/tenli/oneview/model/network/FaceImageCrop;", "profileId", "(ILcom/tenli/oneview/model/network/FaceImageCrop;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getCameraMonitorList", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getFaceList", "Lcom/tenli/oneview/model/network/Face;", "getLicensePlateList", "getMonitorList", "getMonitorParam", "type", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getMonitorStateList", "Lcom/tenli/oneview/model/network/MonitorState;", "getMonitorTypeList", "Lcom/tenli/oneview/model/network/MonitorType;", "getProfileGroupList", "getProfileList", "getProfileMap", "restartService", "command", "", "(Ljava/util/Map;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateLicensePlate", "(ILcom/tenli/oneview/model/network/LicensePlate;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateMonitor", "(ILcom/tenli/oneview/model/network/Monitor;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateProfile", "(ILcom/tenli/oneview/model/network/Profile;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateProfileGroup", "(ILcom/tenli/oneview/model/network/ProfileGroup;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateProfileInProfileGroup", "Lcom/tenli/oneview/model/network/UpdateProfileInProfileGroup;", "(Lcom/tenli/oneview/model/network/UpdateProfileInProfileGroup;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public abstract interface AiApi {
    
    @retrofit2.http.GET(value = "api/Camera")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getCameraMonitorList(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.CameraMonitor>>> $completion);
    
    @retrofit2.http.POST(value = "api/Camera")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createCameraMonitor(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.CameraMonitor model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.CameraMonitor>> $completion);
    
    @retrofit2.http.GET(value = "api/Monitor")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getMonitorList(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.Monitor>>> $completion);
    
    @retrofit2.http.GET(value = "api/MonitorType")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getMonitorTypeList(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.MonitorType>>> $completion);
    
    @retrofit2.http.GET(value = "api/MonitorState")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getMonitorStateList(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.MonitorState>>> $completion);
    
    @retrofit2.http.GET(value = "api/MonitorParam/{type}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getMonitorParam(@retrofit2.http.Path(value = "type")
    @org.jetbrains.annotations.NotNull()
    java.lang.String type, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.lang.Object>> $completion);
    
    @retrofit2.http.POST(value = "api/Monitor")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createMonitor(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.Monitor model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.Monitor>> $completion);
    
    @retrofit2.http.PUT(value = "api/Monitor/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateMonitor(@retrofit2.http.Path(value = "id")
    int id, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.Monitor model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.Monitor>> $completion);
    
    @retrofit2.http.DELETE(value = "api/Monitor/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteMonitor(@retrofit2.http.Path(value = "id")
    int id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    @retrofit2.http.GET(value = "api/ProfileGroup/GetList")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getProfileGroupList(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.ProfileGroup>>> $completion);
    
    @retrofit2.http.POST(value = "api/ProfileGroup/Create")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createProfileGroup(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.ProfileGroup model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.ProfileGroup>> $completion);
    
    @retrofit2.http.PUT(value = "api/ProfileGroup/Update/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateProfileGroup(@retrofit2.http.Path(value = "id")
    int id, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.ProfileGroup model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.ProfileGroup>> $completion);
    
    @retrofit2.http.DELETE(value = "api/ProfileGroup/Delete/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteProfileGroup(@retrofit2.http.Path(value = "id")
    int id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    @retrofit2.http.POST(value = "api/ProfileGroup/AddProfile")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createProfileInProfileGroup(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.ProfileInProfileGroup model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.ProfileInProfileGroup>> $completion);
    
    @retrofit2.http.GET(value = "api/ProfileGroup/ProfileMap")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getProfileMap(@retrofit2.http.Query(value = "profileId")
    int profileId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.ProfileInProfileGroup>>> $completion);
    
    @retrofit2.http.HTTP(method = "DELETE", path = "api/ProfileGroup/DeleteProfile", hasBody = false)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteProfileFromProfileGroup(@retrofit2.http.Query(value = "id")
    int mapId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    @retrofit2.http.POST(value = "api/ProfileGroup/UpdateProfile")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateProfileInProfileGroup(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.UpdateProfileInProfileGroup model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.UpdateProfileInProfileGroup>> $completion);
    
    @retrofit2.http.GET(value = "api/Profile")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getProfileList(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.Profile>>> $completion);
    
    @retrofit2.http.POST(value = "api/Profile")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createProfile(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.Profile model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.Profile>> $completion);
    
    @retrofit2.http.PUT(value = "api/Profile/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateProfile(@retrofit2.http.Path(value = "id")
    int id, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.Profile model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.Profile>> $completion);
    
    @retrofit2.http.DELETE(value = "api/Profile/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteProfile(@retrofit2.http.Path(value = "id")
    int id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    @retrofit2.http.GET(value = "api/Face")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getFaceList(@retrofit2.http.Query(value = "profileId")
    int profileId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.Face>>> $completion);
    
    @retrofit2.http.DELETE(value = "api/Face/{faceId}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteFace(@retrofit2.http.Path(value = "faceId")
    int faceId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    /**
     * Raw binary upload (application/octet-stream) — tương đương postFileBinary trên web
     */
    @retrofit2.http.POST(value = "api/FaceCrop")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object faceCrop(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    okhttp3.RequestBody body, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.lang.Object>> $completion);
    
    /**
     * Raw binary upload (application/octet-stream) — tương đương postFileBinary trên web
     */
    @retrofit2.http.POST(value = "api/FaceCheck")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object faceCheck(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    okhttp3.RequestBody body, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.lang.Object>> $completion);
    
    @retrofit2.http.POST(value = "api/FaceImageCrop")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object faceImageCrop(@retrofit2.http.Query(value = "profileId")
    int profileId, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.FaceImageCrop model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.FaceImageCrop>> $completion);
    
    /**
     * Extract text feature — web gửi POST với query params, body rỗng
     */
    @retrofit2.http.POST(value = "api/ModelRun")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object extractFeatureText(@retrofit2.http.Query(value = "q")
    @org.jetbrains.annotations.NotNull()
    java.lang.String q, @retrofit2.http.Query(value = "f")
    @org.jetbrains.annotations.NotNull()
    java.lang.String f, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.ModelRunTextResponse>> $completion);
    
    /**
     * Extract image feature — raw binary upload với query params
     */
    @retrofit2.http.POST(value = "api/ModelRun")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object extractFeatureImage(@retrofit2.http.Query(value = "f")
    @org.jetbrains.annotations.NotNull()
    java.lang.String f, @retrofit2.http.Query(value = "raw")
    @org.jetbrains.annotations.NotNull()
    java.lang.String raw, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    okhttp3.RequestBody body, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.ModelRunImageResponse>> $completion);
    
    @retrofit2.http.GET(value = "api/LicensePlate")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getLicensePlateList(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.LicensePlate>>> $completion);
    
    @retrofit2.http.POST(value = "api/LicensePlate")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createLicensePlate(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.LicensePlate model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.LicensePlate>> $completion);
    
    @retrofit2.http.PUT(value = "api/LicensePlate/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateLicensePlate(@retrofit2.http.Path(value = "id")
    int id, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.LicensePlate model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.LicensePlate>> $completion);
    
    @retrofit2.http.HTTP(method = "DELETE", path = "api/LicensePlate", hasBody = false)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteLicensePlate(@retrofit2.http.Query(value = "id")
    int mapId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    @retrofit2.http.POST(value = "api/SystemControl")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object restartService(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.String, java.lang.String> command, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.lang.Object>> $completion);
}