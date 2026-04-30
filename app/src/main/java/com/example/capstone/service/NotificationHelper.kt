package com.example.capstone.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.capstone.MainActivity
import com.example.capstone.R

/**
 * Helper for creating and posting notifications.
 * Manages notification channels for API 26+ and provides a consistent notification builder.
 */
class NotificationHelper(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        // Create notification channels for API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannels()
        }
    }

    /**
     * Post an alert notification to the user.
     * Tapping the notification opens MainActivity.
     */
    fun postAlertNotification(
        title: String,
        message: String,
        disasterKey: String? = null
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            if (disasterKey != null) {
                putExtra("disasterKey", disasterKey)
            }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(NOTIFICATION_ID_ALERT, notification)
    }

    /**
     * Post a sync status notification to the user.
     * Useful for indicating when progress has been synced to the cloud.
     */
    fun postSyncNotification(message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_SYNC)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Sync Status")
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        notificationManager.notify(NOTIFICATION_ID_SYNC, notification)
    }

    /**
     * Cancel an alert notification.
     */
    fun cancelAlertNotification() {
        notificationManager.cancel(NOTIFICATION_ID_ALERT)
    }

    /**
     * Cancel a sync notification.
     */
    fun cancelSyncNotification() {
        notificationManager.cancel(NOTIFICATION_ID_SYNC)
    }

    private fun createNotificationChannels() {
        val alertChannel = NotificationChannel(
            CHANNEL_ALERTS,
            "Disaster Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for disaster alerts and warnings"
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(alertChannel)

        val syncChannel = NotificationChannel(
            CHANNEL_SYNC,
            "Sync Status",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Notifications for progress sync status"
            enableVibration(false)
        }
        notificationManager.createNotificationChannel(syncChannel)
    }

    companion object {
        private const val CHANNEL_ALERTS = "disaster_alerts"
        private const val CHANNEL_SYNC = "sync_status"
        private const val NOTIFICATION_ID_ALERT = 1001
        private const val NOTIFICATION_ID_SYNC = 1002
    }
}

