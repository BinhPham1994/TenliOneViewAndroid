package com.tenli.oneview.ui.features.setting.core;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b*\b\u0087\b\u0018\u00002\u00020\u0001B\u009f\u0001\u0012\u000e\b\u0002\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u0012\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u0004\u0012\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u0012\b\b\u0002\u0010\b\u001a\u00020\t\u0012\b\b\u0002\u0010\n\u001a\u00020\u000b\u0012\b\b\u0002\u0010\f\u001a\u00020\u000b\u0012\b\b\u0002\u0010\r\u001a\u00020\t\u0012\b\b\u0002\u0010\u000e\u001a\u00020\t\u0012\b\b\u0002\u0010\u000f\u001a\u00020\u000b\u0012\b\b\u0002\u0010\u0010\u001a\u00020\u0011\u0012\b\b\u0002\u0010\u0012\u001a\u00020\t\u0012\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\u0014\u0012\n\b\u0002\u0010\u0015\u001a\u0004\u0018\u00010\u0011\u0012\b\b\u0002\u0010\u0016\u001a\u00020\t\u00a2\u0006\u0002\u0010\u0017J\u000f\u0010*\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\u00c6\u0003J\t\u0010+\u001a\u00020\u0011H\u00c6\u0003J\t\u0010,\u001a\u00020\tH\u00c6\u0003J\u000b\u0010-\u001a\u0004\u0018\u00010\u0014H\u00c6\u0003J\u0010\u0010.\u001a\u0004\u0018\u00010\u0011H\u00c6\u0003\u00a2\u0006\u0002\u0010 J\t\u0010/\u001a\u00020\tH\u00c6\u0003J\u000b\u00100\u001a\u0004\u0018\u00010\u0004H\u00c6\u0003J\u000b\u00101\u001a\u0004\u0018\u00010\u0007H\u00c6\u0003J\t\u00102\u001a\u00020\tH\u00c6\u0003J\t\u00103\u001a\u00020\u000bH\u00c6\u0003J\t\u00104\u001a\u00020\u000bH\u00c6\u0003J\t\u00105\u001a\u00020\tH\u00c6\u0003J\t\u00106\u001a\u00020\tH\u00c6\u0003J\t\u00107\u001a\u00020\u000bH\u00c6\u0003J\u00a8\u0001\u00108\u001a\u00020\u00002\u000e\b\u0002\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u00042\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u00072\b\b\u0002\u0010\b\u001a\u00020\t2\b\b\u0002\u0010\n\u001a\u00020\u000b2\b\b\u0002\u0010\f\u001a\u00020\u000b2\b\b\u0002\u0010\r\u001a\u00020\t2\b\b\u0002\u0010\u000e\u001a\u00020\t2\b\b\u0002\u0010\u000f\u001a\u00020\u000b2\b\b\u0002\u0010\u0010\u001a\u00020\u00112\b\b\u0002\u0010\u0012\u001a\u00020\t2\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\u00142\n\b\u0002\u0010\u0015\u001a\u0004\u0018\u00010\u00112\b\b\u0002\u0010\u0016\u001a\u00020\tH\u00c6\u0001\u00a2\u0006\u0002\u00109J\u0013\u0010:\u001a\u00020\t2\b\u0010;\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010<\u001a\u00020\u0011H\u00d6\u0001J\t\u0010=\u001a\u00020\u000bH\u00d6\u0001R\u0017\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u0013\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u001bR\u0011\u0010\u0012\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u001cR\u0011\u0010\u0016\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u001cR\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\u001cR\u0011\u0010\r\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u001cR\u0011\u0010\u000e\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u001cR\u0013\u0010\u0013\u001a\u0004\u0018\u00010\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u001eR\u0015\u0010\u0015\u001a\u0004\u0018\u00010\u0011\u00a2\u0006\n\n\u0002\u0010!\u001a\u0004\b\u001f\u0010 R\u0013\u0010\u0005\u001a\u0004\u0018\u00010\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010#R\u0011\u0010\u000f\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010%R\u0011\u0010\u0010\u001a\u00020\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b&\u0010\'R\u0011\u0010\n\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b(\u0010%R\u0011\u0010\f\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010%\u00a8\u0006>"}, d2 = {"Lcom/tenli/oneview/ui/features/setting/core/GroupUiState;", "", "displayGroups", "", "Lcom/tenli/oneview/model/network/HomeGroupDisplay;", "selectedGroup", "groupDetail", "Lcom/tenli/oneview/model/network/GroupDetailData;", "isGroupMenuExpanded", "", "tempGroupName", "", "tempJoinCode", "isMemberMenuExpanded", "isShareCodeDialogOpen", "shareCode", "shareCodeExpireTime", "", "isConfirmDeleteMemberOpen", "memberToDelete", "Lcom/tenli/oneview/model/network/GroupUser;", "selectedExpiryMinutes", "isConfirmGroupActionOpen", "(Ljava/util/List;Lcom/tenli/oneview/model/network/HomeGroupDisplay;Lcom/tenli/oneview/model/network/GroupDetailData;ZLjava/lang/String;Ljava/lang/String;ZZLjava/lang/String;IZLcom/tenli/oneview/model/network/GroupUser;Ljava/lang/Integer;Z)V", "getDisplayGroups", "()Ljava/util/List;", "getGroupDetail", "()Lcom/tenli/oneview/model/network/GroupDetailData;", "()Z", "getMemberToDelete", "()Lcom/tenli/oneview/model/network/GroupUser;", "getSelectedExpiryMinutes", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getSelectedGroup", "()Lcom/tenli/oneview/model/network/HomeGroupDisplay;", "getShareCode", "()Ljava/lang/String;", "getShareCodeExpireTime", "()I", "getTempGroupName", "getTempJoinCode", "component1", "component10", "component11", "component12", "component13", "component14", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(Ljava/util/List;Lcom/tenli/oneview/model/network/HomeGroupDisplay;Lcom/tenli/oneview/model/network/GroupDetailData;ZLjava/lang/String;Ljava/lang/String;ZZLjava/lang/String;IZLcom/tenli/oneview/model/network/GroupUser;Ljava/lang/Integer;Z)Lcom/tenli/oneview/ui/features/setting/core/GroupUiState;", "equals", "other", "hashCode", "toString", "app_debug"})
public final class GroupUiState {
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.tenli.oneview.model.network.HomeGroupDisplay> displayGroups = null;
    @org.jetbrains.annotations.Nullable()
    private final com.tenli.oneview.model.network.HomeGroupDisplay selectedGroup = null;
    @org.jetbrains.annotations.Nullable()
    private final com.tenli.oneview.model.network.GroupDetailData groupDetail = null;
    private final boolean isGroupMenuExpanded = false;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String tempGroupName = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String tempJoinCode = null;
    private final boolean isMemberMenuExpanded = false;
    private final boolean isShareCodeDialogOpen = false;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String shareCode = null;
    private final int shareCodeExpireTime = 0;
    private final boolean isConfirmDeleteMemberOpen = false;
    @org.jetbrains.annotations.Nullable()
    private final com.tenli.oneview.model.network.GroupUser memberToDelete = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer selectedExpiryMinutes = null;
    private final boolean isConfirmGroupActionOpen = false;
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.tenli.oneview.model.network.HomeGroupDisplay> component1() {
        return null;
    }
    
    public final int component10() {
        return 0;
    }
    
    public final boolean component11() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.oneview.model.network.GroupUser component12() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component13() {
        return null;
    }
    
    public final boolean component14() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.oneview.model.network.HomeGroupDisplay component2() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.oneview.model.network.GroupDetailData component3() {
        return null;
    }
    
    public final boolean component4() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component6() {
        return null;
    }
    
    public final boolean component7() {
        return false;
    }
    
    public final boolean component8() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.oneview.ui.features.setting.core.GroupUiState copy(@org.jetbrains.annotations.NotNull()
    java.util.List<com.tenli.oneview.model.network.HomeGroupDisplay> displayGroups, @org.jetbrains.annotations.Nullable()
    com.tenli.oneview.model.network.HomeGroupDisplay selectedGroup, @org.jetbrains.annotations.Nullable()
    com.tenli.oneview.model.network.GroupDetailData groupDetail, boolean isGroupMenuExpanded, @org.jetbrains.annotations.NotNull()
    java.lang.String tempGroupName, @org.jetbrains.annotations.NotNull()
    java.lang.String tempJoinCode, boolean isMemberMenuExpanded, boolean isShareCodeDialogOpen, @org.jetbrains.annotations.NotNull()
    java.lang.String shareCode, int shareCodeExpireTime, boolean isConfirmDeleteMemberOpen, @org.jetbrains.annotations.Nullable()
    com.tenli.oneview.model.network.GroupUser memberToDelete, @org.jetbrains.annotations.Nullable()
    java.lang.Integer selectedExpiryMinutes, boolean isConfirmGroupActionOpen) {
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
    
    public GroupUiState(@org.jetbrains.annotations.NotNull()
    java.util.List<com.tenli.oneview.model.network.HomeGroupDisplay> displayGroups, @org.jetbrains.annotations.Nullable()
    com.tenli.oneview.model.network.HomeGroupDisplay selectedGroup, @org.jetbrains.annotations.Nullable()
    com.tenli.oneview.model.network.GroupDetailData groupDetail, boolean isGroupMenuExpanded, @org.jetbrains.annotations.NotNull()
    java.lang.String tempGroupName, @org.jetbrains.annotations.NotNull()
    java.lang.String tempJoinCode, boolean isMemberMenuExpanded, boolean isShareCodeDialogOpen, @org.jetbrains.annotations.NotNull()
    java.lang.String shareCode, int shareCodeExpireTime, boolean isConfirmDeleteMemberOpen, @org.jetbrains.annotations.Nullable()
    com.tenli.oneview.model.network.GroupUser memberToDelete, @org.jetbrains.annotations.Nullable()
    java.lang.Integer selectedExpiryMinutes, boolean isConfirmGroupActionOpen) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.tenli.oneview.model.network.HomeGroupDisplay> getDisplayGroups() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.oneview.model.network.HomeGroupDisplay getSelectedGroup() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.oneview.model.network.GroupDetailData getGroupDetail() {
        return null;
    }
    
    public final boolean isGroupMenuExpanded() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getTempGroupName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getTempJoinCode() {
        return null;
    }
    
    public final boolean isMemberMenuExpanded() {
        return false;
    }
    
    public final boolean isShareCodeDialogOpen() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getShareCode() {
        return null;
    }
    
    public final int getShareCodeExpireTime() {
        return 0;
    }
    
    public final boolean isConfirmDeleteMemberOpen() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tenli.oneview.model.network.GroupUser getMemberToDelete() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getSelectedExpiryMinutes() {
        return null;
    }
    
    public final boolean isConfirmGroupActionOpen() {
        return false;
    }
    
    public GroupUiState() {
        super();
    }
}