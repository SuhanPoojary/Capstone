# Features

## Feature Status Summary

| Feature | Status | Notes |
|---|---|---|
| Splash screen | Implemented | Launches the app and moves to onboarding |
| Onboarding | Implemented | Two CTA buttons both continue to signup |
| Login screen | Partial | UI is functional; no backend authentication |
| Signup screen | Partial | Collects data in UI only |
| Dashboard | Implemented | Personalized greeting and training entry point |
| Disaster learning catalog | Implemented | Four disaster categories available |
| Disaster detail lesson flow | Implemented | Descriptions plus chapter selection |
| Language selection | Implemented | Shown when multiple lesson languages exist |
| Offline video playback | Implemented | Uses Media3 with `res/raw` assets |
| Fullscreen playback | Implemented | Launches selected video in fullscreen |
| Crash viewer | Implemented | Reads latest crash log from internal storage |
| Location-aware personalization | Partial | Helper exists, not yet used in UI |
| Quiz flow | Planned | CTA exists, logic not implemented |
| AI assistant flow | Planned | Dashboard includes visual entry point only |
| Progress tracking | Planned | Current progress UI is static/mocked |

## Feature Breakdown

### 1. Splash Screen
**Status:** Implemented

**Internal behavior**
- `SplashActivity` loads the splash layout.
- It waits 2 seconds.
- It opens `OnboardingActivity`.

---

### 2. Onboarding
**Status:** Implemented

**Internal behavior**
- `OnboardingActivity` shows the intro screen.
- Both **Get Started** and **Skip Intro** route to signup.

---

### 3. Login
**Status:** Partial

**Internal behavior**
- Accepts email and password input.
- Derives a fallback display name from the email prefix.
- Routes to `DashboardActivity`.
- Includes a footer link to signup.

**Limitations**
- No authentication backend.
- No account verification.
- No persistent session storage.

---

### 4. Signup
**Status:** Partial

**Internal behavior**
- Accepts name, institution, email, and password input.
- Sends the entered name to the dashboard.
- Includes a footer link back to login.

**Limitations**
- No account creation backend.
- No local persistence.

---

### 5. Dashboard
**Status:** Implemented

**Internal behavior**
- Reads the user name from the incoming `Intent`.
- Displays a time-based greeting.
- Generates initials for the profile badge.
- Shows a static location label.
- Opens the learning flow from the training nav item.

---

### 6. Start Learning
**Status:** Implemented

**Internal behavior**
- Displays four disaster cards:
  - Earthquake
  - Floods
  - Cyclone
  - Landslides
- Each card opens the disaster detail screen with a short disaster key.

---

### 7. Disaster Detail
**Status:** Implemented

**Internal behavior**
- Loads a disaster-specific description.
- Displays chapters for the selected disaster.
- Resolves available lesson languages from packaged media.
- Shows a language picker if more than one language exists.
- Plays the selected chapter using ExoPlayer.

**Chapter model**
- Chapter 1 = general / preparation
- Chapter 2 = during
- Chapter 3 = after

**Fallbacks**
- Shows a missing-video dialog if no file is available.
- Uses a direct play path when only one language exists.

---

### 8. Offline Media Playback
**Status:** Implemented

**Internal behavior**
- `DemoVideoRepository` constructs the resource name.
- It resolves a `Uri` pointing to packaged media in `res/raw`.
- Media3 ExoPlayer streams the file directly.

**Naming convention**
```text
{prefix}_ch{n}_{phase}_{lang}.mp4
```

Example:
```text
eq_ch1_general_en.mp4
```

---

### 9. Fullscreen Playback
**Status:** Implemented

**Internal behavior**
- Opens the selected URI in a dedicated player screen.
- Uses its own ExoPlayer instance.
- Supports a close button when present in the layout.

---

### 10. Crash Capture and Inspection
**Status:** Implemented

**Internal behavior**
- `SafeReadyApp` installs a default uncaught exception handler.
- Crashes are written as text files in internal storage.
- `CrashViewerActivity` loads the newest crash file and displays it.

**Developer shortcut**
- `LoginActivity` contains a hidden tap-based shortcut to open the crash viewer.

---

### 11. Location Helper
**Status:** Partial

**Internal behavior**
- Checks coarse/fine location permissions.
- Reads the device’s last known location.
- Reverse geocodes to city/state information.

**Current limitation**
- The helper is available but not yet integrated with the dashboard or risk cards.

## Key Flows

### Auth Flow
```text
Onboarding → Signup/Login → Dashboard
```

### Learning Flow
```text
Dashboard → Start Learning → Disaster Detail → Chapter Select → Playback
```

### Fullscreen Flow
```text
Disaster Detail → Fullscreen Player
```

### Crash Inspection Flow
```text
Crash occurs → SafeReadyApp stores stack trace → CrashViewerActivity displays it
```

