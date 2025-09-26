@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.core.internal.designsystem.theme

import androidx.annotation.RestrictTo

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val ThicknessXS = 1.dp
val ThicknessS = 2.dp
val RadiusXS = 4.dp
val RadiusS = 8.dp
val RadiusM = 16.dp
val RadiusL = 24.dp
val RadiusXL = 32.dp
val RadiusPill = 100.dp

@Immutable
data class DSBorder(
    val thickness: DSBorderThickness = DSBorderThickness(),
    val radius: DSRadius = DSRadius()
) {

    data class DSBorderThickness(
        val xs: Dp = ThicknessXS,
        val s: Dp = ThicknessS
    )

    data class DSRadius(
        val xs: Dp = RadiusXS,
        val s: Dp = RadiusS,
        val m: Dp = RadiusM,
        val l: Dp = RadiusL,
        val xl: Dp = RadiusXL,
        val pill: Dp = RadiusPill
    )
}

val LocalDSBorder = staticCompositionLocalOf { DSBorder() }