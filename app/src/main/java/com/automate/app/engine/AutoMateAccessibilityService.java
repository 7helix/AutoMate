package com.automate.app.engine;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;

/**
 * Optional service that only performs system-wide actions (Home, Back, lock screen ...) when a rule asks.
 * It does not read the screen: window content is disabled in accessibility_config.xml and events are ignored.
 */
public class AutoMateAccessibilityService extends AccessibilityService {
    private static volatile AutoMateAccessibilityService instance;

    public static boolean isConnected() {
        return instance != null;
    }

    public static boolean perform(int action) {
        AutoMateAccessibilityService s = instance;
        return s != null && s.performGlobalAction(action);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Intentionally empty: AutoMate never reads what is on screen.
    }

    @Override
    public void onInterrupt() {}

    @Override
    public boolean onUnbind(Intent intent) {
        instance = null;
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        instance = null;
        super.onDestroy();
    }
}
