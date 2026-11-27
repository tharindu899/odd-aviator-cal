<div align="center">

<img src="docs/icon.png" width="120" alt="Aviator HOC icon" />

# ⚡ Aviator HOC

**A fast, offline signal-time tracker for Android — built with Kotlin & Jetpack Compose**

![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B-3DDC84?logo=android&logoColor=white&style=for-the-badge)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white&style=for-the-badge)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white&style=for-the-badge)
![Room](https://img.shields.io/badge/Storage-Room%20(local)-B6FF5C?style=for-the-badge&labelColor=000000)
![CI](https://img.shields.io/badge/Build-GitHub%20Actions-2088FF?logo=githubactions&logoColor=white&style=for-the-badge)
![Offline](https://img.shields.io/badge/Data-100%25%20on--device-success?style=for-the-badge)

[![Latest APK](https://img.shields.io/github/v/release/tharindu899/odd-aviator-cal?label=Latest%20APK&logo=android&logoColor=white&style=for-the-badge&color=B6FF5C&labelColor=000000)](https://github.com/tharindu899/odd-aviator-cal/releases/latest)
[![Download APK](https://img.shields.io/badge/Download-APK-B6FF5C?logo=android&logoColor=black&style=for-the-badge&labelColor=000000)](https://github.com/tharindu899/odd-aviator-cal/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/tharindu899/odd-aviator-cal/total?label=Downloads&style=for-the-badge&color=2088FF&labelColor=000000)](https://github.com/tharindu899/odd-aviator-cal/releases)

<br><br>

<a href="https://github.com/tharindu899/odd-aviator-cal/releases/latest/download/AviatorHOC-latest.apk">
  <img src="docs/download-apk.svg" width="320" alt="Download latest APK" />
</a>

</div>

---

### ⚠️ Disclaimer — please read first

> [!WARNING]
> This app is a **time calculator and personal log**. It does **not** predict, guarantee or influence the result of any game.
>
> * 🎲 Betting and gambling can cause **real financial loss** and can be addictive. **Never risk money you cannot afford to lose.**
> * 🧪 This software is provided **"as is", without warranty of any kind**. Use it **entirely at your own risk**.
> * 🙅 The author takes **no responsibility** for any loss, damage, decision or consequence of any kind that results from using, misusing or relying on this app, its numbers, notifications or sounds.
> * 🔞 Follow the laws and age limits that apply where you live.
> * 🏷️ Not affiliated with, endorsed by or connected to any game, operator or brand.
>
> **If you do not accept this, please do not install or use the app.**

---

## ✨ Features

| | |
|---|---|
| 🧮 **Signal calculator** | Paste text like `2.02x 21:31:22` and get the exact signal time |
| ⏱️ **Live hero countdown** | Big countdown, smooth flowing progress bar, 45 s bet window before / after the signal |
| ✅ **Quick WIN / LOSS** | Buttons spring up 25 s before the window closes, leave 10 s after (adjustable in Settings) |
| 🤖 **Auto-mark missed** | Unmarked signals become *Missed* automatically (optional) |
| 🔔 **Notifications** | Sound alert about **1 minute before a signal**, even when the app is closed — tap to open |
| 🔊 **Beeps & vibration** | In-app beep / buzz when the window opens and when WIN / LOSS appears |
| 📊 **Results** | Filters, search, sort, edit status, delete, **JSON export** |
| 🌗 **Day / Night / Auto** | Neon Mint OLED night theme + a soft day theme |
| 🔄 **In-app updater** | Checks GitHub Releases on launch, downloads and installs with an animated popup |
| 💾 **Local data** | Everything is stored in a Room database on your phone (no account, no cloud, no tracking) |

## 🧠 How the time is calculated

```
signal time = original time + oddToTime(odd)
window      = signal − 45 s  …  signal + 45 s
```

`oddToTime` is a 1:1 port of the original web app's logic and is covered by unit tests.

## 🛠️ Tech stack

* 🟣 Kotlin 2.0, Jetpack Compose (Material 3, custom animations)
* 🗄️ Room + DataStore (local only)
* ⏰ AlarmManager + notifications for background alerts
* 🤖 GitHub Actions for tests and APK builds

## 📦 Build the APK on GitHub (phone only, from Termux)

```bash
cd aviator-hoc-android
git init -b main
git add .
git commit -m "Initial commit"
git remote add origin https://github.com/tharindu899/odd-aviator-cal.git
git push -u origin main
```

1. Open the repo → **Actions** → **Build APK** → open the finished run.
2. Download the `AviatorHOC-apk-N` artifact.
3. Install **`AviatorHOC-vX.Y.Z-release.apk`** (optimised, smooth). The debug APK is slower.

### 🔐 Signed release & in-app updates

Add these repository **secrets**:

| Secret | Meaning |
|---|---|
| `KEYSTORE_BASE64` | your keystore file, base64 encoded |
| `KEYSTORE_PASSWORD` | keystore password |
| `KEY_ALIAS` | key alias |
| `KEY_PASSWORD` | key password |

With the keystore present, every push also publishes a **GitHub Release** (`build-N`) that the in-app updater reads.

> [!IMPORTANT]
> * Updates only install if every build is signed with the **same key** — keep your keystore safe.
> * The updater reads releases without logging in, so the repository (or a dedicated releases repo) must be **public**.
> * Install the very first build manually; later updates come from inside the app.

### 🔤 Optional font
CI downloads the *Sora* font automatically. If that fails the app falls back to the system font.

## 🗺️ Roadmap (done ✅)

- [x] Project, theme, Room DB, signal logic, CI
- [x] Home screen — hero countdown, flowing bar, upcoming list, WIN/LOSS strip
- [x] Generate screen — paste, live preview, validation
- [x] Results screen — filters, search, sort, export
- [x] Settings page — name, theme, toggles, reset
- [x] Sounds, vibration and background notifications
- [x] Day theme & smoother navigation
- [x] In-app updater with animated popup
- [x] New adaptive app icon

## 🔒 Privacy

No accounts, no analytics, no ads. Your signals never leave the phone. The only network use is the optional update check against GitHub Releases.

## 📄 License & liability

Provided **as is**, with **no warranty** and **no liability** for the author or contributors.
By installing or using this app you accept that you do so **at your own risk**.

<div align="center">

Made with 💚 using Kotlin & Compose

</div>
