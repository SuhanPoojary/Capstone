package com.example.capstone

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.capstone.presentation.AssistantFragment
import com.google.android.material.card.MaterialCardView

class AssistantActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assistant)

        findViewById<AppCompatImageView>(R.id.assistantBackButton).setOnClickListener {
            finish()
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.assistantFragmentContainer, AssistantFragment())
                .commit()
        }
    }
}
