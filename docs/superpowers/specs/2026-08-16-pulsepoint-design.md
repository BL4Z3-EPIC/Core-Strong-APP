# PulsePoint — Design Spec

Date: 2026-08-16

## 1. Purpose

PulsePoint is a health and well-being Android application that:

1. Receives pre-calculated health metrics (body weight, height, muscle mass, fat percentage, BMI, muscle weight, age, gender, BMR) from a server over an existing connection.
2. Visualizes each metric as a time-based chart so users can track progress over time.
3. Lets users browse and complete the workout routines assigned by the server/host.
4. Works fully offline: every received data point and workout is persisted locally and readable with no internet connection.

The app performs no health-data calculations. It only does the simple arithmetic required to render charts (filtering date ranges, computing deltas between snapshots, formatting values).

## 2. Section Naming

The app has exactly two primary sections, exposed through a bottom navigation bar.

| Server concept | Final name | Rationale |
|---|---|---|
| Tracking section | **Vitality** | "Your body in numbers." Evokes living energy and overall wellness rather than clinical "tracking". |
| Training section | **Training Studio** | Positions the assigned routines as a personal training studio the user works within, rather than a generic "training" list. |

Rejected alternatives: "Health Timeline" (too passive), "BodyMetrics" (too clinical), "FitForge" (too aggressive), "Rep Room" (slangy).

## 3. Architecture

Pattern: **MVVM + Repository, offline-first, single-activity Jetpack Compose.**

- One Activity (`MainActivity`) hosting a Compose `Scaffold` with a two-item `NavigationBar`.
- `androidx.navigation:navigation-compose` for navigation between Vitality, Training Studio, and the workout detail screen.
- ViewModels expose immutable `UiState` via `StateFlow`; Compose renders `StateFlow` via `collectAsStateWithLifecycle`.
- ViewModels never touch the network. All reads go through `Flow` from Room, so the UI renders identically online and offline.

### Data flow

```
Demo/trial server (Node + demo-server.html)
      │  GET /api/metrics, /api/workouts   (JSON over HTTP)
      ▼
ApiService (Retrofit + Gson) ──► DTOs
      ▼
Repository.refresh()   [writes to Room in a transaction]
      ▼
Room Database (source of truth) ──Flow──► ViewModel StateFlow ──► Compose UI
      ▲
SyncWorker (WorkManager, periodic) / manual pull-to-refresh
```

### Dependency injection

Manual DI. `PulsePointApplication` builds an `AppContainer` (database, daos, repositories, network client, connection monitor, preferences, sync scheduler) and hands it to ViewModels via `AndroidViewModel`. No annotation processing for DI keeps the build simple and reliable.

## 4. Data model

### Health snapshot (one per day)

`weightKg`, `heightCm`, `muscleMassPct`, `fatPct`, `bmi`, `muscleWeightKg`, `age`, `gender`, `bmr`, plus `date` (ISO `yyyy-MM-dd`). Stored in Room keyed by epoch day.

### Workout

`Workout { id, title, subtitle, muscleGroups, weeklyFrequency, isActive, exercises[] }`
`Exercise { id, name, muscleGroup, sets[] }`
`ExerciseSet { setOrder, reps, weightKg, restSec }`

### Local tables

- `health_snapshots` — metric history
- `workouts`, `exercises`, `exercise_sets` — the assigned programs
- `workout_sessions` — completed sessions logged by the user (`workoutId`, `completedAtEpochMillis`)
- DataStore (preferences) — last-sync timestamp, preferred chart range, cached latest profile

## 5. Components

| Component | Responsibility |
|---|---|
| `MainActivity` | Single activity; hosts NavHost + bottom nav + theme. |
| `VitalityScreen` / `VitalityViewModel` | Summary header, range selector (7/30/90d), metric cards with Vico trend charts, refresh + offline banner. |
| `TrainingScreen` / `TrainingViewModel` | Workout list, detail screen (exercises + sets), "mark complete" session logging. |
| `HealthRepository` | Fetch metrics from API, persist to Room, expose Room `Flow`. |
| `WorkoutRepository` | Fetch workouts, persist, expose joined workout+exercises+sets `Flow`, log sessions. |
| `AppDatabase` + DAOs + entities | Room schema and queries. |
| `ApiService` + DTOs | Retrofit contract for `/api/metrics`, `/api/workouts`. |
| `SyncWorker` / `SyncScheduler` | Periodic background sync (network-constrained). |
| `ConnectionMonitor` | `ConnectivityManager.NetworkCallback` → offline state for the banner. |
| `AppContainer` | Manual DI graph. |

## 6. UI/UX

- **Vitality**: Top app bar (PulsePoint) with refresh; offline banner when unreachable; summary header card with latest weight, BMI, body-fat, muscle-mass, age/gender and BMR chips; segmented 7/30/90-day range selector; vertical list of metric cards, each with a mini line/area chart (Vico), current value, unit, and a delta badge vs. range start. Weight is the hero/first card.
- **Training Studio**: Card list of assigned programs (title, subtitle, muscle-group chips, frequency, exercise count, last-completed badge). Tapping opens a detail screen: each exercise with its set table (set / reps × weight / rest). A prominent "Mark complete" button logs a session; a Snackbar confirms; the list card updates with the last-completed date. Empty, loading, and error states are styled.
- New data points simply extend the Room table; charts re-render automatically because they read Room `Flow`s.

## 7. Data visualization

- **Vico** (`vico-compose`, `vico-compose-m3` 1.15.1) line/area charts.
- X axis = epoch day; Y = metric value; `CartesianValueModel.map` for series; `CartesianRange` for the selected window; bottom-axis `CartesianValueFormatter` renders dates as "MMM d".
- Delta computation and range filtering in `VitalityViewModel` (the only math the app does).
- Charts render purely from local data — fully offline.

## 8. Offline strategy

- Room is the single source of truth; UI reads only Room `Flows`.
- A full dataset is fetched on first launch and stored; every subsequent launch works offline immediately.
- Manual pull-to-refresh and a periodic `SyncWorker` (6h, network-required constraint) keep data fresh when online.
- Offline banner reflects `ConnectionMonitor` and last-sync state.

## 9. Demo/trial server

- `demo-server.html` — single self-contained page styled with CSS, logic in TypeScript (source embedded in a `<script type="text/typescript">` block, compiled bundle inlined). Acts as the data console: generates a realistic 120-day metric history and 4 workout programs, lets the user edit values, previews the exact JSON, and can export `demo-data.json`.
- `server.ts` (Node, zero runtime deps) — serves the page and the real endpoints the app consumes: `GET /api/metrics`, `GET /api/workouts`, `GET /api/health`, `POST /api/data`, CORS-enabled. Reads `demo-data.json` if present, otherwise generates data.
- App base URL defaults to `http://10.0.2.2:8765/` (Android emulator → host loopback), overridable via a `BASE_URL` build-config field.

## 10. Deliverables layout

```
README.md
docs/architecture.md                    (detailed architecture + UI description)
docs/superpowers/specs/2026-08-16-pulsepoint-design.md
demo-server/   (TS sources, demo-server.html, package.json, build scripts)
PulsePoint/    (Gradle + Kotlin project, APK-export-ready)
```

## 11. Out of scope (deliberately)

- Health-metric computation (server provides values).
- Authentication / multi-user accounts.
- Push notifications, wearable/health-connect integration.
- Server hosting beyond the local demo server.
