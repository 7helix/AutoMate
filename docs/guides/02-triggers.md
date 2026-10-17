# Triggers explained

A trigger is the **When** part of a rule.

## Available triggers
- **At a specific time**: pick a time and the days of the week. Uses the system alarm clock.
- **Battery gets low**: fires once when the level drops to your threshold while not charging. It re-arms after the battery rises above the threshold or you plug in.
- **Charger connected / disconnected**: fires when power is plugged in or removed.
- **Wi-Fi connected / disconnected**: optional network name filter. Leave it empty for any network.
- **Bluetooth connected / disconnected**: optional device name filter. Leave it empty for any device.
- **Arrive at / Leave a place**: a saved latitude, longitude and radius.
- **Phone unlocked**: fires each time you unlock the screen.

## More triggers (version 1.3)
- **Repeat at an interval**: every 5 minutes up to 12 hours. Pair it with conditions such as a time window.
- **Screen turns on / off**: fires every time the screen changes state.
- **Airplane mode turned on / off**.
- **Wired headphones plugged in / unplugged**: Bluetooth headphones use the Bluetooth trigger.
- **Battery saver turns on / off**.
- **Phone starts up**: runs after a restart.

Event triggers need the engine to be running. Time and repeat rules work without it.

## Name filters
Filters are not case sensitive but must match the whole name, so `Home WiFi` will not match `Home WiFi 5G`.

## Things to know
- Wi-Fi and Bluetooth **names** need extra permissions. See the Limitations section.
- The engine ignores the "already connected" Wi-Fi event in the first few seconds after it starts, so starting the engine does not trigger a "Wi-Fi connected" rule by itself.
