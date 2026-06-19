package com.example.capstone

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.capstone.data.SafeReadyPreferences
import com.example.capstone.data.UserRepository
import com.example.capstone.data.remote.firebase.FirebaseAuthDataSource
import com.example.capstone.data.remote.firebase.FirebaseUserDataSource
import com.example.capstone.data.repository.AuthRepository

class SplashActivity : AppCompatActivity() {

    private val handler by lazy { Handler(Looper.getMainLooper()) }
    private var hasNavigated = false
    
    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        authRepository = AuthRepository(
            FirebaseAuthDataSource(),
            FirebaseUserDataSource(),
            UserRepository(SafeReadyPreferences(this))
        )

        if (savedInstanceState?.getBoolean(KEY_HAS_NAVIGATED) == true) {
            hasNavigated = true
        }

        if (!hasNavigated) {
            handler.postDelayed({
                if (!isFinishing) {
                    hasNavigated = true
                    checkAuthStateAndNavigate()
                }
            }, SPLASH_DELAY_MS)
        }
    }

    private fun checkAuthStateAndNavigate() {
        if (authRepository.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // For first time users, show onboarding. 
            // In a real app, you might track "onboardingCompleted" in Prefs.
            startActivity(Intent(this, OnboardingActivity::class.java))
        }
        finish()
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
