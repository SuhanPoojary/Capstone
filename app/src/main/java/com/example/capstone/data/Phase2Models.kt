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
)

data class AssistantReply(
    val answer: String,
    val suggestedTopic: String? = null,
    val suggestedModuleKey: String? = null,
    val suggestedChapterIndex: Int? = null,
    val followUpPrompts: List<String> = emptyList(),
)

