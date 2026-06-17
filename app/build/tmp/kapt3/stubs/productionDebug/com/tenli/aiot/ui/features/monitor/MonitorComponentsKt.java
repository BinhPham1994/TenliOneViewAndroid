package com.tenli.aiot.ui.features.monitor;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u00004\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\u001a\u001e\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0007\u001a6\u0010\u0006\u001a\u00020\u00012\u0006\u0010\u0007\u001a\u00020\b2\b\u0010\t\u001a\u0004\u0018\u00010\n2\u0006\u0010\u000b\u001a\u00020\f2\u0012\u0010\r\u001a\u000e\u0012\u0004\u0012\u00020\u000f\u0012\u0004\u0012\u00020\u00010\u000eH\u0007\u001a\u0010\u0010\u0010\u001a\u00020\u00012\u0006\u0010\u0011\u001a\u00020\u0003H\u0007\u001a\u001e\u0010\u0012\u001a\u00020\u00012\u0006\u0010\t\u001a\u00020\n2\f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0007\u00a8\u0006\u0014"}, d2 = {"MonitorDetailTopBar", "", "title", "", "onBack", "Lkotlin/Function0;", "MonitorDetailView", "listState", "Landroidx/compose/foundation/lazy/LazyListState;", "item", "Lcom/tenli/aiot/model/network/MonitorDisplayItem;", "viewModel", "Lcom/tenli/aiot/ui/features/monitor/MonitorViewModel;", "onEventClick", "Lkotlin/Function1;", "Lcom/tenli/aiot/model/network/EventItem;", "MonitorGroupHeader", "type", "MonitorImageCard", "onClick", "app_productionDebug"})
public final class MonitorComponentsKt {
    
    @androidx.compose.runtime.Composable()
    public static final void MonitorGroupHeader(@org.jetbrains.annotations.NotNull()
    java.lang.String type) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void MonitorImageCard(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.model.network.MonitorDisplayItem item, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void MonitorDetailTopBar(@org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onBack) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void MonitorDetailView(@org.jetbrains.annotations.NotNull()
    androidx.compose.foundation.lazy.LazyListState listState, @org.jetbrains.annotations.Nullable()
    com.tenli.aiot.model.network.MonitorDisplayItem item, @org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.monitor.MonitorViewModel viewModel, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.tenli.aiot.model.network.EventItem, kotlin.Unit> onEventClick) {
    }
}