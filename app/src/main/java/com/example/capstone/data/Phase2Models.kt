package com.example.capstone.data

data class RecommendationCard(
    val title: String,
    val reason: String,
    val ctaLabel: String,
    val disasterKey: String,
    val chapterIndex: Int,
)

data class Badge(
    val id: String,
    val title: String,
    val description: String,
)

data class GamificationSummary(
    val points: Int,
    val level: Int,
    val currentStreak: Int,
    val bestStreak: Int,
    val badges: List<Badge>,
    val progressText: String,
    val lessonsCompleted: Int = 0,
    val quizzesCompleted: Int = 0,
    val simulationsCompleted: Int = 0,
)

data class AssistantReply(
    val answer: String,
    val suggestedTopic: String? = null,
    val suggestedModuleKey: String? = null,
    val suggestedChapterIndex: Int? = null,
    val followUpPrompts: List<String> = emptyList(),
)

data class EmergencyContact(
    val id: String,
    val name: String,
    val phone: String,
    val relation: String? = null,
)

