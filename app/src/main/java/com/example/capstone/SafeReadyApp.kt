package com.example.capstone

import android.app.Application
import android.os.Looper
import android.util.Log
import com.example.capstone.service.NotificationHelper
import com.google.firebase.FirebaseApp
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.preference.PreferenceManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.capstone.service.DisasterMonitorWorker
import org.osmdroid.config.Configuration
import java.util.concurrent.TimeUnit

class SafeReadyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize osmdroid configuration
        val ctx = applicationContext
        val configuration = Configuration.getInstance()
        
        // Load existing configuration from shared preferences
        configuration.load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        
        // 1. UNIQUE IDENTIFIER (Mandatory for OSM Tile Usage Policy)
        // Using a highly specific versioned string to bypass potential IP/UA greylisting.
        configuration.userAgentValue = "SafeReady-Emergency-Ops-v3.2-Final (contact: tech-support@safeready-project.org; https://safeready-project.org)"
        
        // 2. FRESH CACHE BUCKET
        // Prevents the app from loading 403-cached "empty" tiles from previous sessions.
        val osmDataDir = ctx.getDir("osmdroid", MODE_PRIVATE)
        configuration.osmdroidBasePath = osmDataDir
        configuration.osmdroidTileCache = File(osmDataDir, "tiles_v5_emergency_stable")
        
        // 3. OPTIMIZED SETTINGS FOR HIGH DENSITY (8000+ Shelters)
        configuration.tileDownloadThreads = 2 
        configuration.tileFileSystemCacheMaxBytes = 300L * 1024 * 1024 // Increased to 300MB
        configuration.expirationExtendedDuration = 14L * 24 * 60 * 60 * 1000L // 14 Days
        
        // Save the configuration
        configuration.save(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize notification channels
        NotificationHelper(this)

        // Schedule Disaster Monitor
        scheduleDisasterMonitoring()

        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            try {
                Log.e("SafeReadyCrash", "Uncaught exception on thread=${t.name}", e)
                writeCrashToFile(t.name, e)

                // Give log a moment to flush in some ROMs.
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    try {
                        Thread.sleep(250)
                    } catch (_: InterruptedException) {
                    }
                }
            } catch (_: Throwable) {
            } finally {
                previous?.uncaughtException(t, e)
            }
        }
    }

    private fun scheduleDisasterMonitoring() {
        val monitorRequest = PeriodicWorkRequestBuilder<DisasterMonitorWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "disaster_monitor",
            ExistingPeriodicWorkPolicy.KEEP,
            monitorRequest
        )
    }

    private fun writeCrashToFile(threadName: String, e: Throwable) {
        try {
            val dir = File(filesDir, "crash")
            dir.mkdirs()

            val ts = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
            val file = File(dir, "crash_$ts.txt")

            val stack = Log.getStackTraceString(e)
            file.writeText(
                "SafeReady crash\n" +
                    "time=$ts\n" +
                    "thread=$threadName\n\n" +
                    stack
            )
        } catch (_: Throwable) {
        }
    }
}
