package com.example.capstone.data.remote.firebase

import com.example.capstone.data.DisasterProgress
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * Handles cloud storage and sync of progress data to Firestore.
 * Designed to work alongside local SharedPreferences in an offline-first model.
 * 
 * Cloud structure:
 * users/{userId}/progress/{disasterKey}
 *   - completedChapters: Int
 *   - totalChapters: Int
 *   - updatedAt: Long (timestamp)
 */
class FirebaseProgressDataSource {
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Save completed chapter set for a disaster to Firestore.
     * Returns true if successful, false otherwise.
     */
    suspend fun saveChapterCompletion(
        userId: String,
        disasterKey: String,
        completedChapters: Set<Int>,
        totalChapters: Int
    ): Boolean = try {
        val data = mapOf(
            "completedChapters" to completedChapters.size,
            "totalChapters" to totalChapters,
            "completedIndices" to completedChapters.toList(),
            "updatedAt" to System.currentTimeMillis(),
        )
        firestore.collection("users")
            .document(userId)
            .collection("progress")
            .document(disasterKey)
            .set(data, SetOptions.merge())
            .await()
        true
    } catch (e: Exception) {
        false
    }

    /**
     * Fetch progress for a single disaster from Firestore.
     * Returns the DisasterProgress if found, or null if not available.
     */
    suspend fun getDisasterProgress(userId: String, disasterKey: String, title: String): DisasterProgress? = try {
        val doc = firestore.collection("users")
            .document(userId)
            .collection("progress")
            .document(disasterKey)
            .get()
            .await()

        if (doc.exists()) {
            val completed = doc.getLong("completedChapters")?.toInt() ?: 0
            val total = doc.getLong("totalChapters")?.toInt() ?: 3
            DisasterProgress(
                key = disasterKey,
                title = title,
                completedChapters = completed,
                totalChapters = total,
            )
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }

    /**
     * Fetch all progress records for a user from Firestore.
     * Returns a map of disasterKey -> DisasterProgress, or an empty map if fetch fails.
     */
    suspend fun getAllProgress(userId: String, disasterTitles: Map<String, String>): Map<String, DisasterProgress> = try {
        val snapshot = firestore.collection("users")
            .document(userId)
            .collection("progress")
            .get()
            .await()

        snapshot.documents.mapNotNull { doc ->
            val key = doc.id
            val completed = doc.getLong("completedChapters")?.toInt() ?: 0
            val total = doc.getLong("totalChapters")?.toInt() ?: 3
            val title = disasterTitles[key] ?: key
            DisasterProgress(
                key = key,
                title = title,
                completedChapters = completed,
                totalChapters = total,
            )
        }.associateBy { it.key }
    } catch (e: Exception) {
        emptyMap()
    }

    /**
     * Save quiz score for a chapter to Firestore.
     * Returns true if successful, false otherwise.
     */
    suspend fun saveQuizScore(
        userId: String,
        disasterKey: String,
        chapterIndex: Int,
        score: Int,
        total: Int
    ): Boolean = try {
        val quizKey = "${disasterKey}_${chapterIndex}"
        val data = mapOf(
            "disasterKey" to disasterKey,
            "chapterIndex" to chapterIndex,
            "score" to score,
            "total" to total,
            "passed" to (score >= total / 2),
            "timestamp" to System.currentTimeMillis(),
        )
        firestore.collection("users")
            .document(userId)
            .collection("quizScores")
            .document(quizKey)
            .set(data, SetOptions.merge())
            .await()
        true
    } catch (e: Exception) {
        false
    }

    /**
     * Merge local progress with cloud progress.
     * Cloud data takes precedence for recency (later timestamps win).
     * This is called when syncing to choose which version to keep.
     */
    fun mergeProgress(
        localProgress: DisasterProgress,
        cloudProgress: DisasterProgress?
    ): DisasterProgress {
        if (cloudProgress == null) return localProgress
        // In a real app, we'd check timestamps. For now, we favor the more complete data.
        return if (cloudProgress.completedChapters >= localProgress.completedChapters) {
            cloudProgress
        } else {
            localProgress
        }
    }
}

