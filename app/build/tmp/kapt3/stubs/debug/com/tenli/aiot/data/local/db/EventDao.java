package com.tenli.aiot.data.local.db;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0000\n\u0002\u0010\b\n\u0002\b\n\bg\u0018\u00002\u00020\u0001J\u000e\u0010\u0002\u001a\u00020\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u0005\u001a\u00020\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0004J\u0018\u0010\u0006\u001a\u0004\u0018\u00010\u00072\u0006\u0010\b\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ\"\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070\r0\f2\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u000f0\rH\'J\u001c\u0010\u0010\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070\r0\f2\u0006\u0010\u0011\u001a\u00020\u000fH\'J\u001c\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00070\r2\u0006\u0010\u0013\u001a\u00020\u000fH\u00a7@\u00a2\u0006\u0002\u0010\u0014J\u001c\u0010\u0015\u001a\u00020\u00032\f\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00070\rH\u00a7@\u00a2\u0006\u0002\u0010\u0017J\u0016\u0010\u0018\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\n\u00a8\u0006\u0019"}, d2 = {"Lcom/tenli/aiot/data/local/db/EventDao;", "", "clearAll", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteOldEvents", "getEventById", "Lcom/tenli/aiot/model/network/EventItem;", "eventId", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getEventsByDevicesFlow", "Lkotlinx/coroutines/flow/Flow;", "", "deviceIds", "", "getEventsByMonitorFlow", "monitorId", "getRecentEvents", "limit", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertEvents", "events", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "markAsRead", "app_debug"})
@androidx.room.Dao()
public abstract interface EventDao {
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertEvents(@org.jetbrains.annotations.NotNull()
    java.util.List<com.tenli.aiot.model.network.EventItem> events, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM events WHERE id = :eventId LIMIT 1")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getEventById(long eventId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.tenli.aiot.model.network.EventItem> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM events WHERE deviceId IN (:deviceIds) ORDER BY eTimestamp DESC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.tenli.aiot.model.network.EventItem>> getEventsByDevicesFlow(@org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.Integer> deviceIds);
    
    @androidx.room.Query(value = "UPDATE events SET isNew = 0 WHERE id = :eventId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object markAsRead(long eventId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM events WHERE id NOT IN (SELECT id FROM events ORDER BY eTimestamp DESC LIMIT 100)")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteOldEvents(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM events")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object clearAll(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM events WHERE eProcessId = :monitorId ORDER BY eTimestamp DESC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.tenli.aiot.model.network.EventItem>> getEventsByMonitorFlow(int monitorId);
    
    @androidx.room.Query(value = "SELECT * FROM events ORDER BY eTimestamp DESC LIMIT :limit")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getRecentEvents(int limit, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.tenli.aiot.model.network.EventItem>> $completion);
}