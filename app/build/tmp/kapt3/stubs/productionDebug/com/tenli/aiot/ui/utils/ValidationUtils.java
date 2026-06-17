package com.tenli.aiot.ui.utils;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\u0011\n\u0002\b\n\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0016\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\u0006J\u000e\u0010\b\u001a\u00020\u00042\u0006\u0010\t\u001a\u00020\u0006J\u001f\u0010\n\u001a\u00020\u00042\u0012\u0010\u000b\u001a\n\u0012\u0006\b\u0001\u0012\u00020\u00060\f\"\u00020\u0006\u00a2\u0006\u0002\u0010\rJ\u000e\u0010\u000e\u001a\u00020\u00042\u0006\u0010\u000f\u001a\u00020\u0006J\u000e\u0010\u0010\u001a\u00020\u00042\u0006\u0010\u0011\u001a\u00020\u0006J\u000e\u0010\u0012\u001a\u00020\u00042\u0006\u0010\u0013\u001a\u00020\u0006J\u0016\u0010\u0014\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\u0006J\u000e\u0010\u0015\u001a\u00020\u00042\u0006\u0010\u0011\u001a\u00020\u0006\u00a8\u0006\u0016"}, d2 = {"Lcom/tenli/aiot/ui/utils/ValidationUtils;", "", "()V", "doPasswordsMatch", "", "pw", "", "confirm", "isIPAddress", "ip", "isNotEmpty", "values", "", "([Ljava/lang/String;)Z", "isPasswordStrong", "password", "isValidUsername", "username", "validateEmail", "email", "validatePassword", "validateUserName", "app_productionDebug"})
public final class ValidationUtils {
    @org.jetbrains.annotations.NotNull()
    public static final com.tenli.aiot.ui.utils.ValidationUtils INSTANCE = null;
    
    private ValidationUtils() {
        super();
    }
    
    public final boolean validateEmail(@org.jetbrains.annotations.NotNull()
    java.lang.String email) {
        return false;
    }
    
    public final boolean isNotEmpty(@org.jetbrains.annotations.NotNull()
    java.lang.String... values) {
        return false;
    }
    
    public final boolean validatePassword(@org.jetbrains.annotations.NotNull()
    java.lang.String pw, @org.jetbrains.annotations.NotNull()
    java.lang.String confirm) {
        return false;
    }
    
    public final boolean validateUserName(@org.jetbrains.annotations.NotNull()
    java.lang.String username) {
        return false;
    }
    
    public final boolean isIPAddress(@org.jetbrains.annotations.NotNull()
    java.lang.String ip) {
        return false;
    }
    
    public final boolean isValidUsername(@org.jetbrains.annotations.NotNull()
    java.lang.String username) {
        return false;
    }
    
    public final boolean isPasswordStrong(@org.jetbrains.annotations.NotNull()
    java.lang.String password) {
        return false;
    }
    
    public final boolean doPasswordsMatch(@org.jetbrains.annotations.NotNull()
    java.lang.String pw, @org.jetbrains.annotations.NotNull()
    java.lang.String confirm) {
        return false;
    }
}