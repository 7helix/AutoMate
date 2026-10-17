package com.automate.app.engine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;

import com.automate.app.data.Prefs;
import com.automate.app.data.TriggerType;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final PendingResult pr = goAsync();
        AlarmScheduler.rescheduleAll(context);
        boolean wanted = Prefs.getBool(context, Prefs.RUN_ON_BOOT, true)
                && Prefs.getBool(context, Prefs.SERVICE_ENABLED, false);
        if (wanted) {
            try {
                ContextCompat.startForegroundService(context, new Intent(context, AutomationService.class));
            } catch (RuntimeException ignored) {
                // Some phone brands block this; the user can still start the engine from the app.
            }
        }
        EngineWidget.refresh(context);
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            RuleEngine.fireMatching(context, TriggerType.DEVICE_BOOT, null, "Phone started", pr::finish);
        } else {
            pr.finish();
        }
    }
}
