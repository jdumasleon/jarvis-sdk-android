package com.jarvis.api.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.jarvis.api.ui.components.MiniFabType.CLOSE
import com.jarvis.api.ui.components.MiniFabType.HOME
import com.jarvis.api.ui.components.MiniFabType.INSPECTOR
import com.jarvis.api.ui.components.MiniFabType.PREFERENCES
import com.jarvis.core.designsystem.R
import com.jarvis.core.designsystem.component.DSIcon
import com.jarvis.core.designsystem.component.DSText
import com.jarvis.core.designsystem.icons.DSIcons
import com.jarvis.core.designsystem.theme.DSJarvisTheme
import kotlinx.coroutines.launch

/**
 * Draggable floating Jarvis button with expandable tool buttons
 */
@Composable
fun JarvisFabButton(
    modifier: Modifier = Modifier,
    onInspectorClick: () -> Unit,
    onPreferencesClick: () -> Unit,
    onHomeClick: () -> Unit,
    onCloseClick: () -> Unit = {},
) {
    var fabOffset by remember { mutableStateOf(IntOffset.Zero) }
    var isExpanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    var pulseId by remember { mutableIntStateOf(0) }

    val density = LocalDensity.current
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp
    val fabSize = DSJarvisTheme.dimensions.xxxxxxl

    val halfScreenWidthPx = with(density) { (screenWidthDp / 2).roundToPx() }
    val halfScreenHeightPx = with(density) { (screenHeightDp / 2).roundToPx() }
    val halfFabPx = with(density) { (fabSize / 2).roundToPx() }
    val minX = -halfScreenWidthPx + halfFabPx
    val maxX = +halfScreenWidthPx - halfFabPx
    val minY = -halfScreenHeightPx + halfFabPx
    val maxY = +halfScreenHeightPx - halfFabPx

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
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
            MiniFabType.entries.forEach { miniFabType ->
                val (miniXpx, miniYpx) = miniFabType.getPositionPx(isExpanded, density)

                if (miniFabSize.value > 0.dp) {
                    FloatingActionButton(
                        modifier = Modifier
                            .zIndex(if (isExpanded) 1f else 0f)
                            .size(miniFabSize.value)
                            .offset {
                                IntOffset(
                                    x = fabOffset.x + miniXpx.value,
                                    y = fabOffset.y + miniYpx.value
                                )
                            },
                        containerColor = DSJarvisTheme.colors.extra.surface,
                        shape = CircleShape,
                        onClick = {
                            when (miniFabType) {
                                HOME -> onHomeClick()
                                INSPECTOR -> onInspectorClick()
                                PREFERENCES -> onPreferencesClick()
                                CLOSE -> onCloseClick()
                            }
                        }
                    ) {
                        DrawMiniFabs(miniFabType)
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .offset { fabOffset }
                .size(fabSize)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            if (change.positionChange() != Offset.Zero) change.consume()
                            val newX = (fabOffset.x + dragAmount.x).toInt().coerceIn(minX, maxX)
                            val newY = (fabOffset.y + dragAmount.y).toInt().coerceIn(minY, maxY)
                            fabOffset = IntOffset(newX, newY)
                        }
                    )
                }
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        isExpanded = !isExpanded
                        pulseId++
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            JarvisFabIcon(
                pulseId = pulseId,
                expanded = isExpanded,
                fabSize = fabSize
            )
        }
    }
}

@Composable
private fun JarvisFabIcon(
    pulseId: Int = 0,
    expanded: Boolean = false,
    fabSize: Dp = DSJarvisTheme.dimensions.xxxxl
) {
    val density = LocalDensity.current
    val cameraDistancePx = with(density) { 32.dp.toPx() }

    val scale = remember { Animatable(1f) }
    val imageRotationZ = remember { Animatable(0f) }
    val glowAlpha = remember { Animatable(0f) }
    val ringRadius = remember { Animatable(0f) }
    val ringAlpha = remember { Animatable(0f) }

    val c1 = DSJarvisTheme.colors.extra.jarvisPink
    val c2 = DSJarvisTheme.colors.extra.jarvisBlue
    val surface = DSJarvisTheme.colors.extra.surface

    LaunchedEffect(pulseId) {
        imageRotationZ.snapTo(0f)
        scale.snapTo(1f)
        glowAlpha.snapTo(0f)
        ringAlpha.snapTo(0f)
        ringRadius.snapTo(0f)

        launch {
            imageRotationZ.animateTo(
                360f,
                tween(durationMillis = 5000, easing = FastOutSlowInEasing)
            )
            imageRotationZ.snapTo(0f)
        }
        launch {
            scale.animateTo(1.12f, tween(140, easing = LinearOutSlowInEasing))
            scale.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessLow))
        }
        launch {
            glowAlpha.animateTo(0.35f, tween(180))
            glowAlpha.animateTo(0f, tween(320))
        }
        launch {
            val maxR = with(density) { (fabSize * 0.9f).toPx() }
            ringAlpha.snapTo(0.28f)
            launch { ringRadius.animateTo(maxR, tween(520, easing = FastOutLinearInEasing)) }
            launch { ringAlpha.animateTo(0f, tween(520)) }
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .drawBehind {
                if (ringAlpha.value > 0f && ringRadius.value > 0f) {
                    drawCircle(
                        brush = Brush.radialGradient(listOf(c1.copy(alpha = ringAlpha.value), Color.Transparent)),
                        radius = ringRadius.value,
                        center = this.size.center
                    )
                }

                if (glowAlpha.value > 0f) {
                    val glowR = size.minDimension * 0.45f
                    drawCircle(
                        brush = Brush.radialGradient(listOf(c2.copy(alpha = glowAlpha.value), Color.Transparent)),
                        radius = glowR,
                        center = this.size.center
                    )
                }
            }
    ) {
        Box(
            modifier = Modifier
                .size(fabSize - DSJarvisTheme.dimensions.xl)
                .shadow(DSJarvisTheme.elevations.level2, CircleShape)
                .background(surface, CircleShape)
        )

        DSText(
            text = stringResource(R.string.core_designsystem_jarvis),
            style = DSJarvisTheme.typography.label.small.copy(
                brush = Brush.linearGradient(listOf(c1, c2)),
                fontWeight = FontWeight.Thin
            )
        )

        Image(
            modifier = Modifier
                .size(fabSize)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    // rotationZ = if(expanded) imageRotationZ.value else -imageRotationZ.value
                    cameraDistance = cameraDistancePx
                },
            painter = painterResource(R.drawable.ic_jarvis_logo_shape),
            contentDescription = "Jarvis Logo"
        )
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
            iconSize = DSJarvisTheme.dimensions.l
        )
        PREFERENCES -> MiniFabIcon(
            icon = DSIcons.settings,
            description = "API Calls Monitoring",
            iconSize = DSJarvisTheme.dimensions.l
        )
        CLOSE -> MiniFabIcon(
            icon = DSIcons.Rounded.close,
            description = "Close Jarvis SDK",
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
    HOME, INSPECTOR, PREFERENCES, CLOSE;

    /**
     * Offsets animados en **px** para alinear dibujo y hitbox.
     */
    @Composable
    fun getPositionPx(expanded: Boolean, density: Density): Pair<State<Int>, State<Int>> {

        @Composable
        fun a(target: Dp, duration: Int = 300): State<Int> =
            animateIntAsState(
                targetValue = with(density) { target.roundToPx() },
                animationSpec = tween(duration),
                label = "miniFabPx"
            )

        return when (this) {
            HOME -> Pair(a(0.dp), a(if (expanded) (-60).dp else 0.dp))
            INSPECTOR -> Pair(a(if (expanded) 45.dp else 0.dp), a(if (expanded) (-45).dp else 0.dp))
            PREFERENCES -> Pair(a(if (expanded) (-45).dp else 0.dp), a(if (expanded) (-45).dp else 0.dp))
            CLOSE -> Pair(a(if (expanded) 60.dp else 0.dp), a(0.dp, duration = 400))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun JarvisFabButtonPreview() {
    DSJarvisTheme {
        JarvisFabButton(
            onInspectorClick = {},
            onPreferencesClick = {},
            onHomeClick = {}
        )
    }
}