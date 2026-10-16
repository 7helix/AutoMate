package com.automate.app.engine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.automate.app.data.Rule;
import com.automate.app.data.RuleStore;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String id = intent.getStringExtra("id");
        if (id != null) {
            Rule r = RuleStore.find(context, id);
            if (r != null && r.getEnabled()) {
                RuleEngine.fire(context, r, "Scheduled time reached");
            }
        }
        AlarmScheduler.rescheduleAll(context); // plan the next occurrence
    }
}
