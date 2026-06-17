package com.tenli.aiot.ui.features.setting.screens.notification;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000Z\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u000b\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u000f\u001a\u00020\u0010J\u000e\u0010\u0011\u001a\u00020\u00102\u0006\u0010\u0012\u001a\u00020\bJ\u0006\u0010\u0013\u001a\u00020\u0010J\u0010\u0010\u0014\u001a\u00020\b2\b\u0010\u0015\u001a\u0004\u0018\u00010\bJ \u0010\u0016\u001a\u0014\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u0018\u0012\u0004\u0012\u00020\u00190\u00172\u0006\u0010\u001a\u001a\u00020\bJ\u000e\u0010\u001b\u001a\u00020\u00182\u0006\u0010\u001a\u001a\u00020\bJ(\u0010\u001c\u001a\u00020\u00102\u0006\u0010\u0012\u001a\u00020\b2\u0018\u0010\u001d\u001a\u0014\u0012\u0004\u0012\u00020\u001f\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u00100\u001eJ\u0018\u0010 \u001a\u00020\u00102\u0006\u0010!\u001a\u00020\b2\b\u0010\"\u001a\u0004\u0018\u00010\bJ\u000e\u0010#\u001a\u00020\u00102\u0006\u0010$\u001a\u00020\bJ\u0018\u0010%\u001a\u00020\u00102\b\u0010\u0012\u001a\u0004\u0018\u00010\b2\u0006\u0010&\u001a\u00020\'J\u000e\u0010(\u001a\u00020\u00102\u0006\u0010&\u001a\u00020\'R\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00050\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000e\u00a8\u0006)"}, d2 = {"Lcom/tenli/aiot/ui/features/setting/screens/notification/NotifyConfigViewModel;", "Landroidx/lifecycle/ViewModel;", "()V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/tenli/aiot/ui/features/setting/screens/notification/NotifyConfigUiState;", "displayOrder", "", "", "repository", "Lcom/tenli/aiot/data/repository/DataRepository;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "closeModeSelection", "", "fetchGroupDetail", "groupKey", "fetchSettings", "getAlarmModeText", "mode", "getDisplayInfo", "Lkotlin/Triple;", "", "Landroidx/compose/ui/graphics/Color;", "key", "getTypeIcon", "onGroupClick", "navigate", "Lkotlin/Function2;", "Lcom/tenli/aiot/ui/features/setting/core/SettingScreenType;", "openModeSelection", "typeKey", "currentMode", "updateAlarmMode", "newMode", "updateGroupEnabled", "enabled", "", "updateMasterNotify", "app_productionDebug"})
public final class NotifyConfigViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.tenli.aiot.ui.features.setting.screens.notification.NotifyConfigUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.tenli.aiot.ui.features.setting.screens.notification.NotifyConfigUiState> uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.aiot.data.repository.DataRepository repository = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<java.lang.String> displayOrder = null;
    
    public NotifyConfigViewModel() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.tenli.aiot.ui.features.setting.screens.notification.NotifyConfigUiState> getUiState() {
        return null;
    }
    
    public final void fetchSettings() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlin.Triple<java.lang.String, java.lang.Integer, androidx.compose.ui.graphics.Color> getDisplayInfo(@org.jetbrains.annotations.NotNull()
    java.lang.String key) {
        return null;
    }
    
    public final void fetchGroupDetail(@org.jetbrains.annotations.NotNull()
    java.lang.String groupKey) {
    }
    
    public final void onGroupClick(@org.jetbrains.annotations.NotNull()
    java.lang.String groupKey, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super com.tenli.aiot.ui.features.setting.core.SettingScreenType, ? super java.lang.String, kotlin.Unit> navigate) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getAlarmModeText(@org.jetbrains.annotations.Nullable()
    java.lang.String mode) {
        return null;
    }
    
    public final void openModeSelection(@org.jetbrains.annotations.NotNull()
    java.lang.String typeKey, @org.jetbrains.annotations.Nullable()
    java.lang.String currentMode) {
    }
    
    public final void closeModeSelection() {
    }
    
    public final int getTypeIcon(@org.jetbrains.annotations.NotNull()
    java.lang.String key) {
        return 0;
    }
    
    public final void updateMasterNotify(boolean enabled) {
    }
    
    public final void updateGroupEnabled(@org.jetbrains.annotations.Nullable()
    java.lang.String groupKey, boolean enabled) {
    }
    
    public final void updateAlarmMode(@org.jetbrains.annotations.NotNull()
    java.lang.String newMode) {
    }
}