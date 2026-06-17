package com.tenli.aiot.ui.features.setting.screens.device.script;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000v\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0010\b\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\n\b\u0007\u0018\u00002\u00020\u0001:\u0001CB;\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0012\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00070\u0005\u0012\u0018\u0010\b\u001a\u0014\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00070\t\u00a2\u0006\u0002\u0010\u000bJL\u0010*\u001a\u00020+2\u0006\u0010,\u001a\u00020\u00062\u0006\u0010-\u001a\u00020.2\u0006\u0010/\u001a\u00020\u00062\u0006\u00100\u001a\u00020\u00062\f\u00101\u001a\b\u0012\u0004\u0012\u000203022\f\u00104\u001a\b\u0012\u0004\u0012\u000203022\u0006\u00105\u001a\u000203H\u0002J\b\u00106\u001a\u00020\u0007H\u0002J\u001c\u00107\u001a\u00020\u00072\u0006\u00108\u001a\u00020\u00062\f\u00109\u001a\b\u0012\u0004\u0012\u00020\u00070:J`\u0010;\u001a\u00020\u00072\u0006\u00108\u001a\u00020\u00062\u0006\u0010,\u001a\u00020\u00062\u0006\u0010-\u001a\u00020.2\u0006\u0010/\u001a\u00020\u00062\u0006\u00100\u001a\u00020\u00062\f\u00101\u001a\b\u0012\u0004\u0012\u000203022\f\u00104\u001a\b\u0012\u0004\u0012\u000203022\u0006\u00105\u001a\u0002032\f\u00109\u001a\b\u0012\u0004\u0012\u00020\u00070:J\u0006\u0010<\u001a\u00020\u0007J\u0010\u0010=\u001a\u00020\u00072\b\b\u0002\u0010>\u001a\u00020.J\u000e\u0010?\u001a\u00020\u00072\u0006\u0010@\u001a\u00020\u001eJX\u0010A\u001a\u00020\u00072\u0006\u0010,\u001a\u00020\u00062\u0006\u0010-\u001a\u00020.2\u0006\u0010/\u001a\u00020\u00062\u0006\u00100\u001a\u00020\u00062\f\u00101\u001a\b\u0012\u0004\u0012\u000203022\f\u00104\u001a\b\u0012\u0004\u0012\u000203022\u0006\u00105\u001a\u0002032\f\u00109\u001a\b\u0012\u0004\u0012\u00020\u00070:J\u001e\u0010B\u001a\u00020.2\u0006\u0010,\u001a\u00020\u00062\f\u00104\u001a\b\u0012\u0004\u0012\u00020302H\u0002R\u0014\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u000e0\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u000f\u001a\u00020\u00108BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0013\u0010\u0014\u001a\u0004\b\u0011\u0010\u0012R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R#\u0010\b\u001a\u0014\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00070\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018R\u001d\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00070\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u001aR\u000e\u0010\u001b\u001a\u00020\u001cX\u0082\u0004\u00a2\u0006\u0002\n\u0000R/\u0010\u001f\u001a\u0004\u0018\u00010\u001e2\b\u0010\u001d\u001a\u0004\u0018\u00010\u001e8F@FX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b$\u0010%\u001a\u0004\b \u0010!\"\u0004\b\"\u0010#R\u0017\u0010&\u001a\b\u0012\u0004\u0012\u00020\u000e0\'\u00a2\u0006\b\n\u0000\u001a\u0004\b(\u0010)\u00a8\u0006D"}, d2 = {"Lcom/tenli/aiot/ui/features/setting/screens/device/script/ScriptViewModel;", "Landroidx/lifecycle/ViewModel;", "device", "Lcom/tenli/aiot/model/network/DeviceItem;", "onShowSnackbar", "Lkotlin/Function1;", "", "", "onNavigateTo", "Lkotlin/Function2;", "Lcom/tenli/aiot/ui/features/setting/core/SettingScreenType;", "(Lcom/tenli/aiot/model/network/DeviceItem;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function2;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/tenli/aiot/ui/features/setting/screens/device/script/ScriptUiState;", "boxApi", "Lcom/tenli/aiot/data/network/api/BoxApi;", "getBoxApi", "()Lcom/tenli/aiot/data/network/api/BoxApi;", "boxApi$delegate", "Lkotlin/Lazy;", "getDevice", "()Lcom/tenli/aiot/model/network/DeviceItem;", "getOnNavigateTo", "()Lkotlin/jvm/functions/Function2;", "getOnShowSnackbar", "()Lkotlin/jvm/functions/Function1;", "repository", "Lcom/tenli/aiot/data/repository/DataRepository;", "<set-?>", "Lcom/tenli/aiot/model/network/ScriptItem;", "selectedScript", "getSelectedScript", "()Lcom/tenli/aiot/model/network/ScriptItem;", "setSelectedScript", "(Lcom/tenli/aiot/model/network/ScriptItem;)V", "selectedScript$delegate", "Landroidx/compose/runtime/MutableState;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "buildScriptJson", "Lcom/google/gson/JsonObject;", "name", "enabled", "", "startTime", "endTime", "days", "", "", "monitorIds", "resetTime", "checkAndFetchMonitors", "deleteScript", "scriptId", "onSuccess", "Lkotlin/Function0;", "editSecurityScript", "fetchMonitors", "fetchScripts", "showLoading", "navigateToEditScript", "script", "saveSecurityScript", "validateInput", "Factory", "app_productionDebug"})
public final class ScriptViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.aiot.model.network.DeviceItem device = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function1<java.lang.String, kotlin.Unit> onShowSnackbar = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function2<com.tenli.aiot.ui.features.setting.core.SettingScreenType, java.lang.String, kotlin.Unit> onNavigateTo = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.tenli.aiot.ui.features.setting.screens.device.script.ScriptUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.tenli.aiot.ui.features.setting.screens.device.script.ScriptUiState> uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.aiot.data.repository.DataRepository repository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy boxApi$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState selectedScript$delegate = null;
    
    public ScriptViewModel(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.model.network.DeviceItem device, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onShowSnackbar, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super com.tenli.aiot.ui.features.setting.core.SettingScreenType, ? super java.lang.String, kotlin.Unit> onNavigateTo) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.aiot.model.network.DeviceItem getDevice() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlin.jvm.functions.Function1<java.lang.String, kotlin.Unit> getOnShowSnackbar() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlin.jvm.functions.Function2<com.tenli.aiot.ui.features.setting.core.SettingScreenType, java.lang.String, kotlin.Unit> getOnNavigateTo() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.tenli.aiot.ui.features.setting.screens.device.script.ScriptUiState> getUiState() {
        return null;
    }
    
    private final com.tenli.aiot.data.network.api.BoxApi getBoxApi() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.aiot.model.network.ScriptItem getSelectedScript() {
        return null;
    }
    
    public final void setSelectedScript(@org.jetbrains.annotations.Nullable()
    com.tenli.aiot.model.network.ScriptItem p0) {
    }
    
    public final void navigateToEditScript(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.model.network.ScriptItem script) {
    }
    
    private final void checkAndFetchMonitors() {
    }
    
    public final void fetchMonitors() {
    }
    
    public final void saveSecurityScript(@org.jetbrains.annotations.NotNull()
    java.lang.String name, boolean enabled, @org.jetbrains.annotations.NotNull()
    java.lang.String startTime, @org.jetbrains.annotations.NotNull()
    java.lang.String endTime, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.Integer> days, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.Integer> monitorIds, int resetTime, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onSuccess) {
    }
    
    public final void fetchScripts(boolean showLoading) {
    }
    
    public final void editSecurityScript(@org.jetbrains.annotations.NotNull()
    java.lang.String scriptId, @org.jetbrains.annotations.NotNull()
    java.lang.String name, boolean enabled, @org.jetbrains.annotations.NotNull()
    java.lang.String startTime, @org.jetbrains.annotations.NotNull()
    java.lang.String endTime, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.Integer> days, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.Integer> monitorIds, int resetTime, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onSuccess) {
    }
    
    private final boolean validateInput(java.lang.String name, java.util.List<java.lang.Integer> monitorIds) {
        return false;
    }
    
    public final void deleteScript(@org.jetbrains.annotations.NotNull()
    java.lang.String scriptId, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onSuccess) {
    }
    
    private final com.google.gson.JsonObject buildScriptJson(java.lang.String name, boolean enabled, java.lang.String startTime, java.lang.String endTime, java.util.List<java.lang.Integer> days, java.util.List<java.lang.Integer> monitorIds, int resetTime) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B;\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0012\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00070\u0005\u0012\u0018\u0010\b\u001a\u0014\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00070\t\u00a2\u0006\u0002\u0010\u000bJ%\u0010\f\u001a\u0002H\r\"\b\b\u0000\u0010\r*\u00020\u000e2\f\u0010\u000f\u001a\b\u0012\u0004\u0012\u0002H\r0\u0010H\u0016\u00a2\u0006\u0002\u0010\u0011R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R \u0010\b\u001a\u0014\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00070\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00070\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0012"}, d2 = {"Lcom/tenli/aiot/ui/features/setting/screens/device/script/ScriptViewModel$Factory;", "Landroidx/lifecycle/ViewModelProvider$Factory;", "device", "Lcom/tenli/aiot/model/network/DeviceItem;", "onShowSnackbar", "Lkotlin/Function1;", "", "", "onNavigateTo", "Lkotlin/Function2;", "Lcom/tenli/aiot/ui/features/setting/core/SettingScreenType;", "(Lcom/tenli/aiot/model/network/DeviceItem;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function2;)V", "create", "T", "Landroidx/lifecycle/ViewModel;", "modelClass", "Ljava/lang/Class;", "(Ljava/lang/Class;)Landroidx/lifecycle/ViewModel;", "app_productionDebug"})
    public static final class Factory implements androidx.lifecycle.ViewModelProvider.Factory {
        @org.jetbrains.annotations.NotNull()
        private final com.tenli.aiot.model.network.DeviceItem device = null;
        @org.jetbrains.annotations.NotNull()
        private final kotlin.jvm.functions.Function1<java.lang.String, kotlin.Unit> onShowSnackbar = null;
        @org.jetbrains.annotations.NotNull()
        private final kotlin.jvm.functions.Function2<com.tenli.aiot.ui.features.setting.core.SettingScreenType, java.lang.String, kotlin.Unit> onNavigateTo = null;
        
        public Factory(@org.jetbrains.annotations.NotNull()
        com.tenli.aiot.model.network.DeviceItem device, @org.jetbrains.annotations.NotNull()
        kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onShowSnackbar, @org.jetbrains.annotations.NotNull()
        kotlin.jvm.functions.Function2<? super com.tenli.aiot.ui.features.setting.core.SettingScreenType, ? super java.lang.String, kotlin.Unit> onNavigateTo) {
            super();
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public <T extends androidx.lifecycle.ViewModel>T create(@org.jetbrains.annotations.NotNull()
        java.lang.Class<T> modelClass) {
            return null;
        }
    }
}