# Aviator HOC – Android (Kotlin + Jetpack Compose)

Native rewrite of the web app. Local only (Room database on the phone), no Firebase, no logo.
UI direction: **D · Neon Mint (OLED)**.

## Build the APK on GitHub (phone only, from Termux)

```bash
cd aviator-hoc-android
git init -b main
git add .
git commit -m "Part 1"
git remote add origin https://github.com/tharindu899/<repo>.git
git push -u origin main
```

Open the repo → **Actions** → **Build APK** → open the finished run → download the
`AviatorHOC-apk-N` artifact → install `AviatorHOC-debug-N.apk`.

## Optional
* **Sora font** – CI downloads it automatically. If the download fails the app uses the system font.
* **Signed release** – add secrets `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`,
  then run the workflow manually with *release* ticked.

## Parts
1. Project, theme, Room DB, signal logic, CI ← done
2. **Home screen** (hero countdown, flowing bar, upcoming list, WIN/LOSS strip, animated nav) ← done
3. **Generate screen** (paste, live preview, examples, validation) ← done
4. **Results screen** (filters, search, sort, edit status, delete, export) ← done
5. **Settings sheet** (name, auto-mark missed, live countdown, sound alerts, vibration, reset), beeps + buzz when a window opens / WIN-LOSS appears ← done
6. **Signal notifications** (sound alert 1 min before a signal, works with the app closed, tap opens the app) ← done
7. **Day theme** (Auto / Day / Night in Settings) + smoother tab switching; CI now always builds an optimised release APK (use this one, debug is laggy) ← done
8. **In-app updater** (checks GitHub Releases, downloads and installs the new APK from Settings; dot on the settings button when an update exists) ← done

Alerts work while the app is open (foreground).
