package com.tenli.aiot.ui.features.setting.core;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u00002\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u000e\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\u001a\u0018\u0010\u0000\u001a\u00020\u0001*\u00020\u00022\f\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00010\u0004\u001a\n\u0010\u0005\u001a\u00020\u0001*\u00020\u0002\u001a\n\u0010\u0006\u001a\u00020\u0001*\u00020\u0002\u001a\u0012\u0010\u0007\u001a\u00020\u0001*\u00020\u00022\u0006\u0010\b\u001a\u00020\t\u001a:\u0010\n\u001a\u00020\u0001*\u00020\u00022\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\f2\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\f2\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\f2\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\f\u001a\u0012\u0010\u0010\u001a\u00020\u0001*\u00020\u00022\u0006\u0010\u0011\u001a\u00020\t\u001a.\u0010\u0012\u001a\u00020\u0001*\u00020\u00022\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\f2\n\b\u0002\u0010\u0014\u001a\u0004\u0018\u00010\f2\n\b\u0002\u0010\u0015\u001a\u0004\u0018\u00010\f\u001a\n\u0010\u0016\u001a\u00020\u0001*\u00020\u0002\u001a\n\u0010\u0017\u001a\u00020\u0001*\u00020\u0002\u001a\u0018\u0010\u0018\u001a\u00020\u0001*\u00020\u00022\f\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00010\u0004\u001a\u0012\u0010\u0019\u001a\u00020\u0001*\u00020\u00022\u0006\u0010\u001a\u001a\u00020\u001b\u001a\u0012\u0010\u001c\u001a\u00020\u0001*\u00020\u00022\u0006\u0010\u001d\u001a\u00020\u001e\u00a8\u0006\u001f"}, d2 = {"changePassword", "", "Lcom/tenli/aiot/ui/features/setting/core/SettingViewModel;", "onSuccess", "Lkotlin/Function0;", "clearAccountTempData", "fetchClientDevices", "logoutRemoteClient", "deviceId", "", "onAccountFieldChange", "name", "", "email", "phone", "address", "onGenderSelect", "gender", "onPasswordChange", "old", "new", "confirm", "prepareChangePassword", "prepareEditProfile", "updateUserProfile", "uploadAvatar", "uri", "Landroid/net/Uri;", "viewClientDetail", "device", "Lcom/tenli/aiot/model/network/ClientDevice;", "app_productionDebug"})
public final class SettingViewModelAccountExtKt {
    
    /**
     * Extension xử lý các tác vụ liên quan đến Tài khoản & Bảo mật
     */
    public static final void prepareEditProfile(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.SettingViewModel $this$prepareEditProfile) {
    }
    
    public static final void onAccountFieldChange(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.SettingViewModel $this$onAccountFieldChange, @org.jetbrains.annotations.Nullable()
    java.lang.String name, @org.jetbrains.annotations.Nullable()
    java.lang.String email, @org.jetbrains.annotations.Nullable()
    java.lang.String phone, @org.jetbrains.annotations.Nullable()
    java.lang.String address) {
    }
    
    public static final void onGenderSelect(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.SettingViewModel $this$onGenderSelect, int gender) {
    }
    
    public static final void updateUserProfile(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.SettingViewModel $this$updateUserProfile, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onSuccess) {
    }
    
    public static final void uploadAvatar(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.SettingViewModel $this$uploadAvatar, @org.jetbrains.annotations.NotNull()
    android.net.Uri uri) {
    }
    
    public static final void onPasswordChange(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.SettingViewModel $this$onPasswordChange, @org.jetbrains.annotations.Nullable()
    java.lang.String old, @org.jetbrains.annotations.Nullable()
    java.lang.String p2_54480, @org.jetbrains.annotations.Nullable()
    java.lang.String confirm) {
    }
    
    public static final void changePassword(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.SettingViewModel $this$changePassword, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onSuccess) {
    }
    
    /**
     * Reset các trường mật khẩu về trống trước khi mở màn hình Đổi mật khẩu
     */
    public static final void prepareChangePassword(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.SettingViewModel $this$prepareChangePassword) {
    }
    
    /**
     * Dọn dẹp toàn bộ dữ liệu tạm trong ngăn Account
     * (Nên gọi khi thoát khỏi module Cài đặt để tránh rác dữ liệu)
     */
    public static final void clearAccountTempData(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.SettingViewModel $this$clearAccountTempData) {
    }
    
    public static final void fetchClientDevices(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.SettingViewModel $this$fetchClientDevices) {
    }
    
    public static final void viewClientDetail(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.SettingViewModel $this$viewClientDetail, @org.jetbrains.annotations.NotNull()
    com.tenli.aiot.model.network.ClientDevice device) {
    }
    
    public static final void logoutRemoteClient(@org.jetbrains.annotations.NotNull()
    com.tenli.aiot.ui.features.setting.core.SettingViewModel $this$logoutRemoteClient, int deviceId) {
    }
}