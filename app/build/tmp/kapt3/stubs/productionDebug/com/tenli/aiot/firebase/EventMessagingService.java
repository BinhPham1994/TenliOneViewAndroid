package com.tenli.aiot.firebase;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J*\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\u00062\b\u0010\b\u001a\u0004\u0018\u00010\u00062\u0006\u0010\t\u001a\u00020\u0006H\u0002J\"\u0010\n\u001a\u00020\u00042\u0006\u0010\u000b\u001a\u00020\f2\b\u0010\b\u001a\u0004\u0018\u00010\u00062\u0006\u0010\r\u001a\u00020\u000eH\u0002J\u0010\u0010\u000f\u001a\u00020\u00042\u0006\u0010\u0010\u001a\u00020\u0011H\u0016J*\u0010\u0012\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\u00062\b\u0010\b\u001a\u0004\u0018\u00010\u00062\u0006\u0010\t\u001a\u00020\u0006H\u0002J*\u0010\u0013\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\u00062\b\u0010\b\u001a\u0004\u0018\u00010\u00062\u0006\u0010\t\u001a\u00020\u0006H\u0002\u00a8\u0006\u0014"}, d2 = {"Lcom/tenli/aiot/firebase/EventMessagingService;", "Lcom/google/firebase/messaging/FirebaseMessagingService;", "()V", "handleIncomingCall", "", "title", "", "body", "url", "eId", "loadImageAndNotify", "builder", "Landroidx/core/app/NotificationCompat$Builder;", "notificationId", "", "onMessageReceived", "remoteMessage", "Lcom/google/firebase/messaging/RemoteMessage;", "showMissedCallNotification", "showRegularNotification", "app_productionDebug"})
@android.annotation.SuppressLint(value = {"MissingFirebaseInstanceTokenRefresh"})
public final class EventMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    
    public EventMessagingService() {
        super();
    }
    
    @java.lang.Override()
    public void onMessageReceived(@org.jetbrains.annotations.NotNull()
    com.google.firebase.messaging.RemoteMessage remoteMessage) {
    }
    
    private final void showMissedCallNotification(java.lang.String title, java.lang.String body, java.lang.String url, java.lang.String eId) {
    }
    
    private final void loadImageAndNotify(androidx.core.app.NotificationCompat.Builder builder, java.lang.String url, int notificationId) {
    }
    
    private final void handleIncomingCall(java.lang.String title, java.lang.String body, java.lang.String url, java.lang.String eId) {
    }
    
    private final void showRegularNotification(java.lang.String title, java.lang.String body, java.lang.String url, java.lang.String eId) {
    }
}