package com.tenli.aiot.ui.features.setting.screens.device.camera;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001Ba\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0012\u0010\n\u001a\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\f0\u000b\u0012\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\f0\u000e\u0012\u0018\u0010\u000f\u001a\u0014\u0012\u0004\u0012\u00020\u0011\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\f0\u0010\u00a2\u0006\u0002\u0010\u0012J%\u0010\u0013\u001a\u0002H\u0014\"\b\b\u0000\u0010\u0014*\u00020\u00152\f\u0010\u0016\u001a\b\u0012\u0004\u0012\u0002H\u00140\u0017H\u0016\u00a2\u0006\u0002\u0010\u0018R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\r\u001a\b\u0012\u0004\u0012\u00020\f0\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R \u0010\u000f\u001a\u0014\u0012\u0004\u0012\u00020\u0011\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\f0\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\n\u001a\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\f0\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0019"}, d2 = {"Lcom/tenli/aiot/ui/features/setting/screens/device/camera/CameraViewModelFactory;", "Landroidx/lifecycle/ViewModelProvider$Factory;", "appRepository", "Lcom/tenli/aiot/data/repository/AppRepository;", "boxRepository", "Lcom/tenli/aiot/data/repository/BoxRepository;", "device", "Lcom/tenli/aiot/model/network/DeviceItem;", "accessKey", "", "onShowSnackbar", "Lkotlin/Function1;", "", "onNavigateBack", "Lkotlin/Function0;", "onNavigateTo", "Lkotlin/Function2;", "Lcom/tenli/aiot/ui/features/setting/core/SettingScreenType;", "(Lcom/tenli/aiot/data/repository/AppRepository;Lcom/tenli/aiot/data/repository/BoxRepository;Lcom/tenli/aiot/model/network/DeviceItem;Ljava/lang/String;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function2;)V", "create", "T", "Landroidx/lifecycle/ViewModel;", "modelClass", "Ljava/lang/Class;", "(Ljava/lang/Class;)Landroidx/lifecycle/ViewModel;", "app_debug"})
public final class CameraViewModelFactory implements androidx.lifecycle.ViewModelProvider.Factory {
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.aiot.data.repository.AppRepository appRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.aiot.data.repository.BoxRepository boxRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.aiot.model.network.DeviceItem device = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String accessKey = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function1<java.lang.String, kotlin.Unit> onShowSnackbar = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateBack = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function2<com.tenli.aiot.ui.features.setting.core.SettingScreenType, java.lang.String, kotlin.Unit> onNavigateTo = null;
    
    public CameraViewModelFactory(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.data.repository.AppRepository appRepository, @org.jetbrains.annotations.NotNull()
    com.tenli.aiot.data.repository.BoxRepository boxRepository, @org.jetbrains.annotations.NotNull()
    com.tenli.aiot.model.network.DeviceItem device, @org.jetbrains.annotations.NotNull()
    java.lang.String accessKey, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onShowSnackbar, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateBack, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super com.tenli.aiot.ui.features.setting.core.SettingScreenType, ? super java.lang.String, kotlin.Unit> onNavigateTo) {
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