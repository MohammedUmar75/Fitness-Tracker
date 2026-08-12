package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FootstepsTab(
    dailySteps: Int,
    targetSteps: Int,
    isStepTrackingActive: Boolean,
    sensorTypeName: String,
    stepHistory: List<Triple<String, String, Int>>,
    userWeightKg: Float,
    userHeightCm: Float,
    customStrideCm: Float = 0f,
    onStartTracking: () -> Unit,
    onPauseTracking: () -> Unit,
    onSetTargetSteps: (Int) -> Unit,
    onSetCustomStrideCm: ((Float) -> Unit)? = null,
    onAddManualSteps: (Int) -> Unit,
    onSimulateStepTick: (Int) -> Unit,
    onResetDailySteps: () -> Unit,
    onTriggerCelebration: (title: String, subtitle: String, emoji: String) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val scrollState = rememberScrollState()

    // Permission state check for ACTIVITY_RECOGNITION on API 29+
    var hasPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            onStartTracking()
        }
    }

    // Calculations based on user characteristics
    val strideMeters = if (dailySteps > 0 && userHeightCm > 0f) (userHeightCm * 0.415f) / 100f else 0f
    val totalDistanceKm = if (dailySteps <= 0 || strideMeters <= 0f) 0f else (dailySteps * strideMeters) / 1000f
    val totalDistanceMiles = if (totalDistanceKm <= 0f) 0f else totalDistanceKm * 0.621371f

    // Estimated step calories burned (requires weight in Profile for accuracy)
    val caloriesBurnedKcal = if (userWeightKg > 0f && dailySteps > 0) dailySteps * (userWeightKg / 70f) * 0.04f else 0f

    // Estimated active walking time (avg 100 steps per minute)
    val activeMinutes = dailySteps / 100

    val progressFraction = (dailySteps.toFloat() / targetSteps.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(durationMillis = 800),
        label = "StepProgress"
    )

    var showTargetDialog by remember { mutableStateOf(false) }
    var customTargetInput by remember { mutableStateOf(targetSteps.toString()) }

    var showManualStepDialog by remember { mutableStateOf(false) }
    var manualStepsInput by remember { mutableStateOf("1000") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Banner & Sensor Status Badge
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.primaryContainer.copy(alpha = 0.4f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Footsteps & Pedometer",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Real-time automatic sensor step tracking",
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = if (isStepTrackingActive && hasPermission) colorScheme.primary else colorScheme.outline.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isStepTrackingActive) Icons.Filled.Sensors else Icons.Outlined.SensorsOff,
                            contentDescription = "Sensor Status",
                            tint = if (isStepTrackingActive && hasPermission) colorScheme.onPrimary else colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isStepTrackingActive && hasPermission) "AUTO SENSING" else "PAUSED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isStepTrackingActive && hasPermission) colorScheme.onPrimary else colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Permission Request Warning (If permission required on Android 10+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !hasPermission) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.errorContainer.copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.DirectionsWalk,
                        contentDescription = "Permission Required",
                        tint = colorScheme.onErrorContainer,
                        modifier = Modifier.size(28.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Activity Recognition Required",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = colorScheme.onErrorContainer
                        )
                        Text(
                            text = "Grant physical activity permission to count steps automatically with phone hardware.",
                            fontSize = 12.sp,
                            color = colorScheme.onErrorContainer.copy(alpha = 0.9f)
                        )
                    }
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.error
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Grant", fontSize = 12.sp)
                    }
                }
            }
        }

        // Hero Step Progress Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(210.dp)
                ) {
                    val trackColor = colorScheme.primary.copy(alpha = 0.15f)
                    val primaryColor = colorScheme.primary
                    val tertiaryColor = colorScheme.tertiary

                    Canvas(modifier = Modifier.size(210.dp)) {
                        drawCircle(
                            color = trackColor,
                            style = Stroke(width = 18.dp.toPx())
                        )
                        if (animatedProgress > 0f) {
                            drawArc(
                                brush = Brush.sweepGradient(
                                    colors = listOf(primaryColor, tertiaryColor, primaryColor)
                                ),
                                startAngle = -90f,
                                sweepAngle = animatedProgress * 360f,
                                useCenter = false,
                                style = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DirectionsWalk,
                                contentDescription = "Footsteps",
                                tint = colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "%,d".format(Locale.getDefault(), dailySteps),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colorScheme.onSurface,
                            modifier = Modifier.testTag("step_count_display")
                        )

                        Text(
                            text = "Target: %,d steps".format(Locale.getDefault(), targetSteps),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (progressFraction >= 1f) colorScheme.primary else colorScheme.secondaryContainer,
                            onClick = {
                                if (progressFraction >= 1f) {
                                    onTriggerCelebration(
                                        "Step Goal Reached! 🏆",
                                        "You've crushed your daily step target of $targetSteps steps!",
                                        "🏃‍♂️"
                                    )
                                }
                            }
                        ) {
                            Text(
                                text = if (progressFraction >= 1f) "Goal Reached! 🎉 Tap" else "${(progressFraction * 100).toInt()}% Achieved",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (progressFraction >= 1f) colorScheme.onPrimary else colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                // Quick Action Controls Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showTargetDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("set_target_steps_btn")
                    ) {
                        Icon(imageVector = Icons.Filled.Flag, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit Target", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { showManualStepDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                        modifier = Modifier.testTag("add_steps_manual_btn")
                    ) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Manual Steps", fontSize = 12.sp)
                    }
                }
            }
        }

        // Live Activity Statistics Grid
        Text(
            text = "Activity Breakdown",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onBackground
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Distance Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Map,
                            contentDescription = "Distance",
                            tint = colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text("Distance", fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                    }
                    Text(
                        text = "%.2f km".format(totalDistanceKm),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    Text(
                        text = "%.2f mi".format(totalDistanceMiles),
                        fontSize = 11.sp,
                        color = colorScheme.outline
                    )
                }
            }

            // Calories Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            imageVector = Icons.Filled.LocalFireDepartment,
                            contentDescription = "Burned",
                            tint = Color(0xFFFF6D00),
                            modifier = Modifier.size(18.dp)
                        )
                        Text("Step Burn", fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                    }
                    Text(
                        text = "${caloriesBurnedKcal.toInt()} kcal",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    Text(
                        text = "Est. Walking Energy",
                        fontSize = 11.sp,
                        color = colorScheme.outline
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Active Time Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Timer,
                            contentDescription = "Active Time",
                            tint = colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text("Active Walking", fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                    }
                    Text(
                        text = "$activeMinutes min",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    Text(
                        text = "Pace ~ 100 steps/m",
                        fontSize = 11.sp,
                        color = colorScheme.outline
                    )
                }
            }

            // Stride & Sensor Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Straighten,
                            contentDescription = "Stride",
                            tint = colorScheme.tertiary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text("Stride Length", fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                    }
                    Text(
                        text = if (dailySteps <= 0) "0.00 m" else if (strideMeters <= 0f) "--" else "%.2f m".format(strideMeters),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    Text(
                        text = if (dailySteps <= 0) "Start walking to calculate stride" else if (userHeightCm > 0f) "Auto-calculated from height" else "Set height in Profile",
                        fontSize = 11.sp,
                        color = if (strideMeters <= 0f) colorScheme.primary else colorScheme.outline,
                        maxLines = 1
                    )
                }
            }
        }

        // Automatic Sensor Tracking Controls & Simulator
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.secondaryContainer.copy(alpha = 0.35f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Sensors,
                            contentDescription = null,
                            tint = colorScheme.secondary
                        )
                        Column {
                            Text(
                                text = "Auto Step Sensor",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "Active hardware: $sensorTypeName",
                                fontSize = 11.sp,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = isStepTrackingActive,
                        onCheckedChange = { active ->
                            if (active) onStartTracking() else onPauseTracking()
                        },
                        modifier = Modifier.testTag("toggle_auto_step_tracking")
                    )
                }

                Divider(color = colorScheme.outlineVariant.copy(alpha = 0.5f))

                Text(
                    text = "Treadmill Walk Simulator (Test Ticks):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.onSecondaryContainer
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onSimulateStepTick(50) },
                        modifier = Modifier.weight(1f).testTag("simulate_50_steps_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.DirectionsWalk, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+50 Walk", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = { onSimulateStepTick(500) },
                        modifier = Modifier.weight(1f).testTag("simulate_500_steps_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.DirectionsRun, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+500 Jog", fontSize = 11.sp)
                    }

                    TextButton(
                        onClick = onResetDailySteps,
                        colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.error)
                    ) {
                        Text("Reset", fontSize = 12.sp)
                    }
                }
            }
        }

        // Weekly Steps History Chart
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Weekly Footsteps History",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )

                    val maxStepsInWeek = (stepHistory.maxOfOrNull { it.third } ?: 0).coerceAtLeast(1)
                    val avgStepsInWeek = if (stepHistory.isNotEmpty()) stepHistory.map { it.third }.average().toInt() else 0
                    Text(
                        text = "Avg: %,d steps/day".format(Locale.getDefault(), avgStepsInWeek),
                        fontSize = 11.sp,
                        color = colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                val maxBarHeight = 120.dp
                val maxVal = (stepHistory.maxOfOrNull { it.third } ?: targetSteps).coerceAtLeast(targetSteps)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    stepHistory.forEach { (_, dayLabel, stepsVal) ->
                        val ratio = (stepsVal.toFloat() / maxVal.toFloat()).coerceIn(0.05f, 1f)
                        val isGoalMet = stepsVal >= targetSteps

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (stepsVal > 0) "${stepsVal / 1000}k" else "0",
                                fontSize = 9.sp,
                                color = if (isGoalMet) colorScheme.primary else colorScheme.outline,
                                fontWeight = if (isGoalMet) FontWeight.Bold else FontWeight.Normal
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Box(
                                modifier = Modifier
                                    .width(20.dp)
                                    .height(maxBarHeight * ratio)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(
                                        if (isGoalMet) {
                                            Brush.verticalGradient(
                                                colors = listOf(colorScheme.primary, colorScheme.primaryContainer)
                                            )
                                        } else {
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    colorScheme.secondary.copy(alpha = 0.6f),
                                                    colorScheme.secondaryContainer.copy(alpha = 0.4f)
                                                )
                                            )
                                        }
                                    )
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = dayLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Target Steps Edit Dialog
    if (showTargetDialog) {
        AlertDialog(
            onDismissRequest = { showTargetDialog = false },
            title = { Text("Set Daily Footstep Target") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Choose a daily step goal:", fontSize = 13.sp, color = colorScheme.onSurfaceVariant)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(5000, 8000, 10000, 12000, 15000).forEach { preset ->
                            FilterChip(
                                selected = customTargetInput == preset.toString(),
                                onClick = { customTargetInput = preset.toString() },
                                label = { Text("${preset / 1000}k", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = customTargetInput,
                        onValueChange = { customTargetInput = it },
                        label = { Text("Custom Target Steps") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("custom_target_input_field")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = customTargetInput.toIntOrNull() ?: 10000
                        onSetTargetSteps(parsed)
                        showTargetDialog = false
                    }
                ) {
                    Text("Save Target")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTargetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Manual Steps Log Dialog
    if (showManualStepDialog) {
        AlertDialog(
            onDismissRequest = { showManualStepDialog = false },
            title = { Text("Add Manual Footsteps") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Enter steps to add to today's total (e.g. treadmill workout or outdoor walk):", fontSize = 13.sp, color = colorScheme.onSurfaceVariant)

                    OutlinedTextField(
                        value = manualStepsInput,
                        onValueChange = { manualStepsInput = it },
                        label = { Text("Steps Count") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("manual_steps_input_field")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(500, 1000, 2500, 5000).forEach { amount ->
                            OutlinedButton(
                                onClick = { manualStepsInput = amount.toString() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+$amount", fontSize = 10.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = manualStepsInput.toIntOrNull() ?: 0
                        if (parsed > 0) {
                            onAddManualSteps(parsed)
                        }
                        showManualStepDialog = false
                    }
                ) {
                    Text("Add Steps")
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualStepDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
