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
