package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.data.api.GeminiService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class FitnessViewModel(
    application: Application,
    private val repository: FitnessRepository
) : AndroidViewModel(application) {

    private val geminiService = GeminiService()

    private val _selectedDate = MutableStateFlow(getCurrentDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // High level loaders for the current day
    val workoutsForCurrentDate: StateFlow<List<WorkoutProgress>> = _selectedDate
        .flatMapLatest { date -> repository.getWorkoutsForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dietsForCurrentDate: StateFlow<List<DietIntake>> = _selectedDate
        .flatMapLatest { date -> repository.getDietsForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Historical streams for visual metrics charts
    val allWorkouts: StateFlow<List<WorkoutProgress>> = repository.allWorkouts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDiets: StateFlow<List<DietIntake>> = repository.allDiets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dynamic AI Guidance State
    private val _currentAIRecommendation = MutableStateFlow<AIRecommendation?>(null)
    val currentAIRecommendation: StateFlow<AIRecommendation?> = _currentAIRecommendation.asStateFlow()

    private val _isGeneratingRecommendation = MutableStateFlow(false)
    val isGeneratingRecommendation: StateFlow<Boolean> = _isGeneratingRecommendation.asStateFlow()

    private val _currentNutritionAnalysis = MutableStateFlow<NutritionAnalysis?>(null)
    val currentNutritionAnalysis: StateFlow<NutritionAnalysis?> = _currentNutritionAnalysis.asStateFlow()

    private val _isAnalyzingNutrition = MutableStateFlow(false)
    val isAnalyzingNutrition: StateFlow<Boolean> = _isAnalyzingNutrition.asStateFlow()

    // SharedPreferences for local settings and water logs
    private val prefs = getApplication<Application>().getSharedPreferences("fitness_prefs", Context.MODE_PRIVATE)

    // Current Date's Water Intake
    private val _waterIntakeMl = MutableStateFlow(0)
    val waterIntakeMl: StateFlow<Int> = _waterIntakeMl.asStateFlow()

    // Calculated Targets override
    private val _customTargetCalories = MutableStateFlow(0)
    val customTargetCalories: StateFlow<Int> = _customTargetCalories.asStateFlow()

    private val _customTargetProtein = MutableStateFlow(0)
    val customTargetProtein: StateFlow<Int> = _customTargetProtein.asStateFlow()

    private val _customTargetCarbs = MutableStateFlow(0)
    val customTargetCarbs: StateFlow<Int> = _customTargetCarbs.asStateFlow()

    private val _customTargetFat = MutableStateFlow(0)
    val customTargetFat: StateFlow<Int> = _customTargetFat.asStateFlow()

    // User profile characteristics
    private val _userWeightKg = MutableStateFlow(0f)
    val userWeightKg: StateFlow<Float> = _userWeightKg.asStateFlow()

    private val _userHeightCm = MutableStateFlow(0f)
    val userHeightCm: StateFlow<Float> = _userHeightCm.asStateFlow()

    private val _userAge = MutableStateFlow(0)
    val userAge: StateFlow<Int> = _userAge.asStateFlow()

    private val _userRestingHeartRate = MutableStateFlow(0)
    val userRestingHeartRate: StateFlow<Int> = _userRestingHeartRate.asStateFlow()

    private val _weightHistoryRefresh = MutableStateFlow(0)
    val weightHistory: StateFlow<List<Triple<String, String, Float>>> = combine(
        _selectedDate,
        _userWeightKg,
        _weightHistoryRefresh
    ) { date, defaultWeight, _ ->
        val baseWeight = defaultWeight
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val labelFormat = SimpleDateFormat("E", Locale.getDefault())
        (6 downTo 0).map { offset ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -offset)
            val calTime = cal.time
            val dateKey = simpleDateFormat.format(calTime)
            val label = labelFormat.format(calTime)
            val dayWeight = prefs.getFloat("weight_log_$dateKey", baseWeight)
            val finalWeight = if (!prefs.contains("weight_log_$dateKey")) {
                baseWeight
            } else {
                dayWeight
            }
            Triple(dateKey, label, finalWeight)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Automatically load existing advice and health profile on date switch
        viewModelScope.launch {
            _selectedDate.collect { date ->
                loadRecommendationForDate(date)
                loadHealthProfileForDate(date)
            }
        }
        // Initialize all trackers directly to zero so user can input custom logs
        clearAllTrackerLogsOnInit()
    }

    fun selectDate(dateStr: String) {
        _selectedDate.value = dateStr
        val date = dateStr
        _waterIntakeMl.value = prefs.getInt("water_ml_$date", 0)
    }

    fun loadHealthProfileForDate(date: String) {
        _waterIntakeMl.value = prefs.getInt("water_ml_$date", 0)
        _customTargetCalories.value = prefs.getInt("target_calories", 0)
        _customTargetProtein.value = prefs.getInt("target_protein", 0)
        _customTargetCarbs.value = prefs.getInt("target_carbs", 0)
        _customTargetFat.value = prefs.getInt("target_fat", 0)
        _userWeightKg.value = prefs.getFloat("profile_weight", 0f)
        _userHeightCm.value = prefs.getFloat("profile_height", 0f)
        _userAge.value = prefs.getInt("profile_age", 0)
        _userRestingHeartRate.value = prefs.getInt("profile_resting_hr", 0)
    }

    fun addWaterIntake(ml: Int) {
        val date = _selectedDate.value
        val current = _waterIntakeMl.value
        val newVal = (current + ml).coerceAtLeast(0)
        _waterIntakeMl.value = newVal
        prefs.edit().putInt("water_ml_$date", newVal).apply()
    }

    fun resetWaterIntake() {
        val date = _selectedDate.value
        _waterIntakeMl.value = 0
        prefs.edit().putInt("water_ml_$date", 0).apply()
    }

    fun saveHealthProfile(weight: Float, height: Float, age: Int, restingHr: Int, targetCal: Int, targetProt: Int, targetCarb: Int, targetFat: Int) {
        prefs.edit()
            .putFloat("profile_weight", weight)
            .putFloat("profile_height", height)
            .putInt("profile_age", age)
            .putInt("profile_resting_hr", restingHr)
            .putInt("target_calories", targetCal)
            .putInt("target_protein", targetProt)
            .putInt("target_carbs", targetCarb)
            .putInt("target_fat", targetFat)
            .apply()
        
        _userWeightKg.value = weight
        _userHeightCm.value = height
        _userAge.value = age
        _userRestingHeartRate.value = restingHr
        _customTargetCalories.value = targetCal
        _customTargetProtein.value = targetProt
        _customTargetCarbs.value = targetCarb
        _customTargetFat.value = targetFat
        _weightHistoryRefresh.value += 1
    }

    fun saveDailyWeight(weight: Float) {
        val date = _selectedDate.value
        prefs.edit().putFloat("weight_log_$date", weight).apply()
        _userWeightKg.value = weight
        prefs.edit().putFloat("profile_weight", weight).apply()
        _weightHistoryRefresh.value += 1
    }

    fun saveWeightForDate(dateKey: String, weight: Float) {
        prefs.edit().putFloat("weight_log_$dateKey", weight).apply()
        val date = _selectedDate.value
        if (dateKey == date) {
            _userWeightKg.value = weight
            prefs.edit().putFloat("profile_weight", weight).apply()
        }
        _weightHistoryRefresh.value += 1
    }

    fun applyPresetRoutine(routineName: String) {
        viewModelScope.launch {
            val date = _selectedDate.value
            when (routineName) {
                "HIIT Cardio Shred" -> {
                    repository.insertWorkout(WorkoutProgress(0, date, "Sprinting & Burpees HIIT", 25, "High", 310))
                    repository.insertWorkout(WorkoutProgress(0, date, "Jumping Rope workout", 15, "Medium", 140))
                    repository.insertDiet(DietIntake(0, date, "Breakfast", "Boiled Eggs & Avocado Salad", 320, 18, 12, 14))
                    repository.insertDiet(DietIntake(0, date, "Lunch", "Grilled Chicken Breast with Steamed Asparagus", 410, 38, 15, 10))
                }
                "Power Strength Builder" -> {
                    repository.insertWorkout(WorkoutProgress(0, date, "Heavy Barbell Squats & Deadlifts", 45, "High", 380))
                    repository.insertWorkout(WorkoutProgress(0, date, "Dumbbell Bench Press", 20, "Medium", 150))
                    repository.insertDiet(DietIntake(0, date, "Breakfast", "Double Protein Shake & Rolled Oats", 510, 42, 58, 8))
                    repository.insertDiet(DietIntake(0, date, "Snack", "Greek Yogurt with Mixed Nuts", 260, 18, 14, 12))
                    repository.insertDiet(DietIntake(0, date, "Lunch", "Lean Ground Beef with Brown Rice", 580, 44, 65, 14))
                }
                "Zen Yoga & Mindful Recovery" -> {
                    repository.insertWorkout(WorkoutProgress(0, date, "Vinyasa Flow Yoga Stretch", 30, "Low", 110))
                    repository.insertWorkout(WorkoutProgress(0, date, "Deep Core Breathing & Pilates", 20, "Low", 85))
                    repository.insertDiet(DietIntake(0, date, "Breakfast", "Mixed Berry Smoothie Bowl with Flaxseeds", 290, 8, 45, 6))
                    repository.insertDiet(DietIntake(0, date, "Lunch", "Quinoa, Sweet Potato & Chickpea Bowl", 480, 16, 75, 12))
                }
                "Lean Metabolic Burner" -> {
                    repository.insertWorkout(WorkoutProgress(0, date, "Indoor Kettlebell Swings", 30, "Medium", 240))
                    repository.insertWorkout(WorkoutProgress(0, date, "Rowing Machine Intervals", 15, "High", 180))
                    repository.insertDiet(DietIntake(0, date, "Breakfast", "Scrambled Tofu with Spinach & Toast", 310, 16, 28, 9))
                    repository.insertDiet(DietIntake(0, date, "Lunch", "Seared Salmon with Steamed Garlic Broccoli", 440, 34, 10, 22))
                }
            }
        }
    }

    private suspend fun loadRecommendationForDate(date: String) {
        val savedAdvice = repository.getRecommendationForDate(date)
        _currentAIRecommendation.value = savedAdvice
        val savedAnalysis = repository.getNutritionAnalysisForDate(date)
        _currentNutritionAnalysis.value = savedAnalysis
    }

    fun addWorkout(exerciseName: String, durationMin: Int, intensity: String, caloriesBurned: Int) {
        viewModelScope.launch {
            val workout = WorkoutProgress(
                date = _selectedDate.value,
                exerciseName = exerciseName.trim(),
                durationMin = durationMin,
                intensity = intensity,
                caloriesBurned = caloriesBurned
            )
            repository.insertWorkout(workout)
        }
    }

    fun deleteWorkout(workout: WorkoutProgress) {
        viewModelScope.launch {
            repository.deleteWorkout(workout)
        }
    }

    fun addDiet(mealType: String, foodName: String, calories: Int, protein: Int, carbs: Int, fat: Int) {
        viewModelScope.launch {
            val diet = DietIntake(
                date = _selectedDate.value,
                mealType = mealType,
                foodName = foodName.trim(),
                calories = calories,
                proteinGram = protein,
                carbsGram = carbs,
                fatGram = fat
            )
            repository.insertDiet(diet)
        }
    }

    fun deleteDiet(diet: DietIntake) {
        viewModelScope.launch {
            repository.deleteDiet(diet)
        }
    }

    fun clearAllTrackerLogs() {
        val sharedPrefs = getApplication<Application>().getSharedPreferences("fitness_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean("has_seeded_data", true).apply()
        viewModelScope.launch {
            repository.clearAllLogs()
            _currentAIRecommendation.value = null
            _currentNutritionAnalysis.value = null
        }
    }

    fun generateNutritionAnalysis() {
        val date = _selectedDate.value
        val diets = dietsForCurrentDate.value

        viewModelScope.launch {
            _isAnalyzingNutrition.value = true
            try {
                val result = geminiService.getNutritionMealAdjustments(diets, date)
                repository.insertNutritionAnalysis(result)
                _currentNutritionAnalysis.value = result
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isAnalyzingNutrition.value = false
            }
        }
    }

    fun generateAIRecommendation() {
        val date = _selectedDate.value
        val workouts = workoutsForCurrentDate.value
        val diets = dietsForCurrentDate.value

        viewModelScope.launch {
            _isGeneratingRecommendation.value = true
            try {
                // Call generative coach
                val freshAdvice = geminiService.getNutritionAdjustment(workouts, diets, date)
                repository.insertRecommendation(freshAdvice)
                _currentAIRecommendation.value = freshAdvice
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isGeneratingRecommendation.value = false
            }
        }
    }

    private fun clearAllTrackerLogsOnInit() {
        val sharedPrefs = getApplication<Application>().getSharedPreferences("fitness_prefs", Context.MODE_PRIVATE)
        val hasCleared = sharedPrefs.getBoolean("has_cleared_initial_v2", false)
        if (!hasCleared) {
            sharedPrefs.edit()
                .putBoolean("has_cleared_initial_v2", true)
                .putBoolean("has_seeded_data", true)
                .apply()
            viewModelScope.launch {
                repository.clearAllLogs()
                _currentAIRecommendation.value = null
                _currentNutritionAnalysis.value = null
            }
        }
    }

    companion object {
        fun getCurrentDateString(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }
    }
}

class FitnessViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FitnessViewModel::class.java)) {
            val database = FitnessDatabase.getDatabase(application)
            val repository = FitnessRepository(database.fitnessDao())
            @Suppress("UNCHECKED_CAST")
            return FitnessViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
