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
**Status:** ✅ COMPLETED (Verified 2026-05-05, Firebase intentionally disabled)
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
**Status:** Partially Completed (Phase 4A complete, phases 4B-4E planned)
**Goal:** Add collaborative features, push notifications, and user analytics while keeping offline-first design.

### Phase 4A: Push Notifications with FCM ✓ COMPLETED
**Status:** ✅ COMPLETED

#### Delivered Components
- `FCMTokenManager.kt` — Manages FCM token lifecycle, subscription, and cleanup
- `SafeReadyMessagingService.kt` — Handles incoming FCM messages and routing
- `NotificationViewModel.kt` — Exposes FCM state to UI
- `AndroidManifest.xml` updated — Added FCM service and required permissions (RECEIVE_BOOT_COMPLETED, WAKE_LOCK)

#### How It Works
1. FCMTokenManager acquires tokens on first launch and caches them locally
2. SafeReadyMessagingService receives messages from Firebase Cloud Messaging
3. Messages can be notification messages (auto-display) or data messages (custom handling)
4. Supports message types: disaster_alert, sync_reminder, achievement_unlocked
5. UI state via NotificationViewModel for testing and debugging

#### Integration Points
- Called from SafeReadyApp on app init (placeholder comment)
- Can be wired into AuthRepository when user logs in
- Notification routing uses existing NotificationHelper

#### Next Steps
- Call `requestNewToken()` in LoginActivity after auth succeeds
- Subscribe to disaster topics after user selects region
- Test message delivery via Firebase Console

### Phase 4B: Real-Time Collaboration (PARTIALLY COMPLETED)
**Status:** 🟡 PARTIAL - Models & repositories created, UI work remains

#### Current Priority Update (2026-05-05)
- Firebase ownership is now available for this project.
- Continue with UI-first delivery: leaderboard/friends/activity feed screens before deep backend polish.

#### Delivered Implementations
- `FirebasePresenceRepository.kt` — Real-time user presence tracking
- `FirebaseFriendshipRepository.kt` — Friend request/acceptance system
- `FirebaseGlobalLeaderboardRepository.kt` — Global user rankings
- `FirebaseRegionalLeaderboardRepository.kt` — Regional rankings
- `FirebaseFriendLeaderboardRepository.kt` — Friends-only rankings
- `LeaderboardViewModel.kt` — UI-ready ViewModel for all leaderboard types
- `Phase4HubActivity.kt` + Profile entry button — UI-first collaboration hub scaffold
- `Phase4LeaderboardActivity.kt` + `activity_phase4_leaderboard.xml` — first Firebase-backed leaderboard UI

#### How It Works
1. Presence system publishes online status to Firestore on login
2. Real-time listeners notify when friends come online/offline
3. Friendship system manages bidirectional friend relationships
4. Leaderboard queries retrieve ranked user lists sorted by points
5. ViewModel exposes leaderboard data as LiveData for UI consumption
6. All repositories support real-time updates via Firestore listeners

#### What's Still Needed
- [x] Regional/Friends leaderboard screens (global leaderboard first slice completed)
- [x] Friends list UI and management screens
- [ ] Friend request notifications
- [x] Activity feed UI (friend progress sharing)
- [ ] Firestore collection setup and indexing
- [ ] LeaderboardViewModel dependency injection setup
- [ ] Replace Phase4Hub placeholders with real leaderboard/friends/feed fragment flows

#### Integration Points Ready
- Use LeaderboardViewModel in any Fragment
- Call `presenceRepo.publishPresence()` in AuthRepository on login
- Set up real-time listeners in onCreate/onDestroy
- Observe LiveData for reactive UI updates

#### Estimated Effort Remaining: 3-4 hours (initial global leaderboard UI completed)

### Phase 4C: Analytics & Event Tracking (PLANNED)
**Status:** 🔴 PLANNED (models/interfaces prepared)

#### Models Created
- `Phase4CModels.kt` — Complete data classes for:
  - AnalyticsEvent, EventType enum
  - LearningSession
  - AnalyticsMetrics, TimePeriod enum
  - PageMetric, ContentMetric, ConversionMetrics, RetentionMetrics
  - FeatureHeatmap, CrashMetrics, PerformanceMetric
  - UserBehaviorInsight, CustomEvent

#### Repository Interfaces Created
- `AnalyticsRepository` — Log events, page views, actions, content interaction, assessments, errors
- `SessionRepository` — Track learning sessions
- `MetricsRepository` — Query aggregated metrics
- `HeatmapRepository` — Feature usage heatmaps
- `CrashRepository` — Report and track crashes
- `PerformanceRepository` — Log and monitor performance
- `InsightsRepository` — Generate behavioral insights
- `CustomEventRepository` — Track custom app events

#### What to Build
1. Event logging system (local + cloud)
2. Session tracking and aggregation
3. Analytics dashboard UI
4. Crash reporting integration
5. Performance monitoring
6. User behavior insights calculation

#### Estimated effort: 3-4 hours

### Phase 4D: Offline Write Queue (PLANNED)
**Status:** 🔴 PLANNED (models/interfaces prepared)

#### Models Created
- `Phase4DModels.kt` — Complete data classes for:
  - QueuedWrite, WriteStatus enum
  - SyncState
  - SyncConflict, ConflictResolution enum
  - OfflineQueueStats, OfflineQueueConfig
  - WriteBatch, BatchStatus enum
  - SyncCheckpoint

#### Repository Interfaces Created
- `OfflineQueueRepository` — Queue writes, manage pending/failed writes
- `SyncRepository` — Manually trigger sync, manage sync state
- `ConflictRepository` — Resolve sync conflicts
- `CheckpointRepository` — Track sync progress
- `OfflineSyncWorker` — Orchestrate entire sync workflow

#### What to Build
1. Local database (Room) for write queue storage
2. Auto-retry logic with exponential backoff
3. Conflict resolution strategies (prefer local, prefer remote, merge)
4. Batch sync for efficiency
5. Offline queue UI (show pending writes, retry options)
6. Network connectivity listener

#### Estimated effort: 2-3 hours

### Phase 4E: Achievements & Leaderboards (PLANNED)
**Status:** 🔴 PLANNED (Models & repository interfaces exist, no UI implementation)

#### Models Created
- `Phase4EModels.kt` — Complete data classes for:
  - AchievementDefinition, UnlockCondition sealed class variants
  - EarnedAchievement, UserAchievementState
  - GlobalLeaderboardEntry, RegionalLeaderboardEntry, FriendLeaderboardEntry, DisasterLeaderboardEntry
  - MilestoneAchievement, MilestoneType enum
  - AchievementProgress, AchievementNotification
  - LeaderboardStats, PersonalStatsCard
  - BadgeCollection, SharedAchievement

#### Repository Interfaces Created
- `AchievementSystemRepository` — Award achievements, track progress, evaluate conditions
- `GlobalLeaderboardRepository` — Global user rankings
- `RegionalLeaderboardRepository` — Regional rankings
- `FriendLeaderboardRepository` — Friends-only rankings
- `DisasterLeaderboardRepository` — Disaster-specific rankings
- `PersonalStatsRepository` — Personal achievement and stats displays
- `MilestoneRepository` — Track milestones (level up, streaks, etc)
- `AchievementNotificationRepository` — Notify users of achievements
- `AchievementSystemFacade` — Comprehensive achievement/leaderboard management

#### What to Build
1. Achievement definitions and unlock conditions
2. Auto-evaluation of achievement conditions
3. Leaderboard queries with ranking
4. Badge collection display and sharing
5. Achievement UI screens
6. Stat dashboards and profiles
7. Real-time leaderboard updates

#### Estimated effort: 2-3 hours

---

### Phase 4 Dependencies
- ✓ Phase 3 complete (auth, cloud sync)
- ✓ Firebase project configured with FCM
- ⚠️ Room database (for offline queue in Phase 4D)
- ✓ WorkManager (already added in Phase 3)
- ⚠️ Firestore collections for leaderboards/social (Phase 4B+)

### Phase 4 Files Created
**Phase 4A (FCM):**
- `service/FCMTokenManager.kt`
- `service/SafeReadyMessagingService.kt`
- `presentation/viewmodel/NotificationViewModel.kt`
- AndroidManifest.xml (updated)

**Phase 4B-E (Models & Repositories):**
- `data/Phase4BModels.kt`
- `data/Phase4CModels.kt`
- `data/Phase4DModels.kt`
- `data/Phase4EModels.kt`
- `data/repository/Phase4BRepositories.kt`
- `data/repository/Phase4CRepositories.kt`
- `data/repository/Phase4DRepositories.kt`
- `data/repository/Phase4ERepositories.kt`

### Total Phase 4 Effort: 13-18 hours (Phase 4A complete, 4B-4E remaining)

---

# 🔵 PHASE 5 — Offline Disaster Mesh Network
**Status:** 🟡 IN PROGRESS - Core components built, Nearby Connections integration and Room migration in progress

## Phase 5 Completion Summary (as of 2026-05-05)
**✅ Completed:**
- `MeshService.kt` — Nearby Connections transport layer with advertising, discovery, and send/receive
- `MeshMessageCache.kt` — SharedPreferences-based message persistence with serialization
- `MeshRepository.kt` — Repository abstraction bridging service and cache with retry logic
- `MeshViewModel.kt` — UI-facing state management with connection state and telemetry
- `MeshDebugActivity.kt` & `MeshDebugFragment.kt` — Debug UI for testing mesh functionality
- Message retry with telemetry (sent, relayed, failed, retried, dropped duplicate/expired)
- Manual resend action for failed messages with outcome feedback
- Room database schema and migration scaffolding via `MeshRoomMigrationPlan.kt`
- Runtime permission handling for Bluetooth and location
- Emergency Mode gating for mesh usage implemented (UI + activity + ViewModel/repository guards)

**🔴 Remaining (High Priority):**
1. Migrate message cache from SharedPreferences to Room database
2. Complete Nearby Connections API integration in `MeshService`
3. Add SOS send action in HomeFragment and EmergencyFragment (EmergencyActivity added; HomeFragment has a quick-send flow)
4. Battery-friendly discovery scheduling
5. End-to-end testing of store-and-forward relaying
6. Unit and integration tests for MessageCache behavior

## Goal
Enable basic device-to-device communication when internet is unavailable using an opportunistic, store-and-forward mesh-like system.

## Key Concept
- Opportunistic Delay-Tolerant Networking (DTN) pattern: devices exchange small messages when in range and relay them further later.
- Store-and-forward messaging with limited TTL and duplicate suppression.

## Features
- Broadcast distress / SOS signals
- Relay messages between nearby devices
- Nearby device discovery and connection management
- Offline message propagation with TTL and duplicate filtering
- Basic message acknowledgment to avoid infinite re-transmit loops

## Technology Choice
Preferred: Nearby Connections API
- Pros: High-level APIs for discovery, reliable/data streams, handles multiple transports (Bluetooth, BLE, Wi‑Fi) where available.
- Cons: Requires Google Play services on device; permission model to manage.

Fallback: Bluetooth Classic / BLE
- Pros: Broader device reach on devices without Play Services if necessary.
- Cons: Lower-level, more engineering effort, connection stability and throughput limitations.

### Tradeoffs
- Nearby Connections simplifies implementation and avoids low-level Bluetooth pitfalls; use it for Phase 5 initial rollout.
- If Nearby proves unavailable in field tests, consider a BLE/Classic fallback for critical features only (SOS, tiny payloads).

---

## Architecture (Message lifecycle)
Message lifecycle: Create → Broadcast/Advertise → Receive → Store → Forward → Expire

Important details:
- Message ID (UUID) for de-duplication
- TTL (time-based and optional hop count)
- Duplicate filtering using seen-message-set with expiry
- Acknowledgement with short ACK messages to prevent re-sends
- Bounded retry and backoff to preserve battery

---

## Data Models

Message (MeshMessage)
- id: String (UUID)
- senderId: String (deviceId / userId)
- timestamp: Long (epoch ms)
- type: Enum (SOS, ALERT, INFO, RELAY)
- content: String (JSON or compact payload)
- location: {lat: Double, lng: Double}? (optional)
- signalStrength: Int? (RSSI sample on receive)
- ttl: Int or expiresAt: Long

Device (MeshDevice)
- deviceId: String
- lastSeen: Long
- signalStrength: Int

---

## Components
- MeshService: long-running component handling discovery, connections, send/receive, and relaying. Prefer WorkManager/foreground service if continuous background operation is required.
- MeshRepository: repository abstraction for sending/receiving messages, persistence, and business rules.
- MeshViewModel: UI-facing state and commands (send SOS, view cache, telemetry).
- MessageCache: local persistence for messages (initially in SharedPreferences/file-based cache; migrate to Room soon).
- DeviceDiscoveryManager: tracks nearby peers and connection health.

---

## Location Estimation (Fallback when GPS fails)
If GPS is unavailable or inaccurate, fall back to a multi-signal heuristic:
1. RSSI (signal strength) to estimate relative distance. Use Tx power calibration when available and a path-loss model (e.g., RSSI_to_distance ≈ 10 ^ ((txPower - RSSI) / (10 * n)), with n ≈ 2-4). Accuracy is low and should be treated as relative ranking only.
2. Device proximity ranking: order peers by RSSI to identify closest devices.
3. Last known GPS: include last valid GPS coordinates if within a reasonable age window (e.g., 10–30 minutes).
4. Multi-hop triangulation: combine last-known locations of multiple hops to approximate a source position (very approximate, use only for human-readable hints).

Notes on RSSI → distance:
- RSSI-based distance estimates are noisy: walls, pockets, device models, and orientation affect readings.
- Use moving-average smoothing and sample over a short time window (3–6 samples) before using RSSI for decisions.
- Present estimated location with uncertainty to users.

---

## Limitations
- Short range and variable reliability.
- Battery usage must be carefully managed (duty-cycling, limited scanning intervals).
- Message delivery delay is expected and acceptable for non-real-time use.
- Location estimates are approximate.

---

# 🔴 PHASE 6 — Emergency Mode System
**Status:** 🟡 IN PROGRESS (Phase 6 foundation started: Emergency Mode toggle and gating)

## Trigger Conditions
Emergency Mode activates when:
1. Fall detection with no response within 30 seconds
2. Manual SOS button press
3. External event ingestion (news/disaster API — optional)
4. Scheduled predicted disaster event (calendar/notification)

## Features
- Broadcast SOS via cloud (when online) and mesh (when offline)
- Share last known location, status (injured/safe/trapped), and optional text
- Auto-repeat broadcasts with configurable interval and backoff
- Rescue acknowledgment system so rescuer devices can mark messages as acknowledged
- Local escalation UI and audible alerts when in Emergency Mode

## UI Flow
Emergency Mode screens:
1. Activation screen (confirm or auto-activate)
2. SOS broadcast screen (shows broadcast status, last sent, peers reached)
3. Rescue acknowledgment screen (incoming acknowledgment and responder details)
4. Survivor status dashboard (history of SOS messages, acknowledged, and last known location)

## Data Flow
Trigger → EmergencyViewModel → EmergencyRepository → MeshRepository / CloudRepository → Nearby devices / Cloud

## Components
- EmergencyViewModel
- EmergencyRepository
- SOSMessage model (specialization of MeshMessage)
- EmergencyController (manages auto-repeat and escalation logic)
- Emergency UI fragments/screens

---

# 🟡 PHASE 7 — UI/UX REDESIGN SYSTEM
**Status:** 🔴 NOT STARTED (Planned for Phase 7)

## Goal
Make the UI modern, calm, and consistent (ChronoVault-inspired) while keeping accessibility and performance in mind.

## Design System Rules
### Typography
- Single font family (e.g., Inter or Poppins) for app; use weights consistently.

### Icons
- Use vector icons only (Material Icons or Phosphor). Avoid emojis in UI.

### Bottom Navigation
- Floating, rounded container with soft elevation and active tab highlight.
- Smooth transitions and micro-interactions when switching tabs.

### Colors
- Soft, accessible gradients; high contrast for text; avoid saturated reds except for critical alerts.

### Cards
- Rounded corners, subtle elevation, consistent padding.

### Animations
- Subtle; prefer crossfade and small translate/elevate motions.

### Accessibility
- Scalable text sizes; proper color contrast; talkback-friendly element descriptions.

### UI Elements to Improve
- Home dashboard: clearer KPI cards and quick actions
- Emergency screens: big, clear SOS controls and status
- Progress screen: visual progress rings and per-disaster breakdown
- Assistant screen: chat bubbles, suggested chips
- Map UI: simplified markers and clustering for rescuer devices

---

# 🧠 PHASE 8 — ML / AI PLACEHOLDER STRUCTURE
**Status:** 🔴 NOT STARTED (Planned for Phase 8)

## Goal
Prepare stubs and interfaces so ML components can be integrated later without major refactors.

## Additions
- RiskPredictionViewModel (exposes mocked risk outputs)
- ModelInterface: interface that defines predict(inputs): RiskOutput
- DummyModel implementation that returns deterministic/mock outputs.

Example mocked outputs:
- "High flood risk"
- "Elevated earthquake exposure"

## Future Integration
- Replace DummyModel with on-device TFLite model or a cloud API.
- Keep ModelInterface stable so ViewModels and UI do not change.

---

# Integration notes and next steps
- Append these phases to `PLAN.md` and update `progress.md` to reflect the new scope.
- Priority order now: 1) Emergency-mode-first UX, 2) Phase 5 mesh completion, 3) Phase 4B UI rollout (Firebase-owned project), 4) deeper backend polish.
- Immediate UI-first next step: add dedicated Emergency screen and SOS controls, then add Phase 4B leaderboard entry and initial fragment UI.
- Keep energy efficiency and clear consent prompts for all background/mesh flows.

---
