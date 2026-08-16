# PulsePoint — Architecture

PulsePoint is a health & wellbeing Android app with two sections: **Vitality** (body metrics) and **Training Studio** (assigned workout programs). It is fully offline-capable: Room is the single source of truth, and a lightweight Node demo server (port 8765) provides trial data the app syncs from over HTTP.

## 1. Overview

```
┌─────────────────────────── Android app (Kotlin) ───────────────────────────┐
│                                                                             │
│  MainActivity ── Scaffold + NavHost                                         │
│    ├── VitalityScreen        (route: "vitality")                            │
│    └── TrainingScreen        (route: "training")                            │
│         └── WorkoutDetailScreen (route: "training/{workoutId}")             │
│                                                                             │
│  ViewModels (manual DI via AppContainer)                                    │
│    VitalityViewModel ──combine──► Room Flows + connectivity + sync state    │
│    TrainingViewModel           Room Flow of workouts                        │
│    WorkoutDetailViewModel      Room Flow of one workout with exercises      │
│                                                                             │
│  Data layer                                                                 │
│    HealthRepository      sync() + Room flow, updates prefs                  │
│    WorkoutRepository     sync() + Room flow                                 │
│    UserPreferences       DataStore prefs (lastSync, chartRange, profile)    │
│                                                                             │
│  Local        Room: HealthSnapshot, Workout, Exercise, ExerciseSet          │
│  Remote       Retrofit ApiService → Node demo server :8765                  │
│  Sync         WorkManager SyncWorker (periodic 6h) + pull-to-refresh        │
└─────────────────────────────────────────────────────────────────────────────┘

                    │  GET /api/metrics  │  GET /api/workouts
                    ▼                    ▼
┌─────────────────── Node demo server (:8765) ────────────────────────────────┐
│  server.ts  zero-dependency http server                                     │
│  ─ reads demo-data.json if present, else generates fresh data               │
│  ─ serves demo-server.html data console (CSS + TypeScript)                 │
└─────────────────────────────────────────────────────────────────────────────┘
```

## 2. Android app (Kotlin, Jetpack Compose)

### 2.1 Stack & versions

| Concern | Choice |
| --- | --- |
| UI | Jetpack Compose, Material 3 (BOM 2024.02.00), single activity |
| Charts | Vico **1.13.1** (`compose` + `compose-m3`) |
| Persistence | Room 2.6.1 (entities + DAOs + relations) |
| Network | Retrofit 2.9.0, OkHttp 4.12.0, kotlinx-serialization via converter |
| Offline first | Room is source of truth; network writes land in Room first |
| Background sync | WorkManager 2.9.0, periodic 6 h `SyncWorker` |
| DI | Manual `AppContainer` held by `PulsePointApplication` |
| Build | AGP 8.2.2, Kotlin 1.9.22, KSP 1.9.22-1.0.17, Gradle 8.2.1 |
| API baseline | minSdk 26, target/compile 34 |

### 2.2 Modules & packages

```
com.pulsepoint.app/
├── PulsePointApplication.kt        # owns AppContainer, schedules periodic sync
├── MainActivity.kt                 # Scaffold + bottom nav + NavHost
├── core/
│   ├── di/AppContainer.kt          # Retrofit, Room DB, repositories, prefs
│   ├── network/                    # NetworkClient, ApiService, ConnectionMonitor,
│   │                               #   HealthSnapshotDto, WorkoutDtos
│   ├── local/                      # AppDatabase, DAOs, entities, relations, Converters
│   ├── data/                       # HealthRepository, WorkoutRepository, UserPreferences
│   ├── sync/                       # SyncWorker, SyncScheduler
│   └── util/DateFormatting.kt
└── ui/
    ├── theme/                      # Color.kt, Theme.kt (light + dark)
    ├── navigation/Destinations.kt
    ├── components/StateViews.kt    # Loading / Error / Empty / OfflineBanner
    ├── vitality/                   # VitalityViewModel, MetricType, VitalityScreen,
    │                               #   components/{MetricChart,MetricCard,SummaryHeader}
    └── training/                   # TrainingViewModel, WorkoutDetailViewModel,
                                    #   TrainingScreen, WorkoutDetailScreen,
                                    #   components/WorkoutComponents
```

### 2.3 Navigation

- Bottom navigation has two items: **Vitality** and **Training Studio**.
- Routes: `vitality`, `training`, `training/{workoutId}`.
- Single activity; screens are Composable destinations in one `NavHost`.

### 2.4 Data model

Health snapshot (per day, pre-calculated by the server — the app never computes health values):

| Field | Type |
| --- | --- |
| date | ISO `yyyy-MM-dd` (epoch-day key) |
| weightKg / heightCm | Double |
| muscleMassPct / fatPct | Double |
| bmi | Double |
| muscleWeightKg | Double |
| age | Int |
| gender | String (`male`/`female`) |
| bmr | Int (kcal) |

Workout program:

```
Workout(id, title, subtitle, muscleGroups[], weeklyFrequency, isActive)
  └── Exercise(id, name, muscleGroup)
        └── ExerciseSet(setOrder, reps, weightKg, restSec)
```

Room relations: `WorkoutWithExercises`, `ExerciseWithSets`. The app treats the latest snapshot's `gender`, `age`, `bmr` and `heightCm` as the cached profile in DataStore.

### 2.5 Sync strategy

- `HealthRepository.refresh()` GETs `/api/metrics`, clears and reinserts into Room, then updates `lastSyncEpochMillis` and the cached profile in DataStore. Returns a `SyncResult`.
- `WorkoutRepository.refresh()` GETs `/api/workouts` and replaces the workout tables in one Room transaction.
- `SyncScheduler` enqueues a periodic 6 h `SyncWorker`; the Vitality screen also triggers a sync when Room is empty on first launch and exposes pull-to-refresh.
- Offline behaviour: the UI only observes Room `Flow`s, so charts and workout lists render from cache with no connection. An `OfflineBanner` appears when `ConnectionMonitor` reports no connectivity.

### 2.6 Vitality UI

- 7 / 30 / 90 day segmented range selector (persisted to DataStore).
- Summary header: latest weight, muscle %, fat %, BMI + delta badges (▲/▼).
- One chart + card per `MetricType`: WEIGHT, MUSCLE_WEIGHT, MUSCLE_MASS, FAT_PERCENT, BMI, BMR. Charts are Vico line charts with a gradient fill and an epoch-day axis formatter.

### 2.7 Training UI

- Workout cards with muscle-group chips (FlowRow), weekly frequency, exercise count.
- Detail screen lists exercises with prescribed sets (reps × weight, rest); a "mark complete" action records a session, then shows a Snackbar confirmation.

## 3. Demo server (Node, TypeScript)

### 3.1 Structure

```
demo-server/
├── package.json                  # scripts: build / start / serve
├── tsconfig.json                 # server compile (node16, @types/node)
├── tsconfig.console.json         # console type-check (DOM lib)
├── scripts/build.mjs             # tsc + esbuild + template assembly
├── demo-server.template.html     # CSS + embedded TypeScript source
├── src/
│   ├── types.ts                  # shared DTOs (mirrors app JSON contract)
│   ├── generator.ts              # metric history + workout seed generation
│   ├── console.ts                # browser console logic (TypeScript)
│   └── server.ts                 # zero-dependency HTTP server
├── dist/                         # build output (gitignored)
└── demo-data.json                # generated payload (gitignored)
```

### 3.2 Build pipeline

`scripts/build.mjs`:

1. `tsc -p tsconfig.json` → compiles `server.ts` to CommonJS in `dist/`.
2. `esbuild src/console.ts --bundle --minify` → `dist/console.bundle.js`.
3. Reads the HTML template and inlines both the minified console bundle and the readable TypeScript source (shown inside the page under a `<details>`), writing `demo-server.html`.

The final `demo-server.html` is therefore a single self-contained file — pure CSS for styling, with its logic written in TypeScript (source embedded) and compiled via esbuild.

### 3.3 Endpoints (default `http://10.0.2.2:8765/`)

| Endpoint | Method | Purpose |
| --- | --- | --- |
| `/` | GET | Serve `demo-server.html` data console |
| `/api/health` | GET | Health check + data summary |
| `/api/metrics` | GET | Array of daily `HealthSnapshot` |
| `/api/workouts` | GET | Array of workout programs |
| `/api/data` | POST | Replace in-memory metrics/workouts (used by future tooling) |

CORS is enabled (`Access-Control-Allow-Origin: *`) so the Android emulator (`10.0.2.2`) and any origin can call the APIs. On startup the server reads `demo-data.json`; if it is absent it regenerates the 120-day metric history and the 4-workout program and writes the file.

### 3.4 Data generation

- `generateMetricHistory({ days })`: a 29-year-old male, 178 cm, walking from 87.4 kg down to 81.6 kg over 120 days, with fat % decreasing and muscle % increasing and daily noise.
- `generateWorkouts()`: **Push Day** (2/wk), **Pull Day** (2/wk), **Leg Day** (2/wk), **Core & Mobility** (1/wk) — 11 exercises, 37 prescribed sets in total.

### 3.5 Data console (CSS + TypeScript)

The console page lets a developer preview and tune what the app receives:

- Live mode: when served by the running Node server it shows the server's actual payload.
- Latest-snapshot editor: edit weight/height/fat/muscle/age/gender; BMI, muscle weight and BMR are recomputed exactly as a real backend would.
- History generator: regenerate 7–365 days of metric history.
- JSON previews of `/api/metrics`, `/api/workouts` and the full bundle with copy buttons, plus a one-click download of `demo-data.json`.

## 4. Build & run

```bash
# Demo server
cd demo-server
npm run build      # tsc + esbuild, produces demo-server.html
npm start          # node dist/server.js on :8765
```

```bash
# Android app
# Open PulsePoint/ in Android Studio and build the debug APK.
# Base URL is BuildConfig.BASE_URL = http://10.0.2.2:8765/ (emulator loopback).
```

## 5. Design spec

See `docs/superpowers/specs/2026-08-16-pulsepoint-design.md` for the approved UX/design decisions behind this architecture.
