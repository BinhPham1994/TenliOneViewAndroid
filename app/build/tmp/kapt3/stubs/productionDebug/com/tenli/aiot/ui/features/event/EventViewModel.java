package com.tenli.aiot.ui.features.event;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000X\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\n\b\u0007\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\u0016\u001a\u00020\u0017J\u0010\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u0019H\u0002J\u0018\u0010\u001b\u001a\u00020\u00192\u0006\u0010\u001c\u001a\u00020\u00192\u0006\u0010\u001d\u001a\u00020\u000fH\u0002J\u000e\u0010\u001e\u001a\u00020\u00172\u0006\u0010\u001f\u001a\u00020\u000fJ\b\u0010 \u001a\u00020\u0017H\u0002J\u000e\u0010!\u001a\u00020\u00172\u0006\u0010\"\u001a\u00020#J\u0006\u0010$\u001a\u00020\u0017J\u0006\u0010%\u001a\u00020\u0017J\u0006\u0010&\u001a\u00020\u0017J\u000e\u0010\'\u001a\u00020\u00172\u0006\u0010(\u001a\u00020\tJ\u000e\u0010)\u001a\u00020\u00172\u0006\u0010*\u001a\u00020\u0019J\u000e\u0010+\u001a\u00020\u00172\u0006\u0010\u001c\u001a\u00020\u0019J\u000e\u0010,\u001a\u00020\u00172\u0006\u0010\u001c\u001a\u00020\u0019R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00070\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015\u00a8\u0006-"}, d2 = {"Lcom/tenli/aiot/ui/features/event/EventViewModel;", "Landroidx/lifecycle/AndroidViewModel;", "application", "Landroid/app/Application;", "(Landroid/app/Application;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/tenli/aiot/ui/features/event/EventUiState;", "currentPage", "", "database", "Lcom/tenli/aiot/data/local/db/AppDatabase;", "eventDao", "Lcom/tenli/aiot/data/local/db/EventDao;", "isLastPage", "", "repository", "Lcom/tenli/aiot/data/repository/DataRepository;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "applyFilter", "", "convertToApiFormat", "", "dateStr", "formatToIso8601", "date", "isEndOfDay", "loadEvents", "isRefresh", "loadEventsFromCache", "markEventAsRead", "event", "Lcom/tenli/aiot/model/network/EventItem;", "navigateBack", "openFilter", "resetFilter", "toggleDeviceSelection", "deviceId", "toggleTypeSelection", "typeKey", "updateFromTime", "updateToTime", "app_productionDebug"})
public final class EventViewModel extends androidx.lifecycle.AndroidViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.aiot.data.local.db.AppDatabase database = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.aiot.data.local.db.EventDao eventDao = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.tenli.aiot.ui.features.event.EventUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.tenli.aiot.ui.features.event.EventUiState> uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.aiot.data.repository.DataRepository repository = null;
    private int currentPage = 1;
    private boolean isLastPage = false;
    
    public EventViewModel(@org.jetbrains.annotations.NotNull()
    android.app.Application application) {
        super(null);
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.tenli.aiot.ui.features.event.EventUiState> getUiState() {
        return null;
    }
    
    private final void loadEventsFromCache() {
    }
    
    public final void loadEvents(boolean isRefresh) {
    }
    
    public final void openFilter() {
    }
    
    public final void toggleDeviceSelection(int deviceId) {
    }
    
    public final void toggleTypeSelection(@org.jetbrains.annotations.NotNull()
    java.lang.String typeKey) {
    }
    
    public final void applyFilter() {
    }
    
    public final void resetFilter() {
    }
    
    public final void navigateBack() {
    }
    
    public final void markEventAsRead(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.model.network.EventItem event) {
    }
    
    private final java.lang.String formatToIso8601(java.lang.String date, boolean isEndOfDay) {
        return null;
    }
    
    public final void updateFromTime(@org.jetbrains.annotations.NotNull()
    java.lang.String date) {
    }
    
    public final void updateToTime(@org.jetbrains.annotations.NotNull()
    java.lang.String date) {
    }
    
    private final java.lang.String convertToApiFormat(java.lang.String dateStr) {
        return null;
    }
}