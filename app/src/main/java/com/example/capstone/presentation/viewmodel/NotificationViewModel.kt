package com.example.capstone.presentation.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.capstone.service.FCMTokenManager

/**
 * ViewModel for managing FCM token state and push notification configuration.
 * 
 * Phase 4A: Push Notifications
 * 
 * Responsibilities:
 * - Manage FCM token lifecycle
 * - Subscribe/unsubscribe from disaster topics
 * - Display token status in UI (for testing)
 */
class NotificationViewModel(application: Application) : AndroidViewModel(application) {
    
    private val fcmTokenManager = FCMTokenManager(application)
    
    private val _fcmToken = MutableLiveData<String?>()
    val fcmToken: LiveData<String?> = _fcmToken
    
    private val _tokenStatus = MutableLiveData("No token")
    val tokenStatus: LiveData<String> = _tokenStatus
    
    private val _subscriptionStatus = MutableLiveData<String>()
    val subscriptionStatus: LiveData<String> = _subscriptionStatus
    
    private val _isTokenReady = MutableLiveData(false)
    val isTokenReady: LiveData<Boolean> = _isTokenReady
    
    init {
        loadSavedToken()
    }
    
    /**
     * Load previously saved FCM token from SharedPreferences.
     */
    private fun loadSavedToken() {
        val saved = fcmTokenManager.getSavedToken()
        if (saved != null) {
            _fcmToken.value = saved
            _tokenStatus.value = "Token loaded: ${saved.take(10)}..."
            _isTokenReady.value = true
        } else {
            _tokenStatus.value = "Requesting new token..."
            requestNewToken()
        }
    }
    
    /**
     * Request a fresh FCM token from Firebase.
     * Should be called on app start or when user logs in.
     */
    fun requestNewToken() {
        Log.d(TAG, "Requesting new FCM token")
        fcmTokenManager.getToken { token ->
            _fcmToken.value = token
            _tokenStatus.value = "Token ready: ${token.take(10)}..."
            _isTokenReady.value = true
            Log.d(TAG, "Token acquired successfully")
        }
    }
    
    /**
     * Subscribe to disaster-specific alert topics.
     * Call this when user selects their region or logs in.
     * 
     * @param disasters List of disaster types (e.g., ["earthquake", "flood", "cyclone"])
     */
    fun subscribeToDisasterTopics(disasters: List<String>) {
        Log.d(TAG, "Subscribing to topics: $disasters")
        fcmTokenManager.subscribeToDisasterTopics(disasters)
        _subscriptionStatus.value = "Subscribed to: ${disasters.joinToString(", ")}"
    }
    
    /**
     * Subscribe to a single disaster topic.
     */
    fun subscribeToTopic(disaster: String) {
        subscribeToDisasterTopics(listOf(disaster))
    }
    
    /**
     * Unsubscribe from all topics (typically on logout).
     */
    fun unsubscribeFromAllTopics() {
        Log.d(TAG, "Unsubscribing from all topics")
        fcmTokenManager.unsubscribeFromAllTopics()
        _subscriptionStatus.value = "Unsubscribed from all topics"
    }
    
    /**
     * Clear the FCM token (on logout).
     */
    fun clearToken() {
        Log.d(TAG, "Clearing FCM token")
        fcmTokenManager.clearToken()
        _fcmToken.value = null
        _tokenStatus.value = "Token cleared"
        _isTokenReady.value = false
    }
    
    /**
     * Get the current token for testing/debugging.
     */
    fun getTokenForTesting(): String? = _fcmToken.value
    
    companion object {
        private const val TAG = "NotificationViewModel"
    }
}

