package com.automate.app.engine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.automate.app.data.Rule;
import com.automate.app.data.RuleStore;
import com.automate.app.data.TriggerType;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final Context app = context.getApplicationContext();
        String id = intent.getStringExtra("id");
        final Rule rule = id != null ? RuleStore.find(app, id) : null;
        final PendingResult pr = goAsync();
        if (rule != null && rule.getEnabled()) {
            boolean repeat = rule.getTrigger() == TriggerType.REPEAT;
            RuleEngine.fire(app, rule, repeat ? "Repeat interval reached" : "Scheduled time reached", () -> {
                if (rule.getTrigger() == TriggerType.REPEAT) AlarmScheduler.advanceRepeat(app, rule);
                AlarmScheduler.rescheduleAll(app); // plan the next occurrence
                pr.finish();
            });
        } else {
            AlarmScheduler.rescheduleAll(app);
            pr.finish();
        }
    }
}
