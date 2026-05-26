package com.example.capstone.data.repository

import androidx.lifecycle.LiveData
import com.example.capstone.data.*

/**
 * Enum representing the types of offline actions that can be queued.
 * 
 * Phase 4D: Offline Write Queue
 */
enum class OfflineAction {
    CREATE,
    UPDATE,
    DELETE,
    MERGE,
    BATCH_UPDATE
}

/**
 * Repository for managing offline write queues.
 * Allows the app to function offline and sync when network is available.
 * 
 * Phase 4D: Offline Write Queue
 */
interface OfflineQueueRepository {
    /**
     * Enqueue a write operation for later sync.
     */
    suspend fun enqueueWrite(write: QueuedWrite): Result<String>  // Returns write ID
    
    /**
     * Enqueue multiple writes as a batch.
     */
    suspend fun enqueueBatch(writes: List<QueuedWrite>): Result<String>  // Returns batch ID
    
    /**
     * Get all pending writes for a user.
     */
    suspend fun getPendingWrites(userId: String): Result<List<QueuedWrite>>
    
    /**
     * Get failed writes that need attention.
     */
    suspend fun getFailedWrites(userId: String): Result<List<QueuedWrite>>
    
    /**
     * Get a specific write by ID.
     */
    suspend fun getWrite(writeId: String): Result<QueuedWrite?>
    
    /**
     * Listen to pending writes in real-time.
     */
    fun observePendingWrites(userId: String): LiveData<List<QueuedWrite>>
    
    /**
     * Remove a write from the queue (e.g., if user cancels).
     */
    suspend fun removeWrite(writeId: String): Result<Unit>
    
    /**
     * Clear all writes for a user (dangerous - use with caution).
     */
    suspend fun clearAllWrites(userId: String): Result<Unit>
    
    /**
     * Get statistics about the queue.
     */
    suspend fun getQueueStats(userId: String): Result<OfflineQueueStats>
}

/**
 * Repository for syncing offline writes to the server.
 * 
 * Phase 4D: Offline Write Queue
 */
interface SyncRepository {
    /**
     * Manually trigger sync of all pending writes.
     */
    suspend fun syncPendingWrites(userId: String): Result<SyncState>
    
    /**
     * Sync writes in batches (more efficient for large queues).
     */
    suspend fun syncWritesBatch(userId: String, batchSize: Int = 25): Result<SyncState>
    
    /**
     * Get current sync state.
     */
    fun getSyncState(userId: String): LiveData<SyncState>
    
    /**
     * Resume sync (if it was paused).
     */
    suspend fun resumeSync(userId: String): Result<Unit>
    
    /**
     * Pause sync temporarily.
     */
    suspend fun pauseSync(userId: String): Result<Unit>
    
    /**
     * Check if device is online (network connectivity).
     */
    fun isDeviceOnline(): Boolean
    
    /**
     * Listen to online status changes.
     */
    fun observeOnlineStatus(): LiveData<Boolean>
}

/**
 * Repository for managing sync conflicts.
 * 
 * Phase 4D: Offline Write Queue
 */
interface ConflictRepository {
    /**
     * Get unresolved conflicts for a user.
     */
    suspend fun getUnresolvedConflicts(userId: String): Result<List<SyncConflict>>
    
    /**
     * Resolve a conflict with a strategy.
     */
    suspend fun resolveConflict(
        conflictId: String,
        resolution: ConflictResolution,
        mergedData: Map<String, Any>? = null
    ): Result<Unit>
    
    /**
     * Listen to new conflicts in real-time.
     */
    fun observeConflicts(userId: String): LiveData<List<SyncConflict>>
    
    /**
     * Auto-resolve conflicts using a merge strategy (latest timestamp wins, etc).
     */
    suspend fun autoResolveConflicts(userId: String): Result<Int>  // Returns count of resolved
}

/**
 * Repository for managing sync checkpoints.
 * Used to track progress when syncing large datasets.
 * 
 * Phase 4D: Offline Write Queue
 */
interface CheckpointRepository {
    /**
     * Save a checkpoint marking data as synced up to this point.
     */
    suspend fun saveCheckpoint(checkpoint: SyncCheckpoint): Result<Unit>
    
    /**
     * Get the last checkpoint for a collection.
     */
    suspend fun getCheckpoint(
        userId: String,
        collection: String
    ): Result<SyncCheckpoint?>
    
    /**
     * Get all checkpoints for a user.
     */
    suspend fun getUserCheckpoints(userId: String): Result<List<SyncCheckpoint>>
    
    /**
     * Reset a checkpoint (start sync from beginning).
     */
    suspend fun resetCheckpoint(userId: String, collection: String): Result<Unit>
}

/**
 * Combined offline sync worker that orchestrates the queue, sync, and conflict resolution.
 * 
 * Phase 4D: Offline Write Queue
 */
interface OfflineSyncWorker {
    /**
     * Start the sync worker (usually called on app init).
     */
    suspend fun start(userId: String)
    
    /**
     * Stop the sync worker.
     */
    suspend fun stop()
    
    /**
     * Enqueue a write and let the worker handle syncing automatically.
     */
    suspend fun enqueueAndSync(write: QueuedWrite): Result<String>
    
    /**
     * Get the current worker state.
     */
    fun getWorkerState(): LiveData<SyncState>
}

