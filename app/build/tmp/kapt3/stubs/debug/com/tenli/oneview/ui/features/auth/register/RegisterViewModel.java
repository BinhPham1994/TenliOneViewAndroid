package com.tenli.oneview.ui.features.auth.register;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000V\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\u0006\b\u0007\u0018\u0000 *2\u00020\u0001:\u0001*B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\u0013\u001a\u00020\u0014J\u000e\u0010\u0015\u001a\u00020\u00142\u0006\u0010\u0016\u001a\u00020\u0017J\u000e\u0010\u0018\u001a\u00020\u00142\u0006\u0010\u0019\u001a\u00020\u001aJ\u000e\u0010\u001b\u001a\u00020\u00142\u0006\u0010\u001c\u001a\u00020\u001aJ\u000e\u0010\u001d\u001a\u00020\u00142\u0006\u0010\u001e\u001a\u00020\u001aJ\u000e\u0010\u001f\u001a\u00020\u00142\u0006\u0010 \u001a\u00020\u001aJ\u000e\u0010!\u001a\u00020\u00142\u0006\u0010\"\u001a\u00020\u001aJ\u0010\u0010#\u001a\u00020\u00142\u0006\u0010$\u001a\u00020%H\u0002J\u0006\u0010&\u001a\u00020\u0014J\u0006\u0010\'\u001a\u00020\u0014J\u0006\u0010(\u001a\u00020\u0014J\u0006\u0010)\u001a\u00020\u0014R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\b\u001a\b\u0012\u0004\u0012\u00020\n0\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00070\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0017\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\n0\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012\u00a8\u0006+"}, d2 = {"Lcom/tenli/oneview/ui/features/auth/register/RegisterViewModel;", "Landroidx/lifecycle/ViewModel;", "authRepository", "Lcom/tenli/oneview/data/repository/AuthRepository;", "(Lcom/tenli/oneview/data/repository/AuthRepository;)V", "_event", "Lkotlinx/coroutines/flow/MutableSharedFlow;", "Lcom/tenli/oneview/ui/features/auth/register/RegisterEvent;", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/tenli/oneview/ui/features/auth/register/RegisterUiState;", "event", "Lkotlinx/coroutines/flow/SharedFlow;", "getEvent", "()Lkotlinx/coroutines/flow/SharedFlow;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "createAccount", "", "onAcceptChange", "accepted", "", "onConfirmPasswordChange", "cp", "", "onEmailChange", "email", "onOtpChange", "otp", "onPasswordChange", "p", "onUsernameChange", "name", "sendError", "uiText", "Lcom/tenli/oneview/ui/utils/UiText;", "toggleConfirmPassword", "togglePassword", "verifyEmail", "verifyOtp", "Companion", "app_debug"})
public final class RegisterViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.oneview.data.repository.AuthRepository authRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.tenli.oneview.ui.features.auth.register.RegisterUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.tenli.oneview.ui.features.auth.register.RegisterUiState> uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableSharedFlow<com.tenli.oneview.ui.features.auth.register.RegisterEvent> _event = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.SharedFlow<com.tenli.oneview.ui.features.auth.register.RegisterEvent> event = null;
    @org.jetbrains.annotations.NotNull()
    private static final androidx.lifecycle.ViewModelProvider.Factory Factory = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.tenli.oneview.ui.features.auth.register.RegisterViewModel.Companion Companion = null;
    
    public RegisterViewModel(@org.jetbrains.annotations.NotNull()
    com.tenli.oneview.data.repository.AuthRepository authRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.tenli.oneview.ui.features.auth.register.RegisterUiState> getUiState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.SharedFlow<com.tenli.oneview.ui.features.auth.register.RegisterEvent> getEvent() {
        return null;
    }
    
    public final void onEmailChange(@org.jetbrains.annotations.NotNull()
    java.lang.String email) {
    }
    
    public final void onOtpChange(@org.jetbrains.annotations.NotNull()
    java.lang.String otp) {
    }
    
    public final void onUsernameChange(@org.jetbrains.annotations.NotNull()
    java.lang.String name) {
    }
    
    public final void onPasswordChange(@org.jetbrains.annotations.NotNull()
    java.lang.String p) {
    }
    
    public final void onConfirmPasswordChange(@org.jetbrains.annotations.NotNull()
    java.lang.String cp) {
    }
    
    public final void onAcceptChange(boolean accepted) {
    }
    
    public final void togglePassword() {
    }
    
    public final void toggleConfirmPassword() {
    }
    
    private final void sendError(com.tenli.oneview.ui.utils.UiText uiText) {
    }
    
    public final void verifyEmail() {
    }
    
    public final void verifyOtp() {
    }
    
    public final void createAccount() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u0011\u0010\u0003\u001a\u00020\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/tenli/oneview/ui/features/auth/register/RegisterViewModel$Companion;", "", "()V", "Factory", "Landroidx/lifecycle/ViewModelProvider$Factory;", "getFactory", "()Landroidx/lifecycle/ViewModelProvider$Factory;", "app_debug"})
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