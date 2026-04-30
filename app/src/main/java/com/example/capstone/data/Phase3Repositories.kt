package com.example.capstone.data

import com.example.capstone.data.remote.firebase.FirebaseProgressDataSource
import com.example.capstone.data.repository.CloudProgressRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Manages the overall sync orchestration between local and cloud storage.
 * This is a high-level coordinator that uses CloudProgressRepository and other repositories
 * to keep data in sync without blocking the UI.
 */
class SyncRepository(
    private val cloudProgressRepository: CloudProgressRepository,
    private val userRepository: UserRepository,
    private val prefs: SafeReadyPreferences,
) {
    private var currentSyncStatus = SyncStatus()

    /**
     * Get the current sync status.
     */
    fun getSyncStatus(): SyncStatus = currentSyncStatus

    /**
     * Perform a full sync of local progress to the cloud.
     * This is non-blocking and returns immediately while sync happens in the background.
     * Returns the current sync status.
     */
    suspend fun syncProgressToCloud(userId: String?): SyncStatus = withContext(Dispatchers.IO) {
        if (userId == null) {
            currentSyncStatus = SyncStatus(
                isInProgress = false,
                message = "No user logged in; skipping cloud sync."
            )
            return@withContext currentSyncStatus
        }

        try {
            currentSyncStatus = SyncStatus(isInProgress = true, message = "Syncing progress...")

            val success = cloudProgressRepository.pushAllProgress(userId)

            currentSyncStatus = if (success) {
                SyncStatus(
                    isInProgress = false,
                    lastSyncTime = System.currentTimeMillis(),
                    successCount = currentSyncStatus.successCount + 1,
                    message = "Progress synced successfully!"
                )
            } else {
                SyncStatus(
                    isInProgress = false,
                    failureCount = currentSyncStatus.failureCount + 1,
                    message = "Sync failed; local progress preserved."
                )
            }

            currentSyncStatus
        } catch (e: Exception) {
            currentSyncStatus = SyncStatus(
                isInProgress = false,
                failureCount = currentSyncStatus.failureCount + 1,
                message = "Sync error: ${e.message}"
            )
            currentSyncStatus
        }
    }

    /**
     * Pull progress from the cloud and merge with local state.
     * This is non-blocking and returns immediately while pull happens in the background.
     * Returns the current sync status.
     */
    suspend fun pullProgressFromCloud(userId: String?): SyncStatus = withContext(Dispatchers.IO) {
        if (userId == null) {
            currentSyncStatus = SyncStatus(
                isInProgress = false,
                message = "No user logged in; skipping cloud pull."
            )
            return@withContext currentSyncStatus
        }

        try {
            currentSyncStatus = SyncStatus(isInProgress = true, message = "Pulling progress...")

            val success = cloudProgressRepository.pullAllProgress(userId)

            currentSyncStatus = if (success) {
                SyncStatus(
                    isInProgress = false,
                    lastSyncTime = System.currentTimeMillis(),
                    successCount = currentSyncStatus.successCount + 1,
                    message = "Progress updated from cloud!"
                )
            } else {
                SyncStatus(
                    isInProgress = false,
                    failureCount = currentSyncStatus.failureCount + 1,
                    message = "Pull failed; using local progress."
                )
            }

            currentSyncStatus
        } catch (e: Exception) {
            currentSyncStatus = SyncStatus(
                isInProgress = false,
                failureCount = currentSyncStatus.failureCount + 1,
                message = "Pull error: ${e.message}"
            )
            currentSyncStatus
        }
    }

    /**
     * Perform a bidirectional sync (pull first, then push).
     * Useful for keeping local and cloud state fully in sync.
     */
    suspend fun fullSync(userId: String?): SyncStatus = withContext(Dispatchers.IO) {
        if (userId == null) {
            return@withContext SyncStatus(message = "No user logged in.")
        }

        // Pull first to get latest cloud state
        pullProgressFromCloud(userId)

        // Then push local changes
        syncProgressToCloud(userId)

        currentSyncStatus
    }

    /**
     * Reset the sync status counter (useful for testing or after clear data).
     */
    fun resetSyncStatus() {
        currentSyncStatus = SyncStatus()
    }
}

/**
 * Simple factory for creating Phase 3 repositories.
 * Used to initialize all data sources and repositories in one place.
 */
object Phase3RepositoryFactory {
    fun createSyncRepository(
        context: android.content.Context,
        authRepository: com.example.capstone.data.repository.AuthRepository,
        cloudProgressRepository: CloudProgressRepository,
    ): SyncRepository {
        val prefs = SafeReadyPreferences(context)
        val userRepository = UserRepository(prefs)
        
        return SyncRepository(
            cloudProgressRepository,
            userRepository,
            prefs,
        )
    }
}

