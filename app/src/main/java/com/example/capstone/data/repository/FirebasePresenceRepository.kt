package com.example.capstone.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.capstone.data.UserPresence
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

/**
 * Firebase implementation of PresenceRepository.
 * Manages user online status and activity tracking using Firestore real-time listeners.
 * 
 * Phase 4B: Real-Time Collaboration
 */
class FirebasePresenceRepository(
    private val firestore: FirebaseFirestore
) : PresenceRepository {

    private val TAG = "FirebasePresenceRepo"
    private val presenceListeners = mutableMapOf<String, ListenerRegistration>()
    private val presenceCache = mutableMapOf<String, MutableLiveData<UserPresence>>()

    /**
     * Publish the current user's presence to Firestore.
     * Called when user comes online (app start, login).
     */
    override suspend fun publishPresence(userId: String, activity: String?) {
        try {
            val presence = mapOf(
                "userId" to userId,
                "isOnline" to true,
                "lastSeen" to System.currentTimeMillis(),
                "currentActivity" to activity,
                "timestamp" to System.currentTimeMillis()
            )
            
            firestore.collection("users")
                .document(userId)
                .collection("presence")
                .document("current")
                .set(presence)
                .await()
            
            Log.d(TAG, "Published presence for user $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to publish presence", e)
            throw e
        }
    }

    /**
     * Update the user's current activity.
     */
    override suspend fun updateActivity(userId: String, activity: String) {
        try {
            firestore.collection("users")
                .document(userId)
                .collection("presence")
                .document("current")
                .update(mapOf("currentActivity" to activity, "lastSeen" to System.currentTimeMillis()))
                .await()
            
            Log.d(TAG, "Updated activity for user $userId: $activity")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update activity", e)
            throw e
        }
    }

    /**
     * Listen to a friend's presence status in real-time.
     * Returns a LiveData that updates whenever presence changes.
     */
    override fun observeFriendPresence(userId: String): LiveData<UserPresence> {
        // Return cached LiveData if already listening
        if (presenceCache.containsKey(userId)) {
            return presenceCache[userId]!!
        }
        
        val liveData = MutableLiveData<UserPresence>()
        presenceCache[userId] = liveData
        
        val listener = firestore.collection("users")
            .document(userId)
            .collection("presence")
            .document("current")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to presence", error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null && snapshot.exists()) {
                    try {
                        val presence = snapshot.toObject(UserPresence::class.java)
                        if (presence != null) {
                            liveData.postValue(presence)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse presence", e)
                    }
                } else {
                    // User is offline
                    liveData.postValue(UserPresence(
                        userId = userId,
                        isOnline = false,
                        lastSeen = System.currentTimeMillis()
                    ))
                }
            }
        
        presenceListeners[userId] = listener
        return liveData
    }

    /**
     * Listen to multiple friends' presence in real-time.
     */
    override fun observeMultipleFriendsPresence(userIds: List<String>): LiveData<List<UserPresence>> {
        val liveData = MutableLiveData<List<UserPresence>>()
        val presenceMap = mutableMapOf<String, UserPresence>()
        
        userIds.forEach { userId ->
            val listener = firestore.collection("users")
                .document(userId)
                .collection("presence")
                .document("current")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error listening to presence for $userId", error)
                        return@addSnapshotListener
                    }
                    
                    if (snapshot != null && snapshot.exists()) {
                        try {
                            val presence = snapshot.toObject(UserPresence::class.java)
                            if (presence != null) {
                                presenceMap[userId] = presence
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to parse presence for $userId", e)
                        }
                    } else {
                        presenceMap[userId] = UserPresence(
                            userId = userId,
                            isOnline = false,
                            lastSeen = System.currentTimeMillis()
                        )
                    }
                    
                    // Update LiveData with complete list
                    liveData.postValue(presenceMap.values.toList())
                }
            
            presenceListeners[userId] = listener
        }
        
        return liveData
    }

    /**
     * Mark user as offline (called on logout or app close).
     */
    override suspend fun markUserOffline(userId: String) {
        try {
            firestore.collection("users")
                .document(userId)
                .collection("presence")
                .document("current")
                .update(mapOf("isOnline" to false, "lastSeen" to System.currentTimeMillis()))
                .await()
            
            // Clean up listeners
            presenceListeners[userId]?.remove()
            presenceListeners.remove(userId)
            presenceCache.remove(userId)
            
            Log.d(TAG, "Marked user $userId as offline")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to mark user offline", e)
            throw e
        }
    }

    /**
     * Clean up all listeners (call on app close or logout).
     */
    fun cleanup() {
        presenceListeners.values.forEach { it.remove() }
        presenceListeners.clear()
        presenceCache.clear()
        Log.d(TAG, "Cleaned up all presence listeners")
    }
}

