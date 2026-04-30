package com.example.capstone.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * A service that handles sending emergency alert notifications.
 * Can be started to send alerts on demand or scheduled for periodic alerts.
 * 
 * In a production app, this would listen for real push notifications or database updates.
 * For the capstone, this simulates alert delivery.
 */
class AlertNotificationService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.action ?: return START_REDELIVER_INTENT
            
            when (action) {
                ACTION_SEND_ALERT -> {
                    val disasterKey = intent.getStringExtra(EXTRA_DISASTER_KEY) ?: "earthquake"
                    val title = intent.getStringExtra(EXTRA_TITLE) ?: "Disaster Alert"
                    val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "An alert has been issued."
                    sendAlert(disasterKey, title, message)
                }
            }
        }
        return START_REDELIVER_INTENT
    }

    private fun sendAlert(disasterKey: String, title: String, message: String) {
        val notificationHelper = NotificationHelper(this)
        notificationHelper.postAlertNotification(title, message, disasterKey)
    }

    companion object {
        const val ACTION_SEND_ALERT = "com.example.capstone.SEND_ALERT"
        const val EXTRA_DISASTER_KEY = "disaster_key"
        const val EXTRA_TITLE = "title"
        const val EXTRA_MESSAGE = "message"
    }
}

