package com.example.capstone.data.repository

import androidx.lifecycle.LiveData
import com.example.capstone.data.*

/**
 * Repository for managing user presence and online status.
 * 
 * Phase 4B: Real-Time Collaboration
 */
interface PresenceRepository {
    /**
     * Publish current user's presence status to the database.
     * Should be called when user comes online.
     */
    suspend fun publishPresence(userId: String, activity: String? = null)
    
    /**
     * Update user's current activity (e.g., "learning_earthquake").
     */
    suspend fun updateActivity(userId: String, activity: String)
    
    /**
     * Listen to a friend's presence status in real-time.
     */
    fun observeFriendPresence(userId: String): LiveData<UserPresence>
    
    /**
     * Listen to multiple friends' presence in real-time.
     */
    fun observeMultipleFriendsPresence(userIds: List<String>): LiveData<List<UserPresence>>
    
    /**
     * Mark user as offline (e.g., on logout or app close).
     */
    suspend fun markUserOffline(userId: String)
}

/**
 * Repository for managing user friendships.
 * 
 * Phase 4B: Real-Time Collaboration
 */
interface FriendshipRepository {
    /**
     * Send a friend request to another user.
     */
    suspend fun sendFriendRequest(fromUserId: String, toUserId: String): Result<Unit>
    
    /**
     * Accept a friend request.
     */
    suspend fun acceptFriendRequest(fromUserId: String, toUserId: String): Result<Unit>
    
    /**
     * Reject or cancel a friend request.
     */
    suspend fun rejectFriendRequest(fromUserId: String, toUserId: String): Result<Unit>
    
    /**
     * Remove a friend.
     */
    suspend fun removeFriend(userId: String, friendId: String): Result<Unit>
    
    /**
     * Block a user (prevents them from sending requests or seeing your profile).
     */
    suspend fun blockUser(userId: String, blockedUserId: String): Result<Unit>
    
    /**
     * Get all friends of a user.
     */
    fun getFriendsLiveData(userId: String): LiveData<List<Friendship>>
    
    /**
     * Get pending friend requests.
     */
    fun getPendingFriendRequests(userId: String): LiveData<List<Friendship>>
    
    /**
     * Listen to friend list changes in real-time.
     */
    fun observeFriends(userId: String): LiveData<List<Friendship>>
}

/**
 * Repository for managing leaderboards.
 * 
 * Phase 4B: Real-Time Collaboration
 */
interface LeaderboardRepository {
    /**
     * Get global leaderboard for all users.
     */
    suspend fun getGlobalLeaderboard(
        type: LeaderboardType = LeaderboardType.GLOBAL_ALL_TIME,
        limit: Int = 50
    ): Result<List<LeaderboardEntry>>
    
    /**
     * Get regional leaderboard for a specific region.
     */
    suspend fun getRegionalLeaderboard(
        region: String,
        limit: Int = 50
    ): Result<List<LeaderboardEntry>>
    
    /**
     * Get friends leaderboard (among your friends only).
     */
    suspend fun getFriendsLeaderboard(
        userId: String,
        limit: Int = 50
    ): Result<List<LeaderboardEntry>>
    
    /**
     * Get leaderboard for a specific disaster topic.
     */
    suspend fun getDisasterLeaderboard(
        disasterKey: String,
        limit: Int = 50
    ): Result<List<LeaderboardEntry>>
    
    /**
     * Listen to leaderboard changes in real-time.
     */
    fun observeLeaderboard(type: LeaderboardType): LiveData<List<LeaderboardEntry>>
    
    /**
     * Get user's current rank on various leaderboards.
     */
    suspend fun getUserRanks(userId: String): Result<Map<LeaderboardType, Int>>
}

/**
 * Repository for viewing and comparing friend progress.
 * 
 * Phase 4B: Real-Time Collaboration
 */
interface FriendProgressRepository {
    /**
     * Compare your progress with a friend's progress.
     */
    suspend fun compareFriendProgress(
        userId: String,
        friendId: String
    ): Result<FriendProgressComparison>
    
    /**
     * Get a friend's progress summary.
     */
    suspend fun getFriendProgress(friendId: String): Result<UserProgress>
    
    /**
     * Listen to a friend's progress updates in real-time.
     */
    fun observeFriendProgress(friendId: String): LiveData<UserProgress>
}

/**
 * Repository for managing achievements and badges.
 * 
 * Phase 4B: Real-Time Collaboration
 */
interface AchievementRepository {
    /**
     * Get all available achievements.
     */
    suspend fun getAllAchievements(): Result<List<Achievement>>
    
    /**
     * Get achievements earned by a user.
     */
    suspend fun getUserAchievements(userId: String): Result<List<Achievement>>
    
    /**
     * Check if user has earned a specific achievement.
     */
    suspend fun hasAchievement(userId: String, achievementId: String): Boolean
    
    /**
     * Listen to user's achievements in real-time (for new unlocks).
     */
    fun observeUserAchievements(userId: String): LiveData<List<Achievement>>
    
    /**
     * Get achievement progress toward earning a specific achievement.
     */
    suspend fun getAchievementProgress(
        userId: String,
        achievementId: String
    ): Result<Int>  // Returns progress percentage 0-100
}

/**
 * Repository for managing real-time notifications.
 * 
 * Phase 4B: Real-Time Collaboration
 */
interface RealtimeNotificationRepository {
    /**
     * Get unread notifications for the user.
     */
    fun getUnreadNotifications(userId: String): LiveData<List<RealtimeNotification>>
    
    /**
     * Mark a notification as read.
     */
    suspend fun markAsRead(notificationId: String): Result<Unit>
    
    /**
     * Delete a notification.
     */
    suspend fun deleteNotification(notificationId: String): Result<Unit>
    
    /**
     * Send a notification to specific users.
     * Usually called from admin/backend, but included for completeness.
     */
    suspend fun sendNotification(notification: RealtimeNotification): Result<Unit>
}

/**
 * Repository for managing challenges between friends.
 * 
 * Phase 4B: Real-Time Collaboration
 */
interface ChallengeRepository {
    /**
     * Create a new challenge.
     */
    suspend fun createChallenge(challenge: Challenge): Result<String>
    
    /**
     * Join an existing challenge.
     */
    suspend fun joinChallenge(challengeId: String, userId: String): Result<Unit>
    
    /**
     * Update progress on a challenge.
     */
    suspend fun updateChallengeProgress(
        challengeId: String,
        userId: String,
        progress: Int
    ): Result<Unit>
    
    /**
     * Get active challenges for a user.
     */
    suspend fun getUserChallenges(userId: String): Result<List<Challenge>>
    
    /**
     * Listen to challenge results in real-time.
     */
    fun observeChallenge(challengeId: String): LiveData<Challenge>
}

/**
 * Repository for managing activity feeds (shared activities).
 * 
 * Phase 4B: Real-Time Collaboration
 */
interface ActivityFeedRepository {
    /**
     * Get activity feed for current user's friends.
     */
    fun getFriendsActivityFeed(userId: String): LiveData<List<SharedActivity>>
    
    /**
     * Post a shared activity.
     */
    suspend fun postActivity(activity: SharedActivity): Result<Unit>
    
    /**
     * Like a shared activity.
     */
    suspend fun likeActivity(activityId: String, userId: String): Result<Unit>
    
    /**
     * Unlike a shared activity.
     */
    suspend fun unlikeActivity(activityId: String, userId: String): Result<Unit>
    
    /**
     * Add a comment to a shared activity.
     */
    suspend fun commentOnActivity(
        activityId: String,
        userId: String,
        comment: String
    ): Result<Unit>
}

// Helper data class for user progress
data class UserProgress(
    val userId: String,
    val userName: String,
    val level: Int,
    val points: Int,
    val lessonsCompleted: Int,
    val totalLessons: Int,
    val averageQuizScore: Float,
    val streak: Int,
    val region: String?
)

