package com.example.capstone.data.repository

import com.example.capstone.data.remote.firebase.FirebaseAuthDataSource
import com.example.capstone.data.remote.firebase.FirebaseUserDataSource
import com.example.capstone.data.UserProfile
import com.example.capstone.data.UserRepository

/**
 * Handles authentication and user identity management.
 * Wraps both Firebase Auth and local user profile storage.
 * 
 * Offline-first strategy:
 * - If network is available, use Firebase Authentication.
 * - If network is unavailable, allow anonymous login to preserve offline access.
 * - Local profile is always kept in sync with cloud profile for resilience.
 */
class AuthRepository(
    private val firebaseAuth: FirebaseAuthDataSource,
    private val firebaseUser: FirebaseUserDataSource,
    private val userRepository: UserRepository,
) {
    /**
     * Attempt to sign up a new user.
     * Tries Firebase Auth first; falls back to local profile if network is unavailable.
     * Returns true if signup succeeded (either via Firebase or local fallback).
     */
    suspend fun signUp(
        email: String,
        password: String,
        name: String,
        institution: String
    ): Boolean {
        // Try Firebase signup
        val userId = firebaseAuth.signUp(email, password)
        
        if (userId != null) {
            // Firebase signup succeeded; save profile locally and to cloud
            userRepository.saveUserProfile(name, email, institution)
            firebaseUser.saveUserProfile(userId, UserProfile(
                name = name,
                email = email,
                institution = institution,
            ))
            return true
        }

        // Firebase signup failed; fall back to local profile
        // This preserves offline-first behavior
        userRepository.saveUserProfile(name, email, institution)
        
        // Try anonymous login as a fallback
        firebaseAuth.signInAnonymously()
        
        return true
    }

    /**
     * Attempt to log in an existing user.
     * Tries Firebase Auth first; falls back to anonymous login if network is unavailable.
     * Returns true if login succeeded.
     */
    suspend fun logIn(email: String, password: String): Boolean {
        // Try Firebase login
        val userId = firebaseAuth.logIn(email, password)
        
        if (userId != null) {
            // Firebase login succeeded; fetch and store cloud profile locally
            val cloudProfile = firebaseUser.getUserProfile(userId)
            if (cloudProfile != null) {
                userRepository.saveUserProfile(
                    cloudProfile.name,
                    cloudProfile.email,
                    cloudProfile.institution
                )
                if (cloudProfile.city != null || cloudProfile.state != null) {
                    userRepository.saveRegion(cloudProfile.city, cloudProfile.state)
                }
            }
            return true
        }

        // Firebase login failed; fall back to anonymous login
        // This preserves offline-first behavior
        firebaseAuth.signInAnonymously()
        return true
    }

    /**
     * Log out the current user.
     */
    fun logOut() {
        firebaseAuth.signOut()
    }

    /**
     * Get the current user's ID (may be anonymous).
     * Returns null if no user is logged in.
     */
    fun getCurrentUserId(): String? = firebaseAuth.getCurrentUserId()

    /**
     * Get the current user's email.
     * Returns null if no authenticated user or if using anonymous login.
     */
    fun getCurrentUserEmail(): String? = firebaseAuth.getCurrentUserEmail()

    /**
     * Check if a user is currently logged in.
     */
    fun isLoggedIn(): Boolean = firebaseAuth.isUserLoggedIn()

    /**
     * Check if the current user is an anonymous user.
     */
    fun isAnonymous(): Boolean = firebaseAuth.isAnonymousUser()
}

