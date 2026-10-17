# Conditions and multi-step rules

Version 1.3 makes rules smarter in two ways: you can add **conditions**, and you can chain **several steps**.

## Conditions ("Only if")
A condition is a check that must pass at the moment the trigger fires. If it fails, the rule is skipped and the Activity log says why.

Available conditions:
- **Time is between** two times. Overnight windows such as 22:00 to 06:00 work.
- **Day of the week** is one of the days you select.
- **Battery** is above or below a level.
- **Phone is charging** or **not charging**.
- **Wi-Fi** is connected, not connected, or connected to a specific network name.
- **Airplane mode** is on or off.
- **Screen is on**.
- **Do Not Disturb is on**.

With two or more conditions you can choose **All must be true** or **Any can be true**.

## Example
*Hydration reminder*: every 60 minutes, but only between 09:00 and 21:00.

## Multiple steps
A rule can run up to 10 steps in order. Use the arrows to reorder them. From the second step onward you can add a **wait** of up to 60 seconds.

Example *Car mode*: set the media volume to 70%, wait 2 seconds, then open Maps.

## Good to know
- A failed step does not stop the later steps. The Activity log shows each result.
- **Test action** always runs every step and ignores conditions, so you can check your steps at any time.
- Waits work reliably while the engine is running. A time rule that runs with the engine off should keep its total waiting time under about 8 seconds.

> Tip: keep rules short. Two small rules are easier to maintain than one long rule.
