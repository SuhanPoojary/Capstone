package com.example.capstone

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private val handler by lazy { Handler(Looper.getMainLooper()) }
    private var hasNavigated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        if (savedInstanceState?.getBoolean(KEY_HAS_NAVIGATED) == true) {
            hasNavigated = true
        }

        if (!hasNavigated) {
            handler.postDelayed({
                if (!isFinishing) {
                    hasNavigated = true
                    startActivity(Intent(this, OnboardingActivity::class.java))
                    finish()
                }
            }, SPLASH_DELAY_MS)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(KEY_HAS_NAVIGATED, hasNavigated)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private companion object {
        const val SPLASH_DELAY_MS = 2000L
        const val KEY_HAS_NAVIGATED = "hasNavigated"
    }
}
