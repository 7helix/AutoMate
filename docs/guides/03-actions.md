# Actions explained

An action is the **Then** part of a rule.

## Available actions
- **Send a notification**: a heads-up notification with your text. Tapping it opens AutoMate.
- **Show a toast message**: a short pop-up message.
- **Open an app**: pick any installed app with a launcher icon.
- **Open a link**: opens a web address in your browser.
- **Vibrate the phone**: one short vibration.
- **Toggle flashlight**: switches the rear flash on or off.
- **Change ringer mode**: silent, vibrate or normal.
- **Open Wi-Fi panel**: shows the system internet panel so you can switch networks yourself.

## Opening apps and links
Android restricts apps from opening other apps while in the background. AutoMate opens them directly when AutoMate is on screen or **Display over other apps** is allowed. Otherwise it sends a notification and you tap it to continue.

## Silent mode
Setting the ringer to **silent** needs **Do Not Disturb access**. Vibrate and normal do not.

## Flashlight
AutoMate remembers the torch state itself. If you switch the flashlight with another app, the next toggle may do the opposite of what you expect.

## Social and sharing actions
- **Open a social app or profile**: pick a platform and optionally a username or link.
- **Open share sheet with text**: shows Android's share menu with your text ready.
- **Open WhatsApp chat with text**: opens a chat with a number and message ready. You press send.
- **Copy text to clipboard**: copies your text so you can paste it anywhere.

These actions open apps, so the same background launch rules apply. See the Social media limits article.

## Sound, display and phone control (version 1.3)
- **Speak text aloud**: uses your phone's text-to-speech voice.
- **Set a volume level**: media, ring, alarm or alerts, from 0 to 100%.
- **Set screen brightness**: needs "Modify system settings".
- **Change Do Not Disturb**: block all, priority only, or off. Needs Do Not Disturb access.
- **Play a notification sound**.
- **Open the camera** or **a settings page**.
- **Press a system button**: needs the Accessibility service. See Phone control.
- **Open dialer, start a text message, start an email**: the app opens with your text ready. You press send.
