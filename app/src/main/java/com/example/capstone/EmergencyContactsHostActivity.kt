package com.example.capstone

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.example.capstone.presentation.EmergencyContactsFragment

class EmergencyContactsHostActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val containerId = View.generateViewId()
        setContentView(FrameLayout(this).apply {
            id = containerId
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        })

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(containerId, EmergencyContactsFragment())
            }
        }
    }
}
