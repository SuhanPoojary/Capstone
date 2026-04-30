package com.example.capstone.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.capstone.data.AssistantRepository
import com.example.capstone.data.AssistantReply
import com.example.capstone.data.GamificationRepository
import com.example.capstone.data.DisasterModule
import com.example.capstone.data.DisasterProgress
import com.example.capstone.data.LessonRepository
import com.example.capstone.data.RecommendationCard
import com.example.capstone.data.RecommendationRepository
import com.example.capstone.data.LocationRepository
import com.example.capstone.data.ProgressRepository
import com.example.capstone.data.GamificationSummary
import com.example.capstone.data.ProgressSnapshot
import com.example.capstone.data.QuizQuestion
import com.example.capstone.data.QuizRepository
import com.example.capstone.data.QuizResult
import com.example.capstone.data.SafeReadyPreferences
import com.example.capstone.data.UserProfile
import com.example.capstone.data.UserRepository

data class HomeState(
    val user: UserProfile = UserProfile(),
    val regionLabel: String = "📍 Location unavailable",
    val overallProgress: Int = 0,
    val recommendedModule: DisasterModule? = null,
    val recommendation: RecommendationCard? = null,
    val gamification: GamificationSummary = GamificationSummary(0, 1, 0, 0, emptyList(), "0 lessons completed"),
    val progressSnapshot: ProgressSnapshot = ProgressSnapshot(0, emptyList()),
)

data class TrainingState(
    val modules: List<DisasterModule> = emptyList(),
)

data class ProgressState(
    val snapshot: ProgressSnapshot = ProgressSnapshot(0, emptyList()),
    val gamification: GamificationSummary = GamificationSummary(0, 1, 0, 0, emptyList(), "0 lessons completed"),
)

data class AssistantState(
    val messages: List<com.example.capstone.data.ChatMessage> = emptyList(),
    val prompts: List<String> = emptyList(),
    val suggestedTopic: String? = null,
    val followUpPrompts: List<String> = emptyList(),
)

data class ProfileState(
    val profile: UserProfile = UserProfile(),
    val completedDisasters: List<DisasterProgress> = emptyList(),
    val gamification: GamificationSummary = GamificationSummary(0, 1, 0, 0, emptyList(), "0 lessons completed"),
)

data class QuizState(
    val question: QuizQuestion? = null,
    val result: QuizResult? = null,
    val selectedIndex: Int? = null,
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = SafeReadyPreferences(application)
    private val userRepository = UserRepository(prefs)
    private val lessonRepository = LessonRepository()
    private val progressRepository = ProgressRepository(prefs, lessonRepository)
    private val gamificationRepository = GamificationRepository(prefs, progressRepository)
    private val recommendationRepository = RecommendationRepository(lessonRepository, progressRepository)
    private val locationRepository = LocationRepository(application.applicationContext)

    val state = MutableLiveData<HomeState>()

    init {
        refresh()
        resolveLocation()
    }

    fun refresh() {
        val user = userRepository.getProfile()
        val snapshot = progressRepository.getAllProgress()
        val overall = progressRepository.getOverallProgress()
        val recommended = progressRepository.getRecommendedModule(user.state ?: user.city, progressRepository.getCompletionHint())
        val gamification = gamificationRepository.getSummary()
        val recommendation = recommendationRepository.buildRecommendation(user, gamification)
        state.value = HomeState(
            user = user,
            regionLabel = formatRegion(user.city, user.state),
            overallProgress = overall,
            recommendedModule = recommended,
            recommendation = recommendation,
            gamification = gamification,
            progressSnapshot = ProgressSnapshot(overall, snapshot),
        )
    }

    fun resolveLocation() {
        locationRepository.resolveState { stateName ->
            if (!stateName.isNullOrBlank()) {
                val profile = userRepository.getProfile()
                userRepository.saveRegion(profile.city, stateName)
                refresh()
            }
        }
    }

    private fun formatRegion(city: String?, state: String?): String {
        return when {
            !city.isNullOrBlank() && !state.isNullOrBlank() -> "📍 $city, $state"
            !state.isNullOrBlank() -> "📍 $state"
            !city.isNullOrBlank() -> "📍 $city"
            else -> "📍 Location unavailable"
        }
    }
}

class TrainingViewModel(application: Application) : AndroidViewModel(application) {
    private val lessonRepository = LessonRepository()
    val state = MutableLiveData(TrainingState(lessonRepository.getModules()))

    fun refresh() {
        state.value = TrainingState(lessonRepository.getModules())
    }
}

class ProgressViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = SafeReadyPreferences(application)
    private val lessonRepository = LessonRepository()
    private val progressRepository = ProgressRepository(prefs, lessonRepository)
    private val gamificationRepository = GamificationRepository(prefs, progressRepository)

    val state = MutableLiveData<ProgressState>()

    init {
        refresh()
    }

    fun refresh() {
        state.value = ProgressState(
            snapshot = ProgressSnapshot(
                overallPercent = progressRepository.getOverallProgress(),
                disasterProgress = progressRepository.getAllProgress(),
            ),
            gamification = gamificationRepository.getSummary(),
        )
    }

    fun markChapterCompleted(disasterKey: String, chapterIndex: Int) {
        if (progressRepository.markChapterCompleted(disasterKey, chapterIndex)) {
            gamificationRepository.recordLessonCompletion(disasterKey, chapterIndex)
        }
        refresh()
    }

    fun saveQuizResult(disasterKey: String, chapterIndex: Int, result: QuizResult) {
        progressRepository.saveQuizScore(disasterKey, chapterIndex, result.score, result.total)
        gamificationRepository.recordQuizAttempt(result.passed)
        refresh()
    }

    fun clearProgress() {
        progressRepository.clearProgress()
        gamificationRepository.reset()
        refresh()
    }
}

class AssistantViewModel(application: Application) : AndroidViewModel(application) {
    private val assistantRepository = AssistantRepository()
    val state = MutableLiveData(
        AssistantState(
            messages = listOf(
                com.example.capstone.data.ChatMessage(
                    text = "Hi! Ask me about earthquakes, floods, cyclones, landslides, evacuation, or emergency kits.",
                    isUser = false,
                )
            ),
            prompts = assistantRepository.quickPrompts(),
        )
    )

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return

        val reply: AssistantReply = assistantRepository.respondWithContext(trimmed)

        val current = state.value?.messages.orEmpty().toMutableList()
        current += com.example.capstone.data.ChatMessage(trimmed, true)
        current += com.example.capstone.data.ChatMessage(reply.answer, false)

        state.value = state.value?.copy(
            messages = current,
            prompts = reply.followUpPrompts.ifEmpty { assistantRepository.quickPrompts() },
            suggestedTopic = reply.suggestedTopic,
            followUpPrompts = reply.followUpPrompts,
        ) ?: AssistantState(
            messages = current,
            prompts = reply.followUpPrompts.ifEmpty { assistantRepository.quickPrompts() },
            suggestedTopic = reply.suggestedTopic,
            followUpPrompts = reply.followUpPrompts,
        )
    }
}

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = SafeReadyPreferences(application)
    private val userRepository = UserRepository(prefs)
    private val lessonRepository = LessonRepository()
    private val progressRepository = ProgressRepository(prefs, lessonRepository)
    private val gamificationRepository = GamificationRepository(prefs, progressRepository)

    val state = MutableLiveData<ProfileState>()

    init {
        refresh()
    }

    fun refresh() {
        state.value = ProfileState(
            profile = userRepository.getProfile(),
            completedDisasters = progressRepository.getAllProgress(),
            gamification = gamificationRepository.getSummary(),
        )
    }

    fun resetAllData() {
        userRepository.clearProfile()
        progressRepository.clearProgress()
        gamificationRepository.reset()
        refresh()
    }
}

class QuizViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = SafeReadyPreferences(application)
    private val lessonRepository = LessonRepository()
    private val progressRepository = ProgressRepository(prefs, lessonRepository)
    private val gamificationRepository = GamificationRepository(prefs, progressRepository)
    private val quizRepository = QuizRepository(lessonRepository)

    val state = MutableLiveData(QuizState())

    fun loadQuestion(disasterKey: String, chapterIndex: Int) {
        state.value = QuizState(question = quizRepository.getQuizForChapter(disasterKey, chapterIndex))
    }

    fun submitAnswer(selectedIndex: Int) {
        val question = state.value?.question ?: return
        val result = quizRepository.evaluate(question, selectedIndex)
        progressRepository.saveQuizScore(question.disasterKey, question.chapterIndex, result.score, result.total)
        gamificationRepository.recordQuizAttempt(result.passed)
        state.value = state.value?.copy(result = result, selectedIndex = selectedIndex)
            ?: QuizState(question = question, result = result, selectedIndex = selectedIndex)
    }

    fun recordCompletion(disasterKey: String, chapterIndex: Int) {
        if (progressRepository.markChapterCompleted(disasterKey, chapterIndex)) {
            gamificationRepository.recordLessonCompletion(disasterKey, chapterIndex)
        }
    }
}

