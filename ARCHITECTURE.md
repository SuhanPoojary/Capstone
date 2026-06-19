# SafeReady — Architecture (Final, Consolidated)

**Version 3.0 — June 2026**
**Status: Single source of truth.** This file replaces and merges:
`ARCHITECTURE.md` (legacy code architecture), `ARCHITECTURE2.md` (product/ecosystem blueprint), `DATABASE_SCHEMA.md`, `FEATURES.md`, and `FIREBASE_CONFIG.md`.

> Keep this as the one file you update going forward. If a change affects code structure, data, features, or Firebase, edit the matching section below instead of creating a new doc.

---

## 0. Reconciliation Notes — read this first

The five source documents were written at different times and disagree with each other in a few places. Rather than silently picking a winner, here's what was found and how it's handled in this merged doc. Resolve these with the team before treating any single section as fully authoritative.

1. **"Not yet implemented" vs. already wired up.** The legacy `ARCHITECTURE.md` lists Firebase, ViewModels, and a navigation framework as gaps. `FIREBASE_CONFIG.md` shows Firebase dependencies, `google-services.json`, and initialization in `SafeReadyApp.kt` already exist. `ARCHITECTURE2.md`'s navigation section assumes a `NavHostFragment` with `HomeFragment`, `LabFragment`, `EmergencyFragment`, `MedReadyFragment`, `ProfileFragment` already exist. **This doc treats the app as mid-migration**: the original Activity-based flow (Splash → Onboarding → Login/Signup → Dashboard → StartLearning → DisasterDetail → FullscreenPlayer) is the legacy shell described in Section 1, while `MainActivity` is becoming the host for a newer Fragment + Navigation Component shell described in Section 2. Confirm with whoever did the Firebase/Nav work how much of Section 2 is actually built vs. still aspirational.

2. **`DashboardActivity` vs. `HomeFragment`.** It's unclear whether `DashboardActivity` (legacy) and the new `Home` tab (per the ecosystem blueprint) are the same screen being replaced, two screens that currently coexist, or a planned future merge. Flagged as an open question — don't assume one supersedes the other without checking the codebase.

3. **Two different "ARCHITECTURE2" framings.** Earlier internal notes referred to a UI Design System (colors, typography, component styles) as a second architecture doc. The actual `ARCHITECTURE2.md` on file is a **product/feature-ownership blueprint** (which tab owns what, Normal Mode vs. Emergency Mode), not a visual design system. If a separate visual design system doc exists elsewhere, it should be merged in later as its own section — it is not included here because it wasn't part of the five files combined in this pass.

4. **Database vs. Firestore.** `DATABASE_SCHEMA.md` says "no Room, Firebase, or remote database" is in use. `FIREBASE_CONFIG.md` describes a Firestore schema (`users`, `progress`, `analytics`, `leaderboards`, `config`) that is planned/partially configured but **not yet created in the Firebase Console** (still "Test mode" / checklist items). Section 4 below presents both: what's actually persisted today (local/in-memory) and what Firestore collections are specified for the near-term rollout.

---

## 1. Current Code Architecture (Legacy Shell)

### 1.1 Pattern
SafeReady currently uses an **activity-centric Android architecture**, not a full MVVM or Clean Architecture implementation. Responsibilities are split across UI screens, a support/utility layer, simple model objects, and an application-level class. This is intentionally lightweight for what started as a capstone-scope app, and is now being layered with Firebase and (per Section 2) a Fragment/Navigation-based shell for the main 5-tab experience.

### 1.2 Layer Breakdown

**UI Layer** — Android Activities + XML layouts. Main screens:
- `SplashActivity`
- `OnboardingActivity`
- `LoginActivity`
- `SignupActivity`
- `DashboardActivity`
- `MainActivity` *(also the host for the newer Fragment shell — see Section 2)*
- `StartLearningActivity`
- `DisasterDetailActivity`
- `FullscreenPlayerActivity`
- `CrashViewerActivity`

These handle rendering, click actions, navigation, and direct user interaction.

**Support Layer** — utility and resource-resolution logic:
- `DemoVideoRepository` — resource-backed media lookup; resolves raw video file names, discovers available language variants, builds playable `Uri` values for ExoPlayer.
- `LocationHelper` — checks location permissions, reads last known location, reverse-geocodes to city/state names. Not yet wired into any UI (see Features, §5.11).

**Model Layer**
- `LanguageOption` — small data class for chapter language selection in the disaster detail flow.

**Application Layer**
- `SafeReadyApp` — custom `Application` class. Installs a global uncaught exception handler and writes crash logs to internal storage. **Also now initializes Firebase** (per `FIREBASE_CONFIG.md` — this is an update to the legacy doc, which listed Firebase as not yet implemented).

### 1.3 Data Flow (Legacy Shell)

**Entry flow**
1. `SplashActivity` opens first.
2. `OnboardingActivity` leads into signup.
3. `LoginActivity` and `SignupActivity` both route to `DashboardActivity`.
4. The user name is passed via `Intent` extras.

**Learning flow**
1. `DashboardActivity` opens `StartLearningActivity`.
2. The user selects a disaster type.
3. `DisasterDetailActivity` loads disaster-specific content.
4. Chapter selection resolves the correct video and language.
5. `FullscreenPlayerActivity` can be launched for large-screen playback.

**Crash flow**
1. A crash occurs anywhere in the app.
2. `SafeReadyApp` intercepts the exception.
3. Stack trace is written to `files/crash/`.
4. `CrashViewerActivity` reads the latest crash file and shows it.

### 1.4 Important Components

**Media playback** — `androidx.media3:media3-exoplayer`, `androidx.media3:media3-ui`, used in `DisasterDetailActivity` and `FullscreenPlayerActivity`.

**Offline lesson assets** — packaged in `res/raw`, discovered dynamically. Naming convention:
```text
{prefix}_ch{n}_{phase}_{lang}.mp4
```
Example: `flood_ch2_during_hi.mp4`

**Navigation (legacy)** — explicit `Intent` navigation with extras for state transfer, e.g. `DashboardActivity.EXTRA_NAME`, `DisasterDetailActivity.EXTRA_DISASTER_KEY`, `FullscreenPlayerActivity.EXTRA_URI`.

### 1.5 Design Decisions (why the legacy shell looks like this)
- **Activity-based:** the project was small enough that direct activity-driven navigation kept the code readable and easy to review.
- **Offline media:** improves reliability in low-connectivity environments and makes demos deterministic.
- **Dynamic raw-resource discovery:** `DemoVideoRepository` scans packaged resources so new language files can be added without code changes.
- **Crash viewer:** gives developers a simple way to inspect failures without backend observability.
- **No database (originally):** the feature set was content-driven rather than state-heavy, so a database would have added complexity without immediate benefit. *(This decision is being revisited — see Section 4.)*

---

## 2. Target/In-Progress Architecture — Product Ecosystem Shell

This section describes where the app is heading: a `MainActivity` hosting a `NavHostFragment` with five top-level tabs, plus a distinct Emergency Mode. Treat this as the **blueprint for ongoing and future work**, and confirm against the actual codebase how much already exists vs. is still being built (see Reconciliation Note 1).

### 2.1 Product Architecture Goal
SafeReady is evolving into an offline-first emergency preparedness ecosystem and emergency operating platform. The architecture should make it easy to answer, at all times:
1. Where does this feature belong?
2. Which tab owns this experience?
3. How does the user get there?
4. What happens when the app enters Emergency Mode?

### 2.2 Two Core App States

**Normal Mode** — the preparedness and planning ecosystem: learning, readiness tracking, personal setup, health/supply preparation, recommendations, progress and growth, account configuration.

**Emergency Mode** — the real-time survival operations ecosystem: SOS broadcasting, emergency communication, offline maps, rescue acknowledgment, disaster alerts, connectivity status, emergency timeline and operational state.

These are two connected operational states with different priorities, UI behavior, and feature emphasis — not just two screens.

### 2.3 App Ecosystem Diagram
```text
MainActivity
│
├── Home        — Situational Awareness Hub
├── Lab         — Preparedness & Simulation Ecosystem
├── Emergency   — Real-Time Survival Operations System
├── MedReady    — Preparedness Supply Intelligence System
└── Profile     — System Configuration & Personalization
```

Mode relationship:
```text
Normal Mode                  Emergency Mode
├── Home                     ├── Emergency
├── Lab                      ├── Home (light status access only if needed)
├── MedReady                 └── Profile (limited safety/config actions only if needed)
└── Profile
```
Emergency Mode takes priority over normal browsing when activated.

### 2.4 Top-Level Tab Ownership

**Home — Situational Awareness Hub.** Lightweight command center. Owns: readiness summary, local risk overview, quick actions, recommendations, emergency shortcuts, map preview, lightweight status info, high-level progress snapshots. Should never own: deep learning modules, full social systems, mesh diagnostics, advanced analytics, long settings flows, detailed account management, heavy feature configuration. Purpose: *"What should I know right now?"*

**Lab — Preparedness & Simulation Ecosystem.** Owns: simulations, drills, challenges, lessons, quizzes, XP, streaks, badges, achievements, leaderboards, progression systems, retention loops. Recommended internal structure: Daily Challenges, Simulations, Quick Drills, Interactive Learning, Achievements, Leaderboards, Progression Systems. Purpose: *"How do I become more prepared?"* Design rules: keep learning fast and structured; keep gamification motivating but not noisy; keep progression visible without burying safety tasks; keep long-form content readable and navigable.

**Emergency — Real-Time Survival Operations System.** The most important subsystem. Owns: SOS broadcasting, emergency communication, offline emergency relay, nearby rescue network, offline maps, rescue acknowledgments, disaster alerts, connectivity status, emergency timeline, quick survival tools, live operational state. Internal structure: SOS System, Emergency Communication, Offline Maps, Rescue Status, Disaster Alerts, Connectivity Status, Emergency Timeline, Quick Survival Tools.

*Naming rule:* mesh networking is an implementation detail, not a user-facing concept. Use "Emergency Communication," "Offline Emergency Relay," "Nearby Rescue Network" — never "Mesh Debug," "Packet Relay," "Transport Layer" in user-facing copy. Purpose: *"What do I do now, and how do I get help?"* Design rules: minimize steps; show clear status; prioritize oversized, readable actions; reduce nonessential content; keep operational information visible and calm; never bury SOS behind deep navigation.

**MedReady — Preparedness Supply Intelligence System.** Owns: OCR scanning, emergency kit analysis, medicine tracking, expiry tracking, preparedness scoring, supply recommendations, AI kit analysis, inventory readiness, offline preparedness packs. Recommended structure: Kit Scanner, Inventory Analysis, Expiry Tracking, Readiness Scoring, Recommendations, Offline Preparedness Packs. Purpose: *"What supplies do I have, what is missing, and what expires soon?"*

**Profile — System Configuration & Personalization.** Profile configures systems; Emergency Mode uses them — this separation is critical. Owns: user identity, emergency contacts, Firebase sync, offline downloads, accessibility settings, connectivity preferences, emergency behavior configuration, notification settings, map download settings, mesh communication settings, account/profile management. Purpose: *"How do I configure SafeReady for my needs?"* Design rule: Profile configures behavior, it should not become a dumping ground for feature controls (e.g. offline maps, mesh behavior, and SOS preferences are *configured* in Profile; Emergency Mode *consumes* those settings).

### 2.5 Feature Ownership Map (summary)

| Tab | Owns |
|---|---|
| Home | summaries, awareness cards, emergency shortcut entry points, quick insights, lightweight map preview |
| Lab | learning flows, simulations, drills, quizzes, achievements, leaderboards, progression |
| Emergency | SOS, emergency communication, offline maps, alerts, acknowledgment, live operational state |
| MedReady | supplies, medicine readiness, kit scanning, inventory intelligence, expiry tracking |
| Profile | identity, settings, sync controls, emergency preferences, accessibility, offline/connectivity configuration |

### 2.6 Cross-Module Relationships

**Shared relationships:** Lab updates preparedness/progression metrics shown on Home. MedReady contributes readiness scores and supply status to Home. Profile configures emergency behavior used by Emergency Mode. Firebase sync can affect all major systems. Achievements from Lab can appear on Home and Profile. Emergency Mode reads offline map and communication settings from Profile.

**Isolation rules:** Home should not own deep learning or communication internals. Lab should not own emergency operational controls. Emergency should not become cluttered with training content. MedReady should not become a generic settings page. Profile should not become a feature graveyard.

### 2.7 User-State Architecture

Primary state sources: authentication state, onboarding state, sync state, offline availability, emergency mode state, readiness/progress state, notification/alert state, configuration state.

```text
User Identity + Settings
        ↓
Local State + Cloud Sync
        ↓
Tab-Specific UI State
        ↓
Normal Mode or Emergency Mode behavior
```

Operational rule: **the UI should react to state, not own state.** Example ownership split: Repository (data source/persistence), ViewModel (screen/feature state), Fragment (display/interaction), Profile (configuration source), Emergency (runtime operational state).

### 2.8 Navigation Architecture

Bottom navigation represents the five major subsystems:
```text
MainActivity
│
├── NavHostFragment
│   ├── HomeFragment
│   ├── LabFragment
│   ├── EmergencyFragment
│   ├── MedReadyFragment
│   └── ProfileFragment
```
Principles: keep the shell stable; keep tab switching predictable; keep deep flows inside their owning tab; use modal/full-screen flows only when they improve clarity; preserve simple, safe backstack behavior.

A floating Emergency FAB may exist across Normal Mode. Rules: always provide a direct path into Emergency Mode; never hide it behind deep navigation; it should feel like a safety action, not a general shortcut; prioritize SOS and emergency-assistance entry points.

Backstack philosophy: Normal Mode back navigation should feel shallow and understandable; Emergency Mode should prioritize operational continuity over decorative navigation; modal screens are for confirmation, not for important system ownership.

### 2.9 Emergency Mode Transition Architecture

```text
Trigger detected
↓
Emergency Mode activated
↓
Emergency tab becomes primary
↓
SOS / communication / map / acknowledgment flows become available
↓
Operational status remains visible until user exits or de-escalates
```

Emergency triggers: manual SOS button press, fall detection, predicted disaster event, external alert ingestion, user escalation from Home/Profile.

Emergency-first behavior: reduce UI clutter; surface critical actions first; keep status visible; prioritize acknowledgment and communication; preserve offline functionality.

### 2.10 App Flow Architecture

```text
Normal Mode flow:    Launch → Home → Lab / MedReady / Profile → return to Home
Emergency Mode flow: Launch or trigger → Emergency Mode → SOS / communication / maps / acknowledgment → remain in operational state or exit safely
Hybrid flow:         Normal Mode → configure emergency preferences in Profile → Home shows readiness/quick actions → Emergency Mode can be entered instantly when needed
```

### 2.11 What Belongs Where (quick lookup)

| Tab | Belongs to | Should never contain |
|---|---|---|
| Home | readiness summary, local risk overview, quick actions, simple recommendations, map preview, emergency shortcut tiles | full social system, mesh diagnostics, long settings flows, deep analytics, full learning catalogs |
| Lab | drills, training, quizzes, progression, gamification, leaderboards, achievements | emergency broadcast UI, map configuration, account configuration, deep profile settings |
| Emergency | SOS, emergency communication, offline map access, acknowledgment, live status, emergency timeline | experimental labels, technical jargon visible to users, unrelated training content, profile setup tasks (unless strictly safety-required) |
| MedReady | kit analysis, medicine readiness, expiry checks, preparedness scoring, supply recommendations | — |
| Profile | identity, configuration, sync, accessibility, notification settings, emergency communication preferences | — |

### 2.12 Firebase Ownership Areas

Firebase is part of the active product architecture.

**Firebase should own:** authentication, cloud profile storage, sync state, social collaboration data, leaderboards, feed events, notification delivery support, Firestore-backed emergency/account metadata where appropriate.

**Firebase should not own alone:** emergency-only critical runtime decisions, offline survival logic, immediate SOS action availability.

**Architectural rule:** cloud is supportive. Local and offline systems remain authoritative for emergency-critical behavior.

### 2.13 Offline Data Flow

```text
User action
↓
Local repository/state update
↓
UI reflects change immediately
↓
Cloud sync occurs when available
```

Principles: the app should remain useful without network access; emergency features should not depend on cloud availability; sync should enhance state, not block it; local state should always be able to power the core experience.

### 2.14 Mesh / Emergency Communication Ownership

Mesh-related functionality lives inside Emergency Mode. Product framing uses human-centered language ("Emergency Communication," "Offline Emergency Relay," "Nearby Rescue Network"); the hidden technical layer (transport, nearby discovery, relay logic, queueing, retry/backoff, diagnostics) stays internal. The user should experience a survival communication tool, not a debugging/networking subsystem.

### 2.15 UI Ownership Structure

Recommended pattern: **Tab owner** defines what the area is for; **ViewModel owner** manages feature state; **Repository owner** handles data and sync; **Fragment owner** presents UI and interactions; **shared components** supply consistent cards, chips, progress indicators, headers, and states. Reusable UI should live in shared component spaces, not be scattered across tabs.

### 2.16 Future Editability Rules

Changes that should stay easy: moving a feature between tabs, splitting a tab into sub-flows, adding a new tab, removing an old experimental screen, expanding Emergency Mode, adding wearable support, adding smarter offline tools, merging/splitting feature modules.

To preserve editability: keep ownership boundaries explicit; keep shared UI components centralized; avoid naming features after implementation details; keep mode logic separate from content logic; document ownership before moving code.

### 2.17 Architectural Guardrails

**Do:** keep the app emergency-first; keep offline-first behavior intact; keep tabs purposeful; keep settings in Profile; keep survival tools in Emergency; keep growth tools in Lab; keep awareness in Home; keep supply intelligence in MedReady.

**Don't:** scatter emergency tools across unrelated tabs; put technical infrastructure names in the UI; turn Profile into a miscellaneous dumping ground; make Home too heavy; make Lab replace Emergency; hide critical actions behind deep navigation.

---

## 3. Features

### 3.1 Status Summary

| Feature | Status | Notes |
|---|---|---|
| Splash screen | Implemented | Launches the app and moves to onboarding |
| Onboarding | Implemented | Two CTA buttons both continue to signup |
| Login screen | Partial | UI functional; no backend authentication |
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

### 3.2 Feature Breakdown

**1. Splash Screen** — Implemented. `SplashActivity` loads the splash layout, waits 2 seconds, opens `OnboardingActivity`.

**2. Onboarding** — Implemented. `OnboardingActivity` shows the intro screen; both **Get Started** and **Skip Intro** route to signup.

**3. Login** — Partial. Accepts email/password input, derives a fallback display name from the email prefix, routes to `DashboardActivity`, includes a footer link to signup. Limitations: no auth backend, no account verification, no persistent session storage.

**4. Signup** — Partial. Accepts name, institution, email, password; sends the entered name to the dashboard; includes a footer link back to login. Limitations: no account-creation backend, no local persistence.

**5. Dashboard** — Implemented. Reads the user name from the incoming `Intent`, displays a time-based greeting, generates initials for the profile badge, shows a static location label, opens the learning flow from the training nav item.

**6. Start Learning** — Implemented. Displays four disaster cards (Earthquake, Floods, Cyclone, Landslides); each opens the disaster detail screen with a short disaster key.

**7. Disaster Detail** — Implemented. Loads a disaster-specific description, displays chapters for the selected disaster, resolves available lesson languages from packaged media, shows a language picker if more than one language exists, plays the selected chapter via ExoPlayer. Chapter model: 1 = general/preparation, 2 = during, 3 = after. Fallbacks: missing-video dialog if no file available; direct play path when only one language exists.

**8. Offline Media Playback** — Implemented. `DemoVideoRepository` constructs the resource name, resolves a `Uri` pointing to packaged media in `res/raw`, Media3 ExoPlayer streams the file directly. Naming convention: `{prefix}_ch{n}_{phase}_{lang}.mp4` (e.g. `eq_ch1_general_en.mp4`).

**9. Fullscreen Playback** — Implemented. Opens the selected URI in a dedicated player screen with its own ExoPlayer instance; supports a close button when present in the layout.

**10. Crash Capture and Inspection** — Implemented. `SafeReadyApp` installs a default uncaught exception handler; crashes are written as text files in internal storage; `CrashViewerActivity` loads the newest crash file and displays it. Developer shortcut: `LoginActivity` has a hidden tap-based shortcut to open the crash viewer.

**11. Location Helper** — Partial. Checks coarse/fine location permissions, reads the device's last known location, reverse-geocodes to city/state information. Limitation: available but not yet integrated with the dashboard or risk cards.

### 3.3 Key Flows

```text
Auth Flow:             Onboarding → Signup/Login → Dashboard
Learning Flow:         Dashboard → Start Learning → Disaster Detail → Chapter Select → Playback
Fullscreen Flow:       Disaster Detail → Fullscreen Player
Crash Inspection Flow: Crash occurs → SafeReadyApp stores stack trace → CrashViewerActivity displays it
```

---

## 4. Data Layer

### 4.1 Current State (No Database Yet)

SafeReady does **not** currently use Room or a remote database for its core data. The data layer today is made of `Intent` extras for screen state, packaged raw media resources for lessons, and internal file storage for crash logs. There are no formal database tables or collections in the current implementation — the entities below are logical, not persisted to disk (aside from crash logs).

**1. User Display Data** — stored as `Intent` extras / in-memory UI state.

| Field | Type | Purpose |
|---|---|---|
| `name` | `String` | Display name shown on dashboard |
| `email` | `String` | Used to derive fallback display name |
| `password` | `String` | Collected in UI only |
| `institution` | `String` | Collected in UI only |

Related screens: `LoginActivity`, `SignupActivity`, `DashboardActivity`. Example: `Name: Arjun Sharma`, `Email: arjun@iitb.ac.in`.

**2. Disaster Lesson Metadata** — derived from resource naming.

| Field | Type | Purpose |
|---|---|---|
| `disasterKey` | `String` | Identifies the disaster topic |
| `chapterIndex` | `Int` | Selects the lesson chapter |
| `phase` | `String` | `general`, `during`, or `after` |
| `languageCode` | `String` | Language variant such as `en`, `hi`, `mr` |
| `resourceName` | `String` | Packaged raw media file name |
| `uri` | `String` | Android resource URI used for playback |

Naming convention: `{prefix}_ch{n}_{phase}_{lang}.mp4`. Examples: `eq_ch1_general_en.mp4`, `flood_ch2_during_hi.mp4`, `cyclone_ch1_general_tel.mp4`. Related code: `DemoVideoRepository.getVideoUri(...)`, `DemoVideoRepository.getAvailableLanguages(...)`, `LanguageOption`.

**3. Language Option** — UI model.

| Field | Type | Purpose |
|---|---|---|
| `code` | `String` | Language code |
| `label` | `String` | User-facing label |

Example: `LanguageOption(code = "hi", label = "Hindi")`.

**4. Crash Log Entry** — internal file storage.

| Field | Type | Purpose |
|---|---|---|
| `time` | `String` | Timestamp of the crash |
| `thread` | `String` | Thread name |
| `stackTrace` | `String` | Exception trace |
| `filePath` | `String` | Saved under `files/crash/` |

Example file: `files/crash/crash_2026-04-30_14-22-09.txt`. Related code: `SafeReadyApp.writeCrashToFile(...)`, `CrashViewerActivity.loadLatestCrash()`.

**Relationships:**
- User → Dashboard: the user name is passed from login/signup into the dashboard via an `Intent` extra.
- Disaster → Chapters: each disaster maps to three chapters (1: general/preparation, 2: during, 3: after).
- Chapter → Languages: available languages depend on which raw media files exist for that chapter.
- Crash → Viewer: the crash viewer loads the latest crash log file from internal storage.

Example raw media structure:
```text
app/src/main/res/raw/
├── eq_ch1_general_en.mp4
├── eq_ch1_general_hi.mp4
├── flood_ch2_during_hi.mp4
└── ...
```

### 4.2 Planned/In-Progress: Firestore Schema

Per `FIREBASE_CONFIG.md`, Firebase dependencies, `google-services.json`, and SDK initialization are already wired into the app (see Section 5). The Firestore collections below are **specified but not yet created** in the Firebase Console — they represent the near-term target schema, not what's live today.

**Collection: `users`** — Document ID: `{uid}` (auto-generated from auth)
```json
{
  "name": "string",
  "email": "string",
  "institution": "string",
  "region": "string",
  "level": "number",
  "points": "number",
  "streak": "number",
  "createdAt": "timestamp",
  "lastSyncAt": "timestamp"
}
```

**Collection: `progress`** — Document ID: `{uid}`, Subcollection: `lessons`, Document ID: `{lessonKey}`
```json
{
  "completed": "boolean",
  "completedAt": "timestamp",
  "videoWatchedMinutes": "number",
  "quizScore": "number",
  "quizPassed": "boolean"
}
```

**Collection: `analytics`** — Document ID: `{uid}`
```json
{
  "totalLessonsCompleted": "number",
  "totalQuizzesAttempted": "number",
  "averageQuizScore": "number",
  "lastActiveAt": "timestamp"
}
```

Two additional collections are referenced in the security rules but not yet detailed with a schema: `leaderboards` (read-only for authenticated users) and `config` (public app config, read-only for everyone).

### 4.3 Notes on Room / Firebase / Backend

- **Room:** not used.
- **Firebase:** SDK-level integration done (see Section 5); Firestore collections/rules not yet created in console.
- **Backend API:** not used.
- **Internal storage:** used only for crash logs today — keeps the app self-contained and offline-friendly even as Firestore comes online.

---

## 5. Firebase Configuration

### 5.1 Current Status
- ✅ `google-services.json` is in place with the Firebase project's credentials.
- ✅ Firebase dependencies are added to `build.gradle.kts`.
- ✅ Firebase initialization is in `SafeReadyApp.kt`.
- ✅ Notification channels are set up for alerts and sync.
- ✅ `app/build.gradle.kts` has all required dependencies.
- ✅ root `build.gradle.kts` has the google-gms plugin.
- ✅ `AndroidManifest.xml` has all required permissions.

### 5.2 Project Configuration Details
- **Project ID:** `pralaytrata`
- **Project Number:** `987511247898`
- **Package Name:** `com.example.capstone`
- **API Key:** `AIzaSyC9YU41ms-fwmtFA2MlqyrDcfuqxNEm95I`
- **App ID:** `1:987511247898:android:aa12a4de3b9fd13249ada3`

*(This is the standard Android client config — it's meant to ship inside the app and is constrained by Firestore security rules / app restrictions, not a secret credential by itself. Don't add extra secrecy handling around it, but do make sure the security rules below are actually deployed before going to production.)*

### 5.3 Outstanding Console Setup (Not Yet Done)

**1. Authentication methods** — Firebase Console → Authentication → Sign-in method:
- ✅ Anonymous (already working for fallback)
- ✅ Email/Password (for user signup/login)

**2. Firestore Database** — Firebase Console → Firestore Database:
- Currently planned to start in **Test mode** for development.
- Choose a region close to users.
- Create the collections specified in Section 4.2 (`users`, `progress` with `lessons` subcollection, `analytics`, plus `leaderboards` and `config`).

**3. Firestore Security Rules** — replace current rules with:
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{uid} {
      allow read, write: if request.auth.uid == uid;
    }
    match /progress/{uid} {
      allow read, write: if request.auth.uid == uid;
      match /lessons/{lesson} {
        allow read, write: if request.auth.uid == uid;
      }
    }
    match /analytics/{uid} {
      allow read, write: if request.auth.uid == uid;
    }
    match /leaderboards/{document=**} {
      allow read: if request.auth != null;
      allow write: if false;
    }
    match /config/{document=**} {
      allow read: if true;
      allow write: if false;
    }
    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```

**4. Firebase Cloud Messaging (FCM)** — for the future push-notification phase: generate a server key (if not auto-generated) for sending notifications from a backend service. Leave as-is until that phase starts.

### 5.4 Not Yet Done (Future Phase)
- FCM token management
- Real push notification handling
- Real-time database listeners for collaboration
- Analytics event tracking
- Offline write queue
- Leaderboards and achievement system

---

## 6. Open Items / Next Steps

Combining all five source docs, here's the consolidated punch list:

**Resolve first (architecture clarity):**
- Confirm how much of the Section 2 Fragment/Navigation shell (`HomeFragment`, `LabFragment`, `EmergencyFragment`, `MedReadyFragment`, `ProfileFragment`, ViewModels, repositories) actually exists in code today vs. is still a target design.
- Decide the relationship between legacy `DashboardActivity` and the new `Home` tab.

**Data / Firebase:**
- Create the `users`, `progress`, `analytics`, `leaderboards`, and `config` collections in the Firebase Console.
- Deploy the production security rules in Section 5.3 (currently still planned to start in Test mode).
- Decide whether/when to introduce Room for local persistence now that the data model is growing beyond `Intent` extras.

**Features still planned (not yet built):**
- Quiz flow (CTA exists, no logic).
- AI assistant flow (visual entry point only on the dashboard).
- Real progress tracking (currently static/mocked UI).
- Location-aware personalization (helper exists, not wired into dashboard/risk cards).
- Persistent session storage and real authentication backend for Login/Signup.

**Future phases (explicitly out of scope for now):**
- FCM push notifications, real-time listeners, analytics event tracking, offline write queue, leaderboards/achievement backend.
- Full mesh/emergency-communication implementation behind the Emergency tab (Section 2.14) — currently a product/architecture spec, not implemented.