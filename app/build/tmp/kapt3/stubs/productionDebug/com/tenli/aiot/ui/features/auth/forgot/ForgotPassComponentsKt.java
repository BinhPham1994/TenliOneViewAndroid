package com.tenli.aiot.ui.features.auth.forgot;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000$\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\u001a$\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0012\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00010\u0005H\u0007\u001a\u0018\u0010\u0006\u001a\u00020\u00012\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0007\u001a,\u0010\u000b\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\f\u001a\u00020\u00032\u0012\u0010\r\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00010\u0005H\u0007\u00a8\u0006\u000e"}, d2 = {"ForgotEmailStep", "", "email", "", "onEmailChange", "Lkotlin/Function1;", "ForgotNewPasswordStep", "uiState", "Lcom/tenli/aiot/ui/features/auth/forgot/ForgotPassUiState;", "viewModel", "Lcom/tenli/aiot/ui/features/auth/forgot/ForgotPassViewModel;", "ForgotOtpStep", "otp", "onOtpChange", "app_productionDebug"})
public final class ForgotPassComponentsKt {
    
    @androidx.compose.runtime.Composable()
    public static final void ForgotEmailStep(@org.jetbrains.annotations.NotNull()
    java.lang.String email, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onEmailChange) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void ForgotOtpStep(@org.jetbrains.annotations.NotNull()
    java.lang.String email, @org.jetbrains.annotations.NotNull()
    java.lang.String otp, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onOtpChange) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void ForgotNewPasswordStep(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.auth.forgot.ForgotPassUiState uiState, @org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.auth.forgot.ForgotPassViewModel viewModel) {
    }
}