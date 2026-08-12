package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AIRecommendation
import com.example.data.DietIntake
import com.example.data.WorkoutProgress
import com.example.ui.components.CenteredProgressRing
import com.example.ui.components.MetricsTrendChart

@Composable
fun DashboardTab(
    workouts: List<WorkoutProgress>,
    diets: List<DietIntake>,
    allWorkouts: List<WorkoutProgress>,
    allDiets: List<DietIntake>,
    aiAdvice: AIRecommendation?,
    onNavigateToCoach: () -> Unit,
    modifier: Modifier = Modifier,
    userProfile: com.example.data.UserProfile? = null,
    onNavigateToProfile: () -> Unit = {},
    customTargetCalories: Int = 0,
    customTargetProtein: Int = 0,
    customTargetCarbs: Int = 0,
    customTargetFat: Int = 0,
    waterIntakeMl: Int = 0,
    onAddWater: (Int) -> Unit = {},
    onResetWater: () -> Unit = {},
    dailySteps: Int = 0,
    targetSteps: Int = 10000,
    onNavigateToFootsteps: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    // Daily totals calculations
    val totalCaloriesConsumed = diets.sumOf { it.calories }.toFloat()
    val totalProteinConsumed = diets.sumOf { it.proteinGram }.toFloat()
    val totalCarbsConsumed = diets.sumOf { it.carbsGram }.toFloat()
    val totalFatConsumed = diets.sumOf { it.fatGram }.toFloat()
    val totalCaloriesBurned = workouts.sumOf { it.caloriesBurned }.toFloat()

    // Goal values: custom override -> AI recommendation -> Profile calculated target
    val targetCalories = if (customTargetCalories > 0) customTargetCalories.toFloat()
        else if (aiAdvice?.caloriesTarget != null && aiAdvice.caloriesTarget > 0) aiAdvice.caloriesTarget.toFloat()
        else (userProfile?.recommendedCalories?.toFloat() ?: 0f)

    val targetProtein = if (customTargetProtein > 0) customTargetProtein.toFloat()
        else if (aiAdvice?.proteinTarget != null && aiAdvice.proteinTarget > 0) aiAdvice.proteinTarget.toFloat()
        else (userProfile?.recommendedProteinGrams?.toFloat() ?: 0f)

    val targetCarbs = if (customTargetCarbs > 0) customTargetCarbs.toFloat()
        else if (aiAdvice?.carbsTarget != null && aiAdvice.carbsTarget > 0) aiAdvice.carbsTarget.toFloat()
        else (userProfile?.recommendedCarbsGrams?.toFloat() ?: 0f)

    val targetFat = if (customTargetFat > 0) customTargetFat.toFloat()
        else if (aiAdvice?.fatTarget != null && aiAdvice.fatTarget > 0) aiAdvice.fatTarget.toFloat()
        else (userProfile?.recommendedFatGrams?.toFloat() ?: 0f)

    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Quick Banner suggestion indicator
        Card(
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.secondaryContainer.copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = "AI Coach active",
                    tint = colorScheme.secondary,
                    modifier = Modifier.size(28.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AI Personalized Guide",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = aiAdvice?.suggestion ?: "No adjustment loaded. Log activity and request an AI nutrition tune-up based on your workouts!",
                        fontSize = 11.sp,
                        color = colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = onNavigateToCoach) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Go to Coach",
                        tint = colorScheme.secondary
                    )
                }
            }
        }

        // Circular Calorie tracker Row
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Daily Caloric Balance",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (targetCalories <= 0f) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBox,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = colorScheme.primary
                        )
                        Text(
                            text = "Calorie Goal Unconfigured",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = colorScheme.onSurface
                        )
                        Text(
                            text = "Complete your profile metrics (age, height, weight) to calculate your recommended daily calorie balance.",
                            fontSize = 12.sp,
                            color = colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = onNavigateToProfile,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Set Up Profile Metrics", fontSize = 12.sp)
                        }
                    }
                } else {
                    CenteredProgressRing(
                        consumed = totalCaloriesConsumed,
                        target = targetCalories,
                        burned = totalCaloriesBurned
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Breakdown metrics rows
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        MetricMiniBox(
                            label = "Eaten",
                            value = "${totalCaloriesConsumed.toInt()} kcal",
                            icon = Icons.Default.Restaurant,
                            color = colorScheme.primary
                        )
                        MetricMiniBox(
                            label = "Burned",
                            value = "-${totalCaloriesBurned.toInt()} kcal",
                            icon = Icons.Default.DirectionsRun,
                            color = colorScheme.tertiary
                        )
                        MetricMiniBox(
                            label = "Remaining",
                            value = "${(targetCalories - totalCaloriesConsumed + totalCaloriesBurned).toInt()} kcal",
                            icon = Icons.Default.Flag,
                            color = colorScheme.secondary
                        )
                    }

                    if (totalCaloriesConsumed == 0f && totalCaloriesBurned == 0f) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No meals or workouts logged today yet.",
                            fontSize = 11.sp,
                            color = colorScheme.onSurfaceVariant,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
        }

        // Footsteps Activity Card on Dashboard
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colorScheme.primaryContainer.copy(alpha = 0.35f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsWalk,
                            contentDescription = "Footsteps",
                            tint = colorScheme.onPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Daily Footsteps",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "%,d / %,d".format(java.util.Locale.getDefault(), dailySteps, targetSteps),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )
                        val stepPercent = ((dailySteps.toFloat() / targetSteps.coerceAtLeast(1)) * 100).toInt()
                        Text(
                            text = "$stepPercent% of daily goal completed",
                            fontSize = 11.sp,
                            color = colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Button(
                    onClick = onNavigateToFootsteps,
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Footsteps Menu", fontSize = 12.sp)
                }
            }
        }

        // Macronutrients Linear Bars Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Macronutrients Intake",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Protein Progress
                MacroProgressBar(
                    label = "Protein",
                    consumed = totalProteinConsumed,
                    target = targetProtein,
                    color = Color(0xFF008080), // Teal
                    unit = "g"
                )

                // Carbs Progress
                MacroProgressBar(
                    label = "Carbohydrates",
                    consumed = totalCarbsConsumed,
                    target = targetCarbs,
                    color = Color(0xFF1F75FE), // Blue
                    unit = "g"
                )

                // Fats Progress
                MacroProgressBar(
                    label = "Dietary Fats",
                    consumed = totalFatConsumed,
                    target = targetFat,
                    color = Color(0xFFD4AF37), // Gold
                    unit = "g"
                )
            }
        }

        // 7-Days Historical Trends Visual bar charts
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                MetricsTrendChart(
                    workouts = allWorkouts,
                    diets = allDiets
                )
            }
        }
    }
}

@Composable
fun MetricMiniBox(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            color = colorScheme.onSurface
        )
    }
}

@Composable
fun MacroProgressBar(
    label: String,
    consumed: Float,
    target: Float,
    color: Color,
    unit: String
) {
    val colorScheme = MaterialTheme.colorScheme
    val fraction = if (target > 0) (consumed / target).coerceIn(0f, 1f) else 0f

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface
            )
            Text(
                text = "${consumed.toInt()}/ ${target.toInt()}$unit",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurfaceVariant
            )
        }
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = colorScheme.surfaceVariant
        )
    }
}
