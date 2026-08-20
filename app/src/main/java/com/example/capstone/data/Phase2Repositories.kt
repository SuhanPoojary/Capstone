package com.example.capstone.data

import java.time.LocalDate
import java.time.ZoneId

class RecommendationRepository(
    private val lessonRepository: LessonRepository,
    private val progressRepository: ProgressRepository,
) {
    fun buildRecommendation(profile: UserProfile, gamification: GamificationSummary): RecommendationCard {
        val region = profile.state ?: profile.city
        val regionMatch = lessonRepository.getRecommendedModule(region, progressRepository.getCompletionHint())
        val module = if (regionMatch.key.isNotBlank()) regionMatch else lessonRepository.getModules().first()
        val nextChapterIndex = progressRepository.getNextIncompleteChapterIndex(module.key)
        val progress = progressRepository.getDisasterProgress(module.key)

        val reason = when {
            !region.isNullOrBlank() -> "Recommended for your region and current readiness level."
            progress.completedChapters > 0 -> "Continue where you left off to keep your learning streak active."
            else -> "Start with a core lesson to build your preparedness foundation."
        }

        val cta = when {
            progress.completedChapters == 0 -> "Start Lesson"
            progress.completedChapters < progress.totalChapters -> "Continue Chapter ${nextChapterIndex + 1}"
            else -> "Review Lesson"
        }

        return RecommendationCard(
            title = module.title,
            reason = "$reason ${gamification.progressText}",
            ctaLabel = cta,
            disasterKey = module.key,
            chapterIndex = nextChapterIndex,
        )
    }
}

class GamificationRepository(
    private val prefs: SafeReadyPreferences,
    private val progressRepository: ProgressRepository,
) {
    fun recordLessonCompletion(disasterKey: String, chapterIndex: Int) {
        prefs.incrementPoints(20)
        updateStreak()
    }

    fun recordQuizSuccess() {
        prefs.incrementPoints(10)
    }

    fun recordQuizAttempt(correct: Boolean) {
        if (correct) recordQuizSuccess()
    }

    fun getSummary(): GamificationSummary {
        val points = prefs.getPoints()
        val currentStreak = prefs.getCurrentStreak()
        val bestStreak = prefs.getBestStreak()
        val level = (points / 50) + 1
        val completedChapters = progressRepository.getTotalCompletedChapters()
        val snapshots = progressRepository.getAllProgress()
        val badges = buildBadges(points, currentStreak, completedChapters, snapshots)

        // Mocking quiz and simulation counts based on points/progress for now
        val quizzes = points / 100
        val simulations = snapshots.count { it.percent >= 100 }

        return GamificationSummary(
            points = points,
            level = level,
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            badges = badges,
            progressText = "${completedChapters} lessons completed • Level $level • ${currentStreak}-day streak",
            lessonsCompleted = completedChapters,
            quizzesCompleted = quizzes,
            simulationsCompleted = simulations
        )
    }

    fun reset() {
        prefs.setPoints(0)
        prefs.setCurrentStreak(0)
        prefs.setBestStreak(0)
        prefs.setLastCompletionDay(0L)
    }

    private fun updateStreak() {
        val today = LocalDate.now(ZoneId.systemDefault()).toEpochDay()
        val last = prefs.getLastCompletionDay()
        val current = prefs.getCurrentStreak()
        val updated = when {
            last == 0L -> 1
            last == today -> current
            last == today - 1 -> current + 1
            else -> 1
        }
        prefs.setCurrentStreak(updated)
        prefs.setBestStreak(maxOf(updated, prefs.getBestStreak()))
        prefs.setLastCompletionDay(today)
    }

    private fun buildBadges(
        points: Int,
        streak: Int,
        completedChapters: Int,
        progress: List<DisasterProgress>,
    ): List<Badge> {
        val badges = mutableListOf<Badge>()

        if (completedChapters >= 1) {
            badges += Badge(
                id = "first_step",
                title = "First Step",
                description = "Completed your first lesson."
            )
        }
        if (completedChapters >= 3) {
            badges += Badge(
                id = "prepared_learner",
                title = "Prepared Learner",
                description = "Completed at least three chapters."
            )
        }
        if (progress.any { it.percent >= 100 }) {
            badges += Badge(
                id = "disaster_defender",
                title = "Disaster Defender",
                description = "Finished one disaster module."
            )
        }
        if (streak >= 3) {
            badges += Badge(
                id = "consistent_learner",
                title = "Consistent Learner",
                description = "Maintained a 3-day learning streak."
            )
        }
        if (points >= 100) {
            badges += Badge(
                id = "prepared_champion",
                title = "Prepared Champion",
                description = "Reached 100 preparedness points."
            )
        }

        return badges
    }
}


