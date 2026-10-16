package com.automate.app.engine;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.automate.app.MainActivity;
import com.automate.app.R;

public final class Notifier {
    public static final String CH_SERVICE = "automate_service";
    public static final String CH_ALERTS = "automate_alerts";
    private static int nextId = 2000;

    private Notifier() {}

    public static void createChannels(Context c) {
        NotificationManager nm = c.getSystemService(NotificationManager.class);
        if (nm == null) return;
        NotificationChannel s = new NotificationChannel(CH_SERVICE, "Automation engine", NotificationManager.IMPORTANCE_LOW);
        s.setDescription("Shown while AutoMate watches your rules");
        nm.createNotificationChannel(s);
        NotificationChannel a = new NotificationChannel(CH_ALERTS, "Rule alerts", NotificationManager.IMPORTANCE_HIGH);
        a.setDescription("Notifications sent by your automation rules");
        nm.createNotificationChannel(a);
    }

    private static PendingIntent openApp(Context c) {
        Intent open = new Intent(c, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(c, 0, open, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static Notification service(Context c, String text) {
        Intent stop = new Intent(c, AutomationService.class).setAction(AutomationService.ACTION_STOP);
        PendingIntent stopPi = PendingIntent.getService(c, 1, stop, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        return new NotificationCompat.Builder(c, CH_SERVICE)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("AutoMate is running")
                .setContentText(text)
                .setContentIntent(openApp(c))
                .setOngoing(true)
                .addAction(0, "Stop", stopPi)
                .build();
    }

    public static synchronized void alert(Context c, String title, String text, PendingIntent tap) {
        Notification n = new NotificationCompat.Builder(c, CH_ALERTS)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setContentIntent(tap != null ? tap : openApp(c))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
        try {
            NotificationManagerCompat.from(c).notify(nextId++, n);
        } catch (SecurityException ignored) {
            // notification permission not granted
        }
    }
}
