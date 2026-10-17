# Location rules

Arrive and leave rules compare your position with a saved point and radius.

## Setting one up
1. Stand at the place and tap **Use my current location** (open a maps app first if no position is found).
2. Choose a radius. **100 to 300 metres** is a good start.
3. Allow **precise location**. For rules that must work with the screen off, also allow **Location all the time**.
4. Start the engine on Home.

## How it works
While the engine runs, AutoMate asks the phone for location updates roughly every 20 seconds or 25 metres. The first reading only records whether you are inside or outside. A rule fires when that state **changes**.

## Tips
- Use bigger radii in cities and indoors, where GPS is less exact.
- Readings less accurate than about 200 metres are ignored to avoid false triggers.
- Location rules use more battery than other triggers. Disable rules you do not need.

> Note: AutoMate never sends your location anywhere. It has no internet permission.
