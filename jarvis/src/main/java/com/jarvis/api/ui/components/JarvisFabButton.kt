package com.jarvis.api.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.jarvis.api.ui.components.MiniFabType.HOME
import com.jarvis.api.ui.components.MiniFabType.INSPECTOR
import com.jarvis.api.ui.components.MiniFabType.PREFERENCES
import com.jarvis.core.designsystem.component.DSIcon
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
    modifier: Modifier = Modifier
) {
    var fabOffset by remember { mutableStateOf(IntOffset.Zero) }
    var isExpanded by remember { mutableStateOf(false) }

    val localDensity = LocalDensity.current
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val fabSize = DSJarvisTheme.dimensions.xxxxl

    val miniFabSize: State<Dp> = animateDpAsState(
        targetValue = if (isExpanded) DSJarvisTheme.dimensions.xxl else 0.dp,
        label = "Change Size MiniFab",
        animationSpec = tween(300)
    )
    val rotation: State<Float> = animateFloatAsState(
        targetValue = if (isExpanded) 315f else 0f,
        label = "Fab rotation"
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
                containerColor = DSJarvisTheme.colors.primary.primary100,
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

        FloatingActionButton(
            modifier = Modifier
                .graphicsLayer(
                    translationX = with(localDensity) {
                        fabOffset.x
                            .toDp()
                            .toPx()
                    },
                    translationY = with(localDensity) {
                        fabOffset.y
                            .toDp()
                            .toPx()
                    }
                )
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            if (change.positionChange() != Offset.Zero) {
                                change.consume()
                            }
                            fabOffset = IntOffset(
                                x = localDensity.run {
                                    (fabOffset.x + dragAmount.x)
                                        .toInt()
                                        .coerceIn(
                                            minimumValue = (-screenWidth / 2 + fabSize / 2).toPx().toInt(),
                                            maximumValue = (screenWidth / 2 - fabSize / 2).toPx().toInt()
                                        )
                                },
                                y = localDensity.run {
                                    (fabOffset.y + dragAmount.y)
                                        .toInt()
                                        .coerceIn(
                                            minimumValue = (-screenHeight / 2 + fabSize / 2).toPx().toInt(),
                                            maximumValue = (screenHeight / 2 - fabSize / 2).toPx().toInt()
                                        )
                                }
                            )
                        }
                    )
                },
            containerColor = DSJarvisTheme.colors.primary.primary100,
            shape = CircleShape,
            onClick = { isExpanded = !isExpanded }
        ) {
            FabIcon(
                icon = if (isExpanded) DSIcons.add else DSIcons.adb,
                rotation = rotation.value
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
private fun FabIcon(icon: ImageVector, rotation: Float) {
    DSIcon(
        imageVector = icon,
        contentDescription = "Jarvis Mode Icon",
        tint = Color.White,
        modifier = Modifier.rotate(rotation)
    )
}

@Composable
private fun MiniFabIcon(icon: ImageVector, description: String, iconSize: Dp) {
    DSIcon(
        imageVector = icon,
        contentDescription = description,
        tint = Color.White,
        modifier = Modifier.size(iconSize)
    )
}

enum class MiniFabType {
    HOME, INSPECTOR, PREFERENCES;

    @Composable
    fun getPosition(expanded: Boolean): Pair<State<Dp>, State<Dp>> {
        return when (this) {
            HOME -> Pair(
                animateDpAsState(0.dp, tween(300), label = ""),
                animateDpAsState(if (expanded) (-56).dp else 0.dp, tween(300), label = "")
            )

            INSPECTOR -> Pair(
                animateDpAsState(if (expanded) 40.dp else 0.dp, tween(300), label = ""),
                animateDpAsState(if (expanded) (-40).dp else 0.dp, tween(300), label = "")
            )

            PREFERENCES -> Pair(
                animateDpAsState(if (expanded) 56.dp else 0.dp, tween(300), label = ""),
                animateDpAsState(0.dp, tween(400), label = "")
            )
        }
    }
}