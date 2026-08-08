package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CenteredProgressRing(
    consumed: Float,
    target: Float,
    burned: Float,
    modifier: Modifier = Modifier
) {
    // Remaining calories calculator: Target - Consumed + Burned
    val remaining = target - consumed + burned
    val rawProgress = if (target > 0) consumed / (target + burned) else 0f
    val progress = rawProgress.coerceIn(0f, 1f)

    val colorScheme = MaterialTheme.colorScheme
    val trackColor = colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val progressColor = colorScheme.primary
    val accentColor = colorScheme.tertiary

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Canvas(modifier = Modifier.size(160.dp)) {
            // Draw background slider track
            drawCircle(
                color = trackColor,
                style = Stroke(width = 12.dp.toPx())
            )

            if (progress > 0f) {
                // Draw elegant primary brush progress
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(progressColor, accentColor, progressColor)
                    ),
                    startAngle = -90f,
                    sweepAngle = progress * 360f,
                    useCenter = false,
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${remaining.toInt()}",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = colorScheme.onSurface
            )
            Text(
                text = "kcal remaining",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .padding(horizontal = 4.dp)
                )
                Text(
                    text = "Base: ${target.toInt()} • Burned: +${burned.toInt()}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.secondary
                )
            }
        }
    }
}
