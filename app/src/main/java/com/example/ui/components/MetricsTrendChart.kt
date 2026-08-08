package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DietIntake
import com.example.data.WorkoutProgress
import java.text.SimpleDateFormat
import java.util.*

data class DayMetric(val dateKey: String, val label: String, val consumed: Float, val burned: Float)

@Composable
fun MetricsTrendChart(
    workouts: List<WorkoutProgress>,
    diets: List<DietIntake>,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val consumeGradient = Brush.verticalGradient(
        colors = listOf(colorScheme.primary, colorScheme.primaryContainer)
    )
    val burnGradient = Brush.verticalGradient(
        colors = listOf(colorScheme.tertiary, colorScheme.tertiaryContainer)
    )

    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val labelFormat = SimpleDateFormat("E", Locale.getDefault()) // "Mon", "Tue"

    // Generate indices for the past 7 calendar days chronologically
    val days = (6 downTo 0).map { offset ->
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -offset)
        cal.time
    }

    val dataMapped = days.map { date ->
        val dateKey = simpleDateFormat.format(date)
        val shortLabel = labelFormat.format(date)
        val consumed = diets.filter { it.date == dateKey }.sumOf { it.calories }.toFloat()
        val burned = workouts.filter { it.date == dateKey }.sumOf { it.caloriesBurned }.toFloat()
        DayMetric(dateKey, shortLabel, consumed, burned)
    }

    // Find upper bound scale dynamically, ensuring a baseline of at least 1500 limit
    val maxVal = dataMapped.maxOfOrNull { maxOf(it.consumed, it.burned, 1500f) } ?: 2000f

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "7-Day Energy Balance",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .padding(end = 4.dp)
                    )
                    Text("Diet", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.primary)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .padding(end = 4.dp)
                    )
                    Text("Workout", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.tertiary)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Custom Canvas Render Layer
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            val width = size.width
            val height = size.height - 18.dp.toPx() // Keep space for labeling row
            val sliceWidth = width / 7f
            val maxBarWidth = sliceWidth * 0.28f

            // Baseline threshold line
            drawLine(
                color = colorScheme.outlineVariant.copy(alpha = 0.5f),
                start = Offset(0f, height),
                end = Offset(width, height),
                strokeWidth = 1.dp.toPx()
            )

            // Draw 50% grid indicator
            drawLine(
                color = colorScheme.outlineVariant.copy(alpha = 0.2f),
                start = Offset(0f, height / 2f),
                end = Offset(width, height / 2f),
                strokeWidth = 0.5.dp.toPx()
            )

            dataMapped.forEachIndexed { index, m ->
                val groupCenterX = (index * sliceWidth) + (sliceWidth / 2f)

                // Consumed Calories Bar (Primary)
                val cBarHeight = (m.consumed / maxVal) * height
                val cBarTop = height - cBarHeight
                if (cBarHeight > 4f) {
                    drawRoundRect(
                        brush = consumeGradient,
                        topLeft = Offset(groupCenterX - maxBarWidth - 2.dp.toPx(), y = cBarTop),
                        size = Size(maxBarWidth, cBarHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }

                // Burned Calories Bar (Tertiary)
                val bBarHeight = (m.burned / maxVal) * height
                val bBarTop = height - bBarHeight
                if (bBarHeight > 4f) {
                    drawRoundRect(
                        brush = burnGradient,
                        topLeft = Offset(groupCenterX + 2.dp.toPx(), y = bBarTop),
                        size = Size(maxBarWidth, bBarHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            dataMapped.forEach { m ->
                Text(
                    text = m.label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
