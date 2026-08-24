package com.example.capstone.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.capstone.MainActivity
import com.example.capstone.R
import com.example.capstone.data.MeshMessage
import com.example.capstone.data.MeshMessageType
import com.example.capstone.data.SafeReadyPreferences
import com.example.capstone.data.EmergencyRepository
import com.example.capstone.data.repository.MeshRepository
import com.example.capstone.location.LocationHelper
import com.example.capstone.util.EmergencyMessageFormatter
import com.example.capstone.util.EmergencySmsHelper
import kotlin.math.sqrt

class EmergencyService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var meshRepository: MeshRepository
    private lateinit var emergencyRepository: EmergencyRepository
    private lateinit var prefs: SafeReadyPreferences

    private var lastShakeTime: Long = 0
    private val SHAKE_THRESHOLD = 800
    
    // Inactivity detection
    private var lastMovementTime: Long = System.currentTimeMillis()
    private val INACTIVITY_TIMEOUT = 10 * 60 * 1000L // 10 minutes
    private var inactivityTask: java.util.TimerTask? = null
    private val timer = java.util.Timer()
    
    // Scream detection stubs
    private var isScreamDetectionEnabled = true

    override fun onCreate() {
        super.onCreate()
        meshRepository = MeshRepository(this)
        prefs = SafeReadyPreferences(this)
        emergencyRepository = EmergencyRepository(this, prefs, meshRepository)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
        startForegroundService()
        registerSensors()
    }

    private fun startForegroundService() {
        val channelId = "emergency_service_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Emergency Monitoring",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("NAVIGATE_TO", "EMERGENCY")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("SafeReady Emergency Mode")
            .setContentText("Monitoring for falls and emergencies...")
            .setSmallIcon(R.drawable.ic_emergency_fab)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1001, notification)
    }

    private fun registerSensors() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        
        startInactivityTimer()
        startScreamDetection()
    }

    private fun startScreamDetection() {
        // STUB: In a real app, we would use AudioRecord or a speech recognition service
        // to listen for specific patterns or high-decibel screams.
        Log.d("EmergencyService", "Scream detection stub initialized")
    }

    private fun startInactivityTimer() {
        inactivityTask?.cancel()
        inactivityTask = object : java.util.TimerTask() {
            override fun run() {
                if (System.currentTimeMillis() - lastMovementTime > INACTIVITY_TIMEOUT) {
                    triggerAutoSOS("Automatic Trigger: Prolonged Inactivity Detected")
                }
            }
        }
        timer.schedule(inactivityTask, INACTIVITY_TIMEOUT, INACTIVITY_TIMEOUT)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            
            val acceleration = sqrt(x * x + y * y + z * z)
            
            // If there is significant movement, reset inactivity timer
            if (acceleration > 12.0f || acceleration < 8.0f) {
                lastMovementTime = System.currentTimeMillis()
            }
            
            detectFall(x, y, z)
        }
    }

    private fun detectFall(x: Float, y: Float, z: Float) {
        val acceleration = sqrt(x * x + y * y + z * z)
        // Simple fall detection logic: very low acceleration (free fall) followed by high impact
        // For this stub, we'll use a high acceleration threshold to simulate a "hard impact" or shake trigger
        if (acceleration > 30) { 
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastShakeTime > 5000) { // Throttle SOS to every 5 seconds
                lastShakeTime = currentTime
                triggerAutoSOS("Automatic Trigger: Fall/Impact Detected")
            }
        }
    }

    private fun triggerAutoSOS(reason: String) {
        Log.d("EmergencyService", "Auto SOS Triggered: $reason")
        emergencyRepository.triggerSos(reason = reason, isAutomatic = true)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        inactivityTask?.cancel()
        timer.cancel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
}
