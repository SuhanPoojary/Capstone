package com.example.capstone.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Handles incoming Firebase Cloud Messaging (FCM) notifications.
 * 
 * Phase 4A: Push Notifications
 * 
 * Message format (from Firebase Console or backend):
 * {
 *   "notification": {
 *     "title": "Earthquake Alert",
 *     "body": "Strong earthquake detected in your region"
 *   },
 *   "data": {
 *     "disaster": "earthquake",
 *     "severity": "high",
 *     "actionUrl": "training://earthquake/chapter2"
 *   }
 * }
 */
class SafeReadyMessagingService : FirebaseMessagingService() {

    /**
     * Called when a new FCM token is generated.
     * This happens when the app is first installed, when the user uninstalls/reinstalls,
     * and periodically (usually once per month).
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        
        // Send token to your backend if needed
        sendTokenToServer(token)
    }

    /**
     * Called when a message is received from FCM.
     * Handles both notification and data messages.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "Message received from: ${remoteMessage.from}")
        
        // Handle notification messages (displayed automatically if app is in background)
        remoteMessage.notification?.let {
            Log.d(TAG, "Notification Title: ${it.title}")
            Log.d(TAG, "Notification Body: ${it.body}")
            
            val title = it.title ?: "SafeReady Alert"
            val body = it.body ?: "You have a new notification"
            
            handleNotificationMessage(title, body, remoteMessage.data)
        }
        
        // Handle data-only messages
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }
    }

    /**
     * Process notification messages and show appropriate notification.
     */
    private fun handleNotificationMessage(
        title: String,
        body: String,
        data: Map<String, String>
    ) {
        val notificationHelper = NotificationHelper(this)
        val disasterKey = data["disaster"] ?: "general"
        
        // Post the notification
        notificationHelper.postAlertNotification(
            title = title,
            message = body,
            disasterKey = disasterKey
        )
        
        Log.d(TAG, "Notification posted: $title - $body")
    }

    /**
     * Process data-only messages (no automatic UI).
     * These are typically used for background work or custom handling.
     */
    private fun handleDataMessage(data: Map<String, String>) {
        val messageType = data["type"] ?: "unknown"
        
        when (messageType) {
            "disaster_alert" -> {
                // Extract alert details
                val disaster = data["disaster"] ?: "general"
                val severity = data["severity"] ?: "medium"
                val message = data["message"] ?: "An alert has been issued"
                
                Log.d(TAG, "Disaster alert: $disaster (severity: $severity)")
                
                // Show notification
                val notificationHelper = NotificationHelper(this)
                notificationHelper.postAlertNotification(
                    title = "Disaster Alert: ${disaster.uppercase()}",
                    message = message,
                    disasterKey = disaster
                )
            }
            "sync_reminder" -> {
                // Background sync or data sync reminder
                Log.d(TAG, "Sync reminder received")
                // Could trigger a background sync here
            }
            "achievement_unlocked" -> {
                // Achievement notification
                val achievement = data["achievement"] ?: "Unknown Achievement"
                val notificationHelper = NotificationHelper(this)
                notificationHelper.postAlertNotification(
                    title = "Achievement Unlocked",
                    message = achievement
                )
            }
            else -> {
                Log.d(TAG, "Unknown message type: $messageType")
            }
        }
    }

    /**
     * Send the FCM token to your backend server.
     * This allows your backend to send personalized messages to this device.
     * 
     * In a production app, you would:
     * 1. Send the token to your backend API
     * 2. Store it with the user's profile in Firestore
     * 3. Use it to send targeted notifications
     */
    private fun sendTokenToServer(token: String) {
        // TODO: In Phase 4B, integrate this with AuthRepository
        // to send the token to Firestore when user logs in
        Log.d(TAG, "Token should be sent to server: $token")
    }

    companion object {
        private const val TAG = "SafeReadyMessaging"
    }
}

