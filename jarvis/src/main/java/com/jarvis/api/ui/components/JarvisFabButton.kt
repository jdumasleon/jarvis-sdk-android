package com.jarvis.api.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.jarvis.api.ui.components.MiniFabType.HOME
import com.jarvis.api.ui.components.MiniFabType.INSPECTOR
import com.jarvis.api.ui.components.MiniFabType.PREFERENCES
import com.jarvis.core.designsystem.component.DSIcon
import com.jarvis.core.designsystem.component.DSJarvisAnimation
import com.jarvis.core.designsystem.icons.DSIcons
import com.jarvis.core.designsystem.theme.DSJarvisTheme

/**
 * Draggable floating Jarvis button with expandable tool buttons
 */
@Composable
fun JarvisFabButton(
    onInspectorClick: () -> Unit,
    onPreferencesClick: () -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDrawerOpen: Boolean = false
) {
    var fabOffset by remember { mutableStateOf(IntOffset.Zero) }
    var isExpanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    val localDensity = LocalDensity.current
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val fabSize = DSJarvisTheme.dimensions.xxxxl

    val miniFabSize: State<Dp> = animateDpAsState(
        targetValue = if (isExpanded) DSJarvisTheme.dimensions.xxl else 0.dp,
        label = "Change Size MiniFab",
        animationSpec = tween(300)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        MiniFabType.entries.forEach { miniFabType ->
            val miniFabPosition = miniFabType.getPosition(isExpanded)
            FloatingActionButton(
                modifier = Modifier
                    .size(miniFabSize.value)
                    .offset(
                        x = with(localDensity) { fabOffset.x.toDp() + miniFabPosition.first.value },
                        y = with(localDensity) { fabOffset.y.toDp() + miniFabPosition.second.value }
                    ),
                containerColor = DSJarvisTheme.colors.extra.surface,
                shape = CircleShape,
                onClick = {
                    when (miniFabType) {
                        HOME -> onHomeClick()
                        INSPECTOR -> onInspectorClick()
                        PREFERENCES -> onPreferencesClick()
                    }
                }
            ) {
                DrawMiniFabs(miniFabType)
            }
        }

        Box(
            modifier = Modifier
                .offset { fabOffset }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            // consume only if there is movement (drag), not on simple tap
                            if (change.positionChange() != Offset.Zero) change.consume()

                            fabOffset = IntOffset(
                                x = (fabOffset.x + dragAmount.x)
                                    .toInt()
                                    .coerceIn(
                                        (-screenWidth / 2 + fabSize / 2).toPx().toInt(),
                                        (screenWidth / 2 - fabSize / 2).toPx().toInt()
                                    ),
                                y = (fabOffset.y + dragAmount.y)
                                    .toInt()
                                    .coerceIn(
                                        (-screenHeight / 2 + fabSize / 2).toPx().toInt(),
                                        (screenHeight / 2 - fabSize / 2).toPx().toInt()
                                    )
                            )
                        }
                    )
                }
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { isExpanded = !isExpanded }
                ),
            contentAlignment = Alignment.Center
        ) {
            DSJarvisAnimation(
                showWaveAnimation = !isExpanded && !isDrawerOpen,
                showRingsAnimation = !isExpanded && !isDrawerOpen
            )
        }
    }
}

@Composable
private fun DrawMiniFabs(miniFabType: MiniFabType) {
    when (miniFabType) {
        HOME -> MiniFabIcon(
            icon = DSIcons.home,
            description = "Change Environment Mode",
            iconSize = DSJarvisTheme.dimensions.l
        )
        INSPECTOR -> MiniFabIcon(
            icon = DSIcons.networkCheck,
            description = "Wifi KO Mode",
            DSJarvisTheme.dimensions.l
        )
        PREFERENCES -> MiniFabIcon(
            icon = DSIcons.settings,
            description = "API Calls Monitoring",
            iconSize = DSJarvisTheme.dimensions.l
        )
    }
}

@Composable
private fun MiniFabIcon(icon: ImageVector, description: String, iconSize: Dp) {
    val colors = listOf(
        DSJarvisTheme.colors.extra.jarvisPink,
        DSJarvisTheme.colors.extra.jarvisBlue
    )
    val brush = remember { Brush.linearGradient(colors) }

    DSIcon(
        imageVector = icon,
        contentDescription = description,
        modifier = Modifier
            .graphicsLayer(alpha = 0.99f)
            .size(iconSize)
            .drawWithCache {
                onDrawWithContent {
                    drawContent()
                    drawRect(
                        brush = brush,
                        blendMode = BlendMode.SrcIn
                    )
                }
            }
    )
}

enum class MiniFabType {
    HOME, INSPECTOR, PREFERENCES;

    @Composable
    fun getPosition(expanded: Boolean): Pair<State<Dp>, State<Dp>> {
        return when (this) {
            HOME -> Pair(
                animateDpAsState(0.dp, tween(300), label = ""),
                animateDpAsState(if (expanded) (-60).dp else 0.dp, tween(300), label = "")
            )

            INSPECTOR -> Pair(
                animateDpAsState(if (expanded) 45.dp else 0.dp, tween(300), label = ""),
                animateDpAsState(if (expanded) (-45).dp else 0.dp, tween(300), label = "")
            )

            PREFERENCES -> Pair(
                animateDpAsState(if (expanded) 60.dp else 0.dp, tween(300), label = ""),
                animateDpAsState(0.dp, tween(400), label = "")
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun JarvisFabButtonPreview() {
    DSJarvisTheme {
        // Preview without running heavy animations
        JarvisFabButton(
            onInspectorClick = {},
            onPreferencesClick = {},
            onHomeClick = {}
        )
    }
}