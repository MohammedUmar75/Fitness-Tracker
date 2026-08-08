package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AIRecommendation
import com.example.data.NutritionAnalysis

@Composable
fun AiCoachTab(
    selectedDate: String,
    advice: AIRecommendation?,
    isGenerating: Boolean,
    onTriggerAdjustment: () -> Unit,
    nutritionAnalysis: NutritionAnalysis?,
    isAnalyzingNutrition: Boolean,
    onTriggerNutritionAnalysis: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val colorScheme = MaterialTheme.colorScheme
    var selectedSubTab by remember { mutableIntStateOf(0) } // 0 = Workout Goals, 1 = Smart Nutrition Swaps

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Nested Sub-Tab Row
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = colorScheme.surfaceVariant.copy(alpha = 0.4f),
            contentColor = colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = { Text("Workout Coach", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.QueryStats, contentDescription = "Workout & Goal Targets", modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = { Text("Food Substitutes", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.RestaurantMenu, contentDescription = "Healthy Swaps Advisor", modifier = Modifier.size(16.dp)) }
            )
        }

        if (selectedSubTab == 0) {
            // ----------- WORKOUT & GOAL TARGETS SUB TAB -----------
            // AI Sport Coach Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = colorScheme.primary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "AI Coaching",
                            tint = colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "AI Sports Coach & Nutritionist",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colorScheme.onPrimary
                        )
                    }
                    Text(
                        text = "Leverage Gemini to analyze your performance logs (current workouts and calorie deficits/surpluses) to compile custom intake budgets and custom food advice suited to support active recoveries.",
                        fontSize = 11.sp,
                        color = colorScheme.onPrimary.copy(alpha = 0.85f),
                        lineHeight = 15.sp
                    )
                }
            }

            if (isGenerating) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(color = colorScheme.primary)
                    Text(
                        text = "Analyzing performance log details...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    Text(
                        text = "Tailoring nutrition ratios and crafting healthy menus...",
                        fontSize = 11.sp,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Regeneration / Optimization button
                Button(
                    onClick = onTriggerAdjustment,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QueryStats,
                        contentDescription = "Optimize",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (advice == null) "Optimize Daily Macros with AI" else "Re-optimize Macros with AI Coach"
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                if (advice != null) {
                    // Coach overview text
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
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChatBubbleOutline,
                                    contentDescription = "Insight",
                                    tint = colorScheme.primary
                                )
                                Text(
                                    text = "Coach Insights & Suggestions",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface
                                )
                            }

                            Text(
                                text = advice.suggestion,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Dynamic adapted targets indicators
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "AI Adjusted Goal Targets",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                TargetBadge(
                                    label = "Calories",
                                    value = "${advice.caloriesTarget} kcal",
                                    color = colorScheme.primaryContainer
                                )
                                TargetBadge(
                                    label = "Protein",
                                    value = "${advice.proteinTarget}g",
                                    color = Color(0xFFE0F2F1) // Soft Teal
                                )
                                TargetBadge(
                                    label = "Carbs",
                                    value = "${advice.carbsTarget}g",
                                    color = Color(0xFFE3F2FD) // Soft Blue
                                )
                                TargetBadge(
                                    label = "Fats",
                                    value = "${advice.fatTarget}g",
                                    color = Color(0xFFFFFDE7) // Soft Yellow
                                )
                            }
                        }
                    }

                    // Generative Diet programs meal plans
                    advice.mealPlanGenerated?.let { rawPlan ->
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
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RestaurantMenu,
                                        contentDescription = "Meals menu",
                                        tint = colorScheme.secondary
                                    )
                                    Text(
                                        text = "Tailored Personal Meal Plan",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.onSurface
                                    )
                                }

                                HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.5f))

                                // Custom Line Parser Markdown emulation
                                val lines = rawPlan.split("\n")
                                lines.forEach { rawLine ->
                                    if (rawLine.isNotBlank()) {
                                        val lineString = rawLine.trim()
                                        when {
                                            lineString.startsWith("###") || lineString.startsWith("##") -> {
                                                val headerText = lineString.replace("###", "").replace("##", "").trim()
                                                Text(
                                                    text = headerText,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = colorScheme.primary,
                                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                                )
                                            }
                                            lineString.startsWith("*") || lineString.startsWith("-") -> {
                                                val bulletBody = lineString.removePrefix("*").removePrefix("-").trim()
                                                Row(
                                                    modifier = Modifier.padding(start = 6.dp, bottom = 4.dp),
                                                    verticalAlignment = Alignment.Top,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = "•",
                                                        color = colorScheme.secondary,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp
                                                    )
                                                    Text(
                                                        text = bulletBody,
                                                        fontSize = 11.5.sp,
                                                        color = colorScheme.onSurfaceVariant,
                                                        lineHeight = 16.sp,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                            }
                                            else -> {
                                                Text(
                                                    text = lineString,
                                                    fontSize = 11.5.sp,
                                                    color = colorScheme.onSurfaceVariant,
                                                    lineHeight = 16.sp,
                                                    modifier = Modifier.padding(bottom = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(10.dp))
                    // Clean empty state guide
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SelfImprovement,
                            contentDescription = "Self improvement",
                            tint = colorScheme.outline,
                            modifier = Modifier.size(54.dp)
                        )
                        Text(
                            text = "Customize Your Nutrient Targets",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Log simple details of what you've eaten so far or workouts performed today in the trackers log. Then click the button above to request a personalized nutritional adjustment plan!",
                            fontSize = 11.sp,
                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        } else {
            // ----------- MEAL ADJUSTMENT & SUBSTITUTIONS SUB TAB -----------
            // AI Healthy Food Substitutes Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = colorScheme.secondaryContainer.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Restaurant,
                            contentDescription = "Nutrition Adjustor",
                            tint = colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "AI Nutrition Swaps & Quality Advisor",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colorScheme.onSecondaryContainer
                        )
                    }
                    Text(
                        text = "Obtain expert, dietitian-approved meal adjustments and food hacks customized specifically to your actual logged meals today to optimize minerals and glucose stability.",
                        fontSize = 11.sp,
                        color = colorScheme.onSecondaryContainer.copy(alpha = 0.85f),
                        lineHeight = 15.sp
                    )
                }
            }

            if (isAnalyzingNutrition) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(color = colorScheme.secondary)
                    Text(
                        text = "Analyzing nutrition details...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    Text(
                        text = "Scanning ingredients and drawing up healthy swaps...",
                        fontSize = 11.sp,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Analysis Trigger Button
                Button(
                    onClick = onTriggerNutritionAnalysis,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.secondary),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Analyze Meal Swaps",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (nutritionAnalysis == null) "Analyze Food Quality & Swaps" else "Refresh Diet Quality Review"
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                if (nutritionAnalysis != null) {
                    // Double Side Card showing Quality Score & Quick Summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Quality Score Ring Card
                        Card(
                            modifier = Modifier
                                .weight(0.4f)
                                .height(125.dp),
                            colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Diet Score",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${nutritionAnalysis.score}",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = when {
                                        nutritionAnalysis.score >= 80 -> Color(0xFF00796B) // Teal
                                        nutritionAnalysis.score >= 65 -> Color(0xFFD4AF37) // Gold/Orange
                                        else -> colorScheme.error
                                    }
                                )
                                Text(
                                    text = "out of 100",
                                    fontSize = 10.sp,
                                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }

                        // Overall Analysis Feedback Card
                        Card(
                            modifier = Modifier
                                .weight(0.6f)
                                .height(125.dp),
                            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Feedback,
                                        contentDescription = "Feedback icon",
                                        tint = colorScheme.secondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Dietitian Review",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = nutritionAnalysis.overallFeedback,
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp,
                                    color = colorScheme.onSurfaceVariant,
                                    maxLines = 4,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Card displaying proposed substitutions
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
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                              ) {
                                Icon(
                                    imageVector = Icons.Default.PublishedWithChanges,
                                    contentDescription = "Replacement adjusters",
                                    tint = colorScheme.primary
                                )
                                Text(
                                    text = "Clever Swaps & Substitutes",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface
                                )
                            }

                            HorizontalDivider(color = colorScheme.outlineVariant.copy(alpha = 0.5f))

                            // Custom Line Parser Markdown emulation
                            val lines = nutritionAnalysis.adjustmentsList.split("\n")
                            lines.forEach { rawLine ->
                                if (rawLine.isNotBlank()) {
                                    val lineString = rawLine.trim()
                                    when {
                                        lineString.startsWith("###") || lineString.startsWith("##") -> {
                                            val headerText = lineString.replace("###", "").replace("##", "").trim()
                                            Text(
                                                text = headerText,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Black,
                                                color = colorScheme.primary,
                                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                            )
                                        }
                                        lineString.startsWith("*") || lineString.startsWith("-") -> {
                                            val bulletBody = lineString.removePrefix("*").removePrefix("-").trim()
                                            Row(
                                                modifier = Modifier.padding(start = 6.dp, bottom = 4.dp),
                                                verticalAlignment = Alignment.Top,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = "•",
                                                    color = colorScheme.primary,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    text = bulletBody,
                                                    fontSize = 11.5.sp,
                                                    color = colorScheme.onSurfaceVariant,
                                                    lineHeight = 16.sp,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                        else -> {
                                            Text(
                                                text = lineString,
                                                fontSize = 11.5.sp,
                                                color = colorScheme.onSurfaceVariant,
                                                lineHeight = 16.sp,
                                                modifier = Modifier.padding(bottom = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(10.dp))
                    // Empty list explanation guide
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NoFood,
                            contentDescription = "Nutrition review instructions",
                            tint = colorScheme.outline,
                            modifier = Modifier.size(54.dp)
                        )
                        Text(
                            text = "No Meal Swaps Generated Yet",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Ensure you log some meals (Breakfast, Lunch, Dinner, or snacks) on the trackers tab first. Then click the analyze button above to review food quality and suggest healthier alternatives!",
                            fontSize = 11.sp,
                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TargetBadge(
    label: String,
    value: String,
    color: Color
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color)
            .padding(8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontSize = 9.sp,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = colorScheme.onSurface
            )
        }
    }
}
