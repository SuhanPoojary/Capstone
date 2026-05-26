# Phase 4 Implementation Guide

## Overview
Phase 4 transforms SafeReady from a solo learning app into a collaborative, social, and data-driven platform. The phase is split into 5 sub-phases, each with clear scope and deliverables.

**Total Estimated Time:** 13-18 hours  
**Recommended Approach:** Complete 4A first, then tackle 4B-4E in parallel or in priority order

---

## Phase 4A: Push Notifications with FCM ✓ COMPLETED

### Status: COMPLETE

### What Was Delivered
1. **FCMTokenManager** (`service/FCMTokenManager.kt`)
   - Acquires FCM tokens from Firebase Cloud Messaging
   - Caches tokens locally in SharedPreferences
   - Handles token refresh and lifecycle
   - Subscribe/unsubscribe from disaster topics
   
2. **SafeReadyMessagingService** (`service/SafeReadyMessagingService.kt`)
   - Handles incoming FCM messages
   - Routes messages by type (disaster alert, sync reminder, achievement, etc.)
   - Uses NotificationHelper to display notifications
   - Supports both notification and data-only messages
   
3. **NotificationViewModel** (`presentation/viewmodel/NotificationViewModel.kt`)
   - Exposes FCM token status and subscription state to UI
   - Methods to request tokens, subscribe/unsubscribe topics
   - LiveData for reactive UI updates
   - Testing methods to verify token acquisition
   
4. **AndroidManifest.xml Updated**
   - Added `RECEIVE_BOOT_COMPLETED` permission (for background tasks)
   - Added `WAKE_LOCK` permission (for WorkManager)
   - Declared `SafeReadyMessagingService` with messaging intent filter

### How to Use Phase 4A

#### In LoginActivity or SignupActivity (after auth success):
```kotlin
val notificationViewModel = ViewModelProvider(this).get(NotificationViewModel::class.java)
notificationViewModel.requestNewToken()
// Subscribe to relevant disaster topics based on user's region
notificationViewModel.subscribeToDisasterTopics(listOf("earthquake", "flood"))
```

#### In ProfileFragment (on logout):
```kotlin
val notificationViewModel = ViewModelProvider(this).get(NotificationViewModel::class.java)
notificationViewModel.clearToken()
notificationViewModel.unsubscribeFromAllTopics()
```

#### Testing Push Notifications:
1. Go to Firebase Console → Cloud Messaging
2. Create a test notification
3. Select your device as the recipient
4. Send the message
5. SafeReadyMessagingService automatically receives and displays it

### Message Format (from Firebase Console or Backend)
```json
{
  "notification": {
    "title": "Earthquake Alert",
    "body": "Strong earthquake detected in Mumbai region"
  },
  "data": {
    "type": "disaster_alert",
    "disaster": "earthquake",
    "severity": "high",
    "actionUrl": "training://earthquake/chapter2"
  }
}
```

### Supported Message Types
- `disaster_alert` — Emergency alerts by disaster type
- `sync_reminder` — Reminds user to sync progress
- `achievement_unlocked` — Notifies about new badges
- Custom types can be added as needed

---

## Phase 4B: Real-Time Collaboration (PLANNED)

### Scope
Add presence tracking, friend systems, and real-time leaderboards for social engagement.

### Models & Repositories Created
- **Models:** `Phase4BModels.kt` with 12 data classes covering presence, friendship, leaderboards, achievements, notifications, challenges, and activity feeds
- **Repositories:** `Phase4BRepositories.kt` with 8 repository interfaces for managing all social features

### Implementation Order
1. **Presence System** (1 hour)
   - Implement PresenceRepository using Firestore Realtime listeners
   - Track online/offline status and current activity
   - Show "online" indicator next to friends in UI

2. **Friendship System** (1.5 hours)
   - Implement FriendshipRepository for requests/acceptance/blocking
   - UI for sending friend requests and viewing friends list
   - Real-time listener for friend request notifications

3. **Leaderboards** (2 hours)
   - Implement all 4 leaderboard repositories (Global, Regional, Friends, Disaster)
   - Firestore queries with pagination
   - Real-time listeners for rank changes
   - UI cards for each leaderboard type

4. **Friend Progress & Achievements** (0.5 hours)
   - Implement FriendProgressRepository for comparisons
   - Implement AchievementRepository for badge display
   - Add progress comparison screen

5. **Notifications & Challenges** (1 hour)
   - Implement RealtimeNotificationRepository
   - Implement ChallengeRepository for friend challenges
   - UI for accepting/participating in challenges

6. **Activity Feeds** (0.5 hours)
   - Implement ActivityFeedRepository
   - UI for seeing what friends are learning
   - Social engagement (likes, comments)

### Key Design Decisions
- **Local-First Caching:** Cache leaderboard data locally, update in background
- **Real-Time Updates:** Use Firestore listeners for presence and notifications
- **Conflict-Free:** Timestamps and IDs ensure consistent ordering across clients
- **Privacy:** Only show data for friends or public leaderboards

### Firebase Structure Needed
```
firestore/
├── users/{uid}
│   └── presence/  (latest online status)
├── friendships/{uid}
│   └── [friendId]: {status, createdAt, ...}
├── leaderboards/
│   ├── global/[timeperiod]/[rank]/
│   ├── regional/[region]/[rank]/
│   ├── friends/{uid}/[rank]/
│   └── disaster/{disasterKey}/[rank]/
├── challenges/{challengeId}/
│   └── participants/{userId}/
├── activities/{activityId}/
└── notifications/{userId}/[notificationId]/
```

### Estimated Effort: 4-5 hours

---

## Phase 4C: Analytics & Event Tracking (PLANNED)

### Scope
Comprehensive user behavior tracking, crash reporting, and performance monitoring.

### Models & Repositories Created
- **Models:** `Phase4CModels.kt` with 10 data classes for events, sessions, metrics, crashes, insights
- **Repositories:** `Phase4CRepositories.kt` with 8 repository interfaces

### Implementation Order
1. **Event Logging** (1 hour)
   - Implement AnalyticsRepository with local SQLite queue
   - Batch events and send to Firebase or backend
   - Support: page views, actions, content interaction, assessments, errors

2. **Session Tracking** (1 hour)
   - Implement SessionRepository
   - Auto-create sessions on app launch
   - Track lessons and quizzes within sessions

3. **Metrics Aggregation** (1 hour)
   - Implement MetricsRepository
   - Cloud functions to aggregate events into metrics
   - Funnel analysis (onboarding → first lesson → quiz)
   - Retention cohort analysis

4. **Heatmaps & Crashes** (0.5 hours)
   - Implement HeatmapRepository for feature usage
   - Implement CrashRepository for crash logs

5. **Performance & Insights** (0.5 hours)
   - Implement PerformanceRepository for app metrics
   - Implement InsightsRepository for behavioral patterns

### Key Design Decisions
- **Offline-First:** Queue events locally, sync when online
- **Privacy:** Anonymize events; don't track sensitive data
- **Batching:** Send events in batches for efficiency
- **Sampling:** For high-frequency events, use sampling to reduce bandwidth

### Integration Points
- Log events at key user actions (lesson completed, quiz taken, badge earned)
- Log page views in each Fragment.onViewCreated()
- Catch and log exceptions app-wide
- Periodically sync event queue to backend

### Estimated Effort: 3-4 hours

---

## Phase 4D: Offline Write Queue (PLANNED)

### Scope
Enable reliable data sync even when network is intermittent or unavailable.

### Models & Repositories Created
- **Models:** `Phase4DModels.kt` with 6 data classes for queue management
- **Repositories:** `Phase4DRepositories.kt` with 5 repository interfaces

### Implementation Order
1. **Room Database Setup** (0.5 hours)
   - Create `WriteQueueEntity` and `DAO`
   - Database migration strategy

2. **Queue Operations** (1 hour)
   - Implement OfflineQueueRepository
   - Enqueue/dequeue operations
   - Retry logic with exponential backoff

3. **Sync Worker** (0.5 hours)
   - Implement SyncRepository
   - Batch sync for efficiency
   - Network listener to trigger sync when online

4. **Conflict Resolution** (0.5 hours)
   - Implement ConflictRepository
   - Merge strategies (prefer local, prefer remote, timestamp-based)
   - UI for manual conflict resolution if needed

5. **Checkpoints & Recovery** (0.5 hours)
   - Implement CheckpointRepository
   - Track sync progress for large datasets
   - Recovery from interrupted syncs

### Key Design Decisions
- **Durability:** All writes go to local DB first, then uploaded
- **Ordering:** Maintain operation ordering to prevent inconsistencies
- **Batching:** Batch writes for efficiency (default 25 per request)
- **Backoff:** Exponential backoff (5s → 10s → 20s → ... → 5min max)
- **TTL:** Clean up successful writes after 30 days

### Integration Points
- Use in ProgressRepository, UserRepository, QuizRepository
- Wrap all Firestore writes in the queue
- Observe sync state in Profile/Settings UI
- Show "syncing..." indicator to user

### Estimated Effort: 2-3 hours

---

## Phase 4E: Achievements & Leaderboards (PLANNED)

### Scope
Advanced gamification with achievement system and real-time leaderboard rankings.

### Models & Repositories Created
- **Models:** `Phase4EModels.kt` with 15 data classes for achievements, leaderboards, stats
- **Repositories:** `Phase4ERepositories.kt` with 9 repository interfaces + 1 facade

### Implementation Order
1. **Achievement System** (1 hour)
   - Implement AchievementSystemRepository
   - Create achievement definitions in Firestore
   - Auto-evaluate conditions and award achievements
   - Notify user when achievement unlocked

2. **Global Leaderboards** (0.5 hours)
   - Implement GlobalLeaderboardRepository
   - Firestore queries for ranking
   - Real-time listener updates
   - Show current user's rank and nearby competitors

3. **Regional & Friend Leaderboards** (0.75 hours)
   - Implement RegionalLeaderboardRepository
   - Implement FriendLeaderboardRepository
   - Filter by region or friend list

4. **Disaster-Specific Leaderboards** (0.25 hours)
   - Implement DisasterLeaderboardRepository
   - Rank users by specific topic expertise

5. **Personal Stats & Badges** (0.5 hours)
   - Implement PersonalStatsRepository
   - Badge collection with favorite badges display
   - Share achievements to activity feed

6. **Milestones** (0.5 hours)
   - Implement MilestoneRepository
   - Track level-ups, streaks, major milestones
   - Special notifications for milestone achievements

7. **UI Screens** (1 hour)
   - Achievement/badge display fragment
   - Leaderboard UI with tabs for each type
   - Personal stats/profile card
   - Milestone timeline

### Achievement Examples
```
- First Lesson: Complete first lesson
- Scholar: Complete 10 lessons
- Quiz Master: Average 90% on quizzes
- Earthquake Expert: Complete all earthquake lessons
- Consistent Learner: 7-day learning streak
- Social Butterfly: Add 10 friends
- Rising Star: Reach top 100 global leaderboard
- Legend: Reach level 10
```

### Key Design Decisions
- **Unlocking:** Conditions auto-evaluated on relevant actions
- **Visible Progress:** Show progress bar toward next achievement
- **Hidden Achievements:** Some achievements are secret until earned
- **Rarity:** Common to Legendary, with cosmetic differences
- **Social Sharing:** Let users share achievements to feed

### Estimated Effort: 2-3 hours (UI excluded; API planning only)

---

## Recommended Phase 4 Roadmap

### Sprint 1: Core Infrastructure (4-5 hours)
- **4A:** Complete FCM setup and test ✓
- **4D:** Set up Room database and basic offline queue (0.5-1 hour bonus)

### Sprint 2: Real-Time Features (4-5 hours)
- **4B:** Implement presence, friendship, and leaderboards

### Sprint 3: Analytics & Gamification (5-6 hours)
- **4C:** Implement event tracking and metrics
- **4E:** Implement achievements and UI

### Optional Sprint 4: Data Management (2-3 hours)
- **4D:** Complete conflict resolution and checkpoint system (if not done in Sprint 1)

---

## Integration Checklist

### In Firebase Console
- [ ] Enable FCM (Cloud Messaging)
- [ ] Create collections for leaderboards, achievements, challenges, activities
- [ ] Set up appropriate security rules
- [ ] Create achievement definitions in Firestore

### In AndroidManifest.xml
- [x] Added FCMTokenManager and SafeReadyMessagingService
- [ ] Add any broadcast receivers for network change notifications (Phase 4D)

### In SafeReadyApp.kt
- [x] Initialize NotificationHelper (done in Phase 3)
- [ ] Initialize analytics event collector (Phase 4C)
- [ ] Initialize offline queue worker (Phase 4D)

### In AuthRepository
- [ ] Call NotificationViewModel.requestNewToken() on login
- [ ] Call NotificationViewModel.clearToken() on logout
- [ ] Subscribe to disaster topics on region selection

### In Repositories
- [ ] Wrap all writes with OfflineQueueRepository (Phase 4D)
- [ ] Log relevant events in AnalyticsRepository (Phase 4C)
- [ ] Check achievement conditions on user activity (Phase 4E)

### In UI Fragments
- [ ] Observe leaderboard LiveData (Phase 4B)
- [ ] Observe achievement notifications (Phase 4E)
- [ ] Display sync status indicator (Phase 4D)
- [ ] Show achievement unlock toasts (Phase 4E)

---

## Testing Strategy

### Phase 4A (FCM)
1. Build and run app
2. Check if token is acquired (logcat search for "FCM Token:")
3. Send test message from Firebase Console
4. Verify notification appears and routes correctly

### Phase 4B (Real-Time)
1. Run two instances of app (emulator + device)
2. Add each other as friends
3. Verify friend requests sync in real-time
4. Check leaderboard updates as users progress

### Phase 4C (Analytics)
1. Perform user actions (open lessons, take quizzes)
2. Check event queue in local database
3. Verify events sync to Firestore/backend
4. Query analytics dashboard

### Phase 4D (Offline Queue)
1. Put device in airplane mode
2. Make changes (complete lesson, take quiz)
3. Verify writes queue locally
4. Turn on network
5. Verify writes sync and queue clears

### Phase 4E (Achievements)
1. Complete achievement conditions
2. Verify achievement unlocks
3. Check leaderboard ranking
4. Verify badge appears in collection

---

## Success Metrics

After Phase 4, the app should:
- ✓ Support push notifications from Firebase
- ✓ Sync real-time leaderboards and friend progress
- ✓ Track detailed user behavior and analytics
- ✓ Work offline with reliable sync when network available
- ✓ Motivate users with achievements and badges
- ✓ Show comprehensive stats and rankings

---

## Known Limitations & Future Enhancements

### Phase 4A
- No FCM token validation; could add server-side verification
- No token rotation strategy; Firebase handles auto-rotation
- No message priority/expiration handling yet

### Phase 4B
- No presence timeout; implement presence refresh every 5 minutes
- No rate limiting on friend requests; could add quotas
- Leaderboard caching is simple; could add more sophisticated caching

### Phase 4C
- No data retention policy; consider archiving old events
- No custom dashboards; basic queries only
- No A/B testing framework

### Phase 4D
- No encryption of local queue; consider adding
- No compression for batch sync; could reduce bandwidth
- No automatic recovery from database corruption

### Phase 4E
- No achievement customization per region/disaster
- No dynamic achievement generation
- No seasonal/limited-time achievements

---

## Questions & Support

For implementation questions:
1. Check the corresponding Phase4*Models.kt and Phase4*Repositories.kt files
2. Review the interface method signatures for required parameters
3. Check Firestore documentation for query examples
4. Refer to Android Architecture Components docs for LiveData patterns


