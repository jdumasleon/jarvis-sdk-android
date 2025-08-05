package com.jarvis.api.detector

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlin.math.sqrt

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
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    
    companion object {
        private const val SHAKE_THRESHOLD = 800f
        private const val TIME_BETWEEN_UPDATES = 100L
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
                
                if (speed > SHAKE_THRESHOLD) {
                    onShakeDetected()
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