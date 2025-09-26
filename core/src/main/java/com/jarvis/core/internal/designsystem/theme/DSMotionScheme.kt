@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.core.internal.designsystem.theme

import androidx.annotation.RestrictTo

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

const val DefaultDuration = 300
const val FastDuration = 150
const val SlowDuration = 500

@Immutable
data class DSMotionScheme(
    val defaultSpec: FiniteAnimationSpec<Float> = spring(stiffness = Spring.StiffnessMediumLow),
    val fastSpec: FiniteAnimationSpec<Float> = tween(durationMillis = FastDuration),
    val slowSpec: FiniteAnimationSpec<Float> = tween(durationMillis = SlowDuration)
) {
    /**
     * Standard animation for most interactions.
     */
    fun default() = defaultSpec

    /**
     * Short, snappy animation for subtle interactions.
     */
    fun fast() = fastSpec

    /**
     * Longer animation for larger transitions (like screen changes).
     */
    fun slow() = slowSpec
}

val LocalDSMotionScheme = staticCompositionLocalOf { DSMotionScheme() }