package com.example.capstone

import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.capstone.data.AuthResult
import com.example.capstone.data.SafeReadyPreferences
import com.example.capstone.data.UserRepository
import com.example.capstone.data.remote.firebase.FirebaseAuthDataSource
import com.example.capstone.data.remote.firebase.FirebaseUserDataSource
import com.example.capstone.data.repository.AuthRepository
import com.example.capstone.presentation.viewmodel.AuthViewModel
import com.example.capstone.presentation.viewmodel.AuthViewModelFactory
import com.example.capstone.util.ValidationUtil
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : AppCompatActivity() {

    private lateinit var viewModel: AuthViewModel
    
    private lateinit var layoutEmail: TextInputLayout
    private lateinit var layoutPassword: TextInputLayout
    private lateinit var btnSignIn: MaterialButton

    private var debugTapCount = 0
    private var lastDebugTapAt = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        setupViewModel()
        initViews()
        setupListeners()
        setupFooter()
        setupCrashViewerEasterEgg()
    }

    private fun setupViewModel() {
        val repository = AuthRepository(
            FirebaseAuthDataSource(),
            FirebaseUserDataSource(),
            UserRepository(SafeReadyPreferences(this))
        )
        val factory = AuthViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        viewModel.authState.observe(this) { result ->
            when (result) {
                is AuthResult.Loading -> showLoading(true)
                is AuthResult.Success -> {
                    showLoading(false)
                    navigateToMain()
                }
                is AuthResult.Error -> {
                    showLoading(false)
                    showGeneralError(result.message)
                }
            }
        }
    }

    private fun initViews() {
        layoutEmail = findViewById(R.id.layoutEmail)
        layoutPassword = findViewById(R.id.layoutPassword)
        btnSignIn = findViewById(R.id.btnSignIn)
    }

    private fun setupListeners() {
        btnSignIn.setOnClickListener {
            clearErrors()
            val email = layoutEmail.editText?.text.toString().trim()
            val password = layoutPassword.editText?.text.toString()

            if (validate(email, password)) {
                viewModel.logIn(email, password)
            }
        }
    }

    private fun validate(email: String, password: String): Boolean {
        var isValid = true

        val emailRes = ValidationUtil.isValidEmail(email)
        if (!emailRes.isValid) {
            layoutEmail.error = emailRes.errorMessage
            isValid = false
        }

        if (password.isBlank()) {
            layoutPassword.error = "Password is required"
            isValid = false
        }

        return isValid
    }

    private fun clearErrors() {
        layoutEmail.error = null
        layoutPassword.error = null
    }

    private fun setupFooter() {
        val footer = findViewById<android.widget.TextView>(R.id.footer)
        val prefix = getString(R.string.login_footer_prefix)
        val action = getString(R.string.login_footer_action)

        val spannable = SpannableStringBuilder()
            .append(prefix)
            .append(action)

        val start = prefix.length
        val end = start + action.length

        val navy = ContextCompat.getColor(this, R.color.text_primary)

        spannable.setSpan(ForegroundColorSpan(navy), start, end, 0)
        spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), start, end, 0)
        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@LoginActivity, SignupActivity::class.java))
                finish()
            }

            override fun updateDrawState(ds: android.text.TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }, start, end, 0)

        footer.text = spannable
        footer.movementMethod = LinkMovementMethod.getInstance()
        footer.highlightColor = android.graphics.Color.TRANSPARENT
    }

    private fun showLoading(isLoading: Boolean) {
        btnSignIn.isEnabled = !isLoading
        btnSignIn.text = if (isLoading) "Signing In..." else getString(R.string.login_primary)
    }

    private fun showGeneralError(message: String) {
        Snackbar.make(findViewById(R.id.loginRoot), message, Snackbar.LENGTH_LONG).show()
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }

    private fun setupCrashViewerEasterEgg() {
        val title = findViewById<android.widget.TextView>(R.id.title)
        title.setOnClickListener {
            val now = android.os.SystemClock.elapsedRealtime()
            if (now - lastDebugTapAt > 1200) {
                debugTapCount = 0
            }
            lastDebugTapAt = now
            debugTapCount++
            if (debugTapCount >= 5) {
                debugTapCount = 0
                startActivity(Intent(this, CrashViewerActivity::class.java))
            }
        }
    }
}
