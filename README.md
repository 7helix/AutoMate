# AutoMate ⚡
All-in-one Android automation: build "When X happens → do Y" rules.

**Triggers:** time & days, battery low, charger on/off, Wi-Fi on/off, Bluetooth on/off, arrive/leave a place, phone unlocked
**Actions:** notification, toast, open app, open link, vibrate, flashlight, ringer mode, Wi-Fi panel

**UI:** Home, Rules, Edit rule, Logs, Settings (+ permission center), About, Onboarding. 4 themes, animations, animated toasts.

## Build on GitHub
1. Create a repo, upload this whole folder (keep `.github/workflows/build.yml`).
2. Open **Actions → Build AutoMate APK** (it also runs on every push).
3. Download the `AutoMate-APKs` artifact. Tag `v1.0.0` to auto-publish a Release.

No Gradle wrapper needed: the workflow installs Gradle 8.7 itself. Opening in Android Studio works too.
