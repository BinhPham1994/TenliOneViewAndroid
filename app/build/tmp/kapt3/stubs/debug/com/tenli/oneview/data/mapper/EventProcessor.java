package com.tenli.oneview.data.mapper;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J*\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00042\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u00072\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\n0\u0007J\u0010\u0010\u000b\u001a\u00020\f2\b\u0010\r\u001a\u0004\u0018\u00010\fJ\u0010\u0010\u000e\u001a\u00020\f2\b\u0010\r\u001a\u0004\u0018\u00010\fJ\u0016\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\f2\u0006\u0010\u0012\u001a\u00020\f\u00a8\u0006\u0013"}, d2 = {"Lcom/tenli/oneview/data/mapper/EventProcessor;", "", "()V", "enrich", "Lcom/tenli/oneview/model/network/EventItem;", "event", "deviceList", "", "Lcom/tenli/oneview/model/network/DeviceItem;", "typeDefs", "Lcom/tenli/oneview/model/network/EventTypeDef;", "getMonitorGroupTitle", "", "type", "getMonitorTitle", "getUIConfig", "Lcom/tenli/oneview/data/mapper/EventUIConfig;", "eType", "eTypeGroup", "app_debug"})
public final class EventProcessor {
    @org.jetbrains.annotations.NotNull()
    public static final com.tenli.oneview.data.mapper.EventProcessor INSTANCE = null;
    
    private EventProcessor() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.oneview.model.network.EventItem enrich(@org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.EventItem event, @org.jetbrains.annotations.NotNull()
    java.util.List<com.tenli.oneview.model.network.DeviceItem> deviceList, @org.jetbrains.annotations.NotNull()
    java.util.List<com.tenli.oneview.model.network.EventTypeDef> typeDefs) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getMonitorGroupTitle(@org.jetbrains.annotations.Nullable()
    java.lang.String type) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getMonitorTitle(@org.jetbrains.annotations.Nullable()
    java.lang.String type) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.oneview.data.mapper.EventUIConfig getUIConfig(@org.jetbrains.annotations.NotNull()
    java.lang.String eType, @org.jetbrains.annotations.NotNull()
    java.lang.String eTypeGroup) {
        return null;
    }
}