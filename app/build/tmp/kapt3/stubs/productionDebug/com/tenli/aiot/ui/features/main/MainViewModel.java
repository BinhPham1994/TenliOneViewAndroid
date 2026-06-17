package com.tenli.aiot.ui.features.main;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0007\u0018\u0000 \u001c2\u00020\u0001:\u0001\u001cB\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0010\u0010\u0013\u001a\u00020\u00142\b\b\u0002\u0010\u0015\u001a\u00020\u0016J\u000e\u0010\u0017\u001a\u00020\u00142\u0006\u0010\u0018\u001a\u00020\u0019J\u000e\u0010\u001a\u001a\u00020\u00142\u0006\u0010\u001b\u001a\u00020\u0016R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\b\u001a\b\u0012\u0004\u0012\u00020\n0\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00070\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0017\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\n0\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012\u00a8\u0006\u001d"}, d2 = {"Lcom/tenli/aiot/ui/features/main/MainViewModel;", "Landroidx/lifecycle/AndroidViewModel;", "application", "Landroid/app/Application;", "(Landroid/app/Application;)V", "_event", "Lkotlinx/coroutines/flow/MutableSharedFlow;", "Lcom/tenli/aiot/ui/features/main/MainEvent;", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/tenli/aiot/ui/features/main/MainUiState;", "event", "Lkotlinx/coroutines/flow/SharedFlow;", "getEvent", "()Lkotlinx/coroutines/flow/SharedFlow;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "logout", "", "isChangePassword", "", "onTabSelected", "tab", "Lcom/tenli/aiot/ui/features/main/MainTab;", "setBottomBarVisibility", "isVisible", "Companion", "app_productionDebug"})
public final class MainViewModel extends androidx.lifecycle.AndroidViewModel {
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.tenli.aiot.ui.features.main.MainUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.tenli.aiot.ui.features.main.MainUiState> uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableSharedFlow<com.tenli.aiot.ui.features.main.MainEvent> _event = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.SharedFlow<com.tenli.aiot.ui.features.main.MainEvent> event = null;
    @org.jetbrains.annotations.NotNull()
    private static final androidx.lifecycle.ViewModelProvider.Factory Factory = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.tenli.aiot.ui.features.main.MainViewModel.Companion Companion = null;
    
    public MainViewModel(@org.jetbrains.annotations.NotNull()
    android.app.Application application) {
        super(null);
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.tenli.aiot.ui.features.main.MainUiState> getUiState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.SharedFlow<com.tenli.aiot.ui.features.main.MainEvent> getEvent() {
        return null;
    }
    
    public final void onTabSelected(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.main.MainTab tab) {
    }
    
    public final void setBottomBarVisibility(boolean isVisible) {
    }
    
    public final void logout(boolean isChangePassword) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u0011\u0010\u0003\u001a\u00020\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/tenli/aiot/ui/features/main/MainViewModel$Companion;", "", "()V", "Factory", "Landroidx/lifecycle/ViewModelProvider$Factory;", "getFactory", "()Landroidx/lifecycle/ViewModelProvider$Factory;", "app_productionDebug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final androidx.lifecycle.ViewModelProvider.Factory getFactory() {
            return null;
        }
    }
}