package com.example.capstone.data

/**
 * Phase 4B: Real-Time Collaboration Models
 * 
 * These models support presence tracking, friend systems, and leaderboards.
 */

/**
 * Represents a user's online status and presence state.
 */
data class UserPresence(
    val userId: String,
    val isOnline: Boolean,
    val lastSeen: Long = System.currentTimeMillis(),
    val currentActivity: String? = null  // e.g., "learning", "viewing_profile", "chatting"
)

/**
 * Represents a friendship relationship between two users.
 */
data class Friendship(
    val userId: String,
    val friendId: String,
    val status: FriendshipStatus = FriendshipStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val friendName: String? = null,
    val friendLevel: Int = 0
)

enum class FriendshipStatus {
    PENDING,    // Request sent, awaiting acceptance
    ACCEPTED,   // Both users are friends
    BLOCKED     // One user blocked the other
}

/**
 * Represents a user's position in the leaderboard.
 */
data class LeaderboardEntry(
    val rank: Int,
    val userId: String,
    val userName: String,
    val points: Int,
    val level: Int,
    val lessonsCompleted: Int,
    val region: String? = null,
    val badge: String? = null,  // Special badge if applicable
    val isFriend: Boolean = false
)

/**
 * Represents a leaderboard type and time period.
 */
enum class LeaderboardType {
    GLOBAL_ALL_TIME,
    GLOBAL_THIS_MONTH,
    GLOBAL_THIS_WEEK,
    REGIONAL_ALL_TIME,
    FRIENDS_ALL_TIME,
    DISASTER_SPECIFIC  // Top scorers in a specific disaster topic
}

/**
 * Represents progress comparison between the user and a friend.
 */
data class FriendProgressComparison(
    val friendId: String,
    val friendName: String,
    val friendLevel: Int,
    val friendPoints: Int,
    val userLevel: Int,
    val userPoints: Int,
    val lessonsDifference: Int,  // Positive if user is ahead, negative if friend is ahead
    val commonCompletedLessons: Int,
    val friendOnlyLessons: Int,
    val userOnlyLessons: Int
)

/**
 * Represents an achievement/badge earned by a user.
 */
data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,  // Resource name or URL
    val earnedAt: Long? = null,  // null if not earned
    val category: AchievementCategory = AchievementCategory.LEARNING
)

enum class AchievementCategory {
    LEARNING,      // Based on lessons and quizzes
    ENGAGEMENT,    // Based on consistency and streaks
    SOCIAL,        // Based on friend interactions
    MILESTONES     // Major accomplishments
}

/**
 * Represents a notification that updates in real-time.
 */
data class RealtimeNotification(
    val id: String,
    val userId: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val relatedUserId: String? = null,  // For friend requests, achievement shares, etc.
    val actionUrl: String? = null,      // Deep link to open when tapped
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

enum class NotificationType {
    FRIEND_REQUEST,
    FRIEND_ACCEPTED,
    ACHIEVEMENT_UNLOCKED,
    CHALLENGE_INVITATION,
    FRIEND_MILESTONE,  // When a friend completes a lesson
    LEADERBOARD_CHANGE,  // When user's rank changes
    DISASTER_ALERT
}

/**
 * Represents a challenge between friends or groups.
 */
data class Challenge(
    val id: String,
    val creatorId: String,
    val participantIds: List<String>,
    val disasterTopic: String,
    val goal: Int,  // e.g., complete 5 lessons, score 80% on quizzes
    val goalType: ChallengeGoalType,
    val startTime: Long,
    val endTime: Long,
    val results: Map<String, Int> = emptyMap()  // userId -> score/progress
)

enum class ChallengeGoalType {
    LESSONS_COMPLETED,
    QUIZ_SCORE_AVERAGE,
    TIME_SPENT_LEARNING,
    ACHIEVEMENTS_EARNED
}

/**
 * Represents shared user activity that appears in feeds.
 */
data class SharedActivity(
    val id: String,
    val userId: String,
    val userName: String,
    val userLevel: Int,
    val activityType: SharedActivityType,
    val content: String,  // e.g., lesson name, achievement name
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val isLikedByCurrentUser: Boolean = false,
    val commentCount: Int = 0
)

enum class SharedActivityType {
    LESSON_COMPLETED,
    QUIZ_PASSED,
    ACHIEVEMENT_EARNED,
    LEVEL_UP,
    STREAK_REACHED
}

