package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "workout_progress")
data class WorkoutProgress(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // format "yyyy-MM-dd"
    val exerciseName: String,
    val durationMin: Int,
    val intensity: String, // "Low", "Medium", "High"
    val caloriesBurned: Int
)

@Entity(tableName = "diet_intake")
data class DietIntake(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // format "yyyy-MM-dd"
    val mealType: String, // "Breakfast", "Lunch", "Dinner", "Snack"
    val foodName: String,
    val calories: Int,
    val proteinGram: Int,
    val carbsGram: Int,
    val fatGram: Int
)

@Entity(tableName = "ai_recommendation")
data class AIRecommendation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // format "yyyy-MM-dd"
    val suggestion: String,
    val mealPlanGenerated: String?, // Markdown or Structured Text
    val caloriesTarget: Int,
    val proteinTarget: Int,
    val carbsTarget: Int,
    val fatTarget: Int,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "nutrition_analysis")
data class NutritionAnalysis(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // format "yyyy-MM-dd"
    val score: Int, // 0-100 rating of logged food quality
    val overallFeedback: String, // Overview of their nutrition profile
    val adjustmentsList: String, // Markdown of healthier substitutions & alternatives
    val createdAt: Long = System.currentTimeMillis()
)

data class EstimatedFoodNutrition(
    val foodName: String,
    val quantity: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int,
    val note: String = ""
)

data class UserProfile(
    val name: String = "",
    val age: Int = 0,
    val gender: String = "",
    val heightCm: Float = 0f,
    val weightKg: Float = 0f,
    val restingHeartRate: Int = 0,
    val customStrideCm: Float = 0f,
    val activityLevel: String = "Moderately Active",
    val fitnessGoal: String = "Weight Loss",
    val targetSteps: Int = 10000,
    val targetCalories: Int = 2000,
    val targetProtein: Int = 140,
    val targetCarbs: Int = 210,
    val targetFat: Int = 65,
    val targetWaterMl: Int = 2500
) {
    val strideMeters: Float
        get() = if (heightCm > 0f) (heightCm * 0.415f) / 100f else 0f

    val bmi: Float
        get() = if (heightCm > 0f && weightKg > 0f) weightKg / ((heightCm / 100f) * (heightCm / 100f)) else 0f

    val bmiCategory: String
        get() = when {
            bmi <= 0f -> "Unconfigured"
            bmi < 18.5f -> "Underweight"
            bmi < 25.0f -> "Healthy Weight"
            bmi < 30.0f -> "Overweight"
            else -> "Obese"
        }

    val bmr: Int
        get() {
            if (weightKg <= 0f || heightCm <= 0f || age <= 0 || gender.isBlank()) return 0
            val base = (10 * weightKg) + (6.25 * heightCm) - (5 * age)
            val genderOffset = if (gender.equals("Female", ignoreCase = true)) -161 else 5
            return (base + genderOffset).toInt().coerceAtLeast(0)
        }

    val activityMultiplier: Float
        get() = when (activityLevel) {
            "Sedentary" -> 1.2f
            "Lightly Active" -> 1.375f
            "Moderately Active" -> 1.55f
            "Very Active" -> 1.725f
            "Extra Active" -> 1.9f
            else -> 1.55f
        }

    val tdee: Int
        get() = if (bmr > 0) (bmr * activityMultiplier).toInt() else 0

    val recommendedCalories: Int
        get() {
            if (tdee <= 0) return 0
            return when (fitnessGoal) {
                "Weight Loss" -> (tdee - 500).coerceAtLeast(1200)
                "Muscle Gain" -> tdee + 350
                "Endurance" -> tdee + 200
                else -> tdee
            }
        }

    val recommendedProteinGrams: Int
        get() {
            if (weightKg <= 0f || recommendedCalories <= 0) return 0
            val mult = when (fitnessGoal) {
                "Muscle Gain" -> 2.0f
                "Weight Loss" -> 1.8f
                else -> 1.5f
            }
            return (weightKg * mult).toInt().coerceAtLeast(40)
        }

    val recommendedFatGrams: Int
        get() = if (recommendedCalories <= 0) 0 else ((recommendedCalories * 0.25f) / 9f).toInt().coerceIn(30, 120)

    val recommendedCarbsGrams: Int
        get() {
            if (recommendedCalories <= 0) return 0
            val proteinCals = recommendedProteinGrams * 4
            val fatCals = recommendedFatGrams * 9
            val carbCals = (recommendedCalories - proteinCals - fatCals).coerceAtLeast(0)
            return (carbCals / 4f).toInt()
        }
}

@Dao
interface FitnessDao {
    // Workout Queries
    @Query("SELECT * FROM workout_progress ORDER BY date DESC, id DESC")
    fun getAllWorkoutProgress(): Flow<List<WorkoutProgress>>

    @Query("SELECT * FROM workout_progress WHERE date = :date ORDER BY id DESC")
    fun getWorkoutProgressForDate(date: String): Flow<List<WorkoutProgress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutProgress)

    @Delete
    suspend fun deleteWorkout(workout: WorkoutProgress)

    // Diet Queries
    @Query("SELECT * FROM diet_intake ORDER BY date DESC, id DESC")
    fun getAllDietIntake(): Flow<List<DietIntake>>

    @Query("SELECT * FROM diet_intake WHERE date = :date ORDER BY id DESC")
    fun getDietIntakeForDate(date: String): Flow<List<DietIntake>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiet(diet: DietIntake)

    @Delete
    suspend fun deleteDiet(diet: DietIntake)

    // AI Recommendation Queries
    @Query("SELECT * FROM ai_recommendation ORDER BY createdAt DESC")
    fun getAllRecommendations(): Flow<List<AIRecommendation>>

    @Query("SELECT * FROM ai_recommendation WHERE date = :date LIMIT 1")
    suspend fun getRecommendationForDate(date: String): AIRecommendation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecommendation(recommendation: AIRecommendation)

    // Nutrition Analysis Queries
    @Query("SELECT * FROM nutrition_analysis WHERE date = :date LIMIT 1")
    suspend fun getNutritionAnalysisForDate(date: String): NutritionAnalysis?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNutritionAnalysis(analysis: NutritionAnalysis)

    // Clear Queries
    @Query("DELETE FROM workout_progress")
    suspend fun clearAllWorkouts()

    @Query("DELETE FROM diet_intake")
    suspend fun clearAllDiets()

    @Query("DELETE FROM ai_recommendation")
    suspend fun clearAllRecommendations()

    @Query("DELETE FROM nutrition_analysis")
    suspend fun clearAllNutritionAnalyses()
}
