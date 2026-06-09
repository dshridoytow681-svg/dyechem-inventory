package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Moshi Request/Response Codegen Structures ---

data class Part(val text: String)
data class Content(val parts: List<Part>)
data class GenerateContentRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null
)

data class Candidate(val content: Content)
data class GenerateContentResponse(val candidates: List<Candidate>?)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }
}

class GeminiVoiceService {

    suspend fun getSmartAIResponse(userVoiceInput: String, inventoryContext: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Local AI Simulator Prompt Intercept: (Note: Enter your Gemini API key in the AI Studio Secrets panel for live cloud processing).\nYou said: \"$userVoiceInput\"\nInventory Context context indicates: $inventoryContext"
        }

        val promptText = """
            You are "DyeChem Smart AI Assistant", the friendly voice-enabled store expert for an industrial mill.
            You should assist Store Keepers or Managers immediately. 
            Keep your response short, highly informative, concise, and localized (you can respond in Bengali if they asked in Bengali, or English if they asked in English), professional and objective.
            
            Current Store Inventory Ledger:
            $inventoryContext
            
            User's Voice Command or Question: "$userVoiceInput"
        """.trimIndent()

        val systemInstruction = """
            Speak as a technical chemical and dye warehouse manager. Be strictly factual, short (maximum 2-3 sentences), and polite.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(promptText)))),
            systemInstruction = Content(parts = listOf(Part(systemInstruction)))
        )

        return try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "Empty response from Gemini server."
        } catch (e: Exception) {
            "Voice processing error or API is unavailable. ${e.localizedMessage ?: "Please check internet connections."}"
        }
    }
}
