package com.example.capstone.data

data class UserProfile(
    val uid: String = "",
    val username: String = "",
    val name: String = "",
    val email: String = "",
    val institution: String = "",
    val city: String? = null,
    val state: String? = null,
    val createdAt: Long = 0L,
    val lastLogin: Long = 0L,
    val profileCompleted: Boolean = false
)

sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Loading : AuthResult()
}

data class LessonChapter(
    val index: Int,
    val title: String,
    val phase: String,
)

data class DisasterModule(
    val key: String,
    val title: String,
    val summary: String,
    val regionTags: List<String>,
    val chapters: List<LessonChapter>,
)

data class DisasterProgress(
    val key: String,
    val title: String,
    val completedChapters: Int,
    val totalChapters: Int,
) {
    val percent: Int
        get() = if (totalChapters <= 0) 0 else ((completedChapters.toFloat() / totalChapters) * 100).toInt()
}

data class ProgressSnapshot(
    val overallPercent: Int,
    val disasterProgress: List<DisasterProgress>,
)

data class QuizQuestion(
    val disasterKey: String,
    val chapterIndex: Int,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
)

data class QuizResult(
    val score: Int,
    val total: Int,
    val passed: Boolean,
    val message: String,
)

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
)

data class MedReadyItem(
    val name: String,
    val status: String, // "Detected", "Missing", "Expired"
    val expiryDate: String? = null,
    val isEssential: Boolean = true
)

data class MedReadyScanResult(
    val id: String = "",
    val timestamp: Long = 0L,
    val itemCount: Int = 0,
    val readinessScore: Int = 0,
    val warnings: Int = 0,
    val items: List<MedReadyItem> = emptyList(),
    val summary: String = ""
)

data class Shelter(
    val state: String,
    val district: String,
    val name: String,
    val disasterType: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val capacity: Int,
    val actualInmates: Int,
    val campType: String,
    val yearEvent: String,
    val source: String
)
