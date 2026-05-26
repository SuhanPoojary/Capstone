package com.example.capstone.data

/**
 * Phase 4E: Achievements, Leaderboards, and Social Features Models
 * 
 * These models support badges, achievements, leaderboards, and social interactions.
 */

/**
 * Represents a single achievement/badge that users can earn.
 */
data class AchievementDefinition(
    val id: String,
    val name: String,
    val description: String,
    val iconUrl: String,
    val category: String,  // e.g., "learning", "engagement", "social"
    val rarity: AchievementRarity = AchievementRarity.COMMON,
    val points: Int = 10,
    val unlockCondition: UnlockCondition,
    val hidden: Boolean = false  // Hidden achievements are not shown until earned
)

enum class AchievementRarity {
    COMMON,
    UNCOMMON,
    RARE,
    EPIC,
    LEGENDARY
}

/**
 * Represents the condition needed to unlock an achievement.
 */
sealed class UnlockCondition {
    data class LessonsCompleted(val count: Int) : UnlockCondition()
    data class QuizzesCompleted(val count: Int) : UnlockCondition()
    data class QuizzesWithScore(val minScore: Int, val count: Int) : UnlockCondition()
    data class StreakDays(val days: Int) : UnlockCondition()
    data class TotalPoints(val points: Int) : UnlockCondition()
    data class DisasterLessons(val disaster: String, val count: Int) : UnlockCondition()
    data class FriendsAdded(val count: Int) : UnlockCondition()
    data class ChallengesWon(val count: Int) : UnlockCondition()
    data class TimeOnApp(val minutes: Int) : UnlockCondition()
    data class FirstAction(val action: String) : UnlockCondition()  // Special achievements
}

/**
 * Represents an achievement earned by a user.
 */
data class EarnedAchievement(
    val userId: String,
    val achievementId: String,
    val unlockedAt: Long,
    val pointsAwarded: Int,
    val notificationSent: Boolean = false,
    val sharedAt: Long? = null  // When user shared this achievement
)

/**
 * Represents the achievements state for a user.
 */
data class UserAchievementState(
    val userId: String,
    val totalAchievements: Int,
    val unlockedAchievements: Int,
    val nextAchievement: String? = null,  // Achievement closest to being earned
    val progressTowardNext: Int = 0,  // Percentage 0-100
    val recentlyUnlocked: List<EarnedAchievement> = emptyList()
)

/**
 * Represents a global leaderboard entry.
 */
data class GlobalLeaderboardEntry(
    val rank: Int,
    val userId: String,
    val userName: String,
    val totalPoints: Int,
    val level: Int,
    val lessonsCompleted: Int,
    val region: String? = null,
    val topAchievement: String? = null,
    val lastActiveAt: Long,
    val trend: RankTrend = RankTrend.STABLE
)

enum class RankTrend {
    RISING,      // Rank improved
    STABLE,      // Rank unchanged
    DECLINING    // Rank worsened
}

/**
 * Represents a regional leaderboard entry.
 */
data class RegionalLeaderboardEntry(
    val rank: Int,
    val userId: String,
    val userName: String,
    val region: String,
    val points: Int,
    val level: Int,
    val isFriend: Boolean = false
)

/**
 * Represents a friends-only leaderboard entry.
 */
data class FriendLeaderboardEntry(
    val rank: Int,
    val userId: String,
    val userName: String,
    val points: Int,
    val level: Int,
    val friendshipStatus: String,  // "best_friend", "friend", etc.
    val pointsDifference: Int  // Positive if user is ahead
)

/**
 * Represents a disaster-specific leaderboard (top scorers in earthquake content, etc).
 */
data class DisasterLeaderboardEntry(
    val rank: Int,
    val userId: String,
    val userName: String,
    val disaster: String,
    val lessonsCompleted: Int,
    val averageQuizScore: Float,
    val totalTimeSpent: Int
)

/**
 * Represents milestone achievements (level up, streak reached, etc).
 */
data class MilestoneAchievement(
    val id: String,
    val userId: String,
    val type: MilestoneType,
    val level: Int? = null,  // For level-ups
    val streak: Int? = null,  // For streaks
    val unlockedAt: Long
)

enum class MilestoneType {
    LEVEL_UP,
    STREAK_REACHED,
    TOTAL_TIME_MILESTONE,
    LESSONS_COMPLETED_MILESTONE,
    POINTS_MILESTONE
}

/**
 * Represents achievement progress toward multiple achievements.
 */
data class AchievementProgress(
    val userId: String,
    val lessonsCompleted: Int,
    val quizzesCompleted: Int,
    val quizzesWithScore: Map<Int, Int> = emptyMap(),  // minScore -> count
    val streakDays: Int,
    val totalPoints: Int,
    val timeOnAppMinutes: Int,
    val friendsAdded: Int,
    val challengesWon: Int
)

/**
 * Represents a notification when someone earns an achievement.
 */
data class AchievementNotification(
    val notificationId: String,
    val userId: String,
    val achievement: AchievementDefinition,
    val earnedBy: String,  // userId of who earned it
    val message: String,
    val timestamp: Long,
    val isRead: Boolean = false
)

/**
 * Represents leaderboard statistics and trends.
 */
data class LeaderboardStats(
    val leaderboardType: String,  // "global", "regional", "friends"
    val totalUsers: Int,
    val averagePoints: Int,
    val medianPoints: Int,
    val topPoints: Int,
    val bottomPoints: Int,
    val lastUpdated: Long,
    val pointsDistribution: Map<Int, Int> = emptyMap()  // pointsBucket -> userCount
)

/**
 * Represents a personal achievement/stats card.
 */
data class PersonalStatsCard(
    val userId: String,
    val userName: String,
    val currentLevel: Int,
    val totalPoints: Int,
    val lessonsCompleted: Int,
    val currentStreak: Int,
    val bestStreak: Int,
    val globalRank: Int? = null,
    val regionalRank: Int? = null,
    val friendsRank: Int? = null,
    val achievements: Int,
    val nextMilestone: String? = null
)

/**
 * Represents a badge collection (visual display of achievements).
 */
data class BadgeCollection(
    val userId: String,
    val badges: List<EarnedAchievement>,
    val favorites: List<String> = emptyList(),  // Favorite badge IDs to display
    val collectionSize: Int,
    val displayLayout: BadgeDisplayLayout = BadgeDisplayLayout.GRID_3X3
)

enum class BadgeDisplayLayout {
    GRID_3X3,
    GRID_4X4,
    GRID_5X5,
    LINEAR_HORIZONTAL,
    LINEAR_VERTICAL
}

/**
 * Represents a social achievement (shared by user on their profile/feed).
 */
data class SharedAchievement(
    val achievementId: String,
    val userId: String,
    val sharedAt: Long,
    val caption: String? = null,
    val likes: Int = 0,
    val shares: Int = 0,
    val comments: Int = 0
)

