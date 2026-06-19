package com.tenli.oneview.data.network.api;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010\t\n\u0002\b\u000e\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J\u001e\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0004H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u001e\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\b0\u00032\b\b\u0001\u0010\u0005\u001a\u00020\bH\u00a7@\u00a2\u0006\u0002\u0010\tJ\u001e\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u000b0\u00032\b\b\u0001\u0010\f\u001a\u00020\rH\u00a7@\u00a2\u0006\u0002\u0010\u000eJ\u001e\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u000b0\u00032\b\b\u0001\u0010\f\u001a\u00020\rH\u00a7@\u00a2\u0006\u0002\u0010\u000eJ\u001a\u0010\u0010\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00040\u00110\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0012J\u0082\u0001\u0010\u0013\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00140\u00110\u00032\b\b\u0003\u0010\u0015\u001a\u00020\u00162\b\b\u0003\u0010\u0017\u001a\u00020\u00162\n\b\u0003\u0010\u0018\u001a\u0004\u0018\u00010\r2\n\b\u0003\u0010\u0019\u001a\u0004\u0018\u00010\r2\n\b\u0003\u0010\u001a\u001a\u0004\u0018\u00010\r2\n\b\u0003\u0010\u001b\u001a\u0004\u0018\u00010\u00162\n\b\u0003\u0010\u001c\u001a\u0004\u0018\u00010\u001d2\n\b\u0003\u0010\u001e\u001a\u0004\u0018\u00010\u001d2\n\b\u0003\u0010\u001f\u001a\u0004\u0018\u00010\u0016H\u00a7@\u00a2\u0006\u0002\u0010 J\u001a\u0010!\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00110\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0012Jd\u0010\"\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00140\u00110\u00032\n\b\u0003\u0010\u001c\u001a\u0004\u0018\u00010\u001d2\n\b\u0003\u0010\u001e\u001a\u0004\u0018\u00010\u001d2\n\b\u0003\u0010\u001a\u001a\u0004\u0018\u00010\r2\n\b\u0003\u0010\u0019\u001a\u0004\u0018\u00010\r2\b\b\u0003\u0010#\u001a\u00020\u00162\u000e\b\u0001\u0010$\u001a\b\u0012\u0004\u0012\u00020\u00010\u0011H\u00a7@\u00a2\u0006\u0002\u0010%J(\u0010&\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\f\u001a\u00020\r2\b\b\u0001\u0010\u0005\u001a\u00020\u0004H\u00a7@\u00a2\u0006\u0002\u0010\'J(\u0010(\u001a\b\u0012\u0004\u0012\u00020\b0\u00032\b\b\u0001\u0010\f\u001a\u00020\r2\b\b\u0001\u0010\u0005\u001a\u00020\bH\u00a7@\u00a2\u0006\u0002\u0010)J(\u0010*\u001a\b\u0012\u0004\u0012\u00020\u000b0\u00032\b\b\u0001\u0010\f\u001a\u00020\r2\b\b\u0001\u0010+\u001a\u00020,H\u00a7@\u00a2\u0006\u0002\u0010-\u00a8\u0006."}, d2 = {"Lcom/tenli/oneview/data/network/api/EventApi;", "", "createAIService", "Lretrofit2/Response;", "Lcom/tenli/oneview/model/network/AIServiceModel;", "model", "(Lcom/tenli/oneview/model/network/AIServiceModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createRule", "Lcom/tenli/oneview/model/network/RuleModel;", "(Lcom/tenli/oneview/model/network/RuleModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteAIService", "", "id", "", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteRule", "getAIServiceList", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getDataList", "Lcom/tenli/oneview/model/network/EventData;", "order", "", "isConfirmed", "lastId", "count", "serviceId", "type", "from", "", "to", "cameraUUID", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/String;Ljava/lang/Long;Ljava/lang/Long;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getRuleList", "searchAIData", "fast", "searchItems", "(Ljava/lang/Long;Ljava/lang/Long;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/String;Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateAIService", "(ILcom/tenli/oneview/model/network/AIServiceModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateRule", "(ILcom/tenli/oneview/model/network/RuleModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "verifyEvent", "confirm", "Lcom/tenli/oneview/model/network/Confirm;", "(ILcom/tenli/oneview/model/network/Confirm;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public abstract interface EventApi {
    
    @retrofit2.http.GET(value = "Data/api/Service")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getAIServiceList(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.AIServiceModel>>> $completion);
    
    @retrofit2.http.POST(value = "Data/api/Service")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createAIService(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.AIServiceModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.AIServiceModel>> $completion);
    
    @retrofit2.http.PUT(value = "Data/api/Service/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateAIService(@retrofit2.http.Path(value = "id")
    int id, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.AIServiceModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.AIServiceModel>> $completion);
    
    @retrofit2.http.DELETE(value = "Data/api/Service/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteAIService(@retrofit2.http.Path(value = "id")
    int id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    @retrofit2.http.GET(value = "Data/api/Data")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getDataList(@retrofit2.http.Query(value = "order")
    @org.jetbrains.annotations.NotNull()
    java.lang.String order, @retrofit2.http.Query(value = "isConfirmed")
    @org.jetbrains.annotations.NotNull()
    java.lang.String isConfirmed, @retrofit2.http.Query(value = "lastId")
    @org.jetbrains.annotations.Nullable()
    java.lang.Integer lastId, @retrofit2.http.Query(value = "count")
    @org.jetbrains.annotations.Nullable()
    java.lang.Integer count, @retrofit2.http.Query(value = "serviceId")
    @org.jetbrains.annotations.Nullable()
    java.lang.Integer serviceId, @retrofit2.http.Query(value = "type")
    @org.jetbrains.annotations.Nullable()
    java.lang.String type, @retrofit2.http.Query(value = "from")
    @org.jetbrains.annotations.Nullable()
    java.lang.Long from, @retrofit2.http.Query(value = "to")
    @org.jetbrains.annotations.Nullable()
    java.lang.Long to, @retrofit2.http.Query(value = "cameraUUID")
    @org.jetbrains.annotations.Nullable()
    java.lang.String cameraUUID, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.EventData>>> $completion);
    
    @retrofit2.http.POST(value = "Data/api/DataSearch")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object searchAIData(@retrofit2.http.Query(value = "from")
    @org.jetbrains.annotations.Nullable()
    java.lang.Long from, @retrofit2.http.Query(value = "to")
    @org.jetbrains.annotations.Nullable()
    java.lang.Long to, @retrofit2.http.Query(value = "serviceId")
    @org.jetbrains.annotations.Nullable()
    java.lang.Integer serviceId, @retrofit2.http.Query(value = "count")
    @org.jetbrains.annotations.Nullable()
    java.lang.Integer count, @retrofit2.http.Query(value = "fast")
    @org.jetbrains.annotations.NotNull()
    java.lang.String fast, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    java.util.List<? extends java.lang.Object> searchItems, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.EventData>>> $completion);
    
    @retrofit2.http.POST(value = "Data/api/Data/confirm/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object verifyEvent(@retrofit2.http.Path(value = "id")
    int id, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.Confirm confirm, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    @retrofit2.http.GET(value = "Data/api/Rule")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getRuleList(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.tenli.oneview.model.network.RuleModel>>> $completion);
    
    @retrofit2.http.POST(value = "Data/api/Rule")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createRule(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.RuleModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.RuleModel>> $completion);
    
    @retrofit2.http.PUT(value = "Data/api/Rule/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateRule(@retrofit2.http.Path(value = "id")
    int id, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.RuleModel model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.tenli.oneview.model.network.RuleModel>> $completion);
    
    @retrofit2.http.DELETE(value = "Data/api/Rule/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteRule(@retrofit2.http.Path(value = "id")
    int id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
    }
}