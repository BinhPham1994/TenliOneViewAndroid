package com.tenli.aiot.ui.features.setting.core;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u008c\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\b\u0010%\u001a\u00020&H\u0002J\u0010\u0010\'\u001a\u00020&H\u0081@\u00a2\u0006\u0004\b(\u0010)J\u0010\u0010*\u001a\u00020&2\b\u0010+\u001a\u0004\u0018\u00010\u0010J\u0006\u0010,\u001a\u00020&J\u0016\u0010-\u001a\u00020&2\u0006\u0010.\u001a\u00020\u00142\u0006\u0010/\u001a\u00020\u0010J\u0014\u00100\u001a\u00020&2\f\u00101\u001a\b\u0012\u0004\u0012\u00020&02J\u0006\u00103\u001a\u00020&J\u000e\u00104\u001a\u00020&2\u0006\u00105\u001a\u00020\u0010J\u000e\u00106\u001a\u00020&2\u0006\u00107\u001a\u000208J\u0015\u00109\u001a\u00020&2\u0006\u0010:\u001a\u00020\u0010H\u0000\u00a2\u0006\u0002\b;J\u000e\u0010<\u001a\u00020&2\u0006\u0010=\u001a\u000208J!\u0010>\u001a\u00020&2\u0012\u0010?\u001a\u000e\u0012\u0004\u0012\u00020A\u0012\u0004\u0012\u00020A0@H\u0000\u00a2\u0006\u0002\bBJ!\u0010C\u001a\u00020&2\u0012\u0010?\u001a\u000e\u0012\u0004\u0012\u00020D\u0012\u0004\u0012\u00020D0@H\u0000\u00a2\u0006\u0002\bEJ!\u0010F\u001a\u00020&2\u0012\u0010?\u001a\u000e\u0012\u0004\u0012\u00020G\u0012\u0004\u0012\u00020G0@H\u0000\u00a2\u0006\u0002\bHR\u001a\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0080\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u001a\u0010\n\u001a\b\u0012\u0004\u0012\u00020\f0\u000bX\u0080\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0010\u0010\u000f\u001a\u0004\u0018\u00010\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000R&\u0010\u0011\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0014\u0012\u0004\u0012\u00020\u00100\u00130\u0012X\u0080\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R\u001c\u0010\u0017\u001a\u0004\u0018\u00010\u0018X\u0080\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0019\u0010\u001a\"\u0004\b\u001b\u0010\u001cR\u0017\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u00070\u001e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010 R\u0017\u0010!\u001a\b\u0012\u0004\u0012\u00020\f0\"\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010$\u00a8\u0006I"}, d2 = {"Lcom/tenli/aiot/ui/features/setting/core/SettingViewModel;", "Landroidx/lifecycle/AndroidViewModel;", "application", "Landroid/app/Application;", "(Landroid/app/Application;)V", "_uiEvent", "Lkotlinx/coroutines/flow/MutableSharedFlow;", "Lcom/tenli/aiot/ui/features/setting/core/SettingUiEvent;", "get_uiEvent$app_productionDebug", "()Lkotlinx/coroutines/flow/MutableSharedFlow;", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/tenli/aiot/ui/features/setting/core/SettingUiState;", "get_uiState$app_productionDebug", "()Lkotlinx/coroutines/flow/MutableStateFlow;", "lastProcessedTarget", "", "navigationStack", "", "Lkotlin/Pair;", "Lcom/tenli/aiot/ui/features/setting/core/SettingScreenType;", "getNavigationStack$app_productionDebug", "()Ljava/util/List;", "timerJob", "Lkotlinx/coroutines/Job;", "getTimerJob$app_productionDebug", "()Lkotlinx/coroutines/Job;", "setTimerJob$app_productionDebug", "(Lkotlinx/coroutines/Job;)V", "uiEvent", "Lkotlinx/coroutines/flow/SharedFlow;", "getUiEvent", "()Lkotlinx/coroutines/flow/SharedFlow;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "checkDeviceIssues", "", "clearLocalData", "clearLocalData$app_productionDebug", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "handleInitialTarget", "target", "navigateBack", "navigateTo", "screen", "title", "performLogout", "onSuccess", "Lkotlin/Function0;", "refreshUserData", "setLanguage", "lang", "showLogoutDialog", "show", "", "showSnackbar", "message", "showSnackbar$app_productionDebug", "toggleScriptMenu", "expanded", "updateAccountState", "action", "Lkotlin/Function1;", "Lcom/tenli/aiot/ui/features/setting/core/AccountUiState;", "updateAccountState$app_productionDebug", "updateDeviceState", "Lcom/tenli/aiot/ui/features/setting/core/ClientUiState;", "updateDeviceState$app_productionDebug", "updateGroupState", "Lcom/tenli/aiot/ui/features/setting/core/GroupUiState;", "updateGroupState$app_productionDebug", "app_productionDebug"})
public final class SettingViewModel extends androidx.lifecycle.AndroidViewModel {
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.tenli.aiot.ui.features.setting.core.SettingUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.tenli.aiot.ui.features.setting.core.SettingUiState> uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableSharedFlow<com.tenli.aiot.ui.features.setting.core.SettingUiEvent> _uiEvent = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.SharedFlow<com.tenli.aiot.ui.features.setting.core.SettingUiEvent> uiEvent = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<kotlin.Pair<com.tenli.aiot.ui.features.setting.core.SettingScreenType, java.lang.String>> navigationStack = null;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job timerJob;
    @org.jetbrains.annotations.Nullable()
    private java.lang.String lastProcessedTarget;
    
    public SettingViewModel(@org.jetbrains.annotations.NotNull()
    android.app.Application application) {
        super(null);
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.MutableStateFlow<com.tenli.aiot.ui.features.setting.core.SettingUiState> get_uiState$app_productionDebug() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.tenli.aiot.ui.features.setting.core.SettingUiState> getUiState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.MutableSharedFlow<com.tenli.aiot.ui.features.setting.core.SettingUiEvent> get_uiEvent$app_productionDebug() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.SharedFlow<com.tenli.aiot.ui.features.setting.core.SettingUiEvent> getUiEvent() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<kotlin.Pair<com.tenli.aiot.ui.features.setting.core.SettingScreenType, java.lang.String>> getNavigationStack$app_productionDebug() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final kotlinx.coroutines.Job getTimerJob$app_productionDebug() {
        return null;
    }
    
    public final void setTimerJob$app_productionDebug(@org.jetbrains.annotations.Nullable()
    kotlinx.coroutines.Job p0) {
    }
    
    public final void handleInitialTarget(@org.jetbrains.annotations.Nullable()
    java.lang.String target) {
    }
    
    public final void refreshUserData() {
    }
    
    private final void checkDeviceIssues() {
    }
    
    public final void navigateTo(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.SettingScreenType screen, @org.jetbrains.annotations.NotNull()
    java.lang.String title) {
    }
    
    public final void navigateBack() {
    }
    
    public final void setLanguage(@org.jetbrains.annotations.NotNull()
    java.lang.String lang) {
    }
    
    public final void showLogoutDialog(boolean show) {
    }
    
    public final void performLogout(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onSuccess) {
    }
    
    public final void toggleScriptMenu(boolean expanded) {
    }
    
    @android.annotation.SuppressLint(value = {"UseKtx"})
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object clearLocalData$app_productionDebug(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    public final void updateAccountState$app_productionDebug(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.tenli.aiot.ui.features.setting.core.AccountUiState, com.tenli.aiot.ui.features.setting.core.AccountUiState> action) {
    }
    
    public final void updateGroupState$app_productionDebug(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.tenli.aiot.ui.features.setting.core.GroupUiState, com.tenli.aiot.ui.features.setting.core.GroupUiState> action) {
    }
    
    public final void updateDeviceState$app_productionDebug(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.tenli.aiot.ui.features.setting.core.ClientUiState, com.tenli.aiot.ui.features.setting.core.ClientUiState> action) {
    }
    
    public final void showSnackbar$app_productionDebug(@org.jetbrains.annotations.NotNull()
    java.lang.String message) {
    }
}