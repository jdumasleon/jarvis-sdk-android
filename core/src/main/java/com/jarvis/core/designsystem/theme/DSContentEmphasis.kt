package com.jarvis.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

import androidx.compose.ui.graphics.Color

const val EmphasisNormal = 1.0f
const val EmphasisMinor = 0.80f
const val EmphasisSubtle = 0.66f
const val EmphasisDisabled = 0.48f

@Immutable
data class DSContentEmphasis(
    val normal: Float = EmphasisNormal,
    val minor: Float = EmphasisMinor,
    val subtle: Float = EmphasisSubtle,
    val disabled: Float = EmphasisDisabled
)

val LocalDSContentEmphasis: ProvidableCompositionLocal<DSContentEmphasis> = staticCompositionLocalOf { DSContentEmphasis() }

@Composable
fun Color.applyEmphasis(emphasis: Float): Color = copy(alpha = emphasis)
