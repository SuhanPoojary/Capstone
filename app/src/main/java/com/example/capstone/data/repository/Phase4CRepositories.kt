package com.example.capstone.data.repository

import androidx.lifecycle.LiveData
import com.example.capstone.data.*

/**
 * Repository for tracking and logging analytics events.
 * 
 * Phase 4C: Analytics and Event Tracking
 */
interface AnalyticsRepository {
    /**
     * Log a single event.
     */
    suspend fun logEvent(event: AnalyticsEvent): Result<Unit>
    
    /**
     * Log a page view.
     */
    suspend fun logPageView(pageName: String, userId: String? = null): Result<Unit>
    
    /**
     * Log a user action (button tap, menu selection, etc).
     */
    suspend fun logUserAction(
        actionName: String,
        userId: String? = null,
        properties: Map<String, String> = emptyMap()
    ): Result<Unit>
    
    /**
     * Log content interaction (lesson viewed, video played, etc).
     */
    suspend fun logContentInteraction(
        contentType: String,
        contentId: String,
        userId: String? = null,
        properties: Map<String, String> = emptyMap()
    ): Result<Unit>
    
    /**
     * Log quiz assessment.
     */
    suspend fun logQuizAssessment(
        quizId: String,
        userId: String,
        score: Int,
        passed: Boolean,
        timeSpent: Int
    ): Result<Unit>
    
    /**
     * Log an error or crash.
     */
    suspend fun logError(
        errorType: String,
        errorMessage: String,
        stackTrace: String,
        userId: String? = null
    ): Result<Unit>
    
    /**
     * Get historical events for a user.
     */
    suspend fun getUserEvents(
        userId: String,
        limit: Int = 100,
        offset: Int = 0
    ): Result<List<AnalyticsEvent>>
    
    /**
     * Get events of a specific type.
     */
    suspend fun getEventsByType(
        eventType: EventType,
        limit: Int = 100
    ): Result<List<AnalyticsEvent>>
}

/**
 * Repository for managing user learning sessions.
 * 
 * Phase 4C: Analytics and Event Tracking
 */
interface SessionRepository {
    /**
     * Start a new learning session.
     */
    suspend fun startSession(userId: String): Result<String>  // Returns sessionId
    
    /**
     * End the current session.
     */
    suspend fun endSession(sessionId: String): Result<Unit>
    
    /**
     * Get the current active session.
     */
    fun getCurrentSession(userId: String): LiveData<LearningSession?>
    
    /**
     * Get past sessions for a user.
     */
    suspend fun getUserSessions(
        userId: String,
        limit: Int = 30,
        offset: Int = 0
    ): Result<List<LearningSession>>
    
    /**
     * Log a lesson view in the current session.
     */
    suspend fun logLessonView(sessionId: String, lessonId: String): Result<Unit>
    
    /**
     * Log a quiz attempt in the current session.
     */
    suspend fun logQuizAttempt(
        sessionId: String,
        quizId: String,
        score: Int,
        passed: Boolean
    ): Result<Unit>
}

/**
 * Repository for querying aggregated analytics metrics.
 * 
 * Phase 4C: Analytics and Event Tracking
 */
interface MetricsRepository {
    /**
     * Get analytics metrics for a specific time period.
     */
    suspend fun getMetrics(
        period: TimePeriod,
        userId: String? = null
    ): Result<AnalyticsMetrics>
    
    /**
     * Get page metrics for a specific page.
     */
    suspend fun getPageMetrics(pageName: String): Result<PageMetric>
    
    /**
     * Get top pages by views.
     */
    suspend fun getTopPages(limit: Int = 10): Result<List<PageMetric>>
    
    /**
     * Get content metrics for a specific lesson/quiz.
     */
    suspend fun getContentMetrics(contentId: String): Result<ContentMetric>
    
    /**
     * Get top content by completion rate.
     */
    suspend fun getTopContentByCompletion(limit: Int = 10): Result<List<ContentMetric>>
    
    /**
     * Get conversion funnel metrics.
     */
    suspend fun getConversionMetrics(): Result<ConversionMetrics>
    
    /**
     * Get retention metrics for a cohort.
     */
    suspend fun getRetentionMetrics(cohortStartDate: Long): Result<RetentionMetrics>
}

/**
 * Repository for heatmap and feature usage data.
 * 
 * Phase 4C: Analytics and Event Tracking
 */
interface HeatmapRepository {
    /**
     * Get heatmap data for all features.
     */
    suspend fun getAllFeatureHeatmaps(): Result<List<FeatureHeatmap>>
    
    /**
     * Get heatmap data for a specific feature.
     */
    suspend fun getFeatureHeatmap(featureName: String): Result<FeatureHeatmap>
    
    /**
     * Record a feature usage event.
     */
    suspend fun recordFeatureUsage(featureName: String, userId: String): Result<Unit>
}

/**
 * Repository for crash and error reporting.
 * 
 * Phase 4C: Analytics and Event Tracking
 */
interface CrashRepository {
    /**
     * Report a crash.
     */
    suspend fun reportCrash(metrics: CrashMetrics): Result<Unit>
    
    /**
     * Get recent crashes.
     */
    suspend fun getRecentCrashes(limit: Int = 50): Result<List<CrashMetrics>>
    
    /**
     * Get crashes for a specific user.
     */
    suspend fun getUserCrashes(userId: String, limit: Int = 50): Result<List<CrashMetrics>>
    
    /**
     * Mark a crash as resolved.
     */
    suspend fun markCrashResolved(crashId: String): Result<Unit>
}

/**
 * Repository for performance metrics.
 * 
 * Phase 4C: Analytics and Event Tracking
 */
interface PerformanceRepository {
    /**
     * Log a performance metric.
     */
    suspend fun logMetric(metric: PerformanceMetric): Result<Unit>
    
    /**
     * Get recent performance metrics.
     */
    suspend fun getRecentMetrics(limit: Int = 100): Result<List<PerformanceMetric>>
    
    /**
     * Listen to performance metrics in real-time.
     */
    fun observeMetrics(): LiveData<List<PerformanceMetric>>
    
    /**
     * Get metrics that exceed their threshold.
     */
    suspend fun getAnomalousMetrics(): Result<List<PerformanceMetric>>
}

/**
 * Repository for user behavior insights and recommendations.
 * 
 * Phase 4C: Analytics and Event Tracking
 */
interface InsightsRepository {
    /**
     * Get behavioral insights for a user.
     */
    suspend fun getUserInsights(userId: String): Result<List<UserBehaviorInsight>>
    
    /**
     * Get global app insights.
     */
    suspend fun getGlobalInsights(): Result<List<UserBehaviorInsight>>
    
    /**
     * Calculate insights from event data.
     * Called periodically by backend or on-demand.
     */
    suspend fun calculateInsights(userId: String, days: Int = 30): Result<List<UserBehaviorInsight>>
}

/**
 * Repository for custom event tracking.
 * 
 * Phase 4C: Analytics and Event Tracking
 * 
 * This is a simplified interface for apps to track custom events beyond the standard types.
 */
interface CustomEventRepository {
    /**
     * Track a custom event.
     */
    suspend fun trackEvent(event: CustomEvent): Result<Unit>
    
    /**
     * Track multiple events in batch.
     */
    suspend fun trackEventBatch(events: List<CustomEvent>): Result<Unit>
}

