package com.example.capstone.data

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherStackApi {
    @GET("current")
    suspend fun getCurrentWeather(
        @Query("access_key") apiKey: String,
        @Query("query") query: String
    ): WeatherStackResponse
}

data class WeatherStackResponse(
    val current: WeatherStackCurrent,
    val location: WeatherStackLocation
)

data class WeatherStackCurrent(
    val temperature: Int,
    val weather_descriptions: List<String>,
    val precip: Double,
    val wind_speed: Int
)

data class WeatherStackLocation(
    val name: String,
    val region: String,
    val country: String
)

interface MediaStackApi {
    @GET("news")
    suspend fun getNews(
        @Query("access_key") apiKey: String,
        @Query("keywords") keywords: String,
        @Query("languages") languages: String = "en",
        @Query("countries") countries: String? = null,
        @Query("limit") limit: Int = 10
    ): MediaStackResponse
}

data class MediaStackResponse(
    val data: List<NewsArticle>
)

data class NewsArticle(
    val title: String,
    val description: String?,
    val source: String,
    val url: String,
    val category: String,
    val published_at: String
)
