@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.core.internal.designsystem.component

import androidx.annotation.RestrictTo

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
import androidx.compose.ui.unit.*
import androidx.compose.ui.unit.lerp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.layout.onSizeChanged

// Constants for animation thresholds
private const val THRESHOLD_X_POSITION = 1.4f
private const val THRESHOLD_Y_ROTATION = 25
private const val THRESHOLD_CAMERA_DISTANCE = 8
private const val FALLBACK_SCREEN_WIDTH = 1080f
private const val FALLBACK_DRAWER_WIDTH = 771f // 1080/1.4

// Performance optimization: Cache expensive calculations
@Stable
data class DrawerAnimationConfig(
    val screenWidth: Float,
    val drawerWidth: Float,
    val cameraDistance: Float
)

@Stable
data class DrawerOffsets(
    val drawerOffset: IntOffset,
    val contentOffset: IntOffset,
    val rotationY: Float,
    val cornerRadius: Dp
)

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

// Optimized calculation function that caches results
@Composable
private fun rememberDrawerAnimationConfig(): DrawerAnimationConfig {
    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current
    
    return remember(windowInfo.containerSize, density.density) {
        val screenWidth = windowInfo.containerSize.width.toFloat().let { width ->
            if (width > 0f && width.isFinite()) width else FALLBACK_SCREEN_WIDTH
        }
        val drawerWidth = screenWidth / THRESHOLD_X_POSITION
        val cameraDistance = THRESHOLD_CAMERA_DISTANCE * density.density
        
        DrawerAnimationConfig(
            screenWidth = screenWidth,
            drawerWidth = drawerWidth,
            cameraDistance = cameraDistance
        )
    }
}

// Memoized offset calculations
@Composable
private fun calculateDrawerOffsets(
    offset: Float,
    animationConfig: DrawerAnimationConfig,
    contentCornerSize: Dp
): DrawerOffsets {
    return remember(offset, animationConfig, contentCornerSize.value) {
        val safeOffset = offset.takeIf { it.isFinite() } ?: 0f
        val screenWidth = animationConfig.screenWidth
        
        // Calculate all offsets at once to minimize work
        val drawerOffset = calculateDrawerOffset(safeOffset, screenWidth)
        val contentOffset = calculateContentOffset(safeOffset, screenWidth)
        val rotationY = calculateRotationY(safeOffset, screenWidth)
        val cornerRadius = calculateCornerRadius(safeOffset, screenWidth, contentCornerSize)
        
        DrawerOffsets(
            drawerOffset = drawerOffset,
            contentOffset = contentOffset,
            rotationY = rotationY,
            cornerRadius = cornerRadius
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
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    
    // Cache expensive calculations
    val animationConfig = rememberDrawerAnimationConfig()
    
    // Track container size for anchors setup - only recalculate when size changes
    var containerWidth by rememberSaveable { mutableFloatStateOf(0f) }
    
    // Only calculate offsets when offset actually changes
    val currentOffset by remember { derivedStateOf { drawerState.offset } }
    val drawerOffsets = calculateDrawerOffsets(
        offset = currentOffset,
        animationConfig = animationConfig,
        contentCornerSize = contentCornerSize
    )

    Box(
        modifier = modifier
            .background(drawerBackgroundColor)
            .onSizeChanged { size ->
                val newWidth = size.width.toFloat()
                if (newWidth != containerWidth && newWidth > 0f && newWidth.isFinite()) {
                    containerWidth = newWidth
                    // Update anchors only when size changes
                    val anchors = DraggableAnchors {
                        DSDrawerValue.Closed at -newWidth
                        DSDrawerValue.Open at 0f
                    }
                    drawerState.updateAnchors(anchors)
                }
            }
            .anchoredDraggable(
                state = drawerState.anchoredDraggableState,
                orientation = Orientation.Horizontal,
                enabled = gesturesEnabled,
                reverseDirection = isRtl
            )
    ) {
        // Drawer surface - memoized to prevent unnecessary recompositions
        key(drawerBackgroundColor, drawerContentColor) {
            DrawerSurface(
                drawerOffset = drawerOffsets.drawerOffset,
                drawerWidth = with(LocalDensity.current) { animationConfig.drawerWidth.toDp() },
                backgroundColor = drawerBackgroundColor,
                contentColor = drawerContentColor,
                isOpen = drawerState.isOpen,
                onDismiss = { scope.launch { drawerState.close() } },
                content = drawerContent
            )
        }

        // Content surface - memoized to prevent unnecessary recompositions
        key(contentBackgroundColor, animationConfig.cameraDistance) {
            ContentSurface(
                contentOffset = drawerOffsets.contentOffset,
                cornerSize = drawerOffsets.cornerRadius,
                rotationY = drawerOffsets.rotationY,
                cameraDistancePx = animationConfig.cameraDistance,
                backgroundColor = contentBackgroundColor,
                content = content
            )
        }
    }
}

// Removed - replaced by rememberDrawerAnimationConfig() for better performance

@Composable
private fun DrawerSurface(
    drawerOffset: IntOffset,
    drawerWidth: Dp,
    backgroundColor: Color,
    contentColor: Color,
    isOpen: Boolean,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    // Memoize the dismiss callback to prevent recreating it on every recomposition
    val stableDismiss = remember { onDismiss }
    
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .offset { drawerOffset }
            .width(drawerWidth)
            .semantics {
                paneTitle = "FullDrawerLayout"
                if (isOpen) {
                    dismiss {
                        stableDismiss()
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
    contentOffset: IntOffset,
    cornerSize: Dp,
    rotationY: Float,
    cameraDistancePx: Float,
    backgroundColor: Color,
    content: @Composable () -> Unit
) {
    // Memoize the shape to prevent recreating it on every recomposition
    val shape = remember(cornerSize) { RoundedCornerShape(cornerSize) }
    
    // Skip graphics layer when not needed for better performance
    val shouldApplyGraphicsLayer = rotationY != 0f || cameraDistancePx != 0f
    
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .offset { contentOffset }
            .let { modifier ->
                if (shouldApplyGraphicsLayer) {
                    modifier.graphicsLayer {
                        this.rotationY = rotationY
                        this.cameraDistance = cameraDistancePx
                    }
                } else {
                    modifier
                }
            },
        color = backgroundColor,
        shape = shape
    ) {
        content()
    }
}

// Optimized calculation functions - reduced allocations and improved performance
private fun calculateDrawerOffset(offset: Float, screenWidth: Float): IntOffset {
    if (offset == 0f) return IntOffset.Zero // Fast path for closed state
    
    val safeOffset = offset.takeIf { it.isFinite() } ?: 0f
    val maxX = (screenWidth / THRESHOLD_X_POSITION).safeRoundToInt()
    val x = safeOffset.safeRoundToInt().coerceAtMost(maxX)
    return IntOffset(x, 0)
}

private fun calculateContentOffset(offset: Float, screenWidth: Float): IntOffset {
    val safeOffset = offset.takeIf { it.isFinite() } ?: 0f
    val safeScreenWidth = screenWidth.takeIf { it.isFinite() && it > 0f } ?: FALLBACK_SCREEN_WIDTH

    val x = (safeOffset + safeScreenWidth).safeRoundToInt()
    val maxX = (safeScreenWidth / THRESHOLD_X_POSITION).safeRoundToInt()
    val clampedX = x.coerceAtMost(maxX)
    return IntOffset(clampedX, 0)
}

private fun calculateRotationY(offset: Float, screenWidth: Float): Float {
    if (offset == 0f) return -THRESHOLD_Y_ROTATION.toFloat() // Fast path
    
    val safeOffset = offset.takeIf { it.isFinite() } ?: 0f
    val safeScreenWidth = screenWidth.takeIf { it.isFinite() && it > 0f } ?: FALLBACK_SCREEN_WIDTH
    val rotationRatio = safeOffset.safeDivision(safeScreenWidth)
    return -((THRESHOLD_Y_ROTATION * rotationRatio) + THRESHOLD_Y_ROTATION)
}

private fun calculateCornerRadius(offset: Float, screenWidth: Float, contentCornerSize: Dp): Dp {
    if (offset == 0f) return contentCornerSize // Fast path
    
    val safeOffset = offset.takeIf { it.isFinite() } ?: 0f
    val safeScreenWidth = screenWidth.takeIf { it.isFinite() && it > 0f } ?: FALLBACK_SCREEN_WIDTH
    val cornerRatio = safeOffset.safeDivision(safeScreenWidth)
    val cornerSize = contentCornerSize.value * (cornerRatio + 1f)

    return if (cornerSize.isFinite() && cornerSize >= 0f) {
        cornerSize.coerceAtLeast(0f).dp
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