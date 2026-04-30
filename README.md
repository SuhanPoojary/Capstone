# SafeReady

SafeReady is an Android disaster-preparedness app that helps users learn how to respond to earthquakes, floods, cyclones, and landslides through structured lessons and offline video playback. It supports cloud synchronization, user authentication, and emergency notifications while maintaining a 100% offline-first design.

## Project Overview

SafeReady is designed as a practical learning app for disaster readiness. It provides:
- a branded onboarding flow,
- lightweight sign-in and sign-up screens,
- a personalized dashboard,
- disaster-specific learning modules,
- chapter-based video lessons,
- fullscreen media playback,
- and local crash inspection for debugging.

The current implementation is optimized for offline content delivery and demo-friendly navigation.

## Problem Statement

Many users do not have a simple, mobile-first way to learn disaster response procedures before an emergency occurs. SafeReady addresses this by packaging preparedness content into a fast, easy-to-use Android experience that works even without network access.

## Key Features

- Splash screen and onboarding flow
- Login and signup screens
- Personalized dashboard greeting
- Disaster learning catalog
- Chapter-based lesson selection
- Language-aware offline lesson playback
- Fullscreen video viewing
- Internal crash logging and crash viewer
- Location helper utility for future regional features

## Tech Stack

- **Language:** Kotlin
- **UI:** Android XML Views + Material Components
- **Architecture:** MVVM + Repository Pattern
- **Android libraries:** AndroidX, Material Components, ConstraintLayout, GridLayout, Lifecycle, WorkManager
- **Media playback:** Media3 ExoPlayer
- **Backend:** Firebase (Authentication, Firestore, Cloud Messaging ready)
- **Storage:** SharedPreferences (local), Firestore (cloud), `res/raw` media assets
- **Async:** Kotlin Coroutines
- **Utilities:** Location services, Notification channels, Firebase Admin SDK

## Project Phases

### Phase 1-2: Learning System (Completed)
- Splash screen and onboarding flow
- Login and signup screens
- Personalized dashboard with recommendations
- Disaster learning catalog with offline video playback
- Quiz system with scoring
- Progress tracking and gamification
- AI Assistant with rule-based responses
- Location-based personalization

### Phase 3: Cloud & Notifications (Completed)
- Firebase Email/Password authentication
- Anonymous authentication fallback (offline mode)
- Cloud user profile storage and sync
- Bi-directional progress synchronization
- Emergency alert notifications
- Alert scheduling with WorkManager
- Complete offline-first architecture
- Graceful network failure handling

## App Flow

```text
SplashActivity
  → OnboardingActivity
    → SignupActivity / LoginActivity
      → DashboardActivity
        → StartLearningActivity
          → DisasterDetailActivity
            → FullscreenPlayerActivity
```

### Supporting flows

- `SafeReadyApp` installs a global crash handler and stores crash details in internal storage.
- `CrashViewerActivity` displays the latest saved crash log.
- `LocationHelper` is available for city/state lookup but is not yet wired into the UI.

## Setup Instructions

### Prerequisites

- Android Studio
- A compatible JDK for the project’s Android Gradle Plugin version
- Android emulator or physical device

### Run the Project

1. Open the project in Android Studio.
2. Sync Gradle.
3. Build and run on an emulator or device.

### Command Line

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
```

## Folder Structure

```text
Capstone/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/example/capstone/
│   │   │   ├── SafeReadyApp.kt
│   │   │   ├── SplashActivity.kt
│   │   │   ├── OnboardingActivity.kt
│   │   │   ├── LoginActivity.kt
│   │   │   ├── SignupActivity.kt
│   │   │   ├── DashboardActivity.kt
│   │   │   ├── MainActivity.kt
│   │   │   ├── StartLearningActivity.kt
│   │   │   ├── DisasterDetailActivity.kt
│   │   │   ├── FullscreenPlayerActivity.kt
│   │   │   ├── CrashViewerActivity.kt
│   │   │   ├── DemoVideoRepository.kt
│   │   │   ├── location/LocationHelper.kt
│   │   │   └── model/LanguageOption.kt
│   │   └── res/
│   │       ├── layout/
│   │       ├── drawable/
│   │       ├── raw/
│   │       ├── values/
│   │       └── xml/
└── gradle/
```

## Current Scope

SafeReady currently includes:
- **Offline-first learning system** with complete functionality without network
- **Cloud synchronization** of user progress and profiles (optional, with automatic fallback)
- **Firebase authentication** with email/password and anonymous login
- **Emergency alert notifications** with background scheduling
- **Gamification system** with points, levels, and streak tracking
- **AI Assistant** with rule-based disaster Q&A
- **Personalized recommendations** based on region and progress
- **Quiz system** with scoring and feedback
- **Crash logging and inspection** for debugging

## Documentation

For detailed Phase 3 implementation information, see:
- **PHASE3_INDEX.md** - Start here for Phase 3 overview
- **PHASE3_SETUP.md** - Firebase configuration guide
- **PHASE3_INTEGRATION_CHECKLIST.md** - Step-by-step integration
- **PHASE3_QUICK_REFERENCE.md** - Code examples and quick reference
- **PHASE3_TROUBLESHOOTING.md** - Error resolution guide
- **PHASE3_COMPLETION_REPORT.md** - Detailed completion status
- **PLAN.md** - Overall project plan and phase breakdown

