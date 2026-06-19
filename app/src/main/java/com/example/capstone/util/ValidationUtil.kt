package com.example.capstone.util

import android.util.Patterns

object ValidationUtil {

    fun isValidEmail(email: String?): ValidationResult {
        if (email.isNullOrBlank()) return ValidationResult(false, "Email cannot be empty")
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return ValidationResult(false, "Invalid email format")
        }
        return ValidationResult(true)
    }

    /**
     * Password Requirements:
     * - Minimum 8 characters
     * - At least 1 uppercase letter
     * - At least 1 lowercase letter
     * - At least 1 number
     * - At least 1 special character
     */
    fun isValidPassword(password: String?): ValidationResult {
        if (password.isNullOrBlank()) return ValidationResult(false, "Password cannot be empty")
        if (password.length < 8) return ValidationResult(false, "Minimum 8 characters required")
        
        val hasUppercase = password.any { it.isUpperCase() }
        val hasLowercase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }

        return when {
            !hasUppercase -> ValidationResult(false, "At least 1 uppercase letter required")
            !hasLowercase -> ValidationResult(false, "At least 1 lowercase letter required")
            !hasDigit -> ValidationResult(false, "At least 1 number required")
            !hasSpecial -> ValidationResult(false, "At least 1 special character required")
            else -> ValidationResult(true)
        }
    }

    fun isValidUsername(username: String?): ValidationResult {
        if (username.isNullOrBlank()) return ValidationResult(false, "Username cannot be empty")
        val trimmed = username.trim()
        if (trimmed.length < 3) return ValidationResult(false, "Minimum 3 characters required")
        if (trimmed.length > 30) return ValidationResult(false, "Maximum 30 characters allowed")
        if (trimmed != username) return ValidationResult(false, "No leading or trailing spaces allowed")
        
        // Allow alphanumeric and underscores
        val regex = Regex("^[a-zA-Z0-9_]+$")
        if (!regex.matches(trimmed)) {
            return ValidationResult(false, "Only letters, numbers, and underscores allowed")
        }
        
        return ValidationResult(true)
    }

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )
}
