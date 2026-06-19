package com.example.capstone.data.remote.firebase

import com.example.capstone.data.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirebaseUserDataSource {
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    suspend fun saveUserProfile(profile: UserProfile): Boolean = try {
        usersCollection.document(profile.uid).set(profile, SetOptions.merge()).await()
        true
    } catch (e: Exception) {
        false
    }

    suspend fun getUserProfile(userId: String): UserProfile? = try {
        val doc = usersCollection.document(userId).get().await()
        doc.toObject(UserProfile::class.java)
    } catch (e: Exception) {
        null
    }

    suspend fun updateLastLogin(userId: String): Boolean = try {
        usersCollection.document(userId).update("lastLogin", System.currentTimeMillis()).await()
        true
    } catch (e: Exception) {
        false
    }
}
