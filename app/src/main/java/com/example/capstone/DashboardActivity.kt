package com.example.capstone

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class DashboardActivity : AppCompatActivity() {

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

        // Location: keep a stable default for now
        findViewById<TextView>(R.id.location).text = "📍 Maharashtra"

        findViewById<android.view.View>(R.id.navTraining)?.setOnClickListener {
            startActivity(Intent(this, StartLearningActivity::class.java))
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
