package com.example.capstone.data

/**
 * Data models for Phase 3 (Firebase and cloud sync features).
 */

/**
 * Represents the result of an authentication attempt.
 */
data class AuthResult(
    val success: Boolean,
    val userId: String? = null,
    val email: String? = null,
    val isAnonymous: Boolean = false,
    val message: String? = null,
)

/**
 * Represents sync status for cloud operations.
 */
data class SyncStatus(
    val isInProgress: Boolean = false,
    val lastSyncTime: Long? = null,
    val successCount: Int = 0,
    val failureCount: Int = 0,
    val message: String? = null,
)

/**
 * Represents a simulated emergency alert.
 */
data class EmergencyAlert(
    val id: String,
    val disasterKey: String,
    val title: String,
    val message: String,
    val severity: AlertSeverity = AlertSeverity.MEDIUM,
    val timestamp: Long = System.currentTimeMillis(),
)

enum class AlertSeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}

/**
 * Represents cloud sync configuration.
 */
data class SyncConfig(
    val autoSyncEnabled: Boolean = true,
    val syncIntervalMinutes: Int = 30,
    val lastSyncTime: Long = 0L,
)

