package com.jarvis.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

import androidx.compose.ui.graphics.Color

@Immutable
data class DSContentEmphasis(
    val normal: Float = 1.0f,
    val minor: Float = 0.80f,
    val subtle: Float = 0.66f,
    val disabled: Float = 0.48f
)

val LocalDSContentEmphasis: ProvidableCompositionLocal<DSContentEmphasis> =
    staticCompositionLocalOf { DSContentEmphasis() }

@Composable
fun Color.applyEmphasis(emphasis: Float): Color = copy(alpha = emphasis)
