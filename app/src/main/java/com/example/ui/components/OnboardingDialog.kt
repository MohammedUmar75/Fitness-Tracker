package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingDialog(
    onComplete: (
        name: String,
        gender: String,
        age: Int,
        heightCm: Float,
        weightKg: Float,
        restingHr: Int,
        activityLevel: String,
        fitnessGoal: String,
        targetSteps: Int,
        targetCalories: Int,
        targetProtein: Int,
        targetCarbs: Int,
        targetFat: Int
    ) -> Unit,
    onDismiss: () -> Unit = {}
) {
    var currentStep by remember { mutableIntStateOf(1) } // 1, 2, 3

    var nameInput by remember { mutableStateOf("") }
    var genderInput by remember { mutableStateOf("") }
    var ageInput by remember { mutableStateOf("") }
    var heightInput by remember { mutableStateOf("") }
    var weightInput by remember { mutableStateOf("") }

    var activityLevelInput by remember { mutableStateOf("Moderately Active") }
    var fitnessGoalInput by remember { mutableStateOf("Weight Loss") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val age = ageInput.toIntOrNull() ?: 0
    val height = heightInput.toFloatOrNull() ?: 0f
    val weight = weightInput.toFloatOrNull() ?: 0f

    val tempProfile = remember(nameInput, genderInput, age, height, weight, activityLevelInput, fitnessGoalInput) {
        UserProfile(
            name = nameInput.ifBlank { "User" },
            age = age,
            gender = genderInput,
            heightCm = height,
            weightKg = weight,
            restingHeartRate = 0,
            activityLevel = activityLevelInput,
            fitnessGoal = fitnessGoalInput
        )
    }

    val colorScheme = MaterialTheme.colorScheme

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(28.dp)),
            color = colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Progress & Header
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = colorScheme.primaryContainer
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.FitnessCenter,
                                        contentDescription = null,
                                        tint = colorScheme.primary,
                                        modifier = Modifier.padding(8.dp).size(20.dp)
                                    )
                                }
                                Text(
                                    text = "Fitness Tracker Setup",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = colorScheme.onSurface
                                )
                            }

                            TextButton(onClick = onDismiss) {
                                Text("Skip for now", fontSize = 12.sp, color = colorScheme.outline)
                            }
                        }

                        // Step Indicator Progress Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            (1..3).forEach { step ->
                                val active = step <= currentStep
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (active) colorScheme.primary else colorScheme.surfaceVariant
                                        )
                                )
                            }
                        }

                        Text(
                            text = when (currentStep) {
                                1 -> "Step 1 of 3: Personal Information"
                                2 -> "Step 2 of 3: Body Measurements"
                                else -> "Step 3 of 3: Goals & Activity Level"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.primary
                        )
                    }

                    if (errorMessage != null) {
                        Surface(
                            color = colorScheme.errorContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = colorScheme.onErrorContainer)
                                Text(
                                    text = errorMessage!!,
                                    color = colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    // Dynamic Step Content Area
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        when (currentStep) {
                            1 -> StepOnePersonal(
                                name = nameInput,
                                onNameChange = { nameInput = it; errorMessage = null },
                                gender = genderInput,
                                onGenderChange = { genderInput = it; errorMessage = null },
                                colorScheme = colorScheme
                            )
                            2 -> StepTwoBodyMetrics(
                                age = ageInput,
                                onAgeChange = { ageInput = it; errorMessage = null },
                                height = heightInput,
                                onHeightChange = { heightInput = it; errorMessage = null },
                                weight = weightInput,
                                onWeightChange = { weightInput = it; errorMessage = null },
                                colorScheme = colorScheme
                            )
                            3 -> StepThreeGoals(
                                fitnessGoal = fitnessGoalInput,
                                onGoalChange = { fitnessGoalInput = it },
                                activityLevel = activityLevelInput,
                                onActivityChange = { activityLevelInput = it },
                                colorScheme = colorScheme
                            )
                        }
                    }

                    // Navigation Footer Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentStep > 1) {
                            OutlinedButton(
                                onClick = { currentStep -= 1; errorMessage = null },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Back")
                            }
                        }

                        Button(
                            onClick = {
                                when (currentStep) {
                                    1 -> {
                                        if (nameInput.isBlank()) {
                                            errorMessage = "Please enter your name."
                                            return@Button
                                        }
                                        if (genderInput.isBlank()) {
                                            errorMessage = "Please select your gender."
                                            return@Button
                                        }
                                        currentStep = 2
                                    }
                                    2 -> {
                                        val validAge = ageInput.toIntOrNull()
                                        if (validAge == null || validAge <= 0) {
                                            errorMessage = "Please enter a valid age."
                                            return@Button
                                        }
                                        val validHeight = heightInput.toFloatOrNull()
                                        if (validHeight == null || validHeight <= 0f) {
                                            errorMessage = "Please enter your height in cm."
                                            return@Button
                                        }
                                        val validWeight = weightInput.toFloatOrNull()
                                        if (validWeight == null || validWeight <= 0f) {
                                            errorMessage = "Please enter your weight in kg."
                                            return@Button
                                        }
                                        currentStep = 3
                                    }
                                    3 -> {
                                        val validAge = ageInput.toIntOrNull() ?: age
                                        val validHeight = heightInput.toFloatOrNull() ?: height
                                        val validWeight = weightInput.toFloatOrNull() ?: weight

                                        val steps = 10000
                                        val cal = if (tempProfile.recommendedCalories > 0) tempProfile.recommendedCalories else 2000
                                        val prot = if (tempProfile.recommendedProteinGrams > 0) tempProfile.recommendedProteinGrams else 140
                                        val carbs = if (tempProfile.recommendedCarbsGrams > 0) tempProfile.recommendedCarbsGrams else 210
                                        val fat = if (tempProfile.recommendedFatGrams > 0) tempProfile.recommendedFatGrams else 65

                                        onComplete(
                                            nameInput.trim(),
                                            genderInput,
                                            validAge,
                                            validHeight,
                                            validWeight,
                                            0,
                                            activityLevelInput,
                                            fitnessGoalInput,
                                            steps,
                                            cal,
                                            prot,
                                            carbs,
                                            fat
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.weight(1.5f).height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
                        ) {
                            Text(
                                text = if (currentStep < 3) "Next Step" else "Save & Start",
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = if (currentStep < 3) Icons.Default.ArrowForward else Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepOnePersonal(
    name: String,
    onNameChange: (String) -> Unit,
    gender: String,
    onGenderChange: (String) -> Unit,
    colorScheme: ColorScheme
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "👋 Welcome! What should we call you?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurface
        )

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Your Name *") },
            placeholder = { Text("Enter your name") },
            leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("onboarding_name_input"),
            shape = RoundedCornerShape(14.dp)
        )

        Text(
            text = "Select Gender (for metabolic calculation)",
            style = MaterialTheme.typography.labelLarge,
            color = colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf("Male" to "👨 Male", "Female" to "👩 Female").forEach { (gKey, label) ->
                val selected = gender.equals(gKey, ignoreCase = true)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onGenderChange(gKey) },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        width = if (selected) 2.dp else 1.dp,
                        color = if (selected) colorScheme.primary else colorScheme.outlineVariant
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected) colorScheme.primaryContainer.copy(alpha = 0.4f) else colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) colorScheme.primary else colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepTwoBodyMetrics(
    age: String,
    onAgeChange: (String) -> Unit,
    height: String,
    onHeightChange: (String) -> Unit,
    weight: String,
    onWeightChange: (String) -> Unit,
    colorScheme: ColorScheme
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "📏 Enter Body Measurements",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = age,
                onValueChange = onAgeChange,
                label = { Text("Age (yrs) *") },
                placeholder = { Text("Age") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f).testTag("onboarding_age_input"),
                shape = RoundedCornerShape(14.dp)
            )

            OutlinedTextField(
                value = height,
                onValueChange = onHeightChange,
                label = { Text("Height (cm) *") },
                placeholder = { Text("Height") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f).testTag("onboarding_height_input"),
                shape = RoundedCornerShape(14.dp)
            )
        }

        OutlinedTextField(
            value = weight,
            onValueChange = onWeightChange,
            label = { Text("Weight (kg) *") },
            placeholder = { Text("Weight") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("onboarding_weight_input"),
            shape = RoundedCornerShape(14.dp)
        )
    }
}

@Composable
private fun StepThreeGoals(
    fitnessGoal: String,
    onGoalChange: (String) -> Unit,
    activityLevel: String,
    onActivityChange: (String) -> Unit,
    colorScheme: ColorScheme
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "🎯 Primary Fitness Goal",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurface
        )

        val goals = listOf(
            "Weight Loss" to "🔥 Fat Burn (-500 kcal deficit)",
            "Muscle Gain" to "💪 Hypertrophy (+350 kcal surplus)",
            "Maintenance" to "⚖️ Balanced energy & wellness"
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            goals.forEach { (gKey, desc) ->
                val selected = fitnessGoal.equals(gKey, ignoreCase = true)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onGoalChange(gKey) },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        width = if (selected) 2.dp else 1.dp,
                        color = if (selected) colorScheme.primary else colorScheme.outlineVariant
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected) colorScheme.primaryContainer.copy(alpha = 0.35f) else colorScheme.surfaceVariant.copy(alpha = 0.25f)
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(gKey, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                            Text(desc, fontSize = 11.sp, color = colorScheme.onSurfaceVariant)
                        }
                        RadioButton(
                            selected = selected,
                            onClick = { onGoalChange(gKey) }
                        )
                    }
                }
            }
        }

        Text(
            text = "⚡ Activity Level",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurface
        )

        val activities = listOf(
            "Sedentary" to "Little or no exercise",
            "Lightly Active" to "1-3 days/week",
            "Moderately Active" to "3-5 days/week",
            "Very Active" to "6-7 days/week"
        )

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            activities.forEach { (actKey, desc) ->
                val selected = activityLevel.equals(actKey, ignoreCase = true)
                FilterChip(
                    selected = selected,
                    onClick = { onActivityChange(actKey) },
                    label = { Text("$actKey ($desc)", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
