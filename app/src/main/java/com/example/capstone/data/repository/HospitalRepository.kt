package com.example.capstone.data.repository

import android.content.Context
import com.example.capstone.data.local.database.dao.HospitalDao
import com.example.capstone.data.local.database.entity.HospitalEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

class HospitalRepository(private val hospitalDao: HospitalDao, private val context: Context) {

    val allHospitals: Flow<List<HospitalEntity>> = hospitalDao.getAllHospitals()
    private var lastFetchBounds: Bounds? = null
    private var lastFetchTimestampMs: Long = 0L

    suspend fun refreshHospitalsIfEmpty() {
        withContext(Dispatchers.IO) {
            val count = hospitalDao.getCount()
            if (count == 0) {
                // Initial fetch for a broad Mumbai area as a fallback
                fetchHospitalsInBbox(18.85, 72.75, 19.35, 73.15)
            }
        }
    }

    suspend fun fetchHospitalsInBbox(minLat: Double, minLon: Double, maxLat: Double, maxLon: Double) {
        withContext(Dispatchers.IO) {
            try {
                val requestedBounds = Bounds(
                    south = min(minLat, maxLat),
                    west = min(minLon, maxLon),
                    north = max(minLat, maxLat),
                    east = max(minLon, maxLon)
                )
                if (!shouldFetch(requestedBounds)) {
                    return@withContext
                }

                android.util.Log.d("HospitalRepository", "Fetching hospitals for bbox: ${requestedBounds.south},${requestedBounds.west} to ${requestedBounds.north},${requestedBounds.east}")
                
                val query = """
                    [out:json][timeout:30];
                    (
                      node["amenity"="hospital"](${requestedBounds.south},${requestedBounds.west},${requestedBounds.north},${requestedBounds.east});
                      way["amenity"="hospital"](${requestedBounds.south},${requestedBounds.west},${requestedBounds.north},${requestedBounds.east});
                    );
                    out center;
                """.trimIndent()

                val url = java.net.URL("https://overpass-api.de/api/interpreter?data=${java.net.URLEncoder.encode(query, "UTF-8")}")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 15000
                connection.readTimeout = 20000
                
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonObject = org.json.JSONObject(response)
                val elements = jsonObject.getJSONArray("elements")
                
                android.util.Log.d("HospitalRepository", "Found ${elements.length()} elements")
                
                val hospitals = mutableListOf<HospitalEntity>()
                for (i in 0 until elements.length()) {
                    val element = elements.getJSONObject(i)
                    val lat = if (element.has("lat")) element.getDouble("lat") else element.optJSONObject("center")?.optDouble("lat")
                    val lon = if (element.has("lon")) element.getDouble("lon") else element.optJSONObject("center")?.optDouble("lon")
                    
                    if (lat != null && lon != null) {
                        val tags = if (element.has("tags")) element.getJSONObject("tags") else null
                        val name = tags?.optString("name") ?: "Hospital"
                        
                        hospitals.add(HospitalEntity(
                            name = name,
                            type = tags?.optString("healthcare") ?: "Medical Facility",
                            address = tags?.optString("addr:street") ?: "Unknown Location",
                            latitude = lat,
                            longitude = lon,
                            emergencyServices = tags?.optString("emergency") == "yes"
                        ))
                    }
                }
                lastFetchBounds = requestedBounds
                lastFetchTimestampMs = System.currentTimeMillis()
                
                if (hospitals.isNotEmpty()) {
                    val deduplicated = hospitals.distinctBy { hospital ->
                        Triple(
                            hospital.name.trim().lowercase(),
                            String.format(Locale.US, "%.5f", hospital.latitude),
                            String.format(Locale.US, "%.5f", hospital.longitude)
                        )
                    }
                    hospitalDao.deleteAll()
                    hospitalDao.insertAll(deduplicated)
                    android.util.Log.d("HospitalRepository", "Inserted ${deduplicated.size} unique hospitals into DB")
                }
            } catch (e: Exception) {
                android.util.Log.e("HospitalRepository", "Error fetching hospitals: ${e.message}")
            }
        }
    }

    private fun shouldFetch(requestedBounds: Bounds): Boolean {
        val now = System.currentTimeMillis()
        val lastBounds = lastFetchBounds ?: return true
        val fetchedRecently = (now - lastFetchTimestampMs) < MIN_FETCH_INTERVAL_MS
        return !(fetchedRecently && lastBounds.contains(requestedBounds))
    }

    private data class Bounds(
        val south: Double,
        val west: Double,
        val north: Double,
        val east: Double
    ) {
        fun contains(other: Bounds): Boolean {
            val margin = 0.02
            return other.south >= (south - margin) &&
                other.west >= (west - margin) &&
                other.north <= (north + margin) &&
                other.east <= (east + margin)
        }
    }

    companion object {
        private const val MIN_FETCH_INTERVAL_MS = 15_000L
    }
}
