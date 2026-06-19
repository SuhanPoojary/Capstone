package com.example.capstone.data

import android.content.Context
import android.content.SharedPreferences
import com.example.capstone.DemoVideoRepository
import com.example.capstone.location.LocationHelper
import androidx.core.content.edit
import java.util.Locale

class SafeReadyPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveUserProfile(profile: UserProfile) {
        prefs.edit {
            putString(KEY_UID, profile.uid)
            putString(KEY_USERNAME, profile.username)
            putString(KEY_NAME, profile.name.trim())
            putString(KEY_EMAIL, profile.email.trim())
            putString(KEY_INSTITUTION, profile.institution.trim())
        }
    }

    fun saveRegion(city: String?, state: String?) {
        prefs.edit {
            putString(KEY_CITY, city?.trim())
            putString(KEY_STATE, state?.trim())
        }
    }

    fun getUserProfile(): UserProfile {
        return UserProfile(
            uid = prefs.getString(KEY_UID, "").orEmpty(),
            username = prefs.getString(KEY_USERNAME, "").orEmpty(),
            name = prefs.getString(KEY_NAME, null).orEmpty().ifBlank { "User" },
            email = prefs.getString(KEY_EMAIL, null).orEmpty(),
            institution = prefs.getString(KEY_INSTITUTION, null).orEmpty(),
            city = prefs.getString(KEY_CITY, null),
            state = prefs.getString(KEY_STATE, null),
        )
    }

    fun clearProfile() {
        prefs.edit {
            remove(KEY_UID)
            remove(KEY_USERNAME)
            remove(KEY_NAME)
            remove(KEY_EMAIL)
            remove(KEY_INSTITUTION)
            remove(KEY_CITY)
            remove(KEY_STATE)
        }
    }

    fun markChapterComplete(disasterKey: String, chapterIndex: Int): Boolean {
        val updated = getCompletedChapterSet(disasterKey).toMutableSet()
        val wasNew = updated.add(chapterIndex)
        prefs.edit {
            putStringSet(completedKey(disasterKey), updated.map { it.toString() }.toSet())
        }
        return wasNew
    }

    fun getCompletedChapterSet(disasterKey: String): Set<Int> {
        return prefs.getStringSet(completedKey(disasterKey), emptySet())
            .orEmpty()
            .mapNotNull { it.toIntOrNull() }
            .toSet()
    }

    fun saveQuizScore(disasterKey: String, chapterIndex: Int, score: Int, total: Int) {
        prefs.edit {
            putString(quizKey(disasterKey, chapterIndex), "$score/$total")
        }
    }

    fun getQuizScore(disasterKey: String, chapterIndex: Int): String? {
        return prefs.getString(quizKey(disasterKey, chapterIndex), null)
    }

    fun getCompletedChapterCount(disasterKey: String): Int = getCompletedChapterSet(disasterKey).size

    fun getTotalCompletedChapters(): Int {
        return prefs.all.keys
            .filter { it.startsWith(KEY_PROGRESS_PREFIX) }
            .sumOf { getCompletedChapterSet(it.removePrefix(KEY_PROGRESS_PREFIX)).size }
    }

    fun getPoints(): Int = prefs.getInt(KEY_POINTS, 0)

    fun setPoints(points: Int) {
        prefs.edit { putInt(KEY_POINTS, points.coerceAtLeast(0)) }
    }

    fun incrementPoints(delta: Int) {
        setPoints(getPoints() + delta)
    }

    fun getCurrentStreak(): Int = prefs.getInt(KEY_CURRENT_STREAK, 0)

    fun setCurrentStreak(value: Int) {
        prefs.edit { putInt(KEY_CURRENT_STREAK, value.coerceAtLeast(0)) }
    }

    fun getBestStreak(): Int = prefs.getInt(KEY_BEST_STREAK, 0)

    fun setBestStreak(value: Int) {
        prefs.edit { putInt(KEY_BEST_STREAK, value.coerceAtLeast(0)) }
    }

    fun getLastCompletionDay(): Long = prefs.getLong(KEY_LAST_COMPLETION_DAY, 0L)

    fun setLastCompletionDay(day: Long) {
        prefs.edit { putLong(KEY_LAST_COMPLETION_DAY, day) }
    }

    fun clearProgress() {
        prefs.all.keys
            .filter { key -> key.startsWith(KEY_PROGRESS_PREFIX) || key.startsWith(KEY_QUIZ_PREFIX) }
            .forEach { key -> prefs.edit { remove(key) } }
        prefs.edit {
            remove(KEY_POINTS)
            remove(KEY_CURRENT_STREAK)
            remove(KEY_BEST_STREAK)
            remove(KEY_LAST_COMPLETION_DAY)
        }
    }

    fun getEmergencyModeEnabled(): Boolean = prefs.getBoolean(KEY_EMERGENCY_MODE_ENABLED, false)

    fun setEmergencyModeEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_EMERGENCY_MODE_ENABLED, enabled) }
    }

    private fun completedKey(disasterKey: String) = "$KEY_PROGRESS_PREFIX$disasterKey"
    private fun quizKey(disasterKey: String, chapterIndex: Int) = "$KEY_QUIZ_PREFIX${disasterKey}_$chapterIndex"

    companion object {
        private const val PREFS_NAME = "safeready_prefs"
        private const val KEY_UID = "user_uid"
        private const val KEY_USERNAME = "user_username"
        private const val KEY_NAME = "user_name"
        private const val KEY_EMAIL = "user_email"
        private const val KEY_INSTITUTION = "user_institution"
        private const val KEY_CITY = "user_city"
        private const val KEY_STATE = "user_state"
        private const val KEY_PROGRESS_PREFIX = "progress_"
        private const val KEY_QUIZ_PREFIX = "quiz_"
        private const val KEY_POINTS = "gamification_points"
        private const val KEY_CURRENT_STREAK = "gamification_current_streak"
        private const val KEY_BEST_STREAK = "gamification_best_streak"
        private const val KEY_LAST_COMPLETION_DAY = "gamification_last_completion_day"
        private const val KEY_EMERGENCY_MODE_ENABLED = "emergency_mode_enabled"
    }
}

class EmergencyRepository(private val prefs: SafeReadyPreferences) {
    fun isEmergencyModeEnabled(): Boolean = prefs.getEmergencyModeEnabled()

    fun setEmergencyModeEnabled(enabled: Boolean) {
        prefs.setEmergencyModeEnabled(enabled)
    }

    fun toggleEmergencyMode(): Boolean {
        val updated = !isEmergencyModeEnabled()
        setEmergencyModeEnabled(updated)
        return updated
    }
}

class UserRepository(private val prefs: SafeReadyPreferences) {
    fun saveUserProfile(profile: UserProfile) = prefs.saveUserProfile(profile)

    fun getProfile(): UserProfile = prefs.getUserProfile()

    fun saveRegion(city: String?, state: String?) = prefs.saveRegion(city, state)

    fun clearProfile() = prefs.clearProfile()
}

class LessonRepository {
    private val modules: List<DisasterModule> = listOf(
        DisasterModule(
            key = "earthquake",
            title = "Earthquake",
            summary = "Learn how to protect yourself before, during, and after seismic activity.",
            regionTags = listOf("earthquake", "seismic", "western", "plateau"),
            chapters = listOf(
                LessonChapter(0, "Understanding Seismic Waves", "general"),
                LessonChapter(1, "Drop, Cover, Hold On", "during"),
                LessonChapter(2, "Post-Quake Evacuation", "after"),
            ),
        ),
        DisasterModule(
            key = "floods",
            title = "Floods",
            summary = "Stay ready for heavy rainfall, water rise, and evacuation planning.",
            regionTags = listOf("flood", "coastal", "river", "low-lying"),
            chapters = listOf(
                LessonChapter(0, "Flood Awareness", "general"),
                LessonChapter(1, "During Flood Conditions", "during"),
                LessonChapter(2, "Recovery After Flooding", "after"),
            ),
        ),
        DisasterModule(
            key = "cyclone",
            title = "Cyclone",
            summary = "Prepare for strong winds, storm surge, and early evacuation guidance.",
            regionTags = listOf("cyclone", "coastal", "storm", "shore"),
            chapters = listOf(
                LessonChapter(0, "Cyclone Basics", "general"),
                LessonChapter(1, "When the Storm Arrives", "during"),
                LessonChapter(2, "Safe Recovery", "after"),
            ),
        ),
        DisasterModule(
            key = "landslides",
            title = "Landslides",
            summary = "Understand slope risks, warning signs, and safe evacuation practices.",
            regionTags = listOf("landslide", "hill", "mountain", "slope"),
            chapters = listOf(
                LessonChapter(0, "Slope Risk Awareness", "general"),
                LessonChapter(1, "During Landslide Conditions", "during"),
                LessonChapter(2, "After the Slide", "after"),
            ),
        ),
    )

    fun getModules(): List<DisasterModule> = modules

    fun getModule(key: String): DisasterModule = modules.first { it.key == key }

    fun getRecommendedModule(region: String?, completionHint: String? = null): DisasterModule {
        val normalizedRegion = region?.lowercase(Locale.US).orEmpty()
        val regionMatch = modules.firstOrNull { module ->
            module.regionTags.any { tag -> normalizedRegion.contains(tag) }
        }
        if (regionMatch != null) return regionMatch

        val hint = completionHint?.lowercase(Locale.US).orEmpty()
        val hintMatch = modules.firstOrNull { module -> hint.contains(module.key) || hint.contains(module.title.lowercase(Locale.US)) }
        return hintMatch ?: modules.first()
    }

    fun getCompletionPercent(completedChapterCount: Int, totalChapters: Int = 3): Int {
        if (totalChapters <= 0) return 0
        return ((completedChapterCount.toFloat() / totalChapters) * 100).toInt().coerceIn(0, 100)
    }

    fun getVideoUri(context: android.content.Context, disasterKey: String, chapterIndex: Int, languageCode: String) =
        DemoVideoRepository.getVideoUri(context, disasterKey, chapterIndex, languageCode)

    fun getAvailableLanguages(context: android.content.Context, disasterKey: String, chapterIndex: Int) =
        DemoVideoRepository.getAvailableLanguages(context, disasterKey, chapterIndex)

    fun getChapterTitle(disasterKey: String, chapterIndex: Int): String {
        val module = getModule(disasterKey)
        return module.chapters.getOrNull(chapterIndex)?.title ?: "Chapter ${chapterIndex + 1}"
    }
}

class ProgressRepository(
    private val prefs: SafeReadyPreferences,
    private val lessons: LessonRepository,
) {
    fun markChapterCompleted(disasterKey: String, chapterIndex: Int): Boolean {
        return prefs.markChapterComplete(disasterKey, chapterIndex)
    }

    fun saveQuizScore(disasterKey: String, chapterIndex: Int, score: Int, total: Int) {
        prefs.saveQuizScore(disasterKey, chapterIndex, score, total)
    }

    fun getDisasterProgress(disasterKey: String): DisasterProgress {
        val module = lessons.getModule(disasterKey)
        val completed = prefs.getCompletedChapterCount(disasterKey)
        return DisasterProgress(module.key, module.title, completed, module.chapters.size)
    }

    fun getAllProgress(): List<DisasterProgress> = lessons.getModules().map { getDisasterProgress(it.key) }

    fun getCompletedChapterSet(disasterKey: String): Set<Int> {
        return prefs.getCompletedChapterSet(disasterKey)
    }

    fun getOverallProgress(): Int {
        val snapshot = getAllProgress()
        if (snapshot.isEmpty()) return 0
        return snapshot.sumOf { it.percent } / snapshot.size
    }

    fun getRecommendedModule(region: String?, completionHint: String? = null): DisasterModule {
        return lessons.getRecommendedModule(region, completionHint)
    }

    fun getCompletionHint(): String {
        val progress = getAllProgress()
        val incomplete = progress.firstOrNull { it.completedChapters < it.totalChapters }
        return incomplete?.key ?: progress.firstOrNull()?.key.orEmpty()
    }

    fun getNextIncompleteChapterIndex(disasterKey: String): Int {
        val completed = prefs.getCompletedChapterSet(disasterKey)
        return (0..2).firstOrNull { it !in completed } ?: 0
    }

    fun getTotalCompletedChapters(): Int = prefs.getTotalCompletedChapters()

    fun clearProgress() = prefs.clearProgress()
}

class QuizRepository(private val lessons: LessonRepository) {
    fun getQuestion(disasterKey: String, chapterIndex: Int): QuizQuestion {
        return when (disasterKey.lowercase(Locale.US)) {
            "earthquake" -> when (chapterIndex) {
                0 -> QuizQuestion(
                    disasterKey,
                    chapterIndex,
                    "What should you do during shaking?",
                    listOf("Run outside immediately", "Drop, Cover, Hold On", "Use the elevator", "Stand near windows"),
                    1,
                    "Drop, Cover, and Hold On is the safest immediate action during shaking."
                )
                1 -> QuizQuestion(
                    disasterKey,
                    chapterIndex,
                    "Which item should be secured before an earthquake?",
                    listOf("Loose furniture", "Shoes", "Water bottle", "Phone charger"),
                    0,
                    "Heavy or loose furniture should be anchored before a quake."
                )
                else -> QuizQuestion(
                    disasterKey,
                    chapterIndex,
                    "After an earthquake, what is important to check first?",
                    listOf("Social media", "Gas leaks and injuries", "Television signal", "Car wash stations"),
                    1,
                    "Check for injuries and hazards such as gas leaks before moving around."
                )
            }
            "floods" -> when (chapterIndex) {
                0 -> QuizQuestion(disasterKey, chapterIndex, "What should you prepare for a flood?", listOf("Sandbags and emergency kit", "A bicycle only", "Fireworks", "A new TV"), 0, "Flood readiness starts with supplies and protection planning.")
                1 -> QuizQuestion(disasterKey, chapterIndex, "During floodwater rise, what is best?", listOf("Drive through water", "Move to higher ground", "Touch electrical sockets", "Open all windows"), 1, "Higher ground is safer during rising water.")
                else -> QuizQuestion(disasterKey, chapterIndex, "After flooding, what should you avoid?", listOf("Cleaning safely", "Checking water safety", "Walking through standing water", "Following official updates"), 2, "Standing water may contain hidden hazards and contamination.")
            }
            "cyclone" -> when (chapterIndex) {
                0 -> QuizQuestion(disasterKey, chapterIndex, "What is a cyclone usually associated with?", listOf("Strong winds and heavy rain", "Snow only", "Earth tremors", "Clear skies"), 0, "Cyclones typically bring strong winds and heavy rainfall.")
                1 -> QuizQuestion(disasterKey, chapterIndex, "If evacuation is advised, what should you do?", listOf("Wait until winds stop", "Follow the official evacuation order", "Go to the beach", "Turn off the radio"), 1, "Early evacuation is safer than waiting too long.")
                else -> QuizQuestion(disasterKey, chapterIndex, "What should you check after a cyclone?", listOf("Roof damage and utilities", "Movie schedules", "Shopping lists", "School attendance"), 0, "Inspect the home carefully and avoid damaged utilities.")
            }
            else -> when (chapterIndex) {
                0 -> QuizQuestion(disasterKey, chapterIndex, "What increases landslide risk?", listOf("Heavy rain", "Quiet weather", "Cold drinks", "Long phone calls"), 0, "Heavy rain can destabilize slopes and trigger slides.")
                1 -> QuizQuestion(disasterKey, chapterIndex, "During a landslide warning, what is safest?", listOf("Move away from slopes", "Stand near steep edges", "Hide under loose rocks", "Drive uphill slowly"), 0, "Move away from the slide path and unstable terrain.")
                else -> QuizQuestion(disasterKey, chapterIndex, "After a landslide, what should you do?", listOf("Return immediately", "Follow authorities and avoid the area", "Ignore road blocks", "Start digging alone"), 1, "The area may still be unstable after the initial slide.")
            }
        }
    }

    fun evaluate(question: QuizQuestion, selectedIndex: Int): QuizResult {
        val correct = selectedIndex == question.correctIndex
        return QuizResult(
            score = if (correct) 1 else 0,
            total = 1,
            passed = correct,
            message = if (correct) question.explanation else "Not quite. ${question.explanation}"
        )
    }

    fun getQuizForChapter(disasterKey: String, chapterIndex: Int) = getQuestion(disasterKey, chapterIndex)
}

class AssistantRepository {
    fun quickPrompts(): List<String> = listOf(
        "What should I do during an earthquake?",
        "How do I prepare for a flood?",
        "When should I evacuate during a cyclone?",
        "What are landslide warning signs?"
    )

    fun respond(input: String): String {
        return respondWithContext(input).answer
    }

    fun respondWithContext(input: String): com.example.capstone.data.AssistantReply {
        val text = input.lowercase(Locale.US)
        return when {
            text.contains("earthquake") || text.contains("shake") -> com.example.capstone.data.AssistantReply(
                answer = "During shaking, Drop, Cover, and Hold On. Stay away from windows and unsecured furniture.",
                suggestedTopic = "Earthquake lesson",
                suggestedModuleKey = "earthquake",
                suggestedChapterIndex = 1,
                followUpPrompts = listOf("How do I secure furniture?", "What should I do after shaking stops?")
            )
            text.contains("flood") || text.contains("water") -> com.example.capstone.data.AssistantReply(
                answer = "Move to higher ground, avoid walking or driving through floodwater, and keep an emergency kit ready.",
                suggestedTopic = "Flood lesson",
                suggestedModuleKey = "floods",
                suggestedChapterIndex = 1,
                followUpPrompts = listOf("What should I pack for floods?", "How do I stay safe at night?")
            )
            text.contains("cyclone") || text.contains("storm") || text.contains("wind") -> com.example.capstone.data.AssistantReply(
                answer = "Follow official alerts, secure loose items, and evacuate early if authorities advise it.",
                suggestedTopic = "Cyclone lesson",
                suggestedModuleKey = "cyclone",
                suggestedChapterIndex = 0,
                followUpPrompts = listOf("How do I prepare my home?", "When should I evacuate?")
            )
            text.contains("landslide") || text.contains("hill") || text.contains("slope") -> com.example.capstone.data.AssistantReply(
                answer = "Watch for cracks, heavy rain, and movement on slopes. Move away from unstable areas immediately.",
                suggestedTopic = "Landslide lesson",
                suggestedModuleKey = "landslides",
                suggestedChapterIndex = 0,
                followUpPrompts = listOf("What are warning signs?", "Should I evacuate before the slide?")
            )
            text.contains("kit") || text.contains("prepare") -> com.example.capstone.data.AssistantReply(
                answer = "Keep water, food, first aid, flashlight, medicine, and documents in a ready-to-go bag.",
                suggestedTopic = "Emergency kit checklist",
                followUpPrompts = listOf("What documents should I store?", "How often should I check my kit?")
            )
            else -> com.example.capstone.data.AssistantReply(
                answer = "I can help with earthquake, flood, cyclone, landslide, evacuation, and emergency kit questions.",
                suggestedTopic = "Try asking about a disaster type",
                followUpPrompts = quickPrompts()
            )
        }
    }
}

class LocationRepository(private val context: Context) {
    fun hasPermission(): Boolean = LocationHelper.hasLocationPermission(context)

    fun resolveState(onResult: (String?) -> Unit) {
        LocationHelper.fetchState(context) { onResult(it?.takeIf { value -> value.isNotBlank() }) }
    }

    fun resolveCity(onResult: (String?) -> Unit) {
        LocationHelper.fetchCity(context) { onResult(it?.takeIf { value -> value.isNotBlank() }) }
    }
}
