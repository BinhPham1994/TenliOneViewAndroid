package com.tenli.oneview.ui.features.setting.screens.device.ai;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u009c\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0006\n\u0002\u0010\"\n\u0002\u0010\b\n\u0000\n\u0002\u0010$\n\u0002\u0010\u0000\n\u0002\b\u0006\n\u0002\u0010 \n\u0002\b\u0007\n\u0002\u0010\u0006\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\r\b\u0007\u0018\u00002\u00020\u0001BQ\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u0007\u0012\u0018\u0010\t\u001a\u0014\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\b0\n\u0012\u0012\u0010\r\u001a\u000e\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\b0\u000e\u00a2\u0006\u0002\u0010\u000fJ \u0010\u001d\u001a\u00020\b2\u0006\u0010\u001e\u001a\u00020\u001f2\u0006\u0010 \u001a\u00020!\u00f8\u0001\u0000\u00a2\u0006\u0004\b\"\u0010#J\u0006\u0010$\u001a\u00020\bJ\u000e\u0010%\u001a\u00020\b2\u0006\u0010&\u001a\u00020\'J\u0014\u0010(\u001a\u00020\b2\f\u0010)\u001a\b\u0012\u0004\u0012\u00020\b0\u0007J\u0006\u0010*\u001a\u00020\bJ\u0006\u0010+\u001a\u00020\bJ\u0006\u0010,\u001a\u00020\bJ&\u0010-\u001a\b\u0012\u0004\u0012\u00020/0.2\u0016\u00100\u001a\u0012\u0012\u0004\u0012\u00020\f\u0012\u0006\u0012\u0004\u0018\u000102\u0018\u000101H\u0002J\u0006\u00103\u001a\u00020\bJ\u0006\u00104\u001a\u00020\bJ\u0012\u00105\u001a\u00020\b2\n\b\u0002\u00106\u001a\u0004\u0018\u00010\fJ\u000e\u00107\u001a\u00020\b2\u0006\u0010&\u001a\u00020\'J\u0016\u00108\u001a\b\u0012\u0004\u0012\u00020\f092\u0006\u0010:\u001a\u00020\fH\u0002J\u0006\u0010;\u001a\u00020\bJ\u000e\u0010<\u001a\u00020\b2\u0006\u0010&\u001a\u00020\'J\u000e\u0010=\u001a\u00020\b2\u0006\u0010>\u001a\u00020\fJ\u0006\u0010?\u001a\u00020\bJ\u001e\u0010@\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020A09092\b\u0010B\u001a\u0004\u0018\u000102H\u0002J\u000e\u0010C\u001a\u00020\b2\u0006\u0010D\u001a\u00020/J\u0006\u0010E\u001a\u00020\bJ\u0006\u0010F\u001a\u00020\bJ\u0014\u0010G\u001a\u00020\b2\f\u0010)\u001a\b\u0012\u0004\u0012\u00020\b0\u0007J\u0014\u0010H\u001a\u00020\b2\f\u0010)\u001a\b\u0012\u0004\u0012\u00020\b0\u0007J\u000e\u0010I\u001a\u00020\b2\u0006\u0010J\u001a\u00020KJ\u000e\u0010L\u001a\u00020\b2\u0006\u0010M\u001a\u00020NJ\u000e\u0010O\u001a\u00020\b2\u0006\u0010M\u001a\u00020PJ\u000e\u0010Q\u001a\u00020\b2\u0006\u0010R\u001a\u00020/J\u000e\u0010S\u001a\u00020\b2\u0006\u0010T\u001a\u00020\'J\u000e\u0010U\u001a\u00020\b2\u0006\u0010V\u001a\u00020/J\u000e\u0010W\u001a\u00020\b2\u0006\u0010T\u001a\u00020\'J\u000e\u0010X\u001a\u00020\b2\u0006\u0010Y\u001a\u00020\fJ\u000e\u0010Z\u001a\u00020\b2\u0006\u0010T\u001a\u00020\'J\u000e\u0010[\u001a\u00020\b2\u0006\u0010\\\u001a\u00020\fR\u0014\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00120\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014R#\u0010\t\u001a\u0014\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\b0\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R\u001d\u0010\r\u001a\u000e\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\b0\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018R\u0017\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00120\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u001c\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006]"}, d2 = {"Lcom/tenli/oneview/ui/features/setting/screens/device/ai/AiSensorViewModel;", "Landroidx/lifecycle/ViewModel;", "boxRepository", "Lcom/tenli/oneview/data/repository/BoxRepository;", "device", "Lcom/tenli/oneview/model/network/DeviceItem;", "onNavigateBack", "Lkotlin/Function0;", "", "onNavigateTo", "Lkotlin/Function2;", "Lcom/tenli/oneview/ui/features/setting/core/SettingScreenType;", "", "onShowSnackbar", "Lkotlin/Function1;", "(Lcom/tenli/oneview/data/repository/BoxRepository;Lcom/tenli/oneview/model/network/DeviceItem;Lkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function2;Lkotlin/jvm/functions/Function1;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/tenli/oneview/ui/features/setting/screens/device/ai/AiSensorState;", "getOnNavigateBack", "()Lkotlin/jvm/functions/Function0;", "getOnNavigateTo", "()Lkotlin/jvm/functions/Function2;", "getOnShowSnackbar", "()Lkotlin/jvm/functions/Function1;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "addAiPoint", "offset", "Landroidx/compose/ui/geometry/Offset;", "size", "Landroidx/compose/ui/unit/IntSize;", "addAiPoint-CowoxoA", "(JJ)V", "askDeleteMonitor", "clearAiZones", "isInclude", "", "confirmDeleteMonitor", "onSuccess", "confirmResetAndBack", "dismissDeleteDialog", "dismissDialogs", "extractMonitorIds", "", "", "params", "", "", "fetchCameras", "fetchMonitorTypes", "fetchSensorsForLogic", "explicitType", "finishAiZone", "getCompatibleSensorTypes", "", "logicType", "handleBackWithValidation", "handleNextWithValidation", "loadDefaultParams", "monitorType", "nextWizardStep", "parseMasks", "", "raw", "prepareAddNewMonitor", "categoryIndex", "previousWizardStep", "resetCurrentDrawing", "saveLogicMonitor", "saveSensorMonitor", "selectCamera", "camera", "Lcom/tenli/oneview/model/network/CameraItem;", "setSelectedMonitorType", "item", "Lcom/tenli/oneview/model/network/MonitorType;", "setupEditMonitor", "Lcom/tenli/oneview/model/network/MonitorDisplayItem;", "toggleInputSensor", "monitorId", "updateAiEnabled", "enabled", "updateAiSensitivity", "level", "updateAlarmEnabled", "updateMonitorName", "name", "updateMqttEnabled", "updateMqttTopic", "topic", "app_debug"})
public final class AiSensorViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.oneview.data.repository.BoxRepository boxRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.oneview.model.network.DeviceItem device = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateBack = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function2<com.tenli.oneview.ui.features.setting.core.SettingScreenType, java.lang.String, kotlin.Unit> onNavigateTo = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function1<java.lang.String, kotlin.Unit> onShowSnackbar = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.tenli.oneview.ui.features.setting.screens.device.ai.AiSensorState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.tenli.oneview.ui.features.setting.screens.device.ai.AiSensorState> uiState = null;
    
    public AiSensorViewModel(@org.jetbrains.annotations.NotNull()
    com.tenli.oneview.data.repository.BoxRepository boxRepository, @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.DeviceItem device, @org.jetbrains.annotations.NotNull()
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
    public final kotlinx.coroutines.flow.StateFlow<com.tenli.oneview.ui.features.setting.screens.device.ai.AiSensorState> getUiState() {
        return null;
    }
    
    public final void fetchMonitorTypes() {
    }
    
    public final void setSelectedMonitorType(@org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.MonitorType item) {
    }
    
    public final void fetchCameras() {
    }
    
    public final void nextWizardStep() {
    }
    
    public final void previousWizardStep() {
    }
    
    public final void selectCamera(@org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.CameraItem camera) {
    }
    
    public final void finishAiZone(boolean isInclude) {
    }
    
    public final void clearAiZones(boolean isInclude) {
    }
    
    public final void resetCurrentDrawing() {
    }
    
    public final void updateAiEnabled(boolean enabled) {
    }
    
    public final void updateMqttEnabled(boolean enabled) {
    }
    
    public final void handleNextWithValidation(boolean isInclude) {
    }
    
    public final void handleBackWithValidation() {
    }
    
    public final void confirmResetAndBack() {
    }
    
    public final void dismissDialogs() {
    }
    
    public final void saveSensorMonitor(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onSuccess) {
    }
    
    public final void updateMonitorName(@org.jetbrains.annotations.NotNull()
    java.lang.String name) {
    }
    
    public final void updateMqttTopic(@org.jetbrains.annotations.NotNull()
    java.lang.String topic) {
    }
    
    public final void loadDefaultParams(@org.jetbrains.annotations.NotNull()
    java.lang.String monitorType) {
    }
    
    public final void updateAlarmEnabled(boolean enabled) {
    }
    
    public final void updateAiSensitivity(int level) {
    }
    
    private final java.util.List<java.util.List<java.lang.Double>> parseMasks(java.lang.Object raw) {
        return null;
    }
    
    public final void setupEditMonitor(@org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.MonitorDisplayItem item) {
    }
    
    public final void prepareAddNewMonitor(int categoryIndex) {
    }
    
    public final void askDeleteMonitor() {
    }
    
    public final void dismissDeleteDialog() {
    }
    
    public final void confirmDeleteMonitor(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onSuccess) {
    }
    
    public final void fetchSensorsForLogic(@org.jetbrains.annotations.Nullable()
    java.lang.String explicitType) {
    }
    
    public final void toggleInputSensor(int monitorId) {
    }
    
    private final java.util.Set<java.lang.Integer> extractMonitorIds(java.util.Map<java.lang.String, ? extends java.lang.Object> params) {
        return null;
    }
    
    private final java.util.List<java.lang.String> getCompatibleSensorTypes(java.lang.String logicType) {
        return null;
    }
    
    public final void saveLogicMonitor(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onSuccess) {
    }
}