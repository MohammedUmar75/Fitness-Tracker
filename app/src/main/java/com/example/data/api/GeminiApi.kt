package com.example.data.api

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @Json(name = "responseMimeType") val responseMimeType: String? = null,
    @Json(name = "temperature") val temperature: Double? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: Content
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>?
)

// Response from the model
@JsonClass(generateAdapter = true)
data class AIResponseParsed(
    @Json(name = "suggestion") val suggestion: String,
    @Json(name = "mealPlan") val mealPlan: String,
    @Json(name = "caloriesTarget") val caloriesTarget: Int,
    @Json(name = "proteinTarget") val proteinTarget: Int,
    @Json(name = "carbsTarget") val carbsTarget: Int,
    @Json(name = "fatTarget") val fatTarget: Int
)

@JsonClass(generateAdapter = true)
data class NutritionAnalysisParsed(
    @Json(name = "score") val score: Int,
    @Json(name = "overallFeedback") val overallFeedback: String,
    @Json(name = "adjustmentsList") val adjustmentsList: String
)

@JsonClass(generateAdapter = true)
data class FoodEstimationParsed(
    @Json(name = "calories") val calories: Int,
    @Json(name = "protein") val protein: Int,
    @Json(name = "carbs") val carbs: Int,
    @Json(name = "fat") val fat: Int,
    @Json(name = "note") val note: String? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiApiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val service: GeminiApiService = retrofit.create(GeminiApiService::class.java)
}
