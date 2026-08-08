package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.FitnessViewModel
import com.example.ui.FitnessViewModelFactory
import com.example.ui.screens.AiCoachTab
import com.example.ui.screens.DashboardTab
import com.example.ui.screens.TrackersTab
import com.example.ui.screens.WellnessToolsTab
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

    var activeTab by remember { mutableStateOf("DASHBOARD") }

    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        modifier = Modifier.fillMaxSize(),
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
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
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
                        onNavigateToCoach = { activeTab = "COACH" },
                        customTargetCalories = customTargetCalories,
                        customTargetProtein = customTargetProtein,
                        customTargetCarbs = customTargetCarbs,
                        customTargetFat = customTargetFat,
                        waterIntakeMl = waterIntakeMl,
                        onAddWater = { viewModel.addWaterIntake(it) },
                        onResetWater = { viewModel.resetWaterIntake() }
                    )
                }
                "TRACKERS" -> {
                    TrackersTab(
                        selectedDate = selectedDate,
                        workouts = workouts,
                        diets = diets,
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
            }
        }
    }
}
