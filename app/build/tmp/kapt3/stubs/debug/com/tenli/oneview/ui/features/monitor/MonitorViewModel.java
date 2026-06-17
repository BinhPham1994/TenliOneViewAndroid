package com.tenli.oneview.ui.features.monitor;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u008c\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0007\u0018\u0000 92\u00020\u0001:\u00019B%\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\u0010\u0010\"\u001a\u00020#2\u0006\u0010$\u001a\u00020%H\u0002J4\u0010&\u001a&\u0012\n\u0012\b\u0012\u0004\u0012\u00020%0\r\u0012\u0016\u0012\u0014\u0012\u0004\u0012\u00020\u001d\u0012\n\u0012\b\u0012\u0004\u0012\u00020%0\r0(0\'2\u0006\u0010)\u001a\u00020*H\u0002J\b\u0010+\u001a\u00020#H\u0002J\u000e\u0010,\u001a\u00020#2\u0006\u0010-\u001a\u00020\u000eJ\u0006\u0010.\u001a\u00020#J\u000e\u0010/\u001a\u00020#2\u0006\u00100\u001a\u000201J\u000e\u00102\u001a\u00020#2\u0006\u0010$\u001a\u00020%J\u0006\u00103\u001a\u00020#J\u0016\u00104\u001a\u00020#2\u0006\u0010$\u001a\u00020%2\u0006\u00105\u001a\u000206J\u0018\u00107\u001a\u00020#2\u0006\u0010$\u001a\u00020%2\u0006\u00108\u001a\u000206H\u0002R\u001a\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00100\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0014X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R#\u0010\u0015\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\r0\u0016\u00a2\u0006\u000e\n\u0000\u0012\u0004\b\u0017\u0010\u0018\u001a\u0004\b\u0019\u0010\u001aR\u0014\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u001d0\u001cX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001e\u001a\u00020\u001fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010 \u001a\b\u0012\u0004\u0012\u00020\u00100\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\u001a\u00a8\u0006:"}, d2 = {"Lcom/tenli/oneview/ui/features/monitor/MonitorViewModel;", "Landroidx/lifecycle/AndroidViewModel;", "application", "Landroid/app/Application;", "boxRepository", "Lcom/tenli/oneview/data/repository/BoxRepository;", "eventRepository", "Lcom/tenli/oneview/data/repository/EventRepository;", "dispatcherProvider", "Lcom/tenli/oneview/util/DispatcherProvider;", "(Landroid/app/Application;Lcom/tenli/oneview/data/repository/BoxRepository;Lcom/tenli/oneview/data/repository/EventRepository;Lcom/tenli/oneview/util/DispatcherProvider;)V", "_monitorEvents", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "Lcom/tenli/oneview/model/network/EventItem;", "_uiState", "Lcom/tenli/oneview/ui/features/monitor/MonitorUiState;", "database", "Lcom/tenli/oneview/data/local/db/AppDatabase;", "eventDao", "Lcom/tenli/oneview/data/local/db/EventDao;", "monitorEvents", "Lkotlinx/coroutines/flow/StateFlow;", "getMonitorEvents$annotations", "()V", "getMonitorEvents", "()Lkotlinx/coroutines/flow/StateFlow;", "monitorTypeComparator", "Ljava/util/Comparator;", "", "repository", "Lcom/tenli/oneview/data/repository/DataRepository;", "uiState", "getUiState", "fetchEventsForMonitor", "", "item", "Lcom/tenli/oneview/model/network/MonitorDisplayItem;", "getFilteredAndGroupedMonitors", "Lkotlin/Pair;", "Ljava/util/SortedMap;", "groupId", "", "loadInitialState", "markEventAsRead", "event", "navigateBack", "onGroupSelected", "groupDisplay", "Lcom/tenli/oneview/model/network/HomeGroupDisplay;", "onMonitorSelected", "refreshMonitors", "toggleMonitorStatus", "isChecked", "", "updateLocalMonitorStatus", "isSecurityEnabled", "Companion", "app_debug"})
public final class MonitorViewModel extends androidx.lifecycle.AndroidViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.oneview.data.repository.BoxRepository boxRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.oneview.data.repository.EventRepository eventRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.oneview.util.DispatcherProvider dispatcherProvider = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.oneview.data.local.db.AppDatabase database = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.oneview.data.local.db.EventDao eventDao = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.tenli.oneview.ui.features.monitor.MonitorUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.tenli.oneview.ui.features.monitor.MonitorUiState> uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.oneview.data.repository.DataRepository repository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.tenli.oneview.model.network.EventItem>> _monitorEvents = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Comparator<java.lang.String> monitorTypeComparator = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.tenli.oneview.model.network.EventItem>> monitorEvents = null;
    @org.jetbrains.annotations.NotNull()
    private static final androidx.lifecycle.ViewModelProvider.Factory Factory = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.tenli.oneview.ui.features.monitor.MonitorViewModel.Companion Companion = null;
    
    public MonitorViewModel(@org.jetbrains.annotations.NotNull()
    android.app.Application application, @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.data.repository.BoxRepository boxRepository, @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.data.repository.EventRepository eventRepository, @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.util.DispatcherProvider dispatcherProvider) {
        super(null);
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.tenli.oneview.ui.features.monitor.MonitorUiState> getUiState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.tenli.oneview.model.network.EventItem>> getMonitorEvents() {
        return null;
    }
    
    @kotlin.OptIn(markerClass = {kotlinx.coroutines.ExperimentalCoroutinesApi.class})
    @java.lang.Deprecated()
    public static void getMonitorEvents$annotations() {
    }
    
    private final kotlin.Pair<java.util.List<com.tenli.oneview.model.network.MonitorDisplayItem>, java.util.SortedMap<java.lang.String, java.util.List<com.tenli.oneview.model.network.MonitorDisplayItem>>> getFilteredAndGroupedMonitors(int groupId) {
        return null;
    }
    
    public final void refreshMonitors() {
    }
    
    public final void onGroupSelected(@org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.HomeGroupDisplay groupDisplay) {
    }
    
    public final void onMonitorSelected(@org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.MonitorDisplayItem item) {
    }
    
    public final void navigateBack() {
    }
    
    private final void loadInitialState() {
    }
    
    private final void fetchEventsForMonitor(com.tenli.oneview.model.network.MonitorDisplayItem item) {
    }
    
    public final void markEventAsRead(@org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.EventItem event) {
    }
    
    private final void updateLocalMonitorStatus(com.tenli.oneview.model.network.MonitorDisplayItem item, boolean isSecurityEnabled) {
    }
    
    public final void toggleMonitorStatus(@org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.MonitorDisplayItem item, boolean isChecked) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u0011\u0010\u0003\u001a\u00020\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/tenli/oneview/ui/features/monitor/MonitorViewModel$Companion;", "", "()V", "Factory", "Landroidx/lifecycle/ViewModelProvider$Factory;", "getFactory", "()Landroidx/lifecycle/ViewModelProvider$Factory;", "app_debug"})
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