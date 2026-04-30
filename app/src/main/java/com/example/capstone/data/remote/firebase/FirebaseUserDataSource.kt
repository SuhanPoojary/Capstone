package com.example.capstone.data.remote.firebase

import com.example.capstone.data.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Handles cloud storage and sync of user profile data to Firestore.
 * Designed to work alongside local SharedPreferences in an offline-first model.
 */
class FirebaseUserDataSource {
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    /**
     * Save user profile to Firestore.
     * Returns true if successful, false otherwise.
     */
    suspend fun saveUserProfile(userId: String, profile: UserProfile): Boolean = try {
        val data = mapOf(
            "name" to profile.name,
            "email" to profile.email,
            "institution" to profile.institution,
            "city" to profile.city,
            "state" to profile.state,
            "updatedAt" to System.currentTimeMillis(),
        )
        usersCollection.document(userId).set(data, SetOptions.merge()).await()
        true
    } catch (e: Exception) {
        false
    }

    /**
     * Fetch user profile from Firestore.
     * Returns a UserProfile if found, or null if the document does not exist or fetch fails.
     */
    suspend fun getUserProfile(userId: String): UserProfile? = try {
        val doc = usersCollection.document(userId).get().await()
        if (doc.exists()) {
            UserProfile(
                name = doc.getString("name") ?: "User",
                email = doc.getString("email") ?: "",
                institution = doc.getString("institution") ?: "",
                city = doc.getString("city"),
                state = doc.getString("state"),
            )
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }

    /**
     * Save just the region (city and state) to Firestore.
     * Returns true if successful, false otherwise.
     */
    suspend fun saveRegion(userId: String, city: String?, state: String?): Boolean = try {
        val data = mapOf(
            "city" to city,
            "state" to state,
            "updatedAt" to System.currentTimeMillis(),
        )
        usersCollection.document(userId).set(data, SetOptions.merge()).await()
        true
    } catch (e: Exception) {
        false
    }

    /**
     * Delete the user profile document from Firestore.
     * Returns true if successful, false otherwise.
     */
    suspend fun deleteUserProfile(userId: String): Boolean = try {
        usersCollection.document(userId).delete().await()
        true
    } catch (e: Exception) {
        false
    }
}

