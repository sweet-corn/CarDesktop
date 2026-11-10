package com.cardesk.home;

import android.service.notification.NotificationListenerService;

/** User-enabled notification access authorizes querying active media sessions. */
public final class MusicNotificationListener extends NotificationListenerService {
    static final String ACTION_CHANGED = "com.cardesk.home.MEDIA_SESSIONS_CHANGED";
    private void notifyHome() { sendBroadcast(new android.content.Intent(ACTION_CHANGED).setPackage(getPackageName())); }
    @Override public void onListenerConnected() { notifyHome(); }
    @Override public void onNotificationPosted(android.service.notification.StatusBarNotification notification) { notifyHome(); }
    @Override public void onNotificationRemoved(android.service.notification.StatusBarNotification notification) { notifyHome(); }
}
