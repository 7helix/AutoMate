package com.automate.app.engine;

import android.content.Context;

import com.automate.app.data.ActionItem;
import com.automate.app.data.LogStore;
import com.automate.app.data.Rule;
import com.automate.app.data.RuleStore;
import com.automate.app.data.StatsStore;
import com.automate.app.data.TriggerType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class RuleEngine {
    private static final ExecutorService EXEC = Executors.newCachedThreadPool();

    private RuleEngine() {}

    /** Automatic run: conditions are checked first. */
    public static void fire(Context ctx, Rule r, String reason) {
        fire(ctx, r, reason, null);
    }

    public static void fire(Context ctx, Rule r, String reason, Runnable done) {
        final Context app = ctx.getApplicationContext();
        EXEC.execute(() -> {
            try {
                runSteps(app, r, reason, false);
            } finally {
                if (done != null) done.run();
            }
        });
    }

    /** Manual run from the app ("Test action"): conditions are ignored. */
    public static void fireManual(Context ctx, Rule r, String reason) {
        final Context app = ctx.getApplicationContext();
        EXEC.execute(() -> runSteps(app, r, reason, true));
    }

    /** Fires every enabled rule with this trigger; for Wi-Fi / Bluetooth the optional name filter must match. */
    public static void fireMatching(Context ctx, TriggerType type, String detail, String reason) {
        fireMatching(ctx, type, detail, reason, null);
    }

    public static void fireMatching(Context ctx, TriggerType type, String detail, String reason, Runnable done) {
        final Context app = ctx.getApplicationContext();
        boolean filtered = type == TriggerType.WIFI_CONNECTED || type == TriggerType.WIFI_DISCONNECTED
                || type == TriggerType.BLUETOOTH_CONNECTED || type == TriggerType.BLUETOOTH_DISCONNECTED;
        final List<Rule> matched = new ArrayList<>();
        for (Rule r : RuleStore.load(app)) {
            if (!r.getEnabled() || r.getTrigger() != type) continue;
            String f = r.getFilter().trim();
            if (filtered && !f.isEmpty() && (detail == null || !f.equalsIgnoreCase(detail.trim()))) continue;
            matched.add(r);
        }
        EXEC.execute(() -> {
            try {
                for (Rule r : matched) runSteps(app, r, reason, false);
            } finally {
                if (done != null) done.run();
            }
        });
    }

    private static boolean noisy(Rule r) {
        TriggerType t = r.getTrigger();
        return t == TriggerType.REPEAT || t == TriggerType.SCREEN_ON || t == TriggerType.SCREEN_OFF
                || t == TriggerType.DEVICE_UNLOCKED;
    }

    private static String nameOf(Rule r) {
        return r.getName().isEmpty() ? "Untitled rule" : r.getName();
    }

    private static void runSteps(Context app, Rule r, String reason, boolean manual) {
        if (!manual) {
            String why = ConditionChecker.firstFailure(app, r);
            if (why != null) {
                if (!noisy(r)) LogStore.add(app, nameOf(r), reason + " → skipped (" + why + ")", true);
                return;
            }
        }
        StringBuilder out = new StringBuilder();
        boolean ok = true;
        List<ActionItem> steps = r.getActions();
        for (int i = 0; i < steps.size(); i++) {
            ActionItem a = steps.get(i);
            if (a.getDelaySec() > 0) {
                try {
                    Thread.sleep(a.getDelaySec() * 1000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            if (out.length() > 0) out.append("; ");
            try {
                out.append(ActionRunner.run(app, a, r.getName()));
            } catch (Exception e) {
                ok = false;
                out.append("failed: ").append(e.getMessage() != null ? e.getMessage() : e.toString());
            }
        }
        RuleStore.markRun(app, r.getId());
        LogStore.add(app, nameOf(r), reason + " → " + out, ok);
        StatsStore.record(app, ok);
    }
}
