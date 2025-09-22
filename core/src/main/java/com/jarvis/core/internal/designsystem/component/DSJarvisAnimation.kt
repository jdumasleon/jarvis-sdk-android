@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@file:Suppress("FunctionName")

package com.jarvis.core.internal.designsystem.component

import androidx.annotation.RestrictTo

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.jarvis.core.R
import com.jarvis.core.internal.designsystem.theme.DSJarvisTheme
import com.jarvis.core.internal.designsystem.theme.JarvisPink
import kotlinx.coroutines.isActive
import kotlin.math.absoluteValue
import kotlin.math.min

// --- Frame Health Monitor ----------------------
// Monitors frame timing with withFrameNanos and applies hysteresis:
// - Healthy: ~60fps
// - Throttled: moderate jank → lower to ~45fps
// - Paused: sustained heavy jank → pause animations
private enum class FrameHealthState { Healthy, Throttled, Paused }

@Stable
private data class FrameHealth(
    val state: FrameHealthState,
    val fpsEstimate: Int,
    val frameStepNanos: Long
)

@Composable
private fun rememberFrameHealthMonitor(
    targetFps: Int = 60,
    warnMs: Long = 18,         // moderate jank (≈ >16.6ms per frame)
    criticalMs: Long = 28,     // heavy jank threshold
    burstForThrottle: Int = 3, // consecutive frames > warn → Throttled
    burstForPause: Int = 2,    // consecutive frames > critical → Paused
    recoveryStableMs: Long = 1500 // ms stable before recovering state
): State<FrameHealth> {
    val state = remember { mutableStateOf(FrameHealth(FrameHealthState.Healthy, targetFps, 0L)) }

    LaunchedEffect(Unit) {
        var last = withFrameNanos { it }
        var consecWarn = 0
        var consecCritical = 0
        var lastBadTs = 0L
        var emaMs = warnMs.toDouble() // Exponential moving average for smoother fps estimate
        val alpha = 0.2

        while (true) {
            val now = withFrameNanos { it }
            val dtMs = (now - last) / 1_000_000.0
            last = now

            // Update EMA
            emaMs = alpha * dtMs + (1 - alpha) * emaMs
            val fps = (1000.0 / emaMs).coerceIn(1.0, 120.0).toInt()

            // Track bursts of slow frames
            when {
                dtMs >= criticalMs -> { consecCritical++; consecWarn++; lastBadTs = now }
                dtMs >= warnMs     -> { consecWarn++; consecCritical = 0; lastBadTs = now }
                else               -> { consecWarn = 0; consecCritical = 0 }
            }

            // State transitions with hysteresis
            val current = state.value.state
            val next = when (current) {
                FrameHealthState.Healthy -> when {
                    consecCritical >= burstForPause -> FrameHealthState.Paused
                    consecWarn >= burstForThrottle  -> FrameHealthState.Throttled
                    else -> FrameHealthState.Healthy
                }
                FrameHealthState.Throttled -> {
                    val stable = (now - lastBadTs) > recoveryStableMs * 1_000_000
                    when {
                        consecCritical >= burstForPause -> FrameHealthState.Paused
                        stable -> FrameHealthState.Healthy
                        else -> FrameHealthState.Throttled
                    }
                }
                FrameHealthState.Paused -> {
                    val stable = (now - lastBadTs) > (recoveryStableMs * 2) * 1_000_000
                    when {
                        stable -> FrameHealthState.Throttled
                        else -> FrameHealthState.Paused
                    }
                }
            }

            // Suggest step duration (0 = vsync, >0 = lower fps, Long.MAX_VALUE = pause)
            val step = when (next) {
                FrameHealthState.Healthy   -> 0L
                FrameHealthState.Throttled -> 22_222_222L   // ~45fps
                FrameHealthState.Paused    -> Long.MAX_VALUE
            }

            state.value = FrameHealth(next, fps, step)
        }
    }

    return state
}

// -----------------------------------------------------
// Vector → Bitmap cache (pre-renders vector layers once per size/density)
// -----------------------------------------------------
@Composable
private fun rememberRingBitmaps(
    layerIds: List<Int>,
    targetPx: Int,                  // square canvas size in px
    contentScale: Float,            // 0..1 inner padding
    viewportW: Float,
    viewportH: Float,
    pivotX: Float,
    pivotY: Float
): List<ImageBitmap> {
    val density = LocalDensity.current
    val vectors = layerIds.map { id -> ImageVector.vectorResource(id) }
    val painters = vectors.map { v -> rememberVectorPainter(v) }

    return remember(targetPx, layerIds, contentScale, viewportW, viewportH, pivotX, pivotY) {
        val inner = targetPx * contentScale
        val s = min(inner / viewportW, inner / viewportH)

        val drawW = viewportW * s
        val drawH = viewportH * s
        val center = targetPx / 2f

        // Translate so that (pivotX, pivotY) maps to the canvas center
        val tx = center - (drawW / 2f) + (viewportW / 2f - pivotX) * s
        val ty = center - (drawH / 2f) + (viewportH / 2f - pivotY) * s

        val scope = CanvasDrawScope()
        val canvasSize = Size(targetPx.toFloat(), targetPx.toFloat())

        painters.map { p ->
            val bmp = ImageBitmap(targetPx, targetPx)
            val canvas = Canvas(bmp)

            scope.draw(
                density = density,
                layoutDirection = LayoutDirection.Ltr,
                canvas = canvas,
                size = canvasSize
            ) {
                withTransform({
                    translate(left = tx, top = ty)
                    scale(scaleX = s, scaleY = s)
                }) {
                    with(p) { draw(size = Size(viewportW, viewportH)) }
                }
            }
            bmp
        }
    }
}

// -----------------------------------------------------
// REAL-TIME, DRAW-ONLY ring animation
// - rotating=true: continuously rotates in real-time
// - rotating=false: shows frozen rings at last captured angle
// -----------------------------------------------------
@Composable
fun DSJarvisRingsRealtime(
    size: Dp = 100.dp,
    periodsMs: List<Float> = listOf(3000f, -4000f, 5000f, -5000f, 6000f),
    layerIds: List<Int> = listOf(
        R.drawable.ic_ring0,
        R.drawable.ic_ring1,
        R.drawable.ic_ring2,
        R.drawable.ic_ring3,
        R.drawable.ic_ring4
    ),
    viewport: Size = Size(554.67f, 446.30f),
    pivotInViewport: Offset = Offset(450f, 400f),
    contentScale: Float = 0.98f,
    frameStepNanos: Long = 0L,
    rotating: Boolean = true
) {
    val density = LocalDensity.current
    val px = with(density) { size.toPx() }.toInt().coerceAtLeast(1)
    val bg = DSJarvisTheme.colors.extra.surface

    val bitmaps = rememberRingBitmaps(
        layerIds, px, contentScale,
        viewport.width, viewport.height,
        pivotInViewport.x, pivotInViewport.y
    )

    val center = Offset(px / 2f, px / 2f)

    // Trigger to invalidate when rotating
    val animationTriggerState = if (rotating) {
        val t = rememberInfiniteTransition(label = "rings")
        t.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = if (frameStepNanos > 0) (frameStepNanos / 1_000_000L).toInt() else 16,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "trigger"
        )
    } else {
        remember { mutableStateOf(0f) }
    }
    val animationTrigger by animationTriggerState

    // Capture frozen angle when disabling rotation
    val frozenTimeNs = remember { mutableLongStateOf(0L) }
    LaunchedEffect(rotating, periodsMs) {
        if (!rotating) frozenTimeNs.longValue = System.nanoTime()
    }

    Box(
        Modifier
            .size(size)
            .drawBehind {
                animationTrigger // forces invalidation

                val baseNow = if (rotating) System.nanoTime() else frozenTimeNs.longValue
                val timeNanos = if (frameStepNanos > 0 && rotating) {
                    (baseNow / frameStepNanos) * frameStepNanos
                } else baseNow

                // Inner background circle
                val innerRadius = ((min(this.size.width, this.size.height) - 120f) * contentScale) / 2f
                drawCircle(color = bg, radius = innerRadius, center = center)

                // Draw each pre-rasterized bitmap rotated
                bitmaps.forEachIndexed { i, bmp ->
                    val periodMs = periodsMs[i]
                    val sign = if (periodMs >= 0f) 1f else -1f
                    val periodNs = kotlin.math.abs(periodMs) * 1_000_000f
                    val angle = if (periodNs > 0f) {
                        ((timeNanos % periodNs) / periodNs) * 360f * sign
                    } else 0f

                    withTransform({
                        rotate(degrees = angle, pivot = center)
                    }) {
                        drawImage(
                            image = bmp,
                            dstSize = IntSize(px, px),
                            dstOffset = IntOffset(0, 0),
                            filterQuality = FilterQuality.Low
                        )
                    }
                }
            }
    )
}

// -----------------------------------------------------
// Letters animation (simple flip cycle per letter)
// -----------------------------------------------------
@Composable
fun DSJarvisAnimationLetters(
    modifier: Modifier = Modifier,
    style: TextStyle = DSJarvisTheme.typography.title.large,
    durationPerFlip: Int = 1600,
    staggerPerLetter: Int = 120,
    cyclePauseMillis: Int = 10000,
    extraSpaceBetweenLettersDp: Dp = 4.dp,
    rightToLeft: Boolean = false,
    enabled: Boolean = true
) {
    if (!enabled) return

    val text = stringResource(R.string.core_designsystem_jarvis)
    val letters = remember(text) { text.toList() }

    val colors = listOf(
        DSJarvisTheme.colors.extra.jarvisPink,
        DSJarvisTheme.colors.extra.jarvisBlue
    )
    val brush = remember { Brush.linearGradient(colors) }

    val perLetter = durationPerFlip + cyclePauseMillis
    val total = perLetter + staggerPerLetter * (letters.size - 1)
    val cameraDistancePx = with(LocalDensity.current) { 32.dp.toPx() }

    val t by rememberInfiniteTransition(label = "letters").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "trigger"
    )

    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        letters.forEachIndexed { index, ch ->
            val order = if (rightToLeft) letters.lastIndex - index else index
            val start = order * staggerPerLetter

            DSText(
                text = ch.toString(),
                style = style.copy(
                    brush = brush,
                    fontWeight = FontWeight.Thin,
                    letterSpacing = 0.sp
                ),
                modifier = Modifier.drawBehind { t }.graphicsLayer {
                    val currentTime = System.nanoTime()
                    val tMs = (currentTime / 1_000_000L).toInt()
                    val localT = (tMs % total).toFloat()
                    val local = (localT - start).coerceIn(0f, perLetter.toFloat())

                    val angle = if (local <= durationPerFlip) 360f * (local / durationPerFlip) else 360f
                    val edge = ((angle % 180f) - 90f).absoluteValue / 90f
                    val alpha = 0.35f + 0.65f * edge

                    rotationY = angle
                    cameraDistance = cameraDistancePx
                    this.alpha = alpha
                }
            )

            if (index != letters.lastIndex && extraSpaceBetweenLettersDp > 0.dp) {
                Spacer(Modifier.size(extraSpaceBetweenLettersDp))
            }
        }
    }
}

// -----------------------------------------------------
// Main animation composable: waves + rings + text
// Adapts automatically based on frame health
// -----------------------------------------------------
@Composable
fun DSJarvisAnimation(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    enabled: Boolean = true,
    showWaveAnimation: Boolean = true,
    showRingsAnimation: Boolean = true,
    triggerLetterAnimation: Boolean = false
) {
    if (!enabled) {
        Box(modifier = modifier.size(size))
        return
    }

    // Frame health monitor
    val health by rememberFrameHealthMonitor()

    // Auto-degradation policy
    val allowWave = showWaveAnimation && health.state == FrameHealthState.Healthy
    val rotateRings = showRingsAnimation && health.state != FrameHealthState.Paused
    val ringStep = when (health.state) {
        FrameHealthState.Healthy   -> 0L
        FrameHealthState.Throttled -> 22_222_222L
        FrameHealthState.Paused    -> 33_333_333L
    }

    // Wave animation state
    var waveState by remember { mutableStateOf(WaveAnimationState()) }

    LaunchedEffect(allowWave, health.state) {
        if (!allowWave) {
            waveState = WaveAnimationState(waves = emptyList())
            return@LaunchedEffect
        }
        var lastFrame = withFrameNanos { it }
        var frameCount = 0
        // Gate updates depending on frame health
        val gate = when (health.state) {
            FrameHealthState.Healthy   -> 1
            FrameHealthState.Throttled -> 2
            FrameHealthState.Paused    -> Int.MAX_VALUE
        }

        while (isActive) {
            withFrameNanos { frameTime ->
                frameCount++
                if (frameCount % gate == 0) {
                    val deltaMs = (frameTime - lastFrame) / 500_000f
                    lastFrame = frameTime
                    waveState = waveState.nextFrame(deltaMs)
                }
            }
        }
    }

    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        // Waves
        if (allowWave) {
            Canvas(Modifier.fillMaxSize()) {
                val center = Offset(this.size.width / 2f, this.size.height / 2f)
                val baseRadius = this.size.minDimension / 8f
                val maxRadius = this.size.minDimension / 2.5f

                waveState.waves.forEach { wave ->
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

        // Background disc
        Box(
            modifier = Modifier
                .size(size * 0.5f)
                .background(DSJarvisTheme.colors.extra.surface, CircleShape)
        )

        // Rings (freeze or throttle if necessary)
        DSJarvisRingsRealtime(
            size = size * 0.8f,
            frameStepNanos = if (rotateRings) ringStep else 0L,
            rotating = rotateRings
        )

        // Center text
        DSText(
            text = stringResource(R.string.core_designsystem_jarvis),
            style = DSJarvisTheme.typography.label.small.copy(
                brush = Brush.linearGradient(
                    listOf(
                        DSJarvisTheme.colors.extra.jarvisPink,
                        DSJarvisTheme.colors.extra.jarvisBlue
                    )
                ),
                fontWeight = FontWeight.Thin
            )
        )
    }
}

// -----------------------------------------------------
// Previews
// -----------------------------------------------------
@Preview(showBackground = true, name = "Animated (run app to see)")
@Composable
fun DSJarvisAnimationAnimatedPreview() {
    DSJarvisTheme {
        DSJarvisAnimation(
            enabled = true,
            showWaveAnimation = true,
            showRingsAnimation = true,
            triggerLetterAnimation = false
        )
    }
}

@Preview(showBackground = true, name = "Rings Frozen")
@Composable
fun DSJarvisAnimationRingsFrozenPreview() {
    DSJarvisTheme {
        DSJarvisAnimation(
            enabled = true,
            showWaveAnimation = false,
            showRingsAnimation = false, // visible but frozen
            triggerLetterAnimation = false
        )
    }
}

@Preview(showBackground = true, name = "Letter Animation")
@Composable
fun DSJarvisAnimationsLetterPreview() {
    DSJarvisTheme {
        DSJarvisAnimation(
            enabled = true,
            showWaveAnimation = true,
            showRingsAnimation = false,
            triggerLetterAnimation = true
        )
    }
}