package com.example.capstone.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.capstone.data.Friendship
import com.example.capstone.data.FriendshipStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

/**
 * Firebase implementation of FriendshipRepository.
 * Manages friend relationships, requests, and blocks using Firestore.
 * 
 * Phase 4B: Real-Time Collaboration
 * 
 * Firestore structure:
 * friendships/{userId}/contacts/{friendId} → {status, createdAt, friendName, friendLevel}
 */
class FirebaseFriendshipRepository(
    private val firestore: FirebaseFirestore,
    private val currentUserId: String
) : FriendshipRepository {

    private val TAG = "FirebaseFriendshipRepo"
    private var friendsListener: ListenerRegistration? = null
    private var requestsListener: ListenerRegistration? = null

    /**
     * Send a friend request to another user.
     */
    override suspend fun sendFriendRequest(fromUserId: String, toUserId: String): Result<Unit> {
        return try {
            val friendship = mapOf(
                "userId" to fromUserId,
                "friendId" to toUserId,
                "status" to FriendshipStatus.PENDING.name,
                "createdAt" to System.currentTimeMillis()
            )
            
            // Store in sender's friendships
            firestore.collection("friendships")
                .document(fromUserId)
                .collection("contacts")
                .document(toUserId)
                .set(friendship)
                .await()
            
            Log.d(TAG, "Friend request sent from $fromUserId to $toUserId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send friend request", e)
            Result.failure(e)
        }
    }

    /**
     * Accept a friend request.
     */
    override suspend fun acceptFriendRequest(fromUserId: String, toUserId: String): Result<Unit> {
        return try {
            val acceptTime = System.currentTimeMillis()
            
            // Update status in both directions
            firestore.collection("friendships")
                .document(toUserId)
                .collection("contacts")
                .document(fromUserId)
                .update(mapOf("status" to FriendshipStatus.ACCEPTED.name, "createdAt" to acceptTime))
                .await()
            
            firestore.collection("friendships")
                .document(fromUserId)
                .collection("contacts")
                .document(toUserId)
                .update(mapOf("status" to FriendshipStatus.ACCEPTED.name, "createdAt" to acceptTime))
                .await()
            
            Log.d(TAG, "Friend request accepted between $fromUserId and $toUserId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to accept friend request", e)
            Result.failure(e)
        }
    }

    /**
     * Reject or cancel a friend request.
     */
    override suspend fun rejectFriendRequest(fromUserId: String, toUserId: String): Result<Unit> {
        return try {
            // Delete from both directions
            firestore.collection("friendships")
                .document(toUserId)
                .collection("contacts")
                .document(fromUserId)
                .delete()
                .await()
            
            firestore.collection("friendships")
                .document(fromUserId)
                .collection("contacts")
                .document(toUserId)
                .delete()
                .await()
            
            Log.d(TAG, "Friend request rejected between $fromUserId and $toUserId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to reject friend request", e)
            Result.failure(e)
        }
    }

    /**
     * Remove a friend.
     */
    override suspend fun removeFriend(userId: String, friendId: String): Result<Unit> {
        return try {
            // Delete from both directions
            firestore.collection("friendships")
                .document(userId)
                .collection("contacts")
                .document(friendId)
                .delete()
                .await()
            
            firestore.collection("friendships")
                .document(friendId)
                .collection("contacts")
                .document(userId)
                .delete()
                .await()
            
            Log.d(TAG, "Friend removed: $userId - $friendId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove friend", e)
            Result.failure(e)
        }
    }

    /**
     * Block a user.
     */
    override suspend fun blockUser(userId: String, blockedUserId: String): Result<Unit> {
        return try {
            val blockRecord = mapOf(
                "userId" to userId,
                "friendId" to blockedUserId,
                "status" to FriendshipStatus.BLOCKED.name,
                "createdAt" to System.currentTimeMillis()
            )
            
            firestore.collection("friendships")
                .document(userId)
                .collection("contacts")
                .document(blockedUserId)
                .set(blockRecord)
                .await()
            
            Log.d(TAG, "User blocked: $userId blocked $blockedUserId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to block user", e)
            Result.failure(e)
        }
    }

    /**
     * Get all friends of a user.
     */
    override fun getFriendsLiveData(userId: String): LiveData<List<Friendship>> {
        val liveData = MutableLiveData<List<Friendship>>()
        
        firestore.collection("friendships")
            .document(userId)
            .collection("contacts")
            .whereEqualTo("status", FriendshipStatus.ACCEPTED.name)
            .get()
            .addOnSuccessListener { snapshot ->
                try {
                    val friendships = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Friendship::class.java)
                    }
                    liveData.postValue(friendships)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse friendships", e)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to fetch friends", e)
            }
        
        return liveData
    }

    /**
     * Get pending friend requests.
     */
    override fun getPendingFriendRequests(userId: String): LiveData<List<Friendship>> {
        val liveData = MutableLiveData<List<Friendship>>()
        
        firestore.collection("friendships")
            .document(userId)
            .collection("contacts")
            .whereEqualTo("status", FriendshipStatus.PENDING.name)
            .get()
            .addOnSuccessListener { snapshot ->
                try {
                    val requests = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Friendship::class.java)
                    }
                    liveData.postValue(requests)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse pending requests", e)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to fetch pending requests", e)
            }
        
        return liveData
    }

    /**
     * Listen to friend list changes in real-time.
     */
    override fun observeFriends(userId: String): LiveData<List<Friendship>> {
        val liveData = MutableLiveData<List<Friendship>>()
        
        // Remove previous listener if any
        friendsListener?.remove()
        
        friendsListener = firestore.collection("friendships")
            .document(userId)
            .collection("contacts")
            .whereEqualTo("status", FriendshipStatus.ACCEPTED.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to friends", error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    try {
                        val friendships = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(Friendship::class.java)
                        }
                        liveData.postValue(friendships)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse friendships", e)
                    }
                }
            }
        
        return liveData
    }

    /**
     * Clean up listeners.
     */
    fun cleanup() {
        friendsListener?.remove()
        requestsListener?.remove()
        Log.d(TAG, "Cleaned up friendship listeners")
    }
}

