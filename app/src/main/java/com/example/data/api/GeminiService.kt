package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.AIRecommendation
import com.example.data.DietIntake
import com.example.data.WorkoutProgress
import com.example.data.NutritionAnalysis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiService {

    suspend fun getNutritionAdjustment(
        workouts: List<WorkoutProgress>,
        diets: List<DietIntake>,
        dateStr: String
    ): AIRecommendation = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getFallbackRecommendation(
                dateStr, 
                "Demo Mode Active: Enter your custom GEMINI_API_KEY in Google AI Studio's Secrets panel to experience real-time sports coach analytics."
            )
        }

        val workoutLog = if (workouts.isEmpty()) {
            "No workouts logged for this date."
        } else {
            workouts.joinToString("\n") {
                "- ${it.exerciseName}: ${it.durationMin} mins, intensity: ${it.intensity}, burned ~${it.caloriesBurned} kcal"
            }
        }

        val dietLog = if (diets.isEmpty()) {
            "No diet intakes logged for this date."
        } else {
            diets.joinToString("\n") {
                "- ${it.foodName} (${it.mealType}): ${it.calories} kcal, Protein: ${it.proteinGram}g, Carbs: ${it.carbsGram}g, Fat: ${it.fatGram}g"
            }
        }

        val prompt = """
            You are an expert sports dietitian and professional fitness trainer.
            Analyze the following workout progress and nutritional intake for better health and weight management, and provide personalized meal plans and AI-powered nutritional adjustments based on their performance data.

            ### CURRENT WORKOUT HISTORY FOR $dateStr:
            $workoutLog

            ### CURRENT DIET INTAKE FOR $dateStr:
            $dietLog

            Based on this performance data:
            - If they logged intense/long workouts, adjust protein and carb targets upwards to support recovery and preserve muscle.
            - If they logged high nutritional intake but low or no workouts, suggest structural deficit targets and lightweight active recoveries.
            - Suggest customized menus/meals (Breakfast, Lunch, Dinner, Snack) fit for these targets in markdown code.

            Return the response EXACTLY as a single JSON object with no markdown wrappers or text outside the curly braces. The JSON structure:
            {
              "suggestion": "Detailed professional review of their daily actions and how to adjust their behavior. Mention custom macronutrient thresholds decided.",
              "mealPlan": "Personalized meals recommendations list in standard clean Markdown bullets",
              "caloriesTarget": 2200,
              "proteinTarget": 120,
              "carbsTarget": 250,
              "fatTarget": 70
            }
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.4
            )
        )

        try {
            val response = GeminiApiClient.service.generateContent(apiKey, request)
            val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return@withContext getFallbackRecommendation(dateStr, "Empty content returned from the generative model.")

            Log.d("GeminiService", "Raw model output: $rawText")

            val cleanedText = cleanJsonString(rawText)
            val adapter = GeminiApiClient.moshi.adapter(AIResponseParsed::class.java)
            val parsedResult = adapter.fromJson(cleanedText)

            if (parsedResult != null) {
                AIRecommendation(
                    date = dateStr,
                    suggestion = parsedResult.suggestion,
                    mealPlanGenerated = parsedResult.mealPlan,
                    caloriesTarget = parsedResult.caloriesTarget,
                    proteinTarget = parsedResult.proteinTarget,
                    carbsTarget = parsedResult.carbsTarget,
                    fatTarget = parsedResult.fatTarget
                )
            } else {
                getFallbackRecommendation(dateStr, "Could not map JSON response keys properly.")
            }
        } catch (e: Exception) {
            Log.e("GeminiService", "Gemini call exception", e)
            getFallbackRecommendation(
                dateStr, 
                "Connection timeout or API quota limit reached. Falling back to structured default adjustments. [Details: ${e.localizedMessage ?: "Network Err"}]"
            )
        }
    }

    private fun cleanJsonString(raw: String): String {
        var temp = raw.trim()
        if (temp.startsWith("```json")) {
            temp = temp.removePrefix("```json").trim()
        } else if (temp.startsWith("```")) {
            temp = temp.removePrefix("```").trim()
        }
        if (temp.endsWith("```")) {
            temp = temp.removeSuffix("```").trim()
        }
        return temp
    }

    private fun getFallbackRecommendation(date: String, bannerMsg: String): AIRecommendation {
        return AIRecommendation(
            date = date,
            suggestion = "$bannerMsg\n\nDaily Summary: Try to drink 2-3 liters of water. Based on standard wellness defaults, a 2000-kcal balanced ratio of 45% Carbs, 30% Protein, and 25% Fat supports optimized muscle recovery and body mass index balance.",
            mealPlanGenerated = """
                ### Recommended Balanced Meal Plan
                
                *   **Breakfast (approx. 450 kcal)**
                    *   3 scrambled egg whites & 1 whole egg cooked with spinach
                    *   1 slice of whole-wheat sourdough bread toast
                    *   1 medium sized apple or cup of fresh blueberries
                    
                *   **Lunch (approx. 600 kcal)**
                    *   150g grilled organic skinless chicken breast
                    *   1 cup of cooked white or brown quinoa
                    *   Steamed garden veggies (broccoli, asparagus, sweet bell peppers)
                    
                *   **Dinner (approx. 650 kcal)**
                    *   150g baked ocean salmon fillet
                    *   180g roasted sweet potatoes brushed with extra virgin olive oil
                    *   Massaged leafy kale salad with fresh lemon juice
                    
                *   **Snack / Pre-Workout (approx. 300 kcal)**
                    *   1 scoop of grass-fed whey protein isolate
                    *   1 medium organic banana or small cup of Greek yogurt with raw pumpkin seeds
            """.trimIndent(),
            caloriesTarget = 2000,
            proteinTarget = 135,
            carbsTarget = 215,
            fatTarget = 62
        )
    }

    suspend fun getNutritionMealAdjustments(
        diets: List<DietIntake>,
        dateStr: String
    ): NutritionAnalysis = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getFallbackNutritionAnalysis(
                dateStr,
                "Demo Mode Activated: Enter your custom GEMINI_API_KEY in Google AI Studio's Secrets panel to experience real-time sports nutrition food swap recommendations."
            )
        }

        if (diets.isEmpty()) {
            return@withContext NutritionAnalysis(
                date = dateStr,
                score = 50,
                overallFeedback = "No meals have been logged yet for today. Log diets first to enable the AI coach to offer smart substitutions.",
                adjustmentsList = """
                    ### Need Food Logs
                    
                    *   Please log your meals (Breakfast, Lunch, Dinner, or snacks) on the trackers tab.
                    *   Our AI will automatically check nutrient ratios and supply healthy alternatives to lower excess calories, saturated fats, or processed sugars.
                """.trimIndent()
            )
        }

        val dietLog = diets.joinToString("\n") {
            "- ${it.foodName} (${it.mealType}): ${it.calories} kcal, P: ${it.proteinGram}g, C: ${it.carbsGram}g, F: ${it.fatGram}g"
        }

        val prompt = """
            You are an expert sports dietitian and professional nutritionist.
            Analyze the following foods/meals logged by the user for $dateStr.
            Review their meal selections and recommend healthier meal adjustments, portion sizes, macronutrient improvements, and intelligent food substitutions.

            ### USER FOOD LOGS FOR $dateStr:
            $dietLog

            Your task:
            1. Suggest helpful food adjustments or clever substitutions (e.g., swapping white rice with brown quinoa, raw kale instead of standard lettuce dressing, baked turkey slices instead of high-fat pork sausages) to minimize saturated fats or refined sugars while optimizing minerals and amino acids.
            2. Rate the quality score (from 0 to 100) of their logged foods. Be honest and balanced. 
            3. Write a small overall feedback review (2-3 sentences) detailing their strengths and weaknesses.

            Return the response EXACTLY as a single JSON object with no markdown code block wrappers (e.g., no ```json) or trailing text outside of curly braces. Output schema:
            {
              "score": 80,
              "overallFeedback": "Overall feedback text detailing eating patterns and nutrient comments...",
              "adjustmentsList": "Markdown text listing healthy food swaps, substitutes, or adjustments..."
            }
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.3
            )
        )

        try {
            val response = GeminiApiClient.service.generateContent(apiKey, request)
            val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return@withContext getFallbackNutritionAnalysis(dateStr, "Empty body returned from nutrition advisor.")

            Log.d("GeminiService", "Raw meal analysis: $rawText")

            val cleanedText = cleanJsonString(rawText)
            val adapter = GeminiApiClient.moshi.adapter(NutritionAnalysisParsed::class.java)
            val parsedResult = adapter.fromJson(cleanedText)

            if (parsedResult != null) {
                NutritionAnalysis(
                    date = dateStr,
                    score = parsedResult.score,
                    overallFeedback = parsedResult.overallFeedback,
                    adjustmentsList = parsedResult.adjustmentsList
                )
            } else {
                getFallbackNutritionAnalysis(dateStr, "Could not map meal analysis keys.")
            }
        } catch (e: Exception) {
            Log.e("GeminiService", "Meal analysis API call failed", e)
            getFallbackNutritionAnalysis(
                dateStr,
                "API rate limits or connectivity issues occurred. Showing standard healthy default swaps. [Details: ${e.ignoredErrorDetails()}]"
            )
        }
    }

    private fun Exception.ignoredErrorDetails(): String {
        return this.localizedMessage ?: "Network Error"
    }

    private fun getFallbackNutritionAnalysis(date: String, bannerMsg: String): NutritionAnalysis {
        return NutritionAnalysis(
            date = date,
            score = 75,
            overallFeedback = "$bannerMsg\n\nYour eating profile can be enhanced with minor nutrient swaps to support stable sugar lines and metabolic efficiency.",
            adjustmentsList = """
                ### Suggested Healthy Swaps & Substitutes
                
                *   **Substitute Simple Sugars / White Flour**
                    *   Swap standard white wheat toasts or highly sweetened morning oats with sprouted whole-grain sourdough toast, organic steel-cut oats, or fiber-rich grains.
                *   **Swap Saturated & High Pork Fat Cuts**
                    *   Swap standard pork belly or high-fat ground patties with baked lean turkey breast strips, organic wild chicken fillets, or sea-fresh Cod.
                *   **Substitute Refined Oils**
                    *   Swap standard butter or high-heat margarine with extra virgin olive oil, cold-pressed avocado oil, or spray dressings in small moderate volumes.
                *   **Substitute High-Sugar Candies**
                    *   Swap synthetic bars or cookies with high-density fibers like raw almonds, sunflower seeds, sugar-free greek yogurt, or fresh organic blueberries.
            """.trimIndent()
        )
    }
}
