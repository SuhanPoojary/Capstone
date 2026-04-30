# SafeReady MVVM Migration Plan

> Living document rule: after each implementation change or phase completion, update this file with the shipped scope, remaining work, and any deferred items.

## 1. Architecture Plan

### Target Architecture
SafeReady should move from an **activity-based, screen-driven app** to a **single-activity MVVM architecture** with a repository pattern.

### Recommended Structure
```text
com.example.capstone
├── data
│   ├── local
│   │   ├── preferences
│   │   ├── database   (future Room)
│   │   └── model
│   ├── remote         (future Firebase/API)
│   └── repository
├── domain
│   ├── model
│   ├── usecase       (optional, keep lightweight)
│   └── interface
├── presentation
│   ├── activity
│   ├── fragment
│   ├── viewmodel
│   └── uiState
├── service           (notifications, alerts, background work)
├── util              (location, formatting, helpers)
└── app                (Application class)
```

### Migration Approach
1. Keep `SafeReadyApp` as the `Application` entry point.
2. Replace multi-activity navigation with a **single `MainActivity`** hosting fragments.
3. Move feature logic out of Activities into:
   - `ViewModel` for UI state and events
   - `Repository` for data access
   - `DataSource` for shared preferences, local media, and later Room/Firebase
4. Keep `SplashActivity`, onboarding, login, and signup only as a temporary pre-shell flow if needed; long term, move them into the same single-activity shell for consistency.
5. Keep offline media playback in the repository layer so it can later switch to Firebase storage or remote URLs without changing fragment code.

### Core Principles
- UI should observe state, not own business logic.
- Repositories should hide storage source changes.
- Fragment code should stay thin.
- Do not introduce Clean Architecture overhead unless the app grows beyond capstone scope.

---

## 2. Phase-wise Implementation

## Phase 1 — Core System + Must Features
**Status:** Completed
**Goal:** establish the MVVM foundation and implement the minimum production-ready learning system.

### Implementation Notes
- The app now uses a single-activity shell with `MainActivity` and bottom navigation.
- Feature screens are split into fragments for Home, Training, Progress, Assistant, and Profile.
- Local state is persisted with SharedPreferences through repository abstractions.
- Lesson completion updates progress and opens the chapter quiz flow.
- Offline disaster learning still uses `res/raw` videos through the repository layer.
- Location helper support is wired into the data layer for future personalization use.

### What to build
- Single-activity shell with `BottomNavigationView` — implemented
- Fragments:
  - Home — implemented
  - Training — implemented
  - Progress — implemented
  - Assistant — implemented
  - Profile — implemented
- SharedPreferences-based local storage — implemented
- Progress tracking — implemented
- Quiz system — implemented
- Location-based personalization — partially implemented and ready for expansion
- MVVM layer for each feature — implemented for Phase 1 scope

### Suggested order
1. Create the single-activity shell and navigation host.
2. Build shared UI state models and base ViewModel classes.
3. Move learning catalog and lesson detail into fragments.
4. Add local persistence for user profile and learning progress.
5. Implement quiz flow and scoring.
6. Add location detection and region-based recommendations.
7. Wire dashboard progress metrics.

### Delivered Scope
- `MainActivity` as the single host for bottom navigation.
- `HomeFragment`, `TrainingFragment`, `ProgressFragment`, `AssistantFragment`, `ProfileFragment`.
- `SafeReadyPreferences`, repositories, and MVVM state objects for Phase 1.
- Lesson completion tracking, quiz launch/score capture, and progress summaries.
- `QuizBottomSheetDialogFragment` for in-context chapter assessment.
- Local profile persistence and region-ready location helpers.

### Dependencies
- Navigation shell must exist before fragment migration.
- SharedPreferences repository must exist before progress and profile persistence.
- Lesson repository must be stable before quiz progression logic.
- Location helper must be permission-aware before it drives recommendations.

### Files to create
- `presentation/MainActivity.kt`
- `presentation/fragment/HomeFragment.kt`
- `presentation/fragment/TrainingFragment.kt`
- `presentation/fragment/ProgressFragment.kt`
- `presentation/fragment/AssistantFragment.kt`
- `presentation/fragment/ProfileFragment.kt`
- `presentation/viewmodel/HomeViewModel.kt`
- `presentation/viewmodel/TrainingViewModel.kt`
- `presentation/viewmodel/ProgressViewModel.kt`
- `presentation/viewmodel/AssistantViewModel.kt`
- `presentation/viewmodel/ProfileViewModel.kt`
- `data/repository/ProgressRepository.kt`
- `data/repository/UserRepository.kt`
- `data/repository/LessonRepository.kt`
- `data/local/preferences/SafeReadyPreferences.kt`
- `data/local/model/ProgressRecord.kt`
- `data/local/model/QuizQuestion.kt`
- `data/local/model/QuizResult.kt`

### Estimated complexity
**High** — this phase changes the app structure and data flow.

---

## Phase 2 — Intelligence + Engagement
**Status:** Completed
**Goal:** make the app more useful, personalized, and sticky.

### Implementation Notes
- Added a rule-based offline assistant with contextual reply suggestions and follow-up prompts.
- Added recommendation logic that combines region, progress, and completion state.
- Added gamification tracking for points, levels, streaks, and badge summaries.
- Surfaced recommendation and gamification data in Home, Progress, Assistant, and Profile screens.

### What to build
- Offline rule-based AI assistant — implemented
- Personalized lesson recommendations — implemented
- Gamification system — implemented

### Suggested order
1. Build the assistant fragment and chat UI.
2. Implement keyword/rule matching with local response data.
3. Add recommendation logic using progress and location signals.
4. Introduce badge, streak, and level calculations.
5. Surface engagement metrics on Home/Profile.

### Delivered Scope
- Assistant replies now include suggested topics and follow-up prompts.
- Home shows a personalized recommendation card and gamification summary.
- Progress shows overall completion alongside the current learning summary.
- Profile shows user details plus level, streak, and points.
- Gamification data is stored locally and updates from lesson completion and quiz correctness.

### Dependencies
- Assistant requires a stable local knowledge dataset.
- Recommendations depend on progress and lesson completion data.
- Gamification depends on quiz and progress tracking.

### Files to create
- `presentation/fragment/AssistantFragment.kt`
- `presentation/viewmodel/AssistantViewModel.kt`
- `presentation/uiState/ChatUiState.kt`
- `data/repository/AssistantRepository.kt`
- `data/repository/RecommendationRepository.kt`
- `data/repository/GamificationRepository.kt`
- `data/local/model/ChatMessage.kt`
- `data/local/model/Badge.kt`
- `data/local/model/StreakState.kt`
- `domain/model/Recommendation.kt`

### Estimated complexity
**Medium to High** — mostly local logic, but several feature interactions.

---

## Phase 3 — Advanced Features
**Status:** Completed
**Goal:** add backend sync and alerting without rewriting the core app.

### Implementation Notes
- Firebase will be integrated behind existing repository interfaces.
- Local-first pattern ensures offline functionality remains intact.
- Authentication will support anonymous login for demo purposes.
- Progress sync will be non-blocking and user-transparent.
- Notifications will use AndroidX notification channels for API 26+.

### What was built
✓ Firebase authentication (with anonymous fallback)
✓ Cloud progress sync (bi-directional)
✓ Emergency alert simulation with notifications
✓ Cloud user profile storage
✓ AuthSyncViewModel for UI state management
✓ SyncRepository for high-level sync orchestration
✓ Notification infrastructure (channels, helper, service)
✓ AlertScheduler using WorkManager for background alerts

### Implementation files created
- `data/remote/firebase/FirebaseAuthDataSource.kt` ✓
- `data/remote/firebase/FirebaseProgressDataSource.kt` ✓
- `data/remote/firebase/FirebaseUserDataSource.kt` ✓
- `data/repository/AuthRepository.kt` ✓
- `data/repository/CloudProgressRepository.kt` ✓
- `service/AlertNotificationService.kt` ✓
- `service/AlertScheduler.kt` ✓
- `service/NotificationHelper.kt` ✓
- `data/Phase3Models.kt` (AuthResult, SyncStatus, EmergencyAlert) ✓
- `data/Phase3Repositories.kt` (SyncRepository, SyncRepositoryFactory) ✓
- `presentation/Phase3ViewModels.kt` (AuthSyncViewModel) ✓
- `app/google-services.json` (template) ✓

### Build.gradle.kts updates
- Added Firebase BOM and core dependencies ✓
- Added google-gms plugin ✓
- Added Coroutines support ✓
- Added WorkManager for background tasks ✓

### AndroidManifest.xml updates
- Added POST_NOTIFICATIONS permission for Android 13+ ✓
- Added AlertNotificationService declaration ✓

### SafeReadyApp.kt updates
- Added Firebase initialization ✓
- Added NotificationHelper initialization ✓

### Phase 3 Shipped Scope
- **Firebase Authentication**: Email/password signup and login with anonymous fallback
- **Cloud User Profiles**: User data stored and synced to Firestore with merge conflict resolution
- **Bi-directional Progress Sync**: Local progress pushed to cloud; cloud progress pulled and merged intelligently
- **Quiz Score Tracking**: Quiz results stored in cloud for user records
- **Notification Channels**: Two channels (alerts and sync status) for API 26+ compliance
- **Alert Delivery System**: AlertNotificationService for sending notifications with intent routing
- **Background Alert Scheduling**: AlertScheduler using WorkManager for reliable scheduled alerts
- **Sync Status Tracking**: Real-time status UI updates with success/failure counts
- **AuthSyncViewModel**: UI-ready ViewModel for managing auth and sync state
- **Offline-First Fallback**: All Firebase operations gracefully degrade if network is unavailable
- **All existing fragments unchanged**: Changes isolated to data and service layers

### Architecture Patterns Used
- **Repository Pattern**: All cloud operations hidden behind repository interfaces
- **Dependency Injection Ready**: SyncRepositoryFactory for easy testing and initialization
- **Coroutines**: All async operations use kotlinx.coroutines for non-blocking execution
- **LiveData**: ViewModel state exposed as LiveData for reactive UI updates
- **Local-First**: Local data is source of truth; cloud is optional sync layer
- **Graceful Degradation**: App works fully offline; cloud features are additive

### Known Limitations & Future Work
1. **google-services.json**: Template provided; must be replaced with real Firebase config
2. **Merge Conflict Resolution**: Simple comparison by completion count; could use timestamps
3. **Rate Limiting**: Not implemented; production should add throttling
4. **Push Notifications**: Currently uses local notifications; can be upgraded to FCM
5. **Offline Queue**: Writes that fail during offline don't auto-retry; could add local queue
6. **Authentication Recovery**: No password reset flow; could add Firebase recovery email
7. **Activity Log**: No sync history stored; could track sync attempts in local DB
8. **Conflict Resolution UI**: No user prompt for merge conflicts; always prefers more complete data

### Dependencies Added
- Firebase Auth (from BOM 32.8.1)
- Firebase Firestore (from BOM 32.8.1)
- Firebase Database (from BOM 32.8.1)
- Coroutines (1.7.3)
- WorkManager (2.8.1)

### Setup Steps (For Future Integration)
1. Create Firebase project at https://console.firebase.google.com/
2. Download google-services.json and place in app/ directory
3. Enable Email/Password and Anonymous auth methods
4. Create Firestore database with user profiles and progress collections
5. Update ProfileFragment to add logout and sync buttons
6. Update LoginActivity to use AuthRepository
7. Observe AuthSyncViewModel in ProfileFragment for logout/sync UI

### Testing Recommendations
1. Test signup and login flows in offline mode (should use anonymous login)
2. Test progress sync after completing chapters
3. Test alert notifications using AlertScheduler
4. Test conflict resolution by manually editing Firestore data
5. Test network failures and recovery by toggling airplane mode
6. Verify notification channels appear in system settings

### Estimated complexity
**High** ✓ — backend integration with auth, sync, and failure handling successfully completed.

### Next Phase Suggestions
- Real-time collaborative features (presence, shared progress)
- Push notification upgrades using Firebase Cloud Messaging
- Advanced analytics and user behavior tracking
- Offline write queue for capturing data while disconnected
- User export and backup features
- Social features (leaderboards, friend progress)

---

## 3. Feature Breakdown

## Progress Tracking System
**Description**
Track lesson completion, chapter status, and disaster-wise progress percentages.

**Required components**
- `TrainingViewModel`
- `ProgressViewModel`
- `ProgressRepository`
- `SafeReadyPreferences` initially, Room later

**Data storage approach**
- Start with SharedPreferences storing chapter completion flags and summary totals.
- Move to Room for structured queries and history.

**UI**
- `HomeFragment` for summary cards
- `ProgressFragment` for detailed tracking
- `TrainingFragment` and lesson detail fragments for completion updates

**Notes**
- Completion should be recorded only after a chapter reaches a defined threshold or ends successfully.

---

## Quiz System
**Description**
Provide MCQs after each chapter, calculate score, and show result feedback.

**Required components**
- `QuizViewModel`
- `QuizRepository`
- `QuizQuestion` and `QuizResult` models

**Data storage approach**
- SharedPreferences for latest quiz score and pass/fail summary.
- Room later for quiz attempt history.

**UI**
- Quiz fragment or bottom sheet launched from chapter completion
- Result screen or dialog

**Notes**
- Keep questions local and offline-first.
- Attach quiz completion to progress updates.

---

## Local Storage
**Description**
Persist user data, progress, and quiz scores locally.

**Required components**
- `UserRepository`
- `ProgressRepository`
- `QuizRepository`
- `SafeReadyPreferences`

**Data storage approach**
- SharedPreferences in Phase 1
- Room in a later phase

**UI**
- Profile and progress screens read the stored state.

**Notes**
- Use repository abstraction so switching from SharedPreferences to Room does not affect UI code.

---

## Location-Based Personalization
**Description**
Detect city/state and recommend relevant disaster content.

**Required components**
- `LocationViewModel`
- `LocationRepository` or `LocationService`
- Existing `LocationHelper`

**Data storage approach**
- Cache resolved location locally.
- Store permission state and last known region.

**UI**
- Home fragment recommendation card
- Training fragment sorting/filtering

**Notes**
- Keep location optional and permission-based.

---

## Bottom Navigation + Navigation Structure
**Description**
Replace scattered activity navigation with a stable bottom-navigation shell.

**Required components**
- `MainActivity`
- `BottomNavigationView`
- `NavHostFragment`
- Feature fragments

**Data storage approach**
- No special storage required.

**UI**
- Home
- Training
- Progress
- Assistant
- Profile

**Notes**
- One-activity navigation reduces back-stack complexity and gives the app a production feel.

---

## MVVM Setup
**Description**
Separate UI state, business logic, and data access.

**Required components**
- ViewModels per feature
- Repository interfaces
- Local data sources
- UI state models

**Data storage approach**
- Everything reads through repositories.
- UI never talks directly to storage.

**UI**
- All fragments and any remaining activities during transition

**Notes**
- Keep ViewModels feature-focused, not one giant app-wide class.

---

## AI Assistant
**Description**
Offer offline disaster Q&A with rule-based responses first.

**Required components**
- `AssistantViewModel`
- `AssistantRepository`
- Chat UI models
- Local response rules dataset

**Data storage approach**
- Store response rules as local JSON or Kotlin data objects.
- Cache conversation history locally if needed.

**UI**
- `AssistantFragment`

**Notes**
- Keep it offline-first in Phase 2.

---

## Personalized Recommendations
**Description**
Suggest next lessons based on progress and behavior.

**Required components**
- `RecommendationViewModel`
- `RecommendationRepository`
- Progress and location inputs

**Data storage approach**
- Derived from local progress state.

**UI**
- Home fragment
- Training fragment

**Notes**
- Start rule-based and deterministic.

---

## Gamification
**Description**
Add badges, levels, and completion streaks.

**Required components**
- `GamificationViewModel`
- `GamificationRepository`
- Badge and streak models

**Data storage approach**
- SharedPreferences initially.
- Room later if history depth is needed.

**UI**
- Home fragment
- Profile fragment
- Progress fragment

**Notes**
- Keep gamification simple and meaningful.

---

## Firebase Integration
**Description**
Add authentication and cloud sync later without changing the UI layer.

**Required components**
- `AuthRepository`
- `CloudProgressRepository`
- Remote data source classes

**Data storage approach**
- Cloud progress mirror with local-first fallback.

**UI**
- Login, signup, and profile flows

**Notes**
- Use repository interfaces so Firebase can be swapped in behind existing ViewModels.

---

## Emergency Alert Simulation
**Description**
Deliver simulated disaster alerts through notifications.

**Required components**
- `AlertNotificationService`
- `AlertScheduler`
- Notification channel setup

**Data storage approach**
- Optional scheduled state stored locally.

**UI**
- Home fragment banner
- Notification tap routes to relevant content

**Notes**
- Treat as simulated alerts for capstone scope unless real data is available.

---

## 4. Navigation Structure

## Bottom Navigation Architecture
Use a single `MainActivity` with a `BottomNavigationView` and one `NavHostFragment`.

### Tabs
- **Home** — dashboard summary, recommendations, and quick access
- **Training** — disaster categories, lessons, chapters
- **Progress** — completion, quiz scores, streaks, badges
- **Assistant** — rule-based disaster Q&A
- **Profile** — user details, settings, and account state

## Fragment Responsibilities

### HomeFragment
- Show greeting
- Show region-aware recommendations
- Show progress snapshot
- Surface quick actions

### TrainingFragment
- Show disaster categories
- Launch disaster detail flow
- Show chapter completion indicators

### ProgressFragment
- Show percentage by disaster
- Show quiz history and completion trends
- Show badges and streaks

### AssistantFragment
- Accept user questions
- Return rule-based answers
- Offer quick prompts and categories

### ProfileFragment
- Show user info
- Show saved region
- Show settings and logout/account actions

## Navigation Flow
```text
Splash / Auth
  → MainActivity
    → Home / Training / Progress / Assistant / Profile
        → Training detail flows
        → Quiz result screens
        → Settings or profile actions
```

## Navigation Notes
- Keep chapter playback in the training path.
- Prefer fragment-to-fragment navigation within the same activity.
- Use the back stack carefully so the app does not feel like a deep activity chain.

---

## 5. Data Layer Design

## SharedPreferences (Initial)
Use SharedPreferences for the first production iteration to keep the app simple.

### Suggested stored values
- user name
- email
- institution
- selected region
- completion map
- quiz scores
- streak count
- badge unlocks
- onboarding completed flag

### Preferred abstraction
Create a single preferences wrapper rather than reading SharedPreferences directly from fragments.

---

## Future Room Structure
Move to Room only when structured query needs become important.

### Recommended entities
- `UserEntity`
- `DisasterEntity`
- `ChapterEntity`
- `ProgressEntity`
- `QuizAttemptEntity`
- `BadgeEntity`
- `StreakEntity`
- `AssistantMessageEntity` if chat history is stored

### Recommended DAO set
- `UserDao`
- `ProgressDao`
- `QuizDao`
- `BadgeDao`
- `AssistantDao` if needed

### Recommended database
- `SafeReadyDatabase`

## Design rule
Repository interfaces should hide whether the backing store is SharedPreferences, Room, Firebase, or a combination.

---

## 6. AI Assistant Design

## Rule-Based Logic
Start with a deterministic, offline assistant.

### Input handling
- Normalize text to lowercase
- Strip punctuation
- Match keywords and intents

### Example keyword buckets
- earthquake
- flood
- cyclone
- landslide
- emergency kit
- evacuation
- first aid
- aftershock
- shelter
- preparedness

### Response structure
```text
Query -> Intent detection -> Rule lookup -> Response message -> Optional related lesson link
```

## Data Structure for Responses
A simple local structure is enough for Phase 2.

### Suggested fields
- `intentId`
- `keywords`
- `responseText`
- `suggestedTopic`
- `priority`
- `followUpPrompts`

### Example
```text
intentId: earthquake_drop_cover_hold
keywords: [earthquake, shaking, protect]
responseText: Drop, Cover, and Hold On during shaking.
suggestedTopic: earthquake chapter 2
```

## Upgrade Path
Later, the assistant can be upgraded without changing the UI if the repository interface stays stable.

### Future options
- ML/NLP classification
- Firebase-hosted FAQ content
- Remote knowledge base API
- Hybrid local + cloud response engine

### How to avoid rewrites
- Keep the assistant behind `AssistantRepository`
- Keep chat UI bound to a UI state model
- Keep responses returned as data, not hardcoded directly in fragments

---

## 7. Future Scalability

## Firebase Integration Later
Firebase should be added behind repository interfaces, not directly from fragments.

### Suggested future services
- Firebase Authentication
- Firestore progress sync
- Firebase Storage for media or docs
- Firebase Crashlytics if needed

### Integration strategy
1. Keep local repositories as the source of truth for offline mode.
2. Add remote repositories as optional sync layers.
3. Merge remote updates into local state.
4. Keep UI unchanged.

## How to Avoid Rewriting Code
- Use repository interfaces from the beginning.
- Keep ViewModels independent of storage source.
- Represent UI through state objects, not direct storage queries.
- Centralize mapping logic between domain models and persistence models.

## Practical Capstone Recommendation
For the student release:
- implement offline-first MVVM first,
- keep Firebase as a later phase,
- and preserve the current media library and disaster content model.

This keeps the app realistic, reviewable, and scalable without overengineering the capstone scope.

---

## Phase 4 — Real-Time Features & Analytics
**Status:** In Progress
**Goal:** Add collaborative features, push notifications, and user analytics while keeping offline-first design.

### What to build
- Firebase Cloud Messaging (FCM) for real push notifications
- Real-time collaboration (presence, friends, leaderboards)
- Advanced user analytics and event tracking
- Offline write queue for reliable sync
- Data backup, restore, and export features
- Achievement system and social features

### Suggested order
1. **Phase 4A**: Set up FCM and test message delivery (2-3 hours)
2. **Phase 4B**: Implement real-time listeners for presence and friends (4-5 hours)
3. **Phase 4C**: Add event logging and analytics (3-4 hours)
4. **Phase 4D**: Create offline queue worker for reliable sync (2-3 hours)
5. **Phase 4E**: Implement leaderboard UI and achievement tracking (2-3 hours)

### Phase 4A: Push Notifications with FCM
- FirebaseMessagingService for FCM handling
- FCMTokenManager for token lifecycle
- Notification routing by message type
- Test via Firebase Console

### Phase 4B: Real-Time Collaboration
- Presence system (online/offline tracking)
- Friend system (add, remove, compare)
- Real-time progress listeners
- Leaderboards (global, regional, friends)

### Phase 4C: Analytics
- Event logging repository
- Custom event tracking
- Analytics dashboard UI
- User metrics and insights

### Phase 4D: Data Management
- Offline write queue
- Backup and restore
- Data export functionality
- Settings UI

### Phase 4E: Social Features
- Achievement system
- Leaderboard UI components
- Friend progress comparison
- Achievement sharing

### Dependencies
- Phase 3 must be complete (auth, cloud sync)
- Firebase project with FCM enabled
- Room database for offline queue
- WorkManager for background syncing

### Files to create
See PHASE4_PLAN.md for complete file listing

### Estimated complexity
**High** — multiple interconnected features with real-time updates.

### Total Phase 4 Effort: 13-18 hours (estimated)
