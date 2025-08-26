@file:Suppress("FunctionName")

package com.jarvis.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarvis.core.designsystem.R
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.core.designsystem.theme.JarvisPink
import kotlinx.coroutines.isActive
import kotlin.math.absoluteValue
import kotlin.math.min

// -----------------------------------------------------
// Vector → Bitmap cache (done once per size/density)
// -----------------------------------------------------
@Composable
private fun rememberRingBitmaps(
    layerIds: List<Int>,
    targetPx: Int,                  // square canvas
    contentScale: Float,            // 0..1 -> inner padding
    viewportW: Float,
    viewportH: Float,
    pivotX: Float,
    pivotY: Float
): List<ImageBitmap> {
    val density = LocalDensity.current
    // Load vectors/painters in composable scope (ok)
    val vectors = layerIds.map { id -> ImageVector.vectorResource(id) }
    val painters = vectors.map { v -> rememberVectorPainter(v) }

    // Rebuild if key parameters change
    return remember(targetPx, layerIds, contentScale, viewportW, viewportH, pivotX, pivotY) {
        val inner = targetPx * contentScale
        val s = min(inner / viewportW, inner / viewportH)

        val drawW = viewportW * s
        val drawH = viewportH * s
        val center = targetPx / 2f

        // translate so that (pivotX,pivotY) of the vector maps to the canvas center
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
// - rotating=true: gira en tiempo real
// - rotating=false: muestra los anillos CONGELADOS en el último ángulo
// -----------------------------------------------------
@Composable
fun DSJarvisRingsRealtime(
    size: Dp = 100.dp,
    // period per revolution in ms; sign = direction
    periodsMs: List<Float> = listOf(3000f, -4000f, 5000f, -5000f, 6000f),
    // your split vector layers (one ring per id)
    layerIds: List<Int> = listOf(
        R.drawable.ic_ring0,
        R.drawable.ic_ring1,
        R.drawable.ic_ring2,
        R.drawable.ic_ring3,
        R.drawable.ic_ring4
    ),
    // vector viewport & pivot (they are the same across layers)
    viewport: Size = Size(554.67f, 446.30f),
    pivotInViewport: Offset = Offset(450f, 400f),
    contentScale: Float = 0.98f,
    // 0 = vsync; try 22_222_222 for ~45fps if you want to reduce load
    frameStepNanos: Long = 0L,
    rotating: Boolean = true
) {
    val density = LocalDensity.current
    val px = with(density) { size.toPx() }.toInt().coerceAtLeast(1)
    val bg = DSJarvisTheme.colors.extra.surface

    // Pre-rasterize all rings once at the requested size
    val bitmaps = rememberRingBitmaps(
        layerIds = layerIds,
        targetPx = px,
        contentScale = contentScale,
        viewportW = viewport.width,
        viewportH = viewport.height,
        pivotX = pivotInViewport.x,
        pivotY = pivotInViewport.y
    )

    val center = Offset(px / 2f, px / 2f)

    // Trigger para invalidar cuando está girando.
    // Cuando rotating=false, exponemos un State<Float> constante (0f), sin animación.
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

    // Cuando se desactiva la animación, "capturamos" el tiempo actual para congelar el ángulo.
    val frozenTimeNs = remember { mutableLongStateOf(0L) }

    LaunchedEffect(rotating, periodsMs) {
        if (!rotating) {
            frozenTimeNs.longValue = System.nanoTime()
        }
    }

    Box(
        Modifier
            .size(size)
            .drawBehind {
                // Forzamos invalidación cuando gira
                animationTrigger

                val baseNow = if (rotating) System.nanoTime() else frozenTimeNs.longValue
                val timeNanos = if (frameStepNanos > 0 && rotating) {
                    (baseNow / frameStepNanos) * frameStepNanos
                } else baseNow

                // background disc matching the inner padding
                val innerRadius = ((min(this.size.width, this.size.height) - 120f) * contentScale) / 2f
                drawCircle(color = bg, radius = innerRadius, center = center)

                // draw each bitmap rotated around the center
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
// LETTERS (kept simple; enable only in HIGH if needed)
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

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
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
                modifier = Modifier.drawBehind {
                    t
                }.graphicsLayer {
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
// Material3-Inspired Assistant Animation
// -----------------------------------------------------
@Composable
fun DSJarvisAnimation(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    enabled: Boolean = true,
    showWaveAnimation: Boolean = true,
    showRingsAnimation: Boolean = true, // when false, rings are visible but frozen
    triggerLetterAnimation: Boolean = false
) {
    if (!enabled) {
        Box(modifier = modifier.size(size))
        return
    }

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
                    val deltaMs = (frameTime - lastFrame) / 500_000f
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
        if (showWaveAnimation) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val center = Offset(this.size.width / 2f, this.size.height / 2f)
                val baseRadius = this.size.minDimension / 8f
                val maxRadius = this.size.minDimension / 2.5f

                // Draw waves based on current state
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

        // Always draw rings; when showRingsAnimation=false they are frozen.
        DSJarvisRingsRealtime(
            size = size * 0.8f,
            frameStepNanos = 0L,
            rotating = showRingsAnimation
        )

        // Simple static text for now (letters animation optional)
        DSText(
            text = stringResource(R.string.core_designsystem_jarvis),
            style = DSJarvisTheme.typography.label.small.copy(
                brush = Brush.linearGradient(
                    colors = listOf(
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