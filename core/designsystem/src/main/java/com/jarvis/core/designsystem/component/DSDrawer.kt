/*
 * DSDrawer - Material 3 Design System Component
 * Using modern AnchoredDraggable API instead of deprecated Swipeable
 */
package com.jarvis.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// Constants for animation thresholds
private const val THRESHOLD_X_POSITION = 1.4f
private const val THRESHOLD_Y_ROTATION = 25
private const val THRESHOLD_CAMERA_DISTANCE = 8
private const val FALLBACK_SCREEN_WIDTH = 1080f
private const val FALLBACK_DRAWER_WIDTH = 771f // 1080/1.4

// Safe math operations
private fun Float.safeRoundToInt(): Int = try {
    if (isFinite()) roundToInt() else 0
} catch (_: IllegalArgumentException) {
    0
}

private fun Float.safeDivision(divisor: Float): Float =
    if (this.isFinite() && divisor.isFinite() && divisor != 0f) this / divisor else 0f

@Composable
fun rememberDSDrawerState(
    initialValue: DSDrawerValue
): DSDrawerState {
    val density = LocalDensity.current
    return remember {
        DSDrawerState(
            initialValue = initialValue,
            density = density
        )
    }
}

class DSDrawerState(
    initialValue: DSDrawerValue,
    @Suppress("UNUSED_PARAMETER") private val density: Density
) {
    internal val anchoredDraggableState = AnchoredDraggableState(initialValue = initialValue)

    val currentValue: DSDrawerValue
        get() = anchoredDraggableState.currentValue

    @Suppress("unused")
    val targetValue: DSDrawerValue
        get() = anchoredDraggableState.targetValue

    val offset: Float
        get() = anchoredDraggableState.offset.takeIf { it.isFinite() } ?: 0f

    val isOpen: Boolean
        get() = currentValue == DSDrawerValue.Open

    val isClosed: Boolean
        get() = currentValue == DSDrawerValue.Closed

    suspend fun open() = anchoredDraggableState.animateTo(DSDrawerValue.Open)
    suspend fun close() = anchoredDraggableState.animateTo(DSDrawerValue.Closed)

    internal fun updateAnchors(anchors: DraggableAnchors<DSDrawerValue>) {
        anchoredDraggableState.updateAnchors(anchors)
    }
}

sealed class DSDrawerValue {
    data object Closed : DSDrawerValue()
    data object Open : DSDrawerValue()
}

/**
 * Material 3 DSDrawer component with card-like 3D animation
 *
 * @param drawerContent composable that represents content inside the drawer
 * @param modifier optional modifier for the drawer
 * @param drawerState state of the drawer
 * @param gesturesEnabled whether or not drawer can be interacted by gestures
 * @param drawerBackgroundColor background color to be used for the drawer sheet
 * @param drawerContentColor color of the content to use inside the drawer sheet
 * @param contentCornerSize size of shape of the content
 * @param contentBackgroundColor background color to be used for the content
 * @param content content of the rest of the UI
 *
 * @throws IllegalStateException when parent has [Float.POSITIVE_INFINITY] width
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DSDrawer(
    drawerContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    drawerState: DSDrawerState = rememberDSDrawerState(DSDrawerValue.Closed),
    gesturesEnabled: Boolean = true,
    drawerBackgroundColor: Color = MaterialTheme.colorScheme.surface,
    drawerContentColor: Color = MaterialTheme.colorScheme.onSurface,
    contentCornerSize: Dp = 0.dp,
    contentBackgroundColor: Color = MaterialTheme.colorScheme.background,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current

    // Calculate safe screen dimensions
    val screenWidth = calculateScreenWidth(windowInfo)
    val drawerWidth = calculateDrawerWidth(screenWidth, density)

    BoxWithConstraints(modifier.background(drawerBackgroundColor)) {
        val maxWidth = this.constraints.maxWidth.toFloat()

        if (!constraints.hasBoundedWidth) {
            throw IllegalStateException("Drawer shouldn't have infinite width")
        }

        // Setup anchors for dragging
        SetupDraggableAnchors(drawerState, maxWidth)

        val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

        Box(
            Modifier.anchoredDraggable(
                state = drawerState.anchoredDraggableState,
                orientation = Orientation.Horizontal,
                enabled = gesturesEnabled,
                reverseDirection = isRtl
            )
        ) {
            DrawerSurface(
                drawerState = drawerState,
                screenWidth = screenWidth,
                drawerWidth = drawerWidth,
                backgroundColor = drawerBackgroundColor,
                contentColor = drawerContentColor,
                scope = scope,
                content = drawerContent
            )

            ContentSurface(
                drawerState = drawerState,
                screenWidth = screenWidth,
                density = density,
                contentCornerSize = contentCornerSize,
                backgroundColor = contentBackgroundColor,
                content = content
            )
        }
    }
}

@Composable
private fun calculateScreenWidth(windowInfo: androidx.compose.ui.platform.WindowInfo): Float {
    return remember(windowInfo) {
        val width = windowInfo.containerSize.width.toFloat()
        if (width > 0f && width.isFinite()) width else FALLBACK_SCREEN_WIDTH
    }
}

@Composable
private fun calculateDrawerWidth(screenWidth: Float, density: Density): Dp {
    return remember(screenWidth) {
        with(density) {
            val widthPx = screenWidth / THRESHOLD_X_POSITION
            val safePx = if (widthPx.isFinite() && widthPx > 0f) widthPx else FALLBACK_DRAWER_WIDTH
            safePx.toDp()
        }
    }
}

@Composable
private fun SetupDraggableAnchors(drawerState: DSDrawerState, maxWidth: Float) {
    LaunchedEffect(maxWidth) {
        if (maxWidth.isFinite() && maxWidth > 0f) {
            val anchors = DraggableAnchors {
                DSDrawerValue.Closed at -maxWidth
                DSDrawerValue.Open at 0f
            }
            drawerState.updateAnchors(anchors)
        }
    }
}

@Composable
private fun DrawerSurface(
    drawerState: DSDrawerState,
    screenWidth: Float,
    drawerWidth: Dp,
    backgroundColor: Color,
    contentColor: Color,
    scope: kotlinx.coroutines.CoroutineScope,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .offset { calculateDrawerOffset(drawerState.offset, screenWidth) }
            .width(drawerWidth)
            .widthIn(0.dp, drawerWidth)
            .semantics {
                paneTitle = "FullDrawerLayout"
                if (drawerState.isOpen) {
                    dismiss {
                        scope.launch { drawerState.close() }
                        true
                    }
                }
            },
        color = backgroundColor,
        contentColor = contentColor
    ) {
        Column(content = content)
    }
}

@Composable
private fun ContentSurface(
    drawerState: DSDrawerState,
    screenWidth: Float,
    density: Density,
    contentCornerSize: Dp,
    backgroundColor: Color,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .offset { calculateContentOffset(drawerState.offset, screenWidth) }
            .graphicsLayer {
                val transform = calculate3DTransform(drawerState.offset, screenWidth, density)
                rotationY = transform.rotationY
                cameraDistance = transform.cameraDistance
            },
        color = backgroundColor,
        shape = RoundedCornerShape(
            calculateCornerRadius(drawerState.offset, screenWidth, contentCornerSize)
        )
    ) {
        content()
    }
}

private fun calculateDrawerOffset(offset: Float, screenWidth: Float): IntOffset {
    val safeOffset = offset.takeIf { it.isFinite() } ?: 0f
    val maxX = (screenWidth / THRESHOLD_X_POSITION).safeRoundToInt()
    val x = safeOffset.safeRoundToInt()
    val clampedX = if (x <= maxX) x else maxX
    return IntOffset(clampedX, 0)
}

private fun calculateContentOffset(offset: Float, screenWidth: Float): IntOffset {
    val safeOffset = offset.takeIf { it.isFinite() } ?: 0f
    val safeScreenWidth = screenWidth.takeIf { it.isFinite() && it > 0f } ?: FALLBACK_SCREEN_WIDTH

    val offsetInt = safeOffset.safeRoundToInt()
    val screenWidthInt = safeScreenWidth.safeRoundToInt()
    val x = offsetInt + screenWidthInt

    val maxX = (safeScreenWidth / THRESHOLD_X_POSITION).safeRoundToInt()
    val clampedX = if (x <= maxX) x else maxX
    return IntOffset(clampedX, 0)
}

private data class Transform3D(
    val rotationY: Float,
    val cameraDistance: Float
)

private fun calculate3DTransform(offset: Float, screenWidth: Float, density: Density): Transform3D {
    val safeOffset = offset.takeIf { it.isFinite() } ?: 0f
    val safeScreenWidth = screenWidth.takeIf { it.isFinite() && it > 0f } ?: FALLBACK_SCREEN_WIDTH
    val rotationRatio = safeOffset.safeDivision(safeScreenWidth)

    return Transform3D(
        rotationY = -((THRESHOLD_Y_ROTATION * rotationRatio) + THRESHOLD_Y_ROTATION),
        cameraDistance = THRESHOLD_CAMERA_DISTANCE * density.density
    )
}

private fun calculateCornerRadius(offset: Float, screenWidth: Float, contentCornerSize: Dp): Dp {
    val safeOffset = offset.takeIf { it.isFinite() } ?: 0f
    val safeScreenWidth = screenWidth.takeIf { it.isFinite() && it > 0f } ?: FALLBACK_SCREEN_WIDTH
    val cornerRatio = safeOffset.safeDivision(safeScreenWidth)
    val cornerSize = ((contentCornerSize.value * cornerRatio) + contentCornerSize.value)

    return if (cornerSize.isFinite() && cornerSize >= 0f) {
        cornerSize.safeRoundToInt().dp
    } else {
        contentCornerSize
    }
}

@Composable
@Preview(showBackground = true)
fun DSDrawerPreview() {
    val drawerState = rememberDSDrawerState(DSDrawerValue.Closed)
    val scope = rememberCoroutineScope()

    DSDrawer(
        drawerState = drawerState,
        contentCornerSize = 16.dp,
        drawerBackgroundColor = Color.DarkGray,
        drawerContentColor = Color.White,
        contentBackgroundColor = Color(0xFFEDEDED),
        drawerContent = {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text("Drawer Title", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Text("Item 1")
                Text("Item 2")
                Text("Item 3")
            }
        },
        content = {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(24.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            if (drawerState.isClosed) drawerState.open()
                            else drawerState.close()
                        }
                    }
                ) {
                    Text("Toggle Drawer")
                }
            }
        }
    )
}