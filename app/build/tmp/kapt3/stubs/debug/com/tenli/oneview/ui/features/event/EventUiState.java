package com.tenli.oneview.ui.features.event;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010$\n\u0002\u0018\u0002\n\u0002\b$\b\u0087\b\u0018\u00002\u00020\u0001B\u00c1\u0001\u0012\u000e\b\u0002\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u0007\u001a\u00020\u0006\u0012\b\b\u0002\u0010\b\u001a\u00020\u0006\u0012\b\b\u0002\u0010\t\u001a\u00020\u0006\u0012\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u0004\u0012\b\b\u0002\u0010\u000b\u001a\u00020\f\u0012\b\b\u0002\u0010\r\u001a\u00020\u0006\u0012\u000e\b\u0002\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\f0\u0003\u0012\u000e\b\u0002\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00100\u0003\u0012\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\u0010\u0012\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\u0010\u0012\u000e\b\u0002\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00140\u0003\u0012\u001a\b\u0002\u0010\u0015\u001a\u0014\u0012\u0004\u0012\u00020\u0010\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00170\u00030\u0016\u00a2\u0006\u0002\u0010\u0018J\u000f\u0010(\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\u00c6\u0003J\u000f\u0010)\u001a\b\u0012\u0004\u0012\u00020\u00100\u0003H\u00c6\u0003J\u000b\u0010*\u001a\u0004\u0018\u00010\u0010H\u00c6\u0003J\u000b\u0010+\u001a\u0004\u0018\u00010\u0010H\u00c6\u0003J\u000f\u0010,\u001a\b\u0012\u0004\u0012\u00020\u00140\u0003H\u00c6\u0003J\u001b\u0010-\u001a\u0014\u0012\u0004\u0012\u00020\u0010\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00170\u00030\u0016H\u00c6\u0003J\t\u0010.\u001a\u00020\u0006H\u00c6\u0003J\t\u0010/\u001a\u00020\u0006H\u00c6\u0003J\t\u00100\u001a\u00020\u0006H\u00c6\u0003J\t\u00101\u001a\u00020\u0006H\u00c6\u0003J\u000b\u00102\u001a\u0004\u0018\u00010\u0004H\u00c6\u0003J\t\u00103\u001a\u00020\fH\u00c6\u0003J\t\u00104\u001a\u00020\u0006H\u00c6\u0003J\u000f\u00105\u001a\b\u0012\u0004\u0012\u00020\f0\u0003H\u00c6\u0003J\u00c5\u0001\u00106\u001a\u00020\u00002\u000e\b\u0002\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\u00062\b\b\u0002\u0010\b\u001a\u00020\u00062\b\b\u0002\u0010\t\u001a\u00020\u00062\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u00042\b\b\u0002\u0010\u000b\u001a\u00020\f2\b\b\u0002\u0010\r\u001a\u00020\u00062\u000e\b\u0002\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\f0\u00032\u000e\b\u0002\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00100\u00032\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\u00102\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\u00102\u000e\b\u0002\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00140\u00032\u001a\b\u0002\u0010\u0015\u001a\u0014\u0012\u0004\u0012\u00020\u0010\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00170\u00030\u0016H\u00c6\u0001J\u0013\u00107\u001a\u00020\u00062\b\u00108\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u00109\u001a\u00020\fH\u00d6\u0001J\t\u0010:\u001a\u00020\u0010H\u00d6\u0001R\u0017\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00140\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u001aR#\u0010\u0015\u001a\u0014\u0012\u0004\u0012\u00020\u0010\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00170\u00030\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u001cR\u0011\u0010\u000b\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u001eR\u0017\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010\u001aR\u0013\u0010\u0011\u001a\u0004\u0018\u00010\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010!R\u0011\u0010\t\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\"R\u0011\u0010\r\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\"R\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\"R\u0011\u0010\b\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\"R\u0011\u0010\u0007\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\"R\u0017\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\f0\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010\u001aR\u0017\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00100\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010\u001aR\u0013\u0010\n\u001a\u0004\u0018\u00010\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010&R\u0013\u0010\u0012\u001a\u0004\u0018\u00010\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010!\u00a8\u0006;"}, d2 = {"Lcom/tenli/oneview/ui/features/event/EventUiState;", "", "events", "", "Lcom/tenli/oneview/model/network/EventItem;", "isEventFilter", "", "isRefreshing", "isPagingLoading", "isDataEmpty", "selectedEvent", "currentLevel", "", "isEndReached", "selectedDeviceIds", "selectedETypes", "", "fromTime", "toTime", "availableDevices", "Lcom/tenli/oneview/model/network/DeviceItem;", "availableTypeGroups", "", "Lcom/tenli/oneview/model/network/EventTypeDef;", "(Ljava/util/List;ZZZZLcom/tenli/oneview/model/network/EventItem;IZLjava/util/List;Ljava/util/List;Ljava/lang/String;Ljava/lang/String;Ljava/util/List;Ljava/util/Map;)V", "getAvailableDevices", "()Ljava/util/List;", "getAvailableTypeGroups", "()Ljava/util/Map;", "getCurrentLevel", "()I", "getEvents", "getFromTime", "()Ljava/lang/String;", "()Z", "getSelectedDeviceIds", "getSelectedETypes", "getSelectedEvent", "()Lcom/tenli/oneview/model/network/EventItem;", "getToTime", "component1", "component10", "component11", "component12", "component13", "component14", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "hashCode", "toString", "app_debug"})
@androidx.compose.runtime.Immutable()
public final class EventUiState {
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.tenli.oneview.model.network.EventItem> events = null;
    private final boolean isEventFilter = false;
    private final boolean isRefreshing = false;
    private final boolean isPagingLoading = false;
    private final boolean isDataEmpty = false;
    @org.jetbrains.annotations.Nullable()
    private final com.tenli.oneview.model.network.EventItem selectedEvent = null;
    private final int currentLevel = 0;
    private final boolean isEndReached = false;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<java.lang.Integer> selectedDeviceIds = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<java.lang.String> selectedETypes = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String fromTime = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String toTime = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.tenli.oneview.model.network.DeviceItem> availableDevices = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Map<java.lang.String, java.util.List<com.tenli.oneview.model.network.EventTypeDef>> availableTypeGroups = null;
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.tenli.oneview.model.network.EventItem> component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.String> component10() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component11() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component12() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.tenli.oneview.model.network.DeviceItem> component13() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<java.lang.String, java.util.List<com.tenli.oneview.model.network.EventTypeDef>> component14() {
        return null;
    }
    
    public final boolean component2() {
        return false;
    }
    
    public final boolean component3() {
        return false;
    }
    
    public final boolean component4() {
        return false;
    }
    
    public final boolean component5() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.oneview.model.network.EventItem component6() {
        return null;
    }
    
    public final int component7() {
        return 0;
    }
    
    public final boolean component8() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.Integer> component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.oneview.ui.features.event.EventUiState copy(@org.jetbrains.annotations.NotNull()
    java.util.List<com.tenli.oneview.model.network.EventItem> events, boolean isEventFilter, boolean isRefreshing, boolean isPagingLoading, boolean isDataEmpty, @org.jetbrains.annotations.Nullable()
    com.tenli.oneview.model.network.EventItem selectedEvent, int currentLevel, boolean isEndReached, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.Integer> selectedDeviceIds, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> selectedETypes, @org.jetbrains.annotations.Nullable()
    java.lang.String fromTime, @org.jetbrains.annotations.Nullable()
    java.lang.String toTime, @org.jetbrains.annotations.NotNull()
    java.util.List<com.tenli.oneview.model.network.DeviceItem> availableDevices, @org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.String, ? extends java.util.List<com.tenli.oneview.model.network.EventTypeDef>> availableTypeGroups) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
    
    public EventUiState(@org.jetbrains.annotations.NotNull()
    java.util.List<com.tenli.oneview.model.network.EventItem> events, boolean isEventFilter, boolean isRefreshing, boolean isPagingLoading, boolean isDataEmpty, @org.jetbrains.annotations.Nullable()
    com.tenli.oneview.model.network.EventItem selectedEvent, int currentLevel, boolean isEndReached, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.Integer> selectedDeviceIds, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> selectedETypes, @org.jetbrains.annotations.Nullable()
    java.lang.String fromTime, @org.jetbrains.annotations.Nullable()
    java.lang.String toTime, @org.jetbrains.annotations.NotNull()
    java.util.List<com.tenli.oneview.model.network.DeviceItem> availableDevices, @org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.String, ? extends java.util.List<com.tenli.oneview.model.network.EventTypeDef>> availableTypeGroups) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.tenli.oneview.model.network.EventItem> getEvents() {
        return null;
    }
    
    public final boolean isEventFilter() {
        return false;
    }
    
    public final boolean isRefreshing() {
        return false;
    }
    
    public final boolean isPagingLoading() {
        return false;
    }
    
    public final boolean isDataEmpty() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.oneview.model.network.EventItem getSelectedEvent() {
        return null;
    }
    
    public final int getCurrentLevel() {
        return 0;
    }
    
    public final boolean isEndReached() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.Integer> getSelectedDeviceIds() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.String> getSelectedETypes() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getFromTime() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getToTime() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.tenli.oneview.model.network.DeviceItem> getAvailableDevices() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<java.lang.String, java.util.List<com.tenli.oneview.model.network.EventTypeDef>> getAvailableTypeGroups() {
        return null;
    }
    
    public EventUiState() {
        super();
    }
}