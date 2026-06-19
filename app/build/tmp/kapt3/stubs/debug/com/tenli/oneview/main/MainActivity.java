package com.tenli.oneview.main;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000e\n\u0002\b\b\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u0019H\u0014J\b\u0010\u001a\u001a\u00020\u0017H\u0002J\"\u0010\u001b\u001a\u00020\u00172\u0006\u0010\u001c\u001a\u00020\u00042\u0006\u0010\u001d\u001a\u00020\u00042\b\u0010\u001e\u001a\u0004\u0018\u00010\u001fH\u0014J\u0012\u0010 \u001a\u00020\u00172\b\u0010!\u001a\u0004\u0018\u00010\"H\u0014J\u0010\u0010#\u001a\u00020\u00172\u0006\u0010$\u001a\u00020\u001fH\u0014J\b\u0010%\u001a\u00020\u0017H\u0014R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0007\u001a\u00020\b8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u000b\u0010\f\u001a\u0004\b\t\u0010\nR/\u0010\u000f\u001a\u0004\u0018\u00010\u000e2\b\u0010\r\u001a\u0004\u0018\u00010\u000e8B@BX\u0082\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\u0014\u0010\u0015\u001a\u0004\b\u0010\u0010\u0011\"\u0004\b\u0012\u0010\u0013\u00a8\u0006&"}, d2 = {"Lcom/tenli/oneview/main/MainActivity;", "Landroidx/activity/ComponentActivity;", "()V", "UPDATE_REQUEST_CODE", "", "appUpdateManager", "Lcom/google/android/play/core/appupdate/AppUpdateManager;", "splashViewModel", "Lcom/tenli/oneview/ui/features/splash/SplashViewModel;", "getSplashViewModel", "()Lcom/tenli/oneview/ui/features/splash/SplashViewModel;", "splashViewModel$delegate", "Lkotlin/Lazy;", "<set-?>", "", "startEventId", "getStartEventId", "()Ljava/lang/String;", "setStartEventId", "(Ljava/lang/String;)V", "startEventId$delegate", "Landroidx/compose/runtime/MutableState;", "attachBaseContext", "", "newBase", "Landroid/content/Context;", "checkUpdate", "onActivityResult", "requestCode", "resultCode", "data", "Landroid/content/Intent;", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onNewIntent", "intent", "onResume", "app_debug"})
public final class MainActivity extends androidx.activity.ComponentActivity {
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy splashViewModel$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState startEventId$delegate = null;
    private com.google.android.play.core.appupdate.AppUpdateManager appUpdateManager;
    private final int UPDATE_REQUEST_CODE = 123;
    
    public MainActivity() {
        super();
    }
    
    private final com.tenli.oneview.ui.features.splash.SplashViewModel getSplashViewModel() {
        return null;
    }
    
    private final java.lang.String getStartEventId() {
        return null;
    }
    
    private final void setStartEventId(java.lang.String p0) {
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    @java.lang.Override()
    protected void attachBaseContext(@org.jetbrains.annotations.NotNull()
    android.content.Context newBase) {
    }
    
    @java.lang.Override()
    protected void onNewIntent(@org.jetbrains.annotations.NotNull()
    android.content.Intent intent) {
    }
    
    private final void checkUpdate() {
    }
    
    @java.lang.Override()
    protected void onActivityResult(int requestCode, int resultCode, @org.jetbrains.annotations.Nullable()
    android.content.Intent data) {
    }
    
    @java.lang.Override()
    protected void onResume() {
    }
}