package com.tenli.oneview.firebase;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u001a\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\u0004H\u0002J*\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u00062\u0006\u0010\u000b\u001a\u00020\u00062\b\u0010\f\u001a\u0004\u0018\u00010\u00062\u0006\u0010\u0005\u001a\u00020\u0006H\u0002J\"\u0010\r\u001a\u00020\t2\u0006\u0010\u000e\u001a\u00020\u000f2\b\u0010\f\u001a\u0004\u0018\u00010\u00062\u0006\u0010\u0010\u001a\u00020\u0004H\u0002J\u0010\u0010\u0011\u001a\u00020\t2\u0006\u0010\u0012\u001a\u00020\u0013H\u0016J*\u0010\u0014\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u00062\u0006\u0010\u000b\u001a\u00020\u00062\b\u0010\f\u001a\u0004\u0018\u00010\u00062\u0006\u0010\u0005\u001a\u00020\u0006H\u0002J*\u0010\u0015\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u00062\u0006\u0010\u000b\u001a\u00020\u00062\b\u0010\f\u001a\u0004\u0018\u00010\u00062\u0006\u0010\u0005\u001a\u00020\u0006H\u0002\u00a8\u0006\u0016"}, d2 = {"Lcom/tenli/oneview/firebase/EventMessagingService;", "Lcom/google/firebase/messaging/FirebaseMessagingService;", "()V", "getSafeNotificationId", "", "eId", "", "offset", "handleIncomingCall", "", "title", "body", "url", "loadImageAndNotify", "builder", "Landroidx/core/app/NotificationCompat$Builder;", "notificationId", "onMessageReceived", "remoteMessage", "Lcom/google/firebase/messaging/RemoteMessage;", "showMissedCallNotification", "showRegularNotification", "app_debug"})
@android.annotation.SuppressLint(value = {"MissingFirebaseInstanceTokenRefresh"})
public final class EventMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    
    public EventMessagingService() {
        super();
    }
    
    @java.lang.Override()
    public void onMessageReceived(@org.jetbrains.annotations.NotNull()
    com.google.firebase.messaging.RemoteMessage remoteMessage) {
    }
    
    private final int getSafeNotificationId(java.lang.String eId, int offset) {
        return 0;
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