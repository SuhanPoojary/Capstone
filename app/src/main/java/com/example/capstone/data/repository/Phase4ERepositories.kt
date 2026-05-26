package com.example.capstone.data.repository

import androidx.lifecycle.LiveData
import com.example.capstone.data.*

/**
 * Repository for managing achievements and badges.
 * 
 * Phase 4E: Achievements and Leaderboards
 */
interface AchievementSystemRepository {
    /**
     * Get all available achievement definitions.
     */
    suspend fun getAllAchievements(): Result<List<AchievementDefinition>>
    
    /**
     * Get user's earned achievements.
     */
    suspend fun getUserAchievements(userId: String): Result<List<EarnedAchievement>>
    
    /**
     * Listen to user's achievements in real-time (for notifications on new unlocks).
     */
    fun observeUserAchievements(userId: String): LiveData<List<EarnedAchievement>>
    
    /**
     * Get achievement state including progress toward next achievement.
     */
    suspend fun getUserAchievementState(userId: String): Result<UserAchievementState>
    
    /**
     * Check progress toward specific achievement.
     */
    suspend fun getAchievementProgress(
        userId: String,
        achievementId: String
    ): Result<Int>  // Returns 0-100
    
    /**
     * Award an achievement to a user.
     * Usually called automatically when conditions are met.
     */
    suspend fun awardAchievement(
        userId: String,
        achievementId: String
    ): Result<Unit>
    
    /**
     * Evaluate all conditions and auto-award any newly earned achievements.
     */
    suspend fun evaluateAndAwardAchievements(userId: String): Result<List<String>>  // Returns awarded achievement IDs
}

/**
 * Repository for managing global leaderboards.
 * 
 * Phase 4E: Achievements and Leaderboards
 */
interface GlobalLeaderboardRepository {
    /**
     * Get global leaderboard (all users).
     */
    suspend fun getGlobalLeaderboard(
        limit: Int = 100,
        offset: Int = 0
    ): Result<List<GlobalLeaderboardEntry>>
    
    /**
     * Listen to global leaderboard changes in real-time.
     */
    fun observeGlobalLeaderboard(limit: Int = 50): LiveData<List<GlobalLeaderboardEntry>>
    
    /**
     * Get user's rank on global leaderboard.
     */
    suspend fun getUserGlobalRank(userId: String): Result<Int>
    
    /**
     * Get users near a specific rank.
     */
    suspend fun getUsersNearRank(rank: Int, range: Int = 10): Result<List<GlobalLeaderboardEntry>>
    
    /**
     * Get stats about the global leaderboard.
     */
    suspend fun getLeaderboardStats(): Result<LeaderboardStats>
}

/**
 * Repository for managing regional leaderboards.
 * 
 * Phase 4E: Achievements and Leaderboards
 */
interface RegionalLeaderboardRepository {
    /**
     * Get regional leaderboard for a specific region.
     */
    suspend fun getRegionalLeaderboard(
        region: String,
        limit: Int = 50
    ): Result<List<RegionalLeaderboardEntry>>
    
    /**
     * Listen to regional leaderboard changes.
     */
    fun observeRegionalLeaderboard(region: String): LiveData<List<RegionalLeaderboardEntry>>
    
    /**
     * Get user's rank in their region.
     */
    suspend fun getUserRegionalRank(userId: String): Result<Int>
    
    /**
     * Get all regions' leaderboard stats.
     */
    suspend fun getAllRegionalStats(): Result<Map<String, LeaderboardStats>>
}

/**
 * Repository for managing friends-only leaderboards.
 * 
 * Phase 4E: Achievements and Leaderboards
 */
interface FriendLeaderboardRepository {
    /**
     * Get leaderboard of user's friends only.
     */
    suspend fun getFriendsLeaderboard(
        userId: String,
        limit: Int = 50
    ): Result<List<FriendLeaderboardEntry>>
    
    /**
     * Listen to friends leaderboard in real-time.
     */
    fun observeFriendsLeaderboard(userId: String): LiveData<List<FriendLeaderboardEntry>>
    
    /**
     * Get user's rank among friends.
     */
    suspend fun getUserFriendRank(userId: String): Result<Int>
}

/**
 * Repository for managing disaster-specific leaderboards.
 * 
 * Phase 4E: Achievements and Leaderboards
 */
interface DisasterLeaderboardRepository {
    /**
     * Get leaderboard for a specific disaster topic.
     */
    suspend fun getDisasterLeaderboard(
        disaster: String,
        limit: Int = 50
    ): Result<List<DisasterLeaderboardEntry>>
    
    /**
     * Listen to disaster leaderboard in real-time.
     */
    fun observeDisasterLeaderboard(disaster: String): LiveData<List<DisasterLeaderboardEntry>>
    
    /**
     * Get user's rank in a specific disaster topic.
     */
    suspend fun getUserDisasterRank(userId: String, disaster: String): Result<Int>
}

/**
 * Repository for managing personal stats and profiles.
 * 
 * Phase 4E: Achievements and Leaderboards
 */
interface PersonalStatsRepository {
    /**
     * Get personal stats card for a user.
     */
    suspend fun getPersonalStats(userId: String): Result<PersonalStatsCard>
    
    /**
     * Listen to personal stats changes.
     */
    fun observePersonalStats(userId: String): LiveData<PersonalStatsCard>
    
    /**
     * Get badge collection for a user.
     */
    suspend fun getBadgeCollection(userId: String): Result<BadgeCollection>
    
    /**
     * Update favorite badges display.
     */
    suspend fun updateFavoriteBadges(userId: String, badgeIds: List<String>): Result<Unit>
    
    /**
     * Share an achievement to social feed.
     */
    suspend fun shareAchievement(
        userId: String,
        achievementId: String,
        caption: String? = null
    ): Result<Unit>
}

/**
 * Repository for milestone-based achievements.
 * 
 * Phase 4E: Achievements and Leaderboards
 */
interface MilestoneRepository {
    /**
     * Get all milestones for a user.
     */
    suspend fun getUserMilestones(userId: String): Result<List<MilestoneAchievement>>
    
    /**
     * Listen to new milestones in real-time.
     */
    fun observeMilestones(userId: String): LiveData<List<MilestoneAchievement>>
    
    /**
     * Check and award milestone achievements.
     */
    suspend fun evaluateMilestones(userId: String): Result<List<MilestoneAchievement>>
}

/**
 * Repository for managing achievement notifications.
 * 
 * Phase 4E: Achievements and Leaderboards
 */
interface AchievementNotificationRepository {
    /**
     * Send notification when someone earns an achievement.
     */
    suspend fun sendAchievementNotification(
        userId: String,
        achievementId: String
    ): Result<Unit>
    
    /**
     * Get achievement-related notifications for a user.
     */
    suspend fun getUserAchievementNotifications(userId: String): Result<List<AchievementNotification>>
    
    /**
     * Listen to achievement notifications in real-time.
     */
    fun observeAchievementNotifications(userId: String): LiveData<List<AchievementNotification>>
}

/**
 * Combined repository for comprehensive achievement and leaderboard features.
 * 
 * Phase 4E: Achievements and Leaderboards
 */
interface AchievementSystemFacade {
    /**
     * Sync all achievement data and leaderboards.
     */
    suspend fun syncAchievementData(userId: String): Result<Unit>
    
    /**
     * Get comprehensive stats dashboard data.
     */
    suspend fun getDashboardData(userId: String): Result<DashboardData>
    
    /**
     * Update user activity (triggers achievement evaluation).
     */
    suspend fun updateUserActivity(userId: String, activity: UserActivityUpdate): Result<Unit>
}

/**
 * Represents a user activity update (triggers achievement checks).
 */
data class UserActivityUpdate(
    val lessonsCompleted: Int = 0,
    val quizzesCompleted: Int = 0,
    val quizScore: Int? = null,
    val timeSpent: Int = 0,
    val friendsAdded: Int = 0,
    val challengesWon: Int = 0,
    val streakDays: Int = 0
)

/**
 * Comprehensive dashboard data for user stats screen.
 */
data class DashboardData(
    val personalStats: PersonalStatsCard,
    val recentAchievements: List<EarnedAchievement>,
    val milestones: List<MilestoneAchievement>,
    val globalRank: Int,
    val regionalRank: Int? = null,
    val friendsRank: Int? = null,
    val nextAchievement: AchievementDefinition? = null
)

