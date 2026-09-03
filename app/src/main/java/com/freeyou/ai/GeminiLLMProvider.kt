package com.freeyou.ai

import com.freeyou.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Gemini Request/Response Models ---
@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null
)

@Serializable
data class Content(
    val role: String? = null,
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String? = null
)

@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

@Serializable
data class Candidate(
    val content: Content? = null
)

// --- Retrofit Interface ---
interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

// --- Provider Implementation ---
class GeminiLLMProvider : LLMProvider {
    private val service: GeminiApiService by lazy {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
            
        val json = Json { ignoreUnknownKeys = true }
        
        val retrofit = Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            
        retrofit.create(GeminiApiService::class.java)
    }

    override suspend fun generateReply(
        systemPrompt: String,
        conversationHistory: List<Pair<String, String>>,
        userText: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "YOUR_GEMINI_API_KEY_HERE") {
            return@withContext "שגיאה: חסר מפתח Gemini API. המערכת פועלת כעת במצב מקומי בלבד (Offline Mode)."
        }

        // Build contents array
        val contents = mutableListOf<Content>()
        
        for ((role, text) in conversationHistory) {
            val validRole = if (role == "mentor" || role == "model") "model" else "user"
            contents.add(
                Content(
                    role = validRole,
                    parts = listOf(Part(text = text))
                )
            )
        }
        
        // Add current user text
        contents.add(
            Content(
                role = "user",
                parts = listOf(Part(text = userText))
            )
        )

        val request = GenerateContentRequest(
            contents = contents,
            systemInstruction = Content(
                parts = listOf(Part(text = systemPrompt))
            )
        )

        try {
            val response = service.generateContent(apiKey, request)
            val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            replyText ?: "לא התקבלה תשובה מהמודל."
        } catch (e: Exception) {
            e.printStackTrace()
            "שגיאת תקשורת עם המודל. עבור למצב חילוץ מקומי."
        }
    }
}
