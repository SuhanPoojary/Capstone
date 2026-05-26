package com.example.capstone.data

/**
 * Phase 4C: Analytics and Event Tracking Models
 * 
 * These models support comprehensive user behavior tracking and analytics.
 */

/**
 * Represents a single analytics event (e.g., lesson viewed, quiz completed).
 */
data class AnalyticsEvent(
    val eventId: String,
    val userId: String,
    val eventType: EventType,
    val eventName: String,  // e.g., "lesson_completed", "quiz_started"
    val category: String,   // e.g., "learning", "engagement", "quiz"
    val value: String? = null,  // e.g., lesson key, quiz score
    val properties: Map<String, String> = emptyMap(),  // Additional custom data
    val timestamp: Long = System.currentTimeMillis(),
    val sessionId: String? = null
)

enum class EventType {
    PAGE_VIEW,           // User opened a screen
    USER_ACTION,         // User tapped a button, completed an action
    CONTENT_INTERACTION, // Viewed lesson, watched video, etc.
    ASSESSMENT,          // Quiz started, completed, scored
    ERROR,               // App crash, error occurred
    ENGAGEMENT,          // User behavior indicating interest
    PERFORMANCE_METRIC   // App performance data
}

/**
 * Represents a user's learning session.
 */
data class LearningSession(
    val sessionId: String,
    val userId: String,
    val startTime: Long,
    val endTime: Long? = null,
    val durationSeconds: Int = 0,
    val disasterKey: String? = null,  // If session was disaster-focused
    val lessonsViewed: List<String> = emptyList(),
    val quizzesAttempted: Int = 0,
    val quizzesCompleted: Int = 0,
    val averageQuizScore: Float = 0f,
    val deviceInfo: String? = null
)

/**
 * Represents metrics for a specific time period.
 */
data class AnalyticsMetrics(
    val period: TimePeriod,
    val userId: String? = null,  // null for global metrics
    val totalEvents: Int,
    val uniqueUsers: Int? = null,  // Only for global metrics
    val sessionCount: Int,
    val averageSessionDuration: Int,
    val eventsByType: Map<EventType, Int> = emptyMap(),
    val topPages: List<PageMetric> = emptyList(),
    val topContentItems: List<ContentMetric> = emptyList(),
    val conversionMetrics: ConversionMetrics? = null,
    val retentionMetrics: RetentionMetrics? = null
)

enum class TimePeriod {
    HOUR, DAY, WEEK, MONTH, QUARTER, YEAR
}

/**
 * Metrics for a specific page/screen.
 */
data class PageMetric(
    val pageName: String,
    val views: Int,
    val uniqueVisitors: Int,
    val avgTimeOnPage: Int,
    val bounceRate: Float = 0f
)

/**
 * Metrics for a specific content item (lesson, quiz, etc).
 */
data class ContentMetric(
    val contentId: String,
    val contentName: String,
    val views: Int,
    val completions: Int,
    val completionRate: Float,
    val avgScore: Float? = null
)

/**
 * Funnel and conversion metrics for the app.
 */
data class ConversionMetrics(
    val onboardingCompletion: Float,  // % of users who complete onboarding
    val firstLessonView: Float,        // % of users who view first lesson
    val quizAttemptRate: Float,        // % of lesson viewers who attempt quizzes
    val quizPassRate: Float,           // % of quiz attempts that pass
    val levelUpRate: Float              // % of active users who level up
)

/**
 * User retention metrics (cohort analysis).
 */
data class RetentionMetrics(
    val cohortDate: Long,  // Start date of cohort
    val cohortSize: Int,   // Users in this cohort
    val day0: Float,       // % of cohort active on day 0
    val day1: Float,       // % of cohort active on day 1
    val day7: Float,       // % of cohort active on day 7
    val day30: Float       // % of cohort active on day 30
)

/**
 * Represents heatmap data (most used features, screens, etc.).
 */
data class FeatureHeatmap(
    val featureName: String,
    val usageCount: Int,
    val uniqueUsers: Int,
    val avgTimeSpent: Int,
    val popularity: Float  // 0.0 to 1.0
)

/**
 * Represents crash and error metrics.
 */
data class CrashMetrics(
    val crashId: String,
    val userId: String,
    val crashType: String,
    val errorMessage: String,
    val stackTrace: String,
    val deviceInfo: String,
    val osVersion: String,
    val appVersion: String,
    val timestamp: Long,
    val isResolved: Boolean = false
)

/**
 * Represents performance metrics for the app.
 */
data class PerformanceMetric(
    val metricName: String,
    val value: Long,
    val unit: String,  // "ms", "bytes", "count", etc.
    val threshold: Long? = null,  // Alert if value exceeds this
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Represents user behavior patterns and insights.
 */
data class UserBehaviorInsight(
    val userId: String,
    val insight: String,  // e.g., "Most active on weekends", "Prefers earthquake content"
    val confidence: Float,  // 0.0 to 1.0
    val data: Map<String, String> = emptyMap()
)

/**
 * Represents a custom event that tracks user behavior.
 */
data class CustomEvent(
    val name: String,
    val userId: String,
    val properties: Map<String, String> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)

