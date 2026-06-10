package com.example.data.repository

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.data.model.OcrRecipeItem
import com.example.data.network.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class GeminiRepository {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    /**
     * Sends the text question with database inventory context to Gemini.
     */
    suspend fun askAssistant(question: String, dbSummary: String): String = withContext(Dispatchers.IO) {
        val apiKey = GeminiClient.getApiKey()
        if (apiKey == "MOCK_KEY" || apiKey.isBlank()) {
            return@withContext "API Key is missing. Please add your GEMINI_API_KEY in the AI Studio Secrets panel."
        }

        val promptText = """
            Current Live Inventory Database State:
            $dbSummary
            
            User Question:
            $question
            
            Your Guidelines:
            1. You are the AI Assistant for DyeChem Smart Inventory Pro.
            2. Automatically detect user's language. If they speak or ask in Bengali, respond in Bengali. If they speak/ask in English, respond in English.
            3. Answer precisely based on the database state. If an item is low in stock or in a specific rack, mention that immediately.
            4. Keep answers clean, concise, and highly professional for industrial dyeing factory personnel.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    parts = listOf(Part(text = promptText))
                )
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = "You are an expert industrial assistant for DyeChem Smart Inventory Pro database."))
            )
        )

        try {
            val response = GeminiClient.apiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "No response from Gemini API."
        } catch (e: Exception) {
            Log.e("GeminiRepository", "Error asking assistant", e)
            "Error contacting Gemini AI: ${e.localizedMessage ?: "Unknown error"}"
        }
    }

    /**
     * Converts a bitmap image to base64 and performs OCR on it using Gemini Vision 2.0.
     */
    suspend fun performOcrOnImage(bitmap: Bitmap): List<OcrRecipeItem> = withContext(Dispatchers.IO) {
        val apiKey = GeminiClient.getApiKey()
        if (apiKey == "MOCK_KEY" || apiKey.isBlank()) {
            return@withContext emptyList()
        }

        // Convert Bitmap to Base64
        val base64Image = bitmap.toBase64()

        val promptText = """
            Perform OCR on this dyeing recipe or label.
            Read: Product Name, Lot Number (if present or visible), and Quantity (if present or visible).
            Return ONLY a valid JSON array of objects. Do NOT include markdown code blocks like ```json or any other text, just the raw JSON.
            Each object in the array must have these exact keys: "productName" (string), "lotNumber" (string), and "quantity" (double).
            Example output format:
            [{"productName":"Caustic Soda","lotNumber":"CS001","quantity":25.0}]
            If a field like lotNumber or quantity cannot be found, populate them with "" and 0.0 respectively.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = promptText),
                        Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Image))
                    )
                )
            ),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json"
            )
        )

        try {
            val response = GeminiClient.apiService.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return@withContext emptyList()

            Log.d("GeminiRepository", "Raw OCR output: $jsonText")

            // Clean markdown or trailing lines
            val cleanedJson = jsonText.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val listType = Types.newParameterizedType(List::class.java, OcrRecipeItem::class.java)
            val adapter = moshi.adapter<List<OcrRecipeItem>>(listType)
            adapter.fromJson(cleanedJson) ?: emptyList()
        } catch (e: Exception) {
            Log.e("GeminiRepository", "Error compiling OCR", e)
            emptyList()
        }
    }

    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        // Resize slightly to fast-track networks but preserve clarity
        val maxDim = 800
        val ratio = minOf(1.0, maxDim.toDouble() / maxOf(width, height))
        val resized = if (ratio < 1.0) {
            Bitmap.createScaledBitmap(this, (width * ratio).toInt(), (height * ratio).toInt(), true)
        } else {
            this
        }
        resized.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
}
