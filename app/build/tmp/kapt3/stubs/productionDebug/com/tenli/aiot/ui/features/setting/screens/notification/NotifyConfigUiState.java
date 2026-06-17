package com.tenli.aiot.ui.features.setting.screens.notification;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\"\n\u0002\u0010\b\n\u0002\b\u0002\b\u0087\b\u0018\u00002\u00020\u0001B\u0081\u0001\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\u000e\b\u0002\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006\u0012\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\t\u0012\b\b\u0002\u0010\n\u001a\u00020\u000b\u0012\b\b\u0002\u0010\f\u001a\u00020\t\u0012\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u000e\u0012\b\b\u0002\u0010\u000f\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0010\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\t\u0012\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\t\u00a2\u0006\u0002\u0010\u0013J\t\u0010\"\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010#\u001a\u0004\u0018\u00010\tH\u00c6\u0003J\u000b\u0010$\u001a\u0004\u0018\u00010\tH\u00c6\u0003J\t\u0010%\u001a\u00020\u0003H\u00c6\u0003J\u000f\u0010&\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006H\u00c6\u0003J\u000b\u0010\'\u001a\u0004\u0018\u00010\tH\u00c6\u0003J\t\u0010(\u001a\u00020\u000bH\u00c6\u0003J\t\u0010)\u001a\u00020\tH\u00c6\u0003J\u000b\u0010*\u001a\u0004\u0018\u00010\u000eH\u00c6\u0003J\t\u0010+\u001a\u00020\u0003H\u00c6\u0003J\t\u0010,\u001a\u00020\u0003H\u00c6\u0003J\u0085\u0001\u0010-\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\u000e\b\u0002\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u00062\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\t2\b\b\u0002\u0010\n\u001a\u00020\u000b2\b\b\u0002\u0010\f\u001a\u00020\t2\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u000e2\b\b\u0002\u0010\u000f\u001a\u00020\u00032\b\b\u0002\u0010\u0010\u001a\u00020\u00032\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\t2\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\tH\u00c6\u0001J\u0013\u0010.\u001a\u00020\u00032\b\u0010/\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u00100\u001a\u000201H\u00d6\u0001J\t\u00102\u001a\u00020\tH\u00d6\u0001R\u0013\u0010\u0012\u001a\u0004\u0018\u00010\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0011\u0010\n\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u0013\u0010\b\u001a\u0004\u0018\u00010\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0015R\u0017\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u001aR\u0011\u0010\u000f\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u001bR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0002\u0010\u001bR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001bR\u0011\u0010\f\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u0015R\u0013\u0010\r\u001a\u0004\u0018\u00010\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001fR\u0013\u0010\u0011\u001a\u0004\u0018\u00010\t\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u0015R\u0011\u0010\u0010\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\u001b\u00a8\u00063"}, d2 = {"Lcom/tenli/aiot/ui/features/setting/screens/notification/NotifyConfigUiState;", "", "isLoading", "", "masterEnabled", "groups", "", "Lcom/tenli/aiot/model/network/EventTypeGroup;", "errorMessage", "", "currentScreen", "Lcom/tenli/aiot/ui/features/setting/core/SettingScreenType;", "screenTitle", "selectedGroupDetail", "Lcom/tenli/aiot/model/network/EventTypeGroupDetail;", "isDetailLoading", "showModeSheet", "selectedTypeKey", "currentMode", "(ZZLjava/util/List;Ljava/lang/String;Lcom/tenli/aiot/ui/features/setting/core/SettingScreenType;Ljava/lang/String;Lcom/tenli/aiot/model/network/EventTypeGroupDetail;ZZLjava/lang/String;Ljava/lang/String;)V", "getCurrentMode", "()Ljava/lang/String;", "getCurrentScreen", "()Lcom/tenli/aiot/ui/features/setting/core/SettingScreenType;", "getErrorMessage", "getGroups", "()Ljava/util/List;", "()Z", "getMasterEnabled", "getScreenTitle", "getSelectedGroupDetail", "()Lcom/tenli/aiot/model/network/EventTypeGroupDetail;", "getSelectedTypeKey", "getShowModeSheet", "component1", "component10", "component11", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "hashCode", "", "toString", "app_productionDebug"})
public final class NotifyConfigUiState {
    private final boolean isLoading = false;
    private final boolean masterEnabled = false;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.tenli.aiot.model.network.EventTypeGroup> groups = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String errorMessage = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.aiot.ui.features.setting.core.SettingScreenType currentScreen = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String screenTitle = null;
    @org.jetbrains.annotations.Nullable()
    private final com.tenli.aiot.model.network.EventTypeGroupDetail selectedGroupDetail = null;
    private final boolean isDetailLoading = false;
    private final boolean showModeSheet = false;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String selectedTypeKey = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String currentMode = null;
    
    public final boolean component1() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component10() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component11() {
        return null;
    }
    
    public final boolean component2() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.tenli.aiot.model.network.EventTypeGroup> component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.aiot.ui.features.setting.core.SettingScreenType component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component6() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.aiot.model.network.EventTypeGroupDetail component7() {
        return null;
    }
    
    public final boolean component8() {
        return false;
    }
    
    public final boolean component9() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.aiot.ui.features.setting.screens.notification.NotifyConfigUiState copy(boolean isLoading, boolean masterEnabled, @org.jetbrains.annotations.NotNull()
    java.util.List<com.tenli.aiot.model.network.EventTypeGroup> groups, @org.jetbrains.annotations.Nullable()
    java.lang.String errorMessage, @org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.SettingScreenType currentScreen, @org.jetbrains.annotations.NotNull()
    java.lang.String screenTitle, @org.jetbrains.annotations.Nullable()
    com.tenli.aiot.model.network.EventTypeGroupDetail selectedGroupDetail, boolean isDetailLoading, boolean showModeSheet, @org.jetbrains.annotations.Nullable()
    java.lang.String selectedTypeKey, @org.jetbrains.annotations.Nullable()
    java.lang.String currentMode) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
    
    public NotifyConfigUiState(boolean isLoading, boolean masterEnabled, @org.jetbrains.annotations.NotNull()
    java.util.List<com.tenli.aiot.model.network.EventTypeGroup> groups, @org.jetbrains.annotations.Nullable()
    java.lang.String errorMessage, @org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.SettingScreenType currentScreen, @org.jetbrains.annotations.NotNull()
    java.lang.String screenTitle, @org.jetbrains.annotations.Nullable()
    com.tenli.aiot.model.network.EventTypeGroupDetail selectedGroupDetail, boolean isDetailLoading, boolean showModeSheet, @org.jetbrains.annotations.Nullable()
    java.lang.String selectedTypeKey, @org.jetbrains.annotations.Nullable()
    java.lang.String currentMode) {
        super();
    }
    
    public final boolean isLoading() {
        return false;
    }
    
    public final boolean getMasterEnabled() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.tenli.aiot.model.network.EventTypeGroup> getGroups() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getErrorMessage() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.aiot.ui.features.setting.core.SettingScreenType getCurrentScreen() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getScreenTitle() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.aiot.model.network.EventTypeGroupDetail getSelectedGroupDetail() {
        return null;
    }
    
    public final boolean isDetailLoading() {
        return false;
    }
    
    public final boolean getShowModeSheet() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getSelectedTypeKey() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getCurrentMode() {
        return null;
    }
    
    public NotifyConfigUiState() {
        super();
    }
}