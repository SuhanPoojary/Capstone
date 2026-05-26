package com.example.capstone

import android.os.Bundle
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.capstone.data.MeshMessage
import com.example.capstone.data.MeshMessageType
import com.example.capstone.data.SafeReadyPreferences
import com.example.capstone.presentation.viewmodel.MeshViewModel
import android.view.animation.AnimationUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class EmergencyActivity : AppCompatActivity() {
    private lateinit var meshViewModel: MeshViewModel
    private lateinit var prefs: SafeReadyPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency)

        meshViewModel = ViewModelProvider(this, defaultViewModelProviderFactory)[MeshViewModel::class.java]
        prefs = SafeReadyPreferences(this)
        prefs.setEmergencyModeEnabled(true)

        val lastAction = findViewById<TextView>(R.id.emergencyLastAction)
        val sosBtn = findViewById<FrameLayout>(R.id.btnSendSosEmergency)
        val exitBtn = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnExitEmergency)

        fun updateUi() {
            val enabled = prefs.getEmergencyModeEnabled()
            sosBtn.isEnabled = enabled
        }

        sosBtn.setOnClickListener {
            if (!prefs.getEmergencyModeEnabled()) {
                Toast.makeText(this, "Enable Emergency Mode first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            AlertDialog.Builder(this)
                .setTitle("Confirm SOS")
                .setMessage("Broadcast an SOS to nearby devices?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Send") { _, _ ->
                    val pkg = packageName
                    val msg = MeshMessage(
                        senderId = pkg,
                        senderName = null,
                        type = MeshMessageType.SOS,
                        content = "SOS: I need help. Sent from $pkg"
                    )
                    meshViewModel.broadcast(msg)
                    Toast.makeText(this, "SOS queued", Toast.LENGTH_SHORT).show()
                }
                .show()
        }

        // Exit button closes emergency activity
        exitBtn.setOnClickListener {
            prefs.setEmergencyModeEnabled(false)
            finish()
        }

        // start a subtle pulse animation on the large SOS button to indicate broadcasting
        val pulse = AnimationUtils.loadAnimation(this, R.anim.sos_pulse)
        sosBtn.startAnimation(pulse)

        lifecycleScope.launch {
            meshViewModel.actionMessage.collectLatest { m ->
                if (!m.isNullOrBlank()) {
                    lastAction.text = m
                }
            }
        }

        updateUi()
    }
}

