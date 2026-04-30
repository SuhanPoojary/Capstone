package com.example.capstone

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.capstone.data.SafeReadyPreferences
import com.example.capstone.data.UserRepository
import com.example.capstone.presentation.AssistantFragment
import com.example.capstone.presentation.HomeFragment
import com.example.capstone.presentation.ProfileFragment
import com.example.capstone.presentation.ProgressFragment
import com.example.capstone.presentation.TrainingFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private var currentTabId: Int = R.id.nav_home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val prefs = SafeReadyPreferences(this)
        val userRepository = UserRepository(prefs)
        val existing = userRepository.getProfile()

        val name = intent.getStringExtra(EXTRA_NAME)?.takeIf { it.isNotBlank() } ?: existing.name
        val email = intent.getStringExtra(EXTRA_EMAIL)?.takeIf { it.isNotBlank() } ?: existing.email
        val institution = intent.getStringExtra(EXTRA_INSTITUTION)?.takeIf { it.isNotBlank() } ?: existing.institution
        userRepository.saveUserProfile(name, email, institution)

        if (savedInstanceState != null) {
            currentTabId = savedInstanceState.getInt(KEY_CURRENT_TAB, R.id.nav_home)
        }

        bottomNav = findViewById(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId != currentTabId) {
                currentTabId = item.itemId
                showTab(item.itemId)
            }
            true
        }

        bottomNav.selectedItemId = currentTabId
        if (savedInstanceState == null) {
            showTab(currentTabId)
        }
    }

    fun selectTab(itemId: Int) {
        bottomNav.selectedItemId = itemId
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(KEY_CURRENT_TAB, currentTabId)
        super.onSaveInstanceState(outState)
    }

    private fun showTab(itemId: Int) {
        val fragment: Fragment = when (itemId) {
            R.id.nav_training -> TrainingFragment()
            R.id.nav_progress -> ProgressFragment()
            R.id.nav_assistant -> AssistantFragment()
            R.id.nav_profile -> ProfileFragment()
            else -> HomeFragment()
        }

        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.mainFragmentContainer, fragment)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .commit()
    }

    companion object {
        const val EXTRA_NAME = "extra_name"
        const val EXTRA_EMAIL = "extra_email"
        const val EXTRA_INSTITUTION = "extra_institution"
        private const val KEY_CURRENT_TAB = "key_current_tab"
    }
}