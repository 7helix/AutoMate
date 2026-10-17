# Troubleshooting

## A rule did not run
1. Is the rule switched on in the Rules tab?
2. For anything except time rules: is the **engine** running on Home?
3. Open **Activity**. Errors appear there in red with the reason.
4. Tap **Test action** on the rule. If the test works, the action is fine and the trigger is the issue.

## Notifications do not appear
Allow **Notifications** in Settings > Permissions and check the phone's notification settings for AutoMate.

## Time rules run late
On Android 12 and newer, allow **Exact alarms**. Without it, Android may delay alarms by minutes.

## Wi-Fi name rules never match
Reading the network name needs **precise location** permission and location services switched on.

## Bluetooth name rules never match
Allow **Nearby devices** and check the exact device name in your phone's Bluetooth list.

## Apps do not open
Allow **Display over other apps**, otherwise AutoMate shows a notification you must tap.

## Silent mode fails
Allow **Do Not Disturb access** in Settings > Permissions.

## Still stuck?
Open an issue on GitHub with your Android version, phone model and the Activity log lines.
