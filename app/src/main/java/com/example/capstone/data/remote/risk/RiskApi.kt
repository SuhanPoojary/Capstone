package com.example.capstone.data.remote.risk

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface RiskApi {
    @GET("states")
    suspend fun getStates(): StatesResponse

    @GET("districts")
    suspend fun getDistricts(@Query("q") query: String): DistrictsResponse

    @GET("disaster_types")
    suspend fun getDisasterTypes(): DisasterTypesResponse

    @POST("predict")
    suspend fun predictRisk(@Body request: RiskRequest): RiskResponse

    @GET("location_profile")
    suspend fun getLocationProfile(
        @Query("state") state: String,
        @Query("month") month: Int
    ): LocationProfileResponse

    @GET("health")
    suspend fun checkHealth(): Map<String, String>
}
