# Focusable

A context-aware focus mode Android application that monitors ambient noise and device movement during focus sessions, logging distractions and notifying the user when thresholds are exceeded.

## Architecture

The project follows **Clean Architecture** with three Gradle modules:

```
:app  -->  :domain  <--  :data
```

| Module | Responsibility |
|--------|---------------|
| `:domain` | Pure Kotlin. Entities (`FocusSession`, `SensorPreferences`), repository interfaces, sensor port interfaces, use case classes, and `Resource` result wrapper. No Android framework dependency. |
| `:data` | Android library. Room database, DataStore preferences, Retrofit + OkHttp (with mock interceptor), sensor implementations (AudioRecord, SensorManager), session controller with coroutine-scoped sampling, notification helper, and Koin DI module. |
| `:app` | Single-activity Compose UI. Jetpack Navigation bottom bar (Home, Sessions, Preferences), onboarding permission flow, Material 3 theming with session-driven light/dark switching, ViewModels that depend only on domain use cases, and Koin DI wiring. |

**Dependency rule:** `:app` depends on `:domain` and `:data`; `:data` depends on `:domain`; `:domain` depends on neither.

### Key classes

- `FocusSessionController` (data) — owns a session-scoped `CoroutineScope` that starts/stops noise and motion sampling, applies threshold + throttle policy, records distractions, fires notifications, and publishes telemetry via `StateFlow`.
- `MockRestInterceptor` (data) — OkHttp application interceptor with in-memory `ConcurrentHashMap` store. Intercepts `POST /sessions`, `GET /sessions`, and `GET /session/{id}` without network access. Located at `data/src/main/kotlin/.../data/remote/MockRestInterceptor.kt`.
- `NotificationHelper` (data) — builds notification channel (API 26+ guarded), fires throttled notifications per distraction type with vibration.

## Native resources

| Resource | Implementation | Notes |
|----------|---------------|-------|
| Microphone | `AudioRecord` with `VOICE_RECOGNITION` source, 8 kHz mono 16-bit PCM | Reads short buffers, computes max amplitude as noise level metric |
| Accelerometer | `SensorManager` + `TYPE_ACCELEROMETER`, `SENSOR_DELAY_UI` | Computes magnitude from x/y/z, uses exponential smoothing |
| Notifications | `NotificationChannel` (API 26+), `NotificationCompat`, `POST_NOTIFICATIONS` (API 33+) | Throttled per type: noise 5s, motion 3s; paired with vibration |
| Screen | `FLAG_KEEP_SCREEN_ON` via `DisposableEffect` while session active | Cleared on session stop or composable disposal |

## Session-driven theming

The app uses two Material 3 color schemes:

- **Light pastel scheme** — active when no session is running (lavender, mint, soft neutrals).
- **Dark focus scheme** — active during a focus session (deep indigo, teal accents, dark surfaces).

The root composable observes `isSessionActive` from the domain layer and wraps all content in `MaterialTheme(colorScheme = ...)` so the theme applies consistently across all tabs.

## Debug dashboard

Available on the Home tab via a toggle button in the top app bar. When enabled, a card displays:

- Live **noise level** and **motion level** values alongside their current thresholds.
- Values that exceed thresholds are highlighted in the error color (red) **and** labeled "Above threshold" for accessibility (no color-only meaning).
- When no session is active, the card shows an idle state message.

The dashboard consumes the same `SensorTelemetrySnapshot` flow that the session controller uses for distraction detection, ensuring the displayed values match the actual decision logic.

## Permissions

The app gates all functionality behind an onboarding screen until required permissions are granted:

- **RECORD_AUDIO** — required on all API levels for microphone access.
- **POST_NOTIFICATIONS** — required on API 33+ for notification delivery.

If a permission was previously denied, the onboarding screen offers an "Open App Settings" button and re-checks grants when the activity resumes.

## Trade-offs

- **Permission gate vs. contextual requests:** The onboarding screen blocks access until all permissions are granted. A production app might use contextual requests and degrade gracefully.
- **Screen-on vs. foreground service:** Sessions rely on `FLAG_KEEP_SCREEN_ON` rather than a foreground service. Detection stops if the screen turns off or the app is backgrounded. A foreground service would be the next step for reliability.
- **Simple thresholds vs. ML:** Distraction detection uses fixed numeric thresholds per sensitivity level. No machine learning or adaptive calibration — thresholds are tunable constants.
- **In-process mock API:** The mock REST interceptor is stateful only for the process lifetime. Data persists locally in Room across process death; the mock store resets.
- **Per-sensor sensitivity:** Noise and motion thresholds can be configured independently (Normal / Sensitive / Extra Sensitive for each), stored as two separate DataStore keys.

## Testability

Three layers of unit tests cover domain logic, data mapping, and presentation:

### Domain tests (`domain/src/test/`)
- **Pure Kotlin, no Android dependencies.** All repository and sensor interfaces have in-memory fake implementations under `domain/src/test/.../fake/`.
- **Model tests** — `SensorPreferencesTest` verifies threshold computation for all sensitivity levels and default values.
- **Use case tests** — `StartFocusSessionUseCaseTest`, `StopFocusSessionUseCaseTest`, `SyncSessionUseCaseTest`, and `UpdateSensorPreferenceUseCaseTest` verify business logic (session lifecycle, preference updates stop active sessions, sync success/error paths) using fakes and `kotlinx-coroutines-test`.

### Data tests (`data/src/test/`)
- **`MockRestInterceptorTest`** — exercises the REST contract (POST creates with generated ID, GET returns stored data, GET 404 for unknown IDs, list filtering) using a real `OkHttpClient` with the interceptor attached. No Android framework needed.
- **`SessionMapperTest`** — verifies all four mapping directions (entity-to-domain, domain-to-entity, domain-to-dto, dto-to-domain) including edge cases like null end times and zero IDs.

### App / ViewModel tests (`app/src/test/`)
- Fakes for domain interfaces are duplicated in `app/src/test/.../fake/` since domain test sources aren't on the app's test classpath.
- **`HomeViewModelTest`** — initial state defaults, debug toggle, start/stop session lifecycle, sync-on-stop, and telemetry propagation.
- **`SessionsViewModelTest`** — empty initial list, past-sessions-only filtering, multiple session accumulation.
- **`PreferencesViewModelTest`** — default preferences, session active detection, per-sensor level updates, independence of noise/motion, and session-stop side-effect on preference change.

### Why these layers?
- **Domain** tests protect pure business rules and threshold math from regressions.
- **Data mapper/interceptor** tests validate the serialization boundary without needing a real network or database, catching contract drift early.
- **ViewModel** tests ensure the presentation layer correctly orchestrates use cases, maps flows to UI state, and handles user actions — all without Compose or Android instrumentation.

`FocusSessionController` and Room DAO queries are not unit-tested in this scope because they depend heavily on Android system APIs (`AudioRecord`, `SensorManager`, Room's generated code). These would be better covered by instrumented tests or integration tests in a CI environment.

### Running tests

```
./gradlew :domain:test
./gradlew :data:test
./gradlew :app:testDebugUnitTest
```

## What would improve with more time

- **Foreground service** for reliable background detection when the screen is off or the app is in the background.
- **Contextual permission requests** with graceful degradation instead of a blocking onboarding gate.
- **Real backend** replacing the mock interceptor, with proper error handling, retry logic, and offline sync queue.
- **Adaptive thresholds** or calibration step to account for different environments and device sensitivities.
- **Distraction event detail screen** showing individual noise/motion events with timestamps within a session.
- **Instrumented UI tests** with Compose testing APIs for navigation flows and screen assertions.
- **ProGuard / R8 rules** for minified release builds (Retrofit, kotlinx-serialization).

## Build

Requires Android Studio with AGP 9.1.0+ and Kotlin 2.2.10.

```
./gradlew :app:assembleDebug
```

Min SDK: 24 | Target/Compile SDK: 36
