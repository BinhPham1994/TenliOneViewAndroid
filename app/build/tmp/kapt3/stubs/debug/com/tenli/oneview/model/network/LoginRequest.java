package com.tenli.oneview.model.network;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\n\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b(\n\u0002\u0010\b\n\u0002\b\u0002\b\u0087\b\u0018\u00002\u00020\u0001B\u0093\u0001\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0007\u001a\u00020\u0003\u0012\b\b\u0002\u0010\b\u001a\u00020\u0003\u0012\b\b\u0002\u0010\t\u001a\u00020\u0003\u0012\b\b\u0002\u0010\n\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u000b\u001a\u00020\u0003\u0012\b\b\u0002\u0010\f\u001a\u00020\u0003\u0012\u0006\u0010\r\u001a\u00020\u000e\u0012\u0006\u0010\u000f\u001a\u00020\u0010\u0012\b\b\u0002\u0010\u0011\u001a\u00020\u0012\u0012\b\b\u0002\u0010\u0013\u001a\u00020\u0012\u0012\b\b\u0002\u0010\u0014\u001a\u00020\u0015\u00a2\u0006\u0002\u0010\u0016J\t\u0010+\u001a\u00020\u0003H\u00c6\u0003J\t\u0010,\u001a\u00020\u0003H\u00c6\u0003J\t\u0010-\u001a\u00020\u000eH\u00c6\u0003J\t\u0010.\u001a\u00020\u0010H\u00c6\u0003J\t\u0010/\u001a\u00020\u0012H\u00c6\u0003J\t\u00100\u001a\u00020\u0012H\u00c6\u0003J\t\u00101\u001a\u00020\u0015H\u00c6\u0003J\t\u00102\u001a\u00020\u0003H\u00c6\u0003J\t\u00103\u001a\u00020\u0003H\u00c6\u0003J\t\u00104\u001a\u00020\u0003H\u00c6\u0003J\t\u00105\u001a\u00020\u0003H\u00c6\u0003J\t\u00106\u001a\u00020\u0003H\u00c6\u0003J\t\u00107\u001a\u00020\u0003H\u00c6\u0003J\t\u00108\u001a\u00020\u0003H\u00c6\u0003J\t\u00109\u001a\u00020\u0003H\u00c6\u0003J\u009f\u0001\u0010:\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\u00032\b\b\u0002\u0010\b\u001a\u00020\u00032\b\b\u0002\u0010\t\u001a\u00020\u00032\b\b\u0002\u0010\n\u001a\u00020\u00032\b\b\u0002\u0010\u000b\u001a\u00020\u00032\b\b\u0002\u0010\f\u001a\u00020\u00032\b\b\u0002\u0010\r\u001a\u00020\u000e2\b\b\u0002\u0010\u000f\u001a\u00020\u00102\b\b\u0002\u0010\u0011\u001a\u00020\u00122\b\b\u0002\u0010\u0013\u001a\u00020\u00122\b\b\u0002\u0010\u0014\u001a\u00020\u0015H\u00c6\u0001J\u0013\u0010;\u001a\u00020\u00122\b\u0010<\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010=\u001a\u00020>H\u00d6\u0001J\t\u0010?\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\b\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018R\u0011\u0010\u0013\u001a\u00020\u0012\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u001aR\u0011\u0010\u0014\u001a\u00020\u0015\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u001cR\u0011\u0010\u000f\u001a\u00020\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u001eR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010\u0018R\u0011\u0010\u000b\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u0018R\u0011\u0010\t\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\u0018R\u0011\u0010\u0007\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010\u0018R\u0011\u0010\r\u001a\u00020\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010$R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010\u0018R\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b&\u0010\u0018R\u0011\u0010\f\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010\u0018R\u0011\u0010\u0011\u001a\u00020\u0012\u00a2\u0006\b\n\u0000\u001a\u0004\b(\u0010\u001aR\u0011\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010\u0018R\u0011\u0010\n\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b*\u0010\u0018\u00a8\u0006@"}, d2 = {"Lcom/tenli/oneview/model/network/LoginRequest;", "", "email", "", "password", "phoneNumber", "userIdToken", "googleIdToken", "appleTokenId", "firebaseIdToken", "verifyAuthCode", "emailVerifyAuthCode", "phoneVerifyAuthCode", "notifyConfig", "Lcom/tenli/oneview/model/network/LoginNotifyConfig;", "device", "Lcom/tenli/oneview/model/network/LoginDeviceConfig;", "saveLogin", "", "createIfNotExist", "createUserOptions", "Lcom/tenli/oneview/model/network/CreateUserOptions;", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/tenli/oneview/model/network/LoginNotifyConfig;Lcom/tenli/oneview/model/network/LoginDeviceConfig;ZZLcom/tenli/oneview/model/network/CreateUserOptions;)V", "getAppleTokenId", "()Ljava/lang/String;", "getCreateIfNotExist", "()Z", "getCreateUserOptions", "()Lcom/tenli/oneview/model/network/CreateUserOptions;", "getDevice", "()Lcom/tenli/oneview/model/network/LoginDeviceConfig;", "getEmail", "getEmailVerifyAuthCode", "getFirebaseIdToken", "getGoogleIdToken", "getNotifyConfig", "()Lcom/tenli/oneview/model/network/LoginNotifyConfig;", "getPassword", "getPhoneNumber", "getPhoneVerifyAuthCode", "getSaveLogin", "getUserIdToken", "getVerifyAuthCode", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "hashCode", "", "toString", "app_debug"})
public final class LoginRequest {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String email = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String password = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String phoneNumber = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String userIdToken = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String googleIdToken = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String appleTokenId = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String firebaseIdToken = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String verifyAuthCode = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String emailVerifyAuthCode = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String phoneVerifyAuthCode = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.oneview.model.network.LoginNotifyConfig notifyConfig = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.oneview.model.network.LoginDeviceConfig device = null;
    private final boolean saveLogin = false;
    private final boolean createIfNotExist = false;
    @org.jetbrains.annotations.NotNull()
    private final com.tenli.oneview.model.network.CreateUserOptions createUserOptions = null;
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component10() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.oneview.model.network.LoginNotifyConfig component11() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.oneview.model.network.LoginDeviceConfig component12() {
        return null;
    }
    
    public final boolean component13() {
        return false;
    }
    
    public final boolean component14() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.oneview.model.network.CreateUserOptions component15() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component6() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component7() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component8() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.oneview.model.network.LoginRequest copy(@org.jetbrains.annotations.NotNull()
    java.lang.String email, @org.jetbrains.annotations.NotNull()
    java.lang.String password, @org.jetbrains.annotations.NotNull()
    java.lang.String phoneNumber, @org.jetbrains.annotations.NotNull()
    java.lang.String userIdToken, @org.jetbrains.annotations.NotNull()
    java.lang.String googleIdToken, @org.jetbrains.annotations.NotNull()
    java.lang.String appleTokenId, @org.jetbrains.annotations.NotNull()
    java.lang.String firebaseIdToken, @org.jetbrains.annotations.NotNull()
    java.lang.String verifyAuthCode, @org.jetbrains.annotations.NotNull()
    java.lang.String emailVerifyAuthCode, @org.jetbrains.annotations.NotNull()
    java.lang.String phoneVerifyAuthCode, @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.LoginNotifyConfig notifyConfig, @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.LoginDeviceConfig device, boolean saveLogin, boolean createIfNotExist, @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.CreateUserOptions createUserOptions) {
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
    
    public LoginRequest(@org.jetbrains.annotations.NotNull()
    java.lang.String email, @org.jetbrains.annotations.NotNull()
    java.lang.String password, @org.jetbrains.annotations.NotNull()
    java.lang.String phoneNumber, @org.jetbrains.annotations.NotNull()
    java.lang.String userIdToken, @org.jetbrains.annotations.NotNull()
    java.lang.String googleIdToken, @org.jetbrains.annotations.NotNull()
    java.lang.String appleTokenId, @org.jetbrains.annotations.NotNull()
    java.lang.String firebaseIdToken, @org.jetbrains.annotations.NotNull()
    java.lang.String verifyAuthCode, @org.jetbrains.annotations.NotNull()
    java.lang.String emailVerifyAuthCode, @org.jetbrains.annotations.NotNull()
    java.lang.String phoneVerifyAuthCode, @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.LoginNotifyConfig notifyConfig, @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.LoginDeviceConfig device, boolean saveLogin, boolean createIfNotExist, @org.jetbrains.annotations.NotNull()
    com.tenli.oneview.model.network.CreateUserOptions createUserOptions) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getEmail() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getPassword() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getPhoneNumber() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getUserIdToken() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getGoogleIdToken() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getAppleTokenId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getFirebaseIdToken() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getVerifyAuthCode() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getEmailVerifyAuthCode() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getPhoneVerifyAuthCode() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.oneview.model.network.LoginNotifyConfig getNotifyConfig() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.oneview.model.network.LoginDeviceConfig getDevice() {
        return null;
    }
    
    public final boolean getSaveLogin() {
        return false;
    }
    
    public final boolean getCreateIfNotExist() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tenli.oneview.model.network.CreateUserOptions getCreateUserOptions() {
        return null;
    }
}