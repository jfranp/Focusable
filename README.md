# Focusable

A context-aware focus mode Android application that monitors ambient noise and device movement during focus sessions, logging distractions and notifying the user when thresholds are exceeded.

## Architecture

Three-module Clean Architecture setup: `:domain`, `:data`, and `:app`.
The domain module is pure Kotlin with zero Android dependencies -- it holds the entities, repository interfaces, and use cases.
The data module implements those interfaces with Room, DataStore, Retrofit, and the actual sensor code.
The app module is Compose UI, ViewModels, navigation, and DI wiring.

The reason I split it this way is that the domain layer becomes trivially testable without any Android framework, and the dependency rule is enforced at the Gradle level -- domain doesn't know about data or app, so business logic can't accidentally couple to implementation details. ViewModels only talk to use cases and repository interfaces, never to Room DAOs or sensor APIs directly.

I used Koin for dependency injection because it's pure kotlin and doesn't require code generation or annotations. For navigation I went with Jetpack Navigation 3 since it integrates cleanly with Compose and supports animated transitions out of the box.

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
- `MockRestInterceptor` (data) — OkHttp application interceptor with in-memory `ConcurrentHashMap` store. Intercepts `POST /sessions`, `GET /sessions`, and `GET /session/{id}` without network access.
- `NotificationHelper` (data) — builds notification channel (API 26+ guarded), fires throttled notifications per distraction type with vibration.

## Trade-offs

The biggest one is using `FLAG_KEEP_SCREEN_ON` instead of a foreground service. A real focus app might need to keep monitoring even when the user locks the screen or switches apps, depending on business rules. I chose the simpler approach because a foreground service adds significant complexity (lifecycle management, persistent notification, wake locks) that would have taken time away from getting the core detection and UI right. The architecture is set up so that swapping in a service later is straightforward -- `FocusSessionController` already owns the sampling coroutine scope independently from any UI lifecycle.

The permission flow is another deliberate simplification. The app blocks everything behind an onboarding screen until all permissions are granted. In production I'd use contextual requests -- ask for the microphone when the user first taps "Start Session" for ex. The current gate is blunt but ensures the app never crashes from missing permissions.

The mock REST interceptor lives in the OkHttp layer, which means the Retrofit service, DTOs, and serialization all run for real -- the only fake part is the network transport itself. The downside is that the mock store resets on process death, but Room handles real persistence, so session data survives restarts regardless.

Distraction detection uses fixed numeric thresholds rather than anything adaptive. I considered a calibration step where the app samples the environment for a few seconds before starting, but decided it was out of scope. The three sensitivity levels (Normal, Sensitive, Extra Sensitive) give the user manual control as a simpler alternative.

## What I intentionally deprioritized

- **Background operation.** Sessions stop detecting when the app is backgrounded. I prioritized getting the in-app experience solid over the service infrastructure.
- **Distraction event history.** The app tracks total distraction count per session but doesn't persist individual events with timestamps. The `DistractionEvent` model exists in the domain layer for this, but I didn't wire up a full event log UI -- the session summary felt more important for the MVP.
- **Polished error handling on sync.** The mock API always succeeds, so I didn't build retry logic or an offline sync queue. The `Resource` wrapper is in place for error propagation, but the UI doesn't surface sync failures yet.
- **ProGuard / R8 configuration.** Minification is disabled. A release build would need keep rules for Retrofit, kotlinx-serialization, and Room.

## What I would improve with more time

First priority would be a **foreground service** so focus sessions survive screen-off and app backgrounding. The controller logic is already decoupled from the UI, so it's mostly lifecycle and notification plumbing.

I'd add a **calibration step** feature that samples ambient noise for 5-10 seconds before starting a session and adjusts thresholds relative to the baseline. This would make the app usable in noisy environments like coffee shops without requiring the user to manually pick "Extra Sensitive."

On the data side, I'd replace the mock interceptor with a **real backend** (or at least a local server for testing) and build an offline-first sync queue with retry and conflict resolution.

The UI could benefit from a **session detail screen** showing a timeline of distraction events, and **charts or trends** across past sessions so the user can see their focus improving over time.

Finally, I'd add **instrumented UI tests** with Compose testing APIs to verify navigation flows, permission handling, and theme switching end-to-end.

## How I approached native resource handling

The app uses three native Android resources directly: the microphone, the accelerometer, and the notification system.

For the **microphone**, I'm using `AudioRecord` with a voice recognition source at 8 kHz mono.

For the **accelerometer**, I register a `SensorManager` listener at `SENSOR_DELAY_UI` rate and compute the vector magnitude from x/y/z.

Both sensors are started and stopped by `FocusSessionController`, which owns a coroutine scope tied to the session lifetime. When a session starts, it launches sampling coroutines; when it stops, the scope is cancelled and sensors are released. This keeps resource management predictable and avoids leaks.

**Notifications** use `NotificationChannel` with `NotificationCompat` for backward compatibility. Each distraction type has its own throttle window (5 seconds for noise, 3 seconds for motion) so the user isn't bombarded. Notifications include vibration to get attention without being too intrusive.

The **screen** stays on during active sessions using `FLAG_KEEP_SCREEN_ON`, applied through a Compose `DisposableEffect` that cleans up when the session ends or the composable leaves composition.

## How I ensured testability

The architecture was designed with testing in mind from the start. The domain module has zero Android dependencies, so all its tests run on the JVM without emulators or Robolectric. Every repository and sensor interface has a corresponding fake implementation that I can control in tests.

Use case tests verify the business logic -- things like "starting a session calls the repository," "stopping a session returns the stopped session," "changing sensitivity stops any active session first," and "sync delegates to the sync repository." These all run with `kotlinx-coroutines-test` and fakes.

In the data module, I test the `SessionMapper` (all four mapping directions with edge cases) and the `MockRestInterceptor` (using a real `OkHttpClient` so the full HTTP serialization path is exercised). These don't need Android either.

ViewModel tests in the app module verify that the presentation layer correctly wires up use cases, propagates flow state, and handles user actions like toggling debug mode or starting/stopping sessions. The fakes are duplicated in the app test sources since Gradle doesn't share test dependencies across modules.

I intentionally didn't unit-test `FocusSessionController` or Room DAOs because they're tightly coupled to Android system APIs. Those are better covered by instrumented tests, which I'd add next.

### Running tests

```
./gradlew :domain:test
./gradlew :data:test
./gradlew :app:testDebugUnitTest
```

## How I would scale this in production

The current architecture already separates concerns cleanly, so scaling is mostly about infrastructure:

**Backend sync** would move from the mock interceptor to a real API with authentication, and I'd add a `WorkManager`-based sync queue that retries on failure and batches uploads when connectivity is restored.

**Background operation** would require a foreground service with a persistent notification showing session, a workmanager service would run on the background with the sensors. The `FocusSessionController` already manages its own coroutine scope, so it could live inside a service without major refactoring.

**Multiple sensor sources** (GPS for location-based focus zones, screen usage tracking, app usage stats) could be added by implementing new `Sampler` interfaces in the data layer and combining their telemetry in the controller. The domain layer wouldn't need to change.

Also, for a bigger production level project it might make sense to change up the module structure. Instead of dividing according to clean layers at the top level, it might make more sense to divide modules according to features, with clean layers inside of them.

## Session-driven theming

I made a little dynamic behavior where the app changes theme/color according to session status.asd

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

If a permission was previously denied, the onboarding screen offers an "Open App Settings" button and re-checks grants when the activity resumes. I did it this way just to simplify permission management into a single place as well as making sure permissions are provided before entering the app, so that the MVP demos can run smoothly without interruptions.
