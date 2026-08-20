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

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.capstone.service.DisasterMonitorWorker
import org.osmdroid.config.Configuration
import java.util.concurrent.TimeUnit

class SafeReadyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Set osmdroid User-Agent to comply with OSM Tile Usage Policy
        Configuration.getInstance().userAgentValue = packageName

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
