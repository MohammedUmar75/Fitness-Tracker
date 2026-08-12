package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProfile
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileTab(
    userProfile: UserProfile,
    dailySteps: Int = 0,
    onSaveFullProfile: (
        name: String,
        gender: String,
        age: Int,
        heightCm: Float,
        weightKg: Float,
        customStrideCm: Float,
        activityLevel: String,
        fitnessGoal: String,
        targetSteps: Int,
        targetCalories: Int,
        targetProtein: Int,
        targetCarbs: Int,
        targetFat: Int
    ) -> Unit
) {
    var nameInput by remember(userProfile.name) { mutableStateOf(userProfile.name) }
    var genderInput by remember(userProfile.gender) { mutableStateOf(userProfile.gender) }
    var ageInput by remember(userProfile.age) { mutableStateOf(if (userProfile.age <= 0) "" else userProfile.age.toString()) }
    var heightInput by remember(userProfile.heightCm) { mutableStateOf(if (userProfile.heightCm <= 0f) "" else userProfile.heightCm.toInt().toString()) }
    var weightInput by remember(userProfile.weightKg) { mutableStateOf(if (userProfile.weightKg <= 0f) "" else userProfile.weightKg.toString()) }

    var activityLevelInput by remember(userProfile.activityLevel) { mutableStateOf(userProfile.activityLevel) }
    var fitnessGoalInput by remember(userProfile.fitnessGoal) { mutableStateOf(userProfile.fitnessGoal) }

    var targetStepsInput by remember(userProfile.targetSteps) { mutableStateOf(userProfile.targetSteps.toString()) }
    var targetCaloriesInput by remember(userProfile.targetCalories) { mutableStateOf(if (userProfile.targetCalories <= 0) "2000" else userProfile.targetCalories.toString()) }
    var targetProteinInput by remember(userProfile.targetProtein) { mutableStateOf(if (userProfile.targetProtein <= 0) "140" else userProfile.targetProtein.toString()) }
    var targetCarbsInput by remember(userProfile.targetCarbs) { mutableStateOf(if (userProfile.targetCarbs <= 0) "210" else userProfile.targetCarbs.toString()) }
    var targetFatInput by remember(userProfile.targetFat) { mutableStateOf(if (userProfile.targetFat <= 0) "65" else userProfile.targetFat.toString()) }

    var showSaveSuccessBanner by remember { mutableStateOf(false) }

    // Live preview calculations based on current input values
    val currentAge = ageInput.toIntOrNull() ?: 0
    val currentHeight = heightInput.toFloatOrNull() ?: 0f
    val currentWeight = weightInput.toFloatOrNull() ?: 0f

    val previewProfile = remember(nameInput, genderInput, currentAge, currentHeight, currentWeight, activityLevelInput, fitnessGoalInput) {
        UserProfile(
            name = nameInput.trim(),
            age = currentAge,
            gender = genderInput,
            heightCm = currentHeight,
            weightKg = currentWeight,
            customStrideCm = 0f,
            restingHeartRate = 0,
            activityLevel = activityLevelInput,
            fitnessGoal = fitnessGoalInput
        )
    }

    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(showSaveSuccessBanner) {
        if (showSaveSuccessBanner) {
            delay(3000)
            showSaveSuccessBanner = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- SAVE SUCCESS BANNER ---
        AnimatedVisibility(
            visible = showSaveSuccessBanner,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Profile Updated Successfully!",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "All fitness parameters & stride lengths are saved and synced.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // --- HERO PROFILE HEADER ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.primaryContainer)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                colorScheme.primaryContainer,
                                colorScheme.secondaryContainer
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.sweepGradient(
                                    colors = listOf(
                                        colorScheme.primary,
                                        colorScheme.tertiary,
                                        colorScheme.primary
                                    )
                                )
                            )
                            .padding(3.dp)
                            .clip(CircleShape)
                            .background(colorScheme.surface)
                    ) {
                        Text(
                            text = previewProfile.name.take(1).uppercase(java.util.Locale.getDefault()).ifBlank { "👤" },
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = previewProfile.name.ifBlank { "User Profile" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onPrimaryContainer
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (previewProfile.gender.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = colorScheme.secondary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (previewProfile.gender.equals("Male", ignoreCase = true)) "👨 Male" else "👩 Female",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colorScheme.secondary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = colorScheme.errorContainer
                                ) {
                                    Text(
                                        text = "⚠️ Gender Unspecified",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colorScheme.onErrorContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = previewProfile.bmiCategory,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        MetricPill(
                            icon = "📏",
                            label = "Height",
                            value = if (previewProfile.heightCm > 0f) "${previewProfile.heightCm.toInt()} cm" else "--",
                            modifier = Modifier.weight(1f)
                        )
                        MetricPill(
                            icon = "⚖️",
                            label = "Weight",
                            value = if (previewProfile.weightKg > 0f) "${"%.1f".format(previewProfile.weightKg)} kg" else "--",
                            modifier = Modifier.weight(1f)
                        )
                        MetricPill(
                            icon = "🎂",
                            label = "Age",
                            value = if (previewProfile.age > 0) "${previewProfile.age} yrs" else "--",
                            modifier = Modifier.weight(1f)
                        )
                        MetricPill(
                            icon = "🚶",
                            label = "Stride",
                            value = if (dailySteps > 0 && previewProfile.strideMeters > 0f) "${"%.2f".format(previewProfile.strideMeters)} m" else "0 m",
                            modifier = Modifier.weight(1f)
                        )
                        MetricPill(
                            icon = "📊",
                            label = "BMI",
                            value = if (previewProfile.bmi > 0f) "${"%.1f".format(previewProfile.bmi)}" else "--",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // --- GROUP 1: BIOMETRIC PROFILE ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Badge,
                        contentDescription = "Biometric Profile",
                        tint = colorScheme.primary
                    )
                    Text(
                        text = "Biometric Profile & Body Metrics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Display Name") },
                    leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("profile_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Gender Selection
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Gender Identification",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Male" to "👨 Male", "Female" to "👩 Female").forEach { (key, label) ->
                            val isSelected = genderInput.equals(key, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    genderInput = if (isSelected) "" else key
                                },
                                label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Text(
                        text = if (genderInput.isBlank()) "⚠️ Select gender for precise BMR & metabolic calculations" else "Selected: $genderInput",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (genderInput.isBlank()) colorScheme.primary else colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }

                // Age, Height, Weight Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = ageInput,
                        onValueChange = { ageInput = it.filter { char -> char.isDigit() } },
                        label = { Text("Age (yrs)") },
                        placeholder = { Text("Age") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("profile_age_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = heightInput,
                        onValueChange = { heightInput = it.filter { char -> char.isDigit() || char == '.' } },
                        label = { Text("Height (cm)") },
                        placeholder = { Text("Height") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("profile_height_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it.filter { char -> char.isDigit() || char == '.' } },
                        label = { Text("Weight (kg)") },
                        placeholder = { Text("Weight") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("profile_weight_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Auto-calculated Stride Length Info
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Straighten,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = if (dailySteps > 0 && previewProfile.strideMeters > 0f) "Calculated Stride Length: %.2f m".format(previewProfile.strideMeters) else "Stride Length: 0.00 m",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (dailySteps <= 0) "Start taking footsteps to calculate active stride length" else if (previewProfile.heightCm > 0f) "Auto-calculated based on height (Height × 0.415)" else "Enter height above to calculate stride length",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // --- DEDICATED BMI ANALYSIS CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f))
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
                        imageVector = Icons.Outlined.Analytics,
                        contentDescription = "BMI Analysis",
                        tint = colorScheme.primary
                    )
                    Text(
                        text = "Body Mass Index (BMI)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (previewProfile.bmi > 0f) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = colorScheme.surface,
                        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "%.1f".format(previewProfile.bmi),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colorScheme.primary
                                )
                                Text(
                                    text = "kg/m²",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when (previewProfile.bmiCategory) {
                                    "Healthy Weight" -> Color(0xFF2E7D32)
                                    "Underweight" -> Color(0xFF0288D1)
                                    "Overweight" -> Color(0xFFED6C02)
                                    "Obese" -> Color(0xFFC62828)
                                    else -> colorScheme.secondary
                                }.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = previewProfile.bmiCategory,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (previewProfile.bmiCategory) {
                                        "Healthy Weight" -> Color(0xFF2E7D32)
                                        "Underweight" -> Color(0xFF0288D1)
                                        "Overweight" -> Color(0xFFED6C02)
                                        "Obese" -> Color(0xFFC62828)
                                        else -> colorScheme.secondary
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        LinearProgressIndicator(
                            progress = { ((previewProfile.bmi - 15f) / 20f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = when (previewProfile.bmiCategory) {
                                "Healthy Weight" -> Color(0xFF2E7D32)
                                "Underweight" -> Color(0xFF0288D1)
                                "Overweight" -> Color(0xFFED6C02)
                                "Obese" -> Color(0xFFC62828)
                                else -> colorScheme.primary
                            },
                            trackColor = colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("<18.5", style = MaterialTheme.typography.labelSmall, fontSize = 8.5.sp, color = colorScheme.outline)
                            Text("18.5-24.9", style = MaterialTheme.typography.labelSmall, fontSize = 8.5.sp, color = colorScheme.outline)
                            Text("25-29.9", style = MaterialTheme.typography.labelSmall, fontSize = 8.5.sp, color = colorScheme.outline)
                            Text("≥30", style = MaterialTheme.typography.labelSmall, fontSize = 8.5.sp, color = colorScheme.outline)
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = colorScheme.primary
                            )
                            Text(
                                text = "Enter both height (cm) and weight (kg) above to view your calculated Body Mass Index (BMI).",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // --- GROUP 2: LIFESTYLE & FITNESS STRATEGY ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FitnessCenter,
                        contentDescription = "Fitness Strategy",
                        tint = colorScheme.primary
                    )
                    Text(
                        text = "Lifestyle & Fitness Strategy",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Primary Fitness Goal",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurfaceVariant
                )

                val goals = listOf(
                    Triple("Weight Loss", "📉 Deficit for fat loss & lean physique", "Weight Loss"),
                    Triple("Muscle Gain", "🏋️ Surplus for muscle growth & strength", "Muscle Gain"),
                    Triple("Maintenance", "⚖️ Balance intake to preserve body weight", "Maintenance"),
                    Triple("Endurance", "🏃 High carbohydrate performance energy", "Endurance")
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    goals.forEach { (title, subtitle, key) ->
                        val isSelected = fitnessGoalInput.equals(key, ignoreCase = true)
                        Surface(
                            onClick = { fitnessGoalInput = key },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) colorScheme.primaryContainer else colorScheme.surface,
                            border = if (isSelected) BorderStroke(2.dp, colorScheme.primary) else BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) colorScheme.onPrimaryContainer else colorScheme.onSurface
                                    )
                                    Text(
                                        text = subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected) colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else colorScheme.onSurfaceVariant
                                    )
                                }
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { fitnessGoalInput = key }
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Daily Activity Level",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurfaceVariant
                )

                val activityLevels = listOf(
                    "Sedentary" to "🛌 Little or no exercise (Desk job)",
                    "Lightly Active" to "🚶 Light exercise 1-3 days/week",
                    "Moderately Active" to "🏃 Moderate exercise 3-5 days/week",
                    "Very Active" to "🚴 Heavy exercise 6-7 days/week",
                    "Extra Active" to "⚡ Intense daily physical training"
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    activityLevels.forEach { (level, desc) ->
                        val isSelected = activityLevelInput.equals(level, ignoreCase = true)
                        Surface(
                            onClick = { activityLevelInput = level },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) colorScheme.secondaryContainer else colorScheme.surface,
                            border = if (isSelected) BorderStroke(1.5.dp, colorScheme.secondary) else null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = level,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) colorScheme.onSecondaryContainer else colorScheme.onSurface
                                    )
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected) colorScheme.onSecondaryContainer.copy(alpha = 0.8f) else colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = colorScheme.secondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- GROUP 3: METABOLIC CALCULATIONS & DAILY TARGETS ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.tertiaryContainer.copy(alpha = 0.4f)),
            border = BorderStroke(1.dp, colorScheme.tertiary.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = "Calculations",
                            tint = colorScheme.tertiary
                        )
                        Text(
                            text = "Metabolic Calculations & Daily Targets",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onTertiaryContainer
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colorScheme.tertiary.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = previewProfile.bmiCategory,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.tertiary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CalculationStatBox(
                        label = "BMR (Basal)",
                        value = if (previewProfile.bmr > 0) "${previewProfile.bmr} kcal" else "--",
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    CalculationStatBox(
                        label = "TDEE (Daily)",
                        value = if (previewProfile.tdee > 0) "${previewProfile.tdee} kcal" else "--",
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    CalculationStatBox(
                        label = "Target Energy",
                        value = if (previewProfile.recommendedCalories > 0) "${previewProfile.recommendedCalories} kcal" else "--",
                        modifier = Modifier.weight(1f)
                    )
                }

                if (previewProfile.bmr <= 0) {
                    Text(
                        text = "💡 Enter your Gender, Age, Height, and Weight above to compute your custom BMR & TDEE metabolic rates.",
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                    )
                }

                Divider(color = colorScheme.tertiary.copy(alpha = 0.2f))

                Text(
                    text = "Recommended Daily Macro Breakdown:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onTertiaryContainer
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MacroChip(label = "Protein", value = if (previewProfile.recommendedProteinGrams > 0) "${previewProfile.recommendedProteinGrams}g" else "--", color = Color(0xFFE53935))
                    MacroChip(label = "Carbs", value = if (previewProfile.recommendedCarbsGrams > 0) "${previewProfile.recommendedCarbsGrams}g" else "--", color = Color(0xFF1E88E5))
                    MacroChip(label = "Fats", value = if (previewProfile.recommendedFatGrams > 0) "${previewProfile.recommendedFatGrams}g" else "--", color = Color(0xFFFDD835))
                    MacroChip(label = "Water", value = "2.5 L", color = Color(0xFF00ACC1))
                }

                Button(
                    onClick = {
                        if (previewProfile.recommendedCalories > 0) {
                            targetCaloriesInput = previewProfile.recommendedCalories.toString()
                            targetProteinInput = previewProfile.recommendedProteinGrams.toString()
                            targetCarbsInput = previewProfile.recommendedCarbsGrams.toString()
                            targetFatInput = previewProfile.recommendedFatGrams.toString()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.tertiary),
                    shape = RoundedCornerShape(12.dp),
                    enabled = previewProfile.recommendedCalories > 0,
                    modifier = Modifier.fillMaxWidth().testTag("autofill_goals_button")
                ) {
                    Icon(Icons.Filled.FlashOn, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Auto-Fill Daily Targets From Calculations", fontWeight = FontWeight.Bold)
                }

                Divider(color = colorScheme.tertiary.copy(alpha = 0.2f))

                Text(
                    text = "Custom Target Goal Overrides",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onTertiaryContainer
                )

                OutlinedTextField(
                    value = targetStepsInput,
                    onValueChange = { targetStepsInput = it.filter { c -> c.isDigit() } },
                    label = { Text("Daily Step Target") },
                    leadingIcon = { Icon(Icons.Outlined.DirectionsWalk, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("target_steps_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = targetCaloriesInput,
                        onValueChange = { targetCaloriesInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Calories (kcal)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("target_calories_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = targetProteinInput,
                        onValueChange = { targetProteinInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Protein (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("target_protein_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = targetCarbsInput,
                        onValueChange = { targetCarbsInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Carbs (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("target_carbs_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = targetFatInput,
                        onValueChange = { targetFatInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Fat (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("target_fat_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // --- SAVE PROFILE & SYNC BUTTON ---
        Button(
            onClick = {
                val age = ageInput.toIntOrNull() ?: previewProfile.age
                val height = heightInput.toFloatOrNull() ?: previewProfile.heightCm
                val weight = weightInput.toFloatOrNull() ?: previewProfile.weightKg
                val stride = 0f

                val steps = targetStepsInput.toIntOrNull() ?: 10000
                val cal = targetCaloriesInput.toIntOrNull() ?: 2000
                val prot = targetProteinInput.toIntOrNull() ?: 140
                val carbs = targetCarbsInput.toIntOrNull() ?: 210
                val fat = targetFatInput.toIntOrNull() ?: 65

                onSaveFullProfile(
                    nameInput.trim(),
                    genderInput,
                    age,
                    height,
                    weight,
                    stride,
                    activityLevelInput,
                    fitnessGoalInput,
                    steps,
                    cal,
                    prot,
                    carbs,
                    fat
                )
                showSaveSuccessBanner = true
            },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("save_profile_button"),
            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
        ) {
            Icon(Icons.Filled.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Save Profile & Sync AI Coach",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun MetricPill(
    icon: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$icon $value",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label,
                fontSize = 9.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CalculationStatBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun MacroChip(
    label: String,
    value: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = "$label: $value",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
