package com.kaixgen.assistant.domain.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiHybridAssistant(
    private val apiKeyProvider: () -> String,
    private val modelName: String = "gemini-2.5-flash"
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    sealed interface HybridAiResult {
        data class Reply(val spokenText: String) : HybridAiResult
        data class Unavailable(val reason: String) : HybridAiResult
    }

    suspend fun askHinglishAssistant(
        userQuery: String,
        preferredLanguage: String = "hi-IN"
    ): HybridAiResult = withContext(Dispatchers.IO) {
        val apiKey = runCatching { apiKeyProvider().trim() }.getOrDefault("")
        if (apiKey.isBlank() || apiKey == "YOUR_GEMINI_API_KEY") {
            return@withContext HybridAiResult.Unavailable(
                "Cloud AI key configured nahi hai. Settings ya .env me GEMINI_API_KEY add karein."
            )
        }

        val systemInstruction =
            "You are KAI, an ultra-fast Indian Android voice assistant. " +
                "Reply in short, natural spoken Hinglish/Hindi (max 2 sentences, no markdown bullets or emojis) " +
                "so Android Text-to-Speech can read it smoothly. Language preference: $preferredLanguage."

        val payload = JSONObject().apply {
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", systemInstruction)))
            })
            put("contents", JSONArray().put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().put(JSONObject().put("text", userQuery)))
            }))
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.4)
                put("maxOutputTokens", 140)
            })
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
        val request = Request.Builder()
            .url(url)
            .post(payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        runCatching {
            client.newCall(request).execute().use { response ->
                val rawBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext HybridAiResult.Unavailable("Cloud AI error (${response.code})")
                }
                val root = JSONObject(rawBody)
                val text = root
                    .optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")
                    ?.trim()
                    .orEmpty()

                if (text.isBlank()) {
                    HybridAiResult.Unavailable("Cloud AI ne empty response diya.")
                } else {
                    HybridAiResult.Reply(text)
                }
            }
        }.getOrElse { err ->
            HybridAiResult.Unavailable("Offline ya network error: ${err.localizedMessage ?: "unknown"}")
        }
    }
}
