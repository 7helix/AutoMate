package com.automate.app.engine;

import android.Manifest;
import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.automate.app.data.Prefs;
import com.automate.app.data.Rule;
import com.automate.app.data.RuleStore;
import com.automate.app.data.TriggerType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Foreground service that listens for battery / charger / Wi-Fi / Bluetooth / unlock / location events. */
public class AutomationService extends Service {
    public static final String ACTION_STOP = "com.automate.app.STOP";
    public static final String ACTION_REFRESH = "com.automate.app.REFRESH";
    private static final int NOTIF_ID = 1001;

    private boolean registered = false;
    private long startedAt;
    private String lastSsid = null;
    private final Set<String> belowFired = new HashSet<>();
    private final Map<String, Boolean> insideState = new HashMap<>();
    private ConnectivityManager.NetworkCallback netCb;
    private LocationManager lm;
    private LocationListener locListener;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent i) {
            String a = i.getAction();
            if (a == null) return;
            switch (a) {
                case Intent.ACTION_BATTERY_CHANGED:
                    onBattery(i);
                    break;
                case Intent.ACTION_POWER_CONNECTED:
                    RuleEngine.fireMatching(c, TriggerType.CHARGER_CONNECTED, null, "Charger connected");
                    break;
                case Intent.ACTION_POWER_DISCONNECTED:
                    RuleEngine.fireMatching(c, TriggerType.CHARGER_DISCONNECTED, null, "Charger disconnected");
                    break;
                case Intent.ACTION_USER_PRESENT:
                    RuleEngine.fireMatching(c, TriggerType.DEVICE_UNLOCKED, null, "Phone unlocked");
                    break;
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    onBluetooth(i, true);
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    onBluetooth(i, false);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notifier.createChannels(this);
        String action = intent != null ? intent.getAction() : null;
        if (ACTION_STOP.equals(action)) {
            Prefs.setBool(this, Prefs.SERVICE_ENABLED, false);
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }
        boolean fromUi = intent != null && intent.getBooleanExtra("from_ui", false);
        startInForeground(fromUi);
        if (!registered) {
            startedAt = SystemClock.elapsedRealtime();
            registerAll();
            registered = true;
        }
        refreshLocation();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        try { unregisterReceiver(receiver); } catch (RuntimeException ignored) {}
        if (netCb != null) {
            try {
                getSystemService(ConnectivityManager.class).unregisterNetworkCallback(netCb);
            } catch (RuntimeException ignored) {}
        }
        stopLocation();
        registered = false;
        super.onDestroy();
    }

    // ---------------------------------------------------------------- foreground

    private boolean granted(String perm) {
        return ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED;
    }

    private void startInForeground(boolean fromUi) {
        int active = 0;
        for (Rule r : RuleStore.load(this)) if (r.getEnabled()) active++;
        String text = active == 0 ? "No active rules yet"
                : "Watching " + active + (active == 1 ? " active rule" : " active rules");
        Notification n = Notifier.service(this, text);

        boolean fine = granted(Manifest.permission.ACCESS_FINE_LOCATION);
        boolean bg = Build.VERSION.SDK_INT < 29 || granted(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        boolean loc = fine && (fromUi || bg);
        try {
            if (Build.VERSION.SDK_INT >= 34) {
                int type = ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE;
                if (loc) type |= ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION;
                startForeground(NOTIF_ID, n, type);
            } else if (Build.VERSION.SDK_INT >= 29) {
                startForeground(NOTIF_ID, n, loc ? ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION : 0);
            } else {
                startForeground(NOTIF_ID, n);
            }
        } catch (RuntimeException e) {
            try {
                if (Build.VERSION.SDK_INT >= 34) {
                    startForeground(NOTIF_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
                } else {
                    startForeground(NOTIF_ID, n);
                }
            } catch (RuntimeException e2) {
                stopSelf();
            }
        }
    }

    // ---------------------------------------------------------------- listeners

    private void registerAll() {
        IntentFilter f = new IntentFilter();
        f.addAction(Intent.ACTION_BATTERY_CHANGED);
        f.addAction(Intent.ACTION_POWER_CONNECTED);
        f.addAction(Intent.ACTION_POWER_DISCONNECTED);
        f.addAction(Intent.ACTION_USER_PRESENT);
        f.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        f.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        ContextCompat.registerReceiver(this, receiver, f, ContextCompat.RECEIVER_NOT_EXPORTED);

        try {
            ConnectivityManager cm = getSystemService(ConnectivityManager.class);
            NetworkRequest req = new NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build();
            netCb = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    lastSsid = currentSsid();
                    if (warm()) {
                        RuleEngine.fireMatching(getApplicationContext(), TriggerType.WIFI_CONNECTED, lastSsid, "Wi-Fi connected");
                    }
                }

                @Override
                public void onLost(@NonNull Network network) {
                    if (warm()) {
                        RuleEngine.fireMatching(getApplicationContext(), TriggerType.WIFI_DISCONNECTED, lastSsid, "Wi-Fi disconnected");
                    }
                }
            };
            cm.registerNetworkCallback(req, netCb);
        } catch (RuntimeException e) {
            netCb = null;
        }
    }

    /** Ignore the "already connected" callback that fires right after registering. */
    private boolean warm() {
        return SystemClock.elapsedRealtime() - startedAt > 3000;
    }

    @SuppressWarnings("deprecation")
    private String currentSsid() {
        try {
            WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wm.getConnectionInfo();
            if (info == null || info.getSSID() == null) return null;
            String s = info.getSSID().replace("\"", "");
            return "<unknown ssid>".equals(s) ? null : s;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private void onBattery(Intent i) {
        int level = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = i.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
        int status = i.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        if (level < 0 || scale <= 0) return;
        int pct = level * 100 / scale;
        boolean charging = status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL;
        for (Rule r : RuleStore.load(this)) {
            if (!r.getEnabled() || r.getTrigger() != TriggerType.BATTERY_LOW) continue;
            if (!charging && pct <= r.getThreshold()) {
                if (belowFired.add(r.getId())) RuleEngine.fire(this, r, "Battery at " + pct + "%");
            } else {
                belowFired.remove(r.getId());
            }
        }
    }

    private void onBluetooth(Intent i, boolean connected) {
        BluetoothDevice d;
        if (Build.VERSION.SDK_INT >= 33) {
            d = i.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice.class);
        } else {
            d = i.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        }
        String name = null;
        if (d != null) {
            try { name = d.getName(); } catch (SecurityException ignored) {}
            if (name == null) name = d.getAddress();
        }
        RuleEngine.fireMatching(this,
                connected ? TriggerType.BLUETOOTH_CONNECTED : TriggerType.BLUETOOTH_DISCONNECTED,
                name, (connected ? "Connected to " : "Disconnected from ") + (name != null ? name : "device"));
    }

    // ---------------------------------------------------------------- location

    private void refreshLocation() {
        stopLocation();
        boolean need = false;
        for (Rule r : RuleStore.load(this)) {
            if (r.getEnabled() && (r.getTrigger() == TriggerType.LOCATION_ENTER || r.getTrigger() == TriggerType.LOCATION_EXIT)) {
                need = true;
                break;
            }
        }
        if (!need || !granted(Manifest.permission.ACCESS_FINE_LOCATION)) return;

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locListener = new LocationListener() {
            @Override public void onLocationChanged(@NonNull Location l) { evaluateLocation(l); }
            @Override public void onStatusChanged(String p, int s, Bundle e) {}
            @Override public void onProviderEnabled(@NonNull String p) {}
            @Override public void onProviderDisabled(@NonNull String p) {}
        };
        try {
            List<String> providers = lm.getProviders(true);
            for (String p : providers) {
                if (LocationManager.PASSIVE_PROVIDER.equals(p)) continue;
                lm.requestLocationUpdates(p, 20_000L, 25f, locListener, Looper.getMainLooper());
                Location last = lm.getLastKnownLocation(p);
                if (last != null) evaluateLocation(last);
            }
        } catch (SecurityException ignored) {
            // permission revoked while running
        }
    }

    private void stopLocation() {
        if (lm != null && locListener != null) {
            try { lm.removeUpdates(locListener); } catch (RuntimeException ignored) {}
        }
        lm = null;
        locListener = null;
    }

    private void evaluateLocation(Location loc) {
        if (loc.hasAccuracy() && loc.getAccuracy() > 200f) return;
        for (Rule r : RuleStore.load(this)) {
            if (!r.getEnabled()) continue;
            boolean enter = r.getTrigger() == TriggerType.LOCATION_ENTER;
            boolean exit = r.getTrigger() == TriggerType.LOCATION_EXIT;
            if (!enter && !exit) continue;
            float[] d = new float[1];
            Location.distanceBetween(loc.getLatitude(), loc.getLongitude(), r.getLat(), r.getLng(), d);
            boolean inside = d[0] <= r.getRadius();
            Boolean prev = insideState.put(r.getId(), inside);
            if (prev == null) continue; // first sample only establishes state
            if (enter && !prev && inside) RuleEngine.fire(this, r, "Arrived at saved place");
            if (exit && prev && !inside) RuleEngine.fire(this, r, "Left saved place");
        }
    }
}
