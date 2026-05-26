package com.example.capstone

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.capstone.data.SafeReadyPreferences
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class Phase4HubActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phase4_hub)

        val ownerStatus = findViewById<TextView>(R.id.phase4OwnerStatus)
        val emergencyStatus = findViewById<TextView>(R.id.phase4EmergencyStatus)
        val leaderboardButton = findViewById<MaterialButton>(R.id.phase4OpenLeaderboardButton)
        val friendsButton = findViewById<MaterialButton>(R.id.phase4OpenFriendsButton)
        val feedButton = findViewById<MaterialButton>(R.id.phase4OpenFeedButton)

        val user = runCatching { FirebaseAuth.getInstance().currentUser }.getOrNull()
        ownerStatus.text = if (user == null) {
            "Firebase: signed out"
        } else {
            val suffix = if (user.isAnonymous) "anonymous" else "authenticated"
            "Firebase owner ready: ${user.uid.take(10)}... ($suffix)"
        }

        val emergencyEnabled = SafeReadyPreferences(this).getEmergencyModeEnabled()
        emergencyStatus.text = if (emergencyEnabled) {
            "Emergency Mode: ON"
        } else {
            "Emergency Mode: OFF"
        }

        leaderboardButton.setOnClickListener {
            startActivity(Intent(this, Phase4LeaderboardActivity::class.java))
        }

        friendsButton.setOnClickListener {
            startActivity(Intent(this, Phase4FriendsActivity::class.java))
        }

        feedButton.setOnClickListener {
            startActivity(Intent(this, Phase4FeedActivity::class.java))
        }
    }
}

