package com.example.capstone

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class OnboardingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_onboarding)

        val goToSignup = {
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }

        findViewById<MaterialButton>(R.id.btnGetStarted).setOnClickListener { goToSignup() }
        findViewById<MaterialButton>(R.id.btnSkip).setOnClickListener { goToSignup() }
    }
}
