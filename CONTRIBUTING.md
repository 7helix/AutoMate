# Contributing to AutoMate

Thanks for helping! Small, focused pull requests are easiest to review.

## Setup
1. Fork and clone the repo.
2. Open it in Android Studio (JDK 17), or push your fork and let GitHub Actions build it.
3. Run `gradle assembleDebug`.

## Project layout
- `app/src/main/java/.../ui` – Jetpack Compose screens (Kotlin)
- `app/src/main/java/.../engine` – background engine, alarms, actions (Java)
- `app/src/main/java/.../data` – models and local storage (Kotlin)
- `docs/` – articles shown in the app **and** on GitHub

## Adding or editing an article
1. Edit or add a `.md` file under `docs/guides`, `docs/limitations` or `docs/rules`.
2. For a new one, add an entry in `data/Docs.kt`. CI fails if a referenced file is missing.
3. Supported markdown: `#`, `##`, `###`, lists, numbered lists, `> notes`, `**bold**`, `` `code` ``.

## Adding a trigger or action
1. Add it to the enum in `data/Models.kt` and its texts in `whenText()` / `thenText()`.
2. Handle it in the Java engine (`AutomationService`, `ActionRunner`).
3. Add its fields to the editor in `EditRuleScreen.kt` and mention it in the docs.

## Pull request checklist
- [ ] Builds with `gradle assembleDebug`
- [ ] Docs updated if behaviour changed
- [ ] New permissions are explained in `docs/rules/02-privacy.md`
