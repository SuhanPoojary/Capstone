package com.example.capstone.data.repository

import com.example.capstone.data.remote.risk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RiskRepository {
    private val api: RiskApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://disaster-risk-api.onrender.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RiskApi::class.java)
    }

    suspend fun getStates(): List<String> = withContext(Dispatchers.IO) {
        try {
            api.getStates().states
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getDistricts(query: String): List<String> = withContext(Dispatchers.IO) {
        try {
            api.getDistricts(query).districts
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getDisasterTypes(): List<String> = withContext(Dispatchers.IO) {
        try {
            api.getDisasterTypes().disasterTypes
        } catch (e: Exception) {
            listOf("Flood", "Earthquake", "Landslide", "Cyclone")
        }
    }

    suspend fun predictRisk(request: RiskRequest): RiskResponse = withContext(Dispatchers.IO) {
        api.predictRisk(request)
    }

    suspend fun getLocationProfile(state: String, month: Int): Map<String, RiskSummary> = withContext(Dispatchers.IO) {
        try {
            api.getLocationProfile(state, month).profile
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
