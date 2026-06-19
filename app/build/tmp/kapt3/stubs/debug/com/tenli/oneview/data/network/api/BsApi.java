package com.tenli.oneview.data.network.api;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u00c0\u0001\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u000b\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J\u001e\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0004H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u001e\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\b0\u00032\b\b\u0001\u0010\u0005\u001a\u00020\bH\u00a7@\u00a2\u0006\u0002\u0010\tJ\u001e\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u000b0\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u000bH\u00a7@\u00a2\u0006\u0002\u0010\fJ\u001e\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000e0\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u000eH\u00a7@\u00a2\u0006\u0002\u0010\u000fJ(\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00110\u00032\b\b\u0001\u0010\u0012\u001a\u00020\u00132\b\b\u0001\u0010\u0005\u001a\u00020\u0011H\u00a7@\u00a2\u0006\u0002\u0010\u0014J\u001e\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00160\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0016H\u00a7@\u00a2\u0006\u0002\u0010\u0017J\u001e\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00190\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0019H\u00a7@\u00a2\u0006\u0002\u0010\u001aJ\u001e\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u001c0\u00032\b\b\u0001\u0010\u001d\u001a\u00020\u0013H\u00a7@\u00a2\u0006\u0002\u0010\u001eJ\u001e\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u001c0\u00032\b\b\u0001\u0010\u001d\u001a\u00020\u0013H\u00a7@\u00a2\u0006\u0002\u0010\u001eJ\u001e\u0010 \u001a\b\u0012\u0004\u0012\u00020\u001c0\u00032\b\b\u0001\u0010\u001d\u001a\u00020\u0013H\u00a7@\u00a2\u0006\u0002\u0010\u001eJ\u001e\u0010!\u001a\b\u0012\u0004\u0012\u00020\u001c0\u00032\b\b\u0001\u0010\u001d\u001a\u00020\u0013H\u00a7@\u00a2\u0006\u0002\u0010\u001eJ(\u0010\"\u001a\b\u0012\u0004\u0012\u00020\u001c0\u00032\b\b\u0001\u0010\u0012\u001a\u00020\u00132\b\b\u0001\u0010#\u001a\u00020\u0013H\u00a7@\u00a2\u0006\u0002\u0010$J\u001e\u0010%\u001a\b\u0012\u0004\u0012\u00020\u001c0\u00032\b\b\u0001\u0010\u001d\u001a\u00020\u0013H\u00a7@\u00a2\u0006\u0002\u0010\u001eJ\u001e\u0010&\u001a\b\u0012\u0004\u0012\u00020\u001c0\u00032\b\b\u0001\u0010\u001d\u001a\u00020\u0013H\u00a7@\u00a2\u0006\u0002\u0010\u001eJ\u001a\u0010\'\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00040(0\u0003H\u00a7@\u00a2\u0006\u0002\u0010)J\u001a\u0010*\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020+0(0\u0003H\u00a7@\u00a2\u0006\u0002\u0010)J\u001a\u0010,\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0(0\u0003H\u00a7@\u00a2\u0006\u0002\u0010)J$\u0010-\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00110(0\u00032\b\b\u0001\u0010\u0012\u001a\u00020\u0013H\u00a7@\u00a2\u0006\u0002\u0010\u001eJ$\u0010.\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00160(0\u00032\b\b\u0003\u0010/\u001a\u000200H\u00a7@\u00a2\u0006\u0002\u00101J$\u00102\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0(0\u00032\b\b\u0003\u0010/\u001a\u000200H\u00a7@\u00a2\u0006\u0002\u00101J\u001a\u00103\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00190(0\u0003H\u00a7@\u00a2\u0006\u0002\u0010)J:\u00104\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u0002050(0\u00032\b\b\u0001\u00106\u001a\u0002072\b\b\u0001\u00108\u001a\u0002072\n\b\u0003\u00109\u001a\u0004\u0018\u000100H\u00a7@\u00a2\u0006\u0002\u0010:J.\u0010;\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020<0(0\u00032\b\b\u0001\u00106\u001a\u0002072\b\b\u0001\u00108\u001a\u000207H\u00a7@\u00a2\u0006\u0002\u0010=J.\u0010>\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020?0(0\u00032\b\b\u0001\u00106\u001a\u0002072\b\b\u0001\u00108\u001a\u000207H\u00a7@\u00a2\u0006\u0002\u0010=J.\u0010@\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020A0(0\u00032\b\b\u0001\u00106\u001a\u0002072\b\b\u0001\u00108\u001a\u000207H\u00a7@\u00a2\u0006\u0002\u0010=JT\u0010B\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020C0(0\u00032\b\b\u0003\u00109\u001a\u0002002\n\b\u0003\u00106\u001a\u0004\u0018\u0001072\n\b\u0003\u00108\u001a\u0004\u0018\u0001072\n\b\u0003\u0010D\u001a\u0004\u0018\u0001002\n\b\u0003\u0010E\u001a\u0004\u0018\u000100H\u00a7@\u00a2\u0006\u0002\u0010FJF\u0010G\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020H0(0\u00032\b\b\u0001\u00106\u001a\u0002072\b\b\u0001\u00108\u001a\u0002072\n\b\u0003\u0010D\u001a\u0004\u0018\u0001002\n\b\u0003\u0010E\u001a\u0004\u0018\u000100H\u00a7@\u00a2\u0006\u0002\u0010IJ.\u0010J\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020K0(0\u00032\b\b\u0001\u00106\u001a\u0002072\b\b\u0001\u00108\u001a\u000207H\u00a7@\u00a2\u0006\u0002\u0010=JF\u0010L\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020M0(0\u00032\b\b\u0001\u00106\u001a\u0002072\b\b\u0001\u00108\u001a\u0002072\n\b\u0003\u0010D\u001a\u0004\u0018\u0001002\n\b\u0003\u0010E\u001a\u0004\u0018\u000100H\u00a7@\u00a2\u0006\u0002\u0010IJ(\u0010N\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u001d\u001a\u00020\u00132\b\b\u0001\u0010\u0005\u001a\u00020\u0004H\u00a7@\u00a2\u0006\u0002\u0010OJ(\u0010P\u001a\b\u0012\u0004\u0012\u00020\u000b0\u00032\b\b\u0001\u0010\u001d\u001a\u00020\u00132\b\b\u0001\u0010\u0005\u001a\u00020\u000bH\u00a7@\u00a2\u0006\u0002\u0010QJ(\u0010R\u001a\b\u0012\u0004\u0012\u00020\u000e0\u00032\b\b\u0001\u0010\u001d\u001a\u00020\u00132\b\b\u0001\u0010\u0005\u001a\u00020\u000eH\u00a7@\u00a2\u0006\u0002\u0010SJ(\u0010T\u001a\b\u0012\u0004\u0012\u00020\u00160\u00032\b\b\u0001\u0010\u001d\u001a\u00020\u00132\b\b\u0001\u0010\u0005\u001a\u00020\u0016H\u00a7@\u00a2\u0006\u0002\u0010UJ(\u0010V\u001a\b\u0012\u0004\u0012\u00020\u00190\u00032\b\b\u0001\u0010\u001d\u001a\u00020\u00132\b\b\u0001\u0010\u0005\u001a\u00020\u0019H\u00a7@\u00a2\u0006\u0002\u0010WJ(\u0010X\u001a\b\u0012\u0004\u0012\u00020Y0\u00032\b\b\u0001\u0010Z\u001a\u00020[2\b\b\u0001\u0010\\\u001a\u00020]H\u00a7@\u00a2\u0006\u0002\u0010^\u00a8\u0006_"}, d2 = {"Lcom/tenli/oneview/data/network/api/BsApi;", "", "createContact", "Lretrofit2/Response;", "Lcom/tenli/oneview/model/network/ContractModel;", "model", "(Lcom/tenli/oneview/model/network/ContractModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createFoodTraceability", "Lcom/tenli/oneview/model/network/FoodTraceability;", "(Lcom/tenli/oneview/model/network/FoodTraceability;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createPlate", "Lcom/tenli/oneview/model/network/PlateModel;", "(Lcom/tenli/oneview/model/network/PlateModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createProfile", "Lcom/tenli/oneview/model/network/BsProfileModel;", "(Lcom/tenli/oneview/model/network/BsProfileModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createProfileFace", "Lcom/tenli/oneview/model/network/ImageProfileModel;", "profileId", "", "(ILcom/tenli/oneview/model/network/ImageProfileModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createProfileGroup", "Lcom/tenli/oneview/model/network/BsProfileGroupModel;", "(Lcom/tenli/oneview/model/network/BsProfileGroupModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createTask", "Lcom/tenli/oneview/model/network/TaskModel;", "(Lcom/tenli/oneview/model/network/TaskModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteContact", "", "id", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteFoodItem", "deletePlate", "deleteProfile", "deleteProfileFace", "faceId", "(IILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteProfileGroup", "deleteTask", "getContactList", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getFoodList", "Lcom/tenli/oneview/model/network/FoodModel;", "getPlateList", "getProfileFaces", "getProfileGroupList", "mode", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getProfileList", "getTaskList", "getUniformCountByLabel", "Lcom/tenli/oneview/model/network/UniformCountByLabelModel;", "fromTime", "", "toTime", "serviceId", "(JJLjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getVehicleCountByPrefix", "Lcom/tenli/oneview/model/network/VehicleCountByPrefixModel;", "(JJLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getVehicleQuickCount", "Lcom/tenli/oneview/model/network/VehicleQuickCountModel;", "getVehicleStatsOverTime", "Lcom/tenli/oneview/model/network/VehicleStatisticalOverTimeModel;", "getVmsCountOverview", "Lcom/tenli/oneview/model/network/VmsCountOverviewModel;", "eventType", "dataEventType", "(Ljava/lang/String;Ljava/lang/Long;Ljava/lang/Long;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getVmsEventCountByCamera", "Lcom/tenli/oneview/model/network/VmsEventCountByCameraModel;", "(JJLjava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getVmsEventCountByType", "Lcom/tenli/oneview/model/network/VmsEventCountByTypeModel;", "getVmsEventStatsOverTime", "Lcom/tenli/oneview/model/network/VmsEventStatisticalOverTimeModel;", "updateContact", "(ILcom/tenli/oneview/model/network/ContractModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updatePlate", "(ILcom/tenli/oneview/model/network/PlateModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateProfile", "(ILcom/tenli/oneview/model/network/BsProfileModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateProfileGroup", "(ILcom/tenli/oneview/model/network/BsProfileGroupModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateTask", "(ILcom/tenli/oneview/model/network/TaskModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "uploadAvatar", "Lcom/tenli/oneview/model/network/FileUploadResponse;", "file", "Lokhttp3/MultipartBody$Part;", "collection", "Lokhttp3/RequestBody;", "(Lokhttp3/MultipartBody$Part;Lokhttp3/RequestBody;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public abstract interface BsApi {
    
    @retrofit2.http.GET(value = "ts/api/v1/plates")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getPlateList(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.PlateModel>>> $completion);
    
    @retrofit2.http.POST(value = "ts/api/v1/plates/create")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createPlate(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.PlateModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.PlateModel>> $completion);
    
    @retrofit2.http.PUT(value = "ts/api/v1/plates/update/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updatePlate(@retrofit2.http.Path(value = "id")
    int id, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.PlateModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.PlateModel>> $completion);
    
    @retrofit2.http.DELETE(value = "ts/api/v1/plates/delete/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deletePlate(@retrofit2.http.Path(value = "id")
    int id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    @retrofit2.http.GET(value = "ts/api/v1/contacts")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getContactList(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.ContractModel>>> $completion);
    
    @retrofit2.http.POST(value = "ts/api/v1/contacts")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createContact(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.ContractModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.ContractModel>> $completion);
    
    @retrofit2.http.PUT(value = "ts/api/v1/contacts/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateContact(@retrofit2.http.Path(value = "id")
    int id, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.ContractModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.ContractModel>> $completion);
    
    @retrofit2.http.DELETE(value = "ts/api/v1/contacts/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteContact(@retrofit2.http.Path(value = "id")
    int id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    @retrofit2.http.GET(value = "ts/api/v1/profile-groups")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getProfileGroupList(@retrofit2.http.Query(value = "mode")
    @org.jetbrains.annotations.NotNull()
    java.lang.String mode, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.BsProfileGroupModel>>> $completion);
    
    @retrofit2.http.POST(value = "ts/api/v1/profile-groups")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createProfileGroup(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.BsProfileGroupModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.BsProfileGroupModel>> $completion);
    
    @retrofit2.http.PUT(value = "ts/api/v1/profile-groups/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateProfileGroup(@retrofit2.http.Path(value = "id")
    int id, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.BsProfileGroupModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.BsProfileGroupModel>> $completion);
    
    @retrofit2.http.DELETE(value = "ts/api/v1/profile-groups/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteProfileGroup(@retrofit2.http.Path(value = "id")
    int id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    @retrofit2.http.GET(value = "ts/api/v1/profiles")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getProfileList(@retrofit2.http.Query(value = "mode")
    @org.jetbrains.annotations.NotNull()
    java.lang.String mode, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.BsProfileModel>>> $completion);
    
    @retrofit2.http.POST(value = "ts/api/v1/profiles")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createProfile(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.BsProfileModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.BsProfileModel>> $completion);
    
    @retrofit2.http.PUT(value = "ts/api/v1/profiles/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateProfile(@retrofit2.http.Path(value = "id")
    int id, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.BsProfileModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.BsProfileModel>> $completion);
    
    @retrofit2.http.DELETE(value = "ts/api/v1/profiles/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteProfile(@retrofit2.http.Path(value = "id")
    int id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    @retrofit2.http.GET(value = "ts/api/v1/profiles/{profileId}/faces")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getProfileFaces(@retrofit2.http.Path(value = "profileId")
    int profileId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.ImageProfileModel>>> $completion);
    
    @retrofit2.http.POST(value = "ts/api/v1/profiles/{profileId}/faces")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createProfileFace(@retrofit2.http.Path(value = "profileId")
    int profileId, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.ImageProfileModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.ImageProfileModel>> $completion);
    
    @retrofit2.http.DELETE(value = "ts/api/v1/profiles/{profileId}/faces/{faceId}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteProfileFace(@retrofit2.http.Path(value = "profileId")
    int profileId, @retrofit2.http.Path(value = "faceId")
    int faceId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    @retrofit2.http.GET(value = "ts/api/v1/tasks")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getTaskList(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.TaskModel>>> $completion);
    
    @retrofit2.http.POST(value = "ts/api/v1/tasks")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createTask(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.TaskModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.TaskModel>> $completion);
    
    @retrofit2.http.PUT(value = "ts/api/v1/tasks/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateTask(@retrofit2.http.Path(value = "id")
    int id, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.TaskModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.TaskModel>> $completion);
    
    @retrofit2.http.DELETE(value = "ts/api/v1/tasks/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteTask(@retrofit2.http.Path(value = "id")
    int id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    @retrofit2.http.GET(value = "ts/api/v1/e/vehical/summary-by-time")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getVehicleStatsOverTime(@retrofit2.http.Query(value = "fromTime")
    long fromTime, @retrofit2.http.Query(value = "toTime")
    long toTime, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.VehicleStatisticalOverTimeModel>>> $completion);
    
    @retrofit2.http.GET(value = "ts/api/v1/e/vehical/count-by-label")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getVehicleQuickCount(@retrofit2.http.Query(value = "fromTime")
    long fromTime, @retrofit2.http.Query(value = "toTime")
    long toTime, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.VehicleQuickCountModel>>> $completion);
    
    @retrofit2.http.GET(value = "ts/api/v1/e/vehical/count-by-prefix")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getVehicleCountByPrefix(@retrofit2.http.Query(value = "fromTime")
    long fromTime, @retrofit2.http.Query(value = "toTime")
    long toTime, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.VehicleCountByPrefixModel>>> $completion);
    
    @retrofit2.http.GET(value = "ts/api/v1/e/uniform/count-by-label")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getUniformCountByLabel(@retrofit2.http.Query(value = "fromTime")
    long fromTime, @retrofit2.http.Query(value = "toTime")
    long toTime, @retrofit2.http.Query(value = "serviceId")
    @org.jetbrains.annotations.Nullable()
    java.lang.String serviceId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.UniformCountByLabelModel>>> $completion);
    
    @retrofit2.http.GET(value = "ts/api/v1/e/vms-event/summary-by-time")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getVmsEventStatsOverTime(@retrofit2.http.Query(value = "fromTime")
    long fromTime, @retrofit2.http.Query(value = "toTime")
    long toTime, @retrofit2.http.Query(value = "eventType")
    @org.jetbrains.annotations.Nullable()
    java.lang.String eventType, @retrofit2.http.Query(value = "dataEventType")
    @org.jetbrains.annotations.Nullable()
    java.lang.String dataEventType, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.VmsEventStatisticalOverTimeModel>>> $completion);
    
    @retrofit2.http.GET(value = "ts/api/v1/e/vms-core/count-overview")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getVmsCountOverview(@retrofit2.http.Query(value = "serviceId")
    @org.jetbrains.annotations.NotNull()
    java.lang.String serviceId, @retrofit2.http.Query(value = "fromTime")
    @org.jetbrains.annotations.Nullable()
    java.lang.Long fromTime, @retrofit2.http.Query(value = "toTime")
    @org.jetbrains.annotations.Nullable()
    java.lang.Long toTime, @retrofit2.http.Query(value = "eventType")
    @org.jetbrains.annotations.Nullable()
    java.lang.String eventType, @retrofit2.http.Query(value = "dataEventType")
    @org.jetbrains.annotations.Nullable()
    java.lang.String dataEventType, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.VmsCountOverviewModel>>> $completion);
    
    @retrofit2.http.GET(value = "ts/api/v1/e/vms-event/count-by-type")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getVmsEventCountByType(@retrofit2.http.Query(value = "fromTime")
    long fromTime, @retrofit2.http.Query(value = "toTime")
    long toTime, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.VmsEventCountByTypeModel>>> $completion);
    
    @retrofit2.http.GET(value = "ts/api/v1/e/vms-event/count-by-camera")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getVmsEventCountByCamera(@retrofit2.http.Query(value = "fromTime")
    long fromTime, @retrofit2.http.Query(value = "toTime")
    long toTime, @retrofit2.http.Query(value = "eventType")
    @org.jetbrains.annotations.Nullable()
    java.lang.String eventType, @retrofit2.http.Query(value = "dataEventType")
    @org.jetbrains.annotations.Nullable()
    java.lang.String dataEventType, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.VmsEventCountByCameraModel>>> $completion);
    
    @retrofit2.http.Multipart()
    @retrofit2.http.POST(value = "ts/api/v1/files/upload")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object uploadAvatar(@retrofit2.http.Part()
    @org.jetbrains.annotations.NotNull()
    okhttp3.MultipartBody.Part file, @retrofit2.http.Part(value = "collection")
    @org.jetbrains.annotations.NotNull()
    okhttp3.RequestBody collection, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.FileUploadResponse>> $completion);
    
    @retrofit2.http.GET(value = "qltp/api/Data")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getFoodList(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.FoodModel>>> $completion);
    
    @retrofit2.http.POST(value = "qltp/api/Data")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createFoodTraceability(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.FoodTraceability model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.FoodTraceability>> $completion);
    
    @retrofit2.http.DELETE(value = "qltp/api/Data/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteFoodItem(@retrofit2.http.Path(value = "id")
    int id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
    }
}