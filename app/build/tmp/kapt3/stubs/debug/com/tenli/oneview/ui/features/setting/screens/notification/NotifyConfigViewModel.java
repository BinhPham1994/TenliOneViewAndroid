package com.tenli.oneview.ui.features.setting.screens.notification;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000`\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u000b\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001:\u0001+B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\u0011\u001a\u00020\u0012J\u000e\u0010\u0013\u001a\u00020\u00122\u0006\u0010\u0014\u001a\u00020\nJ\u0006\u0010\u0015\u001a\u00020\u0012J\u0010\u0010\u0016\u001a\u00020\n2\b\u0010\u0017\u001a\u0004\u0018\u00010\nJ \u0010\u0018\u001a\u0014\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u001a\u0012\u0004\u0012\u00020\u001b0\u00192\u0006\u0010\u001c\u001a\u00020\nJ\u000e\u0010\u001d\u001a\u00020\u001a2\u0006\u0010\u001c\u001a\u00020\nJ(\u0010\u001e\u001a\u00020\u00122\u0006\u0010\u0014\u001a\u00020\n2\u0018\u0010\u001f\u001a\u0014\u0012\u0004\u0012\u00020!\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u00120 J\u0018\u0010\"\u001a\u00020\u00122\u0006\u0010#\u001a\u00020\n2\b\u0010$\u001a\u0004\u0018\u00010\nJ\u000e\u0010%\u001a\u00020\u00122\u0006\u0010&\u001a\u00020\nJ\u0018\u0010\'\u001a\u00020\u00122\b\u0010\u0014\u001a\u0004\u0018\u00010\n2\u0006\u0010(\u001a\u00020)J\u000e\u0010*\u001a\u00020\u00122\u0006\u0010(\u001a\u00020)R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\b\u001a\b\u0012\u0004\u0012\u00020\n0\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00070\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010\u00a8\u0006,"}, d2 = {"Lcom/tenli/oneview/ui/features/setting/screens/notification/NotifyConfigViewModel;", "Landroidx/lifecycle/ViewModel;", "alarmRepository", "Lcom/tenli/oneview/data/repository/AlarmRepository;", "(Lcom/tenli/oneview/data/repository/AlarmRepository;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/tenli/oneview/ui/features/setting/screens/notification/NotifyConfigUiState;", "displayOrder", "", "", "repository", "Lcom/tenli/oneview/data/repository/DataRepository;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "closeModeSelection", "", "fetchGroupDetail", "groupKey", "fetchSettings", "getAlarmModeText", "mode", "getDisplayInfo", "Lkotlin/Triple;", "", "Landroidx/compose/ui/graphics/Color;", "key", "getTypeIcon", "onGroupClick", "navigate", "Lkotlin/Function2;", "Lcom/tenli/oneview/ui/features/setting/core/SettingScreenType;", "openModeSelection", "typeKey", "currentMode", "updateAlarmMode", "newMode", "updateGroupEnabled", "enabled", "", "updateMasterNotify", "Factory", "app_debug"})
public final class NotifyConfigViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.oneview.data.repository.AlarmRepository alarmRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.tenli.oneview.ui.features.setting.screens.notification.NotifyConfigUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.tenli.oneview.ui.features.setting.screens.notification.NotifyConfigUiState> uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.oneview.data.repository.DataRepository repository = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<java.lang.String> displayOrder = null;
    
    public NotifyConfigViewModel(@org.jetbrains.annotations.NotNull()
    com.tenli.oneview.data.repository.AlarmRepository alarmRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.tenli.oneview.ui.features.setting.screens.notification.NotifyConfigUiState> getUiState() {
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
    kotlin.jvm.functions.Function2<? super com.tenli.oneview.ui.features.setting.core.SettingScreenType, ? super java.lang.String, kotlin.Unit> navigate) {
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
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J%\u0010\u0005\u001a\u0002H\u0006\"\b\b\u0000\u0010\u0006*\u00020\u00072\f\u0010\b\u001a\b\u0012\u0004\u0012\u0002H\u00060\tH\u0016\u00a2\u0006\u0002\u0010\nR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000b"}, d2 = {"Lcom/tenli/oneview/ui/features/setting/screens/notification/NotifyConfigViewModel$Factory;", "Landroidx/lifecycle/ViewModelProvider$Factory;", "alarmRepository", "Lcom/tenli/oneview/data/repository/AlarmRepository;", "(Lcom/tenli/oneview/data/repository/AlarmRepository;)V", "create", "T", "Landroidx/lifecycle/ViewModel;", "modelClass", "Ljava/lang/Class;", "(Ljava/lang/Class;)Landroidx/lifecycle/ViewModel;", "app_debug"})
    public static final class Factory implements androidx.lifecycle.ViewModelProvider.Factory {
        @org.jetbrains.annotations.NotNull()
        private final com.tenli.oneview.data.repository.AlarmRepository alarmRepository = null;
        
        public Factory(@org.jetbrains.annotations.NotNull()
        com.tenli.oneview.data.repository.AlarmRepository alarmRepository) {
            super();
        }
        
        @java.lang.Override()
        @kotlin.Suppress(names = {"UNCHECKED_CAST"})
        @org.jetbrains.annotations.NotNull()
        public <T extends androidx.lifecycle.ViewModel>T create(@org.jetbrains.annotations.NotNull()
        java.lang.Class<T> modelClass) {
            return null;
        }
    }
}