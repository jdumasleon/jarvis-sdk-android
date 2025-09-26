package com.jarvis.core.internal.designsystem.utils

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.sync.Mutex
import org.junit.Test
import org.junit.Assert.*

class DSExtensionsTest {

    @Test
    fun `orientation opposite returns correct values`() {
        assertEquals(Orientation.Horizontal, Orientation.Vertical.opposite)
        assertEquals(Orientation.Vertical, Orientation.Horizontal.opposite)
    }

    @Test
    fun `offset getAxis returns correct axis value`() {
        val offset = Offset(10f, 20f)

        assertEquals(20f, offset.getAxis(Orientation.Vertical))
        assertEquals(10f, offset.getAxis(Orientation.Horizontal))
    }

    @Test
    fun `size getAxis returns correct axis value`() {
        val size = Size(100f, 200f)

        assertEquals(200f, size.getAxis(Orientation.Vertical))
        assertEquals(100f, size.getAxis(Orientation.Horizontal))
    }

    @Test
    fun `intOffset getAxis returns correct axis value`() {
        val intOffset = IntOffset(15, 25)

        assertEquals(25, intOffset.getAxis(Orientation.Vertical))
        assertEquals(15, intOffset.getAxis(Orientation.Horizontal))
    }

    @Test
    fun `intSize getAxis returns correct axis value`() {
        val intSize = IntSize(300, 400)

        assertEquals(400, intSize.getAxis(Orientation.Vertical))
        assertEquals(300, intSize.getAxis(Orientation.Horizontal))
    }

    @Test
    fun `offset fromAxis creates correct offset`() {
        val verticalOffset = Offset.fromAxis(Orientation.Vertical, 50f)
        val horizontalOffset = Offset.fromAxis(Orientation.Horizontal, 50f)

        assertEquals(Offset(0f, 50f), verticalOffset)
        assertEquals(Offset(50f, 0f), horizontalOffset)
    }

    @Test
    fun `offset onlyAxis preserves correct axis`() {
        val offset = Offset(10f, 20f)

        assertEquals(Offset(0f, 20f), offset.onlyAxis(Orientation.Vertical))
        assertEquals(Offset(10f, 0f), offset.onlyAxis(Orientation.Horizontal))
    }

    @Test
    fun `offset reverseAxis reverses correct axis`() {
        val offset = Offset(10f, 20f)

        assertEquals(Offset(10f, -20f), offset.reverseAxis(Orientation.Vertical))
        assertEquals(Offset(-10f, 20f), offset.reverseAxis(Orientation.Horizontal))
    }

    @Test
    fun `intOffset fromAxis creates correct intOffset`() {
        val verticalOffset = IntOffset.fromAxis(Orientation.Vertical, 50)
        val horizontalOffset = IntOffset.fromAxis(Orientation.Horizontal, 50)

        assertEquals(IntOffset(0, 50), verticalOffset)
        assertEquals(IntOffset(50, 0), horizontalOffset)
    }

    @Test
    fun `intSize fromAxis creates correct intSize`() {
        val verticalSize = IntSize.fromAxis(Orientation.Vertical, 100)
        val horizontalSize = IntSize.fromAxis(Orientation.Horizontal, 100)

        assertEquals(IntSize(0, 100), verticalSize)
        assertEquals(IntSize(100, 0), horizontalSize)
    }

    @Test
    fun `intSize toMySize converts correctly`() {
        val intSize = IntSize(150, 250)
        val expectedSize = Size(150f, 250f)

        assertEquals(expectedSize, intSize.toMySize())
    }

    @Test
    fun `offset plus size operations work correctly`() {
        val offset = Offset(10f, 20f)
        val size = Size(5f, 15f)

        assertEquals(Offset(15f, 35f), offset + size)
        assertEquals(Offset(5f, 5f), offset - size)
    }

    @Test
    fun `intOffset plus intSize operations work correctly`() {
        val intOffset = IntOffset(10, 20)
        val intSize = IntSize(5, 15)

        assertEquals(IntOffset(15, 35), intOffset + intSize)
        assertEquals(IntOffset(5, 5), intOffset - intSize)
    }

    @Test
    fun `mutex withTryLock executes block when lock acquired`() {
        val mutex = Mutex()
        var executed = false

        val result = mutex.withTryLock {
            executed = true
        }

        assertTrue(result)
        assertTrue(executed)
    }
}