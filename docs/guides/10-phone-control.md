# Phone control

The Phone control page and the new actions let AutoMate change settings and press system buttons for you.

## What you can control
- **Volume**: media, ring, alarm or alert volume as a percentage.
- **Brightness**: set the screen brightness. Needs the **Modify system settings** permission.
- **Ringer mode** and **Do Not Disturb**: both need Do Not Disturb access.
- **Flashlight**, **camera**, and **settings pages** such as Wi-Fi, Bluetooth, Display and Battery saver.
- **System buttons**: Home, Back, Recent apps, notification shade, quick settings, lock screen, screenshot and power menu.
- **Speak text** aloud with your phone's voice.

## Turning on system buttons
System buttons use Android's **Accessibility service**. It is optional and off by default.
1. Open **Home > Phone control**.
2. Tap **Open Accessibility settings**.
3. Choose **AutoMate** under installed or downloaded apps.
4. Switch it on and confirm the Android prompt.

The Phone control page shows whether the service is on, and lets you try each button.

## What the service does and does not do
- It only presses system buttons when one of your rules asks.
- It cannot read your screen, your messages or what you type.
- You can switch it off at any time in Android settings.

## Safety tips
- Test new rules with **Test action** first.
- Avoid rules that lock the screen or press Home at times you may be using the phone.

> Note: Android still does not allow normal apps to switch Wi-Fi, Bluetooth, mobile data or airplane mode on or off. See Phone control limits.
