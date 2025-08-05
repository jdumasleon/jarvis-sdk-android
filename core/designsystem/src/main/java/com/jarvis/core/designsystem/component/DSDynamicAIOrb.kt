package com.jarvis.core.designsystem.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import kotlin.math.*

enum class AIState {
    Idle, Listening, Thinking, Speaking
}

data class StateConfig(
    val name: String,
    val colors: List<Color>,
    val speed: Float,
    val morphIntensity: Float
)

@Composable
fun DSDynamicAIOrb() {
    var aiState by remember { mutableStateOf(AIState.Idle) }

    val stateConfigs = mapOf(
        AIState.Idle to StateConfig(
            name = "💤 Dormant",
            colors = listOf(
                Color(0xFF667eea),
                Color(0xFF764ba2),
                Color(0xFFf093fb)
            ),
            speed = 1f,
            morphIntensity = 4f
        ),
        AIState.Listening to StateConfig(
            name = "👂 Listening",
            colors = listOf(
                Color(0xFF4facfe),
                Color(0xFF00f2fe),
                Color(0xFF43e97b)
            ),
            speed = 1f,
            morphIntensity = 1.2f
        ),
        AIState.Thinking to StateConfig(
            name = "🧠 Processing",
            colors = listOf(
                Color(0xFFF44336),
                Color(0xFFE91E63),
                Color(0xFF9C27B0)
            ),
            speed = 1f,
            morphIntensity = 1.2f
        ),
        AIState.Speaking to StateConfig(
            name = "💬 Responding",
            colors = listOf(
                Color(0xFF3F51B5),
                Color(0xFF2196F3),
                Color(0xFF03A9F4)
            ),
            speed = 1f,
            morphIntensity = 1.2f
        )
    )

    val states = listOf(AIState.Idle, AIState.Listening, AIState.Thinking, AIState.Speaking)
    val currentConfig = stateConfigs[aiState]!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "AI Assistant",
            fontSize = 32.sp,
            fontWeight = FontWeight.Light,
            color = Color.Black.copy(alpha = 0.9f),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Tap the orb to interact",
            fontSize = 16.sp,
            color = Color.Black.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 64.dp)
        )

        // Main AI Orb
        Box(
            modifier = Modifier
                .size(200.dp)
                .clickable {
                    val currentIndex = states.indexOf(aiState)
                    aiState = states[(currentIndex + 1) % states.size]
                },
            contentAlignment = Alignment.Center
        ) {
            DynamicOrbCanvas(
                config = currentConfig,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // State indicator
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.1f)
            ),
            shape = CircleShape
        ) {
            Text(
                text = currentConfig.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black.copy(alpha = 0.9f),
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Dynamic morphing • Flowing particles • Smooth animations",
            fontSize = 14.sp,
            color = Color.Black.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun DynamicOrbCanvas(
    config: StateConfig,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_animation")

    // Main time animation
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (10000 / config.speed).toInt(),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // Intensity animation
    val intensity by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (2000 / config.speed).toInt(),
                easing = EaseInOutSine
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "intensity"
    )

    Canvas(modifier = modifier) {
        val center = size.center
        val baseRadius = minOf(size.width, size.height) * 0.12f

        // Clear background
        drawRect(Color.Transparent, size = size)

        // Draw background waves
        repeat(3) { wave ->
            val waveRadius = baseRadius * (3 + wave) * (1 + intensity * 0.3f)
            val waveAlpha = (0.05f + config.morphIntensity * 0.03f) * (1 - wave * 0.3f)
            val color = config.colors[wave % config.colors.size]

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = waveAlpha),
                        Color.Transparent
                    ),
                    radius = waveRadius
                ),
                radius = waveRadius,
                center = center,
            )

            drawCircle(
                color = color.copy(alpha = waveAlpha),
                radius = waveRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Draw main morphing orb
        repeat(4) { layer ->
            val rotationSpeed = (layer + 1) * config.speed * 0.3f
            val rotation = time * rotationSpeed

            val morphPhase = time * config.speed * 0.02f + layer * PI.toFloat() * 0.5f
            val morphX = 1f + sin(morphPhase) * config.morphIntensity * 0.3f
            val morphY = 1f + cos(morphPhase * 1.3f) * config.morphIntensity * 0.2f

            val layerRadius = baseRadius * (1.2f - layer * 0.2f) * (1f + config.morphIntensity * 0.2f)
            val color1 = config.colors[layer % config.colors.size]
            val color2 = config.colors[(layer + 1) % config.colors.size]

            rotate(rotation, center) {
                scale(morphX, morphY, center) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                color1.copy(alpha = (0.7f - layer * 0.15f) * intensity),
                                color2.copy(alpha = (0.4f - layer * 0.1f) * intensity),
                                Color.Transparent
                            ),
                            radius = layerRadius
                        ),
                        radius = layerRadius,
                        center = center
                    )

                    drawCircle(
                        color = color1.copy(alpha = 0.05f * intensity),
                        radius = layerRadius,
                        center = center,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }
        }

        // Draw floating particles
        val particleCount = 10
        repeat(particleCount) { i ->
            val baseAngle = (i * (360f / particleCount))
            val angleVariation = sin((time + i * 30f) * PI / 180f) * 45f
            val angle = (baseAngle + angleVariation + time * config.speed) * PI / 180f

            val radiusVariation = sin((time * 1.5f + i * 45f) * PI / 180f) * 40f
            val radius = (80f + i * 8f + radiusVariation) * (1f + config.morphIntensity * 0.3f)

            val x = center.x + cos(angle) * radius
            val y = center.y + sin(angle) * radius

            val size = (3f + sin(i * 0.5f) * 2f) * (1f + config.morphIntensity * 0.4f) * intensity
            val alpha = (0.4f + sin(i * 0.8f) * 0.3f) * intensity

            val colorIndex = (i * config.colors.size / particleCount) % config.colors.size
            val color = config.colors[colorIndex]

            // Main particle
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = alpha),
                        Color.Transparent
                    ),
                    radius = size * 2f
                ),
                radius = size,
                center = Offset(x.toFloat(), y.toFloat())
            )

            // Particle trails for active states
            if (config.morphIntensity > 0.5f) {
                val trailLength = (config.morphIntensity * 4f).toInt()
                repeat(trailLength) { trail ->
                    val trailAngle = (baseAngle + angleVariation + time * config.speed - (trail + 1) * 10f) * PI / 180f
                    val trailX = center.x + cos(trailAngle) * radius
                    val trailY = center.y + sin(trailAngle) * radius
                    val trailAlpha = alpha * (1f - trail.toFloat() / trailLength) * 0.6f
                    val trailSize = size * (1f - trail * 0.2f)

                    if (trailSize > 0f && trailAlpha > 0f) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    color.copy(alpha = trailAlpha),
                                    Color.Transparent
                                ),
                                radius = trailSize
                            ),
                            radius = trailSize,
                            center = Offset(trailX.toFloat(), trailY.toFloat())
                        )
                    }
                }
            }
        }
    }
}

// Usage
@Composable
fun AIVoiceScreen() {
    DSJarvisTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
        ) {
            DSDynamicAIOrb()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AIVoiceScreenPreview() {
    DSJarvisTheme {
        AIVoiceScreen()
    }
}