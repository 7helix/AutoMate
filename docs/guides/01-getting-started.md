# Getting started with AutoMate

AutoMate runs **rules**. A rule is simple: **When** something happens, **then** do something.

## Your first rule in 60 seconds
1. Open the **Rules** tab and tap **New rule**.
2. Name it, for example `Low battery`.
3. Under **When**, choose **Battery gets low** and set it to 15%.
4. Under **Then**, choose **Send a notification** and type a message.
5. Tap **Test action** to see it work, then **Save rule**.
6. Go to **Home** and switch the **engine** on.

## What is the engine?
Time rules use the system alarm clock and keep working even when the engine is off. Battery, charger, Wi-Fi, Bluetooth, unlock and location rules are watched by the **engine**, a small foreground service that shows a notification while it runs.

## Good habits
- Start from a **Quick start** template on Home, then tweak it.
- Use **Test action** before you trust a rule.
- Look at the **Activity** tab to see exactly what ran and why.

> Tip: if something does not fire, read the Troubleshooting article before changing the rule.
