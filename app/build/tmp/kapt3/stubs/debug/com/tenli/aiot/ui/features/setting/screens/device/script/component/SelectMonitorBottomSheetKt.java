package com.tenli.aiot.ui.features.setting.screens.device.script.component;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u00002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u001a&\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u0007H\u0007\u001aT\u0010\b\u001a\u00020\u00012\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00030\n2\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\f0\n2\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00010\u00072\u0012\u0010\u000e\u001a\u000e\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\u00010\u000f2\f\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00010\u0007H\u0007\u00a8\u0006\u0011"}, d2 = {"MonitorGridItem", "", "item", "Lcom/tenli/aiot/model/network/MonitorDisplayItem;", "isSelected", "", "onClick", "Lkotlin/Function0;", "SelectMonitorBottomSheet", "monitors", "", "selectedIds", "", "onDismiss", "onToggleMonitor", "Lkotlin/Function1;", "onConfirm", "app_debug"})
public final class SelectMonitorBottomSheetKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void SelectMonitorBottomSheet(@org.jetbrains.annotations.NotNull()
    java.util.List<com.tenli.aiot.model.network.MonitorDisplayItem> monitors, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.Integer> selectedIds, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onToggleMonitor, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onConfirm) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void MonitorGridItem(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.model.network.MonitorDisplayItem item, boolean isSelected, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
}