package com.cardesk.home;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

/** Explicitly enabled by the user, solely for dock navigation actions. */
public final class NavigationAccessibilityService extends AccessibilityService {
    private static NavigationAccessibilityService connected;
    @Override protected void onServiceConnected() { connected = this; }
    @Override public void onDestroy() { if (connected == this) connected = null; super.onDestroy(); }
    @Override public void onAccessibilityEvent(AccessibilityEvent event) { }
    @Override public void onInterrupt() { }
    public static boolean navigate(int action) {
        return connected != null && connected.performGlobalAction(action);
    }
    public static boolean isConnected() { return connected != null; }
}
