# AutoMate ⚡

All-in-one Android automation: build **"When X happens → do Y"** rules. Offline, no accounts, no ads.

## Features
- **11 triggers**: time & days, battery low, charger on/off, Wi-Fi on/off, Bluetooth on/off, arrive/leave a place, phone unlocked
- **12 actions**: notification, toast, open app, open link, vibrate, flashlight, ringer mode, Wi-Fi panel, **open social app/profile, share sheet, WhatsApp chat, copy text**
- **Screens**: Home, Rules, Rule editor, Activity log, Learn (articles), **Social hub** (14 app shortcuts, share, social recipes), Settings (+ permission center), About, Onboarding
- **Look & feel**: Material You, Dark AMOLED, colorful gradient, minimal light; confetti, glowing engine card, bouncy navigation, shimmer loading; pop-up and system toasts
- Kotlin + Jetpack Compose UI, Java background engine

## Build on GitHub
1. Create a repo and upload this whole folder (keep `.github/workflows/build.yml`).
2. Open **Actions → Build AutoMate APK** (it also runs on every push).
3. Download the `AutoMate-APKs` artifact. Push a tag such as `v1.2.0` to publish a Release with the APKs.

No Gradle wrapper is needed: the workflow installs Gradle 8.7 itself.

## Documentation
All articles live in [`docs/`](docs/README.md) and are also shown inside the app (Learn tab).

| Guides | Limitations | Rules & policies |
|---|---|---|
| [Getting started](docs/guides/01-getting-started.md) | [Android restrictions](docs/limitations/01-android-restrictions.md) | [Rules of use](docs/rules/01-rules-of-use.md) |
| [Triggers](docs/guides/02-triggers.md) | [Accuracy & battery](docs/limitations/02-accuracy-and-battery.md) | [Privacy](docs/rules/02-privacy.md) |
| [Actions](docs/guides/03-actions.md) | [Not supported yet](docs/limitations/03-not-supported.md) | [Disclaimer & license](docs/rules/03-disclaimer-and-license.md) |
| [Location rules](docs/guides/04-location-rules.md) | | |
| [Keep the engine running](docs/guides/05-keep-engine-alive.md) | | |
| [Recipes](docs/guides/06-recipes.md) | [Social media limits](docs/limitations/04-social-media-limits.md) | |
| [Social media automation](docs/guides/08-social-automation.md) | | |
| [Troubleshooting](docs/guides/07-troubleshooting.md) | | |

## Limitations at a glance
- Android does not let normal apps switch Wi-Fi, Bluetooth, mobile data or airplane mode.
- Opening apps from the background needs "Display over other apps", otherwise you get a tap-to-open notification.
- Silent mode needs Do Not Disturb access; Wi-Fi/Bluetooth names need location / Nearby devices permissions.
- AutoMate can open social apps and prefill text, but never posts, likes or sends for you.
- Some phone brands stop background apps; see the keep-alive guide.

Full list: [docs/limitations](docs/limitations/01-android-restrictions.md).

## Community
[Contributing](CONTRIBUTING.md) · [Code of Conduct](CODE_OF_CONDUCT.md) · [Security](SECURITY.md) · [Changelog](CHANGELOG.md) · [MIT License](LICENSE)
