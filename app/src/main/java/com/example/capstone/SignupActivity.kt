package com.example.capstone

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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

class SignupActivity : AppCompatActivity() {

    private lateinit var viewModel: AuthViewModel
    
    private lateinit var layoutName: TextInputLayout
    private lateinit var layoutUsername: TextInputLayout
    private lateinit var layoutEmail: TextInputLayout
    private lateinit var layoutPassword: TextInputLayout
    private lateinit var layoutInstitution: TextInputLayout
    private lateinit var btnCreate: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)

        setupViewModel()
        initViews()
        setupListeners()
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
        layoutName = findViewById(R.id.layoutName)
        layoutUsername = findViewById(R.id.layoutUsername)
        layoutEmail = findViewById(R.id.layoutEmail)
        layoutPassword = findViewById(R.id.layoutPassword)
        layoutInstitution = findViewById(R.id.layoutInstitution)
        btnCreate = findViewById(R.id.btnCreate)
    }

    private fun setupListeners() {
        btnCreate.setOnClickListener {
            clearErrors()
            val name = layoutName.editText?.text.toString().trim()
            val username = layoutUsername.editText?.text.toString().trim()
            val email = layoutEmail.editText?.text.toString().trim()
            val password = layoutPassword.editText?.text.toString()
            val institution = layoutInstitution.editText?.text.toString().trim()

            if (validate(name, username, email, password)) {
                viewModel.signUp(email, password!!, username, name, institution)
            }
        }
    }

    private fun validate(name: String, username: String, email: String, password: String?): Boolean {
        var isValid = true

        if (name.isEmpty()) {
            layoutName.error = "Full Name is required"
            isValid = false
        }

        val userRes = ValidationUtil.isValidUsername(username)
        if (!userRes.isValid) {
            layoutUsername.error = userRes.errorMessage
            isValid = false
        }

        val emailRes = ValidationUtil.isValidEmail(email)
        if (!emailRes.isValid) {
            layoutEmail.error = emailRes.errorMessage
            isValid = false
        }

        val passRes = ValidationUtil.isValidPassword(password)
        if (!passRes.isValid) {
            layoutPassword.error = passRes.errorMessage
            isValid = false
        }

        return isValid
    }

    private fun clearErrors() {
        layoutName.error = null
        layoutUsername.error = null
        layoutEmail.error = null
        layoutPassword.error = null
        layoutInstitution.error = null
    }

    private fun showLoading(isLoading: Boolean) {
        btnCreate.isEnabled = !isLoading
        btnCreate.text = if (isLoading) "Creating Account..." else getString(R.string.signup_primary)
    }

    private fun showGeneralError(message: String) {
        Snackbar.make(findViewById(R.id.signupRoot), message, Snackbar.LENGTH_LONG).show()
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }
}
