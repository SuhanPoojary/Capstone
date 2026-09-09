package com.example.capstone.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.capstone.data.remote.risk.*
import com.example.capstone.data.repository.RiskRepository
import com.example.capstone.data.remote.groq.GroqChatDataSource
import com.example.capstone.data.ChatMessage
import kotlinx.coroutines.launch
import java.util.*

class RiskViewModel : ViewModel() {
    private val repository = RiskRepository()
    private val groqDataSource = GroqChatDataSource()

    private val _states = MutableLiveData<List<String>>()
    val states: LiveData<List<String>> = _states

    private val _districts = MutableLiveData<List<String>>()
    val districts: LiveData<List<String>> = _districts

    private val _prediction = MutableLiveData<RiskResponse?>()
    val prediction: LiveData<RiskResponse?> = _prediction

    private val _locationProfile = MutableLiveData<Map<String, RiskSummary>>()
    val locationProfile: LiveData<Map<String, RiskSummary>> = _locationProfile

    private val _explanation = MutableLiveData<String>()
    val explanation: LiveData<String> = _explanation

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadStates()
    }

    private fun loadStates() {
        viewModelScope.launch {
            _states.value = repository.getStates()
        }
    }

    fun searchDistricts(query: String) {
        viewModelScope.launch {
            _districts.value = repository.getDistricts(query)
        }
    }

    fun loadLocationProfile(state: String, month: Int) {
        viewModelScope.launch {
            _locationProfile.value = repository.getLocationProfile(state, month)
        }
    }

    fun checkRisk(state: String?, district: String?, disasterType: String, month: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _prediction.value = null
            _explanation.value = ""
            try {
                val request = RiskRequest(
                    disasterType = disasterType,
                    month = month,
                    state = state,
                    district = district
                )
                val response = repository.predictRisk(request)
                if (response.error != null) {
                    _error.value = response.error
                } else {
                    _prediction.value = response
                    generateAIExplanation(response, state, district, disasterType, month)
                }
            } catch (e: Exception) {
                _error.value = "Failed to fetch risk assessment: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun generateAIExplanation(
        response: RiskResponse,
        state: String?,
        district: String?,
        disasterType: String,
        month: Int
    ) {
        // Dampen probability visually (p^2.5) to avoid alarmism
        // e.g., 0.35 raw prob -> ~0.073 (7.3% visual)
        val visualProbability = Math.pow(response.prediction.probability, 2.5)
        
        val prompt = """
            As an emergency preparedness expert, explain this disaster risk assessment.
            Disaster: $disasterType
            Location: ${district ?: "Unknown district"}, ${state ?: "Unknown state"}
            Month: $month
            Risk Level: ${response.prediction.riskLevel} (${response.prediction.emoji})
            Risk Likelihood: ${String.format(Locale.US, "%.1f", visualProbability * 100)}%
            Confidence: ${response.prediction.dataConfidence}
            
            Historical Data:
            - District events: ${response.breakdown.districtEvents}
            - State events: ${response.breakdown.stateEvents}
            
            Provide a concise (3-4 sentences), encouraging, and actionable explanation. 
            The "Risk Likelihood" is a safety-first metric designed to avoid unnecessary panic. 
            Focus on readiness and safety steps. Do not mention that this percentage is "dampened" or "visual".
        """.trimIndent()

        try {
            val explanation = groqDataSource.complete(
                systemPrompt = "You are a professional disaster risk analyst.",
                conversation = emptyList(),
                userInput = prompt
            )
            _explanation.postValue(explanation)
        } catch (e: Exception) {
            _explanation.postValue("AI explanation unavailable at this moment.")
        }
    }
}
