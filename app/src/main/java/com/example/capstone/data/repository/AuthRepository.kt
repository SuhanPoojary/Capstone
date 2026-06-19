package com.example.capstone.data.repository

import android.util.Log
import com.example.capstone.data.remote.firebase.FirebaseAuthDataSource
import com.example.capstone.data.remote.firebase.FirebaseUserDataSource
import com.example.capstone.data.UserProfile
import com.example.capstone.data.UserRepository
import com.google.firebase.auth.FirebaseUser

class AuthRepository(
    private val firebaseAuth: FirebaseAuthDataSource,
    private val firebaseUser: FirebaseUserDataSource,
    private val userRepository: UserRepository,
) {
    suspend fun signUp(
        email: String,
        password: String,
        username: String,
        name: String,
        institution: String
    ): Result<Unit> {
        return try {
            Log.d("AuthRepo", "Starting Firebase Auth signup for $email")
            val user = firebaseAuth.signUp(email, password)
            if (user != null) {
                val profile = UserProfile(
                    uid = user.uid,
                    username = username,
                    name = name,
                    email = email,
                    institution = institution,
                    createdAt = System.currentTimeMillis(),
                    lastLogin = System.currentTimeMillis(),
                    profileCompleted = true
                )
                
                Log.d("AuthRepo", "Auth succeeded, saving profile to Firestore for UID: ${user.uid}")
                val firestoreSuccess = firebaseUser.saveUserProfile(profile)
                
                if (firestoreSuccess) {
                    userRepository.saveUserProfile(profile)
                    Log.d("AuthRepo", "Signup complete: Auth + Firestore success")
                    Result.success(Unit)
                } else {
                    Log.e("AuthRepo", "Firestore profile save failed. Check rules!")
                    Result.failure(Exception("Account created, but failed to save profile to cloud. Please check your internet or Firestore rules."))
                }
            } else {
                Result.failure(Exception("Failed to create user account"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepo", "Signup error", e)
            Result.failure(e)
        }
    }

    suspend fun logIn(email: String, password: String): Result<Unit> {
        return try {
            val user = firebaseAuth.logIn(email, password)
            if (user != null) {
                firebaseUser.updateLastLogin(user.uid)
                val cloudProfile = firebaseUser.getUserProfile(user.uid)
                if (cloudProfile != null) {
                    userRepository.saveUserProfile(cloudProfile)
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Invalid email or password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logOut() {
        firebaseAuth.signOut()
        userRepository.clearProfile()
    }

    fun getCurrentUserId(): String? = firebaseAuth.getCurrentUser()?.uid
    
    fun getCurrentUserEmail(): String? = firebaseAuth.getCurrentUser()?.email
    
    fun isAnonymous(): Boolean = firebaseAuth.getCurrentUser()?.isAnonymous ?: false

    fun isLoggedIn(): Boolean = firebaseAuth.isLoggedIn()
}
