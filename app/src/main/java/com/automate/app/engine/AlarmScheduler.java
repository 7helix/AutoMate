package com.automate.app.engine;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.automate.app.data.Rule;
import com.automate.app.data.RuleStore;
import com.automate.app.data.TriggerType;

import java.util.Calendar;

public final class AlarmScheduler {
    private AlarmScheduler() {}

    private static PendingIntent pi(Context c, String id) {
        Intent i = new Intent(c, AlarmReceiver.class).setAction("com.automate.app.ALARM").putExtra("id", id);
        return PendingIntent.getBroadcast(c, id.hashCode(), i, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void cancel(Context c, String id) {
        AlarmManager am = c.getSystemService(AlarmManager.class);
        if (am != null) am.cancel(pi(c, id));
    }

    public static void rescheduleAll(Context c) {
        AlarmManager am = c.getSystemService(AlarmManager.class);
        if (am == null) return;
        for (Rule r : RuleStore.load(c)) {
            PendingIntent p = pi(c, r.getId());
            am.cancel(p);
            if (!r.getEnabled() || r.getTrigger() != TriggerType.TIME) continue;
            long t = nextTime(r);
            if (t < 0) continue;
            try {
                if (Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) {
                    am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t, p);
                } else {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t, p);
                }
            } catch (SecurityException e) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t, p);
            }
        }
    }

    /** Next moment this rule should fire, or -1 if no day is selected. days: bit0 = Monday ... bit6 = Sunday. */
    public static long nextTime(Rule r) {
        Calendar now = Calendar.getInstance();
        for (int i = 0; i < 8; i++) {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_YEAR, i);
            c.set(Calendar.HOUR_OF_DAY, r.getHour());
            c.set(Calendar.MINUTE, r.getMinute());
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            int idx = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7;
            boolean dayOk = (r.getDays() & (1 << idx)) != 0;
            if (dayOk && c.after(now)) return c.getTimeInMillis();
        }
        return -1;
    }
}
