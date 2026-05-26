package com.example.capstone.service

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Manages Firebase Cloud Messaging (FCM) token lifecycle.
 * Handles token acquisition, storage, and refresh.
 * 
 * Phase 4A: Push Notifications
 */
class FCMTokenManager(private val context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    /**
     * Get the current FCM token, refreshing if necessary.
     * Called when the app initializes or user logs in.
     */
    fun getToken(onTokenReady: (String) -> Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d(TAG, "FCM Token: $token")
                saveToken(token)
                onTokenReady(token)
            } else {
                Log.e(TAG, "Failed to get FCM token", task.exception)
            }
        }
    }

    /**
     * Save token to local SharedPreferences for offline reference.
     */
    private fun saveToken(token: String) {
        preferences.edit().putString(KEY_FCM_TOKEN, token).apply()
        preferences.edit().putLong(KEY_TOKEN_TIME, System.currentTimeMillis()).apply()
    }

    /**
     * Retrieve previously saved token from SharedPreferences.
     * Returns null if token has expired or not set.
     */
    fun getSavedToken(): String? {
        val savedToken = preferences.getString(KEY_FCM_TOKEN, null)
        val savedTime = preferences.getLong(KEY_TOKEN_TIME, 0)
        
        // Tokens don't typically expire, but we'll consider them stale after 30 days
        val thirtyDaysMs = 30L * 24 * 60 * 60 * 1000
        if (savedToken != null && (System.currentTimeMillis() - savedTime) < thirtyDaysMs) {
            return savedToken
        }
        return null
    }

    /**
     * Delete the saved token (e.g., on logout).
     */
    fun clearToken() {
        preferences.edit().remove(KEY_FCM_TOKEN).apply()
        preferences.edit().remove(KEY_TOKEN_TIME).apply()
        Log.d(TAG, "FCM token cleared")
    }

    /**
     * Unsubscribe from all topics (called on logout).
     */
    fun unsubscribeFromAllTopics() {
        // Unsubscribe from default topics
        FirebaseMessaging.getInstance().unsubscribeFromTopic("all_users")
        FirebaseMessaging.getInstance().unsubscribeFromTopic("alerts")
        Log.d(TAG, "Unsubscribed from all topics")
    }

    /**
     * Subscribe to disaster-specific alert topics (called on login or region selection).
     */
    fun subscribeToDisasterTopics(disasters: List<String>) {
        disasters.forEach { disaster ->
            FirebaseMessaging.getInstance().subscribeToTopic("disaster_$disaster")
            Log.d(TAG, "Subscribed to topic: disaster_$disaster")
        }
    }

    companion object {
        private const val TAG = "FCMTokenManager"
        private const val PREFS_NAME = "fcm_prefs"
        private const val KEY_FCM_TOKEN = "fcm_token"
        private const val KEY_TOKEN_TIME = "fcm_token_time"
    }
}

