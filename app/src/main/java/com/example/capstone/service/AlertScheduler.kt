package com.example.capstone.service

import android.content.Context
import android.content.Intent
import androidx.work.BackoffPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/**
 * Schedules simulated emergency alert notifications.
 * Uses AndroidX WorkManager for reliable background task execution.
 * 
 * In a production app, this would receive real alerts from a backend service.
 * For the capstone, this simulates periodic alert delivery.
 */
class AlertScheduler(private val context: Context) {

    /**
     * Schedule a simulated alert to be sent after a delay.
     * 
     * @param disasterKey The type of disaster (earthquake, flood, cyclone, landslide)
     * @param title Notification title
     * @param message Notification message
     * @param delayMinutes Delay in minutes before sending the alert
     */
    fun scheduleAlert(
        disasterKey: String,
        title: String,
        message: String,
        delayMinutes: Long = 1
    ) {
        val alertRequest = OneTimeWorkRequestBuilder<AlertWorker>()
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                1,
                TimeUnit.MINUTES
            )
            .addTag("alert_$disasterKey")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "alert_${System.currentTimeMillis()}",
            androidx.work.ExistingWorkPolicy.APPEND,
            alertRequest
        )
    }

    /**
     * Cancel all scheduled alerts.
     */
    fun cancelAllAlerts() {
        WorkManager.getInstance(context).cancelAllWork()
    }

    /**
     * Send an alert immediately (synchronously within the service).
     */
    fun sendAlertNow(
        disasterKey: String,
        title: String,
        message: String
    ) {
        val intent = Intent(context, AlertNotificationService::class.java).apply {
            action = AlertNotificationService.ACTION_SEND_ALERT
            putExtra(AlertNotificationService.EXTRA_DISASTER_KEY, disasterKey)
            putExtra(AlertNotificationService.EXTRA_TITLE, title)
            putExtra(AlertNotificationService.EXTRA_MESSAGE, message)
        }
        context.startService(intent)
    }
}

/**
 * Worker for executing alert notifications in the background.
 * Triggered by WorkManager based on schedule.
 */
class AlertWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        return try {
            val disasterKey = inputData.getString("disaster_key") ?: "earthquake"
            val title = inputData.getString("title") ?: "Disaster Alert"
            val message = inputData.getString("message") ?: "An alert has been issued."

            val intent = Intent(applicationContext, AlertNotificationService::class.java).apply {
                action = AlertNotificationService.ACTION_SEND_ALERT
                putExtra(AlertNotificationService.EXTRA_DISASTER_KEY, disasterKey)
                putExtra(AlertNotificationService.EXTRA_TITLE, title)
                putExtra(AlertNotificationService.EXTRA_MESSAGE, message)
            }
            applicationContext.startService(intent)

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

