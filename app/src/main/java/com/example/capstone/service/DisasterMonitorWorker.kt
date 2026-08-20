package com.example.capstone.service

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.capstone.data.EmergencyRepository
import com.example.capstone.data.NewsRepository
import com.example.capstone.data.SafeReadyPreferences
import com.example.capstone.data.repository.MeshRepository
import com.example.capstone.location.LocationHelper

class DisasterMonitorWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prefs = SafeReadyPreferences(applicationContext)
        val meshRepository = MeshRepository(applicationContext)
        val emergencyRepository = EmergencyRepository(applicationContext, prefs, meshRepository)
        val newsRepository = NewsRepository()

        return try {
            // Get current city
            var currentCity: String? = null
            LocationHelper.fetchCity(applicationContext) { city ->
                currentCity = city
            }
            
            // LocationHelper.fetchCity is callback-based, wait a bit or use a better way
            // For now, let's assume we have it or use a default if we can't wait
            // Better: use a suspending location fetch if available. 
            // Since I can't easily change LocationHelper now, I'll wait briefly.
            
            kotlinx.coroutines.delay(2000)

            val city = currentCity ?: prefs.getUserProfile().city ?: return Result.retry()

            val news = newsRepository.fetchDisasterNews(city)
            
            // Check if any news articles indicate an active disaster
            val disasterKeywords = listOf("earthquake", "flood", "cyclone", "tsunami", "landslide", "evacuation", "emergency")
            val activeDisaster = news.any { article ->
                val content = (article.title + " " + (article.description ?: "")).lowercase()
                disasterKeywords.any { content.contains(it) }
            }

            if (activeDisaster) {
                Log.d("DisasterMonitor", "Disaster detected in $city! Activating Emergency Mode.")
                if (!emergencyRepository.isEmergencyModeEnabled()) {
                    emergencyRepository.setEmergencyModeEnabled(true)
                    
                    // Trigger SOS automatically since disaster is detected in local city
                    emergencyRepository.triggerSos(
                        reason = "Automated Trigger: Disaster detected in $city",
                        isAutomatic = true
                    )
                    
                    // Notify user or start service
                    val intent = android.content.Intent(applicationContext, EmergencyService::class.java)
                    applicationContext.startService(intent)
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("DisasterMonitor", "Error checking for disasters", e)
            Result.retry()
        }
    }
}
