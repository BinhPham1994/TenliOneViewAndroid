package com.tenli.aiot.ui.features.setting.screens.device.info;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000D\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0006\n\u0002\b\u0002\u001a&\u0010\u0000\u001a\u00020\u00012\u001c\u0010\u0002\u001a\u0018\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00010\u0003\u00a2\u0006\u0002\b\u0005\u00a2\u0006\u0002\b\u0006H\u0007\u001a\"\u0010\u0007\u001a\u00020\u00012\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\t2\b\b\u0002\u0010\u000b\u001a\u00020\fH\u0007\u001a\u0010\u0010\r\u001a\u00020\u00012\u0006\u0010\u000e\u001a\u00020\u000fH\u0007\u001a\u0010\u0010\u0010\u001a\u00020\u00012\u0006\u0010\u0011\u001a\u00020\tH\u0007\u001a\u0012\u0010\u0012\u001a\u00020\u00012\b\u0010\u0013\u001a\u0004\u0018\u00010\u0014H\u0007\u001a\u0015\u0010\u0015\u001a\u00020\t2\b\u0010\u0016\u001a\u0004\u0018\u00010\u0017\u00a2\u0006\u0002\u0010\u0018\u00a8\u0006\u0019"}, d2 = {"DeviceInfoGroup", "", "content", "Lkotlin/Function1;", "Landroidx/compose/foundation/layout/ColumnScope;", "Landroidx/compose/runtime/Composable;", "Lkotlin/ExtensionFunctionType;", "DeviceInfoRow", "label", "", "value", "isCopyable", "", "DeviceInfoScreen", "viewModel", "Lcom/tenli/aiot/ui/features/setting/core/SettingViewModel;", "DeviceInfoSectionTitle", "title", "StorageStatusView", "state", "Lcom/tenli/aiot/model/network/BoxSystemState;", "formatBoxTime", "timestamp", "", "(Ljava/lang/Double;)Ljava/lang/String;", "app_debug"})
public final class DeviceInfoScreenKt {
    
    @androidx.compose.runtime.Composable()
    public static final void DeviceInfoScreen(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.SettingViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void DeviceInfoSectionTitle(@org.jetbrains.annotations.NotNull()
    java.lang.String title) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void DeviceInfoGroup(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super androidx.compose.foundation.layout.ColumnScope, kotlin.Unit> content) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void DeviceInfoRow(@org.jetbrains.annotations.NotNull()
    java.lang.String label, @org.jetbrains.annotations.NotNull()
    java.lang.String value, boolean isCopyable) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String formatBoxTime(@org.jetbrains.annotations.Nullable()
    java.lang.Double timestamp) {
        return null;
    }
    
    @android.annotation.SuppressLint(value = {"DefaultLocale"})
    @androidx.compose.runtime.Composable()
    public static final void StorageStatusView(@org.jetbrains.annotations.Nullable()
    com.tenli.aiot.model.network.BoxSystemState state) {
    }
}