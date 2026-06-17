package com.tenli.oneview.ui.features.setting.screens.device.camera;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000h\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0000\b\u0007\u0018\u00002\u00020\u0001Ba\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\f0\u000b\u0012\u0018\u0010\r\u001a\u0014\u0012\u0004\u0012\u00020\u000f\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\f0\u000e\u0012\u0012\u0010\u0010\u001a\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\f0\u0011\u00a2\u0006\u0002\u0010\u0012J,\u0010 \u001a\u00020\f2\u0006\u0010!\u001a\u00020\t2\u0006\u0010\"\u001a\u00020\t2\u0006\u0010#\u001a\u00020\t2\f\u0010$\u001a\b\u0012\u0004\u0012\u00020\f0\u000bJ\u001c\u0010%\u001a\u00020\f2\u0006\u0010&\u001a\u00020\'2\f\u0010$\u001a\b\u0012\u0004\u0012\u00020\f0\u000bJ4\u0010(\u001a\u00020\f2\u0006\u0010&\u001a\u00020\'2\u0006\u0010!\u001a\u00020\t2\u0006\u0010\"\u001a\u00020\t2\u0006\u0010#\u001a\u00020\t2\f\u0010$\u001a\b\u0012\u0004\u0012\u00020\f0\u000bJ\u0006\u0010)\u001a\u00020\tJ\u000e\u0010*\u001a\u00020\t2\u0006\u0010+\u001a\u00020,J\u0006\u0010-\u001a\u00020\fJ\u000e\u0010.\u001a\u00020\f2\u0006\u0010+\u001a\u00020,J\u000e\u0010/\u001a\u00020\f2\u0006\u00100\u001a\u000201R\u0014\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00150\u0014X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\n\u001a\b\u0012\u0004\u0012\u00020\f0\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R#\u0010\r\u001a\u0014\u0012\u0004\u0012\u00020\u000f\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\f0\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u001d\u0010\u0010\u001a\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\f0\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u001bR\u0017\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u00150\u001d\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001f\u00a8\u00062"}, d2 = {"Lcom/tenli/oneview/ui/features/setting/screens/device/camera/CameraViewModel;", "Landroidx/lifecycle/ViewModel;", "appRepository", "Lcom/tenli/oneview/data/repository/AppRepository;", "boxRepository", "Lcom/tenli/oneview/data/repository/BoxRepository;", "device", "Lcom/tenli/oneview/model/network/DeviceItem;", "accessKey", "", "onNavigateBack", "Lkotlin/Function0;", "", "onNavigateTo", "Lkotlin/Function2;", "Lcom/tenli/oneview/ui/features/setting/core/SettingScreenType;", "onShowSnackbar", "Lkotlin/Function1;", "(Lcom/tenli/oneview/data/repository/AppRepository;Lcom/tenli/oneview/data/repository/BoxRepository;Lcom/tenli/oneview/model/network/DeviceItem;Ljava/lang/String;Lkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function2;Lkotlin/jvm/functions/Function1;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/tenli/oneview/ui/features/setting/screens/device/camera/CameraUiState;", "getOnNavigateBack", "()Lkotlin/jvm/functions/Function0;", "getOnNavigateTo", "()Lkotlin/jvm/functions/Function2;", "getOnShowSnackbar", "()Lkotlin/jvm/functions/Function1;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "addCameraByBrand", "name", "url", "decodingMode", "onSuccess", "deleteCamera", "id", "", "editCamera", "getAccessKey", "getSnapshotUrl", "camera", "Lcom/tenli/oneview/model/network/CameraInfo;", "refreshCameras", "selectCameraForEdit", "toggleMenu", "expanded", "", "app_debug"})
public final class CameraViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.oneview.data.repository.AppRepository appRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.oneview.data.repository.BoxRepository boxRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.oneview.model.network.DeviceItem device = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String accessKey = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateBack = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function2<com.tenli.oneview.ui.features.setting.core.SettingScreenType, java.lang.String, kotlin.Unit> onNavigateTo = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function1<java.lang.String, kotlin.Unit> onShowSnackbar = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.tenli.oneview.ui.features.setting.screens.device.camera.CameraUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.tenli.oneview.ui.features.setting.screens.device.camera.CameraUiState> uiState = null;
    
    public CameraViewModel(@org.jetbrains.annotations.NotNull()
    com.tenli.oneview.data.repository.AppRepository appRepository, @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.data.repository.BoxRepository boxRepository, @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.DeviceItem device, @org.jetbrains.annotations.NotNull()
    java.lang.String accessKey, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateBack, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super com.tenli.oneview.ui.features.setting.core.SettingScreenType, ? super java.lang.String, kotlin.Unit> onNavigateTo, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onShowSnackbar) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlin.jvm.functions.Function0<kotlin.Unit> getOnNavigateBack() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlin.jvm.functions.Function2<com.tenli.oneview.ui.features.setting.core.SettingScreenType, java.lang.String, kotlin.Unit> getOnNavigateTo() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlin.jvm.functions.Function1<java.lang.String, kotlin.Unit> getOnShowSnackbar() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.tenli.oneview.ui.features.setting.screens.device.camera.CameraUiState> getUiState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getSnapshotUrl(@org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.CameraInfo camera) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getAccessKey() {
        return null;
    }
    
    public final void toggleMenu(boolean expanded) {
    }
    
    public final void refreshCameras() {
    }
    
    public final void addCameraByBrand(@org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    java.lang.String url, @org.jetbrains.annotations.NotNull()
    java.lang.String decodingMode, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onSuccess) {
    }
    
    public final void selectCameraForEdit(@org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.CameraInfo camera) {
    }
    
    public final void editCamera(int id, @org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    java.lang.String url, @org.jetbrains.annotations.NotNull()
    java.lang.String decodingMode, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onSuccess) {
    }
    
    public final void deleteCamera(int id, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onSuccess) {
    }
}