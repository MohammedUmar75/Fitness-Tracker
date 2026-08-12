package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.DietIntake
import com.example.data.WorkoutProgress
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WellnessToolsTab(
    weightKg: Float,
    heightCm: Float,
    age: Int,
    restingHr: Int,
    gender: String = "",
    onSaveProfile: (weight: Float, height: Float, age: Int, restingHr: Int, targetCal: Int, targetProt: Int, targetCarb: Int, targetFat: Int) -> Unit,
    onApplyPreset: (String) -> Unit,
    weightHistory: List<Triple<String, String, Float>>,
    onSaveWeightForDate: (String, Float) -> Unit,
    allWorkouts: List<WorkoutProgress>,
    allDiets: List<DietIntake>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Upper sub-tab toggle state: 0 = Performance Analytics, 1 = Body Calculators
    var selectedSubTab by remember { mutableStateOf(0) }

    // Dialog state for quick weight editor
    var isWeightDiaryOpen by remember { mutableStateOf(false) }
    var selectedWeightDiaryDate by remember { mutableStateOf("") }
    var selectedWeightDiaryLabel by remember { mutableStateOf("") }
    var weightInputForDiary by remember { mutableStateOf("") }

    // User inputs for basic BMR / BMI Calculator
    var weightInput by remember(weightKg) { mutableStateOf(if (weightKg <= 0f) "" else if (weightKg % 1f == 0f) weightKg.toInt().toString() else weightKg.toString()) }
    var heightInput by remember(heightCm) { mutableStateOf(if (heightCm <= 0f) "" else if (heightCm % 1f == 0f) heightCm.toInt().toString() else heightCm.toString()) }
    var ageInput by remember(age) { mutableStateOf(if (age <= 0) "" else age.toString()) }
    var restingHrInput by remember(restingHr) { mutableStateOf(if (restingHr <= 0) "" else restingHr.toString()) }

    var selectedGender by remember(gender) { mutableStateOf(gender) }
    var selectedActivityLevel by remember { mutableStateOf("Moderate") }

    val activityMultipliers = mapOf(
        "Sedentary" to 1.2f,
        "Light" to 1.375f,
        "Moderate" to 1.55f,
        "Active" to 1.725f,
        "Extreme" to 1.9f
    )

    // Mifflin-St Jeor computations
    val wVal = weightInput.toFloatOrNull() ?: 0f
    val hVal = heightInput.toFloatOrNull() ?: 0f
    val aVal = ageInput.toIntOrNull() ?: 0
    val hrVal = restingHrInput.toIntOrNull() ?: 0

    val bmi = if (hVal > 0f && wVal > 0f) wVal / ((hVal / 100f) * (hVal / 100f)) else 0f
    val bmr = if (wVal <= 0f || hVal <= 0f || aVal <= 0 || selectedGender.isBlank()) 0f else if (selectedGender.equals("Female", ignoreCase = true)) {
        (10 * wVal) + (6.25f * hVal) - (5 * aVal) - 161
    } else {
        (10 * wVal) + (6.25f * hVal) - (5 * aVal) + 5
    }

    val multiplier = activityMultipliers[selectedActivityLevel] ?: 1.55f
    val tdee = if (bmr > 0f) bmr * multiplier else 0f

    // Macro budgets
    val targetCaloriesComputed = tdee.toInt()
    val targetProteinComputed = ((tdee * 0.30f) / 4).toInt()
    val targetCarbsComputed = ((tdee * 0.45f) / 4).toInt()
    val targetFatComputed = ((tdee * 0.25f) / 9).toInt()

    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // High-End Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Wellness Desk",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        modifier = Modifier.testTag("wellness_title")
                    )
                    Text(
                        text = "Dynamic tracking charts, weight indices, and calorie analytics.",
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = "Analytics logo",
                    tint = colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Sub-tabs configuration (Segmented Pill Switcher)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    SubTabItem(0, "Performance Trends", Icons.Default.TrendingUp),
                    SubTabItem(1, "Calculator Tools", Icons.Default.Calculate)
                ).forEach { item ->
                    val isSelected = selectedSubTab == item.index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) colorScheme.primary else Color.Transparent)
                            .clickable { selectedSubTab = item.index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) colorScheme.onPrimary else colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = item.label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) colorScheme.onPrimary else colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            AnimatedContent(
                targetState = selectedSubTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "SubtabTransition"
            ) { tab ->
                when (tab) {
                    0 -> {
                        // Tab 1: Performance Trends Dashboard (Weight Curve, Dual Calorie Balance, Workout Intensities)
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            
                            // 1. Performance Overview Row Cards
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Average Weight Card
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text("Average Weight", fontSize = 11.sp, color = colorScheme.onSurfaceVariant)
                                        val allWeights = weightHistory.map { it.third }
                                        val avgWeight = if (allWeights.isNotEmpty()) allWeights.average() else weightKg.toDouble()
                                        Text("%.1f kg".format(avgWeight), fontSize = 18.sp, fontWeight = FontWeight.Black, color = colorScheme.primary)
                                        Text("Dynamic trends", fontSize = 9.sp, color = colorScheme.outline)
                                    }
                                }

                                // Total Active Workouts
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text("Weekly Workouts", fontSize = 11.sp, color = colorScheme.onSurfaceVariant)
                                        val recentWorkouts = allWorkouts.size
                                        Text("$recentWorkouts sessions", fontSize = 18.sp, fontWeight = FontWeight.Black, color = colorScheme.secondary)
                                        Text("Completed log", fontSize = 9.sp, color = colorScheme.outline)
                                    }
                                }
                            }

                            // 2. Beautiful Weight Curve Area Plot (Interactive)
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Timeline,
                                                contentDescription = "Weight diary graph",
                                                tint = colorScheme.primary
                                            )
                                            Text("Weekly Weight Variance", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                                        }

                                        Text(
                                            text = "Tap Day to Update",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = colorScheme.primary,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Weight Bezier Curve Drawing Component
                                    WeightBezierCurve(weightHistory = weightHistory)

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Day buttons for weight logging
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        weightHistory.forEach { entry ->
                                            val (dateKey, label, weight) = entry
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        selectedWeightDiaryDate = dateKey
                                                        selectedWeightDiaryLabel = label
                                                        weightInputForDiary = weight.toString()
                                                        isWeightDiaryOpen = true
                                                    }
                                                    .padding(vertical = 4.dp)
                                            ) {
                                                Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurfaceVariant)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(colorScheme.primary.copy(alpha = 0.8f))
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text("%.1f".format(weight), fontSize = 10.sp, color = colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                }
                            }

                            // 3. Weekly Deficit & Calorie Intake Graph
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Equalizer,
                                            contentDescription = "Energy chart",
                                            tint = colorScheme.secondary
                                        )
                                        Text("Caloric Surplus vs Burnout", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                    
                                    // Visual Legend
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        LegendIndicator(color = colorScheme.primary, label = "Consumed (In)")
                                        LegendIndicator(color = colorScheme.tertiary, label = "Active Burn (Out)")
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Energy comparative drawing
                                    WeeklyEnergyBalanceChart(workouts = allWorkouts, diets = allDiets)
                                }
                            }

                            // 4. Intensity frequency Breakdown capsules
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OfflineBolt,
                                            contentDescription = "Intensity logo",
                                            tint = colorScheme.tertiary
                                        )
                                        Text("Intensity Distribution (7 Days)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                                    }

                                    Text(
                                        text = "Workout routines cataloged by structural cardiovascular intensity brackets:",
                                        fontSize = 11.sp,
                                        color = colorScheme.onSurfaceVariant
                                    )

                                    // Intensity counters
                                    val totalIntensityLogs = allWorkouts.size
                                    val highCount = allWorkouts.count { it.intensity.lowercase() == "high" }
                                    val medCount = allWorkouts.count { it.intensity.lowercase() == "medium" }
                                    val lowCount = allWorkouts.count { it.intensity.lowercase() == "low" }

                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        IntensityProgressRow(
                                            title = "High Impact Intensity",
                                            count = highCount,
                                            total = totalIntensityLogs,
                                            color = Color(0xFFC21807)
                                        )
                                        IntensityProgressRow(
                                            title = "Moderate Intensity",
                                            count = medCount,
                                            total = totalIntensityLogs,
                                            color = Color(0xFFD4AF37)
                                        )
                                        IntensityProgressRow(
                                            title = "Active Recovery / Low",
                                            count = lowCount,
                                            total = totalIntensityLogs,
                                            color = Color(0xFF008080)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // Tab 2: Standard Mifflin-St Jeor calculators + Karvonen heart zones
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                            // Quick Preset Importer Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DirectionsRun,
                                            contentDescription = "Quick Routines",
                                            tint = colorScheme.primary
                                        )
                                        Text(
                                            text = "Instant Routine Seeder",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.onSecondaryContainer
                                        )
                                    }
                                    Text(
                                        text = "Instantly pre-populate workouts & customized matching meal intakes to today's active tracker loops:",
                                        fontSize = 11.sp,
                                        color = colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    val presets = listOf("HIIT Cardio Shred", "Power Strength Builder", "Zen Yoga & Mindful Recovery", "Lean Metabolic Burner")
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        presets.forEach { presetName ->
                                            SuggestionChip(
                                                onClick = {
                                                    onApplyPreset(presetName)
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar("Applied Preset Plan: $presetName")
                                                    }
                                                },
                                                label = { Text(presetName, fontSize = 11.sp) },
                                                modifier = Modifier.testTag("preset_${presetName.replace(" ", "_").lowercase()}")
                                            )
                                        }
                                    }
                                }
                            }

                            // BMI and BMR Calculator Configuration
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.HelpOutline,
                                            contentDescription = "Calculators",
                                            tint = colorScheme.secondary
                                        )
                                        Text(
                                            text = "Interactive BMI & Daily Caloric Estimator",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.onSurface
                                        )
                                    }

                                    // Input Grid
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = weightInput,
                                            onValueChange = { weightInput = it },
                                            label = { Text("Weight (kg)") },
                                            placeholder = { Text("Weight") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("weight_input"),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        OutlinedTextField(
                                            value = heightInput,
                                            onValueChange = { heightInput = it },
                                            label = { Text("Height (cm)") },
                                            placeholder = { Text("Height") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("height_input"),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = ageInput,
                                            onValueChange = { ageInput = it },
                                            label = { Text("Age (yrs)") },
                                            placeholder = { Text("Age") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("age_input"),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }

                                    // Gender selector
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("Gender Identification", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurfaceVariant)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            ElevatedFilterChip(
                                                selected = selectedGender == "Male",
                                                onClick = { selectedGender = "Male" },
                                                label = { Text("Male") },
                                                modifier = Modifier.weight(1f)
                                            )
                                            ElevatedFilterChip(
                                                selected = selectedGender == "Female",
                                                onClick = { selectedGender = "Female" },
                                                label = { Text("Female") },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }

                                    // Activity Level Selector
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("Weekly Activity Level Multiplier", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurfaceVariant)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            listOf("Sedentary", "Moderate", "Active").forEach { act ->
                                                FilterChip(
                                                    selected = selectedActivityLevel == act,
                                                    onClick = { selectedActivityLevel = act },
                                                    label = { Text(act, fontSize = 10.sp) },
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                    }

                                    Divider(color = colorScheme.surfaceVariant.copy(alpha = 0.5f))

                                    // Calculations Output Visual: BMI Gauge
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Body Mass Index (BMI)",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        BmiGaugeSweep(bmi = bmi)

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Text Category Details
                                        val (bmiCategory, bmiColor) = when {
                                            bmi < 18.5f -> "Underweight" to Color(0xFF1F75FE)
                                            bmi < 25f -> "Healthy weight" to Color(0xFF008080)
                                            bmi < 30f -> "Overweight" to Color(0xFFD4AF37)
                                            else -> "Obese bracket" to Color(0xFFC21807)
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(bmiColor)
                                            )
                                            Text(
                                                text = "BMI: %.2f (%s)".format(bmi, bmiCategory),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Black,
                                                color = colorScheme.onSurface
                                            )
                                        }
                                    }

                                    Divider(color = colorScheme.surfaceVariant.copy(alpha = 0.5f))

                                    // BMR & Energy outputs
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Basal Metabolic Rate", fontSize = 11.sp, color = colorScheme.onSurfaceVariant)
                                            Text("${bmr.toInt()} kcal", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colorScheme.secondary)
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Total Energy (TDEE)", fontSize = 11.sp, color = colorScheme.onSurfaceVariant)
                                            Text("${tdee.toInt()} kcal", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colorScheme.primary)
                                        }
                                    }

                                    // Set target controls button
                                    Button(
                                        onClick = {
                                            onSaveProfile(
                                                wVal,
                                                hVal,
                                                aVal,
                                                0,
                                                targetCaloriesComputed,
                                                targetProteinComputed,
                                                targetCarbsComputed,
                                                targetFatComputed
                                            )
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Daily budget targets set to ${targetCaloriesComputed} kcal!")
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("apply_bmr_targets_btn"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Save,
                                            contentDescription = "Save Target",
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Apply TDEE Balanced Diet Budget Today", fontSize = 13.sp)
                                    }
                                }
                            }

                            // Target Heart Rate Zones Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FavoriteBorder,
                                            contentDescription = "Heart rate",
                                            tint = Color(0xFFC21807)
                                        )
                                        Text(
                                            text = "Target Training Heart Rate Zones",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.onSurface
                                        )
                                    }

                                    val maxHr = if (aVal > 0) (220 - aVal) else 195

                                    Text(
                                        text = "Calculated based on standard Maximum Heart Rate (220 - Age = $maxHr bpm):",
                                        fontSize = 11.sp,
                                        color = colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    val zones = listOf(
                                        HeartRateZoneInfo("Recover / Light", 0.5f, 0.6f, Color(0xFF008080), "Warmup, endurance preparation and recovery stimulation."),
                                        HeartRateZoneInfo("Fat Burn Flow", 0.6f, 0.7f, Color(0xFFD4AF37), "Maximum caloric burning ratio from fat stores."),
                                        HeartRateZoneInfo("Aerobic Fitness", 0.7f, 0.8f, Color(0xFFE97451), "Improves cardiovascular conditioning & stamina resources."),
                                        HeartRateZoneInfo("Peak Anaerobic", 0.8f, 0.9f, Color(0xFFC21807), "Increases lactic acid tolerance and raw muscle endurance.")
                                    )

                                    zones.forEach { zone ->
                                        val lowerBpm = (maxHr * zone.lowerLimit).toInt()
                                        val uperBpm = (maxHr * zone.upperLimit).toInt()

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .padding(top = 4.dp)
                                                    .clip(CircleShape)
                                                    .background(zone.color)
                                            )

                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(zone.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                                                    Text("$lowerBpm - $uperBpm bpm", fontSize = 11.sp, fontWeight = FontWeight.Black, color = zone.color)
                                                }
                                                Text(zone.description, fontSize = 10.sp, color = colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Weight Logger Dialog
    if (isWeightDiaryOpen) {
        Dialog(onDismissRequest = { isWeightDiaryOpen = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Log Day Weight ($selectedWeightDiaryLabel)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Document your weight in kilograms for $selectedWeightDiaryDate to compute beautiful analytic plots.",
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = weightInputForDiary,
                        onValueChange = { weightInputForDiary = it },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("diary_weight_field"),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { isWeightDiaryOpen = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                val enteredFloat = weightInputForDiary.toFloatOrNull()
                                if (enteredFloat != null && enteredFloat > 0) {
                                    onSaveWeightForDate(selectedWeightDiaryDate, enteredFloat)
                                    isWeightDiaryOpen = false
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Logged $enteredFloat kg for $selectedWeightDiaryLabel!")
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_diary_weight_btn")
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

data class SubTabItem(val index: Int, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
fun LegendIndicator(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun IntensityProgressRow(title: String, count: Int, total: Int, color: Color) {
    val progress = if (total > 0) count.toFloat() / total.toFloat() else 0f
    val colorScheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
            Text("$count / ${total.coerceAtLeast(0)} logs", fontSize = 10.sp, fontWeight = FontWeight.Black, color = color)
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = colorScheme.surfaceVariant
        )
    }
}

@Composable
fun WeightBezierCurve(
    weightHistory: List<Triple<String, String, Float>>,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    val weights = weightHistory.map { it.third }
    val maxWeight = (weights.maxOrNull() ?: 70f) + 2f
    val minWeight = (weights.minOrNull() ?: 70f) - 2f
    val weightRange = (maxWeight - minWeight).coerceAtLeast(1f)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .background(colorScheme.primary.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
            .border(0.5.dp, colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
    ) {
        val width = size.width
        val height = size.height
        val paddingHorizontal = 16.dp.toPx()
        val paddingVertical = 20.dp.toPx()

        val usableWidth = width - (paddingHorizontal * 2)
        val usableHeight = height - (paddingVertical * 2)

        if (weightHistory.size < 2) return@Canvas

        val stepX = usableWidth / (weightHistory.size - 1)
        val points = weightHistory.mapIndexed { index, triple ->
            val w = triple.third
            val x = paddingHorizontal + index * stepX
            val py = paddingVertical + usableHeight - ((w - minWeight) / weightRange) * usableHeight
            Offset(x, py)
        }

        // 1. Draw smooth fill area path underneath
        val fillPath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                val prev = points[i - 1]
                val curr = points[i]
                val cp1X = prev.x + (curr.x - prev.x) / 2f
                val cp1Y = prev.y
                val cp2X = prev.x + (curr.x - prev.x) / 2f
                val cp2Y = curr.y
                cubicTo(cp1X, cp1Y, cp2X, cp2Y, curr.x, curr.y)
            }
            lineTo(points.last().x, height)
            lineTo(points.first().x, height)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(colorScheme.primary.copy(alpha = 0.2f), Color.Transparent),
                startY = 0f,
                endY = height
            )
        )

        // 2. Draw smooth stroke curve
        val strokePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                val prev = points[i - 1]
                val curr = points[i]
                val cp1X = prev.x + (curr.x - prev.x) / 2f
                val cp1Y = prev.y
                val cp2X = prev.x + (curr.x - prev.x) / 2f
                val cp2Y = curr.y
                cubicTo(cp1X, cp1Y, cp2X, cp2Y, curr.x, curr.y)
            }
        }

        drawPath(
            path = strokePath,
            color = colorScheme.primary,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // 3. Draw glowing vertices nodes
        points.forEach { pt ->
            // Outer halo
            drawCircle(
                color = colorScheme.primary.copy(alpha = 0.3f),
                radius = 6.dp.toPx(),
                center = pt
            )
            // Inner core
            drawCircle(
                color = colorScheme.background,
                radius = 3.dp.toPx(),
                center = pt
            )
            drawCircle(
                color = colorScheme.primary,
                radius = 2.dp.toPx(),
                center = pt
            )
        }
    }
}

@Composable
fun WeeklyEnergyBalanceChart(
    workouts: List<WorkoutProgress>,
    diets: List<DietIntake>,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val days = (6 downTo 0).map { offset ->
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -offset)
        cal.time
    }

    val dailyData = days.map { date ->
        val dateKey = simpleDateFormat.format(date)
        val consumed = diets.filter { it.date == dateKey }.sumOf { it.calories }.toFloat()
        val burned = workouts.filter { it.date == dateKey }.sumOf { it.caloriesBurned }.toFloat()
        consumed to burned
    }

    val maxVal = dailyData.maxOfOrNull { maxOf(it.first, it.second, 1200f) } ?: 2000f

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .background(colorScheme.secondary.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
            .border(0.5.dp, colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
    ) {
        val width = size.width
        val height = size.height
        val sliceWidth = width / 7f
        val barWidth = sliceWidth * 0.28f

        // Draw horizontal threshold marker
        drawLine(
            color = colorScheme.outlineVariant.copy(alpha = 0.3f),
            start = Offset(0f, height * 0.5f),
            end = Offset(width, height * 0.5f),
            strokeWidth = 1.dp.toPx()
        )

        dailyData.forEachIndexed { index, (consumed, burned) ->
            val centerX = (index * sliceWidth) + (sliceWidth / 2f)

            // 1. Food Consumed bar (Primary)
            val cHeight = (consumed / maxVal) * (height - 24.dp.toPx())
            if (cHeight > 4f) {
                drawRoundRect(
                    color = colorScheme.primary,
                    topLeft = Offset(centerX - barWidth - 2.dp.toPx(), height - cHeight - 12.dp.toPx()),
                    size = Size(barWidth, cHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }

            // 2. Active Burned energy bar (Tertiary)
            val bHeight = (burned / maxVal) * (height - 24.dp.toPx())
            if (bHeight > 4f) {
                drawRoundRect(
                    color = colorScheme.tertiary,
                    topLeft = Offset(centerX + 2.dp.toPx(), height - bHeight - 12.dp.toPx()),
                    size = Size(barWidth, bHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }
        }
    }
}

data class HeartRateZoneInfo(
    val name: String,
    val lowerLimit: Float,
    val upperLimit: Float,
    val color: Color,
    val description: String
)

@Composable
fun BmiGaugeSweep(bmi: Float) {
    val colorScheme = MaterialTheme.colorScheme
    val normalizedBmiProgress = ((bmi - 15f) / 20f).coerceIn(0f, 1f)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(180.dp, 100.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val arcSize = Size(size.width - strokeWidth, (size.height * 2) - strokeWidth)
            val offset = strokeWidth / 2

            // Underweight: blue [180 to 225 deg]
            drawArc(
                color = Color(0xFF1F75FE).copy(alpha = 0.4f),
                startAngle = 180f,
                sweepAngle = 45f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = arcSize,
                topLeft = Offset(offset, offset)
            )

            // Healthy: teal [225 to 270 deg]
            drawArc(
                color = Color(0xFF008080).copy(alpha = 0.4f),
                startAngle = 225f,
                sweepAngle = 45f,
                useCenter = false,
                style = Stroke(width = strokeWidth),
                size = arcSize,
                topLeft = Offset(offset, offset)
            )

            // Overweight: yellow [270 to 315 deg]
            drawArc(
                color = Color(0xFFD4AF37).copy(alpha = 0.4f),
                startAngle = 270f,
                sweepAngle = 45f,
                useCenter = false,
                style = Stroke(width = strokeWidth),
                size = arcSize,
                topLeft = Offset(offset, offset)
            )

            // Obese: red [315 to 360 deg]
            drawArc(
                color = Color(0xFFC21807).copy(alpha = 0.4f),
                startAngle = 315f,
                sweepAngle = 45f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = arcSize,
                topLeft = Offset(offset, offset)
            )

            // Needle drawing logic
            val needleAngleDeg = 180f + (normalizedBmiProgress * 180f)
            val needleAngleRad = needleAngleDeg * PI / 180f
            val baseRadius = size.width / 2
            val needleLength = baseRadius - 12.dp.toPx()

            val arcCenterX = size.width / 2
            val arcCenterY = size.height

            val endX = arcCenterX + needleLength * cos(needleAngleRad)
            val endY = arcCenterY + needleLength * sin(needleAngleRad)

            // Draw primary pointer needle line
            drawLine(
                color = Color.DarkGray,
                start = Offset(arcCenterX, arcCenterY),
                end = Offset(endX.toFloat(), endY.toFloat()),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Draw center hinge circles
            drawCircle(
                color = Color.DarkGray,
                radius = 8.dp.toPx(),
                center = Offset(arcCenterX, arcCenterY)
            )
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = Offset(arcCenterX, arcCenterY)
            )
        }
    }
}
