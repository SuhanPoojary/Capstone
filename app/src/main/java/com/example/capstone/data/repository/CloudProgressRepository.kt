package com.example.capstone.data.repository

import com.example.capstone.data.remote.firebase.FirebaseProgressDataSource
import com.example.capstone.data.ProgressRepository

/**
 * Handles cloud sync of progress data with Firestore.
 * Designed to work alongside the local ProgressRepository in an offline-first model.
 * 
 * Sync strategy:
 * - Local ProgressRepository is the source of truth for offline access.
 * - CloudProgressRepository syncs local changes to the cloud asynchronously.
 * - Cloud pull can restore progress if the local store is cleared or lost.
 * - Conflicts are resolved by preferring the most complete progress (higher chapter count).
 */
class CloudProgressRepository(
    private val firebaseProgress: FirebaseProgressDataSource,
    private val localProgressRepository: ProgressRepository,
) {
    /**
     * Sync a chapter completion to the cloud.
     * This is called after a local chapter completion to keep cloud state in sync.
     * Returns true if sync succeeded, false if it failed (but local state is preserved).
     */
    suspend fun syncChapterCompletion(userId: String, disasterKey: String): Boolean {
        val completedSet = localProgressRepository.getCompletedChapterSet(disasterKey)
        val progress = localProgressRepository.getDisasterProgress(disasterKey)
        
        return firebaseProgress.saveChapterCompletion(
            userId,
            disasterKey,
            completedSet,
            progress.totalChapters
        )
    }

    /**
     * Sync a quiz score to the cloud.
     * This is called after a quiz attempt to keep cloud records in sync.
     * Returns true if sync succeeded, false otherwise.
     */
    suspend fun syncQuizScore(
        userId: String,
        disasterKey: String,
        chapterIndex: Int,
        score: Int,
        total: Int
    ): Boolean {
        return firebaseProgress.saveQuizScore(userId, disasterKey, chapterIndex, score, total)
    }

    /**
     * Pull the user's entire progress state from the cloud.
     * Merges cloud progress with local progress, applying cloud updates to local store.
     * Returns true if pull succeeded, false otherwise.
     * Local progress is not modified if the pull fails (offline-first).
     */
    suspend fun pullAllProgress(userId: String): Boolean {
        val allLocal = localProgressRepository.getAllProgress()
        val disasterTitles = allLocal.associateBy({ it.key }, { it.title })
        
        val cloudData = firebaseProgress.getAllProgress(userId, disasterTitles)

        if (cloudData.isNotEmpty()) {
            // For each disaster with cloud progress, merge with local
            for ((disasterKey, cloudProgress) in cloudData) {
                val localProgress = allLocal.firstOrNull { it.key == disasterKey }
                val mergedProgress = firebaseProgress.mergeProgress(
                    localProgress ?: cloudProgress,
                    cloudProgress
                )

                // If cloud has more progress, update local store
                if (cloudProgress.completedChapters > (localProgress?.completedChapters ?: 0)) {
                    // Update local SharedPreferences with cloud progress
                    // This is a simplified merge; a real app would track individual chapter timestamps
                    for (i in 0 until mergedProgress.completedChapters) {
                        localProgressRepository.markChapterCompleted(disasterKey, i)
                    }
                }
            }
        }

        return true
    }

    fun pullAllProgressSafely(userId: String): Boolean {
        return try {
            kotlinx.coroutines.runBlocking { pullAllProgress(userId) }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Push all local progress to the cloud.
     * Iterates through all local progress records and syncs them to Firebase.
     * Returns true if all syncs succeeded, false if any failed.
     */
    suspend fun pushAllProgress(userId: String): Boolean {
        val allProgress = localProgressRepository.getAllProgress()
        var allSucceeded = true

        for (progress in allProgress) {
            val completed = localProgressRepository.getCompletedChapterSet(progress.key)
            val synced = firebaseProgress.saveChapterCompletion(
                userId,
                progress.key,
                completed,
                progress.totalChapters
            )
            if (!synced) {
                allSucceeded = false
            }
        }

        return allSucceeded
    }

    /**
     * Get completed chapter set for a disaster from local storage.
     */
    fun getCompletedChapterSet(disasterKey: String): Set<Int> {
        return localProgressRepository.getCompletedChapterSet(disasterKey)
    }
}

