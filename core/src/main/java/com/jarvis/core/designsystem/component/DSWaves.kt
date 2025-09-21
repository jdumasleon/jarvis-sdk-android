@file:Suppress("FunctionName")

package com.jarvis.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.core.designsystem.theme.JarvisPink
import kotlinx.coroutines.isActive

// -----------------------------------------------------
// Wave Animation State (following withFrameNanos pattern)
// -----------------------------------------------------
data class Wave(
    val progress: Float,
    val speed: Float = 0.0001f // Progress per millisecond - ultra slow for debugging
)

data class WaveAnimationState(
    val waves: List<Wave> = listOf(
        Wave(progress = 0f),
        Wave(progress = 0.33f),
        Wave(progress = 0.66f)
    ),
    val spawnTimer: Float = 0.5f
) {
    fun nextFrame(deltaMs: Float): WaveAnimationState {
        // Update existing waves
        val updatedWaves = waves.map { wave ->
            val newProgress = (wave.progress + wave.speed * deltaMs).coerceIn(0f, 1f)
            wave.copy(progress = newProgress)
        }.filter { it.progress < 1f } // Remove completed waves
        
        // Add new wave if needed
        val newTimer = spawnTimer + deltaMs
        val shouldSpawnWave = newTimer > 3000f && (updatedWaves.isEmpty() || updatedWaves.first().progress > 0.2f)
        
        return if (shouldSpawnWave) {
            copy(
                waves = updatedWaves + Wave(progress = 0f),
                spawnTimer = 0f
            )
        } else {
            copy(
                waves = updatedWaves,
                spawnTimer = newTimer
            )
        }
    }
}

@Composable
fun DSWavesAnimations(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    showWaveAnimation: Boolean = true
) {

    // State for wave animation - similar to dots and lines approach
    var waveState by remember { mutableStateOf(WaveAnimationState()) }

    // Use withFrameNanos pattern with frame limiting
    LaunchedEffect(showWaveAnimation) {
        if (!showWaveAnimation) {
            // Clear waves when animation is disabled
            waveState = WaveAnimationState(waves = emptyList())
            return@LaunchedEffect
        }

        var lastFrame = withFrameNanos { it }
        var frameCount = 0
        while (isActive) {
            withFrameNanos { frameTime ->
                frameCount++
                // Only update every 2nd frame (30fps instead of 60fps)
                if (frameCount % 2 == 0) {
                    val deltaMs = (frameTime - lastFrame) / 1_000_000f
                    lastFrame = frameTime

                    // Update wave state based on elapsed time
                    waveState = waveState.nextFrame(deltaMs)
                }
            }
        }
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Wave animation using Canvas
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = this.size.minDimension / 8f
            val maxRadius = this.size.minDimension / 2.5f

            // Draw waves based on current state
            waveState.waves.forEachIndexed { i, wave ->
                val alpha = (1f - wave.progress) * 0.5f
                if (alpha > 0.05f) {
                    val radius = baseRadius + wave.progress * maxRadius
                    drawCircle(
                        color = JarvisPink.copy(alpha = alpha),
                        radius = radius,
                        center = center,
                        style = Stroke(width = 2.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun DSExpandingWavesDrawBehind(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF2962FF),
    waves: Int = 3,
    durationMs: Int = 5000,
    strokeWidth: Dp = 2.dp,
    maxRadiusFraction: Float = 0.9f,
    showCenterDot: Boolean = true,
    size: Dp = 100.dp
) {
    val t by rememberInfiniteTransition(label = "waves").animateFloat(
        0f, 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "t"
    )

    val strokePx = with(LocalDensity.current) { strokeWidth.toPx() }
    val wavesInt = remember(waves) { waves.coerceAtLeast(1) }
    val colorState by rememberUpdatedState(color) // si cambia el color, no resetea la anim

    Box(
        modifier
            .size(size)
            .aspectRatio(1f)
            .drawWithCache {
                val center = this.size.center
                val maxR = (this.size.minDimension / 2f) * maxRadiusFraction

                onDrawBehind {
                    // 3) Solo draw ops por frame
                    repeat(wavesInt) { i ->
                        val offset = i.toFloat() / wavesInt
                        val p = ((t + offset) % 1f)
                        val r = p * maxR
                        val a = 1f - p
                        drawCircle(
                            color = colorState.copy(alpha = a),
                            radius = r,
                            center = center,
                            style = Stroke(width = strokePx)
                        )
                    }
                    if (showCenterDot) {
                        drawCircle(
                            color = colorState,
                            radius = strokePx * 1.5f,
                            center = center
                        )
                    }
                }
            }
    )
}

// -----------------------------------------------------
// Previews
// -----------------------------------------------------
@Preview(showBackground = true, name = "Animated (run app to see)")
@Composable
fun DSJarvisAssistantAnimatedPreview() {
    DSJarvisTheme {
        DSWavesAnimations(showWaveAnimation = true)
    }
}

@Preview(showBackground = true, name = "Animated (run app to see)")
@Composable
fun DSExpandingWavesDrawBehindPreview() {
    DSJarvisTheme {
        DSExpandingWavesDrawBehind(
            modifier = Modifier.size(200.dp),
            color = DSJarvisTheme.colors.extra.jarvisPink
        )
    }
}