package com.example.capstone.data.remote.risk

import com.google.gson.annotations.SerializedName

data class StatesResponse(
    val states: List<String>,
    val count: Int
)

data class DistrictsResponse(
    val districts: List<String>,
    val count: Int
)

data class DisasterTypesResponse(
    @SerializedName("disaster_types")
    val disasterTypes: List<String>
)

data class RiskRequest(
    @SerializedName("disaster_type")
    val disasterType: String,
    val month: Int,
    val state: String? = null,
    val district: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class RiskPrediction(
    @SerializedName("risk_level")
    val riskLevel: String,
    val emoji: String,
    val probability: Double,
    @SerializedName("is_high_risk")
    val isHighRisk: Boolean,
    val advice: String,
    @SerializedName("data_confidence")
    val dataConfidence: String,
    @SerializedName("contributing_levels")
    val contributingLevels: List<String>
)

data class RiskBreakdown(
    @SerializedName("district_events")
    val districtEvents: Int,
    @SerializedName("district_this_month")
    val districtThisMonth: Int,
    @SerializedName("state_events")
    val stateEvents: Int,
    @SerializedName("state_this_month")
    val stateThisMonth: Int,
    @SerializedName("disaster_month_rate")
    val disasterMonthRate: Double
)

data class RiskResponse(
    val prediction: RiskPrediction,
    val breakdown: RiskBreakdown,
    @SerializedName("model_scores")
    val modelScores: Map<String, Double>,
    val error: String? = null
)

data class LocationProfileResponse(
    val profile: Map<String, RiskSummary>
)

data class RiskSummary(
    @SerializedName("risk_level")
    val riskLevel: String,
    val emoji: String,
    val probability: Double
)
