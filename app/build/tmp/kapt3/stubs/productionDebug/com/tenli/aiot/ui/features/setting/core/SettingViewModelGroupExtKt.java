package com.tenli.aiot.ui.features.setting.core;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000>\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0005\u001a\n\u0010\u0000\u001a\u00020\u0001*\u00020\u0002\u001a\n\u0010\u0003\u001a\u00020\u0001*\u00020\u0002\u001a\u0018\u0010\u0004\u001a\u00020\u0001*\u00020\u00022\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00010\u0006\u001a\u0012\u0010\u0007\u001a\u00020\u0001*\u00020\u00022\u0006\u0010\b\u001a\u00020\t\u001a\u0012\u0010\n\u001a\u00020\u0001*\u00020\u00022\u0006\u0010\u000b\u001a\u00020\f\u001a\n\u0010\r\u001a\u00020\u0001*\u00020\u0002\u001a\u0018\u0010\u000e\u001a\u00020\u0001*\u00020\u00022\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00010\u0006\u001a\u0018\u0010\u000f\u001a\u00020\u0001*\u00020\u00022\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00010\u0006\u001a\"\u0010\u0010\u001a\u00020\u0001*\u00020\u00022\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\t2\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\t\u001a\n\u0010\u0013\u001a\u00020\u0001*\u00020\u0002\u001a\u0012\u0010\u0014\u001a\u00020\u0001*\u00020\u00022\u0006\u0010\u0015\u001a\u00020\u0016\u001a\u0019\u0010\u0017\u001a\u00020\u0001*\u00020\u00022\b\u0010\u0018\u001a\u0004\u0018\u00010\u0016\u00a2\u0006\u0002\u0010\u0019\u001a\u0014\u0010\u001a\u001a\u00020\u0001*\u00020\u00022\b\u0010\u001b\u001a\u0004\u0018\u00010\u001c\u001a\f\u0010\u001d\u001a\u00020\u0001*\u00020\u0002H\u0000\u001a\u0012\u0010\u001e\u001a\u00020\u0001*\u00020\u00022\u0006\u0010\u001f\u001a\u00020 \u001a\u0012\u0010!\u001a\u00020\u0001*\u00020\u00022\u0006\u0010\"\u001a\u00020 \u001a\u0012\u0010#\u001a\u00020\u0001*\u00020\u00022\u0006\u0010\"\u001a\u00020 \u001a\u0018\u0010$\u001a\u00020\u0001*\u00020\u00022\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00010\u0006\u00a8\u0006%"}, d2 = {"closeShareCodeDialog", "", "Lcom/tenli/aiot/ui/features/setting/core/SettingViewModel;", "confirmDeleteMember", "createGroup", "onSuccess", "Lkotlin/Function0;", "createShareCode", "role", "", "fetchGroupDetail", "groupDisplay", "Lcom/tenli/aiot/model/network/HomeGroupDisplay;", "fetchGroups", "handleGroupAction", "joinGroup", "onGroupFieldChange", "name", "code", "prepareEditGroupName", "removeMember", "userId", "", "setExpiryTime", "minutes", "(Lcom/tenli/aiot/ui/features/setting/core/SettingViewModel;Ljava/lang/Integer;)V", "showConfirmDeleteMember", "member", "Lcom/tenli/aiot/model/network/GroupUser;", "startShareCodeTimer", "toggleGroupActionConfirm", "isOpen", "", "toggleGroupMenu", "show", "toggleMemberMenu", "updateGroupName", "app_productionDebug"})
public final class SettingViewModelGroupExtKt {
    
    public static final void fetchGroups(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.SettingViewModel $this$fetchGroups) {
    }
    
    public static final void fetchGroupDetail(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.SettingViewModel $this$fetchGroupDetail, @org.jetbrains.annotations.NotNull()
    com.tenli.aiot.model.network.HomeGroupDisplay groupDisplay) {
    }
    
    public static final void onGroupFieldChange(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.SettingViewModel $this$onGroupFieldChange, @org.jetbrains.annotations.Nullable()
    java.lang.String name, @org.jetbrains.annotations.Nullable()
    java.lang.String code) {
    }
    
    public static final void createGroup(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.SettingViewModel $this$createGroup, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onSuccess) {
    }
    
    public static final void joinGroup(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.SettingViewModel $this$joinGroup, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onSuccess) {
    }
    
    public static final void updateGroupName(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.SettingViewModel $this$updateGroupName, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onSuccess) {
    }
    
    public static final void createShareCode(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.SettingViewModel $this$createShareCode, @org.jetbrains.annotations.NotNull()
    java.lang.String role) {
    }
    
    public static final void startShareCodeTimer(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.SettingViewModel $this$startShareCodeTimer) {
    }
    
    public static final void closeShareCodeDialog(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.SettingViewModel $this$closeShareCodeDialog) {
    }
    
    public static final void removeMember(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.SettingViewModel $this$removeMember, int userId) {
    }
    
    public static final void confirmDeleteMember(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.SettingViewModel $this$confirmDeleteMember) {
    }
    
    public static final void toggleGroupMenu(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.SettingViewModel $this$toggleGroupMenu, boolean show) {
    }
    
    public static final void prepareEditGroupName(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.SettingViewModel $this$prepareEditGroupName) {
    }
    
    public static final void toggleMemberMenu(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.SettingViewModel $this$toggleMemberMenu, boolean show) {
    }
    
    public static final void setExpiryTime(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.SettingViewModel $this$setExpiryTime, @org.jetbrains.annotations.Nullable()
    java.lang.Integer minutes) {
    }
    
    public static final void showConfirmDeleteMember(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.SettingViewModel $this$showConfirmDeleteMember, @org.jetbrains.annotations.Nullable()
    com.tenli.aiot.model.network.GroupUser member) {
    }
    
    public static final void handleGroupAction(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.SettingViewModel $this$handleGroupAction, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onSuccess) {
    }
    
    public static final void toggleGroupActionConfirm(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.SettingViewModel $this$toggleGroupActionConfirm, boolean isOpen) {
    }
}