@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.core.internal.designsystem.theme

import androidx.annotation.RestrictTo

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val Elevation0 = 0.dp
val Elevation1 = 2.dp
val Elevation2 = 6.dp
val Elevation3 = 12.dp
val Elevation4 = 24.dp
val Elevation5 = 31.dp

@Immutable
data class DSElevations(
    val none: Dp = Elevation0,
    val level1: Dp = Elevation1,
    val level2: Dp = Elevation2,
    val level3: Dp = Elevation3,
    val level4: Dp = Elevation4,
    val level5: Dp = Elevation5
)

val LocalDSElevations = staticCompositionLocalOf { DSElevations() }