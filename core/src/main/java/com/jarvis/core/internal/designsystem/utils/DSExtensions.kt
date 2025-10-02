package com.jarvis.core.internal.designsystem.utils

import androidx.annotation.RestrictTo

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.sync.Mutex

@Composable
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun Dp.toPx() = with(LocalDensity.current) { this@toPx.toPx() }

@Composable
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun Int.toDp() = with(LocalDensity.current) { this@toDp.toDp() }

@Composable
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun Float.toDp() = with(LocalDensity.current) { this@toDp.toDp() }

@Stable
fun IntSize.toMySize() = Size(width.toFloat(), height.toFloat())

val Orientation.opposite
    get() = when (this) {
        Orientation.Vertical -> Orientation.Horizontal
        Orientation.Horizontal -> Orientation.Vertical
    }

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun Offset.getAxis(orientation: Orientation) = when (orientation) {
    Orientation.Vertical -> y
    Orientation.Horizontal -> x
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun Size.getAxis(orientation: Orientation) = when (orientation) {
    Orientation.Vertical -> height
    Orientation.Horizontal -> width
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun IntOffset.getAxis(orientation: Orientation) = when (orientation) {
    Orientation.Vertical -> y
    Orientation.Horizontal -> x
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun IntSize.getAxis(orientation: Orientation) = when (orientation) {
    Orientation.Vertical -> height
    Orientation.Horizontal -> width
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun Offset.Companion.fromAxis(orientation: Orientation, value: Float) =
    when (orientation) {
        Orientation.Vertical -> Offset(0f, value)
        Orientation.Horizontal -> Offset(value, 0f)
    }

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun Offset.onlyAxis(orientation: Orientation) =
    when (orientation) {
        Orientation.Vertical -> Offset(0f, y)
        Orientation.Horizontal -> Offset(x, 0f)
    }

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun Offset.reverseAxis(orientation: Orientation) =
    when (orientation) {
        Orientation.Vertical -> Offset(x, -y)
        Orientation.Horizontal -> Offset(-x, y)
    }

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun IntSize.Companion.fromAxis(orientation: Orientation, value: Float) =
    when (orientation) {
        Orientation.Vertical -> Size(0f, value)
        Orientation.Horizontal -> Size(value, 0f)
    }

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun IntOffset.Companion.fromAxis(orientation: Orientation, value: Int) =
    when (orientation) {
        Orientation.Vertical -> IntOffset(0, value)
        Orientation.Horizontal -> IntOffset(value, 0)
    }

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun IntSize.Companion.fromAxis(orientation: Orientation, value: Int) =
    when (orientation) {
        Orientation.Vertical -> IntSize(0, value)
        Orientation.Horizontal -> IntSize(value, 0)
    }

internal operator fun Offset.plus(size: Size) = Offset(x + size.width, y + size.height)
internal operator fun Offset.minus(size: Size) = Offset(x - size.width, y - size.height)

internal operator fun IntOffset.plus(size: IntSize) = IntOffset(x + size.width, y + size.height)
internal operator fun IntOffset.minus(size: IntSize) = IntOffset(x - size.width, y - size.height)


@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun Mutex.withTryLock(block: () -> Unit): Boolean {
    return if (tryLock()) {
        block()
        unlock()
        true
    } else {
        false
    }
}