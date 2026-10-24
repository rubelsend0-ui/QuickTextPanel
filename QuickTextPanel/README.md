# QuickText Panel

Template manager for Android with a custom keyboard, an optional floating panel and an in-app Test Lab.
Kotlin · Jetpack Compose · Material 3 · MVVM · Hilt · Room · DataStore · Coroutines.

## What it does
- **Templates**: boxes + templates (create, rename, delete, duplicate, search, favorite, move between boxes), emoji / multiline / Unicode, live character and word counts. Stored locally with Room.
- **Dashboard**: animated statistics, dark / light / system theme (DataStore).
- **Keyboard** (`QuickTextIme`): QWERTY, symbols, backspace (hold to repeat), enter, box picker and saved templates. A template is inserted only when you tap it. Disabled in password fields. Keystrokes are never stored or sent.
- **Floating panel** (`FloatingPanelService`): optional, movable bubble with search. Tapping a template copies it to the clipboard so you paste it yourself. Permission is explained before it is requested; it can be shut down from the panel or from Setup.
- **Test Lab**: repeats text with limits (max 200 reps, 50–2000 ms) strictly inside its own preview, with progress and logs. It never touches other apps.
- **Privacy screen**; the app has no INTERNET permission. Only permission: `SYSTEM_ALERT_WINDOW` (optional panel).

## Build
Requirements: JDK 17, Android SDK (platform 34), Gradle 8.9.

```bash
# one-time: generate the Gradle wrapper (the wrapper jar is not bundled in this zip)
gradle wrapper --gradle-version 8.9

./gradlew testDebugUnitTest      # unit tests
./gradlew assembleDebug          # debug APK
```
APK output: `app/build/outputs/apk/debug/app-debug.apk`

Without the wrapper you can run the same tasks with a local Gradle 8.9: `gradle testDebugUnitTest assembleDebug`.

## CI
`.github/workflows/android.yml` sets up Java 17, the Android SDK and Gradle 8.9, runs the unit tests, builds the debug APK and uploads it as the `quicktext-panel-debug-apk` artifact.

## Verification status
This project was written without network access, so **it has not been compiled or run yet**. Run the CI workflow (or the commands above) first and fix anything the compiler reports; the first real build is the first verification.

## Layout
```
app/src/main/java/com/quicktext/panel/
  data/local        Room entities, DAOs, database
  data/repository  TemplateRepository, SettingsRepository (DataStore)
  di               Hilt module + service entry point
  keyboard         QuickTextIme + Compose keyboard
  service          FloatingPanelService + overlay UI
  ui/...           theme, components, dashboard, templates, setup, lab, privacy, navigation
  util             text stats, password detection, service lifecycle owner
app/src/test        unit tests
```
