package com.example.capstone.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import kotlinx.coroutines.tasks.await

/**
 * Wrapper around Firebase Authentication for user login and signup.
 * Supports anonymous login as a fallback when network is unavailable.
 */
class FirebaseAuthDataSource {
    private val auth = FirebaseAuth.getInstance()

    /**
     * Sign up a new user with email and password.
     * Returns the user ID on success, or null on failure.
     */
    suspend fun signUp(email: String, password: String): String? = try {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        result.user?.uid
    } catch (e: FirebaseAuthWeakPasswordException) {
        null
    } catch (e: FirebaseAuthUserCollisionException) {
        null
    } catch (e: Exception) {
        null
    }

    /**
     * Log in an existing user with email and password.
     * Returns the user ID on success, or null on failure.
     */
    suspend fun logIn(email: String, password: String): String? = try {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        result.user?.uid
    } catch (e: FirebaseAuthInvalidCredentialsException) {
        null
    } catch (e: Exception) {
        null
    }

    /**
     * Sign in anonymously as a fallback for offline-first mode.
     * Returns the user ID on success, or null on failure.
     */
    suspend fun signInAnonymously(): String? = try {
        val result = auth.signInAnonymously().await()
        result.user?.uid
    } catch (e: Exception) {
        null
    }

    /**
     * Get the currently authenticated user's UID.
     * Returns null if no user is logged in.
     */
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    /**
     * Get the currently authenticated user's email.
     * Returns null if no user is logged in or if the user is anonymous.
     */
    fun getCurrentUserEmail(): String? = auth.currentUser?.email

    /**
     * Check if a user is currently logged in.
     */
    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    /**
     * Check if the current user is an anonymous user.
     */
    fun isAnonymousUser(): Boolean = auth.currentUser?.isAnonymous ?: false

    /**
     * Sign out the current user.
     */
    fun signOut() {
        auth.signOut()
    }
}

