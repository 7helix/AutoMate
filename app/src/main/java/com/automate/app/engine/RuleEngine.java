package com.automate.app.engine;

import android.content.Context;

import com.automate.app.data.LogStore;
import com.automate.app.data.Rule;
import com.automate.app.data.RuleStore;
import com.automate.app.data.TriggerType;

public final class RuleEngine {
    private RuleEngine() {}

    public static void fire(Context ctx, Rule r, String reason) {
        Context app = ctx.getApplicationContext();
        String result;
        boolean ok = true;
        try {
            result = ActionRunner.run(app, r);
        } catch (Exception e) {
            ok = false;
            result = e.getMessage() != null ? e.getMessage() : e.toString();
        }
        RuleStore.markRun(app, r.getId());
        LogStore.add(app, r.getName().isEmpty() ? "Untitled rule" : r.getName(), reason + " → " + result, ok);
    }

    /** Fires every enabled rule with this trigger; for Wi-Fi / Bluetooth the optional name filter must match. */
    public static void fireMatching(Context ctx, TriggerType type, String detail, String reason) {
        boolean filtered = type == TriggerType.WIFI_CONNECTED || type == TriggerType.WIFI_DISCONNECTED
                || type == TriggerType.BLUETOOTH_CONNECTED || type == TriggerType.BLUETOOTH_DISCONNECTED;
        for (Rule r : RuleStore.load(ctx)) {
            if (!r.getEnabled() || r.getTrigger() != type) continue;
            String f = r.getFilter().trim();
            if (filtered && !f.isEmpty() && (detail == null || !f.equalsIgnoreCase(detail.trim()))) continue;
            fire(ctx, r, reason);
        }
    }
}
