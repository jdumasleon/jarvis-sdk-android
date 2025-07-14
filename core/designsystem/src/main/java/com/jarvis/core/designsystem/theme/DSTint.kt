package com.jarvis.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * A class to model background color and tonal elevation values for Jarvis in light and dark theme.
 */
@Immutable
data class DSTintTheme(
    val iconTint: Color = Color.Unspecified,
)

/**
 * A composition local for [DSTintTheme].
 */
val LocalDSTintTheme = staticCompositionLocalOf { DSTintTheme() }