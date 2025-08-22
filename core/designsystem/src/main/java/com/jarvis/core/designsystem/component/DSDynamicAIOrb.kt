@file:Suppress("FunctionName")
package com.jarvis.core.designsystem.component

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jarvis.core.designsystem.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.painter.Painter
import kotlin.math.PI

/**
 * Conversational AI Avatar (Compose only, no external libs).
 */
@Composable
fun ConversationalAgentAvatar(
    modifier: Modifier = Modifier,
    avatarSize: Dp = 180.dp,
    isListening: Boolean,
    isSpeaking: Boolean,
    energy: Float
) {
    val infinite = rememberInfiniteTransition(label = "infinite")

    // Use named args in tween(...) to avoid the Easing/Int mismatch.
    val rotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val basePulse by infinite.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "basePulse"
    )

    val listenPulse by infinite.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "listenPulse"
    )

    // Orbits (use named args)
    val orbitSpeed = 3500
    val orbit1 by infinite.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(durationMillis = orbitSpeed, easing = LinearEasing)),
        label = "orbit1"
    )
    val orbit2 by infinite.animateFloat(
        initialValue = 120f, targetValue = 480f,
        animationSpec = infiniteRepeatable(tween(durationMillis = orbitSpeed, easing = LinearEasing)),
        label = "orbit2"
    )
    val orbit3 by infinite.animateFloat(
        initialValue = 240f, targetValue = 600f,
        animationSpec = infiniteRepeatable(tween(durationMillis = orbitSpeed, easing = LinearEasing)),
        label = "orbit3"
    )

    // Speaking ripples
    data class Ripple(var progress: Float = 0f, var finished: Boolean = false)
    val ripples = remember { mutableStateListOf<Ripple>() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isSpeaking) {
        ripples.clear()
        if (isSpeaking) {
            while (isSpeaking) {
                val r = Ripple()
                ripples += r
                scope.launch {
                    val anim = Animatable(0f)
                    anim.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 1400, easing = LinearEasing)
                    ) { r.progress = value }
                    r.finished = true
                }
                delay(350L)
            }
        }
    }
    LaunchedEffect(ripples.size) { ripples.removeAll { it.finished } }

    // Colors
    val glowColor = Color(0xFF4B9BFF)
    val accent = Color(0xFF64FFD4)
    val accent2 = Color(0xFFB388FF)

    val px = with(LocalDensity.current) { avatarSize.toPx() }
    val blurPx = px * 0.07f

    val clampedEnergy = energy.coerceIn(0f, 1f)
    val energyPulse = 1f + (clampedEnergy * 0.12f)
    val scale =
        if (isListening) basePulse * listenPulse * energyPulse
        else basePulse * (0.98f + clampedEnergy * 0.04f)

    Box(
        modifier = modifier
            .size(avatarSize)
            .drawBehind {
                // Draw blurred glow using Android Paint via drawIntoCanvas
                val r = min(size.width, size.height) / 2f
                val c = this.center
                drawIntoCanvas { canvas ->
                    val p = android.graphics.Paint().apply {
                        color = glowColor.copy(alpha = 0.45f).toArgb()
                        isAntiAlias = true
                        maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
                    }
                    canvas.nativeCanvas.drawCircle(c.x, c.y, r * 0.9f, p)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(avatarSize)) {
            val w = size.width
            val h = size.height
            val radius = min(w, h) / 2f
            val center = Offset(w / 2f, h / 2f)

            // Ripples
            ripples.forEach { r ->
                val alpha = (1f - r.progress).coerceIn(0f, 1f) * 0.5f
                val strokeW = radius * 0.08f * (1f - r.progress)
                drawCircle(
                    center = center,
                    radius = radius * (0.6f + r.progress * 0.7f),
                    color = accent.copy(alpha = alpha),
                    style = Stroke(width = strokeW)
                )
            }

            // Core
            withTransform({
                rotate(rotation, pivot = center)
                scale(scaleX = scale, scaleY = scale, pivot = center)
            }) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        listOf(glowColor, accent, accent2, glowColor),
                        center = center
                    ),
                    radius = radius * 0.58f,
                    center = center,
                    alpha = 0.95f
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.18f), Color.Transparent),
                        center = center,
                        radius = radius * 0.62f
                    ),
                    radius = radius * 0.62f,
                    center = center
                )
            }

            // Thin ring
            drawCircle(
                center = center,
                radius = radius * 0.66f * scale,
                color = Color.White.copy(alpha = 0.10f),
                style = Stroke(width = radius * 0.015f)
            )

            // Orbiting orbs
            val orbitR = radius * 0.82f * scale
            val orbSize = radius * 0.08f * (1f + clampedEnergy * 0.4f)
            fun drawOrb(angleDeg: Float, c: Color) {
                val a = Math.toRadians(angleDeg.toDouble())
                val x = center.x + orbitR * cos(a).toFloat()
                val y = center.y + orbitR * sin(a).toFloat()
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(c.copy(alpha = 0.95f), c.copy(alpha = 0.3f)),
                        center = Offset(x, y),
                        radius = orbSize
                    ),
                    radius = orbSize,
                    center = Offset(x, y)
                )
            }
            drawOrb(orbit1, accent)
            drawOrb(orbit2, glowColor)
            drawOrb(orbit3, accent2)
        }
    }
}


/* ----------------------------- Demo / Preview ----------------------------- */

@Composable
fun AiAvatarDemo() {
    var speaking by remember { mutableStateOf(true) }
    var listening by remember { mutableStateOf(false) }

    // Simple fake energy while you don't hook real audio
    val energy by rememberInfiniteTransition(label = "energy")
        .animateFloat(
            initialValue = 0.2f,
            targetValue = 0.9f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "energyAnim"
        )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0B1220))
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ConversationalAgentAvatar(
            avatarSize = 220.dp,
            isListening = listening,
            isSpeaking = speaking,
            energy = energy
        )
        Spacer(Modifier.height(16.dp))
        Row {
            ToggleChip("Listen", listening) { listening = it; if (it) speaking = false }
            Spacer(Modifier.width(12.dp))
            ToggleChip("Speak", speaking) { speaking = it; if (it) listening = false }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = if (speaking) "Speaking…" else if (listening) "Listening…" else "Idle",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun ToggleChip(
    label: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    val bg = if (checked) Color(0xFF1F3A5B) else Color(0xFF111827)
    val fg = if (checked) Color(0xFFB4E1FF) else Color(0xFFE5E7EB)

    Box(
        modifier = Modifier
            .height(36.dp)
            .wrapContentWidth()
            .background(bg, CircleShape)
            .clickable { onChange(!checked) }
            .padding(horizontal = 14.dp)
            .drawWithContent { drawContent() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = fg, style = MaterialTheme.typography.bodyMedium)
        Box(
            Modifier
                .matchParentSize()
                .drawBehind {
                    drawRoundRect(
                        color = Color.White.copy(0.06f),
                        style = Stroke(width = 1.dp.toPx()),
                        cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx())
                    )
                }
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B1220)
@Composable
private fun PreviewAvatar() {
    Surface(color = Color(0xFF0B1220)) {
        Box {
            ConversationalAgentAvatar(
                avatarSize = 100.dp,
                isListening = false,
                isSpeaking = false,
                energy = 0.5f
            )
            DSIcon(
                modifier = Modifier.size(100.dp),
                painter = painterResource(R.drawable.ic_jarvis_logo_shape),
                contentDescription = "Jarvis logo",
            )
        }

    }
}