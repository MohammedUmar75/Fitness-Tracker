package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.FitnessViewModel
import com.example.ui.FitnessViewModelFactory
import com.example.ui.screens.AiCoachTab
import com.example.ui.screens.DashboardTab
import com.example.ui.screens.FootstepsTab
import com.example.ui.screens.TrackersTab
import com.example.ui.screens.WellnessToolsTab
import com.example.ui.screens.ProfileTab
import com.example.ui.components.ConfettiOverlay
import com.example.ui.components.OnboardingDialog
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                FitnessAppMainScreen()
            }
        }
    }
}

@Composable
fun FitnessAppMainScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val application = context.applicationContext as android.app.Application
    val viewModel: FitnessViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = FitnessViewModelFactory(application)
    )

    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val workouts by viewModel.workoutsForCurrentDate.collectAsStateWithLifecycle()
    val diets by viewModel.dietsForCurrentDate.collectAsStateWithLifecycle()
    val allWorkouts by viewModel.allWorkouts.collectAsStateWithLifecycle()
    val allDiets by viewModel.allDiets.collectAsStateWithLifecycle()
    val aiAdvice by viewModel.currentAIRecommendation.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGeneratingRecommendation.collectAsStateWithLifecycle()
    val nutritionAnalysis by viewModel.currentNutritionAnalysis.collectAsStateWithLifecycle()
    val isAnalyzingNutrition by viewModel.isAnalyzingNutrition.collectAsStateWithLifecycle()

    val waterIntakeMl by viewModel.waterIntakeMl.collectAsStateWithLifecycle()
    val customTargetCalories by viewModel.customTargetCalories.collectAsStateWithLifecycle()
    val customTargetProtein by viewModel.customTargetProtein.collectAsStateWithLifecycle()
    val customTargetCarbs by viewModel.customTargetCarbs.collectAsStateWithLifecycle()
    val customTargetFat by viewModel.customTargetFat.collectAsStateWithLifecycle()
    val userWeightKg by viewModel.userWeightKg.collectAsStateWithLifecycle()
    val userHeightCm by viewModel.userHeightCm.collectAsStateWithLifecycle()
    val userAge by viewModel.userAge.collectAsStateWithLifecycle()
    val userRestingHeartRate by viewModel.userRestingHeartRate.collectAsStateWithLifecycle()
    val weightHistory by viewModel.weightHistory.collectAsStateWithLifecycle()

    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()
    val authProvider by viewModel.authProvider.collectAsStateWithLifecycle()
    var showAuthDialog by remember { mutableStateOf(false) }
    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsStateWithLifecycle()
    val dailySteps by viewModel.dailySteps.collectAsStateWithLifecycle()
    val targetSteps by viewModel.targetSteps.collectAsStateWithLifecycle()
    val isStepTrackingActive by viewModel.isStepTrackingActive.collectAsStateWithLifecycle()
    val sensorTypeName by viewModel.sensorTypeName.collectAsStateWithLifecycle()
    val stepHistory by viewModel.stepHistory.collectAsStateWithLifecycle()

    val isEstimatingFood by viewModel.isEstimatingFood.collectAsStateWithLifecycle()
    val streakCount by viewModel.streakCount.collectAsStateWithLifecycle()
    val isWaterReminderEnabled by viewModel.isWaterReminderEnabled.collectAsStateWithLifecycle()
    val waterReminderIntervalHours by viewModel.waterReminderIntervalHours.collectAsStateWithLifecycle()
    val celebrationEvent by viewModel.celebrationEvent.collectAsStateWithLifecycle()

    val isStepGoalMet = dailySteps >= targetSteps && targetSteps > 0
    val isWaterGoalMet = waterIntakeMl >= 2500
    val totalCaloriesConsumed = diets.sumOf { it.calories }
    val effectiveTargetCalories = if (customTargetCalories <= 0) 2000 else customTargetCalories
    val isCalorieGoalMet = totalCaloriesConsumed >= effectiveTargetCalories
    val isAnyGoalMet = isStepGoalMet || isWaterGoalMet || isCalorieGoalMet

    var activeTab by remember { mutableStateOf("DASHBOARD") }

    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Surface(
                color = colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = colorScheme.primaryContainer
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = "Fitness Tracker Logo",
                                tint = colorScheme.primary,
                                modifier = Modifier.padding(8.dp).size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Fitness Tracker",
                                fontSize = 18.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = colorScheme.onSurface
                            )
                            Text(
                                text = selectedDate,
                                fontSize = 11.sp,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)
                    ) {
                        if (streakCount >= 2) {
                            Surface(
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                                color = colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("🔥", fontSize = 12.sp)
                                    Text(
                                        text = "$streakCount Day Streak",
                                        fontSize = 11.sp,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        color = colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = { activeTab = "PROFILE" },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = if (activeTab == "PROFILE") Icons.Filled.Person else Icons.Outlined.Person,
                                contentDescription = "Profile",
                                tint = if (activeTab == "PROFILE") colorScheme.primary else colorScheme.onSurfaceVariant
                            )
                        }

                        if (isAnyGoalMet) {
                            IconButton(
                                onClick = {
                                    val (title, subtitle, emoji) = when {
                                        isStepGoalMet -> Triple("Step Goal Hit! 🏃‍♂️", "You've completed your $targetSteps step target!", "🏆")
                                        isWaterGoalMet -> Triple("Hydration Goal Met! 💧", "You've reached $waterIntakeMl ml of water today!", "💦")
                                        else -> Triple("Calorie Goal Reached! 🍽️", "You've met your daily $effectiveTargetCalories kcal target!", "🎯")
                                    }
                                    viewModel.triggerCelebration(
                                        title = title,
                                        subtitle = subtitle,
                                        emoji = emoji
                                    )
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Text("🎉", fontSize = 18.sp)
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = activeTab == "DASHBOARD",
                    onClick = { activeTab = "DASHBOARD" },
                    label = { Text("Dashboard", style = MaterialTheme.typography.labelMedium) },
                    icon = {
                        Icon(
                            imageVector = if (activeTab == "DASHBOARD") Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                            contentDescription = "Dashboard"
                        )
                    }
                )
                NavigationBarItem(
                    selected = activeTab == "FOOTSTEPS",
                    onClick = { activeTab = "FOOTSTEPS" },
                    label = { Text("Footsteps", style = MaterialTheme.typography.labelMedium) },
                    icon = {
                        Icon(
                            imageVector = if (activeTab == "FOOTSTEPS") Icons.Filled.DirectionsWalk else Icons.Outlined.DirectionsWalk,
                            contentDescription = "Footsteps"
                        )
                    }
                )
                NavigationBarItem(
                    selected = activeTab == "TRACKERS",
                    onClick = { activeTab = "TRACKERS" },
                    label = { Text("Trackers Log", style = MaterialTheme.typography.labelMedium) },
                    icon = {
                        Icon(
                            imageVector = if (activeTab == "TRACKERS") Icons.Filled.ListAlt else Icons.Outlined.ListAlt,
                            contentDescription = "Trackers"
                        )
                    }
                )
                NavigationBarItem(
                    selected = activeTab == "TOOLS",
                    onClick = { activeTab = "TOOLS" },
                    label = { Text("Calculators", style = MaterialTheme.typography.labelMedium) },
                    icon = {
                        Icon(
                            imageVector = if (activeTab == "TOOLS") Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Tools"
                        )
                    }
                )
                NavigationBarItem(
                    selected = activeTab == "COACH",
                    onClick = { activeTab = "COACH" },
                    label = { Text("AI Coach", style = MaterialTheme.typography.labelMedium) },
                    icon = {
                        Icon(
                            imageVector = if (activeTab == "COACH") Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                            contentDescription = "AI Coach"
                        )
                    }
                )
                NavigationBarItem(
                    selected = activeTab == "PROFILE",
                    onClick = { activeTab = "PROFILE" },
                    label = { Text("Profile", style = MaterialTheme.typography.labelMedium) },
                    icon = {
                        Icon(
                            imageVector = if (activeTab == "PROFILE") Icons.Filled.Person else Icons.Outlined.Person,
                            contentDescription = "Profile"
                        )
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = colorScheme.background
            ) {
                when (activeTab) {
                    "DASHBOARD" -> {
                        DashboardTab(
                            workouts = workouts,
                            diets = diets,
                            allWorkouts = allWorkouts,
                            allDiets = allDiets,
                            aiAdvice = aiAdvice,
                            userProfile = userProfile,
                            onNavigateToCoach = { activeTab = "COACH" },
                            onNavigateToProfile = { activeTab = "PROFILE" },
                            customTargetCalories = customTargetCalories,
                            customTargetProtein = customTargetProtein,
                            customTargetCarbs = customTargetCarbs,
                            customTargetFat = customTargetFat,
                            waterIntakeMl = waterIntakeMl,
                            onAddWater = { viewModel.addWaterIntake(it) },
                            onResetWater = { viewModel.resetWaterIntake() },
                            dailySteps = dailySteps,
                            targetSteps = targetSteps,
                            onNavigateToFootsteps = { activeTab = "FOOTSTEPS" }
                        )
                    }
                    "FOOTSTEPS" -> {
                        FootstepsTab(
                            dailySteps = dailySteps,
                            targetSteps = targetSteps,
                            isStepTrackingActive = isStepTrackingActive,
                            sensorTypeName = sensorTypeName,
                            stepHistory = stepHistory,
                            userWeightKg = userWeightKg,
                            userHeightCm = userHeightCm,
                            customStrideCm = userProfile.customStrideCm,
                            onStartTracking = { viewModel.startStepTracking() },
                            onPauseTracking = { viewModel.pauseStepTracking() },
                            onSetTargetSteps = { viewModel.setTargetSteps(it) },
                            onSetCustomStrideCm = { viewModel.setCustomStrideCm(it) },
                            onAddManualSteps = { viewModel.addManualSteps(it) },
                            onSimulateStepTick = { viewModel.simulateStepTick(it) },
                            onResetDailySteps = { viewModel.resetDailySteps() },
                            onTriggerCelebration = { title, subtitle, emoji ->
                                viewModel.triggerCelebration(title, subtitle, emoji)
                            }
                        )
                    }
                "TRACKERS" -> {
                    TrackersTab(
                        selectedDate = selectedDate,
                        workouts = workouts,
                        diets = diets,
                        waterIntakeMl = waterIntakeMl,
                        isWaterReminderEnabled = isWaterReminderEnabled,
                        waterReminderIntervalHours = waterReminderIntervalHours,
                        onSetWaterReminder = { enabled, interval ->
                            viewModel.setWaterReminder(enabled, interval)
                        },
                        onSendTestWaterNotification = {
                            viewModel.sendTestWaterNotification()
                        },
                        isEstimatingFood = isEstimatingFood,
                        onEstimateFood = { foodName, quantity, onResult ->
                            viewModel.estimateFoodNutrition(foodName, quantity, onResult)
                        },
                        onAddWater = { viewModel.addWaterIntake(it) },
                        onResetWater = { viewModel.resetWaterIntake() },
                        onSelectDate = { viewModel.selectDate(it) },
                        onAddWorkout = { name, duration, intensity, burned ->
                            viewModel.addWorkout(name, duration, intensity, burned)
                        },
                        onDeleteWorkout = { viewModel.deleteWorkout(it) },
                        onAddDiet = { meal, name, cal, prot, carb, fat ->
                            viewModel.addDiet(meal, name, cal, prot, carb, fat)
                        },
                        onDeleteDiet = { viewModel.deleteDiet(it) },
                        onClearAllLogs = { viewModel.clearAllTrackerLogs() }
                    )
                }
                "TOOLS" -> {
                    WellnessToolsTab(
                        weightKg = userWeightKg,
                        heightCm = userHeightCm,
                        age = userAge,
                        restingHr = userRestingHeartRate,
                        gender = userProfile.gender,
                        onSaveProfile = { w, h, a, hr, targetC, targetP, targetCr, targetF ->
                            viewModel.saveHealthProfile(w, h, a, hr, targetC, targetP, targetCr, targetF)
                        },
                        onApplyPreset = { presetName ->
                            viewModel.applyPresetRoutine(presetName)
                        },
                        weightHistory = weightHistory,
                        onSaveWeightForDate = { dateKey, weight ->
                            viewModel.saveWeightForDate(dateKey, weight)
                        },
                        allWorkouts = allWorkouts,
                        allDiets = allDiets
                    )
                }
                "COACH" -> {
                    AiCoachTab(
                        selectedDate = selectedDate,
                        advice = aiAdvice,
                        isGenerating = isGenerating,
                        onTriggerAdjustment = { viewModel.generateAIRecommendation() },
                        nutritionAnalysis = nutritionAnalysis,
                        isAnalyzingNutrition = isAnalyzingNutrition,
                        onTriggerNutritionAnalysis = { viewModel.generateNutritionAnalysis() }
                    )
                }
                "PROFILE" -> {
                    ProfileTab(
                        userProfile = userProfile,
                        dailySteps = dailySteps,
                        onSaveFullProfile = { name, gender, age, height, weight, customStride, act, goal, steps, cal, prot, carbs, fat ->
                            viewModel.saveFullUserProfile(name, gender, age, height, weight, 0, customStride, act, goal, steps, cal, prot, carbs, fat)
                        }
                    )
                }
            }
        }

        ConfettiOverlay(
            event = celebrationEvent,
            onDismiss = { viewModel.clearCelebrationEvent() }
        )

        if (showAuthDialog) {
            com.example.ui.components.AuthDialog(
                onEmailAuth = { email, password, isSignUp, name ->
                    viewModel.loginWithEmail(email, password, isSignUp, name)
                    showAuthDialog = false
                },
                onGoogleAuth = { email, name ->
                    viewModel.loginWithGoogle(email, name)
                    showAuthDialog = false
                },
                onDismiss = { showAuthDialog = false }
            )
        } else if (!isOnboardingCompleted) {
            OnboardingDialog(
                onComplete = { name, gender, age, height, weight, restingHr, act, goal, steps, cal, prot, carbs, fat ->
                    viewModel.saveFullUserProfile(name, gender, age, height, weight, 0, 0f, act, goal, steps, cal, prot, carbs, fat)
                },
                onDismiss = { viewModel.completeOnboardingWithoutSave() }
            )
        }
    }
}
}
