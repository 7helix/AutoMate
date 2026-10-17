package com.automate.app.engine;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.PowerManager;
import android.provider.Settings;

import com.automate.app.data.Condition;
import com.automate.app.data.Rule;

import java.util.Calendar;
import java.util.List;

/** Evaluates the optional "only if" checks of a rule. */
public final class ConditionChecker {
    private ConditionChecker() {}

    /** Returns null when the rule may run, otherwise a short reason why it was skipped. */
    public static String firstFailure(Context c, Rule r) {
        List<Condition> list = r.getConditions();
        if (list == null || list.isEmpty()) return null;
        String firstFail = null;
        boolean anyPass = false;
        for (Condition cond : list) {
            boolean ok;
            try {
                ok = eval(c, cond);
            } catch (RuntimeException e) {
                ok = false;
            }
            if (ok) anyPass = true;
            else if (firstFail == null) firstFail = describe(cond);
        }
        if (r.getMatchAll()) return firstFail;
        return anyPass ? null : "none of the conditions are true";
    }

    private static String describe(Condition c) {
        switch (c.getType()) {
            case TIME_BETWEEN: return "outside the allowed time window";
            case DAYS: return "not an allowed day";
            case BATTERY_ABOVE: return "battery is not above " + c.getA() + "%";
            case BATTERY_BELOW: return "battery is not below " + c.getA() + "%";
            case CHARGING: return "phone is not charging";
            case NOT_CHARGING: return "phone is charging";
            case WIFI_ON: return "Wi-Fi is not connected";
            case WIFI_OFF: return "Wi-Fi is connected";
            case WIFI_NAME: return "not connected to “" + c.getText() + "”";
            case AIRPLANE_ON: return "airplane mode is off";
            case AIRPLANE_OFF: return "airplane mode is on";
            case SCREEN_ON: return "screen is off";
            case DND_ON: return "Do Not Disturb is off";
            default: return "condition not met";
        }
    }

    private static boolean eval(Context c, Condition cond) {
        switch (cond.getType()) {
            case TIME_BETWEEN: {
                Calendar now = Calendar.getInstance();
                int m = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
                int a = cond.getA();
                int b = cond.getB();
                if (a == b) return true;
                return a < b ? (m >= a && m < b) : (m >= a || m < b);
            }
            case DAYS: {
                int idx = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7;
                return (cond.getA() & (1 << idx)) != 0;
            }
            case BATTERY_ABOVE: return batteryPercent(c) > cond.getA();
            case BATTERY_BELOW: return batteryPercent(c) < cond.getA();
            case CHARGING: return isCharging(c);
            case NOT_CHARGING: return !isCharging(c);
            case WIFI_ON: return onWifi(c);
            case WIFI_OFF: return !onWifi(c);
            case WIFI_NAME: {
                String s = ssid(c);
                return s != null && s.equalsIgnoreCase(cond.getText().trim());
            }
            case AIRPLANE_ON: return airplane(c);
            case AIRPLANE_OFF: return !airplane(c);
            case SCREEN_ON: {
                PowerManager pm = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
                return pm != null && pm.isInteractive();
            }
            case DND_ON: {
                NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
                int f = nm.getCurrentInterruptionFilter();
                return f != NotificationManager.INTERRUPTION_FILTER_ALL && f != NotificationManager.INTERRUPTION_FILTER_UNKNOWN;
            }
            default: return true;
        }
    }

    private static Intent battery(Context c) {
        return c.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    private static int batteryPercent(Context c) {
        Intent i = battery(c);
        if (i == null) return 50;
        int level = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = i.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
        return (level < 0 || scale <= 0) ? 50 : level * 100 / scale;
    }

    private static boolean isCharging(Context c) {
        Intent i = battery(c);
        return i != null && i.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0;
    }

    private static boolean onWifi(Context c) {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network n = cm.getActiveNetwork();
        if (n == null) return false;
        NetworkCapabilities caps = cm.getNetworkCapabilities(n);
        return caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
    }

    @SuppressWarnings("deprecation")
    static String ssid(Context c) {
        try {
            WifiManager wm = (WifiManager) c.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wm.getConnectionInfo();
            if (info == null || info.getSSID() == null) return null;
            String s = info.getSSID().replace("\"", "");
            return "<unknown ssid>".equals(s) ? null : s;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static boolean airplane(Context c) {
        return Settings.Global.getInt(c.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
    }
}
