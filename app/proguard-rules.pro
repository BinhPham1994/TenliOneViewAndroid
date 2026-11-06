# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Giữ thông tin dòng code và chữ ký generic cho Firebase Crashlytics & Gson/Retrofit
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature,InnerClasses,EnclosingMethod
-keepattributes *Annotation*

# Giữ nguyên toàn bộ model mạng & dữ liệu cache được serialize/deserialize bằng Gson
-keep class com.tenli.oneview.model.** { *; }
-keep class com.tenli.oneview.data.local.CachedHomeData { *; }
-keep class com.tenli.oneview.data.local.CachedEventData { *; }
-keep class com.tenli.oneview.data.local.CachedProfilesData { *; }
-keep class com.tenli.oneview.data.local.CachedMonitorData { *; }
-keep class com.tenli.oneview.data.local.SnapshotCacheItem { *; }
-keep class com.tenli.oneview.ui.features.home.TimeFilter { *; }

# Giữ nguyên các interface Retrofit API
-keep interface com.tenli.oneview.data.network.api.** { *; }

# Giữ nguyên Gson TypeToken & custom JsonDeserializer
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken
-keepclassmembers class * implements com.google.gson.JsonDeserializer { *; }

# Giữ các class gọi qua Reflection trong FcmTokenManager
-keep class com.google.android.gms.cloudmessaging.Rpc {
    public <init>(android.content.Context);
    public com.google.android.gms.tasks.Task send(android.os.Bundle);
}
-keep class com.google.firebase.installations.FirebaseInstallations {
    public static com.google.firebase.installations.FirebaseInstallations getInstance();
    public com.google.android.gms.tasks.Task getId();
}

# Bỏ qua cảnh báo không cần thiết từ OkHttp / Conscrypt
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
