package com.automate.app.engine;

import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.provider.Settings;
import android.widget.Toast;

import com.automate.app.data.Rule;
import com.automate.app.data.Social;
import com.automate.app.data.SocialPlatform;

public final class ActionRunner {
    /** Set by MainActivity so we can start activities directly while the app is on screen. */
    public static volatile boolean appVisible = false;
    private static volatile boolean torchOn = false;
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private ActionRunner() {}

    /** Runs the rule's action and returns a short human-readable result. Throws on failure. */
    public static String run(Context ctx, Rule r) throws Exception {
        final Context app = ctx.getApplicationContext();
        final String msg = r.getMessage().trim();
        switch (r.getAction()) {
            case NOTIFICATION: {
                String text = msg.isEmpty() ? "Your rule \"" + r.getName() + "\" just ran." : msg;
                Notifier.alert(app, r.getName().isEmpty() ? "AutoMate" : r.getName(), text, null);
                return "Sent notification";
            }
            case TOAST: {
                final String t = msg.isEmpty() ? r.getName() : msg;
                MAIN.post(() -> Toast.makeText(app, t, Toast.LENGTH_LONG).show());
                return "Showed toast";
            }
            case LAUNCH_APP: {
                Intent i = app.getPackageManager().getLaunchIntentForPackage(r.getPackageName());
                if (i == null) throw new IllegalStateException("App not installed: " + r.getPackageName());
                return launch(app, i, "Opened app");
            }
            case OPEN_URL: {
                String u = r.getUrl().trim();
                if (!u.contains("://")) u = "https://" + u;
                return launch(app, new Intent(Intent.ACTION_VIEW, Uri.parse(u)), "Opened link");
            }
            case VIBRATE:
                vibrate(app);
                return "Vibrated";
            case FLASHLIGHT:
                return flashlight(app);
            case RINGER_MODE:
                return ringer(app, r.getRinger());
            case OPEN_WIFI_PANEL: {
                Intent i = new Intent(Build.VERSION.SDK_INT >= 29
                        ? Settings.Panel.ACTION_INTERNET_CONNECTIVITY
                        : Settings.ACTION_WIFI_SETTINGS);
                return launch(app, i, "Opened Wi-Fi panel");
            }
            case SOCIAL_OPEN: {
                SocialPlatform p = Social.find(r.getSocial());
                if (p == null) throw new IllegalStateException("Choose a social platform");
                return launch(app, Social.buildIntent(app, p, r.getUrl()), "Opened " + p.getLabel());
            }
            case SHARE_TEXT: {
                String t = msg.isEmpty() ? r.getName() : msg;
                Intent send = new Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, t);
                return launch(app, Intent.createChooser(send, "Share via"), "Opened share sheet");
            }
            case WHATSAPP_CHAT: {
                if (r.getUrl().replaceAll("[^0-9]", "").isEmpty()) {
                    throw new IllegalStateException("Enter a phone number with country code");
                }
                return launch(app, Social.whatsAppIntent(app, r.getUrl(), msg), "Opened WhatsApp chat (you press send)");
            }
            case COPY_TEXT: {
                ClipboardManager cm = (ClipboardManager) app.getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setPrimaryClip(ClipData.newPlainText("AutoMate", msg.isEmpty() ? r.getName() : msg));
                return "Copied text";
            }
            default:
                throw new IllegalStateException("Unknown action");
        }
    }

    private static void vibrate(Context c) {
        Vibrator v;
        if (Build.VERSION.SDK_INT >= 31) {
            VibratorManager vm = (VibratorManager) c.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            v = vm.getDefaultVibrator();
        } else {
            v = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
        }
        if (v != null) v.vibrate(VibrationEffect.createOneShot(600, VibrationEffect.DEFAULT_AMPLITUDE));
    }

    private static String flashlight(Context c) throws Exception {
        CameraManager cm = (CameraManager) c.getSystemService(Context.CAMERA_SERVICE);
        for (String id : cm.getCameraIdList()) {
            CameraCharacteristics cc = cm.getCameraCharacteristics(id);
            Boolean flash = cc.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            Integer facing = cc.get(CameraCharacteristics.LENS_FACING);
            if (Boolean.TRUE.equals(flash) && facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                torchOn = !torchOn;
                cm.setTorchMode(id, torchOn);
                return torchOn ? "Flashlight on" : "Flashlight off";
            }
        }
        throw new IllegalStateException("No flashlight found on this device");
    }

    private static String ringer(Context c, int mode) {
        AudioManager am = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
        try {
            am.setRingerMode(mode);
        } catch (SecurityException e) {
            throw new IllegalStateException("Silent mode needs Do Not Disturb access (Settings → Permissions)");
        }
        return mode == 0 ? "Ringer: silent" : mode == 1 ? "Ringer: vibrate" : "Ringer: normal";
    }

    private static String launch(Context app, Intent i, String label) {
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (appVisible || Settings.canDrawOverlays(app)) {
            app.startActivity(i);
            return label;
        }
        // Android blocks background activity launches, so hand the user a tappable notification.
        PendingIntent pi = PendingIntent.getActivity(app, (int) (System.nanoTime() & 0x7fffffff), i,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        Notifier.alert(app, label, "Tap to continue. Tip: allow “Display over other apps” for instant launches.", pi);
        return label + " via notification (tap it)";
    }
}
