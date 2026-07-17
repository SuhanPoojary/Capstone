package com.example.capstone.data.remote.groq

import com.example.capstone.BuildConfig
import com.example.capstone.data.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class GroqChatDataSource {
    fun isConfigured(): Boolean = BuildConfig.GROQ_API_KEY.isNotBlank()

    suspend fun complete(
        systemPrompt: String,
        conversation: List<ChatMessage>,
        userInput: String,
    ): String = withContext(Dispatchers.IO) {
        if (!isConfigured()) {
            throw IOException("Groq API key is not configured")
        }

        val messages = JSONArray().apply {
            put(JSONObject().put("role", "system").put("content", systemPrompt))
            conversation.takeLast(8).forEach { message ->
                put(
                    JSONObject()
                        .put("role", if (message.isUser) "user" else "assistant")
                        .put("content", message.text)
                )
            }
            put(JSONObject().put("role", "user").put("content", userInput))
        }

        val request = JSONObject().apply {
            put("model", MODEL)
            put("temperature", 0.2)
            put("max_tokens", 420)
            put("messages", messages)
        }

        val connection = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20_000
            readTimeout = 20_000
            doInput = true
            doOutput = true
            setRequestProperty("Authorization", "Bearer ${BuildConfig.GROQ_API_KEY}")
            setRequestProperty("Content-Type", "application/json")
            outputStream.use { output ->
                output.write(request.toString().toByteArray(StandardCharsets.UTF_8))
            }
        }

        val response = runCatching {
            val payload = if (connection.responseCode in 200..299) {
                connection.inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() }.orEmpty()
            }
            JSONObject(payload.ifBlank { "{}" })
        }.getOrElse { error ->
            connection.disconnect()
            throw IOException("Groq request failed: ${error.message}", error)
        }

        connection.disconnect()

        val answer = response.optJSONArray("choices")
            ?.optJSONObject(0)
            ?.optJSONObject("message")
            ?.optString("content")
            .orEmpty()
            .trim()

        if (answer.isBlank()) {
            throw IOException("Groq response did not include assistant content")
        }

        answer
    }

    private companion object {
        private const val ENDPOINT = "https://api.groq.com/openai/v1/chat/completions"
        private const val MODEL = "llama-3.1-8b-instant"
    }
}
