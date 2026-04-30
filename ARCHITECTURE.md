# Architecture

## Architecture Pattern

SafeReady currently uses an **activity-centric Android architecture** with separated responsibilities across:
- UI screens,
- media/resource access,
- helper utilities,
- and simple model objects.

It is not a full MVVM or Clean Architecture implementation yet. The current structure is intentionally lightweight and practical for a capstone app.

## Layer Breakdown

### UI Layer
The UI is implemented with Android Activities and XML layouts.

Main screens:
- `SplashActivity`
- `OnboardingActivity`
- `LoginActivity`
- `SignupActivity`
- `DashboardActivity`
- `MainActivity`
- `StartLearningActivity`
- `DisasterDetailActivity`
- `FullscreenPlayerActivity`
- `CrashViewerActivity`

These screens handle:
- rendering,
- click actions,
- navigation,
- and direct interaction with the user.

### Support Layer
This layer contains utility and resource-resolution logic.

#### `DemoVideoRepository`
Resource-backed media lookup helper that:
- resolves raw video file names,
- discovers available language variants,
- and builds playable `Uri` values for ExoPlayer.

#### `LocationHelper`
Utility for:
- checking location permissions,
- reading last known location,
- and reverse geocoding to city/state names.

### Model Layer
#### `LanguageOption`
A small data class used for chapter language selection in the disaster detail flow.

### Application Layer
#### `SafeReadyApp`
Custom `Application` class that installs a global uncaught exception handler and stores crash logs in internal storage.

## Data Flow

### Entry Flow
1. `SplashActivity` opens first.
2. `OnboardingActivity` leads into signup.
3. `LoginActivity` and `SignupActivity` both route to `DashboardActivity`.
4. The user name is passed via `Intent` extras.

### Learning Flow
1. `DashboardActivity` opens `StartLearningActivity`.
2. The user selects a disaster type.
3. `DisasterDetailActivity` loads disaster-specific content.
4. Chapter selection resolves the correct video and language.
5. `FullscreenPlayerActivity` can be launched for large-screen playback.

### Crash Flow
1. A crash occurs anywhere in the app.
2. `SafeReadyApp` intercepts the exception.
3. Stack trace is written to `files/crash/`.
4. `CrashViewerActivity` reads the latest crash file and shows it.

## Important Components

### Media Playback
- `androidx.media3:media3-exoplayer`
- `androidx.media3:media3-ui`

Used for lesson playback in:
- `DisasterDetailActivity`
- `FullscreenPlayerActivity`

### Offline Lesson Assets
Lesson videos are packaged in `res/raw` and discovered dynamically.

Naming convention:

```text
{prefix}_ch{n}_{phase}_{lang}.mp4
```

Example:

```text
flood_ch2_during_hi.mp4
```

### Navigation
The app uses explicit `Intent` navigation between screens and extras for state transfer.

Examples:
- `DashboardActivity.EXTRA_NAME`
- `DisasterDetailActivity.EXTRA_DISASTER_KEY`
- `FullscreenPlayerActivity.EXTRA_URI`

## Design Decisions

### Why activity-based architecture?
The project is small enough that direct activity-driven navigation keeps the code readable and easy to review.

### Why offline media?
Offline content improves reliability in low-connectivity environments and makes demos deterministic.

### Why dynamic raw-resource discovery?
`DemoVideoRepository` scans packaged resources so new language files can be added without code changes.

### Why a crash viewer?
It gives developers a simple way to inspect failures without requiring backend observability.

### Why no database yet?
The current feature set is content-driven rather than state-heavy, so a database would add complexity without immediate benefit.

## Current Gaps

Not yet implemented:
- ViewModels
- Room
- Firebase
- API integration
- DI framework
- background workers

These can be added later if the app grows into a networked production system.

