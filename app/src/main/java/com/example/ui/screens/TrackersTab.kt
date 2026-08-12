package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.testTag
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.data.DietIntake
import com.example.data.WorkoutProgress
import com.example.data.EstimatedFoodNutrition
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrackersTab(
    selectedDate: String,
    workouts: List<WorkoutProgress>,
    diets: List<DietIntake>,
    waterIntakeMl: Int = 0,
    isWaterReminderEnabled: Boolean = false,
    waterReminderIntervalHours: Int = 2,
    onSetWaterReminder: (Boolean, Int) -> Unit = { _, _ -> },
    onSendTestWaterNotification: () -> Unit = {},
    isEstimatingFood: Boolean = false,
    onEstimateFood: (String, String, (EstimatedFoodNutrition) -> Unit) -> Unit = { _, _, _ -> },
    onAddWater: (Int) -> Unit = {},
    onResetWater: () -> Unit = {},
    onSelectDate: (String) -> Unit,
    onAddWorkout: (String, Int, String, Int) -> Unit,
    onDeleteWorkout: (WorkoutProgress) -> Unit,
    onAddDiet: (String, String, Int, Int, Int, Int) -> Unit,
    onDeleteDiet: (DietIntake) -> Unit,
    onClearAllLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var showAddWorkoutDialog by remember { mutableStateOf(false) }
    var showAddDietDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    val colorScheme = MaterialTheme.colorScheme

    // Calorie & Macro calculator for headers
    val totalCalsBurned = workouts.sumOf { it.caloriesBurned }
    val totalCalsConsumed = diets.sumOf { it.calories }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Horizontal Date Wheel Picker Header with Reset Option
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Select Log Date",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurfaceVariant
            )

            TextButton(
                onClick = { showResetConfirmDialog = true },
                colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.error),
                modifier = Modifier.height(32.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Logs",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset All Logs", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        DateWheelRow(
            selectedDate = selectedDate,
            onSelectDate = onSelectDate
        )





        // 4. Nutrition Intake Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Restaurant, contentDescription = "Diet", tint = colorScheme.primary)
                        Text(
                            text = "Nutritional Intake (Logs)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(colorScheme.primaryContainer, CircleShape)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "+$totalCalsConsumed kcal",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onPrimaryContainer
                        )
                    }
                }

                if (diets.isEmpty()) {
                    EmptySectionText("No calorie intake logged yet.")
                } else {
                    diets.forEach { item ->
                        DietRowItem(
                            item = item,
                            onDelete = { onDeleteDiet(item) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = { showAddDietDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add diet log")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Log Food / Snack")
                }
            }
        }

        // 4. Interactive Aqueous Water Monitor
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
                            imageVector = Icons.Default.LocalDrink,
                            contentDescription = "Water Tracker",
                            tint = Color(0xFF33B5E5)
                        )
                        Text(
                            text = "Aqueous Water Monitor",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "$waterIntakeMl / 2500 ml",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF33B5E5)
                    )
                }

                // Wave/Liquid percentage bar
                val currentPercent = if (waterIntakeMl > 0) (waterIntakeMl.toFloat() / 2500f).coerceIn(0f, 1f) else 0f
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { currentPercent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                            .clip(RoundedCornerShape(9.dp)),
                        color = Color(0xFF33B5E5),
                        trackColor = Color(0xFF33B5E5).copy(alpha = 0.15f)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (currentPercent >= 1.0f) "Maximum hydration reached! Excellent." else "Track and hit standard hydration quotas.",
                            fontSize = 10.sp,
                            color = colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${(currentPercent * 100).toInt()}%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Interaction controls row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onAddWater(250) },
                        modifier = Modifier.weight(1f).testTag("add_water_250_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF33B5E5))
                    ) {
                        Text("+ 250 ml", fontSize = 11.sp, color = Color.White)
                    }

                    Button(
                        onClick = { onAddWater(500) },
                        modifier = Modifier.weight(1f).testTag("add_water_500_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0099CC))
                    ) {
                        Text("+ 500 ml", fontSize = 11.sp, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = { onResetWater() },
                        modifier = Modifier.weight(0.8f).testTag("reset_water_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Reset", fontSize = 11.sp)
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                // Recurring Notification Reminders Section
                val context = LocalContext.current
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        onSetWaterReminder(true, waterReminderIntervalHours)
                    } else {
                        onSetWaterReminder(false, waterReminderIntervalHours)
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF33B5E5).copy(alpha = 0.15f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "Water Reminders",
                                    tint = Color(0xFF33B5E5),
                                    modifier = Modifier.padding(6.dp).size(16.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Hydration Reminders",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface
                                )
                                Text(
                                    text = if (isWaterReminderEnabled) "Reminders active every $waterReminderIntervalHours hr" else "Get periodic notifications to drink water",
                                    fontSize = 11.sp,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = isWaterReminderEnabled,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    if (Build.VERSION.SDK_INT >= 33) {
                                        val hasPermission = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.POST_NOTIFICATIONS
                                        ) == PackageManager.PERMISSION_GRANTED

                                        if (hasPermission) {
                                            onSetWaterReminder(true, waterReminderIntervalHours)
                                        } else {
                                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        }
                                    } else {
                                        onSetWaterReminder(true, waterReminderIntervalHours)
                                    }
                                } else {
                                    onSetWaterReminder(false, waterReminderIntervalHours)
                                }
                            },
                            modifier = Modifier.testTag("water_reminder_switch")
                        )
                    }

                    if (isWaterReminderEnabled) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Notification Frequency",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurfaceVariant
                            )

                            val intervals = listOf(1, 2, 3, 4)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                intervals.forEach { hrs ->
                                    FilterChip(
                                        selected = waterReminderIntervalHours == hrs,
                                        onClick = { onSetWaterReminder(true, hrs) },
                                        label = { Text("Every ${hrs}h", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        if (Build.VERSION.SDK_INT >= 33) {
                                            val hasPermission = ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.POST_NOTIFICATIONS
                                            ) == PackageManager.PERMISSION_GRANTED
                                            if (!hasPermission) {
                                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                                return@TextButton
                                            }
                                        }
                                        onSendTestWaterNotification()
                                    },
                                    modifier = Modifier.testTag("send_test_water_notif_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Test Notification",
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Send Test Reminder", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal dialogs trigger
    if (showAddWorkoutDialog) {
        AddWorkoutAlertDialog(
            onDismiss = { showAddWorkoutDialog = false },
            onConfirm = { name, duration, intensity, burned ->
                onAddWorkout(name, duration, intensity, burned)
                showAddWorkoutDialog = false
            }
        )
    }

    if (showAddDietDialog) {
        AddDietAlertDialog(
            isEstimatingFood = isEstimatingFood,
            onEstimateFood = onEstimateFood,
            onDismiss = { showAddDietDialog = false },
            onConfirm = { meal, name, cal, protein, carb, fat ->
                onAddDiet(meal, name, cal, protein, carb, fat)
                showAddDietDialog = false
            }
        )
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Reset Tracker Logs", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete all workout progress, diets, and AI recommendations? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllLogs()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error)
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DateWheelRow(
    selectedDate: String,
    onSelectDate: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val monthDayFormat = SimpleDateFormat("d", Locale.getDefault())
    val weekDayFormat = SimpleDateFormat("E", Locale.getDefault()) // "M", "T" etc

    val dates = remember {
        (6 downTo 0).map { offset ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -offset)
            cal.time
        }
    }

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(dates) { date ->
            val dateKey = sdf.format(date)
            val monthDay = monthDayFormat.format(date)
            val weekDay = weekDayFormat.format(date).take(3)
            val isSelected = dateKey == selectedDate

            Box(
                modifier = Modifier
                    .width(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isSelected) colorScheme.primary else colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                    .clickable { onSelectDate(dateKey) }
                    .padding(vertical = 12.dp, horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = weekDay.uppercase(Locale.getDefault()),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) colorScheme.onPrimary else colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = monthDay,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isSelected) colorScheme.onPrimary else colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun EmptySectionText(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp)
    )
}

@Composable
fun WorkoutRowItem(
    item: WorkoutProgress,
    onDelete: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp),
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
                    text = item.exerciseName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Text(
                    text = "${item.durationMin} mins • Intensity: ${item.intensity}",
                    fontSize = 11.sp,
                    color = colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "-${item.caloriesBurned} kcal",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = colorScheme.tertiary
                )

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DietRowItem(
    item: DietIntake,
    onDelete: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.foodName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    Text(
                        text = item.mealType,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "+${item.calories} kcal",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = colorScheme.primary
                    )

                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Micronutrient indicators row
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "P: ${item.proteinGram}g",
                    fontSize = 10.sp,
                    color = colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "C: ${item.carbsGram}g",
                    fontSize = 10.sp,
                    color = colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "F: ${item.fatGram}g",
                    fontSize = 10.sp,
                    color = colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun AddWorkoutAlertDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int, String, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var durationStr by remember { mutableStateOf("") }
    var caloriesStr by remember { mutableStateOf("") }
    var intensity by remember { mutableStateOf("Medium") }

    val intensities = listOf("Low", "Medium", "High")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Log Workout Activity", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Exercise Name (e.g. Boxing, Yoga)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = durationStr,
                        onValueChange = { durationStr = it },
                        label = { Text("Mins") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = caloriesStr,
                        onValueChange = { caloriesStr = it },
                        label = { Text("Calories (kcal)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1.2f)
                    )
                }

                Text("Intensity level", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    intensities.forEach { lvl ->
                        FilterChip(
                            selected = intensity == lvl,
                            onClick = { intensity = lvl },
                            label = { Text(lvl) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val duration = durationStr.toIntOrNull() ?: 0
                    val burned = caloriesStr.toIntOrNull() ?: 0
                    if (name.isNotEmpty() && duration > 0) {
                        onConfirm(name, duration, intensity, burned)
                    }
                }
            ) {
                Text("Add Entry")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddDietAlertDialog(
    isEstimatingFood: Boolean = false,
    onEstimateFood: ((String, String, (EstimatedFoodNutrition) -> Unit) -> Unit)? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, Int, Int, Int) -> Unit
) {
    var foodName by remember { mutableStateOf("") }
    var quantityConsumed by remember { mutableStateOf("") }
    var mealType by remember { mutableStateOf("Snack") }
    var caloriesStr by remember { mutableStateOf("") }
    var proteinStr by remember { mutableStateOf("") }
    var carbsStr by remember { mutableStateOf("") }
    var fatStr by remember { mutableStateOf("") }
    var estimationNote by remember { mutableStateOf("") }

    val meals = listOf("Breakfast", "Lunch", "Dinner", "Snack")
    val colorScheme = MaterialTheme.colorScheme

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Log Nutritional Intake", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Select Meal Group", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    meals.forEach { meal ->
                        FilterChip(
                            selected = mealType == meal,
                            onClick = { mealType = meal },
                            label = { Text(meal, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = foodName,
                    onValueChange = { foodName = it },
                    label = { Text("Food Name (e.g. Biryani, Pizza, Omelet)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = quantityConsumed,
                    onValueChange = { quantityConsumed = it },
                    label = { Text("Quantity Consumed (e.g. 1 plate, 200g, 2 slices)") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (onEstimateFood != null) {
                    FilledTonalButton(
                        onClick = {
                            if (foodName.isNotBlank()) {
                                val qty = if (quantityConsumed.isBlank()) "1 portion" else quantityConsumed
                                onEstimateFood(foodName, qty) { est ->
                                    caloriesStr = est.calories.toString()
                                    proteinStr = est.protein.toString()
                                    carbsStr = est.carbs.toString()
                                    fatStr = est.fat.toString()
                                    estimationNote = est.note
                                }
                            }
                        },
                        enabled = foodName.isNotBlank() && !isEstimatingFood,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isEstimatingFood) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Auto Converting...", fontSize = 12.sp)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("⚡ Auto Convert to Calories & Macros", fontSize = 12.sp)
                        }
                    }
                }

                if (estimationNote.isNotEmpty()) {
                    Text(
                        text = "Note: $estimationNote",
                        fontSize = 10.sp,
                        color = colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                OutlinedTextField(
                    value = caloriesStr,
                    onValueChange = { caloriesStr = it },
                    label = { Text("Calories (kcal)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = proteinStr,
                        onValueChange = { proteinStr = it },
                        label = { Text("Protein (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = carbsStr,
                        onValueChange = { carbsStr = it },
                        label = { Text("Carbs (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = fatStr,
                        onValueChange = { fatStr = it },
                        label = { Text("Fat (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cal = caloriesStr.toIntOrNull() ?: 0
                    val prot = proteinStr.toIntOrNull() ?: 0
                    val carb = carbsStr.toIntOrNull() ?: 0
                    val fat = fatStr.toIntOrNull() ?: 0
                    if (foodName.isNotEmpty() && cal >= 0) {
                        val fullNameWithQty = if (quantityConsumed.isNotBlank() && !foodName.contains(quantityConsumed)) {
                            "$foodName ($quantityConsumed)"
                        } else {
                            foodName
                        }
                        onConfirm(mealType, fullNameWithQty, cal, prot, carb, fat)
                    }
                }
            ) {
                Text("Save Log")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
