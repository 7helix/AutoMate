package com.automate.app.engine;

import android.accessibilityservice.AccessibilityService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.Toast;

import com.automate.app.data.ActionItem;
import com.automate.app.data.Social;
import com.automate.app.data.SocialPlatform;
import com.automate.app.data.SystemKeys;

public final class ActionRunner {
    /** Set by MainActivity so we can start activities directly while the app is on screen. */
    public static volatile boolean appVisible = false;
    private static volatile boolean torchOn = false;
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private ActionRunner() {}

    /** Runs one step and returns a short human-readable result. Throws with a clear message on failure. */
    public static String run(Context ctx, ActionItem a, String ruleName) throws Exception {
        final Context app = ctx.getApplicationContext();
        final String msg = a.getMessage().trim();
        final String title = (ruleName == null || ruleName.isEmpty()) ? "AutoMate" : ruleName;
        switch (a.getType()) {
            case NOTIFICATION: {
                String text = msg.isEmpty() ? "Your rule \"" + title + "\" just ran." : msg;
                Notifier.alert(app, title, text, null);
                return "Sent notification";
            }
            case TOAST: {
                final String t = msg.isEmpty() ? title : msg;
                MAIN.post(() -> Toast.makeText(app, t, Toast.LENGTH_LONG).show());
                return "Showed toast";
            }
            case SPEAK_TEXT: {
                if (msg.isEmpty()) throw new IllegalStateException("Enter the text to speak");
                speak(app, msg);
                return "Speaking";
            }
            case COPY_TEXT: {
                ClipboardManager cm = (ClipboardManager) app.getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setPrimaryClip(ClipData.newPlainText("AutoMate", msg.isEmpty() ? title : msg));
                return "Copied text";
            }
            case DIAL_NUMBER: {
                String n = a.getUrl().trim();
                if (n.isEmpty()) throw new IllegalStateException("Enter a phone number");
                return launch(app, new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(n))), "Opened dialer");
            }
            case SMS_DRAFT: {
                Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + Uri.encode(a.getUrl().trim())));
                i.putExtra("sms_body", msg);
                return launch(app, i, "Opened text message (you press send)");
            }
            case EMAIL_DRAFT: {
                Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + a.getUrl().trim()));
                i.putExtra(Intent.EXTRA_SUBJECT, title);
                i.putExtra(Intent.EXTRA_TEXT, msg);
                return launch(app, i, "Opened email (you press send)");
            }
            case WHATSAPP_CHAT: {
                if (a.getUrl().replaceAll("[^0-9]", "").isEmpty()) {
                    throw new IllegalStateException("Enter a phone number with country code");
                }
                return launch(app, Social.whatsAppIntent(app, a.getUrl(), msg), "Opened WhatsApp chat (you press send)");
            }
            case SHARE_TEXT: {
                String t = msg.isEmpty() ? title : msg;
                Intent send = new Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, t);
                return launch(app, Intent.createChooser(send, "Share via"), "Opened share sheet");
            }
            case VIBRATE:
                vibrate(app);
                return "Vibrated";
            case PLAY_SOUND: {
                Uri u = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(app, u);
                if (r == null) throw new IllegalStateException("No notification sound available");
                r.play();
                return "Played sound";
            }
            case RINGER_MODE:
                return ringer(app, a.getRinger());
            case SET_VOLUME:
                return volume(app, a.getStream(), a.getLevel());
            case SET_BRIGHTNESS:
                return brightness(app, a.getLevel());
            case DND_MODE:
                return dnd(app, a.getRinger());
            case LAUNCH_APP: {
                Intent i = app.getPackageManager().getLaunchIntentForPackage(a.getPackageName());
                if (i == null) throw new IllegalStateException("App not installed: " + a.getPackageName());
                return launch(app, i, "Opened app");
            }
            case OPEN_URL: {
                String u = a.getUrl().trim();
                if (!u.contains("://")) u = "https://" + u;
                return launch(app, new Intent(Intent.ACTION_VIEW, Uri.parse(u)), "Opened link");
            }
            case OPEN_CAMERA:
                return launch(app, new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA), "Opened camera");
            case FLASHLIGHT:
                return flashlight(app);
            case SYSTEM_ACTION:
                return systemAction(a.getSysAction());
            case OPEN_SETTINGS:
                return launch(app, new Intent(SystemKeys.settingsAction(a.getSysAction())), "Opened settings");
            case OPEN_WIFI_PANEL: {
                Intent i = new Intent(Build.VERSION.SDK_INT >= 29
                        ? Settings.Panel.ACTION_INTERNET_CONNECTIVITY
                        : Settings.ACTION_WIFI_SETTINGS);
                return launch(app, i, "Opened Wi-Fi panel");
            }
            case SOCIAL_OPEN: {
                SocialPlatform p = Social.find(a.getSocial());
                if (p == null) throw new IllegalStateException("Choose a social platform");
                return launch(app, Social.buildIntent(app, p, a.getUrl()), "Opened " + p.getLabel());
            }
            default:
                throw new IllegalStateException("Unknown action");
        }
    }

    // ------------------------------------------------------------ phone control

    /** Presses a system button through the optional Accessibility service. */
    public static String systemAction(String key) throws Exception {
        if (!AutoMateAccessibilityService.isConnected()) {
            throw new IllegalStateException("Turn on AutoMate in Accessibility settings (Phone control page)");
        }
        int action;
        String name;
        switch (key) {
            case "back": action = AccessibilityService.GLOBAL_ACTION_BACK; name = "Back"; break;
            case "recents": action = AccessibilityService.GLOBAL_ACTION_RECENTS; name = "Recent apps"; break;
            case "notifications": action = AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS; name = "Notification shade"; break;
            case "quick_settings": action = AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS; name = "Quick settings"; break;
            case "power_menu": action = AccessibilityService.GLOBAL_ACTION_POWER_DIALOG; name = "Power menu"; break;
            case "lock":
                if (Build.VERSION.SDK_INT < 28) throw new IllegalStateException("Lock screen needs Android 9 or newer");
                action = AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN; name = "Lock screen"; break;
            case "screenshot":
                if (Build.VERSION.SDK_INT < 28) throw new IllegalStateException("Screenshots need Android 9 or newer");
                action = AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT; name = "Screenshot"; break;
            default: action = AccessibilityService.GLOBAL_ACTION_HOME; name = "Home"; break;
        }
        if (!AutoMateAccessibilityService.perform(action)) {
            throw new IllegalStateException("Android did not accept the " + name + " action");
        }
        return "Pressed " + name;
    }

    // ------------------------------------------------------------ helpers

    private static void speak(final Context app, final String text) {
        MAIN.post(() -> {
            final TextToSpeech[] holder = new TextToSpeech[1];
            holder[0] = new TextToSpeech(app, status -> {
                final TextToSpeech t = holder[0];
                if (t == null) return;
                if (status != TextToSpeech.SUCCESS) {
                    t.shutdown();
                    return;
                }
                t.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override public void onStart(String id) {}
                    @Override public void onDone(String id) { MAIN.post(t::shutdown); }
                    @Override public void onError(String id) { MAIN.post(t::shutdown); }
                });
                t.speak(text, TextToSpeech.QUEUE_FLUSH, null, "automate");
            });
            MAIN.postDelayed(() -> {
                if (holder[0] != null) holder[0].shutdown();
            }, 30000);
        });
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

    private static String volume(Context c, int stream, int percent) {
        AudioManager am = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
        int type;
        String name;
        switch (stream) {
            case 1: type = AudioManager.STREAM_RING; name = "Ring"; break;
            case 2: type = AudioManager.STREAM_ALARM; name = "Alarm"; break;
            case 3: type = AudioManager.STREAM_NOTIFICATION; name = "Alerts"; break;
            default: type = AudioManager.STREAM_MUSIC; name = "Media"; break;
        }
        int max = am.getStreamMaxVolume(type);
        int v = Math.round(max * Math.max(0, Math.min(100, percent)) / 100f);
        try {
            am.setStreamVolume(type, v, 0);
        } catch (SecurityException e) {
            throw new IllegalStateException("This volume needs Do Not Disturb access (Settings → Permissions)");
        }
        return name + " volume " + percent + "%";
    }

    private static String brightness(Context c, int percent) {
        if (!Settings.System.canWrite(c)) {
            throw new IllegalStateException("Allow “Modify system settings” for AutoMate (Settings → Permissions)");
        }
        ContentResolver cr = c.getContentResolver();
        Settings.System.putInt(cr, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        int v = Math.max(1, Math.min(255, Math.round(255 * Math.max(1, Math.min(100, percent)) / 100f)));
        Settings.System.putInt(cr, Settings.System.SCREEN_BRIGHTNESS, v);
        return "Brightness " + percent + "%";
    }

    private static String dnd(Context c, int mode) {
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (!nm.isNotificationPolicyAccessGranted()) {
            throw new IllegalStateException("Allow Do Not Disturb access (Settings → Permissions)");
        }
        int f = mode == 0 ? NotificationManager.INTERRUPTION_FILTER_NONE
                : mode == 1 ? NotificationManager.INTERRUPTION_FILTER_PRIORITY
                : NotificationManager.INTERRUPTION_FILTER_ALL;
        nm.setInterruptionFilter(f);
        return mode == 0 ? "Do Not Disturb: block all" : mode == 1 ? "Do Not Disturb: priority only" : "Do Not Disturb off";
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
