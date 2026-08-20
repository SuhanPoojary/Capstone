package com.example.capstone.data.remote.groq

import android.util.Base64
import com.example.capstone.BuildConfig
import com.example.capstone.data.MedReadyItem
import com.example.capstone.data.MedReadyScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.UUID

class GroqVisionDataSource {
    fun isConfigured(): Boolean = BuildConfig.GROQ_API_KEY.isNotBlank()

    suspend fun analyzeMedicineKit(imageBytes: ByteArray): MedReadyScanResult = withContext(Dispatchers.IO) {
        if (!isConfigured()) {
            throw IOException("Groq API key is not configured")
        }

        val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
        val imageUrl = "data:image/jpeg;base64,$base64Image"

        val systemPrompt = """
            You are a MedReady AI assistant. Analyze the image of a first aid or medicine kit.
            Identify the items present, their estimated status (Detected, Missing, or Expired if visible), and whether they are essential.
            Provide a readiness score (0-100), count of warnings (missing essentials or expired items), and a brief summary.
            Respond strictly in JSON format:
            {
              "readinessScore": 85,
              "warnings": 1,
              "summary": "Kit is well-stocked but missing antiseptic wipes.",
              "items": [
                {"name": "Bandages", "status": "Detected", "expiryDate": "2026-12", "isEssential": true},
                {"name": "Antiseptic Wipes", "status": "Missing", "isEssential": true}
              ]
            }
        """.trimIndent()

        val messages = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "user")
                put("content", JSONArray().apply {
                    put(JSONObject().put("type", "text").put("text", systemPrompt))
                    put(JSONObject().put("type", "image_url").put("image_url", JSONObject().put("url", imageUrl)))
                })
            })
        }

        val request = JSONObject().apply {
            put("model", MODEL)
            put("messages", messages)
            // Removed response_format for broader model compatibility
            put("temperature", 0.1)
        }

        val connection = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 30_000
            readTimeout = 30_000
            doInput = true
            doOutput = true
            setRequestProperty("Authorization", "Bearer ${BuildConfig.GROQ_API_KEY}")
            setRequestProperty("Content-Type", "application/json")
            outputStream.use { it.write(request.toString().toByteArray(StandardCharsets.UTF_8)) }
        }

        val responseBody = runCatching {
            if (connection.responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            }
        }.getOrElse { 
            connection.disconnect()
            throw IOException("Groq Vision request failed", it) 
        }
        connection.disconnect()

        val response = JSONObject(responseBody)
        if (!response.has("choices")) {
            val errorMsg = response.optJSONObject("error")?.optString("message") 
                ?: "API Error: 'choices' field missing. Response: $responseBody"
            throw IOException(errorMsg)
        }
        var content = response.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
        
        // Robustly extract JSON by stripping markdown wrappers if present
        if (content.contains("```json")) {
            content = content.substringAfter("```json").substringBeforeLast("```")
        } else if (content.contains("```")) {
            content = content.substringAfter("```").substringBeforeLast("```")
        }
        content = content.trim()

        val json = try {
            JSONObject(content)
        } catch (e: Exception) {
            android.util.Log.e("GroqVision", "JSON parsing failed for content: $content", e)
            // Fallback: If stripping failed, try to find the first '{' and last '}'
            val firstBrace = content.indexOf('{')
            val lastBrace = content.lastIndexOf('}')
            if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
                try {
                    JSONObject(content.substring(firstBrace, lastBrace + 1))
                } catch (e2: Exception) {
                    JSONObject().apply {
                        put("readinessScore", 0)
                        put("warnings", 0)
                        put("summary", "Analysis failed to parse: ${e2.message}")
                        put("items", JSONArray())
                    }
                }
            } else {
                // Return a mock object if parsing completely fails to avoid crash
                JSONObject().apply {
                    put("readinessScore", 0)
                    put("warnings", 0)
                    put("summary", "Analysis failed to parse (no braces found).")
                    put("items", JSONArray())
                }
            }
        }
        val itemsArray = json.optJSONArray("items") ?: JSONArray()
        val items = mutableListOf<MedReadyItem>()
        for (i in 0 until itemsArray.length()) {
            val itemObj = itemsArray.getJSONObject(i)
            items.add(MedReadyItem(
                name = itemObj.optString("name", "Unknown Item"),
                status = itemObj.optString("status", "Missing"),
                expiryDate = itemObj.optString("expiryDate"),
                isEssential = itemObj.optBoolean("isEssential", true)
            ))
        }

        MedReadyScanResult(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            itemCount = items.count { it.status == "Detected" },
            readinessScore = json.optInt("readinessScore", 0),
            warnings = json.optInt("warnings", 0),
            summary = json.optString("summary", "No summary available."),
            items = items
        )
    }

    private companion object {
        private const val ENDPOINT = "https://api.groq.com/openai/v1/chat/completions"
        private const val MODEL = "llama-3.2-11b-vision-preview" // Keeping vision for now as it's separate from 70b-versatile
    }
}
