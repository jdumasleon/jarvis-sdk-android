package com.jarvis.core.designsystem.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import com.jarvis.core.designsystem.theme.Neutral40
import com.jarvis.core.designsystem.theme.Primary100
import kotlin.math.max
import kotlin.math.min

@Composable
fun DSCard(
    modifier: Modifier = Modifier,
    shape: CornerBasedShape = DSJarvisTheme.shapes.none,
    color: Color = DSJarvisTheme.colors.extra.white,
    elevation: Dp = DSJarvisTheme.elevations.none,
    border: BorderStroke? = null,
    isDisabled: Boolean = false,

    // ✨ Parallax options
    parallaxEnabled: Boolean = false,
    parallaxMaxTiltDeg: Float = 8f,          // rotation limit
    parallaxMaxShift: Dp = DSJarvisTheme.spacing.m, // px shift limit
    parallaxReturnStiffness: Float = Spring.StiffnessLow, // ease back speed

    content: @Composable ColumnScope.() -> Unit
) {
    val density = LocalDensity.current
    val maxShiftPx = with(density) { parallaxMaxShift.toPx() }

    var size by remember { mutableStateOf(Size.Zero) }
    var targetRotX by remember { mutableFloatStateOf(0f) }
    var targetRotY by remember { mutableFloatStateOf(0f) }
    var targetShiftX by remember { mutableFloatStateOf(0f) }
    var targetShiftY by remember { mutableFloatStateOf(0f) }

    val rotX by animateFloatAsState(
        targetValue = targetRotX,
        animationSpec = spring(stiffness = parallaxReturnStiffness),
        label = "card_rotX"
    )
    val rotY by animateFloatAsState(
        targetValue = targetRotY,
        animationSpec = spring(stiffness = parallaxReturnStiffness),
        label = "card_rotY"
    )
    val shiftX by animateFloatAsState(
        targetValue = targetShiftX,
        animationSpec = spring(stiffness = parallaxReturnStiffness),
        label = "card_shiftX"
    )
    val shiftY by animateFloatAsState(
        targetValue = targetShiftY,
        animationSpec = spring(stiffness = parallaxReturnStiffness),
        label = "card_shiftY"
    )

    fun updateTargets(pointer: Offset?) {
        if (!parallaxEnabled || size.width == 0f || size.height == 0f || pointer == null) {
            targetRotX = 0f; targetRotY = 0f; targetShiftX = 0f; targetShiftY = 0f
            return
        }
        val cx = size.width / 2f
        val cy = size.height / 2f
        val nx = ((pointer.x - cx) / max(cx, 1f)).coerceIn(-1f, 1f)
        val ny = ((pointer.y - cy) / max(cy, 1f)).coerceIn(-1f, 1f)

        targetRotY = parallaxMaxTiltDeg * nx
        targetRotX = -parallaxMaxTiltDeg * ny
        targetShiftX = max(-maxShiftPx, min(maxShiftPx, nx * maxShiftPx))
        targetShiftY = max(-maxShiftPx, min(maxShiftPx, ny * maxShiftPx))
    }

    val parallaxModifier = if (parallaxEnabled) {
        Modifier
            .onSizeChanged { size = Size(it.width.toFloat(), it.height.toFloat()) }
            .pointerInput(Unit) {
                awaitEachGesture {
                    // Detect both mouse move and touch drag
                    val down = awaitFirstDown(requireUnconsumed = false)
                    updateTargets(down.position)

                    while (true) {
                        val event = awaitPointerEvent(pass = PointerEventPass.Main)
                        val pointer = event.changes.firstOrNull()?.position
                        updateTargets(pointer)

                        if (event.changes.all { it.changedToUp() || it.isConsumed || !it.pressed }) break
                    }
                    updateTargets(null)
                }
            }
            .graphicsLayer {
                rotationX = rotX
                rotationY = rotY
                translationX = shiftX
                translationY = shiftY
                cameraDistance = 16 * density.density
            }
    } else {
        Modifier
    }

    Surface(
        modifier = modifier.then(parallaxModifier),
        shape = shape,
        color = if (isDisabled) Neutral40 else color,
        contentColor = if (isDisabled) Neutral40 else color,
        tonalElevation = elevation,
        shadowElevation = elevation,
        border = border,
    ) {
        Column(
            modifier = modifier.padding(DSJarvisTheme.spacing.s),
            content = content
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DSCardPreview() {
    DSJarvisTheme {
        Column(
            modifier = Modifier.padding(DSJarvisTheme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(DSJarvisTheme.spacing.m)
        ) {
            DSCard(color = Primary100, parallaxEnabled = true) {
                DSText(text = "DSCard Preview")
            }
            DSCard(
                shape = DSJarvisTheme.shapes.s,
                elevation = DSJarvisTheme.elevations.level2,
            ) {
                DSText(text = "Small shape, level 2 elevation")
            }
        }
    }
}