package com.tenli.oneview.ui.features.auth.login;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000^\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\n\b\u0007\u0018\u0000 )2\u00020\u0001:\u0001)B\u001d\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u0018\u0010\u0019\u001a\u00020\u001a2\b\b\u0002\u0010\u001b\u001a\u00020\u0014H\u0083@\u00a2\u0006\u0002\u0010\u001cJ\u0010\u0010\u001d\u001a\u00020\u001e2\u0006\u0010\u001f\u001a\u00020 H\u0002J\u0006\u0010!\u001a\u00020\u001eJ\u000e\u0010\"\u001a\u00020\u001e2\u0006\u0010#\u001a\u00020\u0014J\u000e\u0010$\u001a\u00020\u001e2\u0006\u0010%\u001a\u00020\u0014J\u000e\u0010&\u001a\u00020\u001e2\u0006\u0010\'\u001a\u00020\u0014J\u0006\u0010(\u001a\u00020\u001eR\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u000e0\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012R\u000e\u0010\u0013\u001a\u00020\u0014X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u000e0\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018\u00a8\u0006*"}, d2 = {"Lcom/tenli/oneview/ui/features/auth/login/LoginViewModel;", "Landroidx/lifecycle/AndroidViewModel;", "application", "Landroid/app/Application;", "authRepository", "Lcom/tenli/oneview/data/repository/AuthRepository;", "dispatcherProvider", "Lcom/tenli/oneview/util/DispatcherProvider;", "(Landroid/app/Application;Lcom/tenli/oneview/data/repository/AuthRepository;Lcom/tenli/oneview/util/DispatcherProvider;)V", "_event", "Lkotlinx/coroutines/flow/MutableSharedFlow;", "Lcom/tenli/oneview/ui/features/auth/login/LoginEvent;", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/tenli/oneview/ui/features/auth/login/LoginUiState;", "event", "Lkotlinx/coroutines/flow/SharedFlow;", "getEvent", "()Lkotlinx/coroutines/flow/SharedFlow;", "notifyToken", "", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "createLoginRequest", "Lcom/tenli/oneview/model/network/LoginRequest;", "googleIdToken", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "handleLoginSuccess", "", "loginData", "Lcom/tenli/oneview/model/network/LoginResponseData;", "login", "loginWithGoogle", "firebaseIdToken", "onEmailChanged", "email", "onPasswordChanged", "pass", "toggleShowPassword", "Companion", "app_debug"})
public final class LoginViewModel extends androidx.lifecycle.AndroidViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.oneview.data.repository.AuthRepository authRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.oneview.util.DispatcherProvider dispatcherProvider = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.tenli.oneview.ui.features.auth.login.LoginUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.tenli.oneview.ui.features.auth.login.LoginUiState> uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableSharedFlow<com.tenli.oneview.ui.features.auth.login.LoginEvent> _event = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.SharedFlow<com.tenli.oneview.ui.features.auth.login.LoginEvent> event = null;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String notifyToken = "";
    @org.jetbrains.annotations.NotNull()
    private static final androidx.lifecycle.ViewModelProvider.Factory Factory = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.tenli.oneview.ui.features.auth.login.LoginViewModel.Companion Companion = null;
    
    public LoginViewModel(@org.jetbrains.annotations.NotNull()
    android.app.Application application, @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.data.repository.AuthRepository authRepository, @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.util.DispatcherProvider dispatcherProvider) {
        super(null);
    }
    
    public final void onEmailChanged(@org.jetbrains.annotations.NotNull()
    java.lang.String email) {
    }
    
    public final void onPasswordChanged(@org.jetbrains.annotations.NotNull()
    java.lang.String pass) {
    }
    
    public final void toggleShowPassword() {
    }
    
    public final void login() {
    }
    
    public final void loginWithGoogle(@org.jetbrains.annotations.NotNull()
    java.lang.String firebaseIdToken) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.tenli.oneview.ui.features.auth.login.LoginUiState> getUiState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.SharedFlow<com.tenli.oneview.ui.features.auth.login.LoginEvent> getEvent() {
        return null;
    }
    
    private final void handleLoginSuccess(com.tenli.oneview.model.network.LoginResponseData loginData) {
    }
    
    @android.annotation.SuppressLint(value = {"HardwareIds"})
    private final java.lang.Object createLoginRequest(java.lang.String googleIdToken, kotlin.coroutines.Continuation<? super com.tenli.oneview.model.network.LoginRequest> $completion) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u0011\u0010\u0003\u001a\u00020\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/tenli/oneview/ui/features/auth/login/LoginViewModel$Companion;", "", "()V", "Factory", "Landroidx/lifecycle/ViewModelProvider$Factory;", "getFactory", "()Landroidx/lifecycle/ViewModelProvider$Factory;", "app_debug"})
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