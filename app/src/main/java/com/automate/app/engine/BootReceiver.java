package com.automate.app.engine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;

import com.automate.app.data.Prefs;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmScheduler.rescheduleAll(context);
        boolean wanted = Prefs.getBool(context, Prefs.RUN_ON_BOOT, true)
                && Prefs.getBool(context, Prefs.SERVICE_ENABLED, false);
        if (wanted) {
            try {
                ContextCompat.startForegroundService(context, new Intent(context, AutomationService.class));
            } catch (RuntimeException ignored) {
                // Some OEMs block this; the user can still start the engine from the app.
            }
        }
    }
}
