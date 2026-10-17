# Android restrictions

AutoMate is a normal app, not a system app. Android deliberately limits what normal apps can do. These limits are **not bugs**.

## Things Android does not allow
- **Switching Wi-Fi, Bluetooth, mobile data or airplane mode on or off.** Apps could do this on old Android versions. Since Android 10 only the user or the system can. AutoMate can open the Wi-Fi panel so you can do it with one tap.
- **Starting apps from the background without help.** Android blocks it. AutoMate works around it with a tap-to-open notification, or opens apps directly if you allow **Display over other apps**.
- **Silent mode without permission.** It needs Do Not Disturb access.
- **Reading Wi-Fi names without location.** Needs precise location permission and location services on.
- **Reading Bluetooth device names without permission.** Needs the Nearby devices permission.

## Things that depend on your phone brand
- Aggressive battery managers can stop the engine. See "Keeping the engine running".
- Some brands hide or reset special permissions after updates.

## What AutoMate deliberately does not do
- It does **not** use the Accessibility service to tap or read your screen.
- It does **not** have internet access, accounts or analytics.
- It does **not** read your messages, calls or contacts.
