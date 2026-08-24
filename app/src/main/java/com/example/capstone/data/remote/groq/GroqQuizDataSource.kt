package com.example.capstone.data.remote.groq

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONArray
import org.json.JSONObject

data class QuizQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String,
    val timeLimit: Int = 30
)

class GroqQuizDataSource(private val apiKey: String) {

    suspend fun generateQuiz(topic: String, questionCount: Int = 10, level: Int = 1): List<QuizQuestion> {
        if (apiKey.isEmpty()) {
            return getLocalQuizzes()
        }

        val difficulty = when(level) {
            1 -> "beginner-level basics"
            2 -> "intermediate-level practical scenarios"
            3 -> "advanced-level technical details and complex emergency management"
            else -> "advanced mastery"
        }

        val prompt = """
            Generate $questionCount UNIQUE multiple-choice disaster preparedness quiz questions on "$topic".
            Difficulty level: $difficulty (Level $level).
            Seed: ${System.currentTimeMillis()}
            
            For EACH question, respond EXACTLY in this format on separate lines:
            Q: [Question text]
            A) [Option A]
            B) [Option B]
            C) [Option C]
            D) [Option D]
            Answer: [Single letter: A, B, C, or D]
            Explanation: [Brief explanation why this is correct]
            ---
            
            Ensure questions are different from previous sets. Focus on practical, real-world disaster preparedness knowledge suitable for $difficulty.
            Keep answers concise and clear.
            Make 3 options clearly wrong, 1 correct.
        """.trimIndent()

        return try {
            val response = complete(prompt)
            val parsed = parseQuizResponse(response, questionCount)
            if (parsed.isNotEmpty()) parsed else getLocalQuizzes().shuffled().take(questionCount)
        } catch (e: Exception) {
            Log.e("GroqQuiz", "Failed to generate quiz: ${e.message}")
            getLocalQuizzes().shuffled().take(questionCount)
        }
    }

    private suspend fun complete(prompt: String): String = withContext(Dispatchers.IO) {
        val url = URL(API_ENDPOINT)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Authorization", "Bearer $apiKey")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.connectTimeout = 20000
        connection.readTimeout = 20000

        val requestBody = JSONObject().apply {
            put("model", MODEL)
            put("temperature", 0.3)
            put("max_tokens", 2000)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", SYSTEM_PROMPT)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
        }

        connection.outputStream.use { 
            it.write(requestBody.toString().toByteArray())
        }

        val responseCode = connection.responseCode
        if (responseCode != 200) {
            val errorText = connection.errorStream?.bufferedReader()?.use { it.readText() }
            Log.e("GroqQuiz", "API error: $responseCode - $errorText")
            throw Exception("Groq API returned $responseCode")
        }

        val responseText = connection.inputStream.bufferedReader().use { it.readText() }
        val jsonResponse = JSONObject(responseText)

        jsonResponse
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
    }

    private fun parseQuizResponse(response: String, expectedCount: Int): List<QuizQuestion> {
        val questions = mutableListOf<QuizQuestion>()
        val sections = response.split("---")

        sections.forEach { section ->
            if (section.isBlank()) return@forEach

            val lines = section.trim().split("\n").map { it.trim() }
            val question = lines.find { it.startsWith("Q:") }?.removePrefix("Q:")?.trim() ?: return@forEach
            
            val options = mutableListOf<String>()
            var correctAnswer = ""
            var explanation = ""

            lines.forEach { line ->
                when {
                    line.startsWith("A)") -> options.add(line.removePrefix("A)").trim())
                    line.startsWith("B)") -> options.add(line.removePrefix("B)").trim())
                    line.startsWith("C)") -> options.add(line.removePrefix("C)").trim())
                    line.startsWith("D)") -> options.add(line.removePrefix("D)").trim())
                    line.startsWith("Answer:") -> correctAnswer = line.removePrefix("Answer:").trim()
                    line.startsWith("Explanation:") -> explanation = line.removePrefix("Explanation:").trim()
                }
            }

            if (question.isNotEmpty() && options.size == 4 && correctAnswer.isNotEmpty()) {
                questions.add(QuizQuestion(
                    id = "q_${questions.size}",
                    question = question,
                    options = options,
                    correctAnswer = correctAnswer,
                    explanation = explanation,
                    timeLimit = 30
                ))
            }
        }

        return questions.take(expectedCount)
    }

    private fun getLocalQuizzes(): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                id = "q_0",
                question = "What is the first step during an earthquake?",
                options = listOf(
                    "Run outside immediately",
                    "Drop, Cover, and Hold On",
                    "Use the elevator",
                    "Stay at your desk"
                ),
                correctAnswer = "B",
                explanation = "Drop to your hands and knees, take cover under a sturdy desk or table, and hold on until the shaking stops.",
                timeLimit = 30
            ),
            QuizQuestion(
                id = "q_1",
                question = "How much water should each person store for emergencies?",
                options = listOf(
                    "1 liter per day",
                    "1 gallon (3.8L) per day",
                    "100 liters per day",
                    "Water is not essential"
                ),
                correctAnswer = "B",
                explanation = "The CDC recommends at least 1 gallon (3.8 liters) of clean water per person per day for drinking and sanitation.",
                timeLimit = 30
            ),
            QuizQuestion(
                id = "q_2",
                question = "What should you do if trapped in a building during an earthquake?",
                options = listOf(
                    "Yell loudly for help",
                    "Tap on pipes or walls to signal rescuers",
                    "Move around to find an exit",
                    "Stay perfectly silent"
                ),
                correctAnswer = "B",
                explanation = "Tapping on pipes or walls signals rescuers. Constant yelling wastes energy. Stay calm and signal periodically.",
                timeLimit = 30
            ),
            QuizQuestion(
                id = "q_3",
                question = "Which item is most important in a first aid kit?",
                options = listOf(
                    "Pain relievers",
                    "Adhesive bandages",
                    "Sterile gauze pads",
                    "Antibiotic ointment"
                ),
                correctAnswer = "C",
                explanation = "Sterile gauze pads help control bleeding from larger wounds, which is critical in emergencies.",
                timeLimit = 30
            ),
            QuizQuestion(
                id = "q_4",
                question = "What should you do before a flood warning?",
                options = listOf(
                    "Wait for official evacuation order",
                    "Head to higher ground immediately",
                    "Fill your bathtub with water",
                    "Post on social media"
                ),
                correctAnswer = "B",
                explanation = "Move to higher ground before a flood occurs. Don't wait for evacuation orders in high-risk areas.",
                timeLimit = 30
            )
        )
    }

    companion object {
        private const val API_ENDPOINT = "https://api.groq.com/openai/v1/chat/completions"
        private const val MODEL = "openai/gpt-oss-20b"
        private val SYSTEM_PROMPT = """
            You are a disaster preparedness expert creating educational quiz questions.
            Generate clear, practical multiple-choice questions about disaster safety, emergency preparedness, and survival skills.
            Focus on actionable, life-saving information.
            Make questions challenging but fair with one clearly correct answer.
        """.trimIndent()
    }
}
