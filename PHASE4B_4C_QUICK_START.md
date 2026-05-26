# Phase 4B+4C Quick Start Guide

**Updated:** April 30, 2026  
**Phase 4B Status:** ✅ Core implementations complete  
**Phase 4C Status:** 🚧 Ready to build

---

## Phase 4B: What You Have

### Repositories (Ready to Use)
```kotlin
✅ FirebasePresenceRepository
   - Publish/update user presence
   - Real-time friend status listeners
   - Activity tracking

✅ FirebaseFriendshipRepository  
   - Send/accept/reject friend requests
   - Block users
   - Real-time friend list updates

✅ Firebase Leaderboard Repositories
   - Global rankings (all users)
   - Regional rankings (by location)
   - Friends-only rankings
```

### ViewModel (Ready to Use)
```kotlin
✅ LeaderboardViewModel
   - Exposes all 3 leaderboard types via LiveData
   - Handles loading states and errors
   - User rank queries
   - Real-time updates
```

### What Needs to Be Built (Next 3-4 hours)

#### 1. Leaderboard UI Fragment (1.5 hours)
**Location:** `presentation/fragment/LeaderboardFragment.kt`

```kotlin
class LeaderboardFragment : Fragment() {
    private lateinit var viewModel: LeaderboardViewModel
    private lateinit var adapter: LeaderboardAdapter
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set up ViewModel
        viewModel = ViewModelProvider(this)[LeaderboardViewModel::class.java]
        
        // Set up RecyclerView
        adapter = LeaderboardAdapter()
        recyclerView.adapter = adapter
        
        // Observe global leaderboard
        viewModel.observeGlobalLeaderboard()
        viewModel.globalLeaderboard.observe(viewLifecycleOwner) { entries ->
            adapter.submitList(entries)
        }
        
        // Load user's rank
        viewModel.loadUserGlobalRank(currentUserId)
        viewModel.globalRank.observe(viewLifecycleOwner) { rank ->
            userRankText.text = "#$rank"
        }
    }
}
```

**Adapter to Build:**
```kotlin
class LeaderboardAdapter : ListAdapter<GlobalLeaderboardEntry, LeaderboardViewHolder>(
    DiffCallback()
) {
    // Bind rank #1, name, points, level
    // Highlight current user
    // Show online indicator if friends
}
```

#### 2. Friends List UI Fragment (1 hour)
**Location:** `presentation/fragment/FriendsFragment.kt`

```kotlin
class FriendsFragment : Fragment() {
    private lateinit var viewModel: FriendsViewModel
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Observe friend list
        viewModel.observeFriends(currentUserId)
        viewModel.friendsList.observe(viewLifecycleOwner) { friends ->
            // Show friend list with online indicator
            // Each friend: name, level, points, online status
        }
        
        // Show pending requests
        viewModel.getPendingRequests(currentUserId)
        viewModel.pendingRequests.observe(viewLifecycleOwner) { pending ->
            // Accept/reject buttons
        }
    }
}
```

**FriendsViewModel to Build:**
```kotlin
class FriendsViewModel : ViewModel() {
    val friendsList: LiveData<List<Friendship>>
    val pendingRequests: LiveData<List<Friendship>>
    
    fun observeFriends(userId: String)
    fun getPendingRequests(userId: String)
    fun sendFriendRequest(toUserId: String)
    fun acceptFriendRequest(fromUserId: String)
    fun removeFriend(friendId: String)
}
```

#### 3. Add Tab Navigation (1 hour)
Update existing `HomeFragment` or create `StatsFragment`:

```kotlin
// Tab 1: Global Leaderboard
// Tab 2: Regional Leaderboard  
// Tab 3: Friends Leaderboard
// Tab 4: Friends List
```

---

## Phase 4C: What's Ready to Build

### Models Already Defined
```kotlin
✅ AnalyticsEvent, EventType enum
✅ LearningSession
✅ AnalyticsMetrics, ConversionMetrics, RetentionMetrics
✅ CrashMetrics, PerformanceMetric
```

### Repositories Interfaces Ready
- `AnalyticsRepository` - Log events
- `SessionRepository` - Track sessions
- `MetricsRepository` - Query aggregated metrics
- `CrashRepository` - Report crashes
- `PerformanceRepository` - Monitor performance

### Phase 4C Implementation Plan (3-4 hours)

#### Step 1: Create Room Database (1 hour)
```kotlin
// entity/AnalyticsEventEntity.kt
@Entity(tableName = "analytics_events")
data class AnalyticsEventEntity(
    @PrimaryKey val eventId: String,
    val userId: String,
    val eventType: String,
    val eventName: String,
    val timestamp: Long
)

// dao/AnalyticsDao.kt
@Dao
interface AnalyticsDao {
    @Insert
    suspend fun logEvent(event: AnalyticsEventEntity)
    
    @Query("SELECT * FROM analytics_events WHERE userId = :userId")
    suspend fun getUserEvents(userId: String): List<AnalyticsEventEntity>
}

// database/SafeReadyAnalyticsDatabase.kt
@Database(entities = [AnalyticsEventEntity::class], version = 1)
abstract class SafeReadyAnalyticsDatabase : RoomDatabase() {
    abstract fun analyticsDao(): AnalyticsDao
}
```

#### Step 2: Implement AnalyticsRepository (1 hour)
```kotlin
class LocalAnalyticsRepository(private val database: SafeReadyAnalyticsDatabase) : AnalyticsRepository {
    override suspend fun logEvent(event: AnalyticsEvent): Result<Unit> {
        return try {
            database.analyticsDao().logEvent(
                AnalyticsEventEntity(
                    eventId = event.eventId,
                    userId = event.userId,
                    eventType = event.eventType.name,
                    eventName = event.eventName,
                    timestamp = event.timestamp
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

#### Step 3: Wire into Existing Code (1 hour)
```kotlin
// In HomeFragment, TrainingFragment, etc.
// Log page views
analyticsRepo.logPageView("home_fragment", userId)

// In QuizViewModel  
// Log quiz attempts
analyticsRepo.logQuizAssessment(
    quizId = quiz.id,
    userId = userId,
    score = userScore,
    passed = userScore >= 80,
    timeSpent = elapsedSeconds
)

// In any error handler
// Log errors
analyticsRepo.logError(
    "NullPointerException",
    e.message ?: "Unknown error",
    e.stackTraceToString(),
    userId
)
```

---

## Firestore Collections Setup

### Required Collections:

```javascript
// For Phase 4B
firestore.collection("friendships").document(userId).collection("contacts")
firestore.collection("leaderboards").document("global").collection("allTime")
firestore.collection("leaderboards").document("regional").collection(regionName)
firestore.collection("leaderboards").document("friends").collection(userId)
firestore.collection("users").document(userId).collection("presence")

// For Phase 4C (optional, data can stay local)
firestore.collection("analytics").document(userId).collection("events")
firestore.collection("crashes").document(crashId)
```

### Firestore Indexes Needed:

For leaderboard queries to work efficiently:
```
Collection: leaderboards/global/allTime
Index: rank (Ascending)

Collection: leaderboards/regional/{region}
Index: rank (Ascending)

Collection: leaderboards/friends/{userId}
Index: rank (Ascending)

Collection: friendships/{userId}/contacts
Index: status (Ascending), createdAt (Descending)
```

Firestore will automatically suggest these when you first query.

---

## Build Gradle Updates

### Add Room Database (for Phase 4C):
```gradle
// app/build.gradle.kts
dependencies {
    // Room for offline analytics
    implementation("androidx.room:room-runtime:2.5.1")
    implementation("androidx.room:room-ktx:2.5.1")
    kapt("androidx.room:room-compiler:2.5.1")
}
```

### Already Included:
```gradle
✅ Firebase Firestore
✅ Firebase Messaging (FCM)
✅ Coroutines
✅ WorkManager
✅ ViewModel & LiveData
```

---

## Integration with Existing Code

### In HomeFragment
```kotlin
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    
    // Log page view
    analyticsRepository.logPageView("home_fragment", currentUserId)
    
    // Show stats card with rank
    leaderboardViewModel.loadUserGlobalRank(currentUserId)
    leaderboardViewModel.globalRank.observe(viewLifecycleOwner) { rank ->
        // Display rank in stats card
    }
}
```

### In ProfileFragment
```kotlin
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    // Show user's global, regional, and friends rank
    leaderboardViewModel.loadUserGlobalRank(userId)
    leaderboardViewModel.loadRegionalLeaderboard(userRegion)
    leaderboardViewModel.loadFriendsLeaderboard(userId)
}
```

### In MainActivity
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Initialize repositories
    val presenceRepo = FirebasePresenceRepository(FirebaseFirestore.getInstance())
    val friendshipRepo = FirebaseFriendshipRepository(FirebaseFirestore.getInstance(), userId)
    
    // On login
    AuthManager.onLoginSuccess { userId ->
        presenceRepo.publishPresence(userId, "app_active")
    }
    
    // On logout
    AuthManager.onLogout { userId ->
        presenceRepo.markUserOffline(userId)
        presenceRepo.cleanup()
    }
}
```

---

## Testing Checklist

### Phase 4B Testing:
- [ ] Can send friend request from User A to User B
- [ ] User B sees pending request and can accept
- [ ] Both users appear on each other's friend list
- [ ] User A's online indicator appears in User B's UI
- [ ] Global leaderboard updates when user earns points
- [ ] Regional leaderboard shows correct users for region
- [ ] User's rank displays correctly on all 3 leaderboards

### Phase 4C Testing:
- [ ] Page view logged when fragment opens
- [ ] Quiz attempt logged when quiz completed
- [ ] Error logged when exception occurs
- [ ] Local analytics database stores events
- [ ] Events can be queried from local database
- [ ] Batch export of events works

---

## Estimated Timeline

| Phase | What | Time | Status |
|-------|------|------|--------|
| 4A | FCM Setup | 2h | ✅ Done |
| 4B | Presence/Friends/Leaderboards | 4-5h | 🟡 1h done, 3-4h UI remaining |
| 4C | Analytics & Event Logging | 3-4h | 🚧 Ready to build |
| 4D | Offline Queue | 2-3h | 🔜 Next after 4C |
| 4E | Achievements | 2-3h | 🔜 Final phase |
| **Total** | **Complete Phase 4** | **13-18h** | **~2h done** |

---

## Quick Commands

### Build and Test:
```bash
# Clean and rebuild
./gradlew.bat clean build

# Run tests
./gradlew.bat test

# Run on emulator
./gradlew.bat installDebug
adb shell am start -n com.example.capstone/.MainActivity
```

### Firebase Console:
1. Go to https://console.firebase.google.com/
2. Select "pralaytrata" project
3. Firestore > Create Database
4. Set security rules (from FIREBASE_CONFIG.md)
5. Create collections as needed
6. Test with test data

---

## Success Criteria for Phase 4B+4C

### Phase 4B Complete When:
✅ Friends can add each other  
✅ Online status shows in real-time  
✅ Global leaderboard displays  
✅ User's rank visible on all boards  
✅ Leaderboards update when points change  

### Phase 4C Complete When:
✅ Events logged to local database  
✅ Analytics can be queried  
✅ Crash reports captured  
✅ Performance metrics tracked  
✅ Analytics dashboard shows data  

---

**Next Step:** Start with Phase 4B UI (1.5-2 hours of work), then move to Phase 4C analytics implementation.

