package com.example.capstone.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.capstone.data.GlobalLeaderboardEntry
import com.example.capstone.data.LeaderboardStats
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

/**
 * Firebase implementation of GlobalLeaderboardRepository.
 * Manages global user rankings using Firestore real-time queries.
 * 
 * Phase 4B: Real-Time Collaboration
 * 
 * Firestore structure:
 * leaderboards/global/allTime/{rank} → {userId, userName, points, level, lastActiveAt, ...}
 */
class FirebaseGlobalLeaderboardRepository(
    private val firestore: FirebaseFirestore
) : GlobalLeaderboardRepository {

    private val TAG = "FirebaseLeaderboardRepo"
    private var leaderboardListener: ListenerRegistration? = null

    /**
     * Get global leaderboard for all users.
     */
    override suspend fun getGlobalLeaderboard(
        limit: Int,
        offset: Int
    ): Result<List<GlobalLeaderboardEntry>> {
        return try {
            val snapshot = firestore.collection("leaderboards")
                .document("global")
                .collection("allTime")
                .orderBy("rank", Query.Direction.ASCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            
            val entries = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(GlobalLeaderboardEntry::class.java)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse leaderboard entry", e)
                    null
                }
            }
            
            Log.d(TAG, "Fetched ${entries.size} global leaderboard entries")
            Result.success(entries)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch global leaderboard", e)
            Result.failure(e)
        }
    }

    /**
     * Listen to global leaderboard changes in real-time.
     */
    override fun observeGlobalLeaderboard(limit: Int): LiveData<List<GlobalLeaderboardEntry>> {
        val liveData = MutableLiveData<List<GlobalLeaderboardEntry>>()
        
        // Remove previous listener if any
        leaderboardListener?.remove()
        
        leaderboardListener = firestore.collection("leaderboards")
            .document("global")
            .collection("allTime")
            .orderBy("rank", Query.Direction.ASCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to global leaderboard", error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    try {
                        val entries = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(GlobalLeaderboardEntry::class.java)
                        }
                        liveData.postValue(entries)
                        Log.d(TAG, "Global leaderboard updated: ${entries.size} entries")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse leaderboard entries", e)
                    }
                }
            }
        
        return liveData
    }

    /**
     * Get user's rank on global leaderboard.
     */
    override suspend fun getUserGlobalRank(userId: String): Result<Int> {
        return try {
            // Query to find the user's rank
            val snapshot = firestore.collection("leaderboards")
                .document("global")
                .collection("allTime")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            val rank = if (snapshot.documents.isNotEmpty()) {
                snapshot.documents[0].getLong("rank")?.toInt() ?: -1
            } else {
                -1  // User not on leaderboard
            }
            
            Log.d(TAG, "User $userId has global rank: $rank")
            Result.success(rank)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch user global rank", e)
            Result.failure(e)
        }
    }

    /**
     * Get users near a specific rank.
     */
    override suspend fun getUsersNearRank(rank: Int, range: Int): Result<List<GlobalLeaderboardEntry>> {
        return try {
            val startRank = (rank - range).coerceAtLeast(1)
            val endRank = rank + range
            
            val snapshot = firestore.collection("leaderboards")
                .document("global")
                .collection("allTime")
                .whereGreaterThanOrEqualTo("rank", startRank.toLong())
                .whereLessThanOrEqualTo("rank", endRank.toLong())
                .get()
                .await()
            
            val entries = snapshot.documents.mapNotNull { doc ->
                doc.toObject(GlobalLeaderboardEntry::class.java)
            }
            
            Log.d(TAG, "Fetched ${entries.size} users near rank $rank")
            Result.success(entries)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch users near rank", e)
            Result.failure(e)
        }
    }

    /**
     * Get stats about the global leaderboard.
     */
    override suspend fun getLeaderboardStats(): Result<LeaderboardStats> {
        return try {
            // Get stats document
            val snapshot = firestore.collection("leaderboards")
                .document("stats")
                .get()
                .await()
            
            val stats = snapshot.toObject(LeaderboardStats::class.java)
            
            Result.success(
                stats ?: LeaderboardStats(
                    leaderboardType = "global",
                    totalUsers = 0,
                    averagePoints = 0,
                    medianPoints = 0,
                    topPoints = 0,
                    bottomPoints = 0,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch leaderboard stats", e)
            Result.failure(e)
        }
    }

    /**
     * Clean up listeners.
     */
    fun cleanup() {
        leaderboardListener?.remove()
        Log.d(TAG, "Cleaned up leaderboard listener")
    }
}

/**
 * Firebase implementation of RegionalLeaderboardRepository.
 */
class FirebaseRegionalLeaderboardRepository(
    private val firestore: FirebaseFirestore
) : RegionalLeaderboardRepository {

    private val TAG = "FirebaseRegionalLeaderboardRepo"
    private val leaderboardListeners = mutableMapOf<String, ListenerRegistration>()

    override suspend fun getRegionalLeaderboard(
        region: String,
        limit: Int
    ): Result<List<com.example.capstone.data.RegionalLeaderboardEntry>> {
        return try {
            val snapshot = firestore.collection("leaderboards")
                .document("regional")
                .collection(region)
                .orderBy("rank", Query.Direction.ASCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            
            val entries = snapshot.documents.mapNotNull { doc ->
                doc.toObject(com.example.capstone.data.RegionalLeaderboardEntry::class.java)
            }
            
            Result.success(entries)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch regional leaderboard for $region", e)
            Result.failure(e)
        }
    }

    override fun observeRegionalLeaderboard(region: String): LiveData<List<com.example.capstone.data.RegionalLeaderboardEntry>> {
        val liveData = MutableLiveData<List<com.example.capstone.data.RegionalLeaderboardEntry>>()
        
        val listener = firestore.collection("leaderboards")
            .document("regional")
            .collection(region)
            .orderBy("rank", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to regional leaderboard", error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val entries = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(com.example.capstone.data.RegionalLeaderboardEntry::class.java)
                    }
                    liveData.postValue(entries)
                }
            }
        
        leaderboardListeners[region] = listener
        return liveData
    }

    override suspend fun getUserRegionalRank(userId: String): Result<Int> {
        return try {
            // This would need to query across all regions or know the user's region
            // For now, return -1
            Result.success(-1)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllRegionalStats(): Result<Map<String, LeaderboardStats>> {
        return try {
            Result.success(emptyMap())  // Implementation deferred
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun cleanup() {
        leaderboardListeners.values.forEach { it.remove() }
        leaderboardListeners.clear()
    }
}

/**
 * Firebase implementation of FriendLeaderboardRepository.
 */
class FirebaseFriendLeaderboardRepository(
    private val firestore: FirebaseFirestore
) : FriendLeaderboardRepository {

    private val TAG = "FirebaseFriendLeaderboardRepo"

    override suspend fun getFriendsLeaderboard(
        userId: String,
        limit: Int
    ): Result<List<com.example.capstone.data.FriendLeaderboardEntry>> {
        return try {
            val snapshot = firestore.collection("leaderboards")
                .document("friends")
                .collection(userId)
                .orderBy("rank", Query.Direction.ASCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            
            val entries = snapshot.documents.mapNotNull { doc ->
                doc.toObject(com.example.capstone.data.FriendLeaderboardEntry::class.java)
            }
            
            Result.success(entries)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch friends leaderboard", e)
            Result.failure(e)
        }
    }

    override fun observeFriendsLeaderboard(userId: String): LiveData<List<com.example.capstone.data.FriendLeaderboardEntry>> {
        val liveData = MutableLiveData<List<com.example.capstone.data.FriendLeaderboardEntry>>()
        
        firestore.collection("leaderboards")
            .document("friends")
            .collection(userId)
            .orderBy("rank", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to friends leaderboard", error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val entries = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(com.example.capstone.data.FriendLeaderboardEntry::class.java)
                    }
                    liveData.postValue(entries)
                }
            }
        
        return liveData
    }

    override suspend fun getUserFriendRank(userId: String): Result<Int> {
        return try {
            Result.success(-1)  // Implementation deferred
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

