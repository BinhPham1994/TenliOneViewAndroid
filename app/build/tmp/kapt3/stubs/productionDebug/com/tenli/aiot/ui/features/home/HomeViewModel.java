package com.tenli.aiot.ui.features.home;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000j\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0006\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u0012\u001a\u00020\u0013H\u0082@\u00a2\u0006\u0002\u0010\u0014J\u000e\u0010\u0015\u001a\u00020\u0013H\u0082@\u00a2\u0006\u0002\u0010\u0014J\u0016\u0010\u0016\u001a\u00020\u00132\u0006\u0010\u0017\u001a\u00020\u0018H\u0082@\u00a2\u0006\u0002\u0010\u0019J\u000e\u0010\u001a\u001a\u00020\u0013H\u0082@\u00a2\u0006\u0002\u0010\u0014J\u000e\u0010\u001b\u001a\u00020\u0013H\u0082@\u00a2\u0006\u0002\u0010\u0014J\u000e\u0010\u001c\u001a\u00020\u0013H\u0082@\u00a2\u0006\u0002\u0010\u0014J\u001c\u0010\u001d\u001a\u00020\u00132\f\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020 0\u001fH\u0082@\u00a2\u0006\u0002\u0010!J\b\u0010\"\u001a\u00020\u0013H\u0002J\u000e\u0010#\u001a\u00020\u00132\u0006\u0010$\u001a\u00020%J\u000e\u0010&\u001a\u00020\u00132\u0006\u0010\'\u001a\u00020(J\u0010\u0010)\u001a\u00020\u00132\b\b\u0002\u0010*\u001a\u00020+J\b\u0010,\u001a\u00020\u0013H\u0002R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00070\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011\u00a8\u0006-"}, d2 = {"Lcom/tenli/aiot/ui/features/home/HomeViewModel;", "Landroidx/lifecycle/AndroidViewModel;", "application", "Landroid/app/Application;", "(Landroid/app/Application;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/tenli/aiot/ui/features/home/HomeUiState;", "database", "Lcom/tenli/aiot/data/local/db/AppDatabase;", "eventDao", "Lcom/tenli/aiot/data/local/db/EventDao;", "repository", "Lcom/tenli/aiot/data/repository/DataRepository;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "fetchDevices", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "fetchEventDefines", "fetchGroupDetail", "groupId", "", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "fetchGroupEvents", "fetchGroups", "fetchRecentEvents", "fetchScriptsForGroup", "deviceItems", "", "Lcom/tenli/aiot/model/network/DeviceItem;", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "loadCachedData", "markEventAsRead", "event", "Lcom/tenli/aiot/model/network/EventItem;", "onGroupSelected", "groupDisplay", "Lcom/tenli/aiot/model/network/HomeGroupDisplay;", "refreshData", "forceRefresh", "", "updateUiWithCachedDevices", "app_productionDebug"})
@kotlin.OptIn(markerClass = {kotlinx.coroutines.ExperimentalCoroutinesApi.class})
public final class HomeViewModel extends androidx.lifecycle.AndroidViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.aiot.data.local.db.AppDatabase database = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.aiot.data.local.db.EventDao eventDao = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.aiot.data.repository.DataRepository repository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.tenli.aiot.ui.features.home.HomeUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.tenli.aiot.ui.features.home.HomeUiState> uiState = null;
    
    public HomeViewModel(@org.jetbrains.annotations.NotNull()
    android.app.Application application) {
        super(null);
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.tenli.aiot.ui.features.home.HomeUiState> getUiState() {
        return null;
    }
    
    public final void refreshData(boolean forceRefresh) {
    }
    
    private final java.lang.Object fetchDevices(kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final void updateUiWithCachedDevices() {
    }
    
    private final java.lang.Object fetchGroups(kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.Object fetchEventDefines(kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.Object fetchRecentEvents(kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.Object fetchGroupDetail(int groupId, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.Object fetchScriptsForGroup(java.util.List<com.tenli.aiot.model.network.DeviceItem> deviceItems, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    public final void onGroupSelected(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.model.network.HomeGroupDisplay groupDisplay) {
    }
    
    private final java.lang.Object fetchGroupEvents(kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    public final void markEventAsRead(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.model.network.EventItem event) {
    }
    
    private final void loadCachedData() {
    }
}