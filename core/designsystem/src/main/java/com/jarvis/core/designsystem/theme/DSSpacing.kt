package com.jarvis.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val NONE = 0.dp
val SpacingXXS = 2.dp
val SpacingXS = 4.dp
val SpacingS = 8.dp
val SpacingM = 16.dp
val SpacingL = 24.dp
val SpacingXL = 32.dp
val SpacingXXL = 40.dp
val SpacingXXXL = 48.dp
val SpacingXXXXL = 56.dp
val SpacingXXXXXL = 64.dp

@Immutable
data class DSSpacing(
    val none: Dp = NONE,
    val xxs: Dp = SpacingXXS,
    val xs: Dp = SpacingXS,
    val s: Dp = SpacingS,
    val m: Dp = SpacingM,
    val l: Dp = SpacingL,
    val xl: Dp = SpacingXL,
    val xxl: Dp = SpacingXXL,
    val xxxl: Dp = SpacingXXXL,
    val xxxxl: Dp = SpacingXXXXL,
    val xxxxxl: Dp = SpacingXXXXXL
)

val LocalDSSpacing = staticCompositionLocalOf { DSSpacing() }