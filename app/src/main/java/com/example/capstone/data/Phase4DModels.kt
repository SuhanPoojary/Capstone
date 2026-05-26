package com.example.capstone.data

import com.example.capstone.data.repository.OfflineAction

/**
 * Phase 4D: Offline Write Queue Models
 * 
 * These models support queuing writes when offline and syncing when online.
 */

/**
 * Represents a write operation that needs to be synced to the server.
 * This allows the app to function offline and sync when the network is available.
 */
data class QueuedWrite(
    val id: String,
    val userId: String,
    val action: OfflineAction,  // What operation this represents
    val targetCollection: String,  // e.g., "progress", "users", "quizResults"
    val targetDocument: String,    // e.g., userId, lessonId
    val data: Map<String, Any>,    // The actual data to write
    val timestamp: Long = System.currentTimeMillis(),
    val retryCount: Int = 0,
    val lastRetryTime: Long? = null,
    val status: WriteStatus = WriteStatus.PENDING,
    val errorMessage: String? = null
)

enum class WriteStatus {
    PENDING,        // Waiting to be synced
    IN_PROGRESS,    // Currently syncing
    SUCCESS,        // Successfully synced
    FAILED,         // Failed after retries
    CANCELLED       // User cancelled the operation
}

/**
 * Represents the synchronization state of the app.
 */
data class SyncState(
    val isSyncing: Boolean = false,
    val pendingWriteCount: Int = 0,
    val failedWriteCount: Int = 0,
    val lastSyncTime: Long? = null,
    val nextRetryTime: Long? = null,
    val error: String? = null
)

/**
 * Represents a conflict that occurred during sync.
 * For example, user edits progress locally while another device updates the same data.
 */
data class SyncConflict(
    val conflictId: String,
    val targetCollection: String,
    val targetDocument: String,
    val localVersion: Map<String, Any>,
    val remoteVersion: Map<String, Any>,
    val timestamp: Long,
    val resolution: ConflictResolution = ConflictResolution.UNRESOLVED,
    val resolvedData: Map<String, Any>? = null
)

enum class ConflictResolution {
    UNRESOLVED,
    PREFER_LOCAL,
    PREFER_REMOTE,
    MERGED,
    CANCELLED
}

/**
 * Statistics about the offline write queue.
 */
data class OfflineQueueStats(
    val totalQueuedWrites: Int,
    val pendingWrites: Int,
    val failedWrites: Int,
    val successfulWrites: Int,
    val averageSyncTime: Long,
    val largestQueueSize: Int,
    val lastClearedAt: Long
)

/**
 * Configuration for offline queue behavior.
 */
data class OfflineQueueConfig(
    val maxRetries: Int = 3,
    val retryDelayMs: Long = 5000,  // Initial delay before retry
    val maxBackoffMs: Long = 300000,  // Max delay (5 minutes)
    val backoffMultiplier: Float = 2.0f,  // Exponential backoff multiplier
    val maxQueueSize: Int = 1000,  // Max items in queue before warnings
    val autoSync: Boolean = true,  // Automatically sync when network available
    val deleteSuccessfulAfterDays: Int = 30  // Clean up old successful writes
)

/**
 * Represents a batch of writes to be synced together.
 */
data class WriteBatch(
    val batchId: String,
    val userId: String,
    val writes: List<QueuedWrite>,
    val createdAt: Long = System.currentTimeMillis(),
    val sendAt: Long = System.currentTimeMillis(),
    val status: BatchStatus = BatchStatus.PENDING
)

enum class BatchStatus {
    PENDING,
    SENDING,
    SENT,
    ACKNOWLEDGED,
    FAILED,
    PARTIALLY_FAILED
}

/**
 * Represents a sync checkpoint - a point where we know all data up to this point has been synced.
 */
data class SyncCheckpoint(
    val userId: String,
    val collection: String,
    val lastSyncedTimestamp: Long,
    val lastSyncedDocumentId: String? = null,
    val totalDocumentsInCollection: Int = 0
)

