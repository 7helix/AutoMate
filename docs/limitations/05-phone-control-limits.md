# Phone control limits

Phone control is powerful, but Android keeps important limits in place.

## Still not possible
- Turning **Wi-Fi, Bluetooth, mobile data or airplane mode** on or off.
- Reading the screen, tapping on-screen buttons or typing text.
- Answering or ending calls, or sending SMS without your tap.
- Changing system settings that Android reserves for the manufacturer.

## Depends on permissions
- **Brightness** needs "Modify system settings".
- **Silent mode and Do Not Disturb** need Do Not Disturb access.
- **System buttons** need the Accessibility service to be on.
- **Lock screen and screenshot** need Android 9 or newer.
- **Speak text** needs a text-to-speech voice installed on the phone.

## Depends on your phone
- Some manufacturers limit or reset accessibility services in battery saver modes.
- Some phones restrict volume changes for the ring stream when Do Not Disturb is active.
- Pressing a system button while another app is in full-screen mode may behave differently.

## Timing
- Repeat rules under 10 minutes can be delayed while the phone is idle.
- Steps with waits run on a background thread. Keep waits short for time rules that run when the engine is off.

## Why these limits exist
They protect your privacy and security. Without them, any app could control your phone silently.
