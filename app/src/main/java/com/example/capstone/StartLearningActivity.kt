package com.example.capstone

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class StartLearningActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_start_learning)

        findViewById<android.view.View>(R.id.backButton)?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Make cards clickable (each card root has its own id)
        findViewById<android.view.View>(R.id.cardEarthquake)?.setOnClickListener {
            startActivity(DisasterDetailActivity.newIntent(this, "earthquake"))
        }
        findViewById<android.view.View>(R.id.cardFloods)?.setOnClickListener {
            startActivity(DisasterDetailActivity.newIntent(this, "floods"))
        }
        findViewById<android.view.View>(R.id.cardCyclone)?.setOnClickListener {
            startActivity(DisasterDetailActivity.newIntent(this, "cyclone"))
        }
        findViewById<android.view.View>(R.id.cardLandslides)?.setOnClickListener {
            startActivity(DisasterDetailActivity.newIntent(this, "landslides"))
        }
    }
}
