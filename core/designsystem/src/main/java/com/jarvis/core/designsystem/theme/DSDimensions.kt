package com.jarvis.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val DimensionsNone = 0.dp
val DimensionsXXS = 2.dp
val DimensionsXS = 4.dp
val DimensionsS = 8.dp
val DimensionsM = 16.dp
val DimensionsL = 24.dp
val DimensionsXL = 32.dp
val DimensionsXXL = 40.dp
val DimensionsXXXL = 48.dp
val DimensionsXXXXL = 56.dp
val DimensionsXXXXXL = 64.dp
val DimensionsXXXXXXL = 80.dp
val DimensionsXXXXXXXL = 100.dp
val DimensionsXXXXXXXXL = 150.dp
val DimensionsXXXXXXXXXL = 200.dp

@Immutable
data class DSDimensions(
    val none: Dp = DimensionsNone,
    val xxs: Dp = DimensionsXXS,
    val xs: Dp = DimensionsXS,
    val s: Dp = DimensionsS,
    val m: Dp = DimensionsM,
    val l: Dp = DimensionsL,
    val xl: Dp = DimensionsXL,
    val xxl: Dp = DimensionsXXL,
    val xxxl: Dp = DimensionsXXXL,
    val xxxxl: Dp = DimensionsXXXXL,
    val xxxxxl: Dp = DimensionsXXXXXL,
    val xxxxxxl: Dp = DimensionsXXXXXXL,
    val xxxxxxxl: Dp = DimensionsXXXXXXXL,
    val xxxxxxxxl: Dp = DimensionsXXXXXXXXL,
    val xxxxxxxxxl: Dp = DimensionsXXXXXXXXXL
)

val LocalDSDimensions = staticCompositionLocalOf { DSDimensions() }