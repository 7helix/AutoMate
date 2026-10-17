package com.automate.app.engine;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import androidx.core.content.ContextCompat;

import com.automate.app.MainActivity;
import com.automate.app.R;
import com.automate.app.data.Prefs;
import com.automate.app.data.Rule;
import com.automate.app.data.RuleStore;

/** Home-screen widget with a Start / Stop button for the automation engine. */
public class EngineWidget extends AppWidgetProvider {
    public static final String ACTION_TOGGLE = "com.automate.app.WIDGET_TOGGLE";

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        manager.updateAppWidget(ids, build(context));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_TOGGLE.equals(intent.getAction())) {
            boolean on = Prefs.getBool(context, Prefs.SERVICE_ENABLED, false);
            Intent s = new Intent(context, AutomationService.class);
            try {
                if (on) {
                    context.stopService(s);
                    Prefs.setBool(context, Prefs.SERVICE_ENABLED, false);
                } else {
                    ContextCompat.startForegroundService(context, s);
                    Prefs.setBool(context, Prefs.SERVICE_ENABLED, true);
                }
            } catch (RuntimeException ignored) {
                // The system may refuse to start the service from here; the app can still do it.
            }
            refresh(context);
        }
    }

    public static void refresh(Context context) {
        AppWidgetManager m = AppWidgetManager.getInstance(context);
        int[] ids = m.getAppWidgetIds(new ComponentName(context, EngineWidget.class));
        if (ids != null && ids.length > 0) m.updateAppWidget(ids, build(context));
    }

    private static RemoteViews build(Context c) {
        boolean on = Prefs.getBool(c, Prefs.SERVICE_ENABLED, false);
        int active = 0;
        for (Rule r : RuleStore.load(c)) if (r.getEnabled()) active++;

        RemoteViews v = new RemoteViews(c.getPackageName(), R.layout.widget_engine);
        v.setTextViewText(R.id.w_status, on ? "Engine running" : "Engine stopped");
        v.setTextViewText(R.id.w_sub, active + (active == 1 ? " active rule" : " active rules"));
        v.setTextViewText(R.id.w_button, on ? "Stop" : "Start");
        v.setInt(R.id.w_button, "setBackgroundResource", on ? R.drawable.widget_btn_stop : R.drawable.widget_btn_start);

        PendingIntent toggle = PendingIntent.getBroadcast(c, 10,
                new Intent(c, EngineWidget.class).setAction(ACTION_TOGGLE),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        v.setOnClickPendingIntent(R.id.w_button, toggle);

        PendingIntent open = PendingIntent.getActivity(c, 11, new Intent(c, MainActivity.class),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        v.setOnClickPendingIntent(R.id.w_root, open);
        return v;
    }
}
