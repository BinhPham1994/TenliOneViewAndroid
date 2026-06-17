package com.tenli.aiot.ui.features.setting.core;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\"\n\u0002\u0010\b\n\u0002\b\u0002\b\u0087\b\u0018\u00002\u00020\u0001B\u007f\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u0012\b\b\u0002\u0010\b\u001a\u00020\t\u0012\b\b\u0002\u0010\n\u001a\u00020\t\u0012\b\b\u0002\u0010\u000b\u001a\u00020\t\u0012\b\b\u0002\u0010\f\u001a\u00020\u0003\u0012\b\b\u0002\u0010\r\u001a\u00020\t\u0012\b\b\u0002\u0010\u000e\u001a\u00020\u000f\u0012\b\b\u0002\u0010\u0010\u001a\u00020\u0011\u0012\b\b\u0002\u0010\u0012\u001a\u00020\u0013\u0012\b\b\u0002\u0010\u0014\u001a\u00020\u0015\u00a2\u0006\u0002\u0010\u0016J\t\u0010(\u001a\u00020\u0003H\u00c6\u0003J\t\u0010)\u001a\u00020\u0011H\u00c6\u0003J\t\u0010*\u001a\u00020\u0013H\u00c6\u0003J\t\u0010+\u001a\u00020\u0015H\u00c6\u0003J\t\u0010,\u001a\u00020\u0005H\u00c6\u0003J\u000b\u0010-\u001a\u0004\u0018\u00010\u0007H\u00c6\u0003J\t\u0010.\u001a\u00020\tH\u00c6\u0003J\t\u0010/\u001a\u00020\tH\u00c6\u0003J\t\u00100\u001a\u00020\tH\u00c6\u0003J\t\u00101\u001a\u00020\u0003H\u00c6\u0003J\t\u00102\u001a\u00020\tH\u00c6\u0003J\t\u00103\u001a\u00020\u000fH\u00c6\u0003J\u0083\u0001\u00104\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u00072\b\b\u0002\u0010\b\u001a\u00020\t2\b\b\u0002\u0010\n\u001a\u00020\t2\b\b\u0002\u0010\u000b\u001a\u00020\t2\b\b\u0002\u0010\f\u001a\u00020\u00032\b\b\u0002\u0010\r\u001a\u00020\t2\b\b\u0002\u0010\u000e\u001a\u00020\u000f2\b\b\u0002\u0010\u0010\u001a\u00020\u00112\b\b\u0002\u0010\u0012\u001a\u00020\u00132\b\b\u0002\u0010\u0014\u001a\u00020\u0015H\u00c6\u0001J\u0013\u00105\u001a\u00020\t2\b\u00106\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u00107\u001a\u000208H\u00d6\u0001J\t\u00109\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u000e\u001a\u00020\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018R\u0011\u0010\u0014\u001a\u00020\u0015\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u001aR\u0011\u0010\u0010\u001a\u00020\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u001cR\u0011\u0010\f\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u001eR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010 R\u0011\u0010\u0012\u001a\u00020\u0013\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\"R\u0011\u0010\r\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010$R\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010$R\u0011\u0010\u000b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010$R\u0011\u0010\n\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010$R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010\u001eR\u0013\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b&\u0010\'\u00a8\u0006:"}, d2 = {"Lcom/tenli/aiot/ui/features/setting/core/SettingUiState;", "", "title", "", "currentScreen", "Lcom/tenli/aiot/ui/features/setting/core/SettingScreenType;", "userData", "Lcom/tenli/aiot/model/network/UserData;", "isLoading", "", "isLogoutDialogOpen", "isLoggingOut", "currentLanguage", "hasDeviceIssue", "account", "Lcom/tenli/aiot/ui/features/setting/core/AccountUiState;", "client", "Lcom/tenli/aiot/ui/features/setting/core/ClientUiState;", "group", "Lcom/tenli/aiot/ui/features/setting/core/GroupUiState;", "box", "Lcom/tenli/aiot/ui/features/setting/core/BoxUiState;", "(Ljava/lang/String;Lcom/tenli/aiot/ui/features/setting/core/SettingScreenType;Lcom/tenli/aiot/model/network/UserData;ZZZLjava/lang/String;ZLcom/tenli/aiot/ui/features/setting/core/AccountUiState;Lcom/tenli/aiot/ui/features/setting/core/ClientUiState;Lcom/tenli/aiot/ui/features/setting/core/GroupUiState;Lcom/tenli/aiot/ui/features/setting/core/BoxUiState;)V", "getAccount", "()Lcom/tenli/aiot/ui/features/setting/core/AccountUiState;", "getBox", "()Lcom/tenli/aiot/ui/features/setting/core/BoxUiState;", "getClient", "()Lcom/tenli/aiot/ui/features/setting/core/ClientUiState;", "getCurrentLanguage", "()Ljava/lang/String;", "getCurrentScreen", "()Lcom/tenli/aiot/ui/features/setting/core/SettingScreenType;", "getGroup", "()Lcom/tenli/aiot/ui/features/setting/core/GroupUiState;", "getHasDeviceIssue", "()Z", "getTitle", "getUserData", "()Lcom/tenli/aiot/model/network/UserData;", "component1", "component10", "component11", "component12", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "hashCode", "", "toString", "app_productionDebug"})
public final class SettingUiState {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String title = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.aiot.ui.features.setting.core.SettingScreenType currentScreen = null;
    @org.jetbrains.annotations.Nullable()
    private final com.tenli.aiot.model.network.UserData userData = null;
    private final boolean isLoading = false;
    private final boolean isLogoutDialogOpen = false;
    private final boolean isLoggingOut = false;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String currentLanguage = null;
    private final boolean hasDeviceIssue = false;
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.aiot.ui.features.setting.core.AccountUiState account = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.aiot.ui.features.setting.core.ClientUiState client = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.aiot.ui.features.setting.core.GroupUiState group = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.aiot.ui.features.setting.core.BoxUiState box = null;
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.aiot.ui.features.setting.core.ClientUiState component10() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.aiot.ui.features.setting.core.GroupUiState component11() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.aiot.ui.features.setting.core.BoxUiState component12() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.aiot.ui.features.setting.core.SettingScreenType component2() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.aiot.model.network.UserData component3() {
        return null;
    }
    
    public final boolean component4() {
        return false;
    }
    
    public final boolean component5() {
        return false;
    }
    
    public final boolean component6() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component7() {
        return null;
    }
    
    public final boolean component8() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.aiot.ui.features.setting.core.AccountUiState component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.aiot.ui.features.setting.core.SettingUiState copy(@org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.SettingScreenType currentScreen, @org.jetbrains.annotations.Nullable()
    com.tenli.aiot.model.network.UserData userData, boolean isLoading, boolean isLogoutDialogOpen, boolean isLoggingOut, @org.jetbrains.annotations.NotNull()
    java.lang.String currentLanguage, boolean hasDeviceIssue, @org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.AccountUiState account, @org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.ClientUiState client, @org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.GroupUiState group, @org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.BoxUiState box) {
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
    
    public SettingUiState(@org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.SettingScreenType currentScreen, @org.jetbrains.annotations.Nullable()
    com.tenli.aiot.model.network.UserData userData, boolean isLoading, boolean isLogoutDialogOpen, boolean isLoggingOut, @org.jetbrains.annotations.NotNull()
    java.lang.String currentLanguage, boolean hasDeviceIssue, @org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.AccountUiState account, @org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.ClientUiState client, @org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.GroupUiState group, @org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.BoxUiState box) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getTitle() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.aiot.ui.features.setting.core.SettingScreenType getCurrentScreen() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.aiot.model.network.UserData getUserData() {
        return null;
    }
    
    public final boolean isLoading() {
        return false;
    }
    
    public final boolean isLogoutDialogOpen() {
        return false;
    }
    
    public final boolean isLoggingOut() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getCurrentLanguage() {
        return null;
    }
    
    public final boolean getHasDeviceIssue() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.aiot.ui.features.setting.core.AccountUiState getAccount() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.aiot.ui.features.setting.core.ClientUiState getClient() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.aiot.ui.features.setting.core.GroupUiState getGroup() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.aiot.ui.features.setting.core.BoxUiState getBox() {
        return null;
    }
    
    public SettingUiState() {
        super();
    }
}