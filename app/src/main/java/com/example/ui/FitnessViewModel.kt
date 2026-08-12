package com.example.ui

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
) : AndroidViewModel(application), SensorEventListener {

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

    private val _isEstimatingFood = MutableStateFlow(false)
    val isEstimatingFood: StateFlow<Boolean> = _isEstimatingFood.asStateFlow()

    private val _lastFoodEstimate = MutableStateFlow<EstimatedFoodNutrition?>(null)
    val lastFoodEstimate: StateFlow<EstimatedFoodNutrition?> = _lastFoodEstimate.asStateFlow()

    // SharedPreferences for local settings and water logs
    private val prefs = getApplication<Application>().getSharedPreferences("fitness_prefs", Context.MODE_PRIVATE)

    // Daily Usage Streak Tracking
    private val _streakCount = MutableStateFlow(0)
    val streakCount: StateFlow<Int> = _streakCount.asStateFlow()

    // Celebratory Confetti & Goal Achievement Event State
    private val _celebrationEvent = MutableStateFlow<com.example.ui.components.CelebrationEvent?>(null)
    val celebrationEvent: StateFlow<com.example.ui.components.CelebrationEvent?> = _celebrationEvent.asStateFlow()

    fun clearCelebrationEvent() {
        _celebrationEvent.value = null
    }

    fun completeOnboardingWithoutSave() {
        _isOnboardingCompleted.value = true
        prefs.edit().putBoolean("is_onboarding_completed", true).apply()
    }

    fun triggerCelebration(title: String, subtitle: String, emoji: String = "🎉") {
        _celebrationEvent.value = com.example.ui.components.CelebrationEvent(
            title = title,
            subtitle = subtitle,
            emoji = emoji
        )
    }

    // Footsteps & Automatic Sensor Tracking
    private val _dailySteps = MutableStateFlow(0)
    val dailySteps: StateFlow<Int> = _dailySteps.asStateFlow()

    private val _targetSteps = MutableStateFlow(10000)
    val targetSteps: StateFlow<Int> = _targetSteps.asStateFlow()

    private val _isStepTrackingActive = MutableStateFlow(true)
    val isStepTrackingActive: StateFlow<Boolean> = _isStepTrackingActive.asStateFlow()

    private val _sensorTypeName = MutableStateFlow("Hardware Step Counter")
    val sensorTypeName: StateFlow<String> = _sensorTypeName.asStateFlow()

    private val _stepHistoryRefresh = MutableStateFlow(0)
    val stepHistory: StateFlow<List<Triple<String, String, Int>>> = combine(
        _selectedDate,
        _stepHistoryRefresh
    ) { _, _ ->
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val labelFormat = SimpleDateFormat("E", Locale.getDefault())
        (6 downTo 0).map { offset ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -offset)
            val calTime = cal.time
            val dateKey = simpleDateFormat.format(calTime)
            val label = labelFormat.format(calTime)
            val daySteps = prefs.getInt("steps_log_$dateKey", 0)
            Triple(dateKey, label, daySteps)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var sensorManager: SensorManager? = null
    private var initialHardwareSteps = -1f
    private var lastAccelStepTime = 0L
    private var isAccelAbovePeak = false
    private var filteredAccel = 9.81f

    // Current Date's Water Intake
    private val _waterIntakeMl = MutableStateFlow(0)
    val waterIntakeMl: StateFlow<Int> = _waterIntakeMl.asStateFlow()

    // Water Reminder Preferences & Notification System
    private val _isWaterReminderEnabled = MutableStateFlow(prefs.getBoolean("water_reminder_enabled", false))
    val isWaterReminderEnabled: StateFlow<Boolean> = _isWaterReminderEnabled.asStateFlow()

    private val _waterReminderIntervalHours = MutableStateFlow(prefs.getInt("water_reminder_interval", 2))
    val waterReminderIntervalHours: StateFlow<Int> = _waterReminderIntervalHours.asStateFlow()

    // Calculated Targets override
    private val _customTargetCalories = MutableStateFlow(0)
    val customTargetCalories: StateFlow<Int> = _customTargetCalories.asStateFlow()

    private val _customTargetProtein = MutableStateFlow(0)
    val customTargetProtein: StateFlow<Int> = _customTargetProtein.asStateFlow()

    private val _customTargetCarbs = MutableStateFlow(0)
    val customTargetCarbs: StateFlow<Int> = _customTargetCarbs.asStateFlow()

    private val _customTargetFat = MutableStateFlow(0)
    val customTargetFat: StateFlow<Int> = _customTargetFat.asStateFlow()

    // Authentication State
    private val _isLoggedIn = MutableStateFlow(
        prefs.getBoolean("is_logged_in", false) && !(prefs.getString("user_email", "") ?: "").isBlank()
    )
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userEmail = MutableStateFlow(prefs.getString("user_email", "") ?: "")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _authProvider = MutableStateFlow(prefs.getString("auth_provider", "Email") ?: "Email")
    val authProvider: StateFlow<String> = _authProvider.asStateFlow()

    // User profile characteristics & state
    private val _isOnboardingCompleted = MutableStateFlow(
        prefs.getBoolean("is_onboarding_completed", false) && !(prefs.getString("profile_name", "") ?: "").isBlank()
    )
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    private val _userName = MutableStateFlow(prefs.getString("profile_name", "") ?: "")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userGender = MutableStateFlow(prefs.getString("profile_gender", "") ?: "")
    val userGender: StateFlow<String> = _userGender.asStateFlow()

    private val _userWeightKg = MutableStateFlow(prefs.getFloat("profile_weight", 0f).coerceAtLeast(0f))
    val userWeightKg: StateFlow<Float> = _userWeightKg.asStateFlow()

    private val _userHeightCm = MutableStateFlow(prefs.getFloat("profile_height", 0f).coerceAtLeast(0f))
    val userHeightCm: StateFlow<Float> = _userHeightCm.asStateFlow()

    private val _userCustomStrideCm = MutableStateFlow(prefs.getFloat("profile_custom_stride", 0f).coerceAtLeast(0f))
    val userCustomStrideCm: StateFlow<Float> = _userCustomStrideCm.asStateFlow()

    private val _userAge = MutableStateFlow(prefs.getInt("profile_age", 0).coerceAtLeast(0))
    val userAge: StateFlow<Int> = _userAge.asStateFlow()

    private val _userRestingHeartRate = MutableStateFlow(prefs.getInt("profile_resting_hr", 0).coerceAtLeast(0))
    val userRestingHeartRate: StateFlow<Int> = _userRestingHeartRate.asStateFlow()

    private val _userActivityLevel = MutableStateFlow(prefs.getString("profile_activity_level", "Moderately Active") ?: "Moderately Active")
    val userActivityLevel: StateFlow<String> = _userActivityLevel.asStateFlow()

    private val _userFitnessGoal = MutableStateFlow(prefs.getString("profile_fitness_goal", "Weight Loss") ?: "Weight Loss")
    val userFitnessGoal: StateFlow<String> = _userFitnessGoal.asStateFlow()

    val userProfile: StateFlow<com.example.data.UserProfile> = combine(
        combine(_userName, _userAge, _userGender, _userHeightCm, _userWeightKg) { name, age, gender, h, w ->
            listOf(name, age, gender, h, w)
        },
        combine(_userCustomStrideCm, _userRestingHeartRate, _userActivityLevel, _userFitnessGoal, _targetSteps) { stride, rhr, act, goal, steps ->
            listOf(stride, rhr, act, goal, steps)
        },
        combine(_customTargetCalories, _customTargetProtein, _customTargetCarbs, _customTargetFat, _waterIntakeMl) { cal, prot, carbs, fat, water ->
            listOf(cal, prot, carbs, fat, water)
        }
    ) { p1, p2, p3 ->
        com.example.data.UserProfile(
            name = p1[0] as String,
            age = p1[1] as Int,
            gender = p1[2] as String,
            heightCm = p1[3] as Float,
            weightKg = p1[4] as Float,
            customStrideCm = p2[0] as Float,
            restingHeartRate = p2[1] as Int,
            activityLevel = p2[2] as String,
            fitnessGoal = p2[3] as String,
            targetSteps = p2[4] as Int,
            targetCalories = p3[0] as Int,
            targetProtein = p3[1] as Int,
            targetCarbs = p3[2] as Int,
            targetFat = p3[3] as Int,
            targetWaterMl = p3[4] as Int
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        com.example.data.UserProfile()
    )

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
        _targetSteps.value = prefs.getInt("target_steps", 10000)
        _dailySteps.value = prefs.getInt("steps_log_${getCurrentDateString()}", 0)

        // Collect persistent Room UserProfileEntity data
        viewModelScope.launch {
            repository.userProfile.collect { entity ->
                if (entity != null) {
                    if (entity.name.isNotBlank()) _userName.value = entity.name
                    if (entity.gender.isNotBlank()) _userGender.value = entity.gender
                    if (entity.age > 0) _userAge.value = entity.age
                    if (entity.heightCm > 0f) _userHeightCm.value = entity.heightCm
                    if (entity.weightKg > 0f) _userWeightKg.value = entity.weightKg
                    if (entity.restingHeartRate > 0) _userRestingHeartRate.value = entity.restingHeartRate
                    if (entity.activityLevel.isNotBlank()) _userActivityLevel.value = entity.activityLevel
                    if (entity.fitnessGoal.isNotBlank()) _userFitnessGoal.value = entity.fitnessGoal
                    if (entity.targetSteps > 0) _targetSteps.value = entity.targetSteps
                    if (entity.targetCalories > 0) _customTargetCalories.value = entity.targetCalories
                    if (entity.targetProtein > 0) _customTargetProtein.value = entity.targetProtein
                    if (entity.targetCarbs > 0) _customTargetCarbs.value = entity.targetCarbs
                    if (entity.targetFat > 0) _customTargetFat.value = entity.targetFat
                    if (entity.name.isNotBlank() || entity.age > 0 || entity.heightCm > 0f) {
                        _isOnboardingCompleted.value = true
                    }
                }
            }
        }

        setupStepSensors()
        updateStreakOnAppOpen()

        // Automatically load existing advice, health profile, and Room step activity on date switch
        viewModelScope.launch {
            _selectedDate.collect { date ->
                loadRecommendationForDate(date)
                loadHealthProfileForDate(date)
                repository.getActivityForDate(date).collect { activity ->
                    val prefsSteps = prefs.getInt("steps_log_$date", 0)
                    val roomSteps = activity?.stepCount ?: prefsSteps
                    _dailySteps.value = roomSteps
                }
            }
        }
        // Initialize all trackers directly to zero so user can input custom logs
        clearAllTrackerLogsOnInit()
    }

    private fun updateStreakOnAppOpen() {
        val today = getCurrentDateString()
        val lastActiveDate = prefs.getString("last_active_date", null)
        val currentStreak = prefs.getInt("current_streak", 0)

        if (lastActiveDate == null) {
            // First time using app: Day 1 streak
            _streakCount.value = 1
            prefs.edit()
                .putString("last_active_date", today)
                .putInt("current_streak", 1)
                .apply()
        } else if (lastActiveDate == today) {
            // Same day launch
            _streakCount.value = if (currentStreak <= 0) 1 else currentStreak
        } else {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            try {
                val todayDate = sdf.parse(today)
                val lastDate = sdf.parse(lastActiveDate)
                if (todayDate != null && lastDate != null) {
                    val diffInMillis = todayDate.time - lastDate.time
                    val diffInDays = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
                    if (diffInDays == 1) {
                        // Opened on next consecutive day!
                        val newStreak = currentStreak + 1
                        _streakCount.value = newStreak
                        prefs.edit()
                            .putString("last_active_date", today)
                            .putInt("current_streak", newStreak)
                            .apply()
                    } else if (diffInDays > 1) {
                        // Missed days, reset to Day 1
                        _streakCount.value = 1
                        prefs.edit()
                            .putString("last_active_date", today)
                            .putInt("current_streak", 1)
                            .apply()
                    } else {
                        _streakCount.value = currentStreak.coerceAtLeast(1)
                    }
                } else {
                    _streakCount.value = 1
                }
            } catch (e: Exception) {
                _streakCount.value = 1
            }
        }
    }

    private fun setupStepSensors() {
        try {
            sensorManager = getApplication<Application>().getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            val isTracking = prefs.getBoolean("is_step_tracking_active", true)
            _isStepTrackingActive.value = isTracking
            if (isTracking) {
                registerSensorListeners()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _sensorTypeName.value = "Manual Tracking"
        }
    }

    private fun registerSensorListeners() {
        val sm = sensorManager ?: return

        val counterSensor = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (counterSensor != null) {
            sm.registerListener(this, counterSensor, SensorManager.SENSOR_DELAY_UI)
            _sensorTypeName.value = "Hardware Step Counter"
            return
        }

        val detectorSensor = sm.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        if (detectorSensor != null) {
            sm.registerListener(this, detectorSensor, SensorManager.SENSOR_DELAY_UI)
            _sensorTypeName.value = "Hardware Step Detector"
            return
        }

        val accelSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelSensor != null) {
            sm.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_GAME)
            _sensorTypeName.value = "Accelerometer Step Detector"
            return
        }

        _sensorTypeName.value = "Manual Tracking"
    }

    private fun unregisterSensorListeners() {
        try {
            sensorManager?.unregisterListener(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || !_isStepTrackingActive.value) return
        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val totalStepsSinceBoot = event.values[0]
                if (initialHardwareSteps < 0) {
                    initialHardwareSteps = totalStepsSinceBoot - _dailySteps.value
                }
                val currentCalculated = (totalStepsSinceBoot - initialHardwareSteps).toInt().coerceAtLeast(0)
                if (currentCalculated > _dailySteps.value) {
                    val delta = currentCalculated - _dailySteps.value
                    incrementStepCount(delta)
                }
            }
            Sensor.TYPE_STEP_DETECTOR -> {
                if (event.values.isNotEmpty() && event.values[0] == 1.0f) {
                    incrementStepCount(1)
                }
            }
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val magnitude = kotlin.math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                val now = System.currentTimeMillis()
                if (magnitude > 11.8f && !isAccelAbovePeak && (now - lastAccelStepTime) > 280) {
                    isAccelAbovePeak = true
                    lastAccelStepTime = now
                    incrementStepCount(1)
                } else if (magnitude < 10.0f) {
                    isAccelAbovePeak = false
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun incrementStepCount(amount: Int) {
        if (amount <= 0) return
        val date = _selectedDate.value
        val oldTotal = _dailySteps.value
        val newTotal = (oldTotal + amount).coerceAtLeast(0)
        _dailySteps.value = newTotal
        prefs.edit().putInt("steps_log_$date", newTotal).apply()
        _stepHistoryRefresh.value += 1

        viewModelScope.launch {
            val user = userProfile.value
            val distanceKm = if (user.strideMeters > 0f) (newTotal * user.strideMeters) / 1000f else 0f
            val cals = (newTotal * 0.04f).toInt()
            repository.saveDailyActivity(
                com.example.data.DailyActivityEntity(
                    date = date,
                    stepCount = newTotal,
                    distanceKm = distanceKm,
                    caloriesBurned = cals,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }

        val target = _targetSteps.value
        if (oldTotal < target && newTotal >= target) {
            triggerCelebration(
                title = "Daily Step Goal Hit! 🏆",
                subtitle = "Fantastic work! You smashed your goal of $target steps today!",
                emoji = "🏃‍♂️"
            )
        }
    }

    fun addManualSteps(amount: Int) {
        incrementStepCount(amount)
    }

    fun simulateStepTick(count: Int = 100) {
        incrementStepCount(count)
    }

    fun setTargetSteps(target: Int) {
        val safeTarget = target.coerceAtLeast(1000)
        _targetSteps.value = safeTarget
        prefs.edit().putInt("target_steps", safeTarget).apply()
    }

    fun resetDailySteps() {
        val date = _selectedDate.value
        _dailySteps.value = 0
        initialHardwareSteps = -1f
        prefs.edit().putInt("steps_log_$date", 0).apply()
        _stepHistoryRefresh.value += 1

        viewModelScope.launch {
            repository.saveDailyActivity(
                com.example.data.DailyActivityEntity(
                    date = date,
                    stepCount = 0,
                    distanceKm = 0f,
                    caloriesBurned = 0,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun startStepTracking() {
        _isStepTrackingActive.value = true
        prefs.edit().putBoolean("is_step_tracking_active", true).apply()
        registerSensorListeners()
    }

    fun pauseStepTracking() {
        _isStepTrackingActive.value = false
        prefs.edit().putBoolean("is_step_tracking_active", false).apply()
        unregisterSensorListeners()
    }

    override fun onCleared() {
        super.onCleared()
        unregisterSensorListeners()
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
        _userCustomStrideCm.value = prefs.getFloat("profile_custom_stride", 0f)
        _userAge.value = prefs.getInt("profile_age", 0)
        _userRestingHeartRate.value = prefs.getInt("profile_resting_hr", 0)
    }

    fun setCustomStrideCm(strideCm: Float) {
        val validStride = strideCm.coerceAtLeast(0f)
        _userCustomStrideCm.value = validStride
        prefs.edit().putFloat("profile_custom_stride", validStride).apply()
    }

    fun addWaterIntake(ml: Int) {
        val date = _selectedDate.value
        val current = _waterIntakeMl.value
        val newVal = (current + ml).coerceAtLeast(0)
        _waterIntakeMl.value = newVal
        prefs.edit().putInt("water_ml_$date", newVal).apply()

        val waterTarget = 2500 // 2.5 Liters Goal
        if (current < waterTarget && newVal >= waterTarget) {
            triggerCelebration(
                title = "Hydration Goal Achieved! 💧",
                subtitle = "You reached $newVal ml of water today! Outstanding job staying healthy!",
                emoji = "💦"
            )
        }
    }

    fun resetWaterIntake() {
        val date = _selectedDate.value
        _waterIntakeMl.value = 0
        prefs.edit().putInt("water_ml_$date", 0).apply()
    }

    fun setWaterReminder(enabled: Boolean, intervalHours: Int = 2) {
        _isWaterReminderEnabled.value = enabled
        _waterReminderIntervalHours.value = intervalHours

        prefs.edit()
            .putBoolean("water_reminder_enabled", enabled)
            .putInt("water_reminder_interval", intervalHours)
            .apply()

        val context = getApplication<Application>()
        if (enabled) {
            com.example.WaterReminderScheduler.scheduleWaterReminder(context, intervalHours)
        } else {
            com.example.WaterReminderScheduler.cancelWaterReminder(context)
        }
    }

    fun sendTestWaterNotification() {
        val context = getApplication<Application>()
        com.example.WaterReminderReceiver.showWaterNotification(context)
    }

    fun saveFullUserProfile(
        name: String,
        gender: String,
        age: Int,
        heightCm: Float,
        weightKg: Float,
        restingHr: Int,
        customStrideCm: Float,
        activityLevel: String,
        fitnessGoal: String,
        targetSteps: Int,
        targetCalories: Int,
        targetProtein: Int,
        targetCarbs: Int,
        targetFat: Int
    ) {
        prefs.edit()
            .putBoolean("is_onboarding_completed", true)
            .putString("profile_name", name)
            .putString("profile_gender", gender)
            .putInt("profile_age", age)
            .putFloat("profile_height", heightCm)
            .putFloat("profile_weight", weightKg)
            .putFloat("profile_custom_stride", customStrideCm)
            .putInt("profile_resting_hr", restingHr)
            .putString("profile_activity_level", activityLevel)
            .putString("profile_fitness_goal", fitnessGoal)
            .putInt("target_steps", targetSteps)
            .putInt("target_calories", targetCalories)
            .putInt("target_protein", targetProtein)
            .putInt("target_carbs", targetCarbs)
            .putInt("target_fat", targetFat)
            .apply()

        _isOnboardingCompleted.value = true
        _userName.value = name
        _userGender.value = gender
        _userAge.value = age
        _userHeightCm.value = heightCm
        _userWeightKg.value = weightKg
        _userCustomStrideCm.value = customStrideCm
        _userRestingHeartRate.value = restingHr
        _userActivityLevel.value = activityLevel
        _userFitnessGoal.value = fitnessGoal
        _targetSteps.value = targetSteps
        _customTargetCalories.value = targetCalories
        _customTargetProtein.value = targetProtein
        _customTargetCarbs.value = targetCarbs
        _customTargetFat.value = targetFat
        _weightHistoryRefresh.value += 1

        persistUserProfileToRoom(
            name = name,
            gender = gender,
            age = age,
            heightCm = heightCm,
            weightKg = weightKg,
            restingHr = restingHr,
            activityLevel = activityLevel,
            fitnessGoal = fitnessGoal,
            targetSteps = targetSteps,
            targetCalories = targetCalories,
            targetProtein = targetProtein,
            targetCarbs = targetCarbs,
            targetFat = targetFat
        )

        triggerCelebration(
            title = "Profile Updated! 👤",
            subtitle = "Your height, weight, and fitness targets have been updated!",
            emoji = "✨"
        )
    }

    fun loginWithEmail(email: String, password: String, isSignUp: Boolean, name: String? = null) {
        val defaultName = if (!name.isNullOrBlank()) name else email.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() }
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("user_email", email)
            .putString("auth_provider", "Email")
            .apply()

        if (_userName.value.isBlank()) {
            _userName.value = defaultName
            prefs.edit().putString("profile_name", defaultName).apply()
        }

        _isLoggedIn.value = true
        _userEmail.value = email
        _authProvider.value = "Email"

        triggerCelebration(
            title = if (isSignUp) "Account Created! 🎉" else "Welcome Back! 🔑",
            subtitle = "Signed in as $email",
            emoji = "✨"
        )
    }

    fun loginWithGoogle(email: String, name: String) {
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("user_email", email)
            .putString("auth_provider", "Google")
            .apply()

        if (_userName.value.isBlank() || _userName.value == "User" || _userName.value == "Athlete") {
            _userName.value = name
            prefs.edit().putString("profile_name", name).apply()
        }

        _isLoggedIn.value = true
        _userEmail.value = email
        _authProvider.value = "Google"

        triggerCelebration(
            title = "Google Connected! 🌐",
            subtitle = "Signed in as $name",
            emoji = "🎉"
        )
    }

    fun logout() {
        prefs.edit()
            .putBoolean("is_logged_in", false)
            .apply()

        _isLoggedIn.value = false
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

        persistUserProfileToRoom(
            weightKg = weight,
            heightCm = height,
            age = age,
            restingHr = restingHr,
            targetCalories = targetCal,
            targetProtein = targetProt,
            targetCarbs = targetCarb,
            targetFat = targetFat
        )
    }

    fun persistUserProfileToRoom(
        name: String = _userName.value,
        gender: String = _userGender.value,
        age: Int = _userAge.value,
        heightCm: Float = _userHeightCm.value,
        weightKg: Float = _userWeightKg.value,
        restingHr: Int = _userRestingHeartRate.value,
        activityLevel: String = _userActivityLevel.value,
        fitnessGoal: String = _userFitnessGoal.value,
        targetSteps: Int = _targetSteps.value,
        targetCalories: Int = _customTargetCalories.value,
        targetProtein: Int = _customTargetProtein.value,
        targetCarbs: Int = _customTargetCarbs.value,
        targetFat: Int = _customTargetFat.value
    ) {
        viewModelScope.launch {
            val entity = UserProfileEntity(
                id = 1,
                name = name,
                age = age,
                gender = gender,
                heightCm = heightCm,
                weightKg = weightKg,
                restingHeartRate = restingHr,
                activityLevel = activityLevel,
                fitnessGoal = fitnessGoal,
                targetSteps = targetSteps,
                targetWaterMl = 2500,
                targetCalories = targetCalories,
                targetProtein = targetProtein,
                targetCarbs = targetCarbs,
                targetFat = targetFat,
                updatedAt = System.currentTimeMillis()
            )
            repository.saveUserProfile(entity)
        }
    }

    fun saveDailyWeight(weight: Float) {
        val date = _selectedDate.value
        prefs.edit().putFloat("weight_log_$date", weight).apply()
        _userWeightKg.value = weight
        prefs.edit().putFloat("profile_weight", weight).apply()
        _weightHistoryRefresh.value += 1
        persistUserProfileToRoom(weightKg = weight)
    }

    fun saveWeightForDate(dateKey: String, weight: Float) {
        prefs.edit().putFloat("weight_log_$dateKey", weight).apply()
        val date = _selectedDate.value
        if (dateKey == date) {
            _userWeightKg.value = weight
            prefs.edit().putFloat("profile_weight", weight).apply()
            persistUserProfileToRoom(weightKg = weight)
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
            val oldCalories = dietsForCurrentDate.value.sumOf { it.calories }
            val newCalories = oldCalories + calories

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

            val targetCal = _customTargetCalories.value.let { if (it <= 0) 2000 else it }
            if (oldCalories < targetCal && newCalories >= targetCal) {
                triggerCelebration(
                    title = "Nutrition Target Hit! 🥗",
                    subtitle = "Great job logging your meals! You reached your target of $targetCal kcal today!",
                    emoji = "🎯"
                )
            }
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
        val profile = userProfile.value

        viewModelScope.launch {
            _isGeneratingRecommendation.value = true
            try {
                // Call generative coach with personalized user profile
                val freshAdvice = geminiService.getNutritionAdjustment(workouts, diets, date, profile)
                repository.insertRecommendation(freshAdvice)
                _currentAIRecommendation.value = freshAdvice
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isGeneratingRecommendation.value = false
            }
        }
    }

    fun estimateFoodNutrition(foodName: String, quantity: String, onResult: ((EstimatedFoodNutrition) -> Unit)? = null) {
        if (foodName.isBlank()) return
        viewModelScope.launch {
            _isEstimatingFood.value = true
            try {
                val estimation = geminiService.estimateFoodNutrition(foodName, quantity)
                _lastFoodEstimate.value = estimation
                onResult?.invoke(estimation)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isEstimatingFood.value = false
            }
        }
    }

    fun clearFoodEstimate() {
        _lastFoodEstimate.value = null
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
            val repository = FitnessRepository(
                com.example.data.DatabaseModule.provideFitnessDao(application),
                com.example.data.DatabaseModule.provideUserProfileDao(application),
                com.example.data.DatabaseModule.provideDailyActivityDao(application)
            )
            @Suppress("UNCHECKED_CAST")
            return FitnessViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
