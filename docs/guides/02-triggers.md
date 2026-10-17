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

## Name filters
Filters are not case sensitive but must match the whole name, so `Home WiFi` will not match `Home WiFi 5G`.

## Things to know
- Wi-Fi and Bluetooth **names** need extra permissions. See the Limitations section.
- The engine ignores the "already connected" Wi-Fi event in the first few seconds after it starts, so starting the engine does not trigger a "Wi-Fi connected" rule by itself.
