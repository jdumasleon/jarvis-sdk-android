package com.jarvis.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class DSTint(
    val iconTint: Color = Color.Unspecified,
)

val LocalDSTint = staticCompositionLocalOf { DSTint() }