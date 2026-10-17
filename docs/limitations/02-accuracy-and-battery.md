# Accuracy, timing and battery

## Timing
- **Time rules** use exact alarms when allowed. Without the permission, Android can delay them.
- **Doze mode** can postpone background work when the phone sits still for a long time. Exact alarms still fire, but other triggers may arrive late.

## Location accuracy
- GPS is less exact indoors and between tall buildings.
- A rule fires when your position **changes** between inside and outside. If your position jumps near the border, rules can fire twice.
- Use a radius of at least 100 metres.

## Battery use
- The engine itself is light. **Location rules** are the main cost because the phone keeps asking for position updates.
- Fewer enabled rules means less work.

## Battery rule behaviour
The low battery rule fires once per drop and arms again after the battery rises above the threshold or the phone starts charging.

## Run history
AutoMate keeps the latest 200 log lines. Older ones are removed automatically.
