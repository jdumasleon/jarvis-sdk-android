@file:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
package com.jarvis.core.internal.designsystem.utils

import androidx.annotation.RestrictTo

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.platform.LocalContext
import kotlin.math.sqrt

fun interface DragGestureDetector {
    suspend fun PointerInputScope.detect(
        onDragStart: (Offset) -> Unit,
        onDragEnd: () -> Unit,
        onDragCancel: () -> Unit,
        onDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit
    )

    object Press : DragGestureDetector {
        override suspend fun PointerInputScope.detect(
            onDragStart: (Offset) -> Unit,
            onDragEnd: () -> Unit,
            onDragCancel: () -> Unit,
            onDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit
        ) {
            detectDragGestures(onDragStart, onDragEnd, onDragCancel, onDrag)
        }
    }

    object LongPress : DragGestureDetector {
        override suspend fun PointerInputScope.detect(
            onDragStart: (Offset) -> Unit,
            onDragEnd: () -> Unit,
            onDragCancel: () -> Unit,
            onDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit
        ) {
            detectDragGesturesAfterLongPress(onDragStart, onDragEnd, onDragCancel, onDrag)
        }
    }
}

/**
 * Shake detector for activating Jarvis SDK
 * Detects device shake gestures using accelerometer
 */
class ShakeDetector(
    private val context: Context,
    private val onShakeDetected: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var lastUpdate: Long = 0
    private var lastShakeTime: Long = 0
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    private var shakeCount = 0
    private var firstShakeTime: Long = 0

    companion object {
        private const val SHAKE_THRESHOLD = 1000f // Good balance for shake detection
        private const val TIME_BETWEEN_UPDATES = 100L
        private const val SHAKE_COOLDOWN = 3000L // 3 seconds cooldown between shake events
        private const val REQUIRED_SHAKES = 2 // Require at least 2 shake movements
        private const val SHAKE_WINDOW = 1000L // 1 second window for multiple shakes
    }

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastUpdate > TIME_BETWEEN_UPDATES) {
                val timeDiff = currentTime - lastUpdate
                lastUpdate = currentTime

                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val speed = sqrt(
                    ((x - lastX) * (x - lastX) +
                            (y - lastY) * (y - lastY) +
                            (z - lastZ) * (z - lastZ)).toDouble()
                ).toFloat() / timeDiff * 10000

                // Check for shake pattern - require multiple shakes within a window
                if (speed > SHAKE_THRESHOLD) {
                    if (shakeCount == 0) {
                        // First shake detected
                        firstShakeTime = currentTime
                        shakeCount = 1
                    } else if (currentTime - firstShakeTime <= SHAKE_WINDOW) {
                        // Subsequent shake within window
                        shakeCount++

                        // Check if we have enough shakes and cooldown has passed
                        if (shakeCount >= REQUIRED_SHAKES && (currentTime - lastShakeTime) > SHAKE_COOLDOWN) {
                            lastShakeTime = currentTime
                            onShakeDetected()
                            // Reset shake counting
                            shakeCount = 0
                            firstShakeTime = 0
                        }
                    } else {
                        // Too much time passed, reset and start over
                        firstShakeTime = currentTime
                        shakeCount = 1
                    }
                } else {
                    // Reset if no significant movement after some time
                    if (currentTime - firstShakeTime > SHAKE_WINDOW && shakeCount > 0) {
                        shakeCount = 0
                        firstShakeTime = 0
                    }
                }

                lastX = x
                lastY = y
                lastZ = z
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No implementation needed
    }
}

/**
 * Composable that provides shake detection functionality
 */
@Composable
fun rememberShakeDetector(
    onShakeDetected: () -> Unit
): ShakeDetector {
    val context = LocalContext.current

    return remember(onShakeDetected) {
        ShakeDetector(context, onShakeDetected)
    }
}

/**
 * Effect that manages shake detector lifecycle
 */
@Composable
fun ShakeDetectorEffect(
    onShakeDetected: () -> Unit
) {
    val shakeDetector = rememberShakeDetector(onShakeDetected)

    DisposableEffect(shakeDetector) {
        shakeDetector.start()
        onDispose {
            shakeDetector.stop()
        }
    }
}