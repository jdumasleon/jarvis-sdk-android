@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.core.internal.designsystem.theme

import androidx.annotation.RestrictTo

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

val ShapeNone = RoundedCornerShape(0.dp)
val ShapeXSmall = RoundedCornerShape(4.dp)
val ShapeSmall = RoundedCornerShape(8.dp)
val ShapeMedium = RoundedCornerShape(12.dp)
val ShapeLarge = RoundedCornerShape(16.dp)
val ShapeXLarge = RoundedCornerShape(30.dp)
val ShapeXXLarge = RoundedCornerShape(999.dp)

@Immutable
data class DSShape(
    val none: RoundedCornerShape = ShapeNone,
    val xs: RoundedCornerShape = ShapeXSmall,
    val s: RoundedCornerShape = ShapeSmall,
    val m: RoundedCornerShape = ShapeMedium,
    val l: RoundedCornerShape = ShapeLarge,
    val xl: RoundedCornerShape = ShapeXLarge,
    val xxl: RoundedCornerShape = ShapeXXLarge
)

val LocalDSShapes = staticCompositionLocalOf { DSShape() }