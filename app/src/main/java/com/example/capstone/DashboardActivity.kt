package com.example.capstone

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.capstone.location.LocationHelper
import java.util.Calendar

class DashboardActivity : AppCompatActivity() {

    private val requestLocationPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        updateLocation()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        val name = intent.getStringExtra(EXTRA_NAME)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: "User"

        findViewById<TextView>(R.id.greeting).text = getGreeting()
        findViewById<TextView>(R.id.userName).text = name
        findViewById<TextView>(R.id.profileBadge).text = initials(name)

        ensureLocationPermissionAndUpdate()
    }

    private fun ensureLocationPermissionAndUpdate() {
        try {
            val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            val granted = fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED

            if (granted) {
                updateLocation()
                return
            }

            requestLocationPermission.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } catch (_: Throwable) {
            findViewById<TextView>(R.id.location).text = "📍 Location unavailable"
        }
    }

    private fun updateLocation() {
        val locationTv = findViewById<TextView>(R.id.location)
        try {
            LocationHelper.fetchCity(this) { city ->
                runOnUiThread {
                    locationTv.text = if (city.isNullOrBlank()) {
                        "📍 Location unavailable"
                    } else {
                        "📍 $city"
                    }
                }
            }
        } catch (_: Throwable) {
            locationTv.text = "📍 Location unavailable"
        }
    }

    private fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good morning 👋"
            in 12..16 -> "Good afternoon 👋"
            in 17..20 -> "Good evening 👋"
            else -> "Good night 👋"
        }
    }

    private fun initials(name: String): String {
        val parts = name.split(" ").filter { it.isNotBlank() }
        val first = parts.getOrNull(0)?.firstOrNull()?.uppercaseChar()
        val second = parts.getOrNull(1)?.firstOrNull()?.uppercaseChar()
        return listOfNotNull(first, second).joinToString("").ifBlank { "U" }
    }

    companion object {
        const val EXTRA_NAME = "extra_name"
    }
}
