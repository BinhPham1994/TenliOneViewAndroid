package com.tenli.oneview.ui.utils;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u000e\u0010\u0007\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u000e\u0010\b\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u000e\u0010\t\u001a\u00020\n2\u0006\u0010\u0005\u001a\u00020\u0006\u00a8\u0006\u000b"}, d2 = {"Lcom/tenli/oneview/ui/utils/PermissionUtils;", "", "()V", "isCallReady", "", "context", "Landroid/content/Context;", "isIgnoringBattery", "isNotificationEnabled", "openManufacturerPermissions", "", "app_debug"})
public final class PermissionUtils {
    @org.jetbrains.annotations.NotNull()
    public static final com.tenli.oneview.ui.utils.PermissionUtils INSTANCE = null;
    
    private PermissionUtils() {
        super();
    }
    
    /**
     * Kiểm tra App đã được đưa vào danh sách "Không tối ưu hóa pin" chưa.
     * Trả về true = Đã sẵn sàng chạy ngầm.
     */
    public final boolean isIgnoringBattery(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
    
    /**
     * Kiểm tra quyền Thông báo hệ thống.
     */
    public final boolean isNotificationEnabled(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
    
    /**
     * Tổng hợp tất cả các quyền để biết App có thể nhận Call 100% hay chưa.
     */
    public final boolean isCallReady(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
    
    /**
     * Mở cài đặt quyền đặc biệt cho các máy Trung Quốc (Xiaomi, Oppo, Vivo).
     */
    public final void openManufacturerPermissions(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
}