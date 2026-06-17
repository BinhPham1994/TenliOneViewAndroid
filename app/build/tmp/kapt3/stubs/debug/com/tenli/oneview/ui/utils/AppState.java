package com.tenli.oneview.ui.utils;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u001f\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u001a\u0010\u0003\u001a\u00020\u0004X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\bR\u001a\u0010\t\u001a\u00020\nX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\f\"\u0004\b\r\u0010\u000eR\u001a\u0010\u000f\u001a\u00020\nX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0010\u0010\f\"\u0004\b\u0011\u0010\u000eR\u001a\u0010\u0012\u001a\u00020\u0004X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0013\u0010\u0006\"\u0004\b\u0014\u0010\bR\u001a\u0010\u0015\u001a\u00020\nX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0016\u0010\f\"\u0004\b\u0017\u0010\u000eR\u001a\u0010\u0018\u001a\u00020\nX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0018\u0010\f\"\u0004\b\u0019\u0010\u000eR\u001a\u0010\u001a\u001a\u00020\nX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001a\u0010\f\"\u0004\b\u001b\u0010\u000eR\u001a\u0010\u001c\u001a\u00020\nX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001c\u0010\f\"\u0004\b\u001d\u0010\u000eR\u001a\u0010\u001e\u001a\u00020\nX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001e\u0010\f\"\u0004\b\u001f\u0010\u000eR\u001a\u0010 \u001a\u00020\nX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b!\u0010\f\"\u0004\b\"\u0010\u000eR\u001a\u0010#\u001a\u00020\u0004X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b$\u0010\u0006\"\u0004\b%\u0010\bR\u001a\u0010&\u001a\u00020\u0004X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\'\u0010\u0006\"\u0004\b(\u0010\b\u00a8\u0006)"}, d2 = {"Lcom/tenli/oneview/ui/utils/AppState;", "", "()V", "REQUEST_INTENT", "", "getREQUEST_INTENT", "()I", "setREQUEST_INTENT", "(I)V", "addDeviceManualMode", "", "getAddDeviceManualMode", "()Z", "setAddDeviceManualMode", "(Z)V", "checkFinal", "getCheckFinal", "setCheckFinal", "countCheckDeviceScan", "getCountCheckDeviceScan", "setCountCheckDeviceScan", "errorConnectServer", "getErrorConnectServer", "setErrorConnectServer", "isEditAlarm", "setEditAlarm", "isEditScript", "setEditScript", "isIntent", "setIntent", "isShowCallActivity", "setShowCallActivity", "onDestroyApp", "getOnDestroyApp", "setOnDestroyApp", "pageEventMonitorNumber", "getPageEventMonitorNumber", "setPageEventMonitorNumber", "pageEventNumber", "getPageEventNumber", "setPageEventNumber", "app_debug"})
public final class AppState {
    private static boolean addDeviceManualMode = false;
    private static boolean checkFinal = false;
    private static boolean isShowCallActivity = false;
    private static boolean isEditAlarm = false;
    private static boolean isEditScript = false;
    private static boolean isIntent = false;
    private static boolean onDestroyApp = false;
    private static boolean errorConnectServer = false;
    private static int countCheckDeviceScan = 0;
    private static int pageEventNumber = 1;
    private static int pageEventMonitorNumber = 1;
    private static int REQUEST_INTENT = 0;
    @org.jetbrains.annotations.NotNull()
    public static final com.tenli.oneview.ui.utils.AppState INSTANCE = null;
    
    private AppState() {
        super();
    }
    
    public final boolean getAddDeviceManualMode() {
        return false;
    }
    
    public final void setAddDeviceManualMode(boolean p0) {
    }
    
    public final boolean getCheckFinal() {
        return false;
    }
    
    public final void setCheckFinal(boolean p0) {
    }
    
    public final boolean isShowCallActivity() {
        return false;
    }
    
    public final void setShowCallActivity(boolean p0) {
    }
    
    public final boolean isEditAlarm() {
        return false;
    }
    
    public final void setEditAlarm(boolean p0) {
    }
    
    public final boolean isEditScript() {
        return false;
    }
    
    public final void setEditScript(boolean p0) {
    }
    
    public final boolean isIntent() {
        return false;
    }
    
    public final void setIntent(boolean p0) {
    }
    
    public final boolean getOnDestroyApp() {
        return false;
    }
    
    public final void setOnDestroyApp(boolean p0) {
    }
    
    public final boolean getErrorConnectServer() {
        return false;
    }
    
    public final void setErrorConnectServer(boolean p0) {
    }
    
    public final int getCountCheckDeviceScan() {
        return 0;
    }
    
    public final void setCountCheckDeviceScan(int p0) {
    }
    
    public final int getPageEventNumber() {
        return 0;
    }
    
    public final void setPageEventNumber(int p0) {
    }
    
    public final int getPageEventMonitorNumber() {
        return 0;
    }
    
    public final void setPageEventMonitorNumber(int p0) {
    }
    
    public final int getREQUEST_INTENT() {
        return 0;
    }
    
    public final void setREQUEST_INTENT(int p0) {
    }
}