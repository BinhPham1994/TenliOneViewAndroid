package com.tenli.aiot.ui.features.monitor;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000z\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0007\u0018\u0000 32\u00020\u0001:\u00013B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0010\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u001fH\u0002J4\u0010 \u001a&\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001f0\u0007\u0012\u0016\u0012\u0014\u0012\u0004\u0012\u00020\u0017\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001f0\u00070\"0!2\u0006\u0010#\u001a\u00020$H\u0002J\b\u0010%\u001a\u00020\u001dH\u0002J\u000e\u0010&\u001a\u00020\u001d2\u0006\u0010\'\u001a\u00020\bJ\u0006\u0010(\u001a\u00020\u001dJ\u000e\u0010)\u001a\u00020\u001d2\u0006\u0010*\u001a\u00020+J\u000e\u0010,\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u001fJ\u0006\u0010-\u001a\u00020\u001dJ\u0016\u0010.\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u001f2\u0006\u0010/\u001a\u000200J\u0018\u00101\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u001f2\u0006\u00102\u001a\u000200H\u0002R\u001a\u0010\u0005\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\n0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R#\u0010\u000f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0010\u00a2\u0006\u000e\n\u0000\u0012\u0004\b\u0011\u0010\u0012\u001a\u0004\b\u0013\u0010\u0014R\u0014\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00170\u0016X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0018\u001a\u00020\u0019X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\n0\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0014\u00a8\u00064"}, d2 = {"Lcom/tenli/aiot/ui/features/monitor/MonitorViewModel;", "Landroidx/lifecycle/AndroidViewModel;", "application", "Landroid/app/Application;", "(Landroid/app/Application;)V", "_monitorEvents", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "Lcom/tenli/aiot/model/network/EventItem;", "_uiState", "Lcom/tenli/aiot/ui/features/monitor/MonitorUiState;", "database", "Lcom/tenli/aiot/data/local/db/AppDatabase;", "eventDao", "Lcom/tenli/aiot/data/local/db/EventDao;", "monitorEvents", "Lkotlinx/coroutines/flow/StateFlow;", "getMonitorEvents$annotations", "()V", "getMonitorEvents", "()Lkotlinx/coroutines/flow/StateFlow;", "monitorTypeComparator", "Ljava/util/Comparator;", "", "repository", "Lcom/tenli/aiot/data/repository/DataRepository;", "uiState", "getUiState", "fetchEventsForMonitor", "", "item", "Lcom/tenli/aiot/model/network/MonitorDisplayItem;", "getFilteredAndGroupedMonitors", "Lkotlin/Pair;", "Ljava/util/SortedMap;", "groupId", "", "loadInitialState", "markEventAsRead", "event", "navigateBack", "onGroupSelected", "groupDisplay", "Lcom/tenli/aiot/model/network/HomeGroupDisplay;", "onMonitorSelected", "refreshMonitors", "toggleMonitorStatus", "isChecked", "", "updateLocalMonitorStatus", "isSecurityEnabled", "Companion", "app_productionDebug"})
public final class MonitorViewModel extends androidx.lifecycle.AndroidViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.aiot.data.local.db.AppDatabase database = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.aiot.data.local.db.EventDao eventDao = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.tenli.aiot.ui.features.monitor.MonitorUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.tenli.aiot.ui.features.monitor.MonitorUiState> uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.aiot.data.repository.DataRepository repository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.tenli.aiot.model.network.EventItem>> _monitorEvents = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Comparator<java.lang.String> monitorTypeComparator = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.tenli.aiot.model.network.EventItem>> monitorEvents = null;
    @org.jetbrains.annotations.NotNull()
    private static final androidx.lifecycle.ViewModelProvider.Factory Factory = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.tenli.aiot.ui.features.monitor.MonitorViewModel.Companion Companion = null;
    
    public MonitorViewModel(@org.jetbrains.annotations.NotNull()
    android.app.Application application) {
        super(null);
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.tenli.aiot.ui.features.monitor.MonitorUiState> getUiState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.tenli.aiot.model.network.EventItem>> getMonitorEvents() {
        return null;
    }
    
    @kotlin.OptIn(markerClass = {kotlinx.coroutines.ExperimentalCoroutinesApi.class})
    @java.lang.Deprecated()
    public static void getMonitorEvents$annotations() {
    }
    
    private final kotlin.Pair<java.util.List<com.tenli.aiot.model.network.MonitorDisplayItem>, java.util.SortedMap<java.lang.String, java.util.List<com.tenli.aiot.model.network.MonitorDisplayItem>>> getFilteredAndGroupedMonitors(int groupId) {
        return null;
    }
    
    public final void refreshMonitors() {
    }
    
    public final void onGroupSelected(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.model.network.HomeGroupDisplay groupDisplay) {
    }
    
    public final void onMonitorSelected(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.model.network.MonitorDisplayItem item) {
    }
    
    public final void navigateBack() {
    }
    
    private final void loadInitialState() {
    }
    
    private final void fetchEventsForMonitor(com.tenli.aiot.model.network.MonitorDisplayItem item) {
    }
    
    public final void markEventAsRead(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.model.network.EventItem event) {
    }
    
    private final void updateLocalMonitorStatus(com.tenli.aiot.model.network.MonitorDisplayItem item, boolean isSecurityEnabled) {
    }
    
    public final void toggleMonitorStatus(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.model.network.MonitorDisplayItem item, boolean isChecked) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u0011\u0010\u0003\u001a\u00020\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/tenli/aiot/ui/features/monitor/MonitorViewModel$Companion;", "", "()V", "Factory", "Landroidx/lifecycle/ViewModelProvider$Factory;", "getFactory", "()Landroidx/lifecycle/ViewModelProvider$Factory;", "app_productionDebug"})
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