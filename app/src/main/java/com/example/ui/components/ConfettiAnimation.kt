package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class CelebrationEvent(
    val title: String,
    val subtitle: String,
    val emoji: String = "🎉",
    val id: Long = System.currentTimeMillis()
)

private data class ConfettiParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var size: Float,
    var color: Color,
    var rotation: Float,
    var rotationSpeed: Float,
    var shape: Int, // 0 = rect, 1 = circle, 2 = strip, 3 = star
    var alpha: Float = 1f,
    var horizontalOscillation: Float = Random.nextFloat() * 2 * PI.toFloat()
)

@Composable
fun ConfettiOverlay(
    event: CelebrationEvent?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (event == null) return

    var isVisible by remember(event.id) { mutableStateOf(true) }

    // Auto dismiss after 4.5 seconds
    LaunchedEffect(event.id) {
        delay(4200)
        isVisible = false
        delay(300)
        onDismiss()
    }

    Popup(
        alignment = Alignment.TopCenter,
        properties = PopupProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
        onDismissRequest = {
            isVisible = false
            onDismiss()
        }
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(top = 40.dp)
        ) {
            // Background Canvas Confetti
            ConfettiCanvas(
                modifier = Modifier.fillMaxSize(),
                isPlaying = isVisible
            )

            // Celebratory Banner Popup
            AnimatedVisibility(
                visible = isVisible,
                enter = scaleIn(animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                exit = scaleOut() + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 12.dp,
                    shadowElevation = 16.dp,
                    modifier = Modifier.testTag("celebration_popup_card")
                ) {
                    Row(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = event.emoji,
                                    fontSize = 28.sp
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = event.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = event.subtitle,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = {
                                isVisible = false
                                onDismiss()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text("✕", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConfettiCanvas(
    modifier: Modifier = Modifier,
    isPlaying: Boolean = true
) {
    val confettiColors = listOf(
        Color(0xFF0D9488), // Emerald
        Color(0xFF4F46E5), // Indigo
        Color(0xFFD97706), // Amber
        Color(0xFFEC4899), // Pink
        Color(0xFF06B6D4), // Cyan
        Color(0xFF8B5CF6), // Purple
        Color(0xFF10B981), // Green
        Color(0xFFF59E0B), // Gold
        Color(0xFFEF4444)  // Red/Coral
    )

    val particles = remember { mutableStateListOf<ConfettiParticle>() }

    // Initialize particles when isPlaying becomes true
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            particles.clear()
            val particleCount = 100
            for (i in 0 until particleCount) {
                val startXPercent = Random.nextFloat()
                val angle = Random.nextFloat() * PI.toFloat() // upward and outward angles
                val speed = Random.nextFloat() * 18f + 10f
                particles.add(
                    ConfettiParticle(
                        x = startXPercent,
                        y = -0.05f - (Random.nextFloat() * 0.2f), // spawn slightly above screen
                        vx = (cos(angle) * speed * 0.001f) + ((Random.nextFloat() - 0.5f) * 0.002f),
                        vy = (Random.nextFloat() * 0.008f) + 0.003f, // downward velocity
                        size = Random.nextFloat() * 12f + 8f,
                        color = confettiColors.random(),
                        rotation = Random.nextFloat() * 360f,
                        rotationSpeed = (Random.nextFloat() - 0.5f) * 12f,
                        shape = Random.nextInt(4)
                    )
                )
            }
        }
    }

    var frameTimeNanos by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isPlaying) {
        if (!isPlaying) return@LaunchedEffect
        val startTime = System.currentTimeMillis()
        while (isPlaying && (System.currentTimeMillis() - startTime) < 4000) {
            withFrameNanos { nanos ->
                frameTimeNanos = nanos
                val elapsedRatio = (System.currentTimeMillis() - startTime) / 4000f

                for (p in particles) {
                    p.x += p.vx + (sin(p.horizontalOscillation) * 0.0008f)
                    p.y += p.vy
                    p.vy += 0.00018f // Gravity acceleration
                    p.rotation += p.rotationSpeed
                    p.horizontalOscillation += 0.05f

                    if (elapsedRatio > 0.6f) {
                        p.alpha = ((1f - elapsedRatio) / 0.4f).coerceIn(0f, 1f)
                    }
                }
            }
        }
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        for (p in particles) {
            if (p.alpha <= 0f) continue
            val px = p.x * width
            val py = p.y * height

            if (py > height + 50 || py < -200) continue

            withTransform({
                rotate(p.rotation, pivot = Offset(px, py))
            }) {
                when (p.shape) {
                    0 -> { // Rectangle
                        drawRoundRect(
                            color = p.color.copy(alpha = p.alpha),
                            topLeft = Offset(px - p.size / 2, py - p.size / 2),
                            size = Size(p.size, p.size * 0.6f),
                            cornerRadius = CornerRadius(2f, 2f)
                        )
                    }
                    1 -> { // Circle
                        drawCircle(
                            color = p.color.copy(alpha = p.alpha),
                            radius = p.size / 2,
                            center = Offset(px, py)
                        )
                    }
                    2 -> { // Ribbon strip
                        drawRoundRect(
                            color = p.color.copy(alpha = p.alpha),
                            topLeft = Offset(px - p.size / 4, py - p.size),
                            size = Size(p.size / 2, p.size * 1.8f),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                    }
                    else -> { // Star / Diamond
                        val path = Path().apply {
                            moveTo(px, py - p.size)
                            lineTo(px + p.size / 2, py)
                            lineTo(px, py + p.size)
                            lineTo(px - p.size / 2, py)
                            close()
                        }
                        drawPath(
                            path = path,
                            color = p.color.copy(alpha = p.alpha)
                        )
                    }
                }
            }
        }
    }
}
