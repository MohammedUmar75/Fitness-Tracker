package com.example.data

import kotlinx.coroutines.flow.Flow

class FitnessRepository(
    private val fitnessDao: FitnessDao,
    private val userProfileDao: UserProfileDao
) {

    val userProfile: Flow<UserProfileEntity?> = userProfileDao.getUserProfile()

    suspend fun getUserProfileOnce(): UserProfileEntity? = userProfileDao.getUserProfileOnce()

    suspend fun saveUserProfile(profile: UserProfileEntity) {
        userProfileDao.insertOrUpdateProfile(profile)
    }

    suspend fun clearUserProfile() {
        userProfileDao.clearUserProfile()
    }

    val allWorkouts: Flow<List<WorkoutProgress>> = fitnessDao.getAllWorkoutProgress()

    fun getWorkoutsForDate(date: String): Flow<List<WorkoutProgress>> = 
        fitnessDao.getWorkoutProgressForDate(date)

    suspend fun insertWorkout(workout: WorkoutProgress) {
        fitnessDao.insertWorkout(workout)
    }

    suspend fun deleteWorkout(workout: WorkoutProgress) {
        fitnessDao.deleteWorkout(workout)
    }

    val allDiets: Flow<List<DietIntake>> = fitnessDao.getAllDietIntake()

    fun getDietsForDate(date: String): Flow<List<DietIntake>> = 
        fitnessDao.getDietIntakeForDate(date)

    suspend fun insertDiet(diet: DietIntake) {
        fitnessDao.insertDiet(diet)
    }

    suspend fun deleteDiet(diet: DietIntake) {
        fitnessDao.deleteDiet(diet)
    }

    val allRecommendations: Flow<List<AIRecommendation>> = fitnessDao.getAllRecommendations()

    suspend fun getRecommendationForDate(date: String): AIRecommendation? {
        return fitnessDao.getRecommendationForDate(date)
    }

    suspend fun insertRecommendation(recommendation: AIRecommendation) {
        fitnessDao.insertRecommendation(recommendation)
    }

    suspend fun getNutritionAnalysisForDate(date: String): NutritionAnalysis? {
        return fitnessDao.getNutritionAnalysisForDate(date)
    }

    suspend fun insertNutritionAnalysis(analysis: NutritionAnalysis) {
        fitnessDao.insertNutritionAnalysis(analysis)
    }

    suspend fun clearAllLogs() {
        fitnessDao.clearAllWorkouts()
        fitnessDao.clearAllDiets()
        fitnessDao.clearAllRecommendations()
        fitnessDao.clearAllNutritionAnalyses()
        userProfileDao.clearUserProfile()
    }
}
