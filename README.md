# PulsePoint

A health & wellbeing Android app (Kotlin, Jetpack Compose) with two sections:

- **Vitality** — "Your body in numbers": weight, muscle, fat, BMI and BMR trends as charts.
- **Training Studio** — "Your assigned routines, one tap from done": prescribed workout programs with per-set details.

The app is **offline-first**: Room is the single source of truth, and a lightweight **Node demo server** (CSS + TypeScript) supplies trial data the app syncs from.

```
PulsePoint/       Kotlin Android project (exportable as APK from Android Studio)
demo-server/      Node demo server + data console (demo-server.html)
docs/             Design spec + architecture documentation
```

## 1. Android app

### Prerequisites

- Android Studio (Hedgehog or newer recommended), JDK 17
- Android SDK Platform 34, Build-Tools 34

### Build the APK

```bash
# 1. Open the project
#    Android Studio → Open → select the PulsePoint/ folder

# 2. Build the debug APK
#    Build → Build Bundle(s) / APK(s) → Build APK(s)
#    Output: PulsePoint/app/build/outputs/apk/debug/app-debug.apk

# or from the command line (requires JDK + Android SDK in PATH / local.properties)
cd PulsePoint
./gradlew assembleDebug
```

Notes:

- `app/build.gradle.kts` sets the default API base URL to `http://10.0.2.2:8765/`, which is the host loopback as seen from the Android **emulator**. On a physical device, tap the **gear icon** on the Vitality screen and enter `http://<your-PC-LAN-IP>:8765/` — the server prints its LAN address when it starts. The URL is saved and used from then on.
- If the app can't reach the server it shows a clear error with the URL it tried; tap the refresh icon to retry.
- If Gradle is not available locally, the project ships with the wrapper (`gradlew`) pinned to Gradle 8.2.1.

## 2. Demo server

A zero-dependency Node server (port 8765) that the app syncs from, plus a styled data console page written in **CSS + TypeScript**.

```bash
cd demo-server
npm run build        # compile TypeScript: server → dist/, console → demo-server.html
npm start            # node dist/server.js, serves on :8765
```

Endpoints:

| Endpoint | Purpose |
| --- | --- |
| `http://localhost:8765/` | Data console (demo-server.html) |
| `http://localhost:8765/api/health` | Health check + summary |
| `http://localhost:8765/api/metrics` | Daily health snapshots |
| `http://localhost:8765/api/workouts` | Workout programs |

On first start the server generates `demo-data.json` (120 days of metrics, 4 workout programs). Delete it to force regeneration.

The console lets you edit the latest snapshot (BMI / muscle weight / BMR are recomputed server-side), regenerate the history, inspect the JSON the app receives, and download the payload.

## 3. Documentation

- `docs/architecture.md` — full architecture (layers, sync strategy, UI, server, build).
- `docs/superpowers/specs/2026-08-16-pulsepoint-design.md` — approved design spec.

## 4. Tech stack

- Jetpack Compose (Material 3), single activity, bottom navigation
- Vico 1.13.1 charts
- Room 2.6.1 (offline-first), DataStore preferences
- Retrofit 2.9.0 + OkHttp 4.12.0
- WorkManager 2.9.0 (periodic 6 h sync)
- Node.js demo server written in TypeScript (bundled with esbuild)
